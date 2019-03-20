package bc19;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Counter {
    /**
     * Master function for the Rush strategy
     */
    public static Action rush(MyRobot myRobot) {
        if (myRobot.me.unit == myRobot.SPECS.CASTLE) {
            return castle(myRobot);
        } else if (myRobot.me.unit == myRobot.SPECS.PROPHET) {
            return prophet(myRobot);
        } else if (myRobot.me.unit == myRobot.SPECS.PREACHER) {
            return preacher(myRobot);
        } else if (myRobot.me.unit == myRobot.SPECS.PILGRIM) {
            return pilgrim(myRobot);
        } else if (myRobot.me.unit == myRobot.SPECS.CHURCH){
            return church(myRobot);
        } else {
            return null;
        }
    }

    /**
     * Castle strategy
     */
    private static Action castle(MyRobot myRobot){
       if (myRobot.turn <= 4 && (myRobot.karbonite >= 100 || myRobot.pilgrimsProduced > 0)) {
          myRobot.pilgrimsProduced++;
          BuildAction nobleBot = myRobot.buildRobo(myRobot.SPECS.PROPHET, -1);
          if(nobleBot!=null){
              return nobleBot;
          }
       }
     
       return null;
    }

    /**
     * Church strategy
     */
    private static Action church(MyRobot myRobot){
       if (myRobot.turn==1){
          myRobot.log("I am a church");

       }

       // Robot[] robots = getVisibleRobots();


       if (myRobot.karbonite >= 30) {
          Object nobleBot = myRobot.buildRobo(myRobot.SPECS.PREACHER, -1);
          if(nobleBot!=null){return (BuildAction)nobleBot;}
       }
       return null;
    }

    /**
     * Prophet strategy
     */
    private static Action prophet(MyRobot myRobot){
       Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
       if (myRobot.turn == 1) {
            myRobot.log("I am a prophet");
            for(Robot r : myRobot.visibleRobots) {
                if(r.unit == myRobot.SPECS.CASTLE && r.team == myRobot.me.team && r.x > 0) {
                    myRobot.castleLoc = new Tuple(r.x, r.y);
                    myRobot.karboDepots = myRobot.getTypedTiles(myRobot.tileNodeMap, 1);
                    myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc, myRobot.karboDepots);
                 //   myRobot.otherCastleLoc = new Tuple(r.signal / 100, r.signal % 100);
                 //   myRobot.nextEnemyLoc = myRobot.getEnemyLoc(myRobot.otherCastleLoc, myRobot.karboDepots);
                }
            }
        }

        // See if there are enemies that can be attacked
       
       Robot closestEnemy = null;
       
       // Iterate through the visible robots, and all visible enemies 
       // to the visibleEnemies ArrayList
       ArrayList<Robot> visibleEnemies = Combat.getVisibleEnemies(myRobot);

       // Find the closest enemy from the visibleEnemy arrayList
       if (visibleEnemies.size() > 0) {
          int minDist = Integer.MAX_VALUE;
          for (Robot r : visibleEnemies) {
             int dist = MapFunctions.
                      radiusDistance(new Tuple(myRobot.me.x, myRobot.me.y), 
                                       new Tuple(r.x, r.y));
             
             // prophets can't attack enemies within 16 units distance
             if (dist < minDist) {
                minDist = dist;
                closestEnemy = r;
             }
          }
       }
     //   Robot closestEnemy = Combat.getClosestEnemy(myRobot);
        if (closestEnemy != null && MapFunctions.radiusDistance(myLoc, new Tuple(closestEnemy.x, closestEnemy.y)) > 16) {
           Tuple enemyLoc = new Tuple(closestEnemy.x, closestEnemy.y);
           Tuple toEnemy = myRobot.diff(enemyLoc, myLoc);
           // after attacking set your movement mode on so you can move a few steps 
           // towards where the enemies are coming from.  This is done to create 
           // space for other ally troops that will be spawned later
           myRobot.moveMode = true;
           myRobot.moveModeProphet = false;
           myRobot.closestEnemyLoc = enemyLoc;
           myRobot.enemyDirection = Directions.dirTowards(myLoc, enemyLoc);
           return myRobot.attack(toEnemy.x, toEnemy.y);
        } else if (closestEnemy != null && MapFunctions.radiusDistance(myLoc, new Tuple(closestEnemy.x, closestEnemy.y)) <= 16) {
           myRobot.log("Trying to move");
           Tuple tup = myRobot.randomMove(myLoc, 10 + Directions.dirTowards(new Tuple(closestEnemy.x, closestEnemy.y), myLoc));
           if (tup != null) {
              myRobot.log("Successfully moved");
              return myRobot.move(tup.x, tup.y);
           }
        }
        
        Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 2, 0);
        
        if (tup != null) {
           return myRobot.move(tup.x, tup.y);
        }

        return null;

    }

    /**
     * Preacher strategy
     */
    private static Action preacher(MyRobot myRobot) {
       Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
       if (myRobot.turn == 1) {
            myRobot.log("I am a preacher");
            for(Robot r : myRobot.visibleRobots) {
                if(r.unit == myRobot.SPECS.CASTLE && r.team == myRobot.me.team && r.x > 0) {
                    myRobot.castleLoc = new Tuple(r.x, r.y);
                    myRobot.karboDepots = myRobot.getTypedTiles(myRobot.tileNodeMap, 1);
                    myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc, myRobot.karboDepots);
                 //   myRobot.otherCastleLoc = new Tuple(r.signal / 100, r.signal % 100);
                 //   myRobot.nextEnemyLoc = myRobot.getEnemyLoc(myRobot.otherCastleLoc, myRobot.karboDepots);
                }
            }
        }

        // See if there are enemies that can be attacked
        Robot closestEnemy = Combat.getClosestEnemy(myRobot);
        if (closestEnemy != null) {
           Tuple enemyLoc = new Tuple(closestEnemy.x, closestEnemy.y);
           Tuple toEnemy = myRobot.diff(enemyLoc, myLoc);
           // after attacking set your movement mode on so you can move a few steps 
           // towards where the enemies are coming from.  This is done to create 
           // space for other ally troops that will be spawned later
           myRobot.moveMode = true;
           myRobot.moveModeProphet = false;
           myRobot.closestEnemyLoc = enemyLoc;
           myRobot.enemyDirection = Directions.dirTowards(myLoc, enemyLoc);
           return myRobot.attack(toEnemy.x, toEnemy.y);
        }
        
        Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 2, 0);
        
        if (tup != null) {
           return myRobot.move(tup.x, tup.y);
        }

        return null;
    }

    /**
     * Pilgrim strategy
     */
    private static Action pilgrim(MyRobot myRobot){
        // first initialize your closest mining location, and your home base (castle)
        Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
        if (myRobot.turn == 1) {
            for (Robot r : myRobot.visibleRobots) {
                Tuple rLoc = new Tuple(r.x, r.y);
                // First find the castle that has spawned you.  The castle will be
                // adjacent to you, will be visible, and will be sending out a signal
                if (r.unit == myRobot.SPECS.CASTLE && r.x >= 0 && r.signal >= 0 &&
                        MapFunctions.radiusDistance(myLoc, rLoc) <= 2) {
                    myRobot.castleLoc = myRobot.home = rLoc; // set your home base to be the castleLoc

                    // the closest mining location
                    myRobot.closestNode = new TileNode();
                    myRobot.closestNode.tileType = 1;

                    // The castle's signal is a 4 digit number where the first 2 digits
                    // are the x-coordinate, and the last 2 are the y-coordinate
                    myRobot.closestNode.location = new Tuple(r.signal / 100, r.signal % 100);
                    myRobot.log("Closest mining location is " + myRobot.closestNode.location.toString());
                }
            }
        }

        // set the mineRole based on what the closest node is (by default, it's set to 0)
        // 0 - karbonite
        // 1 - fuel
        if(myRobot.tileNodeMap[myRobot.closestNode.location.y][myRobot.closestNode.location.x].tileType==2){
            myRobot.mineRole=1;
        } else {
            myRobot.mineRole=0;
        }

        // detect enemies and run away
        Robot enemyRob = Combat.getClosestCombatEnemy(myRobot);
        if (enemyRob != null) {
            Tuple enemyLoc = new Tuple(enemyRob.x, enemyRob.y);
            // if enemy is within 2 * its attack radius, then start running away
            if (MapFunctions.radiusDistance(enemyLoc, myLoc) <
                    2 * myRobot.SPECS.UNITS[enemyRob.unit].VISION_RADIUS) {
                Tuple randMove = myRobot.randomMove(myLoc, Directions.dirTowards(enemyLoc, myLoc));
                if (randMove != null) {
                    return myRobot.move(randMove.x, randMove.y);
                }
            }
        }

        // if you're not filled in karbonite and fuel and you're not at the closestNode, go to the closestNode
        if (!myRobot.closestNode.location.equals(myLoc) &&
                myRobot.me.karbonite < myRobot.SPECS.UNITS[myRobot.SPECS.PILGRIM].KARBONITE_CAPACITY &&
                myRobot.me.fuel < myRobot.SPECS.UNITS[myRobot.SPECS.PILGRIM].FUEL_CAPACITY) {
            Tuple tup = MapFunctions.moveTo(myRobot, myRobot.closestNode.location, 2,0);
            if (tup != null) {
                return myRobot.move(tup.x, tup.y);
            }
        }

        // if capacity is filled up, go back home to deposit karbonite
        if (myRobot.me.fuel >= myRobot.SPECS.UNITS[myRobot.SPECS.PILGRIM].FUEL_CAPACITY ||
                myRobot.me.karbonite >= myRobot.SPECS.UNITS[myRobot.SPECS.PILGRIM].KARBONITE_CAPACITY) {
            Tuple tup = MapFunctions.moveTo(myRobot, myRobot.home, 1,0);
            if (MapFunctions.radiusDistance(myLoc,myRobot.home)<=2){ // by defn, this will return at some point
                ArrayList<Robot> visibleAllyTroops = Combat.getVisibleAllyTroops(myRobot);
                // try giving the Karbonite to a nearby castle first
                for(Robot r: visibleAllyTroops){
                    Tuple rLoc = new Tuple(r.x,r.y);
                    if(MapFunctions.radiusDistance(rLoc,myLoc)<=2 && r.unit==myRobot.SPECS.CASTLE){
                        Tuple toCastle = myRobot.diff(rLoc,myLoc);
                        return myRobot.give(toCastle.x,toCastle.y,myRobot.me.karbonite,myRobot.me.fuel);
                    }
                }
            }
            else if (tup != null) {
                return myRobot.move(tup.x, tup.y);
            } else  {
                ArrayList<Robot> visibleAllyTroops = Combat.getVisibleAllyTroops(myRobot);
                // try giving the Karbonite to a nearby castle first
                for(Robot r: visibleAllyTroops){
                    Tuple rLoc = new Tuple(r.x,r.y);
                    if(MapFunctions.radiusDistance(rLoc,myLoc)<=2 && r.unit==myRobot.SPECS.CASTLE){
                        Tuple toCastle = myRobot.diff(rLoc,myLoc);
                        return myRobot.give(toCastle.x,toCastle.y,myRobot.me.karbonite,myRobot.me.fuel);
                    }
                }
                // if impossible, try giving the Karbonite to an adjacent ally, and hope they pass it on
                // to someone close by

                for (Robot r : visibleAllyTroops) {
                    Tuple rLoc = new Tuple(r.x,r.y);
                    if(MapFunctions.radiusDistance(rLoc, myLoc) <= 2) {
                        Tuple toAlly = myRobot.diff(rLoc, myLoc);
                        return myRobot.give(toAlly.x, toAlly.y, myRobot.me.karbonite, myRobot.me.fuel);
                    }
                }
            }
        }

        if(myRobot.me.karbonite>=myRobot.SPECS.UNITS[myRobot.SPECS.PILGRIM].KARBONITE_CAPACITY){
            myRobot.log("CURRENT LOC: "+myLoc.toString());
            myRobot.log("DEPOT: "+myRobot.closestNode.toString());
            myRobot.log("HOME: "+myRobot.home.toString());
        }

        // if on mining location
        if (myRobot.closestNode.location.equals(new Tuple(myRobot.me.x, myRobot.me.y))&&
                myRobot.me.karbonite<myRobot.SPECS.UNITS[myRobot.SPECS.PILGRIM].KARBONITE_CAPACITY &&
                    myRobot.me.fuel<myRobot.SPECS.UNITS[myRobot.SPECS.PILGRIM].FUEL_CAPACITY) {
            return myRobot.mine(); // if we can't give, mine.
        }

        return null;
    }

}
