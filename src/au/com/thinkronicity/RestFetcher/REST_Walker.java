/*
 * Decompiled with CFR 0_113.
 * 
 * Could not load the following classes:
 *  com.amazonaws.AmazonClientException
 *  com.amazonaws.auth.AWSCredentials
 *  com.amazonaws.auth.EnvironmentVariableCredentialsProvider
 *  com.amazonaws.regions.Regions
 *  com.amazonaws.services.s3.model.CannedAccessControlList
 */
package au.com.thinkronicity.RestFetcher;

import au.com.thinkronicity.RestFetcher.AWS_S3_Helper;
import au.com.thinkronicity.RestFetcher.Configuration;
import au.com.thinkronicity.RestFetcher.EmailHelper;
import au.com.thinkronicity.RestFetcher.Utility;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.CannedAccessControlList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HttpsURLConnection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * Class to "walk" a REST API, that is it calls an initial method of the API and processes the resultant XML response, 
 * possibly converted from JSON to XML, and calls one or more methods. Results can be transformed and/or stored, and 
 * converted to PDF via XSL:FO, and emails can be sent to deliver PDF or other reports.
 * 
 * @author Ian Hogan, Ian_MacDonald_Hogan@yahoo.com
 *
 */
public class REST_Walker {
    /**
     * Namespace for the Commands XML schema. 
     */
    private static final String commandsXmlNamespace = "http://THINKronicity.com.au/Schemas/Smirnov/RestFetcherCommands.xsd";
    
    /**
     * The configuration for the journey.
     */
    private Configuration walkerConfig = null;
    
    /**
     * The commands to be performed.
     */
    private Document commandsXml = null;
    
    /**
     * The results of the journey
     */
    private String walkResults = "";
    
    /**
     * Credentials to access AWS resources. 
     */
    AWSCredentials myCredentials = null;
    
    /**
     * Namespace map for Commands XML
     */
    private HashMap<String, String> commandsNamespaceMap = null;

    /**
     * getMyCredentials - get the AWS credentials - initialised on first used.
     * 
     * @return 			- AWS credentials.
     */
    public AWSCredentials getMyCredentials() {
        if (this.myCredentials == null) {
            try {
                this.myCredentials = new DefaultAWSCredentialsProviderChain().getCredentials(); // EnvironmentVariableCredentialsProvider().getCredentials();
            }
            catch (Exception e) {
                throw new AmazonClientException("Cannot load the credentials from the AWS_ACCESS_KEY_ID (or AWS_ACCESS_KEY) and AWS_SECRET_KEY (or AWS_SECRET_ACCESS_KEY) environment variables.", (Throwable)e);
            }
        }
        return this.myCredentials;
    }

    /**
     * Constructor
     * 
     * @param appConfig - the configuration for the walker.
     */
    REST_Walker(Configuration appConfig) {
        try {
            this.walkerConfig = appConfig;
            this.commandsXml = Utility.readXmlFromURI(this.walkerConfig.commandsURI);
            this.commandsNamespaceMap = new HashMap<String, String>();
            this.commandsNamespaceMap.put("cmd", commandsXmlNamespace);
            
            // Add BouncyCastleProvider as openjdk runtime does not handle SSL security with EC-based cipher suites
            Security.addProvider(new BouncyCastleProvider());
        }
        catch (Exception e) {
            Utility.LogMessage(Utility.GetStackTrace(e));
        }
    }

    /**
     * walkServices - the initial entry point - start "walking" by issuing the initial command to the REST API.
     * 
     * @return - the results message which was built during the walk.
     * 
     * @throws Exception
     */
    public String walkServices() throws Exception {
        this.walkServices(this.walkerConfig.commandName, this.walkerConfig.parameters, 0);
        return this.walkResults;
    }

