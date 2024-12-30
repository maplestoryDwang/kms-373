package server.games;

import client.MapleCharacter;
import constants.ServerConstants;
import handling.channel.ChannelServer;
import handling.world.World;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import server.Randomizer;
import server.Timer;
import tools.Pair;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.SLFCGPacket;

public class OneCardGame {
   private int objectId = 100000;
   private int fire;
   private List<OneCardGame.OneCard> oneCardDeckInfo = new ArrayList();
   private boolean bClockWiseTurn = true;
   private OneCardGame.OneCard lastCard;
   private OneCardGame.OneCardPlayer lastPlayer;
   public static List<MapleCharacter> oneCardMatchingQueue = new ArrayList();
   private ScheduledFuture<?> oneCardTimer = null;
   private List<OneCardGame.OneCardPlayer> Players = new ArrayList();
   private Point lastPoint = new Point(0, 0);

   public OneCardGame(List<MapleCharacter> chrs) {
      for(int i = 0; i < chrs.size(); ++i) {
         this.getPlayers().add(new OneCardGame.OneCardPlayer((MapleCharacter)chrs.get(i), (byte)i));
      }

   }

   public OneCardGame.OneCardPlayer getPlayer(MapleCharacter chr) {
      Iterator var2 = this.getPlayers().iterator();

      OneCardGame.OneCardPlayer ocp;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         ocp = (OneCardGame.OneCardPlayer)var2.next();
      } while(ocp.chr.getId() != chr.getId());

