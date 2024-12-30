package database;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;

public class PoolableConnectionFactoryAE extends PoolableConnectionFactory {
   private final int validationTimeout;

   public PoolableConnectionFactoryAE(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, int validationTimeout, boolean defaultReadOnly, boolean defaultAutoCommit) {
      super(connFactory, pool, stmtPoolFactory, (String)null, defaultReadOnly, defaultAutoCommit);
      this.validationTimeout = validationTimeout;
   }

   public void validateConnection(Connection conn) throws SQLException {
      if (conn.isClosed()) {
         throw new SQLException("validateConnection: connection closed");
      } else if (this.validationTimeout >= 0 && !conn.isValid(this.validationTimeout)) {
         throw new SQLException("validateConnection: connection invalid");
      }
   }
}
