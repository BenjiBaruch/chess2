import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class Rook extends ChessPiece {
    private boolean castleable;
    public Rook(boolean povPlayer, int position) {
        super(povPlayer, position);
        castleable = true;
    }

    public Rook(boolean povPlayer, int position, boolean castleable) {
        super(povPlayer, position);
        this.castleable = castleable;
    }
    public char toChar(boolean povIsWhite) {
        if (povIsWhite == povPlayer) {
            return (char)9814; // 0x2656 represents white rook char
        } else {
            return (char)9820; // 0x265C represents black rook char
        }
    }
    public int toInt() {
        return 2; // 2 represents rooks
    }
    public BufferedImage getSprite(boolean povIsWhite) { // Help from https://youtu.be/wT9uNGzMEM4
        try {
            if (povIsWhite == povPlayer)
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/rook.png"));
            else
                return ImageIO.read(getClass().getClassLoader().getResourceAsStream("ChessSprites/rook1.png"));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ChessPiece[][] move(ChessPiece[][] board, int p2, int turns) {
        castleable = false;
        return super.move(board, p2, turns);
    }

    public boolean getCastleable() {
        return castleable;
    }

    public int[] moveMap(ChessPiece[][] board, int turns) {
        /*
        Generates a list of all tiles that this rook can move to.
        Does not cull moves that result in check.
         */
        int[] moves = new int[14];
        Arrays.fill(moves, -1);
        shootRay(moves, board, 1, 0); // All moves to the right
        shootRay(moves, board, -1, 0); // All moves to the left
        shootRay(moves, board, 0, 1); // All moves up
        shootRay(moves, board, 0, -1); // All moves down
        return moves;
    }
}
