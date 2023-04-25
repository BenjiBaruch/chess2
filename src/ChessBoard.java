import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class ChessBoard {

    private ChessPiece[][] board;
    final private boolean povIsWhite;
    private int turns;
    private int halfMoveClock;

    public ChessBoard(String boardType, boolean povIsWhite) {
        this();
        if (boardType.equals("default")) {
            for (int i = 0; i < 8; i++) {
                board[1][i] = new Pawn(true, 8 + i);
                board[6][i] = new Pawn(false, 48 + i);
            }
            board[0][0] = new Rook(true, 0);
            board[0][1] = new Knight(true, 1);
            board[0][2] = new Bishop(true, 2);
            board[0][3+(!povIsWhite?0:1)] = new Queen(true, 3+(!povIsWhite?0:1));
            board[0][3+(!povIsWhite?1:0)] = new King(true, 3+(!povIsWhite?1:0));
            board[0][5] = new Bishop(true, 5);
            board[0][6] = new Knight(true, 6);
            board[0][7] = new Rook(true, 7);
            board[7][0] = new Rook(false, 56);
            board[7][1] = new Knight(false, 57);
            board[7][2] = new Bishop(false, 58);
            board[7][3+(!povIsWhite?0:1)] = new Queen(false, 59+(!povIsWhite?0:1));
            board[7][3+(!povIsWhite?1:0)] = new King(false, 59+(!povIsWhite?1:0));
            board[7][5] = new Bishop(false, 61);
            board[7][6] = new Knight(false, 62);
            board[7][7] = new Rook(false, 63);
        }
        else if (boardType.equals("custom")) {
            System.out.println("Enter board code\n");
            Scanner scan = new Scanner(System.in);
            String str = scan.nextLine();
            System.out.println(str);
            int[] intBoard = SimplifiedChessBoard.flipBoard(SimplifiedChessBoard.stringToIntBoard(str, true));
            board = SimplifiedChessBoard.intToChessPieceBoard(intBoard, povIsWhite);
            System.out.println(Search.ChessBoardSearch(this));
            turns = intBoard[69];
        }
    }

    public ChessBoard() {
        povIsWhite = true;
        board = new ChessPiece[8][8];
        turns = 0;
    }

    public void setBoard(ChessPiece[][] board) {
        this.board = board;
    }

    public ChessPiece[][] getBoard() {
        return board;
    }

    public boolean moveValid(int p1, int p2) {
        /*
        Returns true if a move is valid.
        All valid moves fit the following criteria:
        - The piece exists (is not null)
        - If this piece is not a king, moving this piece to
          this tile will not open its king to check
        - If this piece is a king, it is not moving
          to a threatened tile
        - The desired position exists in the move map (a.k.a
          the list of tiles this piece can move to)
         */
        if (board[p1/8][p1%8] == null)
            return false;
        boolean povSelected = board[p1/8][p1%8].isPov();
        ChessPiece[][] nextBoard = new ChessPiece[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                nextBoard[y][x] = board[y][x];
            }
        }
        nextBoard[p2/8][p2%8] = nextBoard[p1/8][p1%8];
        nextBoard[p1/8][p1%8] = null;
        if(checksSelf(nextBoard, findPiece(nextBoard, 0, nextBoard[p2/8][p2%8].isPov()),turns+1, nextBoard[p2/8][p2%8].isPov())) {
            return false;
        }
        ChessPiece piece = board[p1/8][p1%8];
        int[] moves = piece.moveMap(board, turns);
        for (int move : moves) {
            if (move == p2) return true;
        }
        return false;
    }

    public boolean move(int p1, int p2) {
        /*
        Moves the piece on p1 to p2 if the move is valid.
        https://www.chessprogramming.org/Halfmove_Clock
         */
        if (!moveValid(p1, p2)) return false;
        if (board[p2/8][p2%8] != null || board[p1/8][p1%8].toInt() == 5)
            halfMoveClock = 0;
        else
            halfMoveClock++;
        board = board[p1/8][p1%8].move(board, p2, turns);
        turns++;
        return true;
    }

    public boolean moveWithAlgebra(String str) {
        /*
        Converts a string in algebraic notation into two integers, p1 and p2
        Calls the move function with p1 and p2
        Returns move success (false means the move failed or is illegal, true means the move was executed)
         */
        boolean isPov = (turns % 2 == 0) == povIsWhite;
        int p1 = -1; // Piece position
        int p2 = -1; // Destination
        int promotion = -1; // Piece to promote into (for pawns)

        System.out.println(str);

        // Castling
        if (str.charAt(str.length()-1) == '0' || str.charAt(str.length()-1) == 'O') {
            p1 = findPiece(board, 0, isPov == povIsWhite);
            if ((str.length() == 5) == povIsWhite) p2 = p1 - 2;
            else p2 = p1 + 2;
            return move(p1, p2); // If the move is a castle, enter that move, return success, and skip the rest of the method
        }

        // Pawn Promotion
        if ('A' <= str.charAt(str.length()-1) && str.charAt(str.length()-1) <= 'Z') {
            promotion = "KQRBNP".indexOf(str.charAt(str.length()-1));
            str = str.substring(0, str.length()-1);
        }

        // Abridge Suffixes and Xs
        while ("!?+#xX≠‡=:×† e.p".indexOf((str.charAt(str.length()-1))) > -1) str = str.substring(0, str.length()-1);
        int xI = str.indexOf('x');
        if(xI > -1) str = str.substring(0, xI).concat(str.substring(xI+1));


        // Getting p2
        int p2X = str.charAt(str.length()-2)-'a';
        int p2Y = str.charAt(str.length()-1)-'1';
        if (!povIsWhite) p2Y = 7 - p2Y;
        p2 = p2Y*8+p2X;
        if (!(0 <= p2 && p2 <= 63)) return false; // If p2 not in range, return false

        // Figure out specifier count
        // A specifier is a rank or file name, used to disambiguate which piece is being selected for p1
        int piece;
        if (str.charAt(0) < 'A' || str.charAt(0) > 'Z') piece = 5;
        else piece = "KQRBNP".indexOf(str.charAt(0));
        int specifiers = str.length() - 2 - (piece < 5 ? 1 : 0);

        // Getting p1 (zero specifiers)
        if (specifiers == 0) {
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (board[y][x] != null && board[y][x].toInt() == piece && board[y][x].isPov() == isPov) {
                        int[] moveMap = board[y][x].moveMap(board, turns);
                        for (int moveIndex : moveMap) {
                            if (moveIndex == p2) {
                                if (p1 == -1) p1 = y*8+x;
                                else return false;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Getting p1 (one specifier)
        else if (specifiers == 1) {
            int x = -1;
            int y = -1;
            int specIndex = (piece == 5 ? 0 : 1);
            if ('1' <= str.charAt(specIndex) && str.charAt(specIndex) <= '8') y = str.charAt(specIndex) - '1';
            if ('a' <= str.charAt(specIndex) && str.charAt(specIndex) <= 'h') x = str.charAt(specIndex) - 'a';
            if (x == y) return false;
            boolean rankSpecified = (x == -1);
            for (int i = 0; i < 8; i++) {
                if (rankSpecified) x = i;
                else y = i;
                if (board[y][x] != null && board[y][x].toInt() == piece && board[y][x].isPov() == isPov) {
                    int[] moveMap = board[y][x].moveMap(board, turns);
                    for (int moveIndex : moveMap) {
                        if (moveIndex == p2) {
                            if (p1 == -1) p1 = y*8+x;
                            else return false;
                            break;
                        }
                    }
                }
            }
        }

        // Two specifiers
        else {
            int x = str.charAt(1) - 'a';
            int y = str.charAt(2) - '1';
            if (board[y][x] != null && board[y][x].toInt() == piece && board[y][x].isPov() == isPov) {
                int[] moveMap = board[y][x].moveMap(board, turns);
                for (int moveIndex : moveMap) {
                    if (moveIndex == p2) {
                        p1 = y*8+x;
                        break;
                    }
                }
            } else System.out.println("what");
        }

        // Special cases:
        if (promotion > 2) {
            System.out.println("fancy promotion");
            return false;
        }
        if (p1 == -1) {
            System.out.println("piece not found");
            return false;
        }

        // Do move and return success bool
        return move(p1, p2);
    }

    public static int findPiece(ChessPiece[][] inpBoard, int type, boolean pov) {
        /*
        Returns the first index of the requested piece on the inputted board
         */
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (inpBoard[i][j] != null && inpBoard[i][j].toInt() == type && inpBoard[i][j].isPov() == pov) {
                    return i*8+j;
                }
            }
        }
        System.out.println("Search failed: " + type + ' ' + pov);
        return -1;
    }

    public int[] highlights(int piece) {
        /*
        Returns the index of all places a piece can move to
        Culls moves that open a check
        Used by ChessPanel to highlight tiles that can be moved to
         */
        if (isNull(piece/8, piece%8)) return new int[0];
        int[] highlights = board[piece/8][piece%8].moveMap(board, turns);
        for (int i = 0; i < highlights.length; i++) {
            if (highlights[i] > -1 && !moveValid(piece, highlights[i])) {
                highlights[i] = -1;
            }
        }
        return highlights;
    }

    public static boolean checksSelf(ChessPiece[][] nextBoard, int king, int turns, boolean pov) {
        /*
        Checks if a move puts the active player in check.
         */
        if (king == -1) {
            System.out.println("null king");
            return false;
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (nextBoard[i][j] != null && nextBoard[i][j].isPov() != pov) {
                    if (nextBoard[i][j].toInt() > 0) {
                        int[] moveMap = nextBoard[i][j].moveMap(nextBoard, turns);
                        for (int move : moveMap) {
                            if (move == king) {
                                // System.out.println("Checked at " + king);
                                return true;
                            }
                        }
                    } else {
                        if (Math.abs(king / 8 - i) < 2 && Math.abs(king % 8 - j) < 2)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public int checkWin() { // Help from https://www.digitalocean.com/community/tutorials/java-break-statement-label
        /*
        Only 0, 2, 5, 8, and 9 are added as of now.

        Win conditions (returned int):
        0: Game not over
        1: White win by resignation
        2: White win by checkmate
        3: White win by timeout
        4: Black win by resignation
        5: Black win by checkmate
        6: Black win by timeout
        7: Draw by agreement
        8: Draw by stalemate
        9: Draw by insufficient material
        10: Draw by timeout vs insufficient material
         */

        // Black win by checkmate (5) and stalemate (8)
        if (turns % 2 == 1) { // If its white's turn
            boolean whiteCanMove = false;
            whiteExitCodes:
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] != null && board[i][j].isWhite(povIsWhite)) {
                        int[] moves = board[i][j].moveMap(board, turns);
                        for (int move : moves) {
                            if (move > -1 && moveValid(i*8+j, move)) {
                                whiteCanMove = true;
                                break whiteExitCodes;
                            }
                        }
                    }
                }
            }
            if (!whiteCanMove) {
                if (checksSelf(board, findPiece(board, 0, povIsWhite), turns, povIsWhite)) {
                    return 5;
                } else {
                    return 8;
                }
            }
        }

        // White win by checkmate (2) and stalemate (8)
        else { // If its black's turn
            boolean blackCanMove = false;
            blackExitCodes:
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j] != null && !board[i][j].isWhite(povIsWhite)) {
                        int[] moves = board[i][j].moveMap(board, turns);
                        for (int move : moves) {
                            if (move > -1 && moveValid(i*8+j, move)) {
                                blackCanMove = true;
                                break blackExitCodes;
                            }
                        }
                    }
                }
            }
            if (!blackCanMove) {
                if (checksSelf(board, findPiece(board, 0, !povIsWhite), turns, !povIsWhite)) {
                    return 2;
                } else {
                    return 8;
                }
            }
        }

        // Draw by insufficient material (9)
        ArrayList<Integer> pieces = new ArrayList<>(5);
        GetPieceList:
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[j][i] != null) {
                    int piece = board[j][i].toInt();
                    if (piece > 3 || (piece == 3 && !((Bishop)board[j][i]).isOnWhiteTile())) piece++;
                    if (board[j][i].isWhite(povIsWhite)) piece += 8;
                    pieces.add(piece);
                }
                if (pieces.size() > 4) break GetPieceList;
            }
        }
        if (pieces.size() < 5) {
            Collections.sort(pieces);
            int[][] draws = {{0, 8}, {0, 3, 8}, {0, 4, 8}, {0, 5, 8}, {0, 8, 11},
                            {0, 8, 12}, {0, 8, 13}, {0, 3, 8, 11}, {0, 4, 8, 12}};
            CheckDraw:
            for (int[] draw : draws) {
                if (draw.length != pieces.size()) continue;
                for (int piece = 0; piece < draw.length; piece++) {
                    if (pieces.get(piece) != draw[piece])
                        continue CheckDraw;
                }
                return 9;
            }
        }
        return 0;
    }


    public String getPieceAt(int position) {
        return board[position/8][position%8].getClass().getSimpleName();
    }


    public String toString() {
        StringBuilder str = new StringBuilder(72);
        for (int row = 0; row < 8; row++) {
            if (row != 0) str.append('\n');
            for (int column = 0; column < 8; column++) {
                if (board[row][column] == null) {
                    str.append(' ');
                } else {
                    str.append(board[row][column].toChar(true));
                }
            }
        }
        return str.toString();
    }

    public boolean isNull(int row, int col) {
        return board[row][col] == null;
    }
    public boolean isPov(int row, int col) {
        return board[row][col].isPov();
    }
    public boolean isPovIsWhite() {
        return povIsWhite;
    }
    public int getHalfMove() {
        return halfMoveClock;
    }
    public int getTurns() {
        return turns;
    }

    public void print() { System.out.println(this); }
}
