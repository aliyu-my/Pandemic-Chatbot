package action;

import game.Card;
import game.GameState;
import game.User;

	//Ask the user where to move, get the city, and if valid, move user's location.
public class MoveUser extends AbstractAction {
  GameState game;
  
  public MoveUser (GameState game) {
    this.game = game;
  }
	
  public boolean canPerform() {
    return true;
  }

  public boolean perform() {
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

}
