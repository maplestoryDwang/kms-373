package server;

import constants.ServerConstants;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import tools.Pair;
import tools.Triple;

public class Setting {
   public static void setting() {
      try {
         FileInputStream setting = new FileInputStream("Properties/setMonsterHP.properties");
         Properties setting_ = new Properties();
         setting_.load(setting);
         setting.close();
         ServerConstants.boss.clear();
         String[] mob = setting_.getProperty(toUni("몹코드")).split("\\{");
         int i;
         if (mob != null) {
            String[] var3 = mob;
            i = mob.length;

            for(int var5 = 0; var5 < i; ++var5) {
               String s = var3[var5];
               int monsterid = 0;
               long hp = 0L;
               String[] var10 = s.replaceAll("},", "").replaceAll("}", "").replaceAll(" ", "").split(",");
               int var11 = var10.length;

               for(int var12 = 0; var12 < var11; ++var12) {
                  String s2 = var10[var12];
                  if (s2.length() > 0) {
                     if (monsterid == 0) {
                        monsterid = Integer.parseInt(s2);
                     } else {
                        hp = Long.parseLong(s2);
                     }
                  }
               }

               if (monsterid != 0 && hp != 0L) {
                  ServerConstants.boss.add(new Pair(monsterid, hp));
               }
            }
         }

         List<Pair<Integer, Long>> list = ServerConstants.boss;

         for(i = 0; i < list.size(); ++i) {
         }

         System.out.println(ServerConstants.boss.size() + "개의 몬스터 체력 로딩완료.");
      } catch (Exception var14) {
         var14.printStackTrace();
      }

   }

   public static void setting2() {
      try {
         FileInputStream setting = new FileInputStream("Properties/setMonsterHP2.properties");
         Properties setting_ = new Properties();
         setting_.load(setting);
         setting.close();
         ServerConstants.boss2.clear();
         String[] mob = setting_.getProperty(toUni("몹코드")).split("\\{");
         int i;
         if (mob != null) {
            String[] var3 = mob;
            i = mob.length;

            for(int var5 = 0; var5 < i; ++var5) {
               String s = var3[var5];
               int monsterid = 0;
               long hp = 0L;
               String[] var10 = s.replaceAll("},", "").replaceAll("}", "").replaceAll(" ", "").split(",");
               int var11 = var10.length;

               for(int var12 = 0; var12 < var11; ++var12) {
                  String s2 = var10[var12];
                  if (s2.length() > 0) {
                     if (monsterid == 0) {
                        monsterid = Integer.parseInt(s2);
                     } else {
                        hp = Long.parseLong(s2);
                     }
                  }
               }

               if (monsterid != 0 && hp != 0L) {
                  ServerConstants.boss2.add(new Pair(monsterid, hp));
               }
            }
         }

         List<Pair<Integer, Long>> list = ServerConstants.boss2;

         for(i = 0; i < list.size(); ++i) {
         }

         System.out.println(ServerConstants.boss2.size() + "개의 몬스터 체력 로딩완료.");
      } catch (Exception var14) {
         var14.printStackTrace();
      }

   }

