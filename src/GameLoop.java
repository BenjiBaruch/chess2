import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GameLoop implements MouseListener {
    private ChessPanel panel;
    private int selected = -1;
    private boolean povTurn;
    private ChessBoard board;
    private String boardType = "custom";
    /*
    Board Types:
    "default"
    "custom"
     */
    private final boolean povIsWhite = false;
    private final int playMode = 0;
    /*
    Modes:
    0: Player vs Player
    1: Player vs Player w/ ELO
    2: Player vs Player w/ best move
    3: Player vs AI
    4: Player vs AI w/ elo
    5: AI vs AI w/ best move
     */
    private JFrame window;
    private final int highlightMode = 1;

    public void createWindow() { // With help from https://www.youtube.com/watch?v=om59cwR7psI
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Chess");

        panel = new ChessPanel(povIsWhite, highlightMode);
        panel.addMouseListener(this);
        window.add(panel);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
    public void startLoop() {
        board = new ChessBoard(boardType, povIsWhite);
        povTurn = povIsWhite;
        createWindow();
        panel.setBoard(board.getBoard());
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        // Idk why this is here my project breaks without it
    }

    @Override
    public void mousePressed(MouseEvent e) { // Help from https://www.youtube.com/watch?v=jptf1Wd_omw
        int row = e.getY() / 80;
        int col = e.getX() / 80;
        // System.out.println("Row: " + row + ", Col: " + col + ", Index: " + (row * 8 + col));
        if (row * 8 + col != selected && !board.isNull(row, col) && board.isPov(row, col) == povTurn) {
            // Selecting a piece that belongs to the active player
            // Highlighting the tiles that piece can move to
            selected = (e.getY() / 80) * 8 + (e.getX() / 80);
            int[] highlights;
            if (highlightMode == 1) highlights = board.highlights(selected);
            else if (highlightMode == 2) {
                highlights = SimplifiedChessBoard.getMoveList(SimplifiedChessBoard.chessPieceToIntBoard(board.getBoard(), board.getTurns(), !povIsWhite, 0, povIsWhite), board.getTurns()%2==0);
            }
            // else if (highlightMode == 3) {
            //     highlights = EvaluateBoard.evaluate(SimplifiedChessBoard.chessPieceToInt(board.getBoard(), board.getTurns(), !povIsWhite), true, false);
            // }
            else highlights = new int[0];
            panel.setHighlights(highlights);
        } else if (selected > -1 && (board.isNull(row, col) || board.isPov(row, col) != povTurn) && board.move(selected, row * 8 + col)) {
            // Move selected piece to a new tile
            // Handle exit codes and removes selection
            // Swaps turns
            int exitCode = board.checkWin();
            if (exitCode == 0) {
                // System.out.println("Eval: " + EvaluateBoard.evaluate(fancyBoard, true, true));
                panel.setBoard(board.getBoard());
                selected = -1;
                povTurn = !povTurn;
                // board.print();
                // System.out.println((povTurn ? "White" : "Black") + "'s turn");
                panel.setHighlights(new int[0]);
                if (playMode == 2) System.out.println(Search.ChessBoardSearch(board));
                String fen = SimplifiedChessBoard.intBoardToString(SimplifiedChessBoard.chessPieceToIntBoard(board.getBoard(), board.getTurns(), false, board.getHalfMove(), true));
                String[] fen2 = {fen, "0"};
                System.out.println(SimplifiedChessBoard.fenToSample(fen2, true));
                System.out.println("fen " + fen);
            } else {
                String[] endings = {"Game not over","White win by resignation","White win by checkmate",
                        "White win by timeout","Black win by resignation","Black win by checkmate",
                        "Black win by timeout","Draw by agreement","Draw by stalemate",
                        "Draw by insufficient material","Draw by timeout vs insufficient material"};
                System.out.println(endings[exitCode]);
                window.dispose();
            }
        } else {
            /*
            If:
            - No tile is selected and clicked tile is null
            - Clicked tile is already selected
            - No tile is selected and clicked tile is enemy
            - Selected tile cannot move to clicked tile
            then deselect tile and delete highlights
             */
            selected = -1;
            panel.setHighlights(new int[0]);
        }
        panel.setSelected(selected);
        panel.draw();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
