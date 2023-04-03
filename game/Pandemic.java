package game;
/*This is just a simple pandemic file.  It allows the user to type in a few commands.
  It reads in the cities from a file.
  The user can quit, print their current location, and print the actions they can perform,
  move, print all cities, print all connections, and print connections from the current location.
  You probably need to change the path for the cityMapFileName below.
  Chris Huyck wrote this.  I've not asked the pandemic folks to use their game, so don't 
  distribute it beyond CST 3170.
 */

import java.util.Objects;
//Note: you need to import scanner to use it to read from the screen.
import java.util.Scanner; // Import the Scanner class to read text files

import action.AbstractAction;
import action.GameActions;
import ai.Moves;

//There's only one class in this program.  All functions are here, and it runs from
//the main function.
public class Pandemic {
	//class variables on top
	private static Scanner shellInput;    //These two are for the shell scanner.
	private static boolean shellOpen = false;

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
	public static final int PRINT_CITY_CARDS = 9;
	public static final int PRINT_MY_CARDS = 10;
	public static final int CURE_DISEASE = 11;
	public static final int DRAW_CARD = 12;
	public static final int TAKE_CARD = 13;
	public static final int GIVE_CARD = 14;
	public static final int PRINT_RESEARCH_STATIONS = 15;
	public static final int CREATE_STATION = 16;
	public static final int PRINT_CURED_DISEASES = 17;
	public static final int PRINT_ERADICATED_DISEASES = 18;
	public static final int PRINT_OUTBREAKS = 19;
	public static final int PASS_TURN = 20;
	
	static GameState state; // Will keep track of the Game's State
	static GameActions gameActions;
	
	/***Functions for user commands***/
	//Get the users input and translate it to the constants.  Could do lots more 
	//error handling here.
	private static int processUserInput(String inputString) {
		inputString = inputString.toLowerCase();
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
		else if (inputString.compareTo("cured_diseases") == 0)
			return PRINT_CURED_DISEASES;
		else if (inputString.compareTo("eradicated_diseases") == 0)
			return PRINT_ERADICATED_DISEASES;
		else if (inputString.compareTo("stations") == 0)
			return PRINT_RESEARCH_STATIONS;
		else if (inputString.compareTo("outbreaks") == 0)
			return PRINT_OUTBREAKS;
		else if (inputString.compareTo("city_cards") == 0)
			return PRINT_CITY_CARDS;
		else if (inputString.compareTo("my_cards") == 0)
			return PRINT_MY_CARDS;
		else if (inputString.compareTo("move") == 0)
			return MOVE;
		else if (inputString.compareTo("pass_turn") == 0)
			return PASS_TURN;
		else if (inputString.compareTo("remove") == 0)
			return REMOVE;
		else if (inputString.compareTo("cure") == 0)
			return CURE_DISEASE;
		else if (inputString.compareTo("draw_card") == 0)
			return DRAW_CARD;
		else if (inputString.compareTo("take_card") == 0)
			return TAKE_CARD;
		else if (inputString.compareTo("give_card") == 0)
			return GIVE_CARD;
		else if (inputString.compareTo("build_station") == 0)
			return CREATE_STATION;
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
				System.out.println(userInput + " is not a good command. Try 'actions'. \n");				
		}		
		return processedUserInput;
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
		System.out.println ("cured_diseases");
		System.out.println ("eradicated_diseases");
		System.out.println ("stations");
		System.out.println ("outbreaks");
		System.out.println ("city_cards");		
		System.out.println ("pass_turn");
		System.out.println ("my_cards");		
		System.out.println ("move");
		System.out.println ("remove");		
		System.out.println ("cure");		
		System.out.println ("draw_card");		
		System.out.println ("take_card");		
		System.out.println ("give_card");		
		System.out.println ("build_station");		
		System.out.println ("actions");
		System.out.println("");
	}

	//Handle the user's commands.
	private static boolean processUserCommand(int userInput) {
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
		else if (userInput == PRINT_CURED_DISEASES)
			state.printCuredDiseases();
		else if (userInput == PRINT_ERADICATED_DISEASES)
			state.printEradicatedDiseases();
		else if (userInput == PRINT_RESEARCH_STATIONS)
			state.printResearchStations();
		else if (userInput == PRINT_OUTBREAKS)
			state.printOutbreaks();
		else if (userInput == PRINT_ACTIONS) {
			printActions();
			state.printNumberOfActionsLeft();
		}
		else if (userInput == PRINT_CITY_CARDS)
			state.printCityCards();
		else if (userInput == PRINT_MY_CARDS)
			state.printUserCards();
		else if (userInput == MOVE) {
			gameActions.moveUser();
			gameActions.actionDone();
		}
		else if (userInput == DRAW_CARD) {
			gameActions.drawPlayerCard();
			gameActions.actionDone();
		}
		else if (userInput == REMOVE) {
			if (gameActions.removeCube()) gameActions.actionDone();
		}
		else if (userInput == PASS_TURN) {
			if (gameActions.passTurn()) gameActions.actionDone();
		}
		else if (userInput == TAKE_CARD) {
			if (gameActions.takeCard()) gameActions.actionDone();
		}
		else if (userInput == GIVE_CARD) {
			if (gameActions.giveCard()) gameActions.actionDone();
		}
		else if (userInput == CREATE_STATION) {
			if (gameActions.createStation()) gameActions.actionDone();
		}
		else if (userInput == CURE_DISEASE) {
			if (gameActions.cureDisease()) gameActions.actionDone();
		}
		return false;
	}

	public static boolean processAIMove() {
		boolean done = false;
		while (!done) {
			int randomMove = (int) (Math.random() * Moves.allMoves.length);
			String type = Moves.allMoves[randomMove];
			System.out.println(type);
			Moves moves = new Moves(state);
			AbstractAction action = moves.getMove(type);
			if (Objects.isNull(action)) {
			} else {
				if (action.canPerform()) {
					if (action.perform()) {
						gameActions.actionDone();
						done = true;
					}
				}
			}
	
		}
		
		return true;
	}

	//The main function of the program. Enter and exit from here.
	public static void main(String[] args) {
		boolean gameDone = false;

		System.out.println("Hello Pandemic Gamer");
		state = new GameState();
		state.initializeGame();
		gameActions = new GameActions(state);

		while (!gameDone) {
			if (state.users[state.currentUser].type.compareTo(User.AGENT) == 0) {
				processAIMove();
			} else {
				int userInput = getUserInput();	
				gameDone = processUserCommand(userInput);
			}

		}
		
		System.out.println("Goodbye Pandemic Gamer");
	}
}