package client;

import constants.GameConstants;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.StringUtil;
import tools.Triple;

public class SkillFactory {
   private static final Map<Integer, Skill> skills = new HashMap();
   private static final Map<String, Integer> delays = new HashMap();
   private static final Map<Integer, SkillFactory.CraftingEntry> crafts = new HashMap();
   private static final Map<Integer, List<Integer>> skillsByJob = new HashMap();
   private static final Map<Integer, SummonSkillEntry> SummonSkillInformation = new HashMap();
   public static boolean reload = false;

   public static void reload() {
      reload = true;
      skills.clear();
      delays.clear();
      crafts.clear();
      skillsByJob.clear();
      SummonSkillInformation.clear();
      load();
   }

   public static String getSkillDec(int id, MapleData stringData) {
      String strId = Integer.toString(id);
      strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
      MapleData skillroot = stringData.getChildByPath(strId);
      return skillroot != null ? MapleDataTool.getString(skillroot.getChildByPath("desc"), "") : "";
   }

   public static void load() {
      MapleData delayData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Character.wz")).getData("00002000.img");
      MapleData stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/String.wz")).getData("Skill.img");
      MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Skill.wz"));
      MapleDataDirectoryEntry root = datasource.getRoot();
      int del = 0;
      Iterator var5 = delayData.iterator();

      MapleData summon_data;
      while(var5.hasNext()) {
         summon_data = (MapleData)var5.next();
         if (!summon_data.getName().equals("info")) {
            delays.put(summon_data.getName(), del);
            ++del;
         }
      }

      int skillid = false;
      Iterator var8 = root.getFiles().iterator();

      while(true) {
         label92:
         while(var8.hasNext()) {
            MapleDataFileEntry topDir = (MapleDataFileEntry)var8.next();
            Iterator var10;
            MapleData data;
            int skillid;
            if (topDir.getName().length() <= 10) {
               var10 = datasource.getData(topDir.getName()).iterator();

               while(true) {
                  do {
                     if (!var10.hasNext()) {
                        continue label92;
                     }

                     data = (MapleData)var10.next();
                  } while(!data.getName().equals("skill"));

                  Iterator var23 = data.iterator();

                  label131:
                  while(var23.hasNext()) {
                     MapleData data2 = (MapleData)var23.next();

                     try {
                        if (data2 != null) {
                           skillid = Integer.parseInt(data2.getName());
                           Skill skil = Skill.loadFromData(skillid, data2, delayData);
                           List<Integer> job = (List)skillsByJob.get(skillid / 10000);
                           if (job == null) {
                              job = new ArrayList();
                              skillsByJob.put(skillid / 10000, job);
                           }

                           skil.setDesc(getSkillDec(skillid, stringData));
                           ((List)job).add(skillid);
                           skil.setName(getName(skillid, stringData));
                           skills.put(skillid, skil);
                           summon_data = data2.getChildByPath("summon/attack1/info");
                           if (summon_data != null) {
                              SummonSkillEntry sse = new SummonSkillEntry();
                              sse.type = (byte)MapleDataTool.getInt("type", summon_data, 0);
                              sse.mobCount = (byte)(skillid == 33101008 ? 3 : MapleDataTool.getInt("mobCount", summon_data, 1));
                              sse.attackCount = (byte)MapleDataTool.getInt("attackCount", summon_data, 1);
                              if (summon_data.getChildByPath("range/lt") != null) {
                                 MapleData ltd = summon_data.getChildByPath("range/lt");
                                 sse.lt = (Point)ltd.getData();
                                 sse.rb = (Point)summon_data.getChildByPath("range/rb").getData();
                              } else {
                                 sse.lt = new Point(-100, -100);
                                 sse.rb = new Point(100, 100);
                              }

                              sse.delay = MapleDataTool.getInt("effectAfter", summon_data, 0) + MapleDataTool.getInt("attackAfter", summon_data, 0);
                              Iterator var26 = summon_data.iterator();

                              while(true) {
                                 MapleData effect;
                                 do {
                                    if (!var26.hasNext()) {
                                       for(var26 = data2.getChildByPath("summon/attack1").iterator(); var26.hasNext(); sse.delay += MapleDataTool.getIntConvert("delay", effect, 0)) {
                                          effect = (MapleData)var26.next();
                                       }

                                       SummonSkillInformation.put(skillid, sse);
                                       continue label131;
                                    }

                                    effect = (MapleData)var26.next();
                                 } while(effect.getChildren().size() <= 0);

                                 MapleData effectEntry;
                                 for(Iterator var18 = effect.iterator(); var18.hasNext(); sse.delay += MapleDataTool.getIntConvert("delay", effectEntry, 0)) {
                                    effectEntry = (MapleData)var18.next();
                                 }
                              }
                           }
                        }
                     } catch (Exception var20) {
                        var20.printStackTrace();
                     }
                  }
               }
            } else if (topDir.getName().startsWith("Recipe")) {
               var10 = datasource.getData(topDir.getName()).iterator();

               while(var10.hasNext()) {
                  data = (MapleData)var10.next();
                  skillid = Integer.parseInt(data.getName());
                  SkillFactory.CraftingEntry skil = new SkillFactory.CraftingEntry(skillid, (byte)MapleDataTool.getInt("incFatigability", data, 0), (byte)MapleDataTool.getInt("reqSkillLevel", data, 0), (byte)MapleDataTool.getInt("incSkillProficiency", data, 0), MapleDataTool.getInt("needOpenItem", data, 0) > 0, MapleDataTool.getInt("period", data, 0));
                  Iterator var13 = data.getChildByPath("target").iterator();

                  MapleData d;
                  while(var13.hasNext()) {
                     d = (MapleData)var13.next();
                     skil.targetItems.add(new Triple(MapleDataTool.getInt("item", d, 0), MapleDataTool.getInt("count", d, 0), MapleDataTool.getInt("probWeight", d, 0)));
                  }

                  var13 = data.getChildByPath("recipe").iterator();

                  while(var13.hasNext()) {
                     d = (MapleData)var13.next();
                     skil.reqItems.put(MapleDataTool.getInt("item", d, 0), MapleDataTool.getInt("count", d, 0));
                  }

                  crafts.put(skillid, skil);
               }
            }
         }

         return;
      }
   }

