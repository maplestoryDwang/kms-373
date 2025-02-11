package server.life;

import constants.GameConstants;

public class ChangeableStats extends OverrideMonsterStats {
   public int watk;
   public int matk;
   public int acc;
   public int eva;
   public int PDRate;
   public int MDRate;
   public int pushed;
   public int level;
   public int speed;

   public ChangeableStats(MapleMonsterStats stats, OverrideMonsterStats ostats) {
      this.hp = ostats.getHp();
      this.exp = ostats.getExp();
      this.mp = ostats.getMp();
      this.watk = stats.getPhysicalAttack();
      this.matk = stats.getMagicAttack();
      this.acc = stats.getAcc();
      this.eva = stats.getEva();
      this.PDRate = stats.getPDRate();
      this.MDRate = stats.getMDRate();
      this.pushed = stats.getPushed();
      this.level = stats.getLevel();
      this.speed = stats.getSpeed();
   }

   public ChangeableStats(MapleMonsterStats stats, int newLevel, boolean pqMob) {
      double mod = (double)(newLevel / stats.getLevel());
      double hpRatio = (double)(stats.getHp() / stats.getExp());
      double pqMod = pqMob ? 2.5D : 1.0D;
      this.hp = Math.round((!stats.isBoss() ? (double)GameConstants.getMonsterHP(newLevel) : (double)stats.getHp() * mod) * pqMod);
      this.exp = (long)((int)Math.round((!stats.isBoss() ? (double)GameConstants.getMonsterHP(newLevel) / hpRatio : (double)stats.getExp() * mod) * mod * pqMod));
      this.mp = (int)Math.round((double)stats.getMp() * mod * pqMod);
      this.watk = (int)Math.round((double)stats.getPhysicalAttack() * mod);
      this.matk = (int)Math.round((double)stats.getMagicAttack() * mod);
      this.acc = Math.round((float)(stats.getAcc() + Math.max(0, newLevel - stats.getLevel()) * 2));
      this.eva = Math.round((float)(stats.getEva() + Math.max(0, newLevel - stats.getLevel())));
      this.PDRate = Math.min(stats.isBoss() ? 30 : 20, (int)Math.round((double)stats.getPDRate() * mod));
      this.MDRate = Math.min(stats.isBoss() ? 30 : 20, (int)Math.round((double)stats.getMDRate() * mod));
      this.pushed = (int)Math.round((double)stats.getPushed() * mod);
      this.level = newLevel;
   }
}
