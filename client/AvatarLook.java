package client;

import client.inventory.Equip;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import constants.GameConstants;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import server.Randomizer;
import tools.data.LittleEndianAccessor;
import tools.data.MaplePacketLittleEndianWriter;

public class AvatarLook {
   private final Map<Byte, Integer> items = new HashMap();
   private final Map<Byte, Integer> codyItems = new HashMap();
   private final int[] pets = new int[3];
   private int gender;
   private int skin;
   private int face;
   private int hair;
   private int job;
   private int mega;
   private int weaponStickerID;
   private int weaponID;
   private int subWeaponID;
   private int drawElfEar;
   private int pinkbeanChangeColor;
   private int demonSlayerDefFaceAcc;
   private int xenonDefFaceAcc;
   private int arkDefFaceAcc;
   private int hoyeongDefFaceAcc;
   private int isZeroBetaLook;
   private int addColor;
   private int baseProb;
   private int hoyeongAvater;

   public static AvatarLook makeRandomAvatar() {
      AvatarLook a = new AvatarLook();
      int[] skin = new int[]{15, 16, 18, 19};
      int[] face = new int[]{20061, 25017, 21045, 25099, 25050};
      a.gender = Randomizer.nextInt(2);
      int[] hair;
      if (a.gender == 1) {
         hair = new int[]{43980, 40670, 38620, 48370, 41750};
      } else {
         hair = new int[]{43330, 35660, 46340, 33150, 43320};
      }

      a.skin = skin[Randomizer.nextInt(skin.length)];
      a.face = face[Randomizer.nextInt(face.length)] + Randomizer.nextInt(8) * 100;
      a.job = 222;
      a.mega = 0;
      a.hair = hair[Randomizer.nextInt(hair.length)] + Randomizer.nextInt(8);
      int[][] items = new int[][]{{1002186, 1003250, 1000599}, {1012104, 1012379}, {1022079, 1022285}, {1032024}, {1052975, 1053257, 1053351}, {1062183}, {1072153}, {1082102}, {1103068}};

      for(int i = 0; i < items.length; ++i) {
         a.items.put((byte)(i + 1), items[i][Randomizer.nextInt(items[i].length)]);
      }

      int[] weapons = new int[]{1702174, 1702549, 1702945};
      a.weaponStickerID = weapons[Randomizer.nextInt(weapons.length)];
      a.weaponID = Randomizer.nextBoolean() ? 1442000 : 1302000;
      a.subWeaponID = 1092056;
      a.drawElfEar = 0;
      a.pinkbeanChangeColor = 0;

      for(int j = 0; j < 3; ++j) {
         a.pets[j] = 0;
      }

      return a;
   }

   public void save(int position, PreparedStatement ps) throws SQLException {
      ps.setInt(2, position);
      ps.setInt(3, this.gender);
      ps.setInt(4, this.skin);
      ps.setInt(5, this.face);
      ps.setInt(6, this.hair);

      for(int i = 1; i <= 9; ++i) {
         if (this.items.containsKey((byte)i)) {
            ps.setInt(6 + i, (Integer)this.items.get((byte)i));
         } else {
            ps.setInt(6 + i, -1);
         }
      }

      ps.setInt(16, this.weaponStickerID);
      ps.setInt(17, this.weaponID);
      ps.setInt(18, this.subWeaponID);
   }

   public static AvatarLook init(ResultSet rs) throws SQLException {
      AvatarLook a = new AvatarLook();
      a.gender = rs.getInt("gender");
      a.skin = rs.getInt("skin");
      a.face = rs.getInt("face");
      a.hair = rs.getInt("hair");
      a.addColor = rs.getInt("addColor");
      a.baseProb = rs.getInt("baseProb");

      for(int i = 1; i <= 9; ++i) {
         a.items.put((byte)i, rs.getInt("equip" + i));
      }

      a.weaponStickerID = rs.getInt("weaponstickerid");
      a.weaponID = rs.getInt("weaponid");
      a.subWeaponID = rs.getInt("subweaponid");
      return a;
   }

   public int getHairEquip(int pos) {
      if (this.codyItems.containsKey((byte)pos)) {
         return (Integer)this.codyItems.get((byte)pos);
      } else {
         return this.items.containsKey((byte)pos) ? (Integer)this.items.get((byte)pos) : -1;
      }
   }

