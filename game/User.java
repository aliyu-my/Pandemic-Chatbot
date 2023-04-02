package game;
import java.util.ArrayList;

public class User {
  public String name;  // User's name
  public String role; // User's type [Generalist, Scientist...]
  public ArrayList<Card> cards; // User's cards in hand
  public int location; // User's current location
  public String type; // Player or Agent

  public static final String PLAYER = "PLAYER";
  public static final String AGENT = "AGENT";

  // The different user roles that a player can choose
  public static final String GENERALIST = "GENERALIST"; // Can take 5 actions
  public static final String SCIENTIST = "SCIENTIST"; // Can cure a disease with a card less
  public static final String MEDIC = "MEDIC"; // Removes all cubes in a city, even if they are not cured
  public static final String PILOT = "PILOT"; // Can move/fly to any city directly

  // List of user roles as an array
  public static final String[] userRoles = {
    GENERALIST, SCIENTIST, MEDIC, PILOT
  };

  // Constructer to initialize a user
  public User (String name, String role, String type, int location, ArrayList<Card> cards) {
    this.role = role;
    this.name = name;
    this.type = type;
    this.location = location;
    this.cards = cards;
  }
}
