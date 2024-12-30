package server.life;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;

public class MapleHaku {
   private Point pos;
   private int stance;
   private int hair;
   private int face;

   public MapleHaku(Point pos, int stance, int hair, int face) {
      this.setPos(pos);
      this.setStance(stance);
      this.setHair(hair);
      this.setFace(face);
   }

   public Point getPos() {
      return this.pos;
   }

   public void setPos(Point pos) {
      this.pos = pos;
   }

   public int getStance() {
      return this.stance;
   }

   public void setStance(int stance) {
      this.stance = stance;
   }

   public int getHair() {
      return this.hair;
   }

   public void setHair(int hair) {
      this.hair = hair;
   }

   public int getFace() {
      return this.face;
   }

   public void setFace(int face) {
      this.face = face;
   }

   public final void updatePosition(List<LifeMovementFragment> movement) {
      Iterator var2 = movement.iterator();

      while(var2.hasNext()) {
         LifeMovementFragment move = (LifeMovementFragment)var2.next();
         if (move instanceof LifeMovement) {
            if (move instanceof AbsoluteLifeMovement) {
               this.setPos(((LifeMovement)move).getPosition());
            }

            this.setStance(((LifeMovement)move).getNewstate());
         }
      }

   }
}
