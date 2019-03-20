package bc19;

import java.util.ArrayList;

public class Vince {
    /**
     * Master function of Vineet's strat
     */
    public static Action master(MyRobot myRobot){
        if(myRobot.me.unit==myRobot.SPECS.CASTLE){
            return castle(myRobot);
        } else if (myRobot.me.unit==myRobot.SPECS.PILGRIM){
            return pilgrim(myRobot);
        } else if (myRobot.me.unit==myRobot.SPECS.CHURCH){
            return church(myRobot);
        } else if (myRobot.me.unit==myRobot.SPECS.PROPHET){
            return prophet(myRobot);
        } else if (myRobot.me.unit==myRobot.SPECS.CRUSADER){
            return crusader(myRobot);
        } else if (myRobot.me.unit==myRobot.SPECS.PREACHER){
            return preacher(myRobot);
        }
        return null; // just to make intellij behave
    }

    /**
     * Castle logic
     */
    private static Action castle(MyRobot myRobot){


        myRobot.log("Castle turn" + myRobot.turn);

        // first thing to do is figure out where the enemy castle is located
        // because the map is symmetrical this can be done
        if(myRobot.turn == 1){
            Castle.initialize(myRobot);
        }
        if(myRobot.turn<=3){
            Castle.findCastleLocations(myRobot);
        }

        if(myRobot.karbLimit>0){
            BuildAction emergencyTroop = Castle.produceEmergencyTroops(myRobot, myRobot.SPECS.PREACHER);
            if (emergencyTroop != null)
                return emergencyTroop;
        }


        // produce only 2 pilgrims, always make sure we have more than 90 karbonite
        // (3 preachers worth of karbonite) so that we can spawn them if an enemy is
        // spotted
        if (myRobot.pilgrimsProduced < 2  && myRobot.karbonite >= 10 + myRobot.karbLimit) {
            BuildAction pilgrim = Castle.producePilgrim(myRobot);
            if (pilgrim != null) {
                return pilgrim;
            }
        }

        // Otherwise, if we have enough Karbonite, just keep producing Prophets and Pilgrims
        if (myRobot.karbonite >= 25 + myRobot.karbLimit) {
            // build pilgrims alternatively with prophets
            if (myRobot.turn<=3||(myRobot.pilgrimsProduced < myRobot.numKarbo / 5 && myRobot.prophetsProduced % 2 == 1)) {
                BuildAction pilgrim = Castle.producePilgrim(myRobot);
                if (pilgrim != null) {
                    return pilgrim;
                }
            }

            // otherwise build a prophet
            BuildAction prophet = Castle.produceProphet(myRobot);
            if (prophet != null) {
                return prophet;
            }
        }

        return null;
    }

