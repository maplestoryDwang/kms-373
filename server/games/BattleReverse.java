package server.games;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.World;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import server.Randomizer;
import server.Timer;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.SLFCGPacket;

public class BattleReverse {
   public static List<MapleCharacter> BattleReverseMatchingQueue = new ArrayList();
   public static List<MapleCharacter> BattleReverseMatchingQueue2 = new ArrayList();
   public static List<BattleReverse.BattleReverseMatchingInfo> BattleReverseGameList = new ArrayList();
   private List<BattleReverse.BattleReversePlayer> Players = new ArrayList();
   private BattleReverse.BattleReverseStone[][] Board = new BattleReverse.BattleReverseStone[8][8];
   private Point lastPoint = new Point(0, 0);
   private BattleReverse.BattleReversePlayer CurrentPlayer = null;
   private ScheduledFuture<?> BattleReverseTimer = null;

   public BattleReverse(List<MapleCharacter> chrs) {
      int stone = 0;
      Iterator var3 = chrs.iterator();

      while(var3.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var3.next();
         int[] var10005 = new int[]{10000, 0};
         var10005[1] = stone++;
         this.Players.add(new BattleReverse.BattleReversePlayer(chr, var10005));
      }

   }

   private void InitBoard() {
      int a;
      for(a = 0; a < 8; ++a) {
         for(int b = 0; b < 8; ++b) {
            this.Board[a][b] = new BattleReverse.BattleReverseStone(new int[]{a, b, -1});
         }
      }

      this.Board[3][3].setStoneId(0);
      this.Board[4][4].setStoneId(0);
      this.Board[4][3].setStoneId(1);
      this.Board[3][4].setStoneId(1);

      for(a = 0; a < 5; ++a) {
         for(boolean temp = false; !temp; temp = this.MakeHole(new Point(Randomizer.nextInt(8), Randomizer.nextInt(8)))) {
         }
      }

   }

   public List<BattleReverse.BattleReversePlayer> getPlayers() {
      return this.Players;
   }

   public List<BattleReverse.BattleReverseStone> getStones() {
      List<BattleReverse.BattleReverseStone> list = new ArrayList();

      for(int x = 0; x < 8; ++x) {
         for(int y = 0; y < 8; ++y) {
            if (this.Board[x][y].getStoneId() != -1) {
               list.add(this.Board[x][y]);
            }
         }
      }

      return list;
   }

   private BattleReverse.BattleReversePlayer getOpponent(int charid) {
      Iterator var2 = this.Players.iterator();

      BattleReverse.BattleReversePlayer Player;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         Player = (BattleReverse.BattleReversePlayer)var2.next();
      } while(Player.chr.getId() == charid);

