package bc19;

import java.util.ArrayList;

public class VirenTwo {
   public static Action run(MyRobot myRobot) {
      if (myRobot.me.unit == myRobot.SPECS.CASTLE) {
         return castle(myRobot);
      } else if (myRobot.me.unit == myRobot.SPECS.PILGRIM) {
         return pilgrim(myRobot);
      } else if (myRobot.me.unit == myRobot.SPECS.PROPHET) {
         return prophet(myRobot); 
      } else if (myRobot.me.unit == myRobot.SPECS.PREACHER) {
         return preacher(myRobot); 
      } else if (myRobot.me.unit == myRobot.SPECS.CRUSADER) {
         return crusader(myRobot);
      } else if (myRobot.me.unit == myRobot.SPECS.CHURCH) {
         return church(myRobot);
      } else {
         return null;
      }
   }
   
   public static Action castle(MyRobot myRobot) {
      myRobot.log("Turn " + myRobot.turn);
      if (myRobot.turn == 1) {
         Castle.initialize(myRobot);
         myRobot.baseKarb = 0;
      }
      
      if (myRobot.turn <= 3) {
         Castle.findCastleLocations(myRobot);
      }  
      
      if (myRobot.turn % 10 == 0) {
         Castle.adjustKarbLimit(myRobot, myRobot.baseKarb);
      }
      
      if (myRobot.fuel > 1000) {
         myRobot.fuelLimit = 1150;
      }
      
      if (myRobot.turn >= 4 && myRobot.turn < 75) {
         Castle.reactToCastleTalk(myRobot);
      }
      
      if (myRobot.karbonite >= 15) {
         BuildAction troop = Castle.produceEmergencyTroops(myRobot, myRobot.SPECS.PREACHER);
         if (troop != null) {
            return troop;
         }
      }
      
      AttackAction attack = GeneralUnit.attackClosestEnemy(myRobot);
      if (attack != null) {
         return attack;
      }
      
      if (myRobot.karbonite >= 15 && myRobot.pilgrimRescueLoc.x >= 0 && myRobot.pilgrimRescueLoc.y >= 0
               && Castle.isClosestCastle(myRobot, myRobot.pilgrimRescueLoc) && 
               !myRobot.alreadyRescued.contains(myRobot.pilgrimRescueLoc.x * 100 + myRobot.pilgrimRescueLoc.y)) {
         Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);

         myRobot.signal(3 * 10000 + myRobot.pilgrimRescueLoc.x * 100 + 
                  myRobot.pilgrimRescueLoc.y, 2);
         BuildAction nobleBot = myRobot.buildRobo(myRobot.SPECS.CRUSADER, 10 + 
                  Directions.dirTowards(myLoc, myRobot.pilgrimRescueLoc));
         if (nobleBot != null) {
            myRobot.rescueTroops++;
            if (myRobot.rescueTroops >= 3) {
               myRobot.alreadyRescued.add(100 * myRobot.pilgrimRescueLoc.x + myRobot.pilgrimRescueLoc.y);
               myRobot.pilgrimRescueLoc.x = -1;
               myRobot.pilgrimRescueLoc.y = -1;
               myRobot.rescueTroops = 0;
            }
            // no need to check if not null, should be done by parent function
            return nobleBot;
         }
      }

