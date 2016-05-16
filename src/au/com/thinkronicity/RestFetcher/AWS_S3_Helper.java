package au.com.thinkronicity.RestFetcher;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AWS S3 Helper functions.
 * 
 * @author Ian Hogan, Ian.Hogan@THINKronicity.com.au
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
}
