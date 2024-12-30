package handling.channel.handler;

import client.MapleClient;
import client.MapleUnion;
import java.awt.Point;
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
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class UnionHandler {
   public static List<Integer> groupIndex = new ArrayList();
   public static List<Point> boardPos = new ArrayList();
   public static List<Integer> openLevels = new ArrayList();
   public static Map<Integer, Integer> cardSkills = new HashMap();
   public static Map<Integer, Map<Integer, List<Point>>> characterSizes = new HashMap();
   public static List<Integer> skills = new ArrayList();

   public static void loadUnion() {
      String WZpath = System.getProperty("wz");
      MapleDataProvider prov = MapleDataProviderFactory.getDataProvider(new File(WZpath + "/Etc.wz"));
      MapleData nameData = prov.getData("mapleUnion.img");

      try {
         Iterator var3 = nameData.iterator();

         while(true) {
            label86:
            while(var3.hasNext()) {
               MapleData dat = (MapleData)var3.next();
               String name = dat.getName();
               byte var7 = -1;
               switch(name.hashCode()) {
               case -991001942:
                  if (name.equals("CharacterSize")) {
                     var7 = 2;
                  }
                  break;
               case -300141953:
                  if (name.equals("SkillInfo")) {
                     var7 = 3;
                  }
                  break;
               case 2092848:
                  if (name.equals("Card")) {
                     var7 = 1;
                  }
                  break;
               case 397411476:
                  if (name.equals("BoardInfo")) {
                     var7 = 0;
                  }
               }

               Iterator var8;
               MapleData d;
               switch(var7) {
               case 0:
                  var8 = dat.iterator();

                  while(true) {
                     if (!var8.hasNext()) {
                        continue label86;
                     }

                     d = (MapleData)var8.next();
                     groupIndex.add(MapleDataTool.getInt(d.getChildByPath("groupIndex")));
                     boardPos.add(new Point(MapleDataTool.getInt(d.getChildByPath("xPos")), MapleDataTool.getInt(d.getChildByPath("yPos"))));
                     openLevels.add(MapleDataTool.getInt(d.getChildByPath("openLevel")));
                  }
               case 1:
                  var8 = dat.iterator();

                  while(true) {
                     if (!var8.hasNext()) {
                        continue label86;
                     }

                     d = (MapleData)var8.next();
                     cardSkills.put(Integer.parseInt(d.getName()), MapleDataTool.getInt(d.getChildByPath("skillID")));
                  }
               case 2:
                  var8 = dat.iterator();

                  while(var8.hasNext()) {
                     d = (MapleData)var8.next();
                     int num = Integer.parseInt(d.getName());
                     Map<Integer, List<Point>> array = new HashMap();
                     Iterator var12 = d.iterator();

                     while(var12.hasNext()) {
                        MapleData z = (MapleData)var12.next();
                        int idx = Integer.parseInt(z.getName());
                        List<Point> arr = new ArrayList();
                        Iterator var16 = z.iterator();

                        while(var16.hasNext()) {
                           MapleData zz = (MapleData)var16.next();
                           Point data = MapleDataTool.getPoint(zz);
                           arr.add(data);
                        }

                        array.put(idx, arr);
                     }

                     characterSizes.put(num, array);
                  }
                  break;
               case 3:
                  var8 = dat.iterator();

                  while(var8.hasNext()) {
                     d = (MapleData)var8.next();
                     skills.add(MapleDataTool.getInt(d.getChildByPath("skillID")));
                  }
               }
            }

            return;
         }
      } catch (Exception var19) {
         var19.printStackTrace();
      }
   }

   public static void openUnion(MapleClient c) {
      if ((long)Integer.parseInt(c.getKeyValue("rank")) > c.getPlayer().getKeyValue(18771, "rank")) {
         c.getPlayer().setKeyValue(18771, "rank", c.getKeyValue("rank"));
      }

      if (c.getPlayer().getUnionDamage() > 0L) {
         c.getPlayer().RefreshUnionRaid(false);
      }

      c.getSession().writeAndFlush(CField.openUnionUI(c));
   }

   public static void unionFreeset(LittleEndianAccessor slea, MapleClient c) {
      c.getSession().writeAndFlush(CWvsContext.unionFreeset(c, slea.readInt()));
   }

   public static void setUnion(LittleEndianAccessor slea, MapleClient c) {
      try {
         int priset = slea.readInt();
         c.getPlayer().removeKeyValue(500630);
         c.getPlayer().setKeyValue(500630, "presetNo", priset.makeConcatWithConstants<invokedynamic>(priset));
         c.setKeyValue("presetNo", priset.makeConcatWithConstants<invokedynamic>(priset));
         int size2 = slea.readInt();

         int j;
         for(int i = 0; i < size2; ++i) {
            j = slea.readInt();
            c.getPlayer().setKeyValue(18791, i.makeConcatWithConstants<invokedynamic>(i), j.makeConcatWithConstants<invokedynamic>(j));
            c.setCustomData(500627 + priset, i.makeConcatWithConstants<invokedynamic>(i), j.makeConcatWithConstants<invokedynamic>(j));
         }

         size2 = slea.readInt();
         slea.skip(2);
         if (size2 == 0) {
            Iterator var17 = c.getPlayer().getUnions().getUnions().iterator();

            while(var17.hasNext()) {
               MapleUnion union = (MapleUnion)var17.next();
               if (priset == 0) {
                  union.setPriset(0);
                  union.setPos(0);
               } else if (priset == 1) {
                  union.setPriset1(0);
                  union.setPos1(0);
               } else if (priset == 2) {
                  union.setPriset2(0);
                  union.setPos2(0);
               } else if (priset == 3) {
                  union.setPriset3(0);
                  union.setPos3(0);
               } else if (priset == 4) {
                  union.setPriset4(0);
                  union.setPos4(0);
               }
            }
         }

         List<String> names = new ArrayList();

         for(j = 0; j < size2; ++j) {
            slea.skip(4);
            int id = slea.readInt();
            int lv = slea.readInt();
            int job = slea.readInt();
            int unk1 = slea.readInt();
            int unk2 = slea.readInt();
            int pos = slea.readInt();
            int unk3 = slea.readInt();
            String name = slea.readMapleAsciiString();
            names.add(name);
            Iterator var14 = c.getPlayer().getUnions().getUnions().iterator();

            while(var14.hasNext()) {
               MapleUnion union2 = (MapleUnion)var14.next();
               if (union2.getCharid() == id) {
                  union2.setLevel(lv);
                  union2.setJob(job);
                  union2.setUnk1(unk1);
                  union2.setUnk2(unk2);
                  union2.setPosition(pos);
                  union2.setUnk3(unk3);
                  union2.setName(name);
                  if (priset == 0) {
                     union2.setPriset(pos);
                     union2.setPos(unk2);
                  } else if (priset == 1) {
                     union2.setPriset1(pos);
                     union2.setPos1(unk2);
                  } else if (priset == 2) {
                     union2.setPriset2(pos);
                     union2.setPos2(unk2);
                  } else if (priset == 3) {
                     union2.setPriset3(pos);
                     union2.setPos3(unk2);
                  } else if (priset == 4) {
                     union2.setPriset4(pos);
                     union2.setPos4(unk2);
                  }
               }
            }
         }

         Iterator var20 = c.getPlayer().getUnions().getUnions().iterator();

         while(var20.hasNext()) {
            MapleUnion union3 = (MapleUnion)var20.next();
            if (union3.getPosition() != -1 && !names.contains(union3.getName())) {
               union3.setPosition(-1);
            }
         }

         c.getSession().writeAndFlush(CWvsContext.setUnion(c));
         c.send(CWvsContext.enableActions(c.getPlayer()));
         c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
      } catch (Exception var16) {
         var16.printStackTrace();
      }

   }

   public static void setUnionPriset(LittleEndianAccessor slea, MapleClient c) {
      try {
         int priset = slea.readInt();
         c.getPlayer().removeKeyValue(500630);
         c.getPlayer().setKeyValue(500630, "presetNo", priset.makeConcatWithConstants<invokedynamic>(priset));
         c.setKeyValue("presetNo", priset.makeConcatWithConstants<invokedynamic>(priset));
         slea.skip(5);
         slea.skip(4);
         slea.skip(4);
         slea.skip(4);
         slea.skip(4);
         slea.skip(4);
         slea.skip(4);
         slea.skip(4);
         int size2 = slea.readInt();
         List<String> names = new ArrayList();

         for(int i = 0; i < size2; ++i) {
            int key = slea.readInt();
            int id = slea.readInt();
            int lv = slea.readInt();
            int job = slea.readInt();
            int unk1 = slea.readInt();
            int unk2 = slea.readInt();
            int pos = slea.readInt();
            int unk3 = slea.readInt();
            String name = slea.readMapleAsciiString();
            names.add(name);
            Iterator var15 = c.getPlayer().getUnions().getUnions().iterator();

            while(var15.hasNext()) {
               MapleUnion union = (MapleUnion)var15.next();
               if (union.getCharid() == id) {
                  union.setLevel(lv);
                  union.setJob(job);
                  union.setUnk1(unk1);
                  union.setUnk2(unk2);
                  union.setPosition(pos);
                  union.setUnk3(unk3);
                  union.setName(name);
                  if (priset == 0) {
                     union.setPriset(pos);
                     union.setPos(unk2);
                  } else if (priset == 1) {
                     union.setPriset1(pos);
                     union.setPos1(unk2);
                  } else if (priset == 2) {
                     union.setPriset2(pos);
                     union.setPos2(unk2);
                  } else if (priset == 3) {
                     union.setPriset3(pos);
                     union.setPos3(unk2);
                  } else if (priset == 4) {
                     union.setPriset4(pos);
                     union.setPos4(unk2);
                  }
               }
            }
         }

         Iterator var18 = c.getPlayer().getUnions().getUnions().iterator();

         while(var18.hasNext()) {
            MapleUnion union2 = (MapleUnion)var18.next();
            if (union2.getPosition() != -1 && !names.contains(union2.getName())) {
               union2.setPosition(-1);
            }
         }

         c.getSession().writeAndFlush(CWvsContext.setUnion(c));
         c.send(CWvsContext.enableActions(c.getPlayer()));
         c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
      } catch (Exception var17) {
         var17.printStackTrace();
      }

   }
}
