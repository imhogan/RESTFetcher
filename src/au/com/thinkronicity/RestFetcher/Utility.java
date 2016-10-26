/*
 * Decompiled with CFR 0_113.
 * 
 * Could not load the following classes:
 *  com.amazonaws.auth.AWSCredentials
 *  com.amazonaws.auth.EnvironmentVariableCredentialsProvider
 *  com.amazonaws.services.lambda.runtime.Context
 *  com.amazonaws.services.lambda.runtime.LambdaLogger
 *  com.amazonaws.services.s3.model.CannedAccessControlList
 *  net.sf.saxon.TransformerFactoryImpl
 *  org.apache.commons.codec.binary.Base64
 *  org.apache.commons.ssl.OpenSSL
 *  org.json.JSONObject
 *  org.json.XML
 */
package au.com.thinkronicity.RestFetcher;

import au.com.thinkronicity.RestFetcher.AWS_S3_Helper;
import au.com.thinkronicity.RestFetcher.FopHelper;
import au.com.thinkronicity.RestFetcher.MappedNamespaceContext;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.ssl.OpenSSL;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.XML;
// import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Ian Hogan - Ian.Hogan@THINKronicity.com.au
 * 
 * Utility functions
 *
 */
public class Utility {
	
	
    /**
     * myContext - Current execution context.
     */
    private static Context myContext = null;
    
    /**
     * mStrpassword - Password for encryption / decryption
     */
    private static char[] mStrpassword = new char[]{'@', 'n', '3', 'w', '!', '_', 's', '3', 'c', 'r', 'e', 't', '5', '8', '3', '0', '!'};
    
    /**
     * Default base URI for Apache FOP
     */
    private static String fopBaseURI = "https://s3-ap-northeast-1.amazonaws.com/smirnov-dev-files/fop";
    
    
    /**
     * Default configuration file, relative to fopBaseURI for Apache FOP
     */
    private static String fopConfig = "fop.xconf";

    /**
     * getMyContext - get the current context.
     * 
     * @return - the current context.
     */
    public static Context getMyContext() {
        return myContext;
    }

    /**
     * setMyContext - set the current context.
     * 
     * @param myContext - the new context.
     */
    public static void setMyContext(Context myContext) {
        Utility.myContext = myContext;
    }

    /**
     * getFopBaseURI - get the base URI for Apache FOP
     * 
     * @return - the current base URI
     */
    public static String getFopBaseURI() {
        return fopBaseURI;
    }

    /**
     * setFopBaseURI - set the base URI for Apache FOP
     * 
     * @param fopBaseURI - the new base URI
     */
    public static void setFopBaseURI(String fopBaseURI) {
        Utility.fopBaseURI = fopBaseURI;
    }

    /**
     * getFopConfig - get the Apache FOP configuration file.
     * 
     * @return - the configuration file.
     */
    public static String getFopConfig() {
        return fopConfig;
    }

    /**
     * setFopConfig - set the Apache FOP configuration file.
     * 
     * @param fopConfig - the new configuration file.
     */
    public static void setFopConfig(String fopConfig) {
        Utility.fopConfig = fopConfig;
    }

    /**
     * LogMessage - log a given message - prefixed with a timestamp - to the log destination.
     * 
     * @param message - the message to log.
     */
    public static void LogMessage(String message) {
        if (myContext != null) {
            myContext.getLogger().log(Utility.Timestamp() + ": " + message);
        } else {
            System.out.println(Utility.Timestamp() + ": " + message);
        }
    }

    /**
     * GetStackTrace - get stack trace for the given exception as a string.
     * 
     * @param ex - the given exception.
     * 
     * @return - stack trace as a string.
     */
    public static String GetStackTrace(Exception ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    /**
     * Timestamp - get the current date/time in the standard timestamp format.
     * 
     * @return - the current date/time.
     */
    public static String Timestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSXXX");
        return dateFormat.format(new Date());
    }

