import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class King extends ChessPiece {

    private boolean castleable;

    public King(boolean povPlayer, int position) {
        super(povPlayer, position);
        this.castleable = true;
    }
    public King(boolean povPlayer, int position, boolean castleable) {
        super(povPlayer, position);
        this.castleable = castleable;
    }
    public char toChar(boolean povIsWhite) {
        if (povIsWhite == povPlayer) {
            return (char)9812; // 0x2654 represents white king char
        } else {
            return (char)9818; // 0x265A represents black king char
        }
    }

    public int toInt() {
        return 0; // 0 represents kings
    }
    public ChessPiece[][] move(ChessPiece[][] board, int p2, int turns) {
        /*
        Overrides ChessPiece move function to handle castling
         */
        castleable = false;
        board[p2/8][p2%8] = board[position/8][position%8];
        if (position - p2 == 2) { // Left castle
            board[p2/8][0].updatePosition(position-1);
            board[position/8][position%8-1] = board[p2/8][0];
            board[p2/8][0] = null;
        } else if (position - p2 == -2) { // Right castle
            board[p2/8][7].updatePosition(position+1);
            board[position/8][position%8+1] = board[p2/8][7];
            board[p2/8][7] = null;
        }
        board[position/8][position%8] = null;
        position = p2;
        return board;
    }
    public BufferedImage getSprite(boolean povIsWhite) { // Help from https://youtu.be/wT9uNGzMEM4
        try {
            if (povIsWhite == povPlayer)
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/king.png"));
            else
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/king1.png"));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean getCastleable() {
        return castleable;
    }

    public int[] moveMap(ChessPiece[][] board, int turns) {
        /*
        Generates a list of all tiles that this king can move to.
        Includes castling.
        Does not cull moves that result in check, except in cases of castling.
         */
        int[] moves = new int[10];
        Arrays.fill(moves, -1);
        if (position > 7) {
            addMove(moves, board, position - 8); // Up
            if (position % 8 > 0) addMove(moves, board, position - 9); // Up-Left
            if (position % 8 < 7) addMove(moves, board, position - 7); // Up-Right
        }
        if (position < 56) {
            addMove(moves, board, position + 8); // Down
            if (position % 8 > 0) addMove(moves, board, position + 7); // Down-Left
            if (position % 8 < 7) addMove(moves, board, position + 9); // Down-Right
        }
        if (position % 8 > 0) addMove(moves, board, position - 1); // Left
        if (position % 8 < 7) addMove(moves, board, position + 1); // Right
        if (castleable && board[position/8][0] != null && board[position/8][0].getCastleable() &&
                board[position/8][1] == null && board[position/8][2] == null &&
                (position%8 == 3 || board[position/8][2] == null) &&
                !ChessBoard.checksSelf(board, position, turns, povPlayer) &&
                !ChessBoard.checksSelf(board, position - 1, turns, povPlayer))
            addMove(moves, board, position - 2); // Castle Left (doesn't check checks yet)
        if (castleable && board[position/8][7] != null && board[position/8][7].getCastleable() &&
                board[position/8][6] == null && board[position/8][5] == null &&
                (position%8 == 4 || board[position/8][4] == null) &&
                !ChessBoard.checksSelf(board, position, turns, povPlayer) &&
                !ChessBoard.checksSelf(board, position + 1, turns, povPlayer))
            addMove(moves, board, position + 2); // Castle Right (doesn't check checks yet)

        return moves;
    }
}