   public static List<Integer> getSkillsByJob(int jobId) {
      return (List)skillsByJob.get(jobId);
   }

   public static String getSkillName(int id) {
      Skill skil = getSkill(id);
      return skil != null ? skil.getName() : null;
   }

   public static Integer getDelay(String id) {
      return SkillFactory.Delay.fromString(id) != null ? SkillFactory.Delay.fromString(id).i : (Integer)delays.get(id);
   }

   private static String getName(int id, MapleData stringData) {
      String strId = Integer.toString(id);
      strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
      MapleData skillroot = stringData.getChildByPath(strId);
      return skillroot != null ? MapleDataTool.getString(skillroot.getChildByPath("name"), "") : "";
   }

   public static SummonSkillEntry getSummonData(int skillid) {
      return (SummonSkillEntry)SummonSkillInformation.get(skillid);
   }

   public static Collection<Skill> getAllSkills() {
      return skills.values();
   }

   public static Map<Integer, Skill> getSkills() {
      return skills;
   }

   public static Skill getSkill(int id) {
      if (!skills.isEmpty()) {
         return id >= 91000000 && id < 100000000 && crafts.containsKey(id) ? (Skill)crafts.get(id) : (Skill)skills.get(id);
      } else {
         return null;
      }
   }

   public static long getDefaultSExpiry(Skill skill) {
      if (skill == null) {
         return -1L;
      } else {
         return skill.isTimeLimited() ? System.currentTimeMillis() + 2592000000L : -1L;
      }
   }

