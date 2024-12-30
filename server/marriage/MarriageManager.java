package server.marriage;

import client.MapleCharacter;
import client.inventory.Item;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.channel.handler.DueyHandler;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MarriageManager {
   private static MarriageManager instance = null;
   private AtomicInteger runningId;
   private final Map<Integer, MarriageDataEntry> marriages = new HashMap();
   private final List<Integer> toDeleteIds = new ArrayList();
   private final Map<Integer, MarriageEventAgent> eventagents = new HashMap();

   private MarriageManager() {
      this.loadAll();
   }

   public static MarriageManager getInstance() {
      if (instance == null) {
         instance = new MarriageManager();
      }

      return instance;
   }

   private void loadAll() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         this.runningId = new AtomicInteger(0);
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM `wedding_data`");

         MarriageDataEntry data;
         for(rs = ps.executeQuery(); rs.next(); this.marriages.put(rs.getInt("id"), data)) {
            if (rs.getInt("id") > this.runningId.get()) {
               this.runningId.set(rs.getInt("id"));
            }

            data = new MarriageDataEntry(rs.getInt("id"), false);
            data.setGroomId(rs.getInt("groomId"));
            data.setBrideId(rs.getInt("brideId"));
            data.setGroomName(rs.getString("groomName"));
            data.setBrideName(rs.getString("brideName"));
            data.setStatus(rs.getInt("status"));
            data.setWeddingStatus(rs.getInt("weddingStatus"));
            data.setTicketType(MarriageTicketType.getTypeById(rs.getInt("ticketType")));
            data.setEngagementTime(rs.getLong("EngagementTime"));
            data.setMakeReservationTime(rs.getLong("MakeReservationTime"));
            data.setDivorceTimeGroom(rs.getLong("RequestDivorceTimeGroom"));
            data.setDivorceTimeBride(rs.getLong("RequestDivorceTimeBride"));
            PreparedStatement ps2 = null;
            ResultSet rs2 = null;

            try {
               ps2 = con.prepareStatement("SELECT * FROM `wedding_wishlists` WHERE marriageid = ?");
               ps2.setInt(1, data.getMarriageId());
               rs2 = ps2.executeQuery();

               while(rs2.next()) {
                  if (rs2.getInt("gender") == 0) {
                     data.getGroomWishList().add(rs2.getString("string"));
                  } else {
                     data.getBrideWishList().add(rs2.getString("string"));
                  }
               }
            } catch (Exception var86) {
               var86.printStackTrace(System.err);
            } finally {
               if (rs2 != null) {
                  try {
                     rs2.close();
                  } catch (Exception var85) {
                  }
               }

               if (ps2 != null) {
                  try {
                     ps2.close();
                  } catch (Exception var84) {
                  }
               }

            }

            try {
               ps2 = con.prepareStatement("SELECT * FROM `wedding_reserved` WHERE marriageid = ?");
               ps2.setInt(1, data.getMarriageId());
               rs2 = ps2.executeQuery();

               while(rs2.next()) {
                  data.getReservedPeopleList().add(rs2.getInt("chrid"));
               }
            } catch (Exception var88) {
               var88.printStackTrace(System.err);
            } finally {
               if (rs2 != null) {
                  try {
                     rs2.close();
                  } catch (Exception var83) {
                  }
               }

               if (ps2 != null) {
                  try {
                     ps2.close();
                  } catch (Exception var82) {
                  }
               }

            }
         }
      } catch (Exception var90) {
         var90.printStackTrace(System.err);
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (Exception var81) {
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var80) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var79) {
               var79.printStackTrace();
            }
         }

      }

   }

   public void saveAll() {
      System.out.println("Saving Marriage Informations... ");
      Connection con = null;
      PreparedStatement ps = null;
      String query = null;
      String query2 = null;
      String query3 = null;
      Iterator ids;
      if (!this.toDeleteIds.isEmpty()) {
         try {
            con = DatabaseConnection.getConnection();
            query = "DELETE FROM `wedding_data` WHERE ";
            query2 = "DELETE FROM `wedding_reserved` WHERE ";
            query3 = "DELETE FROM `wedding_wishlists` WHERE ";
            ids = this.toDeleteIds.iterator();

            while(ids.hasNext()) {
               Integer id = (Integer)ids.next();
               query = query + "`id` = '" + id + "'";
               query2 = query2 + "`marriageid` = '" + id + "'";
               query3 = query3 + "`marriageid` = '" + id + "'";
               if (ids.hasNext()) {
                  query = query + " OR ";
                  query2 = query2 + " OR ";
                  query3 = query3 + " OR ";
               }
            }

            con.prepareStatement(query);
            con.prepareStatement(query2);
            ps = con.prepareStatement(query3);
            ps.executeUpdate();
            ps.close();
         } catch (Exception var25) {
            var25.printStackTrace(System.err);
         }
      }

      ids = this.marriages.values().iterator();

      while(ids.hasNext()) {
         MarriageDataEntry data = (MarriageDataEntry)ids.next();
         if (data.isNewData()) {
            query = "INSERT INTO `wedding_data` (`groomId`, `brideId`, `groomName`, `brideName`, `status`, `weddingStatus`, `ticketType`, `EngagementTime`, `MakeReservationTime`, `RequestDivorceTimeGroom`, `RequestDivorceTimeBride`, `id`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
         } else {
            query = "UPDATE `wedding_data` SET `groomId` = ?, `brideId` = ?, `groomName` = ?, `brideName` = ?, `status` = ?, `weddingStatus` = ?, `ticketType` = ?, `EngagementTime` = ?, `MakeReservationTime` = ?, `RequestDivorceTimeGroom` = ?, `RequestDivorceTimeBride` = ? WHERE `id` = ?";
         }

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement(query);
            ps.setInt(1, data.getGroomId());
            ps.setInt(2, data.getBrideId());
            ps.setString(3, data.getGroomName());
            ps.setString(4, data.getBrideName());
            ps.setInt(5, data.getStatus());
            ps.setInt(6, data.getWeddingStatus());
            ps.setInt(7, data.getTicketType() == null ? 0 : data.getTicketType().getItemId());
            ps.setLong(8, data.getEngagementTime());
            ps.setLong(9, data.getMakeReservationTime());
            ps.setLong(10, data.getDivorceTimeGroom());
            ps.setLong(11, data.getDivorceTimeBride());
            ps.setInt(12, data.getMarriageId());
            ps.executeUpdate();
            ps.close();
            data.setNewData(false);
            ps = con.prepareStatement("DELETE FROM `wedding_wishlists` WHERE `marriageid` = ?");
            ps.setInt(1, data.getMarriageId());
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO `wedding_wishlists` (marriageid, gender, string) VALUES (?, ?, ?)");
            ps.setInt(1, data.getMarriageId());
            Iterator var8 = data.getGroomWishList().iterator();

            String str;
            while(var8.hasNext()) {
               str = (String)var8.next();
               ps.setInt(2, 0);
               ps.setString(3, str);
               ps.addBatch();
            }

            var8 = data.getBrideWishList().iterator();

            while(var8.hasNext()) {
               str = (String)var8.next();
               ps.setInt(2, 1);
               ps.setString(3, str);
               ps.addBatch();
            }

            ps.executeBatch();
            ps = con.prepareStatement("DELETE FROM `wedding_reserved` WHERE `marriageid` = ?");
            ps.setInt(1, data.getMarriageId());
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO `wedding_reserved` (marriageid, chrid) VALUES (?, ?)");
            ps.setInt(1, data.getMarriageId());
            var8 = data.getReservedPeopleList().iterator();

            while(var8.hasNext()) {
               Integer i = (Integer)var8.next();
               ps.setInt(2, i);
               ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
            this.saveWeddingPresent(data.getGroomPresentList(), data.getGroomId());
            this.saveWeddingPresent(data.getBridePresentList(), data.getBrideId());
         } catch (Exception var23) {
            var23.printStackTrace(System.err);
         } finally {
            if (ps != null) {
               try {
                  ps.close();
               } catch (Exception var22) {
               }
            }

            if (con != null) {
               try {
                  con.close();
               } catch (SQLException var21) {
                  var21.printStackTrace();
               }
            }

         }
      }

   }

   public MarriageDataEntry makeNewMarriage(int groomId) {
      MarriageDataEntry data = new MarriageDataEntry(this.runningId.incrementAndGet(), true);
      this.marriages.put(data.getMarriageId(), data);
      return data;
   }

   public MarriageDataEntry getMarriage(int id) {
      if (this.marriages.containsKey(id)) {
         return (MarriageDataEntry)this.marriages.get(id);
      } else {
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM `wedding_data` WHERE id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
               MarriageDataEntry data = new MarriageDataEntry(rs.getInt("id"), false);
               data.setGroomId(rs.getInt("groomId"));
               data.setBrideId(rs.getInt("brideId"));
               data.setGroomName(rs.getString("groomName"));
               data.setBrideName(rs.getString("brideName"));
               data.setStatus(rs.getInt("status"));
               data.setWeddingStatus(rs.getInt("weddingStatus"));
               data.setTicketType(MarriageTicketType.getTypeById(rs.getInt("ticketType")));
               data.setEngagementTime(rs.getLong("EngagementTime"));
               data.setMakeReservationTime(rs.getLong("MakeReservationTime"));
               data.setDivorceTimeGroom(rs.getLong("RequestDivorceTimeGroom"));
               data.setDivorceTimeBride(rs.getLong("RequestDivorceTimeBride"));
               PreparedStatement ps2 = null;
               ResultSet rs2 = null;

               try {
                  ps2 = con.prepareStatement("SELECT * FROM `wedding_wishlists` WHERE marriageid = ?");
                  ps2.setInt(1, data.getMarriageId());
                  rs2 = ps2.executeQuery();

                  while(rs2.next()) {
                     if (rs2.getInt("gender") == 0) {
                        data.getGroomWishList().add(rs2.getString("string"));
                     } else {
                        data.getBrideWishList().add(rs2.getString("string"));
                     }
                  }
               } catch (Exception var89) {
                  var89.printStackTrace(System.err);
               } finally {
                  if (rs2 != null) {
                     try {
                        rs2.close();
                     } catch (Exception var86) {
                     }
                  }

                  if (ps2 != null) {
                     try {
                        ps2.close();
                     } catch (Exception var85) {
                     }
                  }

               }

               try {
                  ps2 = con.prepareStatement("SELECT * FROM `wedding_reserved` WHERE marriageid = ?");
                  ps2.setInt(1, data.getMarriageId());
                  rs2 = ps2.executeQuery();

                  while(rs2.next()) {
                     data.getReservedPeopleList().add(rs2.getInt("chrid"));
                  }
               } catch (Exception var87) {
                  var87.printStackTrace(System.err);
               } finally {
                  if (rs2 != null) {
                     try {
                        rs2.close();
                     } catch (Exception var84) {
                     }
                  }

                  if (ps2 != null) {
                     try {
                        ps2.close();
                     } catch (Exception var83) {
                     }
                  }

               }

               this.marriages.put(id, data);
            }
         } catch (Exception var91) {
            var91.printStackTrace(System.err);
         } finally {
            if (rs != null) {
               try {
                  rs.close();
               } catch (Exception var82) {
               }
            }

            if (ps != null) {
               try {
                  ps.close();
               } catch (Exception var81) {
               }
            }

            if (con != null) {
               try {
                  con.close();
               } catch (SQLException var80) {
                  var80.printStackTrace();
               }
            }

         }

         return (MarriageDataEntry)this.marriages.get(id);
      }
   }

   public void deleteMarriage(int id) {
      MarriageDataEntry entry = (MarriageDataEntry)this.marriages.remove(id);
      if (entry.getStatus() == 2) {
         processDivorce(entry.getBrideId());
         processDivorce(entry.getGroomId());
      }

      this.toDeleteIds.add(id);
   }

   private static void processDivorce(int cid) {
      int chan = World.Find.findChannel(cid);
      if (chan >= 0) {
         MapleCharacter chr = ChannelServer.getInstance(chan).getPlayerStorage().getCharacterById(cid);
         if (chr != null) {
            int i;
            for(i = 1112300; i <= 1112311; ++i) {
               chr.removeAllEquip(i, true);
            }

            for(i = 4210000; i <= 4210011; ++i) {
               chr.removeAll(i, true);
            }

            chr.removeAllEquip(1112744, true);
            chr.setMarriageId(0);
            chr.dropMessage(1, "이혼이 성립되었습니다. 리붓 이전까지 결혼이 제한됩니다.");
         }
      }

   }

   public MarriageEventAgent getEventAgent(int channel) {
      if (!this.eventagents.containsKey(channel)) {
         this.eventagents.put(channel, new MarriageEventAgent(channel));
      }

      return (MarriageEventAgent)this.eventagents.get(channel);
   }

   private void saveWeddingPresent(List<Item> toSaveList, int cid) {
      if (!toSaveList.isEmpty()) {
         Iterator var3 = toSaveList.iterator();

         while(var3.hasNext()) {
            Item item = (Item)var3.next();
            DueyHandler.addItemToDB(item, 0, "안젤리크", cid, false, "미처 받지 못하신 결혼 선물을 택배로 보내드립니다.", 0, true);
         }
      }

   }
}
