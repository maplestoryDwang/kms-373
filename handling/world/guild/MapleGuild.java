package handling.world.guild;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.RecvPacketOpcode;
import handling.channel.ChannelServer;
import handling.world.World;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sql.rowset.serial.SerialBlob;
import log.DBLogger;
import log.LogType;
import server.SecondaryStatEffect;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.PacketHelper;

public class MapleGuild implements Comparable<MapleGuild> {
   public static final long serialVersionUID = 6322150443228168192L;
   private final List<MapleGuildCharacter> members = new CopyOnWriteArrayList();
   private final List<MapleGuildCharacter> requests = new ArrayList();
   private final Map<Integer, MapleGuildSkill> guildSkills = new HashMap();
   private final String[] rankTitles = new String[5];
   private final int[] rankRoles = new int[5];
   private String name;
   private String notice;
   private double guildScore = 0.0D;
   private int id;
   private int gp;
   private int logo;
   private int logoColor;
   private int leader;
   private int capacity;
   private int logoBG;
   private int logoBGColor;
   private int signature;
   private int level;
   private int noblessskillpoint;
   private int guildlevel;
   private int fame;
   private int beforeattance;
   private int afterattance;
   private int lastResetDay;
   private int weekReputation;
   private boolean bDirty = true;
   private boolean proper = true;
   private int allianceid = 0;
   private int invitedid = 0;
   private byte[] customEmblem;
   private boolean init = false;
   private boolean changed_skills = false;
   private boolean changed_requests = false;

   public MapleGuild(int guildid) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = ?");
         ps.setInt(1, guildid);
         rs = ps.executeQuery();
         if (rs.first()) {
            this.id = guildid;
            this.name = rs.getString("name");
            this.gp = rs.getInt("GP");
            this.fame = rs.getInt("fame");
            this.guildlevel = rs.getInt("level");
            this.logo = rs.getInt("logo");
            this.logoColor = rs.getInt("logoColor");
            this.logoBG = rs.getInt("logoBG");
            this.logoBGColor = rs.getInt("logoBGColor");
            this.capacity = rs.getInt("capacity");
            this.rankTitles[0] = rs.getString("rank1title");
            this.rankTitles[1] = rs.getString("rank2title");
            this.rankTitles[2] = rs.getString("rank3title");
            this.rankTitles[3] = rs.getString("rank4title");
            this.rankTitles[4] = rs.getString("rank5title");
            this.rankRoles[0] = rs.getInt("rank1role");
            this.rankRoles[1] = rs.getInt("rank2role");
            this.rankRoles[2] = rs.getInt("rank3role");
            this.rankRoles[3] = rs.getInt("rank4role");
            this.rankRoles[4] = rs.getInt("rank5role");
            this.leader = rs.getInt("leader");
            this.notice = rs.getString("notice");
            this.signature = rs.getInt("signature");
            this.allianceid = rs.getInt("alliance");
            this.guildScore = (double)rs.getInt("score");
            this.beforeattance = rs.getInt("beforeattance");
            this.afterattance = rs.getInt("afterattance");
            this.lastResetDay = rs.getInt("lastResetDay");
            this.weekReputation = rs.getInt("weekReputation");
            this.noblessskillpoint = rs.getInt("noblesspoint");
            Blob custom = rs.getBlob("customEmblem");
            if (custom != null) {
               this.customEmblem = custom.getBytes(1L, (int)custom.length());
            }

            rs.close();
            ps.close();
            MapleGuildAlliance alliance = World.Alliance.getAlliance(this.allianceid);
            if (alliance == null) {
               this.allianceid = 0;
            }

            ps = con.prepareStatement("SELECT id, name, level, job, guildrank, guildContribution, alliancerank, lastattendance FROM characters WHERE guildid = ? ORDER BY guildrank ASC, name ASC", 1008);
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            if (!rs.first()) {
               rs.close();
               ps.close();
               this.writeToDB(true);
               this.proper = false;
               return;
            }

            boolean leaderCheck = false;
            int gFix = 0;
            byte aFix = 0;

            do {
               int cid = rs.getInt("id");
               byte gRank = rs.getByte("guildrank");
               byte by = rs.getByte("alliancerank");
               if (cid == this.leader) {
                  leaderCheck = true;
                  if (gRank != 1) {
                     gRank = 1;
                     gFix = 1;
                  }

                  if (alliance != null) {
                     if (alliance.getLeaderId() == cid && by != 1) {
                        by = 1;
                        aFix = 1;
                     } else if (alliance.getLeaderId() != cid && by != 2) {
                        by = 2;
                        aFix = 2;
                     }
                  }
               } else {
                  if (gRank == 1) {
                     gRank = 2;
                     gFix = 2;
                  }

                  if (by < 3) {
                     by = 3;
                     aFix = 3;
                  }
               }

               this.members.add(new MapleGuildCharacter(cid, rs.getShort("level"), rs.getString("name"), (byte)-1, rs.getInt("job"), gRank, rs.getInt("guildContribution"), by, guildid, false, rs.getInt("lastattendance")));
            } while(rs.next());

            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM guildsrequest WHERE gid = ?", 1008);
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            ArrayList request = new ArrayList();

            while(rs.next()) {
               request.add(new Pair(rs.getInt("cid"), rs.getInt("gid")));
            }

            rs.close();
            ps.close();
            Iterator var30 = request.iterator();

            while(var30.hasNext()) {
               Pair pair = (Pair)var30.next();
               ps = con.prepareStatement("SELECT id, name, level, job, guildrank, guildContribution, alliancerank FROM characters WHERE id = ?", 1008);
               ps.setInt(1, (Integer)pair.left);

               byte gRank;
               byte aRank;
               for(rs = ps.executeQuery(); rs.next(); this.requests.add(new MapleGuildCharacter((Integer)pair.left, rs.getShort("level"), rs.getString("name"), (byte)-1, rs.getInt("job"), gRank, rs.getInt("guildContribution"), aRank, (Integer)pair.right, false, 0))) {
                  gRank = rs.getByte("guildrank");
                  aRank = rs.getByte("alliancerank");
                  if ((Integer)pair.left == this.leader) {
                     leaderCheck = true;
                     if (gRank != 1) {
                        gRank = 1;
                        gFix = 1;
                     }

                     if (alliance != null) {
                        if (alliance.getLeaderId() == (Integer)pair.left && aRank != 1) {
                           aRank = 1;
                           aFix = 1;
                        } else if (alliance.getLeaderId() != (Integer)pair.left && aRank != 2) {
                           aRank = 2;
                           aFix = 2;
                        }
                     }
                  } else {
                     if (gRank == 1) {
                        gRank = 2;
                        gFix = 2;
                     }

                     if (aRank < 3) {
                        aRank = 3;
                        aFix = 3;
                     }
                  }
               }

               rs.close();
               ps.close();
            }

            if (!leaderCheck) {
               this.writeToDB(true);
               this.proper = false;
               return;
            }

            if (gFix > 0) {
               ps = con.prepareStatement("UPDATE characters SET guildrank = ? WHERE id = ?");
               ps.setByte(1, (byte)gFix);
               ps.setInt(2, this.leader);
               ps.executeUpdate();
               ps.close();
            }

            if (aFix > 0) {
               ps = con.prepareStatement("UPDATE characters SET alliancerank = ? WHERE id = ?");
               ps.setByte(1, (byte)aFix);
               ps.setInt(2, this.leader);
               ps.executeUpdate();
               ps.close();
            }

            ps = con.prepareStatement("SELECT * FROM guildskills WHERE guildid = ?");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();

            while(rs.next()) {
               int sid = rs.getInt("skillid");
               if (sid < 91000000) {
                  rs.close();
                  ps.close();
                  this.writeToDB(true);
                  this.proper = false;
                  return;
               }

               this.guildSkills.put(sid, new MapleGuildSkill(sid, rs.getInt("level"), rs.getLong("timestamp"), rs.getString("purchaser"), ""));
            }

            rs.close();
            ps.close();
            this.level = this.calculateLevel();
            return;
         }