    /**
     * Pilgrim logic
     */
    private static Action pilgrim(MyRobot myRobot){
        // first initialize your closest mining location, and your home base (castle)
        Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
        if (myRobot.turn == 1) {
            for (Robot r : myRobot.visibleRobots) {
                Tuple rLoc = new Tuple(r.x, r.y);
                // First find the castle that has spawned you.  The castle will be
                // adjacent to you, will be visible, and will be sending out a signal
                if (r.unit == myRobot.SPECS.CASTLE && r.x >= 0 && r.signal >= 0 && r.signal <= 6464 &&
                        MapFunctions.radiusDistance(myLoc, rLoc) <= 2) {
                    myRobot.castleLoc = myRobot.home = rLoc; // set your home base to be the castleLoc

                    // the closest mining location
                    myRobot.closestNode = new TileNode();
                    myRobot.closestNode.tileType = 1;

                    // The castle's signal is a 4 digit number where the first 2 digits
                    // are the x-coordinate, and the last 2 are the y-coordinate
                    myRobot.closestNode.location = new Tuple(r.signal / 100, r.signal % 100);

                    if (MapFunctions.radiusDistance(myRobot.closestNode.location, myLoc) <= 5) {
                        myRobot.closeToBase = true;
                        myRobot.karbCapacity = 10;
                    } else {
                        myRobot.karbCapacity = 20;
                    }
                    myRobot.log("Closest mining location is " + myRobot.closestNode.location.toString());
                }
            }
        }

        // detect enemies
        Robot enemyRob = Combat.getClosestCombatEnemy(myRobot);
        if (enemyRob != null) {
            Tuple enemyLoc = new Tuple(enemyRob.x, enemyRob.y);
            // if enemy is within 2 * its attack radius, then start running away
            if (MapFunctions.radiusDistance(enemyLoc, myLoc) <=
                    1.3 * myRobot.SPECS.UNITS[enemyRob.unit].VISION_RADIUS + 16.8) {
                myRobot.log("PILGRIM " + (1.3 * myRobot.SPECS.UNITS[enemyRob.unit].VISION_RADIUS + 16.8));
                Tuple randMove = myRobot.randomMove(myLoc, Directions.dirTowards(enemyLoc, myLoc));
                if (randMove != null) {
                    return myRobot.move(randMove.x, randMove.y);
                }
            }
        }

        // if you have no karbonite go to the closestNode
        if (!myRobot.closestNode.location.equals(myLoc) &&
                myRobot.me.karbonite < myRobot.karbCapacity && myRobot.me.fuel < myRobot.fuelCapacity) {
            Tuple tup = MapFunctions.moveTo(myRobot, myRobot.closestNode.location, 2,0);
            if (tup != null) {
                return myRobot.move(tup.x, tup.y);
            }
        }

        // TODO: this really shouldn't be a class instance variable, should be a local
        // variable so that we don't have to keep setting it to null.  Talk to Vineet
        // to see if we can change this
        myRobot.structLoc = null;
        // check to see if church or castle is adjacent
        for(Robot r : myRobot.visibleRobots){
            Tuple rLoc = new Tuple(r.x,r.y);
            if(MapFunctions.radiusDistance(rLoc,myLoc) <= 2 && r.team == myRobot.me.team &&
                    (r.unit == myRobot.SPECS.CHURCH || r.unit == myRobot.SPECS.CASTLE)){
                myRobot.structLoc = rLoc;
                break;
            }
        }

        // if adjacent structure exists, give it Karbonite when you fill up
        if(myRobot.structLoc != null &&
                (myRobot.me.karbonite >= myRobot.karbCapacity||myRobot.me.fuel >= myRobot.fuelCapacity)){
            return myRobot.give(myRobot.structLoc.x - myLoc.x, myRobot.structLoc.y - myLoc.y,
                    myRobot.karbCapacity ,0);
        }

        // if no adjacent church/castle, try building one
        if(myRobot.structLoc==null &&
                (myRobot.me.karbonite >= myRobot.karbCapacity||myRobot.me.fuel >= myRobot.fuelCapacity) && !myRobot.closeToBase){
            // build a nearby church if you don't see one already
            BuildAction church = myRobot.buildRobo(myRobot.SPECS.CHURCH, -1);
            if(church != null){
                // communicate castleLoc value locally
                myRobot.signal(myRobot.castleLoc.x*100+myRobot.castleLoc.y,1);
                return church;
            }
        }

        // if capacity is filled up and no structure is visible, go back home to deposit karbonite
        if (myRobot.structLoc == null &&
                (myRobot.me.karbonite >= myRobot.karbCapacity||myRobot.me.fuel >= myRobot.fuelCapacity)) {
            Tuple tup = MapFunctions.moveTo(myRobot, myRobot.home, 2,0);
            if (tup != null) {
                return myRobot.move(tup.x, tup.y);
            } else  {
                // try giving the Karbonite to an adjacent ally, and hope they pass it on
                // to someone close by
                ArrayList<Robot> visibleAllyTroops = Combat.getVisibleAllyTroops(myRobot);
                for (Robot r : visibleAllyTroops) {
                    Tuple rLoc = new Tuple(r.x,r.y);
                    if(MapFunctions.radiusDistance(rLoc, myLoc) <= 2) {
                        Tuple toAlly = myRobot.diff(rLoc, myLoc);
                        return myRobot.give(toAlly.x, toAlly.y, myRobot.me.karbonite, myRobot.me.fuel);
                    }
                }
            }
        }


        // if on mining location
        if (myRobot.closestNode.location.equals(new Tuple(myRobot.me.x, myRobot.me.y)) &&
                myRobot.me.karbonite < myRobot.karbCapacity && myRobot.me.fuel < myRobot.fuelCapacity) {
            return myRobot.mine(); // if we can't give, mine.
        }

        return null;
    }

    /**
     * Church logic
     */
    private static Action church(MyRobot myRobot){
        // initialize a home castle location based on pilgrims
        if(myRobot.turn==1){
            myRobot.karboDepots=myRobot.getTypedTiles(myRobot.tileNodeMap,1);
            myRobot.fuelDepots=myRobot.getTypedTiles(myRobot.tileNodeMap,2);
            for(Robot r:myRobot.getVisibleRobots()){
                if(r.unit==myRobot.SPECS.PILGRIM&&r.team==myRobot.me.team&&r.signal>0){
                    myRobot.castleLoc = new Tuple(r.signal/100,r.signal%100);
                    myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc,myRobot.karboDepots);
                }
            }
        }

