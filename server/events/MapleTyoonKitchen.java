package server.events;

import client.MapleCharacter;
import client.SecondaryStat;
import client.SecondaryStatValueHolder;
import client.SkillFactory;
import handling.channel.ChannelServer;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import server.Randomizer;
import server.Timer;
import server.life.MapleLifeFactory;
import server.maps.MapleMap;
import tools.Pair;
import tools.Triple;
import tools.packet.CField;
import tools.packet.SLFCGPacket;

public class MapleTyoonKitchen {
   private MapleCharacter cooker;
   private List<MapleTyoonKitchen.MapleTyoonKitchenRecipe> recipes = new ArrayList();
   private MapleTyoonKitchen.MapleTyoonKitchenRecipe recipe;
   private int ownmoney;
   private int itemid;
   private int type;
   private static List<MapleCharacter> Matchinglist = new ArrayList();
   private static List<MapleCharacter> Playerlist = new ArrayList();
   private ScheduledFuture<?> CookTime = null;

   public MapleTyoonKitchen(MapleCharacter cooker) {
      this.cooker = cooker;
      this.ownmoney = 0;
      this.itemid = 0;
      this.type = -1;
      this.recipe = null;
   }

   public static void ResetMtk(MapleCharacter cooker) {
      if (cooker.getMtk() != null) {
         Iterator var1 = cooker.getMtk().getRecipes().iterator();

         while(var1.hasNext()) {
            MapleTyoonKitchen.MapleTyoonKitchenRecipe re = (MapleTyoonKitchen.MapleTyoonKitchenRecipe)var1.next();
            re.getMakeTime().cancel(true);
         }

         cooker.getMtk().getCookTime().cancel(true);
         cooker.getMtk().setCookTime((ScheduledFuture)null);
         cooker.getMtk().getRecipes().clear();
         cooker.setMtk((MapleTyoonKitchen)null);
      }

   }

