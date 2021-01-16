package filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileDownloader {
  private static final int NUM_ACCOUNTS = 3;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static void main(String[] args) throws IOException {
    String file = CommonUtils.getRequiredProperty("file");
    String downloadPath = CommonUtils.getRequiredProperty("out");
    String keyFile = CommonUtils.getRequiredProperty("key");

    Path filePath = Paths.get(downloadPath, file);
    log.info("Downloading file {} to {}", file, filePath);

    Key key = MAPPER.readValue(new File(keyFile), Key.class);
    int key1 = key.getKey1();
    int key2 = key.getKey2();
    int key3 = key.getKey3();

    try {
      List<byte[]> encryptedContents = new ArrayList<>();
      S3Utils s3Utils = new S3Utils();
      for (int i = 0; i < NUM_ACCOUNTS; i++) {
        encryptedContents.add(s3Utils.read(S3Conf.getBucketName(i), file));
      }

      byte[] decryptedContent = CRTUtils.decrypt(encryptedContents, key1, key2, key3);
      Path p = Paths.get(downloadPath, file);
      log.debug("Decryption path: {}", p);

      boolean b = p.toFile().getParentFile().mkdirs();
      log.debug("Directory created {}", b);

      Files.write(p, decryptedContent);
    } catch (IOException e) {
      log.error("IO Error", e);
    }
  }
}
