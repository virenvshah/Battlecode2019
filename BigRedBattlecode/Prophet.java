package bc19;

import java.util.ArrayList;

public class Prophet {
   public static void initialize(MyRobot myRobot) {
      GeneralUnit.initializeUnit(myRobot);
   }

   public static Action kite(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      Robot closestEnemy = Combat.getClosestEnemy(myRobot);

      if (closestEnemy == null) {
         return null;
      }

      Tuple enemyLoc = new Tuple(closestEnemy.x, closestEnemy.y);

      if (MapFunctions.radiusDistance(myLoc, new Tuple(closestEnemy.x, closestEnemy.y)) > 16) {
         Tuple toEnemy = myRobot.diff(enemyLoc, myLoc);
         myRobot.closestEnemyLoc = enemyLoc;
         myRobot.enemyDirection = Directions.dirTowards(myLoc, enemyLoc);
         myRobot.log("Enemy that is going to be attacked is " + enemyLoc.toString());
         return myRobot.attack(toEnemy.x, toEnemy.y);
      }
      // if the enemy is too close move backwards
      else if (MapFunctions.radiusDistance(myLoc, enemyLoc) <= 16) {
         // adding 10 to direction activates speedy movement in randomMove
         Tuple tup = myRobot.randomMove(myLoc,
                  10 + Directions.dirTowards(enemyLoc, myLoc));
         if (tup != null) {
            return myRobot.move(tup.x, tup.y);
         }
      }

      return null;
   }

   /**
    * Looks for castle signal and sets class variables such as moveMode and
    * attackMode based on those signals.  ONLY FOR RUSH PROPHET (such as in
    * FourProphetRush.java
    */
   public static void reactSignalRush(MyRobot myRobot) {
      int[] types = {myRobot.SPECS.CASTLE};
      int signal = GeneralUnit.getSignalFrom(myRobot, types);

      if (signal < 0) return;

      if (signal / 10000 == 5) {
         myRobot.moveMode = true;
      }

      if (signal / 10000 >= 4) {
         int x_coor = (signal % 10000) / 100;
         int y_coor = signal % 100;
         if (x_coor < myRobot.map.length && y_coor < myRobot.map.length) {
            myRobot.otherEnemyCastleLocs.add(new Tuple(x_coor, y_coor));
         }
      }
   }

   /**
    * Looks for castle signal and sets class variables such as moveMode and
    * attackMode based on those signals.  ONLY FOR RUSH PROPHET (such as in
    * FourProphetRush.java
    */
   public static void reactSignalHold(MyRobot myRobot) {
      int[] types = {myRobot.SPECS.CASTLE};
      int signal = GeneralUnit.getSignalFrom(myRobot, types);

      if (signal < 0) return;

      if (signal / 10000 == 4) {
        int x_coor = (signal % 10000) / 100;
        int y_coor = signal % 100;
        if (x_coor < myRobot.map.length && y_coor < myRobot.map.length) {
           myRobot.otherEnemyCastleLocs.add(new Tuple(x_coor, y_coor));
           myRobot.moveMode = true;
        }
      }
   }

   public static boolean catchUp(MyRobot myRobot) {
      if (myRobot.currentPath == null) {
         return false;
      }

      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      Tuple nextLoc = myRobot.currentPath.peekLocation(1);
      int nextDir = Directions.dirTowards(myLoc, nextLoc);
      // if nextDir is North, nextDirLeft is North-West
      int nextDirLeft = (nextDir + 7) % 8;
      Tuple nextLocLeft = myRobot.add(myLoc, Directions.dirs[nextDirLeft]);
      int nextDirRight = (nextDir + 1) % 8;
      Tuple nextLocRight = myRobot.add(myLoc, Directions.dirs[nextDirRight]);

      // the next adjacent tiles in the direction you want to go are occupied
      if ((!myRobot.map[nextLoc.y][nextLoc.x] || myRobot.visibleMap[nextLoc.y][nextLoc.x] > 0) &&
               (!myRobot.map[nextLocLeft.y][nextLocLeft.x] ||
                        myRobot.visibleMap[nextLocLeft.y][nextLocLeft.x] > 0) &&
               (!myRobot.map[nextLocRight.y][nextLocRight.x] ||
                        myRobot.visibleMap[nextLocRight.y][nextLocRight.x] > 0)) {
         return false;
      }

      int[] types = {myRobot.SPECS.PROPHET};
      ArrayList<Robot> allyTroops = GeneralUnit.getRobotsWithinRadius(myRobot, types, 64);
      int enemyCastleDir = Directions.dirTowards(myLoc, myRobot.enemyCastleLoc);

      for (Robot r : allyTroops) {
         Tuple rLoc = new Tuple(r.x, r.y);
         int robotDir = Directions.dirTowards(myLoc, rLoc);
         if (robotDir == nextDir || robotDir == enemyCastleDir) {
            return true;
         }
      }

      return false;
   }

   /**
    * Looks for castle signal and sets class variables such as moveMode and
    * attackMode based on those signals.  ONLY FOR RUSH PROPHET (such as in
    * FourProphetRush.java
    */
   public static void reactSignalWallInitialize(MyRobot myRobot) {
      int[] types = {myRobot.SPECS.CASTLE, myRobot.SPECS.CHURCH};
      int signal = GeneralUnit.getSignalFrom(myRobot, types);

      if (signal < 0) return;

      myRobot.turn = signal >> 12;
      myRobot.log("Turn is " + myRobot.turn);

      int x_coor = (signal >> 6) & 63;
      int y_coor = signal & 63;
      myRobot.log("Enemy x-coor is " + x_coor + " enemy y-coor is " + y_coor);
      if (x_coor < myRobot.map.length && y_coor < myRobot.map.length) {
         myRobot.otherEnemyCastleLocs.add(new Tuple(x_coor, y_coor));
      }
   }
}
