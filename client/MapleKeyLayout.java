package client;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

public class MapleKeyLayout implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   private boolean changed = false;
   private Map<Integer, Pair<Byte, Integer>> keymap;

   public MapleKeyLayout() {
      this.keymap = new HashMap();
   }

   public MapleKeyLayout(Map<Integer, Pair<Byte, Integer>> keys) {
      this.keymap = keys;
   }

   public final Map<Integer, Pair<Byte, Integer>> Layout() {
      this.changed = true;
      return this.keymap;
   }

   public final void unchanged() {
      this.changed = false;
   }

   public final void writeData(MaplePacketLittleEndianWriter mplew) {
      for(int x = 0; x < 89; ++x) {
         Pair<Byte, Integer> binding = (Pair)this.keymap.get(x);
         if (binding != null) {
            mplew.write((Byte)binding.getLeft());
            mplew.writeInt((Integer)binding.getRight());
         } else {
            mplew.write((int)0);
            mplew.writeInt(0);
         }
      }

      mplew.write((int)1);
      mplew.write((int)1);
   }

   public final void saveKeys(Connection con, int charid) throws SQLException {
      if (this.changed) {
         PreparedStatement ps = con.prepareStatement("DELETE FROM keymap WHERE characterid = ?");
         ps.setInt(1, charid);
         ps.execute();
         ps.close();
         if (!this.keymap.isEmpty()) {
            Iterator<Entry<Integer, Pair<Byte, Integer>>> key = this.keymap.entrySet().iterator();
            ps = con.prepareStatement("INSERT INTO keymap (`characterid`, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, charid);

            while(key.hasNext()) {
               Entry<Integer, Pair<Byte, Integer>> keybinding = (Entry)key.next();
               ps.setInt(2, (Integer)keybinding.getKey());
               ps.setInt(3, (Byte)((Pair)keybinding.getValue()).getLeft());
               ps.setInt(4, (Integer)((Pair)keybinding.getValue()).getRight());
               ps.execute();
            }

            ps.close();
         }
      }
   }
}
