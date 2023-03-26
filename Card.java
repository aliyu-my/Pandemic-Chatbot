public class Card {
  String type;  // Card type ['city', 'skill', 'epidemic']
  int city; // Applicable to city cards
  String color; // Applicable to city cards

  public static final String CITY = "CITY";
  public static final String SKILL = "SKILL";
  public static final String EPIDEMIC = "EPIDEMIC";
  
  public static final String[] TYPES = {CITY, SKILL, EPIDEMIC};
  

  Card (int city, String color) {
    type = CITY;
    this.city = city;
    this.color = color;
  }

  Card (String type) {
    this.type = type;
  }
}
