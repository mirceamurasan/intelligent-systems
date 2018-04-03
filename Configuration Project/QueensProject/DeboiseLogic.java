/**
 * This class implements a basic logic for the n-queens problem to get you started. 
 * Actually, when inserting a queen, it only puts the queen where requested
 * and does not keep track of which other positions are made illegal by this move.
 * 
 * @author Mai Ajspur
 * @version 16.02.2018
 */

public class DeboiseLogic implements IQueensLogic{
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
	private BDD rules; // a bdd representing all the rules of the game
	private BDDFactory factory;
    
    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
		
		factory = JFactory.init(2000000, 200000);
        factory.setVarNum(size * size);

        rules = factory.one();
     

        generateBdd();
    }

    public int[][] getBoard() {
        return board;
    }
	
	public void generateBdd(){
		//oneQueenPerRow();
		
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				queenOnRow(i,j);
				queenOnCol(i,j);
				queenOnDiagonalOne(i,j);
				queenOnDiagonalTwo(i,j);
			}
		}
	}
	
	public void queenOnRow(int i,int j){
		BDD temp=factory.one(); // we use one because we will use the and operator (one being neutral with and)
		int current=getVariable(i,j);
		BDD currentCell=factory.ithVar(current);
		for(int x=0;x<size;x++){
			if(x!=i){
				temp.andWith(factory.nithVar(getVariable(i,j)));
			}
		}
		BDD newRule = currentCell.andWith(temp); //
		rules.andWith(newRule);
	}
	
	public void queenOnCol(int i,int j){
		BDD temp=factory.one(); // we use one because we will use the and operator (one being neutral with and)
		int current=getVariable(i,j);
		BDD currentCell=factory.ithVar(current);
		for(int x=0;x<size;x++){
			if(x!=j){
				temp.andWith(factory.nithVar(getVariable(i,j)));
			}
		}
		BDD newRule = currentCell.andWith(temp); //
		rules.andWith(newRule);
	}
	
	public void queenOnDiagonalOne(int i,int j){
		Bdd temp=factory.one();
		int current=getVariable(i,j);
		BDD currentCell=factory.ithVar(current);
		for(int x=0;x<size;x++){
			for(int y=0;y<size;y++){
				if(x!=i && y!=j){
					temp.andWith(factory.nithVar(getVariable(i,j)));
				}
			}
		}
		BDD newRule = currentCell.andWith(temp); //
		rules.andWith(newRule);
	}
	
	public void queenOnDiagonalTwo(int i,int j){
		Bdd temp=factory.one();
		int current=getVariable(i,j);
		BDD currentCell=factory.ithVar(current);
		for(int x=size-1;x>0;x--){
			for(int y=size-1;y>0;y--){
				if(x!=i && y!=j){
					temp.andWith(factory.nithVar(getVariable(i,j)));
				}
			}
		}
		BDD newRule = currentCell.andWith(temp); //
		rules.andWith(newRule);
	}
	
	public void oneQueenPerRow(){
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
        return row * N + column;
    }

    public void insertQueen(int column, int row)
    {
        if (isValidPlacement(column, row)) {
            markIllegalMoves(column, row);
            board[column][row] = 1;
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
