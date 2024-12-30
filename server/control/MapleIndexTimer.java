package server.control;

import client.MapleCharacter;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.channel.handler.InventoryHandler;
import java.util.Iterator;
import java.util.List;
import server.MapleInventoryManipulator;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;

public class MapleIndexTimer implements Runnable {
   public long lastClearDropTime = 0L;
   private MapleMapItem mapitem;

   public MapleIndexTimer() {
      this.lastClearDropTime = System.currentTimeMillis();
      System.out.println("[Loading Completed] MapleIndexTimer Start");
   }

   public void run() {
      long time = System.currentTimeMillis();
      Iterator channels = ChannelServer.getAllInstances().iterator();

      while(channels.hasNext()) {
         ChannelServer cs = (ChannelServer)channels.next();
         Iterator chrs = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(chrs.hasNext()) {
            MapleCharacter chr = (MapleCharacter)chrs.next();
            this.AutoRoot(chr);
         }
      }

   }

   public void AutoRoot(MapleCharacter chr) {
      if (chr.getKeyValue(12345, "AutoRoot") > 0L && !GameConstants.보스맵(chr.getMapId())) {
         List<MapleMapObject> objs = chr.getMap().getItemsInRange(chr.getPosition(), Double.MAX_VALUE);
         Iterator var3 = objs.iterator();

         while(true) {
            MapleMapObject ob;
            MapleMapItem mapitem;
            do {
               if (!var3.hasNext()) {
                  return;
               }

               ob = (MapleMapObject)var3.next();
               mapitem = (MapleMapItem)ob;
            } while(mapitem.getItem() != null && !MapleInventoryManipulator.checkSpace(chr.getClient(), mapitem.getItemId(), mapitem.getItem().getQuantity(), ""));

            if (!mapitem.isPickpoket() && !mapitem.isPlayerDrop()) {
               InventoryHandler.pickupItem(ob, chr.getClient(), chr);
            }
         }
      }
   }
}
