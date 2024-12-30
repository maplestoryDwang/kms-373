package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleShopLimit;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class MapleShop {
   private static final Set<Integer> rechargeableItems = new LinkedHashSet();
   private int id;
   private int npcId;
   private int coinKey;
   private int rechargeshop;
   private int questEx;
   private String saleString;
   private String shopString;
   private List<MapleShopItem> items;

   public MapleShop(int id, int npcId, int coinKey, int questEx, String shopString, String saleString) {
      this.id = id;
      this.npcId = npcId;
      this.coinKey = coinKey;
      this.questEx = questEx;
      this.shopString = shopString;
      this.saleString = saleString;
      this.items = new LinkedList();
   }

   public MapleShop(int id, int npcId, int coinKey, String shopString, String saleString, int rechargeshop) {
      this.id = id;
      this.npcId = npcId;
      this.coinKey = coinKey;
      this.shopString = shopString;
      this.saleString = saleString;
      this.rechargeshop = rechargeshop;
      this.items = new LinkedList();
   }

   public void addItem(MapleShopItem item) {
      this.items.add(item);
   }

   public void sendShop(MapleClient c) {
      this.sendShop(c, 0);
   }

   public void sendShop(MapleClient c, int id2) {
      if (this.items == null) {
         System.out.println("상점에 아무정보가 없습니다.");
      } else {
         if (MapleShopFactory.getInstance().getShop(this.id).getRechargeShop() == 1) {
            boolean active = false;
            boolean save = false;
            Calendar ocal = Calendar.getInstance();
            Iterator var6 = MapleShopFactory.getInstance().getShop(this.id).getItems().iterator();

            label295:
            while(true) {
               MapleShopItem item;
               int maxday;
               int month;
               int day;
               do {
                  do {
                     do {
                        if (!var6.hasNext()) {
                           if (active) {
                              MapleShopFactory.getInstance().clear();
                              MapleShopFactory.getInstance().getShop(this.id);
                           } else if (save) {
                              c.saveShopLimit(c.getShopLimit());
                           }
                           break label295;
                        }

                        item = (MapleShopItem)var6.next();
                        maxday = ocal.getActualMaximum(5);
                        month = ocal.get(2) + 1;
                        day = ocal.get(5);
                     } while(item.getReCharge() <= 0);

                     Iterator var11 = c.getShopLimit().iterator();

                     while(var11.hasNext()) {
                        MapleShopLimit shl = (MapleShopLimit)var11.next();
                        if (shl.getLastBuyMonth() > 0 && shl.getLastBuyDay() > 0) {
                           GregorianCalendar baseCal = new GregorianCalendar(ocal.get(1), shl.getLastBuyMonth(), shl.getLastBuyDay());
                           GregorianCalendar targetCal = new GregorianCalendar(ocal.get(1), month, day);
                           long diffSec = (targetCal.getTimeInMillis() - baseCal.getTimeInMillis()) / 1000L;
                           long diffDays = diffSec / 86400L;
                           if (shl.getItemid() == item.getItemId() && shl.getShopId() == MapleShopFactory.getInstance().getShop(this.id).getId() && shl.getPosition() == item.getPosition() && diffDays >= 0L) {
                              shl.setLastBuyMonth(0);
                              shl.setLastBuyDay(0);
                              shl.setLimitCountAcc(0);
                              shl.setLimitCountChr(0);
                              save = true;
                              break;
                           }
                        }
                     }
                  } while(item.getReChargeDay() > day);
               } while(item.getReChargeMonth() > month);

               active = true;
               int afterday = day + item.getReCharge();
               if (afterday > maxday) {
                  afterday -= maxday;
                  ++month;
                  if (month > 12) {
                     month = 1;
                  }
               }

               Connection con = null;
               PreparedStatement ps = null;

               try {
                  con = DatabaseConnection.getConnection();
                  ps = con.prepareStatement("UPDATE shopitems SET rechargemonth = ?, rechargeday = ?, resetday = ? WHERE position = ? AND itemid = ? AND tab = ?");
                  ps.setInt(1, month);
                  ps.setInt(2, afterday);
                  ps.setInt(3, day);
                  ps.setInt(4, item.getPosition());
                  ps.setInt(5, item.getItemId());
                  ps.setByte(6, item.getTab());
                  ps.executeUpdate();
                  ps.close();
                  con.close();
               } catch (SQLException var26) {
                  var26.printStackTrace();
               } finally {
                  try {
                     if (con != null) {
                        con.close();
                     }

                     if (ps == null) {
                        continue;
                     }

                     ps.close();
                  } catch (SQLException var27) {
                     var27.printStackTrace();
                  }

               }
            }
         }

         c.getPlayer().setShop(this);
         c.getSession().writeAndFlush(CField.NPCPacket.getNPCShop(id2 == 0 ? this.getNpcId() : id2, MapleShopFactory.getInstance().getShop(this.id), c));
         Iterator var29 = this.getItems().iterator();

         while(true) {
            while(true) {
               MapleShopItem item;
               do {
                  if (!var29.hasNext()) {
                     return;
                  }

                  item = (MapleShopItem)var29.next();
               } while(item.getReCharge() <= 0);

               Iterator var31 = c.getShopLimit().iterator();

               while(var31.hasNext()) {
                  MapleShopLimit shl = (MapleShopLimit)var31.next();
                  if (shl.getItemid() == item.getItemId() && shl.getShopId() == this.getId() && shl.getPosition() == item.getPosition()) {
                     c.send(CField.NPCPacket.getShopLimit(this.getNpcId(), item.getPosition() - 1, item.getItemId(), shl.getLimitCountAcc()));
                     break;
                  }
               }
            }
         }
      }
   }

   public List<MapleShopItem> getItems() {
      return this.items;
   }

   public void buy(MapleClient c, int itemId, short quantity, short position) {
      MapleShopItem item = this.findById(itemId, position);
      if (item != null && item.getItemId() == itemId) {
         if (item.getReCharge() > 0) {
            boolean add = true;
            Iterator var8 = c.getShopLimit().iterator();

            while(var8.hasNext()) {
               MapleShopLimit shl = (MapleShopLimit)var8.next();
               if (shl.getItemid() == item.getItemId() && shl.getShopId() == this.getId() && shl.getPosition() == item.getPosition()) {
                  add = false;
                  shl.setLimitCountAcc(shl.getLimitCountAcc() + 1);
                  shl.setLimitCountChr(shl.getLimitCountChr() + 1);
                  shl.setLastBuyMonth(item.getReChargeMonth());
                  shl.setLastBuyDay(item.getReChargeDay());
                  c.send(CField.NPCPacket.getShopLimit(this.getNpcId(), item.getPosition() - 1, itemId, shl.getLimitCountAcc()));
                  break;
               }
            }

            if (add) {
               MapleShopLimit Sl = new MapleShopLimit(c.getPlayer().getAccountID(), c.getPlayer().getId(), this.getId(), item.getItemId(), item.getPosition(), 1, 1, item.getReChargeMonth(), item.getReChargeDay());
               c.getShopLimit().add(Sl);
               c.send(CField.NPCPacket.getShopLimit(this.getNpcId(), item.getPosition() - 1, itemId, 1));
            }

            c.saveShopLimit(c.getShopLimit());
         }

         MapleItemInformationProvider ii;
         MaplePet var16;
         short var10002;
         String var10006;
         if (item != null && item.getPrice() > 0L && item.getPriceQuantity() == 0 && this.coinKey == 0) {
            if (c.getPlayer().getMeso() >= item.getPrice() * (long)quantity) {
               if (MapleInventoryManipulator.checkSpace(c, itemId, (short)(quantity * item.getQuantity()), "")) {
                  if (GameConstants.isPet(itemId)) {
                     var10002 = (short)(quantity * item.getQuantity());
                     var16 = MaplePet.createPet(itemId, (long)item.getPeriod());
                     var10006 = StringUtil.getAllCurrentTime();
                     MapleInventoryManipulator.addById(c, itemId, var10002, (String)null, var16, 0L, var10006 + "에 " + this.id + " 상점에서 구매한 아이템.");
                  } else {
                     ii = MapleItemInformationProvider.getInstance();
                     if (GameConstants.isRechargable(itemId)) {
                        quantity = ii.getSlotMax(item.getItemId());
                        c.getPlayer().gainMeso(-item.getPrice(), false);
                        MapleInventoryManipulator.addById(c, itemId, (short)(quantity * item.getQuantity()), (String)null, (MaplePet)null, (long)item.getPeriod(), "");
                     } else {
                        c.getPlayer().gainMeso(-(item.getPrice() * (long)quantity), false);
                        c.getPlayer().gainItem(itemId, (short)(item.getQuantity() * quantity), false, item.getPeriod() <= 0 ? 0L : (long)item.getPeriod(), "상점에서 구입한 아이템");
                     }
                  }

                  c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
               } else {
                  c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)7, this, c, -1, itemId));
               }
            } else {
               c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)2, this, c, -1, itemId));
            }
         } else {
            int buyable;
            MapleCharacter var10000;
            long var17;
            long var10004;
            String var10005;
            if (item != null && item.getPrice() > 0L && item.getPriceQuantity() > 0) {
               if (c.getPlayer().haveItem((int)item.getPrice(), item.getPriceQuantity() * quantity, false, true)) {
                  if (MapleInventoryManipulator.checkSpace(c, itemId, (short)(quantity * item.getQuantity()), "")) {
                     if (GameConstants.isPet(itemId)) {
                        MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType((int)item.getPrice()), (int)item.getPrice(), item.getPriceQuantity(), false, false);
                        var10002 = (short)(quantity * item.getQuantity());
                        var16 = MaplePet.createPet(itemId, (long)item.getPeriod());
                        var10006 = StringUtil.getAllCurrentTime();
                        MapleInventoryManipulator.addById(c, itemId, var10002, (String)null, var16, 30L, var10006 + "에 " + this.id + " 상점에서 구매한 아이템.");
                     } else {
                        ii = MapleItemInformationProvider.getInstance();
                        if (GameConstants.isRechargable(itemId)) {
                           quantity = ii.getSlotMax(item.getItemId());
                           MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType((int)item.getPrice()), (int)item.getPrice(), item.getPriceQuantity(), false, false);
                           var10002 = (short)(quantity * item.getQuantity());
                           var17 = (long)item.getPeriod();
                           var10006 = StringUtil.getAllCurrentTime();
                           MapleInventoryManipulator.addById(c, itemId, var10002, (String)null, (MaplePet)null, var17, var10006 + "에 " + this.id + " 상점에서 구매한 아이템.");
                        } else {
                           MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType((int)item.getPrice()), (int)item.getPrice(), item.getPriceQuantity() * quantity, false, false);
                           if (GameConstants.getInventoryType(item.getItemId()) != MapleInventoryType.EQUIP && GameConstants.getInventoryType(item.getItemId()) != MapleInventoryType.CODY) {
                              var10000 = c.getPlayer();
                              var10002 = (short)(item.getQuantity() * quantity);
                              var10004 = item.getPeriod() <= 0 ? 0L : (long)item.getPeriod();
                              var10005 = StringUtil.getAllCurrentTime();
                              var10000.gainItem(itemId, var10002, false, var10004, var10005 + "에 " + this.id + " 상점에서 구입한 아이템");
                           } else {
                              for(buyable = 0; buyable < quantity; ++buyable) {
                                 var10000 = c.getPlayer();
                                 var10004 = item.getPeriod() <= 0 ? 0L : (long)item.getPeriod() * 60L * 1000L + System.currentTimeMillis();
                                 var10005 = StringUtil.getAllCurrentTime();
                                 var10000.gainItem(itemId, (short)1, false, var10004, var10005 + "에 " + this.id + " 상점에서 구입한 아이템");
                              }
                           }
                        }
                     }

                     c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                  } else {
                     c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)7, this, c, -1, itemId));
                  }
               } else {
                  c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)1, this, c, -1, itemId));
               }
            } else {
               long var15;
               int var10001;
               long var10003;
               if (this.coinKey == 500629 && item != null && item.getPrice() > 0L) {
                  if (c.getKeyValue("유니온코인") == null) {
                     c.setKeyValue("유니온코인", c.getPlayer().getKeyValue(this.coinKey, "point").makeConcatWithConstants<invokedynamic>(c.getPlayer().getKeyValue(this.coinKey, "point")));
                  }

                  if (item.getPrice() >= 0L && (long)Integer.parseInt(c.getKeyValue("유니온코인")) >= item.getPrice()) {
                     if (MapleInventoryManipulator.checkSpace(c, itemId, (short)(quantity * item.getQuantity()), "")) {
                        var10000 = c.getPlayer();
                        var10001 = this.coinKey;
                        var10003 = c.getPlayer().getKeyValue(this.coinKey, "point") - item.getPrice() * (long)quantity;
                        var10000.setKeyValue(var10001, "point", var10003.makeConcatWithConstants<invokedynamic>(var10003));
                        var15 = (long)Integer.parseInt(c.getKeyValue("유니온코인"));
                        c.setKeyValue("유니온코인", (var15 - item.getPrice()).makeConcatWithConstants<invokedynamic>(var15 - item.getPrice()));
                        MapleInventoryManipulator.addById(c, itemId, (short)(quantity * item.getQuantity()), (String)null, (MaplePet)null, item.getPeriod() <= 0 ? 0L : (long)item.getPeriod(), "");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     } else {
                        c.getPlayer().dropMessage(1, "인벤토리가 부족합니다.");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     }
                  } else {
                     c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                  }
               } else if (this.coinKey == 501372 && item != null && item.getPrice() > 0L) {
                  if (item.getPrice() >= 0L && (long)Integer.parseInt(c.getKeyValue("WishCoinGain")) >= item.getPrice()) {
                     if (MapleInventoryManipulator.checkSpace(c, itemId, (short)(quantity * item.getQuantity()), "")) {
                        var10000 = c.getPlayer();
                        var10001 = this.coinKey;
                        var10003 = c.getPlayer().getKeyValue(this.coinKey, "point") - item.getPrice() * (long)quantity;
                        var10000.setKeyValue(var10001, "point", var10003.makeConcatWithConstants<invokedynamic>(var10003));
                        var15 = (long)Integer.parseInt(c.getKeyValue("WishCoinGain"));
                        c.setKeyValue("WishCoinGain", (var15 - item.getPrice()).makeConcatWithConstants<invokedynamic>(var15 - item.getPrice()));
                        MapleInventoryManipulator.addById(c, itemId, (short)(quantity * item.getQuantity()), (String)null, (MaplePet)null, item.getPeriod() <= 0 ? 0L : (long)item.getPeriod(), "");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     } else {
                        c.getPlayer().dropMessage(1, "인벤토리가 부족합니다.");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     }
                  } else {
                     c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                  }
               } else if (this.coinKey == 501215 && item != null && item.getPrice() > 0L) {
                  if (item.getPrice() >= 0L && (long)Integer.parseInt(c.getKeyValue("RecommendPoint")) >= item.getPrice()) {
                     if (MapleInventoryManipulator.checkSpace(c, itemId, (short)(quantity * item.getQuantity()), "")) {
                        var10000 = c.getPlayer();
                        var10001 = this.coinKey;
                        var10003 = c.getPlayer().getKeyValue(this.coinKey, "point") - item.getPrice() * (long)quantity;
                        var10000.setKeyValue(var10001, "point", var10003.makeConcatWithConstants<invokedynamic>(var10003));
                        var15 = (long)Integer.parseInt(c.getKeyValue("RecommendPoint"));
                        c.setKeyValue("RecommendPoint", (var15 - item.getPrice()).makeConcatWithConstants<invokedynamic>(var15 - item.getPrice()));
                        MapleInventoryManipulator.addById(c, itemId, (short)(quantity * item.getQuantity()), (String)null, (MaplePet)null, item.getPeriod() <= 0 ? 0L : (long)item.getPeriod(), "");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     } else {
                        c.getPlayer().dropMessage(1, "인벤토리가 부족합니다.");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     }
                  } else {
                     c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                  }
               } else if (this.coinKey == 501368 && item != null && item.getPrice() > 0L) {
                  if (item.getPrice() >= 0L && (long)Integer.parseInt(c.getCustomData(501368, "spoint")) >= item.getPrice()) {
                     if (MapleInventoryManipulator.checkSpace(c, itemId, (short)(quantity * item.getQuantity()), "")) {
                        var10000 = c.getPlayer();
                        var10003 = c.getPlayer().getKeyValue(501368, "point") - item.getPrice() * (long)quantity;
                        var10000.setKeyValue(501368, "point", var10003.makeConcatWithConstants<invokedynamic>(var10003));
                        var10003 = (long)Integer.parseInt(c.getCustomData(501368, "spoint"));
                        c.setCustomData(501368, "spoint", (var10003 - item.getPrice()).makeConcatWithConstants<invokedynamic>(var10003 - item.getPrice()));
                        MapleInventoryManipulator.addById(c, itemId, (short)(quantity * item.getQuantity()), (String)null, (MaplePet)null, item.getPeriod() <= 0 ? 0L : (long)item.getPeriod(), "");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     } else {
                        c.getPlayer().dropMessage(1, "인벤토리가 부족합니다.");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     }
                  } else {
                     c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                  }
               } else if (this.coinKey == 501468 && item != null && item.getPrice() > 0L) {
                  if (item.getPrice() >= 0L && c.getCustomKeyValue(501468, "point") >= item.getPrice()) {
                     if (MapleInventoryManipulator.checkSpace(c, itemId, (short)(quantity * item.getQuantity()), "")) {
                        var10001 = this.coinKey;
                        var10003 = c.getCustomKeyValue(501468, "point") - item.getPrice() * (long)quantity;
                        c.setCustomKeyValue(var10001, "point", var10003.makeConcatWithConstants<invokedynamic>(var10003));
                        MapleInventoryManipulator.addById(c, itemId, (short)(quantity * item.getQuantity()), (String)null, (MaplePet)null, item.getPeriod() <= 0 ? 0L : (long)item.getPeriod(), "");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     } else {
                        c.getPlayer().dropMessage(1, "인벤토리가 부족합니다.");
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                     }
                  } else {
                     c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                  }
               } else if (this.coinKey != 0 && item != null && item.getPrice() > 0L) {
                  if (!MapleInventoryManipulator.checkSpace(c, itemId, (short)(quantity * item.getQuantity()), "")) {
                     c.getPlayer().dropMessage(1, "인벤토리가 부족합니다.");
                     c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                  } else if (item.getPrice() >= 0L && c.getPlayer().getKeyValue(this.coinKey, "point") >= item.getPrice() * (long)quantity) {
                     buyable = (int)c.getPlayer().getKeyValue(this.getQuestEx() + 100000, item.getShopItemId() + "_buyed");
                     if (item.getBuyQuantity() != 0 && item.getBuyQuantity() - buyable <= 0) {
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)20, this, c, -1, itemId));
                        return;
                     }

                     if (MapleInventoryManipulator.checkSpace(c, itemId, (short)(quantity * item.getQuantity()), "")) {
                        c.getPlayer().setKeyValue(this.coinKey, "point", String.valueOf(c.getPlayer().getKeyValue(this.coinKey, "point") - item.getPrice() * (long)quantity));
                        if (GameConstants.isPet(itemId)) {
                           MapleInventoryManipulator.addById(c, itemId, (short)(quantity * item.getQuantity()), (String)null, MaplePet.createPet(itemId, (long)item.getPeriod()), 0L, "");
                        } else {
                           MapleItemInformationProvider ii3 = MapleItemInformationProvider.getInstance();
                           if (GameConstants.isRechargable(itemId)) {
                              quantity = ii3.getSlotMax(item.getItemId());
                              c.getPlayer().gainMeso(-item.getPrice(), false);
                              var10002 = (short)(quantity * item.getQuantity());
                              var17 = (long)item.getPeriod();
                              var10006 = StringUtil.getAllCurrentTime();
                              MapleInventoryManipulator.addById(c, itemId, var10002, (String)null, (MaplePet)null, var17, var10006 + "에 " + this.id + " 상점에서 구매한 아이템.");
                           } else if (GameConstants.getInventoryType(item.getItemId()) == MapleInventoryType.EQUIP) {
                              for(int i = 0; i < quantity; ++i) {
                                 var10000 = c.getPlayer();
                                 var10004 = item.getPeriod() <= 0 ? 0L : (long)item.getPeriod();
                                 var10005 = StringUtil.getAllCurrentTime();
                                 var10000.gainItem(itemId, (short)1, false, var10004, var10005 + "에 " + this.id + " 상점에서 구입한 아이템");
                              }
                           } else {
                              MapleInventoryManipulator.addById(c, itemId, (short)(quantity * item.getQuantity()), (String)null, (MaplePet)null, item.getPeriod() <= 0 ? 0L : (long)item.getPeriod(), "");
                           }
                        }

                        if (item.getBuyQuantity() != 0) {
                           if (c.getPlayer().getKeyValue(this.getQuestEx() + 100000, item.getShopItemId() + "_buyed") == -1L) {
                              c.getPlayer().setKeyValue(this.getQuestEx() + 100000, item.getShopItemId() + "_buyed", "0");
                           }

                           var10000 = c.getPlayer();
                           var10001 = this.getQuestEx() + 100000;
                           String var14 = item.getShopItemId() + "_buyed";
                           var10003 = c.getPlayer().getKeyValue(this.getQuestEx() + 100000, item.getShopItemId() + "_buyed");
                           var10000.setKeyValue(var10001, var14, (var10003 + 1L).makeConcatWithConstants<invokedynamic>(var10003 + 1L));
                           buyable = (int)c.getPlayer().getKeyValue(this.getQuestEx() + 100000, item.getShopItemId() + "_buyed");
                           c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId, item.getBuyQuantity() - buyable));
                           c.getSession().writeAndFlush(CField.NPCPacket.ShopItemInfoReset(this, c, itemId, buyable, item.getPosition()));
                        } else {
                           c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, itemId));
                        }
                     } else {
                        c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)7, this, c, -1, itemId));
                     }
                  } else {
                     c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)3, this, c, -1, itemId));
                  }

                  return;
               }
            }
         }

      } else {
         c.getPlayer().dropMessage(1, "아이템 정보를 불러오는 도중 오류가 발생했습니다.");
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public void sell(MapleClient c, MapleInventoryType type, short slot, short quantity) {
      if (quantity <= 0) {
         quantity = 1;
      }

      Item item = c.getPlayer().getInventory(type).getItem(slot);
      if (item != null) {
         if (item.getType() == 1) {
            Equip eq = (Equip)item;
            if (eq.getEnchantBuff() > 0) {
               c.getPlayer().dropMessage(1, "장비의 흔적은 이동이 불가합니다.");
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }
         }

         if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
            quantity = item.getQuantity();
         }

         short iQuant = item.getQuantity();
         if (iQuant == '\uffff') {
            iQuant = 1;
         }

         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         if (quantity <= iQuant && iQuant > 0) {
            Item itemm = item.copy();
            itemm.setQuantity(quantity);
            c.getPlayer().getRebuy().add(itemm);
            MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
            if (itemm.getReward() != null) {
               if (c.getPlayer().getKeyValue(501619, "count") <= 0L) {
                  System.out.println(c.getPlayer() + " 새끼가 결정석 핵 쓰려고 시도함.");
                  c.disconnect(true, false, false);
                  c.getSession().close();
                  return;
               }

               c.getSession().writeAndFlush(CWvsContext.setBossReward(c.getPlayer()));
            }

            double price;
            if (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId())) {
               if (itemm.getReward() != null) {
                  price = (double)itemm.getReward().getPrice();
                  quantity = 1;
               } else {
                  price = ii.getPrice(item.getItemId());
               }
            } else {
               price = (double)ii.getWholePrice(item.getItemId()) / (double)ii.getSlotMax(item.getItemId());
            }

            long recvMesos = (long)Math.max(Math.ceil(price * (double)quantity), 0.0D);
            if (price != -1.0D && recvMesos > 0L) {
               c.getPlayer().gainMeso(recvMesos, false);
            }

            c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)11, this, c, -1, item.getItemId()));
         }

      }
   }

   public void recharge(MapleClient c, short slot) {
      Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
      if (item != null && (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId()))) {
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         short slotMax = ii.getSlotMax(item.getItemId());
         int skill = GameConstants.getMasterySkill(c.getPlayer().getJob());
         if (skill != 0) {
            slotMax = (short)(slotMax + c.getPlayer().getSkillLevel(SkillFactory.getSkill(skill)) * 10);
         }

         if (item.getQuantity() < slotMax) {
            int price = (int)Math.round(ii.getPrice(item.getItemId()) * (double)(slotMax - item.getQuantity()));
            if (c.getPlayer().getMeso() >= (long)price) {
               item.setQuantity(slotMax);
               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventorySlot(MapleInventoryType.USE, item, false));
               c.getPlayer().gainMeso((long)(-price), false, true);
               c.getSession().writeAndFlush(CField.NPCPacket.confirmShopTransactionItem((byte)0, this, c, -1, item.getItemId()));
            }
         }

      }
   }

   protected MapleShopItem findById(int itemId, int position) {
      Iterator var3 = this.items.iterator();

      MapleShopItem item;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         item = (MapleShopItem)var3.next();
      } while(item.getItemId() != itemId || item.getPosition() != position);

      return item;
   }

   public static MapleShop createFromDB(int id, boolean isShopId) {
      MapleShop ret = null;
      Connection con = null;

      ArrayList recharges;
      try {
         con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(isShopId ? "SELECT * FROM shops WHERE shopid = ?" : "SELECT * FROM shops WHERE npcid = ?");
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            int shopId = rs.getInt("shopid");
            ret = new MapleShop(shopId, rs.getInt("npcid"), rs.getInt("coinKey"), rs.getString("shopString"), rs.getString("saleString"), rs.getInt("rechargeshop"));
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            recharges = new ArrayList(rechargeableItems);

            int i;
            for(i = 1; rs.next(); ++i) {
               if (!GameConstants.isThrowingStar(rs.getInt("itemid")) && !GameConstants.isBullet(rs.getInt("itemid"))) {
                  ret.addItem(new MapleShopItem(rs.getInt("shopitemid"), (short)1000, rs.getInt("itemid"), (long)rs.getInt("price"), rs.getInt("pricequantity"), (byte)rs.getInt("Tab"), rs.getInt("quantity"), rs.getInt("period"), i, rs.getInt("itemrate"), rs.getInt("buyquantity"), rs.getInt("rechargemonth"), rs.getInt("rechargeday"), rs.getInt("resetday"), rs.getInt("recharge"), rs.getInt("rechargecount")));
               } else {
                  MapleShopItem starItem = new MapleShopItem(rs.getInt("shopitemid"), (short)1, rs.getInt("itemid"), (long)rs.getInt("price"), rs.getInt("pricequantity"), (byte)rs.getInt("Tab"), rs.getInt("quantity"), rs.getInt("period"), i, rs.getInt("itemrate"), rs.getInt("buyquantity"), rs.getInt("rechargemonth"), rs.getInt("rechargeday"), rs.getInt("resetday"), rs.getInt("recharge"), rs.getInt("rechargecount"));
                  ret.addItem(starItem);
                  if (rechargeableItems.contains(starItem.getItemId())) {
                     recharges.remove(starItem.getItemId());
                  }
               }
            }

            i = 1;

            for(Iterator var22 = recharges.iterator(); var22.hasNext(); ++i) {
               Integer recharge = (Integer)var22.next();
               ret.addItem(new MapleShopItem(0, (short)1000, recharge, 0L, 0, (byte)0, 0, 0, i, 0, 0, 0, 0, 0, 0, 0));
            }

            rs.close();
            ps.close();
            return ret;
         }

         rs.close();
         ps.close();
         recharges = null;
      } catch (SQLException var20) {
         System.err.println("Could not load shop" + var20);
         return ret;
      } finally {
         if (con != null) {
            try {
               con.close();
            } catch (SQLException var19) {
               Logger.getLogger(MapleShop.class.getName()).log(Level.SEVERE, (String)null, var19);
            }
         }

      }

      return recharges;
   }

   public int getNpcId() {
      return this.npcId;
   }

   public int getId() {
      return this.id;
   }

   public int getCoinKey() {
      return this.coinKey;
   }

   public void setCoinKey(int coinKey) {
      this.coinKey = coinKey;
   }

   public String getSaleString() {
      return this.saleString;
   }

   public void setSaleString(String saleString) {
      this.saleString = saleString;
   }

   public String getShopString() {
      return this.shopString;
   }

   public void setShopString(String shopString) {
      this.shopString = shopString;
   }

   public int getQuestEx() {
      return this.questEx;
   }

   public void setQuestEx(int questEx) {
      this.questEx = questEx;
   }

   public int getRechargeShop() {
      return this.rechargeshop;
   }

   static {
      rechargeableItems.add(2070000);
      rechargeableItems.add(2070001);
      rechargeableItems.add(2070002);
      rechargeableItems.add(2070003);
      rechargeableItems.add(2070004);
      rechargeableItems.add(2070005);
      rechargeableItems.add(2070006);
      rechargeableItems.add(2070007);
      rechargeableItems.add(2070008);
      rechargeableItems.add(2070009);
      rechargeableItems.add(2070010);
      rechargeableItems.add(2070011);
      rechargeableItems.add(2070012);
      rechargeableItems.add(2070013);
      rechargeableItems.add(2070023);
      rechargeableItems.add(2070024);
      rechargeableItems.add(2070026);
      rechargeableItems.add(2330000);
      rechargeableItems.add(2330001);
      rechargeableItems.add(2330002);
      rechargeableItems.add(2330003);
      rechargeableItems.add(2330004);
      rechargeableItems.add(2330005);
      rechargeableItems.add(2330008);
      rechargeableItems.add(2330016);
      rechargeableItems.add(2331000);
      rechargeableItems.add(2332000);
   }
}
