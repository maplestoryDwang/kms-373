package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.messages.CommandProcessor;
import constants.ServerConstants;
import constants.programs.AdminTool;
import discord.ChatListener;
import handling.RecvPacketOpcode;
import handling.channel.ChannelServer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.World;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import log.DBLogger;
import log.LogType;
import server.MapleItemInformationProvider;
import tools.FileoutputUtil;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class ChatHandler {
   public static List<ChatListener> listeners = new ArrayList();

   public static final void GeneralChat(String text, byte unk, MapleClient c, MapleCharacter chr, LittleEndianAccessor slea, RecvPacketOpcode recv) {
      if (text.length() > 0 && chr != null && chr.getMap() != null && !CommandProcessor.processCommand(c, text, ServerConstants.CommandType.NORMAL)) {
         if (!chr.isIntern() && text.length() >= 80) {
            return;
         }

         int color = 8;
         Item medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-21);
         int[] medalArray = new int[]{1149990, 1149991, 1149992, 1149993, 1149994, 1149995, 1149996, 1149997, 1149998, 1149999};
         int[] colorArray = new int[]{5, 6, 7, 9, 4, 11, 12, 13, 15, 16};
         String medalName = "";
         if (medal != null) {
            MapleItemInformationProvider.getInstance().getName(medal.getItemId()).makeConcatWithConstants<invokedynamic>(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
         }

         Item item = null;
         if (recv == RecvPacketOpcode.GENERAL_CHAT_ITEM) {
            slea.readInt();
            byte invType = (byte)slea.readInt();
            short pos = (short)slea.readInt();
            item = c.getPlayer().getInventory(MapleInventoryType.getByType(pos > 0 ? invType : -1)).getItem(pos);
         }

         if (c.getPlayer().getV("chatReq") != null) {
            int i = Integer.parseInt(c.getPlayer().getV("chatReq")) + 1;
            c.getPlayer().addKV("chatReq", i.makeConcatWithConstants<invokedynamic>(i));
         } else {
            c.getPlayer().addKV("chatReq", "1");
         }

         StringBuilder sb = new StringBuilder();
         InventoryHandler.addMedalString(c.getPlayer(), sb);
         sb.append(c.getPlayer().getName());
         sb.append(" : ");
         sb.append(text.substring(1));
         if (c.getChatBlockedTime() == 0L) {
            if (chr.isHidden()) {
               if (chr.isIntern() && !chr.isSuperGM() && unk == 0) {
                  chr.getMap().broadcastGMMessage(chr, CField.getChatText(chr, text, false, 1, item), true);
                  if (unk == 0) {
                     World.Broadcast.broadcastSmega(CWvsContext.HyperMegaPhone(sb.toString(), c.getPlayer().getName(), sb.toString(), c.getChannel(), true, (Item)null));
                  }
               } else {
                  chr.getMap().broadcastGMMessage(chr, CField.getChatText(chr, text, c.getPlayer().isSuperGM(), unk, item), true);
               }
            } else {
               String var10000;
               int var10001;
               if (chr.isIntern() && !chr.isSuperGM() && unk == 0) {
                  if (unk != 0 && !text.startsWith("~")) {
                     var10001 = chr.getClient().getChannel();
                     AdminTool.addMessage(0, "[" + var10001 + "채널] " + chr.getName() + " : " + text.replaceAll("~", ""));
                     var10000 = FileoutputUtil.일반채팅로그;
                     var10001 = chr.getClient().getChannel();
                     FileoutputUtil.log(var10000, "[일반] [" + var10001 + "채널] " + chr.getName() + " : " + text);
                     chr.getMap().broadcastMessage(CField.getChatText(chr, text, false, 1, item), c.getPlayer().getTruePosition());
                  } else if (chr.getHgrade() >= 1) {
                     if (item != null) {
                        if (chr.getMeso() >= 5000000L) {
                           chr.gainMeso(-5000000L, false);
                           World.Broadcast.broadcastSmega(CWvsContext.HyperMegaPhone(sb.toString(), c.getPlayer().getName(), sb.toString(), c.getChannel(), true, item));
                        } else {
                           chr.dropMessage(1, "500만 메소가 필요합니다.");
                        }
                     } else {
                        var10001 = chr.getClient().getChannel();
                        AdminTool.addMessage(1, "[" + var10001 + "채널] " + chr.getName() + " : " + text.replaceAll("~", ""));
                        var10000 = FileoutputUtil.전체채팅로그;
                        var10001 = chr.getClient().getChannel();
                        FileoutputUtil.log(var10000, "[전체] [" + var10001 + "채널] " + chr.getName() + " : " + text.replaceAll("~", ""));
                        World.Broadcast.broadcastSmega(CField.getGameMessage(24, sb.toString()));
                     }
                  } else if (item != null) {
                     if (chr.getMeso() >= 5000000L) {
                        chr.gainMeso(-5000000L, false);
                        World.Broadcast.broadcastSmega(CWvsContext.HyperMegaPhone(sb.toString(), c.getPlayer().getName(), sb.toString(), c.getChannel(), true, item));
                        var10001 = chr.getClient().getChannel();
                        AdminTool.addMessage(1, "[" + var10001 + "채널] " + chr.getName() + " : [" + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "]" + text.replaceAll("~", ""));
                     } else {
                        chr.dropMessage(1, "500만 메소가 필요합니다.");
                     }
                  } else {
                     World.Broadcast.broadcastSmega(CField.getGameMessage(18, sb.toString()));
                  }
               } else if (!text.startsWith("~")) {
                  var10001 = chr.getClient().getChannel();
                  AdminTool.addMessage(0, "[" + var10001 + "채널] " + chr.getName() + " : " + text.replaceAll("~", ""));
                  var10000 = FileoutputUtil.일반채팅로그;
                  var10001 = chr.getClient().getChannel();
                  FileoutputUtil.log(var10000, "[일반] [" + var10001 + "채널] " + chr.getName() + " : " + text);
                  chr.getMap().broadcastMessage(CField.getChatText(chr, text, c.getPlayer().isSuperGM(), unk, item), c.getPlayer().getTruePosition());
               } else {
                  if (chr.getHgrade() >= 1) {
                     if (item != null) {
                        if (chr.getMeso() >= 5000000L) {
                           chr.gainMeso(-5000000L, false);
                           World.Broadcast.broadcastSmega(CWvsContext.HyperMegaPhone(sb.toString(), c.getPlayer().getName(), sb.toString(), c.getChannel(), true, item));
                        } else {
                           chr.dropMessage(1, "500만 메소가 필요합니다.");
                        }
                     } else {
                        for(int i = 0; i < medalArray.length; ++i) {
                           if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(medalArray[i]) != null) {
                              color = colorArray[i];
                              break;
                           }

                           if (c.getPlayer().isGM()) {
                              color = 10;
                              break;
                           }

                           color = 24;
                        }

                        World.Broadcast.broadcastSmega(CField.getGameMessage(color, sb.toString()));
                     }
                  } else if (item != null) {
                     if (chr.getMeso() >= 5000000L) {
                        chr.gainMeso(-5000000L, false);
                        World.Broadcast.broadcastSmega(CWvsContext.HyperMegaPhone(sb.toString(), c.getPlayer().getName(), sb.toString(), c.getChannel(), true, item));
                        var10001 = chr.getClient().getChannel();
                        AdminTool.addMessage(1, "[" + var10001 + "채널] " + chr.getName() + " : [" + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "]" + text.replaceAll("~", ""));
                        var10000 = FileoutputUtil.전체채팅로그;
                        var10001 = chr.getClient().getChannel();
                        FileoutputUtil.log(var10000, "[전체] [" + var10001 + "채널] " + chr.getName() + " : " + text.replaceAll("~", ""));
                     } else {
                        chr.dropMessage(1, "500만 메소가 필요합니다.");
                     }
                  } else if (chr.getLevel() > 1) {
                     World.Broadcast.broadcastSmega(CField.getGameMessage(24, sb.toString()));
                     var10001 = chr.getClient().getChannel();
                     AdminTool.addMessage(1, "[" + var10001 + "채널] " + chr.getName() + " : " + text.replaceAll("~", ""));
                     var10000 = FileoutputUtil.전체채팅로그;
                     var10001 = chr.getClient().getChannel();
                     FileoutputUtil.log(var10000, "[전체] [" + var10001 + "채널] " + chr.getName() + " : " + text.replaceAll("~", ""));
                  } else {
                     chr.dropMessage(6, "전체채팅을 이용하실 수 없어용 뇽홍홍~");
                  }

                  Iterator var18 = listeners.iterator();

                  while(var18.hasNext()) {
                     ChatListener listener = (ChatListener)var18.next();
                     String var21 = chr.getName() + "#CH." + chr.getClient().getChannel();
                     String var10002 = chr.getName();
                     listener.someoneWroteAMessage(var21, var10002 + " : " + text.replaceAll("~", ""));
                  }
               }
            }

            DBLogger var19 = DBLogger.getInstance();
            LogType.Chat var22 = LogType.Chat.General;
            int var20 = c.getPlayer().getId();
            String var10003 = c.getPlayer().getName();
            String var10005 = c.getPlayer().getMap().getStreetName();
            var19.logChat(var22, var20, var10003, text, var10005 + " - " + c.getPlayer().getMap().getMapName() + " (" + c.getPlayer().getMap().getId() + ")");
         } else {
            c.getSession().writeAndFlush(CWvsContext.serverNotice(6, "", "대화 금지 상태이므로 채팅이 불가능합니다."));
         }
      }

   }

   public static final void Others(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr, RecvPacketOpcode recv) {
      int type = slea.readByte();
      byte numRecipients = slea.readByte();
      slea.skip(1);
      if (numRecipients > 0) {
         int[] recipients = new int[numRecipients];

         for(byte i = 0; i < numRecipients; ++i) {
            recipients[i] = slea.readInt();
         }

         String chattext = slea.readMapleAsciiString();
         if (chr != null && chr.getCanTalk()) {
            if (c.isMonitored()) {
               String chattype = "Unknown";
               switch(type) {
               case 0:
                  chattype = "Buddy";
                  break;
               case 1:
                  chattype = "Party";
                  break;
               case 2:
                  chattype = "Guild";
                  break;
               case 3:
                  chattype = "Alliance";
                  break;
               case 4:
                  chattype = "Expedition";
               }
            }

            if (!chattext.equals("Unknown") && !CommandProcessor.processCommand(c, chattext, ServerConstants.CommandType.NORMAL)) {
               String var10000;
               int var10001;
               switch(type) {
               case 0:
                  World.Buddy.buddyChat(recipients, chr, chattext, slea, recv);
                  var10001 = c.getChannel();
                  AdminTool.addMessage(2, "[" + var10001 + "채널] " + c.getPlayer().getName() + " : " + chattext);
                  var10000 = FileoutputUtil.친구채팅로그;
                  var10001 = c.getChannel();
                  FileoutputUtil.log(var10000, "[친구] [" + var10001 + "채널] " + c.getPlayer().getName() + " : " + chattext);
                  break;
               case 1:
                  if (chr.getParty() != null) {
                     World.Party.partyChat(chr, chattext, slea, recv);
                     var10001 = c.getChannel();
                     AdminTool.addMessage(4, "[" + var10001 + "채널] " + c.getPlayer().getName() + " : " + chattext);
                     var10000 = FileoutputUtil.파티채팅로그;
                     var10001 = c.getChannel();
                     FileoutputUtil.log(var10000, "[파티] [" + var10001 + "채널] 파티번호 : " + c.getPlayer().getParty().getId() + " | " + c.getPlayer().getName() + " : " + chattext);
                  }
                  break;
               case 2:
                  if (chr.getGuildId() > 0) {
                     World.Guild.guildChat(chr, chattext, slea, recv);
                     var10001 = c.getChannel();
                     AdminTool.addMessage(3, "[" + var10001 + "채널] " + c.getPlayer().getName() + " : " + chattext);
                     var10000 = FileoutputUtil.길드채팅로그;
                     var10001 = c.getChannel();
                     FileoutputUtil.log(var10000, "[길드] [" + var10001 + "채널] 길드 : " + c.getPlayer().getGuildName() + " | " + c.getPlayer().getName() + " : " + chattext);
                  }
                  break;
               case 3:
                  if (chr.getGuildId() > 0) {
                     World.Alliance.allianceChat(chr, chattext, slea, recv);
                     var10001 = c.getChannel();
                     AdminTool.addMessage(3, "[" + var10001 + "채널] " + c.getPlayer().getName() + " : " + chattext);
                     var10000 = FileoutputUtil.연합채팅로그;
                     var10001 = c.getChannel();
                     FileoutputUtil.log(var10000, "[연합] [" + var10001 + "채널] 길드 : " + c.getPlayer().getGuildName() + " | " + c.getPlayer().getName() + " : " + chattext);
                  }
               }

            }
         } else {
            c.getSession().writeAndFlush(CWvsContext.serverNotice(6, "", "You have been muted and are therefore unable to talk."));
         }
      }
   }

   public static void Messenger(LittleEndianAccessor slea, MapleClient c) {
      MapleMessenger messenger = c.getPlayer().getMessenger();
      String charname;
      String input;
      String chattext;
      MapleCharacter mapleCharacter;
      switch(slea.readByte()) {
      case 0:
         if (messenger == null) {
            byte available = slea.readByte();
            int messengerid = slea.readInt();
            if (messengerid == 0) {
               c.getPlayer().setMessenger(World.Messenger.createMessenger(new MapleMessengerCharacter(c.getPlayer())));
            } else {
               messenger = World.Messenger.getMessenger(messengerid);
               if (messenger != null) {
                  int position = messenger.getLowestPosition();
                  if (messenger.getMembers().size() < available) {
                     if (position > -1 && position < 7) {
                        c.getPlayer().setMessenger(messenger);
                        World.Messenger.joinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()), c.getPlayer().getName(), c.getChannel());
                     }
                  } else {
                     c.getPlayer().dropMessage(5, "이미 해당 메신저는 최대 인원 입니다.");
                  }
               }
            }
            break;
         }
      case 2:
         if (messenger != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
            World.Messenger.leaveMessenger(messenger.getId(), messengerplayer);
            c.getPlayer().setMessenger((MapleMessenger)null);
         }
      case 1:
      case 4:
      case 7:
      case 8:
      case 11:
      case 13:
      case 14:
      default:
         break;
      case 3:
         if (messenger != null) {
            int position = messenger.getLowestPosition();
            if (position <= -1 || position >= 7) {
               return;
            }

            input = slea.readMapleAsciiString();
            mapleCharacter = c.getChannelServer().getPlayerStorage().getCharacterByName(input);
            if (mapleCharacter != null) {
               if (mapleCharacter.getMessenger() == null) {
                  if (mapleCharacter.isIntern() && !c.getPlayer().isIntern()) {
                     c.getSession().writeAndFlush(CField.messengerNote(input, 4, 0));
                  } else {
                     c.getSession().writeAndFlush(CField.messengerNote(input, 4, 1));
                     mapleCharacter.getClient().getSession().writeAndFlush(CField.messengerInvite(c.getPlayer().getName(), messenger.getId()));
                  }
               } else {
                  c.getSession().writeAndFlush(CField.messengerChat(c.getPlayer().getName(), " : " + mapleCharacter.getName() + " is already using Maple Messenger."));
               }
            } else if (World.isConnected(input)) {
               World.Messenger.messengerInvite(c.getPlayer().getName(), messenger.getId(), input, c.getChannel(), c.getPlayer().isIntern());
            } else {
               c.getSession().writeAndFlush(CField.messengerNote(input, 4, 0));
            }
         }
         break;
      case 5:
         String targeted = slea.readMapleAsciiString();
         MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
         if (target != null) {
            if (target.getMessenger() != null) {
               target.getClient().getSession().writeAndFlush(CField.messengerNote(c.getPlayer().getName(), 5, 0));
            }
         } else if (!c.getPlayer().isIntern()) {
            World.Messenger.declineChat(targeted, c.getPlayer().getName());
         }
         break;
      case 6:
         if (messenger != null) {
            charname = slea.readMapleAsciiString();
            input = slea.readMapleAsciiString();
            if (!c.getPlayer().isIntern() && input.length() >= 1000) {
               return;
            }

            chattext = charname + input;
            World.Messenger.messengerChat(messenger.getId(), charname, input, c.getPlayer().getName());
            if (messenger.isMonitored() && chattext.length() > c.getPlayer().getName().length() + 3) {
            }

            int var10001 = c.getChannel();
            AdminTool.addMessage(5, "[" + var10001 + "채널] " + c.getPlayer().getName() + " : " + input);
            String var10000 = FileoutputUtil.메신저채팅로그;
            var10001 = c.getChannel();
            FileoutputUtil.log(var10000, "[메신저] [" + var10001 + "채널] 채팅방번호 : " + messenger.getId() + " | " + c.getPlayer().getName() + " : " + input);
         }
         break;
      case 9:
         if (messenger != null) {
            short like = slea.readShort();
            input = slea.readMapleAsciiString();
            mapleCharacter = c.getChannelServer().getPlayerStorage().getCharacterByName(input);
            c.getSession().writeAndFlush(CField.messengerCharInfo(mapleCharacter));
         }
         break;
      case 10:
         if (messenger != null) {
            slea.readByte();
            charname = slea.readMapleAsciiString();
            input = slea.readMapleAsciiString();
         }
         break;
      case 12:
         if (messenger != null) {
            charname = slea.readMapleAsciiString();
            MapleCharacter character = c.getChannelServer().getPlayerStorage().getCharacterByName(charname);
            c.getSession().writeAndFlush(CField.messengerCharInfo(character));
         }
         break;
      case 15:
         if (messenger != null) {
            charname = slea.readMapleAsciiString();
            input = slea.readMapleAsciiString();
            slea.readByte();
            if (!c.getPlayer().isIntern() && input.length() >= 1000) {
               return;
            }

            chattext = charname + input;
            World.Messenger.messengerWhisperChat(messenger.getId(), charname, input, c.getPlayer().getName());
            if (messenger.isMonitored() && chattext.length() > c.getPlayer().getName().length() + 3) {
            }
         }
      }

   }

   public static final void Whisper_Find(LittleEndianAccessor slea, MapleClient c, RecvPacketOpcode recv) {
      byte mode = slea.readByte();
      slea.readInt();
      String recipient;
      MapleCharacter player;
      switch(mode) {
      case 5:
      case 68:
         recipient = slea.readMapleAsciiString();
         player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
         if (player == null) {
            int ch = World.Find.findChannel(recipient);
            if (ch > 0) {
               player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
               if (player == null) {
                  break;
               }

               if (player != null) {
                  if (!player.isIntern() || c.getPlayer().isIntern() && player.isIntern()) {
                     c.getSession().writeAndFlush(CField.getFindReply(recipient, (byte)ch, mode == 68));
                  } else {
                     c.getSession().writeAndFlush(CField.getWhisperReply(recipient, (byte)0));
                  }

                  return;
               }
            }

            if (ch == -10) {
               c.getSession().writeAndFlush(CField.getFindReplyWithCS(recipient, mode == 68));
            } else {
               c.getSession().writeAndFlush(CField.getWhisperReply(recipient, (byte)0));
            }
         } else if (player.isIntern() && (!c.getPlayer().isIntern() || !player.isIntern())) {
            c.getSession().writeAndFlush(CField.getWhisperReply(recipient, (byte)0));
         } else {
            c.getSession().writeAndFlush(CField.getFindReplyWithMap(player.getName(), player.getMap().getId(), mode == 68));
         }
         break;
      case 6:
         if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return;
         }

         if (!c.getPlayer().getCanTalk()) {
            c.getSession().writeAndFlush(CWvsContext.serverNotice(6, "", "채팅 금지 상태입니다."));
            return;
         }

         recipient = slea.readMapleAsciiString();
         Item item = null;
         if (recv == RecvPacketOpcode.WHISPERITEM) {
            byte invType = (byte)slea.readInt();
            byte pos = (byte)slea.readInt();
            item = c.getPlayer().getInventory(MapleInventoryType.getByType(pos > 0 ? invType : -1)).getItem((short)pos);
         }

         String text = slea.readMapleAsciiString();
         int ch2 = World.Find.findChannel(recipient);
         String var10000;
         int var10001;
         if (ch2 <= 0) {
            c.getSession().writeAndFlush(CField.getWhisperReply(recipient, (byte)0));
            var10001 = c.getChannel();
            AdminTool.addMessage(6, "[" + var10001 + "채널] " + c.getPlayer().getName() + " > " + recipient + " : " + text);
            var10000 = FileoutputUtil.귓속말채팅로그;
            var10001 = c.getChannel();
            FileoutputUtil.log(var10000, "[귓속말] [" + var10001 + "채널] " + c.getPlayer().getName() + " > " + recipient + " : " + text);
         } else {
            MapleCharacter player2 = ChannelServer.getInstance(ch2).getPlayerStorage().getCharacterByName(recipient);
            if (player2 != null) {
               player2.getClient().getSession().writeAndFlush(CField.getWhisper(c.getPlayer().getName(), c.getChannel(), text, item));
               if (!c.getPlayer().isIntern() && player2.isIntern()) {
                  c.getSession().writeAndFlush(CField.getWhisperReply(recipient, (byte)0));
               } else {
                  c.getSession().writeAndFlush(CField.getWhisperReply(recipient, (byte)1));
               }

               var10001 = c.getChannel();
               AdminTool.addMessage(6, "[" + var10001 + "채널] " + c.getPlayer().getName() + " > " + recipient + " : " + text);
               var10000 = FileoutputUtil.귓속말채팅로그;
               var10001 = c.getChannel();
               FileoutputUtil.log(var10000, "[귓속말] [" + var10001 + "채널] " + c.getPlayer().getName() + " > " + recipient + " : " + text);
            }
         }
         break;
      case 34:
         recipient = slea.readMapleAsciiString();
         player = null;
         Iterator var6 = ChannelServer.getAllInstances().iterator();

         while(var6.hasNext()) {
            ChannelServer cserv = (ChannelServer)var6.next();
            player = cserv.getPlayerStorage().getCharacterByName(recipient);
            if (player != null) {
               break;
            }
         }

         if (player != null) {
            c.getSession().writeAndFlush(CField.getWhisperReply(c.getPlayer().getName(), (byte)34, (byte)0));
         }
      }

   }

   public static void Messengerserch(LittleEndianAccessor slea, MapleClient c) {
      List<MapleCharacter> chrs = new ArrayList();
      Iterator var3 = c.getPlayer().getMap().getAllCharactersThreadsafe().iterator();

      while(var3.hasNext()) {
         MapleCharacter mapchr = (MapleCharacter)var3.next();
         if (mapchr.getId() != c.getPlayer().getId()) {
            chrs.add(mapchr);
         }
      }

      c.getSession().writeAndFlush(CField.ChrlistMap(chrs));
   }

   public static void addListener(ChatListener toAdd) {
      listeners.add(toAdd);
   }
}
