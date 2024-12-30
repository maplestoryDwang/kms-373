package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.World;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.Pair;
import tools.packet.MarriageEXPacket;
import tools.packet.PlayerShopPacket;

public abstract class AbstractPlayerStore extends MapleMapObject implements IMaplePlayerShop {
   protected boolean open = false;
   protected boolean available = false;
   protected String ownerName;
   protected String des;
   protected String pass;
   protected int ownerId;
   protected int owneraccount;
   protected int itemId;
   protected int channel;
   protected int map;
   protected AtomicLong meso = new AtomicLong(0L);
   protected WeakReference<MapleCharacter>[] chrs;
   protected List<String> visitors = new LinkedList();
   protected List<AbstractPlayerStore.BoughtItem> bought = new LinkedList();
   protected List<MaplePlayerShopItem> items = new LinkedList();

   public AbstractPlayerStore(MapleCharacter owner, int itemId, String desc, String pass, int slots) {
      this.setPosition(owner.getTruePosition());
      this.ownerName = owner.getName();
      this.ownerId = owner.getId();
      this.owneraccount = owner.getAccountID();
      this.itemId = itemId;
      this.des = desc;
      this.pass = pass;
      this.map = owner.getMapId();
      this.channel = owner.getClient().getChannel();
      this.chrs = new WeakReference[slots];

      for(int i = 0; i < this.chrs.length; ++i) {
         this.chrs[i] = new WeakReference((Object)null);
      }

   }

   public int getMaxSize() {
      return this.chrs.length + 1;
   }

   public int getSize() {
      return this.getFreeSlot() == -1 ? this.getMaxSize() : this.getFreeSlot();
   }

   public void broadcastToVisitors(byte[] packet) {
      this.broadcastToVisitors(packet, true);
   }

   public void broadcastToVisitors(byte[] packet, boolean owner) {
      WeakReference[] var3 = this.chrs;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         WeakReference<MapleCharacter> chr = var3[var5];
         if (chr != null && chr.get() != null) {
            ((MapleCharacter)chr.get()).getClient().getSession().writeAndFlush(packet);
         }
      }

