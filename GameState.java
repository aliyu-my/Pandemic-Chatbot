
//Note: you need to import scanner to use it to read from the screen.
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class GameState {
	//class variables on top
	private Scanner shellInput = new Scanner(System.in);
	//Note use a seed (1) for debugging.  
	// private Random randomGenerator = new Random();
	// private Random randomGenerator = new Random(1);
	private int numberCities = -1;	
	private int numberConnections = -1;	
	private String[] cities; //Cities
	private int[] diseaseCubes; //Number of disease cubes in the associated city.
	private int[][] connections; //The connections via offset in the cities array.
	private boolean[] researchStations; // true/false Research station in the associated city
	private int currentUser = 0;
	private int infectionRate = 0; // Current rate of infection
	private int numberOfOutbreaks = 0;
	private Card[] cityCards;	//List of all city cards and their colors
	private Card[] epidemicCards;	//Epidemic cards
	private ArrayList<Card> playerDeck;
	private ArrayList<Card> infectionDeck;
	private ArrayList<Card> playerDiscardPile = new ArrayList<Card>();
	private ArrayList<Card> infectionDiscardPile = new ArrayList<Card>();
	private ArrayList<Integer> citiesToIgnore; // Cities to ignore during outbreak
	private int number_of_users;
	private int number_of_cards_to_give; // Cards to give users when starting
	
	static int actionsDone = 0; // Number of actions done in a turn

	//##Change this to your path.##
	public static final String cityMapFileName= "fullMap.txt";
	public static final int NUMBER_EPIDEMIC_CARDS = 4; // Number of epidemic cards in game
	public static final int MAX_NUMBER_OF_CARDS_IN_HAND = 7; // Number of epidemic cards in game
	public static final int MAX_NUMBER_OF_OUTBREAKS = 8; // Max number of outbreaks before the game ends
	public static final String[] userNames = {"Al","Bob"};
	public static final int MAX_ACTIONS = 4; // Max number of actions
	public static final String[] diseaseColors = {"Blue", "Yellow", "Red", "Black"};
	public static final int[] infectionRates = {2, 2, 2, 3, 3, 4, 4}; // Tracks the number of infection cards to be drawn
	
	private boolean[] curedDiseases = new boolean[diseaseColors.length]; // Tracks all diseases status
	private boolean[] eradicatedDiseases = new boolean[diseaseColors.length]; // Diseases that are cured and can no longer spawn
	public User[] users;
	
	//Constructor
	//Initializes the board and prepares the game
	GameState () {
		readCityGraph();
		createCityAndEpidemicCards();
		setPlayerAndInfectionDeck();
		infectCities();
		createUsersAndGiveCards();
		System.out.println("Enter a commend: ");
		blankLine();
	}

	/************************************ Action Functions ******************************/
	//Ask the user where to move, get the city, and if valid, move the user's location.
	void moveUser() {
		boolean moved = false;
		while (!moved) {
			System.out.println ("Type where you'd like to move.");
			String userInput = shellInput.nextLine();
			int cityToMoveTo = getCityOffset(userInput);
		
			if (cityToMoveTo == -1) {
				System.out.println(userInput + " is not a valid city. Try one of these.");
				printAdjacentCities();
			}
			//If adjacent move the user
			else if (citiesAdjacent(users[currentUser].location,cityToMoveTo)) {
				System.out.println("The user has moved from " +
					cities[users[currentUser].location] + " to " + 
					cities[cityToMoveTo] + ".");
				users[currentUser].location = cityToMoveTo;
				moved = true;
			}
			// If user is in research station, and destination has researh station
			else if (researchStations[users[currentUser].location] && researchStations[cityToMoveTo]) {
				System.out.println("User has move from "+ 
					cities[users[currentUser].location] + " to " + 
					cities[cityToMoveTo] + " using research stations.");
				users[currentUser].location = cityToMoveTo;
				moved = true;
			}
			// If user has card of city, discard that card and move user
			else {
				int userCard = searchUserCards(currentUser, userInput);
				if (userCard > -1) {
					Card card = users[currentUser].cards.remove(userCard);
					System.out.println(cities[card.city] +" card used to move from "+ 
						cities[users[currentUser].location] + " to " + 
						cities[cityToMoveTo] + ".");
					users[currentUser].location = cityToMoveTo;
					moved = true;
					playerDiscardPile.add(card);
				}
				else {
					// IF user has card of current location, discard that card and move user
					userCard = searchUserCards(currentUser, cities[users[currentUser].location]);
					if (userCard > -1) {
						Card card = users[currentUser].cards.remove(userCard);
						System.out.println(cities[card.city] +" card used to move from "+ 
							cities[users[currentUser].location] + " to " + 
							cities[cityToMoveTo] + ".");
						users[currentUser].location = cityToMoveTo;
						moved = true;
						playerDiscardPile.add(card);
					} else {

						System.out.println ("You can't move to " + userInput + ".  Try one of these.");
						printAdjacentCities();
						blankLine();
					}
				}
			}

		}
		
	}
	
	// Draw from player deck and give user
	void drawPlayerCard() {
		if (playerDeck.size() == 0) {
			System.out.println("No more player cards in deck. Game over");
			/* Game over. Do something */
			return;
		}
		Card card = playerDeck.remove(playerDeck.size() - 1);
		if (card.type == Card.EPIDEMIC) {
			epidemicCardDrawn(card, -1);
		} else {
			users[currentUser].cards.add(card);
			System.out.print("Card drawn; ");
			System.out.println(cities[card.city] +": "+ card.color);
		}
	}

	// Cure disease if criteria is satisfied
	boolean cureDisease() {
		if (!researchStations[users[currentUser].location]) {
			System.out.println("You are not at a research station");
			return false;
		}
		
		// Group user cards by their colors
		int[] colors = new int[diseaseColors.length];
		for (int cards = 0; cards < users[currentUser].cards.size(); cards++) {
			Card card = users[currentUser].cards.get(cards);
			for (int color = 0; color < diseaseColors.length; color++) {
				if (diseaseColors[color] == card.color) {
					colors[color]++;
				}
			}
		}
		
		// check if user has cards at threshold
		boolean found = false;
		for (int color = 0; color < colors.length; color++) {
			if (colors[color] >= 5) {
				found = true;
				// Identify the cards to remove
				ArrayList<Card> cardsToRemove = new ArrayList<Card>(); 
				for (int cards = 0; cards < users[currentUser].cards.size(); cards++) {
					Card card = users[currentUser].cards.get(cards);
					if (card.color.compareTo(diseaseColors[color]) == 0) {
						cardsToRemove.add(card);
					}
				}

				// Now remove the cards after identifing them
				for (int cards = 0; cards < cardsToRemove.size(); cards++) {
					Card card = cardsToRemove.get(cards);
					users[currentUser].cards.remove(card);
					playerDiscardPile.add(card);
				}

				// disease is now cured
				curedDiseases[color] = true;
				System.out.println(diseaseColors[color] +" is now cured");

				checkIfDiseaseIsEradicated(color);
			}
		}

		if (found) {
			checkIfAllDiseasesAreCured();
			return true; 
		} else {
			System.out.println("You do not have 5 cards of the same color");
			return false;
		}
	}

	// Take a card from a user
	boolean takeCard() {
		if (users[currentUser].cards.size() >= 7) {
			System.out.println("User cannot have more than 7 cards at hand");
		}
		// Identify users in current location
		ArrayList<Integer> usersInLocation = new ArrayList<Integer>();
		for (int userNumber = 0; userNumber<number_of_users;userNumber++) {
			if (users[userNumber].location == users[currentUser].location) {
				if (userNumber == currentUser) {
				} else {
					usersInLocation.add(userNumber);
				}
			}
		}

		// No users in current location
		if (usersInLocation.size() == 0) {
			System.out.println("There are no users currently in your location");
			return false;
		} else {
			// Display users in current location
			boolean hasCard = false;
			for (int userNumber: usersInLocation) {
				System.out.println(users[userNumber].name +" is currently in your location");
				// Make sure the user has cards
				if (users[userNumber].cards.size() > 0) {
					hasCard = true;
				}
			}

			// Users in location don't have cards
			if (!hasCard) {
				System.out.println("User(s) in current location have no cards");
				return false;
			}

			// Select user to collect card from
			boolean userFound = false;
			int userIndex = -1;
			while (!userFound) {
				System.out.println("Enter the name of user to collect card from");
				String userInput = shellInput.nextLine();
				userIndex = searchUserName(userInput);
			
				if (userIndex == -1) {
					System.out.println(userInput + " is not a valid User.");
				} else {
					userFound = true;
				}
				blankLine();
			}

			boolean found = false;
			int cardIndex = -1;
			while (!found) {
				// Select card to take by entering city name
				System.out.println("Enter the name of the city on the card to collect");
				String userInput = shellInput.nextLine();
				cardIndex = searchUserCards(userIndex, userInput);
			
				if (cardIndex == -1) {
					System.out.println(userInput + " is not a valid City. Valid options are as follows");
					printUserCards(userIndex);
				} else {
					found = true;
				}
			}

			// take card
			Card card = users[userIndex].cards.remove(cardIndex);
			users[currentUser].cards.add(card);

			System.out.println("Card taken successfully");
			return true;
		}
	}

	// Give your card to a user
	boolean giveCard() {
		if (users[currentUser].cards.size() == 0) {
			System.out.println("User doesn't have a card at hand");
		}

		// Identify users in current location
		ArrayList<Integer> usersInLocation = new ArrayList<Integer>();
		for (int userNumber = 0; userNumber<number_of_users;userNumber++) {
			if (users[userNumber].location == users[currentUser].location) {
				if (userNumber == currentUser) {
				} else {
					usersInLocation.add(userNumber);
				}
			}
		}

		// No users in current location
		if (usersInLocation.size() == 0) {
			System.out.println("There are no users currently in your location");
			return false;
		} else {
			// Display users in current location
			boolean hasMaxCards = false;
			for (int userNumber: usersInLocation) {
				System.out.println(users[userNumber].name +" is currently in your location");
				// Make sure the user has cards
				if (users[userNumber].cards.size() >= 7) {
					hasMaxCards = true;
				}
			}

			// Users in location don't have cards ****************************** fix this
			// if (!hasMaxCards) {
			// 	System.out.println("User(s) in current location have no cards");
			// 	return false;
			// }

			// Select user to give card
			boolean userFound = false;
			int userIndex = -1;
			while (!userFound) {
				System.out.println("Enter the name of user to give card");
				String userInput = shellInput.nextLine();
				userIndex = searchUserName(userInput);
			
				if (userIndex == -1) {
					System.out.println(userInput + " is not a valid User.");
				} else {
					userFound = true;
				}
				blankLine();
			}

			boolean found = false;
			int cardIndex = -1;
			while (!found) {
				// Select card to give by entering city name
				System.out.println("Enter the name of the city on the card to give");
				String userInput = shellInput.nextLine();
				cardIndex = searchUserCards(currentUser, userInput);
			
				if (cardIndex == -1) {
					System.out.println(userInput + " is not a valid City. Valid options are as follows");
					printUserCards(currentUser);
				} else {
					found = true;
				}
			}

			// give card
			Card card = users[currentUser].cards.remove(cardIndex);
			users[userIndex].cards.add(card);

			System.out.println("Card given successfully");
			return true;
		}
	}

	//Remove a cube from the current location.  If there's none, return false for an error.
	boolean removeCube() {
		int currentUserLocation = users[currentUser].location;
		if (diseaseCubes[currentUserLocation] > 0) {
			int colorIndex = searchColorIndex(searchCityCard(currentUserLocation).color);
			if (curedDiseases[colorIndex]) {
				diseaseCubes[currentUserLocation] = 0;
				System.out.println("Cured disease found. All cubes removed from location");
			} else {
				diseaseCubes[currentUserLocation]--;
				System.out.println("There are " + diseaseCubes[currentUserLocation] + " cubes left at location");
			}
			return true;
		} else {
			System.out.println("The space you're on has no disease cubes.");
			return false;
		}
	}
	
	// Create research station
	boolean createStation() {
		User user = users[currentUser];
		for (Card card: user.cards) {
			if (card.city == user.location) {
				if (researchStations[user.location]) {
					System.out.println("A research station already exists at your location");
					return false;
				} else {
					users[currentUser].cards.remove(card);
					researchStations[user.location] = true;
					System.out.println("Research station built at "+ cities[user.location]);
					return true;
				}
			}
		}

		System.out.println("You don't have a card of your current location");
		return false;
	}
	
	// After an action is done, update action counter,
	// draw player and infection cards,
	// and change current user where necessary
	void actionDone() {
		actionsDone++;
		if (actionsDone >= MAX_ACTIONS) {
			blankLine();
			System.out.println("All actions completed");
			System.out.println("Drawing player cards...");
			drawPlayerCard();
			drawPlayerCard();
			if (users[currentUser].cards.size() > MAX_NUMBER_OF_CARDS_IN_HAND) {
				discardPlayerCard();
			}
			drawCardsFromInfectionDeck(infectionRates[infectionRate], 1);
			currentUser++;
			if (currentUser >= number_of_users)
				currentUser = 0;
				
			System.out.println("It's now " + users[currentUser].name + " turn.");	
			actionsDone = 0;
		} else {
			System.out.println("You now have "+ (MAX_ACTIONS - actionsDone) +" actions left.");
		}
		blankLine();
	}
	
	/***********************************HELPER FUNCTIONS***************************************/
	void epidemicCardDrawn(Card card, int userIndex) {
		System.out.println("Epidemic card drawn by "+ users[(userIndex > -1) ? userIndex: currentUser].name);
		playerDiscardPile.add(card);
		if (infectionRate < infectionRates.length - 1) {
			infectionRate++;
		}
		
		drawCardsFromInfectionDeck(1, 3);

		// Might be fishy. Look into this ------------------
		Collections.shuffle(infectionDiscardPile);
		infectionDeck.addAll(infectionDiscardPile);
		infectionDiscardPile = new ArrayList<Card>();
		// ----------------------------------------------------

		System.out.println("Epidemic Simulation Complete");
	}
	
	// Prints a blank like to the console
	void blankLine() {
		System.out.println("");
	}
		
	// Searches for the names of other users
	int searchUserName(String name) {
		for (int userNumber = 0; userNumber<number_of_users;userNumber++) {
			if (users[userNumber].name.compareTo(name) == 0) {
				// Futher checking, might be redundant
				if (userNumber == currentUser) {
					return -1;
				} else {
					return userNumber;
				}
			}
		}

		return -1;
	}

	// Search for color index, given the color name
	int searchColorIndex(String name) {
		for (int color = 0; color < diseaseColors.length; color++) {
			if (diseaseColors[color].compareTo(name) == 0) {
				return color;
			}
		}

		return -1;
	}

	// Discard card in hand down to 7
	void discardPlayerCard() {
		System.out.println("Max number of cards in hand reached for "+ users[currentUser].name);
		while (users[currentUser].cards.size() > MAX_NUMBER_OF_CARDS_IN_HAND) {
			System.out.println("Current number of cards in hand: "+ users[currentUser].cards.size());
			System.out.println("Enter city of card to discard");
			String userInput = shellInput.nextLine();
			int cardIndex = searchUserCards(currentUser, userInput);
			if (cardIndex == -1) {
				System.out.println(userInput +" is not a valid card. Here are the cards you can choose from");
				printUserCards(currentUser);
			} else {
				Card card = users[currentUser].cards.remove(cardIndex);
				playerDiscardPile.add(card);
				System.out.println("Card discarded");
			}
		}
	}

	// Search for a user card, given the city and user
	int searchUserCards(int userIndex, String city) {
		User user = users[userIndex];
		for (int cards = 0; cards<user.cards.size();cards++) {
			if (cities[user.cards.get(cards).city].compareTo(city) == 0) {
				return cards;
			}
		}

		return -1;
	}
	
	// Search for a city card, given the city's offset.
	Card searchCityCard(int cityNumber) {
		for (int card = 0; card < cityCards.length; card ++) {
			if (cityCards[card].city == cityNumber) {
				return cityCards[card];
			}
		}
		return null;
	}

	// Check if no cubes of the disease exist on map
	boolean checkIfDiseaseIsEradicated(int colorIndex) {
		boolean eradicated = true;
		for (int cityNumber = 0;  cityNumber < numberCities; cityNumber ++) {
			if (diseaseCubes[cityNumber] > 0) {
				if (cityCards[cityNumber].color.compareTo(diseaseColors[colorIndex]) == 0) {
					eradicated = false;
				}
			}
		}

		if (eradicated) {
			System.out.println("No more disease cubes of color in game. It's now eradicated");
			eradicatedDiseases[colorIndex] = true;
			return true;
		}

		return false;
	}
	
	// Check if all diseases are cured.
	boolean checkIfAllDiseasesAreCured() {
		boolean allCured = true;
		for (int color = 0; color < curedDiseases.length; color++) {
			if (!curedDiseases[color]) {
				allCured = false;
			}
		}

		if (allCured) {
			// --------------- all diseases cured-------------- end the game
			System.out.println("All diseases cured");
			System.out.println("Congratulations. You've won!");
			System.exit(0);
			return true;
		} else {
			return false;
		}
	}
	
	// Draw infection cards and simulate outbreaks where necessary
	void drawCardsFromInfectionDeck(int numberOfCardsToDraw, int _numberOfCubesToDraw) {
		System.out.println("Drawing from infection deck");
		citiesToIgnore = new ArrayList<Integer>();

		for (int number = 0; number < numberOfCardsToDraw; number++) {
			int numberOfCubesToDraw = _numberOfCubesToDraw;
			Card card = infectionDeck.remove(infectionDeck.size() - 1);
			System.out.println(" - Card drawn: "+ cities[card.city]);
			if (eradicatedDiseases[searchColorIndex(card.color)]) {
				System.out.println(" - Disease already eradicated, no cubes are added");
			} else {
				while (numberOfCubesToDraw > 0) {
					if (diseaseCubes[card.city] == 3) {
						if (!citiesToIgnore.contains(card.city)) {
							System.out.println(" - - Outbreak at city");
							citiesToIgnore.add(card.city);
							outbreak(card.city);
						} else {
							System.out.println("-analytics- city already infected");
						}
					} else {
						diseaseCubes[card.city]++;
						System.out.println(" - Cube added");
					}
					numberOfCubesToDraw--;
				}
			}
			infectionDiscardPile.add(card);
		}

		System.out.println("Drawing complete");
		if (numberOfOutbreaks >= MAX_NUMBER_OF_OUTBREAKS) {
			System.out.println("Maximum number of outbreaks reached. Game over");
			/**************************************Game over, Do somthing! ***********************************/
			System.exit(0);
		}
	}

	// Infect all neighbouring cities
	void outbreak(int city) {
		numberOfOutbreaks++;
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			if (citiesAdjacent(city,cityNumber)) {
				if (diseaseCubes[cityNumber] == 3) {
					if (!citiesToIgnore.contains(cityNumber)) {
						System.out.println(" - - Futher Outbreak at "+ cities[cityNumber]);
						citiesToIgnore.add(cityNumber);
						outbreak(cityNumber);
					} 
				} else {
					diseaseCubes[cityNumber]++;
					System.out.println(" - - Cube added to "+ cities[cityNumber]);
				}
			}
		}
	}

	//Loop through the city array, and return the offset of the cityName parameter in that
	//array.  Return -1 if the cityName is not in the array.
	int getCityOffset(String cityName) {
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			if (cityName.compareTo(cities[cityNumber]) == 0) 
				return cityNumber;
		}
		return -1;
	}

	//Look through the connections and see if the city numbers are in them.  If
	//Return whether they are in the list.
	boolean citiesAdjacent(int city1,int city2) {
		for (int compareConnection = 0; compareConnection < numberConnections; compareConnection ++) {
			if ((connections[0][compareConnection] == city1) &&
				(connections[1][compareConnection] == city2))
				return true;
			//Need to check both ways A to B and B to A as only one connection is stored.
			else if ((connections[0][compareConnection] == city2) &&
					(connections[1][compareConnection] == city1))
					return true;		
		}
		return false;
	}

	// Shuffle given cards
	ArrayList<Card> shuffleCards(Card[] array) {
		int index;
		Random random = new Random();
		for (int i = array.length - 1; i > 0; i--)
		{
			index = random.nextInt(i + 1);
			if (index != i)
			{
				Card temp = array[i];
				array[i] = array[index];
				array[index] = temp;
			}
		}
		return new ArrayList<Card>(Arrays.asList(array));
	}
		
	/**********************************SETUP GAME FUNCTIONS***************************************/

	//Open the city file, allocate the space for the cities, and connections, then read the 
	//cities, and then read the connections.  It uses those class variables.
	void readCityGraph() {

		//Open the file and read it.  
		try {
		      File fileHandle = new File(cityMapFileName);
		      Scanner mapFileReader = new Scanner(fileHandle);

		      //read the number of cities and allocate variables.
		      numberCities = mapFileReader.nextInt();
		      mapFileReader.nextLine();  //read the rest of the line after the int
		      cities = new String[numberCities]; //allocate the cities array
		      diseaseCubes = new int[numberCities];
					researchStations = new boolean[numberCities];
					researchStations[0] = true;
		      
		      //tead the number of connections and allocate variables.
		      numberConnections = mapFileReader.nextInt();
		      mapFileReader.nextLine();  //read the rest of the line after the int
		      connections = new int[2][numberConnections];

		      //read cities
		      readCities(numberCities,mapFileReader);
		      //readConnections 
		      readConnections(numberConnections,mapFileReader);
		      
		      mapFileReader.close();
		    } 
		 
		 catch (FileNotFoundException e) {
		      System.out.println("An error occurred reading the city graph.");
		      e.printStackTrace();
		    }
	}
	
	/***Code for the city graph ***/
	//Read the specified number of cities.  If it crashes, it should throw to the calling catch.
	void readCities(int numCities, Scanner scanner) {
		//A simple loop reading cities in.  It assumes the file is text with the last character 
		//of the line being the last letter of the city name.
		for (int cityNumber = 0; cityNumber < numCities; cityNumber++) {
			String cityName = scanner.nextLine();
			cities[cityNumber] = cityName;
		}
	}

	// Initial infection
	// first 3 cities get 3 cubes each, next 3 get 2 cubes each, final 3 get 1 cube each
	void infectCities() {
		System.out.println("Drawing infection cards...");
		for (int cards = 0; cards < 9; cards++) {
			// Get the card on top
			Card card = infectionDeck.remove(infectionDeck.size() - 1);
			if (cards < 3) {
				diseaseCubes[card.city] = 3;
			} else if (cards < 6) {
				diseaseCubes[card.city] = 2;
			} else {
				diseaseCubes[card.city] = 1;
			}

			infectionDiscardPile.add(card);
		}
	}

	//Read the specified number of connections.  If it crashes, it should throw to the calling catch.
	void readConnections(int numConnections, Scanner scanner) {
		//A simple loop reading connections in.  It assumes the file is text with the last 
		//character of the line being the last letter of the city name.  The two 
		//cities are separated by a ; with no spaces
		for (int connectionNumber = 0; connectionNumber < numConnections; connectionNumber++) {
			String connectionName = scanner.nextLine();
			String cityName[] = connectionName.split(";");
			int firstCityOffset = getCityOffset(cityName[0]);
			int secondCityOffset = getCityOffset(cityName[1]);
			connections[0][connectionNumber] = firstCityOffset;
			connections[1][connectionNumber] = secondCityOffset;
		}
	}		

	void createCityAndEpidemicCards () {
		cityCards = new Card[numberCities];
		int colorCount = 0;
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			cityCards[cityNumber] = new Card(cityNumber, diseaseColors[colorCount]);
			colorCount++;
			if (colorCount >= diseaseColors.length) {
				colorCount = 0;
			}
		}
		
		epidemicCards = new Card[NUMBER_EPIDEMIC_CARDS];
		for (int cards = 0; cards < NUMBER_EPIDEMIC_CARDS; cards++) {
			epidemicCards[cards] = new Card(Card.EPIDEMIC);
		}
	}

	// We set the player deck after combining and shuffling city and epidemic cards
	void setPlayerAndInfectionDeck () {
		Card[] combinedCards = new Card[cityCards.length + epidemicCards.length];
		System.arraycopy(cityCards, 0, combinedCards, 0, cityCards.length);
		System.arraycopy(epidemicCards, 0, combinedCards, cityCards.length, epidemicCards.length);
		playerDeck = shuffleCards(combinedCards);

		Card[] cardsToShuffle = new Card[cityCards.length];
		System.arraycopy(cityCards, 0, cardsToShuffle, 0, cityCards.length);
		infectionDeck = shuffleCards(cardsToShuffle);
	}
	
	// Create users by collecting their names and roles,
	// Then intialize their player cards
	void createUsersAndGiveCards() {
		boolean done = false;
		int numberOfUsers = 0;

		while (!done) {
			System.out.println("Enter how mamy users are playing: ");
			try {
				numberOfUsers = Integer.parseInt(shellInput.nextLine());
				if (numberOfUsers > 4) {
					System.out.println("You cannot have more than 4 players");
				} else if (numberOfUsers < 2) {
					System.out.println("You cannot have less than 2 players");
				} else {
					done = true;
				}
			} catch (Exception e) {
				System.out.println("Invalid input");
			}
		}

		// update instance variables
		number_of_users = numberOfUsers;
		number_of_cards_to_give = 6 - numberOfUsers;

		users = new User[numberOfUsers];

		boolean[] rolesUsed = new boolean[User.userRoles.length]; 
		for (int userIndex = 0; userIndex < numberOfUsers; userIndex++) {
			boolean valid = false;
			String name = "";

			while (!valid) {
				System.out.println("Please enter the name of user "+ (userIndex + 1) +": ");
				name = shellInput.nextLine();
				if (name.trim().length() <= 0) {
					System.out.println("Invalid name provided");
				} else {
					boolean duplicate = false;
					for (int current = 0; current < userIndex; current++) {
						if (users[current].name.compareTo(name) == 0) {
							duplicate = true;
						}
					}

					if (duplicate) {
						System.out.println("A user with the name already exist");
					} else {
						valid = true;
					}
				}
			}
			
			blankLine();
			valid = false;
			String roleSelected = "";
			while (!valid) {
				System.out.println("Enter your user role. Valid choices are as follows");
				for (int role = 0; role < rolesUsed.length; role++) {
					if (!rolesUsed[role]) {
						System.out.println(User.userRoles[role]);
					}
				}
				
				blankLine();
				roleSelected = shellInput.nextLine();
				for (int role = 0; role < User.userRoles.length; role++) {
					if (roleSelected.toUpperCase().compareTo(User.userRoles[role]) == 0) {
						if (rolesUsed[role]) {
							System.out.println("This role is already selected");
						} else {
							roleSelected = User.userRoles[role];
							rolesUsed[role] = true;
							valid = true;
							break;
						}
					}
				}

				if (!valid) 
					System.out.println(roleSelected +" is an invalid input");
			}
			
			System.out.println("Drawing "+ number_of_cards_to_give +" player cards");

			ArrayList<Card> playerCards = new ArrayList<Card>();
			Card epidemicDrawn = new Card("");
			boolean cardDrawn = false;
			for (int cards = 0; cards < number_of_cards_to_give; cards++) {
				Card card = playerDeck.remove(playerDeck.size() - 1);
				if (card.type == Card.EPIDEMIC) {
					cardDrawn = true;
					epidemicDrawn = card;
				} else {
					playerCards.add(card);
				}
			}

			users[userIndex] = new User(name, roleSelected, 0, playerCards);
			
			if (cardDrawn)
				epidemicCardDrawn(epidemicDrawn, userIndex);
		}
	
		System.out.println("Users created successfully");
	}

	/**************************************PRINT FUNCTIONS***************************************/
	
	void printNumberOfActionsLeft() {
		System.out.println("The current user is " + users[currentUser].name);
		System.out.println("They have " + (MAX_ACTIONS - actionsDone) +" action(s) left for their turn");
	}
	
	void printEradicatedDiseases() {
		boolean notFound = true;
		for (int cured = 0; cured < eradicatedDiseases.length; cured++) {
			if (eradicatedDiseases[cured]) {
				notFound = false;
				System.out.println(diseaseColors[cured] +" is eradicated and will not spawn again");
			}
		}

		if (notFound) {
			System.out.println("No eradicated diseases yet");
		}
		blankLine();
	}

	void printResearchStations() {
		System.out.println("The current user is " + users[currentUser].name);
		for (int cityNumber = 0;  cityNumber < numberCities; cityNumber ++) {
			if (researchStations[cityNumber]) {
				System.out.println(cities[cityNumber]);
			}
		}
		blankLine();
	}
	
	//Print out the cities adjacent to the userLocation
	void printAdjacentCities () {
		System.out.println("The current user is " + users[currentUser].name);
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			if (citiesAdjacent(users[currentUser].location,cityNumber)) {
				System.out.println(cities[cityNumber]);
			}
		}
		blankLine();
	}
	
	void printInfectedCities() {
		System.out.println("The current user is " + users[currentUser].name);
		for (int cityNumber = 0;  cityNumber < numberCities; cityNumber ++) {
			if (diseaseCubes[cityNumber] > 0) {
				System.out.println(cities[cityCards[cityNumber].city] + " ("+ cityCards[cityNumber].color +") has " + diseaseCubes[cityNumber] + " cubes.");
			}
		}
		blankLine();
	}
	
	//Print out all the users' locations.
	void printUserLocations() {
		System.out.println("The current user is " + users[currentUser].name);
		for (int userNumber = 0; userNumber<number_of_users;userNumber++) {
			int printUserLocation = users[userNumber].location;
			
			System.out.println (users[userNumber].name + " is in " + cities[printUserLocation]);
		}
		blankLine();
	}
	
	void printCuredDiseases() {
		boolean notFound = true;
		for (int cured = 0; cured < curedDiseases.length; cured++) {
			if (curedDiseases[cured]) {
				notFound = false;
				System.out.println(diseaseColors[cured] +" is cured");
			}
		}

		if (notFound) {
			System.out.println("No cured diseases yet");
		}
		blankLine();
	}
	
	//Print out the full list of connections.
	void printConnections() {
		System.out.println(numberConnections + " Connections.");
		for (int connectionNumber = 0; connectionNumber < numberConnections; connectionNumber++) {
			String firstCity = cities[connections[0][connectionNumber]];
			String secondCity = cities[connections[1][connectionNumber]];
			System.out.println(firstCity + " " + secondCity);
		}
		blankLine();
	}
			
	//Print out all the cards in the game.
	void printCityCards() {
		System.out.println("The current user is " + users[currentUser].name);
		for (int card = 0; card < cityCards.length; card ++) {
			System.out.println(cities[cityCards[card].city] +": "+ cityCards[card].color);
		}
		blankLine();
	}
		
	//Print out the cards currently with users.
	void printUserCards() {
		System.out.println("The current user is " + users[currentUser].name);
		blankLine();
		for (int userNumber = 0; userNumber<number_of_users;userNumber++) {
			User user = users[userNumber];
			System.out.println(user.name +" has "+ user.cards.size() +" cards.");
	
			for (Card card: user.cards) {
				System.out.println(cities[card.city] +": "+ card.color);
			}
			blankLine();
		}
	}
	
	//Print out the cards of a selected user.
	void printUserCards(int user) {
		blankLine();
		for (Card card: users[user].cards) {
			System.out.println(cities[card.city] +": "+ card.color);
		}
		blankLine();
	}

	void printOutbreaks() {
		System.out.println(numberOfOutbreaks +" outbreaks have occured");
	}

	//Print out the list of all the cities.
	void printCities() {
		System.out.println(numberCities + " Cities.");
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			System.out.println(cities[cityNumber]);
		}
		blankLine();
	}
	
}