package handling.world.guild;

public enum MapleGuildType {
   showInfo(53),
   lookInfo(55),
   InfoRes(55),
   getInfo(56),
   newInfo(61),
   newGuildMember(69),
   Request(78),
   delayRequest(83),
   memberLeft(88),
   Expelled(91),
   Disband(94),
   DenyReq(99),
   cancelRequest(100),
   InviteDeny(101),
   Invite(103),
   CapacityChange(111),
   memberUpdate(115),
   memberOnline(116),
   rankTitleChange(122),
   rankChange(128),
   Contribution(130),
   CustomEmblem(132),
   Notice(142),
   Setting(144),
   updatePoint(152),
   rankRequest(153),
   removeDisband(158),
   Skill(160),
   UseNoblessSkill(163),
   ChangeLeader(175),
   Attendance(183);

   private final int type;

   private MapleGuildType(int i) {
      this.type = i;
   }

   public final int getType() {
      return this.type;
   }
}
