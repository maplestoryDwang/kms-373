package server;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.GameConstants;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.CSPacket;

public class CashShop implements Serializable {
   private static final long serialVersionUID = 231541893513373579L;
   private int accountId;
   private int characterId;
   private ItemLoader factory;
   private List<Item> inventory;
   private List<Long> uniqueids;

   public CashShop(int accountId, int characterId, int jobType) throws SQLException {
      this.factory = ItemLoader.CASHSHOP;
      this.inventory = new ArrayList();
      this.uniqueids = new ArrayList();
      this.accountId = accountId;
      this.characterId = characterId;
      Iterator var4 = this.factory.loadCSItems(false, accountId).values().iterator();

      while(var4.hasNext()) {
         Pair<Item, MapleInventoryType> item = (Pair)var4.next();
         this.inventory.add((Item)item.getLeft());
      }

   }

   public int getItemsSize() {
      return this.inventory.size();
   }

   public List<Item> getInventory() {
      return this.inventory;
   }

   public Item findByCashId(long uniqueId, int itemId, byte type) {
      Iterator var5 = this.inventory.iterator();

      Item item;
      do {
         if (!var5.hasNext()) {
            return null;
         }

         item = (Item)var5.next();
      } while(item.getUniqueId() != uniqueId || item.getItemId() != itemId || GameConstants.getInventoryType(item.getItemId()).getType() != type);

      return item;
   }

   public void checkExpire(MapleClient c) {
      List<Item> toberemove = new ArrayList();
      Iterator var3 = this.inventory.iterator();

      Item item;
      while(var3.hasNext()) {
         item = (Item)var3.next();
         if (item != null && !GameConstants.isPet(item.getItemId()) && item.getExpiration() > 0L && item.getExpiration() < System.currentTimeMillis()) {
            toberemove.add(item);
         }
      }

      if (toberemove.size() > 0) {
         var3 = toberemove.iterator();

         while(var3.hasNext()) {
            item = (Item)var3.next();
            this.removeFromInventory(item);
            c.getSession().writeAndFlush(CSPacket.cashItemExpired(item.getUniqueId()));
         }

         toberemove.clear();
      }

   }

   public Item toItem(CashItemInfo cItem) {
      return this.toItem(cItem, MapleInventoryManipulator.getUniqueId(cItem.getId(), (MaplePet)null), "");
   }

   public Item toItem(CashItemInfo cItem, String gift) {
      return this.toItem(cItem, MapleInventoryManipulator.getUniqueId(cItem.getId(), (MaplePet)null), gift);
   }

   public Item toItem(CashItemInfo cItem, long uniqueid) {
      return this.toItem(cItem, uniqueid, "");
   }

   public Item toItem(CashItemInfo cItem, long uniqueid, String gift) {
      if (uniqueid <= 0L) {
         uniqueid = MapleInventoryIdentifier.getInstance();
      }

      long period = (long)cItem.getPeriod();
      Item ret = null;
      int var10001;
      if (GameConstants.getInventoryType(cItem.getId()) != MapleInventoryType.CODY && GameConstants.getInventoryType(cItem.getId()) != MapleInventoryType.EQUIP) {
         Item item = new Item(cItem.getId(), (short)0, (short)cItem.getCount(), 0, uniqueid);
         if (period > 0L) {
            item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
         }

         var10001 = cItem.getSN();
         item.setGMLog("Cash Shop: " + var10001 + " on " + FileoutputUtil.CurrentReadable_Date());
         item.setGiftFrom(gift);
         if (GameConstants.isPet(cItem.getId())) {
            MaplePet pet = MaplePet.createPet(cItem.getId(), uniqueid);
            if (pet != null) {
               item.setPet(pet);
            }
         }

         ret = item.copy();
      } else {
         Equip eq = (Equip)MapleItemInformationProvider.getInstance().getEquipById(cItem.getId(), uniqueid);
         if (period > 0L) {
            eq.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
         }

         var10001 = cItem.getSN();
         eq.setGMLog("Cash Shop: " + var10001 + " on " + FileoutputUtil.CurrentReadable_Date());
         eq.setGiftFrom(gift);
         if (GameConstants.isEffectRing(cItem.getId()) && uniqueid > 0L) {
            MapleRing ring = MapleRing.loadFromDb(uniqueid);
            if (ring != null) {
               eq.setRing(ring);
            }
         }

         ret = eq.copy();
      }

      return ret;
   }

   public void addToInventory(Item item) {
      this.inventory.add(item);
   }

   public void removeFromInventory(Item item) {
      this.inventory.remove(item);
   }

   public void gift(int recipient, String from, String message, int sn) {
      this.gift(recipient, from, message, sn, 0L);
   }

   public void gift(int recipient, String from, String message, int sn, long uniqueid) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO `gifts` VALUES (DEFAULT, ?, ?, ?, ?, ?)");
         ps.setInt(1, recipient);
         ps.setString(2, from);
         ps.setString(3, message);
         ps.setInt(4, sn);
         ps.setLong(5, uniqueid);
         ps.executeUpdate();
         ps.close();
         con.close();
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
               ((ResultSet)rs).close();
            }
         } catch (SQLException var18) {
            var18.printStackTrace();
         }

      }

   }

   public List<Pair<Item, String>> loadGifts() {
      List<Pair<Item, String>> gifts = new ArrayList();
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM `gifts` WHERE `recipient` = ?");
         ps.setInt(1, this.characterId);
         rs = ps.executeQuery();

         while(true) {
            while(true) {
               CashItemInfo cItem;
               do {
                  if (!rs.next()) {
                     rs.close();
                     ps.close();
                     ps = con.prepareStatement("DELETE FROM `gifts` WHERE `recipient` = ?");
                     ps.setInt(1, this.characterId);
                     ps.executeUpdate();
                     ps.close();
                     this.save((Connection)null);
                     return gifts;
                  }

                  cItem = CashItemFactory.getInstance().getItem(rs.getInt("sn"));
               } while(cItem == null);

               Item item = this.toItem(cItem, rs.getLong("uniqueid"), rs.getString("from"));
               gifts.add(new Pair(item, rs.getString("message")));
               this.uniqueids.add(item.getUniqueId());
               List<Integer> packages = CashItemFactory.getInstance().getPackageItems(cItem.getId());
               if (packages != null && packages.size() > 0) {
                  Iterator iterator = packages.iterator();

                  while(iterator.hasNext()) {
                     int packageItem = (Integer)iterator.next();
                     CashItemInfo pack = CashItemFactory.getInstance().getSimpleItem(packageItem);
                     if (pack != null) {
                        this.addToInventory(this.toItem(pack, rs.getString("from")));
                     }
                  }
               } else {
                  this.addToInventory(item);
               }
            }
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

      return gifts;
   }

   public boolean canSendNote(int uniqueid) {
      return this.uniqueids.contains(uniqueid);
   }

   public void sendedNote(int uniqueid) {
      for(int i = 0; i < this.uniqueids.size(); ++i) {
         if (((Long)this.uniqueids.get(i)).intValue() == uniqueid) {
            this.uniqueids.remove(i);
         }
      }

   }

   public void save(Connection con) throws SQLException {
      List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList();
      Iterator var3 = this.inventory.iterator();

      while(var3.hasNext()) {
         Item item = (Item)var3.next();
         itemsWithType.add(new Pair(item, GameConstants.getInventoryType(item.getItemId())));
      }

      this.factory.saveItems(itemsWithType, this.accountId);
   }
}
