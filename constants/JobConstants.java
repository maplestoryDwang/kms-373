package constants;

public class JobConstants {
   public static final boolean enableJobs = true;
   public static final int jobOrder = 35;

   public static enum LoginJob {
      Resistance(0, JobConstants.LoginJob.JobFlag.ENABLED),
      Adventurer(1, JobConstants.LoginJob.JobFlag.ENABLED),
      Cygnus(2, JobConstants.LoginJob.JobFlag.ENABLED),
      Aran(3, JobConstants.LoginJob.JobFlag.ENABLED),
      Evan(4, JobConstants.LoginJob.JobFlag.ENABLED),
      Mercedes(5, JobConstants.LoginJob.JobFlag.ENABLED),
      Demon(6, JobConstants.LoginJob.JobFlag.ENABLED),
      Phantom(7, JobConstants.LoginJob.JobFlag.ENABLED),
      DualBlade(8, JobConstants.LoginJob.JobFlag.ENABLED),
      Mihile(9, JobConstants.LoginJob.JobFlag.ENABLED),
      Luminous(10, JobConstants.LoginJob.JobFlag.ENABLED),
      Kaiser(11, JobConstants.LoginJob.JobFlag.ENABLED),
      AngelicBuster(12, JobConstants.LoginJob.JobFlag.ENABLED),
      Cannoneer(13, JobConstants.LoginJob.JobFlag.ENABLED),
      Xenon(14, JobConstants.LoginJob.JobFlag.ENABLED),
      Zero(15, JobConstants.LoginJob.JobFlag.ENABLED),
      EunWol(16, JobConstants.LoginJob.JobFlag.ENABLED),
      PinkBean(17, JobConstants.LoginJob.JobFlag.DISABLED),
      Kinesis(18, JobConstants.LoginJob.JobFlag.ENABLED),
      Kadena(19, JobConstants.LoginJob.JobFlag.ENABLED),
      Illium(20, JobConstants.LoginJob.JobFlag.ENABLED),
      Ark(21, JobConstants.LoginJob.JobFlag.ENABLED),
      PathFinder(22, JobConstants.LoginJob.JobFlag.ENABLED),
      Hoyeong(23, JobConstants.LoginJob.JobFlag.ENABLED),
      Adel(24, JobConstants.LoginJob.JobFlag.ENABLED),
      Cain(25, JobConstants.LoginJob.JobFlag.ENABLED),
      Yety(26, JobConstants.LoginJob.JobFlag.DISABLED),
      Lara(27, JobConstants.LoginJob.JobFlag.ENABLED),
      Khali(28, JobConstants.LoginJob.JobFlag.ENABLED);

      private final int jobType;
      private final int flag;

      private LoginJob(int jobType, JobConstants.LoginJob.JobFlag flag) {
         this.jobType = jobType;
         this.flag = flag.getFlag();
      }

      public int getJobType() {
         return this.jobType;
      }

      public int getFlag() {
         return this.flag;
      }

      public static enum JobFlag {
         DISABLED(0),
         ENABLED(1),
         LEVEL_DISABLED(2);

         private final int flag;

         private JobFlag(int flag) {
            this.flag = flag;
         }

         public int getFlag() {
            return this.flag;
         }
      }
   }
}
