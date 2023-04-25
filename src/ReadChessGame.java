import javax.swing.*;
import java.io.*;
import java.util.Scanner;

public class ReadChessGame {

    public static ChessPanel createWindow(boolean povIsWhite) { // With help from https://www.youtube.com/watch?v=om59cwR7psI
        /*
        Creates new JFrame window and sets all relevant settings
         */
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Chess");

        ChessPanel panel = new ChessPanel(povIsWhite, 0);
        window.add(panel);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        return panel;
    }

    public static String readLine(BufferedReader gameReader) {
        /*
        Reads one line from the file and handles exceptions
         */
        try {
            return gameReader.readLine();
        } catch (IOException e) {
            System.out.println("IOException");
            return null;
        }
    }


    public static void main(String[] args) { // https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
        /*
        Asks for a txt file path
        Reads the file, splitting it into a series of chess moves
        Sends those moves to the ChessBoard
        Plays through a chess game until an illegal move is made or the game ends
         */

        // Get file path
        System.out.println("Enter file path: ");
        Scanner scan = new Scanner(System.in);
        String file_path = scan.nextLine();
        File gameFile = new File(file_path);

        // Get File
        BufferedReader gameReader;
        try {
            gameReader = new BufferedReader(new FileReader(gameFile));
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
            return;
        }

        // Get POV
        String pov = readLine(gameReader);
        if (pov == null) {
            System.out.println("null pov");
            return;
        }
        boolean povIsWhite = pov.equalsIgnoreCase("white"); // https://stackoverflow.com/questions/1538755/how-to-convert-string-object-to-boolean-object

        // Get rest
        int rest;
        try {
            rest = Integer.valueOf(gameReader.readLine());
        } catch (IOException|NumberFormatException e) { // // https://docs.oracle.com/javase/8/docs/technotes/guides/language/catch-multiple.html
            System.out.println("null rest");
            return;
        }

        // Create board and panel
        ChessBoard board = new ChessBoard("default", povIsWhite);
        ChessPanel panel = createWindow(povIsWhite);

        String[] moveList = new String[500];
        int moves = 0;

        // Splits file into a list of moves
        String line = "";
        while (line != null) {
            line = readLine(gameReader);
            if (line == null) continue;
            String[] terms = line.split(" ");
            for (String term : terms) {
                boolean include = false;
                for (int i = 0; i < term.length(); i++) {
                    char letter = term.charAt(i);
                    if (('A' <= letter && letter <= 'Z') || ('a' <= letter && letter <= 'h')) {
                        include = true;
                        break;
                    }
                }
                if (include) {
                    System.out.print(term + ' ');
                    moveList[moves] = term;
                    moves++;
                }
            }
        }

        // Chill out for a bit
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.out.println("interrupted");
            return;
        }

        // Plays through the game
        for (String move : moveList) {
            if (move == null) break;
            if (!board.moveWithAlgebra(move)) {
                System.out.println("illegal move");
                return;
            }
            panel.setBoard(board.getBoard());
            panel.draw();
            try {
                Thread.sleep(rest);
            } catch (InterruptedException e) {
                System.out.println("interrupted");
                return;
            }
        }

        // Done!
        System.out.println("Done");
    }
}
