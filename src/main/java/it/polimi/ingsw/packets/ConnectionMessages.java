package it.polimi.ingsw.packets;

import java.io.Serializable;

public enum ConnectionMessages implements Serializable {

    INVALID_PACKET("Invalid packet"),
    INSERT_NICKNAME("Insert your nickname"),
    INSERT_NUMBER_OF_PLAYERS("Insert the number of players"),
    IS_IT_HARDCORE("Do you want to play in hardcore mode?"),
    INVALID_NICKNAME("Nickname is already in use, choose another"),
    CONNECTION_CLOSED("Connection closed"),
    MATCH_ENDED("Match ended due to disconnected or not responding clients");

    private final String message;
    private static final long serialVersionUID = -68031328223481106L;

    ConnectionMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}