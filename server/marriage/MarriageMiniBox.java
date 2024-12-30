package server.marriage;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleRing;
import java.lang.ref.WeakReference;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleMapObjectType;
import server.shops.AbstractPlayerStore;
import server.shops.IMaplePlayerShop;
import tools.packet.MarriageEXPacket;
import tools.packet.PlayerShopPacket;

public class MarriageMiniBox extends AbstractPlayerStore {
   private boolean weddingexStart = false;
   private static final int slots = 7;
   private boolean[] exitAfter;
   private int GameType = 0;
   private int storeid;
   int turn = 1;
   int piecetype = 0;
   int firstslot = 0;
   int tie = -1;
   int Partnerid = -1;
   private MapleCharacter player1;
   private MapleCharacter player2;

   public MarriageMiniBox(MapleCharacter owner, int itemId, String description, String pass, int GameType, int partnerid) {
      super(owner, itemId, description, pass, 7);
      this.Partnerid = partnerid;
      this.GameType = GameType;
      this.exitAfter = new boolean[8];
   }

   public MapleCharacter getPlayer1() {
      return this.player1;
   }

   public MapleCharacter getPlayer2() {
      return this.player2;
   }

   public void setPlayer1(MapleCharacter p) {
      this.player1 = p;
   }

   public void setPlayer2(MapleCharacter p) {
      this.player2 = p;
   }

   public int getPartnerId() {
      return this.Partnerid;
   }

   public byte getShopType() {
      return 8;
   }

   public final void setStoreid(int storeid) {
      this.storeid = storeid;
   }

   public void removeVisitor(MapleCharacter visitor) {
      byte slot = this.getVisitorSlot(visitor);
      if (slot > 0) {
         if (slot == 1) {
            this.closeMarriageBox(true, 24);
            return;
         }

         this.broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(slot), slot);
         this.chrs[slot - 1] = new WeakReference((Object)null);
         this.update();
      }

   }

   public void buy(MapleClient c, int z, short i) {
   }

   public void EndMarriage() {
      this.sendPackets(PlayerShopPacket.WEDDING_INTER(109));
      this.update();
      this.closeMarriageBox(false, 3);
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Item item = ii.getEquipById(1112744);
      Item item1 = ii.getEquipById(1112744);
      item.setUniqueId(MapleInventoryIdentifier.getInstance());
      Equip eitem = (Equip)item;
      eitem.setStr((short)300);
      eitem.setDex((short)300);
      eitem.setInt((short)300);
      eitem.setLuk((short)300);
      eitem.setWatk((short)300);
      eitem.setMatk((short)300);
      item1.setUniqueId(MapleInventoryIdentifier.getInstance());
      Equip eitem1 = (Equip)item1;
      eitem1.setStr((short)300);
      eitem1.setDex((short)300);
      eitem1.setInt((short)300);
      eitem1.setLuk((short)300);
      eitem1.setWatk((short)300);
      eitem1.setMatk((short)300);

      try {
         MapleRing.makeRing(1112744, this.getPlayer1(), eitem.getUniqueId(), eitem1.getUniqueId());
         MapleRing.makeRing(1112744, this.getPlayer2(), eitem1.getUniqueId(), eitem.getUniqueId());
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      MapleInventoryManipulator.addbyItem(this.getPlayer1().getClient(), item);
      MapleInventoryManipulator.addbyItem(this.getPlayer2().getClient(), item1);
      this.getPlayer1().reloadChar();
      this.getPlayer2().reloadChar();
   }

   public void StartMarriage() {
      if (!this.getPlayer1().haveItem(5250500, 1)) {
         this.closeMarriageBox(true, 3);
      } else if (this.getPlayer1() != null && this.getPlayer1().getMarriageId() <= 0 && this.getPlayer2() != null && this.getPlayer2().getMarriageId() <= 0 && this.getPlayer2().getId() == this.getPartnerId() && this.getPlayer1().isAlive() && this.getPlayer2().isAlive()) {
         if (MapleInventoryManipulator.checkSpace(this.getPlayer1().getClient(), 1112744, 1, "") && MapleInventoryManipulator.checkSpace(this.getPlayer2().getClient(), 1112744, 1, "")) {
            this.getPlayer1().removeItem(5250500, -1);
            MarriageDataEntry data = MarriageManager.getInstance().makeNewMarriage(1);
            data.setStatus(2);
            data.setWeddingStatus(8);
            data.setBrideId(this.getPlayer1().getId());
            data.setBrideName(this.getPlayer1().getName());
            data.setGroomId(this.getPlayer2().getId());
            data.setGroomName(this.getPlayer2().getName());
            this.getPlayer1().setMarriageId(data.getMarriageId());
            this.getPlayer2().setMarriageId(data.getMarriageId());
            this.setOpen(false);
            this.update();
            this.sendPackets(PlayerShopPacket.WEDDING_INTER(106));
         } else {
            this.closeMarriageBox(true, 25);
         }
      } else {
         this.closeMarriageBox(true, 3);
      }
   }

   public void sendPackets(byte[] packets) {
      this.getPlayer1().getClient().getSession().writeAndFlush(packets);

      for(int i = 0; i < this.chrs.length; ++i) {
         MapleCharacter visitor = this.getVisitor(i);
         if (visitor != null) {
            visitor.getClient().getSession().writeAndFlush(packets);
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

   public void removeAllVisitors(int error) {
      for(int i = 0; i < this.chrs.length; ++i) {
         MapleCharacter visitor = this.getVisitor(i);
         if (visitor != null) {
            visitor.getClient().getSession().writeAndFlush(PlayerShopPacket.shopErrorMessage(error, i + 1));
            this.broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(this.getVisitorSlot(visitor)), this.getVisitorSlot(visitor));
            visitor.setPlayerShop((IMaplePlayerShop)null);
            this.chrs[i] = new WeakReference((Object)null);
         }
      }

      this.update();
   }

   public void closeMarriageBox(boolean z, int error) {
      this.removeAllVisitors(error);
      if (z) {
         this.getMCOwner().getClient().getSession().writeAndFlush(PlayerShopPacket.shopErrorMessage(error, 0));
      }

      if (this.getMCOwner() != null) {
         this.getMCOwner().setPlayerShop((IMaplePlayerShop)null);
      }

      if (this.getPlayer1() != null) {
         this.getPlayer1().setMarriage((MarriageMiniBox)null);
      }

      if (this.getPlayer2() != null) {
         this.getPlayer2().setMarriage((MarriageMiniBox)null);
      }

      this.update();
      this.getMap().removeMapObject(this);
   }

   public void send(MapleClient c) {
      if (this.getMCOwner() == null) {
         this.closeMarriageBox(false, 3);
      } else {
         c.getSession().writeAndFlush(MarriageEXPacket.MarriageRoom(c, this));
         this.update();
      }
   }

   public final int getStoreId() {
      return this.storeid;
   }

   public MapleMapObjectType getType() {
      return MapleMapObjectType.HIRED_MERCHANT;
   }

   public void sendDestroyData(MapleClient client) {
      if (this.isAvailable()) {
         client.getSession().writeAndFlush(PlayerShopPacket.destroyHiredMerchant(this.getOwnerId()));
      }

   }

   public final void sendVisitor(MapleClient c) {
      c.getSession().writeAndFlush(PlayerShopPacket.MerchantVisitorView(this.visitors));
   }

   public void closeShop(boolean saveItems, boolean remove) {
      this.closeMarriageBox(false, 3);
   }
}
