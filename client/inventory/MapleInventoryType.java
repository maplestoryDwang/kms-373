package client.inventory;

public enum MapleInventoryType {
   UNDEFINED(0),
   EQUIP(1),
   USE(2),
   SETUP(3),
   ETC(4),
   CASH(5),
   CODY(6),
   EQUIPPED(-1);

   final byte type;

   private MapleInventoryType(int type) {
      this.type = (byte)type;
   }

   public byte getType() {
      return this.type;
   }

   public long getBitfieldEncoding() {
      return this.type <= 5 ? (long)(2 << this.type) : 2199023255552L << this.type;
   }

   public static MapleInventoryType getByType(byte type) {
      MapleInventoryType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         MapleInventoryType l = var1[var3];
         if (l.getType() == type) {
            return l;
         }
      }

      return null;
   }
}