   public static SkillFactory.CraftingEntry getCraft(int id) {
      return !crafts.isEmpty() ? (SkillFactory.CraftingEntry)crafts.get(id) : null;
   }

   public static boolean sub_60A550(int a1) {
      if (!sub_60A300(a1) && (a1 - 92000000 <= -1000000 || a1 - 92000000 >= 1000000 || a1 % 10000 != 0) && !sub_60A460(a1) && !sub_60A1D0(a1) && !sub_569E20(a1) && !sub_60A210(a1)) {
         int v2 = sub_5692D0(a1);
         int v3 = sub_5B8F80(v2);
         return (v2 - '鱀' < -5 || v2 - '鱀' > 5) && (sub_60A150(a1) || v3 == 4 && !GameConstants.isZero(v2));
      } else {
         return false;
      }
   }

   public static boolean sub_60A150(int a1) {
      boolean v1;
      if (a1 > 101100101) {
         if (a1 > 101110203) {
            if (a1 == 101120104) {
               return true;
            }

            v1 = a1 - 101120104 == 100;
         } else {
            if (a1 == 101110203 || a1 == 101100201 || a1 == 101110102) {
               return true;
            }

            v1 = a1 - 101110102 == 98;
         }
      } else {
         if (a1 == 101100101) {
            return true;
         }

         if (a1 > 4331002) {
            if (a1 == 4340007 || a1 == 4341004) {
               return true;
            }

            v1 = a1 == 101000101;
         } else {
            if (a1 == 4331002 || a1 == 4311003 || a1 == 4321006) {
               return true;
            }

            v1 = a1 == 4330009;
         }
      }

      return v1;
   }

   public static boolean sub_60A460(int a1) {
      boolean result = false;
      if (a1 - 92000000 >= 1000000 || a1 - 92000000 <= -1000000 || a1 % 10000 != 0) {
         int v1 = 10000 * (a1 / 10000);
         if (v1 - 92000000 > -1000000 && v1 - 92000000 < 1000000 && v1 % 10000 == 0) {
            result = true;
         }
      }

      return result;
   }

   public static boolean sub_60A1D0(int a1) {
      int v1 = a1 / 10000;
      if (a1 / 10000 == 8000) {
         v1 = a1 / 100;
      }

      return v1 - 800000 >= -99 && v1 - 800000 <= 99;
   }

   public static boolean sub_569E20(int a1) {
      int v1 = a1 / 10000;
      if (a1 / 10000 == 8000) {
         v1 = a1 / 100;
      }

      boolean result;
      if (v1 - '鱀' < -5 && v1 - '鱀' > 5) {
         result = sub_569620(v1);
      } else {
         result = false;
      }

      return result;
   }

   public static boolean sub_569620(int a1) {
      boolean v1;
      if (a1 > 6002) {
         if (a1 > 14000) {
            if (a1 - 15000 >= -2 && a1 - 15000 <= 2) {
               return true;
            } else if (a1 - '鱀' >= -5 && a1 - '鱀' <= 5) {
               return false;
            } else if (a1 % 1000 == 0) {
               return true;
            } else {
               return a1 - 800000 > -100 && a1 - 800000 < 100;
            }
         } else if (a1 == 14000) {
            return true;
         } else {
            v1 = a1 == 13000;
            if (v1) {
               return true;
            } else if (a1 - '鱀' >= -5 && a1 - '鱀' <= 5) {
               return false;
            } else if (a1 % 1000 == 0) {
               return true;
            } else {
               return a1 - 800000 > -100 && a1 - 800000 < 100;
            }
         }
      } else if (a1 >= 6000) {
         return true;
      } else if (a1 > 3002) {
         v1 = a1 == 5000;
         if (!v1) {
            if (a1 - '鱀' <= 5) {
               return false;
            } else if (a1 % 1000 == 0) {
               return true;
            } else {
               return a1 - 800000 < 100;
            }
         } else {
            return true;
         }
      } else if (a1 < 3001 && (a1 < 2001 || a1 > 2005)) {
         if (a1 - '鱀' >= -5 && a1 - '鱀' <= 5) {
            return false;
         } else if (a1 % 1000 == 0) {
            return true;
         } else {
            return a1 - 800000 > -100 && a1 - 800000 < 100;
         }
      } else {
         return true;
      }
   }

