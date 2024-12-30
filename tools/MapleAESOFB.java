package tools;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import tools.data.MaplePacketLittleEndianWriter;

public class MapleAESOFB {
   private byte[] iv;
   private Cipher cipher;
   private short mapleVersion;
   private boolean isChannel;
   private boolean isOutbound;
   private static final byte[] sSecretKey = new byte[]{-120, 0, 0, 0, 107, 0, 0, 0, -7, 0, 0, 0, 113, 0, 0, 0, 13, 0, 0, 0, -122, 0, 0, 0, -37, 0, 0, 0, 79, 0, 0, 0};
   private static final byte[] funnyBytes = new byte[]{-20, 63, 119, -92, 69, -48, 113, -65, -73, -104, 32, -4, 75, -23, -77, -31, 92, 34, -9, 12, 68, 27, -127, -67, 99, -115, -44, -61, -14, 16, 25, -32, -5, -95, 110, 102, -22, -82, -42, -50, 6, 24, 78, -21, 120, -107, -37, -70, -74, 66, 122, 42, -125, 11, 84, 103, 109, -24, 101, -25, 47, 7, -13, -86, 39, 123, -123, -80, 38, -3, -117, -87, -6, -66, -88, -41, -53, -52, -110, -38, -7, -109, 96, 45, -35, -46, -94, -101, 57, 95, -126, 33, 76, 105, -8, 49, -121, -18, -114, -83, -116, 106, -68, -75, 107, 89, 19, -15, 4, 0, -10, 90, 53, 121, 72, -113, 21, -51, -105, 87, 18, 62, 55, -1, -99, 79, 81, -11, -93, 112, -69, 20, 117, -62, -72, 114, -64, -19, 125, 104, -55, 46, 13, 98, 70, 23, 17, 77, 108, -60, 126, 83, -63, 37, -57, -102, 28, -120, 88, 44, -119, -36, 2, 100, 64, 1, 93, 56, -91, -30, -81, 85, -43, -17, 26, 124, -89, 91, -90, 111, -122, -97, 115, -26, 10, -34, 43, -103, 74, 71, -100, -33, 9, 118, -98, 48, 14, -28, -78, -108, -96, 59, 52, 29, 40, 15, 54, -29, 35, -76, 3, -40, -112, -56, 60, -2, 94, 50, 36, 80, 31, 58, 67, -118, -106, 65, 116, -84, 82, 51, -16, -39, 41, -128, -79, 22, -45, -85, -111, -71, -124, 127, 97, 30, -49, -59, -47, 86, 61, -54, -12, 5, -58, -27, 8, 73};

   public MapleAESOFB(byte[] iv, short mapleVersion, boolean isChannel) {
      this(iv, mapleVersion, isChannel, false);
   }

   public MapleAESOFB(byte[] iv, short mapleVersion, boolean isChannel, boolean isOutbound) {
      Key pKey = new SecretKeySpec(sSecretKey, "AES");
      SecureRandom pRandom = new SecureRandom();
      pRandom.nextBytes(iv);

      try {
         this.cipher = Cipher.getInstance("AES");
      } catch (NoSuchAlgorithmException var9) {
         Logger.getLogger(MapleAESOFB.class.getName()).log(Level.SEVERE, (String)null, var9);
      } catch (NoSuchPaddingException var10) {
         Logger.getLogger(MapleAESOFB.class.getName()).log(Level.SEVERE, (String)null, var10);
      }

      try {
         this.cipher.init(1, pKey);
      } catch (InvalidKeyException var8) {
         Logger.getLogger(MapleAESOFB.class.getName()).log(Level.SEVERE, (String)null, var8);
      }

      this.isChannel = isChannel;
      this.isOutbound = isOutbound;
      this.setIv(iv);
      this.mapleVersion = (short)(mapleVersion >> 8 & 255 | mapleVersion << 8 & '\uff00');
   }

   private void setIv(byte[] iv) {
      this.iv = iv;
   }

   public byte[] getIv() {
      return this.iv;
   }

