package com.sam.hex.ai.bee;

import java.math.*;
import java.util.*;

import android.graphics.Point;

import com.sam.hex.GameAction;
import com.sam.hex.GameObject;
import com.sam.hex.PlayerObject;
import com.sam.hex.PlayingEntity;

/** The "Bee" class.
  * Purpose: To play the game of Hex
  * @author Konstantin Lopyrev
  * @version June 2006
 */
public class BeeGameAI implements PlayingEntity
{
    private final int RED = 1, BLUE = 2, EMPTY = 0;
    private final int MAX_DEPTH = 2, BEAM_SIZE = 5;
    private int[] [] pieces;
    private HashMap lookUpTable;
    private int team;
    private LinkedList<AIHistoryObject> history = new LinkedList<AIHistoryObject>();//List of the AI's state. Used when Undo is called.
    private int gridSize;
    private String name;
	private int color;
	private long timeLeft;
	private GameObject game;
    private boolean skipMove = false;
    private EvaluationNode[] [] nodesArray;

    /** Constructor for the Bee object
    *@param game    the currently running game
    *@param board   the board that stores the currently running game
    *@param colour  the colour of Bee
    */
    public BeeGameAI (int team, GameObject game)
    {
	    this.team = team;
	    this.game = game;
		// Creates the pieces array that stores the board inside Bee
	    gridSize = game.gridSize;
		pieces = new int [gridSize + 2] [gridSize + 2];
		for (int i = 1 ; i < pieces.length - 1 ; i++)
		{
		    pieces [i] [0] = RED;
		    pieces [0] [i] = BLUE;
		    pieces [i] [pieces.length - 1] = RED;
		    pieces [pieces.length - 1] [i] = BLUE;
		}
		lookUpTable = new HashMap ();
    }
    
    public class AIHistoryObject{
    	int[][] pieces;
    	HashMap lookUpTable;
    	
    	public AIHistoryObject(int[][] pieces, HashMap lookUpTable) {
    		this.pieces = new int[pieces.length][pieces.length];
    		for(int i=0;i<pieces.length;i++){
    			for(int j=0;j<pieces.length;j++){
    				this.pieces[i][j] = pieces[i][j];
    			}
    		}
    		this.lookUpTable = lookUpTable;
    	}
    }


    /** Runs the Bee thread
      */
    @Override
    public void getPlayerTurn()
    {
    	skipMove = false;
    	AIHistoryObject state = new AIHistoryObject(pieces, lookUpTable);
		history.add(state);
		int moveNumber = game.moveNumber;
    	
	    Point lastMove;
		try{
			if(moveNumber>1) lastMove = new Point(gridSize-1-game.moveList.getmove().getY(), game.moveList.getmove().getX());
			else lastMove=null;
		}
		catch(Exception e){
			lastMove=null;
		}
		System.out.println("Last move: "+lastMove);
		// If Bee is to make the first move in the game,
		// it makes it in the centre of the board.
		if (lastMove == null)
		{
		    pieces [pieces.length / 2] [pieces.length / 2] = team;
		    if(!skipMove) GameAction.makeMove(this, (byte) team, new Point(pieces.length / 2 - 1, pieces.length / 2 - 1), game);
		}
		// If a move has been made already,
		// Bee records the move in the pieces array
		// and makes its own move.
		else
		{
		    pieces [lastMove.x + 1] [lastMove.y + 1] = team == 1 ? 2: 1;
		    Point bestMove = getBestMove ();
		    pieces [bestMove.x] [bestMove.y] = team;
		    int x = bestMove.x - 1;
		    int y = bestMove.y - 1;
		    
		    if(!skipMove) GameAction.makeMove(this, (byte) team, new Point(y, gridSize-1-x), game);
		}
	}
    
	@Override
	public void undoCalled() {
		if(history.size()>0){
			AIHistoryObject previousState = history.get(history.size()-1);
			pieces = previousState.pieces;
			lookUpTable = previousState.lookUpTable;
			history.remove(history.size()-1);
		}
		skipMove = true;
	}


