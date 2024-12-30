package handling.world.party;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MapleParty implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   private MaplePartyCharacter leader;
   private List<MaplePartyCharacter> members = new LinkedList();
   private int id;
   private byte visible;
   private byte drop;
   private boolean disbanded = false;
   private String partytitle;

   public MapleParty(int id, MaplePartyCharacter chrfor) {
      this.leader = chrfor;
      this.members.add(this.leader);
      this.id = id;
   }

   public byte getVisible() {
      return this.visible;
   }

   public void setVisible(byte set) {
      this.visible = set;
   }

   public byte getPartyDrop() {
      return this.drop;
   }

   public void setPartyDrop(byte set) {
      this.drop = set;
   }

   public String getPatryTitle() {
      return this.partytitle;
   }

   public void setPartyTitle(String title) {
      this.partytitle = title;
   }

   public boolean containsMembers(MaplePartyCharacter member) {
      return this.members.contains(member);
   }

   public void addMember(MaplePartyCharacter member) {
      this.members.add(member);
   }

   public void removeMember(MaplePartyCharacter member) {
      this.members.remove(member);
   }

   public void updateMember(MaplePartyCharacter member) {
      for(int i = 0; i < this.members.size(); ++i) {
         MaplePartyCharacter chr = (MaplePartyCharacter)this.members.get(i);
         if (chr.equals(member)) {
            this.members.set(i, member);
         }
      }

   }

   public MaplePartyCharacter getMemberById(int id) {
      Iterator var2 = this.members.iterator();

      MaplePartyCharacter chr;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         chr = (MaplePartyCharacter)var2.next();
      } while(chr.getId() != id);

      return chr;
   }

   public MaplePartyCharacter getMemberByIndex(int index) {
      return (MaplePartyCharacter)this.members.get(index);
   }

   public Collection<MaplePartyCharacter> getMembers() {
      return new LinkedList(this.members);
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public MaplePartyCharacter getLeader() {
      return this.leader;
   }

   public void setLeader(MaplePartyCharacter nLeader) {
      this.leader = nLeader;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + this.id;
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         MapleParty other = (MapleParty)obj;
         return this.id == other.id;
      }
   }

   public boolean isDisbanded() {
      return this.disbanded;
   }

   public void disband() {
      this.disbanded = true;
   }
}