      return Player;
   }

   private boolean MakeHole(Point p) {
      if (this.Board[p.x][p.y].getStoneId() == -1) {
         this.Board[p.x][p.y].setStoneId(3);
         return true;
      } else {
         return false;
      }
   }

   public BattleReverse.BattleReversePlayer getPlayer(int id) {
      Iterator var2 = this.Players.iterator();

      BattleReverse.BattleReversePlayer player;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         player = (BattleReverse.BattleReversePlayer)var2.next();
      } while(player.chr.getId() != id);

      return player;
   }

   public boolean isValidMove(MapleCharacter chr, Point pt) {
      boolean contains = false;
      BattleReverse.BattleReversePlayer player = this.getPlayer(chr.getId());
      BattleReverse.BattleReversePlayer opponent = this.getOpponent(chr.getId());
      if (player != null && opponent != null & pt != null && (player.getStoneId() == 0 || player.getStoneId() == 1) && (opponent.getStoneId() == 0 || opponent.getStoneId() == 1) && pt.x >= 0 && pt.x < 8 && pt.y >= 0 && pt.y < 8) {
         contains = this.getPlaceablePoints(player).contains(pt);
      }

      return contains;
   }

   public void sendPlaceStone(MapleCharacter mapleCharacter, Point point) {
      if (this.CurrentPlayer.chr != null) {
         if (this.CurrentPlayer.chr.getId() != mapleCharacter.getId()) {
            return;
         }

         if (mapleCharacter.getBattleReverseInstance().isValidMove(mapleCharacter, point)) {
            mapleCharacter.getBattleReverseInstance().placeStone(point, mapleCharacter);
         }
      }

   }

   public void skipPlayer(MapleCharacter mapleCharacter) {
      List placeablePoints;
      if (!(placeablePoints = mapleCharacter.getBattleReverseInstance().getPlaceablePoints(mapleCharacter.getBattleReverseInstance().getPlayer(mapleCharacter.getId()))).isEmpty()) {
         BattleReverse battleReverseInstance = mapleCharacter.getBattleReverseInstance();
         battleReverseInstance.sendPlaceStone(mapleCharacter, (Point)placeablePoints.get(Randomizer.nextInt(placeablePoints.size())));
      } else {
         mapleCharacter.getBattleReverseInstance().endGame(mapleCharacter, true);
         mapleCharacter.dropMessage(1, "오류가 발생하여 게임이 취소됩니다.");
      }
   }

   public void placeStone(Point lastPoint, MapleCharacter mapleCharacter) {
      BattleReverse.BattleReversePlayer player = this.getPlayer(mapleCharacter.getId());
      BattleReverse.BattleReversePlayer opponent = this.getOpponent(mapleCharacter.getId());
      int x = lastPoint.x;
      int y = lastPoint.y;
      int n = x;
      int n2 = y;
      this.setLastPoint(lastPoint);

      int n3;
      int n4;
      label269:
      for(n3 = 0; x >= 0 && y >= 0 && (x == n || this.Board[x][y].getStoneId() != -1 && this.Board[x][y].getStoneId() != 3); --y) {
         if (this.Board[x][y].getStoneId() == player.getStoneId()) {
            n4 = 0;

            while(true) {
               if (n4 >= n - x) {
                  break label269;
               }

               this.Board[x + n4][y + n4].setStoneId(player.getStoneId());
               ++n3;
               ++n4;
            }
         }

         --x;
      }

      n4 = n;

      int n5;
      int n7;
      label249:
      for(n5 = y; n4 >= 0 && (n4 == n || this.Board[n4][n5].getStoneId() != -1 && this.Board[n4][n5].getStoneId() != 3); --n4) {
         if (this.Board[n4][n5].getStoneId() == player.getStoneId()) {
            n7 = 0;

            while(true) {
               if (n7 >= n - n4) {
                  break label249;
               }

               this.Board[n4 + n7][n5].setStoneId(player.getStoneId());
               ++n3;
               ++n7;
            }
         }
      }

      int n11;
      label231:
      for(n7 = n; n7 >= 0 && n5 <= 7 && (n7 == n || this.Board[n7][n5].getStoneId() != -1 && this.Board[n7][n5].getStoneId() != 3); ++n5) {
         if (this.Board[n7][n5].getStoneId() == player.getStoneId()) {
            n11 = 0;

            while(true) {
               if (n11 >= n - n7) {
                  break label231;
               }

               this.Board[n7 + n11][n5 - n11].setStoneId(player.getStoneId());
               ++n3;
               ++n11;
            }
         }

         --n7;
      }

      n7 = n;

      int n12;
      label211:
      for(n11 = y; n11 <= 7 && (n11 == n2 || this.Board[n7][n11].getStoneId() != -1 && this.Board[n7][n11].getStoneId() != 3); ++n11) {
         if (this.Board[n7][n11].getStoneId() == player.getStoneId()) {
            n12 = 0;

            while(true) {
               if (n12 >= n11 - n2) {
                  break label211;
               }

               this.Board[n7][n11 - n12].setStoneId(player.getStoneId());
               ++n3;
               ++n12;
            }
         }
      }

      label193:
      for(n11 = n2; n7 <= 7 && n11 <= 7 && (n7 == n || this.Board[n7][n11].getStoneId() != -1 && this.Board[n7][n11].getStoneId() != 3); ++n11) {
         if (this.Board[n7][n11].getStoneId() == player.getStoneId()) {
            n12 = 0;

            while(true) {
               if (n12 >= n7 - n) {
                  break label193;
               }

               this.Board[n7 - n12][n11 - n12].setStoneId(player.getStoneId());
               ++n3;
               ++n12;
            }
         }

         ++n7;
      }

      n11 = n;

      int n16;
      label173:
      for(n12 = n2; n11 <= 7 && (n11 == n || this.Board[n11][n12].getStoneId() != -1 && this.Board[n11][n12].getStoneId() != 3); ++n11) {
         if (this.Board[n11][n12].getStoneId() == player.getStoneId()) {
            n16 = 0;

            while(true) {
               if (n16 >= n11 - n) {
                  break label173;
               }

               this.Board[n11 - n16][n12].setStoneId(player.getStoneId());
               ++n3;
               ++n16;
            }
         }
      }

      int n17;
      label155:
      for(n16 = n; n16 <= 7 && n12 >= 0 && (n16 == n || this.Board[n16][n12].getStoneId() != -1 && this.Board[n16][n12].getStoneId() != 3); --n12) {
         if (this.Board[n16][n12].getStoneId() == player.getStoneId()) {
            n17 = 0;

            while(true) {
               if (n17 >= n16 - n) {
                  break label155;
               }

               this.Board[n16 - n17][n12 + n17].setStoneId(player.getStoneId());
               ++n3;
               ++n17;
            }
         }

         ++n16;
      }

      n16 = n;

      label135:
      for(n17 = n2; n17 >= 0 && (n17 == n2 || this.Board[n16][n17].getStoneId() != -1 && this.Board[n16][n17].getStoneId() != 3); --n17) {
         if (this.Board[n16][n17].getStoneId() == player.getStoneId()) {
            int n18 = 0;

            while(true) {
               if (n18 >= n2 - n17) {
                  break label135;
               }

               this.Board[n16][n17 + n18].setStoneId(player.getStoneId());
               ++n3;
               ++n18;
            }
         }
      }

      opponent.setHP(opponent.getHP() - n3 * 10);
      if (n3 >= 4) {
         if (n3 >= 6) {
            opponent.setHP(opponent.getHP() - 100);
         } else {
            opponent.setHP(opponent.getHP() - 50);
         }
      }

      this.Board[n16][n2].setStoneId(player.getStoneId());
      if (opponent.getHP() <= 0) {
         Iterator var18 = this.Players.iterator();

         while(var18.hasNext()) {
            BattleReverse.BattleReversePlayer battleReversePlayer4 = (BattleReverse.BattleReversePlayer)var18.next();
            battleReversePlayer4.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onBoardUpdate(opponent.getPlayer().getId() == battleReversePlayer4.getPlayer().getId(), lastPoint, player.getStoneId(), this.getOpponent(player.getPlayer().getId()).getHP(), this.getStones(), (byte)1));
         }

         this.endGame(mapleCharacter, false);
      } else {
         this.setTurn(mapleCharacter, player, opponent, lastPoint);
      }
   }

   public void setTurn(MapleCharacter mapleCharacter, BattleReverse.BattleReversePlayer battleReversePlayer, BattleReverse.BattleReversePlayer battleReversePlayer2, Point point) {
      Iterator iterator3;
      BattleReverse.BattleReversePlayer battleReversePlayer5;
      if (this.getPlaceablePoints(this.getOpponent(mapleCharacter.getId())).size() != 0) {
         iterator3 = this.Players.iterator();

         while(iterator3.hasNext()) {
            battleReversePlayer5 = (BattleReverse.BattleReversePlayer)iterator3.next();
            battleReversePlayer5.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onBoardUpdate(battleReversePlayer2.getPlayer().getId() == battleReversePlayer5.getPlayer().getId(), point, battleReversePlayer.getStoneId(), this.getOpponent(battleReversePlayer.getPlayer().getId()).getHP(), this.getStones(), (byte)1));
         }

         if (this.BattleReverseTimer != null) {
            this.BattleReverseTimer.cancel(false);
         }

         this.CurrentPlayer = battleReversePlayer2;
         this.BattleReverseTimer = Timer.EventTimer.getInstance().schedule(() -> {
            this.skipPlayer(battleReversePlayer2.chr);
         }, 11200L);
      } else if (this.getPlaceablePoints(this.getPlayer(mapleCharacter.getId())).size() == 0) {
         iterator3 = this.Players.iterator();

         while(iterator3.hasNext()) {
            battleReversePlayer5 = (BattleReverse.BattleReversePlayer)iterator3.next();
            battleReversePlayer5.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onBoardUpdate(battleReversePlayer2.getPlayer().getId() == battleReversePlayer5.getPlayer().getId(), point, battleReversePlayer.getStoneId(), this.getOpponent(battleReversePlayer.getPlayer().getId()).getHP(), this.getStones(), (byte)1));
         }

         this.endGame(mapleCharacter, false);
      } else {
         iterator3 = this.Players.iterator();

         while(iterator3.hasNext()) {
            (battleReversePlayer5 = (BattleReverse.BattleReversePlayer)iterator3.next()).chr.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo("둘 수 있는 곳이 없어 턴이 넘어갑니다.", 3, 20, 20));
            battleReversePlayer5.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onBoardUpdate(battleReversePlayer.getPlayer().getId() == battleReversePlayer5.getPlayer().getId(), point, this.getOpponent(mapleCharacter.getId()).getStoneId(), this.getPlayer(mapleCharacter.getId()).getHP(), this.getStones(), (byte)0));
         }

      }
   }

   public void endGame(MapleCharacter mapleCharacter, boolean b) {
      BattleReverse.BattleReversePlayer player = this.getPlayer(mapleCharacter.getId());
      BattleReverse.BattleReversePlayer opponent = this.getOpponent(mapleCharacter.getId());
      BattleReverse.BattleReversePlayer battleReversePlayer = null;
      BattleReverse.BattleReversePlayer battleReversePlayer2 = null;
      if (!b) {
         if (player.getHP() <= 0) {
            battleReversePlayer = opponent;
            battleReversePlayer2 = player;
         } else if (opponent.getHP() <= 0) {
            battleReversePlayer = player;
            battleReversePlayer2 = opponent;
         } else if (player.getHP() > opponent.getHP()) {
            battleReversePlayer = player;
            battleReversePlayer2 = opponent;
         } else if (player.getHP() < opponent.getHP()) {
            battleReversePlayer = opponent;
            battleReversePlayer2 = player;
         } else {
            player.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onResult(2));
            opponent.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onResult(2));
         }
      } else {
         opponent.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onResult(4));
         opponent.chr.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo("상대방이 미니게임을 종료하여 게임이 종료됩니다.", 3, 20, 20));
         opponent.chr.getClient().getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/oneCard/victory"));
      }

      if (battleReversePlayer != null && battleReversePlayer2 != null) {
         battleReversePlayer.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onResult(4));
         battleReversePlayer.chr.getClient().getSession().writeAndFlush(CWvsContext.onSessionValue("svBattleResult", "win"));
         battleReversePlayer.chr.getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/victory"));
         battleReversePlayer2.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onResult(4));
         battleReversePlayer2.chr.getClient().getSession().writeAndFlush(CWvsContext.onSessionValue("svBattleResult", "lose"));
         battleReversePlayer2.chr.getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/gameover"));
         battleReversePlayer.chr.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo(battleReversePlayer.chr.getName() + "님의 승리!", 3, 20, 20));
         battleReversePlayer2.chr.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo(battleReversePlayer.chr.getName() + "님의 승리!", 3, 20, 20));
         battleReversePlayer.chr.setKeyValue(100664, "reverse", "1");
         battleReversePlayer2.chr.setKeyValue(100664, "reverse", "2");
      }

      if (this.BattleReverseTimer != null) {
         this.BattleReverseTimer.cancel(false);
         this.BattleReverseTimer = null;
      }

      Timer.EventTimer.getInstance().schedule(() -> {
         Iterator var1 = this.Players.iterator();

         while(var1.hasNext()) {
            BattleReverse.BattleReversePlayer pp = (BattleReverse.BattleReversePlayer)var1.next();
            if (pp != null) {
               pp.chr.warp(993186800);
               pp.chr.setBattleReverseInstance((BattleReverse)null);
            }
         }

      }, 4000L);
   }

   public List getPlaceablePoints(BattleReverse.BattleReversePlayer battleReversePlayer) {
      ArrayList<Point> list = new ArrayList();
      BattleReverse.BattleReversePlayer opponent = this.getOpponent(battleReversePlayer.chr.getId());

      for(int i = 0; i < 8; ++i) {
         for(int j = 0; j < 8; ++j) {
            if (this.Board[i][j].getStoneId() == opponent.getStoneId()) {
               if (i - 1 >= 0 && j - 1 >= 0 && i + 1 <= 7 && j + 1 <= 7 && this.Board[i + 1][j + 1].getStoneId() == battleReversePlayer.getStoneId()) {
                  while(i - 1 >= 0 && j - 1 >= 0) {
                     if (this.Board[i - 1][j - 1].getStoneId() != opponent.getStoneId()) {
                        if (this.Board[i - 1][j - 1].getStoneId() == -1) {
                           list.add(new Point(i - 1, j - 1));
                        }
                        break;
                     }

                     --i;
                     --j;
                  }
               }

               int n3 = i;
               int n4 = j;
               if (i + 1 <= 7 && j - 1 >= 0 && i - 1 >= 0 && j + 1 <= 7 && this.Board[i - 1][j + 1].getStoneId() == battleReversePlayer.getStoneId()) {
                  while(n3 + 1 <= 7 && n4 - 1 >= 0) {
                     if (this.Board[n3 + 1][n4 - 1].getStoneId() != opponent.getStoneId()) {
                        if (this.Board[n3 + 1][n4 - 1].getStoneId() == -1) {
                           list.add(new Point(n3 + 1, n4 - 1));
                        }
                        break;
                     }

                     ++n3;
                     --n4;
                  }
               }

               int n5 = i;
               int n6 = j;
               if (i + 1 <= 7 && j + 1 <= 7 && i - 1 >= 0 && j - 1 >= 0 && this.Board[i - 1][j - 1].getStoneId() == battleReversePlayer.getStoneId()) {
                  while(n5 + 1 <= 7 && n6 + 1 <= 7) {
                     if (this.Board[n5 + 1][n6 + 1].getStoneId() != opponent.getStoneId()) {
                        if (this.Board[n5 + 1][n6 + 1].getStoneId() == -1) {
                           list.add(new Point(n5 + 1, n6 + 1));
                        }
                        break;
                     }

                     ++n5;
                     ++n6;
                  }
               }

               int n7 = i;
               int n8 = j;
               if (i - 1 >= 0 && j + 1 <= 7 && i + 1 <= 7 && j - 1 >= 0 && this.Board[i + 1][j - 1].getStoneId() == battleReversePlayer.getStoneId()) {
                  while(n7 - 1 >= 0 && n8 + 1 <= 7) {
                     if (this.Board[n7 - 1][n8 + 1].getStoneId() != opponent.getStoneId()) {
                        if (this.Board[n7 - 1][n8 + 1].getStoneId() == -1) {
                           list.add(new Point(n7 - 1, n8 + 1));
                        }
                        break;
                     }

                     --n7;
                     ++n8;
                  }
               }

               int n9 = i;
               int n10 = j;
               if (j - 1 >= 0 && j + 1 <= 7 && this.Board[i][j + 1].getStoneId() == battleReversePlayer.getStoneId()) {
                  while(n10 - 1 >= 0) {
                     if (this.Board[n9][n10 - 1].getStoneId() != opponent.getStoneId()) {
                        if (this.Board[n9][n10 - 1].getStoneId() == -1) {
                           list.add(new Point(n9, n10 - 1));
                        }
                        break;
                     }

                     --n10;
                  }
               }

               int n11 = i;
               int n12 = j;
               if (j + 1 <= 7 && j - 1 >= 0 && this.Board[i][j - 1].getStoneId() == battleReversePlayer.getStoneId()) {
                  while(n12 + 1 <= 7) {
                     if (this.Board[n11][n12 + 1].getStoneId() != opponent.getStoneId()) {
                        if (this.Board[n11][n12 + 1].getStoneId() == -1) {
                           list.add(new Point(n11, n12 + 1));
                        }
                        break;
                     }

                     ++n12;
                  }
               }

               int n13 = i;
               int n14 = j;
               if (i + 1 <= 7 && i - 1 >= 0 && this.Board[i - 1][j].getStoneId() == battleReversePlayer.getStoneId()) {
                  while(n13 + 1 <= 7) {
                     if (this.Board[n13 + 1][n14].getStoneId() != opponent.getStoneId()) {
                        if (this.Board[n13 + 1][n14].getStoneId() == -1) {
                           list.add(new Point(n13 + 1, n14));
                        }
                        break;
                     }

                     ++n13;
                  }
               }

               int n15 = i;
               int n16 = j;
               if (i - 1 >= 0 && i + 1 <= 7 && this.Board[i + 1][j].getStoneId() == battleReversePlayer.getStoneId()) {
                  while(n15 - 1 >= 0) {
                     if (this.Board[n15 - 1][n16].getStoneId() != opponent.getStoneId()) {
                        if (this.Board[n15 - 1][n16].getStoneId() == -1) {
                           list.add(new Point(n15 - 1, n16));
                        }
                        break;
                     }

                     --n15;
                  }
               }

               i = i;
               j = j;
            }
         }
      }

      return list;
   }

   public static void addQueue(MapleCharacter mapleCharacter, int n) {
      if (BattleReverseMatchingQueue.size() >= 2) {
         mapleCharacter.getClient().send(CField.NPCPacket.getNPCTalk(9062354, (byte)0, "잠시 후에 다시 시도해 주세요.", "00 00", (byte)0, mapleCharacter.getId()));
      } else if (!BattleReverseMatchingQueue.contains(mapleCharacter)) {
         BattleReverseMatchingQueue.add(mapleCharacter);
         mapleCharacter.getClient().send(SLFCGPacket.ContentsWaiting(mapleCharacter, 993186700, 11, 2, 1, 23));
         if (BattleReverseMatchingQueue.size() == n) {
            Iterator var2 = BattleReverseMatchingQueue.iterator();

            while(var2.hasNext()) {
               final MapleCharacter mapleCharacter2 = (MapleCharacter)var2.next();
               mapleCharacter2.getClient().send(SLFCGPacket.ContentsWaiting(mapleCharacter2, 0, 19, 0, 1, 23));
               (mapleCharacter2.ConstentTimer = new java.util.Timer()).schedule(new TimerTask() {
                  public void run() {
                     List<MapleCharacter> remover = new ArrayList();
                     BattleReverse.BattleReverseMatchingQueue.remove(mapleCharacter2);
                     Iterator var2 = BattleReverse.BattleReverseMatchingQueue.iterator();

                     MapleCharacter chr2;
                     while(var2.hasNext()) {
                        chr2 = (MapleCharacter)var2.next();
                        if (mapleCharacter2.getId() != chr2.getId()) {
                           remover.add(chr2);
                           chr2.getClient().send(SLFCGPacket.ContentsWaiting(chr2, 0, 11, 5, 1, 23));
                        }
                     }

                     var2 = remover.iterator();

                     while(var2.hasNext()) {
                        chr2 = (MapleCharacter)var2.next();
                        BattleReverse.BattleReverseMatchingQueue.remove(chr2);
                        BattleReverse.BattleReverseMatchingQueue2.remove(chr2);
                     }

                     mapleCharacter2.getClient().send(SLFCGPacket.ContentsWaiting(mapleCharacter2, 0, 11, 5, 1, 23));
                     if (mapleCharacter2.ConstentTimer != null) {
                        mapleCharacter2.ConstentTimer.cancel();
                        mapleCharacter2.ConstentTimer = null;
                     }

                  }
               }, 10000L);
            }
         }
      }

   }

   public static void addQueue(MapleCharacter mapleCharacter, boolean b) {
      if (b) {
         Iterator var2 = mapleCharacter.getParty().getMembers().iterator();

         while(var2.hasNext()) {
            MaplePartyCharacter maplePartyCharacter = (MaplePartyCharacter)var2.next();
            int channel = World.Find.findChannel(maplePartyCharacter.getId());
            if (channel > 0) {
               addQueue(ChannelServer.getInstance(channel).getPlayerStorage().getCharacterById(maplePartyCharacter.getId()), false);
            }
         }
      } else if (!BattleReverseMatchingQueue.contains(mapleCharacter)) {
         BattleReverseMatchingQueue.add(mapleCharacter);
         if (BattleReverseMatchingQueue.size() >= 2) {
            BattleReverse.BattleReverseMatchingInfo battleReverseMatchingInfo = new BattleReverse.BattleReverseMatchingInfo(new MapleCharacter[]{(MapleCharacter)BattleReverseMatchingQueue.get(0), (MapleCharacter)BattleReverseMatchingQueue.get(1)});
            BattleReverseGameList.add(battleReverseMatchingInfo);
            BattleReverseMatchingQueue.remove(battleReverseMatchingInfo.p1);
            BattleReverseMatchingQueue.remove(battleReverseMatchingInfo.p2);
            battleReverseMatchingInfo.p1.warp(993186700);
            battleReverseMatchingInfo.p2.warp(993186700);
            Timer.MapTimer.getInstance().schedule(() -> {
               ArrayList<MapleCharacter> chrs = new ArrayList();
               chrs.add(battleReverseMatchingInfo.p1);
               chrs.add(battleReverseMatchingInfo.p2);
               BattleReverse br = new BattleReverse(chrs);
               Iterator var3 = chrs.iterator();

               MapleCharacter p;
               while(var3.hasNext()) {
                  p = (MapleCharacter)var3.next();
                  p.setBattleReverseInstance(br);
               }

               br.InitBoard();
               var3 = chrs.iterator();

               while(var3.hasNext()) {
                  p = (MapleCharacter)var3.next();
                  p.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.createUI(chrs, p, br.getPlayer(p.getId()).getStoneId()));
               }

               Timer.EventTimer.getInstance().schedule(() -> {
                  br.StartGame();
               }, 3000L);
            }, 5000L);
         }
      }

   }

   public static void StartGame2() {
      if (BattleReverseMatchingQueue.size() >= 2) {
         BattleReverse.BattleReverseMatchingInfo battleReverseMatchingInfo = new BattleReverse.BattleReverseMatchingInfo(new MapleCharacter[]{(MapleCharacter)BattleReverseMatchingQueue.get(0), (MapleCharacter)BattleReverseMatchingQueue.get(1)});
         BattleReverseGameList.add(battleReverseMatchingInfo);
         BattleReverseMatchingQueue.remove(battleReverseMatchingInfo.p1);
         BattleReverseMatchingQueue.remove(battleReverseMatchingInfo.p2);
         battleReverseMatchingInfo.p1.warp(993186700);
         battleReverseMatchingInfo.p2.warp(993186700);
         battleReverseMatchingInfo.p1.getClient().send(SLFCGPacket.ContentsWaiting(battleReverseMatchingInfo.p1, 0, 11, 5, 1, 23));
         battleReverseMatchingInfo.p2.getClient().send(SLFCGPacket.ContentsWaiting(battleReverseMatchingInfo.p2, 0, 11, 5, 1, 23));
         Timer.MapTimer.getInstance().schedule(() -> {
            ArrayList<MapleCharacter> chrs = new ArrayList();
            chrs.add(battleReverseMatchingInfo.p1);
            chrs.add(battleReverseMatchingInfo.p2);
            BattleReverse br = new BattleReverse(chrs);
            Iterator var3 = chrs.iterator();

            MapleCharacter p;
            while(var3.hasNext()) {
               p = (MapleCharacter)var3.next();
               p.setBattleReverseInstance(br);
            }

            br.InitBoard();
            var3 = chrs.iterator();

            while(var3.hasNext()) {
               p = (MapleCharacter)var3.next();
               p.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.createUI(chrs, p, br.getPlayer(p.getId()).getStoneId()));
            }

            Timer.EventTimer.getInstance().schedule(() -> {
               br.StartGame();
            }, 3000L);
         }, 5000L);
      }

   }

   private void StartGame() {
      BattleReverse.BattleReversePlayer First = (BattleReverse.BattleReversePlayer)this.Players.get(0);
      Iterator var2 = this.Players.iterator();

      while(var2.hasNext()) {
         BattleReverse.BattleReversePlayer bp = (BattleReverse.BattleReversePlayer)var2.next();
         bp.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiOthelloGamePacket.onInit(this.getStones(), First.StoneId));
      }

      this.CurrentPlayer = First;
      if (this.BattleReverseTimer != null) {
         this.BattleReverseTimer.cancel(false);
      }

   }

   public Point getLastPoint() {
      return this.lastPoint;
   }

   public void setLastPoint(Point lastPoint) {
      this.lastPoint = lastPoint;
   }

   public ScheduledFuture getOthelloTimer() {
      return this.BattleReverseTimer;
   }

   public void setOthelloTimer(ScheduledFuture d) {
      this.BattleReverseTimer = d;
   }

   public class BattleReversePlayer {
      private int HP;
      private int StoneId;
      private MapleCharacter chr;

      public BattleReversePlayer(MapleCharacter player, int... args) {
         this.chr = player;
         this.HP = args[0];
         this.StoneId = args[1];
      }

      public MapleCharacter getPlayer() {
         return this.chr;
      }

      public void setHP(int a1) {
         this.HP = a1;
      }

      public int getHP() {
         return this.HP;
      }

      public int getStoneId() {
         return this.StoneId;
      }
   }

   public class BattleReverseStone {
      private int StoneId;
      private Point Position;

      public BattleReverseStone(int... args) {
         this.Position = new Point(args[0], args[1]);
         this.StoneId = args[2];
      }

      public Point getStonePosition() {
         return this.Position;
      }

      public int getStoneId() {
         return this.StoneId;
      }

      public void setStoneId(int a) {
         this.StoneId = a;
      }
   }

   public static class BattleReverseMatchingInfo {
      public MapleCharacter p1;
      public MapleCharacter p2;

      public BattleReverseMatchingInfo(MapleCharacter... chrs) {
         this.p1 = chrs[0];
         this.p2 = chrs[1];
      }
   }
}
