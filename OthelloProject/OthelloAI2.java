import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.*;

public class OthelloAI2 implements IOthelloAI {
    // minimax solution

    // we have to share the hashMap, otherwise if it is created inside the methods
    // it just creates a new empty one for new each move
    private static ConcurrentHashMap<GameState, Tuple> cachedComputations = new ConcurrentHashMap<>();

    private final int DEPTH = 8;
    public int[][] weights = null;

    public Position decideMove(GameState s) {
        //return decideMoveSequential(s); //Sequential (old implementation)
        return decideMoveInParallelStream(s); //Parallel (new implementation) - reaches 2x speedup against DumAI
        //return decideMoveInParallelFuture(s); //Parallel future implementaiton, same speed as stream.
    }

    public Position decideMoveSequential(GameState s) {
        // Defining weights
        definingWeights(s);
        int searchDepth = DEPTH;

        Timer timer = new Timer();
        ArrayList<Position> moves = s.legalMoves();
        System.out.println("Number of available moves: " + moves.size());

        Position best = moves.get(0);
        int max = 0;
        for (Position p : moves) {
            GameState s1 = new GameState(s.getBoard(), s.getPlayerInTurn());
            s1.insertToken(p);
            int utility = min(s1, 0, s.getBoard().length * s.getBoard()[0].length, searchDepth - 1);
            if (max < utility) {
                max = utility;
                best = p;
            }
        }
        System.out.println("Took: " + timer.check() + " seconds.");
        return best;
    }

    /**
     * Uses Java 8 parallel streams to speed up implementation.
     *
     * @param s
     * @return
     */
    public Position decideMoveInParallelStream(GameState s) {
        // Defining weights
        definingWeights(s);
        int searchDepth = DEPTH;
        //Setting up:
        Timer timer = new Timer();
        ArrayList<Position> moves = s.legalMoves();
        System.out.println("Number of available moves: " + moves.size());
//        ConcurrentHashMap<GameState, Tuple> cachedComputations = new ConcurrentHashMap<>();

        //Computing best move in parallel
        Optional<Tuple> bestMove = moves.stream().parallel().map(position -> {
            GameState s1 = new GameState(s.getBoard(), s.getPlayerInTurn());
            System.out.println("Size of cached HashMap: " + cachedComputations.size());

            if (cachedComputations.contains(s1)) {
                System.out.println("state was cached! ******************************************");

                // only consider reusing the cached info if it was from a deeper search
//                if ((searchDepth < cachedComputations.get(s1).oldDepth))

                // check for null because min and max return only the utility value and not the best move
                if (cachedComputations.get(s1) != null)
                    return cachedComputations.get(s1);
            }
            s1.insertToken(position);
            int utility = min(s1, 0, s.getBoard().length * s.getBoard()[0].length, searchDepth - 1);
            Tuple result = new Tuple(position, utility, searchDepth);
//            cachedComputations.putIfAbsent(s1, result);
            return result;
        }).max(Comparator.comparingInt(t -> t.utilityValue));
        //Print time:
        System.out.println("Took: " + timer.check() + " seconds.");
        // cache
        Tuple result = new Tuple(bestMove.get().move, bestMove.get().utilityValue, searchDepth);
        cachedComputations.putIfAbsent(s, result);
        //Return result:
        return bestMove.get().move; //Will never return null, since this isn't run if s.legalMoves() returns 0.
    }

    /**
     * Class used for parallel computation. Stores information about a given Game State so it can
     * be cached and reused instead of being recomputed
     */
    public class Tuple {
        public final Position move;
        public final int utilityValue;
        // The depth of the search this information is from
        // TODO: this is probably not needed because on each new
        // move we start counting from 8 (or 9,10..) so we lose the info
        // of how deep we are in the search.
        public final int oldDepth;

        public Tuple(Position move, int utilityValue, int oldDepth) {
            this.move = move;
            this.utilityValue = utilityValue;
            this.oldDepth = oldDepth;
        }
    }

    /**
     * Class used for timing in seconds.
     */
    public class Timer {
        private long start, spent = 0;

        public Timer() {
            play();
        }

        public double check() {
            return (System.nanoTime() - start + spent) / 1e9;
        }

        public void pause() {
            spent += System.nanoTime() - start;
        }

        public void play() {
            start = System.nanoTime();
        }
    }

