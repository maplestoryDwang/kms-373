package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import constants.GameConstants;
import java.util.EnumMap;
import java.util.Iterator;
import server.Randomizer;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class StatsHandling {
   public static final void DistributeAP(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      EnumMap<MapleStat, Long> statupdate = new EnumMap(MapleStat.class);
      c.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statupdate, true, chr));
      slea.readInt();
      PlayerStats stat = chr.getStat();
      short job = chr.getJob();
      if (chr.getRemainingAp() > 0) {
         long maxmp;
         switch(slea.readInt()) {
         case 64:
            if (stat.getStr() >= 32767) {
               return;
            }

            stat.setStr((short)(stat.getStr() + 1), chr);
            statupdate.put(MapleStat.STR, (long)stat.getStr());
            break;
         case 128:
            if (stat.getDex() >= 32767) {
               return;
            }

            stat.setDex((short)(stat.getDex() + 1), chr);
            statupdate.put(MapleStat.DEX, (long)stat.getDex());
            break;
         case 256:
            if (stat.getInt() >= 32767) {
               return;
            }

            stat.setInt((short)(stat.getInt() + 1), chr);
            statupdate.put(MapleStat.INT, (long)stat.getInt());
            break;
         case 512:
            if (stat.getLuk() >= 32767) {
               return;
            }

            stat.setLuk((short)(stat.getLuk() + 1), chr);
            statupdate.put(MapleStat.LUK, (long)stat.getLuk());
            break;
         case 2048:
            maxmp = stat.getMaxHp();
            if (chr.getHpApUsed() >= 10000 || maxmp >= 500000L) {
               return;
            }

            maxmp = GameConstants.isBeginnerJob(job) ? maxmp + (long)Randomizer.rand(8, 12) : (job >= 100 && job <= 132 || job >= 3200 && job <= 3212 || job >= 1100 && job <= 1112 || job >= 3100 && job <= 3112 ? maxmp + (long)Randomizer.rand(36, 42) : ((job < 200 || job > 232) && !GameConstants.isEvan(job) ? (job >= 300 && job <= 322 || job >= 400 && job <= 434 || job >= 1300 && job <= 1312 || job >= 1400 && job <= 1412 || job >= 3300 && job <= 3312 || job >= 2300 && job <= 2312 ? maxmp + (long)Randomizer.rand(16, 20) : (job >= 510 && job <= 512 || job >= 1510 && job <= 1512 ? maxmp + (long)Randomizer.rand(28, 32) : ((job < 500 || job > 532) && (job < 3500 || job > 3512) && job != 1500 ? (job >= 1200 && job <= 1212 ? maxmp + (long)Randomizer.rand(15, 21) : (job >= 2000 && job <= 2112 ? maxmp + (long)Randomizer.rand(38, 42) : maxmp + (long)Randomizer.rand(50, 100))) : maxmp + (long)Randomizer.rand(18, 22)))) : maxmp + (long)Randomizer.rand(10, 20)));
            maxmp = Math.min(500000L, Math.abs(maxmp));
            chr.setHpApUsed((short)(chr.getHpApUsed() + 1));
            stat.setMaxHp(maxmp, chr);
            statupdate.put(MapleStat.MAXHP, maxmp);
            break;
         case 8192:
            maxmp = stat.getMaxMp();
            if (chr.getHpApUsed() >= 10000 || stat.getMaxMp() >= 500000L) {
               return;
            }

            if (GameConstants.isBeginnerJob(job)) {
               maxmp += (long)Randomizer.rand(6, 8);
            } else {
               if (job >= 3100 && job <= 3112) {
                  return;
               }

               maxmp = (job < 200 || job > 232) && !GameConstants.isEvan(job) && (job < 3200 || job > 3212) && (job < 1200 || job > 1212) ? ((job < 300 || job > 322) && (job < 400 || job > 434) && (job < 500 || job > 532) && (job < 3200 || job > 3212) && (job < 3500 || job > 3512) && (job < 1300 || job > 1312) && (job < 1400 || job > 1412) && (job < 1500 || job > 1512) && (job < 2300 || job > 2312) ? ((job < 100 || job > 132) && (job < 1100 || job > 1112) && (job < 2000 || job > 2112) ? maxmp + (long)Randomizer.rand(50, 100) : maxmp + (long)Randomizer.rand(6, 9)) : maxmp + (long)Randomizer.rand(10, 12)) : maxmp + (long)Randomizer.rand(38, 40);
            }

            maxmp = Math.min(500000L, Math.abs(maxmp));
            chr.setHpApUsed((short)(chr.getHpApUsed() + 1));
            stat.setMaxMp(maxmp, chr);
            statupdate.put(MapleStat.MAXMP, maxmp);
            break;
         default:
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            return;
         }

         chr.setRemainingAp((short)(chr.getRemainingAp() - 1));
         statupdate.put(MapleStat.AVAILABLEAP, (long)chr.getRemainingAp());
         c.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statupdate, false, chr));
      }

   }

   public static final void DistributeSP(int skillid, int count, MapleClient c, MapleCharacter chr) {
      boolean isBeginnerSkill = false;
      int remainingSp;
      int maxlevel;
      int curLevel;
      int skillbook;
      if (!GameConstants.isBeginnerJob(skillid / 10000) || skillid % 10000 != 1000 && skillid % 10000 != 1001 && skillid % 10000 != 1002 && skillid % 10000 != 2) {
         if (GameConstants.isBeginnerJob(skillid / 10000)) {
            return;
         }

         remainingSp = chr.getRemainingSp(GameConstants.getSkillBookForSkill(skillid));
      } else {
         boolean resistance = skillid / 10000 == 3000 || skillid / 10000 == 3001;
         maxlevel = chr.getSkillLevel(SkillFactory.getSkill(skillid / 10000 * 10000 + 1000));
         curLevel = chr.getSkillLevel(SkillFactory.getSkill(skillid / 10000 * 10000 + 1001));
         skillbook = chr.getSkillLevel(SkillFactory.getSkill(skillid / 10000 * 10000 + (resistance ? 2 : 1002)));
         remainingSp = Math.min(chr.getLevel() - 1, resistance ? 9 : 6) - maxlevel - curLevel - skillbook;
         isBeginnerSkill = true;
      }

      Skill skill = SkillFactory.getSkill(skillid);
      Iterator var14 = skill.getRequiredSkills().iterator();

      while(true) {
         if (!var14.hasNext()) {
            maxlevel = skill.isFourthJob() ? chr.getMasterLevel(skill) : skill.getMaxLevel();
            curLevel = chr.getSkillLevel(skill);
            if (!skill.isInvisible() || chr.getSkillLevel(skill) != 0 || (!skill.isFourthJob() || chr.getMasterLevel(skill) != 0) && (skill.isFourthJob() || maxlevel >= 10 || isBeginnerSkill)) {
               int[] var16 = GameConstants.blockedSkills;
               int var10 = var16.length;

               for(int var11 = 0; var11 < var10; ++var11) {
                  int i = var16[var11];
                  if (skill.getId() == i) {
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     chr.dropMessage(1, "This skill has been blocked and may not be added.");
                     return;
                  }
               }

               if (remainingSp > 0 && curLevel + count <= maxlevel && skill.canBeLearnedBy(chr)) {
                  if (!isBeginnerSkill) {
                     skillbook = GameConstants.getSkillBookForSkill(skillid);
                     chr.setRemainingSp(chr.getRemainingSp(skillbook) - count, skillbook);
                  }

                  chr.updateSingleStat(MapleStat.AVAILABLESP, 0L);
                  chr.changeSingleSkillLevel(skill, (byte)(curLevel + count), chr.getMasterLevel(skill));
               } else {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               }

               return;
            }

            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            return;
         }

         Pair<String, Integer> ski = (Pair)var14.next();
         if (((String)ski.left).equals("level")) {
            if (chr.getLevel() < (Integer)ski.right) {
               break;
            }
         } else {
            if (chr.getSkillLevel(SkillFactory.getSkill(Integer.parseInt((String)ski.left))) >= (Integer)ski.right) {
               continue;
            }
            break;
         }
      }

   }

   public static final void AutoAssignAP(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      slea.skip(4);
      int count = slea.readInt();
      int PrimaryStat = slea.readInt();
      int amount = slea.readInt();
      int SecondaryStat2 = count == 2 ? slea.readInt() : 0;
      int amount2 = count == 2 ? slea.readInt() : 0;
      if (amount >= 0 && amount2 >= 0) {
         if (chr.getRemainingAp() >= amount + amount2 && chr.getRemainingAp() >= amount && chr.getRemainingAp() >= amount2) {
            PlayerStats playerst = chr.getStat();
            EnumMap<MapleStat, Long> statupdate = new EnumMap(MapleStat.class);
            c.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statupdate, true, chr));
            if (chr.getRemainingAp() == amount + amount2 || GameConstants.isXenon(chr.getJob())) {
               switch(PrimaryStat) {
               case 64:
                  if (playerst.getStr() + amount > 32767) {
                     return;
                  }

                  playerst.setStr((short)(playerst.getStr() + amount), chr);
                  statupdate.put(MapleStat.STR, (long)playerst.getStr());
                  break;
               case 128:
                  if (playerst.getDex() + amount > 32767) {
                     return;
                  }

                  playerst.setDex((short)(playerst.getDex() + amount), chr);
                  statupdate.put(MapleStat.DEX, (long)playerst.getDex());
                  break;
               case 256:
                  if (playerst.getInt() + amount > 32767) {
                     return;
                  }

                  playerst.setInt((short)(playerst.getInt() + amount), chr);
                  statupdate.put(MapleStat.INT, (long)playerst.getInt());
                  break;
               case 512:
                  if (playerst.getLuk() + amount > 32767) {
                     return;
                  }

                  playerst.setLuk((short)(playerst.getLuk() + amount), chr);
                  statupdate.put(MapleStat.LUK, (long)playerst.getLuk());
                  break;
               case 2048:
                  if (playerst.getMaxHp() + (long)(amount * 30) > 500000L) {
                     return;
                  }

                  playerst.setMaxHp(playerst.getMaxHp() + (long)(amount * 30), chr);
                  statupdate.put(MapleStat.MAXHP, playerst.getMaxHp());
                  break;
               default:
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               switch(SecondaryStat2) {
               case 64:
                  if (playerst.getStr() + amount2 > 32767) {
                     return;
                  }

                  playerst.setStr((short)(playerst.getStr() + amount2), chr);
                  statupdate.put(MapleStat.STR, (long)playerst.getStr());
                  break;
               case 128:
                  if (playerst.getDex() + amount2 > 32767) {
                     return;
                  }

                  playerst.setDex((short)(playerst.getDex() + amount2), chr);
                  statupdate.put(MapleStat.DEX, (long)playerst.getDex());
                  break;
               case 256:
                  if (playerst.getInt() + amount2 > 32767) {
                     return;
                  }

                  playerst.setInt((short)(playerst.getInt() + amount2), chr);
                  statupdate.put(MapleStat.INT, (long)playerst.getInt());
                  break;
               case 512:
                  if (playerst.getLuk() + amount2 > 32767) {
                     return;
                  }

                  playerst.setLuk((short)(playerst.getLuk() + amount2), chr);
                  statupdate.put(MapleStat.LUK, (long)playerst.getLuk());
                  break;
               case 2048:
                  if (playerst.getMaxHp() + (long)(amount * 30) > 500000L) {
                     return;
                  }

                  playerst.setMaxHp(playerst.getMaxHp() + (long)(amount * 30), chr);
                  statupdate.put(MapleStat.MAXHP, playerst.getMaxHp());
               }

               chr.setRemainingAp((short)(chr.getRemainingAp() - (amount + amount2)));
               statupdate.put(MapleStat.AVAILABLEAP, (long)chr.getRemainingAp());
               c.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statupdate, true, chr));
            }

         }
      }
   }
}
