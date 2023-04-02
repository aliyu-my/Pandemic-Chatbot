package action;

import game.Card;
import game.GameState;

// Draw from player deck and give user
public class DrawPlayerCard extends AbstractAction {
  GameState game;
  
  public DrawPlayerCard (GameState game) {
    this.game = game;
  }
	
  public boolean canPerform() {
    return true;
  }

  public boolean perform() {
		if (game.playerDeck.size() == 0) {
			System.out.println("No more player cards in deck. Game over");
			/* Game over. Do something */
			return false;
		}
		Card card = game.playerDeck.remove(game.playerDeck.size() - 1);
		if (card.type == Card.EPIDEMIC) {
			game.epidemicCardDrawn(card, -1);
		} else {
			game.users[game.currentUser].cards.add(card);
			System.out.print("Card drawn; ");
			System.out.println(game.cities[card.city] +": "+ card.color);
		}

    return true;
	}
	
  public boolean requiresInput() {
    return false;
  }

}
