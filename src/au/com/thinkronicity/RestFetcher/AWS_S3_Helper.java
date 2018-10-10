package au.com.thinkronicity.RestFetcher;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * AWS S3 Helper functions.
 * 
 * @author Ian Hogan, Ian_MacDonald_Hogan@yahoo.com
 *
 */
public class AWS_S3_Helper {

	/**
	 * writeStreamToS3File - Write a given Stream to a specified S3 file, returing a pre-signed URL to the file.  
	 * 
     * @param credentials	- Credentials for accessing AWS Resources
     * @param bucketName	- The name of the bucket to write to.
     * @param key			- The key for the file to write.
     * @param stream		- The stream to be written to the file.
     * @param contentType	- Content type for the file.
     * @param acl			- Access Control List specifying file permissions.
     * @return				- A pre-signed URL to access the file contents - with a 15 minute expiration time.
     */
    public static String writeStreamToS3File(
    		 AWSCredentials credentials
    		,String bucketName
    		,String key
    		,ByteArrayOutputStream stream
    		,String contentType
    		,CannedAccessControlList acl
    	) {
        String result = "";
        AmazonS3Client conn = new AmazonS3Client(credentials);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength((long)stream.toByteArray().length);
        ByteArrayInputStream input = new ByteArrayInputStream(stream.toByteArray());
        conn.putObject(bucketName, key, (InputStream)input, metadata);
        conn.setObjectAcl(bucketName, key, acl);
        result = conn.generatePresignedUrl(bucketName, key, new Date(new Date().getTime() + 900000), HttpMethod.GET).toString();
        return result;
    }

    /**
	 * getS3FileURL - Get a pre-signed URL to access a specified S3 file.  
	 * 
     * @param credentials	- Credentials for accessing AWS Resources
     * @param bucketName	- The name of the bucket containing the file.
     * @param key			- The key for the file to access.
     * @param expirySeconds - The expiration period in seconds 
     * @return				- A pre-signed URL to access the file contents - with the given expiration time.
     */
    public static String getS3FileURL(
    		 AWSCredentials credentials
    		,String bucketName
    		,String key
    		,Integer expirySeconds
    	) {
        String result = "";
        try {
            AmazonS3Client conn = new AmazonS3Client(credentials);
            result = conn.generatePresignedUrl(bucketName, key, new Date(new Date().getTime() + (long)(1000 * expirySeconds)), HttpMethod.GET).toString();
        }
        catch (Exception ex) {
            result = "";
        }
        return result;
    }

    /**
	 * deleteS3File - Delete a specified S3 file, returning a status message.
	 * 
     * @param credentials	- Credentials for accessing AWS Resources
     * @param bucketName	- The name of the bucket containing the file.
     * @param key			- The key for the file to delete.
     * @return				- Deletion confirmation message, or error message if the delete failed.
     */
    public static String deleteS3File(
    		 AWSCredentials credentials
    		,String bucketName
    		,String key
    	) {
        String result = "Delete s3://" + bucketName + "/" + key + " ... ";
        try {
            AmazonS3Client conn = new AmazonS3Client(credentials);
            conn.deleteObject(new DeleteObjectRequest(bucketName, key));
            result = String.valueOf(result) + "succeeded.";
        }
        catch (Exception ex) {
            result = String.valueOf(result) + "failed - " + ex.getMessage();
        }
        return result;
    }

    /**
	 * resolveURI - Resolve a URI in the form of S3://bucketName/path/to/file to a pre-signed URL to access the S3 File.  
	 * 
     * @param credentials	- Credentials for accessing AWS Resources
     * @param sourceURI
     * @param expirySeconds
     * @return
     */
    public static String resolveURI(
    		 AWSCredentials credentials
    		,String sourceURI
    		,Integer expirySeconds
    	) {
        String resolvedURI = sourceURI;
        Matcher resolver = Pattern.compile("^[sS]3://(?<b>[^/]+)/(?<k>.+)$").matcher(resolvedURI);
        if (resolver.matches()) {
            resolvedURI = AWS_S3_Helper.getS3FileURL(credentials, resolver.group("b"), resolver.group("k"), expirySeconds);
        }
        return resolvedURI;
    }
    
    /**
	 * listS3Files - List files from a specified S3 bucket, returning XML results.
	 * 
     * @param credentials	- Credentials for accessing AWS Resources
     * @param bucketName	- The name of the bucket containing the files.
     * @param prefix		- The prefix for the files to list.
     * @return				- List of files in XML format.
     * 
     * @throws Exception
     */
    public static Document listS3Files(
    		 AWSCredentials credentials
    		,String bucketName
    		,String prefix
    	) throws Exception {
    	Document filesList =  Utility.readXmlFromString("<Contents/>");
    	
    	Element contentRoot = filesList.getDocumentElement();
    	contentRoot.setAttribute("bucketName", bucketName);
    	contentRoot.setAttribute("prefix", prefix);
    	
    	try {
            AmazonS3Client conn = new AmazonS3Client(credentials);
            ObjectListing foundObjects = conn.listObjects(bucketName, prefix);
            while (true) {
                for (Iterator<?> iterator =
                        foundObjects.getObjectSummaries().iterator();
                        iterator.hasNext();) {
                    S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
                    
                    Element key = filesList.createElement("Key");
                    
                    key.setTextContent(summary.getKey());
                    
                    contentRoot.appendChild(key);
                    // Add result for summary.getKey());
                }
        
                // more object_listing to retrieve?
                if (foundObjects.isTruncated()) {
                    foundObjects = conn.listNextBatchOfObjects(foundObjects);
                } else {
                    break;
                }
            }
    	    
    	}
        catch (Exception ex) {
    	   contentRoot.setAttribute("error", ex.getMessage());
        }

        return filesList;
    }
}

