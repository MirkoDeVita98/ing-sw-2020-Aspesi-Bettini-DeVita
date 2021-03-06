package it.polimi.ingsw.client.cli.graphical;

import it.polimi.ingsw.client.cli.utilities.CharStream;
import it.polimi.ingsw.client.cli.utilities.colors.BackColor;
import it.polimi.ingsw.client.cli.utilities.colors.ForeColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphicalCardsMenu implements CharFigure {

    private CharStream stream;
    private Map<String, String> godCards;
    private List<String> chosenCards;
    private int cardsPerRow;
    private List<String> availableCards;
    private static final int defaultRequiredWidth = 159;
    private static final int marginTitle = 40;
    private static final BackColor cardsInGameTitleColor = BackColor.ANSI_BG_RED;
    private static final BackColor chooseCardsTitleColor = BackColor.ANSI_BRIGHT_BG_GREEN;
    private static final BackColor godCardsTitleColor =  BackColor.ANSI_BG_YELLOW;
    private static final int marginAvailableChosenY = 10;
    private static final ForeColor titleCharacterColor = ForeColor.ANSI_BLACK;
    private static final int space_2Cards = 20;
    private static final int space_3Cards = 10;
    private static final int marginX_2Cards = 40;
    private static final int marginX_3Cards = 25;
    private static final int cardsDeltaFromTitle = 7;
    private static final int deltaWidthFromRight = 19;
    private static final int deltaHeightChooseCards = 20;
    private static final int deltaHeightAllCards = 7;

    /**
     * This constructor initializes all the godCards received from the server. GodCards are all the received god cards,
     * chosenCards are the cards of the players in the match and the availableCards are the possible cards to choose at
     * the beginning of the match.
     */
    public GraphicalCardsMenu(){
        this.godCards = new HashMap<>();
        this.chosenCards = new ArrayList<>();
        this.availableCards = new ArrayList<>();
        this.cardsPerRow = 4;
    }

    /**
     * This method sets the stream used by the GraphicalCardsMenu to print itself.
     * @param stream is the CharsStream instance.
     */
    public void setStream(CharStream stream) {
        this.stream = stream;
    }

    /**
     * This method returns the width required by the GraphicalCardsMenu to print itself on the stream.
     * @return an integer corresponding to the required width.
     */
    public int getRequiredWidth(){
        if(!chosenCards.isEmpty() || availableCards.size() <= 3) return defaultRequiredWidth;
        return GraphicalCard.getWidth() * cardsPerRow + deltaWidthFromRight;
    }

    /**
     * This method returns the height required by the GraphicalCardsMenu to print itself on the stream.
     * @return an integer corresponding to the required height.
     */
    public int getRequiredHeight(){
        if(!chosenCards.isEmpty() || availableCards.size() <= 3) return GraphicalCard.getHeight() + deltaHeightChooseCards;
        int count = godCards.size();
        while(count % cardsPerRow != 0){
            count ++;
        }
        return (count / cardsPerRow) * GraphicalCard.getHeight() + 2 * (count / cardsPerRow + 1) + deltaHeightAllCards;
    }

    /**
     * This method sets the god cards received from the server. All of them are only displayed to the challenger.
     * Otherwise they are used to get the description for the ChosenCards or the Available ones.
     * @param godCards is the Map that associates god cards' names to their descriptions.
     */
    public void setGodCards(Map<String, String> godCards) {
        this.godCards = godCards;
        if(godCards.size() % 3 == 0) cardsPerRow = 3;
        else cardsPerRow = 4;
    }

    /**
     * This method sets the available god cards that the user can choose.
     * @param availableCards is a list containing the available cards' names.
     */
    public void setAvailableCards(List<String> availableCards) {
        this.availableCards = availableCards.stream().sorted().collect(Collectors.toList());
    }

    /**
     * This method sets the chose god cards in the current match.
     * @param chosenCards  is a list containing the chosen cards' names.
     */
    public void setChosenCards(List<String> chosenCards) {
        this.chosenCards = chosenCards.stream().sorted().collect(Collectors.toList());
    }

    /**
     * This method is used to display the GraphicalCardsMenu on the stream's default position.
     */
    @Override
    public void draw() {
        draw(CharStream.defaultX, CharStream.defaultY);
    }

    /**
     * This method is used to display the GraphicalCardsMenu on the stream.
     * If the cards have to be displayed to the challenger printAllCards is called.
     * If the chosen cards are set, the user can view them while choosing his worker.
     * If the available cards to choose are set they are displayed to players that are not the challenger.
     */
    @Override
    public void draw(int relX, int relY) {
        if (stream == null) return;

        if (!chosenCards.isEmpty()) {
            printAvailableOrChosen("CARDS IN GAME", relX, relY, chosenCards, cardsInGameTitleColor);
            return;
        }

        if(availableCards.size() <= 3){
            printAvailableOrChosen("CHOOSE A CARD", relX, relY, availableCards, chooseCardsTitleColor);
            return;
        }

        if(!godCards.isEmpty()) printAllCards(relX, relY);
    }

    /**
     * This method prints all the god cards received from th server.
     * @param relX is the X coordinate relative to the menu.
     * @param relY is the Y coordinate relative to the menu.
     */
    private void printAllCards(int relX, int relY){
        int marginForHeading = (cardsPerRow * space_3Cards) - (cardsPerRow <= 2 ? space_2Cards : 0) + 5;
        stream.setMessage("GOD CARDS", relX + marginForHeading, relY + 2, titleCharacterColor, godCardsTitleColor);
        int countY = 0;
        int countX = 0;
        for (String godCard : availableCards) {
            GraphicalCard graphicalCard = new GraphicalCard(stream, godCard, godCards.get(godCard));
            graphicalCard.draw(relX + GraphicalCard.getWidth() * countY + cardsPerRow * (countY + 1), relY + cardsDeltaFromTitle + GraphicalCard.getHeight() * countX + 2 * (countX + 1));
            countY++;
            if (countY == cardsPerRow) {
                countY = 0;
                countX++;
            }
        }
    }

    /**
     * This method prints the available/chosen cards.
     * @param relX is the X coordinate relative to the menu.
     * @param relY is the Y coordinate relative to the menu.
     */
    private void printAvailableOrChosen(String message, int relX, int relY, List<String> cardsToPrint, BackColor titleColor){
        stream.setMessage(message, relX + marginTitle, relY + 2, titleCharacterColor, titleColor);

        if (cardsToPrint.size() == 2) {
            int space = 0;
            for (String card : cardsToPrint) {
                GraphicalCard graphicalCard = new GraphicalCard(stream, card, godCards.get(card));
                graphicalCard.draw(relX + marginX_2Cards + space, relY + marginAvailableChosenY);
                space += GraphicalCard.getWidth() + space_2Cards;
            }
        } else if (cardsToPrint.size() == 3) {
            int space = 0;
            for (String card : cardsToPrint) {
                GraphicalCard graphicalCard = new GraphicalCard(stream, card, godCards.get(card));
                graphicalCard.draw(relX + marginX_3Cards + space, relY + marginAvailableChosenY);
                space += GraphicalCard.getWidth() + space_3Cards;
            }
        }
    }
}