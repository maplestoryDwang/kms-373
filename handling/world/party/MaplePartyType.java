package handling.world.party;

public enum MaplePartyType {
   LogState(26),
   Created(27),
   Disband(29),
   Join(31),
   LeaderUpdate(50),
   PartyTitle(61);

   private final int type;

   private MaplePartyType(int i) {
      this.type = i;
   }

   public final int getType() {
      return this.type;
   }
}
