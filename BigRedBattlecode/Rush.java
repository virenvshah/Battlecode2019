package bc19;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Rush {
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
        // first check to see if there are any enemies around
        Robot[] visibleRobots = myRobot.getVisibleRobots();
        ArrayList<Robot> visibleEnemies = new ArrayList<Robot>();

        if(myRobot.otherCastleLocs==null){
            myRobot.otherCastleLocs=new HashSet<>();
        }
        if(myRobot.otherCastleLocsX==null){
            myRobot.otherCastleLocsX = new HashMap<>();
        }
        if(myRobot.otherCastleLocsY==null){
           myRobot.otherCastleLocsY = new HashMap<>();
        }

        if(myRobot.turn==1){
            myRobot.castleTalk(myRobot.me.x);
        }
        if(myRobot.turn==2){
            myRobot.castleTalk(myRobot.me.y);
        }


        for (Robot r : visibleRobots) {
            // the second condition is to ensure that the location can be seen, which means
            // that it is not a robot that is visible only because of signaling
            if (r.team != myRobot.me.team && r.x >= 0)
                visibleEnemies.add(r);
            if(r.team==myRobot.me.team && r.unit==myRobot.SPECS.CASTLE&&myRobot.turn==2){
                myRobot.otherCastleLocsX.put(r.id,r.castle_talk);
            }
            if(r.team==myRobot.me.team && r.unit==myRobot.SPECS.CASTLE&&myRobot.turn==3){
                myRobot.otherCastleLocsY.put(r.id,r.castle_talk);
            }
        }

        // if we have both x and y, then everything is there
        for(int id: myRobot.otherCastleLocsX.keySet()){
            if(myRobot.otherCastleLocsY.containsKey(id)){
                myRobot.otherCastleLocs.add(new Tuple(myRobot.otherCastleLocsX.get(id),myRobot.otherCastleLocsY.get(id)));
            }
        }


        // if there are enemies start spawning prophets in the enemy's direction
        if (visibleEnemies.size() > 0) {
            Tuple enemyLoc = new Tuple(visibleEnemies.get(0).x, visibleEnemies.get(0).y);
            myRobot.log("Turn: " + myRobot.turn + " Enemy spotted " + enemyLoc.toString() +
                    " Number of enemies: " + visibleEnemies.size() + " Troop type " + visibleEnemies.get(0).unit);
            // calculate enemy direction
            int enemyDirection = Directions.dirTowards(new Tuple(myRobot.me.x, myRobot.me.y), enemyLoc);
            Object nobleBot = myRobot.buildRobo(myRobot.SPECS.PROPHET, enemyDirection);
            if(nobleBot!=null){return (BuildAction)nobleBot;}
        }
        // produce 1 karbonite pilgrim & 1 fuel pilgrim & always make sure we have more than 75 karbonite
        else if (!myRobot.karboPilgrim) {
            myRobot.log("Building a karbonite pilgrim");
            Object nobleBot = myRobot.buildRobo(myRobot.SPECS.PILGRIM, -1);
            if(nobleBot!=null){
                myRobot.pilgrimsProduced++;
                myRobot.karboPilgrim = true;
                if (myRobot.karboDepots == null){
                    myRobot.log("getting list of all tile nodes with karbonite");
                    myRobot.karboDepots=myRobot.getTypedTiles(myRobot.tileNodeMap,1);
                }

                myRobot.log("finding closest karbo depot");
                myRobot.closestNode = myRobot.closestNode(myRobot.karboDepots);


                // so that another pilgrim doesn't visit the same depot
                myRobot.karboDepots.remove(myRobot.closestNode);
                // signal next turn for the pilgrim
                myRobot.signal(myRobot.closestNode.location.x * 100 + myRobot.closestNode.location.y, 2);

                return (BuildAction)nobleBot;
            }
        } else if (myRobot.fuelPilgrim && myRobot.preachersProduced<1) { // only if karbo pilgrim and fuel pilgrim were made (which is atleast 2 turns in)
            Object nobleBot = myRobot.buildRobo(myRobot.SPECS.PREACHER, -1);
            if(nobleBot!=null){
                myRobot.preachersProduced++;
                if(myRobot.otherCastleLoc==null){
                    int minDist = Integer.MAX_VALUE;
                    Tuple center = new Tuple(myRobot.map[0].length/2,myRobot.map.length/2);
                    for(Tuple cas:myRobot.otherCastleLocs){
                        if(minDist>MapFunctions.manhattanDistance(center,cas)){
                            minDist = MapFunctions.manhattanDistance(center,cas);
                            myRobot.otherCastleLoc = cas;
                        }
                    }
                }

                // signal next turn for preacher
                myRobot.signal(myRobot.otherCastleLoc.x*100+myRobot.otherCastleLoc.y,2);
                return (BuildAction)nobleBot;
            }
        } else if(!myRobot.fuelPilgrim){ // only run if karbo pilgrim was made but fuel wasn't made
            myRobot.log("Building a fuel pilgrim");
            Object nobleBot = myRobot.buildRobo(myRobot.SPECS.PILGRIM, -1);
            if(nobleBot!=null){
                myRobot.pilgrimsProduced++;
                myRobot.fuelPilgrim = true;
                if (myRobot.fuelDepots == null){
                    myRobot.log("getting list of all tile nodes with fuel");
                    myRobot.fuelDepots=myRobot.getTypedTiles(myRobot.tileNodeMap,2);
                }

                myRobot.log("finding closest fuel depot");
                myRobot.closestNode = myRobot.closestNode(myRobot.fuelDepots);


                // so that another pilgrim doesn't visit the same depot
                myRobot.fuelDepots.remove(myRobot.closestNode);
                // signal next turn for the pilgrim
                myRobot.signal(myRobot.closestNode.location.x * 100 + myRobot.closestNode.location.y, 2);

                return (BuildAction)nobleBot;
            }
        } else { // make prophets
            Object nobleBot = myRobot.buildRobo(myRobot.SPECS.PROPHET, -1);
            if(nobleBot!=null){
                if(myRobot.otherCastleLoc==null){
                    int minDist = Integer.MAX_VALUE;
                    Tuple center = new Tuple(myRobot.map[0].length/2,myRobot.map.length/2);
                    for(Tuple cas:myRobot.otherCastleLocs){
                        if(minDist>MapFunctions.manhattanDistance(center,cas)){
                            minDist = MapFunctions.manhattanDistance(center,cas);
                            myRobot.otherCastleLoc = cas;
                        }
                    }
                }

                // signal next turn for prophet
                myRobot.signal(myRobot.otherCastleLoc.x*100+myRobot.otherCastleLoc.y,2);
                return (BuildAction)nobleBot;
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
        if (myRobot.turn == 1) {
            myRobot.log("I am a prophet");
            for(Robot r : myRobot.visibleRobots) {
                if(r.unit == myRobot.SPECS.CASTLE && r.team == myRobot.me.team && r.x > 0) {
                    myRobot.castleLoc = new Tuple(r.x, r.y);
                    myRobot.karboDepots = myRobot.getTypedTiles(myRobot.tileNodeMap, 1);
                    myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc, myRobot.karboDepots);
                    myRobot.otherCastleLoc = new Tuple(r.signal / 100, r.signal % 100);
                    myRobot.nextEnemyLoc = myRobot.getEnemyLoc(myRobot.otherCastleLoc, myRobot.karboDepots);
                }
            }
        }

        Robot[] visibleRobots = myRobot.getVisibleRobots();

        // filter out same team robots
        ArrayList<Robot> visibleEnemies = new ArrayList<Robot>();
        for (Robot r : visibleRobots) {
            if ((myRobot.castleLoc == null || myRobot.otherCastleLoc == null) && r.team == myRobot.me.team && r.unit == myRobot.SPECS.CASTLE){
                myRobot.log("found castle");
                myRobot.castleLoc = new Tuple(r.x, r.y);
                myRobot.otherCastleLoc = new Tuple(r.signal / 100, r.signal % 100);
            }

            if (r.team != myRobot.me.team)
                visibleEnemies.add(r);
        }
        if (visibleEnemies.size() == 0) {
            if (!myRobot.enemyCastleLoc.equals(new Tuple(myRobot.me.x, myRobot.me.y))) {
                Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 2, 0);
                if (tup != null) {
                    return myRobot.move(tup.x, tup.y);
                }
            }
            else{
                myRobot.enemyCastleLoc = myRobot.nextEnemyLoc;
                if (!myRobot.enemyCastleLoc.equals(new Tuple(myRobot.me.x, myRobot.me.y))) {
                    Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 2, 0);
                    if (tup != null) {
                        return myRobot.move(tup.x, tup.y);
                    }
                }
            }

        }
        else {
            // find the closest robot and its distance
            int minDist = Integer.MAX_VALUE;
            int mindx = 0;
            int mindy = 0;
            for (Robot r : visibleEnemies) {
                int xDiff = Math.abs(myRobot.me.x - r.x);
                int yDiff = Math.abs(myRobot.me.y - r.y);
                int dist = Math.max(xDiff, yDiff);
                if (dist < minDist) {
                    minDist = dist;
                    mindx = r.x - myRobot.me.x;
                    mindy = r.y - myRobot.me.y;
                }
            }

            // if enemy within attacking range, attack
            if (minDist >= 4 && minDist <= 8) // these attack range values hard-coded from spec, change to API call if necessary
                return myRobot.attack(mindx, mindy);
        }

        return null;

    }

    /**
     * Preacher strategy
     */
    private static Action preacher(MyRobot myRobot){
        if (myRobot.turn == 1) {
            myRobot.log("I am a preacher");
            for(Robot r : myRobot.visibleRobots) {
                if(r.unit == myRobot.SPECS.CASTLE && r.team == myRobot.me.team && r.x > 0) {
                    myRobot.castleLoc = new Tuple(r.x, r.y);
                    myRobot.karboDepots = myRobot.getTypedTiles(myRobot.tileNodeMap, 1);
                    myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc, myRobot.karboDepots);
                    myRobot.otherCastleLoc = new Tuple(r.signal / 100, r.signal % 100);
                    myRobot.nextEnemyLoc = myRobot.getEnemyLoc(myRobot.otherCastleLoc, myRobot.karboDepots);
                }
            }
        }

        Robot[] visibleRobots = myRobot.getVisibleRobots();

        // filter out same team robots
        ArrayList<Robot> visibleEnemies = new ArrayList<Robot>();
        for (Robot r : visibleRobots) {
            if ((myRobot.castleLoc == null || myRobot.otherCastleLoc == null) && r.team == myRobot.me.team && r.unit == myRobot.SPECS.CASTLE){
                myRobot.log("found castle");
                myRobot.castleLoc = new Tuple(r.x, r.y);
                myRobot.otherCastleLoc = new Tuple(r.signal / 100, r.signal % 100);
            }

            if (r.team != myRobot.me.team)
                visibleEnemies.add(r);
        }
        if (visibleEnemies.size() == 0) {
            if (!myRobot.enemyCastleLoc.equals(new Tuple(myRobot.me.x, myRobot.me.y))) {
                Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 2, 0);
                if (tup != null) {
                    return myRobot.move(tup.x, tup.y);
                }
            }
            else{
                myRobot.enemyCastleLoc = myRobot.nextEnemyLoc;
                if (!myRobot.enemyCastleLoc.equals(new Tuple(myRobot.me.x, myRobot.me.y))) {
                    Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 2, 0);
                    if (tup != null) {
                        return myRobot.move(tup.x, tup.y);
                    }
                }
            }

        }
        else {
            // find the closest robot and its distance
            int minDist = Integer.MAX_VALUE;
            int mindx = 0;
            int mindy = 0;
            for (Robot r : visibleEnemies) {
                int xDiff = Math.abs(myRobot.me.x - r.x);
                int yDiff = Math.abs(myRobot.me.y - r.y);
                int dist = Math.max(xDiff, yDiff);
                if (dist < minDist) {
                    minDist = dist;
                    mindx = r.x - myRobot.me.x;
                    mindy = r.y - myRobot.me.y;
                }
            }

            // if enemy within attacking range, attack
            if (minDist >= 1 && minDist <= 4) // these attack range values hard-coded from spec, change to API call if necessary
                return myRobot.attack(mindx, mindy);
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
               // ArrayList<Robot> visibleAllyTroops = Combat.getVisibleAllyTroops(myRobot);
                // try giving the Karbonite to a nearby castle first
                for(Robot r: myRobot.visibleRobots){
                   
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
            myRobot.log("PILGRIM CURRENT LOC: "+myLoc.toString());
            myRobot.log("PILGRIM DEPOT: "+myRobot.closestNode.toString());
            myRobot.log("PILGRIM HOME: "+myRobot.home.toString());
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
