import java.util.ArrayList;
public class OthelloAI2 implements IOthelloAI{
	// minimax solution
	public int[][] weights = null;

	public Position decideMove(GameState s){

		// Defining weights
		definingWeights(s);
        int searchDepth = 10;

		ArrayList<Position> moves = s.legalMoves();
		Position best=moves.get(0);
		int max=0;
		for(Position p : moves){
			GameState s1=new GameState(s.getBoard(),s.getPlayerInTurn());
			s1.insertToken(p);
			int utility=min(s1,0,s.getBoard().length*s.getBoard()[0].length, searchDepth - 1);
					if(max<utility){
						max=utility;
						best=p;
					}
		}
		return best;
	}

	public int max(GameState s,int alpha,int beta, int searchDepth){
		if (s.isFinished())
			return s.countTokens()[0];
		if(searchDepth==0)
			return cutOffEval(s);
		int v =0;
		ArrayList<Position> moves = s.legalMoves();
		for(Position p : moves){
			GameState s1=new GameState(s.getBoard(),s.getPlayerInTurn());
			s1.insertToken(p);
			v=Math.max(v,min(s1,alpha,beta, searchDepth - 1));
			if(v>beta)
				return v;
			alpha=Math.max(v,alpha);
		}
		return v;
	}

	public int min(GameState s,int alpha,int beta, int searchDepth){
		if (s.isFinished())
			return s.countTokens()[0];
		if(searchDepth==0)
			return cutOffEval(s);
		int v =s.getBoard().length*s.getBoard()[0].length;
		ArrayList<Position> moves = s.legalMoves();
		for(Position p : moves){
			GameState s1=new GameState(s.getBoard(),s.getPlayerInTurn());
			s1.insertToken(p);
			v=Math.min(v,max(s1,alpha,beta, searchDepth - 1));
			if(v<=alpha)
				return v;
			beta=Math.min(v,beta);
		}
		return v;
	}
	
	public int cutOffEval(GameState s){
		int value=0;
		for(int i=0;i<s.getBoard().length;i++){
			for(int j=0;j<s.getBoard().length;j++){
				if(s.getBoard()[i][j]==1)
					value+=weights[i][j];
				else if (s.getBoard()[i][j]==2)
					value-=weights[i][j];
			}
		}
		return value;
	}

	private void definingWeights(GameState s) {
		if (weights == null) {
			int size = s.getBoard().length;
			System.out.println(size);
			weights = new int[size][size];

			// Fill in weights
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					weights[x][y] = 2;
				}
			}

			// Edges
			for (int x = 0; x < size; x++) {
				weights[x][0] = 3;
				weights[x][size-1] = 3;
			}

			for (int y = 0; y < size; y++) {
				weights[0][y] = 3;
				weights[size-1][y] = 3;
			}

			// Second Edge if size 6 or larger
			if (size >= 6) {
				for (int x = 1; x < size-1; x++) {
					weights[x][1] = 1;
					weights[x][size-2] = 1;
				}

				for (int y = 1; y < size-1; y++) {
					weights[1][y] = 1;
					weights[size-2][y] = 1;
				}
			}

			// Corners
			weights[0][0] = 4;
			weights[0][size-1] = 4;
			weights[size-1][0] = 4;
			weights[size-1][size-1] = 4;
		}

        System.out.println("Weight Layout");
        for (int x = 0; x < weights.length; x++) {
            for (int y = 0; y < weights.length; y++) {
                System.out.print(weights[x][y] + " ");
            }
            System.out.println();
        }
	}
}