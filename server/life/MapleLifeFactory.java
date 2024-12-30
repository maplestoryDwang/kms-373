package server.life;

import constants.GameConstants;
import constants.ServerConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.Randomizer;
import tools.Pair;
import tools.StringUtil;

public class MapleLifeFactory {
   private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Mob.wz"));
   private static final MapleDataProvider npcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Npc.wz"));
   private static final MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/String.wz"));
   private static final MapleDataProvider etcDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Etc.wz"));
   private static final MapleData mobStringData;
   private static final MapleData npcStringData;
   private static final MapleData npclocData;
   private static Map<Integer, String> npcNames;
   private static Map<Integer, String> npcScripts;
   private static Map<Integer, MapleMonsterStats> monsterStats;
   private static Map<Integer, Integer> NPCLoc;
   private static Map<Integer, List<Integer>> questCount;

   public static AbstractLoadedMapleLife getLife(int id, String type) {
      if (type.equalsIgnoreCase("n")) {
         return getNPC(id);
      } else if (type.equalsIgnoreCase("m")) {
         return getMonster(id);
      } else {
         System.err.println("Unknown Life type: " + type);
         return null;
      }
   }

   public static int getNPCLocation(int npcid) {
      if (NPCLoc.containsKey(npcid)) {
         return (Integer)NPCLoc.get(npcid);
      } else {
         int map = MapleDataTool.getIntConvert(Integer.toString(npcid) + "/0", npclocData, -1);
         NPCLoc.put(npcid, map);
         return map;
      }
   }

   public static final void loadQuestCounts() {
      if (questCount.size() <= 0) {
         Iterator var0 = data.getRoot().getSubdirectories().iterator();

         label81:
         while(true) {
            MapleDataDirectoryEntry mapz;
            do {
               if (!var0.hasNext()) {
                  var0 = npcStringData.iterator();

                  while(var0.hasNext()) {
                     MapleData c = (MapleData)var0.next();

                     int nid;
                     try {
                        nid = Integer.parseInt(c.getName());
                     } catch (Exception var11) {
                        continue;
                     }

                     String n = StringUtil.getLeftPaddedStr(nid + ".img", '0', 11);

                     try {
                        if (npcData.getData(n) != null) {
                           String name = MapleDataTool.getString("name", c, "MISSINGNO");
                           if (!name.contains("Maple TV") && !name.contains("Baby Moon Bunny")) {
                              npcNames.put(nid, name);
                           }
                        }
                     } catch (NullPointerException var9) {
                     } catch (RuntimeException var10) {
                     }
                  }

                  return;
               }

               mapz = (MapleDataDirectoryEntry)var0.next();
            } while(!mapz.getName().equals("QuestCountGroup"));

            Iterator var2 = mapz.getFiles().iterator();

            while(true) {
               while(true) {
                  if (!var2.hasNext()) {
                     continue label81;
                  }

                  MapleDataFileEntry entry = (MapleDataFileEntry)var2.next();
                  int id = Integer.parseInt(entry.getName().substring(0, entry.getName().length() - 4));
                  MapleData dat = data.getData("QuestCountGroup/" + entry.getName());
                  if (dat != null && dat.getChildByPath("info") != null) {
                     List<Integer> z = new ArrayList();
                     Iterator var7 = dat.getChildByPath("info").iterator();

                     while(var7.hasNext()) {
                        MapleData da = (MapleData)var7.next();
                        z.add(MapleDataTool.getInt(da, 0));
                     }

                     questCount.put(id, z);
                  } else {
                     System.out.println("null questcountgroup");
                  }
               }
            }
         }
      }
   }

