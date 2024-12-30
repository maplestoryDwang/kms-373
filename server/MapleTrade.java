package server;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import client.messages.CommandProcessor;
import constants.GameConstants;
import constants.ServerConstants;
import handling.world.World;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import log.DBLogger;
import log.LogType;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.PlayerShopPacket;

public class MapleTrade {
   private MapleTrade partner = null;
   private final List<Item> items = new LinkedList();
   private List<Item> exchangeItems;
   private long meso = 0L;
   private long exchangeMeso = 0L;
   private boolean locked = false;
   private boolean inTrade = false;
   private final WeakReference<MapleCharacter> chr;
   private final byte tradingslot;
   private byte rps = 0;

   public MapleTrade(byte tradingslot, MapleCharacter chr) {
      this.tradingslot = tradingslot;
      this.chr = new WeakReference(chr);
   }

   public final void CompleteTrade() {
      String var10000;
      String var10001;
      if (this.exchangeItems != null) {
         List<Item> itemz = new LinkedList(this.exchangeItems);
         Iterator var2 = itemz.iterator();

         while(var2.hasNext()) {
            Item item = (Item)var2.next();
            var10001 = StringUtil.getAllCurrentTime();
            item.setGMLog(var10001 + "에 " + this.partner.getChr().getName() + "과의 교환으로 얻은 아이템.");
            if (ItemFlag.KARMA_EQUIP.check(item.getFlag())) {
               item.setFlag(item.getFlag() - ItemFlag.KARMA_EQUIP.getValue());
            }

            if (ItemFlag.KARMA_USE.check(item.getFlag())) {
               item.setFlag(item.getFlag() - ItemFlag.KARMA_USE.getValue());
            }

            MapleInventoryManipulator.addbyItem(((MapleCharacter)this.chr.get()).getClient(), item, false, false);
            var10000 = FileoutputUtil.교환로그;
            var10001 = ((MapleCharacter)this.chr.get()).getName();
            FileoutputUtil.log(var10000, "[교환 완료] " + var10001 + "이 " + this.partner.getChr().getName() + "의 교환 창에서 " + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "을 " + item.getQuantity() + "개 획득.");
         }

         this.exchangeItems.clear();
      }

      if (this.exchangeMeso > 0L) {
         ((MapleCharacter)this.chr.get()).gainMeso(this.exchangeMeso - GameConstants.getTaxAmount(this.exchangeMeso), false, false);
         var10000 = FileoutputUtil.교환메소로그;
         var10001 = ((MapleCharacter)this.chr.get()).getName();
         FileoutputUtil.log(var10000, "[교환 완료] " + var10001 + "이 " + this.partner.getChr().getName() + "의 교환 창에서 " + (this.exchangeMeso - GameConstants.getTaxAmount(this.exchangeMeso)) + "를 획득.");
      }

      this.exchangeMeso = 0L;
      ((MapleCharacter)this.chr.get()).getClient().getSession().writeAndFlush(CField.InteractionPacket.TradeMessage(this.tradingslot, (byte)7));
   }

   public final void cancel(MapleClient c, MapleCharacter chr) {
      if (this.items != null) {
         List<Item> itemz = new LinkedList(this.items);
         Iterator var4 = itemz.iterator();

         while(var4.hasNext()) {
            Item item = (Item)var4.next();
            MapleInventoryManipulator.addbyItem(c, item, true, false);
         }

         this.items.clear();
      }

      if (this.meso > 0L) {
         chr.gainMeso(this.meso, false, false);
      }

      this.meso = 0L;
      c.getSession().writeAndFlush(CField.InteractionPacket.getTradeCancel(this.tradingslot));
   }

   public final boolean isLocked() {
      return this.locked;
   }

   public final void setMeso(long meso) {
      if (!this.locked && this.partner != null && meso > 0L && this.meso + meso > 0L) {
         if (((MapleCharacter)this.chr.get()).getMeso() >= meso) {
            ((MapleCharacter)this.chr.get()).gainMeso(-meso, false, false);
            this.meso += meso;
            ((MapleCharacter)this.chr.get()).getClient().getSession().writeAndFlush(CField.InteractionPacket.getTradeMesoSet((byte)0, this.meso));
            if (this.partner != null) {
               this.partner.getChr().getClient().getSession().writeAndFlush(CField.InteractionPacket.getTradeMesoSet((byte)1, this.meso));
            }

            String var10000 = FileoutputUtil.교환메소로그;
            String var10001 = ((MapleCharacter)this.chr.get()).getName();
            FileoutputUtil.log(var10000, "[교환 메소 올림] " + var10001 + "이 " + this.partner.getChr().getName() + "의 교환 창에서 " + this.meso + "를 올림.");
         }

      }
   }

