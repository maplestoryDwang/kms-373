package server;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.StructPotentialItem;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import java.awt.Point;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.enchant.EnchantFlag;
import server.enchant.EquipmentEnchant;
import server.enchant.StarForceStats;
import server.quest.MapleQuest;
import tools.Pair;
import tools.Triple;
import tools.packet.CWvsContext;

public class MapleItemInformationProvider {
   private static final MapleItemInformationProvider instance = new MapleItemInformationProvider();
   protected final MapleDataProvider chrData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Character.wz"));
   protected final MapleDataProvider etcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Etc.wz"));
   protected final MapleDataProvider itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Item.wz"));
   protected final MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/String.wz"));
   protected final Map<Integer, ItemInformation> dataCache = new HashMap();
   protected final Map<String, List<Triple<String, Point, Point>>> afterImage = new HashMap();
   protected final Map<Integer, List<StructPotentialItem>> potentialCache = new HashMap();
   protected final Map<Integer, SecondaryStatEffect> itemEffects = new HashMap();
   protected final Map<Integer, SecondaryStatEffect> itemEffectsEx = new HashMap();
   protected final Map<Integer, Integer> mobIds = new HashMap();
   protected final Map<Integer, Pair<Integer, Integer>> potLife = new HashMap();
   protected final Map<Integer, Triple<Pair<List<Integer>, List<Integer>>, List<Integer>, Integer>> androids = new HashMap();
   protected final Map<Integer, Triple<Integer, List<Integer>, List<Integer>>> monsterBookSets = new HashMap();
   protected final Map<Integer, StructSetItem> setItems = new HashMap();
   protected final List<Pair<Integer, String>> itemNameCache = new ArrayList();
   protected final Map<Integer, Integer> scrollUpgradeSlotUse = new HashMap();
   protected final Map<Integer, Integer> cursedCache = new HashMap();
   protected final Map<Integer, Integer> successCache = new HashMap();
   protected final Map<Integer, List<Triple<Boolean, Integer, Integer>>> potentialOpCache = new HashMap();
   private ItemInformation tmpInfo = null;

   public void runEtc() {
      if (this.setItems.isEmpty() && this.potentialCache.isEmpty()) {
         MapleData setsData = this.etcData.getData("SetItemInfo.img");
         Iterator var2 = setsData.iterator();

         MapleData lifesData;
         Iterator var7;
         MapleData skill;
         while(var2.hasNext()) {
            lifesData = (MapleData)var2.next();
            StructSetItem itemz = new StructSetItem();
            itemz.setItemID = Integer.parseInt(lifesData.getName());
            itemz.completeCount = (byte)MapleDataTool.getIntConvert("completeCount", lifesData, 0);
            itemz.jokerPossible = MapleDataTool.getIntConvert("jokerPossible", lifesData, 0) > 0;
            itemz.zeroWeaponJokerPossible = MapleDataTool.getIntConvert("zeroWeaponJokerPossible", lifesData, 0) > 0;
            Iterator var5 = lifesData.getChildByPath("ItemID").iterator();

            while(true) {
               MapleData level;
               while(var5.hasNext()) {
                  level = (MapleData)var5.next();
                  if (level.getType() != MapleDataType.INT) {
                     var7 = level.iterator();

                     while(var7.hasNext()) {
                        MapleData leve = (MapleData)var7.next();
                        if (!leve.getName().equals("representName") && !leve.getName().equals("typeName")) {
                           itemz.itemIDs.add(MapleDataTool.getInt(leve));
                        }
                     }
                  } else {
                     itemz.itemIDs.add(MapleDataTool.getInt(level));
                  }
               }

               StructSetItem.SetItem itez;
               for(var5 = lifesData.getChildByPath("Effect").iterator(); var5.hasNext(); itemz.items.put(Integer.parseInt(level.getName()), itez)) {
                  level = (MapleData)var5.next();
                  itez = new StructSetItem.SetItem();
                  itez.incPDD = MapleDataTool.getIntConvert("incPDD", level, 0);
                  itez.incMDD = MapleDataTool.getIntConvert("incMDD", level, 0);
                  itez.incSTR = MapleDataTool.getIntConvert("incSTR", level, 0);
                  itez.incDEX = MapleDataTool.getIntConvert("incDEX", level, 0);
                  itez.incINT = MapleDataTool.getIntConvert("incINT", level, 0);
                  itez.incLUK = MapleDataTool.getIntConvert("incLUK", level, 0);
                  itez.incACC = MapleDataTool.getIntConvert("incACC", level, 0);
                  itez.incPAD = MapleDataTool.getIntConvert("incPAD", level, 0);
                  itez.incMAD = MapleDataTool.getIntConvert("incMAD", level, 0);
                  itez.incSpeed = MapleDataTool.getIntConvert("incSpeed", level, 0);
                  itez.incMHP = MapleDataTool.getIntConvert("incMHP", level, 0);
                  itez.incMMP = MapleDataTool.getIntConvert("incMMP", level, 0);
                  itez.incMHPr = MapleDataTool.getIntConvert("incMHPr", level, 0);
                  itez.incMMPr = MapleDataTool.getIntConvert("incMMPr", level, 0);
                  itez.incAllStat = MapleDataTool.getIntConvert("incAllStat", level, 0);
                  itez.option1 = MapleDataTool.getIntConvert("Option/1/option", level, 0);
                  itez.option2 = MapleDataTool.getIntConvert("Option/2/option", level, 0);
                  itez.option1Level = MapleDataTool.getIntConvert("Option/1/level", level, 0);
                  itez.option2Level = MapleDataTool.getIntConvert("Option/2/level", level, 0);
                  if (level.getChildByPath("activeSkill") != null) {
                     Iterator var29 = level.getChildByPath("activeSkill").iterator();

                     while(var29.hasNext()) {
                        skill = (MapleData)var29.next();
                        itez.activeSkills.put(MapleDataTool.getIntConvert("id", skill, 0), (byte)MapleDataTool.getIntConvert("level", skill, 0));
                     }
                  }
               }

               this.setItems.put(itemz.setItemID, itemz);
               break;
            }
         }

         MapleDataDirectoryEntry e = (MapleDataDirectoryEntry)this.etcData.getRoot().getEntry("Android");
         Iterator var19 = e.getFiles().iterator();

         MapleData iz;
         while(var19.hasNext()) {
            MapleDataEntry d = (MapleDataEntry)var19.next();
            iz = this.etcData.getData("Android/" + d.getName());
            int gender = 0;
            List<Integer> hair = new ArrayList();
            List<Integer> face = new ArrayList();
            List<Integer> skin = new ArrayList();
            Iterator var10 = iz.getChildByPath("costume/hair").iterator();

            MapleData ds;
            while(var10.hasNext()) {
               ds = (MapleData)var10.next();
               hair.add(MapleDataTool.getInt(ds, 30000));
            }

            var10 = iz.getChildByPath("costume/face").iterator();

            while(var10.hasNext()) {
               ds = (MapleData)var10.next();
               face.add(MapleDataTool.getInt(ds, 20000));
            }

            var10 = iz.getChildByPath("costume/skin").iterator();

            while(var10.hasNext()) {
               ds = (MapleData)var10.next();
               skin.add(MapleDataTool.getInt(ds, 0));
            }

            var10 = iz.getChildByPath("info").iterator();

            while(var10.hasNext()) {
               ds = (MapleData)var10.next();
               if (ds.getName().equals("gender")) {
                  gender = MapleDataTool.getInt(ds, 0);
               }
            }

            this.androids.put(Integer.parseInt(d.getName().substring(0, 4)), new Triple(new Pair(hair, face), skin, gender));
         }

         lifesData = this.etcData.getData("ItemPotLifeInfo.img");
         Iterator var21 = lifesData.iterator();

         while(var21.hasNext()) {
            iz = (MapleData)var21.next();
            if (iz.getChildByPath("info") != null && MapleDataTool.getInt("type", iz.getChildByPath("info"), 0) == 1) {
               this.potLife.put(MapleDataTool.getInt("counsumeItem", iz.getChildByPath("info"), 0), new Pair(Integer.parseInt(iz.getName()), iz.getChildByPath("level").getChildren().size()));
            }
         }

         List<Triple<String, Point, Point>> thePointK = new ArrayList();
         List<Triple<String, Point, Point>> thePointA = new ArrayList();
         MapleDataDirectoryEntry a = (MapleDataDirectoryEntry)this.chrData.getRoot().getEntry("Afterimage");

         MapleDataEntry b;
         ArrayList thePoint;
         label178:
         for(var7 = a.getFiles().iterator(); var7.hasNext(); this.afterImage.put(b.getName().substring(0, b.getName().length() - 4), thePoint)) {
            b = (MapleDataEntry)var7.next();
            skill = this.chrData.getData("Afterimage/" + b.getName());
            thePoint = new ArrayList();
            Map<String, Pair<Point, Point>> dummy = new HashMap();
            Iterator var12 = skill.iterator();

            label163:
            while(var12.hasNext()) {
               MapleData i = (MapleData)var12.next();
               Iterator var14 = i.iterator();

               while(true) {
                  MapleData xD;
                  do {
                     do {
                        do {
                           do {
                              do {
                                 if (!var14.hasNext()) {
                                    continue label163;
                                 }

                                 xD = (MapleData)var14.next();
                              } while(xD.getName().contains("prone"));
                           } while(xD.getName().contains("double"));
                        } while(xD.getName().contains("triple"));
                     } while((b.getName().contains("bow") || b.getName().contains("Bow")) && !xD.getName().contains("shoot"));
                  } while((b.getName().contains("gun") || b.getName().contains("cannon")) && !xD.getName().contains("shot"));

                  Point point1;
                  Point ourRb;
                  if (dummy.containsKey(xD.getName())) {
                     if (xD.getChildByPath("lt") != null) {
                        point1 = (Point)xD.getChildByPath("lt").getData();
                        ourRb = (Point)((Pair)dummy.get(xD.getName())).left;
                        if (point1.x < ourRb.x) {
                           ourRb.x = point1.x;
                        }

                        if (point1.y < ourRb.y) {
                           ourRb.y = point1.y;
                        }
                     }

                     if (xD.getChildByPath("rb") != null) {
                        point1 = (Point)xD.getChildByPath("rb").getData();
                        ourRb = (Point)((Pair)dummy.get(xD.getName())).right;
                        if (point1.x > ourRb.x) {
                           ourRb.x = point1.x;
                        }

                        if (point1.y > ourRb.y) {
                           ourRb.y = point1.y;
                        }
                     }
                  } else {
                     point1 = null;
                     ourRb = null;
                     if (xD.getChildByPath("lt") != null) {
                        point1 = (Point)xD.getChildByPath("lt").getData();
                     }

                     if (xD.getChildByPath("rb") != null) {
                        ourRb = (Point)xD.getChildByPath("rb").getData();
                     }

                     dummy.put(xD.getName(), new Pair(point1, ourRb));
                  }
               }
            }

            var12 = dummy.entrySet().iterator();

            while(true) {
               while(true) {
                  if (!var12.hasNext()) {
                     continue label178;
                  }

                  Entry<String, Pair<Point, Point>> ez = (Entry)var12.next();
                  if (((String)ez.getKey()).length() > 2 && ((String)ez.getKey()).substring(((String)ez.getKey()).length() - 2, ((String)ez.getKey()).length() - 1).equals("D")) {
                     thePointK.add(new Triple((String)ez.getKey(), (Point)((Pair)ez.getValue()).left, (Point)((Pair)ez.getValue()).right));
                  } else if (((String)ez.getKey()).contains("PoleArm")) {
                     thePointA.add(new Triple((String)ez.getKey(), (Point)((Pair)ez.getValue()).left, (Point)((Pair)ez.getValue()).right));
                  } else {
                     thePoint.add(new Triple((String)ez.getKey(), (Point)((Pair)ez.getValue()).left, (Point)((Pair)ez.getValue()).right));
                  }
               }
            }
         }

         this.afterImage.put("katara", thePointK);
         this.afterImage.put("aran", thePointA);
      }
   }

   public void runItems() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM wz_itemdata");
         rs = ps.executeQuery();

         while(rs.next()) {
            this.initItemInformation(rs);
         }

         rs.close();
         ps.close();
         ps = con.prepareStatement("SELECT * FROM wz_itemequipdata ORDER BY itemid");
         rs = ps.executeQuery();

         while(rs.next()) {
            this.initItemEquipData(rs);
         }

         rs.close();
         ps.close();
         ps = con.prepareStatement("SELECT * FROM wz_itemadddata ORDER BY itemid");
         rs = ps.executeQuery();

         while(rs.next()) {
            this.initItemAddData(rs);
         }

         rs.close();
         ps.close();
         ps = con.prepareStatement("SELECT * FROM wz_itemrewarddata ORDER BY itemid");
         rs = ps.executeQuery();

         while(rs.next()) {
            this.initItemRewardData(rs);
         }

         rs.close();
         ps.close();
         Iterator var4 = this.dataCache.entrySet().iterator();