	@Override
	public void newgameCalled() {
		skipMove = true;
	}
	
	@Override
	public boolean supportsUndo() {
		if(team==1){
			return game.player2 instanceof PlayerObject;
		}
		else{
			return game.player1 instanceof PlayerObject;
		}
	}

	@Override
	public boolean supportsNewgame() {
		return true;
	}


    /** Gets the best move on the board
    *@return    the point containing the move coordinates
     */
    private Point getBestMove ()
    {
	// Initially sets the best move to an invalid move with
	// the lowest possible move value
	int bestValue = team == RED ? Integer.MIN_VALUE:
	Integer.MAX_VALUE;
	int bestRow = -1;
	int bestColumn = -1;

	// Tries every single move possible and evaluates how good it is.
	for (int i = 1 ; i < pieces.length - 1 ; i++)
	{
	    for (int j = 1 ; j < pieces.length - 1 ; j++)
	    {
		if (pieces [i] [j] != 0)
		    continue;

		// Gets the evaluation for the move by expanding
		// the game tree.
		pieces [i] [j] = team;
		int value = expand (1, bestValue, team == RED ? BLUE:
		RED, nodesArray);
		pieces [i] [j] = 0;

		// Compares the last move to the best move so far
		// and records the move if it is better.
		if (team == RED && value > bestValue)
		{
		    bestValue = value;
		    bestRow = i;
		    bestColumn = j;
		}
		else if (team == BLUE && value < bestValue)
		{
		    bestValue = value;
		    bestRow = i;
		    bestColumn = j;
		}
	    }
	}
	return new Point (bestRow, bestColumn);
    }


    /** Evaluates the current branch of the game tree.
    *@param depth   the depth of the current branch
    *@param previousBest    the best move value of parallel branches
    *@param currentColour   the player colour to which the current branch corresponds to
    *@return    the value of the current branch
    */
    private int expand (int depth, int previousBest, int currentColour, EvaluationNode[] [] nodesArray)
    {
	// If depth is maximum depth, evaluates the branch using
	// a board evaluation instead of expanding it.
	if (depth == MAX_DEPTH)
	    return evaluate ();
	int bestValue = currentColour == RED ? Integer.MIN_VALUE:
	Integer.MAX_VALUE;

	// Gets all the moves possible to make.
	Iterator iter = getMoves ().iterator ();

	// Considers only the several best moves that are possible to make.
	for (int i = 0 ; i < BEAM_SIZE && iter.hasNext () ; i++)
	{
	    // Gets the move value of the next move.
	    Move nextMove = (Move) iter.next ();
	    pieces [nextMove.row] [nextMove.column] = currentColour;
	    int value = expand (depth + 1, bestValue,
		    currentColour == RED ? BLUE:
	    RED, nodesArray);
	    pieces [nextMove.row] [nextMove.column] = 0;

	    // Compares the last move to the best move so far
	    //and records the move if it is better.
	    if (currentColour == RED && value > bestValue)
		bestValue = value;
	    else if (currentColour == BLUE && value < bestValue)
		bestValue = value;

	    // If the current move makes the whole branch
	    // too worthless to be better than any of the parallel branches, stops expanding it.
	    if (currentColour == RED && bestValue > previousBest ||
		    currentColour == BLUE && bestValue < previousBest)
		return bestValue;
	}

	// If no moves are possible at this depth,
	// returns the evaluation of the board.
	if (bestValue == Integer.MAX_VALUE || bestValue == Integer.MIN_VALUE)
	    bestValue = evaluate ();
	return bestValue;
    }


