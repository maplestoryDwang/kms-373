package tools.wztosql;

import database.DatabaseConnection;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.quest.MapleQuestActionType;
import server.quest.MapleQuestRequirementType;
import tools.Pair;

public class DumpQuests {
   private MapleDataProvider quest;
   protected boolean hadError = false;
   protected boolean update = false;
   protected int id = 0;
   private Connection con = DatabaseConnection.getConnection();

   public DumpQuests(boolean update) throws Exception {
      this.update = update;
      this.quest = MapleDataProviderFactory.getDataProvider(new File("Wz/Quest.wz"));
      if (this.quest == null) {
         this.hadError = true;
      }

   }

   public boolean isHadError() {
      return this.hadError;
   }

   public void dumpQuests() throws Exception {
      if (!this.hadError) {
         PreparedStatement psai = this.con.prepareStatement("INSERT INTO wz_questactitemdata(uniqueid, itemid, count, period, gender, job, jobEx, prop) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
         PreparedStatement psas = this.con.prepareStatement("INSERT INTO wz_questactskilldata(uniqueid, skillid, skillLevel, masterLevel) VALUES (?, ?, ?, ?)");
         PreparedStatement psaq = this.con.prepareStatement("INSERT INTO wz_questactquestdata(uniqueid, quest, state) VALUES (?, ?, ?)");
         PreparedStatement ps = this.con.prepareStatement("INSERT INTO wz_questdata(questid, name, autoStart, autoPreComplete, viewMedalItem, selectedSkillID, blocked, autoAccept, autoComplete, autoCompleteAction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         PreparedStatement psr = this.con.prepareStatement("INSERT INTO wz_questreqdata(questid, type, name, stringStore, intStoresFirst, intStoresSecond) VALUES (?, ?, ?, ?, ?, ?)");
         PreparedStatement psq = this.con.prepareStatement("INSERT INTO wz_questpartydata(questid, rank, mode, property, value) VALUES(?,?,?,?,?)");
         PreparedStatement psa = this.con.prepareStatement("INSERT INTO wz_questactdata(questid, type, name, intStore, applicableJobs, uniqueid) VALUES (?, ?, ?, ?, ?, ?)");

         try {
            this.dumpQuests(psai, psas, psaq, ps, psr, psq, psa);
         } catch (Exception var12) {
            System.out.println(this.id + " quest.");
            var12.printStackTrace();
            this.hadError = true;
         } finally {
            psai.executeBatch();
            psai.close();
            psas.executeBatch();
            psas.close();
            psaq.executeBatch();
            psaq.close();
            psa.executeBatch();
            psa.close();
            psr.executeBatch();
            psr.close();
            psq.executeBatch();
            psq.close();
            ps.executeBatch();
            ps.close();
         }
      }

   }

   public void delete(String sql) throws Exception {
      PreparedStatement ps = this.con.prepareStatement(sql);
      ps.executeUpdate();
      ps.close();
   }

   public boolean doesExist(String sql) throws Exception {
      PreparedStatement ps = this.con.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      boolean ret = rs.next();
      rs.close();
      ps.close();
      return ret;
   }

   public void dumpQuests(PreparedStatement psai, PreparedStatement psas, PreparedStatement psaq, PreparedStatement ps, PreparedStatement psr, PreparedStatement psq, PreparedStatement psa) throws Exception {
      if (!this.update) {
         this.delete("DELETE FROM wz_questdata");
         this.delete("DELETE FROM wz_questactdata");
         this.delete("DELETE FROM wz_questactitemdata");
         this.delete("DELETE FROM wz_questactskilldata");
         this.delete("DELETE FROM wz_questactquestdata");
         this.delete("DELETE FROM wz_questreqdata");
         this.delete("DELETE FROM wz_questpartydata");
         System.out.println("Deleted wz_questdata successfully.");
      }

      MapleData checkz = this.quest.getData("Check.img");
      MapleData actz = this.quest.getData("Act.img");
      MapleData infoz = this.quest.getData("QuestInfo.img");
      MapleData pinfoz = this.quest.getData("PQuest.img");
      System.out.println("Adding into wz_questdata.....");
      int uniqueid = 0;
      Iterator var13 = checkz.getChildren().iterator();

      while(true) {
         MapleData qz;
         do {
            if (!var13.hasNext()) {
               System.out.println("Done wz_questdata...");
               return;
            }

            qz = (MapleData)var13.next();
            this.id = Integer.parseInt(qz.getName());
         } while(this.update && this.doesExist("SELECT * FROM wz_questdata WHERE questid = " + this.id));

         ps.setInt(1, this.id);

         MapleData pinfoData;
         Iterator var17;
         MapleData req;
         Iterator var32;
         MapleData sEntry;
         label293:
         for(int i = 0; i < 2; ++i) {
            pinfoData = qz.getChildByPath(String.valueOf(i));
            StringBuilder applicableJobs;
            Iterator var33;
            if (pinfoData != null) {
               psr.setInt(1, this.id);
               psr.setInt(2, i);
               var17 = pinfoData.getChildren().iterator();

               label236:
               while(true) {
                  do {
                     if (!var17.hasNext()) {
                        break label236;
                     }

                     req = (MapleData)var17.next();
                  } while(MapleQuestRequirementType.getByWZName(req.getName()) == MapleQuestRequirementType.UNDEFINED);

                  psr.setString(3, req.getName());
                  if (req.getName().equals("fieldEnter")) {
                     psr.setString(4, String.valueOf(MapleDataTool.getIntConvert("0", req, 0)));
                  } else if (!req.getName().equals("end") && !req.getName().equals("startscript") && !req.getName().equals("endscript")) {
                     psr.setString(4, String.valueOf(MapleDataTool.getInt(req, 0)));
                  } else {
                     psr.setString(4, MapleDataTool.getString(req, ""));
                  }

                  StringBuilder intStore1 = new StringBuilder();
                  applicableJobs = new StringBuilder();
                  List<Pair<Integer, Integer>> dataStore = new LinkedList();
                  List child;
                  int x;
                  if (req.getName().equals("job")) {
                     child = req.getChildren();

                     for(x = 0; x < child.size(); ++x) {
                        dataStore.add(new Pair(i, MapleDataTool.getInt((MapleData)child.get(x), -1)));
                     }
                  } else {
                     MapleData childdata;
                     if (req.getName().equals("skill")) {
                        child = req.getChildren();

                        for(x = 0; x < child.size(); ++x) {
                           childdata = (MapleData)child.get(x);
                           if (childdata != null) {
                              dataStore.add(new Pair(MapleDataTool.getInt(childdata.getChildByPath("id"), 0), MapleDataTool.getInt(childdata.getChildByPath("acquire"), 0)));
                           }
                        }
                     } else if (req.getName().equals("quest")) {
                        child = req.getChildren();

                        for(x = 0; x < child.size(); ++x) {
                           childdata = (MapleData)child.get(x);
                           if (childdata != null) {
                              dataStore.add(new Pair(MapleDataTool.getInt(childdata.getChildByPath("id"), 0), MapleDataTool.getInt(childdata.getChildByPath("state"), 0)));
                           }
                        }
                     } else if (!req.getName().equals("item") && !req.getName().equals("mob")) {
                        if (req.getName().equals("mbcard")) {
                           child = req.getChildren();

                           for(x = 0; x < child.size(); ++x) {
                              childdata = (MapleData)child.get(x);
                              if (childdata != null) {
                                 dataStore.add(new Pair(MapleDataTool.getInt(childdata.getChildByPath("id"), 0), MapleDataTool.getInt(childdata.getChildByPath("min"), 0)));
                              }
                           }
                        } else if (req.getName().equals("pet")) {
                           child = req.getChildren();

                           for(x = 0; x < child.size(); ++x) {
                              childdata = (MapleData)child.get(x);
                              if (childdata != null) {
                                 dataStore.add(new Pair(i, MapleDataTool.getInt(childdata.getChildByPath("id"), 0)));
                              }
                           }
                        }
                     } else {
                        child = req.getChildren();

                        for(x = 0; x < child.size(); ++x) {
                           childdata = (MapleData)child.get(x);
                           if (childdata != null) {
                              dataStore.add(new Pair(MapleDataTool.getInt(childdata.getChildByPath("id"), 0), MapleDataTool.getInt(childdata.getChildByPath("count"), 0)));
                           }
                        }
                     }
                  }

                  var33 = dataStore.iterator();

                  while(var33.hasNext()) {
                     Pair<Integer, Integer> data = (Pair)var33.next();
                     if (intStore1.length() > 0) {
                        intStore1.append(", ");
                        applicableJobs.append(", ");
                     }

                     intStore1.append(data.getLeft());
                     applicableJobs.append(data.getRight());
                  }

                  psr.setString(5, intStore1.toString());
                  psr.setString(6, applicableJobs.toString());
                  psr.addBatch();
               }
            }

            MapleData actData = actz.getChildByPath(this.id + "/" + i);
            if (actData != null) {
               psa.setInt(1, this.id);
               psa.setInt(2, i);
               Iterator var27 = actData.getChildren().iterator();

               while(true) {
                  MapleData act;
                  do {
                     if (!var27.hasNext()) {
                        continue label293;
                     }

                     act = (MapleData)var27.next();
                  } while(MapleQuestActionType.getByWZName(act.getName()) == MapleQuestActionType.UNDEFINED);

                  psa.setString(3, act.getName());
                  if (act.getName().equals("sp")) {
                     psa.setInt(4, MapleDataTool.getIntConvert("0/sp_value", act, 0));
                  } else {
                     psa.setInt(4, MapleDataTool.getInt(act, 0));
                  }

                  applicableJobs = new StringBuilder();
                  if (!act.getName().equals("sp") && !act.getName().equals("skill")) {
                     if (act.getChildByPath("job") != null) {
                        for(var32 = act.getChildByPath("job").iterator(); var32.hasNext(); applicableJobs.append(MapleDataTool.getInt(sEntry, 0))) {
                           sEntry = (MapleData)var32.next();
                           if (applicableJobs.length() > 0) {
                              applicableJobs.append(", ");
                           }
                        }
                     }
                  } else {
                     MapleData d;
                     for(int index = 0; act.getChildByPath(index + "/job") != null; ++index) {
                        for(var33 = act.getChildByPath(index + "/job").iterator(); var33.hasNext(); applicableJobs.append(MapleDataTool.getInt(d, 0))) {
                           d = (MapleData)var33.next();
                           if (applicableJobs.length() > 0) {
                              applicableJobs.append(", ");
                           }
                        }
                     }
                  }

                  psa.setString(5, applicableJobs.toString());
                  psa.setInt(6, -1);
                  if (act.getName().equals("item")) {
                     ++uniqueid;
                     psa.setInt(6, uniqueid);
                     psai.setInt(1, uniqueid);

                     for(var32 = act.getChildren().iterator(); var32.hasNext(); psai.addBatch()) {
                        sEntry = (MapleData)var32.next();
                        psai.setInt(2, MapleDataTool.getInt("id", sEntry, 0));
                        psai.setInt(3, MapleDataTool.getInt("count", sEntry, 0));
                        psai.setInt(4, MapleDataTool.getInt("period", sEntry, 0));
                        psai.setInt(5, MapleDataTool.getInt("gender", sEntry, 2));
                        psai.setInt(6, MapleDataTool.getInt("job", sEntry, -1));
                        psai.setInt(7, MapleDataTool.getInt("jobEx", sEntry, -1));
                        if (sEntry.getChildByPath("prop") == null) {
                           psai.setInt(8, -2);
                        } else {
                           psai.setInt(8, MapleDataTool.getInt("prop", sEntry, -1));
                        }
                     }
                  } else if (act.getName().equals("skill")) {
                     ++uniqueid;
                     psa.setInt(6, uniqueid);
                     psas.setInt(1, uniqueid);
                     var32 = act.iterator();

                     while(var32.hasNext()) {
                        sEntry = (MapleData)var32.next();
                        psas.setInt(2, MapleDataTool.getInt("id", sEntry, 0));
                        psas.setInt(3, MapleDataTool.getInt("skillLevel", sEntry, 0));
                        psas.setInt(4, MapleDataTool.getInt("masterLevel", sEntry, 0));
                        psas.addBatch();
                     }
                  } else if (act.getName().equals("quest")) {
                     ++uniqueid;
                     psa.setInt(6, uniqueid);
                     psaq.setInt(1, uniqueid);
                     var32 = act.iterator();

                     while(var32.hasNext()) {
                        sEntry = (MapleData)var32.next();
                        psaq.setInt(2, MapleDataTool.getInt("id", sEntry, 0));
                        psaq.setInt(3, MapleDataTool.getInt("state", sEntry, 0));
                        psaq.addBatch();
                     }
                  }

                  psa.addBatch();
               }
            }
         }

         MapleData infoData = infoz.getChildByPath(String.valueOf(this.id));
         if (infoData != null) {
            ps.setString(2, MapleDataTool.getString("name", infoData, ""));
            ps.setInt(3, MapleDataTool.getInt("autoStart", infoData, 0) > 0 ? 1 : 0);
            ps.setInt(4, MapleDataTool.getInt("autoPreComplete", infoData, 0) > 0 ? 1 : 0);
            ps.setInt(5, MapleDataTool.getInt("viewMedalItem", infoData, 0));
            ps.setInt(6, MapleDataTool.getInt("selectedSkillID", infoData, 0));
            ps.setInt(7, MapleDataTool.getInt("blocked", infoData, 0));
            ps.setInt(8, MapleDataTool.getInt("autoAccept", infoData, 0));
            ps.setInt(9, MapleDataTool.getInt("autoComplete", infoData, 0));
            ps.setInt(10, MapleDataTool.getInt("autoCompleteAction", infoData, 0));
         } else {
            ps.setString(2, "");
            ps.setInt(3, 0);
            ps.setInt(4, 0);
            ps.setInt(5, 0);
            ps.setInt(6, 0);
            ps.setInt(7, 0);
            ps.setInt(8, 0);
            ps.setInt(9, 0);
            ps.setInt(10, 0);
         }

         ps.addBatch();
         pinfoData = pinfoz.getChildByPath(String.valueOf(this.id));
         if (pinfoData != null && pinfoData.getChildByPath("rank") != null) {
            psq.setInt(1, this.id);
            var17 = pinfoData.getChildByPath("rank").iterator();

            while(var17.hasNext()) {
               req = (MapleData)var17.next();
               psq.setString(2, req.getName());
               Iterator var29 = req.iterator();

               while(var29.hasNext()) {
                  MapleData c = (MapleData)var29.next();
                  psq.setString(3, c.getName());
                  var32 = c.iterator();

                  while(var32.hasNext()) {
                     sEntry = (MapleData)var32.next();
                     psq.setString(4, sEntry.getName());
                     psq.setInt(5, MapleDataTool.getInt(sEntry, 0));
                     psq.addBatch();
                  }
               }
            }
         }

         System.out.println("Added quest: " + this.id);
      }
   }

   public int currentId() {
      return this.id;
   }

   public static void main(String[] args) {
      try {
         DatabaseConnection.init();
         boolean hadError = false;
         boolean update = false;
         long startTime = System.currentTimeMillis();
         String[] var5 = args;
         int var6 = args.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String file = var5[var7];
            if (file.equalsIgnoreCase("-update")) {
               update = true;
            }
         }

         byte currentQuest = 0;

         try {
            DumpQuests dq = new DumpQuests(update);
            System.out.println("Dumping quests");
            dq.dumpQuests();
            hadError |= dq.isHadError();
            int var16 = dq.currentId();
         } catch (Exception var13) {
            hadError = true;
            var13.printStackTrace();
            System.out.println(currentQuest + " quest.");
         }

         long endTime = System.currentTimeMillis();
         double elapsedSeconds = (double)(endTime - startTime) / 1000.0D;
         int elapsedSecs = (int)elapsedSeconds % 60;
         int elapsedMinutes = (int)(elapsedSeconds / 60.0D);
         String withErrors = "";
         if (hadError) {
            withErrors = " with errors";
         }

         System.out.println("Finished" + withErrors + " in " + elapsedMinutes + " minutes " + elapsedSecs + " seconds");
      } catch (Exception var14) {
         var14.printStackTrace();
      }

   }
}
