package bc19;

public class Preacher {
   public static void initialize(MyRobot myRobot) {
      GeneralUnit.initializeUnit(myRobot);
   }
   
   /**
    * Looks for castle signal and sets class variables such as moveMode and
    * attackMode based on those signals
    */
   public static void reactToCastleSignal(MyRobot myRobot) {
      int[] types = {myRobot.SPECS.CASTLE, myRobot.SPECS.CHURCH};
      int signal = GeneralUnit.getSignalFrom(myRobot, types);
      
      if (signal < 10000 || signal >= 50000) {
         // no enemies nearby
         myRobot.alert = false;
         return;
      }
      
      if (signal <= 26464) {
         int x = (signal % 10000) / 100;
         int y = signal % 100;
         myRobot.closestEnemyLoc = new Tuple(x, y);
         myRobot.alert = true;
         
         if (signal / 10000 == 1) {
            myRobot.moveMode = true;
         } else {
            // there is a prophet
            myRobot.moveModeProphet = true;
         }
         
         for (Robot r : myRobot.visibleRobots) {
            Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
            Tuple rLoc = new Tuple(r.x, r.y);
            // if the castle is adjacent (means it spawned you), set your castleLoc
            // to that castleLoc
            if (r.unit == myRobot.SPECS.CHURCH && r.x >= 0 &&
                     MapFunctions.radiusDistance(rLoc, myLoc) <= 2) {
               myRobot.castleLoc = myRobot.home = rLoc;
               myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc,
                        myRobot.karboDepots);
               myRobot.enemyDirection = Directions.dirTowards(myRobot.castleLoc, 
                        myRobot.enemyCastleLoc);
               GeneralUnit.initializeMiningLocations(myRobot, 1);
               GeneralUnit.initializeMiningLocations(myRobot, 2);
               break;
            }
         }
         
         myRobot.rushMode = false;
      } else if (signal == 59999) {
         myRobot.attackMode = true;
      }
   }
   
   public static AttackAction attackClosestEnemy(MyRobot myRobot) {
      return GeneralUnit.attackClosestEnemy(myRobot);
   }
   
}