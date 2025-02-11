package constants;

import handling.channel.handler.MovementParse;
import tools.HexTool;
import tools.data.ByteArrayByteStream;
import tools.data.LittleEndianAccessor;

public class ForceAtomParser {
   public static void main(String[] args) {
      byte[] data = HexTool.getByteArrayFromHexString("02 93 E8 BE 7C 33 8A 03 52 00 00 00 00 00 5C 07 B3 02 00 00 00 00 29 01 00 5C 07 B3 02 00 00 00 00 6F 00 00 00 00 00 FF FF 04 D2 00 00 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 00 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 00 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 04 0C 00 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 04 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 0C 01 00 5C 07 B3 02 00 00 00 00 6F 00 00 00 00 00 FF FF 04 2C 01 00 11 00 00 00 00 00 00 00 00 00");
      LittleEndianAccessor slea = new LittleEndianAccessor(new ByteArrayByteStream(data));
      slea.skip(22);
      MovementParse.parseMovement(slea, 1);
   }
}
