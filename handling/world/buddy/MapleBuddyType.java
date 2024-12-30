package handling.world.buddy;

public enum MapleBuddyType {
   addBuddy(27),
   Attendance(183);

   private final int type;

   private MapleBuddyType(int i) {
      this.type = i;
   }

   public final int getType() {
      return this.type;
   }
}
