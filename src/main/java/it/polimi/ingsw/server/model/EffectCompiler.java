package it.polimi.ingsw.server.model;

import it.polimi.ingsw.server.cards.RuleEffect;
import it.polimi.ingsw.server.cards.enums.EffectType;
import it.polimi.ingsw.common.enums.BuildingType;
import it.polimi.ingsw.server.model.enums.LevelType;
import it.polimi.ingsw.server.model.enums.PlayerFlag;
import it.polimi.ingsw.server.model.enums.PlayerState;
import it.polimi.ingsw.server.model.exceptions.PlayerLostSignal;
import it.polimi.ingsw.server.model.exceptions.PlayerWonSignal;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Translates Rule Effects into Compiled Effects
 */
class EffectCompiler {

    /** Compiles the given effect
     * @param model the internal model is needed to encapsulate it in the lambdas
     * @param effect the effect to compile
     * @return the compiled effect
     */
    public static LambdaEffect compileEffect(InternalModel model, RuleEffect effect) {

        assert (model != null && effect != null);

        EffectType effectType = effect.getType();
        LambdaEffect compiledEffect = null;

        switch (effectType){
            case ALLOW:
                switch (effect.getAllowType()){
                    case STANDARD:
                        compiledEffect = compileAllowStandardEffect(model, effect);
                        break;
                    case SET_OPPONENT:
                        compiledEffect = compileAllowSetOpponentEffect(model, effect);
                        break;
                    case BUILD_UNDER:
                        compiledEffect = compileAllowBuildUnderEffect(model, effect);
                        break;
                    default:
                        assert false;
                }
                break;
            case DENY:
                compiledEffect = ((moveData, buildData, simulate) -> {
                    if (!simulate) {
                        throw new PlayerLostSignal();
                    }
                    return true;
                });
                break;
            case WIN:
                compiledEffect = ((moveData, buildData, simulate) -> {
                    if (!simulate) {
                        throw new PlayerWonSignal();
                    }
                    return true;
                });
                break;
        }
        return compiledEffect;
    }

    private static LambdaEffect compileAllowStandardEffect(InternalModel model, RuleEffect effect) {


        PlayerState nextPlayerState = effect.getNextState();

        return (moveData, buildData, simulate) -> {

            if(buildData == null) {

                // All the player moves
                List<Point> moves = moveData.getData();
                Point startPosition = moveData.getWorker().getPosition();
                Point finalPosition = moves.get(moves.size() - 1);
                Cell startPositionCell = model.getBoard().getCell(startPosition);
                Cell finalPositionCell = model.getBoard().getCell(finalPosition);
                Worker myWorker = moveData.getWorker();



                // Where i want to go should be without workers and domes (should be already tested)
                if (finalPositionCell.isOccupied()){
                    assert false;
                    return false;
                }

                // Check i am where i want to start the move
                assert(startPositionCell.getWorkerID().equals(myWorker.getID()));


                // If we are not in a simulation
                if(!simulate){

                    // Set my worker in final cell
                    finalPositionCell.setWorker(myWorker.getID());

                    // remove my worker from previous position
                    if ((!startPositionCell.removeWorker())) throw new AssertionError();

                    // Set my new worker's position
                    myWorker.setPosition(finalPosition);

                    // Set the new player state
                    moveData.getPlayer().setPlayerState(nextPlayerState);

                    setPlayerFlags(model.getBoard(),moveData,startPosition); //Update flags
                }
                return true;
            }

            else if(moveData == null){


                Map<Point, List<BuildingType>> builds = buildData.getData();
                Iterator<Point> buildingPos = builds.keySet().iterator();
                List<BuildingType> allBuildingsIWantToBuild = new ArrayList<>();

                // CHeck i can build the chosen buildings in the chosen cells
                while(buildingPos.hasNext()){
                    Point whereIWantToBuild = buildingPos.next();
                    List<BuildingType> whatIWantToBuildHere = builds.get(whereIWantToBuild);
                    allBuildingsIWantToBuild.addAll(whatIWantToBuildHere);
                    if(!model.getBoard().getCell(whereIWantToBuild).canBuild(whatIWantToBuildHere))
                        return false;
                }

                if(notEnoughBuildings(model, allBuildingsIWantToBuild))
                    return false;

                if(!simulate){
                    buildingPos = builds.keySet().iterator();
                    while(buildingPos.hasNext()){
                        Point whereIWantToBuild = buildingPos.next();
                        List<BuildingType> whatIWantToBuildHere = builds.get(whereIWantToBuild);
                        for(BuildingType b : whatIWantToBuildHere)
                            if(!model.getBoard().getCell(whereIWantToBuild).addBuilding(b)) {
                                System.err.println("L'effetto allow build del worker " + buildData.getWorker().getID() + "nell applicazione dell'effetto ha trovato cose diverse da quelle che ha checkato nell'effetto");
                                assert false;
                            }
                    }

                    Board board = model.getBoard();
                    for(BuildingType b : allBuildingsIWantToBuild)
                        board.useBuilding(b);

                    // Set next player state
                    buildData.getPlayer().setPlayerState(nextPlayerState);
                }

                return true;

            }
            return false;
        };

    }

