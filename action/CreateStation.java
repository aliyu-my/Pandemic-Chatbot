
package action;

import game.Card;
import game.GameState;
import game.User;

// Create research station in current location by discarding city card.
public class CreateStation extends AbstractAction {
  GameState game;
  
  public CreateStation (GameState game) {
    this.game = game;
  }
	
  public boolean canPerform() {
		User user = game.users[game.currentUser];
		for (Card card: user.cards) {
			if (card.city == user.location) {
				if (game.researchStations[user.location]) {
					return false;
				} else {
					return true;
				}
			}
		}

		return false;
  }

  public boolean perform() {
		User user = game.users[game.currentUser];
		for (Card card: user.cards) {
			if (card.city == user.location) {
				if (game.researchStations[user.location]) {
					System.out.println("A research station already exists at your location");
					return false;
				} else {
					game.users[game.currentUser].cards.remove(card);
					game.researchStations[user.location] = true;
					System.out.println("Research station built at "+ game.cities[user.location]);
					return true;
				}
			}
		}

		System.out.println("You don't have a card of your current location");
		return false;
	}
	
  public boolean requiresInput() {
    return false;
  }

}
