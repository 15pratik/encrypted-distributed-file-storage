package filesystem;

public class S3Conf {
  public static final String ACCESS_KEY = "accessKey";
  public static final String SECRET_KEY = "secretKey";
  public static final String ENDPOINT = "http://127.0.0.1:9000";
  public static final String BUCKET_FORMAT = "my-bucket-%d";

  public static String getBucketName(int bucketNumber) {
    return String.format(BUCKET_FORMAT, bucketNumber);
  }
}
