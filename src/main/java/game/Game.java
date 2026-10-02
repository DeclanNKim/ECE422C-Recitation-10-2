package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

/** Guess-the-picture game: an ASCII image is revealed gradually with each wrong guess. */
public class Game {
    static final String[] IMAGE_FILES = {"pegasus", "house", "fish"};
    static final int MAX_WRONG = 6;

    /** An ASCII picture and the answer the player must guess. */
    static class Picture {
        final String answer;
        final List<String> lines;

        Picture(String answer, List<String> lines) {
            this.answer = answer;
            this.lines = lines;
        }
    }

    static Picture load(String name) throws IOException {
        InputStream in = Game.class.getResourceAsStream("/images/" + name + ".txt");
        if (in == null) {
            throw new IOException("Missing image: " + name);
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String answer = r.readLine().trim();
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = r.readLine()) != null) {
                lines.add(line);
            }
            return new Picture(answer, lines);
        }
    }

    /**
     * Renders the picture with only a portion of its visible characters shown.
     * The reveal order is a fixed shuffle so earlier details stay visible in later stages.
     */
    static String render(Picture p, int stage, int totalStages, long seed) {
        List<int[]> cells = new ArrayList<>();
        for (int y = 0; y < p.lines.size(); y++) {
            String l = p.lines.get(y);
            for (int x = 0; x < l.length(); x++) {
                if (l.charAt(x) != ' ') {
                    cells.add(new int[] {y, x});
                }
            }
        }
        Collections.shuffle(cells, new Random(seed));
        int shown = stage >= totalStages ? cells.size() : cells.size() * stage / totalStages;
        char[][] grid = new char[p.lines.size()][];
        for (int y = 0; y < grid.length; y++) {
            grid[y] = new char[p.lines.get(y).length()];
            java.util.Arrays.fill(grid[y], ' ');
        }
        for (int i = 0; i < shown; i++) {
            int[] c = cells.get(i);
            grid[c[0]][c[1]] = p.lines.get(c[0]).charAt(c[1]);
        }
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            sb.append(new String(row).replaceAll("\\s+$", "")).append('\n');
        }
        return sb.toString();
    }

    static boolean isCorrect(Picture p, String guess) {
        return p.answer.equalsIgnoreCase(guess.trim());
    }

    public static void main(String[] args) throws IOException {
        Random rng = new Random();
        Scanner in = new Scanner(System.in);
        System.out.println("=== Guess the ASCII Picture ===");
        boolean again = true;
        while (again) {
            Picture p = load(IMAGE_FILES[rng.nextInt(IMAGE_FILES.length)]);
            long seed = rng.nextLong();
            int wrong = 0;
            boolean won = false;
            // stage 1 starts with few details; the last stage reveals everything
            int totalStages = MAX_WRONG + 1;
            while (wrong <= MAX_WRONG && !won) {
                System.out.println();
                System.out.print(render(p, wrong + 1, totalStages, seed));
                if (wrong == MAX_WRONG) {
                    break;
                }
                System.out.printf("Wrong guesses: %d/%d. What is it? ", wrong, MAX_WRONG);
                if (!in.hasNextLine()) {
                    return;
                }
                String guess = in.nextLine();
                if (isCorrect(p, guess)) {
                    won = true;
                } else {
                    wrong++;
                    System.out.println("Nope! Here is more detail...");
                }
            }
            System.out.println(won ? "Correct! It was a " + p.answer + "!"
                    : "Out of guesses. It was a " + p.answer + ".");
            System.out.print("Play again? (y/n) ");
            again = in.hasNextLine() && in.nextLine().trim().toLowerCase(Locale.ROOT).startsWith("y");
        }
    }
}
