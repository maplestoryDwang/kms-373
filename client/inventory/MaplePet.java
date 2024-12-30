package client.inventory;

import database.DatabaseConnection;
import java.awt.Point;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.MapleItemInformationProvider;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;

public class MaplePet implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   private String name;
   private String ExceptionList;
   private int Fh = 0;
   private int stance = 0;
   private int color = -1;
   private int petitemid;
   private int secondsLeft = 0;
   private int buffSkillId = 0;
   private int buffSkillId2 = 0;
   private int wonderGrade = -1;
   private long uniqueid;
   private Point pos;
   private byte fullness = 100;
   private byte level = 1;
   private byte summoned = 0;
   private short inventorypos = 0;
   private short closeness = 0;
   private short flags = 0;
   private short size = 100;
   private boolean changed = false;

   private MaplePet(int petitemid, long uniqueid) {
      this.petitemid = petitemid;
      this.uniqueid = uniqueid;
   }

   private MaplePet(int petitemid, long uniqueid, short inventorypos) {
      this.petitemid = petitemid;
      this.uniqueid = uniqueid;
      this.inventorypos = inventorypos;
   }

   public static final MaplePet loadFromDb(int itemid, long petid, short inventorypos) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MaplePet var8;
      try {
         MaplePet ret = new MaplePet(itemid, petid, inventorypos);
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM pets WHERE petid = ?");
         ps.setLong(1, petid);
         rs = ps.executeQuery();
         if (rs.next()) {
            ret.setName(rs.getString("name"));
            ret.setCloseness(rs.getShort("closeness"));
            ret.setLevel(rs.getByte("level"));
            ret.setFullness(rs.getByte("fullness"));
            ret.setSecondsLeft(rs.getInt("seconds"));
            ret.setFlags(rs.getShort("flags"));
            ret.setBuffSkillId(rs.getInt("petbuff"));
            ret.setPetSize(rs.getShort("size"));
            ret.setWonderGrade(rs.getInt("wonderGrade"));
            ret.setExceptionList(rs.getString("exceptionlist"));
            ret.setBuffSkillId2(rs.getInt("petbuff2"));
            ret.setChanged(false);
            rs.close();
            ps.close();
            con.close();
            var8 = ret;
            return var8;
         }

         rs.close();
         ps.close();
         var8 = null;
         return var8;
      } catch (SQLException var19) {
         Logger.getLogger(MaplePet.class.getName()).log(Level.SEVERE, (String)null, var19);
         var8 = null;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var18) {
            var18.printStackTrace();
         }

      }

      return var8;
   }

   public final void saveToDb(Connection con) {
      if (this.isChanged()) {
         PreparedStatement ps = null;

         try {
            ps = con.prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ?, seconds = ?, flags = ?, petbuff = ?, size = ?, wonderGrade = ?, exceptionlist = ?, petbuff2 = ? WHERE petid = ?");
            ps.setString(1, this.name);
            ps.setByte(2, this.level);
            ps.setShort(3, this.closeness);
            ps.setByte(4, this.fullness);
            ps.setInt(5, this.secondsLeft);
            ps.setShort(6, this.flags);
            ps.setInt(7, this.buffSkillId);
            ps.setShort(8, this.size);
            ps.setInt(9, this.wonderGrade);
            ps.setString(10, this.ExceptionList == null ? "" : this.ExceptionList);
            ps.setInt(11, this.buffSkillId2);
            ps.setLong(12, this.uniqueid);
            ps.executeUpdate();
            ps.close();
            this.setChanged(false);
         } catch (SQLException var12) {
            var12.printStackTrace();
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var11) {
               var11.printStackTrace();
            }

         }

      }
   }

   public static final MaplePet createPet(int itemid, long uniqueid) {
      return createPet(itemid, MapleItemInformationProvider.getInstance().getName(itemid), 1, 0, 100, uniqueid, 18000, (short)0);
   }

   public static final MaplePet createPet(int itemid, String name, int level, int closeness, int fullness, long uniqueid, int secondsLeft, short flag) {
      if (uniqueid <= -1L) {
         uniqueid = MapleInventoryIdentifier.getInstance();
      }

      Connection con = null;
      PreparedStatement pse = null;
      Object rs = null;

      label112: {
         Object var13;
         try {
            con = DatabaseConnection.getConnection();
            pse = con.prepareStatement("INSERT INTO pets (petid, name, level, closeness, fullness, seconds, flags, size, wonderGrade, exceptionlist) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pse.setLong(1, uniqueid);
            pse.setString(2, name);
            pse.setByte(3, (byte)level);
            pse.setShort(4, (short)closeness);
            pse.setByte(5, (byte)fullness);
            pse.setInt(6, secondsLeft);
            pse.setShort(7, flag);
            pse.setShort(8, (short)100);
            pse.setInt(9, PetDataFactory.getWonderGrade(itemid));
            pse.setString(10, "");
            pse.executeUpdate();
            pse.close();
            con.close();
            break label112;
         } catch (SQLException var23) {
            var23.printStackTrace();
            var13 = null;
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (pse != null) {
                  pse.close();
               }

               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var22) {
               var22.printStackTrace();
            }

         }

         return (MaplePet)var13;
      }

      MaplePet pet = new MaplePet(itemid, uniqueid);
      pet.setName(name);
      pet.setLevel(level);
      pet.setFullness(fullness);
      pet.setCloseness(closeness);
      pet.setFlags(flag);
      pet.setSecondsLeft(secondsLeft);
      pet.setWonderGrade(PetDataFactory.getWonderGrade(itemid));
      pet.setPetSize((short)100);
      return pet;
   }

   public final String getName() {
      return this.name;
   }

   public final void setName(String name) {
      this.name = name;
      this.setChanged(true);
   }

   public final boolean getSummoned() {
      return this.summoned > 0;
   }

   public final byte getSummonedValue() {
      return this.summoned;
   }

   public final void setSummoned(byte summoned) {
      this.summoned = summoned;
   }

   public final short getInventoryPosition() {
      return this.inventorypos;
   }

   public final void setInventoryPosition(short inventorypos) {
      this.inventorypos = inventorypos;
   }

   public long getUniqueId() {
      return this.uniqueid;
   }

   public final short getCloseness() {
      return this.closeness;
   }

   public final void setCloseness(int closeness) {
      this.closeness = (short)closeness;
      this.setChanged(true);
   }

   public final byte getLevel() {
      return this.level;
   }

   public final void setLevel(int level) {
      this.level = (byte)level;
      this.setChanged(true);
   }

   public final byte getFullness() {
      return 100;
   }

   public final void setFullness(int fullness) {
      this.fullness = (byte)fullness;
      this.setChanged(true);
   }

   public final short getFlags() {
      return 503;
   }

   public final void setFlags(int fffh) {
      this.flags = (short)fffh;
      this.setChanged(true);
   }

   public final int getBuffSkillId() {
      return this.buffSkillId;
   }

   public final void setBuffSkillId(int skillId) {
      this.buffSkillId = skillId;
      this.setChanged(true);
   }

   public final int getBuffSkillId2() {
      return this.buffSkillId2;
   }

   public final void setBuffSkillId2(int skillId) {
      this.buffSkillId2 = skillId;
      this.setChanged(true);
   }

   public final int getWonderGrade() {
      return this.wonderGrade;
   }

   public final void setWonderGrade(int grade) {
      this.wonderGrade = grade;
      this.setChanged(true);
   }

   public final short getPetSize() {
      return this.size;
   }

   public final void setPetSize(short size) {
      this.size = size;
      this.setChanged(true);
   }

   public void addPetSize(short size) {
      this.size += size;
      this.setChanged(true);
   }

   public final int getFh() {
      return this.Fh;
   }

   public final void setFh(int Fh) {
      this.Fh = Fh;
   }

   public final Point getPos() {
      return this.pos;
   }

   public final void setPos(Point pos) {
      this.pos = pos;
   }

   public final int getStance() {
      return this.stance;
   }

   public final void setStance(int stance) {
      this.stance = stance;
   }

   public final int getColor() {
      return this.color;
   }

   public final void setColor(int color) {
      this.color = color;
   }

   public final int getPetItemId() {
      return this.petitemid;
   }

   public final boolean canConsume(int itemId) {
      MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
      Iterator iterator = mii.getItemEffect(itemId).getPetsCanConsume().iterator();

      int petId;
      do {
         if (!iterator.hasNext()) {
            return false;
         }

         petId = (Integer)iterator.next();
      } while(petId != this.petitemid);

      return true;
   }

   public final void updatePosition(List<LifeMovementFragment> movement) {
      Iterator var2 = movement.iterator();

      while(var2.hasNext()) {
         LifeMovementFragment move = (LifeMovementFragment)var2.next();
         if (move instanceof LifeMovement) {
            if (move instanceof AbsoluteLifeMovement) {
               this.setPos(((LifeMovement)move).getPosition());
            }

            this.setStance(((LifeMovement)move).getNewstate());
         }
      }

   }

   public final int getSecondsLeft() {
      return this.secondsLeft;
   }

   public final void setSecondsLeft(int sl) {
      this.secondsLeft = sl;
      this.setChanged(true);
   }

   public boolean isChanged() {
      return this.changed;
   }

   public void setChanged(boolean changed) {
      this.changed = changed;
   }

   public String getExceptionList() {
      return this.ExceptionList;
   }

   public void setExceptionList(String ExceptionList) {
      this.ExceptionList = ExceptionList;
      this.setChanged(true);
   }

   public static enum PetFlag {
      ITEM_PICKUP(1, 5190000, 5191000),
      EXPAND_PICKUP(2, 5190002, 5191002),
      AUTO_PICKUP(4, 5190003, 5191003),
      UNPICKABLE(8, 5190005, -1),
      LEFTOVER_PICKUP(16, 5190004, 5191004),
      HP_CHARGE(32, 5190001, 5191001),
      MP_CHARGE(64, 5190006, -1),
      PET_BUFF(128, 5190010, -1),
      PET_TRAINING(256, 5190011, -1),
      PET_GIANT(512, 5190012, -1),
      PET_SHOP(1024, 5190013, -1);

      private final int i;
      private final int item;
      private final int remove;

      private PetFlag(int i, int item, int remove) {
         this.i = i;
         this.item = item;
         this.remove = remove;
      }

      public final int getValue() {
         return this.i;
      }

      public final boolean check(int flag) {
         return (flag & this.i) == this.i;
      }

      public static final MaplePet.PetFlag getByAddId(int itemId) {
         MaplePet.PetFlag[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            MaplePet.PetFlag flag = var1[var3];
            if (flag.item == itemId) {
               return flag;
            }
         }

         return null;
      }

      public static final MaplePet.PetFlag getByDelId(int itemId) {
         MaplePet.PetFlag[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            MaplePet.PetFlag flag = var1[var3];
            if (flag.remove == itemId) {
               return flag;
            }
         }

         return null;
      }
   }
}