    private ArrayList getMoves ()
    {
	// Builds the evaluation board for the current position
    nodesArray = new EvaluationNode [pieces.length] [pieces.length];
	EvaluationNode.buildEvaluationBoard (pieces, nodesArray);

	// Generates the four two-distance arrays.
	int[] [] redA = new int [pieces.length] [pieces.length];
	int[] [] redB = new int [pieces.length] [pieces.length];
	int[] [] blueA = new int [pieces.length] [pieces.length];
	int[] [] blueB = new int [pieces.length] [pieces.length];
	for (int i = 0 ; i < pieces.length ; i++)
	{
	    for (int j = 0 ; j < pieces.length ; j++)
	    {
		redA [i] [j] = 100000;
		redB [i] [j] = 100000;
		blueA [i] [j] = 100000;
		blueB [i] [j] = 100000;
	    }
	}
	// Sets the lower corners to 0 and builds
	// the two-distance array from there.
	redA [0] [0] = 0;
	redA [redA.length - 1] [0] = 0;
	redB [0] [redB.length - 1] = 0;
	redB [redB.length - 1] [redB.length - 1] = 0;
	blueA [0] [0] = 0;
	blueA [0] [blueA.length - 1] = 0;
	blueB [blueB.length - 1] [0] = 0;
	blueB [blueB.length - 1] [blueB.length - 1] = 0;

	// Builds the first RED two-distance array
	boolean found = true;
	while (found)
	{
	    found = false;
	    // Considers every position on the board
	    // and checks if it is possible to update it.
	    for (int j = 1 ; j < redA.length - 1 ; j++)
	    {
		for (int i = 1 ; i < redA.length - 1 ; i++)
		{
		    if (redA [i] [j] != 100000)
			continue;
		    if (pieces [i] [j] != 0)
			continue;

		    // Updates the position by considering all
		    // of its neighbours and assigning
		    // the two-distance value of 1 more
		    // than the second minimum value of its neighbours.
		    int min = 100000;
		    int secondMin = 100000;
		    Iterator iter = nodesArray [i] [j].redNeighbours.iterator ();

		    while (iter.hasNext ())
		    {

			EvaluationNode next = (EvaluationNode) iter.next ();
			int number = redA [next.row] [next.column];
			if (number < secondMin)
			{
			    secondMin = number;
			    if (number < min)
			    {
				secondMin = min;
				min = number;
			    }
			}
		    }
		    if (secondMin < 100)
		    {
			if (redA [i] [j] != secondMin + 1)
			{
			    found = true;
			    redA [i] [j] = secondMin + 1;
			}
		    }
		}
	    }
	}

	// Builds the second RED two-distance array
	found = true;
	while (found)
	{
	    found = false;
	    for (int j = redB.length - 2 ; j > 0 ; j--)
	    {
		for (int i = 1 ; i < redB.length - 1 ; i++)
		{
		    if (redB [i] [j] != 100000)
			continue;
		    if (pieces [i] [j] != 0)
			continue;
		    int min = 100000;
		    int secondMin = 100000;
		    Iterator iter = nodesArray [i] [j].redNeighbours.iterator ();

		    while (iter.hasNext ())
		    {
			EvaluationNode next = (EvaluationNode) iter.next ();
			int number = redB [next.row] [next.column];
			if (number < secondMin)
			{
			    secondMin = number;
			    if (number < min)
			    {
				secondMin = min;
				min = number;
			    }
			}
		    }
		    if (secondMin < 100)
		    {
			if (redB [i] [j] != secondMin + 1)
			{
			    found = true;
			    redB [i] [j] = secondMin + 1;
			}
		    }
		}
	    }
	}

	// Builds the first BLUE two-distance array
	found = true;
	while (found)
	{
	    found = false;
	    for (int i = 1 ; i < blueA.length - 1 ; i++)
	    {
		for (int j = 1 ; j < blueA.length - 1 ; j++)
		{
		    if (blueA [i] [j] != 100000)
			continue;
		    if (pieces [i] [j] != 0)
			continue;
		    int min = 100000;
		    int secondMin = 100000;
		    Iterator iter = nodesArray [i] [j].blueNeighbours.iterator ();

		    while (iter.hasNext ())
		    {
			EvaluationNode next = (EvaluationNode) iter.next ();
			int number = blueA [next.row] [next.column];
			if (number < secondMin)
			{
			    secondMin = number;
			    if (number < min)
			    {
				secondMin = min;
				min = number;
			    }
			}
		    }
		    if (secondMin < 100)
		    {
			if (blueA [i] [j] != secondMin + 1)
			{
			    found = true;
			    blueA [i] [j] = secondMin + 1;
			}
		    }
		}
	    }
	}
	// Builds the second BLUE two-distance array
	found = true;
	while (found)
	{
	    found = false;
	    for (int i = 1 ; i < blueB.length - 1 ; i++)
	    {
		for (int j = blueB.length - 2 ; j > 0 ; j--)
		{
		    if (blueB [i] [j] != 100000)
			continue;
		    if (pieces [i] [j] != 0)
			continue;
		    int min = 100000;
		    int secondMin = 100000;
		    Iterator iter = nodesArray [i] [j].blueNeighbours.iterator ();

		    while (iter.hasNext ())
		    {
			EvaluationNode next = (EvaluationNode) iter.next ();
			int number = blueB [next.row] [next.column];
			if (number < secondMin)
			{
			    secondMin = number;
			    if (number < min)
			    {
				secondMin = min;
				min = number;
			    }
			}
		    }
		    if (secondMin < 100)
		    {
			if (blueB [i] [j] != secondMin + 1)
			{
			    found = true;
			    blueB [i] [j] = secondMin + 1;
			}
		    }
		}
	    }
	}
	ArrayList moves = new ArrayList ();

	// Adds each move to the moves array
	// with the move value of the sum of its two-distances.
	for (int i = 1 ; i < pieces.length - 1 ; i++)
	{
	    for (int j = 1 ; j < pieces.length - 1 ; j++)
	    {
		if (pieces [i] [j] != 0)
		    continue;
		moves.add (new Move (i, j, redA [i] [j]
			    + redB [i] [j]
			    + blueA [i] [j]
			    + blueB [i] [j]));
	    }
	}
	// Sorts the moves in order from best to worst.
	Collections.sort (moves);
	return moves;
    }