    /**
     * walkServices - issue the command using the supplied parameters. 
     * 
     * @param commandName	- the command to issue to the REST API.
     * @param parameters	- collection of parameters to send with the command.
     * @param currentDepth	- current depth of the hierarchical journey - used to prevent infinite recursion.
     * @return				- the XML document resulting from the actions applied to the XML returned by the REST API.
     * 
     * @throws Exception
     */
    private Document walkServices(String commandName, HashMap<String, String> parameters, Integer currentDepth) throws Exception {
        Document restDocument = null;
        Document walkDocument = null;
        
        // Log start if required.
        if (this.walkerConfig.debug || this.walkerConfig.verbose) {
            Utility.LogMessage("Processing command " + commandName + ".");
        }
        
        // Check for depth exceeded.
        if (this.walkerConfig.depthLimit != -1 && currentDepth > this.walkerConfig.depthLimit) {
            throw new Exception("walkServices - depth limit of " + this.walkerConfig.depthLimit + " exceeded!");
        }
        
        try {
        	
        	// Get the XML element for the given command name, or the first command element if the name is missing.
            Element commandElement = this.getCommandElement(commandName);
            
            // Save the command name if it was not supplied.
            if (commandName.isEmpty()) {
                commandName = commandElement.getAttribute("Name");
            }
            
            if (commandElement.getAttribute("IsNOP").equals("true")) {
            	
                // In the case of the NOP (No OPeration) command, just return the Commands XML document itself. 
                restDocument = this.commandsXml;
            } else {
                
                // Get the default endpoint
                String endpoint = this.walkerConfig.serviceEndPoint;
                
                NodeList endpointNodes = Utility.getNodesByXPath(commandElement, "cmd:Endpoint", this.commandsNamespaceMap);
                if (endpointNodes.getLength() == 1) {
                    
                    endpoint = Utility.getParameterValue("Endpoint", (Element)endpointNodes.item(0), this.commandsNamespaceMap, parameters, this.commandsXml.getDocumentElement(), this.walkerConfig.verbose, this.walkerConfig.debug);
                }
            	
            	// Generate the URL for the command using the supplied parameters.
                String commandURL = this.makeCommandURL(commandName, endpoint, parameters);
                URL url = new URL(commandURL);
                
                // Create a connection for the URL.
                HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
                
                // Set the timeout
                if (commandElement.hasAttribute("TimeOut")) {
                    urlConnection.setConnectTimeout(Integer.parseInt(commandElement.getAttribute("TimeOut")));
                } else {
                    urlConnection.setConnectTimeout(this.walkerConfig.serviceTimeout);
                }
                
                // Add the headers to the connection.
                HashMap<String, String> commandHeaders = Utility.loadParameters(false, "cmd:Headers/cmd:Header", commandElement, this.commandsNamespaceMap, parameters, null, this.walkerConfig.verbose, this.walkerConfig.debug);
                for (Map.Entry<String, String> entry : commandHeaders.entrySet()) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                    if (!this.walkerConfig.debug && !this.walkerConfig.verbose) continue;
                    Utility.LogMessage("Header " + entry.getKey() + "=" + entry.getValue());
                }
                
                // Set the verb for the connection.
                String httpMethod = commandElement.getAttribute("Verb");
                if (httpMethod.isEmpty()) {
                    httpMethod = "GET";
                }
                urlConnection.setRequestMethod(httpMethod);
                
                // Report the verb
                if (this.walkerConfig.debug || this.walkerConfig.verbose) {
                    Utility.LogMessage("HTTP Method is " + httpMethod);
                }
                
                
                urlConnection.setDoInput(true);
                
                // Add the body for the command, if there is one.
                NodeList bodyNodes = Utility.getNodesByXPath(commandElement, "cmd:Body", this.commandsNamespaceMap);
                if (bodyNodes.getLength() == 1) {
                    Element bodyElement = (Element)bodyNodes.item(0);
                    String contentType = this.walkerConfig.contentType;
                    if (bodyElement.hasAttribute("ContentType")) {
                       contentType = bodyElement.getAttribute("ContentType");
                    } 
                    
                    urlConnection.setRequestProperty("Content-Type", contentType);
                    urlConnection.setDoOutput(true);
                    OutputStream os = urlConnection.getOutputStream();
                    String bodyValue = Utility.getParameterValue("Body", bodyElement, this.commandsNamespaceMap, parameters, this.commandsXml.getDocumentElement(), this.walkerConfig.verbose, this.walkerConfig.debug);
                    if (this.walkerConfig.debug || this.walkerConfig.verbose) {
                        Utility.LogMessage("body of type '"+contentType+"' is " + bodyValue);
                    }
                    os.write(bodyValue.getBytes("UTF-8"));
                    os.close();
                } else {
                    if (bodyNodes.getLength() > 1) {
                        throw new Exception("TODO: Implement multi-part body support!");
                    }
                    if (httpMethod.equals("POST") && (this.walkerConfig.debug || this.walkerConfig.verbose)) {
                        Utility.LogMessage("Warning - no body to post!");
                    }
                }
                
                // Get the result from the command response
                
                if (commandElement.getAttribute("NoResultBody").toLowerCase().equals("true") || urlConnection.getContentLengthLong() == 0) {
                    
                    restDocument = Utility.readXmlFromString("<HTTP_Response><Code>"+(new Integer(urlConnection.getResponseCode())).toString()+"</Code><Message>"+urlConnection.getResponseMessage()+"</Message></HTTP_Response>");
                    
                }
                else {
                    restDocument = Utility.readXmlFromStream(urlConnection.getInputStream(), commandElement.getAttribute("IsJSON").toLowerCase().equals("true"), commandElement.getAttribute("UseJSON2SafeXML").toLowerCase().equals("true"));
                }

                String responseXmlFileName = this.walkerConfig.configurationProperties.getProperty("service.responseXml", "");
                if (!responseXmlFileName.equals("")) {
                    Utility.writeXmlDocument2File(restDocument, responseXmlFileName);
                    if (this.walkerConfig.debug || this.walkerConfig.verbose) {
                        Utility.LogMessage("Wrote results to " + responseXmlFileName);
                    }
                }
                if (this.walkerConfig.debug || this.walkerConfig.verbose) {
                    Utility.LogMessage("Received " + Utility.writeXmlToString(restDocument));
                }
            }
            
            // Apply the actions for this command to the results returned from the REST API.
            // This will typically invoke further commands, hence the hierarchical nature of the walk.
            walkDocument = this.doActions(commandElement, ".", parameters, restDocument.getDocumentElement(), currentDepth);
        }
        catch (Exception e) {
            throw new Exception("walkServices threw Exception " + e.getMessage(), e);
        }
        
