package it.polimi.ingsw.server.communication;

import it.polimi.ingsw.server.ServerLogger;
import it.polimi.ingsw.server.communication.enums.ServerPhase;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ServerImpl implements Server {

    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService executor;

    private List<ConnectionToClient> waitingClients;
    private final List<Match> activeMatches;

    ReentrantLock lockLobby = new ReentrantLock(true);
    ReentrantLock lockMatches = new ReentrantLock(true);

    private int currMatchSize;
    private boolean currMatchHardcore;
    private int currMatchID;

    private volatile ServerPhase serverPhase;
    private volatile ConnectionToClient whoIAmCurrentlyWaiting;

    private final Logger serverLogger = Logger.getLogger(ServerLogger.LOGGER_NAME);

    /**
     * The constructor initializes the variables
     * @param port Port of the Server
     */
    public ServerImpl(int port) {
        this.executor = Executors.newCachedThreadPool();
        this.waitingClients = new LinkedList<>();
        this.activeMatches = new ArrayList<>();
        this.currMatchSize = -1;
        this.currMatchHardcore = false;
        this.currMatchID = 1;
        this.port = port;
        this.serverPhase = ServerPhase.EMPTY_LOBBY;
        this.whoIAmCurrentlyWaiting = null;
    }

    /**
     * This method creates a server socket
     * and then loops continuing to accept incoming connections
     * and assigning them to a thread in the thread pool
     */
    public void startServer(){
        try{
            serverSocket = new ServerSocket(port);
        }catch (IOException e){
            serverLogger.log(Level.SEVERE, "Cannot open server on port " + port);
            return; //Close server
        }
        try {
            serverLogger.info("Server is ready");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                serverLogger.info("Received connection from address: [" + clientSocket.getInetAddress().getHostAddress() + "]");
                ConnectionToClient clientConnection = new ConnectionToClient(clientSocket);

                clientConnection.setNickNameChosenHandler(this::handleNickChosen);
                clientConnection.setGameDesiresHandler(this::handleDesires);
                clientConnection.setClosureHandler(this::deregister);

                executor.submit(clientConnection);
            }
        } catch (IOException e) {
            serverLogger.log(Level.SEVERE, "Server socket has stopped working with an exception:", e);
            if(serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * This method is called when a client provides a valid nickname,
     * it checks that the server was actually waiting for that nick,
     * and acts accordingly, by either requesting again the nick (if it is already chosen),
     * or adding the client to the lobby
     * @param connection the client providing the nick
     */
    private void handleNickChosen(ConnectionToClient connection) {

        if (serverPhase == ServerPhase.WAITING_USERNAME_REINSERTION && whoIAmCurrentlyWaiting.equals(connection)) {
            if (alreadyTaken(connection))
                connection.askNicknameAgain();
            else {
                setNextLobbyPhase();
            }
        } else {
            lockLobby.lock();
            try {
                if(!waitingClients.contains(connection)) {
                    waitingClients.add(connection);
                    if(serverPhase != ServerPhase.WAITING_DESIRES && serverPhase != ServerPhase.WAITING_USERNAME_REINSERTION)
                        setNextLobbyPhase();
                }
            } finally {
                lockLobby.unlock();
            }

        }
    }

    /**
     * This method is called when a client provides valid desires for the match creation,
     * it checks that the server was actually waiting for the desires from this client,
     * and acts accordingly, setting the desires and the next lobby state
     * @param connection the client providing the desires
     */
    private void handleDesires(ConnectionToClient connection) {
        if (serverPhase == ServerPhase.WAITING_DESIRES && whoIAmCurrentlyWaiting.equals(connection)) {
            lockLobby.lock();
            try {
                currMatchSize = connection.getDesiredNumOfPlayers();
                currMatchHardcore = connection.isDesiredHardcore();

                setNextLobbyPhase();

            } finally {
                lockLobby.unlock();
            }
        }
    }


    /**
     * Called when a wait situation has been resolved
     * or when no waiting situation is present and a new client joins the lobby.
     * It manages the next lobby phase
     */
    private void setNextLobbyPhase() {
        lockLobby.lock();
        try {

            whoIAmCurrentlyWaiting = null;

            for(int i = 1; i < currMatchSize && i < waitingClients.size(); i++){
                if(alreadyTaken(waitingClients.get(i))){
                    serverPhase = ServerPhase.WAITING_USERNAME_REINSERTION;
                    whoIAmCurrentlyWaiting = waitingClients.get(i);
                    whoIAmCurrentlyWaiting.askNicknameAgain();
                    return;
                }
            }

            if (waitingClients.size() == 0) {
                serverPhase = ServerPhase.EMPTY_LOBBY;
                currMatchSize = -1;
                currMatchHardcore = false;
            } else if (currMatchSize == -1) {
                serverPhase = ServerPhase.WAITING_DESIRES;
                whoIAmCurrentlyWaiting = waitingClients.get(0);
                whoIAmCurrentlyWaiting.askForDesiredPlayersAndGamemode();
            } else if (waitingClients.size() >= currMatchSize) {
                createMatch();
            } else{
                serverPhase = ServerPhase.FILLING_LOBBY;
            }

        } finally {
            lockLobby.unlock();
        }
    }

    /**
     * Creates a match as the required lobby size as been reached
     */
    private void createMatch() {
        lockLobby.lock();
        try {
            serverLogger.info("Creating match with id [" + currMatchID + "], players : [" + waitingClients.subList(0, currMatchSize).stream().map(ConnectionToClient::getClientNickname).collect(Collectors.joining(", ")) + "]");
            Match match = new Match(waitingClients.subList(0, currMatchSize), currMatchHardcore, currMatchID);
            match.setClosureHandler(this::deregisterMatch);

            lockMatches.lock();
            try {
                activeMatches.add(match);
            } finally {
                lockMatches.unlock();
            }

            currMatchID++;

            waitingClients = waitingClients.stream().filter(x -> waitingClients.indexOf(x) >= currMatchSize).collect(Collectors.toList());

            currMatchSize = -1;
            currMatchHardcore = false;

            setNextLobbyPhase();

            match.start();

        } finally {
            lockLobby.unlock();
        }
    }

    /**
     * This methods de-registers the selected client from the server
     * It looks if the client is in the lobby and, if it is,
     * the client is removed from the lobby.
     * Afterwards, if a waiting situation is resolved or no waiting situation was present
     * it calls the setNextLobbyPhase method
     *
     * @param connectionToClient the client to be de-registered
     */
    private void deregister(ConnectionToClient connectionToClient) {
        lockLobby.lock();
        try {
            if(waitingClients.contains(connectionToClient)) {
                waitingClients.remove(connectionToClient);
                serverLogger.info("Client [" + connectionToClient.getClientNickname() + "] unregistered from lobby");
                if(whoIAmCurrentlyWaiting == null || whoIAmCurrentlyWaiting.equals(connectionToClient))
                    setNextLobbyPhase();
            }
        } finally {
            lockLobby.unlock();
        }
    }

    /**
     * This method is called when a match is closing,
     * it removes the match from the current matches
     * @param match the closing match
     */
    private void deregisterMatch(Match match) {
        lockMatches.lock();
        try {
            assert activeMatches.contains(match);
            activeMatches.remove(match);
            serverLogger.info("Deregistered match with ID: [" + match.getId() + "]");
        } finally {
            lockMatches.unlock();
        }
    }

    /**
     * Checks if a provided nick is already taken by other players in the lobby
     * @param connection the client to check
     * @return is it taken
     */
    private boolean alreadyTaken(ConnectionToClient connection) {
        lockLobby.lock();
        try {
            if (waitingClients.size() > 1) {
                for (ConnectionToClient waitingClient : waitingClients) {
                    if (waitingClient.equals(connection))
                        return false;
                    else if (waitingClient.getClientNickname().equals(connection.getClientNickname()))
                        return true;
                }
            }
            return false;
        } finally {
            lockLobby.unlock();
        }
    }

}
