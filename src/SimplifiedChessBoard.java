import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SimplifiedChessBoard {
    /*
    Tile Codes:
    -1: Empty
    0: White King, castleable
    1: White King, uncastleable
    2: White Queen
    3: White Rook, castleable
    4. White Rook, uncastleable
    5: White Bishop
    6: White Knight
    7: White Pawn, unenpassantable
    8: White Pawn, enpassantable
    9: Black King, castleable
    10: Black King, uncastleable
    11: Black Queen
    12: Black Rook, castleable
    13: Black Rook, uncastleable
    14: Black Bishop
    15: Black Knight
    16: Black Pawn, unenpassantable
    17: Black Pawn, enpassantable

    Board Setup:
    0-63: tiles
    64: 1 is white to move, 0 is black to move
    65: white king position
    66: black king position
    67: en passant pawn position
    68: halfmove clock
    69: fullmove clock
    */

    // https://www.bettercodebytes.com/the-cost-of-object-creation-in-java-including-garbage-collection/

    static final int[][] weights = getWeights();
    static final int[] samplePieceValues = {0, -6, -6, -5, -4, -4, -2, -3, -1, -1, 6, 6, 5, 4, 4, 2, 3, 1, 1};
    static final int[] sampleOrder = {10, 10, 8, 6, 6, 2, 4, 0, 0, 11, 11, 9, 7, 7, 3, 5, 1, 1};
    public static int[] newBlankBoard() {
        /*
        Creates int[] chess board with initial position.
         */
        int[] board = new int[70];
        for (int i = 16; i < 56; i++) {
            board[i] = -1;
        }
        for (int i = 0; i < 8; i++) {
            board[8+i] = 7;
            board[8*6+i] = 16;
        }
        board[0] = board[7] = 3;
        board[1] = board[6] = 6;
        board[2] = board[5] = 5;
        board[4] = 2;
        board[56] = board[63] = 12;
        board[57] = board[62] = 15;
        board[58] = board[61] = 14;
        board[60] = 11;
        board[59] = 9;
        board[64] = 1;
        board[65] = 3;
        board[66] = 59;
        board[67] = -1;
        return board;
    }

    public static int[] chessPieceToIntBoard(ChessPiece[][] wackyBoard, int turn, boolean flipBoard, int halfMove, boolean povIsWhite) {
        /*
        Creates int[] board from ChessPiece[][] board
         */
        int[] board = new int[70];
        board[67] = -1;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (wackyBoard[i][j] == null) board[i*8+j] = -1;
                else if (!flipBoard) {
                    int piece = wackyBoard[i][j].toInt() + (wackyBoard[i][j].isPov() ? 0 : 9);
                    if (piece % 9 > 0) piece++;
                    if (piece % 9 > 3) piece++;
                    else if ((piece % 9 == 0 || piece % 9 == 3) && !wackyBoard[i][j].getCastleable()) piece++;
                    if (piece % 9 == 7 && wackyBoard[i][j].getEnPassantable(halfMove)) {
                        piece++;
                        board[67] = i*8+j;
                    }
                    board[i*8+j] = piece;
                    if (piece == 0 || piece == 1) board[65] = i*8+j;
                    if (piece == 9 || piece == 10) board[66] = i*8+j;
                }
                else {
                    int piece = wackyBoard[7-i][j].toInt() + (wackyBoard[7-i][j].isPov() ? 0 : 9);
                    if (piece % 9 > 0) piece++;
                    if (piece % 9 > 3) piece++;
                    else if ((piece % 9 == 0 || piece % 9 == 3) && !wackyBoard[7-i][j].getCastleable()) piece++;
                    if (piece % 9 == 7 && wackyBoard[7-i][j].getEnPassantable(halfMove)) {
                        piece++;
                        board[67] = i*8+j;
                    }
                    board[i*8+j] = piece;
                    if (piece == 0 || piece == 1) board[65] = i*8+j;
                    if (piece == 9 || piece == 10) board[66] = i*8+j;
                }
            }
        }
        board[64] = povIsWhite ? 1 : 0;
        board[68] = halfMove;
        board[69] = turn/2;
        return board;
    }


    public static ChessPiece[][] intToChessPieceBoard(int[] board, boolean povIsWhite) {
        /*
        Creates ChessPiece[][] board from int[] board
         */
        ChessPiece[][] wackyBoard = new ChessPiece[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int piece = board[y*8+x];
                if (piece == -1) continue;
                boolean pov = piece < 9 == povIsWhite;
                boolean special = piece%9==0 || piece%9==3 || piece%9==8;
                if (piece%9<2) wackyBoard[y][x] = new King(pov, y*8+x, special);
                else if (piece%9==2) wackyBoard[y][x] = new Queen(pov, y*8+x);
                else if (piece%9==3||piece%9==4) wackyBoard[y][x] = new Rook(pov, y*8+x, special);
                else if (piece%9==5) wackyBoard[y][x] = new Bishop(pov, y*8+x);
                else if (piece%9==6) wackyBoard[y][x] = new Knight(pov, y*8+x);
                else wackyBoard[y][x] = new Pawn(pov, y*8+x, special);
            }
        }
        return wackyBoard;
    }

    public static int[] stringToIntBoard(String str, boolean flipBoard) {
        /*
        Creates int[] board from FEN string

        https://www.chess.com/terms/fen-chess
        https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
         */
        int[] board = new int[70];
        int strIndex = 0;
        int spaces = 0;
        for (int pos = 0; pos < 64; pos++) {
            int i;
            if (flipBoard) i = (7-pos/8)*8+(pos%8);
            else i = pos;
            // Write empty tiles
            if (str.charAt(strIndex) == '/') strIndex++;
            if (spaces > 0) {
                board[i] = -1;
                spaces--;
            }
            // Figure out how many empty tiles to write
            else if (str.charAt(strIndex) > '0' && str.charAt(strIndex) <= '9') {
                board[i] = -1;
                spaces = str.charAt(strIndex++) - '0' - 1;
            }
            // Write a piece to a tile, ignore slashes
            else {
                char p = str.charAt(strIndex++);
                if (p=='K') {board[i] = 1; board[65] = i;}
                else if (p=='Q') board[i] = 2;
                else if (p=='R') board[i] = 4;
                else if (p=='B') board[i] = 5;
                else if (p=='N') board[i] = 6;
                else if (p=='P') board[i] = 7;
                else if (p=='k') {board[i] = 10; board[66] = i;}
                else if (p=='q') board[i] = 11;
                else if (p=='r') board[i] = 13;
                else if (p=='b') board[i] = 14;
                else if (p=='n') board[i] = 15;
                else if (p=='p') board[i] = 16;
                else System.out.println("Unexpected " + p);
            }
        }
        // Skip unnecessary slash character at end
        if (str.charAt(strIndex) == '/') strIndex++;
        // If no extra info, return board
        if (str.charAt(strIndex++) != ' ') return board;
        // Set whose turn it is
        if (str.charAt(strIndex) == 'w') board[64] = 1;
        else if (str.charAt(strIndex) == 'b') board[64] = 0;
        else if (str.charAt(strIndex) != '-') strIndex -= 2;
        // If no extra info, return board
        if (str.charAt(++strIndex) != ' ') return board;
        // Determine castling rights
        while (str.charAt(++strIndex) != ' ') {
            if (str.charAt(strIndex) == 'Q') {
                board[board[65]] = 0;
                board[7] = (!flipBoard ? 12 : 3);
            }
            else if (str.charAt(strIndex) == 'K') {
                board[board[65]] = 0;
                board[0] = (!flipBoard ? 12 : 3);
            }
            else if (str.charAt(strIndex) == 'q') {
                board[board[66]] = 9;
                board[63] = (!flipBoard ? 3 : 12);
            }
            else if (str.charAt(strIndex) == 'k') {
                board[board[66]] = 9;
                board[56] = (!flipBoard ? 3 : 12);
            }
        }
        // If no extra info, return board
        if (str.charAt(strIndex) != ' ') return board;
        // Determine en passant capabilitiy
        if (str.charAt(strIndex+1) == '-') board[67] = -1;
        else if (str.charAt(strIndex+2) == '3') {
            board[67] = 24 + str.charAt(++strIndex) - 'a';
            board[board[67]] = 8;
        }
        else if (str.charAt(strIndex+2) == '6') {
            board[67] = 32 + str.charAt(++strIndex) - 'a';
            board[board[67]] = 17;
        } else {
            board[67] = -1;
        }
        // If no extra info, return board
        strIndex += 2;
        if (str.charAt(strIndex) != ' ') return board;
        // Determine half-moves
        if (str.charAt(strIndex+2) == ' ') {
            board[68] = str.charAt(++strIndex) - (int)'0';
        }
        else if (str.charAt(strIndex+3) == ' ') {
            board[68] = 10 * (str.charAt(++strIndex) - (int)'0');
            board[68] += str.charAt(++strIndex) - (int)'0';
        } else {
            board[68] = 100 * (str.charAt(++strIndex) - (int)'0');
            board[68] += 10 * (str.charAt(++strIndex) - (int)'0');
            board[68] += str.charAt(++strIndex) - (int)'0';
        }
        // If no extra info, return board
        if (str.charAt(++strIndex) != ' ') return board;
        // Determine full moves
        if (str.length() - strIndex == 2 || str.charAt(strIndex+2) == ' ') {
            board[69] = str.charAt(++strIndex) - (int)'0';
        }
        else if (str.length() - strIndex == 3 || str.charAt(strIndex+3) == ' ') {
            board[69] = 10 * (str.charAt(++strIndex) - (int)'0');
            board[69] += str.charAt(++strIndex) - (int)'0';
        } else {
            board[69] = 100 * (str.charAt(++strIndex) - (int)'0');
            board[69] += 10 * (str.charAt(++strIndex) - (int)'0');
            board[69] += str.charAt(++strIndex) - (int)'0';
        }
        return board;
    }

    public static String intBoardToString(int[] board) {
        StringBuilder str = new StringBuilder();
        int blank = 0;
        // 0: White King, castleable
        // 1: White King, uncastleable
        // 2: White Queen
        // 3: White Rook, castleable
        // 4. White Rook, uncastleable
        // 5: White Bishop
        // 6: White Knight
        // 7: White Pawn, unenpassantable
        // 8: White Pawn, enpassantable
        // 9: Black King, castleable
        // 10: Black King, uncastleable
        // 11: Black Queen
        // 12: Black Rook, castleable
        // 13: Black Rook, uncastleable
        // 14: Black Bishop
        // 15: Black Knight
        // 16: Black Pawn, unenpassantable
        // 17: Black Pawn, enpassantable
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0 && i > 0) {
                if (blank > 0) str.append(blank);
                blank = 0;
                str.append('/');
            }
            if (board[i] == -1) {
                blank++;
                continue;
            }
            if (blank > 0) str.append(blank);
            blank = 0;
            char x;
            switch (board[i]) {
                case 0, 1 ->    x = 'K';
                case 2 ->       x = 'Q';
                case 3, 4 ->    x = 'R';
                case 5 ->       x = 'B';
                case 6 ->       x = 'N';
                case 7, 8 ->    x = 'P';
                case 9, 10 ->   x = 'k';
                case 11 ->      x = 'q';
                case 12, 13 ->  x = 'r';
                case 14 ->      x = 'b';
                case 15 ->      x = 'n';
                case 16, 17 ->  x = 'p';
                default ->      x = '?';
            }
            str.append(x);
        }
        return str.toString();
    }

    public static int[] flipBoard(int[] board1) {
        /*
        Flips the board vertically
         */
        int[] board2 = board1.clone();
        for (int i = 0; i < 8; i++) for (int j = 0; j < 8; j++) {
            board2[i*8+j] = board1[(7-i)*8+j];
        }
        return board2;
    }

    public static int[][] getWeights() {
        /*
        Returns move weights (see getMoveList).

        ## New weight system:

        Special (move type: weight)
        Castle: 32
        Promote to Knight: 60
        Promote to Queen: 62
        En Passant Capture: 12

        Piece moves (piece: weight)
        P: 4
        B: 8
        N: 10
        R: 6
        Q: 8
        K: 2

        Captures (piece: default weight {Pw, Bw, Nw, Nw, Qw, Kw})
        Px: 16 {13, 31, 31, 39, 47, 61}
        Bx: 12 {9, 27, 27, 35, 43, 57}
        Nx: 12 {9, 27, 27, 35, 43, 57}
        Rx: 6 {3, 21, 21, 29, 37, 51}
        Qx: 4 {1, 19, 19, 27, 35, 49}
        Kx: 18 {15, 33, 33, 41, 49, 63}

        Capture Weights (captured piece: weight adjustment)
        xP: -3
        xN: +15
        xB: +15
        xR: +23
        xQ: +31
        xK: +45
        */

        int[] pieceMoves = {2, 2, 8, 6, 6, 8, 10, 4, 4};
        int[] pieceCaptures = {18, 18, 4, 6, 6, 12, 12, 16, 16};
        int[] captureWeights = {45, 45, 31, 23, 23, 15, 15, -3, -3};
        int[][] weights = {pieceMoves, pieceCaptures, captureWeights};
        return weights;
    }

    public static int[] getMoveList(int[] board, boolean turn) {
        /*
        This function searches the board and returns an int array of all the possible moves that the active player can make.

        Higher weight means a move is more likely to be the best move. Moves will be sorted from greatest to least,
        and likely good moves will be searched first so that alpha-beta pruning is most efficient.
        Pos1 is the position of the piece being moved and Pos2 is the destination.
        Weight, pos1, and pos2 values are all between 0 and 63.
        Move is the number that stores weight, pos1, and pos2. This is what is returned to the search algorithm.

        Move = (weight * 4096) + (pos1 * 64) + pos2

        Weight = move / 4096
        Pos1 = (move / 64) % 64
        Pos2 = move % 64
        */


        int[] moveList = new int[143];
        moveList[0] = 1;
        for (int i = 0; i < 64; i++) {
            if (board[i] > -1 && ((board[i] < 9) == turn)) movePiece(board, moveList, i);
        }
        return Arrays.copyOfRange(moveList, 1, moveList[0]);
    }

    public static int[] getPieceMoves(int[] board, int pos) {
        /*
        Returns all moves for one piece
         */
        int[] moveList = new int[29];
        moveList[0] = 1;
        return movePiece(board, moveList, pos);
    }


    private static int shootRay(int[] board, int[] moveList, int pos, int offset) {
        /*
        Gets all moves in a direction, adds them to moveList
        Used for pieces that can move any distance (bishops, rooks, queens)
         */
        if (board[pos] == -1) return board[0];
        boolean turn = board[pos] < 9;
        int piece = board[pos] % 9;
        int moveWeight = weights[0][piece] * 4096;
        int size = moveList[0];
        int offPos = pos;
        while(offPos+offset > -1 && offPos+offset < 64 && board[offPos+offset] == -1 &&
                !(offPos%8 == 0 && (offset%8 == -1 || offset%8 == 7)) && !(offPos%8 == 7 && (offset%8 == -7 || offset%8 == 1))) {
            offPos += offset;
            moveList[size++] = moveWeight + 64*pos + offPos;
        }
        if (offPos+offset > -1 && offPos+offset < 64 && (board[offPos+offset] < 9) != turn &&
                !(offPos%8 == 0 && (offset%8 == -1 || offset%8 == 7)) && !(offPos%8 == 7 && (offset%8 == -7 || offset%8 == 1))) {
            moveList[size++] = 4096*(weights[1][piece]+weights[2][board[offPos+offset]%9]) + 64*pos + offPos + offset;
        }
        moveList[0] = size;
        return size;
    }


    private static int[] movePiece(int[] board, int[] moveList, int pos) {
        /*
        Gets all moves for a piece, adds them to moveList
         */
        boolean turn = board[pos] < 9;
        int piece = board[pos] % 9;
        int size = moveList[0];

        // King
        if (piece < 2) {
            for (int i = -8; i <= 8; i+=8) {
                for (int j = -1; j <= 1; j++) {
                    if (i-j != 0 && pos+i+j > 0 && pos+i+j < 64 && pos%8+j != -1 && pos%8+j != 8 &&
                            (board[pos+i+j] == -1 || (board[pos+i+j] < 9) != turn)) { // Move king
                        moveList[size++] = 4096*(board[pos+i+j]>-1?18+weights[2][board[pos+i+j]%9]:2) + 65*pos + i+j;
                    }
                }
            }
            if (piece == 0) {
                if (((pos == 3 | (pos == 4 && board[3] == -1)) && board[0] == 3 && board[1] == -1 && board[2] == -1) ||
                        ((pos == 59 | (pos == 60 && board[59] == -1)) && board[56] == 12 && board[57] == -1 && board[58] == -1)) {
                    // check danger
                    moveList[size++] = 131072 + pos*65-2; // castle left
                }
                if (((pos == 4 | (pos == 3 && board[4] == -1)) && board[7] == 3 && board[6] == -1 && board[5] == -1) ||
                        ((pos == 60 | (pos == 59 && board[60] == -1)) && board[63] == 12 && board[62] == -1 && board[61] == -1)) {
                    // check danger
                    moveList[size++] = 131072 + pos*65+2; // Castle right
                }
            }
        }

        // Queen
        else if (piece == 2) {
            for (int i = -8; i <= 8; i+=8) { // Up, Middle, Down
                for (int j = -1; j <= 1; j++) { // Left, Middle, Right
                    if (i-j!=0) size = shootRay(board, moveList, pos, i+j); // Ray in direction if not middle-middle
                }
            }
        }

        // Rook
        else if (piece == 3 || piece == 4) {
            size = shootRay(board, moveList, pos, 1); // Right
            size = shootRay(board, moveList, pos, -1); // Left
            size = shootRay(board, moveList, pos, 8); // Down
            size = shootRay(board, moveList, pos, -8); // Up
        }

        // Bishop
        else if (piece == 5) {
            size = shootRay(board, moveList, pos, 9); // Down-Right
            size = shootRay(board, moveList, pos, 7); // Down-Left
            size = shootRay(board, moveList, pos, -9); // Up-Left
            size = shootRay(board, moveList, pos, -7); // Up-Right
        }

        // Knight
        else if (piece == 6) {
            if (pos > 7) {
                if (pos > 15) {
                    if (pos % 8 < 7 && (board[pos-15] == -1 || (board[pos-15] > 8) == turn)) {
                        moveList[size++] = 4096*(board[pos-15]>-1?12+weights[2][board[pos-15]%9]:8) + pos*65 - 15; // Up two right one
                    }
                    if (pos % 8 > 0 && (board[pos-17] == -1 || (board[pos-17] > 8) == turn)) {
                        moveList[size++] = 4096*(board[pos-17]>-1?12+weights[2][board[pos-17]%9]:8) + pos*65 - 17; // Up two left one
                    }
                }
                if (pos % 8 < 6 && (board[pos-6] == -1 || (board[pos-6] > 8) == turn)) {
                    moveList[size++] = 4096*(board[pos-6]>-1?12+weights[2][board[pos-6]%9]:8) + pos*65 - 6; // Up one right two
                }
                if (pos % 8 > 1 && (board[pos-10] == -1 || (board[pos-10] > 8) == turn)) {
                    moveList[size++] = 4096*(board[pos-10]>-1?12+weights[2][board[pos-10]%9]:8) + pos*65 - 10; // Up one left two
                }
            }
            if (pos < 56) {
                if (pos < 48) {
                    if (pos % 8 < 7 && (board[pos+17] == -1 || (board[pos+17] > 8) == turn)) {
                        moveList[size++] = 4096*(board[pos+17]>-1?12+weights[2][board[pos+17]%9]:8) + pos*65 + 17; // Down two right one
                    }
                    if (pos % 8 > 0 && (board[pos+15] == -1 || (board[pos+15] > 8) == turn)) {
                        moveList[size++] = 4096*(board[pos+15]>-1?12+weights[2][board[pos+15]%9]:8) + pos*65 + 15; // Down two left one
                    }
                }
                if (pos % 8 < 6 && (board[pos+10] == -1 || (board[pos+10] > 8) == turn)) {
                    moveList[size++] = 4096*(board[pos+10]>-1?12+weights[2][board[pos+10]%9]:8) + pos*65 + 10; // Down one right two
                }
                if (pos % 8 > 1 && (board[pos+6] == -1 || (board[pos+6] > 8) == turn)) {
                    moveList[size++] = 4096*(board[pos+6]>-1?12+weights[2][board[pos+6]%9]:8) + pos*65 + 6; // Down one left two
                }
            }
        }

        // Pawn
        else {
            // White pawn moves
            if (turn) {
                if (board[pos+8] == -1) {
                    if (pos/8==6) {
                        moveList[size++] = 245760+pos*65+8; // Pawn promote to knight
                        moveList[size++] = 253952+pos*65+8; // Pawn promote to queen
                    } else {
                        moveList[size++] = 16384+pos*65+8; // pawn push 1
                        if (pos/8 == 1 && board[pos+16] == -1) {
                            moveList[size++] = 16384+pos*65+16; // pawn push 2
                        }
                    }
                }
                if (pos%8 > 0 && (board[pos+7] > 8 ^ board[pos-1] == 17)) {
                    if (pos/8==6) {
                        moveList[size++] = 245760+pos*65+7; // Pawn capture left and promote to knight
                        moveList[size++] = 253952+pos*65+7; // Pawn capture left and promote to queen
                    }
                    else // Pawn capture left
                        moveList[size++] = (board[pos-1]!=17?4096*(16+weights[2][board[pos+7]%9]):49152) + pos*65+7;
                }
                if (pos < 55 && pos%8 < 7 && (board[pos+9] > 8 ^ board[pos+1] == 17)) {
                    if (pos/8==6) {
                        moveList[size++] = 245760 + pos * 65 + 9; // Pawn capture right and promote to knight
                        moveList[size++] = 253952 + pos * 65 + 9; // Pawn capture right and promote to queen
                    }
                    else // Pawn capture right
                        moveList[size++] = (board[pos+1]!=17?4096*(16+weights[2][board[pos+9]%9]):49152) + pos*65+9;
                }
            }
            // Black pawn moves
            else {
                if (board[pos-8] == -1) {
                    if (pos/8==1) {
                        moveList[size++] = 245760 + pos * 65 - 8; // Pawn promote to knight
                        moveList[size++] = 253952 + pos * 65 - 8; // Pawn promote to queen
                    } else {
                        moveList[size++] = 16384+pos*65-8; // weight + pos*64 + pos - 8, pawn push 1
                        if (pos/8 == 6 && board[pos-16] == -1) {
                            moveList[size++] = 16384+pos*65-16; // weight + pos*64 + pos - 16, pawn push 2
                        }
                    }
                }
                if (pos%8 < 7 && ((board[pos-7] > -1 && board[pos-7] < 9) ^ board[pos+1] == 8)) {
                    if (pos/8==1) {
                        moveList[size++] = 245760 + pos * 65 - 7; // Pawn capture right and promote to knight
                        moveList[size++] = 253952 + pos * 65 - 7; // Pawn capture right and promote to queen
                    }
                    else // Pawn capture right
                        moveList[size++] = (board[pos+1]!=8?4096*(16+weights[2][board[pos-7]]):49152) + pos*65-7;
                }
                if (pos > 8 && pos%8 > 0 && ((board[pos-9] > -1 && board[pos-9] < 9) ^ board[pos-1] == 8)) {
                    if (pos/8==1) {
                        moveList[size++] = 245760 + pos * 65 - 9; // Pawn capture left and promote to knight
                        moveList[size++] = 253952 + pos * 65 - 9; // Pawn capture left and promote to queen
                    }
                    else // Pawn capture left
                        moveList[size++] = ((board[pos-1]!=8)?(4096*(16+weights[2][board[pos-9]])):49152) + pos*65-9;
                }
            }
        }
        moveList[0] = size;
        return moveList;
    }

    public static boolean[][] attackMaps(int[] board) {
        boolean[][] map = new boolean[2][64];
        for (int i = 0; i < 64; i++) {
            if (board[i] > -1) {
                map[board[i]>8?1:0][i] = true;
                for (int move : getPieceMoves(board, i)) {
                    map[board[i]>8?1:0][move%64] = true;
                }
            }
        }
        return map;
    }

    public static String moveToString(int[] board, int move) {
        /*
        Converts a move from int to string, formatted in algebraic notation
        https://en.wikipedia.org/wiki/Algebraic_notation_(chess)
         */
        // Initialize variables
        int weight = move / 4096;
        int pos1 = (move / 64) % 64;
        int pos2 = move % 64;
        System.out.println("\npos1 = " + pos1 + " pos2 = " + pos2);
        if (pos1 < 0 || pos2 < 0)
            return Integer.toString(move);
        int pn = board[pos1];

        // Initialize move string components
        char piece;
        String specifiers;
        String dest;
        String promotion = "";
        String suffix = "";
        String addendum = "";

        // Check pawn promotion
        if (weight == 60) promotion = "N";
        else if (weight == 62) promotion = "Q";

        // Check en passant
        if (weight == 10) addendum = " e.p.";

        // Check suffix (to-do)

        // Get second half of move str
        boolean capture = weight % 2 == 1;
        dest = (capture ? "x" : "") + (char)(pos2%8+'a') + ((pos2/8)+1);

        // Look for other pieces of the same type that could make a simple piece indicator ambiguous
        int[] ambiguators = new int[64];
        for (int i = 0; i < 64; i++) {
            if (board[i] == pn || (board[i] > 15 && pn > 15) ||
                    ((board[i] == 3 || board[i] == 4) && (pn == 3 || pn == 4)) ||
                    ((board[i] == 7 || board[i] == 8) && (pn == 7 || pn == 8)) ||
                    ((board[i] == 12 || board[i] == 13) && (pn == 12 || pn == 13)))
                ambiguators[++ambiguators[0]] = i;
        }
        ambiguators = Arrays.copyOfRange(ambiguators, 1, ambiguators[0]+1);
        int rowCopiers = -1;
        int colCopiers = -1;
        for (int pos : ambiguators) {
            if (pos/8 == board[pos1]/8) rowCopiers++;
            if (pos%8 == board[pos1]%8) colCopiers++;
        }

        // Determine specifiers
        specifiers = (rowCopiers > 0 ? Character.toString(pos1%8+'a') : "") + (colCopiers > 0 ? Integer.toString(pos1/8+1) : "");

        // Get piece char
        if (pn == -1) piece = '?';
        else if (pn%9 < 2) piece = 'K';
        else if (pn%9 == 2) piece = 'Q';
        else if (pn%9 == 3 || pn%9 == 4) piece = 'R';
        else if (pn%9 == 5) piece = 'B';
        else if (pn%9 == 6) piece = 'N';
        else piece = 'P';

        return (piece == 'P' ? specifiers : piece + specifiers) + dest + promotion + suffix + addendum;
    }

    public static String toString(int[] board) {
        StringBuilder str = new StringBuilder();
        int spaces = 0;
        for (int i = 0; i < 64; i++) {
            int pos = (7-i/8)*8+(i%8);
            if (board[pos] == -1) {
                if (pos%8<7) spaces++;
                else {
                    str.append(++spaces);
                    if (i<63) str.append('/');
                    spaces = 0;
                }
            }
            else {
                if (spaces > 0) {
                    str.append(spaces);
                    spaces = 0;
                }
                int p = board[pos];
                if (p < 2) str.append('K');
                else if (p == 2) str.append('Q');
                else if (p == 3 || p == 4) str.append('R');
                else if (p == 5) str.append('B');
                else if (p == 6) str.append('N');
                else if (p == 7 || p == 8) str.append('P');
                else if (p == 9 || p == 10) str.append('k');
                else if (p == 11) str.append('q');
                else if (p == 12 || p == 13) str.append('r');
                else if (p == 14) str.append('b');
                else if (p == 15) str.append('n');
                else if (p == 16 || p == 17) str.append('p');
                else str.append('X');
                if (pos%8==7&&i<63) str.append('/');
            }
        }
        str.append(' ');
        str.append(board[64] == 1 ? 'w' : 'b');
        str.append(' ');
        int l = str.length();
        if (board[0] == 3 && board[board[65]] == 0) str.append('K');
        if (board[7] == 3 && board[board[65]] == 0) str.append('Q');
        if (board[56] == 12 && board[board[66]] == 9) str.append('k');
        if (board[63] == 12 && board[board[66]] == 9) str.append('q');
        if (str.length() == l) str.append('-');
        str.append(' ');
        if (board[67] > -1) {
            str.append((char)((board[67] % 8) + 'a'));
            str.append(board[67] / 8 == 2 ? '3' : '6');
        }
        else str.append('-');
        str.append(' ');
        str.append(Integer.toString(board[68]));
        str.append(' ');
        str.append(Integer.toString(board[69]));
        return str.toString();
    }
    public static String toFormattedString(int[] board, boolean pov, boolean numeric) {
        /*
        Like toString, but uses unicode chess symbols instead of letters.
        Instead uses numbers if numeric is true.
         */
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = i*8; j < i*8+8; j++) {
                int h;
                if (pov) h = board[j];
                else h = board[8 * (7 - (j / 8)) + (j % 8)];
                if (numeric) {
                    if (h == -1) str.append('_');
                    else str.append(h%9);
                }
                else {
                    if (h == -1) str.append('_');
                    else if (h == 0 || h == 1) str.append((char) 9812);
                    else if (h == 9 || h == 10) str.append((char) 9818);
                    else if (h == 2) str.append((char) 9813);
                    else if (h == 11) str.append((char) 9819);
                    else if (h == 3 || h == 4) str.append((char) 9814);
                    else if (h == 12 || h == 13) str.append((char) 9820);
                    else if (h == 5) str.append((char) 9815);
                    else if (h == 14) str.append((char) 9821);
                    else if (h == 6) str.append((char) 9816);
                    else if (h == 15) str.append((char) 9822);
                    else if (h == 7 || h == 8) str.append((char) 9817);
                    else if (h == 16 || h == 17) str.append((char) 9823);
                }
            }
            str.append('\n');
        }
        return str.toString();
    }


    private static int parseEval(String eval) {
        if (eval.charAt(0) == '#') {
            // '#' indicates end of game
            if (eval.charAt(2) == '0') return 0;
            if (eval.charAt(1) == '+') return -9999;
            if (eval.charAt(1) == '-') return 9999;
            System.out.println("\n? " + eval);
            return 0;
        }
        else if (eval.charAt(0) == '+') {
            return Integer.parseInt(eval.substring(1));
        }
        else {
            return Integer.parseInt(eval);
        }
    }

    public static String fenToSample(String[] fen) {
        int[] board = stringToIntBoard(fen[0], false);
        int eval = parseEval(fen[1]);
        StringBuilder sample = new StringBuilder(200);
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 64; j++) {
                if (board[j] > -1 && sampleOrder[board[j]] == i)
                    sample.append('1');
                else
                    sample.append('0');
                sample.append(',');
            }
        }
        for (boolean[] side : attackMaps(board)) {
            for (boolean i : side) {
                sample.append(i ? '1' : '0');
                sample.append(',');
            }
        }
        sample.append(Integer.toString(eval));
        sample.append('\n');
        return sample.toString();
    }


    public static void convertFenCSV(String iPath, String oPath, int stop) {
        FileWriter out;
        try {
            out = new FileWriter(oPath, true);
        } catch (IOException e) {
            System.out.println("frick 3");
            return;
        }
        try (BufferedReader in = new BufferedReader(new FileReader(iPath))) {
            String entry;
            in.readLine();
            int i = 0;
            while ((entry = in.readLine()) != null) {
                if (i++ % 100 == 0) System.out.print('i');
                if (i >= stop) return;
                String[] fen = entry.split(",");
                String sample = fenToSample(fen);
                out.write(sample);
                out.flush();
            }
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("frick 404");
        } catch (IOException e) {
            System.out.println("frick 2");
        }
    }
/*
    public static String sampleToFen(String iPath, int start, int stop) {
        ArrayList<String> fens = new ArrayList<>(stop - start);
        try (BufferedReader in = new BufferedReader(new FileReader(iPath))) {
            String entry;
            String[] sample;
            int[]
            for (int i = 0; i < start; i++) {
                in.readLine();
            }
            for (int i = start; i < stop; i++) {
                entry = in.readLine();
                if (entry == null) continue;
                sample = entry.split(",");

            }
        } catch (FileNotFoundException e) {
            System.out.println("frick 404");
        } catch (IOException e) {
            System.out.println("frick 2");
        }
    }

 */

    public static void main(String[] args) {
        convertFenCSV(
        "C:\\Users\\ultra\\IdeaProjects\\chess2\\data\\fen1.csv",
        "C:\\Users\\ultra\\IdeaProjects\\chess2\\data\\samples.csv",
        1000000
        );
    }
}
