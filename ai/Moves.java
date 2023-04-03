package ai;

import action.AbstractAction;
import action.CreateStation;
import action.CureDisease;
import action.DiscardCard;
import action.DrawPlayerCard;
import action.MoveUser;
import action.Pass;
import action.RemoveCube;
import game.GameState;

public class Moves {
  
  public GameState state;

  public static final String PASS = "PASS";
  public static final String CURE = "CURE";
  public static final String REMOVE = "REMOVE";
  public static final String CREATE = "CREATE";
  public static final String DRAW_CARD = "DRAW_CARD";
  public static final String MOVE_USER = "MOVE_USER";
  public static final String DISCARD_CARD = "DISCARD_CARD";

  public static final String[] allMoves = {
    PASS, MOVE_USER, REMOVE, CURE, CREATE
  };

  public static final String[] staticMoves = {
    PASS, REMOVE, CURE, CREATE
  };

  public Moves (GameState state) {
    this.state = state;
  }

  public AbstractAction getMove(String type) {
    if (type == PASS) {
      return new Pass(state);
    } else if (type == CURE) {
      return new CureDisease(state);
    } else if (type == REMOVE) {
      return new RemoveCube(state);
    } else if (type == CREATE) {
      return new CreateStation(state);
    } else if (type == DRAW_CARD) {
      return new DrawPlayerCard(state);
    } else if (type == MOVE_USER) {
      return new MoveUser(state);
    } else if (type == DISCARD_CARD) {
      return new DiscardCard(state);
    } else {
      return null;
    }
  }
}
