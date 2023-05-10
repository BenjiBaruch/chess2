public class Search {
    /*
    todo:
    * Check danger when castling
    * Add danger to mobility?
    * Quiescent search
    * Add special promotions to ChessBoard moves
     */
    static final int depth = 4;
    public static String intBoardSearch(int[] board) {
        return getBestMove(board);
    }
    public static String ChessPieceBoardSearch(ChessPiece[][] board, int turn, int halfmove, boolean povIsWhite) {
        return getBestMove(SimplifiedChessBoard.chessPieceToIntBoard(board, turn, !povIsWhite, halfmove, povIsWhite));
    }
    public static String ChessBoardSearch(ChessBoard board) {
        return ChessPieceBoardSearch(board.getBoard(), board.getTurns(), board.getHalfMove(), board.isPovIsWhite());
    }
    public static String stringSearch (String board) {
        return getBestMove(SimplifiedChessBoard.stringToIntBoard(board, true, false));
    }
    private static String getBestMove(int[] globalBoard) {
        System.out.println(SimplifiedChessBoard.toFormattedString(globalBoard, true, false));
        int[] moveList = SearchThread.reverseSort(SimplifiedChessBoard.getMoveList(globalBoard, globalBoard[64] == 1));
        SearchThread[] threads = new SearchThread[moveList.length];
        boolean evalWhite = globalBoard[64] == 1;
        for (int i = 0; i < moveList.length; i++) {
            int move = moveList[i];
            int weight = move / 4096;
            int pos1 = (move / 64) % 64;
            int pos2 = move % 64;
            int[] board = globalBoard.clone();

            if (weight == 62) board[pos2] = evalWhite ? 2 : 11; // Pawn promote to queen
            else if (weight == 60) board[pos2] = evalWhite ? 6 : 15; // Pawn promote to knight
            else board[pos2] = board[pos1] + (weight == 32 ? 1 : 0);
            board[pos1] = -1;
            if (weight == 32) { // Castling
                board[pos2 + (pos2>pos1 ? -1:1)] = board[pos1/8*8 + (pos2>pos1 ? 7:0)]+1;
                board[pos1/8*8 + (pos2>pos1 ? 7:0)] = -1;
            }
            if (weight == 10)// En Passant
                board[pos2 + (pos2>pos1 ? -8:8)] = -1;
            else if (board[67] > -1 && board[board[67]]%9 == 8) { // Unmark old pawn en passantable
                board[board[67]] -= 1;
            }
            if (weight == 4 && Math.abs(pos1-pos2) == 16) { // Pawn push 2
                board[pos2] += 1;
                board[67] = pos2; // Save en passant position
            }
            else board[67] = -1; // Clear en passant position

            threads[i] = new SearchThread(board, depth, moveList, !evalWhite);
            threads[i].start();
        }
        int bestMoveEval = Integer.MIN_VALUE;
        int bestMove = -1;
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            }
            catch (InterruptedException e) {}
            if (-threads[i].getEval() > bestMoveEval) {
                bestMoveEval = -threads[i].getEval();
                bestMove = moveList[i];
            }
        }
        return SimplifiedChessBoard.moveToString(globalBoard, bestMove);
    }
    public static void main(String[] args) {
        int[] board = SimplifiedChessBoard.stringToIntBoard("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 0", true, false);
        System.out.println(intBoardSearch(board));
    }
}
