package client.inventory;

import constants.GameConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapleInventory implements Iterable<Item>, Serializable {
   private Map<Short, Item> inventory = new LinkedHashMap();
   private short slotLimit = 0;
   private MapleInventoryType type;

   public MapleInventory(MapleInventoryType type) {
      this.type = type;
   }

   public void addSlot(short slot) {
      this.slotLimit += slot;
      if (this.slotLimit > 128) {
         this.slotLimit = 128;
      }

   }

   public short getSlotLimit() {
      return this.slotLimit;
   }

   public void setSlotLimit(short slot) {
      if (slot > 128) {
         slot = 128;
      }

      this.slotLimit = slot;
   }

   public Item findById(int itemId) {
      Iterator var2 = this.inventory.values().iterator();

      Item item;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         item = (Item)var2.next();
      } while(item.getItemId() != itemId);

      return item;
   }

   public Item findByUniqueId(long uniqueId) {
      Iterator var3 = this.inventory.values().iterator();

      Item item;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         item = (Item)var3.next();
      } while(item.getUniqueId() != uniqueId);

      return item;
   }

   public Item findByInventoryId(long itemId, int itemI) {
      Iterator var4 = this.inventory.values().iterator();

      Item item;
      do {
         if (!var4.hasNext()) {
            return this.findById(itemI);
         }

         item = (Item)var4.next();
      } while(item.getInventoryId() != itemId || item.getItemId() != itemI);

      return item;
   }

   public Item findByInventoryIdOnly(long itemId, int itemI) {
      Iterator var4 = this.inventory.values().iterator();

      Item item;
      do {
         if (!var4.hasNext()) {
            return null;
         }

         item = (Item)var4.next();
      } while(item.getInventoryId() != itemId || item.getItemId() != itemI);

      return item;
   }

   public int countById(int itemId) {
      int possesed = 0;
      Iterator var3 = this.inventory.values().iterator();

      while(var3.hasNext()) {
         Item item = (Item)var3.next();
         if (item.getItemId() == itemId) {
            possesed += item.getQuantity();
         }
      }

      return possesed;
   }

   public List<Item> listById(int itemId) {
      List<Item> ret = new ArrayList();
      Iterator var3 = this.inventory.values().iterator();

      while(var3.hasNext()) {
         Item item = (Item)var3.next();
         if (item.getItemId() == itemId) {
            ret.add(item);
         }
      }

      if (ret.size() > 1) {
         Collections.sort(ret);
      }

      return ret;
   }

   public Collection<Item> list() {
      return this.inventory.values();
   }

   public Map<Short, Item> lists() {
      return this.inventory;
   }

   public List<Item> newList() {
      return (List)(this.inventory.size() <= 0 ? Collections.emptyList() : new LinkedList(this.inventory.values()));
   }

   public List<Integer> listIds() {
      List<Integer> ret = new ArrayList();
      Iterator var2 = this.inventory.values().iterator();

      while(var2.hasNext()) {
         Item item = (Item)var2.next();
         if (!ret.contains(item.getItemId())) {
            ret.add(item.getItemId());
         }
      }

      if (ret.size() > 1) {
         Collections.sort(ret);
      }

      return ret;
   }

   public short addItem(Item item) {
      short slotId = this.getNextFreeSlot();
      if (slotId < 0) {
         return -1;
      } else {
         this.inventory.put(slotId, item);
         item.setPosition(slotId);
         return slotId;
      }
   }

   public void addFromDB(Item item) {
      if (item.getPosition() >= 0 || this.type.equals(MapleInventoryType.EQUIPPED)) {
         if (item.getPosition() <= 0 || !this.type.equals(MapleInventoryType.EQUIPPED)) {
            this.inventory.put(item.getPosition(), item);
         }
      }
   }

   public void move(short sSlot, short dSlot, short slotMax) {
      Item source = (Item)this.inventory.get(sSlot);
      Item target = (Item)this.inventory.get(dSlot);
      if (source == null) {
         throw new InventoryException("Trying to move empty slot");
      } else {
         if (target == null) {
            if (dSlot < 0 && !this.type.equals(MapleInventoryType.EQUIPPED)) {
               return;
            }

            if (dSlot > 0 && this.type.equals(MapleInventoryType.EQUIPPED)) {
               return;
            }

            source.setPosition(dSlot);
            this.inventory.put(dSlot, source);
            this.inventory.remove(sSlot);
         } else if (target.getItemId() == source.getItemId() && !GameConstants.isThrowingStar(source.getItemId()) && !GameConstants.isBullet(source.getItemId()) && target.getOwner().equals(source.getOwner()) && target.getExpiration() == source.getExpiration()) {
            if (this.type.getType() != MapleInventoryType.EQUIP.getType() && this.type.getType() != MapleInventoryType.CASH.getType()) {
               if (source.getQuantity() + target.getQuantity() > slotMax) {
                  source.setQuantity((short)(source.getQuantity() + target.getQuantity() - slotMax));
                  target.setQuantity(slotMax);
               } else {
                  target.setQuantity((short)(source.getQuantity() + target.getQuantity()));
                  this.inventory.remove(sSlot);
               }
            } else {
               this.swap(target, source);
            }
         } else {
            this.swap(target, source);
         }

      }
   }

   private void swap(Item source, Item target) {
      this.inventory.remove(source.getPosition());
      this.inventory.remove(target.getPosition());
      short swapPos = source.getPosition();
      source.setPosition(target.getPosition());
      target.setPosition(swapPos);
      this.inventory.put(source.getPosition(), source);
      this.inventory.put(target.getPosition(), target);
   }

   public Item getItem(short slot) {
      return (Item)this.inventory.get(slot);
   }

   public void removeItem(short slot) {
      this.removeItem(slot, (short)1, false);
   }

   public void removeItem(short slot, short quantity, boolean allowZero) {
      Item item = (Item)this.inventory.get(slot);
      if (item != null) {
         item.setQuantity((short)(item.getQuantity() - quantity));
         if (item.getQuantity() < 0) {
            item.setQuantity((short)0);
         }

         if (item.getQuantity() == 0 && !allowZero) {
            this.removeSlot(slot);
         }

      }
   }

   public void removeSlot(short slot) {
      this.inventory.remove(slot);
   }

   public boolean isFull() {
      return this.inventory.size() >= this.slotLimit;
   }

   public boolean isFull(int margin) {
      return this.inventory.size() + margin >= this.slotLimit;
   }

   public short getNextFreeSlot() {
      if (this.isFull()) {
         return -1;
      } else {
         for(short i = 1; i <= this.slotLimit; ++i) {
            if (!this.inventory.containsKey(i)) {
               return i;
            }
         }

         return -1;
      }
   }

   public short getNumFreeSlot() {
      if (this.isFull()) {
         return 0;
      } else {
         short free = 0;

         for(short i = 1; i <= this.slotLimit; ++i) {
            if (!this.inventory.containsKey(i)) {
               ++free;
            }
         }

         return free;
      }
   }

   public short getNextItemSlot(short k) {
      if (this.isFull()) {
         return -1;
      } else {
         for(short i = k; i <= this.slotLimit; ++i) {
            if (!this.inventory.containsKey(i)) {
               return i;
            }
         }

         return -1;
      }
   }

   public MapleInventoryType getType() {
      return this.type;
   }

   public Iterator<Item> iterator() {
      return Collections.unmodifiableCollection(this.inventory.values()).iterator();
   }
}
