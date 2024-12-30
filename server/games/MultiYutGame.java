package server.games;

import client.MapleCharacter;
import client.MapleCoolDownValueHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import server.Randomizer;
import server.Timer;
import tools.packet.CField;
import tools.packet.SLFCGPacket;

public class MultiYutGame {
   private int objectId = 1;
   public static List<MapleCharacter> multiYutMagchingQueue = new ArrayList();
   public static List<MapleCharacter> multiYutMagchingQueue2 = new ArrayList();
   private ScheduledFuture<?> multiYutTimer = null;
   private List<MultiYutGame.MultiYutPlayer> players = new ArrayList();
   private MultiYutGame.MultiYutPlayer whoTurn = null;

   public MultiYutGame(List<MapleCharacter> chrs) {
      MapleCharacter first = Randomizer.nextBoolean() ? (MapleCharacter)chrs.get(1) : (MapleCharacter)chrs.get(0);

      for(int i = 0; i < chrs.size(); ++i) {
         if (first.getId() == ((MapleCharacter)chrs.get(i)).getId()) {
            this.getPlayers().add(new MultiYutGame.MultiYutPlayer((MapleCharacter)chrs.get(i), (byte)0));
         } else {
            this.getPlayers().add(new MultiYutGame.MultiYutPlayer((MapleCharacter)chrs.get(i), (byte)1));
         }
      }

   }

   public MultiYutGame.MultiYutPlayer getPlayer(MapleCharacter chr) {
      Iterator var2 = this.getPlayers().iterator();

      MultiYutGame.MultiYutPlayer ocp;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         ocp = (MultiYutGame.MultiYutPlayer)var2.next();
      } while(ocp.chr.getId() != chr.getId());

