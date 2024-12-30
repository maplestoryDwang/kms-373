package tools.packet;

import client.AvatarLook;
import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import handling.SendPacketOpcode;
import handling.channel.handler.PlayerInteractionHandler;
import java.util.Iterator;
import server.marriage.MarriageMiniBox;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

public class MarriageEXPacket {
   public static byte[] MarriageRoom(MapleClient c, MarriageMiniBox marriage) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
      mplew.write((int)20);
      mplew.write((int)0);
      mplew.writeShort(0);
      mplew.write((int)8);
      mplew.write(marriage.getMaxSize());
      mplew.writeShort(marriage.getVisitorSlot(c.getPlayer()));
      AvatarLook.encodeAvatarLook(mplew, marriage.getMCOwner(), false, GameConstants.isZero(c.getPlayer().getJob()) && c.getPlayer().getGender() == 1);
      mplew.writeMapleAsciiString(marriage.getOwnerName());
      mplew.writeShort(marriage.getMCOwner().getJob());
      Iterator var3 = marriage.getVisitors().iterator();

      while(var3.hasNext()) {
         Pair<Byte, MapleCharacter> visitorz = (Pair)var3.next();
         mplew.write((Byte)visitorz.getLeft());
         AvatarLook.encodeAvatarLook(mplew, (MapleCharacter)visitorz.getRight(), false, GameConstants.isZero(((MapleCharacter)visitorz.right).getJob()) && ((MapleCharacter)visitorz.right).getGender() == 1);
         mplew.writeMapleAsciiString(((MapleCharacter)visitorz.getRight()).getName());
         mplew.writeShort(((MapleCharacter)visitorz.getRight()).getJob());
      }

      mplew.write((int)-1);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static final byte[] MarriageVisit(MapleCharacter chr, int slot) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
      mplew.write(PlayerInteractionHandler.Interaction.VISIT.action);
      mplew.write((int)0);
      mplew.writeShort(0);
      mplew.write(slot);
      AvatarLook.encodeAvatarLook(mplew, chr, false, GameConstants.isZero(chr.getJob()) && chr.getGender() == 1);
      mplew.writeMapleAsciiString(chr.getName());
      mplew.writeShort(chr.getJob());
      mplew.writeInt(0);
      return mplew.getPacket();
   }
}
