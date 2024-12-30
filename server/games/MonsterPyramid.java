package server.games;

import client.MapleCharacter;
import client.MapleCoolDownValueHolder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import server.Timer;
import tools.Pair;
import tools.Triple;
import tools.packet.CField;
import tools.packet.SLFCGPacket;

public class MonsterPyramid {
   public static List<MapleCharacter> monsterPyramidMatchingQueue = new ArrayList();
   public static List<MapleCharacter> monsterPyramidMatchingQueue2 = new ArrayList();
   public static List<Triple<Integer, Integer, Integer>> pyramidcheck = new ArrayList();
   private List<MonsterPyramid.PyramidPlayer> a;
   public int round = 0;
   private ScheduledFuture<?> MonsterPyramidTimer = null;

   public MonsterPyramid(List<MapleCharacter> list) {
      this.round = 1;
      this.a = new ArrayList();

      for(int i = 0; i < list.size(); ++i) {
         this.a.add(new MonsterPyramid.PyramidPlayer((MapleCharacter)list.get(i), (byte)i, false, i));
      }

      Iterator<MonsterPyramid.PyramidPlayer> iterator = this.getPlayers().iterator();
      if (iterator.hasNext()) {
         MonsterPyramid.PyramidPlayer p = (MonsterPyramid.PyramidPlayer)iterator.next();
         p.randBlocks();
      }

   }

   public MonsterPyramid.PyramidPlayer getPlayer(MapleCharacter mapleCharacter) {
      Iterator iterator = this.getPlayers().iterator();

      MonsterPyramid.PyramidPlayer pyramidPlayer;
      do {
         if (!iterator.hasNext()) {
            return null;
         }
      } while((pyramidPlayer = (MonsterPyramid.PyramidPlayer)iterator.next()).getPlayer().getId() != mapleCharacter.getId());

      return pyramidPlayer;
   }

