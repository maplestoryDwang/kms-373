package tools.data;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import tools.HexTool;

public class MaplePacketLittleEndianWriter {
   private final ByteArrayOutputStream baos;
   private static final Charset ASCII = Charset.forName("MS949");
   private int byteCount;
   private byte[] bytes;

   public MaplePacketLittleEndianWriter() {
      this(32);
   }

   public MaplePacketLittleEndianWriter(int size) {
      this.byteCount = 0;
      this.bytes = new byte[8];
      this.baos = new ByteArrayOutputStream(size);
   }

   public final byte[] getPacket() {
      return this.baos.toByteArray();
   }

   public final String toString() {
      return HexTool.toString(this.baos.toByteArray());
   }

   public final void writeZeroBytes(int i) {
      for(int x = 0; x < i; ++x) {
         this.baos.write(0);
      }

   }

   public final void writeBit(int b, int bit) {
      for(int i = 0; i < bit; ++i) {
         this.bytes[this.byteCount] = (byte)(b >>> i & 255 & 1);
         ++this.byteCount;
         if (this.byteCount == 8) {
            byte data = 0;

            for(int a = 0; a < 8; ++a) {
               data = (byte)(data + (this.bytes[a] << a));
            }

            this.baos.write(data);
            this.byteCount = 0;
         }
      }

   }

   public final void write(byte[] b) {
      for(int x = 0; x < b.length; ++x) {
         this.baos.write(b[x]);
      }

   }

   public final void write(boolean b) {
      this.baos.write(b ? 1 : 0);
   }

   public void write(byte b) {
      this.baos.write(b);
   }

   public void write(int b) {
      if (b != -88888) {
         this.baos.write((byte)b);
      }

   }

   public final void writeShort(int i) {
      this.baos.write((byte)(i & 255));
      this.baos.write((byte)(i >>> 8 & 255));
   }

   public final void writeInt(int i) {
      if (i != -88888) {
         this.baos.write((byte)(i & 255));
         this.baos.write((byte)(i >>> 8 & 255));
         this.baos.write((byte)(i >>> 16 & 255));
         this.baos.write((byte)(i >>> 24 & 255));
      }

   }

   public void writeInt(long i) {
      this.baos.write((byte)((int)(i & 255L)));
      this.baos.write((byte)((int)(i >>> 8 & 255L)));
      this.baos.write((byte)((int)(i >>> 16 & 255L)));
      this.baos.write((byte)((int)(i >>> 24 & 255L)));
   }

   public final void writeAsciiString(String s) {
      this.write(s.getBytes(ASCII));
   }

   public final void writeAsciiString(String s, int max) {
      if (s.getBytes(ASCII).length > max) {
         s = s.substring(0, max);
      }

      this.write(s.getBytes(ASCII));

      for(int i = s.getBytes(ASCII).length; i < max; ++i) {
         this.write((int)0);
      }

   }

   public final void writeMapleAsciiString(String s) {
      this.writeShort((short)s.getBytes(ASCII).length);
      this.writeAsciiString(s);
   }

   public final void writePos(Point s) {
      this.writeShort(s.x);
      this.writeShort(s.y);
   }

   public void writePosInt(Point s) {
      this.writeInt(s.x);
      this.writeInt(s.y);
   }

   public final void writeNRect(Rectangle s) {
      this.writeInt(s.x);
      this.writeInt(s.y);
      this.writeInt(s.width);
      this.writeInt(s.height);
   }

   public final void writeRect(Rectangle s) {
      this.writeInt(s.x);
      this.writeInt(s.y);
      this.writeInt(s.x + s.width);
      this.writeInt(s.y + s.height);
   }

   public final void writeMapleAsciiString2(String s) {
      this.writeShort((short)s.getBytes(Charset.forName("UTF-8")).length);
      this.write(s.getBytes(Charset.forName("UTF-8")));
   }

   public final void writeLong(long l) {
      this.baos.write((byte)((int)(l & 255L)));
      this.baos.write((byte)((int)(l >>> 8 & 255L)));
      this.baos.write((byte)((int)(l >>> 16 & 255L)));
      this.baos.write((byte)((int)(l >>> 24 & 255L)));
      this.baos.write((byte)((int)(l >>> 32 & 255L)));
      this.baos.write((byte)((int)(l >>> 40 & 255L)));
      this.baos.write((byte)((int)(l >>> 48 & 255L)));
      this.baos.write((byte)((int)(l >>> 56 & 255L)));
   }

   public final void writeReversedLong(long l) {
      this.baos.write((byte)((int)(l >>> 32 & 255L)));
      this.baos.write((byte)((int)(l >>> 40 & 255L)));
      this.baos.write((byte)((int)(l >>> 48 & 255L)));
      this.baos.write((byte)((int)(l >>> 56 & 255L)));
      this.baos.write((byte)((int)(l & 255L)));
      this.baos.write((byte)((int)(l >>> 8 & 255L)));
      this.baos.write((byte)((int)(l >>> 16 & 255L)));
      this.baos.write((byte)((int)(l >>> 24 & 255L)));
   }

   public final void writeDouble(double d) {
      this.writeLong(Double.doubleToLongBits(d));
   }
}
