package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.world.World;
import handling.world.guild.MapleGuild;
import java.util.Iterator;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class AllianceHandler {
   public static final void HandleAlliance(LittleEndianAccessor slea, MapleClient c, boolean denied) {
      if (c.getPlayer().getGuildId() <= 0) {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         MapleGuild gs = World.Guild.getGuild(c.getPlayer().getGuildId());
         if (gs == null) {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         } else {
            byte op = slea.readByte();
            if (c.getPlayer().getGuildRank() == 1 || op == 1) {
               if (op == 22) {
                  denied = true;
               }

               int leaderid = 0;
               if (gs.getAllianceId() > 0) {
                  leaderid = World.Alliance.getAllianceLeader(gs.getAllianceId());
               }

               if (op != 4 && !denied) {
                  if (gs.getAllianceId() <= 0 || leaderid <= 0) {
                     return;
                  }
               } else if (leaderid > 0 || gs.getAllianceId() > 0) {
                  return;
               }

               if (denied) {
                  DenyInvite(c, gs);
               } else {
                  switch(op) {
                  case 1:
                     Iterator var13 = World.Alliance.getAllianceInfo(gs.getAllianceId(), false).iterator();

                     while(var13.hasNext()) {
                        byte[] pack = (byte[])var13.next();
                        if (pack != null) {
                           c.getSession().writeAndFlush(pack);
                        }
                     }

                     return;
                  case 2:
                  case 6:
                     int gid;
                     if (op == 6 && slea.available() >= 4L) {
                        gid = slea.readInt();
                        if (slea.available() >= 4L && gs.getAllianceId() != slea.readInt()) {
                           return;
                        }
                     } else {
                        gid = c.getPlayer().getGuildId();
                     }

                     if (c.getPlayer().getAllianceRank() <= 2 && (c.getPlayer().getAllianceRank() == 1 || c.getPlayer().getGuildId() == gid) && !World.Alliance.removeGuildFromAlliance(gs.getAllianceId(), gid, c.getPlayer().getGuildId() != gid)) {
                        c.getPlayer().dropMessage(5, "An error occured when removing guild.");
                     }

                     return;
                  case 3:
                     int newGuild = World.Guild.getGuildLeader(slea.readMapleAsciiString());
                     if (newGuild > 0 && c.getPlayer().getAllianceRank() == 1 && leaderid == c.getPlayer().getId()) {
                        MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(newGuild);
                        if (chr != null && chr.getGuildId() > 0 && World.Alliance.canInvite(gs.getAllianceId())) {
                           chr.getClient().getSession().writeAndFlush(CWvsContext.AlliancePacket.sendAllianceInvite(World.Alliance.getAlliance(gs.getAllianceId()).getName(), c.getPlayer()));
                           World.Guild.setInvitedId(chr.getGuildId(), gs.getAllianceId());
                        } else {
                           c.getPlayer().dropMessage(1, "Make sure the leader of the guild is online and in your channel.");
                        }
                     } else {
                        c.getPlayer().dropMessage(1, "That Guild was not found. Please enter the correct Guild Name. (Not the player name)");
                     }

                     return;
                  case 4:
                     int inviteid = World.Guild.getInvitedId(c.getPlayer().getGuildId());
                     if (inviteid > 0) {
                        if (!World.Alliance.addGuildToAlliance(inviteid, c.getPlayer().getGuildId())) {
                           c.getPlayer().dropMessage(5, "An error occured when adding guild.");
                        }

                        World.Guild.setInvitedId(c.getPlayer().getGuildId(), 0);
                     }

                     return;
                  case 5:
                  default:
                     System.out.println("Unhandled GuildAlliance op: " + op + ", \n" + slea.toString());
                     return;
                  case 7:
                     if (c.getPlayer().getAllianceRank() == 1 && leaderid == c.getPlayer().getId() && !World.Alliance.changeAllianceLeader(gs.getAllianceId(), slea.readInt())) {
                        c.getPlayer().dropMessage(5, "An error occured when changing leader.");
                     }

                     return;
                  case 8:
                     if (leaderid == c.getPlayer().getId()) {
                        String[] ranks = new String[5];

                        for(int i = 0; i < 5; ++i) {
                           ranks[i] = slea.readMapleAsciiString();
                        }

                        World.Alliance.updateAllianceRanks(gs.getAllianceId(), ranks);
                     }

                     return;
                  case 9:
                     if (c.getPlayer().getAllianceRank() <= 2 && !World.Alliance.changeAllianceRank(gs.getAllianceId(), slea.readInt(), slea.readByte())) {
                        c.getPlayer().dropMessage(5, "An error occured when changing rank.");
                     }

                     return;
                  case 10:
                     if (c.getPlayer().getAllianceRank() <= 2) {
                        String notice = slea.readMapleAsciiString();
                        if (notice.length() <= 100) {
                           World.Alliance.updateAllianceNotice(gs.getAllianceId(), notice);
                        }
                     }

                  }
               }
            }
         }
      }
   }

   public static final void DenyInvite(MapleClient c, MapleGuild gs) {
      int inviteid = World.Guild.getInvitedId(c.getPlayer().getGuildId());
      if (inviteid > 0) {
         int newAlliance = World.Alliance.getAllianceLeader(inviteid);
         if (newAlliance > 0) {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(newAlliance);
            if (chr != null) {
               chr.dropMessage(5, gs.getName() + " Guild has rejected the Guild Union invitation.");
            }

            World.Guild.setInvitedId(c.getPlayer().getGuildId(), 0);
         }
      }

   }
}
