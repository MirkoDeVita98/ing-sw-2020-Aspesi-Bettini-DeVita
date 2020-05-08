package it.polimi.ingsw.CLI.strategies;

import it.polimi.ingsw.CLI.CLI;
import it.polimi.ingsw.ConnectionStatus;

public class ConnectionSetupStrategy implements ConnectionStrategy {
    @Override
    public void handleConnection(ConnectionStatus connectionStatus, CLI cli) {
        if(connectionStatus.isClosed()){
            System.out.println("\n" + connectionStatus.getReasonOfClosure());
            cli.setAskConnectionParameters(true);
            cli.run();
        }
        else{
            System.out.println("Connection established!");
            cli.setConnectionInGameStrategy();
        }

    }
}