   public static final void loadNpcScripts() {
      Iterator var0 = npcStringData.iterator();

      label80:
      while(var0.hasNext()) {
         MapleData c = (MapleData)var0.next();

         int nid;
         try {
            nid = Integer.parseInt(c.getName());
         } catch (Exception var12) {
            continue;
         }

         String n = StringUtil.getLeftPaddedStr(nid + ".img", '0', 11);

         try {
            if (npcData.getData(n) != null) {
               Iterator var4 = npcData.getData(n).iterator();

               label76:
               while(true) {
                  MapleData d;
                  do {
                     if (!var4.hasNext()) {
                        continue label80;
                     }

                     d = (MapleData)var4.next();
                  } while(!d.getName().equals("info"));

                  Iterator var6 = d.iterator();

                  label74:
                  while(true) {
                     MapleData e;
                     do {
                        if (!var6.hasNext()) {
                           continue label76;
                        }

                        e = (MapleData)var6.next();
                     } while(!e.getName().equals("script"));

                     Iterator var8 = e.iterator();

                     while(true) {
                        MapleData f;
                        do {
                           if (!var8.hasNext()) {
                              continue label74;
                           }

                           f = (MapleData)var8.next();
                        } while(!e.getName().equals("script"));

                        MapleData scripts;
                        for(Iterator var10 = f.iterator(); var10.hasNext(); npcScripts.put(nid, (String)scripts.getData())) {
                           scripts = (MapleData)var10.next();
                           if (scripts.getType() != MapleDataType.STRING) {
                           }
                        }
                     }
                  }
               }
            }
         } catch (NullPointerException var13) {
            System.out.println(c.getName());
            var13.printStackTrace();
         } catch (RuntimeException var14) {
         }
      }

      npcScripts.put(9000216, "mannequin_manage");
   }

   public static final List<Integer> getQuestCount(int id) {
      return (List)questCount.get(id);
   }

   public static MapleMonster getMonster(int mid) {
      MapleMonsterStats stats = getMonsterStats(mid);
      return stats == null ? null : new MapleMonster(mid, stats);
   }

   public static MapleMonster getMonster(int mid, boolean extreme) {
      MapleMonsterStats stats = getMonsterStats(mid);
      return stats == null ? null : new MapleMonster(mid, stats, extreme, false);
   }

   public static MapleMonster getMonster(int mid, boolean extreme, boolean hellMode) {
      MapleMonsterStats stats = getMonsterStats(mid);
      return stats == null ? null : new MapleMonster(mid, stats, extreme, hellMode);
   }

   public static MapleMonsterStats getMonsterStats(int mid) {
      MapleMonsterStats stats = (MapleMonsterStats)monsterStats.get(mid);
      if (stats != null && stats.getHp() <= 0L) {
         monsterStats.remove(mid);
         stats = null;
      }

      if (stats == null) {
         MapleData monsterData = null;

         try {
            monsterData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(mid) + ".img", '0', 11));
         } catch (RuntimeException var27) {
            return null;
         }

         if (monsterData == null) {
            return null;
         }

         MapleData monsterInfoData = monsterData.getChildByPath("info");
         stats = new MapleMonsterStats(mid);
         long maxpHP = 0L;
         if (mid == 8645123) {
            System.out.println("체크");
         }

         try {
            if (monsterInfoData.getChildByPath("maxHP").getType() == MapleDataType.INT) {
               maxpHP = (long)MapleDataTool.getIntConvert("maxHP", monsterInfoData);
            } else if (monsterInfoData.getChildByPath("maxHP").getType() == MapleDataType.LONG) {
               maxpHP = MapleDataTool.getLongConvert("maxHP", monsterInfoData, 0);
            }
         } catch (Exception var26) {
         }

         List<Pair<Integer, Long>> list = ServerConstants.boss;
         boolean boss = false;

         for(int i = 0; i < list.size(); ++i) {
            if (mid == (Integer)((Pair)list.get(i)).getLeft()) {
               long hp = (Long)((Pair)list.get(i)).getRight();
               stats = new MapleMonsterStats((Integer)((Pair)list.get(i)).getLeft());
               stats.setHp(hp);
               boss = true;
            }
         }

         List<Pair<Integer, Long>> list2 = ServerConstants.boss2;

         for(int j = 0; j < list2.size(); ++j) {
            if (mid == (Integer)((Pair)list2.get(j)).getLeft()) {
               long hp = (Long)((Pair)list2.get(j)).getRight();
               stats = new MapleMonsterStats((Integer)((Pair)list2.get(j)).getLeft());
               stats.setHp(hp);
               boss = true;
            }
         }

