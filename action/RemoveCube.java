package action;

import game.GameState;
import game.User;

// Remove a cube from the current location.  If there's none, return false for an error.
public class RemoveCube extends AbstractAction {
  GameState game;
  
  public RemoveCube (GameState game) {
    this.game = game;
  }
	
  public boolean canPerform() {
		int currentUserLocation = game.users[game.currentUser].location;
		if (game.diseaseCubes[currentUserLocation] > 0) {
			return true;
		} else {
			return false;
		}
  }

  public boolean perform() {
		int currentUserLocation = game.users[game.currentUser].location;
		if (game.diseaseCubes[currentUserLocation] > 0) {
			int colorIndex = game.searchColorIndex(game.searchCityCard(currentUserLocation).color);
			if (game.curedDiseases[colorIndex]) {
				game.diseaseCubes[currentUserLocation] = 0;
				System.out.println("Cured disease found. All cubes removed from location");
			} else {
				if (game.users[game.currentUser].role == User.MEDIC) {
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

  public boolean requiresInput() {
    return false;
  }

}