   public final void addItem(Item item) {
      if (!this.locked && this.partner != null) {
         this.items.add(item);
         ((MapleCharacter)this.chr.get()).getClient().getSession().writeAndFlush(CField.InteractionPacket.getTradeItemAdd((byte)0, item));
         if (this.partner != null) {
            this.partner.getChr().getClient().getSession().writeAndFlush(CField.InteractionPacket.getTradeItemAdd((byte)1, item));
         }

         String var10000 = FileoutputUtil.교환로그;
         String var10001 = ((MapleCharacter)this.chr.get()).getName();
         FileoutputUtil.log(var10000, "[교환 아이템 올림] " + var10001 + "이 " + this.partner.getChr().getName() + "의 교환 창에서 " + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "을 " + item.getQuantity() + "개 올림.");
      }
   }

   public final void chat(String message) {
      if (!CommandProcessor.processCommand(((MapleCharacter)this.chr.get()).getClient(), message, ServerConstants.CommandType.TRADE)) {
         ((MapleCharacter)this.chr.get()).getClient().getSession().writeAndFlush(PlayerShopPacket.shopChat((MapleCharacter)this.chr.get(), ((MapleCharacter)this.chr.get()).getName(), ((MapleCharacter)this.chr.get()).getId(), message, ((MapleCharacter)this.chr.get()).getTrade().tradingslot));
         if (this.partner != null) {
            this.partner.getChr().getClient().getSession().writeAndFlush(PlayerShopPacket.shopChat((MapleCharacter)this.chr.get(), ((MapleCharacter)this.chr.get()).getName(), ((MapleCharacter)this.chr.get()).getId(), message, ((MapleCharacter)this.chr.get()).getTrade().tradingslot));
         }
      }

      DBLogger.getInstance().logChat(LogType.Chat.Trade, ((MapleCharacter)this.chr.get()).getId(), ((MapleCharacter)this.chr.get()).getName(), message, "수신 : " + this.partner.getChr().getName());
      String var10001;
      String var10002;
      if (((MapleCharacter)this.chr.get()).getClient().isMonitored()) {
         var10001 = ((MapleCharacter)this.chr.get()).getName();
         var10002 = ((MapleCharacter)this.chr.get()).getName();
         World.Broadcast.broadcastGMMessage(CWvsContext.serverNotice(6, var10001, var10002 + " said in trade with " + this.partner.getChr().getName() + ": " + message));
      } else if (this.partner != null && this.partner.getChr() != null && this.partner.getChr().getClient().isMonitored()) {
         var10001 = ((MapleCharacter)this.chr.get()).getName();
         var10002 = ((MapleCharacter)this.chr.get()).getName();
         World.Broadcast.broadcastGMMessage(CWvsContext.serverNotice(6, var10001, var10002 + " said in trade with " + this.partner.getChr().getName() + ": " + message));
      }

   }

   public final void chatAuto(String message) {
      ((MapleCharacter)this.chr.get()).dropMessage(-2, message);
      if (this.partner != null) {
         this.partner.getChr().getClient().getSession().writeAndFlush(PlayerShopPacket.shopChat(this.partner.getChr(), this.partner.getChr().getName(), this.partner.getChr().getId(), message, 1));
      }

      String var10001;
      String var10002;
      if (((MapleCharacter)this.chr.get()).getClient().isMonitored()) {
         var10001 = ((MapleCharacter)this.chr.get()).getName();
         var10002 = ((MapleCharacter)this.chr.get()).getName();
         World.Broadcast.broadcastGMMessage(CWvsContext.serverNotice(6, var10001, var10002 + " said in trade [Automated] with " + this.partner.getChr().getName() + ": " + message));
      } else if (this.partner != null && this.partner.getChr() != null && this.partner.getChr().getClient().isMonitored()) {
         var10001 = ((MapleCharacter)this.chr.get()).getName();
         var10002 = ((MapleCharacter)this.chr.get()).getName();
         World.Broadcast.broadcastGMMessage(CWvsContext.serverNotice(6, var10001, var10002 + " said in trade [Automated] with " + this.partner.getChr().getName() + ": " + message));
      }

   }

   public final MapleTrade getPartner() {
      return this.partner;
   }

   public final void setPartner(MapleTrade partner) {
      if (!this.locked) {
         this.partner = partner;
      }
   }

   public final MapleCharacter getChr() {
      return (MapleCharacter)this.chr.get();
   }

   public final int getNextTargetSlot() {
      if (this.items.size() >= 9) {
         return -1;
      } else {
         int ret = 1;
         Iterator var2 = this.items.iterator();

         while(var2.hasNext()) {
            Item item = (Item)var2.next();
            if (item.getPosition() == ret) {
               ++ret;
            }
         }

         return ret;
      }
   }

   public boolean inTrade() {
      return this.inTrade;
   }

