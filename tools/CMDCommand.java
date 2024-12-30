package tools;

import client.MapleCharacter;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;
import handling.auction.AuctionServer;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import server.ShutdownServer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CWvsContext;

public class CMDCommand {
   private static CMDCommand instance = new CMDCommand();
   public static String target = "";
   private static int a;

   public static void main() {
      Scanner scan = new Scanner(System.in);

      while(true) {
         System.out.print("Input Command : ");

         try {
            target = scan.nextLine();
            String[] command = target.split(" ");
            command = target.split(" ");
            String var6 = command[0];
            byte var7 = -1;
            switch(var6.hashCode()) {
            case -642202653:
               if (var6.equals("경매장저장")) {
                  var7 = 4;
               }
               break;
            case -104317708:
               if (var6.equals("서버메세지")) {
                  var7 = 6;
               }
               break;
            case 3555:
               if (var6.equals("or")) {
                  var7 = 9;
               }
               break;
            case 1424235:
               if (var6.equals("공지")) {
                  var7 = 3;
               }
               break;
            case 1631105:
               if (var6.equals("임명")) {
                  var7 = 7;
               }
               break;
            case 1638631:
               if (var6.equals("종료")) {
                  var7 = 2;
               }
               break;
            case 1729167:
               if (var6.equals("패킷")) {
                  var7 = 8;
               }
               break;
            case 45850068:
               if (var6.equals("도움말")) {
                  var7 = 0;
               }
               break;
            case 281407225:
               if (var6.equals("프로파일링")) {
                  var7 = 1;
               }
               break;
            case 1469531535:
               if (var6.equals("모두종료")) {
                  var7 = 5;
               }
            }

            label180: {
               MapleCharacter chr;
               Iterator var15;
               ChannelServer cserv;
               Iterator var17;
               label181: {
                  label182: {
                     label169: {
                        CPUSampler sampler;
                        switch(var7) {
                        case 0:
                           System.out.println("<  CMD커맨드 도움말 >");
                           System.out.println("[명령어 목록] :: \r\n");
                           System.out.println("<공지> - 공지사항을 보냅니다.");
                           System.out.println("<모두종료> - 서버에 있는 유저들을 모두 종료시킵니다.");
                           System.out.println("<경매장저장> - 서버에 저장된 경매장 아이템을 모두 저장합니다.");
                           System.out.println("<고상저장> - 서버에 열려있는 고용상인을 모두 저장합니다.");
                           System.out.println("<임명> - 플레이어에게 GM권한 레벨을 부여합니다.");
                           System.out.println("<패킷> - 서버에 센드 패킷 스트링을 보냅니다.");
                           System.out.println("<OR> - 옵코드를 리로딩합니다.");
                           System.out.println("<동접> - 현재 서버에 접속중인 유저를 표시합니다.");
                        case 1:
                           sampler = CPUSampler.getInstance();
                           sampler.addIncluded("client");
                           sampler.addIncluded("connector");
                           sampler.addIncluded("constants");
                           sampler.addIncluded("database");
                           sampler.addIncluded("handling");
                           sampler.addIncluded("log");
                           sampler.addIncluded("provider");
                           sampler.addIncluded("scripting");
                           sampler.addIncluded("server");
                           sampler.addIncluded("tools");
                           sampler.start();
                        case 2:
                           sampler = CPUSampler.getInstance();

                           try {
                              String filename = "CPU프로파일링.txt";
                              if (command.length > 1) {
                                 filename = command[1];
                              }

                              File file = new File(filename);
                              if (file.exists()) {
                                 System.out.println("이미 존재하는 파일입니다. 삭제나 이름 변경을 해주세요.");
                                 continue;
                              }

                              sampler.stop();
                              FileWriter fw = new FileWriter(file);
                              sampler.save(fw, 1, 10);
                              fw.close();
                           } catch (IOException var13) {
                              System.err.println("Error saving profile" + var13);
                           }

                           sampler.reset();
                        case 3:
                           var15 = ChannelServer.getAllInstances().iterator();

                           while(var15.hasNext()) {
                              cserv = (ChannelServer)var15.next();
                              var17 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

                              while(var17.hasNext()) {
                                 chr = (MapleCharacter)var17.next();
                                 chr.dropMessage(1, "[공지사항]\r\n" + StringUtil.joinStringFrom(command, 1));
                              }
                           }
                        case 4:
                           AuctionServer.saveItems();
                        case 5:
                           break;
                        case 6:
                           break label169;
                        case 7:
                           break label182;
                        case 8:
                           break label181;
                        case 9:
                           break label180;
                        default:
                           continue;
                        }

                        Thread t = new Thread(ShutdownServer.getInstance());
                        ShutdownServer.getInstance().shutdown();
                        System.out.println("서버에 있는 유저들을 종료시켰습니다.");
                        t.start();
                     }

                     String outputMessage = command[1];
                     var15 = ChannelServer.getAllInstances().iterator();

                     while(var15.hasNext()) {
                        cserv = (ChannelServer)var15.next();
                        cserv.setServerMessage(outputMessage);
                     }
                  }

                  a = 0;
                  var15 = ChannelServer.getAllInstances().iterator();

                  while(var15.hasNext()) {
                     cserv = (ChannelServer)var15.next();
                     MapleCharacter player = null;
                     if (command[1] == null) {
                        System.out.println("캐릭터 이름을 입력해주세요.");
                     } else {
                        player = cserv.getPlayerStorage().getCharacterByName(command[1]);
                     }

                     if (player != null) {
                        String num = command[2];
                        byte number = num == null ? 6 : Byte.parseByte(num);
                        player.getClient().getSession().writeAndFlush(CWvsContext.getTopMsg("[알림] 해당 플레이어가 GM " + command[2] + "레벨이 되었습니다."));
                        System.out.println("[알림] " + command[1] + " 플레이어를 GM레벨 " + command[2] + "(으)로 설정하였습니다.");
                        player.setGMLevel(number);
                        a = 1;
                     } else if (player == null && a == 0) {
                        System.out.println("[알림] " + command[1] + " 플레이어를 찾지 못하였습니다.");
                        a = 1;
                     }
                  }
               }

               MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
               mplew.write(HexTool.getByteArrayFromHexString(StringUtil.joinStringFrom(command, 1)));
               var15 = ChannelServer.getAllInstances().iterator();

               while(var15.hasNext()) {
                  cserv = (ChannelServer)var15.next();
                  var17 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

                  while(var17.hasNext()) {
                     chr = (MapleCharacter)var17.next();
                     chr.getClient().getSession().writeAndFlush(mplew.getPacket());
                  }
               }

               var15 = CashShopServer.getPlayerStorage().getAllCharacters().values().iterator();

               while(var15.hasNext()) {
                  MapleCharacter chr = (MapleCharacter)var15.next();
                  if (chr.getName().equals("시온")) {
                     chr.getClient().getSession().writeAndFlush(mplew.getPacket());
                  }
               }

               System.out.println(StringUtil.joinStringFrom(command, 1));
            }

            RecvPacketOpcode.reloadValues();
            SendPacketOpcode.reloadValues();
            System.out.print("옵코드 재설정이 완료되었습니다.");
         } catch (NoSuchElementException var14) {
         }
      }
   }

   public static String converToDecimalFromHex(String hex) {
      String decimal = "";
      hex = hex.trim();

      for(int i = 0; i < hex.length(); i += 2) {
         String tmp = hex.substring(i, i + 2);
         long val = Long.parseLong(tmp, 16);
         decimal = decimal + val;
         decimal = decimal + ",";
      }

      return decimal;
   }
}
