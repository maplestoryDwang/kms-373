package tools.packet;

import client.AvatarLook;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import constants.GameConstants;
import handling.SendPacketOpcode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.CashItemFactory;
import server.CashItemInfo;
import server.CashShop;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

public class CSPacket {
   public static byte[] warpCS(MapleClient c) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.CS_OPEN.getValue());
      PacketHelper.addCharacterInfo(packet, c.getPlayer());
      CashItemFactory cs = CashItemFactory.getInstance();
      packet.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
      packet.write(false);
      packet.write(true);
      packet.write(false);
      packet.write(false);
      packet.write(false);
      packet.write(false);
      packet.writeShort(cs.getAllModInfo().size());
      Iterator var3 = cs.getAllModInfo().iterator();

      while(var3.hasNext()) {
         CashItemInfo.CashModInfo info = (CashItemInfo.CashModInfo)var3.next();
         addModCashItemInfo(packet, info);
      }

      packet.writeShort(0);

      int i;
      for(i = 0; i < 0; ++i) {
         packet.writeInt(0);
         packet.writeMapleAsciiString("");
      }

      packet.writeInt(cs.getRandomItemInfo().size());
      Iterator var10 = cs.getRandomItemInfo().entrySet().iterator();

      while(true) {
         Entry items;
         int id;
         do {
            if (!var10.hasNext()) {
               packet.writeInt(cs.getBestItems().size());
               var10 = cs.getBestItems().iterator();

               while(var10.hasNext()) {
                  Pair<Integer, Integer> item = (Pair)var10.next();
                  packet.write((Integer)item.left);
                  packet.writeInt((Integer)item.right);
               }

               packet.writeInt(0);

               for(i = 0; i < 0; ++i) {
                  packet.write((int)3);
                  packet.writeInt(110000030);
               }

               String[][] strs = new String[][]{{"로얄", "더욱더 예쁘고 멋진 캐릭터를 원해요? 그 비법이 궁금하면 -----> 클릭"}, {"매지컬 하프", "피버의 정상을 향해! 피버의 순간에는! 좋은 아이템이 찾아올 가능성이 더블로 UP됩니다."}, {"메이플 로얄 스타일", "강력한 옵션이 붙어있는 기간한정 특급 아이템들을 놓치지 마세요."}};
               packet.writeInt(strs.length);
               String[][] var13 = strs;
               int var15 = strs.length;

               for(id = 0; id < var15; ++id) {
                  String[] str = var13[id];
                  packet.writeMapleAsciiString(str[0]);
                  packet.writeMapleAsciiString(str[1]);
               }

               packet.writeShort(0);

               int j;
               for(j = 0; j < 0; ++j) {
                  packet.writeZeroBytes(8);
               }

               packet.writeShort(0);

               for(j = 0; j < 0; ++j) {
                  packet.writeInt(0);
                  packet.writeInt(0);
                  packet.writeZeroBytes(40);
                  packet.writeInt(0);
                  packet.writeInt(0);
                  packet.writeInt(0);
                  packet.write((int)0);
                  packet.writeInt(0);
                  packet.writeInt(0);
                  packet.writeInt(0);
                  packet.writeInt(0);
                  packet.writeInt(0);
                  packet.writeZeroBytes(28);
                  packet.write((int)0);
                  packet.writeMapleAsciiString("");
                  packet.writeInt(0);
               }

               packet.writeInt(60);
               return packet.getPacket();
            }

            items = (Entry)var10.next();
            packet.writeInt((Integer)items.getKey());
         } while((Integer)items.getKey() / 1000 != 5533);

         packet.writeInt(((List)items.getValue()).size());
         Iterator iterator = ((List)items.getValue()).iterator();

         while(iterator.hasNext()) {
            id = (Integer)iterator.next();
            packet.writeInt(id);
         }
      }
   }

   public static void addModCashItemInfo(MaplePacketLittleEndianWriter mplew, CashItemInfo.CashModInfo item) {
      int flags = item.flags;
      mplew.writeInt(item.sn);
      mplew.writeLong((long)flags);
      if ((flags & 1) != 0) {
         mplew.writeInt(item.itemid);
      }

      if ((flags & 2) != 0) {
         mplew.writeShort(item.count);
      }

      if ((flags & 16) != 0) {
         mplew.write(item.priority);
      }

      if ((flags & 4) != 0) {
         mplew.writeInt(item.discountPrice);
      }

      if ((flags & 268435456) != 0) {
         mplew.writeInt(0);
      }

      if ((flags & 33554432) != 0) {
         mplew.writeInt(0);
      }

      if ((flags & 8) != 0) {
         mplew.write(item.unk_1 - 1);
      }

      if ((flags & 32) != 0) {
         mplew.writeShort(item.period);
      }

      if ((flags & 131072) != 0) {
         mplew.writeShort(0);
      }

      if ((flags & 262144) != 0) {
         mplew.writeShort(0);
      }

      if ((flags & 64) != 0) {
         mplew.writeInt(0);
      }

      if ((flags & 128) != 0) {
         mplew.writeInt(item.meso);
      }

      if ((flags & 256) != 0) {
         mplew.write(item.unk_2 - 1);
      }

      if ((flags & 512) != 0) {
         mplew.write(item.gender);
      }

      if ((flags & 1024) != 0) {
         mplew.write(item.showUp ? 1 : 0);
      }

      if ((flags & 2048) != 0) {
         mplew.write(item.mark);
      }

      if ((flags & 4096) != 0) {
         mplew.write(item.unk_3 - 1);
      }

      if ((flags & 8192) != 0) {
         mplew.writeShort(0);
      }

      if ((flags & 16384) != 0) {
         mplew.writeShort(0);
      }

      if ((flags & '耀') != 0) {
         mplew.writeShort(0);
      }

      if ((flags & 65536) != 0) {
         List<Integer> pack = CashItemFactory.getInstance().getPackageItems(item.sn);
         if (pack == null) {
            mplew.write((int)0);
         } else {
            mplew.write(pack.size());

            for(int i = 0; i < pack.size(); ++i) {
               mplew.writeInt((Integer)pack.get(i));
            }
         }
      }

      if ((flags & 524288) != 0) {
         mplew.writeInt(0);
      }

      if ((flags & 1048576) != 0) {
         mplew.writeInt(0);
      }

      if ((flags & 2097152) != 0) {
         mplew.write((int)0);
      }

      if ((flags & 4194304) != 0) {
         mplew.write((int)0);
      }

      if ((flags & 8388608) != 0) {
         mplew.write((int)0);
         mplew.write((int)0);
      }

      if ((flags & 16777216) != 0) {
         mplew.write((int)0);
      }

      if ((flags & 67108864) != 0) {
         mplew.write((int)0);
      }

      if ((flags & 134217728) != 0) {
         mplew.writeInt(0);
      }

      if ((flags & 536870912) != 0) {
         mplew.write((int)0);
      }

      if ((flags & 1073741824) != 0) {
         mplew.writeLong(0L);
      }

      if ((flags & Integer.MIN_VALUE) != 0) {
         mplew.write((int)0);
         mplew.write((int)0);
      }

   }

   public static byte[] addCashPoint(String url) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_CHARGE.getValue());
      mplew.writeMapleAsciiString(url);
      return mplew.getPacket();
   }

   public static byte[] showNXMapleTokens(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
      mplew.writeInt(chr.getCSPoints(1));
      mplew.writeInt(chr.getCSPoints(2));
      mplew.writeInt(chr.getCSPoints(4));
      return mplew.getPacket();
   }

   public static byte[] showCount(int sn, int count) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)97);
      mplew.writeInt(sn);
      mplew.writeInt(count);
      return mplew.getPacket();
   }

   public static byte[] LunaCrystal(Item item) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.LUNA_CRYSTAL.getValue());
      mplew.write(true);
      PacketHelper.addItemInfo(mplew, item);
      mplew.writeInt(132);
      mplew.write((int)1);
      mplew.write(GameConstants.getInventoryType(item.getItemId()).getType());
      mplew.writeInt(item.getPosition());
      return mplew.getPacket();
   }

   public static byte[] WonderBerry(byte effect, Item item, int useitemid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WONDER_BERRY.getValue());
      mplew.write(effect);
      mplew.writeInt(useitemid);
      PacketHelper.addItemInfo(mplew, item);
      return mplew.getPacket();
   }

   public static byte[] getCSInventory(MapleClient c) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)6);
      mplew.write(false);
      CashShop mci = c.getPlayer().getCashInventory();
      mplew.writeShort(mci.getItemsSize());
      Iterator var3 = mci.getInventory().iterator();

      while(var3.hasNext()) {
         Item itemz = (Item)var3.next();
         addCashItemInfo(mplew, itemz, c, 0, 0, 0);
      }

      mplew.writeShort(512);
      mplew.writeShort(c.getPlayer().getStorage().getSlots());
      mplew.writeShort(c.getCharacterSlots());
      mplew.writeShort(c.getCharacterSlots() - 4);
      mplew.writeShort(1);
      return mplew.getPacket();
   }

   public static byte[] getCSGifts(MapleClient c) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)8);
      List<Pair<Item, String>> mci = c.getPlayer().getCashInventory().loadGifts();
      mplew.writeShort(mci.size());
      Iterator var3 = mci.iterator();

      while(var3.hasNext()) {
         Pair<Item, String> mcz = (Pair)var3.next();
         mplew.writeLong(((Item)mcz.getLeft()).getUniqueId());
         mplew.writeInt(((Item)mcz.getLeft()).getItemId());
         mplew.writeAsciiString(((Item)mcz.getLeft()).getGiftFrom(), 13);
         mplew.writeAsciiString((String)mcz.getRight(), 73);
      }

      return mplew.getPacket();
   }

   public static byte[] sendWishList(MapleCharacter chr, boolean update) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write(update ? 12 : 10);
      int[] list = chr.getWishlist();

      for(int i = 0; i < 12; ++i) {
         mplew.writeInt(list[i] != -1 ? list[i] : 0);
      }

      return mplew.getPacket();
   }

   public static byte[] showBoughtCSItem(Item item, MapleClient c, int sn, int paybackRate, int discountRate) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)14);
      addCashItemInfo(mplew, item, c, sn, paybackRate, discountRate);
      mplew.writeInt(0);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] resultGiftItem(boolean fail, String recvName, int itemId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write(fail ? 24 : 23);
      if (fail) {
         mplew.write((int)81);
      } else {
         mplew.writeMapleAsciiString(recvName);
         mplew.writeInt(itemId);
         mplew.writeShort(0);
      }

      return mplew.getPacket();
   }

   public static byte[] showBoughtCSPackage(Map<Integer, Item> ccc, MapleClient c) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)70);
      mplew.write(ccc.size());
      Iterator var3 = ccc.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Integer, Item> sn = (Entry)var3.next();
         addCashItemInfo(mplew, (Item)sn.getValue(), c, (Integer)sn.getKey(), 0, 0);
      }

      mplew.writeShort(0);
      mplew.writeInt(0);
      mplew.write(false);
      return mplew.getPacket();
   }

   public static byte[] sendGift(int price, int itemid, int quantity, String receiver, boolean packages) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write(packages ? 130 : 81);
      mplew.writeMapleAsciiString(receiver);
      mplew.writeInt(itemid);
      mplew.writeShort(quantity);
      if (packages) {
         mplew.writeShort(0);
      }

      mplew.writeInt(price);
      return mplew.getPacket();
   }

   public static byte[] showCouponRedeemedItem(Map<Integer, Item> items, int mesos, int maplePoints, MapleClient c) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)61);
      mplew.write(items.size());
      Iterator var5 = items.entrySet().iterator();

      while(var5.hasNext()) {
         Entry<Integer, Item> item = (Entry)var5.next();
         addCashItemInfo(mplew, (Item)item.getValue(), c, (Integer)item.getKey(), 0, 0);
      }

      mplew.writeInt(maplePoints);
      mplew.writeInt(0);
      mplew.writeInt(mesos);
      return mplew.getPacket();
   }

   public static byte[] confirmFromCSInventory(Item item, MapleClient c, short pos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)33);
      mplew.write(true);
      mplew.writeShort(pos);
      PacketHelper.addItemInfo(mplew, item);
      mplew.writeInt(0);
      mplew.write(false);
      return mplew.getPacket();
   }

   public static byte[] confirmToCSInventory(Item item, MapleClient c, int sn) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)35);
      mplew.write(false);
      addCashItemInfo(mplew, item, c, sn, 0, 0);
      return mplew.getPacket();
   }

   public static byte[] cashItemExpired(long uniqueid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)95);
      mplew.writeLong(uniqueid);
      return mplew.getPacket();
   }

   public static byte[] updatePurchaseRecord(int sn) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)97);
      mplew.writeInt(sn);
      mplew.writeInt(100);
      return mplew.getPacket();
   }

   public static byte[] sendBoughtRings(boolean couple, Item item, int sn, MapleClient c, String receiver) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write(couple ? 126 : 116);
      addCashItemInfo(mplew, item, c, sn, 0, 0);
      mplew.writeMapleAsciiString(receiver);
      mplew.writeInt(item.getItemId());
      mplew.writeShort(1);
      return mplew.getPacket();
   }

   public static byte[] showBoughtCSQuestItem(int price, short quantity, byte position, int itemid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)388);
      mplew.writeInt(1);
      mplew.writeInt(quantity);
      mplew.writeInt(itemid);
      return mplew.getPacket();
   }

   public static byte[] sendRandomBox(long uniqueid, Item item, short pos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)154);
      mplew.writeLong(uniqueid);
      mplew.writeInt(0);
      PacketHelper.addItemInfo(mplew, item);
      mplew.writeShort(pos);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] changeNameCheck(String charname, boolean nameUsed) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHANGE_NAME_CHECK.getValue());
      mplew.writeMapleAsciiString(charname);
      mplew.write(nameUsed ? 1 : 0);
      return mplew.getPacket();
   }

   public static byte[] changeNameResponse(int mode, int pic) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHANGE_NAME_RESPONSE.getValue());
      mplew.writeInt(0);
      mplew.write(mode);
      mplew.writeInt(pic);
      return mplew.getPacket();
   }

   public static byte[] playCashSong(int itemid, String name) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CASH_SONG.getValue());
      mplew.writeInt(itemid);
      mplew.writeMapleAsciiString(name);
      return mplew.getPacket();
   }

   public static byte[] ViciousHammer(boolean start, boolean hammered) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
      mplew.write((int)0);
      mplew.write(start ? 0 : 2);
      mplew.writeInt(hammered ? 1 : 0);
      return mplew.getPacket();
   }

   public static byte[] changePetFlag(long uniqueId, boolean added, int flagAdded) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PET_FLAG_CHANGE.getValue());
      mplew.writeLong(uniqueId);
      mplew.write(added ? 1 : 0);
      mplew.writeShort(flagAdded);
      return mplew.getPacket();
   }

   public static byte[] changePetName(MapleCharacter chr, String newname, int slot) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(slot);
      mplew.writeMapleAsciiString(newname);
      return mplew.getPacket();
   }

   public static byte[] OnMemoResult(byte act, byte mode) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
      mplew.write(act);
      if (act == 7 || act == 9) {
         mplew.write(mode);
      }

      return mplew.getPacket();
   }

   public static byte[] NoteHandler(int type, int id) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
      mplew.write(type);
      switch(type) {
      case 11:
      case 13:
         mplew.writeInt(1);
         mplew.writeInt(id);
      default:
         return mplew.getPacket();
      }
   }

   public static byte[] SendNote(int id, int otherid, MapleCharacter chr, String name, String msg, long timestamp) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
      mplew.write((int)15);
      mplew.writeInt(id);
      mplew.write((int)0);
      mplew.writeInt(otherid);
      mplew.writeMapleAsciiString(chr.getName());
      mplew.writeInt(chr.getId());
      mplew.writeMapleAsciiString(name);
      mplew.writeMapleAsciiString(msg);
      mplew.writeLong(timestamp);
      return mplew.getPacket();
   }

   public static byte[] showsendNotes(MapleCharacter chr, ResultSet notes, int count) throws SQLException {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
      mplew.write((int)7);
      mplew.write(count);

      for(int i = 0; i < count; ++i) {
         mplew.writeInt(notes.getInt("id"));
         mplew.write(notes.getInt("show"));
         mplew.writeInt(chr.getId());
         mplew.writeMapleAsciiString(notes.getString("to"));
         mplew.writeInt(notes.getInt("senderid"));
         mplew.writeMapleAsciiString(notes.getString("from"));
         mplew.writeMapleAsciiString(notes.getString("message"));
         mplew.writeLong(PacketHelper.getKoreanTimestamp(notes.getLong("timestamp")));
         notes.next();
      }

      return mplew.getPacket();
   }

   public static byte[] showNotes(ResultSet notes, int count) throws SQLException {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
      mplew.write((int)6);
      mplew.write(count);

      for(int i = 0; i < count; ++i) {
         mplew.writeInt(notes.getInt("id"));
         mplew.write(notes.getInt("show"));
         mplew.writeInt(notes.getInt("senderid"));
         mplew.writeMapleAsciiString(notes.getString("from"));
         mplew.writeMapleAsciiString(notes.getString("message"));
         mplew.writeLong(PacketHelper.getKoreanTimestamp(notes.getLong("timestamp")));
         mplew.write(notes.getInt("gift"));
         mplew.writeMapleAsciiString(notes.getString("from"));
         mplew.writeMapleAsciiString(notes.getString("message"));
         mplew.writeLong(0L);
         mplew.write((int)-1);
         mplew.writeInt(0);
         notes.next();
      }

      return mplew.getPacket();
   }

   public static byte[] useChalkboard(int charid, String msg) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHALKBOARD.getValue());
      mplew.writeInt(charid);
      if (msg != null && msg.length() > 0) {
         mplew.write((int)1);
         mplew.writeMapleAsciiString(msg);
      } else {
         mplew.write((int)0);
      }

      return mplew.getPacket();
   }

   public static byte[] OnMapTransferResult(MapleCharacter chr, byte vip, boolean delete) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.TROCK_LOCATIONS.getValue());
      mplew.write(delete ? 2 : 3);
      mplew.write(vip);
      int[] map;
      int i;
      if (vip == 1) {
         map = chr.getRegRocks();

         for(i = 0; i < 5; ++i) {
            mplew.writeInt(map[i]);
         }
      } else if (vip == 2) {
         map = chr.getRocks();

         for(i = 0; i < 10; ++i) {
            mplew.writeInt(map[i]);
         }
      } else if (vip == 3) {
         map = chr.getHyperRocks();

         for(i = 0; i < 13; ++i) {
            mplew.writeInt(map[i]);
         }
      }

      return mplew.getPacket();
   }

   public static final void addCashItemInfo(MaplePacketLittleEndianWriter mplew, Item item, MapleClient c, int sn, int nPaybackRate, int nDisCountRate) {
      mplew.writeLong(item.getUniqueId());
      mplew.writeInt(c.getAccID());
      mplew.writeInt(c.getPlayer().getId());
      mplew.writeInt(item.getItemId());
      mplew.writeInt(sn);
      mplew.writeShort(item.getQuantity());
      mplew.writeAsciiString(item.getGiftFrom(), 13);
      PacketHelper.addExpirationTime(mplew, item.getExpiration());
      mplew.writeInt(nPaybackRate);
      mplew.writeLong((long)nDisCountRate);
      mplew.writeInt(item.getPosition());
      mplew.writeInt(0);
      mplew.write(false);
      mplew.write((int)1);
      mplew.write(false);
      mplew.write(true);
      PacketHelper.addItemInfo(mplew, item);
   }

   public static byte[] sendCSFail(int err) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)22);
      mplew.write(err);
      return mplew.getPacket();
   }

   public static byte[] enableCSUse() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_USE.getValue());
      mplew.write((int)1);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] buyCharacterSlot() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)31);
      mplew.writeShort(0);
      return mplew.getPacket();
   }

   public static byte[] buyPendantSlot(short date) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
      mplew.write((int)33);
      mplew.writeShort(0);
      mplew.writeShort(date);
      return mplew.getPacket();
   }

   public static byte[] coodinationResult(int type, int msg, MapleCharacter chr) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.COODINATION_RESULT.getValue());
      packet.writeInt(type);
      packet.writeInt(msg);
      packet.writeInt(chr.getCoodination().size());

      for(int i = 0; i < chr.getCoodination().size(); ++i) {
         AvatarLook a = (AvatarLook)chr.getCoodination().get(i);
         a.encodeUnpackAvatarLook(packet);
      }

      return packet.getPacket();
   }
}
