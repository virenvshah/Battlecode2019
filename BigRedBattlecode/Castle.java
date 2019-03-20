package bc19;

import java.util.ArrayList;

public class Castle {
   public static void initialize(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      GeneralUnit.initializeUnit(myRobot);
      myRobot.karbLimit = 0;
      ArrayList<TileNode> miningLocs = new ArrayList<TileNode>();
      miningLocs.addAll(myRobot.homeFuelDepots);
      miningLocs.addAll(myRobot.homeKarboDepots);
      for (int i = 0; i < miningLocs.size(); i++) {
         TileNode tile = miningLocs.get(i);
         miningLocs.remove(tile);
         i--;
         
         if (MapFunctions.radiusDistance(tile.location, myLoc) <= 25) {
            miningLocs.add(tile);
            i++;
            continue;
         }
         
         boolean inCluster = false;
         for (TileNode t : miningLocs) {
            if (MapFunctions.radiusDistance(tile.location, t.location) <= 18) {
               inCluster = true;
               break;
            }
         }
         
         if (!inCluster) {
            miningLocs.add(tile);
            i++;
         } else {
            if (tile.tileType == 1) {
               myRobot.homeKarboDepots.remove(tile);
            } else {
               myRobot.homeFuelDepots.remove(tile);
            }
         }
      }
      
      String msg = "Home fuel locations: ";
      for (TileNode t : myRobot.homeFuelDepots) {
         msg += t.toString();
      }
      myRobot.log(msg);
   }
   
   public static void reactToCastleTalk(MyRobot myRobot) {
      // if the pilgrim Rescue location has already been initialized
      if (myRobot.pilgrimRescueLoc.x >= 0 && myRobot.pilgrimRescueLoc.y >= 0) {
         return;
      }
      
      for (Robot r : myRobot.visibleRobots) {
         if (r.castle_talk > 0 && (r.team == myRobot.me.team)) {
            // castle_talk is an x-coordinate
            if (r.castle_talk < 100) {
               myRobot.log("Got x from " + r.id);
               myRobot.pilgrimRescueLoc.x = r.castle_talk - 1;
            }
            // castle_talk is a y-coordinate
            else {
               myRobot.log("Got y");
               myRobot.pilgrimRescueLoc.y = (r.castle_talk % 100) - 1;
            }
         }
      }
   }
   
