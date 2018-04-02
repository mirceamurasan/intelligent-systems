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
    
    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
    }

    public int[][] getBoard() {
        return board;
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

        // from queen to top right
/*        int x = column;
        int y = row;
        do {
            board[y][x] = -1;
        } while (x++ < size-1 && y-- < size-1);*/

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
/*
        // Diagonal (bottom left -> right top)
        x = 0;
        y = size-1;
        do {
            board[y][x] = -1;
        } while (x++ < size && y-- >= 1);*/
    }

    private boolean isValidPlacement(int column, int row)
    {
        return board[column][row] == 0;
    }
}