         rs.close();
         ps.close();
         this.id = -1;
      } catch (SQLException var27) {
         var27.printStackTrace();
         return;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            var26.printStackTrace();
         }

      }

   }

   public boolean isProper() {
      return this.proper;
   }

   public final void setGuildLevel(int level) {
      this.guildlevel = level;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET level = ? WHERE guildid = ?");
         ps.setInt(1, this.guildlevel);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var17) {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var16) {
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var15) {
         }

      }

   }

   public final void setBeforeAttance(int attance) {
      this.beforeattance = attance;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET beforeattance = ? WHERE guildid = ?");
         ps.setInt(1, this.beforeattance);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var17) {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var16) {
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var15) {
         }

      }

   }

   public final void setAfterAttance(int attance) {
      this.afterattance = attance;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET afterattance = ? WHERE guildid = ?");
         ps.setInt(1, this.afterattance);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var17) {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var16) {
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var15) {
         }

      }

   }

   public final void setGuildFame(int fame) {
      this.fame = fame;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET fame = ? WHERE guildid = ?");
         ps.setInt(1, this.fame);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var17) {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var16) {
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var15) {
         }

      }

   }

   public final void setGuildGP(int gp) {
      this.gp = gp;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET GP = ? WHERE guildid = ?");
         ps.setInt(1, gp);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var17) {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var16) {
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var15) {
         }

      }

      this.broadcast(CWvsContext.GuildPacket.guildUpdateOnlyGP(this.id, this.gp));
   }

   public final void writeToDB(boolean bDisband) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         if (!bDisband) {
            StringBuilder buf = new StringBuilder("UPDATE guilds SET GP = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?, ");

            for(int i = 1; i < 6; ++i) {
               buf.append("rank").append(i).append("title = ?, ");
               buf.append("rank").append(i).append("role = ?, ");
            }

            buf.append("capacity = ?, notice = ?, alliance = ?, leader = ?, customEmblem = ?, noblesspoint = ?, score = ?, weekReputation = ? WHERE guildid = ?");
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement(buf.toString());
            ps.setInt(1, this.gp);
            ps.setInt(2, this.logo);
            ps.setInt(3, this.logoColor);
            ps.setInt(4, this.logoBG);
            ps.setInt(5, this.logoBGColor);
            ps.setString(6, this.rankTitles[0]);
            ps.setInt(7, this.rankRoles[0]);
            ps.setString(8, this.rankTitles[1]);
            ps.setInt(9, this.rankRoles[1]);
            ps.setString(10, this.rankTitles[2]);
            ps.setInt(11, this.rankRoles[2]);
            ps.setString(12, this.rankTitles[3]);
            ps.setInt(13, this.rankRoles[3]);
            ps.setString(14, this.rankTitles[4]);
            ps.setInt(15, this.rankRoles[4]);
            ps.setInt(16, this.capacity);
            ps.setString(17, this.notice);
            ps.setInt(18, this.allianceid);
            ps.setInt(19, this.leader);
            Blob blob = null;
            if (this.customEmblem != null) {
               blob = new SerialBlob(this.customEmblem);
            }

            ps.setBlob(20, blob);
            ps.setInt(21, this.noblessskillpoint);
            ps.setInt(22, (int)this.guildScore);
            ps.setInt(23, this.weekReputation);
            ps.setInt(24, this.id);
            ps.executeUpdate();
            ps.close();
            Iterator var7;
            if (this.changed_skills) {
               ps = con.prepareStatement("DELETE FROM guildskills WHERE guildid = ?");
               ps.setInt(1, this.id);
               ps.execute();
               ps.close();
               ps = con.prepareStatement("INSERT INTO guildskills(`guildid`, `skillid`, `level`, `timestamp`, `purchaser`) VALUES(?, ?, ?, ?, ?)");
               ps.setInt(1, this.id);
               var7 = this.guildSkills.values().iterator();

               while(var7.hasNext()) {
                  MapleGuildSkill mapleGuildSkill = (MapleGuildSkill)var7.next();
                  ps.setInt(2, mapleGuildSkill.skillID);
                  ps.setByte(3, (byte)mapleGuildSkill.level);
                  ps.setLong(4, mapleGuildSkill.timestamp);
                  ps.setString(5, mapleGuildSkill.purchaser);
                  ps.execute();
               }

               ps.close();
            }

            this.changed_skills = false;
            if (this.changed_requests) {
               ps = con.prepareStatement("DELETE FROM guildsrequest WHERE gid = ?");
               ps.setInt(1, this.id);
               ps.execute();
               ps.close();
               ps = con.prepareStatement("INSERT INTO guildsrequest(`gid`, `cid`) VALUES(?, ?)");
               var7 = this.requests.iterator();

               while(var7.hasNext()) {
                  MapleGuildCharacter mgc = (MapleGuildCharacter)var7.next();
                  ps.setInt(1, mgc.getGuildId());
                  ps.setInt(2, mgc.getId());
                  ps.execute();
               }

               ps.close();
            }

            this.changed_requests = false;
         } else {
            con = DatabaseConnection.getConnection();

            try {
               ps = con.prepareStatement("DELETE FROM guildskills WHERE guildid = ?");
               ps.setInt(1, this.id);
               ps.executeUpdate();
               ps.close();
            } catch (Exception var18) {
               var18.printStackTrace();
            }

            ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
            ps.setInt(1, this.id);
            ps.executeUpdate();
            ps.close();
            if (this.allianceid > 0) {
               MapleGuildAlliance alliance = World.Alliance.getAlliance(this.allianceid);
               if (alliance != null) {
                  alliance.removeGuild(this.id, false);
               }
            }

            this.broadcast(CWvsContext.GuildPacket.guildDisband(this.id));
            this.broadcast(CWvsContext.GuildPacket.guildDisband2());
         }
      } catch (SQLException var19) {
         var19.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var17) {
            var17.printStackTrace();
         }

      }

   }

   public final int getId() {
      return this.id;
   }

   public final int getLeaderId() {
      return this.leader;
   }

   public final MapleCharacter getLeader(MapleClient c) {
      return c.getChannelServer().getPlayerStorage().getCharacterById(this.leader);
   }

   public final int getGP() {
      return this.gp;
   }

   public final int getLogo() {
      return this.logo;
   }

   public final void setLogo(int l) {
      this.logo = l;
   }

   public final int getLogoColor() {
      return this.logoColor;
   }

   public final void setLogoColor(int c) {
      this.logoColor = c;
   }

   public final int getLogoBG() {
      return this.logoBG;
   }

   public final void setLogoBG(int bg) {
      this.logoBG = bg;
   }

   public final int getLogoBGColor() {
      return this.logoBGColor;
   }

   public final void setLogoBGColor(int c) {
      this.logoBGColor = c;
   }

   public final String getNotice() {
      return this.notice == null ? "" : this.notice;
   }

   public final String getName() {
      return this.name;
   }

   public final int getCapacity() {
      return this.capacity;
   }

   public final int getSignature() {
      return this.signature;
   }

   public final void RankBroadCast(byte[] packet, int rank) {
      Iterator var3 = this.members.iterator();

      while(var3.hasNext()) {
         MapleGuildCharacter mgc = (MapleGuildCharacter)var3.next();
         if (mgc.isOnline() && mgc.getGuildRank() == rank) {
            int ch = World.Find.findChannel(mgc.getId());
            if (ch < 0) {
               return;
            }

            MapleCharacter c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(mgc.getId());
            if (c != null && c.getGuildId() == mgc.getGuildId()) {
               c.getClient().getSession().writeAndFlush(packet);
            }
         }
      }

   }

   public final void broadcast(byte[] packet) {
      this.broadcast(packet, -1, MapleGuild.BCOp.NONE);
   }

   public final void broadcast(byte[] packet, int exception) {
      this.broadcast(packet, exception, MapleGuild.BCOp.NONE);
   }

   public final void broadcast(byte[] packet, int exceptionId, MapleGuild.BCOp bcop) {
      this.broadcast(packet, exceptionId, bcop, (MapleCharacter)null);
   }

   public final void broadcast(byte[] packet, int exceptionId, MapleGuild.BCOp bcop, MapleCharacter chr) {
      this.buildNotifications();
      Iterator var5 = this.members.iterator();

      while(var5.hasNext()) {
         MapleGuildCharacter mgc = (MapleGuildCharacter)var5.next();
         if (bcop == MapleGuild.BCOp.DISBAND) {
            if (mgc.isOnline()) {
               World.Guild.setGuildAndRank(mgc.getId(), 0, 5, 0, 5);
            } else {
               setOfflineGuildStatus(0, (byte)5, 0, (byte)5, mgc.getId());
            }
         } else if (mgc.isOnline() && mgc.getId() != exceptionId) {
            if (bcop == MapleGuild.BCOp.EMBELMCHANGE) {
               World.Guild.changeEmblem(chr, mgc.getId(), this);
            } else {
               World.Broadcast.sendGuildPacket(mgc.getId(), packet, exceptionId, this.id);
            }
         }
      }

   }

   private final void buildNotifications() {
      if (this.bDirty) {
         List<Integer> mem = new LinkedList();
         Iterator toRemove = this.members.iterator();

         while(true) {
            while(true) {
               MapleGuildCharacter mgc;
               do {
                  if (!toRemove.hasNext()) {
                     this.bDirty = false;
                     return;
                  }

                  mgc = (MapleGuildCharacter)toRemove.next();
               } while(!mgc.isOnline());

               if (!mem.contains(mgc.getId()) && mgc.getGuildId() == this.id) {
                  mem.add(mgc.getId());
               } else {
                  this.members.remove(mgc);
               }
            }
         }
      }
   }

   public final void setOnline(int cid, boolean online, int channel) {
      boolean bBroadcast = true;
      Iterator var5 = this.members.iterator();

      while(var5.hasNext()) {
         MapleGuildCharacter mgc = (MapleGuildCharacter)var5.next();
         if (mgc.getGuildId() == this.id && mgc.getId() == cid) {
            if (mgc.isOnline() == online) {
               bBroadcast = false;
            }

            mgc.setOnline(online);
            mgc.setChannel((byte)channel);
            break;
         }
      }

      if (bBroadcast) {
         this.broadcast(CWvsContext.GuildPacket.guildMemberOnline(this.id, cid, online), cid);
         if (this.allianceid > 0) {
            World.Alliance.sendGuild(CWvsContext.AlliancePacket.allianceMemberOnline(this.allianceid, this.id, cid, online), this.id, this.allianceid);
         }
      }

      this.bDirty = true;
      this.init = true;
   }

   public final void guildChat(MapleCharacter player, String msg, LittleEndianAccessor slea, RecvPacketOpcode recv) {
      Item item = null;
      if (recv == RecvPacketOpcode.PARTYCHATITEM) {
         byte invType = (byte)slea.readInt();
         byte pos = (byte)slea.readInt();
         item = player.getInventory(MapleInventoryType.getByType(pos > 0 ? invType : -1)).getItem((short)pos);
      }

      DBLogger.getInstance().logChat(LogType.Chat.Guild, player.getId(), this.name, msg, "[" + this.getName() + "]");
      this.broadcast(CField.multiChat(player, msg, 2, item), player.getId());
   }

   public final void allianceChat(MapleCharacter player, String msg, LittleEndianAccessor slea, RecvPacketOpcode recv) {
      Item item = null;
      if (recv == RecvPacketOpcode.PARTYCHATITEM) {
         byte invType = (byte)slea.readInt();
         byte pos = (byte)slea.readInt();
         item = player.getInventory(MapleInventoryType.getByType(pos > 0 ? invType : -1)).getItem((short)pos);
      }

      this.broadcast(CField.multiChat(player, msg, 3, item), player.getId());
   }

   public final String getRankTitle(int rank) {
      return this.rankTitles[rank - 1];
   }

   public final int getRankRole(int role) {
      return this.rankRoles[role - 1];
   }

   public final byte[] getCustomEmblem() {
      return this.customEmblem;
   }

   public final void setCustomEmblem(byte[] emblem) {
      this.customEmblem = emblem;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         Blob blob = null;
         if (emblem != null) {
            blob = new SerialBlob(emblem);
         }

         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET customEmblem = ? WHERE guildid = ?");
         ps.setBlob(1, blob);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var14) {
         var14.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var13) {
            var13.printStackTrace();
         }

      }

   }

   public int getAllianceId() {
      return this.allianceid;
   }

   public int getInvitedId() {
      return this.invitedid;
   }

   public void setInvitedId(int iid) {
      this.invitedid = iid;
   }

   public void setAllianceId(int a) {
      this.allianceid = a;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET alliance = ? WHERE guildid = ?");
         ps.setInt(1, a);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var14) {
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var13) {
            var13.printStackTrace();
         }

      }

   }

   public static final int createGuild(int leaderId, String name) {
      if (name.length() > 12) {
         return 0;
      } else {
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         byte var5;
         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (!rs.first()) {
               ps.close();
               rs.close();
               ps = con.prepareStatement("INSERT INTO guilds (`leader`, `name`, `signature`, `alliance`) VALUES (?, ?, ?, 0)", 1);
               ps.setInt(1, leaderId);
               ps.setString(2, name);
               ps.setInt(3, (int)(System.currentTimeMillis() / 1000L));
               ps.executeUpdate();
               rs = ps.getGeneratedKeys();
               int ret = 0;
               if (rs.next()) {
                  ret = rs.getInt(1);
               }

               rs.close();
               ps.close();
               con.close();
               int var20 = ret;
               return var20;
            }

            rs.close();
            ps.close();
            con.close();
            var5 = 0;
         } catch (SQLException var17) {
            var17.printStackTrace();
            byte var6 = 0;
            return var6;
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var16) {
               var16.printStackTrace();
            }

         }

         return var5;
      }
   }

   public final int addGuildMember(MapleGuildCharacter mgc) {
      if (this.members.size() >= this.capacity) {
         return 0;
      } else {
         for(int i = this.members.size() - 1; i >= 0; --i) {
            if (((MapleGuildCharacter)this.members.get(i)).getGuildRank() < 5 || ((MapleGuildCharacter)this.members.get(i)).getName().compareTo(mgc.getName()) < 0) {
               this.members.add(i + 1, mgc);
               this.bDirty = true;
               break;
            }
         }

         this.broadcast(CWvsContext.GuildPacket.newGuildMember(mgc));
         if (this.allianceid > 0) {
            World.Alliance.sendGuild(this.allianceid);
         }

         return 1;
      }
   }

   public final int addGuildMember(MapleCharacter chr, MapleGuild guild) {
      MapleGuildCharacter chrs = new MapleGuildCharacter(chr);
      this.members.add(chrs);
      this.broadcast(CWvsContext.GuildPacket.newGuildMember(chrs));
      return 1;
   }

   public final void leaveGuild(MapleGuildCharacter mgc) {
      Iterator itr = this.members.iterator();

      while(itr.hasNext()) {
         MapleGuildCharacter mgcc = (MapleGuildCharacter)itr.next();
         if (mgcc.getId() == mgc.getId()) {
            for(int i = 1; i <= 5; ++i) {
               this.RankBroadCast(CWvsContext.GuildPacket.memberLeft(mgcc, i != 5), i);
            }

            this.bDirty = true;
            this.members.remove(mgcc);
            if (mgc.isOnline()) {
               World.Guild.setGuildAndRank(mgcc.getId(), 0, 5, 0, 5);
            } else {
               setOfflineGuildStatus(0, (byte)5, 0, (byte)5, mgcc.getId());
            }
            break;
         }
      }

      if (this.bDirty && this.allianceid > 0) {
         World.Alliance.sendGuild(this.allianceid);
      }

   }

   public final void expelMember(MapleGuildCharacter initiator, String name, int cid) {
      Iterator itr = this.members.iterator();

      while(itr.hasNext()) {
         MapleGuildCharacter mgc = (MapleGuildCharacter)itr.next();
         if (mgc.getId() == cid && initiator.getGuildRank() < mgc.getGuildRank()) {
            this.broadcast(CWvsContext.GuildPacket.memberLeft(mgc, true));
            this.bDirty = true;
            if (mgc.isOnline()) {
               World.Guild.setGuildAndRank(cid, 0, 5, 0, 5);
            } else {
               MapleCharacterUtil.sendNote(mgc.getName(), initiator.getName(), "길드에서 강퇴당했습니다.", 0, 6, initiator.getId());
               setOfflineGuildStatus(0, (byte)5, 0, (byte)5, cid);
            }

            this.members.remove(mgc);
            break;
         }
      }

      if (this.bDirty && this.allianceid > 0) {
         World.Alliance.sendGuild(this.allianceid);
      }

   }

   public final void changeARank() {
      this.changeARank(false);
   }

   public final void changeARank(boolean leader) {
      if (this.allianceid > 0) {
         MapleGuildCharacter mgc;
         byte newRank;
         for(Iterator var2 = this.members.iterator(); var2.hasNext(); mgc.setAllianceRank(newRank)) {
            mgc = (MapleGuildCharacter)var2.next();
            newRank = 3;
            if (this.leader == mgc.getId()) {
               newRank = (byte)(leader ? 1 : 2);
            }

            if (mgc.isOnline()) {
               World.Guild.setGuildAndRank(mgc.getId(), this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank);
            } else {
               setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank, mgc.getId());
            }
         }

         World.Alliance.sendGuild(this.allianceid);
      }
   }

   public final void changeARank(int newRank) {
      if (this.allianceid > 0) {
         MapleGuildCharacter mgc;
         for(Iterator var2 = this.members.iterator(); var2.hasNext(); mgc.setAllianceRank((byte)newRank)) {
            mgc = (MapleGuildCharacter)var2.next();
            if (mgc.isOnline()) {
               World.Guild.setGuildAndRank(mgc.getId(), this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank);
            } else {
               setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), (byte)newRank, mgc.getId());
            }
         }

         World.Alliance.sendGuild(this.allianceid);
      }
   }

   public final boolean changeARank(int cid, int newRank) {
      if (this.allianceid <= 0) {
         return false;
      } else {
         Iterator var3 = this.members.iterator();

         MapleGuildCharacter mgc;
         do {
            if (!var3.hasNext()) {
               return false;
            }

            mgc = (MapleGuildCharacter)var3.next();
         } while(cid != mgc.getId());

         if (mgc.isOnline()) {
            World.Guild.setGuildAndRank(cid, this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank);
         } else {
            setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), (byte)newRank, cid);
         }

         mgc.setAllianceRank((byte)newRank);
         World.Alliance.sendGuild(this.allianceid);
         return true;
      }
   }

   public final void changeGuildLeader(int cid) {
      if (this.changeRank(cid, 1) && this.changeRank(this.leader, 2)) {
         if (this.allianceid > 0) {
            int aRank = this.getMGC(this.leader).getAllianceRank();
            if (aRank == 1) {
               World.Alliance.changeAllianceLeader(this.allianceid, cid, true);
            } else {
               this.changeARank(cid, aRank);
            }

            this.changeARank(this.leader, 3);
         }

         this.broadcast(CWvsContext.GuildPacket.guildLeaderChanged(this.id, this.leader, cid, this.allianceid));
         this.leader = cid;
         Connection con = null;
         PreparedStatement ps = null;
         Object rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE guilds SET leader = ? WHERE guildid = ?");
            ps.setInt(1, cid);
            ps.setInt(2, this.id);
            ps.execute();
            ps.close();
            con.close();
         } catch (SQLException var14) {
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var13) {
               var13.printStackTrace();
            }

         }
      }

   }

   public final boolean changeRank(int cid, int newRank) {
      Iterator var3 = this.members.iterator();

      MapleGuildCharacter mgc;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         mgc = (MapleGuildCharacter)var3.next();
      } while(cid != mgc.getId());

      if (mgc.isOnline()) {
         World.Guild.setGuildAndRank(cid, this.id, newRank, mgc.getGuildContribution(), mgc.getAllianceRank());
      } else {
         setOfflineGuildStatus(this.id, (byte)newRank, mgc.getGuildContribution(), mgc.getAllianceRank(), cid);
      }

      mgc.setGuildRank((byte)newRank);
      this.broadcast(CWvsContext.GuildPacket.changeRank(mgc));
      return true;
   }

   public final void setGuildNotice(MapleCharacter chr, String notice) {
      this.notice = notice;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET notice = ? WHERE guildid = ?");
         ps.setString(1, notice);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var15) {
         var15.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      this.broadcast(CWvsContext.GuildPacket.guildNotice(chr, notice));
   }

   public final void memberLevelJobUpdate(MapleGuildCharacter mgc) {
      Iterator var2 = this.members.iterator();

      while(var2.hasNext()) {
         MapleGuildCharacter member = (MapleGuildCharacter)var2.next();
         if (member.getId() == mgc.getId()) {
            int old_level = member.getLevel();
            int old_job = member.getJobId();
            member.setJobId(mgc.getJobId());
            member.setLevel((short)mgc.getLevel());
            if (old_level != mgc.getLevel() && mgc.getLevel() >= 200) {
               this.broadcast(CWvsContext.sendLevelup(false, mgc.getLevel(), mgc.getName()), mgc.getId());
            }

            if (old_job != mgc.getJobId()) {
               this.broadcast(CWvsContext.sendJobup(false, mgc.getJobId(), mgc.getName()), mgc.getId());
            }

            this.broadcast(CWvsContext.GuildPacket.guildMemberLevelJobUpdate(mgc));
            if (this.allianceid > 0) {
               World.Alliance.sendGuild(CWvsContext.AlliancePacket.updateAlliance(mgc, this.allianceid), this.id, this.allianceid);
            }
            break;
         }
      }

   }

   public void setRankTitle(String[] ranks) {
      for(int i = 0; i < 5; ++i) {
         this.rankTitles[i] = ranks[i];
      }

   }

   public final void changeRankTitle(MapleCharacter chr, String[] ranks) {
      int[] roles = this.rankRoles;

      for(int i = 0; i < 5; ++i) {
         this.rankTitles[i] = ranks[i];
      }

      this.updateRankRole();
      this.broadcast(CWvsContext.GuildPacket.rankTitleChange(125, chr, ranks, roles));
   }

   public final void changeRankRole(MapleCharacter chr, int[] roles) {
      String[] ranks = this.rankTitles;

      for(int i = 0; i < 5; ++i) {
         this.rankRoles[i] = roles[i];
      }

      this.updateRankRole();
      this.broadcast(CWvsContext.GuildPacket.rankTitleChange(127, chr, ranks, roles));
   }

   public final void changeRankTitleRole(MapleCharacter chr, String ranks, int roles, byte type) {
      String[] ranks1 = this.rankTitles;
      int[] roles1 = this.rankRoles;
      this.rankRoles[type] = roles;
      this.rankTitles[type] = ranks;
      this.updateRankRole();
      this.broadcast(CWvsContext.GuildPacket.rankTitleChange(122, chr, ranks1, roles1));
   }

   public final void updateRankRole() {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         StringBuilder buf = new StringBuilder("UPDATE guilds SET ");

         for(int i = 1; i < 6; ++i) {
            buf.append("rank").append(i).append("title = ?, ");
            buf.append("rank").append(i).append("role = ?");
            if (i != 5) {
               buf.append(", ");
            }
         }

         buf.append(" WHERE guildid = ?");
         ps = con.prepareStatement(buf.toString());
         ps.setString(1, this.rankTitles[0]);
         ps.setInt(2, this.rankRoles[0]);
         ps.setString(3, this.rankTitles[1]);
         ps.setInt(4, this.rankRoles[1]);
         ps.setString(5, this.rankTitles[2]);
         ps.setInt(6, this.rankRoles[2]);
         ps.setString(7, this.rankTitles[3]);
         ps.setInt(8, this.rankRoles[3]);
         ps.setString(9, this.rankTitles[4]);
         ps.setInt(10, this.rankRoles[4]);
         ps.setInt(11, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var14) {
         var14.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var13) {
            var13.printStackTrace();
         }

      }

   }

   public final void disbandGuild() {
      this.writeToDB(true);
      this.broadcast((byte[])null, -1, MapleGuild.BCOp.DISBAND);
   }

   public final void setGuildEmblem(MapleCharacter chr, short bg, byte bgcolor, short logo, byte logocolor) {
      this.logoBG = bg;
      this.logoBGColor = bgcolor;
      this.logo = logo;
      this.logoColor = logocolor;
      this.setCustomEmblem((byte[])null);
      this.broadcast((byte[])null, -1, MapleGuild.BCOp.EMBELMCHANGE, chr);
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ? WHERE guildid = ?");
         ps.setInt(1, logo);
         ps.setInt(2, this.logoColor);
         ps.setInt(3, this.logoBG);
         ps.setInt(4, this.logoBGColor);
         ps.setInt(5, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var18) {
         var18.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var17) {
            var17.printStackTrace();
         }

      }

   }

   public final void setGuildCustomEmblem(MapleCharacter chr, byte[] imgdata) {
      this.logoBG = 0;
      this.logoBGColor = 0;
      this.logo = 0;
      this.logoColor = 0;
      this.setCustomEmblem(imgdata);
      this.broadcast(CWvsContext.GuildPacket.changeCustomGuildEmblem(chr, imgdata));
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ? WHERE guildid = ?");
         ps.setInt(1, this.logo);
         ps.setInt(2, this.logoColor);
         ps.setInt(3, this.logoBG);
         ps.setInt(4, this.logoBGColor);
         ps.setInt(5, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var15) {
         var15.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

   }

   public final MapleGuildCharacter getMGC(int cid) {
      Iterator var2 = this.members.iterator();

      MapleGuildCharacter mgc;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         mgc = (MapleGuildCharacter)var2.next();
      } while(mgc.getId() != cid);

      return mgc;
   }

   public final boolean increaseCapacity(boolean trueMax) {
      if (this.capacity < (trueMax ? 200 : 100) && this.capacity + 10 <= (trueMax ? 200 : 100)) {
         this.capacity += 10;
         this.broadcast(CWvsContext.GuildPacket.guildCapacityChange(this.id, this.capacity));
         Connection con = null;
         PreparedStatement ps = null;
         Object rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE guilds SET capacity = ? WHERE guildid = ?");
            ps.setInt(1, this.capacity);
            ps.setInt(2, this.id);
            ps.execute();
            ps.close();
            con.close();
         } catch (SQLException var14) {
            var14.printStackTrace();
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var13) {
               var13.printStackTrace();
            }

         }

         return true;
      } else {
         return false;
      }
   }

   public final void gainGP(int amount) {
      this.gainGP(amount, true, -1);
   }

   public final void gainGP(int amount, boolean broadcast) {
      this.gainGP(amount, broadcast, -1);
   }

   public final void gainGP(int amount, boolean broadcast, int cid) {
      MapleGuildCharacter mg = this.getMGC(cid);
      MapleCharacter chr = null;
      Iterator var6 = ChannelServer.getAllInstances().iterator();

      while(var6.hasNext()) {
         ChannelServer cs = (ChannelServer)var6.next();
         Iterator var8 = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(var8.hasNext()) {
            MapleCharacter chr2 = (MapleCharacter)var8.next();
            if (chr2.getId() == cid) {
               chr = chr2;
            }
         }
      }

      if (amount != 0 && chr != null) {
         if (chr.getKeyValue(210302, "GP") < 10000L) {
            chr.setKeyValue(210302, "GP", (chr.getKeyValue(210302, "GP") + (long)amount).makeConcatWithConstants<invokedynamic>(chr.getKeyValue(210302, "GP") + (long)amount));
            mg.setGuildContribution(mg.getGuildContribution() + amount);
            chr.setGuildContribution(mg.getGuildContribution() + amount);
            chr.saveGuildStatus();
            this.setGuildFame(this.getFame() + amount);
            boolean var10 = false;
            int gp;
            if (amount < 100) {
               float gp2 = (float)amount;
               gp = (int)(gp2 / 100.0F * 30.0F);
            } else {
               gp = amount / 100 * 30;
            }

            this.setGuildGP(this.getGP() + gp);
            this.setWeekReputation(this.getWeekReputation() + gp);
            if (this.getFame() >= GameConstants.getGuildExpNeededForLevel(this.getLevel())) {
               this.setGuildLevel(this.getLevel() + 1);
               this.broadcast(CWvsContext.GuildPacket.showGuildInfo(chr));
               this.broadcast(CWvsContext.serverNotice(5, "", "<길드> 길드의 레벨이 상승 하였습니다."));
            }

            this.broadcast(CWvsContext.GuildPacket.updateGP(this.id, this.getFame(), this.getGP(), this.getLevel()));
            this.broadcast(CWvsContext.GuildPacket.GainGP(this, chr, mg.getGuildContribution()));
            if (broadcast) {
               this.broadcast(CWvsContext.InfoPacket.getGPMsg(amount));
               chr.getClient().send(CWvsContext.InfoPacket.getGPContribution(amount));
            }

         }
      }
   }

   public Collection<MapleGuildSkill> getSkills() {
      return this.guildSkills.values();
   }

   public Map<Integer, MapleGuildSkill> getGuildSkills() {
      return this.guildSkills;
   }

   public int getSkillLevel(int sid) {
      return !this.guildSkills.containsKey(sid) ? 0 : ((MapleGuildSkill)this.guildSkills.get(sid)).level;
   }

   public boolean activateSkill(int skill, String name) {
      if (!this.guildSkills.containsKey(skill)) {
         return false;
      } else {
         MapleGuildSkill ourSkill = (MapleGuildSkill)this.guildSkills.get(skill);
         SecondaryStatEffect skillid = SkillFactory.getSkill(skill).getEffect(ourSkill.level);
         if (ourSkill.timestamp <= System.currentTimeMillis() && skillid.getPeriod() > 0) {
            ourSkill.timestamp = System.currentTimeMillis() + (long)skillid.getPeriod() * 60000L;
            ourSkill.activator = name;
            this.writeToDB(false);
            this.broadcast(CWvsContext.GuildPacket.guildSkillPurchased(this.id, skill, ourSkill.level, ourSkill.timestamp, ourSkill.purchaser, name));
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean purchaseSkill(int skill, String name, int cid) {
      SecondaryStatEffect skillid = SkillFactory.getSkill(skill).getEffect(this.getSkillLevel(skill) + 1);
      if (skillid.getReqGuildLevel() <= this.getLevel() && skillid.getLevel() > this.getSkillLevel(skill)) {
         MapleGuildSkill ourSkill = (MapleGuildSkill)this.guildSkills.get(skill);
         if (ourSkill == null) {
            ourSkill = new MapleGuildSkill(skill, skillid.getLevel(), 0L, name, name);
            this.guildSkills.put(skill, ourSkill);
         } else {
            ourSkill.level = skillid.getLevel();
            ourSkill.purchaser = name;
            ourSkill.activator = name;
         }

         if (skillid.getPeriod() <= 0) {
            ourSkill.timestamp = -1L;
         } else {
            ourSkill.timestamp = System.currentTimeMillis() + (long)skillid.getPeriod() * 60000L;
         }

         this.changed_skills = true;
         this.writeToDB(false);
         this.broadcast(CWvsContext.GuildPacket.guildSkillPurchased(this.id, skill, ourSkill.level, ourSkill.timestamp, name, name));
         return true;
      } else {
         return false;
      }
   }

   public boolean removeSkill(int skill, String name) {
      if (this.guildSkills.containsKey(skill)) {
         this.guildSkills.remove(skill);
      }

      this.changed_skills = true;
      this.writeToDB(false);
      this.broadcast(CWvsContext.GuildPacket.guildSkillPurchased(this.id, skill, 0, -1L, name, name));
      return true;
   }

   public int getLevel() {
      return this.guildlevel;
   }

   public final int calculateLevel() {
      for(int i = 1; i < 30; ++i) {
         if (this.gp < GameConstants.getGuildExpNeededForLevel(i)) {
            return i;
         }
      }

      return 30;
   }

   public final int calculateGuildPoints() {
      int rgp = this.gp;

      for(int i = 1; i < 30; ++i) {
         if (rgp < GameConstants.getGuildExpNeededForLevel(i)) {
            return rgp;
         }

         rgp -= GameConstants.getGuildExpNeededForLevel(i);
      }

      return rgp;
   }

   public final void addMemberData(MaplePacketLittleEndianWriter mplew) {
      mplew.writeShort(this.members.size());
      Iterator var2 = this.members.iterator();

      MapleGuildCharacter mgc;
      while(var2.hasNext()) {
         mgc = (MapleGuildCharacter)var2.next();
         mplew.writeInt(mgc.getId());
      }

      var2 = this.members.iterator();

      while(var2.hasNext()) {
         mgc = (MapleGuildCharacter)var2.next();
         mplew.writeInt(mgc.getId());
         mplew.writeAsciiString(mgc.getName(), 13);
         mplew.writeInt(mgc.getJobId());
         mplew.writeInt(mgc.getLevel());
         mplew.writeInt(mgc.getGuildRank());
         mplew.writeInt(mgc.isOnline() ? 1 : 0);
         mplew.writeLong(PacketHelper.getTime(mgc.getLastDisconnectTime()));
         mplew.writeInt(mgc.getAllianceRank());
         mplew.writeInt(mgc.getGuildContribution());
         mplew.writeInt(mgc.getGuildContribution());
         mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
         mplew.writeInt(mgc.getLastAttendance(mgc.getId()));
         mplew.writeLong(PacketHelper.getTime(-2L));
         mplew.writeLong(PacketHelper.getTime(-2L));
      }

   }

   public final void addRequestMemberData(MaplePacketLittleEndianWriter mplew) {
      mplew.writeShort(this.requests.size());
      Iterator var2 = this.requests.iterator();

      MapleGuildCharacter mgc;
      while(var2.hasNext()) {
         mgc = (MapleGuildCharacter)var2.next();
         mplew.writeInt(mgc.getId());
      }

      var2 = this.requests.iterator();

      while(var2.hasNext()) {
         mgc = (MapleGuildCharacter)var2.next();
         mplew.writeInt(mgc.getId());
         mplew.writeAsciiString(mgc.getName(), 13);
         mplew.writeInt(mgc.getJobId());
         mplew.writeInt(mgc.getLevel());
         mplew.writeInt(mgc.getGuildRank());
         mplew.writeInt(mgc.isOnline() ? 1 : 0);
         mplew.writeLong(PacketHelper.getTime(mgc.getLastDisconnectTime()));
         mplew.writeInt(mgc.getAllianceRank());
         mplew.writeInt(mgc.getGuildContribution());
         mplew.writeInt(0);
         mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
         mplew.writeInt(0);
         mplew.writeLong(PacketHelper.getTime(-2L));
         mplew.writeLong(PacketHelper.getTime(-2L));
      }

      mplew.writeInt(this.requests.size());
      var2 = this.requests.iterator();

      while(var2.hasNext()) {
         mgc = (MapleGuildCharacter)var2.next();
         mplew.writeShort(0);
      }

   }

   public static final MapleGuildResponse sendInvite(MapleClient c, String targetName) {
      MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(targetName);
      if (mc == null) {
         return MapleGuildResponse.NOT_IN_CHANNEL;
      } else if (mc.getGuildId() > 0) {
         return MapleGuildResponse.ALREADY_IN_GUILD;
      } else {
         mc.getClient().getSession().writeAndFlush(CWvsContext.GuildPacket.guildInvite(c.getPlayer().getGuildId(), c.getPlayer().getGuild().getName(), c.getPlayer()));
         return null;
      }
   }

   public Collection<MapleGuildCharacter> getMembers() {
      return Collections.unmodifiableCollection(this.members);
   }

   public final boolean isInit() {
      return this.init;
   }

   public boolean hasSkill(int id) {
      return this.guildSkills.containsKey(id);
   }

   public static void setOfflineGuildStatus(int guildid, byte guildrank, int contribution, byte alliancerank, int cid) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, guildContribution = ?, alliancerank = ? WHERE id = ?");
         ps.setInt(1, guildid);
         ps.setInt(2, guildrank);
         ps.setInt(3, contribution);
         ps.setInt(4, alliancerank);
         ps.setInt(5, cid);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var17) {
         System.out.println("SQLException: " + var17.getLocalizedMessage());
         var17.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var16) {
            var16.printStackTrace();
         }

      }

   }

   public int avergeMemberLevel() {
      int mean = 0;
      int size = 0;

      for(Iterator var3 = this.members.iterator(); var3.hasNext(); ++size) {
         MapleGuildCharacter gc = (MapleGuildCharacter)var3.next();
         mean += gc.getLevel();
      }

      return mean != 0 && size != 0 ? mean / size : 0;
   }

   public String getLeaderName() {
      Iterator var1 = this.members.iterator();

      MapleGuildCharacter gc;
      do {
         if (!var1.hasNext()) {
            return "없음";
         }

         gc = (MapleGuildCharacter)var1.next();
      } while(gc.getId() != this.leader);

      return gc.getName();
   }

   public boolean addRequest(MapleGuildCharacter mgc) {
      this.changed_requests = true;
      Iterator toRemove = this.requests.iterator();

      MapleGuildCharacter mgc2;
      do {
         if (!toRemove.hasNext()) {
            this.requests.add(mgc);
            return true;
         }

         mgc2 = (MapleGuildCharacter)toRemove.next();
      } while(mgc2.getId() != mgc.getId());

      return false;
   }

   public void removeRequest(int cid) {
      Iterator toRemove = this.requests.iterator();

      while(toRemove.hasNext()) {
         MapleGuildCharacter mgc = (MapleGuildCharacter)toRemove.next();
         if (mgc.getId() == cid) {
            this.requests.remove(mgc);
            this.changed_requests = true;
            break;
         }
      }

   }

   public MapleGuildCharacter getRequest(int cid) {
      Iterator toRemove = this.requests.iterator();

      MapleGuildCharacter mgc;
      do {
         if (!toRemove.hasNext()) {
            return null;
         }

         mgc = (MapleGuildCharacter)toRemove.next();
      } while(mgc.getId() != cid);

      return mgc;
   }

   public void setNoblessSkillPoint(int point) {
      this.noblessskillpoint = point;
      this.writeToDB(false);
   }

   public int getNoblessSkillPoint() {
      return this.noblessskillpoint;
   }

   public void updateGuildScore(long totDamageToOneMonster) {
      double guildScore = this.guildScore;
      double add = (double)totDamageToOneMonster / 1.0E12D;
      this.guildScore += add;
      if (guildScore != this.guildScore) {
         this.broadcast(CField.updateGuildScore((int)this.guildScore));
      }

      this.setGuildScore(this.guildScore);
   }

   public double getGuildScore() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = ?");
         ps.setInt(1, this.id);
         rs = ps.executeQuery();
         if (rs.next()) {
            this.guildScore = (double)rs.getInt("score");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var13) {
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var12) {
         }

      }

      return this.guildScore;
   }

   public void setGuildScore(double guildScore) {
      this.guildScore = guildScore;
      this.writeToDB(false);
   }

   public final int getFame() {
      return this.fame;
   }

   public final int getBeforeAttance() {
      return this.beforeattance;
   }

   public final int getAfterAttance() {
      return this.afterattance;
   }

   public final void setLevel(int level) {
      this.guildlevel = level;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET level = ? WHERE guildid = ?");
         ps.setInt(1, this.guildlevel);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var17) {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var16) {
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var15) {
         }

      }

   }

   public int getLastResetDay() {
      return this.lastResetDay;
   }

   public void setLastResetDay(int lastResetDay) {
      this.lastResetDay = lastResetDay;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE guilds SET lastresetday = ? WHERE guildid = ?");
         ps.setInt(1, this.lastResetDay);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var17) {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var16) {
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var15) {
         }

      }

   }

   public int getWeekReputation() {
      return this.weekReputation;
   }

   public void setWeekReputation(int weekReputation) {
      this.weekReputation = weekReputation;
   }

   public int compareTo(MapleGuild o) {
      if (this.getGuildScore() < o.getGuildScore()) {
         return 1;
      } else {
         return this.getGuildScore() > o.getGuildScore() ? -1 : 0;
      }
   }

   private static enum BCOp {
      NONE,
      DISBAND,
      EMBELMCHANGE;
   }
}
