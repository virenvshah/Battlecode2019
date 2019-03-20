package bc19;

public class Crusader {
   public static void initialize(MyRobot myRobot) {
      GeneralUnit.initializeUnit(myRobot);
   }
   
   /**
    * Looks for castle signal and sets class variables such as moveMode and
    * attackMode based on those signals
    */
   public static void reactToCastleChurchSignal(MyRobot myRobot) {
      int[] types = {myRobot.SPECS.CASTLE, myRobot.SPECS.CHURCH};
      int signal = GeneralUnit.getSignalFrom(myRobot, types);
      
      if (signal < 10000 || signal >= 59999) {
         return;
      }
      
      int x = (signal % 10000) / 100;
      int y = signal % 100;
      if (!(x < myRobot.map.length && y < myRobot.map.length)) {
         return;
      }
      
      if (signal / 10000 >= 4) {
         myRobot.otherEnemyCastleLocs.add(new Tuple(x, y));
      } else {
         myRobot.closestEnemyLoc = new Tuple(x, y);
         if (signal / 10000 != 3) {
            myRobot.attackMode = true;
         }
      }
   }
   
   /**
    * Tries to attack the closest enemy, if enemy too far away, moves towards the enemy
    */
   public static Action attackClosestEnemy(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      Robot closestEnemy = Combat.getClosestEnemy(myRobot);
      if (closestEnemy != null) {
         Tuple enemyLoc = new Tuple(closestEnemy.x, closestEnemy.y);
         if (MapFunctions.radiusDistance(myLoc, enemyLoc) <= 16) {
            Tuple toEnemy = myRobot.diff(enemyLoc, myLoc);
            myRobot.closestEnemyLoc = enemyLoc;
            myRobot.enemyDirection = Directions.dirTowards(myLoc, enemyLoc);
            return myRobot.attack(toEnemy.x, toEnemy.y);
         } else {
            Tuple tup = MapFunctions.moveTo(myRobot, enemyLoc, 3, 0); 
            if (tup != null) {
               return myRobot.move(tup.x, tup.y);
            } 
         }
      }
      
      return null;
   }
}