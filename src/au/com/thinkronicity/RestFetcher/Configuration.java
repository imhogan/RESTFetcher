package au.com.thinkronicity.RestFetcher;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.activation.URLDataSource;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import  java.util.Properties;

/**
 * Configuration builder.
 * 
 * @author Ian Hogan, Ian.Hogan@THINKronicity.com.au
 *
 */
public class Configuration {

	/**----------------------------------------------------------------------
	 * Configuration file defaults
	 *----------------------------------------------------------------------/
	
	
	/**
	 * Default depth limit.
	 */
	public static String  defaultDepthLimit = "-1";
	
	/**---------------------------------------------------------------------- 
	 * Properties
	 *----------------------------------------------------------------------/

	public String configurationURI = "";
	/**
	 * The initial command to execute.
	 */
	public String commandName = "";
	/**
	 * The array of user specified arguments.
	 */
	public HashMap<String,String> parameters = new HashMap<String,String>();
	/**
	 * True if verbose logging is required.
	 */
	public Boolean verbose = false;
	/**
	 * True if debug logging is required.
	 */
	public Boolean debug = false;

	/**----------------------------------------------------------------------
	 * Configuration file values.
	 *----------------------------------------------------------------------/
	
	/**
	 * The service end point.
	 */
	public String serviceEndPoint = "";
	
    /**
     * The default content type.
     */
    public String contentType = "application/xml; charset=UTF-8";
    
	/**
	 * The default commands URI. 
	 */
	public String commandsURI = "";
	
	/**
	 * The recursive depth limit.
	 */
	public Integer depthLimit = -1;
	
	/**
	 * Default timeout for commands
	 */
	public Integer serviceTimeout = 5000;
	
	/**----------------------------------------------------------------------
	 * Other configuration properties.
	 *----------------------------------------------------------------------/

	/**
	 * Configuration properties object.
	 */
	public Properties configurationProperties = new Properties();
	
	
	/**
	 * Constructor - takes in an array of command line options and parses it. Also loads properties from the 
	 *               configuration file. If there is an invalid parameter then an exception is thrown. If
	 *               there is no work to do - eg --help or -e options are given, then the exit flag is set to
	 *               true.
	 *               
	 * @param args - array of command line parameters.
	 * 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public Configuration(String applicationName, Object input) throws Exception {

    	if (input instanceof LinkedHashMap<?, ?>) {

    		// Add parameters from input 
			for (Map.Entry<?, ?> entrySet : ((LinkedHashMap<String, String>)input).entrySet()) {
                Utility.LogMessage("Configuration parameters.put(" + entrySet.getKey().toString() + ", " + entrySet.getValue().toString() + ")");
                parameters.put(entrySet.getKey().toString(), entrySet.getValue().toString());
			}    		
        }
    	else {
    		throw new Exception("Unacceptable input class - "+input.getClass());
    	}
        AWSCredentials awsCredentials = null;
        awsCredentials = parameters.containsKey("AWS_ACCESS_KEY") && parameters.containsKey("AWS_SECRET_KEY") 
        			   ? new BasicAWSCredentials(getParameter("AWS_ACCESS_KEY"), getParameter("AWS_SECRET_KEY")) 
        			   : new DefaultAWSCredentialsProviderChain().getCredentials(); //  EnvironmentVariableCredentialsProvider()
        String configurationURI = AWS_S3_Helper.resolveURI(awsCredentials, getParameter("configurationURI"), 3600);
    	
    	if (!configurationURI.isEmpty()) {
            URLDataSource fds;
            if (debug) {
                Utility.LogMessage("Getting Properties from " + configurationURI);
            }
            if ((fds = new URLDataSource(new URL(configurationURI))).getContentType().equals("text/xml") || fds.getContentType().equals("application/xml")) {
                configurationProperties.loadFromXML(fds.getInputStream());
	    	} else {
                configurationProperties.load(fds.getInputStream());
	    	}
            String prefix = "parameter.";
	    	for(Enumeration<Object> e = configurationProperties.keys(); e.hasMoreElements();) {
	    		String key = (String) e.nextElement();
	    		if (key.startsWith(prefix) && !parameters.containsKey(key.substring(prefix.length()))) {
    	    		parameters.put(key.substring(prefix.length()), Utility.replaceParameters(configurationProperties.getProperty(key), parameters));
	    		}
	    	}
    	}
    	else {
		    if (debug)
				Utility.LogMessage("No properties provided - just using parameters");
    		
    	}
    	
    	// Get parameters - default is given from configuration file, or internal default.
    	
    	verbose 			= getParameter("traceVerbose", 	configurationProperties.getProperty("trace.verbose", 		verbose.toString())).equals("true");
    	debug 				= getParameter("traceDebug", 	configurationProperties.getProperty("trace.debug", 			debug.toString())).equals("true");
    	serviceEndPoint 	= getParameter("endPoint", 		configurationProperties.getProperty("service.endPoint", 	serviceEndPoint));
    	contentType 		= getParameter("contentType", 	configurationProperties.getProperty("service.contentType", 	contentType));
    	commandsURI 		= getParameter("commandsURI", 	configurationProperties.getProperty("service.commandsFile", commandsURI));
        commandsURI 		= AWS_S3_Helper.resolveURI(awsCredentials, commandsURI, 3600);
        commandName 		= getParameter("command", 		configurationProperties.getProperty("service.startCommand", commandName));
        depthLimit 			= Integer.parseInt(getParameter("depthLimit", 		configurationProperties.getProperty("service.depthLimit", 	depthLimit.toString())));
        serviceTimeout 		= Integer.parseInt(getParameter("serviceTimeout", 	configurationProperties.getProperty("service.timeout", 		serviceTimeout.toString())));
    }


    /**
     * getParameter - get the named parameter from the parameters collection, or default value if the name is not know. Any embedded ${ParamName} references are evaluated.
     * 
     * @param name			- parameter name.
     * @param defaultValue	- default value.
     * 
     * @return				- the value of the named parameter or default value.
     */
    public String getParameter(String name, String defaultValue) {
        return Utility.replaceParameters((String)parameters.getOrDefault(name, defaultValue), parameters);
    }

    /**
     * getParameter - get the named parameter from the parameters collection, or the empty string if the name is not know. Any embedded ${ParamName} references are evaluated.
     * 
     * @param name			- parameter name.
     * 
     * @return				- the value of the named parameter or the empty string.
     */
    public String getParameter(String name) {
        return getParameter(name, "");
    }
}
