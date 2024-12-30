package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import constants.GameConstants;
import constants.KoreaCalendar;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.guild.MapleGuildResponse;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.SecondaryStatEffect;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class GuildHandler {
   private static List<GuildHandler.Invited> invited = new LinkedList();
   private static Map<Integer, Long> request = new LinkedHashMap();
   private static long nextPruneTime = System.currentTimeMillis() + 300000L;

   public static final void DenyGuildRequest(String from, MapleClient c) {
      MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterByName(from);
      if (cfrom != null) {
         cfrom.getClient().getSession().writeAndFlush(CWvsContext.GuildPacket.denyGuildInvitation(c.getPlayer().getName()));
      }

   }

   private static boolean isGuildNameAcceptable(String name) throws UnsupportedEncodingException {
      return name.getBytes("EUC-KR").length >= 2 && name.getBytes("EUC-KR").length <= 12;
   }

   private static void respawnPlayer(MapleCharacter mc) {
      if (mc.getMap() != null) {
         mc.getMap().broadcastMessage(CField.loadGuildIcon(mc));
      }
   }

   public static final void GuildCancelRequest(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      MapleGuild guild = World.Guild.getGuild(slea.readInt());
      if (c != null && chr != null && guild != null) {
         chr.setKeyValue(26015, "name", "");
         chr.setKeyValue(26015, "time", System.currentTimeMillis().makeConcatWithConstants<invokedynamic>(System.currentTimeMillis()));
         guild.removeRequest(chr.getId());
         c.getSession().writeAndFlush(CWvsContext.GuildPacket.RequestDeny(chr, guild));
         List<MapleGuild> g = new ArrayList();
         Iterator var5 = World.Guild.getGuilds().iterator();

         while(var5.hasNext()) {
            MapleGuild guilds = (MapleGuild)var5.next();
            if (guilds.getRequest(c.getPlayer().getId()) != null) {
               g.add(guilds);
            }
         }

         c.getSession().writeAndFlush(CWvsContext.GuildPacket.RequestListGuild(g));
         c.getSession().writeAndFlush(CWvsContext.GuildPacket.RecruitmentGuild(c.getPlayer()));
      }
   }

   public static final void GuildJoinRequest(LittleEndianAccessor slea, MapleCharacter chr) {
      int gid = slea.readInt();
      String requestss = slea.readMapleAsciiString();
      slea.skip(10);
      if (chr != null && gid > 0) {
         if (chr.getKeyValue(26015, "time") + 60000L >= System.currentTimeMillis()) {
            chr.getClient().getSession().writeAndFlush(CWvsContext.GuildPacket.DelayRequest());
         } else {
            MapleGuild g = World.Guild.getGuild(gid);
            MapleGuildCharacter mgc2 = new MapleGuildCharacter(chr);
            mgc2.setGuildId(gid);
            mgc2.setRequest(requestss);
            if (request.get(chr.getId()) == null) {
               if (g.addRequest(mgc2)) {
                  g.broadcast(CWvsContext.GuildPacket.addRegisterRequest(mgc2));
                  chr.dropMessage(5, "[" + g.getName() + "] 길드 가입 요청이 성공 하였습니다.");
               }

               chr.setKeyValue(26015, "name", g.getName());
               chr.setKeyValue(26015, "time", System.currentTimeMillis().makeConcatWithConstants<invokedynamic>(System.currentTimeMillis()));
            } else {
               request.remove(chr.getId());
               if (g.addRequest(mgc2)) {
                  chr.dropMessage(5, "[" + g.getName() + "] 길드 가입 요청이 성공 하였습니다.");
                  g.broadcast(CWvsContext.GuildPacket.addRegisterRequest(mgc2));
               }

               chr.setKeyValue(26015, "name", "");
               chr.setKeyValue(26015, "time", System.currentTimeMillis().makeConcatWithConstants<invokedynamic>(System.currentTimeMillis()));
            }

            List<MapleGuild> gs = new ArrayList();
            Iterator var7 = World.Guild.getGuilds().iterator();

            while(var7.hasNext()) {
               MapleGuild guilds = (MapleGuild)var7.next();
               if (guilds.getRequest(chr.getId()) != null) {
                  gs.add(guilds);
               }
            }

            chr.getClient().send(CWvsContext.GuildPacket.RequestListGuild(gs));
            chr.getClient().send(CWvsContext.GuildPacket.RecruitmentGuild(chr));
            g.writeToDB(false);
         }
      }
   }

   public static final void GuildJoinDeny(LittleEndianAccessor slea, MapleCharacter chr) {
      if (chr != null) {
         byte action = slea.readByte();

         for(int i = 0; i < action; ++i) {
            int cid = slea.readInt();
            if (chr.getGuildId() > 0 && chr.getGuildRank() <= 2) {
               MapleGuild g = chr.getGuild();
               if (chr.getGuildRank() <= 2) {
                  g.removeRequest(cid);
                  int ch = World.Find.findChannel(cid);
                  if (ch < 0) {
                     return;
                  }

                  MapleCharacter c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(cid);
                  c.getClient().getSession().writeAndFlush(CWvsContext.GuildPacket.RequestDeny(c, g));
                  chr.setKeyValue(26015, "name", "");
                  request.put(cid, System.currentTimeMillis());
               } else {
                  chr.dropMessage(6, "길드 권한이 부족합니다.");
               }
            }
         }

      }
   }

   public static final void GuildRegisterAccept(LittleEndianAccessor slea, MapleCharacter chr) {
      if (chr != null) {
         byte action = slea.readByte();

         for(int i = 0; i < action; ++i) {
            int cid = slea.readInt();
            if (chr.getGuildId() > 0 && chr.getGuildRank() <= 2) {
               MapleGuild g = chr.getGuild();
               if (chr.getGuildRank() <= 2 && g != null) {
                  MapleCharacter c = null;
                  Iterator var7 = ChannelServer.getAllInstances().iterator();

                  while(var7.hasNext()) {
                     ChannelServer cs = (ChannelServer)var7.next();
                     c = cs.getPlayerStorage().getCharacterById(cid);
                     if (c != null) {
                        MapleGuildCharacter temp = g.getRequest(cid);
                        g.addGuildMember(temp);
                        c.getClient().getSession().writeAndFlush(CWvsContext.GuildPacket.showGuildInfo(chr));
                        g.removeRequest(cid);
                        c.setGuildId(g.getId());
                        c.setGuildRank((byte)5);
                        c.saveGuildStatus();
                        c.setKeyValue(26015, "name", "");
                        c.getClient().send(CWvsContext.GuildPacket.guildLoadAattendance());
                        c.dropMessage(5, "`" + g.getName() + "` 길드에 가입 되었습니다.");
                        Iterator var10 = World.Guild.getGuilds().iterator();

                        while(var10.hasNext()) {
                           MapleGuild guilds = (MapleGuild)var10.next();
                           if (guilds.getRequest(c.getId()) != null) {
                              guilds.removeRequest(c.getId());
                           }
                        }

                        respawnPlayer(c);
                        break;
                     }
                  }

                  if (c == null) {
                     MapleGuildCharacter temp = OfflineMapleGuildCharacter(cid, chr.getGuildId());
                     if (temp != null) {
                        temp.setOnline(false);
                        g.addGuildMember(temp);
                        MapleGuild.setOfflineGuildStatus(g.getId(), (byte)5, 0, (byte)5, cid);
                        g.removeRequest(cid);
                        Iterator var13 = World.Guild.getGuilds().iterator();

                        while(var13.hasNext()) {
                           MapleGuild guilds = (MapleGuild)var13.next();
                           if (guilds.getRequest(temp.getId()) != null) {
                              guilds.removeRequest(temp.getId());
                           }
                        }
                     } else {
                        chr.dropMessage(5, "존재하지 않는 캐릭터입니다.");
                     }
                  }
               } else {
                  chr.dropMessage(6, "길드 권한이 부족합니다.");
               }
            }
         }

      }
   }

   public static final MapleGuildCharacter OfflineMapleGuildCharacter(int cid, int gid) {
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection con = null;

      MapleGuildCharacter var7;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM characters where id = ?");
         ps.setInt(1, cid);
         rs = ps.executeQuery();
         if (!rs.next()) {
            ps.close();
            rs.close();
            return null;
         }

         byte gRank = rs.getByte("guildrank");
         byte aRank = rs.getByte("alliancerank");
         var7 = new MapleGuildCharacter(cid, rs.getShort("level"), rs.getString("name"), (byte)-1, rs.getInt("job"), gRank, rs.getInt("guildContribution"), aRank, gid, false, 0);
      } catch (SQLException var28) {
         System.err.println("Error Laod Offline MapleGuildCharacter");
         var28.printStackTrace();
         return null;
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (SQLException var27) {
               Logger.getLogger(GuildHandler.class.getName()).log(Level.SEVERE, (String)null, var27);
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (SQLException var26) {
               Logger.getLogger(GuildHandler.class.getName()).log(Level.SEVERE, (String)null, var26);
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var25) {
               var25.printStackTrace();
            }
         }

      }

      return var7;
   }

   public static final void GuildRequest(int guildid, MapleCharacter player) {
      player.dropMessage(1, "현재 이 기능은 사용하실 수 없습니다.");
   }

   public static void cancelGuildRequest(MapleClient c, MapleCharacter player) {
      player.dropMessage(1, "현재 이 기능은 사용하실 수 없습니다.");
   }

   public static void SendGuild(LittleEndianAccessor slea, MapleClient c) {
      c.getPlayer().dropMessage(1, "현재 이 기능은 사용하실 수 없습니다.");
   }

   public static final void Guild(LittleEndianAccessor slea, MapleClient c) {
      if (System.currentTimeMillis() >= nextPruneTime) {
         Iterator itr = invited.iterator();

         while(itr.hasNext()) {
            GuildHandler.Invited inv = (GuildHandler.Invited)itr.next();
            if (System.currentTimeMillis() >= inv.expiration) {
               itr.remove();
            }
         }

         nextPruneTime += 300000L;
      }

      try {
         int action = slea.readByte();
         MapleGuild mapleGuild1;
         String str2;
         int eff;
         Iterator var39;
         int cid;
         int m;
         switch(action) {
         case 1:
            String str1 = slea.readMapleAsciiString();
            c.getPlayer().setGuildName(str1);
            c.getSession().writeAndFlush(CWvsContext.GuildPacket.genericGuildMessage(42, str1));
         case 2:
         case 3:
         case 4:
         case 5:
         case 8:
         case 9:
         case 11:
         case 13:
         case 14:
         case 15:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 31:
         case 36:
         case 37:
         case 38:
         case 40:
         case 41:
         case 42:
         case 43:
         case 45:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         default:
            break;
         case 6:
            cid = slea.readInt();
            str2 = slea.readMapleAsciiString();
            if (cid == c.getPlayer().getId() && str2.equals(c.getPlayer().getName()) && c.getPlayer().getGuildId() > 0) {
               World.Guild.leaveGuild(c.getPlayer().getMGC());
               break;
            }

            return;
         case 7:
            cid = slea.readInt();
            str2 = slea.readMapleAsciiString();
            if (c.getPlayer().getGuildRank() <= 2 && c.getPlayer().getGuildId() > 0) {
               World.Guild.expelMember(c.getPlayer().getMGC(), str2, cid);
               respawnPlayer(c.getPlayer());
               break;
            }

            return;
         case 10:
            cid = slea.readInt();
            byte newRank = slea.readByte();
            if (newRank <= 1 || newRank > 5 || c.getPlayer().getGuildRank() > 2 || newRank <= 2 && c.getPlayer().getGuildRank() != 1 || c.getPlayer().getGuildId() <= 0) {
               return;
            }

            World.Guild.changeRank(c.getPlayer().getGuildId(), cid, newRank);
            break;
         case 12:
            if (c.getPlayer().getGuildId() > 0 && c.getPlayer().getGuildRank() == 1) {
               byte type = (byte)(slea.readByte() - 1);
               String ranks1 = slea.readMapleAsciiString();
               int role = type != 0 ? slea.readInt() : -1;
               World.Guild.changeRankTitleRole(c.getPlayer(), ranks1, role, type);
               break;
            }

            return;
         case 16:
            mapleGuild1 = World.Guild.getGuild(c.getPlayer().getGuildId());
            if (c.getPlayer().getGuildId() > 0 && c.getPlayer().getGuildRank() == 1) {
               byte isCustomImage = slea.readByte();
               if (isCustomImage == 0) {
                  short bg = slea.readShort();
                  byte bgcolor = slea.readByte();
                  short logo = slea.readShort();
                  byte logocolor = slea.readByte();
                  World.Guild.setGuildEmblem(c.getPlayer(), bg, bgcolor, logo, logocolor);
               } else {
                  if (mapleGuild1.getGP() < 250000) {
                     c.getPlayer().dropMessage(1, "[알림] GP가 부족합니다.");
                     c.getPlayer().getClient().send(CWvsContext.enableActions(c.getPlayer()));
                     return;
                  }

                  mapleGuild1.setGuildGP(mapleGuild1.getGP() - 250000);
                  m = slea.readInt();
                  byte[] imgdata = new byte[m];

                  for(int n = 0; n < m; ++n) {
                     imgdata[n] = slea.readByte();
                  }

                  World.Guild.setGuildCustomEmblem(c.getPlayer(), imgdata);
               }

               respawnPlayer(c.getPlayer());
               break;
            }

            c.getPlayer().dropMessage(1, "길드가 없거나 마스터가 아닙니다.");
            return;
         case 17:
            String notice = slea.readMapleAsciiString();
            if (notice.length() <= 100 && c.getPlayer().getGuildId() > 0 && c.getPlayer().getGuildRank() <= 2) {
               World.Guild.setGuildNotice(c.getPlayer(), notice);
               break;
            }

            return;
         case 29:
            mapleGuild1 = World.Guild.getGuild(c.getPlayer().getGuildId());
            if (mapleGuild1 == null) {
               return;
            }

            if (mapleGuild1.getGP() < 50000) {
               c.getPlayer().dropMessage(1, "GP가 부족합니다.");
               c.send(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            mapleGuild1.setGuildGP(mapleGuild1.getGP() - '썐');
            mapleGuild1.getSkills().clear();
            mapleGuild1.broadcast(CWvsContext.GuildPacket.showGuildInfo(mapleGuild1));
            c.getPlayer().dropMessage(1, "길드스킬 초기화가 완료 되었습니다. 길드창을 닫았다 열어주세요.");
            break;
         case 30:
            cid = slea.readInt();
            if (c.getPlayer().getGuildId() > 0 && c.getPlayer().getGuildRank() <= 1) {
               World.Guild.setGuildLeader(c.getPlayer().getGuildId(), cid);
               break;
            }

            return;
         case 32:
            mapleGuild1 = World.Guild.getGuild(slea.readInt());
            c.getSession().writeAndFlush(CWvsContext.GuildPacket.LooksGuildInformation(mapleGuild1));
            break;
         case 33:
            List<MapleGuild> g = new ArrayList();
            var39 = World.Guild.getGuilds().iterator();

            while(var39.hasNext()) {
               MapleGuild mapleGuild = (MapleGuild)var39.next();
               if (mapleGuild.getRequest(c.getPlayer().getId()) != null) {
                  g.add(mapleGuild);
               }
            }

            c.getSession().writeAndFlush(CWvsContext.GuildPacket.RequestListGuild(g));
            break;
         case 34:
            if (c.getPlayer().getGuildId() > 0 && c.getPlayer().getGuildRank() <= 2) {
               String name = slea.readMapleAsciiString();
               MapleGuildResponse mgr = MapleGuild.sendInvite(c, name);
               if (mgr != null) {
                  c.getSession().writeAndFlush(mgr.getPacket());
               } else {
                  GuildHandler.Invited inv = new GuildHandler.Invited(name, c.getPlayer().getGuildId());
                  if (!invited.contains(inv)) {
                     invited.add(inv);
                  }
               }
               break;
            }

            return;
         case 35:
            MapleGuild guild = World.Guild.getGuild(c.getPlayer().getGuildId());
            int size = 0;
            var39 = guild.getMembers().iterator();

            while(var39.hasNext()) {
               MapleGuildCharacter member = (MapleGuildCharacter)var39.next();
               if (member.getLastAttendance(member.getId()) == GameConstants.getCurrentDateday()) {
                  ++size;
               }
            }

            c.getPlayer().setLastAttendance(GameConstants.getCurrentDateday());
            guild.setAfterAttance(guild.getAfterAttance() + 30);
            if (size == 10 || size == 30 || size == 60 || size == 100) {
               m = size == 100 ? 2000 : (size == 60 ? 1000 : (size == 30 ? 100 : 50));
               guild.setAfterAttance(guild.getAfterAttance() + m);
               guild.setGuildFame(guild.getFame() + m);
               guild.setGuildGP(guild.getGP() + m / 100 * 30);
            }

            c.getPlayer().saveGuildStatus();
            World.Guild.gainContribution(guild.getId(), 30, c.getPlayer().getId());
            GuildBroadCast(CWvsContext.GuildPacket.guildAattendance(guild, c.getPlayer()), guild);
            if (guild.getFame() >= GameConstants.getGuildExpNeededForLevel(guild.getLevel())) {
               guild.setGuildLevel(guild.getLevel() + 1);
               GuildBroadCast(CWvsContext.serverNotice(5, "", "<길드> 길드의 레벨이 상승 하였습니다."), guild);
            }
            break;
         case 39:
            short mode = slea.readShort();
            String text = slea.readMapleAsciiString();
            if (mode == 4) {
               c.getSession().writeAndFlush(CWvsContext.GuildPacket.RecruitmentGuild(c.getPlayer()));
            } else {
               int option = slea.readShort();
               slea.skip(2);
               c.getSession().writeAndFlush(CWvsContext.GuildPacket.showSearchGuildInfo(c.getPlayer(), World.Guild.getGuildsByName(text, option == 1, (byte)mode), text, (byte)mode, option));
            }
            break;
         case 44:
            Skill skilli = SkillFactory.getSkill(slea.readInt());
            if (c.getPlayer().getGuildId() > 0 && skilli != null && skilli.getId() >= 91000000) {
               eff = World.Guild.getSkillLevel(c.getPlayer().getGuildId(), skilli.getId()) + 1;
               if (eff > skilli.getMaxLevel()) {
                  return;
               }

               SecondaryStatEffect skillid = skilli.getEffect(eff);
               if (skillid.getReqGuildLevel() >= 0 && c.getPlayer().getMeso() >= (long)skillid.getPrice()) {
                  if (World.Guild.purchaseSkill(c.getPlayer().getGuildId(), skillid.getSourceId(), c.getPlayer().getName(), c.getPlayer().getId())) {
                  }
                  break;
               }

               return;
            }

            return;
         case 46:
            if (c.getPlayer().getGuildId() <= 0) {
               return;
            }

            int sid = slea.readInt();
            eff = World.Guild.getSkillLevel(c.getPlayer().getGuildId(), sid);
            SkillFactory.getSkill(sid).getEffect(eff).applyTo(c.getPlayer());
            c.getSession().writeAndFlush(CField.skillCooldown(sid, 3600000));
            c.getPlayer().addCooldown(sid, System.currentTimeMillis(), 3600000L);
            break;
         case 59:
            if (c.getPlayer().getGuildId() > 0) {
               c.getPlayer().dropMessage(1, "이미 길드에 가입되어 있어 길드를 만들 수 없습니다.");
               return;
            }

            if (c.getPlayer().getMeso() < 5000000L) {
               c.getPlayer().dropMessage(1, "길드 제작에 필요한 메소 [500만 메소] 가 충분하지 않습니다.");
               return;
            }

            String guildName = c.getPlayer().getGuildName();
            if (!isGuildNameAcceptable(guildName)) {
               c.getPlayer().dropMessage(1, "해당 길드 이름은 만들 수 없습니다.");
               return;
            }

            int guildId = World.Guild.createGuild(c.getPlayer().getId(), guildName);
            if (guildId == 0) {
               c.getPlayer().dropMessage(1, "잠시후에 다시 시도 해주세요.");
               return;
            }

            c.getPlayer().gainMeso(-5000000L, true, true);
            c.getPlayer().setGuildId(guildId);
            c.getPlayer().setGuildRank((byte)1);
            c.getPlayer().saveGuildStatus();
            MapleGuild mapleGuild2 = World.Guild.getGuild(guildId);
            String[] arrayOfString2 = new String[]{"마스터", "부마스터", null, null, null};
            int a = 1;

            for(int k = 2; k < 5; ++k) {
               arrayOfString2[k] = "길드원" + a;
               ++a;
            }

            mapleGuild2.changeRankTitle(c.getPlayer(), arrayOfString2);
            mapleGuild2.setLevel(1);
            KoreaCalendar kc = new KoreaCalendar();
            String var10001 = kc.getYears();
            mapleGuild2.setLastResetDay(Integer.parseInt(var10001 + kc.getMonths() + kc.getDays()));
            World.Guild.setGuildMemberOnline(c.getPlayer().getMGC(), true, c.getChannel());
            c.getSession().writeAndFlush(CWvsContext.GuildPacket.newGuildInfo(c.getPlayer()));
            c.getSession().writeAndFlush(CWvsContext.GuildPacket.showGuildInfo(c.getPlayer()));
            respawnPlayer(c.getPlayer());
         }
      } catch (Exception var43) {
         var43.printStackTrace();
      }

   }

   public static void guildRankingRequest(byte type, MapleClient c) {
      c.getSession().writeAndFlush(CWvsContext.GuildPacket.showGuildRanks(type, c, MapleGuildRanking.getInstance()));
   }

   public static void GuildBroadCast(byte[] packet, MapleGuild guild) {
      Iterator var2 = ChannelServer.getAllInstances().iterator();

      while(var2.hasNext()) {
         ChannelServer cs = (ChannelServer)var2.next();
         Iterator var4 = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(var4.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var4.next();
            if (chr.getGuildId() == guild.getId()) {
               chr.getClient().getSession().writeAndFlush(packet);
            }
         }
      }

   }

   private static class Invited {
      public String name;
      public int gid;
      public long expiration;

      public Invited(String n, int id) {
         this.name = n.toLowerCase();
         this.gid = id;
         this.expiration = System.currentTimeMillis() + 3600000L;
      }

      public boolean equals(Object other) {
         if (!(other instanceof GuildHandler.Invited)) {
            return false;
         } else {
            GuildHandler.Invited oth = (GuildHandler.Invited)other;
            return this.gid == oth.gid && this.name.equals(oth);
         }
      }
   }
}
