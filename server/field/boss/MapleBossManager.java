package server.field.boss;

import client.MapleCharacter;
import client.SecondaryStat;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import server.Obstacle;
import server.Randomizer;
import server.Timer;
import server.field.boss.dunkel.DunkelEliteBoss;
import server.field.boss.lucid.Butterfly;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.Transform;
import server.maps.MapleMap;
import server.maps.MapleNodes;
import tools.Pair;
import tools.Triple;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.SLFCGPacket;

public class MapleBossManager {
   public static void changePhase(MapleMonster monster) {
      if (monster.getId() >= 8800102 && monster.getId() <= 8800110) {
         int[] arms = new int[]{8800103, 8800104, 8800105, 8800106, 8800107, 8800108, 8800109, 8800110};
         if (monster.getPhase() == 0) {
            monster.setPhase((byte)1);
         }

         if (monster.getId() == 8800102) {
            boolean nextPhase = true;
            int[] var3 = arms;
            int var4 = arms.length;

            int var5;
            int arm;
            for(var5 = 0; var5 < var4; ++var5) {
               arm = var3[var5];
               if (monster.getMap().getMonsterById(arm) != null) {
                  nextPhase = false;
                  break;
               }
            }

            if (nextPhase) {
               monster.setPhase((byte)3);
            }

            if (monster.getHPPercent() <= 20) {
               monster.setPhase((byte)4);
               var3 = arms;
               var4 = arms.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  arm = var3[var5];
                  monster.getMap().killMonster(arm);
               }
            }
         }

         monster.getMap().broadcastMessage(MobPacket.changePhase(monster));
      } else if (monster.getId() == 8880000 || monster.getId() == 8880002 || monster.getId() == 8880010) {
         byte phase;
         if (monster.getHPPercent() <= 25) {
            phase = 4;
         } else if (monster.getHPPercent() <= 50) {
            phase = 3;
         } else if (monster.getHPPercent() <= 75) {
            phase = 2;
         } else {
            phase = 1;
         }

         if (monster.getPhase() != phase) {
            monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 5, 1, "매그너스가 구와르를 제어하는 힘이 약화 되었습니다. 구와르의 기운이 더욱 강해집니다."));
            monster.setPhase(phase);
            monster.getMap().broadcastMessage(MobPacket.changePhase(monster));
         }
      }

   }

   public static void setBlockAttack(MapleMonster monster) {
      List<Integer> blocks = new ArrayList();
      switch(monster.getId()) {
      case 8800102:
         if (monster.getPhase() != 2) {
            monster.getMap().killMonster(8800117);
            List<String> updateLists = new ArrayList();
            monster.getMap().updateEnvironment(updateLists);
            blocks.add(1);
         }

         if (monster.getPhase() != 3) {
            blocks.add(2);
            blocks.add(3);
            blocks.add(4);
            blocks.add(5);
         }

         if (monster.getPhase() != 4) {
            blocks.add(6);
            blocks.add(7);
            blocks.add(8);
         }
         break;
      case 8850011:
      case 8850111:
         blocks.add(4);
         break;
      case 8880300:
      case 8880301:
      case 8880303:
      case 8880304:
      case 8880321:
      case 8880322:
      case 8880325:
      case 8880326:
      case 8880340:
      case 8880341:
      case 8880343:
      case 8880344:
      case 8880351:
      case 8880352:
      case 8880355:
      case 8880356:
         return;
      case 8910000:
      case 8910100:
         if (monster.getHPPercent() > 10) {
            blocks.add(3);
            blocks.add(4);
            blocks.add(5);
            blocks.add(6);
            blocks.add(7);
         }

         if (monster.getHPPercent() > 70) {
            blocks.add(2);
         }
         break;
      case 8930000:
      case 8930100:
         if (monster.getId() == 8930100 && monster.getHPPercent() > 70) {
            blocks.add(2);
            blocks.add(3);
            blocks.add(4);
         }

         if (monster.getHPPercent() > 40) {
            blocks.add(8);
            blocks.add(9);
            blocks.add(10);
            blocks.add(12);
            blocks.add(13);
            blocks.add(14);
            blocks.add(15);
         }
      }

      monster.getMap().broadcastMessage(MobPacket.BlockAttack(monster, blocks));
   }

   public static void ZakumBodyHandler(MapleMonster monster, MapleCharacter chr, boolean facingLeft) {
      long time = System.currentTimeMillis();
      List<MobSkill> useableSkills = new ArrayList();
      Iterator var6 = monster.getSkills().iterator();

      while(var6.hasNext()) {
         MobSkill msi = (MobSkill)var6.next();
         if (time - monster.getLastSkillUsed(msi.getSkillId(), msi.getSkillLevel()) >= 0L && monster.getHPPercent() <= msi.getHP() && !msi.isOnlyOtherSkill()) {
            if (msi.isOnlyFsm()) {
               if (monster.getPhase() == 2) {
                  if (msi.getSkillId() == 201 && msi.getSkillLevel() == 162) {
                     useableSkills.add(msi);
                  }
               } else if (monster.getPhase() == 3 && msi.getSkillId() == 176 && msi.getSkillLevel() == 27 && msi.getSkillId() != 201 && msi.getSkillLevel() != 162) {
                  useableSkills.add(msi);
               }
            } else {
               useableSkills.add(msi);
            }
         }
      }

      if (!useableSkills.isEmpty()) {
         MobSkill msi = (MobSkill)useableSkills.get(Randomizer.nextInt(useableSkills.size()));
         monster.setLastSkillUsed(msi, time, msi.getInterval());
         monster.setNextSkill(msi.getSkillId());
         monster.setNextSkillLvl(msi.getSkillLevel());
      }

   }

   public static void ZakumArmHandler(MapleMonster monster, MapleCharacter chr, int actionAndDir) {
      if ((monster.getId() == 8800002 || monster.getId() == 8800102 && monster.getPhase() <= 2) && monster.getCustomValue(8800002) == null) {
         int sized;
         int randMob;
         if (monster.getPhase() == 1) {
            Map<Integer, Integer> list = new LinkedHashMap();
            boolean changephase = false;
            if (monster.getCustomValue0(8800003) != 2L && monster.getCustomValue0(8800003) != 3L && monster.getCustomValue0(8800003) != 4L) {
               if (monster.getCustomValue0(8800003) == 6L) {
                  monster.addSkillCustomInfo(8800004, 1L);
                  if (monster.getCustomValue0(8800004) != 2L) {
                     monster.setCustomInfo(8800002, 0, 5300);
                  } else {
                     monster.setCustomInfo(8800003, 2, 0);
                     monster.removeCustomInfo(8800004);
                     monster.addSkillCustomInfo(8800005, 1L);
                     if (monster.getCustomValue0(8800005) == 2L) {
                        monster.removeCustomInfo(8800005);
                        Iterator var5 = monster.getMap().getAllMonster().iterator();

                        label267:
                        while(true) {
                           MapleMonster mob;
                           do {
                              if (!var5.hasNext()) {
                                 break label267;
                              }

                              mob = (MapleMonster)var5.next();
                           } while((mob.getId() < 8800002 || mob.getId() > 8800010) && (mob.getId() < 8800102 || mob.getId() > 8800110));

                           mob.setPhase((byte)2);
                           mob.getMap().broadcastMessage(MobPacket.changePhase(mob));
                           changephase = true;
                        }
                     }

                     monster.setCustomInfo(8800002, 0, 4000);
                  }
               }
            } else {
               monster.addSkillCustomInfo(8800004, 1L);
               if (monster.getCustomValue0(8800004) == 3L) {
                  monster.addSkillCustomInfo(8800003, monster.getCustomValue0(8800003) == 4L ? 2L : 1L);
                  monster.removeCustomInfo(8800004);
               }

               monster.setCustomInfo(8800002, 0, 2000);
            }

            Iterator var17;
            if (!changephase) {
               int mobsize = 0;
               var17 = monster.getMap().getAllMonster().iterator();

               label247:
               while(true) {
                  MapleMonster mob;
                  do {
                     if (!var17.hasNext()) {
                        sized = (int)monster.getCustomValue0(8800003);
                        if ((long)mobsize < monster.getCustomValue0(8800003)) {
                           sized = mobsize;
                        }

                        while(list.size() < sized) {
                           randMob = Randomizer.rand(8800003, 8800010);
                           if (monster.getId() == 8800102) {
                              randMob += 100;
                           }

                           if (!list.containsKey(randMob)) {
                              list.put(randMob, 0);
                           }
                        }

                        Iterator var21 = monster.getMap().getAllMonster().iterator();

                        while(var21.hasNext()) {
                           MapleMonster mob = (MapleMonster)var21.next();
                           Iterator var9 = list.entrySet().iterator();

                           while(var9.hasNext()) {
                              Entry<Integer, Integer> mobs = (Entry)var9.next();
                              if ((Integer)mobs.getKey() == mob.getId()) {
                                 MobSkill msi;
                                 if (monster.getId() == 8800102) {
                                    msi = mob.getStats().getSkill(176, monster.getCustomValue0(8800003) == 6L ? 26 : 25);
                                    msi.setMobSkillDelay(chr, mob, monster.getCustomValue0(8800003) == 6L ? 2430 : 1420, (short)0, (actionAndDir & 1) != 0);
                                 } else {
                                    msi = mob.getStats().getSkill(176, monster.getCustomValue0(8800003) == 6L ? 34 : 33);
                                    msi.setMobSkillDelay(chr, mob, monster.getCustomValue0(8800003) == 6L ? 2430 : 1420, (short)0, (actionAndDir & 1) != 0);
                                 }

                                 mob.getMap().broadcastMessage(MobPacket.setAttackZakumArm(mob.getObjectId(), monster.getCustomValue0(8800003) == 6L ? 1 : 0));
                              }
                           }
                        }
                        break label247;
                     }

                     mob = (MapleMonster)var17.next();
                  } while((mob.getId() < 8800002 || mob.getId() > 8800010) && (mob.getId() < 8800102 || mob.getId() > 8800110));

                  ++mobsize;
               }
            } else if (changephase) {
               List<MapleNodes.Environment> envs = new ArrayList();
               var17 = monster.getMap().getNodez().getEnvironments().iterator();

               while(var17.hasNext()) {
                  MapleNodes.Environment env = (MapleNodes.Environment)var17.next();
                  if (env.getName().contains("zdc")) {
                     env.setShow(true);
                     envs.add(env);
                  }
               }

               monster.getMap().broadcastMessage(CField.getUpdateEnvironment(envs));
               Timer.MapTimer.getInstance().schedule(() -> {
                  int monsterid = 8800117;
                  monster.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(monsterid), new Point(-60, 86));
               }, 1500L);
               monster.setCustomInfo(8800002, 0, 3000);
            }
         } else if (monster.getPhase() == 2) {
            int mobid = 8800000;
            if (monster.getId() == 8800102) {
               mobid += 100;
            }

            MapleMonster firstmob = null;
            MapleMonster secondmob = null;
            sized = 0;

            Iterator var20;
            MapleMonster mob;
            while(firstmob == null || secondmob == null) {
               randMob = Randomizer.rand(3, 5);
               var20 = monster.getMap().getAllMonster().iterator();

               while(var20.hasNext()) {
                  mob = (MapleMonster)var20.next();
                  if (mobid + randMob == mob.getId()) {
                     firstmob = mob;
                  } else if (mobid + randMob + 4 == mob.getId()) {
                     secondmob = mob;
                  }
               }

               if (firstmob != null && secondmob != null) {
                  MobSkill msi = firstmob.getStats().getSkill(176, 27);
                  msi.setMobSkillDelay(chr, firstmob, 1800, (short)0, (actionAndDir & 1) != 0);
                  firstmob.getMap().broadcastMessage(MobPacket.setAttackZakumArm(firstmob.getObjectId(), 2));
                  msi = secondmob.getStats().getSkill(176, 27);
                  msi.setMobSkillDelay(chr, secondmob, 1800, (short)0, (actionAndDir & 1) != 0);
                  firstmob.getMap().broadcastMessage(MobPacket.setAttackZakumArm(secondmob.getObjectId(), 2));
                  break;
               }

               ++sized;
               if (sized == 100) {
                  break;
               }
            }

            monster.addSkillCustomInfo(8800004, 1L);
            if (monster.getCustomValue0(8800004) == 11L) {
               monster.removeCustomInfo(8800004);
               List<MapleNodes.Environment> envs = new ArrayList();
               var20 = monster.getMap().getNodez().getEnvironments().iterator();

               while(var20.hasNext()) {
                  MapleNodes.Environment env = (MapleNodes.Environment)var20.next();
                  if (env.getName().contains("zdc")) {
                     env.setShow(false);
                     envs.add(env);
                  }
               }

               monster.getMap().broadcastMessage(CField.getUpdateEnvironment(envs));
               var20 = monster.getMap().getAllMonster().iterator();

               label185:
               while(true) {
                  while(true) {
                     if (!var20.hasNext()) {
                        break label185;
                     }

                     mob = (MapleMonster)var20.next();
                     if (mob.getId() >= 8800002 && mob.getId() <= 8800010 || mob.getId() >= 8800102 && mob.getId() <= 8800110) {
                        mob.setPhase((byte)1);
                        mob.getMap().broadcastMessage(MobPacket.changePhase(mob));
                     } else if (mob.getId() == 8800117 || mob.getId() == 8800120) {
                        mob.getMap().killMonster(mob.getId());
                     }
                  }
               }
            }

            monster.setCustomInfo(8800002, 0, 5000);
         }
      }

   }

   public static void magnusHandler(MapleMonster monster, int type, int actionAndDir) {
      if (monster != null) {
         int count;
         if (type == 0) {
            int[] types = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
            List<Obstacle> obs = new ArrayList();
            int size = Randomizer.rand(monster.getPhase() == 1 ? 5 : 3 * monster.getPhase(), 8 + monster.getPhase());

            for(count = 0; count < size; ++count) {
               int obtype = types[Randomizer.nextInt(types.length)];
               int x = Randomizer.rand(550, 3050);
               Obstacle ob;
               if (type <= 5) {
                  ob = new Obstacle(obtype, new Point(x, -2000), new Point(x, -1347), 25, monster.getId() == 8880010 ? 10 : 50, 1459, Randomizer.rand(80, 230), 1, 653, 0);
               } else if (type <= 7) {
                  ob = new Obstacle(obtype, new Point(x, -2000), new Point(x, -1347), 45, monster.getId() == 8880010 ? 30 : 100, 1481, Randomizer.rand(80, 230), 1, 653, 0);
               } else {
                  ob = new Obstacle(obtype, new Point(x, -2000), new Point(x, -1347), 65, monster.getId() == 8880010 ? 50 : 100, 542, Randomizer.rand(50, 270), 2, 653, 0);
               }

               obs.add(ob);
            }

            monster.getMap().broadcastMessage(MobPacket.createObstacle(monster, obs, (byte)0));
         } else if (type == 1) {
            if (monster.getCustomValue(monster.getId()) == null) {
               int time = Randomizer.rand(20000, 40000);
               int objtype = Randomizer.rand(1, 2);
               int[] randlist = new int[]{1, 3, 4, 5};
               if (objtype == 1) {
                  count = Randomizer.rand(1, 3);
                  int i = 0;
                  List<Integer> listed = new ArrayList();
                  listed.add(1);
                  listed.add(3);
                  listed.add(4);
                  listed.add(5);
                  Collections.shuffle(listed);
                  Iterator var18 = listed.iterator();

                  while(var18.hasNext()) {
                     Integer a = (Integer)var18.next();
                     int[] randlist2 = new int[]{a};
                     int delay = Randomizer.isSuccess(50) ? 0 : Randomizer.rand(1000, 1100);
                     if (delay > 0) {
                        Timer.MapTimer.getInstance().schedule(() -> {
                           monster.getMap().broadcastMessage(CField.DebuffObjON(randlist2, monster.getId() == 8880000));
                        }, (long)delay);
                     } else {
                        monster.getMap().broadcastMessage(CField.DebuffObjON(randlist2, monster.getId() == 8880000));
                     }

                     ++i;
                     if (i == count) {
                        break;
                     }
                  }
               } else if (objtype == 2) {
                  monster.getMap().broadcastMessage(CField.DebuffObjON(randlist, monster.getId() == 8880000));
               }

               monster.setCustomInfo(monster.getId(), 0, monster.getPhase() > 0 ? time - monster.getPhase() * 1000 : 30000);
            }
         } else if (type == 2) {
            if (actionAndDir == 27 || actionAndDir == 26) {
               monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 5, 1, "매그너스가 주변의 적들을 뿌리치려 합니다."));
            }

            if (actionAndDir == 34 || actionAndDir == 35) {
               monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 5, 1, "매그너스가 남은 적을 처리하기 위해 연속 공격을 시전합니다."));
            }

            if (actionAndDir == 60) {
               monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 5, 1, "매그너스가 느려진 적들을 향해 강력한 일격을 준비 합니다."));
            }

            if (actionAndDir == 62) {
               monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 5, 1, "매그너스가 회복/강화 하는 적을 보고 대응책을 준비합니다."));
            }

            AggressIve(monster);
         }
      }

   }

   public static void HillaHandler(MapleMonster monster, MapleCharacter chr, int actionAndDir) {
      if (actionAndDir != -1) {
         if (monster.getId() == 8870100) {
            if (actionAndDir != 76 && actionAndDir != 77) {
               if (actionAndDir == 36 || actionAndDir == 37) {
                  chr.getMap().broadcastMessage(MobPacket.SpeakingMonster(monster, 1, 0));
               }
            } else {
               chr.getMap().broadcastMessage(MobPacket.SpeakingMonster(monster, 3, 0));
            }
         } else if (actionAndDir != 30 && actionAndDir != 31) {
            if (actionAndDir == 34 || actionAndDir == 35) {
               chr.getMap().broadcastMessage(MobPacket.SpeakingMonster(monster, 1, 0));
            }
         } else {
            chr.getMap().broadcastMessage(MobPacket.SpeakingMonster(monster, 0, 0));
         }
      }

   }

   public static void lotusHandler(MapleMonster monster) {
      if (monster != null) {
         boolean hard = monster.getId() == 8950000 || monster.getId() == 8950001 || monster.getId() == 8950002;
         int[] var10000 = new int[]{48, 49, 50, 51, 52};
         List<Obstacle> obs = new ArrayList();
         int aa = monster.getId() % 10;
         int size = Randomizer.rand(1, 4);
         if (aa == 1) {
            size = Randomizer.rand(2, 5);
         } else if (aa == 2) {
            size = Randomizer.rand(2, 5);
         }

         List<Pair<Integer, Integer>> random = new ArrayList();
         if (aa == 1) {
            random.add(new Pair(48, hard ? 2000 : 4000));
            random.add(new Pair(49, hard ? 3000 : 3000));
            random.add(new Pair(50, hard ? 3000 : 3000));
            random.add(new Pair(51, hard ? 4000 : 2000));
         } else if (aa == 2) {
            random.add(new Pair(48, hard ? 2000 : 2000));
            random.add(new Pair(49, hard ? 2000 : 3000));
            random.add(new Pair(50, hard ? 2000 : 2000));
            random.add(new Pair(51, hard ? 3000 : 2500));
            random.add(new Pair(52, hard ? 1000 : 500));
         } else {
            random.add(new Pair(48, hard ? 3000 : 5000));
            random.add(new Pair(49, hard ? 3000 : 3000));
            random.add(new Pair(50, hard ? 4000 : 2000));
         }

         for(int i = 0; i < size; ++i) {
            int type = GameConstants.getWeightedRandom(random);
            int x = Randomizer.rand(-600, 650);
            Obstacle ob;
            if (type == 48) {
               ob = new Obstacle(48, new Point(x, -520), new Point(x, -16), 36, 10, Randomizer.rand(0, 1000), Randomizer.rand(250, 450), 1, 504, 0);
               if (!hard) {
                  ob.setEffect(false);
               }
            } else if (type == 49) {
               ob = new Obstacle(49, new Point(x, -520), new Point(x, -16), 51, hard ? 20 : 10, Randomizer.rand(0, 1000), Randomizer.rand(200, 350), 1, 504, 0);
            } else if (type == 50) {
               ob = new Obstacle(50, new Point(x, -520), new Point(x, -16), 51, hard ? 30 : 20, Randomizer.rand(0, 1000), Randomizer.rand(100, 200), 2, 504, 0);
            } else if (type == 51) {
               ob = new Obstacle(51, new Point(x, -520), new Point(x, -16), 65, hard ? 50 : 40, Randomizer.rand(0, 1000), Randomizer.rand(100, 200), 2, 504, 0);
            } else {
               ob = new Obstacle(52, new Point(x, -520), new Point(x, -16), 190, 100, 190, Randomizer.rand(150, 250), 1, 504, 0);
            }

            obs.add(ob);
         }

         monster.getMap().CreateObstacle(monster, obs);
      }

   }

   public static void duskHandler(MapleMonster dusk, MapleMap map) {
      map.broadcastMessage(SLFCGPacket.OnYellowDlg(0, 3000, "촉수가 눈을 방어하고 있어 제대로 된 피해를 주기 힘들겠군.", ""));
      map.broadcastMessage(CField.enforceMSG("점차 공포가 차올라 있을 수 없는 것이 보이게 됩니다! 견디지 못하면 공포가 전이되니 주의하세요!", 250, 3000));
      dusk.setPhase((byte)1);
      dusk.setLastSpecialAttackTime(System.currentTimeMillis());
      Iterator var2 = map.getCharacters().iterator();

      while(var2.hasNext()) {
         MapleCharacter cchr = (MapleCharacter)var2.next();
         cchr.setDuskGauge(0);
      }

      dusk.setSchedule(Timer.MobTimer.getInstance().register(() -> {
         if (map.getId() == 450009400 || map.getId() == 450009450) {
            long time = System.currentTimeMillis();
            MapleMonster att = null;
            if (map.getId() == 450009400) {
               att = map.getMonsterById(8644658);
            } else if (map.getId() == 450009450) {
               att = map.getMonsterById(8644659);
            }

            if (dusk != null) {
               if (map.getCharactersSize() <= 0) {
                  map.removeMapObject(dusk);
                  dusk.killed();
               }

               MapleCharacter cchr;
               int size;
               for(Iterator var5 = map.getCharacters().iterator(); var5.hasNext(); cchr.getClient().getSession().writeAndFlush(MobPacket.BossDusk.handleDuskGauge(cchr.isDuskBlind(), cchr.getDuskGauge(), 1000))) {
                  cchr = (MapleCharacter)var5.next();
                  if (!cchr.isDuskBlind()) {
                     cchr.setDuskGauge(Math.min(1000, cchr.getDuskGauge() + 5));
                     if (cchr.getDuskGauge() >= 1000) {
                        SkillFactory.getSkill(80002902).getEffect(1).applyTo(cchr);
                        cchr.setDuskBlind(true);
                     }
                  } else {
                     MapleMonster m;
                     if (time - cchr.getLastSpawnBlindMobTime() >= 3000L) {
                        for(size = 0; size < 3; ++size) {
                           m = MapleLifeFactory.getMonster(8644653);
                           MapleMonster mob2 = MapleLifeFactory.getMonster(8644653);
                           m.setOwner(cchr.getId());
                           mob2.setOwner(cchr.getId());
                           map.spawnMonsterOnGroundBelow(m, new Point(Randomizer.rand(-650, 650), Randomizer.rand(-500, -200)));
                           map.spawnMonsterOnGroundBelow(mob2, new Point(Randomizer.rand(-650, 650), -157));
                           cchr.setLastSpawnBlindMobTime(time);
                        }
                     }

                     cchr.setDuskGauge(Math.max(0, cchr.getDuskGauge() - 40));
                     if (cchr.getDuskGauge() <= 0) {
                        Iterator var16 = map.getAllMonstersThreadsafe().iterator();

                        while(var16.hasNext()) {
                           m = (MapleMonster)var16.next();
                           if (m.getOwner() == cchr.getId()) {
                              m.setHp(0L);
                              map.broadcastMessage(MobPacket.killMonster(m.getObjectId(), 1));
                              map.removeMapObject(m);
                              m.killed();
                           }
                        }

                        cchr.cancelEffectFromBuffStat(SecondaryStat.DuskDarkness, 80002902);
                        cchr.setDuskBlind(false);
                     }
                  }
               }

               if (dusk.getPhase() == 1 && time - dusk.getLastSpecialAttackTime() >= 60000L) {
                  dusk.setPhase((byte)0);
                  dusk.setUseSpecialSkill(true);
                  dusk.setLastSpecialAttackTime(time);
                  map.broadcastMessage(MobPacket.changeMobZone(dusk));
                  map.broadcastMessage(MobPacket.BossDusk.spawnTempFoothold());
                  map.broadcastMessage(CField.enforceMSG("방어하던 촉수로 강력한 공격을 할거예요! 버텨낸다면 드러난 공허의 눈을 공격할 수 있어요!", 250, 3000));
               }

               if (dusk.getPhase() == 0) {
                  if (time - dusk.getLastSpecialAttackTime() >= 25000L && dusk.isUseSpecialSkill()) {
                     MobSkill msi = MobSkillFactory.getMobSkill(186, 11);
                     msi.applyEffect((MapleCharacter)null, dusk, true, true);
                     if (map.getId() == 450009450) {
                        MobSkill msi2 = MobSkillFactory.getMobSkill(213, 10);
                        msi2.applyEffect((MapleCharacter)null, dusk, true, true);
                     }

                     dusk.setNextSkill(213);
                     dusk.setNextSkillLvl(10);
                     dusk.setUseSpecialSkill(false);
                     att.setLastSeedCountedTime(time);
                     map.broadcastMessage(MobPacket.moveMonsterResponse(dusk.getObjectId(), (short)0, dusk.getMp(), true, dusk.getNextSkill(), dusk.getNextSkillLvl(), 0));
                  }

                  if (time - dusk.getLastSpecialAttackTime() >= 35000L) {
                     dusk.setPhase((byte)1);
                     map.broadcastMessage(MobPacket.changeMobZone(dusk));
                     dusk.setLastSpecialAttackTime(time);
                  }
               }

               if (att != null && dusk.getPhase() == 1 && !dusk.isUseSpecialSkill()) {
                  if (time - att.getLastSeedCountedTime() >= 10000L) {
                     map.broadcastMessage(MobPacket.BossDusk.spawnDrillAttack(Randomizer.rand(-650, 650), Randomizer.nextInt(100) < 50));
                     att.setLastSeedCountedTime(time);
                  }

                  if (time - att.getLastSpecialAttackTime() >= 8000L) {
                     map.broadcastMessage(MobPacket.enableOnlyFsmAttack(att, 2, 0));
                     att.setLastSpecialAttackTime(time);
                  }
               }

               if (time - dusk.getLastStoneTime() >= 3000L) {
                  int[] types = new int[]{65, 66, 67};
                  List<Obstacle> obs = new ArrayList();
                  size = Randomizer.rand(3, 10);

                  for(int i = 0; i < size; ++i) {
                     int type = types[Randomizer.nextInt(types.length)];
                     int x = Randomizer.rand(-654, 652);
                     Obstacle ob;
                     if (type == 65) {
                        ob = new Obstacle(type, new Point(x, -1055), new Point(x, -157), 24, map.getId() == 450009450 ? 30 : 15, 157, Randomizer.rand(300, 850), 1, 898, 0);
                     } else if (type == 66) {
                        ob = new Obstacle(type, new Point(x, -1055), new Point(x, -157), 24, map.getId() == 450009450 ? 30 : 15, 185, Randomizer.rand(300, 850), 1, 898, 0);
                     } else if (type == 67) {
                        ob = new Obstacle(type, new Point(x, -1055), new Point(x, -157), 24, map.getId() == 450009450 ? 30 : 15, 718, Randomizer.rand(300, 850), 1, 898, 0);
                     } else {
                        ob = new Obstacle(type, new Point(x, -1055), new Point(x, -157), 24, map.getId() == 450009450 ? 30 : 15, 718, Randomizer.rand(300, 850), 1, 898, 0);
                     }

                     obs.add(ob);
                  }

                  dusk.getMap().broadcastMessage(MobPacket.createObstacle(dusk, obs, (byte)0));
                  dusk.setLastStoneTime(time);
               }
            }
         }

      }, 500L));
   }

   public static void DemianStigmaGive(MapleMonster monster) {
      if (monster != null) {
         String[] EffectMsgs = new String[]{"데미안이 가장 위협적인 적에게 낙인을 새깁니다.", "데미안이 낙인이 가장 많은 적에게 낙인을 새깁니다.", "데미안이 낙인이 가장 적은 적에게 낙인을 새깁니다.", "데미안이 누구에게 낙인을 새길지 알 수 없습니다."};
         int time = monster.getHPPercent() >= 50 ? 28000 : 18000;
         monster.setStigmaType(Randomizer.rand(0, 3));
         monster.getMap().broadcastMessage(CField.StigmaTime(time));
         monster.getMap().broadcastMessage(CField.enforceMSG(EffectMsgs[monster.getStigmaType()], 216, 30000000));
         monster.setSpecialtxt(EffectMsgs[monster.getStigmaType()]);
         Timer.MobTimer.getInstance().schedule(() -> {
            int size = monster.getMap().getAllCharactersThreadsafe().size();
            if (size > 0) {
               MapleCharacter chr = null;
               if (monster.getStigmaType() < 3) {
                  chr = RandCharacter(monster, monster.getStigmaType(), monster.getStigmaType() != 2);
               }

               if (chr == null) {
                  chr = (MapleCharacter)monster.getMap().getAllCharactersThreadsafe().get(Randomizer.nextInt(size));
               }

               if (chr != null) {
                  MobSkill ms = MobSkillFactory.getMobSkill(237, 1);
                  ms.applyEffect(chr, monster, true, monster.isFacingLeft());
               }

               if (monster.isAlive()) {
                  DemianStigmaGive(monster);
               }
            }

         }, (long)time);
      }
   }

   public static MapleCharacter RandCharacter(MapleMonster monster, int type, boolean first) {
      MapleCharacter chr = null;
      List<Pair<String, Long>> a2 = new ArrayList();
      Iterator var5 = monster.getController().getParty().getMembers().iterator();

      while(var5.hasNext()) {
         MaplePartyCharacter c = (MaplePartyCharacter)var5.next();
         if (c.getPlayer() != null) {
            long value = type == 0 ? c.getPlayer().getAggressiveDamage() : (long)c.getPlayer().Stigma;
            a2.add(new Pair(c.getName(), value));
         }
      }

      for(int i = 0; i < a2.size() - 1; ++i) {
         for(int j = 0; j < a2.size() - i - 1; ++j) {
            if ((Long)((Pair)a2.get(j)).getRight() < (Long)((Pair)a2.get(j + 1)).getRight()) {
               String chridtmp = (String)((Pair)a2.get(j + 1)).getLeft();
               long chrpointtmp = (Long)((Pair)a2.get(j + 1)).getRight();
               a2.set(j + 1, (Pair)a2.get(j));
               a2.set(j, new Pair(chridtmp, chrpointtmp));
            }
         }
      }

      if (first) {
         chr = monster.getMap().getCharacterByName((String)((Pair)a2.get(0)).getLeft());
      } else {
         chr = monster.getMap().getCharacterByName((String)((Pair)a2.get(a2.size() - 1)).getLeft());
      }

      return chr;
   }

   public static void demianHandler(MapleMonster monster) {
      if (monster.getId() == 8880100 || monster.getId() == 8880110 || monster.getId() == 8880101 || monster.getId() == 8880111) {
         DemianStigmaGive(monster);
      }

   }

   public static void pierreHandler(MapleMonster monster) {
      monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
         if ((monster.getEventInstance() == null || !monster.isAlive()) && monster.getMap().getCharacters().size() <= 0) {
            monster.getSchedule().cancel(true);
            monster.setSchedule((ScheduledFuture)null);
            monster.getMap().killMonsterType(monster, 0);
         } else if (monster.getSeperateSoul() <= 0) {
            if (monster.getCustomValue(111645) == null) {
               List<Point> pos = new ArrayList();
               int type = Randomizer.rand(1, 2);

               for(int i = 0; i < 11; ++i) {
                  if (type == 1) {
                     if (Randomizer.isSuccess(50)) {
                        pos.add(new Point(-400 + i * 180, 551));
                     }
                  } else if (type == 2 && Randomizer.isSuccess(50)) {
                     pos.add(new Point(-310 + i * 180, 551));
                  }
               }

               monster.getMap().broadcastMessage(MobPacket.dropStone("CapEffect", pos));
               monster.setCustomInfo(111645, 0, Randomizer.rand(5000, 11000));
            }

            boolean Chaos = monster.getId() % 1000 < 100;
            Transform trans = null;
            if (Chaos) {
               trans = monster.getStats().getTrans();
            }

            String monstercolor = monster.getId() % 10 == 0 ? "Purple" : (monster.getId() % 10 == 1 ? "Red" : "Blue");
            int time = Chaos ? trans.getDuration() : (monstercolor.equals("Purple") ? 10 : 20);
            if (System.currentTimeMillis() - monster.lastCapTime >= (long)(time * 1000) && monster.lastCapTime != 0L || monster.lastCapTime == 1L) {
               int TransFormHpPercent = 70;
               int DevideHpPercent = 31;
               boolean Trans = monster.getHPPercent() <= TransFormHpPercent;
               boolean Devide = monster.getHPPercent() <= DevideHpPercent;
               if (!Chaos) {
                  Devide = false;
                  Trans = true;
               }

               if (monster.getCustomValue0(8900000) == 1L) {
                  Devide = true;
                  Trans = true;
               }

               MapleMonster copy;
               if (Trans && !Devide) {
                  if ((monstercolor.equals("Purple") || monstercolor.equals("Red") || monstercolor.equals("Blue")) && TransFormHpPercent >= monster.getHPPercent()) {
                     copy = MapleLifeFactory.getMonster(Randomizer.nextBoolean() ? monster.getId() + 1 : monster.getId() + 2);
                     if (monstercolor.equals("Red") || monstercolor.equals("Blue")) {
                        int minus = monstercolor.equals("Red") ? 1 : 2;
                        copy = MapleLifeFactory.getMonster(monster.getId() - minus);
                     }

                     copy.setHp(monster.getHp());
                     copy.lastCapTime = System.currentTimeMillis();
                     if (monster.getEventInstance() != null) {
                        monster.getEventInstance().registerMonster(copy);
                     }

                     monster.getMap().spawnMonsterWithEffect(copy, 254, monster.getTruePosition());
                     monster.getMap().killMonster(monster, monster.getController(), false, false, (byte)0);
                     monster.getMap().broadcastMessage(MobPacket.showBossHP(monster.getId(), -1L, 0L));
                     Iterator var24 = copy.getMap().getAllCharactersThreadsafe().iterator();

                     while(var24.hasNext()) {
                        MapleCharacter chr = (MapleCharacter)var24.next();
                        Pair<Integer, Integer> skill = new Pair(Randomizer.rand(189, 190), 1);
                        if (chr.isAlive()) {
                           chr.cancelDisease(SecondaryStat.CapDebuff);
                           if (chr.getBuffedValue(SecondaryStat.NotDamaged) == null && chr.getBuffedValue(SecondaryStat.IndieNotDamaged) == null) {
                              MobSkillFactory.getMobSkill((Integer)skill.left, (Integer)skill.right).applyEffect(chr, copy, true, copy.isFacingLeft());
                           }
                        }
                     }
                  }
               } else if (Trans && Devide && Chaos) {
                  Point mobpos2;
                  if (monster.getCustomValue(8900000) != null && monster.getCustomValue0(8900000) != 0L) {
                     if (monster.getCustomValue0(8900000) == 1L) {
                        copy = MapleLifeFactory.getMonster(monster.getId() + (monstercolor.equals("Red") ? 1 : -1));
                        List<Pair<MonsterStatus, MonsterStatusEffect>> stats = new ArrayList();
                        stats.add(new Pair(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(187, 210000000, 70L)));
                        stats.add(new Pair(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(187, 210000000, 70L)));
                        stats.add(new Pair(MonsterStatus.MS_Pad, new MonsterStatusEffect(187, 210000000, 17000L)));
                        stats.add(new Pair(MonsterStatus.MS_Mad, new MonsterStatusEffect(187, 210000000, 17000L)));
                        if (copy != null) {
                           copy.setCustomInfo(8900000, 1, 0);
                           copy.lastCapTime = System.currentTimeMillis();
                           copy.setHp(monster.getHp());
                           int size = 0;
                           int mobid = 0;
                           Iterator var28 = monster.getMap().getAllMonster().iterator();

                           label197:
                           while(true) {
                              MapleMonster m;
                              do {
                                 if (!var28.hasNext()) {
                                    if (size <= 1) {
                                       MapleMonster other = MapleLifeFactory.getMonster(mobid == 8900001 ? 8900001 : 8900002);
                                       other.lastCapTime = System.currentTimeMillis();
                                       other.setCustomInfo(8900000, 1, 0);
                                       if (copy.getId() == other.getId()) {
                                          other = MapleLifeFactory.getMonster(copy.getId() == 8900001 ? 8900002 : 8900001);
                                       }

                                       mobpos2 = new Point(monster.getTruePosition().x - 150, monster.getTruePosition().y);
                                       if (monster.getMap().getFootholds().findBelow(mobpos2) == null) {
                                          mobpos2 = monster.getPosition();
                                       }

                                       other.setHp(monster.getStats().getHp() * 10L / 100L);
                                       monster.getMap().spawnMonsterWithEffect(other, 254, mobpos2);
                                       if (other != null) {
                                          other.applyMonsterBuff(other.getMap(), stats, MobSkillFactory.getMobSkill(187, 1));
                                       }
                                    }

                                    if (monster.getEventInstance() != null) {
                                       monster.getEventInstance().registerMonster(copy);
                                    }

                                    monster.getMap().spawnMonsterWithEffect(copy, 254, monster.getPosition());
                                    copy.applyMonsterBuff(copy.getMap(), stats, MobSkillFactory.getMobSkill(187, 1));
                                    monster.getMap().killMonster(monster, monster.getController(), false, false, (byte)0);
                                    monster.getMap().broadcastMessage(MobPacket.showBossHP(monster.getId(), -1L, 0L));
                                    var28 = copy.getMap().getAllCharactersThreadsafe().iterator();

                                    while(var28.hasNext()) {
                                       MapleCharacter chrx = (MapleCharacter)var28.next();
                                       Pair<Integer, Integer> skillx = (Pair)trans.getSkills().get(Randomizer.nextInt(trans.getSkills().size()));
                                       if (chrx.isAlive()) {
                                          chrx.cancelDisease(SecondaryStat.CapDebuff);
                                          if (chrx.getBuffedValue(SecondaryStat.NotDamaged) == null && chrx.getBuffedValue(SecondaryStat.IndieNotDamaged) == null) {
                                             MobSkillFactory.getMobSkill((Integer)skillx.left, (Integer)skillx.right).applyEffect(chrx, copy, true, copy.isFacingLeft());
                                          }
                                       }
                                    }
                                    break label197;
                                 }

                                 m = (MapleMonster)var28.next();
                              } while(m.getId() != 8900001 && m.getId() != 8900002);

                              ++size;
                              mobid = m.getId();
                           }
                        }
                     }
                  } else {
                     copy = MapleLifeFactory.getMonster(8900001);
                     MapleMonster copy2 = MapleLifeFactory.getMonster(8900002);
                     copy.setCustomInfo(8900000, 1, 0);
                     copy2.setCustomInfo(8900000, 1, 0);
                     copy.lastCapTime = System.currentTimeMillis();
                     copy2.lastCapTime = System.currentTimeMillis();
                     long hp = monster.getHp() <= 0L ? monster.getStats().getHp() * 10L / 100L : monster.getHp();
                     copy.setHp(hp);
                     copy2.setHp(hp);
                     Point mobpos = new Point(monster.getTruePosition().x + 150, monster.getTruePosition().y);
                     if (monster.getMap().getFootholds().findBelow(mobpos) == null) {
                        mobpos = monster.getPosition();
                     }

                     mobpos2 = new Point(monster.getTruePosition().x - 150, monster.getTruePosition().y);
                     if (monster.getMap().getFootholds().findBelow(mobpos2) == null) {
                        mobpos2 = monster.getPosition();
                     }

                     if (monster.getEventInstance() != null) {
                        monster.getEventInstance().registerMonster(copy);
                        monster.getEventInstance().registerMonster(copy2);
                     }

                     monster.getMap().spawnMonsterWithEffect(copy, 254, mobpos);
                     monster.getMap().spawnMonsterWithEffect(copy2, 254, mobpos2);
                     List<Pair<MonsterStatus, MonsterStatusEffect>> statsx = new ArrayList();
                     statsx.add(new Pair(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(187, 210000000, 70L)));
                     statsx.add(new Pair(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(187, 210000000, 70L)));
                     statsx.add(new Pair(MonsterStatus.MS_Pad, new MonsterStatusEffect(187, 210000000, 17000L)));
                     statsx.add(new Pair(MonsterStatus.MS_Mad, new MonsterStatusEffect(187, 210000000, 17000L)));
                     copy.applyMonsterBuff(copy.getMap(), statsx, MobSkillFactory.getMobSkill(187, 1));
                     copy2.applyMonsterBuff(copy2.getMap(), statsx, MobSkillFactory.getMobSkill(187, 1));
                     monster.getMap().killMonster(monster, monster.getController(), false, false, (byte)0);
                     monster.getMap().broadcastMessage(MobPacket.showBossHP(monster.getId(), -1L, 0L));
                     Iterator var16 = copy.getMap().getAllCharactersThreadsafe().iterator();

                     while(var16.hasNext()) {
                        MapleCharacter chrxx = (MapleCharacter)var16.next();
                        Pair<Integer, Integer> skillxx = (Pair)trans.getSkills().get(Randomizer.nextInt(trans.getSkills().size()));
                        if (chrxx.isAlive()) {
                           chrxx.cancelDisease(SecondaryStat.CapDebuff);
                           if (chrxx.getBuffedValue(SecondaryStat.NotDamaged) == null && chrxx.getBuffedValue(SecondaryStat.IndieNotDamaged) == null) {
                              MobSkillFactory.getMobSkill((Integer)skillxx.left, (Integer)skillxx.right).applyEffect(chrxx, copy, true, copy.isFacingLeft());
                           }
                        }
                     }
                  }
               }
            }

         }
      }, 1000L));
   }

   public static void blackMageHandler(MapleMonster monster) {
      monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
         if ((monster.getEventInstance() == null || !monster.isAlive()) && monster.getMap().getCharacters().size() <= 0) {
            monster.getSchedule().cancel(true);
            monster.setSchedule((ScheduledFuture)null);
            monster.getMap().killMonsterType(monster, 0);
         } else {
            MapleMap map = monster.getMap();
            Iterator var2 = map.getAllChracater().iterator();

            MapleCharacter chr;
            Iterator chrs;
            MapleMonster mobx;
            while(var2.hasNext()) {
               chr = (MapleCharacter)var2.next();
               if (chr.hasDisease(SecondaryStat.CurseOfCreation) && chr.isAlive()) {
                  chr.addHP(chr.getStat().getCurrentMaxHp() / 100L * 4L);
                  chr.addMP(chr.getStat().getCurrentMaxMp(chr) / 100L * 4L);
               }

               if (chr.getMapId() == 450013100 && chr.getMap().getMobsSize(8880500) > 0 && chr.getMap().getMobsSize(8880501) > 0 || chr.getMapId() == 450013300 && chr.getMap().getMobsSize(8880502) > 0 || chr.getMapId() == 450013500 && chr.getMap().getMobsSize(8880503) > 0 || chr.getMapId() == 450013700 && chr.getMap().getMobsSize(8880504) > 0) {
                  if (chr.getMapId() != 450013700 && chr.getMapId() != 450013500 && chr.getSkillCustomValue(45001310) == null && map.getCustomValue0(45001316) == 0) {
                     chr.setSkillCustomInfo(45001310, 0L, (long)Randomizer.rand(7000, 11000));
                     chrs = chr.getMap().getAllMonster().iterator();

                     while(chrs.hasNext()) {
                        mobx = (MapleMonster)chrs.next();
                        if (mobx.getId() == 8880502) {
                           map.broadcastMessage(MobPacket.setAttackZakumArm(mobx.getObjectId(), 0));
                           break;
                        }
                     }

                     chr.getClient().send(CField.getFieldSkillAdd(100007, Randomizer.rand(1, 3), false));
                  }

                  if (chr.getSkillCustomValue(45001311) == null && chr.hasDisease(SecondaryStat.CurseOfCreation)) {
                     chr.setSkillCustomInfo(45001311, 0L, (long)Randomizer.rand(1000, 3000));
                     if (chr.isAlive()) {
                        chr.getClient().send(CField.getFieldSkillAdd(100015, chr.getMapId() == 450013700 ? 2 : 1, false));
                     }
                  }
               }
            }

            int mobid;
            Iterator var13;
            if (map.getCustomValue0(45001317) > 0) {
               mobid = map.getCustomValue0(45001317);
               int bx = 0;
               int afterx = 0;
               switch(mobid) {
               case 1:
                  bx = -105;
                  afterx = 158;
                  break;
               case 2:
                  bx = -980;
                  afterx = -781;
                  break;
               case 3:
                  bx = 789;
                  afterx = 950;
               }

               var13 = map.getAllCharactersThreadsafe().iterator();

               label493:
               while(true) {
                  MapleCharacter chr2;
                  do {
                     if (!var13.hasNext()) {
                        break label493;
                     }

                     chr2 = (MapleCharacter)var13.next();
                  } while(chr2.getTruePosition().x > bx && afterx > chr2.getTruePosition().x && chr2.getPosition().y > -168);

                  if (chr2.getBuffedEffect(SecondaryStat.NotDamaged) == null && chr2.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null) {
                     chr2.getPercentDamage(monster, 999, 999, 12500, true);
                  }
               }
            } else if (map.getCustomValue0(45001360) == 1) {
               var2 = map.getAllCharactersThreadsafe().iterator();

               while(var2.hasNext()) {
                  chr = (MapleCharacter)var2.next();
                  if (chr.getPosition().y == 85 && chr.getMapId() == 450013500 && chr.isAlive() && chr.getBuffedEffect(SecondaryStat.NotDamaged) == null && chr.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null) {
                     chr.addHP(-chr.getStat().getCurrentMaxHp() * 10L);
                     chr.getClient().send(CField.DamagePlayer2((int)chr.getStat().getCurrentMaxHp() * 10));
                  }
               }
            }

            ArrayList skillinfox;
            int type;
            ArrayList skillinfo;
            if (map.getId() == 450013100 && map.getMobsSize(8880500) > 0 && map.getMobsSize(8880501) > 0 || map.getId() == 450013300 && map.getMobsSize(8880502) > 0 || map.getId() == 450013500 && map.getMobsSize(8880503) > 0 && map.getCharactersSize() > 0) {
               Point posx;
               if (map.getCustomValue0(45001316) == 0 && map.getCustomValue(45001310) == null) {
                  mobid = Randomizer.rand(1, 2);
                  skillinfo = new ArrayList();
                  map.setCustomInfo(45001310, 0, 60000);

                  for(type = 0; type < (map.getRight() - map.getLeft()) / 150; ++type) {
                     Obstacle ob;
                     if (mobid == 1) {
                        ob = new Obstacle(75, new Point(map.getRight() - type * 150, -540), new Point(map.getRight() - 300, 16), 50, 50, 0, type * 530, 82, 4, 573, 0);
                        skillinfo.add(ob);
                     } else {
                        ob = new Obstacle(75, new Point(map.getLeft() + type * 150, -540), new Point(map.getLeft() - 300, 16), 50, 50, 0, type * 530, 82, 4, 573, 0);
                        skillinfo.add(ob);
                     }
                  }

                  map.CreateObstacle(monster, skillinfo);
               } else if (map.getCustomValue(45001311) == null && map.getId() == 450013100 && map.getMobsSize(8880500) > 0 && map.getMobsSize(8880501) > 0) {
                  map.setCustomInfo(45001311, 0, 63000);
                  map.broadcastMessage(CField.enforceMSG("불길한 붉은 번개가 내리쳐 움직임을 제한한다.", 265, 3000));
                  map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880506), new Point(-1600, 85));
                  map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880506), new Point(400, 85));
                  map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880506), new Point(-1000, 85));
                  map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880506), new Point(1000, 85));
                  map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880506), new Point(-400, 85));
                  map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880506), new Point(1600, 85));
                  Timer.MapTimer.getInstance().schedule(() -> {
                     Iterator var1 = map.getAllMonster().iterator();

                     while(var1.hasNext()) {
                        MapleMonster mob = (MapleMonster)var1.next();
                        if (mob.getId() == 8880506) {
                           map.killMonster(mob, -1);
                        }
                     }

                  }, 20000L);
               } else if (map.getCustomValue(45001315) == null && map.getId() == 450013300 && map.getMobsSize(8880502) > 0) {
                  boolean use = true;
                  int randt = 2;
                  chrs = map.getAllMonster().iterator();

                  while(chrs.hasNext()) {
                     mobx = (MapleMonster)chrs.next();
                     if (mobx.getId() == 8880502) {
                        if (mobx.isSkillForbid()) {
                           use = false;
                        } else {
                           map.broadcastMessage(MobPacket.UseSkill(mobx.getObjectId(), randt == 1 ? 11 : (randt == 2 ? 10 : 12)));
                        }
                        break;
                     }
                  }

                  if (use) {
                     map.setCustomInfo(45001315, 0, 75000);
                     map.setCustomInfo(45001316, randt, 0);
                     map.broadcastMessage(CField.enforceMSG("검은 마법사의 붉은 번개가 모든 곳을 뒤덮는다. 피할 곳을 찾아야 한다.", 265, 4000));
                     Timer.MapTimer.getInstance().schedule(() -> {
                        map.broadcastMessage(MobPacket.ShowBlackMageSkill(randt - 1));
                        map.getMonsterById(8880502).setSkillForbid(true);
                     }, 5000L);
                     Timer.MapTimer.getInstance().schedule(() -> {
                        map.setCustomInfo(45001317, randt, 0);
                     }, 6000L);
                     Timer.MapTimer.getInstance().schedule(() -> {
                        map.setCustomInfo(45001316, 0, 0);
                        map.setCustomInfo(45001317, 0, 0);
                     }, 15000L);
                     Timer.MapTimer.getInstance().schedule(() -> {
                        map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880516), new Point(-7, 88));
                        map.getMonsterById(8880502).setSkillForbid(false);
                        map.broadcastMessage(MobPacket.mobBarrierEffect(map.getMonsterById(8880502).getObjectId(), "UI/UIWindow8.img/BlackMageShield/mobEffect", "Sound/Etc.img/BlackMageShield", "UI/UIWindow8.img/BlackMageShield/mobEffect0"));
                        map.getMonsterById(8880502).gainShield(3000000000000L, true, 30);
                     }, 16000L);
                  }
               } else {
                  int typex;
                  if (map.getCustomValue(45001312) == null && map.getId() == 450013100 && map.getMobsSize(8880500) > 0 && map.getMobsSize(8880501) > 0) {
                     skillinfox = new ArrayList();
                     typex = map.getMobsSize(8880507) + map.getMobsSize(8880508);
                     int basex = 2172;
                     map.setCustomInfo(45001312, 0, 50000);
                     map.broadcastMessage(CField.enforceMSG("통곡의 장벽이 솟아올라 공간을 잠식한다.", 265, 3000));
                     if (typex < 16) {
                        skillinfox.add(new Point(-basex + map.getCustomValue0(45001313) * 174, 84));
                        skillinfox.add(new Point(basex - map.getCustomValue0(45001313) * 174, 84));
                        map.broadcastMessage(CField.getFieldSkillEffectAdd(100008, 1, skillinfox));
                     }

                     var13 = map.getAllMonster().iterator();

                     label381:
                     while(true) {
                        MapleMonster mobxx;
                        do {
                           if (!var13.hasNext()) {
                              Timer.MapTimer.getInstance().schedule(() -> {
                                 if (typex < 16) {
                                    map.broadcastMessage(CField.getFieldSkillAdd(100008, 1, true));
                                    map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880507), new Point(-basex + map.getCustomValue0(45001313) * 174, 85));
                                    map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880508), new Point(basex - map.getCustomValue0(45001313) * 174, 85));
                                 }

                                 map.setCustomInfo(45001313, map.getCustomValue0(45001313) + 1, 0);
                                 Iterator var3 = map.getAllMonster().iterator();

                                 while(true) {
                                    MapleMonster mob;
                                    do {
                                       if (!var3.hasNext()) {
                                          return;
                                       }

                                       mob = (MapleMonster)var3.next();
                                    } while(mob.getId() != 8880500 && mob.getId() != 8880501 && mob.getId() != 8880505);

                                    if (mob.getId() == 8880500 || mob.getId() == 8880501) {
                                       map.broadcastMessage(MobPacket.mobBarrierEffect(mob.getObjectId(), "UI/UIWindow8.img/BlackMageShield/mobEffect", "Sound/Etc.img/BlackMageShield", "UI/UIWindow8.img/BlackMageShield/mobEffect0"));
                                    }

                                    mob.gainShield(1000000000000L, true, 15);
                                 }
                              }, 3000L);
                              break label381;
                           }

                           mobxx = (MapleMonster)var13.next();
                        } while(mobxx.getId() != 8880501 && mobxx.getId() != 8880500);

                        int x = 350;
                        if (mobxx.getId() == 8880500) {
                           x *= -1;
                        }

                        mobxx.getMap().broadcastMessage(MobPacket.setAttackZakumArm(mobxx.getObjectId(), 0));
                        mobxx.setNextSkill(170);
                        mobxx.setNextSkillLvl(mobxx.getId() == 8880500 ? 62 : 64);
                        mobxx.getMap().broadcastMessage(MobPacket.moveMonsterResponse(mobxx.getObjectId(), (short)((int)mobxx.getCustomValue0(99991)), mobxx.getMp(), true, 170, mobxx.getId() == 8880500 ? 62 : 64, 0));
                        mobxx.getMap().broadcastMessage(MobPacket.TeleportMonster(mobxx, false, 3, new Point(x, 85)));
                     }
                  } else {
                     MapleMonster mob;
                     if (map.getCustomValue0(45001316) == 0 && map.getCustomValue(45001313) == null && map.getId() == 450013300 && map.getMobsSize(8880502) > 0) {
                        map.setCustomInfo(45001313, 0, Randomizer.rand(38000, 43000));
                        map.broadcastMessage(CField.enforceMSG("파멸의 눈이 적을 쫒는다.", 265, 4000));
                        var2 = map.getAllMonster().iterator();

                        while(var2.hasNext()) {
                           mob = (MapleMonster)var2.next();
                           if (mob.getId() == 8880502) {
                              map.broadcastMessage(MobPacket.UseSkill(mob.getObjectId(), 5));
                              break;
                           }
                        }

                        map.broadcastMessage(CField.getFieldSkillAdd(100012, Randomizer.rand(1, 2), false));
                     } else if (map.getCustomValue0(45001316) == 0 && map.getCustomValue(45101313) == null && map.getId() == 450013300 && map.getMobsSize(8880502) > 0) {
                        var2 = map.getAllMonster().iterator();

                        while(var2.hasNext()) {
                           mob = (MapleMonster)var2.next();
                           if (mob.getId() == 8880516) {
                              map.broadcastMessage(MobPacket.enableOnlyFsmAttack(mob, 1, 0));
                              map.setCustomInfo(45101313, 0, Randomizer.rand(10000, 13000));
                              break;
                           }
                        }
                     } else if (map.getCustomValue0(45001316) != 0 || map.getCustomValue(45001314) != null || (map.getId() != 450013300 || map.getMobsSize(8880502) <= 0) && (map.getId() != 450013500 || map.getMobsSize(8880503) <= 0)) {
                        boolean spattack;
                        if (map.getCustomValue0(45001316) == 0 && map.getCustomValue(45001320) == null && map.getId() == 450013500 && map.getMobsSize(8880503) > 0) {
                           skillinfox = new ArrayList();

                           for(typex = 0; typex < Randomizer.rand(3, 5); ++typex) {
                              skillinfox.add(new Triple(new Point(Randomizer.rand(-970, 970), Randomizer.rand(-400, 60)), Randomizer.rand(0, 180), 100 + Randomizer.rand(50, 100) * typex));
                           }

                           spattack = false;
                           chrs = map.getAllMonster().iterator();

                           while(chrs.hasNext()) {
                              mobx = (MapleMonster)chrs.next();
                              if (mobx.getId() == 8880503) {
                                 map.broadcastMessage(MobPacket.enableOnlyFsmAttack(mobx, 1, 0));
                                 map.setCustomInfo(45101313, 0, Randomizer.rand(10000, 13000));
                                 if (Randomizer.isSuccess(30)) {
                                    spattack = true;
                                    map.broadcastMessage(MobPacket.UseSkill(mobx.getObjectId(), 13));
                                 }
                                 break;
                              }
                           }

                           map.setCustomInfo(45001320, 0, Randomizer.rand(4000, 6000));
                           map.broadcastMessage(CField.getFieldLaserAdd(100011, spattack ? 1 : 2, skillinfox));
                        } else if (map.getCustomValue0(45001316) == 0 && map.getCustomValue(45001321) == null && map.getId() == 450013500 && map.getMobsSize(8880503) > 0) {
                           Point pos = null;

                           for(typex = Randomizer.rand(1, 6); map.getCustomValue0(45001350 + typex) == 1; typex = Randomizer.rand(1, 6)) {
                           }

                           switch(typex) {
                           case 1:
                              pos = new Point(-792, -153);
                              break;
                           case 2:
                              pos = new Point(-568, -298);
                              break;
                           case 3:
                              pos = new Point(-289, -211);
                              break;
                           case 4:
                              pos = new Point(143, -90);
                              break;
                           case 5:
                              pos = new Point(485, -185);
                              break;
                           case 6:
                              pos = new Point(791, -309);
                           }

                           List<Triple<Point, String, Integer>> skillinfoxx = new ArrayList();
                           map.setCustomInfo(45001321, 0, Randomizer.rand(10000, 35000));
                           skillinfoxx.add(new Triple(pos, typex == 1 ? "foot1" : "foo" + typex, 1));
                           map.broadcastMessage(CField.getFieldFootHoldAdd(100013, 1, skillinfoxx, false));
                           map.setCustomInfo(45001350 + typex, 1, 0);
                        } else if (map.getCustomValue0(45001316) == 0 && (map.getCustomValue(45001322) == null || map.getCustomValue(45001323) == null) && map.getId() == 450013500 && map.getMobsSize(8880503) > 0) {
                           skillinfox = new ArrayList();
                           Obstacle obx;
                           if (map.getCustomValue(45001322) == null) {
                              posx = new Point(Randomizer.rand(map.getLeft(), map.getRight()), 110);
                              map.setCustomInfo(45001322, 0, Randomizer.rand(50000, 65000));
                              obx = new Obstacle(76, posx, new Point(posx.x, 16), 50, 10, 0, 600, 82, 1, 1100, 700);
                              skillinfox.add(obx);
                           } else if (map.getCustomValue(45001323) == null) {
                              posx = new Point(Randomizer.rand(map.getLeft(), map.getRight()), -491);
                              map.setCustomInfo(45001323, 0, Randomizer.rand(30000, 45000));
                              obx = new Obstacle(79, posx, new Point(posx.x, 16), 50, 10, 390, 82, 1, 573);
                              skillinfox.add(obx);
                           }

                           if (!skillinfox.isEmpty()) {
                              map.CreateObstacle(monster, skillinfox);
                           }
                        } else if (map.getCustomValue(45001324) == null && map.getId() == 450013500 && map.getMobsSize(8880503) > 0) {
                           skillinfox = new ArrayList();
                           spattack = true;
                           map.broadcastMessage(CField.enforceMSG("검은 마법사가 창조와 파괴의 권능을 사용한다. 위와 아래, 어느 쪽으로 피할지 선택해야 한다.", 265, 4000));
                           map.setCustomInfo(45001324, 0, Randomizer.rand(60000, 75000));
                           map.setCustomInfo(45001316, 1, 0);
                           map.getMonsterById(8880503).setSkillForbid(true);
                           map.broadcastMessage(MobPacket.UseSkill(map.getMonsterById(8880503).getObjectId(), 11));
                           Timer.MapTimer.getInstance().schedule(() -> {
                              ArrayList<Obstacle> obs = new ArrayList();

                              for(int i = 0; i < 45; ++i) {
                                 int bx = 0;
                                 int ax = 0;
                                 int x = Randomizer.rand(-970, 970);
                                 int y = true;
                                 int cy = 0;
                                 int cya = 685;

                                 for(int a = 1; a <= 6; ++a) {
                                    switch(a) {
                                    case 1:
                                       bx = -922;
                                       ax = -674;
                                       cy = 395;
                                       break;
                                    case 2:
                                       bx = -630;
                                       ax = -500;
                                       cy = 245;
                                       break;
                                    case 3:
                                       bx = -357;
                                       ax = -150;
                                       cy = 338;
                                       break;
                                    case 4:
                                       bx = 77;
                                       ax = 201;
                                       cy = 448;
                                       break;
                                    case 5:
                                       bx = 385;
                                       ax = 570;
                                       cy = 348;
                                       break;
                                    case 6:
                                       bx = 662;
                                       ax = 915;
                                       cy = 250;
                                    }

                                    if (map.getCustomValue0(45001350 + a) == 1) {
                                       bx -= 50;
                                       if (x >= bx) {
                                          ax += 50;
                                          if (x <= ax) {
                                             cya = cy;
                                          }
                                       }
                                    }
                                 }

                                 Obstacle ob = new Obstacle(78, new Point(x, -600), new Point(x, -85), 100, 999, 0, 450, 0, Randomizer.rand(400, 600), Randomizer.rand(1, 3), cya);
                                 obs.add(ob);
                              }

                              map.broadcastMessage(MobPacket.CreateObstacle3(obs));
                           }, 1500L);

                           for(type = 1; type <= 6; ++type) {
                              if (map.getCustomValue0(45001350 + type) == 1) {
                                 Point posxx = null;
                                 switch(type) {
                                 case 1:
                                    posxx = new Point(-792, -153);
                                    break;
                                 case 2:
                                    posxx = new Point(-568, -298);
                                    break;
                                 case 3:
                                    posxx = new Point(-289, -211);
                                    break;
                                 case 4:
                                    posxx = new Point(143, -90);
                                    break;
                                 case 5:
                                    posxx = new Point(485, -185);
                                    break;
                                 case 6:
                                    posxx = new Point(791, -309);
                                 }

                                 skillinfox.add(new Triple(posxx, type == 1 ? "foot1" : "foo" + type, 0));
                              }
                           }

                           Timer.MapTimer.getInstance().schedule(() -> {
                              map.removeCustomInfo(45001351);
                              map.removeCustomInfo(45001352);
                              map.removeCustomInfo(45001353);
                              map.removeCustomInfo(45001354);
                              map.removeCustomInfo(45001355);
                              map.removeCustomInfo(45001356);
                              map.removeCustomInfo(45001316);
                              map.removeCustomInfo(45001360);
                              map.getMonsterById(8880503).setSkillForbid(false);
                              map.broadcastMessage(CField.getFieldFootHoldAdd(100013, 1, skillinfox, false));
                              map.broadcastMessage(MobPacket.mobBarrierEffect(map.getMonsterById(8880503).getObjectId(), "UI/UIWindow8.img/BlackMageShield/mobEffect_210", "Sound/Etc.img/BlackMageShield", "UI/UIWindow8.img/BlackMageShield/mobEffect0_210"));
                              map.getMonsterById(8880503).gainShield(3000000000000L, true, 0);
                              ArrayList<Pair<Long, Integer>> damage = new ArrayList();
                              map.setCustomInfo(45001326, 0, 15000);
                              map.setCustomInfo(45001327, map.getMonsterById(8880503).getPosition().x + 300, 0);
                              map.setCustomInfo(45001328, -150, 0);
                              Point pos = new Point(map.getCustomValue0(45001327), map.getCustomValue(45001328));
                              int dealy = 540;
                              MapleCharacter chr = null;
                              Iterator<MapleCharacter> chrs = map.getCharactersThreadsafe().iterator();
                              if (chrs.hasNext()) {
                                 chr = (MapleCharacter)chrs.next();
                              }

                              for(int i = 0; i < 14; ++i) {
                                 damage.add(new Pair(9999999999L, dealy + i * 180));
                                 if (chr != null) {
                                    map.getMonsterById(8880503).damage(chr, 9999999999L, false);
                                 }
                              }

                              map.broadcastMessage(MobPacket.FieldSummonTeleport(pos, pos.x < 0));
                              Timer.MapTimer.getInstance().schedule(() -> {
                                 map.broadcastMessage(MobPacket.FieldSummonAttack(3, true, pos, map.getMonsterById(8880503).getObjectId(), damage));
                                 map.setCustomInfo(45001327, map.getMonsterById(8880503).getPosition().x + Randomizer.rand(200, 500), 0);
                                 map.setCustomInfo(45001328, -304, 0);
                                 map.broadcastMessage(MobPacket.FieldSummonTeleport(new Point(map.getCustomValue0(45001327), -304), true));
                              }, 2000L);
                           }, 5000L);
                        }
                     } else {
                        map.setCustomInfo(45001314, 0, Randomizer.rand(45000, 51000));
                        map.getObtacles(Randomizer.rand(1, 2));
                     }
                  }
               }

               if (map.getCustomValue(45021328) == null && map.getId() == 450013500 && map.getMobsSize(8880503) > 0 && map.getCustomValue0(45011328) < 3) {
                  mobid = 0;
                  map.broadcastMessage(CField.enforceMSG("파괴의 천사가 무에서 창조된다.", 265, 4000));
                  map.setCustomInfo(45011328, map.getCustomValue0(45011328) + 1, 0);
                  if (map.getCustomValue0(45011328) < 3) {
                     map.setCustomInfo(45021328, 0, 120000);
                  }

                  posx = null;
                  switch(map.getCustomValue0(45011328)) {
                  case 1:
                     mobid = 8880509;
                     posx = new Point(746, 85);
                     break;
                  case 2:
                     mobid = 8880510;
                     posx = new Point(443, 85);
                     break;
                  case 3:
                     mobid = 8880511;
                     posx = new Point(209, 85);
                  }

                  map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobid), new Point(posx.x, posx.y));
                  map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobid), new Point(-posx.x, posx.y));
               }

               if (map.getCustomValue(45001326) == null && map.getId() == 450013500 && map.getMobsSize(8880503) > 0) {
                  skillinfox = new ArrayList();
                  map.setCustomInfo(45001326, 0, 5000);
                  skillinfox.add(new Pair(3000000000L, 0));
                  chr = null;
                  chrs = map.getCharactersThreadsafe().iterator();
                  if (chrs.hasNext()) {
                     chr = (MapleCharacter)chrs.next();
                  }

                  if (chr != null) {
                     map.getMonsterById(8880503).damage(chr, 3000000000L, false);
                  }

                  map.broadcastMessage(MobPacket.FieldSummonAttack(3, false, new Point(0, 0), map.getMonsterById(8880503).getObjectId(), skillinfox));
               }
            }

            if (map.getId() == 450013700 && map.getMobsSize(8880504) > 0) {
               if (map.getCustomValue(45004445) == null) {
                  skillinfox = new ArrayList();
                  map.broadcastMessage(CField.enforceMSG("신에 가까운 자의 권능이 발현된다. 창조와 파괴, 어떤 힘을 품을지 선택해야 한다.", 265, 5000));
                  map.setCustomInfo(45004445, 0, 30000);
                  map.setCustomInfo(45004446, 1, 0);
                  skillinfox.add(new Triple(new Point(-801, -404), new Point(-681, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(-681, -404), new Point(-561, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(-561, -404), new Point(-441, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(-441, -404), new Point(-321, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(-321, -404), new Point(-201, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(-201, -404), new Point(-81, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(-81, -404), new Point(39, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(39, -404), new Point(159, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(159, -404), new Point(279, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(279, -404), new Point(399, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(399, -404), new Point(519, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(519, -404), new Point(639, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(639, -404), new Point(759, 238), Randomizer.rand(0, 3)));
                  skillinfox.add(new Triple(new Point(759, -404), new Point(879, 238), Randomizer.rand(0, 3)));
                  map.broadcastMessage(CField.getFieldFinalLaserAdd(100014, 2, skillinfox, 0));
                  Timer.MapTimer.getInstance().schedule(() -> {
                     map.setCustomInfo(45004446, 0, 0);
                     map.getMonsterById(8880519).gainShield(3000000000000L, true, 0);
                     map.broadcastMessage(MobPacket.mobBarrierEffect(map.getMonsterById(8880504).getObjectId(), "UI/UIWindow8.img/BlackMageShield/mobEffect_211", "Sound/Etc.img/BlackMageShield", "UI/UIWindow8.img/BlackMageShield/mobEffect0_211"));
                  }, 5000L);
               } else if (map.getCustomValue(45004444) == null && map.getCustomValue0(45004446) == 0) {
                  map.setCustomInfo(45004444, 0, Randomizer.rand(7000, 12000));

                  for(mobid = 0; mobid < Randomizer.rand(3, 4); ++mobid) {
                     skillinfo = new ArrayList();
                     type = Randomizer.rand(map.getLeft(), map.getRight());
                     int rany = Randomizer.rand(-251, 159);
                     skillinfo.add(new Triple(new Point(type - 150, rany - 150), new Point(type + 150, rany + 150), Randomizer.rand(0, 1)));
                     map.broadcastMessage(CField.getFieldFinalLaserAdd(100016, 1, skillinfo, mobid * 240));
                  }
               }
            }

         }
      }, 1000L));
   }

   public static void SerenHandler(MapleMonster monster) {
      switch(monster.getId()) {
      case 8880600:
         monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            List<Obstacle> obs = new ArrayList();

            for(int i = 0; i < 3; ++i) {
               int x = Randomizer.rand(-1030, 1030);
               Obstacle ob = new Obstacle(84, new Point(x, -440), new Point(x, 275), 30, 15, i * 1000, Randomizer.rand(16, 32), 3, 715, 0);
               obs.add(ob);
            }

            monster.getMap().CreateObstacle(monster, obs);
            Iterator var5 = monster.getMap().getAllChracater().iterator();

            while(var5.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var5.next();
               if (chr.isAlive()) {
                  chr.addSerenGauge(-10);
               }
            }

         }, 5000L));
         break;
      case 8880601:
      case 8880604:
      case 8880608:
      case 8880613:
         int time = monster.getId() == 8880601 ? 7000 : Randomizer.rand(7000, 13000);
         monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            monster.getMap().broadcastMessage(MobPacket.enableOnlyFsmAttack(monster, 1, 0));
         }, (long)time));
         break;
      case 8880602:
         monster.ResetSerenTime(true);
         monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            if (monster.getMap().getAllChracater().size() > 0 && monster.getCustomValue0(8880603) == 0L) {
               monster.addSkillCustomInfo(8880602, 1L);
               if (monster.getCustomValue0(8880602) >= (monster.getSerenTimetype() == 3 ? 1L : 5L)) {
                  monster.removeCustomInfo(8880602);
                  if (monster.getSerenTimetype() == 4) {
                     monster.gainShield(monster.getStats().getHp() / 100L, monster.getShield() <= 0L, 0);
                  }

                  Iterator var1 = monster.getMap().getAllChracater().iterator();

                  while(var1.hasNext()) {
                     MapleCharacter chr = (MapleCharacter)var1.next();
                     if (chr.isAlive()) {
                        chr.addSerenGauge(monster.getSerenTimetype() == 3 ? -20 : 20);
                     }
                  }
               }

               monster.AddSerenTimeHandler(monster.getSerenTimetype(), -1);
            }

         }, 1000L));
         break;
      case 8880603:
         monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            if (monster == null || monster.getMap().getMonsterById(8880603) == null) {
               monster.getSchedule().cancel(true);
               monster.setSchedule((ScheduledFuture)null);
            }

            if (monster != null) {
               for(int i = 0; i < 2; ++i) {
                  int time = i == 0 ? 10 : 1000;
                  Timer.MobTimer.getInstance().schedule(() -> {
                     monster.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880606), new Point(470, 305));
                     monster.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880606), new Point(-10, 305));
                     monster.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880606), new Point(-450, 305));
                  }, (long)time);
               }
            }

         }, (long)Randomizer.rand(7000, 11000)));
         break;
      case 8880605:
         monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            Iterator var1 = monster.getMap().getAllChracater().iterator();

            while(var1.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var1.next();
               if (chr.isAlive() && chr.getPosition().x - 300 <= monster.getPosition().x && chr.getPosition().x + 300 >= monster.getPosition().x) {
                  monster.addSkillCustomInfo(8880605, 1L);
                  if (monster.getCustomValue0(8880605) >= 10L) {
                     monster.switchController(chr.getClient().getRandomCharacter(), true);
                     monster.removeCustomInfo(8880605);
                  }

                  monster.getMap().broadcastMessage(MobPacket.enableOnlyFsmAttack(monster, 1, 0));
                  break;
               }
            }

         }, 2000L));
      case 8880606:
      case 8880609:
      case 8880611:
      case 8880612:
      default:
         break;
      case 8880607:
         monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            MapleMap mapleMap = monster.getMap();
            FieldSkillFactory.getInstance();
            mapleMap.broadcastMessage(MobPacket.useFieldSkill(FieldSkillFactory.getFieldSkill(100023, 1)));
         }, (long)Randomizer.rand(5000, 10000)));
         break;
      case 8880610:
         monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            if (monster != null) {
               int type = Randomizer.rand(0, 2);
               if (type == 0) {
                  monster.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880611), new Point(-320, 305));
                  monster.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880611), new Point(470, 305));
               } else {
                  monster.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880611), type == 1 ? new Point(-320, 305) : new Point(470, 305));
               }
            }

         }, (long)Randomizer.rand(30000, 50000)));
      }

   }

   public static void LucidHandler(MapleMonster monster) {
      switch(monster.getId()) {
      case 8880140:
      case 8880141:
      case 8880142:
      case 8880150:
      case 8880151:
      case 8880153:
      case 8880155:
         monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            if ((monster.getEventInstance() == null || !monster.isAlive()) && monster.getMap().getCharacters().size() <= 0) {
               monster.getSchedule().cancel(true);
               monster.setSchedule((ScheduledFuture)null);
               monster.getMap().killMonsterType(monster, 0);
            } else {
               if (monster.getCustomValue0(23888) == 0L) {
                  if ((monster.getId() == 8880150 || monster.getId() == 8880151 || monster.getId() == 8880153 || monster.getId() == 8880155) && monster.getCustomValue(8880150) == null) {
                     ArrayList<String> foot = new ArrayList();
                     int rand = Randomizer.rand(0, 11);

                     Point pos;
                     for(pos = null; monster.getMap().getCustomValue(4200000 + rand) != null; rand = Randomizer.rand(0, 11)) {
                     }

                     switch(rand) {
                     case 0:
                        foot.add("Bblue1");
                        pos = new Point(Randomizer.rand(169, 501), -855);
                        break;
                     case 1:
                        foot.add("except1");
                        pos = new Point(Randomizer.rand(953, 1094), -842);
                        break;
                     case 2:
                        foot.add("Bred1");
                        pos = new Point(Randomizer.rand(936, 1276), -619);
                        break;
                     case 3:
                        foot.add("Bblue2");
                        pos = new Point(Randomizer.rand(540, 894), -490);
                        break;
                     case 4:
                        foot.add("Bred2");
                        pos = new Point(Randomizer.rand(-12, 331), -550);
                        break;
                     case 5:
                        foot.add("Mred2");
                        pos = new Point(Randomizer.rand(273, 498), -378);
                        break;
                     case 6:
                        foot.add("Mred3");
                        pos = new Point(Randomizer.rand(856, 1088), -331);
                        break;
                     case 7:
                        foot.add("Bred3");
                        pos = new Point(Randomizer.rand(642, 957), -194);
                        break;
                     case 8:
                        foot.add("Myellow3");
                        pos = new Point(Randomizer.rand(1028, 1268), -143);
                        break;
                     case 9:
                        foot.add("Myellow2");
                        pos = new Point(Randomizer.rand(22, 236), -267);
                        break;
                     case 10:
                        foot.add("Bblue3");
                        pos = new Point(Randomizer.rand(152, 472), -125);
                        break;
                     case 11:
                        foot.add("Myellow1");
                        pos = new Point(Randomizer.rand(414, 641), -685);
                     }

                     MapleMonster monste3r = MapleLifeFactory.getMonster(monster.getId() == 8880155 ? 8880194 : (monster.getId() == 8880150 ? 8880170 : 8880182));
                     monster.getMap().spawnMonsterOnGroundBelow(monste3r, pos);
                     monster.getMap().killMonsterType(monste3r, 2);
                     monster.getMap().broadcastMessage(MobPacket.BossLucid.setStainedGlassOnOff(false, foot));
                     MapleMonster monste3r4 = MapleLifeFactory.getMonster(monster.getId() == 8880155 ? 8880195 : (monster.getId() == 8880150 ? 8880171 : 8880183));
                     Timer.MapTimer.getInstance().schedule(() -> {
                        monster.getMap().broadcastMessage(MobPacket.BossLucid.setStainedGlassOnOff(true, foot));
                        monster.getMap().spawnMonsterOnGroundBelow(monste3r4, pos);
                     }, 2000L);
                     monster.setCustomInfo(8880150, 0, 15000);
                     monster.getMap().setCustomInfo(4200000 + rand, 0, 120000);
                  }

                  if (monster != null && monster.getCustomValue(8880141) == null) {
                     Butterfly bf = new Butterfly(Randomizer.rand(0, 8), Butterfly.getPosition(monster.getId() != 8880150 && monster.getId() != 8880151 && monster.getId() != 8880155, Randomizer.rand(0, 49)));
                     ArrayList<Butterfly> bfl = new ArrayList();
                     ArrayList<Integer> chra = new ArrayList();
                     bfl.add(bf);
                     monster.getMap().broadcastMessage(MobPacket.BossLucid.createButterfly(0, bfl));
                     monster.addSkillCustomInfo(8880140, 1L);
                     if (monster.getCustomValue0(8880140) == 20L) {
                        monster.getMap().broadcastMessage(CField.enforceMSG("꿈이 강해지고 있습니다. 조심하세요!", 222, 3000));
                     } else if (monster.getCustomValue0(8880140) >= 40L) {
                        if (monster.getId() != 8880150 && monster.getId() != 8880151 && monster.getId() != 8880155) {
                           for(int i = 0; i < 40; ++i) {
                              chra.add(monster.getController().getClient().getRandomCharacter().getId());
                           }

                           monster.removeCustomInfo(8880140);
                           monster.getMap().broadcastMessage(MobPacket.BossLucid.AttackButterfly(chra));
                        } else {
                           monster.setNextSkill(238);
                           monster.setNextSkillLvl(9);
                        }
                     }

                     if (monster.getId() == 8880140 || monster.getId() == 8880141 || monster.getId() == 8880150 || monster.getId() == 8880151 || monster.getId() == 8880142 || monster.getId() == 8880155) {
                        if (monster.getPhase() == 0 && monster.getHPPercent() <= 75) {
                           monster.setPhase((byte)1);
                           monster.getMap().broadcastMessage(CField.enforceMSG("루시드가 힘을 이끌어내고 있습니다!", 222, 5000));
                        } else if (monster.getPhase() == 1 && monster.getHPPercent() <= 50) {
                           monster.setPhase((byte)2);
                           monster.getMap().broadcastMessage(CField.enforceMSG("루시드가 더 강한 힘을 발휘할 겁니다!", 222, 5000));
                        } else if (monster.getPhase() == 2 && monster.getHPPercent() <= 25) {
                           monster.setPhase((byte)3);
                           monster.getMap().broadcastMessage(CField.enforceMSG("루시드가 분노한 것 같습니다!", 222, 5000));
                        }
                     }

                     monster.setCustomInfo(8880141, 0, monster.getPhase() == 0 ? 4000 : (monster.getPhase() == 1 ? 3000 : (monster.getPhase() == 2 ? 2000 : (monster.getPhase() == 3 ? 1000 : 3000))));
                  }

                  if (monster.getId() == 8880153 && monster.getCustomValue(8880153) == null) {
                     monster.getMap().broadcastMessage(CWvsContext.getTopMsg("루시드가 전범위 공격을 지속합니다."));
                     Iterator var8 = monster.getMap().getAllCharactersThreadsafe().iterator();

                     while(var8.hasNext()) {
                        MapleCharacter achr = (MapleCharacter)var8.next();
                        if (achr != null && achr.getBuffedValue(SecondaryStat.NotDamaged) == null && achr.getBuffedValue(SecondaryStat.IndieNotDamaged) == null) {
                           achr.addHP(-(achr.getStat().getCurrentMaxHp() / 100L * 30L));
                        }
                     }

                     monster.setCustomInfo(8880153, 0, Randomizer.rand(4000, 7000));
                  }
               }

            }
         }, 1000L));
      case 8880143:
      case 8880144:
      case 8880145:
      case 8880146:
      case 8880147:
      case 8880148:
      case 8880149:
      case 8880152:
      case 8880154:
      case 8880156:
      case 8880157:
      case 8880159:
      case 8880160:
      case 8880161:
      case 8880162:
      case 8880163:
      case 8880164:
      case 8880165:
      default:
         break;
      case 8880158:
      case 8880166:
         monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            if ((monster.getEventInstance() == null || !monster.isAlive()) && monster.getMap().getCharacters().size() <= 0) {
               monster.getSchedule().cancel(true);
               monster.setSchedule((ScheduledFuture)null);
               monster.getMap().killMonsterType(monster, 0);
            } else {
               int count = Randomizer.rand(1, 2);

               for(int i = 0; i < count; ++i) {
                  Point pos = new Point(Randomizer.rand(216, 1786), 43);
                  int monsterid = monster.getMap().getId() == 450004450 ? 8880180 : (monster.getMap().getId() == 450003840 ? 8880186 : 8880160);
                  MapleMonster mob = MapleLifeFactory.getMonster(monsterid);
                  mob.setDeadTimeKillmob(1000);
                  monster.getMap().spawnMonsterWithEffect(mob, 77, pos);
               }

            }
         }, monster.getMap().getId() == 450003840 ? 18000L : 15000L));
      }

   }

   public static void AggressIve(MapleMonster monster) {
      if (monster.getCustomValue(987654) == null) {
         List<Pair<String, Long>> a2 = new ArrayList();
         int i2 = false;
         Iterator var3 = monster.getController().getParty().getMembers().iterator();

         while(var3.hasNext()) {
            MaplePartyCharacter c = (MaplePartyCharacter)var3.next();
            if (c.getPlayer() != null) {
               a2.add(new Pair(c.getName(), c.getPlayer().getAggressiveDamage()));
            }
         }

         for(int i = 0; i < a2.size() - 1; ++i) {
            for(int j = 0; j < a2.size() - i - 1; ++j) {
               if ((Long)((Pair)a2.get(j)).getRight() < (Long)((Pair)a2.get(j + 1)).getRight()) {
                  String chridtmp = (String)((Pair)a2.get(j + 1)).getLeft();
                  long chrpointtmp = (Long)((Pair)a2.get(j + 1)).getRight();
                  a2.set(j + 1, (Pair)a2.get(j));
                  a2.set(j, new Pair(chridtmp, chrpointtmp));
               }
            }
         }

         MapleCharacter chr = monster.getMap().getCharacterByName((String)((Pair)a2.get(0)).getLeft());
         monster.getMap().broadcastMessage(CField.Aggressive(a2, monster.getMap()));
         String txt = !monster.getStats().getName().equals("데미안") && !monster.getStats().getName().equals("반 레온") && !monster.getStats().getName().equals("카웅") ? "가" : "이";
         monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 1, 3, 0, monster.getStats().getName() + txt + " " + chr.getName() + "님을 가장 위협적인 적으로 간주하고 있습니다."));
         if (monster.getStats().getName().equals("반 레온")) {
            Iterator var12 = monster.getSkills().iterator();

            label48:
            while(true) {
               MobSkill msi;
               do {
                  if (!var12.hasNext()) {
                     break label48;
                  }

                  msi = (MobSkill)var12.next();
               } while((msi.getSkillId() != 170 || msi.getSkillLevel() != 1) && msi.getSkillLevel() != 105);

               msi.setOnlyFsm(false);
            }
         }

         monster.setCustomInfo(987654, 0, 20000);
      }

   }

   public static void dunkelHandler(MapleMonster dunkel, MapleMap map) {
      dunkel.setSchedule(Timer.MobTimer.getInstance().register(() -> {
         if ((dunkel.getEventInstance() == null || !dunkel.isAlive()) && dunkel.getMap().getCharacters().size() <= 0) {
            dunkel.getSchedule().cancel(true);
            dunkel.setSchedule((ScheduledFuture)null);
            dunkel.getMap().killMonster(dunkel);
         } else {
            long time = System.currentTimeMillis();
            if (dunkel.isAlive()) {
               if (dunkel.getCustomValue(dunkel.getId()) == null) {
                  dunkel.setCustomInfo(dunkel.getId(), 0, 10000);
                  int radomcount = Randomizer.rand(2, 5);

                  for(int ix = 0; ix < radomcount; ++ix) {
                     MapleMonster fallenWarrior = MapleLifeFactory.getMonster(8645003);
                     if (dunkel.getId() == 8645009) {
                        fallenWarrior.setHp(10000000000L);
                        fallenWarrior.getStats().setHp(10000000000L);
                     } else if (dunkel.getId() == 8645066) {
                        fallenWarrior.setHp(30000000000L);
                        fallenWarrior.getStats().setHp(30000000000L);
                     }

                     dunkel.getMap().spawnMonsterOnGroundBelow(fallenWarrior, new Point(Randomizer.rand(-782, 774), 29));
                     List<Pair<MonsterStatus, MonsterStatusEffect>> stats = new ArrayList();
                     stats.add(new Pair(MonsterStatus.MS_PowerImmune, new MonsterStatusEffect(146, 12000, 1L)));
                     if (fallenWarrior != null) {
                        fallenWarrior.applyMonsterBuff(dunkel.getMap(), stats, new MobSkill(146, 18));
                     }
                  }
               }

               int i;
               int j;
               if (dunkel.getCustomValue(dunkel.getId() + 1) == null) {
                  int[] types = new int[]{72, 73, 74};
                  List<Obstacle> obs = new ArrayList();
                  i = Randomizer.rand(3, 8);

                  for(j = 0; j < i; ++j) {
                     int type = types[Randomizer.nextInt(types.length)];
                     int x = Randomizer.rand(-782, 774);
                     Obstacle ob;
                     if (type == 72) {
                        ob = new Obstacle(type, new Point(x, -815), new Point(x, 29), 24, 10, 180, Randomizer.rand(300, 850), 1, 844, 0);
                     } else if (type == 73) {
                        ob = new Obstacle(type, new Point(x, -815), new Point(x, 29), 24, 10, 601, Randomizer.rand(300, 850), 1, 844, 0);
                     } else if (type == 74) {
                        ob = new Obstacle(type, new Point(x, -815), new Point(x, 29), 24, 10, 1377, Randomizer.rand(300, 850), 1, 844, 0);
                     } else {
                        ob = new Obstacle(type, new Point(x, -815), new Point(x, 29), 24, 10, 0, Randomizer.rand(300, 850), 1, 844, 0);
                     }

                     obs.add(ob);
                  }

                  dunkel.getMap().CreateObstacle(dunkel, obs);
                  dunkel.setCustomInfo(dunkel.getId() + 1, 0, 3000);
               }

               if (dunkel.getCustomValue(dunkel.getId() + 2) == null) {
                  List<DunkelEliteBoss> eliteBosses = new ArrayList();
                  int count = 1;
                  if (dunkel.getId() == 8645009) {
                     if (dunkel.getHPPercent() >= 66) {
                        count = 1;
                     } else if (dunkel.getHPPercent() < 66 && dunkel.getHPPercent() >= 33) {
                        count = 2;
                     } else {
                        count = 3;
                     }
                  } else if (dunkel.getId() == 8645066) {
                     if (dunkel.getHPPercent() >= 50) {
                        count = 2;
                     } else {
                        count = 3;
                     }
                  }

                  for(i = 0; i < count; ++i) {
                     eliteBosses.add(getEliteBossAttack(dunkel));

                     for(j = 0; j < i; ++j) {
                        if (((DunkelEliteBoss)eliteBosses.get(i)).getbosscode() == ((DunkelEliteBoss)eliteBosses.get(j)).getbosscode()) {
                           eliteBosses.set(i, getEliteBossAttack(dunkel));
                           --j;
                        }
                     }
                  }

                  dunkel.getMap().broadcastMessage(MobPacket.BossDunKel.eliteBossAttack(dunkel, eliteBosses, (MapleCharacter)null, Randomizer.nextBoolean()));
                  dunkel.setCustomInfo(dunkel.getId() + 2, 0, 7000);
               }
            }

         }
      }, 1000L));
   }

   public static DunkelEliteBoss getEliteBossAttack(MapleMonster dunkel) {
      int type = Randomizer.nextInt(10);
      List<MapleCharacter> chrlist = dunkel.getMap().getAllChracater();
      Collections.shuffle(chrlist);
      MapleCharacter chr = (MapleCharacter)chrlist.get(0);
      Point cp = chr.getPosition();
      byte isLeft = (byte)(Randomizer.nextBoolean() ? 0 : 1);
      DunkelEliteBoss eboss;
      if (type == 0) {
         eboss = new DunkelEliteBoss((short)type, (short)1, 2800, 1440, 1, 3, 0, 0, 0, (short)0, (short)0, (short)0, (byte)0, isLeft, new Point(-280, -220), new Point(10, 10), cp, (short)0, (short)0);
      } else if (type == 1) {
         eboss = new DunkelEliteBoss((short)type, (short)2, 3000, 1620, 1, 4, 300, 0, 1200, (short)65, (short)100, (short)0, (byte)0, (byte)1, new Point(-100, -75), new Point(0, 0), new Point(0, 0), (short)0, (short)2);
      } else if (type == 2) {
         eboss = new DunkelEliteBoss((short)type, (short)2, 3000, 1800, 1, 5, 0, 1, 1600, (short)35, (short)600, (short)0, (byte)0, (byte)0, new Point(-45, -20), new Point(0, 0), new Point(0, 0), (short)0, (short)2);
      } else if (type == 3) {
         eboss = new DunkelEliteBoss((short)type, (short)1, 2900, 1500, 1, 6, 0, 0, 0, (short)0, (short)0, (short)0, (byte)0, isLeft, new Point(-620, -135), new Point(50, 5), cp, (short)0, (short)0);
      } else if (type == 4) {
         eboss = new DunkelEliteBoss((short)type, (short)3, 3300, 1710, 5, 7, 0, 0, 0, (short)0, (short)0, (short)0, (byte)1, isLeft, new Point(-40, -80), new Point(40, 0), cp, (short)Randomizer.rand(5, 8), (short)0);
      } else if (type == 5) {
         eboss = new DunkelEliteBoss((short)type, (short)1, 4800, 3630, 1, 8, 0, 0, 0, (short)0, (short)0, (short)1, (byte)1, isLeft, new Point(-290, -420), new Point(270, 25), cp, (short)0, (short)0);
      } else if (type == 6) {
         eboss = new DunkelEliteBoss((short)type, (short)3, 3000, 2160, 7, 11, 0, 0, 0, (short)0, (short)0, (short)1, (byte)1, isLeft, new Point(-50, -170), new Point(50, 5), new Point(0, 0), (short)Randomizer.rand(5, 9), (short)0);
      } else if (type == 7) {
         eboss = new DunkelEliteBoss((short)type, (short)1, 4800, 3630, 1, 8, 0, 0, 0, (short)0, (short)0, (short)1, (byte)1, isLeft, new Point(-290, -420), new Point(270, 25), cp, (short)0, (short)0);
      } else if (type == 8) {
         eboss = new DunkelEliteBoss((short)type, (short)1, 2800, 840, 1, 12, 0, 0, 0, (short)0, (short)0, (short)0, (byte)0, isLeft, new Point(-360, -155), new Point(10, 10), cp, (short)0, (short)0);
      } else if (type == 9) {
         eboss = new DunkelEliteBoss((short)type, (short)1, 2700, 840, 1, 10, 0, 0, 0, (short)0, (short)0, (short)0, (byte)0, isLeft, new Point(-350, -155), new Point(10, 10), cp, (short)0, (short)0);
      } else {
         eboss = null;
      }

      return eboss;
   }

   public static void JinHillaGlassTime(MapleMonster jinhilla, int time) {
      if (jinhilla != null && jinhilla.getMap().getAllChracater().size() > 0) {
         MobSkill msi = MobSkillFactory.getMobSkill(247, 1);
         jinhilla.setSchedule(Timer.MobTimer.getInstance().schedule(() -> {
            if (jinhilla != null && jinhilla.isAlive()) {
               MapleCharacter player = jinhilla.getController();
               if (player == null) {
                  Iterator<MapleCharacter> iterator = jinhilla.getMap().getAllChracater().iterator();
                  if (iterator.hasNext()) {
                     MapleCharacter chr = (MapleCharacter)iterator.next();
                     player = chr;
                  }
               }

               if (player == null) {
                  return;
               }

               jinhilla.setCustomInfo(24701, 1, 0);
               jinhilla.setNextSkill(247);
               jinhilla.setNextSkillLvl(1);
               jinhilla.getMap().broadcastMessage(MobPacket.setAttackZakumArm(jinhilla.getObjectId(), 2));
               jinhilla.getMap().broadcastMessage(MobPacket.moveMonsterResponse(jinhilla.getObjectId(), (short)((int)jinhilla.getCustomValue0(99991)), jinhilla.getMp(), true, 247, 1, 0));
               msi.applyEffect(player, jinhilla, true, true);
               jinhilla.setLastSkillUsed(msi, System.currentTimeMillis(), (long)(time * 1000));
               JinHillaGlassTime(jinhilla, jinhilla.getHPPercent() >= 60 ? 150 : (jinhilla.getHPPercent() >= 30 ? 120 : 100));
            }

         }, (long)(time * 1000)));
      }
   }
}