    private static LambdaEffect compileAllowSetOpponentEffect(InternalModel model, RuleEffect effect) {

        PlayerState nextPlayerState = effect.getNextState();
        LambdaEffect lambdaEffect = null;

        if (effect.getData().equals("PUSH_STRAIGHT")){
            lambdaEffect = ((moveData, buildData, simulate) -> {

                assert(buildData == null);

                Point startPosition = moveData.getWorker().getPosition();
                Cell startPositionCell = model.getBoard().getCell(startPosition);
                List<Point> moves = moveData.getData();
                Point finalPosition = moves.get(moves.size()-1);
                Cell finalPositionCell = model.getBoard().getCell(finalPosition);
                Point mySecondToLastPosition;
                Worker myWorker = moveData.getWorker();
                Worker hisWorker = model.getWorkerByID(finalPositionCell.getWorkerID());
                if(moves.size() > 1)
                    mySecondToLastPosition = moves.get(moves.size()-2);
                else
                    mySecondToLastPosition = startPosition;

                // Check i am where i want to start the move
                assert(startPositionCell.getWorkerID().equals(myWorker.getID()));

                // Check in my final pos there is not a dome
                assert (finalPositionCell.getTopBuilding() != LevelType.DOME);

                // Check there is someone in my final position and it is not me

                if(hisWorker == null || moveData.getPlayer().getWorkers().contains(hisWorker)) {
                    //System.err.println("There is no one in the cell i want to push with my worker or he is one of mine, i am the set opp pos push effect of worker " + moveData.getWorker().getID());
                    //NOTE: if the card was written correctly, no way i can enter here.
                    return false;
                }

                int deltaX = finalPosition.x - mySecondToLastPosition.x;
                int deltaY = finalPosition.y - mySecondToLastPosition.y;

                Cell whereHeHasToGo = model.getBoard().getCell(new Point(finalPosition.x+deltaX,finalPosition.y+deltaY));

                if(whereHeHasToGo == null || whereHeHasToGo.isOccupied())
                    return false;

                if (!simulate) {

                    whereHeHasToGo.setWorker(hisWorker.getID());

                    hisWorker.setPosition(whereHeHasToGo.getPosition());

                    finalPositionCell.removeWorker();

                    finalPositionCell.setWorker(myWorker.getID());

                    startPositionCell.removeWorker();

                    myWorker.setPosition(finalPosition);

                    moveData.getPlayer().setPlayerState(nextPlayerState);

                    setPlayerFlags(model.getBoard(),moveData,startPosition); //Update flags
                }

                return true;

            });
        }
        else if(effect.getData().equals("SWAP")){
             lambdaEffect = ((moveData, buildData, simulate) -> {

                 assert(buildData == null);

                 List<Point> moves = moveData.getData();

                 Point startPosition = moveData.getWorker().getPosition();
                 Cell startPositionCell = model.getBoard().getCell(startPosition);
                 Point finalPosition = moves.get(moves.size()-1);
                 Cell finalPositionCell = model.getBoard().getCell(finalPosition);
                 Point mySecondToLastPosition;
                 if(moves.size() > 1)
                     mySecondToLastPosition = moves.get(moves.size()-2);
                 else
                     mySecondToLastPosition = startPosition;
                 Cell mySecondToLastCell = model.getBoard().getCell(mySecondToLastPosition);
                 Worker myWorker = moveData.getWorker();
                 Worker hisWorker = model.getWorkerByID(finalPositionCell.getWorkerID());

                 // Check i am where i want to start the move
                 assert(startPositionCell.getWorkerID().equals(myWorker.getID()));

                 // My second to last cell is empty
                 assert ((mySecondToLastCell.getWorkerID() == null || mySecondToLastCell.getWorkerID().equals(myWorker.getID())) && mySecondToLastCell.getTopBuilding() != LevelType.DOME);

                 // Check in my final pos there is not a dome
                 assert (finalPositionCell.getTopBuilding() != LevelType.DOME);

                 //System.out.println(finalPosition);
                 // Check there is someone in my final position and it is not me
                 if(hisWorker == null || moveData.getPlayer().getWorkers().contains(hisWorker)) {
                     //System.err.println("There is no one in the cell i want to push with my worker or he is one of mine, i am the set opp pos swap effect of worker " + moveData.getWorker().getID());
                     //NOTE: if the card was written correctly, no way i can enter here.
                     return false;
                 }


                 if (!simulate) {

                     startPositionCell.removeWorker();

                     finalPositionCell.removeWorker();

                     finalPositionCell.setWorker(myWorker.getID());

                     mySecondToLastCell.setWorker(hisWorker.getID());

                     myWorker.setPosition(finalPosition);

                     hisWorker.setPosition(mySecondToLastPosition);

                     moveData.getPlayer().setPlayerState(nextPlayerState);

                     setPlayerFlags(model.getBoard(),moveData,startPosition); //Update flags
                 }

                 return true;
            });
        }
        return lambdaEffect;
    }

