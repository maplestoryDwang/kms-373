package server.quest;

public enum MapleQuestActionType {
   UNDEFINED(-1),
   exp(0),
   item(1),
   nextQuest(2),
   money(3),
   quest(4),
   skill(5),
   pop(6),
   buffItemID(7),
   infoNumber(8),
   sp(9),
   charismaEXP(10),
   charmEXP(11),
   willEXP(12),
   insightEXP(13),
   senseEXP(14),
   craftEXP(15);

   final byte type;

   private MapleQuestActionType(int type) {
      this.type = (byte)type;
   }

   public byte getType() {
      return this.type;
   }

   public static MapleQuestActionType getByType(byte type) {
      MapleQuestActionType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         MapleQuestActionType l = var1[var3];
         if (l.getType() == type) {
            return l;
         }
      }

      return null;
   }

   public static MapleQuestActionType getByWZName(String name) {
      try {
         return valueOf(name);
      } catch (IllegalArgumentException var2) {
         return UNDEFINED;
      }
   }
}
