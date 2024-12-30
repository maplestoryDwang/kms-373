package handling.login;

import client.MapleClient;
import handling.channel.ChannelServer;
import handling.login.handler.CharLoginHandler;
import java.util.Map;
import tools.packet.CWvsContext;
import tools.packet.LoginPacket;

public class LoginWorker {
   private static long lastUpdate = 0L;

   public static void registerClient(MapleClient c, String id, String pwd) {
      if (LoginServer.isAdminOnly() && !c.isGm() && !c.isLocalhost()) {
         c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "서버 점검중입니다."));
         c.getSession().writeAndFlush(LoginPacket.getLoginFailed(21));
      } else {
         if (System.currentTimeMillis() - lastUpdate > 600000L) {
            lastUpdate = System.currentTimeMillis();
            Map<Integer, Integer> load = ChannelServer.getChannelLoad();
            int usersOn = 0;
            if (load == null || load.size() <= 0) {
               lastUpdate = 0L;
               c.getSession().writeAndFlush(LoginPacket.getLoginFailed(7));
               return;
            }

            LoginServer.setLoad(load, usersOn);
            lastUpdate = System.currentTimeMillis();
         }

         if (c.finishLogin() == 0) {
            c.getSession().writeAndFlush(LoginPacket.checkLogin());
            c.getSession().writeAndFlush(LoginPacket.getAuthSuccessRequest(c, id, pwd));
            CharLoginHandler.ServerListRequest(c, false);
         } else {
            c.getSession().writeAndFlush(LoginPacket.getLoginFailed(7));
         }
      }
   }
}