         if (!boss) {
            switch(mid) {
            case 2400206:
               stats.setHp(10994286240L);
            case 2400207:
               stats.setHp(15994286240L);
            case 2400201:
               stats.setHp(20994286240L);
            case 2400200:
               stats.setHp(25994286240L);
            case 2400202:
               stats.setHp(30994286240L);
            case 9300574:
               stats.setHp(40994286240L);
            case 8148001:
               stats.setHp(50994286240L);
            case 8148009:
               stats.setHp(60994286240L);
            case 8148007:
               stats.setHp(70994286240L);
            case 9300782:
               stats.setHp(80994286240L);
            case 8880518:
               stats.setHp(50000L);
            case 8880614:
               stats.setHp(50000L);
               break;
            case 8120105:
               stats.setHp(3216291020L);
               break;
            case 8220110:
               stats.setHp(999625223624000L);
               break;
            case 8644611:
               stats.setHp(21000000000L);
               break;
            case 8644612:
            case 8644650:
            case 8645009:
               stats.setHp(26000000000000L);
               break;
            case 8880100:
               stats.setHp(24850000000000L);
               break;
            case 8880101:
               stats.setHp(10650000000000L);
               break;
            case 8880110:
               stats.setHp(840000000000L);
               break;
            case 8880111:
               stats.setHp(360000000000L);
               break;
            case 8880140:
               stats.setHp(12000000000000L);
               break;
            case 8880141:
               stats.setHp(54000000000000L);
               break;
            case 8880150:
               stats.setHp(12000000000000L);
               break;
            case 8880151:
               stats.setHp(54000000000000L);
               break;
            case 8880153:
               stats.setHp(15750000000000L);
               break;
            case 8880181:
            case 8880183:
            case 8880184:
            case 8880185:
               stats.setHp(600000000000L);
               break;
            case 8880190:
               stats.setHp(13500000000L);
               break;
            case 8880191:
               stats.setHp(6500000000L);
               break;
            case 8880300:
            case 8880303:
            case 8880304:
               stats.setHp(42000000000000L);
               break;
            case 8880301:
               stats.setHp(31500000000000L);
               break;
            case 8880302:
               stats.setHp(52500000000000L);
               break;
            case 8880340:
            case 8880343:
            case 8880344:
               stats.setHp(8400000000000L);
               break;
            case 8880341:
               stats.setHp(6300000000000L);
               break;
            case 8880342:
               stats.setHp(10500000000000L);
               break;
            case 8880405:
               stats.setHp(176000000000000L);
               break;
            case 8880408:
               stats.setHp(15000000000000L);
               break;
            case 8880409:
               stats.setHp(15000000000000L);
               break;
            case 8880500:
            case 8880501:
               stats.setHp(65000000000000L);
               break;
            case 8880502:
               stats.setHp(135000000000000L);
               break;
            case 8880503:
               stats.setHp(200000000000000L);
               break;
            case 8880504:
               stats.setHp(100000000000000L);
               break;
            case 8880505:
               stats.setHp(5000000000L);
               break;
            case 8881000:
               stats.setHp(2625223624000L);
               break;
            case 8950000:
               stats.setHp(1400000000000L);
               break;
            case 8950001:
               stats.setHp(7000000000000L);
               break;
            case 8950002:
               stats.setHp(24125000000000L);
               break;
            case 8950100:
               stats.setHp(400000000000L);
               break;
            case 8950101:
               stats.setHp(400000000000L);
               break;
            case 8950102:
               stats.setHp(700000000000L);
               break;
            case 9101078:
            case 9500654:
            case 9833885:
               stats.setHp(2982480000L);
               break;
            case 9300757:
               stats.setHp(1330449192000000000L);
               break;
            case 9303162:
               stats.setHp(25000000000L);
               break;
            case 9500650:
               stats.setHp(648000000000L);
               break;
            case 9500651:
               stats.setHp(1620000000000L);
               break;
            case 9500652:
               stats.setHp(162000000000000L);
               break;
            case 9500653:
               stats.setHp(1134000000000000L);
               break;
            case 9832024:
               stats.setSummonType((byte)112);
               break;
            case 9833376:
               stats.setHp(133044912000000000L);
               break;
            case 9833886:
               stats.setHp(6957192000L);
               break;
            case 9833887:
               stats.setHp(11784960000L);
               break;
            case 9833888:
               stats.setHp(29322734000L);
               break;
            case 9833889:
               stats.setHp(100445184000L);
               break;
            case 9833890:
               stats.setHp(171057744000L);
               break;
            case 9833891:
               stats.setHp(1197404208000L);
               break;
            case 9833892:
               stats.setHp(59870210400000L);
               break;
            case 9833898:
               stats.setHp(3162360000L);
               break;
            case 9833899:
               stats.setHp(4910400000L);
               break;
            case 9833900:
               stats.setHp(11277974400L);
               break;
            case 9833901:
               stats.setHp(23915520000L);
               break;
            case 9833902:
               stats.setHp(38012832000L);
               break;
            case 9833903:
               stats.setHp(266089824000L);
               break;
            case 9833904:
               stats.setHp(13304491200000L);
               break;
            default:
               stats.setHp(GameConstants.getPartyPlayHP(mid) > 0 ? (long)GameConstants.getPartyPlayHP(mid) : (monsterInfoData.getChildByPath("finalmaxHP") != null ? maxpHP + MapleDataTool.getLongConvert("finalmaxHP", monsterInfoData, 0) : maxpHP));
            }
         }

