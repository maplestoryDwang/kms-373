package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class UnknownMovement5 extends AbstractLifeMovement {
   private Point pixelsPerSecond;
   private byte ForcedStop;
   private int unnow;

   public UnknownMovement5(int type, Point position, int duration, int newstate, short FH, byte unk) {
      super(type, position, duration, newstate, FH, unk);
   }

   public Point getPixelsPerSecond() {
      return this.pixelsPerSecond;
   }

   public void setPixelsPerSecond(Point wobble) {
      this.pixelsPerSecond = wobble;
   }

   public int getUnow() {
      return this.unnow;
   }

   public void setUnow(int unk) {
      this.unnow = unk;
   }

   public void serialize(MaplePacketLittleEndianWriter packet) {
      packet.write(this.getType());
      packet.writePos(this.getPosition());
      packet.writeShort(this.getDuration());
      packet.writeShort(this.getNewstate());
      packet.writeInt(this.getFootHolds());
      packet.write(this.getUnk());
   }
}