    /**
     * Future based implementation. Has same performance as parallel stream based implementation.
     *
     * @param s
     * @return
     */
    public Position decideMoveInParallelFuture(GameState s) {
        // Defining weights
        definingWeights(s);
        int searchDepth = DEPTH;

        Timer timer = new Timer();
        ArrayList<Position> moves = s.legalMoves();
        System.out.println("Number of available moves: " + moves.size());

        //Submit futures:
        ArrayList<Future<Tuple>> utilityValueOfMoves = new ArrayList<>();
        ExecutorService es = Executors.newWorkStealingPool();
        for (Position p : moves) {
            Future<Tuple> fut = es.submit(() -> {
                GameState s1 = new GameState(s.getBoard(), s.getPlayerInTurn());
                s1.insertToken(p);
                int utility = min(s1, 0, s.getBoard().length * s.getBoard()[0].length, searchDepth - 1);
                return new Tuple(p, utility, searchDepth);
            });
            utilityValueOfMoves.add(fut);
        }

        //Find best move:
        int bestUtility = Integer.MIN_VALUE;
        Position bestMove = null;
        for (Future<Tuple> fut : utilityValueOfMoves) {
            try {
                int utilityValue = fut.get().utilityValue;
                if (utilityValue > bestUtility) {
                    bestUtility = utilityValue;
                    bestMove = fut.get().move;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Took: " + timer.check() + " seconds.");
        return bestMove;
    }

    public int max(GameState s, int alpha, int beta, int searchDepth) {
        if (s.isFinished())
            return s.countTokens()[0];
        if (searchDepth == 0)
            return cutOffEval(s);
        int v = 0;

        ArrayList<Position> moves = s.legalMoves();
        for (Position p : moves) {
            GameState s1 = new GameState(s.getBoard(), s.getPlayerInTurn());
            if (cachedComputations.contains(s1)) {
                System.out.println("MAX: state was cached! ******************************************");

                // only consider reusing the utility if it was from a deeper search
//                if ((searchDepth < cachedComputations.get(s1).oldDepth))
                return cachedComputations.get(s1).utilityValue;
            }
            s1.insertToken(p);
            v = Math.max(v, min(s1, alpha, beta, searchDepth - 1));
            if (v > beta) {
                Tuple cached = new Tuple(null, v, searchDepth);
                cachedComputations.putIfAbsent(s, cached);
                return v;
            }
            alpha = Math.max(v, alpha);
        }
        Tuple cached = new Tuple(null, v, searchDepth);
        cachedComputations.putIfAbsent(s, cached);
        return v;
    }

    public int min(GameState s, int alpha, int beta, int searchDepth) {
        if (s.isFinished())
            return s.countTokens()[0];
        if (searchDepth == 0)
            return cutOffEval(s);
        int v = s.getBoard().length * s.getBoard()[0].length;
        ArrayList<Position> moves = s.legalMoves();
        for (Position p : moves) {
            GameState s1 = new GameState(s.getBoard(), s.getPlayerInTurn());
            if (cachedComputations.contains(s1)) {
                System.out.println("MIN: state was cached! ******************************************");

                // only consider reusing the utility if it was from a deeper search
//                if ((searchDepth < cachedComputations.get(s1).oldDepth))
                return cachedComputations.get(s1).utilityValue;
            }
            s1.insertToken(p);
            v = Math.min(v, max(s1, alpha, beta, searchDepth - 1));
            if (v <= alpha) {
                Tuple cached = new Tuple(null, v, searchDepth);
                cachedComputations.putIfAbsent(s, cached);
                return v;
            }
            beta = Math.min(v, beta);
        }
        Tuple cached = new Tuple(null, v, searchDepth);
        cachedComputations.putIfAbsent(s, cached);
        return v;
    }

    public int cutOffEval(GameState s) {
        int value = 0;
        for (int i = 0; i < s.getBoard().length; i++) {
            for (int j = 0; j < s.getBoard().length; j++) {
                if (s.getBoard()[i][j] == 1)
                    value += weights[i][j];
                else if (s.getBoard()[i][j] == 2)
                    value -= weights[i][j];
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
                weights[x][size - 1] = 3;
            }

            for (int y = 0; y < size; y++) {
                weights[0][y] = 3;
                weights[size - 1][y] = 3;
            }

            // Second Edge if size 6 or larger
            if (size >= 6) {
                for (int x = 1; x < size - 1; x++) {
                    weights[x][1] = 1;
                    weights[x][size - 2] = 1;
                }

                for (int y = 1; y < size - 1; y++) {
                    weights[1][y] = 1;
                    weights[size - 2][y] = 1;
                }
            }

            // Corners
            weights[0][0] = 4;
            weights[0][size - 1] = 4;
            weights[size - 1][0] = 4;
            weights[size - 1][size - 1] = 4;
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