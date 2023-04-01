import java.util.ArrayList;

public class User {
  String name;  // User's name
  String type; // User's type [Generalist, Scientist...]
  ArrayList<Card> cards; // User's cards in hand
  int location; // User's current location

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
  User (String name, String type, int location, ArrayList<Card> cards) {
    this.type = type;
    this.name = name;
    this.location = location;
    this.cards = cards;
  }
}
