// Sprites from Lucas312 on OpenGameArt.Org
// https://opengameart.org/content/pixel-chess-pieces

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ChessPiece {
    boolean povPlayer; // Check why this can't be private
    int position;

    public ChessPiece() {
        povPlayer = true;
        position = 0;
    }

    public ChessPiece(boolean povPlayer, int position) {
        this.povPlayer = povPlayer;
        this.position = position;
    }

    public int[] moveMap(ChessPiece[][] board, int turns) {
        return new int[0];
    }

    public void addMove(int[] moves, ChessPiece[][] board, int position) {
        /*
        Adds a move to the move list
        Excludes capturing own pieces (cannibal moves)
         */
        if (!(board[position/8][position%8] == null || board[position/8][position%8].isPov() != povPlayer)) {
            return;
        }
        int index = 0;
        while(moves[index] > -1) {
            index++;
        }
        moves[index] = position;
    }

    public boolean getEnPassantable(int turn) {
        return false;
    } // If this runs, the piece is not a pawn
    public boolean getCastleable() {
        return false;
    } // if this runs, the piece is not a rook


    public ChessPiece[][] move(ChessPiece[][] board, int p2, int turns) {
        /*
        Updates the board with a move
        Assumes the move is legal
        Returns board
         */
        board[p2/8][p2%8] = board[position/8][position%8];
        board[position/8][position%8] = null;
        position = p2;
        return board;
    }

    public BufferedImage getSprite(boolean povIsWhite) {
        System.out.println("Why the hell is this running? That's not suppose to happen!");
        return null;
    }
    public void shootRay(int[] moves, ChessPiece[][] board, int offsetX, int offsetY) {
        /*
        Adds all valid moves from a position in a direction.
        Stops when a piece or edge is hit.
        Used for pieces that can move any distance (Bishops, Rooks, and Queens).
         */
        int x = position % 8 + offsetX;
        int y = position / 8 + offsetY;
        while (x >= 0 && x < 8 && y >= 0 && y < 8 && board[y][x] == null) {
            // If the next tile is blank, add it to the move list and iterate x and/or y.
            addMove(moves, board, y * 8 + x);
            x += offsetX;
            y += offsetY;
        }
        if (x >= 0 && x < 8 && y >= 0 && y < 8 && board[y][x] != null && board[y][x].isPov() != povPlayer) {
            // If the final piece in a ray belongs to the opponent, add its capture to the move list.
            addMove(moves, board, y * 8 + x);
        }
    }

    public void updatePosition(int p2) {
        /*
        Currently only used by King to move Rook when castling
         */
        position = p2;
    }

    public boolean isPov() {
        return povPlayer;
    }

    public boolean isWhite(boolean povIsWhite) {
        return povIsWhite == povPlayer;
    }

    public char toChar(boolean povIsWhite) {
        return 'X'; // 'X' represents pieces without a type
    }

    public int toInt() {
        return -1; // -1 represents pieces without a type
    }

}
