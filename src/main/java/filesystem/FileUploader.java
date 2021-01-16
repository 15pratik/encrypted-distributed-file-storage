package filesystem;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUploader {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String ENC_DEST_PATH = "/tmp/encFiles/";
  private static final String ACCOUNT_PREFIX = "part-%d";

  static {
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
  }

  public static void main(String[] args) throws IOException {
    String inputPath = CommonUtils.getRequiredProperty("file");
    String outputPath = CommonUtils.getRequiredProperty("out");
    String keyFile = CommonUtils.getRequiredProperty("key");

    Path file = Paths.get(inputPath);
    if (!file.toFile().exists()) {
      throw new IllegalArgumentException("File does not exist " + file);
    }

    log.info("Uploading {} to {}", file.toUri(), outputPath);

    Key key = mapper.readValue(new File(keyFile), Key.class);
    int key1 = key.getKey1();
    int key2 = key.getKey2();
    int key3 = key.getKey3();

    try {
      byte[] fileContent = Files.readAllBytes(file);
      List<byte[]> encryptedContent = CRTUtils.encrypt(fileContent, key1, key2, key3);
      S3Utils s3Utils = new S3Utils();
      for (int i = 0; i < encryptedContent.size(); i++) {
        Path p = Paths.get(ENC_DEST_PATH, String.format(ACCOUNT_PREFIX, i), outputPath);
        boolean b = p.toFile().getParentFile().mkdirs();
        log.debug("Directory created {}", b);

        Files.write(p, encryptedContent.get(i));
        try {
          s3Utils.upload(S3Conf.getBucketName(i), outputPath, p.toFile());
        } finally {
          Files.delete(p);
        }
      }
    } catch (IOException e) {
      log.error("IO Error", e);
    }
  }
}
