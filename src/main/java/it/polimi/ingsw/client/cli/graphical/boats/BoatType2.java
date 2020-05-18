package it.polimi.ingsw.client.cli.graphical.boats;

import it.polimi.ingsw.client.cli.graphical.CharFigure;
import it.polimi.ingsw.client.cli.utilities.CharStream;
import it.polimi.ingsw.client.cli.colors.BackColor;
import it.polimi.ingsw.client.cli.colors.ForeColor;

class BoatType2 implements CharFigure {

    private final CharStream stream;

    /**
     * The constructor only need to know the stream in order to print itself.
     * @param stream is the object used to set colors and characters to be able to print them.
     */
    public BoatType2(CharStream stream){
        this.stream = stream;
    }

    /**
     * This method is overridden from the CharFigure interface.
     * Since the board position on the stream is relative to the one of the graphical ocean this method is not used.
     */
    @Override
    public void draw() {
        draw(0,0);
    }

    /**
     * This method will set colors and characters used to display the board through the stream.
     * Colors of the BoatType2: Red with Black sail.
     * @param relX is the position on the X axis.
     * @param relY is the position on the Y axis.
     */
    @Override
    public void draw(int relX, int relY) {
        stream.addChar('_',relX - 2, relY + 1, ForeColor.ANSI_BRIGHT_WHITE, BackColor.ANSI_BRIGHT_BG_CYAN);
        stream.addChar('_',relX + 5, relY + 1, ForeColor.ANSI_BRIGHT_WHITE, BackColor.ANSI_BRIGHT_BG_CYAN);
        stream.addColor(relX - 1, relY + 1, BackColor.ANSI_BRIGHT_BG_RED);
        stream.addColor(relX , relY + 1, BackColor.ANSI_BRIGHT_BG_RED);
        stream.addColor(relX + 1 , relY + 1, BackColor.ANSI_BRIGHT_BG_RED);
        stream.addColor(relX + 2, relY + 1, BackColor.ANSI_BRIGHT_BG_RED);
        stream.addColor(relX + 3, relY + 1, BackColor.ANSI_BRIGHT_BG_RED);
        stream.addColor(relX + 4, relY + 1, BackColor.ANSI_BRIGHT_BG_RED);
        stream.addColor(relX + 1, relY, BackColor.ANSI_BRIGHT_BG_BLACK);
        stream.addColor(relX + 2, relY, BackColor.ANSI_BRIGHT_BG_BLACK);
        stream.addChar('|',relX + 2, relY, ForeColor.ANSI_BRIGHT_WHITE, BackColor.ANSI_BRIGHT_BG_BLACK);
    }
}