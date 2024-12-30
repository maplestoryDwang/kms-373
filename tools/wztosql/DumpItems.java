package tools.wztosql;

import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.MapleDataType;

public class DumpItems {
   private final MapleDataProvider item;
   private final MapleDataProvider string = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
   private final MapleDataProvider character;
   protected final MapleData cashStringData;
   protected final MapleData consumeStringData;
   protected final MapleData eqpStringData;
   protected final MapleData etcStringData;
   protected final MapleData insStringData;
   protected final MapleData petStringData;
   protected final Set<Integer> doneIds;
   protected boolean hadError;
   protected boolean update;
   protected int id;
   private final Connection con;
   private final List<String> subCon;
   private final List<String> subMain;

   public DumpItems(boolean update) throws Exception {
      this.cashStringData = this.string.getData("Cash.img");
      this.consumeStringData = this.string.getData("Consume.img");
      this.eqpStringData = this.string.getData("Eqp.img");
      this.etcStringData = this.string.getData("Etc.img");
      this.insStringData = this.string.getData("Ins.img");
      this.petStringData = this.string.getData("Pet.img");
      this.doneIds = new LinkedHashSet();
      this.hadError = false;
      this.update = false;
      this.id = 0;
      this.con = DatabaseConnection.getConnection();
      this.subCon = new LinkedList();
      this.subMain = new LinkedList();
      this.update = update;
      this.item = MapleDataProviderFactory.getDataProvider(new File("wz/Item.wz"));
      this.character = MapleDataProviderFactory.getDataProvider(new File("wz/Character.wz"));
      if (this.item == null || this.string == null || this.character == null) {
         this.hadError = true;
      }

   }

   public boolean isHadError() {
      return this.hadError;
   }

