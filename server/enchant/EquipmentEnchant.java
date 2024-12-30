package server.enchant;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleWeaponType;
import constants.GameConstants;
import constants.ServerConstants;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class EquipmentEnchant {
   public static int[] usejuhun = new int[4];

   public static int scrollType(String name) {
      if (name.contains("100%")) {
         return 0;
      } else if (name.contains("이노센트")) {
         return 4;
      } else if (name.contains("순백")) {
         return 5;
      } else if (name.contains("70%")) {
         return 1;
      } else if (name.contains("30%")) {
         return 2;
      } else {
         return name.contains("15%") ? 3 : 0;
      }
   }

   public static boolean isMagicWeapon(MapleWeaponType type) {
      switch(type) {
      case ESPLIMITER:
      case STAFF:
      case WAND:
      case PLANE:
      case MAGICGUNTLET:
         return true;
      default:
         return false;
      }
   }

   public static void checkEquipmentStats(MapleClient c, Equip equip) {
      boolean changed = false;
      if (equip != null) {
         Equip item = (Equip)MapleItemInformationProvider.getInstance().getEquipById(equip.getItemId(), false);
         if (MapleItemInformationProvider.getInstance().getName(equip.getItemId()).startsWith("제네시스")) {
            item.setEnchantWatk((short)0);
            item.setEnchantMatk((short)0);
            item.setEnchantStr((short)0);
            item.setEnchantDex((short)0);
            item.setEnchantInt((short)0);
            item.setEnchantLuk((short)0);
            item.setEnchantWdef((short)0);
            item.setEnchantMdef((short)0);
            item.setEnchantHp((short)0);
            item.setEnchantMp((short)0);
            item.setEnchantAcc((short)0);
            item.setEnchantAvoid((short)0);
         } else if ((equip.getEquipmentType() & 5888) != 5888) {
            item.setStr(equip.getStr());
            item.setDex(equip.getDex());
            item.setInt(equip.getInt());
            item.setLuk(equip.getLuk());
            item.setHp(equip.getHp());
            item.setMp(equip.getMp());
            item.setWatk(equip.getWatk());
            item.setMatk(equip.getMatk());
            item.setWdef(equip.getWdef());
            item.setMdef(equip.getMdef());
            item.setAcc(equip.getAcc());
            item.setAvoid(equip.getAvoid());
            item.setFire(equip.getFire());
            byte max = equip.getEnhance();

            while(item.getEnhance() < max) {
               StarForceStats statz = starForceStats(item);
               item.setEnchantBuff((short)0);
               item.setEnhance((byte)(item.getEnhance() + 1));
               Iterator var6 = statz.getStats().iterator();

               while(var6.hasNext()) {
                  Pair<EnchantFlag, Integer> stat = (Pair)var6.next();
                  if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantWatk((short)(item.getEnchantWatk() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantMatk((short)(item.getEnchantMatk() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantStr((short)(item.getEnchantStr() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantDex((short)(item.getEnchantDex() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantInt((short)(item.getEnchantInt() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantLuk((short)(item.getEnchantLuk() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantWdef((short)(item.getEnchantWdef() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantMdef((short)(item.getEnchantMdef() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantHp((short)(item.getEnchantHp() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantMp((short)(item.getEnchantMp() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantAcc((short)(item.getEnchantAcc() + (Integer)stat.right));
                  }

                  if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantAvoid((short)(item.getEnchantAvoid() + (Integer)stat.right));
                  }
               }
            }

            if (equip.getEnchantStr() != item.getEnchantStr()) {
               changed = true;
               equip.setEnchantStr(item.getEnchantStr());
            }

            if (equip.getEnchantDex() != item.getEnchantDex()) {
               changed = true;
               equip.setEnchantDex(item.getEnchantDex());
            }

            if (equip.getEnchantInt() != item.getEnchantInt()) {
               changed = true;
               equip.setEnchantInt(item.getEnchantInt());
            }

            if (equip.getEnchantLuk() != item.getEnchantLuk()) {
               changed = true;
               equip.setEnchantLuk(item.getEnchantLuk());
            }

            if (equip.getEnchantHp() != item.getEnchantHp()) {
               changed = true;
               equip.setEnchantHp(item.getEnchantHp());
            }

            if (equip.getEnchantMp() != item.getEnchantMp()) {
               changed = true;
               equip.setEnchantMp(item.getEnchantMp());
            }

            if (equip.getEnchantWatk() != item.getEnchantWatk()) {
               changed = true;
               equip.setEnchantWatk(item.getEnchantWatk());
            }

            if (equip.getEnchantMatk() != item.getEnchantMatk()) {
               changed = true;
               equip.setEnchantMatk(item.getEnchantMatk());
            }

            if (equip.getEnchantWdef() != item.getEnchantWdef()) {
               changed = true;
               equip.setEnchantWdef(item.getEnchantWdef());
            }

            if (equip.getEnchantMdef() != item.getEnchantMdef()) {
               changed = true;
               equip.setEnchantMdef(item.getEnchantMdef());
            }

            if (equip.getEnchantAcc() != item.getEnchantAcc()) {
               changed = true;
               equip.setEnchantAcc(item.getEnchantAcc());
            }

            if (equip.getEnchantAvoid() != item.getEnchantAvoid()) {
               changed = true;
               equip.setEnchantAvoid(item.getEnchantAvoid());
            }

            if (changed && c != null) {
            }

         }
      }
   }

   public static List<EquipmentScroll> equipmentScrolls(Equip equip) {
      List<EquipmentScroll> ess = new ArrayList();
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      int reqLevel = ii.getReqLevel(equip.getItemId());
      MapleWeaponType weaponType = GameConstants.getWeaponType(equip.getItemId());
      List<Pair<EnchantFlag, Integer>> stats = new ArrayList();
      setJuhun(reqLevel, GameConstants.isWeapon(equip.getItemId()));
      if (equip.getUpgradeSlots() > 0) {
         if (GameConstants.isWeapon(equip.getItemId())) {
            if (isMagicWeapon(weaponType)) {
               if (reqLevel < 80) {
                  stats.add(new Pair(EnchantFlag.Matk, 1));
               } else if (reqLevel < 120) {
                  stats.add(new Pair(EnchantFlag.Matk, 2));
               } else {
                  stats.add(new Pair(EnchantFlag.Matk, 3));
               }

               ess.add(new EquipmentScroll("100% 마력 주문서", usejuhun[0], stats));
               stats.clear();
               if (reqLevel < 80) {
                  stats.add(new Pair(EnchantFlag.Matk, 2));
                  ess.add(new EquipmentScroll("70% 마력 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Matk, 3));
                  stats.add(new Pair(EnchantFlag.Int, 1));
                  ess.add(new EquipmentScroll("30% 마력(지력) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Matk, 5));
                  stats.add(new Pair(EnchantFlag.Int, 2));
                  ess.add(new EquipmentScroll("15% 마력(지력) 주문서", usejuhun[3], stats));
                  stats.clear();
               } else if (reqLevel < 120) {
                  stats.add(new Pair(EnchantFlag.Matk, 3));
                  stats.add(new Pair(EnchantFlag.Int, 1));
                  ess.add(new EquipmentScroll("70% 마력(지력) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Matk, 5));
                  stats.add(new Pair(EnchantFlag.Int, 2));
                  ess.add(new EquipmentScroll("30% 마력(지력) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Matk, 7));
                  stats.add(new Pair(EnchantFlag.Int, 3));
                  ess.add(new EquipmentScroll("15% 마력(지력) 주문서", usejuhun[3], stats));
                  stats.clear();
               } else {
                  stats.add(new Pair(EnchantFlag.Matk, 5));
                  stats.add(new Pair(EnchantFlag.Int, 2));
                  ess.add(new EquipmentScroll("70% 마력(지력) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Matk, 7));
                  stats.add(new Pair(EnchantFlag.Int, 3));
                  ess.add(new EquipmentScroll("30% 마력(지력) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Matk, 9));
                  stats.add(new Pair(EnchantFlag.Int, 4));
                  ess.add(new EquipmentScroll("15% 마력(지력) 주문서", usejuhun[3], stats));
                  stats.clear();
               }
            } else {
               if (reqLevel < 80) {
                  stats.add(new Pair(EnchantFlag.Watk, 1));
               } else if (reqLevel < 120) {
                  stats.add(new Pair(EnchantFlag.Watk, 2));
               } else {
                  stats.add(new Pair(EnchantFlag.Watk, 3));
               }

               ess.add(new EquipmentScroll("100% 공격력 주문서", usejuhun[0], stats));
               stats.clear();
               if (reqLevel < 80) {
                  stats.add(new Pair(EnchantFlag.Watk, 2));
                  ess.add(new EquipmentScroll("70% 공격력 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 3));
                  stats.add(new Pair(EnchantFlag.Str, 1));
                  ess.add(new EquipmentScroll("30% 공격력(힘) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Str, 2));
                  ess.add(new EquipmentScroll("15% 공격력(힘) 주문서", usejuhun[3], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 3));
                  stats.add(new Pair(EnchantFlag.Dex, 1));
                  ess.add(new EquipmentScroll("30% 공격력(민첩) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Dex, 2));
                  ess.add(new EquipmentScroll("15% 공격력(민첩) 주문서", usejuhun[3], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 3));
                  stats.add(new Pair(EnchantFlag.Luk, 1));
                  ess.add(new EquipmentScroll("30% 공격력(행운) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Luk, 2));
                  ess.add(new EquipmentScroll("15% 공격력(행운) 주문서", usejuhun[3], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 3));
                  stats.add(new Pair(EnchantFlag.Hp, 50));
                  ess.add(new EquipmentScroll("30% 공격력(체력) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Hp, 100));
                  ess.add(new EquipmentScroll("15% 공격력(체력) 주문서", usejuhun[3], stats));
                  stats.clear();
               } else if (reqLevel < 120) {
                  stats.add(new Pair(EnchantFlag.Watk, 3));
                  stats.add(new Pair(EnchantFlag.Str, 1));
                  ess.add(new EquipmentScroll("70% 공격력(힘) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Str, 2));
                  ess.add(new EquipmentScroll("30% 공격력(힘) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 7));
                  stats.add(new Pair(EnchantFlag.Str, 3));
                  ess.add(new EquipmentScroll("15% 공격력(힘) 주문서", usejuhun[3], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 3));
                  stats.add(new Pair(EnchantFlag.Dex, 1));
                  ess.add(new EquipmentScroll("70% 공격력(민첩) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Dex, 2));
                  ess.add(new EquipmentScroll("30% 공격력(민첩) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 7));
                  stats.add(new Pair(EnchantFlag.Dex, 3));
                  ess.add(new EquipmentScroll("15% 공격력(민첩) 주문서", usejuhun[3], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 3));
                  stats.add(new Pair(EnchantFlag.Luk, 1));
                  ess.add(new EquipmentScroll("70% 공격력(행운) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Luk, 2));
                  ess.add(new EquipmentScroll("30% 공격력(행운) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 7));
                  stats.add(new Pair(EnchantFlag.Luk, 3));
                  ess.add(new EquipmentScroll("15% 공격력(행운) 주문서", usejuhun[3], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 3));
                  stats.add(new Pair(EnchantFlag.Hp, 50));
                  ess.add(new EquipmentScroll("70% 공격력(체력) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Hp, 100));
                  ess.add(new EquipmentScroll("30% 공격력(체력) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 7));
                  stats.add(new Pair(EnchantFlag.Hp, 150));
                  ess.add(new EquipmentScroll("15% 공격력(체력) 주문서", usejuhun[3], stats));
                  stats.clear();
               } else {
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Str, 2));
                  ess.add(new EquipmentScroll("70% 공격력(힘) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 7));
                  stats.add(new Pair(EnchantFlag.Str, 3));
                  ess.add(new EquipmentScroll("30% 공격력(힘) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 9));
                  stats.add(new Pair(EnchantFlag.Str, 4));
                  ess.add(new EquipmentScroll("15% 공격력(힘) 주문서", usejuhun[3], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Dex, 2));
                  ess.add(new EquipmentScroll("70% 공격력(민첩) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 7));
                  stats.add(new Pair(EnchantFlag.Dex, 3));
                  ess.add(new EquipmentScroll("30% 공격력(민첩) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 9));
                  stats.add(new Pair(EnchantFlag.Dex, 4));
                  ess.add(new EquipmentScroll("15% 공격력(민첩) 주문서", usejuhun[3], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Luk, 2));
                  ess.add(new EquipmentScroll("70% 공격력(행운) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 7));
                  stats.add(new Pair(EnchantFlag.Luk, 3));
                  ess.add(new EquipmentScroll("30% 공격력(행운) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 9));
                  stats.add(new Pair(EnchantFlag.Luk, 4));
                  ess.add(new EquipmentScroll("15% 공격력(행운) 주문서", usejuhun[3], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 5));
                  stats.add(new Pair(EnchantFlag.Hp, 100));
                  ess.add(new EquipmentScroll("70% 공격력(체력) 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 7));
                  stats.add(new Pair(EnchantFlag.Hp, 150));
                  ess.add(new EquipmentScroll("30% 공격력(체력) 주문서", usejuhun[2], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 9));
                  stats.add(new Pair(EnchantFlag.Hp, 200));
                  ess.add(new EquipmentScroll("15% 공격력(체력) 주문서", usejuhun[3], stats));
                  stats.clear();
               }
            }
         } else if (equip.getItemId() / 10000 == 108) {
            if (reqLevel < 80) {
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("100% 방어력 주문서", usejuhun[0], stats));
               stats.clear();
               if (ii.getReqJob(equip.getItemId()) == 2) {
                  stats.add(new Pair(EnchantFlag.Matk, 1));
                  ess.add(new EquipmentScroll("70% 마력 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Matk, 2));
                  ess.add(new EquipmentScroll("30% 마력 주문서", usejuhun[2], stats));
                  stats.clear();
               } else {
                  stats.add(new Pair(EnchantFlag.Watk, 1));
                  ess.add(new EquipmentScroll("70% 공격력 주문서", usejuhun[1], stats));
                  stats.clear();
                  stats.add(new Pair(EnchantFlag.Watk, 2));
                  ess.add(new EquipmentScroll("30% 공격력 주문서", usejuhun[2], stats));
                  stats.clear();
               }
            } else if (ii.getReqJob(equip.getItemId()) == 2) {
               stats.add(new Pair(EnchantFlag.Matk, 1));
               ess.add(new EquipmentScroll("100% 마력 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Matk, 1));
               ess.add(new EquipmentScroll("70% 마력 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Matk, 2));
               ess.add(new EquipmentScroll("30% 마력 주문서", usejuhun[2], stats));
               stats.clear();
            } else {
               stats.add(new Pair(EnchantFlag.Watk, 1));
               ess.add(new EquipmentScroll("100% 공격력 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Watk, 2));
               ess.add(new EquipmentScroll("70% 공격력 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Watk, 3));
               ess.add(new EquipmentScroll("30% 공격력 주문서", usejuhun[2], stats));
               stats.clear();
            }
         } else if (equip.getItemId() / 10000 != 111 && equip.getItemId() / 10000 != 112 && equip.getItemId() / 10000 != 113 && equip.getItemId() / 10000 != 103) {
            if (reqLevel < 80) {
               stats.add(new Pair(EnchantFlag.Str, 1));
               stats.add(new Pair(EnchantFlag.Hp, 5));
               stats.add(new Pair(EnchantFlag.Wdef, 1));
               ess.add(new EquipmentScroll("100% 힘 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Str, 2));
               stats.add(new Pair(EnchantFlag.Hp, 15));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("70% 힘 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Str, 3));
               stats.add(new Pair(EnchantFlag.Hp, 30));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("30% 힘 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Dex, 1));
               stats.add(new Pair(EnchantFlag.Hp, 5));
               stats.add(new Pair(EnchantFlag.Wdef, 1));
               ess.add(new EquipmentScroll("100% 민첩 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Dex, 2));
               stats.add(new Pair(EnchantFlag.Hp, 15));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("70% 민첩 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Dex, 3));
               stats.add(new Pair(EnchantFlag.Hp, 30));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("30% 민첩 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Int, 1));
               stats.add(new Pair(EnchantFlag.Hp, 5));
               stats.add(new Pair(EnchantFlag.Wdef, 1));
               ess.add(new EquipmentScroll("100% 지력 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Int, 2));
               stats.add(new Pair(EnchantFlag.Hp, 15));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("70% 지력 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Int, 3));
               stats.add(new Pair(EnchantFlag.Hp, 30));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("30% 지력 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Luk, 1));
               stats.add(new Pair(EnchantFlag.Hp, 5));
               stats.add(new Pair(EnchantFlag.Wdef, 1));
               ess.add(new EquipmentScroll("100% 행운 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Luk, 2));
               stats.add(new Pair(EnchantFlag.Hp, 15));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("70% 행운 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Luk, 3));
               stats.add(new Pair(EnchantFlag.Hp, 30));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("30% 행운 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Hp, 55));
               stats.add(new Pair(EnchantFlag.Wdef, 1));
               ess.add(new EquipmentScroll("100% 체력 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Hp, 115));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("70% 체력 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Hp, 180));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("30% 체력 주문서", usejuhun[2], stats));
               stats.clear();
            } else if (reqLevel < 120) {
               stats.add(new Pair(EnchantFlag.Str, 2));
               stats.add(new Pair(EnchantFlag.Hp, 20));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("100% 힘 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Str, 3));
               stats.add(new Pair(EnchantFlag.Hp, 40));
               stats.add(new Pair(EnchantFlag.Wdef, 4));
               ess.add(new EquipmentScroll("70% 힘 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Str, 5));
               stats.add(new Pair(EnchantFlag.Hp, 70));
               stats.add(new Pair(EnchantFlag.Wdef, 7));
               ess.add(new EquipmentScroll("30% 힘 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Dex, 2));
               stats.add(new Pair(EnchantFlag.Hp, 20));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("100% 민첩 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Dex, 3));
               stats.add(new Pair(EnchantFlag.Hp, 40));
               stats.add(new Pair(EnchantFlag.Wdef, 4));
               ess.add(new EquipmentScroll("70% 민첩 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Dex, 5));
               stats.add(new Pair(EnchantFlag.Hp, 70));
               stats.add(new Pair(EnchantFlag.Wdef, 7));
               ess.add(new EquipmentScroll("30% 민첩 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Int, 2));
               stats.add(new Pair(EnchantFlag.Hp, 20));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("100% 지력 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Int, 3));
               stats.add(new Pair(EnchantFlag.Hp, 40));
               stats.add(new Pair(EnchantFlag.Wdef, 4));
               ess.add(new EquipmentScroll("70% 지력 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Int, 5));
               stats.add(new Pair(EnchantFlag.Hp, 70));
               stats.add(new Pair(EnchantFlag.Wdef, 7));
               ess.add(new EquipmentScroll("30% 지력 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Luk, 2));
               stats.add(new Pair(EnchantFlag.Hp, 20));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("100% 행운 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Luk, 3));
               stats.add(new Pair(EnchantFlag.Hp, 40));
               stats.add(new Pair(EnchantFlag.Wdef, 4));
               ess.add(new EquipmentScroll("70% 행운 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Luk, 5));
               stats.add(new Pair(EnchantFlag.Hp, 70));
               stats.add(new Pair(EnchantFlag.Wdef, 7));
               ess.add(new EquipmentScroll("30% 행운 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Hp, 120));
               stats.add(new Pair(EnchantFlag.Wdef, 2));
               ess.add(new EquipmentScroll("100% 체력 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Hp, 190));
               stats.add(new Pair(EnchantFlag.Wdef, 4));
               ess.add(new EquipmentScroll("70% 체력 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Hp, 320));
               stats.add(new Pair(EnchantFlag.Wdef, 7));
               ess.add(new EquipmentScroll("30% 체력 주문서", usejuhun[2], stats));
               stats.clear();
            } else {
               stats.add(new Pair(EnchantFlag.Str, 3));
               stats.add(new Pair(EnchantFlag.Hp, 30));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("100% 힘 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Str, 4));
               stats.add(new Pair(EnchantFlag.Hp, 70));
               stats.add(new Pair(EnchantFlag.Wdef, 5));
               ess.add(new EquipmentScroll("70% 힘 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Str, 7));
               stats.add(new Pair(EnchantFlag.Hp, 120));
               stats.add(new Pair(EnchantFlag.Wdef, 10));
               ess.add(new EquipmentScroll("30% 힘 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Dex, 3));
               stats.add(new Pair(EnchantFlag.Hp, 30));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("100% 민첩 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Dex, 4));
               stats.add(new Pair(EnchantFlag.Hp, 70));
               stats.add(new Pair(EnchantFlag.Wdef, 5));
               ess.add(new EquipmentScroll("70% 민첩 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Dex, 7));
               stats.add(new Pair(EnchantFlag.Hp, 120));
               stats.add(new Pair(EnchantFlag.Wdef, 10));
               ess.add(new EquipmentScroll("30% 민첩 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Int, 3));
               stats.add(new Pair(EnchantFlag.Hp, 30));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("100% 지력 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Int, 4));
               stats.add(new Pair(EnchantFlag.Hp, 70));
               stats.add(new Pair(EnchantFlag.Wdef, 5));
               ess.add(new EquipmentScroll("70% 지력 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Int, 7));
               stats.add(new Pair(EnchantFlag.Hp, 120));
               stats.add(new Pair(EnchantFlag.Wdef, 10));
               ess.add(new EquipmentScroll("30% 지력 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Luk, 3));
               stats.add(new Pair(EnchantFlag.Hp, 30));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("100% 행운 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Luk, 4));
               stats.add(new Pair(EnchantFlag.Hp, 70));
               stats.add(new Pair(EnchantFlag.Wdef, 5));
               ess.add(new EquipmentScroll("70% 행운 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Luk, 7));
               stats.add(new Pair(EnchantFlag.Hp, 120));
               stats.add(new Pair(EnchantFlag.Wdef, 10));
               ess.add(new EquipmentScroll("30% 행운 주문서", usejuhun[2], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Hp, 180));
               stats.add(new Pair(EnchantFlag.Wdef, 3));
               ess.add(new EquipmentScroll("100% 체력 주문서", usejuhun[0], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Hp, 270));
               stats.add(new Pair(EnchantFlag.Wdef, 5));
               ess.add(new EquipmentScroll("70% 체력 주문서", usejuhun[1], stats));
               stats.clear();
               stats.add(new Pair(EnchantFlag.Hp, 470));
               stats.add(new Pair(EnchantFlag.Wdef, 10));
               ess.add(new EquipmentScroll("30% 체력 주문서", usejuhun[2], stats));
               stats.clear();
            }
         } else if (reqLevel < 80) {
            stats.add(new Pair(EnchantFlag.Str, 1));
            ess.add(new EquipmentScroll("100% 힘 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Str, 2));
            ess.add(new EquipmentScroll("70% 힘 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Str, 3));
            ess.add(new EquipmentScroll("30% 힘 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Dex, 1));
            ess.add(new EquipmentScroll("100% 민첩 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Dex, 2));
            ess.add(new EquipmentScroll("70% 민첩 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Dex, 3));
            ess.add(new EquipmentScroll("30% 민첩 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Int, 1));
            ess.add(new EquipmentScroll("100% 지력 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Int, 2));
            ess.add(new EquipmentScroll("70% 지력 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Int, 3));
            ess.add(new EquipmentScroll("30% 지력 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Luk, 1));
            ess.add(new EquipmentScroll("100% 행운 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Luk, 2));
            ess.add(new EquipmentScroll("70% 행운 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Luk, 3));
            ess.add(new EquipmentScroll("30% 행운 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Hp, 50));
            ess.add(new EquipmentScroll("100% 체력 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Hp, 100));
            ess.add(new EquipmentScroll("70% 체력 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Hp, 150));
            ess.add(new EquipmentScroll("30% 체력 주문서", usejuhun[2], stats));
            stats.clear();
         } else if (reqLevel < 120) {
            stats.add(new Pair(EnchantFlag.Str, 1));
            ess.add(new EquipmentScroll("100% 힘 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Str, 2));
            ess.add(new EquipmentScroll("70% 힘 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Str, 4));
            ess.add(new EquipmentScroll("30% 힘 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Dex, 1));
            ess.add(new EquipmentScroll("100% 민첩 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Dex, 2));
            ess.add(new EquipmentScroll("70% 민첩 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Dex, 4));
            ess.add(new EquipmentScroll("30% 민첩 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Int, 1));
            ess.add(new EquipmentScroll("100% 지력 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Int, 2));
            ess.add(new EquipmentScroll("70% 지력 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Int, 4));
            ess.add(new EquipmentScroll("30% 지력 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Luk, 1));
            ess.add(new EquipmentScroll("100% 행운 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Luk, 2));
            ess.add(new EquipmentScroll("70% 행운 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Luk, 4));
            ess.add(new EquipmentScroll("30% 행운 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Hp, 50));
            ess.add(new EquipmentScroll("100% 체력 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Hp, 100));
            ess.add(new EquipmentScroll("70% 체력 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Hp, 200));
            ess.add(new EquipmentScroll("30% 체력 주문서", usejuhun[2], stats));
            stats.clear();
         } else {
            stats.add(new Pair(EnchantFlag.Str, 2));
            ess.add(new EquipmentScroll("100% 힘 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Str, 3));
            ess.add(new EquipmentScroll("70% 힘 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Str, 5));
            ess.add(new EquipmentScroll("30% 힘 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Dex, 2));
            ess.add(new EquipmentScroll("100% 민첩 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Dex, 3));
            ess.add(new EquipmentScroll("70% 민첩 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Dex, 5));
            ess.add(new EquipmentScroll("30% 민첩 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Int, 2));
            ess.add(new EquipmentScroll("100% 지력 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Int, 3));
            ess.add(new EquipmentScroll("70% 지력 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Int, 5));
            ess.add(new EquipmentScroll("30% 지력 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Luk, 2));
            ess.add(new EquipmentScroll("100% 행운 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Luk, 3));
            ess.add(new EquipmentScroll("70% 행운 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Luk, 5));
            ess.add(new EquipmentScroll("30% 행운 주문서", usejuhun[2], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Hp, 100));
            ess.add(new EquipmentScroll("100% 체력 주문서", usejuhun[0], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Hp, 150));
            ess.add(new EquipmentScroll("70% 체력 주문서", usejuhun[1], stats));
            stats.clear();
            stats.add(new Pair(EnchantFlag.Hp, 250));
            ess.add(new EquipmentScroll("30% 체력 주문서", usejuhun[2], stats));
            stats.clear();
         }
      }

      ess.add(new EquipmentScroll("이노센트 주문서 30%", 5000, stats));
      ess.add(new EquipmentScroll("아크 이노센트 주문서 30%", 10000, stats));
      if (equip.getViciousHammer() == 0) {
         if (equip.getUpgradeSlots() < ii.getSlots(equip.getItemId())) {
            ess.add(new EquipmentScroll("순백의 주문서 5%", 3000, stats));
         }
      } else if (equip.getUpgradeSlots() < ii.getSlots(equip.getItemId()) + 1) {
         ess.add(new EquipmentScroll("순백의 주문서 5%", 3000, stats));
      }

      return ess;
   }

   public static int addExtra(int enhance) {
      int extra = 0;
      int i;
      if (enhance < 5) {
         for(i = 0; i < enhance; ++i) {
            extra += i + 1;
         }
      } else if (enhance < 10) {
         for(i = 0; i < enhance - 5; ++i) {
            ++extra;
         }
      } else if (enhance < 15) {
         for(i = 0; i < enhance - 10; ++i) {
            extra += 2;
         }
      }

      return extra;
   }

   public static StarForceStats starForceStats(Equip item) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      int reqLevel = ii.getReqLevel(item.getItemId());
      List<Pair<EnchantFlag, Integer>> stats = new ArrayList();
      MapleWeaponType weaponType = GameConstants.getWeaponType(item.getItemId());
      if ((Boolean)ii.isSuperial(item.getItemId()).right) {
         if (item.getEnhance() >= 5 && item.getEnhance() < 10) {
            if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("Helisium")) {
               stats.add(new Pair(EnchantFlag.Watk, 3 + addExtra(item.getEnhance())));
               stats.add(new Pair(EnchantFlag.Matk, 3 + addExtra(item.getEnhance())));
            } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("Nova")) {
               stats.add(new Pair(EnchantFlag.Watk, 6 + addExtra(item.getEnhance())));
               stats.add(new Pair(EnchantFlag.Matk, 6 + addExtra(item.getEnhance())));
            } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("Tilent")) {
               stats.add(new Pair(EnchantFlag.Watk, 9 + addExtra(item.getEnhance())));
               stats.add(new Pair(EnchantFlag.Matk, 9 + addExtra(item.getEnhance())));
            } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("MindPendent")) {
               stats.add(new Pair(EnchantFlag.Watk, 9 + addExtra(item.getEnhance())));
               stats.add(new Pair(EnchantFlag.Matk, 9 + addExtra(item.getEnhance())));
            }
         } else if (item.getEnhance() >= 10 && item.getEnhance() < 15) {
            if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("Helisium")) {
               stats.add(new Pair(EnchantFlag.Watk, 9 + addExtra(item.getEnhance())));
               stats.add(new Pair(EnchantFlag.Matk, 9 + addExtra(item.getEnhance())));
            } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("Nova")) {
               stats.add(new Pair(EnchantFlag.Watk, 12 + addExtra(item.getEnhance())));
               stats.add(new Pair(EnchantFlag.Matk, 12 + addExtra(item.getEnhance())));
            } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("Tilent")) {
               stats.add(new Pair(EnchantFlag.Watk, 15 + addExtra(item.getEnhance())));
               stats.add(new Pair(EnchantFlag.Matk, 15 + addExtra(item.getEnhance())));
            } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("MindPendent")) {
               stats.add(new Pair(EnchantFlag.Watk, 15 + addExtra(item.getEnhance())));
               stats.add(new Pair(EnchantFlag.Matk, 15 + addExtra(item.getEnhance())));
            }
         } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("Helisium")) {
            stats.add(new Pair(EnchantFlag.Str, 5 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Dex, 5 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Int, 5 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Luk, 5 + addExtra(item.getEnhance())));
         } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("Nova")) {
            stats.add(new Pair(EnchantFlag.Str, 10 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Dex, 10 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Int, 10 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Luk, 10 + addExtra(item.getEnhance())));
         } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("Tilent")) {
            stats.add(new Pair(EnchantFlag.Str, 19 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Dex, 19 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Int, 19 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Luk, 19 + addExtra(item.getEnhance())));
         } else if (((String)ii.isSuperial(item.getItemId()).getLeft()).equals("MindPendent")) {
            stats.add(new Pair(EnchantFlag.Str, 19 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Dex, 19 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Int, 19 + addExtra(item.getEnhance())));
            stats.add(new Pair(EnchantFlag.Luk, 19 + addExtra(item.getEnhance())));
         }
      } else {
         int ordinary;
         long fire1;
         long fire2;
         long fire3;
         long fire4;
         int i;
         int dat;
         int value;
         if (item.getEnhance() < 5) {
            stats.add(new Pair(EnchantFlag.Str, 2));
            stats.add(new Pair(EnchantFlag.Dex, 2));
            stats.add(new Pair(EnchantFlag.Int, 2));
            stats.add(new Pair(EnchantFlag.Luk, 2));
            if (item.getItemId() / 10000 == 108 && item.getEnhance() == 4) {
               if (ii.getReqJob(item.getItemId()) == 2) {
                  stats.add(new Pair(EnchantFlag.Matk, 1));
               } else {
                  stats.add(new Pair(EnchantFlag.Watk, 1));
               }
            } else if (GameConstants.isWeapon(item.getItemId())) {
               if (isMagicWeapon(weaponType)) {
                  ordinary = item.getMatk() + item.getEnchantMatk();
               } else {
                  ordinary = item.getWatk() + item.getEnchantWatk();
               }

               if (item.getFire() > 0L) {
                  fire1 = item.getFire() % 1000L / 10L;
                  fire2 = item.getFire() % 1000000L / 10000L;
                  fire3 = item.getFire() % 1000000000L / 10000000L;
                  fire4 = item.getFire() % 1000000000000L / 10000000000L;

                  for(i = 0; i < 4; ++i) {
                     dat = (int)(i == 0 ? fire1 : (i == 1 ? fire2 : (i == 2 ? fire3 : fire4)));
                     if (dat == (isMagicWeapon(weaponType) ? 18 : 17)) {
                        if (i == 0) {
                           value = (int)(item.getFire() % 10L / 1L);
                        } else if (i == 1) {
                           value = (int)(item.getFire() % 10000L / 1000L);
                        } else if (i == 2) {
                           value = (int)(item.getFire() % 10000000L / 1000000L);
                        } else {
                           value = (int)(item.getFire() % 10000000000L / 1000000000L);
                        }

                        switch(value) {
                        case 3:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 1200 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 1500 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 1800 / 10000 + 1);
                           }
                           break;
                        case 4:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 1760 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 2200 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 2640 / 10000 + 1);
                           }
                           break;
                        case 5:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 2420 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 3025 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 3630 / 10000 + 1);
                           }
                           break;
                        case 6:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 3200 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 4000 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 4800 / 10000 + 1);
                           }
                           break;
                        case 7:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 4100 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 5125 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 6150 / 10000 + 1);
                           }
                        }
                     }
                  }
               }

               if (isMagicWeapon(weaponType)) {
                  stats.add(new Pair(EnchantFlag.Matk, ordinary / 50 + 1));
               } else {
                  stats.add(new Pair(EnchantFlag.Watk, ordinary / 50 + 1));
               }
            }
         } else if (item.getEnhance() < 15) {
            stats.add(new Pair(EnchantFlag.Str, 3));
            stats.add(new Pair(EnchantFlag.Dex, 3));
            stats.add(new Pair(EnchantFlag.Int, 3));
            stats.add(new Pair(EnchantFlag.Luk, 3));
            if (item.getItemId() / 10000 == 108) {
               if (item.getEnhance() == 6 || item.getEnhance() == 8 || item.getEnhance() == 10 || item.getEnhance() >= 12) {
                  if (ii.getReqJob(item.getItemId()) == 2) {
                     stats.add(new Pair(EnchantFlag.Matk, 1));
                  } else {
                     stats.add(new Pair(EnchantFlag.Watk, 1));
                  }
               }
            } else if (GameConstants.isWeapon(item.getItemId())) {
               if (isMagicWeapon(weaponType)) {
                  ordinary = item.getMatk() + item.getEnchantMatk();
               } else {
                  ordinary = item.getWatk() + item.getEnchantWatk();
               }

               if (item.getFire() > 0L) {
                  fire1 = item.getFire() % 1000L / 10L;
                  fire2 = item.getFire() % 1000000L / 10000L;
                  fire3 = item.getFire() % 1000000000L / 10000000L;
                  fire4 = item.getFire() % 1000000000000L / 10000000000L;

                  for(i = 0; i < 4; ++i) {
                     dat = (int)(i == 0 ? fire1 : (i == 1 ? fire2 : (i == 2 ? fire3 : fire4)));
                     if (dat == (isMagicWeapon(weaponType) ? 18 : 17)) {
                        if (i == 0) {
                           value = (int)(item.getFire() % 10L / 1L);
                        } else if (i == 1) {
                           value = (int)(item.getFire() % 10000L / 1000L);
                        } else if (i == 2) {
                           value = (int)(item.getFire() % 10000000L / 1000000L);
                        } else {
                           value = (int)(item.getFire() % 10000000000L / 1000000000L);
                        }

                        switch(value) {
                        case 3:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 1200 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 1500 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 1800 / 10000 + 1);
                           }
                           break;
                        case 4:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 1760 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 2200 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 2640 / 10000 + 1);
                           }
                           break;
                        case 5:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 2420 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 3025 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 3630 / 10000 + 1);
                           }
                           break;
                        case 6:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 3200 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 4000 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 4800 / 10000 + 1);
                           }
                           break;
                        case 7:
                           if (reqLevel <= 150) {
                              ordinary -= (short)(ordinary * 4100 / 10000 + 1);
                           } else if (reqLevel <= 160) {
                              ordinary -= (short)(ordinary * 5125 / 10000 + 1);
                           } else {
                              ordinary -= (short)(ordinary * 6150 / 10000 + 1);
                           }
                        }
                     }
                  }
               }

               if (isMagicWeapon(weaponType)) {
                  stats.add(new Pair(EnchantFlag.Matk, ordinary / 50 + 1));
               } else {
                  stats.add(new Pair(EnchantFlag.Watk, ordinary / 50 + 1));
               }
            }
         } else {
            if (reqLevel < 140) {
               stats.add(new Pair(EnchantFlag.Str, 7));
               stats.add(new Pair(EnchantFlag.Dex, 7));
               stats.add(new Pair(EnchantFlag.Int, 7));
               stats.add(new Pair(EnchantFlag.Luk, 7));
            } else if (reqLevel < 150) {
               stats.add(new Pair(EnchantFlag.Str, 9));
               stats.add(new Pair(EnchantFlag.Dex, 9));
               stats.add(new Pair(EnchantFlag.Int, 9));
               stats.add(new Pair(EnchantFlag.Luk, 9));
            } else if (reqLevel < 160) {
               stats.add(new Pair(EnchantFlag.Str, 11));
               stats.add(new Pair(EnchantFlag.Dex, 11));
               stats.add(new Pair(EnchantFlag.Int, 11));
               stats.add(new Pair(EnchantFlag.Luk, 11));
            } else if (reqLevel < 200) {
               stats.add(new Pair(EnchantFlag.Str, 13));
               stats.add(new Pair(EnchantFlag.Dex, 13));
               stats.add(new Pair(EnchantFlag.Int, 13));
               stats.add(new Pair(EnchantFlag.Luk, 13));
            } else {
               stats.add(new Pair(EnchantFlag.Str, 15));
               stats.add(new Pair(EnchantFlag.Dex, 15));
               stats.add(new Pair(EnchantFlag.Int, 15));
               stats.add(new Pair(EnchantFlag.Luk, 15));
            }

            if (GameConstants.isWeapon(item.getItemId())) {
               if (reqLevel < 140) {
                  switch(item.getEnhance()) {
                  case 15:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 6));
                     break;
                  case 16:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 7));
                     break;
                  case 17:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 7));
                     break;
                  case 18:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 8));
                     break;
                  case 19:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 9));
                  }
               } else if (reqLevel < 150) {
                  switch(item.getEnhance()) {
                  case 15:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 7));
                     break;
                  case 16:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 8));
                     break;
                  case 17:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 8));
                     break;
                  case 18:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 9));
                     break;
                  case 19:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 10));
                     break;
                  case 20:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 11));
                     break;
                  case 21:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 12));
                     break;
                  case 22:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 13));
                     break;
                  case 23:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 14));
                     break;
                  case 24:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 15));
                  }
               } else if (reqLevel < 160) {
                  switch(item.getEnhance()) {
                  case 15:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 8));
                     break;
                  case 16:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 9));
                     break;
                  case 17:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 9));
                     break;
                  case 18:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 10));
                     break;
                  case 19:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 11));
                     break;
                  case 20:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 12));
                     break;
                  case 21:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 13));
                     break;
                  case 22:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 14));
                     break;
                  case 23:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 15));
                     break;
                  case 24:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 16));
                  }
               } else if (reqLevel < 200) {
                  switch(item.getEnhance()) {
                  case 15:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 9));
                     break;
                  case 16:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 9));
                     break;
                  case 17:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 10));
                     break;
                  case 18:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 11));
                     break;
                  case 19:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 12));
                     break;
                  case 20:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 13));
                     break;
                  case 21:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 14));
                     break;
                  case 22:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 15));
                     break;
                  case 23:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 16));
                     break;
                  case 24:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 17));
                  }
               } else {
                  switch(item.getEnhance()) {
                  case 15:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 13));
                     break;
                  case 16:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 13));
                     break;
                  case 17:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 14));
                     break;
                  case 18:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 14));
                     break;
                  case 19:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 15));
                     break;
                  case 20:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 16));
                     break;
                  case 21:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 17));
                     break;
                  case 22:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 18));
                     break;
                  case 23:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 19));
                     break;
                  case 24:
                     stats.add(new Pair(isMagicWeapon(weaponType) ? EnchantFlag.Matk : EnchantFlag.Watk, 20));
                  }
               }
            } else if (reqLevel < 140) {
               switch(item.getEnhance()) {
               case 15:
                  stats.add(new Pair(EnchantFlag.Watk, 7));
                  stats.add(new Pair(EnchantFlag.Matk, 7));
                  break;
               case 16:
                  stats.add(new Pair(EnchantFlag.Watk, 8));
                  stats.add(new Pair(EnchantFlag.Matk, 8));
                  break;
               case 17:
                  stats.add(new Pair(EnchantFlag.Watk, 9));
                  stats.add(new Pair(EnchantFlag.Matk, 9));
                  break;
               case 18:
                  stats.add(new Pair(EnchantFlag.Watk, 10));
                  stats.add(new Pair(EnchantFlag.Matk, 10));
                  break;
               case 19:
                  stats.add(new Pair(EnchantFlag.Watk, 11));
                  stats.add(new Pair(EnchantFlag.Matk, 11));
               }
            } else if (reqLevel < 150) {
               switch(item.getEnhance()) {
               case 15:
                  stats.add(new Pair(EnchantFlag.Watk, 8));
                  stats.add(new Pair(EnchantFlag.Matk, 8));
                  break;
               case 16:
                  stats.add(new Pair(EnchantFlag.Watk, 9));
                  stats.add(new Pair(EnchantFlag.Matk, 9));
                  break;
               case 17:
                  stats.add(new Pair(EnchantFlag.Watk, 10));
                  stats.add(new Pair(EnchantFlag.Matk, 10));
                  break;
               case 18:
                  stats.add(new Pair(EnchantFlag.Watk, 11));
                  stats.add(new Pair(EnchantFlag.Matk, 11));
                  break;
               case 19:
                  stats.add(new Pair(EnchantFlag.Watk, 12));
                  stats.add(new Pair(EnchantFlag.Matk, 12));
                  break;
               case 20:
                  stats.add(new Pair(EnchantFlag.Watk, 13));
                  stats.add(new Pair(EnchantFlag.Matk, 13));
                  break;
               case 21:
                  stats.add(new Pair(EnchantFlag.Watk, 15));
                  stats.add(new Pair(EnchantFlag.Matk, 15));
                  break;
               case 22:
                  stats.add(new Pair(EnchantFlag.Watk, 16));
                  stats.add(new Pair(EnchantFlag.Matk, 16));
                  break;
               case 23:
                  stats.add(new Pair(EnchantFlag.Watk, 17));
                  stats.add(new Pair(EnchantFlag.Matk, 17));
                  break;
               case 24:
                  stats.add(new Pair(EnchantFlag.Watk, 18));
                  stats.add(new Pair(EnchantFlag.Matk, 18));
               }
            } else if (reqLevel < 160) {
               switch(item.getEnhance()) {
               case 15:
                  stats.add(new Pair(EnchantFlag.Watk, 9));
                  stats.add(new Pair(EnchantFlag.Matk, 9));
                  break;
               case 16:
                  stats.add(new Pair(EnchantFlag.Watk, 10));
                  stats.add(new Pair(EnchantFlag.Matk, 10));
                  break;
               case 17:
                  stats.add(new Pair(EnchantFlag.Watk, 11));
                  stats.add(new Pair(EnchantFlag.Matk, 11));
                  break;
               case 18:
                  stats.add(new Pair(EnchantFlag.Watk, 12));
                  stats.add(new Pair(EnchantFlag.Matk, 12));
                  break;
               case 19:
                  stats.add(new Pair(EnchantFlag.Watk, 13));
                  stats.add(new Pair(EnchantFlag.Matk, 13));
                  break;
               case 20:
                  stats.add(new Pair(EnchantFlag.Watk, 14));
                  stats.add(new Pair(EnchantFlag.Matk, 14));
                  break;
               case 21:
                  stats.add(new Pair(EnchantFlag.Watk, 16));
                  stats.add(new Pair(EnchantFlag.Matk, 16));
                  break;
               case 22:
                  stats.add(new Pair(EnchantFlag.Watk, 17));
                  stats.add(new Pair(EnchantFlag.Matk, 17));
                  break;
               case 23:
                  stats.add(new Pair(EnchantFlag.Watk, 18));
                  stats.add(new Pair(EnchantFlag.Matk, 18));
                  break;
               case 24:
                  stats.add(new Pair(EnchantFlag.Watk, 19));
                  stats.add(new Pair(EnchantFlag.Matk, 19));
               }
            } else if (reqLevel < 200) {
               switch(item.getEnhance()) {
               case 15:
                  stats.add(new Pair(EnchantFlag.Watk, 10));
                  stats.add(new Pair(EnchantFlag.Matk, 10));
                  break;
               case 16:
                  stats.add(new Pair(EnchantFlag.Watk, 11));
                  stats.add(new Pair(EnchantFlag.Matk, 11));
                  break;
               case 17:
                  stats.add(new Pair(EnchantFlag.Watk, 12));
                  stats.add(new Pair(EnchantFlag.Matk, 12));
                  break;
               case 18:
                  stats.add(new Pair(EnchantFlag.Watk, 13));
                  stats.add(new Pair(EnchantFlag.Matk, 13));
                  break;
               case 19:
                  stats.add(new Pair(EnchantFlag.Watk, 14));
                  stats.add(new Pair(EnchantFlag.Matk, 14));
                  break;
               case 20:
                  stats.add(new Pair(EnchantFlag.Watk, 15));
                  stats.add(new Pair(EnchantFlag.Matk, 15));
                  break;
               case 21:
                  stats.add(new Pair(EnchantFlag.Watk, 17));
                  stats.add(new Pair(EnchantFlag.Matk, 17));
                  break;
               case 22:
                  stats.add(new Pair(EnchantFlag.Watk, 18));
                  stats.add(new Pair(EnchantFlag.Matk, 18));
                  break;
               case 23:
                  stats.add(new Pair(EnchantFlag.Watk, 19));
                  stats.add(new Pair(EnchantFlag.Matk, 19));
                  break;
               case 24:
                  stats.add(new Pair(EnchantFlag.Watk, 20));
                  stats.add(new Pair(EnchantFlag.Matk, 20));
               }
            } else {
               switch(item.getEnhance()) {
               case 15:
                  stats.add(new Pair(EnchantFlag.Watk, 12));
                  stats.add(new Pair(EnchantFlag.Matk, 12));
                  break;
               case 16:
                  stats.add(new Pair(EnchantFlag.Watk, 13));
                  stats.add(new Pair(EnchantFlag.Matk, 13));
                  break;
               case 17:
                  stats.add(new Pair(EnchantFlag.Watk, 14));
                  stats.add(new Pair(EnchantFlag.Matk, 14));
                  break;
               case 18:
                  stats.add(new Pair(EnchantFlag.Watk, 15));
                  stats.add(new Pair(EnchantFlag.Matk, 15));
                  break;
               case 19:
                  stats.add(new Pair(EnchantFlag.Watk, 16));
                  stats.add(new Pair(EnchantFlag.Matk, 16));
                  break;
               case 20:
                  stats.add(new Pair(EnchantFlag.Watk, 17));
                  stats.add(new Pair(EnchantFlag.Matk, 17));
                  break;
               case 21:
                  stats.add(new Pair(EnchantFlag.Watk, 19));
                  stats.add(new Pair(EnchantFlag.Matk, 19));
                  break;
               case 22:
                  stats.add(new Pair(EnchantFlag.Watk, 20));
                  stats.add(new Pair(EnchantFlag.Matk, 20));
                  break;
               case 23:
                  stats.add(new Pair(EnchantFlag.Watk, 21));
                  stats.add(new Pair(EnchantFlag.Matk, 21));
                  break;
               case 24:
                  stats.add(new Pair(EnchantFlag.Watk, 22));
                  stats.add(new Pair(EnchantFlag.Matk, 22));
               }
            }
         }
      }

      return new StarForceStats(stats);
   }

   public static void handleEnchant(LittleEndianAccessor slea, MapleClient c) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      byte type = slea.readByte();
      if (Calendar.getInstance().get(7) == 5) {
         ServerConstants.starForceSalePercent = 10;
      } else {
         ServerConstants.starForceSalePercent = 0;
      }

      Equip item;
      short pos;
      switch(type) {
      case 0:
         slea.skip(4);
         pos = slea.readShort();
         int index = slea.readInt();
         if (pos > 0) {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(pos);
         } else {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(pos);
         }

         Equip equip1 = null;
         if (GameConstants.isZeroWeapon(item.getItemId())) {
            equip1 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(pos == -11 ? -10 : -11));
         }

         List<EquipmentScroll> ess = equipmentScrolls(item);
         if (ess.size() <= index) {
            return;
         }

         EquipmentScroll es = (EquipmentScroll)ess.get(index);
         int i = 0;
         int percent;
         if (scrollType(es.getName()) == 4) {
            percent = 30;
         } else if (scrollType(es.getName()) == 5) {
            percent = 5;
         } else {
            percent = Integer.parseInt(es.getName().split("%")[0]);
         }

         if (ServerConstants.feverTime) {
            if (es.getName().startsWith("순백")) {
               percent += 5;
            } else if (!es.getName().startsWith("이노센트") && !es.getName().startsWith("아크")) {
               percent = Integer.valueOf(es.getName().split("%")[0]);
               if (percent == 15) {
                  percent += 10;
               } else if (percent == 30) {
                  percent += 15;
               } else if (percent == 70) {
                  percent += 25;
               }
            } else {
               percent += 20;
            }
         }

         if (Randomizer.nextInt(1000) < percent * 10) {
            i = 1;
         }

         if (c.getPlayer().isGM()) {
            i = 1;
         }

         if (!c.getPlayer().haveItem(4001832, es.getJuhun())) {
            return;
         }

         c.getPlayer().removeItem(4001832, -es.getJuhun());
         Equip equip3 = (Equip)item.copy();
         if (i > 0) {
            if (scrollType(es.getName()) == 4) {
               Equip origin = (Equip)MapleItemInformationProvider.getInstance().getEquipById(item.getItemId(), false);
               int reqLevel = MapleItemInformationProvider.getInstance().getReqLevel(item.getItemId());
               int ordinaryPad = origin.getWatk() > 0 ? origin.getWatk() : origin.getMatk();
               int ordinaryMad = origin.getMatk() > 0 ? origin.getMatk() : origin.getWatk();
               origin.setState(equip3.getState());
               origin.setPotential1(equip3.getPotential1());
               origin.setPotential2(equip3.getPotential2());
               origin.setPotential3(equip3.getPotential3());
               origin.setPotential4(equip3.getPotential4());
               origin.setPotential5(equip3.getPotential5());
               origin.setPotential6(equip3.getPotential6());
               origin.setSoulEnchanter(equip3.getSoulEnchanter());
               origin.setSoulName(equip3.getSoulName());
               origin.setSoulPotential(equip3.getSoulPotential());
               origin.setSoulSkill(equip3.getSoulSkill());
               origin.setFire(equip3.getFire());
               origin.setKarmaCount(equip3.getKarmaCount());
               item.set(origin);
               if (equip1 != null) {
                  equip1.set(origin);
               }

               int[] rebirth = new int[4];
               String fire = String.valueOf(item.getFire());
               if (fire.length() == 12) {
                  rebirth[0] = Integer.parseInt(fire.substring(0, 3));
                  rebirth[1] = Integer.parseInt(fire.substring(3, 6));
                  rebirth[2] = Integer.parseInt(fire.substring(6, 9));
                  rebirth[3] = Integer.parseInt(fire.substring(9));
               } else if (fire.length() == 11) {
                  rebirth[0] = Integer.parseInt(fire.substring(0, 2));
                  rebirth[1] = Integer.parseInt(fire.substring(2, 5));
                  rebirth[2] = Integer.parseInt(fire.substring(5, 8));
                  rebirth[3] = Integer.parseInt(fire.substring(8));
               } else if (fire.length() == 10) {
                  rebirth[0] = Integer.parseInt(fire.substring(0, 1));
                  rebirth[1] = Integer.parseInt(fire.substring(1, 4));
                  rebirth[2] = Integer.parseInt(fire.substring(4, 7));
                  rebirth[3] = Integer.parseInt(fire.substring(7));
               }

               int k;
               if (fire.length() >= 10) {
                  for(k = 0; k < 4; ++k) {
                     int randomOption = rebirth[k] / 10;
                     int randomValue = rebirth[k] - rebirth[k] / 10 * 10;
                     item.setFireOption(randomOption, reqLevel, randomValue, ordinaryPad, ordinaryMad);
                     if (equip1 != null) {
                        equip1.setFireOption(randomOption, reqLevel, randomValue, ordinaryPad, ordinaryMad);
                     }
                  }
               }

               item.setEquipmentType(4352);
               if (es.getName().contains("아크")) {
                  for(k = 0; k < equip3.getEnhance(); ++k) {
                     StarForceStats starForceStats = starForceStats(item);
                     item.setEnhance((byte)(item.getEnhance() + 1));
                     if (equip1 != null) {
                        equip1.setEnhance((byte)(equip1.getEnhance() + 1));
                     }

                     Iterator var53 = starForceStats.getStats().iterator();

                     while(var53.hasNext()) {
                        Pair<EnchantFlag, Integer> stat = (Pair)var53.next();
                        if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantWatk((short)(item.getEnchantWatk() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantWatk((short)(equip1.getEnchantWatk() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantMatk((short)(item.getEnchantMatk() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantMatk((short)(equip1.getEnchantMatk() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantStr((short)(item.getEnchantStr() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantStr((short)(equip1.getEnchantStr() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantDex((short)(item.getEnchantDex() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantDex((short)(equip1.getEnchantDex() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantInt((short)(item.getEnchantInt() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantInt((short)(equip1.getEnchantInt() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantLuk((short)(item.getEnchantLuk() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantLuk((short)(equip1.getEnchantLuk() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantWdef((short)(item.getEnchantWdef() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantWdef((short)(equip1.getEnchantWdef() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantMdef((short)(item.getEnchantMdef() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantMdef((short)(equip1.getEnchantMdef() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantHp((short)(item.getEnchantHp() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantHp((short)(equip1.getEnchantHp() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantMp((short)(item.getEnchantMp() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantMp((short)(equip1.getEnchantMp() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantAcc((short)(item.getEnchantAcc() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantAcc((short)(equip1.getEnchantAcc() + (Integer)stat.right));
                           }
                        }

                        if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                           item.setEnchantAvoid((short)(item.getEnchantAvoid() + (Integer)stat.right));
                           if (equip1 != null) {
                              equip1.setEnchantAvoid((short)(equip1.getEnchantAvoid() + (Integer)stat.right));
                           }
                        }
                     }
                  }
               }
            } else if (scrollType(es.getName()) == 5) {
               item.setUpgradeSlots((byte)(item.getUpgradeSlots() + 1));
               if (equip1 != null) {
                  equip1.setUpgradeSlots((byte)(equip1.getUpgradeSlots() + 1));
               }

               c.getSession().writeAndFlush(CWvsContext.equipmentEnchantResult(50, item, (Equip)null, (EquipmentScroll)null, (StarForceStats)null));
            } else {
               item.setLevel((byte)(item.getLevel() + 1));
               if (equip1 != null) {
                  equip1.setLevel((byte)(equip1.getLevel() + 1));
               }

               if (EnchantFlag.Watk.check(es.getFlag())) {
                  item.setWatk((short)(item.getWatk() + (Integer)es.getFlag(EnchantFlag.Watk).right));
                  if (equip1 != null) {
                     equip1.setWatk((short)(equip1.getWatk() + (Integer)es.getFlag(EnchantFlag.Watk).right));
                  }
               }

               if (EnchantFlag.Matk.check(es.getFlag())) {
                  item.setMatk((short)(item.getMatk() + (Integer)es.getFlag(EnchantFlag.Matk).right));
                  if (equip1 != null) {
                     equip1.setMatk((short)(equip1.getMatk() + (Integer)es.getFlag(EnchantFlag.Matk).right));
                  }
               }

               if (EnchantFlag.Str.check(es.getFlag())) {
                  item.setStr((short)(item.getStr() + (Integer)es.getFlag(EnchantFlag.Str).right));
                  if (equip1 != null) {
                     equip1.setStr((short)(equip1.getStr() + (Integer)es.getFlag(EnchantFlag.Str).right));
                  }
               }

               if (EnchantFlag.Dex.check(es.getFlag())) {
                  item.setDex((short)(item.getDex() + (Integer)es.getFlag(EnchantFlag.Dex).right));
                  if (equip1 != null) {
                     equip1.setDex((short)(equip1.getDex() + (Integer)es.getFlag(EnchantFlag.Dex).right));
                  }
               }

               if (EnchantFlag.Int.check(es.getFlag())) {
                  item.setInt((short)(item.getInt() + (Integer)es.getFlag(EnchantFlag.Int).right));
                  if (equip1 != null) {
                     equip1.setInt((short)(equip1.getInt() + (Integer)es.getFlag(EnchantFlag.Int).right));
                  }
               }

               if (EnchantFlag.Luk.check(es.getFlag())) {
                  item.setLuk((short)(item.getLuk() + (Integer)es.getFlag(EnchantFlag.Luk).right));
                  if (equip1 != null) {
                     equip1.setLuk((short)(equip1.getLuk() + (Integer)es.getFlag(EnchantFlag.Luk).right));
                  }
               }

               if (EnchantFlag.Wdef.check(es.getFlag())) {
                  item.setWdef((short)(item.getWdef() + (Integer)es.getFlag(EnchantFlag.Wdef).right));
                  if (equip1 != null) {
                     equip1.setWdef((short)(equip1.getWdef() + (Integer)es.getFlag(EnchantFlag.Wdef).right));
                  }
               }

               if (EnchantFlag.Mdef.check(es.getFlag())) {
                  item.setMdef((short)(item.getMdef() + (Integer)es.getFlag(EnchantFlag.Mdef).right));
                  if (equip1 != null) {
                     equip1.setMdef((short)(equip1.getMdef() + (Integer)es.getFlag(EnchantFlag.Mdef).right));
                  }
               }

               if (EnchantFlag.Hp.check(es.getFlag())) {
                  item.setHp((short)(item.getHp() + (Integer)es.getFlag(EnchantFlag.Hp).right));
                  if (equip1 != null) {
                     equip1.setHp((short)(equip1.getHp() + (Integer)es.getFlag(EnchantFlag.Hp).right));
                  }
               }

               if (EnchantFlag.Mp.check(es.getFlag())) {
                  item.setMp((short)(item.getMp() + (Integer)es.getFlag(EnchantFlag.Mp).right));
                  if (equip1 != null) {
                     equip1.setMp((short)(equip1.getMp() + (Integer)es.getFlag(EnchantFlag.Mp).right));
                  }
               }

               if (EnchantFlag.Acc.check(es.getFlag())) {
                  item.setAcc((short)(item.getAcc() + (Integer)es.getFlag(EnchantFlag.Acc).right));
                  if (equip1 != null) {
                     equip1.setAcc((short)(equip1.getAcc() + (Integer)es.getFlag(EnchantFlag.Acc).right));
                  }
               }

               if (EnchantFlag.Avoid.check(es.getFlag())) {
                  item.setAvoid((short)(item.getAvoid() + (Integer)es.getFlag(EnchantFlag.Avoid).right));
                  if (equip1 != null) {
                     equip1.setAvoid((short)(equip1.getAvoid() + (Integer)es.getFlag(EnchantFlag.Avoid).right));
                  }
               }

               if (!GameConstants.isWeapon(item.getItemId()) && item.getItemId() / 10000 != 108 && item.getLevel() == 4) {
                  if (ii.getReqJob(item.getItemId()) == 2) {
                     item.addMatk((short)1);
                     if (equip1 != null) {
                        equip1.addMatk((short)1);
                     }
                  } else {
                     item.addWatk((short)1);
                     if (equip1 != null) {
                        equip1.addWatk((short)1);
                     }
                  }
               }
            }
         }

         if (scrollType(es.getName()) <= 3) {
            boolean safety = false;
            if (ItemFlag.SAFETY_SHIELD.check(item.getFlag())) {
               item.setFlag(item.getFlag() - ItemFlag.SAFETY_SHIELD.getValue());
               if (equip1 != null) {
                  equip1.setFlag(equip1.getFlag() - ItemFlag.SAFETY_SHIELD.getValue());
               }

               safety = true;
            }

            if ((!safety || i <= 0) && safety) {
               c.getPlayer().dropMessage(5, "세이프티 실드의 효과로 업그레이드 횟수가 차감되지 않았습니다.");
            } else {
               item.setUpgradeSlots((byte)(item.getUpgradeSlots() - 1));
               if (equip1 != null) {
                  equip1.setUpgradeSlots((byte)(equip1.getUpgradeSlots() - 1));
               }
            }
         }

         checkEquipmentStats(c, item);
         if (equip1 != null) {
            checkEquipmentStats(c, equip1);
         }

         c.getSession().writeAndFlush(CWvsContext.equipmentEnchantResult(100, equip3, item, es, (StarForceStats)null, i));
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item));
         if (equip1 != null) {
            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, equip1));
         }
         break;
      case 1:
         slea.skip(4);
         pos = slea.readShort();
         if (pos > 0) {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(pos);
         } else {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(pos);
         }

         Equip item2 = null;
         if (GameConstants.isZeroWeapon(item.getItemId())) {
            item2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(pos == -11 ? -10 : -11));
         }

         byte catchStar = slea.readByte();
         if (catchStar == 1) {
            slea.skip(4);
         }

         slea.readInt();
         slea.readInt();
         boolean bool1 = c.getPlayer().shield;
         Equip equip2 = (Equip)item.copy();
         Pair<Integer, Integer> pair1 = starForcePercent(item);
         int i = (Integer)pair1.left;
         int j = (Integer)pair1.right;
         long l = StarForceMeso(item);
         double rate = (double)(100 - ServerConstants.starForceSalePercent) / 100.0D;
         l = (long)((double)l * rate);
         if (l < 0L) {
            l &= 4294967295L;
         }

         if (catchStar == 1) {
            i += 45;
         }

         if (bool1) {
            j = 0;
         }

         if (c.getPlayer().isGM()) {
            i = 1000;
         }

         if (item.getEnhance() < 22) {
            if (c.getPlayer().haveItem(4310021)) {
               c.getPlayer().gainItem(4310021, (short)-1, false, 0L, "");
               i += 100;
            } else if (c.getPlayer().haveItem(4310019)) {
               c.getPlayer().gainItem(4310019, (short)-1, false, 0L, "");
               i += 50;
            }
         }

         if (i >= 1000) {
            i = 1000;
         }

         if (c.getPlayer().getMeso() < l) {
            c.getPlayer().dropMessage(1, "메소가 부족합니다.");
            return;
         }

         c.getPlayer().gainMeso(-l, false);
         if (bool1) {
            c.getPlayer().gainMeso(-l, false);
         }

         if ((item.getEnchantBuff() & 32) != 0) {
            i = 1000;
            j = 0;
         }

         byte result;
         StarForceStats starForceStats;
         Iterator var35;
         Pair stat;
         if (Randomizer.nextInt(1000) < i) {
            starForceStats = starForceStats(item);
            item.setEnhance((byte)(item.getEnhance() + 1));
            if (item2 != null) {
               item2.setEnhance((byte)(item2.getEnhance() + 1));
            }

            result = 1;
            var35 = starForceStats.getStats().iterator();

            while(var35.hasNext()) {
               stat = (Pair)var35.next();
               if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantWatk((short)(item.getEnchantWatk() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantWatk((short)(item2.getEnchantWatk() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantMatk((short)(item.getEnchantMatk() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantMatk((short)(item2.getEnchantMatk() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantStr((short)(item.getEnchantStr() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantStr((short)(item2.getEnchantStr() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantDex((short)(item.getEnchantDex() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantDex((short)(item2.getEnchantDex() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantInt((short)(item.getEnchantInt() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantInt((short)(item2.getEnchantInt() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantLuk((short)(item.getEnchantLuk() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantLuk((short)(item2.getEnchantLuk() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantWdef((short)(item.getEnchantWdef() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantWdef((short)(item2.getEnchantWdef() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantMdef((short)(item.getEnchantMdef() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantMdef((short)(item2.getEnchantMdef() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantHp((short)(item.getEnchantHp() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantHp((short)(item2.getEnchantHp() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantMp((short)(item.getEnchantMp() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantMp((short)(item2.getEnchantMp() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantAcc((short)(item.getEnchantAcc() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantAcc((short)(item2.getEnchantAcc() + (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantAvoid((short)(item.getEnchantAvoid() + (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantAvoid((short)(item2.getEnchantAvoid() + (Integer)stat.right));
                  }
               }
            }

            if ((item.getEnchantBuff() & 32) != 0) {
               item.setEnchantBuff((short)(item.getEnchantBuff() - 32));
               if (item2 != null && (item2.getEnchantBuff() & 32) != 0) {
                  item2.setEnchantBuff((short)(item2.getEnchantBuff() - 32));
               }
            }

            if ((item.getEnchantBuff() & 16) != 0) {
               item.setEnchantBuff((short)(item.getEnchantBuff() - 16));
               if (item2 != null && (item2.getEnchantBuff() & 16) != 0) {
                  item2.setEnchantBuff((short)(item2.getEnchantBuff() - 16));
               }
            }

            checkEquipmentStats(c, item);
            if (item2 != null) {
               checkEquipmentStats(c, item2);
            }
         } else if (Randomizer.nextInt(1000) < j) {
            result = 2;

            while(true) {
               if (item.getEnhance() <= ((Boolean)ii.isSuperial(item.getItemId()).right ? 0 : 12)) {
                  item.setEnchantBuff((short)136);
                  if (item2 != null) {
                     item2.setEnchantBuff((short)136);
                  }

                  checkEquipmentStats(c, item);
                  if (item2 != null) {
                     checkEquipmentStats(c, item2);
                  }
                  break;
               }

               item.setEnhance((byte)(item.getEnhance() - 1));
               if (item2 != null) {
                  item2.setEnhance((byte)(item2.getEnhance() - 1));
               }

               starForceStats = starForceStats(item);
               var35 = starForceStats.getStats().iterator();

               while(var35.hasNext()) {
                  stat = (Pair)var35.next();
                  if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantWatk((short)(item.getEnchantWatk() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantWatk((short)(item2.getEnchantWatk() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantMatk((short)(item.getEnchantMatk() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantMatk((short)(item2.getEnchantMatk() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantStr((short)(item.getEnchantStr() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantStr((short)(item2.getEnchantStr() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantDex((short)(item.getEnchantDex() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantDex((short)(item2.getEnchantDex() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantInt((short)(item.getEnchantInt() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantInt((short)(item2.getEnchantInt() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantLuk((short)(item.getEnchantLuk() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantLuk((short)(item2.getEnchantLuk() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantWdef((short)(item.getEnchantWdef() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantWdef((short)(item2.getEnchantWdef() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantMdef((short)(item.getEnchantMdef() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantMdef((short)(item2.getEnchantMdef() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantHp((short)(item.getEnchantHp() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantHp((short)(item2.getEnchantHp() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantMp((short)(item.getEnchantMp() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantMp((short)(item2.getEnchantMp() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantAcc((short)(item.getEnchantAcc() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantAcc((short)(item2.getEnchantAcc() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                     item.setEnchantAvoid((short)(item.getEnchantAvoid() - (Integer)stat.right));
                     if (item2 != null) {
                        item2.setEnchantAvoid((short)(item2.getEnchantAvoid() - (Integer)stat.right));
                     }
                  }
               }
            }
         } else if (((Boolean)ii.isSuperial(item.getItemId()).right || item.getEnhance() > 10) && item.getEnhance() % 5 != 0) {
            result = 0;
            item.setEnhance((byte)(item.getEnhance() - 1));
            if (item2 != null) {
               item2.setEnhance((byte)(item2.getEnhance() - 1));
            }

            starForceStats = starForceStats(item);
            var35 = starForceStats.getStats().iterator();

            while(var35.hasNext()) {
               stat = (Pair)var35.next();
               if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantWatk((short)(item.getEnchantWatk() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantWatk((short)(item2.getEnchantWatk() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantMatk((short)(item.getEnchantMatk() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantMatk((short)(item2.getEnchantMatk() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantStr((short)(item.getEnchantStr() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantStr((short)(item2.getEnchantStr() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantDex((short)(item.getEnchantDex() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantDex((short)(item2.getEnchantDex() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantInt((short)(item.getEnchantInt() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantInt((short)(item2.getEnchantInt() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantLuk((short)(item.getEnchantLuk() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantLuk((short)(item2.getEnchantLuk() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantWdef((short)(item.getEnchantWdef() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantWdef((short)(item2.getEnchantWdef() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantMdef((short)(item.getEnchantMdef() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantMdef((short)(item2.getEnchantMdef() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantHp((short)(item.getEnchantHp() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantHp((short)(item2.getEnchantHp() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantMp((short)(item.getEnchantMp() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantMp((short)(item2.getEnchantMp() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantAcc((short)(item.getEnchantAcc() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantAcc((short)(item2.getEnchantAcc() - (Integer)stat.right));
                  }
               }

               if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                  item.setEnchantAvoid((short)(item.getEnchantAvoid() - (Integer)stat.right));
                  if (item2 != null) {
                     item2.setEnchantAvoid((short)(item2.getEnchantAvoid() - (Integer)stat.right));
                  }
               }
            }

            checkEquipmentStats(c, item);
            if (item2 != null) {
               checkEquipmentStats(c, item2);
            }

            if ((item.getEnchantBuff() & 16) != 0) {
               item.setEnchantBuff((short)(item.getEnchantBuff() + 32));
            } else {
               item.setEnchantBuff((short)(item.getEnchantBuff() + 16));
            }

            if (item2 != null) {
               if ((item2.getEnchantBuff() & 16) != 0) {
                  item2.setEnchantBuff((short)(item2.getEnchantBuff() + 32));
               } else {
                  item2.setEnchantBuff((short)(item2.getEnchantBuff() + 16));
               }
            }
         } else {
            result = 3;
         }

         if (item.getEnchantBuff() >= 136 && item.getPosition() < 0) {
            MapleInventoryManipulator.unequip(c, item.getPosition(), c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot(), MapleInventoryType.EQUIP);
            if (item2 != null) {
               MapleInventoryManipulator.unequip(c, item2.getPosition(), c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot(), MapleInventoryType.EQUIP);
               Item zw = MapleInventoryManipulator.addId_Item(c, 1572000, (short)1, "", (MaplePet)null, -1L, "", false);
               if (zw != null) {
                  MapleInventoryManipulator.equip(c, zw.getPosition(), (short)-11, MapleInventoryType.EQUIP);
               }

               Item zw2 = MapleInventoryManipulator.addId_Item(c, 1562000, (short)1, "", (MaplePet)null, -1L, "", false);
               if (zw2 != null) {
                  MapleInventoryManipulator.equip(c, zw2.getPosition(), (short)-10, MapleInventoryType.EQUIP);
               }
            }
         }

         c.getPlayer().shield = false;
         c.getSession().writeAndFlush(CWvsContext.equipmentEnchantResult(101, equip2, item, (EquipmentScroll)null, (StarForceStats)null, result));
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item));
         if (item2 != null) {
            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item2));
         }
         break;
      case 2:
         slea.skip(4);
         pos = slea.readShort();
         if (pos > 0) {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(pos);
         } else {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(pos);
         }

         pos = slea.readShort();
         Equip trace;
         if (pos > 0) {
            trace = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(pos);
         } else {
            trace = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(pos);
         }

         Equip ordinary = (Equip)trace.copy();
         trace.setEnchantBuff((short)0);
         MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, item.getPosition(), (short)1, false);
         c.getSession().writeAndFlush(CWvsContext.equipmentEnchantResult(101, ordinary, trace, (EquipmentScroll)null, (StarForceStats)null, 1));
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, trace));
         if (GameConstants.isZeroWeapon(trace.getItemId())) {
            if (GameConstants.isAlphaWeapon(trace.getItemId())) {
               MapleInventoryManipulator.equip(c, trace.getPosition(), (short)-11, MapleInventoryType.EQUIP);
            } else if (GameConstants.isBetaWeapon(trace.getItemId())) {
               MapleInventoryManipulator.equip(c, trace.getPosition(), (short)-10, MapleInventoryType.EQUIP);
            }
         }
         break;
      case 50:
         pos = slea.readShort();
         if (pos > 0) {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(pos);
         } else {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(pos);
         }

         c.getSession().writeAndFlush(CWvsContext.equipmentEnchantResult(type, item, (Equip)null, (EquipmentScroll)null, (StarForceStats)null));
         break;
      case 52:
         pos = (short)slea.readInt();
         if (pos > 0) {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(pos);
         } else {
            item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(pos);
         }

         boolean shield = slea.readByte() == 1;
         c.getPlayer().shield = shield;
         Pair<Integer, Integer> per = starForcePercent(item);
         int success = (Integer)per.left;
         int destroy = (Integer)per.right;
         int meso = (int)StarForceMeso(item);
         if (shield) {
            meso *= 2;
         }

         if (item.getEnhance() < 22) {
            if (c.getPlayer().haveItem(4310021)) {
               success += 100;
               c.getPlayer().dropMessage(-1, "[안내] 스타포스 10% 확률 업 티켓 효과로 성공 확률이 증가 하였습니다.");
               c.getPlayer().dropMessage(5, "[안내] 스타포스 10% 확률 업 티켓 효과로 성공 확률이 증가 하였습니다.");
            } else if (c.getPlayer().haveItem(4310019)) {
               success += 50;
               c.getPlayer().dropMessage(-1, "[안내] 스타포스 5% 확률 업 티켓 효과로 성공 확률이 증가 하였습니다.");
               c.getPlayer().dropMessage(5, "[안내] 스타포스 5% 확률 업 티켓 효과로 성공 확률이 증가 하였습니다.");
            }
         }

         if (success >= 1000) {
            success = 1000;
         }

         int down = 1000 - success - destroy;
         StarForceStats stats = starForceStats(item);
         if (!(Boolean)ii.isSuperial(item.getItemId()).right && item.getEnhance() <= 10 || item.getEnhance() % 5 == 0) {
            down = 0;
         }

         if ((item.getEnchantBuff() & 32) != 0) {
            success = 1000;
            down = 0;
            boolean var44 = false;
         }

         if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < (GameConstants.isZeroWeapon(item.getItemId()) ? 2 : 1)) {
            c.getPlayer().dropMessage(1, "장비 창에 " + (GameConstants.isZeroWeapon(item.getItemId()) ? 2 : 1) + "칸 이상의 공간이 필요합니다.");
            return;
         }

         int despoer = 0;
         if (item.getEnhance() >= 12) {
            switch(item.getEnhance()) {
            case 12:
               despoer = 10;
               break;
            case 13:
            case 14:
               despoer = 20;
               break;
            case 15:
            case 16:
            case 17:
               despoer = 30;
               break;
            case 18:
            case 19:
               despoer = 40;
               break;
            case 20:
            case 21:
               despoer = 100;
               break;
            case 22:
               despoer = 200;
               break;
            case 23:
               despoer = 300;
               break;
            case 24:
               despoer = 400;
            }
         }

         if ((Boolean)ii.isSuperial(item.getItemId()).right) {
            switch(item.getEnhance()) {
            case 5:
               despoer = 30;
               break;
            case 6:
               despoer = 50;
               break;
            case 7:
               despoer = 70;
               break;
            case 8:
               despoer = 100;
               break;
            case 9:
               despoer = 150;
               break;
            case 10:
               despoer = 200;
               break;
            case 11:
               despoer = 251;
               break;
            case 12:
            case 13:
               despoer = 500;
               break;
            case 14:
               despoer = 500;
            }
         }

         c.getSession().writeAndFlush(CWvsContext.equipmentEnchantResult(type, item, (Equip)null, (EquipmentScroll)null, stats, down, despoer, success, meso));
         break;
      case 53:
         c.getSession().writeAndFlush(CWvsContext.equipmentEnchantResult(53, (Equip)null, (Equip)null, (EquipmentScroll)null, (StarForceStats)null, -1525457920));
      }

   }

   public static Pair<Integer, Integer> starForcePercent(Equip item) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      boolean superial = (Boolean)ii.isSuperial(item.getItemId()).right;
      Pair<Integer, Integer> percent = new Pair(0, 0);
      switch(item.getEnhance()) {
      case 0:
         percent.left = superial ? 500 : 950;
         break;
      case 1:
         percent.left = superial ? 500 : 900;
         break;
      case 2:
         percent.left = superial ? 450 : 850;
         break;
      case 3:
         percent.left = superial ? 400 : 850;
         break;
      case 4:
         percent.left = superial ? 400 : 800;
         break;
      case 5:
         percent.left = superial ? 400 : 750;
         percent.right = superial ? 18 : 0;
         break;
      case 6:
         percent.left = superial ? 400 : 700;
         percent.right = superial ? 30 : 0;
         break;
      case 7:
         percent.left = superial ? 400 : 650;
         percent.right = superial ? 42 : 0;
         break;
      case 8:
         percent.left = superial ? 400 : 600;
         percent.right = superial ? 60 : 0;
         break;
      case 9:
         percent.left = superial ? 370 : 550;
         percent.right = superial ? 95 : 0;
         break;
      case 10:
         percent.left = superial ? 350 : 500;
         percent.right = superial ? 130 : 0;
         break;
      case 11:
         percent.left = superial ? 350 : 450;
         percent.right = superial ? 163 : 0;
         break;
      case 12:
         percent.left = superial ? 30 : 400;
         percent.right = superial ? 485 : 6;
         break;
      case 13:
         percent.left = superial ? 20 : 350;
         percent.right = superial ? 490 : 13;
         break;
      case 14:
         percent.left = superial ? 10 : 300;
         percent.right = superial ? 495 : 14;
         break;
      case 15:
         percent.left = 300;
         percent.right = 21;
         break;
      case 16:
         percent.left = 300;
         percent.right = 21;
         break;
      case 17:
         percent.left = 300;
         percent.right = 21;
         break;
      case 18:
         percent.left = 300;
         percent.right = 21;
         break;
      case 19:
         percent.left = 300;
         percent.right = 21;
         break;
      case 20:
         percent.left = 300;
         percent.right = 70;
         break;
      case 21:
         percent.left = 300;
         percent.right = 70;
         break;
      case 22:
         percent.left = 30;
         percent.right = 194;
         break;
      case 23:
         percent.left = 20;
         percent.right = 294;
         break;
      case 24:
         percent.left = 10;
         percent.right = 999;
      }

      return percent;
   }

   public static long StarForceMeso(Equip item) {
      long base = 0L;
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Integer ReqLevel = (Integer)ii.getEquipStats(item.getItemId()).get("reqLevel");
      if (ReqLevel == null) {
         ReqLevel = 0;
      }

      int enhance = item.getEnhance();
      if ((Boolean)ii.isSuperial(item.getItemId()).right) {
         String var6 = (String)ii.isSuperial(item.getItemId()).left;
         byte var7 = -1;
         switch(var6.hashCode()) {
         case -1789830892:
            if (var6.equals("Tilent")) {
               var7 = 2;
            }
            break;
         case -730667576:
            if (var6.equals("Helisium")) {
               var7 = 0;
            }
            break;
         case 2434124:
            if (var6.equals("Nova")) {
               var7 = 1;
            }
         }

         switch(var7) {
         case 0:
            return (long)(5956600 + 0 * enhance);
         case 1:
            return (long)(18507900 + 0 * enhance);
         case 2:
            return (long)(55832200 + 0 * enhance);
         }
      } else {
         if (ReqLevel < 110) {
            base = 41000L;
            return base + (long)(enhance * '鱀');
         }

         if (ReqLevel >= 110 && ReqLevel < 120) {
            if (enhance <= 0) {
               return 54200L;
            }

            if (enhance <= 1) {
               return 107500L;
            }

            if (enhance <= 2) {
               return 160700L;
            }

            if (enhance <= 3) {
               return 214000L;
            }

            if (enhance <= 4) {
               return 267200L;
            }

            if (enhance <= 5) {
               return 320400L;
            }

            if (enhance <= 6) {
               return 373700L;
            }

            if (enhance <= 7) {
               return 426900L;
            }

            if (enhance <= 8) {
               return 480200L;
            }

            if (enhance <= 9) {
               return 533400L;
            }
         } else if (ReqLevel >= 120 && ReqLevel < 130) {
            if (enhance <= 0) {
               return 70100L;
            }

            if (enhance <= 1) {
               return 139200L;
            }

            if (enhance <= 2) {
               return 208400L;
            }

            if (enhance <= 3) {
               return 277500L;
            }

            if (enhance <= 4) {
               return 346600L;
            }

            if (enhance <= 5) {
               return 415700L;
            }

            if (enhance <= 6) {
               return 484800L;
            }

            if (enhance <= 7) {
               return 554000L;
            }

            if (enhance <= 8) {
               return 623100L;
            }

            if (enhance <= 9) {
               return 692200L;
            }

            if (enhance <= 10) {
               return 2801600L;
            }

            if (enhance <= 11) {
               return 3543200L;
            }

            if (enhance <= 12) {
               return 4397700L;
            }

            if (enhance <= 13) {
               return 5371700L;
            }

            if (enhance <= 14) {
               return 6471400L;
            }
         } else if (ReqLevel >= 130 && ReqLevel < 140) {
            if (enhance <= 0) {
               return 88900L;
            }

            if (enhance <= 1) {
               return 176800L;
            }

            if (enhance <= 2) {
               return 264600L;
            }

            if (enhance <= 3) {
               return 352500L;
            }

            if (enhance <= 4) {
               return 440400L;
            }

            if (enhance <= 5) {
               return 528300L;
            }

            if (enhance <= 6) {
               return 616200L;
            }

            if (enhance <= 7) {
               return 704000L;
            }

            if (enhance <= 8) {
               return 791900L;
            }

            if (enhance <= 9) {
               return 879800L;
            }

            if (enhance <= 10) {
               return 3561700L;
            }

            if (enhance <= 11) {
               return 4504600L;
            }

            if (enhance <= 12) {
               return 5591100L;
            }

            if (enhance <= 13) {
               return 6829300L;
            }

            if (enhance <= 14) {
               return 8227500L;
            }

            if (enhance <= 15) {
               return 19586000L;
            }

            if (enhance <= 16) {
               return 23069100L;
            }

            if (enhance <= 17) {
               return 26918600L;
            }

            if (enhance <= 18) {
               return 31149300L;
            }

            if (enhance <= 19) {
               return 35776100L;
            }
         } else if (ReqLevel >= 140 && ReqLevel < 150) {
            if (enhance <= 0) {
               return 110800L;
            }

            if (enhance <= 1) {
               return 220500L;
            }

            if (enhance <= 2) {
               return 330300L;
            }

            if (enhance <= 3) {
               return 440000L;
            }

            if (enhance <= 4) {
               return 549800L;
            }

            if (enhance <= 5) {
               return 659600L;
            }

            if (enhance <= 6) {
               return 769300L;
            }

            if (enhance <= 7) {
               return 879100L;
            }

            if (enhance <= 8) {
               return 988800L;
            }

            if (enhance <= 9) {
               return 1098600L;
            }

            if (enhance <= 10) {
               return 4448200L;
            }

            if (enhance <= 11) {
               return 5625900L;
            }

            if (enhance <= 12) {
               return 6982900L;
            }

            if (enhance <= 13) {
               return 8529400L;
            }

            if (enhance <= 14) {
               return 10275700L;
            }

            if (enhance <= 15) {
               return 24462200L;
            }

            if (enhance <= 16) {
               return 28812500L;
            }

            if (enhance <= 17) {
               return 33620400L;
            }

            if (enhance <= 18) {
               return 38904500L;
            }

            if (enhance <= 19) {
               return 44683300L;
            }

            if (enhance <= 20) {
               return 50974700L;
            }

            if (enhance <= 21) {
               return 57796700L;
            }

            if (enhance <= 22) {
               return 65166700L;
            }

            if (enhance <= 23) {
               return 73102200L;
            }

            if (enhance <= 24) {
               return 81620200L;
            }
         } else if (ReqLevel >= 150 && ReqLevel < 160) {
            if (enhance <= 0) {
               return 136000L;
            }

            if (enhance <= 1) {
               return 271000L;
            }

            if (enhance <= 2) {
               return 406000L;
            }

            if (enhance <= 3) {
               return 541000L;
            }

            if (enhance <= 4) {
               return 676000L;
            }

            if (enhance <= 5) {
               return 811000L;
            }

            if (enhance <= 6) {
               return 946000L;
            }

            if (enhance <= 7) {
               return 1081000L;
            }

            if (enhance <= 8) {
               return 1216000L;
            }

            if (enhance <= 9) {
               return 1351000L;
            }

            if (enhance <= 10) {
               return 5470800L;
            }

            if (enhance <= 11) {
               return 6919400L;
            }

            if (enhance <= 12) {
               return 8588400L;
            }

            if (enhance <= 13) {
               return 10490600L;
            }

            if (enhance <= 14) {
               return 12638500L;
            }

            if (enhance <= 15) {
               return 30087200L;
            }

            if (enhance <= 16) {
               return 35437900L;
            }

            if (enhance <= 17) {
               return 41351400L;
            }

            if (enhance <= 18) {
               return 47850600L;
            }

            if (enhance <= 19) {
               return 54958200L;
            }

            if (enhance <= 20) {
               return 62696400L;
            }

            if (enhance <= 21) {
               return 71087200L;
            }

            if (enhance <= 22) {
               return 80152000L;
            }

            if (enhance <= 23) {
               return 89912300L;
            }

            if (enhance <= 24) {
               return 100389000L;
            }
         } else if (ReqLevel >= 160 && ReqLevel < 170) {
            if (enhance <= 0) {
               return 164800L;
            }

            if (enhance <= 1) {
               return 328700L;
            }

            if (enhance <= 2) {
               return 492500L;
            }

            if (enhance <= 3) {
               return 656400L;
            }

            if (enhance <= 4) {
               return 820200L;
            }

            if (enhance <= 5) {
               return 984000L;
            }

            if (enhance <= 6) {
               return 1147900L;
            }

            if (enhance <= 7) {
               return 1311700L;
            }

            if (enhance <= 8) {
               return 1475600L;
            }

            if (enhance <= 9) {
               return 1639400L;
            }

            if (enhance <= 10) {
               return 6639400L;
            }

            if (enhance <= 11) {
               return 8397300L;
            }

            if (enhance <= 12) {
               return 10422900L;
            }

            if (enhance <= 13) {
               return 12731500L;
            }

            if (enhance <= 14) {
               return 15338200L;
            }

            if (enhance <= 15) {
               return 36514500L;
            }

            if (enhance <= 16) {
               return 43008300L;
            }

            if (enhance <= 17) {
               return 50185100L;
            }

            if (enhance <= 18) {
               return 58072700L;
            }

            if (enhance <= 19) {
               return 66698700L;
            }

            if (enhance <= 20) {
               return 76090000L;
            }

            if (enhance <= 21) {
               return 86273300L;
            }

            if (enhance <= 22) {
               return 97274600L;
            }

            if (enhance <= 23) {
               return 89912300L;
            }

            if (enhance <= 24) {
               return 100389000L;
            }
         } else if (ReqLevel >= 170) {
            if (enhance <= 0) {
               return 321000L;
            }

            if (enhance <= 1) {
               return 641000L;
            }

            if (enhance <= 2) {
               return 961000L;
            }

            if (enhance <= 3) {
               return 1281000L;
            }

            if (enhance <= 4) {
               return 1601000L;
            }

            if (enhance <= 5) {
               return 1921000L;
            }

            if (enhance <= 6) {
               return 2241000L;
            }

            if (enhance <= 7) {
               return 2561000L;
            }

            if (enhance <= 8) {
               return 2881000L;
            }

            if (enhance <= 9) {
               return 3201000L;
            }

            if (enhance <= 10) {
               return 12966500L;
            }

            if (enhance <= 11) {
               return 16400100L;
            }

            if (enhance <= 12) {
               return 20356300L;
            }

            if (enhance <= 13) {
               return 24865300L;
            }

            if (enhance <= 14) {
               return 29956500L;
            }

            if (enhance <= 15) {
               return 71316500L;
            }

            if (enhance <= 16) {
               return 83999600L;
            }

            if (enhance <= 17) {
               return 98016700L;
            }

            if (enhance <= 18) {
               return 113422300L;
            }

            if (enhance <= 19) {
               return 130270000L;
            }

            if (enhance <= 20) {
               return 148612400L;
            }

            if (enhance <= 21) {
               return 168501500L;
            }

            if (enhance <= 22) {
               return 189988600L;
            }

            if (enhance <= 23) {
               return 213124000L;
            }

            if (enhance <= 24) {
               return 237957700L;
            }
         }
      }

      return 0L;
   }

   private static void setJuhun(int level, boolean weapon) {
      switch(level / 10) {
      case 1:
         usejuhun[0] = 2;
         usejuhun[1] = 3;
         usejuhun[2] = 4;
         usejuhun[3] = 5;
         break;
      case 2:
         usejuhun[0] = 3;
         usejuhun[1] = 4;
         usejuhun[2] = 5;
         usejuhun[3] = 6;
         break;
      case 3:
         usejuhun[0] = 5;
         usejuhun[1] = 7;
         usejuhun[2] = 8;
         usejuhun[3] = 10;
         break;
      case 4:
         usejuhun[0] = 6;
         usejuhun[1] = 8;
         usejuhun[2] = 10;
         usejuhun[3] = 12;
         break;
      case 5:
         usejuhun[0] = 8;
         usejuhun[1] = 10;
         usejuhun[2] = 12;
         usejuhun[3] = 14;
         break;
      case 6:
         usejuhun[0] = 9;
         usejuhun[1] = 12;
         usejuhun[2] = 14;
         usejuhun[3] = 17;
         break;
      case 7:
         usejuhun[0] = 11;
         usejuhun[1] = 14;
         usejuhun[2] = 17;
         usejuhun[3] = 20;
         break;
      case 8:
         usejuhun[0] = 23;
         usejuhun[1] = 30;
         usejuhun[2] = 36;
         usejuhun[3] = 43;
         break;
      case 9:
         usejuhun[0] = 29;
         usejuhun[1] = 38;
         usejuhun[2] = 46;
         usejuhun[3] = 55;
         break;
      case 10:
         usejuhun[0] = 36;
         usejuhun[1] = 47;
         usejuhun[2] = 56;
         usejuhun[3] = 67;
         break;
      case 11:
         usejuhun[0] = 43;
         usejuhun[1] = 56;
         usejuhun[2] = 67;
         usejuhun[3] = 80;
         break;
      case 12:
         if (!weapon) {
            usejuhun[0] = 95;
            usejuhun[1] = 120;
            usejuhun[2] = 145;
         } else {
            usejuhun[0] = 155;
            usejuhun[1] = 200;
            usejuhun[2] = 240;
            usejuhun[3] = 290;
         }
         break;
      case 13:
         if (!weapon) {
            usejuhun[0] = 120;
            usejuhun[1] = 155;
            usejuhun[2] = 190;
         } else {
            usejuhun[0] = 200;
            usejuhun[1] = 260;
            usejuhun[2] = 310;
            usejuhun[3] = 370;
         }
         break;
      case 14:
         if (!weapon) {
            usejuhun[0] = 150;
            usejuhun[1] = 195;
            usejuhun[2] = 230;
         } else {
            usejuhun[0] = 240;
            usejuhun[1] = 320;
            usejuhun[2] = 380;
            usejuhun[3] = 460;
         }
         break;
      case 15:
      case 16:
      case 20:
         if (!weapon) {
            usejuhun[0] = 185;
            usejuhun[1] = 240;
            usejuhun[2] = 290;
         } else {
            usejuhun[0] = 280;
            usejuhun[1] = 380;
            usejuhun[2] = 450;
            usejuhun[3] = 570;
         }
      case 17:
      case 18:
      case 19:
      }

   }
}
