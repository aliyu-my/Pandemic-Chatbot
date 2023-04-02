import java.util.ArrayList;

public class Action {
  GameState game;
  
  Action (GameState game) {
    this.game = game;
  }
	
	//Ask the user where to move, get the city, and if valid, move the user's location.
	public void moveUser() {
		boolean moved = false;
		while (!moved) {
			System.out.println ("Type where you'd like to move.");
			String userInput = game.shellInput.nextLine();
			int cityToMoveTo = game.getCityOffset(userInput);
		
			if (cityToMoveTo == -1) {
				System.out.println(userInput + " is not a valid city. Try one of these.");
				game.printAdjacentCities();
			}
			//If adjacent move the user
			else if (game.citiesAdjacent(game.users[game.currentUser].location,cityToMoveTo)) {
				System.out.println("The user has moved from " +
          game.cities[game.users[game.currentUser].location] + " to " + 
          game.cities[cityToMoveTo] + ".");
				game.users[game.currentUser].location = cityToMoveTo;
				moved = true;
			}
			// If user is a pilot, they can travel anywhere
			else if (game.users[game.currentUser].type == User.PILOT) {
				System.out.println("Pilot user has moved from " +
          game.cities[game.users[game.currentUser].location] + " to " + 
          game.cities[cityToMoveTo] + ".");
				game.users[game.currentUser].location = cityToMoveTo;
				moved = true;
			}
			// If user is in research station, and destination has researh station
			else if (game.researchStations[game.users[game.currentUser].location] && game.researchStations[cityToMoveTo]) {
				System.out.println("User has move from "+ 
          game.cities[game.users[game.currentUser].location] + " to " + 
          game.cities[cityToMoveTo] + " using research stations.");
				game.users[game.currentUser].location = cityToMoveTo;
				moved = true;
			}
			// If user has card of city, discard that card and move user
			else {
				int userCard = game.searchUserCards(game.currentUser, userInput);
				if (userCard > -1) {
					Card card = game.users[game.currentUser].cards.remove(userCard);
					System.out.println(game.cities[card.city] +" card used to move from "+ 
            game.cities[game.users[game.currentUser].location] + " to " + 
            game.cities[cityToMoveTo] + ".");
					game.users[game.currentUser].location = cityToMoveTo;
					moved = true;
					game.playerDiscardPile.add(card);
				}
				else {
					// IF user has card of current location, discard that card and move user
					userCard = game.searchUserCards(game.currentUser, game.cities[game.users[game.currentUser].location]);
					if (userCard > -1) {
						Card card = game.users[game.currentUser].cards.remove(userCard);
						System.out.println(game.cities[card.city] +" card used to move from "+ 
              game.cities[game.users[game.currentUser].location] + " to " + 
              game.cities[cityToMoveTo] + ".");
            game.users[game.currentUser].location = cityToMoveTo;
						moved = true;
						game.playerDiscardPile.add(card);
					} else {

						System.out.println ("You can't move to " + userInput + ".  Try one of these.");
						game.printAdjacentCities();
						game.blankLine();
					}
				}
			}

		}
		
	}
	
  // Draw from player deck and give user
	public void drawPlayerCard() {
		if (game.playerDeck.size() == 0) {
			System.out.println("No more player cards in deck. Game over");
			/* Game over. Do something */
			return;
		}
		Card card = game.playerDeck.remove(game.playerDeck.size() - 1);
		if (card.type == Card.EPIDEMIC) {
			game.epidemicCardDrawn(card, -1);
		} else {
			game.users[game.currentUser].cards.add(card);
			System.out.print("Card drawn; ");
			System.out.println(game.cities[card.city] +": "+ card.color);
		}
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
				String userInput = game.shellInput.nextLine();
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
				String userInput = game.shellInput.nextLine();
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
				String userInput = game.shellInput.nextLine();
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
				String userInput = game.shellInput.nextLine();
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
				(game.users[game.currentUser].type == User.SCIENTIST ? 1: 0);

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

	//Remove a cube from the current location.  If there's none, return false for an error.
  public boolean removeCube() {
		int currentUserLocation = game.users[game.currentUser].location;
		if (game.diseaseCubes[currentUserLocation] > 0) {
			int colorIndex = game.searchColorIndex(game.searchCityCard(currentUserLocation).color);
			if (game.curedDiseases[colorIndex]) {
				game.diseaseCubes[currentUserLocation] = 0;
				System.out.println("Cured disease found. All cubes removed from location");
			} else {
				if (game.users[game.currentUser].type == User.MEDIC) {
					game.diseaseCubes[currentUserLocation] = 0;
					System.out.println("All cubes removed by Medic user");
				} else {
					game.diseaseCubes[currentUserLocation]--;
					System.out.println("There are " + game.diseaseCubes[currentUserLocation] + " cubes left at location");
				}
			}
			return true;
		} else {
			System.out.println("The space you're on has no disease cubes.");
			return false;
		}
  }

	// Create research station
  public boolean createStation() {
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
				game.discardPlayerCard();
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
