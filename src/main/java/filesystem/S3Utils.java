package filesystem;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3Utils {
  private static final int BUFFER_SIZE = 1024 * 1024;
  private final AmazonS3 s3Client;

  public S3Utils() {
    s3Client =
        AmazonS3ClientBuilder.standard()
            .withCredentials(
                new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(S3Conf.ACCESS_KEY, S3Conf.SECRET_KEY)))
            .withEndpointConfiguration(
                new EndpointConfiguration(S3Conf.ENDPOINT, Regions.US_EAST_1.getName()))
            .withPathStyleAccessEnabled(true)
            .build();
  }

  public void upload(String bucket, String key, File file) {
    long startTime = System.nanoTime();

    ObjectMetadata meta = new ObjectMetadata();
    meta.setContentLength(file.length());
    log.debug("Uploading to {} {}", bucket, key);
    PutObjectRequest putRequest = new PutObjectRequest(bucket, key, file);
    putRequest.getRequestClientOptions().setReadLimit(BUFFER_SIZE);
    s3Client.putObject(putRequest);

    long endTime = System.nanoTime();
    double seconds = (double) (endTime - startTime) / 1_000_000_000.0;
    log.info("Upload completed for {} time:{}", key, seconds);
  }

  public byte[] read(String bucket, String key) throws IOException {
    S3Object object = s3Client.getObject(new GetObjectRequest(bucket, key));
    try (InputStream stream = object.getObjectContent()) {
      return IOUtils.toByteArray(stream);
    }
  }
}
