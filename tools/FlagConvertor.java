package tools;

import java.util.BitSet;
import java.util.Scanner;

public class FlagConvertor {
   public static void main(String[] args) {
      Scanner sc = new Scanner(System.in);

      while(sc.hasNext()) {
         try {
            String packet = sc.nextLine().strip();
            if (packet.length() > 11) {
               throw new RuntimeException("너무 긴 패킷입니다.");
            }

            byte[] data = HexTool.getByteArrayFromHexString(packet);
            BitSet b = BitSet.valueOf(data);

            for(int i = 0; i < b.size(); ++i) {
               if (b.get(i)) {
                  System.out.println("0x" + Integer.toHexString(1 << i % 32));
               }
            }
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }

   }
}
