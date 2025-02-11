package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class AbsoluteLifeMovement extends AbstractLifeMovement {
   private Point pixelsPerSecond;
   private Point offset;
   private short v307;
   private int nAttr;

   public AbsoluteLifeMovement(int type, Point position, int duration, int newstate, short FH, byte unk) {
      super(type, position, duration, newstate, FH, unk);
   }

   public void setPixelsPerSecond(Point wobble) {
      this.pixelsPerSecond = wobble;
   }

   public void setOffset(Point wobble) {
      this.offset = wobble;
   }

   public void setnAttr(int nAttr) {
      this.nAttr = nAttr;
   }

   public void serialize(MaplePacketLittleEndianWriter packet) {
      packet.write(this.getType());
      packet.writePos(this.getPosition());
      packet.writePos(this.pixelsPerSecond);
      packet.writeShort(this.getFootHolds());
      if (this.getType() == 15 || this.getType() == 17) {
         packet.writeShort(this.nAttr);
      }

      packet.writePos(this.offset);
      packet.writeShort(this.v307);
      if (this.getType() != 73 && this.getType() != 75) {
         packet.write(this.getNewstate());
         packet.writeShort(this.getDuration());
         packet.write(this.getUnk());
      }

   }

   public short getV307() {
      return this.v307;
   }

   public void setV307(short v307) {
      this.v307 = v307;
   }
}
