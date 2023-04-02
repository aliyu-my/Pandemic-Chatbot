package game;
public class Card {
  public String type;  // Card type ['city', 'skill', 'epidemic']
  public int city; // Applicable to city cards
  public String color; // Applicable to city cards

  public static final String CITY = "CITY";
  public static final String SKILL = "SKILL";
  public static final String EPIDEMIC = "EPIDEMIC";
  
  public static final String[] TYPES = {CITY, SKILL, EPIDEMIC};
  

  public Card (int city, String color) {
    type = CITY;
    this.city = city;
    this.color = color;
  }

  public Card (String type) {
    this.type = type;
  }
}
