package au.com.thinkronicity.RestFetcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Helper functions for Apache FOP.
 * 
 * @author Ian Hogan, Ian_MacDonald_Hogan@yahoo.com
 *
 */
public class FopHelper {
    /**
     * FOP Factory 
     */
    private FopFactory fopFactory = null;

    /**
     * FopHelper - constructor, taking a base URI and configuration file path.
     * 
     * @param baseURI			- base URI for execution. The configuration, cache folder and font files are relative to this URI.
     * @param configFilePath	- the configuration file path.
     * 
     * @throws URISyntaxException
     * @throws SAXException
     * @throws IOException
     */
    public FopHelper(URI baseURI, String configFilePath) throws URISyntaxException, SAXException, IOException {
        FopConfParser parser = new FopConfParser(new File(String.valueOf(baseURI.toString()) + "/" + configFilePath)); //parsing configuration  
        FopFactoryBuilder builder = parser.getFopFactoryBuilder(); //building the factory with the user options
        this.fopFactory = builder.build();            
    }

    /**
     * convertFO2PDF - convert an XSL:FO document to a PDF output stream.
     *  
     * @param fo	- The XSL:FO document.
     * @return		- The PDF result.
     * 
     * @throws Exception
     */
    public ByteArrayOutputStream convertFO2PDF(Document fo) throws Exception {
        if (this.fopFactory == null) {
            throw new Exception("Not initialised!");
        }
        FOUserAgent foUserAgent = this.fopFactory.newFOUserAgent();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = this.fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, (OutputStream)out);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        DOMSource src = new DOMSource(fo);
        SAXResult res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        return out;
    }
}