   /**
    * Changes the karbLimit so that other castles can also 
    * produce units.  Otherwise the castle that comes first 
    * in the queue order would always end up producing more units
    */
   public static void adjustKarbLimit(MyRobot myRobot, int minimum) {
      myRobot.karbLimit = (myRobot.karbLimit % 31) + minimum;
   }
   
   
   /**
    * Returns a build action if we need to produce units to protect us from 
    * a rush of enemies, otherwise returns null
    */
   public static BuildAction produceEmergencyTroops(MyRobot myRobot, int allyUnit) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);

      // only preacher or crusader or prophet
      Robot enemyRobo = Combat.getClosestEnemy(myRobot);
      Tuple enemyLoc = null;

      if (enemyRobo == null) {
         // no need to produce any troops
         myRobot.allyTroopsToProduce = 0;

         // signal the location of the enemy to all Ally troops
      } else {
         myRobot.log("I am " + myLoc.toString());
         enemyLoc = new Tuple(enemyRobo.x, enemyRobo.y);
         int enemyRobotType;
         if (enemyRobo.unit == myRobot.SPECS.PROPHET || enemyRobo.unit == myRobot.SPECS.PILGRIM) {
            enemyRobotType = 2;
            allyUnit = myRobot.SPECS.CRUSADER;
            // as of now we don't distinguish between preachers and crusaders
         } else {
            enemyRobotType = 1;
         }

         // find the farthest visible Ally, so that the signal strength can 
         // reach the ally
         Robot farthestAlly = Combat.getFarthestCombatAlly(myRobot);
         int farthestDist = 2;
         if (farthestAlly != null) {
            Tuple allyLoc = new Tuple(farthestAlly.x, farthestAlly.y);
            farthestDist = MapFunctions.radiusDistance(myLoc, allyLoc);
            // don't use too much fuel when signaling, if the farthest troop
            // is more than 81 then just reduce the signal to 81
            if (farthestDist > 64) farthestDist = 64;
         }

         // eg. 21223 means that there is an enemy prophet at (12, 23)
         myRobot.signal(enemyRobotType * 10000 + enemyLoc.x * 100 + 
                  enemyLoc.y, farthestDist);
      }


      ArrayList<Robot> visibleEnemies = Combat.getVisibleEnemies(myRobot);
      ArrayList<Robot> visibleAllyTroops = Combat.getVisibleAllyTroops(myRobot);
      // only spawn more emergency troop if there are more visible enemies than ally troops spawned to 
      // counter these enemies. or if there are more visible enemies than
      // allies
      if (visibleEnemies.size() > myRobot.allyTroopsToProduce || 
               visibleEnemies.size() > visibleAllyTroops.size()) {
         // calculate enemy direction
         if (enemyLoc != null) {
            myRobot.enemyDirection = Directions.dirTowards(myLoc, enemyLoc);
            myRobot.closestEnemyLoc = enemyLoc;
         }

         // build a the unit and have it spawn between the castle and the enemy
         BuildAction nobleBot = myRobot.buildRobo(allyUnit, 10 + myRobot.enemyDirection);
         myRobot.allyTroopsToProduce++;
         // no need to check if not null, should be done by parent function
         return nobleBot;


      }

      return null;
   }
   
   public static BuildAction producePilgrim(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      Tuple closestNode = null;
      if (myRobot.karbOverFuel) {
         myRobot.log("Building a karbonite pilgrim");
         myRobot.log("finding closest karbo depot");

         // find the closest Karbonite deposit to send the pilgrim to
         Path closestNodePath = MapFunctions.bfsGetClosestMiningLoc(myRobot, myRobot.homeKarboDepots);
         if (closestNodePath == null) {
            return null;
         }
         
         closestNode = closestNodePath.destination();
      } else {
         myRobot.log("finding closest fuel depot");
         myRobot.log("Building a fuel pilgrim");

         // find the closest Karbonite deposit to send the pilgrim to
         Path closestNodePath = MapFunctions.bfsGetClosestMiningLoc(myRobot, myRobot.homeFuelDepots);
         if (closestNodePath == null) {
            return null;
         }
         
         closestNode = closestNodePath.destination();
      }

      if (closestNode != null ) {
         BuildAction nobleBot = myRobot.buildRobo(myRobot.SPECS.PILGRIM, 
                  Directions.dirTowards(myLoc, closestNode));
         if (nobleBot != null) {
            myRobot.closestNode = myRobot.tileNodeMap[closestNode.y][closestNode.x];
            // so that another pilgrim build later on doesn't visit the same depot
            if (myRobot.karbOverFuel) {
               myRobot.karbPilgrimsProduced++;
               myRobot.karboDepots.remove(myRobot.closestNode);
               myRobot.homeKarboDepots.remove(myRobot.closestNode);
            } else {
               myRobot.fuelPilgrimsProduced++;
               myRobot.fuelDepots.remove(myRobot.closestNode);
               myRobot.homeFuelDepots.remove(myRobot.closestNode);
            }
            // tell the pilgrim the location of the Karbonite deposit

            myRobot.pilgrimsProduced++;
            myRobot.signal(myRobot.closestNode.location.x * 100 + myRobot.closestNode.location.y, 2);
            return nobleBot;
         }
      }

      return null;
   }
   
   public static BuildAction produceProphet(MyRobot myRobot) {
      BuildAction nobleBot = myRobot.buildRobo(myRobot.SPECS.PROPHET, myRobot.enemyDirection);
      if(nobleBot!=null){
         myRobot.prophetsProduced++;
         // keep changing your karbLimit so other castles can also produce prophets
         myRobot.karbLimit = (myRobot.karbLimit % 54) + 90;
         myRobot.signal(1 * 10000 + myRobot.closestEnemyLoc.x * 100 + 
                  myRobot.closestEnemyLoc.y, 10);
         return nobleBot;
      }
      
      return null;
   }
   
   /**
    * Updates the castle with the other castles' locations and also broadcasts
    * this castle's location to the other castles' locations.
    * 
    * Requires: this function must be called throughout the first three turns of 
    * a castle (ie the first three turns of the game)
    */
   public static void findCastleLocations(MyRobot myRobot) {
      // add 1 to everything because the default value of castle_talk is 0.
      // Hence we must send 0 as the x-coordinate we won't be able to tell
      // if there is no castle talk and just the default value is showing up
      // or if we are actually sending a zero
      if (myRobot.turn == 1) {
         myRobot.castleTalk(myRobot.me.x + 1);
      } else if (myRobot.turn == 2) {
         myRobot.castleTalk(100 + myRobot.me.y + 1);
      }
      
      // we can determine which castle this is by its turn queue order on 
      // the first turn.
      // if it sees no other castle-talk, it has to be the first castle
      // if it sees one other castle-talk its the second
      // if it sees 2 other castle-talk its the third castle
      int castleNum = 1;
      for (Robot r : myRobot.visibleRobots) {
         if ((r.team == myRobot.me.team) && (r.id != myRobot.me.id) && r.castle_talk > 0) {
            castleNum++;
            // castle_talk is an x-coordinate
            if (r.castle_talk < 100) {
               myRobot.allyCastleIds.add(r.id);
               myRobot.allyCastleLocs.add(new Tuple(r.castle_talk - 1, -1));
            }
            // castle_talk is a y-coordinate
            else {
               int index = myRobot.allyCastleIds.indexOf(r.id);
               Tuple cLoc = myRobot.allyCastleLocs.get(index);
               cLoc.y = (r.castle_talk - 1) % 100;
               // myRobot.allyCastleLocs.set(index, cLoc);
            }
         }
      }
      
      // if it hasn't been initialized yet (first turn)
      if (myRobot.castleNumber == -1) {
         myRobot.castleNumber = castleNum;
      }
      
      if (myRobot.turn == 3) {
         for (int i = 0; i < myRobot.allyCastleLocs.size(); i++) {
            myRobot.otherEnemyCastleLocs.add(myRobot.getEnemyLoc(myRobot.allyCastleLocs.get(i),
                     myRobot.karboDepots));
         }
         
         for (int i = 0; i < myRobot.homeKarboDepots.size(); i++) {
            TileNode node = myRobot.homeKarboDepots.get(i);
            if (!isClosestCastle(myRobot, node.location)) {
               myRobot.homeKarboDepots.remove(node);
               i--;
            }
         }
         
         for (int i = 0; i < myRobot.homeFuelDepots.size(); i++) {
            TileNode node = myRobot.homeFuelDepots.get(i);
            if (!isClosestCastle(myRobot, node.location)) {
               myRobot.homeFuelDepots.remove(node);
               i--;
            }
         }
      }
   }
   
   public static boolean isClosestCastle(MyRobot myRobot, Tuple tup) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      int dist = MapFunctions.radiusDistance(myLoc, tup);
      
      for (Tuple castleLoc : myRobot.allyCastleLocs) {
         if (MapFunctions.radiusDistance(castleLoc, tup) < dist) {
            return false;
         }
      }
      
      return true;
   }

   public static int getTalkFrom(MyRobot myRobot, int[] unitTypes) {
      for (Robot r : myRobot.visibleRobots) {
         boolean correctType = false;

         // check if the robot is of the correct type
         for (int i = 0; i < unitTypes.length; i++) {
            if (r.unit == unitTypes[i]) {
               correctType = true;
               break;
            }
         }

         if (correctType && r.team == myRobot.me.team && r.castle_talk >= 0) {
            return r.castle_talk;
         }
      }

      return -1;
   }
}