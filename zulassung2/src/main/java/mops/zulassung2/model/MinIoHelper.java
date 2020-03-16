package mops.zulassung2.model;

import io.minio.MinioClient;
import io.minio.ServerSideEncryption;
import io.minio.errors.*;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class MinIoHelper {
  private MinioClient minioClient;

  public MinIoHelper() {
    initializeClient();
  }

  private void initializeClient() {
    try {
      minioClient = new MinioClient("http://localhost:9000", "minioadmin", "minioadmin");
    } catch (InvalidEndpointException | InvalidPortException e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks whether given bucketName exists.
   *
   * @param bucketName name of the bucket
   * @return
   */
  public boolean bucketExists(String bucketName) {
    try {
      return minioClient.bucketExists(bucketName);
    } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException
            | IOException | InvalidKeyException | NoResponseException
            | XmlPullParserException | ErrorResponseException | InternalException
            | InvalidResponseException e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Creates a bucket with the given name.
   *
   * @param bucketName name of the bucket
   */
  public void makeBucket(String bucketName) {
    try {
      minioClient.makeBucket(bucketName);
    } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException
            | IOException | InvalidKeyException | NoResponseException
            | XmlPullParserException | ErrorResponseException | InternalException
            | InvalidResponseException | RegionConflictException e) {
      e.printStackTrace();
    }
  }

  /**
   * Removes a bucket with the given name.
   *
   * @param bucketName name of the bucket
   */
  public void removeBucket(String bucketName) {
    try {
      minioClient.removeBucket(bucketName);
    } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException
            | IOException | InvalidKeyException | NoResponseException
            | XmlPullParserException | ErrorResponseException | InternalException
            | InvalidResponseException e) {
      e.printStackTrace();
    }
  }

  /**
   * Puts an object into a bucket.
   *
   * @param bucketName  name of the bucket
   * @param objectName  name of the file
   * @param fileName    path + name of the file
   * @param size        size of the file
   * @param headerMap   additional metadata
   * @param contentType file content type
   */
  public void putObject(String bucketName, String objectName, String fileName, Long size,
                        Map<String, String> headerMap, String contentType) {
    try {
      minioClient.putObject(bucketName, objectName, fileName, size, headerMap,
              null, contentType); //no encryption necessary
    } catch (InvalidBucketNameException | NoSuchAlgorithmException | IOException
            | InvalidKeyException | NoResponseException | XmlPullParserException
            | ErrorResponseException | InternalException | InvalidArgumentException
            | InsufficientDataException | InvalidResponseException e) {
      e.printStackTrace();
    }
  }

  /**
   * Removes an object from a given bucket.
   *
   * @param bucketName name of the bucket
   * @param objectName name of the file
   */
  public void removeObject(String bucketName, String objectName) {
    try {
      minioClient.removeObject(bucketName, objectName);
    } catch (InvalidBucketNameException | NoSuchAlgorithmException | IOException
            | InvalidKeyException | NoResponseException | XmlPullParserException
            | ErrorResponseException | InternalException | InvalidArgumentException
            | InsufficientDataException | InvalidResponseException e) {
      e.printStackTrace();
    }
  }
}