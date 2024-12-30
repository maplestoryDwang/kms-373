package constants.programs;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GarbageDataBaseRemover {
   public static void main() {
      System.out.println("[알림] GarbageDataBaseRemover Running...\r\n");
      int deletedrows = 0;

      try {
         DatabaseConnection.init();
         System.out.println("[keyvalue_boss]");
         System.out.println("[알림] Deleting the garbage database from the 'keyvalue_boss' table...\r\n");
         Connection c = DatabaseConnection.getConnection();
         PreparedStatement p = c.prepareStatement("SELECT * FROM keyvalue_boss");
         ResultSet r = p.executeQuery();

         while(r.next()) {
            int id = r.getInt("id");
            PreparedStatement b = c.prepareStatement("SELECT * FROM `keyvalue_boss` WHERE id = ?");
            b.setInt(1, id);
            ResultSet rs = b.executeQuery();
            if (!rs.next()) {
               PreparedStatement d = c.prepareStatement("DELETE FROM keyvalue_boss WHERE id = ?");
               d.setInt(1, id);
               d.executeUpdate();
               ++deletedrows;
               d.close();
            }

            b.close();
            rs.close();
         }

         p.close();
         r.close();
      } catch (Throwable var8) {
         var8.printStackTrace();
      }

      System.out.println("[알림] 데이터베이스 정리 프로그램에서 " + deletedrows + "개의 행을 제거하였습니다.");
   }
}
