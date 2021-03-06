package it.polimi.ingsw.server.model;

import it.polimi.ingsw.server.cards.CardFactory;
import it.polimi.ingsw.server.cards.CardFile;
import it.polimi.ingsw.server.cards.exceptions.InvalidCardException;
import it.polimi.ingsw.common.enums.BuildingType;
import it.polimi.ingsw.server.model.enums.LevelType;
import it.polimi.ingsw.server.model.enums.PlayerState;
import it.polimi.ingsw.server.model.exceptions.PlayerLostSignal;
import it.polimi.ingsw.server.model.exceptions.PlayerWonSignal;
import it.polimi.ingsw.common.packets.InvalidPacketException;
import it.polimi.ingsw.common.packets.PacketBuild;
import it.polimi.ingsw.common.packets.PacketMove;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InternalModelMirkoTest {

    private static CardFactory cardFactory;
    private InternalModel model;
    private Player Matteo;
    private Player Mirko;
    private Player Andrea;
    private Worker MatteoW1;
    private Worker MatteoW2;
    private Worker AndreaW1;
    private Worker AndreaW2;
    private Worker MirkoW1;
    private Worker MirkoW2;

    @BeforeAll
    static void init() throws InvalidCardException {
        //CardFactory
        cardFactory = CardFactory.getInstance();
    }

    @BeforeEach
    void createModel(){
        List<String> players = new ArrayList<>();
        players.add("Andrea");
        players.add("Matteo");
        players.add("Mirko");
        model = new InternalModel(players, cardFactory);
        Andrea = model.getPlayerByNick("Andrea");
        Matteo = model.getPlayerByNick("Matteo");
        Mirko = model.getPlayerByNick("Mirko");
        MatteoW1 = Matteo.getWorkers().get(0);
        MatteoW2 = Matteo.getWorkers().get(1);
        MirkoW1 = Mirko.getWorkers().get(0);
        MirkoW2 = Mirko.getWorkers().get(1);
        AndreaW1 = Andrea.getWorkers().get(0);
        AndreaW2 = Andrea.getWorkers().get(1);
    }

    /**
     * Test if after a player loses the CardStrategy is recompiled so that loser's deny effects are removed for the opponents.
     */
    @Test
    void testRecompiledCardStrategy(){
          /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | FF |M1  |    | M2 |    |
                +----+----+----+----+----+
            2   | A1 |    |    |    |    |
                | FF |    | B1 |FF  |    |
                +----+----+----+----+----+
            3   |    |    |    |  B2|    |
                +----+----+----+----+----+
            4   |    |    | A2 |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile athena = cardFactory.getCards().stream().filter(c->c.getName().equals("Athena")).findAny().orElse(null);
        CardFile hephaestus = cardFactory.getCards().stream().filter(c->c.getName().equals("Hephaestus")).findAny().orElse(null);
        CardFile hypnus = cardFactory.getCards().stream().filter(c->c.getName().equals("Hypnus")).findAny().orElse(null);

        Mirko.setCard(athena);
        Andrea.setCard(hephaestus);
        Matteo.setCard(hypnus);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startAthena = new Point(1,1);
        Point endAthena = new Point(0,1);
        Point startHypnus = new Point(2, 2);
        Point endHypnus = new Point(3, 2);
        Point start3rdPlayer = new Point(0,2);
        Point end3rdPlayer = new Point(1,2);

        board.getCell(endAthena).addBuilding(BuildingType.FIRST_FLOOR);
        board.getCell(endHypnus).addBuilding(BuildingType.FIRST_FLOOR);
        board.getCell(start3rdPlayer).addBuilding(BuildingType.FIRST_FLOOR);

        //SET ATHENA'S POSITIONS
        board.getCell(startAthena).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(startAthena);
        board.getCell(new Point(3, 1)).setWorker(MirkoW2.getID());
        MirkoW2.setPosition(new Point(3, 1));

        //SET HYPNUS' POSITIONS
        board.getCell(startHypnus).setWorker(MatteoW1.getID());
        MatteoW1.setPosition(startHypnus);
        board.getCell(new Point(3, 3)).setWorker(MatteoW2.getID());
        MatteoW2.setPosition(new Point(3, 3));

        //SET 3RD PLAYER'S POSITIONS
        board.getCell(start3rdPlayer).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(start3rdPlayer);
        board.getCell(new Point(2, 4)).setWorker(AndreaW2.getID());
        AndreaW2.setPosition(new Point(2, 4));

         /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   | M1 | <- |    |    |    |
                | FF |    |    | M2 |    |
                +----+----+----+----+----+
            2   | A1 |    |    |    |    |
                | FF |    | B1 |FF  |    |
                +----+----+----+----+----+
            3   |    |    |    |  B2|    |
                +----+----+----+----+----+
            4   |    |    | A2 |    |    |
                +----+----+----+----+----+
            Y
        */

        //FIRST ATHENA MOVES UP
        List<Point> points = new LinkedList<>();
        points.add(endAthena);

        PacketMove packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(Mirko.getState(),PlayerState.MOVED);
        assertEquals(MirkoW1.getPosition(), endAthena);
        assertEquals(board.getCell(endAthena).getWorkerID(), MirkoW1.getID());

          /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   | M1 |    |    |    |    |
                | FF |    |    | M2 |    |
                +----+----+----+----+----+
            2   | A1 |    | -> |B1  |    |
                | FF |    |    |FF  |    |
                +----+----+----+----+----+
            3   |    |    |    |  B2|    |
                +----+----+----+----+----+
            4   |    |    | A2 |    |    |
                +----+----+----+----+----+
            Y
        */

        //HYPNUS LOSES BECAUSE HE MOVES UP

        points = new LinkedList<>();
        points.add(endHypnus);

        packetMove = new PacketMove(Matteo.getNickname(),MatteoW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertFalse(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert true;
        }

        model.addLoser(Matteo);
        assertTrue(model.getLosers().contains(Matteo));

          /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   | M1 |    |    |    |    |
                | FF |    |    | M2 |    |
                +----+----+----+----+----+
            2   | -> |    |    |B1  |    |
                | FF |A1  |    |FF  |    |
                +----+----+----+----+----+
            3   |    |    |    |  B2|    |
                +----+----+----+----+----+
            4   |    |    | A2 |    |    |
                +----+----+----+----+----+
            Y
        */

        //3RD PLAYER MOVES HIS HIGHEST WORKER, SINCE HYPNUS HAS LOST HE SHOULD BE ABLE TO COMPLETE HIS ACTION

        points = new LinkedList<>();
        points.add(end3rdPlayer);

        packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(Andrea.getState(),PlayerState.MOVED);
        assertEquals(AndreaW1.getPosition(), end3rdPlayer);
        assertEquals(board.getCell(end3rdPlayer).getWorkerID(), AndreaW1.getID());


    }

    /**
     * Test if Aphrodite correctly forbids possible positions to opponents and makes them lose.
     */
    @Test
    void testCompiledCardStrategyAphrodite1(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | X  |M1  |    | M2 |    |
                +----+----+----+----+----+
            2   | A1 |    |    |    |    |
                |    | X  |    |    |    |
                +----+----+----+----+----+
            3   | F  | F  |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile aphrodite = cardFactory.getCards().stream().filter(c->c.getName().equals("Aphrodite")).findAny().orElse(null);
        CardFile hephaestus = cardFactory.getCards().stream().filter(c->c.getName().equals("Hephaestus")).findAny().orElse(null);

        Mirko.setCard(aphrodite);
        Andrea.setCard(hephaestus);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point forbiddenCell = new Point(0,3);
        Point aphroditeCell = new Point(1,1);

        board.getCell(startCell).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(startCell);
        board.getCell(aphroditeCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(aphroditeCell);
        board.getCell(new Point(3,1)).setWorker(MirkoW2.getID());
        MirkoW2.setPosition(new Point(3,1));

        List<Point> points = new LinkedList<>();
        points.add(forbiddenCell);

        PacketMove packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertFalse(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert true;
        }

        assertEquals(Andrea.getState(),PlayerState.TURN_STARTED);
        assertEquals(AndreaW1.getPosition(), startCell);
        assertEquals(board.getCell(startCell).getWorkerID(),AndreaW1.getID());

    }

    /**
     * Test if Hypnus correctly forbids possible positions to the highest opponents' worker and makes him lose.
     */
    @Test
    void testCompiledCardStrategyHypnus1(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |M1FF|    | M2 |    |
                +----+----+----+----+----+
            2   | A1 | x1 |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    | x2 |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile artemis = cardFactory.getCards().stream().filter(c->c.getName().equals("Artemis")).findAny().orElse(null);
        CardFile hypnus = cardFactory.getCards().stream().filter(c->c.getName().equals("Hypnus")).findAny().orElse(null);

        Mirko.setCard(artemis);
        Andrea.setCard(hypnus);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(1,1);
        Point firstMoveCell = new Point(1,2);
        Point secondMoveCell = new Point(1,3);
        Point hypnusCell = new Point(0,2);

        board.getCell(hypnusCell).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(hypnusCell);

        board.getCell(startCell).addBuilding(BuildingType.FIRST_FLOOR);
        board.getCell(startCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(startCell);

        board.getCell(new Point(3,1)).setWorker(MirkoW2.getID());
        MirkoW2.setPosition(new Point(3,1));

        List<Point> points = new LinkedList<>();
        points.add(firstMoveCell);
        points.add(secondMoveCell);

        PacketMove packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertFalse(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert true;
        }

        assertEquals(Mirko.getState(),PlayerState.TURN_STARTED);
        assertEquals(MirkoW1.getPosition(), startCell);
        assertEquals(board.getCell(startCell).getWorkerID(),MirkoW1.getID());

    }

    /**
     * Test if Hypnus correctly doesnt' forbid possible positions to the opponents' worker.
     */
    @Test
    void testCompiledCardStrategyHypnus2(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    | M1 |    | M2 |    |
                +----+----+----+----+----+
            2   | A1 | x1 |    |    |    |
                |    | FF |    |    |    |
                +----+----+----+----+----+
            3   |    | x2 |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile artemis = cardFactory.getCards().stream().filter(c->c.getName().equals("Artemis")).findAny().orElse(null);
        CardFile hypnus = cardFactory.getCards().stream().filter(c->c.getName().equals("Hypnus")).findAny().orElse(null);

        Mirko.setCard(artemis);
        Andrea.setCard(hypnus);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(1,1);
        Point firstMoveCell = new Point(1,2);
        Point secondMoveCell = new Point(1,3);
        Point hypnusCell = new Point(0,2);

        board.getCell(hypnusCell).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(hypnusCell);

        board.getCell(startCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(startCell);

        board.getCell(new Point(3,1)).setWorker(MirkoW2.getID());
        MirkoW2.setPosition(new Point(3,1));

        board.getCell(firstMoveCell).addBuilding(BuildingType.FIRST_FLOOR);

        List<Point> points = new LinkedList<>();
        points.add(firstMoveCell);
        points.add(secondMoveCell);

        PacketMove packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(Mirko.getState(),PlayerState.MOVED);
        assertEquals(MirkoW1.getPosition(), secondMoveCell);
        assertEquals(board.getCell(secondMoveCell).getWorkerID(),MirkoW1.getID());

    }

    /**
     * Hestia tries to build an additional block on the perimeter space, since the mode is set to hardcore she should lose.
     */
    @Test
    void testCompiledCardStrategyHestia1(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | X  |    |    |    |    |
                +----+----+----+----+----+
            2   | A1 |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile hestia = cardFactory.getCards().stream().filter(c->c.getName().equals("Hestia")).findAny().orElse(null);

        Andrea.setCard(hestia);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell = new Point(0,1);

        board.getCell(startCell).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(startCell);

        List<Point> points = new LinkedList<>();
        points.add(endCell);

        PacketMove packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(Andrea.getState(),PlayerState.MOVED);
        assertEquals(AndreaW1.getPosition(), endCell);
        assertEquals(board.getCell(endCell).getWorkerID(),AndreaW1.getID());

         /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   | X  |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | A1 | X  |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //NOW ANDREA'S W1 TRIES TO BUILD A FIRST FLOOR ON 1,1 AND A FIRST FLOOR ON 0,0 WHICH IS ON THE PERIMETER -> HE LOSES

        Point buildCell = new Point(1,1);
        Point perimeterBuildCell = new Point(0,0);

        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<BuildingType> buildingTypes = new LinkedList<>();
        List<Point> dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.FIRST_FLOOR);
        dataOrder.add(buildCell);
        builds.put(buildCell,buildingTypes);
        dataOrder.add(perimeterBuildCell);
        builds.put(perimeterBuildCell, buildingTypes);

        PacketBuild packetBuild = new PacketBuild(Andrea.getNickname(),AndreaW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertFalse(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert true;
        }

        assertEquals(board.getCell(buildCell).getTopBuilding(), LevelType.GROUND);
        assertEquals(Andrea.getState(),PlayerState.MOVED);

    }

    /**
     * Hestia tries to build an additional block not on the perimeter space and in the same space of the first build, this action should be allowed.
     */
    @Test
    void testCompiledCardStrategyHestia2(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | X  |    |    |    |    |
                +----+----+----+----+----+
            2   | A1 |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile hestia = cardFactory.getCards().stream().filter(c->c.getName().equals("Hestia")).findAny().orElse(null);

        Andrea.setCard(hestia);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell = new Point(0,1);

        board.getCell(startCell).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(startCell);

        List<Point> points = new LinkedList<>();
        points.add(endCell);

        PacketMove packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(Andrea.getState(),PlayerState.MOVED);
        assertEquals(AndreaW1.getPosition(), endCell);
        assertEquals(board.getCell(endCell).getWorkerID(),AndreaW1.getID());

         /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | A1 | X  |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //NOW ANDREA'S W1 TRIES TO BUILD A FIRST FLOOR AND A SECOND FLOOR ON 1,1

        Point buildCell = new Point(1,1);

        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<BuildingType> buildingTypes = new LinkedList<>();
        List<Point> dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.FIRST_FLOOR);
        buildingTypes.add(BuildingType.SECOND_FLOOR);
        dataOrder.add(buildCell);
        dataOrder.add(buildCell);
        builds.put(buildCell,buildingTypes);

        PacketBuild packetBuild = new PacketBuild(Andrea.getNickname(),AndreaW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertTrue(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(board.getCell(buildCell).getTopBuilding(), LevelType.SECOND_FLOOR);
        assertEquals(Andrea.getState(),PlayerState.BUILT);

    }

    /**
     * Hestia tries to build an additional block NOT on the perimeter space and NOT in the same space of the first build, this action should be allowed.
     */
    @Test
    void testCompiledCardStrategyHestia3(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | X  |    |    |    |    |
                +----+----+----+----+----+
            2   | A1 |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile hestia = cardFactory.getCards().stream().filter(c->c.getName().equals("Hestia")).findAny().orElse(null);

        Andrea.setCard(hestia);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell = new Point(0,1);

        board.getCell(startCell).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(startCell);

        List<Point> points = new LinkedList<>();
        points.add(endCell);

        PacketMove packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(Andrea.getState(),PlayerState.MOVED);
        assertEquals(AndreaW1.getPosition(), endCell);
        assertEquals(board.getCell(endCell).getWorkerID(),AndreaW1.getID());

         /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | A1 | X1 |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                |    | X2 |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //NOW ANDREA'S W1 TRIES TO BUILD A FIRST FLOOR ON 1,1 AND A FIRST FLOOR ON 1,2

        Point firstBuildCell = new Point(1,1);
        Point secondBuildCell = new Point(1,2);

        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<BuildingType> buildingTypes = new LinkedList<>();
        List<Point> dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.FIRST_FLOOR);
        dataOrder.add(firstBuildCell);
        builds.put(firstBuildCell,buildingTypes);
        dataOrder.add(secondBuildCell);
        builds.put(secondBuildCell,buildingTypes);

        PacketBuild packetBuild = new PacketBuild(Andrea.getNickname(),AndreaW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertTrue(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(board.getCell(firstBuildCell).getTopBuilding(), LevelType.FIRST_FLOOR);
        assertEquals(board.getCell(secondBuildCell).getTopBuilding(), LevelType.FIRST_FLOOR);
        assertEquals(Andrea.getState(),PlayerState.BUILT);

    }

    /**
     * Atlas tries to build a dome (it doesn't complete a tower) near Limus' worker.
     * Since this action is a deny and the mode is set to hardcore Atlas should lose.
     */
    @Test
    void testCompiledCardStrategyLimus1(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | X  |M1  |    | M2 |    |
                +----+----+----+----+----+
            2   | A1 |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile limus = cardFactory.getCards().stream().filter(c->c.getName().equals("Limus")).findAny().orElse(null);
        CardFile atlas = cardFactory.getCards().stream().filter(c->c.getName().equals("Atlas")).findAny().orElse(null);

        Mirko.setCard(limus);
        Andrea.setCard(atlas);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell = new Point(0,1);
        Point buildCell = new Point(1,2);
        Point limusCell = new Point(1, 1);

        board.getCell(startCell).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(startCell);
        board.getCell(limusCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(limusCell);
        board.getCell(new Point(3,1)).setWorker(MirkoW2.getID());
        MirkoW2.setPosition(new Point(3,1));

        List<Point> points = new LinkedList<>();
        points.add(endCell);

        PacketMove packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(Andrea.getState(),PlayerState.MOVED);
        assertEquals(AndreaW1.getPosition(), endCell);
        assertEquals(board.getCell(endCell).getWorkerID(),AndreaW1.getID());

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |A1  |M1  |    | M2 |    |
                +----+----+----+----+----+
            2   |    | X  |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //NOW ANDREA'S W1 TRIES TO BUILD A DOME NEAR MIRKO'S WORKER
        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<BuildingType> buildingTypes = new LinkedList<>();
        List<Point> dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.DOME);
        dataOrder.add(buildCell);
        builds.put(buildCell,buildingTypes);

        PacketBuild packetBuild = new PacketBuild(Andrea.getNickname(),AndreaW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertFalse(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert true;
        }

        assertEquals(board.getCell(buildCell).getTopBuilding(), LevelType.GROUND);
        assertEquals(Andrea.getState(),PlayerState.MOVED);

    }

    /**
     * Atlas tries to build a dome (it completes a tower) near Limus' worker. This action should be allowed.
     */
    @Test
    void testCompiledCardStrategyLimus2(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |M1  |    | M2 |    |
                +----+----+----+----+----+
            2   | A1 | TF |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile limus = cardFactory.getCards().stream().filter(c->c.getName().equals("Limus")).findAny().orElse(null);
        CardFile atlas = cardFactory.getCards().stream().filter(c->c.getName().equals("Atlas")).findAny().orElse(null);

        Mirko.setCard(limus);
        Andrea.setCard(atlas);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell = new Point(0,1);
        Point buildCell = new Point(1,2);
        Point limusCell = new Point(1, 1);

        model.getBoard().getCell(buildCell).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(buildCell).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(buildCell).addBuilding(BuildingType.THIRD_FLOOR);

        board.getCell(startCell).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(startCell);
        board.getCell(limusCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(limusCell);
        board.getCell(new Point(3,1)).setWorker(MirkoW2.getID());
        MirkoW2.setPosition(new Point(3,1));

        List<Point> points = new LinkedList<>();
        points.add(endCell);

        PacketMove packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(), false, points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(Andrea.getState(),PlayerState.MOVED);
        assertEquals(AndreaW1.getPosition(), endCell);
        assertEquals(board.getCell(endCell).getWorkerID(),AndreaW1.getID());

        //NOW ANDREA'S W1 TRIES TO BUILD A DOME NEAR MIRKO'S WORKER
        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<BuildingType> buildingTypes = new LinkedList<>();
        List<Point> dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.DOME);
        dataOrder.add(buildCell);
        builds.put(buildCell,buildingTypes);

        PacketBuild packetBuild = new PacketBuild(Andrea.getNickname(),AndreaW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertTrue(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(board.getCell(buildCell).getTopBuilding(), LevelType.DOME);
        assertEquals(Andrea.getState(),PlayerState.BUILT);

    }

    /**
     * Test if Zeus correctly can build a block under itself.
     * Test if Zeus can't build a Dome under itself.
     */
    @Test
    void testCompiledCardStrategyZeus1(){

        /*

                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   | M1 |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   | m  |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */


        CardFile zeus = cardFactory.getCards().stream().filter(c->c.getName().equals("Zeus")).findAny().orElse(null);

        Mirko.setCard(zeus);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell = new Point(0,3);

        board.getCell(startCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(startCell);

        List<Point> points = new LinkedList<>();
        points.add(endCell);

        PacketMove packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(), points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(MirkoW1.getPosition(),endCell);
        assertEquals(board.getCell(endCell).getWorkerID(),MirkoW1.getID());
        assertEquals(Mirko.getState(),PlayerState.MOVED);

        /*

                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |M1FF|    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */


        Point buildPoint = new Point(0,3);

        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<BuildingType> buildingTypes = new LinkedList<>();
        List<Point> dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.FIRST_FLOOR);
        dataOrder.add(buildPoint);
        builds.put(buildPoint,buildingTypes);

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertTrue(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(board.getCell(buildPoint).getTopBuilding(), LevelType.FIRST_FLOOR);
        assertEquals(PlayerState.BUILT,Mirko.getState());

        Mirko.setPlayerState(PlayerState.MOVED);

        /*

                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |M1SF|    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        builds = new HashMap<>();
        buildingTypes = new LinkedList<>();
        dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.SECOND_FLOOR);
        dataOrder.add(buildPoint);
        builds.put(buildPoint,buildingTypes);

        packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertTrue(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(board.getCell(buildPoint).getTopBuilding(), LevelType.SECOND_FLOOR);
        assertEquals(PlayerState.BUILT,Mirko.getState());

        Mirko.setPlayerState(PlayerState.MOVED);

        /*

                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |M1TF|    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        builds = new HashMap<>();
        buildingTypes = new LinkedList<>();
        dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.THIRD_FLOOR);
        dataOrder.add(buildPoint);
        builds.put(buildPoint,buildingTypes);

        packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertTrue(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(board.getCell(buildPoint).getTopBuilding(), LevelType.THIRD_FLOOR);
        assertEquals(PlayerState.BUILT,Mirko.getState());

        Mirko.setPlayerState(PlayerState.MOVED);

        /*

                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |M1 D|    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        builds = new HashMap<>();
        buildingTypes = new LinkedList<>();
        dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.DOME);
        dataOrder.add(buildPoint);
        builds.put(buildPoint,buildingTypes);

        packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertFalse(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert true;
        }

        assertEquals(board.getCell(buildPoint).getTopBuilding(), LevelType.THIRD_FLOOR);
        assertEquals(PlayerState.MOVED,Mirko.getState());

    }


    /**
     * Test if Hephaestus can correctly build FIRST_FLOOR and SECOND_FLOOR at the same time but on the same spot. (b1)
     * Test if Hephaestus cannot build on different positions (b2 and b3)
     */
    @Test
    void testCompiledCardStrategyHephaestus1(){

        /*

                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   | M1 |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   | m  | b1 |    |    |    |
                +----+----+----+----+----+
            4   | b2 | b3 |    |    |    |
                +----+----+----+----+----+
            Y
        */


        CardFile hephaestus = cardFactory.getCards().stream().filter(c->c.getName().equals("Hephaestus")).findAny().orElse(null);

        Mirko.setCard(hephaestus);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell = new Point(0,3);

        board.getCell(startCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(startCell);

        List<Point> points = new LinkedList<>();
        points.add(endCell);

        PacketMove packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(), points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(MirkoW1.getPosition(),endCell);
        assertEquals(board.getCell(endCell).getWorkerID(),MirkoW1.getID());
        assertEquals(Mirko.getState(),PlayerState.MOVED);

        Point buildPoint = new Point(1,3);

        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<BuildingType> buildingTypes = new LinkedList<>();
        List<Point> dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.FIRST_FLOOR);
        dataOrder.add(buildPoint);
        buildingTypes.add(BuildingType.SECOND_FLOOR);
        dataOrder.add(buildPoint);
        builds.put(buildPoint,buildingTypes);

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertTrue(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(board.getCell(buildPoint).getTopBuilding(), LevelType.SECOND_FLOOR);
        assertEquals(PlayerState.BUILT,Mirko.getState());

        Mirko.setPlayerState(PlayerState.MOVED);
        assertEquals(PlayerState.MOVED,Mirko.getState());

        //SECOND CASE
        Point buildPoint1 = new Point(0,4);
        Point buildPoint2 = new Point(1,4);
        builds = new HashMap<>();
        dataOrder = new ArrayList<>();
        buildingTypes = new LinkedList<>();
        buildingTypes.add(BuildingType.FIRST_FLOOR);
        dataOrder.add(buildPoint1);
        builds.put(buildPoint1,buildingTypes);
        buildingTypes = new LinkedList<>();
        buildingTypes.add(BuildingType.SECOND_FLOOR);
        dataOrder.add(buildPoint2);
        builds.put(buildPoint2,buildingTypes);


        packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertFalse(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(board.getCell(buildPoint1).getTopBuilding(), LevelType.GROUND);
        assertEquals(board.getCell(buildPoint2).getTopBuilding(), LevelType.GROUND);

    }

    /**
     * Test if Hephaestus cannot build two blocks on the same spot because one of them is a Dome.
     */
    @Test
    void testCompiledCardStrategyHephaestus2(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   | M1 |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   | m  | b  |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile hephaestus = cardFactory.getCards().stream().filter(c->c.getName().equals("Hephaestus")).findAny().orElse(null);

        Mirko.setCard(hephaestus);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell = new Point(0,3);

        board.getCell(startCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(startCell);

        List<Point> points = new LinkedList<>();
        points.add(endCell);

        PacketMove packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(), points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(Mirko.getState(),PlayerState.MOVED);
        assertEquals(MirkoW1.getPosition(),endCell);
        assertEquals(board.getCell(endCell).getWorkerID(),MirkoW1.getID());



        Point buildPoint = new Point(1,3);

        board.getCell(buildPoint).addBuilding(BuildingType.FIRST_FLOOR);
        board.getCell(buildPoint).addBuilding(BuildingType.SECOND_FLOOR);

        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<BuildingType> buildingTypes = new LinkedList<>();
        List<Point> dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.THIRD_FLOOR);
        dataOrder.add(buildPoint);
        buildingTypes.add(BuildingType.DOME);
        dataOrder.add(buildPoint);
        builds.put(buildPoint,buildingTypes);

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertFalse(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(Mirko.getState(),PlayerState.MOVED);
        assertEquals(board.getCell(buildPoint).getTopBuilding(), LevelType.SECOND_FLOOR);

    }

    /**
     * Test if Hephaestus cannot move because he is blocked by the other Workers and a Dome.
     */
    @Test
    void testCompiledCardStrategyHephaestus3(){

        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | A2 | D  |    |    |    |
                +----+----+----+----+----+
            2   | M1 |  A1|    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   | M2 | M1 |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile hephaestus = cardFactory.getCards().stream().filter(c->c.getName().equals("Hephaestus")).findAny().orElse(null);

        Mirko.setCard(hephaestus);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell1 = new Point(0,3);
        Point endCell2 = new Point(1,1);
        Point andreaW1Pos = new Point(1,2);
        Point andreaW2Pos = new Point(0,1);
        Point matteoW1Pos = new Point(1,3);
        Point matteoW2Pos = new Point(0,3);

        board.getCell(startCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(startCell);

        board.getCell(andreaW1Pos).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(andreaW1Pos);
        board.getCell(andreaW2Pos).setWorker(AndreaW2.getID());
        AndreaW2.setPosition(andreaW2Pos);

        board.getCell(matteoW1Pos).setWorker(MatteoW1.getID());
        MatteoW1.setPosition(matteoW1Pos);
        board.getCell(matteoW2Pos).setWorker(MatteoW2.getID());
        MatteoW2.setPosition(matteoW2Pos);

        board.getCell(endCell2).addBuilding(BuildingType.FIRST_FLOOR);
        board.getCell(endCell2).addBuilding(BuildingType.SECOND_FLOOR);
        board.getCell(endCell2).addBuilding(BuildingType.THIRD_FLOOR);
        board.getCell(endCell2).addBuilding(BuildingType.DOME);

        List<Point> points = new LinkedList<>();
        points.add(endCell1);

        PacketMove packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(), points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertFalse(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(MirkoW1.getPosition(),startCell);
        assertEquals(PlayerState.TURN_STARTED, Mirko.getState());

        points = new LinkedList<>();
        points.add(endCell2);

        packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(), points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertFalse(model.makeMove(moveData));
            assert false;
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert true;
        }

        assertEquals(MirkoW1.getPosition(),startCell);
        assertEquals(PlayerState.TURN_STARTED, Mirko.getState());


    }

    /**
     * Test if Hephaestus cannot build where he moved and cannot build onto another Worker.
     */
    @Test
    void testCompiledCardStrategyHephaestus4(){

         /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                | A2 | D  |    |    |    |
                +----+----+----+----+----+
            2   | M1 |  A1|    |    |    |
                |    |  FF|    |    |    |
                +----+----+----+----+----+
            3   | M2 |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        CardFile hephaestus = cardFactory.getCards().stream().filter(c->c.getName().equals("Hephaestus")).findAny().orElse(null);

        Mirko.setCard(hephaestus);
        model.compileCardStrategy();

        Board board = model.getBoard();

        Point startCell = new Point(0,2);
        Point endCell = new Point(1,3);
        Point andreaW1Pos = new Point(1,2);
        Point andreaW2Pos = new Point(0,1);
        Point matteoW2Pos = new Point(0,3);

        board.getCell(startCell).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(startCell);

        board.getCell(andreaW1Pos).addBuilding(BuildingType.FIRST_FLOOR);
        board.getCell(andreaW1Pos).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(andreaW1Pos);
        board.getCell(andreaW2Pos).setWorker(AndreaW2.getID());
        AndreaW2.setPosition(andreaW2Pos);

        board.getCell(matteoW2Pos).setWorker(MatteoW2.getID());
        MatteoW2.setPosition(matteoW2Pos);

        List<Point> points = new LinkedList<>();
        points.add(endCell);

        PacketMove packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(), points);

        assertNotNull(packetMove);

        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal | InvalidPacketException e) {
            assert false;
        }

        assertEquals(MirkoW1.getPosition(),endCell);
        assertEquals(board.getCell(endCell).getWorkerID(),MirkoW1.getID());

        assertEquals(Mirko.getState(),PlayerState.MOVED);

        Point buildPoint = new Point(1,3);

        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<BuildingType> buildingTypes = new LinkedList<>();
        List<Point> dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.FIRST_FLOOR);
        dataOrder.add(buildPoint);
        buildingTypes.add(BuildingType.SECOND_FLOOR);
        dataOrder.add(buildPoint);
        builds.put(buildPoint,buildingTypes);

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertFalse(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(board.getCell(buildPoint).getTopBuilding(), LevelType.GROUND);

        assertEquals(Mirko.getState(),PlayerState.MOVED);

        buildPoint = new Point(1,2);

        builds = new HashMap<>();
        buildingTypes = new LinkedList<>();
        dataOrder = new ArrayList<>();
        buildingTypes.add(BuildingType.FIRST_FLOOR);
        dataOrder.add(buildPoint);
        buildingTypes.add(BuildingType.SECOND_FLOOR);
        dataOrder.add(buildPoint);
        builds.put(buildPoint,buildingTypes);

        packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,dataOrder);

        assertNotNull(packetBuild);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assertFalse(model.makeBuild(buildData));
        } catch (InvalidPacketException | PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }

        assertEquals(Mirko.getState(),PlayerState.MOVED);
        assertEquals(board.getCell(buildPoint).getTopBuilding(), LevelType.FIRST_FLOOR);

    }

    @Test
    void apollo_atlas_prometheus(){
         /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    | x  |x   | x  |    |
                +----+----+----+----+----+
            2   |    |  x |D1  | x  |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |  x | x  |  M1|    |
                |    |    |    | x  |    |
                +----+----+----+----+----+
            4   |    |    |    | A1 |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */
        // Initializing cards
        CardFile prometheus = cardFactory.getCards().stream().filter(x -> x.getName().equals("Prometheus")).findFirst().orElse(null);
        CardFile atlas = cardFactory.getCards().stream().filter(x -> x.getName().equals("Atlas")).findFirst().orElse(null);
        CardFile apollo = cardFactory.getCards().stream().filter(x -> x.getName().equals("Apollo")).findFirst().orElse(null);

        Matteo.setCard(prometheus); //M1
        Andrea.setCard(atlas); //A1
        Mirko.setCard(apollo); //D1
        model.compileCardStrategy();

        //Initializing positions pointXY

        Point point22 = new Point(2,2);
        Point point33 = new Point(3,3);
        Point point34 = new Point(3,4);
        Point point32 = new Point(3,2);
        Point point43 = new Point(4,3);
        Point point42 = new Point(4,2);
        Point point44 = new Point(4,4);
        Point point31 = new Point(3,1);
        Point point21 = new Point(2,1);
        Point point23 = new Point(2,3);

        model.getBoard().getCell(point22).setWorker(MirkoW1.getID());
        MirkoW1.setPosition(point22);

        model.getBoard().getCell(point33).setWorker(MatteoW1.getID());
        MatteoW1.setPosition(point33);

        model.getBoard().getCell(point34).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(point34);

        //MOVE UTILS
        List<Point> moves = new ArrayList<>();
        PacketMove packetMove;
        MoveData moveData;
        Set<Point> possibleMoves = new HashSet<>();

        //BUILDS UTILS
        List<BuildingType> buildings = new ArrayList<>();
        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<Point> buildsOrder = new ArrayList<>();
        PacketBuild packetBuild;
        BuildData buildData;
        Set<Point> possibleBuilds = new HashSet<>();

        //FIRST MOVE MIRKO AS APOLLO SWAPS HIMSELF WITH MATTEO
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    | x  | x  | x  |    |
                +----+----+----+----+----+
            2   |    |    |M1  |    |    |
                |    | x  |  ↘ | x  |    |
                +----+----+----+----+----+
            3   |    | x  |  x |  D1|    |
                |    |    |    |  x |    |
                +----+----+----+----+----+
            4   |    |    |    | A1 |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */
        possibleMoves.add(point32);
        possibleMoves.add(point33);
        possibleMoves.add(point21);
        possibleMoves.add(new Point(1,1));
        possibleMoves.add(point31);
        possibleMoves.add(new Point(1,2));
        possibleMoves.add(new Point(1,3));
        possibleMoves.add(point23);

        assertEquals(possibleMoves, model.getPossibleMoves(Mirko, MirkoW1));

        moves.add(point33);
        packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Mirko.getState());
        assertEquals(point33, MirkoW1.getPosition());
        assertEquals(MirkoW1.getID(), model.getBoard().getCell(point33).getWorkerID());

        assertEquals(PlayerState.TURN_STARTED, Matteo.getState());
        assertEquals(point22, MatteoW1.getPosition());
        assertEquals(MatteoW1.getID(), model.getBoard().getCell(point22).getWorkerID());

        //MIRKO'S WORKER BUILDS IN POSITION 32
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |M1  | FF |    |
                |    |    |    | x  | x  |
                +----+----+----+----+----+
            3   |    |    | x  |  D1| x  |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    | A1 | x  |
                |    |    | x  |    |    |
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(point32);
        possibleBuilds.add(point42);
        possibleBuilds.add(new Point(4,3));
        possibleBuilds.add(point44);
        possibleBuilds.add(new Point(2,4));
        possibleBuilds.add(point23);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Mirko, MirkoW1).keySet());

        buildings.add(BuildingType.FIRST_FLOOR);
        builds.put(point32,buildings);
        buildsOrder.add(point32);

        packetBuild = new PacketBuild(Mirko.getNickname(), MirkoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point32).getTopBuilding(), LevelType.FIRST_FLOOR);
        assertEquals(PlayerState.BUILT, Mirko.getState());

        //MATTEO'S WORKER MOVES IN POSITION 32
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    | x  | x  | x  |    |
                +----+----+----+----+----+
            2   |    |    |    | FF |    |
                |    |x   | -> | xM1|    |
                +----+----+----+----+----+
            3   |    | x  | x  |  D1|    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    | A1 |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(point32);
        possibleMoves.add(point31);
        possibleMoves.add(point21);
        possibleMoves.add(new Point(1,1));
        possibleMoves.add(new Point(1,2));
        possibleMoves.add(new Point(1,3));
        possibleMoves.add(point23);

        assertEquals(possibleMoves, model.getPossibleMoves(Matteo, MatteoW1));

        moves.add(point32);
        packetMove = new PacketMove(Matteo.getNickname(),MatteoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Matteo.getState());
        assertEquals(point32, MatteoW1.getPosition());
        assertEquals(MatteoW1.getID(), model.getBoard().getCell(point32).getWorkerID());

        //MATTEO'S WORKER BUILDS IN POSITION 22
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    | x  | x  | x  |
                +----+----+----+----+----+
            2   |    |    | x  | FF | x  |
                |    |    | FF | M1 |    |
                +----+----+----+----+----+
            3   |    |    | x  |  D1| x  |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    | A1 |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(point22);
        possibleBuilds.add(point21);
        possibleBuilds.add(point31);
        possibleBuilds.add(new Point(4,1));
        possibleBuilds.add(point42);
        possibleBuilds.add(new Point(4,3));
        possibleBuilds.add(point23);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Matteo, MatteoW1).keySet());

        buildings.add(BuildingType.FIRST_FLOOR);
        builds.put(point22,buildings);
        buildsOrder.add(point22);

        packetBuild = new PacketBuild(Matteo.getNickname(), MatteoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point22).getTopBuilding(), LevelType.FIRST_FLOOR);
        assertEquals(PlayerState.BUILT, Matteo.getState());


        //ANDREA'S WORKER MOVES IN POSITION 43
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    | FF |    |
                |    |    | FF | M1 |    |
                +----+----+----+----+----+
            3   |    |    | x  |  D1| A1 |
                |    |    |    |    | x  |
                +----+----+----+----+----+
            4   |    |    | x  | ↗  | x  |
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(point43);
        possibleMoves.add(point23);
        possibleMoves.add(new Point(2,4));
        possibleMoves.add(point44);

        assertEquals(possibleMoves,model.getPossibleMoves(Andrea, AndreaW1));

        moves.add(point43);
        packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Andrea.getState());
        assertEquals(point43, AndreaW1.getPosition());
        assertEquals(AndreaW1.getID(), model.getBoard().getCell(point43).getWorkerID());

        //ANDREA'S WORKER BUILDS A DOME IN POSITION 42
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    | FF | C  |
                |    |    | FF | M1 | x  |
                +----+----+----+----+----+
            3   |    |    |    |  D1| A1 |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |  x |  x |
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(point42);
        possibleBuilds.add(point44);
        possibleBuilds.add(point34);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Andrea, AndreaW1).keySet());

        buildings.add(BuildingType.DOME);
        builds.put(point42,buildings);
        buildsOrder.add(point42);

        packetBuild = new PacketBuild(Andrea.getNickname(), AndreaW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point42).getTopBuilding(), LevelType.DOME);
        assertEquals(PlayerState.BUILT, Andrea.getState());

        ///////////////////FIRST TURN ENDED///////////////////////////////////

        Mirko.setPlayerState(PlayerState.TURN_STARTED);
        Matteo.setPlayerState(PlayerState.TURN_STARTED);
        Andrea.setPlayerState(PlayerState.TURN_STARTED);

        //MIRKO'S WORKER MOVES IN POSITION 43 AND SWAPS HIMSELF WITH ANDREA'S WORKER
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    | x  | FF | C  |
                |    |    | FF | xM1|    |
                +----+----+----+----+----+
            3   |    |    | x  |  A1| D1 |
                |    |    |    |  ->| x  |
                +----+----+----+----+----+
            4   |    |    | x  |  x |  x |
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(point43);
        possibleMoves.add(point44);
        possibleMoves.add(point34);
        possibleMoves.add(new Point(2,4));
        possibleMoves.add(point23);
        possibleMoves.add(point22);
        possibleMoves.add(point32);

        assertEquals(possibleMoves,model.getPossibleMoves(Mirko, MirkoW1));

        moves.add(point43);
        packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Mirko.getState());
        assertEquals(point43, MirkoW1.getPosition());
        assertEquals(MirkoW1.getID(), model.getBoard().getCell(point43).getWorkerID());

        assertEquals(PlayerState.TURN_STARTED, Andrea.getState());
        assertEquals(point33, AndreaW1.getPosition());
        assertEquals(AndreaW1.getID(), model.getBoard().getCell(point33).getWorkerID());

        //MIRKO'S WORKER BUILDS IN POSITION 43
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    | FF | C  |
                |    |    | FF |  M1|    |
                +----+----+----+----+----+
            3   |    |    |    |  A1| D1 |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |  x |  FF|
                |    |    |    |    |   x|
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(point44);
        possibleBuilds.add(point34);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Mirko, MirkoW1).keySet());

        buildings.add(BuildingType.FIRST_FLOOR);
        builds.put(point44,buildings);
        buildsOrder.add(point44);

        packetBuild = new PacketBuild(Mirko.getNickname(), MirkoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point44).getTopBuilding(), LevelType.FIRST_FLOOR);
        assertEquals(PlayerState.BUILT, Mirko.getState());

        //MATTEO'S WORKER BUILDS A SECOND FLOOR IN POSITION 22
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    | x  | x  | x  |
                +----+----+----+----+----+
            2   |    |    | X  | FF | C  |
                |    |    | SF |  M1|    |
                +----+----+----+----+----+
            3   |    |    | x  |  A1| D1 |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleBuilds.add(point22);
        possibleBuilds.add(point21);
        possibleBuilds.add(point31);
        possibleBuilds.add(new Point(4,1));
        possibleBuilds.add(point23);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Matteo, MatteoW1).keySet());

        buildings.add(BuildingType.SECOND_FLOOR);
        builds.put(point22,buildings);
        buildsOrder.add(point22);

        packetBuild = new PacketBuild(Matteo.getNickname(), MatteoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point22).getTopBuilding(), LevelType.SECOND_FLOOR);
        assertEquals(PlayerState.FIRST_BUILT, Matteo.getState());

        //MATTEO'S TRIES TO MOVE IN POSITION 22 BUT HE CAN'T
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    | FF | C  |
                |    |    | SF |<- M1    |
                +----+----+----+----+----+
            3   |    |    |    |  A1| D1 |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        moves.add(point22);
        packetMove = new PacketMove(Matteo.getNickname(),MatteoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertFalse(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.FIRST_BUILT, Matteo.getState());
        assertEquals(point32, MatteoW1.getPosition());
        assertEquals(MatteoW1.getID(), model.getBoard().getCell(point32).getWorkerID());

        //MATTEO'S WORKER MOVES IN POSITION 31
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    | x  |    |
                |    |    | x  | M1 | x  |
                +----+----+----+----+----+
            2   |    |    |    | FF | C  |
                |    |    | SF |  ⬆|    |
                +----+----+----+----+----+
            3   |    |    | x  |  A1| D1 |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        moves.clear();

        possibleMoves.add(new Point(4,1));
        possibleMoves.add(point31);
        possibleMoves.add(point21);
        possibleMoves.add(point23);

        assertEquals(possibleMoves,model.getPossibleMoves(Matteo, MatteoW1));

        moves.add(point31);
        packetMove = new PacketMove(Matteo.getNickname(),MatteoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Matteo.getState());
        assertEquals(point31, MatteoW1.getPosition());
        assertEquals(MatteoW1.getID(), model.getBoard().getCell(point31).getWorkerID());

        //MATTEO'S WORKER BUILDS IN POSITION 21
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    | x  | x  | x  |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    | x  | M1 | x  |
                +----+----+----+----+----+
            2   |    |    | x  | FF | C  |
                |    |    | SF | x  |    |
                +----+----+----+----+----+
            3   |    |    |    |  A1| D1 |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleBuilds.add(new Point(4,1));
        possibleBuilds.add(new Point(4,0));
        possibleBuilds.add(new Point(3,0));
        possibleBuilds.add(new Point(2,0));
        possibleBuilds.add(point21);
        possibleBuilds.add(point22);
        possibleBuilds.add(point32);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Matteo, MatteoW1).keySet());

        buildings.add(BuildingType.FIRST_FLOOR);
        builds.put(point21,buildings);
        buildsOrder.add(point21);

        packetBuild = new PacketBuild(Matteo.getNickname(), MatteoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point21).getTopBuilding(), LevelType.FIRST_FLOOR);
        assertEquals(PlayerState.BUILT, Matteo.getState());

        //ANDREA'S WORKER MOVES IN POSITION 23
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    |    | M1 |    |
                +----+----+----+----+----+
            2   |    |    |    | FF | C  |
                |    |    | SF | x  |    |
                +----+----+----+----+----+
            3   |    |    | A1 |    | D1 |
                |    |    |  X | <- |    |
                +----+----+----+----+----+
            4   |    |    |  x | x  |  FF|
                |    |    |    |    |  x |
                +----+----+----+----+----+
            Y
        */


        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(point32);
        possibleMoves.add(point23);
        possibleMoves.add(new Point(2,4));
        possibleMoves.add(point34);
        possibleMoves.add(point44);

        assertEquals(possibleMoves,model.getPossibleMoves(Andrea, AndreaW1));

        moves.add(point23);
        packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Andrea.getState());
        assertEquals(point23, AndreaW1.getPosition());
        assertEquals(AndreaW1.getID(), model.getBoard().getCell(point23).getWorkerID());

        //ANDREA'S WORKER BUILDS IN POSITION 33
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    |    | M1 |    |
                +----+----+----+----+----+
            2   |    | x  | x  | FF | C  |
                |    |    | SF | x  |    |
                +----+----+----+----+----+
            3   |    |  x | A1 |  FF| D1 |
                |    |    |    |  X |    |
                +----+----+----+----+----+
            4   |    | x  |  x | x  |  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(point22);
        possibleBuilds.add(point32);
        possibleBuilds.add(point33);
        possibleBuilds.add(point34);
        possibleBuilds.add(new Point(2,4));
        possibleBuilds.add(new Point(1,4));
        possibleBuilds.add(new Point(1,3));
        possibleBuilds.add(new Point(1,2));

        assertEquals(possibleBuilds, model.getPossibleBuilds(Andrea, AndreaW1).keySet());

        buildings.add(BuildingType.FIRST_FLOOR);
        builds.put(point33,buildings);
        buildsOrder.add(point33);

        packetBuild = new PacketBuild(Andrea.getNickname(), AndreaW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point33).getTopBuilding(), LevelType.FIRST_FLOOR);
        assertEquals(PlayerState.BUILT, Andrea.getState());

        ///////////////////SECOND TURN ENDED///////////////////////////////////

        Mirko.setPlayerState(PlayerState.TURN_STARTED);
        Matteo.setPlayerState(PlayerState.TURN_STARTED);
        Andrea.setPlayerState(PlayerState.TURN_STARTED);

        //MIRKO'S WORKER MOVES IN POSITION 33
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    |    | M1 |    |
                +----+----+----+----+----+
            2   |    |    |    | FF | C  |
                |    |    | SF | x  |    |
                +----+----+----+----+----+
            3   |    |    | A1 |  FF|    |
                |    |    |    |xD1 | <- |
                +----+----+----+----+----+
            4   |    |    |    | x  |  FF|
                |    |    |    |    | x  |
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(point44);
        possibleMoves.add(point34);
        possibleMoves.add(point33);
        possibleMoves.add(point32);

        assertEquals(possibleMoves,model.getPossibleMoves(Mirko, MirkoW1));

        moves.add(point33);
        packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Mirko.getState());
        assertEquals(point33, MirkoW1.getPosition());
        assertEquals(MirkoW1.getID(), model.getBoard().getCell(point33).getWorkerID());

        //MIRKO'S WORKER BUILDS IN POSITION 22
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    |    | M1 |    |
                +----+----+----+----+----+
            2   |    |    | X  | FF | C  |
                |    |    | TF | x  |    |
                +----+----+----+----+----+
            3   |    |    | A1 |  FF|    |
                |    |    |    | D1 |  x |
                +----+----+----+----+----+
            4   |    |    |  x | x  |  FF|
                |    |    |    |    | x  |
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(point22);
        possibleBuilds.add(point32);
        possibleBuilds.add(point43);
        possibleBuilds.add(point44);
        possibleBuilds.add(point34);
        possibleBuilds.add(new Point(2,4));

        assertEquals(possibleBuilds, model.getPossibleBuilds(Mirko, MirkoW1).keySet());

        buildings.add(BuildingType.THIRD_FLOOR);
        builds.put(point22,buildings);
        buildsOrder.add(point22);

        packetBuild = new PacketBuild(Mirko.getNickname(), MirkoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point22).getTopBuilding(), LevelType.THIRD_FLOOR);
        assertEquals(PlayerState.BUILT, Mirko.getState());

        //MATTEO'S WORKER MOVES IN POSITION 21
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    | x  | x  | x  |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    | xM1| <- | x  |
                +----+----+----+----+----+
            2   |    |    |    | FF | C  |
                |    |    | TF | x  |    |
                +----+----+----+----+----+
            3   |    |    | A1 |  FF|    |
                |    |    |    | D1 |    |
                +----+----+----+----+----+
            4   |    |    |    |    |  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(point32);
        possibleMoves.add(new Point(4,1));
        possibleMoves.add(new Point(4,0));
        possibleMoves.add(new Point(3,0));
        possibleMoves.add(new Point(2,0));
        possibleMoves.add(point21);

        assertEquals(possibleMoves, model.getPossibleMoves(Matteo, MatteoW1));

        moves.add(point21);
        packetMove = new PacketMove(Matteo.getNickname(),MatteoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Matteo.getState());
        assertEquals(point21, MatteoW1.getPosition());
        assertEquals(MatteoW1.getID(), model.getBoard().getCell(point21).getWorkerID());

        //MATTEO'S WORKER BUILDS IN POSITION 32
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    | x  | x  | x  |    |
                +----+----+----+----+----+
            1   |    | x  | FF |    |    |
                |    |    |  M1| x  |    |
                +----+----+----+----+----+
            2   |    | x  | x  | SF | C  |
                |    |    | TF | X  |    |
                +----+----+----+----+----+
            3   |    |    | A1 |  FF|    |
                |    |    |    | D1 |    |
                +----+----+----+----+----+
            4   |    |    |    |    |  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(new Point(3,0));
        possibleBuilds.add(new Point(2,0));
        possibleBuilds.add(new Point(1,0));
        possibleBuilds.add(new Point(1,1));
        possibleBuilds.add(new Point(1,2));
        possibleBuilds.add(point22);
        possibleBuilds.add(point32);
        possibleBuilds.add(point31);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Matteo, MatteoW1).keySet());

        buildings.add(BuildingType.SECOND_FLOOR);
        builds.put(point32,buildings);
        buildsOrder.add(point32);

        packetBuild = new PacketBuild(Matteo.getNickname(), MatteoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point32).getTopBuilding(), LevelType.SECOND_FLOOR);
        assertEquals(PlayerState.BUILT, Matteo.getState());

        //ANDREA'S WORKER MOVES IN POSITION 34
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    |  M1|    |    |
                +----+----+----+----+----+
            2   |    | x  |    | SF | C  |
                |    |    | TF |    |    |
                +----+----+----+----+----+
            3   |    | x  | ↘  |  FF|    |
                |    |    |    | D1 |    |
                +----+----+----+----+----+
            4   |    | x  | x  | xA1|  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(new Point(1,2));
        possibleMoves.add(new Point(1,3));
        possibleMoves.add(new Point(1,4));
        possibleMoves.add(new Point(2,4));
        possibleMoves.add(point34);

        assertEquals(possibleMoves,model.getPossibleMoves(Andrea, AndreaW1));

        moves.add(point34);
        packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Andrea.getState());
        assertEquals(point34, AndreaW1.getPosition());
        assertEquals(AndreaW1.getID(), model.getBoard().getCell(point34).getWorkerID());

        //ANDREA'S WORKER BUILDS IN POSITION 23
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    |  M1|    |    |
                +----+----+----+----+----+
            2   |    |    |    | SF | C  |
                |    |    | TF |    |    |
                +----+----+----+----+----+
            3   |    |    | C  |  FF| X  |
                |    |    | X  | D1 |    |
                +----+----+----+----+----+
            4   |    |    | x  |  A1|  FF|
                |    |    |    |    |  x |
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(point23);
        possibleBuilds.add(new Point(2,4));
        possibleBuilds.add(point44);
        possibleBuilds.add(point43);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Andrea, AndreaW1).keySet());

        buildings.add(BuildingType.DOME);
        builds.put(point23,buildings);
        buildsOrder.add(point23);

        packetBuild = new PacketBuild(Andrea.getNickname(), AndreaW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point23).getTopBuilding(), LevelType.DOME);
        assertEquals(PlayerState.BUILT, Andrea.getState());

        ///////////////////THIRD TURN ENDED///////////////////////////////////

        Mirko.setPlayerState(PlayerState.TURN_STARTED);
        Matteo.setPlayerState(PlayerState.TURN_STARTED);
        Andrea.setPlayerState(PlayerState.TURN_STARTED);

        //MIRKO'S WORKER MOVES IN POSITION 32
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    |  M1|    |    |
                +----+----+----+----+----+
            2   |    |    |    | SF | C  |
                |    |    | TF | XD1|    |
                +----+----+----+----+----+
            3   |    |    | C  |  FF| X  |
                |    |    |    |  ⬆|    |
                +----+----+----+----+----+
            4   |    |    | x  |  A1|  FF|
                |    |    |    |  x |  x |
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(point44);
        possibleMoves.add(point34);
        possibleMoves.add(point43);
        possibleMoves.add(point32);
        possibleMoves.add(new Point(2,4));

        assertEquals(possibleMoves,model.getPossibleMoves(Mirko, MirkoW1));

        moves.add(point32);
        packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Mirko.getState());
        assertEquals(point32, MirkoW1.getPosition());
        assertEquals(MirkoW1.getID(), model.getBoard().getCell(point32).getWorkerID());

        //MIRKO'S WORKER BUILDS IN POSITION 33
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | FF |    |    |
                |    |    |  M1| x  | x  |
                +----+----+----+----+----+
            2   |    |    |  x | SF | C  |
                |    |    | TF | D1 |    |
                +----+----+----+----+----+
            3   |    |    | C  |  SF| x  |
                |    |    |    |  X |    |
                +----+----+----+----+----+
            4   |    |    |    |  A1|  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(point31);
        possibleBuilds.add(new Point(4,1));
        possibleBuilds.add(point43);
        possibleBuilds.add(point33);
        possibleBuilds.add(point22);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Mirko, MirkoW1).keySet());

        buildings.add(BuildingType.SECOND_FLOOR);
        builds.put(point33,buildings);
        buildsOrder.add(point33);

        packetBuild = new PacketBuild(Mirko.getNickname(), MirkoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point33).getTopBuilding(), LevelType.SECOND_FLOOR);
        assertEquals(PlayerState.BUILT, Mirko.getState());

        //MATTEO'S WORKER BUILDS IN POSITION 31
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    | x  | x  | x  |    |
                +----+----+----+----+----+
            1   |    |    | FF | FF |    |
                |    | x  |  M1| x  |    |
                +----+----+----+----+----+
            2   |    | x  |  x | SF | C  |
                |    |    | TF | D1 |    |
                +----+----+----+----+----+
            3   |    |    | C  |  SF|    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |  A1|  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleBuilds.add(point31);
        possibleBuilds.add(new Point(3,0));
        possibleBuilds.add(new Point(2,0));
        possibleBuilds.add(new Point(1,0));
        possibleBuilds.add(new Point(1,1));
        possibleBuilds.add(new Point(1,2));
        possibleBuilds.add(point22);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Matteo, MatteoW1).keySet());

        buildings.add(BuildingType.FIRST_FLOOR);
        builds.put(point31,buildings);
        buildsOrder.add(point31);

        packetBuild = new PacketBuild(Matteo.getNickname(), MatteoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point31).getTopBuilding(), LevelType.FIRST_FLOOR);
        assertEquals(PlayerState.FIRST_BUILT, Matteo.getState());

        //MATTEO'S WORKER MOVES IN POSITION 31
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    | x  | x  | x  |    |
                +----+----+----+----+----+
            1   |    |    | FF | FF |    |
                |    | x  |  ->| xM1|    |
                +----+----+----+----+----+
            2   |    | x  |    | SF | C  |
                |    |    | TF | D1 |    |
                +----+----+----+----+----+
            3   |    |    | C  |  SF|    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |  A1|  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        possibleMoves.add(point31);
        possibleMoves.add(new Point(3,0));
        possibleMoves.add(new Point(2,0));
        possibleMoves.add(new Point(1,0));
        possibleMoves.add(new Point(1,1));
        possibleMoves.add(new Point(1,2));

        assertEquals(possibleMoves, model.getPossibleMoves(Matteo, MatteoW1));

        moves.add(point31);
        packetMove = new PacketMove(Matteo.getNickname(),MatteoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Matteo.getState());
        assertEquals(point31, MatteoW1.getPosition());
        assertEquals(MatteoW1.getID(), model.getBoard().getCell(point31).getWorkerID());

        //MATTEO'S WORKER BUILDS IN POSITION 21
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    | x  | x  | x  |
                +----+----+----+----+----+
            1   |    |    | SF | FF |    |
                |    |    |  X | M1 | x  |
                +----+----+----+----+----+
            2   |    |    | x  | SF | C  |
                |    |    | TF | D1 |    |
                +----+----+----+----+----+
            3   |    |    | C  |  SF|    |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |  A1|  FF|
                |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */

        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleBuilds.add(point21);
        possibleBuilds.add(new Point(3,0));
        possibleBuilds.add(new Point(2,0));
        possibleBuilds.add(new Point(4,0));
        possibleBuilds.add(new Point(4,1));
        possibleBuilds.add(point22);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Matteo, MatteoW1).keySet());

        buildings.add(BuildingType.SECOND_FLOOR);
        builds.put(point21,buildings);
        buildsOrder.add(point21);

        packetBuild = new PacketBuild(Matteo.getNickname(), MatteoW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point21).getTopBuilding(), LevelType.SECOND_FLOOR);
        assertEquals(PlayerState.BUILT, Matteo.getState());

        //ANDREA'S WORKER MOVES IN POSITION 44
        /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | SF | FF |    |
                |    |    |    | M1 |    |
                +----+----+----+----+----+
            2   |    |    |    | SF | C  |
                |    |    | TF | D1 |    |
                +----+----+----+----+----+
            3   |    |    | C  |  SF| x  |
                |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    | x  |    |  FF|
                |    |    |    |  ->| xA1|
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(new Point(2,4));
        possibleMoves.add(point44);
        possibleMoves.add(point43);

        assertEquals(possibleMoves,model.getPossibleMoves(Andrea, AndreaW1));

        moves.add(point44);
        packetMove = new PacketMove(Andrea.getNickname(),AndreaW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal | PlayerLostSignal e) {
            assert false;
        }
        assertEquals(PlayerState.MOVED, Andrea.getState());
        assertEquals(point44, AndreaW1.getPosition());
        assertEquals(AndreaW1.getID(), model.getBoard().getCell(point44).getWorkerID());

        //ANDREA'S WORKER BUILDS IN POSITION 33
         /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | SF | FF |    |
                |    |    |    | M1 |    |
                +----+----+----+----+----+
            2   |    |    |    | SF | C  |
                |    |    | TF | D1 |    |
                +----+----+----+----+----+
            3   |    |    | C  |  TF| x  |
                |    |    |    |  x |    |
                +----+----+----+----+----+
            4   |    |    |    |    |  FF|
                |    |    |    |  x |  A1|
                +----+----+----+----+----+
            Y
        */

        possibleBuilds.add(point34);
        possibleBuilds.add(point33);
        possibleBuilds.add(point43);

        assertEquals(possibleBuilds, model.getPossibleBuilds(Andrea, AndreaW1).keySet());

        buildings.add(BuildingType.THIRD_FLOOR);
        builds.put(point33,buildings);
        buildsOrder.add(point33);

        packetBuild = new PacketBuild(Andrea.getNickname(), AndreaW1.getID(), builds, buildsOrder);
        buildData = null;
        try {
            buildData = model.packetBuildToBuildData(packetBuild);
        } catch (InvalidPacketException e) {
            assert false;
        }
        assertNotNull(buildData);
        try {
            assertTrue(model.makeBuild(buildData));
        } catch (PlayerWonSignal | PlayerLostSignal playerWonSignal) {
            assert false;
        }

        assertSame(model.getBoard().getCell(point33).getTopBuilding(), LevelType.THIRD_FLOOR);
        assertEquals(PlayerState.BUILT, Andrea.getState());

        Mirko.setPlayerState(PlayerState.TURN_STARTED);

        //MIRKO NOW CAN WIN IN TWO WAYS: BY GOING IN 2,2 OR 3,3
         /*
                  0    1     2    3    4   X
                +----+----+----+----+----+
            0   |    |    |    |    |    |
                +----+----+----+----+----+
            1   |    |    | SF | FF |    |
                |    |    | x  |x M1| x  |
                +----+----+----+----+----+
            2   |    |    | WIN| SF | C  |
                |    |    | TF | D1 |    |
                +----+----+----+----+----+
            3   |    |    | C  |  TF| x  |
                |    |    |    |WIN |    |
                +----+----+----+----+----+
            4   |    |    |    |    |  FF|
                |    |    |    |    |  A1|
                +----+----+----+----+----+
            Y
        */

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        possibleMoves.add(new Point(4,1));
        possibleMoves.add(point31);
        possibleMoves.add(point21);
        possibleMoves.add(point22);
        possibleMoves.add(point33);
        possibleMoves.add(point43);

        assertEquals(possibleMoves,model.getPossibleMoves(Mirko, MirkoW1));

        moves.add(point33);
        packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal e) {
            assert true;
        }
        catch (PlayerLostSignal e){
            assert false;
        }
        assertEquals(PlayerState.MOVED, Mirko.getState());
        assertEquals(point33, MirkoW1.getPosition());
        assertEquals(MirkoW1.getID(), model.getBoard().getCell(point33).getWorkerID());

        //RESET MIRKO'S WORKER IN 3,2 AND MAKE HIM WIN IN 2,2
        Mirko.setPlayerState(PlayerState.TURN_STARTED);
        MirkoW1.setPosition(point32);
        model.getBoard().getCell(point33).removeWorker();
        model.getBoard().getCell(point32).setWorker(MirkoW1.getID());

        //RESET UTILITIES
        possibleMoves.clear();
        moves.clear();
        buildings.clear();
        builds.clear();
        buildsOrder.clear();
        possibleBuilds.clear();

        moves.add(point22);
        packetMove = new PacketMove(Mirko.getNickname(),MirkoW1.getID(),moves);
        moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
        } catch (InvalidPacketException e){
            assert false;
        }
        assertNotNull(moveData);
        try {
            assertTrue(model.makeMove(moveData));
        } catch (PlayerWonSignal e) {
            assert true;
        }
        catch (PlayerLostSignal e){
            assert false;
        }
        assertEquals(PlayerState.MOVED, Mirko.getState());
        assertEquals(point22, MirkoW1.getPosition());
        assertEquals(MirkoW1.getID(), model.getBoard().getCell(point22).getWorkerID());

    }

    //Tests from 1-9 test if the method that converts a PacketMove into a MoveData correctly returns the InvalidPacketException.
    //Test 10 tests the correct execution.
    /**
     * Test if the argument PlayerNickname is not one of the registered Player.
     */
    @Test
    void testPacketMoveToMoveData1(){
        //CASE WRONG PLAYER
        List<Point> moves = new ArrayList<>();
        moves.add(new Point(0,0));
        PacketMove packetMove = new PacketMove("Marco", MirkoW1.getID(),moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }
        
        assertNull(moveData);

        /*CASE MOVES IS NULL
        moves = null;

        packetMove = new PacketMove("Mirko", MirkoW1.getID(),moves);
        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }*/


        /*CASE A POINT IS NULL
        moves = new ArrayList<>();
        moves.add(null);

        packetMove = new PacketMove("Mirko", MirkoW1.getID(),moves);
        try{
            MoveData moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }*/

    }

    /**
     * Test if the argument workerID is not one of the Worker in the game.
     */
    @Test
    void testPacketMoveToMoveData2(){
        //CASE WRONG WORKER
        List<Point> moves = new ArrayList<>();
        moves.add(new Point(0,0));

        PacketMove packetMove = new PacketMove(Mirko.getNickname(), "MirkoW3",moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(moveData);
    }

    /**
     * Test if the moves list is empty.
     */
    @Test
    void testPacketMoveToMoveData3(){
        //CASE MOVES IS EMPTY

        List<Point> moves = new ArrayList<>();

        PacketMove packetMove = new PacketMove(Mirko.getNickname(), MirkoW1.getID(),moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(moveData);
    }

    /**
     * Test if one of the points is not on the Board.
     */
    @Test
    void testPacketMoveToMoveData4(){
        //CASE A POINT IS NOT ON THE BOARD
        List<Point >moves = new ArrayList<>();
        moves.add(new Point(6,6));

        PacketMove packetMove = new PacketMove(Mirko.getNickname(), MirkoW1.getID(),moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(moveData);
    }

    /**
     * Test if in the first move position there is a DOME.
     */
    @Test
    void testPacketMoveToMoveData5(){
        //CASE IN FIRST MOVE THERE IS A DOME
        Point startPosition = new Point(0,0);
        MirkoW1.setPosition(startPosition);
        Point firstMove = new Point(0,1);
        model.getBoard().getCell(firstMove).addBuilding(BuildingType.DOME);

        List<Point> moves = new ArrayList<>();
        moves.add(firstMove);

        PacketMove packetMove = new PacketMove("Mirko", MirkoW1.getID(),moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(moveData);
    }

    /**
     * Test if the first move position is not adjacent to the start position.
     */
    @Test
    void testPacketMoveToMoveData6(){
        //CASE THE FIRST MOVE IS NOT ADJACENT TO THE START POSITION

        Point startPosition = new Point(0,0);
        MirkoW1.setPosition(startPosition);
        Point firstMove = new Point(0,2);

        List<Point> moves = new ArrayList<>();
        moves.add(firstMove);

        PacketMove packetMove = new PacketMove("Mirko", MirkoW1.getID(),moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(moveData);
    }

    /**
     * Test if the second move position is not adjacent to the first move position.
     */
    @Test
    void testPacketMoveToMoveData7(){
        //CASE THE SECOND MOVE IS NOT ADJACENT TO THE FIRST MOVE

        Point startPosition = new Point(0,0);
        MirkoW1.setPosition(startPosition);
        Point firstMove = new Point(1,0);
        Point secondMove = new Point(0,3);

        List<Point> moves = new ArrayList<>();
        moves.add(firstMove);
        moves.add(secondMove);

        PacketMove packetMove = new PacketMove("Mirko", MirkoW1.getID(),moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(moveData);
    }

    /**
     * Test if in the second move position there is a DOME.
     */
    @Test
    void testPacketMoveToMoveData8(){
        //CASE THE SECOND MOVE IS A DOME

        Point startPosition = new Point(0,0);
        MirkoW1.setPosition(startPosition);
        Point firstMove = new Point(0,1);
        Point secondMove = new Point(0,2);
        model.getBoard().getCell(secondMove).addBuilding(BuildingType.DOME);

        List<Point> moves = new ArrayList<>();
        moves.add(firstMove);
        moves.add(secondMove);

        PacketMove packetMove = new PacketMove("Mirko", MirkoW1.getID(),moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(moveData);
    }

    /**
     * The Worker belongs to another Player.
     */
    @Test
    void testPacketMoveToMoveData9(){
        List<Point> moves = new ArrayList<>();
        moves.add(new Point(0,0));
        PacketMove packetMove = new PacketMove(Andrea.getNickname(), MirkoW1.getID(),moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(moveData);
    }

    /**
     * Test if the method correctly convert the packet because all the conditions are satisfied.
     */
    @Test
    void testPacketMoveToMoveData10(){
        //CASE EVERYTHING SHOULD BE FINE
        Point startPosition = new Point(0,0);
        MirkoW1.setPosition(startPosition);
        Point firstMove = new Point(1,0);
        Point secondMove = new Point(1,1);
        model.getBoard().getCell(firstMove).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(secondMove).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(secondMove).addBuilding(BuildingType.SECOND_FLOOR);

        List<Point> moves = new ArrayList<>();
        moves.add(firstMove);
        moves.add(secondMove);

        PacketMove packetMove = new PacketMove("Mirko", MirkoW1.getID(),moves);
        MoveData moveData = null;
        try{
            moveData = model.packetMoveToMoveData(packetMove);
            assert true;
        } catch (InvalidPacketException e) {
            assert false;
        }

        assertNotNull(moveData);
        assertEquals(moveData.getData(), moves);

    }


    //Tests from 1 - 10 test the method that converts a PacketBuild into a BuildData correctly returns the InvalidPacketException.
    //Test 11 tests the correct execution.
    /**
     * The argument PlayerNickname is not one of the registered Player.
     */
    @Test
    void testPacketBuildToBuildData1(){

        Map<Point, List<BuildingType>> builds = new HashMap<>();

        List<Point> orderBuilds = new ArrayList<>();

        Point startPosition = new Point(0,0);
        Point firstBuild = new Point(0,1);

        List<BuildingType> buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);
        buildings.add(BuildingType.SECOND_FLOOR);
        builds.put(firstBuild,buildings);

        orderBuilds.add(firstBuild);

        MirkoW1.setPosition(startPosition);


        //CASE WRONG PLAYER
        PacketBuild packetBuild = new PacketBuild("Marco",MirkoW1.getID(),builds,orderBuilds);
        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);

        /*CASE BUILDSORDER IS NULL

        packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,null);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }*/


        /*CASE BUILD IS NULL
        orderBuilds = new ArrayList<>();
        orderBuilds.add(firstBuild);
        packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),null,orderBuilds);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }*/


        /*CASE BUILDINGTYPE IN BUILDS IS NULL
        builds = new HashMap<>();
        orderBuilds = new ArrayList<>();
        builds.put(firstBuild,null);
        orderBuilds.add(firstBuild);

        packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,orderBuilds);

        try{
            BuildData buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }*/

    }

    /**
     * The argument workerID is not one of the Worker in the game.
     */
    @Test
    void testPacketBuildToBuildData2(){

        Map<Point, List<BuildingType>> builds = new HashMap<>();

        List<Point> orderBuilds = new ArrayList<>();
        Point startPosition = new Point(0,0);
        Point firstBuild = new Point(0,1);

        List<BuildingType> buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);
        buildings.add(BuildingType.SECOND_FLOOR);

        builds.put(firstBuild,buildings);
        orderBuilds.add(firstBuild);

        MirkoW1.setPosition(startPosition);

        //CASE WRONG WORKER
        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),"MirkoW3",builds,orderBuilds);

        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);
    }

    /**
     * The buildsOrder List is empty.
     */
    @Test
    void testPacketBuildToBuildData3(){

        Map<Point, List<BuildingType>> builds = new HashMap<>();

        List<Point> orderBuilds = new ArrayList<>();

        Point startPosition = new Point(0,0);
        Point firstBuild = new Point(0,1);

        List<BuildingType> buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);
        buildings.add(BuildingType.SECOND_FLOOR);

        builds.put(firstBuild,buildings);

        MirkoW1.setPosition(startPosition);

        //CASE BUILDSORDER EMPTY

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,orderBuilds);

        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);
    }

    /**
     * The size of buildsOrder is not equal to the size of builds.
     */
    @Test
    void testPacketBuildToBuildData4(){
        Map<Point, List<BuildingType>> builds = new HashMap<>();

        List<Point> orderBuilds = new ArrayList<>();

        Point startPosition = new Point(0,0);
        Point firstBuild = new Point(0,1);
        Point secondBuild = new Point(1,0);

        List<BuildingType> buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);
        buildings.add(BuildingType.SECOND_FLOOR);

        builds.put(firstBuild,buildings);
        builds.put(secondBuild, buildings); //BUILDS HAS SIZE 2

        orderBuilds.add(firstBuild); //ORDERBUILDS HAS SIZE 1

        MirkoW1.setPosition(startPosition);

        //CASE BUILDSORDER.size() != BUILDS.size()


        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,orderBuilds);

        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);
    }

    /**
     * The builds Map is empty.
     */
    @Test
    void testPacketBuildToBuildData5(){

        Map<Point, List<BuildingType>> builds = new HashMap<>();

        Point startPosition = new Point(0,0);
        Point firstBuild = new Point(0,1);

        List<Point> orderBuilds = new ArrayList<>();
        orderBuilds.add(firstBuild);

        MirkoW1.setPosition(startPosition);

        //CASE BUILDS IS EMPTY

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,orderBuilds);

        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);
    }

    /**
     * There is a buildingType empty list in builds Map.
     */
    @Test
    void testPacketBuildToBuildData6(){

        Map<Point, List<BuildingType>> builds = new HashMap<>();

        Point startPosition = new Point(0,0);
        Point firstBuild = new Point(0,1);

        List<BuildingType> buildings = new ArrayList<>();
        builds.put(firstBuild,buildings);

        List<Point> orderBuilds = new ArrayList<>();
        orderBuilds.add(firstBuild);

        MirkoW1.setPosition(startPosition);

        //CASE BUILDINGTYPE LIST IN BUILDS IS EMPTY
        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,orderBuilds);

        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);
    }

    /**
     * There is a point not on the Board in builds Map.
     */
    @Test
    void testPacketBuildToBuildData7(){
        Map<Point, List<BuildingType>> builds = new HashMap<>();

        List<Point> orderBuilds = new ArrayList<>();

        Point startPosition = new Point(0,0);
        Point out = new Point(6,6);

        List<BuildingType> buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);

        builds.put(out,buildings);
        orderBuilds.add(out);

        MirkoW1.setPosition(startPosition);

        //CASE POINT IN BUILDS IS NOT ON THE BOARD

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,orderBuilds);

        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);
    }

    /**
     * There is a not adjacent points in builds.
     */
    @Test
    void testPacketBuildToBuildData8(){
        Map<Point, List<BuildingType>> builds = new HashMap<>();

        List<Point> orderBuilds = new ArrayList<>();

        Point startPosition = new Point(0,0);

        List<BuildingType> buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);

        Point notAdjacent = new Point(0,3);

        builds.put(notAdjacent,buildings);
        orderBuilds.add(notAdjacent);


        MirkoW1.setPosition(startPosition);

        //CASE BUILD POINT NOT ADJACENT

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,orderBuilds);

        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);
    }

    /**
     * There is a point in builds Map that is different from the one in buildsOrder List.
     */
    @Test
    void testPacketBuildToBuildData9(){
        Map<Point, List<BuildingType>> builds = new HashMap<>();

        List<Point> orderBuilds = new ArrayList<>();

        Point startPosition = new Point(0,0);

        Point firstBuild = new Point(0,1);

        Point differentBuild = new Point(1,0);

        List<BuildingType> buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);
        buildings.add(BuildingType.SECOND_FLOOR);
        builds.put(firstBuild,buildings);

        orderBuilds.add(differentBuild);

        MirkoW1.setPosition(startPosition);

        //CASE BUILD POINT IS NOT THE SAME IN BUILDSORDER

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,orderBuilds);

        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);
    }

    /**
     * The Worker belongs to another player.
     */
    @Test
    void testPacketBuildToBuildData10(){
        Map<Point, List<BuildingType>> builds = new HashMap<>();

        List<Point> orderBuilds = new ArrayList<>();

        Point startPosition = new Point(0,0);
        Point firstBuild = new Point(0,1);

        List<BuildingType> buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);
        buildings.add(BuildingType.SECOND_FLOOR);
        builds.put(firstBuild,buildings);

        orderBuilds.add(firstBuild);

        MirkoW1.setPosition(startPosition);


        //CASE WRONG PLAYER
        PacketBuild packetBuild = new PacketBuild(Andrea.getNickname(),MirkoW1.getID(),builds,orderBuilds);

        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert false;
        } catch (InvalidPacketException e) {
            assert true;
        }

        assertNull(buildData);
    }

    /**
     * The method correctly converts the packet because all the conditions are satisfied.
     */
    @Test
    void testPacketBuildToBuildData11(){
        //CASE EVERYTHING SHOULD BE FINE
        Map<Point, List<BuildingType>> builds = new HashMap<>();

        List<Point> orderBuilds = new ArrayList<>();

        Point startPosition = new Point(0,0);

        Point firstBuild = new Point(0,1);

        Point secondBuild = new Point(1,0);

        List<BuildingType> buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);
        orderBuilds.add(firstBuild);
        buildings.add(BuildingType.SECOND_FLOOR);
        orderBuilds.add(firstBuild);

        builds.put(firstBuild,buildings);

        buildings = new ArrayList<>();
        buildings.add(BuildingType.FIRST_FLOOR);
        orderBuilds.add(secondBuild);
        builds.put(secondBuild,buildings);


        MirkoW1.setPosition(startPosition);

        PacketBuild packetBuild = new PacketBuild(Mirko.getNickname(),MirkoW1.getID(),builds,orderBuilds);
        BuildData buildData = null;
        try{
            buildData = model.packetBuildToBuildData(packetBuild);
            assert true;
        } catch (InvalidPacketException e) {
            assert false;
        }

        assertNotNull(buildData);
        assertEquals(buildData.getData(), builds);
        assertEquals(buildData.getDataOrder(), orderBuilds);

    }

}