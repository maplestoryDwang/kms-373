package server;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import database.DatabaseException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import tools.packet.CField;

public class MapleStorage implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   private int id;
   private int accountId;
   private List<Item> items;
   private long meso;
   private int lastNPC = 0;
   private short slots = 128;
   private boolean changed = false;
   private Map<MapleInventoryType, List<Item>> typeItems = new EnumMap(MapleInventoryType.class);

   private MapleStorage(int id, short slots, long meso, int accountId) {
      this.id = id;
      this.slots = 128;
      this.items = new LinkedList();
      this.meso = meso;
      this.accountId = accountId;
   }

   public static int create(int id) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO storages (accountid, slots, meso) VALUES (?, ?, ?)", 1);
         ps.setInt(1, id);
         ps.setInt(2, 4);
         ps.setLong(3, 0L);
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            ps.close();
            rs.close();
            con.close();
            throw new DatabaseException("Inserting char failed.");
         }

         int storageid = rs.getInt(1);
         ps.close();
         rs.close();
         con.close();
         var5 = storageid;
      } catch (Exception var17) {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
               return 0;
            }
         } catch (SQLException var16) {
            var16.printStackTrace();
         }

         return 0;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      return var5;
   }

   public static MapleStorage loadStorage(int id) {
      MapleStorage ret = null;
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM storages WHERE accountid = ?");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         int storeId;
         if (!rs.next()) {
            storeId = create(id);
            ret = new MapleStorage(storeId, (short)4, 0L, id);
            rs.close();
            ps.close();
         } else {
            storeId = rs.getInt("storageid");
            ret = new MapleStorage(storeId, rs.getShort("slots"), rs.getLong("meso"), id);
            rs.close();
            ps.close();
            MapleInventoryType[] var6 = MapleInventoryType.values();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               MapleInventoryType type = var6[var8];
               if (type.getType() > 0) {
                  Iterator var10 = ItemLoader.STORAGE.loadItems(false, id, type).entrySet().iterator();

                  while(var10.hasNext()) {
                     Entry<Long, Item> mit = (Entry)var10.next();
                     ret.items.add((Item)mit.getValue());
                  }
               }
            }
         }

         con.close();
      } catch (SQLException var20) {
         System.err.println("Error loading storage" + var20);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            var19.printStackTrace();
         }

      }

      return ret;
   }

   public void saveToDB(Connection con) {
      if (this.changed) {
         try {
            PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?");
            ps.setInt(1, 128);
            ps.setLong(2, this.meso);
            ps.setInt(3, this.id);
            ps.executeUpdate();
            ps.close();
            Map<MapleInventoryType, List<Item>> listing = new HashMap();
            MapleInventoryType[] types = new MapleInventoryType[]{MapleInventoryType.EQUIP, MapleInventoryType.USE, MapleInventoryType.SETUP, MapleInventoryType.ETC, MapleInventoryType.CASH, MapleInventoryType.CODY};
            MapleInventoryType[] var5 = types;
            int var6 = types.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               MapleInventoryType type = var5[var7];
               listing.put(type, new ArrayList());
            }

            Iterator var10 = this.items.iterator();

            while(var10.hasNext()) {
               Item item = (Item)var10.next();
               ((List)listing.get(GameConstants.getInventoryType(item.getItemId()))).add(item);
            }

            var10 = listing.entrySet().iterator();

            while(var10.hasNext()) {
               Entry<MapleInventoryType, List<Item>> iter = (Entry)var10.next();
               if (con != null) {
                  ItemLoader.STORAGE.saveItems((List)iter.getValue(), con, this.accountId, (MapleInventoryType)iter.getKey(), false);
               } else {
                  ItemLoader.STORAGE.saveItems((List)iter.getValue(), this.accountId, (MapleInventoryType)iter.getKey(), false);
               }
            }

            this.changed = false;
         } catch (SQLException var9) {
            System.err.println("Error saving storage" + var9);
         }

      }
   }

   public Item takeOut(byte slot) {
      if (slot < this.items.size() && slot >= 0) {
         this.changed = true;
         Item ret = (Item)this.items.remove(slot);
         MapleInventoryType type = GameConstants.getInventoryType(ret.getItemId());
         this.typeItems.put(type, this.filterItems(type));
         return ret;
      } else {
         return null;
      }
   }

   public void store(Item item) {
      this.changed = true;
      this.items.add(item);
      MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());
      this.typeItems.put(type, this.filterItems(type));
   }

   public void arrange() {
      Collections.sort(this.items, (o1, o2) -> {
         return o1.getItemId() < o2.getItemId() ? -1 : (o1.getItemId() == o2.getItemId() ? 0 : 1);
      });
      MapleInventoryType[] var1 = MapleInventoryType.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         MapleInventoryType type = var1[var3];
         this.typeItems.put(type, this.items);
      }

   }

   public List<Item> getItems() {
      return Collections.unmodifiableList(this.items);
   }

   private List<Item> filterItems(MapleInventoryType type) {
      List<Item> ret = new ArrayList();
      Iterator var3 = this.items.iterator();

      while(var3.hasNext()) {
         Item item = (Item)var3.next();
         if (GameConstants.getInventoryType(item.getItemId()) == type) {
            ret.add(item);
         }
      }

      return ret;
   }

   public byte getSlot(MapleInventoryType type, byte slot) {
      byte ret = 0;
      List<Item> it = (List)this.typeItems.get(type);
      if (it != null && slot < it.size() && slot >= 0) {
         for(Iterator var5 = this.items.iterator(); var5.hasNext(); ++ret) {
            Item item = (Item)var5.next();
            if (item == it.get(slot)) {
               return ret;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public void sendStorage(MapleClient c, int npcId) {
      this.lastNPC = npcId;
      Collections.sort(this.items, (o1, o2) -> {
         return GameConstants.getInventoryType(o1.getItemId()).getType() < GameConstants.getInventoryType(o2.getItemId()).getType() ? -1 : (GameConstants.getInventoryType(o1.getItemId()) == GameConstants.getInventoryType(o2.getItemId()) ? 0 : 1);
      });
      MapleInventoryType[] var3 = MapleInventoryType.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         MapleInventoryType type = var3[var5];
         this.typeItems.put(type, this.items);
      }

      c.getSession().writeAndFlush(CField.NPCPacket.getStorage(npcId, this.slots, this.items, this.meso));
   }

   public void update(MapleClient c) {
      c.getSession().writeAndFlush(CField.NPCPacket.arrangeStorage(this.slots, this.items, true));
   }

   public void sendStored(MapleClient c, MapleInventoryType type) {
      c.getSession().writeAndFlush(CField.NPCPacket.storeStorage(this.slots, type, (Collection)this.typeItems.get(type)));
   }

   public void sendTakenOut(MapleClient c, MapleInventoryType type) {
      c.getSession().writeAndFlush(CField.NPCPacket.takeOutStorage(this.slots, type, (Collection)this.typeItems.get(type)));
   }

   public long getMeso() {
      return this.meso;
   }

   public Item findById(int itemId) {
      Iterator var2 = this.items.iterator();

      Item item;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         item = (Item)var2.next();
      } while(item.getItemId() != itemId);

      return item;
   }

   public void setMeso(long meso) {
      if (meso >= 0L) {
         this.changed = true;
         this.meso = meso;
      }
   }

   public void sendMeso(MapleClient c) {
      c.getSession().writeAndFlush(CField.NPCPacket.mesoStorage(this.slots, this.meso));
   }

   public boolean isFull() {
      return this.items.size() >= this.slots;
   }

   public int getSlots() {
      return this.slots;
   }

   public void increaseSlots(byte gain) {
      this.changed = true;
      this.slots = (short)(this.slots + gain);
   }

   public void setSlots(byte set) {
      this.changed = true;
      this.slots = (short)set;
   }

   public void close() {
      this.typeItems.clear();
   }
}