         stats.setMp(MapleDataTool.getIntConvert("maxMP", monsterInfoData, 0));
         stats.setExp(mid == 9300027 ? 0L : (long)(GameConstants.getPartyPlayEXP(mid) > 0 ? GameConstants.getPartyPlayEXP(mid) : MapleDataTool.getIntConvert("exp", monsterInfoData, 0)));
         stats.setLevel((short)MapleDataTool.getIntConvert("level", monsterInfoData, 1));
         stats.setCharismaEXP((short)MapleDataTool.getIntConvert("charismaEXP", monsterInfoData, 0));
         stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", monsterInfoData, 0));
         stats.setrareItemDropLevel((byte)MapleDataTool.getIntConvert("rareItemDropLevel", monsterInfoData, 0));
         stats.setFixedDamage(MapleDataTool.getIntConvert("fixedDamage", monsterInfoData, -1));
         stats.setOnlyNormalAttack(MapleDataTool.getIntConvert("onlyNormalAttack", monsterInfoData, 0) > 0);
         stats.setBoss(GameConstants.getPartyPlayHP(mid) > 0 || MapleDataTool.getIntConvert("boss", monsterInfoData, 0) > 0 || mid == 8810018 || mid == 9410066 || mid >= 8810118 && mid <= 8810122);
         stats.setNotSeperateSoul(MapleDataTool.getIntConvert("notSeperateSoul", monsterInfoData, 0) > 0);
         stats.setExplosiveReward(MapleDataTool.getIntConvert("explosiveReward", monsterInfoData, 0) > 0);
         stats.setUndead(MapleDataTool.getIntConvert("undead", monsterInfoData, 0) > 0);
         stats.setEscort(MapleDataTool.getIntConvert("escort", monsterInfoData, 0) > 0);
         stats.setPartyBonus(GameConstants.getPartyPlayHP(mid) > 0 || MapleDataTool.getIntConvert("partyBonusMob", monsterInfoData, 0) > 0);
         stats.setPartyBonusRate(MapleDataTool.getIntConvert("partyBonusR", monsterInfoData, 0));
         if (mobStringData.getChildByPath(String.valueOf(mid)) != null) {
            stats.setName(MapleDataTool.getString("name", mobStringData.getChildByPath(String.valueOf(mid)), "MISSINGNO"));
         }

         stats.setBuffToGive(MapleDataTool.getIntConvert("buff", monsterInfoData, -1));
         stats.setChange(MapleDataTool.getIntConvert("changeableMob", monsterInfoData, 0) > 0);
         stats.setFriendly(MapleDataTool.getIntConvert("damagedByMob", monsterInfoData, 0) > 0);
         stats.setNoDoom(MapleDataTool.getIntConvert("noDoom", monsterInfoData, 0) > 0);
         stats.setCP((byte)MapleDataTool.getIntConvert("getCP", monsterInfoData, 0));
         stats.setPoint(MapleDataTool.getIntConvert("point", monsterInfoData, 0));
         stats.setDropItemPeriod(MapleDataTool.getIntConvert("dropItemPeriod", monsterInfoData, 0));
         stats.setPhysicalAttack(MapleDataTool.getIntConvert("PADamage", monsterInfoData, 0));
         stats.setMagicAttack(MapleDataTool.getIntConvert("MADamage", monsterInfoData, 0));
         stats.setPDRate(MapleDataTool.getIntConvert("PDRate", monsterInfoData, 0));
         stats.setMDRate(MapleDataTool.getIntConvert("MDRate", monsterInfoData, 0));
         stats.setAcc(MapleDataTool.getIntConvert("acc", monsterInfoData, 0));
         stats.setEva(MapleDataTool.getIntConvert("eva", monsterInfoData, 0));
         stats.setSummonType((byte)MapleDataTool.getIntConvert("summonType", monsterInfoData, 0));
         stats.setHpLinkMob(MapleDataTool.getIntConvert("HpLinkMob", monsterInfoData, 0));
         if (mid == 8880512) {
            stats.setSummonType((byte)1);
         }