   public byte[] crypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
      if (this.isChannel && this.isOutbound) {
         byte[] arrayOfByte = new byte[data.length];
         System.arraycopy(data, 0, arrayOfByte, 0, data.length);

         for(int i = 0; i < arrayOfByte.length; ++i) {
            arrayOfByte[i] += this.iv[0];
         }

         this.updateIv();
         return arrayOfByte;
      } else {
         int remaining = data.length;
         byte[] datac = new byte[remaining];
         System.arraycopy(data, 0, datac, 0, data.length);
         int llength = 1456;

         for(int start = 0; remaining > 0; llength = 1460) {
            byte[] myIv = BitTools.multiplyBytes(this.iv, 4, 4);
            if (remaining < llength) {
               llength = remaining;
            }

            for(int x = start; x < start + llength; ++x) {
               if ((x - start) % myIv.length == 0) {
                  byte[] newIv = this.cipher.doFinal(myIv);

                  for(int j = 0; j < myIv.length; ++j) {
                     myIv[j] = newIv[j];
                  }
               }

               datac[x] ^= myIv[(x - start) % myIv.length];
            }

            start += llength;
            remaining -= llength;
         }

         this.updateIv();
         return datac;
      }
   }

   private void updateIv() {
      this.iv = getNewIv(this.iv);
   }

   public byte[] getPacketHeader(int length) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      int uSeqSnd = (this.iv[2] & 255 | this.iv[3] << 8) & '\uffff';
      uSeqSnd ^= 64386;
      mplew.writeShort((short)uSeqSnd);
      if (length >= 65280) {
         mplew.writeShort((short)('\uff00' ^ uSeqSnd));
         mplew.writeInt(length ^ uSeqSnd);
      } else {
         mplew.writeShort((short)(length ^ uSeqSnd));
      }

      return mplew.getPacket();
   }

   public static int getPacketLength(int packetHeader) {
      int packetLength = packetHeader >>> 16 ^ packetHeader & '\uffff';
      packetLength = packetLength << 8 & '\uff00' | packetLength >>> 8 & 255;
      return packetLength;
   }

   public boolean checkPacket(byte[] packet) {
      return true;
   }

   public boolean checkPacket(int packetHeader) {
      byte[] packetHeaderBuf = new byte[]{(byte)(packetHeader >> 24 & 255), (byte)(packetHeader >> 16 & 255)};
      return this.checkPacket(packetHeaderBuf);
   }

   public static byte[] getNewIv(byte[] oldIv) {
      byte[] newIv = new byte[]{-14, 83, 80, -58};

      for(int i = 0; i < 4; ++i) {
         Shuffle(oldIv[i], newIv);
      }

      return newIv;
   }

   private static byte[] Shuffle(byte inputValue, byte[] newIV) {
      byte elina = newIV[1];
      byte moritz = funnyBytes[elina & 255];
      moritz -= inputValue;
      newIV[0] += moritz;
      moritz = newIV[2];
      moritz ^= funnyBytes[inputValue & 255];
      elina = (byte)(elina - (moritz & 255));
      newIV[1] = elina;
      elina = newIV[3];
      moritz = elina;
      elina = (byte)(elina - (newIV[0] & 255));
      moritz = funnyBytes[moritz & 255];
      moritz += inputValue;
      moritz ^= newIV[2];
      newIV[2] = moritz;
      elina = (byte)(elina + (funnyBytes[inputValue & 255] & 255));
      newIV[3] = elina;
      int merry = newIV[0] & 255;
      merry |= newIV[1] << 8 & '\uff00';
      merry |= newIV[2] << 16 & 16711680;
      merry |= newIV[3] << 24 & -16777216;
      int ret_value = merry >>> 29;
      merry <<= 3;
      ret_value |= merry;
      newIV[0] = (byte)(ret_value & 255);
      newIV[1] = (byte)(ret_value >> 8 & 255);
      newIV[2] = (byte)(ret_value >> 16 & 255);
      newIV[3] = (byte)(ret_value >> 24 & 255);
      return newIV;
   }

   public String toString() {
      return "IV: " + HexTool.toString(this.iv);
   }
}
