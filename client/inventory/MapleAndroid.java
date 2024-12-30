package client.inventory;

import constants.GameConstants;
import database.DatabaseConnection;
import java.awt.Point;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import tools.Pair;
import tools.Triple;

public class MapleAndroid implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   private int stance = 0;
   private int itemid;
   private int hair;
   private int face;
   private int skin;
   private int gender;
   private long uniqueid;
   private boolean ear;
   private String name;
   private Point pos = new Point(0, 0);
   private boolean changed = false;

   private MapleAndroid(int itemid, long uniqueid) {
      this.itemid = itemid;
      this.uniqueid = uniqueid;
   }

   public static final MapleAndroid loadFromDb(int itemid, long uid) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MapleAndroid var7;
      try {
         MapleAndroid ret = new MapleAndroid(itemid, uid);
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM androids WHERE uniqueid = ?");
         ps.setLong(1, uid);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            var7 = null;
            return var7;
         }

         ret.setHair(rs.getInt("hair"));
         ret.setFace(rs.getInt("face"));
         ret.setSkin(rs.getInt("skin"));
         ret.setGender(rs.getInt("gender"));
         ret.setName(rs.getString("name"));
         ret.setEar(rs.getBoolean("ear"));
         ret.changed = false;
         rs.close();
         ps.close();
         con.close();
         var7 = ret;
      } catch (SQLException var18) {
         var18.printStackTrace();
         var7 = null;
         return var7;
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
         } catch (SQLException var17) {
            var17.printStackTrace();
         }

      }

      return var7;
   }

   public final void saveToDb() {
      if (this.changed) {
         Connection con = null;
         PreparedStatement ps = null;
         Object rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE androids SET hair = ?, face = ?, name = ?, skin = ?, gender = ?, ear = ? WHERE uniqueid = ?");
            ps.setInt(1, this.hair);
            ps.setInt(2, this.face);
            ps.setString(3, this.name);
            ps.setInt(4, this.skin);
            ps.setInt(5, this.gender);
            ps.setBoolean(6, this.ear);
            ps.setLong(7, this.uniqueid);
            ps.executeUpdate();
            ps.close();
            con.close();
            this.changed = false;
         } catch (SQLException var13) {
            var13.printStackTrace();
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var12) {
               var12.printStackTrace();
            }

         }

      }
   }

   public static final MapleAndroid create(int itemid, long uniqueid) {
      Triple<Pair<List<Integer>, List<Integer>>, List<Integer>, Integer> aInfo = MapleItemInformationProvider.getInstance().getAndroidInfo(GameConstants.getAndroidType(itemid));
      return aInfo == null ? null : create(itemid, uniqueid, (Integer)((List)((Pair)aInfo.left).left).get(Randomizer.nextInt(((List)((Pair)aInfo.left).left).size())), (Integer)((List)((Pair)aInfo.left).right).get(Randomizer.nextInt(((List)((Pair)aInfo.left).right).size())), (Integer)((List)aInfo.mid).get(Randomizer.nextInt(((List)aInfo.mid).size())), (Integer)aInfo.right);
   }

   public static final MapleAndroid create(int itemid, long uniqueid, int hair, int face, int skin, int gender) {
      if (uniqueid <= -1L) {
         uniqueid = MapleInventoryIdentifier.getInstance();
      }

      skin -= 2000;
      Connection con = null;
      PreparedStatement pse = null;
      Object rs = null;

      label112: {
         Object var11;
         try {
            con = DatabaseConnection.getConnection();
            pse = con.prepareStatement("INSERT INTO androids (uniqueid, hair, face, skin, gender, name, ear) VALUES (?, ?, ?, ?, ?, ?, ?)");
            pse.setLong(1, uniqueid);
            pse.setInt(2, hair);
            pse.setInt(3, face);
            pse.setInt(4, skin);
            pse.setInt(5, gender);
            pse.setString(6, "안드로이드");
            pse.setBoolean(7, true);
            pse.executeUpdate();
            pse.close();
            con.close();
            break label112;
         } catch (SQLException var21) {
            var21.printStackTrace();
            var11 = null;
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
            } catch (SQLException var20) {
               var20.printStackTrace();
            }

         }

         return (MapleAndroid)var11;
      }

      MapleAndroid pet = new MapleAndroid(itemid, uniqueid);
      pet.setHair(hair);
      pet.setFace(face);
      pet.setSkin(skin);
      pet.setGender(gender);
      pet.setName("안드로이드");
      pet.setEar(true);
      return pet;
   }

   public long getUniqueId() {
      return this.uniqueid;
   }

   public final void setEar(boolean ear) {
      this.ear = ear;
      this.changed = true;
   }

   public final boolean getEar() {
      return this.ear;
   }

   public final void setHair(int closeness) {
      this.hair = closeness;
      this.changed = true;
   }

   public final int getHair() {
      return this.hair;
   }

   public final int getSkin() {
      return this.skin;
   }

   public final int getGender() {
      return this.gender;
   }

   public final void setFace(int closeness) {
      this.face = closeness;
      this.changed = true;
   }

   public final void setSkin(int closeness) {
      this.skin = closeness;
      this.changed = true;
   }

   public final void setGender(int ged) {
      this.gender = ged;
      this.changed = true;
   }

   public final int getFace() {
      return this.face;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String n) {
      this.name = n;
      this.changed = true;
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

   public final int getItemId() {
      return this.itemid;
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
}
