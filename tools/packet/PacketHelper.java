package tools.packet;

import client.Core;
import client.InnerSkillValueHolder;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleCoolDownValueHolder;
import client.MapleHyperStats;
import client.MapleMannequin;
import client.MapleQuestStatus;
import client.MapleTrait;
import client.PlayerStats;
import client.SecondaryStat;
import client.SecondaryStatValueHolder;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.VMatrix;
import client.inventory.Equip;
import client.inventory.EquipSpecialStat;
import client.inventory.EquipStat;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleAndroid;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.KoreaCalendar;
import handling.world.party.MaplePartyCharacter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.Map.Entry;
import server.MapleChatEmoticon;
import server.MapleItemInformationProvider;
import server.MapleSavedEmoticon;
import server.SecondaryStatEffect;
import server.marriage.MarriageDataEntry;
import server.marriage.MarriageManager;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.shops.AbstractPlayerStore;
import server.shops.IMaplePlayerShop;
import server.shops.MapleMiniGame;
import server.shops.MapleShop;
import server.shops.MapleShopItem;
import tools.BitTools;
import tools.HexTool;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class PacketHelper {
   public static final long FT_UT_OFFSET = 116445060000000000L;
   public static final long MAX_TIME = 150842304000000000L;
   public static final long ZERO_TIME = 94354848000000000L;
   public static final long PERMANENT = 150841440000000000L;
   public static final long ZERO_TIME_REVERSE = -153052018564450501L;

   public static final long getKoreanTimestamp(long realTimestamp) {
      return getTime(realTimestamp);
   }

   public static final long getMapleShopTime(int month, int day, int recharge) {
      Calendar ocal = Calendar.getInstance();
      int nowmonth = ocal.get(2) + 1;
      int nowday = ocal.get(5);
      int maxday = ocal.getActualMaximum(5);
      day += recharge;
      if (month == 1 && nowmonth == 12) {
         month = 12;
      } else if (day > maxday) {
         day -= maxday;
      }

      if (month == 2) {
         day += 3;
      } else if (month == 4 || month == 6 || month == 9 || month == 11) {
         ++day;
      }

      long mapletime = 0L;
      boolean hour = false;
      int baseday = 153225;
      GregorianCalendar baseCal = new GregorianCalendar(2020, 7, 8);
      GregorianCalendar targetCal = new GregorianCalendar(ocal.get(1), ocal.get(2) + 1, ocal.get(5));
      long diffSec = (targetCal.getTimeInMillis() - baseCal.getTimeInMillis()) / 1000L;
      long diffDays = diffSec / 86400L;
      baseday = (int)((long)baseday + diffDays);

      int i;
      for(i = 0; i < baseday; ++i) {
         mapletime += 3375000000L;
      }

      if (month - nowmonth == 0) {
         for(i = 0; i < day - nowday; ++i) {
            mapletime += 3375000000L;
         }
      }

      if (month - nowmonth > 0) {
         baseCal = new GregorianCalendar(ocal.get(1), ocal.get(2) + 1, ocal.get(5));
         targetCal = new GregorianCalendar(ocal.get(1), month, day);
         diffSec = (targetCal.getTimeInMillis() - baseCal.getTimeInMillis()) / 1000L;
         diffDays = diffSec / 86400L;

         for(i = 0; (long)i < diffDays; ++i) {
            mapletime += 3375000000L;
         }
      }

      return mapletime;
   }

   public static final long getTime(long realTimestamp) {
      if (realTimestamp == -1L) {
         return 150842304000000000L;
      } else if (realTimestamp == -2L) {
         return 94354848000000000L;
      } else if (realTimestamp == -3L) {
         return 150841440000000000L;
      } else {
         return realTimestamp == -4L ? -153052018564450501L : realTimestamp * 10000L + 116445060000000000L;
      }
   }

   public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
      if (SimpleTimeZone.getDefault().inDaylightTime(new Date())) {
         timeStampinMillis -= 3600000L;
      }

      long time;
      if (roundToMinutes) {
         time = timeStampinMillis / 1000L / 60L * 600000000L;
      } else {
         time = timeStampinMillis * 10000L;
      }

      return time + 116445060000000000L;
   }

   public static byte[] sendPacket(String args) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.write(HexTool.getByteArrayFromHexString(args));
      return mplew.getPacket();
   }

   public static void addQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      List<MapleQuestStatus> started = chr.getStartedQuests();
      mplew.write((int)1);
      mplew.writeShort(started.size());
      Iterator var3 = started.iterator();

      while(true) {
         while(var3.hasNext()) {
            MapleQuestStatus q = (MapleQuestStatus)var3.next();
            mplew.writeInt(q.getQuest().getId());
            if (q.hasMobKills()) {
               StringBuilder sb = new StringBuilder();
               Iterator i$ = q.getMobKills().values().iterator();

               while(i$.hasNext()) {
                  int kills = (Integer)i$.next();
                  sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
               }

               mplew.writeMapleAsciiString(sb.toString());
            } else {
               mplew.writeMapleAsciiString(q.getCustomData() == null ? "" : q.getCustomData());
            }
         }

         mplew.write((int)1);
         List<MapleQuestStatus> completed = chr.getCompletedQuests();
         mplew.writeShort(completed.size());
         Iterator var9 = completed.iterator();

         while(var9.hasNext()) {
            MapleQuestStatus q = (MapleQuestStatus)var9.next();
            mplew.writeInt(q.getQuest().getId());
            mplew.writeLong(getTime(q.getCompletionTime()));
         }

         return;
      }
   }

   public static boolean jobskill(MapleCharacter chr, int skillid) {
      if (GameConstants.isPhantom(chr.getJob())) {
         return true;
      } else if (GameConstants.isAdventurer(chr.getJob()) && skillid < 1000000) {
         return true;
      } else if (skillid == 30001068 && GameConstants.isBattleMage(chr.getJob())) {
         return false;
      } else {
         boolean fame = GameConstants.JobCodeCheck(skillid / 10000, chr.getJob());
         if (skillid / 10000 == 100 && GameConstants.isWarrior(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 200 && GameConstants.isMagician(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 300 && GameConstants.isArcher(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 400 && GameConstants.isThief(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 500 && GameConstants.isPirate(chr.getJob()) && !GameConstants.isCannon(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 1000 && GameConstants.isSoulMaster(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 1000 && GameConstants.isFlameWizard(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 1000 && GameConstants.isWindBreaker(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 1000 && GameConstants.isNightWalker(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 1000 && GameConstants.isStriker(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 3000 && GameConstants.isBlaster(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 3000 && GameConstants.isMechanic(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 3000 && GameConstants.isBattleMage(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 3000 && GameConstants.isWildHunter(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 3001 && GameConstants.isDemonAvenger(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 13500 && GameConstants.isYeti(chr.getJob())) {
            return true;
         } else if (skillid / 10000 == 13100 && GameConstants.isPinkBean(chr.getJob())) {
            return true;
         } else if (PlayerStats.getSkillByJob(12, chr.getJob()) != skillid && PlayerStats.getSkillByJob(73, chr.getJob()) != skillid) {
            if (skillid != 150020079 && skillid != 150021251) {
               return fame;
            } else {
               return false;
            }
         } else {
            return true;
         }
      }
   }

   public static final void addSkillInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.write((int)1);
      Map<Skill, SkillEntry> skills = chr.getSkills();
      mplew.writeShort(skills.size());
      Iterator var3 = skills.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Skill, SkillEntry> skill = (Entry)var3.next();
         mplew.writeInt(((Skill)skill.getKey()).getId());
         mplew.writeInt(((SkillEntry)skill.getValue()).skillevel);
         mplew.writeLong(getTime(((SkillEntry)skill.getValue()).expiration));
         if (SkillFactory.sub_60A550(((Skill)skill.getKey()).getId())) {
            mplew.writeInt(((SkillEntry)skill.getValue()).masterlevel);
         }
      }

      mplew.writeShort(0);

      for(int i = 0; i < 0; ++i) {
         mplew.writeInt(0);
         mplew.writeShort(0);
      }

      List<Triple<Skill, SkillEntry, Integer>> linkskills = chr.getLinkSkills();
      mplew.writeInt(linkskills.size());
      Iterator var9 = linkskills.iterator();

      while(var9.hasNext()) {
         Triple<Skill, SkillEntry, Integer> linkskil = (Triple)var9.next();
         addLinkSkillInfo(mplew, ((Skill)linkskil.getLeft()).getId(), (Integer)linkskil.getRight(), chr.getSkillLevel(((Skill)linkskil.getLeft()).getId()) == 0 ? (Integer)linkskil.getRight() : chr.getId(), ((SkillEntry)linkskil.getMid()).skillevel);
      }

      mplew.write((int)chr.getKeyValue(2498, "hyperstats"));

      for(int i = 0; i <= 2; ++i) {
         mplew.writeInt(chr.loadHyperStats(i).size());
         Iterator var11 = chr.loadHyperStats(i).iterator();

         while(var11.hasNext()) {
            MapleHyperStats mhsz = (MapleHyperStats)var11.next();
            mplew.writeInt(mhsz.getPosition());
            mplew.writeInt(mhsz.getSkillid());
            mplew.writeInt(mhsz.getSkillLevel());
         }
      }

   }

   public static final void addLinkSkillInfo(MaplePacketLittleEndianWriter mplew, int skillid, int sendid, int recvid, int level) {
      mplew.writeInt(sendid);
      mplew.writeInt(recvid);
      mplew.writeInt(skillid);
      mplew.writeShort(level);
      mplew.writeLong(getTime(-2L));
      mplew.writeInt(0);
   }

   public static final void addCoolDownInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      List<MapleCoolDownValueHolder> cd = chr.getCooldowns();
      mplew.writeShort(cd.size());
      Iterator var3 = cd.iterator();

      while(var3.hasNext()) {
         MapleCoolDownValueHolder cooling = (MapleCoolDownValueHolder)var3.next();
         mplew.writeInt(cooling.skillId);
         mplew.writeInt((int)(cooling.length + cooling.startTime - System.currentTimeMillis()) / 1000);
      }

   }

   public static final void addRocksInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      int[] mapz = chr.getRegRocks();

      for(int i = 0; i < 5; ++i) {
         mplew.writeInt(mapz[i]);
      }

      int[] map = chr.getRocks();

      for(int j = 0; j < 10; ++j) {
         mplew.writeInt(map[j]);
      }

      int[] maps = chr.getHyperRocks();

      for(int k = 0; k < 13; ++k) {
         mplew.writeInt(maps[k]);
      }

   }

   public static final void addRingInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
      List<MapleRing> cRing = (List)aRing.getLeft();
      mplew.writeShort(cRing.size());
      Iterator var4 = cRing.iterator();

      while(var4.hasNext()) {
         MapleRing ring = (MapleRing)var4.next();
         mplew.writeInt(ring.getPartnerChrId());
         mplew.writeAsciiString(ring.getPartnerName(), 13);
         mplew.writeLong(ring.getRingId());
         mplew.writeLong(ring.getPartnerRingId());
      }

      List<MapleRing> fRing = (List)aRing.getMid();
      mplew.writeShort(fRing.size());
      Iterator var10 = fRing.iterator();

      while(var10.hasNext()) {
         MapleRing ring = (MapleRing)var10.next();
         mplew.writeInt(ring.getPartnerChrId());
         mplew.writeAsciiString(ring.getPartnerName(), 13);
         mplew.writeLong(ring.getRingId());
         mplew.writeLong(ring.getPartnerRingId());
         mplew.writeInt(ring.getItemId());
      }

      List<MapleRing> mRing = (List)aRing.getRight();
      mplew.writeShort(mRing.size());
      Iterator var12 = mRing.iterator();

      while(var12.hasNext()) {
         MapleRing ring = (MapleRing)var12.next();
         MarriageDataEntry data = MarriageManager.getInstance().getMarriage(chr.getMarriageId());
         if (data == null) {
            System.out.println(chr.getName() + " 캐릭터는 웨딩 데이터가 존재하지 않음.");
            mplew.writeZeroBytes(48);
         } else {
            mplew.writeInt(data.getMarriageId());
            mplew.writeInt(data.getBrideId() == chr.getId() ? data.getBrideId() : data.getGroomId());
            mplew.writeInt(data.getBrideId() == chr.getId() ? data.getGroomId() : data.getBrideId());
            mplew.writeShort(data.getStatus() == 2 ? 3 : data.getStatus());
            mplew.writeInt(ring.getItemId());
            mplew.writeInt(ring.getItemId());
            mplew.writeAsciiString(data.getBrideId() == chr.getId() ? data.getBrideName() : data.getGroomName(), 13);
            mplew.writeAsciiString(data.getBrideId() == chr.getId() ? data.getGroomName() : data.getBrideName(), 13);
         }
      }

   }

   public static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.write((int)chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
      mplew.write((int)chr.getInventory(MapleInventoryType.USE).getSlotLimit());
      mplew.write((int)chr.getInventory(MapleInventoryType.SETUP).getSlotLimit());
      mplew.write((int)chr.getInventory(MapleInventoryType.ETC).getSlotLimit());
      mplew.write((int)chr.getInventory(MapleInventoryType.CASH).getSlotLimit());
      mplew.write((int)chr.getInventory(MapleInventoryType.CODY).getSlotLimit());
      MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(122700));
      if (stat != null && stat.getCustomData() != null && Long.parseLong(stat.getCustomData()) > System.currentTimeMillis()) {
         mplew.writeLong(getTime(Long.parseLong(stat.getCustomData())));
      } else {
         mplew.writeLong(getTime(-1L));
      }

      boolean change_load_type = false;
      mplew.write(change_load_type);
      MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
      List<Item> equipped = iv.newList();
      Collections.sort(equipped);
      Iterator var6 = equipped.iterator();

      Item item;
      while(var6.hasNext()) {
         item = (Item)var6.next();
         if (item.getPosition() < 0 && item.getPosition() > -100) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      if (!change_load_type) {
         iv = chr.getInventory(MapleInventoryType.EQUIP);
         var6 = iv.newList().iterator();

         while(var6.hasNext()) {
            item = (Item)var6.next();
            if (GameConstants.isArcaneSymbol(item.getItemId()) || GameConstants.isAuthenticSymbol(item.getItemId())) {
               chr.getSymbol().add((Equip)item);
            }

            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }

         mplew.writeShort(0);
      }

      var6 = equipped.iterator();

      while(var6.hasNext()) {
         item = (Item)var6.next();
         if (item.getPosition() <= -1000 && item.getPosition() > -1100) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      var6 = equipped.iterator();

      while(var6.hasNext()) {
         item = (Item)var6.next();
         if (item.getPosition() <= -1100 && item.getPosition() > -1200) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      var6 = equipped.iterator();

      while(var6.hasNext()) {
         item = (Item)var6.next();
         if (item.getPosition() <= -1400 && item.getPosition() > -1500) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      var6 = equipped.iterator();

      while(var6.hasNext()) {
         item = (Item)var6.next();
         if (item.getPosition() <= -5000 && item.getPosition() >= -5002) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      var6 = equipped.iterator();

      while(var6.hasNext()) {
         item = (Item)var6.next();
         if (item.getPosition() <= -1600 && item.getPosition() > -1700) {
            chr.getSymbol().add((Equip)item);
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      var6 = equipped.iterator();

      while(var6.hasNext()) {
         item = (Item)var6.next();
         if (item.getPosition() <= -1700 && item.getPosition() > -1800) {
            chr.getSymbol().add((Equip)item);
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      boolean change_load_type2 = false;
      mplew.write(change_load_type2);
      Iterator var14 = equipped.iterator();

      Item item;
      while(var14.hasNext()) {
         item = (Item)var14.next();
         if (item.getPosition() <= -100 && item.getPosition() > -1000) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      if (!change_load_type2) {
         iv = chr.getInventory(MapleInventoryType.CODY);
         var14 = iv.newList().iterator();

         while(var14.hasNext()) {
            item = (Item)var14.next();
            if (GameConstants.isArcaneSymbol(item.getItemId()) || GameConstants.isAuthenticSymbol(item.getItemId())) {
               chr.getSymbol().add((Equip)item);
            }

            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }

         mplew.writeShort(0);
      }

      var14 = equipped.iterator();

      while(var14.hasNext()) {
         item = (Item)var14.next();
         if (item.getPosition() <= -1200 && item.getPosition() > -1300) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      var14 = equipped.iterator();

      while(var14.hasNext()) {
         item = (Item)var14.next();
         if (item.getPosition() <= -1300 && item.getPosition() > -1400) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      var14 = equipped.iterator();

      while(var14.hasNext()) {
         item = (Item)var14.next();
         if (item.getPosition() <= -1500 && item.getPosition() > -1600) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      mplew.writeShort(0);
      mplew.writeShort(0);
      mplew.writeShort(0);
      iv = chr.getInventory(MapleInventoryType.USE);
      var14 = iv.newList().iterator();

      while(var14.hasNext()) {
         item = (Item)var14.next();
         addItemPosition(mplew, item, false, false);
         addItemInfo(mplew, item, chr);
      }

      mplew.writeShort(0);
      iv = chr.getInventory(MapleInventoryType.SETUP);
      var14 = iv.newList().iterator();

      while(var14.hasNext()) {
         item = (Item)var14.next();
         addItemPosition(mplew, item, false, false);
         addItemInfo(mplew, item, chr);
      }

      mplew.writeShort(0);
      iv = chr.getInventory(MapleInventoryType.ETC);
      var14 = iv.newList().iterator();

      while(var14.hasNext()) {
         item = (Item)var14.next();
         if (item.getPosition() < 100) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
         }
      }

      mplew.writeShort(0);
      iv = chr.getInventory(MapleInventoryType.CASH);
      var14 = iv.newList().iterator();

      while(var14.hasNext()) {
         item = (Item)var14.next();
         addItemPosition(mplew, item, false, false);
         addItemInfo(mplew, item, chr);
      }

      mplew.writeShort(0);

      for(int z = 0; z < 3; ++z) {
         int a = 0;
         mplew.writeInt(a);

         for(int k = 0; k < a; ++k) {
            mplew.writeInt(chr.getExtendedSlots().size());
            mplew.writeInt(0);

            for(int i = 0; i < chr.getExtendedSlots().size(); ++i) {
               Iterator var11 = chr.getInventory(MapleInventoryType.ETC).list().iterator();

               while(var11.hasNext()) {
                  Item item = (Item)var11.next();
                  if (item.getPosition() > 10000 && item.getPosition() < 10200) {
                     mplew.writeInt(i);
                     addItemInfo(mplew, item, chr);
                  }
               }

               mplew.writeInt(-1);
            }
         }
      }

      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write((int)0);
   }

   public static final void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      int size;
      for(size = 0; size < 2; ++size) {
         mplew.writeInt(chr.getId());
      }

      mplew.writeInt(0);
      mplew.writeAsciiString(chr.getName(), 13);
      mplew.write(chr.getGender());
      mplew.write(chr.getSkinColor());
      mplew.writeInt(chr.getFace());
      if (chr.getBaseColor() != -1) {
         size = chr.getHair();
         int basehair = false;
         size = (chr.getHair() / 10 * 10 + chr.getBaseColor()) * 1000;
         size += chr.getAddColor() * 100;
         size += chr.getBaseProb();
         mplew.writeInt(size);
      } else {
         mplew.writeInt(chr.getHair());
      }

      mplew.writeInt(chr.getLevel());
      mplew.writeShort(chr.getJob());
      chr.getStat().connectData(mplew);
      mplew.writeShort(chr.getRemainingAp());
      size = chr.getRemainingSpSize();
      if (GameConstants.isSeparatedSp(chr.getJob())) {
         mplew.write(size);

         for(int i = 0; i < chr.getRemainingSps().length; ++i) {
            if (chr.getRemainingSp(i) > 0) {
               mplew.write(i + 1);
               mplew.writeInt(chr.getRemainingSp(i));
            }
         }
      } else {
         mplew.writeShort(chr.getRemainingSp());
      }

      mplew.writeLong(chr.getExp());
      mplew.writeInt(chr.getFame());
      mplew.writeInt(GameConstants.isZero(chr.getJob()) ? chr.getStat().getMp() : 99999L);
      mplew.writeInt(chr.getMapId());
      mplew.write(chr.getInitialSpawnpoint());
      mplew.writeShort(chr.getSubcategory());
      if (!GameConstants.isDemonSlayer(chr.getJob()) && !GameConstants.isXenon(chr.getJob()) && !GameConstants.isDemonAvenger(chr.getJob()) && !GameConstants.isArk(chr.getJob())) {
         if (GameConstants.isHoyeong(chr.getJob())) {
            mplew.writeInt(chr.getDemonMarking());
         }
      } else {
         mplew.writeInt(chr.getDemonMarking());
      }

      mplew.write((int)0);
      mplew.writeLong(getTime(-2L));
      mplew.writeShort(chr.getFatigue());
      KoreaCalendar kc = new KoreaCalendar();
      String var10000 = kc.getYears();
      String now = var10000 + kc.getMonths() + kc.getDays() + kc.getHours();
      mplew.writeInt(Integer.parseInt(now));
      MapleTrait.MapleTraitType[] var5 = MapleTrait.MapleTraitType.values();
      int var6 = var5.length;

      int var7;
      MapleTrait.MapleTraitType t;
      for(var7 = 0; var7 < var6; ++var7) {
         t = var5[var7];
         mplew.writeInt(chr.getTrait(t).getTotalExp());
      }

      var5 = MapleTrait.MapleTraitType.values();
      var6 = var5.length;

      for(var7 = 0; var7 < var6; ++var7) {
         t = var5[var7];
         mplew.writeInt(chr.getTrait(t).getExp());
      }

      mplew.write((int)0);
      mplew.writeLong(getTime(-2L));
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write((int)10);
      mplew.writeInt(0);
      mplew.write((int)5);
      mplew.write((int)5);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeLong(getTime(-1L));
      mplew.writeLong(getTime(-2L));
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write((int)0);
   }

   public static final void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time) {
      mplew.writeLong(getTime(time));
   }

   public static void addItemPosition(MaplePacketLittleEndianWriter mplew, Item item, boolean trade, boolean bagSlot) {
      if (item == null) {
         mplew.write((int)0);
      } else {
         short pos = item.getPosition();
         if (pos <= -1) {
            pos = (short)(pos * -1);
            if (pos > 100 && pos < 1000) {
               pos = (short)(pos - 100);
            }
         }

         if (bagSlot) {
            mplew.writeInt(pos % 100 - 1);
         } else {
            mplew.writeShort(pos);
         }

      }
   }

   public static final void addItemInfo(MaplePacketLittleEndianWriter mplew, Item item) {
      addItemInfo(mplew, item, (MapleCharacter)null);
   }

   public static final void addItemInfo(MaplePacketLittleEndianWriter mplew, Item item, MapleCharacter chr) {
      mplew.write(item.getPet() != null ? 3 : item.getType());
      mplew.writeInt(item.getItemId());
      boolean hasUniqueId = item.getUniqueId() > 0L && !GameConstants.isMarriageRing(item.getItemId()) && item.getItemId() / 10000 != 166;
      mplew.write(hasUniqueId ? 1 : 0);
      if (hasUniqueId) {
         mplew.writeLong(item.getUniqueId());
      }

      if (item.getPet() != null) {
         addPetItemInfo(mplew, chr, item, item.getPet(), true, false);
      } else {
         addExpirationTime(mplew, item.getExpiration());
         mplew.writeInt(chr == null ? -1 : chr.getExtendedSlots().indexOf(item.getItemId()));
         mplew.write(item.getType() == 1 || item.getItemId() == 4001886);
         if (item.getType() == 1) {
            Equip equip = Equip.calculateEquipStats((Equip)item);
            addEquipStats(mplew, equip);
            addEquipBonusStats(mplew, equip, hasUniqueId, chr);
            if (equip.getItemId() / 1000 == 1662) {
               addAndroidLooks(mplew, equip);
            }
         } else if (item.getItemId() == 4001886) {
            if (item.getReward() != null) {
               mplew.writeInt(item.getQuantity());
               mplew.writeMapleAsciiString(item.getOwner());
               mplew.writeLong((long)item.getReward().getObjectId());
               mplew.writeInt(0);
            } else {
               mplew.writeInt(item.getQuantity());
               mplew.writeMapleAsciiString(item.getOwner());
               mplew.writeLong(1L);
               mplew.writeInt(0);
            }
         } else {
            mplew.writeShort(item.getQuantity());
            mplew.writeMapleAsciiString(item.getOwner());
            mplew.writeShort(item.getFlag());
            mplew.writeInt(0);
            if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId()) || item.getItemId() / 10000 == 287) {
               mplew.writeLong(item.getInventoryId() <= 0L ? -1L : item.getInventoryId());
            }
         }
      }

   }

   public static void addEquipStats(MaplePacketLittleEndianWriter mplew, Equip equip) {
      int head = 0;
      EquipStat stat;
      if (equip.getStats().size() > 0) {
         for(Iterator var3 = equip.getStats().iterator(); var3.hasNext(); head |= stat.getValue()) {
            stat = (EquipStat)var3.next();
         }
      }

      mplew.writeInt(head);
      if (head != 0) {
         if (equip.getStats().contains(EquipStat.SLOTS)) {
            mplew.write(equip.getUpgradeSlots());
         }

         if (equip.getStats().contains(EquipStat.LEVEL)) {
            mplew.write(equip.getLevel());
         }

         if (equip.getStats().contains(EquipStat.STR)) {
            mplew.writeShort(equip.getTotalStr());
         }

         if (equip.getStats().contains(EquipStat.DEX)) {
            mplew.writeShort(equip.getTotalDex());
         }

         if (equip.getStats().contains(EquipStat.INT)) {
            mplew.writeShort(equip.getTotalInt());
         }

         if (equip.getStats().contains(EquipStat.LUK)) {
            mplew.writeShort(equip.getTotalLuk());
         }

         if (equip.getStats().contains(EquipStat.MHP)) {
            mplew.writeShort(equip.getTotalHp());
         }

         if (equip.getStats().contains(EquipStat.MMP)) {
            mplew.writeShort(equip.getTotalMp());
         }

         if (equip.getStats().contains(EquipStat.WATK)) {
            mplew.writeShort(equip.getTotalWatk());
         }

         if (equip.getStats().contains(EquipStat.MATK)) {
            mplew.writeShort(equip.getTotalMatk());
         }

         if (equip.getStats().contains(EquipStat.WDEF)) {
            mplew.writeShort(equip.getTotalWdef());
         }

         if (equip.getStats().contains(EquipStat.MDEF)) {
            mplew.writeShort(equip.getTotalMdef());
         }

         if (equip.getStats().contains(EquipStat.ACC)) {
            mplew.writeShort(equip.getTotalAcc());
         }

         if (equip.getStats().contains(EquipStat.AVOID)) {
            mplew.writeShort(equip.getTotalAvoid());
         }

         if (equip.getStats().contains(EquipStat.HANDS)) {
            mplew.writeShort(equip.getHands());
         }

         if (equip.getStats().contains(EquipStat.SPEED)) {
            mplew.writeShort(equip.getSpeed());
         }

         if (equip.getStats().contains(EquipStat.JUMP)) {
            mplew.writeShort(equip.getJump());
         }

         if (equip.getStats().contains(EquipStat.FLAG)) {
            mplew.writeShort(equip.getFlag());
         }

         if (equip.getStats().contains(EquipStat.INC_SKILL)) {
            mplew.write(equip.getIncSkill() > 0 ? 1 : 0);
         }

         if (equip.getStats().contains(EquipStat.ITEM_LEVEL)) {
            mplew.write(Math.max(equip.getBaseLevel(), equip.getEquipLevel()));
         }

         if (equip.getStats().contains(EquipStat.ITEM_EXP)) {
            mplew.writeLong((long)(equip.getExpPercentage() * 100000));
         }

         if (equip.getStats().contains(EquipStat.DURABILITY)) {
            mplew.writeInt(equip.getDurability());
         }

         if (equip.getStats().contains(EquipStat.VICIOUS_HAMMER)) {
            mplew.writeInt(equip.getViciousHammer());
         }

         if (equip.getStats().contains(EquipStat.PVP_DAMAGE)) {
            mplew.writeShort(equip.getPVPDamage());
         }

         if (equip.getStats().contains(EquipStat.DOWNLEVEL)) {
            mplew.write(-equip.getReqLevel());
         }

         if (equip.getStats().contains(EquipStat.ENHANCT_BUFF)) {
            mplew.writeShort(equip.getEnchantBuff());
         }

         if (equip.getStats().contains(EquipStat.DURABILITY_SPECIAL)) {
            mplew.writeInt(equip.getDurability());
         }

         if (equip.getStats().contains(EquipStat.REQUIRED_LEVEL)) {
            mplew.write(equip.getReqLevel());
         }

         if (equip.getStats().contains(EquipStat.YGGDRASIL_WISDOM)) {
            mplew.write(equip.getYggdrasilWisdom());
         }

         if (equip.getStats().contains(EquipStat.FINAL_STRIKE)) {
            mplew.write(equip.getFinalStrike());
         }

         if (equip.getStats().contains(EquipStat.IndieBdr)) {
            mplew.write((int)equip.getBossDamage());
         }

         if (equip.getStats().contains(EquipStat.IGNORE_PDR)) {
            mplew.write((int)equip.getIgnorePDR());
         }
      }

      addEquipSpecialStats(mplew, equip);
   }

   public static void addEquipSpecialStats(MaplePacketLittleEndianWriter mplew, Equip equip) {
      int head = 0;
      EquipSpecialStat stat;
      if (equip.getSpecialStats().size() > 0) {
         for(Iterator var3 = equip.getSpecialStats().iterator(); var3.hasNext(); head |= stat.getValue()) {
            stat = (EquipSpecialStat)var3.next();
         }
      }

      mplew.writeInt(head);
      if (head != 0) {
         if (equip.getSpecialStats().contains(EquipSpecialStat.TOTAL_DAMAGE)) {
            mplew.write(equip.getTotalDamage());
         }

         if (equip.getSpecialStats().contains(EquipSpecialStat.ALL_STAT)) {
            mplew.write(equip.getAllStat());
         }

         if (equip.getSpecialStats().contains(EquipSpecialStat.KARMA_COUNT)) {
            mplew.write(equip.getKarmaCount());
         }

         if (equip.getSpecialStats().contains(EquipSpecialStat.REBIRTH_FIRE)) {
            mplew.writeLong(equip.getFire());
         }

         if (equip.getSpecialStats().contains(EquipSpecialStat.EQUIPMENT_TYPE)) {
            mplew.writeInt(equip.getEquipmentType() == 1 ? 306 : (equip.getEquipmentType() == 2 ? 344 : (equip.getEquipmentType() == 3 ? 282 : equip.getEquipmentType())));
         }
      }

   }

   public static void addEquipBonusStats(MaplePacketLittleEndianWriter mplew, Equip equip, boolean hasUniqueId, MapleCharacter chr) {
      mplew.writeMapleAsciiString(equip.getOwner());
      mplew.write(equip.getState());
      mplew.write(equip.getEnhance());
      mplew.writeShort(equip.getPotential1());
      mplew.writeShort(equip.getPotential2());
      mplew.writeShort(equip.getPotential3());
      mplew.writeShort(equip.getPotential4());
      mplew.writeShort(equip.getPotential5());
      mplew.writeShort(equip.getPotential6());
      mplew.writeShort(equip.getMoru());
      if (!hasUniqueId) {
         mplew.writeLong(equip.getInventoryId() <= 0L ? -1L : equip.getInventoryId());
      }

      mplew.writeLong(equip.getUniqueId() == -1L ? 0L : equip.getUniqueId());
      if (equip.getOptionExpiration() > 0L) {
         addExpirationTime(mplew, equip.getOptionExpiration());
      } else {
         mplew.writeLong(getTime(-2L));
      }

      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (ii.isCash(equip.getItemId())) {
         if (equip.getItemId() / 1000000 == 1) {
            mplew.writeInt(equip.getEquipmentType());
            mplew.writeInt(equip.getCoption1());
            mplew.writeInt(equip.getCoption2());
            mplew.writeInt(equip.getCoption3());
         }
      } else {
         mplew.writeZeroBytes(16);
      }

      mplew.writeShort(equip.getSoulName());
      mplew.writeShort(equip.getSoulEnchanter());
      mplew.writeShort(equip.getSoulPotential());
      if (GameConstants.isArcaneSymbol(equip.getItemId())) {
         mplew.writeShort(equip.getArc());
         mplew.writeInt(equip.getArcEXP());
         mplew.writeShort(equip.getArcLevel());
      } else if (GameConstants.isAuthenticSymbol(equip.getItemId())) {
         mplew.writeShort(equip.getArc());
         mplew.writeInt(equip.getArcEXP());
         mplew.writeShort(equip.getArcLevel());
      }

      mplew.writeShort(-1);
      mplew.writeLong(getTime(-1L));
      mplew.writeLong(getTime(-2L));
      mplew.writeLong(getTime(-1L));
   }

   public static void addAndroidLooks(MaplePacketLittleEndianWriter mplew, Equip equip) {
      MapleAndroid and = equip.getAndroid();
      if (and == null) {
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.writeShort(0);
         mplew.writeShort(0);
         mplew.writeShort(0);
         mplew.writeMapleAsciiString("안드로이드");
         mplew.writeInt(0);
         mplew.writeLong(getTime(-2L));
      } else {
         mplew.write(and.getSkin());
         mplew.write((int)0);
         mplew.writeShort(and.getHair());
         mplew.writeShort(0);
         mplew.writeShort(and.getFace());
         mplew.writeShort(0);
         mplew.writeMapleAsciiString(and.getName());
         mplew.writeInt(and.getEar() ? 0 : 1);
         mplew.writeLong(getTime(-2L));
      }

   }

   public static final void serializeMovementList(MaplePacketLittleEndianWriter lew, List<LifeMovementFragment> moves) {
      lew.writeShort(moves.size());
      Iterator var2 = moves.iterator();

      while(var2.hasNext()) {
         LifeMovementFragment move = (LifeMovementFragment)var2.next();
         move.serialize(lew);
      }

   }

   public static final void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr) && chr.getPlayerShop().getShopType() != 1 && chr.getPlayerShop().isAvailable()) {
         addInteraction(mplew, chr.getPlayerShop());
      } else {
         mplew.write((int)0);
      }

   }

   public static final void addInteraction(MaplePacketLittleEndianWriter mplew, IMaplePlayerShop shop) {
      mplew.write(shop.getGameType());
      mplew.writeInt(((AbstractPlayerStore)shop).getObjectId());
      mplew.writeMapleAsciiString(shop.getDescription());
      if (shop.getShopType() != 1) {
         mplew.write(shop.getPassword().length() > 0 ? 1 : 0);
      }

      if (shop.getItemId() == 5250500) {
         mplew.write((int)0);
      } else if (shop.getItemId() == 4080100) {
         mplew.write(((MapleMiniGame)shop).getPieceType());
      } else if (shop.getItemId() >= 4080000 && shop.getItemId() < 4080100) {
         mplew.write(((MapleMiniGame)shop).getPieceType());
      } else {
         mplew.write(shop.getItemId() % 10);
      }

      mplew.write(shop.getSize());
      mplew.write(shop.getMaxSize());
      if (shop.getShopType() != 1) {
         mplew.write(shop.isOpen() ? 0 : 1);
      }

      ChatPacket(mplew, shop.getOwnerName(), "[미니룸]" + shop.getDescription());
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeShort(0);
   }

   public static final void addCharacterInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.writeLong(-1L);
      mplew.write((int)0);

      for(int i = 0; i < 3; ++i) {
         mplew.writeInt(-3);
      }

      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.write((int)0);
      addCharStats(mplew, chr);
      mplew.write(chr.getBuddylist().getCapacity());
      if (chr.getBlessOfFairyOrigin() != null) {
         mplew.write((int)1);
         mplew.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
      } else {
         mplew.write((int)0);
      }

      if (chr.getBlessOfEmpressOrigin() != null) {
         mplew.write((int)1);
         mplew.writeMapleAsciiString(chr.getBlessOfEmpressOrigin());
      } else {
         mplew.write((int)0);
      }

      MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(111111));
      if (ultExplorer != null && ultExplorer.getCustomData() != null) {
         mplew.write((int)1);
         mplew.writeMapleAsciiString(ultExplorer.getCustomData());
      } else {
         mplew.write((int)0);
      }

      mplew.writeLong(chr.getMeso());
      mplew.writeInt(0);
      addInventoryInfo(mplew, chr);
      addSkillInfo(mplew, chr);
      addCoolDownInfo(mplew, chr);
      mplew.writeZeroBytes(30);
      addQuestInfo(mplew, chr);
      mplew.writeShort(0);
      addRingInfo(mplew, chr);
      addRocksInfo(mplew, chr);
      chr.QuestInfoPacket(mplew);
      mplew.writeShort(0);
      mplew.write(true);
      chr.specialQustInfoPacket(mplew);
      mplew.writeInt(1);
      mplew.writeInt(chr.getAccountID());
      mplew.writeInt(-1);
      if (GameConstants.isWildHunter(chr.getJob())) {
         addJaguarInfo(mplew, chr);
      }

      if (GameConstants.isZero(chr.getJob())) {
         addZeroInfo(mplew, chr);
      }

      mplew.writeShort(0);
      mplew.writeShort(0);
      addStealSkills(mplew, chr);
      addAbilityInfo(mplew, chr);
      mplew.writeShort(0);
      mplew.writeInt(0);
      mplew.write((int)0);
      addHonorInfo(mplew, chr);
      mplew.write((int)1);
      mplew.writeShort(0);
      mplew.write(chr.returnscroll != null);
      if (chr.returnscroll != null) {
         addItemInfo(mplew, chr.returnscroll);
         mplew.writeInt(chr.returnSc);
      }

      boolean tr = GameConstants.isAngelicBuster(chr.getJob());
      mplew.writeInt(tr ? chr.getSecondFace() : 0);
      int hair = chr.getSecondHair();
      if (chr.getSecondBaseColor() != -1) {
         hair = chr.getSecondHair() / 10 * 10 + chr.getSecondBaseColor();
      }

      mplew.writeInt(tr ? hair : 0);
      mplew.writeInt(tr ? 1051291 : 0);
      mplew.write(tr ? chr.getSecondSkinColor() : 0);
      mplew.writeShort(0);
      mplew.writeShort(0);
      mplew.write((int)0);
      addFarmInfo(mplew, chr.getClient(), 0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write(chr.choicepotential != null && chr.memorialcube != null);
      if (chr.choicepotential != null && chr.memorialcube != null) {
         addItemInfo(mplew, chr.choicepotential);
         mplew.writeInt(chr.memorialcube.getItemId());
         mplew.writeInt(chr.choicepotential.getPosition());
      }

      mplew.writeLong(0L);
      mplew.writeLong(0L);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeShort(0);
      mplew.writeLong(0L);
      mplew.writeInt(0);
      mplew.writeInt(chr.getId());
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeLong(0L);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeLong(0L);
      mplew.writeLong(0L);
      mplew.writeShort(chr.getClient().getCustomDatas().size());
      Iterator var5 = chr.getClient().getCustomDatas().entrySet().iterator();

      while(var5.hasNext()) {
         Entry<Integer, List<Pair<String, String>>> customData = (Entry)var5.next();
         mplew.writeInt((Integer)customData.getKey());
         StringBuilder sb = new StringBuilder();
         Iterator var8 = ((List)customData.getValue()).iterator();

         while(var8.hasNext()) {
            Pair<String, String> datas = (Pair)var8.next();
            String var10001 = (String)datas.left;
            sb.append(var10001 + "=" + (String)datas.right + ";");
         }

         mplew.writeMapleAsciiString(sb.toString());
      }

      String[][] values = new String[][]{{"2", "0:0:2:10=1;2:0:2:10=106;4:0:4:20=9;5:0:2:13=1;3:1:2:14=12;0:1:4:22=59;2:1:4:22=3;8:1:3:15=64;11:0:3:16=19;3:1:0:2=4"}, {"4", "1:0:2:10=18;9:0:2:10=1106;2:1:2:10=1760;7:0:4:21=321;5:0:2:14=2;7:1:4:20=8;0:5:4:21=4;7:0:0:1=90;9:1:3:17=5;4:3:0:0=26"}, {"6", "7:1:2:10=1;10:1:2:11=427;8:2:2:10=99;9:1:2:12=354;2:0:4:22=52;10:2:4:20=65;10:2:2:13=29;0:1:0:0=24;8:1:0:0=61;11:1:1:5=35"}, {"8", "9:2:2:10=404;3:2:2:11=16;3:1:2:12=19;9:0:4:20=2;1:0:2:14=11;2:1:2:13=1;0:3:2:13=69;7:0:3:16=176;8:1:0:1=45;8:3:3:19=20"}, {"10", ""}};
      mplew.writeShort(values.length);
      String[][] var12 = values;
      int var15 = values.length;

      for(int var18 = 0; var18 < var15; ++var18) {
         String[] value = var12[var18];
         mplew.writeInt(Integer.parseInt(value[0]));
         mplew.writeMapleAsciiString(value[1]);
      }

      mplew.write(chr.getClient().isFarm());
      mplew.writeInt(0);
      mplew.write((int)0);
      addMatrixInfo(mplew, chr);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeLong(0L);
      mplew.writeLong(0L);
      mplew.writeLong(0L);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      addHairRoomInfo(mplew, chr);
      addFaceRoomInfo(mplew, chr);
      addSkinRoomInfo(mplew, chr);
      mplew.writeInt(chr.getEmoticons().size());
      Iterator var13 = chr.getEmoticons().iterator();

      while(var13.hasNext()) {
         Triple<Long, Integer, Short> em = (Triple)var13.next();
         mplew.writeInt((Integer)em.mid);
         mplew.writeInt((Integer)em.mid);
         mplew.writeLong((Long)em.left);
         mplew.writeShort((Short)em.right);
      }

      int count = 0;
      mplew.writeInt(chr.getEmoticonTabs().size());
      Iterator var17 = chr.getEmoticonTabs().iterator();

      while(var17.hasNext()) {
         MapleChatEmoticon em = (MapleChatEmoticon)var17.next();
         ++count;
         mplew.writeShort(count);
         mplew.writeInt(em.getEmoticonid());
      }

      mplew.writeShort(8);
      count = 0;
      mplew.writeInt(chr.getSavedEmoticon().size());
      var17 = chr.getSavedEmoticon().iterator();

      MapleSavedEmoticon em;
      while(var17.hasNext()) {
         em = (MapleSavedEmoticon)var17.next();
         ++count;
         mplew.writeShort(count);
         mplew.writeInt(em.getEmoticonid());
         mplew.writeAsciiString(em.getText(), 21);
      }

      mplew.writeInt(chr.getSavedEmoticon().size());
      var17 = chr.getSavedEmoticon().iterator();

      while(var17.hasNext()) {
         em = (MapleSavedEmoticon)var17.next();
         mplew.writeMapleAsciiString(em.getText());
         mplew.writeInt(em.getEmoticonid());
         mplew.writeAsciiString(em.getText(), 21);
      }

      mplew.writeInt(0);
   }

   public static void addFaceRoomInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.write((int)15);
      mplew.write(chr.getFaceRoom().size());

      for(int i = 1; i <= 21; ++i) {
         if (chr.getFaceRoom().size() < i) {
            mplew.write(false);
         } else {
            MapleMannequin hair = (MapleMannequin)chr.getFaceRoom().get(i - 1);
            mplew.write(i);
            mplew.write((int)0);
            mplew.writeInt(hair.getValue());
         }
      }

   }

   public static void addHairRoomInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.write((int)15);
      mplew.write(chr.getHairRoom().size());

      for(int i = 1; i <= 21; ++i) {
         if (chr.getHairRoom().size() < i) {
            mplew.write(false);
         } else {
            MapleMannequin hair = (MapleMannequin)chr.getHairRoom().get(i - 1);
            mplew.write(i);
            mplew.write((int)0);
            mplew.writeInt(hair.getValue());
         }
      }

   }

   public static void addSkinRoomInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.write((int)6);
      mplew.write(chr.getSkinRoom().size());

      for(int i = 1; i <= 21; ++i) {
         if (chr.getSkinRoom().size() < i) {
            mplew.write(false);
         } else {
            MapleMannequin skin = (MapleMannequin)chr.getSkinRoom().get(i - 1);
            mplew.write(i);
            mplew.write((int)0);
            mplew.writeInt(skin.getValue());
         }
      }

   }

   public static void addMatrixInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.writeInt(chr.getCore().size());
      Iterator var2 = chr.getCore().iterator();

      while(var2.hasNext()) {
         Core m = (Core)var2.next();
         mplew.writeLong(m.getCrcId());
         mplew.writeInt(m.getCoreId());
         mplew.writeInt(m.getLevel());
         mplew.writeInt(m.getExp());
         mplew.writeInt(m.getState());
         mplew.writeInt(m.getSkill1());
         mplew.writeInt(m.getSkill2());
         mplew.writeInt(m.getSkill3());
         mplew.writeInt(m.getPosition());
         mplew.writeLong(m.getPeriod() > 0L ? getTime(m.getPeriod()) : getTime(-1L));
         mplew.write(m.isLock());
      }

      mplew.writeInt(chr.getMatrixs().size());
      var2 = chr.getMatrixs().iterator();

      while(var2.hasNext()) {
         VMatrix matrix = (VMatrix)var2.next();
         mplew.writeInt(matrix.getId());
         mplew.writeInt(matrix.getPosition());
         mplew.writeInt(matrix.getLevel());
         mplew.write(matrix.isUnLock());
      }

   }

   public static void addFarmInfo(MaplePacketLittleEndianWriter mplew, MapleClient c, int idk) {
      mplew.writeMapleAsciiString("생성중");
      mplew.writeInt(0);
      mplew.writeInt(1);
      mplew.writeInt(10);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(1);
   }

   public static void addZeroInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.writeShort(-1);
      mplew.write(chr.getGender());
      mplew.writeInt(chr.getStat().getHp());
      mplew.writeInt(chr.getStat().getMp());
      mplew.write(chr.getSecondSkinColor());
      int hair = chr.getSecondHair();
      if (chr.getSecondBaseColor() != -1) {
         hair = chr.getSecondHair() / 10 * 10 + chr.getSecondBaseColor();
      }

      mplew.writeInt(hair);
      mplew.writeInt(chr.getSecondFace());
      mplew.writeInt(chr.getStat().getMaxHp());
      mplew.writeInt(chr.getStat().getMaxMp());
      mplew.writeInt(0);
      mplew.writeInt(0);
   }

   public static void addAbilityInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      List<InnerSkillValueHolder> skills = chr.getInnerSkills();
      mplew.writeShort(skills.size());

      for(int i = 0; i < skills.size(); ++i) {
         mplew.write(i + 1);
         mplew.writeInt(((InnerSkillValueHolder)skills.get(i)).getSkillId());
         mplew.write(((InnerSkillValueHolder)skills.get(i)).getSkillLevel());
         mplew.write(((InnerSkillValueHolder)skills.get(i)).getRank());
      }

   }

   public static void addHonorInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.writeInt(chr.getHonorLevel());
      mplew.writeInt(chr.getHonourExp());
   }

   public static void addStolenSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int jobNum) {
      int count = 0;
      if (chr.getStolenSkills() != null) {
         Iterator var4 = chr.getStolenSkills().iterator();

         while(var4.hasNext()) {
            Pair<Integer, Boolean> sk = (Pair)var4.next();
            if (GameConstants.getJobNumber((Integer)sk.left) == jobNum) {
               mplew.writeInt((Integer)sk.left);
               ++count;
               if (count >= GameConstants.getNumSteal(jobNum)) {
                  break;
               }
            }
         }
      }

      while(count < GameConstants.getNumSteal(jobNum)) {
         mplew.writeInt(0);
         ++count;
      }

   }

   public static void addChosenSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      for(int i = 1; i <= 5; ++i) {
         boolean found = false;
         if (chr.getStolenSkills() != null) {
            Iterator var4 = chr.getStolenSkills().iterator();

            while(var4.hasNext()) {
               Pair<Integer, Boolean> sk = (Pair)var4.next();
               if (GameConstants.getJobNumber((Integer)sk.left) == i && (Boolean)sk.right) {
                  mplew.writeInt((Integer)sk.left);
                  found = true;
                  break;
               }
            }
         }

         if (!found) {
            mplew.writeInt(0);
         }
      }

   }

   public static void addStealSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      for(int i = 1; i <= 5; ++i) {
         addStolenSkills(mplew, chr, i);
      }

      addChosenSkills(mplew, chr);
   }

   public static final void addPetItemInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter player, Item item, MaplePet pet, boolean unequip, boolean petLoot) {
      if (item == null) {
         mplew.writeLong(getKoreanTimestamp((long)((double)System.currentTimeMillis() * 1.5D)));
      } else {
         if (item.getExpiration() <= 0L) {
            item.setExpiration(System.currentTimeMillis() + 7776000000L);
         }

         addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
      }

      mplew.writeInt(-1);
      mplew.write((int)1);
      mplew.writeAsciiString(pet.getName() == null ? "" : pet.getName(), 13);
      mplew.write(pet.getLevel());
      mplew.writeShort(pet.getCloseness());
      mplew.write(pet.getFullness());
      if (item == null) {
         mplew.writeLong(getKoreanTimestamp((long)((double)System.currentTimeMillis() * 1.5D)));
      } else {
         mplew.writeLong(getTime(item.getExpiration()));
      }

      mplew.writeShort(0);
      mplew.writeShort(pet.getFlags());
      mplew.writeInt(0);
      mplew.writeShort(item != null && ItemFlag.KARMA_USE.check(item.getFlag()) ? 1 : 0);
      mplew.write(unequip ? 0 : player.getPetIndex(pet) + 1);
      if (player == null) {
         mplew.writeInt(0);
         mplew.writeInt(0);
      } else {
         mplew.writeInt(pet.getBuffSkillId());
         mplew.writeInt(pet.getBuffSkillId2());
      }

      mplew.writeInt(pet.getColor());
      mplew.writeShort(pet.getPetSize());
      mplew.writeShort(pet.getWonderGrade());
   }

   public static void addShopInfo(MaplePacketLittleEndianWriter mplew, MapleShop shop, MapleClient c) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      mplew.writeShort(0);
      mplew.writeShort(shop.getItems().size());
      Iterator var4 = shop.getItems().iterator();

      while(var4.hasNext()) {
         MapleShopItem item = (MapleShopItem)var4.next();
         addShopItemInfo(mplew, item, shop, ii, (Item)null, c.getPlayer());
      }

      if (shop.getNpcId() == 9001212) {
         c.send(CField.NPCPacket.BossRewardSetting());
      }

   }

   public static void addShopItemInfo(MaplePacketLittleEndianWriter mplew, MapleShopItem item, MapleShop shop, MapleItemInformationProvider ii, Item i, MapleCharacter chr) {
      int quantity;
      if (shop.getQuestEx() != 0) {
         quantity = (int)chr.getKeyValue(shop.getQuestEx() + 100000, item.getShopItemId() + "_buyed");
         if (quantity <= -1) {
            quantity = 0;
         }

         mplew.writeInt(item.getBuyQuantity() - quantity);
      } else {
         mplew.writeInt(item.getBuyQuantity());
      }

      mplew.writeInt(item.getItemId());
      mplew.writeInt(item.getTab());
      if (shop.getQuestEx() != 0) {
         quantity = (int)chr.getKeyValue(shop.getQuestEx() + 100000, item.getShopItemId() + "_buyed");
         if (quantity <= -1) {
            quantity = 0;
         }

         mplew.writeInt(item.getBuyQuantity() - quantity);
      } else {
         mplew.writeInt(item.getBuyQuantity());
      }

      mplew.writeInt(item.getPeriod() * 1440);
      mplew.writeInt(0);
      if (shop.getCoinKey() > 0) {
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
      } else if (item.getPriceQuantity() > 0) {
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(item.getPrice());
         mplew.writeInt(item.getPriceQuantity());
      } else {
         mplew.writeInt(item.getPrice());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
      }

      mplew.writeInt(shop.getCoinKey());
      mplew.writeInt(shop.getCoinKey() > 0 ? item.getPrice() : 0L);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeZeroBytes(11);
      mplew.writeLong(getTime(-2L));
      mplew.writeLong(getTime(-1L));
      mplew.writeInt(0);
      mplew.writeShort(1);
      mplew.write((int)0);
      mplew.writeInt(shop.getQuestEx());
      mplew.writeShort(0);
      mplew.writeInt(item.getItemRate());
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeShort(0);
      if (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId())) {
         mplew.writeShort(item.getQuantity() > 1 ? item.getQuantity() : 1);
         mplew.writeShort(item.getBuyable());
      } else {
         mplew.write(HexTool.getByteArrayFromHexString("9A 99 99 99 99 99"));
         mplew.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
         mplew.writeShort(ii.getSlotMax(item.getItemId()));
      }

      mplew.write(i == null ? 0 : 1);
      if (i != null) {
         addItemInfo(mplew, i);
      }

   }

   public static final void addJaguarInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
      mplew.write(chr.getInfoQuest(123456).equals("") ? 0 : Byte.parseByte(chr.getInfoQuest(123456)));

      for(int i = 0; i < 5; ++i) {
         mplew.writeInt(0);
      }

   }

   public static void writeMonsterMask(MaplePacketLittleEndianWriter mplew, List<Pair<MonsterStatus, MonsterStatusEffect>> statups) {
      int[] mask = new int[4];

      Pair statup;
      for(Iterator var3 = statups.iterator(); var3.hasNext(); mask[((MonsterStatus)statup.left).getPosition() - 1] |= ((MonsterStatus)statup.left).getValue()) {
         statup = (Pair)var3.next();
      }

      for(int i = mask.length; i >= 1; --i) {
         mplew.writeInt(mask[i - 1]);
      }

   }

   public static void writeMonsterMaskT(MaplePacketLittleEndianWriter mplew, Collection<Pair<MonsterStatus, MonsterStatusEffect>> statups) {
      int[] mask = new int[4];

      Pair statup;
      for(Iterator var3 = statups.iterator(); var3.hasNext(); mask[((MonsterStatus)statup.left).getPosition() - 1] |= ((MonsterStatus)statup.left).getValue()) {
         statup = (Pair)var3.next();
      }

      for(int i = mask.length; i >= 1; --i) {
         mplew.writeInt(mask[i - 1]);
      }

   }

   public static void writeBuffMask(MaplePacketLittleEndianWriter mplew, List<SecondaryStat> statups) {
      int[] mask = new int[31];

      SecondaryStat statup;
      for(Iterator var3 = statups.iterator(); var3.hasNext(); mask[statup.getPosition() - 1] |= statup.getValue()) {
         statup = (SecondaryStat)var3.next();
      }

      for(int i = mask.length; i >= 1; --i) {
         mplew.writeInt(mask[i - 1]);
      }

   }

   public static void writeBuffMask(MaplePacketLittleEndianWriter mplew, Collection<Pair<SecondaryStat, Pair<Integer, Integer>>> statups) {
      int[] mask = new int[31];

      Pair statup;
      for(Iterator var3 = statups.iterator(); var3.hasNext(); mask[((SecondaryStat)statup.left).getPosition() - 1] |= ((SecondaryStat)statup.left).getValue()) {
         statup = (Pair)var3.next();
      }

      for(int i = mask.length; i >= 1; --i) {
         mplew.writeInt(mask[i - 1]);
      }

   }

   public static List<Pair<SecondaryStat, Pair<Integer, Integer>>> sortBuffStats(Map<SecondaryStat, Pair<Integer, Integer>> statups) {
      List<Pair<SecondaryStat, Pair<Integer, Integer>>> statvals = new ArrayList();
      Iterator var3 = statups.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<SecondaryStat, Pair<Integer, Integer>> stat = (Entry)var3.next();
         statvals.add(new Pair((SecondaryStat)stat.getKey(), (Pair)stat.getValue()));
      }

      boolean changed;
      do {
         changed = false;
         int i = 0;
         int k = 1;

         for(int iter = 0; iter < statvals.size() - 1; ++iter) {
            Pair<SecondaryStat, Pair<Integer, Integer>> a = (Pair)statvals.get(i);
            Pair<SecondaryStat, Pair<Integer, Integer>> b = (Pair)statvals.get(k);
            if (a != null && b != null && ((SecondaryStat)a.left).getFlag() > ((SecondaryStat)b.left).getFlag()) {
               Pair<SecondaryStat, Pair<Integer, Integer>> swap = new Pair((SecondaryStat)a.left, (Pair)a.right);
               statvals.remove(i);
               statvals.add(i, b);
               statvals.remove(k);
               statvals.add(k, swap);
               changed = true;
            }

            ++i;
            ++k;
         }
      } while(changed);

      return statvals;
   }

   public static List<Pair<SecondaryStat, List<SecondaryStatValueHolder>>> sortIndieBuffStats(Map<SecondaryStat, List<SecondaryStatValueHolder>> statups) {
      List<Pair<SecondaryStat, List<SecondaryStatValueHolder>>> statvals = new ArrayList();
      Iterator var3 = statups.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<SecondaryStat, List<SecondaryStatValueHolder>> stat = (Entry)var3.next();
         statvals.add(new Pair((SecondaryStat)stat.getKey(), (List)stat.getValue()));
      }

      boolean changed;
      do {
         changed = false;
         int i = 0;
         int k = 1;

         for(int iter = 0; iter < statvals.size() - 1; ++iter) {
            Pair<SecondaryStat, List<SecondaryStatValueHolder>> a = (Pair)statvals.get(i);
            Pair<SecondaryStat, List<SecondaryStatValueHolder>> b = (Pair)statvals.get(k);
            if (a != null && b != null && ((SecondaryStat)a.left).getFlag() > ((SecondaryStat)b.left).getFlag()) {
               Pair<SecondaryStat, List<SecondaryStatValueHolder>> swap = new Pair((SecondaryStat)a.left, (List)a.right);
               statvals.remove(i);
               statvals.add(i, b);
               statvals.remove(k);
               statvals.add(k, swap);
               changed = true;
            }

            ++i;
            ++k;
         }
      } while(changed);

      return statvals;
   }

   public static List<Pair<MonsterStatus, MonsterStatusEffect>> sortMBuffStats(List<Pair<MonsterStatus, MonsterStatusEffect>> statups) {
      List<Pair<MonsterStatus, MonsterStatusEffect>> statvals = new LinkedList();
      Iterator var2 = statups.iterator();

      while(var2.hasNext()) {
         Pair<MonsterStatus, MonsterStatusEffect> ms = (Pair)var2.next();
         if (ms != null) {
            statvals.add(new Pair((MonsterStatus)ms.getLeft(), (MonsterStatusEffect)ms.getRight()));
         }
      }

      boolean changed;
      do {
         changed = false;
         int i = 0;
         int k = 1;

         for(int iter = 0; iter < statvals.size() - 1; ++iter) {
            Pair<MonsterStatus, MonsterStatusEffect> a = (Pair)statvals.get(i);
            Pair<MonsterStatus, MonsterStatusEffect> b = (Pair)statvals.get(k);
            if (a != null && b != null && ((MonsterStatus)a.left).getFlag() > ((MonsterStatus)b.left).getFlag()) {
               Pair<MonsterStatus, MonsterStatusEffect> swap = new Pair((MonsterStatus)a.left, (MonsterStatusEffect)a.right);
               statvals.remove(i);
               statvals.add(i, b);
               statvals.remove(k);
               statvals.add(k, swap);
               changed = true;
            }

            ++i;
            ++k;
         }
      } while(changed);

      return statvals;
   }

   public static void ArcaneSymbol(MaplePacketLittleEndianWriter mplew, Item item) {
      Equip equip = (Equip)item;
      mplew.writeInt(0);
      mplew.writeInt(equip.getArcLevel());
      mplew.writeInt(equip.getArcLevel() * equip.getArcLevel() + 11);
      mplew.writeLong((long)(12440000 + 6600000 * equip.getArcLevel()));
      mplew.writeLong(0L);

      for(byte i = 0; i < 12; ++i) {
         mplew.writeShort(0);
      }

   }

   public static void encodeForRemote(MaplePacketLittleEndianWriter mplew, Map<SecondaryStat, Pair<Integer, Integer>> statups, MapleCharacter chr) {
      if (statups.containsKey(SecondaryStat.Speed)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.Speed));
      }

      if (statups.containsKey(SecondaryStat.ComboCounter)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.ComboCounter));
      }

      if (statups.containsKey(SecondaryStat.BlessedHammer)) {
         mplew.writeShort(chr.getElementalCharge());
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BlessedHammer) == 0 ? 400011052 : chr.getBuffSource(SecondaryStat.BlessedHammer));
      }

      if (statups.containsKey(SecondaryStat.SnowCharge)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.SnowCharge));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SnowCharge));
      }

      if (statups.containsKey(SecondaryStat.ElementalCharge)) {
         mplew.writeShort(chr.getElementalCharge());
      }

      if (statups.containsKey(SecondaryStat.Stun)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Stun));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Stun));
      }

      if (statups.containsKey(SecondaryStat.Shock)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.Shock));
      }

      if (statups.containsKey(SecondaryStat.Darkness)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Darkness));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Darkness));
      }

      if (statups.containsKey(SecondaryStat.Seal)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Seal));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Seal));
      }

      if (statups.containsKey(SecondaryStat.Weakness)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Weakness));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Weakness));
      }

      if (statups.containsKey(SecondaryStat.WeaknessMdamage)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.WeaknessMdamage));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.WeaknessMdamage));
      }

      if (statups.containsKey(SecondaryStat.Curse)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Curse));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Curse));
      }

      if (statups.containsKey(SecondaryStat.Slow)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Slow));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Slow));
      }

      if (statups.containsKey(SecondaryStat.PvPRaceEffect)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.PvPRaceEffect));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PvPRaceEffect));
      }

      if (statups.containsKey(SecondaryStat.TimeBomb)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.TimeBomb));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.TimeBomb));
      }

      if (statups.containsKey(SecondaryStat.Team)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.Team));
      }

      if (statups.containsKey(SecondaryStat.DisOrder)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DisOrder));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DisOrder));
      }

      if (statups.containsKey(SecondaryStat.Thread)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Thread));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Thread));
      }

      if (statups.containsKey(SecondaryStat.Poison)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Poison));
      }

      if (statups.containsKey(SecondaryStat.Poison)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Poison));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Poison));
      }

      if (statups.containsKey(SecondaryStat.ShadowPartner)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ShadowPartner));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ShadowPartner));
      }

      if (statups.containsKey(SecondaryStat.Morph)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Morph));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Morph));
      }

      if (statups.containsKey(SecondaryStat.Ghost)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Ghost));
      }

      if (statups.containsKey(SecondaryStat.Attract)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Attract));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Attract));
      }

      if (statups.containsKey(SecondaryStat.Magnet)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Magnet));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Magnet));
      }

      if (statups.containsKey(SecondaryStat.MagnetArea)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.MagnetArea));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.MagnetArea));
      }

      if (statups.containsKey(SecondaryStat.NoBulletConsume)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.NoBulletConsume));
      }

      if (statups.containsKey(SecondaryStat.BanMap)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BanMap));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BanMap));
      }

      if (statups.containsKey(SecondaryStat.Barrier)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Barrier));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Barrier));
      }

      if (statups.containsKey(SecondaryStat.DojangShield)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DojangShield));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DojangShield));
      }

      if (statups.containsKey(SecondaryStat.ReverseInput)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ReverseInput));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ReverseInput));
      }

      if (statups.containsKey(SecondaryStat.RespectPImmune)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.RespectPImmune));
      }

      if (statups.containsKey(SecondaryStat.RespectMImmune)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.RespectMImmune));
      }

      if (statups.containsKey(SecondaryStat.DefenseAtt)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DefenseAtt));
      }

      if (statups.containsKey(SecondaryStat.DefenseState)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DefenseState));
      }

      if (statups.containsKey(SecondaryStat.DojangBerserk)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DojangBerserk));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DojangBerserk));
      }

      if (statups.containsKey(SecondaryStat.RepeatEffect)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.RepeatEffect));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.RepeatEffect));
      }

      if (statups.containsKey(SecondaryStat.StopPortion)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.StopPortion));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.StopPortion));
      }

      if (statups.containsKey(SecondaryStat.StopMotion)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.StopMotion));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.StopMotion));
      }

      if (statups.containsKey(SecondaryStat.Fear)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Fear));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Fear));
      }

      if (statups.containsKey(SecondaryStat.MagicShield)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.MagicShield));
      }

      if (statups.containsKey(SecondaryStat.Frozen)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Frozen));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Frozen));
      }

      if (statups.containsKey(SecondaryStat.Frozen2)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Frozen2));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Frozen2));
      }

      if (statups.containsKey(SecondaryStat.Web)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Web));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Web));
      }

      if (statups.containsKey(SecondaryStat.DrawBack)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DrawBack));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DrawBack));
      }

      if (statups.containsKey(SecondaryStat.FinalCut)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.FinalCut));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.FinalCut));
      }

      if (statups.containsKey(SecondaryStat.OnCapsule)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.OnCapsule));
      }

      if (statups.containsKey(SecondaryStat.Mechanic)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Mechanic));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Mechanic));
      }

      if (statups.containsKey(SecondaryStat.Inflation)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Inflation));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Inflation));
      }

      if (statups.containsKey(SecondaryStat.Explosion)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Explosion));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Explosion));
      }

      if (statups.containsKey(SecondaryStat.DarkTornado)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DarkTornado));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DarkTornado));
      }

      if (statups.containsKey(SecondaryStat.AmplifyDamage)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.AmplifyDamage));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.AmplifyDamage));
      }

      if (statups.containsKey(SecondaryStat.HideAttack)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.HideAttack));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.HideAttack));
      }

      if (statups.containsKey(SecondaryStat.DevilishPower)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DevilishPower));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DevilishPower));
      }

      if (statups.containsKey(SecondaryStat.SpiritLink)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.SpiritLink));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SpiritLink));
      }

      if (statups.containsKey(SecondaryStat.Event)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Event));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Event));
      }

      if (statups.containsKey(SecondaryStat.Event2)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Event2));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Event2));
      }

      if (statups.containsKey(SecondaryStat.DeathMark)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DeathMark));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DeathMark));
      }

      if (statups.containsKey(SecondaryStat.PainMark)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.PainMark));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PainMark));
      }

      if (statups.containsKey(SecondaryStat.Lapidification)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Lapidification));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Lapidification));
      }

      if (statups.containsKey(SecondaryStat.VampDeath)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.VampDeath));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.VampDeath));
      }

      if (statups.containsKey(SecondaryStat.VampDeathSummon)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.VampDeathSummon));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.VampDeathSummon));
      }

      if (statups.containsKey(SecondaryStat.VenomSnake)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.VenomSnake));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.VenomSnake));
      }

      if (statups.containsKey(SecondaryStat.PyramidEffect)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PyramidEffect));
      }

      if (statups.containsKey(SecondaryStat.PinkbeanRollingGrade)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.PinkbeanRollingGrade));
      }

      if (statups.containsKey(SecondaryStat.IgnoreTargetDEF)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.IgnoreTargetDEF));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.IgnoreTargetDEF));
      }

      if (statups.containsKey(SecondaryStat.UNK_249)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UNK_249));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UNK_249));
      }

      if (statups.containsKey(SecondaryStat.Invisible)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Invisible));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Invisible));
      }

      if (statups.containsKey(SecondaryStat.Judgement)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Judgement));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Judgement));
      }

      if (statups.containsKey(SecondaryStat.KeyDownAreaMoving)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.KeyDownAreaMoving));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.KeyDownAreaMoving));
      }

      if (statups.containsKey(SecondaryStat.StackBuff)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.StackBuff));
      }

      if (statups.containsKey(SecondaryStat.Larkness)) {
         mplew.writeShort(2);
         mplew.writeInt(chr.getBuffedValue(20040219) ? 20040219 : (chr.getBuffedValue(20040220) ? 20040220 : chr.getBuffSource(SecondaryStat.Larkness)));
      }

      if (statups.containsKey(SecondaryStat.ReshuffleSwitch)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ReshuffleSwitch));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ReshuffleSwitch));
      }

      if (statups.containsKey(SecondaryStat.SpecialAction)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.SpecialAction));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SpecialAction));
      }

      if (statups.containsKey(SecondaryStat.StopForceAtominfo)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.StopForceAtominfo));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.StopForceAtominfo));
      }

      if (statups.containsKey(SecondaryStat.SoulGazeCriDamR)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.SoulGazeCriDamR));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SoulGazeCriDamR));
      }

      if (statups.containsKey(SecondaryStat.PowerTransferGauge)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.PowerTransferGauge));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PowerTransferGauge));
      }

      if (statups.containsKey(SecondaryStat.BlitzShield)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BlitzShield));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BlitzShield));
      }

      if (statups.containsKey(SecondaryStat.AffinitySlug)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.AffinitySlug));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.AffinitySlug));
      }

      if (statups.containsKey(SecondaryStat.SoulExalt)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.SoulExalt));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SoulExalt));
      }

      if (statups.containsKey(SecondaryStat.HiddenPieceOn)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.HiddenPieceOn));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.HiddenPieceOn));
      }

      if (statups.containsKey(SecondaryStat.SmashStack)) {
         mplew.writeShort(chr.getKaiserCombo());
         mplew.writeInt(0);
      }

      if (statups.containsKey(SecondaryStat.MobZoneState)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.MobZoneState));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.MobZoneState));
      }

      if (statups.containsKey(SecondaryStat.GiveMeHeal)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.GiveMeHeal));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.GiveMeHeal));
      }

      if (statups.containsKey(SecondaryStat.TouchMe)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.TouchMe));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.TouchMe));
      }

      if (statups.containsKey(SecondaryStat.Contagion)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Contagion));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Contagion));
      }

      if (statups.containsKey(SecondaryStat.Contagion)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Contagion));
      }

      if (statups.containsKey(SecondaryStat.ComboUnlimited)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ComboUnlimited));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ComboUnlimited));
      }

      if (statups.containsKey(SecondaryStat.IgnorePCounter)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.IgnorePCounter));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.IgnorePCounter));
      }

      if (statups.containsKey(SecondaryStat.IgnoreAllCounter)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.IgnoreAllCounter));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.IgnoreAllCounter));
      }

      if (statups.containsKey(SecondaryStat.IgnorePImmune)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.IgnorePImmune));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.IgnorePImmune));
      }

      if (statups.containsKey(SecondaryStat.IgnoreAllImmune)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.IgnoreAllImmune));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.IgnoreAllImmune));
      }

      if (statups.containsKey(SecondaryStat.UnkBuffStat6)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UnkBuffStat6));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat6));
      }

      if (statups.containsKey(SecondaryStat.FireAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.FireAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.FireAura));
      }

      if (statups.containsKey(SecondaryStat.HeavensDoor)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.HeavensDoor));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.HeavensDoor));
      }

      if (statups.containsKey(SecondaryStat.DamAbsorbShield)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DamAbsorbShield));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DamAbsorbShield));
      }

      if (statups.containsKey(SecondaryStat.AntiMagicShell)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.AntiMagicShell));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.AntiMagicShell));
      }

      if (statups.containsKey(SecondaryStat.NotDamaged)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.NotDamaged));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.NotDamaged));
      }

      if (statups.containsKey(SecondaryStat.BleedingToxin)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BleedingToxin));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BleedingToxin));
      }

      if (statups.containsKey(SecondaryStat.WindBreakerFinal)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.WindBreakerFinal));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.WindBreakerFinal));
      }

      if (statups.containsKey(SecondaryStat.IgnoreMobDamR)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.IgnoreMobDamR));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.IgnoreMobDamR));
      }

      if (statups.containsKey(SecondaryStat.Asura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Asura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Asura));
      }

      if (statups.containsKey(SecondaryStat.MegaSmasher)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.MegaSmasher));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.MegaSmasher));
      }

      if (statups.containsKey(SecondaryStat.MegaSmasher)) {
         mplew.writeInt(0);
      }

      if (statups.containsKey(SecondaryStat.UnityOfPower)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UnityOfPower));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnityOfPower));
      }

      if (statups.containsKey(SecondaryStat.Stimulate)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Stimulate));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Stimulate));
      }

      if (statups.containsKey(SecondaryStat.ReturnTeleport)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.ReturnTeleport));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ReturnTeleport));
      }

      if (statups.containsKey(SecondaryStat.CapDebuff)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.CapDebuff));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.CapDebuff));
      }

      if (statups.containsKey(SecondaryStat.OverloadCount)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.OverloadCount));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.OverloadCount));
      }

      if (statups.containsKey(SecondaryStat.FireBomb)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.FireBomb));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.FireBomb));
      }

      if (statups.containsKey(SecondaryStat.SurplusSupply)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.SurplusSupply));
      }

      if (statups.containsKey(SecondaryStat.NewFlying)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.NewFlying));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.NewFlying));
      }

      if (statups.containsKey(SecondaryStat.NaviFlying)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.NaviFlying));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.NaviFlying));
      }

      if (statups.containsKey(SecondaryStat.AmaranthGenerator)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.AmaranthGenerator));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.AmaranthGenerator));
      }

      if (statups.containsKey(SecondaryStat.CygnusElementSkill)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.CygnusElementSkill));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.CygnusElementSkill));
      }

      if (statups.containsKey(SecondaryStat.StrikerHyperElectric)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.StrikerHyperElectric));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.StrikerHyperElectric));
      }

      if (statups.containsKey(SecondaryStat.EventPointAbsorb)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.EventPointAbsorb));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.EventPointAbsorb));
      }

      if (statups.containsKey(SecondaryStat.EventAssemble)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.EventAssemble));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.EventAssemble));
      }

      if (statups.containsKey(SecondaryStat.Translucence)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Translucence));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Translucence));
      }

      if (statups.containsKey(SecondaryStat.PoseType)) {
         mplew.writeShort(chr.getBuffedValue(11121012) ? 1 : chr.getBuffedSkill(SecondaryStat.PoseType));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PoseType));
      }

      if (statups.containsKey(SecondaryStat.CosmicForge)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.CosmicForge));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.CosmicForge));
      }

      if (statups.containsKey(SecondaryStat.ElementSoul)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ElementSoul));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ElementSoul));
      }

      if (statups.containsKey(SecondaryStat.GlimmeringTime)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.GlimmeringTime));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.GlimmeringTime));
      }

      if (statups.containsKey(SecondaryStat.ReincarnationFull)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ReincarnationFull));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ReincarnationFull));
      }

      if (statups.containsKey(SecondaryStat.Beholder)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Beholder));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Beholder));
      }

      if (statups.containsKey(SecondaryStat.QuiverCatridge)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.QuiverCatridge));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.QuiverCatridge));
      }

      if (statups.containsKey(SecondaryStat.ArmorPiercing)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ArmorPiercing));
      }

      if (statups.containsKey(SecondaryStat.ArmorPiercing)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ArmorPiercing));
      }

      if (statups.containsKey(SecondaryStat.FullSoulMP)) {
         mplew.writeInt(0);
         mplew.writeInt(0);
      }

      if (statups.containsKey(SecondaryStat.AntiMagicShell)) {
         mplew.write(chr.getAntiMagicShell());
         mplew.writeInt(0);
      }

      if (statups.containsKey(SecondaryStat.Dance)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.Dance));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Dance));
      }

      if (statups.containsKey(SecondaryStat.SpiritGuard)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.SpiritGuard));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SpiritGuard));
      }

      if (statups.containsKey(SecondaryStat.DemonDamageAbsorbShield)) {
         mplew.writeInt(chr.getSkillCustomValue0(400001016));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DemonDamageAbsorbShield));
      }

      if (statups.containsKey(SecondaryStat.ComboTempest)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ComboTempest));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ComboTempest));
      }

      if (statups.containsKey(SecondaryStat.HalfstatByDebuff)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.HalfstatByDebuff));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.HalfstatByDebuff));
      }

      if (statups.containsKey(SecondaryStat.ComplusionSlant)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ComplusionSlant));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ComplusionSlant));
      }

      if (statups.containsKey(SecondaryStat.JaguarSummoned)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.JaguarSummoned));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.JaguarSummoned));
      }

      if (statups.containsKey(SecondaryStat.BombTime)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BombTime));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BombTime));
      }

      if (statups.containsKey(SecondaryStat.Transform)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Transform));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Transform));
      }

      if (statups.containsKey(SecondaryStat.EnergyBurst)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.EnergyBurst));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.EnergyBurst));
      }

      if (statups.containsKey(SecondaryStat.Striker1st)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Striker1st));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Striker1st));
      }

      if (statups.containsKey(SecondaryStat.BulletParty)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BulletParty));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BulletParty));
      }

      if (statups.containsKey(SecondaryStat.SelectDice)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.SelectDice));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SelectDice));
      }

      if (statups.containsKey(SecondaryStat.Pray)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Pray));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Pray));
      }

      if (statups.containsKey(SecondaryStat.DarkLighting)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DarkLighting));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DarkLighting));
      }

      if (statups.containsKey(SecondaryStat.AttackCountX)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.AttackCountX));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.AttackCountX));
      }

      if (statups.containsKey(SecondaryStat.FireBarrier)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.FireBarrier));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.FireBarrier));
      }

      if (statups.containsKey(SecondaryStat.KeyDownMoving)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.KeyDownMoving));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.KeyDownMoving));
      }

      if (statups.containsKey(SecondaryStat.MichaelSoulLink)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.MichaelSoulLink));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.MichaelSoulLink));
      }

      if (statups.containsKey(SecondaryStat.KinesisPsychicEnergeShield)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.KinesisPsychicEnergeShield));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.KinesisPsychicEnergeShield));
      }

      if (statups.containsKey(SecondaryStat.BladeStance)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BladeStance));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BladeStance));
      }

      if (statups.containsKey(SecondaryStat.BladeStance)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BladeStance));
      }

      if (statups.containsKey(SecondaryStat.Fever)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Fever));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Fever));
      }

      if (statups.containsKey(SecondaryStat.AdrenalinBoost)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.AdrenalinBoost));
      }

      if (statups.containsKey(SecondaryStat.RwBarrier)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.RwBarrier));
      }

      if (statups.containsKey(SecondaryStat.RWUnk)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.RWUnk));
      }

      if (statups.containsKey(SecondaryStat.RwMagnumBlow)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.RwMagnumBlow));
      }

      if (statups.containsKey(SecondaryStat.SerpentScrew)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SerpentScrew));
      }

      if (statups.containsKey(SecondaryStat.Cosmos)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Cosmos));
      }

      if (statups.containsKey(SecondaryStat.GuidedArrow)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.GuidedArrow));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.GuidedArrow));
      }

      if (statups.containsKey(SecondaryStat.UnkBuffStat4)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UnkBuffStat4));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat4));
      }

      if (statups.containsKey(SecondaryStat.BlessMark)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BlessMark));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BlessMark));
      }

      if (statups.containsKey(SecondaryStat.BonusAttack)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BonusAttack));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BonusAttack));
      }

      if (statups.containsKey(SecondaryStat.UnkBuffStat5)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UnkBuffStat5));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat5));
      }

      if (statups.containsKey(SecondaryStat.Stigma)) {
         mplew.writeShort(chr.Stigma);
         mplew.writeInt(7);
      }

      if (statups.containsKey(SecondaryStat.HolyUnity)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.HolyUnity));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.HolyUnity));
      }

      if (statups.containsKey(SecondaryStat.RhoAias)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.RhoAias));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.RhoAias));
      }

      if (statups.containsKey(SecondaryStat.PsychicTornado)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PsychicTornado));
      }

      if (statups.containsKey(SecondaryStat.InstallMaha)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.InstallMaha));
      }

      if (statups.containsKey(SecondaryStat.OverloadMana)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.OverloadMana));
      }

      if (statups.containsKey(SecondaryStat.TrueSniping)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.TrueSniping));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.TrueSniping));
      }

      if (statups.containsKey(SecondaryStat.KawoongDebuff)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.KawoongDebuff));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.KawoongDebuff));
      }

      if (statups.containsKey(SecondaryStat.Spotlight)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Spotlight));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Spotlight));
      }

      if (statups.containsKey(SecondaryStat.Overload)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Overload));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Overload));
      }

      if (statups.containsKey(SecondaryStat.FreudsProtection)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.FreudsProtection));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.FreudsProtection));
      }

      if (statups.containsKey(SecondaryStat.BlessedHammer2)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BlessedHammer2));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BlessedHammer2));
      }

      if (statups.containsKey(SecondaryStat.OverDrive)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.OverDrive));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.OverDrive));
      }

      if (statups.containsKey(SecondaryStat.Etherealform)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Etherealform));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Etherealform));
      }

      if (statups.containsKey(SecondaryStat.ReadyToDie)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ReadyToDie));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ReadyToDie));
      }

      if (statups.containsKey(SecondaryStat.Oblivion)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Oblivion));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Oblivion));
      }

      if (statups.containsKey(SecondaryStat.CriticalReinForce)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.CriticalReinForce));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.CriticalReinForce));
      }

      if (statups.containsKey(SecondaryStat.CurseOfCreation)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.CurseOfCreation));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.CurseOfCreation));
      }

      if (statups.containsKey(SecondaryStat.CurseOfDestruction)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.CurseOfDestruction));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.CurseOfDestruction));
      }

      if (statups.containsKey(SecondaryStat.BlackMageDebuff)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BlackMageDebuff));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BlackMageDebuff));
      }

      if (statups.containsKey(SecondaryStat.BodyOfSteal)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BodyOfSteal));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BodyOfSteal));
      }

      if (statups.containsKey(SecondaryStat.GloryWing)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.GloryWing));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.GloryWing));
      }

      if (statups.containsKey(SecondaryStat.PapulCuss)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.PapulCuss));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PapulCuss));
      }

      if (statups.containsKey(SecondaryStat.PapulCuss)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PapulCuss));
      }

      if (statups.containsKey(SecondaryStat.PapulBomb)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.PapulBomb));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PapulBomb));
      }

      if (statups.containsKey(SecondaryStat.HarmonyLink)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.HarmonyLink));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.HarmonyLink));
      }

      if (statups.containsKey(SecondaryStat.FastCharge)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.FastCharge));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.FastCharge));
      }

      if (statups.containsKey(SecondaryStat.SpectorTransForm)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.SpectorTransForm));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SpectorTransForm));
      }

      if (statups.containsKey(SecondaryStat.ComingDeath)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ComingDeath));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ComingDeath));
      }

      if (statups.containsKey(SecondaryStat.WillPoison)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.WillPoison));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.WillPoison));
      }

      if (statups.containsKey(SecondaryStat.PapulBomb)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PapulBomb));
      }

      if (statups.containsKey(SecondaryStat.GrandCrossSize)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.GrandCrossSize));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.GrandCrossSize));
      }

      if (statups.containsKey(SecondaryStat.Protective)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Protective));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Protective));
      }

      if (statups.containsKey(SecondaryStat.UNK_580)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UNK_580));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UNK_580));
      }

      if (statups.containsKey(SecondaryStat.BattlePvP_Wonky_Awesome)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BattlePvP_Wonky_Awesome));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BattlePvP_Wonky_Awesome));
      }

      if (statups.containsKey(SecondaryStat.UnkBuffStat42)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UnkBuffStat42));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat42));
      }

      if (statups.containsKey(SecondaryStat.UnkBuffStat44)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UnkBuffStat44));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat44));
      }

      if (statups.containsKey(SecondaryStat.UNK_585)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UNK_585));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UNK_585));
      }

      if (statups.containsKey(SecondaryStat.UNK_586)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UNK_586));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UNK_586));
      }

      if (statups.containsKey(SecondaryStat.PinkBeanMatroCyca)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.PinkBeanMatroCyca));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PinkBeanMatroCyca));
      }

      if (statups.containsKey(SecondaryStat.UnkBuffStat50)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UnkBuffStat50));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat50));
      }

      if (statups.containsKey(SecondaryStat.AltergoReinforce)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.AltergoReinforce));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.AltergoReinforce));
      }

      if (statups.containsKey(SecondaryStat.YalBuff)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.YalBuff));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.YalBuff));
      }

      if (statups.containsKey(SecondaryStat.IonBuff)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.IonBuff));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.IonBuff));
      }

      if (statups.containsKey(SecondaryStat.UnkBuffStat53)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UnkBuffStat53));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat53));
      }

      if (statups.containsKey(SecondaryStat.Graffiti)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Graffiti));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Graffiti));
      }

      if (statups.containsKey(SecondaryStat.Novility)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Novility));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Novility));
      }

      if (statups.containsKey(SecondaryStat.RuneOfPure)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.RuneOfPure));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.RuneOfPure));
      }

      if (statups.containsKey(SecondaryStat.RuneOfTransition)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.RuneOfTransition));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.RuneOfTransition));
      }

      if (statups.containsKey(SecondaryStat.DuskDarkness)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DuskDarkness));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DuskDarkness));
      }

      if (statups.containsKey(SecondaryStat.YellowAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.YellowAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.YellowAura));
      }

      if (statups.containsKey(SecondaryStat.DrainAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DrainAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DrainAura));
      }

      if (statups.containsKey(SecondaryStat.BlueAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BlueAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BlueAura));
      }

      if (statups.containsKey(SecondaryStat.DarkAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DarkAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DarkAura));
      }

      if (statups.containsKey(SecondaryStat.DebuffAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DebuffAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DebuffAura));
      }

      if (statups.containsKey(SecondaryStat.UnionAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UnionAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnionAura));
      }

      if (statups.containsKey(SecondaryStat.IceAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.IceAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.IceAura));
      }

      if (statups.containsKey(SecondaryStat.KnightsAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.KnightsAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.KnightsAura));
      }

      if (statups.containsKey(SecondaryStat.ZeroAuraStr)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ZeroAuraStr));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ZeroAuraStr));
      }

      if (statups.containsKey(SecondaryStat.IncarnationAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.IncarnationAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.IncarnationAura));
      }

      if (statups.containsKey(SecondaryStat.BlizzardTempest)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.BlizzardTempest));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BlizzardTempest));
      }

      if (statups.containsKey(SecondaryStat.PhotonRay)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.PhotonRay));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.PhotonRay));
      }

      if (statups.containsKey(SecondaryStat.DarknessAura)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DarknessAura));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DarknessAura));
      }

      if (statups.containsKey(SecondaryStat.SilhouetteMirage)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.SilhouetteMirage));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.SilhouetteMirage));
      }

      if (statups.containsKey(SecondaryStat.LiberationOrbActive)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.LiberationOrbActive));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.LiberationOrbActive));
      }

      if (statups.containsKey(SecondaryStat.ThanatosDescent)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ThanatosDescent));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ThanatosDescent));
      }

      if (statups.containsKey(SecondaryStat.UNK_672)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UNK_672));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UNK_672));
      }

      if (statups.containsKey(SecondaryStat.YetiAngerMode)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.YetiAngerMode));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.YetiAngerMode));
      }

      if (statups.containsKey(SecondaryStat.흡수_강)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.흡수_강));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.흡수_강));
      }

      if (statups.containsKey(SecondaryStat.흡수_바람)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.흡수_바람));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.흡수_바람));
      }

      if (statups.containsKey(SecondaryStat.흡수_해)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.흡수_해));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.흡수_해));
      }

      if (statups.containsKey(SecondaryStat.UNK_696)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UNK_696));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UNK_696));
      }

      if (statups.containsKey(SecondaryStat.UNK_698)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UNK_698));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UNK_698));
      }

      if (statups.containsKey(SecondaryStat.FlashMirage)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.FlashMirage));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.FlashMirage));
      }

      if (statups.containsKey(SecondaryStat.HolyBlood)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.HolyBlood));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.HolyBlood));
      }

      if (statups.containsKey(SecondaryStat.Infinity)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.Infinity));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.Infinity));
      }

      if (statups.containsKey(SecondaryStat.TeleportMastery)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.TeleportMastery));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.TeleportMastery));
      }

      if (statups.containsKey(SecondaryStat.ChillingStep)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ChillingStep));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.ChillingStep));
      }

      if (statups.containsKey(SecondaryStat.BlessingArmor)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BlessingArmor));
      }

      if (statups.containsKey(SecondaryStat.UNK_705)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UNK_705));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UNK_705));
      }

      if (statups.containsKey(SecondaryStat.UNK_706)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.UNK_706));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UNK_706));
      }

      if (statups.containsKey(SecondaryStat.DemonFrenzy)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.DemonFrenzy));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.DemonFrenzy));
      }

      if (statups.containsKey(SecondaryStat.CrystalGate)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.CrystalGate));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.CrystalGate));
      }

      if (statups.containsKey(SecondaryStat.KinesisPsychicPoint)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.KinesisPsychicPoint));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.KinesisPsychicPoint));
      }

      mplew.write(statups.containsKey(SecondaryStat.DefenseAtt));
      mplew.write(statups.containsKey(SecondaryStat.DefenseState));
      mplew.write(statups.containsKey(SecondaryStat.PVPDamage));
      mplew.writeInt(chr.energyCharge && chr.getKeyValue(1544, "5100015") <= 0L ? 5120018 : 0);
      if (statups.containsKey(SecondaryStat.CurseOfCreation)) {
         mplew.writeInt(chr.getDisease(SecondaryStat.CurseOfCreation).getValue());
      }

      if (statups.containsKey(SecondaryStat.CurseOfDestruction)) {
         mplew.writeInt(chr.getDisease(SecondaryStat.CurseOfDestruction).getValue());
      }

      if (statups.containsKey(SecondaryStat.PoseType)) {
         mplew.write((int)0);
         mplew.write((int)1);
      }

      if (statups.containsKey(SecondaryStat.BattlePvP_Helena_Mark)) {
         mplew.writeInt(10);
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BattlePvP_Helena_Mark));
         mplew.writeInt(chr.getId());
      }

      if (statups.containsKey(SecondaryStat.BattlePvP_LangE_Protection)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.BattlePvP_LangE_Protection));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BattlePvP_LangE_Protection));
      }

      if (statups.containsKey(SecondaryStat.MichaelSoulLink)) {
         boolean isParty = chr.getParty() != null;
         int size = 0;
         if (isParty) {
            Iterator var5 = chr.getParty().getMembers().iterator();

            while(var5.hasNext()) {
               MaplePartyCharacter chr1 = (MaplePartyCharacter)var5.next();
               MapleCharacter chr2 = chr.getMap().getCharacter(chr1.getId());
               if (chr1.isOnline() && chr2.getBuffedValue(51111008)) {
                  ++size;
               }
            }
         }

         mplew.writeInt(isParty ? chr.getParty().getMembers().size() : 1);
         mplew.write(size >= 2 ? 0 : 1);
         mplew.writeInt(isParty ? chr.getParty().getId() : 0);
         mplew.writeInt(0);
      }

      if (statups.containsKey(SecondaryStat.AdrenalinBoost)) {
         mplew.write(chr.getBuffedSkill(SecondaryStat.AdrenalinBoost));
      }

      if (statups.containsKey(SecondaryStat.Stigma)) {
         mplew.writeInt(7);
      }

      if (statups.containsKey(SecondaryStat.HolyUnity)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.HolyUnity));
      }

      if (statups.containsKey(SecondaryStat.DemonFrenzy)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.DemonFrenzy));
      }

      if (statups.containsKey(SecondaryStat.ShadowSpear)) {
         mplew.writeShort(chr.getBuffedSkill(SecondaryStat.ShadowSpear));
      }

      if (statups.containsKey(SecondaryStat.RhoAias)) {
         mplew.writeInt(chr.getBuffedOwner(400011011) == chr.getId() ? chr.getId() : chr.getBuffedOwner(400011011));
         SecondaryStatEffect effect = chr.getBuffedEffect(SecondaryStat.RhoAias);
         if (effect != null) {
            if (chr.getRhoAias() <= effect.getY()) {
               mplew.writeInt(3);
            } else if (chr.getRhoAias() <= effect.getY() + effect.getW()) {
               mplew.writeInt(2);
            } else {
               mplew.writeInt(1);
            }

            mplew.writeInt(chr.getRhoAias());
            if (chr.getRhoAias() <= effect.getY()) {
               mplew.writeInt(3);
            } else if (chr.getRhoAias() <= effect.getY() + effect.getW()) {
               mplew.writeInt(2);
            } else {
               mplew.writeInt(1);
            }
         } else {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
         }
      }

      if (statups.containsKey(SecondaryStat.VampDeath)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.VampDeath));
      }

      if (statups.containsKey(SecondaryStat.GloryWing)) {
         mplew.writeInt(chr.canUseMortalWingBeat ? 1 : 0);
         mplew.writeInt(1);
      }

      if (statups.containsKey(SecondaryStat.BlessMark)) {
         mplew.writeInt(chr.getBuffedSkill(SecondaryStat.BlessMark));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BlessMark));
      }

      if (statups.containsKey(SecondaryStat.BattlePvP_Rude_Stack)) {
         mplew.writeInt(80002338);
         mplew.writeInt(80002338);
         mplew.writeInt(80002338);
      }

      if (statups.containsKey(SecondaryStat.UnkBuffStat35)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat35));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat35));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat35));
      }

      if (statups.containsKey(SecondaryStat.BattlePvP_Wonky_ChargeA)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BattlePvP_Wonky_ChargeA));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BattlePvP_Wonky_ChargeA));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BattlePvP_Wonky_ChargeA));
      }

      if (statups.containsKey(SecondaryStat.BattlePvP_Wonky_Awesome)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.BattlePvP_Wonky_Awesome));
      }

      if (statups.containsKey(SecondaryStat.StopForceAtominfo)) {
         mplew.writeInt(chr.getBuffSource(SecondaryStat.StopForceAtominfo) == 61121217 ? 4 : (chr.getBuffSource(SecondaryStat.StopForceAtominfo) == 61110211 ? 3 : (chr.getBuffSource(SecondaryStat.StopForceAtominfo) != 61101002 && chr.getBuffSource(SecondaryStat.StopForceAtominfo) != 61110211 ? 2 : 1)));
         mplew.writeInt(chr.getBuffSource(SecondaryStat.StopForceAtominfo) != 61101002 && chr.getBuffSource(SecondaryStat.StopForceAtominfo) != 61110211 ? 5 : 3);
         mplew.writeInt(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11) == null ? 0 : chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId());
         mplew.writeInt(chr.getBuffSource(SecondaryStat.StopForceAtominfo) != 61101002 && chr.getBuffSource(SecondaryStat.StopForceAtominfo) != 61110211 ? 5 : 3);
         mplew.writeZeroBytes(chr.getBuffSource(SecondaryStat.StopForceAtominfo) != 61101002 && chr.getBuffSource(SecondaryStat.StopForceAtominfo) != 61110211 ? 20 : 12);
      } else {
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
      }

      Iterator var9 = statups.entrySet().iterator();

      while(true) {
         while(true) {
            Entry stat;
            do {
               do {
                  if (!var9.hasNext()) {
                     List<Pair<SecondaryStat, Pair<Integer, Integer>>> newstatups = sortBuffStats(statups);
                     CWvsContext.BuffPacket.encodeIndieTempStat(mplew, newstatups, chr);
                     if (statups.containsKey(SecondaryStat.KawoongDebuff)) {
                        mplew.writeInt(chr.getBuffSource(SecondaryStat.KawoongDebuff));
                     }

                     if (statups.containsKey(SecondaryStat.KeyDownMoving)) {
                        mplew.writeInt(chr.getBuffedSkill(SecondaryStat.KeyDownMoving));
                     }

                     if (statups.containsKey(SecondaryStat.WillPoison)) {
                        mplew.writeInt(100);
                     }

                     if (statups.containsKey(SecondaryStat.ComboCounter)) {
                        mplew.writeInt(chr.getKeyValue(1548, "버프이펙트") == 1L ? 1 : 0);
                        mplew.writeInt(0);
                     }

                     if (statups.containsKey(SecondaryStat.UnkBuffStat50)) {
                        mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat50));
                        mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat50));
                        mplew.writeInt(chr.getBuffSource(SecondaryStat.UnkBuffStat50));
                     }

                     if (statups.containsKey(SecondaryStat.Graffiti)) {
                        mplew.writeInt(chr.getBuffSource(SecondaryStat.Graffiti));
                     }

                     mplew.write((int)0);
                     if (statups.containsKey(SecondaryStat.Novility)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(0);
                     }

                     if (statups.containsKey(SecondaryStat.YellowAura)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                     }

                     if (statups.containsKey(SecondaryStat.DrainAura)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                     }

                     if (statups.containsKey(SecondaryStat.BlueAura)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                        mplew.writeInt(0);
                     }

                     if (statups.containsKey(SecondaryStat.DarkAura)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                     }

                     if (statups.containsKey(SecondaryStat.DebuffAura)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                     }

                     if (statups.containsKey(SecondaryStat.UnionAura)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                     }

                     if (statups.containsKey(SecondaryStat.IceAura)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                     }

                     if (statups.containsKey(SecondaryStat.KnightsAura)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                     }

                     if (statups.containsKey(SecondaryStat.ZeroAuraStr)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                     }

                     if (statups.containsKey(SecondaryStat.IncarnationAura)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(1);
                     }

                     if (statups.containsKey(SecondaryStat.BlizzardTempest)) {
                        mplew.writeInt(chr.getId());
                        mplew.writeInt(0);
                     }

                     if (statups.containsKey(SecondaryStat.PhotonRay)) {
                        mplew.writeInt(chr.photonRay);
                     }

                     if (statups.containsKey(SecondaryStat.SilhouetteMirage)) {
                        mplew.writeInt(chr.getSkillCustomValue0(400031053));
                     }

                     if (statups.containsKey(SecondaryStat.Infinity)) {
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                     }

                     if (statups.containsKey(SecondaryStat.YetiAngerMode)) {
                        mplew.writeInt(chr.getBuffSource(SecondaryStat.YetiAngerMode));
                     }

                     return;
                  }

                  stat = (Entry)var9.next();
               } while(((SecondaryStat)stat.getKey()).canStack());
            } while(!((SecondaryStat)stat.getKey()).isSpecialBuff());

            mplew.writeInt((Integer)((Pair)stat.getValue()).left);
            mplew.writeInt(chr.getBuffSource((SecondaryStat)stat.getKey()));
            if (stat.getKey() == SecondaryStat.PartyBooster) {
               mplew.write((int)1);
               mplew.writeInt(chr.getBuffedEffect((SecondaryStat)stat.getKey()).getStarttime());
            } else if (stat.getKey() == SecondaryStat.EnergyCharged) {
               mplew.write(chr.energyCharge);
            }

            mplew.write((int)0);
            mplew.writeInt(0);
            if (stat.getKey() == SecondaryStat.GuidedBullet) {
               mplew.writeInt(chr.guidedBullet);
               mplew.writeInt(0);
            } else if (stat.getKey() != SecondaryStat.RideVehicleExpire && stat.getKey() != SecondaryStat.PartyBooster && stat.getKey() != SecondaryStat.DashJump && stat.getKey() != SecondaryStat.DashSpeed) {
               if (stat.getKey() == SecondaryStat.Grave) {
                  mplew.writeInt(chr.graveObjectId);
                  mplew.writeInt(0);
               }
            } else {
               mplew.writeShort((Integer)((Pair)stat.getValue()).right / 1000);
            }
         }
      }
   }

   public static void ChatPacket(MaplePacketLittleEndianWriter mplew, String name, String chat) {
      mplew.writeMapleAsciiString(name);
      mplew.writeMapleAsciiString(chat);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write((int)1);
      mplew.writeInt(0);
   }

   public static void chairPacket(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int itemId) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (ii.getItemInformation(itemId) == null) {
         System.out.println(itemId + " null chair Packet.");
      } else {
         String chairType = ii.getItemInformation(itemId).chairType;
         byte var7 = -1;
         switch(chairType.hashCode()) {
         case -1914932375:
            if (chairType.equals("worldLvChair")) {
               var7 = 24;
            }
            break;
         case -1852370943:
            if (chairType.equals("trickOrTreatChair")) {
               var7 = 3;
            }
            break;
         case -1836353630:
            if (chairType.equals("popButtonChair")) {
               var7 = 8;
            }
            break;
         case -1832216437:
            if (chairType.equals("mannequinChair")) {
               var7 = 11;
            }
            break;
         case -1770259898:
            if (chairType.equals("mirrorChair")) {
               var7 = 7;
            }
            break;
         case -1260300310:
            if (chairType.equals("unionRankChair")) {
               var7 = 22;
            }
            break;
         case -1103185536:
            if (chairType.equals("arcaneForceChair")) {
               var7 = 18;
            }
            break;
         case -1081097020:
            if (chairType.equals("yetiChair")) {
               var7 = 23;
            }
            break;
         case -1076280221:
            if (chairType.equals("worldLvChairNonshowLevel")) {
               var7 = 26;
            }
            break;
         case -1017881194:
            if (chairType.equals("androidChair")) {
               var7 = 10;
            }
            break;
         case -1005488347:
            if (chairType.equals("charLvChair")) {
               var7 = 16;
            }
            break;
         case -884022974:
            if (chairType.equals("scaleAvatarChair")) {
               var7 = 19;
            }
            break;
         case -725777470:
            if (chairType.equals("randomChair")) {
               var7 = 5;
            }
            break;
         case -503248982:
            if (chairType.equals("rollingHouseChair")) {
               var7 = 9;
            }
            break;
         case -397329532:
            if (chairType.equals("2019rollingHouseChair")) {
               var7 = 21;
            }
            break;
         case -396996366:
            if (chairType.equals("atkPwrChair")) {
               var7 = 25;
            }
            break;
         case -278282511:
            if (chairType.equals("rotatedSleepingBagChair")) {
               var7 = 12;
            }
            break;
         case 10439672:
            if (chairType.equals("timeChair")) {
               var7 = 0;
            }
            break;
         case 247858123:
            if (chairType.equals("wasteChair")) {
               var7 = 20;
            }
            break;
         case 455872742:
            if (chairType.equals("petChair")) {
               var7 = 15;
            }
            break;
         case 536296615:
            if (chairType.equals("identityChair")) {
               var7 = 6;
            }
            break;
         case 626458356:
            if (chairType.equals("popChair")) {
               var7 = 1;
            }
            break;
         case 808378703:
            if (chairType.equals("eventPointChair")) {
               var7 = 13;
            }
            break;
         case 1064442508:
            if (chairType.equals("starForceChair")) {
               var7 = 2;
            }
            break;
         case 1414966430:
            if (chairType.equals("celebChair")) {
               var7 = 4;
            }
            break;
         case 1482755705:
            if (chairType.equals("hashTagChair")) {
               var7 = 14;
            }
            break;
         case 1557656403:
            if (chairType.equals("scoreChair")) {
               var7 = 17;
            }
         }

         int i;
         switch(var7) {
         case 0:
            mplew.writeInt(0);
            break;
         case 1:
            mplew.writeInt(0);
         case 2:
         case 5:
         case 7:
         case 10:
         case 12:
         case 13:
         case 18:
            break;
         case 3:
            mplew.writeInt(0);
            mplew.writeInt(0);
            break;
         case 4:
            mplew.writeInt(0);
            break;
         case 6:
            mplew.write(true);
            mplew.writeLong(getTime(System.currentTimeMillis()));
            break;
         case 8:
            mplew.writeInt(chr.getFame());
            break;
         case 9:
            mplew.writeInt(itemId);
            mplew.writeInt(0);
            break;
         case 11:
            mplew.writeInt(chr.getHairRoom().size());
            Iterator var12 = chr.getHairRoom().iterator();

            while(var12.hasNext()) {
               MapleMannequin hr = (MapleMannequin)var12.next();
               mplew.write((int)0);
               mplew.write((int)0);
               mplew.writeInt(hr.getValue());
               mplew.write(hr.getBaseColor());
               mplew.write(hr.getAddColor());
               mplew.write(hr.getBaseProb());
            }

            return;
         case 14:
            for(i = 0; i < 18; ++i) {
               mplew.writeMapleAsciiString("");
            }

            return;
         case 15:
            for(i = 0; i < 3; ++i) {
               MaplePet pet = chr.getPet((long)i);
               if (pet != null) {
                  mplew.writeInt(pet.getPetItemId());
                  mplew.writeInt(pet.getPos().x);
                  mplew.writeInt(pet.getPos().y);
               } else {
                  mplew.writeInt(0);
                  mplew.writeInt(0);
                  mplew.writeInt(0);
               }
            }

            return;
         case 16:
            mplew.writeInt(chr.getLevel());
            break;
         case 17:
            mplew.writeInt(0);
            break;
         case 19:
            mplew.write(itemId == 3018465);
            if (itemId == 3018465) {
               mplew.writeInt(chr.getKeyValue(100466, "Floor"));
            }
            break;
         case 20:
            mplew.writeLong((long)chr.getMesoChairCount());
            break;
         case 21:
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            break;
         case 22:
            mplew.write(false);
            break;
         case 23:
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            break;
         case 24:
         case 25:
         case 26:
            mplew.write(false);
            break;
         default:
            if (GameConstants.isTextChair(itemId)) {
               mplew.writeMapleAsciiString(chr.getChairText());
               ChatPacket(mplew, chr.getName(), "[의자]" + chr.getChairText());
            } else if (GameConstants.isTowerChair(itemId)) {
               String towerchair = chr.getInfoQuest(7266);
               if (towerchair.equals("")) {
                  mplew.writeInt(0);
               } else {
                  String[] temp = towerchair.split(";");
                  mplew.writeInt(temp.length);

                  for(int a = 0; a < temp.length; ++a) {
                     int chairid = Integer.parseInt(temp[a].substring(2));
                     mplew.writeInt(chairid);
                  }
               }
            } else if (itemId == 3015440 || itemId == 3015650 || itemId == 3015651 || itemId == 3015897 || itemId == 3018430 | itemId == 3018450) {
               mplew.writeLong((long)chr.getMesoChairCount());
            }
         }

      }
   }
}
