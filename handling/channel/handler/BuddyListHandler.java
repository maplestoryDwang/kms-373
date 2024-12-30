package handling.channel.handler;

import client.BuddyList;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class BuddyListHandler {
   private static final void nextPendingRequest(MapleClient c) {
      CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
      if (pendingBuddyRequest != null) {
         c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.requestBuddylistAdd(pendingBuddyRequest.getId(), pendingBuddyRequest.getAccId(), pendingBuddyRequest.getName(), pendingBuddyRequest.getLevel(), pendingBuddyRequest.getJob(), c, pendingBuddyRequest.getGroupName(), pendingBuddyRequest.getMemo()));
      }

   }

   private static final BuddyListHandler.CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(String name, String groupname, String memo) throws SQLException {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      BuddyListHandler.CharacterIdNameBuddyCapacity ret = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM characters WHERE name LIKE ?");
         ps.setString(1, name);
         rs = ps.executeQuery();
         if (rs.next()) {
            ret = new BuddyListHandler.CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getInt("accountid"), rs.getString("name"), rs.getString("name"), rs.getInt("level"), rs.getInt("job"), rs.getInt("buddyCapacity"), groupname, memo);
         }

         rs.close();
         ps.close();
         con.close();
      } catch (Exception var19) {
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
         } catch (SQLException var18) {
            var18.printStackTrace();
         }
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
         } catch (SQLException var17) {
            var17.printStackTrace();
         }

      }

      return ret;
   }

   public static final void BuddyOperation(LittleEndianAccessor slea, MapleClient c) {
      int mode = slea.readByte();
      BuddyList buddylist = c.getPlayer().getBuddylist();
      String groupName;
      String memo;
      if (mode == 1) {
         String addName = slea.readMapleAsciiString();
         int accid = MapleCharacterUtil.getAccByName(addName);
         groupName = slea.readMapleAsciiString();
         memo = slea.readMapleAsciiString();
         byte accountBuddyCheck = slea.readByte();
         String nickName = "";
         if (accountBuddyCheck == 1) {
            nickName = slea.readMapleAsciiString();
         }

         BuddylistEntry ble = buddylist.get(accid);
         if (addName.length() > 13 || groupName.length() > 16 || nickName.length() > 13 || memo.length() > 260) {
            return;
         }

         if (ble != null && !ble.isVisible()) {
            c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "이미 친구로 등록되어 있습니다."));
            return;
         }

         if (buddylist.isFull()) {
            c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "친구리스트가 꽉 찼습니다."));
            return;
         }

         if (accid == c.getAccID()) {
            c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "자기계정에 있는 캐릭터는 친구추가 하실 수 없습니다."));
            return;
         }

         if (accountBuddyCheck == 0) {
            c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "현재 이 기능은 사용하실 수 없습니다.\r\n아래 계정 통합 체크를 해주세요."));
            return;
         }

         try {
            BuddyListHandler.CharacterIdNameBuddyCapacity charWithId = null;
            MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterByName(addName);
            int channel;
            if (otherChar != null) {
               channel = c.getChannel();
               if (!otherChar.isGM() || c.getPlayer().isGM()) {
                  charWithId = new BuddyListHandler.CharacterIdNameBuddyCapacity(otherChar.getId(), otherChar.getAccountID(), otherChar.getName(), otherChar.getName(), otherChar.getLevel(), otherChar.getJob(), otherChar.getBuddylist().getCapacity(), groupName, memo);
               }
            } else {
               channel = World.Find.findChannel(addName);
               charWithId = getCharacterIdAndNameFromDatabase(addName, groupName, memo);
            }

            if (charWithId != null) {
               BuddyList.BuddyAddResult buddyAddResult = null;
               if (channel != -1) {
                  buddyAddResult = World.Buddy.requestBuddyAdd(addName, c.getAccID(), c.getChannel(), c.getPlayer().getId(), c.getPlayer().getName(), c.getPlayer().getLevel(), c.getPlayer().getJob(), groupName, memo);
               } else {
                  Connection con = null;
                  PreparedStatement ps = null;
                  ResultSet rs = null;

                  try {
                     con = DatabaseConnection.getConnection();
                     ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE accid = ? AND pending = 0");
                     ps.setInt(1, charWithId.getAccId());
                     rs = ps.executeQuery();
                     if (!rs.next()) {
                        ps.close();
                        rs.close();
                        throw new RuntimeException("Result set expected");
                     }

                     int count = rs.getInt("buddyCount");
                     if (count >= charWithId.getBuddyCapacity()) {
                        buddyAddResult = BuddyList.BuddyAddResult.BUDDYLIST_FULL;
                     }

                     rs.close();
                     ps.close();
                     ps = con.prepareStatement("SELECT pending FROM buddies WHERE accid = ? AND buddyaccid = ?");
                     ps.setInt(1, charWithId.getAccId());
                     ps.setInt(2, c.getAccID());
                     rs = ps.executeQuery();
                     if (rs.next()) {
                        buddyAddResult = BuddyList.BuddyAddResult.ALREADY_ON_LIST;
                     }

                     rs.close();
                     ps.close();
                     con.close();
                  } catch (Exception var54) {
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
                     } catch (SQLException var53) {
                        var53.printStackTrace();
                     }
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
                     } catch (SQLException var52) {
                        var52.printStackTrace();
                     }

                  }
               }

               if (buddyAddResult == BuddyList.BuddyAddResult.BUDDYLIST_FULL) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "상대 친구목록이 꽉 찼습니다."));
                  return;
               }

               int displayChannel = -1;
               int otherCid = charWithId.getId();
               if (buddyAddResult == BuddyList.BuddyAddResult.ALREADY_ON_LIST) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "이미 대상의 친구목록에 캐릭터가 있습니다."));
                  return;
               }

               if (buddyAddResult != BuddyList.BuddyAddResult.ALREADY_ON_LIST && channel == -1) {
                  Connection con = null;
                  PreparedStatement ps = null;
                  Object rs = null;

                  try {
                     con = DatabaseConnection.getConnection();
                     ps = con.prepareStatement("INSERT INTO buddies (`accid`, `buddyaccid`, `groupname`, `pending`, `memo`) VALUES (?, ?, ?, 1, ?)");
                     ps.setInt(1, charWithId.getAccId());
                     ps.setInt(2, c.getAccID());
                     ps.setString(3, groupName);
                     ps.setString(4, memo == null ? "" : memo);
                     ps.executeUpdate();
                     ps.close();
                     con.close();
                  } catch (Exception var50) {
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
                     } catch (SQLException var49) {
                        var49.printStackTrace();
                     }
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
                     } catch (SQLException var48) {
                        var48.printStackTrace();
                     }

                  }
               }

               buddylist.put(new BuddylistEntry(charWithId.getName(), charWithId.getName(), accid, otherCid, groupName, displayChannel, true, charWithId.getLevel(), charWithId.getJob(), memo));
               c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.buddyAddMessage(addName));
               c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.updateBuddylist(buddylist.getBuddies(), ble, (byte)21));
            } else {
               c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "대상을 발견하지 못했습니다."));
            }
         } catch (SQLException var56) {
            var56.printStackTrace();
         }
      } else {
         int accId;
         String otherName;
         if (mode != 2 && mode != 3) {
            if (mode == 4) {
               accId = slea.readInt();
               if (buddylist.contains(accId)) {
                  buddylist.remove(accId);
                  c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.deleteBuddy(accId));
               }

               nextPendingRequest(c);
            } else if (mode == 5) {
               accId = slea.readInt();
               if (buddylist.contains(accId)) {
                  buddylist.remove(accId);
                  c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.deleteBuddy(accId));
               }

               nextPendingRequest(c);
            } else if (mode != 6 && mode != 7) {
               if (mode == 10) {
                  if (c.getPlayer().getMeso() >= 50000L && c.getPlayer().getBuddyCapacity() < 100) {
                     c.getPlayer().setBuddyCapacity((byte)(c.getPlayer().getBuddyCapacity() + 5));
                     c.getPlayer().gainMeso(-50000L, false);
                  } else {
                     c.getPlayer().dropMessage(1, "메소가 부족하거나 이미 친구 목록이 최대입니다.");
                  }

                  c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.updateBuddyCapacity(c.getPlayer().getBuddyCapacity()));
               } else if (mode == 11) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "현재 이 기능은 사용하실 수 없습니다."));
               } else if (mode == 12) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "현재 이 기능은 사용하실 수 없습니다."));
               } else if (mode == 13) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "현재 이 기능은 사용하실 수 없습니다."));
               } else if (mode == 14) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "현재 이 기능은 사용하실 수 없습니다."));
                  c.getPlayer().dropMessage(5, "오프라인 상태로 변경되었습니다.");
               }
            } else {
               accId = slea.readInt();
               otherName = "";
               int ch = World.Find.findAccChannel(accId);
               if (ch > 0) {
                  MapleCharacter otherChar = ChannelServer.getInstance(ch).getPlayerStorage().getClientById(accId).getPlayer();
                  if (buddylist.containsVisible(accId) && otherChar != null) {
                     notifyRemoteChannel(c, otherChar.getClient().getChannel(), otherChar.getId(), BuddyList.BuddyOperation.DELETED, otherName);
                  }

                  if (otherChar != null) {
                     otherChar.getClient().getSession().writeAndFlush(CWvsContext.BuddylistPacket.buddyDeclineMessage(c.getPlayer().getName()));
                  }

                  buddylist.remove(accId);
                  c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.deleteBuddy(accId));
                  nextPendingRequest(c);
               }
            }
         } else {
            accId = slea.readInt();
            if (!buddylist.isFull()) {
               otherName = null;
               groupName = "그룹 미지정";
               memo = "";
               int otherLevel = 0;
               int otherJob = 0;
               int ch = World.Find.findAccChannel(accId);
               if (ch > 0) {
                  MapleCharacter otherChar = ChannelServer.getInstance(ch).getPlayerStorage().getClientById(accId).getPlayer();
                  if (otherChar == null) {
                     Iterator var65 = c.getPlayer().getBuddylist().getPendingRequests().iterator();

                     while(var65.hasNext()) {
                        CharacterNameAndId ca = (CharacterNameAndId)var65.next();
                        if (ca.getAccId() == accId) {
                           otherName = ca.getName();
                           otherLevel = ca.getLevel();
                           otherJob = ca.getJob();
                           memo = ca.getMemo();
                           break;
                        }
                     }
                  } else {
                     otherName = otherChar.getName();
                     otherLevel = otherChar.getLevel();
                     otherJob = otherChar.getJob();
                  }

                  if (otherName != null) {
                     BuddylistEntry ble = new BuddylistEntry(otherName, otherName, accId, otherChar.getId(), groupName, otherChar.getClient().getChannel(), true, otherLevel, otherJob, memo);
                     buddylist.put(ble);
                     c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.updateBuddylist(buddylist.getBuddies(), ble, (byte)21));
                     notifyRemoteChannel(c, otherChar.getClient().getChannel(), otherChar.getId(), BuddyList.BuddyOperation.ADDED, memo);
                  }
               }
            }

            nextPendingRequest(c);
         }
      }

   }

   private static final void notifyRemoteChannel(MapleClient c, int remoteChannel, int otherCid, BuddyList.BuddyOperation operation, String memo) {
      MapleCharacter player = c.getPlayer();
      if (remoteChannel > 0) {
         World.Buddy.buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation, player.getLevel(), player.getJob(), c.getAccID(), memo);
      }

   }

   private static final class CharacterIdNameBuddyCapacity extends CharacterNameAndId {
      private int buddyCapacity;

      public CharacterIdNameBuddyCapacity(int id, int accId, String name, String repName, int level, int job, int buddyCapacity, String groupname, String memo) {
         super(id, accId, name, repName, level, job, groupname, memo);
         this.buddyCapacity = buddyCapacity;
      }

      public int getBuddyCapacity() {
         return this.buddyCapacity;
      }
   }
}
