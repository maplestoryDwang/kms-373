package handling.cashshop.handler;

import client.AvatarLook;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleQuestStatus;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.CharacterTransfer;
import handling.world.PlayerBuffStorage;
import handling.world.World;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import server.CashItemFactory;
import server.CashItemInfo;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.CurrentTime;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CSPacket;
import tools.packet.CWvsContext;

public class CashShopOperation {
   public static final int R = 3;

   public static void LeaveCS(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      CashShopServer.getPlayerStorage().deregisterPlayer(chr);
      c.updateLoginState(1, c.getSessionIPAddress());
      boolean var7 = false;

      String var10000;
      int var10001;
      try {
         var7 = true;
         PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
         PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
         World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), c.getChannel());
         c.getSession().writeAndFlush(CField.getChannelChange(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1])));
         var7 = false;
      } finally {
         if (var7) {
            var10000 = FileoutputUtil.캐시샵퇴장로그;
            var10001 = chr.getClient().getAccID();
            FileoutputUtil.log(var10000, "[퇴장] 계정 번호 : " + var10001 + " | 캐릭터 : " + chr.getName() + "(" + chr.getId() + ")  캐시샵 퇴장");
            String s = c.getSessionIPAddress();
            LoginServer.addIPAuth(s.substring(s.indexOf(47) + 1, s.length()));
            chr.saveToDB(true, true);
            c.setPlayer((MapleCharacter)null);
            c.setReceiving(false);
            c.setCashShop(false);
         }
      }

      var10000 = FileoutputUtil.캐시샵퇴장로그;
      var10001 = chr.getClient().getAccID();
      FileoutputUtil.log(var10000, "[퇴장] 계정 번호 : " + var10001 + " | 캐릭터 : " + chr.getName() + "(" + chr.getId() + ")  캐시샵 퇴장");
      String s = c.getSessionIPAddress();
      LoginServer.addIPAuth(s.substring(s.indexOf(47) + 1, s.length()));
      chr.saveToDB(true, true);
      c.setPlayer((MapleCharacter)null);
      c.setReceiving(false);
      c.setCashShop(false);
   }

   public static void EnterCS(int playerid, MapleClient c) {
      CharacterTransfer transfer = CashShopServer.getPlayerStorage().getPendingCharacter(playerid);
      MapleCharacter chr = MapleCharacter.ReconstructChr(transfer, c, false);
      c.setPlayer(chr);
      c.setAccID(chr.getAccountID());
      if (!c.CheckIPAddress()) {
         c.getSession().close();
      } else {
         c.loadKeyValues();
         c.loadCustomDatas();
         updateCharge(c);
         c.setCashShop(true);
         chr.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(chr.getId()));
         World.isCharacterListConnected(c.getPlayer().getName(), c.loadCharacterNames(c.getWorld()));
         c.updateLoginState(2, c.getSessionIPAddress());
         CashShopServer.getPlayerStorage().registerPlayer(chr);
         c.getSession().writeAndFlush(CSPacket.warpCS(c));
         String var10000 = FileoutputUtil.캐시샵입장로그;
         int var10001 = chr.getClient().getAccID();
         FileoutputUtil.log(var10000, "[입장] 계정 번호 : " + var10001 + " | 캐릭터 : " + chr.getName() + "(" + chr.getId() + ")  캐시샵 입장");

         int i;
         int value;
         for(i = 120000000; i < 120000100; ++i) {
            if (c.getKeyValue(i.makeConcatWithConstants<invokedynamic>(i)) != null) {
               value = Integer.parseInt(c.getKeyValue(i.makeConcatWithConstants<invokedynamic>(i)));
               c.getSession().writeAndFlush(CSPacket.showCount(i, value));
            } else {
               c.getSession().writeAndFlush(CSPacket.showCount(i, 0));
            }
         }

         for(i = 120100000; i < 120100100; ++i) {
            if (c.getKeyValue(i.makeConcatWithConstants<invokedynamic>(i)) != null) {
               value = Integer.parseInt(c.getKeyValue(i.makeConcatWithConstants<invokedynamic>(i)));
               c.getSession().writeAndFlush(CSPacket.showCount(i, value));
            } else {
               c.getSession().writeAndFlush(CSPacket.showCount(i, 0));
            }
         }

         c.getSession().writeAndFlush(CSPacket.getCSInventory(c));
         c.getSession().writeAndFlush(CSPacket.coodinationResult(0, 0, chr));
         doCSPackets(c);
      }
   }

   public static void updateCharge(MapleClient c) {
      String lastEnter = c.getCustomData(6, "enter");
      String var10000 = String.valueOf(CurrentTime.getYear());
      String date = var10000 + StringUtil.getLeftPaddedStr(String.valueOf(CurrentTime.getMonth()), '0', 2);
      if (lastEnter == null || !lastEnter.equals(date)) {
         c.setCustomData(6, "enter", date);
         if (lastEnter != null) {
            int grade = c.getMVPGrade();
            c.setCustomData(6, "sp_" + grade, date);
         }
      }

   }

   public static void csCharge(MapleClient c) {
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      c.getSession().writeAndFlush(CSPacket.addCashPoint("."));
   }

   public static void CSUpdate(MapleClient c) {
      c.getSession().writeAndFlush(CSPacket.getCSGifts(c));

      int i;
      int value;
      for(i = 120000000; i < 120000100; ++i) {
         if (c.getKeyValue(i.makeConcatWithConstants<invokedynamic>(i)) != null) {
            value = Integer.parseInt(c.getKeyValue(i.makeConcatWithConstants<invokedynamic>(i)));
            c.getSession().writeAndFlush(CSPacket.showCount(i, value));
         }
      }

      for(i = 120100000; i < 120100100; ++i) {
         if (c.getKeyValue(i.makeConcatWithConstants<invokedynamic>(i)) != null) {
            value = Integer.parseInt(c.getKeyValue(i.makeConcatWithConstants<invokedynamic>(i)));
            c.getSession().writeAndFlush(CSPacket.showCount(i, value));
         }
      }

      doCSPackets(c);
      c.getSession().writeAndFlush(CSPacket.sendWishList(c.getPlayer(), false));
   }

   public static void updateCharge(MapleClient c, int value) {
      String var10000 = String.valueOf(CurrentTime.getYear());
      String date = var10000 + StringUtil.getLeftPaddedStr(String.valueOf(CurrentTime.getMonth()), '0', 2);
      int grade = c.getMVPGrade();
      String charge = c.getCustomData(4, date);
      int add = charge == null ? 0 : Integer.parseInt(charge);
      c.setCustomData(4, date, String.valueOf(add + value));
      if (grade != c.getMVPGrade()) {
         c.setCustomData(6, "sp_" + c.getMVPGrade(), date);
      }

   }

   public static void mvpSpecialPack(int grade, MapleClient c) {
      int mvpGrade = c.getMVPGrade();
      if (grade <= mvpGrade) {
         String var10000 = String.valueOf(CurrentTime.getYear());
         String date = var10000 + StringUtil.getLeftPaddedStr(String.valueOf(CurrentTime.getMonth()), '0', 2);
         c.setCustomData(6, "sp_" + grade, "R" + date);
      } else {
         c.getPlayer().dropMessage(1, "스페셜팩 수령이 불가능합니다.");
      }

   }

   public static void mvpGiftPack(MapleClient c) {
      String data = c.getCustomData(6, "gp");
      System.out.println("MVP 등급 : " + c.getMVPGrade());
      System.out.println("최근 3달 충전 내역 : " + c.chargePoint());
      System.out.println("총 충전 내역 : " + c.allChargePoint());
      String var10000 = String.valueOf(CurrentTime.getYear());
      String date = var10000 + StringUtil.getLeftPaddedStr(String.valueOf(CurrentTime.getMonth()), '0', 2) + StringUtil.getLeftPaddedStr(String.valueOf(CurrentTime.getDate()), '0', 2) + StringUtil.getLeftPaddedStr(String.valueOf(CurrentTime.getHour()), '0', 2);
      if (data != null && (data == null || data.contains(date))) {
         c.getPlayer().dropMessage(1, "기프트팩 수령이 불가능합니다.");
      } else {
         c.setCustomData(6, "gp", date);
      }

      System.out.println("String : " + date);
   }

   public static void coodinationResult(LittleEndianAccessor slea, MapleClient c) throws IOException {
      int type = slea.readInt();
      MapleCharacter chr = c.getPlayer();
      if (chr == null) {
         c.getSession().writeAndFlush(CSPacket.coodinationResult(type, 1, chr));
      } else {
         AvatarLook a = new AvatarLook();
         if (type == 1) {
            AvatarLook.decodeAvatarLook(a, slea);
            Iterator var5 = chr.getCoodination().iterator();

            while(var5.hasNext()) {
               AvatarLook aL = (AvatarLook)var5.next();
               if (aL != null && aL.compare(a)) {
                  c.getSession().writeAndFlush(CSPacket.coodinationResult(type, 3, chr));
                  return;
               }
            }

            if (chr.getCoodination().size() < 6) {
               chr.getCoodination().add(a);
               c.getSession().writeAndFlush(CSPacket.coodinationResult(type, 0, chr));
            } else {
               c.getSession().writeAndFlush(CSPacket.coodinationResult(type, 4, chr));
            }
         } else if (type == 2) {
            AvatarLook.decodeUnpackAvatarLook(a, slea);
            AvatarLook target = null;
            Iterator var9 = chr.getCoodination().iterator();

            while(var9.hasNext()) {
               AvatarLook aL = (AvatarLook)var9.next();
               if (aL != null && aL.compare(a)) {
                  target = aL;
                  break;
               }
            }

            if (target != null) {
               chr.getCoodination().remove(target);
               c.getSession().writeAndFlush(CSPacket.coodinationResult(type, 0, chr));
            } else {
               c.getSession().writeAndFlush(CSPacket.coodinationResult(type, 6, chr));
            }
         }

      }
   }

   public static void csGift(LittleEndianAccessor slea, MapleClient c) {
      c.getSession().writeAndFlush(CSPacket.resultGiftItem(true, "", 0));
   }

   public static void CouponCode(String code, MapleClient c) {
      if (code.length() > 0) {
         Triple info = null;

         try {
            info = MapleCharacterUtil.getNXCodeInfo(code);
         } catch (SQLException var11) {
            var11.printStackTrace();
         }

         if (info != null && (Boolean)info.left) {
            int type = (Integer)info.mid;
            int item = (Integer)info.right;

            try {
               MapleCharacterUtil.setNXCodeUsed(c.getPlayer().getName(), code);
            } catch (SQLException var10) {
               var10.printStackTrace();
            }

            Map<Integer, Item> itemz = new HashMap();
            int maplePoints = 0;
            int mesos = 0;
            switch(type) {
            case 1:
            case 2:
               c.getPlayer().modifyCSPoints(type, item, false);
               maplePoints = item;
               break;
            case 3:
               CashItemInfo itez = CashItemFactory.getInstance().getItem(item);
               if (itez == null) {
                  c.getSession().writeAndFlush(CSPacket.sendCSFail(0));
                  return;
               }

               byte slot = MapleInventoryManipulator.addId(c, itez.getId(), (short)1, "", "Cash shop: coupon code on " + FileoutputUtil.CurrentReadable_Date());
               if (slot <= -1) {
                  c.getSession().writeAndFlush(CSPacket.sendCSFail(0));
                  return;
               }

               itemz.put(item, c.getPlayer().getInventory(GameConstants.getInventoryType(item)).getItem((short)slot));
               break;
            case 4:
               c.getPlayer().gainMeso((long)item, false);
               mesos = item;
            }

            c.getSession().writeAndFlush(CSPacket.showCouponRedeemedItem(itemz, mesos, maplePoints, c));
         } else {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(info == null ? 167 : 165));
         }

         doCSPackets(c);
      }
   }

   public static final void BuyCashItem(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      int action = slea.readByte();
      CashItemInfo item;
      String var10000;
      int var10001;
      CashItemInfo cashItemInfo2;
      int quantity;
      int value;
      boolean j;
      switch(action) {
      case 0:
         slea.skip(2);
         CouponCode(slea.readMapleAsciiString(), c);
         break;
      case 1:
      case 2:
      case 4:
      case 10:
      case 11:
      case 12:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 32:
      case 33:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 40:
      case 41:
      case 42:
      case 43:
      case 44:
      case 45:
      case 46:
      case 47:
      default:
         System.out.println("New Action: " + action + " Remaining: " + slea.toString());
         c.getSession().writeAndFlush(CSPacket.sendCSFail(0));
         break;
      case 3:
         boolean useMaplePoint = slea.readByte() == 1;
         boolean useMileage = slea.readByte() == 1;
         boolean useAllMileage = slea.readByte() == 1;
         item = CashItemFactory.getInstance().getItem(slea.readInt());
         quantity = slea.readInt();
         byte toCharge;
         if (useMaplePoint) {
            toCharge = 2;
         } else if (useAllMileage) {
            toCharge = 4;
         } else {
            toCharge = 1;
         }

         if (item.getSN() >= 130400000 && item.getSN() <= 130599999 || item.getSN() >= 130000000 && item.getSN() <= 130000500 || item.getSN() >= 130002000 && item.getSN() <= 130002500) {
            toCharge = 2;
            if (!useMaplePoint) {
               c.send(CWvsContext.serverNotice(1, "", "홍보 아이템은 홍보 포인트로만 구입이 가능합니다."));
               doCSPackets(c);
               return;
            }
         } else if (item.getPrice() > 0) {
            if (useMaplePoint) {
               c.send(CWvsContext.serverNotice(1, "", "후원 아이템은 후원 포인트로만 구입이 가능합니다."));
               doCSPackets(c);
               return;
            }

            toCharge = 1;
         }

         int paybackRate;
         if (item.getLimitMax() > 0) {
            if (c.getKeyValue(item.getSN().makeConcatWithConstants<invokedynamic>(item.getSN())) != null && item.getLimitMax() <= Integer.parseInt(c.getKeyValue(item.getSN().makeConcatWithConstants<invokedynamic>(item.getSN())))) {
               return;
            }

            paybackRate = c.getKeyValue(item.getSN().makeConcatWithConstants<invokedynamic>(item.getSN())) != null ? Integer.parseInt(c.getKeyValue(item.getSN().makeConcatWithConstants<invokedynamic>(item.getSN()))) + 1 : 1;
            c.setKeyValue(item.getSN().makeConcatWithConstants<invokedynamic>(item.getSN()), paybackRate.makeConcatWithConstants<invokedynamic>(paybackRate));
            c.getSession().writeAndFlush(CSPacket.showCount(item.getSN(), paybackRate));
         }

         if (item == null) {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(0));
         } else {
            if (!item.genderEquals(c.getPlayer().getGender())) {
               c.getSession().writeAndFlush(CSPacket.sendCSFail(166));
               doCSPackets(c);
               return;
            }

            if (c.getPlayer().getCashInventory().getItemsSize() >= 500) {
               c.getSession().writeAndFlush(CSPacket.sendCSFail(177));
               doCSPackets(c);
               return;
            }

            if (Arrays.asList(GameConstants.cashBlock).contains(item.getId())) {
               c.getPlayer().dropMessage(1, GameConstants.getCashBlockedMsg(item.getId()));
               doCSPackets(c);
               return;
            }

            paybackRate = useAllMileage ? 0 : 30;
            int discountRate = useMileage ? 30 : 0;
            int price = item.getPrice();
            if (discountRate > 0) {
               price = price * (100 - discountRate) / 100;
            }

            chr.modifyCSPoints(toCharge, -price, false);
            if (useMileage) {
               chr.modifyCSPoints(4, -(price * discountRate / 100), false);
            }

            if (toCharge == 1) {
               updateCharge(c, price);
            }

            Item itemz = chr.getCashInventory().toItem(item);
            if (itemz != null && itemz.getUniqueId() > 0L && itemz.getItemId() == item.getId()) {
               if (toCharge == 1) {
                  itemz.setFlag(itemz.getFlag() | (itemz.getType() == 1 ? ItemFlag.KARMA_EQUIP.getValue() : ItemFlag.KARMA_USE.getValue()));
               }

               itemz.setPosition((short)(chr.getCashInventory().getItemsSize() + 1));
               chr.getCashInventory().addToInventory(itemz);
               String amount = "amount=900;";
               String given = "given=-1;";
               String per = "per=9";
               c.getSession().writeAndFlush(CSPacket.showBoughtCSItem(itemz, c, item.getSN(), paybackRate, discountRate));
               save(c);
               var10000 = FileoutputUtil.캐시샵구매로그;
               var10001 = chr.getClient().getAccID();
               FileoutputUtil.log(var10000, "[아이템 구입] 계정 번호 : " + var10001 + " | 캐릭터 : " + chr.getName() + "(" + chr.getId() + ")  | 캐시샵에서 " + MapleItemInformationProvider.getInstance().getName(itemz.getItemId()) + "(" + itemz.getItemId() + ")를 [" + itemz.getQuantity() + "]개 구입");
            } else {
               c.getSession().writeAndFlush(CSPacket.sendCSFail(0));
            }
         }
         break;
      case 5:
         chr.clearWishlist();
         slea.skip(1);
         if (slea.available() < 48L) {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(0));
            doCSPackets(c);
            return;
         }

         int[] wishlist = new int[12];

         for(int i = 0; i < 12; ++i) {
            wishlist[i] = slea.readInt();
         }

         chr.setWishlist(wishlist);
         c.getSession().writeAndFlush(CSPacket.sendWishList(chr, true));
         break;
      case 6:
         slea.skip(1);
         j = true;
         boolean coupon = slea.readByte() > 0;
         MapleInventory var10002;
         if (coupon) {
            MapleInventoryType mapleInventoryType = getInventoryType(slea.readInt());
            if (chr.getCSPoints(1) >= 12000 && chr.getInventory(mapleInventoryType).getSlotLimit() < 89) {
               chr.modifyCSPoints(1, -12000, false);
               chr.getInventory(mapleInventoryType).addSlot((short)8);
               var10002 = chr.getInventory(mapleInventoryType);
               chr.dropMessage(1, "인벤토리 공간을 늘렸습니다. 현재 " + var10002.getSlotLimit() + " 슬롯이 되었습니다.\r\n\r\n캐시샵에서 늘려진 슬롯이 바로 보이지 않아도 실제로는 늘려졌으니, 캐시샵에서 나가시면 정상적으로 슬롯이 늘어난걸 볼 수 있습니다.");
            } else {
               chr.dropMessage(1, "슬롯을 더 이상 늘릴 수 없습니다.");
            }
         } else {
            MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
            if (chr.getCSPoints(1) >= 8000 && chr.getInventory(type).getSlotLimit() < 93) {
               chr.modifyCSPoints(1, -8000, false);
               chr.getInventory(type).addSlot((short)4);
               var10002 = chr.getInventory(type);
               chr.dropMessage(1, "인벤토리 공간을 늘렸습니다. 현재 " + var10002.getSlotLimit() + " 슬롯이 되었습니다.\r\n\r\n캐시샵에서 늘려진 슬롯이 바로 보이지 않아도 실제로는 늘려졌으니, 캐시샵에서 나가시면 정상적으로 슬롯이 늘어난걸 볼 수 있습니다.");
            } else {
               chr.dropMessage(1, "슬롯을 더 이상 늘릴 수 없습니다.");
            }
         }
         break;
      case 7:
         if (chr.getCSPoints(1) >= 8000 && chr.getStorage().getSlots() < 48) {
            chr.modifyCSPoints(1, -8000, false);
            chr.getStorage().increaseSlots((byte)4);
            chr.dropMessage(1, "창고슬롯을 늘렸습니다. 현재 창고 슬롯은 " + chr.getStorage().getSlots() + "칸 입니다.");
         } else {
            chr.dropMessage(1, "슬롯을 더 이상 늘릴 수 없습니다.");
         }
         break;
      case 8:
         slea.skip(1);
         j = true;
         CashItemInfo cashItemInfo1 = CashItemFactory.getInstance().getItem(slea.readInt());
         int slots = c.getCharacterSlots();
         if (cashItemInfo1 != null && c.getPlayer().getCSPoints(1) >= cashItemInfo1.getPrice() && slots <= 15 && cashItemInfo1.getId() == 5430000) {
            if (c.gainCharacterSlot()) {
               c.getPlayer().modifyCSPoints(1, -cashItemInfo1.getPrice(), false);
               c.getSession().writeAndFlush(CSPacket.buyCharacterSlot());
            } else {
               chr.dropMessage(1, "슬롯을 더 이상 늘릴 수 없습니다.");
            }
            break;
         }

         doCSPackets(c);
         return;
      case 9:
         int j = slea.readByte() + 1;
         int sn = slea.readInt();
         cashItemInfo2 = CashItemFactory.getInstance().getItem(sn);
         if (cashItemInfo2 == null || c.getPlayer().getCSPoints(j) < cashItemInfo2.getPrice() || cashItemInfo2.getId() / 10000 != 555) {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(0));
            doCSPackets(c);
            return;
         }

         MapleQuestStatus marr = c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(122700));
         chr.dropMessage(1, "이미 펜던트 늘리기가 적용중입니다.");
         doCSPackets(c);
         break;
      case 13:
         Item item1 = chr.getCashInventory().findByCashId(slea.readLong(), slea.readInt(), slea.readByte());
         if (item1 != null && item1.getQuantity() > 0 && MapleInventoryManipulator.checkSpace(c, item1.getItemId(), item1.getQuantity(), item1.getOwner())) {
            Item item_ = item1.copy();
            short pos = MapleInventoryManipulator.addbyItem(c, item_, false, true);
            if (pos >= 0) {
               if (item_.getPet() != null) {
                  item_.getPet().setInventoryPosition(pos);
               }

               chr.getCashInventory().removeFromInventory(item1);
               c.getSession().writeAndFlush(CSPacket.confirmFromCSInventory(item_, c, pos));
               save(c);
               var10000 = FileoutputUtil.캐시샵인벤로그;
               var10001 = chr.getClient().getAccID();
               FileoutputUtil.log(var10000, "[캐시템 꺼내기] 계정 번호 : " + var10001 + " | 캐릭터 : " + chr.getName() + "(" + chr.getId() + ")  | 캐시샵에서 " + MapleItemInformationProvider.getInstance().getName(item_.getItemId()) + "(" + item_.getItemId() + ")를 [" + item_.getQuantity() + "] 인벤으로 옮김");
            } else {
               c.getSession().writeAndFlush(CSPacket.sendCSFail(177));
            }
         } else {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(177));
         }
         break;
      case 14:
         short slot = -1;
         long uniqueId = slea.readLong();
         int itemId = slea.readInt();
         byte b = slea.readByte();
         MapleInventory inv = chr.getInventory(b);
         Item item2 = inv.findByUniqueId(uniqueId);
         MapleInventory[] var47 = c.getPlayer().getInventorys();
         int var42 = var47.length;

         for(int var43 = 0; var43 < var42; ++var43) {
            MapleInventory iv = var47[var43];
            item2 = iv.findByUniqueId(uniqueId);
            if (item2 != null) {
               slot = item2.getPosition();
               inv = iv;
               break;
            }
         }

         if (GameConstants.isPet(item2.getItemId())) {
            for(value = 0; value < c.getPlayer().pets.length; ++value) {
               if (c.getPlayer().pets[value] != null && slot == c.getPlayer().pets[value].getInventoryPosition()) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "장착 중인 펫은 캐시 보관함에 넣으실 수 없습니다."));
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  c.getSession().writeAndFlush(CSPacket.sendCSFail(3));
                  c.getSession().writeAndFlush(CSPacket.showNXMapleTokens(c.getPlayer()));
                  c.getSession().writeAndFlush(CSPacket.enableCSUse());
                  c.getPlayer().getCashInventory().checkExpire(c);
                  return;
               }
            }
         }

         if (item2 != null && item2.getItemId() == itemId) {
            c.getPlayer().getCashInventory().addToInventory(item2);
            c.getSession().writeAndFlush(CSPacket.confirmToCSInventory(item2, c, -1));
            var10000 = FileoutputUtil.캐시샵인벤로그;
            var10001 = chr.getClient().getAccID();
            FileoutputUtil.log(var10000, "[캐시템 넣기] 계정 번호 : " + var10001 + " | 캐릭터 : " + chr.getName() + "(" + chr.getId() + ")  | 캐시샵에서 " + MapleItemInformationProvider.getInstance().getName(item2.getItemId()) + "(" + item2.getItemId() + ")를 [" + item2.getQuantity() + "] 캐시샵에 넣음");
            if (item2.getPet() != null) {
               c.getPlayer().removePet(item2.getPet(), false);
            }

            save(c);
            inv.removeSlot(slot);
         } else {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(177));
         }
         break;
      case 31:
         slea.skip(5);
         String pwd = slea.readMapleAsciiString();
         int var18 = slea.readInt();
         break;
      case 34:
         int toCharge = slea.readByte() + 1;
         boolean bool1 = toCharge == 2;
         cashItemInfo2 = CashItemFactory.getInstance().getItem(slea.readInt());
         List<Integer> ccc = null;
         if (cashItemInfo2 != null) {
            ccc = CashItemFactory.getInstance().getPackageItems(cashItemInfo2.getId());
         }

         if (cashItemInfo2 == null || ccc == null || ccc.isEmpty()) {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(3));
            doCSPackets(c);
            return;
         }

         if (cashItemInfo2.getSN() >= 130400000 && cashItemInfo2.getSN() <= 130599999 || cashItemInfo2.getSN() >= 130000000 && cashItemInfo2.getSN() <= 130000500 || cashItemInfo2.getSN() >= 130002000 && cashItemInfo2.getSN() <= 130002500) {
            toCharge = 2;
            if (!bool1) {
               c.send(CWvsContext.serverNotice(1, "", "홍보 아이템은 홍보 포인트로만 구입이 가능합니다."));
               doCSPackets(c);
               return;
            }
         } else if (cashItemInfo2.getPrice() > 0) {
            if (bool1) {
               c.send(CWvsContext.serverNotice(1, "", "후원 아이템은 후원 포인트로만 구입이 가능합니다."));
               doCSPackets(c);
               return;
            }

            toCharge = 1;
         }

         if (cashItemInfo2.getLimitMax() > 0) {
            if (c.getKeyValue(cashItemInfo2.getSN().makeConcatWithConstants<invokedynamic>(cashItemInfo2.getSN())) != null && cashItemInfo2.getLimitMax() <= Integer.parseInt(c.getKeyValue(cashItemInfo2.getSN().makeConcatWithConstants<invokedynamic>(cashItemInfo2.getSN())))) {
               return;
            }

            value = c.getKeyValue(cashItemInfo2.getSN().makeConcatWithConstants<invokedynamic>(cashItemInfo2.getSN())) != null ? Integer.parseInt(c.getKeyValue(cashItemInfo2.getSN().makeConcatWithConstants<invokedynamic>(cashItemInfo2.getSN()))) + 1 : 1;
            c.setKeyValue(cashItemInfo2.getSN().makeConcatWithConstants<invokedynamic>(cashItemInfo2.getSN()), value.makeConcatWithConstants<invokedynamic>(value));
            c.getSession().writeAndFlush(CSPacket.showCount(cashItemInfo2.getSN(), value));
         }

         quantity = slea.readInt() / ccc.size();
         if (c.getPlayer().getCSPoints(toCharge) < cashItemInfo2.getPrice() * quantity) {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(3));
            doCSPackets(c);
            return;
         }

         if (!cashItemInfo2.genderEquals(c.getPlayer().getGender())) {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(11));
            doCSPackets(c);
            return;
         }

         if (c.getPlayer().getCashInventory().getItemsSize() >= 500 - ccc.size() * quantity) {
            c.getSession().writeAndFlush(CSPacket.sendCSFail(24));
            doCSPackets(c);
            return;
         }

         if (Arrays.asList(GameConstants.cashBlock).contains(cashItemInfo2.getId())) {
            c.getPlayer().dropMessage(1, GameConstants.getCashBlockedMsg(cashItemInfo2.getId()));
            doCSPackets(c);
            return;
         }

         new HashMap();
         chr.modifyCSPoints(toCharge, -cashItemInfo2.getPrice() * quantity, false);
         save(c);
         if (toCharge == 1) {
            updateCharge(c, cashItemInfo2.getPrice() * quantity);
         }
         break;
      case 48:
         item = CashItemFactory.getInstance().getItem(slea.readInt());
         c.getSession().writeAndFlush(CSPacket.updatePurchaseRecord(item.getSN()));
      }

      doCSPackets(c);
   }

   private static final MapleInventoryType getInventoryType(int id) {
      switch(id) {
      case 50200093:
         return MapleInventoryType.EQUIP;
      case 50200094:
         return MapleInventoryType.USE;
      case 50200095:
         return MapleInventoryType.ETC;
      case 50200197:
         return MapleInventoryType.SETUP;
      default:
         return MapleInventoryType.UNDEFINED;
      }
   }

   public static final void doCSPackets(MapleClient c) {
      c.getSession().writeAndFlush(CSPacket.showNXMapleTokens(c.getPlayer()));
      c.getSession().writeAndFlush(CSPacket.enableCSUse());
      c.getPlayer().getCashInventory().checkExpire(c);
   }

   public static void save(MapleClient c) {
      Connection con = null;

      try {
         con = DatabaseConnection.getConnection();
         c.getPlayer().getCashInventory().save(con);
         con.close();
      } catch (Exception var14) {
         try {
            if (con != null) {
               con.close();
            }
         } catch (Exception var13) {
         }
      } finally {
         try {
            if (con != null) {
               con.close();
            }
         } catch (Exception var12) {
         }

      }

   }
}
