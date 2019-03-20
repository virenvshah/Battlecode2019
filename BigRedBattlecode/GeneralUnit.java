package bc19;

import java.util.ArrayList;

public class GeneralUnit {
   /**
    * Initializes basic class variables such as enemyCastleLocation
    */
   public static void initializeUnit(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      
      if (myRobot.me.unit == myRobot.SPECS.CASTLE) {
         myRobot.castleLoc = myLoc;
      } else {
         for (Robot r : myRobot.visibleRobots) {
            Tuple rLoc = new Tuple(r.x, r.y);
            
            // if the castle is adjacent (means it spawned you), set your castleLoc
            // to that castleLoc
            if (r.unit == myRobot.SPECS.CASTLE && r.x >= 0 &&
                     MapFunctions.radiusDistance(rLoc, myLoc) <= 2) {
               myRobot.castleLoc = myRobot.home = rLoc;
               break;
            } 
            
            // if spawned by a church, set your castle loc to the signal sent by the 
            // church
            else if (r.unit == myRobot.SPECS.CHURCH && r.x >= 0 &&
                     MapFunctions.radiusDistance(rLoc, myLoc) <= 2 && r.signal >= 0) {
               if (myRobot.me.unit == myRobot.SPECS.PROPHET) {
                  myRobot.castleLoc = new Tuple((r.signal >> 6) & 63, r.signal & 63);
                  myRobot.home = rLoc;
               } else if (myRobot.me.unit != myRobot.SPECS.PILGRIM) {
                  myRobot.castleLoc = new Tuple((r.signal % 10000) / 100, r.signal % 100);
                  myRobot.home = rLoc;
                  break;
               } else {
                  myRobot.castleLoc = myRobot.home = rLoc;
                  break;
               }
            }
         }
      }

      // we don't know whether the map is vertically or horizontally symmetrical
      // hence we need to use the Karbonite locations to determine the symmetry
      // of the map.  getEnemyLoc() handles this for us if we pass in not only 
      // our own castle location but the Karbonite locations too.
      myRobot.karboDepots = myRobot.getTypedTiles(myRobot.tileNodeMap, 1);
      myRobot.fuelDepots = myRobot.getTypedTiles(myRobot.tileNodeMap, 2);
      myRobot.numKarbo = myRobot.karboDepots.size();
      myRobot.numFuel = myRobot.fuelDepots.size();
      myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc,
               myRobot.karboDepots);
      myRobot.enemyDirection = Directions.dirTowards(myRobot.castleLoc, 
               myRobot.enemyCastleLoc);
      initializeMiningLocations(myRobot, 1);
      initializeMiningLocations(myRobot, 2);
      // in the beginning its the only enemy we know of
      myRobot.closestEnemyLoc = myRobot.enemyCastleLoc;
   }
   
   public static void initializeMiningLocations(MyRobot myRobot, int type) {      
      ArrayList<TileNode> locations;
      if (type == 1) {
         myRobot.homeKarboDepots = new ArrayList<TileNode>();
         myRobot.enemyKarboDepots = new ArrayList<TileNode>();
         locations = myRobot.karboDepots;
      } else {
         myRobot.homeFuelDepots = new ArrayList<TileNode>();
         myRobot.enemyFuelDepots = new ArrayList<TileNode>();
         locations = myRobot.fuelDepots;
      }
      
      for (TileNode loc : locations) {
         if (myRobot.horizRefl) {
            if (myRobot.castleLoc.x >= myRobot.map.length / 2) {
               if (loc.location.x >= myRobot.map.length / 2) {
                  if (type == 1) {
                     myRobot.homeKarboDepots.add(loc);
                  } else {
                     myRobot.homeFuelDepots.add(loc);
                  }
               } else {
                  if (type == 1) {
                     myRobot.enemyKarboDepots.add(loc);
                  } else {
                     myRobot.enemyFuelDepots.add(loc);
                  }
               }
            } else {
               if (loc.location.x < myRobot.map.length / 2) {
                  if (type == 1) {
                     myRobot.homeKarboDepots.add(loc);
                  } else {
                     myRobot.homeFuelDepots.add(loc);
                  }
               } else {
                  if (type == 1) {
                     myRobot.enemyKarboDepots.add(loc);
                  } else {
                     myRobot.enemyFuelDepots.add(loc);
                  }
               }
            }
         } else {
            if (myRobot.castleLoc.y >= myRobot.map.length / 2) {
               if (loc.location.y >= myRobot.map.length / 2) {
                  if (type == 1) {
                     myRobot.homeKarboDepots.add(loc);
                  } else {
                     myRobot.homeFuelDepots.add(loc);
                  }
               } else {
                  if (type == 1) {
                     myRobot.enemyKarboDepots.add(loc);
                  } else {
                     myRobot.enemyFuelDepots.add(loc);
                  }
               }
            } else {
               if (loc.location.y < myRobot.map.length / 2) {
                  if (type == 1) {
                     myRobot.homeKarboDepots.add(loc);
                  } else {
                     myRobot.homeFuelDepots.add(loc);
                  }
               } else {
                  if (type == 1) {
                     myRobot.enemyKarboDepots.add(loc);
                  } else {
                     myRobot.enemyFuelDepots.add(loc);
                  }
               }
            }
         }
      }
   }
   
   public static int getSignalFrom(MyRobot myRobot, int[] unitTypes) {
      for (Robot r : myRobot.visibleRobots) {
         boolean correctType = false;
         
         // check if the robot is of the correct type
         for (int i = 0; i < unitTypes.length; i++) {
            if (r.unit == unitTypes[i]) {
               correctType = true;
               break;
            }
         }
         
         if (correctType && r.team == myRobot.me.team && r.signal >= 0) {
            return r.signal;
         }
      }
      
      return -1;
   }
   
   public static AttackAction attackClosestEnemy(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      Robot closestEnemy = Combat.getClosestEnemy(myRobot);
      if (closestEnemy != null) {
         Tuple enemyLoc = new Tuple(closestEnemy.x, closestEnemy.y);
         
         if (myRobot.me.unit == myRobot.SPECS.CASTLE && 
                  MapFunctions.radiusDistance(myLoc, enemyLoc) > 64) {
            return null;
         }
         
         Tuple toEnemy = myRobot.diff(enemyLoc, myLoc); 
         // after attacking set your movement mode on so you can move a few steps 
         // towards where the enemies are coming from.  This is done to create 
         // space for other ally troops that will be spawned later
         myRobot.closestEnemyLoc = enemyLoc;
         myRobot.enemyDirection = Directions.dirTowards(myLoc, enemyLoc);
         return myRobot.attack(toEnemy.x, toEnemy.y);
      }
      
      return null;
   }
   
   /**
    * Returns the robot at location loc.  Returns null if no robot exists
    */
   public static Robot getUnitAtLoc(MyRobot myRobot, Tuple loc) {
      for (Robot r : myRobot.visibleRobots) {
         // the r.x >=0 is to make sure we can see the unit
         if (r.x >= 0 && loc.equals(new Tuple(r.x, r.y))) {
            return r;
         }
      }
      
      return null;
   }
   
   /**
    * Gives the resource to another robot in the direction of the castle
    */
   public static GiveAction giveResourceTowardsCastle(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      
      // if you have fuel or karbonite
      if (myRobot.me.karbonite > 0 || myRobot.me.fuel > 0) {
         int dirToCastle = Directions.dirTowards(myLoc, myRobot.castleLoc);
         Tuple[] sortedDirs = Directions.orderDirs(dirToCastle);
         for (int i = 0; i < 5; i++) {
            // if a robot exists on the tile, give it to them
            if (myRobot.visibleMap[myLoc.y + sortedDirs[i].y][myLoc.x + sortedDirs[i].x] > 0) {
               return myRobot.give(sortedDirs[i].x, sortedDirs[i].y, 
                        myRobot.me.karbonite, myRobot.me.fuel);
            }
         }
      }
      
      return null;
   }
   
   /**
    * Returns all visible units that are any of the types in the unitType array, and within
    * the specified radius of myRobot
    */
   public static ArrayList<Robot> getRobotsWithinRadius(MyRobot myRobot, 
            int[] unitTypes, int radius) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      ArrayList<Robot> robots = new ArrayList<Robot>();
      
      for (Robot r : myRobot.visibleRobots) {
         boolean correctType = false;
         
         if (!(r.id >= 0)) {
            continue;
         }
         
         // check if the robot is of the correct type
         for (int i = 0; i < unitTypes.length; i++) {
            if (r.unit == unitTypes[i]) {
               correctType = true;
               break;
            }
         }
         
         if (correctType) {
            Tuple rLoc = new Tuple(r.x, r.y);
            
            if (MapFunctions.radiusDistance(rLoc, myLoc) <= radius) {
               robots.add(r);
            }
         }     
      }
      
      return robots;
   }
}