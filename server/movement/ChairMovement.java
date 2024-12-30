package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class ChairMovement extends AbstractLifeMovement {
   private int unk;

   public ChairMovement(int type, Point position, int duration, int newstate, short unk, byte unk2) {
      super(type, position, duration, newstate, unk, unk2);
   }

   public void serialize(MaplePacketLittleEndianWriter packet) {
      packet.write(this.getType());
      packet.writePos(this.getPosition());
      packet.writeShort(this.getFootHolds());
      packet.writeInt(this.unk);
      packet.write(this.getNewstate());
      packet.writeShort(this.getDuration());
      packet.write(this.getUnk());
   }

   public void setUnk(int unk) {
      this.unk = unk;
   }
}