        // Return the results of the walk.
        return walkDocument;
    }

    /**
     * doActions - do the actions associated with a command.
     * 
     * @param actionsBaseElement	- the command element with the actions to perform.
     * @param actionsBasePath		- the path to the actions containing element - typically just ".".
     * @param contextParameters		- the current collection of context parameters.
     * @param contextElement		- the context element in the API result document.
     * @param currentDepth			- current depth of the hierarchical journey - used to prevent infinite recursion.
     * @return						- the XML document resulting from the actions performed.
     * 
     * @throws Exception
     */
    private Document doActions(
    		 Element actionsBaseElement
    		,String actionsBasePath
    		,HashMap<String, String> contextParameters
    		,Element contextElement
    		,Integer currentDepth
    	) throws Exception {
    	
    	// The number of actions performed - for logging.
        int actions = 0;
        
        // Initialize the result document.
        Document actionDocument = null;
        
        // Get the set of actions to perform.
        NodeList actionNodes = Utility.getNodesByXPath(actionsBaseElement, String.valueOf(actionsBasePath) + "/cmd:Actions/cmd:Action", this.commandsNamespaceMap);
        
        
        if (actionNodes.getLength() == 0) {
        	
            // No actions - just return the context element's document.
            actionDocument = contextElement.getOwnerDocument();
        }
        else {
            
        	// Process each action.
        	for (int action = 0; action < actionNodes.getLength(); ++action) {
        		
        		// Get the action element
                Element actionElement = (Element)actionNodes.item(action);
                
                String actionMatch = actionElement.getAttribute("Match");
                if (!actionMatch.equals("")) {

                	// If a Match attribute is supplied, then the actions are performed for each element found by evaluating this as an XPath expression
                	// applied to the context element. For example a REST API may return a list of user id's and you may want to get details for 
                	// each user from the REST API, so the Match element would specify an XPath expression which would return all the user id nodes
                	// and the action would specify how to get the user details given a user id node.
                	// All API call results will be added as child elements to the result document root node.
                	
                	// Get the name for the root node.
                    String rootElementName = actionElement.getAttribute("Root");
                    if (rootElementName.equals("")) {
                        rootElementName = "Results";
                    }
                    
                    // Create the result document with just the root node.
                    actionDocument = Utility.readXmlFromString("<" + rootElementName + "/>");
                    
                    NodeList matchedNodes = Utility.getNodesByXPath(contextElement, actionMatch, this.commandsNamespaceMap);
                    if (matchedNodes.getLength() > 0) {
                    	
                        // Apply the action to each matched node.
                        for (int match = 0; match < matchedNodes.getLength(); ++match) {
                            Element matchElement = (Element)matchedNodes.item(match);
                            
                            if (this.walkerConfig.debug) {
                                Utility.LogMessage("Match [" + match + "]: " + matchElement.getNodeName());
                            }
                            
                            // Get the parameters for the action and perform the action.
                            HashMap<String, String> actionParameters = Utility.loadParameters(true, "cmd:Parameters/cmd:Parameter", actionElement, this.commandsNamespaceMap, contextParameters, matchElement, this.walkerConfig.verbose, this.walkerConfig.debug);
                            Document actionResult = this.doAction(actionElement, actionParameters, contextElement, currentDepth);
                            
                            // Add the results from this action to the results list.
                            actionDocument.getDocumentElement().appendChild(actionDocument.importNode(actionResult.getDocumentElement(), true));
                        }
                        
                        // Perform any child actions of this current action, and use the results as the current results of the actions list.
                        actionDocument = this.doActions(actionElement, ".", contextParameters, actionDocument.getDocumentElement(), currentDepth);
                    }
                } else {
                	
                    // Get the parameters for the action and perform the action.
                    HashMap<String, String> actionParameters = Utility.loadParameters(true, "cmd:Parameters/cmd:Parameter", actionElement, this.commandsNamespaceMap, contextParameters, contextElement, this.walkerConfig.verbose, this.walkerConfig.debug);
                    actionDocument = this.doAction(actionElement, actionParameters, contextElement, currentDepth);
                    
                    
                    // Perform any child actions of this current action, and use the results as the current results of the actions list.
                    actionDocument = this.doActions(actionElement, ".", actionParameters, actionDocument.getDocumentElement(), currentDepth);
                }
                
                // Count the actions.
                ++actions;
            }
        }

        // Log progress if required.
        if (this.walkerConfig.debug || this.walkerConfig.verbose) {
            String nodeName = actionsBaseElement.getAttribute("Name");
            if (nodeName.isEmpty()) {
                nodeName = actionsBaseElement.getAttribute("Type");
            }
            Utility.LogMessage("Performed " + actions + " actions for " + actionsBaseElement.getNodeName() + " " + nodeName + ".");
        }
        
        // Return the result document.
        return actionDocument;
    }

    /**
     * doAction - do an individual action.
     * 
     * @param actionElement			- The action element.			
     * @param actionParameters		- the collection of parameters for the action.
     * @param contextElement		- the context element in the API result document.
     * @param currentDepth			- current depth of the hierarchical journey - used to prevent infinite recursion.
     * @return						- the XML document resulting from the action performed.
     * 
     * @throws Exception
     */
    private Document doAction(Element actionElement, HashMap<String, String> actionParameters, Element contextElement, Integer currentDepth) throws Exception {
    	
    	
        // Initialize the result to the context element's document.
        Document actionDocument = contextElement.getOwnerDocument();
        
        // Get the type and mode of the action.
        String actionType = actionElement.getAttribute("Type");
        String actionMode = actionElement.getAttribute("Mode");
        
        // Skip if mode is not matched. This allows for actions to be only performed in debug mode for example.
        if (actionMode.equals("verbose") && !this.walkerConfig.verbose || actionMode.equals("debug") && !this.walkerConfig.debug) {
            Utility.LogMessage("Skipping action " + actionType + " as Mode " + actionMode + " is not matched.");
        }
        else if (actionMode.equals("!verbose") && this.walkerConfig.verbose || actionMode.equals("!debug") && this.walkerConfig.debug) {
            Utility.LogMessage("Skipping action " + actionType + " as Mode " + actionMode + " is not matched.");
        }
        else if (actionType.equals("Command")) {

        	// For a command action, we call recurse through walkServices. 
            actionDocument =  this.walkServices(actionElement.getAttribute("Command"), actionParameters, currentDepth + 1);
        }
        else if (actionType.equals("AddResult")) {

        	// For add result action we add all or selected action parameter values to the walk results buffer.
        	
        	// If a ParametersRegExp is supplied, then only parameters whose name matches this expression will be included.
            String paramRegExp = actionElement.getAttribute("ParametersRegExp");
            Pattern paramPattern = null;
            if (!paramRegExp.isEmpty()) {
                paramPattern = Pattern.compile(paramRegExp);
            }
            
            // Process all the parameters.
            for (Map.Entry<String, String> entry : actionParameters.entrySet()) {
                if (this.walkerConfig.debug || this.walkerConfig.verbose) {
                    Utility.LogMessage("AddResult Parameter:  " + entry.getKey() + "=" + entry.getValue());
                }
                if (paramPattern == null || paramPattern.matcher(entry.getKey()).matches()) {
	                this.walkResults = this.walkResults + entry.getKey() + ": " + entry.getValue() + "\n";
	                if (this.walkerConfig.debug || this.walkerConfig.verbose) {
	                	Utility.LogMessage("AddResult:  " + entry.getKey() + "=" + entry.getValue());
	                }
                }
            }
        }
        else if (actionType.equals("Output")) {
        	
        	// Output the context document, or the contents of a specified URI, to a specified file.
        	
        	// Validate parameters.
            String missingParams = this.missingParameters("Name", actionParameters);
            if (!missingParams.isEmpty()) {
                throw new Exception("Action with Type Output is missing required parameters " + missingParams + "!");
            }
            
            // Create the folder if required.
            String folder = "";
            if (actionParameters.containsKey("Folder")) {
                folder = actionParameters.get("Folder");
                File theDir = new File(folder);
                try {
                    if (!theDir.exists() && !theDir.mkdirs()) {
                        throw new Exception("Could not create folder " + folder);
                    }
                }
                catch (Exception e) {
                    throw new Exception("Could not create folder " + folder, e);
                }
            }
            
            // Create the file.
            String fileName = actionParameters.get("Name");
            fileName = actionParameters.containsKey("Extension") ? String.valueOf(fileName) + actionParameters.get("Extension") : String.valueOf(fileName) + ".xml";
            File outputFile = new File(folder, fileName);
            if (this.walkerConfig.debug || this.walkerConfig.verbose) {
                Utility.LogMessage("Writing file " + outputFile.getPath());
            }
            
            // Output the contents of the source URI or the context document.
            if (actionParameters.containsKey("SourceURI")) {
                URI u = URI.create(actionParameters.get("SourceURI"));
                InputStream in = u.toURL().openStream();
                Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                in.close();
            } else {
                Utility.writeXmlDocument2File(contextElement.getOwnerDocument(), outputFile.getPath());
            }
            
            if (this.walkerConfig.debug) {
            	Utility.LogMessage("Wrote file '"+outputFile.getPath()+"'");
            }
        }
        else if (actionType.equals("S3PUT")) {
        	
        	// Write the context document, or a specified body content, to an S3 file.
        	
        	// Validate parameters.
            String missingParams = this.missingParameters("Bucket,Name", actionParameters);
            if (!missingParams.isEmpty()) {
                throw new Exception("Action with Type S3PUT is missing required parameters " + missingParams + "!");
            }
            
            // Get the file permissions. 
            CannedAccessControlList acl = CannedAccessControlList.Private;
            if (actionParameters.containsKey("ACL")) {
                acl = CannedAccessControlList.valueOf((String)actionParameters.get("ACL"));
            }
            
            // Get the content type.
            String contentType = "application/xml";
            if (actionParameters.containsKey("ContentType")) {
                contentType = actionParameters.get("ContentType");
            }
            
            // Get the content.
            Document outputDocument = null;
            NodeList bodyNodes = Utility.getNodesByXPath(actionElement, "cmd:Body", this.commandsNamespaceMap);
            // actionElement.getElementsByTagNameNS(commandsXmlNamespace, "Body");
            
            // Log information if required.
            if (this.walkerConfig.debug) {
	            Utility.LogMessage("S3PUT found " + (new Integer(bodyNodes.getLength())).toString() + " Body nodes.");
            }

            if (bodyNodes.getLength() == 1) {
                Element bodyElement = (Element)bodyNodes.item(0);
                
                if (bodyElement.hasAttribute("ContentType")) {
                    contentType = bodyElement.getAttribute("ContentType");
                }

                String bodyContent = Utility.getParameterValue("Body", bodyElement, this.commandsNamespaceMap, actionParameters, contextElement, this.walkerConfig.verbose, this.walkerConfig.debug);
                outputDocument = Utility.readXmlFromString(bodyContent);
            } else {
                outputDocument = contextElement.getOwnerDocument();
            }
            
            // Write the content to the S3 file.
            String s3FileURL = AWS_S3_Helper.writeStreamToS3File(this.getMyCredentials(), actionParameters.get("Bucket"), actionParameters.get("Name"), Utility.outputStreamFromDocument(outputDocument), contentType, acl);
            
            // Log information if required.
            if (this.walkerConfig.debug) {
	            Utility.LogMessage("S3PUT to '" + s3FileURL + "'.");
            }
        }
        else if (actionType.equals("S3DELETE")) {
        	
        	// Delete a specified S3 file.
        	
        	// Validate parameters.
            String missingParams = this.missingParameters("Bucket,Name", actionParameters);
            if (!missingParams.isEmpty()) {
                throw new Exception("Action with Type S3DELETE is missing required parameters " + missingParams + "!");
            }
            
            // Delete the file.
            String s3FileDeleteResult = AWS_S3_Helper.deleteS3File(this.getMyCredentials(), actionParameters.get("Bucket"), actionParameters.get("Name"));
            
            // Log the result.
            Utility.LogMessage(s3FileDeleteResult);
        }
        else if (actionType.equals("XSLT")) {
        	
        	// Transform the context document, using the XSLT1 transform in the action element's Transform attribute or in the parameter named Transform. 
            String transformURI = "";
            if (actionElement.hasAttribute("Transform")) {
                transformURI = actionElement.getAttribute("Transform");
            } 
            else if (actionParameters.containsKey("Transform")){
                transformURI = actionParameters.get("Transform");
            }
            else {
                 throw new Exception("Action with Type XSLT requires a Transform parameter or attribute!");
            }
            
            actionDocument =  Utility.xslTransform(contextElement.getOwnerDocument(), transformURI, actionParameters, this.walkerConfig.debug || this.walkerConfig.verbose);
        }
        else if (actionType.equals("XSLT2")) {

        	// Transform the context document, using the XSLT2 transform in the action element's Transform attribute or in the parameter named Transform. 
            String transformURI = "";
            if (actionElement.hasAttribute("Transform")) {
                transformURI = actionElement.getAttribute("Transform");
            } 
            else if (actionParameters.containsKey("Transform")){
                transformURI = actionParameters.get("Transform");
            }
            else {
                 throw new Exception("Action with Type XSLT2 requires a Transform parameter or attribute!");
            }

            actionDocument =  Utility.xsl2Transform(contextElement.getOwnerDocument(), transformURI, actionParameters, this.walkerConfig.debug || this.walkerConfig.verbose);
        }
        else if (actionType.equals("FOP")) {
        	
        	// Transform the context document to PDF and store the result as an S3 file, using the XSLT2 transform in the action element's Transform attribute or in the parameter named Transform. 
            String transformURI = "";
            if (actionElement.hasAttribute("Transform")) {
                transformURI = actionElement.getAttribute("Transform");
            } 
            else if (actionParameters.containsKey("Transform")){
                transformURI = actionParameters.get("Transform");
            }
            else {
                 throw new Exception("Action with Type FOP requires a Transform parameter or attribute!");
            }

            // Validate other parameters.
            String missingParams = this.missingParameters("Bucket,Name", actionParameters);
            if (!missingParams.isEmpty()) {
                throw new Exception("Action with Type FOP is missing required parameters " + missingParams + "!");
            }
            
            // Do the transform
            String s3URL = Utility.xmlToS3PDF(this.getMyCredentials(), contextElement.getOwnerDocument(), transformURI, actionParameters, actionParameters.get("Bucket"), actionParameters.get("Name"), this.walkerConfig.debug != false || this.walkerConfig.verbose != false);

            if (this.walkerConfig.debug) {
	            Utility.LogMessage("S3PUT to '" + s3URL + "'.");
            }

            // The results document specifies the URL of the S3 file.
            actionDocument =  Utility.readXmlFromString("<Result><OutputURL><![CDATA[" + s3URL + "]]></OutputURL></Result>");
        }
        else if (actionType.equals("eMail")) {
        	
        	// Send an email using AWS SES using the supplied body.
        	
        	// Validate the parameters.
            String missingParams = this.missingParameters("From,To,Subject", actionParameters);
            if (!missingParams.isEmpty()) {
                throw new Exception("Action with Type eMail is missing required parameters " + missingParams + "!");
            }
            
            // Get the email body.
            String bodyType = "Text";
            String bodyContent = actionParameters.get("Body");
            NodeList bodyNodes = actionElement.getElementsByTagNameNS(commandsXmlNamespace, "Body");
            if (bodyNodes.getLength() == 1) {
                Element bodyElement = (Element)bodyNodes.item(0);
                if (bodyElement.hasAttribute("ContentType")) {
                    bodyType = bodyElement.getAttribute("ContentType");
                }
                bodyContent = Utility.getParameterValue("Body", bodyElement, this.commandsNamespaceMap, actionParameters, contextElement, this.walkerConfig.verbose, this.walkerConfig.debug);
            }
            
            // Get the AWS region to send email through
            String regionName = this.walkerConfig.getParameter("AWS.SES_Region");
            if (actionParameters.containsKey("SES_Region")) {
                regionName = actionParameters.get("SES_Region");
            }
            if (regionName.isEmpty()) {
                throw new Exception("An SES Region Name must be specified in the configuration file or as parameter SES_Region for action eMail!");
            }
            if (this.walkerConfig.debug) {
                Utility.LogMessage("SES Region is '" + regionName + "'.");
            }
            
            // Send the email.
            EmailHelper.sendEmailViaSES(
                this.getMyCredentials()
                , actionParameters.get("From")
                , actionParameters.get("ReplyTo")
                , actionParameters.get("Attachments")
                , actionParameters.get("Subject")
                , bodyContent
                , bodyType
                , actionParameters.get("To")
                , actionParameters.get("CC")
                , actionParameters.get("BCC")
                , Regions.fromName((String)regionName)
                , this.walkerConfig.debug || this.walkerConfig.verbose
            );
            
            // The result document provides the sent email details.
            actionDocument =  Utility.readXmlFromString(Utility.replaceParameters("<Result><EmailLog><From><![CDATA[${From}]]></From><To><![CDATA[${To}]]></To><ReplyTo><![CDATA[${ReplyTo}]]></ReplyTo><Subject><![CDATA[${Subject}]]></Subject><Body><![CDATA[" + bodyContent + "]]></Body>" + "</EmailLog></Result>", actionParameters));
        }
        else if (actionType.equals("ZIP")) {
        	
        	// Send an email using AWS SES using the supplied body.
        	
        	// Validate the parameters.
            String missingParams = this.missingParameters("Name,SourceFiles", actionParameters);
            if (!missingParams.isEmpty()) {
                throw new Exception("Action with Type ZIP is missing required parameters " + missingParams + "!");
            }
            
            // Create the folder if required.
            String folder = "";
            if (actionParameters.containsKey("Folder")) {
                folder = actionParameters.get("Folder");
                File theDir = new File(folder);
                try {
                    if (!theDir.exists() && !theDir.mkdirs()) {
                        throw new Exception("Could not create folder " + folder);
                    }
                }
                catch (Exception e) {
                    throw new Exception("Could not create folder " + folder, e);
                }
            }
            
            // Create the file.
            String fileName = actionParameters.get("Name");
            fileName = actionParameters.containsKey("Extension") ? String.valueOf(fileName) + actionParameters.get("Extension") : String.valueOf(fileName) + ".xml";
            File outputFile = new File(folder, fileName);
            if (this.walkerConfig.debug || this.walkerConfig.verbose) {
                Utility.LogMessage("Writing file " + outputFile.getPath());
            }

            // Get the zip files.
            String zipFilesList = actionParameters.get("SourceFiles");
            
            // Zip Output - to local file 
            FileOutputStream fos = new FileOutputStream(outputFile.getPath());
            ZipOutputStream zos = new ZipOutputStream(fos);

            try {
                //create byte buffer
                byte[] buffer = new byte[1024];

                String[] zipFiles = zipFilesList.split(";");
                
                for (int a = 0; a < zipFiles.length; a++) {
                	
                    FileInputStream fin = new FileInputStream(zipFiles[a]);

                    zos.putNextEntry(new ZipEntry((new File(zipFiles[a])).getName()));

                    /*
                     * After creating entry in the zip file, actually
                     * write the file.
                     */
                    int length;
     
                    while((length = fin.read(buffer)) > 0)
                    {
                       zos.write(buffer, 0, length);
                    }
     
                    /*
                     * After writing the file to ZipOutputStream, use
                     *
                     * void closeEntry() method of ZipOutputStream class to
                     * close the current entry and position the stream to
                     * write the next entry.
                     */
     
                     fin.close();
                     
                    // not available on BufferedOutputStream
                    zos.closeEntry();
                }
            }
            finally {
                zos.close();
            }
            
            // The result document provides the sent email details.
            actionDocument =  Utility.readXmlFromString(Utility.replaceParameters("<Result><ZipLog><SourceFiles><![CDATA[${SourceFiles}]]></SourceFiles><ZipFile><![CDATA[${ZipFile}]]></ZipFile></ZipLog></Result>", actionParameters));
        }
        else {
        	Utility.LogMessage("Warning - Unknown action type: " + actionType);
        }
        
        return actionDocument;
    }

    /**
     * missingParameters - check for missing parameters.
     * 
     * @param parameterNames		- the required parameter names.
     * @param parametersCollection	- the parameters collection to check.
     * @return						- the list of missing parameters.
     */
    private String missingParameters(String parameterNames, HashMap<String, String> parametersCollection) {
        String result = "";
        String[] requiredParams = parameterNames.split(",");
        for (int rp = 0; rp < requiredParams.length; ++rp) {
            String paramName = requiredParams[rp];
            if (!parametersCollection.containsKey(paramName)) {
                result = result + (result.isEmpty() ? "" : ", ") + paramName;
            }
        }
        return result;
    }

    /**
     * makeCommandURL - make the URL for a given command with the given parameters.
     * 
     * @param commandName	- the name of the command - if missing the first command element will be used.
     * @param endpoint      - the endpoint for the command.
     * @param parameters	- the parameters for the REST API call.
     * @return				- the URL to call the REST API.
     * 
     * @throws Exception
     */
    private String makeCommandURL(String commandName, String endpoint, HashMap<String, String> parameters) throws Exception {
        String result = endpoint;
        if (this.walkerConfig.debug || this.walkerConfig.verbose) {
            Utility.LogMessage("Making URL for command " + commandName);
        }
        if (Utility.getNodesByXPath(this.commandsXml, "//cmd:Command[@Name='" + commandName + "']", this.commandsNamespaceMap).getLength() <= 0) {
            throw new Exception("Could not find a unique Command named " + commandName);
        }
        result = result + Utility.makeParametersString(new StringBuilder("//cmd:Command[@Name='").append(commandName).append("']/cmd:Parameters/cmd:Parameter").toString(), this.commandsXml.getDocumentElement(), this.commandsNamespaceMap, parameters, null, this.walkerConfig.verbose, this.walkerConfig.debug);
        if (this.walkerConfig.debug) {
            Utility.LogMessage("Made URL " + result);
        }
        return result;
    }

    /**
     * getCommandElement - get the named command element, or the first command element if no name is given.
     * 
     * @param commandName	- the command name to search for.
     * @return				- the specified command element.
     * 
     * @throws Exception
     */
    private Element getCommandElement(String commandName) throws Exception {
        String xpath = "";
        String notFoundError = "";
        if (commandName.isEmpty()) {
            xpath = "//cmd:Command[1]";
            notFoundError = "Could not find the first command element!";
        } else {
            xpath = "//cmd:Command[@Name='" + commandName + "']";
            notFoundError = "Could not find a unique Command named '" + commandName + "'";
        }
        NodeList result = Utility.getNodesByXPath(this.commandsXml, xpath, this.commandsNamespaceMap);
        if (result != null && result.getLength() > 0) {
            return (Element)result.item(0);
        }
        throw new Exception(notFoundError);
    }
}
