package client.inventory;

import client.MapleClient;
import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import server.MapleItemInformationProvider;
import server.enchant.EquipmentEnchant;
import server.maps.BossReward;
import tools.Pair;

public enum ItemLoader {
   INVENTORY("inventoryitems", "inventoryequipment", "inventoryequipenchant", 0, "characterid"),
   STORAGE("inventoryitems", "inventoryequipment", "inventoryequipenchant", 1, "accountid"),
   CASHSHOP("csitems", "csequipment", (String)null, 2, "accountid"),
   HIRED_MERCHANT("hiredmerchitems", "hiredmerchequipment", (String)null, 5, "packageid"),
   DUEY("dueyitems", "dueyequipment", "dueyequipenchant", 6, "packageid");

   private int value;
   private String table;
   private String table_equip;
   private String table_enchant;
   private String arg;

   private ItemLoader(String table, String table_equip, String table_enchant, int value, String arg) {
      this.table = table;
      this.table_equip = table_equip;
      this.table_enchant = table_enchant;
      this.value = value;
      this.arg = arg;
   }

   public int getValue() {
      return this.value;
   }

   public Map<Long, Item> loadItems(boolean login, int id, MapleInventoryType type) throws SQLException {
      Map<Long, Item> items = new LinkedHashMap();
      StringBuilder query = new StringBuilder();
      query.append("SELECT * FROM `");
      if (this.value <= 1) {
         switch(type.getType()) {
         case 2:
            query.append("inventoryitemsuse");
            break;
         case 3:
            query.append("inventoryitemssetup");
            break;
         case 4:
            query.append("inventoryitemsetc");
            break;
         case 5:
            query.append("inventoryitemscash");
            break;
         case 6:
            query.append("inventoryitemscody");
            break;
         default:
            query.append("inventoryitems");
         }
      } else {
         query.append(this.table);
      }

      query.append("` LEFT JOIN `");
      if (this.value <= 1 && type.getType() == 6) {
         query.append("inventoryequipmentcody");
      } else {
         query.append(this.table_equip);
      }

      query.append("` USING (`inventoryitemid`) WHERE `type` = ?");
      query.append(" AND `");
      query.append(this.arg);
      query.append("` = ?");
      if (login) {
         query.append(" AND `inventorytype` = ");
         query.append(MapleInventoryType.EQUIPPED.getType());
      }

      Connection con = DatabaseConnection.getConnection();

      try {
         PreparedStatement ps = con.prepareStatement(query.toString());

         try {
            ps.setInt(1, this.value);
            ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();

            try {
               MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

               while(rs.next()) {
                  if (ii.itemExists(rs.getInt("itemid"))) {
                     Item item1;
                     if (rs.getInt("itemid") / 1000000 == 1) {
                        Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getLong("uniqueid"), rs.getInt("flag"));
                        if (!login) {
                           equip.setQuantity((short)1);
                           equip.setInventoryId(rs.getLong("inventoryitemid"));
                           equip.setOwner(rs.getString("owner"));
                           equip.setExpiration(rs.getLong("expiredate"));
                           equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                           equip.setLevel(rs.getByte("level"));
                           equip.setStr(rs.getShort("str"));
                           equip.setDex(rs.getShort("dex"));
                           equip.setInt(rs.getShort("int"));
                           equip.setLuk(rs.getShort("luk"));
                           equip.setHp(rs.getShort("hp"));
                           equip.setMp(rs.getShort("mp"));
                           equip.setWatk(rs.getShort("watk"));
                           equip.setMatk(rs.getShort("matk"));
                           equip.setWdef(rs.getShort("wdef"));
                           equip.setMdef(rs.getShort("mdef"));
                           equip.setAcc(rs.getShort("acc"));
                           equip.setAvoid(rs.getShort("avoid"));
                           equip.setHands(rs.getShort("hands"));
                           equip.setSpeed(rs.getShort("speed"));
                           equip.setJump(rs.getShort("jump"));
                           equip.setViciousHammer(rs.getByte("ViciousHammer"));
                           equip.setItemEXP(rs.getInt("itemEXP"));
                           equip.setGMLog(rs.getString("GM_Log"));
                           equip.setDurability(rs.getInt("durability"));
                           equip.setEnhance(rs.getByte("enhance"));
                           equip.setState(rs.getByte("state"));
                           equip.setLines(rs.getByte("line"));
                           equip.setPotential1(rs.getInt("potential1"));
                           equip.setPotential2(rs.getInt("potential2"));
                           equip.setPotential3(rs.getInt("potential3"));
                           equip.setPotential4(rs.getInt("potential4"));
                           equip.setPotential5(rs.getInt("potential5"));
                           equip.setPotential6(rs.getInt("potential6"));
                           equip.setGiftFrom(rs.getString("sender"));
                           equip.setIncSkill(rs.getInt("incSkill"));
                           equip.setPVPDamage(rs.getShort("pvpDamage"));
                           equip.setCharmEXP(rs.getShort("charmEXP"));
                           if (equip.getCharmEXP() < 0) {
                              equip.setCharmEXP(((Equip)ii.getEquipById(equip.getItemId())).getCharmEXP());
                           }

                           if (equip.getUniqueId() > -1L) {
                              if (GameConstants.isEffectRing(rs.getInt("itemid"))) {
                                 MapleRing ring = MapleRing.loadFromDb(equip.getUniqueId(), type.equals(MapleInventoryType.EQUIPPED));
                                 if (ring != null) {
                                    equip.setRing(ring);
                                 }
                              } else if (equip.getItemId() / 10000 == 166) {
                                 MapleAndroid ring = MapleAndroid.loadFromDb(equip.getItemId(), equip.getUniqueId());
                                 if (ring != null) {
                                    equip.setAndroid(ring);
                                 }
                              }
                           }

                           equip.setEnchantBuff(rs.getShort("enchantbuff"));
                           equip.setReqLevel(rs.getByte("reqLevel"));
                           equip.setYggdrasilWisdom(rs.getByte("yggdrasilWisdom"));
                           equip.setFinalStrike(rs.getByte("finalStrike") > 0);
                           equip.setBossDamage((short)rs.getByte("bossDamage"));
                           equip.setIgnorePDR((short)rs.getByte("ignorePDR"));
                           equip.setTotalDamage(rs.getByte("totalDamage"));
                           equip.setAllStat(rs.getByte("allStat"));
                           equip.setKarmaCount(rs.getByte("karmaCount"));
                           equip.setSoulEnchanter(rs.getShort("soulenchanter"));
                           equip.setSoulName(rs.getShort("soulname"));
                           equip.setSoulPotential(rs.getShort("soulpotential"));
                           equip.setSoulSkill(rs.getInt("soulskill"));
                           equip.setFire(rs.getLong("fire") < 0L ? 0L : rs.getLong("fire"));
                           equip.setArc(rs.getShort("arc"));
                           equip.setArcEXP(rs.getInt("arcexp"));
                           equip.setArcLevel(rs.getInt("arclevel"));
                           equip.setEquipmentType(rs.getInt("equipmenttype"));
                           equip.setMoru(rs.getInt("moru"));
                           equip.setAttackSpeed(rs.getInt("attackSpeed"));
                           equip.setOptionExpiration(rs.getLong("optionexpiration"));
                           equip.setCoption1(rs.getInt("coption1"));
                           equip.setCoption2(rs.getInt("coption2"));
                           equip.setCoption3(rs.getInt("coption3"));
                           if (this.table_enchant != null && type.getType() != 6) {
                              PreparedStatement ps1 = con.prepareStatement("SELECT * FROM `" + this.table_enchant + "` WHERE inventoryitemid = ?");
                              ps1.setLong(1, equip.getInventoryId());
                              ResultSet rs1 = ps1.executeQuery();
                              if (rs1.next()) {
                                 equip.setEnchantStr(rs1.getShort("str"));
                                 equip.setEnchantDex(rs1.getShort("dex"));
                                 equip.setEnchantInt(rs1.getShort("int"));
                                 equip.setEnchantLuk(rs1.getShort("luk"));
                                 equip.setEnchantHp(rs1.getShort("hp"));
                                 equip.setEnchantMp(rs1.getShort("mp"));
                                 equip.setEnchantWatk(rs1.getShort("watk"));
                                 equip.setEnchantMatk(rs1.getShort("matk"));
                                 equip.setEnchantWdef(rs1.getShort("wdef"));
                                 equip.setEnchantMdef(rs1.getShort("mdef"));
                                 equip.setEnchantAcc(rs1.getShort("acc"));
                                 equip.setEnchantAvoid(rs1.getShort("avoid"));
                              }

                              rs1.close();
                              ps1.close();
                           }
                        }

                        item1 = equip.copy();
                        if (isCanMadeItem((Equip)item1)) {
                           items.put(rs.getLong("inventoryitemid"), item1);
                        }
                     } else {
                        Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getInt("flag"), rs.getLong("uniqueid"));
                        item.setOwner(rs.getString("owner"));
                        item.setInventoryId(rs.getLong("inventoryitemid"));
                        item.setExpiration(rs.getLong("expiredate"));
                        item.setGMLog(rs.getString("GM_Log"));
                        item.setGiftFrom(rs.getString("sender"));
                        item.setMarriageId(rs.getInt("marriageId"));
                        if (GameConstants.isPet(item.getItemId())) {
                           if (item.getUniqueId() > -1L) {
                              MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                              if (pet != null) {
                                 item.setPet(pet);
                              }
                           } else {
                              item.setPet(MaplePet.createPet(item.getItemId(), MapleInventoryIdentifier.getInstance()));
                           }
                        }

                        if (item.getItemId() == 4001886) {
                           item.setReward(new BossReward(rs.getInt("objectid"), rs.getInt("mobid"), rs.getInt("partyid"), rs.getInt("price")));
                        }

                        item1 = item.copy();
                        items.put(rs.getLong("inventoryitemid"), item1);
                     }
                  }
               }

               ps.close();
               rs.close();
            } catch (Throwable var22) {
               if (rs != null) {
                  try {
                     rs.close();
                  } catch (Throwable var21) {
                     var22.addSuppressed(var21);
                  }
               }

               throw var22;
            }

            if (rs != null) {
               rs.close();
            }
         } catch (Throwable var23) {
            if (ps != null) {
               try {
                  ps.close();
               } catch (Throwable var20) {
                  var23.addSuppressed(var20);
               }
            }

            throw var23;
         }