   public static boolean sub_60A210(int a1) {
      if (a1 != 0 && a1 >= 0) {
         int v1 = a1 / 10000;
         if (a1 / 10000 == 8000) {
            v1 = a1 / 100;
         }

         return v1 == 9500;
      } else {
         return false;
      }
   }

   public static int sub_5692D0(int a1) {
      int result = a1 / 10000;
      if (a1 / 10000 == 8000) {
         result = a1 / 100;
      }

      return result;
   }

   public static int sub_5B8F80(int a1) {
      if (!sub_569620(a1) && a1 % 100 != 0 && a1 != 501 && a1 != 3101 && a1 != 301) {
         if (GameConstants.isEvan(a1)) {
            return GameConstants.get_evan_job_level(a1);
         } else {
            int result;
            if (a1 / 10 == 43) {
               result = 0;
               int v2 = (a1 - 430) / 2;
               if (v2 <= 2) {
                  result = v2 + 2;
               }
            } else {
               result = 0;
               if (a1 % 10 >= -2 && a1 % 10 <= 2) {
                  result = a1 % 10 + 2;
               }
            }

            return result;
         }
      } else {
         return 1;
      }
   }

   public static boolean sub_60A300(int a1) {
      boolean v1;
      if (a1 > 5321006) {
         if (a1 > 33120010) {
            if (a1 <= 152120003) {
               if (a1 != 152120003 && a1 != 35120014 && a1 != 51120000) {
                  v1 = a1 == 80001913;
                  return v1;
               }

               return true;
            }

            if (a1 > 152121006) {
               v1 = a1 == 152121010;
               return v1;
            }

            if (a1 != 152121006 && (a1 < 152120012 || a1 > 152120013)) {
               return false;
            }
         } else if (a1 != 33120010) {
            if (a1 > 22171069) {
               if (a1 != 23120013 && a1 - 23120013 != 995) {
                  v1 = a1 - 23120013 == 998;
                  return v1;
               }

               return true;
            }

            if (a1 != 22171069) {
               if (a1 > 21120021) {
                  v1 = a1 == 21121008;
               } else {
                  if (a1 >= 21120020 || a1 == 21120011) {
                     return true;
                  }

                  v1 = a1 - 21120011 == 3;
               }

               return v1;
            }
         }

         return true;
      } else if (a1 == 5321006) {
         return true;
      } else if (a1 > 4340010) {
         if (a1 > 5220014) {
            if (a1 != 5221022 && a1 != 5320007) {
               v1 = a1 == 5321004;
               return v1;
            } else {
               return true;
            }
         } else {
            if (a1 != 5220014) {
               if (a1 > 5120012) {
                  v1 = a1 == 5220012;
                  return v1;
               }

               if (a1 < 5120011) {
                  v1 = a1 == 4340012;
                  return v1;
               }
            }

            return true;
         }
      } else if (a1 == 4340010) {
         return true;
      } else if (a1 > 2321010) {
         if (a1 != 3210015 && a1 != 4110012) {
            v1 = a1 == 4210012;
            return v1;
         } else {
            return true;
         }
      } else if (a1 == 2321010) {
         return true;
      } else {
         if (a1 > 2121009) {
            v1 = a1 == 2221009;
         } else {
            if (a1 == 2121009 || a1 == 1120012) {
               return true;
            }

            v1 = a1 == 1320011;
         }

         return v1;
      }
   }