    /** Evaluates the current board.
    *@return    the board value
    */
    private int evaluate ()
    {
	// Checks if the board has been
	// evaluated before and if it has, returns the previous value.
	BigInteger piecesString = piecesString ();
	Integer piecesValue = (Integer) lookUpTable.get (piecesString);
	if (piecesValue != null)
	    return piecesValue.intValue ();

	// Builds the evaluation board for the current position
	nodesArray = new EvaluationNode [pieces.length] [pieces.length];
	EvaluationNode.buildEvaluationBoard (pieces, nodesArray);

	// Builds the four two-distance arrays.
	int[] [] redA = new int [pieces.length] [pieces.length];
	int[] [] redB = new int [pieces.length] [pieces.length];
	int[] [] blueA = new int [pieces.length] [pieces.length];
	int[] [] blueB = new int [pieces.length] [pieces.length];
	for (int i = 0 ; i < pieces.length ; i++)
	{
	    for (int j = 0 ; j < pieces.length ; j++)
	    {
		redA [i] [j] = 100000;
		redB [i] [j] = 100000;
		blueA [i] [j] = 100000;
		blueB [i] [j] = 100000;
	    }
	}

	// Sets the four corners of the arrays to 0 and builds from there.
	redA [0] [0] = 0;
	redA [redA.length - 1] [0] = 0;
	redB [0] [redB.length - 1] = 0;
	redB [redB.length - 1] [redB.length - 1] = 0;
	blueA [0] [0] = 0;
	blueA [0] [blueA.length - 1] = 0;
	blueB [blueB.length - 1] [0] = 0;
	blueB [blueB.length - 1] [blueB.length - 1] = 0;

	// Builds the first RED two-distance array
	boolean found = true;
	while (found)
	{
	    found = false;
	    // Considers every position on the board
	    // and checks if it is possible to update it.
	    for (int j = 1 ; j < redA.length - 1 ; j++)
	    {
		for (int i = 1 ; i < redA.length - 1 ; i++)
		{
		    if (redA [i] [j] != 100000)
			continue;
		    if (pieces [i] [j] != 0)
			continue;

		    // Updates the position by considering
		    // all of its neighbours and assigning
		    // the two-distance value of 1 more
		    // than the second minimum value of its neighbours.
		    int min = 100000;
		    int secondMin = 100000;
		    Iterator iter = nodesArray [i] [j].redNeighbours.iterator ();

		    while (iter.hasNext ())
		    {

			EvaluationNode next = (EvaluationNode) iter.next ();
			int number = redA [next.row] [next.column];
			if (number < secondMin)
			{
			    secondMin = number;
			    if (number < min)
			    {
				secondMin = min;
				min = number;
			    }
			}
		    }
		    if (secondMin < 100)
		    {
			if (redA [i] [j] != secondMin + 1)
			{
			    found = true;
			    redA [i] [j] = secondMin + 1;
			}
		    }
		}
	    }
	}

	// Builds the second RED two-distance array
	found = true;
	while (found)
	{
	    found = false;
	    for (int j = redB.length - 2 ; j > 0 ; j--)
	    {
		for (int i = 1 ; i < redB.length - 1 ; i++)
		{
		    if (redB [i] [j] != 100000)
			continue;
		    if (pieces [i] [j] != 0)
			continue;
		    int min = 100000;
		    int secondMin = 100000;
		    Iterator iter = nodesArray [i] [j].redNeighbours.iterator ();

		    while (iter.hasNext ())
		    {
			EvaluationNode next = (EvaluationNode) iter.next ();
			int number = redB [next.row] [next.column];
			if (number < secondMin)
			{
			    secondMin = number;
			    if (number < min)
			    {
				secondMin = min;
				min = number;
			    }
			}
		    }
		    if (secondMin < 100)
		    {
			if (redB [i] [j] != secondMin + 1)
			{
			    found = true;
			    redB [i] [j] = secondMin + 1;
			}
		    }
		}
	    }
	}

	// Builds the first BLUE two-distance array
	found = true;
	while (found)
	{
	    found = false;
	    for (int i = 1 ; i < blueA.length - 1 ; i++)
	    {
		for (int j = 1 ; j < blueA.length - 1 ; j++)
		{
		    if (blueA [i] [j] != 100000)
			continue;
		    if (pieces [i] [j] != 0)
			continue;
		    int min = 100000;
		    int secondMin = 100000;
		    Iterator iter = nodesArray [i] [j].blueNeighbours.iterator ();

		    while (iter.hasNext ())
		    {
			EvaluationNode next = (EvaluationNode) iter.next ();
			int number = blueA [next.row] [next.column];
			if (number < secondMin)
			{
			    secondMin = number;
			    if (number < min)
			    {
				secondMin = min;
				min = number;
			    }
			}
		    }
		    if (secondMin < 100)
		    {
			if (blueA [i] [j] != secondMin + 1)
			{
			    found = true;
			    blueA [i] [j] = secondMin + 1;
			}
		    }
		}
	    }
	}

	// Builds the second BLUE two-distance array
	found = true;
	while (found)
	{
	    found = false;
	    for (int i = 1 ; i < blueB.length - 1 ; i++)
	    {
		for (int j = blueB.length - 2 ; j > 0 ; j--)
		{
		    if (blueB [i] [j] != 100000)
			continue;
		    if (pieces [i] [j] != 0)
			continue;
		    int min = 100000;
		    int secondMin = 100000;
		    Iterator iter = nodesArray [i] [j].blueNeighbours.iterator ();

		    while (iter.hasNext ())
		    {
			EvaluationNode next = (EvaluationNode) iter.next ();
			int number = blueB [next.row] [next.column];
			if (number < secondMin)
			{
			    secondMin = number;
			    if (number < min)
			    {
				secondMin = min;
				min = number;
			    }
			}
		    }
		    if (secondMin < 100)
		    {
			if (blueB [i] [j] != secondMin + 1)
			{
			    found = true;
			    blueB [i] [j] = secondMin + 1;
			}
		    }
		}
	    }
	}
	// Calculates the potentials and the mobility.
	// The potential of a board for a
	// particular colour is the smallest
	// two-distance value that occurs on
	// the sum of the boards corresponding to that colour.
	// The mobility of a board for a
	// particular colour is how many times
	// the smallest two-distance value occurs on
	// the sum of the boards corresponding to that colour.
	int redPotential = 100000;
	int bluePotential = 100000;
	int redMobility = 0;
	int blueMobility = 0;
	for (int i = 1 ; i < redA.length - 1 ; i++)
	{
	    for (int j = 1 ; j < redA.length - 1 ; j++)
	    {
		if (pieces [i] [j] == 0)
		{
		    if (redA [i] [j] + redB [i] [j] < redPotential)
		    {
			redPotential = redA [i] [j] + redB [i] [j];
			redMobility = 1;
		    }
		    else if (redA [i] [j] + redB [i] [j] == redPotential)
			redMobility++;
		    if (blueA [i] [j] + blueB [i] [j] < bluePotential)
		    {
			bluePotential = blueA [i] [j] + blueB [i] [j];
			blueMobility = 1;
		    }
		    else if (blueA [i] [j] + blueB [i] [j] == bluePotential)
			blueMobility++;
		}
	    }
	}

	// Stores the value of the current board in
	// the look-up table for future use.
	lookUpTable.put (piecesString, new Integer
		(100 * (bluePotential - redPotential) - (blueMobility - redMobility)));

	// Returns the value of the board.
	return
	    100 * (bluePotential - redPotential) - (blueMobility - redMobility);
    }


