package server.events;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import server.Randomizer;
import tools.Pair;

public class MapleOxQuizFactory {
   private final Map<Pair<Integer, Integer>, MapleOxQuizFactory.MapleOxQuizEntry> questionCache = new HashMap();
   private static final MapleOxQuizFactory instance = new MapleOxQuizFactory();

   public MapleOxQuizFactory() {
      this.initialize();
   }

   public static MapleOxQuizFactory getInstance() {
      return instance;
   }

   public Entry<Pair<Integer, Integer>, MapleOxQuizFactory.MapleOxQuizEntry> grabRandomQuestion() {
      int size = this.questionCache.size();

      while(true) {
         Iterator var2 = this.questionCache.entrySet().iterator();

         while(var2.hasNext()) {
            Entry<Pair<Integer, Integer>, MapleOxQuizFactory.MapleOxQuizEntry> oxquiz = (Entry)var2.next();
            if (Randomizer.nextInt(size) == 0) {
               return oxquiz;
            }
         }
      }
   }

   private void initialize() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM wz_oxdata");
         rs = ps.executeQuery();

         while(rs.next()) {
            this.questionCache.put(new Pair(rs.getInt("questionset"), rs.getInt("questionid")), this.get(rs));
         }

         rs.close();
         ps.close();
         con.close();
      } catch (Exception var13) {
         var13.printStackTrace();
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
         } catch (SQLException var12) {
            var12.printStackTrace();
         }

      }

   }

   private MapleOxQuizFactory.MapleOxQuizEntry get(ResultSet rs) throws SQLException {
      return new MapleOxQuizFactory.MapleOxQuizEntry(rs.getString("question"), rs.getString("display"), this.getAnswerByText(rs.getString("answer")), rs.getInt("questionset"), rs.getInt("questionid"));
   }

   private int getAnswerByText(String text) {
      if (text.equalsIgnoreCase("x")) {
         return 0;
      } else {
         return text.equalsIgnoreCase("o") ? 1 : -1;
      }
   }

   public static class MapleOxQuizEntry {
      private String question;
      private String answerText;
      private int answer;
      private int questionset;
      private int questionid;

      public MapleOxQuizEntry(String question, String answerText, int answer, int questionset, int questionid) {
         this.question = question;
         this.answerText = answerText;
         this.answer = answer;
         this.questionset = questionset;
         this.questionid = questionid;
      }

      public String getQuestion() {
         return this.question;
      }

      public String getAnswerText() {
         return this.answerText;
      }

      public int getAnswer() {
         return this.answer;
      }

      public int getQuestionSet() {
         return this.questionset;
      }

      public int getQuestionId() {
         return this.questionid;
      }
   }
}
