import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class Knight extends ChessPiece {

    public Knight(boolean povPlayer, int position) {
        super(povPlayer, position);
    }
    public char toChar(boolean povIsWhite) {
        if (povIsWhite == povPlayer) {
            return (char)9816; // 0x2658 represents white knight char
        } else {
            return (char)9822; // 0x265E represents black knight char
        }
    }
    public int toInt() {
        return 4; // 4 represents knights
    }
    public BufferedImage getSprite(boolean povIsWhite) { // Help from https://youtu.be/wT9uNGzMEM4
        try {
            if (povIsWhite == povPlayer)
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/knight.png"));
            else
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/knight1.png"));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] moveMap(ChessPiece[][] board, int turns) {
        /*
        Generates a list of all tiles that this pawn can move to.
        Does not cull moves that result in check.
         */
        int[] moves = new int[8];
        Arrays.fill(moves, -1);
        if (position > 7) {
            if (position > 15) {
                if (position % 8 < 7) addMove(moves, board, position - 15); // Up two right one
                if (position % 8 > 0) addMove(moves, board, position - 17); // Up two left one
            }
            if (position % 8 < 6) addMove(moves, board, position - 6); // Up one right two
            if (position % 8 > 1) addMove(moves, board, position - 10); // Up one left two
        }
        if (position < 56) {
            if (position < 48) {
                if (position % 8 < 7) addMove(moves, board, position + 17); // Down two right one
                if (position % 8 > 0) addMove(moves, board, position + 15); // Down two left one
            }
            if (position % 8 < 6) addMove(moves, board, position + 10); // Down one right two
            if (position % 8 > 1) addMove(moves, board, position + 6); // Down one left two
        }
        return moves;
    }
}
