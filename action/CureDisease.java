package action;

import java.util.ArrayList;

import game.Card;
import game.GameState;
import game.User;

	// Cure disease if criteria is satisfied
public class CureDisease extends AbstractAction {
  GameState game;
  
  public CureDisease (GameState game) {
    this.game = game;
  }
	
  public boolean canPerform() {
		if (!game.researchStations[game.users[game.currentUser].location]) {
			return false;
		}
		
		// Group user cards by their colors
		int[] colors = new int[GameState.diseaseColors.length];
		for (int cards = 0; cards < game.users[game.currentUser].cards.size(); cards++) {
			Card card = game.users[game.currentUser].cards.get(cards);
			for (int color = 0; color < GameState.diseaseColors.length; color++) {
				if (GameState.diseaseColors[color] == card.color) {
					colors[color]++;
				}
			}
		}
		
		// check if user has cards at threshold
		for (int color = 0; color < colors.length; color++) {
			// Check if user is a scientist and reduce the number of cards required for cure
			int numberOfCardsRequired = GameState.CARDS_TO_CURE_DISEASE - 
				(game.users[game.currentUser].role == User.SCIENTIST ? 1: 0);

			if (colors[color] >= numberOfCardsRequired) {
        return true;
			}
		}

    return false;
  }

  public boolean perform() {
		if (!game.researchStations[game.users[game.currentUser].location]) {
			System.out.println("You are not at a research station");
			return false;
		}
		
		// Group user cards by their colors
		int[] colors = new int[GameState.diseaseColors.length];
		for (int cards = 0; cards < game.users[game.currentUser].cards.size(); cards++) {
			Card card = game.users[game.currentUser].cards.get(cards);
			for (int color = 0; color < GameState.diseaseColors.length; color++) {
				if (GameState.diseaseColors[color] == card.color) {
					colors[color]++;
				}
			}
		}
		
		// check if user has cards at threshold
		boolean found = false;
		for (int color = 0; color < colors.length; color++) {
			// Check if user is a scientist and reduce the number of cards required for cure
			int numberOfCardsRequired = GameState.CARDS_TO_CURE_DISEASE - 
				(game.users[game.currentUser].role == User.SCIENTIST ? 1: 0);

			if (colors[color] >= numberOfCardsRequired) {
				found = true;
				// Identify the cards to remove
				ArrayList<Card> cardsToRemove = new ArrayList<Card>(); 
				for (int cards = 0; cards < game.users[game.currentUser].cards.size(); cards++) {
					Card card = game.users[game.currentUser].cards.get(cards);
					if (card.color.compareTo(GameState.diseaseColors[color]) == 0) {
						cardsToRemove.add(card);
					}
				}

				// Now remove the cards after identifing them
				for (int cards = 0; cards < cardsToRemove.size(); cards++) {
					Card card = cardsToRemove.get(cards);
					game.users[game.currentUser].cards.remove(card);
					game.playerDiscardPile.add(card);
				}

				// disease is now cured
				game.curedDiseases[color] = true;
				System.out.println(GameState.diseaseColors[color] +" is now cured");

				game.checkIfDiseaseIsEradicated(color);
			}
		}

		if (found) {
			game.checkIfAllDiseasesAreCured();
			return true; 
		} else {
			System.out.println("You do not have 5 cards of the same color");
			return false;
		}
  }

  public boolean requiresInput() {
    return false;
  }

}
