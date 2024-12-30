package handling.world;

import client.BuddyList;
import client.BuddylistEntry;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import database.DatabaseConnection;
import handling.RecvPacketOpcode;
import handling.auction.AuctionServer;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.PlayerStorage;
import handling.farm.FarmServer;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import handling.world.guild.MapleGuildCharacter;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartyOperation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import log.DBLogger;
import log.LogType;
import tools.CollectionUtil;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class World {
   public static void init() {
      World.Find.findChannel(0);
      World.Messenger.getMessenger(0);
      World.Party.getParty(0);
   }

   public static List<MapleCharacter> getAllCharacters() {
      List<MapleCharacter> temp = new ArrayList();
      Iterator var1 = ChannelServer.getAllInstances().iterator();

      while(var1.hasNext()) {
         ChannelServer cs = (ChannelServer)var1.next();
         Iterator var3 = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(var3.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var3.next();
            if (!temp.contains(chr)) {
               temp.add(chr);
            }
         }
      }

      var1 = CashShopServer.getPlayerStorage().getAllCharacters().values().iterator();

      MapleCharacter chr2;
      while(var1.hasNext()) {
         chr2 = (MapleCharacter)var1.next();
         temp.add(chr2);
      }

      var1 = AuctionServer.getPlayerStorage().getAllCharacters().values().iterator();

      while(var1.hasNext()) {
         chr2 = (MapleCharacter)var1.next();
         temp.add(chr2);
      }

      var1 = FarmServer.getPlayerStorage().getAllCharacters().values().iterator();

      while(var1.hasNext()) {
         chr2 = (MapleCharacter)var1.next();
         temp.add(chr2);
      }

      return temp;
   }

   public static MapleCharacter getChar(int id) {
      Iterator var1 = ChannelServer.getAllInstances().iterator();

      while(var1.hasNext()) {
         ChannelServer cs = (ChannelServer)var1.next();
         Iterator var3 = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(var3.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var3.next();
            if (chr.getId() == id) {
               return chr;
            }
         }
      }

      var1 = CashShopServer.getPlayerStorage().getAllCharacters().values().iterator();

      MapleCharacter csplayer;
      do {
         if (!var1.hasNext()) {
            var1 = AuctionServer.getPlayerStorage().getAllCharacters().values().iterator();

            do {
               if (!var1.hasNext()) {
                  var1 = FarmServer.getPlayerStorage().getAllCharacters().values().iterator();

                  do {
                     if (!var1.hasNext()) {
                        return null;
                     }

                     csplayer = (MapleCharacter)var1.next();
                  } while(csplayer.getId() != id);

                  return csplayer;
               }

               csplayer = (MapleCharacter)var1.next();
            } while(csplayer.getId() != id);

            return csplayer;
         }

         csplayer = (MapleCharacter)var1.next();
      } while(csplayer.getId() != id);

      return csplayer;
   }

   public static String getStatus() {
      StringBuilder ret = new StringBuilder();
      int totalUsers = 0;
      Iterator var2 = ChannelServer.getAllInstances().iterator();

      while(var2.hasNext()) {
         ChannelServer cs = (ChannelServer)var2.next();
         ret.append("Channel ");
         ret.append(cs.getChannel());
         ret.append(": ");
         int channelUsers = cs.getConnectedClients();
         totalUsers += channelUsers;
         ret.append(channelUsers);
         ret.append(" users\n");
      }

      ret.append("Total users online: ");
      ret.append(totalUsers);
      ret.append("\n");
      return ret.toString();
   }

   public static Map<Integer, Integer> getConnected() {
      Map<Integer, Integer> ret = new HashMap();
      int total = 0;

      int curConnected;
      for(Iterator var2 = ChannelServer.getAllInstances().iterator(); var2.hasNext(); total += curConnected) {
         ChannelServer cs = (ChannelServer)var2.next();
         curConnected = cs.getConnectedClients();
         ret.put(cs.getChannel(), curConnected);
      }

      ret.put(0, total);
      return ret;
   }

   public static List<CheaterData> getCheaters() {
      List<CheaterData> allCheaters = new ArrayList();
      Iterator var1 = ChannelServer.getAllInstances().iterator();

      while(var1.hasNext()) {
         ChannelServer cs = (ChannelServer)var1.next();
         allCheaters.addAll(cs.getCheaters());
      }

      Collections.sort(allCheaters);
      return CollectionUtil.copyFirst(allCheaters, 20);
   }

   public static List<CheaterData> getReports() {
      List<CheaterData> allCheaters = new ArrayList();
      Iterator var1 = ChannelServer.getAllInstances().iterator();

      while(var1.hasNext()) {
         ChannelServer cs = (ChannelServer)var1.next();
         allCheaters.addAll(cs.getReports());
      }

      Collections.sort(allCheaters);
      return CollectionUtil.copyFirst(allCheaters, 20);
   }

   public static boolean isConnected(String charName) {
      return World.Find.findChannel(charName) > 0;
   }

   public static void toggleMegaphoneMuteState() {
      Iterator var0 = ChannelServer.getAllInstances().iterator();

      while(var0.hasNext()) {
         ChannelServer cs = (ChannelServer)var0.next();
         cs.toggleMegaphoneMuteState();
      }

   }

   public static void ChannelChange_Data(CharacterTransfer Data, int characterid, int toChannel) {
      getStorage(toChannel).registerPendingPlayer(Data, characterid);
   }

   public static void isCharacterListConnected(String name, List<String> charName) {
      Iterator var2 = ChannelServer.getAllInstances().iterator();

      while(var2.hasNext()) {
         ChannelServer cs = (ChannelServer)var2.next();
         Iterator var4 = charName.iterator();

         while(var4.hasNext()) {
            String c = (String)var4.next();
            if (cs.getPlayerStorage().getCharacterByName(c) != null) {
               cs.getPlayerStorage().deregisterPlayer(cs.getPlayerStorage().getCharacterByName(c));
            }
         }
      }

   }

   public static PlayerStorage getStorage(int channel) {
      if (channel == -10) {
         return CashShopServer.getPlayerStorage();
      } else {
         return channel == -20 ? AuctionServer.getPlayerStorage() : ChannelServer.getInstance(channel).getPlayerStorage();
      }
   }

   public static int getPendingCharacterSize() {
      int ret = 0;

      ChannelServer cserv;
      for(Iterator var1 = ChannelServer.getAllInstances().iterator(); var1.hasNext(); ret += cserv.getPlayerStorage().pendingCharacterSize()) {
         cserv = (ChannelServer)var1.next();
      }

      return ret;
   }

   public static boolean isChannelAvailable(int ch) {
      return ChannelServer.getInstance(ch) != null && ChannelServer.getInstance(ch).getPlayerStorage() != null && ChannelServer.getInstance(ch).getPlayerStorage().getConnectedClients() < (ch == 1 ? 600 : 400);
   }

   public static class Find {
      private static Map<Integer, Integer> idToChannel = new ConcurrentHashMap();
      private static Map<String, Integer> nameToChannel = new ConcurrentHashMap();
      private static Map<Integer, Integer> accIdToChannel = new ConcurrentHashMap();

      public static void forceDeregister(int id) {
         idToChannel.remove(id);
      }

      public static void forceDeregister(String id) {
         nameToChannel.remove(id.toLowerCase());
      }

      public static void register(int id, int accId, String name, int channel) {
         idToChannel.put(id, channel);
         nameToChannel.put(name.toLowerCase(), channel);
         accIdToChannel.put(accId, channel);
      }

      public static void forceAccDeregister(int id) {
         accIdToChannel.remove(id);
      }

      public static void forceDeregister(int id, int accId, String name) {
         idToChannel.remove(id);
         nameToChannel.remove(name.toLowerCase());
         accIdToChannel.remove(accId);
      }

      public static int findChannel(int id) {
         Integer ret = (Integer)idToChannel.get(id);
         if (ret == null) {
            return -1;
         } else if (ret != -10 && ChannelServer.getInstance(ret) == null) {
            forceDeregister(id);
            return -1;
         } else {
            return ret;
         }
      }

      public static int findChannel(String st) {
         Integer ret = (Integer)nameToChannel.get(st.toLowerCase());
         if (ret == null) {
            return -1;
         } else if (ret != -10 && ChannelServer.getInstance(ret) == null) {
            forceDeregister(st);
            return -1;
         } else {
            return ret;
         }
      }

      public static int findAccChannel(int id) {
         Integer ret = (Integer)accIdToChannel.get(id);
         if (ret == null) {
            return -1;
         } else if (ret != -10 && ChannelServer.getInstance(ret) == null) {
            forceAccDeregister(id);
            return -1;
         } else {
            return ret;
         }
      }

      public static AccountIdChannelPair[] multiBuddyFind(BuddyList bl, int charIdFrom, int[] accIds) {
         List<AccountIdChannelPair> foundsChars = new ArrayList(accIds.length);
         int[] var4 = accIds;
         int var5 = accIds.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int i = var4[var6];
            int ret = findAccChannel(i);
            if (ret > 0) {
               MapleClient c = ChannelServer.getInstance(ret).getPlayerStorage().getClientById(i);
               if (bl.contains(i) && c != null) {
                  BuddylistEntry ble = bl.get(i);
                  ble.setCharacterId(c.getPlayer().getId());
                  ble.setName(c.getPlayer().getName());
               }
            }

            foundsChars.add(new AccountIdChannelPair(i, ret));
         }

         Collections.sort(foundsChars);
         return (AccountIdChannelPair[])foundsChars.toArray(new AccountIdChannelPair[foundsChars.size()]);
      }
   }

   public static class Messenger {
      private static Map<Integer, MapleMessenger> messengers = new HashMap();
      private static final AtomicInteger runningMessengerId;

      public static MapleMessenger createMessenger(MapleMessengerCharacter chrfor) {
         int messengerid = runningMessengerId.getAndIncrement();
         MapleMessenger messenger = new MapleMessenger(messengerid, chrfor);
         messengers.put(messenger.getId(), messenger);
         return messenger;
      }

      public static void declineChat(String target, String namefrom) {
         int ch = World.Find.findChannel(target);
         if (ch > 0) {
            ChannelServer cs = ChannelServer.getInstance(ch);
            MapleCharacter chr = cs.getPlayerStorage().getCharacterByName(target);
            if (chr != null) {
               MapleMessenger messenger = chr.getMessenger();
               if (messenger != null) {
                  chr.getClient().getSession().writeAndFlush(CField.messengerNote(namefrom, 5, 0));
               }
            }
         }

      }

      public static MapleMessenger getMessenger(int messengerid) {
         return (MapleMessenger)messengers.get(messengerid);
      }

      public static void leaveMessenger(int messengerid, MapleMessengerCharacter target) {
         MapleMessenger messenger = getMessenger(messengerid);
         if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
         } else {
            int position = messenger.getPositionByName(target.getName());
            messenger.removeMember(target);
            Iterator var4 = messenger.getMembers().iterator();

            while(var4.hasNext()) {
               MapleMessengerCharacter mmc = (MapleMessengerCharacter)var4.next();
               if (mmc != null) {
                  int ch = World.Find.findChannel(mmc.getId());
                  if (ch > 0) {
                     MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(mmc.getName());
                     if (chr != null) {
                        chr.getClient().getSession().writeAndFlush(CField.removeMessengerPlayer(position));
                     }
                  }
               }
            }

         }
      }

      public static void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target) {
         MapleMessenger messenger = getMessenger(messengerid);
         if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
         } else {
            messenger.silentRemoveMember(target);
         }
      }

      public static void silentJoinMessenger(int messengerid, MapleMessengerCharacter target) {
         MapleMessenger messenger = getMessenger(messengerid);
         if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
         } else {
            messenger.silentAddMember(target);
         }
      }

      public static void updateMessenger(int messengerid, String namefrom, int fromchannel) {
         MapleMessenger messenger = getMessenger(messengerid);
         int position = messenger.getPositionByName(namefrom);
         Iterator var5 = messenger.getMembers().iterator();

         while(var5.hasNext()) {
            MapleMessengerCharacter messengerchar = (MapleMessengerCharacter)var5.next();
            if (messengerchar != null && !messengerchar.getName().equals(namefrom)) {
               int ch = World.Find.findChannel(messengerchar.getName());
               if (ch > 0) {
                  MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
                  if (chr != null) {
                     MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(namefrom);
                     chr.getClient().getSession().writeAndFlush(CField.updateMessengerPlayer(namefrom, from, position, fromchannel - 1));
                  }
               }
            }
         }

      }

      public static void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) {
         MapleMessenger messenger = getMessenger(messengerid);
         if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
         } else {
            messenger.addMember(target);
            int position = messenger.getPositionByName(target.getName());
            Iterator var6 = messenger.getMembers().iterator();

            while(var6.hasNext()) {
               MapleMessengerCharacter messengerchar = (MapleMessengerCharacter)var6.next();
               if (messengerchar != null) {
                  int mposition = messenger.getPositionByName(messengerchar.getName());
                  int ch = World.Find.findChannel(messengerchar.getName());
                  if (ch > 0) {
                     MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
                     if (chr != null) {
                        if (!messengerchar.getName().equals(from)) {
                           MapleCharacter fromCh = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(from);
                           if (fromCh != null) {
                              chr.getClient().getSession().writeAndFlush(CField.addMessengerPlayer(from, fromCh, position, fromchannel - 1));
                              fromCh.getClient().getSession().writeAndFlush(CField.addMessengerPlayer(chr.getName(), chr, mposition, messengerchar.getChannel() - 1));
                           }
                        } else {
                           chr.getClient().getSession().writeAndFlush(CField.joinMessenger(mposition));
                        }
                     }
                  }
               }
            }

         }
      }

      public static void messengerChat(int messengerid, String charname, String text, String namefrom) {
         MapleMessenger messenger = getMessenger(messengerid);
         if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
         } else {
            Iterator var5 = messenger.getMembers().iterator();

            while(var5.hasNext()) {
               MapleMessengerCharacter messengerchar = (MapleMessengerCharacter)var5.next();
               if (messengerchar != null && !messengerchar.getName().equals(namefrom)) {
                  int ch = World.Find.findChannel(messengerchar.getName());
                  if (ch > 0) {
                     MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
                     if (chr != null) {
                        chr.getClient().getSession().writeAndFlush(CField.messengerChat(charname, text));
                     }
                  }
               }
            }

         }
      }

      public static void messengerWhisperChat(int messengerid, String charname, String text, String namefrom) {
         MapleMessenger messenger = getMessenger(messengerid);
         if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
         } else {
            Iterator var5 = messenger.getMembers().iterator();

            while(var5.hasNext()) {
               MapleMessengerCharacter messengerchar = (MapleMessengerCharacter)var5.next();
               if (messengerchar != null && !messengerchar.getName().equals(namefrom)) {
                  int ch = World.Find.findChannel(messengerchar.getName());
                  if (ch > 0) {
                     MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
                     if (chr != null) {
                        chr.getClient().getSession().writeAndFlush(CField.messengerWhisperChat(charname, text));
                     }
                  }
               }
            }

         }
      }

      public static void messengerInvite(String sender, int messengerid, String target, int fromchannel, boolean gm) {
         if (World.isConnected(target)) {
            int ch = World.Find.findChannel(target);
            if (ch > 0) {
               MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(sender);
               MapleCharacter targeter = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(target);
               if (targeter != null && targeter.getMessenger() == null) {
                  if (targeter.isIntern() && !gm) {
                     from.getClient().getSession().writeAndFlush(CField.messengerNote(target, 4, 0));
                  } else {
                     targeter.getClient().getSession().writeAndFlush(CField.messengerInvite(sender, messengerid));
                     from.getClient().getSession().writeAndFlush(CField.messengerNote(target, 4, 1));
                  }
               } else {
                  from.getClient().getSession().writeAndFlush(CField.messengerChat(sender, " : " + target + " is already using Maple Messenger"));
               }
            }
         }

      }

      static {
         (runningMessengerId = new AtomicInteger()).set(1);
      }
   }

   public static class Party {
      private static Map<Integer, MapleParty> parties = new HashMap();
      private static final AtomicInteger runningPartyId = new AtomicInteger(1);
      private static final AtomicInteger runningExpedId = new AtomicInteger(1);

      public static void partyChat(MapleCharacter chr, String chattext, LittleEndianAccessor slea, RecvPacketOpcode recv) {
         partyChat(chr, chattext, 1, slea, recv);
      }

      public static void partyPacket(int partyid, byte[] packet, MaplePartyCharacter exception) {
         MapleParty party = getParty(partyid);
         if (party != null) {
            Iterator var4 = party.getMembers().iterator();

            while(true) {
               MaplePartyCharacter partychar;
               int ch;
               do {
                  do {
                     if (!var4.hasNext()) {
                        return;
                     }

                     partychar = (MaplePartyCharacter)var4.next();
                     ch = World.Find.findChannel(partychar.getName());
                  } while(ch <= 0);
               } while(exception != null && partychar.getId() == exception.getId());

               MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
               if (chr != null) {
                  chr.getClient().getSession().writeAndFlush(packet);
               }
            }
         }
      }

      public static void partyChat(MapleCharacter player, String chattext, int mode, LittleEndianAccessor slea, RecvPacketOpcode recv) {
         MapleParty party = getParty(player.getParty().getId());
         if (party != null) {
            Item item = null;
            if (recv == RecvPacketOpcode.PARTYCHATITEM && player != null) {
               byte invType = (byte)slea.readInt();
               short pos = (short)slea.readInt();
               item = player.getInventory(MapleInventoryType.getByType(pos > 0 ? invType : -1)).getItem(pos);
            }

            Iterator var11 = party.getMembers().iterator();

            while(var11.hasNext()) {
               MaplePartyCharacter partychar = (MaplePartyCharacter)var11.next();
               int ch = World.Find.findChannel(partychar.getName());
               if (ch > 0) {
                  MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
                  if (chr != null && !chr.getName().equalsIgnoreCase(player.getName())) {
                     chr.getClient().getSession().writeAndFlush(CField.multiChat(player, chattext, mode, item));
                     if (chr.getClient().isMonitored()) {
                     }
                  }
               }
            }

         }
      }

      public static void partyMessage(int partyid, String chattext) {
         MapleParty party = getParty(partyid);
         if (party != null) {
            Iterator var3 = party.getMembers().iterator();

            while(var3.hasNext()) {
               MaplePartyCharacter partychar = (MaplePartyCharacter)var3.next();
               int ch = World.Find.findChannel(partychar.getName());
               if (ch > 0) {
                  MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
                  if (chr != null) {
                     chr.dropMessage(5, chattext);
                  }
               }
            }

         }
      }

      public static void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) {
         MapleParty party = getParty(partyid);
         if (party != null) {
            int oldSize = party.getMembers().size();
            int oldInd = true;
            switch(operation) {
            case JOIN:
               party.addMember(target);
               break;
            case EXPEL:
            case LEAVE:
               party.removeMember(target);
               break;
            case DISBAND:
               disbandParty(partyid);
               break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
               party.updateMember(target);
               break;
            case CHANGE_LEADER:
            case CHANGE_LEADER_DC:
               party.setLeader(target);
               break;
            default:
               throw new RuntimeException("Unhandeled updateParty operation " + operation.name());
            }

            MaplePartyCharacter lchr;
            if (operation == PartyOperation.LEAVE || operation == PartyOperation.EXPEL) {
               int chz = World.Find.findChannel(target.getName());
               if (chz > 0) {
                  MapleCharacter chr = World.getStorage(chz).getCharacterByName(target.getName());
                  if (chr != null) {
                     chr.setParty((MapleParty)null);
                     chr.getClient().getSession().writeAndFlush(CWvsContext.PartyPacket.updateParty(chr.getClient().getChannel(), party, operation, target, chr));
                  }
               }

               if (target.getId() == party.getLeader().getId() && party.getMembers().size() > 0) {
                  lchr = null;
                  Iterator var8 = party.getMembers().iterator();

                  label72:
                  while(true) {
                     MaplePartyCharacter pchr;
                     do {
                        do {
                           if (!var8.hasNext()) {
                              if (lchr != null) {
                                 updateParty(partyid, PartyOperation.CHANGE_LEADER_DC, lchr);
                              }
                              break label72;
                           }

                           pchr = (MaplePartyCharacter)var8.next();
                        } while(pchr == null);
                     } while(lchr != null && lchr.getLevel() >= pchr.getLevel());

                     lchr = pchr;
                  }
               }
            }

            if (party.getMembers().size() <= 0) {
               disbandParty(partyid);
            }

            Iterator var10 = party.getMembers().iterator();

            while(var10.hasNext()) {
               lchr = (MaplePartyCharacter)var10.next();
               if (lchr != null) {
                  int ch = World.Find.findChannel(lchr.getName());
                  if (ch > 0) {
                     MapleCharacter chr2 = World.getStorage(ch).getCharacterByName(lchr.getName());
                     if (chr2 != null) {
                        if (operation == PartyOperation.DISBAND) {
                           chr2.setParty((MapleParty)null);
                        } else {
                           chr2.setParty(party);
                        }

                        chr2.getClient().getSession().writeAndFlush(CWvsContext.PartyPacket.updateParty(chr2.getClient().getChannel(), party, operation, target, chr2));
                        chr2.getStat().recalcLocalStats(chr2);
                     }
                  }
               }
            }

         }
      }

      public static MapleParty createParty(MaplePartyCharacter chrfor) {
         MapleParty party = new MapleParty(runningPartyId.getAndIncrement(), chrfor);
         parties.put(party.getId(), party);
         return party;
      }

      public static MapleParty getParty(int partyid) {
         return (MapleParty)parties.get(partyid);
      }

      public static MapleParty disbandParty(int partyid) {
         MapleParty ret = (MapleParty)parties.remove(partyid);
         if (ret == null) {
            return null;
         } else {
            ret.disband();
            return ret;
         }
      }

      static {
         Connection con = null;
         PreparedStatement ps = null;
         Object rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE characters SET party = -1, fatigue = 0");
            ps.executeUpdate();
            ps.close();
            con.close();
         } catch (SQLException var15) {
            var15.printStackTrace();

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

   public static class Alliance {
      private static final Map<Integer, MapleGuildAlliance> alliances = new ConcurrentHashMap();

      public static MapleGuildAlliance getAlliance(int allianceid) {
         MapleGuildAlliance ret = (MapleGuildAlliance)alliances.get(allianceid);
         if (ret == null) {
            ret = new MapleGuildAlliance(allianceid);
            if (ret == null || ret.getId() <= 0) {
               return null;
            }

            alliances.put(allianceid, ret);
         }

         return ret;
      }

      public static int getAllianceLeader(int allianceid) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         return mga != null ? mga.getLeaderId() : 0;
      }

      public static void updateAllianceRanks(int allianceid, String[] ranks) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         if (mga != null) {
            mga.setRank(ranks);
         }

      }

      public static void updateAllianceNotice(int allianceid, String notice) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         if (mga != null) {
            mga.setNotice(notice);
         }

      }

      public static boolean canInvite(int allianceid) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         return mga != null && mga.getCapacity() > mga.getNoGuilds();
      }

      public static boolean changeAllianceLeader(int allianceid, int cid) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         return mga != null && mga.setLeaderId(cid);
      }

      public static boolean changeAllianceLeader(int allianceid, int cid, boolean sameGuild) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         return mga != null && mga.setLeaderId(cid, sameGuild);
      }

      public static boolean changeAllianceRank(int allianceid, int cid, int change) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         return mga != null && mga.changeAllianceRank(cid, change);
      }

      public static boolean changeAllianceCapacity(int allianceid) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         return mga != null && mga.setCapacity();
      }

      public static boolean disbandAlliance(int allianceid) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         return mga != null && mga.disband();
      }

      public static boolean addGuildToAlliance(int allianceid, int gid) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         return mga != null && mga.addGuild(gid);
      }

      public static boolean removeGuildFromAlliance(int allianceid, int gid, boolean expelled) {
         MapleGuildAlliance mga = getAlliance(allianceid);
         return mga != null && mga.removeGuild(gid, expelled);
      }

      public static void sendGuild(int allianceid) {
         MapleGuildAlliance alliance = getAlliance(allianceid);
         if (alliance != null) {
            sendGuild(CWvsContext.AlliancePacket.getAllianceUpdate(alliance), -1, allianceid);
            sendGuild(CWvsContext.AlliancePacket.getGuildAlliance(alliance), -1, allianceid);
         }

      }

      public static void sendGuild(byte[] packet, int exceptionId, int allianceid) {
         MapleGuildAlliance alliance = getAlliance(allianceid);
         if (alliance != null) {
            for(int i = 0; i < alliance.getNoGuilds(); ++i) {
               int gid = alliance.getGuildId(i);
               if (gid > 0 && gid != exceptionId) {
                  World.Guild.guildPacket(gid, packet);
               }
            }
         }

      }

      public static boolean createAlliance(String alliancename, int cid, int cid2, int gid, int gid2) {
         int allianceid = MapleGuildAlliance.createToDb(cid, alliancename, gid, gid2);
         if (allianceid <= 0) {
            return false;
         } else {
            MapleGuild g = World.Guild.getGuild(gid);
            MapleGuild g_ = World.Guild.getGuild(gid2);
            g.setAllianceId(allianceid);
            g_.setAllianceId(allianceid);
            g.changeARank(true);
            g_.changeARank(false);
            MapleGuildAlliance alliance = getAlliance(allianceid);
            sendGuild(CWvsContext.AlliancePacket.createGuildAlliance(alliance), -1, allianceid);
            sendGuild(CWvsContext.AlliancePacket.getAllianceInfo(alliance), -1, allianceid);
            sendGuild(CWvsContext.AlliancePacket.getGuildAlliance(alliance), -1, allianceid);
            sendGuild(CWvsContext.AlliancePacket.changeAlliance(alliance, true), -1, allianceid);
            return true;
         }
      }

      public static void allianceChat(MapleCharacter player, String msg, LittleEndianAccessor slea, RecvPacketOpcode recv) {
         MapleGuild g = World.Guild.getGuild(player.getGuildId());
         if (g != null) {
            MapleGuildAlliance ga = getAlliance(g.getAllianceId());
            if (ga != null) {
               for(int i = 0; i < ga.getNoGuilds(); ++i) {
                  MapleGuild g_ = World.Guild.getGuild(ga.getGuildId(i));
                  if (g_ != null) {
                     g_.allianceChat(player, msg, slea, recv);
                     if (i == 0) {
                        DBLogger.getInstance().logChat(LogType.Chat.Guild, player.getId(), player.getName(), msg, "[" + ga.getName() + " - 연합 ]");
                     }
                  }
               }
            }
         }

      }

      public static void setNewAlliance(int gid, int allianceid) {
         MapleGuildAlliance alliance = getAlliance(allianceid);
         MapleGuild guild = World.Guild.getGuild(gid);
         if (alliance != null && guild != null) {
            for(int i = 0; i < alliance.getNoGuilds(); ++i) {
               if (gid == alliance.getGuildId(i)) {
                  guild.setAllianceId(allianceid);
                  guild.broadcast(CWvsContext.AlliancePacket.getAllianceInfo(alliance));
                  guild.broadcast(CWvsContext.AlliancePacket.getGuildAlliance(alliance));
                  guild.broadcast(CWvsContext.AlliancePacket.changeAlliance(alliance, true));
                  guild.changeARank();
                  guild.writeToDB(false);
               } else {
                  MapleGuild g_ = World.Guild.getGuild(alliance.getGuildId(i));
                  if (g_ != null) {
                     g_.broadcast(CWvsContext.AlliancePacket.addGuildToAlliance(alliance, guild));
                     g_.broadcast(CWvsContext.AlliancePacket.changeGuildInAlliance(alliance, guild, true));
                  }
               }
            }
         }

      }

      public static void setOldAlliance(int gid, boolean expelled, int allianceid) {
         MapleGuildAlliance alliance = getAlliance(allianceid);
         MapleGuild g_ = World.Guild.getGuild(gid);
         if (alliance != null) {
            for(int i = 0; i < alliance.getNoGuilds(); ++i) {
               MapleGuild guild = World.Guild.getGuild(alliance.getGuildId(i));
               if (guild == null) {
                  if (gid != alliance.getGuildId(i)) {
                     alliance.removeGuild(gid, false, true);
                  }
               } else if (g_ != null && gid != alliance.getGuildId(i)) {
                  if (g_ != null) {
                     guild.broadcast(CWvsContext.serverNotice(5, "", "[" + g_.getName() + "] Guild has left the alliance."));
                     guild.broadcast(CWvsContext.AlliancePacket.changeGuildInAlliance(alliance, g_, false));
                     guild.broadcast(CWvsContext.AlliancePacket.removeGuildFromAlliance(alliance, g_, expelled));
                  }
               } else {
                  guild.changeARank(5);
                  guild.setAllianceId(0);
                  guild.broadcast(CWvsContext.AlliancePacket.disbandAlliance(allianceid));
               }
            }
         }

         if (gid == -1) {
            alliances.remove(allianceid);
         }

      }

      public static List<byte[]> getAllianceInfo(int allianceid, boolean start) {
         List<byte[]> ret = new ArrayList();
         MapleGuildAlliance alliance = getAlliance(allianceid);
         if (alliance != null) {
            if (start) {
               ret.add(CWvsContext.AlliancePacket.getAllianceInfo(alliance));
               ret.add(CWvsContext.AlliancePacket.getGuildAlliance(alliance));
            }

            ret.add(CWvsContext.AlliancePacket.getAllianceUpdate(alliance));
         }

         return ret;
      }

      public static void save() {
         System.out.println("Saving alliances...");
         Iterator var0 = alliances.values().iterator();

         while(var0.hasNext()) {
            MapleGuildAlliance a = (MapleGuildAlliance)var0.next();
            a.saveToDb();
         }

      }

      static {
         Collection<MapleGuildAlliance> allGuilds = MapleGuildAlliance.loadAll();
         Iterator var1 = allGuilds.iterator();

         while(var1.hasNext()) {
            MapleGuildAlliance g = (MapleGuildAlliance)var1.next();
            alliances.put(g.getId(), g);
         }

      }
   }

   public static class Broadcast {
      public static long chatDelay = 0L;

      public static void broadcastSmega(byte[] message) {
         Iterator var1 = ChannelServer.getAllInstances().iterator();

         while(var1.hasNext()) {
            ChannelServer cs = (ChannelServer)var1.next();
            cs.broadcastSmega(message);
         }

      }

      public static void broadcastGMMessage(byte[] message) {
         Iterator var1 = ChannelServer.getAllInstances().iterator();

         while(var1.hasNext()) {
            ChannelServer cs = (ChannelServer)var1.next();
            cs.broadcastGMMessage(message);
         }

      }

      public static void broadcastMessage(byte[] message) {
         Iterator var1 = ChannelServer.getAllInstances().iterator();

         while(var1.hasNext()) {
            ChannelServer cs = (ChannelServer)var1.next();
            cs.broadcastMessage(message);
         }

      }

      public static void sendPacket(List<Integer> targetIds, byte[] packet, int exception) {
         Iterator var3 = targetIds.iterator();

         while(var3.hasNext()) {
            int i = (Integer)var3.next();
            if (i != exception) {
               int ch = World.Find.findChannel(i);
               if (ch >= 0) {
                  MapleCharacter c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(i);
                  if (c != null) {
                     c.getClient().getSession().writeAndFlush(packet);
                  }
               }
            }
         }

      }

      public static void sendPacket(int targetId, byte[] packet) {
         int ch = World.Find.findChannel(targetId);
         if (ch >= 0) {
            MapleCharacter c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(targetId);
            if (c != null) {
               c.getClient().getSession().writeAndFlush(packet);
            }

         }
      }

      public static void sendGuildPacket(int targetIds, byte[] packet, int exception, int guildid) {
         if (targetIds != exception) {
            int ch = World.Find.findChannel(targetIds);
            if (ch >= 0) {
               MapleCharacter c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(targetIds);
               if (c != null && c.getGuildId() == guildid) {
                  c.getClient().getSession().writeAndFlush(packet);
               }

            }
         }
      }
   }

   public static class Guild {
      private static final Map<Integer, MapleGuild> guilds = new ConcurrentHashMap();

      public static void addLoadedGuild(MapleGuild f) {
         if (f.isProper()) {
            guilds.put(f.getId(), f);
         }

      }

      public static int createGuild(int leaderId, String name) {
         return MapleGuild.createGuild(leaderId, name);
      }

      public static Collection<MapleGuild> getGuilds() {
         return guilds.values();
      }

      public static MapleGuild getGuild(int id) {
         MapleGuild ret = (MapleGuild)guilds.get(id);
         if (ret == null) {
            ret = new MapleGuild(id);
            if (ret == null || ret.getId() <= 0 || !ret.isProper()) {
               return null;
            }

            guilds.put(id, ret);
         }

         return ret;
      }

      public static List<MapleGuild> getGuildsByName(String name) {
         List<MapleGuild> ret = new ArrayList();
         Iterator var2 = guilds.values().iterator();

         while(var2.hasNext()) {
            MapleGuild g = (MapleGuild)var2.next();
            if (g.getName().matches(name)) {
               ret.add(g);
            }
         }

         return ret;
      }

      public static List<MapleGuild> getGuildsByName(String name, boolean option, byte type) {
         List<MapleGuild> ret = new ArrayList();
         Iterator var4 = guilds.values().iterator();

         while(var4.hasNext()) {
            MapleGuild g = (MapleGuild)var4.next();
            if (option) {
               if (type == 1) {
                  if (g.getName().contains(name)) {
                     ret.add(g);
                  }
               } else if (type == 2) {
                  if (g.getLeaderName().contains(name)) {
                     ret.add(g);
                  }
               } else if (type == 3) {
                  if (g.getName().matches(name)) {
                     ret.add(g);
                  }

                  if (g.getLeaderName().matches(name)) {
                     ret.add(g);
                  }
               }
            } else if (type == 1) {
               if (g.getName().contains(name)) {
                  ret.add(g);
               }
            } else if (type == 2) {
               if (g.getLeaderName().contains(name)) {
                  ret.add(g);
               }
            } else if (type == 3) {
               if (g.getName().contains(name)) {
                  ret.add(g);
               }

               if (g.getLeaderName().contains(name)) {
                  ret.add(g);
               }
            }
         }

         return ret;
      }

      public static List<MapleGuild> getGuilds(int minlevel, int maxlevel, int minsize, int maxsize, int minmlevel, int maxmlevel) {
         List<MapleGuild> ret = new ArrayList();
         Iterator var7 = guilds.values().iterator();

         while(var7.hasNext()) {
            MapleGuild guild = (MapleGuild)var7.next();
            if (guild.getLevel() >= minlevel && guild.getLevel() <= maxlevel && guild.getMembers().size() >= minsize && guild.getMembers().size() <= maxsize && guild.getLevel() >= minmlevel && guild.getLevel() <= maxmlevel) {
               ret.add(guild);
            }
         }

         return ret;
      }

      public static MapleGuild getGuildByName(String guildName) {
         Iterator var1 = guilds.values().iterator();

         MapleGuild g;
         do {
            if (!var1.hasNext()) {
               return null;
            }

            g = (MapleGuild)var1.next();
         } while(!g.getName().equalsIgnoreCase(guildName));

         return g;
      }

      public static MapleGuild getGuild(MapleCharacter mc) {
         return getGuild(mc.getGuildId());
      }

      public static void setGuildMemberOnline(MapleGuildCharacter mc, boolean bOnline, int channel) {
         MapleGuild g = getGuild(mc.getGuildId());
         if (g != null) {
            g.setOnline(mc.getId(), bOnline, channel);
         }

      }

      public static void guildPacket(int gid, byte[] message) {
         MapleGuild g = getGuild(gid);
         if (g != null) {
            g.broadcast(message);
         }

      }

      public static int addGuildMember(MapleGuildCharacter mc) {
         MapleGuild g = getGuild(mc.getGuildId());
         return g != null ? g.addGuildMember(mc) : 0;
      }

      public static void leaveGuild(MapleGuildCharacter mc) {
         MapleGuild g = getGuild(mc.getGuildId());
         if (g != null) {
            g.leaveGuild(mc);
         }

      }

      public static void guildChat(MapleCharacter chr, String msg, LittleEndianAccessor slea, RecvPacketOpcode recv) {
         MapleGuild g = getGuild(chr.getGuildId());
         if (g != null) {
            g.guildChat(chr, msg, slea, recv);
         }

      }

      public static void changeRank(int gid, int cid, int newRank) {
         MapleGuild g = getGuild(gid);
         if (g != null) {
            g.changeRank(cid, newRank);
         }

      }

      public static void expelMember(MapleGuildCharacter initiator, String name, int cid) {
         MapleGuild g = getGuild(initiator.getGuildId());
         if (g != null) {
            g.expelMember(initiator, name, cid);
         }

      }

      public static void setGuildNotice(MapleCharacter chr, String notice) {
         MapleGuild g = getGuild(chr.getGuildId());
         if (g != null) {
            g.setGuildNotice(chr, notice);
         }

      }

      public static void setGuildLeader(int gid, int cid) {
         MapleGuild g = getGuild(gid);
         if (g != null) {
            g.changeGuildLeader(cid);
         }

      }

      public static int getSkillLevel(int gid, int sid) {
         MapleGuild g = getGuild(gid);
         return g != null ? g.getSkillLevel(sid) : 0;
      }

      public static boolean purchaseSkill(int gid, int sid, String name, int cid) {
         MapleGuild g = getGuild(gid);
         return g != null ? g.purchaseSkill(sid, name, cid) : false;
      }

      public static boolean activateSkill(int gid, int sid, String name) {
         MapleGuild g = getGuild(gid);
         return g != null ? g.activateSkill(sid, name) : false;
      }

      public static void memberLevelJobUpdate(MapleGuildCharacter mc) {
         MapleGuild g = getGuild(mc.getGuildId());
         if (g != null) {
            g.memberLevelJobUpdate(mc);
         }

      }

      public static void changeRankTitle(MapleCharacter chr, String[] ranks) {
         MapleGuild g = getGuild(chr.getGuildId());
         if (g != null) {
            g.changeRankTitle(chr, ranks);
         }

      }

      public static void changeRankRole(MapleCharacter chr, int[] roles) {
         MapleGuild g = getGuild(chr.getGuildId());
         if (g != null) {
            g.changeRankRole(chr, roles);
         }

      }

      public static void changeRankTitleRole(MapleCharacter chr, String ranks, int roles, byte type) {
         MapleGuild g = getGuild(chr.getGuildId());
         if (g != null) {
            g.changeRankTitleRole(chr, ranks, roles, type);
         }

      }

      public static void setGuildEmblem(MapleCharacter chr, short bg, byte bgcolor, short logo, byte logocolor) {
         MapleGuild g = getGuild(chr.getGuildId());
         if (g != null) {
            g.setGuildEmblem(chr, bg, bgcolor, logo, logocolor);
         }

      }

      public static void setGuildCustomEmblem(MapleCharacter chr, byte[] imgdata) {
         MapleGuild g = getGuild(chr.getGuildId());
         if (g != null) {
            g.setGuildCustomEmblem(chr, imgdata);
         }

      }

      public static void disbandGuild(int gid) {
         MapleGuild g = getGuild(gid);
         if (g != null) {
            g.disbandGuild();
            guilds.remove(gid);
         }

      }

      public static void deleteGuildCharacter(int guildid, int charid) {
         MapleGuild g = getGuild(guildid);
         if (g != null) {
            MapleGuildCharacter mc = g.getMGC(charid);
            if (mc != null) {
               if (mc.getGuildRank() > 1) {
                  g.leaveGuild(mc);
               } else {
                  g.disbandGuild();
               }
            }
         }

      }

      public static boolean increaseGuildCapacity(int gid, boolean b) {
         MapleGuild g = getGuild(gid);
         return g != null ? g.increaseCapacity(b) : false;
      }

      public static void gainContribution(int gid, int amount) {
         MapleGuild g = getGuild(gid);
         if (g != null) {
            g.gainGP(amount);
         }

      }

      public static void gainContribution(int gid, int amount, int cid) {
         MapleGuild g = getGuild(gid);
         if (g != null) {
            g.gainGP(amount, true, cid);
         }

      }

      public static int getGP(int gid) {
         MapleGuild g = getGuild(gid);
         return g != null ? g.getGP() : 0;
      }

      public static int getInvitedId(int gid) {
         MapleGuild g = getGuild(gid);
         return g != null ? g.getInvitedId() : 0;
      }

      public static void setInvitedId(int gid, int inviteid) {
         MapleGuild g = getGuild(gid);
         if (g != null) {
            g.setInvitedId(inviteid);
         }

      }

      public static int getGuildLeader(int guildName) {
         MapleGuild mga = getGuild(guildName);
         return mga != null ? mga.getLeaderId() : 0;
      }

      public static int getGuildLeader(String guildName) {
         MapleGuild mga = getGuildByName(guildName);
         return mga != null ? mga.getLeaderId() : 0;
      }

      public static void save() {
         System.out.println("Saving guilds...");
         Iterator var0 = guilds.values().iterator();

         while(var0.hasNext()) {
            MapleGuild a = (MapleGuild)var0.next();
            a.writeToDB(false);
         }

      }

      public static void load() {
         System.out.println("Load guilds...");
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM guilds");
            rs = ps.executeQuery();

            while(rs.next()) {
               getGuild(rs.getInt("guildid"));
            }

            rs.close();
            ps.close();
         } catch (SQLException var20) {
            var20.printStackTrace();
         } finally {
            if (ps != null) {
               try {
                  ps.close();
               } catch (SQLException var19) {
                  Logger.getLogger(World.class.getName()).log(Level.SEVERE, (String)null, var19);
               }
            }

            if (rs != null) {
               try {
                  rs.close();
               } catch (SQLException var18) {
                  Logger.getLogger(World.class.getName()).log(Level.SEVERE, (String)null, var18);
               }
            }

            if (con != null) {
               try {
                  con.close();
               } catch (SQLException var17) {
                  var17.printStackTrace();
               }
            }

         }

      }

      public static void changeEmblem(MapleCharacter chr, int affectedPlayers, MapleGuild mgs) {
         World.Broadcast.sendGuildPacket(affectedPlayers, CWvsContext.GuildPacket.guildEmblemChange(chr, (short)mgs.getLogoBG(), (byte)mgs.getLogoBGColor(), (short)mgs.getLogo(), (byte)mgs.getLogoColor()), -1, chr.getGuildId());
         setGuildAndRank(affectedPlayers, -1, -1, -1, -1);
      }

      public static void setGuildAndRank(int cid, int guildid, int rank, int contribution, int alliancerank) {
         int ch = World.Find.findChannel(cid);
         if (ch != -1) {
            MapleCharacter mc = World.getStorage(ch).getCharacterById(cid);
            if (mc != null) {
               boolean bDifferentGuild;
               if (guildid == -1 && rank == -1) {
                  bDifferentGuild = true;
               } else {
                  bDifferentGuild = guildid != mc.getGuildId();
                  mc.setGuildId(guildid);
                  mc.setGuildRank((byte)rank);
                  mc.setGuildContribution(contribution);
                  mc.setAllianceRank((byte)alliancerank);
                  mc.saveGuildStatus();
               }

               if (bDifferentGuild && ch > 0) {
                  mc.getMap().broadcastMessage(mc, CField.loadGuildIcon(mc), false);
               }

            }
         }
      }
   }

   public static class Buddy {
      public static void buddyChat(int[] recipientCharacterIds, MapleCharacter player, String chattext, LittleEndianAccessor slea, RecvPacketOpcode recv) {
         String targets = "";
         Item item = null;
         if (recv == RecvPacketOpcode.PARTYCHATITEM && player != null) {
            byte invType = (byte)slea.readInt();
            byte pos = (byte)slea.readInt();
            item = player.getInventory(MapleInventoryType.getByType(pos > 0 ? invType : -1)).getItem((short)pos);
         }

         int[] var13 = recipientCharacterIds;
         int var14 = recipientCharacterIds.length;

         for(int var9 = 0; var9 < var14; ++var9) {
            int characterId = var13[var9];
            int ch = World.Find.findChannel(characterId);
            if (ch > 0) {
               MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(characterId);
               if (chr != null && player != null && chr.getBuddylist().containsVisible(player.getAccountID())) {
                  targets = targets + chr.getName() + ", ";
                  chr.getClient().getSession().writeAndFlush(CField.multiChat(player, chattext, 0, item));
                  if (chr.getClient().isMonitored()) {
                  }
               }
            }
         }

         DBLogger.getInstance().logChat(LogType.Chat.Buddy, player.getId(), player.getName(), chattext, "수신 : " + targets);
      }

      public static void updateBuddies(String name, int characterId, int channel, int[] buddies, int accId, boolean offline) {
         int[] var6 = buddies;
         int var7 = buddies.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            int buddy = var6[var8];
            int ch = World.Find.findAccChannel(buddy);
            if (ch > 0) {
               MapleClient c = ChannelServer.getInstance(ch).getPlayerStorage().getClientById(buddy);
               if (c != null && c.getPlayer() != null) {
                  BuddylistEntry ble = c.getPlayer().getBuddylist().get(accId);
                  if (ble != null && ble.isVisible()) {
                     int mcChannel;
                     if (offline) {
                        ble.setChannel(-1);
                        mcChannel = -1;
                     } else {
                        ble.setChannel(channel);
                        mcChannel = channel - 1;
                     }

                     ble.setName(name);
                     ble.setCharacterId(characterId);
                     c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.updateBuddyChannel(ble.getCharacterId(), accId, mcChannel, name));
                  }
               }
            }
         }

      }

      public static void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyList.BuddyOperation operation, int level, int job, int accId, String memo) {
         int ch = World.Find.findChannel(cid);
         if (ch > 0) {
            MapleCharacter addChar = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(cid);
            if (addChar != null) {
               BuddyList buddylist = addChar.getBuddylist();
               switch(operation) {
               case ADDED:
                  if (buddylist.contains(accId)) {
                     buddylist.put(new BuddylistEntry(name, name, accId, cidFrom, "그룹 미지정", channel, true, level, job, memo));
                     addChar.getClient().getSession().writeAndFlush(CWvsContext.BuddylistPacket.updateBuddyChannel(cidFrom, accId, channel, buddylist.get(accId).getName()));
                  }
                  break;
               case DELETED:
                  if (buddylist.contains(accId)) {
                     buddylist.put(new BuddylistEntry(name, name, accId, cidFrom, "그룹 미지정", -1, buddylist.get(accId).isVisible(), level, job, memo));
                     addChar.getClient().getSession().writeAndFlush(CWvsContext.BuddylistPacket.updateBuddyChannel(cidFrom, accId, -1, buddylist.get(accId).getName()));
                  }
               }
            }
         }

      }

      public static BuddyList.BuddyAddResult requestBuddyAdd(String addName, int accid, int channelFrom, int cidFrom, String nameFrom, int levelFrom, int jobFrom, String groupName, String memo) {
         Iterator var9 = ChannelServer.getAllInstances().iterator();

         while(var9.hasNext()) {
            ChannelServer server = (ChannelServer)var9.next();
            MapleCharacter addChar = server.getPlayerStorage().getCharacterByName(addName);
            if (addChar != null) {
               BuddyList buddylist = addChar.getBuddylist();
               if (buddylist.isFull()) {
                  return BuddyList.BuddyAddResult.BUDDYLIST_FULL;
               }

               if (!buddylist.contains(accid)) {
                  buddylist.addBuddyRequest(addChar.getClient(), accid, cidFrom, nameFrom, nameFrom, channelFrom, levelFrom, jobFrom, groupName, memo);
               } else if (buddylist.containsVisible(accid)) {
                  return BuddyList.BuddyAddResult.ALREADY_ON_LIST;
               }
            }
         }

         return BuddyList.BuddyAddResult.OK;
      }

      public static void loggedOn(String name, int characterId, int channel, int accId, int[] buddies) {
         updateBuddies(name, characterId, channel, buddies, accId, false);
      }

      public static void loggedOff(String name, int characterId, int channel, int accId, int[] buddies) {
         updateBuddies(name, characterId, channel, buddies, accId, true);
      }
   }
}