        // Churches mostly build combat units
        BuildAction prophet = Castle.produceProphet(myRobot);
        if (prophet != null) {
            myRobot.signal(myRobot.castleLoc.x*100+myRobot.castleLoc.y,1);
            return prophet;
        }

        return null;
    }

    /**
     * Prophet logic
     */
    private static Action prophet(MyRobot myRobot){
        Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);

        if (myRobot.turn == 1) {
            myRobot.log("I am a prophet");
            for (Robot r : myRobot.visibleRobots){
                // If the unit is a castle on our team that is in our vision (r.x can be seen)
                // and the castle is also broadcasting a signal
                if ((r.unit == myRobot.SPECS.CASTLE) && r.team == myRobot.me.team && r.x >= 0 &&
                        r.signal >= 10000 && r.signal < 26464 /* r.signal >= 0 && r.signal < 8 */) {
                    myRobot.castleLoc = new Tuple(r.x, r.y);
                    myRobot.karboDepots = myRobot.getTypedTiles(myRobot.tileNodeMap,1);
                    myRobot.closestEnemyLoc = new Tuple((r.signal % 10000) / 100, r.signal % 100);
                    myRobot.enemyDirection = Directions.dirTowards(myLoc, myRobot.closestEnemyLoc);
                    myRobot.moveMode = true;

                    // calculate the enemy castle location
                    myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc, myRobot.karboDepots);
                }
            }

            if (myRobot.enemyCastleLoc != null) {
                myRobot.log("enemy castle location");
            } else {
                myRobot.log("ENEMY CASTLE LOCATION IS NULL");
            }
        }

        if (myRobot.attackCounter && myRobot.turn % 10 == 0) {
            myRobot.attackMode = true;
        }

        for (Robot r : myRobot.visibleRobots) {
            // if given a signal to move by a church or castle then move randomly
            if ((r.unit == myRobot.SPECS.CHURCH || r.unit == myRobot.SPECS.CASTLE) && r.signal == 59999) {
                myRobot.attackMode = true;
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
            myRobot.enemyDirection = Directions.dirTowards(myLoc, enemyLoc);
            return myRobot.attack(toEnemy.x, toEnemy.y);
        }

        // if in attack mode, rush to enemy
        if(myRobot.attackMode && !myRobot.enemyCastleLoc.equals(myLoc)){
            myRobot.attackCounter = true;
            // move quickly towards the enemy
            Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 1, 0);

            if (tup != null) {
                myRobot.attackMode = false;
                return myRobot.move(tup.x, tup.y);
            } else {
                // cannot move
                myRobot.attackMode = false;
            }
        }

        // if you're set to moveMode, move randomly towards the enemy
        if(myRobot.moveMode) {
            Tuple randMove = myRobot.randomMove(new Tuple(myRobot.me.x, myRobot.me.y), myRobot.enemyDirection);
            if (randMove != null) {
                myRobot.moveMode = false;
                return myRobot.move(randMove.x, randMove.y);
            }
        }



        // otherwise check if you have karbonite to give to castle
        for (Robot r : myRobot.visibleRobots) {
            if (myRobot.me.karbonite > 0 && r.unit == myRobot.SPECS.CASTLE && r.x >= 0) {
                Tuple castleLoc = new Tuple(r.x, r.y);
                // if the castle is adjacent, give Karbonite
                if (MapFunctions.radiusDistance(castleLoc, myLoc) <= 2) {
                    Tuple diffTup = myRobot.diff(castleLoc, myLoc);
                    return myRobot.give(diffTup.x, diffTup.y, myRobot.me.karbonite, myRobot.me.fuel);
                }
            }
        }

        if (myRobot.me.karbonite > 0) {
            int dirToCastle = Directions.dirTowards(myLoc, myRobot.castleLoc);
            Tuple[] sortedDirs = Directions.orderDirs(dirToCastle);
            for (int i = 0; i < 5; i++) {
                if (myRobot.visibleMap[myLoc.y + sortedDirs[i].y][myLoc.x + sortedDirs[i].x] > 0) {
                    return myRobot.give(sortedDirs[i].x, sortedDirs[i].y, myRobot.me.karbonite, myRobot.me.fuel);
                }
            }
        }

        return null;
    }

    /**
     * Crusader logic
     */
    private static Action crusader(MyRobot myRobot){
        Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);

        if (myRobot.turn == 1) {
            myRobot.log("I am a crusader");
            for (Robot r : myRobot.visibleRobots){
                // If the unit is a castle on our team that is in our vision (r.x can be seen)
                // and the castle is also broadcasting a signal
                // 20000+ means prophet
                if ((r.unit == myRobot.SPECS.CASTLE) && r.team == myRobot.me.team && r.x >= 0 &&
                        r.signal >= 10000 && r.signal < 26464 /* r.signal >= 0 && r.signal < 8 */) {
                    myRobot.castleLoc = new Tuple(r.x, r.y);
                    myRobot.karboDepots = myRobot.getTypedTiles(myRobot.tileNodeMap,1);
                    myRobot.closestEnemyLoc = new Tuple((r.signal % 10000) / 100, r.signal % 100);
                    myRobot.enemyDirection = Directions.dirTowards(myLoc, myRobot.closestEnemyLoc);

                    if (r.signal / 10000 == 1) {
                        myRobot.moveMode = true;
                    } else {
                        // there is a prophet
                        myRobot.moveModeProphet = true;
                    }

                    myRobot.alert = true;

                    myRobot.log("CRUSADER The closest enemy is at " + myRobot.closestEnemyLoc.toString());

                    // calculate the enemy castle location
                    myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc, myRobot.karboDepots);
                }
            }
        }
        // See if there are enemies that can be attacked
        Robot closestEnemy = Combat.getClosestEnemy(myRobot);
        if (closestEnemy != null) {
            myRobot.castleTalk(101);
            Tuple enemyLoc = new Tuple(closestEnemy.x, closestEnemy.y);
            if (MapFunctions.radiusDistance(myLoc, enemyLoc) <= 16) {
                Tuple toEnemy = myRobot.diff(enemyLoc, myLoc);
                // after attacking set your movement mode on so you can move a few steps
                // towards where the enemies are coming from.  This is done to create
                // space for other ally troops that will be spawned later
                myRobot.moveMode = true;
                myRobot.moveModeProphet = false;
                myRobot.closestEnemyLoc = enemyLoc;
                myRobot.enemyDirection = Directions.dirTowards(myLoc, enemyLoc);
                return myRobot.attack(toEnemy.x, toEnemy.y);
            } else {
                myRobot.closestEnemyLoc = enemyLoc;
                myRobot.moveModeProphet = true;
            }
        }

        // check if castle has given any signals
        for (Robot r : myRobot.visibleRobots) {
            // if given a signal to move by a church or castle then move randomly
            // the castle need not be in visible range
            if ((r.unit == myRobot.SPECS.CHURCH || r.unit == myRobot.SPECS.CASTLE) &&
                    r.signal == 50000) {
                myRobot.attackMode = true;
            }

            // sending out closest enemy location
            if (r.unit == myRobot.SPECS.CASTLE && r.x > 0 && r.signal >= 10000 && r.signal < 26464) {
                if (r.signal / 10000 == 1) {
                    myRobot.moveMode = true;
                } else {
                    // there is a prophet
                    myRobot.moveModeProphet = true;
                }
                myRobot.log("CRUSADER The closest enemy is at " + myRobot.closestEnemyLoc.toString());
                myRobot.log("CRUSADER Alert and Move set to true");
                myRobot.alert = true;
                myRobot.closestEnemyLoc = new Tuple((r.signal % 10000) / 100, r.signal % 100);
            } else if (r.unit == myRobot.SPECS.CASTLE && r.x >= 0) {
                myRobot.log("CRUSADER Alert set to false");
                myRobot.alert = false;
            }
        }

        // if in attack mode, rush to enemy
        if(myRobot.attackMode && !myRobot.enemyCastleLoc.equals(myLoc)){
            myRobot.attackCounter = true;
            // move quickly towards the enemy
            Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 1, 0);

            if (tup != null) {
                return myRobot.move(tup.x, tup.y);
            } else {
                // cannot move
                myRobot.attackMode = false;
            }
        }

        if (myRobot.moveModeProphet) {
            myRobot.log("Trying to move to " + myRobot.closestEnemyLoc.toString());
            Tuple closestEnemyMove = MapFunctions.moveTo(myRobot, myRobot.closestEnemyLoc, 3, 0);
            if (closestEnemyMove != null) {
                myRobot.log("CRUSADER moving towards " + new Tuple(closestEnemyMove.x + myLoc.x, closestEnemyMove.y + myLoc.y).toString());
                return myRobot.move(closestEnemyMove.x, closestEnemyMove.y);
            } else {
                myRobot.log("CRUSADER Cannot move towards enemy PROPHET anymore, set move to false");
                myRobot.moveModeProphet = false;
            }
        }

        // if there are enemies in the area, avoid moving too close to them
        // because you want to get the first shot
        if (myRobot.moveMode && myRobot.alert) {
            Path pathToEnemy = MapFunctions.shortestPath(myRobot, myRobot.closestEnemyLoc, 0);
            if (pathToEnemy.radialMovementDist <= 6) {
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

        // otherwise check if you have karbonite to give to castle
        for (Robot r : myRobot.visibleRobots) {
            if (myRobot.me.karbonite > 0 && r.unit == myRobot.SPECS.CASTLE && r.x >= 0) {
                Tuple castleLoc = new Tuple(r.x, r.y);
                // if the castle is adjacent, give Karbonite
                if (MapFunctions.radiusDistance(castleLoc, myLoc) <= 2) {
                    Tuple diffTup = myRobot.diff(castleLoc, myLoc);
                    return myRobot.give(diffTup.x, diffTup.y, myRobot.me.karbonite, myRobot.me.fuel);
                }
            }
        }

        if (myRobot.me.karbonite > 0) {
            int dirToCastle = Directions.dirTowards(myLoc, myRobot.castleLoc);
            Tuple[] sortedDirs = Directions.orderDirs(dirToCastle);
            for (int i = 0; i < 5; i++) {
                if (myRobot.visibleMap[myLoc.y + sortedDirs[i].y][myLoc.x + sortedDirs[i].x] > 0) {
                    return myRobot.give(sortedDirs[i].x, sortedDirs[i].y, myRobot.me.karbonite, myRobot.me.fuel);
                }
            }
        }

        return null;
    }

    /**
     * Preacher logic
     */
    private static Action preacher(MyRobot myRobot){
        Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);

        if (myRobot.turn == 1) {
            myRobot.log("I am a preacher");
            GeneralUnit.initializeUnit(myRobot);
        }

        Preacher.reactToCastleSignal(myRobot);

        if (myRobot.attackCounter && myRobot.turn % 10 == 0) {
            myRobot.attackMode = true;
        }

        AttackAction attack = Preacher.attackClosestEnemy(myRobot);
        if (attack != null) {
            // after attacking set your movement mode on so you can move a few steps
            // towards where the enemies are coming from.  This is done to create
            // space for other ally troops that will be spawned later
            myRobot.moveMode = true;
            myRobot.moveModeProphet = false;
            return attack;
        }

        // if in attack mode, rush to enemy
        if(myRobot.attackMode && !myRobot.enemyCastleLoc.equals(myLoc)){
            myRobot.attackCounter = true;
            // move quickly towards the enemy
            Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 1, 0);

            if (tup != null) {
                myRobot.attackMode = false;
                return myRobot.move(tup.x, tup.y);
            } else {
                // cannot move
                myRobot.attackMode = false;
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
            if (pathToEnemy.radialMovementDist <= 6) {
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

        // otherwise check if you have karbonite to give to castle
        for (Robot r : myRobot.visibleRobots) {
            if (myRobot.me.karbonite > 0 && r.unit == myRobot.SPECS.CASTLE && r.x >= 0) {
                Tuple castleLoc = new Tuple(r.x, r.y);
                // if the castle is adjacent, give Karbonite
                if (MapFunctions.radiusDistance(castleLoc, myLoc) <= 2) {
                    Tuple diffTup = myRobot.diff(castleLoc, myLoc);
                    return myRobot.give(diffTup.x, diffTup.y, myRobot.me.karbonite, myRobot.me.fuel);
                }
            }
        }

        if (myRobot.me.karbonite > 0) {
            int dirToCastle = Directions.dirTowards(myLoc, myRobot.castleLoc);
            Tuple[] sortedDirs = Directions.orderDirs(dirToCastle);
            for (int i = 0; i < 5; i++) {
                if (myRobot.visibleMap[myLoc.y + sortedDirs[i].y][myLoc.x + sortedDirs[i].x] > 0) {
                    return myRobot.give(sortedDirs[i].x, sortedDirs[i].y, myRobot.me.karbonite, myRobot.me.fuel);
                }
            }
        }

        return null;
    }


}