      return ocp;
   }

   public MultiYutGame.MultiYutPlayer getOpponent(MapleCharacter chr) {
      Iterator var2 = this.getPlayers().iterator();

      MultiYutGame.MultiYutPlayer ocp;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         ocp = (MultiYutGame.MultiYutPlayer)var2.next();
      } while(ocp.chr.getId() == chr.getId());

      return ocp;
   }

   private List<MultiYutGame.MultiYutPlayer> getPlayers() {
      return this.players;
   }

   public static void StartGame(int n) {
      if (multiYutMagchingQueue.size() == n) {
         MultiYutGame.multiYutMagchingInfo yutmatchingInfo = new MultiYutGame.multiYutMagchingInfo(multiYutMagchingQueue);
         ArrayList<MapleCharacter> chrs = new ArrayList();
         Iterator var3 = yutmatchingInfo.players.iterator();

         while(var3.hasNext()) {
            MapleCharacter mapleCharacter2 = (MapleCharacter)var3.next();
            multiYutMagchingQueue.remove(mapleCharacter2);
            chrs.add(mapleCharacter2);
            mapleCharacter2.warp(993189900);
         }

         Timer.EtcTimer.getInstance().schedule(() -> {
            MultiYutGame myg = new MultiYutGame(chrs);
            Iterator var2 = chrs.iterator();

            while(var2.hasNext()) {
               MapleCharacter p = (MapleCharacter)var2.next();
               p.setMultiYutInstance(myg);
            }

            var2 = myg.getPlayers().iterator();

            while(var2.hasNext()) {
               MultiYutGame.MultiYutPlayer myp = (MultiYutGame.MultiYutPlayer)var2.next();
               MultiYutGame.MultiYutPlayer me = myg.getPlayer(myp.getPlayer());
               MultiYutGame.MultiYutPlayer opponent = myg.getOpponent(myp.getPlayer());
               myp.chr.getClient().getSession().writeAndFlush(SLFCGPacket.MultiYutGamePacket.createUI(me, opponent));
            }

         }, 5000L);
      }

   }

   private void StartGame(MapleCharacter chr) {
      MultiYutGame.MultiYutPlayer var10000;
      if (Randomizer.nextBoolean()) {
         var10000 = (MultiYutGame.MultiYutPlayer)this.getPlayers().get(1);
      } else {
         var10000 = (MultiYutGame.MultiYutPlayer)this.getPlayers().get(0);
      }

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

         mapleCharacter.getClient().send(CField.NPCPacket.getNPCTalk(9062462, (byte)0, "#r#e" + 초 + "분 후#n#k에 미니게임에 참여할 수 있어요!\r\n\r\n#b(미니게임 이용 중 포기를 하는 경우 잠시 동안 미니게임을 이용할 수 없습니다.)#k", "00 00", (byte)0, mapleCharacter.getId()));
      } else if (multiYutMagchingQueue.size() >= 2) {
         mapleCharacter.getClient().send(CField.NPCPacket.getNPCTalk(9062462, (byte)0, "잠시 후에 다시 시도해 주세요.", "00 00", (byte)0, mapleCharacter.getId()));
      } else if (!multiYutMagchingQueue.contains(mapleCharacter)) {
         multiYutMagchingQueue.add(mapleCharacter);
         mapleCharacter.getClient().send(SLFCGPacket.ContentsWaiting(mapleCharacter, 993189800, 11, 2, 1, 18));
         if (multiYutMagchingQueue.size() == n) {
            Iterator var2 = multiYutMagchingQueue.iterator();

            while(var2.hasNext()) {
               MapleCharacter mapleCharacter2 = (MapleCharacter)var2.next();
               mapleCharacter2.getClient().send(SLFCGPacket.ContentsWaiting(mapleCharacter2, 0, 19, 0, 1, 18));
            }
         }
      }

   }

   public void sendPacketToPlayers(byte[] packet) {
      Iterator var2 = this.players.iterator();

      while(var2.hasNext()) {
         MultiYutGame.MultiYutPlayer player = (MultiYutGame.MultiYutPlayer)var2.next();
         player.getPlayer().getClient().getSession().writeAndFlush(packet);
      }

   }

   public ScheduledFuture<?> getMultiYutTimer() {
      return this.multiYutTimer;
   }

   public void setMultiYutTimer(ScheduledFuture<?> multiYutTimer) {
      this.multiYutTimer = multiYutTimer;
   }

   public int getObjectId() {
      return this.objectId;
   }

   public void setObjectId(int objectId) {
      this.objectId = objectId;
   }

   public MultiYutGame.MultiYutPlayer getWhoTurn() {
      return this.whoTurn;
   }

   public void setWhoTurn(MultiYutGame.MultiYutPlayer whoTurn) {
      this.whoTurn = whoTurn;
   }

   public class MultiYutPlayer {
      private byte position;
      private MapleCharacter chr;
      private List<MultiYutGame.PlayersHorses> Horses;
      private List<MultiYutGame.PlayersSkill> skilllist;
      private Map<Integer, Integer> Yut;
      private int TurnStack;

      public MultiYutPlayer() {
      }

      public MultiYutPlayer(MapleCharacter player, byte position) {
         this.chr = player;
         this.position = position;
         this.Yut = new HashMap();

         int i;
         for(i = 1; i < 7; ++i) {
            this.Yut.put(i, 0);
         }

         this.Horses = new ArrayList();
         this.skilllist = new ArrayList();
         this.TurnStack = 0;
         if (position == 0) {
            MultiYutGame.this.whoTurn = this;
         }

         for(i = 0; i < 4; ++i) {
            MultiYutGame.PlayersHorses horse = MultiYutGame.this.new PlayersHorses((byte)i);
            this.Horses.add(horse);
         }

         for(i = 0; i < 2; ++i) {
            MultiYutGame.PlayersSkill skill = MultiYutGame.this.new PlayersSkill(Randomizer.rand(0, 7));
            this.skilllist.add(skill);
         }

      }

      public void ThrowYut() {
         boolean onemore = false;
         boolean noadd = false;
         int type;
         if (Randomizer.isSuccess(5)) {
            type = Randomizer.rand(6, 7);
            if (type == 7) {
               noadd = true;
               onemore = true;
            }
         } else if (Randomizer.isSuccess(10)) {
            type = Randomizer.rand(4, 5);
            onemore = true;
         } else {
            type = Randomizer.rand(1, 3);
         }

         Iterator var4;
         if (!noadd) {
            ++this.TurnStack;
            if (this.Yut.containsKey(type)) {
               var4 = this.Yut.entrySet().iterator();

               while(var4.hasNext()) {
                  Entry<Integer, Integer> yut = (Entry)var4.next();
                  if ((Integer)yut.getKey() == type) {
                     yut.setValue((Integer)yut.getValue() + 1);
                     break;
                  }
               }
            } else {
               this.Yut.put(type, 1);
            }
         } else {
            MultiYutGame.this.whoTurn = MultiYutGame.this.getOpponent(this.chr);
         }

         var4 = MultiYutGame.this.players.iterator();

         while(var4.hasNext()) {
            MultiYutGame.MultiYutPlayer player = (MultiYutGame.MultiYutPlayer)var4.next();
            player.getChr().getClient().send(SLFCGPacket.MultiYutGamePacket.ThrowYut(MultiYutGame.this.players, type - 1, MultiYutGame.this.whoTurn.getPosition(), onemore));
         }

      }

      public void MoveHorse(int horsePos, int movecount, int yuttype) {
         MultiYutGame.PlayersHorses horse = (MultiYutGame.PlayersHorses)this.getHorses().get(horsePos);
         boolean nextturn = true;
         boolean catched = false;
         boolean onemore = false;
         if (horse != null && horse.getInvposition() == horsePos) {
            int beforecount = horse.getNowposition();
            horse.getLayout().clear();
            int ix;
            if (yuttype == 6) {
               horse.getLayout().add(Integer.valueOf(beforecount));
               if (beforecount != 26 && beforecount != 21 && beforecount != 28 && beforecount != 1 && (beforecount != 20 || movecount != 29) && (beforecount != 23 || movecount != 27) && (beforecount != 15 || movecount != 25)) {
                  horse.getLayout().add(beforecount - 1);
               } else {
                  ix = beforecount == 1 ? 20 : (beforecount == 21 ? 5 : (beforecount == 26 ? 10 : (beforecount == 28 ? 23 : (beforecount == 20 ? 29 : (beforecount == 23 ? 27 : (beforecount == 15 ? 25 : 0))))));
                  horse.getLayout().add(ix);
               }
            } else {
               int ixx;
               if (beforecount != 5 && beforecount != 10 && beforecount != 23) {
                  if ((beforecount == 21 || beforecount == 22 || beforecount == 24 || beforecount == 25) && beforecount + yuttype > 25) {
                     ix = beforecount + yuttype - 25;
                     horse.getLayout().add(Integer.valueOf(beforecount));

                     for(ixx = beforecount; ixx < beforecount + yuttype; ++ixx) {
                        if (beforecount < 25) {
                           horse.getLayout().add(ixx);
                        }
                     }

                     for(ixx = 15; ixx < 15 + ix; ++ixx) {
                        horse.getLayout().add(ixx);
                     }
                  } else if (beforecount != 26 && beforecount != 27) {
                     for(ix = beforecount; ix <= beforecount + yuttype; ++ix) {
                        if (ix >= 21 && (beforecount <= 20 || beforecount >= 30)) {
                           horse.getLayout().add(31);
                        } else if (ix == 30) {
                           horse.getLayout().add(20);
                        } else {
                           horse.getLayout().add(ix);
                        }
                     }
                  } else if (beforecount == 26 && yuttype >= 2) {
                     horse.getLayout().add(26);
                     horse.getLayout().add(27);
                     horse.getLayout().add(23);
                     switch(yuttype) {
                     case 3:
                        horse.getLayout().add(28);
                        break;
                     case 4:
                        horse.getLayout().add(28);
                        horse.getLayout().add(29);
                        break;
                     case 5:
                        horse.getLayout().add(28);
                        horse.getLayout().add(29);
                        horse.getLayout().add(20);
                     }
                  } else if (beforecount == 27 && yuttype >= 1) {
                     horse.getLayout().add(27);
                     switch(yuttype) {
                     case 2:
                        horse.getLayout().add(23);
                        horse.getLayout().add(28);
                        break;
                     case 3:
                        horse.getLayout().add(23);
                        horse.getLayout().add(28);
                        horse.getLayout().add(29);
                        break;
                     case 4:
                     case 5:
                        horse.getLayout().add(23);
                        horse.getLayout().add(28);
                        horse.getLayout().add(29);
                        horse.getLayout().add(20);
                        if (yuttype == 5) {
                           horse.getLayout().add(31);
                        }
                     }
                  }
               } else {
                  ix = beforecount == 5 ? 21 : (beforecount == 10 ? 26 : (beforecount == 23 ? 28 : 0));
                  if (beforecount == 10) {
                     if (yuttype >= 3) {
                        horse.getLayout().add(Integer.valueOf(beforecount));
                        horse.getLayout().add(26);
                        horse.getLayout().add(27);
                        if (yuttype == 3) {
                           horse.getLayout().add(23);
                        } else if (yuttype == 4) {
                           horse.getLayout().add(23);
                           horse.getLayout().add(28);
                        } else if (yuttype == 5) {
                           horse.getLayout().add(23);
                           horse.getLayout().add(28);
                           horse.getLayout().add(29);
                        }
                     } else {
                        horse.getLayout().add(Integer.valueOf(beforecount));

                        for(ixx = ix; ixx < ix + yuttype; ++ixx) {
                           horse.getLayout().add(ixx);
                        }
                     }
                  } else {
                     horse.getLayout().add(Integer.valueOf(beforecount));

                     for(ixx = ix; ixx < ix + yuttype; ++ixx) {
                        horse.getLayout().add(ixx);
                     }
                  }
               }
            }

            Iterator var13 = horse.getLayout().iterator();

            while(var13.hasNext()) {
               Integer i = (Integer)var13.next();
               if (i > 30) {
                  horse.setFinish(true);
                  break;
               }
            }

            System.out.println(beforecount + " : " + horse.getLayout());
            var13 = this.Yut.entrySet().iterator();

            while(var13.hasNext()) {
               Entry<Integer, Integer> yut = (Entry)var13.next();
               if ((Integer)yut.getKey() == yuttype && (Integer)yut.getValue() > 0) {
                  yut.setValue((Integer)yut.getValue() - 1);
               }
            }

            horse.setNowposition((byte)movecount);
            MultiYutGame.PlayersHorses hor;
            if (horse.getOverlap().size() > 0) {
               var13 = this.getHorses().iterator();

               while(var13.hasNext()) {
                  hor = (MultiYutGame.PlayersHorses)var13.next();
                  Iterator var11 = horse.getOverlap().entrySet().iterator();

                  while(var11.hasNext()) {
                     Entry<Integer, Integer> overlap = (Entry)var11.next();
                     if (hor.getInvposition() == (Integer)overlap.getKey()) {
                        hor.setNowposition(horse.getNowposition());
                     }
                  }
               }
            }

            var13 = MultiYutGame.this.getOpponent(this.getChr()).getHorses().iterator();

            while(var13.hasNext()) {
               hor = (MultiYutGame.PlayersHorses)var13.next();
               if (hor.getNowposition() == horse.getNowposition() && !hor.isFinish()) {
                  hor.getOverlap().clear();
                  hor.setNowposition((byte)0);
                  hor.setOverlapOwner(false);
                  catched = true;
                  nextturn = false;
               }
            }

            if (nextturn) {
               MultiYutGame.this.whoTurn = MultiYutGame.this.getOpponent(this.getChr());
               onemore = true;
            }

            var13 = this.getHorses().iterator();

            while(var13.hasNext()) {
               hor = (MultiYutGame.PlayersHorses)var13.next();
               if (hor.getInvposition() != horse.getInvposition() && hor.getNowposition() == horse.getNowposition()) {
                  if (hor.getOverlap().size() > 0) {
                     if (hor.isOverlapOwner() && !hor.getOverlap().containsKey(horse.getInvposition())) {
                        hor.getOverlap().put(Integer.valueOf(horse.getInvposition()), 0);
                        horse.getOverlap().put(Integer.valueOf(hor.getInvposition()), 0);
                        break;
                     }
                  } else if (hor.getOverlap().size() <= 0) {
                     hor.setOverlapOwner(true);
                     hor.getOverlap().put(Integer.valueOf(horse.getInvposition()), 0);
                     horse.getOverlap().put(Integer.valueOf(hor.getInvposition()), 0);
                     break;
                  }
               }
            }

            var13 = MultiYutGame.this.players.iterator();

            while(var13.hasNext()) {
               MultiYutGame.MultiYutPlayer player = (MultiYutGame.MultiYutPlayer)var13.next();
               player.getChr().getClient().send(SLFCGPacket.MultiYutGamePacket.MovedHorse(MultiYutGame.this.players, this, horse.getLayout(), beforecount, MultiYutGame.this.whoTurn.getPosition(), horsePos, catched, onemore));
            }
         }

      }

      public boolean Myturn(MultiYutGame.MultiYutPlayer player) {
         boolean my = false;
         if (MultiYutGame.this.whoTurn.getChr().getId() == player.getChr().getId()) {
            my = true;
         }

         return my;
      }

      public MapleCharacter getPlayer() {
         return this.chr;
      }

      public byte getPosition() {
         return this.position;
      }

      public void setPosition(byte position) {
         this.position = position;
      }

      public MapleCharacter getChr() {
         return this.chr;
      }

      public void setChr(MapleCharacter chr) {
         this.chr = chr;
      }

      public List<MultiYutGame.PlayersHorses> getHorses() {
         return this.Horses;
      }

      public void setHorses(List<MultiYutGame.PlayersHorses> Horses) {
         this.Horses = Horses;
      }

      public List<MultiYutGame.PlayersSkill> getSkilllist() {
         return this.skilllist;
      }

      public void setSkilllist(List<MultiYutGame.PlayersSkill> skilllist) {
         this.skilllist = skilllist;
      }

      public Map<Integer, Integer> getYut() {
         return this.Yut;
      }

      public void setYut(Map<Integer, Integer> Yut) {
         this.Yut = Yut;
      }

      public int getTurnStack() {
         return this.TurnStack;
      }

      public void setTurnStack(int TurnStack) {
         this.TurnStack = TurnStack;
      }
   }

   public static class multiYutMagchingInfo {
      public List<MapleCharacter> players = new ArrayList();

      public multiYutMagchingInfo(List<MapleCharacter> chrs) {
         Iterator var2 = chrs.iterator();

         while(var2.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var2.next();
            this.players.add(chr);
         }

      }
   }

   public class PlayersSkill extends MultiYutGame.MultiYutPlayer {
      private int skill;
      boolean skillused;

      public PlayersSkill(int skill) {
         super();
         this.skill = skill;
         this.skillused = false;
      }

      public int getSkill() {
         return this.skill;
      }

      public void setSkill(int skill) {
         this.skill = skill;
      }

      public boolean isSkillused() {
         return this.skillused;
      }

      public void setSkillused(boolean skillused) {
         this.skillused = skillused;
      }
   }

   public class PlayersHorses extends MultiYutGame.MultiYutPlayer {
      private byte nowposition = 0;
      private byte invposition;
      private boolean OverlapOwner;
      private boolean finish;
      private Map<Integer, Integer> overlap;
      private List<Integer> layout;

      public PlayersHorses(byte i) {
         super();
         this.invposition = i;
         this.overlap = new HashMap();
         this.layout = new ArrayList();
         this.OverlapOwner = false;
         this.finish = false;
      }

      public byte getNowposition() {
         return this.nowposition;
      }

      public void setNowposition(byte nowposition) {
         this.nowposition = nowposition;
      }

      public byte getInvposition() {
         return this.invposition;
      }

      public void setInvposition(byte invposition) {
         this.invposition = invposition;
      }

      public boolean isOverlapOwner() {
         return this.OverlapOwner;
      }

      public void setOverlapOwner(boolean OverlapOwner) {
         this.OverlapOwner = OverlapOwner;
      }

      public Map<Integer, Integer> getOverlap() {
         return this.overlap;
      }

      public List<Integer> getLayout() {
         return this.layout;
      }

      public void setLayout(List<Integer> layout) {
         this.layout = layout;
      }

      public boolean isFinish() {
         return this.finish;
      }

      public void setFinish(boolean finish) {
         this.finish = finish;
      }
   }
}
