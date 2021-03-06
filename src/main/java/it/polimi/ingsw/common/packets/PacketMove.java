package it.polimi.ingsw.common.packets;


import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the info about a move action performed by a Player.
 */
public class PacketMove implements Serializable {
    private static final long serialVersionUID = 6117802389323885746L;
    /**
     * This is the nickname of the Player who performed the action.
     */
    private final String playerNickname;
    /**
     * This is the ID of Worker that is used by the Player in his action.
     */
    private final String workerID;
    private final boolean simulate;
    /**
     * This is an ordered List of Points that the Player has reached during his action.
     */
    private final List<Point> move;


    public PacketMove(String playerNickname, String workerID, boolean simulate, List<Point> move){
        assert (playerNickname != null && move != null);
        this.simulate = simulate;
        this.playerNickname = playerNickname;
        this.workerID = workerID;
        this.move = new ArrayList<>();
        for(Point p : move){
            assert(p != null);
            this.move.add(new Point(p));
        }
    }

    public PacketMove(String playerNickname){
        this(playerNickname, null, true, new ArrayList<>());
    }

    public PacketMove(String playerNickname, String workerID, List<Point> move){
        this(playerNickname,workerID,true, move);
    }

    public boolean isSimulate() {
        return simulate;
    }

    /**
     * Getter that returns the nickname of the Player.
     * @return a String containing the Player nickname.
     */
    public String getPlayerNickname() { return this.playerNickname; }

    /**
     * Getter that returns the ID of the Worker.
     * @return a String containing the ID of the Worker.
     */
    public String getWorkerID() { return this.workerID; }

    /**
     * Getter that returns the ordered List of performed moves.
     * @return a List of points indicating the reached cells.
     */
    public List<Point> getMove() { return this.move; }
}
