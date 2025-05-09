import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;

public class ThreePrisonersDilemmaExperimentDefect {
    static int[][][] payoff = { { { 6, 3 }, // payoffs when first and second players cooperate
            { 3, 0 } }, // payoffs when first player coops, second defects
            { { 8, 5 }, // payoffs when first player defects, second coops
                    { 5, 2 } } };// payoffs when first and second players defect

    abstract class Player {
        // This procedure takes in the number of rounds elapsed so far (n), and
        // the previous plays in the match, and returns the appropriate action.
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            throw new RuntimeException("You need to override the selectAction method.");
        }

        // Used to extract the name of this player class.
        String name() {
            String result = getClass().getName();
            return result.substring(result.indexOf('$') + 1);
        }
    }

    class NicePlayer extends Player {
        // NicePlayer always cooperates
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 0;
        }
    }

    class NastyPlayer extends Player {
        // NastyPlayer always defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 1;
        }
    }

    class RandomPlayer extends Player {
        // RandomPlayer randomly picks his action each time
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (Math.random() < 0.5)
                return 0; // cooperates half the time
            else
                return 1; // defects half the time
        }
    }

    class TolerantPlayer extends Player {
        // TolerantPlayer looks at his opponents' histories, and only defects
        // if at least half of the other players' actions have been defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            int opponentCoop = 0;
            int opponentDefect = 0;
            for (int i = 0; i < n; i++) {
                if (oppHistory1[i] == 0)
                    opponentCoop = opponentCoop + 1;
                else
                    opponentDefect = opponentDefect + 1;
            }
            for (int i = 0; i < n; i++) {
                if (oppHistory2[i] == 0)
                    opponentCoop = opponentCoop + 1;
                else
                    opponentDefect = opponentDefect + 1;
            }
            if (opponentDefect > opponentCoop)
                return 1;
            else
                return 0;
        }
    }

    class FreakyPlayer extends Player {
        // FreakyPlayer determines, at the start of the match,
        // either to always be nice or always be nasty.
        // Note that this class has a non-trivial constructor.
        int action;

        FreakyPlayer() {
            if (Math.random() < 0.5)
                action = 0; // cooperates half the time
            else
                action = 1; // defects half the time
        }

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return action;
        }
    }

    class T4TPlayer extends Player {
        // Picks a random opponent at each play,
        // and uses the 'tit-for-tat' strategy against them
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 0; // cooperate by default
            if (Math.random() < 0.5)
                return oppHistory1[n - 1];
            else
                return oppHistory2[n - 1];
        }
    }

    /* Implementation of different strategies by Lian Hong Shen Jordan */

    class SuspiciousT4TPlayer extends Player {
        // Impelments the Suspicious Tit-for-tat strategy
        // Picks a random opponent at each play,
        // and uses the 'tit-for-tat' strategy against them
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 1; // Defect on first round
            if (Math.random() < 0.5)
                return oppHistory1[n - 1];
            else
                return oppHistory2[n - 1];
        }
    }

    class StandardT4TPlayer extends Player {
        // Implements the Standard Tit-for-tat strategy
        // Does not pick a random opponent at each play, considers both opponents
        // defects if either of them defected in the previous round
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 0; // Cooperate on first round

            // Defect if any opponent defected in the previous round
            if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) {
                return 1;
            }

            return 0; // Otherwise cooperate
        }
    }

    class SuspiciousStandardT4TPlayer extends Player {
        // Implements the Suspicious Standard Tit-for-tat strategy
        // Does not pick a random opponent at each play, considers both opponents
        // defects if either of them defected in the previous round
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 1; // Defect on first round

            // Defect if any opponent defected in the previous round
            if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) {
                return 1;
            }

            return 0; // Otherwise cooperate
        }
    }

    class GenerousT4TPlayer extends Player {
        // Implements the Generous Tit-for-tat strategy
        // Similar to Standard T4T but with
        // min{1− (T-R)/(R-S) , (R-P)/(T-P)} probability of forgiving defections
        // R (Reward) = 6 (payoff[0][0][0])
        // P (Punishment) = 2 (payoff[1][1][1])
        // T (Temptation) = 8 (payoff[1][0][0])
        // S (Sucker) = 0 (payoff[0][1][1])
        // Math.min(1 - (payoff[1][0][0] - payoff[0][0][0]) / (payoff[0][0][0] -
        // payoff[0][1][1]),(payoff[0][0][0] - payoff[1][1][1]) / (payoff[1][0][0] -
        // payoff[1][1][1])) = min{2/3, 2/3} = 2/3
        double gCooperate = 2 / 3;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 0; // Cooperate on first round

            // If any opponent defected in the previous round
            if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) {
                // 2/3 chance to forgive and cooperate anyway
                if (Math.random() < gCooperate) {
                    return 0;
                } else {
                    return 1; // Defect to retaliate
                }
            }

            return 0; // Both opponents cooperated, so cooperate
        }
    }

    class JossPlayer extends Player {
        // Implements the Joss strategy
        // Similar to Standard T4T but once in a while, defects randomly
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 0; // Cooperate on first round

            // If any opponent defected, defect
            if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) {
                return 1;
            }

            // 10% chance to defect even when opponents cooperated
            if (Math.random() < 0.1) {
                return 1;
            }

            return 0; // Otherwise cooperate
        }
    }

    class StandardT42TPlayer extends Player {
        // Implements the Tit-for-2-Tat strategy
        // Similar to Standard T4T,
        // but only defects if oppponent defected in the last two round
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n < 2)
                return 0; // Cooperate on first two rounds

            // Check if either opponent defected in both of the last two rounds
            if ((oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1)
                    || (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1)) {
                return 1;
            }

            return 0; // Otherwise cooperate
        }
    }

    class StandardT43TPlayer extends Player {
        // Implements the Tit-for-3-Tat strategy
        // Similar to Standard T42T,
        // but only defects if oppponent defected in the last three round
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n < 3)
                return 0; // Cooperate on first three rounds

            // Check if either opponent defected in both of the last three rounds
            if ((oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1 && oppHistory1[n - 3] == 1)
                    || (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1 && oppHistory2[n - 3] == 1)) {
                return 1;
            }

            return 0; // Otherwise cooperate
        }
    }

    class StandardT44TPlayer extends Player {
        // Implements the Tit-for-4-Tat strategy
        // Similar to Standard T42T,
        // but only defects if oppponent defected in the last four round
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n < 4)
                return 0; // Cooperate on first four rounds

            // Check if either opponent defected in both of the last four rounds
            if ((oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1 && oppHistory1[n - 3] == 1
                    && oppHistory1[n - 4] == 1)
                    || (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1 && oppHistory2[n - 3] == 1
                            && oppHistory2[n - 4] == 1)) {
                return 1;
            }

            return 0; // Otherwise cooperate
        }
    }

    class TesterPlayer extends Player {
        // Implements the Tester strategy
        // Tests opponents by defecting on the first round
        // if opponent defects, switch to Standard T4T
        // if opponent cooperates, exploit by mixing cooperation and defection
        // exploit every 5 rounds
        boolean retaliationDetected = false;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 1; // Defect on first round

            // Check if any opponent retaliated to our initial defection
            if (n == 1 && (oppHistory1[0] == 1 || oppHistory2[0] == 1)) {
                retaliationDetected = true;
            }

            if (retaliationDetected) {
                // Use standard Tit-for-Tat
                if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                // No retaliation detected, periodically defect
                if (n % 5 == 0) { // Defect every 5 rounds
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }

    class PavlovPlayer extends Player {
        // Implements the Pavlov Strategy
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 0; // Cooperate on first round

            // Get previous payoff
            int lastPayoff = ThreePrisonersDilemmaExperimentDefect.payoff[myHistory[n - 1]][oppHistory1[n - 1]][oppHistory2[n - 1]];

            // If payoff was good (≥ 5), stick with previous move
            if (lastPayoff >= 5) {
                return myHistory[n - 1];
            } else {
                // Otherwise change move
                return 1 - myHistory[n - 1];
            }
        }
    }

    class TriggerPlayer extends Player {
        // Implements the Trigger/Grim/Friedman strategy
        // This strategy cooperates until any opponent defects, then defects forever
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 0; // Cooperate on first round

            // Check if any opponent has ever defected
            for (int i = 0; i < n; i++) {
                if (oppHistory1[i] == 1 || oppHistory2[i] == 1) {
                    return 1; // Defect forever if betrayed
                }
            }

            return 0; // Otherwise cooperate
        }
    }

    class EndGameDefactorPlayer extends Player {
        // Implements the End Game Defector strategy
        // This strategy cooperates with Standard TFT
        // until the last 10 rounds, then defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0)
                return 0; // Cooperate on first round

            // Defect in the last ~10 rounds (we know there are ~90-110 rounds)
            if (n >= 100) {
                return 1;
            }

            // Use Tit-for-Tat strategy before the end
            if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) {
                return 1;
            }

            return 0; // Otherwise cooperate
        }
    }

    class NamedNastyPlayer extends Player {
        private final String customName;

        // Constructor that takes a custom name
        public NamedNastyPlayer(String name) {
            this.customName = name;
        }

        // Override the name() method to return the custom name
        @Override
        String name() {
            return customName;
        }

        // NastyPlayer always defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 1;
        }
    }

    /*
     * In our tournament, each pair of strategies will play one match against each
     * other. This procedure simulates a single match and returns the scores.
     */
    float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
        int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
        float ScoreA = 0, ScoreB = 0, ScoreC = 0;

        for (int i = 0; i < rounds; i++) {
            int PlayA = A.selectAction(i, HistoryA, HistoryB, HistoryC);
            int PlayB = B.selectAction(i, HistoryB, HistoryC, HistoryA);
            int PlayC = C.selectAction(i, HistoryC, HistoryA, HistoryB);
            ScoreA = ScoreA + payoff[PlayA][PlayB][PlayC];
            ScoreB = ScoreB + payoff[PlayB][PlayC][PlayA];
            ScoreC = ScoreC + payoff[PlayC][PlayA][PlayB];
            HistoryA = extendIntArray(HistoryA, PlayA);
            HistoryB = extendIntArray(HistoryB, PlayB);
            HistoryC = extendIntArray(HistoryC, PlayC);
        }
        float[] result = { ScoreA / rounds, ScoreB / rounds, ScoreC / rounds };
        return result;
    }

    // This is a helper function needed by scoresOfMatch.
    int[] extendIntArray(int[] arr, int next) {
        int[] result = new int[arr.length + 1];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        result[result.length - 1] = next;
        return result;
    }

    /*
     * The procedure makePlayer is used to reset each of the Players (strategies) in
     * between matches. When you add your own strategy, you will need to add a new
     * entry to makePlayer, and change numPlayers.
     */

    int numPlayers = 18;

    Player makePlayer(int which) {
        switch (which) {
        case 0:
            return new NamedNastyPlayer("NastyPlayer1");
        case 1:
            return new NamedNastyPlayer("NastyPlayer2");
        case 2:
            return new NamedNastyPlayer("NastyPlayer3");
        case 3:
            return new NamedNastyPlayer("NastyPlayer4");
        case 4:
            return new NamedNastyPlayer("NastyPlayer5");
        case 5:
            return new NamedNastyPlayer("NastyPlayer6");
        case 6:
            return new NamedNastyPlayer("NastyPlayer7");
        case 7:
            return new NamedNastyPlayer("NastyPlayer8");
        case 8:
            return new NamedNastyPlayer("NastyPlayer9");
        case 9:
            return new NamedNastyPlayer("NastyPlayer10");
        case 10:
            return new StandardT4TPlayer();
        case 11:
            return new StandardT42TPlayer();
        case 12:
            return new StandardT43TPlayer();
        case 13:
            return new StandardT44TPlayer();
        case 14:
            return new NamedNastyPlayer("NastyPlayer11");
        case 15:
            return new NamedNastyPlayer("NastyPlayer12");
        case 16:
            return new NamedNastyPlayer("NastyPlayer13");
        case 17:
            return new NamedNastyPlayer("NastyPlayer14");
        }

        throw new RuntimeException("Bad argument passed to makePlayer");
    }

    // Run tournament and log results
    void runTournament(int experimentNum, PrintWriter logWriter, Map<String, List<Float>> scoresHistory) {
        float[] totalScore = new float[numPlayers];

        logWriter.println("Experiment #" + experimentNum);
        logWriter.println("------------------------------");

        // This loop plays each triple of players against each other
        for (int i = 0; i < numPlayers; i++) {
            for (int j = i; j < numPlayers; j++) {
                for (int k = j; k < numPlayers; k++) {
                    Player A = makePlayer(i);
                    Player B = makePlayer(j);
                    Player C = makePlayer(k);
                    int rounds = 90 + (int) Math.rint(20 * Math.random()); // Between 90 and 110 rounds
                    float[] matchResults = scoresOfMatch(A, B, C, rounds); // Run match
                    totalScore[i] = totalScore[i] + matchResults[0];
                    totalScore[j] = totalScore[j] + matchResults[1];
                    totalScore[k] = totalScore[k] + matchResults[2];

                    logWriter.println(A.name() + " scored " + String.format("%.2f", matchResults[0]) + " points, "
                            + B.name() + " scored " + String.format("%.2f", matchResults[1]) + " points, and "
                            + C.name() + " scored " + String.format("%.2f", matchResults[2]) + " points. (Rounds: "
                            + rounds + ")");
                }
            }
        }

        int[] sortedOrder = new int[numPlayers];
        // Sort players by score
        for (int i = 0; i < numPlayers; i++) {
            int j = i - 1;
            for (; j >= 0; j--) {
                if (totalScore[i] > totalScore[sortedOrder[j]])
                    sortedOrder[j + 1] = sortedOrder[j];
                else
                    break;
            }
            sortedOrder[j + 1] = i;
        }

        // Print sorted results
        logWriter.println("\nTournament Results for Experiment #" + experimentNum);
        logWriter.println("------------------------------");
        for (int i = 0; i < numPlayers; i++) {
            Player p = makePlayer(sortedOrder[i]);
            String name = p.name();
            float score = totalScore[sortedOrder[i]];

            logWriter.println(name + ": " + String.format("%.2f", score) + " points.");

            // Store score for plotting later
            if (!scoresHistory.containsKey(name)) {
                scoresHistory.put(name, new ArrayList<>());
            }
            scoresHistory.get(name).add(score);
        }
        logWriter.println("\n");
    }

    void createConsolidatedTable(Map<String, List<Float>> scoresHistory, String outputFilename) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(outputFilename));

        // Calculate average scores for each strategy
        Map<String, Float> averageScores = new HashMap<>();
        for (Map.Entry<String, List<Float>> entry : scoresHistory.entrySet()) {
            String strategy = entry.getKey();
            List<Float> scores = entry.getValue();

            // Calculate sum for average
            float sum = 0;
            for (Float score : scores) {
                sum += score;
            }

            // Calculate average
            float average = sum / scores.size();
            averageScores.put(strategy, average);
        }

        // Sort strategies by average score (descending)
        List<Map.Entry<String, Float>> sortedEntries = new ArrayList<>(averageScores.entrySet());
        sortedEntries.sort((e1, e2) -> Float.compare(e2.getValue(), e1.getValue())); // Descending order

        // Write the header row with experiment numbers and average
        writer.print("Strategy");
        for (int i = 1; i <= 50; i++) {
            writer.print(",Exp" + i);
        }
        writer.println(",Average");

        // Write each strategy's scores in descending order of average score
        for (Map.Entry<String, Float> sortedEntry : sortedEntries) {
            String strategy = sortedEntry.getKey();
            List<Float> scores = scoresHistory.get(strategy);
            float average = sortedEntry.getValue();

            writer.print(strategy);

            // Write each experiment's score
            for (Float score : scores) {
                writer.print("," + String.format("%.2f", score));
            }

            // Write the average
            writer.println("," + String.format("%.2f", average));
        }

        writer.close();
        System.out.println("Consolidated score table saved to: " + outputFilename);
    }

    static Color[] generateDistinctColors(int count) {
        Color[] colors = new Color[count];

        // Use HSB (Hue, Saturation, Brightness) color model for even distribution
        // around the color wheel
        float hueStep = 1.0f / count;

        for (int i = 0; i < count; i++) {
            // Distribute hues evenly around the color wheel
            float hue = i * hueStep;

            // Alternate saturation and brightness for adjacent colors for better
            // distinction
            float saturation = 0.8f + (i % 2) * 0.2f; // Between 0.8 and 1.0
            float brightness = 0.7f + ((i / 2) % 2) * 0.3f; // Between 0.7 and 1.0

            colors[i] = Color.getHSBColor(hue, saturation, brightness);
        }

        // Add a shuffle step to ensure colors that are next to each other in the array
        // are not too similar in the visual spectrum
        for (int i = 0; i < count; i++) {
            int swapIndex = (i + count / 2) % count;
            Color temp = colors[i];
            colors[i] = colors[swapIndex];
            colors[swapIndex] = temp;
        }

        return colors;
    }

    Map<String, Color> generateStrategyColorMap(Set<String> strategyNames) {
        Map<String, Color> colorMap = new HashMap<>();
        Color[] colors = generateDistinctColors(strategyNames.size());

        int i = 0;
        for (String strategy : strategyNames) {
            colorMap.put(strategy, colors[i++]);
        }

        return colorMap;
    }

    // Create a simple plot of scores across experiments
    void createScorePlot(Map<String, List<Float>> scoresHistory, Map<String, Color> strategyColorMap,
            String outputFilename) throws IOException {
        // Define plot dimensions
        int width = 800;
        int height = 600;
        int padding = 60;

        // Create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Find max score for scaling
        float maxScore = 0;
        for (List<Float> scores : scoresHistory.values()) {
            for (Float score : scores) {
                if (score > maxScore)
                    maxScore = score;
            }
        }

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(padding, height - padding, width - padding, height - padding); // x-axis
        g2d.drawLine(padding, height - padding, padding, padding); // y-axis

        // Draw axis labels
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Experiment Number", width / 2 - 50, height - 20);
        g2d.rotate(-Math.PI / 2, 20, height / 2);
        g2d.drawString("Score", 20, height / 2);
        g2d.rotate(Math.PI / 2, 20, height / 2);

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Strategy Scores Across Experiments", width / 2 - 150, 30);

        // Draw data lines
        int xStep = (width - 2 * padding) / (scoresHistory.get(scoresHistory.keySet().iterator().next()).size() - 1);

        // Draw a legend in the bottom left corner, ensuring it doesn't overlap with
        // data
        int legendColumns = 3; // Split legend into multiple columns for better space usage
        int entriesPerColumn = (int) Math.ceil(scoresHistory.size() / (double) legendColumns);
        int legendEntryHeight = 20;
        int legendColumnWidth = 200;

        // Calculate dimensions
        int legendWidth = legendColumns * legendColumnWidth;
        int legendHeight = entriesPerColumn * legendEntryHeight + 10;

        // Position at bottom left with padding
        int legendX = padding;
        int legendY = height - padding - legendHeight;

        // Draw legend background
        g2d.setColor(new Color(255, 255, 255, 230)); // More opaque background
        g2d.fillRect(legendX, legendY, legendWidth, legendHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendX, legendY, legendWidth, legendHeight);

        // Draw legend entries in multiple columns
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int entryCounter = 0;
        int currentColumn = 0;

        for (Map.Entry<String, List<Float>> entry : scoresHistory.entrySet()) {
            String name = entry.getKey();
            List<Float> scores = entry.getValue();

            // Calculate position in the legend grid
            int currentX = legendX + currentColumn * legendColumnWidth + 10;
            int currentY = legendY + (entryCounter % entriesPerColumn) * legendEntryHeight + 20;

            g2d.setColor(strategyColorMap.get(name));

            // Draw the legend entry
            g2d.drawLine(currentX, currentY - 5, currentX + 30, currentY - 5);
            g2d.drawString(name, currentX + 40, currentY);

            // Increment counters
            entryCounter++;
            if (entryCounter % entriesPerColumn == 0) {
                currentColumn++;
            }

            // Draw the line graph
            for (int i = 0; i < scores.size() - 1; i++) {
                int x1 = padding + i * xStep;
                int y1 = height - padding - (int) (scores.get(i) / maxScore * (height - 2 * padding));
                int x2 = padding + (i + 1) * xStep;
                int y2 = height - padding - (int) (scores.get(i + 1) / maxScore * (height - 2 * padding));

                g2d.drawLine(x1, y1, x2, y2);

                // Mark points
                g2d.fillOval(x1 - 3, y1 - 3, 6, 6);
                if (i == scores.size() - 2) {
                    g2d.fillOval(x2 - 3, y2 - 3, 6, 6);
                }
            }
        }

        // Draw scale on y-axis
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = 0; i <= 10; i++) {
            int y = height - padding - i * (height - 2 * padding) / 10;
            float scaleValue = i * maxScore / 10;
            g2d.drawLine(padding - 5, y, padding, y);
            g2d.drawString(String.format("%.1f", scaleValue), padding - 40, y + 5);
        }

        // Draw scale on x-axis
        int experimentCount = scoresHistory.get(scoresHistory.keySet().iterator().next()).size();
        for (int i = 0; i < experimentCount; i++) {
            int x = padding + i * xStep;
            g2d.drawLine(x, height - padding, x, height - padding + 5);
            g2d.drawString(String.valueOf(i + 1), x - 5, height - padding + 20);
        }

        g2d.dispose();

        // Save the image
        File outputFile = new File(outputFilename);
        ImageIO.write(image, "png", outputFile);
        System.out.println("Score plot saved to: " + outputFilename);
    }

    void createAverageScoreBarChart(Map<String, List<Float>> scoresHistory, Map<String, Color> strategyColorMap,
            String outputFilename) throws IOException {
        // Calculate averages first
        Map<String, Float> averageScores = new HashMap<>();
        for (Map.Entry<String, List<Float>> entry : scoresHistory.entrySet()) {
            String strategy = entry.getKey();
            List<Float> scores = entry.getValue();

            // Calculate average
            float sum = 0;
            for (Float score : scores) {
                sum += score;
            }
            float average = sum / scores.size();
            averageScores.put(strategy, average);
        }

        // Sort strategies by average score (descending)
        List<Map.Entry<String, Float>> sortedEntries = new ArrayList<>(averageScores.entrySet());
        sortedEntries.sort((e1, e2) -> Float.compare(e2.getValue(), e1.getValue())); // Descending order

        // Define chart dimensions
        int width = 1000;
        int height = 600;
        int padding = 100; // Increased padding for labels

        // Create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Find max score for scaling
        float maxScore = 0;
        for (Map.Entry<String, Float> entry : sortedEntries) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
            }
        }

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(padding, height - padding, width - padding, height - padding); // x-axis
        g2d.drawLine(padding, height - padding, padding, padding); // y-axis

        // Draw axis labels
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Strategy", width / 2 - 30, height - 40);
        g2d.rotate(-Math.PI / 2, 40, height / 2);
        g2d.drawString("Average Score", 40, height / 2);
        g2d.rotate(Math.PI / 2, 40, height / 2);

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Average Strategy Scores After 50 Experiments", width / 2 - 200, 40);

        // Calculate bar width based on available space and number of strategies
        int availableWidth = width - 2 * padding;
        int barWidth = (int) (availableWidth * 0.8 / sortedEntries.size());
        int barSpacing = (int) (availableWidth * 0.2 / (sortedEntries.size() + 1));

        // Draw bars and labels
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        for (int i = 0; i < sortedEntries.size(); i++) {
            Map.Entry<String, Float> entry = sortedEntries.get(i);
            String strategy = entry.getKey();
            float score = entry.getValue();

            // Calculate bar dimensions
            int barX = padding + barSpacing * (i + 1) + barWidth * i;
            int barHeight = (int) (score / maxScore * (height - 2 * padding));
            int barY = height - padding - barHeight;

            // Draw the bar
            g2d.setColor(strategyColorMap.get(strategy));
            g2d.fillRect(barX, barY, barWidth, barHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(barX, barY, barWidth, barHeight);

            // Draw score value on top of bar
            g2d.drawString(String.format("%.2f", score), barX + barWidth / 2 - 15, barY - 5);

            // Draw strategy name below bar (rotated for better readability if many
            // strategies)
            if (sortedEntries.size() > 10) {
                // For many strategies, rotate the labels
                g2d.rotate(-Math.PI / 4, barX + barWidth / 2, height - padding + 15);
                g2d.drawString(strategy, barX + barWidth / 2 - strategy.length() * 3, height - padding + 15);
                g2d.rotate(Math.PI / 4, barX + barWidth / 2, height - padding + 15);
            } else {
                // For fewer strategies, regular horizontal labels
                g2d.drawString(strategy, barX + barWidth / 2 - strategy.length() * 3, height - padding + 15);
            }
        }

        // Draw scale on y-axis
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = 0; i <= 10; i++) {
            int y = height - padding - i * (height - 2 * padding) / 10;
            float scaleValue = i * maxScore / 10;
            g2d.drawLine(padding - 5, y, padding, y);
            g2d.drawString(String.format("%.1f", scaleValue), padding - 40, y + 5);
        }

        g2d.dispose();

        // Save the image
        File outputFile = new File(outputFilename);
        ImageIO.write(image, "png", outputFile);
        System.out.println("Average score bar chart saved to: " + outputFilename);
    }

    public static void main(String[] args) {
        // Create a directory for output files
        File outputDir = new File("experimentdefect_results");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        try {
            // Create log file for tournament results
            PrintWriter logWriter = new PrintWriter(new FileWriter("experimentdefect_results/tournament_log.txt"));

            // Map to store scores for each player across experiments
            Map<String, List<Float>> scoresHistory = new HashMap<>();

            // Create an instance of the experiment
            ThreePrisonersDilemmaExperimentDefect instance = new ThreePrisonersDilemmaExperimentDefect();

            // Run 50 experiments
            for (int i = 1; i <= 50; i++) {
                System.out.println("Running experiment " + i + " of 50...");
                instance.runTournament(i, logWriter, scoresHistory);
            }

            // Close the log file
            logWriter.close();
            System.out.println("Log file saved to: experimentdefect_results/tournament_log.txt");

            // Create consolidated table with all scores and averages
            instance.createConsolidatedTable(scoresHistory, "experimentdefect_results/consolidated_scores.csv");

            // Generate a consistent color mapping for all strategies
            Map<String, Color> strategyColorMap = instance.generateStrategyColorMap(scoresHistory.keySet());

            // Create a plot of scores across experiments using the color map
            instance.createScorePlot(scoresHistory, strategyColorMap, "experimentdefect_results/strategy_scores.png");

            // Create average score bar chart using the same color map
            instance.createAverageScoreBarChart(scoresHistory, strategyColorMap,
                    "experimentdefect_results/average_scores_bar_chart.png");

        } catch (IOException e) {
            System.err.println("Error writing output files: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
