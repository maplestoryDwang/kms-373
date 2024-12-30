package tools.data;

import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class LittleEndianAccessor {
   private final ByteArrayByteStream bs;
   public int bits = 0;

   public LittleEndianAccessor(ByteArrayByteStream bs) {
      this.bs = bs;
   }

   public final byte[] getByteArray() {
      return this.bs.getByteArray();
   }

   public final int readBit(int bit) throws IOException {
      byte[] arr = new byte[8];
      byte b = (byte)(this.bs.getByteArray()[(int)this.bs.getPosition()] & 255);
      int bb = this.bits;
      int v = 0;

      for(int i = 0; i < bit; ++i) {
         arr[this.bits] = (byte)(b >>> this.bits & 255 & 1);
         v += arr[this.bits] << i;
         ++this.bits;
         if (this.bits == 8) {
            arr = new byte[8];
            this.bs.seek(this.bs.getPosition() + 1L);
            this.bits = 0;
            if (bit - i > 1) {
               b = (byte)(this.bs.getByteArray()[(int)this.bs.getPosition()] & 255);
            }
         }
      }

      return v;
   }

   public final byte readByte() {
      return (byte)this.bs.readByte();
   }

   public final int readByteToInt() {
      return this.bs.readByte();
   }

   public final int readInt() {
      int byte1 = this.bs.readByte();
      int byte2 = this.bs.readByte();
      int byte3 = this.bs.readByte();
      int byte4 = this.bs.readByte();
      return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
   }

   public final short readShort() {
      int byte1 = this.bs.readByte();
      int byte2 = this.bs.readByte();
      return (short)((byte2 << 8) + byte1);
   }

   public final int readUShort() {
      int quest = this.readShort();
      if (quest < 0) {
         quest += 65536;
      }

      return quest;
   }

   public final char readChar() {
      return (char)this.readShort();
   }

   public final long readLong() {
      long byte1 = (long)this.bs.readByte();
      long byte2 = (long)this.bs.readByte();
      long byte3 = (long)this.bs.readByte();
      long byte4 = (long)this.bs.readByte();
      long byte5 = (long)this.bs.readByte();
      long byte6 = (long)this.bs.readByte();
      long byte7 = (long)this.bs.readByte();
      long byte8 = (long)this.bs.readByte();
      return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
   }

   public final float readFloat() {
      return Float.intBitsToFloat(this.readInt());
   }

   public final double readDouble() {
      return Double.longBitsToDouble(this.readLong());
   }

   public String readAsciiString(int n) {
      byte[] ret = new byte[n];

      for(int x = 0; x < n; ++x) {
         ret[x] = this.readByte();
      }

      return new String(ret, Charset.forName("MS949"));
   }

   public final String readAsciiString2(int n) {
      try {
         byte[] ret = new byte[n];

         for(int x = 0; x < n; ++x) {
            ret[x] = this.readByte();
         }

         return new String(ret, "UTF-8");
      } catch (UnsupportedEncodingException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public final String readMapleAsciiString2() {
      return this.readAsciiString2(this.readShort());
   }

   public final String readNullTerminatedAsciiString() {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      while(true) {
         byte b = this.readByte();
         if (b == 0) {
            byte[] buf = baos.toByteArray();
            char[] chrBuf = new char[buf.length];

            for(int x = 0; x < buf.length; ++x) {
               chrBuf[x] = (char)buf[x];
            }

            return String.valueOf(chrBuf);
         }

         baos.write(b);
      }
   }

   public final long getBytesRead() {
      return this.bs.getBytesRead();
   }

   public final String readMapleAsciiString() {
      return this.readAsciiString(this.readShort());
   }

   public final Point readPos() {
      int x = this.readShort();
      int y = this.readShort();
      return new Point(x, y);
   }

   public final Point readIntPos() {
      int x = this.readInt();
      int y = this.readInt();
      return new Point(x, y);
   }

   public final byte[] read(int num) {
      byte[] ret = new byte[num];

      for(int x = 0; x < num; ++x) {
         ret[x] = this.readByte();
      }

      return ret;
   }

   public final long available() {
      return this.bs.available();
   }

   public final String toString() {
      return this.bs.toString();
   }

   public final String toString(boolean b) {
      return this.bs.toString(b);
   }

   public final void seek(long offset) {
      try {
         this.bs.seek(offset);
      } catch (IOException var4) {
         System.err.println("Seek failed" + var4);
      }

   }

   public final long getPosition() {
      return this.bs.getPosition();
   }

   public final void skip(int num) {
      this.seek(this.getPosition() + (long)num);
   }
}
