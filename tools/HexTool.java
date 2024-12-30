package tools;

import java.io.ByteArrayOutputStream;

public class HexTool {
   private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

   public static final String toString(byte byteValue) {
      int tmp = byteValue << 8;
      char[] retstr = new char[]{HEX[tmp >> 12 & 15], HEX[tmp >> 8 & 15]};
      return String.valueOf(retstr);
   }

   public static final String toString(int intValue) {
      return Integer.toHexString(intValue);
   }

   public static final String toString(byte[] bytes) {
      StringBuilder hexed = new StringBuilder();

      for(int i = 0; i < bytes.length; ++i) {
         hexed.append(toString(bytes[i]));
         hexed.append(' ');
      }

      return hexed.substring(0, hexed.length() - 1);
   }

   public static final String toStringFromAscii(byte[] bytes) {
      byte[] ret = new byte[bytes.length];

      for(int x = 0; x < bytes.length; ++x) {
         if (bytes[x] < 32 && bytes[x] >= 0) {
            ret[x] = 46;
         } else {
            int chr = (short)bytes[x] & 255;
            ret[x] = (byte)chr;
         }
      }

      try {
         String str = new String(ret, "MS949");
         return str;
      } catch (Exception var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public static final String toPaddedStringFromAscii(byte[] bytes) {
      String str = toStringFromAscii(bytes);
      StringBuilder ret = new StringBuilder(str.length() * 3);

      for(int i = 0; i < str.length(); ++i) {
         ret.append(str.charAt(i));
         ret.append("  ");
      }

      return ret.toString();
   }

   public static byte[] getByteArrayFromHexString(String hex) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int nexti = 0;
      int nextb = 0;
      boolean highoc = true;

      while(true) {
         int number;
         for(number = -1; number == -1; ++nexti) {
            if (nexti == hex.length()) {
               return baos.toByteArray();
            }

            char chr = hex.charAt(nexti);
            if (chr >= '0' && chr <= '9') {
               number = chr - 48;
            } else if (chr >= 'a' && chr <= 'f') {
               number = chr - 97 + 10;
            } else if (chr >= 'A' && chr <= 'F') {
               number = chr - 65 + 10;
            } else {
               number = -1;
            }
         }

         if (highoc) {
            nextb = number << 4;
            highoc = false;
         } else {
            nextb |= number;
            highoc = true;
            baos.write(nextb);
         }
      }
   }

   public static final String getOpcodeToString(int op) {
      String var10000 = Integer.toHexString(op).toUpperCase();
      return "0x" + StringUtil.getLeftPaddedStr(var10000, '0', 4);
   }
}
