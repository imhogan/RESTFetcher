package au.com.thinkronicity.RestFetcher;

import au.com.thinkronicity.RestFetcher.AWS_S3_Helper;
import au.com.thinkronicity.RestFetcher.Configuration;
import au.com.thinkronicity.RestFetcher.REST_Walker;
import au.com.thinkronicity.RestFetcher.Utility;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class to recursively fetch and process XML (or JSON) data from a REST API in an AWS Lambda context. 
 * 
 * @author Ian Hogan, Ian_MacDonald_Hogan@yahoo.com
 *
 */
/**
 * @author Ian
 *
 */
public class Fetch
implements RequestHandler<Object, String> {
	
    /**
     * input from the Lamba invocation.
     */
    private LinkedHashMap<String, String> input = null;
    
    /**
     * Default debug mode.  
     */
    private static final boolean debug = true;
    
    /**
     * Version of this codebase.
     */
    private static final String version = "2.4.8CE";
    
    /**
     * Namespace for the Commands XML schema. 
     */
    private static final String commandsXmlNamespace = "http://THINKronicity.com.au/Schemas/Smirnov/RestFetcherCommands.xsd";
    
    /**
     * Current configuration.
     */
    private Configuration fetchConfig = null;
    
    /**
     * Event map XML - for S3 Event handler mode.
     */
    private Document eventMapXml = null;
    
    /**
     * Credentials for AWS calls.
     */
    AWSCredentials myCredentials = null;
    
    /**
     * Namespace map for Commands XML
     */
    private HashMap<String, String> commandsNamespaceMap = null;

    /**
     * Initialise local variables at start of event processing.
     * 
     *  This is required as AWS Lambda will instantiate the handler class and then send it 
     *  events as they arrive, until the handler resources are released when the container is 
     *  destroyed on timeout.
     */
    private void initialise() {
    	input = new LinkedHashMap<String, String>();
    	fetchConfig = null;
    	eventMapXml = null;
    }
    
    /**
     * getMyCredentials - get the AWS credentials - initialised on first used.
     * 
     * @return 			- AWS credentials.
     */
    public AWSCredentials getMyCredentials() {
        if (this.myCredentials == null) {
            try {
                this.myCredentials = new EnvironmentVariableCredentialsProvider().getCredentials();
            }
            catch (Exception e) {
                throw new AmazonClientException("Cannot load the credentials from the AWS_ACCESS_KEY_ID (or AWS_ACCESS_KEY) and AWS_SECRET_KEY (or AWS_SECRET_ACCESS_KEY) environment variables.", (Throwable)e);
            }
        }
        return this.myCredentials;
    }

    /**
     * processEventMap - process the given event map to work out how to handle an AWS S3 event. 
     * 
     * This gets any initial parameter values and the initial command.
     * 
     * @param triggerfileURI	- URI of the trigger file.
     * @param event				- S3 event.
     * @param eventMatchKey		- key of the S3 file containing the event map.
     */
    public void loadTriggerfileParameters(String triggerfileURI, String paramsXPath) {
        try {
            Document triggerXml = Utility.readXmlFromURI(triggerfileURI);
            NodeList triggerNodes = Utility.getNodesByXPath(triggerXml, paramsXPath, null);
            if (triggerNodes.getLength() > 0) {

                for (int tn = 0; tn < triggerNodes.getLength(); ++tn) {
                    Element triggerElement = (Element)triggerNodes.item(tn);
                    
                    this.input.put(triggerElement.getLocalName(), triggerElement.getTextContent());

                }
                return;
            }
            else {
                Utility.LogMessage("Warning: Could not find parameter elements in '" + triggerfileURI + "' using XPath '" + paramsXPath + "'.");
            }
        }
        catch (Exception e) {
            Utility.LogMessage(Utility.GetStackTrace(e));
        }
    }

    /**
     * processEventMap - process the given event map to work out how to handle an AWS S3 event. 
     * 
     * This gets any initial parameter values and the initial command.
     * 
     * @param eventMapURI		- URI of the event map file.
     * @param event				- S3 event.
     * @param eventMatchKey		- key of the S3 file containing the event map.
     */
    public void processEventMap(String eventMapURI, String event, String eventMatchKey) {
        try {
            this.eventMapXml = Utility.readXmlFromURI(eventMapURI);
            this.commandsNamespaceMap = new HashMap<String, String>();
            this.commandsNamespaceMap.put("cmd", commandsXmlNamespace);
            NodeList eventNodes = Utility.getNodesByXPath(this.eventMapXml, "//cmd:LambdaEvent[@Event='" + event + "']", this.commandsNamespaceMap);
            if (eventNodes.getLength() > 0) {
                for (int evt = 0; evt < eventNodes.getLength(); ++evt) {
                    Element eventElement = (Element)eventNodes.item(evt);
                    NodeList regExpNodes = Utility.getNodesByXPath(eventElement, "cmd:MatchKey/cmd:RegExp", this.commandsNamespaceMap);
                    if (regExpNodes.getLength() > 0) {
                        for (int re = 0; re < regExpNodes.getLength(); ++re) {
                            Element regExpElement = (Element)regExpNodes.item(re);
                            String expValue = regExpElement.getTextContent();
                            Matcher resolver = Pattern.compile(expValue).matcher(eventMatchKey);
                            if (resolver.matches()) {
                                Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(expValue);
                                while (m.find()) {
                                    String groupName = m.group(1);
                                    this.input.put(groupName, resolver.group(groupName));
                                }
                                HashMap<String, String> eventParameters = Utility.loadParameters(false, "../cmd:Parameters/cmd:Parameter", regExpElement, this.commandsNamespaceMap, this.input, null, false, debug);
                                NodeList matchNodes = Utility.getNodesByXPath(regExpElement, "..", this.commandsNamespaceMap);
                                this.input.put("command", ((Element)matchNodes.item(0)).getAttribute("Command"));
                                this.input.putAll(eventParameters);
                                return;
                            }
                        }
                    }
                }
            }
            else {
                throw new Exception("Could not find a map for event  '" + event + "' in '" + eventMapURI + "'.");
            }
        }
        catch (Exception e) {
            Utility.LogMessage(Utility.GetStackTrace(e));
        }
    }

    /**
     * createInputFromS3Event - create the input parameters from an S3 event.
     * 
     * @param s3event		- the event which triggered this invocation.
     * @param context		- the context of the invocation.
     * @return				- the input parameters.				
     * 
     * @throws IOException
     */
    public Object createInputFromS3Event(S3Event s3event, Context context) throws IOException {
        try {
        	LinkedHashMap<String, String> eventInput = new LinkedHashMap<String, String>();
            S3EventNotification.S3EventNotificationRecord record = (S3EventNotification.S3EventNotificationRecord)s3event.getRecords().get(0);
            String srcBucket = record.getS3().getBucket().getName();
            String srcKey = record.getS3().getObject().getKey().replace('+', ' ');
            srcKey = URLDecoder.decode(srcKey, "UTF-8");
            eventInput.put("S3_Bucket", srcBucket);
            eventInput.put("S3_Key", srcKey);
            eventInput.put("AWS_Event_Name", record.getEventName());
            eventInput.put("AWS_Event_Source", record.getEventSource());
            Utility.LogMessage("Processing event " + record.getEventName() + " from " + record.getEventSource() + " for s3://" + srcBucket + "/" + srcKey);
            String eventMappingURI = AWS_S3_Helper.resolveURI(this.getMyCredentials(), "s3://" + srcBucket + "/config/LambdaEventsMap.xml", 3600);
            if (!eventMappingURI.isEmpty()) {
                this.processEventMap(eventMappingURI, String.valueOf(record.getEventSource()) + ":" + record.getEventName(), srcKey);
                // Read extra event parameters from "s3://" + srcBucket + "/" + srcKey using XPath, if it is an XML file and the
                // input parameter triggerFileParamsXPath is not empty. Note if the default value is "/*/*" if the parameter is not present,
                // so an empty parameter is required to suppress this behaviour.
                String triggerfileXPath = this.input.getOrDefault("triggerFileParamsXPath", "/*/*");
                if (!triggerfileXPath.equals("") && srcKey.toUpperCase().endsWith(".XML")) {
                    String triggerfileURI = AWS_S3_Helper.resolveURI(this.getMyCredentials(), "s3://" + srcBucket +  "/" + srcKey, 3600);
                    this.loadTriggerfileParameters(triggerfileURI, triggerfileXPath);
                }
            } else {
                Utility.LogMessage("Could not resolve URI 's3://" + srcBucket + "/config/LambdaEventsMap.xml");
            }
            eventInput.putAll(this.input);
            return eventInput;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* handleRequest - default request handler for this Lambda function.
     * 
     * (non-Javadoc)
     * @see com.amazonaws.services.lambda.runtime.RequestHandler#handleRequest(java.lang.Object, com.amazonaws.services.lambda.runtime.Context)
     */
    public String handleRequest(Object input, Context context) {
    	this.initialise();
        Utility.setMyContext(context);
        Utility.LogMessage("RestFetcher - Version "+version);
        Utility.LogMessage("[INPUT] " + input);
        Utility.LogMessage("Handling an event of type " + input.getClass().getTypeName() + ".");
        String result = "";
        try {
            if (input instanceof LinkedHashMap && ((LinkedHashMap<?, ?>)input).containsKey("Records")) {
                JSONObject jsonData = new JSONObject(input.toString().replaceAll("(?i)([{ ])([a-z0-9\\-]+)=", "$1\"$2\":").replaceAll("(?i):([^,}\\[{]+)([,}])", ":\"$1\"$2"));
                String xmlContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n<root>" + XML.toString((Object)jsonData) + "</root>";
                context.getLogger().log("[xmlContent] " + xmlContent);
                Document eventDoc = Utility.readXmlFromString(xmlContent);
                String srcBucket = Utility.getNodeValueByXPath(eventDoc.getDocumentElement(), "//bucket//name", null, "");
                String srcKey = Utility.getNodeValueByXPath(eventDoc.getDocumentElement(), "//object//key", null, "");
                String eventName = Utility.getNodeValueByXPath(eventDoc.getDocumentElement(), "//eventName", null, "");
                String eventSource = Utility.getNodeValueByXPath(eventDoc.getDocumentElement(), "//eventSource", null, "");
                this.input.put("S3_Bucket", srcBucket);
                this.input.put("S3_Key", srcKey);
                this.input.put("AWS_Event_Name", eventName);
                this.input.put("AWS_Event_Source", eventSource);
                this.input.put("event_id", context.getAwsRequestId());
                this.input.put("function_name", context.getFunctionName());
                Utility.LogMessage("Processing event " + eventName + " from " + eventSource + " for s3://" + srcBucket + "/" + srcKey);
                String eventMappingURI = AWS_S3_Helper.resolveURI(this.getMyCredentials(), "s3://" + srcBucket + "/config/LambdaEventsMap.xml", 3600);
                if (!eventMappingURI.isEmpty()) {
                    this.processEventMap(eventMappingURI, String.valueOf(eventSource) + ":" + eventName, srcKey);
                    // Read extra event parameters from "s3://" + srcBucket + "/" + srcKey using XPath, if it is an XML file and the
                    // input parameter triggerFileParamsXPath is not empty. Note if the default value is "/*/*" if the parameter is not present,
                    // so an empty parameter is required to suppress this behaviour.
                    String triggerfileXPath = this.input.getOrDefault("triggerFileParamsXPath", "/*/*");
                    if (!triggerfileXPath.equals("") && srcKey.toUpperCase().endsWith(".XML")) {
                        String triggerfileURI = AWS_S3_Helper.resolveURI(this.getMyCredentials(), "s3://" + srcBucket +  "/" + srcKey, 3600);
                        this.loadTriggerfileParameters(triggerfileURI, triggerfileXPath);
                    }
                } else {
                    Utility.LogMessage("Could not resolve URI 's3://" + srcBucket + "/config/LambdaEventsMap.xml");
                }
                this.fetchConfig = new Configuration("Fetch", this.input);
            }
            if (this.fetchConfig == null) {
                this.fetchConfig = new Configuration("Fetch", input);
                this.fetchConfig.parameters.put("event_id", context.getAwsRequestId());
                this.fetchConfig.parameters.put("function_name", context.getFunctionName());
            }
            if (!this.fetchConfig.commandsURI.isEmpty()) {
                Utility.setFopBaseURI(this.fetchConfig.getParameter("FOP_BASE_URI", Utility.getFopBaseURI()));
                Utility.setFopConfig(this.fetchConfig.getParameter("FOP_CONFIG", Utility.getFopConfig()));
                
                if (this.fetchConfig.getParameter("ENABLE_SSL_LOGGING", "false").toLowerCase().equals("true")) {
                    System.setProperty("javax.net.debug","all");
                }
                
                REST_Walker walker = new REST_Walker(this.fetchConfig);
                result = walker.walkServices();
            } else {
                result = "Nothing to do - no commandsURI provided!";
            }
        }
        catch (MalformedURLException e) {
            result = Utility.GetStackTrace(e);
        }
        catch (IOException e) {
            result = Utility.GetStackTrace(e);
        }
        catch (Exception e) {
            result = Utility.GetStackTrace(e);
        }
        Utility.LogMessage("Result is '" + result + "'");
        return result;
    }

    /**
     * handleRequest - Handle an S3 event.
     * 
     * @param s3event		- the event which triggered this invocation.
     * @param context		- the context of the invocation.
     * @return				- the result of the request.
     */
    public String handleRequest(S3Event s3event, Context context) {
        Utility.setMyContext(context);
        Utility.LogMessage("Handling an S3Event");
        try {
            return this.handleRequest(this.createInputFromS3Event(s3event, context), context);
        }
        catch (IOException e) {
            Utility.LogMessage(Utility.GetStackTrace(e));
            return "";
        }
    }
}
