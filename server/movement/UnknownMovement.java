package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class UnknownMovement extends AbstractLifeMovement {
   private Point pixelsPerSecond;

   public UnknownMovement(int type, Point position, int duration, int newstate, short FH, byte unk) {
      super(type, position, duration, newstate, FH, unk);
   }

   public Point getPixelsPerSecond() {
      return this.pixelsPerSecond;
   }

   public void setPixelsPerSecond(Point wobble) {
      this.pixelsPerSecond = wobble;
   }

   public void serialize(MaplePacketLittleEndianWriter packet) {
      packet.write(this.getType());
      packet.writePos(this.getPosition());
      packet.writePos(this.pixelsPerSecond);
      packet.writeShort(this.getFootHolds());
      packet.write(this.getNewstate());
      packet.writeShort(this.getDuration());
      packet.write(this.getUnk());
   }
}
