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
        fact = JFactory.init(20,20);
        int nVars = size*size;
        fact.setVarNum(nVars);
        System.out.println("The node table: ");
        fact.printAll();
    }

    private BDD getVariable(int col, int row){
        return fact.ithVar((col+2) + (row * size));
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {
        board[column][row] = 1;
    }
}
