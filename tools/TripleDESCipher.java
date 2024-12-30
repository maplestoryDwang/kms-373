package tools;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class TripleDESCipher {
   public byte[] aKey = new byte[24];
   public Key pKey;

   public TripleDESCipher(byte[] aKey) {
      System.arraycopy(aKey, 0, this.aKey, 0, aKey.length);
      this.pKey = new SecretKeySpec(aKey, "DESede");
   }

   public byte[] Encrypt(byte[] aData) throws Exception {
      Cipher pCipher = Cipher.getInstance("DESede");
      pCipher.init(1, this.pKey);
      return pCipher.doFinal(aData);
   }

   public byte[] Decrypt(byte[] aData) throws Exception {
      Cipher pCipher = Cipher.getInstance("DESede");
      pCipher.init(2, this.pKey);
      return pCipher.doFinal(aData);
   }
}
