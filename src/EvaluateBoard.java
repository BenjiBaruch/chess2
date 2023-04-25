import java.util.Arrays;

public class EvaluateBoard {

    // Piece Square Tables:
    /*
    - These are the piece-square tables that the evaluation function will use to adjust the value
    of pieces based on where they are on the board.
    - This can encourage the algorithm to try to put its pieces in a desirable position even if it can't see
    far enough into the future to see why these positions are beneficial.
    - Some inspiration from https://www.chessprogramming.org/Simplified_Evaluation_Function
    - Because the king should stay on the first rank in the midgame, but move towards the center in the endgame,
    the king has two piece-square tables
    - https://www.tutorialspoint.com/difference-between-constants-and-final-variables-in-java
    */
    // King Mid: Prioritize castling, stay on first rank, avoid edges if moving up
    static int[] pst_kingMid = {5, 10, 5, 0, -5, 10, 5, 5,
                     0, 0, -5, -5, -5, -5, 0, 0,
                     -5, -10, -10, -10, -10, -10, -10, -5,
                     -20, -18, -15, -12, -12, -15, -18, -20,
                     -30, -27, -24, -20, -20, -24, -27, -30,
                     -35, -32, -29, -25, -25, -29, -32, -35,
                     -35, -32, -29, -25, -25, -29, -32, -35,
                     -35, -32, -29, -25, -25, -29, -32, -35};
    // King End: Stay in center, avoid edges
    static int[] pst_kingEnd = {-10, -10, -10, -10, -10, -10, -10, -10,
                     -10, -7, -7, -5, -5, -7, -7, -10,
                     -10, -7, -5, 0, 0, -5, -7, -10,
                     -10, -5, 0, 5, 5, 0, -5, -10,
                     -10, -5, 0, 5, 5, 0, -5, -10,
                     -10, -7, -5, 0, 0, -5, -7, -10,
                     -10, -7, -7, -5, -5, -7, -7, -10,
                     -10, -10, -10, -10, -10, -10, -10, -10};
    // Queen: Prioritize center, get away from first two ranks
    static int[] pst_queen = {-15, -10, -5, -5, 0, -5, -10, -15,
                   -10, -5, -5, -5, -5, -5, -5, -5,
                   -5, 0, 5, 5, 5, 5, 0, -5,
                   0, 0, 5, 5, 5, 5, 0, 0,
                   0, 0, 5, 5, 5, 5, 0, 0,
                   -5, 0, 5, 5, 5, 5, 0, -5,
                   0, 0, 0, 0, 0, 0, 0, 0,
                   -5, 0, 0, 0, 0, 0, 0, -5};
    // Rook: Avoid a and h files, prioritize 7 rank
    // The values on this table were from https://www.chessprogramming.org/Simplified_Evaluation_Function
    static int[] pst_rook = {0, 0, 0, 5, 5, 0, 0, 0,
                  -5, 0, 0, 0, 0, 0, 0, -5,
                  -5, 0, 0, 0, 0, 0, 0, -5,
                  -5, 0, 0, 0, 0, 0, 0, -5,
                  -5, 0, 0, 0, 0, 0, 0, -5,
                  -5, 0, 0, 0, 0, 0, 0, -5,
                  5, 10, 10, 10, 10, 10, 10, 5,
                  0, 0, 0, 0, 0, 0, 0, 0};
    // Bishop: Avoid edges, really avoid corners, prioritize strong diagonals in the center
    // The values on this table were from https://www.chessprogramming.org/Simplified_Evaluation_Function
    static int[] pst_bishop = {-20, -10, -10, -10, -10, -10, -10, -20,
                    -10, 5, 0, 0, 0, 0, 5, -10,
                    -10, 10, 10, 10, 10, 10, 10, -10,
                    -10, 0, 10, 10, 10, 10, 0, -10,
                    -10, 5, 5, 10, 10, 5, 5, -10,
                    -10, 0, 5, 10, 10, 5, 0, -10,
                    -10, 0, 0, 0, 0, 0, 0, -10,
                    -20, -10, -10, -10, -10, -10, -10, -20};
    // Knight: Heavily penalizes corners and edges b/c knights move slowly. Prioritizes center.
    // Initial positions have a heavy penalty and ideal first move has a boost to encourage early activation
    static int[] pst_knight = {-50, -30, -20, -20, -20, -20, -30, -50,
                    -30, -10, 0, 5, 5, 0, -10, -30,
                    -20, 3, 6, 12, 12, 6, 3, -20,
                    -10, 0, 9, 15, 15, 9, 0, -10,
                    -10, 3, 9, 15, 15, 9, 3, -10,
                    -10, 0, 6, 12, 12, 6, 0, -10,
                    -10, 0, 5, 5, 5, 5, 0, -10,
                    -20, -10, -10, -10, -10, -10, -10, -20};

    // Add boosts to pawns on a2, b2, c2, f2, g2, h2 because those defend a castled king.
    // Penalize keeping pawns on d2, e2 and boost d4, e4 to encourage taking the center.
    // Penalize moving a pawn to 3rd rank except on a, h files because those can block a bishop.
    // Give boosts to pawns closer to promotion.
    // Values on 1st and 8th rank don't matter because there can't be a pawn on those ranks.
    static int[] pst_pawn = {-999, -999, -999, -999, -999, -999, -999, -999,
                  3, 5, 5, -10, -10, 5, 5, 3,
                  3, -3, -5, 0, 0, -5, -3, 5,
                  0, 0, 0, 10, 10, 0, 0, 0,
                  3, 3, 6, 13, 13, 6, 3, 3,
                  6, 6, 12, 16, 16, 12, 6, 6,
                  30, 30, 30, 30, 30, 30, 30, 30,
                  999, 999, 999, 999, 999, 999, 999, 999};