      if (this.getShopType() != 1 && owner && this.getMCOwner() != null) {
         this.getMCOwner().getClient().getSession().writeAndFlush(packet);
      }

   }

   public void broadcastToVisitors(byte[] packet, int exception) {
      WeakReference[] var3 = this.chrs;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         WeakReference<MapleCharacter> chr = var3[var5];
         if (chr != null && chr.get() != null && this.getVisitorSlot((MapleCharacter)chr.get()) != exception) {
            ((MapleCharacter)chr.get()).getClient().getSession().writeAndFlush(packet);
         }
      }

      if (this.getShopType() != 1 && this.getMCOwner() != null && exception != this.ownerId) {
         this.getMCOwner().getClient().getSession().writeAndFlush(packet);
      }

   }

   public long getMeso() {
      return this.meso.get();
   }

   public void setMeso(long gainmeso) {
      this.meso.set(gainmeso);
   }

   public void setOpen(boolean open) {
      this.open = open;
   }

   public boolean isOpen() {
      return this.open;
   }

   public boolean saveItems() {
      if (this.getShopType() != 1) {
         return false;
      } else {
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM hiredmerch WHERE accountid = ? OR characterid = ?");
            ps.setInt(1, this.owneraccount);
            ps.setInt(2, this.ownerId);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO hiredmerch (characterid, accountid, Mesos, time) VALUES (?, ?, ?, ?)", 1);
            ps.setInt(1, this.ownerId);
            ps.setInt(2, this.owneraccount);
            ps.setLong(3, this.meso.get());
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               rs.close();
               ps.close();
               throw new RuntimeException("Error, adding merchant to DB");
            }

            int packageid = rs.getInt(1);
            rs.close();
            ps.close();
            Map<MapleInventoryType, List<Item>> iters = new HashMap();
            MapleInventoryType[] types = new MapleInventoryType[]{MapleInventoryType.EQUIPPED, MapleInventoryType.EQUIP, MapleInventoryType.USE, MapleInventoryType.SETUP, MapleInventoryType.ETC, MapleInventoryType.CASH};
            MapleInventoryType[] var7 = types;
            int var8 = types.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               MapleInventoryType type = var7[var9];
               iters.put(type, new ArrayList());
            }

            Iterator var21 = this.items.iterator();

            while(true) {
               MaplePlayerShopItem pItems;
               do {
                  do {
                     do {
                        if (!var21.hasNext()) {
                           var21 = iters.entrySet().iterator();

                           while(var21.hasNext()) {
                              Entry<MapleInventoryType, List<Item>> iter = (Entry)var21.next();
                              ItemLoader.HIRED_MERCHANT.saveItems((List)iter.getValue(), packageid, (MapleInventoryType)iter.getKey(), false);
                           }

                           boolean var22 = true;
                           return var22;
                        }

                        pItems = (MaplePlayerShopItem)var21.next();
                     } while(pItems.item == null);
                  } while(pItems.bundles <= 0);
               } while(pItems.item.getQuantity() <= 0 && !GameConstants.isRechargable(pItems.item.getItemId()));

               Item item = pItems.item.copy();
               item.setQuantity((short)(item.getQuantity() * pItems.bundles));
               ((List)iters.get(GameConstants.getInventoryType(item.getItemId()))).add(item);
            }
         } catch (SQLException var19) {
            var19.printStackTrace();
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
            } catch (SQLException var18) {
               var18.printStackTrace();
            }

         }

         return false;
      }
   }

   public MapleCharacter getVisitor(int num) {
      return (MapleCharacter)this.chrs[num].get();
   }

   public void update() {
      if (this.isAvailable() && this.getMCOwner() != null) {
         this.getMap().broadcastMessage(PlayerShopPacket.sendPlayerShopBox(this.getMCOwner()));
      }

   }

   public void addVisitor(MapleCharacter visitor) {
      int i = this.getFreeSlot();
      if (i > 0) {
         if (this.getShopType() >= 3) {
            if (this.getShopType() == 8) {
               this.broadcastToVisitors(MarriageEXPacket.MarriageVisit(visitor, i));
            } else {
               this.broadcastToVisitors(PlayerShopPacket.getMiniGameNewVisitor(visitor, i, (MapleMiniGame)this));
            }
         } else {
            this.broadcastToVisitors(PlayerShopPacket.shopVisitorAdd(visitor, i));
         }

         this.chrs[i - 1] = new WeakReference(visitor);
         if (!this.isOwner(visitor)) {
            this.visitors.add(visitor.getName());
         }

         if (this.getItemId() >= 4080000 && this.getItemId() <= 4080100) {
            if (i == 1) {
               this.update();
            }
         } else if (i == 3) {
            this.update();
         }
      }

   }

   public void removeVisitor(MapleCharacter visitor) {
      byte slot = this.getVisitorSlot(visitor);
      boolean shouldUpdate = this.getFreeSlot() == -1;
      if (slot > 0) {
         this.broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(slot), slot);
         this.chrs[slot - 1] = new WeakReference((Object)null);
         if (shouldUpdate) {
            this.update();
         }
      }

   }

   public byte getVisitorSlot(MapleCharacter visitor) {
      for(byte i = 0; i < this.chrs.length; ++i) {
         if (this.chrs[i] != null && this.chrs[i].get() != null && ((MapleCharacter)this.chrs[i].get()).getId() == visitor.getId()) {
            return (byte)(i + 1);
         }
      }

      if (visitor.getId() == this.ownerId) {
         return 0;
      } else {
         return -1;
      }
   }

   public void removeAllVisitors(int error, int type) {
      for(int i = 0; i < this.chrs.length; ++i) {
         MapleCharacter visitor = this.getVisitor(i);
         if (visitor != null) {
            if (type != -1) {
               visitor.getClient().getSession().writeAndFlush(PlayerShopPacket.shopErrorMessage(error, type));
            }

            this.broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(this.getVisitorSlot(visitor)), this.getVisitorSlot(visitor));
            visitor.setPlayerShop((IMaplePlayerShop)null);
            this.chrs[i] = new WeakReference((Object)null);
         }
      }

      this.update();
   }

   public String getOwnerName() {
      return this.ownerName;
   }

   public int getOwnerId() {
      return this.ownerId;
   }

   public int getOwnerAccId() {
      return this.owneraccount;
   }

   public String getDescription() {
      return this.des == null ? "" : this.des;
   }

   public List<Pair<Byte, MapleCharacter>> getVisitors() {
      List<Pair<Byte, MapleCharacter>> chrz = new LinkedList();

      for(byte i = 0; i < this.chrs.length; ++i) {
         if (this.chrs[i] != null && this.chrs[i].get() != null) {
            chrz.add(new Pair((byte)(i + 1), (MapleCharacter)this.chrs[i].get()));
         }
      }

      return chrz;
   }

   public List<MaplePlayerShopItem> getItems() {
      return this.items;
   }

   public void addItem(MaplePlayerShopItem item) {
      this.items.add(item);
   }

   public boolean removeItem(int item) {
      return false;
   }

   public void removeFromSlot(int slot) {
      this.items.remove(slot);
   }

   public byte getFreeSlot() {
      for(byte i = 0; i < this.chrs.length; ++i) {
         if (this.chrs[i] == null || this.chrs[i].get() == null) {
            return (byte)(i + 1);
         }
      }

      return -1;
   }

   public int getItemId() {
      return this.itemId;
   }

   public boolean isOwner(MapleCharacter chr) {
      return chr.getId() == this.ownerId && chr.getName().equals(this.ownerName);
   }

   public String getPassword() {
      return this.pass == null ? "" : this.pass;
   }

   public void sendDestroyData(MapleClient client) {
   }

   public void sendSpawnData(MapleClient client) {
   }

   public MapleMapObjectType getType() {
      return MapleMapObjectType.SHOP;
   }

   public MapleCharacter getMCOwnerWorld() {
      int ourChannel = World.Find.findChannel(this.ownerId);
      return ourChannel <= 0 ? null : ChannelServer.getInstance(ourChannel).getPlayerStorage().getCharacterById(this.ownerId);
   }

   public MapleCharacter getMCOwnerChannel() {
      return ChannelServer.getInstance(this.channel).getPlayerStorage().getCharacterById(this.ownerId);
   }

   public MapleCharacter getMCOwner() {
      return this.getMap().getCharacterById(this.ownerId);
   }

   public MapleMap getMap() {
      return ChannelServer.getInstance(this.channel).getMapFactory().getMap(this.map);
   }

   public int getGameType() {
      if (this.getShopType() == 1) {
         return 6;
      } else if (this.getShopType() == 2) {
         return 5;
      } else if (this.getShopType() == 3) {
         return 1;
      } else if (this.getShopType() == 4) {
         return 2;
      } else {
         return this.getShopType() == 8 ? 8 : 0;
      }
   }

   public boolean isAvailable() {
      return this.available;
   }

   public void setAvailable(boolean b) {
      this.available = b;
   }

   public List<AbstractPlayerStore.BoughtItem> getBoughtItems() {
      return this.bought;
   }

   public String getMemberNames() {
      String ret = "";
      WeakReference[] var2 = this.chrs;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         WeakReference<MapleCharacter> chr = var2[var4];
         if (chr != null && chr.get() != null) {
            ret = ret + ((MapleCharacter)chr.get()).getName() + ", ";
         }
      }

      return ret;
   }

   public static final class BoughtItem {
      public int id;
      public int quantity;
      public long totalPrice;
      public String buyer;

      public BoughtItem(int id, int quantity, long totalPrice, String buyer) {
         this.id = id;
         this.quantity = quantity;
         this.totalPrice = totalPrice;
         this.buyer = buyer;
      }
   }
}
