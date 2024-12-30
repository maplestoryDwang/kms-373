package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class RelativeLifeMovement extends AbstractLifeMovement {
   private int nAttr;
   private Point v307;

   public RelativeLifeMovement(int type, Point position, int duration, int newstate, byte unk) {
      super(type, position, duration, newstate, (short)0, unk);
   }

   public void setAttr(int nAttr) {
      this.nAttr = nAttr;
   }

   public void setV307(Point v307) {
      this.v307 = v307;
   }

   public void serialize(MaplePacketLittleEndianWriter packet) {
      packet.write(this.getType());
      packet.writePos(this.getPosition());
      if (this.getType() == 21 || this.getType() == 22) {
         packet.writeShort(this.nAttr);
      }

      if (this.getType() == 59) {
         packet.writePos(this.v307);
      }

      packet.write(this.getNewstate());
      packet.writeShort(this.getDuration());
      packet.write(this.getUnk());
   }
}
