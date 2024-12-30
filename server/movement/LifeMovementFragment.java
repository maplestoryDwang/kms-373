package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public interface LifeMovementFragment {
   void serialize(MaplePacketLittleEndianWriter var1);

   Point getPosition();
}