    /** Creates a BigInteger representation of the current board to use in the look-up table
    *@return    the BigInteger representation
    */
    private BigInteger piecesString ()
    {
	BigInteger value =
	    new BigInteger (Integer.toString ((pieces.length - 2)));
	for (int i = 1 ; i < pieces.length - 1 ; i++)
	{
	    for (int j = 1 ; j < pieces.length - 1 ; j++)
	    {
		value = value.multiply (new BigInteger ("3"));
		value = value.add (
			new BigInteger (Integer.toString (pieces [i] [j])));
	    }
	}
	return value;
    }

	@Override
	public void quit() {
		skipMove = true;
	}


	@Override
	public void win() {
	}


	@Override
	public void lose() {
	}


	@Override
	public boolean supportsSave() {
//		if(team==1){
//			return Global.player2 instanceof PlayerObject;
//		}
//		else{
//			return Global.player1 instanceof PlayerObject;
//		}
		return false;
	}


	@Override
	public void endMove() {
		skipMove = true;
	}


	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public void setTime(long time) {
		this.timeLeft = time;
	}

	@Override
	public long getTime() {
		return timeLeft;
	}


	@Override
	public void setMove(Object o, Point hex) {
	}

	@Override
	public boolean giveUp() {
		return false;
	}
}


/** The "Move" class.
  * Purpose: Stores a move
  * @author Konstantin Lopyrev
  * @version June 2006
 */
