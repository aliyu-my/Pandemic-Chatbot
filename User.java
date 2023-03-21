import java.util.ArrayList;

public class User {
  String name;  // User's name
  String type; // User's type [Generalist, Scientist...]
  ArrayList<Card> cards; // User's cards in hand
  int location;

  public static final String GENERALIST = "GENERALIST";
  public static final String SCIENTIST = "SCIENTIST";

  public static final String[] userRoles = {
    GENERALIST, SCIENTIST
  };

  User (String name, String type, int location, ArrayList<Card> cards) {
    this.type = type;
    this.name = name;
    this.location = location;
    this.cards = cards;
  }
}
