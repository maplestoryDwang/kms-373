package handling.channel.handler;

import client.InnerSkillValueHolder;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleStat;
import client.MapleTrait;
import client.PlayerStats;
import client.SecondaryStat;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.custom.inventory.CustomItem;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import constants.CubeOption;
import constants.EdiCubeOption;
import constants.GameConstants;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.RecvPacketOpcode;
import handling.world.World;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import io.netty.channel.Channel;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import log.DBLogger;
import log.LogType;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.EventManager;
import scripting.NPCScriptManager;
import server.InnerAbillity;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.RandomRewards;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.StructRewardItem;
import server.Timer;
import server.enchant.EquipmentEnchant;
import server.enchant.StarForceStats;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.BossReward;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import server.shops.MapleShopFactory;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.BattleGroundPacket;
import tools.packet.BossRewardMeso;
import tools.packet.CField;
import tools.packet.CSPacket;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.PetPacket;
import tools.packet.SLFCGPacket;

public class InventoryHandler {
   public static final void ItemMove(LittleEndianAccessor slea, MapleClient c) {
      try {
         c.getPlayer().setScrolledPosition((short)0);
         slea.readInt();
         MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
         short src = slea.readShort();
         short dst = slea.readShort();
         short quantity = slea.readShort();
         if (src < 0 && dst > 0) {
            MapleInventoryManipulator.unequip(c, src, dst, type);
         } else if (dst < 0) {
            MapleInventoryManipulator.equip(c, src, dst, type);
         } else if (dst == 0) {
            MapleInventoryManipulator.drop(c, type, src, quantity);
         } else {
            MapleInventoryManipulator.move(c, type, src, dst);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public static final void SwitchBag(LittleEndianAccessor slea, MapleClient c) {
      c.getPlayer().setScrolledPosition((short)0);
      slea.readInt();
      short src = (short)slea.readInt();
      short dst = (short)slea.readInt();
      if (src >= 100 && dst >= 100) {
         MapleInventoryManipulator.move(c, MapleInventoryType.ETC, src, dst);
      }
   }

   public static final void MoveBag(LittleEndianAccessor slea, MapleClient c) {
      c.getPlayer().setScrolledPosition((short)0);
      slea.readInt();
      boolean srcFirst = slea.readInt() > 0;
      if (slea.readByte() != 4) {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         short dst = (short)slea.readInt();
         short src = slea.readShort();
         MapleInventoryManipulator.move(c, MapleInventoryType.ETC, srcFirst ? dst : src, srcFirst ? src : dst);
      }
   }

   public static final void ItemSort(LittleEndianAccessor slea, MapleClient c) {
      slea.readInt();
      c.getPlayer().setScrolledPosition((short)0);
      MapleInventoryType pInvType = MapleInventoryType.getByType(slea.readByte());
      if (pInvType == MapleInventoryType.UNDEFINED) {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         MapleInventory pInv = c.getPlayer().getInventory(pInvType);
         List<Item> itemMap = new LinkedList();
         Iterator var5 = pInv.list().iterator();

         while(var5.hasNext()) {
            Item item = (Item)var5.next();
            itemMap.add(item.copy());
         }

         List<Pair<Short, Short>> updateSlots = new ArrayList();

         for(int i = 1; i <= pInv.getSlotLimit(); ++i) {
            Item item2 = pInv.getItem((short)i);
            if (item2 == null) {
               Item nextItem = pInv.getItem(pInv.getNextItemSlot((short)i));
               if (nextItem != null) {
                  short oldPos = nextItem.getPosition();
                  pInv.removeItem(nextItem.getPosition());
                  short nextPos = pInv.addItem(nextItem);
                  updateSlots.add(new Pair(oldPos, nextPos));
               }
            }
         }

         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.moveInventoryItem(pInvType, updateSlots));
         c.getSession().writeAndFlush(CWvsContext.finishedSort(pInvType.getType()));
      }
   }

   public static final void ItemGather(LittleEndianAccessor slea, MapleClient c) {
      slea.readInt();
      c.getPlayer().setScrolledPosition((short)0);
      byte mode = slea.readByte();
      MapleInventoryType invType = MapleInventoryType.getByType(mode);
      MapleInventory inv = c.getPlayer().getInventory(invType);
      if (mode > MapleInventoryType.UNDEFINED.getType()) {
         List<Item> itemMap = new LinkedList();
         Iterator var6 = inv.list().iterator();

         Item itemStats;
         while(var6.hasNext()) {
            itemStats = (Item)var6.next();
            itemMap.add(itemStats.copy());
         }

         var6 = itemMap.iterator();

         while(var6.hasNext()) {
            itemStats = (Item)var6.next();
            MapleInventoryManipulator.removeFromSlot(c, invType, itemStats.getPosition(), itemStats.getQuantity(), true, false);
         }

         List<Item> sortedItems = sortItems(itemMap);
         Iterator var10 = sortedItems.iterator();

         while(var10.hasNext()) {
            Item item2 = (Item)var10.next();
            MapleInventoryManipulator.addFromDrop(c, item2, false, false, false, true);
         }
      }

      c.getSession().writeAndFlush(CWvsContext.finishedGather(mode));
   }

   private static final List<Item> sortItems(List<Item> passedMap) {
      List<Integer> itemIds = new ArrayList();
      Iterator var2 = passedMap.iterator();

      while(var2.hasNext()) {
         Item item = (Item)var2.next();
         itemIds.add(item.getItemId());
      }

      Collections.sort(itemIds);
      List<Item> sortedList = new LinkedList();
      Iterator var8 = itemIds.iterator();

      while(true) {
         while(var8.hasNext()) {
            Integer val = (Integer)var8.next();
            Iterator var5 = passedMap.iterator();

            while(var5.hasNext()) {
               Item item2 = (Item)var5.next();
               if (val == item2.getItemId()) {
                  sortedList.add(item2);
                  passedMap.remove(item2);
                  break;
               }
            }
         }

         return sortedList;
      }
   }

   public static final boolean UseRewardItem(short slot, int itemId, MapleClient c, MapleCharacter chr) {
      Item toUse = c.getPlayer().getInventory(GameConstants.getInventoryType(itemId)).getItem(slot);
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
         if (chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            Pair<Integer, List<StructRewardItem>> rewards = ii.getRewardItem(itemId);
            if (rewards != null && (Integer)rewards.getLeft() > 0) {
               StructRewardItem reward = null;

               while(true) {
                  Iterator iterator = ((List)rewards.getRight()).iterator();

                  while(iterator.hasNext()) {
                     reward = (StructRewardItem)iterator.next();
                     if (reward.prob > 0 && Randomizer.nextInt((Integer)rewards.getLeft()) < reward.prob) {
                        if (GameConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP) {
                           Item item = ii.getEquipById(reward.itemid);
                           if (reward.period > 0L) {
                              item.setExpiration(System.currentTimeMillis() + reward.period * 60L * 60L * 10L);
                           }

                           item.setGMLog("Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                           MapleInventoryManipulator.addbyItem(c, item);
                        } else {
                           MapleInventoryManipulator.addById(c, reward.itemid, reward.quantity, "Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                        }

                        MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemId), itemId, 1, false, false);
                        c.getSession().writeAndFlush(CField.EffectPacket.showRewardItemEffect(chr, reward.itemid, true, reward.effect));
                        chr.getMap().broadcastMessage(chr, CField.EffectPacket.showRewardItemEffect(chr, reward.itemid, false, reward.effect), false);
                        return true;
                     }
                  }
               }
            }

            int reward2;
            if (itemId == 2028154) {
               reward2 = 1113097 + Randomizer.rand(1, 31);
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType((byte)(toUse.getItemId() / 1000000)), slot, (short)1, false);
               Equip item2 = (Equip)ii.getEquipById(reward2);
               MapleInventoryManipulator.addbyItem(c, item2);
               c.getSession().writeAndFlush(CField.EffectPacket.showEffect(c.getPlayer(), reward2, 0, 38, 1, 0, (byte)0, true, (Point)null, "", (Item)null));
               c.getSession().writeAndFlush(CField.EffectPacket.showRewardItemEffect(c.getPlayer(), itemId, true, ""));
               if (item2.getBaseLevel() >= 4 && (reward2 == 1113098 || reward2 == 1113099 || reward2 >= 1113113 && reward2 <= 1113116 || reward2 == 1113122)) {
                  World.Broadcast.broadcastMessage(CWvsContext.serverMessage(11, c.getChannel(), c.getPlayer().getName(), c.getPlayer().getName() + "님이 상자에서 [" + ii.getName(reward2) + "] 아이템을 획득했습니다.", true, item2));
               }

               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else if (itemId == 2028272) {
               reward2 = RandomRewards.getTheSeedReward();
               if (reward2 == 0) {
                  c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(9000155, (byte)0, "아쉽지만, 꽝이 나왔습니다. 다음 기회에 다시 이용해주세요!", "00 00", (byte)0));
               } else if (reward2 == 1) {
                  chr.gainMeso(10000000L, true);
                  c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(9000155, (byte)0, "1천만메소를 획득하셨습니다!", "00 00", (byte)0));
               } else {
                  int max_quantity = 1;
                  switch(reward2) {
                  case 4001208:
                  case 4001209:
                  case 4001210:
                  case 4001211:
                  case 4001547:
                  case 4001548:
                  case 4001549:
                  case 4001550:
                  case 4001551:
                     max_quantity = 1;
                     break;
                  case 4310014:
                     max_quantity = 10;
                     break;
                  case 4310016:
                     max_quantity = 10;
                     break;
                  case 4310034:
                     max_quantity = 10;
                  }

                  c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(9000155, (byte)0, "축하드립니다!!\r\n돌림판에서 [#b#i" + reward2 + "##z" + reward2 + "#](이)가 나왔습니다.", "00 00", (byte)0));
                  c.getPlayer().gainItem(reward2, max_quantity);
                  c.getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(c.getPlayer(), reward2, 1, true, ""));
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               }
            } else if (itemId != 2028208 && itemId != 2028209) {
               chr.dropMessage(6, "아이템 보상 정보를 찾을 수 없습니다.");
            } else {
               NPCScriptManager.getInstance().startItem(c, 9000162, "consume_" + itemId);
            }
         } else {
            chr.dropMessage(6, "Insufficient inventory slot.");
         }
      }

      return false;
   }

   public static final void UseItem(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.isAlive() && chr.getBuffedEffect(SecondaryStat.DebuffIncHp) == null && chr.getMap() != null && !chr.hasDisease(SecondaryStat.StopPortion) && chr.getBuffedValue(SecondaryStat.StopPortion) == null) {
         try {
            long time = System.currentTimeMillis();
            slea.skip(4);
            short slot = slea.readShort();
            int itemId = slea.readInt();
            Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
            if (toUse.getItemId() != itemId) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
               if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr, true)) {
                  if (toUse.getItemId() != 2000054) {
                     MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
                  }

                  c.getSession().writeAndFlush(CField.potionCooldown());
               }
            } else {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            }
         } catch (Exception var8) {
            var8.printStackTrace();
         }

      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static final void UseReturnScroll(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr.isAlive() && chr.getMapId() != 749040100 && !chr.inPVP()) {
         slea.readInt();
         short slot = slea.readShort();
         int itemId = slea.readInt();
         Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
         if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
            String var10000 = FileoutputUtil.아이템사용로그;
            int var10001 = c.getAccID();
            FileoutputUtil.log(var10000, "[주문서 사용] 계정 아이디 : " + var10001 + " | " + c.getPlayer().getName() + "이 " + MapleItemInformationProvider.getInstance().getName(toUse.getItemId()) + "를 " + MapleItemInformationProvider.getInstance().getName(itemId) + "에 사용함.");
            if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
               if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyReturnScroll(chr)) {
                  MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
               } else {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               }
            } else {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            }

         } else {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         }
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static void UseMagnify(LittleEndianAccessor slea, MapleClient c) {
      try {
         slea.skip(4);
         boolean useGlass = false;
         boolean isEquipped = false;
         short useSlot = slea.readShort();
         short equSlot = slea.readShort();
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         Equip equip;
         if (equSlot < 0) {
            equip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(equSlot);
            isEquipped = true;
         } else {
            equip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(equSlot);
         }

         Item glass = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(useSlot);
         if (useSlot != 20000) {
            if (glass == null || equip == null) {
               c.getPlayer().dropMessage(1, "GLASS NULL!");
               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryFull());
               return;
            }

            useGlass = true;
         } else {
            long price = GameConstants.getMagnifyPrice(equip);
            c.getPlayer().gainMeso(-price, false);
         }

         int rank;
         if (equip.getState() == 1) {
            rank = Randomizer.nextInt(100);
            if (equip.getLines() == 0) {
               equip.setLines((byte)2);
            }

            if (rank < 3) {
               equip.setState((byte)18);
            } else if (rank < 1) {
               equip.setState((byte)19);
            } else {
               equip.setState((byte)17);
            }
         } else {
            equip.setState((byte)(equip.getState() + 16));
         }

         rank = equip.getState() - 16;
         equip.setPotential1(CubeOption.getRedCubePotentialId(equip.getItemId(), rank, 1));
         equip.setPotential2(CubeOption.getRedCubePotentialId(equip.getItemId(), rank, 2));
         if (equip.getLines() == 3) {
            equip.setPotential3(CubeOption.getRedCubePotentialId(equip.getItemId(), rank, 3, equip.getPotential1(), equip.getPotential2()));
         }

         if (GameConstants.isZero(c.getPlayer().getJob())) {
            Equip eq2;
            if (equSlot == -10) {
               eq2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
               if (eq2 != null) {
                  eq2.setState(equip.getState());
                  eq2.setLines(equip.getLines());
                  eq2.setPotential1(equip.getPotential1());
                  eq2.setPotential2(equip.getPotential2());
                  eq2.setPotential3(equip.getPotential3());
                  c.getPlayer().forceReAddItem(eq2, MapleInventoryType.EQUIPPED);
               }
            } else if (equSlot == -11) {
               eq2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
               if (eq2 != null) {
                  eq2.setState(equip.getState());
                  eq2.setLines(equip.getLines());
                  eq2.setPotential1(equip.getPotential1());
                  eq2.setPotential2(equip.getPotential2());
                  eq2.setPotential3(equip.getPotential3());
                  c.getPlayer().forceReAddItem(eq2, MapleInventoryType.EQUIPPED);
               }
            }
         }

         if (useGlass) {
            MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
            useInventory.removeItem(useSlot, (short)1, false);
         }

         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, equip));
         c.getPlayer().getTrait(MapleTrait.MapleTraitType.insight).addExp(10, c.getPlayer());
         c.getPlayer().getMap().broadcastMessage(CField.showMagnifyingEffect(c.getPlayer().getId(), equSlot));
         if (isEquipped) {
            c.getPlayer().forceReAddItem_NoUpdate(equip, MapleInventoryType.EQUIPPED);
         } else {
            c.getPlayer().forceReAddItem_NoUpdate(equip, MapleInventoryType.EQUIP);
         }

         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } catch (Exception var11) {
         var11.printStackTrace();
      }

   }

   public static int potential(int itemid, int level) {
      return potential(itemid, level, false);
   }

   public static int potential(int itemid, int level, boolean editional) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      int itemtype = itemid / 1000;
      return ii.getPotentialOptionID(Math.max(1, level), editional, itemtype);
   }

   public static void UseStamp(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      short slot = slea.readShort();
      short dst = slea.readShort();
      boolean sucstamp = false;
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Equip toStamp;
      if (dst < 0) {
         toStamp = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
      } else {
         toStamp = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
      }

      MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
      Item stamp = useInventory.getItem(slot);
      int temp;
      int level;
      if (GameConstants.isZero(c.getPlayer().getJob())) {
         Equip toStamp2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         if (Randomizer.isSuccess(ii.getSuccess(toStamp2.getItemId(), c.getPlayer(), toStamp2))) {
            toStamp2.setLines((byte)3);
            level = temp = toStamp2.getState() - 16;

            for(int var12 = 0; temp > 1; ++var12) {
               if (temp > 1) {
               }

               --temp;
            }

            toStamp2.setPotential3(potential(toStamp2.getItemId(), level != 1 && Randomizer.nextInt(100) >= 1 ? level - 1 : level));
            sucstamp = true;
         }

         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(stamp, toStamp2));
      }

      if (Randomizer.isSuccess(ii.getSuccess(toStamp.getItemId(), c.getPlayer(), toStamp))) {
         toStamp.setLines((byte)3);
         int temp2;
         temp = temp2 = toStamp.getState() - 16;

         for(level = 0; temp2 > 1; ++level) {
            if (temp2 > 1) {
            }

            --temp2;
         }

         toStamp.setPotential3(potential(toStamp.getItemId(), temp != 1 && Randomizer.nextInt(100) >= 1 ? temp - 1 : temp));
         sucstamp = true;
      }

      useInventory.removeItem(stamp.getPosition(), (short)1, false);
      c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(stamp, toStamp));
      c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), sucstamp, stamp.getItemId(), toStamp.getItemId()));
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static void UseEditionalStamp(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      short slot = slea.readShort();
      short dst = slea.readShort();
      boolean sucstamp = false;
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Equip toStamp;
      if (dst < 0) {
         toStamp = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
      } else {
         toStamp = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
      }

      MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
      Item stamp = useInventory.getItem(slot);
      if (GameConstants.isZero(c.getPlayer().getJob())) {
         Equip toStamp2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         int level = toStamp2.getState() - 16;
         if (Randomizer.isSuccess(ii.getSuccess(toStamp2.getItemId(), c.getPlayer(), toStamp2))) {
            toStamp2.setPotential6(potential(toStamp2.getItemId(), level != 1 && Randomizer.nextInt(100) >= 1 ? level - 1 : level, true));
            sucstamp = true;
         }

         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(stamp, toStamp));
      }

      if (Randomizer.isSuccess(ii.getSuccess(toStamp.getItemId(), c.getPlayer(), toStamp))) {
         int level2 = toStamp.getState() - 16;
         toStamp.setPotential6(potential(toStamp.getItemId(), level2 != 1 && Randomizer.nextInt(100) >= 1 ? level2 - 1 : level2, true));
         sucstamp = true;
      }

      useInventory.removeItem(stamp.getPosition(), (short)1, false);
      c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(stamp, toStamp));
      c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), sucstamp, stamp.getItemId(), toStamp.getItemId()));
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static void UseChooseCube(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      byte type = slea.readByte();
      Equip equip = null;
      Equip zeroequip = null;
      if (c.getPlayer().choicepotential.getPosition() > 0) {
         equip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(c.getPlayer().choicepotential.getPosition());
      } else {
         equip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(c.getPlayer().choicepotential.getPosition());
      }

      int cube = Integer.parseInt(c.getPlayer().getV("lastCube"));
      if (type == 6) {
         if (c.getPlayer().choicepotential.getPosition() > 0) {
            equip.set(c.getPlayer().choicepotential);
         } else {
            equip.set(c.getPlayer().choicepotential);
         }
      }

      if (GameConstants.isZeroWeapon(c.getPlayer().choicepotential.getItemId())) {
         zeroequip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         zeroequip.setState(equip.getState());
         zeroequip.setLines(equip.getLines());
         zeroequip.setPotential1(equip.getPotential1());
         zeroequip.setPotential2(equip.getPotential2());
         zeroequip.setPotential3(equip.getPotential3());
         zeroequip.setPotential4(equip.getPotential4());
         zeroequip.setPotential5(equip.getPotential5());
         zeroequip.setPotential6(equip.getPotential6());
      }

      c.getPlayer().choicepotential = null;
      c.getPlayer().memorialcube = null;
      if (zeroequip != null) {
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIPPED, equip));
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIPPED, zeroequip));
         c.getPlayer().forceReAddItem(equip, MapleInventoryType.EQUIPPED);
      } else {
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, equip));
         c.getPlayer().forceReAddItem(equip, MapleInventoryType.EQUIP);
      }

   }

   public static final void addToScrollLog(int accountID, int charID, int scrollID, int itemID, byte oldSlots, byte newSlots, byte viciousHammer, String result, boolean ws, boolean ls, int vega) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO scroll_log VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         ps.setInt(1, accountID);
         ps.setInt(2, charID);
         ps.setInt(3, scrollID);
         ps.setInt(4, itemID);
         ps.setByte(5, oldSlots);
         ps.setByte(6, newSlots);
         ps.setByte(7, viciousHammer);
         ps.setString(8, result);
         ps.setByte(9, (byte)(ws ? 1 : 0));
         ps.setByte(10, (byte)(ls ? 1 : 0));
         ps.setInt(11, vega);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var26) {
         FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var26);

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
         } catch (SQLException var25) {
            var25.printStackTrace();
         }
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
         } catch (SQLException var24) {
            var24.printStackTrace();
         }

      }

   }

   public static void useSilverKarma(LittleEndianAccessor slea, MapleCharacter chr) {
      slea.skip(4);
      Item scroll = chr.getInventory(MapleInventoryType.USE).getItem(slea.readShort());
      Item toScroll = chr.getInventory(MapleInventoryType.getByType((byte)slea.readShort())).getItem(slea.readShort());
      if (scroll.getItemId() == 2720000 || scroll.getItemId() == 2720001) {
         if (!MapleItemInformationProvider.getInstance().isKarmaEnabled(toScroll.getItemId())) {
            chr.dropMessage(5, "가위를 사용할 수 없는 아이템입니다.");
            return;
         }

         if (toScroll.getType() == 1) {
            Equip nEquip = (Equip)toScroll;
            if (nEquip.getKarmaCount() > 0) {
               nEquip.setKarmaCount((byte)(nEquip.getKarmaCount() - 1));
            } else if (nEquip.getKarmaCount() == 0) {
               chr.dropMessage(5, "가위를 사용할 수 없는 아이템입니다.");
               return;
            }
         }

         int flag = toScroll.getFlag();
         if (toScroll.getType() == 1) {
            flag += ItemFlag.KARMA_EQUIP.getValue();
         } else {
            flag += ItemFlag.KARMA_USE.getValue();
         }

         toScroll.setFlag(flag);
      }

      chr.removeItem(scroll.getItemId(), -1);
      chr.getClient().getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, GameConstants.getInventoryType(toScroll.getItemId()), toScroll));
   }

   public static boolean UseUpgradeScroll(RecvPacketOpcode header, short slot, short dst, byte ws, MapleClient c, MapleCharacter chr) {
      boolean whiteScroll = false;
      boolean legendarySpirit = false;
      boolean recovery = false;
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Equip toScroll = null;
      Equip toScroll2 = null;
      Item scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
      if (dst < 0) {
         toScroll = (Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
         if (GameConstants.isZero(chr.getJob())) {
            if (toScroll.getPosition() == -11) {
               toScroll2 = (Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
            } else {
               toScroll2 = (Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            }
         }
      } else {
         toScroll = (Equip)chr.getInventory(MapleInventoryType.EQUIP).getItem(dst);
      }

      if (ii.getName(scroll.getItemId()).contains("펫장비") && dst > 0) {
         toScroll = (Equip)chr.getInventory(MapleInventoryType.CODY).getItem(dst);
      }

      if (toScroll == null) {
         return false;
      } else {
         byte oldLevel = toScroll.getLevel();
         byte oldEnhance = toScroll.getEnhance();
         byte oldState = toScroll.getState();
         int oldFlag = toScroll.getFlag();
         byte oldSlots = toScroll.getUpgradeSlots();
         if (scroll != null && header != RecvPacketOpcode.USE_FLAG_SCROLL) {
            if (!GameConstants.isSpecialScroll(scroll.getItemId()) && !GameConstants.isCleanSlate(scroll.getItemId()) && !GameConstants.isEquipScroll(scroll.getItemId()) && !GameConstants.isPotentialScroll(scroll.getItemId()) && !GameConstants.isRebirthFireScroll(scroll.getItemId()) && scroll.getItemId() / 10000 != 204 && scroll.getItemId() / 10000 != 272 && scroll.getItemId() / 10000 != 264) {
               scroll = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
            }
         } else {
            scroll = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
         }

         if (scroll == null) {
            c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
            chr.dropMessage(1, "존재하지 않는 주문서입니다.");
            return false;
         } else {
            MapleItemInformationProvider ii2 = MapleItemInformationProvider.getInstance();
            if (scroll != null && toScroll != null) {
               String var10000 = FileoutputUtil.아이템사용로그;
               int var10001 = c.getAccID();
               FileoutputUtil.log(var10000, "[주문서 사용] 계정 아이디 : " + var10001 + " | " + c.getPlayer().getName() + "이 " + ii2.getName(scroll.getItemId()) + "를 " + ii2.getName(toScroll.getItemId()) + "에 사용함.");
            }

            if (scroll.getItemId() / 100 == 20496) {
               Equip origin = (Equip)MapleItemInformationProvider.getInstance().getEquipById(toScroll.getItemId());
               toScroll.setAcc(origin.getAcc());
               toScroll.setAvoid(origin.getAvoid());
               toScroll.setDex(origin.getDex());
               toScroll.setHands(origin.getHands());
               toScroll.setHp(origin.getHp());
               toScroll.setInt(origin.getInt());
               toScroll.setJump(origin.getJump());
               toScroll.setLevel(origin.getLevel());
               toScroll.setLuk(origin.getLuk());
               toScroll.setMatk(origin.getMatk());
               toScroll.setMdef(origin.getMdef());
               toScroll.setMp(origin.getMp());
               toScroll.setSpeed(origin.getSpeed());
               toScroll.setStr(origin.getStr());
               toScroll.setUpgradeSlots(origin.getUpgradeSlots());
               toScroll.setWatk(origin.getWatk());
               toScroll.setWdef(origin.getWdef());
               toScroll.setEnhance((byte)0);
               toScroll.setViciousHammer((byte)0);
               chr.getInventory(MapleInventoryType.USE).removeItem(scroll.getPosition());
               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(scroll, toScroll));
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.getScrollEffect(c.getPlayer().getId(), Equip.ScrollResult.SUCCESS, false, scroll.getItemId(), toScroll.getItemId()), true);
               return false;
            } else {
               if (!GameConstants.isSpecialScroll(scroll.getItemId()) && !GameConstants.isCleanSlate(scroll.getItemId()) && !GameConstants.isEquipScroll(scroll.getItemId()) && scroll.getItemId() != 2049360 && scroll.getItemId() != 2049361 && !GameConstants.isPotentialScroll(scroll.getItemId()) && !GameConstants.isRebirthFireScroll(scroll.getItemId()) && !GameConstants.isLuckyScroll(scroll.getItemId())) {
                  if (toScroll.getUpgradeSlots() < 1 && scroll.getItemId() != 2644001 && scroll.getItemId() != 2644002 && scroll.getItemId() != 2644004 && scroll.getItemId() != 2049371 && scroll.getItemId() != 2049372 && GameConstants.isStarForceScroll(scroll.getItemId()) < 0) {
                     c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                     chr.dropMessage(1, "업그레이드 슬롯이 부족합니다.");
                     return false;
                  }
               } else if (GameConstants.isEquipScroll(scroll.getItemId()) && (scroll.getItemId() != 2049360 && scroll.getItemId() != 2049361 && toScroll.getUpgradeSlots() >= 1 || toScroll.getEnhance() >= 15 || ii.isCash(toScroll.getItemId()))) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                  chr.dropMessage(1, "더 이상 강화할 수 없는 아이템입니다.");
                  return false;
               }

               if ((scroll.getItemId() == 2049166 || scroll.getItemId() == 2049167) && !GameConstants.isWeapon(toScroll.getItemId())) {
                  chr.dropMessage(1, "해당 아이템에 주문서를 사용할 수 없습니다.");
                  chr.getClient().send(CWvsContext.enableActions(chr));
                  return false;
               } else if (toScroll.getItemId() / 1000 != 1672 && !GameConstants.canScroll(toScroll.getItemId()) && GameConstants.isChaosScroll(scroll.getItemId())) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                  chr.dropMessage(1, "주문서를 사용하실 수 없는 아이템입니다.");
                  return false;
               } else {
                  if (ii.isCash(toScroll.getItemId())) {
                     if (toScroll.getItemId() / 1000 != 1802) {
                        c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                        chr.dropMessage(1, "캐시 아이템은 강화가 불가능합니다.");
                        return false;
                     }

                     if (!ii.getName(scroll.getItemId()).contains("펫장비")) {
                        c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                        chr.dropMessage(1, "펫장비에는 펫장비 주문서만 사용하실 수 있습니다.");
                        return false;
                     }
                  }

                  if (scroll.getItemId() == 2049135 && (toScroll.getItemId() < 1182000 || toScroll.getItemId() > 1182005)) {
                     chr.dropMessage(1, "여명 아이템에만 사용하실 수 있습니다.");
                     chr.getClient().send(CWvsContext.enableActions(chr));
                     return false;
                  } else if (GameConstants.isTablet(scroll.getItemId()) && toScroll.getDurability() < 0) {
                     c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                     chr.dropMessage(1, "연성서를 사용하실 수 없는 아이템입니다.");
                     return false;
                  } else {
                     Item wscroll = null;
                     List<Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
                     if (scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
                        c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                        chr.dropMessage(1, "RETURN 8");
                        return false;
                     } else {
                        if (whiteScroll) {
                           wscroll = chr.getInventory(MapleInventoryType.USE).findById(2340000);
                           if (wscroll == null) {
                              whiteScroll = false;
                           }
                        }

                        if (scroll.getItemId() == 2041200 && toScroll.getItemId() != 1122000 && toScroll.getItemId() != 1122076) {
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                           chr.dropMessage(1, "드래곤의 돌은 혼테일의 목걸이에만 사용 가능한 아이템입니다.");
                           return false;
                        } else if (scroll.getItemId() != 2046856 && scroll.getItemId() != 2046857 || toScroll.getItemId() / 1000 != 1152 && GameConstants.isAccessory(toScroll.getItemId())) {
                           if ((scroll.getItemId() == 2049166 || scroll.getItemId() == 2046991 || scroll.getItemId() == 2046992 || scroll.getItemId() == 2046996 || scroll.getItemId() == 2046997) && GameConstants.isTwoHanded(toScroll.getItemId())) {
                              c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                              chr.dropMessage(1, "한손무기에만 사용 가능한 주문서입니다.");
                              return false;
                           } else if ((scroll.getItemId() == 2049167 || scroll.getItemId() == 2047814 || scroll.getItemId() == 2047818) && !GameConstants.isTwoHanded(toScroll.getItemId()) && toScroll.getItemId() / 1000 != 1672) {
                              c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                              chr.dropMessage(1, "두손무기에만 사용 가능한 주문서입니다.");
                              return false;
                           } else if (GameConstants.isAccessoryScroll(scroll.getItemId()) && !GameConstants.isAccessory(toScroll.getItemId())) {
                              c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                              chr.dropMessage(1, "악세서리 주문서를 사용하실 수 없는 아이템입니다.");
                              return false;
                           } else if (toScroll.getUpgradeSlots() > 0 && scroll.getItemId() >= 2049370 && scroll.getItemId() <= 2049377) {
                              c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                              chr.dropMessage(1, "아직 업그레이드 슬롯이 남아있습니다.");
                              return false;
                           } else if (scroll.getQuantity() <= 0) {
                              chr.dropMessage(1, "존재하지 않는 주문서는 사용할 수 없습니다.");
                              return false;
                           } else {
                              if (ItemFlag.RETURN_SCROLL.check(toScroll.getFlag())) {
                                 chr.returnSc = scroll.getItemId();
                                 chr.returnscroll = (Equip)toScroll.copy();
                              }

                              if (header == RecvPacketOpcode.USE_BLACK_REBIRTH_SCROLL) {
                                 long newRebirth = toScroll.newRebirth(ii.getReqLevel(toScroll.getItemId()), scroll.getItemId(), false);
                                 c.getSession().writeAndFlush(CWvsContext.useBlackRebirthScroll(toScroll, scroll, newRebirth, false));
                                 MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(scroll.getItemId()), scroll.getPosition(), (short)1, false, false);
                                 c.getSession().writeAndFlush(CWvsContext.blackRebirthResult(true, toScroll.getFire(), toScroll));
                                 c.getSession().writeAndFlush(CWvsContext.blackRebirthResult(false, newRebirth, toScroll));
                                 chr.blackRebirth = newRebirth;
                                 chr.blackRebirthScroll = (Equip)toScroll.copy();
                                 chr.blackRebirthPos = slot;
                                 return false;
                              } else {
                                 Equip scrolled = (Equip)ii.scrollEquipWithId(toScroll, scroll, whiteScroll, chr);
                                 Equip.ScrollResult scrollSuccess;
                                 if (scrolled == null) {
                                    scrollSuccess = Equip.ScrollResult.CURSE;
                                 } else if (GameConstants.isRebirthFireScroll(scroll.getItemId())) {
                                    scrollSuccess = Equip.ScrollResult.SUCCESS;
                                 } else if (scrolled.getLevel() <= oldLevel && scrolled.getEnhance() <= oldEnhance && scrolled.getState() == oldState && scrolled.getFlag() <= oldFlag) {
                                    if (GameConstants.isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() > oldSlots) {
                                       scrollSuccess = Equip.ScrollResult.SUCCESS;
                                    } else {
                                       scrollSuccess = Equip.ScrollResult.FAIL;
                                       if (ItemFlag.RECOVERY_SHIELD.check(toScroll.getFlag())) {
                                          recovery = true;
                                       }
                                    }
                                 } else {
                                    scrollSuccess = Equip.ScrollResult.SUCCESS;
                                 }

                                 if (recovery) {
                                    chr.dropMessage(5, "주문서의 효과로 사용된 주문서가 차감되지 않았습니다.");
                                 } else if (GameConstants.isZero(chr.getJob()) && toScroll.getPosition() == -11) {
                                    chr.getInventory(GameConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short)1, false);
                                 } else {
                                    chr.getInventory(GameConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short)1, false);
                                 }

                                 if (scrollSuccess == Equip.ScrollResult.SUCCESS) {
                                    EquipmentEnchant.checkEquipmentStats(c, toScroll);
                                    if (toScroll2 != null) {
                                       EquipmentEnchant.checkEquipmentStats(c, toScroll2);
                                    }
                                 }

                                 if (whiteScroll) {
                                    MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(scroll.getItemId()), wscroll.getPosition(), (short)1, false, false);
                                 }

                                 if (header != RecvPacketOpcode.USE_FLAG_SCROLL) {
                                    if (ItemFlag.RECOVERY_SHIELD.check(toScroll.getFlag())) {
                                       toScroll.setFlag(toScroll.getFlag() - ItemFlag.RECOVERY_SHIELD.getValue());
                                       if (GameConstants.isZero(chr.getJob()) && toScroll2 != null) {
                                          toScroll2.setFlag(toScroll2.getFlag() - ItemFlag.RECOVERY_SHIELD.getValue());
                                       }
                                    }

                                    if (ItemFlag.SAFETY_SHIELD.check(toScroll.getFlag())) {
                                       toScroll.setFlag(toScroll.getFlag() - ItemFlag.SAFETY_SHIELD.getValue());
                                       if (GameConstants.isZero(chr.getJob()) && toScroll2 != null) {
                                          toScroll2.setFlag(toScroll2.getFlag() - ItemFlag.SAFETY_SHIELD.getValue());
                                       }
                                    }

                                    if (ItemFlag.PROTECT_SHIELD.check(toScroll.getFlag())) {
                                       toScroll.setFlag(toScroll.getFlag() - ItemFlag.PROTECT_SHIELD.getValue());
                                       if (GameConstants.isZero(chr.getJob()) && toScroll2 != null) {
                                          toScroll2.setFlag(toScroll2.getFlag() - ItemFlag.PROTECT_SHIELD.getValue());
                                       }
                                    }

                                    if (ItemFlag.LUCKY_PROTECT_SHIELD.check(toScroll.getFlag())) {
                                       toScroll.setFlag(toScroll.getFlag() - ItemFlag.LUCKY_PROTECT_SHIELD.getValue());
                                       if (GameConstants.isZero(chr.getJob()) && toScroll2 != null) {
                                          toScroll2.setFlag(toScroll2.getFlag() - ItemFlag.LUCKY_PROTECT_SHIELD.getValue());
                                       }
                                    }
                                 }

                                 if (scrollSuccess == Equip.ScrollResult.CURSE) {
                                    c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(scroll, toScroll, true));
                                    if (dst < 0) {
                                       chr.getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
                                    } else {
                                       chr.getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
                                    }
                                 } else {
                                    c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(scroll, toScroll, false));
                                    if (toScroll2 != null) {
                                       c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(scroll, toScroll2, false));
                                    }

                                    if (!GameConstants.isSpecialScroll(scroll.getItemId()) && !GameConstants.isCleanSlate(scroll.getItemId()) && !GameConstants.isEquipScroll(scroll.getItemId()) && scroll.getItemId() != 2049360 && scroll.getItemId() != 2049361 && !GameConstants.isPotentialScroll(scroll.getItemId()) && !GameConstants.isRebirthFireScroll(scroll.getItemId()) && !GameConstants.isLuckyScroll(scroll.getItemId()) && c.getPlayer().returnscroll != null && scrollSuccess == Equip.ScrollResult.SUCCESS) {
                                       c.getSession().writeAndFlush(CWvsContext.returnEffectConfirm(c.getPlayer().returnscroll, scroll.getItemId()));
                                       c.getSession().writeAndFlush(CWvsContext.returnEffectModify(c.getPlayer().returnscroll, scroll.getItemId()));
                                    } else if (!GameConstants.isSpecialScroll(scroll.getItemId()) && !GameConstants.isCleanSlate(scroll.getItemId()) && !GameConstants.isEquipScroll(scroll.getItemId()) && scroll.getItemId() != 2049360 && scroll.getItemId() != 2049361 && !GameConstants.isPotentialScroll(scroll.getItemId()) && !GameConstants.isRebirthFireScroll(scroll.getItemId()) && !GameConstants.isLuckyScroll(scroll.getItemId()) && c.getPlayer().returnscroll != null && scrollSuccess == Equip.ScrollResult.FAIL) {
                                       c.getPlayer().returnscroll = null;
                                       toScroll.setFlag(toScroll.getFlag() - ItemFlag.RETURN_SCROLL.getValue());
                                       toScroll.setUpgradeSlots((byte)(toScroll.getUpgradeSlots() + 1));
                                       chr.getClient().getSession().writeAndFlush(CWvsContext.InventoryPacket.updateEquipSlot(toScroll));
                                    }
                                 }

                                 if (GameConstants.isZero(chr.getJob()) && toScroll.getPosition() == -11) {
                                    chr.getMap().broadcastMessage(chr, CField.getScrollEffect(c.getPlayer().getId(), scrollSuccess, false, scroll.getItemId(), toScroll.getItemId()), true);
                                 } else {
                                    chr.getMap().broadcastMessage(chr, CField.getScrollEffect(c.getPlayer().getId(), scrollSuccess, false, scroll.getItemId(), toScroll.getItemId()), true);
                                 }

                                 if (scrolled.getShowScrollOption() != null) {
                                    chr.getClient().getSession().writeAndFlush(CField.showScrollOption(scrolled.getItemId(), scroll.getItemId(), scrolled.getShowScrollOption()));
                                    scrolled.setShowScrollOption((StarForceStats)null);
                                 }

                                 if (dst < 0 && (scrollSuccess == Equip.ScrollResult.SUCCESS || scrollSuccess == Equip.ScrollResult.CURSE)) {
                                    chr.equipChanged();
                                 }

                                 if (header == RecvPacketOpcode.USE_REBIRTH_SCROLL) {
                                    chr.getClient().send(CWvsContext.RebirthScrollWindow(scroll.getItemId(), toScroll.getPosition()));
                                 }

                                 c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                                 return true;
                              }
                           }
                        } else {
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                           chr.dropMessage(1, "악세서리에만 사용 가능한 주문서입니다.");
                           return false;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void UseEditionalScroll(LittleEndianAccessor slea, MapleClient c) {
      try {
         slea.skip(4);
         short mode = slea.readShort();
         Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(mode);
         if (toUse.getItemId() >= 2048305 && toUse.getItemId() <= 2048316) {
            short slot = slea.readShort();
            Item item;
            if (slot < 0) {
               item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
            } else {
               item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
            }

            Equip zeroEquip = null;
            if (GameConstants.isAlphaWeapon(item.getItemId())) {
               zeroEquip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
            } else if (GameConstants.isBetaWeapon(item.getItemId())) {
               zeroEquip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            }

            boolean option3_sbal;
            boolean level;
            if (GameConstants.isZero(c.getPlayer().getJob()) && zeroEquip != null) {
               Item item2 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
               Item item3 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
               Equip eq1 = (Equip)item2;
               Equip eq2 = (Equip)item3;
               if (eq1.getState() == 0 || eq2.getState() == 0 || eq1.getState() == 1 && eq1.getPotential1() == 0 || eq2.getState() == 1 && eq2.getPotential1() == 0) {
                  c.getPlayer().dropMessage(1, "먼저 잠재능력을 열어주세요.");
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
               option3_sbal = Randomizer.isSuccess(ii.getSuccess(item.getItemId(), c.getPlayer(), item));
               if (option3_sbal) {
                  level = false;
                  int alpha_option2 = false;
                  int alpha_option3_sbal = false;
                  int alpha_level = true;
                  int beta_option = false;
                  int beta_option2 = false;
                  int beta_option3_sbal = false;
                  int beta_level = true;
                  int alpha_option = potential(eq1.getItemId(), 1, true);
                  int alpha_option2 = potential(eq1.getItemId(), 1, true);
                  int alpha_option3_sbal = potential(eq1.getItemId(), 1, true);
                  if (Randomizer.nextInt(100) >= 20 && toUse.getItemId() != 2048306) {
                     eq1.setPotential4(alpha_option);
                     eq1.setPotential5(alpha_option2);
                     eq2.setPotential4(alpha_option);
                     eq2.setPotential5(alpha_option2);
                  } else {
                     eq1.setPotential4(alpha_option);
                     eq1.setPotential5(alpha_option2);
                     eq1.setPotential6(alpha_option3_sbal);
                     eq2.setPotential4(alpha_option);
                     eq2.setPotential5(alpha_option2);
                     eq2.setPotential6(alpha_option3_sbal);
                  }
               }

               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(toUse, item2));
               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(toUse, item3));
               c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), option3_sbal, toUse.getItemId(), item.getItemId()));
               MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false);
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               Equip eq3 = (Equip)item;
               if (eq3.getState() == 0 || eq3.getPotential1() == 0) {
                  c.getPlayer().dropMessage(1, "먼저 잠재능력을 열어주세요.");
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               MapleItemInformationProvider ii2 = MapleItemInformationProvider.getInstance();
               boolean succes2 = Randomizer.isSuccess(ii2.getSuccess(item.getItemId(), c.getPlayer(), item));
               if (succes2) {
                  int option = false;
                  int option2 = false;
                  option3_sbal = false;
                  level = true;
                  int option = potential(eq3.getItemId(), 2, true);
                  int option2 = potential(eq3.getItemId(), 2, true);
                  int option3_sbal = potential(eq3.getItemId(), 2, true);
                  if (Randomizer.nextInt(100) >= 20 && toUse.getItemId() != 2048306) {
                     eq3.setPotential4(option);
                     eq3.setPotential5(option2);
                  } else {
                     eq3.setPotential4(option);
                     eq3.setPotential5(option2);
                     eq3.setPotential6(option3_sbal);
                  }
               }

               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateScrollandItem(toUse, item));
               c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), succes2, toUse.getItemId(), eq3.getItemId()));
               MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false);
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            }
         }
      } catch (Exception var21) {
         var21.printStackTrace();
      }

   }

   public static final boolean UseSkillBook(short slot, int itemId, MapleClient c, MapleCharacter chr) {
      Item toUse = chr.getInventory(GameConstants.getInventoryType(itemId)).getItem(slot);
      if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
         Map<String, Integer> skilldata = MapleItemInformationProvider.getInstance().getEquipStats(toUse.getItemId());
         if (skilldata == null) {
            return false;
         } else {
            boolean canuse = false;
            boolean success = false;
            int skill = false;
            int maxlevel = false;
            Integer SuccessRate = (Integer)skilldata.get("success");
            Integer ReqSkillLevel = (Integer)skilldata.get("reqSkillLevel");
            Integer MasterLevel = (Integer)skilldata.get("masterLevel");
            byte i = 0;

            while(true) {
               Integer CurrentLoopedSkillId = (Integer)skilldata.get("skillid" + i);
               ++i;
               if (CurrentLoopedSkillId == null || MasterLevel == null) {
                  break;
               }

               Skill CurrSkillData = SkillFactory.getSkill(CurrentLoopedSkillId);
               if (CurrSkillData != null && CurrSkillData.canBeLearnedBy(chr) && (ReqSkillLevel == null || chr.getSkillLevel(CurrSkillData) >= ReqSkillLevel) && chr.getMasterLevel(CurrSkillData) < MasterLevel) {
                  canuse = true;
                  if (SuccessRate != null && Randomizer.nextInt(100) > SuccessRate) {
                     success = false;
                  } else {
                     success = true;
                     chr.changeSingleSkillLevel(CurrSkillData, chr.getSkillLevel(CurrSkillData), (byte)MasterLevel);
                  }

                  MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(itemId), slot, (short)1, false);
                  break;
               }
            }

            c.getPlayer().getMap().broadcastMessage(CWvsContext.useSkillBook(chr, 0, 0, canuse, success));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            return canuse;
         }
      } else {
         return false;
      }
   }

   public static final void UseCatchItem(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      slea.readInt();
      c.getPlayer().setScrolledPosition((short)0);
      short slot = slea.readShort();
      int itemid = slea.readInt();
      MapleMonster mob = chr.getMap().getMonsterByOid(slea.readInt());
      Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
      MapleMap map = chr.getMap();
      if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && mob != null && itemid / 10000 == 227 && MapleItemInformationProvider.getInstance().getCardMobId(itemid) == mob.getId()) {
         if (MapleItemInformationProvider.getInstance().isMobHP(itemid) && mob.getHp() > mob.getMobMaxHp() / 2L) {
            map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), (byte)0));
            c.getSession().writeAndFlush(CWvsContext.catchMob(mob.getId(), itemid, (byte)0));
         } else {
            map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), (byte)1));
            map.killMonster(mob, chr, true, false, (byte)1);
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false, false);
            if (MapleItemInformationProvider.getInstance().getCreateId(itemid) > 0) {
               MapleInventoryManipulator.addById(c, MapleItemInformationProvider.getInstance().getCreateId(itemid), (short)1, "Catch item " + itemid + " on " + FileoutputUtil.CurrentReadable_Date());
            }
         }
      }

      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static final void UseMountFood(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      slea.readInt();
      short slot = slea.readShort();
      int itemid = slea.readInt();
      Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
      MapleMount mount = chr.getMount();
      if (itemid / 10000 == 226 && toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && mount != null) {
         int fatigue = mount.getFatigue();
         boolean levelup = false;
         mount.setFatigue((byte)-30);
         if (fatigue > 0) {
            mount.increaseExp();
            int level = mount.getLevel();
            if (level < 30 && mount.getExp() >= GameConstants.getMountExpNeededForLevel(level + 1)) {
               mount.setLevel((byte)(level + 1));
               levelup = true;
            }
         }

         chr.getMap().broadcastMessage(CWvsContext.updateMount(chr, levelup));
         MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
      }

      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static final void UseScriptedNPCItem(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      short slot = slea.readShort();
      int itemId = slea.readInt();
      Item toUse = chr.getInventory(GameConstants.getInventoryType(itemId)).getItem(slot);
      long expiration_days = 0L;
      int mountid = 0;
      int skillid;
      int j;
      if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && !chr.inPVP()) {
         String var10000 = FileoutputUtil.스크립트아이템사용로그;
         int var10001 = c.getAccID();
         FileoutputUtil.log(var10000, "[스크립트 아이템 사용] 계정 아이디 : " + var10001 + " | " + c.getPlayer().getName() + "이 " + MapleItemInformationProvider.getInstance().getName(toUse.getItemId()) + "(" + toUse.getItemId() + ")을 사용함.");
         long point;
         int var10003;
         byte id;
         MapleQuestStatus marr;
         switch(toUse.getItemId()) {
         case 2350000:
            if (c.getCharacterSlots() >= 48) {
               c.getPlayer().dropMessage(5, "현재 캐릭터 슬롯 증가쿠폰을 사용하실 수 없습니다.");
               return;
            }

            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            if (!c.gainCharacterSlot()) {
               c.getPlayer().dropMessage(5, "현재 캐릭터 슬롯 증가쿠폰을 사용하실 수 없습니다.");
            } else {
               c.getPlayer().dropMessage(1, "캐릭터 슬롯 개수를 늘렸습니다.");
            }
            break;
         case 2430008:
            chr.saveLocation(SavedLocationType.RICHIE);
            boolean warped = false;

            for(j = 390001000; j <= 390001004; ++j) {
               MapleMap map = c.getChannelServer().getMapFactory().getMap(j);
               if (map.getCharactersSize() == 0) {
                  chr.changeMap(map, map.getPortal(0));
                  warped = true;
                  break;
               }
            }

            if (warped) {
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            } else {
               c.getPlayer().dropMessage(5, "All maps are currently in use, please try again later.");
            }
            break;
         case 2430036:
            mountid = 1027;
            expiration_days = 1L;
            break;
         case 2430037:
            mountid = 1028;
            expiration_days = 1L;
            break;
         case 2430038:
            mountid = 1029;
            expiration_days = 1L;
            break;
         case 2430039:
            mountid = 1030;
            expiration_days = 1L;
            break;
         case 2430040:
            mountid = 1031;
            expiration_days = 1L;
            break;
         case 2430053:
            mountid = 1027;
            expiration_days = 30L;
            break;
         case 2430054:
            mountid = 1028;
            expiration_days = 30L;
            break;
         case 2430055:
            mountid = 1029;
            expiration_days = 30L;
            break;
         case 2430056:
            mountid = 1035;
            expiration_days = 30L;
            break;
         case 2430057:
            mountid = 1033;
            expiration_days = 30L;
            break;
         case 2430072:
            mountid = 1034;
            expiration_days = 7L;
            break;
         case 2430075:
            mountid = 1038;
            expiration_days = 15L;
            break;
         case 2430076:
            mountid = 1039;
            expiration_days = 15L;
            break;
         case 2430077:
            mountid = 1040;
            expiration_days = 15L;
            break;
         case 2430080:
            mountid = 1042;
            expiration_days = 20L;
            break;
         case 2430082:
            mountid = 1044;
            expiration_days = 7L;
            break;
         case 2430091:
            mountid = 1049;
            expiration_days = 10L;
            break;
         case 2430092:
            mountid = 1050;
            expiration_days = 10L;
            break;
         case 2430093:
            mountid = 1051;
            expiration_days = 10L;
            break;
         case 2430101:
            mountid = 1052;
            expiration_days = 10L;
            break;
         case 2430102:
            mountid = 1053;
            expiration_days = 10L;
            break;
         case 2430103:
            mountid = 1054;
            expiration_days = 30L;
            break;
         case 2430112:
            if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 1) {
               c.getPlayer().dropMessage(5, "Please make some space.");
            } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 25) {
               if (MapleInventoryManipulator.checkSpace(c, 2049400, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 25, true, false)) {
                  var10003 = toUse.getItemId();
                  MapleInventoryManipulator.addById(c, 2049400, (short)1, "Scripted item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                  break;
               }

               c.getPlayer().dropMessage(5, "Please make some space.");
            } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) < 10) {
               c.getPlayer().dropMessage(5, "There needs to be 10 Fragments for a Potential Scroll, 25 for Advanced Potential Scroll.");
            } else {
               if (MapleInventoryManipulator.checkSpace(c, 2049400, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false)) {
                  var10003 = toUse.getItemId();
                  MapleInventoryManipulator.addById(c, 2049401, (short)1, "Scripted item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                  break;
               }

               c.getPlayer().dropMessage(5, "Please make some space.");
            }
            break;
         case 2430117:
            mountid = 1036;
            expiration_days = 365L;
            break;
         case 2430118:
            mountid = 1039;
            expiration_days = 365L;
            break;
         case 2430119:
            mountid = 1040;
            expiration_days = 365L;
            break;
         case 2430120:
            mountid = 1037;
            expiration_days = 365L;
            break;
         case 2430130:
         case 2430131:
            if (GameConstants.isResist(c.getPlayer().getJob())) {
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
               c.getPlayer().gainExp((long)(20000 + c.getPlayer().getLevel() * 50 * c.getChannelServer().getExpRate()), true, true, false);
            } else {
               c.getPlayer().dropMessage(5, "You may not use this item.");
            }
            break;
         case 2430132:
         case 2430133:
         case 2430134:
         case 2430142:
            if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < 1) {
               c.getPlayer().dropMessage(5, "Make some space.");
            } else {
               if (c.getPlayer().getJob() != 3200 && c.getPlayer().getJob() != 3210 && c.getPlayer().getJob() != 3211 && c.getPlayer().getJob() != 3212) {
                  if (c.getPlayer().getJob() != 3300 && c.getPlayer().getJob() != 3310 && c.getPlayer().getJob() != 3311 && c.getPlayer().getJob() != 3312) {
                     if (c.getPlayer().getJob() != 3500 && c.getPlayer().getJob() != 3510 && c.getPlayer().getJob() != 3511 && c.getPlayer().getJob() != 3512) {
                        c.getPlayer().dropMessage(5, "You may not use this item.");
                        break;
                     }

                     MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
                     MapleInventoryManipulator.addById(c, 1492080, (short)1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                     break;
                  }

                  MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
                  MapleInventoryManipulator.addById(c, 1462093, (short)1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                  break;
               }

               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
               MapleInventoryManipulator.addById(c, 1382101, (short)1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
            }
            break;
         case 2430144:
            skillid = Randomizer.nextInt(373) + 2290000;
            if (MapleItemInformationProvider.getInstance().itemExists(skillid) && !MapleItemInformationProvider.getInstance().getName(skillid).contains("Special") && !MapleItemInformationProvider.getInstance().getName(skillid).contains("Event")) {
               var10003 = toUse.getItemId();
               MapleInventoryManipulator.addById(c, skillid, (short)1, "Reward item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            }
            break;
         case 2430149:
            mountid = 1072;
            expiration_days = 30L;
            break;
         case 2430158:
            if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() < 1) {
               c.getPlayer().dropMessage(5, "Please make some space.");
            } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) >= 100) {
               if (MapleInventoryManipulator.checkSpace(c, 4310010, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) {
                  var10003 = toUse.getItemId();
                  MapleInventoryManipulator.addById(c, 4310010, (short)1, "Scripted item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                  break;
               }

               c.getPlayer().dropMessage(5, "Please make some space.");
            } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) < 50) {
               c.getPlayer().dropMessage(5, "There needs to be 50 Purification Totems for a Noble Lion King Medal, 100 for Royal Lion King Medal.");
            } else {
               if (MapleInventoryManipulator.checkSpace(c, 4310009, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) {
                  var10003 = toUse.getItemId();
                  MapleInventoryManipulator.addById(c, 4310009, (short)1, "Scripted item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                  break;
               }

               c.getPlayer().dropMessage(5, "Please make some space.");
            }
            break;
         case 2430159:
            MapleQuest.getInstance(3182).forceComplete(c.getPlayer(), 2161004);
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430179:
            mountid = 80001026;
            expiration_days = 15L;
            break;
         case 2430200:
            if (c.getPlayer().getQuestStatus(31152) != 2) {
               c.getPlayer().dropMessage(5, "You have no idea how to use it.");
            } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() < 1) {
               c.getPlayer().dropMessage(5, "Please make some space.");
            } else {
               if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000660) >= 1 && c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000661) >= 1 && c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000662) >= 1 && c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000663) >= 1) {
                  if (MapleInventoryManipulator.checkSpace(c, 4032923, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000660, 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000661, 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000662, 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000663, 1, true, false)) {
                     var10003 = toUse.getItemId();
                     MapleInventoryManipulator.addById(c, 4032923, (short)1, "Scripted item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                     break;
                  }

                  c.getPlayer().dropMessage(5, "Please make some space.");
                  break;
               }

               c.getPlayer().dropMessage(5, "There needs to be 1 of each Stone for a Dream Key.");
            }
            break;
         case 2430206:
            mountid = 1033;
            expiration_days = 7L;
            break;
         case 2430211:
            mountid = 80001009;
            expiration_days = 30L;
            break;
         case 2430212:
            marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122500));
            if (marr.getCustomData() == null) {
               marr.setCustomData("0");
            }

            point = Long.parseLong(marr.getCustomData());
            if (point + 600000L > System.currentTimeMillis()) {
               c.getPlayer().dropMessage(5, "You can only use one energy drink per 10 minutes.");
            } else if (c.getPlayer().getFatigue() > 0) {
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
               c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 5);
            }
            break;
         case 2430213:
            marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122500));
            if (marr.getCustomData() == null) {
               marr.setCustomData("0");
            }

            point = Long.parseLong(marr.getCustomData());
            if (point + 600000L > System.currentTimeMillis()) {
               c.getPlayer().dropMessage(5, "You can only use one energy drink per 10 minutes.");
            } else if (c.getPlayer().getFatigue() > 0) {
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
               c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 10);
            }
            break;
         case 2430214:
         case 2430220:
            if (c.getPlayer().getFatigue() > 0) {
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
               c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 30);
            }
            break;
         case 2430225:
            mountid = 1031;
            expiration_days = 10L;
            break;
         case 2430227:
            if (c.getPlayer().getFatigue() > 0) {
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
               c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 50);
            }
            break;
         case 2430231:
            marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122500));
            if (marr.getCustomData() == null) {
               marr.setCustomData("0");
            }

            point = Long.parseLong(marr.getCustomData());
            if (point + 600000L > System.currentTimeMillis()) {
               c.getPlayer().dropMessage(5, "You can only use one energy drink per 10 minutes.");
            } else if (c.getPlayer().getFatigue() > 0) {
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
               c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 40);
            }
            break;
         case 2430232:
            mountid = 1106;
            expiration_days = 10L;
            break;
         case 2430242:
            mountid = 1063;
            expiration_days = 10L;
            break;
         case 2430243:
            mountid = 1064;
            expiration_days = 10L;
            break;
         case 2430249:
            mountid = 80001027;
            expiration_days = 3L;
            break;
         case 2430257:
            mountid = 1029;
            expiration_days = 7L;
            break;
         case 2430258:
            mountid = 1115;
            expiration_days = 365L;
            break;
         case 2430259:
            mountid = 1031;
            expiration_days = 3L;
            break;
         case 2430260:
            mountid = 1044;
            expiration_days = 3L;
            break;
         case 2430261:
            mountid = 1064;
            expiration_days = 3L;
            break;
         case 2430262:
            mountid = 1072;
            expiration_days = 3L;
            break;
         case 2430263:
            mountid = 1050;
            expiration_days = 3L;
            break;
         case 2430264:
            mountid = 1019;
            expiration_days = 3L;
            break;
         case 2430265:
            mountid = 1151;
            expiration_days = 3L;
            break;
         case 2430266:
            mountid = 1054;
            expiration_days = 3L;
            break;
         case 2430271:
            mountid = 80001191;
            expiration_days = 3L;
            break;
         case 2430272:
            mountid = 80001032;
            expiration_days = 3L;
            break;
         case 2430275:
            mountid = 80001033;
            expiration_days = 7L;
            break;
         case 2430283:
            mountid = 1025;
            expiration_days = 10L;
            break;
         case 2430292:
            mountid = 1145;
            expiration_days = 90L;
            break;
         case 2430294:
            mountid = 1146;
            expiration_days = 90L;
            break;
         case 2430296:
            mountid = 1147;
            expiration_days = 90L;
            break;
         case 2430298:
            mountid = 1148;
            expiration_days = 90L;
            break;
         case 2430300:
            mountid = 1149;
            expiration_days = 90L;
            break;
         case 2430302:
            mountid = 1150;
            expiration_days = 90L;
            break;
         case 2430304:
            mountid = 1151;
            expiration_days = 90L;
            break;
         case 2430306:
            mountid = 1152;
            expiration_days = 90L;
            break;
         case 2430308:
            mountid = 1153;
            expiration_days = 90L;
            break;
         case 2430310:
            mountid = 1154;
            expiration_days = 90L;
            break;
         case 2430312:
            mountid = 1156;
            expiration_days = 90L;
            break;
         case 2430313:
            mountid = 1156;
            expiration_days = -1L;
            break;
         case 2430314:
            mountid = 1156;
            expiration_days = 90L;
            break;
         case 2430316:
            mountid = 1118;
            expiration_days = 90L;
            break;
         case 2430317:
            mountid = 1121;
            expiration_days = -1L;
            break;
         case 2430318:
            mountid = 1121;
            expiration_days = 90L;
            break;
         case 2430319:
            mountid = 1122;
            expiration_days = -1L;
            break;
         case 2430320:
            mountid = 1122;
            expiration_days = 90L;
            break;
         case 2430321:
            mountid = 1123;
            expiration_days = -1L;
            break;
         case 2430322:
            mountid = 1123;
            expiration_days = 90L;
            break;
         case 2430323:
            mountid = 1124;
            expiration_days = -1L;
            break;
         case 2430325:
            mountid = 1129;
            expiration_days = -1L;
            break;
         case 2430326:
            mountid = 1129;
            expiration_days = 90L;
            break;
         case 2430327:
            mountid = 1130;
            expiration_days = -1L;
            break;
         case 2430328:
            mountid = 1130;
            expiration_days = 90L;
            break;
         case 2430329:
            mountid = 1063;
            expiration_days = -1L;
            break;
         case 2430330:
            mountid = 1063;
            expiration_days = 90L;
            break;
         case 2430331:
            mountid = 1025;
            expiration_days = -1L;
            break;
         case 2430332:
            mountid = 1025;
            expiration_days = 90L;
            break;
         case 2430333:
            mountid = 1034;
            expiration_days = -1L;
            break;
         case 2430334:
            mountid = 1034;
            expiration_days = 90L;
            break;
         case 2430335:
            mountid = 1136;
            expiration_days = -1L;
            break;
         case 2430336:
            mountid = 1136;
            expiration_days = 90L;
            break;
         case 2430337:
            mountid = 1051;
            expiration_days = -1L;
            break;
         case 2430338:
            mountid = 1051;
            expiration_days = 90L;
            break;
         case 2430339:
            mountid = 1138;
            expiration_days = -1L;
            break;
         case 2430340:
            mountid = 1138;
            expiration_days = 90L;
            break;
         case 2430341:
            mountid = 1139;
            expiration_days = -1L;
            break;
         case 2430342:
            mountid = 1139;
            expiration_days = 90L;
            break;
         case 2430343:
            mountid = 1027;
            expiration_days = -1L;
            break;
         case 2430344:
            mountid = 1027;
            expiration_days = 90L;
            break;
         case 2430346:
            mountid = 1029;
            expiration_days = -1L;
            break;
         case 2430347:
            mountid = 1029;
            expiration_days = 90L;
            break;
         case 2430348:
            mountid = 1028;
            expiration_days = -1L;
            break;
         case 2430349:
            mountid = 1028;
            expiration_days = 90L;
            break;
         case 2430350:
            mountid = 1033;
            expiration_days = -1L;
            break;
         case 2430352:
            mountid = 1064;
            expiration_days = -1L;
            break;
         case 2430354:
            mountid = 1096;
            expiration_days = -1L;
            break;
         case 2430356:
            mountid = 1101;
            expiration_days = -1L;
            break;
         case 2430358:
            mountid = 1102;
            expiration_days = -1L;
            break;
         case 2430360:
            mountid = 1054;
            expiration_days = -1L;
            break;
         case 2430362:
            mountid = 1053;
            expiration_days = -1L;
            break;
         case 2430369:
            mountid = 1049;
            expiration_days = 10L;
            break;
         case 2430370:
            if (MapleInventoryManipulator.checkSpace(c, 2028062, 1, "")) {
               var10003 = toUse.getItemId();
               MapleInventoryManipulator.addById(c, 2028062, (short)1, "Reward item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            }
            break;
         case 2430392:
            mountid = 80001038;
            expiration_days = 90L;
            break;
         case 2430458:
            mountid = 80001044;
            expiration_days = 7L;
            break;
         case 2430469:
            chr.gainItem(1122017, (short)1, false, System.currentTimeMillis() + 604800000L, "정령의 펜던트");
            chr.removeItem(toUse.getItemId(), -1);
            break;
         case 2430481:
            if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 1) {
               c.getPlayer().dropMessage(5, "Please make some space.");
            } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430481) >= 30) {
               if (MapleInventoryManipulator.checkSpace(c, 2049701, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 30, true, false)) {
                  var10003 = toUse.getItemId();
                  MapleInventoryManipulator.addById(c, 2049701, (short)1, "Scripted item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                  break;
               }

               c.getPlayer().dropMessage(5, "Please make some space.");
            } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430481) < 20) {
               c.getPlayer().dropMessage(5, "There needs to be 20 Fragments for a Advanced Equip Enhancement Scroll, 30 for Epic Potential Scroll 80%.");
            } else {
               if (MapleInventoryManipulator.checkSpace(c, 2049300, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 20, true, false)) {
                  var10003 = toUse.getItemId();
                  MapleInventoryManipulator.addById(c, 2049300, (short)1, "Scripted item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                  break;
               }

               c.getPlayer().dropMessage(5, "Please make some space.");
            }
            break;
         case 2430506:
            mountid = 80001082;
            expiration_days = 30L;
            break;
         case 2430507:
            mountid = 80001083;
            expiration_days = 30L;
            break;
         case 2430508:
            mountid = 80001175;
            expiration_days = 30L;
            break;
         case 2430518:
            mountid = 80001090;
            expiration_days = 30L;
            break;
         case 2430521:
            mountid = 80001044;
            expiration_days = 30L;
            break;
         case 2430691:
            if (c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() < 1) {
               c.getPlayer().dropMessage(5, "Please make some space.");
            } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430691) < 10) {
               c.getPlayer().dropMessage(5, "There needs to be 10 Fragments for a Nebulite Diffuser.");
            } else {
               if (MapleInventoryManipulator.checkSpace(c, 5750001, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false)) {
                  var10003 = toUse.getItemId();
                  MapleInventoryManipulator.addById(c, 5750001, (short)1, "Scripted item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                  break;
               }

               c.getPlayer().dropMessage(5, "Please make some space.");
            }
            break;
         case 2430727:
            mountid = 80001148;
            expiration_days = 30L;
            break;
         case 2430732:
            id = 0;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430748:
            if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() < 1) {
               c.getPlayer().dropMessage(5, "Please make some space.");
            } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430748) < 20) {
               c.getPlayer().dropMessage(5, "There needs to be 20 Fragments for a Premium Fusion Ticket.");
            } else {
               if (MapleInventoryManipulator.checkSpace(c, 4420000, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 20, true, false)) {
                  var10003 = toUse.getItemId();
                  MapleInventoryManipulator.addById(c, 4420000, (short)1, "Scripted item: " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                  break;
               }

               c.getPlayer().dropMessage(5, "Please make some space.");
            }
            break;
         case 2430885:
            id = 1;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430886:
            id = 2;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430887:
            id = 3;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430888:
            id = 4;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430889:
            id = 5;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430890:
            id = 6;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430891:
            id = 7;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430892:
            id = 8;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430893:
            id = 9;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430894:
            id = 10;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430908:
            mountid = 80001175;
            expiration_days = 30L;
            break;
         case 2430927:
            mountid = 80001183;
            expiration_days = 30L;
            break;
         case 2430934:
            mountid = 1042;
            expiration_days = 0L;
            break;
         case 2430937:
            mountid = 80001193;
            expiration_days = 0L;
            break;
         case 2430938:
            mountid = 80001148;
            expiration_days = 0L;
            break;
         case 2430939:
            mountid = 80001195;
            expiration_days = 0L;
            break;
         case 2430945:
            id = 11;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2430946:
            id = 12;
            chr.addCustomItem(id);
            c.getPlayer().dropMessage(5, ((CustomItem)GameConstants.customItems.get(id)).getName() + " 를 획득하였습니다. 소비칸에 특수 장비창 -> 인벤토리를 확인해주세요.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2431174:
            chr.gainHonor(Randomizer.rand(1, 50));
            chr.removeItem(toUse.getItemId(), -1);
            break;
         case 2431940:
            int pirodo = 1000;
            switch(1) {
            case 1:
               pirodo = 60;
               break;
            case 2:
               pirodo = 80;
               break;
            case 3:
               pirodo = 100;
               break;
            case 4:
               pirodo = 120;
               break;
            case 5:
               pirodo = 160;
               break;
            case 6:
               pirodo = 160;
               break;
            case 7:
               pirodo = 160;
               break;
            case 8:
               pirodo = 160;
            }

            point = c.getPlayer().getKeyValue(123, "pp") + 10L;
            if (c.getPlayer().getKeyValue(123, "pp") >= (long)pirodo) {
               c.getPlayer().dropMessage(5, "이미 모든 피로도가 충전되있습니다.");
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            if (c.getPlayer().getKeyValue(123, "pp") + 10L > (long)pirodo) {
               point = (long)pirodo;
            }

            c.getPlayer().setKeyValue(123, "pp", String.valueOf(point));
            MapleCharacter var24 = c.getPlayer();
            MapleCharacter var10002 = c.getPlayer();
            var24.dropMessage(5, "피로도가 증가했습니다. 피로도 : " + var10002.getKeyValue(123, "pp"));
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2432290:
         case 2631501:
            if (c.getPlayer().getMap().MapiaIng) {
               c.send(CWvsContext.serverNotice(1, "", "지금은 사용 할 수 없습니다."));
               c.send(CWvsContext.enableActions(chr));
               return;
            }

            c.getPlayer().getMap().MapiaIng = true;
            Iterator var13 = c.getPlayer().getMap().getAllChracater().iterator();

            while(var13.hasNext()) {
               MapleCharacter chrs = (MapleCharacter)var13.next();
               if (chrs.isAlive()) {
                  chrs.cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId() == 2631501 ? 2023300 : 2023912));
                  MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId() == 2631501 ? 2023912 : 2023300).applyTo(chrs);
               }
            }

            c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("길드의 축복이 여러분과 함께 하기를...", toUse.getItemId() == 2631501 ? 5121101 : 5121041, true));
            Timer.BuffTimer.getInstance().schedule(() -> {
               c.getPlayer().getMap().MapiaIng = false;
               c.getPlayer().getMap().broadcastMessage(CField.removeMapEffect());
            }, 10000L);
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            break;
         case 2432636:
            if (!GameConstants.isZero(chr.getJob())) {
               NPCScriptManager.getInstance().startItem(c, 9010000, "consume_2411020");
            } else {
               chr.dropMessage(5, "제로 직업군은 데미지 스킨을 사용해도 아무 효과도 얻을 수 없다.");
            }
            break;
         case 2433509:
            NPCScriptManager.getInstance().startItem(c, 9000162, "consume_2433509");
            break;
         case 2433510:
            NPCScriptManager.getInstance().startItem(c, 9000162, "consume_2433510");
            break;
         case 2434006:
            NPCScriptManager.getInstance().startItem(c, 9000162, "consume_2434006");
            break;
         case 2434021:
            chr.gainHonor(10000);
            chr.removeItem(toUse.getItemId(), -1);
            break;
         case 2434287:
            chr.gainHonor(-10000);
            chr.gainItem(2432970, 1);
            chr.removeItem(2434287, -1);
            break;
         case 2434290:
            chr.gainHonor(10000);
            chr.removeItem(2434290, -1);
            break;
         case 2434813:
            chr.gainItem(4001852, 1);
            chr.removeItem(2434813, -1);
            break;
         case 2434814:
            chr.gainItem(4001853, 1);
            chr.removeItem(2434814, -1);
            break;
         case 2434815:
            chr.gainItem(4001854, 1);
            chr.removeItem(2434815, -1);
            break;
         case 2434816:
            chr.gainItem(4001862, 1);
            chr.removeItem(2434816, -1);
            break;
         case 2435122:
         case 2435513:
         case 2436784:
         case 2439631:
            if (!GameConstants.isZero(chr.getJob())) {
               NPCScriptManager.getInstance().startItem(c, 9010000, "consume_" + toUse.getItemId());
            } else {
               chr.dropMessage(5, "제로 직업군은 데미지 스킨을 사용해도 아무 효과도 얻을 수 없다.");
            }
            break;
         case 2435719:
         case 2435902:
            if (!GameConstants.isPinkBean(c.getPlayer().getJob()) && !GameConstants.isYeti(c.getPlayer().getJob())) {
               MatrixHandler.UseCoreJamStone(c, toUse.getItemId(), Randomizer.nextLong());
               break;
            }

            c.getPlayer().dropMessage(1, "핑크빈과 예티는 불가능한 행동입니다.");
            break;
         case 2438411:
         case 2438412:
            if (!GameConstants.isPinkBean(c.getPlayer().getJob()) && !GameConstants.isYeti(c.getPlayer().getJob())) {
               MatrixHandler.UseMirrorCoreJamStone(c, toUse.getItemId(), (long)Randomizer.nextInt());
               break;
            }

            c.getPlayer().dropMessage(1, "핑크빈과 예티는 불가능한 행동입니다.");
            break;
         case 2631527:
            if (!GameConstants.isPinkBean(c.getPlayer().getJob()) && !GameConstants.isYeti(c.getPlayer().getJob())) {
               MatrixHandler.UseEnforcedCoreJamStone(c, toUse.getItemId(), (long)Randomizer.nextInt());
               break;
            }

            c.getPlayer().dropMessage(1, "핑크빈과 예티는 불가능한 행동입니다.");
            break;
         case 2632972:
            if (!GameConstants.isPinkBean(c.getPlayer().getJob()) && !GameConstants.isYeti(c.getPlayer().getJob())) {
               MatrixHandler.UseCraftCoreJamStone(c, toUse.getItemId(), (long)Randomizer.nextInt());
               break;
            }

            c.getPlayer().dropMessage(1, "핑크빈과 예티는 불가능한 행동입니다.");
            break;
         case 3994225:
            c.getPlayer().dropMessage(5, "Please bring this item to the NPC.");
            break;
         case 5680019:
            skillid = 32150 + c.getPlayer().getHair() % 10;
            c.getPlayer().setHair(skillid);
            c.getPlayer().updateSingleStat(MapleStat.HAIR, (long)skillid);
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (short)1, false);
            break;
         case 5680020:
            skillid = 32160 + c.getPlayer().getHair() % 10;
            c.getPlayer().setHair(skillid);
            c.getPlayer().updateSingleStat(MapleStat.HAIR, (long)skillid);
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (short)1, false);
            break;
         case 5680222:
            NPCScriptManager.getInstance().startItem(c, 9000216, "mannequin_add");
            break;
         case 5680531:
            NPCScriptManager.getInstance().startItem(c, 9000216, "mannequin_slotadd");
            break;
         default:
            NPCScriptManager.getInstance().startItem(c, 9010060, "consume_" + toUse.getItemId());
         }

         if (GameConstants.getDSkinNum(toUse.getItemId()) != -1) {
            MapleQuest quest = MapleQuest.getInstance(7291);
            MapleQuestStatus queststatus = new MapleQuestStatus(quest, 1);
            int skinnum = GameConstants.getDSkinNum(toUse.getItemId());
            String skinString = String.valueOf(skinnum);
            queststatus.setCustomData(skinString == null ? "0" : skinString);
            chr.updateQuest(queststatus, true);
            chr.setKeyValue(7293, "damage_skin", String.valueOf(toUse.getItemId()));
            chr.dropMessage(5, "데미지 스킨이 변경되었습니다.");
            chr.getMap().broadcastMessage(chr, CField.showForeignDamageSkin(chr, skinnum), false);
            chr.updateDamageSkin();
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
         }

         if (GameConstants.getRidingNum(toUse.getItemId()) != -1) {
            skillid = GameConstants.getRidingNum(toUse.getItemId());
            chr.changeSkillLevel(skillid, (byte)1, (byte)1);
            chr.dropMessage(5, MapleItemInformationProvider.getInstance().getName(GameConstants.getRidingItemIdbyNum(skillid)) + "(이)가 등록 되었습니다.");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
         } else if (GameConstants.getMountItemEx(toUse.getItemId()) != 0) {
            skillid = GameConstants.getMountItemEx(toUse.getItemId());
            chr.changeSkillLevel(skillid, (byte)1, (byte)1);
            chr.dropMessage(-1, MapleItemInformationProvider.getInstance().getName(skillid) + "을(를) 획득 하였습니다!!");
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
         }
      }

      if (mountid > 0) {
         c.getPlayer().getStat();
         mountid = PlayerStats.getSkillByJob(mountid, c.getPlayer().getJob());
         skillid = GameConstants.getMountItem(mountid, c.getPlayer());
         if (skillid > 0 && mountid < 80001000) {
            for(j = 80001001; j < 80001999; ++j) {
               Skill skill = SkillFactory.getSkill(j);
               if (skill != null && GameConstants.getMountItem(skill.getId(), c.getPlayer()) == skillid) {
                  mountid = j;
                  break;
               }
            }
         }

         if (c.getPlayer().getSkillLevel(mountid) > 0) {
            c.getPlayer().dropMessage(5, "이미 해당 라이딩스킬이 있습니다.");
         } else if (SkillFactory.getSkill(mountid) == null) {
            c.getPlayer().dropMessage(5, "해당스킬은 얻으실 수 없습니다.");
         } else if (expiration_days > 0L) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(mountid), 1, (byte)1, System.currentTimeMillis() + expiration_days * 24L * 60L * 60L * 1000L);
            c.getPlayer().dropMessage(-1, "[" + SkillFactory.getSkillName(mountid) + "] 스킬을 얻었습니다.");
         } else if (expiration_days == 0L) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
            c.getPlayer().changeSkillLevel(SkillFactory.getSkill(mountid), (byte)1, (byte)1);
            c.getPlayer().dropMessage(-1, "[" + SkillFactory.getSkillName(mountid) + "] 스킬을 얻었습니다.");
         }
      }

      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static final void UseSummonBag(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr.isAlive() && !chr.inPVP()) {
         slea.readInt();
         short slot = slea.readShort();
         int itemId = slea.readInt();
         Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
         if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && (c.getPlayer().getMapId() < 910000000 || c.getPlayer().getMapId() > 910000022)) {
            Map<String, Integer> toSpawn = MapleItemInformationProvider.getInstance().getEquipStats(itemId);
            if (toSpawn == null) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            MapleMonster ht = null;
            int type = false;
            Iterator var9 = toSpawn.entrySet().iterator();

            while(var9.hasNext()) {
               Entry<String, Integer> i = (Entry)var9.next();
               if (((String)i.getKey()).startsWith("mob") && Randomizer.nextInt(99) <= (Integer)i.getValue()) {
                  ht = MapleLifeFactory.getMonster(Integer.parseInt(((String)i.getKey()).substring(3)));
                  chr.getMap().spawnMonster_sSack(ht, chr.getPosition(), 0);
               }
            }

            if (ht == null) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
         }

         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static final void UseCashItem(LittleEndianAccessor slea, MapleClient c) {
      if (c.getPlayer() != null && c.getPlayer().getMap() != null && !c.getPlayer().inPVP()) {
         slea.readInt();
         c.getPlayer().setScrolledPosition((short)0);
         short slot = slea.readShort();
         int itemId = slea.readInt();
         int select_skincolor;
         if (itemId == 5150190) {
            if (!GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
               slea.readByte();
               slea.readByte();
               select_skincolor = slea.readInt();
               c.getPlayer().setHair(select_skincolor);
               c.getPlayer().updateSingleStat(MapleStat.HAIR, (long)select_skincolor);
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               slea.readByte();
               slea.readByte();
               select_skincolor = slea.readInt();
               c.getPlayer().엔젤릭버스터임시 = select_skincolor;
               c.removeClickedNPC();
               NPCScriptManager.getInstance().dispose(c);
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               NPCScriptManager.getInstance().start(c, 2007, "hairchoice");
               c.getPlayer().dropMessageGM(6, "선택된 헤어 : " + select_skincolor);
            }

         } else if (itemId == 5152259) {
            if (!GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
               slea.readByte();
               slea.readByte();
               select_skincolor = slea.readInt();
               c.getPlayer().setFace(select_skincolor);
               c.getPlayer().updateSingleStat(MapleStat.FACE, (long)select_skincolor);
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               slea.readByte();
               slea.readByte();
               select_skincolor = slea.readInt();
               c.getPlayer().엔젤릭버스터임시 = select_skincolor;
               c.removeClickedNPC();
               NPCScriptManager.getInstance().dispose(c);
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               NPCScriptManager.getInstance().start(c, 2007, "hairchoice");
               c.getPlayer().dropMessageGM(6, "선택된 헤어 : " + select_skincolor);
            }

         } else {
            int facecolor;
            if (itemId == 5151036) {
               if (!GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                  slea.readByte();
                  slea.readByte();
                  select_skincolor = slea.readInt();
                  facecolor = c.getPlayer().getHair() - c.getPlayer().getHair() % 10 + select_skincolor;
                  c.getPlayer().setHair(facecolor);
                  c.getPlayer().updateSingleStat(MapleStat.HAIR, (long)facecolor);
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               } else {
                  slea.readByte();
                  slea.readByte();
                  select_skincolor = slea.readInt();
                  facecolor = c.getPlayer().getHair() - c.getPlayer().getHair() % 10 + select_skincolor;
                  c.getPlayer().엔젤릭버스터임시 = facecolor;
                  c.removeClickedNPC();
                  NPCScriptManager.getInstance().dispose(c);
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  NPCScriptManager.getInstance().start(c, 2007, "hairchoice");
               }

            } else {
               boolean used;
               if (itemId == 5152111) {
                  if (!GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                     slea.readByte();
                     slea.readByte();
                     select_skincolor = slea.readInt();
                     used = false;
                     facecolor = c.getPlayer().getFace() - c.getPlayer().getFace() % 1000 + c.getPlayer().getFace() % 100 + 100 * select_skincolor;
                     c.getPlayer().setFace(facecolor);
                     c.getPlayer().updateSingleStat(MapleStat.FACE, (long)facecolor);
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  } else {
                     slea.readByte();
                     slea.readByte();
                     select_skincolor = slea.readInt();
                     used = false;
                     facecolor = c.getPlayer().getFace() - c.getPlayer().getFace() % 1000 + c.getPlayer().getFace() % 100 + 100 * select_skincolor;
                     c.getPlayer().엔젤릭버스터임시 = facecolor;
                     c.removeClickedNPC();
                     NPCScriptManager.getInstance().dispose(c);
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     NPCScriptManager.getInstance().start(c, 2007, "hairchoice");
                  }

               } else if (itemId == 5153000) {
                  if (!GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                     slea.readByte();
                     slea.readByte();
                     select_skincolor = slea.readInt();
                     c.getPlayer().setSkinColor((byte)select_skincolor);
                     c.getPlayer().updateSingleStat(MapleStat.SKIN, (long)select_skincolor);
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  } else {
                     slea.readByte();
                     slea.readByte();
                     select_skincolor = slea.readInt();
                     c.getPlayer().엔젤릭버스터임시 = select_skincolor;
                     c.removeClickedNPC();
                     NPCScriptManager.getInstance().dispose(c);
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     NPCScriptManager.getInstance().start(c, 2007, "hairchoice");
                  }

               } else {
                  if (itemId == 5064000 || itemId == 5064100 || itemId == 5064300 || itemId == 5064400) {
                     slea.readShort();
                  }

                  Item toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot);
                  if ((toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) && itemId != 5153015 && itemId != 5150132 && itemId != 5152020) {
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  } else {
                     used = false;
                     boolean cc = false;
                     MapleItemInformationProvider ii3 = MapleItemInformationProvider.getInstance();
                     if (itemId != 5062009 && itemId != 5062010 && itemId != 5062500 && itemId != 5153015 && itemId != 5150132 && itemId != 5152020) {
                        String var10000 = FileoutputUtil.아이템사용로그;
                        int var10001 = c.getAccID();
                        FileoutputUtil.log(var10000, "[캐시 아이템 사용] 계정 아이디 : " + var10001 + " | " + c.getPlayer().getName() + "이 " + ii3.getName(toUse.getItemId()) + "(" + toUse.getItemId() + ")를 사용함.");
                     }

                     Item item3;
                     boolean up;
                     int pos;
                     int[] forbiddenFaces;
                     MapleInventoryType type;
                     Equip toScroll;
                     Item item;
                     long uniqueId;
                     MapleItemInformationProvider ii;
                     MapleItemInformationProvider mapleItemInformationProvider4;
                     Item item7;
                     Item item6;
                     Item item4;
                     short dst;
                     boolean ear;
                     boolean days;
                     int flag;
                     MaplePet maplePet1;
                     int petIndex;
                     int baseFace;
                     MaplePet.PetFlag zz;
                     int i4;
                     int i5;
                     int newFace;
                     String[] var152;
                     int Random;
                     int i8;
                     String str;
                     String z;
                     int theJob;
                     Equip neq;
                     String[] var186;
                     boolean a;
                     int stat;
                     StringBuilder stringBuilder;
                     boolean stat2;
                     int[] var192;
                     int stat1;
                     MaplePet[] var196;
                     MaplePet maplePet;
                     String str;
                     byte numLines;
                     boolean 공;
                     boolean suc;
                     Equip zeroequip;
                     Equip equip6;
                     MapleMap mapleMap;
                     Object item13;
                     Equip neq;
                     label6170:
                     switch(itemId) {
                     case 2320000:
                     case 5040000:
                     case 5040001:
                     case 5040002:
                     case 5040003:
                     case 5040004:
                     case 5041000:
                     case 5041001:
                        used = UseTeleRock(slea, c, itemId);
                        break;
                     case 5043000:
                     case 5043001:
                        short questid = slea.readShort();
                        int npcid = slea.readInt();
                        MapleQuest quest = MapleQuest.getInstance(questid);
                        if (c.getPlayer().getQuest(quest).getStatus() == 1 && quest.canComplete(c.getPlayer(), npcid)) {
                           int mapId = MapleLifeFactory.getNPCLocation(npcid);
                           if (mapId != -1) {
                              mapleMap = c.getChannelServer().getMapFactory().getMap(mapId);
                              if (mapleMap.containsNPC(npcid) && !FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit()) && !FieldLimitType.VipRock.check(mapleMap.getFieldLimit())) {
                                 c.getPlayer().changeMap(mapleMap, mapleMap.getPortal(0));
                              }

                              used = true;
                           } else {
                              c.getPlayer().dropMessage(1, "Unknown error has occurred.");
                           }
                        }
                        break;
                     case 5044000:
                     case 5044001:
                     case 5044002:
                     case 5044006:
                     case 5044007:
                        slea.readByte();
                        int mapid = slea.readInt();
                        if (mapid == 180000000) {
                           c.getPlayer().warp(ServerConstants.warpMap);
                           c.getPlayer().dropMessage(1, "그곳으로 이동하실 수 없습니다.");
                           return;
                        }

                        MapleMap target = c.getChannelServer().getMapFactory().getMap(mapid);
                        c.getPlayer().changeMap(target, target.getPortal(0));
                        if (ItemFlag.KARMA_USE.check(toUse.getFlag())) {
                           toUse.setFlag(toUse.getFlag() - ItemFlag.KARMA_USE.getValue() + ItemFlag.UNTRADEABLE.getValue());
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.CASH, toUse));
                        }
                        break;
                     case 5050000:
                        Map<MapleStat, Long> statupdate = new EnumMap(MapleStat.class);
                        int apto = slea.readInt();
                        int apfrom = slea.readInt();
                        if (apto != apfrom) {
                           int job = c.getPlayer().getJob();
                           PlayerStats playerst = c.getPlayer().getStat();
                           used = true;
                           switch(apto) {
                           case 64:
                              if (playerst.getStr() >= 999) {
                                 used = false;
                              }
                              break;
                           case 128:
                              if (playerst.getDex() >= 999) {
                                 used = false;
                              }
                              break;
                           case 256:
                              if (playerst.getInt() >= 999) {
                                 used = false;
                              }
                              break;
                           case 512:
                              if (playerst.getLuk() >= 999) {
                                 used = false;
                              }
                              break;
                           case 2048:
                              if (playerst.getMaxHp() >= 500000L) {
                                 used = false;
                              }
                              break;
                           case 8192:
                              if (playerst.getMaxMp() >= 500000L) {
                                 used = false;
                              }
                           }

                           switch(apfrom) {
                           case 64:
                              if (playerst.getStr() <= 4 || c.getPlayer().getJob() % 1000 / 100 == 1 && playerst.getStr() <= 35) {
                                 used = false;
                              }
                              break;
                           case 128:
                              if (playerst.getDex() <= 4 || c.getPlayer().getJob() % 1000 / 100 == 3 && playerst.getDex() <= 25 || c.getPlayer().getJob() % 1000 / 100 == 4 && playerst.getDex() <= 25 || c.getPlayer().getJob() % 1000 / 100 == 5 && playerst.getDex() <= 20) {
                                 used = false;
                              }
                              break;
                           case 256:
                              if (playerst.getInt() <= 4 || c.getPlayer().getJob() % 1000 / 100 == 2 && playerst.getInt() <= 20) {
                                 used = false;
                              }
                              break;
                           case 512:
                              if (playerst.getLuk() <= 4) {
                                 used = false;
                              }
                              break;
                           case 2048:
                              if (c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getHpApUsed() >= 10000) {
                                 used = false;
                                 c.getPlayer().dropMessage(1, "You need points in HP or MP in order to take points out.");
                              }
                              break;
                           case 8192:
                              if (c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getHpApUsed() >= 10000) {
                                 used = false;
                                 c.getPlayer().dropMessage(1, "You need points in HP or MP in order to take points out.");
                              }
                           }

                           if (used) {
                              long maxmp;
                              switch(apto) {
                              case 64:
                                 theJob = playerst.getStr() + 1;
                                 playerst.setStr((short)theJob, c.getPlayer());
                                 statupdate.put(MapleStat.STR, (long)theJob);
                                 break;
                              case 128:
                                 theJob = playerst.getDex() + 1;
                                 playerst.setDex((short)theJob, c.getPlayer());
                                 statupdate.put(MapleStat.DEX, (long)theJob);
                                 break;
                              case 256:
                                 theJob = playerst.getInt() + 1;
                                 playerst.setInt((short)theJob, c.getPlayer());
                                 statupdate.put(MapleStat.INT, (long)theJob);
                                 break;
                              case 512:
                                 theJob = playerst.getLuk() + 1;
                                 playerst.setLuk((short)theJob, c.getPlayer());
                                 statupdate.put(MapleStat.LUK, (long)theJob);
                                 break;
                              case 2048:
                                 long l2 = playerst.getMaxHp();
                                 if (GameConstants.isBeginnerJob(job)) {
                                    l2 += (long)Randomizer.rand(4, 8);
                                 } else if ((job < 100 || job > 132) && (job < 3200 || job > 3212) && (job < 1100 || job > 1112) && (job < 3100 || job > 3112)) {
                                    if ((job < 200 || job > 232) && !GameConstants.isEvan(job) && (job < 1200 || job > 1212)) {
                                       if (job >= 300 && job <= 322 || job >= 400 && job <= 434 || job >= 1300 && job <= 1312 || job >= 1400 && job <= 1412 || job >= 3300 && job <= 3312 || job >= 2300 && job <= 2312) {
                                          l2 += (long)Randomizer.rand(14, 18);
                                       } else if ((job < 510 || job > 512) && (job < 1510 || job > 1512)) {
                                          if ((job < 500 || job > 532) && (job < 3500 || job > 3512) && job != 1500) {
                                             if (job >= 2000 && job <= 2112) {
                                                l2 += (long)Randomizer.rand(34, 38);
                                             } else {
                                                l2 += (long)Randomizer.rand(50, 100);
                                             }
                                          } else {
                                             l2 += (long)Randomizer.rand(16, 20);
                                          }
                                       } else {
                                          l2 += (long)Randomizer.rand(24, 28);
                                       }
                                    } else {
                                       l2 += (long)Randomizer.rand(10, 12);
                                    }
                                 } else {
                                    l2 += (long)Randomizer.rand(36, 42);
                                 }

                                 l2 = Math.min(500000L, Math.abs(l2));
                                 c.getPlayer().setHpApUsed((short)(c.getPlayer().getHpApUsed() + 1));
                                 playerst.setMaxHp(l2, c.getPlayer());
                                 statupdate.put(MapleStat.MAXHP, l2);
                                 break;
                              case 8192:
                                 label5942: {
                                    maxmp = playerst.getMaxMp();
                                    if (GameConstants.isBeginnerJob(job)) {
                                       maxmp += (long)Randomizer.rand(6, 8);
                                    } else {
                                       if (job >= 3100 && job <= 3112) {
                                          break label5942;
                                       }

                                       if ((job < 100 || job > 132) && (job < 1100 || job > 1112) && (job < 2000 || job > 2112)) {
                                          if (job >= 200 && job <= 232 || GameConstants.isEvan(job) || job >= 3200 && job <= 3212 || job >= 1200 && job <= 1212) {
                                             maxmp += (long)Randomizer.rand(32, 36);
                                          } else if ((job < 300 || job > 322) && (job < 400 || job > 434) && (job < 500 || job > 532) && (job < 3200 || job > 3212) && (job < 3500 || job > 3512) && (job < 1300 || job > 1312) && (job < 1400 || job > 1412) && (job < 1500 || job > 1512) && (job < 2300 || job > 2312)) {
                                             maxmp += (long)Randomizer.rand(50, 100);
                                          } else {
                                             maxmp += (long)Randomizer.rand(8, 10);
                                          }
                                       } else {
                                          maxmp += (long)Randomizer.rand(4, 9);
                                       }
                                    }

                                    maxmp = Math.min(500000L, Math.abs(maxmp));
                                    c.getPlayer().setHpApUsed((short)(c.getPlayer().getHpApUsed() + 1));
                                    playerst.setMaxMp(maxmp, c.getPlayer());
                                    statupdate.put(MapleStat.MAXMP, maxmp);
                                 }
                              }

                              switch(apfrom) {
                              case 64:
                                 stat = playerst.getStr() - 1;
                                 playerst.setStr((short)stat, c.getPlayer());
                                 statupdate.put(MapleStat.STR, (long)stat);
                                 break;
                              case 128:
                                 stat = playerst.getDex() - 1;
                                 playerst.setDex((short)stat, c.getPlayer());
                                 statupdate.put(MapleStat.DEX, (long)stat);
                                 break;
                              case 256:
                                 stat = playerst.getInt() - 1;
                                 playerst.setInt((short)stat, c.getPlayer());
                                 statupdate.put(MapleStat.INT, (long)stat);
                                 break;
                              case 512:
                                 stat = playerst.getLuk() - 1;
                                 playerst.setLuk((short)stat, c.getPlayer());
                                 statupdate.put(MapleStat.LUK, (long)stat);
                                 break;
                              case 2048:
                                 long maxhp = playerst.getMaxHp();
                                 if (GameConstants.isBeginnerJob(job)) {
                                    maxhp -= 12L;
                                 } else if (job >= 200 && job <= 232 || job >= 1200 && job <= 1212) {
                                    maxhp -= 10L;
                                 } else if ((job < 300 || job > 322) && (job < 400 || job > 434) && (job < 1300 || job > 1312) && (job < 1400 || job > 1412) && (job < 3300 || job > 3312) && (job < 3500 || job > 3512) && (job < 2300 || job > 2312)) {
                                    if (job >= 500 && job <= 532 || job >= 1500 && job <= 1512) {
                                       maxhp -= 22L;
                                    } else if ((job < 100 || job > 132) && (job < 1100 || job > 1112) && (job < 3100 || job > 3112)) {
                                       if ((job < 2000 || job > 2112) && (job < 3200 || job > 3212)) {
                                          maxhp -= 20L;
                                       } else {
                                          maxhp -= 40L;
                                       }
                                    } else {
                                       maxhp -= 32L;
                                    }
                                 } else {
                                    maxhp -= 15L;
                                 }

                                 c.getPlayer().setHpApUsed((short)(c.getPlayer().getHpApUsed() - 1));
                                 playerst.setMaxHp(maxhp, c.getPlayer());
                                 statupdate.put(MapleStat.MAXHP, maxhp);
                                 break;
                              case 8192:
                                 label5802: {
                                    maxmp = playerst.getMaxMp();
                                    if (GameConstants.isBeginnerJob(job)) {
                                       maxmp -= 8L;
                                    } else {
                                       if (job >= 3100 && job <= 3112) {
                                          break label5802;
                                       }

                                       if ((job < 100 || job > 132) && (job < 1100 || job > 1112)) {
                                          if (job >= 200 && job <= 232 || job >= 1200 && job <= 1212) {
                                             maxmp -= 30L;
                                          } else if (job >= 500 && job <= 532 || job >= 300 && job <= 322 || job >= 400 && job <= 434 || job >= 1300 && job <= 1312 || job >= 1400 && job <= 1412 || job >= 1500 && job <= 1512 || job >= 3300 && job <= 3312 || job >= 3500 && job <= 3512 || job >= 2300 && job <= 2312) {
                                             maxmp -= 10L;
                                          } else if (job >= 2000 && job <= 2112) {
                                             maxmp -= 5L;
                                          } else {
                                             maxmp -= 20L;
                                          }
                                       } else {
                                          maxmp -= 4L;
                                       }
                                    }

                                    c.getPlayer().setHpApUsed((short)(c.getPlayer().getHpApUsed() - 1));
                                    playerst.setMaxMp(maxmp, c.getPlayer());
                                    statupdate.put(MapleStat.MAXMP, maxmp);
                                 }
                              }

                              c.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statupdate, false, c.getPlayer()));
                           }
                        }
                        break;
                     case 5050001:
                     case 5050002:
                     case 5050003:
                     case 5050004:
                     case 5050005:
                     case 5050006:
                     case 5050007:
                     case 5050008:
                     case 5050009:
                        if (itemId >= 5050005 && !GameConstants.isEvan(c.getPlayer().getJob())) {
                           c.getPlayer().dropMessage(1, "This reset is only for Evans.");
                        } else if (itemId < 5050005 && GameConstants.isEvan(c.getPlayer().getJob())) {
                           c.getPlayer().dropMessage(1, "This reset is only for non-Evans.");
                        } else {
                           int skill1 = slea.readInt();
                           int skill2 = slea.readInt();
                           var192 = GameConstants.blockedSkills;
                           Random = var192.length;

                           for(i8 = 0; i8 < Random; ++i8) {
                              stat = var192[i8];
                              if (skill1 == stat) {
                                 c.getPlayer().dropMessage(1, "You may not add this skill.");
                                 return;
                              }
                           }

                           Skill skillSPTo = SkillFactory.getSkill(skill1);
                           Skill skillSPFrom = SkillFactory.getSkill(skill2);
                           if (!skillSPTo.isBeginnerSkill() && !skillSPFrom.isBeginnerSkill()) {
                              if (GameConstants.getSkillBookForSkill(skill1) != GameConstants.getSkillBookForSkill(skill2)) {
                                 c.getPlayer().dropMessage(1, "You may not add different job skills.");
                              } else if (c.getPlayer().getSkillLevel(skillSPTo) + 1 <= skillSPTo.getMaxLevel() && c.getPlayer().getSkillLevel(skillSPFrom) > 0 && skillSPTo.canBeLearnedBy(c.getPlayer())) {
                                 if (skillSPTo.isFourthJob() && c.getPlayer().getSkillLevel(skillSPTo) + 1 > c.getPlayer().getMasterLevel(skillSPTo)) {
                                    c.getPlayer().dropMessage(1, "You will exceed the master level.");
                                 } else {
                                    if (itemId >= 5050005) {
                                       if (GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2 && GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2 + 1) {
                                          c.getPlayer().dropMessage(1, "You may not add this job SP using this reset.");
                                          break;
                                       }
                                    } else {
                                       theJob = GameConstants.getJobNumber(skill2);
                                       switch(skill2 / 10000) {
                                       case 430:
                                          theJob = 1;
                                          break;
                                       case 431:
                                       case 432:
                                          theJob = 2;
                                          break;
                                       case 433:
                                          theJob = 3;
                                          break;
                                       case 434:
                                          theJob = 4;
                                       }

                                       if (theJob != itemId - 5050000) {
                                          c.getPlayer().dropMessage(1, "You may not subtract from this skill. Use the appropriate SP reset.");
                                          break;
                                       }
                                    }

                                    Map<Skill, SkillEntry> sa = new HashMap();
                                    sa.put(skillSPFrom, new SkillEntry((byte)(c.getPlayer().getSkillLevel(skillSPFrom) - 1), c.getPlayer().getMasterLevel(skillSPFrom), SkillFactory.getDefaultSExpiry(skillSPFrom)));
                                    sa.put(skillSPTo, new SkillEntry((byte)(c.getPlayer().getSkillLevel(skillSPTo) + 1), c.getPlayer().getMasterLevel(skillSPTo), SkillFactory.getDefaultSExpiry(skillSPTo)));
                                    c.getPlayer().changeSkillsLevel(sa);
                                    used = true;
                                 }
                              }
                           } else {
                              c.getPlayer().dropMessage(1, "You may not add beginner skills.");
                           }
                        }
                        break;
                     case 5060000:
                        Item item1 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                        if (item1 != null && item1.getOwner().equals("")) {
                           a = true;
                           var186 = GameConstants.RESERVED;
                           i8 = var186.length;

                           for(stat = 0; stat < i8; ++stat) {
                              z = var186[stat];
                              if (c.getPlayer().getName().indexOf(z) != -1) {
                                 a = false;
                              }
                           }

                           if (a) {
                              item1.setOwner(c.getPlayer().getName());
                              c.getPlayer().forceReAddItem(item1, MapleInventoryType.EQUIPPED);
                              used = true;
                           }
                        }
                        break;
                     case 5060001:
                        type = MapleInventoryType.getByType((byte)slea.readInt());
                        item4 = c.getPlayer().getInventory(type).getItem((short)slea.readInt());
                        if (item4 != null && item4.getExpiration() == -1L) {
                           theJob = item4.getFlag();
                           theJob |= ItemFlag.LOCK.getValue();
                           item4.setFlag(theJob);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item4));
                           used = true;
                        }
                        break;
                     case 5060003:
                     case 5060004:
                        item = c.getPlayer().getInventory(MapleInventoryType.ETC).findById(itemId == 5060003 ? 4170023 : 4170024);
                        if (item == null || item.getQuantity() <= 0) {
                           return;
                        }
                        break;
                     case 5060048:
                        if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() > 0 && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 2 && c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() > 0 && c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() > 0) {
                           a = Randomizer.isSuccess(ServerConstants.SgoldappleSuc);
                           List<Triple<Integer, Integer, Integer>> list = ServerConstants.goldapple;
                           if (a) {
                              list = ServerConstants.Sgoldapple;
                           }

                           i8 = 0;
                           stat = 0;
                           double bestValue = Double.MAX_VALUE;
                           Iterator var202 = list.iterator();

                           while(var202.hasNext()) {
                              Triple<Integer, Integer, Integer> element = (Triple)var202.next();
                              double a = (double)(Integer)element.getRight();
                              double r = a / 10000.0D;
                              double value = -Math.log(Randomizer.nextDouble()) / r;
                              if (value < bestValue) {
                                 bestValue = value;
                                 i8 = (Integer)element.getLeft();
                                 stat = (Integer)element.getMid();
                              }
                           }

                           if (i8 > 0 && stat > 0) {
                              Item item13 = new Item(i8, (short)0, (short)stat);
                              MapleItemInformationProvider mapleItemInformationProvider = MapleItemInformationProvider.getInstance();
                              if (GameConstants.isPet(i8)) {
                                 MapleInventoryManipulator.addId_Item(c, i8, (short)1, "", MaplePet.createPet(i8, -1L), 30L, "", false);
                              } else {
                                 if (GameConstants.getInventoryType(i8) == MapleInventoryType.EQUIP) {
                                    item13 = (Equip)mapleItemInformationProvider.getEquipById(i8);
                                 }

                                 if (MapleItemInformationProvider.getInstance().isCash(i8)) {
                                    ((Item)item13).setUniqueId(MapleInventoryIdentifier.getInstance());
                                 }

                                 MapleInventoryManipulator.addbyItem(c, (Item)item13);
                                 c.getSession().writeAndFlush(CWvsContext.goldApple((Item)item13, toUse));
                                 c.getPlayer().gainItem(2435458, 1);
                                 used = true;
                                 if (a) {
                                    World.Broadcast.broadcastMessage(CWvsContext.serverMessage(11, c.getPlayer().getClient().getChannel(), "", c.getPlayer().getName() + "님이 골드애플에서 {} 을 획득하였습니다.", true, (Item)item13));
                                 }
                              }
                           }
                        }
                        break;
                     case 5061000:
                        type = MapleInventoryType.getByType((byte)slea.readInt());
                        item4 = c.getPlayer().getInventory(type).getItem((short)slea.readInt());
                        if (item4 != null && item4.getExpiration() == -1L) {
                           theJob = item4.getFlag();
                           theJob |= ItemFlag.LOCK.getValue();
                           item4.setFlag(theJob);
                           item4.setExpiration(System.currentTimeMillis() + 604800000L);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item4));
                           used = true;
                        }
                        break;
                     case 5061001:
                        type = MapleInventoryType.getByType((byte)slea.readInt());
                        item4 = c.getPlayer().getInventory(type).getItem((short)slea.readInt());
                        if (item4 != null && item4.getExpiration() == -1L) {
                           theJob = item4.getFlag();
                           theJob |= ItemFlag.LOCK.getValue();
                           item4.setFlag(theJob);
                           item4.setExpiration(System.currentTimeMillis() + -1702967296L);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item4));
                           used = true;
                        }
                        break;
                     case 5061002:
                        type = MapleInventoryType.getByType((byte)slea.readInt());
                        item4 = c.getPlayer().getInventory(type).getItem((short)slea.readInt());
                        if (item4 != null && item4.getExpiration() == -1L) {
                           theJob = item4.getFlag();
                           theJob |= ItemFlag.LOCK.getValue();
                           item4.setFlag(theJob);
                           item4.setExpiration(System.currentTimeMillis() + -813934592L);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item4));
                           used = true;
                        }
                        break;
                     case 5061003:
                        type = MapleInventoryType.getByType((byte)slea.readInt());
                        item4 = c.getPlayer().getInventory(type).getItem((short)slea.readInt());
                        if (item4 != null && item4.getExpiration() == -1L) {
                           theJob = item4.getFlag();
                           theJob |= ItemFlag.LOCK.getValue();
                           item4.setFlag(theJob);
                           item4.setExpiration(System.currentTimeMillis() + 1471228928L);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item4));
                           used = true;
                        }
                        break;
                     case 5062005:
                        pos = slea.readInt();
                        item7 = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)pos);
                        if (GameConstants.isZero(c.getPlayer().getJob()) && item7 == null) {
                           item7 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)pos);
                        }

                        if (item7 != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                           equip6 = (Equip)item7;
                           Random = equip6.getState() - 16;
                           if (Random < 4) {
                              c.getPlayer().dropMessage(1, "레전더리 등급인 아이템만 사용 가능합니다.");
                              c.send(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           MapleInventoryManipulator.addById(c, 2430759, (short)1, (String)null, (MaplePet)null, 0L, "");
                           i8 = CubeOption.getPlatinumUnlimitiedCubePotentialId(item7.getItemId(), Random, 1);
                           equip6.setPotential1(i8);
                           equip6.setPotential2(i8);
                           equip6.setPotential3(i8);
                           c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), true, itemId, equip6.getItemId()));
                           c.getPlayer().forceReAddItem_NoUpdate(item7, MapleInventoryType.EQUIP);
                           used = true;
                           if (GameConstants.isZeroWeapon(equip6.getItemId())) {
                              zeroequip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                              zeroequip.setState(equip6.getState());
                              zeroequip.setLines(equip6.getLines());
                              zeroequip.setPotential1(equip6.getPotential1());
                              zeroequip.setPotential2(equip6.getPotential2());
                              zeroequip.setPotential3(equip6.getPotential3());
                              if (zeroequip != null) {
                                 c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, zeroequip));
                              }
                           }

                           c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), true, itemId, equip6.getItemId()));
                           c.getPlayer().forceReAddItem_NoUpdate(item7, MapleInventoryType.EQUIP);
                           c.send(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, equip6));
                           used = true;
                           break;
                        }

                        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), false, itemId, 0));
                        break;
                     case 5062006:
                        pos = slea.readInt();
                        item7 = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)pos);
                        if (GameConstants.isZero(c.getPlayer().getJob()) && item7 == null) {
                           item7 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)pos);
                        }

                        if (item7 != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                           equip6 = (Equip)item7;
                           Random = equip6.getState() - 16;
                           if (Random < 4) {
                              c.getPlayer().dropMessage(1, "레전더리 등급인 아이템만 사용 가능합니다.");
                              c.send(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           equip6.setPotential1(CubeOption.getPlatinumUnlimitiedCubePotentialId(item7.getItemId(), Random, 1));
                           equip6.setPotential2(CubeOption.getPlatinumUnlimitiedCubePotentialId(item7.getItemId(), Random, 2));
                           if (equip6.getPotential3() > 0) {
                              equip6.setPotential3(CubeOption.getPlatinumUnlimitiedCubePotentialId(item7.getItemId(), Random, 3, equip6.getPotential1(), equip6.getPotential2()));

                              while(!GameConstants.getPotentialCheck(equip6.getPotential3(), equip6.getPotential1(), equip6.getPotential2())) {
                                 equip6.setPotential3(CubeOption.getPlatinumUnlimitiedCubePotentialId(item7.getItemId(), Random, 3, equip6.getPotential1(), equip6.getPotential2()));
                              }
                           }

                           if (GameConstants.isZeroWeapon(equip6.getItemId())) {
                              neq = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                              neq.setState(equip6.getState());
                              neq.setLines(equip6.getLines());
                              neq.setPotential1(equip6.getPotential1());
                              neq.setPotential2(equip6.getPotential2());
                              neq.setPotential3(equip6.getPotential3());
                              if (neq != null) {
                                 c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, neq));
                              }
                           }

                           c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), true, itemId, equip6.getItemId()));
                           c.getPlayer().forceReAddItem_NoUpdate(item7, MapleInventoryType.EQUIP);
                           c.send(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, equip6));
                           used = true;
                        } else {
                           c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(c.getPlayer().getId(), false, itemId, 0));
                        }
                        break;
                     case 5062009:
                        ii = MapleItemInformationProvider.getInstance();
                        pos = slea.readInt();
                        item = c.getPlayer().getInventory(pos < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem((short)pos);
                        up = false;
                        if (item != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                           equip6 = (Equip)item;
                           Random = Randomizer.nextInt(100);
                           MapleInventoryManipulator.addById(c, 2431893, (short)1, (String)null, (MaplePet)null, 0L, "Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                           if (equip6.getState() != 1 && equip6.getState() != 17) {
                              if (equip6.getState() == 18 && !ii.isCash(equip6.getItemId())) {
                                 if (Random < 5) {
                                    up = true;
                                    equip6.setState((byte)19);
                                 } else {
                                    equip6.setState((byte)18);
                                 }
                              } else if (equip6.getState() == 19) {
                                 if (Random < 3) {
                                    up = true;
                                    equip6.setState((byte)20);
                                 } else {
                                    equip6.setState((byte)19);
                                 }
                              }
                           } else if (Random < 10) {
                              up = true;
                              equip6.setState((byte)18);
                           } else {
                              equip6.setState((byte)17);
                           }

                           i8 = equip6.getState() - 16;
                           equip6.setPotential1(potential(item.getItemId(), i8));
                           equip6.setPotential2(potential(item.getItemId(), i8 != 1 && Randomizer.nextInt(100) >= 2 ? i8 - 1 : i8));
                           equip6.setPotential3(equip6.getPotential3() == 0 ? 0 : potential(item.getItemId(), i8 != 1 && Randomizer.nextInt(100) >= 1 ? i8 - 1 : i8));
                           equip6.setLines((byte)(equip6.getPotential3() > 0 ? 3 : 2));
                           c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(equip6.getItemId())), false);
                           if (GameConstants.isZeroWeapon(equip6.getItemId())) {
                              zeroequip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                              zeroequip.setState(equip6.getState());
                              zeroequip.setLines(equip6.getLines());
                              zeroequip.setPotential1(equip6.getPotential1());
                              zeroequip.setPotential2(equip6.getPotential2());
                              zeroequip.setPotential3(equip6.getPotential3());
                              if (zeroequip != null) {
                                 c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, zeroequip));
                              }
                           }

                           c.getPlayer().forceReAddItem(item, pos < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP);
                           c.getSession().writeAndFlush(CField.showPotentialReset(c.getPlayer().getId(), true, itemId, equip6.getItemId()));
                           c.getSession().writeAndFlush(CField.getRedCubeStart(c.getPlayer(), item, up, itemId, c.getPlayer().itemQuantity(toUse.getItemId()) - 1));
                           used = true;
                           break;
                        }

                        c.getPlayer().dropMessage(5, "소비 인벤토리의 공간이 부족하여 잠재 설정을 할 수 없습니다.");
                        break;
                     case 5062010:
                        MapleItemInformationProvider mapleItemInformationProvider1 = MapleItemInformationProvider.getInstance();
                        int k = slea.readInt();
                        Item item9 = c.getPlayer().getInventory(k < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem((short)k);
                        boolean bool5 = false;
                        if (item9 != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                           equip6 = (Equip)item9;
                           Random = Randomizer.nextInt(100);
                           MapleInventoryManipulator.addById(c, 2431894, (short)1, (String)null, (MaplePet)null, 0L, "");
                           c.getPlayer().addKV("lastCube", itemId.makeConcatWithConstants<invokedynamic>(itemId));
                           neq = (Equip)equip6.copy();
                           if (neq.getState() >= 17) {
                              if (neq.getState() != 1 && neq.getState() != 17) {
                                 if (neq.getState() == 18 && !mapleItemInformationProvider1.isCash(neq.getItemId())) {
                                    if (Random < 5) {
                                       bool5 = true;
                                       neq.setState((byte)19);
                                    } else {
                                       neq.setState((byte)18);
                                    }
                                 } else if (neq.getState() == 19) {
                                    if (Random < 3) {
                                       bool5 = true;
                                       neq.setState((byte)20);
                                    } else {
                                       neq.setState((byte)19);
                                    }
                                 }
                              } else if (Random < 10) {
                                 bool5 = true;
                                 neq.setState((byte)18);
                              } else {
                                 neq.setState((byte)17);
                              }

                              stat = neq.getState() - 16;
                              neq.setPotential1(CubeOption.getBlackCubePotentialId(item9.getItemId(), stat, 1));
                              neq.setPotential2(CubeOption.getBlackCubePotentialId(item9.getItemId(), stat, 2));
                              if (neq.getPotential3() > 0) {
                                 neq.setPotential3(CubeOption.getBlackCubePotentialId(item9.getItemId(), stat, 3, neq.getPotential1(), neq.getPotential2()));

                                 while(!GameConstants.getPotentialCheck(neq.getPotential3(), neq.getPotential1(), neq.getPotential2())) {
                                    neq.setPotential3(CubeOption.getBlackCubePotentialId(item9.getItemId(), stat, 3, neq.getPotential1(), neq.getPotential2()));
                                 }
                              }

                              neq.setLines((byte)(neq.getPotential3() > 0 ? 3 : 2));
                              c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(neq.getItemId())), false);
                              c.getSession().writeAndFlush(CField.getBlackCubeStart(c.getPlayer(), neq, bool5, 5062010, toUse.getPosition(), c.getPlayer().itemQuantity(toUse.getItemId()) - 1));
                              c.getPlayer().getMap().broadcastMessage(CField.getBlackCubeEffect(c.getPlayer().getId(), bool5, 5062010, neq.getItemId()));
                              c.getPlayer().choicepotential = neq;
                              if (c.getPlayer().memorialcube == null) {
                                 c.getPlayer().memorialcube = toUse.copy();
                              }

                              used = true;
                           } else {
                              c.getPlayer().dropMessage(5, "장비에 잠재능력이 부여되어 있는지 확인하십시오.");
                           }
                           break;
                        }

                        c.getPlayer().dropMessage(5, "소비 인벤토리의 공간이 부족하여 잠재 설정을 할 수 없습니다.");
                        break;
                     case 5062400:
                     case 5062402:
                     case 5062405:
                        short viewSlot = (short)slea.readInt();
                        short descSlot = (short)slea.readInt();
                        Equip view_Item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(viewSlot);
                        Equip desc_Item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(descSlot);
                        if (view_Item.getMoru() != 0) {
                           desc_Item.setMoru(view_Item.getMoru());
                        } else {
                           str = Integer.valueOf(view_Item.getItemId()).toString();
                           String ss = str.substring(3, 7);
                           desc_Item.setMoru(Integer.parseInt(ss));
                        }

                        c.getPlayer().forceReAddItem(desc_Item, MapleInventoryType.EQUIP);
                        used = true;
                        break;
                     case 5062500:
                        up = false;
                        int n = slea.readInt();
                        Item item10 = c.getPlayer().getInventory(n < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem((short)n);
                        if (item10 != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                           equip6 = (Equip)item10;
                           if (equip6.getPotential4() <= 0) {
                              c.getPlayer().dropMessage(1, "에디셔널 잠재능력이 부여되지 않았습니다.");
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                              int level = false;
                              Random = equip6.getPotential4() >= 10000 ? equip6.getPotential4() / 10000 : equip6.getPotential4() / 100;
                              if (Random >= 4) {
                                 Random = 4;
                              }

                              i8 = Random == 3 ? 3 : (Random == 2 ? 5 : (Random == 1 ? 10 : 0));
                              if (Randomizer.nextInt(100) < i8) {
                                 up = true;
                                 ++Random;
                              }

                              stat = Random;
                              stat1 = 0;

                              while(stat > 1) {
                                 if (stat > 1) {
                                    --stat;
                                    ++stat1;
                                 }
                              }

                              equip6.setPotential4(EdiCubeOption.getEdiCubePotentialId(item10.getItemId(), Random, 1));
                              equip6.setPotential5(EdiCubeOption.getEdiCubePotentialId(item10.getItemId(), Random, 2));
                              if (equip6.getPotential6() > 0) {
                                 equip6.setPotential6(EdiCubeOption.getEdiCubePotentialId(item10.getItemId(), Random, 3, equip6.getPotential4(), equip6.getPotential5()));

                                 while(!GameConstants.getPotentialCheck(equip6.getPotential6(), equip6.getPotential4(), equip6.getPotential5())) {
                                    equip6.setPotential6(EdiCubeOption.getEdiCubePotentialId(item10.getItemId(), Random, 3, equip6.getPotential4(), equip6.getPotential5()));
                                 }
                              }

                              if (GameConstants.isZeroWeapon(equip6.getItemId())) {
                                 Equip zeroequip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                                 zeroequip.setPotential4(equip6.getPotential4());
                                 zeroequip.setPotential5(equip6.getPotential5());
                                 zeroequip.setPotential6(equip6.getPotential6());
                                 if (zeroequip != null) {
                                    c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, zeroequip));
                                 }
                              }

                              c.getSession().writeAndFlush(CField.getEditionalCubeStart(c.getPlayer(), item10, up, itemId, c.getPlayer().itemQuantity(toUse.getItemId()) - 1));
                              c.getSession().writeAndFlush(CField.showPotentialReset(c.getPlayer().getId(), true, itemId, equip6.getItemId()));
                              c.getPlayer().forceReAddItem(item10, n < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP);
                              MapleInventoryManipulator.addById(c, 2430915, (short)1, (String)null, (MaplePet)null, 0L, "Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                              used = true;
                              c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(equip6.getItemId())), false);
                           } else {
                              c.getPlayer().dropMessage(5, "소비 아이템 여유 공간이 부족하여 잠재능력 재설정을 실패하였습니다.");
                           }
                        }
                        break;
                     case 5062503:
                        int i = slea.readInt();
                        Item item8 = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)i);
                        if (GameConstants.isZero(c.getPlayer().getJob()) && item8 == null) {
                           item8 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)i);
                        }

                        boolean bool4 = false;
                        if (item8 != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                           equip6 = (Equip)item8;
                           neq = (Equip)equip6.copy();
                           c.getPlayer().addKV("lastCube", itemId.makeConcatWithConstants<invokedynamic>(itemId));
                           if (neq.getPotential4() >= 0) {
                              공 = false;
                              i8 = neq.getPotential4() >= 10000 ? neq.getPotential4() / 10000 : neq.getPotential4() / 100;
                              if (i8 >= 4) {
                                 i8 = 4;
                              }

                              stat = i8 == 3 ? 5 : (i8 == 2 ? 7 : (i8 == 1 ? 12 : 0));
                              if (Randomizer.nextInt(100) < stat) {
                                 bool4 = true;
                                 ++i8;
                              }

                              MapleInventoryManipulator.addById(c, 2434782, (short)1, (String)null, (MaplePet)null, 0L, "");
                              neq.setPotential4(EdiCubeOption.getEdiCubePotentialId(item8.getItemId(), i8, 1));
                              neq.setPotential5(EdiCubeOption.getEdiCubePotentialId(item8.getItemId(), i8, 2));
                              if (neq.getPotential6() > 0) {
                                 neq.setPotential6(EdiCubeOption.getEdiCubePotentialId(item8.getItemId(), i8, 3, neq.getPotential4(), neq.getPotential5()));

                                 while(!GameConstants.getPotentialCheck(neq.getPotential6(), neq.getPotential4(), neq.getPotential5())) {
                                    neq.setPotential6(EdiCubeOption.getEdiCubePotentialId(item8.getItemId(), i8, 3, neq.getPotential4(), neq.getPotential5()));
                                 }
                              }

                              c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(neq.getItemId())), false);
                              c.getSession().writeAndFlush(CField.getWhiteCubeStart(c.getPlayer(), neq, bool4, 5062503, c.getPlayer().itemQuantity(toUse.getItemId()) - 1));
                              c.getPlayer().getMap().broadcastMessage(CField.getBlackCubeEffect(c.getPlayer().getId(), bool4, 5062503, neq.getItemId()));
                              c.getPlayer().choicepotential = neq;
                              if (c.getPlayer().memorialcube == null) {
                                 c.getPlayer().memorialcube = toUse.copy();
                              }

                              used = true;
                           } else {
                              c.getPlayer().dropMessage(5, "장비에 잠재능력이 부여되어 있는지 확인하십시오.");
                           }
                           break;
                        }

                        c.getPlayer().dropMessage(5, "소비 인벤토리의 공간이 부족하여 잠재 설정을 할 수 없습니다.");
                        break;
                     case 5062800:
                     case 5062801:
                        List<InnerSkillValueHolder> newValues = new LinkedList();
                        InnerSkillValueHolder ivholder = null;
                        InnerSkillValueHolder ivholder2 = null;
                        Iterator var242 = c.getPlayer().getInnerSkills().iterator();

                        while(true) {
                           while(var242.hasNext()) {
                              InnerSkillValueHolder isvh = (InnerSkillValueHolder)var242.next();
                              if (ivholder == null) {
                                 공 = true;
                                 stat = Randomizer.nextInt(100);
                                 byte nowrank;
                                 if (isvh.getRank() == 3) {
                                    nowrank = 3;
                                 } else if (isvh.getRank() == 2) {
                                    if (stat < 5) {
                                       nowrank = 3;
                                    } else {
                                       nowrank = 2;
                                    }
                                 } else if (isvh.getRank() == 1) {
                                    if (stat < 10) {
                                       nowrank = 2;
                                    } else {
                                       nowrank = 1;
                                    }
                                 } else if (stat < 40) {
                                    nowrank = 1;
                                 } else {
                                    nowrank = 0;
                                 }

                                 for(ivholder = InnerAbillity.getInstance().renewSkill(nowrank, true); isvh.getSkillId() == ivholder.getSkillId(); ivholder = InnerAbillity.getInstance().renewSkill(nowrank, true)) {
                                 }

                                 newValues.add(ivholder);
                              } else if (ivholder2 == null) {
                                 for(ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, true); isvh.getSkillId() == ivholder2.getSkillId() || ivholder.getSkillId() == ivholder2.getSkillId(); ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, true)) {
                                 }

                                 newValues.add(ivholder2);
                              } else {
                                 InnerSkillValueHolder ivholder3;
                                 for(ivholder3 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, true); isvh.getSkillId() == ivholder3.getSkillId() || ivholder.getSkillId() == ivholder3.getSkillId() || ivholder2.getSkillId() == ivholder3.getSkillId(); ivholder3 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, true)) {
                                 }

                                 newValues.add(ivholder3);
                              }
                           }

                           c.getPlayer().innerCirculator = newValues;
                           c.getSession().writeAndFlush(CWvsContext.MiracleCirculator(newValues, itemId));
                           used = true;
                           break label6170;
                        }
                     case 5063000:
                        type = MapleInventoryType.getByType((byte)slea.readInt());
                        item4 = c.getPlayer().getInventory(type).getItem((short)slea.readInt());
                        if (item4 != null && item4.getType() == 1) {
                           theJob = item4.getFlag();
                           theJob |= ItemFlag.LUCKY_PROTECT_SHIELD.getValue();
                           item4.setFlag(theJob);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item4));
                           used = true;
                        }
                        break;
                     case 5064000:
                        short s2 = slea.readShort();
                        if (s2 < 0) {
                           toScroll = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(s2);
                        } else {
                           toScroll = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(s2);
                        }

                        if (toScroll.getEnhance() < 12) {
                           flag = toScroll.getFlag();
                           flag |= ItemFlag.PROTECT_SHIELD.getValue();
                           toScroll.setFlag(flag);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, toScroll));
                           c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.getScrollEffect(c.getPlayer().getId(), Equip.ScrollResult.SUCCESS, false, toUse.getItemId(), toScroll.getItemId()), true);
                           used = true;
                        }
                        break;
                     case 5064100:
                        short s1 = slea.readShort();
                        if (s1 < 0) {
                           toScroll = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(s1);
                        } else {
                           toScroll = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(s1);
                        }

                        if (toScroll.getUpgradeSlots() != 0) {
                           flag = toScroll.getFlag();
                           flag |= ItemFlag.SAFETY_SHIELD.getValue();
                           toScroll.setFlag(flag);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, toScroll));
                           c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.getScrollEffect(c.getPlayer().getId(), Equip.ScrollResult.SUCCESS, false, toUse.getItemId(), toScroll.getItemId()), true);
                           used = true;
                        }
                        break;
                     case 5064200:
                        slea.skip(4);
                        short s3 = slea.readShort();
                        Equip equip2;
                        if (s3 < 0) {
                           equip2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(s3);
                        } else {
                           equip2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(s3);
                        }

                        if (equip2 != null) {
                           equip6 = (Equip)MapleItemInformationProvider.getInstance().getEquipById(equip2.getItemId());
                           equip2.setAcc(equip6.getAcc());
                           equip2.setAvoid(equip6.getAvoid());
                           equip2.setDex(equip6.getDex());
                           equip2.setHands(equip6.getHands());
                           equip2.setHp(equip6.getHp());
                           equip2.setInt(equip6.getInt());
                           equip2.setJump(equip6.getJump());
                           equip2.setLevel(equip6.getLevel());
                           equip2.setLuk(equip6.getLuk());
                           equip2.setMatk(equip6.getMatk());
                           equip2.setMdef(equip6.getMdef());
                           equip2.setMp(equip6.getMp());
                           equip2.setSpeed(equip6.getSpeed());
                           equip2.setStr(equip6.getStr());
                           equip2.setUpgradeSlots(equip6.getUpgradeSlots());
                           equip2.setWatk(equip6.getWatk());
                           equip2.setWdef(equip6.getWdef());
                           equip2.setEnhance((byte)0);
                           equip2.setViciousHammer((byte)0);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, equip2));
                           c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.getScrollEffect(c.getPlayer().getId(), Equip.ScrollResult.SUCCESS, cc, toUse.getItemId(), equip2.getItemId()), true);
                           used = true;
                        } else {
                           c.getPlayer().dropMessage(1, "널 오류가 발생했습니다. 오류게시판에 어떤아이템을 사용하셨는지 자세히 설명해주세요.");
                        }
                        break;
                     case 5064300:
                        dst = slea.readShort();
                        if (dst < 0) {
                           toScroll = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
                        } else {
                           toScroll = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
                        }

                        flag = toScroll.getFlag();
                        flag |= ItemFlag.RECOVERY_SHIELD.getValue();
                        toScroll.setFlag(flag);
                        c.getSession().writeAndFlush(CWvsContext.InventoryPacket.addInventorySlot(MapleInventoryType.EQUIP, toScroll));
                        c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, toScroll));
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.getScrollEffect(c.getPlayer().getId(), Equip.ScrollResult.SUCCESS, false, toUse.getItemId(), toScroll.getItemId()), true);
                        used = true;
                        break;
                     case 5064400:
                        dst = slea.readShort();
                        if (dst < 0) {
                           toScroll = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
                        } else {
                           toScroll = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
                        }

                        flag = toScroll.getFlag();
                        flag |= ItemFlag.RETURN_SCROLL.getValue();
                        toScroll.setFlag(flag);
                        c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, toScroll));
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.getScrollEffect(c.getPlayer().getId(), Equip.ScrollResult.SUCCESS, false, toUse.getItemId(), toScroll.getItemId()), true);
                        used = true;
                        break;
                     case 5068300:
                        if (c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= 1 && c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= 1 && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                           List<Pair<Integer, Integer>> list = new ArrayList();
                           int[][] NormalItem = new int[][]{{5068300, 1}};
                           int[] HighuerItem = new int[]{5000765, 5000766, 5000767, 5000768, 5000769, 5000793, 5000794, 5000795, 5000796, 5000797, 5000921, 5000922, 5000923, 5000924, 5000925, 5000936, 5000937, 5000938, 5000939, 5000940, 5000966, 5000967, 5000968, 5000965, 5000970, 5002036, 5002037, 5002038, 5002039, 5002040, 5002085, 5002086, 5002140, 5002141, 5002164, 5002165, 5002189, 5002190, 5002203, 5002204, 5002229, 5002230, 5002231};
                           int[] UniqueItem = new int[]{5000762, 5000763, 5000764, 5000790, 5000791, 5000792, 5000918, 5000919, 5000920, 5000933, 5000934, 5000935, 5000963, 5000964, 5000965, 5002033, 5002034, 5002035, 5002082, 5002083, 5002084, 5002137, 5002138, 5002139, 5002161, 5002162, 5002163, 5002186, 5002187, 5002188, 5002200, 5002201, 5002202, 5002226, 5002227, 5002228};
                           stat2 = Calendar.getInstance().get(7) == 2;
                           list.add(new Pair(1, 3004));
                           list.add(new Pair(2, 6000 + (stat2 ? -500 : 0)));
                           list.add(new Pair(3, 996 + (stat2 ? 500 : 0)));
                           int itemid = 0;
                           int count = 1;
                           int i8 = GameConstants.getWeightedRandom(list);
                           int rand;
                           if (i8 == 1) {
                              rand = Randomizer.rand(0, NormalItem.length - 1);
                              itemid = NormalItem[rand][0];
                              count = NormalItem[rand][1];
                           } else if (i8 == 2) {
                              rand = Randomizer.rand(0, HighuerItem.length - 1);
                              itemid = HighuerItem[rand];
                           } else if (i8 == 3) {
                              rand = Randomizer.rand(0, UniqueItem.length - 1);
                              itemid = UniqueItem[rand];
                           }

                           MapleItemInformationProvider mapleItemInformationProvider = MapleItemInformationProvider.getInstance();
                           if (GameConstants.isPet(itemid)) {
                              item13 = MapleInventoryManipulator.addId_Item(c, itemid, (short)1, "", MaplePet.createPet(itemid, -1L), 30L, "", false);
                           } else {
                              if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
                                 item13 = (Equip)mapleItemInformationProvider.getEquipById(itemid);
                              } else {
                                 item13 = new Item(itemid, (short)0, (short)count);
                              }

                              if (MapleItemInformationProvider.getInstance().isCash(itemid)) {
                                 ((Item)item13).setUniqueId(MapleInventoryIdentifier.getInstance());
                              }

                              MapleInventoryManipulator.addbyItem(c, (Item)item13);
                           }

                           if (item13 != null) {
                              if (i8 == 3) {
                                 World.Broadcast.broadcastMessage(CWvsContext.serverMessage(11, c.getPlayer().getClient().getChannel(), "", c.getPlayer().getName() + "님이 위습의 원더베리에서 {} 을 획득하였습니다.", true, (Item)item13));
                              }

                              c.getSession().writeAndFlush(CSPacket.WonderBerry((byte)1, (Item)item13, toUse.getItemId()));
                           }

                           used = true;
                        } else {
                           c.getPlayer().dropMessage(5, "소비, 캐시, 장비 여유 공간이 각각 한칸이상 부족합니다.");
                        }
                        break;
                     case 5068302:
                        if (c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= 1 && c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= 1 && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                           int[][] itemid = new int[][]{{1113070, 1}, {1152155, 1}, {1032216, 1}, {2435755, 1}, {2439653, 1}, {2046996, 1}, {2046997, 1}, {2047818, 1}, {5000930, 1}, {5000931, 1}, {5000932, 1}, {2591659, 1}, {2438686, 1}, {2591640, 1}, {2591676, 1}, {1113063, 1}, {1113064, 1}, {1022232, 1}, {1672077, 1}, {1113063, 1}, {1113064, 1}, {1113065, 1}, {1113066, 1}, {1112663, 1}, {1112586, 1}, {2438686, 1}, {5002079, 1}, {5002080, 1}, {5002081, 1}, {2438686, 1}};
                           i8 = Randomizer.nextInt(itemid.length);
                           MapleItemInformationProvider mapleItemInformationProvider = MapleItemInformationProvider.getInstance();
                           if (GameConstants.isPet(itemid[i8][0])) {
                              item13 = MapleInventoryManipulator.addId_Item(c, itemid[i8][0], (short)itemid[i8][1], "", MaplePet.createPet(itemid[i8][0], -1L), 30L, "", false);
                           } else {
                              if (GameConstants.getInventoryType(itemid[i8][0]) == MapleInventoryType.EQUIP) {
                                 item13 = (Equip)mapleItemInformationProvider.getEquipById(itemid[i8][0]);
                              } else {
                                 item13 = new Item(itemid[i8][0], (short)0, (short)itemid[i8][1], (byte)ItemFlag.UNTRADEABLE.getValue());
                              }

                              if (MapleItemInformationProvider.getInstance().isCash(itemid[i8][0])) {
                                 ((Item)item13).setUniqueId(MapleInventoryIdentifier.getInstance());
                              }

                              MapleInventoryManipulator.addbyItem(c, (Item)item13);
                           }

                           if (item13 != null) {
                              c.getSession().writeAndFlush(CSPacket.WonderBerry((byte)1, (Item)item13, toUse.getItemId()));
                           }

                           used = true;
                        } else {
                           c.getPlayer().dropMessage(5, "소비, 캐시, 장비 여유 공간이 각각 한칸이상 부족합니다.");
                        }
                        break;
                     case 5069000:
                     case 5069001:
                        int[] SLabel = new int[]{1003548, 1003549, 1050234, 1051284, 1702357, 1102420, 1003831, 1052605, 1702415, 1072808, 1082520, 1003867, 1042264, 1060182, 1061206, 1702424, 1082527, 1003892, 1050285, 1051352, 1702433, 1072831, 1102583, 1003909, 1050291, 1051357, 1702442, 1072836, 1102593, 1003945, 1050296, 1051362, 1702451, 1072852, 1102608, 1003957, 1003958, 1050300, 1051367, 1702457, 1072862, 1102619, 1003971, 1003972, 1051369, 1050302, 1702464, 1072868, 1102621, 1004002, 1050305, 1051373, 1702473, 1070057, 1071074, 1102632, 1003998, 1050304, 1051372, 1702468, 1072876, 1082565, 1000069, 1050311, 1051383, 1702486, 1072901, 1102667, 1000072, 1001095, 1050310, 1051382, 1702485, 1072897, 1102669, 1000074, 1001097, 1050319, 1051390, 1702503, 1071076, 1102674, 1004158, 1050322, 1051392, 1702512, 1070061, 1071078, 1102688, 1004180, 1042319, 1062207, 1702523, 1072934, 1004192, 1050335, 1051405, 1702528, 1072943, 1102706, 1004213, 1050337, 1051406, 1702535, 1072951, 1102712, 1004279, 1050341, 1051410, 1702540, 1072998, 1102748, 1004327, 1050346, 1051415, 1702549, 1073011, 1102758, 1000079, 1001100, 1050351, 1051420, 1702553, 1070064, 1100004, 1101000, 1004411, 1050353, 1051422, 1702561, 1070065, 1081014, 1004453, 1050359, 1051429, 1702570, 1070067, 1071083, 1102811, 1004468, 1050362, 1051432, 1702574, 1073050, 1102816, 1004486, 1050364, 1051434, 1702586, 1073056, 1102822, 1004527, 1050368, 1051437, 1702591, 1071085, 1070069, 1102831, 1004541, 1050370, 1051439, 1702595, 1073075, 1102836, 1004568, 1050372, 1051441, 1702607, 1073079, 1102844, 1000088, 1001110, 1050380, 1051450, 1702620, 1073084, 1102848, 1004590, 1050386, 1051456, 1073088, 1702625, 1102859, 1004602, 1050389, 1051459, 1702628, 1070072, 1071089, 1102864, 1000091, 1001113, 1050392, 1051463, 1702631, 1070073, 1071090, 1102869, 1004602, 1050394, 1051465, 1702637, 1073105, 1102876, 1002447, 1004690, 1050414, 1051483, 1702654, 1073127, 1102900, 1004701, 1050417, 1051486, 1702668, 1073128, 1102907, 1004716, 1050422, 1051490, 1070078, 1071095, 1102915, 1702676, 1004739, 1050423, 1051491, 1702681, 1070079, 1071096, 1102920, 1004774, 1004775, 1050424, 1051492, 1702687, 1073148, 1102928, 1004797, 1050430, 1051498, 1702696, 1073152, 1102936, 1004794, 1050429, 1051497, 1702694, 1070082, 1071099, 1102934, 1004814, 1050432, 1051500, 1702706, 1070083, 1071100, 1102951, 1004845, 1004846, 1050435, 1051503, 1702715, 1073170, 1073171, 1102959, 1102960, 1004852, 1050438, 1051506, 1702717, 1073175, 1073176, 1102964, 1004873, 1050441, 1051509, 1702726, 1073183, 1073184, 1102974, 1004881, 1004882, 1003460, 1050442, 1051510, 1702728, 1070084, 1071101, 1102976, 1004894, 1004895, 1050444, 1051512, 1070085, 1071102, 1102992, 1004923, 1050452, 1051519, 1702744, 1073200, 1102998, 1004947, 1004948, 1050454, 1051521, 1702755, 1073212, 1103010, 1004954, 1004955, 1050456, 1051523, 1702759, 1070088, 1071105, 1103013, 1004965, 1050461, 1051528, 1702766, 1070089, 1071106, 1103018, 1004975, 1050463, 1051530, 1702770, 1073226, 1103023, 1004988, 1050464, 1051531, 1702774, 1070090, 1071107, 1103029, 1005000, 1050468, 1051535, 1702779, 1073237, 1103035, 1005032, 1005033, 1050470, 1051537, 1702790, 1073246, 1103050, 1005043, 1050474, 1051541, 1702795, 1070093, 1071110, 1103055, 1005065, 1005066, 1053257, 1702804, 1073254, 1103067, 1005083, 1005084, 1050477, 1051544, 1702807, 1073255, 1103072, 1005092, 1050481, 1051548, 1702810, 1073258, 1103074, 1005111, 1050484, 1051551, 1702815, 1070097, 1071114, 1103079, 1005143, 1050486, 1051553, 1702826, 1073271, 1103094, 1005152, 1053305, 1702830, 1073273, 1103096, 1005166, 1050491, 1051559, 1702837, 1073280, 1103101, 1005184, 1005185, 1050492, 1051560, 1073290, 1702844, 1103114, 1005193, 1050495, 1051563, 1702850, 1073298, 1103118, 1005217, 1005218, 1050499, 1051567, 1702858, 1073302, 1103130, 1005231, 1005232, 1053351, 1053352, 1702865, 1073308, 1103138, 1005243, 1005244, 1050503, 1051573, 1702870, 1070103, 1103144, 1005260, 1005261, 1050505, 1051575, 1702876, 1070105, 1071121, 1103148, 1005272, 1050507, 1051577, 1702882, 1073322, 1103152, 1005280, 1005281, 1050509, 1051579, 1702887, 1070107, 1071123, 1103157, 1005319, 1050514, 1051584, 1702901, 1073335, 1103171, 1005324, 1050516, 1051586, 1702905, 1070110, 1071126, 1103175, 1005327, 1053416, 1702907, 1073342, 1103177, 1005354, 1053435, 1702918, 1073355, 1103185, 1005368, 1005369, 1050523, 1051593, 1702928, 1070113, 1071129, 1082744, 1005386, 1050525, 1051595, 1702937, 1070114, 1071130, 1103202, 1005399, 1050530, 1051601, 1702945, 1073378, 1103212, 1005412, 1050531, 1051602, 1103219, 1073362, 1702951, 1005419, 1005420, 1050534, 1051605, 1073390, 1103221, 1702956, 1005437, 1005438, 1050535, 1051606, 1073394, 1103224, 1702961, 1005458, 1053516, 1073402, 1103232, 1702970, 1005477, 1005478, 1050538, 1051609, 1073415, 1103235, 1702973, 1005499, 1053543, 1073428, 1103243, 1702981};
                        int[] 모자 = new int[]{1000070, 1000076, 1004897, 1001093, 1001098, 1004898, 1003955, 1004450, 1004591, 1004592, 1004777, 1005037, 1005038, 1005209, 1005210, 1005356, 1005495};
                        int[] 한벌남 = new int[]{1050299, 1050312, 1050339, 1050356, 1050385, 1050427, 1050445, 1050472, 1050497, 1050520, 1050542};
                        int[] 한벌여 = new int[]{1051366, 1051384, 1051408, 1051426, 1051455, 1051495, 1051513, 1051539, 1051565, 1051589, 1051613};
                        int[] 신발 = new int[]{1070071, 1070080, 1070086, 1070091, 1070100, 1070111, 1071088, 1071097, 1071103, 1071108, 1071117, 1071127, 1072860, 1072908, 1072978, 1073041, 1073425};
                        int[] 무기 = new int[]{1702456, 1702488, 1702538, 1702565, 1702624, 1702689, 1702736, 1702786, 1702856, 1702919, 1702976};
                        int[] 망토장갑 = new int[]{1102729, 1102809, 1102858, 1102932, 1102988, 1103053, 1103126, 1103127, 1103187, 1103241, 1082555, 1082580};
                        MapleItemInformationProvider mapleItemInformationProvider5 = MapleItemInformationProvider.getInstance();
                        int baseitemid = slea.readInt();
                        short baseitempos = slea.readShort();
                        slea.skip(8);
                        int useitemid = slea.readInt();
                        short useitempos = slea.readShort();
                        slea.skip(8);
                        Equip equip4 = (Equip)c.getPlayer().getInventory(MapleInventoryType.CODY).getItem(baseitempos);
                        Equip equip5 = (Equip)c.getPlayer().getInventory(MapleInventoryType.CODY).getItem(useitempos);
                        int 마라벨나올확률 = equip4.getEquipmentType() == 1 ? 7 : (ServerConstants.ServerTest ? 100 : 5);
                        int MItemidselect = 0;
                        int Label = 0;
                        int own = false;
                        int two = 0;
                        int three = 0;
                        boolean 마라벨인가 = false;

                        MapleMap mapleMap;
                        try {
                           if (Randomizer.isSuccess(마라벨나올확률)) {
                              if (GameConstants.isCap(baseitemid)) {
                                 a = true;
                                 Random = (int)Math.floor(Math.random() * (double)모자.length - 1.0D);
                                 MItemidselect = 모자[Random];
                                 if (c.getPlayer().getGender() == 0) {
                                    while(a) {
                                       if (GameConstants.여자모자(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)모자.length - 1.0D);
                                          MItemidselect = 모자[Random];
                                       } else {
                                          a = false;
                                       }
                                    }
                                 } else {
                                    while(a) {
                                       if (GameConstants.남자모자(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)모자.length - 1.0D);
                                          MItemidselect = 모자[Random];
                                       } else {
                                          a = false;
                                       }
                                    }
                                 }
                              } else if (GameConstants.isLongcoat(baseitemid)) {
                                 if (c.getPlayer().getGender() == 0) {
                                    theJob = (int)Math.floor(Math.random() * (double)한벌남.length - 1.0D);
                                    MItemidselect = 한벌남[theJob];
                                 } else {
                                    theJob = (int)Math.floor(Math.random() * (double)한벌여.length - 1.0D);
                                    MItemidselect = 한벌여[theJob];
                                 }
                              } else if (!GameConstants.isCape(baseitemid) && !GameConstants.isGlove(baseitemid)) {
                                 if (GameConstants.isShoes(baseitemid)) {
                                    a = true;
                                    Random = (int)Math.floor(Math.random() * (double)신발.length - 1.0D);
                                    MItemidselect = 신발[Random];
                                    if (c.getPlayer().getGender() == 0) {
                                       while(a) {
                                          if (GameConstants.여자신발(MItemidselect)) {
                                             Random = (int)Math.floor(Math.random() * (double)신발.length - 1.0D);
                                             MItemidselect = 신발[Random];
                                          } else {
                                             a = false;
                                          }
                                       }
                                    } else {
                                       while(a) {
                                          if (GameConstants.남자신발(MItemidselect)) {
                                             Random = (int)Math.floor(Math.random() * (double)신발.length - 1.0D);
                                             MItemidselect = 신발[Random];
                                          } else {
                                             a = false;
                                          }
                                       }
                                    }
                                 } else if (GameConstants.isWeapon(baseitemid)) {
                                    theJob = (int)Math.floor(Math.random() * (double)무기.length - 1.0D);
                                    MItemidselect = 무기[theJob];
                                 }
                              } else {
                                 theJob = (int)Math.floor(Math.random() * (double)망토장갑.length - 1.0D);
                                 MItemidselect = 망토장갑[theJob];
                              }

                              마라벨인가 = true;
                           } else {
                              if (equip4.getEquipmentType() == 1) {
                                 Label = 2;
                              } else {
                                 Label = 1;
                              }

                              if (GameConstants.isCap(baseitemid)) {
                                 a = true;
                                 Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                 MItemidselect = SLabel[Random];
                                 if (c.getPlayer().getGender() == 0) {
                                    while(a) {
                                       if (!GameConstants.isCap(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else if (GameConstants.여자모자(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else {
                                          a = false;
                                       }
                                    }
                                 } else {
                                    while(a) {
                                       if (!GameConstants.isCap(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else if (GameConstants.남자모자(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else {
                                          a = false;
                                       }
                                    }
                                 }
                              } else if (GameConstants.isLongcoat(baseitemid)) {
                                 a = true;
                                 Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                 MItemidselect = SLabel[Random];
                                 if (c.getPlayer().getGender() == 0) {
                                    while(a) {
                                       if (!GameConstants.isLongcoat(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else if (GameConstants.여자한벌(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else {
                                          a = false;
                                       }
                                    }
                                 } else {
                                    while(a) {
                                       if (!GameConstants.isLongcoat(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else if (GameConstants.남자한벌(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else {
                                          a = false;
                                       }
                                    }
                                 }
                              } else if (GameConstants.isCape(baseitemid)) {
                                 a = true;
                                 Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                 MItemidselect = SLabel[Random];

                                 label5443:
                                 while(true) {
                                    while(true) {
                                       if (!a) {
                                          break label5443;
                                       }

                                       if (!GameConstants.isCape(MItemidselect) && !GameConstants.isGlove(baseitemid)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else {
                                          a = false;
                                       }
                                    }
                                 }
                              } else if (!GameConstants.isGlove(baseitemid)) {
                                 if (GameConstants.isShoes(baseitemid)) {
                                    a = true;
                                    Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                    MItemidselect = SLabel[Random];
                                    if (c.getPlayer().getGender() == 0) {
                                       while(a) {
                                          if (!GameConstants.isShoes(MItemidselect)) {
                                             Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                             MItemidselect = SLabel[Random];
                                          } else if (GameConstants.여자신발(MItemidselect)) {
                                             Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                             MItemidselect = SLabel[Random];
                                          } else {
                                             a = false;
                                          }
                                       }
                                    } else {
                                       while(a) {
                                          if (!GameConstants.isShoes(MItemidselect)) {
                                             Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                             MItemidselect = SLabel[Random];
                                          } else if (GameConstants.남자신발(MItemidselect)) {
                                             Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                             MItemidselect = SLabel[Random];
                                          } else {
                                             a = false;
                                          }
                                       }
                                    }
                                 } else if (GameConstants.isWeapon(baseitemid)) {
                                    a = true;

                                    while(a) {
                                       if (!GameConstants.isWeapon(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else {
                                          a = false;
                                       }
                                    }
                                 }
                              } else {
                                 a = true;
                                 Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                 MItemidselect = SLabel[Random];

                                 label5460:
                                 while(true) {
                                    while(true) {
                                       if (!a) {
                                          break label5460;
                                       }

                                       if (!GameConstants.isGlove(MItemidselect) && !GameConstants.isCape(MItemidselect)) {
                                          Random = (int)Math.floor(Math.random() * (double)SLabel.length);
                                          MItemidselect = SLabel[Random];
                                       } else {
                                          a = false;
                                       }
                                    }
                                 }
                              }
                           }

                           if (MItemidselect <= 0) {
                              c.getPlayer().dropMessage(1, "마스터 피스가 준비되지 않았습니다. 다시 시도해주세요.");
                              mapleMap = c.getChannelServer().getMapFactory().getMap(c.getPlayer().getMapId());
                              c.getPlayer().changeMap(mapleMap, mapleMap.getPortal(0));
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           equip6 = (Equip)mapleItemInformationProvider5.getEquipById(MItemidselect);
                           if (equip6 == null) {
                              c.getPlayer().dropMessage(1, "마스터 피스가 준비되지 않았습니다. 다시 시도해주세요.");
                              mapleMap = c.getChannelServer().getMapFactory().getMap(c.getPlayer().getMapId());
                              c.getPlayer().changeMap(mapleMap, mapleMap.getPortal(0));
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           equip6.setEquipmentType(Label);
                           int own;
                           if (GameConstants.isWeapon(equip6.getItemId())) {
                              공 = false;
                              suc = false;
                              int attack = false;
                              stat2 = false;
                              stat1 = Randomizer.isSuccess(50) ? 21 : 22;
                              if (stat1 == 21) {
                                 공 = true;
                              }

                              own = stat1 * 1000 + (마라벨인가 ? 60 : Randomizer.rand(25, 35));
                              if (Randomizer.isSuccess(60)) {
                                 suc = true;
                                 two = (공 ? 22 : 21) * 1000 + (마라벨인가 ? 60 : Randomizer.rand(25, 35));
                              }

                              if (Randomizer.isSuccess(10)) {
                                 three = (Randomizer.isSuccess(50) ? 21 : 22) * 1000 + (마라벨인가 ? 60 : Randomizer.rand(25, 35));
                                 if (!suc) {
                                    three = (공 ? 22 : 21) * 1000 + (마라벨인가 ? 60 : Randomizer.rand(25, 35));
                                 }
                              }
                           } else {
                              공 = false;
                              suc = false;
                              stat1 = 0;
                              stat2 = false;
                              stat = Randomizer.rand(11, 14);
                              own = stat * 1000 + (마라벨인가 ? 60 : Randomizer.rand(25, 35));
                              if (Randomizer.isSuccess(60)) {
                                 공 = true;

                                 for(stat1 = Randomizer.rand(11, 14); stat == stat1; stat1 = Randomizer.rand(11, 14)) {
                                 }

                                 two = stat1 * 1000 + (마라벨인가 ? 60 : Randomizer.rand(25, 35));
                              }

                              if (마라벨인가 && Randomizer.isSuccess(30)) {
                                 if (!공) {
                                    for(stat1 = Randomizer.rand(11, 14); stat == stat1; stat1 = Randomizer.rand(11, 14)) {
                                    }

                                    three = stat1 * 1000 + (마라벨인가 ? 60 : Randomizer.rand(25, 35));
                                 } else {
                                    int stat2 = Randomizer.rand(11, 14);

                                    while(true) {
                                       if (stat1 != stat2 && stat != stat2) {
                                          three = stat2 * 1000 + (마라벨인가 ? 60 : Randomizer.rand(25, 35));
                                          break;
                                       }

                                       stat2 = Randomizer.rand(11, 14);
                                    }
                                 }
                              }
                           }

                           if (c.getPlayer().getQuestStatus(50008) == 1) {
                              c.getPlayer().setKeyValue(50008, "1", "1");
                           }

                           equip6.setCoption1(own);
                           equip6.setCoption2(two);
                           equip6.setCoption3(three);
                           i8 = equip6.getFlag();
                           if (itemId == 5069001) {
                              i8 |= ItemFlag.KARMA_EQUIP.getValue();
                              i8 |= ItemFlag.CHARM_EQUIPED.getValue();
                           }

                           equip6.setUniqueId(MapleInventoryIdentifier.getInstance());
                           equip6.setFlag(i8);
                           equip6.setOptionExpiration(System.currentTimeMillis() + (Randomizer.isSuccess(10) ? 60L : (Randomizer.isSuccess(30) ? 28L : 14L)) * 24L * 60L * 60L * 1000L);
                           equip6.setKarmaCount((byte)-1);
                           MapleInventoryManipulator.addbyItem(c, equip6);
                           MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CODY, equip4.getPosition(), (short)1, false);
                           MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CODY, equip5.getPosition(), (short)1, false);
                           c.getSession().writeAndFlush(CSPacket.LunaCrystal(equip6));
                           if (마라벨인가) {
                              World.Broadcast.broadcastMessage(CWvsContext.serverMessage(11, c.getPlayer().getClient().getChannel(), "", c.getPlayer().getName() + "님이 프리미엄 마스터피스에서 {} 을 획득하였습니다.", true, equip6));
                           }

                           used = true;
                           break;
                        } catch (Exception var179) {
                           c.getPlayer().dropMessage(1, "마스터 피스가 준비되지 않았습니다. 다시 시도해주세요.");
                           mapleMap = c.getChannelServer().getMapFactory().getMap(c.getPlayer().getMapId());
                           c.getPlayer().changeMap(mapleMap, mapleMap.getPortal(0));
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           return;
                        }
                     case 5069100:
                        int[] wonderblack = new int[]{5000762, 5000763, 5000764, 5000790, 5000791, 5000792, 5000918, 5000919, 5000920, 5000933, 5000934, 5000935, 5000963, 5000964, 5000965, 5002033, 5002034, 5002035, 5002082, 5002083, 5002084, 5002137, 5002138, 5002139, 5002161, 5002162, 5002163, 5002186, 5002187, 5002188, 5002200, 5002201, 5002202, 5002226, 5002227, 5002228};
                        int[] luna = new int[]{5000930, 5000931, 5000932, 5002079, 5002080, 5002081, 5002254, 5002255, 5002256};
                        slea.skip(4);
                        short baseslot = slea.readShort();
                        slea.skip(12);
                        short usingslot = slea.readShort();
                        Item baseitem = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(baseslot);
                        Item usingitem = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(usingslot);
                        int basegrade = baseitem.getPet().getWonderGrade();
                        if (baseitem == null || usingitem == null) {
                           return;
                        }

                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, baseitem.getPosition(), (short)1, false);
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, usingitem.getPosition(), (short)1, false);
                        List<Pair<Integer, Integer>> random = new ArrayList();
                        boolean event = Calendar.getInstance().get(7) == 3;
                        boolean sp = false;
                        MaplePet maplePet3 = null;
                        Item item12 = null;
                        if (basegrade == 1) {
                           random.add(new Pair(1, 9140 + (event ? -500 : 0)));
                           random.add(new Pair(2, 664 + (event ? 500 : 0)));
                           random.add(new Pair(3, 196));
                        } else if (basegrade == 4) {
                           random.add(new Pair(1, 8460 + (event ? -500 : 0)));
                           random.add(new Pair(2, 1140 + (event ? 500 : 0)));
                           random.add(new Pair(3, 400));
                        }

                        int i7 = GameConstants.getWeightedRandom(random);
                        int itemidselect = false;
                        if (i7 == 1 || i7 == 2) {
                           int itemidselect = wonderblack[Randomizer.rand(0, wonderblack.length - 1)];
                           if (i7 == 2) {
                              itemidselect = luna[Randomizer.rand(0, luna.length - 1)];
                              sp = true;
                           }

                           maplePet3 = MaplePet.createPet(itemidselect, -1L);
                           if (basegrade == 1 && i7 == 1) {
                              maplePet3.setWonderGrade(4);
                           } else if (i7 == 1) {
                              maplePet3.setWonderGrade(5);
                           }

                           Connection con = null;

                           try {
                              con = DatabaseConnection.getConnection();
                              maplePet3.saveToDb(con);
                              con.close();
                           } catch (Exception var177) {
                              try {
                                 if (con != null) {
                                    con.close();
                                 }
                              } catch (Exception var176) {
                              }
                           } finally {
                              try {
                                 if (con != null) {
                                    con.close();
                                 }
                              } catch (Exception var175) {
                              }

                           }

                           item12 = MapleInventoryManipulator.addId_Item(c, itemidselect, (short)1, "", maplePet3, 30L, "", false);
                           if (item12 != null) {
                              c.getSession().writeAndFlush(CSPacket.LunaCrystal(item12));
                              if (sp) {
                                 World.Broadcast.broadcastMessage(CWvsContext.serverMessage(11, c.getPlayer().getClient().getChannel(), "", c.getPlayer().getName() + "님이 루나 크리스탈에서 {} 을 획득하였습니다.", true, item12));
                              }

                              used = true;
                           }
                           break;
                        }
                     case 5068301:
                        if (c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= 1 && c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= 1 && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                           new ArrayList();
                           int[] UniqueItem = new int[]{5000762, 5000763, 5000764, 5000790, 5000791, 5000792, 5000918, 5000919, 5000920, 5000933, 5000934, 5000935, 5000963, 5000964, 5000965, 5002033, 5002034, 5002035, 5002082, 5002083, 5002084, 5002137, 5002138, 5002139, 5002161, 5002162, 5002163, 5002186, 5002187, 5002188, 5002200, 5002201, 5002202, 5002226, 5002227, 5002228};
                           공 = false;
                           suc = true;
                           stat1 = Randomizer.rand(0, UniqueItem.length - 1);
                           i8 = UniqueItem[stat1];
                           MapleItemInformationProvider ii7 = MapleItemInformationProvider.getInstance();
                           if (GameConstants.isPet(i8)) {
                              item6 = MapleInventoryManipulator.addId_Item(c, i8, (short)1, "", MaplePet.createPet(i8, -1L), 30L, "", false);
                           } else {
                              if (GameConstants.getInventoryType(i8) == MapleInventoryType.EQUIP) {
                                 item6 = ii7.getEquipById(i8);
                              } else {
                                 item6 = new Item(i8, (short)0, (short)1);
                              }

                              if (MapleItemInformationProvider.getInstance().isCash(i8)) {
                                 item6.setUniqueId(MapleInventoryIdentifier.getInstance());
                              }

                              MapleInventoryManipulator.addbyItem(c, item6);
                           }

                           if (item6 != null) {
                              World.Broadcast.broadcastMessage(CWvsContext.serverMessage(11, c.getPlayer().getClient().getChannel(), "", c.getPlayer().getName() + "님이 위습의 블랙베리에서 {} 을 획득하였습니다.", true, item6));
                              c.getSession().writeAndFlush(CSPacket.WonderBerry((byte)1, item6, toUse.getItemId()));
                           }

                           used = true;
                        } else {
                           c.getPlayer().dropMessage(5, "소비, 캐시, 장비 여유 공간이 각각 한칸이상 부족합니다.");
                        }
                        break;
                     case 5070000:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "Cannot be used here.");
                        } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                           str = slea.readMapleAsciiString();
                           if (str.length() <= 65) {
                              stringBuilder = new StringBuilder();
                              addMedalString(c.getPlayer(), stringBuilder);
                              stringBuilder.append(c.getPlayer().getName());
                              stringBuilder.append(" : ");
                              stringBuilder.append(str);
                              c.getPlayer().getMap().broadcastMessage(CWvsContext.serverNotice(2, c.getPlayer().getName(), stringBuilder.toString()));
                              DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), str, "채널 : " + c.getChannel());
                              used = true;
                           }
                        } else {
                           c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                        }
                        break;
                     case 5071000:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "Cannot be used here.");
                        } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                           str = slea.readMapleAsciiString();
                           if (str.length() <= 65) {
                              stringBuilder = new StringBuilder();
                              addMedalString(c.getPlayer(), stringBuilder);
                              stringBuilder.append(c.getPlayer().getName());
                              stringBuilder.append(" : ");
                              stringBuilder.append(str);
                              if (System.currentTimeMillis() - World.Broadcast.chatDelay >= 3000L) {
                                 World.Broadcast.chatDelay = System.currentTimeMillis();
                                 c.getChannelServer().broadcastSmegaPacket(CWvsContext.serverNotice(2, c.getPlayer().getName(), stringBuilder.toString()));
                                 DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), str, "채널 : " + c.getChannel());
                                 used = true;
                              } else {
                                 c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "전체 채팅은 3초마다 하실 수 있습니다."));
                              }
                           }
                        } else {
                           c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                        }
                        break;
                     case 5072000:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "Cannot be used here.");
                        } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                           str = slea.readMapleAsciiString();
                           if (str.length() <= 65) {
                              stringBuilder = new StringBuilder();
                              addMedalString(c.getPlayer(), stringBuilder);
                              stringBuilder.append(c.getPlayer().getName());
                              stringBuilder.append(" : ");
                              stringBuilder.append(str);
                              공 = slea.readByte() != 0;
                              if (System.currentTimeMillis() - World.Broadcast.chatDelay >= 3000L) {
                                 World.Broadcast.chatDelay = System.currentTimeMillis();
                                 DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), str, "채널 : " + c.getChannel());
                                 World.Broadcast.broadcastSmega(CWvsContext.serverNotice(3, c.getChannel(), c.getPlayer().getName(), stringBuilder.toString(), 공));
                                 used = true;
                              } else {
                                 c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "전체 채팅은 3초마다 하실 수 있습니다."));
                              }
                           }
                        } else {
                           c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                        }
                        break;
                     case 5073000:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "Cannot be used here.");
                        } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                           str = slea.readMapleAsciiString();
                           if (str.length() <= 65) {
                              stringBuilder = new StringBuilder();
                              addMedalString(c.getPlayer(), stringBuilder);
                              stringBuilder.append(c.getPlayer().getName());
                              stringBuilder.append(" : ");
                              stringBuilder.append(str);
                              공 = slea.readByte() != 0;
                              if (System.currentTimeMillis() - World.Broadcast.chatDelay >= 3000L) {
                                 World.Broadcast.chatDelay = System.currentTimeMillis();
                                 DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), str, "채널 : " + c.getChannel());
                                 World.Broadcast.broadcastSmega(CWvsContext.serverNotice(9, c.getChannel(), c.getPlayer().getName(), stringBuilder.toString(), 공));
                                 used = true;
                              } else {
                                 c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "전체 채팅은 3초마다 하실 수 있습니다."));
                              }
                           }
                        } else {
                           c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                        }
                        break;
                     case 5074000:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "Cannot be used here.");
                        } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                           str = slea.readMapleAsciiString();
                           if (str.length() <= 65) {
                              stringBuilder = new StringBuilder();
                              addMedalString(c.getPlayer(), stringBuilder);
                              stringBuilder.append(c.getPlayer().getName());
                              stringBuilder.append(" : ");
                              stringBuilder.append(str);
                              공 = slea.readByte() != 0;
                              if (System.currentTimeMillis() - World.Broadcast.chatDelay >= 3000L) {
                                 World.Broadcast.chatDelay = System.currentTimeMillis();
                                 DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), str, "채널 : " + c.getChannel());
                                 World.Broadcast.broadcastSmega(CWvsContext.serverNotice(22, c.getChannel(), c.getPlayer().getName(), stringBuilder.toString(), 공));
                                 used = true;
                              } else {
                                 c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "전체 채팅은 3초마다 하실 수 있습니다."));
                              }
                           }
                        } else {
                           c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                        }
                        break;
                     case 5075003:
                     case 5075004:
                     case 5075005:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "Cannot be used here.");
                        } else {
                           int tvType = itemId % 10;
                           if (tvType == 3) {
                              slea.readByte();
                           }

                           ear = tvType != 1 && tvType != 2 && slea.readByte() > 1;
                           MapleCharacter victim = tvType != 1 && tvType != 4 ? c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString()) : null;
                           if (tvType != 0 && tvType != 3) {
                              if (victim == null) {
                                 c.getPlayer().dropMessage(1, "That character is not in the channel.");
                                 break;
                              }
                           } else {
                              victim = null;
                           }

                           String str1 = slea.readMapleAsciiString();
                           if (System.currentTimeMillis() - World.Broadcast.chatDelay >= 3000L) {
                              World.Broadcast.chatDelay = System.currentTimeMillis();
                              DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), str1, "채널 : " + c.getChannel());
                              World.Broadcast.broadcastSmega(CWvsContext.serverNotice(3, c.getChannel(), c.getPlayer().getName(), c.getPlayer().getName() + " : " + str1, ear));
                              used = true;
                           } else {
                              c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "전체 채팅은 3초마다 하실 수 있습니다."));
                           }
                        }
                        break;
                     case 5076000:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "Cannot be used here.");
                        } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                           str = slea.readMapleAsciiString();
                           if (str.length() <= 65) {
                              stringBuilder = new StringBuilder();
                              addMedalString(c.getPlayer(), stringBuilder);
                              stringBuilder.append(c.getPlayer().getName());
                              stringBuilder.append(" : ");
                              stringBuilder.append(str);
                              공 = slea.readByte() > 0;
                              Item item13 = null;
                              if (slea.readByte() == 1) {
                                 byte invType = (byte)slea.readInt();
                                 byte b1 = (byte)slea.readInt();
                                 if (b1 <= 0) {
                                    invType = -1;
                                 }

                                 item13 = c.getPlayer().getInventory(MapleInventoryType.getByType(invType)).getItem((short)b1);
                              }

                              if (System.currentTimeMillis() - World.Broadcast.chatDelay >= 3000L) {
                                 World.Broadcast.chatDelay = System.currentTimeMillis();
                                 DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), str, "채널 : " + c.getChannel());
                                 World.Broadcast.broadcastSmega(CWvsContext.itemMegaphone(c.getPlayer().getName(), stringBuilder.toString(), 공, c.getChannel(), item13, itemId));
                                 used = true;
                              } else {
                                 c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "전체 채팅은 3초마다 하실 수 있습니다."));
                              }
                           }
                        } else {
                           c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                        }
                        break;
                     case 5076100:
                        String message = slea.readMapleAsciiString();
                        StringBuilder sb = new StringBuilder();
                        addMedalString(c.getPlayer(), sb);
                        sb.append(c.getPlayer().getName());
                        sb.append(" : ");
                        sb.append(message);
                        ear = slea.readByte() > 0;
                        item = null;
                        if (slea.readInt() == 1) {
                           numLines = (byte)slea.readInt();
                           byte pos2 = (byte)slea.readInt();
                           if (pos2 <= 0) {
                              numLines = -1;
                           }

                           item = c.getPlayer().getInventory(MapleInventoryType.getByType(numLines)).getItem((short)pos2);
                        }

                        World.Broadcast.broadcastSmega(CWvsContext.HyperMegaPhone(sb.toString(), c.getPlayer().getName(), message, c.getChannel(), ear, item));
                        used = true;
                        break;
                     case 5077000:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "Cannot be used here.");
                        } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                           numLines = slea.readByte();
                           if (numLines > 3) {
                              return;
                           }

                           List<String> messages = new LinkedList();

                           for(i8 = 0; i8 < numLines; ++i8) {
                              str = slea.readMapleAsciiString();
                              if (str.length() > 65) {
                                 break;
                              }

                              DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), str, "채널 : " + c.getChannel());
                              String var245 = c.getPlayer().getName();
                              messages.add(var245 + " : " + str);
                           }

                           공 = slea.readByte() > 0;
                           if (System.currentTimeMillis() - World.Broadcast.chatDelay >= 3000L) {
                              World.Broadcast.chatDelay = System.currentTimeMillis();
                              World.Broadcast.broadcastSmega(CWvsContext.tripleSmega(c.getPlayer().getName(), messages, 공, c.getChannel()));
                              used = true;
                           } else {
                              c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "전체 채팅은 3초마다 하실 수 있습니다."));
                           }
                        } else {
                           c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                        }
                        break;
                     case 5079000:
                     case 5079001:
                     case 5390000:
                     case 5390001:
                     case 5390002:
                     case 5390003:
                     case 5390004:
                     case 5390005:
                     case 5390006:
                     case 5390007:
                     case 5390008:
                     case 5390009:
                     case 5390010:
                     case 5390011:
                     case 5390012:
                     case 5390013:
                     case 5390014:
                     case 5390015:
                     case 5390016:
                     case 5390017:
                     case 5390018:
                     case 5390019:
                     case 5390020:
                     case 5390021:
                     case 5390022:
                     case 5390023:
                     case 5390024:
                     case 5390025:
                     case 5390026:
                     case 5390027:
                     case 5390028:
                     case 5390029:
                     case 5390030:
                     case 5390031:
                     case 5390032:
                     case 5390033:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "10 레벨 이상이어야합니다.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "여기에서는 사용하실 수 없습니다.");
                        } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                           List<String> lines = new LinkedList();
                           stringBuilder = new StringBuilder();

                           for(i8 = 0; i8 < 4; ++i8) {
                              str = slea.readMapleAsciiString();
                              if (str.length() > 55) {
                                 lines.add("");
                              } else {
                                 lines.add(str);
                                 stringBuilder.append(str);
                              }
                           }

                           공 = slea.readByte() != 0;
                           if (System.currentTimeMillis() - World.Broadcast.chatDelay >= 3000L) {
                              World.Broadcast.chatDelay = System.currentTimeMillis();
                              DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), stringBuilder.toString(), "채널 : " + c.getChannel());
                              World.Broadcast.broadcastSmega(CWvsContext.getAvatarMega(c.getPlayer(), c.getChannel(), itemId, lines, 공));
                              used = true;
                           } else {
                              c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "전체 채팅은 3초마다 하실 수 있습니다."));
                           }
                        } else {
                           c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                        }
                        break;
                     case 5079004:
                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                        } else if (c.getPlayer().getMapId() == 180000002) {
                           c.getPlayer().dropMessage(5, "Cannot be used here.");
                        } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                           str = slea.readMapleAsciiString();
                           if (str.length() <= 65) {
                              if (System.currentTimeMillis() - World.Broadcast.chatDelay >= 3000L) {
                                 World.Broadcast.chatDelay = System.currentTimeMillis();
                                 World.Broadcast.broadcastSmega(CWvsContext.echoMegaphone(c.getPlayer().getName(), str));
                                 DBLogger.getInstance().logChat(LogType.Chat.Megaphone, c.getPlayer().getId(), c.getPlayer().getName(), str, "채널 : " + c.getChannel());
                                 used = true;
                              } else {
                                 c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "전체 채팅은 3초마다 하실 수 있습니다."));
                              }
                           }
                        } else {
                           c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                        }
                        break;
                     case 5100000:
                        c.getPlayer().getMap().broadcastMessage(CField.musicChange("Jukebox/Congratulation"));
                        used = true;
                        break;
                     case 5130000:
                        if (c.getPlayer().getKeyValue(210416, "TotalDeadTime") <= 0L) {
                           c.getPlayer().dropMessage(5, "캐릭터 사망으로 인한 경험치 획득, 드롭률 감소 효과가 적용 중일 때에만 사용할 수 있습니다.");
                           c.send(CWvsContext.enableActions(c.getPlayer()));
                        } else {
                           c.send(CField.ExpDropPenalty(false, 0, 0, 0, 0));
                           c.getPlayer().removeKeyValue(210416);
                           used = true;
                        }
                        break;
                     case 5150132:
                     case 5152020:
                     case 5153015:
                        int unk = slea.readInt();
                        slea.skip(2);
                        int code = slea.readInt();
                        if (itemId == 5150132) {
                           if (!c.getPlayer().getDressup() && (!GameConstants.isZero(c.getPlayer().getJob()) || c.getPlayer().getGender() != 1)) {
                              c.getPlayer().setHair(code);
                              c.getPlayer().updateSingleStat(MapleStat.HAIR, (long)code);
                           } else {
                              c.getPlayer().setSecondHair(code);
                              if (c.getPlayer().getDressup()) {
                                 c.getPlayer().updateAngelicStats();
                              } else {
                                 c.send(CWvsContext.updateZeroSecondStats(c.getPlayer()));
                              }
                           }

                           c.getPlayer().equipChanged();
                        } else if (itemId == 5153015) {
                           if (c.getPlayer().getDressup() || GameConstants.isZero(c.getPlayer().getJob()) && c.getPlayer().getGender() == 1) {
                              c.getPlayer().setSecondSkinColor((byte)code);
                              if (c.getPlayer().getDressup()) {
                                 c.getPlayer().updateAngelicStats();
                              } else {
                                 c.send(CWvsContext.updateZeroSecondStats(c.getPlayer()));
                              }
                           } else {
                              c.getPlayer().setSkinColor((byte)code);
                              c.getPlayer().updateSingleStat(MapleStat.SKIN, (long)code);
                           }

                           c.getPlayer().equipChanged();
                        } else if (itemId == 5152020) {
                           if (c.getPlayer().getDressup() || GameConstants.isZero(c.getPlayer().getJob()) && c.getPlayer().getGender() == 1) {
                              c.getPlayer().setSecondFace(code);
                              if (c.getPlayer().getDressup()) {
                                 c.getPlayer().updateAngelicStats();
                              } else {
                                 c.send(CWvsContext.updateZeroSecondStats(c.getPlayer()));
                              }
                           } else {
                              c.getPlayer().setFace(code);
                              c.getPlayer().updateSingleStat(MapleStat.FACE, (long)code);
                           }

                           c.getPlayer().equipChanged();
                        }

                        c.getPlayer().dropMessage(5, "성공적으로 변경 되었습니다.");
                        c.send(CWvsContext.enableActions(c.getPlayer()));
                        break;
                     case 5152300:
                        forbiddenFaces = new int[]{22100, 22200, 22300, 22400, 22500, 22600, 22700, 22800};
                        var192 = forbiddenFaces;
                        Random = forbiddenFaces.length;

                        for(i8 = 0; i8 < Random; ++i8) {
                           stat = var192[i8];
                           if (c.getPlayer().getFace() == stat) {
                              used = false;
                              c.getPlayer().dropMessage(1, "믹스렌즈가 불가능한 성형입니다.");
                              return;
                           }
                        }

                        boolean bool1 = slea.readByte() == 1;
                        boolean bool3 = slea.readByte() == 1;
                        int ordinaryColor = slea.readInt();
                        c.getPlayer().dropMessageGM(6, "ordinaryColor : " + ordinaryColor);
                        baseFace = c.getPlayer().getFace() < 100000 ? c.getPlayer().getFace() : c.getPlayer().getFace() / 1000;
                        i4 = ordinaryColor / 10000;
                        c.getPlayer().dropMessageGM(6, "i4 : " + i4);
                        i5 = ordinaryColor / 1000 - i4 * 10;
                        c.getPlayer().dropMessageGM(6, "i5 : " + i5);
                        int i6 = ordinaryColor % 100;
                        c.getPlayer().dropMessageGM(6, "i6 : " + i6);

                        while(i5 == i4) {
                           i5 = Randomizer.nextInt(8);
                        }

                        baseFace = baseFace - baseFace % 1000 + baseFace % 100 + i4 * 100;
                        c.getPlayer().dropMessageGM(6, "baseFace 1 : " + baseFace);
                        newFace = baseFace * 1000 + i5 * 100 + i6;
                        c.getPlayer().dropMessageGM(6, "baseFace 2 : " + newFace);
                        if (bool1) {
                           c.getPlayer().setSecondFace(newFace);
                           c.getPlayer().updateAngelicStats();
                        } else if (bool3) {
                           c.getPlayer().setSecondFace(newFace);
                           c.getPlayer().updateZeroStats();
                        } else {
                           c.getPlayer().setFace(newFace);
                           c.getPlayer().updateSingleStat(MapleStat.FACE, (long)newFace);
                        }

                        c.getSession().writeAndFlush(CWvsContext.mixLense(itemId, baseFace, newFace, bool1, bool3, false, c.getPlayer()));
                        c.getPlayer().equipChanged();
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        used = true;
                        break;
                     case 5152301:
                        forbiddenFaces = new int[]{22100, 22200, 22300, 22400, 22500, 22600, 22700, 22800};
                        var192 = forbiddenFaces;
                        Random = forbiddenFaces.length;

                        for(i8 = 0; i8 < Random; ++i8) {
                           stat = var192[i8];
                           if (c.getPlayer().getFace() == stat) {
                              used = false;
                              c.getPlayer().dropMessage(1, "믹스렌즈가 불가능한 성형입니다.");
                              return;
                           }
                        }

                        boolean dressUp = slea.readByte() == 1;
                        boolean isBeta = slea.readByte() == 1;
                        boolean isAlphaBeta = slea.readByte() == 1;
                        baseFace = c.getPlayer().getFace() < 100000 ? c.getPlayer().getFace() : c.getPlayer().getFace() / 1000;
                        i4 = Randomizer.nextInt(8);

                        for(i5 = Randomizer.nextInt(8); i5 == i4; i5 = Randomizer.nextInt(8)) {
                        }

                        baseFace = baseFace - baseFace % 1000 + baseFace % 100 + i4 * 100;
                        newFace = baseFace * 1000 + i5 * 100 + Randomizer.rand(1, 99);
                        c.getSession().writeAndFlush(CWvsContext.mixLense(itemId, baseFace, newFace, dressUp, isBeta, isAlphaBeta, c.getPlayer()));
                        if (dressUp) {
                           c.getPlayer().setSecondFace(newFace);
                           if (c.getPlayer().getDressup()) {
                              c.getPlayer().updateSingleStat(MapleStat.FACE, (long)newFace);
                           }
                        } else if (isBeta) {
                           c.getPlayer().setSecondFace(newFace);
                           if (c.getPlayer().getGender() == 1) {
                              c.getPlayer().updateSingleStat(MapleStat.FACE, (long)newFace);
                           }
                        } else if (isAlphaBeta) {
                           c.getPlayer().setFace(newFace);
                           c.getPlayer().updateSingleStat(MapleStat.FACE, (long)newFace);
                        } else {
                           c.getPlayer().setFace(newFace);
                           c.getPlayer().updateSingleStat(MapleStat.FACE, (long)newFace);
                        }

                        c.getPlayer().equipChanged();
                        used = true;
                        break;
                     case 5155000:
                     case 5155004:
                     case 5155005:
                        int j = slea.readInt();
                        String effect = "";
                        switch(j) {
                        case 0:
                           if (GameConstants.isMercedes(c.getPlayer().getJob())) {
                              j = 1;
                           }

                           if (GameConstants.isIllium(c.getPlayer().getJob())) {
                              j = 2;
                           }

                           if (GameConstants.isAdel(c.getPlayer().getJob()) || GameConstants.isArk(c.getPlayer().getJob())) {
                              j = 3;
                           }

                           effect = "Effect/BasicEff.img/JobChanged";
                           break;
                        case 1:
                           if (GameConstants.isMercedes(c.getPlayer().getJob())) {
                              j = 0;
                           }

                           effect = "Effect/BasicEff.img/JobChangedElf";
                           break;
                        case 2:
                           if (GameConstants.isIllium(c.getPlayer().getJob())) {
                              j = 0;
                           }

                           effect = "Effect/BasicEff.img/JobChangedIlliumFront";
                           break;
                        case 3:
                           if (GameConstants.isAdel(c.getPlayer().getJob()) || GameConstants.isArk(c.getPlayer().getJob())) {
                              j = 0;
                           }

                           effect = "Effect/BasicEff.img/JobChangedArkFront";
                        }

                        c.getPlayer().setKeyValue(7784, "sw", String.valueOf(j));
                        c.getSession().writeAndFlush(CField.EffectPacket.showEffect(c.getPlayer(), 0, itemId, 38, 2, 0, (byte)0, true, (Point)null, effect, (Item)null));
                        used = true;
                        break;
                     case 5155001:
                        if (GameConstants.isKaiser(c.getPlayer().getJob())) {
                           if (c.getPlayer().getKeyValue(7786, "sw") == 0L) {
                              c.getPlayer().setKeyValue(7786, "sw", "1");
                           } else {
                              c.getPlayer().setKeyValue(7786, "sw", "0");
                           }

                           c.getPlayer().dropMessage(5, "드래곤 테일 쉬프트의 신비로운 힘으로 모습이 바뀌었습니다.");
                           used = true;
                        } else {
                           c.getPlayer().dropMessage(5, "드래곤 테일 쉬프트는 카이저에게만 효과가 있는것 같다.");
                        }
                        break;
                     case 5155002:
                        if (c.getPlayer().getKeyValue(7786, "sw") == 0L) {
                           c.getPlayer().setKeyValue(7786, "sw", "1");
                           c.getPlayer().dropMessage(5, "신비한 힘으로 제너레이트 마크가 드러났습니다.");
                        } else {
                           c.getPlayer().setKeyValue(7786, "sw", "0");
                           c.getPlayer().dropMessage(5, "신비한 힘으로 제너레이트 마크가 감춰졌습니다.");
                        }

                        used = true;
                        break;
                     case 5155003:
                        if (c.getPlayer().getKeyValue(7786, "sw") == 0L) {
                           c.getPlayer().setKeyValue(7786, "sw", "1");
                           c.getPlayer().dropMessage(5, "신비한 힘으로 마족의 표식이 드러났습니다.");
                        } else {
                           c.getPlayer().setKeyValue(7786, "sw", "0");
                           c.getPlayer().dropMessage(5, "신비한 힘으로 마족의 표식이 감춰졌습니다.");
                        }

                        used = true;
                        break;
                     case 5155006:
                        if (c.getPlayer().getKeyValue(7786, "sw") == 0L) {
                           c.getPlayer().setKeyValue(7786, "sw", "1");
                           c.getPlayer().dropMessage(5, "신비한 힘으로 심연의 세례가 드러났습니다.");
                        } else {
                           c.getPlayer().setKeyValue(7786, "sw", "0");
                           c.getPlayer().dropMessage(5, "신비한 힘으로 심연의 세례가 감춰졌습니다.");
                        }

                        used = true;
                        break;
                     case 5170000:
                        long uniqueid = slea.readLong();
                        MaplePet pet = c.getPlayer().getPet(0L);
                        int slo = 0;
                        if (pet != null) {
                           label6441: {
                              if (pet.getUniqueId() != uniqueid) {
                                 pet = c.getPlayer().getPet(1L);
                                 slo = 1;
                                 if (pet == null) {
                                    break label6441;
                                 }

                                 if (pet.getUniqueId() != uniqueid) {
                                    pet = c.getPlayer().getPet(2L);
                                    slo = 2;
                                    if (pet == null || pet.getUniqueId() != uniqueid) {
                                       break label6441;
                                    }
                                 }
                              }

                              String str2 = slea.readMapleAsciiString();
                              var152 = GameConstants.RESERVED;
                              Random = var152.length;

                              for(i8 = 0; i8 < Random; ++i8) {
                                 str = var152[i8];
                                 if (pet.getName().indexOf(str) != -1 || str2.indexOf(str) != -1) {
                                    break;
                                 }
                              }

                              pet.setName(str2);
                              c.getSession().writeAndFlush(PetPacket.updatePet(c.getPlayer(), pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false, c.getPlayer().getPetLoot()));
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              c.getPlayer().getMap().broadcastMessage(CSPacket.changePetName(c.getPlayer(), str2, slo));
                              used = true;
                           }
                        }
                        break;
                     case 5190000:
                     case 5190001:
                     case 5190002:
                     case 5190003:
                     case 5190004:
                     case 5190005:
                     case 5190006:
                     case 5190010:
                     case 5190011:
                     case 5190012:
                     case 5190013:
                        uniqueId = slea.readLong();
                        maplePet1 = null;
                        petIndex = c.getPlayer().getPetIndex(uniqueId);
                        if (petIndex >= 0) {
                           maplePet1 = c.getPlayer().getPet((long)petIndex);
                        } else {
                           maplePet1 = c.getPlayer().getInventory(MapleInventoryType.CASH).findByUniqueId(uniqueId).getPet();
                        }

                        if (maplePet1 == null) {
                           c.getPlayer().dropMessage(1, "펫을 찾는데 실패하였습니다!");
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           return;
                        }

                        zz = MaplePet.PetFlag.getByAddId(itemId);
                        maplePet1.setFlags(maplePet1.getFlags() | zz.getValue());
                        c.getPlayer().getMap().broadcastMessage(PetPacket.updatePet(c.getPlayer(), maplePet1, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(maplePet1.getInventoryPosition()), false, c.getPlayer().getPetLoot()));
                        used = true;
                        break;
                     case 5191000:
                     case 5191001:
                     case 5191002:
                     case 5191003:
                     case 5191004:
                        uniqueId = slea.readLong();
                        maplePet1 = null;
                        petIndex = c.getPlayer().getPetIndex(uniqueId);
                        if (petIndex >= 0) {
                           maplePet1 = c.getPlayer().getPet((long)petIndex);
                        } else {
                           maplePet1 = c.getPlayer().getInventory(MapleInventoryType.CASH).findByUniqueId(uniqueId).getPet();
                        }

                        if (maplePet1 == null) {
                           c.getPlayer().dropMessage(1, "펫을 찾는데 실패하였습니다!");
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           return;
                        }

                        zz = MaplePet.PetFlag.getByAddId(itemId);
                        maplePet1.setFlags(maplePet1.getFlags() - zz.getValue());
                        c.getPlayer().getMap().broadcastMessage(PetPacket.updatePet(c.getPlayer(), maplePet1, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(maplePet1.getInventoryPosition()), false, c.getPlayer().getPetLoot()));
                        used = true;
                        break;
                     case 5240000:
                     case 5240001:
                     case 5240002:
                     case 5240003:
                     case 5240004:
                     case 5240005:
                     case 5240006:
                     case 5240007:
                     case 5240008:
                     case 5240009:
                     case 5240010:
                     case 5240011:
                     case 5240012:
                     case 5240013:
                     case 5240014:
                     case 5240015:
                     case 5240016:
                     case 5240017:
                     case 5240018:
                     case 5240019:
                     case 5240020:
                     case 5240021:
                     case 5240022:
                     case 5240023:
                     case 5240024:
                     case 5240025:
                     case 5240026:
                     case 5240027:
                     case 5240028:
                     case 5240029:
                     case 5240030:
                     case 5240031:
                     case 5240032:
                     case 5240033:
                     case 5240034:
                     case 5240035:
                     case 5240036:
                     case 5240037:
                     case 5240038:
                     case 5240039:
                     case 5240040:
                     case 5240088:
                        var196 = c.getPlayer().getPets();
                        Random = var196.length;

                        for(i8 = 0; i8 < Random; ++i8) {
                           maplePet = var196[i8];
                           if (maplePet != null && !maplePet.canConsume(itemId)) {
                              stat1 = c.getPlayer().getPetIndex(maplePet);
                              maplePet.setFullness(100);
                              if (maplePet.getCloseness() < 30000) {
                                 if (maplePet.getCloseness() + 100 > 30000) {
                                    maplePet.setCloseness(30000);
                                 } else {
                                    maplePet.setCloseness(maplePet.getCloseness() + 100);
                                 }

                                 if (maplePet.getCloseness() >= GameConstants.getClosenessNeededForLevel(maplePet.getLevel() + 1)) {
                                    maplePet.setLevel(maplePet.getLevel() + 1);
                                    c.getSession().writeAndFlush(CField.EffectPacket.showPetLevelUpEffect(c.getPlayer(), maplePet.getPetItemId(), true));
                                    c.getPlayer().getMap().broadcastMessage(CField.EffectPacket.showPetLevelUpEffect(c.getPlayer(), maplePet.getPetItemId(), false));
                                 }
                              }

                              c.getSession().writeAndFlush(PetPacket.updatePet(c.getPlayer(), maplePet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(maplePet.getInventoryPosition()), false, c.getPlayer().getPetLoot()));
                              c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(c.getPlayer().getId(), (byte)1, (byte)stat1, true), true);
                           }
                        }

                        used = true;
                        break;
                     case 5300000:
                     case 5300001:
                     case 5300002:
                        ii = MapleItemInformationProvider.getInstance();
                        ii.getItemEffect(itemId).applyTo(c.getPlayer(), true);
                        used = true;
                        break;
                     case 5330000:
                        c.getPlayer().setConversation(2);
                        c.getSession().writeAndFlush(CField.sendDuey((byte)9, (List)null, (List)null));
                        break;
                     case 5370000:
                     case 5370001:
                        c.getPlayer().setChalkboard(slea.readMapleAsciiString());
                        break;
                     case 5450000:
                     case 5450003:
                     case 5452001:
                        var192 = GameConstants.blockedMaps;
                        Random = var192.length;

                        for(i8 = 0; i8 < Random; ++i8) {
                           stat = var192[i8];
                           if (c.getPlayer().getMapId() == stat) {
                              c.getPlayer().dropMessage(5, "You may not use this command here.");
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }
                        }

                        if (c.getPlayer().getLevel() < 10) {
                           c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
                        } else if ((c.getPlayer().getMapId() < 680000210 || c.getPlayer().getMapId() > 680000502) && (c.getPlayer().getMapId() / 1000 != 980000 || c.getPlayer().getMapId() == 980000000) && c.getPlayer().getMapId() / 100 != 1030008 && c.getPlayer().getMapId() / 100 != 922010 && c.getPlayer().getMapId() / 10 != 13003000) {
                           MapleShopFactory.getInstance().getShop(61).sendShop(c);
                        } else {
                           c.getPlayer().dropMessage(5, "You may not use this command here.");
                        }
                        break;
                     case 5450005:
                        c.getPlayer().setConversation(4);
                        c.getPlayer().getStorage().sendStorage(c, 1022005);
                        break;
                     case 5500000:
                        item3 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                        mapleItemInformationProvider4 = MapleItemInformationProvider.getInstance();
                        days = true;
                        if (item3 != null && !GameConstants.isAccessory(item3.getItemId()) && item3.getExpiration() > -1L && !mapleItemInformationProvider4.isCash(item3.getItemId()) && System.currentTimeMillis() + 8640000000L > item3.getExpiration() + 86400000L) {
                           a = true;
                           var186 = GameConstants.RESERVED;
                           i8 = var186.length;

                           for(stat = 0; stat < i8; ++stat) {
                              z = var186[stat];
                              if (c.getPlayer().getName().indexOf(z) != -1 || item3.getOwner().indexOf(z) != -1) {
                                 a = false;
                              }
                           }

                           if (a) {
                              item3.setExpiration(item3.getExpiration() + 86400000L);
                              c.getPlayer().forceReAddItem(item3, MapleInventoryType.EQUIPPED);
                              used = true;
                           } else {
                              c.getPlayer().dropMessage(1, "It may not be used on this item.");
                           }
                        }
                        break;
                     case 5500001:
                        item3 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                        mapleItemInformationProvider4 = MapleItemInformationProvider.getInstance();
                        days = true;
                        if (item3 != null && !GameConstants.isAccessory(item3.getItemId()) && item3.getExpiration() > -1L && !mapleItemInformationProvider4.isCash(item3.getItemId()) && System.currentTimeMillis() + 8640000000L > item3.getExpiration() + 604800000L) {
                           a = true;
                           var186 = GameConstants.RESERVED;
                           i8 = var186.length;

                           for(stat = 0; stat < i8; ++stat) {
                              z = var186[stat];
                              if (c.getPlayer().getName().indexOf(z) != -1 || item3.getOwner().indexOf(z) != -1) {
                                 a = false;
                              }
                           }

                           if (a) {
                              item3.setExpiration(item3.getExpiration() + 604800000L);
                              c.getPlayer().forceReAddItem(item3, MapleInventoryType.EQUIPPED);
                              used = true;
                           } else {
                              c.getPlayer().dropMessage(1, "It may not be used on this item.");
                           }
                        }
                        break;
                     case 5500002:
                        item3 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                        mapleItemInformationProvider4 = MapleItemInformationProvider.getInstance();
                        days = true;
                        if (item3 != null && !GameConstants.isAccessory(item3.getItemId()) && item3.getExpiration() > -1L && !mapleItemInformationProvider4.isCash(item3.getItemId()) && System.currentTimeMillis() + 8640000000L > item3.getExpiration() + 1728000000L) {
                           a = true;
                           var186 = GameConstants.RESERVED;
                           i8 = var186.length;

                           for(stat = 0; stat < i8; ++stat) {
                              z = var186[stat];
                              if (c.getPlayer().getName().indexOf(z) != -1 || item3.getOwner().indexOf(z) != -1) {
                                 a = false;
                              }
                           }

                           if (a) {
                              item3.setExpiration(item3.getExpiration() + 1728000000L);
                              c.getPlayer().forceReAddItem(item3, MapleInventoryType.EQUIPPED);
                              used = true;
                           } else {
                              c.getPlayer().dropMessage(1, "It may not be used on this item.");
                           }
                        }
                        break;
                     case 5500005:
                        item3 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                        mapleItemInformationProvider4 = MapleItemInformationProvider.getInstance();
                        days = true;
                        if (item3 != null && !GameConstants.isAccessory(item3.getItemId()) && item3.getExpiration() > -1L && !mapleItemInformationProvider4.isCash(item3.getItemId()) && System.currentTimeMillis() + 8640000000L > item3.getExpiration() + 4320000000L) {
                           a = true;
                           var186 = GameConstants.RESERVED;
                           i8 = var186.length;

                           for(stat = 0; stat < i8; ++stat) {
                              z = var186[stat];
                              if (c.getPlayer().getName().indexOf(z) != -1 || item3.getOwner().indexOf(z) != -1) {
                                 a = false;
                              }
                           }

                           if (a) {
                              item3.setExpiration(item3.getExpiration() + 25032704L);
                              c.getPlayer().forceReAddItem(item3, MapleInventoryType.EQUIPPED);
                              used = true;
                           } else {
                              c.getPlayer().dropMessage(1, "It may not be used on this item.");
                           }
                        }
                        break;
                     case 5500006:
                        Item item2 = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                        MapleItemInformationProvider mapleItemInformationProvider3 = MapleItemInformationProvider.getInstance();
                        days = true;
                        if (item2 != null && !GameConstants.isAccessory(item2.getItemId()) && item2.getExpiration() > -1L && !mapleItemInformationProvider3.isCash(item2.getItemId()) && System.currentTimeMillis() + 8640000000L > item2.getExpiration() + 8553600000L) {
                           a = true;
                           var186 = GameConstants.RESERVED;
                           i8 = var186.length;

                           for(stat = 0; stat < i8; ++stat) {
                              z = var186[stat];
                              if (c.getPlayer().getName().indexOf(z) != -1 || item2.getOwner().indexOf(z) != -1) {
                                 a = false;
                              }
                           }

                           if (a) {
                              item2.setExpiration(item2.getExpiration() + -36334592L);
                              c.getPlayer().forceReAddItem(item2, MapleInventoryType.EQUIPPED);
                              used = true;
                           } else {
                              c.getPlayer().dropMessage(1, "It may not be used on this item.");
                           }
                        }
                        break;
                     case 5501001:
                     case 5501002:
                        Skill skil = SkillFactory.getSkill(slea.readInt());
                        if (skil != null && skil.getId() / 10000 == 8000 && c.getPlayer().getSkillLevel(skil) > 0 && skil.isTimeLimited() && GameConstants.getMountItem(skil.getId(), c.getPlayer()) > 0) {
                           long toAdd = (long)((itemId == 5501001 ? 30 : 60) * 24 * 60 * 60) * 1000L;
                           long expire = c.getPlayer().getSkillExpiry(skil);
                           if (expire >= System.currentTimeMillis() && expire + toAdd < System.currentTimeMillis() + 31536000000L) {
                              c.getPlayer().changeSingleSkillLevel(skil, c.getPlayer().getSkillLevel(skil), c.getPlayer().getMasterLevel(skil), expire + toAdd);
                              used = true;
                           }
                        }
                        break;
                     case 5520000:
                     case 5520001:
                        MapleInventoryType mapleInventoryType1 = MapleInventoryType.getByType((byte)slea.readInt());
                        Item item5 = c.getPlayer().getInventory(mapleInventoryType1).getItem((short)slea.readInt());
                        if (item5 != null && !ItemFlag.KARMA_EQUIP.check(item5.getFlag()) && !ItemFlag.KARMA_USE.check(item5.getFlag()) && (itemId == 5520000 && MapleItemInformationProvider.getInstance().isKarmaEnabled(item5.getItemId()) || itemId == 5520001 && MapleItemInformationProvider.getInstance().isPKarmaEnabled(item5.getItemId()))) {
                           theJob = item5.getFlag();
                           if (mapleInventoryType1 == MapleInventoryType.EQUIP) {
                              theJob += ItemFlag.KARMA_EQUIP.getValue();
                           } else {
                              theJob += ItemFlag.KARMA_USE.getValue();
                           }

                           if (item5.getType() == 1) {
                              neq = (Equip)item5;
                              if (neq.getKarmaCount() > 0) {
                                 neq.setKarmaCount((byte)(neq.getKarmaCount() - 1));
                              }
                           }

                           item5.setFlag(theJob);
                           c.getPlayer().forceReAddItem_NoUpdate(item5, mapleInventoryType1);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, mapleInventoryType1, item5));
                           used = true;
                        }
                        break;
                     case 5521000:
                        MapleInventoryType mapleInventoryType2 = MapleInventoryType.getByType((byte)slea.readInt());
                        item6 = c.getPlayer().getInventory(mapleInventoryType2).getItem((short)slea.readInt());
                        if (item6 != null && !ItemFlag.TRADEABLE_ONETIME_EQUIP.check(item6.getFlag()) && MapleItemInformationProvider.getInstance().isShareTagEnabled(item6.getItemId())) {
                           theJob = item6.getFlag();
                           if (mapleInventoryType2 != MapleInventoryType.EQUIP) {
                              return;
                           }

                           theJob += ItemFlag.TRADEABLE_ONETIME_EQUIP.getValue();
                           item6.setFlag(theJob);
                           c.getPlayer().forceReAddItem_NoUpdate(item6, mapleInventoryType2);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item6));
                           used = true;
                        }
                        break;
                     case 5570000:
                        slea.readInt();
                        Equip equip1 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)slea.readInt());
                        if (equip1 != null) {
                           if (GameConstants.canHammer(equip1.getItemId()) && MapleItemInformationProvider.getInstance().getSlots(equip1.getItemId()) > 0 && equip1.getViciousHammer() < 2) {
                              equip1.setViciousHammer((byte)(equip1.getViciousHammer() + 1));
                              equip1.setUpgradeSlots((byte)(equip1.getUpgradeSlots() + 1));
                              c.getPlayer().forceReAddItem(equip1, MapleInventoryType.EQUIP);
                              c.getSession().writeAndFlush(CSPacket.ViciousHammer(true, true));
                              used = true;
                           } else {
                              c.getPlayer().dropMessage(5, "You may not use it on this item.");
                              c.getSession().writeAndFlush(CSPacket.ViciousHammer(true, false));
                           }
                        }
                        break;
                     case 5700000:
                        slea.skip(8);
                        if (c.getPlayer().getAndroid() == null) {
                           c.getPlayer().dropMessage(1, "장착중인 안드로이드가 없어 작명 할 수 없습니다.");
                        } else {
                           String nName = slea.readMapleAsciiString();
                           var152 = GameConstants.RESERVED;
                           Random = var152.length;

                           for(i8 = 0; i8 < Random; ++i8) {
                              str = var152[i8];
                              if (c.getPlayer().getAndroid().getName().indexOf(str) != -1 || nName.indexOf(str) != -1) {
                                 break;
                              }
                           }

                           c.getPlayer().getAndroid().setName(nName);
                           c.getPlayer().setAndroid(c.getPlayer().getAndroid());
                           used = true;
                        }
                        break;
                     case 5781002:
                        long l1 = slea.readLong();
                        int color = slea.readInt();
                        MaplePet maplePet2 = c.getPlayer().getPet(0L);
                        int i3 = false;
                        if (maplePet2 != null) {
                           label5572: {
                              if (maplePet2.getUniqueId() != l1) {
                                 maplePet2 = c.getPlayer().getPet(1L);
                                 i3 = true;
                                 if (maplePet2 == null) {
                                    break label5572;
                                 }

                                 if (maplePet2.getUniqueId() != l1) {
                                    maplePet2 = c.getPlayer().getPet(2L);
                                    i3 = true;
                                    if (maplePet2 == null || maplePet2.getUniqueId() != l1) {
                                       break label5572;
                                    }
                                 }
                              }

                              maplePet2.setColor(color);
                              c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PetPacket.showPet(c.getPlayer(), maplePet2, false, false), true);
                           }
                        }
                        break;
                     default:
                        if (itemId / 10000 != 512 && itemId != 2432290) {
                           if (itemId / 10000 == 510) {
                              c.getPlayer().getMap().startJukebox(c.getPlayer().getName(), itemId);
                              used = true;
                           } else if (itemId / 10000 == 562) {
                              if (UseSkillBook(slot, itemId, c, c.getPlayer())) {
                                 c.getPlayer().gainSP(1);
                              }
                           } else if (itemId / 10000 == 553) {
                              UseRewardItem(slot, itemId, c, c.getPlayer());
                           } else if (itemId / 10000 == 524) {
                              var196 = c.getPlayer().getPets();
                              Random = var196.length;

                              for(i8 = 0; i8 < Random; ++i8) {
                                 maplePet = var196[i8];
                                 if (maplePet != null && maplePet.canConsume(itemId)) {
                                    stat1 = c.getPlayer().getPetIndex(maplePet);
                                    maplePet.setFullness(100);
                                    if (maplePet.getCloseness() < 30000) {
                                       if (maplePet.getCloseness() + 100 > 30000) {
                                          maplePet.setCloseness(30000);
                                       } else {
                                          maplePet.setCloseness(maplePet.getCloseness() + 100);
                                       }

                                       if (maplePet.getCloseness() >= GameConstants.getClosenessNeededForLevel(maplePet.getLevel() + 1)) {
                                          maplePet.setLevel(maplePet.getLevel() + 1);
                                          c.getSession().writeAndFlush(CField.EffectPacket.showPetLevelUpEffect(c.getPlayer(), maplePet.getPetItemId(), true));
                                          c.getPlayer().getMap().broadcastMessage(CField.EffectPacket.showPetLevelUpEffect(c.getPlayer(), maplePet.getPetItemId(), false));
                                       }
                                    }

                                    c.getSession().writeAndFlush(PetPacket.updatePet(c.getPlayer(), maplePet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(maplePet.getInventoryPosition()), false, c.getPlayer().getPetLoot()));
                                    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(c.getPlayer().getId(), (byte)1, (byte)stat1, true), true);
                                 }
                              }

                              used = true;
                           } else if (itemId / 10000 != 519) {
                           }
                        } else {
                           MapleItemInformationProvider mapleItemInformationProvider = MapleItemInformationProvider.getInstance();
                           mapleItemInformationProvider.getMsg(itemId);
                           String ourMsg = slea.readMapleAsciiString();
                           c.getPlayer().getMap().startMapEffect(ourMsg, itemId);
                           stat = mapleItemInformationProvider.getStateChangeItem(itemId);
                           if (stat != 0) {
                              Iterator var224 = c.getPlayer().getMap().getCharactersThreadsafe().iterator();

                              while(var224.hasNext()) {
                                 MapleCharacter mChar = (MapleCharacter)var224.next();
                                 mapleItemInformationProvider.getItemEffect(stat).applyTo(mChar, true);
                              }
                           }

                           used = true;
                        }
                     }

                     if (used) {
                        if (ItemFlag.KARMA_USE.check(toUse.getFlag())) {
                           toUse.setFlag(toUse.getFlag() - ItemFlag.KARMA_USE.getValue() + ItemFlag.UNTRADEABLE.getValue());
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.CASH, toUse));
                        }

                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (short)1, false, true);
                     }

                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     if (cc) {
                        if (!c.getPlayer().isAlive() || c.getPlayer().getEventInstance() != null || FieldLimitType.ChannelSwitch.check(c.getPlayer().getMap().getFieldLimit())) {
                           c.getPlayer().dropMessage(1, "Auto relog failed.");
                           return;
                        }

                        c.getPlayer().dropMessage(5, "Auto relogging. Please wait.");
                        c.getPlayer().fakeRelog();
                     }

                  }
               }
            }
         }
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static void pickupItem(MapleMapObject ob, MapleClient c, MapleCharacter chr) {
      MapleMapItem mapitem = (MapleMapItem)ob;

      try {
         mapitem.getLock().lock();
         if (mapitem.isPickedUp()) {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            return;
         }

         int bonus;
         if (mapitem.getItemId() == 2431174) {
            bonus = Randomizer.rand(1, 10) * 10;
            c.getPlayer().gainHonor(bonus);
            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
            removeItem(chr, mapitem, ob);
            return;
         }

         if (mapitem.getItemId() == 2433103) {
            c.getPlayer().gainHonor(10000);
            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
            removeItem(chr, mapitem, ob);
            return;
         }

         if (mapitem.getItemId() == 2433019) {
            bonus = Randomizer.rand(1000000, 100000000);
            chr.gainMeso((long)bonus, true);
            removeItem(chr, mapitem, ob);
            c.getPlayer().dropMessage(-8, "메소럭키백에서 메소를 " + bonus + "메소 만큼 휙득 하였습니다.");
            return;
         }

         if (mapitem.getItemId() == 2433979) {
            bonus = Randomizer.rand(1000000, 100000000);
            chr.gainMeso((long)bonus, true);
            removeItem(chr, mapitem, ob);
            c.getPlayer().dropMessage(-8, "메소럭키백에서 메소를 " + bonus + "메소 만큼 휙득 하였습니다.");
            return;
         }

         if (mapitem.getItemId() == 4001169 && c.getPlayer().getMap().getMonstermarble() != 20) {
            c.getPlayer().getMap().setMonstermarble(c.getPlayer().getMap().getMonstermarble() + 1);
            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
            c.getPlayer().getMap().broadcastMessage(CWvsContext.getTopMsg("몬스터구슬 " + c.getPlayer().getMap().getMonstermarble() + " / 20"));
            removeItem(chr, mapitem, ob);
            return;
         }

         if (mapitem.getItemId() == 4001169) {
            removeItem(chr, mapitem, ob);
            return;
         }

         int var10001;
         MapleMap var54;
         if (mapitem.getItemId() == 4001101 && c.getPlayer().getMap().getMoonCake() != 80) {
            c.getPlayer().getMap().setMoonCake(c.getPlayer().getMap().getMoonCake() + 1);
            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
            var54 = c.getPlayer().getMap();
            var10001 = c.getPlayer().getMap().getMoonCake();
            var54.broadcastMessage(CWvsContext.getTopMsg("어흥이를 위해 월묘의 떡 " + var10001 + "개를 모았습니다.  앞으로 " + (80 - c.getPlayer().getMap().getMoonCake()) + "개 더!"));
            removeItem(chr, mapitem, ob);
            return;
         }

         if (mapitem.getItemId() != 4001101) {
            if (mapitem.getItemId() == 4000884) {
               c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("달맞이꽃 씨앗을 되찾았습니다.", 5120016, true));
            }

            String wishCoinCheck;
            int mesos;
            int i;
            int mobid;
            if (mapitem.getItemId() >= 2432139 && mapitem.getItemId() < 2432149) {
               EventManager em = c.getChannelServer().getEventSM().getEventManager("KerningPQ");
               String stage4 = em.getProperty("stage4r");
               wishCoinCheck = c.getPlayer().getEventInstance().getProperty("stage4M");
               mesos = Integer.parseInt(wishCoinCheck);

               for(i = 1; i < 10; ++i) {
                  if (mapitem.getItemId() == 2432139 + i) {
                     if (mesos != c.getPlayer().getMap().getKerningPQ()) {
                        if (stage4 == "0") {
                           c.getPlayer().getMap().setKerningPQ(i);
                        } else {
                           byte var11 = -1;
                           switch(stage4.hashCode()) {
                           case 49:
                              if (stage4.equals("1")) {
                                 var11 = 0;
                              }
                              break;
                           case 50:
                              if (stage4.equals("2")) {
                                 var11 = 1;
                              }
                              break;
                           case 51:
                              if (stage4.equals("3")) {
                                 var11 = 2;
                              }
                              break;
                           case 52:
                              if (stage4.equals("4")) {
                                 var11 = 3;
                              }
                           }

                           switch(var11) {
                           case 0:
                              c.getPlayer().dropMessage(5, i + "/" + stage4);
                              c.getPlayer().getMap().setKerningPQ(c.getPlayer().getMap().getKerningPQ() + i);
                              c.getPlayer().dropMessage(5, c.getPlayer().getMap().getKerningPQ().makeConcatWithConstants<invokedynamic>(c.getPlayer().getMap().getKerningPQ()));
                              break;
                           case 1:
                              if (stage4 != "0") {
                                 c.getPlayer().getMap().setKerningPQ(c.getPlayer().getMap().getKerningPQ() - i);
                              }
                              break;
                           case 2:
                              if (stage4 != "0") {
                                 c.getPlayer().getMap().setKerningPQ(c.getPlayer().getMap().getKerningPQ() * i);
                              }
                              break;
                           case 3:
                              if (stage4 != "0") {
                                 mobid = (int)Math.floor((double)(c.getPlayer().getMap().getKerningPQ() / i));
                                 c.getPlayer().getMap().setKerningPQ(mobid);
                              }
                           }
                        }

                        c.getPlayer().getMap().broadcastMessage(CWvsContext.getTopMsg("현재 숫자 : " + c.getPlayer().getMap().getKerningPQ()));
                        c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("목표 숫자 : " + wishCoinCheck, 5120017, true));
                        if (mesos == c.getPlayer().getMap().getKerningPQ()) {
                           c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(75));
                           c.getPlayer().getMap().broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
                           c.getPlayer().getMap().broadcastMessage(CField.environmentChange("gate", 2));
                        }
                     }

                     c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                     removeItem(chr, mapitem, ob);
                     return;
                  }
               }
            }

            if (mapitem.getItemId() == 4001022) {
               c.getPlayer().getMap().setRPTicket(c.getPlayer().getMap().getRPTicket() + 1);
               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
               c.getPlayer().getMap().broadcastMessage(CWvsContext.getTopMsg("통행증 " + c.getPlayer().getMap().getRPTicket() + "장을 모았습니다."));
               if (c.getPlayer().getMap().getRPTicket() == 20) {
                  c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(10));
                  c.getPlayer().getMap().broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
                  c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("통행증을 모두 모았습니다. 레드 벌룬에게 말을 걸어 다음 단계로 이동해 주세요.", 5120018, true));
               }

               removeItem(chr, mapitem, ob);
               return;
            }

            if (mapitem.getItemId() == 4001022) {
               removeItem(chr, mapitem, ob);
               return;
            }

            if (mapitem.getItemId() != 2023484 && mapitem.getItemId() != 2023494 && mapitem.getItemId() != 2023495 && mapitem.getItemId() != 2023669 && mapitem.getItemId() != 2023927) {
               if (mapitem.getItemId() == 2434851) {
                  if (!chr.getBuffedValue(25121133)) {
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     return;
                  }

                  SecondaryStatEffect effect = SkillFactory.getSkill(25121133).getEffect(1);
                  long duration = chr.getBuffLimit(25121133);
                  if ((duration += 4000L) >= (long)effect.getCoolRealTime()) {
                     duration = (long)effect.getCoolRealTime();
                  }

                  effect.applyTo(chr, chr, false, chr.getPosition(), (int)duration, (byte)0, false);
                  c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                  removeItem(chr, mapitem, ob);
                  return;
               }

               if (mapitem.getItemId() == 2002058) {
                  if (c.getPlayer().hasDisease(SecondaryStat.DeathMark)) {
                     c.getPlayer().cancelDisease(SecondaryStat.DeathMark);
                  }

                  c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                  removeItem(chr, mapitem, ob);
                  return;
               }

               if (mapitem.getItemId() != 4001849 && mapitem.getItemId() != 4001847) {
                  if (mapitem.getItemId() >= 4034942 && mapitem.getItemId() <= 4034958) {
                     if ((Integer)c.getPlayer().getRecipe().left == mapitem.getItemId()) {
                        c.getPlayer().setRecipe(new Pair(mapitem.getItemId(), (Integer)c.getPlayer().getRecipe().right + 1));
                     } else {
                        c.getPlayer().setRecipe(new Pair(mapitem.getItemId(), 1));
                     }

                     chr.getMap().broadcastMessage(CField.addItemMuto(chr));
                     c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                     removeItem(chr, mapitem, ob);
                     return;
                  }

                  int bossid;
                  int mesos;
                  boolean touchitem;
                  int party;
                  touchitem = false;
                  int[] itemlist;
                  Equip Item3;
                  boolean state;
                  label1528:
                  switch(mapitem.getItemId()) {
                  case 2432391:
                     mesos = Randomizer.rand(1000000, 3000000);
                     chr.gainExp((long)mesos, true, true, true);
                     touchitem = true;
                     break;
                  case 2432392:
                     mesos = Randomizer.rand(1000000, 3000000);
                     chr.gainExp((long)(mesos * 2), true, true, true);
                     touchitem = true;
                     break;
                  case 2432393:
                     mesos = Randomizer.rand(50000, 70000);
                     chr.gainMeso((long)mesos, true);
                     touchitem = true;
                     break;
                  case 2432394:
                     mesos = Randomizer.rand(50000, 100000);
                     chr.gainMeso((long)mesos, true);
                     touchitem = true;
                     break;
                  case 2432395:
                     itemlist = new int[]{2000005, 2001556, 2001554, 2001530};
                     party = (int)Math.floor(Math.random() * (double)itemlist.length);
                     mesos = itemlist[party];
                     chr.gainItem(mesos, Randomizer.rand(1, 10));
                     touchitem = true;
                     break;
                  case 2432396:
                     itemlist = new int[]{1082608, 1082609, 1082610, 1082611, 1082612, 1072967, 1072968, 1072969, 1072970, 1072971, 1052799, 1052800, 1052801, 1052802, 1052803, 1004229, 1004230, 1004231, 1004232, 1004233, 1102718, 1102719, 1102720, 1102721, 1102722};
                     party = (int)Math.floor(Math.random() * (double)itemlist.length);
                     mesos = itemlist[party];
                     Item3 = (Equip)MapleItemInformationProvider.getInstance().getEquipById(mesos);
                     if (Item3 == null) {
                        removeItem(chr, mapitem, ob);
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        return;
                     }

                     state = true;
                     mobid = Randomizer.isSuccess(50) ? 2 : (Randomizer.isSuccess(30) ? 3 : 1);
                     Item3.setState((byte)mobid);
                     MapleInventoryManipulator.addbyItem(c, Item3);
                     touchitem = true;
                     break;
                  case 2432397:
                     itemlist = new int[]{1212101, 1222095, 1232095, 1242102, 1242133, 1262011, 1272013, 1282013, 1292014, 1302315, 1312185, 1322236, 1332260, 1342100, 1362121, 1372207, 1382245, 1402236, 1412164, 1422171, 1432200, 1442254, 1452238, 1462225, 1472247, 1482202, 1492212, 1522124, 1532130, 1582011, 1592016};
                     party = (int)Math.floor(Math.random() * (double)itemlist.length);
                     mesos = itemlist[party];
                     Item3 = (Equip)MapleItemInformationProvider.getInstance().getEquipById(mesos);
                     if (Item3 == null) {
                        removeItem(chr, mapitem, ob);
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        return;
                     }

                     state = true;
                     mobid = Randomizer.isSuccess(50) ? 2 : (Randomizer.isSuccess(30) ? 3 : 1);
                     Item3.setState((byte)mobid);
                     MapleInventoryManipulator.addbyItem(c, Item3);
                     touchitem = true;
                     break;
                  case 2432398:
                     ArrayList<Pair<Integer, Integer>> random = new ArrayList();
                     int[][] NormalItem = new int[][]{{5062009, 5}, {5062009, 10}, {5062500, 10}, {2435719, 1}, {2435719, 2}, {2435719, 3}, {4001832, 100}, {4001832, 150}, {4001832, 200}, {4310012, 15}, {4310012, 20}, {4310012, 25}, {4310012, 30}, {4310012, 35}};
                     int[][] HighuerItem = new int[][]{{2048716, 1}, {2048716, 2}, {2048716, 3}, {2048717, 1}, {2048717, 2}, {2049752, 1}, {4310005, 1}, {4310005, 2}, {5069000, 1}};
                     int[][] UniqueItem = new int[][]{{5062503, 10}, {5062503, 15}, {2049153, 1}, {2049153, 2}, {2049153, 3}, {5068300, 1}, {5069001, 1}, {2048753, 1}, {2049370, 1}, {5062503, 5}};
                     random.add(new Pair(1, 6400));
                     random.add(new Pair(2, 3500));
                     random.add(new Pair(3, 100));
                     int itemid = 0;
                     int count = 0;
                     bossid = GameConstants.getWeightedRandom(random);
                     if (bossid == 1) {
                        mesos = Randomizer.rand(0, NormalItem.length - 1);
                        itemid = NormalItem[mesos][0];
                        count = NormalItem[mesos][1];
                     } else if (bossid == 2) {
                        mesos = Randomizer.rand(0, HighuerItem.length - 1);
                        itemid = HighuerItem[mesos][0];
                        count = HighuerItem[mesos][1];
                     } else if (bossid == 3) {
                        mesos = Randomizer.rand(0, UniqueItem.length - 1);
                        itemid = UniqueItem[mesos][0];
                        count = UniqueItem[mesos][1];
                     }

                     chr.gainItem(itemid, count);
                     String var10002 = MapleItemInformationProvider.getInstance().getName(itemid);
                     chr.dropMessage(5, "[" + var10002 + "]를 " + count + "개 획득 했습니다.");
                     touchitem = true;
                     break;
                  case 2633304:
                  case 2633609:
                     String[] bossname = new String[]{"이지 시그너스", "하드 힐라", "카오스 핑크빈", "노멀 시그너스", "카오스 자쿰", "카오스 피에르", "카오스 반반", "카오스 블러디퀸", "하드 매그너스", "카오스 벨룸", "카오스 파풀라투스", "노멀 스우", "노멀 데미안", "이지 루시드", "노멀 루시드", "노멀 윌", "노멀 더스크", "노멀 듄켈", "하드 데미안", "하드 스우", "하드 루시드", "하드 윌", "카오스 더스크", "하드 듄켈", "진 힐라", "세렌"};
                     wishCoinCheck = null;
                     String NewKeyvalue = "";
                     i = 0;
                     Iterator var30 = ServerConstants.NeoPosList.iterator();

                     while(true) {
                        if (var30.hasNext()) {
                           label1704: {
                              Pair<Integer, Integer> list = (Pair)var30.next();
                              MapleMonster mob = (MapleMonster)mapitem.getDropper();
                              if ((Integer)list.getLeft() != mob.getId()) {
                                 ++i;
                                 continue;
                              }

                              int coincount = (Integer)list.getRight();
                              if (ServerConstants.Event_MapleLive) {
                                 bossid = 501471 + i;
                                 if (chr.getClient().getCustomKeyValue(bossid, "state") == 1L || chr.getClient().getCustomKeyValue(bossid, "state") == 2L) {
                                    chr.getClient().send(CField.enforceMsgNPC(9062558, 3000, "이런, 이미 이번주에 #r" + bossname[i] + "#k를 처치했군."));
                                    break label1704;
                                 }

                                 if (Randomizer.isSuccess(50)) {
                                    chr.getClient().send(CField.enforceMsgNPC(9062558, 3000, "호오, #r" + bossname[i] + "#k를 처치했군.\r\n검은콩 #b" + coincount + "#k개를 찾았다."));
                                 } else {
                                    chr.getClient().send(CField.enforceMsgNPC(9062558, 3000, "좋아, #r" + bossname[i] + "#k를 처치했군.\r\n검은콩 #b" + coincount + "#k개를 찾았어."));
                                 }
                              }

                              ((Object[])wishCoinCheck)[i] = "1";

                              for(int to = 0; to < ((Object[])wishCoinCheck).length; ++to) {
                                 NewKeyvalue = NewKeyvalue + ((Object[])wishCoinCheck)[to];
                              }

                              if (ServerConstants.Event_MapleLive) {
                                 bossid = 501471 + i;
                                 chr.getClient().setCustomKeyValue(501468, "reward", NewKeyvalue);
                                 chr.getClient().setCustomKeyValue(bossid, "clear", "1");
                                 chr.getClient().setCustomKeyValue(bossid, "state", "1");
                              }
                           }
                        }

                        touchitem = true;
                        break label1528;
                     }
                  case 2633343:
                     if (chr.getBuffedValue(80003046)) {
                        mesos = 0;
                        party = chr.getQuestStatus(100801) == 2 ? 1 : (chr.getQuestStatus(100802) == 2 ? 2 : 0);

                        for(mesos = 0; mesos < party; ++mesos) {
                           if (Randomizer.isSuccess(50)) {
                              ++mesos;
                           }
                        }

                        mesos = (int)chr.getKeyValue(100803, "count");
                        if (mesos < 0) {
                           mesos = 0;
                        }

                        chr.setKeyValue(100803, "count", (mesos + 1).makeConcatWithConstants<invokedynamic>(mesos + 1));
                        chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 1 : 2, mapitem.getPosition());
                        chr.getClient().send(SLFCGPacket.EventSkillOnFlowerEffect(7, 1));
                        if (chr.getKeyValue(100803, "count") == 5L) {
                           chr.getClient().send(CField.enforceMsgNPC(9062527, 1300, "꽃의 보석에 햇살의 힘이 #r절반#k이나 모였어요!"));
                        } else if (chr.getKeyValue(100803, "count") == 10L) {
                           chr.setKeyValue(100803, "count", "0");
                           chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40, chr.getPosition());
                           chr.getClient().send(SLFCGPacket.EventSkillOn(1, 1 + mesos));
                           chr.getClient().send(CField.enforceMsgNPC(9062527, 1000, "햇살의 힘으로 #r꽃씨#k를 펑!펑!"));
                           MapleClient var10000 = chr.getClient();
                           var10001 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40;
                           var10000.send(CField.UIPacket.detailShowInfo("하나의 꽃씨 뿌리기로 블루밍 코인 " + var10001 + "개를 획득했습니다.", 3, 20, 20));
                           if (mesos >= 1 && chr.getQuestStatus(100801) == 2) {
                              Timer.BuffTimer.getInstance().schedule(() -> {
                                 chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 10 : 20, chr.getPosition());
                                 chr.getClient().send(CField.enforceMsgNPC(9062528, 1000, "#r꽃비#k가 쏴아아!\r\n너무 예뻐!"));
                                 MapleClient var10000 = chr.getClient();
                                 int var10001 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 10 : 20;
                                 var10000.send(CField.UIPacket.detailShowInfo("두나의 꽃비로 블루밍 코인 " + var10001 + "개를 획득했습니다.", 3, 20, 20));
                              }, 2500L);
                           }

                           if (mesos == 2 && chr.getQuestStatus(100802) == 2) {
                              Timer.BuffTimer.getInstance().schedule(() -> {
                                 chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40, chr.getPosition());
                                 chr.getClient().send(CField.enforceMsgNPC(9062529, 1000, "#r햇살#k이 샤라랑!\r\n꽃들아 잘 자라라~!"));
                                 MapleClient var10000 = chr.getClient();
                                 int var10001 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40;
                                 var10000.send(CField.UIPacket.detailShowInfo("세나의 햇살 비추기로 블루밍 코인 " + var10001 + "개를 획득했습니다.", 3, 20, 20));
                              }, 4500L);
                           }
                        }

                        if (chr.getKeyValue(100711, "today") >= (Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000L : 6000L)) {
                           chr.getClient().send(CField.enforceMsgNPC(9062527, 1000, "오늘은 이만하면 됐어요."));
                           chr.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(1297));
                           chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062524, 0));
                           chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062525, 0));
                           chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062526, 0));
                           chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.EventSpecialSkill));
                        }
                     }

                     touchitem = true;
                  }

                  if (mapitem.getItemId() >= 2437659 && mapitem.getItemId() <= 2437664) {
                     int plushour = 0;
                     int plusmit = 0;
                     switch(mapitem.getItemId()) {
                     case 2437659:
                        plusmit = 10;
                        break;
                     case 2437660:
                        plusmit = 30;
                        break;
                     case 2437661:
                        plusmit = 50;
                        break;
                     case 2437662:
                        plushour = 2;
                        break;
                     case 2437663:
                        plushour = 4;
                        break;
                     case 2437664:
                        plushour = 9;
                     }

                     var54 = c.getPlayer().getMap();
                     var54.Papullatushour += plushour;
                     if (c.getPlayer().getMap().Papullatushour > 12) {
                        var54 = c.getPlayer().getMap();
                        var54.Papullatushour -= 12;
                     }

                     var54 = c.getPlayer().getMap();
                     var54.Papullatusminute += plusmit;
                     if (c.getPlayer().getMap().Papullatusminute >= 60) {
                        ++c.getPlayer().getMap().Papullatushour;
                        var54 = c.getPlayer().getMap();
                        var54.Papullatusminute -= 60;
                     }

                     c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("파풀라투스의 시계가 움직입니다. 차원의 포탈을 통해 시간을 봉인하세요.", 5120177, true));
                     c.getPlayer().getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTimePatten(1, plushour, plusmit, 0));
                     removeItem(chr, mapitem, ob);
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     return;
                  }

                  if (mapitem.getItemId() != 2437606 && mapitem.getItemId() != 2437607) {
                     if (touchitem) {
                        removeItem(chr, mapitem, ob);
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        return;
                     }

                     if (mapitem.getQuest() > 0 && chr.getQuestStatus(mapitem.getQuest()) != 1) {
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        return;
                     }

                     if (mapitem.getOwner() != chr.getId() && (!mapitem.isPlayerDrop() && mapitem.getDropType() == 0 || mapitem.isPlayerDrop() && chr.getMap().getEverlast())) {
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        return;
                     }

                     if (mapitem.isPlayerDrop() || mapitem.getDropType() != 1 || mapitem.getOwner() == chr.getId() || chr.getParty() != null && chr.getParty().getMemberById(mapitem.getOwner()) != null) {
                        if (mapitem.getMeso() > 0) {
                           if (chr.getParty() != null && mapitem.getOwner() != chr.getId()) {
                              LinkedList<MapleCharacter> toGive = new LinkedList();
                              party = mapitem.getMeso() * 40 / 100;
                              Iterator var42 = chr.getParty().getMembers().iterator();

                              while(var42.hasNext()) {
                                 MaplePartyCharacter z = (MaplePartyCharacter)var42.next();
                                 MapleCharacter m = chr.getMap().getCharacterById(z.getId());
                                 if (m != null && m.getId() != chr.getId()) {
                                    toGive.add(m);
                                 }
                              }

                              MapleCharacter m;
                              for(var42 = toGive.iterator(); var42.hasNext(); m.gainMeso((long)mobid, true, false, false, true)) {
                                 m = (MapleCharacter)var42.next();
                                 mobid = party / toGive.size() + (m.getStat().hasPartyBonus ? (int)((double)mapitem.getMeso() / 20.0D) : 0);
                                 if (mapitem.getDropper() instanceof MapleMonster && m.getStat().incMesoProp > 0) {
                                    mobid = (int)((double)mobid + Math.floor((double)((float)(m.getStat().incMesoProp * mobid) / 100.0F)));
                                 }
                              }

                              mesos = mapitem.getMeso() - party;
                              if (mapitem.getDropper() instanceof MapleMonster && chr.getStat().incMesoProp > 0) {
                                 mesos = (int)((double)mesos + Math.floor((double)((float)(chr.getStat().incMesoProp * mesos) / 100.0F)));
                              }

                              chr.gainMeso((long)mesos, true, false, false, true);
                           } else {
                              mesos = mapitem.getMeso();
                              if (mapitem.getDropper() instanceof MapleMonster && chr.getStat().incMesoProp > 0) {
                                 mesos = (int)((double)mesos + Math.floor((double)((float)(chr.getStat().incMesoProp * mesos) / 100.0F)));
                              }

                              chr.gainMeso((long)mesos, true, false, false, true);
                           }

                           if (mapitem.getDropper().getType() == MapleMapObjectType.MONSTER) {
                              c.getSession().writeAndFlush(CWvsContext.onMesoPickupResult(mapitem.getMeso()));
                           }

                           removeItem(chr, mapitem, ob);
                           return;
                        } else if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId())) {
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           c.getPlayer().dropMessage(5, "This item cannot be picked up.");
                           return;
                        } else if (c.getPlayer().inPVP() && Integer.parseInt(c.getPlayer().getEventInstance().getProperty("ice")) == c.getPlayer().getId()) {
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryFull());
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getShowInventoryFull());
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           return;
                        } else if (mapitem.getItemId() == 2431835) {
                           MapleItemInformationProvider.getInstance().getItemEffect(2002093).applyTo(chr, true);
                           removeItem(chr, mapitem, ob);
                           return;
                        } else {
                           if (mapitem.getItemId() / 10000 != 291 && MapleInventoryManipulator.checkSpace(c, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                              if (mapitem.getItem().getQuantity() >= 50 && mapitem.getItemId() == 2340000) {
                                 c.setMonitored(true);
                              }

                              if (mapitem.getEquip() != null && mapitem.getDropper().getType() == MapleMapObjectType.MONSTER && mapitem.getEquip().getState() > 0) {
                                 c.getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 0, 65, 0, 0, (byte)0, true, (Point)null, (String)null, mapitem.getItem()));
                              }

                              if (GameConstants.isArcaneSymbol(mapitem.getItemId())) {
                                 Equip equip2 = (Equip)mapitem.getItem();
                                 equip2.setArc((short)30);
                                 equip2.setArcLevel(1);
                                 equip2.setArcEXP(1);
                                 if (GameConstants.isXenon(c.getPlayer().getJob())) {
                                    equip2.setStr((short)117);
                                    equip2.setDex((short)117);
                                    equip2.setLuk((short)117);
                                 } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
                                    equip2.setHp((short)525);
                                 } else if (GameConstants.isWarrior(c.getPlayer().getJob())) {
                                    equip2.setStr((short)300);
                                 } else if (GameConstants.isMagician(c.getPlayer().getJob())) {
                                    equip2.setInt((short)300);
                                 } else if (!GameConstants.isArcher(c.getPlayer().getJob()) && !GameConstants.isCaptain(c.getPlayer().getJob()) && !GameConstants.isMechanic(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                                    if (GameConstants.isThief(c.getPlayer().getJob())) {
                                       equip2.setLuk((short)300);
                                    } else if (GameConstants.isPirate(c.getPlayer().getJob())) {
                                       equip2.setStr((short)300);
                                    }
                                 } else {
                                    equip2.setDex((short)300);
                                 }
                              } else {
                                 Equip equip;
                                 if (GameConstants.isAuthenticSymbol(mapitem.getItemId()) && (equip = (Equip)mapitem.getItem()).getArcLevel() == 0) {
                                    equip.setArc((short)10);
                                    equip.setArcLevel(1);
                                    equip.setArcEXP(1);
                                    if (GameConstants.isXenon(c.getPlayer().getJob())) {
                                       equip.setStr((short)317);
                                       equip.setDex((short)317);
                                       equip.setLuk((short)317);
                                    } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
                                       equip.setHp((short)725);
                                    } else if (GameConstants.isWarrior(c.getPlayer().getJob())) {
                                       equip.setStr((short)500);
                                    } else if (GameConstants.isMagician(c.getPlayer().getJob())) {
                                       equip.setInt((short)500);
                                    } else if (!GameConstants.isArcher(c.getPlayer().getJob()) && !GameConstants.isCaptain(c.getPlayer().getJob()) && !GameConstants.isMechanic(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                                       if (GameConstants.isThief(c.getPlayer().getJob())) {
                                          equip.setLuk((short)500);
                                       } else if (GameConstants.isPirate(c.getPlayer().getJob())) {
                                          equip.setStr((short)500);
                                       }
                                    } else {
                                       equip.setDex((short)500);
                                    }
                                 }
                              }

                              if (mapitem.getItem().getItemId() == 4001886) {
                                 if (mapitem.getItem().getBossid() == 0) {
                                    mapitem.getItem().setReward(new BossReward(chr.getInventory(MapleInventoryType.ETC).countById(4001886) + 1, 100100, 1, 1));
                                 } else {
                                    party = chr.getParty() != null ? chr.getParty().getMembers().size() : 1;
                                    long meso = 0L;
                                    mobid = 0;
                                    Iterator var47 = BossRewardMeso.getLists().iterator();

                                    while(var47.hasNext()) {
                                       Triple<Integer, Integer, Integer> list = (Triple)var47.next();
                                       bossid = BossRewardMeso.RewardBossId((Integer)list.getMid());
                                       if (bossid == 0) {
                                          bossid = (Integer)list.getLeft();
                                       }

                                       if (bossid == mapitem.getItem().getBossid() || (Integer)list.getMid() == mapitem.getItem().getBossid()) {
                                          meso = (long)(Integer)list.getRight();
                                          mobid = (Integer)list.getMid();
                                          break;
                                       }
                                    }

                                    mapitem.getItem().setReward(new BossReward(chr.getInventory(MapleInventoryType.ETC).countById(4001886) + 1, mobid, party, (int)(meso * 10L)));
                                    mapitem.getItem().setExpiration(System.currentTimeMillis() + 604800000L);
                                 }
                              }

                              String var53 = FileoutputUtil.아이템획득로그;
                              var10001 = chr.getClient().getAccID();
                              FileoutputUtil.log(var53, "[PickUP_Player] 계정번호 : " + var10001 + " | " + chr.getName() + "이(가)  맵 :" + c.getPlayer().getMapId() + "에서 " + MapleItemInformationProvider.getInstance().getName(mapitem.getItem().getItemId()) + "(" + mapitem.getItem().getItemId() + ")를 " + mapitem.getItem().getQuantity() + " 개 획득");
                              MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, mapitem.getDropper() instanceof MapleMonster, false);
                              removeItem(chr, mapitem, ob);
                           } else {
                              c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryFull());
                              c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getShowInventoryFull());
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           }

                           return;
                        }
                     }

                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     return;
                  }

                  MobSkill ms1 = MobSkillFactory.getMobSkill(241, 3);
                  int duration = false;
                  if (chr.getSkillCustomTime(241) != null) {
                     party = mapitem.getItemId() == 2437606 ? chr.getSkillCustomTime(241) * 2 : chr.getSkillCustomTime(241) / 2;
                     chr.cancelDisease(SecondaryStat.PapulCuss);
                     if (party > 60000) {
                        party = 60000;
                     }

                     if (party > 0) {
                        ms1.setDuration((long)party);
                        chr.setSkillCustomInfo(241, 0L, (long)party);
                        chr.giveDebuff(SecondaryStat.PapulCuss, ms1);
                     }
                  }

                  removeItem(chr, mapitem, ob);
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               if (chr.getBattleGroundChr() != null) {
                  chr.setSkillCustomInfo(80001741, chr.getSkillCustomValue0(80001741) + 1L, 0L);
                  chr.getBattleGroundChr().setTeam(2);
                  chr.getMap().broadcastMessage(chr, BattleGroundPacket.UpdateAvater(chr.getBattleGroundChr(), GameConstants.BattleGroundJobType(chr.getBattleGroundChr())), false);
                  chr.getBattleGroundChr().setTeam(1);
                  chr.getClient().send(BattleGroundPacket.UpdateAvater(chr.getBattleGroundChr(), GameConstants.BattleGroundJobType(chr.getBattleGroundChr())));
               }

               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               removeItem(chr, mapitem, ob);
               return;
            }

            if (mapitem.getDropper() instanceof MapleMonster) {
               if (chr.getClient().getKeyValue("combokill") == null) {
                  chr.getClient().setKeyValue("combokill", "1");
                  chr.getClient().send(CField.ImageTalkNpc(9010049, 10000, "#b[안내] 콤보킬 퍼레이드#k\r\n\r\n콤보를 50단위씩 쌓아서 얻는 #b[콤보킬 퍼레이드]#k를 획득 하셨어요!\r\n\r\n#b[콤보킬 퍼레이드]#k에 접촉 시 #b추가 보너스 경험치#k를 획득할 수 있어요!"));
               }

               bonus = mapitem.getItemId() % 100 == 84 ? 5 : (mapitem.getItemId() % 100 == 94 ? 7 : (mapitem.getItemId() % 100 == 95 ? 10 : 12));
               if (chr.getSkillLevel(20000297) > 0) {
                  bonus += SkillFactory.getSkill(20000297).getEffect(chr.getSkillLevel(20000297)).getX() / 100;
               } else if (chr.getSkillLevel(80000370) > 0) {
                  bonus += SkillFactory.getSkill(80000370).getEffect(chr.getSkillLevel(80000370)).getX() / 100;
               }

               if (mapitem.getItemId() == 2023927) {
                  bonus = 30;
               }

               MapleMonster mob = (MapleMonster)mapitem.getDropper();
               long exp = mob.getMobExp() * (long)bonus;
               chr.gainExp(exp, true, true, false);
               c.send(CField.EffectPacket.gainExp(exp));
            }

            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
            removeItem(chr, mapitem, ob);
            return;
         }

         removeItem(chr, mapitem, ob);
      } finally {
         mapitem.getLock().unlock();
      }

   }

   public static final void Pickup_Player(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      MapleParty party_player = c.getPlayer().getParty();
      if (party_player != null && party_player.getPartyDrop() == 1 && party_player.getLeader().getId() != chr.getId()) {
         c.getPlayer().dropMessage(5, "아이템 습득 권한이 파티장으로 설정되어 파티원은 드롭할 수 없습니다.");
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         slea.readInt();
         slea.skip(1);
         Point Client_Reportedpos = slea.readPos();
         if (chr != null && chr.getMap() != null) {
            chr.setScrolledPosition((short)0);
            MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);
            if (ob == null) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               MapleMapItem mapitem = (MapleMapItem)ob;

               try {
                  mapitem.getLock().lock();
                  if (mapitem.isPickedUp()) {
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     return;
                  }

                  int bonus;
                  if (mapitem.getItemId() == 2431174) {
                     bonus = Randomizer.rand(1, 10) * 10;
                     c.getPlayer().gainHonor(bonus);
                     c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                     removeItem(chr, mapitem, ob);
                     return;
                  }

                  if (mapitem.getItemId() == 2433103) {
                     c.getPlayer().gainHonor(10000);
                     c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                     removeItem(chr, mapitem, ob);
                     return;
                  }

                  if (mapitem.getItemId() == 2433979) {
                     bonus = Randomizer.rand(1000000, 100000000);
                     chr.gainMeso((long)bonus, true);
                     removeItem(chr, mapitem, ob);
                     c.getPlayer().dropMessage(-8, "메소럭키백에서 메소를 " + bonus + "메소 만큼 휙득 하였습니다.");
                     return;
                  }

                  if (mapitem.getItemId() == 4001169 && c.getPlayer().getMap().getMonstermarble() != 20) {
                     c.getPlayer().getMap().setMonstermarble(c.getPlayer().getMap().getMonstermarble() + 1);
                     c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                     c.getPlayer().getMap().broadcastMessage(CWvsContext.getTopMsg("몬스터구슬 " + c.getPlayer().getMap().getMonstermarble() + " / 20"));
                     removeItem(chr, mapitem, ob);
                     return;
                  }

                  if (mapitem.getItemId() == 4001169) {
                     removeItem(chr, mapitem, ob);
                     return;
                  }

                  int var10001;
                  MapleMap var56;
                  if (mapitem.getItemId() == 4001101 && c.getPlayer().getMap().getMoonCake() != 80) {
                     c.getPlayer().getMap().setMoonCake(c.getPlayer().getMap().getMoonCake() + 1);
                     c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                     var56 = c.getPlayer().getMap();
                     var10001 = c.getPlayer().getMap().getMoonCake();
                     var56.broadcastMessage(CWvsContext.getTopMsg("어흥이를 위해 월묘의 떡 " + var10001 + "개를 모았습니다.  앞으로 " + (80 - c.getPlayer().getMap().getMoonCake()) + "개 더!"));
                     removeItem(chr, mapitem, ob);
                     return;
                  }

                  if (mapitem.getItemId() == 4001101) {
                     removeItem(chr, mapitem, ob);
                     return;
                  }

                  if (mapitem.getItemId() == 4000884) {
                     c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("달맞이꽃 씨앗을 되찾았습니다.", 5120016, true));
                  }

                  int meso;
                  int mobid;
                  int mesos;
                  if (mapitem.getItemId() >= 2432139 && mapitem.getItemId() < 2432149) {
                     EventManager em = c.getChannelServer().getEventSM().getEventManager("KerningPQ");
                     String stage4 = em.getProperty("stage4r");
                     String stage4M = c.getPlayer().getEventInstance().getProperty("stage4M");
                     meso = Integer.parseInt(stage4M);

                     for(mobid = 1; mobid < 10; ++mobid) {
                        if (mapitem.getItemId() == 2432139 + mobid) {
                           if (meso != c.getPlayer().getMap().getKerningPQ()) {
                              if (stage4 == "0") {
                                 c.getPlayer().getMap().setKerningPQ(mobid);
                              } else {
                                 byte var14 = -1;
                                 switch(stage4.hashCode()) {
                                 case 49:
                                    if (stage4.equals("1")) {
                                       var14 = 0;
                                    }
                                    break;
                                 case 50:
                                    if (stage4.equals("2")) {
                                       var14 = 1;
                                    }
                                    break;
                                 case 51:
                                    if (stage4.equals("3")) {
                                       var14 = 2;
                                    }
                                    break;
                                 case 52:
                                    if (stage4.equals("4")) {
                                       var14 = 3;
                                    }
                                 }

                                 switch(var14) {
                                 case 0:
                                    c.getPlayer().dropMessage(5, mobid + "/" + stage4);
                                    c.getPlayer().getMap().setKerningPQ(c.getPlayer().getMap().getKerningPQ() + mobid);
                                    c.getPlayer().dropMessage(5, c.getPlayer().getMap().getKerningPQ().makeConcatWithConstants<invokedynamic>(c.getPlayer().getMap().getKerningPQ()));
                                    break;
                                 case 1:
                                    if (stage4 != "0") {
                                       c.getPlayer().getMap().setKerningPQ(c.getPlayer().getMap().getKerningPQ() - mobid);
                                    }
                                    break;
                                 case 2:
                                    if (stage4 != "0") {
                                       c.getPlayer().getMap().setKerningPQ(c.getPlayer().getMap().getKerningPQ() * mobid);
                                    }
                                    break;
                                 case 3:
                                    if (stage4 != "0") {
                                       mesos = (int)Math.floor((double)(c.getPlayer().getMap().getKerningPQ() / mobid));
                                       c.getPlayer().getMap().setKerningPQ(mesos);
                                    }
                                 }
                              }

                              c.getPlayer().getMap().broadcastMessage(CWvsContext.getTopMsg("현재 숫자 : " + c.getPlayer().getMap().getKerningPQ()));
                              c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("목표 숫자 : " + stage4M, 5120017, true));
                              if (meso == c.getPlayer().getMap().getKerningPQ()) {
                                 c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(75));
                                 c.getPlayer().getMap().broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
                                 c.getPlayer().getMap().broadcastMessage(CField.environmentChange("gate", 2));
                              }
                           }

                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                           removeItem(chr, mapitem, ob);
                           return;
                        }
                     }
                  }

                  if (mapitem.getItemId() != 4001022) {
                     if (mapitem.getItemId() == 4001022) {
                        removeItem(chr, mapitem, ob);
                        return;
                     }

                     if (mapitem.getItemId() != 2023484 && mapitem.getItemId() != 2023494 && mapitem.getItemId() != 2023495 && mapitem.getItemId() != 2023669 && mapitem.getItemId() != 2023927) {
                        if (mapitem.getItemId() == 2434851) {
                           if (!chr.getBuffedValue(25121133)) {
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           SecondaryStatEffect effect = SkillFactory.getSkill(25121133).getEffect(1);
                           long duration = chr.getBuffLimit(25121133);
                           if ((duration += 4000L) >= (long)effect.getCoolRealTime()) {
                              duration = (long)effect.getCoolRealTime();
                           }

                           effect.applyTo(chr, chr, false, chr.getPosition(), (int)duration, (byte)0, false);
                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                           removeItem(chr, mapitem, ob);
                           return;
                        }

                        if (mapitem.getItemId() == 2002058) {
                           if (c.getPlayer().hasDisease(SecondaryStat.DeathMark)) {
                              c.getPlayer().cancelDisease(SecondaryStat.DeathMark);
                           }

                           c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                           removeItem(chr, mapitem, ob);
                           return;
                        }

                        if (mapitem.getItemId() != 4001849 && mapitem.getItemId() != 4001847) {
                           if (mapitem.getItemId() >= 4034942 && mapitem.getItemId() <= 4034958) {
                              if ((Integer)c.getPlayer().getRecipe().left == mapitem.getItemId()) {
                                 c.getPlayer().setRecipe(new Pair(mapitem.getItemId(), (Integer)c.getPlayer().getRecipe().right + 1));
                              } else {
                                 c.getPlayer().setRecipe(new Pair(mapitem.getItemId(), 1));
                              }

                              chr.getMap().broadcastMessage(CField.addItemMuto(chr));
                              c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                              removeItem(chr, mapitem, ob);
                              return;
                           }

                           boolean touchitem = false;
                           int questid;
                           int mesos;
                           int party;
                           int[] itemlist;
                           Iterator var33;
                           Equip Item3;
                           boolean state;
                           int bossid;
                           switch(mapitem.getItemId()) {
                           case 2432391:
                              mesos = Randomizer.rand(1000000, 3000000);
                              chr.gainExp((long)mesos, true, true, true);
                              touchitem = true;
                              break;
                           case 2432392:
                              mesos = Randomizer.rand(1000000, 3000000);
                              chr.gainExp((long)(mesos * 2), true, true, true);
                              touchitem = true;
                              break;
                           case 2432393:
                              mesos = Randomizer.rand(50000, 70000);
                              chr.gainMeso((long)mesos, true);
                              touchitem = true;
                              break;
                           case 2432394:
                              mesos = Randomizer.rand(50000, 100000);
                              chr.gainMeso((long)mesos, true);
                              touchitem = true;
                              break;
                           case 2432395:
                              itemlist = new int[]{2000005, 2001556, 2001554, 2001530};
                              party = (int)Math.floor(Math.random() * (double)itemlist.length);
                              meso = itemlist[party];
                              chr.gainItem(meso, Randomizer.rand(1, 10));
                              touchitem = true;
                              break;
                           case 2432396:
                              itemlist = new int[]{1082608, 1082609, 1082610, 1082611, 1082612, 1072967, 1072968, 1072969, 1072970, 1072971, 1052799, 1052800, 1052801, 1052802, 1052803, 1004229, 1004230, 1004231, 1004232, 1004233, 1102718, 1102719, 1102720, 1102721, 1102722};
                              party = (int)Math.floor(Math.random() * (double)itemlist.length);
                              meso = itemlist[party];
                              Item3 = (Equip)MapleItemInformationProvider.getInstance().getEquipById(meso);
                              if (Item3 == null) {
                                 removeItem(chr, mapitem, ob);
                                 c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                                 return;
                              }

                              state = true;
                              mesos = Randomizer.isSuccess(50) ? 2 : (Randomizer.isSuccess(30) ? 3 : 1);
                              Item3.setState((byte)mesos);
                              MapleInventoryManipulator.addbyItem(c, Item3);
                              touchitem = true;
                              break;
                           case 2432397:
                              itemlist = new int[]{1212101, 1222095, 1232095, 1242102, 1242133, 1262011, 1272013, 1282013, 1292014, 1302315, 1312185, 1322236, 1332260, 1342100, 1362121, 1372207, 1382245, 1402236, 1412164, 1422171, 1432200, 1442254, 1452238, 1462225, 1472247, 1482202, 1492212, 1522124, 1532130, 1582011, 1592016};
                              party = (int)Math.floor(Math.random() * (double)itemlist.length);
                              meso = itemlist[party];
                              Item3 = (Equip)MapleItemInformationProvider.getInstance().getEquipById(meso);
                              if (Item3 == null) {
                                 removeItem(chr, mapitem, ob);
                                 c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                                 return;
                              }

                              state = true;
                              mesos = Randomizer.isSuccess(50) ? 2 : (Randomizer.isSuccess(30) ? 3 : 1);
                              Item3.setState((byte)mesos);
                              MapleInventoryManipulator.addbyItem(c, Item3);
                              touchitem = true;
                              break;
                           case 2432398:
                              ArrayList<Pair<Integer, Integer>> random = new ArrayList();
                              int[][] NormalItem = new int[][]{{5062009, 5}, {5062009, 10}, {5062500, 10}, {2435719, 1}, {2435719, 2}, {2435719, 3}, {4001832, 100}, {4001832, 150}, {4001832, 200}, {4310012, 15}, {4310012, 20}, {4310012, 25}, {4310012, 30}, {4310012, 35}};
                              int[][] HighuerItem = new int[][]{{2048716, 1}, {2048716, 2}, {2048716, 3}, {2048717, 1}, {2048717, 2}, {2049752, 1}, {4310005, 1}, {4310005, 2}, {5069000, 1}};
                              int[][] UniqueItem = new int[][]{{5062503, 10}, {5062503, 15}, {2049153, 1}, {2049153, 2}, {2049153, 3}, {5068300, 1}, {5069001, 1}, {2048753, 1}, {2049370, 1}, {5062503, 5}};
                              random.add(new Pair(1, 6400));
                              random.add(new Pair(2, 3500));
                              random.add(new Pair(3, 100));
                              int itemid = 0;
                              bossid = 0;
                              questid = GameConstants.getWeightedRandom(random);
                              if (questid == 1) {
                                 mesos = Randomizer.rand(0, NormalItem.length - 1);
                                 itemid = NormalItem[mesos][0];
                                 bossid = NormalItem[mesos][1];
                              } else if (questid == 2) {
                                 mesos = Randomizer.rand(0, HighuerItem.length - 1);
                                 itemid = HighuerItem[mesos][0];
                                 bossid = HighuerItem[mesos][1];
                              } else if (questid == 3) {
                                 mesos = Randomizer.rand(0, UniqueItem.length - 1);
                                 itemid = UniqueItem[mesos][0];
                                 bossid = UniqueItem[mesos][1];
                              }

                              chr.gainItem(itemid, bossid);
                              String var10002 = MapleItemInformationProvider.getInstance().getName(itemid);
                              chr.dropMessage(5, "[" + var10002 + "]를 " + bossid + "개 획득 했습니다.");
                              touchitem = true;
                              break;
                           case 2633304:
                           case 2633609:
                              String[] bossname = new String[]{"이지 시그너스", "하드 힐라", "카오스 핑크빈", "노멀 시그너스", "카오스 자쿰", "카오스 피에르", "카오스 반반", "카오스 블러디퀸", "하드 매그너스", "카오스 벨룸", "카오스 파풀라투스", "노멀 스우", "노멀 데미안", "이지 루시드", "노멀 루시드", "노멀 윌", "노멀 더스크", "노멀 듄켈", "하드 데미안", "하드 스우", "하드 루시드", "하드 윌", "카오스 더스크", "하드 듄켈", "진 힐라", "세렌"};
                              String[] wishCoinCheck = null;
                              if (ServerConstants.Event_MapleLive) {
                                 wishCoinCheck = chr.getClient().getCustomKeyValueStr(501468, "reward").split("");
                              }

                              String NewKeyvalue = "";
                              mobid = 0;

                              for(var33 = ServerConstants.NeoPosList.iterator(); var33.hasNext(); ++mobid) {
                                 Pair<Integer, Integer> list = (Pair)var33.next();
                                 MapleMonster mob = (MapleMonster)mapitem.getDropper();
                                 if ((Integer)list.getLeft() == mob.getId()) {
                                    int coincount = (Integer)list.getRight();
                                    if (ServerConstants.Event_MapleLive) {
                                       questid = 501471 + mobid;
                                       if (chr.getClient().getCustomKeyValue(questid, "state") == 1L || chr.getClient().getCustomKeyValue(questid, "state") == 2L) {
                                          chr.getClient().send(CField.enforceMsgNPC(9062558, 3000, "이런, 이미 이번주에 #r" + bossname[mobid] + "#k를 처치했군."));
                                          break;
                                       }

                                       if (Randomizer.isSuccess(50)) {
                                          chr.getClient().send(CField.enforceMsgNPC(9062558, 3000, "호오, #r" + bossname[mobid] + "#k를 처치했군.\r\n검은콩 #b" + coincount + "#k개를 찾았다."));
                                       } else {
                                          chr.getClient().send(CField.enforceMsgNPC(9062558, 3000, "좋아, #r" + bossname[mobid] + "#k를 처치했군.\r\n검은콩 #b" + coincount + "#k개를 찾았어."));
                                       }
                                    }

                                    wishCoinCheck[mobid] = "1";

                                    for(int to = 0; to < wishCoinCheck.length; ++to) {
                                       NewKeyvalue = NewKeyvalue + wishCoinCheck[to];
                                    }

                                    if (ServerConstants.Event_MapleLive) {
                                       questid = 501471 + mobid;
                                       chr.getClient().setCustomKeyValue(501468, "reward", NewKeyvalue);
                                       chr.getClient().setCustomKeyValue(questid, "clear", "1");
                                       chr.getClient().setCustomKeyValue(questid, "state", "1");
                                    }
                                    break;
                                 }
                              }

                              touchitem = true;
                              break;
                           case 2633343:
                              if (chr.getBuffedValue(80003046)) {
                                 mesos = 0;
                                 party = chr.getQuestStatus(100801) == 2 ? 1 : (chr.getQuestStatus(100802) == 2 ? 2 : 0);

                                 for(meso = 0; meso < party; ++meso) {
                                    if (Randomizer.isSuccess(50)) {
                                       ++mesos;
                                    }
                                 }

                                 meso = (int)chr.getKeyValue(100803, "count");
                                 if (meso < 0) {
                                    meso = 0;
                                 }

                                 chr.setKeyValue(100803, "count", (meso + 1).makeConcatWithConstants<invokedynamic>(meso + 1));
                                 chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 1 : 2, mapitem.getPosition());
                                 chr.getClient().send(SLFCGPacket.EventSkillOnFlowerEffect(7, 1));
                                 if (chr.getKeyValue(100803, "count") == 5L) {
                                    chr.getClient().send(CField.enforceMsgNPC(9062527, 1300, "꽃의 보석에 햇살의 힘이 #r절반#k이나 모였어요!"));
                                 } else if (chr.getKeyValue(100803, "count") == 10L) {
                                    chr.setKeyValue(100803, "count", "0");
                                    chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40, chr.getPosition());
                                    chr.getClient().send(SLFCGPacket.EventSkillOn(1, 1 + mesos));
                                    chr.getClient().send(CField.enforceMsgNPC(9062527, 1000, "햇살의 힘으로 #r꽃씨#k를 펑!펑!"));
                                    MapleClient var10000 = chr.getClient();
                                    var10001 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40;
                                    var10000.send(CField.UIPacket.detailShowInfo("하나의 꽃씨 뿌리기로 블루밍 코인 " + var10001 + "개를 획득했습니다.", 3, 20, 20));
                                    if (mesos >= 1 && chr.getQuestStatus(100801) == 2) {
                                       Timer.BuffTimer.getInstance().schedule(() -> {
                                          chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 10 : 20, chr.getPosition());
                                          chr.getClient().send(CField.enforceMsgNPC(9062528, 1000, "#r꽃비#k가 쏴아아!\r\n너무 예뻐!"));
                                          MapleClient var10000 = chr.getClient();
                                          int var10001 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 10 : 20;
                                          var10000.send(CField.UIPacket.detailShowInfo("두나의 꽃비로 블루밍 코인 " + var10001 + "개를 획득했습니다.", 3, 20, 20));
                                       }, 2500L);
                                    }

                                    if (mesos == 2 && chr.getQuestStatus(100802) == 2) {
                                       Timer.BuffTimer.getInstance().schedule(() -> {
                                          chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40, chr.getPosition());
                                          chr.getClient().send(CField.enforceMsgNPC(9062529, 1000, "#r햇살#k이 샤라랑!\r\n꽃들아 잘 자라라~!"));
                                          MapleClient var10000 = chr.getClient();
                                          int var10001 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40;
                                          var10000.send(CField.UIPacket.detailShowInfo("세나의 햇살 비추기로 블루밍 코인 " + var10001 + "개를 획득했습니다.", 3, 20, 20));
                                       }, 4500L);
                                    }
                                 }

                                 if (chr.getKeyValue(100711, "today") >= (Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000L : 6000L)) {
                                    chr.getClient().send(CField.enforceMsgNPC(9062527, 1000, "오늘은 이만하면 됐어요."));
                                    chr.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(1297));
                                    chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062524, 0));
                                    chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062525, 0));
                                    chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062526, 0));
                                    chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.EventSpecialSkill));
                                 }
                              }

                              touchitem = true;
                           }

                           if (mapitem.getItemId() >= 2437659 && mapitem.getItemId() <= 2437664) {
                              int plushour = 0;
                              int plusmit = 0;
                              switch(mapitem.getItemId()) {
                              case 2437659:
                                 plusmit = 10;
                                 break;
                              case 2437660:
                                 plusmit = 30;
                                 break;
                              case 2437661:
                                 plusmit = 50;
                                 break;
                              case 2437662:
                                 plushour = 2;
                                 break;
                              case 2437663:
                                 plushour = 4;
                                 break;
                              case 2437664:
                                 plushour = 9;
                              }

                              var56 = c.getPlayer().getMap();
                              var56.Papullatushour += plushour;
                              if (c.getPlayer().getMap().Papullatushour > 12) {
                                 var56 = c.getPlayer().getMap();
                                 var56.Papullatushour -= 12;
                              }

                              var56 = c.getPlayer().getMap();
                              var56.Papullatusminute += plusmit;
                              if (c.getPlayer().getMap().Papullatusminute >= 60) {
                                 ++c.getPlayer().getMap().Papullatushour;
                                 var56 = c.getPlayer().getMap();
                                 var56.Papullatusminute -= 60;
                              }

                              c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("파풀라투스의 시계가 움직입니다. 차원의 포탈을 통해 시간을 봉인하세요.", 5120177, true));
                              c.getPlayer().getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTimePatten(1, plushour, plusmit, 0));
                              removeItem(chr, mapitem, ob);
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           if (mapitem.getItemId() != 2437606 && mapitem.getItemId() != 2437607) {
                              if (!touchitem) {
                                 if (mapitem.getQuest() > 0 && chr.getQuestStatus(mapitem.getQuest()) != 1) {
                                    c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                                    return;
                                 }

                                 if (mapitem.getOwner() == chr.getId() || (mapitem.isPlayerDrop() || mapitem.getDropType() != 0) && (!mapitem.isPlayerDrop() || !chr.getMap().getEverlast())) {
                                    if (!mapitem.isPlayerDrop() && mapitem.getDropType() == 1 && mapitem.getOwner() != chr.getId() && (chr.getParty() == null || chr.getParty().getMemberById(mapitem.getOwner()) == null)) {
                                       c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                                       return;
                                    }

                                    if (mapitem.getMeso() > 0) {
                                       if (chr.getParty() != null && mapitem.getOwner() != chr.getId()) {
                                          LinkedList<MapleCharacter> toGive = new LinkedList();
                                          party = mapitem.getMeso() * 40 / 100;
                                          Iterator var44 = chr.getParty().getMembers().iterator();

                                          while(var44.hasNext()) {
                                             MaplePartyCharacter z = (MaplePartyCharacter)var44.next();
                                             MapleCharacter m = chr.getMap().getCharacterById(z.getId());
                                             if (m != null && m.getId() != chr.getId()) {
                                                toGive.add(m);
                                             }
                                          }

                                          MapleCharacter m;
                                          for(var44 = toGive.iterator(); var44.hasNext(); m.gainMeso((long)mesos, true, false, false, true)) {
                                             m = (MapleCharacter)var44.next();
                                             mesos = party / toGive.size() + (m.getStat().hasPartyBonus ? (int)((double)mapitem.getMeso() / 20.0D) : 0);
                                             if (mapitem.getDropper() instanceof MapleMonster && m.getStat().incMesoProp > 0) {
                                                mesos = (int)((double)mesos + Math.floor((double)((float)(m.getStat().incMesoProp * mesos) / 100.0F)));
                                             }
                                          }

                                          meso = mapitem.getMeso() - party;
                                          if (mapitem.getDropper() instanceof MapleMonster && chr.getStat().incMesoProp > 0) {
                                             meso = (int)((double)meso + Math.floor((double)((float)(chr.getStat().incMesoProp * meso) / 100.0F)));
                                          }

                                          chr.gainMeso((long)meso, true, false, false, true);
                                       } else {
                                          mesos = mapitem.getMeso();
                                          if (mapitem.getDropper() instanceof MapleMonster && chr.getStat().incMesoProp > 0) {
                                             mesos = (int)((double)mesos + Math.floor((double)((float)(chr.getStat().incMesoProp * mesos) / 100.0F)));
                                          }

                                          chr.gainMeso((long)mesos, true, false, false, true);
                                       }

                                       if (mapitem.getDropper().getType() == MapleMapObjectType.MONSTER) {
                                          c.getSession().writeAndFlush(CWvsContext.onMesoPickupResult(mapitem.getMeso()));
                                       }

                                       removeItem(chr, mapitem, ob);
                                       return;
                                    } else if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId())) {
                                       c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                                       c.getPlayer().dropMessage(5, "This item cannot be picked up.");
                                       return;
                                    } else if (c.getPlayer().inPVP() && Integer.parseInt(c.getPlayer().getEventInstance().getProperty("ice")) == c.getPlayer().getId()) {
                                       c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryFull());
                                       c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getShowInventoryFull());
                                       c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                                       return;
                                    } else if (mapitem.getItemId() == 2431835) {
                                       MapleItemInformationProvider.getInstance().getItemEffect(2002093).applyTo(chr, true);
                                       removeItem(chr, mapitem, ob);
                                       return;
                                    } else {
                                       if (mapitem.getItemId() / 10000 != 291 && MapleInventoryManipulator.checkSpace(c, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                                          if (mapitem.getItem().getQuantity() >= 50 && mapitem.getItemId() == 2340000) {
                                             c.setMonitored(true);
                                          }

                                          if (mapitem.getEquip() != null && mapitem.getDropper().getType() == MapleMapObjectType.MONSTER && mapitem.getEquip().getState() > 0) {
                                             c.getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 0, 65, 0, 0, (byte)0, true, (Point)null, (String)null, mapitem.getItem()));
                                          }

                                          if (GameConstants.isArcaneSymbol(mapitem.getItemId())) {
                                             Equip equip2 = (Equip)mapitem.getItem();
                                             equip2.setArc((short)30);
                                             equip2.setArcLevel(1);
                                             equip2.setArcEXP(1);
                                             if (GameConstants.isXenon(c.getPlayer().getJob())) {
                                                equip2.setStr((short)117);
                                                equip2.setDex((short)117);
                                                equip2.setLuk((short)117);
                                             } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
                                                equip2.setHp((short)525);
                                             } else if (GameConstants.isWarrior(c.getPlayer().getJob())) {
                                                equip2.setStr((short)300);
                                             } else if (GameConstants.isMagician(c.getPlayer().getJob())) {
                                                equip2.setInt((short)300);
                                             } else if (!GameConstants.isArcher(c.getPlayer().getJob()) && !GameConstants.isCaptain(c.getPlayer().getJob()) && !GameConstants.isMechanic(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                                                if (GameConstants.isThief(c.getPlayer().getJob())) {
                                                   equip2.setLuk((short)300);
                                                } else if (GameConstants.isPirate(c.getPlayer().getJob())) {
                                                   equip2.setStr((short)300);
                                                }
                                             } else {
                                                equip2.setDex((short)300);
                                             }
                                          } else {
                                             Equip equip;
                                             if (GameConstants.isAuthenticSymbol(mapitem.getItemId()) && (equip = (Equip)mapitem.getItem()).getArcLevel() == 0) {
                                                equip.setArc((short)10);
                                                equip.setArcLevel(1);
                                                equip.setArcEXP(1);
                                                if (GameConstants.isXenon(c.getPlayer().getJob())) {
                                                   equip.setStr((short)317);
                                                   equip.setDex((short)317);
                                                   equip.setLuk((short)317);
                                                } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
                                                   equip.setHp((short)725);
                                                } else if (GameConstants.isWarrior(c.getPlayer().getJob())) {
                                                   equip.setStr((short)500);
                                                } else if (GameConstants.isMagician(c.getPlayer().getJob())) {
                                                   equip.setInt((short)500);
                                                } else if (!GameConstants.isArcher(c.getPlayer().getJob()) && !GameConstants.isCaptain(c.getPlayer().getJob()) && !GameConstants.isMechanic(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                                                   if (GameConstants.isThief(c.getPlayer().getJob())) {
                                                      equip.setLuk((short)500);
                                                   } else if (GameConstants.isPirate(c.getPlayer().getJob())) {
                                                      equip.setStr((short)500);
                                                   }
                                                } else {
                                                   equip.setDex((short)500);
                                                }
                                             }
                                          }

                                          if (mapitem.getItem().getItemId() == 4001886) {
                                             if (mapitem.getItem().getBossid() == 0) {
                                                mapitem.getItem().setReward(new BossReward(chr.getInventory(MapleInventoryType.ETC).countById(4001886) + 1, 100100, 1, 1));
                                             } else {
                                                party = chr.getParty() != null ? chr.getParty().getMembers().size() : 1;
                                                meso = 0;
                                                mobid = 0;
                                                var33 = BossRewardMeso.getLists().iterator();

                                                while(var33.hasNext()) {
                                                   Triple<Integer, Integer, Integer> list = (Triple)var33.next();
                                                   bossid = BossRewardMeso.RewardBossId((Integer)list.getMid());
                                                   if (bossid == 0) {
                                                      bossid = (Integer)list.getLeft();
                                                   }

                                                   if (bossid == mapitem.getItem().getBossid() || (Integer)list.getMid() == mapitem.getItem().getBossid()) {
                                                      meso = (Integer)list.getRight();
                                                      mobid = (Integer)list.getMid();
                                                      break;
                                                   }
                                                }

                                                mapitem.getItem().setReward(new BossReward(chr.getInventory(MapleInventoryType.ETC).countById(4001886) + 1, mobid, party, meso));
                                                mapitem.getItem().setExpiration(System.currentTimeMillis() + 604800000L);
                                             }
                                          }

                                          String var55 = FileoutputUtil.아이템획득로그;
                                          var10001 = chr.getClient().getAccID();
                                          FileoutputUtil.log(var55, "[PickUP_Player] 계정번호 : " + var10001 + " | " + chr.getName() + "이(가)  맵 :" + c.getPlayer().getMapId() + "에서 " + MapleItemInformationProvider.getInstance().getName(mapitem.getItem().getItemId()) + "(" + mapitem.getItem().getItemId() + ")를 " + mapitem.getItem().getQuantity() + " 개 획득");
                                          MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, mapitem.getDropper() instanceof MapleMonster, false);
                                          removeItem(chr, mapitem, ob);
                                       } else {
                                          c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryFull());
                                          c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getShowInventoryFull());
                                          c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                                       }

                                       return;
                                    }
                                 }

                                 c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                                 return;
                              }

                              removeItem(chr, mapitem, ob);
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           MobSkill ms1 = MobSkillFactory.getMobSkill(241, 3);
                           int duration = false;
                           if (chr.getSkillCustomTime(241) != null) {
                              party = mapitem.getItemId() == 2437606 ? chr.getSkillCustomTime(241) * 2 : chr.getSkillCustomTime(241) / 2;
                              chr.cancelDisease(SecondaryStat.PapulCuss);
                              if (party > 60000) {
                                 party = 60000;
                              }

                              if (party > 0) {
                                 ms1.setDuration((long)party);
                                 chr.setSkillCustomInfo(241, 0L, (long)party);
                                 chr.giveDebuff(SecondaryStat.PapulCuss, ms1);
                              }
                           }

                           removeItem(chr, mapitem, ob);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           return;
                        }

                        if (chr.getBattleGroundChr() != null) {
                           chr.setSkillCustomInfo(80001741, chr.getSkillCustomValue0(80001741) + 1L, 0L);
                           chr.getBattleGroundChr().setTeam(2);
                           chr.getMap().broadcastMessage(chr, BattleGroundPacket.UpdateAvater(chr.getBattleGroundChr(), GameConstants.BattleGroundJobType(chr.getBattleGroundChr())), false);
                           chr.getBattleGroundChr().setTeam(1);
                           chr.getClient().send(BattleGroundPacket.UpdateAvater(chr.getBattleGroundChr(), GameConstants.BattleGroundJobType(chr.getBattleGroundChr())));
                        }

                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        removeItem(chr, mapitem, ob);
                        return;
                     }

                     if (mapitem.getDropper() instanceof MapleMonster) {
                        if (chr.getClient().getKeyValue("combokill") == null) {
                           chr.getClient().setKeyValue("combokill", "1");
                           chr.getClient().send(CField.ImageTalkNpc(9010049, 10000, "#b[안내] 콤보킬 퍼레이드#k\r\n\r\n콤보를 50단위씩 쌓아서 얻는 #b[콤보킬 퍼레이드]#k를 획득 하셨어요!\r\n\r\n#b[콤보킬 퍼레이드]#k에 접촉 시 #b추가 보너스 경험치#k를 획득할 수 있어요!"));
                        }

                        bonus = mapitem.getItemId() % 100 == 84 ? 5 : (mapitem.getItemId() % 100 == 94 ? 7 : (mapitem.getItemId() % 100 == 95 ? 10 : 12));
                        if (chr.getSkillLevel(20000297) > 0) {
                           bonus += SkillFactory.getSkill(20000297).getEffect(chr.getSkillLevel(20000297)).getX() / 100;
                        } else if (chr.getSkillLevel(80000370) > 0) {
                           bonus += SkillFactory.getSkill(80000370).getEffect(chr.getSkillLevel(80000370)).getX() / 100;
                        }

                        if (mapitem.getItemId() == 2023927) {
                           bonus = 30;
                        }

                        MapleMonster mob = (MapleMonster)mapitem.getDropper();
                        long exp = mob.getMobExp() * (long)bonus;
                        chr.gainExp(exp, true, true, false);
                        c.send(CField.EffectPacket.gainExp(exp));
                     }

                     c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                     removeItem(chr, mapitem, ob);
                     return;
                  }

                  c.getPlayer().getMap().setRPTicket(c.getPlayer().getMap().getRPTicket() + 1);
                  c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                  c.getPlayer().getMap().broadcastMessage(CWvsContext.getTopMsg("통행증 " + c.getPlayer().getMap().getRPTicket() + "장을 모았습니다."));
                  if (c.getPlayer().getMap().getRPTicket() == 20) {
                     c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(10));
                     c.getPlayer().getMap().broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
                     c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("통행증을 모두 모았습니다. 레드 벌룬에게 말을 걸어 다음 단계로 이동해 주세요.", 5120018, true));
                  }

                  removeItem(chr, mapitem, ob);
               } finally {
                  mapitem.getLock().unlock();
               }

            }
         }
      }
   }

   public static final void Pickup_Pet(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null) {
         if (!c.getPlayer().inPVP()) {
            MapleParty party_player = c.getPlayer().getParty();
            if (party_player != null && party_player.getPartyDrop() == 1 && party_player.getLeader().getId() != chr.getId()) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               c.getPlayer().setScrolledPosition((short)0);
               byte petz = (byte)slea.readInt();
               MaplePet pet = chr.getPet((long)petz);
               slea.skip(1);
               slea.readInt();
               Point Client_Reportedpos = slea.readPos();
               MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);
               if (ob != null && pet != null) {
                  MapleMapItem mapitem = (MapleMapItem)ob;

                  try {
                     mapitem.getLock().lock();
                     if (mapitem.isPickedUp()) {
                        return;
                     }

                     if (mapitem.getOwner() != chr.getId() && mapitem.isPlayerDrop() || mapitem.getItemId() == 2023484 || mapitem.getItemId() == 2023494 || mapitem.getItemId() == 2023495 || mapitem.getItemId() == 2023669 || mapitem.getItemId() == 2023927) {
                        return;
                     }

                     int party;
                     if (mapitem.getItemId() == 2431174) {
                        party = Randomizer.rand(1, 10) * 10;
                        c.getPlayer().gainHonor(party);
                        removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                        return;
                     }

                     if (mapitem.getItemId() == 2433103) {
                        party = Randomizer.rand(1, 10) * 10;
                        c.getPlayer().gainHonor(10000);
                        removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                        return;
                     }

                     if (mapitem.getItemId() == 4001169 && c.getPlayer().getMap().getMonstermarble() != 20) {
                        c.getPlayer().getMap().setMonstermarble(c.getPlayer().getMap().getMonstermarble() + 1);
                        c.getPlayer().getMap().broadcastMessage(CWvsContext.getTopMsg("몬스터구슬 " + c.getPlayer().getMap().getMonstermarble() + " / 20"));
                        removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                        return;
                     }

                     if (mapitem.getItemId() == 4001169) {
                        removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                        return;
                     }

                     int var10001;
                     if (mapitem.getItemId() == 4001101 && c.getPlayer().getMap().getMoonCake() != 80) {
                        c.getPlayer().getMap().setMoonCake(c.getPlayer().getMap().getMoonCake() + 1);
                        c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                        MapleMap var30 = c.getPlayer().getMap();
                        var10001 = c.getPlayer().getMap().getMoonCake();
                        var30.broadcastMessage(CWvsContext.getTopMsg("어흥이를 위해 월묘의 떡 " + var10001 + "개를 모았습니다.  앞으로 " + (80 - c.getPlayer().getMap().getMoonCake()) + "개 더!"));
                        removeItem(chr, mapitem, ob);
                        return;
                     }

                     if (mapitem.getItemId() == 4001101) {
                        removeItem(chr, mapitem, ob);
                        return;
                     }

                     if (mapitem.getItemId() == 4000884) {
                        c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("달맞이꽃 씨앗을 되찾았습니다.", 5120016, true));
                     }

                     if (mapitem.getItemId() == 4001022) {
                        c.getPlayer().getMap().setRPTicket(c.getPlayer().getMap().getRPTicket() + 1);
                        c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                        c.getPlayer().getMap().broadcastMessage(CWvsContext.getTopMsg("통행증 " + c.getPlayer().getMap().getRPTicket() + "장을 모았습니다."));
                        if (c.getPlayer().getMap().getRPTicket() == 20) {
                           c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(10));
                           c.getPlayer().getMap().broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
                           c.getPlayer().getMap().broadcastMessage(CField.startMapEffect("통행증을 모두 모았습니다. 레드 벌룬에게 말을 걸어 다음 단계로 이동해 주세요.", 5120018, true));
                        }

                        removeItem(chr, mapitem, ob);
                        return;
                     }

                     if (mapitem.getItemId() != 4001022) {
                        if (mapitem.getItemId() != 2633304 && mapitem.getItemId() != 2633609) {
                           int meso;
                           int mobid;
                           if (mapitem.getItemId() != 2633343) {
                              if (mapitem.getItemId() == 2002058) {
                                 if (c.getPlayer().hasDisease(SecondaryStat.DeathMark)) {
                                    c.getPlayer().cancelDisease(SecondaryStat.DeathMark);
                                 }

                                 c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                                 removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                                 return;
                              }

                              if (mapitem.getItemId() == 2434851) {
                                 if (!chr.getBuffedValue(25121133)) {
                                    c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                                    return;
                                 }

                                 SecondaryStatEffect effect = SkillFactory.getSkill(25121133).getEffect(1);
                                 long duration = chr.getBuffLimit(25121133);
                                 duration += 4000L;
                                 if (duration >= (long)effect.getCoolRealTime()) {
                                    duration = (long)effect.getCoolRealTime();
                                 }

                                 effect.applyTo(chr, chr, false, chr.getPosition(), (int)duration, (byte)0, false);
                                 c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryStatus(true));
                                 removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                                 return;
                              }

                              if (mapitem.getOwner() != chr.getId() && (!mapitem.isPlayerDrop() && mapitem.getDropType() == 0 || mapitem.isPlayerDrop() && chr.getMap().getEverlast())) {
                                 return;
                              }

                              if (!mapitem.isPlayerDrop() && mapitem.getDropType() == 1 && mapitem.getOwner() != chr.getId() && (chr.getParty() == null || chr.getParty().getMemberById(mapitem.getOwner()) == null)) {
                                 return;
                              }

                              Equip equip;
                              if (GameConstants.isArcaneSymbol(mapitem.getItemId())) {
                                 equip = (Equip)mapitem.getItem();
                                 equip.setArc((short)30);
                                 equip.setArcLevel(1);
                                 equip.setArcEXP(1);
                                 if (GameConstants.isXenon(c.getPlayer().getJob())) {
                                    equip.setStr((short)117);
                                    equip.setDex((short)117);
                                    equip.setLuk((short)117);
                                 } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
                                    equip.setHp((short)525);
                                 } else if (GameConstants.isWarrior(c.getPlayer().getJob())) {
                                    equip.setStr((short)300);
                                 } else if (GameConstants.isMagician(c.getPlayer().getJob())) {
                                    equip.setInt((short)300);
                                 } else if (!GameConstants.isArcher(c.getPlayer().getJob()) && !GameConstants.isCaptain(c.getPlayer().getJob()) && !GameConstants.isMechanic(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                                    if (GameConstants.isThief(c.getPlayer().getJob())) {
                                       equip.setLuk((short)300);
                                    } else if (GameConstants.isPirate(c.getPlayer().getJob())) {
                                       equip.setStr((short)300);
                                    }
                                 } else {
                                    equip.setDex((short)300);
                                 }
                              } else if (GameConstants.isAuthenticSymbol(mapitem.getItemId())) {
                                 equip = (Equip)mapitem.getItem();
                                 if (equip.getArcLevel() == 0) {
                                    equip.setArc((short)10);
                                    equip.setArcLevel(1);
                                    equip.setArcEXP(1);
                                    if (GameConstants.isXenon(c.getPlayer().getJob())) {
                                       equip.setStr((short)317);
                                       equip.setDex((short)317);
                                       equip.setLuk((short)317);
                                    } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
                                       equip.setHp((short)725);
                                    } else if (GameConstants.isWarrior(c.getPlayer().getJob())) {
                                       equip.setStr((short)500);
                                    } else if (GameConstants.isMagician(c.getPlayer().getJob())) {
                                       equip.setInt((short)500);
                                    } else if (!GameConstants.isArcher(c.getPlayer().getJob()) && !GameConstants.isCaptain(c.getPlayer().getJob()) && !GameConstants.isMechanic(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                                       if (GameConstants.isThief(c.getPlayer().getJob())) {
                                          equip.setLuk((short)500);
                                       } else if (GameConstants.isPirate(c.getPlayer().getJob())) {
                                          equip.setStr((short)500);
                                       }
                                    } else {
                                       equip.setDex((short)500);
                                    }
                                 }
                              }

                              if (mapitem.isPickpoket()) {
                                 return;
                              }

                              if (mapitem.getMeso() > 0) {
                                 chr.gainMeso((long)mapitem.getMeso(), true, false, true, true);
                                 removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                                 return;
                              } else {
                                 if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId()) || mapitem.getItemId() / 10000 == 291) {
                                    return;
                                 }

                                 if (useItem(c, mapitem.getItemId())) {
                                    removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                                    return;
                                 } else {
                                    if (mapitem.getItemId() == 2431835) {
                                       MapleItemInformationProvider.getInstance().getItemEffect(2002093).applyTo(chr, true);
                                       removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                                    } else if (MapleInventoryManipulator.checkSpace(c, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                                       if (mapitem.getItem().getItemId() == 4001886) {
                                          if (mapitem.getItem().getBossid() == 0) {
                                             mapitem.getItem().setReward(new BossReward(chr.getInventory(MapleInventoryType.ETC).countById(4001886) + 1, 100100, 1, 1));
                                          } else {
                                             party = chr.getParty() != null ? chr.getParty().getMembers().size() : 1;
                                             meso = 0;
                                             mobid = 0;
                                             Iterator var26 = BossRewardMeso.getLists().iterator();

                                             while(var26.hasNext()) {
                                                Triple<Integer, Integer, Integer> list = (Triple)var26.next();
                                                int bossid = BossRewardMeso.RewardBossId((Integer)list.getMid());
                                                if (bossid == 0) {
                                                   bossid = (Integer)list.getLeft();
                                                }

                                                if (bossid == mapitem.getItem().getBossid() || (Integer)list.getMid() == mapitem.getItem().getBossid()) {
                                                   meso = (Integer)list.getRight();
                                                   mobid = (Integer)list.getMid();
                                                   break;
                                                }
                                             }

                                             mapitem.getItem().setExpiration(System.currentTimeMillis() + 604800000L);
                                             mapitem.getItem().setReward(new BossReward(chr.getInventory(MapleInventoryType.ETC).countById(4001886) + 1, mobid, party, meso));
                                          }
                                       }

                                       MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), false, mapitem.getDropper() instanceof MapleMonster, true);
                                       removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                                       if (mapitem.getEquip() != null && mapitem.getDropper().getType() == MapleMapObjectType.MONSTER && mapitem.getEquip().getState() > 0) {
                                          c.getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 0, 65, 0, 0, (byte)0, true, (Point)null, (String)null, mapitem.getItem()));
                                       }

                                       return;
                                    }

                                    return;
                                 }
                              }
                           }

                           if (chr.getBuffedValue(80003046)) {
                              party = 0;
                              meso = chr.getQuestStatus(100801) == 2 ? 1 : (chr.getQuestStatus(100802) == 2 ? 2 : 0);

                              for(mobid = 0; mobid < meso; ++mobid) {
                                 if (Randomizer.isSuccess(50)) {
                                    ++party;
                                 }
                              }

                              meso = (int)chr.getKeyValue(100803, "count");
                              if (meso < 0) {
                                 meso = 0;
                              }

                              chr.setKeyValue(100803, "count", (meso + 1).makeConcatWithConstants<invokedynamic>(meso + 1));
                              chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 1 : 2, mapitem.getPosition());
                              chr.getClient().send(SLFCGPacket.EventSkillOnFlowerEffect(7, 1));
                              if (chr.getKeyValue(100803, "count") == 5L) {
                                 chr.getClient().send(CField.enforceMsgNPC(9062527, 1300, "꽃의 보석에 햇살의 힘이 #r절반#k이나 모였어요!"));
                              } else if (chr.getKeyValue(100803, "count") == 10L) {
                                 chr.setKeyValue(100803, "count", "0");
                                 chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40, chr.getPosition());
                                 chr.getClient().send(SLFCGPacket.EventSkillOn(1, 1 + party));
                                 chr.getClient().send(CField.enforceMsgNPC(9062527, 1000, "햇살의 힘으로 #r꽃씨#k를 펑!펑!"));
                                 MapleClient var10000 = chr.getClient();
                                 var10001 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40;
                                 var10000.send(CField.UIPacket.detailShowInfo("하나의 꽃씨 뿌리기로 블루밍 코인 " + var10001 + "개를 획득했습니다.", 3, 20, 20));
                                 if (party >= 1 && chr.getQuestStatus(100801) == 2) {
                                    Timer.BuffTimer.getInstance().schedule(() -> {
                                       chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 10 : 20, chr.getPosition());
                                       chr.getClient().send(CField.enforceMsgNPC(9062528, 1000, "#r꽃비#k가 쏴아아!\r\n너무 예뻐!"));
                                       MapleClient var10000 = chr.getClient();
                                       int var10001 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 10 : 20;
                                       var10000.send(CField.UIPacket.detailShowInfo("두나의 꽃비로 블루밍 코인 " + var10001 + "개를 획득했습니다.", 3, 20, 20));
                                    }, 2500L);
                                 }

                                 if (party == 2 && chr.getQuestStatus(100802) == 2) {
                                    Timer.BuffTimer.getInstance().schedule(() -> {
                                       chr.AddBloomingCoin(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40, chr.getPosition());
                                       chr.getClient().send(CField.enforceMsgNPC(9062529, 1000, "#r햇살#k이 샤라랑!\r\n꽃들아 잘 자라라~!"));
                                       MapleClient var10000 = chr.getClient();
                                       int var10001 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 20 : 40;
                                       var10000.send(CField.UIPacket.detailShowInfo("세나의 햇살 비추기로 블루밍 코인 " + var10001 + "개를 획득했습니다.", 3, 20, 20));
                                    }, 4500L);
                                 }
                              }

                              if (chr.getKeyValue(100794, "today") >= (long)(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000 : 6000)) {
                                 chr.getClient().send(CField.enforceMsgNPC(9062527, 1000, "오늘은 이만하면 됐어요."));
                                 chr.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(1297));
                                 chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062524, 0));
                                 chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062525, 0));
                                 chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062526, 0));
                                 chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.EventSpecialSkill));
                              }
                           }

                           removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                           return;
                        }

                        String[] bossname = new String[]{"이지 시그너스", "하드 힐라", "카오스 핑크빈", "노멀 시그너스", "카오스 자쿰", "카오스 피에르", "카오스 반반", "카오스 블러디퀸", "하드 매그너스", "카오스 벨룸", "카오스 파풀라투스", "노멀 스우", "노멀 데미안", "이지 루시드", "노멀 루시드", "노멀 윌", "노멀 더스크", "노멀 듄켈", "하드 데미안", "하드 스우", "하드 루시드", "하드 윌", "카오스 더스크", "하드 듄켈", "진 힐라", "세렌"};
                        String[] wishCoinCheck = null;
                        if (ServerConstants.Event_MapleLive) {
                           wishCoinCheck = chr.getClient().getCustomKeyValueStr(501468, "reward").split("");
                        }

                        String NewKeyvalue = "";
                        int i = 0;

                        for(Iterator var13 = ServerConstants.NeoPosList.iterator(); var13.hasNext(); ++i) {
                           Pair<Integer, Integer> list = (Pair)var13.next();
                           MapleMonster mob = (MapleMonster)mapitem.getDropper();
                           if ((Integer)list.getLeft() == mob.getId()) {
                              int coincount = (Integer)list.getRight();
                              int questid;
                              if (ServerConstants.Event_MapleLive) {
                                 questid = 501471 + i;
                                 if (chr.getClient().getCustomKeyValue(questid, "state") == 1L || chr.getClient().getCustomKeyValue(questid, "state") == 2L) {
                                    chr.getClient().send(CField.enforceMsgNPC(9062558, 3000, "이런, 이미 이번주에 #r" + bossname[i] + "#k를 처치했군."));
                                    break;
                                 }

                                 if (Randomizer.isSuccess(50)) {
                                    chr.getClient().send(CField.enforceMsgNPC(9062558, 3000, "호오, #r" + bossname[i] + "#k를 처치했군.\r\n검은콩 #b" + coincount + "#k개를 찾았다."));
                                 } else {
                                    chr.getClient().send(CField.enforceMsgNPC(9062558, 3000, "좋아, #r" + bossname[i] + "#k를 처치했군.\r\n검은콩 #b" + coincount + "#k개를 찾았어."));
                                 }
                              }

                              wishCoinCheck[i] = "1";

                              for(questid = 0; questid < wishCoinCheck.length; ++questid) {
                                 NewKeyvalue = NewKeyvalue + wishCoinCheck[questid];
                              }

                              if (ServerConstants.Event_MapleLive) {
                                 questid = 501471 + i;
                                 chr.getClient().setCustomKeyValue(501468, "reward", NewKeyvalue);
                                 chr.getClient().setCustomKeyValue(questid, "state", "1");
                              }
                              break;
                           }
                        }

                        removeItem_Pet(chr, mapitem, petz, pet.getPetItemId());
                        return;
                     }

                     removeItem(chr, mapitem, ob);
                  } finally {
                     mapitem.getLock().unlock();
                  }

               }
            }
         }
      }
   }

   public static final boolean useItem(MapleClient c, int id) {
      if (GameConstants.isUse(id)) {
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         SecondaryStatEffect eff = ii.getItemEffect(id);
         if (eff == null) {
            return false;
         }

         Iterator var5;
         if (id / 10000 == 291) {
            boolean area = false;
            var5 = c.getPlayer().getMap().getAreas().iterator();

            while(true) {
               if (var5.hasNext()) {
                  Rectangle rect = (Rectangle)var5.next();
                  if (!rect.contains(c.getPlayer().getTruePosition())) {
                     continue;
                  }

                  area = true;
               }

               if (!c.getPlayer().inPVP() || c.getPlayer().getTeam() == id - 2910000 && area) {
                  return false;
               }
               break;
            }
         }

         int consumeval = eff.getConsume();
         if (consumeval > 0) {
            if (c.getPlayer().getMapId() != 109090300) {
               consumeItem(c, eff);
               consumeItem(c, ii.getItemEffectEX(id));
               c.getSession().writeAndFlush(CWvsContext.InfoPacket.getShowItemGain(id, (short)-1, true));
               return true;
            }

            var5 = c.getPlayer().getMap().getCharacters().iterator();

            while(true) {
               MapleCharacter chr;
               do {
                  do {
                     if (!var5.hasNext()) {
                        c.getSession().writeAndFlush(CWvsContext.InfoPacket.getShowItemGain(id, (short)-1, true));
                        return true;
                     }

                     chr = (MapleCharacter)var5.next();
                  } while(chr == null);
               } while((id != 2022163 || c.getPlayer().isCatched != chr.isCatched) && (id != 2022165 && id != 2022166 || c.getPlayer().isCatched == chr.isCatched));

               if (id == 2022163) {
                  ii.getItemEffect(id).applyTo(chr);
               } else if (id == 2022166) {
                  chr.giveDebuff(SecondaryStat.Stun, MobSkillFactory.getMobSkill(123, 1));
               } else if (id == 2022165) {
                  chr.giveDebuff(SecondaryStat.Slow, MobSkillFactory.getMobSkill(126, 1));
               }
            }
         }
      }

      return false;
   }

   public static final void consumeItem(MapleClient c, SecondaryStatEffect eff) {
      if (eff != null) {
         if (eff.getConsume() == 2) {
            if (c.getPlayer().getParty() != null && c.getPlayer().isAlive()) {
               Iterator var2 = c.getPlayer().getParty().getMembers().iterator();

               while(var2.hasNext()) {
                  MaplePartyCharacter pc = (MaplePartyCharacter)var2.next();
                  MapleCharacter chr = c.getPlayer().getMap().getCharacterById(pc.getId());
                  if (chr != null && chr.isAlive()) {
                     eff.applyTo(chr, true);
                  }
               }
            } else {
               eff.applyTo(c.getPlayer(), true);
            }
         } else if (c.getPlayer().isAlive()) {
            eff.applyTo(c.getPlayer(), true);
         }

      }
   }

   public static final void removeItem_Pet(MapleCharacter chr, MapleMapItem mapitem, int index, int id) {
      mapitem.setPickedUp(true);
      chr.getMap().broadcastMessage(CField.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), index));
      chr.getMap().removeMapObject(mapitem);
      if (mapitem.isRandDrop()) {
         chr.getMap().spawnRandDrop();
      }

   }

   public static final void removeItem(MapleCharacter chr, MapleMapItem mapitem, MapleMapObject ob) {
      mapitem.setPickedUp(true);
      chr.getMap().broadcastMessage(CField.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
      chr.getMap().removeMapObject(mapitem);
      if (mapitem.isRandDrop()) {
         chr.getMap().spawnRandDrop();
      }

   }

   public static final void addMedalString(MapleCharacter c, StringBuilder sb) {
      Item medal = c.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-21);
      if (medal != null) {
         sb.append("<");
         if (medal.getItemId() == 1142257 && GameConstants.isAdventurer(c.getJob())) {
            MapleQuestStatus stat = c.getQuestNoAdd(MapleQuest.getInstance(111111));
            if (stat != null && stat.getCustomData() != null) {
               sb.append(stat.getCustomData());
               sb.append("'s Successor");
            } else {
               sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
            }
         } else {
            sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
         }

         sb.append("> ");
      }

   }

   public static final void TeleRock(LittleEndianAccessor slea, MapleClient c) {
      short slot = slea.readShort();
      int itemId = slea.readInt();
      Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
      if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && itemId / 10000 == 232) {
         boolean used = UseTeleRock(slea, c, itemId);
         if (used) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
         }

         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static final boolean UseTeleRock(LittleEndianAccessor slea, MapleClient c, int itemId) {
      boolean used = false;
      if (slea.readByte() == 0) {
         MapleMap target = c.getChannelServer().getMapFactory().getMap(slea.readInt());
         if (target != null && (itemId == 5041000 && c.getPlayer().isRockMap(target.getId()) || (itemId == 5040000 || itemId == 5040001) && c.getPlayer().isRegRockMap(target.getId()) || (itemId == 5040004 || itemId == 5041001) && (c.getPlayer().isHyperRockMap(target.getId()) || GameConstants.isHyperTeleMap(target.getId())))) {
            if (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit()) && !FieldLimitType.VipRock.check(target.getFieldLimit())) {
               c.getPlayer().changeMap(target, target.getPortal(0));
               used = true;
            } else {
               c.getPlayer().dropMessage(1, "You cannot go to that place.");
            }
         } else {
            c.getPlayer().dropMessage(1, "You cannot go to that place.");
         }
      } else {
         String name = slea.readMapleAsciiString();
         MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
         if (victim != null && !victim.isIntern() && c.getPlayer().getEventInstance() == null && victim.getEventInstance() == null) {
            if (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit()) && !FieldLimitType.VipRock.check(c.getChannelServer().getMapFactory().getMap(victim.getMapId()).getFieldLimit())) {
               if (itemId != 5041000 && itemId != 5040004 && itemId != 5041001 && victim.getMapId() / 100000000 != c.getPlayer().getMapId() / 100000000) {
                  c.getPlayer().dropMessage(1, "You cannot go to that place.");
               } else {
                  c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestPortal(victim.getTruePosition()));
                  used = true;
               }
            } else {
               c.getPlayer().dropMessage(1, "You cannot go to that place.");
            }
         } else {
            c.getPlayer().dropMessage(1, "(" + name + ") is currently difficult to locate, so the teleport will not take place.");
         }
      }

      return used;
   }

   public static void UsePetLoot(LittleEndianAccessor slea, MapleClient c) {
      slea.readInt();
      short mode = slea.readShort();
      c.getPlayer().setPetLoot(mode == 1);

      for(int i = 0; i < c.getPlayer().getPets().length; ++i) {
         if (c.getPlayer().getPet((long)i) != null) {
            c.getSession().writeAndFlush(PetPacket.updatePet(c.getPlayer(), c.getPlayer().getPet((long)i), c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(c.getPlayer().getPet((long)i).getInventoryPosition()), false, c.getPlayer().getPetLoot()));
         }
      }

      c.getSession().writeAndFlush(PetPacket.updatePetLootStatus(mode));
   }

   public static void SelectPQReward(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(1);
      int randval = RandomRewards.getRandomReward();
      short quantity = (short)Randomizer.rand(1, 10);
      MapleInventoryManipulator.addById(c, randval, quantity, "Reward item: " + randval + " on " + FileoutputUtil.CurrentReadable_Date());
      if (c.getPlayer().getMapId() == 100000203) {
         MapleMap map = c.getChannelServer().getMapFactory().getMap(960000000);
         c.getPlayer().changeMap(map, map.getPortal(0));
      } else {
         c.getPlayer().fakeRelog();
      }

      c.getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(c.getPlayer(), randval, 1, true, ""));
   }

   public static void resetZeroWeapon(MapleCharacter chr) {
      Equip newa = (Equip)MapleItemInformationProvider.getInstance().getEquipById(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId());
      Equip newb = (Equip)MapleItemInformationProvider.getInstance().getEquipById(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10).getItemId());
      ((Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11)).set(newa);
      ((Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10)).set(newb);
      chr.getClient().getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11)));
      chr.getClient().getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10)));
      chr.dropMessage(5, "제로의 장비는 파괴되는대신 처음 상태로 되돌아갑니다.");
   }

   public static void UseNameChangeCoupon(LittleEndianAccessor slea, MapleClient c) {
      short slot = slea.readShort();
      int itemId = slea.readInt();
      Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
      if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
         c.setNameChangeEnable((byte)1);
         MapleCharacter.updateNameChangeCoupon(c);
         c.getSession().writeAndFlush(CWvsContext.nameChangeUI(true));
         MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
      } else {
         c.getSession().writeAndFlush(CWvsContext.nameChangeUI(false));
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static void UseKaiserColorChange(LittleEndianAccessor slea, MapleClient c) {
      short slot = slea.readShort();
      slea.skip(2);
      int itemId = slea.readInt();
      Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
      if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
         int[] colors = new int[]{841, 842, 843, 758, 291, 317, 338, 339, 444, 445, 446, 458, 461, 447, 450, 454, 455, 456, 457, 459, 460, 462, 463, 464, 289, 4, 34, 35, 64, 9, 10, 12, 11, 16, 17, 22, 24, 53, 61, 62, 63, 67, 68, 109, 110, 111, 112, 113, 114, 115, 116, 117, 121, 125, 128, 129, 145, 150};
         if (itemId == 2350004) {
            c.getPlayer().setKeyValue(12860, "extern", colors[Randomizer.nextInt(colors.length)].makeConcatWithConstants<invokedynamic>(colors[Randomizer.nextInt(colors.length)]));
         } else if (itemId == 2350005) {
            c.getPlayer().setKeyValue(12860, "inner", colors[Randomizer.nextInt(colors.length)].makeConcatWithConstants<invokedynamic>(colors[Randomizer.nextInt(colors.length)]));
         } else if (itemId == 2350006) {
            c.getPlayer().setKeyValue(12860, "extern", "842");
         } else if (itemId == 2350007) {
            c.getPlayer().setKeyValue(12860, "premium", "0");
            c.getPlayer().setKeyValue(12860, "inner", "0");
            c.getPlayer().setKeyValue(12860, "extern", "0");
         }

         if (c.getPlayer().getKeyValue(12860, "extern") == -1L) {
            c.getPlayer().setKeyValue(12860, "extern", "0");
         }

         if (c.getPlayer().getKeyValue(12860, "inner") == -1L) {
            c.getPlayer().setKeyValue(12860, "inner", "0");
         }

         if (c.getPlayer().getKeyValue(12860, "premium") == -1L) {
            c.getPlayer().setKeyValue(12860, "premium", "0");
         }

         c.getPlayer().getMap().broadcastMessage(CField.KaiserChangeColor(c.getPlayer().getId(), c.getPlayer().getKeyValue(12860, "extern") == -1L ? 0 : (int)c.getPlayer().getKeyValue(12860, "extern"), c.getPlayer().getKeyValue(12860, "inner") == -1L ? 0 : (int)c.getPlayer().getKeyValue(12860, "inner"), c.getPlayer().getKeyValue(12860, "premium") == -1L ? 0 : (byte)((int)c.getPlayer().getKeyValue(12860, "premium"))));
         MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static final void UseSoulEnchanter(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      slea.skip(4);
      short useslot = slea.readShort();
      short slot = slea.readShort();
      MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
      Item enchanter = useInventory.getItem(useslot);
      Item equip;
      if (slot == -11) {
         equip = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
      } else {
         equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
      }

      Equip nEquip = (Equip)equip;
      nEquip.setSoulEnchanter((short)9);
      c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, nEquip));
      chr.getMap().broadcastMessage(chr, CField.showEnchanterEffect(chr.getId(), (byte)1), true);
      MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.USE, enchanter.getItemId(), 1, true, false);
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static final void UseSoulScroll(LittleEndianAccessor rh, MapleClient c, MapleCharacter chr) {
      rh.skip(4);
      short useslot = rh.readShort();
      short slot = rh.readShort();
      MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
      Item soul = useInventory.getItem(useslot);
      int soula = soul.getItemId() - 2590999;
      int soulid = soul.getItemId();
      boolean great = false;
      MapleDataProvider sourceData = MapleDataProviderFactory.getDataProvider(new File("wz/Item.wz"));
      MapleData dd = sourceData.getData("SkillOption.img");
      int skillid = MapleDataTool.getIntConvert(dd.getChildByPath("skill/" + soula + "/skillId"));
      Item equip;
      if (slot == -11) {
         equip = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
      } else {
         equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
      }

      if (slot == -11) {
         chr.setSoulMP((Equip)equip);
      }

      if (dd.getChildByPath("skill/" + soula + "/tempOption/1/id") != null) {
         great = true;
      }

      short statid = false;
      short statid;
      if (great) {
         statid = (short)MapleDataTool.getIntConvert(dd.getChildByPath("skill/" + soula + "/tempOption/" + Randomizer.nextInt(7) + "/id"));
      } else {
         statid = (short)MapleDataTool.getIntConvert(dd.getChildByPath("skill/" + soula + "/tempOption/0/id"));
      }

      Equip nEquip = (Equip)equip;
      if (SkillFactory.getSkill(nEquip.getSoulSkill()) != null) {
         chr.changeSkillLevel(nEquip.getSoulSkill(), (byte)-1, (byte)0);
      }

      nEquip.setSoulName(GameConstants.getSoulName(soulid));
      nEquip.setSoulPotential(statid);
      nEquip.setSoulSkill(skillid);
      Equip zeros = null;
      if (GameConstants.isZero(c.getPlayer().getJob())) {
         if (slot == -11) {
            zeros = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
         } else if (slot == -10) {
            zeros = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         }
      }

      if (zeros != null) {
         if (SkillFactory.getSkill(zeros.getSoulSkill()) != null) {
            chr.changeSkillLevel(zeros.getSoulSkill(), (byte)-1, (byte)0);
         }

         zeros.setSoulName(nEquip.getSoulName());
         zeros.setSoulPotential(nEquip.getSoulPotential());
         zeros.setSoulSkill(nEquip.getSoulSkill());
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, zeros));
      }

      chr.changeSkillLevel(skillid, (byte)1, (byte)1);
      c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, nEquip));
      chr.getMap().broadcastMessage(chr, CField.showSoulScrollEffect(chr.getId(), (byte)1, false, nEquip), true);
      MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.USE, soulid, 1, true, false);
      c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
   }

   public static void UseCube(LittleEndianAccessor slea, MapleClient c) {
      int pos = false;
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Item cube = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slea.readShort());
      if (cube.getItemId() >= 2730000 && cube.getItemId() <= 2730005) {
         int itemid = slea.readInt();
         if (itemid != cube.getItemId()) {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            return;
         }
      }

      int pos = slea.readShort();
      Equip eq = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)pos);
      int rand2;
      boolean up2;
      int potential1;
      int potential2;
      int potential3;
      Equip eq2;
      boolean up;
      int level2;
      int rate;
      int rand;
      switch(cube.getItemId()) {
      case 2436499:
      case 2711000:
      case 2711001:
      case 2711009:
      case 2711011:
         if (GameConstants.isZero(c.getPlayer().getJob()) && eq == null) {
            eq = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
            eq2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            rand = Randomizer.nextInt(100);
            up = false;
            if (c.getPlayer().getMeso() < (long)GameConstants.getCubeMeso(eq.getItemId())) {
               c.getPlayer().dropMessage(6, "메소가 부족합니다.");
               return;
            }

            if (eq.getState() == 17) {
               if (rand < 2) {
                  eq.setState((byte)18);
                  eq2.setState((byte)18);
                  up = true;
               } else {
                  eq.setState((byte)17);
                  eq2.setState((byte)17);
               }
            } else if (eq.getState() == 18) {
               eq.setState((byte)18);
               eq2.setState((byte)18);
            }

            rate = eq.getState() - 16;
            potential1 = potential(eq.getItemId(), rate);
            potential2 = potential(eq.getItemId(), rate != 1 && !Randomizer.isSuccess(2, 200) ? rate - 1 : rate);
            potential3 = eq.getPotential3() == 0 ? 0 : potential(eq.getItemId(), rate != 1 && !Randomizer.isSuccess(1, 200) ? rate - 1 : rate);
            eq.setPotential1(potential1);
            eq.setPotential2(potential2);
            eq.setPotential3(potential3);
            eq2.setPotential1(potential1);
            eq2.setPotential2(potential2);
            eq2.setPotential3(potential3);
            c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(eq.getItemId())), false);
            c.getSession().writeAndFlush(CField.showPotentialReset(c.getPlayer().getId(), true, cube.getItemId(), eq.getItemId()));
            c.getSession().writeAndFlush(CField.getCubeStart(c.getPlayer(), eq, up, cube.getItemId(), cube.getQuantity()));
            c.getPlayer().forceReAddItem(eq, MapleInventoryType.EQUIPPED);
            c.getPlayer().forceReAddItem(eq2, MapleInventoryType.EQUIPPED);
         } else {
            rand2 = Randomizer.nextInt(100);
            up2 = false;
            if (c.getPlayer().getMeso() < (long)GameConstants.getCubeMeso(eq.getItemId())) {
               c.getPlayer().dropMessage(6, "메소가 부족합니다.");
               return;
            }

            if (eq.getState() == 17) {
               if (rand2 < 2) {
                  eq.setState((byte)18);
                  up2 = true;
               } else {
                  eq.setState((byte)17);
               }
            } else if (eq.getState() == 18) {
               eq.setState((byte)18);
            }

            level2 = eq.getState() - 16;
            eq.setPotential1(potential(eq.getItemId(), level2));
            eq.setPotential2(potential(eq.getItemId(), level2 != 1 && !Randomizer.isSuccess(2, 200) ? level2 - 1 : level2));
            eq.setPotential3(eq.getPotential3() == 0 ? 0 : potential(eq.getItemId(), level2 != 1 && !Randomizer.isSuccess(1, 200) ? level2 - 1 : level2));
            c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(eq.getItemId())), false);
            c.getSession().writeAndFlush(CField.showPotentialReset(c.getPlayer().getId(), true, cube.getItemId(), eq.getItemId()));
            c.getSession().writeAndFlush(CField.getCubeStart(c.getPlayer(), eq, up2, cube.getItemId(), cube.getQuantity()));
            c.getPlayer().forceReAddItem(eq, MapleInventoryType.EQUIP);
         }
         break;
      case 2711003:
      case 2711005:
      case 2711012:
         if (GameConstants.isZero(c.getPlayer().getJob()) && eq == null) {
            eq = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
            eq2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            rand = Randomizer.nextInt(100);
            up = false;
            if (c.getPlayer().getMeso() < (long)GameConstants.getCubeMeso(eq.getItemId())) {
               c.getPlayer().dropMessage(6, "메소가 부족합니다.");
               return;
            }

            if (eq.getState() == 17) {
               if (Randomizer.isSuccess(2, 200)) {
                  eq.setState((byte)18);
                  eq2.setState((byte)18);
                  up = true;
               } else {
                  eq.setState((byte)17);
                  eq2.setState((byte)17);
               }
            } else if (eq.getState() == 18) {
               if (Randomizer.isSuccess(1, 200)) {
                  up = true;
                  eq.setState((byte)19);
                  eq2.setState((byte)19);
               } else {
                  eq.setState((byte)18);
               }
            } else if (eq.getState() == 19) {
               eq.setState((byte)19);
            }

            rate = eq.getState() - 16;
            potential1 = potential(eq.getItemId(), rate);
            potential2 = potential(eq.getItemId(), rate != 1 && !Randomizer.isSuccess(2, 200) ? rate - 1 : rate);
            potential3 = eq.getPotential3() == 0 ? 0 : potential(eq.getItemId(), rate != 1 && !Randomizer.isSuccess(1, 200) ? rate - 1 : rate);
            eq.setPotential1(potential1);
            eq.setPotential2(potential2);
            eq.setPotential3(potential3);
            eq2.setPotential1(potential1);
            eq2.setPotential2(potential2);
            eq2.setPotential3(potential3);
            c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(eq.getItemId())), false);
            c.getSession().writeAndFlush(CField.showPotentialReset(c.getPlayer().getId(), true, cube.getItemId(), eq.getItemId()));
            c.getSession().writeAndFlush(CField.getCubeStart(c.getPlayer(), eq, up, cube.getItemId(), cube.getQuantity()));
            c.getPlayer().forceReAddItem(eq, MapleInventoryType.EQUIPPED);
            c.getPlayer().forceReAddItem(eq2, MapleInventoryType.EQUIPPED);
         } else {
            rand2 = Randomizer.nextInt(100);
            up2 = false;
            if (c.getPlayer().getMeso() < (long)GameConstants.getCubeMeso(eq.getItemId())) {
               c.getPlayer().dropMessage(6, "메소가 부족합니다.");
               return;
            }

            if (eq.getState() == 17) {
               if (Randomizer.isSuccess(2, 200)) {
                  eq.setState((byte)18);
                  up2 = true;
               } else {
                  eq.setState((byte)17);
               }
            } else if (eq.getState() == 18) {
               if (Randomizer.isSuccess(1, 200)) {
                  up2 = true;
                  eq.setState((byte)19);
               } else {
                  eq.setState((byte)18);
               }
            } else if (eq.getState() == 19) {
               eq.setState((byte)19);
            }

            level2 = eq.getState() - 16;
            eq.setPotential1(potential(eq.getItemId(), level2));
            eq.setPotential2(potential(eq.getItemId(), level2 != 1 && !Randomizer.isSuccess(2, 200) ? level2 - 1 : level2));
            eq.setPotential3(eq.getPotential3() == 0 ? 0 : potential(eq.getItemId(), level2 != 1 && !Randomizer.isSuccess(1, 200) ? level2 - 1 : level2));
            c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(eq.getItemId())), false);
            c.getSession().writeAndFlush(CField.showPotentialReset(c.getPlayer().getId(), true, cube.getItemId(), eq.getItemId()));
            c.getSession().writeAndFlush(CField.getCubeStart(c.getPlayer(), eq, up2, cube.getItemId(), cube.getQuantity()));
            c.getPlayer().forceReAddItem(eq, MapleInventoryType.EQUIP);
         }
         break;
      case 2711004:
      case 2711006:
      case 2711013:
      case 2711017:
         if (GameConstants.isZero(c.getPlayer().getJob()) && eq == null) {
            eq = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
            eq2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            rand = Randomizer.nextInt(100);
            up = false;
            if (c.getPlayer().getMeso() < (long)GameConstants.getCubeMeso(eq.getItemId())) {
               c.getPlayer().dropMessage(6, "메소가 부족합니다.");
               return;
            }

            if (eq.getState() == 17) {
               if (Randomizer.isSuccess(2, 200)) {
                  eq.setState((byte)18);
                  eq2.setState((byte)18);
                  up = true;
               } else {
                  eq.setState((byte)17);
                  eq2.setState((byte)17);
               }
            } else if (eq.getState() == 18) {
               if (Randomizer.isSuccess(2, 200)) {
                  up = true;
                  eq.setState((byte)19);
                  eq2.setState((byte)19);
               } else {
                  eq.setState((byte)18);
               }
            } else if (eq.getState() == 19) {
               if (Randomizer.isSuccess(1, 200)) {
                  up = true;
                  eq.setState((byte)20);
                  eq2.setState((byte)20);
               } else {
                  eq.setState((byte)19);
               }
            }

            rate = eq.getState() - 16;
            potential1 = potential(eq.getItemId(), rate);
            potential2 = potential(eq.getItemId(), rate != 1 && !Randomizer.isSuccess(2, 200) ? rate - 1 : rate);
            potential3 = eq.getPotential3() == 0 ? 0 : potential(eq.getItemId(), rate != 1 && !Randomizer.isSuccess(1, 200) ? rate - 1 : rate);
            eq.setPotential1(potential1);
            eq.setPotential2(potential2);
            eq.setPotential3(potential3);
            eq2.setPotential1(potential1);
            eq2.setPotential2(potential2);
            eq2.setPotential3(potential3);
            c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(eq.getItemId())), false);
            c.getSession().writeAndFlush(CField.showPotentialReset(c.getPlayer().getId(), true, cube.getItemId(), eq.getItemId()));
            c.getSession().writeAndFlush(CField.getCubeStart(c.getPlayer(), eq, up, cube.getItemId(), cube.getQuantity()));
            c.getPlayer().forceReAddItem(eq, MapleInventoryType.EQUIPPED);
            c.getPlayer().forceReAddItem(eq2, MapleInventoryType.EQUIPPED);
         } else {
            rand2 = Randomizer.nextInt(100);
            up2 = false;
            if (c.getPlayer().getMeso() < (long)GameConstants.getCubeMeso(eq.getItemId())) {
               c.getPlayer().dropMessage(6, "메소가 부족합니다.");
               return;
            }

            if (eq.getState() == 17) {
               if (Randomizer.isSuccess(3, 200)) {
                  eq.setState((byte)18);
                  up2 = true;
               } else {
                  eq.setState((byte)17);
               }
            } else if (eq.getState() == 18) {
               if (Randomizer.isSuccess(2, 200)) {
                  up2 = true;
                  eq.setState((byte)19);
               } else {
                  eq.setState((byte)18);
               }
            } else if (eq.getState() == 19) {
               if (Randomizer.isSuccess(1, 200)) {
                  up2 = true;
                  eq.setState((byte)20);
               } else {
                  eq.setState((byte)19);
               }
            }

            level2 = eq.getState() - 16;
            eq.setPotential1(potential(eq.getItemId(), level2));
            eq.setPotential2(potential(eq.getItemId(), level2 != 1 && !Randomizer.isSuccess(2, 200) ? level2 - 1 : level2));
            eq.setPotential3(eq.getPotential3() == 0 ? 0 : potential(eq.getItemId(), level2 != 1 && !Randomizer.isSuccess(1, 200) ? level2 - 1 : level2));
            c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(eq.getItemId())), false);
            c.getSession().writeAndFlush(CField.showPotentialReset(c.getPlayer().getId(), true, cube.getItemId(), eq.getItemId()));
            c.getSession().writeAndFlush(CField.getCubeStart(c.getPlayer(), eq, up2, cube.getItemId(), cube.getQuantity()));
            c.getPlayer().forceReAddItem(eq, MapleInventoryType.EQUIP);
         }
         break;
      case 2730000:
      case 2730001:
      case 2730002:
      case 2730004:
      case 2730005:
         rand2 = Randomizer.nextInt(100);
         up2 = false;
         if (eq != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
            if (eq.getPotential4() <= 0) {
               c.getPlayer().dropMessage(1, "에디셔널 잠재능력이 부여되지 않았습니다.");
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
               if (GameConstants.isZero(c.getPlayer().getJob())) {
                  Item item2 = c.getPlayer().getInventory(pos < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem((short)-11);
                  Equip eq3 = (Equip)item2;
                  if (eq3 != null) {
                     eq3.setState((byte)(eq.getState() + 32));
                     c.getPlayer().forceReAddItem_NoUpdate(item2, MapleInventoryType.EQUIPPED);
                  }
               }

               up = false;
               level2 = eq.getPotential4() >= 10000 ? eq.getPotential4() / 10000 : eq.getPotential4() / 100;
               if (level2 >= 4) {
                  level2 = 4;
               }

               rate = level2 == 1 ? 3 : 0;
               if (Randomizer.nextInt(100) < rate) {
                  up2 = true;
                  ++level2;
               }

               if (eq.getPotential6() > 0) {
                  eq.setPotential4(potential(eq.getItemId(), level2, true));
                  eq.setPotential5(potential(eq.getItemId(), level2 != 1 && !Randomizer.isSuccess(2, 200) ? level2 - 1 : level2, true));
                  eq.setPotential6(potential(eq.getItemId(), level2 != 1 && !Randomizer.isSuccess(2, 200) ? level2 - 1 : level2, true));
               } else {
                  eq.setPotential4(potential(eq.getItemId(), level2, true));
                  eq.setPotential5(potential(eq.getItemId(), level2 != 1 && !Randomizer.isSuccess(2, 200) ? level2 - 1 : level2, true));
               }

               c.getSession().writeAndFlush(CField.getCubeStart(c.getPlayer(), eq, up2, cube.getItemId(), cube.getQuantity()));
               c.getSession().writeAndFlush(CField.showPotentialReset(c.getPlayer().getId(), true, cube.getItemId(), eq.getItemId()));
               c.getPlayer().forceReAddItem(eq, MapleInventoryType.EQUIP);
               c.getPlayer().gainMeso((long)(-GameConstants.getCubeMeso(eq.getItemId())), false);
            } else {
               c.getPlayer().dropMessage(5, "소비 아이템 여유 공간이 부족하여 잠재능력 재설정을 실패하였습니다.");
            }
         }
      }

      c.getPlayer().removeItem(cube.getItemId(), -1);
   }

   public static void UseGoldenHammer(LittleEndianAccessor rh, MapleClient c) {
      c.getPlayer().vh = false;
      rh.skip(4);
      byte slot = (byte)rh.readInt();
      int itemId = rh.readInt();
      rh.skip(4);
      byte victimslot = (byte)rh.readInt();
      Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short)slot);
      Equip victim = null;
      Equip victim_ = null;
      if (victimslot < 0) {
         victim = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)victimslot);
         if (GameConstants.isZero(c.getPlayer().getJob())) {
            if (victim.getPosition() == -10) {
               victim_ = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            } else {
               victim_ = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
            }
         }
      } else {
         victim = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)victimslot);
      }

      if (victim != null && toUse != null && toUse.getItemId() == itemId && toUse.getQuantity() >= 1) {
         c.getSession().writeAndFlush(CSPacket.ViciousHammer(true, c.getPlayer().vh));
         victim.setViciousHammer((byte)1);
         if (victim_ != null) {
            victim_.setViciousHammer((byte)1);
         }

         if ((itemId == 2470001 || itemId == 2470002) && Randomizer.nextInt(100) > 50) {
            victim.setUpgradeSlots((byte)(victim.getUpgradeSlots() + 1));
            if (victim_ != null) {
               victim_.setUpgradeSlots((byte)(victim.getUpgradeSlots() + 1));
            }

            c.getPlayer().vh = true;
         } else if (itemId == 2470000) {
            victim.setUpgradeSlots((byte)(victim.getUpgradeSlots() + 1));
            if (victim_ != null) {
               victim_.setUpgradeSlots((byte)(victim_.getUpgradeSlots() + 1));
            }

            c.getPlayer().vh = true;
         } else if (itemId == 2470018) {
            victim.setUpgradeSlots((byte)(victim.getUpgradeSlots() + 3));
            if (victim_ != null) {
               victim_.setUpgradeSlots((byte)(victim_.getUpgradeSlots() + 3));
            }

            c.getPlayer().vh = true;
         } else if (itemId == 2470021) {
            victim.setUpgradeSlots((byte)(victim.getUpgradeSlots() + 5));
            if (victim_ != null) {
               victim_.setUpgradeSlots((byte)(victim_.getUpgradeSlots() + 5));
            }

            c.getPlayer().vh = true;
         } else if (itemId == 2470003) {
            victim.setUpgradeSlots((byte)(victim.getUpgradeSlots() + 1));
            if (victim_ != null) {
               victim_.setUpgradeSlots((byte)(victim_.getUpgradeSlots() + 1));
            }

            c.getPlayer().vh = true;
         } else if (itemId == 2470007) {
            victim.setUpgradeSlots((byte)(victim.getUpgradeSlots() + 1));
            if (victim_ != null) {
               victim_.setUpgradeSlots((byte)(victim_.getUpgradeSlots() + 1));
            }

            c.getPlayer().vh = true;
         } else if (itemId == 2470010) {
            victim.setUpgradeSlots((byte)(victim.getUpgradeSlots() + 1));
            if (victim_ != null) {
               victim_.setUpgradeSlots((byte)(victim_.getUpgradeSlots() + 1));
            }

            c.getPlayer().vh = true;
         } else if (itemId == 2470021) {
            victim.setUpgradeSlots((byte)(victim.getUpgradeSlots() + 5));
            if (victim_ != null) {
               victim_.setUpgradeSlots((byte)(victim_.getUpgradeSlots() + 5));
            }

            c.getPlayer().vh = true;
         }

         MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short)slot, (short)1, false);
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, victim));
         if (victim_ != null) {
            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIPPED, victim));
            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIPPED, victim_));
            c.getPlayer().getMap().broadcastMessage(CField.getScrollEffect(c.getPlayer().getId(), Equip.ScrollResult.SUCCESS, false, itemId, victim_.getItemId()));
         }

      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static void Todd(LittleEndianAccessor slea, MapleClient c) {
   }

   public static void returnScrollResult(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      byte type = slea.readByte();
      Equip equip = null;
      Equip zeroequip = null;
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (c.getPlayer().returnscroll == null) {
         c.getPlayer().dropMessage(1, "리턴 스크롤 사용 중 오류가 발생하였습니다.");
      } else {
         equip = c.getPlayer().returnscroll.getPosition() > 0 ? (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(c.getPlayer().returnscroll.getPosition()) : (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(c.getPlayer().returnscroll.getPosition());
         if (equip == null) {
            c.getPlayer().dropMessage(1, "리턴 스크롤 사용 중 오류가 발생하였습니다.");
         } else {
            if (type == 1) {
               if (c.getPlayer().returnscroll.getPosition() > 0) {
                  equip.set(c.getPlayer().returnscroll);
               } else {
                  equip.set(c.getPlayer().returnscroll);
               }
            }

            equip.setFlag(equip.getFlag() - ItemFlag.RETURN_SCROLL.getValue());
            if (GameConstants.isZeroWeapon(c.getPlayer().returnscroll.getItemId())) {
               zeroequip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
               zeroequip.set(c.getPlayer().returnscroll);
               zeroequip.setFlag(equip.getFlag() - ItemFlag.RETURN_SCROLL.getValue());
            }

            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, equip));
            if (zeroequip != null) {
               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, zeroequip));
            }

            if (type == 1) {
               Channel var10000 = c.getSession();
               String var10002 = ii.getName(equip.getItemId());
               var10000.writeAndFlush(CField.getGameMessage(11, "리턴 주문서의 힘으로 " + var10002 + "가 " + ii.getName(c.getPlayer().returnSc) + " 사용 이전 상태로 돌아왔습니다."));
            } else {
               c.getSession().writeAndFlush(CField.getGameMessage(11, "리턴 주문서의 효과가 사라졌습니다."));
            }

            c.getSession().writeAndFlush(CWvsContext.returnEffectModify((Equip)null, 0));
            c.getPlayer().returnscroll = null;
            c.getPlayer().returnSc = 0;
         }
      }
   }

   public static void ArcaneCatalyst(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      int slot = slea.readInt();
      Equip equip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)slot).copy();
      equip.setEquipmentType(equip.getEquipmentType() | 16384);
      equip.setArcLevel(1);
      int totalexp = 0;

      for(int i = 1; i < equip.getArcLevel(); ++i) {
         totalexp += GameConstants.ArcaneNextUpgrade(i);
      }

      totalexp += equip.getArcEXP();
      equip.setArcEXP((int)Math.floor((double)totalexp * 0.8D));
      if (GameConstants.isXenon(c.getPlayer().getJob())) {
         equip.setStr((short)117);
         equip.setDex((short)117);
         equip.setLuk((short)117);
      } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
         equip.setHp((short)4200);
      } else if (GameConstants.isWarrior(c.getPlayer().getJob())) {
         equip.setStr((short)300);
      } else if (GameConstants.isMagician(c.getPlayer().getJob())) {
         equip.setInt((short)300);
      } else if (!GameConstants.isArcher(c.getPlayer().getJob()) && !GameConstants.isCaptain(c.getPlayer().getJob()) && !GameConstants.isMechanic(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
         if (GameConstants.isThief(c.getPlayer().getJob())) {
            equip.setLuk((short)300);
         } else if (GameConstants.isPirate(c.getPlayer().getJob())) {
            equip.setStr((short)300);
         }
      } else {
         equip.setDex((short)300);
      }

      c.getSession().writeAndFlush(CWvsContext.ArcaneCatalyst(equip, slot));
   }

   public static void ArcaneCatalyst2(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      int slot = slea.readInt();
      Equip equip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)slot);
      if ((equip.getEquipmentType() & 16384) == 0) {
         equip.setArc((short)30);
         int totalexp = 0;

         for(int i = 1; i < equip.getArcLevel(); ++i) {
            totalexp += GameConstants.ArcaneNextUpgrade(i);
         }

         totalexp += equip.getArcEXP();
         equip.setArcEXP((int)Math.floor((double)totalexp * 0.8D));
         equip.setArcLevel(1);
         if (GameConstants.isXenon(c.getPlayer().getJob())) {
            equip.setStr((short)117);
            equip.setDex((short)117);
            equip.setLuk((short)117);
         } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
            equip.setHp((short)4200);
         } else if (GameConstants.isWarrior(c.getPlayer().getJob())) {
            equip.setStr((short)300);
         } else if (GameConstants.isMagician(c.getPlayer().getJob())) {
            equip.setInt((short)300);
         } else if (!GameConstants.isArcher(c.getPlayer().getJob()) && !GameConstants.isCaptain(c.getPlayer().getJob()) && !GameConstants.isMechanic(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
            if (GameConstants.isThief(c.getPlayer().getJob())) {
               equip.setLuk((short)300);
            } else if (GameConstants.isPirate(c.getPlayer().getJob())) {
               equip.setStr((short)300);
            }
         } else {
            equip.setDex((short)300);
         }

         equip.setEquipmentType(equip.getEquipmentType() | 16384);
      } else {
         equip.setEquipmentType(equip.getEquipmentType() - 16384);
      }

      c.getSession().writeAndFlush(CWvsContext.ArcaneCatalyst2(equip));
      c.getPlayer().removeItem(2535000, -1);
      c.getPlayer().forceReAddItem(equip, MapleInventoryType.EQUIP);
   }

   public static void ArcaneCatalyst3(LittleEndianAccessor slea, MapleClient c) {
      int slot = slea.readInt();
      Equip equip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)slot).copy();
      equip.setEquipmentType(equip.getEquipmentType() - 16384);
      c.getSession().writeAndFlush(CWvsContext.ArcaneCatalyst(equip, slot));
   }

   public static void ArcaneCatalyst4(LittleEndianAccessor slea, MapleClient c) {
      int slot = slea.readInt();
      Equip equip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)slot);
      equip.setEquipmentType(equip.getEquipmentType() - 16384);
      c.getSession().writeAndFlush(CWvsContext.ArcaneCatalyst2(equip));
      c.getPlayer().forceReAddItem(equip, MapleInventoryType.EQUIP);
   }

   public static void ReturnSynthesizing(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      int scrollId = slea.readInt();
      slea.skip(4);
      int eqpId = slea.readInt();
      int eqpslot = slea.readInt();
      Equip equip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)eqpslot);
      if (equip.getItemId() == eqpId) {
         equip.setMoru(0);
         c.getPlayer().forceReAddItem(equip, MapleInventoryType.EQUIP);
         StringBuilder msg = new StringBuilder("[");
         msg.append(MapleItemInformationProvider.getInstance().getName(equip.getItemId()));
         msg.append("]의 외형이 원래대로 복구되었습니다.");
         c.getSession().writeAndFlush(CWvsContext.showPopupMessage(msg.toString()));
         c.getPlayer().gainItem(scrollId, -1);
      }

   }

   public static void blackRebirthResult(LittleEndianAccessor slea, MapleClient c) {
      int result = slea.readInt();
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Equip eq = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(c.getPlayer().blackRebirthScroll.getPosition());
      if (eq == null) {
         eq = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(c.getPlayer().blackRebirthScroll.getPosition());
         if (eq == null) {
            return;
         }
      }

      Equip zeroequip = null;
      if (eq.getPosition() == -11) {
         zeroequip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
      } else if (eq.getPosition() == -10) {
         zeroequip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
      }

      if (result == 2) {
         eq.resetRebirth(ii.getReqLevel(eq.getItemId()));
         int[] rebirth = new int[4];
         String fire = String.valueOf(c.getPlayer().blackRebirth);
         Equip ordinary = (Equip)MapleItemInformationProvider.getInstance().getEquipById(eq.getItemId(), false);
         Equip ordinary2 = null;
         int ordinaryPad2 = 0;
         int ordinaryMad2 = 0;
         if (zeroequip != null) {
            zeroequip.resetRebirth(ii.getReqLevel(zeroequip.getItemId()));
            ordinary2 = (Equip)MapleItemInformationProvider.getInstance().getEquipById(zeroequip.getItemId(), false);
            ordinaryPad2 = ordinary2.getWatk() > 0 ? ordinary2.getWatk() : ordinary2.getMatk();
            ordinaryMad2 = ordinary2.getMatk() > 0 ? ordinary2.getMatk() : ordinary2.getWatk();
         }

         int ordinaryPad3 = ordinary.getWatk() > 0 ? ordinary.getWatk() : ordinary.getMatk();
         int ordinaryMad3 = ordinary.getMatk() > 0 ? ordinary.getMatk() : ordinary.getWatk();
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

         eq.setFire(c.getPlayer().blackRebirth);
         if (zeroequip != null) {
            zeroequip.setFire(c.getPlayer().blackRebirth);
         }

         for(int i = 0; i < rebirth.length; ++i) {
            int value = rebirth[i] - rebirth[i] / 10 * 10;
            eq.setFireOption(rebirth[i] / 10, ii.getReqLevel(eq.getItemId()), value, ordinaryPad3, ordinaryMad3);
            if (zeroequip != null && ordinaryPad2 != 0) {
               zeroequip.setFireOption(rebirth[i] / 10, ii.getReqLevel(zeroequip.getItemId()), value, ordinaryPad2, ordinaryMad2);
            }
         }

         c.getPlayer().forceReAddItem(eq, MapleInventoryType.EQUIP);
         if (zeroequip != null) {
            c.getPlayer().forceReAddItem(zeroequip, MapleInventoryType.EQUIP);
         }
      } else if (result == 3) {
         Item scroll = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(c.getPlayer().blackRebirthPos);
         if (scroll != null) {
            Equip neweqs = (Equip)eq.copy();
            neweqs.resetRebirth(ii.getReqLevel(neweqs.getItemId()));
            neweqs.setFire(neweqs.newRebirth(ii.getReqLevel(neweqs.getItemId()), scroll.getItemId(), true));
            long newRebirth = neweqs.getFire();
            c.getSession().writeAndFlush(CWvsContext.useBlackRebirthScroll(eq, scroll, newRebirth, false));
            MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(scroll.getItemId()), scroll.getPosition(), (short)1, false, false);
            c.getSession().writeAndFlush(CWvsContext.blackRebirthResult(true, eq.getFire(), eq));
            c.getSession().writeAndFlush(CWvsContext.blackRebirthResult(false, newRebirth, neweqs));
            c.getPlayer().blackRebirth = newRebirth;
            c.getPlayer().blackRebirthScroll = (Equip)eq.copy();
         }
      }

      if (result == 1 || result == 2) {
         c.getSession().writeAndFlush(CWvsContext.useBlackRebirthScroll(eq, (Item)null, 0L, true));
      }

      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static void UseCirculator(LittleEndianAccessor slea, MapleClient c) {
      int itemId = slea.readInt();
      int slot = slea.readInt();
      Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short)slot);
      if (item.getItemId() == itemId) {
         List<InnerSkillValueHolder> newValues = new LinkedList();
         InnerSkillValueHolder ivholder = null;
         InnerSkillValueHolder ivholder2 = null;
         int nowrank = -1;
         Iterator var9;
         InnerSkillValueHolder isvh;
         InnerSkillValueHolder ivholder3;
         byte nowrank;
         switch(itemId) {
         case 2702002:
            nowrank = 2;
            var9 = c.getPlayer().getInnerSkills().iterator();

            for(; var9.hasNext(); c.getPlayer().changeSkillLevel_Inner(SkillFactory.getSkill(isvh.getSkillId()), 0, (byte)0)) {
               isvh = (InnerSkillValueHolder)var9.next();
               if (ivholder == null) {
                  for(ivholder = InnerAbillity.getInstance().renewSkill(nowrank, false); isvh.getSkillId() == ivholder.getSkillId(); ivholder = InnerAbillity.getInstance().renewSkill(nowrank, false)) {
                  }

                  newValues.add(ivholder);
               } else if (ivholder2 == null) {
                  for(ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank(), false); isvh.getSkillId() == ivholder2.getSkillId() || ivholder.getSkillId() == ivholder2.getSkillId(); ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank(), false)) {
                  }

                  newValues.add(ivholder2);
               } else {
                  for(ivholder3 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank(), false); isvh.getSkillId() == ivholder3.getSkillId() || ivholder.getSkillId() == ivholder3.getSkillId() || ivholder2.getSkillId() == ivholder3.getSkillId(); ivholder3 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank(), false)) {
                  }

                  newValues.add(ivholder3);
               }
            }
            break;
         case 2702003:
         case 2702004:
            if (c.getPlayer().getInnerSkills().size() > 0) {
               nowrank = ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(0)).getRank();
            }

            var9 = c.getPlayer().getInnerSkills().iterator();

            while(var9.hasNext()) {
               isvh = (InnerSkillValueHolder)var9.next();
               newValues.add(InnerAbillity.getInstance().renewLevel(nowrank, isvh.getSkillId()));
               c.getPlayer().changeSkillLevel_Inner(SkillFactory.getSkill(isvh.getSkillId()), 0, (byte)0);
            }
         case 2702005:
         default:
            break;
         case 2702006:
            nowrank = 3;

            for(var9 = c.getPlayer().getInnerSkills().iterator(); var9.hasNext(); c.getPlayer().changeSkillLevel_Inner(SkillFactory.getSkill(isvh.getSkillId()), 0, (byte)0)) {
               isvh = (InnerSkillValueHolder)var9.next();
               if (ivholder == null) {
                  for(ivholder = InnerAbillity.getInstance().renewSkill(nowrank, false); isvh.getSkillId() == ivholder.getSkillId(); ivholder = InnerAbillity.getInstance().renewSkill(nowrank, false)) {
                  }

                  newValues.add(ivholder);
               } else if (ivholder2 == null) {
                  for(ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank(), false); isvh.getSkillId() == ivholder2.getSkillId() || ivholder.getSkillId() == ivholder2.getSkillId(); ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank(), false)) {
                  }

                  newValues.add(ivholder2);
               } else {
                  for(ivholder3 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank(), false); isvh.getSkillId() == ivholder3.getSkillId() || ivholder.getSkillId() == ivholder3.getSkillId() || ivholder2.getSkillId() == ivholder3.getSkillId(); ivholder3 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank(), false)) {
                  }

                  newValues.add(ivholder3);
               }
            }
         }

         if (newValues.size() == 3) {
            c.getPlayer().getInnerSkills().clear();
            var9 = newValues.iterator();

            while(var9.hasNext()) {
               isvh = (InnerSkillValueHolder)var9.next();
               c.getPlayer().getInnerSkills().add(isvh);
               c.getPlayer().changeSkillLevel_Inner(SkillFactory.getSkill(isvh.getSkillId()), isvh.getSkillLevel(), isvh.getSkillLevel());
               c.getPlayer().getClient().getSession().writeAndFlush(CField.updateInnerAbility(isvh, c.getPlayer().getInnerSkills().size(), c.getPlayer().getInnerSkills().size() == 3));
            }

            c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short)slot, (short)1, false);
         }
      }

   }
}
