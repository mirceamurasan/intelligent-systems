import java.util.ArrayList;
public class OthelloIA implements IOthelloAI{
	// just uses a greedy algorithm (makes the move that captures the most tokens)
	public Position decideMove(GameState s){
		ArrayList<Position> moves = s.legalMoves();
		Position best=moves.get(0);
		int max=0;
		for(Position p : moves){
			for (int deltaX = -1; deltaX <= 1; deltaX++){
				for (int deltaY = -1; deltaY <= 1; deltaY++){
					int captives=s.captureInDirection(p, deltaX, deltaY);
					if(max<captives){
						max=captives;
						best=p;
					}
				}
			}
		}
		return best;
	}
}