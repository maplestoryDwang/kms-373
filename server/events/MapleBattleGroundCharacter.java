package server.events;

import client.MapleCharacter;
import client.MapleStat;
import client.inventory.Item;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import server.Randomizer;
import server.Timer;
import server.games.BattleGroundGameHandler;
import tools.Triple;
import tools.packet.BattleGroundPacket;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.SLFCGPacket;

public class MapleBattleGroundCharacter implements Comparable<MapleBattleGroundCharacter> {
   private MapleCharacter chr;
   public static List<MapleBattleGroundCharacter> bchr = new ArrayList();
   private String name;
   private boolean alive = true;
   private int Jobtype;
   private int id;
   private int attackcount = 0;
   private int attackup;
   private int hpmpup;
   private int criticalup;
   private int speedup;
   private int regenup;
   private int money;
   private int skillmindamup = 0;
   private int skillmaxdamup = 0;
   private int skillhpup = 0;
   private int skillmpup = 0;
   private int level;
   private int speed;
   private int jump;
   private int maxhp;
   private int hp;
   private int maxmp;
   private int mp;
   private int hpregen;
   private int mpregen;
   private int mindam;
   private int maxdam;
   private int critical;
   private int exp;
   private int team;
   private int kill;
   private int death;
   private int attackspeed;
   private int deathcount = 0;
   private List<Triple<Integer, Integer, Integer>> skilllist = new ArrayList();

