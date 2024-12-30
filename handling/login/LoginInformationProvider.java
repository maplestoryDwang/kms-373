package handling.login;

import constants.GameConstants;
import constants.ServerConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Triple;

public class LoginInformationProvider {
   private static final LoginInformationProvider instance = new LoginInformationProvider();
   protected final List<String> ForbiddenName = new ArrayList();
   protected final Map<Triple<Integer, Integer, Integer>, List<Integer>> makeCharInfo = new HashMap();

   public static LoginInformationProvider getInstance() {
      return instance;
   }

   protected LoginInformationProvider() {
      String WZpath = System.getProperty("wz");
      MapleDataProvider prov = MapleDataProviderFactory.getDataProvider(new File(WZpath + "/Etc.wz"));
      MapleData nameData = prov.getData("ForbiddenName.img");
      Iterator var4 = nameData.getChildren().iterator();

      MapleData uA;
      while(var4.hasNext()) {
         uA = (MapleData)var4.next();
         this.ForbiddenName.add(MapleDataTool.getString(uA));
      }

      nameData = prov.getData("Curse.img");
      var4 = nameData.getChildren().iterator();

      while(var4.hasNext()) {
         uA = (MapleData)var4.next();
         this.ForbiddenName.add(MapleDataTool.getString(uA).split(",")[0]);
      }

      MapleData infoData = prov.getData("MakeCharInfo.img");
      Iterator var26 = infoData.iterator();

      label134:
      while(var26.hasNext()) {
         MapleData dat = (MapleData)var26.next();

         try {
            int type;
            if (dat.getName().equals("000_1")) {
               type = LoginInformationProvider.JobType.DualBlade.type;
            } else if (dat.getName().equals("000_3")) {
               type = LoginInformationProvider.JobType.pathFinder.type;
            } else if (dat.getName().equals("10112")) {
               type = LoginInformationProvider.JobType.Zero.type;
            } else {
               type = LoginInformationProvider.JobType.getById(Integer.parseInt(dat.getName())).type;
            }

            Iterator var8 = dat.iterator();

            while(true) {
               MapleData d;
               byte val;
               while(true) {
                  if (!var8.hasNext()) {
                     continue label134;
                  }

                  d = (MapleData)var8.next();
                  if (d.getName().contains("female")) {
                     val = 1;
                     break;
                  }

                  if (d.getName().contains("male")) {
                     val = 0;
                     break;
                  }
               }

               Iterator var11 = d.iterator();

               label128:
               while(var11.hasNext()) {
                  MapleData da = (MapleData)var11.next();
                  int index = Integer.parseInt(da.getName());
                  Triple<Integer, Integer, Integer> key = new Triple(Integer.valueOf(val), index, type);
                  List<Integer> our = (List)this.makeCharInfo.get(key);
                  if (our == null) {
                     our = new ArrayList();
                     this.makeCharInfo.put(key, our);
                  }

                  Iterator var16 = da.iterator();

                  while(true) {
                     while(true) {
                        if (!var16.hasNext()) {
                           continue label128;
                        }

                        MapleData dd = (MapleData)var16.next();
                        if (dd.getName().equalsIgnoreCase("color")) {
                           Iterator var18 = dd.iterator();

                           while(var18.hasNext()) {
                              MapleData dda = (MapleData)var18.next();
                              Iterator var34 = dda.iterator();

                              while(var34.hasNext()) {
                                 MapleData ddd = (MapleData)var34.next();
                                 ((List)our).add(MapleDataTool.getInt(ddd, -1));
                              }
                           }
                        } else {
                           try {
                              ((List)our).add(MapleDataTool.getInt(dd, -1));
                           } catch (Exception var23) {
                              Iterator var19 = dd.iterator();

                              while(var19.hasNext()) {
                                 MapleData dda = (MapleData)var19.next();
                                 Iterator var21 = dda.iterator();

                                 while(var21.hasNext()) {
                                    MapleData ddd = (MapleData)var21.next();
                                    ((List)our).add(MapleDataTool.getInt(ddd, -1));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } catch (NullPointerException | NumberFormatException var24) {
         }
      }

      uA = infoData.getChildByPath("UltimateAdventurer");
      Iterator var27 = uA.iterator();

      while(var27.hasNext()) {
         MapleData dat = (MapleData)var27.next();
         Triple<Integer, Integer, Integer> key = new Triple(-1, Integer.parseInt(dat.getName()), LoginInformationProvider.JobType.UltimateAdventurer.type);
         List<Integer> our = (List)this.makeCharInfo.get(key);
         if (our == null) {
            our = new ArrayList();
            this.makeCharInfo.put(key, our);
         }

         Iterator var31 = dat.iterator();

         while(var31.hasNext()) {
            MapleData d = (MapleData)var31.next();
            ((List)our).add(MapleDataTool.getInt(d, -1));
         }
      }

   }

   public static boolean isExtendedSpJob(int jobId) {
      return GameConstants.isSeparatedSp((short)jobId);
   }

   public final boolean isForbiddenName(String in) {
      Iterator var2 = this.ForbiddenName.iterator();

      String name;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         name = (String)var2.next();
      } while(!in.toLowerCase().contains(name.toLowerCase()));

      return true;
   }

   public final boolean isEligibleItem(int gender, int val, int job, int item) {
      if (item < 0) {
         System.out.println(String.format("item is <0, isEligibleItem(%d, %d, %d, %d)", gender, val, job, item));
         return false;
      } else {
         Triple<Integer, Integer, Integer> key = new Triple(gender, val, job);
         List<Integer> our = (List)this.makeCharInfo.get(key);
         if (our == null) {
            System.out.println(String.format("isEligibleItem(%d, %d, %d, %d)", gender, val, job, item));
            return false;
         } else {
            return our.contains(item);
         }
      }
   }

   public static enum JobType {
      UltimateAdventurer(-1, 0, 100000000, false, false, true, false),
      Resistance(0, 3000, 931000000, false, false, false, false),
      Adventurer(1, 0, 4000000, false, false, false, false),
      Cygnus(2, 1000, 130030000, false, false, false, true),
      Aran(3, 2000, 914000000, false, false, true, false),
      Evan(4, 2001, 900010000, false, false, true, false),
      Mercedes(5, 2002, 910150000, false, false, false, false),
      Demon(6, 3001, 931050310, true, false, false, false),
      Phantom(7, 2003, 915000000, false, false, false, true),
      DualBlade(8, 0, 103050900, false, false, false, false),
      Mihile(9, 5000, 913070000, false, false, true, false),
      Luminous(10, 2004, 101000000, false, false, false, true),
      Kaiser(11, 6000, 0, false, false, false, false),
      AngelicBuster(12, 6001, 940011000, false, false, false, false),
      Cannoneer(13, 1, 0, false, false, true, false),
      Xenon(14, 3002, 931050920, true, false, false, false),
      Zero(15, 10100, 100000000, false, false, false, true),
      EunWol(16, 2005, 552000050, false, false, true, true),
      PinkBean(17, 13000, 0, false, false, false, false),
      Kinesis(18, 14000, 0, false, false, false, false),
      Kadena(19, 6002, 940200405, false, false, false, false),
      Iliume(20, 15000, 940200405, false, false, false, false),
      ark(21, 15001, 940200405, true, false, false, false),
      pathFinder(22, 0, 100000000, false, true, false, false),
      Hoyeong(23, 16000, 100000000, true, false, false, true),
      Adel(24, 15002, 100000000, false, false, false, false),
      Cain(25, 6003, 100000000, false, false, false, false),
      Yeti(26, 13500, 100000000, false, false, false, false),
      Lara(27, 16001, 100000000, false, false, false, false),
      Khali(28, 15003, 100000000, false, true, false, false);

      public int type;
      public int id;
      public int map;
      public boolean hairColor;
      public boolean skinColor;
      public boolean faceMark;
      public boolean hat;
      public boolean bottom;
      public boolean cape;

      private JobType(int type, int id, int map, boolean faceMark, boolean hat, boolean bottom, boolean cape) {
         this.type = type;
         this.id = id;
         this.map = ServerConstants.StartMap;
         this.faceMark = faceMark;
         this.hat = hat;
         this.bottom = bottom;
         this.cape = cape;
      }

      public static LoginInformationProvider.JobType getByType(int g) {
         if (g == Cannoneer.type) {
            return Adventurer;
         } else {
            LoginInformationProvider.JobType[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
               LoginInformationProvider.JobType e = var1[var3];
               if (e.type == g) {
                  return e;
               }
            }

            return null;
         }
      }

      public static LoginInformationProvider.JobType getById(int g) {
         if (g == Adventurer.id) {
            return Adventurer;
         } else {
            LoginInformationProvider.JobType[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
               LoginInformationProvider.JobType e = var1[var3];
               if (e.id == g) {
                  return e;
               }
            }

            return null;
         }
      }
   }
}
