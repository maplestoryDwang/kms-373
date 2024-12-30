package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import server.ServerProperties;

public class DatabaseConnection {
   private static DataSource dataSource;
   private static GenericObjectPool connectionPool;
   private static String databaseName;
   private static int databaseMajorVersion;
   private static int databaseMinorVersion;
   private static String databaseProductVersion;
   private static int MySQLMINCONNECTION = 100;
   private static int MySQLMAXCONNECTION = 2100000000;
   public static final String MYSQLSCHEMA = ServerProperties.getProperty("query.schema");
   public static final String MySQLUSER = ServerProperties.getProperty("query.user");
   public static final String MySQLPASS = ServerProperties.getProperty("query.password");
   public static final String MySQLURL;
   public static final int CLOSE_CURRENT_RESULT = 1;
   public static final int KEEP_CURRENT_RESULT = 2;
   public static final int CLOSE_ALL_RESULTS = 3;
   public static final int SUCCESS_NO_INFO = -2;
   public static final int EXECUTE_FAILED = -3;
   public static final int RETURN_GENERATED_KEYS = 1;
   public static final int NO_GENERATED_KEYS = 2;

   public static synchronized void init() {
      if (dataSource == null) {
         try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
         } catch (Throwable var3) {
            var3.printStackTrace();
            System.exit(1);
         }

         connectionPool = new GenericObjectPool();
         if (MySQLMINCONNECTION > MySQLMAXCONNECTION) {
            MySQLMAXCONNECTION = MySQLMINCONNECTION;
         }

         connectionPool.setMaxIdle(MySQLMINCONNECTION);
         connectionPool.setMaxActive(MySQLMAXCONNECTION);
         connectionPool.setTestOnBorrow(true);
         connectionPool.setMaxWait(5000L);

         try {
            dataSource = setupDataSource();
            Connection c = getConnection();
            DatabaseMetaData dmd = c.getMetaData();
            databaseName = dmd.getDatabaseProductName();
            databaseMajorVersion = dmd.getDatabaseMajorVersion();
            databaseMinorVersion = dmd.getDatabaseMinorVersion();
            databaseProductVersion = dmd.getDatabaseProductVersion();
            c.close();
         } catch (Exception var2) {
            System.exit(1);
         }

      }
   }

   private static DataSource setupDataSource() throws Exception {
      ConnectionFactory conFactory = new DriverManagerConnectionFactory(MySQLURL, MySQLUSER, MySQLPASS);
      new PoolableConnectionFactoryAE(conFactory, connectionPool, (KeyedObjectPoolFactory)null, 1, false, true);
      return new PoolingDataSource(connectionPool);
   }

   public static void closeObject(Connection con) {
      try {
         con.close();
      } catch (Exception var5) {
      } finally {
         con = null;
      }

   }

   public static synchronized void shutdown() {
      try {
         connectionPool.close();
      } catch (Exception var1) {
      }

      dataSource = null;
   }

   public static Connection getConnection() throws SQLException {
      if (connectionPool.getNumIdle() == 0) {
         connectionPool.setMaxActive(Math.min(connectionPool.getMaxActive() + 1, 10000000));
      }

      Connection con = dataSource.getConnection();
      return con;
   }

   public static int getActiveConnections() {
      return connectionPool.getNumActive();
   }

   public static int getIdleConnections() {
      return connectionPool.getNumIdle();
   }

   static {
      MySQLURL = "jdbc:mysql://localhost:3306/" + MYSQLSCHEMA + "?autoReconnect=true&characterEncoding=euckr&maxReconnects=5";
   }
}
