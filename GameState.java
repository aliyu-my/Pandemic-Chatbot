
//Note: you need to import scanner to use it to read from the screen.
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Random;

public class GameState {
	//class variables on top
	private Scanner shellInput = new Scanner(System.in);
	//Note use a seed (1) for debugging.  
	private Random randomGenerator = new Random(1);
	private int numberCities = -1;	
	private int numberConnections = -1;	
	private String[] cities; //Cities
	private int[] diseaseCubes; //Number of disease cubes in the associated city.
	private int[][] connections; //The connections via offset in the cities array.
	private int[] userLocation = {0,0};  //These are the users' location that can change.
	private int currentUser = 0;
	private Card[] playerCards;	//List of all Player cards
	private int[] infectionCards;	 //List of all Infection cards
	private Card[] playerDeck;
	private int[] infectionDeck;

	//##Change this to your path.##
	public static final String cityMapFileName= "fullMap.txt";
	public static final int NUMBER_USERS = 2;
	public static final String[] userNames = {"Al","Bob"};
	static final String[] diseaseColors = {"Blue", "Yellow", "Red", "Black"};


	//The constants for the commands.
	public static final int QUIT = 0;
	public static final int PRINT_LOCATION = 1;
	public static final int MOVE = 2;
	public static final int PRINT_ACTIONS = 3;
	public static final int PRINT_CITIES = 4;
	public static final int PRINT_CONNECTIONS = 5;
	public static final int PRINT_ADJACENT_CITIES = 6;
	public static final int PRINT_DISEASES = 7;
	public static final int REMOVE = 8;
	
	GameState () {
		readCityGraph();
		addPlayerAndInfectionCards();
		setPlayerAndInfectionDeck();
		infectCities();
	}

	static int[] shuffleIntArray(int[] array) {
		int index;
		Random random = new Random();
		for (int i = array.length - 1; i > 0; i--)
		{
			index = random.nextInt(i + 1);
			if (index != i)
			{
					array[index] ^= array[i];
					array[i] ^= array[index];
					array[index] ^= array[i];
			}
		}
		return array;
	}

	static Card[] shuffleCards(Card[] array) {
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
					// array[index] ^= array[i];
					// array[i] ^= array[index];
					// array[index] ^= array[i];
			}
		}
		return array;
	}
	//Print out the cities adjacent to the userLocation
	void printAdjacentCities () {
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			if (citiesAdjacent(userLocation[currentUser],cityNumber)) {
				System.out.println(cities[cityNumber]);
			}
		}
	}
	
	//Print out all the users' locations.
	void printUserLocations() {
		System.out.println("The current user is " + userNames[currentUser]);
		for (int userNumber = 0; userNumber<NUMBER_USERS;userNumber++) {
			int printUserLocation = userLocation[userNumber];
			
			System.out.println (userNames[userNumber] + " is in " + cities[printUserLocation]);
		}
	}
	
	/*** Action Functions ***/
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
			else if (citiesAdjacent(userLocation[currentUser],cityToMoveTo)) {
				System.out.println("The user has moved from " +
					cities[userLocation[currentUser]] + " to " + 
					cities[cityToMoveTo] + ".");
				userLocation[currentUser] = cityToMoveTo;
				moved = true;
			}
			else {
				System.out.println ("You can't move to " + userInput + ".  Try one of these.");
				printAdjacentCities();
				}
		}
		
	}
	
	//Remove a cube from the current location.  If there's not, return false for an error.
	boolean removeCube() {
		int currentUserLocation = userLocation[currentUser];
		if (diseaseCubes[currentUserLocation] > 0) 
			{
			diseaseCubes[currentUserLocation]--;
			System.out.println("There are " + diseaseCubes[currentUserLocation] + " left");
			return true;
			}
		else {
			System.out.println("The space you're on has no disease cubes.");
			return false;
		}
	}
	
	void actionDone() {
		currentUser++;
		currentUser%=NUMBER_USERS;
		System.out.println("It's now " + userNames[currentUser] + " turn.");	
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
		
	//Print out the list of all the cities.
	void printCities() {
		System.out.println(numberCities + " Cities.");
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			System.out.println(cities[cityNumber]);
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

	void addPlayerAndInfectionCards () {
		playerCards = new Card[numberCities];
		infectionCards = new int[numberCities];
		int colorCount = 0;
		for (int cityNumber = 0; cityNumber < numberCities; cityNumber++) {
			playerCards[cityNumber] = new Card(cityNumber, diseaseColors[colorCount]);
			infectionCards[cityNumber] = colorCount;
			colorCount++;
			if (colorCount >= diseaseColors.length) {
				colorCount = 0;
			}
		}
	}

	// For now, we set it directly after some shuffling
	void setPlayerAndInfectionDeck () {
		playerDeck = shuffleCards(playerCards);
		infectionDeck = shuffleIntArray(infectionCards);
	}
	
	//Print out the full list of connections.
	void printConnections( ) {
		System.out.println(numberConnections + " Connections.");
		for (int connectionNumber = 0; connectionNumber < numberConnections; connectionNumber++) {
			String firstCity = cities[connections[0][connectionNumber]];
			String secondCity = cities[connections[1][connectionNumber]];
			System.out.println(firstCity + " " + secondCity);
		}
	}
			
	//Print out the full list of connections.
	void printPlayerCards( ) {
		for (int card = 0; card < playerCards.length; card ++) {
			System.out.println(cities[playerCards[card].city] +": "+ playerCards[card].color);
		}
	}

	//Open the city file, allocate the space for the cities, and connections, then read the 
	//cities, and then read the connections.  It uses those class variables.
	void readCityGraph() {

		//Open the file and read it.  
		try {
		      File fileHandle = new File(cityMapFileName);
		      Scanner mapFileReader = new Scanner(fileHandle);

		      //read the number of cities and allocate variables.
		      numberCities = mapFileReader.nextInt();
		      String data = mapFileReader.nextLine();  //read the rest of the line after the int
		      cities = new String[numberCities]; //allocate the cities array
		      diseaseCubes = new int[numberCities];
		      
		      //tead the number of connections and allocate variables.
		      numberConnections = mapFileReader.nextInt();
		      data = mapFileReader.nextLine();  //read the rest of the line after the int
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
	
	//A stub for now just to put some disease cubes on the board.  Do it properly later.
	void infectCities() {
		for (int city = 0; city < numberCities; city ++) {
			int randNumber = randomGenerator.nextInt(20);
			if (randNumber  < 4)
			diseaseCubes[city] = randNumber;
		}
	}

	void printInfectedCities() {
		for (int cityNumber = 0;  cityNumber < numberCities; cityNumber ++) {
			if (diseaseCubes[cityNumber] > 0) {
				System.out.println(cities[cityNumber] + " has " + diseaseCubes[cityNumber] + " cubes.");
			}
		}
	}
	
}