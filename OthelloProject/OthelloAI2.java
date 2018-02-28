import java.util.ArrayList;
public class OthelloAI2 implements IOthelloAI{
	// minimax solution
	public int[][] weights;

	public Position decideMove(GameState s){
		
		// Defining weights
		if (weights == null) {
			int size = s.getBoard().length;
			System.out.println(size);
			weights = new int[size][size];
			
			// Fill in weights		
			
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					
					// Edges
					if (x == 0 || x == size || y == 0 || y == size) {
						weights[x][y] = 3;
					} else {
						weights[x][y] = 2;
					}
					
					// Second Edge
					//if (x == 1 || x == size-1 || y == 1 || y == size-1) { 
					//	weights[x][y] = 1;
					//}
				}
			}
			
			// Corners
			weights[0][0] = 4;
			weights[0][size-1] = 4;
			weights[size-1][0] = 4;
			weights[size-1][size-1] = 4;
			
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					System.out.print(weights[x][y] + " ");
				}
				System.out.println();
			}
		}
		
		ArrayList<Position> moves = s.legalMoves();
		Position best=moves.get(0);
		int max=0;
		for(Position p : moves){
			GameState s1=new GameState(s.getBoard(),s.getPlayerInTurn());
			s1.insertToken(p);
			int utility=min(s1,0,s.getBoard().length*s.getBoard()[0].length);
					if(max<utility){
						max=utility;
						best=p;
					}
		}
		return best;
	}
	
	public int max(GameState s,int alpha,int beta){
		if (s.isFinished())
			return s.countTokens()[0];
		int v =0;
		ArrayList<Position> moves = s.legalMoves();
		for(Position p : moves){
			GameState s1=new GameState(s.getBoard(),s.getPlayerInTurn());
			s1.insertToken(p);
			v=Math.max(v,min(s1,alpha,beta));
			if(v>beta)
				return v;
			alpha=Math.max(v,alpha);
		}
		return v;
	}
	
	public int min(GameState s,int alpha,int beta){
		if (s.isFinished())
			return s.countTokens()[0];
		int v =s.getBoard().length*s.getBoard()[0].length;
		ArrayList<Position> moves = s.legalMoves();
		for(Position p : moves){
			GameState s1=new GameState(s.getBoard(),s.getPlayerInTurn());
			s1.insertToken(p);
			v=Math.min(v,max(s1,alpha,beta));
			if(v<=alpha)
				return v;
			beta=Math.min(v,beta);
		}
		return v;
	}
}