         stats.setCategory((byte)MapleDataTool.getIntConvert("category", monsterInfoData, 0));
         stats.setSpeed(MapleDataTool.getIntConvert("speed", monsterInfoData, 0));
         stats.setPushed(MapleDataTool.getIntConvert("pushed", monsterInfoData, 0));
         stats.setPublicReward(MapleDataTool.getIntConvert("publicReward", monsterInfoData, 0) > 0 || MapleDataTool.getIntConvert("individualReward", monsterInfoData, 0) > 0);
         stats.setIgnoreMovable(MapleDataTool.getIntConvert("ignoreMovable", monsterInfoData, 0));
         stats.setIgnoreMoveableMsg(MapleDataTool.getString("ignoreMoveableMsg", monsterInfoData, ""));
         boolean var10000;
         if (MapleDataTool.getIntConvert("HPgaugeHide", monsterInfoData, 0) <= 0 && MapleDataTool.getIntConvert("hideHP", monsterInfoData, 0) <= 0) {
            var10000 = false;
         } else {
            var10000 = true;
         }

         MapleData selfd = monsterInfoData.getChildByPath("selfDestruction");
         if (selfd != null) {
            stats.setSelfDHP(MapleDataTool.getIntConvert("hp", selfd, 0));
            stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", selfd, stats.getRemoveAfter()));
            stats.setSelfD((byte)MapleDataTool.getIntConvert("action", selfd, -1));
         } else {
            stats.setSelfD((byte)-1);
         }

         MapleData firstAttackData = monsterInfoData.getChildByPath("firstAttack");
         if (firstAttackData != null) {
            if (firstAttackData.getType() == MapleDataType.FLOAT) {
               stats.setFirstAttack(Math.round(MapleDataTool.getFloat(firstAttackData)) > 0);
            } else {
               stats.setFirstAttack(MapleDataTool.getInt(firstAttackData) > 0);
            }
         }

         if (stats.isBoss() || isDmgSponge(mid)) {
            if (monsterInfoData.getChildByPath("hpTagColor") != null && monsterInfoData.getChildByPath("hpTagBgcolor") != null) {
               stats.setTagColor(MapleDataTool.getIntConvert("hpTagColor", monsterInfoData));
               stats.setTagBgColor(MapleDataTool.getIntConvert("hpTagBgcolor", monsterInfoData));
            } else {
               stats.setTagColor(0);
               stats.setTagBgColor(0);
            }
         }

         MapleData banishData = monsterInfoData.getChildByPath("ban");
         if (banishData != null) {
            stats.setBanishInfo(new BanishInfo(MapleDataTool.getString("banMsg", banishData), MapleDataTool.getInt("banMap/0/field", banishData, -1), MapleDataTool.getString("banMap/0/portal", banishData, "sp")));
         }

         if (mid == 8860000 || mid == 8860001 || mid == 8860005 || mid == 8860007) {
            stats.setBanishInfo(new BanishInfo("자신 속의 추악한 내면을 마주한 기분이 어떠신지요?", 272020300, "0"));
         }

         MapleData reviveInfo = monsterInfoData.getChildByPath("revive");
         Iterator var15;
         MapleData monsterSkillData;
         if (reviveInfo != null) {
            List<Integer> revives = new LinkedList();
            var15 = reviveInfo.iterator();

            while(var15.hasNext()) {
               monsterSkillData = (MapleData)var15.next();
               revives.add(MapleDataTool.getInt(monsterSkillData));
            }

            stats.setRevives(revives);
         }

         MapleData skeletonData = monsterData.getChildByPath("HitParts");
         int k;
         if (skeletonData != null) {
            var15 = skeletonData.iterator();

            while(var15.hasNext()) {
               monsterSkillData = (MapleData)var15.next();
               k = Integer.valueOf(MapleDataTool.getInt("0/stat/durability", monsterSkillData, 0));
               stats.addSkeleton(monsterSkillData.getName(), 0, k);
            }
         }

