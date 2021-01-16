package filesystem;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CRTUtils {

  public static List<byte[]> encrypt(byte[] content, int... keys) {
    List<byte[]> encryptedContents = new ArrayList<>();
    for (int key : keys) {
      byte[] encContent = new byte[content.length];
      for (int i = 0; i < content.length; i++) {
        encContent[i] = (byte) (unsignedToBytes(content[i]) % key);
      }
      encryptedContents.add(encContent);
    }
    return encryptedContents;
  }

  public static byte[] decrypt(List<byte[]> encryptedContents, int... keys) {
    int size = encryptedContents.get(0).length;
    byte[] decContent = new byte[size];

    for (int i = 0; i < size; i++) {
      List<Byte> byteList = new ArrayList<>();
      for (byte[] fileContent : encryptedContents) {
        byteList.add(fileContent[i]);
      }
      byte val = (byte) solveCRT(byteList, keys);
      decContent[i] = val;
    }
    return decContent;
  }

  private static int solveCRT(List<Byte> byteList, int... keys) {
    int productOfKeys = 1;
    for (int key : keys) {
      productOfKeys *= key;
    }

    int result = 0;
    for (int i = 0; i < keys.length; i++) {
      int partialProduct = productOfKeys / keys[i];
      result += unsignedToBytes(byteList.get(i)) * inv(partialProduct, keys[i]) * partialProduct;
    }
    return result % productOfKeys;
  }

  /*
   * Say, N = partialProduct, M = key
   * N, M are co-prime
   * => gcd(N,M) = 1
   * => x.N + y.M = 1
   * => x.N - 1 = (- y).M
   * => N . x ≡ 1 mod M
   *
   * Solving for x here
   */
  private static int inv(int partialProduct, int key) {
    int initialVal = key;
    // initial value
    int x = 1;
    int y = 0;

    int quotient;
    int tmp;

    // base check
    if (key == 1) {
      return 0;
    }

    /* using Euclidean Theorem
     * x’ = y - ⌊N/M⌋ * x
     * y’ = x */
    while (partialProduct > 1) {
      quotient = partialProduct / key;
      tmp = key;

      key = partialProduct % key;
      partialProduct = tmp;
      tmp = y;

      y = x - quotient * y;
      x = tmp;
    }

    if (x < 0) {
      // handling negative val
      x += initialVal;
    }
    return x;
  }

  public static int unsignedToBytes(byte b) {
    return b & 0xFF;
  }
}
