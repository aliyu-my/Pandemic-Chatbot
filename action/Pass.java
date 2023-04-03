package action;

import game.GameState;

public class Pass extends AbstractAction {
  GameState game;

  public Pass (GameState game) {
    this.game = game;
  }
	
  public boolean canPerform() {
    return true;
  }

  public boolean perform() {
    game.actionsDone = game.getUserMaxActions();
    System.out.println(game.users[game.currentUser].name +" has passed their turn");
    return true;
  }

  public boolean requiresInput() {
    return false;
  }

}