         if (ps != null) {
            ps.close();
         }
      } catch (SQLException var24) {
         var24.printStackTrace();
      } finally {
         if (con != null) {
            con.close();
         }

      }

      return items;
   }

   public void saveItems(List<Item> items, int id, MapleInventoryType type, boolean dc) {
      try {
         Connection con = DatabaseConnection.getConnection();
         this.saveItems(items, con, id, type, dc);
         con.close();
      } catch (SQLException var6) {
         var6.printStackTrace();
      }

   }

   public void saveItems(List<Item> items, Connection con, int id, MapleInventoryType type, boolean dc) {
      StringBuilder query = new StringBuilder();
      query.append("DELETE FROM `");
      if (this.value <= 1) {
         switch(type.getType()) {
         case 2:
            query.append("inventoryitemsuse");
            break;
         case 3:
            query.append("inventoryitemssetup");
            break;
         case 4:
            query.append("inventoryitemsetc");
            break;
         case 5:
            query.append("inventoryitemscash");
            break;
         case 6:
            query.append("inventoryitemscody");
            break;
         default:
            query.append("inventoryitems");
         }
      } else {
         query.append(this.table);
      }

      query.append("`WHERE `type` = ?");
      query.append(" AND `").append(this.arg);
      query.append("` = ?");

      try {
         PreparedStatement ps = con.prepareStatement(query.toString());
         ps.setInt(1, this.value);
         ps.setInt(2, id);
         ps.executeUpdate();
         ps.close();
         if (items == null) {
            return;
         }

         StringBuilder query_2 = new StringBuilder("INSERT INTO `");
         if (this.value <= 1) {
            switch(type.getType()) {
            case 2:
               query_2.append("inventoryitemsuse");
               break;
            case 3:
               query_2.append("inventoryitemssetup");
               break;
            case 4:
               query_2.append("inventoryitemsetc");
               break;
            case 5:
               query_2.append("inventoryitemscash");
               break;
            case 6:
               query_2.append("inventoryitemscody");
               break;
            default:
               query_2.append("inventoryitems");
            }
         } else {
            query_2.append(this.table);
         }

         query_2.append("` (");
         query_2.append(this.arg);
         query_2.append(", itemid, inventorytype, position, quantity, owner, GM_Log, uniqueid, expiredate, flag, `type`, sender, marriageId");
         query_2.append(", price, partyid, mobid, objectid");
         query_2.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
         query_2.append(", ?, ?, ?, ?");
         query_2.append(")");
         ps = con.prepareStatement(query_2.toString(), 1);
         PreparedStatement pse;
         if (this.value <= 1 && type.getType() == 6) {
            pse = con.prepareStatement("INSERT INTO inventoryequipmentcody VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         } else {
            pse = con.prepareStatement("INSERT INTO " + this.table_equip + " VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         }

         Iterator iter = items.iterator();

         while(true) {
            while(iter.hasNext()) {
               Item item = (Item)iter.next();
               ps.setInt(1, id);
               ps.setInt(2, item.getItemId());
               ps.setInt(3, GameConstants.getInventoryType(item.getItemId()).getType());
               ps.setInt(4, item.getPosition());
               ps.setInt(5, item.getQuantity());
               ps.setString(6, item.getOwner());
               ps.setString(7, item.getGMLog());
               if (item.getPet() != null) {
                  ps.setLong(8, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
               } else {
                  ps.setLong(8, item.getUniqueId());
               }

               ps.setLong(9, item.getExpiration());
               if (item.getFlag() < 0) {
                  ps.setInt(10, MapleItemInformationProvider.getInstance().getItemInformation(item.getItemId()).flag);
               } else {
                  ps.setInt(10, item.getFlag());
               }

               ps.setByte(11, (byte)this.value);
               ps.setString(12, item.getGiftFrom());
               ps.setInt(13, item.getMarriageId());
               if (item.getReward() != null) {
                  ps.setInt(14, item.getReward().getPrice());
                  ps.setInt(15, item.getReward().getPartyId());
                  ps.setInt(16, item.getReward().getMobId());
                  ps.setInt(17, item.getReward().getObjectId());
               } else {
                  ps.setInt(14, 0);
                  ps.setInt(15, 0);
                  ps.setInt(16, 0);
                  ps.setInt(17, 0);
               }

               ps.executeUpdate();
               ResultSet rs = ps.getGeneratedKeys();
               if (!rs.next()) {
                  rs.close();
               } else {
                  long iid = rs.getLong(1);
                  rs.close();
                  if (dc) {
                     item.setInventoryId(iid);
                  }

                  if (item.getItemId() / 1000000 == 1) {
                     Equip equip = (Equip)item;
                     if (equip != null && isCanMadeItem(equip)) {
                        pse.setLong(1, iid);
                        pse.setInt(2, equip.getUpgradeSlots() <= 0 ? 0 : equip.getUpgradeSlots());
                        if (equip.getItemId() >= 1113098 && equip.getItemId() <= 1113128) {
                           pse.setInt(3, equip.getBaseLevel());
                        } else {
                           pse.setInt(3, equip.getLevel());
                        }

                        pse.setInt(4, equip.getStr());
                        pse.setInt(5, equip.getDex());
                        pse.setInt(6, equip.getInt());
                        pse.setInt(7, equip.getLuk());
                        pse.setShort(8, equip.getArc());
                        pse.setInt(9, equip.getArcEXP());
                        pse.setInt(10, equip.getArcLevel());
                        pse.setInt(11, equip.getHp());
                        pse.setInt(12, equip.getMp());
                        pse.setInt(13, equip.getWatk());
                        pse.setInt(14, equip.getMatk());
                        pse.setInt(15, equip.getWdef());
                        pse.setInt(16, equip.getMdef());
                        pse.setInt(17, equip.getAcc());
                        pse.setInt(18, equip.getAvoid());
                        pse.setInt(19, equip.getHands());
                        pse.setInt(20, equip.getSpeed());
                        pse.setInt(21, equip.getJump());
                        pse.setInt(22, equip.getViciousHammer());
                        pse.setInt(23, equip.getItemEXP());
                        pse.setInt(24, equip.getDurability());
                        pse.setByte(25, equip.getEnhance());
                        pse.setByte(26, equip.getState());
                        pse.setByte(27, equip.getLines());
                        pse.setInt(28, equip.getPotential1());
                        pse.setInt(29, equip.getPotential2());
                        pse.setInt(30, equip.getPotential3());
                        pse.setInt(31, equip.getPotential4());
                        pse.setInt(32, equip.getPotential5());
                        pse.setInt(33, equip.getPotential6());
                        pse.setInt(34, equip.getIncSkill());
                        pse.setShort(35, equip.getCharmEXP());
                        pse.setShort(36, equip.getPVPDamage());
                        pse.setShort(37, equip.getEnchantBuff());
                        pse.setByte(38, equip.getReqLevel());
                        pse.setByte(39, equip.getYggdrasilWisdom());
                        pse.setByte(40, (byte)(equip.getFinalStrike() ? 1 : 0));
                        pse.setShort(41, equip.getBossDamage());
                        pse.setShort(42, equip.getIgnorePDR());
                        pse.setByte(43, equip.getTotalDamage());
                        pse.setByte(44, equip.getAllStat());
                        pse.setByte(45, equip.getKarmaCount());
                        pse.setShort(46, equip.getSoulName());
                        pse.setShort(47, equip.getSoulEnchanter());
                        pse.setShort(48, equip.getSoulPotential());
                        pse.setInt(49, equip.getSoulSkill());
                        pse.setLong(50, equip.getFire());
                        pse.setInt(51, equip.getEquipmentType());
                        pse.setInt(52, equip.getMoru());
                        pse.setInt(53, equip.getAttackSpeed());
                        pse.setLong(54, equip.getOptionExpiration());
                        pse.setInt(55, equip.getCoption1());
                        pse.setInt(56, equip.getCoption2());
                        pse.setInt(57, equip.getCoption3());
                        pse.executeUpdate();
                        if (equip.getItemId() / 10000 == 166 && equip.getAndroid() != null) {
                           equip.getAndroid().saveToDb();
                        }

                        if (this.table_enchant != null && type.getType() != 6) {
                           PreparedStatement ps2 = con.prepareStatement("INSERT INTO `" + this.table_enchant + "` VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                           ps2.setLong(1, iid);
                           ps2.setShort(2, equip.getEnchantStr());
                           ps2.setShort(3, equip.getEnchantDex());
                           ps2.setShort(4, equip.getEnchantInt());
                           ps2.setShort(5, equip.getEnchantLuk());
                           ps2.setShort(6, equip.getEnchantHp());
                           ps2.setShort(7, equip.getEnchantMp());
                           ps2.setShort(8, equip.getEnchantWatk());
                           ps2.setShort(9, equip.getEnchantMatk());
                           ps2.setShort(10, equip.getEnchantWdef());
                           ps2.setShort(11, equip.getEnchantMdef());
                           ps2.setShort(12, equip.getEnchantAcc());
                           ps2.setShort(13, equip.getEnchantAvoid());
                           ps2.executeUpdate();
                           ps2.close();
                        }
                     }
                  }
               }
            }

            ps.close();
            pse.close();
            break;
         }
      } catch (Exception var17) {
         var17.printStackTrace();
      }

   }

   public static boolean isCanMadeItem(Equip equip) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (ii.getName(equip.getItemId()) == null) {
         return true;
      } else if (ii.getName(equip.getItemId()).startsWith("제네시스")) {
         EquipmentEnchant.checkEquipmentStats((MapleClient)null, equip);
         return true;
      } else {
         if (equip.getFire() > 0L && (GameConstants.isRing(equip.getItemId()) || equip.getItemId() / 1000 == 1092 || equip.getItemId() / 1000 == 1342 || equip.getItemId() / 1000 == 1713 || equip.getItemId() / 1000 == 1712 || equip.getItemId() / 1000 == 1152 || equip.getItemId() / 1000 == 1143 || equip.getItemId() / 1000 == 1672 || GameConstants.isSecondaryWeapon(equip.getItemId()) || equip.getItemId() / 1000 == 1190 || equip.getItemId() / 1000 == 1182 || equip.getItemId() / 1000 == 1662 || equip.getItemId() / 1000 == 1802)) {
            equip.setFire(0L);
         }

         int reqLevel = ii.getReqLevel(equip.getItemId());
         boolean isSuperiol = ii.isSuperial(equip.getItemId()).left != null;
         boolean var4;
         if (reqLevel < 95) {
            var4 = isSuperiol ? true : true;
         } else if (reqLevel <= 107) {
            var4 = isSuperiol ? true : true;
         } else if (reqLevel <= 119) {
            var4 = isSuperiol ? true : true;
         } else if (reqLevel <= 129) {
            var4 = isSuperiol ? true : true;
         } else if (reqLevel <= 139) {
            var4 = isSuperiol ? true : true;
         } else {
            var4 = isSuperiol ? true : true;
         }

         if (equip.getArcLevel() > 20) {
            equip.setArcLevel(20);
         }

         if (equip.getArc() > 220) {
            equip.setArc((short)0);
         }

         if ((GameConstants.isArcaneSymbol(equip.getItemId()) || GameConstants.isAuthenticSymbol(equip.getItemId())) && equip.getItemId() != 1162002 && equip.getState() > 0) {
            equip.setState((byte)0);
            equip.setLines((byte)0);
            equip.setPotential1(0);
            equip.setPotential2(0);
            equip.setPotential3(0);
            equip.setPotential4(0);
            equip.setPotential5(0);
            equip.setPotential6(0);
         }

         if (equip.getItemId() == 1672077) {
            int wonflag = equip.getFlag();
            int flag = equip.getFlag();
            if (ItemFlag.RETURN_SCROLL.check(flag)) {
               flag -= ItemFlag.RETURN_SCROLL.getValue();
            }

            if (ItemFlag.PROTECT_SHIELD.check(flag)) {
               flag -= ItemFlag.PROTECT_SHIELD.getValue();
            }

            if (ItemFlag.SAFETY_SHIELD.check(flag)) {
               flag -= ItemFlag.SAFETY_SHIELD.getValue();
            }

            if (ItemFlag.RECOVERY_SHIELD.check(flag)) {
               flag -= ItemFlag.RECOVERY_SHIELD.getValue();
            }

            if (flag == 16) {
               equip.setFlag(0);
            }

            if (flag == 24) {
               equip.setFlag(8);
            }

            if (ItemFlag.RETURN_SCROLL.check(wonflag)) {
               equip.setFlag(equip.getFlag() | ItemFlag.RETURN_SCROLL.getValue());
            }

            if (ItemFlag.PROTECT_SHIELD.check(wonflag)) {
               equip.setFlag(equip.getFlag() | ItemFlag.PROTECT_SHIELD.getValue());
            }

            if (ItemFlag.SAFETY_SHIELD.check(wonflag)) {
               equip.setFlag(equip.getFlag() | ItemFlag.SAFETY_SHIELD.getValue());
            }

            if (ItemFlag.RECOVERY_SHIELD.check(wonflag)) {
               equip.setFlag(equip.getFlag() | ItemFlag.RECOVERY_SHIELD.getValue());
            }
         }

         if (equip.getItemId() == 1162002 && equip.getKarmaCount() > 0) {
            equip.setKarmaCount((byte)-1);
         }

         if (equip.getItemId() != 1182285 && equip.getItemId() != 1122430 && ii.getEquipStats(equip.getItemId()).get("undecomposable") == null && ii.getEquipStats(equip.getItemId()).get("unsyntesizable") == null) {
            EquipmentEnchant.checkEquipmentStats((MapleClient)null, equip);
            if (equip.getItemId() == 1672082) {
               equip.setPotential1(60011);
               equip.setPotential2(60010);
            }

            if (equip.getItemId() == 1672083) {
               equip.setState((byte)20);
               equip.setLines((byte)3);
               equip.setPotential1(40601);
               equip.setPotential2(30291);
               equip.setPotential3(42061);
               equip.setPotential4(42060);
               equip.setPotential5(42060);
               equip.setEnhance((byte)15);
            }

            if (equip.getItemId() == 1672085 || equip.getItemId() == 1672086) {
               equip.setState((byte)20);
               equip.setLines((byte)2);
               equip.setPotential1(40601);
               equip.setPotential2(30291);
               equip.setEnhance((byte)15);
            }

            return true;
         } else {
            return true;
         }
      }
   }

   public void saveItems(List<Pair<Item, MapleInventoryType>> items, int id) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         this.saveCSItems(items, con, id, (MapleClient)null);
         con.close();
      } catch (SQLException var15) {
         var15.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ((PreparedStatement)ps).close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var14) {
         }

      }

   }

   public void saveCSItems(List<Pair<Item, MapleInventoryType>> items, Connection con, int id, MapleClient c) throws SQLException {
      StringBuilder query = new StringBuilder();
      query.append("DELETE FROM `");
      query.append(this.table);
      query.append("`WHERE `type` = ?");
      query.append(" AND `").append(this.arg);
      query.append("` = ?");

      try {
         PreparedStatement ps = con.prepareStatement(query.toString());
         ps.setInt(1, this.value);
         ps.setInt(2, id);
         ps.executeUpdate();
         query = new StringBuilder();
         query.append("SELECT `inventoryitemid`, `type`, `characterid` FROM `");
         query.append(this.table);
         query.append("` WHERE `type` = ? AND `");
         query.append(this.arg);
         query.append("` = ?");
         ps = con.prepareStatement(query.toString());
         ps.setInt(1, this.value);
         ps.setInt(2, id);
         ps.close();
         if (items != null) {
            StringBuilder query_2 = new StringBuilder("INSERT INTO `");
            query_2.append(this.table);
            query_2.append("` (");
            query_2.append(this.arg);
            query_2.append(", itemid, inventorytype, position, quantity, owner, GM_Log, uniqueid, expiredate, flag, `type`, sender, marriageId");
            query_2.append(", price, partyid, mobid, objectid");
            query_2.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
            query_2.append(", ?, ?, ?, ?");
            query_2.append(")");
            ps = con.prepareStatement(query_2.toString(), 1);
            PreparedStatement pse = con.prepareStatement("INSERT INTO " + this.table_equip + " VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            Iterator iter = items.iterator();

            while(true) {
               while(true) {
                  Item item;
                  MapleInventoryType mit;
                  do {
                     do {
                        if (!iter.hasNext()) {
                           pse.close();
                           ps.close();
                           return;
                        }

                        Pair<Item, MapleInventoryType> pair = (Pair)iter.next();
                        item = (Item)pair.getLeft();
                        mit = (MapleInventoryType)pair.getRight();
                     } while(item.getPosition() == -55);
                  } while(item == null);

                  ps.setInt(1, id);
                  ps.setInt(2, item.getItemId());
                  ps.setInt(3, mit == null ? 0 : mit.getType());
                  ps.setInt(4, item.getPosition());
                  ps.setInt(5, item.getQuantity());
                  ps.setString(6, item.getOwner());
                  ps.setString(7, item.getGMLog());
                  if (item.getPet() != null) {
                     ps.setLong(8, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                  } else {
                     ps.setLong(8, item.getUniqueId());
                  }

                  ps.setLong(9, item.getExpiration());
                  if (item.getFlag() < 0) {
                     ps.setInt(10, MapleItemInformationProvider.getInstance().getItemInformation(item.getItemId()).flag);
                  } else {
                     ps.setInt(10, item.getFlag());
                  }

                  ps.setByte(11, (byte)this.value);
                  ps.setString(12, item.getGiftFrom());
                  ps.setInt(13, item.getMarriageId());
                  if (item.getReward() != null) {
                     ps.setInt(14, item.getReward().getPrice());
                     ps.setInt(15, item.getReward().getPartyId());
                     ps.setInt(16, item.getReward().getMobId());
                     ps.setInt(17, item.getReward().getObjectId());
                  } else {
                     ps.setInt(14, 0);
                     ps.setInt(15, 0);
                     ps.setInt(16, 0);
                     ps.setInt(17, 0);
                  }

                  ps.executeUpdate();
                  ResultSet rs = ps.getGeneratedKeys();
                  if (!rs.next()) {
                     rs.close();
                  } else {
                     long iid = rs.getLong(1);
                     rs.close();
                     if (item.getInventoryId() == 0L) {
                        item.setInventoryId(iid);
                     } else {
                        item.setInventoryId(item.getInventoryId());
                     }

                     if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED) || mit.equals(MapleInventoryType.CODY)) {
                        Equip equip = (Equip)item;
                        if (isCanMadeItem(equip)) {
                           pse.setLong(1, iid);
                           pse.setInt(2, equip.getUpgradeSlots());
                           if (equip.getItemId() >= 1113098 && equip.getItemId() <= 1113128) {
                              pse.setInt(3, equip.getBaseLevel());
                           } else {
                              pse.setInt(3, equip.getLevel());
                           }

                           pse.setInt(4, equip.getStr());
                           pse.setInt(5, equip.getDex());
                           pse.setInt(6, equip.getInt());
                           pse.setInt(7, equip.getLuk());
                           pse.setShort(8, equip.getArc());
                           pse.setInt(9, equip.getArcEXP());
                           pse.setInt(10, equip.getArcLevel());
                           pse.setInt(11, equip.getHp());
                           pse.setInt(12, equip.getMp());
                           pse.setInt(13, equip.getWatk());
                           pse.setInt(14, equip.getMatk());
                           pse.setInt(15, equip.getWdef());
                           pse.setInt(16, equip.getMdef());
                           pse.setInt(17, equip.getAcc());
                           pse.setInt(18, equip.getAvoid());
                           pse.setInt(19, equip.getHands());
                           pse.setInt(20, equip.getSpeed());
                           pse.setInt(21, equip.getJump());
                           pse.setInt(22, equip.getViciousHammer());
                           pse.setInt(23, equip.getItemEXP());
                           pse.setInt(24, equip.getDurability());
                           pse.setByte(25, equip.getEnhance());
                           pse.setByte(26, equip.getState());
                           pse.setByte(27, equip.getLines());
                           pse.setInt(28, equip.getPotential1());
                           pse.setInt(29, equip.getPotential2());
                           pse.setInt(30, equip.getPotential3());
                           pse.setInt(31, equip.getPotential4());
                           pse.setInt(32, equip.getPotential5());
                           pse.setInt(33, equip.getPotential6());
                           pse.setInt(34, equip.getIncSkill());
                           pse.setShort(35, equip.getCharmEXP());
                           pse.setShort(36, equip.getPVPDamage());
                           pse.setShort(37, equip.getEnchantBuff());
                           pse.setByte(38, equip.getReqLevel());
                           pse.setByte(39, equip.getYggdrasilWisdom());
                           pse.setByte(40, (byte)(equip.getFinalStrike() ? 1 : 0));
                           pse.setShort(41, equip.getBossDamage());
                           pse.setShort(42, equip.getIgnorePDR());
                           pse.setByte(43, equip.getTotalDamage());
                           pse.setByte(44, equip.getAllStat());
                           pse.setByte(45, equip.getKarmaCount());
                           pse.setShort(46, equip.getSoulName());
                           pse.setShort(47, equip.getSoulEnchanter());
                           pse.setShort(48, equip.getSoulPotential());
                           pse.setInt(49, equip.getSoulSkill());
                           pse.setLong(50, equip.getFire());
                           pse.setInt(51, equip.getEquipmentType());
                           pse.setInt(52, equip.getMoru());
                           pse.setInt(53, equip.getAttackSpeed());
                           pse.setLong(54, equip.getOptionExpiration());
                           pse.setInt(55, equip.getCoption1());
                           pse.setInt(56, equip.getCoption2());
                           pse.setInt(57, equip.getCoption3());
                           pse.executeUpdate();
                           if (equip.getItemId() / 10000 == 166 && equip.getAndroid() != null) {
                              equip.getAndroid().saveToDb();
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var17) {
         var17.printStackTrace();
      }
   }

   public Map<Long, Pair<Item, MapleInventoryType>> loadCSItems(boolean login, int id) throws SQLException {
      Map<Long, Pair<Item, MapleInventoryType>> items = new LinkedHashMap();
      StringBuilder query = new StringBuilder();
      query.append("SELECT * FROM `");
      query.append(this.table);
      query.append("` LEFT JOIN `");
      query.append(this.table_equip);
      String var10001 = this.getValue() != 7 ? "(`inventoryitemid`)" : "(`auctionid`)";
      query.append("` USING " + var10001 + (this.getValue() != 7 ? " WHERE `type` = ?" : ""));
      if (this.getValue() != 7) {
         query.append(" AND `");
         query.append(this.arg);
         query.append("` = ?");
      } else {
         query.append("ORDER BY startdate ASC");
      }

      if (login) {
         query.append(" AND `inventorytype` = ");
         query.append(MapleInventoryType.EQUIPPED.getType());
      }

      Connection con = DatabaseConnection.getConnection();

      try {
         PreparedStatement ps = con.prepareStatement(query.toString());

         try {
            if (this.getValue() != 7) {
               ps.setInt(1, this.value);
               ps.setInt(2, id);
            }

            ResultSet rs = ps.executeQuery();

            try {
               MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

               label306:
               while(true) {
                  while(true) {
                     do {
                        if (!rs.next()) {
                           ps.close();
                           rs.close();
                           break label306;
                        }
                     } while(!ii.itemExists(rs.getInt("itemid")));

                     MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                     Item item1;
                     if (!mit.equals(MapleInventoryType.EQUIP) && !mit.equals(MapleInventoryType.EQUIPPED) && !mit.equals(MapleInventoryType.CODY)) {
                        Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getInt("flag"), rs.getLong("uniqueid"));
                        item.setOwner(rs.getString("owner"));
                        item.setInventoryId(rs.getLong("inventoryitemid"));
                        item.setExpiration(rs.getLong("expiredate"));
                        item.setGMLog(rs.getString("GM_Log"));
                        item.setGiftFrom(rs.getString("sender"));
                        item.setMarriageId(rs.getInt("marriageId"));
                        if (GameConstants.isPet(item.getItemId())) {
                           if (item.getUniqueId() > -1L) {
                              MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                              if (pet != null) {
                                 item.setPet(pet);
                              }
                           } else {
                              item.setPet(MaplePet.createPet(item.getItemId(), MapleInventoryIdentifier.getInstance()));
                           }
                        }

                        if (item.getItemId() == 4001886) {
                           item.setReward(new BossReward(rs.getInt("objectid"), rs.getInt("mobid"), rs.getInt("partyid"), rs.getInt("price")));
                        }

                        item1 = item.copy();
                        items.put(rs.getLong("inventoryitemid"), new Pair(item1, mit));
                     } else {
                        Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), (long)rs.getInt("uniqueid"), rs.getInt("flag"));
                        if (!login && equip.getPosition() != -55) {
                           equip.setQuantity((short)1);
                           equip.setInventoryId(rs.getLong("inventoryitemid"));
                           equip.setOwner(rs.getString("owner"));
                           equip.setExpiration(rs.getLong("expiredate"));
                           equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                           equip.setLevel(rs.getByte("level"));
                           equip.setStr(rs.getShort("str"));
                           equip.setDex(rs.getShort("dex"));
                           equip.setInt(rs.getShort("int"));
                           equip.setLuk(rs.getShort("luk"));
                           equip.setHp(rs.getShort("hp"));
                           equip.setMp(rs.getShort("mp"));
                           equip.setWatk(rs.getShort("watk"));
                           equip.setMatk(rs.getShort("matk"));
                           equip.setWdef(rs.getShort("wdef"));
                           equip.setMdef(rs.getShort("mdef"));
                           equip.setAcc(rs.getShort("acc"));
                           equip.setAvoid(rs.getShort("avoid"));
                           equip.setHands(rs.getShort("hands"));
                           equip.setSpeed(rs.getShort("speed"));
                           equip.setJump(rs.getShort("jump"));
                           equip.setViciousHammer(rs.getByte("ViciousHammer"));
                           equip.setItemEXP(rs.getInt("itemEXP"));
                           equip.setGMLog(rs.getString("GM_Log"));
                           equip.setDurability(rs.getInt("durability"));
                           equip.setEnhance(rs.getByte("enhance"));
                           equip.setState(rs.getByte("state"));
                           equip.setLines(rs.getByte("line"));
                           equip.setPotential1(rs.getInt("potential1"));
                           equip.setPotential2(rs.getInt("potential2"));
                           equip.setPotential3(rs.getInt("potential3"));
                           equip.setPotential4(rs.getInt("potential4"));
                           equip.setPotential5(rs.getInt("potential5"));
                           equip.setPotential6(rs.getInt("potential6"));
                           equip.setGiftFrom(rs.getString("sender"));
                           equip.setIncSkill(rs.getInt("incSkill"));
                           equip.setPVPDamage(rs.getShort("pvpDamage"));
                           equip.setCharmEXP(rs.getShort("charmEXP"));
                           if (equip.getCharmEXP() < 0) {
                              equip.setCharmEXP(((Equip)ii.getEquipById(equip.getItemId())).getCharmEXP());
                           }

                           if (equip.getUniqueId() > -1L) {
                              if (GameConstants.isEffectRing(rs.getInt("itemid"))) {
                                 MapleRing ring = MapleRing.loadFromDb(equip.getUniqueId(), mit.equals(MapleInventoryType.EQUIPPED));
                                 if (ring != null) {
                                    equip.setRing(ring);
                                 }
                              } else if (equip.getItemId() / 10000 == 166) {
                                 MapleAndroid ring = MapleAndroid.loadFromDb(equip.getItemId(), equip.getUniqueId());
                                 if (ring != null) {
                                    equip.setAndroid(ring);
                                 }
                              }
                           }

                           equip.setEnchantBuff(rs.getShort("enchantbuff"));
                           equip.setReqLevel(rs.getByte("reqLevel"));
                           equip.setYggdrasilWisdom(rs.getByte("yggdrasilWisdom"));
                           equip.setFinalStrike(rs.getByte("finalStrike") > 0);
                           equip.setBossDamage((short)rs.getByte("bossDamage"));
                           equip.setIgnorePDR((short)rs.getByte("ignorePDR"));
                           equip.setTotalDamage(rs.getByte("totalDamage"));
                           equip.setAllStat(rs.getByte("allStat"));
                           equip.setKarmaCount(rs.getByte("karmaCount"));
                           equip.setSoulEnchanter(rs.getShort("soulenchanter"));
                           equip.setSoulName(rs.getShort("soulname"));
                           equip.setSoulPotential(rs.getShort("soulpotential"));
                           equip.setSoulSkill(rs.getInt("soulskill"));
                           equip.setFire(rs.getLong("fire") < 0L ? 0L : rs.getLong("fire"));
                           equip.setArc(rs.getShort("arc"));
                           equip.setArcEXP(rs.getInt("arcexp"));
                           equip.setArcLevel(rs.getInt("arclevel"));
                           equip.setEquipmentType(rs.getInt("equipmenttype"));
                           equip.setMoru(rs.getInt("moru"));
                           equip.setAttackSpeed(rs.getInt("attackSpeed"));
                           equip.setOptionExpiration(rs.getLong("optionexpiration"));
                           equip.setCoption1(rs.getInt("coption1"));
                           equip.setCoption2(rs.getInt("coption2"));
                           equip.setCoption3(rs.getInt("coption3"));
                        }

                        item1 = equip.copy();
                        if (isCanMadeItem((Equip)item1)) {
                           items.put(rs.getLong("inventoryitemid"), new Pair(item1, mit));
                        }
                     }
                  }
               }
            } catch (Throwable var21) {
               if (rs != null) {
                  try {
                     rs.close();
                  } catch (Throwable var20) {
                     var21.addSuppressed(var20);
                  }
               }

               throw var21;
            }

            if (rs != null) {
               rs.close();
            }
         } catch (Throwable var22) {
            if (ps != null) {
               try {
                  ps.close();
               } catch (Throwable var19) {
                  var22.addSuppressed(var19);
               }
            }

            throw var22;
         }

         if (ps != null) {
            ps.close();
         }
      } catch (SQLException var23) {
         var23.printStackTrace();
      } finally {
         if (con != null) {
            con.close();
         }

      }

      return items;
   }
}