      // only produce if you have more Karbonite than karbLimit
      if (myRobot.karbonite > myRobot.karbLimit) {
         BuildAction unit = null;

         if (myRobot.homeFuelDepots.size() > 0 && myRobot.unitsProduced >= 1 &&
                  // changed from myRobot.pilgrimsProduced because once we finish 
                  // producing fuelPilgrims we don't produce any more karbonite pilgrims
                  (myRobot.turn % 2 == 0 || myRobot.homeKarboDepots.size() == 0)){
            myRobot.karbOverFuel = false;            
            unit = Castle.producePilgrim(myRobot);
         }
         if (unit == null && myRobot.homeKarboDepots.size() > 0) {
            myRobot.karbOverFuel = true;
            unit = Castle.producePilgrim(myRobot);
         }

         if (unit == null) {
            unit = myRobot.buildRobo(myRobot.SPECS.PROPHET, myRobot.enemyDirection);
            if (myRobot.otherEnemyCastleLocs.size() > 0) {
               Tuple enemyLoc = myRobot.otherEnemyCastleLocs.get(0);
               int signalNum = enemyLoc.y | (enemyLoc.x << 6) | ((myRobot.turn % 10) << 12);
               myRobot.log("Signal is " + signalNum);
               myRobot.signal(signalNum, 2);
            } else {
               int signalNum = (myRobot.turn % 10) << 12;
               myRobot.signal(signalNum, 2);
            }
         }
   
         if (unit != null) {
            myRobot.unitsProduced++;
            Castle.adjustKarbLimit(myRobot, myRobot.baseKarb);
            if (myRobot.unitsProduced == 8) {
               myRobot.baseKarb = 75;
               Castle.adjustKarbLimit(myRobot, myRobot.baseKarb);
            }
            if (myRobot.homeFuelDepots.size() == 0 && myRobot.homeKarboDepots.size() == 0) {
               myRobot.baseKarb = 100;
               Castle.adjustKarbLimit(myRobot, myRobot.baseKarb);
            }
            return unit;
         }
      }
      
