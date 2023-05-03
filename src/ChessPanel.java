import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

// With help from RyiSnow's Youtube Tutorial
// https://www.youtube.com/watch?v=om59cwR7psI

public class ChessPanel extends JPanel {
        ChessPiece[][] board;
        private int selected = -1;
        private int[] highlights;
        private final boolean povIsWhite;
        /*
        Highlight modes:
        0: No highlights
        1: Possible moves
        2: Move weights
        3: Eval
         */
        private final int highlightMode;



        public ChessPanel(boolean povIsWhite, int highlightMode) {
                this.povIsWhite = povIsWhite;
                this.highlights = new int[0];
                this.setPreferredSize(new Dimension(640, 640));
                this.setBackground(new Color(237, 227, 199));
                this.setDoubleBuffered(true);
                board = new ChessBoard("default", povIsWhite).getBoard();
                this.highlightMode = highlightMode;
        }

        public ChessPanel(ChessPiece[][] initialBoard) {
                this.povIsWhite = true;
                this.highlights = new int[0];
                this.setPreferredSize(new Dimension(640, 640));
                this.setBackground(new Color(237, 227, 199));
                this.setDoubleBuffered(true);
                this.board = initialBoard;
                this.highlightMode = 1;
        }

        public void draw() {
                repaint();
        }

        public void setBoard(ChessPiece[][] board) {
                this.board = board;
        }

        public void setSelected(int selected) {
                this.selected = selected;
        }

        public void setHighlights(int[] highlights) {
                this.highlights = highlights;
        }

        public void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(122, 93, 17));
                for (int row = 0; row < 8; row++) {
                        for (int col = 0; col < 8; col++) {
                                if (row * 8 + col == selected) {
                                        // Paint selected tile teal
                                        g2.setColor(new Color(0, 196, 190));
                                        g2.fillRect(col * 80, row * 80, 80, 80);
                                        g2.setColor(new Color(122, 93, 17));
                                } else if ((row + col) % 2 == 1) {
                                        // Paint unselected dark tile in brown
                                        g2.fillRect(col * 80, row * 80, 80, 80);
                                }
                                g2.drawString("" + (char)(col+'A') + row, col*80-35, row*80-35);
                                if (board[row][col] != null) {
                                        // Draw piece on board
                                        ChessPiece piece = board[row][col];
                                        g2.drawImage(piece.getSprite(povIsWhite), col * 80 + 20, row * 80 + 20, null);
                                }
                        }
                }
                g2.setColor(new Color(190, 205, 230, 192));
                g2.setStroke(new BasicStroke(3));
                if (highlightMode == 1) for (int tile : highlights) {
                        if (tile == -1) continue;
                        if (board[tile/8][tile%8] == null) {
                                // Highlight on empty square
                                g2.fillOval(tile % 8 * 80 + 20, tile / 8 * 80 + 20, 40, 40);
                        } else {
                                // Highlight on capturable square
                                g2.drawOval(tile % 8 * 80 + 5, tile / 8 * 80 + 5, 70, 70);
                        }
                }
                else if (highlightMode == 2) {
                        System.out.println("Moves: " + Arrays.toString(highlights));
                        g2.setColor(Color.black);
                        g2.setFont(new Font("default", Font.BOLD, 16));
                        for (int tile : highlights) {
                                if (tile == -1) continue;
                                int weight = tile / 4096;
                                int pos1 = (tile / 64) % 64;
                                int pos2 = tile % 64;
                                /* System.out.println("Move: " + tile + "\nWeight: " + weight +
                                        "\npos1: " + pos1 + "\npos2: " + pos2);
                                g2.drawOval(tile % 8 * 80 + 5, tile / 8 * 80 + 5, 70, 70);
                                g2.drawString(Integer.toString(weight), pos2 % 8 * 80 + 20, pos2 / 8 * 80 + 20);
                                */
                                g2.setStroke(new BasicStroke(Math.max(1, (int)Math.pow(5-(Math.abs(pos1/8-pos2/8)+Math.abs(pos1%8-pos2%8)), 2))));
                                g2.drawLine(pos1 % 8 * 80 + 40, pos1 / 8 * 80 + 40, pos2 % 8 * 80 + 40, pos2 / 8 * 80 + 40);
                        }

                }
                else if (highlightMode == 3) {
                        if (highlights.length < 64) return;
                        System.out.println("EvalTable: " + Arrays.toString(highlights));
                        g2.setColor(Color.black);
                        g2.setFont(new Font("default", Font.BOLD, 16));
                        for (int i = 0; i < 64; i++) {
                                if (board[i/8][i%8] == null) continue;
                                g2.drawString(Integer.toString(highlights[i]), i % 8 * 80 + 20, i / 8 * 80 + 20);
                        }
                }
                g2.dispose();
        }
}