   public static class CraftingEntry extends Skill {
      public boolean needOpenItem;
      public int period;
      public byte incFatigability;
      public byte reqSkillLevel;
      public byte incSkillProficiency;
      public List<Triple<Integer, Integer, Integer>> targetItems = new ArrayList();
      public Map<Integer, Integer> reqItems = new HashMap();

      public CraftingEntry(int id, byte incFatigability, byte reqSkillLevel, byte incSkillProficiency, boolean needOpenItem, int period) {
         super(id);
         this.incFatigability = incFatigability;
         this.reqSkillLevel = reqSkillLevel;
         this.incSkillProficiency = incSkillProficiency;
         this.needOpenItem = needOpenItem;
         this.period = period;
      }
   }

   public static enum Delay {
      walk1(0),
      walk2(1),
      stand1(2),
      stand2(3),
      alert(4),
      swingO1(5),
      swingO2(6),
      swingO3(7),
      swingOF(8),
      swingT1(9),
      swingT2(10),
      swingT3(11),
      swingTF(12),
      swingP1(13),
      swingP2(14),
      swingPF(15),
      stabO1(16),
      stabO2(17),
      stabOF(18),
      stabT1(19),
      stabT2(20),
      stabTF(21),
      swingD1(22),
      swingD2(23),
      stabD1(24),
      swingDb1(25),
      swingDb2(26),
      swingC1(27),
      swingC2(28),
      rushBoom(28),
      tripleBlow(25),
      quadBlow(26),
      deathBlow(27),
      finishBlow(28),
      finishAttack(29),
      finishAttack_link(30),
      finishAttack_link2(30),
      shoot1(31),
      shoot2(32),
      shootF(33),
      shootDb2(40),
      shotC1(41),
      dash(37),
      dash2(38),
      proneStab(41),
      prone(42),
      heal(43),
      fly(44),
      jump(45),
      sit(46),
      rope(47),
      dead(48),
      ladder(49),
      rain(50),
      alert2(52),
      alert3(53),
      alert4(54),
      alert5(55),
      alert6(56),
      alert7(57),
      ladder2(58),
      rope2(59),
      shoot6(60),
      magic1(61),
      magic2(62),
      magic3(63),
      magic5(64),
      magic6(65),
      explosion(65),
      burster1(66),
      burster2(67),
      savage(68),
      avenger(69),
      assaulter(70),
      prone2(71),
      assassination(72),
      assassinationS(73),
      tornadoDash(76),
      tornadoDashStop(76),
      tornadoRush(76),
      rush(77),
      rush2(78),
      brandish1(79),
      brandish2(80),
      braveSlash(81),
      braveslash1(81),
      braveslash2(81),
      braveslash3(81),
      braveslash4(81),
      darkImpale(97),
      sanctuary(82),
      meteor(83),
      paralyze(84),
      blizzard(85),
      genesis(86),
      blast(88),
      smokeshell(89),
      showdown(90),
      ninjastorm(91),
      chainlightning(92),
      holyshield(93),
      resurrection(94),
      somersault(95),
      straight(96),
      eburster(97),
      backspin(98),
      eorb(99),
      screw(100),
      doubleupper(101),
      dragonstrike(102),
      doublefire(103),
      triplefire(104),
      fake(105),
      airstrike(106),
      edrain(107),
      octopus(108),
      backstep(109),
      shot(110),
      rapidfire(110),
      fireburner(112),
      coolingeffect(113),
      fist(114),
      timeleap(115),
      homing(117),
      ghostwalk(118),
      ghoststand(119),
      ghostjump(120),
      ghostproneStab(121),
      ghostladder(122),
      ghostrope(123),
      ghostfly(124),
      ghostsit(125),
      cannon(126),
      torpedo(127),
      darksight(128),
      bamboo(129),
      pyramid(130),
      wave(131),
      blade(132),
      souldriver(133),
      firestrike(134),
      flamegear(135),
      stormbreak(136),
      vampire(137),
      swingT2PoleArm(139),
      swingP1PoleArm(140),
      swingP2PoleArm(141),
      doubleSwing(142),
      tripleSwing(143),
      fullSwingDouble(144),
      fullSwingTriple(145),
      overSwingDouble(146),
      overSwingTriple(147),
      rollingSpin(148),
      comboSmash(149),
      comboFenrir(150),
      comboTempest(151),
      finalCharge(152),
      finalBlow(154),
      finalToss(155),
      magicmissile(156),
      lightningBolt(157),
      dragonBreathe(158),
      breathe_prepare(159),
      dragonIceBreathe(160),
      icebreathe_prepare(161),
      blaze(162),
      fireCircle(163),
      illusion(164),
      magicFlare(165),
      elementalReset(166),
      magicRegistance(167),
      magicBooster(168),
      magicShield(169),
      recoveryAura(170),
      flameWheel(171),
      killingWing(172),
      OnixBlessing(173),
      Earthquake(174),
      soulStone(175),
      dragonThrust(176),
      ghostLettering(177),
      darkFog(178),
      slow(179),
      mapleHero(180),
      Awakening(181),
      flyingAssaulter(182),
      tripleStab(183),
      fatalBlow(184),
      slashStorm1(185),
      slashStorm2(186),
      bloodyStorm(187),
      flashBang(188),
      upperStab(189),
      bladeFury(190),
      chainPull(192),
      chainAttack(192),
      owlDead(193),
      monsterBombPrepare(195),
      monsterBombThrow(195),
      finalCut(196),
      finalCutPrepare(196),
      suddenRaid(198),
      fly2(199),
      fly2Move(200),
      fly2Skill(201),
      knockback(202),
      rbooster_pre(206),
      rbooster(206),
      rbooster_after(206),
      CrossOverChainRoad(209),
      nemesis(210),
      tank(217),
      tank_laser(221),
      siege_pre(223),
      tank_siegepre(223),
      sonicBoom(226),
      darkLightning(228),
      darkChain(229),
      cyclone_pre(0),
      cyclone(0),
      glacialchain(247),
      flamethrower(233),
      flamethrower_pre(233),
      flamethrower2(234),
      flamethrower_pre2(234),
      gatlingshot(239),
      gatlingshot2(240),
      drillrush(241),
      earthslug(242),
      rpunch(243),
      clawCut(244),
      swallow(247),
      swallow_attack(247),
      swallow_loop(247),
      flashRain(249),
      OnixProtection(264),
      OnixWill(265),
      phantomBlow(266),
      comboJudgement(267),
      arrowRain(268),
      arrowEruption(269),
      iceStrike(270),
      swingT2Giant(273),
      cannonJump(295),
      swiftShot(296),
      giganticBackstep(298),
      mistEruption(299),
      cannonSmash(300),
      cannonSlam(301),
      flamesplash(302),
      noiseWave(306),
      superCannon(310),
      jShot(312),
      demonSlasher(313),
      bombExplosion(314),
      cannonSpike(315),
      speedDualShot(316),
      strikeDual(317),
      bluntSmash(319),
      CrossOverChainPiercing(320),
      piercing(321),
      elfTornado(323),
      immolation(324),
      multiSniping(327),
      windEffect(328),
      elfrush(329),
      elfrush2(329),
      dealingRush(334),
      maxForce0(336),
      maxForce1(337),
      maxForce2(338),
      maxForce3(339),
      iceAttack1(274),
      iceAttack2(275),
      iceSmash(276),
      iceTempest(277),
      iceChop(278),
      icePanic(279),
      iceDoubleJump(280),
      shockwave(292),
      demolition(293),
      snatch(294),
      windspear(295),
      windshot(296);

      public int i;

      private Delay(int i) {
         this.i = i;
      }

      public static SkillFactory.Delay fromString(String s) {
         SkillFactory.Delay[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            SkillFactory.Delay b = var1[var3];
            if (b.name().equalsIgnoreCase(s)) {
               return b;
            }
         }

         return null;
      }
   }
}