         while(true) {
            Entry entry;
            do {
               if (!var4.hasNext()) {
                  this.cachePotentialItems();
                  this.cachePotentialOption();
                  con.close();
                  return;
               }

               entry = (Entry)var4.next();
            } while(GameConstants.getInventoryType((Integer)entry.getKey()) != MapleInventoryType.EQUIP && GameConstants.getInventoryType((Integer)entry.getKey()) != MapleInventoryType.CODY);

            this.finalizeEquipData((ItemInformation)entry.getValue());
         }
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
               rs.close();
            }
         } catch (SQLException var13) {
            var13.printStackTrace();
         }

      }

   }

   public final int getPotentialOptionID(int level, boolean additional, int itemtype) {
      List<Integer> potentials = new ArrayList();
      int var5 = 0;

      while(potentials.size() <= 0 || potentials.isEmpty()) {
         potentials = new ArrayList();
         this.potentialSet(potentials, level, additional, itemtype);
         if (var5++ == 10) {
            break;
         }
      }

      if (potentials.size() <= 0 || potentials.isEmpty()) {
         System.out.println(level + "레벨 " + itemtype + "타입 아이템의 잠재능력 리스트 0개 -_- / 에디셔널 여부 : " + additional);
      }

      return (Integer)potentials.get(Randomizer.nextInt(potentials.size()));
   }

   private void potentialSet(List<Integer> potentials, int level, boolean additional, int itemtype) {
      if (this.isWeaponPotential(itemtype)) {
         this.addPotential(potentials, (List)this.potentialOpCache.get(10), level, additional, itemtype);
      } else {
         this.addPotential(potentials, (List)this.potentialOpCache.get(11), level, additional, itemtype);
      }

      if (!this.isWeaponPotential(itemtype)) {
         if (this.isAccessoryPotential(itemtype)) {
            this.addPotential(potentials, (List)this.potentialOpCache.get(40), level, additional, itemtype);
         } else if (additional) {
            this.addPotential(potentials, (List)this.potentialOpCache.get(20), level, additional, itemtype);
         }
      }

      if (itemtype / 10 == 100) {
         this.addPotential(potentials, (List)this.potentialOpCache.get(51), level, additional, itemtype);
      }

      if (itemtype / 10 == 104) {
         this.addPotential(potentials, (List)this.potentialOpCache.get(52), level, additional, itemtype);
      }

      if (itemtype / 10 == 106) {
         this.addPotential(potentials, (List)this.potentialOpCache.get(53), level, additional, itemtype);
      }

      if (itemtype / 10 == 107) {
         this.addPotential(potentials, (List)this.potentialOpCache.get(55), level, additional, itemtype);
      }

      if (itemtype / 10 == 108) {
         this.addPotential(potentials, (List)this.potentialOpCache.get(54), level, additional, itemtype);
      }

      this.addPotential(potentials, (List)this.potentialOpCache.get(-1), level, additional, itemtype);
   }

   private void addPotential(List<Integer> potentials, List<Triple<Boolean, Integer, Integer>> list, int level, boolean additional, int itemtype) {
      Iterator var6 = list.iterator();

      while(true) {
         Triple potential;
         do {
            label54:
            do {
               while(var6.hasNext()) {
                  potential = (Triple)var6.next();
                  if (additional) {
                     continue label54;
                  }

                  if (!(Boolean)potential.left && (itemtype != 1190 && itemtype != 1191 || (Integer)potential.right % 1000 < 601 || (Integer)potential.right % 1000 > 604) && (Integer)potential.right % 1000 >= 40 && level == (Integer)potential.right / 10000) {
                     potentials.add((Integer)potential.right);
                  }
               }

               return;
            } while(!(Boolean)potential.left);
         } while((itemtype == 1190 || itemtype == 1191) && (Integer)potential.right % 1000 >= 601 && (Integer)potential.right % 1000 <= 604);

         if ((Integer)potential.right % 1000 >= 40 && level == (Integer)potential.right / 10000) {
            potentials.add((Integer)potential.right);
         }
      }
   }

   private boolean isWeaponPotential(int itemtype) {
      return GameConstants.isWeapon(itemtype * 1000) || itemtype == 1098 || itemtype == 1092 || itemtype == 1099 || itemtype == 1190 || itemtype == 1191;
   }

   private boolean isAccessoryPotential(int itemtype) {
      return itemtype >= 1112 && itemtype <= 1115 || itemtype == 1122 || itemtype == 1012 || itemtype == 1022 || itemtype == 1032;
   }

   public String getPotentialName(Item item, int id) {
      String msg = "";
      int level = this.getReqLevel(item.getItemId()) / 10;
      if (level >= 20) {
         level -= 8;
      }

      msg = msg + this.getPotentialInfo(id).get(level);
      return msg;
   }

   public final List<StructPotentialItem> getPotentialInfo(int potId) {
      return (List)this.potentialCache.get(potId);
   }

   public void cachePotentialOption() {
      MapleData potsData = this.itemData.getData("ItemOption.img");
      Iterator var2 = potsData.iterator();

      while(var2.hasNext()) {
         MapleData data = (MapleData)var2.next();
         int potentialID = Integer.parseInt(data.getName());
         int type = MapleDataTool.getInt("info/optionType", data, -1);
         int reqLevel = MapleDataTool.getInt("info/reqLevel", data, 0);
         switch(potentialID) {
         case 32052:
         case 32054:
         case 32058:
         case 32059:
         case 32060:
         case 32061:
         case 32062:
         case 32071:
         case 32087:
         case 32116:
         case 40081:
         case 42052:
         case 42054:
         case 42058:
         case 42063:
         case 42064:
         case 42065:
         case 42066:
         case 42071:
         case 42087:
         case 42116:
         case 42291:
         case 42601:
         case 42650:
         case 42656:
         case 42661:
            break;
         default:
            boolean additional = potentialID % 10000 / 1000 == 2;
            if (this.potentialOpCache.get(type) == null) {
               List<Triple<Boolean, Integer, Integer>> potentialIds = new ArrayList();
               potentialIds.add(new Triple(additional, reqLevel, potentialID));
               this.potentialOpCache.put(type, potentialIds);
            } else {
               ((List)this.potentialOpCache.get(type)).add(new Triple(additional, reqLevel, potentialID));
            }
         }
      }

   }

   public void cachePotentialItems() {
      MapleData potsData = this.itemData.getData("ItemOption.img");
      Iterator var2 = potsData.iterator();

      while(var2.hasNext()) {
         MapleData data = (MapleData)var2.next();
         List<StructPotentialItem> items = new LinkedList();

         StructPotentialItem item;
         for(Iterator var5 = data.getChildByPath("level").iterator(); var5.hasNext(); items.add(item)) {
            MapleData level = (MapleData)var5.next();
            item = new StructPotentialItem();
            item.optionType = MapleDataTool.getIntConvert("info/optionType", data, 0);
            item.reqLevel = MapleDataTool.getIntConvert("info/reqLevel", data, 0);
            item.weight = MapleDataTool.getIntConvert("info/weight", data, 0);
            item.string = MapleDataTool.getString("info/string", level, "");
            item.face = MapleDataTool.getString("face", level, "");
            item.boss = MapleDataTool.getIntConvert("boss", level, 0) > 0;
            item.potentialID = Integer.parseInt(data.getName());
            item.attackType = (short)MapleDataTool.getIntConvert("attackType", level, 0);
            item.incMHP = (short)MapleDataTool.getIntConvert("incMHP", level, 0);
            item.incMMP = (short)MapleDataTool.getIntConvert("incMMP", level, 0);
            item.incSTR = (byte)MapleDataTool.getIntConvert("incSTR", level, 0);
            item.incDEX = (byte)MapleDataTool.getIntConvert("incDEX", level, 0);
            item.incINT = (byte)MapleDataTool.getIntConvert("incINT", level, 0);
            item.incLUK = (byte)MapleDataTool.getIntConvert("incLUK", level, 0);
            item.incACC = (byte)MapleDataTool.getIntConvert("incACC", level, 0);
            item.incEVA = (byte)MapleDataTool.getIntConvert("incEVA", level, 0);
            item.incSpeed = (byte)MapleDataTool.getIntConvert("incSpeed", level, 0);
            item.incJump = (byte)MapleDataTool.getIntConvert("incJump", level, 0);
            item.incPAD = (byte)MapleDataTool.getIntConvert("incPAD", level, 0);
            item.incMAD = (byte)MapleDataTool.getIntConvert("incMAD", level, 0);
            item.incPDD = (byte)MapleDataTool.getIntConvert("incPDD", level, 0);
            item.incMDD = (byte)MapleDataTool.getIntConvert("incMDD", level, 0);
            item.prop = (byte)MapleDataTool.getIntConvert("prop", level, 0);
            item.time = (byte)MapleDataTool.getIntConvert("time", level, 0);
            item.incSTRr = (byte)MapleDataTool.getIntConvert("incSTRr", level, 0);
            item.incDEXr = (byte)MapleDataTool.getIntConvert("incDEXr", level, 0);
            item.incINTr = (byte)MapleDataTool.getIntConvert("incINTr", level, 0);
            item.incLUKr = (byte)MapleDataTool.getIntConvert("incLUKr", level, 0);
            item.incMHPr = (byte)MapleDataTool.getIntConvert("incMHPr", level, 0);
            item.incMMPr = (byte)MapleDataTool.getIntConvert("incMMPr", level, 0);
            item.incACCr = (byte)MapleDataTool.getIntConvert("incACCr", level, 0);
            item.incEVAr = (byte)MapleDataTool.getIntConvert("incEVAr", level, 0);
            item.incPADr = (byte)MapleDataTool.getIntConvert("incPADr", level, 0);
            item.incMADr = (byte)MapleDataTool.getIntConvert("incMADr", level, 0);
            item.incPDDr = (byte)MapleDataTool.getIntConvert("incPDDr", level, 0);
            item.incMDDr = (byte)MapleDataTool.getIntConvert("incMDDr", level, 0);
            item.incCr = (byte)MapleDataTool.getIntConvert("incCr", level, 0);
            item.incDAMr = (byte)MapleDataTool.getIntConvert("incDAMr", level, 0);
            item.RecoveryHP = (byte)MapleDataTool.getIntConvert("RecoveryHP", level, 0);
            item.RecoveryMP = (byte)MapleDataTool.getIntConvert("RecoveryMP", level, 0);
            item.HP = (byte)MapleDataTool.getIntConvert("HP", level, 0);
            item.MP = (byte)MapleDataTool.getIntConvert("MP", level, 0);
            item.level = (byte)MapleDataTool.getIntConvert("level", level, 0);
            item.ignoreTargetDEF = (byte)MapleDataTool.getIntConvert("ignoreTargetDEF", level, 0);
            item.ignoreDAM = (byte)MapleDataTool.getIntConvert("ignoreDAM", level, 0);
            item.DAMreflect = (byte)MapleDataTool.getIntConvert("DAMreflect", level, 0);
            item.mpconReduce = (byte)MapleDataTool.getIntConvert("mpconReduce", level, 0);
            item.mpRestore = (byte)MapleDataTool.getIntConvert("mpRestore", level, 0);
            item.incMesoProp = (byte)MapleDataTool.getIntConvert("incMesoProp", level, 0);
            item.incRewardProp = (byte)MapleDataTool.getIntConvert("incRewardProp", level, 0);
            item.incAllskill = (byte)MapleDataTool.getIntConvert("incAllskill", level, 0);
            item.ignoreDAMr = (byte)MapleDataTool.getIntConvert("ignoreDAMr", level, 0);
            item.RecoveryUP = (byte)MapleDataTool.getIntConvert("RecoveryUP", level, 0);
            item.reduceCooltime = (byte)MapleDataTool.getIntConvert("reduceCooltime", level, 0);
            switch(item.potentialID) {
            case 31001:
            case 31002:
            case 31003:
            case 31004:
               item.skillID = item.potentialID - 23001;
               break;
            case 41005:
            case 41006:
            case 41007:
               item.skillID = item.potentialID - '胩';
               break;
            default:
               item.skillID = 0;
            }
         }

         this.potentialCache.put(Integer.parseInt(data.getName()), items);
      }

   }

   public final Collection<Integer> getMonsterBookList() {
      return this.mobIds.values();
   }

   public final Map<Integer, Integer> getMonsterBook() {
      return this.mobIds;
   }

   public final Pair<Integer, Integer> getPot(int f) {
      return (Pair)this.potLife.get(f);
   }

   public static final MapleItemInformationProvider getInstance() {
      return instance;
   }

   public final List<Pair<Integer, String>> getAllEquips() {
      List<Pair<Integer, String>> itemPairs = new ArrayList();
      MapleData itemsData = this.stringData.getData("Eqp.img").getChildByPath("Eqp");
      Iterator var3 = itemsData.getChildren().iterator();

      while(var3.hasNext()) {
         MapleData eqpType = (MapleData)var3.next();
         Iterator var5 = eqpType.getChildren().iterator();

         while(var5.hasNext()) {
            MapleData itemFolder = (MapleData)var5.next();
            itemPairs.add(new Pair(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
         }
      }

      return itemPairs;
   }

   public final List<Pair<Integer, String>> getAllItems() {
      if (!this.itemNameCache.isEmpty()) {
         return this.itemNameCache;
      } else {
         List<Pair<Integer, String>> itemPairs = new ArrayList();
         MapleData itemsData = this.stringData.getData("Cash.img");
         Iterator var3 = itemsData.getChildren().iterator();

         MapleData itemFolder;
         while(var3.hasNext()) {
            itemFolder = (MapleData)var3.next();
            itemPairs.add(new Pair(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
         }

         itemsData = this.stringData.getData("Consume.img");
         var3 = itemsData.getChildren().iterator();

         while(var3.hasNext()) {
            itemFolder = (MapleData)var3.next();
            itemPairs.add(new Pair(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
         }

         itemsData = this.stringData.getData("Eqp.img").getChildByPath("Eqp");
         var3 = itemsData.getChildren().iterator();

         while(var3.hasNext()) {
            itemFolder = (MapleData)var3.next();
            Iterator var5 = itemFolder.getChildren().iterator();

            while(var5.hasNext()) {
               MapleData itemFolder = (MapleData)var5.next();
               itemPairs.add(new Pair(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
            }
         }

         itemsData = this.stringData.getData("Etc.img").getChildByPath("Etc");
         var3 = itemsData.getChildren().iterator();

         while(var3.hasNext()) {
            itemFolder = (MapleData)var3.next();
            itemPairs.add(new Pair(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
         }

         itemsData = this.stringData.getData("Ins.img");
         var3 = itemsData.getChildren().iterator();

         while(var3.hasNext()) {
            itemFolder = (MapleData)var3.next();
            itemPairs.add(new Pair(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
         }

         itemsData = this.stringData.getData("Pet.img");
         var3 = itemsData.getChildren().iterator();

         while(var3.hasNext()) {
            itemFolder = (MapleData)var3.next();
            itemPairs.add(new Pair(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME")));
         }

         return itemPairs;
      }
   }

   public final Triple<Pair<List<Integer>, List<Integer>>, List<Integer>, Integer> getAndroidInfo(int i) {
      return (Triple)this.androids.get(i);
   }

   public final Triple<Integer, List<Integer>, List<Integer>> getMonsterBookInfo(int i) {
      return (Triple)this.monsterBookSets.get(i);
   }

   public final Map<Integer, Triple<Integer, List<Integer>, List<Integer>>> getAllMonsterBookInfo() {
      return this.monsterBookSets;
   }

   protected final MapleData getItemData(int itemId) {
      MapleData ret = null;
      String idStr = "0" + String.valueOf(itemId);
      MapleDataDirectoryEntry root = this.itemData.getRoot();
      Iterator var5 = root.getSubdirectories().iterator();

      MapleDataProvider var10000;
      String var10001;
      MapleDataDirectoryEntry topDir;
      Iterator var7;
      MapleDataFileEntry iFile;
      while(var5.hasNext()) {
         topDir = (MapleDataDirectoryEntry)var5.next();
         var7 = topDir.getFiles().iterator();

         while(var7.hasNext()) {
            iFile = (MapleDataFileEntry)var7.next();
            if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
               var10000 = this.itemData;
               var10001 = topDir.getName();
               ret = var10000.getData(var10001 + "/" + iFile.getName());
               if (ret == null) {
                  return null;
               }

               ret = ret.getChildByPath(idStr);
               return ret;
            }

            if (iFile.getName().equals(idStr.substring(1) + ".img")) {
               var10000 = this.itemData;
               var10001 = topDir.getName();
               return var10000.getData(var10001 + "/" + iFile.getName());
            }
         }
      }

      root = this.chrData.getRoot();
      var5 = root.getSubdirectories().iterator();

      while(var5.hasNext()) {
         topDir = (MapleDataDirectoryEntry)var5.next();
         var7 = topDir.getFiles().iterator();

         while(var7.hasNext()) {
            iFile = (MapleDataFileEntry)var7.next();
            if (iFile.getName().equals(idStr + ".img")) {
               var10000 = this.chrData;
               var10001 = topDir.getName();
               return var10000.getData(var10001 + "/" + iFile.getName());
            }
         }
      }

      return ret;
   }

   public Integer getItemIdByMob(int mobId) {
      return (Integer)this.mobIds.get(mobId);
   }

   public Integer getSetId(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : i.cardSet;
   }

   public final short getSlotMax(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? 0 : i.slotMax;
   }

   public final int getUpgradeScrollUseSlot(int itemid) {
      if (this.scrollUpgradeSlotUse.containsKey(itemid)) {
         return (Integer)this.scrollUpgradeSlotUse.get(itemid);
      } else {
         int useslot = MapleDataTool.getIntConvert("info/tuc", this.getItemData(itemid), 1);
         this.scrollUpgradeSlotUse.put(itemid, useslot);
         return (Integer)this.scrollUpgradeSlotUse.get(itemid);
      }
   }

   public final int getWholePrice(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? 0 : i.wholePrice;
   }

   public final double getPrice(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? -1.0D : i.price;
   }

   protected int rand(int min, int max) {
      return Math.abs(Randomizer.rand(min, max));
   }

   public Equip levelUpEquip(Equip equip, Map<String, Integer> sta) {
      Equip nEquip = (Equip)equip.copy();

      try {
         Iterator var4 = sta.entrySet().iterator();

         while(var4.hasNext()) {
            Entry<String, Integer> stat = (Entry)var4.next();
            if (((String)stat.getKey()).equals("STRMin")) {
               nEquip.setStr((short)(nEquip.getStr() + this.rand((Integer)stat.getValue(), (Integer)sta.get("STRMax"))));
            } else if (((String)stat.getKey()).equals("DEXMin")) {
               nEquip.setDex((short)(nEquip.getDex() + this.rand((Integer)stat.getValue(), (Integer)sta.get("DEXMax"))));
            } else if (((String)stat.getKey()).equals("INTMin")) {
               nEquip.setInt((short)(nEquip.getInt() + this.rand((Integer)stat.getValue(), (Integer)sta.get("INTMax"))));
            } else if (((String)stat.getKey()).equals("LUKMin")) {
               nEquip.setLuk((short)(nEquip.getLuk() + this.rand((Integer)stat.getValue(), (Integer)sta.get("LUKMax"))));
            } else if (((String)stat.getKey()).equals("PADMin")) {
               nEquip.setWatk((short)(nEquip.getWatk() + this.rand((Integer)stat.getValue(), (Integer)sta.get("PADMax"))));
            } else if (((String)stat.getKey()).equals("PDDMin")) {
               nEquip.setWdef((short)(nEquip.getWdef() + this.rand((Integer)stat.getValue(), (Integer)sta.get("PDDMax"))));
            } else if (((String)stat.getKey()).equals("MADMin")) {
               nEquip.setMatk((short)(nEquip.getMatk() + this.rand((Integer)stat.getValue(), (Integer)sta.get("MADMax"))));
            } else if (((String)stat.getKey()).equals("MDDMin")) {
               nEquip.setMdef((short)(nEquip.getMdef() + this.rand((Integer)stat.getValue(), (Integer)sta.get("MDDMax"))));
            } else if (((String)stat.getKey()).equals("ACCMin")) {
               nEquip.setAcc((short)(nEquip.getAcc() + this.rand((Integer)stat.getValue(), (Integer)sta.get("ACCMax"))));
            } else if (((String)stat.getKey()).equals("EVAMin")) {
               nEquip.setAvoid((short)(nEquip.getAvoid() + this.rand((Integer)stat.getValue(), (Integer)sta.get("EVAMax"))));
            } else if (((String)stat.getKey()).equals("SpeedMin")) {
               nEquip.setSpeed((short)(nEquip.getSpeed() + this.rand((Integer)stat.getValue(), (Integer)sta.get("SpeedMax"))));
            } else if (((String)stat.getKey()).equals("JumpMin")) {
               nEquip.setJump((short)(nEquip.getJump() + this.rand((Integer)stat.getValue(), (Integer)sta.get("JumpMax"))));
            } else if (((String)stat.getKey()).equals("MHPMin")) {
               nEquip.setHp((short)(nEquip.getHp() + this.rand((Integer)stat.getValue(), (Integer)sta.get("MHPMax"))));
            } else if (((String)stat.getKey()).equals("MMPMin")) {
               nEquip.setMp((short)(nEquip.getMp() + this.rand((Integer)stat.getValue(), (Integer)sta.get("MMPMax"))));
            } else if (((String)stat.getKey()).equals("MaxHPMin")) {
               nEquip.setHp((short)(nEquip.getHp() + this.rand((Integer)stat.getValue(), (Integer)sta.get("MaxHPMax"))));
            } else if (((String)stat.getKey()).equals("MaxMPMin")) {
               nEquip.setMp((short)(nEquip.getMp() + this.rand((Integer)stat.getValue(), (Integer)sta.get("MaxMPMax"))));
            }
         }
      } catch (NullPointerException var6) {
         var6.printStackTrace();
      }

      return nEquip;
   }

   public final List<Triple<String, String, String>> getEquipAdditions(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : i.equipAdditions;
   }

   public final String getEquipAddReqs(int itemId, String key, String sub) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return null;
      } else {
         Iterator var5 = i.equipAdditions.iterator();

         Triple data;
         do {
            if (!var5.hasNext()) {
               return null;
            }

            data = (Triple)var5.next();
         } while(!((String)data.getLeft()).equals("key") || !((String)data.getMid()).equals("con:" + sub));

         return (String)data.getRight();
      }
   }

   public final Map<Integer, Map<String, Integer>> getEquipIncrements(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : i.equipIncs;
   }

   public final List<Integer> getEquipSkills(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : i.incSkill;
   }

   public final boolean canEquip(Map<String, Integer> stats, int itemid, int level, int job, int fame, int str, int dex, int luk, int int_, int supremacy) {
      if (str >= (stats.containsKey("reqSTR") ? (Integer)stats.get("reqSTR") : 0) && dex >= (stats.containsKey("reqDEX") ? (Integer)stats.get("reqDEX") : 0) && luk >= (stats.containsKey("reqLUK") ? (Integer)stats.get("reqLUK") : 0) && int_ >= (stats.containsKey("reqINT") ? (Integer)stats.get("reqINT") : 0)) {
         Integer fameReq = (Integer)stats.get("reqPOP");
         return fameReq == null || fame >= fameReq;
      } else {
         return GameConstants.isDemonAvenger(job);
      }
   }

   public final Map<String, Integer> getEquipStats(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : i.equipStats;
   }

   public final int getReqLevel(int itemId) {
      return this.getEquipStats(itemId) != null && this.getEquipStats(itemId).containsKey("reqLevel") ? (Integer)this.getEquipStats(itemId).get("reqLevel") : 0;
   }

   public final int getReqJob(int itemId) {
      return this.getEquipStats(itemId) != null && this.getEquipStats(itemId).containsKey("reqJob") && itemId != 1190999 ? (Integer)this.getEquipStats(itemId).get("reqJob") : 0;
   }

   public final int getSlots(int itemId) {
      return this.getEquipStats(itemId) != null && this.getEquipStats(itemId).containsKey("tuc") ? (Integer)this.getEquipStats(itemId).get("tuc") : 0;
   }

   public final Integer getSetItemID(int itemId) {
      return this.getEquipStats(itemId) != null && this.getEquipStats(itemId).containsKey("setItemID") ? (Integer)this.getEquipStats(itemId).get("setItemID") : 0;
   }

   public final boolean isOnlyEquip(int itemId) {
      if (this.getEquipStats(itemId) != null && this.getEquipStats(itemId).containsKey("onlyEquip")) {
         return (Integer)this.getEquipStats(itemId).get("onlyEquip") > 0;
      } else {
         return false;
      }
   }

   public final StructSetItem getSetItem(int setItemId) {
      return (StructSetItem)this.setItems.get(setItemId);
   }

   public final int getCursed(int itemId, MapleCharacter player) {
      return this.getCursed(itemId, player, (Item)null);
   }

   public final int getCursed(int itemId, MapleCharacter player, Item equip) {
      if (this.cursedCache.containsKey(itemId)) {
         return (Integer)this.cursedCache.get(itemId);
      } else {
         MapleData item = this.getItemData(itemId);
         if (item == null) {
            return -1;
         } else {
            int success = false;
            int success = MapleDataTool.getIntConvert("info/cursed", item, -1);
            this.cursedCache.put(itemId, success);
            return success;
         }
      }
   }

   public final List<Integer> getScrollReqs(int itemId) {
      List<Integer> ret = new ArrayList();
      MapleData data = this.getItemData(itemId).getChildByPath("req");
      if (data == null) {
         return ret;
      } else {
         Iterator var4 = data.getChildren().iterator();

         while(var4.hasNext()) {
            MapleData req = (MapleData)var4.next();
            ret.add(MapleDataTool.getInt(req));
         }

         return ret;
      }
   }

   public final Item scrollEquipWithId(Item equip, Item scrollId, boolean ws, MapleCharacter chr) {
      if (equip.getType() == 1) {
         Equip nEquip = (Equip)equip;
         Equip zeroEquip = null;
         if (GameConstants.isAlphaWeapon(nEquip.getItemId())) {
            zeroEquip = (Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
         } else if (GameConstants.isBetaWeapon(nEquip.getItemId())) {
            zeroEquip = (Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         }

         boolean failed;
         Map<String, Integer> stats = this.getEquipStats(scrollId.getItemId());
         Map<String, Integer> eqstats = this.getEquipStats(equip.getItemId());
         failed = false;
         ArrayList statz;
         int flag;
         ArrayList list;
         short str;
         int randomstat;
         int rand;
         short dex;
         short int_;
         short luk;
         short s1;
         short s2;
         byte allstat;
         label1495:
         switch(scrollId.getItemId()) {
         case 2046025:
         case 2046026:
         case 2046119:
            statz = new ArrayList();
            allstat = 3;
            rand = Randomizer.rand(18, 20);
            nEquip.addStr((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addStr((short)allstat);
            }

            nEquip.addDex((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addDex((short)allstat);
            }

            nEquip.addInt((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addInt((short)allstat);
            }

            nEquip.addLuk((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addLuk((short)allstat);
            }

            if (scrollId.getItemId() == 2046026) {
               nEquip.addMatk((short)rand);
               if (zeroEquip != null) {
                  zeroEquip.addMatk((short)rand);
               }

               statz.add(new Pair(EnchantFlag.Matk, rand));
            } else {
               nEquip.addWatk((short)rand);
               if (zeroEquip != null) {
                  zeroEquip.addWatk((short)rand);
               }

               statz.add(new Pair(EnchantFlag.Watk, rand));
            }

            equip.setShowScrollOption(new StarForceStats(statz));
            break;
         case 2046054:
         case 2046055:
         case 2046056:
         case 2046057:
         case 2046058:
         case 2046059:
         case 2046094:
         case 2046095:
         case 2046120:
         case 2046138:
         case 2046139:
         case 2046140:
         case 2046162:
         case 2046163:
         case 2046251:
         case 2046340:
         case 2046341:
         case 2046374:
         case 2046564:
         case 2049049:
         case 2049050:
         case 2049052:
            if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
               if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr)) && ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                  chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
               }

               failed = true;
            } else {
               switch(scrollId.getItemId()) {
               case 2046025:
                  nEquip.setWatk((short)(nEquip.getWatk() + Randomizer.rand(7, 8)));
                  break label1495;
               case 2046026:
                  nEquip.setMatk((short)(nEquip.getMatk() + Randomizer.rand(7, 8)));
                  break label1495;
               case 2046054:
               case 2046055:
               case 2046056:
               case 2046057:
               case 2046138:
               case 2046139:
                  if (scrollId.getItemId() != 2046055 && scrollId.getItemId() != 2046057) {
                     nEquip.setWatk((short)(nEquip.getWatk() + 5));
                  } else {
                     nEquip.setMatk((short)(nEquip.getMatk() + 5));
                  }

                  nEquip.setStr((short)(nEquip.getStr() + 3));
                  nEquip.setDex((short)(nEquip.getDex() + 3));
                  nEquip.setInt((short)(nEquip.getInt() + 3));
                  nEquip.setLuk((short)(nEquip.getLuk() + 3));
                  nEquip.setAcc((short)(nEquip.getAcc() + 15));
                  break label1495;
               case 2046058:
               case 2046059:
               case 2046140:
                  if (scrollId.getItemId() == 2046059) {
                     nEquip.setMatk((short)(nEquip.getMatk() + 2));
                  } else {
                     nEquip.setWatk((short)(nEquip.getWatk() + 2));
                  }

                  nEquip.setStr((short)(nEquip.getStr() + 1));
                  nEquip.setDex((short)(nEquip.getDex() + 1));
                  nEquip.setInt((short)(nEquip.getInt() + 1));
                  nEquip.setLuk((short)(nEquip.getLuk() + 1));
                  nEquip.setAcc((short)(nEquip.getAcc() + 5));
                  break label1495;
               case 2046094:
                  nEquip.setWatk((short)(nEquip.getWatk() + Randomizer.rand(7, 9)));
                  break label1495;
               case 2046095:
                  nEquip.setMatk((short)(nEquip.getMatk() + Randomizer.rand(7, 9)));
                  break label1495;
               case 2046119:
                  nEquip.setWatk((short)(nEquip.getWatk() + Randomizer.rand(7, 8)));
                  break label1495;
               case 2046120:
                  nEquip.setMatk((short)(nEquip.getMatk() + Randomizer.rand(7, 8)));
                  break label1495;
               case 2046162:
                  nEquip.setWatk((short)(nEquip.getWatk() + Randomizer.rand(7, 9)));
                  break label1495;
               case 2046163:
                  nEquip.setMatk((short)(nEquip.getMatk() + Randomizer.rand(7, 9)));
                  break label1495;
               case 2046251:
                  nEquip.setStr((short)(nEquip.getStr() + 3));
                  nEquip.setInt((short)(nEquip.getInt() + 3));
                  nEquip.setDex((short)(nEquip.getDex() + 3));
                  nEquip.setLuk((short)(nEquip.getLuk() + 3));
                  break label1495;
               case 2046340:
                  nEquip.setWatk((short)(nEquip.getWatk() + 1));
                  break label1495;
               case 2046341:
                  nEquip.setMatk((short)(nEquip.getMatk() + 1));
                  break label1495;
               case 2046374:
                  nEquip.setWatk((short)(nEquip.getWatk() + 3));
                  nEquip.setMatk((short)(nEquip.getMatk() + 3));
                  nEquip.setWdef((short)(nEquip.getWdef() + 25));
                  nEquip.setMdef((short)(nEquip.getMdef() + 25));
                  nEquip.setStr((short)(nEquip.getStr() + 3));
                  nEquip.setDex((short)(nEquip.getDex() + 3));
                  nEquip.setInt((short)(nEquip.getInt() + 3));
                  nEquip.setLuk((short)(nEquip.getLuk() + 3));
                  nEquip.setAvoid((short)(nEquip.getAvoid() + 30));
                  nEquip.setAcc((short)(nEquip.getAcc() + 30));
                  nEquip.setSpeed((short)(nEquip.getSpeed() + 3));
                  nEquip.setJump((short)(nEquip.getJump() + 2));
                  nEquip.setMp((short)(nEquip.getMp() + 25));
                  nEquip.setHp((short)(nEquip.getHp() + 25));
                  break label1495;
               case 2046564:
                  nEquip.setStr((short)(nEquip.getStr() + 5));
                  nEquip.setInt((short)(nEquip.getInt() + 5));
                  nEquip.setDex((short)(nEquip.getDex() + 5));
                  nEquip.setLuk((short)(nEquip.getLuk() + 5));
                  break label1495;
               case 2048082:
               case 2048827:
               case 2048832:
               case 5530338:
                  nEquip.setWatk((short)(nEquip.getWatk() + Randomizer.rand(2, 4)));
                  break label1495;
               case 2048083:
               case 2048828:
               case 2048833:
               case 5530339:
                  nEquip.setMatk((short)(nEquip.getMatk() + Randomizer.rand(2, 4)));
                  break label1495;
               case 2048094:
               case 2048804:
               case 2048836:
               case 2048838:
               case 5530442:
                  nEquip.setWatk((short)(nEquip.getWatk() + Randomizer.rand(4, 5)));
                  break label1495;
               case 2048095:
               case 2048805:
               case 2048837:
               case 2048839:
               case 5530443:
                  nEquip.setMatk((short)(nEquip.getMatk() + Randomizer.rand(4, 5)));
                  break label1495;
               case 2049049:
                  nEquip.setTotalDamage((byte)((short)(nEquip.getTotalDamage() + 1)));
                  break label1495;
               case 2049050:
                  nEquip.setBossDamage((short)(nEquip.getBossDamage() + 1));
                  break label1495;
               case 2049052:
                  nEquip.setUpgradeSlots((byte)(nEquip.getUpgradeSlots() + 1));
                  break label1495;
               case 5530336:
                  nEquip.setWatk((short)(nEquip.getWatk() + Randomizer.rand(2, 4)));
                  break label1495;
               case 5530337:
                  nEquip.setMatk((short)(nEquip.getMatk() + Randomizer.rand(2, 4)));
               }
            }
            break;
         case 2046841:
         case 2046842:
         case 2046967:
         case 2046971:
         case 2047803:
         case 2047917:
            if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
               if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr)) && ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                  chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
               }

               failed = true;
            } else {
               switch(scrollId.getItemId()) {
               case 2046841:
                  nEquip.setWatk((short)(nEquip.getWatk() + 1));
                  break label1495;
               case 2046842:
                  nEquip.setMatk((short)(nEquip.getMatk() + 1));
                  break label1495;
               case 2046967:
                  nEquip.setWatk((short)(nEquip.getWatk() + 9));
                  nEquip.setStr((short)(nEquip.getStr() + 3));
                  nEquip.setInt((short)(nEquip.getInt() + 3));
                  nEquip.setDex((short)(nEquip.getDex() + 3));
                  nEquip.setLuk((short)(nEquip.getLuk() + 3));
                  break label1495;
               case 2046971:
                  nEquip.setMatk((short)(nEquip.getMatk() + 9));
                  nEquip.setStr((short)(nEquip.getStr() + 3));
                  nEquip.setInt((short)(nEquip.getInt() + 3));
                  nEquip.setDex((short)(nEquip.getDex() + 3));
                  nEquip.setLuk((short)(nEquip.getLuk() + 3));
                  break label1495;
               case 2047803:
                  nEquip.setWatk((short)(nEquip.getWatk() + 9));
                  nEquip.setStr((short)(nEquip.getStr() + 3));
                  nEquip.setInt((short)(nEquip.getInt() + 3));
                  nEquip.setDex((short)(nEquip.getDex() + 3));
                  nEquip.setLuk((short)(nEquip.getLuk() + 3));
                  break label1495;
               case 2047917:
                  nEquip.setStr((short)(nEquip.getStr() + 9));
                  nEquip.setInt((short)(nEquip.getInt() + 9));
                  nEquip.setDex((short)(nEquip.getDex() + 9));
                  nEquip.setLuk((short)(nEquip.getLuk() + 9));
               }
            }
            break;
         case 2046856:
         case 2046857:
            list = new ArrayList();
            list.add(new Pair(4, 85));
            list.add(new Pair(5, 15));
            randomstat = GameConstants.isRandStat(list, 100);
            if (scrollId.getItemId() == 2046857) {
               nEquip.addMatk((short)randomstat);
            } else {
               nEquip.addWatk((short)randomstat);
            }
            break;
         case 2046991:
         case 2046992:
         case 2047814:
            list = new ArrayList();
            list.add(new Pair(9, 50));
            list.add(new Pair(10, 40));
            list.add(new Pair(11, 10));
            randomstat = GameConstants.isRandStat(list, 100);
            nEquip.addStr((short)3);
            nEquip.addDex((short)3);
            nEquip.addInt((short)3);
            nEquip.addLuk((short)3);
            if (scrollId.getItemId() == 2046992) {
               nEquip.addMatk((short)randomstat);
               if (zeroEquip != null) {
                  zeroEquip.addMatk((short)randomstat);
                  zeroEquip.addStr((short)3);
                  zeroEquip.addDex((short)3);
                  zeroEquip.addInt((short)3);
                  zeroEquip.addLuk((short)3);
               }
            } else {
               nEquip.addWatk((short)randomstat);
               if (zeroEquip != null) {
                  zeroEquip.addWatk((short)randomstat);
                  zeroEquip.addStr((short)3);
                  zeroEquip.addDex((short)3);
                  zeroEquip.addInt((short)3);
                  zeroEquip.addLuk((short)3);
               }
            }
            break;
         case 2046996:
         case 2047818:
            short watk = (short)Randomizer.rand(10, 10);
            str = (short)Randomizer.rand(3, 3);
            dex = (short)Randomizer.rand(3, 3);
            int_ = (short)Randomizer.rand(3, 3);
            luk = (short)Randomizer.rand(3, 3);
            nEquip.addWatk(watk);
            if (zeroEquip != null) {
               zeroEquip.addWatk(watk);
            }

            nEquip.addStr(str);
            if (zeroEquip != null) {
               zeroEquip.addStr(str);
            }

            nEquip.addDex(dex);
            if (zeroEquip != null) {
               zeroEquip.addDex(dex);
            }

            nEquip.addInt(int_);
            if (zeroEquip != null) {
               zeroEquip.addInt(int_);
            }

            nEquip.addLuk(luk);
            if (zeroEquip != null) {
               zeroEquip.addLuk(int_);
            }
            break;
         case 2046997:
            short matk = (short)Randomizer.rand(10, 10);
            str = (short)Randomizer.rand(3, 3);
            dex = (short)Randomizer.rand(3, 3);
            int_ = (short)Randomizer.rand(3, 3);
            luk = (short)Randomizer.rand(3, 3);
            nEquip.addMatk(matk);
            if (zeroEquip != null) {
               zeroEquip.addWatk(matk);
            }

            nEquip.addStr(str);
            if (zeroEquip != null) {
               zeroEquip.addStr(str);
            }

            nEquip.addDex(dex);
            if (zeroEquip != null) {
               zeroEquip.addDex(dex);
            }

            nEquip.addInt(int_);
            if (zeroEquip != null) {
               zeroEquip.addInt(int_);
            }

            nEquip.addLuk(luk);
            if (zeroEquip != null) {
               zeroEquip.addLuk(int_);
            }
            break;
         case 2047405:
         case 2047406:
            list = new ArrayList();
            list.add(new Pair(4, 80));
            list.add(new Pair(5, 20));
            randomstat = GameConstants.isRandStat(list, 100);
            if (scrollId.getItemId() == 2047406) {
               nEquip.addMatk((short)randomstat);
               if (zeroEquip != null) {
                  zeroEquip.addMatk((short)randomstat);
               }
            } else {
               nEquip.addWatk((short)randomstat);
               if (zeroEquip != null) {
                  zeroEquip.addWatk((short)randomstat);
               }
            }
            break;
         case 2048094:
         case 2048804:
         case 2048836:
         case 2048838:
            list = new ArrayList();
            list.add(new Pair(4, 85));
            list.add(new Pair(5, 15));
            randomstat = GameConstants.isRandStat(list, 100);
            nEquip.setWatk((short)(nEquip.getWatk() + randomstat));
            break;
         case 2048095:
         case 2048805:
         case 2048837:
         case 2048839:
            list = new ArrayList();
            list.add(new Pair(4, 85));
            list.add(new Pair(5, 15));
            randomstat = GameConstants.isRandStat(list, 100);
            nEquip.setMatk((short)(nEquip.getMatk() + randomstat));
            break;
         case 2048306:
         case 2048338:
         case 2048344:
            if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
               if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr)) && ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                  chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
               }

               failed = true;
            } else if (nEquip.getState() <= 17) {
               nEquip.setState((byte)4);
               if (zeroEquip != null) {
                  zeroEquip.setLines((byte)4);
               }

               if (Randomizer.nextInt(100) < 30) {
                  nEquip.setLines((byte)3);
                  if (zeroEquip != null) {
                     zeroEquip.setLines((byte)3);
                  }
               } else {
                  nEquip.setLines((byte)2);
                  if (zeroEquip != null) {
                     zeroEquip.setLines((byte)2);
                  }
               }
            }
            break;
         case 2048809:
            nEquip.setWatk((short)(nEquip.getWatk() + 2));
            break;
         case 2048810:
            nEquip.setMatk((short)(nEquip.getMatk() + 2));
            break;
         case 2048900:
         case 2048901:
         case 2048902:
         case 2048903:
         case 2048904:
         case 2048905:
         case 2048906:
         case 2048907:
         case 2048912:
         case 2048913:
         case 2048915:
         case 2048918:
            if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
               failed = true;
            } else {
               MapleQuest quest = MapleQuest.getInstance(41907);
               String stringa = String.valueOf(GameConstants.getLuckyInfofromItemId(scrollId.getItemId()));
               chr.setKeyValue(46523, "luckyscroll", stringa);
               MapleQuestStatus queststatus = new MapleQuestStatus(quest, 1);
               queststatus.setCustomData(stringa == null ? "0" : stringa);
               chr.updateQuest(queststatus, true);
            }
            break;
         case 2049000:
         case 2049001:
         case 2049002:
         case 2049004:
         case 2049005:
            if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
               failed = true;
            } else if (nEquip.getLevel() + nEquip.getUpgradeSlots() < (Integer)eqstats.get("tuc") + (nEquip.getViciousHammer() > 0 ? 1 : 0)) {
               nEquip.setUpgradeSlots((byte)(nEquip.getUpgradeSlots() + 1));
               if (zeroEquip != null) {
                  zeroEquip.setUpgradeSlots((byte)(zeroEquip.getUpgradeSlots() + 1));
               }
            }
            break;
         case 2049006:
         case 2049007:
         case 2049008:
            if (nEquip.getLevel() + nEquip.getUpgradeSlots() < (Integer)eqstats.get("tuc") + (nEquip.getViciousHammer() > 0 ? 1 : 0)) {
               nEquip.setUpgradeSlots((byte)(nEquip.getUpgradeSlots() + 2));
               if (zeroEquip != null) {
                  zeroEquip.setUpgradeSlots((byte)(zeroEquip.getUpgradeSlots() + 2));
               }
            }
            break;
         case 2049135:
            statz = new ArrayList();
            allstat = 3;
            rand = Randomizer.rand(4, 6);
            int randb = Randomizer.rand(1, 3);
            nEquip.addStr((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addStr((short)allstat);
            }

            nEquip.addDex((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addDex((short)allstat);
            }

            nEquip.addInt((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addInt((short)allstat);
            }

            nEquip.addLuk((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addLuk((short)allstat);
            }

            nEquip.addMatk((short)rand);
            if (zeroEquip != null) {
               zeroEquip.addMatk((short)rand);
            }

            statz.add(new Pair(EnchantFlag.Matk, rand));
            nEquip.addWatk((short)rand);
            if (zeroEquip != null) {
               zeroEquip.addWatk((short)rand);
            }

            statz.add(new Pair(EnchantFlag.Watk, rand));
            nEquip.addBossDamage((byte)randb);
            if (zeroEquip != null) {
               zeroEquip.addBossDamage((byte)randb);
            }

            equip.setShowScrollOption(new StarForceStats(statz));
            break;
         case 2049136:
            statz = new ArrayList();
            int allstat = Randomizer.rand(12, 14);
            rand = Randomizer.rand(10, 11);
            nEquip.addStr((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addStr((short)allstat);
            }

            nEquip.addDex((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addDex((short)allstat);
            }

            nEquip.addInt((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addInt((short)allstat);
            }

            nEquip.addLuk((short)allstat);
            if (zeroEquip != null) {
               zeroEquip.addLuk((short)allstat);
            }

            statz.add(new Pair(EnchantFlag.Str, allstat));
            statz.add(new Pair(EnchantFlag.Dex, allstat));
            statz.add(new Pair(EnchantFlag.Int, allstat));
            statz.add(new Pair(EnchantFlag.Luk, allstat));
            nEquip.addMatk((short)rand);
            if (zeroEquip != null) {
               zeroEquip.addMatk((short)rand);
            }

            statz.add(new Pair(EnchantFlag.Matk, rand));
            nEquip.addWatk((short)rand);
            if (zeroEquip != null) {
               zeroEquip.addWatk((short)rand);
            }

            statz.add(new Pair(EnchantFlag.Watk, rand));
            equip.setShowScrollOption(new StarForceStats(statz));
            break;
         case 2049166:
         case 2049167:
            statz = new ArrayList();
            short str = 5;
            short dex = 5;
            short int_ = 5;
            short luk = 5;
            s1 = (short)Randomizer.rand(15, 17);
            s2 = (short)Randomizer.rand(15, 17);
            nEquip.addWatk(s1);
            if (zeroEquip != null) {
               zeroEquip.addWatk(s1);
            }

            statz.add(new Pair(EnchantFlag.Watk, Integer.valueOf(s1)));
            nEquip.addMatk(s2);
            if (zeroEquip != null) {
               zeroEquip.addMatk(s2);
            }

            statz.add(new Pair(EnchantFlag.Matk, Integer.valueOf(s2)));
            nEquip.addStr(str);
            if (zeroEquip != null) {
               zeroEquip.addStr(str);
            }

            nEquip.addDex(dex);
            if (zeroEquip != null) {
               zeroEquip.addDex(dex);
            }

            nEquip.addInt(int_);
            if (zeroEquip != null) {
               zeroEquip.addInt(int_);
            }

            nEquip.addLuk(luk);
            if (zeroEquip != null) {
               zeroEquip.addLuk(int_);
            }

            equip.setShowScrollOption(new StarForceStats(statz));
            break;
         case 2049168:
            statz = new ArrayList();
            str = (short)Randomizer.rand(6, 9);
            dex = (short)Randomizer.rand(6, 9);
            int_ = (short)Randomizer.rand(6, 9);
            luk = (short)Randomizer.rand(6, 9);
            s1 = (short)Randomizer.rand(6, 9);
            s2 = (short)Randomizer.rand(6, 9);
            nEquip.addWatk(s1);
            if (zeroEquip != null) {
               zeroEquip.addWatk(s1);
            }

            statz.add(new Pair(EnchantFlag.Watk, Integer.valueOf(s1)));
            nEquip.addMatk(s2);
            if (zeroEquip != null) {
               zeroEquip.addMatk(s2);
            }

            statz.add(new Pair(EnchantFlag.Matk, Integer.valueOf(s2)));
            nEquip.addStr(str);
            if (zeroEquip != null) {
               zeroEquip.addStr(str);
            }

            statz.add(new Pair(EnchantFlag.Str, Integer.valueOf(str)));
            nEquip.addDex(dex);
            if (zeroEquip != null) {
               zeroEquip.addDex(dex);
            }

            statz.add(new Pair(EnchantFlag.Dex, Integer.valueOf(dex)));
            nEquip.addInt(int_);
            if (zeroEquip != null) {
               zeroEquip.addInt(int_);
            }

            statz.add(new Pair(EnchantFlag.Int, Integer.valueOf(int_)));
            nEquip.addLuk(luk);
            if (zeroEquip != null) {
               zeroEquip.addLuk(luk);
            }

            statz.add(new Pair(EnchantFlag.Luk, Integer.valueOf(luk)));
            equip.setShowScrollOption(new StarForceStats(statz));
            break;
         case 2049700:
         case 2049701:
         case 2049702:
         case 2049703:
            if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
               if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr)) && ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                  chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
               }

               failed = true;
            } else if (nEquip.getState() <= 17) {
               nEquip.setState((byte)2);
               if (zeroEquip != null) {
                  zeroEquip.setLines((byte)2);
               }

               if (Randomizer.nextInt(100) < 30) {
                  nEquip.setLines((byte)3);
                  if (zeroEquip != null) {
                     zeroEquip.setLines((byte)3);
                  }
               } else {
                  nEquip.setLines((byte)2);
                  if (zeroEquip != null) {
                     zeroEquip.setLines((byte)2);
                  }
               }
            }
            break;
         case 2049704:
         case 5063000:
            if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
               if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr))) {
               }

               failed = true;
            } else if (nEquip.getState() <= 17) {
               nEquip.setState((byte)4);
               if (zeroEquip != null) {
                  zeroEquip.setState((byte)4);
               }

               if (Randomizer.nextInt(100) < 30) {
                  nEquip.setLines((byte)3);
                  if (zeroEquip != null) {
                     zeroEquip.setLines((byte)3);
                  }
               } else {
                  nEquip.setLines((byte)2);
                  if (zeroEquip != null) {
                     zeroEquip.setLines((byte)2);
                  }
               }
            }
            break;
         case 2049750:
         case 2049751:
         case 2049752:
            if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
               if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr)) && ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                  chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
               }

               failed = true;
            } else if (nEquip.getState() <= 19) {
               nEquip.setState((byte)3);
               if (zeroEquip != null) {
                  zeroEquip.setLines((byte)3);
               }

               if (Randomizer.nextInt(100) < 30) {
                  nEquip.setLines((byte)3);
                  if (zeroEquip != null) {
                     zeroEquip.setLines((byte)3);
                  }
               } else {
                  nEquip.setLines((byte)2);
                  if (zeroEquip != null) {
                     zeroEquip.setLines((byte)2);
                  }
               }
            }
            break;
         case 2530000:
         case 2530001:
         case 2530002:
            flag = nEquip.getFlag();
            flag += ItemFlag.LUCKY_PROTECT_SHIELD.getValue();
            nEquip.setFlag(flag);
            if (zeroEquip != null) {
               zeroEquip.setFlag(flag);
            }
            break;
         case 2531000:
         case 2531001:
         case 2531005:
            flag = nEquip.getFlag();
            flag += ItemFlag.PROTECT_SHIELD.getValue();
            nEquip.setFlag(flag);
            if (zeroEquip != null) {
               zeroEquip.setFlag(flag);
            }
            break;
         case 2532000:
         case 2532002:
         case 2532005:
            flag = nEquip.getFlag();
            flag += ItemFlag.SAFETY_SHIELD.getValue();
            nEquip.setFlag(flag);
            if (zeroEquip != null) {
               zeroEquip.setFlag(flag);
            }
            break;
         case 2533000:
            flag = nEquip.getFlag();
            flag += ItemFlag.RECOVERY_SHIELD.getValue();
            nEquip.setFlag(flag);
            if (zeroEquip != null) {
               zeroEquip.setFlag(flag);
            }
            break;
         case 2643128:
            if (nEquip.getItemId() == 1114300) {
               nEquip.addStr((short)1);
               nEquip.addDex((short)1);
               nEquip.addInt((short)1);
               nEquip.addLuk((short)1);
               nEquip.addWatk((short)1);
               nEquip.addMatk((short)1);
               nEquip.addHp((short)100);
               nEquip.addMp((short)100);
            }
            break;
         case 2643130:
            if (nEquip.getItemId() == 1114303) {
               nEquip.addStr((short)1);
               nEquip.addDex((short)1);
               nEquip.addInt((short)1);
               nEquip.addLuk((short)1);
               nEquip.addWatk((short)1);
               nEquip.addMatk((short)1);
               nEquip.addHp((short)100);
               nEquip.addMp((short)100);
            }
            break;
         case 2645000:
         case 2645001:
            switch(nEquip.getItemId()) {
            case 1032220:
            case 1032221:
            case 1032222:
            case 1113072:
            case 1113073:
            case 1113074:
            case 1122264:
            case 1122265:
            case 1122266:
            case 1132243:
            case 1132244:
            case 1132245:
               nEquip.addStr((short)3);
               nEquip.addDex((short)3);
               nEquip.addInt((short)3);
               nEquip.addLuk((short)3);
               if (scrollId.getItemId() == 2645000) {
                  nEquip.addWatk((short)3);
               } else {
                  nEquip.addMatk((short)3);
               }
            default:
               break label1495;
            }
         case 2645002:
         case 2645003:
            switch(nEquip.getItemId()) {
            case 1032223:
            case 1113075:
            case 1122267:
            case 1132246:
               nEquip.addStr((short)Randomizer.rand(10, 30));
               nEquip.addDex((short)Randomizer.rand(10, 30));
               nEquip.addInt((short)Randomizer.rand(10, 30));
               nEquip.addLuk((short)Randomizer.rand(10, 30));
               if (scrollId.getItemId() == 2645002) {
                  nEquip.addWatk((short)Randomizer.rand(10, 20));
               } else {
                  nEquip.addMatk((short)Randomizer.rand(10, 20));
               }
            default:
               break label1495;
            }
         default:
            int slevel;
            if (GameConstants.isChaosScroll(scrollId.getItemId())) {
               if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
                  if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr, nEquip)) && ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                     chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
                  }

                  failed = true;
               } else {
                  List<Pair<EnchantFlag, Integer>> list1 = new ArrayList();
                  if (GameConstants.getChaosNumber(scrollId.getItemId()) != 999) {
                     if (nEquip.getStr() > 0) {
                        slevel = GameConstants.getChaosNumber(scrollId.getItemId());
                        if (slevel != 999) {
                           nEquip.addStr((short)slevel);
                           if (zeroEquip != null) {
                              zeroEquip.addStr((short)slevel);
                           }

                           list1.add(new Pair(EnchantFlag.Str, slevel));
                        }
                     }

                     if (nEquip.getDex() > 0) {
                        slevel = GameConstants.getChaosNumber(scrollId.getItemId());
                        if (slevel != 999) {
                           nEquip.addDex((short)slevel);
                           if (zeroEquip != null) {
                              zeroEquip.addDex((short)slevel);
                           }

                           list1.add(new Pair(EnchantFlag.Dex, slevel));
                        }
                     }

                     if (nEquip.getInt() > 0) {
                        slevel = GameConstants.getChaosNumber(scrollId.getItemId());
                        if (slevel != 999) {
                           nEquip.addInt((short)slevel);
                           if (zeroEquip != null) {
                              zeroEquip.addInt((short)slevel);
                           }

                           list1.add(new Pair(EnchantFlag.Int, slevel));
                        }
                     }

                     if (nEquip.getLuk() > 0) {
                        slevel = GameConstants.getChaosNumber(scrollId.getItemId());
                        if (slevel != 999) {
                           nEquip.addLuk((short)slevel);
                           if (zeroEquip != null) {
                              zeroEquip.addLuk((short)slevel);
                           }

                           list1.add(new Pair(EnchantFlag.Luk, slevel));
                        }
                     }

                     if (nEquip.getWatk() > 0) {
                        slevel = GameConstants.getChaosNumber(scrollId.getItemId());
                        if (slevel != 999) {
                           nEquip.addWatk((short)slevel);
                           if (zeroEquip != null) {
                              zeroEquip.addWatk((short)slevel);
                           }

                           list1.add(new Pair(EnchantFlag.Watk, slevel));
                        }
                     }

                     if (nEquip.getMatk() > 0) {
                        slevel = GameConstants.getChaosNumber(scrollId.getItemId());
                        if (slevel != 999) {
                           nEquip.addMatk((short)slevel);
                           if (zeroEquip != null) {
                              zeroEquip.addMatk((short)slevel);
                           }

                           list1.add(new Pair(EnchantFlag.Matk, slevel));
                        }
                     }

                     if (nEquip.getHp() > 0) {
                        slevel = GameConstants.getChaosNumber(scrollId.getItemId()) * 10;
                        if (slevel != 999) {
                           nEquip.addHp((short)slevel);
                           if (zeroEquip != null) {
                              zeroEquip.addHp((short)slevel);
                           }

                           list1.add(new Pair(EnchantFlag.Hp, slevel));
                        }
                     }

                     if (nEquip.getMp() > 0) {
                        slevel = GameConstants.getChaosNumber(scrollId.getItemId()) * 10;
                        if (slevel != 999) {
                           nEquip.addMp((short)slevel);
                           if (zeroEquip != null) {
                              zeroEquip.addMp((short)slevel);
                           }

                           list1.add(new Pair(EnchantFlag.Mp, slevel));
                        }
                     }

                     equip.setShowScrollOption(new StarForceStats(list1));
                  }
               }
            } else {
               byte state;
               if (scrollId.getItemId() != 2049360 && scrollId.getItemId() != 2049361) {
                  int maxEnhance;
                  if (GameConstants.isStarForceScroll(scrollId.getItemId()) <= 0) {
                     if (GameConstants.isEquipScroll(scrollId.getItemId())) {
                        if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
                           if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr, nEquip))) {
                              if (!ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                                 return null;
                              }

                              chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
                           }

                           failed = true;
                        } else {
                           for(maxEnhance = 1; maxEnhance <= MapleDataTool.getIntConvert("info/forceUpgrade", this.getItemData(scrollId.getItemId()), 1); ++maxEnhance) {
                              if (GameConstants.isSuperior(nEquip.getItemId())) {
                                 slevel = this.getReqLevel(nEquip.getItemId());
                                 int senhance = nEquip.getEnhance();
                                 if (senhance < 1) {
                                    nEquip.setStr((short)(nEquip.getStr() + (slevel > 70 ? 2 : (slevel > 100 ? 9 : (slevel > 140 ? 19 : 1)))));
                                    nEquip.setDex((short)(nEquip.getDex() + (slevel > 70 ? 2 : (slevel > 100 ? 9 : (slevel > 140 ? 19 : 1)))));
                                    nEquip.setInt((short)(nEquip.getInt() + (slevel > 70 ? 2 : (slevel > 100 ? 9 : (slevel > 140 ? 19 : 1)))));
                                    nEquip.setLuk((short)(nEquip.getLuk() + (slevel > 70 ? 2 : (slevel > 100 ? 9 : (slevel > 140 ? 19 : 1)))));
                                    nEquip.setEnhance((byte)1);
                                 } else if (senhance == 1) {
                                    nEquip.setStr((short)(nEquip.getStr() + (slevel > 70 ? 3 : (slevel > 100 ? 10 : (slevel > 140 ? 20 : 2)))));
                                    nEquip.setDex((short)(nEquip.getDex() + (slevel > 70 ? 3 : (slevel > 100 ? 10 : (slevel > 140 ? 20 : 2)))));
                                    nEquip.setInt((short)(nEquip.getInt() + (slevel > 70 ? 3 : (slevel > 100 ? 10 : (slevel > 140 ? 20 : 2)))));
                                    nEquip.setLuk((short)(nEquip.getLuk() + (slevel > 70 ? 3 : (slevel > 100 ? 10 : (slevel > 140 ? 20 : 2)))));
                                    nEquip.setEnhance((byte)2);
                                 } else if (senhance == 2) {
                                    nEquip.setStr((short)(nEquip.getStr() + (slevel > 70 ? 5 : (slevel > 100 ? 12 : (slevel > 140 ? 22 : 4)))));
                                    nEquip.setDex((short)(nEquip.getDex() + (slevel > 70 ? 5 : (slevel > 100 ? 12 : (slevel > 140 ? 22 : 4)))));
                                    nEquip.setInt((short)(nEquip.getInt() + (slevel > 70 ? 5 : (slevel > 100 ? 12 : (slevel > 140 ? 22 : 4)))));
                                    nEquip.setLuk((short)(nEquip.getLuk() + (slevel > 70 ? 5 : (slevel > 100 ? 12 : (slevel > 140 ? 22 : 4)))));
                                    nEquip.setEnhance((byte)3);
                                 } else if (senhance == 3) {
                                    nEquip.setStr((short)(nEquip.getStr() + (slevel > 70 ? 8 : (slevel > 100 ? 15 : (slevel > 140 ? 25 : 7)))));
                                    nEquip.setDex((short)(nEquip.getDex() + (slevel > 70 ? 8 : (slevel > 100 ? 15 : (slevel > 140 ? 25 : 7)))));
                                    nEquip.setInt((short)(nEquip.getInt() + (slevel > 70 ? 8 : (slevel > 100 ? 15 : (slevel > 140 ? 25 : 7)))));
                                    nEquip.setLuk((short)(nEquip.getLuk() + (slevel > 70 ? 8 : (slevel > 100 ? 15 : (slevel > 140 ? 25 : 7)))));
                                    nEquip.setEnhance((byte)4);
                                 } else if (senhance == 4) {
                                    nEquip.setStr((short)(nEquip.getStr() + (slevel > 70 ? 12 : (slevel > 100 ? 19 : (slevel > 140 ? 29 : 11)))));
                                    nEquip.setDex((short)(nEquip.getDex() + (slevel > 70 ? 12 : (slevel > 100 ? 19 : (slevel > 140 ? 29 : 11)))));
                                    nEquip.setInt((short)(nEquip.getInt() + (slevel > 70 ? 12 : (slevel > 100 ? 19 : (slevel > 140 ? 29 : 11)))));
                                    nEquip.setLuk((short)(nEquip.getLuk() + (slevel > 70 ? 12 : (slevel > 100 ? 19 : (slevel > 140 ? 29 : 11)))));
                                    nEquip.setEnhance((byte)5);
                                 } else if (senhance == 5) {
                                    nEquip.setWatk((short)(nEquip.getWatk() + (slevel > 70 ? 2 : (slevel > 100 ? 5 : (slevel > 140 ? 9 : 2)))));
                                    nEquip.setMatk((short)(nEquip.getMatk() + (slevel > 70 ? 2 : (slevel > 100 ? 5 : (slevel > 140 ? 9 : 2)))));
                                    nEquip.setEnhance((byte)6);
                                 } else if (senhance == 6) {
                                    nEquip.setWatk((short)(nEquip.getWatk() + (slevel > 70 ? 3 : (slevel > 100 ? 6 : (slevel > 140 ? 10 : 3)))));
                                    nEquip.setMatk((short)(nEquip.getMatk() + (slevel > 70 ? 3 : (slevel > 100 ? 6 : (slevel > 140 ? 10 : 3)))));
                                    nEquip.setEnhance((byte)7);
                                 } else if (senhance == 7) {
                                    nEquip.setWatk((short)(nEquip.getWatk() + (slevel > 70 ? 4 : (slevel > 100 ? 7 : (slevel > 140 ? 11 : 5)))));
                                    nEquip.setMatk((short)(nEquip.getMatk() + (slevel > 70 ? 4 : (slevel > 100 ? 7 : (slevel > 140 ? 11 : 5)))));
                                    nEquip.setEnhance((byte)8);
                                 } else if (senhance == 8) {
                                    nEquip.setWatk((short)(nEquip.getWatk() + (slevel > 70 ? 5 : (slevel > 100 ? 8 : (slevel > 140 ? 12 : 8)))));
                                    nEquip.setMatk((short)(nEquip.getMatk() + (slevel > 70 ? 5 : (slevel > 100 ? 8 : (slevel > 140 ? 12 : 8)))));
                                    nEquip.setEnhance((byte)9);
                                 } else if (senhance == 9) {
                                    nEquip.setWatk((short)(nEquip.getWatk() + (slevel > 70 ? 6 : (slevel > 100 ? 9 : (slevel > 140 ? 13 : 12)))));
                                    nEquip.setMatk((short)(nEquip.getMatk() + (slevel > 70 ? 6 : (slevel > 100 ? 9 : (slevel > 140 ? 13 : 12)))));
                                    nEquip.setEnhance((byte)10);
                                 } else {
                                    nEquip.setStr((short)(nEquip.getStr() + (slevel > 70 ? 15 : (slevel > 100 ? 20 : (slevel > 140 ? 30 : 10)))));
                                    nEquip.setDex((short)(nEquip.getDex() + (slevel > 70 ? 15 : (slevel > 100 ? 20 : (slevel > 140 ? 30 : 10)))));
                                    nEquip.setInt((short)(nEquip.getInt() + (slevel > 70 ? 15 : (slevel > 100 ? 20 : (slevel > 140 ? 30 : 10)))));
                                    nEquip.setLuk((short)(nEquip.getLuk() + (slevel > 70 ? 15 : (slevel > 100 ? 20 : (slevel > 140 ? 30 : 10)))));
                                    nEquip.setEnhance((byte)(nEquip.getEnhance() + 1));
                                 }
                              } else {
                                 if (nEquip.getStr() > 0) {
                                    nEquip.setStr((short)(nEquip.getStr() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(0, 1))));
                                 }

                                 if (nEquip.getDex() > 0) {
                                    nEquip.setDex((short)(nEquip.getDex() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(0, 1))));
                                 }

                                 if (nEquip.getInt() > 0) {
                                    nEquip.setInt((short)(nEquip.getInt() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(0, 1))));
                                 }

                                 if (nEquip.getLuk() > 0) {
                                    nEquip.setLuk((short)(nEquip.getLuk() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(0, 1))));
                                 }

                                 if (nEquip.getWatk() > 0) {
                                    nEquip.setWatk((short)(nEquip.getWatk() + getEquipLevel(this.getReqLevel(nEquip.getItemId()))));
                                 }

                                 if (nEquip.getWdef() > 0) {
                                    nEquip.setWdef((short)(nEquip.getWdef() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(1, 2))));
                                 }

                                 if (nEquip.getMatk() > 0) {
                                    nEquip.setMatk((short)(nEquip.getMatk() + getEquipLevel(this.getReqLevel(nEquip.getItemId()))));
                                 }

                                 if (nEquip.getMdef() > 0) {
                                    nEquip.setMdef((short)(nEquip.getMdef() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(1, 2))));
                                 }

                                 if (nEquip.getAcc() > 0) {
                                    nEquip.setAcc((short)(nEquip.getAcc() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(1, 2))));
                                 }

                                 if (nEquip.getAvoid() > 0) {
                                    nEquip.setAvoid((short)(nEquip.getAvoid() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(1, 2))));
                                 }

                                 if (nEquip.getHp() > 0) {
                                    nEquip.setHp((short)(nEquip.getHp() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(1, 2))));
                                 }

                                 if (nEquip.getMp() > 0) {
                                    nEquip.setMp((short)(nEquip.getMp() + getEquipLevel(this.getReqLevel(nEquip.getItemId()) + Randomizer.rand(1, 2))));
                                 }

                                 nEquip.setEnhance((byte)(nEquip.getEnhance() + 1));
                              }
                           }
                        }
                     } else if (GameConstants.isPotentialScroll(scrollId.getItemId())) {
                        if (nEquip.getState() == 0) {
                           if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
                              if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr)) && ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                                 chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
                              }

                              failed = true;
                           } else {
                              state = 1;
                              switch(scrollId.getItemId()) {
                              case 2049762:
                              case 2079790:
                                 state = 3;
                              default:
                                 nEquip.setState((byte)state);
                                 if (zeroEquip != null) {
                                    zeroEquip.setState((byte)state);
                                 }
                              }
                           }
                        }
                     } else {
                        if (GameConstants.isRebirthFireScroll(scrollId.getItemId())) {
                           if (GameConstants.isZero(chr.getJob()) && nEquip.getPosition() == -11) {
                              nEquip.resetRebirth(this.getReqLevel(nEquip.getItemId()));
                              if (zeroEquip != null) {
                                 zeroEquip.resetRebirth(this.getReqLevel(nEquip.getItemId()));
                                 zeroEquip.setZeroRebirth(chr, this.getReqLevel(zeroEquip.getItemId()), scrollId.getItemId());
                              }
                           } else {
                              nEquip.resetRebirth(this.getReqLevel(nEquip.getItemId()));
                              nEquip.setFire(nEquip.newRebirth(this.getReqLevel(nEquip.getItemId()), scrollId.getItemId(), true));
                           }

                           return nEquip;
                        }

                        if (!Randomizer.isSuccess(this.getSuccess(scrollId.getItemId(), chr, nEquip))) {
                           if (Randomizer.isSuccess(this.getCursed(scrollId.getItemId(), chr)) && ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                              chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
                           }

                           failed = true;
                        } else {
                           Iterator var59 = stats.entrySet().iterator();

                           while(var59.hasNext()) {
                              Entry<String, Integer> stat = (Entry)var59.next();
                              String key = (String)stat.getKey();
                              if (key.equals("STR")) {
                                 nEquip.setStr((short)(nEquip.getStr() + (Integer)stat.getValue()));
                              } else if (key.equals("DEX")) {
                                 nEquip.setDex((short)(nEquip.getDex() + (Integer)stat.getValue()));
                              } else if (key.equals("INT")) {
                                 nEquip.setInt((short)(nEquip.getInt() + (Integer)stat.getValue()));
                              } else if (key.equals("LUK")) {
                                 nEquip.setLuk((short)(nEquip.getLuk() + (Integer)stat.getValue()));
                              } else if (key.equals("PAD")) {
                                 nEquip.setWatk((short)(nEquip.getWatk() + (Integer)stat.getValue()));
                              } else if (key.equals("PDD")) {
                                 nEquip.setWdef((short)(nEquip.getWdef() + (Integer)stat.getValue()));
                              } else if (key.equals("MAD")) {
                                 nEquip.setMatk((short)(nEquip.getMatk() + (Integer)stat.getValue()));
                              } else if (key.equals("MDD")) {
                                 nEquip.setMdef((short)(nEquip.getMdef() + (Integer)stat.getValue()));
                              } else if (key.equals("ACC")) {
                                 nEquip.setAcc((short)(nEquip.getAcc() + (Integer)stat.getValue()));
                              } else if (key.equals("EVA")) {
                                 nEquip.setAvoid((short)(nEquip.getAvoid() + (Integer)stat.getValue()));
                              } else if (key.equals("Speed")) {
                                 nEquip.setSpeed((short)(nEquip.getSpeed() + (Integer)stat.getValue()));
                              } else if (key.equals("Jump")) {
                                 nEquip.setJump((short)(nEquip.getJump() + (Integer)stat.getValue()));
                              } else if (key.equals("MHP")) {
                                 nEquip.setHp((short)(nEquip.getHp() + (Integer)stat.getValue()));
                              } else if (key.equals("MMP")) {
                                 nEquip.setMp((short)(nEquip.getMp() + (Integer)stat.getValue()));
                              }

                              if (zeroEquip != null) {
                                 if (key.equals("STR")) {
                                    zeroEquip.setStr((short)(zeroEquip.getStr() + (Integer)stat.getValue()));
                                 } else if (key.equals("DEX")) {
                                    zeroEquip.setDex((short)(zeroEquip.getDex() + (Integer)stat.getValue()));
                                 } else if (key.equals("INT")) {
                                    zeroEquip.setInt((short)(zeroEquip.getInt() + (Integer)stat.getValue()));
                                 } else if (key.equals("LUK")) {
                                    zeroEquip.setLuk((short)(zeroEquip.getLuk() + (Integer)stat.getValue()));
                                 } else if (key.equals("PAD")) {
                                    zeroEquip.setWatk((short)(zeroEquip.getWatk() + (Integer)stat.getValue()));
                                 } else if (key.equals("PDD")) {
                                    zeroEquip.setWdef((short)(zeroEquip.getWdef() + (Integer)stat.getValue()));
                                 } else if (key.equals("MAD")) {
                                    zeroEquip.setMatk((short)(zeroEquip.getMatk() + (Integer)stat.getValue()));
                                 } else if (key.equals("MDD")) {
                                    zeroEquip.setMdef((short)(zeroEquip.getMdef() + (Integer)stat.getValue()));
                                 } else if (key.equals("ACC")) {
                                    zeroEquip.setAcc((short)(zeroEquip.getAcc() + (Integer)stat.getValue()));
                                 } else if (key.equals("EVA")) {
                                    zeroEquip.setAvoid((short)(zeroEquip.getAvoid() + (Integer)stat.getValue()));
                                 } else if (key.equals("Speed")) {
                                    zeroEquip.setSpeed((short)(zeroEquip.getSpeed() + (Integer)stat.getValue()));
                                 } else if (key.equals("Jump")) {
                                    zeroEquip.setJump((short)(zeroEquip.getJump() + (Integer)stat.getValue()));
                                 } else if (key.equals("MHP")) {
                                    zeroEquip.setHp((short)(zeroEquip.getHp() + (Integer)stat.getValue()));
                                 } else if (key.equals("MMP")) {
                                    zeroEquip.setMp((short)(zeroEquip.getMp() + (Integer)stat.getValue()));
                                 }
                              }
                           }
                        }
                     }
                  } else {
                     slevel = GameConstants.isStarForceScroll(scrollId.getItemId());
                     boolean isSuperiol = this.isSuperial(nEquip.getItemId()).left != null;
                     int reqLevel = this.getReqLevel(nEquip.getItemId());
                     if (reqLevel < 95) {
                        maxEnhance = isSuperiol ? 3 : 5;
                     } else if (reqLevel <= 107) {
                        maxEnhance = isSuperiol ? 5 : 8;
                     } else if (reqLevel <= 119) {
                        maxEnhance = isSuperiol ? 8 : 10;
                     } else if (reqLevel <= 129) {
                        maxEnhance = isSuperiol ? 10 : 15;
                     } else if (reqLevel <= 139) {
                        maxEnhance = isSuperiol ? 12 : 20;
                     } else {
                        maxEnhance = isSuperiol ? 15 : 25;
                     }

                     if (maxEnhance < slevel) {
                        slevel = maxEnhance;
                     }

                     while(true) {
                        if (nEquip.getEnhance() >= slevel) {
                           EquipmentEnchant.checkEquipmentStats(chr.getClient(), nEquip);
                           if (zeroEquip != null) {
                              EquipmentEnchant.checkEquipmentStats(chr.getClient(), zeroEquip);
                           }
                           break;
                        }

                        StarForceStats starForceStats = EquipmentEnchant.starForceStats(nEquip);
                        nEquip.setEnchantBuff((short)0);
                        nEquip.setEnhance((byte)(nEquip.getEnhance() + 1));
                        Iterator var53 = starForceStats.getStats().iterator();

                        while(var53.hasNext()) {
                           Pair<EnchantFlag, Integer> stat = (Pair)var53.next();
                           if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantWatk((short)(nEquip.getEnchantWatk() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantWatk((short)(zeroEquip.getEnchantWatk() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantMatk((short)(nEquip.getEnchantMatk() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantMatk((short)(zeroEquip.getEnchantMatk() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantStr((short)(nEquip.getEnchantStr() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantStr((short)(zeroEquip.getEnchantStr() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantDex((short)(nEquip.getEnchantDex() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantDex((short)(zeroEquip.getEnchantDex() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantInt((short)(nEquip.getEnchantInt() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantInt((short)(zeroEquip.getEnchantInt() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantLuk((short)(nEquip.getEnchantLuk() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantLuk((short)(zeroEquip.getEnchantLuk() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantWdef((short)(nEquip.getEnchantWdef() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantWdef((short)(zeroEquip.getEnchantWdef() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantMdef((short)(nEquip.getEnchantMdef() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantMdef((short)(zeroEquip.getEnchantMdef() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantHp((short)(nEquip.getEnchantHp() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantHp((short)(zeroEquip.getEnchantHp() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantMp((short)(nEquip.getEnchantMp() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantMp((short)(zeroEquip.getEnchantMp() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantAcc((short)(nEquip.getEnchantAcc() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantAcc((short)(zeroEquip.getEnchantAcc() + (Integer)stat.right));
                              }
                           }

                           if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                              nEquip.setEnchantAvoid((short)(nEquip.getEnchantAvoid() + (Integer)stat.right));
                              if (zeroEquip != null) {
                                 zeroEquip.setEnchantAvoid((short)(zeroEquip.getEnchantAvoid() + (Integer)stat.right));
                              }
                           }
                        }
                     }
                  }
               } else {
                  MapleData IData = this.getItemData(nEquip.getItemId());
                  MapleData info = IData.getChildByPath("info");
                  int level = MapleDataTool.getInt("reqLevel", info, 0);
                  if (level > 150) {
                     chr.dropMessage(6, "150레벨 이하의 장비 아이템에만 사용하실 수 있습니다.");
                  } else {
                     switch(nEquip.getEnhance()) {
                     case 0:
                        state = 60;
                        break;
                     case 1:
                        state = 55;
                        break;
                     case 2:
                        state = 50;
                        break;
                     case 3:
                        state = 40;
                        break;
                     case 4:
                        state = 30;
                        break;
                     case 5:
                        state = 20;
                        break;
                     case 6:
                        state = 19;
                        break;
                     case 7:
                        state = 18;
                        break;
                     case 8:
                        state = 17;
                        break;
                     case 9:
                        state = 16;
                        break;
                     case 10:
                        state = 14;
                        break;
                     case 11:
                        state = 12;
                        break;
                     default:
                        state = 10;
                     }

                     if (chr.getGMLevel() > 0) {
                        state = 100;
                     }

                     if (!Randomizer.isSuccess(state)) {
                        if (!ItemFlag.PROTECT_SHIELD.check(nEquip.getFlag())) {
                           return null;
                        }

                        chr.dropMessage(5, "주문서의 효과로 아이템이 파괴되지 않았습니다.");
                     } else {
                        if (EquipmentEnchant.isMagicWeapon(GameConstants.getWeaponType(nEquip.getItemId()))) {
                           slevel = nEquip.getMatk() + nEquip.getEnchantMatk();
                        } else {
                           slevel = nEquip.getWatk() + nEquip.getEnchantWatk();
                        }

                        if (nEquip.getFire() > 0L) {
                           long fire1 = nEquip.getFire() % 1000L / 10L;
                           long fire2 = nEquip.getFire() % 1000000L / 10000L;
                           long fire3 = nEquip.getFire() % 1000000000L / 10000000L;
                           long fire4 = nEquip.getFire() % 1000000000000L / 10000000000L;

                           for(int i = 0; i < 4; ++i) {
                              int dat = (int)(i == 0 ? fire1 : (i == 1 ? fire2 : (i == 2 ? fire3 : fire4)));
                              if (dat == (EquipmentEnchant.isMagicWeapon(GameConstants.getWeaponType(nEquip.getItemId())) ? 18 : 17)) {
                                 int value;
                                 if (i == 0) {
                                    value = (int)(nEquip.getFire() % 10L / 1L);
                                 } else if (i == 1) {
                                    value = (int)(nEquip.getFire() % 10000L / 1000L);
                                 } else if (i == 2) {
                                    value = (int)(nEquip.getFire() % 10000000L / 1000000L);
                                 } else {
                                    value = (int)(nEquip.getFire() % 10000000000L / 1000000000L);
                                 }

                                 switch(value) {
                                 case 3:
                                    if (this.getReqLevel(nEquip.getItemId()) <= 150) {
                                       slevel -= (short)(slevel * 1200 / 10000 + 1);
                                    } else if (this.getReqLevel(nEquip.getItemId()) <= 160) {
                                       slevel -= (short)(slevel * 1500 / 10000 + 1);
                                    } else {
                                       slevel -= (short)(slevel * 1800 / 10000 + 1);
                                    }
                                    break;
                                 case 4:
                                    if (this.getReqLevel(nEquip.getItemId()) <= 150) {
                                       slevel -= (short)(slevel * 1760 / 10000 + 1);
                                    } else if (this.getReqLevel(nEquip.getItemId()) <= 160) {
                                       slevel -= (short)(slevel * 2200 / 10000 + 1);
                                    } else {
                                       slevel -= (short)(slevel * 2640 / 10000 + 1);
                                    }
                                    break;
                                 case 5:
                                    if (this.getReqLevel(nEquip.getItemId()) <= 150) {
                                       slevel -= (short)(slevel * 2420 / 10000 + 1);
                                    } else if (this.getReqLevel(nEquip.getItemId()) <= 160) {
                                       slevel -= (short)(slevel * 3025 / 10000 + 1);
                                    } else {
                                       slevel -= (short)(slevel * 3630 / 10000 + 1);
                                    }
                                    break;
                                 case 6:
                                    if (this.getReqLevel(nEquip.getItemId()) <= 150) {
                                       slevel -= (short)(slevel * 3200 / 10000 + 1);
                                    } else if (this.getReqLevel(nEquip.getItemId()) <= 160) {
                                       slevel -= (short)(slevel * 4000 / 10000 + 1);
                                    } else {
                                       slevel -= (short)(slevel * 4800 / 10000 + 1);
                                    }
                                    break;
                                 case 7:
                                    if (this.getReqLevel(nEquip.getItemId()) <= 150) {
                                       slevel -= (short)(slevel * 4100 / 10000 + 1);
                                    } else if (this.getReqLevel(nEquip.getItemId()) <= 160) {
                                       slevel -= (short)(slevel * 5125 / 10000 + 1);
                                    } else {
                                       slevel -= (short)(slevel * 6150 / 10000 + 1);
                                    }
                                 }
                              }
                           }
                        }

                        int weaponwatk = slevel / 50 + 1;
                        int weaponmatk = slevel / 50 + 1;
                        int reallevel = level / 10 * 10;
                        int[] data;
                        switch(reallevel) {
                        case 80:
                           data = new int[]{2, 3, 5, 8, 12, 2, 3, 4, 5, 6, 7, 9, 10, 11};
                           break;
                        case 90:
                           data = new int[]{4, 5, 7, 10, 14, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13};
                           break;
                        case 100:
                           data = new int[]{7, 8, 10, 13, 17, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14};
                           break;
                        case 110:
                           data = new int[]{9, 10, 12, 15, 19, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15};
                           break;
                        case 120:
                           data = new int[]{12, 13, 15, 18, 22, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16};
                           break;
                        case 130:
                           data = new int[]{14, 15, 17, 20, 24, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17};
                           break;
                        case 140:
                           data = new int[]{17, 18, 20, 23, 27, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18};
                           break;
                        case 150:
                           data = new int[]{19, 20, 22, 25, 29, 9, 10, 11, 12, 13, 14, 16, 17, 18, 19};
                           break;
                        default:
                           data = new int[]{1, 2, 4, 7, 11, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11};
                        }

                        if (nEquip.getEnhance() < 5) {
                           nEquip.addStr((short)data[nEquip.getEnhance()]);
                           nEquip.addDex((short)data[nEquip.getEnhance()]);
                           nEquip.addInt((short)data[nEquip.getEnhance()]);
                           nEquip.addLuk((short)data[nEquip.getEnhance()]);
                        } else {
                           nEquip.addWatk((short)data[nEquip.getEnhance()]);
                           nEquip.addMatk((short)data[nEquip.getEnhance()]);
                        }

                        if (GameConstants.isWeapon(nEquip.getItemId())) {
                           nEquip.addWatk((short)weaponwatk);
                           nEquip.addMatk((short)weaponmatk);
                           if (Randomizer.nextBoolean()) {
                              nEquip.addWatk((short)1);
                              nEquip.addMatk((short)1);
                           }
                        } else if (GameConstants.isAccessory(nEquip.getItemId()) && Randomizer.nextBoolean()) {
                           if (level < 120) {
                              if (nEquip.getEnhance() < 5) {
                                 nEquip.addStr((short)1);
                                 nEquip.addDex((short)1);
                                 nEquip.addInt((short)1);
                                 nEquip.addLuk((short)1);
                              } else {
                                 nEquip.addStr((short)2);
                                 nEquip.addDex((short)2);
                                 nEquip.addInt((short)2);
                                 nEquip.addLuk((short)2);
                              }
                           } else if (nEquip.getEnhance() < 5) {
                              nEquip.addStr((short)Randomizer.rand(1, 2));
                              nEquip.addDex((short)Randomizer.rand(1, 2));
                              nEquip.addInt((short)Randomizer.rand(1, 2));
                              nEquip.addLuk((short)Randomizer.rand(1, 2));
                           } else {
                              nEquip.addStr((short)2);
                              nEquip.addDex((short)2);
                              nEquip.addInt((short)2);
                              nEquip.addLuk((short)2);
                           }
                        }

                        nEquip.setEnhance((byte)(nEquip.getEnhance() + 1));
                        nEquip.setEquipmentType(nEquip.getEquipmentType() | 1536);
                     }
                  }
               }
            }
         }

         if (!GameConstants.isCleanSlate(scrollId.getItemId()) && !GameConstants.isSpecialScroll(scrollId.getItemId()) && !GameConstants.isEquipScroll(scrollId.getItemId()) && !GameConstants.isPotentialScroll(scrollId.getItemId()) && !GameConstants.isRebirthFireScroll(scrollId.getItemId()) && scrollId.getItemId() != 2049360 && scrollId.getItemId() != 2049361) {
            if (ItemFlag.SAFETY_SHIELD.check(nEquip.getFlag()) && failed) {
               chr.dropMessage(5, "주문서의 효과로 업그레이드 가능 횟수가 차감되지 않았습니다.");
            } else {
               nEquip.setUpgradeSlots((byte)(nEquip.getUpgradeSlots() - this.getUpgradeScrollUseSlot(scrollId.getItemId())));
               if (zeroEquip != null) {
                  zeroEquip.setUpgradeSlots((byte)(zeroEquip.getUpgradeSlots() - this.getUpgradeScrollUseSlot(scrollId.getItemId())));
               }
            }

            if (!failed) {
               nEquip.setLevel((byte)(nEquip.getLevel() + 1));
               if (zeroEquip != null) {
                  zeroEquip.setLevel((byte)(zeroEquip.getLevel() + 1));
                  chr.getClient().send(CWvsContext.InventoryPacket.addInventorySlot(MapleInventoryType.EQUIPPED, zeroEquip));
               }
            }
         }
      }

      return equip;
   }

   private static int getEquipLevel(int level) {
      int stat = false;
      byte stat;
      if (level >= 0 && level <= 50) {
         stat = 1;
      } else if (level >= 51 && level <= 100) {
         stat = 2;
      } else {
         stat = 3;
      }

      return stat;
   }

   public final int getSuccess(int itemId, MapleCharacter player, Item equip) {
      if (player.getGMLevel() > 0) {
         return 100;
      } else if (equip == null) {
         System.err.println("[오류] 주문서의 성공확률을 구하던 중, 장비 아이템 값에 널 값이 입력되었습니다." + itemId);
         player.dropMessage(5, "[오류] 현재 주문서의 성공확률을 구하는데 실패하였습니다.");
         player.gainItem(itemId, (short)1, false, -1L, "주문서 성공확률 얻기 실패로 얻은 주문서");
         player.getClient().getSession().writeAndFlush(CWvsContext.enableActions(player));
         return 0;
      } else {
         Equip t = (Equip)equip.copy();
         if (itemId / 100 == 20493) {
            int i = 0;
            Equip lev = (Equip)equip.copy();
            byte leve = lev.getEnhance();
            switch(itemId) {
            case 2049300:
            case 2049303:
            case 2049306:
            case 2049323:
               if (leve == 0) {
                  i = 100;
               } else if (leve == 1) {
                  i = 90;
               } else if (leve == 2) {
                  i = 80;
               } else if (leve == 3) {
                  i = 70;
               } else if (leve == 4) {
                  i = 60;
               } else if (leve == 5) {
                  i = 50;
               } else if (leve == 6) {
                  i = 40;
               } else if (leve == 7) {
                  i = 30;
               } else if (leve == 8) {
                  i = 20;
               } else if (leve == 9) {
                  i = 10;
               } else if (leve >= 10) {
                  i = 5;
               }

               return i;
            case 2049301:
            case 2049307:
               if (leve == 0) {
                  i = 80;
               } else if (leve == 1) {
                  i = 70;
               } else if (leve == 2) {
                  i = 60;
               } else if (leve == 3) {
                  i = 50;
               } else if (leve == 4) {
                  i = 40;
               } else if (leve == 5) {
                  i = 30;
               } else if (leve == 6) {
                  i = 20;
               } else if (leve == 7) {
                  i = 10;
               } else if (leve >= 8) {
                  i = 5;
               }

               return i;
            }
         }

         switch(itemId) {
         case 2046841:
         case 2046842:
         case 2046967:
         case 2046971:
         case 2047803:
         case 2047917:
            return 20;
         default:
            if (this.successCache.containsKey(itemId)) {
               return (Integer)this.successCache.get(itemId);
            } else {
               MapleData item = this.getItemData(itemId);
               if (item == null) {
                  System.err.println("[오류] 주문서의 성공확률을 구하던 중, 주문서 데이터 값에 널 값이 입력되었습니다." + itemId);
                  player.dropMessage(5, "[오류] 현재 주문서의 성공확률을 구하는데 실패하였습니다.");
                  player.gainItem(itemId, (short)1, false, -1L, "주문서 성공확률 얻기 실패로 얻은 주문서");
                  player.getClient().getSession().writeAndFlush(CWvsContext.enableActions(player));
                  return 0;
               } else {
                  int success = false;
                  int success;
                  if (item.getChildByPath("info/successRates") != null) {
                     success = MapleDataTool.getIntConvert(t.getLevel().makeConcatWithConstants<invokedynamic>(t.getLevel()), item.getChildByPath("info/successRates"), 20);
                  } else {
                     success = MapleDataTool.getIntConvert("info/success", item, 100);
                  }

                  if (!GameConstants.isPotentialScroll(itemId) && !GameConstants.isEquipScroll(itemId) && ItemFlag.LUCKY_PROTECT_SHIELD.check(t.getFlag())) {
                     success += 10;
                  }

                  this.successCache.put(itemId, success);
                  return success;
               }
            }
         }
      }
   }

   public final Item getEquipById(int equipId) {
      return this.getEquipById(equipId, -1L, true);
   }

   public final Item getEquipById(int equipId, boolean rebirth) {
      return this.getEquipById(equipId, -1L, rebirth);
   }

   public final Item getEquipById(int equipId, long ringId) {
      return this.getEquipById(equipId, ringId, true);
   }

   public final Item getEquipById(int equipId, long ringId, boolean rebirth) {
      ItemInformation i = this.getItemInformation(equipId);
      if (i == null) {
         return new Equip(equipId, (short)0, ringId, 0);
      } else {
         Item eq = i.eq.copy();
         eq.setUniqueId(ringId);
         Equip eqz = (Equip)eq;
         if (!this.isCash(equipId) && rebirth) {
            eqz.setFire(eqz.newRebirth(this.getReqLevel(equipId), 0, true));
            if (ItemFlag.UNTRADEABLE.check(eqz.getFlag()) && eqz.getKarmaCount() < 0 && (this.isKarmaEnabled(equipId) || this.isPKarmaEnabled(equipId))) {
               eqz.setKarmaCount((byte)10);
            }
         }

         return eq;
      }
   }

   public final int getTotalStat(Equip equip) {
      return equip.getStr() + equip.getDex() + equip.getInt() + equip.getLuk() + equip.getMatk() + equip.getWatk() + equip.getAcc() + equip.getAvoid() + equip.getJump() + equip.getHands() + equip.getSpeed() + equip.getHp() + equip.getMp() + equip.getWdef() + equip.getMdef();
   }

   public final SecondaryStatEffect getItemEffect(int itemId) {
      SecondaryStatEffect ret = (SecondaryStatEffect)this.itemEffects.get(itemId);
      if (ret == null) {
         MapleData item = this.getItemData(itemId);
         if (item == null || item.getChildByPath("spec") == null) {
            return null;
         }

         ret = SecondaryStatEffect.loadItemEffectFromData(item.getChildByPath("spec"), itemId);
         this.itemEffects.put(itemId, ret);
      }

      return ret;
   }

   public final SecondaryStatEffect getItemEffectEX(int itemId) {
      SecondaryStatEffect ret = (SecondaryStatEffect)this.itemEffectsEx.get(itemId);
      if (ret == null) {
         MapleData item = this.getItemData(itemId);
         if (item == null || item.getChildByPath("specEx") == null) {
            return null;
         }

         ret = SecondaryStatEffect.loadItemEffectFromData(item.getChildByPath("specEx"), itemId);
         this.itemEffectsEx.put(itemId, ret);
      }

      return ret;
   }

   public final int getCreateId(int id) {
      ItemInformation i = this.getItemInformation(id);
      return i == null ? 0 : i.create;
   }

   public final int getCardMobId(int id) {
      ItemInformation i = this.getItemInformation(id);
      return i == null ? 0 : i.monsterBook;
   }

   public final int getBagType(int id) {
      ItemInformation i = this.getItemInformation(id);
      return i == null ? 0 : i.flag & 15;
   }

   public final int getWatkForProjectile(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i != null && i.equipStats != null && i.equipStats.get("incPAD") != null ? (Integer)i.equipStats.get("incPAD") : 0;
   }

   public final boolean canScroll(int scrollid, int itemid) {
      return scrollid / 100 % 100 == itemid / 10000 % 100;
   }

   public final String getName(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : i.name;
   }

   public final String getDesc(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : i.desc;
   }

   public final String getMsg(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : i.msg;
   }

   public final short getItemMakeLevel(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? 0 : i.itemMakeLevel;
   }

   public final boolean isDropRestricted(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return ((i.flag & 512) != 0 || (i.flag & 1024) != 0 || GameConstants.isDropRestricted(itemId)) && (itemId == 3012000 || itemId == 3012015 || itemId / 10000 != 301) && itemId != 2041200 && itemId != 5640000 && itemId != 4170023 && itemId != 2040124 && itemId != 2040125 && itemId != 2040126 && itemId != 2040211 && itemId != 2040212 && itemId != 2040227 && itemId != 2040228 && itemId != 2040229 && itemId != 2040230 && itemId != 1002926 && itemId != 1002906 && itemId != 1002927;
      }
   }

   public final boolean isPickupRestricted(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return ((i.flag & 128) != 0 || GameConstants.isPickupRestricted(itemId)) && itemId != 4001168 && itemId != 4031306 && itemId != 4031307;
      }
   }

   public final boolean isAccountShared(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return (i.flag & 256) != 0;
      }
   }

   public final int getStateChangeItem(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? 0 : i.stateChange;
   }

   public final int getMeso(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? 0 : i.meso;
   }

   public final boolean isShareTagEnabled(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return (i.flag & 2048) != 0;
      }
   }

   public final boolean isKarmaEnabled(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return i.karmaEnabled == 1;
      }
   }

   public final boolean isPKarmaEnabled(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return i.karmaEnabled == 2;
      }
   }

   public final boolean isPickupBlocked(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return (i.flag & 64) != 0;
      }
   }

   public final boolean isLogoutExpire(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return (i.flag & 32) != 0;
      }
   }

   public final boolean cantSell(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return (i.flag & 16) != 0;
      }
   }

   public final Pair<Integer, List<StructRewardItem>> getRewardItem(int itemid) {
      ItemInformation i = this.getItemInformation(itemid);
      return i == null ? null : new Pair(i.totalprob, i.rewardItems);
   }

   public final boolean isMobHP(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return (i.flag & 4096) != 0;
      }
   }

   public final boolean isQuestItem(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      if (i == null) {
         return false;
      } else {
         return (i.flag & 512) != 0 && itemId / 10000 != 301;
      }
   }

   public final Pair<Integer, List<Integer>> questItemInfo(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : new Pair(i.questId, i.questItems);
   }

   public final Pair<Integer, String> replaceItemInfo(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : new Pair(i.replaceItem, i.replaceMsg);
   }

   public final List<Triple<String, Point, Point>> getAfterImage(String after) {
      return (List)this.afterImage.get(after);
   }

   public final String getAfterImage(int itemId) {
      ItemInformation i = this.getItemInformation(itemId);
      return i == null ? null : i.afterImage;
   }

   public final boolean isJokerToSetItem(int itemId) {
      return this.getEquipStats(itemId) == null ? false : this.getEquipStats(itemId).containsKey("jokerToSetItem");
   }

   public final boolean itemExists(int itemId) {
      if (GameConstants.getInventoryType(itemId) == MapleInventoryType.UNDEFINED) {
         return false;
      } else {
         return this.getItemInformation(itemId) != null;
      }
   }

   public final boolean isCash(int itemId) {
      return this.getEquipStats(itemId) != null && this.getEquipStats(itemId).get("cash") != null;
   }

   public final ItemInformation getItemInformation(int itemId) {
      return itemId <= 0 ? null : (ItemInformation)this.dataCache.get(itemId);
   }

   public void initItemRewardData(ResultSet sqlRewardData) throws SQLException {
      int itemID = sqlRewardData.getInt("itemid");
      if (this.tmpInfo == null || this.tmpInfo.itemId != itemID) {
         if (!this.dataCache.containsKey(itemID)) {
            System.out.println("[initItemRewardData] Tried to load an item while this is not in the cache: " + itemID);
            return;
         }

         this.tmpInfo = (ItemInformation)this.dataCache.get(itemID);
      }

      if (this.tmpInfo.rewardItems == null) {
         this.tmpInfo.rewardItems = new ArrayList();
      }

      StructRewardItem add = new StructRewardItem();
      add.itemid = sqlRewardData.getInt("item");
      add.period = (long)(add.itemid == 1122017 ? Math.max(sqlRewardData.getInt("period"), 7200) : sqlRewardData.getInt("period"));
      add.prob = sqlRewardData.getInt("prob");
      add.quantity = sqlRewardData.getShort("quantity");
      add.worldmsg = sqlRewardData.getString("worldMsg").length() <= 0 ? null : sqlRewardData.getString("worldMsg");
      add.effect = sqlRewardData.getString("effect");
      this.tmpInfo.rewardItems.add(add);
   }

   public void initItemAddData(ResultSet sqlAddData) throws SQLException {
      int itemID = sqlAddData.getInt("itemid");
      if (this.tmpInfo == null || this.tmpInfo.itemId != itemID) {
         if (!this.dataCache.containsKey(itemID)) {
            System.out.println("[initItemAddData] Tried to load an item while this is not in the cache: " + itemID);
            return;
         }

         this.tmpInfo = (ItemInformation)this.dataCache.get(itemID);
      }

      if (this.tmpInfo.equipAdditions == null) {
         this.tmpInfo.equipAdditions = new LinkedList();
      }

      while(sqlAddData.next()) {
         this.tmpInfo.equipAdditions.add(new Triple(sqlAddData.getString("key"), sqlAddData.getString("subKey"), sqlAddData.getString("value")));
      }

   }

   public void initItemEquipData(ResultSet sqlEquipData) throws SQLException {
      int itemID = sqlEquipData.getInt("itemid");
      if (this.tmpInfo == null || this.tmpInfo.itemId != itemID) {
         if (!this.dataCache.containsKey(itemID)) {
            System.out.println("[initItemEquipData] Tried to load an item while this is not in the cache: " + itemID);
            return;
         }

         this.tmpInfo = (ItemInformation)this.dataCache.get(itemID);
      }

      if (this.tmpInfo.equipStats == null) {
         this.tmpInfo.equipStats = new HashMap();
      }

      int itemLevel = sqlEquipData.getInt("itemLevel");
      if (itemLevel == -1) {
         this.tmpInfo.equipStats.put(sqlEquipData.getString("key"), sqlEquipData.getInt("value"));
      } else {
         if (this.tmpInfo.equipIncs == null) {
            this.tmpInfo.equipIncs = new HashMap();
         }

         Map<String, Integer> toAdd = (Map)this.tmpInfo.equipIncs.get(itemLevel);
         if (toAdd == null) {
            toAdd = new HashMap();
            this.tmpInfo.equipIncs.put(itemLevel, toAdd);
         }

         ((Map)toAdd).put(sqlEquipData.getString("key"), sqlEquipData.getInt("value"));
      }

   }

   public void finalizeEquipData(ItemInformation item) {
      int itemId = item.itemId;
      if (item.equipStats == null) {
         item.equipStats = new HashMap();
      }

      item.eq = new Equip(itemId, (short)0, -1L, 0);
      short stats = GameConstants.getStat(itemId, 0);
      if (stats > 0) {
         item.eq.setStr(stats);
         item.eq.setDex(stats);
         item.eq.setInt(stats);
         item.eq.setLuk(stats);
      }

      stats = GameConstants.getATK(itemId, 0);
      if (stats > 0) {
         item.eq.setWatk(stats);
         item.eq.setMatk(stats);
      }

      stats = GameConstants.getHpMp(itemId, 0);
      if (stats > 0) {
         item.eq.setHp(stats);
         item.eq.setMp(stats);
      }

      stats = GameConstants.getDEF(itemId, 0);
      if (stats > 0) {
         item.eq.setWdef(stats);
         item.eq.setMdef(stats);
      }

      if (item.equipStats.size() > 0) {
         Iterator var4 = item.equipStats.entrySet().iterator();

         while(var4.hasNext()) {
            Entry<String, Integer> stat = (Entry)var4.next();
            String key = (String)stat.getKey();
            if (key.equals("STR")) {
               item.eq.setStr(GameConstants.getStat(itemId, (Integer)stat.getValue()));
            } else if (key.equals("DEX")) {
               item.eq.setDex(GameConstants.getStat(itemId, (Integer)stat.getValue()));
            } else if (key.equals("INT")) {
               item.eq.setInt(GameConstants.getStat(itemId, (Integer)stat.getValue()));
            } else if (key.equals("LUK")) {
               item.eq.setLuk(GameConstants.getStat(itemId, (Integer)stat.getValue()));
            } else if (key.equals("PAD")) {
               item.eq.setWatk(GameConstants.getATK(itemId, (Integer)stat.getValue()));
            } else if (key.equals("PDD")) {
               item.eq.setWdef(GameConstants.getDEF(itemId, (Integer)stat.getValue()));
            } else if (key.equals("MAD")) {
               item.eq.setMatk(GameConstants.getATK(itemId, (Integer)stat.getValue()));
            } else if (key.equals("MDD")) {
               item.eq.setMdef(GameConstants.getDEF(itemId, (Integer)stat.getValue()));
            } else if (key.equals("ACC")) {
               item.eq.setAcc((short)(Integer)stat.getValue());
            } else if (key.equals("EVA")) {
               item.eq.setAvoid((short)(Integer)stat.getValue());
            } else if (key.equals("Speed")) {
               item.eq.setSpeed((short)(Integer)stat.getValue());
            } else if (key.equals("Jump")) {
               item.eq.setJump((short)(Integer)stat.getValue());
            } else if (key.equals("MHP")) {
               item.eq.setHp(GameConstants.getHpMp(itemId, (Integer)stat.getValue()));
            } else if (key.equals("MMP")) {
               item.eq.setMp(GameConstants.getHpMp(itemId, (Integer)stat.getValue()));
            } else if (key.equals("tuc")) {
               item.eq.setUpgradeSlots(((Integer)stat.getValue()).byteValue());
            } else if (key.equals("Craft")) {
               item.eq.setHands(((Integer)stat.getValue()).shortValue());
            } else if (key.equals("durability")) {
               item.eq.setDurability((Integer)stat.getValue());
            } else if (key.equals("charmEXP")) {
               item.eq.setCharmEXP(((Integer)stat.getValue()).shortValue());
            } else if (key.equals("PVPDamage")) {
               item.eq.setPVPDamage(((Integer)stat.getValue()).shortValue());
            } else if (key.equals("bdR")) {
               item.eq.setBossDamage(((Integer)stat.getValue()).shortValue());
            } else if (key.equals("imdR")) {
               item.eq.setIgnorePDR(((Integer)stat.getValue()).shortValue());
            } else if (key.equals("attackSpeed")) {
               item.eq.setAttackSpeed((Integer)stat.getValue());
            }
         }

         if (item.equipStats.get("cash") != null && item.eq.getCharmEXP() <= 0) {
            short exp = 0;
            int identifier = itemId / 10000;
            if (!GameConstants.isWeapon(itemId) && identifier != 106) {
               if (identifier == 100) {
                  exp = 50;
               } else if (!GameConstants.isAccessory(itemId) && identifier != 102 && identifier != 108 && identifier != 107) {
                  if (identifier == 104 || identifier == 105 || identifier == 110) {
                     exp = 30;
                  }
               } else {
                  exp = 40;
               }
            } else {
               exp = 60;
            }

            item.eq.setCharmEXP(exp);
         }
      }

   }

   public void initItemInformation(ResultSet sqlItemData) throws SQLException {
      ItemInformation ret = new ItemInformation();
      int itemId = sqlItemData.getInt("itemid");
      ret.itemId = itemId;
      ret.slotMax = GameConstants.getSlotMax(itemId) > 0 ? GameConstants.getSlotMax(itemId) : sqlItemData.getShort("slotMax");
      ret.price = Double.parseDouble(sqlItemData.getString("price"));
      ret.wholePrice = sqlItemData.getInt("wholePrice");
      ret.stateChange = sqlItemData.getInt("stateChange");
      ret.name = sqlItemData.getString("name");
      ret.desc = sqlItemData.getString("desc");
      ret.msg = sqlItemData.getString("msg");
      ret.flag = sqlItemData.getInt("flags");
      ret.karmaEnabled = sqlItemData.getByte("karma");
      ret.meso = sqlItemData.getInt("meso");
      ret.monsterBook = sqlItemData.getInt("monsterBook");
      ret.itemMakeLevel = sqlItemData.getShort("itemMakeLevel");
      ret.questId = sqlItemData.getInt("questId");
      ret.create = sqlItemData.getInt("create");
      ret.replaceItem = sqlItemData.getInt("replaceId");
      ret.replaceMsg = sqlItemData.getString("replaceMsg");
      ret.afterImage = sqlItemData.getString("afterImage");
      ret.chairType = sqlItemData.getString("chairType");
      ret.nickSkill = sqlItemData.getInt("nickSkill");
      ret.cardSet = 0;
      if (ret.monsterBook > 0 && itemId / 10000 == 238) {
         this.mobIds.put(ret.monsterBook, itemId);
         Iterator var4 = this.monsterBookSets.entrySet().iterator();

         while(var4.hasNext()) {
            Entry<Integer, Triple<Integer, List<Integer>, List<Integer>>> set = (Entry)var4.next();
            if (((List)((Triple)set.getValue()).mid).contains(itemId)) {
               ret.cardSet = (Integer)set.getKey();
               break;
            }
         }
      }

      String scrollRq = sqlItemData.getString("scrollReqs");
      String[] scroll;
      int var8;
      if (scrollRq.length() > 0) {
         ret.scrollReqs = new ArrayList();
         String[] scroll = scrollRq.split(",");
         scroll = scroll;
         int var7 = scroll.length;

         for(var8 = 0; var8 < var7; ++var8) {
            String s = scroll[var8];
            if (s.length() > 1) {
               ret.scrollReqs.add(Integer.parseInt(s));
            }
         }
      }

      String consumeItem = sqlItemData.getString("consumeItem");
      String[] scroll;
      int var18;
      if (consumeItem.length() > 0) {
         ret.questItems = new ArrayList();
         scroll = scrollRq.split(",");
         scroll = scroll;
         var8 = scroll.length;

         for(var18 = 0; var18 < var8; ++var18) {
            String s = scroll[var18];
            if (s.length() > 1) {
               ret.questItems.add(Integer.parseInt(s));
            }
         }
      }

      ret.totalprob = sqlItemData.getInt("totalprob");
      String incRq = sqlItemData.getString("incSkill");
      if (incRq.length() > 0) {
         ret.incSkill = new ArrayList();
         scroll = incRq.split(",");
         String[] var17 = scroll;
         var18 = scroll.length;

         for(int var19 = 0; var19 < var18; ++var19) {
            String s = var17[var19];
            if (s.length() > 1) {
               ret.incSkill.add(Integer.parseInt(s));
            }
         }
      }

      this.dataCache.put(itemId, ret);
   }

   public Pair<String, Boolean> isSuperial(int itemid) {
      if (itemid >= 1102471 && itemid <= 1102475 || itemid >= 1072732 && itemid <= 1072736 || itemid >= 1132164 && itemid <= 1132168) {
         return new Pair("Helisium", true);
      } else if (itemid >= 1102476 && itemid <= 1102480 || itemid >= 1072737 && itemid <= 1072741 || itemid >= 1132169 && itemid <= 1132173) {
         return new Pair("Nova", true);
      } else if (itemid >= 1102481 && itemid <= 1102485 || itemid >= 1072743 && itemid <= 1072747 || itemid >= 1132174 && itemid <= 1132178 || itemid >= 1082543 && itemid <= 1082547) {
         return new Pair("Tilent", true);
      } else {
         return itemid >= 1122241 && itemid <= 1122245 ? new Pair("MindPendent", true) : new Pair((Object)null, false);
      }
   }

   public final Equip fuse(Equip equip1, Equip equip2) {
      if (equip1.getItemId() != equip2.getItemId()) {
         return equip1;
      } else {
         Equip equip = (Equip)this.getEquipById(equip1.getItemId());
         equip.setStr(this.getRandStatFusion(equip.getStr(), equip1.getStr(), equip2.getStr()));
         equip.setDex(this.getRandStatFusion(equip.getDex(), equip1.getDex(), equip2.getDex()));
         equip.setInt(this.getRandStatFusion(equip.getInt(), equip1.getInt(), equip2.getInt()));
         equip.setLuk(this.getRandStatFusion(equip.getLuk(), equip1.getLuk(), equip2.getLuk()));
         equip.setMatk(this.getRandStatFusion(equip.getMatk(), equip1.getMatk(), equip2.getMatk()));
         equip.setWatk(this.getRandStatFusion(equip.getWatk(), equip1.getWatk(), equip2.getWatk()));
         equip.setAcc(this.getRandStatFusion(equip.getAcc(), equip1.getAcc(), equip2.getAcc()));
         equip.setAvoid(this.getRandStatFusion(equip.getAvoid(), equip1.getAvoid(), equip2.getAvoid()));
         equip.setJump(this.getRandStatFusion(equip.getJump(), equip1.getJump(), equip2.getJump()));
         equip.setHands(this.getRandStatFusion(equip.getHands(), equip1.getHands(), equip2.getHands()));
         equip.setSpeed(this.getRandStatFusion(equip.getSpeed(), equip1.getSpeed(), equip2.getSpeed()));
         equip.setWdef(this.getRandStatFusion(equip.getWdef(), equip1.getWdef(), equip2.getWdef()));
         equip.setMdef(this.getRandStatFusion(equip.getMdef(), equip1.getMdef(), equip2.getMdef()));
         equip.setHp(this.getRandStatFusion(equip.getHp(), equip1.getHp(), equip2.getHp()));
         equip.setMp(this.getRandStatFusion(equip.getMp(), equip1.getMp(), equip2.getMp()));
         return equip;
      }
   }

   protected final short getRandStatFusion(short defaultValue, int value1, int value2) {
      if (defaultValue == 0) {
         return 0;
      } else {
         int range = (value1 + value2) / 2 - defaultValue;
         int rand = Randomizer.nextInt(Math.abs(range) + 1);
         return (short)(defaultValue + (range < 0 ? -rand : rand));
      }
   }

   public final Equip randomizeStats(Equip equip) {
      equip.setStr(this.getRandStat(equip.getStr(), 5));
      equip.setDex(this.getRandStat(equip.getDex(), 5));
      equip.setInt(this.getRandStat(equip.getInt(), 5));
      equip.setLuk(this.getRandStat(equip.getLuk(), 5));
      equip.setMatk(this.getRandStat(equip.getMatk(), 5));
      equip.setWatk(this.getRandStat(equip.getWatk(), 5));
      equip.setAcc(this.getRandStat(equip.getAcc(), 5));
      equip.setAvoid(this.getRandStat(equip.getAvoid(), 5));
      equip.setJump(this.getRandStat(equip.getJump(), 5));
      equip.setHands(this.getRandStat(equip.getHands(), 5));
      equip.setSpeed(this.getRandStat(equip.getSpeed(), 5));
      equip.setWdef(this.getRandStat(equip.getWdef(), 10));
      equip.setMdef(this.getRandStat(equip.getMdef(), 10));
      equip.setHp(this.getRandStat(equip.getHp(), 10));
      equip.setMp(this.getRandStat(equip.getMp(), 10));
      return equip;
   }

   protected final short getRandStat(short defaultValue, int maxRange) {
      if (defaultValue == 0) {
         return 0;
      } else {
         int lMaxRange = (int)Math.min(Math.ceil((double)defaultValue * 0.1D), (double)maxRange);
         return (short)(defaultValue - lMaxRange + Randomizer.nextInt(lMaxRange * 2 + 1));
      }
   }
}
