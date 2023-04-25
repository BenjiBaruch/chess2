import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class Pawn extends ChessPiece {
    private int enPassant;
    private boolean enPassantOverride;
    public Pawn(boolean povPlayer, int position) {
        super(povPlayer, position);
        enPassant = -2;
        enPassantOverride = false;
    }
    public Pawn(boolean povPlayer, int position, boolean enPassantOverride) {
        super(povPlayer, position);
        enPassant = -2;
        this.enPassantOverride = enPassantOverride;
    }
    public char toChar(boolean povIsWhite) {
        if (povIsWhite == povPlayer) {
            return (char)9817; // 0x2659 represents white pawn char
        } else {
            return (char)9823; // 0x265F represents black pawn char
        }
    }

    public int toInt() {
        return 5; // 5 represents pawns
    }

    public BufferedImage getSprite(boolean povIsWhite) { // Help from https://youtu.be/wT9uNGzMEM4
        try {
            if (povIsWhite == povPlayer)
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/pawn.png"));
            else
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/pawn1.png"));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean getEnPassantable(int turn) {
        return enPassant + 1 == turn || enPassantOverride;
    }

    public ChessPiece[][] move(ChessPiece[][] board, int p2, int turns) {
        if (Math.abs(position - p2) == 16)
            enPassant = turns; // Indicates that pawn can be captured with en passant if it has just moved up two tiles
        if ((Math.abs(position - p2) == 7 || Math.abs(position - p2) == 9) && board[p2/8][p2%8] == null)
            board[(p2/8) + (position < p2 ? -1 : 1)][p2%8] = null; // Deletes piece that was killed in en passant attack
        if (p2 > 7 && p2 < 56) board[p2/8][p2%8] = board[position/8][position%8]; // Moves pawn
        else board[p2/8][p2%8] = new Queen(povPlayer, p2); // Replaces pawn w/ queen
        board[position/8][position%8] = null;
        position = p2;
        return board;
    }

    public int[] moveMap(ChessPiece[][] board, int turns) {
        /*
        Generates a list of all tiles that this pawn can move to.
        Includes en passant.
        Does not cull moves that result in check.
         */
        int[] moves = new int[4];
        Arrays.fill(moves, -1);
        if (povPlayer) {
            if (position < 56) {
                if (board[position/8+1][position%8] == null) {
                    addMove(moves, board,position + 8); // Push 1
                    if (position/8 == 1 && board[3][position%8] == null) {
                        addMove(moves, board, position + 16); // Push 2
                    }
                }
                if (position % 8 > 0 && board[position/8+1][position%8-1] != null && !board[position/8+1][position%8-1].isPov()) {
                    addMove(moves, board, position + 7); // Capture Up-Left
                } else if (position % 8 > 0 && board[position/8][position%8-1] != null && board[position/8][position%8-1].getEnPassantable(turns)) {
                    addMove(moves, board, position + 7); // En Passant Capture Up-Left
                }
                if (position % 8 < 7 && board[position/8+1][position%8+1] != null && !board[position/8+1][position%8+1].isPov()) {
                    addMove(moves, board, position + 9); // Capture Up-Right
                } else if (position % 8 < 7 && board[position/8][position%8+1] != null && board[position/8][position%8+1].getEnPassantable(turns)) {
                    addMove(moves, board, position + 9); // En Passant Capture Up-Right
                }
            }
        } else {
            if (position > 7) {
                if (board[position/8-1][position%8] == null) {
                    addMove(moves, board, position - 8); // Push 1
                    if (position/8 == 6 && board[4][position%8] == null) {
                        addMove(moves, board, position - 16); // Push 2
                    }
                }
                if (position % 8 > 0 && board[position/8-1][position%8-1] != null && board[position/8-1][position%8-1].isPov()) {
                    addMove(moves, board, position - 9); // Attack Down-Left
                } else if (position % 8 > 0 && board[position/8][position%8-1] != null && board[position/8][position%8-1].getEnPassantable(turns)) {
                    addMove(moves, board, position - 9); // En Passant Capture Up-Left
                }
                if (position % 8 < 7 && board[position/8-1][position%8+1] != null && board[position/8-1][position%8+1].isPov()) {
                    addMove(moves, board, position - 7); // Attack Down-Right
                } else if (position % 8 < 7 && board[position/8][position%8+1] != null && board[position/8][position%8+1].getEnPassantable(turns)) {
                    addMove(moves, board, position - 7); // En Passant Capture Up-Right
                }
            }
        }
        return moves;
    }
}