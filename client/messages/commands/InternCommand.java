package client.messages.commands;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.messages.CommandProcessorUtil;
import constants.ServerConstants;
import handling.auction.AuctionServer;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.farm.FarmServer;
import handling.world.CheaterData;
import handling.world.World;
import java.awt.Point;
import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.EventInstanceManager;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.enchant.EnchantFlag;
import server.enchant.EquipmentEnchant;
import server.enchant.StarForceStats;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleReactor;
import server.quest.MapleQuest;
import tools.Pair;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class InternCommand {
   public static ServerConstants.PlayerGMRank getPlayerLevelRequired() {
      return ServerConstants.PlayerGMRank.INTERN;
   }

   public static class 이메일 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         ServerConstants.mailid = splitted[1];
         ServerConstants.mailpw = splitted[2];
         c.getPlayer().dropMessage(5, "이메일 아이디 : " + ServerConstants.mailid);
         c.getPlayer().dropMessage(5, "이메일 비밀번호 : " + ServerConstants.mailpw);
         c.getPlayer().dropMessage(5, "이메일 인증 아이디 변경완료.");
         return 1;
      }
   }

   public static class 총온라인 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         Iterator var3 = ChannelServer.getAllInstances().iterator();

         while(var3.hasNext()) {
            ChannelServer cs = (ChannelServer)var3.next();
            MapleCharacter var10000 = c.getPlayer();
            int var10002 = cs.getChannel();
            var10000.dropMessage(6, var10002 + "채널 : " + cs.getPlayerStorage().getOnlinePlayers(true));
         }

         return 1;
      }
   }

   public static class 사냥유저 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         StringBuilder sb = new StringBuilder();
         int i = 0;
         Iterator var5 = World.getAllCharacters().iterator();

         while(var5.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var5.next();
            if (chr.getMap().isSpawnPoint()) {
               sb.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
               sb.append(",");
               ++i;
            }
         }

         c.getPlayer().dropMessage(6, sb.toString());
         c.getPlayer().dropMessage(6, "현재 사냥중인 유저 수는 : " + i + "명 입니다.");
         return 1;
      }
   }

   public static class 킬올 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         MapleMap map = c.getPlayer().getMap();
         double range = Double.POSITIVE_INFINITY;
         if (splitted.length > 1) {
            int irange = Integer.parseInt(splitted[1]);
            if (splitted.length <= 2) {
               range = (double)(irange * irange);
            } else {
               map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
            }
         }

         byte animation = 1;
         if (map == null) {
            c.getPlayer().dropMessage(6, "Map does not exist");
            return 0;
         } else {
            Iterator var7 = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER)).iterator();

            while(true) {
               MapleMonster mob;
               do {
                  if (!var7.hasNext()) {
                     return 1;
                  }

                  MapleMapObject monstermo = (MapleMapObject)var7.next();
                  mob = (MapleMonster)monstermo;
               } while(mob.getStats().isBoss() && !mob.getStats().isPartyBonus() && !c.getPlayer().isGM());

               map.killMonster(mob, c.getPlayer(), true, false, animation);
            }
         }
      }
   }

   public static class 스타포스해제 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         Equip item = null;
         item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(Short.parseShort(splitted[1]));
         if (item == null) {
            c.getPlayer().dropMessage(1, "해당 위치에 아이템이 존재하지 않습니다.");
         } else {
            while(true) {
               if (item.getEnhance() <= 0) {
                  c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIP);
                  break;
               }

               item.setEnhance((byte)(item.getEnhance() - 1));
               StarForceStats stats = EquipmentEnchant.starForceStats(item);
               Iterator var5 = stats.getStats().iterator();

               while(var5.hasNext()) {
                  Pair<EnchantFlag, Integer> stat = (Pair)var5.next();
                  short matk;
                  if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                     matk = item.getWatk();
                     if (matk / 50 != (item.getWatk() - (Integer)stat.right) / 50) {
                        item.setWatk((short)(item.getWatk() - (Integer)stat.right + 1));
                     } else {
                        item.setWatk((short)(item.getWatk() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                     matk = item.getMatk();
                     if (matk / 50 != (item.getMatk() - (Integer)stat.right) / 50) {
                        item.setMatk((short)(item.getMatk() - (Integer)stat.right + 1));
                     } else {
                        item.setMatk((short)(item.getMatk() - (Integer)stat.right));
                     }
                  }

                  if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                     item.setStr((short)(item.getStr() - (Integer)stat.right));
                  }

                  if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                     item.setDex((short)(item.getDex() - (Integer)stat.right));
                  }

                  if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                     item.setInt((short)(item.getInt() - (Integer)stat.right));
                  }

                  if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                     item.setLuk((short)(item.getLuk() - (Integer)stat.right));
                  }

                  if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                     item.setWdef((short)(item.getWdef() - (Integer)stat.right));
                  }

                  if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                     item.setMdef((short)(item.getMdef() - (Integer)stat.right));
                  }

                  if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                     item.setHp((short)(item.getHp() - (Integer)stat.right));
                  }

                  if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                     item.setMp((short)(item.getMp() - (Integer)stat.right));
                  }

                  if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                     item.setAcc((short)(item.getAcc() - (Integer)stat.right));
                  }

                  if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                     item.setAvoid((short)(item.getAvoid() - (Integer)stat.right));
                  }
               }
            }
         }

         return 1;
      }
   }

   public static class 스타포스 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length < 2) {
            c.getPlayer().dropMessage(5, "!스타포스 <몇성?> - 장비 탭 첫번째 아이템이 강화됩니다.");
         } else {
            Equip nEquip = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)1);
            if (nEquip != null) {
               while(nEquip.getEnhance() < Integer.parseInt(splitted[1])) {
                  StarForceStats statz = EquipmentEnchant.starForceStats(nEquip);
                  nEquip.setEnhance((byte)(nEquip.getEnhance() + 1));
                  Iterator var5 = statz.getStats().iterator();

                  while(var5.hasNext()) {
                     Pair<EnchantFlag, Integer> stat = (Pair)var5.next();
                     if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setWatk((short)(nEquip.getWatk() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setMatk((short)(nEquip.getMatk() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setStr((short)(nEquip.getStr() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setDex((short)(nEquip.getDex() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setInt((short)(nEquip.getInt() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setLuk((short)(nEquip.getLuk() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setWdef((short)(nEquip.getWdef() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setMdef((short)(nEquip.getMdef() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setHp((short)(nEquip.getHp() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setMp((short)(nEquip.getMp() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setAcc((short)(nEquip.getAcc() + (Integer)stat.right));
                     }

                     if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                        nEquip.setAvoid((short)(nEquip.getAvoid() + (Integer)stat.right));
                     }
                  }
               }
            }
         }

         return 1;
      }
   }

   public static class 찾기 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length == 1) {
            c.getPlayer().dropMessage(6, splitted[0] + ": <엔피시> <몹> <아이템> <맵> <스킬> <퀘스트> <스크립트>");
         } else if (splitted.length == 2) {
            c.getPlayer().dropMessage(6, "검색어를 입력하지 않았습니다.");
         } else {
            String type = splitted[1];
            String search = StringUtil.joinStringFrom(splitted, 2);
            MapleData data = null;
            MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/String.wz"));
            c.getPlayer().dropMessage(6, "<<타입: " + type + " | 검색어: " + search + ">>");
            ArrayList retScripts;
            LinkedList mapPairList;
            Iterator var9;
            MapleData mobIdData;
            Pair mobPair;
            String singleRetMob;
            if (type.equalsIgnoreCase("엔피시")) {
               retScripts = new ArrayList();
               data = dataProvider.getData("Npc.img");
               mapPairList = new LinkedList();
               var9 = data.getChildren().iterator();

               while(var9.hasNext()) {
                  mobIdData = (MapleData)var9.next();
                  mapPairList.add(new Pair(Integer.parseInt(mobIdData.getName()), MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
               }

               var9 = mapPairList.iterator();

               while(var9.hasNext()) {
                  mobPair = (Pair)var9.next();
                  if (((String)mobPair.getRight()).toLowerCase().contains(search.toLowerCase())) {
                     retScripts.add(mobPair.getLeft() + " - " + (String)mobPair.getRight());
                  }
               }

               if (retScripts != null && retScripts.size() > 0) {
                  var9 = retScripts.iterator();

                  while(var9.hasNext()) {
                     singleRetMob = (String)var9.next();
                     c.getPlayer().dropMessage(6, singleRetMob);
                  }
               } else {
                  c.getPlayer().dropMessage(6, "입력한 엔피시코드를 찾을 수 없습니다.");
               }
            } else if (type.equalsIgnoreCase("맵")) {
               retScripts = new ArrayList();
               data = dataProvider.getData("Map.img");
               mapPairList = new LinkedList();
               var9 = data.getChildren().iterator();

               while(var9.hasNext()) {
                  mobIdData = (MapleData)var9.next();
                  Iterator var11 = mobIdData.getChildren().iterator();

                  while(var11.hasNext()) {
                     MapleData mapIdData = (MapleData)var11.next();
                     Integer var10003 = Integer.parseInt(mapIdData.getName());
                     String var10004 = MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "NO-NAME");
                     mapPairList.add(new Pair(var10003, var10004 + " - " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "NO-NAME")));
                  }
               }

               var9 = mapPairList.iterator();

               while(var9.hasNext()) {
                  mobPair = (Pair)var9.next();
                  if (((String)mobPair.getRight()).toLowerCase().contains(search.toLowerCase())) {
                     retScripts.add(mobPair.getLeft() + " - " + (String)mobPair.getRight());
                  }
               }

               if (retScripts != null && retScripts.size() > 0) {
                  var9 = retScripts.iterator();

                  while(var9.hasNext()) {
                     singleRetMob = (String)var9.next();
                     c.getPlayer().dropMessage(6, singleRetMob);
                  }
               } else {
                  c.getPlayer().dropMessage(6, "입력한 맵코드를 찾을 수 없습니다.");
               }
            } else if (type.equalsIgnoreCase("몹")) {
               retScripts = new ArrayList();
               data = dataProvider.getData("Mob.img");
               mapPairList = new LinkedList();
               var9 = data.getChildren().iterator();

               while(var9.hasNext()) {
                  mobIdData = (MapleData)var9.next();
                  mapPairList.add(new Pair(Integer.parseInt(mobIdData.getName()), MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
               }

               var9 = mapPairList.iterator();

               while(var9.hasNext()) {
                  mobPair = (Pair)var9.next();
                  if (((String)mobPair.getRight()).toLowerCase().contains(search.toLowerCase())) {
                     retScripts.add(mobPair.getLeft() + " - " + (String)mobPair.getRight());
                  }
               }

               if (retScripts != null && retScripts.size() > 0) {
                  var9 = retScripts.iterator();

                  while(var9.hasNext()) {
                     singleRetMob = (String)var9.next();
                     c.getPlayer().dropMessage(6, singleRetMob);
                  }
               } else {
                  c.getPlayer().dropMessage(6, "입력한 몹코드를 찾을 수 없습니다.");
               }
            } else {
               Iterator var13;
               String singleRetSkill;
               if (type.equalsIgnoreCase("아이템")) {
                  retScripts = new ArrayList();
                  var13 = MapleItemInformationProvider.getInstance().getAllItems().iterator();

                  while(var13.hasNext()) {
                     Pair<Integer, String> itemPair = (Pair)var13.next();
                     if (((String)itemPair.getRight()).toLowerCase().contains(search.toLowerCase())) {
                        retScripts.add(itemPair.getLeft() + " - " + (String)itemPair.getRight());
                     }
                  }

                  if (retScripts != null && retScripts.size() > 0) {
                     var13 = retScripts.iterator();

                     while(var13.hasNext()) {
                        singleRetSkill = (String)var13.next();
                        c.getPlayer().dropMessage(6, singleRetSkill);
                     }
                  } else {
                     c.getPlayer().dropMessage(6, "입력한 아이템코드를 찾을 수 없습니다.");
                  }
               } else {
                  int var10001;
                  if (type.equalsIgnoreCase("퀘스트")) {
                     retScripts = new ArrayList();
                     var13 = MapleQuest.getAllInstances().iterator();

                     while(var13.hasNext()) {
                        MapleQuest itemPair = (MapleQuest)var13.next();
                        if (itemPair.getName().length() > 0 && itemPair.getName().toLowerCase().contains(search.toLowerCase())) {
                           var10001 = itemPair.getId();
                           retScripts.add(var10001 + " - " + itemPair.getName());
                        }
                     }

                     if (retScripts != null && retScripts.size() > 0) {
                        var13 = retScripts.iterator();

                        while(var13.hasNext()) {
                           singleRetSkill = (String)var13.next();
                           c.getPlayer().dropMessage(6, singleRetSkill);
                        }
                     } else {
                        c.getPlayer().dropMessage(6, "입력한 퀘스트코드를 찾을 수 없습니다.");
                     }
                  } else if (type.equalsIgnoreCase("스킬")) {
                     retScripts = new ArrayList();
                     var13 = SkillFactory.getAllSkills().iterator();

                     while(var13.hasNext()) {
                        Skill skil = (Skill)var13.next();
                        if (skil.getName() != null && skil.getName().toLowerCase().contains(search.toLowerCase())) {
                           var10001 = skil.getId();
                           retScripts.add(var10001 + " - " + skil.getName());
                        }
                     }

                     if (retScripts != null && retScripts.size() > 0) {
                        var13 = retScripts.iterator();

                        while(var13.hasNext()) {
                           singleRetSkill = (String)var13.next();
                           c.getPlayer().dropMessage(6, singleRetSkill);
                        }
                     } else {
                        c.getPlayer().dropMessage(6, "입력한 스킬코드를 찾을 수 없습니다.");
                     }
                  } else if (type.equalsIgnoreCase("스크립트")) {
                     retScripts = new ArrayList();
                     var13 = MapleLifeFactory.getNpcScripts().entrySet().iterator();

                     while(var13.hasNext()) {
                        Entry<Integer, String> scri = (Entry)var13.next();
                        if (((String)scri.getValue()).toLowerCase().contains(search.toLowerCase())) {
                           retScripts.add(scri.getKey() + " - " + (String)scri.getValue());
                        }
                     }

                     if (retScripts != null && retScripts.size() > 0) {
                        var13 = retScripts.iterator();

                        while(var13.hasNext()) {
                           singleRetSkill = (String)var13.next();
                           c.getPlayer().dropMessage(6, singleRetSkill);
                        }
                     } else {
                        c.getPlayer().dropMessage(6, "입력한 스크립트를 찾을 수 없습니다.");
                     }
                  } else {
                     c.getPlayer().dropMessage(6, "검색을 할 수 없습니다, 검색타입을 확인후 다시시도 해주세요.");
                  }
               }
            }
         }

         return 0;
      }
   }

   public static class 검색 extends InternCommand.찾기 {
   }

   public static class 편지 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length < 3) {
            c.getPlayer().dropMessage(6, "syntax: !letter <color (green/red)> <word>");
            return 0;
         } else {
            int start;
            int nstart;
            if (splitted[1].equalsIgnoreCase("green")) {
               start = 3991026;
               nstart = 3990019;
            } else {
               if (!splitted[1].equalsIgnoreCase("red")) {
                  c.getPlayer().dropMessage(6, "Unknown color!");
                  return 0;
               }

               start = 3991000;
               nstart = 3990009;
            }

            String splitString = StringUtil.joinStringFrom(splitted, 2);
            List<Integer> chars = new ArrayList();
            splitString = splitString.toUpperCase();

            for(int i = 0; i < splitString.length(); ++i) {
               char chr = splitString.charAt(i);
               if (chr == ' ') {
                  chars.add(-1);
               } else if (chr >= 'A' && chr <= 'Z') {
                  chars.add(Integer.valueOf(chr));
               } else if (chr >= '0' && chr <= '9') {
                  chars.add(chr + 200);
               }
            }

            int w = true;
            int dStart = c.getPlayer().getPosition().x - splitString.length() / 2 * 32;
            Iterator var9 = chars.iterator();

            while(var9.hasNext()) {
               Integer integer = (Integer)var9.next();
               if (integer == -1) {
                  dStart += 32;
               } else {
                  int val;
                  Item item;
                  if (integer < 200) {
                     val = start + integer - 65;
                     item = new Item(val, (short)0, (short)1);
                     c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, new Point(dStart, c.getPlayer().getPosition().y), false, false);
                     dStart += 32;
                  } else if (integer >= 200 && integer <= 300) {
                     val = nstart + integer - 48 - 200;
                     item = new Item(val, (short)0, (short)1);
                     c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, new Point(dStart, c.getPlayer().getPosition().y), false, false);
                     dStart += 32;
                  }
               }
            }

            return 1;
         }
      }
   }

   public static class 말 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (!c.getPlayer().isGM()) {
               sb.append("Intern ");
            }

            sb.append(c.getPlayer().getName());
            sb.append("] ");
            sb.append(StringUtil.joinStringFrom(splitted, 1));
            World.Broadcast.broadcastMessage(CWvsContext.serverNotice(c.getPlayer().isGM() ? 6 : 5, c.getPlayer().getName(), sb.toString()));
            return 1;
         } else {
            c.getPlayer().dropMessage(6, "Syntax: say <message>");
            return 0;
         }
      }
   }

   public static class 감옥 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length < 3) {
            c.getPlayer().dropMessage(6, "jail [name] [minutes, 0 = forever]");
            return 0;
         } else {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int minutes = Math.max(0, Integer.parseInt(splitted[2]));
            if (victim != null && c.getPlayer().getGMLevel() >= victim.getGMLevel()) {
               MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(180000002);
               victim.getQuestNAdd(MapleQuest.getInstance(123456)).setCustomData(String.valueOf(minutes * 60));
               victim.changeMap(target, target.getPortal(0));
               return 1;
            } else {
               c.getPlayer().dropMessage(6, "Please be on their channel.");
               return 0;
            }
         }
      }
   }

   public static class 워프 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
         if (victim != null && c.getPlayer().getGMLevel() >= victim.getGMLevel() && !victim.inPVP() && !c.getPlayer().inPVP()) {
            if (splitted.length == 2) {
               c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getTruePosition()));
            } else {
               MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[2]));
               if (target == null) {
                  c.getPlayer().dropMessage(6, "맵이 존재하지 않습니다.");
                  return 0;
               }

               MaplePortal targetPortal = null;
               if (splitted.length > 3) {
                  try {
                     targetPortal = target.getPortal(Integer.parseInt(splitted[3]));
                  } catch (IndexOutOfBoundsException var11) {
                     c.getPlayer().dropMessage(5, "Invalid portal selected.");
                  } catch (NumberFormatException var12) {
                  }
               }

               if (targetPortal == null) {
                  targetPortal = target.getPortal(0);
               }

               victim.changeMap(target, targetPortal);
            }
         } else {
            try {
               victim = c.getPlayer();
               int ch = World.Find.findChannel(splitted[1]);
               MapleMap target;
               if (ch < 0) {
                  target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
                  if (target == null) {
                     c.getPlayer().dropMessage(6, "맵이 존재하지 않습니다.");
                     return 0;
                  }

                  MaplePortal targetPortal = null;
                  if (splitted.length > 2) {
                     try {
                        targetPortal = target.getPortal(Integer.parseInt(splitted[2]));
                     } catch (IndexOutOfBoundsException var8) {
                        c.getPlayer().dropMessage(5, "Invalid portal selected.");
                     } catch (NumberFormatException var9) {
                     }
                  }

                  if (targetPortal == null) {
                     targetPortal = target.getPortal(0);
                  }

                  c.getPlayer().changeMap(target, targetPortal);
               } else {
                  victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
                  c.getPlayer().dropMessage(6, "채널을 변경하였습니다. 잠시만 기달려주세요.");
                  if (victim.getMapId() != c.getPlayer().getMapId()) {
                     target = c.getChannelServer().getMapFactory().getMap(victim.getMapId());
                     c.getPlayer().changeMap(target, target.findClosestPortal(victim.getTruePosition()));
                  }

                  c.getPlayer().changeChannel(ch);
               }
            } catch (Exception var10) {
               c.getPlayer().dropMessage(6, "Something went wrong " + var10.getMessage());
               return 0;
            }
         }

         return 1;
      }
   }

   public static class 채널맵 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length >= 3) {
            c.getPlayer().changeChannelMap(Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]));
         } else {
            c.getPlayer().dropMessage(1, "올바른 값을 입력해주세요.");
         }

         return 1;
      }
   }

   public static class 소환 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
         if (victim != null) {
            if (c.getPlayer().inPVP() || !c.getPlayer().isGM() && victim.isGM()) {
               c.getPlayer().dropMessage(5, "잠시후에 다시시도 해주세요.");
               return 0;
            }

            victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestPortal(c.getPlayer().getTruePosition()));
         } else {
            int ch = World.Find.findChannel(splitted[1]);
            if (ch < 0) {
               c.getPlayer().dropMessage(5, "캐릭터를 찾을 수 없습니다.");
               return 0;
            }

            victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim == null || victim.inPVP()) {
               c.getPlayer().dropMessage(5, "잠시후에 다시시도 해주세요.");
               return 0;
            }

            c.getPlayer().dropMessage(5, "채널을 변경하여 소환합니다.");
            victim.changeChannel(c.getChannel());
         }

         return 1;
      }
   }

   public static class 시계 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().getMap().broadcastMessage(CField.getClock(CommandProcessorUtil.getOptionalIntArg(splitted, 1, 60)));
         return 1;
      }
   }

   public static class 좌표 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         Point pos = c.getPlayer().getPosition();
         MapleCharacter var10000 = c.getPlayer();
         int var10002 = pos.x;
         var10000.dropMessage(6, "X: " + var10002 + " | Y: " + pos.y + " | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getFH() + " | MapId: " + c.getPlayer().getMapId());
         return 1;
      }
   }

   public static class 포탈정보 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         Iterator var3 = c.getPlayer().getMap().getPortals().iterator();

         while(var3.hasNext()) {
            MaplePortal portal = (MaplePortal)var3.next();
            MapleCharacter var10000 = c.getPlayer();
            int var10002 = portal.getId();
            var10000.dropMessage(5, "Portal: ID: " + var10002 + " script: " + portal.getScriptName() + " name: " + portal.getName() + " pos: " + portal.getPosition().x + "," + portal.getPosition().y + " target: " + portal.getTargetMapId() + " / " + portal.getTarget());
         }

         return 0;
      }
   }

   public static class 리엑터정보 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         Iterator var3 = c.getPlayer().getMap().getAllReactorsThreadsafe().iterator();

         while(var3.hasNext()) {
            MapleMapObject reactor1l = (MapleMapObject)var3.next();
            MapleReactor reactor2l = (MapleReactor)reactor1l;
            MapleCharacter var10000 = c.getPlayer();
            int var10002 = reactor2l.getObjectId();
            var10000.dropMessage(5, "Reactor: oID: " + var10002 + " reactorID: " + reactor2l.getReactorId() + " Position: " + reactor2l.getPosition().toString() + " State: " + reactor2l.getState() + " Name: " + reactor2l.getName());
         }

         return 0;
      }
   }

   public static class 엔피시정보 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         Iterator var3 = c.getPlayer().getMap().getAllNPCsThreadsafe().iterator();

         while(var3.hasNext()) {
            MapleNPC reactor1l = (MapleNPC)var3.next();
            MapleCharacter var10000 = c.getPlayer();
            int var10002 = reactor1l.getObjectId();
            var10000.dropMessage(5, "NPC: oID: " + var10002 + " npcID: " + reactor1l.getId() + " Position: " + reactor1l.getPosition().toString() + " Name: " + reactor1l.getName());
         }

         return 0;
      }
   }

   public static class 몹디버그 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         MapleMap map = c.getPlayer().getMap();
         double range = Double.POSITIVE_INFINITY;
         if (splitted.length > 1) {
            int irange = Integer.parseInt(splitted[1]);
            if (splitted.length <= 2) {
               range = (double)(irange * irange);
            } else {
               map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
            }
         }

         if (map == null) {
            c.getPlayer().dropMessage(6, "Map does not exist");
            return 0;
         } else {
            Iterator var9 = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER)).iterator();

            while(var9.hasNext()) {
               MapleMapObject monstermo = (MapleMapObject)var9.next();
               MapleMonster mob = (MapleMonster)monstermo;
               c.getPlayer().dropMessage(6, "Monster " + mob.toString());
            }

            return 1;
         }
      }
   }

   public static class EventInstance extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (c.getPlayer().getEventInstance() == null) {
            c.getPlayer().dropMessage(5, "none");
         } else {
            EventInstanceManager eim = c.getPlayer().getEventInstance();
            MapleCharacter var10000 = c.getPlayer();
            String var10002 = eim.getName();
            var10000.dropMessage(5, "Event " + var10002 + ", charSize: " + eim.getPlayers().size() + ", dcedSize: " + eim.getDisconnected().size() + ", mobSize: " + eim.getMobs().size() + ", eventManager: " + eim.getEventManager().getName() + ", timeLeft: " + eim.getTimeLeft() + ", iprops: " + eim.getProperties().toString() + ", eprops: " + eim.getEventManager().getProperties().toString());
         }

         return 1;
      }
   }

   public static class 업타임 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         MapleCharacter var10000 = c.getPlayer();
         long var10002 = ChannelServer.serverStartTime;
         var10000.dropMessage(6, "Server has been up for " + StringUtil.getReadableMillis(var10002, System.currentTimeMillis()));
         return 1;
      }
   }

   public static class 드롭삭제 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().dropMessage(5, "Cleared " + c.getPlayer().getMap().getNumItems() + " drops");
         c.getPlayer().getMap().removeDrops();
         return 1;
      }
   }

   public static class 캐릭터리스폰 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().fakeRelog();
         return 1;
      }
   }

   public static class 디버그스폰 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().dropMessage(6, c.getPlayer().getMap().spawnDebug());
         return 1;
      }
   }

   public static class NearestPortal extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         MaplePortal portal = c.getPlayer().getMap().findClosestPortal(c.getPlayer().getTruePosition());
         MapleCharacter var10000 = c.getPlayer();
         String var10002 = portal.getName();
         var10000.dropMessage(6, var10002 + " id: " + portal.getId() + " script: " + portal.getScriptName());
         return 1;
      }
   }

   public static class 접속수 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         Map<Integer, Integer> connected = World.getConnected();
         StringBuilder conStr = new StringBuilder("Connected Clients: ");
         boolean first = true;
         Iterator iterator = connected.keySet().iterator();

         while(iterator.hasNext()) {
            int i = (Integer)iterator.next();
            if (!first) {
               conStr.append(", ");
            } else {
               first = false;
            }

            if (i == 0) {
               conStr.append("Total: ");
               conStr.append(connected.get(i));
            } else {
               conStr.append("Channel");
               conStr.append(i);
               conStr.append(": ");
               conStr.append(connected.get(i));
            }
         }

         c.getPlayer().dropMessage(6, conStr.toString());
         return 1;
      }
   }

   public static class 핵유저 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         List<CheaterData> cheaters = World.getCheaters();

         for(int x = cheaters.size() - 1; x >= 0; --x) {
            CheaterData cheater = (CheaterData)cheaters.get(x);
            c.getPlayer().dropMessage(6, cheater.getInfo());
         }

         return 1;
      }
   }

   public static class 리포트 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         List<CheaterData> cheaters = World.getReports();

         for(int x = cheaters.size() - 1; x >= 0; --x) {
            CheaterData cheater = (CheaterData)cheaters.get(x);
            c.getPlayer().dropMessage(6, cheater.getInfo());
         }

         return 1;
      }
   }

   public static class 캐릭터정보 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         StringBuilder builder = new StringBuilder();
         MapleCharacter other = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
         if (other == null) {
            builder.append("...does not exist");
            c.getPlayer().dropMessage(6, builder.toString());
            return 0;
         } else {
            if (other.getClient().getLastPing() <= 0L) {
            }

            builder.append(MapleClient.getLogMessage(other, ""));
            builder.append(" at ").append(other.getPosition().x);
            builder.append(" /").append(other.getPosition().y);
            builder.append(" || HP : ");
            builder.append(other.getStat().getHp());
            builder.append(" /");
            builder.append(other.getStat().getCurrentMaxHp());
            builder.append(" || MP : ");
            builder.append(other.getStat().getMp());
            builder.append(" /");
            builder.append(other.getStat().getCurrentMaxMp(other));
            builder.append(" || BattleshipHP : ");
            builder.append(other.currentBattleshipHP());
            builder.append(" || WATK : ");
            builder.append(other.getStat().getTotalWatk());
            builder.append(" || MATK : ");
            builder.append(other.getStat().getTotalMagic());
            builder.append(" || MAXDAMAGE : ");
            builder.append(other.getStat().getCurrentMaxBaseDamage());
            builder.append(" || DAMAGE% : ");
            builder.append(other.getStat().dam_r);
            builder.append(" || BOSSDAMAGE% : ");
            builder.append(other.getStat().bossdam_r);
            builder.append(" || CRIT CHANCE : ");
            builder.append(other.getStat().critical_rate);
            builder.append(" || CRIT DAMAGE : ");
            builder.append(other.getStat().critical_damage);
            builder.append(" || STR : ");
            builder.append(other.getStat().getStr());
            builder.append(" || DEX : ");
            builder.append(other.getStat().getDex());
            builder.append(" || INT : ");
            builder.append(other.getStat().getInt());
            builder.append(" || LUK : ");
            builder.append(other.getStat().getLuk());
            builder.append(" || Total STR : ");
            builder.append(other.getStat().getTotalStr());
            builder.append(" || Total DEX : ");
            builder.append(other.getStat().getTotalDex());
            builder.append(" || Total INT : ");
            builder.append(other.getStat().getTotalInt());
            builder.append(" || Total LUK : ");
            builder.append(other.getStat().getTotalLuk());
            builder.append(" || EXP : ");
            builder.append(other.getExp());
            builder.append(" || MESO : ");
            builder.append(other.getMeso());
            builder.append(" || party : ");
            builder.append(other.getParty() == null ? -1 : other.getParty().getId());
            builder.append(" || hasTrade: ");
            builder.append(other.getTrade() != null);
            builder.append(" || Latency: ");
            builder.append(other.getClient().getLatency());
            builder.append(" || PING: ");
            builder.append(other.getClient().getLastPing());
            builder.append(" || PONG: ");
            builder.append(other.getClient().getLastPong());
            builder.append(" || remoteAddress: ");
            other.getClient().DebugMessage(builder);
            c.getPlayer().dropMessage(6, builder.toString());
            return 1;
         }
      }
   }

   public static class V포인트체크 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length < 2) {
            c.getPlayer().dropMessage(6, "Need playername.");
            return 0;
         } else {
            MapleCharacter chrs = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chrs == null) {
               c.getPlayer().dropMessage(6, "Make sure they are in the correct channel");
            } else {
               MapleCharacter var10000 = c.getPlayer();
               String var10002 = chrs.getName();
               var10000.dropMessage(6, var10002 + " has " + chrs.getVPoints() + " vpoints.");
            }

            return 1;
         }
      }
   }

   public static class 포인트체크 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length < 2) {
            c.getPlayer().dropMessage(6, "Need playername.");
            return 0;
         } else {
            MapleCharacter chrs = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chrs == null) {
               c.getPlayer().dropMessage(6, "Make sure they are in the correct channel");
            } else {
               MapleCharacter var10000 = c.getPlayer();
               String var10002 = chrs.getName();
               var10000.dropMessage(6, var10002 + " has " + chrs.getPoints() + " points.");
            }

            return 1;
         }
      }
   }

   public static class 노래 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().getMap().broadcastMessage(CField.musicChange(splitted[1]));
         return 1;
      }
   }

   public static class 아이템체크 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length >= 3 && splitted[1] != null && !splitted[1].equals("") && splitted[2] != null && !splitted[2].equals("")) {
            int item = Integer.parseInt(splitted[2]);
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int itemamount = chr.getItemQuantity(item, true);
            if (itemamount > 0) {
               c.getPlayer().dropMessage(6, chr.getName() + " has " + itemamount + " (" + item + ").");
            } else {
               MapleCharacter var10000 = c.getPlayer();
               String var10002 = chr.getName();
               var10000.dropMessage(6, var10002 + " doesn't have (" + item + ")");
            }

            return 1;
         } else {
            c.getPlayer().dropMessage(6, "!itemcheck <playername> <itemid>");
            return 0;
         }
      }
   }

   public static class WhereAmI extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().dropMessage(5, "You are on map " + c.getPlayer().getMap().getId());
         return 1;
      }
   }

   public static class 킬 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         MapleCharacter player = c.getPlayer();
         if (splitted.length < 2) {
            c.getPlayer().dropMessage(6, "Syntax: !kill <list player names>");
            return 0;
         } else {
            MapleCharacter victim = null;

            for(int i = 1; i < splitted.length; ++i) {
               try {
                  victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[i]);
               } catch (Exception var7) {
                  c.getPlayer().dropMessage(6, "Player " + splitted[i] + " not found.");
               }

               if (player.allowedToTarget(victim) && player.getGMLevel() >= victim.getGMLevel()) {
                  victim.getStat().setHp(0L, victim);
                  victim.updateSingleStat(MapleStat.HP, victim.getStat().getHp());
               }
            }

            return 1;
         }
      }
   }

   public static class 접속끊기 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         String name = splitted[splitted.length - 1];
         Iterator var4 = ChannelServer.getAllInstances().iterator();

         while(var4.hasNext()) {
            ChannelServer cserv = (ChannelServer)var4.next();
            Iterator var6 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

            while(var6.hasNext()) {
               MapleCharacter player = (MapleCharacter)var6.next();
               if (player != null && player.getName().contains(splitted[splitted.length - 1])) {
                  player.getClient().getChannelServer().getPlayerStorage().deregisterPendingPlayer(player.getId());
                  player.getClient().getSession().close();
                  player.getClient().disconnect(player, true, false, true);
                  c.getPlayer().dropMessage(6, player.getName() + " 이 연결 끊김.");
               }
            }
         }

         var4 = CashShopServer.getPlayerStorage().getAllCharacters().values().iterator();

         MapleCharacter csplayer;
         while(var4.hasNext()) {
            csplayer = (MapleCharacter)var4.next();
            if (csplayer != null && csplayer.getName() != null && csplayer.getName().equals(name)) {
               csplayer.getWorldGMMsg(csplayer, "캐시샵에서 접속이 끊김");
               CashShopServer.getPlayerStorage().deregisterPlayer(csplayer);
               csplayer.getClient().disconnect(csplayer, true, true, false);
               csplayer.getClient().getSession().close();
            }
         }

         var4 = AuctionServer.getPlayerStorage().getAllCharacters().values().iterator();

         while(var4.hasNext()) {
            csplayer = (MapleCharacter)var4.next();
            if (csplayer != null && csplayer.getName() != null && csplayer.getName().equals(name)) {
               csplayer.getWorldGMMsg(csplayer, "경매장에서 접속이 끊김");
               AuctionServer.getPlayerStorage().deregisterPlayer(csplayer);
               csplayer.getClient().disconnect(csplayer, true, true, false);
               csplayer.getClient().getSession().close();
            }
         }

         var4 = FarmServer.getPlayerStorage().getAllCharacters().values().iterator();

         while(var4.hasNext()) {
            csplayer = (MapleCharacter)var4.next();
            if (csplayer != null && csplayer.getName() != null && csplayer.getName().equals(name)) {
               csplayer.getWorldGMMsg(csplayer, "농장에서 접속이 끊김");
               FarmServer.getPlayerStorage().deregisterPlayer(csplayer);
               csplayer.getClient().disconnect(csplayer, true, true, false);
               csplayer.getClient().getSession().close();
            }
         }

         return 0;
      }
   }

   public static class 키벨류조작 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length < 5) {
            c.getPlayer().dropMessage(6, "Syntax: !키벨류조작 캐릭명 type key value");
            return 0;
         } else {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
               victim.setKeyValue(Integer.parseInt(splitted[2]), splitted[3], splitted[4]);
               c.getPlayer().dropMessage(6, "키벨류 수정완료");
               return 1;
            } else {
               c.getPlayer().dropMessage(6, "The victim does not exist.");
               return 0;
            }
         }
      }
   }

   public static class 스킬초기화 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().skillReset();
         return 1;
      }
   }

   public static class DC extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[splitted.length - 1]);
         if (victim != null && c.getPlayer().getGMLevel() >= victim.getGMLevel()) {
            victim.getClient().getSession().close();
            victim.getClient().disconnect(true, false);
            return 1;
         } else {
            c.getPlayer().dropMessage(6, "The victim does not exist.");
            return 0;
         }
      }
   }

   public static class CCPlayer extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().changeChannel(World.Find.findChannel(splitted[1]));
         return 1;
      }
   }

   public static class CC extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().changeChannel(Integer.parseInt(splitted[1]));
         return 1;
      }
   }

   public static class 밴 extends CommandExecute {
      protected boolean hellban = false;
      protected boolean ipBan = false;

      private String getCommand() {
         return this.hellban ? "영구밴" : "밴";
      }

      public int execute(MapleClient c, String[] splitted) {
         if (splitted.length < 3) {
            c.getPlayer().dropMessage(5, "[Syntax] !" + this.getCommand() + " <IGN> <Reason>");
            return 0;
         } else {
            StringBuilder sb = new StringBuilder();
            if (this.hellban) {
               sb.append("Banned ").append(splitted[1]).append(": ").append(StringUtil.joinStringFrom(splitted, 2));
            } else {
               sb.append(c.getPlayer().getName()).append(" banned ").append(splitted[1]).append(": ").append(StringUtil.joinStringFrom(splitted, 2));
            }

            MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            MapleCharacter var10000;
            String var10002;
            if (target != null) {
               if (c.getPlayer().getGMLevel() <= target.getGMLevel() && !c.getPlayer().isAdmin()) {
                  c.getPlayer().dropMessage(6, "[" + this.getCommand() + "] May not ban GMs...");
                  return 1;
               } else {
                  sb.append(" (IP: ").append(target.getClient().getSessionIPAddress()).append(")");
                  if (target.ban(sb.toString(), this.hellban || this.ipBan, false, this.hellban)) {
                     var10000 = c.getPlayer();
                     var10002 = this.getCommand();
                     var10000.dropMessage(6, "[" + var10002 + "] Successfully banned " + splitted[1] + ".");
                     return 1;
                  } else {
                     c.getPlayer().dropMessage(6, "[" + this.getCommand() + "] Failed to ban.");
                     return 0;
                  }
               }
            } else if (MapleCharacter.ban(splitted[1], sb.toString(), false, c.getPlayer().isAdmin() ? 250 : c.getPlayer().getGMLevel(), this.hellban)) {
               var10000 = c.getPlayer();
               var10002 = this.getCommand();
               var10000.dropMessage(6, "[" + var10002 + "] Successfully offline banned " + splitted[1] + ".");
               return 1;
            } else {
               var10000 = c.getPlayer();
               var10002 = this.getCommand();
               var10000.dropMessage(6, "[" + var10002 + "] Failed to ban " + splitted[1]);
               return 0;
            }
         }
      }
   }

   public static class 기간밴 extends CommandExecute {
      protected boolean ipBan = false;
      private String[] types = new String[]{"핵", "봇", "AD", "HARASS", "CURSE", "SCAM", "MISCONDUCT", "SELL", "ICASH", "TEMP", "GM", "IPROGRAM", "메가폰"};

      public int execute(MapleClient c, String[] splitted) {
         int i;
         if (splitted.length >= 4) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            i = Integer.parseInt(splitted[2]);
            int numDay = Integer.parseInt(splitted[3]);
            Calendar cal = Calendar.getInstance();
            cal.add(5, numDay);
            DateFormat df = DateFormat.getInstance();
            if (victim != null && i >= 0 && i < this.types.length) {
               victim.tempban("Temp banned by " + c.getPlayer().getName() + " for " + this.types[i] + " reason", cal, i, this.ipBan);
               c.getPlayer().dropMessage(6, "The character " + splitted[1] + " has been successfully tempbanned till " + df.format(cal.getTime()));
               return 1;
            } else {
               c.getPlayer().dropMessage(6, "Unable to find character or reason was not valid, type tempban to see reasons");
               return 0;
            }
         } else {
            c.getPlayer().dropMessage(6, "Tempban [name] [REASON] [days]");
            StringBuilder s = new StringBuilder("Tempban reasons: ");

            for(i = 0; i < this.types.length; ++i) {
               s.append(i + 1).append(" - ").append(this.types[i]).append(", ");
            }

            c.getPlayer().dropMessage(6, s.toString());
            return 0;
         }
      }
   }

   public static class 스타포스할인 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         Integer sale = Integer.valueOf(splitted[1]);
         if (sale != null && sale >= 0 && sale <= 100) {
            ServerConstants.starForceSalePercent = sale;
            c.getPlayer().dropMessage(6, "스타포스 할인율이 " + sale + "%로 조정되었습니다.");
            return 1;
         } else {
            c.getPlayer().dropMessage(6, "잘못된 값을 입력했습니다.");
            return 0;
         }
      }
   }

   public static class 맵힐 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         MapleCharacter player = c.getPlayer();
         Iterator var4 = player.getMap().getCharacters().iterator();

         while(var4.hasNext()) {
            MapleCharacter mch = (MapleCharacter)var4.next();
            if (mch != null) {
               mch.getStat().setHp(mch.getStat().getMaxHp(), mch);
               mch.updateSingleStat(MapleStat.HP, mch.getStat().getMaxHp());
               mch.getStat().setMp(mch.getStat().getMaxMp(), mch);
               mch.updateSingleStat(MapleStat.MP, mch.getStat().getMaxMp());
            }
         }

         return 1;
      }
   }

   public static class 힐 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().getStat().heal(c.getPlayer());
         return 0;
      }
   }

   public static class 체력낮추기 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         c.getPlayer().getStat().setHp(1L, c.getPlayer());
         c.getPlayer().updateSingleStat(MapleStat.HP, 1L);
         return 0;
      }
   }

   public static class 하이드 extends CommandExecute {
      public int execute(MapleClient c, String[] splitted) {
         SkillFactory.getSkill(9001004).getEffect(1).applyTo(c.getPlayer(), true);
         return 0;
      }
   }
}
