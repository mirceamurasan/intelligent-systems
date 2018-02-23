import java.util.ArrayList;
import java.util.Random;

/**
 * A simple OthelloAI-implementation. The method to decide the next move just
 * returns the first legal move that it finds. 
 * @author Mai Ajspur
 * @version 9.2.2018
 */
public class DumAI implements IOthelloAI{

	/**
	 * Returns first legal move
	 */
	public Position decideMove(GameState s){
		ArrayList<Position> moves = s.legalMoves();
		Random r=new Random();
		if ( !moves.isEmpty() )
			return moves.get(r.nextInt(moves.size()));
		else
			return new Position(-1,-1);
	}
	
}
