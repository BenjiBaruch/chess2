import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Puzzles {
    private static final List<List<String>> puzzleSet = getPuzzleArray();
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

    private static List<String> findPuzzle(String criterion, int arg) {
        if (criterion.equalsIgnoreCase("mate in n")) {
            for (List<String> puzzle : puzzleSet) {
                arg++;
            }
        }
        return null;
    }
    public static void main(String[] args) {
        for (int i = 0; i < 100; i++)
            System.out.println(puzzleSet.get(i));
    }
}

// frick you
