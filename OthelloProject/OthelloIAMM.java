import java.util.ArrayList;
public class OthelloIAMM implements IOthelloAI{
	// minimax solution
	public Position decideMove(GameState s){
		
		ArrayList<Position> moves = s.legalMoves();
		Position best=moves.get(0);
		int max=0;
		for(Position p : moves){
			GameState s1=new GameState(s.getBoard(),s.getPlayerInTurn());
			s1.insertToken(p);
			int utility=min(s1);
					if(max<utility){
						max=utility;
						best=p;
					}
		}
		return best;
	}
	
	public int max(GameState s){
		if (s.isFinished())
			return s.countTokens()[0];
		int v =0;
		ArrayList<Position> moves = s.legalMoves();
		for(Position p : moves){
			GameState s1=new GameState(s.getBoard(),s.getPlayerInTurn());
			s1.insertToken(p);
			v=Math.max(v,min(s1));
		}
		return v;
	}
	
	public int min(GameState s){
		if (s.isFinished())
			return s.countTokens()[0];
		int v =s.getBoard().length*s.getBoard()[0].length;
		ArrayList<Position> moves = s.legalMoves();
		for(Position p : moves){
			GameState s1=new GameState(s.getBoard(),s.getPlayerInTurn());
			s1.insertToken(p);
			v=Math.min(v,max(s1));
		}
		return v;
	}
}