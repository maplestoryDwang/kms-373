package scripting;

import client.MapleClient;
import server.MaplePortal;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {
   private final MaplePortal portal;

   public PortalPlayerInteraction(MapleClient c, MaplePortal portal) {
      super(c, portal.getId(), c.getPlayer().getMapId());
      this.portal = portal;
   }

   public final MaplePortal getPortal() {
      return this.portal;
   }

   public final void inFreeMarket() {
      if (this.getMapId() != 910000000) {
         this.saveLocation("FREE_MARKET");
         this.playPortalSE();
         this.warp(910000000, "st00");
      }

   }

   public final void inArdentmill() {
      if (this.getMapId() != 910001000) {
         if (this.getPlayer().getLevel() >= 30) {
            this.saveLocation("ARDENTMILL");
            this.playPortalSE();
            this.warp(910001000, "st00");
         } else {
            this.playerMessage(5, "?덈꺼 30 ?댁긽 ?섏뼱??留덉씠?ㅽ꽣 鍮뚮줈 ?대룞?섏떎 ???덉뒿?덈떎.");
         }
      }

   }

   public void spawnMonster(int id) {
      this.spawnMonster(id, 1, this.portal.getPosition());
   }

   public void spawnMonster(int id, int qty) {
      this.spawnMonster(id, qty, this.portal.getPosition());
   }
}