    /**
     * decryptPassword - decrypt the specified password.
     * 
     * @param strPassword 	- the password to decrypt.
     * @return 				- the password in clear text.
     * @throws Exception
     */
    public static String decryptPassword(String strPassword) throws Exception {
        try {
            byte[] encrypted = strPassword.getBytes();
            byte[] data = OpenSSL.decrypt((String)"des3", (char[])mStrpassword, (byte[])encrypted);
            strPassword = new String(data);
        }
        catch (Exception ex) {
            throw new Exception("Decrypting Password " + strPassword + " " + ex.getMessage().toString());
        }
        return strPassword;
    }

    /**
     * encryptPassword - encrypt the given password.
     * 
     * @param strPassword 	- the password to encrypt.
     * @return 				- the encrypted password.
     * @throws Exception
     */
    public static String encryptPassword(String strPassword) throws Exception {
        try {
            byte[] plain = strPassword.getBytes();
            byte[] data = OpenSSL.encrypt((String)"des3", (char[])mStrpassword, (byte[])plain);
            strPassword = new String(data);
        }
        catch (Exception ex) {
            throw new Exception("Encrypting Password " + strPassword + " " + ex.getMessage().toString());
        }
        return strPassword;
    }

    /**
     * writeXmlToString - write the given XML document to a string.
     * 
     * @param xmlDoc 	- the document to write.
     * @return 			- the string representation of the XML document.
     * @throws TransformerException
     */
    public static String writeXmlToString(Document xmlDoc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty("omit-xml-declaration", "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
        String output = writer.getBuffer().toString();
        return output;
    }

    /**
     * writeXmlString2File - write the XML string to the specified file.
     * 
     * @param xmlText 	- the XML string to write.
     * @param filename 	- the filename to write to.
     * @return 			- true if the file was written, false otherwise.
     */
    public static Boolean writeXmlString2File(String xmlText, String filename) {
        File file = null;
        StreamResult result = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = null;
            docBuilder = docBuilderFactory.newDocumentBuilder();
            byte[] b = xmlText.getBytes("UTF-8");
            ByteArrayInputStream xmlBytes = new ByteArrayInputStream(b);
            Document domData = docBuilder.parse(xmlBytes);
            if (domData == null) {
                Utility.LogMessage("No XML Loaded");
            } else {
                domData.normalize();
                DOMSource source = new DOMSource(domData);
                file = new File(filename);
                result = new StreamResult(file);
                Transformer xformer = TransformerFactory.newInstance().newTransformer();
                xformer.transform(source, result);
            }
            return true;
        }
        catch (TransformerConfigurationException e) {
            Utility.LogMessage("Transformer Configuration Exception: " + e.getMessage());
        }
        catch (TransformerException e) {
            Utility.LogMessage("Transformer Exception: " + e.getMessage());
        }
        catch (Exception e) {
            Utility.LogMessage("Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * writeXmlDocument2File - write the given XML document to the named file.
     * 
     * @param xmlDocument 	- the XML document to write.
     * @param filename 		- the file to write to.
     * @return 				- true if the file was written, false otherwise.
     */
    public static Boolean writeXmlDocument2File(Document xmlDocument, String filename) {
        File file = null;
        StreamResult result = null;
        try {
            xmlDocument.normalize();
            DOMSource source = new DOMSource(xmlDocument);
            file = new File(filename);
            result = new StreamResult(file);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
            return true;
        }
        catch (TransformerConfigurationException e) {
            Utility.LogMessage("Transformer Configuration Exception: " + e.getMessage());
        }
        catch (TransformerException e) {
            Utility.LogMessage("Transformer Exception: " + e.getMessage());
        }
        catch (Exception e) {
            Utility.LogMessage("Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * xslTransform - apply a named XSL transform file to a given XML document, with supplied parameters.
     * 
     * @param xmlDocument 			- the XML document to transform.
     * @param transform 			- the filename of the transform file.
     * @param transformParameters 	- parameters for the transform.
     * @param trace 				- if true trace messages are emitted.
     * @return 						- the document resulting from the transform.
     */
    public static Document xslTransform(Document xmlDocument, String transform, HashMap<String, String> transformParameters, boolean trace) {
        try {
            xmlDocument.normalize();
            DOMSource source = new DOMSource(xmlDocument);
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(transform)));
            Transformer xformer = template.newTransformer();
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            DocumentBuilder builder = f.newDocumentBuilder();
            Document doc = builder.newDocument();
            DOMResult result = new DOMResult(doc);
            if (trace) {
                Utility.LogMessage("Transform file:" + transform);
            }
            for (Map.Entry<String, String> entry : transformParameters.entrySet()) {
                xformer.setParameter(entry.getKey(), entry.getValue());
                if (!trace) continue;
                Utility.LogMessage("Transform parameter " + entry.getKey() + "=" + entry.getValue());
            }
            xformer.transform(source, result);
            return doc;
        }
        catch (TransformerConfigurationException e) {
            Utility.LogMessage("Transformer Configuration Exception: " + e.getMessage());
        }
        catch (TransformerException e) {
            Utility.LogMessage("Transformer Exception: " + e.getMessage());
        }
        catch (Exception e) {
            Utility.LogMessage("Exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * xmlToS3PDF - transform a given XML document with specified transform and parameters and convert the resulting XSL:FO document to PDF and write it to an S3 file.
     * 
     * @param credentials			- Credentials for accessing AWS Resources
     * @param xmlDocument 			- the XML document to transform.
     * @param transformURI 			- the URI of the transform file.
     * @param transformParameters 	- parameters for the transform.
     * @param bucketName			- The name of the bucket containing the file.
     * @param key					- The key for the file to access.
     * @param trace 				- if true trace messages are emitted.
     * @return						- A pre-signed URL to access the file contents - with the given expiration time.
     */
    public static String xmlToS3PDF(
    		 AWSCredentials credentials
    		,Document xmlDocument
    		,String transformURI
    		,HashMap<String, String> transformParameters
    		,String bucketName
    		,String key
    		,Boolean trace
    	) {
        String result = "";
        try {
            Document xslOutput = Utility.xsl2Transform(xmlDocument, transformURI, transformParameters, trace);
            URI baseURI = new URI((String)transformParameters.getOrDefault((Object)"FopBaseURI", (String)fopBaseURI));
            String configFilePath = (String)transformParameters.getOrDefault((Object)"FopConfig", (String)fopConfig);
            if (trace) {
                Utility.LogMessage("FOP Config is " + baseURI.toString() + "/" + configFilePath);
            }
            FopHelper myFop = new FopHelper(baseURI, configFilePath);
            ByteArrayOutputStream pdf = myFop.convertFO2PDF(xslOutput);
            result = AWS_S3_Helper.writeStreamToS3File(credentials, bucketName, key, pdf, "application/pdf", CannedAccessControlList.Private);
        }
        catch (Exception e) {
            Utility.LogMessage("Exception: " + e.getMessage());
        }
        return result;
    }

    /**
     * xsl2Transform - Perform an XSLT 2 Transform of the given XML document using the given transform and parameters.
     * 
     * @param xmlDocument 			- the XML document to transform.
     * @param transformURI 			- the URI of the transform file.
     * @param transformParameters 	- parameters for the transform.
     * @param trace 				- if true trace messages are emitted.
     * @return 						- the document resulting from the transform.
     */
    public static Document xsl2Transform(Document xmlDocument, String transformURI, HashMap<String, String> transformParameters, boolean trace) {
        try {
            if (trace) {
                Utility.LogMessage("Transform file:" + transformURI);
            }
            xmlDocument.normalize();
            DOMSource source = new DOMSource(xmlDocument);
            if (trace) {
                Utility.LogMessage("source set");
            }
            TransformerFactoryImpl factory = new TransformerFactoryImpl();
            if (trace) {
                Utility.LogMessage("factory set");
            }
            Templates template = factory.newTemplates(new StreamSource(transformURI));
            if (trace) {
                Utility.LogMessage("template set");
            }
            Transformer xformer = template.newTransformer();
            if (trace) {
                Utility.LogMessage("xformer set");
            }
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            DocumentBuilder builder = f.newDocumentBuilder();
            Document doc = builder.newDocument();
            DOMResult result = new DOMResult(doc);
            if (trace) {
                Utility.LogMessage("result set");
            }
            if (transformParameters != null) {
                for (Map.Entry<String, String> entry : transformParameters.entrySet()) {
                    xformer.setParameter(entry.getKey(), entry.getValue());
                    if (!trace) continue;
                    Utility.LogMessage("Transform parameter " + entry.getKey() + "=" + entry.getValue());
                }
            } else if (trace) {
                Utility.LogMessage("no parameters");
            }
            xformer.transform(source, result);
            if (trace) {
                Utility.LogMessage("transform done");
            }
            return doc;
        }
        catch (TransformerConfigurationException e) {
            Utility.LogMessage("Transformer Configuration Exception: " + e.getMessage());
        }
        catch (TransformerException e) {
            Utility.LogMessage("Transformer Exception: " + e.getMessage());
        }
        catch (Exception e) {
            Utility.LogMessage("Exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * readXmlFromFile - read an XML document from the given file.
     * 
     * @param fileName	- the name of the file to read.
     * @return			- the XML document
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document readXmlFromFile(String fileName) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder b = f.newDocumentBuilder();
        Document doc = b.parse(new File(fileName));
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * outputStreamFromDocument - convert a given XML document to an output stream.
     * 
     * @param xmlDocument	- the document to convert.
     * @return				- the output stream to read the bytes from.
     * 
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     */
    public static ByteArrayOutputStream outputStreamFromDocument(Document xmlDocument) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DOMSource xmlSource = new DOMSource(xmlDocument);
        StreamResult outputTarget = new StreamResult(outputStream);
        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
        return outputStream;
    }

    /**
     * readXmlFromURI - read an XML document from a supplied URI.
     * 
     * @param uri 	- the URI to read from.
     * @return		- the XML document read.
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document readXmlFromURI(String uri) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder b = f.newDocumentBuilder();
        Document doc = b.parse(uri);
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * readXmlFromStream - read an XML document from a given stream. If the stream is a JSON document, convert it to XML first.
     * 
     * @param is		- the input stream to read from.
     * @param isJSON	- if true, convert the stream contents from JSON to XML.
     * @return			- the XML document read.
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document readXmlFromStream(InputStream is, Boolean isJSON, Boolean useJSON2SafeXML) throws ParserConfigurationException, SAXException, IOException {
        Document doc = null;
        if (isJSON) {
            StringBuilder textBuilder;
            BufferedInputStream in;
            in = new BufferedInputStream(is);
            textBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)in, Charset.forName(StandardCharsets.UTF_8.name())));
            try {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char)c);
                }
            }
            finally {
                if (reader != null) {
                    reader.close();
                }
            }
            in.close();
            String jsonString = sanitizeJson4XML(textBuilder.toString());
            
            Utility.LogMessage("jsonString is " + jsonString);
            
            JSONObject jsonData = new JSONObject(jsonString);
            
            if (useJSON2SafeXML) {
                
                doc = Utility.JSON2SafeXML(jsonData, null);
            }
            else {
                String xmlContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n<root>" + XML.toString((Object)jsonData) + "</root>";
                doc = Utility.readXmlFromString(xmlContent);
            }
        } else {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            DocumentBuilder b = f.newDocumentBuilder();
            doc = b.parse(is);
        }
        doc.getDocumentElement().normalize();
        return doc;
    }


    /**
     * JSON2SafeXML - Convert a JSONObject to an XML document safely.
     * 
     * @param jsonData    - The JSON object to process.
     * @param xmlElement  - The document to 
     * @return			  - an "XML safe" JSON string.
     * 
     */
    public static Document JSON2SafeXML(Object jsonData, Element parentElement) 
    throws ParserConfigurationException
    {
        // Map null to empty string     
        if (jsonData == null) {
            jsonData = "";
        }
        
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;

        if (parentElement == null) {
            
            icBuilder = icFactory.newDocumentBuilder();
            Document doc = icBuilder.newDocument();
            parentElement = doc.createElement("JSON");
            parentElement.setAttribute("class", jsonData.getClass().getName());
            doc.appendChild(parentElement);
        }
         
        Document doc = parentElement.getOwnerDocument();
        
        
        if (jsonData instanceof JSONObject) {
            
            for (Object key : ((JSONObject)jsonData).keySet()) {

                Element container = doc.createElement("JSONObject");
                
                String keyStr = (String)key;
                Object keyvalue = ((JSONObject)jsonData).get(keyStr);
                
                Element keyElement = doc.createElement("key");
                
                keyElement.setTextContent(keyStr);
                
                container.appendChild(keyElement);
                
                Element valueElement = doc.createElement("value");
    
                if (keyvalue == null) {
                    keyvalue = "";
                } else if (keyvalue.getClass().isArray()) {
                    keyvalue = new JSONArray(keyvalue);
                }
                
                valueElement.setAttribute("class", keyvalue.getClass().getName());

                if (keyvalue instanceof JSONArray) {
                    JSON2SafeXML(keyvalue, valueElement);                    
                }
                else if (keyvalue instanceof JSONObject) {
                    JSON2SafeXML(keyvalue, valueElement);                    
                }
                else {
                    valueElement.setTextContent(keyvalue.toString());
                }
                
                container.appendChild(valueElement);
                
                parentElement.appendChild(container);
            }
        }
        else if (jsonData instanceof JSONArray) {
            
            Element container = doc.createElement("JSONArray");
            Integer i = 0;
            for (Object val : (JSONArray)jsonData) {
                Element rowElement = doc.createElement("row");
                rowElement.setAttribute("order", i.toString());
                rowElement.setAttribute("class", val.getClass().getName());
                JSON2SafeXML(val, rowElement);
                container.appendChild(rowElement);
                i++;
            }
            parentElement.appendChild(container);
            
        }
        else if (jsonData.getClass().isArray() ) {
            
            JSON2SafeXML(new JSONArray(jsonData), parentElement);
            
        }
        else {
            parentElement.setTextContent(jsonData.toString());
        }
        
        return doc;

    }

    /**
     * sanitizeJson4XML - Sanitize a JSON string so it converts to XML without error.
     * 
     * @param jsonString    - the JSON string to process.
     * @return			    - an "XML safe" JSON string.
     * 
     */
    public static String sanitizeJson4XML(String jsonString) {
        
        // Remove leading & trailing whitespace
        jsonString = jsonString.trim();
        
        // Handle anonymous object results
        if (!jsonString.startsWith("{")) {
            
            jsonString = "{\"Anonymous\":"+jsonString+"}";
        }
               
        return jsonString;
    }
    
    /**
     * readXmlFromString - read an XML document from an XML string.
     * 
     * @param xmlText	- the XML string to read.
     * @return			- the XML document read.
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document readXmlFromString(String xmlText) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder b = f.newDocumentBuilder();
        Document doc = b.parse(new InputSource(new StringReader(xmlText)));
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * getNodesByXPath - get nodes from an XML document by applying a given XPath expression to a base node.
     * 
     * @param baseNode		- the base node for the XPath expression.
     * @param nodesXPath	- the XPath expression to evaluate.
     * @param prefMap		- the XML prefix map for namespace evaluation.
     * @return				- the nodes matching the supplied expression.
     */
    public static NodeList getNodesByXPath(Node baseNode, String nodesXPath, Map<String, String> prefMap) {
        NodeList result = null;
        try {
            if (baseNode != null) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                if (prefMap != null) {
                    MappedNamespaceContext namespaces = new MappedNamespaceContext(prefMap);
                    xPath.setNamespaceContext(namespaces);
                    XPathExpression expr = xPath.compile(nodesXPath);
                    result = (NodeList)expr.evaluate(baseNode, XPathConstants.NODESET);
                } else {
                    result = (NodeList)xPath.evaluate(nodesXPath, baseNode, XPathConstants.NODESET);
                }
            }
        }
        catch (Exception e) {
            Utility.LogMessage("Exception: " + e.getMessage());
            result = null;
        }
        return result;
    }

    /**
     * getNodeValueByXPath	- get the value of the first node matching a given XPath expression applied to a base node.  
     * 
     * @param baseNode		- the base node for the XPath expression.
     * @param nodesXPath	- the XPath expression to evaluate.
     * @param prefMap		- the XML prefix map for namespace evaluation.
     * @param defaultValue	- default value if there is no matched node.
     * @return				- the value of the first node matching the supplied expression, or defaultValue if no matching node was found.
     */
    public static String getNodeValueByXPath(Node baseNode, String nodesXPath, Map<String, String> prefMap, String defaultValue) {
        String result = defaultValue;
        try {
            if (baseNode != null) {
                NodeList resultNodes;
                XPath xPath = XPathFactory.newInstance().newXPath();
                if (prefMap != null) {
                    MappedNamespaceContext namespaces = new MappedNamespaceContext(prefMap);
                    xPath.setNamespaceContext(namespaces);
                    XPathExpression expr = xPath.compile(nodesXPath);
                    resultNodes = (NodeList)expr.evaluate(baseNode, XPathConstants.NODESET);
                } else {
                    resultNodes = (NodeList)xPath.evaluate(nodesXPath, baseNode, XPathConstants.NODESET);
                }
                if (resultNodes != null && resultNodes.getLength() > 0) {
                    result = resultNodes.item(0).getTextContent();
                }
            }
        }
        catch (Exception e) {
            Utility.LogMessage("Exception: " + e.getMessage());
        }
        return result;
    }

    /**
     * makeParametersString - make the parameters section of a URI for a REST call. The parameters can be path or query parameters. 
     * 
     * @param parametersBaseXPath	- the XPath expression used to find the parameter elements.
     * @param parametersElement		- the parent element to search from.
     * @param prefMap				- the XML prefix map for namespace evaluation.
     * @param contextParameters		- the context parameters.
     * @param contextElement		- the context element for parameter evaluation.
     * @param verbose				- if true emit verbose logging.
     * @param debug					- if true emit debug logging.
     * @return						- the parameters string.
     * 
     * @throws Exception
     */
    public static String makeParametersString(
    		 String parametersBaseXPath
    		,Element parametersElement
    		,Map<String, String> prefMap
    		,HashMap<String, String> contextParameters
    		,Element contextElement
    		,Boolean verbose
    		,Boolean debug
    	) throws Exception {
        String result = "";
        NodeList parameterNodes = Utility.getNodesByXPath(parametersElement, parametersBaseXPath, prefMap);
        if (debug) {
            Utility.LogMessage("XPath " + parametersBaseXPath + " applied to " + parametersElement.getNodeType() + " matches " + parameterNodes.getLength() + " nodes.");
        }
        if (parameterNodes.getLength() > 0) {
            int param = 0;
            while (param < parameterNodes.getLength()) {
                Element paramElement = (Element)parameterNodes.item(param);
                String paramName = paramElement.getAttribute("Name");
                String paramValue = Utility.getParameterValue(paramName, paramElement, prefMap, contextParameters, contextElement, verbose, debug);
                String paramSeparator = paramElement.getAttribute("Separator");
                if (paramSeparator.equals("")) {
                    paramSeparator = "/";
                }
                if (paramElement.getAttribute("Format").equals("Query")) {
                    paramValue = paramName + "=" + URLEncoder.encode(paramValue, "UTF-8");
                }
                result = result + paramSeparator + paramValue;
                ++param;
            }
        }
        return result;
    }

    /**
     * loadParameters - load parameters into a HashMap collection.
     * 
     * @param includeContext		- if true the context parameters are added the collection.
     * @param parametersBaseXPath	- the XPath expression used to find the parameter elements.
     * @param parametersElement		- the parent element to search from.
     * @param prefMap				- the XML prefix map for namespace evaluation.
     * @param contextParameters		- the context parameters.
     * @param contextElement		- the context element for parameter evaluation.
     * @param verbose				- if true emit verbose logging.
     * @param debug					- if true emit debug logging.
     * @return						- the parameters collection.
     * 
     * @throws Exception
     */
    public static HashMap<String, String> loadParameters(
    		 Boolean includeContext
    		,String parametersBaseXPath
    		,Element parametersElement
    		,Map<String, String> prefMap
    		,HashMap<String, String> contextParameters
    		,Element contextElement
    		,Boolean verbose
    		,Boolean debug
    	) throws Exception {
        HashMap<String, String> result = new HashMap<String, String>();
        if (includeContext) {
            result.putAll(contextParameters);
        }
        HashMap<String, String> currentContext = new HashMap<String, String>();
        currentContext.putAll(contextParameters);
        NodeList parameterNodes = Utility.getNodesByXPath(parametersElement, parametersBaseXPath, prefMap);
        if (parameterNodes != null && parameterNodes.getLength() > 0) {
            for (int param = 0; param < parameterNodes.getLength(); ++param) {
                Element paramElement = (Element)parameterNodes.item(param);
                String paramName = paramElement.getAttribute("Name");
                String paramValue = Utility.getParameterValue(paramName, paramElement, prefMap, currentContext, contextElement, verbose, debug);
                currentContext.put(paramName, paramValue);
                result.put(paramName, paramValue);
                NodeList regExpNodes = paramElement.getElementsByTagName("RegExp");
                if (regExpNodes != null && regExpNodes.getLength() > 0) {
                	// If the parameter element has RegExp child nodes, then each pattern is applied to the current parameter value and each matching named group is added to the 
                	// parameter collection.
                    for (Integer exp = 0; exp < regExpNodes.getLength(); exp++) {
                        Element regExpElement = (Element)regExpNodes.item(exp);
                        String expValue = Utility.getParameterValue(String.valueOf(paramName) + ".regExp[" + exp.toString() + "]", regExpElement, prefMap, currentContext, contextElement, verbose, debug);
                        Matcher resolver = Pattern.compile(expValue).matcher(paramValue);
                        if (resolver.matches()) {
                            Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(expValue);
                            while (m.find()) {
                                String groupName = m.group(1);
                                result.put(String.valueOf(paramName) + "." + groupName, resolver.group(groupName));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * getParameterValue - get the value of a parameter.
     * 
     * @param paramName				- the name of the parameter.
     * @param paramElement			- the parameter element node.
     * @param prefMap				- the XML prefix map for namespace evaluation.
     * @param contextParameters		- the context parameters.
     * @param contextElement		- the context element for parameter evaluation.
     * @param verbose				- if true emit verbose logging.
     * @param debug					- if true emit debug logging.
     * @return						- the parameter value.
     * 
     * @throws Exception
     */
    public static String getParameterValue(String paramName, Element paramElement, Map<String, String> prefMap, HashMap<String, String> contextParameters, Element contextElement, Boolean verbose, Boolean debug) throws Exception {

    	// Start by getting the text value of the element.
    	String paramValue = paramElement.getTextContent();
    	
    	// If there is an XPath attribute, evaluate this expression.
        if (paramElement.hasAttribute("XPath")) {
            if (contextElement == null) {
                throw new Exception("No context element to resolve XPath for parameter named " + paramName);
            }
            NodeList argNodes = Utility.getNodesByXPath(contextElement, paramElement.getAttribute("XPath"), prefMap);
            if (argNodes.getLength() > 0) {
                paramValue = argNodes.item(0).getTextContent();
                for (int arg = 1; arg < argNodes.getLength(); ++arg) {
                    paramValue = String.valueOf(paramValue) + " " + argNodes.item(arg).getTextContent();
                }
            } else if (paramElement.hasAttribute("Default")) {
                paramValue = paramElement.getAttribute("Default");
            }
        }
        
        if (debug) {
            Utility.LogMessage(String.valueOf(paramName) + "='" + paramValue + "'");
        }
        
        if (paramValue.startsWith("$")) {
            // If the parameter value starts with a $ sign, the value is the value of the context parameter whose name follows the $ sign.
            if (contextParameters.containsKey(paramValue.substring(1))) {
                paramValue = contextParameters.get(paramValue.substring(1));
            } else {
                if (!paramElement.hasAttribute("Default")) throw new Exception("Could not find a unique a parameter named " + paramValue.substring(1));
                paramValue = paramElement.getAttribute("Default");
            }
        } else if (paramValue.startsWith("=")) {
            // If the parameter value starts with an = sign, substitute all patterns of the form ${SomeName} with the value of the context parameter whose name is SomeName.
            paramValue = Utility.replaceParameters(paramValue.substring(1), contextParameters);
        }
        if (paramElement.hasAttribute("Function")) {
            // If the parameter element has a function attribute, apply that function to the current value to get the new value.
        	// Currently only S3URL is known, and the parameter value should be a string of the form bucket,key[,expirySeconds]
            String functionName = paramElement.getAttribute("Function");
            if (!functionName.equals("S3URL")) throw new Exception("Unknown function '" + functionName + "' for parameter " + paramName);
            String[] functionParams = paramValue.split(",");
            if (functionParams.length < 2) {
                throw new Exception("Insufficient parameters for function S3URL, for parameter " + paramName);
            }
            Integer expirySeconds = 30;
            if (functionParams.length >= 3) {
                expirySeconds = Integer.parseInt(functionParams[2]);
            } // EnvironmentVariableCredentialsProvider().getCredentials()
            paramValue = AWS_S3_Helper.getS3FileURL(new DefaultAWSCredentialsProviderChain().getCredentials(), functionParams[0], functionParams[1], expirySeconds);
        }
        if (paramElement.hasAttribute("Type")) {
            // If the parameter element has a Type attribute, convert the current value using the supplied type, which may be BasicAuth, AuthToken or Text.
            String paramType = paramElement.getAttribute("Type");
            if (paramType.equals("BasicAuth")) {
                byte[] authEncBytes = Base64.encodeBase64((byte[])paramValue.getBytes());
                paramValue = new String(authEncBytes);
            } else if (paramType.equals("AuthToken")) {
                paramValue = "bearer " + paramValue;
            } else if (!paramType.equals("Text")) {
                throw new Exception("Unknown type '" + paramType + "' for parameter " + paramName);
            }
        }
        
        if (debug || verbose) {
	        Utility.LogMessage(String.valueOf(paramName) + "='" + paramValue + "'");
        }
        
        return paramValue;
    }

    /**
     * replaceParameters - replace parameters of the form ${ParamName} in the given string.
     * 
     * @param value				- value to process. 
     * @param contextParameters	- the context parameters.
     * @return					- the evaluated string.
     */
    public static String replaceParameters(String value, HashMap<String, String> contextParameters) {
        String result = value;
        for (Map.Entry<String, String> e : contextParameters.entrySet()) {
            result = result.replaceAll("\\$\\{" + e.getKey() + "\\}", e.getValue());
        }
        return result;
    }
}
