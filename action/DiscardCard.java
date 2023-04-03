package action;

import java.util.ArrayList;

import game.Card;
import game.GameState;
import game.User;

public class DiscardCard extends AbstractAction {
  GameState game;
  
  public DiscardCard (GameState game) {
    this.game = game;
  }
	
  public boolean canPerform() {
    if (game.users[game.currentUser].cards.size() > 0) {
      return true;
    }

    return false;
  }

  public boolean perform(GameState game) {
    this.game = game;
    return perform();
  }

  public boolean perform() {
		System.out.println("Max number of cards in hand reached for "+ game.users[game.currentUser].name);
		while (game.users[game.currentUser].cards.size() > GameState.MAX_NUMBER_OF_CARDS_IN_HAND) {
			System.out.println("Current number of cards in hand: "+ game.users[game.currentUser].cards.size());
			System.out.println("Enter city of card to discard");
			String userInput = "";
			int cardIndex = -1;
      if (game.users[game.currentUser].type == User.PLAYER) {
        userInput = GameState.shellInput.nextLine();
        cardIndex = game.searchUserCards(game.currentUser, userInput);
      } else {
        cardIndex = (int) (Math.random() * inputOptions().size());
        System.out.println(game.cities[cardIndex]);
      }
			if (cardIndex == -1) {
				System.out.println(userInput +" is not a valid card. Here are the cards you can choose from");
				game.printUserCards(game.currentUser);
			} else {
				Card card = game.users[game.currentUser].cards.remove(cardIndex);
				game.playerDiscardPile.add(card);
				System.out.println("Card discarded");
			}
		}

    return true;
  }

  public boolean requiresInput() {
    return true;
  }

  public ArrayList<Card> inputOptions () {
    return game.users[game.currentUser].cards;
  }

  public int excessCards() {
    return game.users[game.currentUser].cards.size() - GameState.MAX_NUMBER_OF_CARDS_IN_HAND;
  }
}
