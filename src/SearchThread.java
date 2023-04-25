import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;

public class SearchThread extends Thread {
    // No code from chessprogramming.org was copied.
    // https://www.chessprogramming.org/Alpha-Beta
    // https://www.stmintz.com/ccc/index.php?id=136513
    // https://www.chessprogramming.org/Aspiration_Windows
    // https://www.chessprogramming.org/Quiescence_Search
    // https://www.chessprogramming.org/Delta_Pruning
    // https://www.chessprogramming.org/Perft_Results
    // https://www.youtube.com/watch?v=r_MbozD32eo
    static boolean perftTesting = false;
    static int iterations;
    static int successes;
    static int fails;
    static int captures;
    static int castles;
    static int totalDepth;
    private int[] inputBoard;
    private final int inputDepth;
    private final int[] inputDanger;
    private final boolean inputEvalSide;
    private int eval;
    //static Hashtable<Integer, Integer> detailedPerft = new Hashtable<>(20);
    static Hashtable<Integer, Integer> pieceMoves = new Hashtable<>(17);

    public SearchThread(int[] board, int depth, int[] danger, boolean evalWhite) {
        inputBoard = board;
        inputDepth = depth;
        inputDanger = danger;
        inputEvalSide = evalWhite;
    }


    @Override
    public void run() {
        eval = search(inputBoard, inputDanger, inputDepth, Integer.MIN_VALUE/2, Integer.MAX_VALUE/2, inputEvalSide);
    }

    public int getEval() {
        return eval;
    }