      return null;
   }
   
   
   public static Action church(MyRobot myRobot) {
      if (myRobot.turn == 1) {
         Church.initialize(myRobot);
         myRobot.fuelLimit = 1300;
      }
      
      // no need to rush preachers if you're not close to the enemy mine
      if (myRobot.turn % 10 == 0 && (myRobot.unitsProduced >= 7 || !myRobot.closeToEnemyMine)) {
         Castle.adjustKarbLimit(myRobot, 120);
      }
      
      if (myRobot.karbonite >= 15) {
         BuildAction troop = Castle.produceEmergencyTroops(myRobot, myRobot.SPECS.PREACHER);
         if (troop != null) {
            return troop;
         }
      }
      
      BuildAction unit = null;
      if (myRobot.fuelDepots.size() > 0 &&
               // changed from myRobot.pilgrimsProduced because once we finish 
               // producing fuelPilgrims we don't produce any more karbonite pilgrims
               (myRobot.turn % 2 == 0 || myRobot.karboDepots.size() == 0)) {
         myRobot.karbOverFuel = false;            
         unit = Church.producePilgrim(myRobot);
      }
      if (unit == null && myRobot.karboDepots.size() > 0) {
         myRobot.karbOverFuel = true;
         unit = Church.producePilgrim(myRobot);
      }
      
      if (unit != null) {
         return unit;
      }
      
      // only produce if you have more Karbonite than karbLimit
      if (myRobot.karbonite > myRobot.karbLimit) {
         int unitType;
         if (myRobot.unitsProduced < 3 && myRobot.closeToEnemyMine) {
            unitType = myRobot.SPECS.PREACHER;
            myRobot.signal(5 * 10000 + myRobot.castleLoc.x * 100 + 
                     myRobot.castleLoc.y, 2);
         } else {
            unitType = myRobot.SPECS.PROPHET;
            int signalNum = myRobot.castleLoc.y | (myRobot.castleLoc.x << 6) | ((myRobot.turn % 10) << 12);
            myRobot.log("Church signal is " + signalNum);
            myRobot.signal(signalNum, 2);
         }
         
         unit = myRobot.buildRobo(unitType, 
                  myRobot.enemyDirection);
         
         if (unit != null) {
            myRobot.log("Churched produced a unit");
            myRobot.unitsProduced++;
            if (myRobot.unitsProduced >= 7) {
               Castle.adjustKarbLimit(myRobot, 120);
            }
            return unit;
         }
      }
      
      return null;
   }
   
   public static Action prophet(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      myRobot.speed = 1;
      
      if (myRobot.turn == 1) {
         myRobot.log("I am a prophet");
         Prophet.initialize(myRobot);
         Prophet.reactSignalWallInitialize(myRobot);
         myRobot.fuelLimit = 950;
      }

      // move once every 10 turns starting from 1st turn, if you have ally troops with you
      // also move if you are very close to your castle
      // also move if you are on a resource tile
      int[] types = {myRobot.SPECS.PROPHET, myRobot.SPECS.CRUSADER, myRobot.SPECS.PREACHER};
      ArrayList<Robot> allyTroops = GeneralUnit.getRobotsWithinRadius(myRobot, types, 16);
      if ((((myRobot.turn % 20 == 1 && allyTroops.size() >= 6 ) || Prophet.catchUp(myRobot)) && myRobot.fuel > myRobot.fuelLimit) ||
               MapFunctions.radiusDistance(myLoc, myRobot.home) <= 9 ||               
               myRobot.karboniteMap[myLoc.y][myLoc.x] || myRobot.fuelMap[myLoc.y][myLoc.x]) {
            myRobot.moveMode = true;
      } else {
         myRobot.moveMode = false;
      }
      
      // first thing to do is check if you can attack
      Action action = Prophet.kite(myRobot);
      if (action != null) {
         return action;
      }
      
      if (myRobot.moveMode) {
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, myRobot.speed, 0);
         if (tup != null) {
            myRobot.moveMode = false;
            return myRobot.move(tup.x, tup.y);
         } else {
            // if the castle is visible, check if it is destroyed
            if (MapFunctions.radiusDistance(myLoc, myRobot.enemyCastleLoc) <= 64) {
               // if it is destroyed, change enemyCastleLoc to the next enemy castle
               // location
               Robot r = GeneralUnit.getUnitAtLoc(myRobot, myRobot.enemyCastleLoc);
               // castle destroyed
               if (r == null || r.unit != myRobot.SPECS.CASTLE) {
                  myRobot.castleTalk(myRobot.enemyCastleLoc.x + myRobot.enemyCastleLoc.y);
                  if (myRobot.otherEnemyCastleLocs.size() > 0) {
                     myRobot.enemyCastleLoc = myRobot.otherEnemyCastleLocs.get(0);
                     // for some reason remove(index) doesn't seem to transpile to js
                     myRobot.otherEnemyCastleLocs.remove(myRobot.enemyCastleLoc);
                  }
               }
            }
         }
      }
      
      // if nothing else see if you have Karbonite / fuel to give to castle
      GiveAction giveResource = GeneralUnit.giveResourceTowardsCastle(myRobot);
      if (giveResource != null) {
         return giveResource;
      }
      
      return null;
   }
   
   public static Action preacher(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      myRobot.speed = 1;
      
      if (myRobot.turn == 1) {
         Prophet.initialize(myRobot);
         myRobot.fuelLimit = 750;
         myRobot.log("I am a preacher");
      }

      Preacher.reactToCastleSignal(myRobot);
      // move once every 10 turns starting from 1st turn, if you have ally troops with you
      // also move if you are very close to your castle
      // also move if you are on a resource tile
      int[] types = {myRobot.SPECS.PREACHER};
      ArrayList<Robot> allyTroops = GeneralUnit.getRobotsWithinRadius(myRobot, types, 25);
      
      if ((allyTroops.size() >= 3 || myRobot.rushMode) && !myRobot.alert) {
         if (myRobot.nextEnemyLoc == null && myRobot.enemyFuelDepots.size() > 0) {
            myRobot.nextEnemyLoc = MapFunctions.bfsGetClosestMiningLoc(
                     myRobot, myRobot.enemyFuelDepots).destination();
            // don't go near enemy castle
            if (MapFunctions.radiusDistance(myRobot.nextEnemyLoc, myRobot.enemyCastleLoc) < 100) {
               myRobot.enemyFuelDepots.remove(
                        myRobot.tileNodeMap[myRobot.nextEnemyLoc.y][myRobot.nextEnemyLoc.x]);
               myRobot.nextEnemyLoc = null;
            }
         }

         myRobot.rushMode = true;
      }
      
      if (MapFunctions.radiusDistance(myLoc, myRobot.home) <= 2 ||               
               myRobot.karboniteMap[myLoc.y][myLoc.x] || myRobot.fuelMap[myLoc.y][myLoc.x]) {
         // need to have enough fuel
         myRobot.moveMode = true;
      } else {
         myRobot.moveMode = false;
      }
      
      // first thing to do is check if you can attack
      // myRobot.log("About to give");
      Action action = GeneralUnit.attackClosestEnemy(myRobot);
      if (action != null) {
         return action;
      }
      
      if (myRobot.rushMode && myRobot.nextEnemyLoc != null) {
         myRobot.log("Entered rushmode");
         myRobot.log(myRobot.nextEnemyLoc.toString());
         myRobot.speed = 2;
         if (MapFunctions.radiusDistance(myLoc, myRobot.nextEnemyLoc) <= 16) {
            myRobot.log("Enemy location " + myRobot.nextEnemyLoc.toString() + " distance " +
                     MapFunctions.radiusDistance(myLoc, myRobot.nextEnemyLoc));
            myRobot.enemyFuelDepots.remove(
                     myRobot.tileNodeMap[myRobot.nextEnemyLoc.y][myRobot.nextEnemyLoc.x]);
            myRobot.nextEnemyLoc = null;
            return null;
            // if the enemy mining location is visible then its clear because you didn't 
            // attack anyone
         } else {
            Tuple tup = MapFunctions.moveTo(myRobot, myRobot.nextEnemyLoc, myRobot.speed, 0);
            if (tup != null) {
               
               return myRobot.move(tup.x, tup.y);
            } else {
               myRobot.enemyFuelDepots.remove(
                        myRobot.tileNodeMap[myRobot.nextEnemyLoc.y][myRobot.nextEnemyLoc.x]);
               myRobot.nextEnemyLoc = null;
            }
         }        
      }
      
      if (myRobot.moveModeProphet) {
         Tuple closestEnemyMove = MapFunctions.moveTo(myRobot, myRobot.closestEnemyLoc, 2, 0);
         if (closestEnemyMove != null) {
            return myRobot.move(closestEnemyMove.x, closestEnemyMove.y);
         } else {
            myRobot.log("PREACHER Cannot move towards enemy PROPHET anymore, set move to false");
            myRobot.moveModeProphet = false;
         }
      }
      
      // if there are enemies in the area, avoid moving too close to them
      // because you want to get the first shot
      if (myRobot.moveMode && myRobot.alert) {
         Path pathToEnemy = MapFunctions.shortestPath(myRobot, myRobot.closestEnemyLoc, 0);
         if (pathToEnemy.radialMovementDist < 6) {
            myRobot.log("PREACHER Too close to enemy, stop moving!");
            myRobot.moveMode = false;
            myRobot.moveModeTimes = 0;
         // move diagonally so you still remain out of shot
         } else if (pathToEnemy.radialMovementDist == 7) {
            int closestEnemyDir = Directions.dirTowards(myLoc, myRobot.closestEnemyLoc);
            Tuple randMove = myRobot.randomMovePreacher(myLoc, 
                     Directions.getPreviousDir(closestEnemyDir));
            if (randMove != null) {
               return myRobot.move(randMove.x, randMove.y);
            }
         }
      }
      
      // if you're set to moveMode, move randomly towards the enemy, in the hope
      // of creating space between you and other allies so that you don't get
      // AOEed by enemy preachers, also stay close to home castle
      if (myRobot.moveMode && MapFunctions.radiusDistance(myLoc, myRobot.castleLoc) < 25) {
         myRobot.log("Trying to move to " + myRobot.closestEnemyLoc.toString());
         Tuple closestEnemyMove = MapFunctions.moveTo(myRobot, myRobot.closestEnemyLoc, 1, 0);
         if (closestEnemyMove != null) {
            int closestEnemyDir = Directions.tupleToDir(closestEnemyMove);
            Tuple randMove = myRobot.randomMovePreacher(myLoc, closestEnemyDir);
            if (randMove != null) {
               // number of times you've moved
               myRobot.moveModeTimes++;
               if (myRobot.moveModeTimes > 4) {
                  myRobot.moveMode = false;
                  myRobot.moveModeTimes = 0;
               }
               myRobot.moveModeTimes++;
               myRobot.log("PREACHER Randomly moving towards " + new Tuple(closestEnemyMove.x + myLoc.x, closestEnemyMove.y + myLoc.y).toString());
               return myRobot.move(randMove.x, randMove.y);
            }
         } else {
            myRobot.log("PREACHER Cannot move towards enemy anymore, set move to false");
            myRobot.moveMode = false;
            myRobot.moveModeTimes = 0;
         }
      }

      // if nothing else see if you have Karbonite / fuel to give to castle
      GiveAction giveResource = GeneralUnit.giveResourceTowardsCastle(myRobot);
      if (giveResource != null) {
         return giveResource;
      }
      
      return null;
   }

   
   public static Action crusader(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      
      if (myRobot.turn == 1) {
         Crusader.initialize(myRobot);
      }
      
      Crusader.reactToCastleChurchSignal(myRobot);
      
      if (MapFunctions.radiusDistance(myLoc, myRobot.home) <= 2 ||               
               myRobot.karboniteMap[myLoc.y][myLoc.x] || myRobot.fuelMap[myLoc.y][myLoc.x]) {
         // need to have enough fuel
         myRobot.moveMode = true;
      } else {
         myRobot.moveMode = false;
      }
      
      int[] types = {myRobot.SPECS.CRUSADER};
      ArrayList<Robot> allyTroops = GeneralUnit.getRobotsWithinRadius(myRobot, types, 36);
      
      if (allyTroops.size() >= 3) {
         myRobot.attackMode = true;
      }
      
      Action action = Crusader.attackClosestEnemy(myRobot);
      if (action != null) {
         return action;
      }
      
      if (myRobot.attackMode) {
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.closestEnemyLoc, 3, 0); 
         if (tup != null) {
            return myRobot.move(tup.x, tup.y);
         } else {
            myRobot.attackMode = false;
         }
      }
      
      if (myRobot.moveMode) {
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.closestEnemyLoc, 1, 0);
         if (tup != null) {
            myRobot.moveMode = false;
            return myRobot.move(tup.x, tup.y);
         }
      }
       
      return null;
   }
   
   
   public static Action pilgrim(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);

      if (myRobot.turn == 1) {
         Pilgrim.initialize(myRobot);
      }
      
      if (myRobot.pilgrimSentEnemyX) {
         myRobot.log("I am a PILGRIM and I'm sending y");
         myRobot.castleTalk(100 + myRobot.closestNode.location.y + 1);
         myRobot.pilgrimSentEnemyX = false;
         myRobot.pilgrimSentEnemyY = true;
      }

      // detect enemies
      Robot enemyRob = Combat.getClosestCombatEnemy(myRobot);
      if (enemyRob != null) {
         Tuple enemyLoc = new Tuple(enemyRob.x, enemyRob.y);
         if (myRobot.pilgrimSentEnemyY) {
            myRobot.pilgrimSentEnemyY = false;
         } else if (!myRobot.pilgrimSentEnemyX) {
            myRobot.log("PILGRIM Sending x");
            myRobot.castleTalk(myRobot.closestNode.location.x + 1);
            myRobot.pilgrimSentEnemyX = true;
         }
         // if enemy is within 2 * its attack radius, then start running away
         if (MapFunctions.radiusDistance(enemyLoc, myLoc) <= 
                  1.3 * myRobot.SPECS.UNITS[enemyRob.unit].VISION_RADIUS + 16.8) {
            myRobot.log("PILGRIM " + (1.3 * myRobot.SPECS.UNITS[enemyRob.unit].VISION_RADIUS + 16.8));
            Tuple randMove = myRobot.randomMove(myLoc, 10 + Directions.dirTowards(enemyLoc, myLoc));
            if (randMove != null) {
               myRobot.castleTalk(enemyLoc.x);
               myRobot.castleNum = enemyLoc.y;
               return myRobot.move(randMove.x, randMove.y);
            }
         }
      }
      
      // look for a structure close to the mining location if there exists one
      if (myRobot.closestNode.location.equals(myLoc) && !myRobot.closeToBase) {
         int[] types = {myRobot.SPECS.CASTLE, myRobot.SPECS.CHURCH};
         ArrayList<Robot> temp = GeneralUnit.
                  getRobotsWithinRadius(myRobot, types, 25);
         ArrayList<Robot> structures = new ArrayList<Robot>();
         for (int i = 0; i < temp.size(); i++) {
            Robot r = temp.get(i);
            if (r.team == myRobot.me.team) {
               structures.add(r);
            }
         }
         if (structures.size() > 0) {
            Robot homeStruct = structures.get(0);
            myRobot.home = new Tuple(homeStruct.x, homeStruct.y);
            myRobot.closeToBase = true;
         }
      }
      
      if (myRobot.me.karbonite >= myRobot.karbCapacity || myRobot.me.fuel >= 100) {
         // if adjacent structure exists, give it Karbonite or Fuel when you fill up
         Robot closestStruct = Pilgrim.getAdjacentStructure(myRobot);
         if (closestStruct != null) {
            return myRobot.give(closestStruct.x - myLoc.x, closestStruct.y - myLoc.y,
                     myRobot.me.karbonite, myRobot.me.fuel);
         }
      }

      // if you are on your mining location,
      // have enough karbonite and you're far from your base, build a church
      if (myRobot.closestNode.location.equals(myLoc) && myRobot.karbonite >= 50
               && !myRobot.closeToBase) {
         // build a nearby church if you don't see one already
         // try building the church on a non-resource tile
         int direction = -1;
         for (Tuple t : Directions.dirs) {
            if (myLoc.y + t.y >= myRobot.map.length || myLoc.y + t.y < 0 ||
                     myLoc.x + t.x >= myRobot.map.length || myLoc.x + t.x < 0)
               continue;
            
            if (!myRobot.karboniteMap[myLoc.y + t.y][myLoc.x + t.x] && 
                     !myRobot.fuelMap[myLoc.y + t.y][myLoc.x + t.x]) {
               direction = Directions.tupleToDir(t);
            }
         }
         BuildAction church = myRobot.buildRobo(myRobot.SPECS.CHURCH, direction);
         if(church != null) {
            myRobot.log("Building a church");
            Path closestEnemyMinePath = MapFunctions.bfsGetClosestMiningLoc(
                     myRobot, myRobot.enemyFuelDepots);
            if (closestEnemyMinePath.radialMovementDist <= 15) {
               // don't produce the 3 preachers if already in enemy territory
               if (myRobot.homeFuelDepots.contains(myRobot.closestNode) ||
                        myRobot.homeKarboDepots.contains(myRobot.closestNode)) {
                  myRobot.signal(2 * 10000 + 100 * myRobot.castleLoc.x + myRobot.castleLoc.y, 2);
               } else {
                  myRobot.signal(1 * 10000 + 100 * myRobot.castleLoc.x + myRobot.castleLoc.y, 2);
               }
               myRobot.closeToBase = true;
               return church;
            } else {
               Robot enemy = Combat.getClosestEnemy(myRobot);
               myRobot.log("I am a pilgrim and I am here");
               if (myRobot.karbonite >= 80 || enemy != null) {
                  myRobot.signal(1 * 10000 + 100 * myRobot.castleLoc.x + myRobot.castleLoc.y, 2);
                  myRobot.closeToBase = true;
                  return church;
               }
            }
         }
      }
      
      // if you have not filled up on karbonite or Fuel go to the closest mining location
      if (!myRobot.closestNode.location.equals(myLoc) && 
               (myRobot.me.karbonite < myRobot.karbCapacity && myRobot.me.fuel < 100)) {
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.closestNode.location, 2, 0);
         if (tup != null) {
            return myRobot.move(tup.x, tup.y);
         } else {
            // If there is a pilgrim or a church on your mining location, reassign your mining location
            Robot obstruction = GeneralUnit.getUnitAtLoc(myRobot, myRobot.closestNode.location);
            if (obstruction != null && 
                     (obstruction.unit == myRobot.SPECS.CHURCH || obstruction.unit == myRobot.SPECS.PILGRIM)) {
               int type = myRobot.karboPilgrim ? 1 : 2;
               Pilgrim.reassignMiningLoc(myRobot, type);
            }      
         }
      }
      
      // if capacity is filled up and no structure is visible, go back home to deposit karbonite
      if (!myRobot.home.equals(myLoc) && (myRobot.me.karbonite >= myRobot.karbCapacity
               || myRobot.me.fuel >= 100)) {
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.home, 2,0);
         if (tup != null) {
            return myRobot.move(tup.x, tup.y);
         } 
         else {
            // if cannot go home (probably because of a unit obstruction,  
            // see if you have Karbonite / fuel to give to castle, and you can give 
            // the Karb / fuel to the unit who will pass it on to the castle
            GiveAction giveResource = GeneralUnit.giveResourceTowardsCastle(myRobot);
            if (giveResource != null) {
               return giveResource;
            }
         }
      }
      
      // if on mining location
      if (myRobot.closestNode.location.equals(myLoc) &&
               (myRobot.me.karbonite < myRobot.karbCapacity && myRobot.me.fuel < 100)) {
         return myRobot.mine(); // if we can't give, mine.
      }
      
      return null;
   }
}