   public final boolean setItems(MapleClient c, Item item, byte targetSlot, int quantity) {
      int target = this.getNextTargetSlot();
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (this.partner != null && target != -1 && !this.isLocked() && (GameConstants.getInventoryType(item.getItemId()) != MapleInventoryType.EQUIP && GameConstants.getInventoryType(item.getItemId()) != MapleInventoryType.CODY || quantity == 1)) {
         int flag = item.getFlag();
         if (ItemFlag.LOCK.check(flag)) {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            return false;
         } else if ((ItemFlag.UNTRADEABLE.check(flag) || GameConstants.isPet(item.getItemId()) || ii.isDropRestricted(item.getItemId()) || ii.isAccountShared(item.getItemId())) && !ItemFlag.KARMA_EQUIP.check(flag) && !ItemFlag.KARMA_USE.check(flag)) {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            return false;
         } else {
            if (item.getType() == 1) {
               Equip equip = (Equip)item;
               if ((equip.getEnchantBuff() & 136) != 0) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  c.getPlayer().dropMessage(1, "장비의 흔적은 교환하실 수 없습니다.");
                  return false;
               }
            }

            Item tradeItem = item.copy();
            if (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId())) {
               tradeItem.setQuantity((short)quantity);
               MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(item.getItemId()), item.getPosition(), (short)quantity, true);
            } else {
               tradeItem.setQuantity(item.getQuantity());
               MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(item.getItemId()), item.getPosition(), item.getQuantity(), true);
            }

            if (targetSlot < 0) {
               targetSlot = (byte)target;
            } else {
               Iterator var9 = this.items.iterator();

               while(var9.hasNext()) {
                  Item itemz = (Item)var9.next();
                  if (itemz.getPosition() == targetSlot) {
                     targetSlot = (byte)target;
                     break;
                  }
               }
            }

            tradeItem.setPosition((short)targetSlot);
            this.addItem(tradeItem);
            return true;
         }
      } else {
         return false;
      }
   }

   private final int check() {
      if (((MapleCharacter)this.chr.get()).getMeso() + this.exchangeMeso < 0L) {
         return 1;
      } else {
         if (this.exchangeItems != null) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            byte eq = 0;
            byte use = 0;
            byte setup = 0;
            byte etc = 0;
            byte cash = 0;
            Iterator var7 = this.exchangeItems.iterator();

            while(true) {
               if (!var7.hasNext()) {
                  if (((MapleCharacter)this.chr.get()).getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq || ((MapleCharacter)this.chr.get()).getInventory(MapleInventoryType.USE).getNumFreeSlot() < use || ((MapleCharacter)this.chr.get()).getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup || ((MapleCharacter)this.chr.get()).getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc || ((MapleCharacter)this.chr.get()).getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash) {
                     return 1;
                  }
                  break;
               }

               Item item = (Item)var7.next();
               switch(GameConstants.getInventoryType(item.getItemId())) {
               case EQUIP:
                  ++eq;
                  break;
               case USE:
                  ++use;
                  break;
               case SETUP:
                  ++setup;
                  break;
               case ETC:
                  ++etc;
                  break;
               case CASH:
                  ++cash;
               }

               if (ii.isPickupRestricted(item.getItemId()) && ((MapleCharacter)this.chr.get()).haveItem(item.getItemId(), 1, true, true)) {
                  return 2;
               }
            }
         }

         return 0;
      }
   }

   public static final void completeTrade(MapleCharacter c) {
      MapleTrade local = c.getTrade();
      MapleTrade partner = local.getPartner();
      if (partner != null && !local.locked) {
         local.locked = true;
         partner.getChr().getClient().getSession().writeAndFlush(CField.InteractionPacket.getTradeConfirmation());
         partner.exchangeItems = new LinkedList(local.items);
         partner.exchangeMeso = local.meso;
         if (partner.isLocked()) {
            int lz = local.check();
            int lz2 = partner.check();
            if (lz == 0 && lz2 == 0) {
               local.CompleteTrade();
               partner.CompleteTrade();
            } else {
               partner.cancel(partner.getChr().getClient(), partner.getChr());
               local.cancel(c.getClient(), c);
            }

            partner.getChr().setTrade((MapleTrade)null);
            c.setTrade((MapleTrade)null);
         }

      }
   }

   public static final void cancelTrade(MapleTrade Localtrade, MapleClient c, MapleCharacter chr) {
      Localtrade.cancel(c, chr);
      MapleTrade partner = Localtrade.getPartner();
      if (partner != null && partner.getChr() != null) {
         partner.cancel(partner.getChr().getClient(), partner.getChr());
         partner.getChr().setTrade((MapleTrade)null);
      }

      chr.setTrade((MapleTrade)null);
   }

   public static final void startTrade(MapleCharacter c, boolean isTrade) {
      if (c.getTrade() == null) {
         c.setTrade(new MapleTrade((byte)0, c));
         c.getClient().getSession().writeAndFlush(CField.InteractionPacket.getTradeStart(c.getClient(), c.getTrade(), (byte)0, isTrade));
         c.isTrade = isTrade;
      } else {
         c.getClient().getSession().writeAndFlush(CWvsContext.serverNotice(5, "", "다른 유저와 교환중인 유저입니다."));
      }

   }

   public static final void startCashTrade(MapleCharacter c) {
      if (c.getTrade() == null) {
         c.setTrade(new MapleTrade((byte)0, c));
         c.getClient().getSession().writeAndFlush(CField.InteractionPacket.getCashTradeStart(c.getClient(), c.getTrade(), (byte)0));
      } else {
         c.getClient().getSession().writeAndFlush(CWvsContext.serverNotice(5, "", "다른 유저와 교환중인 유저입니다."));
      }

   }

   public static final void inviteTrade(MapleCharacter c1, MapleCharacter c2, boolean isTrade) {
      if (c1 != null && c1.getTrade() != null) {
         if (c2 != null && c2.getTrade() == null) {
            c2.setTrade(new MapleTrade((byte)1, c2));
            c2.getTrade().setPartner(c1.getTrade());
            c1.getTrade().setPartner(c2.getTrade());
            c2.getClient().getSession().writeAndFlush(CField.InteractionPacket.getTradeInvite(c1, isTrade));
         } else {
            c1.getClient().getSession().writeAndFlush(CWvsContext.serverNotice(5, "", "다른 유저와 교환중인 유저입니다."));
            cancelTrade(c1.getTrade(), c1.getClient(), c1);
         }

      }
   }

   public static final void inviteCashTrade(MapleCharacter c1, MapleCharacter c2) {
      if (c1 != null && c1.getTrade() != null) {
         if (c2 != null && c2.getTrade() == null) {
            c2.setTrade(new MapleTrade((byte)1, c2));
            c2.getTrade().setPartner(c1.getTrade());
            c1.getTrade().setPartner(c2.getTrade());
            c2.getClient().getSession().writeAndFlush(CField.InteractionPacket.getCashTradeInvite(c1));
         } else {
            c1.getClient().getSession().writeAndFlush(CWvsContext.serverNotice(5, "", "다른 유저와 교환중인 유저입니다."));
            cancelTrade(c1.getTrade(), c1.getClient(), c1);
         }

      }
   }

   public static final void visitTrade(MapleCharacter c1, MapleCharacter c2, boolean isTrade) {
      if (c2 != null && c1.getTrade() != null && c1.getTrade().getPartner() == c2.getTrade() && c2.getTrade() != null && c2.getTrade().getPartner() == c1.getTrade()) {
         c1.getTrade().inTrade = true;
         c2.getClient().getSession().writeAndFlush(PlayerShopPacket.shopVisitorAdd(c1, 1));
         c1.getClient().getSession().writeAndFlush(CField.InteractionPacket.getTradeStart(c1.getClient(), c1.getTrade(), (byte)1, isTrade));
      } else {
         c1.getClient().getSession().writeAndFlush(CWvsContext.serverNotice(5, "", "다른 유저와 교환중인 유저입니다."));
      }

   }

   public static final void visitCashTrade(MapleCharacter c1, MapleCharacter c2) {
      if (c2 != null && c1.getTrade() != null && c1.getTrade().getPartner() == c2.getTrade() && c2.getTrade() != null && c2.getTrade().getPartner() == c1.getTrade()) {
         c1.getTrade().inTrade = true;
         c2.getClient().getSession().writeAndFlush(PlayerShopPacket.shopVisitorAdd(c1, 1));
         c1.getClient().getSession().writeAndFlush(CField.InteractionPacket.getCashTradeStart(c1.getClient(), c1.getTrade(), (byte)1));
      } else {
         c1.getClient().getSession().writeAndFlush(CWvsContext.serverNotice(5, "", "다른 유저와 교환중인 유저입니다."));
      }

   }

   public static final void declineTrade(MapleCharacter c) {
      MapleTrade trade = c.getTrade();
      if (trade != null) {
         if (trade.getPartner() != null) {
            MapleCharacter other = trade.getPartner().getChr();
            if (other != null && other.getTrade() != null) {
               other.getTrade().cancel(other.getClient(), other);
               other.setTrade((MapleTrade)null);
               other.dropMessage(5, c.getName() + "님이 교환을 취소했습니다.");
            }
         }

         trade.cancel(c.getClient(), c);
         c.setTrade((MapleTrade)null);
      }

   }

   public byte getPRS() {
      return this.rps;
   }

   public void setRPS(byte rps) {
      this.rps = rps;
   }
}
