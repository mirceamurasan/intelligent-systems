import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;

/**
 * This class uses BDDs to infer solutions to the N-Queen problem, and lets the user pick the solution.
 * It is made as a mandatory group assignment for Intelligent Systems Programming course at the IT University of
 * Copenhagen.
 * @author darb@itu.dk, eipo@itu.dk, mirm@itu.dk, mube@itu.dk, pvcl@itu.dk.
 */

public class Group31_QueensLogic implements IQueensLogic{
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed).
	private BDD rules; 		// A BDD representing all the rules of the game.
	private BDDFactory factory; //A factory that makes it easy to interact with the BDD.
	private int queensPlaced;	//Number of queens placed, used for auto finnish when there are no choices left.

	/**
	 * Initializes the board and the BDD.
	 * @param size The size of the board ( i.e. size = #rows = #columns)
	 */
	public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
		queensPlaced = 0;

		factory = JFactory.init(2000000, 200000);
        factory.setVarNum(size * size);

        rules = factory.one();

        generateBdd();
    }

	/**
	 * Returns the board in it's current state.
	 */
	public int[][] getBoard() {
        return board;
    }

	/**
	 * Generates the rules for the NQueens game and updates the BDD.
	 * We have the rule that we need one Queen per row (to finnish the game)
	 * Also we need
	 */
	public void generateBdd(){
		generateOneQueenPerColumnRule();
		
		for(int col=0;col<size;col++){
			for(int row=0;row<size;row++){
				generateRowRule(col,row);
				generateColumnRule(col,row);
				generateDiagonalRuleOne(col,row);
				generateDiagonalRuleTwo(col,row);
			}
		}
	}

	/**
	 * Adds one rule per column, saying that at least one queen should be placed in each column.
	 * In other words: At least one variable in each column should be true.
	 */
	public void generateOneQueenPerColumnRule(){
		for(int col = 0 ; col < size; col++){
			//Create one BDD for each column:
			BDD temp = factory.zero();
			for(int row = 0; row < size; row++){
				int varNum = getVariable(col,row);
				//For each column, one variable needs to be true, is the same as or'ing all variables in same
				//column together.
				temp.orWith(factory.ithVar(varNum));
			}
			//Add the rule for this column to the BDD of global rules.
			rules.andWith(temp);
		}
	}

	/**
	 * Adds rule saying that if a queen is placed on position (column,row) then we can't place a queen on any other
	 * column position in the same row.
	 */
	public void generateRowRule(int column, int row){
		BDD temp = factory.one(); // we use one because we will use the and operator (one being neutral with and)
		int current = getVariable(column,row);
		BDD currentCell = factory.ithVar(current);
		for(int col = 0; col < size; col++){
			if(col != column){
				//And'ing the negation of all other variables in the same row together.
				temp.andWith(factory.nithVar(getVariable(col, row)));
			}
		}
		//Saying if position (column,row) is true, then all variables in temp has to be false.
		BDD newRule = currentCell.imp(temp);
		rules.andWith(newRule);
	}

	/**
	 * Adds rule saying that if a queen is placed on position (column,row) then we can't place a queen on any other
	 * row position in the same column.
	 */
	public void generateColumnRule(int column, int row){
		BDD temp = factory.one(); // we use one because we will use the and operator (one being neutral with and)
		int current = getVariable(column,row);
		BDD currentCell = factory.ithVar(current);
		for(int r = 0; r < size; r++){
			if(r != row){
				//And'ing the negation of all other variables in the same column together.
				temp.andWith(factory.nithVar(getVariable(column, r)));
			}
		}
		//Saying if position (column,row) is true, then all variables in temp has to be false.
		BDD newRule = currentCell.imp(temp);
		rules.andWith(newRule);
	}

	/**
	 * Adds a rule for position (column,row), saying that we can't place queens in the same diagonal
	 * (top left to bottom right) diagonal as this position.
	 */
	public void generateDiagonalRuleOne(int column, int row){
		BDD temp = factory.one();
		int current = getVariable(column,row);
		BDD currentCell = factory.ithVar(current);

		int y = column - size;
		int x = row - size;
		while (x < size && y < size) {
			if (x >= 0 && y >= 0 && x < size && y < size) {
				if(x != row && y != column){
					//And'ing the negation of all other variables in the same diagonal together.
					temp.andWith(factory.nithVar(getVariable(y,x)));
				}
			}
			y++;
			x++;
		}
		//Saying if position (column,row) is true, then all variables in temp has to be false.
		BDD newRule = currentCell.imp(temp);
		rules.andWith(newRule);
	}

	/**
	 * Adds a rule for position (column, row), saying that we can't place queens in the same diagonal
	 * (top right to bottom left) diagonal as this position.
	 */
	public void generateDiagonalRuleTwo(int column, int row){
		BDD temp = factory.one();
		int current = getVariable(column,row);
		BDD currentCell = factory.ithVar(current);

		int x = row - size;
		int y = column + size;
		while (x < size && y >= 0) {
			if (x >= 0 && y < size) {
				if(x != row && y != column){
					//And'ing the negation of all other variables in the same diagonal together.
					temp.andWith(factory.nithVar(getVariable(y,x)));
				}
			}
			x++;
			y--;
		}
		//Saying if position (column,row) is true, then all variables in temp has to be false.
		BDD newRule = currentCell.imp(temp);
		rules.andWith(newRule);
	}

	/**
	 * Returns the variable number a given column and row.
	 */
	private int getVariable(int column, int row) {
        return row * this.size + column;
    }

	/**
	 * Inserts a queen in position (column,row), updates the BDD and the board based on the BDD.
	 * It also checks if it can autocomplete, and does so if it can.
	 */
	public void insertQueen(int column, int row)
    {
        if (isValidPlacement(column, row)) {
			//Assigns true to the variable on position (column,row):
			rules.restrictWith(factory.ithVar(getVariable(column, row)));
			board[column][row] = 1;

			for (int col = 0; col < size; col++) {
				for (int rov = 0; rov < size; rov++) {
					//Checks if the BDD evaluates to false if placing a queen on (col,rov):
					if (rules.restrict(factory.ithVar(getVariable(col,rov))).isZero()) {
						board[col][rov] = -1;
					}
				}
			}
			queensPlaced++;
			if(canAutoComplete()){
				autoComplete();
			}
        }
    }

	/**
	 * Returns true if number of queens placed + number of queens is equal to the N.
	 * In other words: If the user has no more choices to place queens.
	 */
	private boolean canAutoComplete(){
		int numMissingQueens = 0;
		for(int col = 0; col < size; col++){
			for(int row = 0; row < size; row++){
				if(getBoard()[col][row] == 0){
					numMissingQueens++;
				}
			}
		}
		if(numMissingQueens + queensPlaced == size){
			return true;
		}else
			return false;
	}

	/**
	 * Checks where there are missing queens, updates the BDD (to show crosses) and the board to show queens.
	 */
	private void autoComplete(){
		for(int col = 0; col < size; col++){
			for(int row = 0; row < size; row++){
				if(getBoard()[col][row] == 0){
					//Assigns true to the variable on position (col,row):
					rules.restrictWith(factory.ithVar(getVariable(col, row)));
					board[col][row] = 1;
				}
			}
		}
	}

	/**
	 * Returns true if I can place a queen on a given position (column,row).
	 */
    private boolean isValidPlacement(int column, int row)
    {
        return board[column][row] == 0;
    }
}