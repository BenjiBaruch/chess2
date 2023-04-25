import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class Queen extends ChessPiece {

    public Queen(boolean povPlayer, int position) {
        super(povPlayer, position);
    }
    public char toChar(boolean povIsWhite) {
        if (povIsWhite == povPlayer) {
            return (char)9813; // 0x2655 represents white queen char
        } else {
            return (char)9819; // 0x265B represents black queen char
        }
    }
    public int toInt() {
        return 1; // 1 represents queens
    }
    public BufferedImage getSprite(boolean povIsWhite) { // Help from https://youtu.be/wT9uNGzMEM4
        try {
            if (povIsWhite == povPlayer)
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/queen.png"));
            else
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/queen1.png"));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] moveMap(ChessPiece[][] board, int turns) {
        /*
        Generates a list of all tiles that this queen can move to.
        Does not cull moves that result in check.
         */
        int[] moves = new int[28];
        Arrays.fill(moves, -1);
        shootRay(moves, board, 1, 0); // All moves to the right
        shootRay(moves, board, -1, 0); // All moves to the left
        shootRay(moves, board, 0, 1); // All moves up
        shootRay(moves, board, 0, -1); // All moves down
        shootRay(moves, board, 1, 1); // Up-Right
        shootRay(moves, board, -1, 1); // Up-Left
        shootRay(moves, board, 1, -1); // Down-Right
        shootRay(moves, board, -1, -1); // Down-Left
        return moves;
    }
}