class Move implements Comparable
{
    public int row;
    public int column;
    private int value;
    /** Constructor for the Move class
    *@param row     the row of the move
    *@param column  the column of the move
    *@param value   the value of the move
    */
    public Move (int row, int column, int value)
    {
	this.row = row;
	this.column = column;
	this.value = value;
    }


    /** Compares this move to another object by value
    *@param other   the object to compare to
    *@return    0 if equals, -ve if less than, +ve if greater than
    */
    public int compareTo (Object other)
    {
	return this.value - ((Move) other).value;
    }
}
/** The "EvaluationNode" class.
  * Purpose: Stores the neighbours of each piece on the Hex board
  * @author Konstantin Lopyrev
  * @version June 2006
 */
class EvaluationNode
{
    public HashSet redNeighbours;
    public HashSet blueNeighbours;
    public int row;
    public int column;

    /** Constructor for the EvaluationNode class.
    *@param row     the row of the piece
    *@param column  the column of the piece
    */
    public EvaluationNode (int row, int column)
    {
	this.row = row;
	this.column = column;
	redNeighbours = new HashSet ();
	blueNeighbours = new HashSet ();
    }


    /** Creates the evaluation board for the corresponding pieces board
    *@param pieces  the corresponding pieces board
    */
    public static void buildEvaluationBoard (int[] [] pieces, EvaluationNode[] [] nodesArray)
    {
	// Initially creates all the EvaluationNodes without their neighbours
	for (int i = 0 ; i < nodesArray.length ; i++)
	    for (int j = 0 ; j < nodesArray.length ; j++)
		nodesArray [i] [j] = new EvaluationNode (i, j);

	// Builds the neighbours of each EvaluationNode
	for (int i = 0 ; i < nodesArray.length ; i++)
	    for (int j = 0 ; j < nodesArray.length ; j++)
	    {
		if (pieces [i] [j] != 0)
		    continue;
		nodesArray [i] [j].redNeighbours = nodesArray [i] [j].getNeighbours (1, new HashSet (), nodesArray, pieces);
		nodesArray [i] [j].redNeighbours.remove (nodesArray [i] [j]);
		nodesArray [i] [j].blueNeighbours = nodesArray [i] [j].getNeighbours (2, new HashSet (), nodesArray, pieces);
		nodesArray [i] [j].blueNeighbours.remove (nodesArray [i] [j]);
	    }
    }


