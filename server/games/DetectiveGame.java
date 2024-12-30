package server.games;

import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import handling.channel.ChannelServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import server.Randomizer;
import server.Timer;
import tools.packet.CField;
import tools.packet.SLFCGPacket;

public class DetectiveGame {
   private int Stage = 1;
   private int MessageTime = 3;
   private List<MapleCharacter> Rank = new ArrayList();
   private List<MapleCharacter> Fail = new ArrayList();
   private Map<MapleCharacter, Integer> Players = new HashMap();
   private Map<MapleCharacter, Integer> Answers = new HashMap();
   private ScheduledFuture<?> DetectiveTimer = null;
   private MapleCharacter Owner = null;
   public static boolean isRunning = false;

   public DetectiveGame(MapleCharacter owner, boolean isByAdmin) {
      isRunning = true;
      this.Owner = owner;
      Iterator var3 = ChannelServer.getAllInstances().iterator();

      while(var3.hasNext()) {
         ChannelServer cs = (ChannelServer)var3.next();
         Iterator var5 = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(var5.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var5.next();
            if (isByAdmin) {
               chr.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3003501, 7000, "#face0#운영자가 암호 추리 게임 참여자를 모집 중이야", ""));
            } else {
               chr.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3003156, 7000, "#face0##b#e" + this.Owner.getName() + "#k#n가 암호 추리 게임 참여자를 모집 중이야!", ""));
            }
         }
      }

   }

   public void sendMessage() {
      Iterator var1 = ChannelServer.getAllInstances().iterator();

      while(var1.hasNext()) {
         ChannelServer cs = (ChannelServer)var1.next();
         Iterator var3 = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(var3.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var3.next();
            if (this.Owner.isGM()) {
               chr.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3003501, 5000, "#face0#운영자가 암호 추리 게임 참여자를 모집 중이야!\r\n지금 #r" + this.Owner.getMap().getAllChracater().size() + "명#k이 대기실에 있어!", ""));
            } else {
               chr.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3003156, 5000, "#face0##e#b" + this.Owner.getName() + "#k#n가 암호 추리 게임 참여자를 모집 중이야!\r\n지금 #r" + this.Owner.getMap().getAllChracater().size() + "명#k이 대기실에 있어!", ""));
            }
         }
      }

      --this.MessageTime;
   }

   public void RegisterPlayers(List<MapleCharacter> chars) {
      Iterator var2 = chars.iterator();

      while(var2.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var2.next();
         this.Players.put(chr, 1);
      }

   }

   public void addAttempt(MapleCharacter chr) {
      this.Players.put(chr, (Integer)this.Players.get(chr) + 1);
      int temp = (Integer)this.Players.get(chr);
      if ((Integer)this.Players.get(chr) == 15) {
         this.Fail.add(chr);
      }

   }

   public int getAnswer(MapleCharacter chr) {
      return (Integer)this.Answers.get(chr);
   }

   public List<MapleCharacter> getRanking() {
      return this.Rank;
   }

   public MapleCharacter getOwner() {
      return this.Owner;
   }

   public Set<MapleCharacter> getPlayers() {
      return this.Players.keySet();
   }

   public void addRank(MapleCharacter a1) {
      if (!this.Rank.contains(a1)) {
         this.Rank.add(a1);
         a1.getMap().broadcastMessage(SLFCGPacket.HundredDetectiveGameAddRank(a1.getId(), a1.getName()));
         if (this.Rank.size() == 30 || this.Rank.size() + this.Fail.size() == this.Players.size()) {
            this.DetectiveTimer.cancel(true);
            Iterator var2 = this.Players.keySet().iterator();

            MapleCharacter chr;
            while(var2.hasNext()) {
               chr = (MapleCharacter)var2.next();
               chr.getClient().getSession().writeAndFlush(SLFCGPacket.HundredDetectiveGameControl(4, this.Stage));
               chr.getClient().getSession().writeAndFlush(SLFCGPacket.HundredDetectiveReEnable(16));
               this.Players.put(chr, 1);
            }

            if (this.Stage == 3) {
               this.StopGame();
            } else {
               ++this.Stage;
               this.Answers.clear();
               this.Rank.clear();
               this.Fail.clear();
               var2 = this.Players.keySet().iterator();

               int ranknumber;
               while(var2.hasNext()) {
                  chr = (MapleCharacter)var2.next();
                  ranknumber = this.GetRandomNumber();
                  this.Answers.put(chr, ranknumber);
               }

               Timer.EventTimer.getInstance().schedule(() -> {
                  Iterator var1 = this.Players.keySet().iterator();

                  while(var1.hasNext()) {
                     MapleCharacter chr = (MapleCharacter)var1.next();
                     chr.getClient().getSession().writeAndFlush(SLFCGPacket.HundredDetectiveGameReady(this.Stage));
                     chr.getClient().getSession().writeAndFlush(SLFCGPacket.HundredDetectiveGameControl(2, this.Stage));
                     chr.getClient().getSession().writeAndFlush(SLFCGPacket.HundredDetectiveGameControl(3, this.Stage));
                  }

               }, 5000L);
               var2 = this.Rank.iterator();

               label70:
               while(true) {
                  while(true) {
                     do {
                        if (!var2.hasNext()) {
                           var2 = this.Players.keySet().iterator();

                           while(var2.hasNext()) {
                              chr = (MapleCharacter)var2.next();
                              if (chr != null && !this.Rank.contains(chr) && chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() > 0) {
                                 chr.AddStarDustCoin(2, 2);
                              }
                           }
                           break label70;
                        }

                        chr = (MapleCharacter)var2.next();
                     } while(chr == null);

                     ranknumber = this.Rank.indexOf(chr) + 1;
                     if (ranknumber == 1) {
                        if (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= 10) {
                           chr.AddStarDustCoin(2, 100);
                        }
                     } else if (ranknumber >= 2 && ranknumber <= 10) {
                        if (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= 5) {
                           chr.AddStarDustCoin(2, 20);
                        }
                     } else if (ranknumber >= 11 && ranknumber <= 20) {
                        if (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= 4) {
                           chr.AddStarDustCoin(2, 10);
                        }
                     } else if (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= 3) {
                        chr.AddStarDustCoin(2, 5);
                     }
                  }
               }
            }
         }
      }

   }

   private int GetRandomNumber() {
      ArrayList temp = new ArrayList();

      int a;
      while(temp.size() != 3) {
         for(a = 0; a == 0; a = Randomizer.nextInt(10)) {
         }

         if (!temp.contains(a)) {
            temp.add(a);
         }
      }

      for(a = (Integer)temp.get(0) * 100 + (Integer)temp.get(1) * 10 + (Integer)temp.get(2); this.Answers.containsValue(a); a = this.GetRandomNumber()) {
      }

      return a;
   }

   public void StartGame() {
      Iterator var1 = this.Players.keySet().iterator();

      while(var1.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var1.next();
         chr.getClient().getSession().writeAndFlush(CField.musicChange("BgmEvent2/adventureIsland"));
         chr.getClient().getSession().writeAndFlush(SLFCGPacket.HundredDetectiveGameExplain());
         int Answer = this.GetRandomNumber();
         this.Answers.put(chr, Answer);
      }

      Timer.EventTimer.getInstance().schedule(() -> {
         Iterator var1 = this.Players.keySet().iterator();

         while(var1.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var1.next();
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.HundredDetectiveGameReady(this.Stage));
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.HundredDetectiveGameControl(2, this.Stage));
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.HundredDetectiveGameControl(3, this.Stage));
         }

      }, 40000L);
   }

   public void StopGame() {
      Timer.EventTimer.getInstance().schedule(() -> {
         Iterator var1 = this.Players.keySet().iterator();

         while(var1.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var1.next();
            if (chr != null) {
               chr.warp(993022200);
               chr.setDetectiveGame((DetectiveGame)null);
            }
         }

         isRunning = false;
      }, 5000L);
   }
}
