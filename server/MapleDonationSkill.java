package server;

public enum MapleDonationSkill {
   HOLY_SYMBOL(1, 2311003),
   WIND_BOOSTER(2, 5121009),
   SHARP_EYES(4, 3121002),
   CROSS_OVER_CHAIN(8, 1311015),
   MAGIC_GUARD(16, 2001002),
   BUCKSHOT(32, 5321054),
   TRIPLING_WIM(64, 13100022),
   FINAL_CUT(128, 4341002);

   private final int i;
   private final int j;

   private MapleDonationSkill(int i, int j) {
      this.i = i;
      this.j = j;
   }

   public int getValue() {
      return this.i;
   }

   public int getSkillId() {
      return this.j;
   }

   public static final MapleDonationSkill getByValue(int value) {
      MapleDonationSkill[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         MapleDonationSkill stat = var1[var3];
         if (stat.i == value) {
            return stat;
         }
      }

      return null;
   }

   public static final MapleDonationSkill getBySkillId(int value) {
      MapleDonationSkill[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         MapleDonationSkill stat = var1[var3];
         if (stat.j == value) {
            return stat;
         }
      }

      return null;
   }
}
