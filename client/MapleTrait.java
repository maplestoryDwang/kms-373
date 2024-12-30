package client;

import constants.GameConstants;
import tools.packet.CWvsContext;

public class MapleTrait {
   private MapleTrait.MapleTraitType type;
   private int totalExp = 0;
   private int localTotalExp = 0;
   private short exp = 0;
   private byte level = 0;

   public MapleTrait(MapleTrait.MapleTraitType t) {
      this.type = t;
   }

   public void setExp(int e) {
      this.totalExp = e;
      this.localTotalExp = e;
      this.recalcLevel();
   }

   public void addExp(int e) {
      this.totalExp += e;
      this.localTotalExp += e;
      if (e != 0) {
         this.recalcLevel();
      }

   }

   public void addExp(int e, MapleCharacter c) {
      this.addTrueExp(e * c.getClient().getChannelServer().getTraitRate(), c);
   }

   public void addTrueExp(int e, MapleCharacter c) {
      if (e != 0) {
         this.totalExp += e;
         this.localTotalExp += e;
         c.updateSingleStat(this.type.stat, (long)this.totalExp);
         c.getClient().getSession().writeAndFlush(CWvsContext.InfoPacket.showTraitGain(this.type, e));
         this.recalcLevel();
      }

   }

   public boolean recalcLevel() {
      if (this.totalExp < 0) {
         this.totalExp = 0;
         this.localTotalExp = 0;
         this.level = 0;
         this.exp = 0;
         return false;
      } else {
         int oldLevel = this.level;

         for(byte i = 0; i < 100; ++i) {
            if (GameConstants.getTraitExpNeededForLevel(i) > this.localTotalExp) {
               this.exp = (short)(GameConstants.getTraitExpNeededForLevel(i) - this.localTotalExp);
               this.level = (byte)(i - 1);
               return this.level > oldLevel;
            }
         }

         this.exp = 0;
         this.level = 100;
         this.totalExp = GameConstants.getTraitExpNeededForLevel(this.level);
         this.localTotalExp = this.totalExp;
         return this.level > oldLevel;
      }
   }

   public void setExp(short exp) {
      this.exp = exp;
   }

   public void setLevel(byte level) {
      this.level = level;
   }

   public int getLevel() {
      return this.level;
   }

   public int getExp() {
      return this.exp;
   }

   public int getTotalExp() {
      return this.totalExp;
   }

   public int getLocalTotalExp() {
      return this.localTotalExp;
   }

   public void addLocalExp(int e) {
      this.localTotalExp += e;
   }

   public void clearLocalExp() {
      this.localTotalExp = this.totalExp;
   }

   public MapleTrait.MapleTraitType getType() {
      return this.type;
   }

   public static enum MapleTraitType {
      charisma(500, MapleStat.CHARISMA),
      insight(500, MapleStat.INSIGHT),
      will(500, MapleStat.WILL),
      craft(500, MapleStat.CRAFT),
      sense(500, MapleStat.SENSE),
      charm(5000, MapleStat.CHARM);

      final int limit;
      final MapleStat stat;

      private MapleTraitType(int type, MapleStat theStat) {
         this.limit = type;
         this.stat = theStat;
      }

      public int getLimit() {
         return this.limit;
      }

      public MapleStat getStat() {
         return this.stat;
      }

      public static MapleTrait.MapleTraitType getByQuestName(String q) {
         String qq = q.substring(0, q.length() - 3);
         MapleTrait.MapleTraitType[] var2 = values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            MapleTrait.MapleTraitType t = var2[var4];
            if (t.name().equals(qq)) {
               return t;
            }
         }

         return null;
      }
   }
}
