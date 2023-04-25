import javax.swing.*;
import java.util.Scanner;

public class DisplayChessBoard {
    private static ChessPanel createWindow() { // With help from https://www.youtube.com/watch?v=om59cwR7psI
        /*
        Creates new JFrame window and sets all relevant settings
         */
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Chess");

        ChessPanel panel = new ChessPanel(true, 0);
        window.add(panel);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        return panel;
    }
    public static void main(String[] args) {
        System.out.println("Enter board code\n");
        Scanner scan = new Scanner(System.in);
        String str = scan.nextLine();
        System.out.println(str);
        int[] board = SimplifiedChessBoard.stringToIntBoard(str, true);
        board = SimplifiedChessBoard.flipBoard(board);
        ChessPiece[][] wackyBoard = SimplifiedChessBoard.intToChessPieceBoard(board, true);
        ChessPanel panel = createWindow();
        panel.setBoard(wackyBoard);
        panel.draw();

        ChessBoard cB = new ChessBoard();
        cB.setBoard(wackyBoard);
        int len = 0;
        for (int i = 0; i < 64; i++)
            if (!cB.isNull(i/8, i%8) && cB.isPov(i/8, i%8)) {
                int[] highlights = cB.highlights(i);
                for (int highlight : highlights)
                    if (highlight > -1)
                        len++;
            }
        System.out.println("Move list len: " + len);
    }
}