   public boolean compare(AvatarLook a) {
      if (a.gender == this.gender && a.skin == this.skin && a.face == this.face && a.hair == this.hair && a.subWeaponID == this.subWeaponID && a.weaponStickerID == this.weaponStickerID) {
         for(int i = 1; i <= 9; ++i) {
            if (a.getHairEquip(i) != this.getHairEquip(i)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static void decodeUnpackAvatarLook(AvatarLook a, LittleEndianAccessor slea) throws IOException {
      a.gender = slea.readBit(1);
      a.skin = slea.readBit(10);
      boolean is5thFace = slea.readBit(1) == 1;
      int face1 = slea.readBit(10);
      int face2 = slea.readBit(4);
      if (face1 == 1023) {
         a.face = 0;
      } else {
         a.face = face1 + 1000 * (face2 + (is5thFace ? 50 : 20));
      }

      boolean is4thHair = slea.readBit(1) == 1;
      int hair1 = slea.readBit(10);
      int hair2 = slea.readBit(4);
      if (hair1 == 1023) {
         a.hair = 0;
      } else {
         a.hair = hair1 + 1000 * (hair2 + 30) + (is4thHair ? 10000 : 0);
      }

      int equip1_1 = slea.readBit(10);
      int equip1_2 = slea.readBit(3);
      if (equip1_1 != 1023) {
         a.items.put((byte)1, equip1_1 + 1000 * (equip1_2 + 1000));
      }

      int equip2_1 = slea.readBit(10);
      int equip2_2 = slea.readBit(2);
      if (equip2_1 != 1023) {
         a.items.put((byte)2, equip2_1 + 1000 * (equip2_2 + 1010));
      }

      int equip3_1 = slea.readBit(10);
      int equip3_2 = slea.readBit(2);
      if (equip3_1 != 1023) {
         a.items.put((byte)3, equip3_1 + 1000 * (equip3_2 + 1020));
      }

      int equip4_1 = slea.readBit(10);
      int equip4_2 = slea.readBit(2);
      if (equip4_1 != 1023) {
         a.items.put((byte)4, equip4_1 + 1000 * (equip4_2 + 1030));
      }

      boolean equip5_check = slea.readBit(1) == 1;
      int equip5_1 = slea.readBit(10);
      int equip5_2 = slea.readBit(4);
      if (equip5_1 != 1023) {
         a.items.put((byte)5, equip5_1 + 1000 * (equip5_2 + 10 * (equip5_check ? 105 : 104)));
      }

      int equip6_1 = slea.readBit(10);
      int equip6_2 = slea.readBit(2);
      if (equip6_1 != 1023) {
         a.items.put((byte)6, equip6_1 + 1000 * (equip6_2 + 1060));
      }

      int equip7_1 = slea.readBit(10);
      int equip7_2 = slea.readBit(2);
      if (equip7_1 != 1023) {
         a.items.put((byte)7, equip7_1 + 1000 * (equip7_2 + 1070));
      }

      int equip8_1 = slea.readBit(10);
      int equip8_2 = slea.readBit(2);
      if (equip8_1 != 1023) {
         a.items.put((byte)8, equip8_1 + 1000 * (equip8_2 + 1080));
      }

      int equip9_1 = slea.readBit(10);
      int equip9_2 = slea.readBit(2);
      if (equip9_1 != 1023) {
         a.items.put((byte)9, equip9_1 + 1000 * (equip9_2 + 1100));
      }

      int equip10_check = slea.readBit(2);
      int equip10_1 = slea.readBit(10);
      int equip10_2 = slea.readBit(4);
      int v53 = equip10_check == 3 ? 135 : (equip10_check == 2 ? 134 : (equip10_check == 1 ? 109 : 0));
      if (equip10_1 != 1023) {
         a.subWeaponID = equip10_1 + 1000 * (equip10_2 + 10 * v53);
      }

      boolean weaponStickerCheck = slea.readBit(1) == 1;
      int equip11_1 = slea.readBit(10);
      int equip11_2 = slea.readBit(2);
      if (weaponStickerCheck) {
         a.weaponStickerID = 1000 * (equip11_2 + 1700) + equip11_1;
      }

   }

   public void encodeUnpackAvatarLook(MaplePacketLittleEndianWriter mplew) {
      mplew.writeBit(this.gender, 1);
      mplew.writeBit(this.skin, 10);
      if (this.face >= 100000) {
         this.face /= 1000;
      }

      mplew.writeBit(this.face / 10000 == 5 ? 1 : 0, 1);
      mplew.writeBit(this.face % 1000, 10);
      mplew.writeBit(this.face / 1000 % 10, 4);
      mplew.writeBit(this.hair / 10000, 4);
      mplew.writeBit(this.hair % 1000, 10);
      mplew.writeBit(this.hair / 1000 % 10, 4);
      mplew.writeBit(this.getHairEquip(1) % 1000, 10);
      mplew.writeBit(this.getHairEquip(1) / 1000 % 10, 3);
      mplew.writeBit(this.getHairEquip(2) % 1000, 10);
      mplew.writeBit(this.getHairEquip(2) / 1000 % 10, 2);
      mplew.writeBit(this.getHairEquip(3) % 1000, 10);
      mplew.writeBit(this.getHairEquip(3) / 1000 % 10, 2);
      mplew.writeBit(this.getHairEquip(4) % 1000, 10);
      mplew.writeBit(this.getHairEquip(4) / 1000 % 10, 2);
      mplew.writeBit(Integer.toUnsignedLong(this.getHairEquip(5) - 1050000) < 10000L ? 1 : 0, 1);
      mplew.writeBit(this.getHairEquip(5) % 1000, 10);
      mplew.writeBit(this.getHairEquip(5) / 1000 % 10, 4);
      mplew.writeBit(this.getHairEquip(6) % 1000, 10);
      mplew.writeBit(this.getHairEquip(6) / 1000 % 10, 2);
      mplew.writeBit(this.getHairEquip(7) % 1000, 10);
      mplew.writeBit(this.getHairEquip(7) / 1000 % 10, 2);
      mplew.writeBit(this.getHairEquip(8) % 1000, 10);
      mplew.writeBit(this.getHairEquip(8) / 1000 % 10, 2);
      mplew.writeBit(this.getHairEquip(9) % 1000, 10);
      mplew.writeBit(this.getHairEquip(9) / 1000 % 10, 2);
      int v39 = 0;
      if (this.subWeaponID > 0) {
         if (this.subWeaponID / 10000 == 109) {
            v39 = 1;
         } else {
            v39 = Integer.toUnsignedLong(this.subWeaponID - 1340000) < 10000L ? 2 : 3;
         }
      }

      mplew.writeBit(v39, 2);
      mplew.writeBit(this.subWeaponID % 1000, 10);
      mplew.writeBit(this.subWeaponID / 1000 % 10, 4);
      mplew.writeBit(this.weaponStickerID > 0 ? 1 : 0, 1);
      mplew.writeBit(this.weaponStickerID % 1000, 10);
      mplew.writeBit(this.weaponStickerID / 1000 % 10, 2);
      mplew.writeBit(this.weaponID % 1000, 10);
      mplew.writeBit(this.weaponID / 1000 % 10, 2);
      int weaponType = this.weaponID / 10000 % 100;
      if (GameConstants.getWeaponType(this.weaponID) == MapleWeaponType.TUNER) {
         weaponType = 213;
      }

      Integer[] wt = new Integer[]{30, 31, 32, 33, 37, 38, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 39, 34, 52, 53, 35, 36, 21, 22, 23, 24, 56, 57, 26, 58, 27, 28, 59, 29, 213, 214, 0};
      int index = Arrays.asList(wt).indexOf(weaponType);
      mplew.writeBit(index == -1 ? wt.length : index + 1, 8);
      mplew.writeBit(this.drawElfEar & 1, 4);
      mplew.writeBit(this.addColor, 4);
      mplew.writeBit(this.baseProb, 8);
      mplew.writeBit(this.hoyeongDefFaceAcc, 8);
      mplew.writeBit(!GameConstants.isPinkBean(this.job) && !GameConstants.isYeti(this.job) ? 0 : 1, 1);
      mplew.writeBit(0, 18);
      mplew.writeZeroBytes(88);
      mplew.write((int)21);
   }

   public void encodeAvatarLook(MaplePacketLittleEndianWriter mplew) {
      mplew.write(this.gender);
      mplew.write(this.skin);
      mplew.writeInt(this.face);
      mplew.writeInt(this.job);
      mplew.write(this.mega);
      mplew.writeInt(this.hair);
      Iterator var2 = this.items.entrySet().iterator();

      Entry entry;
      while(var2.hasNext()) {
         entry = (Entry)var2.next();
         mplew.write((Byte)entry.getKey());
         mplew.writeInt((Integer)entry.getValue());
      }

      mplew.write((int)-1);
      var2 = this.codyItems.entrySet().iterator();

      while(var2.hasNext()) {
         entry = (Entry)var2.next();
         mplew.write((Byte)entry.getKey());
         mplew.writeInt((Integer)entry.getValue());
      }

      mplew.write((int)-1);
      mplew.writeInt(this.weaponStickerID);
      mplew.writeInt(this.weaponID);
      mplew.writeInt(this.subWeaponID);
      mplew.writeInt(this.drawElfEar);
      mplew.writeInt(this.pinkbeanChangeColor);
      mplew.write(this.hoyeongAvater);

      for(int i = 0; i < 3; ++i) {
         mplew.writeInt(this.pets[i]);
      }

      if (!GameConstants.isDemonSlayer(this.job) && !GameConstants.isDemonAvenger(this.job)) {
         if (GameConstants.isXenon(this.job)) {
            mplew.writeInt(this.xenonDefFaceAcc);
         } else if (GameConstants.isArk(this.job)) {
            mplew.writeInt(this.arkDefFaceAcc);
         } else if (GameConstants.isHoyeong(this.job)) {
            mplew.writeInt(this.hoyeongDefFaceAcc);
         } else if (GameConstants.isZero(this.job)) {
            mplew.write(this.isZeroBetaLook);
         }
      } else {
         mplew.writeInt(this.demonSlayerDefFaceAcc);
      }

      mplew.write(this.addColor);
      mplew.write(this.baseProb);
      mplew.writeInt(153525);
   }

   public static final void encodeAvatarLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega, boolean second) {
      boolean isAlpha = GameConstants.isZero(chr.getJob()) && chr.getGender() == 0 && chr.getSecondGender() == 1;
      boolean isBeta = GameConstants.isZero(chr.getJob()) && chr.getGender() == 1 && chr.getSecondGender() == 0;
      if (GameConstants.isZero(chr.getJob())) {
         mplew.write(!second && !isBeta ? 0 : 1);
      } else {
         mplew.write(chr.getGender());
      }

      mplew.write(!second && !isBeta ? chr.getSkinColor() : chr.getSecondSkinColor());
      mplew.writeInt(!second && !isBeta ? chr.getFace() : chr.getSecondFace());
      mplew.writeInt(chr.getJob());
      mplew.write(mega ? 0 : 1);
      int hair;
      if (!second && !isBeta) {
         hair = chr.getHair();
         if (chr.getBaseColor() != -1) {
            hair = chr.getHair() / 10 * 10 + chr.getBaseColor();
         }

         mplew.writeInt(hair);
      } else {
         hair = chr.getSecondHair();
         if (chr.getSecondBaseColor() != -1) {
            hair = chr.getSecondHair() / 10 * 10 + chr.getSecondBaseColor();
         }

         mplew.writeInt(hair);
      }

      Map<Byte, Integer> myEquip = new LinkedHashMap();
      Map<Byte, Integer> maskedEquip = new LinkedHashMap();
      Map<Byte, Integer> totemEquip = new LinkedHashMap();
      Map<Short, Integer> equip = second ? chr.getSecondEquips() : chr.getEquips();
      Iterator var10 = equip.entrySet().iterator();

      while(true) {
         while(true) {
            Entry item;
            short pos;
            Equip item_;
            do {
               do {
                  if (!var10.hasNext()) {
                     var10 = chr.getTotems().entrySet().iterator();

                     while(var10.hasNext()) {
                        item = (Entry)var10.next();
                        byte pos = (byte)((Byte)item.getKey() * -1);
                        if (pos >= 0 && pos <= 2 && (Integer)item.getValue() >= 1200000 && (Integer)item.getValue() < 1210000) {
                           totemEquip.put(pos, (Integer)item.getValue());
                        }
                     }

                     var10 = myEquip.entrySet().iterator();

                     while(true) {
                        while(true) {
                           int weapon;
                           do {
                              do {
                                 if (!var10.hasNext()) {
                                    mplew.write((int)-1);
                                    var10 = maskedEquip.entrySet().iterator();

                                    while(var10.hasNext()) {
                                       item = (Entry)var10.next();
                                       mplew.write((Byte)item.getKey());
                                       mplew.writeInt((Integer)item.getValue());
                                    }

                                    mplew.write((int)-1);
                                    Integer cWeapon;
                                    Integer Weapon;
                                    if (isBeta) {
                                       cWeapon = (Integer)equip.get(-1511);
                                       mplew.writeInt(cWeapon != null ? cWeapon : 0);
                                       Weapon = (Integer)equip.get(Short.valueOf((short)-11));
                                       mplew.writeInt(Weapon != null ? Weapon : 0);
                                       mplew.writeInt(0);
                                    } else {
                                       cWeapon = (Integer)equip.get(Short.valueOf((short)-111));
                                       mplew.writeInt(cWeapon != null ? cWeapon : 0);
                                       Weapon = (Integer)equip.get(Short.valueOf((short)-11));
                                       mplew.writeInt(Weapon != null ? Weapon : 0);
                                       Integer Shield = (Integer)equip.get(Short.valueOf((short)-10));
                                       if (!GameConstants.isZero(chr.getJob()) && Shield != null) {
                                          mplew.writeInt(Shield);
                                       } else {
                                          mplew.writeInt(0);
                                       }
                                    }

                                    mplew.writeInt(0);
                                    mplew.writeInt(chr.getKeyValue(100229, "hue"));
                                    mplew.writeInt(0);

                                    for(int i = 0; i < 3; ++i) {
                                       if (chr.getPet((long)i) != null) {
                                          mplew.writeInt(chr.getPet((long)i).getPetItemId());
                                       } else {
                                          mplew.writeInt(0);
                                       }
                                    }

                                    if (!GameConstants.isDemonSlayer(chr.getJob()) && !GameConstants.isXenon(chr.getJob()) && !GameConstants.isDemonAvenger(chr.getJob()) && !GameConstants.isArk(chr.getJob())) {
                                       if (GameConstants.isHoyeong(chr.getJob())) {
                                          mplew.writeInt(chr.getDemonMarking());
                                       } else if (GameConstants.isZero(chr.getJob())) {
                                          mplew.write(!second && !isBeta ? chr.getGender() : chr.getSecondGender());
                                       } else if (GameConstants.isAngelicBuster(chr.getJob())) {
                                          mplew.write(chr.getDressup());
                                       }
                                    } else {
                                       mplew.writeInt(chr.getDemonMarking());
                                    }

                                    mplew.write((int)0);
                                    mplew.writeInt(0);
                                    return;
                                 }

                                 item = (Entry)var10.next();
                                 weapon = (Integer)item.getValue();
                              } while(isAlpha && GameConstants.getWeaponType(weapon) == MapleWeaponType.BIG_SWORD);
                           } while(isBeta && GameConstants.getWeaponType(weapon) == MapleWeaponType.LONG_SWORD);

                           if (isBeta && GameConstants.getWeaponType(weapon) == MapleWeaponType.BIG_SWORD) {
                              mplew.write((int)11);
                              mplew.writeInt((Integer)item.getValue());
                           } else {
                              mplew.write((Byte)item.getKey());
                              mplew.writeInt((Integer)item.getValue());
                           }
                        }
                     }
                  }

                  item = (Entry)var10.next();
               } while((Short)item.getKey() < -2000);

               pos = (short)((Short)item.getKey() * -1);
               item_ = (Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-pos));
            } while(item_ == null);

            String lol;
            String ss;
            int moru;
            if (GameConstants.isAngelicBuster(chr.getJob()) && second) {
               if (pos >= 1300 && pos < 1400) {
                  pos = (short)(pos - 1300);
                  switch(pos) {
                  case 0:
                     pos = 1;
                     break;
                  case 1:
                     pos = 9;
                  case 2:
                  case 3:
                  default:
                     break;
                  case 4:
                     pos = 8;
                     break;
                  case 5:
                     pos = 3;
                     break;
                  case 6:
                     pos = 4;
                     break;
                  case 7:
                     pos = 5;
                     break;
                  case 8:
                     pos = 6;
                     break;
                  case 9:
                     pos = 7;
                  }

                  if (myEquip.get((byte)pos) != null) {
                     maskedEquip.put((byte)pos, (Integer)myEquip.get((byte)pos));
                  }

                  lol = ((Integer)item.getValue()).toString();
                  ss = lol.substring(0, 3);
                  moru = Integer.parseInt(ss + Integer.valueOf(item_.getMoru()).toString());
                  myEquip.put((byte)pos, item_.getMoru() != 0 ? moru : (Integer)item.getValue());
               } else if (pos > 100 && pos < 200 && pos != 111) {
                  pos = (short)(pos - 100);
                  switch(pos) {
                  case 10:
                  case 12:
                  case 13:
                  case 15:
                  case 16:
                     if (myEquip.get((byte)pos) != null) {
                        maskedEquip.put((byte)pos, (Integer)myEquip.get((byte)pos));
                     }

                     lol = ((Integer)item.getValue()).toString();
                     ss = lol.substring(0, 3);
                     moru = Integer.parseInt(ss + Integer.valueOf(item_.getMoru()).toString());
                     myEquip.put((byte)pos, item_.getMoru() != 0 ? moru : (Integer)item.getValue());
                  case 11:
                  case 14:
                  }
               }

               if (pos < 100) {
                  if (myEquip.get((byte)pos) == null) {
                     lol = ((Integer)item.getValue()).toString();
                     ss = lol.substring(0, 3);
                     moru = Integer.parseInt(ss + Integer.valueOf(item_.getMoru()).toString());
                     myEquip.put((byte)pos, item_.getMoru() != 0 ? moru : (Integer)item.getValue());
                  } else {
                     maskedEquip.put((byte)pos, (Integer)item.getValue());
                  }
               }
            } else if (isBeta) {
               if (pos < 100 && myEquip.get((byte)pos) == null) {
                  lol = ((Integer)item.getValue()).toString();
                  ss = lol.substring(0, 3);
                  moru = Integer.parseInt(ss + Integer.valueOf(item_.getMoru()).toString());
                  myEquip.put((byte)pos, item_.getMoru() != 0 ? moru : (Integer)item.getValue());
               } else if (pos > 1500 && pos != 1511) {
                  if (pos > 1500) {
                     pos = (short)(pos - 1500);
                  }

                  myEquip.put((byte)pos, (Integer)item.getValue());
               }
            } else if (isAlpha || GameConstants.isAngelicBuster(chr.getJob()) && !second || !GameConstants.isZero(chr.getJob()) && !GameConstants.isAngelicBuster(chr.getJob())) {
               if (pos < 100 && myEquip.get((byte)pos) == null) {
                  lol = ((Integer)item.getValue()).toString();
                  ss = lol.substring(0, 3);
                  moru = Integer.parseInt(ss + Integer.valueOf(item_.getMoru()).toString());
                  myEquip.put((byte)pos, item_.getMoru() != 0 ? moru : (Integer)item.getValue());
               } else if (pos > 100 && pos != 111) {
                  pos = (short)(pos - 100);
                  if (myEquip.get((byte)pos) != null) {
                     maskedEquip.put((byte)pos, (Integer)myEquip.get((byte)pos));
                  }

                  lol = ((Integer)item.getValue()).toString();
                  ss = lol.substring(0, 3);
                  moru = Integer.parseInt(ss + Integer.valueOf(item_.getMoru()).toString());
                  myEquip.put((byte)pos, item_.getMoru() != 0 ? moru : (Integer)item.getValue());
               } else if (myEquip.get((byte)pos) != null) {
                  maskedEquip.put((byte)pos, (Integer)item.getValue());
               }
            }
         }
      }
   }

   public static void decodeAvatarLook(AvatarLook a, LittleEndianAccessor slea) {
      a.gender = slea.readByte();
      a.skin = slea.readByte();
      a.face = slea.readInt();
      a.job = slea.readInt();
      a.mega = slea.readByte();
      a.hair = slea.readInt();

      for(byte pos = slea.readByte(); pos != -1; pos = slea.readByte()) {
         int itemId = slea.readInt();
         a.items.put(pos, itemId);
      }

      int i;
      for(byte pos2 = slea.readByte(); pos2 != -1; pos2 = slea.readByte()) {
         i = slea.readInt();
         a.codyItems.put(pos2, i);
      }

      a.weaponStickerID = slea.readInt();
      a.weaponID = slea.readInt();
      a.subWeaponID = slea.readInt();
      a.drawElfEar = slea.readInt();
      a.pinkbeanChangeColor = slea.readInt();
      slea.readByte();

      for(i = 0; i < 3; ++i) {
         a.pets[i] = slea.readInt();
      }

      a.addColor = slea.readByte();
      a.baseProb = slea.readByte();
   }
}
