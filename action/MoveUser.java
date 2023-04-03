package action;

import java.util.ArrayList;

import game.Card;
import game.GameState;
import game.User;

//Ask the user where to move, get the city, and if valid, move user's location.
public class MoveUser extends AbstractAction {
  GameState game;
	int city = -1;
  
  public MoveUser (GameState game) {
    this.game = game;
		int random = (int) (Math.random() * inputOptions().size());
		this.city = inputOptions().get(random);
  }
	
	public MoveUser(GameState game, int city) {
		this.game = game;
		this.city = city;
	}

  public boolean canPerform() {
    return true;
  }

  public boolean perform() {
		boolean moved = false;
		while (!moved) {
			int cityToMoveTo = -1;
			String userInput = "";

			if (game.users[game.currentUser].type == User.PLAYER) {
				System.out.println ("Type where you'd like to move.");
				userInput = GameState.shellInput.nextLine();
				cityToMoveTo = game.getCityOffset(userInput);
			} else {
				cityToMoveTo = city;
				userInput = game.cities[city];
			}
			
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
			else if (game.users[game.currentUser].role == User.PILOT) {
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
    
    return true;
  }

  public boolean requiresInput() {
    return true;
  }

	public ArrayList<Integer> inputOptions () {
		ArrayList<Integer> validOptions = new ArrayList<Integer>();
		User user = game.users[game.currentUser];

		// Pilot user
		if (user.role == User.PILOT) {	
			for (int cityNumber = 0; cityNumber < game.numberCities; cityNumber++) {
				if (user.location != cityNumber) {
					validOptions.add(cityNumber);
				}
			}

			return validOptions;
		} else {
			for (int cityNumber = 0; cityNumber < game.numberCities; cityNumber++) {
				if (user.location == cityNumber) {
					// Can't travel to current location
				} 
				// adjacent cities
				else if (game.citiesAdjacent(user.location,cityNumber)) {
					validOptions.add(cityNumber);
				} 
				// research station
				else if (game.researchStations[user.location] && game.researchStations[cityNumber]) {
					validOptions.add(cityNumber);
				} 
				// has card of destination
				else if (game.searchUserCards(game.currentUser, game.cities[cityNumber]) > -1) {
					validOptions.add(cityNumber);
				}
				// has card of current location
				else if (game.searchUserCards(game.currentUser, game.cities[user.location]) > -1) {
					validOptions.add(cityNumber);
				}
			}

			return validOptions;
		}
	}
}
