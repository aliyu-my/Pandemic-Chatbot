
//Note: you need to import scanner to use it to read from the screen.
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList;
import java.util.Arrays;
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
	private int[] userLocation = {0,0};  //These are the users' location that can change.
	private boolean[] researchStations; // true/false Research station in the associated city
	private int currentUser = 0;
	private Card[] cityCards;	//List of all city cards and their colors
	private Card[] epidemicCards;	//Epidemic cards
	private ArrayList<Card> playerDeck;
	private ArrayList<Card> infectionDeck;
	private ArrayList<Card> playerDiscardPile = new ArrayList<Card>();
	private ArrayList<Card> infectionDiscardPile = new ArrayList<Card>();
	private ArrayList<Integer> citiesToIgnore; // Cities to ignore during outbreak
	
	static int actionsDone = 0; // Number of actions done in a turn

	//##Change this to your path.##
	public static final String cityMapFileName= "fullMap.txt";
	public static final int NUMBER_USERS = 2;
	public static final int NUMBER_EPIDEMIC_CARDS = 4; // Number of epidemic cards in game
	public static final String[] userNames = {"Al","Bob"};
	public static final int MAX_ACTIONS = 4; // Max number of actions
	public static final String[] diseaseColors = {"Blue", "Yellow", "Red", "Black"};
	public static final int NUMBER_OF_CARDS_TO_GIVE = 6 - NUMBER_USERS; // Cards to give users when starting
	
	private boolean[] curedDiseases = new boolean[diseaseColors.length];
	public User[] users;
	
	GameState () {
		readCityGraph();
		createCityAndEpidemicCards();
		setPlayerAndInfectionDeck();
		infectCities();
		createUsersAndGiveCards();
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
		
			//If adjacent move the user, if not print an error.
			else if (citiesAdjacent(users[currentUser].location,cityToMoveTo)) {
				System.out.println("The user has moved from " +
					cities[users[currentUser].location] + " to " + 
					cities[cityToMoveTo] + ".");
				users[currentUser].location = cityToMoveTo;
				moved = true;
			}
			else {
				System.out.println ("You can't move to " + userInput + ".  Try one of these.");
				printAdjacentCities();
				}
			
			blankLine();
		}
		
	}
	
	void drawPlayerCard() {
		Card card = playerDeck.remove(playerDeck.size() - 1);
		if (card.type == Card.EPIDEMIC) {
			System.out.println("Epidemic card drawn by "+ users[currentUser].name);
		} else {
			users[currentUser].cards.add(card);
			System.out.println("Card drawn;");
			System.out.println(cities[card.city] +": "+ card.color);
		}
	}

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
					if (card.color == diseaseColors[color]) {
						cardsToRemove.add(card);
					}
				}

				// Now remove the cards after identifing them
				for (int cards = 0; cards < cardsToRemove.size(); cards++) {
					Card card = cardsToRemove.get(cards);
					users[currentUser].cards.remove(card);
					playerDiscardPile.add(card);
				}

				// disease color is now cured
				curedDiseases[color] = true;
				System.out.println(diseaseColors[color] +" is now cured. Congratulations");
			}
		}

		if (found) {
			boolean allCured = true;
			for (int color = 0; color < curedDiseases.length; color++) {
				if (!curedDiseases[color]) {
					allCured = false;
				}
			}

			if (allCured) {
				// --------------- all diseases cured-------------- end the game
				System.out.println("All diseases cured");
				return false;
			} else {
				return true;
			}
		} else {
			System.out.println("You do not have 5 cards of the same color");
			return false;
		}
	}

	boolean takeCard() {
		// Show all cards currently with users
		// printUserCards();

		// Identify users in current location
		ArrayList<Integer> usersInLocation = new ArrayList<Integer>();
		for (int userNumber = 0; userNumber<NUMBER_USERS;userNumber++) {
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

	//Remove a cube from the current location.  If there's none, return false for an error.
	boolean removeCube() {
		int currentUserLocation = userLocation[currentUser];
		if (diseaseCubes[currentUserLocation] > 0) {
			if (curedDiseases[currentUserLocation]) {
				diseaseCubes[currentUserLocation] = 0;
				System.out.println("All cubes removed from location");
			} else {
				diseaseCubes[currentUserLocation]--;
				System.out.println("There are " + diseaseCubes[currentUserLocation] + " left");
			}
			return true;
		} else {
			System.out.println("The space you're on has no disease cubes.");
			return false;
		}
	}
	
	void actionDone() {
		actionsDone++;
		if (actionsDone >= 4) {
			blankLine();
			drawCardsFromInfectionDeck(2);
			currentUser++;
			currentUser%=NUMBER_USERS;
			System.out.println("It's now " + users[currentUser].name + " turn.");	
			actionsDone = 0;
		} else {
			System.out.println("Action completed. You now have "+ (MAX_ACTIONS - actionsDone) +" actions left.");
		}
		blankLine();
	}
	
	
	/***********************************HELPER FUNCTIONS***************************************/
	// Prints a blank like to the console
	void blankLine() {
		System.out.println("");
	}
		
	// Searches for the names of other users
	int searchUserName(String name) {
		for (int userNumber = 0; userNumber<NUMBER_USERS;userNumber++) {
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

	int searchUserCards(int userIndex, String city) {
		User user = users[userIndex];
		for (int cards = 0; cards<user.cards.size();cards++) {
			if (cities[user.cards.get(cards).city].compareTo(city) == 0) {
				return cards;
			}
		}

		return -1;
	}
	// Draw infection cards and simulate outbreaks where necessary
	void drawCardsFromInfectionDeck(int numberToDraw) {
		System.out.println("Drawing from infection pile");
		citiesToIgnore = new ArrayList<Integer>();

		for (int number = 0; number < numberToDraw; number++) {
			Card card = infectionDeck.remove(infectionDeck.size() - 1);
			System.out.println(" - Card drawn: "+ cities[card.city]);
			if (diseaseCubes[card.city] == 3) {
				if (!citiesToIgnore.contains(card.city)) {
					System.out.println(" - - Outbreak at city");
					citiesToIgnore.add(card.city);
					outbreak(card.city);
				}
			} else {
				diseaseCubes[card.city]++;
				System.out.println(" - Cube added");
			}
			infectionDiscardPile.add(card);
		}

		System.out.println("Drawing complete");
	}

	// Infect all neighbouring cities
	void outbreak(int city) {
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

	//A stub for now just to put some disease cubes on the board.  Do it properly later.
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
		infectionDeck = shuffleCards(cityCards);
	}
	
	void createUsersAndGiveCards() {
		users = new User[NUMBER_USERS];
		for (int user = 0; user < NUMBER_USERS; user++) {
			ArrayList<Card> playerCards = new ArrayList<Card>();
			for (int cards = 0; cards < NUMBER_OF_CARDS_TO_GIVE; cards++) {
				Card card = playerDeck.remove(playerDeck.size() - 1);
				if (card.type == Card.EPIDEMIC) {
					System.out.println("Epidemic card drawn for "+ userNames[user]);
				} else {
					playerCards.add(card);
				}
			}
			users[user] = new User(userNames[user], User.userRoles[user], 0, playerCards);
		}
		
	}


	/************************************PRINT FUNCTIONS***************************************/
	
	void printNumberOfActionsLeft() {
		System.out.println("The current user is " + users[currentUser].name);
		System.out.println("They have  " + actionsDone +" action(s) left for their turn");
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
				System.out.println(cities[cityNumber] + " has " + diseaseCubes[cityNumber] + " cubes.");
			}
		}
		blankLine();
	}
	
	//Print out all the users' locations.
	void printUserLocations() {
		System.out.println("The current user is " + users[currentUser].name);
		for (int userNumber = 0; userNumber<NUMBER_USERS;userNumber++) {
			int printUserLocation = users[userNumber].location;
			
			System.out.println (users[userNumber].name + " is in " + cities[printUserLocation]);
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
		for (int userNumber = 0; userNumber<NUMBER_USERS;userNumber++) {
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

	//Print out the list of all the cities.
	void printCities() {
		System.out.println(numberCities + " Cities.");
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			System.out.println(cities[cityNumber]);
		}
		blankLine();
	}
	
}