    private static LambdaEffect compileAllowBuildUnderEffect(InternalModel model, RuleEffect effect){
        PlayerState nextPlayerState = effect.getNextState();

        return (moveData, buildData, simulate) -> {
            assert moveData == null;

            Map<Point, List<BuildingType>> builds = buildData.getData();
            Iterator<Point> buildingPos = builds.keySet().iterator();
            List<BuildingType> allBuildingsIWantToBuild = new ArrayList<>();
            Worker workerOnBuild = buildData.getWorker();


            while(buildingPos.hasNext()){
                Point whereIWantToBuild = buildingPos.next();
                Cell cellWhereIWantToBuild = model.getBoard().getCell(whereIWantToBuild);
                List<BuildingType> whatIWantToBuildHere = builds.get(whereIWantToBuild);
                boolean isMyPos = whereIWantToBuild.equals(workerOnBuild.getPosition());

                // I CHECK THE POSSIBILITY OF BUILDING USING THE EXCLUDE WORKER FLAG
                if(!cellWhereIWantToBuild.canBuild(whatIWantToBuildHere, isMyPos))
                    return false;

                // WHEN I'M TRYING TO BUILD UNDER MYSELF THE WORKER CAN'T BUILD A DOME
                if(isMyPos && whatIWantToBuildHere.contains(BuildingType.DOME))
                    return false;

                allBuildingsIWantToBuild.addAll(whatIWantToBuildHere);
            }

            if(notEnoughBuildings(model, allBuildingsIWantToBuild))
                return false;

            if(!simulate){

                buildingPos = builds.keySet().iterator();
                while(buildingPos.hasNext()){
                    Point whereIWantToBuild = buildingPos.next();
                    Cell cellWhereIWantToBuild = model.getBoard().getCell(whereIWantToBuild);
                    List<BuildingType> whatIWantToBuildHere = builds.get(whereIWantToBuild);
                    boolean isMyPos = whereIWantToBuild.equals(workerOnBuild.getPosition());
                    if(isMyPos)
                        cellWhereIWantToBuild.removeWorker();
                    for(BuildingType b : whatIWantToBuildHere){
                        if(!cellWhereIWantToBuild.addBuilding(b)){
                            System.err.println("The allow build under effect of worker " + buildData.getWorker().getID() + " encountered different things compared to the checked ones");
                            model.getBoard().getCell(workerOnBuild.getPosition()).setWorker(workerOnBuild.getID());
                            assert  false;
                        }
                    }
                    if(isMyPos)
                        cellWhereIWantToBuild.setWorker(workerOnBuild.getID());
                }

                Board board = model.getBoard();
                for(BuildingType b : allBuildingsIWantToBuild)
                    board.useBuilding(b);

                // Set next player state
                buildData.getPlayer().setPlayerState(nextPlayerState);
            }

            return true;

        };

    }

    private static void setPlayerFlags(Board board, MoveData moveData, Point startPosition){
        List<Integer> deltas = board.getMoveDeltas(moveData.getData(),startPosition);
        if (deltas.stream().max(Integer::compareTo).orElse(0) > 0) //If the player moved up at least once
            moveData.getPlayer().addFlag(PlayerFlag.MOVED_UP_ONCE);
    }

    private static boolean notEnoughBuildings(InternalModel model, List<BuildingType> allBuildingsIWantToBuild){

        long numOfFirstFloorsIWantToUse = allBuildingsIWantToBuild.stream()
                .filter((buildingType -> buildingType == BuildingType.FIRST_FLOOR))
                .count();
        long numOfSecondFloorsIWantToUse = allBuildingsIWantToBuild.stream()
                .filter((buildingType -> buildingType == BuildingType.SECOND_FLOOR))
                .count();
        long numOfThirdFloorsIWantToUse = allBuildingsIWantToBuild.stream()
                .filter((buildingType -> buildingType == BuildingType.THIRD_FLOOR))
                .count();
        long numOfDomesIWantToUse = allBuildingsIWantToBuild.stream()
                .filter((buildingType -> buildingType == BuildingType.DOME))
                .count();

        if(numOfFirstFloorsIWantToUse > model.getBoard().availableBuildings(BuildingType.FIRST_FLOOR))
            return true;
        if(numOfSecondFloorsIWantToUse > model.getBoard().availableBuildings(BuildingType.SECOND_FLOOR))
            return true;
        if(numOfThirdFloorsIWantToUse > model.getBoard().availableBuildings(BuildingType.THIRD_FLOOR))
            return true;
        return numOfDomesIWantToUse > model.getBoard().availableBuildings(BuildingType.DOME);
    }

}