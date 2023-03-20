public class Card {
  String type;  // Card type ['city', 'skill']
  int city; // Applicable to city cards
  String color; // Applicable to city cards

  Card (int city, String color) {
    type = "city";
    this.city = city;
    this.color = color;
  }
}
