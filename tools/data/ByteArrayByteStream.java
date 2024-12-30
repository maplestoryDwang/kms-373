package tools.data;

import java.io.IOException;
import tools.HexTool;

public class ByteArrayByteStream {
   private int pos = 0;
   private long bytesRead = 0L;
   private final byte[] arr;

   public ByteArrayByteStream(byte[] arr) {
      this.arr = arr;
   }

   public byte[] getByteArray() {
      return this.arr;
   }

   public long getPosition() {
      return (long)this.pos;
   }

   public void seek(long offset) throws IOException {
      this.pos = (int)offset;
   }

   public long getBytesRead() {
      return this.bytesRead;
   }

   public int readByte() {
      ++this.bytesRead;
      return this.arr[this.pos++] & 255;
   }

   public String toString() {
      return this.toString(false);
   }

   public String toString(boolean b) {
      String nows = "";
      if (this.arr.length - this.pos > 0) {
         byte[] now = new byte[this.arr.length - this.pos];
         System.arraycopy(this.arr, this.pos, now, 0, this.arr.length - this.pos);
         nows = HexTool.toString(now);
      }

      if (b) {
         String var10000 = HexTool.toString(this.arr);
         return "All: " + var10000 + "\nNow: " + nows;
      } else {
         return nows.makeConcatWithConstants<invokedynamic>(nows);
      }
   }

   public long available() {
      return (long)(this.arr.length - this.pos);
   }
}
