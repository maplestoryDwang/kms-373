package client;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class LoginCryptoLegacy {
   private static final Random rand = new Random();
   private static final char[] iota64 = new char[64];

   public static final String hashPassword(String password) {
      byte[] randomBytes = new byte[6];
      rand.setSeed(System.currentTimeMillis());
      rand.nextBytes(randomBytes);
      return myCrypt(password, genSalt(randomBytes));
   }

   public static final boolean checkPassword(String password, String hash) {
      return myCrypt(password, hash).equals(hash);
   }

   public static final boolean isLegacyPassword(String hash) {
      return hash.substring(0, 3).equals("$H$");
   }

   private static final String myCrypt(String password, String seed) throws RuntimeException {
      String out = null;
      int count = 8;
      if (!seed.substring(0, 3).equals("$H$")) {
         byte[] randomBytes = new byte[6];
         rand.nextBytes(randomBytes);
         seed = genSalt(randomBytes);
      }

      String salt = seed.substring(4, 12);
      if (salt.length() != 8) {
         throw new RuntimeException("Error hashing password - Invalid seed.");
      } else {
         try {
            MessageDigest digester = MessageDigest.getInstance("SHA-1");
            digester.update((salt + password).getBytes("iso-8859-1"), 0, (salt + password).length());
            byte[] sha1Hash = digester.digest();

            do {
               byte[] CombinedBytes = new byte[sha1Hash.length + password.length()];
               System.arraycopy(sha1Hash, 0, CombinedBytes, 0, sha1Hash.length);
               System.arraycopy(password.getBytes("iso-8859-1"), 0, CombinedBytes, sha1Hash.length, password.getBytes("iso-8859-1").length);
               digester.update(CombinedBytes, 0, CombinedBytes.length);
               sha1Hash = digester.digest();
               --count;
            } while(count > 0);

            out = seed.substring(0, 12);
            out = out + encode64(sha1Hash);
            if (out == null) {
               throw new RuntimeException("Error hashing password - out = null");
            }

            return out;
         } catch (NoSuchAlgorithmException var8) {
            System.err.println("Error hashing password." + var8);
         } catch (UnsupportedEncodingException var9) {
            System.err.println("Error hashing password." + var9);
         }

         if (out == null) {
            throw new RuntimeException("Error hashing password - out = null");
         } else {
            return out;
         }
      }
   }

   private static final String genSalt(byte[] arrayOfByte) {
      StringBuilder Salt = new StringBuilder("$H$");
      Salt.append(iota64[30]);
      Salt.append(encode64(arrayOfByte));
      return Salt.toString();
   }

   private static final String convertToHex(byte[] data) {
      StringBuilder buf = new StringBuilder();
      int i = 0;
      if (i < data.length) {
         int halfbyte = data[i] >>> 4 & 15;
         int var4 = 0;

         while(true) {
            if (0 <= halfbyte && halfbyte <= 9) {
               buf.append((char)(48 + halfbyte));
            } else {
               buf.append((char)(97 + halfbyte - 10));
            }

            halfbyte = data[i] & 15;
            if (var4++ >= 1) {
               ++i;
            }
         }
      }

      return buf.toString();
   }

   public static final String encodeSHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update(text.getBytes("iso-8859-1"), 0, text.length());
      return convertToHex(md.digest());
   }

   private static final String encode64(byte[] Input) {
      int iLen = Input.length;
      int oDataLen = (iLen * 4 + 2) / 3;
      int oLen = (iLen + 2) / 3 * 4;
      char[] out = new char[oLen];
      int ip = 0;

      for(int op = 0; ip < iLen; ++op) {
         int i0 = Input[ip++] & 255;
         int i1 = ip < iLen ? Input[ip++] & 255 : 0;
         int i2 = ip < iLen ? Input[ip++] & 255 : 0;
         int o0 = i0 >>> 2;
         int o1 = (i0 & 3) << 4 | i1 >>> 4;
         int o2 = (i1 & 15) << 2 | i2 >>> 6;
         int o3 = i2 & 63;
         out[op++] = iota64[o0];
         out[op++] = iota64[o1];
         out[op] = op < oDataLen ? iota64[o2] : 61;
         ++op;
         out[op] = op < oDataLen ? iota64[o3] : 61;
      }

      return new String(out);
   }

   static {
      int i = 0;
      int var2 = i + 1;
      iota64[i] = '.';
      iota64[var2++] = '/';

      char c;
      for(c = 'A'; c <= 'Z'; iota64[var2++] = c++) {
      }

      for(c = 'a'; c <= 'z'; iota64[var2++] = c++) {
      }

      for(c = '0'; c <= '9'; iota64[var2++] = c++) {
      }

   }
}
