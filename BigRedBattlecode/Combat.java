package bc19;

import java.util.ArrayList;

public class Combat {
   public static ArrayList<Robot> getVisibleEnemies(MyRobot robot) {
      // Iterate through the visible robots, and all visible enemies 
      // to the visibleEnemies ArrayList
      ArrayList<Robot> visibleEnemies = new ArrayList<Robot>();
      for (Robot r : robot.visibleRobots) {
         // the r.x >=0 is to make sure we can see the unit
         // and the unit isn't far away and signalling
         if (r.team != robot.me.team && r.x >= 0)
            visibleEnemies.add(r);
      }
      
      return visibleEnemies;
   }
   
   public static ArrayList<Robot> getVisibleCombatEnemies(MyRobot robot) {
      // Iterate through the visible robots, and all visible enemies 
      // to the visibleEnemies ArrayList
      ArrayList<Robot> visibleEnemies = new ArrayList<Robot>();
      for (Robot r : robot.visibleRobots) {
         // the r.x >=0 is to make sure we can see the unit
         // and the unit isn't far away and signalling
         if (r.team != robot.me.team && r.x >= 0 && (r.unit == robot.SPECS.PROPHET ||
                  r.unit == robot.SPECS.PREACHER || r.unit == robot.SPECS.CRUSADER))
            visibleEnemies.add(r);
      }
      
      return visibleEnemies;
   }
   
   public static ArrayList<Robot> getVisibleAllyTroops(MyRobot robot) {
      // Iterate through the visible robots, and all visible enemies 
      // to the visibleEnemies ArrayList
      ArrayList<Robot> visibleAllyTroops = new ArrayList<Robot>();
      for (Robot r : robot.visibleRobots) {
         // the r.x >=0 is to make sure we can see the unit
         if (r.team == robot.me.team && r.x >= 0 && (r.unit == robot.SPECS.PROPHET ||
                  r.unit == robot.SPECS.PREACHER || r.unit == robot.SPECS.CRUSADER))
            visibleAllyTroops.add(r);
      }
      
      return visibleAllyTroops;
   }
   
   /**
    * Finds the closest enemy from the visible robots. If enemy robot within view, return {@code null}.
    */
   public static Robot getClosestEnemy(MyRobot robot) {
      Robot closestRobot = null;
      
      // Iterate through the visible robots, and all visible enemies 
      // to the visibleEnemies ArrayList
      ArrayList<Robot> visibleEnemies = getVisibleEnemies(robot);

      // Find the closest enemy from the visibleEnemy arrayList
      if (visibleEnemies.size() > 0) {
         int minDist = Integer.MAX_VALUE;
         for (Robot r : visibleEnemies) {
            int dist = MapFunctions.
                     radiusDistance(new Tuple(robot.me.x, robot.me.y), 
                                      new Tuple(r.x, r.y));
            
            
            if (dist < minDist && dist <= robot.SPECS.UNITS[robot.me.unit].VISION_RADIUS) {
               minDist = dist;
               closestRobot = r;
            }
         }
      }
      
      return closestRobot;
   }
   
   /**
    * Finds the closest enemy from the visible robots. If enemy robot within view, return {@code null}.
    */
   public static Robot getClosestCombatEnemy(MyRobot robot) {
      Robot closestRobot = null;
      
      // Iterate through the visible robots, and all visible enemies 
      // to the visibleEnemies ArrayList
      ArrayList<Robot> visibleEnemies = getVisibleEnemies(robot);

      // Find the closest enemy from the visibleEnemy arrayList
      if (visibleEnemies.size() > 0) {
         int minDist = Integer.MAX_VALUE;
         for (Robot r : visibleEnemies) {
            int dist = MapFunctions.
                     radiusDistance(new Tuple(robot.me.x, robot.me.y), 
                                      new Tuple(r.x, r.y));
            if (dist < minDist && r.x >= 0 && (r.unit == robot.SPECS.PROPHET ||
                  r.unit == robot.SPECS.PREACHER || r.unit == robot.SPECS.CRUSADER)) {
               minDist = dist;
               closestRobot = r;
            }
         }
      }
      
      return closestRobot;
   }
   
   public static Robot getFarthestCombatAlly(MyRobot robot) {
      Robot farthestRobot = null;
      
      // Iterate through the visible robots, and all visible enemies 
      // to the visibleEnemies ArrayList
      ArrayList<Robot> visibleAllies = getVisibleAllyTroops(robot);

      // Find the closest enemy from the visibleEnemy arrayList
      if (visibleAllies.size() > 0) {
         int maxDist = 0;
         for (Robot r : visibleAllies) {
            int dist = MapFunctions.
                     radiusDistance(new Tuple(robot.me.x, robot.me.y), 
                                      new Tuple(r.x, r.y));
            if (dist > maxDist && r.x >= 0) {
               maxDist = dist;
               farthestRobot = r;
            }
         }
      }
      
      return farthestRobot;
   }
   
   public static int getMaxAttackRange(MyRobot r, Robot other)
   {
	   int[] range_arr = r.SPECS.UNITS[other.unit].ATTACK_RADIUS;
	   return range_arr[range_arr.length - 1];
   }
}