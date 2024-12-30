package server.events;

public enum MapleEventType {
   Coconut(new int[]{109080000}),
   CokePlay(new int[]{109080010}),
   Fitness(new int[]{109040000, 109040001, 109040002, 109040003, 109040004}),
   OlaOla(new int[]{109030001, 109030002, 109030003}),
   OxQuiz(new int[]{109020001}),
   Survival(new int[]{809040000, 809040100}),
   Snowball(new int[]{109060000});

   public int[] mapids;

   private MapleEventType(int[] mapids) {
      this.mapids = mapids;
   }

   public static final MapleEventType getByString(String splitted) {
      MapleEventType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         MapleEventType t = var1[var3];
         if (t.name().equalsIgnoreCase(splitted)) {
            return t;
         }
      }

      return null;
   }
}
