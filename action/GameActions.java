package action;
import java.util.ArrayList;

import game.Card;
import game.GameState;
import game.User;

public class GameActions {
  GameState game;
  
  public GameActions (GameState game) {
    this.game = game;
  }
	
	public boolean tryAction(AbstractAction action) {
		if (game.users[game.currentUser].type == User.AGENT) {
			if (action.canPerform()) {
				return action.perform();
			}
		} else {
			return action.perform();
		}

		return false;
	}

	//Ask the user where to move, get the city, and if valid, move user's location.
	public void moveUser() {
		MoveUser action = new MoveUser(game);
		tryAction(action);
	}

  // Draw from player deck and give user
	public boolean drawPlayerCard() {
		DrawPlayerCard action = new DrawPlayerCard(game);
		return tryAction(action);
	}

	// Take a card from a user
	public boolean takeCard() {
		if (game.users[game.currentUser].cards.size() >= 7) {
			System.out.println("User cannot have more than 7 cards at hand");
		}
		// Identify users in current location
		ArrayList<Integer> usersInLocation = new ArrayList<Integer>();
		for (int userNumber = 0; userNumber<game.number_of_users;userNumber++) {
			if (game.users[userNumber].location == game.users[game.currentUser].location) {
				if (userNumber == game.currentUser) {
				} else {
					// Make sure the user has cards
					if (game.users[userNumber].cards.size() > 0) {
						usersInLocation.add(userNumber);
					}
				}
			}
		}

		// No users in current location
		if (usersInLocation.size() == 0) {
			System.out.println("There are no users currently in your location");
			return false;
		} else {
			// Display users in current location
			for (int userNumber: usersInLocation) {
				System.out.println(game.users[userNumber].name +" is currently in your location");
			}

			// Select user to collect card from
			boolean userFound = false;
			int userIndex = -1;
			while (!userFound) {
				System.out.println("Enter the name of user to collect card from");
				String userInput = GameState.shellInput.nextLine();
				userIndex = game.searchUserName(userInput);
			
				if (userIndex == -1) {
					System.out.println(userInput + " is not a valid User.");
				} else {
					userFound = true;
				}
				game.blankLine();
			}

			boolean found = false;
			int cardIndex = -1;
			while (!found) {
				// Select card to take by entering city name
				System.out.println("Enter the name of the city on the card to collect");
				String userInput = GameState.shellInput.nextLine();
				cardIndex = game.searchUserCards(userIndex, userInput);
			
				if (cardIndex == -1) {
					System.out.println(userInput + " is not a valid City. Valid options are as follows");
					game.printUserCards(userIndex);
				} else {
					found = true;
				}
			}

			// take card
			Card card = game.users[userIndex].cards.remove(cardIndex);
			game.users[game.currentUser].cards.add(card);

			System.out.println("Card taken successfully");
			return true;
		}
	}

	// Give your card to a user
	public boolean giveCard() {
		if (game.users[game.currentUser].cards.size() == 0) {
			System.out.println("User doesn't have a card at hand");
			return false;
		}

		// Identify users in current location
		ArrayList<Integer> usersInLocation = new ArrayList<Integer>();
		for (int userNumber = 0; userNumber<game.number_of_users;userNumber++) {
			if (game.users[userNumber].location == game.users[game.currentUser].location) {
				if (userNumber == game.currentUser) {
				} else {
					// Check if user has cards
					if (game.users[userNumber].cards.size() > 0) {
						usersInLocation.add(userNumber);
					}
				}
			}
		}

		// No users in current location
		if (usersInLocation.size() == 0) {
			System.out.println("There are no users currently in your location");
			return false;
		} else {
			// Display users in current location
			for (int userNumber: usersInLocation) {
				System.out.println(game.users[userNumber].name +" is currently in your location");
			}

			// Select user to give card
			boolean userFound = false;
			int userIndex = -1;
			while (!userFound) {
				System.out.println("Enter the name of user to give card");
				String userInput = GameState.shellInput.nextLine();
				userIndex = game.searchUserName(userInput);
			
				if (userIndex == -1) {
					System.out.println(userInput + " is not a valid User.");
				} else {
					userFound = true;
				}
				game.blankLine();
			}

			boolean found = false;
			int cardIndex = -1;
			while (!found) {
				// Select card to give by entering city name
				System.out.println("Enter the name of the city on the card to give");
				String userInput = GameState.shellInput.nextLine();
				cardIndex = game.searchUserCards(game.currentUser, userInput);
			
				if (cardIndex == -1) {
					System.out.println(userInput + " is not a valid City. Valid options are as follows");
					game.printUserCards(game.currentUser);
				} else {
					found = true;
				}
			}

			// give card
			Card card = game.users[game.currentUser].cards.remove(cardIndex);
			game.users[userIndex].cards.add(card);

			System.out.println("Card given successfully");
			return true;
		}
	}

	// Cure disease if criteria is satisfied
	public boolean cureDisease() {
		CureDisease action = new CureDisease(game);
		return tryAction(action);
	}

	//Remove a cube from the current location.  If there's none, return false
  public boolean removeCube() {
		RemoveCube action = new RemoveCube(game);
		return tryAction(action);
  }

	// Create research station
  public boolean createStation() {
		CreateStation action = new CreateStation(game);
		return tryAction(action);
	}
	
	// Pass user turn to the next user
	public boolean passTurn() {
		Pass action = new Pass(game);
		return tryAction(action);
	}

	// After an action is done, update action counter,
	// draw player and infection cards,
	// and change current user where necessary
	public void actionDone() {
		game.actionsDone++;
		if (game.actionsDone >= game.getUserMaxActions()) {
			game.blankLine();
			System.out.println("All actions completed");
			System.out.println("Drawing player cards...");
			drawPlayerCard();
			drawPlayerCard();
			if (game.users[game.currentUser].cards.size() > GameState.MAX_NUMBER_OF_CARDS_IN_HAND) {
				DiscardCard action = new DiscardCard(game);
				action.perform();
			}
			game.drawCardsFromInfectionDeck(GameState.infectionRates[game.infectionRate], 1);
			game.currentUser++;
			if (game.currentUser >= game.number_of_users)
      	game.currentUser = 0;
				
			System.out.println("It's now " + game.users[game.currentUser].name + " turn.");	
			game.actionsDone = 0;
		} else {
			System.out.println("You now have "+ (game.getUserMaxActions() - game.actionsDone) +" actions left.");
		}
		game.blankLine();
	}
}
