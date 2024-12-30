package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleHyperStats;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import java.util.HashMap;
import java.util.Iterator;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class HyperHandler {
   public static int[] table = new int[]{0, 1, 2, 4, 8, 10, 15, 20, 25, 30, 35, 50, 65, 80, 95, 110};

   public static void getHyperSkill(LittleEndianAccessor rh, MapleClient c) {
      String value = rh.readMapleAsciiString();
      int rate = false;
      int lvl = rh.readInt();
      int sp = rh.readInt();
      if (!value.equals("needHyperStatLv") && !value.equals("incHyperStat")) {
         if (value.equals("hyper")) {
            int table = 0;
            boolean unk = false;
            if (lvl >= 28 && lvl <= 40) {
               unk = true;
               table = sp == 1 ? (lvl != 28 && lvl != 32 && lvl != 38 ? 0 : 1) : (lvl != 28 && lvl != 30 && lvl != 33 && lvl != 36 && lvl != 38 ? 0 : 1);
            }

            c.getSession().writeAndFlush(CWvsContext.updateHyperSp(value, lvl, sp, table, unk));
         } else {
            if (value.equals("honorLeveling")) {
               return;
            }

            if (!value.equals("hyper") && !value.equals("incHyperStat")) {
               int rate;
               if (!value.startsWith("9200") && !value.startsWith("9201") && !value.startsWith("9203") && !value.startsWith("9204")) {
                  if (value.equals("honorLeveling")) {
                     c.getSession().writeAndFlush(CWvsContext.updateSpecialStat(value, lvl, sp, c.getPlayer().getHonourNextExp()));
                     return;
                  }

                  rate = Math.max(0, 100 - (lvl + 1 - c.getPlayer().getProfessionLevel(Integer.parseInt(value))) * 20);
               } else {
                  rate = 100;
               }

               c.getSession().writeAndFlush(CWvsContext.updateSpecialStat(value, lvl, sp, rate));
            }
         }
      } else {
         if (value.equals("honorLeveling")) {
            return;
         }

         int up = 0;
         if (value.equals("incHyperStat")) {
            if (lvl >= 140 && lvl <= 149) {
               up = 3;
            } else if (lvl >= 150 && lvl <= 159) {
               up = 4;
            } else if (lvl >= 160 && lvl <= 169) {
               up = 5;
            } else if (lvl >= 170 && lvl <= 179) {
               up = 6;
            } else if (lvl >= 180 && lvl <= 189) {
               up = 7;
            } else if (lvl >= 190 && lvl <= 199) {
               up = 8;
            } else if (lvl >= 200 && lvl <= 209) {
               up = 9;
            } else if (lvl >= 210 && lvl <= 219) {
               up = 10;
            } else if (lvl >= 220 && lvl <= 229) {
               up = 11;
            } else if (lvl >= 230 && lvl <= 239) {
               up = 12;
            } else if (lvl >= 240 && lvl <= 249) {
               up = 13;
            } else if (lvl >= 250 && lvl <= 259) {
               up = 14;
            } else if (lvl >= 260 && lvl <= 269) {
               up = 15;
            } else if (lvl >= 270 && lvl <= 300) {
               up = 16;
            }
         }

         c.getSession().writeAndFlush(CWvsContext.updateHyperSp(value, lvl, sp, value.equals("incHyperStat") ? up : (value.equals("needHyperStatLv") ? HyperHandler.table[lvl] : 1), true));
      }

   }

   public static void HyperStatHandler(LittleEndianAccessor slea, MapleClient c) {
      int pos = slea.readInt();
      int skillid = slea.readInt();
      Skill skill = SkillFactory.getSkill(skillid);
      int maxlevel = skill.getMaxLevel();
      int curLevel = c.getPlayer().getSkillLevel(skill);
      boolean stat = skill.getId() / 100 == 800004;
      boolean create = c.getPlayer().getKeyValue(2424, pos + "/" + skillid) < 0L;
      int sum = 0;
      Iterator var10 = c.getPlayer().getSkills().keySet().iterator();

      while(true) {
         Skill skil;
         int a;
         do {
            if (!var10.hasNext()) {
               if (curLevel == 0) {
                  ++sum;
               }

               int up = false;
               int i = c.getPlayer().getLevel();
               a = i % 10;
               int var14;
               if (i >= 140 && i <= 149) {
                  var14 = 3 + 3 * a;
               } else if (i >= 150 && i <= 159) {
                  var14 = 44 + 4 * a;
               } else if (i >= 160 && i <= 169) {
                  var14 = 75 + 5 * a;
               } else if (i >= 170 && i <= 179) {
                  var14 = 126 + 6 * a;
               } else if (i >= 180 && i <= 189) {
                  var14 = 187 + 7 * a;
               } else if (i >= 190 && i <= 199) {
                  var14 = 258 + 8 * a;
               } else if (i >= 200 && i <= 209) {
                  var14 = 339 + 9 * a;
               } else if (i >= 210 && i <= 219) {
                  var14 = 430 + 10 * a;
               } else if (i >= 220 && i <= 229) {
                  var14 = 531 + 11 * a;
               } else if (i >= 230 && i <= 239) {
                  var14 = 642 + 12 * a;
               } else if (i >= 240 && i <= 249) {
                  var14 = 763 + 13 * a;
               } else if (i >= 250 && i <= 259) {
                  var14 = 894 + 14 * a;
               } else if (i >= 260 && i <= 269) {
                  var14 = 1035 + 15 * a;
               } else if (i >= 270 && i <= 300) {
                  var14 = 1186 + 16 * a;
               }

               if (curLevel + 1 <= maxlevel) {
                  c.getPlayer().changeSingleSkillLevel(skill, (byte)curLevel + 1, (byte)maxlevel);
                  if (create) {
                     c.getPlayer().addHyperStats(pos, skillid, curLevel + 1);
                  } else {
                     c.getPlayer().UpdateHyperStats(pos, skillid, curLevel + 1);
                  }

                  if (skill.getId() == 80000404 || skill.getId() == 80000405) {
                     c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
                  }

                  c.getSession().writeAndFlush(CWvsContext.updateHyperPresets(c.getPlayer(), pos, (byte)1));
                  c.getPlayer().setKeyValue(2424, pos + "/" + skillid, (curLevel + 1).makeConcatWithConstants<invokedynamic>(curLevel + 1));
               } else {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               }

               return;
            }

            skil = (Skill)var10.next();
         } while(skil.getId() / 100 != 800004);

         for(a = 1; a <= c.getPlayer().getSkillLevel(skil.getId()); ++a) {
            sum += table[a];
         }

         if (skil.getId() == skill.getId()) {
            sum += table[curLevel + 1];
         }
      }
   }

   public static void ResetHyperStatHandler(LittleEndianAccessor slea, MapleClient c) {
      int pos = slea.readInt();
      long price = 10000000L;
      MapleCharacter chr = c.getPlayer();
      if (chr.getMeso() < price) {
         chr.dropMessage(1, "메소가 부족합니다.");
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         HashMap<Skill, SkillEntry> sa = new HashMap();
         Iterator var7 = chr.getSkills().keySet().iterator();

         while(var7.hasNext()) {
            Skill skil = (Skill)var7.next();
            if (skil.getId() / 100 == 800004) {
               sa.put(skil, new SkillEntry(0, (byte)skil.getMaxLevel(), -1L));
            }
         }

         var7 = chr.loadHyperStats(pos).iterator();

         while(var7.hasNext()) {
            MapleHyperStats right = (MapleHyperStats)var7.next();
            chr.resetHyperStats(right.getPosition(), right.getSkillid());
         }

         chr.gainMeso(-price, false);
         chr.changeSkillsLevel(sa);
         c.getSession().writeAndFlush(CWvsContext.updateHyperPresets(c.getPlayer(), pos, (byte)1));
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static void HyperStatPresets(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      slea.skip(4);
      int pos = slea.readInt();
      HashMap<Skill, SkillEntry> hps = new HashMap();
      Iterator var5 = chr.loadHyperStats((int)chr.getKeyValue(2498, "hyperstats")).iterator();

      MapleHyperStats right;
      while(var5.hasNext()) {
         right = (MapleHyperStats)var5.next();
         hps.put(SkillFactory.getSkill(right.getSkillid()), new SkillEntry(0, (byte)SkillFactory.getSkill(right.getSkillid()).getMaxLevel(), -1L));
      }

      var5 = chr.loadHyperStats(pos).iterator();

      while(var5.hasNext()) {
         right = (MapleHyperStats)var5.next();
         hps.put(SkillFactory.getSkill(right.getSkillid()), new SkillEntry(right.getSkillLevel(), (byte)SkillFactory.getSkill(right.getSkillid()).getMaxLevel(), -1L));
      }

      chr.changeSkillsLevel(hps);
      c.getSession().writeAndFlush(CWvsContext.updateHyperPresets(c.getPlayer(), pos, (byte)0));
      chr.setKeyValue(2498, "hyperstats", pos.makeConcatWithConstants<invokedynamic>(pos));
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static void ResetHyperSkill(MapleClient c) {
      long price = 100000L;
      MapleCharacter chr = c.getPlayer();
      if (chr.getMeso() < price) {
         chr.dropMessage(1, "메소가 부족합니다.");
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         HashMap<Skill, SkillEntry> sa = new HashMap();
         Iterator var5 = chr.getSkills().keySet().iterator();

         while(var5.hasNext()) {
            Skill skil = (Skill)var5.next();
            if (skil.isHyper()) {
               sa.put(skil, new SkillEntry(0, (byte)skil.getMaxLevel(), -1L));
            }
         }

         chr.gainMeso(-price, false);
         chr.changeSkillsLevel(sa);
      }
   }
}
