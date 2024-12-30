package server.games;

import client.MapleCharacter;
import constants.GameConstants;
import handling.world.World;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.Timer;
import server.maps.MapleMap;
import tools.Pair;
import tools.packet.SLFCGPacket;

public class BingoGame {
   private Map<MapleCharacter, int[][]> players = new HashMap();
   private List<MapleCharacter> rank = new ArrayList();
   private List<Integer> hostednumbers = new ArrayList();
   private ScheduledFuture<?> BingoTimer = null;
   private MapleCharacter Owner = null;
   private int round = 1;
   private int MessageTime = 3;
   public static int point = 0;
   public static List<Pair<Integer, Integer>> items = new ArrayList();
   public static boolean isRunning = true;

   public BingoGame(MapleCharacter owner, boolean isByAdmin) {
      isRunning = true;
      this.Owner = owner;
      String channel = owner.getClient().getChannel() == 1 ? "1" : (owner.getClient().getChannel() == 2 ? "20세 이상" : String.valueOf(owner.getClient().getChannel() - 1));
      Iterator var4 = World.getAllCharacters().iterator();

      while(var4.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var4.next();
         if (isByAdmin) {
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3003301, 7000, "#face3#운영자가 빙고 게임 참여자를 모집 중이야! " + channel + "채널에서 @빙고입장을 통해 입장해봐~", ""));
         } else {
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3003301, 7000, "#face2##b#e" + this.Owner.getName() + "#k#n가 빙고 게임 참여자를 모집 중이야! " + channel + "채널에서 @빙고입장을 통해 입장해봐~", ""));
         }
      }

   }

   public int getMessageTime() {
      return this.MessageTime;
   }

   public void addRank(MapleCharacter a1) {
      if (!this.rank.contains(a1)) {
         this.rank.add(a1);
         a1.getMap().broadcastMessage(SLFCGPacket.BingoAddRank(a1));
      }

   }

   public MapleCharacter getOwner() {
      return this.Owner;
   }

   public int[][] getTable(MapleCharacter a1) {
      return (int[][])this.players.get(a1);
   }

   public void setTable(MapleCharacter a1, int[][] a2) {
      this.players.replace(a1, (int[][])this.players.get(a1), a2);
   }

   public List<MapleCharacter> getRanking() {
      return this.rank;
   }

   public void StartGame() {
      Iterator var1 = this.players.keySet().iterator();

      while(var1.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var1.next();
         chr.cancelAllBuffs();
         chr.getClient().getSession().writeAndFlush(SLFCGPacket.BingoUI(3, this.round));
         chr.getClient().getSession().writeAndFlush(SLFCGPacket.BingoHostNumber(-1, 75));
      }

      if (this.getBingoTimer() != null) {
         this.getBingoTimer().cancel(false);
      }

      this.setBingoTimer(Timer.EventTimer.getInstance().register(new Runnable() {
         public void run() {
            int temp = BingoGame.this.players.size();
            Iterator var2 = BingoGame.this.players.keySet().iterator();

            while(var2.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var2.next();
               if (chr == null) {
                  --temp;
               }
            }

            if (temp <= 0) {
               BingoGame.this.StopBingo();
            } else if (BingoGame.this.hostednumbers.size() != 75 && BingoGame.this.rank.size() != 30 && BingoGame.this.rank.size() < BingoGame.this.players.size()) {
               int number;
               do {
                  do {
                     number = Randomizer.rand(1, 76);
                  } while(BingoGame.this.hostednumbers.contains(number));
               } while(number > 75);

               BingoGame.this.hostednumbers.add(number);
               Iterator var6 = BingoGame.this.players.keySet().iterator();

               while(var6.hasNext()) {
                  MapleCharacter chrx = (MapleCharacter)var6.next();
                  chrx.getClient().getSession().writeAndFlush(SLFCGPacket.BingoHostNumberReady());
                  chrx.getClient().getSession().writeAndFlush(SLFCGPacket.BingoHostNumber(number, 75 - BingoGame.this.hostednumbers.size()));
               }

            } else {
               BingoGame.this.StopBingo();
            }
         }
      }, 5000L));
   }

   public void InitGame(List<MapleCharacter> a1) {
      Iterator var2 = a1.iterator();

      while(var2.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var2.next();
         int[][] table = new int[5][5];
         List<Integer> temp = new ArrayList();

         for(int x = 0; x < 5; ++x) {
            for(int y = 0; y < 5; ++y) {
               int number;
               do {
                  number = Randomizer.rand(x * 15 + 1, (x + 1) * 15);
               } while(temp.contains(number) || number > (x + 1) * 15);

               temp.add(number);
               table[x][y] = number;
            }
         }

         table[2][2] = 0;
         this.players.put(chr, table);
         chr.getClient().getSession().writeAndFlush(SLFCGPacket.BingoInit(table));
      }

   }

   public void StopBingo() {
      this.getBingoTimer().cancel(true);
      Iterator var1 = this.players.keySet().iterator();

      while(true) {
         MapleCharacter chr;
         do {
            do {
               if (!var1.hasNext()) {
                  var1 = this.rank.iterator();

                  while(true) {
                     while(true) {
                        do {
                           if (!var1.hasNext()) {
                              isRunning = false;
                              return;
                           }

                           chr = (MapleCharacter)var1.next();
                        } while(chr == null);

                        int ranknumber = this.rank.indexOf(chr) + 1;
                        chr.dropMessage(5, "빙고 " + ranknumber + "등 보상 및 전체 보상이 지급되었습니다. 인벤토리 및 오즈포인트를 확인해주세요.");
                        if (ranknumber == 1) {
                           chr.AddStarDustCoin(2, point * 10);
                        } else if (ranknumber >= 2 && ranknumber <= 10) {
                           chr.AddStarDustCoin(2, point * 5);
                        } else if (ranknumber >= 11 && ranknumber <= 20) {
                           chr.AddStarDustCoin(2, point * 3);
                        } else {
                           chr.AddStarDustCoin(2, point);
                        }
                     }
                  }
               }

               chr = (MapleCharacter)var1.next();
               chr.getClient().getSession().writeAndFlush(SLFCGPacket.playSE("multiBingo/gameover"));
            } while(chr == null);

            MapleMap target = chr.getClient().getChannelServer().getMapFactory().getMap(922290200);
            chr.changeMap(target, target.getPortal(0));
            chr.setBingoGame((BingoGame)null);
         } while(this.rank.contains(chr));

         Iterator var4 = items.iterator();

         while(var4.hasNext()) {
            Pair<Integer, Integer> item = (Pair)var4.next();
            if (chr.getInventory(GameConstants.getInventoryType((Integer)item.left)).getNumFreeSlot() > 0) {
               chr.gainItem((Integer)item.left, (Integer)item.right);
            } else {
               String var10002 = MapleItemInformationProvider.getInstance().getName((Integer)item.left);
               chr.dropMessage(-7, "인벤토리 공간이 부족하여 " + var10002 + "아이템 " + item.right + "개를 지급받지 못했습니다. 이 메세지를 캡쳐하여 GM에게 문의해주세요.");
            }
         }
      }
   }

   public ScheduledFuture<?> getBingoTimer() {
      return this.BingoTimer;
   }

   public void setBingoTimer(ScheduledFuture<?> bingoTimer) {
      this.BingoTimer = bingoTimer;
   }
}
