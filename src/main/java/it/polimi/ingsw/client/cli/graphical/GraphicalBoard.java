package it.polimi.ingsw.client.cli.graphical;

import it.polimi.ingsw.client.cli.utilities.CharStream;
import it.polimi.ingsw.client.cli.colors.BackColor;
import it.polimi.ingsw.client.cli.colors.ForeColor;

import java.awt.*;
import java.util.List;

public class GraphicalBoard implements CharFigure {
    private final int rows = 5;
    private final int columns = 5;
    private final CharStream stream;
    private final GraphicalCell[][] graphicalCells;
    private final int RATEOX = 20;
    private final int RATEOY = 8;
    private final int defaultX = 50;
    private final int defaultY = 5;
    private List<Point> possiblePositions;

    /**
     * This constructor initializes the GraphicalBoard given the stream that will be used in order to print itself.
     * The GraphicalBoard rep is a rows * columns matrix of GraphicalCells.
     * @param stream is the given CHarStream.
     */
    public GraphicalBoard(CharStream stream){
        this.stream = stream;
        this.graphicalCells = new GraphicalCell[rows][columns];
        for(int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                graphicalCells[i][j] = new GraphicalCell(new Point(i, j), stream, RATEOX, RATEOY);
            }
        }
    }

    /**
     * This method is used to draw the GraphicalBoard on the stream. The absolute default coordinates are used.
     */
    @Override
    public void draw() {
        draw(defaultX, defaultY);
    }

    /**
     * This method is used to draw the GraphicalBoard on the stream given the coordinates.
     * Alongside the board coordinates are displayed and this method calls the worker and buildings' draw
     * method to display themselves relatively to the GraphicalBoard position on th stream.
     * @param relX is the X coordinate.
     * @param relY is the Y coordinate.
     */
    @Override
    public void draw(int relX, int relY) {

        BackColor backColor = BackColor.ANSI_BG_YELLOW;
        ForeColor foreColor = ForeColor.ANSI_BLACK;

        char[] coordinatesYaxis = new char[rows];
        char startingLetter = 'A';
        for(int i = 0; i < rows; ++i) coordinatesYaxis[i] = (char)(startingLetter + i);

        char[] coordinatesXaxis = new char[columns];
        char startingNumber = '1';
        for(int i = 0; i < columns; ++i) coordinatesXaxis[i] = (char)(startingNumber + i);

        int indexX = 0;
        int indexY = 0;

        for(int i = 0; i <= rows * RATEOX; ++i){
            for(int j = 0; j <= columns * RATEOY; ++j){
                if(i == 0 && j == 0) stream.addChar('╔', i + relX, j + relY, foreColor, backColor);
                else if(i == 0  && j % RATEOY == 0 && j != columns * RATEOY) stream.addChar('╠', i + relX, j + relY, foreColor, backColor);
                else if(i == 0 && j == columns * RATEOY) stream.addChar('╚', i + relX, j + relY, foreColor, backColor);
                else if(i == rows * RATEOX && j == 0) stream.addChar('╗', i + relX, j + relY, foreColor, backColor);
                else if(i == rows * RATEOX  && j % RATEOY == 0 && j != columns * RATEOY) stream.addChar('╣', i + relX, j + relY, foreColor, backColor);
                else if(i == rows * RATEOX && j == columns * RATEOY) stream.addChar('╝', i + relX, j + relY, foreColor, backColor);
                else if(i % RATEOX == 0 && i != rows * RATEOX && j == 0) stream.addChar('╦', i + relX, j + relY, foreColor,backColor);
                else if(i % RATEOX == 0 && i != rows * RATEOX && j == columns * RATEOY) stream.addChar('╩', i + relX, j + relY, foreColor, backColor);
                else if(i % RATEOX == 0 && j % RATEOY == 0) stream.addChar('╬', i + relX, j + relY, foreColor, backColor);
                else if(i % RATEOX == 0) stream.addChar('║', i + relX, j + relY, foreColor, backColor);
                else if(j % RATEOY == 0) stream.addChar('═', i + relX, j + relY, foreColor, backColor);
                else stream.addColor(i + relX, j + relY, foreColor, BackColor.ANSI_BG_GREEN);

                if(i % 10 == 0 && j == 0 && i % RATEOX != 0){
                    stream.addChar(coordinatesXaxis[indexX], i + relX, j + relY - 1, foreColor, backColor);
                    ++indexX;
                }
                if(j % 4 == 0 && i == 0 && j % RATEOY != 0){
                    stream.addChar(coordinatesYaxis[indexY], i + relX - 1, j + relY, foreColor, backColor);
                    ++indexY;
                }
            }
        }


        for(int i = 0; i < rows; ++i){
            for(int j = 0; j < columns; ++j){
                graphicalCells[i][j].draw(relX + i * RATEOX, relY + j * RATEOY);
            }
        }

        if(possiblePositions != null) highlightActions(possiblePositions, relX, relY);


    }

    /**
     * This method resets the highlighted cells that corresponds to the possible actions.
     */
    public void resetPossibleActions(){
        this.possiblePositions = null;
    }

    /**
     * This method sets the highlighted cells that corresponds to the possible actions given a list of positions.
     * @param possiblePositions is the list of possible positions.
     */
    public void setPossibleActions(List<Point> possiblePositions){
        this.possiblePositions = possiblePositions;
    }

    /**
     * This method highlight the set possible positions on the GraphicalBoard in order to display
     * possible actions to the user.
     * @param positions is the list of positions to highlight.
     * @param relX is the X coordinate of the GraphicalBoard.
     * @param relY is the Y coordinate of the GraphicalBoard.
     */
    private void highlightActions(List<Point> positions, int relX, int relY){
        ForeColor foreColor = ForeColor.ANSI_BLACK;
        BackColor backColor = BackColor.ANSI_BRIGHT_BG_GREEN;
        if(positions == null) return;
        for(Point pos : positions){
            int X = pos.x * RATEOX;
            int Y = pos.y * RATEOY;
            for(int i = 1; i < RATEOX; ++ i){
                stream.addColor(i + relX + X, relY + Y + 1, foreColor, backColor);
            }
            for(int i = 1; i < RATEOY; ++ i){
                stream.addColor(relX + X + 1, i + relY + Y, foreColor, backColor);
            }
            for(int i = 1; i < RATEOX; ++ i){
                stream.addColor(i + relX + X, relY + Y + RATEOY - 1, foreColor, backColor);
            }
            for(int i = 1; i < RATEOY; ++ i){
                stream.addColor(relX + X + RATEOX - 1, i + relY + Y, foreColor, backColor);
            }
        }
    }

    /**
     * This method returns a GraphicalCell instance given its position on the GraphicalBoard.
     * @param pos is the given position.
     * @return and instance of GraphicalCell.
     */
    public GraphicalCell getCell(Point pos){
        if (pos.x < 0 || pos.x >= rows || pos.y < 0 || pos.y >= columns) return null;
        return graphicalCells[pos.x][pos.y];
    }

    /**
     * This method removes a worker from the GraphicalBoard given its playerID and its number.
     * @param playerID is the String containing the playerID.
     * @param workerNumber is the number of the given worker.
     */
    public void removeWorker(String playerID, Integer workerNumber){
        Point position = getWorkerPosition(playerID, workerNumber);
        if(position == null) return;
        graphicalCells[position.x][position.y].removeWorker();
    }

    /**
     * This method returns the position on the GraphicalBoard of the given worker.
     * @param playerID is the String containing the playerID.
     * @param workerNumber is the number of the given worker.
     * @return a Point containing the worker's coordinates, null if it's not present.
     */
    public Point getWorkerPosition(String playerID, Integer workerNumber){
        for(int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                GraphicalWorker graphicalWorker = graphicalCells[i][j].getWorker();
                if(graphicalWorker != null && graphicalWorker.getPlayerName() != null && graphicalWorker.getNumber() != null){
                    if(playerID.equals(graphicalWorker.getPlayerName()) && workerNumber.equals(graphicalWorker.getNumber())) return new Point(i, j);
                }
            }
        }
        return null;
    }

}
