package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class UnknownMovement2 extends AbstractLifeMovement {
   private Point pixelsPerSecond;
   private short xOffset;

   public UnknownMovement2(int type, Point position, int duration, int newstate, byte unk) {
      super(type, position, duration, newstate, (short)0, unk);
   }

   public void setPixelsPerSecond(Point wobble) {
      this.pixelsPerSecond = wobble;
   }

   public void setXOffset(short xOffset) {
      this.xOffset = xOffset;
   }

   public void serialize(MaplePacketLittleEndianWriter packet) {
      packet.write(this.getType());
      packet.writePos(this.getPosition());
      packet.writePos(this.pixelsPerSecond);
      packet.writeShort(this.xOffset);
      packet.write(this.getNewstate());
      packet.writeShort(this.getDuration());
      packet.write(this.getUnk());
   }
}
