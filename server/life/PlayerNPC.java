package server.life;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.World;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.maps.MapleMap;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class PlayerNPC extends MapleNPC {
   private Map<Byte, Integer> equips = new HashMap();
   private Map<Byte, Integer> secondEquips = new HashMap();
   private int mapid;
   private int face;
   private int hair;
   private int charId;
   private int secondFace;
   private byte skin;
   private byte gender;
   private byte secondSkin;
   private byte secondGender;
   private int[] pets = new int[3];

   public PlayerNPC(ResultSet rs) throws Exception {
      super(rs.getInt("ScriptId"), rs.getString("name"));
      this.hair = rs.getInt("hair");
      this.face = rs.getInt("face");
      this.mapid = rs.getInt("map");
      this.skin = rs.getByte("skin");
      this.charId = rs.getInt("charid");
      this.gender = rs.getByte("gender");
      this.setCoords(rs.getInt("x"), rs.getInt("y"), rs.getInt("dir"), rs.getInt("Foothold"));
      String[] pet = rs.getString("pets").split(",");

      for(int i = 0; i < 3; ++i) {
         if (pet[i] != null) {
            this.pets[i] = Integer.parseInt(pet[i]);
         } else {
            this.pets[i] = 0;
         }
      }

      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs2 = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM playernpcs_equip WHERE NpcId = ?");
         ps.setInt(1, this.getId());
         rs2 = ps.executeQuery();

         while(rs2.next()) {
            this.equips.put(rs2.getByte("equippos"), rs2.getInt("equipid"));
         }

         rs2.close();
         ps.close();
         con.close();
      } catch (Exception var18) {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs2 != null) {
               rs2.close();
            }
         } catch (SQLException var17) {
            var17.printStackTrace();
         }
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs2 != null) {
               rs2.close();
            }
         } catch (SQLException var16) {
            var16.printStackTrace();
         }

      }

   }

   public PlayerNPC(MapleCharacter cid, int npc, MapleMap map, MapleCharacter base) {
      super(npc, cid.getName());
      this.charId = cid.getId();
      this.mapid = map.getId();
      this.setCoords(base.getTruePosition().x, base.getTruePosition().y, 0, base.getFH());
      this.update(cid);
   }

   public void setCoords(int x, int y, int f, int fh) {
      this.setPosition(new Point(x, y));
      this.setCy(y);
      this.setRx0(x - 50);
      this.setRx1(x + 50);
      this.setF(f);
      this.setFh(fh);
   }

   public static void loadAll() {
      List<PlayerNPC> toAdd = new ArrayList();
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM playernpcs");
         rs = ps.executeQuery();

         while(rs.next()) {
            toAdd.add(new PlayerNPC(rs));
         }

         rs.close();
         ps.close();
      } catch (Exception var16) {
         var16.printStackTrace();

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
         } catch (SQLException var15) {
            var15.printStackTrace();
         }
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
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      Iterator var4 = toAdd.iterator();

      while(var4.hasNext()) {
         PlayerNPC npc = (PlayerNPC)var4.next();
         npc.addToServer();
      }

   }

   public static void updateByCharId(MapleCharacter chr) {
      if (World.Find.findChannel(chr.getId()) > 0) {
         Iterator var1 = ChannelServer.getInstance(World.Find.findChannel(chr.getId())).getAllPlayerNPC().iterator();

         while(var1.hasNext()) {
            PlayerNPC npc = (PlayerNPC)var1.next();
            npc.update(chr);
         }
      }

   }

   public void addToServer() {
      Iterator var1 = ChannelServer.getAllInstances().iterator();

      while(var1.hasNext()) {
         ChannelServer cserv = (ChannelServer)var1.next();
         cserv.addPlayerNPC(this);
      }

   }

   public void removeFromServer() {
      Iterator var1 = ChannelServer.getAllInstances().iterator();

      while(var1.hasNext()) {
         ChannelServer cserv = (ChannelServer)var1.next();
         cserv.removePlayerNPC(this);
      }

   }

   public void update(MapleCharacter chr) {
      if (chr != null && this.charId == chr.getId()) {
         this.setName(chr.getName());
         this.setHair(chr.getHair());
         this.setFace(chr.getFace());
         this.setSkin(chr.getSkinColor());
         this.setGender(chr.getGender());
         this.equips = new HashMap();
         Iterator var2 = chr.getInventory(MapleInventoryType.EQUIPPED).newList().iterator();

         while(var2.hasNext()) {
            Item item = (Item)var2.next();
            if (item.getPosition() >= -127) {
               this.equips.put((byte)item.getPosition(), item.getItemId());
            }
         }

         this.saveToDB();
      }
   }

   public void destroy() {
      this.destroy(false);
   }

   public void destroy(boolean remove) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("DELETE FROM playernpcs WHERE scriptid = ?");
         ps.setInt(1, this.getId());
         ps.executeUpdate();
         ps.close();
         ps = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ?");
         ps.setInt(1, this.getId());
         ps.executeUpdate();
         ps.close();
         if (remove) {
            this.removeFromServer();
         }
      } catch (Exception var17) {
         var17.printStackTrace();

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
         } catch (SQLException var16) {
            var16.printStackTrace();
         }
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
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

   }

   public void saveToDB() {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         if (this.getNPCFromWZ() != null) {
            con = DatabaseConnection.getConnection();
            this.destroy();
            ps = con.prepareStatement("INSERT INTO playernpcs(name, hair, face, skin, x, y, map, charid, scriptid, foothold, dir, gender, pets) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, this.getName());
            ps.setInt(2, this.getHair());
            ps.setInt(3, this.getFace());
            ps.setInt(4, this.getSkinColor());
            ps.setInt(5, this.getTruePosition().x);
            ps.setInt(6, this.getTruePosition().y);
            ps.setInt(7, this.getMapId());
            ps.setInt(8, this.getCharId());
            ps.setInt(9, this.getId());
            ps.setInt(10, this.getFh());
            ps.setInt(11, this.getF());
            ps.setInt(12, this.getGender());
            String[] pet = new String[]{"0", "0", "0"};

            for(int i = 0; i < 3; ++i) {
               if (this.pets[i] > 0) {
                  pet[i] = String.valueOf(this.pets[i]);
               }
            }

            ps.setString(13, pet[0] + "," + pet[1] + "," + pet[2]);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO playernpcs_equip(npcid, charid, equipid, equippos) VALUES (?, ?, ?, ?)");
            ps.setInt(1, this.getId());
            ps.setInt(2, this.getCharId());
            Iterator var20 = this.equips.entrySet().iterator();

            while(var20.hasNext()) {
               Entry<Byte, Integer> equip = (Entry)var20.next();
               ps.setInt(3, (Integer)equip.getValue());
               ps.setInt(4, (Byte)equip.getKey());
               ps.executeUpdate();
            }

            ps.close();
            return;
         }

         this.destroy(true);
      } catch (Exception var18) {
         var18.printStackTrace();

         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
               return;
            }
         } catch (SQLException var17) {
            var17.printStackTrace();
         }

         return;
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
         } catch (SQLException var16) {
            var16.printStackTrace();
         }

      }

   }

   public short getJob() {
      return 0;
   }

   public int getDemonMarking() {
      return 0;
   }

   public Map<Byte, Integer> getEquips(boolean moru) {
      return this.equips;
   }

   public Map<Byte, Integer> getSecondEquips(boolean moru) {
      return this.secondEquips;
   }

   public Map<Byte, Integer> getTotems() {
      return new HashMap();
   }

   public byte getSkinColor() {
      return this.skin;
   }

   public byte getSecondSkinColor() {
      return this.secondSkin;
   }

   public byte getGender() {
      return this.gender;
   }

   public byte getSecondGender() {
      return this.secondGender;
   }

   public int getFace() {
      return this.face;
   }

   public int getSecondFace() {
      return this.secondFace;
   }

   public int getHair() {
      return this.hair;
   }

   public int getCharId() {
      return this.charId;
   }

   public int getMapId() {
      return this.mapid;
   }

   public void setSkin(byte s) {
      this.skin = s;
   }

   public void setFace(int f) {
      this.face = f;
   }

   public void setHair(int h) {
      this.hair = h;
   }

   public void setGender(int g) {
      this.gender = (byte)g;
   }

   public int getPet(int i) {
      return this.pets[i] > 0 ? this.pets[i] : 0;
   }

   public void setPets(List<MaplePet> p) {
      for(int i = 0; i < 3; ++i) {
         if (p != null && p.size() > i && p.get(i) != null) {
            this.pets[i] = ((MaplePet)p.get(i)).getPetItemId();
         } else {
            this.pets[i] = 0;
         }
      }

   }

   public void sendSpawnData(MapleClient client) {
      client.getSession().writeAndFlush(CField.NPCPacket.spawnNPC(this, true));
      client.getSession().writeAndFlush(CWvsContext.spawnPlayerNPC(this, client.getPlayer()));
      client.getSession().writeAndFlush(CField.NPCPacket.spawnNPCRequestController(this, true));
   }

   public MapleNPC getNPCFromWZ() {
      MapleNPC npc = MapleLifeFactory.getNPC(this.getId());
      if (npc != null) {
         npc.setName(this.getName());
      }

      return npc;
   }
}
