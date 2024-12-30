package web.mail;

import client.MapleClient;
import constants.ServerConstants;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class PlainMail {
   public static void main(MapleClient c, String msg) throws UnsupportedEncodingException {
      String host = "smtp.gmail.com";
      final String user = ServerConstants.mailid + "@gmail.com";
      final String password = ServerConstants.mailpw;
      Properties props = new Properties();
      props.put("mail.smtp.host", host);
      props.put("mail.smtp.port", 465);
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.ssl.enable", "true");
      props.put("mail.smtp.ssl.trust", host);
      Session session = Session.getDefaultInstance(props, new Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password);
         }
      });

      try {
         int num = Integer.parseInt(msg);
         int one = num / 1000;
         int two = (num - one * 1000) / 100;
         int four = num % 10;
         int three = (num % 100 - four) / 10;
         MimeMessage message = new MimeMessage(session);
         InternetAddress addr = new InternetAddress(user, "TheBlack", "UTF-8");
         message.setFrom(addr);
         message.addRecipient(RecipientType.TO, new InternetAddress(c.getEmail()));
         message.setSubject("[The Black] OTP 본인 인증 입니다.");
         message.setContent("<img src=\"https://black-s.vip/img/OTP/Black_OTP.png \"><br><img src=\"https://black-s.vip/img/OTP/Num" + one + ".png \"><img src=\"https://black-s.vip/img/OTP/Num" + two + ".png \"><img src=\"https://black-s.vip/img/OTP/Num" + three + ".png \"><img src=\"https://black-s.vip/img/OTP/Num" + four + ".png \">", "text/html");
         Transport.send(message);
         System.out.println(c.getAccountName() + " : Success Message Send");
      } catch (MessagingException var14) {
         var14.printStackTrace();
      }

   }
}
