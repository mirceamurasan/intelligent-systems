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
	private int queensPlaced;	

	/**
	 *
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

    public int[][] getBoard() {
        return board;
    }
	
	public void generateBdd(){
		generateOneQueenPerRowRule();
		
		for(int col=0;col<size;col++){
			for(int row=0;row<size;row++){
				generateRowRule(col,row);
				generateColumnRule(col,row);
				generateDiagonalRuleOne(col,row);
				generateDiagonalRuleTwo(col,row);
			}
		}
	}
	
	public void generateRowRule(int column, int row){
		BDD temp = factory.one(); // we use one because we will use the and operator (one being neutral with and)
		int current = getVariable(column,row);
		BDD currentCell = factory.ithVar(current);
		for(int col = 0; col < size; col++){
			if(col != column){
				temp.andWith(factory.nithVar(getVariable(col, row)));
			}
		}
		BDD newRule = currentCell.imp(temp);
		rules.andWith(newRule);
	}

	// [Column, Row] [i, j]
	public void generateColumnRule(int column, int row){
		BDD temp = factory.one(); // we use one because we will use the and operator (one being neutral with and)
		int current = getVariable(column,row);
		BDD currentCell = factory.ithVar(current);
		for(int r = 0; r < size; r++){
			if(r != row){
				temp.andWith(factory.nithVar(getVariable(column, r)));
			}
		}
		BDD newRule = currentCell.imp(temp); //
		rules.andWith(newRule);
	}
	
	public void generateDiagonalRuleOne(int column, int row){
		BDD temp = factory.one();
		int current = getVariable(column,row);
		BDD currentCell = factory.ithVar(current);

		int y = column - size;
		int x = row - size;
		while (x < size && y < size) {
			if (x >= 0 && y >= 0 && x < size && y < size) {
				if(x != row && y != column){
					temp.andWith(factory.nithVar(getVariable(y,x)));
				}
			}
			y++;
			x++;
		}

		BDD newRule = currentCell.imp(temp); //
		rules.andWith(newRule);
	}
	
	public void generateDiagonalRuleTwo(int i, int j){
		BDD temp=factory.one();
		int current=getVariable(i,j);
		BDD currentCell=factory.ithVar(current);

		int x = j - size;
		int y = i + size;
		while (x < size && y >= 0) {
			if (x >= 0 && y >= 0 && x < size && y < size) {
				if(x!=j && y!=i){
					temp.andWith(factory.nithVar(getVariable(y,x)));
				}
			}

			x++;
			y--;
		}
		BDD newRule = currentCell.imp(temp); //
		rules.andWith(newRule);
	}
	
	public void generateOneQueenPerRowRule(){
		for(int i=0;i<size;i++){
			BDD temp=factory.zero(); // one bdd for each row
			for(int j=0;j<size;j++){ // it can only be one of the cells on each row
				int var=getVariable(i,j);
				temp.orWith(factory.ithVar(var));// that's why we use or
			}
			rules.andWith(temp); // add the rule for this row to the global rules
			
		}
	}
	
	 private int getVariable(int column, int row) {
        return row * this.size + column;
    }

    public void insertQueen(int column, int row)
    {
    	// Need to add constraint to the BDD (Column, Row)
		// Need to update Board with getVariable for all positions
        if (isValidPlacement(column, row)) {
			board[column][row] = 1;
			rules.restrictWith(factory.ithVar(getVariable(column, row)));

			//factory.ithVar(getVariable(column, row)).restrictWith(factory.one());

			for (int col = 0; col < size; col++) {
				for (int rov = 0; rov < size; rov++) {
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

	private void autoComplete(){
		for(int col = 0; col < size; col++){
			for(int row = 0; row < size; row++){
				if(getBoard()[col][row] == 0){
					rules.restrictWith(factory.ithVar(getVariable(col, row)));
					board[col][row] = 1;
				}
			}
		}
	}

    private void markIllegalMoves(int column, int row)
    {
        // Horizontal
        for (int i = 0; i < size; i++) {
            board[column][i] = -1;
        }

        // Vertical
        for (int i = 0; i < size; i++) {
            board[i][row] = -1;
        }

        // Diagonal (left top -> bottom right)
        int x = row - size;
        int y = column - size;
        while (x < size && y < size) {
            if (x >= 0 && y >= 0 && x < size && y < size) {
                board[y][x] = -1;
            }

            x++;
            y++;
        }

        x = row - size;
        y = column + size;
        while (x < size && y >= 0) {
            if (x >= 0 && y >= 0 && x < size && y < size) {
                board[y][x] = -1;
            }

            x++;
            y--;
        }
    }

    private boolean isValidPlacement(int column, int row)
    {
        return board[column][row] == 0;
    }
}