    public static int[] reverseSort(int[] array) {
        if (array.length < 2) return array;
        for (int i = 0; i < array.length-1; i++) {
            boolean sorted = true;
            for (int j = 0; j < array.length-1; j++) {
                if (array[j] < array[j+1]) {
                    int temp = array[j];
                    array[j] = array[j+1];
                    array[j+1] = temp;
                    sorted = false;
                }
            }
            if (sorted) break;
        }
        return array;
    }
    private static String moveType(int[] board, int move) {
        String[] pieces = {"White King (c)", "White King (!c)", "White Queen", "White Rook (c)", "White Rook (!c)",
                "White Bishop", "White Knight", "White Pawn (!ep)", "White Pawn (ep)", "Black King (c)", "Black King(!c)",
                "Black Queen", "Black Rook (c)", "Black Rook (!c)", "Black Bishop", "Black Knight", "Black Pawn (!ep)",
                "Black Pawn (ep)"};
        int weight = move / 4096;
        int pos1 = (move / 64) % 64;
        int pos2 = move % 64;
        String moveStr = " {" + pos1 + ", " + pos2 + "} ";
        String str;
        if (weight == 32) str = "Castle";
        else if (weight == 60) str = "Pawn to Knight Promotion";
        else if (weight == 62) str = "Pawn to Queen Promotion";
        else if (weight == 12) str = "En Passant Capture";
        else if (weight == 4 && Math.abs(pos1-pos2) == 16) str = pieces[board[pos1]]+moveStr+"Double Push";
        else if (weight % 2 == 0) str = pieces[board[pos1]]+moveStr+"Movement";
        else str = pieces[board[pos1]]+" {"+pos1+"} captures "+pieces[board[pos2]]+" {"+pos2+"}";
        return str;
    }
    public int search(int[] board, int[] danger, int depth, int alpha, int beta, boolean evalWhite) {
        int score;
        if (depth < 1) {
            if (perftTesting) iterations++;
            //int submoves = detailedPerft.containsKey(initialMove) ? detailedPerft.get(initialMove) + 1 : 1;
            //detailedPerft.put(initialMove, submoves);
            return EvaluateBoard.evaluate(board, evalWhite, false);
        } else {
            int[] moveList = reverseSort(SimplifiedChessBoard.getMoveList(board, evalWhite));
            //System.out.println(Arrays.toString(moveList));

            if (moveList.length == 0) { // No available moves
                int kingIndex = board[evalWhite ? 65:66];
                assert kingIndex > -1;
                for (int move : danger) if (move % 64 == kingIndex) return Integer.MIN_VALUE/2; // Checkmate
                return 0; // Stalemate
            }

            for (int move : moveList) {
                int weight = move / 4096;
                if (perftTesting && weight % 2 == 1) captures++;
                int pos1 = (move / 64) % 64;
                int pos2 = move % 64;
                int temp = board[pos2];
                int enPassantID = board[67];
                if (enPassantID > -1 && board[enPassantID]%9!=8) System.out.println("err ID");
                int[] boardCopy = board.clone();

                if (temp == (evalWhite ? 9 : 0) || temp == (evalWhite ? 10 : 1))
                    return Integer.MIN_VALUE/2;

                if (boardCopy[pos1] == -1) {
                    System.out.println("Inv move");
                    continue;
                }
                if (perftTesting && totalDepth == 1) {
                    pieceMoves.put(board[pos1], pieceMoves.containsKey(board[pos1]) ? pieceMoves.get(board[pos1]) + 1 : 1);
                }


                // Add move to board
                //moveSequence[msLength++] = moveType(board, move);
                if (weight == 62) board[pos2] = evalWhite ? 2 : 11; // Pawn promote to queen
                else if (weight == 60) board[pos2] = evalWhite ? 6 : 15; // Pawn promote to knight
                else board[pos2] = board[pos1] + (weight == 32 ? 1 : 0);
                board[pos1] = -1;
                if (weight == 32) { // Castling
                    board[pos2 + (pos2>pos1 ? -1:1)] = board[pos1/8*8 + (pos2>pos1 ? 7:0)]+1;
                    board[pos1/8*8 + (pos2>pos1 ? 7:0)] = -1;
                    if (perftTesting) castles++;
                }

                if (weight == 12)// En Passant
                    board[pos2 + (pos2>pos1 ? -8:8)] = -1;
                else if (enPassantID > -1 && board[enPassantID]%9 == 8) { // Unmark old pawn en passantable
                    board[enPassantID] -= 1;
                }
                if (weight == 4 && Math.abs(pos1-pos2) == 16) { // Pawn push 2
                    board[pos2] += 1;
                    board[67] = pos2; // Save en passant position
                }
                else board[67] = -1; // Clear en passant position

                // System.out.println(SimplifiedChessBoard.toString(board, true, true));

                // Evaluate this move
                score = -search(board, moveList, depth-1, -beta, -alpha, !evalWhite);

                // Get old board
                if (weight > 61) board[pos1] = evalWhite ? 7 : 16; // Undo pawn promotion
                else if (weight == 4 && Math.abs(pos1-pos2) == 16) board[pos1] = board[pos2] - 1; // Undo double pawn push
                else board[pos1] = board[pos2]; // Undo move
                board[pos2] = temp; // Replace captured piece
                if (weight == 32) { // Undo castle
                    board[pos2 + (pos2>pos1 ? -1:1)] = -1;
                    board[pos1/8*8+(pos2>pos1 ? 7:0)] = evalWhite ? 3 : 12;
                    board[pos1]--;
                }

                if (weight == 12) // Undo en passant
                    board[pos2 + (pos2>pos1 ? -8:8)] = evalWhite ? 17 : 8;
                if (enPassantID > -1 && board[enPassantID]%9 == 7) // Remark old en passant
                    board[enPassantID] += 1;
                board[67] = enPassantID;

                if(!Arrays.equals(board, boardCopy)) {
                    /*
                    System.out.print("frickity frackity " + "epid=" + enPassantID + " d=" + depth);
                    //for (int i = 0; i < msLength; i++)
                    //    System.out.print("\n-  " + moveSequence[i]);
                    System.out.print("\n-  Discontinuities: ");
                    for (int i = 0; i < 68; i++) if (board[i] != boardCopy[i])
                        System.out.print(i + (i%9==8?"!":"") + " (" + boardCopy[i] + ", " + board[i] + "), ");
                    System.out.print('\n');
                    */

                    System.out.print('e');

                    board = boardCopy;
                    if (perftTesting) fails++;
                } else {
                    if (perftTesting) successes++;
                }

                //if (depth == 2) System.out.println(pos1 + "," + pos2 + ": " + (iterations - tempI));

                // Set alpha/beta
                // Next two lines took heavy inspiration from https://www.chessprogramming.org/Alpha-Beta
                //if (eval >= beta) return beta;
                //moveSequence[--msLength] = null;
                if (score > alpha) alpha = score;
            }
            return alpha;
        }
    }
    /*
    public static void main(String[] args) {
        //https://www.chessprogramming.org/Perft_Results
        //https://www.javacodeexamples.com/print-hashtable-in-java-example/3154

        int[] board = SimplifiedChessBoard.stringToIntBoard("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 0", true);
        totalDepth = 1;
        System.out.println(search(board, new int[0],  totalDepth, Integer.MIN_VALUE/2, Integer.MAX_VALUE/2, true));
        System.out.println("i: " + iterations + ", s: " + successes + ", f: " + fails + ", x: " + captures + ", c: " + castles);
        System.out.println(pieceMoves);
        //for (int move : SimplifiedChessBoard.getMoveList(SimplifiedChessBoard.newBlankBoard(), true)) {
        //    System.out.println(detailedPerft.get(move) + " for " + moveType(board, move));
        //}
    }
    */

}
