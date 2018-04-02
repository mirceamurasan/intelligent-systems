import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;

public class PeterQueensLogic implements IQueensLogic {
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)
    private BDDFactory fact;

    @Override
    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];

        //Init factory
        fact = JFactory.init(2_000_000,200_000);

        //Create n x n vars:
        fact.setVarNum(size*size);

        System.out.println("The node table: ");
        fact.printAll();
    }

    private BDD getVariable(int col, int row){
        return fact.ithVar((col) + (row * size));
    }

    private void assignToFalse(int col, int row){

    }

    @Override
    public int[][] getBoard() {
        for(int column = 0; column < size; column++ ){
            for(int row = 0; row < size; row++){
                if(getVariable(column, row).isOne()) board[column][row] = 0;
            }
        }
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {
        //Mark board:
        board[column][row] = 1;
        //Change BDD:

    }
}
