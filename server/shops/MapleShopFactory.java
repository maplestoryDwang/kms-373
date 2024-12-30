package server.shops;

import java.util.HashMap;
import java.util.Map;

public class MapleShopFactory {
   private final Map<Integer, MapleShop> shops = new HashMap();
   private final Map<Integer, MapleShop> npcShops = new HashMap();
   private static final MapleShopFactory instance = new MapleShopFactory();

   public static MapleShopFactory getInstance() {
      return instance;
   }

   public void clear() {
      this.shops.clear();
      this.npcShops.clear();
   }

   public MapleShop getShop(int shopId) {
      return this.shops.containsKey(shopId) ? (MapleShop)this.shops.get(shopId) : this.loadShop(shopId, true);
   }

   public MapleShop getShopForNPC(int npcId) {
      return this.npcShops.containsKey(npcId) ? (MapleShop)this.npcShops.get(npcId) : this.loadShop(npcId, false);
   }

   private MapleShop loadShop(int id, boolean isShopId) {
      MapleShop ret = MapleShop.createFromDB(id, isShopId);
      if (ret != null) {
         this.shops.put(ret.getId(), ret);
         this.npcShops.put(ret.getNpcId(), ret);
      } else if (isShopId) {
         this.shops.put(id, (Object)null);
      } else {
         this.npcShops.put(id, (Object)null);
      }

      return ret;
   }
}