   public void skipPlayer(MonsterPyramid.PyramidPlayer p) {
      int floor = -2;
      int pos = -2;
      int type = -2;
      if (this.MonsterPyramidTimer != null || p.b == null) {
         this.MonsterPyramidTimer.cancel(true);
         this.MonsterPyramidTimer = null;
      }

      Iterator var5 = this.getPlayers().iterator();

      while(var5.hasNext()) {
         MonsterPyramid.PyramidPlayer pp = (MonsterPyramid.PyramidPlayer)var5.next();
         if (pp.b.getId() == p.b.getId()) {
            Iterator var7 = pyramidcheck.iterator();

            while(true) {
               Triple check;
               Iterator var9;
               Pair block;
               do {
                  do {
                     if (!var7.hasNext()) {
                        if (floor == -2) {
                           var7 = pyramidcheck.iterator();

                           label84:
                           while(true) {
                              do {
                                 do {
                                    if (!var7.hasNext()) {
                                       break label84;
                                    }

                                    check = (Triple)var7.next();
                                 } while((Integer)check.getLeft() <= 0);
                              } while((Integer)check.getRight() != -1);

                              var9 = pp.blocktype.iterator();

                              while(var9.hasNext()) {
                                 block = (Pair)var9.next();
                                 if (this.test((Integer)check.getLeft(), (Integer)check.getMid(), (Integer)block.getLeft()) && (Integer)block.getRight() > 0) {
                                    floor = (Integer)check.getLeft();
                                    pos = (Integer)check.getMid();
                                    type = (Integer)block.getLeft();
                                 }
                              }
                           }
                        }

                        if (p.b.getKeyValue(100668, "피라미드제한") < 0L) {
                           p.b.setKeyValue(100668, "피라미드제한", "0");
                        }

                        MapleCharacter var10000 = p.b;
                        long var10003 = p.b.getKeyValue(100668, "피라미드제한");
                        var10000.setKeyValue(100668, "피라미드제한", (var10003 + 1L).makeConcatWithConstants<invokedynamic>(var10003 + 1L));
                        p.b.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo("제한시간 총 " + p.b.getKeyValue(100668, "피라미드제한") + "번 이상 되셨습니다.(3번 이상 퇴장 및 패널티)", 3, 20, 20));
                        List<MapleCharacter> chrs = new ArrayList();
                        Iterator var12 = this.getPlayers().iterator();

                        MonsterPyramid.PyramidPlayer p2;
                        while(var12.hasNext()) {
                           p2 = (MonsterPyramid.PyramidPlayer)var12.next();
                           if (p2 != null) {
                              chrs.add(p2.b);
                           }
                        }

                        var12 = this.getPlayers().iterator();

                        while(var12.hasNext()) {
                           p2 = (MonsterPyramid.PyramidPlayer)var12.next();
                           p2.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(chrs, false, 1, 4, 0, pp.turn, 7, 0, 0));
                        }

                        if (p.b.getKeyValue(100668, "피라미드제한") >= 3L) {
                           this.endGame(pp.b, true);
                        } else {
                           this.setBlock(pp.b, floor, pos, type);
                        }

                        return;
                     }

                     check = (Triple)var7.next();
                  } while((Integer)check.getLeft() != 0);
               } while((Integer)check.getRight() != -1);

               var9 = pp.blocktype.iterator();

               while(var9.hasNext()) {
                  block = (Pair)var9.next();
                  if ((Integer)block.getRight() > 0) {
                     floor = (Integer)check.getLeft();
                     pos = (Integer)check.getMid();
                     type = (Integer)block.getLeft();
                  }
               }
            }
         }
      }

   }

   public void endGame(MapleCharacter chr, boolean gang) {
      if (this.MonsterPyramidTimer != null) {
         this.MonsterPyramidTimer.cancel(false);
         this.MonsterPyramidTimer = null;
      }

      if (gang) {
         chr.addCooldown(100668, System.currentTimeMillis(), 900000L);
         chr.setKeyValue(100668, "Rank", "0");
      }

      Iterator var3 = this.getPlayers().iterator();

      while(var3.hasNext()) {
         MonsterPyramid.PyramidPlayer p = (MonsterPyramid.PyramidPlayer)var3.next();
         if (p != null) {
            if (gang) {
               p.b.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo(chr.getName() + "님이 전장을 이탈하여 게임이 종료 되었습니다.", 3, 20, 20));
            }

            p.b.warp(993186500);
            p.b.setMonsterPyramidInstance((MonsterPyramid)null);
         }
      }

   }

   public void playerDead() {
   }

   public static void StartGame(int n) {
      if (monsterPyramidMatchingQueue.size() == n) {
         MonsterPyramid.MonsterPyramidMatchingInfo monsterPyramidMatchingInfo = new MonsterPyramid.MonsterPyramidMatchingInfo(monsterPyramidMatchingQueue);
         ArrayList<MapleCharacter> list = new ArrayList();
         Iterator var3 = monsterPyramidMatchingInfo.players.iterator();

         while(var3.hasNext()) {
            MapleCharacter mapleCharacter2 = (MapleCharacter)var3.next();
            monsterPyramidMatchingQueue.remove(mapleCharacter2);
            list.add(mapleCharacter2);
            mapleCharacter2.warp(993186400);
            mapleCharacter2.getClient().send(SLFCGPacket.ContentsWaiting(mapleCharacter2, 0, 11, 5, 1, 24));
         }

         pyramidcheck.clear();

         for(int i = 0; i < 8; ++i) {
            for(int i2 = 0; i2 < 8 - i; ++i2) {
               pyramidcheck.add(new Triple(i, i2, -1));
            }
         }

         Timer.EtcTimer.getInstance().schedule(() -> {
            MonsterPyramid monsterPyramidInstance = null;

            try {
               monsterPyramidInstance = new MonsterPyramid(list);
            } catch (Exception var4) {
               System.out.println(var4);
            }

            try {
               Iterator var2 = list.iterator();

               while(var2.hasNext()) {
                  MapleCharacter p = (MapleCharacter)var2.next();
                  p.setMonsterPyramidInstance(monsterPyramidInstance);
                  p.setKeyValue(100668, "피라미드제한", "0");
                  p.getClient().send(SLFCGPacket.MonsterPyramidPacket.createUI(list));
               }
            } catch (Exception var5) {
               System.out.println(var5);
            }

         }, 5000L);
      }
   }

   private void startTimer(MonsterPyramid.PyramidPlayer pp) {
      this.MonsterPyramidTimer = Timer.EventTimer.getInstance().schedule(() -> {
         this.skipPlayer(pp);
      }, 11000L);
   }

   public static void CancelWaiting(MapleCharacter chr, int n) {
      List<MapleCharacter> remover = new ArrayList();
      monsterPyramidMatchingQueue.remove(chr);
      Iterator var3 = monsterPyramidMatchingQueue.iterator();

      MapleCharacter chr2;
      while(var3.hasNext()) {
         chr2 = (MapleCharacter)var3.next();
         if (chr.getId() != chr2.getId()) {
            remover.add(chr2);
            chr2.getClient().send(SLFCGPacket.ContentsWaiting(chr2, 0, 11, 5, 1, 24));
         }
      }

      var3 = remover.iterator();

      while(var3.hasNext()) {
         chr2 = (MapleCharacter)var3.next();
         monsterPyramidMatchingQueue.remove(chr2);
      }

      chr.getClient().send(SLFCGPacket.ContentsWaiting(chr, 0, 11, 5, 1, 24));
   }

   public static void addQueue(MapleCharacter mapleCharacter, int n) {
      if (mapleCharacter.skillisCooling(100668) && !mapleCharacter.isGM()) {
         String 초 = "";
         Iterator var6 = mapleCharacter.getCooldowns().iterator();

         while(var6.hasNext()) {
            MapleCoolDownValueHolder m = (MapleCoolDownValueHolder)var6.next();
            if (m.skillId == 100668) {
               long var10000 = (m.length + m.startTime - System.currentTimeMillis()) / 1000L;
               초 = (var10000 / 60L).makeConcatWithConstants<invokedynamic>(var10000 / 60L);
               break;
            }
         }

         mapleCharacter.getClient().send(CField.NPCPacket.getNPCTalk(9062354, (byte)0, "#r#e" + 초 + "분 후#n#k에 미니게임에 참여할 수 있어요!\r\n\r\n#b(미니게임 이용 중 포기를 하는 경우 잠시 동안 미니게임을 이용할 수 없습니다.)#k", "00 00", (byte)0, mapleCharacter.getId()));
      } else if (monsterPyramidMatchingQueue.size() >= 3) {
         mapleCharacter.getClient().send(CField.NPCPacket.getNPCTalk(9062354, (byte)0, "잠시 후에 다시 시도해 주세요.", "00 00", (byte)0, mapleCharacter.getId()));
      } else if (!monsterPyramidMatchingQueue.contains(mapleCharacter)) {
         monsterPyramidMatchingQueue.add(mapleCharacter);
         mapleCharacter.getClient().send(SLFCGPacket.ContentsWaiting(mapleCharacter, 993186400, 11, 2, 1, 24));
         if (monsterPyramidMatchingQueue.size() == n) {
            Iterator var2 = monsterPyramidMatchingQueue.iterator();

            while(var2.hasNext()) {
               final MapleCharacter mapleCharacter2 = (MapleCharacter)var2.next();
               mapleCharacter2.getClient().send(SLFCGPacket.ContentsWaiting(mapleCharacter2, 0, 19, 0, 1, 24));
               mapleCharacter2.ConstentTimer = new java.util.Timer();
               mapleCharacter2.ConstentTimer.schedule(new TimerTask() {
                  public void run() {
                     List<MapleCharacter> remover = new ArrayList();
                     MonsterPyramid.monsterPyramidMatchingQueue.remove(mapleCharacter2);
                     Iterator var2 = MonsterPyramid.monsterPyramidMatchingQueue.iterator();

                     MapleCharacter chr2;
                     while(var2.hasNext()) {
                        chr2 = (MapleCharacter)var2.next();
                        if (mapleCharacter2.getId() != chr2.getId()) {
                           remover.add(chr2);
                           chr2.getClient().send(SLFCGPacket.ContentsWaiting(chr2, 0, 11, 5, 1, 24));
                        }
                     }

                     var2 = remover.iterator();

                     while(var2.hasNext()) {
                        chr2 = (MapleCharacter)var2.next();
                        MonsterPyramid.monsterPyramidMatchingQueue.remove(chr2);
                        MonsterPyramid.monsterPyramidMatchingQueue2.remove(chr2);
                     }

                     mapleCharacter2.getClient().send(SLFCGPacket.ContentsWaiting(mapleCharacter2, 0, 11, 5, 1, 24));
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

   public List<MonsterPyramid.PyramidPlayer> getPlayers() {
      return this.a;
   }

   public void setPlayers(List<MonsterPyramid.PyramidPlayer> a) {
      this.a = a;
   }

   public void setBlock(MapleCharacter chr, int floor, int pos, int type) {
      int a = 0;
      Iterator var6 = this.getPlayer(chr).blocktype.iterator();

      while(var6.hasNext()) {
         Pair<Integer, Integer> block = (Pair)var6.next();
         if ((Integer)block.getLeft() == type) {
            a = (Integer)block.getRight();
            break;
         }
      }

      this.getPlayer(chr).blocktype.set(type, new Pair(type, a - 1));
      int index = 0;

      for(Iterator var15 = pyramidcheck.iterator(); var15.hasNext(); ++index) {
         Triple<Integer, Integer, Integer> check = (Triple)var15.next();
         if ((Integer)check.getLeft() == floor && (Integer)check.getMid() == pos && (Integer)check.getRight() == -1) {
            pyramidcheck.set(index, new Triple(floor, pos, type));
            break;
         }
      }

      this.getPlayer(chr).setPoint(this.getPlayer(chr).getPoint() + 1);
      if (this.MonsterPyramidTimer != null) {
         this.MonsterPyramidTimer.cancel(false);
      }

      List<MapleCharacter> chrs = new ArrayList();
      Iterator var17 = this.getPlayers().iterator();

      while(var17.hasNext()) {
         MonsterPyramid.PyramidPlayer pp = (MonsterPyramid.PyramidPlayer)var17.next();
         pp.setRank(2);
         Iterator var10 = this.getPlayers().iterator();

         while(var10.hasNext()) {
            MonsterPyramid.PyramidPlayer ppp = (MonsterPyramid.PyramidPlayer)var10.next();
            if (pp.getPoint() > ppp.getPoint()) {
               pp.setRank(pp.getRank() - 1);
            }
         }

         if (pp != null) {
            chrs.add(pp.b);
         }
      }

      int ntturn = this.nextTurn(chr.getMonsterPyramidInstance().getPlayer(chr));
      int passcount = 0;
      boolean passcheck = false;
      Iterator var21 = this.getPlayers().iterator();

      MonsterPyramid.PyramidPlayer pp;
      while(var21.hasNext()) {
         pp = (MonsterPyramid.PyramidPlayer)var21.next();
         if (pp.turn == ntturn) {
            this.MonsterPyramidTimer = Timer.EventTimer.getInstance().schedule(() -> {
               this.skipPlayer(pp);
            }, 11000L);
            if (this.nextTurnCheck(pp)) {
               passcheck = true;
            }
            break;
         }
      }

      MonsterPyramid.PyramidPlayer pp;
      int blockcount;
      Iterator var23;
      if (passcheck) {
         passcheck = false;
         blockcount = ntturn - 1;
         if (blockcount < 0) {
            blockcount = 2;
         }

         var23 = this.getPlayers().iterator();

         while(var23.hasNext()) {
            pp = (MonsterPyramid.PyramidPlayer)var23.next();
            pp.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(chrs, true, 1, 3, blockcount, this.round, 0, 0, 0, 3, 3, 0));
         }

         ++ntturn;
         if (ntturn >= 3) {
            ntturn = 0;
         }

         var23 = this.getPlayers().iterator();

         while(var23.hasNext()) {
            pp = (MonsterPyramid.PyramidPlayer)var23.next();
            if (pp.turn == ntturn) {
               ++ntturn;
               if (this.nextTurnCheck(pp) && ntturn >= 3) {
                  ntturn = 0;
                  pp.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(chrs, true, 1, 3, ntturn, this.round, 0, 0, 0, 3, 3, 0));
               }
               break;
            }
         }
      }

      var21 = this.getPlayers().iterator();

      while(var21.hasNext()) {
         pp = (MonsterPyramid.PyramidPlayer)var21.next();
         if (this.nextTurnCheck(pp)) {
            ++passcount;
         }
      }

      var21 = this.getPlayers().iterator();

      while(var21.hasNext()) {
         pp = (MonsterPyramid.PyramidPlayer)var21.next();
         pp.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(chrs, false, 1, 3, ntturn, this.round, floor, pos, type, 13, 1, 6));
      }

      blockcount = 0;

      Pair block;
      for(var23 = this.getPlayer(chr).blocktype.iterator(); var23.hasNext(); blockcount += (Integer)block.getRight()) {
         block = (Pair)var23.next();
      }

      if (passcount >= 3 || blockcount == 0) {
         var23 = this.getPlayers().iterator();

         while(var23.hasNext()) {
            pp = (MonsterPyramid.PyramidPlayer)var23.next();
            if (this.round < 3) {
               if (blockcount == 0) {
                  pp.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(chrs, true, 1, 6, ntturn, this.round, ntturn - 1));
               }

               pp.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(chrs, true, 1, 4, pp.turn, this.round, 5, 0, 9));
            }
         }

         this.nextStage(ntturn, blockcount == 0);
      }

   }

   public void nextStage(int ntturn, boolean used) {
      ++this.round;
      if (this.MonsterPyramidTimer != null) {
         this.MonsterPyramidTimer.cancel(true);
         this.MonsterPyramidTimer = null;
      }

      ArrayList list;
      Iterator var4;
      MonsterPyramid.PyramidPlayer pp;
      if (this.round >= 4) {
         list = new ArrayList();
         var4 = this.getPlayers().iterator();

         while(var4.hasNext()) {
            pp = (MonsterPyramid.PyramidPlayer)var4.next();
            list.add(pp.b);
         }

         var4 = this.getPlayers().iterator();

         while(var4.hasNext()) {
            pp = (MonsterPyramid.PyramidPlayer)var4.next();
            pp.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(list, true, 1, 4, pp.turn, this.round, 5, 0, 9));
            pp.setRank(2);
            Iterator var6 = this.getPlayers().iterator();

            while(var6.hasNext()) {
               MonsterPyramid.PyramidPlayer ppp = (MonsterPyramid.PyramidPlayer)var6.next();
               if (pp.getPoint() > ppp.getPoint()) {
                  pp.setRank(pp.getRank() - 1);
               }
            }

            if (pp.getRank() == 0) {
               pp.b.setKeyValue(100668, "Rank", "1");
               pp.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(list, true, 1, 3, pp.turn, 3, 0, 0, 0, 11, 0, 0, 0));
            } else if (pp.getRank() == 1) {
               pp.b.setKeyValue(100668, "Rank", "2");
               pp.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(list, true, 1, 3, pp.turn, 3, 0, 0, 0, 12, 0, 0, 0));
            } else {
               pp.b.setKeyValue(100668, "Rank", "3");
               pp.b.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(list, true, 1, 3, pp.turn, 3, 0, 0, 0, 12, 0, 0, 0));
            }
         }

         Timer.EtcTimer.getInstance().schedule(() -> {
            this.endGame((MapleCharacter)null, false);
         }, 4000L);
      } else {
         pyramidcheck.clear();

         for(int i = 0; i < 8; ++i) {
            for(int i2 = 0; i2 < 8 - i; ++i2) {
               pyramidcheck.add(new Triple(i, i2, -1));
            }
         }

         list = new ArrayList();
         var4 = this.getPlayers().iterator();

         while(var4.hasNext()) {
            pp = (MonsterPyramid.PyramidPlayer)var4.next();
            list.add(pp.b);
         }

         Timer.EtcTimer.getInstance().schedule(() -> {
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
               MapleCharacter p = (MapleCharacter)var3.next();
               p.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(list, false, 1, 5, ntturn, this.round));
            }

         }, 4000L);
         Timer.EtcTimer.getInstance().schedule(() -> {
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
               MapleCharacter p = (MapleCharacter)var3.next();
               p.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(list, true, 1, 4, ntturn, this.round, 0, 0, 0, 8, 3, 6));
               p.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(list, true, 1, 4, ntturn, this.round, 8, 0, 6));
               p.getClient().send(SLFCGPacket.MonsterPyramidPacket.Handler(list, true, 1, 3, ntturn, this.round, 0, 0, 0, 8, 3, 6));
            }

         }, 6000L);
      }

   }

   public boolean nextTurnCheck(MonsterPyramid.PyramidPlayer p) {
      boolean pass = false;
      int insertnow = 0;
      Iterator var4 = this.getPlayers().iterator();

      label89:
      while(var4.hasNext()) {
         MonsterPyramid.PyramidPlayer pp = (MonsterPyramid.PyramidPlayer)var4.next();
         if (pp.b.getId() == p.b.getId()) {
            Iterator var6 = pyramidcheck.iterator();

            while(true) {
               Triple check;
               Iterator var8;
               Pair block;
               do {
                  do {
                     if (!var6.hasNext()) {
                        var6 = pyramidcheck.iterator();

                        while(true) {
                           do {
                              do {
                                 if (!var6.hasNext()) {
                                    var6 = pp.blocktype.iterator();

                                    while(true) {
                                       if (!var6.hasNext()) {
                                          break label89;
                                       }

                                       Pair<Integer, Integer> block = (Pair)var6.next();
                                       if ((Integer)block.getLeft() == 5 && (Integer)block.getRight() > 0) {
                                          ++insertnow;
                                       }
                                    }
                                 }

                                 check = (Triple)var6.next();
                              } while((Integer)check.getLeft() <= 0);
                           } while((Integer)check.getRight() != -1);

                           var8 = pp.blocktype.iterator();

                           while(var8.hasNext()) {
                              block = (Pair)var8.next();
                              if (this.test((Integer)check.getLeft(), (Integer)check.getMid(), (Integer)block.getLeft())) {
                                 insertnow += (Integer)block.getRight();
                              }
                           }
                        }
                     }

                     check = (Triple)var6.next();
                  } while((Integer)check.getLeft() != 0);
               } while((Integer)check.getRight() != -1);

               for(var8 = pp.blocktype.iterator(); var8.hasNext(); insertnow += (Integer)block.getRight()) {
                  block = (Pair)var8.next();
               }
            }
         }
      }

      if (insertnow <= 0) {
         pass = true;
      }

      return pass;
   }

   public boolean test(int floor, int pos, int type) {
      boolean set = false;
      int a = -1;
      int b = -1;
      Iterator var7 = pyramidcheck.iterator();

      Triple check;
      while(var7.hasNext()) {
         check = (Triple)var7.next();
         if (floor - 1 == (Integer)check.getLeft() && (Integer)check.getMid() == pos) {
            a = (Integer)check.getRight();
            break;
         }
      }

      var7 = pyramidcheck.iterator();

      while(var7.hasNext()) {
         check = (Triple)var7.next();
         if (floor - 1 == (Integer)check.getLeft() && (Integer)check.getMid() == pos + 1) {
            b = (Integer)check.getRight();
            break;
         }
      }

      if ((type == a || type == b) && b != -1 && a != -1 && a != 5 && b != 5) {
         set = true;
      }

      return set;
   }

   public int nextTurn(MonsterPyramid.PyramidPlayer p) {
      int a = 0;
      Iterator var3 = this.getPlayers().iterator();

      while(var3.hasNext()) {
         MonsterPyramid.PyramidPlayer pp = (MonsterPyramid.PyramidPlayer)var3.next();
         if (pp.b.getId() == p.b.getId()) {
            a = p.turn;
            break;
         }
      }

      ++a;
      if (a >= 3) {
         a = 0;
      }

      return a;
   }

   public class PyramidPlayer {
      private byte a;
      private int point = 0;
      private int rank = 0;
      private int turn = 0;
      private MapleCharacter b;
      private List<Pair<Integer, Integer>> blocktype = new ArrayList();

      public PyramidPlayer(MapleCharacter b, byte a, boolean select6, int turn) {
         this.b = b;
         this.a = a;
         this.turn = turn;
      }

      public void randBlocks() {
         List<Pair<Integer, Integer>> blockadd = new ArrayList();
         blockadd.add(new Pair(0, 7));
         blockadd.add(new Pair(1, 7));
         blockadd.add(new Pair(2, 7));
         blockadd.add(new Pair(3, 7));
         blockadd.add(new Pair(4, 7));
         blockadd.add(new Pair(5, 1));
         int i2 = 0;

         for(Iterator var3 = MonsterPyramid.this.getPlayers().iterator(); var3.hasNext(); ++i2) {
            MonsterPyramid.PyramidPlayer p = (MonsterPyramid.PyramidPlayer)var3.next();
            p.blocktype.clear();
            if (p.blocktype.isEmpty()) {
               for(int i = 0; i < 6; ++i) {
                  p.blocktype.add(new Pair(i, 0));
               }
            }

            p.randblock(blockadd, p, i2);
         }

      }

      public List<Pair<Integer, Integer>> randblock(List<Pair<Integer, Integer>> blockadd, MonsterPyramid.PyramidPlayer p, int n) {
         boolean stop = false;
         boolean pass = false;
         int beforepblocksizex = false;
         int after = 0;
         int typeblocksize = 0;
         Iterator var9;
         Pair blcx;
         if (n >= 2) {
            var9 = p.blocktype.iterator();

            while(var9.hasNext()) {
               blcx = (Pair)var9.next();
               Iterator var17 = blockadd.iterator();

               while(var17.hasNext()) {
                  Pair<Integer, Integer> blc = (Pair)var17.next();
                  if (blcx.getLeft() == blc.getLeft()) {
                     p.blocktype.set((Integer)blc.getLeft(), new Pair((Integer)blc.getLeft(), (Integer)blc.getRight()));
                  }
               }
            }
         } else {
            int beforepblocksize;
            do {
               if (stop) {
                  return blockadd;
               }

               beforepblocksize = 0;

               for(var9 = p.blocktype.iterator(); var9.hasNext(); beforepblocksize += (Integer)blcx.getRight()) {
                  blcx = (Pair)var9.next();
               }

               if (beforepblocksize < 12) {
                  var9 = blockadd.iterator();

                  label113:
                  while(true) {
                     int type;
                     int rand;
                     Iterator var13;
                     Pair bt;
                     do {
                        do {
                           if (!var9.hasNext()) {
                              break label113;
                           }

                           blcx = (Pair)var9.next();
                        } while((Integer)blcx.getRight() <= 0);

                        type = (Integer)blcx.getLeft();
                        rand = 2;
                        var13 = p.blocktype.iterator();

                        while(var13.hasNext()) {
                           bt = (Pair)var13.next();
                           if (bt.getLeft() == blcx.getLeft()) {
                              typeblocksize = (Integer)bt.getRight();
                              break;
                           }
                        }

                        int su;
                        if (typeblocksize + rand > 4) {
                           su = beforepblocksize + rand - 4;
                           rand -= su;
                        }

                        if (beforepblocksize + rand > 12) {
                           su = beforepblocksize + rand - 12;
                           rand -= su;
                        }

                        if (rand > (Integer)blcx.getRight()) {
                           rand = (Integer)blcx.getRight();
                        }
                     } while(rand <= 0);

                     blockadd.set(type, new Pair(type, (Integer)blcx.getRight() - rand));
                     beforepblocksize += rand;
                     var13 = p.blocktype.iterator();

                     while(var13.hasNext()) {
                        bt = (Pair)var13.next();
                        if ((Integer)bt.getLeft() == type) {
                           p.blocktype.set(type, new Pair(type, (Integer)bt.getRight() + rand));
                        }
                     }
                  }
               }

               ++after;
            } while(beforepblocksize < 12 && after <= 100000);

            if (after > 100000) {
               var9 = MonsterPyramid.this.getPlayers().iterator();

               while(var9.hasNext()) {
                  MonsterPyramid.PyramidPlayer pp = (MonsterPyramid.PyramidPlayer)var9.next();
                  pp.b.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo("오류발생! 게임을 다시 시작하여 주세요!", 3, 20, 20));
               }

               MonsterPyramid.this.endGame(p.getPlayer(), false);
            }

            stop = true;
         }

         return blockadd;
      }

      public MapleCharacter getPlayer() {
         return this.b;
      }

      public byte getPosition() {
         return this.a;
      }

      public List<Pair<Integer, Integer>> getBlocks() {
         return this.blocktype;
      }

      public int getSelectBlockType(int type) {
         int a = 0;
         Iterator var3 = this.blocktype.iterator();

         while(var3.hasNext()) {
            Pair<Integer, Integer> block = (Pair)var3.next();
            if ((Integer)block.getLeft() == type) {
               a = (Integer)block.getRight();
               break;
            }
         }

         return a;
      }

      public void setPosition(byte a) {
         this.a = a;
      }

      public int getPoint() {
         return this.point;
      }

      public void setPoint(int a) {
         this.point = a;
      }

      public int getRank() {
         return this.rank;
      }

      public void setRank(int a) {
         this.rank = a;
      }
   }

   public static class MonsterPyramidMatchingInfo {
      public List<MapleCharacter> players = new ArrayList();

      public MonsterPyramidMatchingInfo(List<MapleCharacter> list) {
         Iterator iterator = list.iterator();

         while(iterator.hasNext()) {
            this.players.add((MapleCharacter)iterator.next());
         }

      }
   }
}
