package handling.login.handler;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutoRegister {
   private static final int ACCOUNTS_PER_IP = 100;
   public static final boolean autoRegister = false;

   public static boolean getAccountExists(String login) {
      boolean accountExists = false;
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT name FROM accounts WHERE name = ?");
         ps.setString(1, login);
         rs = ps.executeQuery();
         if (rs.first()) {
            accountExists = true;
         }

         ps.close();
         rs.close();
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
               rs.close();
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
               rs.close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      return accountExists;
   }

   public static boolean createAccount(String login, String pwd, String eip) {
      String sockAddr = eip;
      boolean success = false;

      Connection con;
      try {
         con = DatabaseConnection.getConnection();
      } catch (Exception var31) {
         var31.printStackTrace();
         return success;
      }

      try {
         PreparedStatement ipc = con.prepareStatement("SELECT SessionIP FROM accounts WHERE SessionIP = ?");

         try {
            ipc.setString(1, sockAddr.substring(1, sockAddr.lastIndexOf(58)));
            ResultSet rs = ipc.executeQuery();
            if (!rs.first() || rs.last() && rs.getRow() < 1) {
               try {
                  PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs, SessionIP) VALUES (?, ?, ?, ?, ?, ?)");

                  try {
                     ps.setString(1, login);
                     ps.setString(2, pwd);
                     ps.setString(3, "no@email.provided");
                     ps.setString(4, "2008-04-07");
                     ps.setString(5, "00-00-00-00-00-00");
                     ps.setString(6, sockAddr.substring(1, sockAddr.lastIndexOf(58)));
                     ps.executeUpdate();
                     ps.close();
                     if (ps != null) {
                        ps.close();
                     }
                  } catch (Throwable var32) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var30) {
                           var32.addSuppressed(var30);
                        }
                     }

                     throw var32;
                  }

                  success = true;
               } catch (SQLException var33) {
                  var33.printStackTrace();
                  if (ipc != null) {
                     ipc.close();
                  }

                  boolean var10 = success;
                  return var10;
               }
            }

            rs.close();
            ipc.close();
            if (ipc != null) {
               ipc.close();
            }

            return success;
         } catch (Throwable var34) {
            if (ipc != null) {
               try {
                  ipc.close();
               } catch (Throwable var29) {
                  var34.addSuppressed(var29);
               }
            }

            throw var34;
         }
      } catch (SQLException var35) {
         var35.printStackTrace();
         if (con == null) {
            return success;
         }

         try {
            con.close();
         } catch (SQLException var28) {
            var28.printStackTrace();
         }
      } finally {
         if (con != null) {
            try {
               con.close();
            } catch (SQLException var27) {
               var27.printStackTrace();
            }
         }

      }

      return success;
   }
}
