package handling.channel.handler;

import client.Core;
import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.SpecialCoreOption;
import client.VMatrix;
import constants.GameConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleInventoryManipulator;
import server.Randomizer;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class MatrixHandler {
   private static Map<Integer, Pair<Core, List<String>>> cores = new ConcurrentHashMap();
   private static List<Pair<Core, List<String>>> passiveCores = new ArrayList();
   private static List<Pair<Core, List<String>>> activeCores = new ArrayList();
   private static List<Pair<Core, List<String>>> specialCores = new ArrayList();

   public static void loadCore() {
      String WZpath = System.getProperty("wz");
      MapleDataProvider prov = MapleDataProviderFactory.getDataProvider(new File(WZpath + "/Etc.wz"));
      MapleData nameData = prov.getData("VCore.img");

      try {
         Iterator var3 = nameData.iterator();

         while(true) {
            MapleData dat;
            do {
               if (!var3.hasNext()) {
                  return;
               }

               dat = (MapleData)var3.next();
            } while(!dat.getName().equals("CoreData"));

            Iterator var5 = dat.iterator();

            while(var5.hasNext()) {
               MapleData d = (MapleData)var5.next();
               int coreid = Integer.parseInt(d.getName());
               int skillid = MapleDataTool.getInt("connectSkill/0", d, 0);
               int skillid2 = MapleDataTool.getInt("connectSkill/1", d, 0);
               int skillid3 = MapleDataTool.getInt("connectSkill/2", d, 0);
               int maxlevel = MapleDataTool.getInt("maxLevel", d, 0);
               List<String> jobs = new ArrayList();
               if (d.getName().equals(d.getName())) {
                  Iterator var13 = d.iterator();

                  label62:
                  while(true) {
                     MapleData j;
                     do {
                        if (!var13.hasNext()) {
                           break label62;
                        }

                        j = (MapleData)var13.next();
                     } while(!j.getName().equals("job"));

                     Iterator var15 = j.iterator();

                     while(var15.hasNext()) {
                        MapleData jobz = (MapleData)var15.next();
                        String job = MapleDataTool.getString(jobz);
                        jobs.add(job);
                     }
                  }
               }

               if (!jobs.contains("none")) {
                  SpecialCoreOption spOption = null;
                  if (d.getChildByPath("spCoreOption") != null) {
                     spOption = new SpecialCoreOption();
                     spOption.setCondType(MapleDataTool.getString("spCoreOption/cond/type", d, (String)null));
                     spOption.setCooltime(MapleDataTool.getInt("spCoreOption/cond/cooltime", d, 0));
                     spOption.setCount(MapleDataTool.getInt("spCoreOption/cond/count", d, 0));
                     spOption.setValidTime(MapleDataTool.getInt("spCoreOption/cond/validTime", d, 0));
                     spOption.setProb(MapleDataTool.getDouble("spCoreOption/cond/prob", d, 0.0D));
                     spOption.setEffectType(MapleDataTool.getString("spCoreOption/effect/type", d, (String)null));
                     spOption.setSkillid(MapleDataTool.getInt("spCoreOption/effect/skill_id", d, 0));
                     spOption.setSkilllevel(MapleDataTool.getInt("spCoreOption/effect/skill_level", d, 0));
                     spOption.setHeal_percent(MapleDataTool.getInt("spCoreOption/effect/heal_percent", d, 0));
                     spOption.setReducePercent(MapleDataTool.getInt("spCoreOption/effect/reducePercent", d, 0));
                  }

                  Core core = new Core(-1L, coreid, 0, 1, 0, 1, maxlevel, skillid, skillid2, skillid3, -1, spOption);
                  Pair<Core, List<String>> pair = new Pair(core, jobs);
                  cores.put(coreid, pair);
                  switch(coreid / 10000000) {
                  case 1:
                     activeCores.add(new Pair(new Core(-1L, coreid, 0, 1, 0, 1, maxlevel, skillid, 0, 0, -1, spOption), jobs));
                     break;
                  case 2:
                     passiveCores.add(new Pair(new Core(-1L, coreid, 0, 1, 0, 1, maxlevel, skillid, 0, 0, -1, spOption), jobs));
                     break;
                  case 3:
                     specialCores.add(new Pair(new Core(-1L, coreid, 0, 1, 0, 1, maxlevel, skillid, 0, 0, -1, spOption), jobs));
                  }
               }
            }
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      }
   }

   private static boolean CheckUseableJobs(List<String> jobz, List<String> list) {
      Iterator var2 = jobz.iterator();

      while(var2.hasNext()) {
         String job = (String)var2.next();
         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            String jobs = (String)var4.next();
            if (!jobs.equals("none") && !job.equals("none")) {
               if (!jobs.equals("all") && !job.equals("all")) {
                  if (jobs.equals("warrior") && GameConstants.isWarrior(Short.valueOf(job))) {
                     return true;
                  }

                  if (jobs.equals("magician") && GameConstants.isMagician(Short.valueOf(job))) {
                     return true;
                  }

                  if (jobs.equals("archer") && GameConstants.isArcher(Short.valueOf(job))) {
                     return true;
                  }

                  if (jobs.equals("rogue") && GameConstants.isThief(Short.valueOf(job))) {
                     return true;
                  }

                  if (jobs.equals("pirate") && GameConstants.isPirate(Short.valueOf(job))) {
                     return true;
                  }

                  if (job.equals("warrior") && GameConstants.isWarrior(Short.valueOf(jobs))) {
                     return true;
                  }

                  if (job.equals("magician") && GameConstants.isMagician(Short.valueOf(jobs))) {
                     return true;
                  }

                  if (job.equals("archer") && GameConstants.isArcher(Short.valueOf(jobs))) {
                     return true;
                  }

                  if (job.equals("rogue") && GameConstants.isThief(Short.valueOf(jobs))) {
                     return true;
                  }

                  if (job.equals("pirate") && GameConstants.isPirate(Short.valueOf(jobs))) {
                     return true;
                  }

                  if (GameConstants.JobCodeCheck(Short.valueOf(job), Short.valueOf(jobs))) {
                     return true;
                  }

                  if (GameConstants.JobCodeCheck(Short.valueOf(jobs), Short.valueOf(job))) {
                     return true;
                  }
                  continue;
               }

               return true;
            }

            return true;
         }
      }

      return false;
   }

   public static boolean isNumeric(String s) {
      try {
         Double.parseDouble(s);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   public static boolean checkOwnUseableJobs(Pair<Core, List<String>> data, MapleClient c) {
      int jobcode = c.getPlayer().getJob();
      List<String> list = (List)data.getRight();
      if (((Core)data.getLeft()).getCoreId() != 10000024 && ((Core)data.getLeft()).getCoreId() != 10000031) {
         Iterator var4 = list.iterator();

         String jobs;
         do {
            if (!var4.hasNext()) {
               return false;
            }

            jobs = (String)var4.next();
         } while(!isNumeric(jobs) || !GameConstants.JobCodeCheck(Short.valueOf(jobs), jobcode));

         return true;
      } else {
         return false;
      }
   }

   public static boolean checkUseableJobs(Pair<Core, List<String>> data, MapleClient c) {
      int jobcode = c.getPlayer().getJob();
      List<String> list = (List)data.getRight();
      if (((Core)data.getLeft()).getCoreId() != 10000024 && ((Core)data.getLeft()).getCoreId() != 10000031) {
         Iterator var4 = list.iterator();

         String jobs;
         do {
            if (!var4.hasNext()) {
               return false;
            }

            jobs = (String)var4.next();
            if (jobs.equals("none")) {
               return true;
            }

            if (jobs.equals("all")) {
               return true;
            }

            if (jobs.equals("warrior") && GameConstants.isWarrior(jobcode)) {
               return true;
            }

            if (jobs.equals("magician") && GameConstants.isMagician(jobcode)) {
               return true;
            }

            if (jobs.equals("archer") && GameConstants.isArcher(jobcode)) {
               return true;
            }

            if (jobs.equals("rogue") && GameConstants.isThief(jobcode)) {
               return true;
            }

            if (jobs.equals("pirate") && GameConstants.isPirate(jobcode)) {
               return true;
            }
         } while(!isNumeric(jobs) || !GameConstants.JobCodeCheck(Short.valueOf(jobs), jobcode));

         return true;
      } else {
         return false;
      }
   }

   public static boolean ResetCore(MapleClient c, Pair<Core, List<String>> origin, Pair<Core, List<String>> fresh, boolean checkjob) {
      return ((Core)origin.getLeft()).getCoreId() == ((Core)fresh.getLeft()).getCoreId() || ((Core)fresh.getLeft()).getCoreId() / 10000000 != 2 || ((Core)origin.getLeft()).getSkill1() == ((Core)fresh.getLeft()).getSkill1() && ((Core)origin.getLeft()).getSkill1() != 0 && ((Core)fresh.getLeft()).getSkill1() != 0 || ((Core)origin.getLeft()).getSkill2() == ((Core)fresh.getLeft()).getSkill2() && ((Core)origin.getLeft()).getSkill2() != 0 && ((Core)fresh.getLeft()).getSkill2() != 0 || ((Core)origin.getLeft()).getSkill3() == ((Core)fresh.getLeft()).getSkill3() && ((Core)origin.getLeft()).getSkill3() != 0 && ((Core)fresh.getLeft()).getSkill3() != 0 || checkjob && !CheckUseableJobs((List)origin.getRight(), (List)fresh.getRight()) || !checkUseableJobs(fresh, c);
   }

   public static boolean ResetCore(MapleClient c, Pair<Core, List<String>> origin, Pair<Core, List<String>> fresh, Pair<Core, List<String>> fresh2, boolean checkjob) {
      return ((Core)origin.getLeft()).getCoreId() == ((Core)fresh.getLeft()).getCoreId() || ((Core)fresh.getLeft()).getCoreId() / 10000000 != 2 || ((Core)origin.getLeft()).getSkill1() == ((Core)fresh.getLeft()).getSkill1() && ((Core)origin.getLeft()).getSkill1() != 0 && ((Core)fresh.getLeft()).getSkill1() != 0 || ((Core)origin.getLeft()).getSkill2() == ((Core)fresh.getLeft()).getSkill2() && ((Core)origin.getLeft()).getSkill2() != 0 && ((Core)fresh.getLeft()).getSkill2() != 0 || ((Core)origin.getLeft()).getSkill3() == ((Core)fresh.getLeft()).getSkill3() && ((Core)origin.getLeft()).getSkill3() != 0 && ((Core)fresh.getLeft()).getSkill3() != 0 || ((Core)origin.getLeft()).getSkill1() == ((Core)fresh2.getLeft()).getSkill1() && ((Core)origin.getLeft()).getSkill1() != 0 && ((Core)fresh2.getLeft()).getSkill1() != 0 || ((Core)origin.getLeft()).getSkill2() == ((Core)fresh2.getLeft()).getSkill2() && ((Core)origin.getLeft()).getSkill2() != 0 && ((Core)fresh2.getLeft()).getSkill2() != 0 || ((Core)origin.getLeft()).getSkill3() == ((Core)fresh2.getLeft()).getSkill3() && ((Core)origin.getLeft()).getSkill3() != 0 && ((Core)fresh2.getLeft()).getSkill3() != 0 || checkjob && !CheckUseableJobs((List)origin.getRight(), (List)fresh.getRight()) || !checkUseableJobs(fresh, c);
   }

   public static void UseMirrorCoreJamStone(MapleClient c, int itemid, long crcid) {
      crcid = Randomizer.nextLong();
      if (c.getPlayer().getCore().size() >= 200) {
         c.getPlayer().dropMessage(1, "코어는 최대 200개까지 보유하실 수 있습니다.");
      } else {
         if (c.getPlayer().haveItem(itemid)) {
            MapleInventoryManipulator.removeById_Lock(c, GameConstants.getInventoryType(itemid), itemid);
            Core core = new Core(crcid, 10000024, c.getPlayer().getId(), 1, 0, 1, 25, 400001039, 0, 0, -1, (SpecialCoreOption)null);
            c.getPlayer().getCore().add(core);
            core.setId(c.getPlayer().getCore().indexOf(core));
            c.getSession().writeAndFlush(CWvsContext.AddCore(core));
         }

         c.getSession().writeAndFlush(CWvsContext.UpdateCore(c.getPlayer()));
      }
   }

   public static void UseCraftCoreJamStone(MapleClient c, int itemid, long crcid) {
      crcid = Randomizer.nextLong();
      if (c.getPlayer().getCore().size() >= 200) {
         c.getPlayer().dropMessage(1, "코어는 최대 200개까지 보유하실 수 있습니다.");
      } else {
         if (c.getPlayer().haveItem(itemid)) {
            MapleInventoryManipulator.removeById_Lock(c, GameConstants.getInventoryType(itemid), itemid);
            Core core = new Core(crcid, 10000031, c.getPlayer().getId(), 1, 0, 1, 25, 400001059, 0, 0, -1, (SpecialCoreOption)null);
            c.getPlayer().getCore().add(core);
            core.setId(c.getPlayer().getCore().indexOf(core));
            c.getSession().writeAndFlush(CWvsContext.AddCore(core));
         }

         c.getSession().writeAndFlush(CWvsContext.UpdateCore(c.getPlayer()));
      }
   }

   public static void UseEnforcedCoreJamStone(MapleClient c, int itemid, long crcid) {
      if (c.getPlayer().getCore().size() >= 200) {
         c.getPlayer().dropMessage(1, "코어는 최대 200개까지 보유하실 수 있습니다.");
      } else {
         if (c.getPlayer().haveItem(itemid) && MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemid), itemid, 1, false, false)) {
            Core core = new Core(crcid, 40000000, c.getPlayer().getId(), 1, 0, 1, 0, 1, 0, 0, -1, (SpecialCoreOption)null);
            c.getPlayer().getCore().add(core);
            core.setId(c.getPlayer().getCore().indexOf(core));
            c.getSession().writeAndFlush(CWvsContext.AddCore(core));
         }

         c.getSession().writeAndFlush(CWvsContext.UpdateCore(c.getPlayer()));
      }
   }

   public static void UseCoreJamStone(MapleClient c, int itemid, long crcid) {
      crcid = Randomizer.nextLong();
      if (c.getPlayer().getCore().size() >= 200) {
         c.getPlayer().dropMessage(1, "코어는 최대 200개까지 보유하실 수 있습니다.");
      } else if (c.getPlayer().getLevel() >= 200 && !GameConstants.isYeti(c.getPlayer().getJob()) && !GameConstants.isPinkBean(c.getPlayer().getJob())) {
         if (c.getPlayer().haveItem(itemid)) {
            MapleInventoryManipulator.removeById_Lock(c, GameConstants.getInventoryType(itemid), itemid);
            int rand = Randomizer.nextInt(100);
            boolean sp = false;
            Pair skill1;
            Pair skill2;
            Pair skill3;
            int rand2;
            if (rand < 5) {
               rand2 = Randomizer.nextInt(specialCores.size());
               skill1 = (Pair)specialCores.get(rand2);
               skill2 = null;

               for(skill3 = null; !checkUseableJobs(skill1, c); skill1 = (Pair)specialCores.get(Randomizer.nextInt(specialCores.size()))) {
               }

               sp = true;
            } else if (rand >= 85) {
               rand2 = Randomizer.nextInt(activeCores.size());
               skill1 = (Pair)activeCores.get(rand2);
               skill2 = null;

               for(skill3 = null; !checkUseableJobs(skill1, c); skill1 = (Pair)activeCores.get(Randomizer.nextInt(activeCores.size()))) {
               }
            } else {
               rand2 = Randomizer.nextInt(passiveCores.size());
               int rand3 = Randomizer.nextInt(passiveCores.size());
               int rand4 = Randomizer.nextInt(passiveCores.size());
               skill1 = (Pair)passiveCores.get(rand2);
               skill2 = (Pair)passiveCores.get(rand3);

               for(skill3 = (Pair)passiveCores.get(rand4); !checkUseableJobs(skill1, c); skill1 = (Pair)passiveCores.get(Randomizer.nextInt(passiveCores.size()))) {
               }

               while(ResetCore(c, skill1, skill2, true)) {
                  skill2 = (Pair)passiveCores.get(Randomizer.nextInt(passiveCores.size()));
               }

               while(ResetCore(c, skill3, skill1, skill2, true)) {
                  skill3 = (Pair)passiveCores.get(Randomizer.nextInt(passiveCores.size()));
               }
            }

            Pair<Core, List<String>> tempCore = (Pair)cores.get(((Core)skill1.getLeft()).getCoreId());
            SpecialCoreOption spCore = null;
            if (tempCore != null) {
               spCore = ((Core)tempCore.left).getSpCoreOption();
            }

            Core core = new Core(crcid, ((Core)skill1.getLeft()).getCoreId(), c.getPlayer().getId(), 1, 0, 1, ((Core)skill1.getLeft()).getMaxlevel(), ((Core)skill1.getLeft()).getSkill1(), skill2 == null ? 0 : ((Core)skill2.getLeft()).getSkill1(), skill3 == null ? 0 : ((Core)skill3.getLeft()).getSkill1(), -1, spCore);
            if (sp) {
               core.setPeriod(System.currentTimeMillis() + 604800000L);
            }

            c.getPlayer().getCore().add(core);
            core.setId(c.getPlayer().getCore().indexOf(core));
            c.getSession().writeAndFlush(CWvsContext.AddCore(core));
         }

         c.getSession().writeAndFlush(CWvsContext.UpdateCore(c.getPlayer()));
      }
   }

   public static void gainVCoreLevel(MapleCharacter player) {
      Iterator var1 = activeCores.iterator();

      while(var1.hasNext()) {
         Pair<Core, List<String>> coreskill = (Pair)var1.next();
         if (checkOwnUseableJobs(coreskill, player.getClient())) {
            Core core = new Core(Randomizer.nextLong(), ((Core)coreskill.getLeft()).getCoreId(), player.getId(), 1, 0, 1, ((Core)coreskill.getLeft()).getMaxlevel(), ((Core)coreskill.getLeft()).getSkill1(), 0, 0, -1, ((Core)coreskill.getLeft()).getSpCoreOption());
            player.getCore().add(core);
            core.setId(player.getCore().indexOf(core));
         }
      }

      player.getClient().getSession().writeAndFlush(CWvsContext.UpdateCore(player));
      player.getClient().getSession().writeAndFlush(CWvsContext.enableActions(player));
   }

   public static void updateCore(LittleEndianAccessor slea, MapleClient c) {
      int state = slea.readInt();
      int coreId;
      int position;
      Iterator var6;
      Core core3;
      int i;
      int nCount;
      VMatrix matrix3;
      VMatrix matrix2;
      Core core;
      Core core2;
      ArrayList removes;
      VMatrix matrix;
      Core prevCore;
      int exp;
      Pair skill2;
      Core core5;
      label320:
      switch(state) {
      case 0:
         coreId = slea.readInt();
         nCount = slea.readInt();
         slea.skip(4);
         position = slea.readInt();
         if (position < 0) {
            position = 0;

            for(var6 = c.getPlayer().getMatrixs().iterator(); var6.hasNext(); ++position) {
               matrix = (VMatrix)var6.next();
               if (matrix.getId() == -1) {
                  break;
               }
            }
         }

         core = corefromId(c.getPlayer(), coreId);
         matrix = VMatrixFromPos(c.getPlayer(), position);
         if (core == null || matrix == null) {
            c.getPlayer().dropMessage(6, "매트릭스 장착 오류가 발생했습니다.");
            return;
         }

         if (!matrix.isUnLock() && position > c.getPlayer().getLevel() / 5 - 36) {
            c.getPlayer().dropMessage(6, "착용이 불가능합니다.");
            return;
         }

         if (nCount >= 0) {
            prevCore = (Core)c.getPlayer().getCore().get(nCount);
            if (prevCore.getPosition() != -1 && prevCore.getState() != 1) {
               prevCore.setState(1);
               prevCore.setPosition(-1);
               core.setState(2);
               core.setPosition(position);
               matrix.setId(coreId);
            } else {
               c.getPlayer().dropMessage(6, "코어 장착 도중 오류가 발생했습니다.");
            }
         } else if (core.getPosition() < 0 && core.getState() != 2) {
            core.setState(2);
            core.setPosition(position);
            matrix.setId(coreId);
         } else {
            c.getPlayer().dropMessage(6, "이미 착용중인 코어입니다.");
         }

         c.send(CWvsContext.UpdateCore(c.getPlayer(), 1));
         calcSkillLevel(c.getPlayer(), -1);
         c.getPlayer().setEqpSpCore(c.getPlayer().getEquippedSpecialCore());
         break;
      case 1:
         coreId = slea.readInt();
         slea.skip(4);
         core2 = corefromId(c.getPlayer(), coreId);
         matrix2 = VMatrixFromPos(c.getPlayer(), core2.getPosition());
         if (core2 != null && matrix2 != null) {
            if (core2.getPosition() != -1 && core2.getState() != 1) {
               if (c.getPlayer().getCooldownLimit(core2.getSkill1()) > 0L) {
                  c.getPlayer().dropMessage(6, "재사용 대기시간 중인 코어는 해제할 수 없습니다.");
                  return;
               }

               core2.setState(1);
               core2.setPosition(-1);
               matrix2.setId(-1);
            } else {
               c.getPlayer().dropMessage(6, "미착용중인 코어입니다.");
            }

            c.send(CWvsContext.UpdateCore(c.getPlayer(), -1));
            calcSkillLevel(c.getPlayer(), -1);
            break;
         }

         c.getPlayer().dropMessage(6, "매트릭스 해제 오류가 발생했습니다.");
         return;
      case 2:
         coreId = slea.readInt();
         nCount = slea.readInt();
         position = slea.readInt();
         exp = slea.readInt();
         matrix = VMatrixFromPos(c.getPlayer(), position);
         VMatrix sourceMatrix = VMatrixFromPos(c.getPlayer(), exp);
         skill2 = null;
         Core sourceCore = null;
         Core targetCore = (Core)c.getPlayer().getCore().get(coreId);
         if (nCount != -1) {
            sourceCore = (Core)c.getPlayer().getCore().get(nCount);
         }

         if (matrix == null || sourceMatrix == null && nCount != -1) {
            c.getPlayer().dropMessage(6, "매트릭스 교체 오류가 발생했습니다.");
            return;
         }

         if (c.getPlayer().getCooldownLimit(targetCore.getSkill1()) > 0L || sourceCore != null && c.getPlayer().getCooldownLimit(sourceCore.getSkill1()) > 0L) {
            c.getPlayer().dropMessage(6, "재사용 대기시간 중인 코어는 해제할 수 없습니다.");
            return;
         }

         targetCore.setPosition(exp);
         matrix.setId(nCount);
         if (sourceCore != null) {
            sourceCore.setPosition(position);
            sourceMatrix.setId(coreId);
         }

         calcSkillLevel(c.getPlayer(), -1);
         break;
      case 3:
         System.out.println("New state " + state + " detected : " + slea);
         break;
      case 4:
         coreId = slea.readInt();
         nCount = slea.readInt();
         removes = new ArrayList();
         exp = 0;
         core3 = corefromId(c.getPlayer(), coreId);
         if (core3 == null) {
            c.getPlayer().dropMessage(6, "매트릭스 강화 오류가 발생했습니다.");
            return;
         }

         int prevLevel = core3.getLevel();

         for(i = 0; i < nCount; ++i) {
            int source = slea.readInt();
            core5 = (Core)c.getPlayer().getCore().get(source);
            int gainExp = expByLevel(core5);
            core3.setExp(core3.getExp() + gainExp);
            exp += gainExp;
            core5.setState(0);
            core5.setExp(0);
            core5.setLevel(0);
            removes.add(core5);
         }

         c.getPlayer().getCore().removeAll(removes);

         while(core3.getExp() >= neededLevelUpExp(core3)) {
            core3.setExp(core3.getExp() - neededLevelUpExp(core3));
            core3.setLevel(core3.getLevel() + 1);
            if (core3.getLevel() >= 25) {
               core3.setLevel(25);
               core3.setExp(0);
               break;
            }
         }

         calcSkillLevel(c.getPlayer(), 3);
         c.getSession().writeAndFlush(CWvsContext.OnCoreEnforcementResult(coreId, exp, prevLevel, core3.getLevel()));
         break;
      case 5:
         coreId = slea.readInt();
         slea.skip(4);
         core2 = corefromId(c.getPlayer(), coreId);
         if (core2 == null) {
            c.getPlayer().dropMessage(6, "매트릭스 분해 오류가 발생했습니다.");
            return;
         }

         position = core2.getCoreId() / 10000000;
         exp = 0;
         switch(position) {
         case 1:
            exp = 2 * core2.getLevel() * (core2.getLevel() + 19);
            break;
         case 2:
            exp = (3 * core2.getLevel() * core2.getLevel() + 13 * core2.getLevel() + 4) / 2;
            break;
         case 3:
            exp = 50;
         }

         c.getPlayer().setKeyValue(1477, "count", String.valueOf(c.getPlayer().getKeyValue(1477, "count") + (long)exp));
         c.getPlayer().getCore().remove(coreId);
         calcSkillLevel(c.getPlayer(), 5);
         c.getSession().writeAndFlush(CWvsContext.DeleteCore(exp));
         break;
      case 6:
         coreId = slea.readInt();
         nCount = 0;
         removes = new ArrayList();

         for(exp = 0; exp < coreId; ++exp) {
            int source2 = slea.readInt();
            prevCore = corefromId(c.getPlayer(), source2);
            if (prevCore == null) {
               c.getPlayer().dropMessage(6, "매트릭스 다중 분해 오류가 발생했습니다.");
            } else {
               i = prevCore.getCoreId() / 10000000;
               switch(i) {
               case 1:
                  nCount += 2 * prevCore.getLevel() * (prevCore.getLevel() + 19);
                  break;
               case 2:
                  nCount += (3 * prevCore.getLevel() * prevCore.getLevel() + 13 * prevCore.getLevel() + 4) / 2;
                  break;
               case 3:
                  nCount += 50;
               }

               removes.add(prevCore);
            }
         }

         c.getPlayer().setKeyValue(1477, "count", String.valueOf(c.getPlayer().getKeyValue(1477, "count") + (long)nCount));
         c.getPlayer().getCore().removeAll(removes);
         calcSkillLevel(c.getPlayer(), 5);
         c.getSession().writeAndFlush(CWvsContext.DeleteCore(nCount));
         break;
      case 7:
         coreId = slea.readInt();
         nCount = slea.readInt();
         List<Pair<Core, List<String>>> cores = new ArrayList();
         int lostpont = 0;
         switch(coreId / 10000000) {
         case 1:
            cores = activeCores;
            lostpont = 140;
            break;
         case 2:
            cores = passiveCores;
            lostpont = 70;
            break;
         case 3:
            cores = specialCores;
            lostpont = 250;
         }

         Iterator var22 = ((List)cores).iterator();

         while(true) {
            while(true) {
               Pair skill1;
               do {
                  if (!var22.hasNext()) {
                     break label320;
                  }

                  skill1 = (Pair)var22.next();
               } while(((Core)skill1.getLeft()).getCoreId() != coreId);

               Pair skill4;
               if (nCount == 1) {
                  skill2 = (Pair)((List)cores).get(Randomizer.nextInt(((List)cores).size()));
                  skill4 = (Pair)((List)cores).get(Randomizer.nextInt(((List)cores).size()));
                  if (((Core)skill1.getLeft()).getCoreId() / 10000000 != 2) {
                     skill2 = null;
                     skill4 = null;
                  } else {
                     while(ResetCore(c, skill1, skill2, true)) {
                        skill2 = (Pair)((List)cores).get(Randomizer.nextInt(((List)cores).size()));
                     }

                     while(ResetCore(c, skill4, skill1, skill2, true)) {
                        skill4 = (Pair)((List)cores).get(Randomizer.nextInt(((List)cores).size()));
                     }
                  }

                  core5 = new Core(Randomizer.nextLong(), ((Core)skill1.getLeft()).getCoreId(), c.getPlayer().getId(), 1, 0, 1, ((Core)skill1.getLeft()).getMaxlevel(), ((Core)skill1.getLeft()).getSkill1(), skill2 == null ? 0 : ((Core)skill2.getLeft()).getSkill1(), skill4 == null ? 0 : ((Core)skill4.getLeft()).getSkill1(), -1, ((Core)skill1.getLeft()).getSpCoreOption());
                  c.getPlayer().getCore().add(core5);
                  core5.setId(c.getPlayer().getCore().indexOf(core5));
                  c.getSession().writeAndFlush(CWvsContext.ViewNewCore(core5, nCount));
                  c.getSession().writeAndFlush(CWvsContext.UpdateCore(c.getPlayer(), core5.getId()));
                  c.getPlayer().setKeyValue(1477, "count", String.valueOf(c.getPlayer().getKeyValue(1477, "count") - (long)lostpont));
               } else {
                  for(i = 0; i < nCount; ++i) {
                     skill4 = (Pair)((List)cores).get(Randomizer.nextInt(((List)cores).size()));
                     Pair<Core, List<String>> skill5 = (Pair)((List)cores).get(Randomizer.nextInt(((List)cores).size()));
                     if (((Core)skill1.getLeft()).getCoreId() / 10000000 != 2) {
                        skill4 = null;
                        skill5 = null;
                     } else {
                        while(ResetCore(c, skill1, skill4, true)) {
                           skill4 = (Pair)((List)cores).get(Randomizer.nextInt(((List)cores).size()));
                        }

                        while(ResetCore(c, skill1, skill5, true)) {
                           skill5 = (Pair)((List)cores).get(Randomizer.nextInt(((List)cores).size()));
                        }

                        while(ResetCore(c, skill4, skill5, true)) {
                           skill5 = (Pair)((List)cores).get(Randomizer.nextInt(((List)cores).size()));
                        }
                     }

                     Core core6 = new Core(Randomizer.nextLong(), ((Core)skill1.getLeft()).getCoreId(), c.getPlayer().getId(), 1, 0, 1, ((Core)skill1.getLeft()).getMaxlevel(), ((Core)skill1.getLeft()).getSkill1(), skill4 == null ? 0 : ((Core)skill4.getLeft()).getSkill1(), skill5 == null ? 0 : ((Core)skill5.getLeft()).getSkill1(), -1, ((Core)skill1.getLeft()).getSpCoreOption());
                     c.getPlayer().getCore().add(core6);
                     core6.setId(c.getPlayer().getCore().indexOf(core6));
                     c.getSession().writeAndFlush(CWvsContext.UpdateCore(c.getPlayer(), core6.getId()));
                  }

                  c.getPlayer().setKeyValue(1477, "count", String.valueOf(c.getPlayer().getKeyValue(1477, "count") - (long)(nCount * lostpont)));
                  c.getSession().writeAndFlush(CWvsContext.ViewNewCore((Core)skill1.getLeft(), nCount));
               }
            }
         }
      case 8:
      case 12:
      default:
         System.out.println("New state " + state + " detected : " + slea);
         break;
      case 9:
         coreId = slea.readInt();
         slea.skip(4);
         matrix3 = VMatrixFromPos(c.getPlayer(), coreId);
         if (matrix3 == null) {
            c.getPlayer().dropMessage(6, "매트릭스 강화 오류가 발생했습니다.");
            return;
         }

         matrix3.setLevel(Math.min(5, ((VMatrix)c.getPlayer().getMatrixs().get(coreId)).getLevel() + 1));
         calcSkillLevel(c.getPlayer(), -1);
         break;
      case 10:
         coreId = slea.readInt();
         slea.skip(4);
         Iterator var16 = c.getPlayer().getMatrixs().iterator();

         while(var16.hasNext()) {
            matrix2 = (VMatrix)var16.next();
            if (matrix2.getPosition() == coreId) {
               matrix2.setUnLock(true);
               break;
            }
         }

         c.getPlayer().gainMeso(-GameConstants.MatrixSlotAddMeso(c.getPlayer().getLevel(), coreId), false, true);
         c.send(CWvsContext.UpdateCore(c.getPlayer()));
         break;
      case 11:
         Iterator var13 = c.getPlayer().getMatrixs().iterator();

         while(var13.hasNext()) {
            matrix3 = (VMatrix)var13.next();
            matrix3.setLevel(0);
         }

         calcSkillLevel(c.getPlayer(), -1);
         break;
      case 13:
         coreId = slea.readInt();
         nCount = 0;

         for(Iterator var17 = c.getPlayer().getCore().iterator(); var17.hasNext(); ++nCount) {
            core = (Core)var17.next();
            if (nCount == coreId) {
               core.setLock(true);
            }
         }

         c.getSession().writeAndFlush(CWvsContext.UpdateCore(c.getPlayer()));
         break;
      case 14:
         coreId = slea.readInt();
         String secondPassword = slea.readMapleAsciiString();
         if (c.CheckSecondPassword(secondPassword)) {
            position = 0;

            for(var6 = c.getPlayer().getCore().iterator(); var6.hasNext(); ++position) {
               core3 = (Core)var6.next();
               if (position == coreId) {
                  core3.setLock(false);
               }
            }

            c.getSession().writeAndFlush(CWvsContext.UpdateCore(c.getPlayer()));
         }
      }

      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   private static int neededLevelUpExp(Core core) {
      int type = core.getCoreId() / 10000000;
      return type == 1 ? 5 * core.getLevel() + 50 : 15 * core.getLevel() + 40;
   }

   private static int expByLevel(Core core) {
      if (core.getCoreId() / 10000000 == 4) {
         return 150;
      } else {
         int a = core.getExp();

         for(int i = 0; i < core.getLevel(); ++i) {
            a += 50 + i * 5;
         }

         return a;
      }
   }

   public static void gainMatrix(MapleCharacter chr) {
      List matrixs = chr.getMatrixs();

      while(matrixs.size() < 26) {
         matrixs.add(new VMatrix(-1, matrixs.size(), 0, false));
      }

      chr.setMatrixs(matrixs);
      chr.getClient().getSession().writeAndFlush(CWvsContext.UpdateCore(chr));
   }

   public static VMatrix VMatrixFromPos(MapleCharacter player, int pos) {
      Iterator var2 = player.getMatrixs().iterator();

      VMatrix ma;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         ma = (VMatrix)var2.next();
      } while(ma.getPosition() != pos);

      return ma;
   }

   public static Core corefromId(MapleCharacter player, int id) {
      Iterator var2 = player.getCore().iterator();

      Core core;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         core = (Core)var2.next();
      } while(core.getId() != id);

      return core;
   }

   public static void calcSkillLevel(MapleCharacter player, int position) {
      Map<Skill, SkillEntry> updateSkills = new HashMap();
      Iterator var3 = player.getSkills().entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Skill, SkillEntry> skill = (Entry)var3.next();
         if (((Skill)skill.getKey()).isVMatrix()) {
            updateSkills.put((Skill)skill.getKey(), new SkillEntry(0, (byte)0, -1L));
         }
      }

      var3 = player.getMatrixs().iterator();

      while(var3.hasNext()) {
         VMatrix matrix = (VMatrix)var3.next();
         matrix.setId(-1);
      }

      player.changeSkillsLevel(updateSkills);
      updateSkills.clear();
      Map<Integer, Integer> addSkills = new HashMap();
      Iterator var9 = player.getCore().iterator();

      while(true) {
         while(true) {
            Core core;
            do {
               do {
                  if (!var9.hasNext()) {
                     var9 = addSkills.entrySet().iterator();

                     while(var9.hasNext()) {
                        Entry<Integer, Integer> addSkill = (Entry)var9.next();
                        if (SkillFactory.getSkill((Integer)addSkill.getKey()) != null) {
                           updateSkills.put(SkillFactory.getSkill((Integer)addSkill.getKey()), new SkillEntry((Integer)addSkill.getValue(), (byte)SkillFactory.getSkill((Integer)addSkill.getKey()).getMasterLevel(), -1L));
                        }
                     }

                     player.changeSkillsLevel(updateSkills);
                     if (position != -1) {
                        player.getClient().getSession().writeAndFlush(CWvsContext.UpdateCore(player, 1, position));
                     } else {
                        player.getClient().getSession().writeAndFlush(CWvsContext.UpdateCore(player));
                     }

                     return;
                  }

                  core = (Core)var9.next();
                  core.setId(player.getCore().indexOf(core));
               } while(core.getState() != 2);
            } while(core.getPosition() < 0);

            if (core.getPosition() >= 28) {
               core.setState(1);
               core.setPosition(-1);
            } else {
               VMatrix matrix2 = VMatrixFromPos(player, core.getPosition());
               if (matrix2 != null) {
                  if (matrix2.getId() != core.getId()) {
                     matrix2.setId(core.getId());
                  }

                  if (core.getSkill1() != 0) {
                     if (addSkills.containsKey(core.getSkill1())) {
                        addSkills.put(core.getSkill1(), (Integer)addSkills.get(core.getSkill1()) + core.getLevel() + Math.max(0, matrix2.getLevel()));
                     } else {
                        addSkills.put(core.getSkill1(), core.getLevel() + Math.max(0, matrix2.getLevel()));
                     }

                     if (core.getSkill1() == 400051000 && (GameConstants.isStriker(player.getJob()) || GameConstants.isArk(player.getJob()) || GameConstants.isEunWol(player.getJob()) || GameConstants.isAngelicBuster(player.getJob()) || GameConstants.isXenon(player.getJob()))) {
                        addSkills.put(400051001, core.getLevel() + Math.max(0, matrix2.getLevel()));
                     }
                  }

                  if (core.getSkill2() != 0) {
                     if (addSkills.containsKey(core.getSkill2())) {
                        addSkills.put(core.getSkill2(), (Integer)addSkills.get(core.getSkill2()) + core.getLevel() + Math.max(0, matrix2.getLevel()));
                     } else {
                        addSkills.put(core.getSkill2(), core.getLevel() + Math.max(0, matrix2.getLevel()));
                     }
                  }

                  if (core.getSkill3() != 0) {
                     if (addSkills.containsKey(core.getSkill3())) {
                        addSkills.put(core.getSkill3(), (Integer)addSkills.get(core.getSkill3()) + core.getLevel() + Math.max(0, matrix2.getLevel()));
                     } else {
                        addSkills.put(core.getSkill3(), core.getLevel() + Math.max(0, matrix2.getLevel()));
                     }
                  }
               }
            }
         }
      }
   }

   public static Map<Integer, Pair<Core, List<String>>> getCores() {
      return cores;
   }

   public static void setCores(Map<Integer, Pair<Core, List<String>>> cores) {
      MatrixHandler.cores = cores;
   }
}
