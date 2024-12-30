package handling.world;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

public class MapleMessenger implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   private MapleMessengerCharacter[] members = new MapleMessengerCharacter[3];
   private String[] silentLink = new String[3];
   private int id;

   public MapleMessenger(int id, MapleMessengerCharacter chrfor) {
      this.id = id;
      this.addMem(0, chrfor);
   }

   public void addMem(int pos, MapleMessengerCharacter chrfor) {
      if (this.members[pos] == null) {
         this.members[pos] = chrfor;
      }
   }

   public boolean containsMembers(MapleMessengerCharacter member) {
      return this.getPositionByName(member.getName()) < 4;
   }

   public void addMember(MapleMessengerCharacter member) {
      int position = this.getLowestPosition();
      if (position > -1 && position < 4) {
         this.addMem(position, member);
      }

   }

   public void removeMember(MapleMessengerCharacter member) {
      int position = this.getPositionByName(member.getName());
      if (position > -1 && position < 4) {
         this.members[position] = null;
      }

   }

   public void silentRemoveMember(MapleMessengerCharacter member) {
      int position = this.getPositionByName(member.getName());
      if (position > -1 && position < 4) {
         this.members[position] = null;
         this.silentLink[position] = member.getName();
      }

   }

   public void silentAddMember(MapleMessengerCharacter member) {
      for(int i = 0; i < this.silentLink.length; ++i) {
         if (this.silentLink[i] != null && this.silentLink[i].equalsIgnoreCase(member.getName())) {
            this.addMem(i, member);
            this.silentLink[i] = null;
            return;
         }
      }

   }

   public void updateMember(MapleMessengerCharacter member) {
      for(int i = 0; i < this.members.length; ++i) {
         MapleMessengerCharacter chr = this.members[i];
         if (chr != null && chr.equals(member)) {
            this.members[i] = null;
            this.addMem(i, member);
            return;
         }
      }

   }

   public int getLowestPosition() {
      for(int i = 0; i < this.members.length; ++i) {
         if (this.members[i] == null) {
            return i;
         }
      }

      return 4;
   }

   public int getPositionByName(String name) {
      for(int i = 0; i < this.members.length; ++i) {
         MapleMessengerCharacter messengerchar = this.members[i];
         if (messengerchar != null && messengerchar.getName().equalsIgnoreCase(name)) {
            return i;
         }
      }

      return 4;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int hashCode() {
      return 31 + this.id;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         MapleMessenger other = (MapleMessenger)obj;
         return this.id == other.id;
      }
   }

   public Collection<MapleMessengerCharacter> getMembers() {
      return Arrays.asList(this.members);
   }

   public boolean isMonitored() {
      int ch = true;
      MapleMessengerCharacter[] var2 = this.members;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         MapleMessengerCharacter m = var2[var4];
         if (m != null) {
            int ch = World.Find.findChannel(m.getName());
            if (ch != -1) {
               MapleCharacter mc = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(m.getName());
               if (mc != null && mc.getClient() != null && mc.getClient().isMonitored()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public String getMemberNamesDEBUG() {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < this.members.length; ++i) {
         if (this.members[i] != null) {
            sb.append(this.members[i].getName());
            if (i != this.members.length - 1) {
               sb.append(',');
            }
         }
      }

      return sb.toString();
   }
}