   public static void CashShopSetting() {
      try {
         FileInputStream setting = new FileInputStream("Properties/CashShopMain.properties");
         Properties setting_ = new Properties();
         setting_.load(setting);
         setting.close();
         ServerConstants.CashMainInfo.clear();
         String[] mob = setting_.getProperty(toUni("Info")).split("\\{");
         if (mob != null) {
            String[] var3 = mob;
            int var4 = mob.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               String s = var3[var5];
               int monsterid = 0;
               int hp = 0;
               String[] var9 = s.replaceAll("},", "").replaceAll("}", "").replaceAll(" ", "").split(",");
               int var10 = var9.length;

               for(int var11 = 0; var11 < var10; ++var11) {
                  String s2 = var9[var11];
                  if (s2.length() > 0) {
                     if (monsterid == 0) {
                        monsterid = Integer.parseInt(s2);
                     } else {
                        hp = Integer.parseInt(s2);
                     }
                  }
               }

               if (monsterid != 0 && hp != 0) {
                  ServerConstants.CashMainInfo.add(new Pair(monsterid, hp));
               }
            }
         }

         System.out.println(ServerConstants.CashMainInfo.size() + "개의 캐시샵 정보 로딩 완료.");
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   public static void settingGoldApple() {
      try {
         FileInputStream setting = new FileInputStream("Properties/골드애플.properties");
         Properties setting_ = new Properties();
         setting_.load(setting);
         setting.close();
         ServerConstants.goldapple.clear();
         ServerConstants.Sgoldapple.clear();
         String[] items = setting_.getProperty(toUni("item")).split("\\{");
         String[] Sitems = setting_.getProperty(toUni("item2")).split("\\{");
         ServerConstants.SgoldappleSuc = Integer.parseInt(setting_.getProperty(toUni("rate")));
         String[] var4;
         int var5;
         int var6;
         String s;
         int itemid;
         int count;
         int suc;
         String[] var11;
         int var12;
         int var13;
         String s2;
         if (items != null) {
            var4 = items;
            var5 = items.length;

            for(var6 = 0; var6 < var5; ++var6) {
               s = var4[var6];
               itemid = 0;
               count = 0;
               suc = 0;
               var11 = s.replaceAll("},", "").replaceAll("}", "").replaceAll(" ", "").split(",");
               var12 = var11.length;

               for(var13 = 0; var13 < var12; ++var13) {
                  s2 = var11[var13];
                  if (s2.length() > 0) {
                     if (itemid == 0) {
                        itemid = Integer.parseInt(s2);
                     } else if (count == 0) {
                        count = Integer.parseInt(s2);
                     } else if (suc == 0) {
                        suc = Integer.parseInt(s2);
                     }
                  }
               }

               if (itemid != 0 && count != 0 && suc != 0) {
                  ServerConstants.goldapple.add(new Triple(itemid, count, suc));
               }
            }
         }

         if (Sitems != null) {
            var4 = Sitems;
            var5 = Sitems.length;

            for(var6 = 0; var6 < var5; ++var6) {
               s = var4[var6];
               itemid = 0;
               count = 0;
               suc = 0;
               var11 = s.replaceAll("},", "").replaceAll("}", "").replaceAll(" ", "").split(",");
               var12 = var11.length;

               for(var13 = 0; var13 < var12; ++var13) {
                  s2 = var11[var13];
                  if (s2.length() > 0) {
                     if (itemid == 0) {
                        itemid = Integer.parseInt(s2);
                     } else if (count == 0) {
                        count = Integer.parseInt(s2);
                     } else if (suc == 0) {
                        suc = Integer.parseInt(s2);
                     }
                  }
               }

               if (itemid != 0 && count != 0 && suc != 0) {
                  ServerConstants.Sgoldapple.add(new Triple(itemid, count, suc));
               }
            }
         }

         System.out.println("골드애플 일반 아이템 갯수 : " + ServerConstants.goldapple.size() + "개 로딩 완료!");
         System.out.println("골드애플 스페셜 아이템 갯수 : " + ServerConstants.Sgoldapple.size() + "개 로딩 완료!");
         System.out.println("골드애플 스페셜 아이템 확률 : '" + ServerConstants.SgoldappleSuc + "%' 로딩 완료!");
      } catch (Exception var15) {
         var15.printStackTrace();
      }

   }

   public static void settingNeoPos() {
      try {
         FileInputStream setting = new FileInputStream("Properties/위시코인.properties");
         Properties setting_ = new Properties();
         setting_.load(setting);
         setting.close();
         ServerConstants.NeoPosList.clear();
         String[] info = setting_.getProperty(toUni("몹코드")).split("\\{");
         if (info != null) {
            String[] var3 = info;
            int var4 = info.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               String s = var3[var5];
               int monsterid = 0;
               int count = 0;
               String[] var9 = s.replaceAll("},", "").replaceAll("}", "").replaceAll(" ", "").split(",");
               int var10 = var9.length;

               for(int var11 = 0; var11 < var10; ++var11) {
                  String s2 = var9[var11];
                  if (s2.length() > 0) {
                     if (monsterid == 0) {
                        monsterid = Integer.parseInt(s2);
                     } else {
                        count = Integer.parseInt(s2);
                     }
                  }
               }

               if (monsterid != 0 && count != 0) {
                  ServerConstants.NeoPosList.add(new Pair(monsterid, count));
               }
            }
         }

         System.out.println("위시코인 리스트 : " + ServerConstants.NeoPosList.size() + "개 로딩 완료!");
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   protected static String toUni(String kor) throws UnsupportedEncodingException {
      return new String(kor.getBytes("KSC5601"), "8859_1");
   }
}
