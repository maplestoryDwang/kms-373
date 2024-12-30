package server.games;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import server.Randomizer;

public class OXQuizProvider {
   public static OXQuiz[] getQuizList(int amount) {
      OXQuiz[] list = new OXQuiz[amount];
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM OXQuiz", 1004, 1007);
         rs = ps.executeQuery();
         rs.last();
         int rowcount = rs.getRow();
         rs.beforeFirst();
         ps.close();
         rs.close();

         for(int a = 0; a < amount; ++a) {
            int quizid = Randomizer.rand(0, rowcount);
            ps = con.prepareStatement("SELECT * FROM OXQuiz WHERE Id = ?");
            ps.setInt(1, quizid);
            rs = ps.executeQuery();
            if (rs.next()) {
               String Question = rs.getString("Question");
               String Explaination = rs.getString("Explaination");
               if (Question.endsWith("= ") || Question.endsWith("= -")) {
                  continue;
               }

               boolean isX = !rs.getString("Result").equals("O");
               OXQuiz quiz = new OXQuiz(Question, Explaination, isX);
               list[a] = quiz;
            }

            ps.close();
            rs.close();
         }

         con.close();
      } catch (SQLException var20) {
         var20.printStackTrace();
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
         } catch (SQLException var19) {
            var19.printStackTrace();
         }

      }

      return list;
   }

   public static List<OXQuiz> getQuizList2(int amount) {
      List<OXQuiz> quizes = new ArrayList();
      OXQuiz[] var2 = getQuizList(amount);
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         OXQuiz q = var2[var4];
         quizes.add(q);
      }

      return quizes;
   }
}