    /** Recursive method which returns the neighbours of a piece
    *@param colour          the current colour
    *@param piecesVisited   stores the pieces that have been visited already so that they are not touched again
    *@return    the neighbours of the piece in a HashSet
    */
    private HashSet getNeighbours (int colour, HashSet piecesVisited, EvaluationNode[] [] nodesArray, int[] [] pieces)
    {
	// If the current piece has been visited already,
	// returns an empty HashSet
	if (piecesVisited.contains (this))
	    return new HashSet ();
	HashSet returnValue = new HashSet ();
	if (pieces [row] [column] == colour)
	    piecesVisited.add (this);

	// Considers all the neighbours of the current piece.
	for (int a = -1 ; a <= 1 ; a++)
	{
	    for (int b = -1 ; b <= 1 ; b++)
	    {
		if (a + b == 0)
		    continue;
		if (row + a < 0 || row + a == nodesArray.length ||
			column + b < 0 || column + b == nodesArray.length)
		    continue;

		// If the current neighbour is empty,
		// adds it to the neighbours list.
		if (pieces [row + a] [column + b] == 0)
		    returnValue.add (nodesArray [row + a] [column + b]);

		// If the current neighbour is a piece of
		// the opposing colour, ignores it.
		else if (pieces [row + a] [column + b] != colour)
		    continue;

		// If the current neighbour is a piece of
		// the same colour,
		// adds all of its neighbours to the neighbours list.
		else
		    returnValue.addAll (nodesArray [row + a] [column + b].getNeighbours (colour, piecesVisited, nodesArray, pieces));
	    }
	}
	return returnValue;
    }


    /** Returns a hashCode for the current EvaluationNode
    *@return    the hash code
    */
    public int hashCode ()
    {
	return row * 100 + column;
    }


    /** Compares this EvaluationNode to another object
    *@param other   the object to compare to
    *@return    true if equal, false otherwise
    */
    public boolean equals (Object other)
    {
	EvaluationNode otherNode = (EvaluationNode) other;
	return row == otherNode.row && column == otherNode.column;
    }
}