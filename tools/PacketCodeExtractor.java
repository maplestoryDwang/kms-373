package tools;

import java.util.Scanner;
import tools.data.ByteArrayByteStream;
import tools.data.LittleEndianAccessor;

public class PacketCodeExtractor {
   public static void main(String[] args) {
      long hair = 0L;
      Scanner sc = new Scanner(System.in);
      System.out.print("추출할 패킷 입력 : ");
      byte[] data = HexTool.getByteArrayFromHexString(sc.nextLine());
      LittleEndianAccessor slea = new LittleEndianAccessor(new ByteArrayByteStream(data));
      byte count = slea.readByte();

      int i;
      for(i = 0; i < 100; ++i) {
         hair = (long)slea.readInt();
         System.err.print(hair + ", ");
      }

      System.err.println("/");

      for(i = 0; i < 28; ++i) {
         hair = (long)slea.readInt();
         System.err.print(hair + ", ");
      }

      sc.nextLine();
   }
}
