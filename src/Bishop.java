import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class Bishop extends ChessPiece {

    public Bishop(boolean povPlayer, int position) {
        super(povPlayer, position);
    }
    public char toChar(boolean povIsWhite) {
        if (povIsWhite == povPlayer) {
            return (char)9815; // 0x2657 represents white bishop char
        } else {
            return (char)9821; // 0x265D represents black bishop char
        }
    }
    public int toInt() {
        return 3; // 3 represents bishops
    }
    public BufferedImage getSprite(boolean povIsWhite) { // Help from https://youtu.be/wT9uNGzMEM4
        try {
            if (povIsWhite == povPlayer)
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/bishop.png"));
            else
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/bishop1.png"));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public int[] moveMap(ChessPiece[][] board, int turns) {
        /*
        Generates a list of all tiles that this bishop can move to.
        Does not cull moves that result in check.
         */
        int[] moves = new int[14];
        Arrays.fill(moves, -1);
        shootRay(moves, board, 1, 1); // Up-Right
        shootRay(moves, board, -1, 1); // Up-Left
        shootRay(moves, board, 1, -1); // Down-Right
        shootRay(moves, board, -1, -1); // Down-Left
        return moves;
    }

    public boolean isOnWhiteTile() {
        return (position % 2 == 1);
    }
}