   public static void SpawnMonster(MapleMap map) {
      map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833966), new Point(474, 112));
      map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833967), new Point(474, 0));
      map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833968), new Point(474, 400));
      map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833968), new Point(660, -400));
      map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833966), new Point(660, 0));
      map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833967), new Point(660, 400));
      map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833967), new Point(838, 112));
      map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833968), new Point(838, 0));
      map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833966), new Point(838, 400));
   }

   public void OtherCookerSet() {
      Iterator var1 = this.cooker.getMap().getAllChracater().iterator();

      while(var1.hasNext()) {
         MapleCharacter cookers = (MapleCharacter)var1.next();
         if (this.cooker.getId() != cookers.getId()) {
            cookers.getMtk().setRecipes(this.recipes);
         }
      }

   }

   public static void startGame() {
   }

   public static void startPartyGame(MapleCharacter chr) {
      if (chr.getParty() != null) {
         ChannelServer.getInstance(chr.getClient().getChannel()).getMapFactory().getMap(993194500).resetFully();
         SpawnMonster(ChannelServer.getInstance(chr.getClient().getChannel()).getMapFactory().getMap(993194500));
         if (chr.getParty().getMembers().size() >= 1 && chr.getParty().getMembers().size() <= 3) {
            Iterator var1 = chr.getParty().getMembers().iterator();

            label43:
            while(true) {
               MaplePartyCharacter pm;
               do {
                  if (!var1.hasNext()) {
                     break label43;
                  }

                  pm = (MaplePartyCharacter)var1.next();
               } while(pm.getPlayer() == null);

               pm.getPlayer().warp(993194500);
               ResetMtk(pm.getPlayer());
               Iterator var3 = pm.getPlayer().getEffects().iterator();

               while(var3.hasNext()) {
                  Pair<SecondaryStat, SecondaryStatValueHolder> data = (Pair)var3.next();
                  SecondaryStatValueHolder mbsvh = (SecondaryStatValueHolder)data.right;
                  if (SkillFactory.getSkill(mbsvh.effect.getSourceId()) != null && mbsvh.effect.getSourceId() != 80002282 && mbsvh.effect.getSourceId() != 2321055) {
                     pm.getPlayer().cancelEffect(mbsvh.effect, Arrays.asList((SecondaryStat)data.left));
                  }
               }

               if (chr.getId() != pm.getPlayer().getId()) {
                  MapleTyoonKitchen mtks = new MapleTyoonKitchen(pm.getPlayer());
                  pm.getPlayer().setMtk(mtks);
                  mtks.setCookTime(Timer.EventTimer.getInstance().schedule(() -> {
                     if (pm.getPlayer() != null && pm.getPlayer().getMtk() != null) {
                        if (pm.getPlayer().getMapId() == 993194500) {
                           ResetMtk(pm.getPlayer());
                           pm.getPlayer().warp(993194400);
                        }

                     }
                  }, 1800000L));
               }
            }
         }

         MapleTyoonKitchen mtk = new MapleTyoonKitchen(chr);
         chr.getMap().broadcastMessage(SLFCGPacket.TyoonKitchenPacket.Handler(1));
         chr.getMap().broadcastMessage(SLFCGPacket.TyoonKitchenPacket.Handler(2));
         chr.getMap().broadcastMessage(SLFCGPacket.TyoonKitchenPacket.Setting());
         chr.getMap().broadcastMessage(CField.environmentChange("UI/TyoonKitchen_UI.img/TyoonKitchen_UI/effect/countdown", 16));
         chr.getMap().broadcastMessage(SLFCGPacket.TyoonKitchenPacket.Handler(3));
         chr.getMap().broadcastMessage(SLFCGPacket.TyoonKitchenPacket.getClock(1800000));
         Timer.EventTimer.getInstance().schedule(() -> {
            for(int i = 0; i < 3; ++i) {
               mtk.getClass();
               List var10000 = mtk.recipes;
               Objects.requireNonNull(mtk);
               var10000.add(mtk.new MapleTyoonKitchenRecipe(i));
            }

            chr.setMtk(mtk);
            mtk.setCookTime(Timer.EventTimer.getInstance().schedule(() -> {
               if (mtk != null && chr != null && chr.getMapId() == 993194500) {
                  if (chr == null || chr.getMtk() == null) {
                     return;
                  }

                  if (chr.getMapId() == 993194500) {
                     ResetMtk(chr);
                     chr.warp(993194400);
                  }
               }

            }, 1800000L));
            chr.getMtk().OtherCookerSet();
            chr.getMap().broadcastMessage(CField.environmentChange("UI/UIMiniGame.img/mapleOneCard/Effect/screeneff/start", 16));
            chr.getMap().broadcastMessage(SLFCGPacket.playSE("Sound/MiniGame.img/oneCard/start"));
            chr.getMap().broadcastMessage(CField.environmentMove("cookPlate1", 0));
            chr.getMap().broadcastMessage(CField.environmentMove("cookPlate2", 0));
            chr.getMap().broadcastMessage(CField.environmentMove("cookPlate3", 0));
            chr.getMap().broadcastMessage(SLFCGPacket.TyoonKitchenPacket.Menu(mtk.recipes, getAllMtk(chr)));
         }, 4000L);
      }

   }

   public static List<MapleTyoonKitchen> getAllMtk(MapleCharacter chr) {
      List<MapleTyoonKitchen> list = new ArrayList();
      Iterator var2 = chr.getMap().getAllChracater().iterator();

      while(var2.hasNext()) {
         MapleCharacter chrs = (MapleCharacter)var2.next();
         if (chrs.getMtk() != null) {
            list.add(chrs.getMtk());
         }
      }

      return list;
   }

   public static int getAllMoney(MapleCharacter chr) {
      int money = 0;
      Iterator var2 = chr.getMap().getAllChracater().iterator();

      while(var2.hasNext()) {
         MapleCharacter chrs = (MapleCharacter)var2.next();
         if (chrs.getMtk() != null) {
            money += chrs.getMtk().getOwnmoney();
         }
      }

      return money;
   }

   public MapleCharacter getCooker() {
      return this.cooker;
   }

   public void setCooker(MapleCharacter cooker) {
      this.cooker = cooker;
   }

   public int getOwnmoney() {
      return this.ownmoney;
   }

   public void setOwnmoney(int ownmoney) {
      this.ownmoney = ownmoney;
   }

   public List<MapleTyoonKitchen.MapleTyoonKitchenRecipe> getRecipes() {
      return this.recipes;
   }

   public void setRecipes(List<MapleTyoonKitchen.MapleTyoonKitchenRecipe> recipes) {
      this.recipes = recipes;
   }

   public int getItemid() {
      return this.itemid;
   }

   public void setItemid(int itemid) {
      this.itemid = itemid;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public MapleTyoonKitchen.MapleTyoonKitchenRecipe getRecipe() {
      return this.recipe;
   }

   public void setRecipe(MapleTyoonKitchen.MapleTyoonKitchenRecipe recipe) {
      this.recipe = recipe;
   }

   public ScheduledFuture<?> getCookTime() {
      return this.CookTime;
   }

   public void setCookTime(ScheduledFuture<?> CookTime) {
      this.CookTime = CookTime;
   }

   public class MapleTyoonKitchenRecipe {
      private int type;
      private int destination;
      private int makenum = 0;
      private int recipe;
      private int money;
      private int type2;
      private ScheduledFuture<?> makeTime = null;
      private long limittime;
      private List<Triple<Integer, Integer, Integer>> RecipeInfo = new ArrayList();

      public MapleTyoonKitchenRecipe(int number) {
         this.reset(true, number, false);
      }

      public void reset(int number) {
         this.reset(false, number, false);
      }

      public void reset(boolean first, int number, boolean update) {
         this.RecipeInfo.clear();
         if (MapleTyoonKitchen.this.cooker != null) {
            this.type = 1;
            this.type2 = 0;
            this.makenum = number;
            this.destination = Randomizer.rand(0, 4);
            this.recipe = Randomizer.rand(2024028, 2024071);
            int level = Randomizer.rand(1, 3);
            if (first) {
               level = number + 1;
            }

            int maketime = 0;
            if (level == 1) {
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024025, 2024027), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.money = 3000;
               maketime = 60000;
               this.limittime = System.currentTimeMillis() + 60000L;
            } else if (level == 2) {
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024025, 2024027), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024025, 2024027), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.limittime = System.currentTimeMillis() + 90000L;
               maketime = 90000;
               this.money = 4500;
            } else if (level == 3) {
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024025, 2024027), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024025, 2024027), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024025, 2024027), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024020, 2024024), number, 0));
               this.RecipeInfo.add(new Triple(Randomizer.rand(2024025, 2024027), number, 0));
               this.limittime = System.currentTimeMillis() + 120000L;
               maketime = 120000;
               this.money = 6000;
            }

            if (update) {
               MapleTyoonKitchen.this.OtherCookerSet();
            }

            if (maketime > 0) {
               this.setMakeTime(Timer.EventTimer.getInstance().schedule(() -> {
                  if (MapleTyoonKitchen.this.cooker != null && MapleTyoonKitchen.this.cooker.getMtk() != null) {
                     if (this.RecipeInfo != null) {
                        if (MapleTyoonKitchen.this.cooker.getMapId() != 993194500) {
                           this.getMakeTime().cancel(true);
                           this.setMakeTime((ScheduledFuture)null);
                           MapleTyoonKitchen.this.cooker.setMtk((MapleTyoonKitchen)null);
                        } else {
                           String[] msg = new String[]{"방금 손님은 다시는 우리 가게에 오지 않겠군.", "기다리고 있는 손님이 불쌍하지도 않나?", "이대로라면 식당이 곧 파산하겠군.", "주문이 취소되었네!"};
                           MapleTyoonKitchen.this.cooker.getMap().startMapEffect(msg[Randomizer.rand(0, msg.length - 1)], 5120216, 4000);
                           Iterator var2 = MapleTyoonKitchen.this.cooker.getMap().getAllChracater().iterator();

                           while(var2.hasNext()) {
                              MapleCharacter chrs = (MapleCharacter)var2.next();
                              if (chrs != null && chrs.getMtk().getRecipe() == this) {
                                 chrs.getMtk().setRecipe((MapleTyoonKitchen.MapleTyoonKitchenRecipe)null);
                                 chrs.getMtk().setItemid(0);
                                 chrs.getMtk().setType(-1);
                              }
                           }

                           MapleTyoonKitchen.this.cooker.getMap().broadcastMessage(CField.environmentMove("cookPlate" + (this.getMakenum() + 1), 0));
                           this.getMakeTime().cancel(true);
                           this.setMakeTime((ScheduledFuture)null);
                           this.type = 0;
                           this.type2 = -1;
                           this.destination = -1;
                           this.recipe = 0;
                           this.RecipeInfo.clear();
                           this.limittime = 0L;
                           this.money = 0;
                           MapleTyoonKitchen.this.cooker.getMap().broadcastMessage(SLFCGPacket.TyoonKitchenPacket.Menu(MapleTyoonKitchen.this.recipes, MapleTyoonKitchen.getAllMtk(MapleTyoonKitchen.this.cooker)));
                           Timer.EventTimer.getInstance().schedule(() -> {
                              this.reset(false, this.makenum, true);
                              MapleTyoonKitchen.this.cooker.getMap().broadcastMessage(SLFCGPacket.TyoonKitchenPacket.Menu(MapleTyoonKitchen.this.recipes, MapleTyoonKitchen.getAllMtk(MapleTyoonKitchen.this.cooker)));
                           }, 3000L);
                        }
                     }

                  } else {
                     this.getMakeTime().cancel(true);
                     this.setMakeTime((ScheduledFuture)null);
                  }
               }, (long)maketime));
            }

            if (update) {
               MapleTyoonKitchen.this.cooker.getMap().broadcastMessage(SLFCGPacket.TyoonKitchenPacket.Menu(MapleTyoonKitchen.this.recipes, MapleTyoonKitchen.getAllMtk(MapleTyoonKitchen.this.cooker)));
            }

         }
      }

      public int CookingCheck(int num) {
         int check = 0;
         Iterator var3 = this.RecipeInfo.iterator();

         while(var3.hasNext()) {
            Triple<Integer, Integer, Integer> checks = (Triple)var3.next();
            if ((Integer)checks.getMid() == num && (Integer)checks.getRight() == 1) {
               ++check;
            }
         }

         return check;
      }

      public int getType() {
         return this.type;
      }

      public void setType(int type) {
         this.type = type;
      }

      public int getDestination() {
         return this.destination;
      }

      public void setDestination(int destination) {
         this.destination = destination;
      }

      public int getMakenum() {
         return this.makenum;
      }

      public void setMakenum(int makenum) {
         this.makenum = makenum;
      }

      public int getRecipe() {
         return this.recipe;
      }

      public void setRecipe(int recipe) {
         this.recipe = recipe;
      }

      public int getMoney() {
         return this.money;
      }

      public void setMoney(int money) {
         this.money = money;
      }

      public long getLimittime() {
         return this.limittime;
      }

      public void setLimittime(long limittime) {
         this.limittime = limittime;
      }

      public List<Triple<Integer, Integer, Integer>> getRecipeInfo() {
         return this.RecipeInfo;
      }

      public void setRecipeInfo(List<Triple<Integer, Integer, Integer>> RecipeInfo) {
         this.RecipeInfo = RecipeInfo;
      }

      public ScheduledFuture<?> getMakeTime() {
         return this.makeTime;
      }

      public void setMakeTime(ScheduledFuture<?> makeTime) {
         this.makeTime = makeTime;
      }

      public int getType2() {
         return this.type2;
      }

      public void setType2(int type2) {
         this.type2 = type2;
      }
   }
}
