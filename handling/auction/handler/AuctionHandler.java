package handling.auction.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.AuctionHistory;
import client.inventory.AuctionItem;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.auction.AuctionServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.CharacterTransfer;
import handling.world.MapleMessengerCharacter;
import handling.world.PlayerBuffStorage;
import handling.world.World;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.games.BattleReverse;
import server.games.OneCardGame;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.PacketHelper;

public class AuctionHandler {
   public static void LeaveAuction(MapleClient c, MapleCharacter chr) {
      AuctionServer.getPlayerStorage().deregisterPlayer(chr);
      c.updateLoginState(1, c.getSessionIPAddress());
      boolean var6 = false;

      try {
         var6 = true;
         PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
         PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
         World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), c.getChannel());
         c.getSession().writeAndFlush(CField.getChannelChange(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1])));
         var6 = false;
      } finally {
         if (var6) {
            String s = c.getSessionIPAddress();
            LoginServer.addIPAuth(s.substring(s.indexOf(47) + 1, s.length()));
            chr.saveToDB(true, false);
            c.setPlayer((MapleCharacter)null);
            c.setAuction(false);
         }
      }

      String s = c.getSessionIPAddress();
      LoginServer.addIPAuth(s.substring(s.indexOf(47) + 1, s.length()));
      chr.saveToDB(true, false);
      c.setPlayer((MapleCharacter)null);
      c.setAuction(false);
   }

   public static void EnterAuction(MapleCharacter chr, MapleClient client) {
      chr.changeRemoval();
      ChannelServer ch = ChannelServer.getInstance(client.getChannel());
      if (chr.getMessenger() != null) {
         World.Messenger.silentLeaveMessenger(chr.getMessenger().getId(), new MapleMessengerCharacter(chr));
      }

      PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
      PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
      World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), -20);
      ch.removePlayer(chr);
      client.setAuction(true);
      client.updateLoginState(3, client.getSessionIPAddress());
      String s = client.getSessionIPAddress();
      LoginServer.addIPAuth(s.substring(s.indexOf(47) + 1, s.length()));
      client.getSession().writeAndFlush(CField.enterAuction(chr));
      chr.saveToDB(true, false);
      chr.getMap().removePlayer(chr);
      if (OneCardGame.oneCardMatchingQueue.contains(chr)) {
         OneCardGame.oneCardMatchingQueue.remove(chr);
      }

      if (BattleReverse.BattleReverseMatchingQueue.contains(chr)) {
         BattleReverse.BattleReverseMatchingQueue.remove(chr);
      }

   }

   public static final void Handle(LittleEndianAccessor slea, MapleClient c) {
      int op = slea.readInt();
      Map<Integer, AuctionItem> items = AuctionServer.getItems();
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      int dwAuctionID;
      long dwInventoryId;
      int dwAuctionId;
      ArrayList completeItems;
      String var10000;
      int var10001;
      long nPrice;
      AuctionItem item;
      int k;
      int nCount;
      int dwAccountId;
      int dwCharacterId;
      int nItemId;
      int nState;
      long l3;
      long nBuyTime;
      int deposit;
      int i3;
      int nWorldId;
      Iterator var66;
      AuctionItem auctionItem;
      int itemType;
      String wish;
      Item item1;
      Item it;
      AuctionItem auctionItem;
      AuctionHistory history;
      Iterator iterator1;
      int ch;
      switch(op) {
      case 0:
         CharacterTransfer transfer = AuctionServer.getPlayerStorage().getPendingCharacter(c.getPlayer().getId());
         MapleCharacter chr = MapleCharacter.ReconstructChr(transfer, c, false);
         c.setPlayer(chr);
         c.setAccID(chr.getAccountID());
         if (!c.CheckIPAddress()) {
            c.getSession().close();
            return;
         }

         chr.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(chr.getId()));
         chr.silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(chr.getId()));
         World.isCharacterListConnected(c.getPlayer().getName(), c.loadCharacterNames(c.getWorld()));
         c.updateLoginState(2, c.getSessionIPAddress());
         AuctionServer.getPlayerStorage().registerPlayer(chr);
         List<AuctionItem> list1 = new ArrayList();
         List<AuctionItem> list2 = new ArrayList();
         List<AuctionItem> marketPriceItems = new ArrayList();
         List<AuctionItem> recentlySellItems = new ArrayList();
         List<Integer> list3 = new ArrayList();
         List<AuctionItem> list4 = new ArrayList();

         for(int i2 = 0; i2 < 10; ++i2) {
            wish = c.getKeyValue("wish" + i2);
            if (wish != null) {
               list3.add(Integer.parseInt(wish));
            }
         }

         var66 = items.entrySet().iterator();

         while(var66.hasNext()) {
            Entry<Integer, AuctionItem> itemz = (Entry)var66.next();
            auctionItem = (AuctionItem)itemz.getValue();
            if ((auctionItem.getEndDate() < System.currentTimeMillis() || auctionItem.getState() >= 2) && (auctionItem.getState() == 2 && auctionItem.getBidUserId() == c.getPlayer().getId() || (auctionItem.getState() == 3 || auctionItem.getState() == 4) && auctionItem.getAccountId() == c.getAccID())) {
               list1.add(auctionItem);
            }

            if (auctionItem.getAccountId() == c.getAccID() && auctionItem.getState() == 0) {
               list2.add(auctionItem);
            }

            if (auctionItem.getState() == 0 && recentlySellItems.size() < 1000) {
               recentlySellItems.add(auctionItem);
            }

            if ((auctionItem.getState() == 3 || auctionItem.getState() == 8) && marketPriceItems.size() < 1000) {
               marketPriceItems.add(auctionItem);
            }

            iterator1 = list3.iterator();

            while(iterator1.hasNext()) {
               ch = (Integer)iterator1.next();
               if (auctionItem.getAuctionId() == ch) {
                  list4.add(auctionItem);
               }
            }
         }

         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteItems(list1));
         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionSellingMyItems(list2));
         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionWishlist(list4));
         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionOn());
         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionMarketPrice(marketPriceItems));
         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionSearchItems(recentlySellItems));
         var10000 = FileoutputUtil.경매장입장로그;
         var10001 = c.getAccID();
         FileoutputUtil.log(var10000, "[입장] 계정 번호 : " + var10001 + " | 캐릭 번호 : " + c.getPlayer().getId() + " | 캐릭터 닉네임 : " + c.getPlayer().getName());
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 9:
      case 13:
      case 14:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
      case 22:
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 32:
      case 33:
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
      case 42:
      case 43:
      case 44:
      case 48:
      case 49:
      default:
         break;
      case 10:
         int nAuctionType = slea.readInt();
         int nItemID = slea.readInt();
         int nNumber = slea.readInt();
         long l2 = slea.readLong();
         int nEndHour = slea.readInt();
         byte nTI = slea.readByte();
         int nItemPos = slea.readInt();
         Item source = c.getPlayer().getInventory(nTI).getItem((short)nItemPos);
         if (source == null || source.getItemId() != nItemID || source.getQuantity() < nNumber || nNumber < 0 || l2 < 0L) {
            System.out.println(c.getPlayer().getName() + " 캐릭터가 경매장에 비정상적인 패킷을 유도함.");
            c.getSession().close();
            return;
         }

         if (nNumber <= 0) {
            System.out.println("quantity 0이하");
            c.getSession().close();
            return;
         }

         if (source.getInventoryId() <= 0L) {
            System.out.println("inventoryId : " + source.getInventoryId());
            return;
         }

         Item target = source.copy();
         MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(nTI), (short)nItemPos, (short)nNumber, false);
         target.setQuantity((short)nNumber);
         if (target.getInventoryId() <= 0L) {
            System.out.println("inventoryId : " + target.getInventoryId());
            return;
         }

         AuctionItem auctionItem3 = new AuctionItem();
         auctionItem3.setAuctionType(nAuctionType);
         auctionItem3.setItem(target);
         if (GameConstants.getInventoryType(auctionItem3.getItem().getItemId()) == MapleInventoryType.CASH && nNumber > 1) {
            auctionItem3.setPrice(l2);
            auctionItem3.setDirectPrice(l2);
         } else {
            auctionItem3.setPrice(l2);
            auctionItem3.setDirectPrice(l2 * (long)nNumber);
         }

         auctionItem3.setEndDate(System.currentTimeMillis() + (long)(nEndHour * 60 * 60 * 1000));
         auctionItem3.setRegisterDate(System.currentTimeMillis());
         auctionItem3.setAccountId(c.getAccID());
         auctionItem3.setCharacterId(c.getPlayer().getId());
         auctionItem3.setState(0);
         auctionItem3.setWorldId(c.getWorld());
         auctionItem3.setName(c.getPlayer().getName());
         auctionItem3.setAuctionId(AuctionItemIdentifier.getInstance());
         items.put(auctionItem3.getAuctionId(), auctionItem3);
         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionSellItemUpdate(auctionItem3));
         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionSellItem(auctionItem3));
         var10000 = FileoutputUtil.경매장판매등록로그;
         var10001 = c.getAccID();
         FileoutputUtil.log(var10000, "[아이템등록] 계정 번호 : " + var10001 + " | 캐릭 번호 : " + c.getPlayer().getId() + " | 캐릭터 닉네임 : " + c.getPlayer().getName() + " | 아이템 이름 : " + ii.getName(auctionItem3.getItem().getItemId()) + " | 아이템 코드 : " + auctionItem3.getItem().getItemId() + " | 수량 " + auctionItem3.getItem().getQuantity() + " | 판매 메소 : " + l2);
         break;
      case 11:
         long l1 = slea.readLong();
         k = slea.readInt();
         int m = slea.readInt();
         int i1 = slea.readInt();
         nItemId = slea.readInt();
         nState = slea.readInt();
         l3 = slea.readLong();
         nBuyTime = slea.readLong();
         deposit = slea.readInt();
         deposit = slea.readInt();
         i3 = slea.readInt();
         nWorldId = slea.readInt();
         if (i3 < 0 || i3 > 32767 || l3 < 0L) {
            System.out.println(c.getPlayer().getName() + " 캐릭터가 경매장에 비정상적인 패킷을 유도함.");
            c.getSession().close();
            return;
         }

         AuctionItem auctionItem5 = (AuctionItem)items.get(k);
         if (auctionItem5 != null && auctionItem5.getItem() != null && auctionItem5.getHistory() != null) {
            history = auctionItem5.getHistory();
            if (history.getId() != l1) {
               System.out.println("return 1");
               return;
            }

            if (history.getAuctionId() != k) {
               System.out.println("return 2");
               return;
            }

            if (history.getAccountId() != m) {
               System.out.println("return 3");
               return;
            }

            if (history.getCharacterId() != i1) {
               System.out.println("return 4");
               return;
            }

            if (history.getItemId() != nItemId) {
               System.out.println("return 5");
               return;
            }

            if (history.getState() != nState) {
               System.out.println("return 6");
               return;
            }

            if (history.getPrice() != l3) {
               System.out.println("return 7");
               return;
            }

            if (PacketHelper.getTime(history.getBuyTime()) != nBuyTime) {
               System.out.println("return 8");
               return;
            }

            if (history.getDeposit() != deposit) {
               System.out.println("return 9");
               return;
            }

            if (history.getQuantity() != i3) {
               System.out.println("return 10");
               return;
            }

            if (history.getWorldId() != nWorldId) {
               System.out.println("return 11");
               return;
            }

            auctionItem5.setEndDate(System.currentTimeMillis() + 86400000L);
            auctionItem5.setRegisterDate(System.currentTimeMillis());
            auctionItem5.setState(9);
            history.setState(9);
            item1 = auctionItem5.getItem();
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteItemUpdate(auctionItem5));
            auctionItem = new AuctionItem();
            auctionItem.setAuctionType(auctionItem5.getAuctionType());
            auctionItem.setItem(item1);
            auctionItem.setPrice(auctionItem5.getDirectPrice());
            auctionItem.setSecondPrice(0L);
            auctionItem.setDirectPrice(auctionItem5.getDirectPrice());
            auctionItem.setEndDate(System.currentTimeMillis() + 86400000L);
            auctionItem.setRegisterDate(System.currentTimeMillis());
            auctionItem.setAccountId(c.getAccID());
            auctionItem.setCharacterId(c.getPlayer().getId());
            auctionItem.setState(0);
            auctionItem.setWorldId(c.getWorld());
            auctionItem.setName(c.getPlayer().getName());
            auctionItem.setAuctionId(AuctionItemIdentifier.getInstance());
            items.put(auctionItem.getAuctionId(), auctionItem);
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionSellItemUpdate(auctionItem));
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionReSellItem(auctionItem));
            var10000 = FileoutputUtil.경매장판매등록로그;
            var10001 = c.getAccID();
            FileoutputUtil.log(var10000, "[아이템재등록] 계정 번호 : " + var10001 + " | 캐릭 번호 : " + c.getPlayer().getId() + " | 캐릭터 닉네임 : " + c.getPlayer().getName() + " | 아이템 이름 : " + ii.getName(auctionItem5.getItem().getItemId()) + " | 아이템 코드 : " + auctionItem5.getItem().getItemId() + " | 수량 " + auctionItem5.getItem().getQuantity() + " | 판매 메소 : " + auctionItem5.getDirectPrice());
         }
         break;
      case 12:
         dwAuctionID = slea.readInt();
         AuctionItem aItem = (AuctionItem)items.get(dwAuctionID);
         if (aItem != null && aItem.getItem() != null) {
            if (aItem.getState() != 0) {
               return;
            }

            aItem.setState(4);
            aItem.setPrice(0L);
            aItem.setSecondPrice(-1L);
            history = new AuctionHistory();
            history.setAuctionId(aItem.getAuctionId());
            history.setAccountId(aItem.getAccountId());
            history.setCharacterId(aItem.getCharacterId());
            history.setItemId(aItem.getItem().getItemId());
            history.setState(aItem.getState());
            history.setPrice(aItem.getPrice());
            history.setBuyTime(System.currentTimeMillis());
            history.setDeposit(aItem.getDeposit());
            history.setQuantity(aItem.getItem().getQuantity());
            history.setWorldId(aItem.getWorldId());
            history.setId((long)AuctionHistoryIdentifier.getInstance());
            aItem.setHistory(history);
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionSellItemUpdate(aItem));
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteItemUpdate(aItem));
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionStopSell(aItem));
            var10000 = FileoutputUtil.경매장판매중지로그;
            var10001 = c.getAccID();
            FileoutputUtil.log(var10000, "[판매중지] 계정 번호 : " + var10001 + " | 캐릭 번호 : " + c.getPlayer().getId() + " | 캐릭터 닉네임 : " + c.getPlayer().getName() + " | 아이템 이름 : " + ii.getName(aItem.getItem().getItemId()) + " | 아이템 코드 : " + aItem.getItem().getItemId() + " | 갯수 : " + aItem.getItem().getQuantity());
         }
         break;
      case 20:
      case 21:
         dwAuctionID = slea.readInt();
         nPrice = slea.readLong();
         nCount = 1;
         if (op == 21) {
            nCount = slea.readInt();
         }

         for(int n = 0; n < 10; ++n) {
            wish = c.getKeyValue("wish" + n);
            if (wish != null && wish.equals(String.valueOf(dwAuctionID))) {
               c.removeKeyValue("wish" + n);
               break;
            }
         }

         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionWishlistUpdate(dwAuctionID));
         if (nPrice < 0L || nCount < 0 || nCount > 32767) {
            System.out.println(c.getPlayer().getName() + " 캐릭터가 경매장에 비정상적인 패킷을 유도함.");
            c.getSession().close();
            return;
         }

         if (c.getPlayer().getMeso() < nPrice) {
            if (op == 20) {
               c.getSession().writeAndFlush(CField.AuctionPacket.AuctionBuyEquipResult(106, dwAuctionID));
            } else {
               c.getSession().writeAndFlush(CField.AuctionPacket.AuctionBuyItemResult(106, dwAuctionID));
            }
         } else {
            AuctionItem auctionItem2 = (AuctionItem)items.get(dwAuctionID);
            if (op == 20 && auctionItem2.getItem() != null) {
               nCount = auctionItem2.getItem().getQuantity();
            }

            if (auctionItem2 != null && auctionItem2.getItem() != null && auctionItem2.getItem().getQuantity() >= nCount) {
               if (auctionItem2.getAccountId() == c.getAccID()) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "[알림] 자신이 올린 아이템은 구매 할 수 없습니다."));
                  return;
               }

               if (auctionItem2.getCharacterId() == c.getPlayer().getId() || auctionItem2.getState() != 0) {
                  return;
               }

               if (auctionItem2.getPrice() * (long)nCount != nPrice) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "[알림] 등록 가격과 구매 가격이 일치하지 않습니다."));
                  return;
               }

               c.getPlayer().gainMeso(-nPrice, false);
               it = auctionItem2.getItem();
               item1 = it.copy();
               it.setQuantity((short)(it.getQuantity() - nCount));
               item1.setQuantity((short)nCount);
               AuctionHistory history;
               if (it.getQuantity() <= 0) {
                  it.setQuantity((short)nCount);
                  auctionItem2.setState(3);
                  auctionItem2.setBidUserId(c.getPlayer().getId());
                  auctionItem2.setBidUserName(c.getPlayer().getName());
                  auctionItem2.setPrice(nPrice);
                  AuctionHistory auctionHistory = new AuctionHistory();
                  auctionHistory.setAuctionId(auctionItem2.getAuctionId());
                  auctionHistory.setAccountId(auctionItem2.getAccountId());
                  auctionHistory.setCharacterId(auctionItem2.getCharacterId());
                  auctionHistory.setItemId(auctionItem2.getItem().getItemId());
                  auctionHistory.setState(auctionItem2.getState());
                  auctionHistory.setPrice(auctionItem2.getPrice());
                  auctionHistory.setBuyTime(System.currentTimeMillis());
                  auctionHistory.setDeposit(auctionItem2.getDeposit());
                  auctionHistory.setQuantity(auctionItem2.getItem().getQuantity());
                  auctionHistory.setWorldId(auctionItem2.getWorldId());
                  auctionHistory.setId((long)AuctionHistoryIdentifier.getInstance());
                  auctionHistory.setBidUserId(c.getPlayer().getId());
                  auctionHistory.setBidUserName(c.getPlayer().getName());
                  auctionItem2.setHistory(auctionHistory);
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionBuyItemUpdate(auctionItem2, false));
                  var10000 = FileoutputUtil.경매장구매로그;
                  var10001 = c.getAccID();
                  FileoutputUtil.log(var10000, "[구입] 계정 번호 : " + var10001 + " | 캐릭 번호 : " + c.getPlayer().getId() + " | 캐릭터 닉네임 : " + c.getPlayer().getName() + " | 아이템 이름 : " + ii.getName(auctionItem2.getItem().getItemId()) + " | 아이템 코드 : " + auctionItem2.getItem().getItemId() + " | 수량 " + auctionItem2.getItem().getQuantity() + " | 구매 메소 : " + nPrice + " | 해당 아이템 판매자 이름 : " + auctionItem2.getName());
               } else {
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionBuyItemUpdate(auctionItem2, true));
                  auctionItem = new AuctionItem();
                  auctionItem.setAuctionType(auctionItem2.getAuctionType());
                  auctionItem.setItem(item1);
                  auctionItem.setPrice(nPrice);
                  auctionItem.setDirectPrice(nPrice);
                  auctionItem.setEndDate(auctionItem2.getEndDate());
                  auctionItem.setRegisterDate(auctionItem2.getRegisterDate());
                  auctionItem.setAccountId(auctionItem2.getAccountId());
                  auctionItem.setCharacterId(auctionItem2.getCharacterId());
                  auctionItem.setState(3);
                  auctionItem.setWorldId(auctionItem2.getWorldId());
                  auctionItem.setName(auctionItem2.getName());
                  auctionItem.setBidUserId(c.getPlayer().getId());
                  auctionItem.setBidUserName(c.getPlayer().getName());
                  auctionItem.setAuctionId(AuctionItemIdentifier.getInstance());
                  history = new AuctionHistory();
                  history.setAuctionId(auctionItem.getAuctionId());
                  history.setAccountId(auctionItem.getAccountId());
                  history.setCharacterId(auctionItem.getCharacterId());
                  history.setItemId(auctionItem.getItem().getItemId());
                  history.setState(auctionItem.getState());
                  history.setPrice(auctionItem.getPrice());
                  history.setBuyTime(System.currentTimeMillis());
                  history.setDeposit(auctionItem.getDeposit());
                  history.setQuantity(auctionItem.getItem().getQuantity());
                  history.setWorldId(auctionItem.getWorldId());
                  history.setId((long)AuctionHistoryIdentifier.getInstance());
                  history.setBidUserId(c.getPlayer().getId());
                  history.setBidUserName(c.getPlayer().getName());
                  auctionItem.setHistory(history);
                  items.put(auctionItem.getAuctionId(), auctionItem);
                  var10000 = FileoutputUtil.경매장구매로그;
                  var10001 = c.getAccID();
                  FileoutputUtil.log(var10000, "[구입] 계정 번호 : " + var10001 + " | 캐릭 번호 : " + c.getPlayer().getId() + " | 캐릭터 닉네임 : " + c.getPlayer().getName() + " | 아이템 이름 : " + ii.getName(auctionItem2.getItem().getItemId()) + " | 아이템 코드 : " + auctionItem2.getItem().getItemId() + " | 수량 " + auctionItem2.getItem().getQuantity() + " | 구매 메소 : " + nPrice + " | 해당 아이템 판매자 이름 : " + auctionItem2.getName());
               }

               auctionItem = new AuctionItem();
               auctionItem.setAuctionType(auctionItem2.getAuctionType());
               auctionItem.setItem(item1);
               auctionItem.setPrice(nPrice);
               auctionItem.setDirectPrice(auctionItem2.getDirectPrice());
               auctionItem.setEndDate(auctionItem2.getEndDate());
               auctionItem.setRegisterDate(auctionItem2.getRegisterDate());
               auctionItem.setAccountId(auctionItem2.getAccountId());
               auctionItem.setCharacterId(auctionItem2.getCharacterId());
               auctionItem.setState(2);
               auctionItem.setWorldId(auctionItem2.getWorldId());
               auctionItem.setName(auctionItem2.getName());
               auctionItem.setBidUserId(c.getPlayer().getId());
               auctionItem.setBidUserName(c.getPlayer().getName());
               auctionItem.setAuctionId(AuctionItemIdentifier.getInstance());
               history = new AuctionHistory();
               history.setAuctionId(auctionItem.getAuctionId());
               history.setAccountId(auctionItem.getAccountId());
               history.setCharacterId(auctionItem.getCharacterId());
               history.setItemId(auctionItem.getItem().getItemId());
               history.setState(auctionItem.getState());
               history.setPrice(auctionItem.getPrice());
               history.setBuyTime(System.currentTimeMillis());
               history.setDeposit(auctionItem.getDeposit());
               history.setQuantity(auctionItem.getItem().getQuantity());
               history.setWorldId(auctionItem.getWorldId());
               history.setId((long)AuctionHistoryIdentifier.getInstance());
               history.setBidUserId(c.getPlayer().getId());
               history.setBidUserName(c.getPlayer().getName());
               auctionItem.setHistory(history);
               items.put(auctionItem.getAuctionId(), auctionItem);
               c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteItemUpdate(auctionItem, item1));
               c.getSession().writeAndFlush(CField.AuctionPacket.AuctionBuyItemResult(0, dwAuctionID));
               ch = World.Find.findAccChannel(auctionItem2.getAccountId());
               if (ch >= 0) {
                  MapleClient ac = AuctionServer.getPlayerStorage().getClientById(auctionItem2.getAccountId());
                  if (ac == null) {
                     ac = ChannelServer.getInstance(ch).getPlayerStorage().getClientById(auctionItem2.getAccountId());
                  }

                  if (ac != null) {
                     ac.getSession().writeAndFlush(CWvsContext.AlarmAuction(0, auctionItem));
                  }
               }
            }
         }
         break;
      case 30:
         dwInventoryId = slea.readLong();
         k = slea.readInt();
         dwAccountId = slea.readInt();
         dwCharacterId = slea.readInt();
         nItemId = slea.readInt();
         nState = slea.readInt();
         l3 = slea.readLong();
         nBuyTime = slea.readLong();
         deposit = slea.readInt();
         deposit = slea.readInt();
         i3 = slea.readInt();
         nWorldId = slea.readInt();
         if (i3 < 0 || i3 > 32767 || l3 < 0L) {
            System.out.println(c.getPlayer().getName() + " 캐릭터가 경매장에 비정상적인 패킷을 유도함.");
            c.getSession().close();
            return;
         }

         AuctionItem auctionItem4 = (AuctionItem)items.get(k);
         if (auctionItem4 != null && auctionItem4.getItem() != null && auctionItem4.getHistory() != null) {
            history = auctionItem4.getHistory();
            if (history.getId() != dwInventoryId) {
               System.out.println("return 1");
               return;
            }

            if (history.getAuctionId() != k) {
               System.out.println("return 2");
               return;
            }

            if (history.getAccountId() != dwAccountId) {
               System.out.println("return 3");
               return;
            }

            if (history.getCharacterId() != dwCharacterId) {
               System.out.println("return 4");
               return;
            }

            if (history.getItemId() != nItemId) {
               System.out.println("return 5");
               return;
            }

            if (history.getState() != nState) {
               System.out.println("return 6");
               return;
            }

            if (history.getPrice() != l3) {
               System.out.println("return 7");
               return;
            }

            if (PacketHelper.getTime(history.getBuyTime()) != nBuyTime) {
               System.out.println("return 8");
               return;
            }

            if (history.getDeposit() != deposit) {
               System.out.println("return 9");
               return;
            }

            if (history.getQuantity() != i3) {
               System.out.println("return 10");
               return;
            }

            if (history.getWorldId() != nWorldId) {
               System.out.println("return 11");
               return;
            }

            history.setState(8);
            auctionItem4.setState(8);
            c.getPlayer().gainMeso((long)((double)l3 * 0.95D), false);
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteItemUpdate(auctionItem4));
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteMesoResult());
            var10000 = FileoutputUtil.경매장대금수령로그;
            var10001 = c.getAccID();
            FileoutputUtil.log(var10000, "[대금수령] 계정 번호 : " + var10001 + " | 캐릭 번호 : " + c.getPlayer().getId() + " | 캐릭터 닉네임 : " + c.getPlayer().getName() + " | 아이템 이름 : " + ii.getName(auctionItem4.getItem().getItemId()) + " | 아이템 코드 : " + auctionItem4.getItem().getItemId() + " | 수량 " + auctionItem4.getItem().getQuantity() + " | 받은 메소 : " + (long)((double)l3 * 0.95D) + " | 해당 아이템 판매자 이름 : " + auctionItem4.getName());
         }
         break;
      case 31:
         dwInventoryId = slea.readLong();
         dwAuctionId = slea.readInt();
         dwAccountId = slea.readInt();
         dwCharacterId = slea.readInt();
         nItemId = slea.readInt();
         nState = slea.readInt();
         nPrice = slea.readLong();
         nBuyTime = slea.readLong();
         deposit = slea.readInt();
         deposit = slea.readInt();
         nCount = slea.readInt();
         nWorldId = slea.readInt();
         if (nCount >= 0 && nCount <= 32767 && nPrice >= 0L) {
            item = (AuctionItem)items.get(dwAuctionId);
            if (item != null && item.getItem() != null && item.getHistory() != null) {
               it = item.getItem().copy();
               AuctionHistory history = item.getHistory();
               if (history.getId() != dwInventoryId) {
                  System.out.println("return 1");
                  return;
               }

               if (history.getAuctionId() != dwAuctionId) {
                  System.out.println("return 2");
                  return;
               }

               if (history.getAccountId() != dwAccountId) {
                  System.out.println("return 3");
                  return;
               }

               if (history.getCharacterId() != dwCharacterId) {
                  System.out.println("return 4");
                  return;
               }

               if (history.getItemId() != nItemId) {
                  System.out.println("return 5");
                  return;
               }

               if (history.getState() != nState) {
                  System.out.println("return 6");
                  return;
               }

               if (history.getPrice() != nPrice) {
                  System.out.println("return 7");
                  return;
               }

               if (PacketHelper.getTime(history.getBuyTime()) != nBuyTime) {
                  System.out.println("return 8");
                  return;
               }

               if (history.getDeposit() != deposit) {
                  System.out.println("return 9");
                  return;
               }

               if (history.getQuantity() != nCount) {
                  System.out.println("return 10");
                  return;
               }

               if (history.getWorldId() != nWorldId) {
                  System.out.println("return 11");
                  return;
               }

               if (c.getPlayer().getId() != dwCharacterId) {
                  if (ItemFlag.KARMA_EQUIP.check(it.getFlag())) {
                     it.setFlag(it.getFlag() - ItemFlag.KARMA_EQUIP.getValue());
                  } else if (ItemFlag.KARMA_USE.check(it.getFlag())) {
                     it.setFlag(it.getFlag() - ItemFlag.KARMA_USE.getValue());
                  }
               }

               short slot = c.getPlayer().getInventory(GameConstants.getInventoryType(nItemId)).addItem(it);
               if (slot >= 0) {
                  item.setState(item.getState() + 5);
                  history.setState(history.getState() + 5);
                  it.setGMLog(StringUtil.getAllCurrentTime() + "에 " + "경매장에서 얻은 " + dwCharacterId + "의 아이템.");
                  c.getSession().writeAndFlush(CWvsContext.InventoryPacket.addInventorySlot(GameConstants.getInventoryType(nItemId), it));
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteItemUpdate(item));
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteItemResult());
               }
            }
            break;
         }

         System.out.println(c.getPlayer().getName() + " 캐릭터가 경매장에 비정상적인 패킷을 유도함.");
         c.getSession().close();
         return;
      case 40:
      case 41:
         List<AuctionItem> searchItems = new ArrayList();
         slea.skip(1);
         int searchType = slea.readInt();
         String nameWithSpace = slea.readMapleAsciiString();
         String nameWithoutSpace = slea.readMapleAsciiString();
         if (searchType == -1) {
            var66 = items.values().iterator();

            label533:
            while(true) {
               String name;
               do {
                  do {
                     do {
                        if (!var66.hasNext()) {
                           break label533;
                        }

                        auctionItem = (AuctionItem)var66.next();
                        name = ii.getName(auctionItem.getItem().getItemId());
                     } while(name == null);
                  } while(!name.replaceAll(" ", "").contains(nameWithSpace) && !name.replaceAll(" ", "").contains(nameWithoutSpace));
               } while((op != 40 || auctionItem.getState() != 0) && (op != 41 || auctionItem.getState() != 3 && auctionItem.getState() != 8));

               searchItems.add(auctionItem);
            }
         } else {
            itemType = slea.readInt();
            int itemSemiType = slea.readInt();
            int lvMin = slea.readInt();
            int lvMax = slea.readInt();
            long priceMin = slea.readLong();
            long priceMax = slea.readLong();
            int potentialType = slea.readInt();
            boolean and = slea.readByte() == 1;
            int optionalSearchCount = slea.readInt();

            int level;
            int level;
            for(int i4 = 0; i4 < optionalSearchCount; ++i4) {
               boolean isStarForce = slea.readInt() == 1;
               level = slea.readInt();
               level = slea.readInt();
            }

            boolean lvLimit;
            boolean priceLimit;
            Iterator var105;
            AuctionItem auctionItem;
            if (searchType <= 1) {
               var105 = items.values().iterator();

               label608:
               while(true) {
                  do {
                     String name;
                     do {
                        boolean potentialLimit;
                        do {
                           do {
                              do {
                                 boolean typeLimit;
                                 do {
                                    do {
                                       do {
                                          do {
                                             if (!var105.hasNext()) {
                                                break label608;
                                             }

                                             auctionItem = (AuctionItem)var105.next();
                                          } while(auctionItem.getItem() == null);
                                       } while(auctionItem.getItem().getType() != 1);

                                       Equip equip = (Equip)auctionItem.getItem();
                                       level = ii.getReqLevel(auctionItem.getItem().getItemId());
                                       lvLimit = level >= lvMin && level <= lvMax;
                                       priceLimit = auctionItem.getPrice() >= priceMin && auctionItem.getPrice() <= priceMax;
                                       potentialLimit = potentialType == -1 || potentialType == 0 && equip.getState() == 0 || potentialType > 0 && equip.getState() - 16 == potentialType;
                                       typeLimit = typeLimit(searchType, itemType, itemSemiType, equip.getItemId());
                                       name = ii.getName(auctionItem.getItem().getItemId());
                                    } while(!typeLimit);
                                 } while(!lvLimit);
                              } while(!priceLimit);
                           } while(!potentialLimit);
                        } while(!name.contains(nameWithSpace) && !name.contains(nameWithoutSpace) && !nameWithoutSpace.isEmpty());
                     } while(!equipOptionTypes());
                  } while((op != 40 || auctionItem.getState() != 0) && (op != 41 || auctionItem.getState() != 3 && auctionItem.getState() != 8));

                  searchItems.add(auctionItem);
               }
            } else {
               var105 = items.values().iterator();

               label654:
               while(true) {
                  String name;
                  do {
                     do {
                        boolean lvLimit;
                        do {
                           do {
                              do {
                                 if (!var105.hasNext()) {
                                    break label654;
                                 }

                                 auctionItem = (AuctionItem)var105.next();
                                 level = ii.getReqLevel(auctionItem.getItem().getItemId());
                                 lvLimit = level >= lvMin && level <= lvMax;
                                 lvLimit = auctionItem.getPrice() >= priceMin && auctionItem.getPrice() <= priceMax;
                                 priceLimit = typeLimit(searchType, itemType, itemSemiType, auctionItem.getItem().getItemId());
                                 name = ii.getName(auctionItem.getItem().getItemId());
                              } while(!priceLimit);
                           } while(!lvLimit);
                        } while(!lvLimit);
                     } while(!name.contains(nameWithSpace) && !name.contains(nameWithoutSpace) && !nameWithoutSpace.isEmpty());
                  } while((op != 40 || auctionItem.getState() != 0) && (op != 41 || auctionItem.getState() != 3 && auctionItem.getState() != 8));

                  searchItems.add(auctionItem);
               }
            }
         }

         if (op == 40) {
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionSearchItems(searchItems));
         } else {
            c.getSession().writeAndFlush(CField.AuctionPacket.AuctionMarketPrice(searchItems));
         }

         var10000 = FileoutputUtil.경매장물품반환로그;
         var10001 = c.getAccID();
         FileoutputUtil.log(var10000, "[검색] 계정 번호 : " + var10001 + " | 캐릭 번호 : " + c.getPlayer().getId() + " | 캐릭터 닉네임 : " + c.getPlayer().getName() + " | 아이템 검색 : " + nameWithoutSpace);
         break;
      case 45:
         int j = slea.readInt();
         AuctionItem auctionItem1 = (AuctionItem)items.get(j);
         if (auctionItem1 != null) {
            for(itemType = 0; itemType < 10; ++itemType) {
               if (c.getKeyValue("wish" + itemType) == null) {
                  c.setKeyValue("wish" + itemType, String.valueOf(j));
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionAddWishlist(auctionItem1));
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionWishlistResult(auctionItem1));
                  break;
               }
            }
         }
         break;
      case 46:
         List<Integer> wishlist = new ArrayList();
         List<AuctionItem> wishItems = new ArrayList();

         for(int i = 0; i < 10; ++i) {
            wish = c.getKeyValue("wish" + i);
            if (wish != null) {
               wishlist.add(Integer.parseInt(wish));
            }
         }

         Iterator iterator = wishlist.iterator();

         while(iterator.hasNext()) {
            itemType = (Integer)iterator.next();
            auctionItem = (AuctionItem)items.get(itemType);
            if (auctionItem != null) {
               wishItems.add(auctionItem);
            }
         }

         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionWishlist(wishItems));
         break;
      case 47:
         dwAuctionId = slea.readInt();
         item = (AuctionItem)items.get(dwAuctionId);
         if (item != null) {
            for(itemType = 0; itemType < 10; ++itemType) {
               if (c.getKeyValue("wish" + itemType).equals(String.valueOf(dwAuctionId))) {
                  c.removeKeyValue("wish" + itemType);
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionWishlistUpdate(dwAuctionId));
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionWishlistDeleteResult(dwAuctionId));
                  break;
               }
            }
         }
         break;
      case 50:
         List<AuctionItem> sellingItems = new ArrayList();
         var66 = items.values().iterator();

         while(var66.hasNext()) {
            auctionItem = (AuctionItem)var66.next();
            if (auctionItem.getAccountId() == c.getAccID() && auctionItem.getState() == 0) {
               sellingItems.add(auctionItem);
            }
         }

         c.getSession().writeAndFlush(CField.AuctionPacket.AuctionSellingMyItems(sellingItems));
         break;
      case 51:
         completeItems = new ArrayList();
         var66 = items.values().iterator();

         label680:
         while(true) {
            do {
               if (!var66.hasNext()) {
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteItems(completeItems));
                  break label680;
               }

               auctionItem = (AuctionItem)var66.next();
            } while((auctionItem.getState() != 2 && auctionItem.getState() != 7 || auctionItem.getBidUserId() != c.getPlayer().getId()) && (auctionItem.getState() == 7 || auctionItem.getState() < 3 || auctionItem.getAccountId() != c.getAccID()));

            completeItems.add(auctionItem);
         }
      }

      if (op != 0 && op != 1 && op != 41 && op != 40 && op != 20 && op != 21) {
         completeItems = new ArrayList();
         List<AuctionItem> list5 = new ArrayList();
         List<AuctionItem> list6 = new ArrayList();
         new ArrayList();
         iterator1 = items.entrySet().iterator();

         while(true) {
            AuctionItem auctionItem;
            do {
               if (!iterator1.hasNext()) {
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionCompleteItems(completeItems));
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionMarketPrice(list5));
                  c.getSession().writeAndFlush(CField.AuctionPacket.AuctionSearchItems(list6));
                  return;
               }

               Entry<Integer, AuctionItem> itemz = (Entry)iterator1.next();
               auctionItem = (AuctionItem)itemz.getValue();
               if ((auctionItem.getEndDate() < System.currentTimeMillis() || auctionItem.getState() >= 2) && (auctionItem.getState() == 2 && auctionItem.getBidUserId() == c.getPlayer().getId() || (auctionItem.getState() == 3 || auctionItem.getState() == 4) && auctionItem.getAccountId() == c.getAccID())) {
                  completeItems.add(auctionItem);
               }

               if (auctionItem.getState() == 0 && list6.size() < 1000) {
                  list6.add(auctionItem);
               }
            } while(auctionItem.getState() != 3 && auctionItem.getState() != 8);

            if (list5.size() < 1000) {
               list5.add(auctionItem);
            }
         }
      }
   }

   private static boolean equipOptionTypes() {
      return true;
   }

   private static boolean typeLimit(int searchType, int itemType, int itemSemiType, int itemId) {
      switch(searchType) {
      case 0:
         label592:
         switch(itemType) {
         case 0:
            return !GameConstants.isWeapon(itemId);
         case 1:
            switch(itemSemiType) {
            case 0:
               return GameConstants.isWeapon(itemId) || GameConstants.isAccessory(itemId);
            case 1:
               return itemId / 1000 == 100;
            case 2:
               return itemId / 1000 == 104;
            case 3:
               return itemId / 1000 == 105;
            case 4:
               return itemId / 1000 == 106;
            case 5:
               return itemId / 1000 == 107;
            case 6:
               return itemId / 1000 == 108;
            case 7:
               return itemId / 1000 == 109;
            case 8:
               return itemId / 1000 == 110;
            default:
               break label592;
            }
         case 2:
            switch(itemSemiType) {
            case 0:
               return !GameConstants.isAccessory(itemId);
            case 1:
               return itemId / 1000 == 1012;
            case 2:
               return itemId / 1000 == 1022;
            case 3:
               return itemId / 1000 == 1032;
            case 4:
               return GameConstants.isRing(itemId);
            case 5:
               return itemId / 1000 == 1122 || itemId / 1000 == 1123;
            case 6:
               return itemId / 1000 == 1132;
            case 7:
               return GameConstants.isMedal(itemId);
            case 8:
               return itemId / 1000 == 1152;
            case 9:
               return itemId / 1000 == 1162;
            case 10:
               return itemId / 1000 == 1182;
            default:
               break label592;
            }
         case 3:
            switch(itemSemiType) {
            case 0:
               return itemId / 1000 >= 1612 && itemId / 1000 <= 1652;
            case 1:
               return itemId / 1000 == 1662;
            case 2:
               return itemId / 1000 == 1672;
            case 3:
               return itemId / 1000 >= 1942 && itemId / 1000 <= 1972;
            }
         }
      case 1:
         switch(itemType) {
         case 0:
            return GameConstants.isWeapon(itemId);
         case 1:
            switch(itemSemiType) {
            case 0:
               return !GameConstants.isTwoHanded(itemId);
            case 1:
               return itemId / 1000 == 1212;
            case 2:
               return itemId / 1000 == 1222;
            case 3:
               return itemId / 1000 == 1232;
            case 4:
               return itemId / 1000 == 1242;
            case 5:
               return itemId / 1000 == 1302;
            case 6:
               return itemId / 1000 == 1312;
            case 7:
               return itemId / 1000 == 1322;
            case 8:
               return itemId / 1000 == 1332;
            case 9:
               return itemId / 1000 == 1342;
            case 10:
               return itemId / 1000 == 1362;
            case 11:
               return itemId / 1000 == 1372;
            case 12:
               return itemId / 1000 == 1262;
            case 13:
               return itemId / 1000 == 1272;
            case 14:
               return itemId / 1000 == 1282;
            case 15:
               return itemId / 1000 == 1292;
            case 16:
               return itemId / 1000 == 1213;
            default:
               return false;
            }
         case 2:
            switch(itemSemiType) {
            case 0:
               return GameConstants.isTwoHanded(itemId);
            case 1:
               return itemId / 1000 == 1402;
            case 2:
               return itemId / 1000 == 1412;
            case 3:
               return itemId / 1000 == 1422;
            case 4:
               return itemId / 1000 == 1432;
            case 5:
               return itemId / 1000 == 1442;
            case 6:
               return itemId / 1000 == 1452;
            case 7:
               return itemId / 1000 == 1462;
            case 8:
               return itemId / 1000 == 1472;
            case 9:
               return itemId / 1000 == 1482;
            case 10:
               return itemId / 1000 == 1492;
            case 11:
               return itemId / 1000 == 1522;
            case 12:
               return itemId / 1000 == 1532;
            case 13:
               return itemId / 1000 == 1582;
            case 14:
               return itemId / 1000 == 1592;
            default:
               return false;
            }
         case 3:
            switch(itemSemiType) {
            case 0:
               return itemId / 1000 == 1352 || itemId / 1000 == 1353;
            case 1:
               return itemId / 10 == 135220;
            case 2:
               return itemId / 10 == 135221;
            case 3:
               return itemId / 10 == 135222;
            case 4:
               return itemId / 10 == 135223 || itemId / 10 == 135224 || itemId / 10 == 135225;
            case 5:
               return itemId / 10 == 135226;
            case 6:
               return itemId / 10 == 135227;
            case 7:
               return itemId / 10 == 135228;
            case 8:
               return itemId / 10 == 135229;
            case 9:
               return itemId / 10 == 135290;
            case 10:
               return itemId / 10 == 135291;
            case 11:
               return itemId / 10 == 135292;
            case 12:
               return itemId / 10 == 135297;
            case 13:
               return itemId / 10 == 135293;
            case 14:
               return itemId / 10 == 135294;
            case 15:
               return itemId / 10 == 135240;
            case 16:
               return itemId / 10 == 135201;
            case 17:
               return itemId / 10 == 135210;
            case 18:
               return itemId / 10 == 135310;
            case 19:
               return itemId / 10 == 135295;
            case 20:
               return itemId / 10 == 135296;
            case 21:
               return itemId / 10 == 135300;
            case 22:
               return itemId / 10 == 135270;
            case 23:
               return itemId / 10 == 135250;
            case 24:
               return itemId / 10 == 135260;
            case 25:
               return itemId / 10 == 135320;
            case 26:
               return itemId / 10 == 135340;
            case 27:
               return itemId / 10 == 135330;
            case 28:
               return itemId / 10 == 135350;
            case 29:
               return itemId / 10 == 135360;
            case 30:
               return itemId / 10 == 135370;
            case 31:
               return itemId / 10 == 135380;
            case 32:
               return itemId / 10 == 135390;
            }
         }
      default:
         return false;
      case 2:
         return itemId / 1000000 == 2;
      case 3:
         return MapleItemInformationProvider.getInstance().isCash(itemId);
      case 4:
         return itemId / 1000000 == 4 || itemId / 1000000 == 3;
      }
   }
}
