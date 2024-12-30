package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import server.MapleInventoryManipulator;
import tools.packet.PlayerShopPacket;

public class MaplePlayerShop extends AbstractPlayerStore {
   private int boughtnumber = 0;
   private List<String> bannedList = new ArrayList();

   public MaplePlayerShop(MapleCharacter owner, int itemId, String desc) {
      super(owner, itemId, desc, "", 3);
   }

   public void buy(MapleClient c, int item, short quantity) {
      MaplePlayerShopItem pItem = (MaplePlayerShopItem)this.items.get(item);
      if (pItem.bundles > 0) {
         Item newItem = pItem.item.copy();
         newItem.setQuantity((short)(quantity * newItem.getQuantity()));
         int flag = newItem.getFlag();
         if (ItemFlag.KARMA_EQUIP.check(flag)) {
            newItem.setFlag(flag - ItemFlag.KARMA_EQUIP.getValue());
         } else if (ItemFlag.KARMA_USE.check(flag)) {
            newItem.setFlag(flag - ItemFlag.KARMA_USE.getValue());
         }

         long gainmeso = pItem.price * (long)quantity;
         if (c.getPlayer().getMeso() >= gainmeso) {
            if (this.getMCOwner().getMeso() + gainmeso > 0L && MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner()) && MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
               pItem.bundles -= quantity;
               this.bought.add(new AbstractPlayerStore.BoughtItem(newItem.getItemId(), quantity, gainmeso, c.getPlayer().getName()));
               c.getPlayer().gainMeso(-gainmeso, false);
               this.getMCOwner().gainMeso(gainmeso, false);
               if (pItem.bundles <= 0) {
                  ++this.boughtnumber;
                  if (this.boughtnumber == this.items.size()) {
                     this.closeShop(false, true);
                     return;
                  }
               }
            } else {
               c.getPlayer().dropMessage(1, "Your inventory is full.");
            }
         } else {
            c.getPlayer().dropMessage(1, "You do not have enough mesos.");
         }

         this.getMCOwner().getClient().getSession().writeAndFlush(PlayerShopPacket.shopItemUpdate(this));
      }

   }

   public byte getShopType() {
      return 2;
   }

   public void closeShop(boolean saveItems, boolean remove) {
      MapleCharacter owner = this.getMCOwner();
      this.removeAllVisitors(10, 1);
      this.getMap().removeMapObject(this);
      Iterator var4 = this.getItems().iterator();

      while(var4.hasNext()) {
         MaplePlayerShopItem items = (MaplePlayerShopItem)var4.next();
         if (items.bundles > 0) {
            Item newItem = items.item.copy();
            newItem.setQuantity((short)(items.bundles * newItem.getQuantity()));
            if (!MapleInventoryManipulator.addFromDrop(owner.getClient(), newItem, false)) {
               this.saveItems();
               break;
            }

            items.bundles = 0;
         }
      }

      owner.setPlayerShop((IMaplePlayerShop)null);
      this.update();
      this.getMCOwner().getClient().getSession().writeAndFlush(PlayerShopPacket.shopErrorMessage(10, 0));
   }

   public void banPlayer(String name) {
      if (!this.bannedList.contains(name)) {
         this.bannedList.add(name);
      }

      for(int i = 0; i < 3; ++i) {
         MapleCharacter chr = this.getVisitor(i);
         if (chr.getName().equals(name)) {
            chr.getClient().getSession().writeAndFlush(PlayerShopPacket.shopErrorMessage(5, 1));
            chr.setPlayerShop((IMaplePlayerShop)null);
            this.removeVisitor(chr);
         }
      }

   }

   public boolean isBanned(String name) {
      return this.bannedList.contains(name);
   }
}
