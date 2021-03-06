package it.polimi.ingsw.server.model;

import it.polimi.ingsw.common.enums.BuildingType;
import it.polimi.ingsw.server.cards.CardFactory;
import it.polimi.ingsw.server.cards.RuleStatement;
import it.polimi.ingsw.server.cards.RuleStatementImplTest;
import it.polimi.ingsw.server.cards.enums.StatementType;
import it.polimi.ingsw.server.cards.enums.StatementVerbType;
import it.polimi.ingsw.server.cards.exceptions.InvalidCardException;
import it.polimi.ingsw.server.model.enums.LevelType;
import it.polimi.ingsw.server.model.enums.PlayerFlag;
import it.polimi.ingsw.server.model.enums.PlayerState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatementCompilerTest {

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



/*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

    @BeforeEach
    void setUp() throws InvalidCardException {
        List<String> players = new ArrayList<>();
        players.add("Andrea");
        players.add("Matteo");
        players.add("Mirko");
        model = new InternalModel(players, CardFactory.getInstance());
        Andrea = model.getPlayerByNick("Andrea");
        Matteo = model.getPlayerByNick("Matteo");
        Mirko = model.getPlayerByNick("Mirko");
        MatteoW1 = Matteo.getWorkers().get(0);
        MatteoW2 = Matteo.getWorkers().get(1);
        MirkoW1 = Mirko.getWorkers().get(0);
        MirkoW2 = Mirko.getWorkers().get(1);
        AndreaW1 = Andrea.getWorkers().get(0);
        AndreaW2 = Andrea.getWorkers().get(1);


        model.getBoard().getCell(new Point(0,0)).setWorker(AndreaW1.getID());
        model.getBoard().getCell(new Point(2,2)).setWorker(AndreaW2.getID());
        model.getBoard().getCell(new Point(1,2)).setWorker(MatteoW1.getID());
        model.getBoard().getCell(new Point(3,2)).setWorker(MatteoW2.getID());
        model.getBoard().getCell(new Point(2,4)).setWorker(MirkoW1.getID());
        model.getBoard().getCell(new Point(3,3)).setWorker(MirkoW2.getID());

        AndreaW1.setPosition(new Point(0,0));
        AndreaW2.setPosition(new Point(2,2));
        MatteoW1.setPosition(new Point(1,2));
        MatteoW2.setPosition(new Point(3,2));
        MirkoW1.setPosition(new Point(2,4));
        MirkoW2.setPosition(new Point(3,3));
    }

    @AfterEach
    void tearDown() {
        model = null;
    }

    /*
        In-depth test of player equals
     */
    @Test
    void playerEquals_Test1() {
        for (Player cardOwner : model.getPlayers()){
            RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.PLAYER_EQUALS, "CARD_OWNER");
            LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, cardOwner);

            // WITH IF
            for (Player p : model.getPlayers()) {
                Worker w = p.getWorkers().get(0);
                List<Point> emptyList = new LinkedList<>();
                Map<Point, List<BuildingType>> data = new HashMap<>();

                MoveData moveData = new MoveData(p, w, emptyList);
                BuildData buildData = new BuildData(p, w, data, emptyList);
                if (p.equals(cardOwner)) {
                    assert (compiledStatement.evaluate(moveData, null));
                    assert (compiledStatement.evaluate(null, buildData));
                } else {
                    assert (!compiledStatement.evaluate(null, buildData));
                    assert (!compiledStatement.evaluate(moveData, null));
                }
            }

            playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.PLAYER_EQUALS, "CARD_OWNER");
            compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, cardOwner);

            // WITH NIF
            for (Player p : model.getPlayers()) {
                Worker w = p.getWorkers().get(0);
                List<Point> emptyList = new LinkedList<>();
                Map<Point, List<BuildingType>> data = new HashMap<>();

                MoveData moveData = new MoveData(p, w, emptyList);
                BuildData buildData = new BuildData(p, w, data, emptyList);
                if (p.equals(cardOwner)) {
                    assert (!compiledStatement.evaluate(moveData, null));
                    assert (!compiledStatement.evaluate(null, buildData));
                } else {
                    assert (compiledStatement.evaluate(null, buildData));
                    assert (compiledStatement.evaluate(moveData, null));
                }
            }
        }
    }
    /**
     * Testing the statement STATE_EQUALS when the object is TURN_STARTED.
     * If the Statement type is IF the evaluation should be true.
     * If the Statement type is NIF the evaluation should be false.
     */
    @Test
    void stateEquals_Test1(){

        RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.STATE_EQUALS, "TURN_STARTED");
        LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        // WITH IF AND TURN_STARTED
        Player p = Andrea;
        Worker w = p.getWorkers().get(0);
        List<Point> emptyList = new LinkedList<>();
        Map<Point, List<BuildingType>> data = new HashMap<>();

        MoveData moveData = new MoveData(p, w, emptyList);
        BuildData buildData = new BuildData(p, w, data, emptyList);
        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.MOVED);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.BUILT);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.FIRST_BUILT);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));






        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.STATE_EQUALS, "TURN_STARTED");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        // WITH NIF AND TURN_STARTED
        p.setPlayerState(PlayerState.TURN_STARTED);
        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.MOVED);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.BUILT);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.FIRST_BUILT);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));



    }
    /**
     * Testing the statement STATE_EQUALS when the object is MOVED.
     * If the Statement type is IF the evaluation should be true.
     * If the Statement type is NIF the evaluation should be false.
     */
    @Test
    void stateEquals_Test2(){

        RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.STATE_EQUALS, "MOVED");
        LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        Player p = Andrea;
        Worker w = p.getWorkers().get(0);
        List<Point> emptyList = new LinkedList<>();
        Map<Point, List<BuildingType>> data = new HashMap<>();

        MoveData moveData = new MoveData(p, w, emptyList);
        BuildData buildData = new BuildData(p, w, data, emptyList);

        // WITH IF AND MOVED
        p.setPlayerState(PlayerState.MOVED);
        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.TURN_STARTED);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.BUILT);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.FIRST_BUILT);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));


        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.STATE_EQUALS, "MOVED");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        // WITH NIF AND MOVED
        p.setPlayerState(PlayerState.MOVED);
        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.TURN_STARTED);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.BUILT);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.FIRST_BUILT);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

    }
    /**
     * Testing the statement STATE_EQUALS when the object is BUILT.
     * If the Statement type is IF the evaluation should be true.
     * If the Statement type is NIF the evaluation should be false.
     */
    @Test
    void stateEquals_Test3(){

        RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.STATE_EQUALS, "BUILT");
        LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        Player p = Andrea;
        Worker w = p.getWorkers().get(0);
        List<Point> emptyList = new LinkedList<>();
        Map<Point, List<BuildingType>> data = new HashMap<>();

        MoveData moveData = new MoveData(p, w, emptyList);
        BuildData buildData = new BuildData(p, w, data, emptyList);


        // WITH IF AND BUILT
        p.setPlayerState(PlayerState.BUILT);
        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.TURN_STARTED);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.MOVED);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.FIRST_BUILT);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));


        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.STATE_EQUALS, "BUILT");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        // WITH NIF AND BUILT
        p.setPlayerState(PlayerState.BUILT);
        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.TURN_STARTED);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.MOVED);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.FIRST_BUILT);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));



    }
    /**
     * Testing the statement STATE_EQUALS when the object is FIRST_BUILT.
     * If the Statement type is IF the evaluation should be true.
     * If the Statement type is NIF the evaluation should be false.
     */
    @Test
    void stateEquals_Test4(){

        RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.STATE_EQUALS, "FIRST_BUILT");
        LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        Player p = Andrea;
        Worker w = p.getWorkers().get(0);
        List<Point> emptyList = new LinkedList<>();
        Map<Point, List<BuildingType>> data = new HashMap<>();

        MoveData moveData = new MoveData(p, w, emptyList);
        BuildData buildData = new BuildData(p, w, data, emptyList);

        // WITH IF AND FIRST_BUILT
        p.setPlayerState(PlayerState.FIRST_BUILT);
        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.TURN_STARTED);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.MOVED);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.BUILT);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));


        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.STATE_EQUALS, "FIRST_BUILT");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        // WITH NIF AND FIRST_BUILT
        p.setPlayerState(PlayerState.FIRST_BUILT);
        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.TURN_STARTED);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.MOVED);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        p.setPlayerState(PlayerState.BUILT);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

    }
    /**
     * Testing the Statement Verb HAS_FLAG:
     * When the obj is not a flag contained in Player the evaluation should be false with the Statement type IF, true if contained.
     * When the obj is not a flag contained in Player the evaluation should be true with the Statement type NIF, false if contained.
     */
    @Test
    void hasFlag_Test1(){
        RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.HAS_FLAG, "MOVED_UP_ONCE");
        LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        Player p = Andrea;
        Worker w = p.getWorkers().get(0);
        List<Point> emptyList = new LinkedList<>();
        Map<Point, List<BuildingType>> data = new HashMap<>();

        MoveData moveData = new MoveData(p, w, emptyList);
        BuildData buildData = new BuildData(p, w, data, emptyList);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.HAS_FLAG, "MOVED_UP_ONCE");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);


        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.addFlag(PlayerFlag.MOVED_UP_ONCE);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.HAS_FLAG, "MOVED_UP_ONCE");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

    }
    /**
     * Testing the Statement Verb HAS_FLAG:
     * When the obj is not a flag contained in CardOwner the evaluation should be false with the Statement type IF, true if contained.
     * When the obj is not a flag contained in CardOwner the evaluation should be true with the Statement type NIF, false if contained.
     */
    @Test
    void hasFlag_Test2(){
        RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CARD_OWNER", StatementVerbType.HAS_FLAG, "MOVED_UP_ONCE");
        LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        Player p = Andrea;
        Worker w = p.getWorkers().get(0);
        List<Point> emptyList = new LinkedList<>();
        Map<Point, List<BuildingType>> data = new HashMap<>();

        MoveData moveData = new MoveData(p, w, emptyList);
        BuildData buildData = new BuildData(p, w, data, emptyList);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "CARD_OWNER", StatementVerbType.HAS_FLAG, "MOVED_UP_ONCE");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);


        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

        p.addFlag(PlayerFlag.MOVED_UP_ONCE);

        assert (compiledStatement.evaluate(moveData, null));
        assert (compiledStatement.evaluate(null, buildData));

        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CARD_OWNER", StatementVerbType.HAS_FLAG, "MOVED_UP_ONCE");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        assert (!compiledStatement.evaluate(moveData, null));
        assert (!compiledStatement.evaluate(null, buildData));

    }
    /**
     * Testing the Statement Verb MOVE_LENGTH when the obj is 0.
     * If there is a single move, then the evaluation should be false with the Statement Type IF.
     * If there is a single move, then the evaluation should be true with the Statement Type NIF.
     */
    @Test
    void moveLength_Test1(){
        RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.MOVE_LENGTH, "0");
        LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        Player p = Andrea;
        List<Point> moves = new ArrayList<>();

        moves.add(new Point(1,1));

        MoveData moveData = new MoveData(p, AndreaW1, moves);

        assert (!compiledStatement.evaluate(moveData, null));

        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.MOVE_LENGTH, "0");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        assert (compiledStatement.evaluate(moveData, null));
    }
    /**
     * Testing the Statement Verb MOVE_LENGTH when the obj is 1.
     * If there is a single move, then the evaluation should be true with the Statement Type IF.
     * If there is a single move, then the evaluation should be false with the Statement Type NIF.
     * If there are two moves, then the evaluation should be false with the Statement Type IF.
     * If there are two moves, then the evaluation should be true with the Statement Type NIF.
     */
    @Test
    void moveLength_Test2(){
        RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.MOVE_LENGTH, "1");
        LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);


        //MOVE FROM 0,0 TO 1,1
        Player p = Andrea;
        List<Point> moves = new ArrayList<>();

        moves.add(new Point(1,1));

        MoveData moveData = new MoveData(p, AndreaW1, moves);

        assert (compiledStatement.evaluate(moveData, null));

        moves.add(new Point(2,2));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (!compiledStatement.evaluate(moveData, null));

        moves.clear();

        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.MOVE_LENGTH, "1");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        moves.add(new Point(1,1));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (!compiledStatement.evaluate(moveData, null));

        moves.add(new Point(2,2));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (compiledStatement.evaluate(moveData, null));

    }
    /**
     * Testing the Statement Verb MOVE_LENGTH when the obj is 2.
     * If there is a single move, then the evaluation should be false with the Statement Type IF.
     * If there is a single move, then the evaluation should be true with the Statement Type NIF.
     * If there are two moves, then the evaluation should be true with the Statement Type IF.
     * If there are two moves, then the evaluation should be false with the Statement Type NIF.
     * If there are more than two moves, then the evaluation should be false with the Statement Type IF, true with NIF.
     */
    @Test
    void moveLength_Test3(){
        RuleStatement playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.MOVE_LENGTH, "2");
        LambdaStatement compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        //MOVE FROM 0,0 TO 2,2 WITH TWO ADJACENT MOVES

        Player p = Andrea;
        List<Point> moves = new ArrayList<>();

        moves.add(new Point(1,1));

        MoveData moveData = new MoveData(p, AndreaW1, moves);

        assert (!compiledStatement.evaluate(moveData, null));

        moves.add(new Point(2,2));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (compiledStatement.evaluate(moveData, null));

        moves.add(new Point(3,3));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (!compiledStatement.evaluate(moveData, null));

        moves.add(new Point(4,4));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (!compiledStatement.evaluate(moveData, null));

        moves.clear();


        playerEqualsStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.MOVE_LENGTH, "2");
        compiledStatement = StatementCompiler.compileStatement(model, playerEqualsStatement, Andrea);

        moves.add(new Point(1,1));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (compiledStatement.evaluate(moveData, null));

        moves.add(new Point(2,2));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (!compiledStatement.evaluate(moveData, null));

        moves.add(new Point(3,3));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (compiledStatement.evaluate(moveData, null));

        moves.add(new Point(4,4));

        moveData = new MoveData(p, AndreaW1, moves);

        assert (compiledStatement.evaluate(moveData, null));
    }
    /*
        Testing with a board having 0 buildings on it that exists delta more than 0 is always false
        moving without touching neither workers nor domes
     */
    @Test
    void existsDeltaMore_Test1(){
/*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 | 3  |
        +----+----+----+----+----+
    4   |    |    | D1 | 2  | 1  |
        +----+----+----+----+----+
*/


        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(4,4);
        Point point2 = new Point(3,4);
        Point point3 = new Point(4, 3);
        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        MoveData moveData = new MoveData(Mirko, MirkoW2, moves);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_MORE,"0");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertFalse(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_DELTA_MORE,"0");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertTrue(lambdaStatement.evaluate(moveData, null));

    }
    /*
        Testing with a board having 0 buildings on it that NIF esxists delta more than 0 is always true
        moving without touching domes but touching workers
     */
    @Test
    void existsDeltaMore_Test2() {
 /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/



        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(3,2);
        Point point2 = new Point(2,2);
        Point point3 = new Point(1, 2);
        Point point4 = new Point(0, 2);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        MoveData moveData = new MoveData(Mirko, MirkoW2, moves);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_DELTA_MORE,"0");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_MORE,"0");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertFalse(lambdaStatement.evaluate(moveData, null));

    }
    /*
        Testing in a path with some first floors that exists delta more than one
        is always false
     */
    @Test
    void existsDeltaMore_Test3(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | FF |    | FF | FF |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(3, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(4, 0)).addBuilding(BuildingType.FIRST_FLOOR);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_MORE,"0");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(2,0);
        Point point3 = new Point(3, 0);
        Point point4 = new Point(4, 0);
        Point point5 = new Point(4, 1);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);
        assertTrue(lambdaStatement.evaluate(moveData, null));
    }
    /*
        Testing in a path with some first floors and second floors that exists delta more than 2
         is always false
     */
    @Test
    void existsDeltaMore_Test4(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | SF |    | FF | SF |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(3, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(4, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(4, 0)).addBuilding(BuildingType.SECOND_FLOOR);


        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_MORE,"2");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(2,0);
        Point point3 = new Point(3, 0);
        Point point4 = new Point(4, 0);
        Point point5 = new Point(4, 1);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);
        assertFalse(lambdaStatement.evaluate(moveData, null));
    }
    /*
        Testing in a path with some first floors and second floors and third floors that exists delta more than 3
         is always false
     */
    @Test
    void existsDeltaMore_Test5(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | SF |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        |    | TF | FF | TF |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.SECOND_FLOOR);

        String id = model.getBoard().getCell(new Point(1, 2)).getWorkerID();
        model.getBoard().getCell(new Point(1, 2)).removeWorker();
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).setWorker(id);

        id = model.getBoard().getCell(new Point(2, 2)).getWorkerID();
        model.getBoard().getCell(new Point(2, 2)).removeWorker();
        model.getBoard().getCell(new Point(2, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(2, 2)).setWorker(id);

        id = model.getBoard().getCell(new Point(3, 2)).getWorkerID();
        model.getBoard().getCell(new Point(3, 2)).removeWorker();
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).setWorker(id);



        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(1,1);
        Point point3 = new Point(1,2);
        Point point4 = new Point(2, 2);
        Point point5 = new Point(3, 2);
        Point point6 = new Point(4, 2);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        moves.add(point6);
        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_MORE,"2");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_DELTA_MORE,"2");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(moveData, null));


    }
    /*
       Testing in a path with some first floors and second floors and third floors that
        it doesnt exist a delta more than 3
        or that exists delta more than 2 is true
    */
    @Test
    void existsDeltaMore_Test6(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | SF |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        |    | FF |    | TF |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.SECOND_FLOOR);


        String id = model.getBoard().getCell(new Point(1, 2)).getWorkerID();
        model.getBoard().getCell(new Point(1, 2)).removeWorker();
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).setWorker(id);



        id = model.getBoard().getCell(new Point(3, 2)).getWorkerID();
        model.getBoard().getCell(new Point(3, 2)).removeWorker();
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).setWorker(id);


        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_DELTA_MORE,"3");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(1,1);
        Point point3 = new Point(1,2);
        Point point4 = new Point(2, 2);
        Point point5 = new Point(3, 2);
        Point point6 = new Point(4, 2);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        moves.add(point6);
        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);
        assertTrue(lambdaStatement.evaluate(moveData, null));
    }
    /*
        Testing with a board having 0 buildings on it that esxists delta less than 0 is always false
        moving without touching neither workers nor domes
     */
    @Test
    void existsDeltaLess_Test1(){
/*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 | 3  |
        +----+----+----+----+----+
    4   |    |    | D1 | 2  | 1  |
        +----+----+----+----+----+
*/
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_LESS,"0");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(4,4);
        Point point2 = new Point(3,4);
        Point point3 = new Point(4, 3);
        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        MoveData moveData = new MoveData(Mirko, MirkoW2, moves);
        assertFalse(lambdaStatement.evaluate(moveData, null));

    }
    /*
        Testing with a board having 0 buildings on it that NIF esxists delta less than 0 is always true
        moving without touching domes but touching workers
     */
    @Test
    void existsDeltaLess_Test2() {
 /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_DELTA_LESS,"0");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(3,2);
        Point point2 = new Point(2,2);
        Point point3 = new Point(1, 2);
        Point point4 = new Point(0, 2);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        MoveData moveData = new MoveData(Mirko, MirkoW2, moves);
        assertTrue(lambdaStatement.evaluate(moveData, null));

    }
    /*
        Testing in a path with some first floors that exists delta less than -1
        is always false
     */
    @Test
    void existsDeltaLess_Test3(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | FF |    | FF | FF |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(3, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(4, 0)).addBuilding(BuildingType.FIRST_FLOOR);



        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(2,0);
        Point point3 = new Point(3, 0);
        Point point4 = new Point(4, 0);
        Point point5 = new Point(4, 1);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_DELTA_LESS,"-1");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_LESS,"-1");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertFalse(lambdaStatement.evaluate(moveData, null));
    }
    /*
         Testing in a path with some first floors and second floors that exists delta less than -2
         is always false
     */
    @Test
    void existsDeltaLess_Test4(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | SF |    | FF | SF |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(3, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(4, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(4, 0)).addBuilding(BuildingType.SECOND_FLOOR);


        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_LESS,"-2");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(2,0);
        Point point3 = new Point(3, 0);
        Point point4 = new Point(4, 0);
        Point point5 = new Point(4, 1);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);
        assertFalse(lambdaStatement.evaluate(moveData, null));
    }
    /*
        Testing in a path with some first floors and second floors and third floors that exists delta less than -3
         is always false
     */
    @Test
    void existsDeltaLess_Test5(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | SF |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        |    | TF | FF | TF |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.SECOND_FLOOR);

        String id = model.getBoard().getCell(new Point(1, 2)).getWorkerID();
        model.getBoard().getCell(new Point(1, 2)).removeWorker();
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).setWorker(id);

        id = model.getBoard().getCell(new Point(2, 2)).getWorkerID();
        model.getBoard().getCell(new Point(2, 2)).removeWorker();
        model.getBoard().getCell(new Point(2, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(2, 2)).setWorker(id);

        id = model.getBoard().getCell(new Point(3, 2)).getWorkerID();
        model.getBoard().getCell(new Point(3, 2)).removeWorker();
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).setWorker(id);


        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_LESS,"-3");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(1,1);
        Point point3 = new Point(1,2);
        Point point4 = new Point(2, 2);
        Point point5 = new Point(3, 2);
        Point point6 = new Point(4, 2);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        moves.add(point6);
        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);
        assertFalse(lambdaStatement.evaluate(moveData, null));
    }
    /*
       Testing in a path with some first floors and second floors and third floors that
        it doesnt exist a delta less than -3
        or that exists delta less than -2 is true
    */
    @Test
    void existsDeltaLess_Test6(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | SF |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        |    | FF |    | TF |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.SECOND_FLOOR);


        String id = model.getBoard().getCell(new Point(1, 2)).getWorkerID();
        model.getBoard().getCell(new Point(1, 2)).removeWorker();
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).setWorker(id);



        id = model.getBoard().getCell(new Point(3, 2)).getWorkerID();
        model.getBoard().getCell(new Point(3, 2)).removeWorker();
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).setWorker(id);




        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(1,1);
        Point point3 = new Point(1,2);
        Point point4 = new Point(2, 2);
        Point point5 = new Point(3, 2);
        Point point6 = new Point(4, 2);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        moves.add(point6);
        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);


        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_DELTA_LESS,"-2");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_DELTA_LESS,"-3");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(moveData, null));
    }
    /**
     * Testing exists level type when moving from two points [GROUND] -> [FIRST_FLOOR]
     * Should return true with IF and false with NIF
     */
    @Test
    void existsLevelType_Test1(){
        RuleStatement ruleStmFirstIF = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_LEVEL_TYPE,"FIRST_FLOOR");
        RuleStatement ruleStmFirstNIF = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_LEVEL_TYPE,"FIRST_FLOOR");
        LambdaStatement compiledStmFirstIF = StatementCompiler.compileStatement(model, ruleStmFirstIF, Andrea);
        LambdaStatement compiledStmFirstNIF = StatementCompiler.compileStatement(model, ruleStmFirstNIF, Andrea);

        /*
                  0    1     2    3    4
                +----+----+----+----+----+ X
            0   | A1 | FF |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */
        Board board = model.getBoard();

        Point p00 = new Point(0,0);
        board.getCell(p00).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(p00);

        Point p01 = new Point(1,0);
        board.getCell(p01).addBuilding(BuildingType.FIRST_FLOOR);

        //Generate move info
        List<Point> movePoints = new LinkedList<>();
        movePoints.add(p01);
        MoveData moveData = new MoveData(Andrea, AndreaW1, movePoints);

        assertTrue(compiledStmFirstIF.evaluate(moveData,null));
        assertFalse(compiledStmFirstNIF.evaluate(moveData,null));
    }
    /**
     * Testing exists level type when moving from two points [GROUND] -> [FIRST_FLOOR] -> [GROUND]
     * Should return true with IF and false with NIF
     */
    @Test
    void existsLevelType_Test2(){
        RuleStatement ruleStmFirstIF = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_LEVEL_TYPE,"FIRST_FLOOR");
        RuleStatement ruleStmFirstNIF = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_LEVEL_TYPE,"FIRST_FLOOR");
        LambdaStatement compiledStmFirstIF = StatementCompiler.compileStatement(model, ruleStmFirstIF, Andrea);
        LambdaStatement compiledStmFirstNIF = StatementCompiler.compileStatement(model, ruleStmFirstNIF, Andrea);

        /*
                  0    1     2    3    4
                +----+----+----+----+----+ X
            0   | A1 | FF |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */
        Board board = model.getBoard();

        Point p00 = new Point(0,0);
        board.getCell(p00).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(p00);

        Point p01 = new Point(1,0);
        board.getCell(p01).addBuilding(BuildingType.FIRST_FLOOR);

        Point p11 = new Point(1,1);

        //Generate move info
        List<Point> movePoints = new LinkedList<>();
        movePoints.add(p01);
        movePoints.add(p11);
        MoveData moveData = new MoveData(Andrea, AndreaW1, movePoints);

        assertTrue(compiledStmFirstIF.evaluate(moveData,null));
        assertFalse(compiledStmFirstNIF.evaluate(moveData,null));
    }
    /**
     *  Testing exists level type when moving from two points [FIRST_FLOOR] -> [GROUND]
     *  Should return false with IF and true with NIF
     */
    @Test
    void existsLevelType_Test3(){
        RuleStatement ruleStmFirstIF = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.EXISTS_LEVEL_TYPE,"FIRST_FLOOR");
        RuleStatement ruleStmFirstNIF = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.EXISTS_LEVEL_TYPE,"FIRST_FLOOR");
        LambdaStatement compiledStmFirstIF = StatementCompiler.compileStatement(model, ruleStmFirstIF, Andrea);
        LambdaStatement compiledStmFirstNIF = StatementCompiler.compileStatement(model, ruleStmFirstNIF, Andrea);

        /*
                  0    1     2    3    4
                +----+----+----+----+----+ X
            0   | A1 | FF |    |    |    |
                +----+----+----+----+----+
            1   |    |    |    |    |    |
                +----+----+----+----+----+
            2   |    |    |    |    |    |
                +----+----+----+----+----+
            3   |    |    |    |    |    |
                +----+----+----+----+----+
            4   |    |    |    |    |    |
                +----+----+----+----+----+
            Y
        */
        Board board = model.getBoard();

        Point p00 = new Point(0,0);
        board.getCell(p00).addBuilding(BuildingType.FIRST_FLOOR);

        Point p01 = new Point(1,0);
        board.getCell(p01).setWorker(AndreaW1.getID());
        AndreaW1.setPosition(p01);

        //Generate move info
        List<Point> movePoints = new LinkedList<>();
        movePoints.add(p00);
        MoveData moveData = new MoveData(Andrea, AndreaW1, movePoints);

        assertFalse(compiledStmFirstIF.evaluate(moveData,null));
        assertTrue(compiledStmFirstNIF.evaluate(moveData,null));
    }
    /*
       Tests a move with 3 interactions on a wide range of different levels
    */
    @Test
    void interactionNum_Test1(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | SF |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        |    | TF | FF | TF |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.SECOND_FLOOR);

        String id = model.getBoard().getCell(new Point(1, 2)).getWorkerID();
        model.getBoard().getCell(new Point(1, 2)).removeWorker();
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).setWorker(id);

        id = model.getBoard().getCell(new Point(2, 2)).getWorkerID();
        model.getBoard().getCell(new Point(2, 2)).removeWorker();
        model.getBoard().getCell(new Point(2, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(2, 2)).setWorker(id);

        id = model.getBoard().getCell(new Point(3, 2)).getWorkerID();
        model.getBoard().getCell(new Point(3, 2)).removeWorker();
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).setWorker(id);


        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.INTERACTION_NUM,"3");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(1,1);
        Point point3 = new Point(1,2);
        Point point4 = new Point(2, 2);
        Point point5 = new Point(3, 2);
        Point point6 = new Point(4, 2);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        moves.add(point6);
        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);
        assertTrue(lambdaStatement.evaluate(moveData, null));
    }
    /*
       Tests a move with 7 interactions on a wide range of different levels
       in a sequance of move that touches some cells 2 times
    */
    @Test
    void interactionNum_Test2(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 | SF |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        |    | TF | FF | TF |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 0)).addBuilding(BuildingType.SECOND_FLOOR);

        String id = model.getBoard().getCell(new Point(1, 2)).getWorkerID();
        model.getBoard().getCell(new Point(1, 2)).removeWorker();
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(1, 2)).setWorker(id);

        id = model.getBoard().getCell(new Point(2, 2)).getWorkerID();
        model.getBoard().getCell(new Point(2, 2)).removeWorker();
        model.getBoard().getCell(new Point(2, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(2, 2)).setWorker(id);

        id = model.getBoard().getCell(new Point(3, 2)).getWorkerID();
        model.getBoard().getCell(new Point(3, 2)).removeWorker();
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(3, 2)).setWorker(id);


        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,0);
        Point point2 = new Point(1,1);
        Point point3 = new Point(1,2);
        Point point4 = new Point(2, 2);
        Point point5 = new Point(3, 2);
        Point point6 = new Point(3, 3);
        Point point7 = new Point(2, 4);
        Point point8 = new Point(3, 3);
        Point point9 = new Point(3, 2);




        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);
        moves.add(point5);
        moves.add(point6);
        moves.add(point7);
        moves.add(point8);
        moves.add(point9);

        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"YOU", StatementVerbType.INTERACTION_NUM,"7");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertFalse(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"YOU", StatementVerbType.INTERACTION_NUM,"7");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(moveData, null));
    }
    /*
       Test position equals when concluding a move in the start position
    */
    @Test
    void positionEquals_Test1(){

        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,1);
        Point point2 = new Point(0,1);
        Point point3 = new Point(0,2);
        Point point4 = new Point(1, 2);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);

        MoveData moveData = new MoveData(Matteo, MatteoW1, moves);

        RuleStatement ruleStatement1 = RuleStatementImplTest.getStatement(StatementType.IF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "START_POSITION");
        RuleStatement ruleStatement2 = RuleStatementImplTest.getStatement(StatementType.IF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "OPPONENTS");
        LambdaStatement lambdaStatement1 = StatementCompiler.compileStatement(model, ruleStatement1, Matteo);
        LambdaStatement lambdaStatement2 = StatementCompiler.compileStatement(model, ruleStatement2, Matteo);

        assertTrue(lambdaStatement1.evaluate(moveData, null));
        assertFalse(lambdaStatement2.evaluate(moveData, null));

        ruleStatement1 = RuleStatementImplTest.getStatement(StatementType.NIF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "START_POSITION");
        ruleStatement2 = RuleStatementImplTest.getStatement(StatementType.NIF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "OPPONENTS");
        lambdaStatement1 = StatementCompiler.compileStatement(model, ruleStatement1, Matteo);
        lambdaStatement2 = StatementCompiler.compileStatement(model, ruleStatement2, Matteo);

        assertFalse(lambdaStatement1.evaluate(moveData, null));
        assertTrue(lambdaStatement2.evaluate(moveData, null));
    }
    /*
      Test position equals when concluding a move in an opponents position
   */
    @Test
    void positionEquals_Test2(){

        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,1);
        Point point2 = new Point(2,1);
        Point point3 = new Point(2,2);

        moves.add(point1);
        moves.add(point2);
        moves.add(point3);


        MoveData moveData = new MoveData(Matteo, MatteoW1, moves);

        RuleStatement ruleStatement1 = RuleStatementImplTest.getStatement(StatementType.IF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "START_POSITION");
        RuleStatement ruleStatement2 = RuleStatementImplTest.getStatement(StatementType.IF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "OPPONENTS");
        LambdaStatement lambdaStatement1 = StatementCompiler.compileStatement(model, ruleStatement1, Matteo);
        LambdaStatement lambdaStatement2 = StatementCompiler.compileStatement(model, ruleStatement2, Matteo);

        assertFalse(lambdaStatement1.evaluate(moveData, null));
        assertTrue(lambdaStatement2.evaluate(moveData, null));

        ruleStatement1 = RuleStatementImplTest.getStatement(StatementType.NIF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "START_POSITION");
        ruleStatement2 = RuleStatementImplTest.getStatement(StatementType.NIF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "OPPONENTS");
        lambdaStatement1 = StatementCompiler.compileStatement(model, ruleStatement1, Matteo);
        lambdaStatement2 = StatementCompiler.compileStatement(model, ruleStatement2, Matteo);

        assertTrue(lambdaStatement1.evaluate(moveData, null));
        assertFalse(lambdaStatement2.evaluate(moveData, null));
    }
    /*
        Test position equals when concluding a move in an empty cell
    */
    @Test
    void positionEquals_Test3(){

        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,1);
        Point point2 = new Point(2,1);


        moves.add(point1);
        moves.add(point2);


        MoveData moveData = new MoveData(Matteo, MatteoW1, moves);

        RuleStatement ruleStatement1 = RuleStatementImplTest.getStatement(StatementType.IF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "START_POSITION");
        RuleStatement ruleStatement2 = RuleStatementImplTest.getStatement(StatementType.IF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "OPPONENTS");
        LambdaStatement lambdaStatement1 = StatementCompiler.compileStatement(model, ruleStatement1, Matteo);
        LambdaStatement lambdaStatement2 = StatementCompiler.compileStatement(model, ruleStatement2, Matteo);

        assertFalse(lambdaStatement1.evaluate(moveData, null));
        assertFalse(lambdaStatement2.evaluate(moveData, null));

        ruleStatement1 = RuleStatementImplTest.getStatement(StatementType.NIF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "START_POSITION");
        ruleStatement2 = RuleStatementImplTest.getStatement(StatementType.NIF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "OPPONENTS");
        lambdaStatement1 = StatementCompiler.compileStatement(model, ruleStatement1, Matteo);
        lambdaStatement2 = StatementCompiler.compileStatement(model, ruleStatement2, Matteo);

        assertTrue(lambdaStatement1.evaluate(moveData, null));
        assertTrue(lambdaStatement2.evaluate(moveData, null));
    }
    /*
        Test position equals when concluding a move in a cell with your other worker
    */
    @Test
    void positionEquals_Test4(){

        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,1);
        Point point2 = new Point(2,1);
        Point point3 = new Point(3,1);
        Point point4 = new Point(3,2);


        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        moves.add(point4);


        MoveData moveData = new MoveData(Matteo, MatteoW1, moves);

        RuleStatement ruleStatement1 = RuleStatementImplTest.getStatement(StatementType.IF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "START_POSITION");
        RuleStatement ruleStatement2 = RuleStatementImplTest.getStatement(StatementType.IF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "OPPONENTS");
        LambdaStatement lambdaStatement1 = StatementCompiler.compileStatement(model, ruleStatement1, Matteo);
        LambdaStatement lambdaStatement2 = StatementCompiler.compileStatement(model, ruleStatement2, Matteo);

        assertFalse(lambdaStatement1.evaluate(moveData, null));
        assertFalse(lambdaStatement2.evaluate(moveData, null));

        ruleStatement1 = RuleStatementImplTest.getStatement(StatementType.NIF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "START_POSITION");
        ruleStatement2 = RuleStatementImplTest.getStatement(StatementType.NIF, "FINAL_POSITION", StatementVerbType.POSITION_EQUALS, "OPPONENTS");
        lambdaStatement1 = StatementCompiler.compileStatement(model, ruleStatement1, Matteo);
        lambdaStatement2 = StatementCompiler.compileStatement(model, ruleStatement2, Matteo);

        assertTrue(lambdaStatement1.evaluate(moveData, null));
        assertTrue(lambdaStatement2.evaluate(moveData, null));
    }
    /*
          Tests build num with 1 building
    */
    @Test
    void buildNum_Test1(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        Point point1 = new Point(2,3);
        buildOrder.add(point1);
        List<BuildingType> buildsInPoint = new ArrayList<>();
        buildsInPoint.add(BuildingType.DOME);
        builds.put(point1, buildsInPoint);



        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_NUM, "1");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_NUM, "1");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

        assertFalse(lambdaStatement.evaluate(null, buildData));

        for(int i =0; i<1; i++){
            ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_NUM, String.valueOf(i));
            lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            assertFalse(lambdaStatement.evaluate(null, buildData));

            ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_NUM, String.valueOf(i));
            lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            assertTrue(lambdaStatement.evaluate(null, buildData));
        }
        for(int i =2; i<10; i++){
            ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_NUM, String.valueOf(i));
            lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            assertFalse(lambdaStatement.evaluate(null, buildData));

            ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_NUM, String.valueOf(i));
            lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            assertTrue(lambdaStatement.evaluate(null, buildData));
        }



    }
    /*
        Tests build num with a lot of buildings
     */
    @Test
    void buildNum_Test2(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        Point point1 = new Point(2,3);
        buildOrder.add(point1);
        List<BuildingType> buildsInPoint = new ArrayList<>();
        buildsInPoint.add(BuildingType.DOME);
        builds.put(point1, buildsInPoint);

        Point point2 = new Point(1,3);
        buildOrder.add(point2);
        List<BuildingType> buildsInPoint2 = new ArrayList<>();
        buildsInPoint2.add(BuildingType.FIRST_FLOOR);
        buildsInPoint2.add(BuildingType.DOME);
        builds.put(point2, buildsInPoint2);

        Point point3 = new Point(0,3);
        buildOrder.add(point3);
        List<BuildingType> buildsInPoint3 = new ArrayList<>();
        buildsInPoint3.add(BuildingType.FIRST_FLOOR);
        buildsInPoint3.add(BuildingType.SECOND_FLOOR);
        buildsInPoint3.add(BuildingType.DOME);
        builds.put(point3, buildsInPoint3);

        Point point4 = new Point(0, 2);
        buildOrder.add(point4);
        List<BuildingType> buildsInPoint4 = new ArrayList<>();
        buildsInPoint4.add(BuildingType.FIRST_FLOOR);
        buildsInPoint4.add(BuildingType.SECOND_FLOOR);
        buildsInPoint4.add(BuildingType.THIRD_FLOOR);
        buildsInPoint4.add(BuildingType.DOME);
        builds.put(point4, buildsInPoint4);



        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_NUM, "10");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_NUM, "10");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

        assertFalse(lambdaStatement.evaluate(null, buildData));

        for(int i =0; i<10; i++){
            ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_NUM, String.valueOf(i));
            lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            assertFalse(lambdaStatement.evaluate(null, buildData));

            ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_NUM, String.valueOf(i));
            lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            assertTrue(lambdaStatement.evaluate(null, buildData));
        }


    }
     /*
       This test verifies that build dome except returns false when called with ground as object
       and the player wants to build a dome on the ground
       returns true with all other objects
   */
    @Test
    void buildDomeExcept_Test1(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.FIRST_FLOOR);

        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.SECOND_FLOOR);


        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.THIRD_FLOOR);


        for(LevelType lt : LevelType.values()) {
            if (lt == LevelType.DOME) {
                break;
            }
            RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_DOME_EXCEPT, lt.toString());
            LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            Map<Point,List<BuildingType>> builds = new HashMap<>();
            List<Point> buildOrder = new LinkedList<>();

            Point point1 = new Point(2,3);
            buildOrder.add(point1);
            List<BuildingType> buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point1, buildsInPoint);

          /*  Point point2 = new Point(1,3);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point2, buildsInPoint);

            Point point3 = new Point(0,3);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point3, buildsInPoint);

            Point point4 = new Point(0, 2);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point4, buildsInPoint);*/

           /* Point point5 = new Point(0, 1);
            Point point6 = new Point(1, 1);
            Point point7 = new Point(2,1);*/

            BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

            if(lt == LevelType.GROUND)
                assertFalse(lambdaStatement.evaluate(null, buildData));
            else
                assertTrue(lambdaStatement.evaluate(null, buildData));
        }
    }
    /*
       This test verifies that build dome except returns always true when
       a player wants to build two domes on two different levels
    */
    @Test
    void buildDomeExcept_Test2(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        for(LevelType lt : LevelType.values()) {
            if (lt == LevelType.DOME) {
                break;
            }
            RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_DOME_EXCEPT, lt.toString());
            LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            Map<Point,List<BuildingType>> builds = new HashMap<>();
            List<Point> buildOrder = new LinkedList<>();

       /*     Point point1 = new Point(2,3);
            List<BuildingType> buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point1, buildsInPoint);*/

            /*Point point2 = new Point(1,3);
            List<BuildingType> buildsInPoint2 = new ArrayList<>();
            buildsInPoint2.add(BuildingType.FIRST_FLOOR);
            buildsInPoint2.add(BuildingType.DOME);
            builds.put(point2, buildsInPoint2);*/

            Point point3 = new Point(0,3);
            buildOrder.add(point3);
            List<BuildingType> buildsInPoint3 = new ArrayList<>();
            buildsInPoint3.add(BuildingType.FIRST_FLOOR);
            buildsInPoint3.add(BuildingType.SECOND_FLOOR);
            buildsInPoint3.add(BuildingType.DOME);
            builds.put(point3, buildsInPoint3);

            Point point4 = new Point(0, 2);
            buildOrder.add(point4);
            List<BuildingType> buildsInPoint4 = new ArrayList<>();
            buildsInPoint4.add(BuildingType.FIRST_FLOOR);
            buildsInPoint4.add(BuildingType.SECOND_FLOOR);
            buildsInPoint4.add(BuildingType.THIRD_FLOOR);
            buildsInPoint4.add(BuildingType.DOME);
            builds.put(point4, buildsInPoint4);


            BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

            assertFalse(lambdaStatement.evaluate(null, buildData));

        }
    }
    /*
       This test verifies that build dome except returns false when called with second level as object
       and the player wants to build a dome on the second level
       returns true with all other objects
   */
    @Test
    void buildDomeExcept_Test3(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        model.getBoard().getCell(new Point(2, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(2, 3)).addBuilding(BuildingType.SECOND_FLOOR);

        model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.SECOND_FLOOR);

        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.SECOND_FLOOR);

        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.SECOND_FLOOR);


        for(LevelType lt : LevelType.values()) {
            if (lt == LevelType.DOME) {
                break;
            }
            RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_DOME_EXCEPT, lt.toString());
            LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            Map<Point,List<BuildingType>> builds = new HashMap<>();
            List<Point> buildOrder = new LinkedList<>();

            Point point1 = new Point(2,3);
            buildOrder.add(point1);
            List<BuildingType> buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point1, buildsInPoint);

            Point point2 = new Point(1,3);
            buildOrder.add(point2);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point2, buildsInPoint);

            Point point3 = new Point(0,3);
            buildOrder.add(point3);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point3, buildsInPoint);

            Point point4 = new Point(0, 2);
            buildOrder.add(point4);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point4, buildsInPoint);

          /*  Point point5 = new Point(0, 1);
            Point point6 = new Point(1, 1);
            Point point7 = new Point(2,1);*/


            BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

            if(lt == LevelType.SECOND_FLOOR)
                assertFalse(lambdaStatement.evaluate(null, buildData));
            else
                assertTrue(lambdaStatement.evaluate(null, buildData));

        }
    }
    /*
       This test verifies that build dome except returns always true when
       a player wants to build a dome on a different level from object
   */
    @Test
    void buildDomeExcept_Test4(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   | DM | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   | TF | SF | FF | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        model.getBoard().getCell(new Point(2, 3)).addBuilding(BuildingType.FIRST_FLOOR);

        model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.SECOND_FLOOR);

        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.THIRD_FLOOR);

        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.DOME);



        for(LevelType lt : LevelType.values()) {
            if (lt == LevelType.DOME) {
                break;
            }


            Map<Point,List<BuildingType>> builds = new HashMap<>();
            List<Point> buildOrder = new LinkedList<>();
            List<BuildingType> buildsInPoint = new ArrayList<>();

            if(lt == LevelType.THIRD_FLOOR) {
                Point point1 = new Point(2, 3);
                buildOrder.add(point1);
                buildsInPoint.add(BuildingType.DOME);
                builds.put(point1, buildsInPoint);
            }

            if(lt == LevelType.SECOND_FLOOR) {
                Point point2 = new Point(1, 3);
                buildOrder.add(point2);
                buildsInPoint = new ArrayList<>();
                buildsInPoint.add(BuildingType.DOME);
                builds.put(point2, buildsInPoint);
            }

            if(lt == LevelType.FIRST_FLOOR) {
                Point point3 = new Point(0, 3);
                buildOrder.add(point3);
                buildsInPoint = new ArrayList<>();
                buildsInPoint.add(BuildingType.DOME);
                builds.put(point3, buildsInPoint);
            }

            if(lt == LevelType.GROUND) {
                Point point4 = new Point(0, 2);
                buildOrder.add(point4);
                buildsInPoint = new ArrayList<>();
                buildsInPoint.add(BuildingType.DOME);
                builds.put(point4, buildsInPoint);
            }

            BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

            RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_DOME_EXCEPT, lt.toString());
            LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);
            if(lt == LevelType.SECOND_FLOOR)
                assertFalse(lambdaStatement.evaluate(null, buildData));
            else
                assertTrue(lambdaStatement.evaluate(null, buildData));

            ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_DOME_EXCEPT, lt.toString());
            lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);
            if(lt == LevelType.SECOND_FLOOR)
                assertTrue(lambdaStatement.evaluate(null, buildData));
            else
                assertFalse(lambdaStatement.evaluate(null, buildData));
        }
    }
    /*
          This test verifies that build dome returns true when a player
          wants to build only one dome on any type of object
      */
    @Test
    void buildDome_Test1(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/      model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.FIRST_FLOOR);

        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.SECOND_FLOOR);


        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.THIRD_FLOOR);


        for(LevelType lt : LevelType.values()) {
            if (lt == LevelType.DOME) {
                break;
            }

            Map<Point,List<BuildingType>> builds = new HashMap<>();
            List<Point> buildOrder = new LinkedList<>();

            Point point1 = new Point(2,3);
            buildOrder.add(point1);
            List<BuildingType> buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point1, buildsInPoint);

            Point point2 = new Point(1,3);
            buildOrder.add(point2);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point2, buildsInPoint);

            Point point3 = new Point(0,3);
            buildOrder.add(point3);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point3, buildsInPoint);

            Point point4 = new Point(0, 2);
            buildOrder.add(point4);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point4, buildsInPoint);


            BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

            RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_DOME, lt.toString());
            LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);
            assertTrue(lambdaStatement.evaluate(null, buildData));

            ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_DOME, lt.toString());
            lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);
            assertFalse(lambdaStatement.evaluate(null, buildData));
        }
    }
    /*
       This test verifies that build dome returns true when a player
       wants to build only one dome on the specified type of object
       and returns false in all other cases
    */
    @Test
    void buildDome_Test2(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/      model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.FIRST_FLOOR);

        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.SECOND_FLOOR);


        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.THIRD_FLOOR);


        for(LevelType lt : LevelType.values()) {
            if (lt == LevelType.DOME) {
                break;
            }
            RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_DOME, lt.toString());
            LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            Map<Point,List<BuildingType>> builds = new HashMap<>();
            List<Point> buildOrder = new LinkedList<>();

            /*Point point1 = new Point(2,3);
            List<BuildingType> buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point1, buildsInPoint);*/

            /*Point point2 = new Point(1,3);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point2, buildsInPoint);*/

            Point point3 = new Point(0,3);
            buildOrder.add(point3);
            List<BuildingType> buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point3, buildsInPoint);

          /*  Point point4 = new Point(0, 2);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point4, buildsInPoint);*/


            BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

            if(lt == LevelType.SECOND_FLOOR)
                assertFalse(lambdaStatement.evaluate(null, buildData));
            else
                assertTrue(lambdaStatement.evaluate(null, buildData));
        }
    }
    /*
       This test makes a player want to build only domes and
       only on third floors, having as statement objects all
       level types apart from third floors and domes, therefore the statement should always be false
    */
    @Test
    void buildDome_Test3(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        model.getBoard().getCell(new Point(2, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(2, 3)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(2, 3)).addBuilding(BuildingType.THIRD_FLOOR);

        model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(1, 3)).addBuilding(BuildingType.THIRD_FLOOR);

        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(0, 3)).addBuilding(BuildingType.THIRD_FLOOR);

        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(new Point(0, 2)).addBuilding(BuildingType.THIRD_FLOOR);


        for(LevelType lt : LevelType.values()) {
            if (lt == LevelType.DOME || lt == LevelType.THIRD_FLOOR) {
                break;
            }
            RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_DOME, lt.toString());
            LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            Map<Point,List<BuildingType>> builds = new HashMap<>();
            List<Point> buildOrder = new LinkedList<>();

            Point point1 = new Point(2,3);
            buildOrder.add(point1);
            List<BuildingType> buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point1, buildsInPoint);

            Point point2 = new Point(1,3);
            buildOrder.add(point2);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point2, buildsInPoint);

            Point point3 = new Point(0,3);
            buildOrder.add(point3);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point3, buildsInPoint);

            Point point4 = new Point(0, 2);
            buildOrder.add(point4);
            buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point4, buildsInPoint);

            BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

            assertFalse(lambdaStatement.evaluate(null, buildData));
        }
    }
    /*
       It is like build dome test 1 but this time everything
       is built by the player
    */
    @Test
    void buildDome_Test4(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        for(LevelType lt : LevelType.values()) {
            if (lt == LevelType.DOME) {
                break;
            }


            Map<Point,List<BuildingType>> builds = new HashMap<>();
            List<Point> buildOrder = new LinkedList<>();

            Point point1 = new Point(2,3);
            buildOrder.add(point1);
            List<BuildingType> buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.DOME);
            builds.put(point1, buildsInPoint);

            Point point2 = new Point(1,3);
            buildOrder.add(point2);
            List<BuildingType> buildsInPoint2 = new ArrayList<>();
            buildsInPoint2.add(BuildingType.FIRST_FLOOR);
            buildsInPoint2.add(BuildingType.DOME);
            builds.put(point2, buildsInPoint2);

            Point point3 = new Point(0,3);
            buildOrder.add(point3);
            List<BuildingType> buildsInPoint3 = new ArrayList<>();
            buildsInPoint3.add(BuildingType.FIRST_FLOOR);
            buildsInPoint3.add(BuildingType.SECOND_FLOOR);
            buildsInPoint3.add(BuildingType.DOME);
            builds.put(point3, buildsInPoint3);

            Point point4 = new Point(0, 2);
            buildOrder.add(point4);
            List<BuildingType> buildsInPoint4 = new ArrayList<>();
            buildsInPoint4.add(BuildingType.FIRST_FLOOR);
            buildsInPoint4.add(BuildingType.SECOND_FLOOR);
            buildsInPoint4.add(BuildingType.THIRD_FLOOR);
            buildsInPoint4.add(BuildingType.DOME);
            builds.put(point4, buildsInPoint4);


            BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

            RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_DOME, lt.toString());
            LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);
            assertTrue(lambdaStatement.evaluate(null, buildData));

            ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_DOME, lt.toString());
            lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);
            assertFalse(lambdaStatement.evaluate(null, buildData));
        }
    }
    /*
        The player builds dome on second and third level
   */
    @Test
    void buildDome_Test5(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/

        for(LevelType lt : LevelType.values()) {
            if (lt == LevelType.DOME) {
                break;
            }
            RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_DOME, lt.toString());
            LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

            Map<Point,List<BuildingType>> builds = new HashMap<>();
            List<Point> buildOrder = new LinkedList<>();

            Point point1 = new Point(2,3);
            buildOrder.add(point1);
            List<BuildingType> buildsInPoint = new ArrayList<>();
            buildsInPoint.add(BuildingType.FIRST_FLOOR);
            builds.put(point1, buildsInPoint);

            Point point2 = new Point(1,3);
            buildOrder.add(point2);
            List<BuildingType> buildsInPoint2 = new ArrayList<>();
            buildsInPoint2.add(BuildingType.FIRST_FLOOR);
            buildsInPoint2.add(BuildingType.SECOND_FLOOR);
            builds.put(point2, buildsInPoint2);

            Point point3 = new Point(0,3);
            buildOrder.add(point3);
            List<BuildingType> buildsInPoint3 = new ArrayList<>();
            buildsInPoint3.add(BuildingType.FIRST_FLOOR);
            buildsInPoint3.add(BuildingType.SECOND_FLOOR);
            buildsInPoint3.add(BuildingType.DOME);
            builds.put(point3, buildsInPoint3);

            Point point4 = new Point(0, 2);
            buildOrder.add(point4);
            List<BuildingType> buildsInPoint4 = new ArrayList<>();
            buildsInPoint4.add(BuildingType.FIRST_FLOOR);
            buildsInPoint4.add(BuildingType.SECOND_FLOOR);
            buildsInPoint4.add(BuildingType.THIRD_FLOOR);
            buildsInPoint4.add(BuildingType.DOME);
            builds.put(point4, buildsInPoint4);


            BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

            if(lt == LevelType.GROUND || lt == LevelType.FIRST_FLOOR)
                assertFalse(lambdaStatement.evaluate(null, buildData));
            else
                assertTrue(lambdaStatement.evaluate(null, buildData));

        }
    }
    /*
        STRANGE CASE
        Building a dome on a dome it is ok for the statement
        if dome is the object
    */
    @Test
    void buildDome_STRANGE_CASE(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        model.getBoard().getCell(new Point(2, 3)).addBuilding(BuildingType.DOME);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_DOME, "DOME");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        Point point1 = new Point(2,3);
        buildOrder.add(point1);
        List<BuildingType> buildsInPoint = new ArrayList<>();
        buildsInPoint.add(BuildingType.DOME);
        builds.put(point1, buildsInPoint);


        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        assertTrue(lambdaStatement.evaluate(null, buildData));
    }
    /*
        Testing building in same spot fails with a lot of buildings in diff spots
     */
    @Test
    void buildInSameSpot_Test1(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        Point point1 = new Point(2,3);
        buildOrder.add(point1);
        List<BuildingType> buildsInPoint = new ArrayList<>();
        buildsInPoint.add(BuildingType.DOME);
        builds.put(point1, buildsInPoint);

        Point point2 = new Point(1,3);
        buildOrder.add(point2);
        List<BuildingType> buildsInPoint2 = new ArrayList<>();
        buildsInPoint2.add(BuildingType.FIRST_FLOOR);
        buildsInPoint2.add(BuildingType.DOME);
        builds.put(point2, buildsInPoint2);

        Point point3 = new Point(0,3);
        buildOrder.add(point3);
        List<BuildingType> buildsInPoint3 = new ArrayList<>();
        buildsInPoint3.add(BuildingType.FIRST_FLOOR);
        buildsInPoint3.add(BuildingType.SECOND_FLOOR);
        buildsInPoint3.add(BuildingType.DOME);
        builds.put(point3, buildsInPoint3);

        Point point4 = new Point(0, 2);
        buildOrder.add(point4);
        List<BuildingType> buildsInPoint4 = new ArrayList<>();
        buildsInPoint4.add(BuildingType.FIRST_FLOOR);
        buildsInPoint4.add(BuildingType.SECOND_FLOOR);
        buildsInPoint4.add(BuildingType.THIRD_FLOOR);
        buildsInPoint4.add(BuildingType.DOME);
        builds.put(point4, buildsInPoint4);



        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_IN_SAME_SPOT, "ALL");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

        assertFalse(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_IN_SAME_SPOT, "ALL");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

        assertTrue(lambdaStatement.evaluate(null, buildData));

    }
    /*
        Testing build in same spot succeeds when building in just one spot
     */
    @Test
    void buildInSameSpot_Test2(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        Point point4 = new Point(0, 2);
        buildOrder.add(point4);
        List<BuildingType> buildsInPoint4 = new ArrayList<>();
        buildsInPoint4.add(BuildingType.FIRST_FLOOR);
        buildsInPoint4.add(BuildingType.SECOND_FLOOR);
        buildsInPoint4.add(BuildingType.THIRD_FLOOR);
        buildsInPoint4.add(BuildingType.DOME);
        builds.put(point4, buildsInPoint4);


        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.BUILD_IN_SAME_SPOT, "ALL");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.BUILD_IN_SAME_SPOT, "ALL");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Matteo);

        assertFalse(lambdaStatement.evaluate(null, buildData));
    }
    /*
          Testing IS_NEAR with subject START_POSITION
           when there are 3 players and the player moving
           starts his move near to only one of the other players
       */
    @Test
    void isNear_StartPosition_Test1(){

    /*
              0    1     2    3    4
            +----+----+----+----+----+
        0   | A1 |    |    |    |    |
            +----+----+----+----+----+
        1   |    |    |    |    |    |
            +----+----+----+----+----+
        2   | 1  | B1 | A2 | B2 |    |
            +----+----+----+----+----+
        3   | 2  |    |    | D2 |    |
            +----+----+----+----+----+
        4   |    |  3 | D1 |    |    |
            +----+----+----+----+----+
    */


        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(0,2);
        Point point2 = new Point(0,3);
        Point point3 = new Point(1, 4);
        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        MoveData moveData = new MoveData(Matteo, MatteoW1, moves);

        // START POSITION OF MATTEO W1 IS NEAR ANDREA
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"START_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"START_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertFalse(lambdaStatement.evaluate(moveData, null));

        // START POSITION OF MATTEO W1 IS NOT NEAR MIRKO
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"START_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertFalse(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"START_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertTrue(lambdaStatement.evaluate(moveData, null));



    }
    /*
          Testing IS_NEAR with subject START_POSITION
           when there are 3 players and the player moving
           starts his move near both the other players
       */
    @Test
    void isNear_StartPosition_Test2(){

        /*
              0    1     2    3    4
            +----+----+----+----+----+
        0   | A1 |    |    |    |    |
            +----+----+----+----+----+
        1   |    |    |    |    |    |
            +----+----+----+----+----+
        2   |    | B1 | A2 | B2 | 1  |
            +----+----+----+----+----+
        3   |    |    |    | D2 |    |
            +----+----+----+----+----+
        4   |    |    | D1 |    |    |
            +----+----+----+----+----+
    */

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(4,2);
        moves.add(point1);
        MoveData moveData = new MoveData(Matteo, MatteoW2, moves);


        // START POSITION OF MATTEO W2 IS NEAR ANDREA
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"START_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"START_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertFalse(lambdaStatement.evaluate(moveData, null));

        // START POSITION OF MATTEO W2 IS NEAR MIRKO
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"START_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"START_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertFalse(lambdaStatement.evaluate(moveData, null));

    }
    /*
        Testing IS_NEAR with subject FINAL_POSITION
         when there are 3 players and the player moving
         finishes his move near to only one of the other players
     */
    @Test
    void isNear_FinalPositionPosition_Test3(){

    /*
              0    1     2    3    4
            +----+----+----+----+----+
        0   | A1 |    |    |    |    |
            +----+----+----+----+----+
        1   |    |    |    |    |    |
            +----+----+----+----+----+
        2   | 1  | B1 | A2 | B2 |    |
            +----+----+----+----+----+
        3   | 2  |    |    | D2 |    |
            +----+----+----+----+----+
        4   |    |  3 | D1 |    |    |
            +----+----+----+----+----+
    */


        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(0,2);
        Point point2 = new Point(0,3);
        Point point3 = new Point(1, 4);
        moves.add(point1);
        moves.add(point2);
        moves.add(point3);
        MoveData moveData = new MoveData(Matteo, MatteoW1, moves);

        // FINAL POSITION OF MATTEO W1 IS NEAR MIRKO
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"FINAL_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"FINAL_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertFalse(lambdaStatement.evaluate(moveData, null));

        // FINAL POSITION OF MATTEO W1 IS NOT NEAR ANDREA
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"FINAL_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertFalse(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"FINAL_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(moveData, null));

    }
    /*
          Testing IS_NEAR with subject FINAL_POSITION
           when there are 3 players and the player moving
           finishes his move near both the other players
       */
    @Test
    void isNear_FinalPosition_Test4(){

        /*
              0    1     2    3    4
            +----+----+----+----+----+
        0   | A1 |    |    |    |    |
            +----+----+----+----+----+
        1   |    |    |    |    |    |
            +----+----+----+----+----+
        2   |    | B1 | A2 | B2 |    |
            +----+----+----+----+----+
        3   |    |    |  1 | D2 |    |
            +----+----+----+----+----+
        4   |    |    | D1 |    |    |
            +----+----+----+----+----+
    */

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(2,3);
        moves.add(point1);
        MoveData moveData = new MoveData(Matteo, MatteoW2, moves);


        // FINAL POSITION OF MATTEO W2 IS NEAR ANDREA
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"FINAL_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"FINAL_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertFalse(lambdaStatement.evaluate(moveData, null));

        // FINAL POSITION OF MATTEO W2 IS NEAR MIRKO
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"FINAL_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertTrue(lambdaStatement.evaluate(moveData, null));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"FINAL_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertFalse(lambdaStatement.evaluate(moveData, null));

    }
    /*
           Testing IS_NEAR with subject ONE_BUILD_POSITION
            when there are 3 players and the player does not build
            near to any other player
        */
    @Test
    void isNear_oneBuildPosition_Test5(){

    /*
              0    1     2    3    4
            +----+----+----+----+----+
        0   | A1 |    |    |    |    |
            +----+----+----+----+----+
        1   |    |    |    |    |    |
            +----+----+----+----+----+
        2   | SF | B1 | A2 | B2 |    |
            +----+----+----+----+----+
        3   |    |    |    | D2 |    |
            +----+----+----+----+----+
        4   |    |    | D1 |    |    |
            +----+----+----+----+----+
    */


        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<Point> buildsOrder = new ArrayList<>();

        builds.put(new Point(0,2), Arrays.asList(BuildingType.FIRST_FLOOR, BuildingType.SECOND_FLOOR));
        buildsOrder.add(new Point(0,2));

        BuildData buildData = new BuildData(Matteo,MatteoW1,builds, buildsOrder);

        // ONE BUILD POSITION POSITION OF MATTEO W1 IS NOT NEAR TO ANY OTHER PLAYER
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertFalse(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertFalse(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertTrue(lambdaStatement.evaluate(null, buildData));

    }
    /*
          Testing IS_NEAR with subject ONE_BUILD_POSITION
           when there are 3 players and the player builds
           near to one other player
       */
    @Test
    void isNear_oneBuildPosition_Test6(){

    /*
              0    1     2    3    4
            +----+----+----+----+----+
        0   | A1 |    |    |    |    |
            +----+----+----+----+----+
        1   |    | DM |    |    |    |
            +----+----+----+----+----+
        2   | SF | B1 | A2 | B2 |    |
            +----+----+----+----+----+
        3   |    |    |    | D2 |    |
            +----+----+----+----+----+
        4   |    |    | D1 |    |    |
            +----+----+----+----+----+
    */


        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<Point> buildsOrder = new ArrayList<>();

        builds.put(new Point(0,2), Arrays.asList(BuildingType.FIRST_FLOOR, BuildingType.SECOND_FLOOR));
        builds.put(new Point(1,1), Collections.singletonList(BuildingType.DOME));
        buildsOrder.add(new Point(0,2));
        buildsOrder.add(new Point(1,1));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildsOrder);

        // ONE BUILD POSITION POSITION OF MATTEO W1 IS NEAR ANDREA
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertFalse(lambdaStatement.evaluate(null, buildData));


        // NO BUILD POSITION POSITION OF MATTEO W1 IS NEAR MIRKO
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertFalse(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertTrue(lambdaStatement.evaluate(null, buildData));

    }
    /*
          Testing IS_NEAR with subject ONE_BUILD_POSITION
           when there are 3 players and the player builds
           near to both the other players
       */
    @Test
    void isNear_oneBuildPosition_Test7(){

    /*
              0    1     2    3    4
            +----+----+----+----+----+
        0   | A1 |    |    |    |    |
            +----+----+----+----+----+
        1   |    |    |    |    |    |
            +----+----+----+----+----+
        2   | SF | B1 | A2 | B2 |    |
            +----+----+----+----+----+
        3   |    |    | FF | D2 |    |
            +----+----+----+----+----+
        4   |    |    | D1 |    |    |
            +----+----+----+----+----+
    */


        Map<Point, List<BuildingType>> builds = new HashMap<>();
        List<Point> buildsOrder = new ArrayList<>();

        builds.put(new Point(0,2), Arrays.asList(BuildingType.FIRST_FLOOR, BuildingType.SECOND_FLOOR));
        builds.put(new Point(2,3), Collections.singletonList(BuildingType.FIRST_FLOOR));
        buildsOrder.add(new Point(0,2));
        buildsOrder.add(new Point(2,3));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildsOrder);

        // ONE BUILD POSITION POSITION OF MATTEO W1 IS NEAR ANDREA
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);
        assertFalse(lambdaStatement.evaluate(null, buildData));


        // ONE BUILD POSITION POSITION OF MATTEO W1 IS NEAR MIRKO
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF,"ONE_BUILD_POSITION", StatementVerbType.IS_NEAR,"CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);
        assertFalse(lambdaStatement.evaluate(null, buildData));

    }
    /*
            Tests ONLY_COMPLETE_TOWERS_NEAR
            when the player builds far
            from the card owner
         */
    @Test
    void onlyCompleteTowersNear_Test1(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    | FF | DM |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        builds.put(new Point(1,1), Collections.singletonList(BuildingType.FIRST_FLOOR));
        builds.put(new Point(2,1), Collections.singletonList(BuildingType.DOME));

        buildOrder.add(new Point(1,1));
        buildOrder.add(new Point(2,1));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        // MATTEO W1 BUILDS NOTHING NEAR THE CARD OWNER
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertFalse(lambdaStatement.evaluate(null, buildData));



    }
    /*
           Tests ONLY_COMPLETE_TOWERS_NEAR
           when the player builds
           near the card owner, but does not complete any tower
        */
    @Test
    void onlyCompleteTowersNear_Test2(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    | DM |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        builds.put(new Point(2,1), Collections.singletonList(BuildingType.DOME));

        buildOrder.add(new Point(2,1));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        // MATTEO W1 BUILDS NEAR TO THE CARD OWNER BUT DOES NOT COMPLETE ANY TOWER
        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(null, buildData));



    }
    /*
           Tests ONLY_COMPLETE_TOWERS_NEAR
           when the player builds
           near the card owner, completing one tower
        */
    @Test
    void onlyCompleteTowersNear_Test3(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    | DM |    |    |    |
        +----+----+----+----+----+
    2   | DM | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        builds.put(new Point(0,2), Collections.singletonList(BuildingType.DOME));
        builds.put(new Point(1,1), Arrays.asList(BuildingType.FIRST_FLOOR, BuildingType.SECOND_FLOOR, BuildingType.THIRD_FLOOR, BuildingType.DOME));

        buildOrder.add(new Point(0,2));
        buildOrder.add(new Point(1,1));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(null, buildData));

    }
    /*
          Tests ONLY_COMPLETE_TOWERS_NEAR
          when the player builds
          near the card owner, completing one tower
          but also building something else that does not
          complete a tower near him
       */
    @Test
    void onlyCompleteTowersNear_Test4(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   | DM | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    | SF | DM | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        builds.put(new Point(0,2), Collections.singletonList(BuildingType.DOME));
        builds.put(new Point(1,3), Arrays.asList(BuildingType.FIRST_FLOOR, BuildingType.SECOND_FLOOR));
        builds.put(new Point(2,3), Arrays.asList(BuildingType.FIRST_FLOOR, BuildingType.SECOND_FLOOR, BuildingType.THIRD_FLOOR, BuildingType.DOME));

        buildOrder.add(new Point(0,2));
        buildOrder.add(new Point(1,3));
        buildOrder.add(new Point(2,3));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertFalse(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertTrue(lambdaStatement.evaluate(null, buildData));

    }
    /*
        Tests ONLY_COMPLETE_TOWERS_NEAR
        when the player builds
        near the card owner, only completing towers near him
     */
    @Test
    void onlyCompleteTowersNear_Test5(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   | DM | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    | DM | DM | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        builds.put(new Point(0,2), Collections.singletonList(BuildingType.DOME));
        builds.put(new Point(1,3), Arrays.asList(BuildingType.FIRST_FLOOR, BuildingType.SECOND_FLOOR, BuildingType.THIRD_FLOOR, BuildingType.DOME));
        builds.put(new Point(2,3), Arrays.asList(BuildingType.FIRST_FLOOR, BuildingType.SECOND_FLOOR, BuildingType.THIRD_FLOOR, BuildingType.DOME));

        buildOrder.add(new Point(0,2));
        buildOrder.add(new Point(1,3));
        buildOrder.add(new Point(2,3));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.ONLY_COMPLETE_TOWERS_NEAR, "CARD_OWNER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertFalse(lambdaStatement.evaluate(null, buildData));

    }
    /*
       Tests LAST_BUILd_ON
       when the player does not build
       on the perimeter
     */
    @Test
    void lastBuildOn_Test1(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   |    | FF | DM |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        builds.put(new Point(1,1), Collections.singletonList(BuildingType.FIRST_FLOOR));
        builds.put(new Point(2,1), Collections.singletonList(BuildingType.DOME));

        buildOrder.add(new Point(1,1));
        buildOrder.add(new Point(2,1));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.LAST_BUILD_ON, "PERIMETER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertFalse(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.LAST_BUILD_ON, "PERIMETER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertTrue(lambdaStatement.evaluate(null, buildData));
    }
    /*
       Tests LAST_BUILd_ON
       when the player builds
       his last build on the perimeter
     */
    @Test
    void lastBuildOn_Test2(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   | FF | FF | DM |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        builds.put(new Point(1,1), Collections.singletonList(BuildingType.FIRST_FLOOR));
        builds.put(new Point(2,1), Collections.singletonList(BuildingType.DOME));
        builds.put(new Point(0,1), Collections.singletonList(BuildingType.FIRST_FLOOR));

        buildOrder.add(new Point(1,1));
        buildOrder.add(new Point(2,1));
        buildOrder.add(new Point(0,1));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.LAST_BUILD_ON, "PERIMETER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.LAST_BUILD_ON, "PERIMETER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertFalse(lambdaStatement.evaluate(null, buildData));
    }
    /*
       Tests LAST_BUILd_ON
       when the player builds
       only on the perimeter
     */
    @Test
    void lastBuildOn_Test3(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   | FF |    |   |    |    |
        +----+----+----+----+----+
    2   | DM | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();


        builds.put(new Point(0,2), Collections.singletonList(BuildingType.DOME));
        builds.put(new Point(0,1), Collections.singletonList(BuildingType.FIRST_FLOOR));

        buildOrder.add(new Point(0,1));
        buildOrder.add(new Point(0,2));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.LAST_BUILD_ON, "PERIMETER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertTrue(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.LAST_BUILD_ON, "PERIMETER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertFalse(lambdaStatement.evaluate(null, buildData));
    }
    /*
       Tests LAST_BUILd_ON
       when the player builds
       everything on the perimeter
       except the last build
     */
    @Test
    void lastBuildOn_Test4(){
         /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   | A1 |    |    |    |    |
        +----+----+----+----+----+
    1   | FF |    |   |    |    |
        +----+----+----+----+----+
    2   |    | B1 | A2 | B2 |    |
        +----+----+----+----+----+
    3   |    | DM |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
*/
        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();


        builds.put(new Point(1,3), Collections.singletonList(BuildingType.DOME));
        builds.put(new Point(0,1), Collections.singletonList(BuildingType.FIRST_FLOOR));

        buildOrder.add(new Point(0,1));
        buildOrder.add(new Point(1,3));

        BuildData buildData = new BuildData(Matteo, MatteoW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "YOU", StatementVerbType.LAST_BUILD_ON, "PERIMETER");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertFalse(lambdaStatement.evaluate(null, buildData));

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "YOU", StatementVerbType.LAST_BUILD_ON, "PERIMETER");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Mirko);

        assertTrue(lambdaStatement.evaluate(null, buildData));
    }
    /**
     * Test if given workers on the same level isTheHighest correctly returns false
     */
    @Test
    void isTheHighest_Test1(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   |A1  |    |    |    |    |
        +----+----+----+----+----+
    1   |    | 1  |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 |A2  | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
       */

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,1);

        moves.add(point1);


        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(moveData, null));

        //TRY STATEMENT WITH NIF
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(moveData, null));

        //TRY WHEN WORKERS ARE ON THE FIRST FLOOR
        model.getBoard().getCell(AndreaW1.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW2.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW1.getPosition()).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(AndreaW2.getPosition()).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(AndreaW1.getPosition()).setWorker(AndreaW1.getID());
        model.getBoard().getCell(AndreaW2.getPosition()).setWorker(AndreaW2.getID());

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(moveData, null));

        //TRY STATEMENT WITH NIF
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(moveData, null));

        //TRY WHEN WORKERS ARE ON THE SECOND FLOOR
        model.getBoard().getCell(AndreaW1.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW2.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW1.getPosition()).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(AndreaW2.getPosition()).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(AndreaW1.getPosition()).setWorker(AndreaW1.getID());
        model.getBoard().getCell(AndreaW2.getPosition()).setWorker(AndreaW2.getID());

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(moveData, null));

        //TRY STATEMENT WITH NIF
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(moveData, null));

        //TRY WHEN WORKERS ARE ON THE THIRD FLOOR
        model.getBoard().getCell(AndreaW1.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW2.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW1.getPosition()).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(AndreaW2.getPosition()).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(AndreaW1.getPosition()).setWorker(AndreaW1.getID());
        model.getBoard().getCell(AndreaW2.getPosition()).setWorker(AndreaW2.getID());

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(moveData, null));

        //TRY STATEMENT WITH NIF
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(moveData, null));

    }
    /**
     * Test if given workers on the different level isTheHighest correctly returns true when the highest workers tries to move.
     */
    @Test
    void isTheHighest_Test2(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   |A1F1| 1  |    |    |    |
        +----+----+----+----+----+
    1   |    |    |    |    |    |
        +----+----+----+----+----+
    2   |    | B1 |A2  | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
       */

        List<Point> moves = new ArrayList<>();
        Point point1 = new Point(1,1);

        moves.add(point1);

        model.getBoard().getCell(AndreaW1.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW1.getPosition()).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(AndreaW1.getPosition()).setWorker(AndreaW1.getID());

        MoveData moveData = new MoveData(Andrea, AndreaW1, moves);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(moveData, null));

        //TRY STATEMENT WITH NIF
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(moveData, null));

        //TRY WHEN W1 IS ON SF AND W2 ON FF, BUT A WORKER OF ANOTHER PLAYER IS ABOVE THEM

        model.getBoard().getCell(AndreaW1.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW2.getPosition()).removeWorker();
        model.getBoard().getCell(MatteoW2.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW1.getPosition()).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(AndreaW2.getPosition()).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(MatteoW2.getPosition()).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(AndreaW1.getPosition()).setWorker(AndreaW1.getID());
        model.getBoard().getCell(AndreaW2.getPosition()).setWorker(AndreaW2.getID());
        model.getBoard().getCell(MatteoW2.getPosition()).setWorker(MatteoW2.getID());

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(moveData, null));

        //TRY STATEMENT WITH NIF
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(moveData, null));

        //TRY WHEN W2 IS ON TF AND W1 ON SF, BUT A WORKER OF ANOTHER PLAYER IS ON TF TOO

        model.getBoard().getCell(AndreaW2.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW2.getPosition()).addBuilding(BuildingType.SECOND_FLOOR);
        model.getBoard().getCell(AndreaW2.getPosition()).addBuilding(BuildingType.THIRD_FLOOR);
        model.getBoard().getCell(AndreaW2.getPosition()).setWorker(AndreaW2.getID());

        ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(moveData, null));

        //TRY STATEMENT WITH NIF
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(moveData, null));


    }

    /**
     * Test if given workers on the different level isTheHighest correctly returns true when the highest workers tries to build.
     */
    @Test
    void isTheHighest_Test3(){
        /*
          0    1     2    3    4
        +----+----+----+----+----+
    0   |A1FF|    |    |    |    |
        +----+----+----+----+----+
    1   |    |x FF|    |    |    |
        +----+----+----+----+----+
    2   |    | B1 |A2  | B2 |    |
        +----+----+----+----+----+
    3   |    |    |    | D2 |    |
        +----+----+----+----+----+
    4   |    |    | D1 |    |    |
        +----+----+----+----+----+
       */

        Map<Point,List<BuildingType>> builds = new HashMap<>();
        List<Point> buildOrder = new LinkedList<>();

        Point point = new Point(1, 1);
        buildOrder.add(point);
        List<BuildingType> buildsInPoint = new ArrayList<>();
        buildsInPoint.add(BuildingType.FIRST_FLOOR);
        builds.put(point, buildsInPoint);

        model.getBoard().getCell(AndreaW1.getPosition()).removeWorker();
        model.getBoard().getCell(AndreaW1.getPosition()).addBuilding(BuildingType.FIRST_FLOOR);
        model.getBoard().getCell(AndreaW1.getPosition()).setWorker(AndreaW1.getID());

        BuildData buildData = new BuildData(Andrea, AndreaW1, builds, buildOrder);

        RuleStatement ruleStatement = RuleStatementImplTest.getStatement(StatementType.IF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        LambdaStatement lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertTrue(lambdaStatement.evaluate(null, buildData));

        //TRY STATEMENT WITH NIF
        ruleStatement = RuleStatementImplTest.getStatement(StatementType.NIF, "CHOSEN_WORKER", StatementVerbType.IS_THE_HIGHEST, "YOUR_WORKERS");
        lambdaStatement = StatementCompiler.compileStatement(model, ruleStatement, Andrea);

        assertFalse(lambdaStatement.evaluate(null, buildData));
    }
}
