package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConstants;
import constants.programs.AdminTool;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import log.DBLogger;
import log.LogType;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.Randomizer;
import server.Timer;
import server.games.OneCardGame;
import server.maps.FieldLimitType;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.marriage.MarriageMiniBox;
import server.shops.IMaplePlayerShop;
import server.shops.MapleMiniGame;
import server.shops.MaplePlayerShop;
import server.shops.MaplePlayerShopItem;
import tools.FileoutputUtil;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.PlayerShopPacket;
import tools.packet.SLFCGPacket;

public class PlayerInteractionHandler {
   public static final void PlayerInteraction(LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
      PlayerInteractionHandler.Interaction action = PlayerInteractionHandler.Interaction.getByAction(slea.readByte() & 255);
      if (chr != null && action != null) {
         c.getPlayer().setScrolledPosition((short)0);
         int obid;
         byte type3;
         byte createType;
         IMaplePlayerShop ips3;
         int objectId;
         MapleMiniGame game2;
         int j;
         int turn;
         int itemId;
         MaplePlayerShopItem items;
         String message;
         String pass;
         Item shop;
         short perBundle;
         IMaplePlayerShop ips;
         String pass2;
         long price;
         IMaplePlayerShop iMaplePlayerShop;
         short slot;
         String desc;
         switch(action) {
         case CREATE:
            if (chr.getPlayerShop() != null || c.getChannelServer().isShutdown()) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            createType = slea.readByte();
            if (createType != 3 && createType != 4) {
               if (createType != 1 && createType != 2 && createType != 5 && createType != 6) {
                  break;
               }

               if (chr.getMap().getMapObjectsInRange(chr.getTruePosition(), 20000.0D, Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.HIRED_MERCHANT)).size() != 0 || chr.getMap().getPortalsInRange(chr.getTruePosition(), 20000.0D).size() != 0) {
                  chr.dropMessage(1, "이곳에 상점을 세울 수 없습니다.");
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               if ((createType == 1 || createType == 2) && (FieldLimitType.Minigames.check(chr.getMap().getFieldLimit()) || chr.getMap().allowPersonalShop())) {
                  chr.dropMessage(1, "이곳에 미니게임을 개설할 수 없습니다.");
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               desc = slea.readMapleAsciiString();
               pass = "";
               if (slea.readByte() > 0) {
                  pass = slea.readMapleAsciiString();
               }

               if (createType != 1 && createType != 2) {
                  if (chr.getMap().allowPersonalShop()) {
                     shop = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slea.readShort());
                     if (shop == null || shop.getQuantity() <= 0 || shop.getItemId() != slea.readInt() || c.getPlayer().getMapId() < 910000001 || c.getPlayer().getMapId() > 910000022) {
                        return;
                     }

                     if (createType == 4) {
                     }
                  }
                  break;
               }

               int piece = slea.readByte();
               itemId = createType == 1 ? 4080000 + piece : 4080100;
               if (!chr.haveItem(itemId) || c.getPlayer().getMapId() >= 910000001 && c.getPlayer().getMapId() <= 910000022) {
                  return;
               }

               MapleMiniGame game = new MapleMiniGame(chr, itemId, desc, pass, createType);
               game.setPieceType(piece);
               chr.setPlayerShop(game);
               game.setAvailable(true);
               game.setOpen(true);
               game.send(c);
               chr.getMap().addMapObject(game);
               game.update();
               break;
            }

            MapleTrade.startTrade(chr, createType == 4);
            break;
         case INVITE_TRADE:
            if (chr.getMap() == null) {
               return;
            }

            obid = slea.readInt();
            MapleCharacter chrr = chr.getMap().getCharacterById(obid);
            if (chrr == null || c.getChannelServer().isShutdown()) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            MapleTrade.inviteTrade(chr, chrr, true);
            break;
         case INVITE_ROCK_PAPER_SCISSORS:
            if (chr.getMap() == null) {
               return;
            }

            MapleCharacter chrr2 = chr.getMap().getCharacterById(slea.readInt());
            if (chrr2 == null || c.getChannelServer().isShutdown()) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            MapleTrade.inviteTrade(chr, chrr2, false);
            break;
         case DENY_TRADE:
            if (chr.getMarriage() != null) {
               chr.getMarriage().closeMarriageBox(true, 24);
               chr.setMarriage((MarriageMiniBox)null);
            } else {
               MapleTrade.declineTrade(chr);
            }
            break;
         case WEDDING_START:
            c.getPlayer().getMarriage().StartMarriage();
            break;
         case WEDDING_END:
            c.getPlayer().getMarriage().EndMarriage();
            break;
         case VISIT:
            if (c.getChannelServer().isShutdown()) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            if (chr.getTrade() == null && chr.getPlayerShop() != null) {
               chr.dropMessage(1, "이미 닫힌 방입니다.");
               return;
            }

            if (chr.getTrade() != null && chr.getTrade().getPartner() != null && !chr.getTrade().inTrade()) {
               MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr(), chr.getTrade().getPartner().getChr().isTrade);
            } else if (chr.getMap() != null && chr.getTrade() == null) {
               obid = slea.readInt();
               if (obid == 0) {
                  if (chr.getMarriage() == null || chr.getMarriage().getPlayer1().getMarriage() == null) {
                     chr.dropMessage(1, "이미 닫힌 방입니다.");
                     return;
                  }

                  if (chr.getMarriage() != null && chr.getMarriage().getPartnerId() == chr.getId()) {
                     chr.setPlayerShop(chr.getMarriage());
                     chr.getMarriage().setPlayer2(chr);
                     chr.getMarriage().setAvailable(true);
                     chr.getMarriage().addVisitor(chr);
                     chr.getMarriage().send(c);
                     chr.getMarriage().update();
                     return;
                  }
               }

               MapleMapObject ob = chr.getMap().getMapObject(obid, MapleMapObjectType.HIRED_MERCHANT);
               if (ob == null) {
                  ob = chr.getMap().getMapObject(obid, MapleMapObjectType.SHOP);
               }

               if (ob instanceof IMaplePlayerShop && chr.getPlayerShop() == null) {
                  ips = (IMaplePlayerShop)ob;
                  if (ips instanceof MaplePlayerShop && ((MaplePlayerShop)ips).isBanned(chr.getName())) {
                     chr.dropMessage(1, "상점에서 강퇴당했습니다.");
                     return;
                  }

                  if (ips.getFreeSlot() >= 0 && ips.getVisitorSlot(chr) <= -1 && ips.isOpen() && ips.isAvailable()) {
                     if (slea.available() > 0L && slea.readByte() > 0) {
                        pass2 = slea.readMapleAsciiString();
                        if (!pass2.equals(ips.getPassword())) {
                           c.getPlayer().dropMessage(1, "The password you entered is incorrect.");
                           return;
                        }
                     } else if (ips.getPassword().length() > 0) {
                        c.getPlayer().dropMessage(1, "The password you entered is incorrect.");
                        return;
                     }

                     chr.setPlayerShop(ips);
                     ips.addVisitor(chr);
                     if (ips instanceof MarriageMiniBox) {
                        ((MarriageMiniBox)ips).send(c);
                     } else if (ips instanceof MapleMiniGame) {
                        ((MapleMiniGame)ips).send(c);
                     } else {
                        c.getSession().writeAndFlush(PlayerShopPacket.getPlayerStore(chr, false));
                     }
                  } else {
                     c.getSession().writeAndFlush(PlayerShopPacket.getMiniGameFull());
                  }
               }
            }
            break;
         case HIRED_MERCHANT_MAINTENANCE:
            if (c.getChannelServer().isShutdown() || chr.getMap() == null || chr.getTrade() != null) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            slea.skip(1);
            createType = slea.readByte();
            if (createType != 5) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            desc = slea.readMapleAsciiString();
            j = slea.readInt();
            MapleMapObject ob2 = chr.getMap().getMapObject(j, MapleMapObjectType.HIRED_MERCHANT);
            if (ob2 == null || chr.getPlayerShop() != null) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }
            break;
         case CHAT:
            slea.readInt();
            message = slea.readMapleAsciiString();
            String var10000;
            int var82;
            if (chr.getTrade() != null) {
               chr.getTrade().chat(message);
               var82 = c.getChannel();
               AdminTool.addMessage(7, "[" + var82 + "채널] " + c.getPlayer().getName() + " : " + message);
               var10000 = FileoutputUtil.교환채팅로그;
               var82 = c.getChannel();
               FileoutputUtil.log(var10000, "[교환] [" + var82 + "채널] 상대방이름 : " + chr.getTrade().getPartner().getChr().getName() + " | " + c.getPlayer().getName() + " : " + message);
            } else if (chr.getPlayerShop() != null) {
               iMaplePlayerShop = chr.getPlayerShop();
               if (iMaplePlayerShop instanceof MapleMiniGame) {
                  iMaplePlayerShop.broadcastToVisitors(PlayerShopPacket.shopChat(chr, chr.getName(), chr.getId(), message.makeConcatWithConstants<invokedynamic>(message), iMaplePlayerShop.getVisitorSlot(chr)));
               } else {
                  iMaplePlayerShop.broadcastToVisitors(PlayerShopPacket.shopChat(chr, chr.getName(), chr.getId(), chr.getName() + " : " + message, iMaplePlayerShop.getVisitorSlot(chr)));
               }

               LogType.Chat chatType = LogType.Chat.PlayerShop;
               pass2 = "";
               if (iMaplePlayerShop instanceof MaplePlayerShop) {
                  chatType = LogType.Chat.PlayerShop;
                  var10000 = iMaplePlayerShop.getOwnerName();
                  pass2 = "주인 : " + var10000 + " / 상점명 : " + iMaplePlayerShop.getDescription() + " / 수신 : " + iMaplePlayerShop.getMemberNames();
               } else if (iMaplePlayerShop instanceof MapleMiniGame) {
                  chatType = LogType.Chat.MiniGame;
                  var10000 = iMaplePlayerShop.getOwnerName();
                  pass2 = "주인 : " + var10000 + " / 게임명 : " + iMaplePlayerShop.getDescription() + " / 암호 : " + (iMaplePlayerShop.getPassword() == null ? "없음" : "있음 - " + iMaplePlayerShop.getPassword()) + " / 수신 : " + iMaplePlayerShop.getMemberNames();
               }

               var82 = c.getChannel();
               AdminTool.addMessage(8, "[" + var82 + "채널] " + c.getPlayer().getName() + " : " + message);
               var10000 = FileoutputUtil.미니게임채팅로그;
               var82 = c.getChannel();
               FileoutputUtil.log(var10000, "[미니게임] [" + var82 + "채널] | " + c.getPlayer().getName() + " : " + message);
               DBLogger.getInstance().logChat(chatType, c.getPlayer().getId(), c.getPlayer().getName(), message, pass2);
               if (chr.getClient().isMonitored()) {
               }
            }
            break;
         case EXIT:
            if (chr.getTrade() != null) {
               MapleTrade.cancelTrade(chr.getTrade(), chr.getClient(), chr);
            } else if (chr.getOneCardInstance() != null) {
               chr.getOneCardInstance().sendPacketToPlayers(SLFCGPacket.leaveResult(chr.getOneCardInstance().getPlayer(chr).getPosition()));
               chr.getOneCardInstance().playerDead(chr.getOneCardInstance().getPlayer(chr), false);
            } else if (chr.getBattleReverseInstance() != null) {
               chr.getBattleReverseInstance().endGame(chr, true);
            } else {
               ips3 = chr.getPlayerShop();
               if (ips3 == null) {
                  return;
               }

               if (ips3.isOwner(chr) && ips3.getShopType() != 1) {
                  ips3.closeShop(false, ips3.isAvailable());
               } else {
                  ips3.removeVisitor(chr);
               }

               chr.setPlayerShop((IMaplePlayerShop)null);
            }
            break;
         case OPEN:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3.isOwner(chr) && ips3.getShopType() < 3 && !ips3.isAvailable()) {
               if (!chr.getMap().allowPersonalShop()) {
                  c.disconnect(true, false, false);
                  c.getSession().close();
               } else {
                  if (c.getChannelServer().isShutdown()) {
                     chr.dropMessage(1, "서버가 곧 종료되기때문에, 상점을 세울수 없습니다.");
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     ips3.closeShop(ips3.getShopType() == 1, false);
                     return;
                  }

                  if (ips3.getShopType() == 2) {
                     ips3.setOpen(true);
                     ips3.setAvailable(true);
                     ips3.update();
                  }
               }
            }
            break;
         case SET_ITEMS4:
         case SET_ITEMS3:
         case SET_ITEMS2:
         case SET_ITEMS1:
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
            Item item = chr.getInventory(ivType).getItem(slea.readShort());
            perBundle = slea.readShort();
            byte targetSlot = slea.readByte();
            if (chr.getTrade() != null && item != null && (perBundle <= item.getQuantity() && perBundle >= 0 || GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId()))) {
               chr.getTrade().setItems(c, item, targetSlot, perBundle);
            }
            break;
         case SET_MESO4:
         case SET_MESO3:
         case SET_MESO2:
         case SET_MESO1:
            MapleTrade trade = chr.getTrade();
            if (trade != null) {
               long meso = slea.readLong();
               if (meso < 0L) {
                  meso &= 4294967295L;
               }

               trade.setMeso(meso);
            }
            break;
         case ADD_ITEM4:
         case ADD_ITEM3:
         case ADD_ITEM2:
         case ADD_ITEM1:
            MapleInventoryType type2 = MapleInventoryType.getByType(slea.readByte());
            slot = slea.readShort();
            short bundles = slea.readShort();
            perBundle = slea.readShort();
            price = slea.readLong();
            if (price <= 0L || bundles <= 0 || perBundle <= 0) {
               return;
            }

            IMaplePlayerShop shop3 = chr.getPlayerShop();
            if (shop3 == null || !shop3.isOwner(chr) || shop3 instanceof MapleMiniGame) {
               return;
            }

            Item ivItem = chr.getInventory(type2).getItem(slot);
            MapleItemInformationProvider ii2 = MapleItemInformationProvider.getInstance();
            if (ivItem != null) {
               long check = (long)(bundles * perBundle);
               if (check > 32767L || check <= 0L) {
                  return;
               }

               short bundles_perbundle = (short)(bundles * perBundle);
               if (ivItem.getQuantity() < bundles_perbundle) {
                  chr.dropMessage(1, "물품을 판매하려면 적어도 1개이상 있어야 합니다.");
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               int flag = ivItem.getFlag();
               if (ItemFlag.LOCK.check(flag)) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               if ((ii2.isDropRestricted(ivItem.getItemId()) || ii2.isAccountShared(ivItem.getItemId()) || ItemFlag.UNTRADEABLE.check(flag)) && !ItemFlag.KARMA_EQUIP.check(flag) && !ItemFlag.KARMA_USE.check(flag)) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               if (bundles_perbundle >= 50 && ivItem.getItemId() == 2340000) {
               }

               if ((long)GameConstants.getLowestPrice(ivItem.getItemId()) > price) {
                  c.getPlayer().dropMessage(1, "The lowest you can sell this for is " + GameConstants.getLowestPrice(ivItem.getItemId()));
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               Item sellItem;
               if (!GameConstants.isThrowingStar(ivItem.getItemId()) && !GameConstants.isBullet(ivItem.getItemId())) {
                  MapleInventoryManipulator.removeFromSlot(c, type2, slot, bundles_perbundle, true);
                  sellItem = ivItem.copy();
                  sellItem.setQuantity(perBundle);
                  shop3.addItem(new MaplePlayerShopItem(sellItem, bundles, price));
               } else {
                  MapleInventoryManipulator.removeFromSlot(c, type2, slot, ivItem.getQuantity(), true);
                  sellItem = ivItem.copy();
                  shop3.addItem(new MaplePlayerShopItem(sellItem, (short)1, price));
               }

               c.getSession().writeAndFlush(PlayerShopPacket.shopItemUpdate(shop3));
            }
            break;
         case CONFIRM_TRADE_MESO1:
         case CONFIRM_TRADE_MESO2:
         case CONFIRM_TRADE2:
         case CONFIRM_TRADE1:
         case BUY_ITEM_PLAYER_SHOP:
         case BUY_ITEM_STORE:
         case BUY_ITEM_HIREDMERCHANT:
            if (chr.getTrade() != null) {
               MapleTrade.completeTrade(chr);
            } else {
               createType = slea.readByte();
               slot = slea.readShort();
               ips = chr.getPlayerShop();
               if (ips == null || ips.isOwner(chr) || ips instanceof MapleMiniGame || createType >= ips.getItems().size()) {
                  return;
               }

               items = (MaplePlayerShopItem)ips.getItems().get(createType);
               if (items == null) {
                  return;
               }

               price = (long)(items.bundles * slot);
               long check3 = items.price * (long)slot;
               long check4 = (long)(items.item.getQuantity() * slot);
               if (price <= 0L || check3 > 2147483647L || check3 <= 0L || check4 > 32767L || check4 < 0L) {
                  return;
               }

               if (items.bundles < slot || items.bundles % slot != 0 && GameConstants.isEquip(items.item.getItemId()) || chr.getMeso() - check3 < 0L || chr.getMeso() - check3 > 2147483647L || ips.getMeso() + check3 < 0L || ips.getMeso() + check3 > 2147483647L) {
                  return;
               }

               if (slot >= 50 && items.item.getItemId() == 2340000) {
                  c.setMonitored(true);
               }

               ips.buy(c, createType, slot);
               ips.broadcastToVisitors(PlayerShopPacket.shopItemUpdate(ips));
            }
            break;
         case RESET_HIRED:
            createType = slea.readByte();
            type3 = slea.readByte();
            if (createType != 19 || type3 != 5 && type3 != 6) {
               if (createType == 11 && type3 == 4) {
                  ips = chr.getPlayerShop();
                  ips.setOpen(true);
                  ips.setAvailable(true);
                  ips.update();
               } else if (createType == 16 && type3 == 7) {
                  MapleCharacter chrr3 = chr.getMap().getCharacterById(slea.readInt());
                  if (chrr3 == null || c.getChannelServer().isShutdown()) {
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     return;
                  }

                  MapleTrade.startCashTrade(chr);
                  MapleTrade.inviteCashTrade(chr, chrr3);
               } else if (createType == 19 && type3 == 7) {
                  pass = slea.readMapleAsciiString();
                  if (c.CheckSecondPassword(pass)) {
                     if (chr != null && chr.getTrade() != null && chr.getTrade().getPartner() != null && chr.getTrade().getPartner().getChr() != null) {
                        MapleTrade.visitCashTrade(chr, chr.getTrade().getPartner().getChr());
                     } else {
                        c.getPlayer().dropMessage(1, "오류가 발생했습니다. \r\n잠시 후 다시 시도해 주세요.");
                     }
                  } else {
                     c.getPlayer().dropMessage(1, "2차비밀번호가 일치하지 않습니다. \r\n확인 후 다시 시도해 주세요.");
                  }
               }
            } else {
               pass = slea.readMapleAsciiString();
               if (c.CheckSecondPassword(pass)) {
                  turn = slea.readInt();
                  MapleMapObject ob3 = chr.getMap().getMapObject(turn, MapleMapObjectType.HIRED_MERCHANT);
                  if (ob3 == null) {
                     return;
                  }
               } else {
                  c.getPlayer().dropMessage(1, "2차비밀번호가 일치하지 않습니다. \r\n확인 후 다시 시도해 주세요.");
               }
            }
            break;
         case REMOVE_ITEM:
            slea.skip(1);
            int slot2 = slea.readShort();
            iMaplePlayerShop = chr.getPlayerShop();
            if (iMaplePlayerShop == null || !iMaplePlayerShop.isOwner(chr) || iMaplePlayerShop instanceof MapleMiniGame || iMaplePlayerShop.getItems().size() <= 0 || iMaplePlayerShop.getItems().size() <= slot2 || slot2 < 0) {
               return;
            }

            MaplePlayerShopItem item3 = (MaplePlayerShopItem)iMaplePlayerShop.getItems().get(slot2);
            if (item3 != null && item3.bundles > 0) {
               shop = item3.item.copy();
               price = (long)(item3.bundles * item3.item.getQuantity());
               if (price < 0L || price > 32767L) {
                  return;
               }

               shop.setQuantity((short)((int)price));
               if (shop.getQuantity() >= 50 && item3.item.getItemId() == 2340000) {
                  c.setMonitored(true);
               }

               if (MapleInventoryManipulator.checkSpace(c, shop.getItemId(), shop.getQuantity(), shop.getOwner())) {
                  MapleInventoryManipulator.addFromDrop(c, shop, false);
                  item3.bundles = 0;
                  iMaplePlayerShop.removeFromSlot(slot2);
               }
            }

            c.getSession().writeAndFlush(PlayerShopPacket.shopItemUpdate(iMaplePlayerShop));
            break;
         case MAINTANCE_ORGANISE:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3.isOwner(chr) && !(ips3 instanceof MapleMiniGame)) {
               for(objectId = 0; objectId < ips3.getItems().size(); ++objectId) {
                  if (((MaplePlayerShopItem)ips3.getItems().get(objectId)).bundles == 0) {
                     ips3.getItems().remove(objectId);
                  }
               }

               if (chr.getMeso() + ips3.getMeso() > 0L) {
                  chr.gainMeso(ips3.getMeso(), false);
                  ips3.setMeso(0L);
               }

               c.getSession().writeAndFlush(PlayerShopPacket.shopItemUpdate(ips3));
            }
            break;
         case CLOSE_MERCHANT:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3.getShopType() == 1 && ips3.isOwner(chr)) {
               boolean save = false;
               if (chr.getMeso() + ips3.getMeso() < 0L) {
                  save = false;
               } else {
                  if (ips3.getMeso() > 0L) {
                     chr.gainMeso(ips3.getMeso(), false);
                  }

                  ips3.setMeso(0L);
                  if (ips3.getItems().size() > 0) {
                     Iterator var27 = ips3.getItems().iterator();

                     while(var27.hasNext()) {
                        items = (MaplePlayerShopItem)var27.next();
                        if (items.bundles > 0) {
                           Item item_get2 = items.item.copy();
                           item_get2.setQuantity((short)(items.bundles * items.item.getQuantity()));
                           if (!MapleInventoryManipulator.addFromDrop(c, item_get2, false)) {
                              save = true;
                              break;
                           }

                           items.bundles = 0;
                           save = false;
                        }
                     }
                  }
               }

               if (save) {
                  c.getPlayer().dropMessage(1, "프레드릭 에게서 아이템을 찾아가 주십시오.");
                  c.getSession().writeAndFlush(PlayerShopPacket.shopErrorMessage(20, 0));
               } else {
                  c.getSession().writeAndFlush(PlayerShopPacket.MerchantClose(0, 0));
               }

               ips3.closeShop(save, true);
               chr.setPlayerShop((IMaplePlayerShop)null);
            }
            break;
         case TAKE_MESOS:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3.isOwner(chr)) {
               if (chr.getMeso() + ips3.getMeso() < 0L) {
                  c.getSession().writeAndFlush(PlayerShopPacket.shopItemUpdate(ips3));
               } else {
                  chr.gainMeso(ips3.getMeso(), false);
                  ips3.setMeso(0L);
                  c.getSession().writeAndFlush(PlayerShopPacket.shopItemUpdate(ips3));
               }
            }
            break;
         case ADMIN_STORE_NAMECHANGE:
            message = slea.readMapleAsciiString();
            c.getSession().writeAndFlush(PlayerShopPacket.merchantNameChange(chr.getId(), message));
            break;
         case GIVE_UP:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOpen()) {
                  game2.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game2, 0, game2.getVisitorSlot(chr)));
                  game2.nextLoser();
                  game2.setOpen(true);
                  game2.update();
                  game2.checkExitAfterGame();
               }
            }
            break;
         case EXPEL:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame && ((MapleMiniGame)ips3).isOpen()) {
               ips3.removeAllVisitors(5, 1);
            }
            break;
         case READY:
         case UN_READY:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOwner(chr) && game2.isOpen()) {
                  game2.setReady(game2.getVisitorSlot(chr));
                  game2.broadcastToVisitors(PlayerShopPacket.getMiniGameReady(game2.isReady(game2.getVisitorSlot(chr))));
               }
            }
            break;
         case START:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (game2.isOwner(chr) && game2.isOpen()) {
                  for(j = 1; j < ips3.getSize(); ++j) {
                     if (!game2.isReady(j)) {
                        return;
                     }
                  }

                  game2.setGameType();
                  game2.shuffleList();
                  if (game2.getGameType() == 1) {
                     game2.broadcastToVisitors(PlayerShopPacket.getMiniGameStart(game2.getLoser()));
                  } else {
                     game2.broadcastToVisitors(PlayerShopPacket.getMatchCardStart(game2, game2.getLoser()));
                  }

                  game2.setOpen(false);
                  game2.update();
                  game2.broadcastToVisitors(PlayerShopPacket.getMiniGameInfoMsg((byte)102, chr.getName()));
               }
            } else if (chr.getTrade() != null && chr.getTrade().getPartner() != null) {
               c.getSession().writeAndFlush(PlayerShopPacket.StartRPS());
               chr.getTrade().getPartner().getChr().getClient().getSession().writeAndFlush(PlayerShopPacket.StartRPS());
            }
            break;
         case REQUEST_TIE:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOpen()) {
                  if (game2.isOwner(chr)) {
                     game2.broadcastToVisitors(PlayerShopPacket.getMiniGameRequestTie(), false);
                  } else {
                     game2.getMCOwner().getClient().getSession().writeAndFlush(PlayerShopPacket.getMiniGameRequestTie());
                  }

                  game2.setRequestedTie(game2.getVisitorSlot(chr));
               }
            }
            break;
         case ANSWER_TIE:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOpen() && game2.getRequestedTie() > -1 && game2.getRequestedTie() != game2.getVisitorSlot(chr)) {
                  if (slea.readByte() > 0) {
                     game2.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game2, 1, game2.getRequestedTie()));
                     game2.nextLoser();
                     game2.setOpen(true);
                     game2.update();
                     game2.checkExitAfterGame();
                  } else {
                     game2.broadcastToVisitors(PlayerShopPacket.getMiniGameDenyTie());
                  }

                  game2.setRequestedTie(-1);
               }
            }
            break;
         case REQUEST_REDO:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOpen()) {
                  if (game2.isOwner(chr)) {
                     game2.broadcastToVisitors(PlayerShopPacket.getMiniGameRequestRedo(), false);
                  } else {
                     game2.getMCOwner().getClient().getSession().writeAndFlush(PlayerShopPacket.getMiniGameRequestRedo());
                  }
               }
            }
            break;
         case ANSWER_REDO:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOpen()) {
                  if (slea.readByte() > 0) {
                     ips3.broadcastToVisitors(PlayerShopPacket.getMiniGameSkip(ips3.getVisitorSlot(chr) == 0 ? 1 : 0));
                     game2.nextLoser();
                  } else {
                     game2.broadcastToVisitors(PlayerShopPacket.getMiniGameDenyRedo());
                  }
               }
            }
            break;
         case SKIP:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOpen()) {
                  if (game2.getLoser() != ips3.getVisitorSlot(chr)) {
                     return;
                  }

                  ips3.broadcastToVisitors(PlayerShopPacket.getMiniGameSkip(ips3.getVisitorSlot(chr) == 0 ? 1 : 0));
                  game2.nextLoser();
               }
            } else if (chr.getTrade() != null && chr.getTrade().getPartner() != null) {
               chr.getTrade().setRPS(slea.readByte());
               Timer.ShowTimer.getInstance().schedule(new Runnable() {
                  public void run() {
                     byte result = PlayerInteractionHandler.getResult(chr.getTrade().getPRS(), chr.getTrade().getPartner().getPRS());
                     if (result == 2) {
                        chr.dropMessage(1, "아쉽지만, 가위바위보에서 지셨습니다!");
                     } else if (result == 0) {
                        chr.dropMessage(1, "축하합니다! 가위바위보에서 이기셨습니다!");
                     }

                     c.getSession().writeAndFlush(PlayerShopPacket.FinishRPS(result, chr.getTrade().getPartner().getPRS()));
                  }
               }, 1000L);
            }
            break;
         case MOVE_OMOK:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOpen()) {
                  if (game2.getLoser() != game2.getVisitorSlot(chr)) {
                     return;
                  }

                  game2.setPiece(slea.readInt(), slea.readInt(), slea.readByte(), chr);
               }
            }
            break;
         case SELECT_CARD:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOpen()) {
                  if (game2.getLoser() != game2.getVisitorSlot(chr)) {
                     return;
                  }

                  if (slea.readByte() != game2.getTurn()) {
                     return;
                  }

                  int slot3 = slea.readByte();
                  turn = game2.getTurn();
                  itemId = game2.getFirstSlot();
                  if (turn == 1) {
                     game2.setFirstSlot(slot3);
                     if (game2.isOwner(chr)) {
                        game2.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot3, itemId, turn), false);
                     } else {
                        game2.getMCOwner().getClient().getSession().writeAndFlush(PlayerShopPacket.getMatchCardSelect(turn, slot3, itemId, turn));
                     }

                     game2.setTurn(0);
                     return;
                  }

                  if (itemId > 0 && game2.getCardId(itemId + 1) == game2.getCardId(slot3 + 1)) {
                     game2.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot3, itemId, game2.isOwner(chr) ? 2 : 3));
                     game2.setPoints(game2.getVisitorSlot(chr));
                  } else {
                     game2.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot3, itemId, game2.isOwner(chr) ? 0 : 1));
                     game2.nextLoser();
                  }

                  game2.setTurn(1);
                  game2.setFirstSlot(0);
               }
            }
            break;
         case EXIT_AFTER_GAME:
         case CANCEL_EXIT:
            ips3 = chr.getPlayerShop();
            if (ips3 != null && ips3 instanceof MapleMiniGame) {
               game2 = (MapleMiniGame)ips3;
               if (!game2.isOpen()) {
                  game2.setExitAfter(chr);
                  if (game2.isExitAfter(chr)) {
                     game2.broadcastToVisitors(PlayerShopPacket.getMiniGameInfoMsg((byte)5, chr.getName()));
                  } else {
                     game2.broadcastToVisitors(PlayerShopPacket.getMiniGameInfoMsg((byte)6, chr.getName()));
                  }
               }
            }
            break;
         case ONECARD:
            createType = slea.readByte();
            OneCardGame oc;
            OneCardGame.OneCardPlayer ocp;
            Iterator nextPlayer;
            OneCardGame.OneCard card;
            Iterator var51;
            switch(createType) {
            case 0:
               objectId = slea.readInt();
               oc = chr.getOneCardInstance();
               if (oc == null) {
                  return;
               }

               ocp = oc.getPlayer(chr);
               if (ocp == null) {
                  return;
               }

               OneCardGame.OneCard selCard = null;
               nextPlayer = ocp.getCards().iterator();

               while(true) {
                  if (nextPlayer.hasNext()) {
                     card = (OneCardGame.OneCard)nextPlayer.next();
                     if (card.getObjectId() != objectId) {
                        continue;
                     }

                     selCard = card;
                  }

                  if (selCard == null) {
                     System.out.println("selCard에 문제 발생.");
                     oc.sendPacketToPlayers(CWvsContext.serverNotice(1, "", "원카드에 문제가 발생하여 게임이 종료됩니다."));
                     oc.endGame(ocp, true);
                     return;
                  }

                  oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onPutCardResult(ocp, selCard));
                  ocp.getCards().remove(selCard);
                  if (ocp.getCards().size() == 0 || ocp.getCards().isEmpty()) {
                     oc.endGame(ocp, false);
                     return;
                  }

                  if (ocp.getCards().size() == 1) {
                     chr.getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/lastcard"));
                  }

                  oc.setLastCard(selCard);
                  if (oc.getLastCard().getType() == 11) {
                     oc.setbClockWiseTurn(!oc.isbClockWiseTurn());
                  }

                  nextPlayer = null;
                  OneCardGame.OneCardPlayer nextPlayer;
                  if (oc.getLastCard().getType() != 9 && oc.getLastCard().getType() != 8 && (oc.getLastCard().getType() != 12 || oc.getLastCard().getColor() != 3)) {
                     if (oc.getLastCard().getType() == 10) {
                        nextPlayer = oc.setNextPlayer(oc.setNextPlayer(oc.getLastPlayer(), oc.isbClockWiseTurn()), oc.isbClockWiseTurn());
                     } else {
                        nextPlayer = oc.setNextPlayer(oc.getLastPlayer(), oc.isbClockWiseTurn());
                     }
                  } else {
                     nextPlayer = oc.getLastPlayer();
                  }

                  if (oc.getLastCard().getType() == 6) {
                     oc.setFire(oc.getFire() + 2);
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(2, 2, chr.getId(), false));
                     oc.sendPacketToPlayers(SLFCGPacket.onShowText("마법 : " + chr.getName() + "님의 공격!"));
                  } else if (oc.getLastCard().getType() == 7) {
                     oc.setFire(oc.getFire() + 3);
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(2, 3, chr.getId(), false));
                     oc.sendPacketToPlayers(SLFCGPacket.onShowText("마법 : " + chr.getName() + "님의 공격!"));
                  } else if (oc.getLastCard().getType() == 12 & oc.getLastCard().getColor() == 0) {
                     oc.setFire(oc.getFire() + 5);
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/oz"));
                     oc.sendPacketToPlayers(CField.playSound("Sound/MiniGame.img/oneCard/flame_burst"));
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(2, 5, chr.getId(), false));
                     oc.sendPacketToPlayers(SLFCGPacket.onShowText("마법 : " + chr.getName() + "님의 공격!"));
                  }

                  if (oc.getLastCard().getType() == 8) {
                     oc.sendPacketToPlayers(SLFCGPacket.onShowText("마법 : 색 바꾸기!"));
                  }

                  if (oc.getLastCard().getType() == 9) {
                     oc.sendPacketToPlayers(SLFCGPacket.onShowText("마법 : 한 번 더!"));
                  }

                  if (oc.getLastCard().getType() == 10) {
                     oc.sendPacketToPlayers(SLFCGPacket.onShowText("마법 : 점프!"));
                  }

                  if (oc.getLastCard().getType() == 11) {
                     oc.sendPacketToPlayers(SLFCGPacket.onShowText("마법 : 거꾸로!"));
                  }

                  int i;
                  ArrayList possibleCards;
                  if (oc.getLastCard().getType() == 12) {
                     if (oc.getLastCard().getColor() == 1) {
                        oc.setFire(0);
                        oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/michael"));
                        oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(3, 0, chr.getId(), false));
                        oc.sendPacketToPlayers(CField.playSound("Sound/MiniGame.img/oneCard/shield_appear"));
                     } else {
                        ArrayList newcards;
                        OneCardGame.OneCardPlayer bp;
                        if (oc.getLastCard().getColor() != 2) {
                           if (oc.getLastCard().getColor() == 3) {
                              oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/irina"));
                              var51 = oc.getPlayers().iterator();

                              while(var51.hasNext()) {
                                 bp = (OneCardGame.OneCardPlayer)var51.next();
                                 newcards = new ArrayList();
                                 Iterator var72 = bp.getCards().iterator();

                                 OneCardGame.OneCard card;
                                 while(var72.hasNext()) {
                                    card = (OneCardGame.OneCard)var72.next();
                                    if (card.getColor() == 3) {
                                       oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onPutCardResult(bp, card));
                                       newcards.add(card);
                                    }
                                 }

                                 var72 = newcards.iterator();

                                 while(var72.hasNext()) {
                                    card = (OneCardGame.OneCard)var72.next();
                                    bp.getCards().remove(card);
                                 }

                                 if (bp.getCards().size() == 0 || bp.getCards().isEmpty()) {
                                    oc.endGame(bp, false);
                                    return;
                                 }

                                 if (ocp.getCards().size() == 1) {
                                    oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/lastcard"));
                                 }
                              }

                              possibleCards = new ArrayList();

                              for(i = 0; i <= 3; ++i) {
                                 possibleCards.add(i);
                              }

                              c.getSession().writeAndFlush(SLFCGPacket.OneCardGamePacket.onChangeColorRequest(possibleCards));
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              oc.setLastPlayer(nextPlayer);
                              if (oc.getOneCardTimer() != null) {
                                 oc.getOneCardTimer().cancel(false);
                              }

                              oc.setOneCardTimer(Timer.ShowTimer.getInstance().schedule(() -> {
                                 oc.skipPlayer();
                              }, 15000L));
                              return;
                           }

                           if (oc.getLastCard().getColor() == 4) {
                              oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/icart"));
                           }
                        } else {
                           oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/hawkeye"));
                           var51 = oc.getPlayers().iterator();

                           label1086:
                           while(true) {
                              do {
                                 if (!var51.hasNext()) {
                                    break label1086;
                                 }

                                 bp = (OneCardGame.OneCardPlayer)var51.next();
                                 newcards = new ArrayList();
                              } while(bp.getPlayer().getId() == chr.getId());

                              for(int i = 0; i < 2; ++i) {
                                 if (oc.getOneCardDeckInfo().size() == 0) {
                                    oc.resetDeck();
                                    if (oc.getOneCardDeckInfo().size() == 0) {
                                       break;
                                    }
                                 }

                                 int num = Randomizer.nextInt(oc.getOneCardDeckInfo().size());
                                 OneCardGame.OneCard card = (OneCardGame.OneCard)oc.getOneCardDeckInfo().get(num);
                                 bp.getCards().add(card);
                                 newcards.add(card);
                                 oc.getOneCardDeckInfo().remove(num);
                              }

                              oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onGetCardResult(bp, newcards));
                           }
                        }
                     }
                  }

                  oc.setLastPlayer(nextPlayer);
                  if (nextPlayer != null) {
                     if (oc.getLastCard().getType() == 8) {
                        possibleCards = new ArrayList();

                        for(i = 0; i <= 3; ++i) {
                           if (i != oc.getLastCard().getColor()) {
                              possibleCards.add(i);
                           }
                        }

                        c.getSession().writeAndFlush(SLFCGPacket.OneCardGamePacket.onChangeColorRequest(possibleCards));
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     } else {
                        possibleCards = new ArrayList();
                        OneCardGame.OneCard card;
                        Iterator var66;
                        if (oc.getLastCard().getColor() == 4) {
                           var66 = nextPlayer.getCards().iterator();

                           label1057:
                           while(true) {
                              while(true) {
                                 if (!var66.hasNext()) {
                                    break label1057;
                                 }

                                 card = (OneCardGame.OneCard)var66.next();
                                 if (oc.getFire() == 0) {
                                    possibleCards.add(card);
                                 } else if (card.getType() == 6 || card.getType() == 7 || card.getType() == 12 && card.getColor() == 0) {
                                    possibleCards.add(card);
                                 }
                              }
                           }
                        } else {
                           var66 = nextPlayer.getCards().iterator();

                           label1041:
                           while(true) {
                              do {
                                 while(true) {
                                    if (!var66.hasNext()) {
                                       break label1041;
                                    }

                                    card = (OneCardGame.OneCard)var66.next();
                                    if (card.getType() <= 5) {
                                       break;
                                    }

                                    if (card.getType() <= 7) {
                                       if (oc.getFire() == 0) {
                                          if (card.getColor() == oc.getLastCard().getColor() || card.getType() == oc.getLastCard().getType()) {
                                             possibleCards.add(card);
                                          }
                                       } else if (oc.getLastCard().getType() == 6) {
                                          if (card.getType() == 6) {
                                             possibleCards.add(card);
                                          } else if (card.getType() == 7 && card.getColor() == oc.getLastCard().getColor()) {
                                             possibleCards.add(card);
                                          }
                                       } else if (oc.getLastCard().getType() == 7 && card.getType() == 7) {
                                          possibleCards.add(card);
                                       }
                                    } else if (card.getType() <= 11) {
                                       if ((card.getColor() == oc.getLastCard().getColor() || card.getType() == oc.getLastCard().getType()) && oc.getFire() == 0) {
                                          possibleCards.add(card);
                                       }
                                    } else {
                                       switch(card.getColor()) {
                                       case 0:
                                          if (oc.getFire() > 0) {
                                             possibleCards.add(card);
                                          }
                                          break;
                                       case 1:
                                          if (oc.getFire() > 0 || oc.getLastCard().getColor() == 1) {
                                             possibleCards.add(card);
                                          }
                                          break;
                                       case 2:
                                          if (oc.getLastCard().getColor() == card.getColor() && oc.getFire() == 0) {
                                             possibleCards.add(card);
                                          }
                                          break;
                                       case 3:
                                          if (oc.getLastCard().getColor() == card.getColor() && oc.getFire() == 0) {
                                             possibleCards.add(card);
                                          }
                                          break;
                                       case 4:
                                          possibleCards.add(card);
                                       }
                                    }
                                 }
                              } while(card.getColor() != oc.getLastCard().getColor() && card.getType() != oc.getLastCard().getType());

                              if (oc.getFire() == 0) {
                                 possibleCards.add(card);
                              }
                           }
                        }

                        if (oc.getOneCardDeckInfo().size() != 0 && !oc.getOneCardDeckInfo().isEmpty()) {
                           oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(nextPlayer, possibleCards, possibleCards.size() == 0 && nextPlayer.getCards().size() == 16 || nextPlayer.getCards().size() < 16, oc.isbClockWiseTurn()));
                        } else {
                           oc.resetDeck();
                           oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(0, 0, 0, false));
                           oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(nextPlayer, possibleCards, possibleCards.size() == 0 && nextPlayer.getCards().size() == 16 || nextPlayer.getCards().size() < 16, oc.isbClockWiseTurn()));
                        }

                        nextPlayer.getPlayer().getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/myturn"));
                        nextPlayer.getPlayer().getClient().getSession().writeAndFlush(CWvsContext.enableActions(nextPlayer.getPlayer()));
                        nextPlayer.getPlayer().getClient().getSession().writeAndFlush(SLFCGPacket.onShowText("당신의 턴입니다."));
                     }

                     if (oc.getOneCardTimer() != null) {
                        oc.getOneCardTimer().cancel(false);
                     }

                     oc.setOneCardTimer(Timer.ShowTimer.getInstance().schedule(() -> {
                        oc.skipPlayer();
                     }, 15000L));
                  }

                  return;
               }
            case 1:
               OneCardGame oc = chr.getOneCardInstance();
               OneCardGame.OneCardPlayer ocp = oc.getPlayer(chr);
               List<OneCardGame.OneCard> newcards = new ArrayList();
               if (oc.getFire() > 0) {
                  String var10001 = chr.getName();
                  oc.sendPacketToPlayers(SLFCGPacket.onShowText(var10001 + "님이 " + oc.getFire() + "의 피해를 입었습니다."));
               }

               for(itemId = 0; itemId < (oc.getFire() > 0 ? oc.getFire() : 1); ++itemId) {
                  if (oc.getOneCardDeckInfo().size() == 0) {
                     oc.resetDeck();
                     if (oc.getOneCardDeckInfo().size() == 0) {
                        break;
                     }
                  }

                  int num = Randomizer.nextInt(oc.getOneCardDeckInfo().size());
                  card = (OneCardGame.OneCard)oc.getOneCardDeckInfo().get(num);
                  ocp.getCards().add(card);
                  newcards.add(card);
                  oc.getOneCardDeckInfo().remove(num);
               }

               oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onGetCardResult(ocp, newcards));
               if (ocp.getCards().size() >= 17) {
                  oc.setFire(0);
                  oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/gameover"));
                  chr.getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/gameover"));
                  oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(5, 0, chr.getId(), true));
                  oc.playerDead(ocp, false);
               } else if (oc.getFire() > 0) {
                  oc.setFire(0);
                  oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(4, 0, chr.getId(), false));
               }

               OneCardGame.OneCardPlayer nextPlayer = oc.setNextPlayer(oc.getLastPlayer(), oc.isbClockWiseTurn());
               oc.setLastPlayer(nextPlayer);
               if (nextPlayer != null) {
                  List<OneCardGame.OneCard> possibleCards = new ArrayList();
                  OneCardGame.OneCard card;
                  if (oc.getLastCard().getColor() == 4) {
                     var51 = nextPlayer.getCards().iterator();

                     while(var51.hasNext()) {
                        card = (OneCardGame.OneCard)var51.next();
                        possibleCards.add(card);
                     }
                  } else {
                     var51 = nextPlayer.getCards().iterator();

                     label1135:
                     while(true) {
                        do {
                           while(true) {
                              if (!var51.hasNext()) {
                                 break label1135;
                              }

                              card = (OneCardGame.OneCard)var51.next();
                              if (card.getType() <= 11) {
                                 break;
                              }

                              switch(card.getColor()) {
                              case 0:
                                 if (oc.getLastCard().getColor() == card.getColor()) {
                                    possibleCards.add(card);
                                 }
                                 break;
                              case 1:
                                 possibleCards.add(card);
                                 break;
                              case 2:
                                 if (oc.getLastCard().getColor() == card.getColor()) {
                                    possibleCards.add(card);
                                 }
                                 break;
                              case 3:
                                 if (oc.getLastCard().getColor() == card.getColor()) {
                                    possibleCards.add(card);
                                 }
                                 break;
                              case 4:
                                 possibleCards.add(card);
                              }
                           }
                        } while(card.getColor() != oc.getLastCard().getColor() && card.getType() != oc.getLastCard().getType());

                        possibleCards.add(card);
                     }
                  }

                  if (oc.getOneCardDeckInfo().size() != 0 && !oc.getOneCardDeckInfo().isEmpty()) {
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(nextPlayer, possibleCards, nextPlayer.getCards().size() < 17, oc.isbClockWiseTurn()));
                  } else {
                     oc.resetDeck();
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(0, 0, 0, false));
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(nextPlayer, possibleCards, nextPlayer.getCards().size() < 17, oc.isbClockWiseTurn()));
                  }

                  nextPlayer.getPlayer().getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/myturn"));
                  nextPlayer.getPlayer().getClient().getSession().writeAndFlush(CWvsContext.enableActions(nextPlayer.getPlayer()));
                  if (oc.getOneCardTimer() != null) {
                     oc.getOneCardTimer().cancel(false);
                  }

                  oc.setOneCardTimer(Timer.ShowTimer.getInstance().schedule(() -> {
                     oc.skipPlayer();
                  }, 15000L));
               }

               return;
            case 2:
               type3 = slea.readByte();
               oc = chr.getOneCardInstance();
               oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onChangeColorResult(oc.getLastCard().getType() == 12, type3));
               ocp = oc.setNextPlayer(oc.getLastPlayer(), oc.isbClockWiseTurn());
               oc.getLastCard().setColor(type3);
               oc.setLastPlayer(ocp);
               if (ocp != null) {
                  List<OneCardGame.OneCard> possibleCards = new ArrayList();
                  if (oc.getLastCard().getColor() == 4) {
                     nextPlayer = ocp.getCards().iterator();

                     while(nextPlayer.hasNext()) {
                        card = (OneCardGame.OneCard)nextPlayer.next();
                        possibleCards.add(card);
                     }
                  } else {
                     nextPlayer = ocp.getCards().iterator();

                     label1175:
                     while(true) {
                        do {
                           while(true) {
                              if (!nextPlayer.hasNext()) {
                                 break label1175;
                              }

                              card = (OneCardGame.OneCard)nextPlayer.next();
                              if (card.getType() <= 11) {
                                 break;
                              }

                              switch(card.getColor()) {
                              case 0:
                                 if (oc.getLastCard().getColor() == card.getColor()) {
                                    possibleCards.add(card);
                                 }
                                 break;
                              case 1:
                                 possibleCards.add(card);
                                 break;
                              case 2:
                                 if (oc.getLastCard().getColor() == card.getColor()) {
                                    possibleCards.add(card);
                                 }
                                 break;
                              case 3:
                                 if (oc.getLastCard().getColor() == card.getColor()) {
                                    possibleCards.add(card);
                                 }
                                 break;
                              case 4:
                                 possibleCards.add(card);
                              }
                           }
                        } while(card.getColor() != oc.getLastCard().getColor() && card.getType() != oc.getLastCard().getType());

                        possibleCards.add(card);
                     }
                  }

                  if (oc.getOneCardDeckInfo().size() != 0 && !oc.getOneCardDeckInfo().isEmpty()) {
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(ocp, possibleCards, ocp.getCards().size() < 17, oc.isbClockWiseTurn()));
                  } else {
                     oc.resetDeck();
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(0, 0, 0, false));
                     oc.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(ocp, possibleCards, ocp.getCards().size() < 17, oc.isbClockWiseTurn()));
                  }

                  ocp.getPlayer().getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/myturn"));
                  ocp.getPlayer().getClient().getSession().writeAndFlush(CWvsContext.enableActions(ocp.getPlayer()));
                  if (oc.getOneCardTimer() != null) {
                     oc.getOneCardTimer().cancel(false);
                  }

                  oc.setOneCardTimer(Timer.ShowTimer.getInstance().schedule(() -> {
                     oc.skipPlayer();
                  }, 15000L));
               }

               return;
            default:
               return;
            }
         case ONECARD_EMOTION:
            slea.skip(4);
            obid = slea.readInt();
            chr.getOneCardInstance().sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEmotion(chr.getId(), obid));
            chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
            break;
         default:
            System.out.println("Unhandled interaction action by " + chr.getName() + " : " + action + ", " + slea.toString());
         }

      }
   }

   public static void minigameOperation(LittleEndianAccessor slea, MapleClient c) {
      int type = slea.readInt();
      switch(type) {
      case 185:
         Point pos = new Point(slea.readInt(), slea.readInt());
         if (c.getPlayer().getBattleReverseInstance() != null) {
            c.getPlayer().getBattleReverseInstance().sendPlaceStone(c.getPlayer(), pos);
         } else {
            c.getPlayer().warp(ServerConstants.warpMap);
            c.getPlayer().dropMessage(5, "오류가 발생하여 게임이 취소됩니다.");
         }
      default:
      }
   }

   public static final byte getResult(byte rps1, byte rps2) {
      switch(rps1) {
      case 0:
         if (rps2 == 1) {
            return 2;
         }

         if (rps2 == 2) {
            return 0;
         }
         break;
      case 1:
         if (rps2 == 2) {
            return 2;
         }

         if (rps2 == 0) {
            return 0;
         }
         break;
      case 2:
         if (rps2 == 0) {
            return 2;
         }

         if (rps2 == 1) {
            return 0;
         }
      }

      return 1;
   }

   public static enum Interaction {
      SET_ITEMS1(0),
      SET_ITEMS2(1),
      SET_ITEMS3(2),
      SET_ITEMS4(3),
      SET_MESO1(4),
      SET_MESO2(5),
      SET_MESO3(6),
      SET_MESO4(7),
      CONFIRM_TRADE1(8),
      CONFIRM_TRADE2(9),
      CONFIRM_TRADE_MESO1(10),
      CONFIRM_TRADE_MESO2(11),
      CREATE(16),
      VISIT(19),
      INVITE_TRADE(21),
      DENY_TRADE(22),
      CHAT(24),
      OPEN(26),
      EXIT(28),
      HIRED_MERCHANT_MAINTENANCE(29),
      RESET_HIRED(30),
      ADD_ITEM1(31),
      ADD_ITEM2(32),
      ADD_ITEM3(33),
      ADD_ITEM4(34),
      BUY_ITEM_HIREDMERCHANT(35),
      PLAYER_SHOP_ADD_ITEM(36),
      BUY_ITEM_PLAYER_SHOP(37),
      BUY_ITEM_STORE(38),
      REMOVE_ITEM(47),
      MAINTANCE_OFF(48),
      MAINTANCE_ORGANISE(49),
      CLOSE_MERCHANT(50),
      TAKE_MESOS(52),
      VIEW_MERCHANT_VISITOR(55),
      VIEW_MERCHANT_BLACKLIST(56),
      MERCHANT_BLACKLIST_ADD(57),
      MERCHANT_BLACKLIST_REMOVE(58),
      ADMIN_STORE_NAMECHANGE(59),
      REQUEST_TIE(85),
      ANSWER_TIE(86),
      GIVE_UP(87),
      REQUEST_REDO(89),
      ANSWER_REDO(90),
      EXIT_AFTER_GAME(91),
      CANCEL_EXIT(92),
      READY(93),
      UN_READY(94),
      EXPEL(95),
      START(96),
      GAME_RESULT(97),
      SKIP(98),
      MOVE_OMOK(99),
      SELECT_CARD(103),
      WEDDING_START(105),
      WEDDING_END(108),
      INVITE_ROCK_PAPER_SCISSORS(112),
      ONECARD(155),
      ONECARD_EMOTION(156);

      public int action;

      private Interaction(int action) {
         this.action = action;
      }

      public static PlayerInteractionHandler.Interaction getByAction(int i) {
         PlayerInteractionHandler.Interaction[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            PlayerInteractionHandler.Interaction s = var1[var3];
            if (s.action == i) {
               return s;
            }
         }

         return null;
      }
   }
}