         MapleData trans = monsterInfoData.getChildByPath("trans");
         if (trans != null) {
            Transform transform = new Transform(MapleDataTool.getInt("0", trans, 0), MapleDataTool.getInt("1", trans, 0), MapleDataTool.getInt("cooltime", trans, 0), MapleDataTool.getInt("hpTriggerOff", trans, 0), MapleDataTool.getInt("hpTriggerOn", trans, 0), MapleDataTool.getInt("time", trans, 0), MapleDataTool.getInt("withMob", trans, 0));
            List<Pair<Integer, Integer>> skills = new ArrayList();
            MapleData transSkills = trans.getChildByPath("skill");
            if (transSkills != null) {
               Iterator var19 = transSkills.getChildren().iterator();

               while(var19.hasNext()) {
                  MapleData transSkill = (MapleData)var19.next();
                  skills.add(new Pair(MapleDataTool.getInt("skill", transSkill, 0), MapleDataTool.getInt("level", transSkill, 0)));
               }
            }

            transform.setSkills(skills);
            stats.setTrans(transform);
         }

         monsterSkillData = monsterInfoData.getChildByPath("skill");
         int k;
         if (monsterSkillData != null) {
            k = 0;
            ArrayList skills = new ArrayList();

            while(true) {
               if (monsterSkillData.getChildByPath(Integer.toString(k)) == null) {
                  stats.setSkills(skills);
                  break;
               }

               int onlyFsm = Integer.valueOf(MapleDataTool.getInt(k + "/onlyFsm", monsterSkillData, 0));
               k = Integer.valueOf(MapleDataTool.getInt(k + "/onlyOtherSkill", monsterSkillData, 0));
               MobSkill ms = MobSkillFactory.getMobSkill(Integer.valueOf(MapleDataTool.getInt(k + "/skill", monsterSkillData, 0)), Integer.valueOf(MapleDataTool.getInt(k + "/level", monsterSkillData, 0)));
               if (ms != null) {
                  ms.setOnlyFsm(ms.getSkillId() == 215 && ms.getSkillLevel() == 4 ? false : onlyFsm > 0);
                  ms.setAction(MapleDataTool.getInt(k + "/action", monsterSkillData, 0));
                  int skillAfter = Integer.valueOf(MapleDataTool.getInt(k + "/skillAfter", monsterSkillData, 0));
                  if (skillAfter > ms.getSkillAfter()) {
                     ms.setSkillAfter(skillAfter);
                  }

                  ms.setOnlyOtherSkill(k > 0);
                  ms.setSkillForbid((long)Integer.valueOf(MapleDataTool.getInt(k + "/skillForbid", monsterSkillData, 0)));
                  ms.setAfterAttack(Integer.valueOf(MapleDataTool.getInt(k + "/afterAttack", monsterSkillData, -1)));
                  ms.setAfterAttackCount(Integer.valueOf(MapleDataTool.getInt(k + "/afterAttackCount", monsterSkillData, 0)));
                  ms.setAfterDead(Integer.valueOf(MapleDataTool.getInt(k + "/afterDead", monsterSkillData, 0)));
                  skills.add(ms);
               }

               ++k;
            }
         }

         decodeElementalString(stats, MapleDataTool.getString("elemAttr", monsterInfoData, ""));
         k = MapleDataTool.getIntConvert("link", monsterInfoData, 0);
         if (k != 0) {
            monsterData = data.getData(StringUtil.getLeftPaddedStr(k + ".img", '0', 11));
         }

         Iterator var36 = monsterData.iterator();

         MapleData monsterAtt;
         while(var36.hasNext()) {
            monsterAtt = (MapleData)var36.next();
            if (monsterAtt.getName().equals("fly")) {
               stats.setFly(true);
               stats.setMobile(true);
               break;
            }

            if (monsterAtt.getName().equals("move")) {
               stats.setMobile(true);
            }
         }