      return ocp;
   }

   public void resetDeck() {
      List<Pair<Integer, Integer>> cardData = new ArrayList();
      Iterator var2 = this.getPlayers().iterator();

      while(var2.hasNext()) {
         OneCardGame.OneCardPlayer ocp = (OneCardGame.OneCardPlayer)var2.next();
         Iterator var4 = ocp.cards.iterator();

         while(var4.hasNext()) {
            OneCardGame.OneCard card = (OneCardGame.OneCard)var4.next();
            cardData.add(new Pair(card.color, card.type));
         }
      }

      if (this.lastCard != null) {
         cardData.add(new Pair(this.lastCard.color, this.lastCard.type));
      }

      for(int i = 0; i <= 3; ++i) {
         for(int k = 0; k <= 12; ++k) {
            if (!cardData.contains(new Pair(i, k))) {
               this.oneCardDeckInfo.add(new OneCardGame.OneCard(new int[]{++this.objectId, i, k}));
            }
         }
      }

      this.oneCardDeckInfo.add(new OneCardGame.OneCard(new int[]{++this.objectId, 4, 12}));
   }

   private void StartGame(MapleCharacter chr) {
      this.oneCardDeckInfo.clear();
      this.fire = 0;
      this.resetDeck();
      OneCardGame.OneCardPlayer first = (OneCardGame.OneCardPlayer)this.Players.get(Randomizer.nextInt(this.Players.size()));
      this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onStart(this.getPlayers()));
      Iterator var3 = this.getPlayers().iterator();

      while(var3.hasNext()) {
         OneCardGame.OneCardPlayer bp = (OneCardGame.OneCardPlayer)var3.next();
         List<OneCardGame.OneCard> newcards = new ArrayList();

         for(int i = 0; i < 5; ++i) {
            int num = Randomizer.nextInt(this.oneCardDeckInfo.size());
            OneCardGame.OneCard card = (OneCardGame.OneCard)this.oneCardDeckInfo.get(num);
            bp.cards.add(card);
            newcards.add(card);
            this.oneCardDeckInfo.remove(num);
         }

         this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onGetCardResult(bp, newcards));
      }

      OneCardGame.OneCard firstCard;
      for(firstCard = (OneCardGame.OneCard)this.oneCardDeckInfo.get(Randomizer.nextInt(this.oneCardDeckInfo.size())); firstCard.type >= 6; firstCard = (OneCardGame.OneCard)this.oneCardDeckInfo.get(Randomizer.nextInt(this.oneCardDeckInfo.size()))) {
      }

      this.lastCard = firstCard;
      this.lastPlayer = first;
      this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onPutCardResult((OneCardGame.OneCardPlayer)null, firstCard));
      this.oneCardDeckInfo.remove(firstCard);
      List<OneCardGame.OneCard> possibleCards = new ArrayList();
      Iterator var11 = first.cards.iterator();

      while(true) {
         OneCardGame.OneCard card;
         do {
            label105:
            do {
               while(var11.hasNext()) {
                  card = (OneCardGame.OneCard)var11.next();
                  if (card.getType() <= 5) {
                     continue label105;
                  }

                  if (card.getType() <= 7) {
                     if (this.getFire() == 0) {
                        if (card.getColor() == this.getLastCard().getColor() || card.getType() == this.getLastCard().getType()) {
                           possibleCards.add(card);
                        }
                     } else if (card.getType() == 6) {
                        if (card.getColor() == this.getLastCard().getColor() && (card.getType() == 6 || card.getType() == 7)) {
                           possibleCards.add(card);
                        }
                     } else if (card.getColor() == this.getLastCard().getColor() && card.getType() == 7) {
                        possibleCards.add(card);
                     }
                  } else if (card.getType() <= 11) {
                     if ((card.getColor() == this.getLastCard().getColor() || card.getType() == this.getLastCard().getType()) && this.getFire() == 0) {
                        possibleCards.add(card);
                     }
                  } else {
                     switch(card.getColor()) {
                     case 0:
                        if (this.getLastCard().getColor() == card.getColor()) {
                           possibleCards.add(card);
                        }
                        break;
                     case 1:
                        possibleCards.add(card);
                        break;
                     case 2:
                        if (this.getLastCard().getColor() == card.getColor() && this.getFire() == 0) {
                           possibleCards.add(card);
                        }
                        break;
                     case 3:
                        if (this.getLastCard().getColor() == card.getColor() && this.getFire() == 0) {
                           possibleCards.add(card);
                        }
                        break;
                     case 4:
                        possibleCards.add(card);
                     }
                  }
               }

               this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(first, possibleCards, first.cards.size() < 17, this.isbClockWiseTurn()));
               if (this.getOneCardTimer() != null) {
                  this.getOneCardTimer().cancel(false);
               }

               this.setOneCardTimer(Timer.EventTimer.getInstance().schedule(() -> {
                  this.skipPlayer();
               }, 15000L));
               return;
            } while(card.getColor() != this.getLastCard().getColor() && card.getType() != this.getLastCard().getType());
         } while(this.getLastCard().getType() > 5 && (this.getLastCard().getType() < 8 || this.getLastCard().getType() > 11));

         if (this.getFire() == 0) {
            possibleCards.add(card);
         }
      }
   }

   public void skipPlayer() {
      List<OneCardGame.OneCard> newcards = new ArrayList();

      for(int i = 0; i < (this.getFire() > 0 ? this.getFire() : 1); ++i) {
         if (this.getOneCardDeckInfo().size() == 0) {
            this.resetDeck();
            if (this.getOneCardDeckInfo().size() == 0) {
               break;
            }
         }

         int num = Randomizer.nextInt(this.getOneCardDeckInfo().size());
         OneCardGame.OneCard card = (OneCardGame.OneCard)this.getOneCardDeckInfo().get(num);
         this.getLastPlayer().getCards().add(card);
         newcards.add(card);
         this.getOneCardDeckInfo().remove(num);
      }

      this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onGetCardResult(this.getLastPlayer(), newcards));
      if (this.getLastPlayer().getCards().size() >= 17) {
         this.setFire(0);
         this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/gameover"));
         this.getLastPlayer().chr.getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/gameover"));
         this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(5, 0, this.getLastPlayer().chr.getId(), true));
         this.playerDead(this.getLastPlayer(), false);
      } else if (this.getFire() > 0) {
         this.setFire(0);
         this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(4, 0, this.getLastPlayer().chr.getId(), false));
      }

      if (this.getLastCard().getType() == 11) {
         this.setbClockWiseTurn(!this.isbClockWiseTurn());
      }

      OneCardGame.OneCardPlayer nextPlayer = this.setNextPlayer(this.getLastPlayer(), this.isbClockWiseTurn());
      this.setLastPlayer(nextPlayer);
      if (nextPlayer != null) {
         List<OneCardGame.OneCard> possibleCards = new ArrayList();
         Iterator var8 = nextPlayer.getCards().iterator();

         while(true) {
            OneCardGame.OneCard card;
            do {
               label146:
               do {
                  while(var8.hasNext()) {
                     card = (OneCardGame.OneCard)var8.next();
                     if (card.getType() <= 5) {
                        continue label146;
                     }

                     if (card.getType() <= 7) {
                        if (this.getFire() == 0) {
                           if (card.getColor() == this.getLastCard().getColor() || card.getType() == this.getLastCard().getType()) {
                              possibleCards.add(card);
                           }
                        } else if (card.getType() == 6) {
                           if (card.getColor() == this.getLastCard().getColor() && (card.getType() == 6 || card.getType() == 7)) {
                              possibleCards.add(card);
                           }
                        } else if (card.getColor() == this.getLastCard().getColor() && card.getType() == 7) {
                           possibleCards.add(card);
                        }
                     } else if (card.getType() <= 11) {
                        if (card.getColor() == this.getLastCard().getColor() || card.getType() == this.getLastCard().getType()) {
                           possibleCards.add(card);
                        }
                     } else {
                        switch(card.getColor()) {
                        case 0:
                           if (this.getLastCard().getColor() == card.getColor()) {
                              possibleCards.add(card);
                           }
                           break;
                        case 1:
                           possibleCards.add(card);
                           break;
                        case 2:
                           if (this.getLastCard().getColor() == card.getColor()) {
                              possibleCards.add(card);
                           }
                           break;
                        case 3:
                           if (this.getLastCard().getColor() == card.getColor()) {
                              possibleCards.add(card);
                           }
                           break;
                        case 4:
                           possibleCards.add(card);
                        }
                     }
                  }

                  if (this.getOneCardDeckInfo().size() != 0 && !this.getOneCardDeckInfo().isEmpty()) {
                     this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(nextPlayer, possibleCards, possibleCards.size() == 0 && nextPlayer.getCards().size() == 16 || nextPlayer.getCards().size() < 16, this.isbClockWiseTurn()));
                  } else {
                     this.resetDeck();
                     this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onEffectResult(0, 0, 0, false));
                     this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(nextPlayer, possibleCards, possibleCards.size() == 0 && nextPlayer.getCards().size() == 16 || nextPlayer.getCards().size() < 16, this.isbClockWiseTurn()));
                  }

                  nextPlayer.getPlayer().getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/myturn"));
                  nextPlayer.getPlayer().getClient().getSession().writeAndFlush(CWvsContext.enableActions(nextPlayer.getPlayer()));
                  nextPlayer.getPlayer().getClient().getSession().writeAndFlush(SLFCGPacket.onShowText("당신의 턴입니다."));
                  if (this.getOneCardTimer() != null) {
                     this.getOneCardTimer().cancel(false);
                  }

                  this.setOneCardTimer(Timer.MapTimer.getInstance().schedule(() -> {
                     this.skipPlayer();
                  }, 15000L));
                  return;
               } while(card.getColor() != this.getLastCard().getColor() && card.getType() != this.getLastCard().getType());
            } while(this.getLastCard().getType() > 5 && (this.getLastCard().getType() < 8 || this.getLastCard().getType() > 11));

            possibleCards.add(card);
         }
      }
   }

   public void endGame(OneCardGame.OneCardPlayer winner, boolean error) {
      Iterator var3 = this.getPlayers().iterator();

      while(var3.hasNext()) {
         OneCardGame.OneCardPlayer bp = (OneCardGame.OneCardPlayer)var3.next();
         MapleCharacter var10000;
         long var10003;
         if (bp.getPlayer().getId() == winner.getPlayer().getId()) {
            if (bp.getPlayer().getKeyValue(501215, "today") <= 0L) {
               bp.getPlayer().setKeyValue(501215, "today", "0");
            }

            if (bp.getPlayer().getKeyValue(501215, "today") >= 50L) {
               bp.getPlayer().dropMessage(-8, "하루동안 [미니게임 플레이]로 획득 가능한 보라코인 갯수를 초과하였습니다.");
            } else {
               var10000 = bp.getPlayer();
               var10003 = bp.getPlayer().getKeyValue(501215, "today");
               var10000.setKeyValue(501215, "today", (var10003 + 10L).makeConcatWithConstants<invokedynamic>(var10003 + 10L));
               bp.getPlayer().dropMessage(-8, "게임에서 승리하여 보라코인 10개를 획득하였습니다.");
               bp.getPlayer().gainCabinetItemPlayer(4310029, 10, 1, "미니게임 보상 입니다. 보관 기간 내에 수령하지 않을 시 보관함에서 사라집니다.");
            }

            this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/victory"));
         } else {
            if (bp.getPlayer().getKeyValue(501215, "today") <= 0L) {
               bp.getPlayer().setKeyValue(501215, "today", "0");
            }

            if (bp.getPlayer().getKeyValue(501215, "today") >= 50L) {
               bp.getPlayer().dropMessage(-8, "하루동안 [미니게임 플레이]로 획득 가능한 보라코인 갯수를 초과하였습니다.");
            } else {
               var10000 = bp.getPlayer();
               var10003 = bp.getPlayer().getKeyValue(501215, "today");
               var10000.setKeyValue(501215, "today", (var10003 + 5L).makeConcatWithConstants<invokedynamic>(var10003 + 5L));
               bp.getPlayer().dropMessage(-8, "게임에서 패배하여 보라코인 5개를 획득하였습니다.");
               bp.getPlayer().gainCabinetItemPlayer(4310029, 5, 1, "미니게임 보상 입니다. 보관 기간 내에 수령하지 않을 시 보관함에서 사라집니다.");
            }

            this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onShowScreenEffect("/Effect/screeneff/gameover"));
         }
      }

      this.sendPacketToPlayers(SLFCGPacket.onShowText(winner.chr.getName() + "님의 승리! 게임이 종료됩니다."));
      if (this.getOneCardTimer() != null) {
         this.getOneCardTimer().cancel(false);
      }

      Timer.EventTimer.getInstance().schedule(() -> {
         Iterator var1 = this.getPlayers().iterator();

         while(var1.hasNext()) {
            OneCardGame.OneCardPlayer bp = (OneCardGame.OneCardPlayer)var1.next();
            this.sendPacketToPlayers(SLFCGPacket.leaveResult(bp.position));
         }

      }, 5000L);
      Timer.EventTimer.getInstance().schedule(() -> {
         Iterator var1 = this.getPlayers().iterator();

         while(var1.hasNext()) {
            OneCardGame.OneCardPlayer bp = (OneCardGame.OneCardPlayer)var1.next();
            bp.chr.warp(ServerConstants.warpMap);
            bp.chr.setOneCardInstance((OneCardGame)null);
         }

      }, 10000L);
   }

   public void playerDead(OneCardGame.OneCardPlayer player, boolean exit) {
      Iterator var3 = player.cards.iterator();

      while(var3.hasNext()) {
         OneCardGame.OneCard card = (OneCardGame.OneCard)var3.next();
         this.oneCardDeckInfo.add(card);
      }

      List<OneCardGame.OneCardPlayer> players = new ArrayList();
      Iterator var9 = this.getPlayers().iterator();

      OneCardGame.OneCardPlayer chr;
      while(var9.hasNext()) {
         chr = (OneCardGame.OneCardPlayer)var9.next();
         if (chr.getPosition() != player.getPosition()) {
            players.add(chr);
         }
      }

      this.getPlayers().clear();
      var9 = players.iterator();

      while(var9.hasNext()) {
         chr = (OneCardGame.OneCardPlayer)var9.next();
         this.getPlayers().add(chr);
      }

      player.chr.warp(ServerConstants.warpMap);
      player.chr.setOneCardInstance((OneCardGame)null);
      if (this.getPlayers().size() == 1) {
         this.endGame((OneCardGame.OneCardPlayer)this.getPlayers().get(0), exit);
      } else if (this.getLastPlayer().position == player.position) {
         boolean check = false;

         for(int i = 0; i < this.getPlayers().size(); ++i) {
            if (((OneCardGame.OneCardPlayer)this.getPlayers().get(i)).position == (this.isbClockWiseTurn() ? player.getPosition() + 1 : player.getPosition() - 1)) {
               this.setLastPlayer((OneCardGame.OneCardPlayer)this.getPlayers().get(i));
               check = true;
               break;
            }
         }

         if (!check) {
            check = true;
            if (this.isbClockWiseTurn()) {
               this.setLastPlayer((OneCardGame.OneCardPlayer)this.getPlayers().get(0));
            } else {
               this.setLastPlayer((OneCardGame.OneCardPlayer)this.getPlayers().get(this.getPlayers().size() - 1));
            }
         }

         if (check) {
            List<OneCardGame.OneCard> possibleCards = new ArrayList();
            Iterator var6;
            OneCardGame.OneCard card;
            if (this.getLastCard().getColor() == 4) {
               var6 = this.getLastPlayer().getCards().iterator();

               label129:
               while(true) {
                  while(true) {
                     if (!var6.hasNext()) {
                        break label129;
                     }

                     card = (OneCardGame.OneCard)var6.next();
                     if (this.getFire() == 0) {
                        possibleCards.add(card);
                     } else if (card.getType() == 6 || card.getType() == 7 || card.getType() == 12 && card.getColor() == 0) {
                        possibleCards.add(card);
                     }
                  }
               }
            } else {
               var6 = this.getLastPlayer().getCards().iterator();

               label170:
               while(true) {
                  do {
                     while(true) {
                        if (!var6.hasNext()) {
                           break label170;
                        }

                        card = (OneCardGame.OneCard)var6.next();
                        if (card.getType() <= 5) {
                           break;
                        }

                        if (card.getType() <= 7) {
                           if (this.getFire() == 0) {
                              if (card.getColor() == this.getLastCard().getColor() || card.getType() == this.getLastCard().getType()) {
                                 possibleCards.add(card);
                              }
                           } else if (card.getType() == 6) {
                              if (this.getLastCard().getType() == 6) {
                                 possibleCards.add(card);
                              } else if (this.getLastCard().getType() == 7 && card.getColor() == this.getLastCard().getColor()) {
                                 possibleCards.add(card);
                              }
                           } else if (card.getColor() == this.getLastCard().getColor()) {
                              if (this.getLastCard().getType() == 6 || this.getLastCard().getType() == 7) {
                                 possibleCards.add(card);
                              }
                           } else if (this.getLastCard().getType() == 7) {
                              possibleCards.add(card);
                           }
                        } else if (card.getType() <= 11) {
                           if ((card.getColor() == this.getLastCard().getColor() || card.getType() == this.getLastCard().getType()) && this.getFire() == 0) {
                              possibleCards.add(card);
                           }
                        } else {
                           switch(card.getColor()) {
                           case 0:
                              if (this.getLastCard().getColor() == card.getColor() || this.getFire() > 0) {
                                 possibleCards.add(card);
                              }
                              break;
                           case 1:
                              possibleCards.add(card);
                              break;
                           case 2:
                              if (this.getLastCard().getColor() == card.getColor() && this.getFire() == 0) {
                                 possibleCards.add(card);
                              }
                              break;
                           case 3:
                              if (this.getLastCard().getColor() == card.getColor() && this.getFire() == 0) {
                                 possibleCards.add(card);
                              }
                              break;
                           case 4:
                              possibleCards.add(card);
                           }
                        }
                     }
                  } while(card.getColor() != this.getLastCard().getColor() && card.getType() != this.getLastCard().getType());

                  if (this.getFire() == 0) {
                     possibleCards.add(card);
                  }
               }
            }

            this.sendPacketToPlayers(SLFCGPacket.OneCardGamePacket.onUserPossibleAction(this.getLastPlayer(), possibleCards, possibleCards.size() == 0 && this.getLastPlayer().getCards().size() == 16 || this.getLastPlayer().getCards().size() < 16, this.isbClockWiseTurn()));
            this.getLastPlayer().getPlayer().getClient().getSession().writeAndFlush(CField.playSound("Sound/MiniGame.img/oneCard/myturn"));
            this.getLastPlayer().getPlayer().getClient().getSession().writeAndFlush(CWvsContext.enableActions(this.getLastPlayer().getPlayer()));
         }
      }

   }

   public static void addQueue(MapleCharacter chr, int req) {
      if (!oneCardMatchingQueue.contains(chr)) {
         oneCardMatchingQueue.add(chr);
         if (oneCardMatchingQueue.size() == req) {
            OneCardGame.oneCardMatchingInfo info = new OneCardGame.oneCardMatchingInfo(oneCardMatchingQueue);
            List<MapleCharacter> chrs = new ArrayList();
            Iterator var4 = info.players.iterator();

            while(var4.hasNext()) {
               MapleCharacter player = (MapleCharacter)var4.next();
               oneCardMatchingQueue.remove(player);
               player.warp(910044100);
               chrs.add(player);
            }

            Timer.EventTimer.getInstance().schedule(() -> {
               OneCardGame ocg = new OneCardGame(chrs);
               Iterator var3 = chrs.iterator();

               while(var3.hasNext()) {
                  MapleCharacter p = (MapleCharacter)var3.next();
                  p.setOneCardInstance(ocg);
               }

               var3 = ocg.getPlayers().iterator();

               while(var3.hasNext()) {
                  OneCardGame.OneCardPlayer ocp = (OneCardGame.OneCardPlayer)var3.next();
                  ocp.chr.getClient().getSession().writeAndFlush(SLFCGPacket.OneCardGamePacket.CreateUI(ocp.chr, ocp.position, chrs));
               }

               Timer.EventTimer.getInstance().schedule(() -> {
                  ocg.StartGame(chr);
               }, 3000L);
            }, 5000L);
         } else {
            String var10002 = chr.getName();
            World.Broadcast.broadcastSmega(CWvsContext.serverNotice(19, "", var10002 + "님이 원카드 대기열에 캐릭터를 등록했습니다. 남은 인원 : " + (req - oneCardMatchingQueue.size())));
         }
      }

   }

   public static void addQueueParty(MapleCharacter leader) {
      List<MapleCharacter> players = new ArrayList();
      Iterator var2 = leader.getParty().getMembers().iterator();

      while(var2.hasNext()) {
         MaplePartyCharacter p = (MaplePartyCharacter)var2.next();
         int ch = World.Find.findChannel(p.getId());
         if (ch > 0) {
            MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(p.getId());
            if (!oneCardMatchingQueue.contains(chr)) {
               oneCardMatchingQueue.remove(chr);
            }

            players.add(chr);
         }
      }

      OneCardGame.oneCardMatchingInfo info = new OneCardGame.oneCardMatchingInfo(players);
      Iterator var7 = info.players.iterator();

      while(var7.hasNext()) {
         MapleCharacter player = (MapleCharacter)var7.next();
         oneCardMatchingQueue.remove(player);
         player.warp(910044100);
      }

      Timer.EventTimer.getInstance().schedule(() -> {
         OneCardGame ocg = new OneCardGame(players);
         Iterator var2 = players.iterator();

         while(var2.hasNext()) {
            MapleCharacter p = (MapleCharacter)var2.next();
            p.setOneCardInstance(ocg);
         }

         var2 = ocg.getPlayers().iterator();

         while(var2.hasNext()) {
            OneCardGame.OneCardPlayer ocp = (OneCardGame.OneCardPlayer)var2.next();
            ocp.chr.getClient().getSession().writeAndFlush(SLFCGPacket.OneCardGamePacket.CreateUI(ocp.chr, ocp.position, players));
         }

         Timer.EventTimer.getInstance().schedule(() -> {
            ocg.StartGame((MapleCharacter)players.get(0));
         }, 3000L);
      }, 5000L);
   }

   public void sendPacketToPlayers(byte[] packet) {
      Iterator var2 = this.Players.iterator();

      while(var2.hasNext()) {
         OneCardGame.OneCardPlayer player = (OneCardGame.OneCardPlayer)var2.next();
         player.getPlayer().getClient().getSession().writeAndFlush(packet);
      }

   }

   public Point getLastPoint() {
      return this.lastPoint;
   }

   public void setLastPoint(Point lastPoint) {
      this.lastPoint = lastPoint;
   }

   public int getObjectId() {
      return this.objectId;
   }

   public void setObjectId(int objectId) {
      this.objectId = objectId;
   }

   public List<OneCardGame.OneCard> getOneCardDeckInfo() {
      return this.oneCardDeckInfo;
   }

   public void setOneCardDeckInfo(List<OneCardGame.OneCard> oneCardDeckInfo) {
      this.oneCardDeckInfo = oneCardDeckInfo;
   }

   public boolean isbClockWiseTurn() {
      return this.bClockWiseTurn;
   }

   public void setbClockWiseTurn(boolean bClockWiseTurn) {
      this.bClockWiseTurn = bClockWiseTurn;
   }

   public int getFire() {
      return this.fire;
   }

   public void setFire(int fire) {
      this.fire = fire;
   }

   public OneCardGame.OneCard getLastCard() {
      return this.lastCard;
   }

   public void setLastCard(OneCardGame.OneCard lastCard) {
      this.lastCard = lastCard;
   }

   public List<OneCardGame.OneCardPlayer> getPlayers() {
      return this.Players;
   }

   public void setPlayers(List<OneCardGame.OneCardPlayer> players) {
      this.Players = players;
   }

   public ScheduledFuture<?> getOneCardTimer() {
      return this.oneCardTimer;
   }

   public void setOneCardTimer(ScheduledFuture<?> oneCardTimer) {
      this.oneCardTimer = oneCardTimer;
   }

   public void setLastPlayer(OneCardGame.OneCardPlayer ocp) {
      this.lastPlayer = ocp;
   }

   public OneCardGame.OneCardPlayer getLastPlayer() {
      return this.lastPlayer;
   }

   public OneCardGame.OneCardPlayer setNextPlayer(OneCardGame.OneCardPlayer lastPlayer, boolean isClock) {
      OneCardGame.OneCardPlayer nextPlayer = null;
      Iterator var4;
      OneCardGame.OneCardPlayer pp;
      int i;
      if (isClock) {
         if (this.getPlayers().size() == 2) {
            var4 = this.getPlayers().iterator();

            while(var4.hasNext()) {
               pp = (OneCardGame.OneCardPlayer)var4.next();
               if (pp.getPlayer().getId() != lastPlayer.getPlayer().getId()) {
                  nextPlayer = pp;
               }
            }
         } else {
            for(i = 0; i < this.getPlayers().size(); ++i) {
               if (((OneCardGame.OneCardPlayer)this.getPlayers().get(i)).getPosition() == lastPlayer.getPosition()) {
                  if (i == this.getPlayers().size() - 1) {
                     nextPlayer = (OneCardGame.OneCardPlayer)this.getPlayers().get(0);
                  } else {
                     nextPlayer = (OneCardGame.OneCardPlayer)this.getPlayers().get(i + 1);
                  }
               }
            }
         }
      } else if (this.getPlayers().size() == 2) {
         var4 = this.getPlayers().iterator();

         while(var4.hasNext()) {
            pp = (OneCardGame.OneCardPlayer)var4.next();
            if (pp.getPlayer().getId() != lastPlayer.getPlayer().getId()) {
               nextPlayer = pp;
            }
         }
      } else {
         for(i = 0; i < this.getPlayers().size(); ++i) {
            if (((OneCardGame.OneCardPlayer)this.getPlayers().get(i)).getPosition() == lastPlayer.getPosition()) {
               if (i == 0) {
                  nextPlayer = (OneCardGame.OneCardPlayer)this.getPlayers().get(this.getPlayers().size() - 1);
               } else {
                  nextPlayer = (OneCardGame.OneCardPlayer)this.getPlayers().get(i - 1);
               }
            }
         }
      }

      return nextPlayer;
   }

   public class OneCardPlayer {
      private byte position;
      private List<OneCardGame.OneCard> cards;
      private MapleCharacter chr;

      public OneCardPlayer(MapleCharacter player, byte position) {
         this.chr = player;
         this.cards = new ArrayList();
         this.position = position;
      }

      public MapleCharacter getPlayer() {
         return this.chr;
      }

      public List<OneCardGame.OneCard> getCards() {
         return this.cards;
      }

      public void setCards(List<OneCardGame.OneCard> cards) {
         this.cards = cards;
      }

      public byte getPosition() {
         return this.position;
      }

      public void setPosition(byte position) {
         this.position = position;
      }
   }

   public class OneCard {
      private int color;
      private int type;
      private int objectId;

      public OneCard(int... args) {
         this.objectId = args[0];
         this.color = args[1];
         this.setType(args[2]);
      }

      public int getColor() {
         return this.color;
      }

      public void setColor(int a) {
         this.color = a;
      }

      public int getType() {
         return this.type;
      }

      public void setType(int type) {
         this.type = type;
      }

      public int getObjectId() {
         return this.objectId;
      }

      public void setObjectId(int objectId) {
         this.objectId = objectId;
      }
   }

   public static class oneCardMatchingInfo {
      public List<MapleCharacter> players = new ArrayList();

      public oneCardMatchingInfo(List<MapleCharacter> chrs) {
         Iterator var2 = chrs.iterator();

         while(var2.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var2.next();
            this.players.add(chr);
         }

      }
   }
}
