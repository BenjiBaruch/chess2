import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Puzzles {
    private static final List<List<String>> puzzleSet = getPuzzleArray();
    /*
    Puzzle criteria:

    [crushing, hangingPiece, long, middlegame, advantage, endgame, short, pawnEndgame, mate, mateIn2,
    master, fork, trappedPiece, pin, backRankMate, discoveredAttack, sacrifice, bodenMate, mateIn1,
    oneMove, deflection, kingsideAttack, advancedPawn, attraction, promotion, masterVsMaster, skewer,
    superGM, opening, defensiveMove, rookEndgame, veryLong, exposedKing, mateIn3, clearance, quietMove,
    equality, knightEndgame, queensideAttack, hookMate, intermezzo, bishopEndgame, xRayAttack,
    capturingDefender, attackingF2F7, doubleCheck, mateIn4, zugzwang, queenEndgame, queenRookEndgame,
    interference, arabianMate, dovetailMate, smotheredMate, anastasiaMate, enPassant, castling,
    underPromotion, mateIn5, doubleBishopMate]

    Code for generating criteria:

    ArrayList<String> criteria = new ArrayList<>(10);
        for (int i = 0; i < puzzleSet.size(); i++)
            for (String criterion : puzzleSet.get(i).get(7).split(" "))
                if (!criteria.contains(criterion))
                    criteria.add(criterion);
        System.out.println(criteria);
     */
    public static List<List<String>> getPuzzleArray() {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("data/lichess_db_puzzle.csv"))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (i++ % 10000 == 0) System.out.print("i");
                if (i % 1000000 == 999999) break;
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        } catch (FileNotFoundException e) {
            System.out.println("frick 1");
        } catch (IOException e) {
            System.out.println("frick 2");
        }
        return records;
    }

    private static ArrayList<List<String>> findPuzzles(String criterion, int count) {
        ArrayList<List<String>> puzzles = new ArrayList<List<String>>(count);
        for (List<String> puzzle : puzzleSet)
            if (puzzles.size() >= count) return puzzles;
            else if (Arrays.asList(puzzle.get(7).split(" ")).contains(criterion))
                puzzles.add(puzzle);
        return puzzles.size() > 0 ? puzzles : null;
    }
    public static void main(String[] args) {
        /*
        Sample:
        [0000D, 5rk1/1p3ppp/pq3b2/8/8/1P1Q1N2/P4PPP/3R2K1 w - - 2 27, d3d6 f8d8 d6d8 f6d8,
        1513, 74, 96, 16164, advantage endgame short, https://lichess.org/F8M8OS71#53]

        Format: [ID, board (FEN), ideal moves, ?, ?, ?, ?, criteria, link]

        Mate in 1: Q5k1/p1p3p1/5rP1/8/3P4/7P/q3r3/B4RK1 b - - 1 34  -=-  f6f8 a8f8
         */
        ArrayList<List<String>> mateInOnePuzzles = findPuzzles("mateIn1", 10);
        for (List<String> puzzle : mateInOnePuzzles)
            System.out.println(puzzle.get(1) + "  -=-  " + puzzle.get(2) + '\n');
    }
}