         boolean mobZone = monsterInfoData.getChildByPath("mobZone") != null;
         stats.setMobZone(mobZone);
         monsterAtt = monsterInfoData.getChildByPath("attack");
         if (monsterAtt != null) {
            k = 0;
            ArrayList attacks = new ArrayList();

            while(true) {
               if (monsterAtt.getChildByPath(Integer.toString(k)) == null) {
                  stats.setAttacks(attacks);
                  break;
               }

               MobAttack attack = new MobAttack(MapleDataTool.getInt(k + "/action", monsterAtt, -1), MapleDataTool.getInt(k + "/afterAttack", monsterAtt, -1), MapleDataTool.getInt(k + "/fixAttack", monsterAtt, -1), MapleDataTool.getInt(k + "/onlyAfterAttack", monsterAtt, -1), MapleDataTool.getInt(k + "/cooltime", monsterAtt, -1), MapleDataTool.getInt(k + "/afterAttackCount", monsterAtt, -1));
               MapleData callSkillData;
               if (monsterAtt.getChildByPath(Integer.toString(k) + "/callSkill") != null) {
                  callSkillData = monsterAtt.getChildByPath(Integer.toString(k) + "/callSkill");

                  for(int m = 0; callSkillData.getChildByPath(String.valueOf(m)) != null; ++m) {
                     MapleData callSkillIdxData = callSkillData.getChildByPath(String.valueOf(m));
                     attack.addSkill(MapleDataTool.getInt("skill", callSkillIdxData, 0), MapleDataTool.getInt("level", callSkillIdxData, 0), MapleDataTool.getInt("delay", callSkillIdxData, 0));
                  }
               }

               if (monsterAtt.getChildByPath(Integer.toString(k) + "/callSkillWithData") != null) {
                  callSkillData = monsterAtt.getChildByPath(Integer.toString(k) + "/callSkillWithData");
                  attack.addSkill(MapleDataTool.getInt("skill", callSkillData, 0), MapleDataTool.getInt("level", callSkillData, 0), MapleDataTool.getInt("delay", callSkillData, 0));
               }

               attacks.add(attack);
               ++k;
            }
         }

         byte hpdisplaytype = -1;
         if (stats.getTagColor() > 0) {
            hpdisplaytype = 0;
         } else if (stats.isFriendly()) {
            hpdisplaytype = 1;
         } else if (mid >= 9300184 && mid <= 9300215) {
            hpdisplaytype = 2;
         } else if (!stats.isBoss() || mid == 9410066 || stats.isPartyBonus()) {
            hpdisplaytype = 3;
         }

         stats.setHPDisplayType(hpdisplaytype);
         monsterStats.put(mid, stats);
      }

      return stats;
   }

   public static final void decodeElementalString(MapleMonsterStats stats, String elemAttr) {
      for(int i = 0; i < elemAttr.length(); i += 2) {
         stats.setEffectiveness(Element.getFromChar(elemAttr.charAt(i)), ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
      }

   }

   private static final boolean isDmgSponge(int mid) {
      switch(mid) {
      case 8810018:
      case 8810119:
      case 8810120:
      case 8810121:
      case 8810122:
      case 8820009:
      case 8820010:
      case 8820011:
      case 8820012:
      case 8820013:
      case 8820014:
      case 8820110:
      case 8820111:
      case 8820112:
      case 8820113:
      case 8820114:
         return true;
      default:
         return false;
      }
   }

   public static MapleNPC getNPC(int nid) {
      String name = (String)npcNames.get(nid);
      return name == null ? null : new MapleNPC(nid, name);
   }

   public static int getRandomNPC() {
      List<Integer> vals = new ArrayList(npcNames.keySet());
      int ret = 0;

      while(ret <= 0) {
         ret = (Integer)vals.get(Randomizer.nextInt(vals.size()));
         if (((String)npcNames.get(ret)).contains("MISSINGNO")) {
            ret = 0;
         }
      }

      return ret;
   }

   public static Map<Integer, String> getNpcScripts() {
      return npcScripts;
   }

   public static void setNpcScripts(Map<Integer, String> npcScripts) {
      MapleLifeFactory.npcScripts = npcScripts;
   }

   public static Map<Integer, MapleMonsterStats> getMonsterStats() {
      return monsterStats;
   }

   static {
      mobStringData = stringDataWZ.getData("Mob.img");
      npcStringData = stringDataWZ.getData("Npc.img");
      npclocData = etcDataWZ.getData("NpcLocation.img");
      npcNames = new HashMap();
      npcScripts = new HashMap();
      monsterStats = new HashMap();
      NPCLoc = new HashMap();
      questCount = new HashMap();
   }
}
