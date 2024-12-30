package server.life;

public enum Element {
   NEUTRAL(0),
   PHYSICAL(1),
   FIRE(2, true),
   ICE(3, true),
   LIGHTING(4),
   POISON(5),
   HOLY(6, true),
   DARKNESS(7);

   private boolean special = false;
   private int value;

   private Element(int v) {
      this.value = v;
   }

   private Element(int v, boolean special) {
      this.value = v;
      this.special = special;
   }

   public boolean isSpecial() {
      return this.special;
   }

   public static Element getFromChar(char c) {
      switch(Character.toUpperCase(c)) {
      case 'D':
         return DARKNESS;
      case 'E':
      case 'G':
      case 'J':
      case 'K':
      case 'M':
      case 'N':
      case 'O':
      case 'Q':
      case 'R':
      default:
         throw new IllegalArgumentException("unknown elemnt char " + c);
      case 'F':
         return FIRE;
      case 'H':
         return HOLY;
      case 'I':
         return ICE;
      case 'L':
         return LIGHTING;
      case 'P':
         return PHYSICAL;
      case 'S':
         return POISON;
      }
   }

   public static Element getFromId(int c) {
      Element[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Element e = var1[var3];
         if (e.value == c) {
            return e;
         }
      }

      throw new IllegalArgumentException("unknown elemnt id " + c);
   }

   public int getValue() {
      return this.value;
   }
}