    public static int getPhase(int[] board) {
        return 0;
    }

    public static int mobilityRay(int[] board, int pos, int offset, int emptyBoost, int captureBoost, int defendBoost) {
        int boost = 0;
        boolean pov = pos < 9;
        if (pos + offset > -1 && pos + offset < 64 && Math.abs((pos%8)-((pos+offset)%8)) < 3)
            pos += offset;
        else return 0;
        while (board[pos] == -1) {
            boost += emptyBoost;
            if (pos + offset > -1 && pos + offset < 64 && Math.abs((pos%8)-((pos+offset)%8)) < 3)
                pos += offset;
            else break;
        }
        if (board[pos] > -1) {
            if ((board[pos] < 9) == pov) boost += defendBoost;
            else boost += captureBoost;
        }
        return boost;
    }

    public static int mobilityPoint(int[] board, int pos, int dest) {
        if (dest > -1 & dest < 64 && Math.abs((pos%8)-(dest%8)) < 4) {
            if (board[dest] == -1) return 0;
            if ((board[dest] < 9) == (board[pos] < 9)) return 2;
            else return 3;
        } else return -3;
    }

    public static int evaluate(int[] board, boolean evalPov, boolean mobilitySearch) {
        /*
        https://www.chessprogramming.org/Tapered_Eval
        P = 100
        N = 320
        B = 330
        R = 500
        Q = 900
        K = 20000
         */
        int evaluation = 0;
        //int[] evalTable = new int[64];
        int phase = getPhase(board);
        for (int pos = 0; pos < 64; pos++) if (board[pos] > -1) {
            int score;
            int piece = board[pos] % 9;
            boolean pov = board[pos] < 9;
            int pst_pos = pov ? pos : ((7-pos/8)*8+(pos%8));

            if (piece < 2) {
                score = 50000 + (pst_kingMid[pst_pos] * (256 - phase) + pst_kingEnd[pst_pos] * phase) / 256;
            }
            else if (piece == 2) {
                score = 900 + pst_queen[pst_pos];
                if (mobilitySearch) {
                    score += mobilityRay(board, pos, -9, 1, 3, 0);
                    score += mobilityRay(board, pos, -8, 1, 3, 0);
                    score += mobilityRay(board, pos, -7, 1, 3, 0);
                    score += mobilityRay(board, pos, -1, 1, 3, 0);
                    score += mobilityRay(board, pos, 1, 1, 3, 0);
                    score += mobilityRay(board, pos, 7, 1, 3, 0);
                    score += mobilityRay(board, pos, 8, 1, 3, 0);
                    score += mobilityRay(board, pos, 9, 1, 3, 0);
                }
            }
            else if (piece == 3 || piece == 4) {
                score = 500 + pst_rook[pst_pos];
                if (mobilitySearch) {
                    score += mobilityRay(board, pos, -8, 1, 4, 1);
                    score += mobilityRay(board, pos, -1, 1, 4, 1);
                    score += mobilityRay(board, pos, 1, 1, 4, 1);
                    score += mobilityRay(board, pos, 8, 1, 4, 1);
                }
            }
            else if (piece == 5) {
                score = 315 + pst_bishop[pst_pos];
                if (mobilitySearch) {
                    score += mobilityRay(board, pos, -9, 2, 4, 2);
                    score += mobilityRay(board, pos, -7, 2, 4, 2);
                    score += mobilityRay(board, pos, 7, 2, 4, 2);
                    score += mobilityRay(board, pos, 9, 2, 4, 2);
                }
            }
            else if (piece == 6) {
                score = 320 + pst_knight[pst_pos];
                if (mobilitySearch) {
                    score += mobilityPoint(board, pos, pos - 17);
                    score += mobilityPoint(board, pos, pos - 15);
                    score += mobilityPoint(board, pos, pos - 10);
                    score += mobilityPoint(board, pos, pos - 6);
                    score += mobilityPoint(board, pos, pos + 17);
                    score += mobilityPoint(board, pos, pos + 15);
                    score += mobilityPoint(board, pos, pos + 10);
                    score += mobilityPoint(board, pos, pos + 6);
                }
                if ((pos/8)%7 != 0 && (pos%8)%7 != 0 &&
                        (pov ? (board[pos-9]==7 || board[pos-9]==8 || board[pos-7]==7 || board[pos-7]==8) :
                                (board[pos+9]==16 || board[pos+9]==17 || board[pos+7]==16 || board[pos+7]==17)))
                    score += 20;
            }
            else {
                score = 85 + pst_pawn[pst_pos];
                if (pov) {
                    if (pos%8 > 0 && board[pos+7] > -1) score += board[pos+7]<7 ? 15 : 5;
                    if (pos%8 < 7 && board[pos+9] > -1) score += board[pos+9]<7 ? 15 : 5;
                    if (board[pos+8] == 7) score -= 20;
                } else {
                    if (pos%8 > 0 && board[pos-9] > -1) score += board[pos-9]>8 && board[pos-9]<16 ? 15 : 5;
                    if (pos%8 < 7 && board[pos-7] > -1) score += board[pos-7]>8 && board[pos-7]<16 ? 15 : 5;
                    if (board[pos-8] == 16) score -= 20;
                }
            }
            //evalTable[pos] = score;
            if (pov == evalPov) evaluation += score;
            else evaluation -= score;
        }
        return evaluation;
    }
}