   public void dumpItems() throws Exception {
      System.setProperty("wz", "wz");
      if (!this.hadError) {
         PreparedStatement psa = this.con.prepareStatement("INSERT INTO wz_itemadddata(itemid, `key`, `subKey`, `value`) VALUES (?, ?, ?, ?)");
         PreparedStatement psr = this.con.prepareStatement("INSERT INTO wz_itemrewarddata(itemid, item, prob, quantity, period, worldMsg, effect) VALUES (?, ?, ?, ?, ?, ?, ?)");
         PreparedStatement ps = this.con.prepareStatement("INSERT INTO wz_itemdata(itemid, name, msg, `desc`, slotMax, price, wholePrice, stateChange, flags, karma, meso, itemMakeLevel, questId, scrollReqs, consumeItem, totalprob, incSkill, replaceId, replaceMsg, `create`, afterImage, `forceUpgrade`, `chairType`, `nickSkill`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         PreparedStatement pse = this.con.prepareStatement("INSERT INTO wz_itemequipdata(itemid, itemLevel, `key`, `value`) VALUES (?, ?, ?, ?)");

         try {
            this.dumpItems(psa, psr, ps, pse);
         } catch (Exception var9) {
            var9.printStackTrace();
            System.out.println(this.id + " quest.");
            this.hadError = true;
         } finally {
            psr.executeBatch();
            psr.close();
            psa.executeBatch();
            psa.close();
            pse.executeBatch();
            pse.close();
            ps.executeBatch();
            ps.close();
         }
      }

   }

   public void delete(String sql) throws Exception {
      PreparedStatement ps = this.con.prepareStatement(sql);

      try {
         ps.executeUpdate();
      } catch (Throwable var6) {
         if (ps != null) {
            try {
               ps.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (ps != null) {
         ps.close();
      }

   }

   public boolean doesExist(String sql) throws Exception {
      PreparedStatement ps = this.con.prepareStatement(sql);

      boolean ret;
      try {
         ResultSet rs = ps.executeQuery();

         try {
            ret = rs.next();
         } catch (Throwable var9) {
            if (rs != null) {
               try {
                  rs.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (rs != null) {
            rs.close();
         }
      } catch (Throwable var10) {
         if (ps != null) {
            try {
               ps.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }
         }

         throw var10;
      }

      if (ps != null) {
         ps.close();
      }

      return ret;
   }

   public void dumpItems(MapleDataProvider d, PreparedStatement psa, PreparedStatement psr, PreparedStatement ps, PreparedStatement pse, boolean charz) throws Exception {
      Iterator var7 = d.getRoot().getSubdirectories().iterator();

      label55:
      while(true) {
         MapleDataDirectoryEntry topDir;
         do {
            do {
               do {
                  do {
                     if (!var7.hasNext()) {
                        return;
                     }

                     topDir = (MapleDataDirectoryEntry)var7.next();
                  } while(topDir.getName().equalsIgnoreCase("Special"));
               } while(topDir.getName().equalsIgnoreCase("Hair"));
            } while(topDir.getName().equalsIgnoreCase("Face"));
         } while(topDir.getName().equalsIgnoreCase("Afterimage"));

         Iterator var9 = topDir.getFiles().iterator();

         while(true) {
            while(true) {
               if (!var9.hasNext()) {
                  continue label55;
               }

               MapleDataFileEntry ifile = (MapleDataFileEntry)var9.next();
               String var10001 = topDir.getName();
               MapleData iz = d.getData(var10001 + "/" + ifile.getName());
               if (!charz && !topDir.getName().equalsIgnoreCase("Pet")) {
                  Iterator var12 = iz.iterator();

                  while(var12.hasNext()) {
                     MapleData itemData = (MapleData)var12.next();
                     this.dumpItem(psa, psr, ps, pse, itemData);
                  }
               } else {
                  this.dumpItem(psa, psr, ps, pse, iz);
               }
            }
         }
      }
   }

   public void dumpItem(PreparedStatement psa, PreparedStatement psr, PreparedStatement ps, PreparedStatement pse, MapleData iz) throws Exception {
      try {
         if (iz.getName().endsWith(".img")) {
            this.id = Integer.parseInt(iz.getName().substring(0, iz.getName().length() - 4));
         } else {
            this.id = Integer.parseInt(iz.getName());
         }
      } catch (NumberFormatException var37) {
         return;
      }

      if (!this.doneIds.contains(this.id) && GameConstants.getInventoryType(this.id) != MapleInventoryType.UNDEFINED) {
         this.doneIds.add(this.id);
         if (!this.update || !this.doesExist("SELECT * FROM wz_itemdata WHERE itemid = " + this.id)) {
            ps.setInt(1, this.id);
            MapleData stringData = this.getStringData(this.id);
            if (stringData == null) {
               ps.setString(2, "");
               ps.setString(3, "");
               ps.setString(4, "");
            } else {
               ps.setString(2, MapleDataTool.getString("name", stringData, ""));
               ps.setString(3, MapleDataTool.getString("msg", stringData, ""));
               ps.setString(4, MapleDataTool.getString("desc", stringData, ""));
            }

            MapleData smEntry = iz.getChildByPath("info/slotMax");
            short ret;
            if (smEntry == null) {
               if (GameConstants.getInventoryType(this.id) == MapleInventoryType.EQUIP) {
                  ret = 1;
               } else {
                  ret = 100;
               }
            } else {
               ret = (short)MapleDataTool.getIntConvert(smEntry, -1);
            }

            ps.setInt(5, ret);
            MapleData pData = iz.getChildByPath("info/unitPrice");
            double d;
            if (pData != null) {
               try {
                  d = MapleDataTool.getDouble(pData);
               } catch (Exception var36) {
                  d = (double)MapleDataTool.getIntConvert(pData, -1);
               }
            } else {
               pData = iz.getChildByPath("info/price");
               if (pData == null) {
                  d = -1.0D;
               } else {
                  d = (double)MapleDataTool.getIntConvert(pData, -1);
               }
            }

            if (this.id == 2070019 || this.id == 2330007) {
               d = 1.0D;
            }

            ps.setString(6, String.valueOf(d));
            ps.setInt(7, MapleDataTool.getIntConvert("info/price", iz, -1));
            ps.setInt(8, MapleDataTool.getIntConvert("info/stateChangeItem", iz, 0));
            int flags = MapleDataTool.getIntConvert("info/bagType", iz, 0);
            if (MapleDataTool.getIntConvert("info/notSale", iz, 0) > 0) {
               flags |= 16;
            }

            if (MapleDataTool.getIntConvert("info/expireOnLogout", iz, 0) > 0) {
               flags |= 32;
            }

            if (MapleDataTool.getIntConvert("info/pickUpBlock", iz, 0) > 0) {
               flags |= 64;
            }

            if (MapleDataTool.getIntConvert("info/only", iz, 0) > 0) {
               flags |= 128;
            }

            if (MapleDataTool.getIntConvert("info/accountSharable", iz, 0) > 0) {
               flags |= 256;
            }

            if (MapleDataTool.getIntConvert("info/quest", iz, 0) > 0) {
               flags |= 512;
            }

            if (this.id != 4310008 && MapleDataTool.getIntConvert("info/tradeBlock", iz, 0) > 0) {
               flags |= 1024;
            }

            if (MapleDataTool.getIntConvert("info/accountShareTag", iz, 0) > 0) {
               flags |= 2048;
            }

            if (MapleDataTool.getIntConvert("info/mobHP", iz, 0) > 0 && MapleDataTool.getIntConvert("info/mobHP", iz, 0) < 100) {
               flags |= 4096;
            }

            ps.setInt(9, flags);
            ps.setInt(10, MapleDataTool.getIntConvert("info/tradeAvailable", iz, 0));
            ps.setInt(11, MapleDataTool.getIntConvert("info/meso", iz, 0));
            ps.setInt(12, MapleDataTool.getIntConvert("info/lv", iz, 0));
            ps.setInt(13, MapleDataTool.getIntConvert("info/questId", iz, 0));
            int totalprob = 0;
            StringBuilder scrollReqs = new StringBuilder();
            StringBuilder consumeItem = new StringBuilder();
            StringBuilder incSkill = new StringBuilder();
            MapleData dat = iz.getChildByPath("req");
            Iterator var18;
            MapleData req;
            if (dat != null) {
               for(var18 = dat.iterator(); var18.hasNext(); scrollReqs.append(MapleDataTool.getIntConvert(req, 0))) {
                  req = (MapleData)var18.next();
                  if (scrollReqs.length() > 0) {
                     scrollReqs.append(",");
                  }
               }
            }

            dat = iz.getChildByPath("consumeItem");
            if (dat != null) {
               for(var18 = dat.iterator(); var18.hasNext(); consumeItem.append(MapleDataTool.getIntConvert(req, 0))) {
                  req = (MapleData)var18.next();
                  if (consumeItem.length() > 0) {
                     consumeItem.append(",");
                  }
               }
            }

            ps.setString(14, scrollReqs.toString());
            ps.setString(15, consumeItem.toString());
            Map<Integer, Map<String, Integer>> equipStats = new HashMap();
            equipStats.put(-1, new HashMap());
            dat = iz.getChildByPath("mob");
            MapleData info;
            Iterator var39;
            if (dat != null) {
               var39 = dat.iterator();

               while(var39.hasNext()) {
                  info = (MapleData)var39.next();
                  ((Map)equipStats.get(-1)).put("mob" + MapleDataTool.getIntConvert("id", info, 0), MapleDataTool.getIntConvert("prob", info, 0));
               }
            }

            dat = iz.getChildByPath("info/level/case");
            MapleData incs;
            MapleData mapleData;
            int dd;
            if (dat != null) {
               var39 = dat.iterator();

               label400:
               while(var39.hasNext()) {
                  info = (MapleData)var39.next();
                  Iterator var21 = info.iterator();

                  while(true) {
                     do {
                        do {
                           if (!var21.hasNext()) {
                              continue label400;
                           }

                           incs = (MapleData)var21.next();
                        } while(incs.getName().length() != 1);
                     } while(incs.getChildByPath("Skill") == null);

                     Iterator var23 = incs.getChildByPath("Skill").iterator();

                     while(var23.hasNext()) {
                        mapleData = (MapleData)var23.next();
                        dd = MapleDataTool.getIntConvert("id", mapleData, 0);
                        if (dd != 0) {
                           if (incSkill.length() > 0) {
                              incSkill.append(",");
                           }

                           incSkill.append(dd);
                        }
                     }
                  }
               }
            }

            dat = iz.getChildByPath("info/level/info");
            int lv;
            Iterator var47;
            if (dat != null) {
               var39 = dat.iterator();

               label375:
               while(true) {
                  do {
                     if (!var39.hasNext()) {
                        break label375;
                     }

                     info = (MapleData)var39.next();
                  } while(MapleDataTool.getIntConvert("exp", info, 0) == 0);

                  lv = Integer.parseInt(info.getName());
                  if (equipStats.get(lv) == null) {
                     equipStats.put(lv, new HashMap());
                  }

                  var47 = info.iterator();

                  while(var47.hasNext()) {
                     MapleData data = (MapleData)var47.next();
                     if (data.getName().length() > 3 && data.getType() != MapleDataType.CANVAS) {
                        ((Map)equipStats.get(lv)).put(data.getName().substring(3), MapleDataTool.getIntConvert(data, 0));
                     }
                  }
               }
            }

            dat = iz.getChildByPath("info");
            Iterator var41;
            MapleData mapleData;
            String stat;
            if (dat != null) {
               ps.setString(21, MapleDataTool.getString("afterImage", dat, ""));
               Map<String, Integer> rett = (Map)equipStats.get(-1);
               var41 = dat.getChildren().iterator();

               int gg;
               while(var41.hasNext()) {
                  mapleData = (MapleData)var41.next();
                  if (mapleData.getName().startsWith("inc")) {
                     gg = MapleDataTool.getIntConvert(mapleData, 0);
                     if (gg != 0) {
                        rett.put(mapleData.getName().substring(3), gg);
                     }
                  }
               }

               String[] var44 = GameConstants.stats;
               lv = var44.length;

               for(gg = 0; gg < lv; ++gg) {
                  stat = var44[gg];
                  mapleData = dat.getChildByPath(stat);
                  if (stat.equals("canLevel")) {
                     if (dat.getChildByPath("level") != null) {
                        rett.put(stat, 1);
                     }
                  } else if (mapleData != null) {
                     if (stat.equals("skill")) {
                        for(dd = 0; dd < mapleData.getChildren().size(); ++dd) {
                           rett.put("skillid" + dd, MapleDataTool.getIntConvert(Integer.toString(dd), mapleData, 0));
                        }
                     } else {
                        dd = MapleDataTool.getIntConvert(mapleData, 0);
                        if (dd != 0) {
                           rett.put(stat, dd);
                        }
                     }
                  }
               }
            } else {
               ps.setString(21, "");
            }

            ps.setInt(22, MapleDataTool.getIntConvert("info/forceUpgrade", iz, 0));
            String a = MapleDataTool.getString("info/customChair/type", iz, "");
            ps.setString(23, a);
            ps.setInt(24, MapleDataTool.getIntConvert("info/nickSkill", iz, 0));
            pse.setInt(1, this.id);
            var41 = equipStats.entrySet().iterator();

            while(var41.hasNext()) {
               Entry<Integer, Map<String, Integer>> stats = (Entry)var41.next();
               pse.setInt(2, (Integer)stats.getKey());
               var47 = ((Map)stats.getValue()).entrySet().iterator();

               while(var47.hasNext()) {
                  Entry<String, Integer> stat = (Entry)var47.next();
                  pse.setString(3, (String)stat.getKey());
                  pse.setLong(4, (long)(Integer)stat.getValue());
                  pse.addBatch();
               }
            }

            dat = iz.getChildByPath("info/addition");
            if (dat != null) {
               psa.setInt(1, this.id);
               var41 = dat.getChildren().iterator();

               label325:
               while(true) {
                  label289:
                  while(true) {
                     if (!var41.hasNext()) {
                        break label325;
                     }

                     mapleData = (MapleData)var41.next();
                     incs = null;
                     stat = mapleData.getName();
                     byte var52 = -1;
                     switch(stat.hashCode()) {
                     case -1897135862:
                        if (stat.equals("statinc")) {
                           var52 = 0;
                        }
                        break;
                     case -1068860032:
                        if (stat.equals("mobdie")) {
                           var52 = 3;
                        }
                        break;
                     case -290813612:
                        if (stat.equals("elemBoost")) {
                           var52 = 6;
                        }
                        break;
                     case -261260940:
                        if (stat.equals("elemboost")) {
                           var52 = 5;
                        }
                        break;
                     case 3029869:
                        if (stat.equals("boss")) {
                           var52 = 8;
                        }
                        break;
                     case 109496913:
                        if (stat.equals("skill")) {
                           var52 = 2;
                        }
                        break;
                     case 376267774:
                        if (stat.equals("mobcategory")) {
                           var52 = 7;
                        }
                        break;
                     case 708288539:
                        if (stat.equals("hpmpchange")) {
                           var52 = 4;
                        }
                        break;
                     case 1952151455:
                        if (stat.equals("critical")) {
                           var52 = 1;
                        }
                     }

                     switch(var52) {
                     case 0:
                     case 1:
                     case 2:
                     case 3:
                     case 4:
                     case 5:
                     case 6:
                     case 7:
                     case 8:
                        Iterator var53 = mapleData.getChildren().iterator();

                        while(true) {
                           label297:
                           while(true) {
                              if (!var53.hasNext()) {
                                 continue label289;
                              }

                              MapleData subKey = (MapleData)var53.next();
                              if (subKey.getName().equals("con")) {
                                 Iterator var28 = subKey.getChildren().iterator();

                                 while(true) {
                                    while(true) {
                                       if (!var28.hasNext()) {
                                          continue label297;
                                       }

                                       MapleData conK = (MapleData)var28.next();
                                       String var31 = conK.getName();
                                       byte var32 = -1;
                                       switch(var31.hashCode()) {
                                       case 105405:
                                          if (var31.equals("job")) {
                                             var32 = 0;
                                          }
                                          break;
                                       case 1226831624:
                                          if (var31.equals("weekDay")) {
                                             var32 = 1;
                                          }
                                       }

                                       switch(var32) {
                                       case 0:
                                          StringBuilder sbbb = new StringBuilder();
                                          if (conK.getData() != null) {
                                             if (Integer.valueOf(conK.getData().toString()) != null) {
                                                sbbb.append(conK.getData().toString());
                                             }
                                          } else {
                                             Iterator var33 = conK.getChildren().iterator();

                                             while(var33.hasNext()) {
                                                MapleData ids = (MapleData)var33.next();
                                                if (Integer.valueOf(ids.getData().toString()) != null) {
                                                   sbbb.append(ids.getData().toString());
                                                   sbbb.append(",");
                                                }
                                             }

                                             sbbb.deleteCharAt(sbbb.length() - 1);
                                          }

                                          psa.setString(2, mapleData.getName().equals("elemBoost") ? "elemboost" : mapleData.getName());
                                          psa.setString(3, "con:job");
                                          psa.setString(4, sbbb.toString());
                                          psa.addBatch();
                                       case 1:
                                          break;
                                       default:
                                          psa.setString(2, mapleData.getName().equals("elemBoost") ? "elemboost" : mapleData.getName());
                                          psa.setString(3, "con:" + conK.getName());
                                          Integer integer = Integer.valueOf(conK.getData().toString());
                                          if (integer != null) {
                                             psa.setString(4, conK.getData().toString());
                                          }

                                          psa.addBatch();
                                       }
                                    }
                                 }
                              } else {
                                 psa.setString(2, mapleData.getName().equals("elemBoost") ? "elemboost" : mapleData.getName());
                                 psa.setString(3, subKey.getName());

                                 Integer data;
                                 try {
                                    data = Integer.parseInt(subKey.getData().toString());
                                 } catch (Exception var35) {
                                    data = 0;
                                 }

                                 if (data != 0) {
                                    psa.setString(4, subKey.getData().toString());
                                 }

                                 psa.addBatch();
                              }
                           }
                        }
                     default:
                        PrintStream var10000 = System.out;
                        String var10001 = mapleData.getName();
                        var10000.println("UNKNOWN EQ ADDITION : " + var10001 + " from " + this.id);
                     }
                  }
               }
            }

            dat = iz.getChildByPath("reward");
            if (dat != null) {
               psr.setInt(1, this.id);

               for(var41 = dat.iterator(); var41.hasNext(); totalprob += MapleDataTool.getIntConvert("prob", mapleData, 0)) {
                  mapleData = (MapleData)var41.next();
                  psr.setInt(2, MapleDataTool.getIntConvert("item", mapleData, 0));
                  psr.setInt(3, MapleDataTool.getIntConvert("prob", mapleData, 0));
                  psr.setInt(4, MapleDataTool.getIntConvert("count", mapleData, 0));
                  psr.setInt(5, MapleDataTool.getIntConvert("period", mapleData, 0));
                  psr.setString(6, MapleDataTool.getString("worldMsg", mapleData, ""));
                  psr.setString(7, MapleDataTool.getString("Effect", mapleData, ""));
                  psr.addBatch();
               }
            }

            ps.setInt(16, totalprob);
            ps.setString(17, incSkill.toString());
            dat = iz.getChildByPath("replace");
            if (dat != null) {
               ps.setInt(18, MapleDataTool.getInt("itemid", dat, 0));
               ps.setString(19, MapleDataTool.getString("msg", dat, ""));
            } else {
               ps.setInt(18, 0);
               ps.setString(19, "");
            }

            ps.setInt(20, MapleDataTool.getInt("info/create", iz, 0));
            ps.addBatch();
         }
      }
   }

   public void dumpItems(PreparedStatement psa, PreparedStatement psr, PreparedStatement ps, PreparedStatement pse) throws Exception {
      if (!this.update) {
         this.delete("DELETE FROM wz_itemdata");
         this.delete("DELETE FROM wz_itemequipdata");
         this.delete("DELETE FROM wz_itemadddata");
         this.delete("DELETE FROM wz_itemrewarddata");
         System.out.println("Deleted wz_itemdata successfully.");
      }

      System.out.println("Adding into wz_itemdata.....");
      this.dumpItems(this.item, psa, psr, ps, pse, false);
      this.dumpItems(this.character, psa, psr, ps, pse, true);
      System.out.println("Done wz_itemdata...");
      if (!this.subMain.isEmpty()) {
         System.out.println(this.subMain.toString());
      }

      if (!this.subCon.isEmpty()) {
         System.out.println(this.subCon.toString());
      }

   }

   public int currentId() {
      return this.id;
   }

   public static void main(String[] args) {
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
         DatabaseConnection.init();
         DumpItems dq = new DumpItems(update);
         System.out.println("Dumping Items");
         dq.dumpItems();
         hadError |= dq.isHadError();
         int var15 = dq.currentId();
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
   }

   protected final MapleData getStringData(int itemId) {
      String cat = null;
      MapleData data;
      if (itemId >= 5010000) {
         data = this.cashStringData;
      } else if (itemId >= 2000000 && itemId < 3000000) {
         data = this.consumeStringData;
      } else if (itemId >= 1132000 && itemId < 1183000 || itemId >= 1010000 && itemId < 1040000 || itemId >= 1122000 && itemId < 1123000) {
         data = this.eqpStringData;
         cat = "Eqp/Accessory";
      } else if (itemId >= 1662000 && itemId < 1680000) {
         data = this.eqpStringData;
         cat = "Eqp/Android";
      } else if (itemId >= 1000000 && itemId < 1010000) {
         data = this.eqpStringData;
         cat = "Eqp/Cap";
      } else if (itemId >= 1102000 && itemId < 1105000) {
         data = this.eqpStringData;
         cat = "Eqp/Cape";
      } else if (itemId >= 1040000 && itemId < 1050000) {
         data = this.eqpStringData;
         cat = "Eqp/Coat";
      } else if (itemId >= 20000 && itemId < 22000) {
         data = this.eqpStringData;
         cat = "Eqp/Face";
      } else if (itemId >= 1080000 && itemId < 1090000) {
         data = this.eqpStringData;
         cat = "Eqp/Glove";
      } else if (itemId >= 30000 && itemId < 35000) {
         data = this.eqpStringData;
         cat = "Eqp/Hair";
      } else if (itemId >= 1050000 && itemId < 1060000) {
         data = this.eqpStringData;
         cat = "Eqp/Longcoat";
      } else if (itemId >= 1060000 && itemId < 1070000) {
         data = this.eqpStringData;
         cat = "Eqp/Pants";
      } else if (itemId >= 1610000 && itemId < 1660000) {
         data = this.eqpStringData;
         cat = "Eqp/Mechanic";
      } else if (itemId >= 1802000 && itemId < 1820000) {
         data = this.eqpStringData;
         cat = "Eqp/PetEquip";
      } else if (itemId >= 1920000 && itemId < 2000000) {
         data = this.eqpStringData;
         cat = "Eqp/Dragon";
      } else if (itemId >= 1112000 && itemId < 1120000) {
         data = this.eqpStringData;
         cat = "Eqp/Ring";
      } else if (itemId >= 1092000 && itemId < 1100000) {
         data = this.eqpStringData;
         cat = "Eqp/Shield";
      } else if (itemId >= 1070000 && itemId < 1080000) {
         data = this.eqpStringData;
         cat = "Eqp/Shoes";
      } else if (itemId >= 1900000 && itemId < 1920000) {
         data = this.eqpStringData;
         cat = "Eqp/Taming";
      } else if (itemId >= 1200000 && itemId < 1210000) {
         data = this.eqpStringData;
         cat = "Eqp/Totem";
      } else if (itemId >= 1210000 && itemId < 1800000) {
         data = this.eqpStringData;
         cat = "Eqp/Weapon";
      } else if (itemId >= 4000000 && itemId < 5000000) {
         data = this.etcStringData;
         cat = "Etc";
      } else if (itemId >= 3000000 && itemId < 4000000) {
         data = this.insStringData;
      } else {
         if (itemId < 5000000 || itemId >= 5010000) {
            return null;
         }

         data = this.petStringData;
      }

      return cat == null ? data.getChildByPath(String.valueOf(itemId)) : data.getChildByPath(cat + "/" + itemId);
   }
}
