
/*This is just a simple pandemic file.  It allows the user to type in a few commands.
  It reads in the cities from a file.
  The user can quit, print their current location, and print the actions they can perform,
  move, print all cities, print all connections, and print connections from the current location.
  You probably need to change the path for the cityMapFileName below.
  Chris Huyck wrote this.  I've not asked the pandemic folks to use their game, so don't 
  distribute it beyond CST 3170.
 */

//Note: you need to import scanner to use it to read from the screen.
import java.util.Scanner; // Import the Scanner class to read text files

//There's only one class in this program.  All functions are here, and it runs from
//the main function.
public class pandemic {
	//class variables on top
	private static Scanner shellInput;    //These two are for the shell scanner.
	private static boolean shellOpen = false;
	private static int[] diseaseCubes; //Number of disease cubes in the associated city.
	private static int[] userLocation = {0,0};  //These are the users' location that can change.
	private static int currentUser = 0;

	//The constants for the commands.
	private static final int QUIT = 0;
	private static final int PRINT_LOCATION = 1;
	private static final int MOVE = 2;
	private static final int PRINT_ACTIONS = 3;
	private static final int PRINT_CITIES = 4;
	private static final int PRINT_CONNECTIONS = 5;
	private static final int PRINT_ADJACENT_CITIES = 6;
	private static final int PRINT_DISEASES = 7;
	private static final int REMOVE = 8;
	public static final int PRINT_PLAYER_CARDS = 9;
	
	static GameState state;
	/***Functions for user commands***/
	//Get the users input and translate it to the constants.  Could do lots more 
	//error handling here.
	private static int processUserInput(String inputString) {
		if (inputString.compareTo("quit") == 0)
			return QUIT;
		else if (inputString.compareTo("location") == 0)
			return PRINT_LOCATION;
		else if (inputString.compareTo("cities") == 0)
			return PRINT_CITIES;
		else if (inputString.compareTo("connections") == 0)
			return PRINT_CONNECTIONS;
		else if (inputString.compareTo("adjacent") == 0)
			return PRINT_ADJACENT_CITIES;
		else if (inputString.compareTo("infections") == 0)
			return PRINT_DISEASES;
		else if (inputString.compareTo("move") == 0)
			return MOVE;
		else if (inputString.compareTo("remove") == 0)
			return REMOVE;
		else if (inputString.compareTo("player_cards") == 0)
			return PRINT_PLAYER_CARDS;
		else if ((inputString.compareTo("actions") == 0) ||
				 (inputString.compareTo("help") == 0))
			return PRINT_ACTIONS;
		else 
			return -1;
	}
	
	//Make sure the scanner is open, then get the user input and make sure it's reasonable.
	//Return the integer command.
	private static int getUserInput() {
		boolean gotReasonableInput = false;
		int processedUserInput = -1;

		//Open up the scanner if it's not already open.
		if (!shellOpen) {
			shellInput = new Scanner(System. in);
			shellOpen = true;
			//todo, add error checking.
		}
		//loop until the user types in a command that is named.  It may not be a valid move.
		while (!gotReasonableInput) {
			String userInput = shellInput.nextLine();
			System.out.println("The user typed:"+ userInput);
			//Translate the user's input to an integer.
			processedUserInput = processUserInput(userInput); 						
			if (processedUserInput >= 0)
				gotReasonableInput = true;
			else
				System.out.println(userInput + "is not a good command. Try 'actions'.");				
		}		
		return processedUserInput;
	}

	//print out the integer associated with what the user typed.
	private static void echoUserInput(int userInput) {
		System.out.println("The user chose:"+ userInput);
	}

	//Print out all possible user actions.
	private static void printActions() {
		System.out.println ("Type in on the terminal with the following followed by no spaces finish with return.");
		System.out.println ("quit");
		System.out.println ("location");
		System.out.println ("cities");
		System.out.println ("connections");
		System.out.println ("adjacent");
		System.out.println ("infections");
		System.out.println ("move");
		System.out.println ("remove");		
		System.out.println ("player_cards");		
		System.out.println ("actions");
	}

	//Handle the user's commands.
	private static boolean processUserCommand(int userInput) {
		echoUserInput(userInput);
		
		if (userInput == QUIT) 
			return true;
		else if (userInput == PRINT_LOCATION)
			state.printUserLocations();
		else if (userInput == PRINT_CITIES)
			state.printCities();
		else if (userInput == PRINT_CONNECTIONS)
			state.printConnections();
		else if (userInput == PRINT_ADJACENT_CITIES)
			state.printAdjacentCities();
		else if (userInput == PRINT_DISEASES)
			state.printInfectedCities();
		else if (userInput == PRINT_ACTIONS)
			printActions();
		else if (userInput == PRINT_PLAYER_CARDS)
			state.printPlayerCards();
		else if (userInput == MOVE) {
			state.moveUser();
			state.actionDone();
		}
		else if (userInput == REMOVE) {
			if (removeCube()) state.actionDone();
		}
		return false;
	}
	
	//Remove a cube from the current location.  If there's not, return false for an error.
	private static boolean removeCube() {
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

	//The main function of the program.  Enter and exit from here.
	//It is a simple getInput processInput loop until the game is over.  
	public static void main(String[] args) {
		boolean gameDone = false;
		state = new GameState();

		System.out.println("Hello Pandemic Tester");
		
		while (!gameDone) {
			int userInput = getUserInput();	
			gameDone = processUserCommand(userInput);
		}
		
		System.out.println("Goodbye Pandemic Tester");
	}
}