   public MapleBattleGroundCharacter(MapleCharacter chr, String job) {
      this.chr = chr;
      this.name = chr.getName();
      this.id = chr.getId();
      this.level = 1;
      byte var4 = -1;
      switch(job.hashCode()) {
      case -668539270:
         if (job.equals("폴로&프리토")) {
            var4 = 9;
         }
         break;
      case 1507843:
         if (job.equals("랑이")) {
            var4 = 6;
         }
         break;
      case 1517188:
         if (job.equals("류드")) {
            var4 = 7;
         }
         break;
      case 1526132:
         if (job.equals("만지")) {
            var4 = 0;
         }
         break;
      case 1529921:
         if (job.equals("무공")) {
            var4 = 4;
         }
         break;
      case 1631715:
         if (job.equals("웡키")) {
            var4 = 8;
         }
         break;
      case 47341376:
         if (job.equals("마이크")) {
            var4 = 1;
         }
         break;
      case 54120552:
         if (job.equals("하인즈")) {
            var4 = 3;
         }
         break;
      case 54133884:
         if (job.equals("헬레나")) {
            var4 = 5;
         }
         break;
      case 1417093128:
         if (job.equals("다크로드")) {
            var4 = 2;
         }
      }

      switch(var4) {
      case 0:
         this.Jobtype = 1;
         this.mindam = 180;
         this.maxdam = 200;
         this.hp = 3000;
         this.mp = 1000;
         this.critical = 0;
         this.attackspeed = 400;
         this.hpregen = 15;
         this.mpregen = 10;
         this.speed = 120;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(0, 80001647, 1));
         this.skilllist.add(new Triple(0, 80001648, 0));
         this.skilllist.add(new Triple(0, 80001649, 0));
         this.skilllist.add(new Triple(0, 80001650, 0));
         this.skilllist.add(new Triple(0, 80001651, 0));
         this.skilllist.add(new Triple(0, 80001678, 1));
         break;
      case 1:
         this.Jobtype = 2;
         this.mindam = 126;
         this.maxdam = 140;
         this.attackspeed = 400;
         this.hp = 3000;
         this.mp = 1000;
         this.critical = 0;
         this.hpregen = 15;
         this.mpregen = 10;
         this.speed = 110;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(0, 80001652, 1));
         this.skilllist.add(new Triple(0, 80001653, 0));
         this.skilllist.add(new Triple(0, 80001654, 0));
         this.skilllist.add(new Triple(0, 80001655, 0));
         this.skilllist.add(new Triple(0, 80001656, 0));
         this.skilllist.add(new Triple(0, 80001676, 1));
         this.skilllist.add(new Triple(0, 80001677, 1));
         this.skilllist.add(new Triple(0, 80001678, 1));
         break;
      case 2:
         this.Jobtype = 3;
         this.mindam = 216;
         this.maxdam = 240;
         this.attackspeed = 330;
         this.hp = 2400;
         this.mp = 1000;
         this.critical = 0;
         this.hpregen = 12;
         this.mpregen = 10;
         this.speed = 120;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(1, 80000330, 0));
         this.skilllist.add(new Triple(0, 80001657, 1));
         this.skilllist.add(new Triple(0, 80001658, 0));
         this.skilllist.add(new Triple(0, 80001659, 0));
         this.skilllist.add(new Triple(0, 80001660, 0));
         this.skilllist.add(new Triple(0, 80001675, 0));
         this.skilllist.add(new Triple(0, 80001678, 1));
         break;
      case 3:
         this.Jobtype = 4;
         this.mindam = 234;
         this.maxdam = 260;
         this.hp = 2100;
         this.mp = 1000;
         this.attackspeed = 400;
         this.critical = 0;
         this.hpregen = 10;
         this.mpregen = 10;
         this.speed = 120;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(0, 80001661, 1));
         this.skilllist.add(new Triple(0, 80001662, 0));
         this.skilllist.add(new Triple(0, 80001663, 0));
         this.skilllist.add(new Triple(0, 80001664, 0));
         this.skilllist.add(new Triple(0, 80001665, 0));
         this.skilllist.add(new Triple(0, 80001678, 1));
         break;
      case 4:
         this.Jobtype = 5;
         this.mindam = 189;
         this.maxdam = 210;
         this.hp = 3150;
         this.mp = 1000;
         this.attackspeed = 400;
         this.critical = 0;
         this.hpregen = 15;
         this.mpregen = 10;
         this.speed = 120;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(0, 80001666, 1));
         this.skilllist.add(new Triple(0, 80001667, 0));
         this.skilllist.add(new Triple(0, 80001668, 0));
         this.skilllist.add(new Triple(0, 80001669, 0));
         this.skilllist.add(new Triple(0, 80001670, 0));
         this.skilllist.add(new Triple(0, 80001678, 1));
         this.skilllist.add(new Triple(0, 80001679, 1));
         break;
      case 5:
         this.Jobtype = 6;
         this.mindam = 252;
         this.maxdam = 280;
         this.hp = 1800;
         this.mp = 1000;
         this.attackspeed = 400;
         this.critical = 0;
         this.hpregen = 18;
         this.mpregen = 10;
         this.speed = 120;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(0, 80001678, 1));
         this.skilllist.add(new Triple(0, 80001732, 1));
         this.skilllist.add(new Triple(0, 80001733, 0));
         this.skilllist.add(new Triple(0, 80001734, 0));
         this.skilllist.add(new Triple(0, 80001735, 0));
         this.skilllist.add(new Triple(0, 80001736, 0));
         break;
      case 6:
         this.Jobtype = 7;
         this.mindam = 207;
         this.maxdam = 230;
         this.hp = 2850;
         this.mp = 1000;
         this.attackspeed = 400;
         this.critical = 0;
         this.hpregen = 14;
         this.mpregen = 10;
         this.speed = 120;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(0, 80001678, 1));
         this.skilllist.add(new Triple(0, 80001737, 1));
         this.skilllist.add(new Triple(0, 80001738, 0));
         this.skilllist.add(new Triple(0, 80001739, 0));
         this.skilllist.add(new Triple(0, 80001740, 0));
         this.skilllist.add(new Triple(0, 80001741, 1));
         break;
      case 7:
         this.Jobtype = 9;
         this.mindam = 150;
         this.maxdam = 170;
         this.hp = 3450;
         this.mp = 1000;
         this.attackspeed = 400;
         this.critical = 0;
         this.hpregen = 17;
         this.mpregen = 10;
         this.speed = 120;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(0, 80001678, 1));
         this.skilllist.add(new Triple(0, 80002338, 1));
         this.skilllist.add(new Triple(0, 80002339, 0));
         this.skilllist.add(new Triple(0, 80002341, 0));
         this.skilllist.add(new Triple(0, 80002342, 0));
         this.skilllist.add(new Triple(0, 80002344, 0));
         break;
      case 8:
         this.Jobtype = 10;
         this.mindam = 207;
         this.maxdam = 230;
         this.hp = 2550;
         this.mp = 1000;
         this.attackspeed = 400;
         this.critical = 0;
         this.hpregen = 12;
         this.mpregen = 10;
         this.speed = 120;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(0, 80001678, 1));
         this.skilllist.add(new Triple(0, 80002670, 1));
         this.skilllist.add(new Triple(0, 80002671, 0));
         this.skilllist.add(new Triple(0, 80002672, 0));
         this.skilllist.add(new Triple(0, 80002673, 0));
         this.skilllist.add(new Triple(0, 80002674, 0));
         break;
      case 9:
         this.Jobtype = 11;
         this.mindam = 225;
         this.maxdam = 250;
         this.hp = 2400;
         this.mp = 1000;
         this.critical = 0;
         this.attackspeed = 400;
         this.hpregen = 15;
         this.mpregen = 10;
         this.speed = 120;
         this.jump = 120;
         this.skilllist.add(new Triple(2, 1, 1));
         this.skilllist.add(new Triple(0, 80001678, 1));
         this.skilllist.add(new Triple(0, 80003001, 1));
         this.skilllist.add(new Triple(0, 80003002, 0));
         this.skilllist.add(new Triple(0, 80003003, 0));
         this.skilllist.add(new Triple(0, 80003004, 0));
         this.skilllist.add(new Triple(0, 80003005, 0));
         this.skilllist.add(new Triple(0, 80003006, 0));
         this.skilllist.add(new Triple(0, 80003007, 0));
         this.skilllist.add(new Triple(0, 80003008, 0));
         this.skilllist.add(new Triple(0, 80003009, 0));
         this.skilllist.add(new Triple(0, 80003010, 0));
         this.skilllist.add(new Triple(0, 80003011, 0));
         this.skilllist.add(new Triple(0, 80003012, 0));
         this.skilllist.add(new Triple(0, 80003013, 1));
      }

      this.maxhp = this.hp;
      this.maxmp = this.mp;
      this.attackup = 1;
      this.hpmpup = 1;
      this.criticalup = 1;
      this.speedup = 1;
      this.regenup = 1;
      this.team = 1;
      this.money = 0;
      bchr.add(this);
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getJobType() {
      return this.Jobtype;
   }

   public void setJobType(int Jobtype) {
      this.Jobtype = Jobtype;
   }

   public int getAttackCount() {
      return this.attackcount;
   }

   public void setAttackcount(int attackcount) {
      this.attackcount = attackcount;
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public int getSpeed() {
      return this.speed;
   }

   public void setSpeed(int speed) {
      this.speed = speed;
   }

   public int getJump() {
      return this.jump;
   }

   public void setJump(int jump) {
      this.jump = jump;
   }

   public int getHp() {
      return this.hp;
   }

   public void setHp(int hp) {
      this.hp = hp;
      if (this.hp >= this.getMaxHp()) {
         this.hp = this.getMaxHp();
      }

      if (this.chr.getBattleGroundChr().getHp() <= 0) {
         int respawntime = 0;
         switch(this.level) {
         case 1:
         case 2:
            respawntime = 1;
            break;
         case 3:
         case 4:
            respawntime = 2;
            break;
         case 5:
            respawntime = 3;
            break;
         case 6:
            respawntime = 4;
            break;
         case 7:
            respawntime = 5;
            break;
         case 8:
            respawntime = 6;
            break;
         case 9:
            respawntime = 7;
            break;
         case 10:
            respawntime = 8;
            break;
         case 11:
            respawntime = 9;
            break;
         case 12:
            respawntime = 10;
            break;
         case 13:
            respawntime = 11;
            break;
         case 14:
            respawntime = 12;
            break;
         case 15:
            respawntime = 20;
         }

         this.hp = 0;
         if (this.chr.getMap().getBattleGroundMainTimer() > respawntime) {
            this.chr.getClient().send(BattleGroundPacket.Respawn(this.getId(), 1));
            this.chr.getClient().send(BattleGroundPacket.Death(respawntime));
         }

         this.chr.getMap().broadcastMessage(BattleGroundPacket.DeathEffect(this));
         if (BattleGroundGameHandler.isEndOfGame()) {
            return;
         }

         Timer.MapTimer.getInstance().schedule(() -> {
            if (!BattleGroundGameHandler.isEndOfGame()) {
               this.Heal();
               int now = Randomizer.rand(1, 12);
               this.chr.getClient().send(CField.instantMapWarp(this.chr, (byte)now));
               this.setAlive(true);
            }
         }, (long)(respawntime * 1000 + 1000));
      }

   }

   public int getMaxHp() {
      return this.maxhp + this.skillhpup;
   }

   public void setMaxHp(int maxhp) {
      this.maxhp = maxhp;
   }

   public int getMp() {
      return this.mp;
   }

   public void setMp(int mp) {
      this.mp = mp;
      if (this.mp >= this.getMaxMp()) {
         this.mp = this.getMaxMp();
      }

      if (this.chr.getBattleGroundChr().getMp() <= 0) {
         this.mp = 0;
      }

   }

   public int getMaxMp() {
      return this.maxmp + this.skillmpup;
   }

   public void setMaxMp(int maxmp) {
      this.maxmp = maxmp;
   }

   public int getHpRegen() {
      return this.hpregen;
   }

   public void setHpRegen(int hpregen) {
      this.hpregen = hpregen;
   }

   public int getMpRegen() {
      return this.mpregen;
   }

   public void setMpRegen(int mpregen) {
      this.mpregen = mpregen;
   }

   public int getMindam() {
      return this.mindam + this.skillmindamup;
   }

   public void setMindam(int mindam) {
      this.mindam = mindam;
   }

   public int getMaxdam() {
      return this.maxdam + this.skillmaxdamup;
   }

   public void setMaxdam(int maxdam) {
      this.maxdam = maxdam;
   }

   public int getCritical() {
      return this.critical;
   }

   public void setCritical(int critical) {
      this.critical = critical;
   }

   public int getAttackSpeed() {
      return this.attackspeed;
   }

   public void setAttackSpeed(int attackspeed) {
      this.attackspeed = attackspeed;
   }

   public int getAttackUp() {
      return this.attackup;
   }

   public void setAttackUp(int attackup) {
      this.attackup = attackup;
   }

   public int getHpMpUp() {
      return this.hpmpup;
   }

   public void setHpMpUp(int hpmpup) {
      this.hpmpup = hpmpup;
   }

   public int getCriUp() {
      return this.criticalup;
   }

   public void setCriUp(int criticalup) {
      this.criticalup = criticalup;
   }

   public int getSpeedUp() {
      return this.speedup;
   }

   public void setSpeedUp(int speedup) {
      this.speedup = speedup;
   }

   public int getRegenUp() {
      return this.regenup;
   }

   public void setRegenUp(int regenup) {
      this.regenup = regenup;
   }

   public int getSkillMinDamUp() {
      return this.skillmindamup;
   }

   public void setSkillMinDamUp(int skillmindamup) {
      this.skillmindamup = skillmindamup;
   }

   public int getSkillMaxDamUp() {
      return this.skillmaxdamup;
   }

   public void setSkillMaxDamUp(int skillmaxdamup) {
      this.skillmaxdamup = skillmaxdamup;
   }

   public int getSkillHPUP() {
      return this.skillhpup;
   }

   public void setSkillHPUP(int skillhpup) {
      this.skillhpup = skillhpup;
   }

   public int getSkillMPUP() {
      return this.skillmpup;
   }

   public void setSkillMPUP(int skillmpup) {
      this.skillmpup = skillmpup;
   }

   public int getMoney() {
      return this.money;
   }

   public void setMoney(int money) {
      this.money = money;
   }

   public int getKill() {
      return this.kill;
   }

   public void setKill(int kill) {
      this.kill = kill;
   }

   public int getDeath() {
      return this.death;
   }

   public void setDeath(int death) {
      this.death = death;
   }

   public int getExp() {
      return this.exp;
   }

   public void setExp(int exp) {
      this.exp = exp;
      int max = 0;
      switch(this.level) {
      case 1:
         max = 51;
         break;
      case 2:
         max = 76;
         break;
      case 3:
         max = 107;
         break;
      case 4:
         max = 134;
         break;
      case 5:
         max = 188;
         break;
      case 6:
         max = 245;
         break;
      case 7:
         max = 318;
         break;
      case 8:
         max = 414;
         break;
      case 9:
         max = 496;
         break;
      case 10:
         max = 596;
         break;
      case 11:
         max = 715;
         break;
      case 12:
         max = 787;
         break;
      case 13:
         max = 866;
         break;
      case 14:
         max = 952;
         break;
      case 15:
         max = 9999;
      }

      if (this.level >= 15) {
         max = 9999;
      }

      if (this.level >= 15) {
         this.exp = 0;
         exp = 0;
      }

      if (exp >= max) {
         this.levelup();
      }

   }

   public void levelup() {
      this.exp = 0;
      int beforelevel = this.level - 1;
      if (beforelevel < 0) {
         beforelevel = 0;
      }

      this.setLevel(this.level + 1);
      switch(this.Jobtype) {
      case 1:
         this.mindam += 6 + beforelevel * 2;
         this.maxdam += 6 + beforelevel * 2;
         this.maxhp += 300;
         this.hpregen += 3;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001648, 1, 0, 0, 0, 5000));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001649, 1, 0, 0, 0, 7000));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001650, 1, 0, 0, 0, 8000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001651, 1, 0, 0, 0, 70000));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001651, 2, 0, 0, 0, 70000));
         }
         break;
      case 2:
         this.mindam += 4 + beforelevel * 2;
         this.maxdam += 4 + beforelevel * 2;
         this.maxhp += 300;
         this.hpregen += 2;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001653, 1, 0, 0, 0, 6000));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001654, 1, 0, 0, 0, 9000));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001655, 1, 5800, 80001676, 0, 20000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001656, 1, 11800, 80001677, 0, 60000));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001656, 2, 11800, 80001677, 0, 60000));
         }
         break;
      case 3:
         this.mindam += 5 + beforelevel * 2;
         this.maxdam += 5 + beforelevel * 2;
         this.maxhp += 240;
         this.hpregen += 2;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80000330, 1, 0, 0, 0, 0));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80000330, 2, 0, 0, 0, 0));
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001658, 1, 0, 0, 0, 10000));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80000330, 3, 0, 0, 0, 0));
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001659, 1, 0, 0, 0, 12000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001660, 1, 0, 0, 0, 60000));
         } else if (this.level == 9) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80000330, 4, 0, 0, 0, 0));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80000330, 5, 0, 0, 0, 0));
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001660, 2, 0, 0, 0, 60000));
         }
         break;
      case 4:
         this.mindam += 5 + beforelevel * 2;
         this.maxdam += 5 + beforelevel * 2;
         this.maxhp += 210;
         this.hpregen += 2;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001662, 1, 0, 0, 0, 6000));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001663, 1, 0, 0, 0, 12000));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001664, 1, 0, 0, 0, 10000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001665, 1, 0, 0, 0, 60000));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001665, 2, 0, 0, 0, 60000));
         }
         break;
      case 5:
         this.mindam += 4 + beforelevel * 2;
         this.maxdam += 4 + beforelevel * 2;
         this.maxhp += 315;
         this.hpregen += 3;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001667, 1, 0, 0, 0, 5000));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001668, 1, 0, 0, 0, 10000));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001669, 1, 1000, 0, 80001679, 7000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001670, 1, 0, 0, 0, 50000));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001670, 2, 0, 0, 0, 50000));
         }
         break;
      case 6:
         this.mindam += 6 + beforelevel * 3;
         this.maxdam += 6 + beforelevel * 3;
         this.maxhp += 180;
         this.hpregen += 2;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001733, 1, 0, 0, 0, 10000));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001734, 1, 0, 0, 0, 15000));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001735, 1, 0, 0, 0, 15000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001736, 1, 0, 0, 0, 45000));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001736, 2, 0, 0, 0, 45000));
         }
         break;
      case 7:
         this.mindam += 4 + beforelevel * 2;
         this.maxdam += 4 + beforelevel * 2;
         this.maxhp += 285;
         this.hpregen += 3;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001738, 1, 0, 0, 0, 3000));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001739, 1, 0, 0, 0, 10000, 3, 3));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001740, 1, 0, 0, 0, 17000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001741, 2, 0, 0, 0, 0));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80001741, 3, 0, 0, 0, 0));
         }
      case 8:
      default:
         break;
      case 9:
         this.mindam += 3 + beforelevel * 2;
         this.maxdam += 3 + beforelevel * 2;
         this.maxhp += 350;
         this.hpregen += 3;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002339, 1, 0, 0, 0, 7000));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002341, 1, 0, 0, 0, 20000));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002342, 1, 0, 0, 0, 20000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002344, 1, 0, 0, 0, 70000));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002344, 2, 0, 0, 0, 70000));
         }
         break;
      case 10:
         this.mindam += 5 + beforelevel * 2;
         this.maxdam += 5 + beforelevel * 2;
         this.maxhp += 255;
         this.hpregen += 2;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002671, 1, 0, 0, 0, 15000));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002673, 1, 0, 0, 0, 12000));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002672, 1, 0, 0, 0, 20000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002674, 1, 0, 0, 0, 50000));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80002674, 2, 0, 0, 0, 50000));
         }
         break;
      case 11:
      case 12:
         this.mindam += 5 + beforelevel * 2;
         this.maxdam += 5 + beforelevel * 2;
         this.maxhp += 250;
         this.hpregen += 2;
         if (this.level == 3) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003002, 1, 0, 0, 0, 4000));
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003007, 1, 0, 0, 0, 4000));
         } else if (this.level == 5) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003003, 1, 0, 0, 0, 5000));
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003008, 1, 0, 0, 0, 15000));
         } else if (this.level == 7) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003004, 1, 0, 0, 0, 15000));
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003009, 1, 0, 0, 0, 20000));
         } else if (this.level == 8) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003005, 1, 0, 0, 0, 50000));
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003012, 1, 0, 0, 0, 50000));
         } else if (this.level == 11) {
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003005, 2, 0, 0, 0, 50000));
            this.chr.getClient().send(BattleGroundPacket.SkillOn(this, 80003012, 2, 0, 0, 0, 50000));
         }
      }

      if (this.level == 6) {
         this.chr.getClient().send(CField.ImageTalkNpc(9001153, 3000, "아바타 상태 분석 완료. 다음 사냥터로 이동하는 것을 추천드립니다."));
      } else if (this.level == 8 && this.Jobtype != 7) {
         this.chr.getClient().send(CField.ImageTalkNpc(9001153, 3000, "아바타 상태 분석 완료. 궁극기를 사용할 수 있게 되었습니다."));
      } else if (this.level == 15) {
         this.chr.getClient().send(CField.ImageTalkNpc(9001153, 3000, "시스템 성장 임계점에 도달하였습니다. 더욱 적극적으로 상대방을 격파 하십시오."));
      }

      this.maxmp += 100;
      ++this.mpregen;
      this.skillmindamup = 0;
      this.skillmaxdamup = 0;
      this.chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(this.chr, 0, 0, 29, 0, 0, (byte)(this.chr.isFacingLeft() ? 1 : 0), true, this.chr.getTruePosition(), "Effect/BasicEff.img/PvpLevelUp", (Item)null));
      this.chr.getMap().broadcastMessage(this.chr, CField.EffectPacket.showEffect(this.chr, 0, 0, 29, 0, 0, (byte)(this.chr.isFacingLeft() ? 1 : 0), false, this.chr.getTruePosition(), "Effect/BasicEff.img/PvpLevelUp", (Item)null), false);
      this.chr.getMap().broadcastMessage(SLFCGPacket.playSE("Sound/Game.img/PvpLevelUp"));
      int attackup = this.getAttackUp() == 1 ? 2 : (this.getAttackUp() == 2 ? 5 : (this.getAttackUp() == 3 ? 10 : (this.getAttackUp() == 4 ? 15 : (this.getAttackUp() == 5 ? 20 : 0))));
      this.setSkillMinDamUp(Math.round((float)(this.getSkillMinDamUp() + this.getMindam() / 100 * attackup)));
      this.setSkillMaxDamUp(Math.round((float)(this.getSkillMaxDamUp() + this.getMaxdam() / 100 * attackup)));
      int hpmpup = this.getHpMpUp() == 1 ? 2 : (this.getHpMpUp() == 2 ? 7 : (this.getHpMpUp() == 3 ? 15 : (this.getHpMpUp() == 4 ? 20 : (this.getHpMpUp() == 5 ? 30 : 0))));
      this.setSkillHPUP(this.getMaxHp() / 100 * hpmpup);
      this.setSkillMPUP(this.getMaxMp() / 100 * hpmpup);
   }

   public int getTeam() {
      return this.team;
   }

   public void setTeam(int team) {
      this.team = team;
   }

   public boolean isAlive() {
      return this.alive;
   }

   public void setAlive(boolean alive) {
      this.alive = alive;
   }

   public List<Triple<Integer, Integer, Integer>> getSkillList() {
      return this.skilllist;
   }

   public MapleCharacter getChr() {
      return this.chr;
   }

   public void Heal() {
      this.hp = this.getMaxHp();
      this.mp = this.getMaxMp();
      this.getChr().dispel();
      Map<MapleStat, Long> statup = new EnumMap(MapleStat.class);
      statup.put(MapleStat.MAXHP, (long)this.getMaxHp());
      statup.put(MapleStat.MAXMP, (long)this.getMaxMp());
      statup.put(MapleStat.HP, (long)this.hp);
      statup.put(MapleStat.MP, (long)this.mp);
      this.chr.getClient().send(CWvsContext.updatePlayerStats(statup, false, this.chr));
      this.chr.getMap().broadcastMessage(CField.updatePartyMemberHP(this.chr.getId(), this.getHp(), this.getMaxHp()));
   }

   public int getDeathcount() {
      return this.deathcount;
   }

   public void setDeathcount(int deathcount) {
      this.deathcount = deathcount;
   }

   public int compareTo(MapleBattleGroundCharacter other) {
      return this.kill <= other.kill ? 1 : -1;
   }
}
