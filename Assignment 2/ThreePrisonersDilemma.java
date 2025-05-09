public class ThreePrisonersDilemma {

    /*
     * This Java program models the two-player Prisoner's Dilemma game. We use the
     * integer "0" to represent cooperation, and "1" to represent defection.
     * 
     * Recall that in the 2-players dilemma, U(DC) > U(CC) > U(DD) > U(CD), where we
     * give the payoff for the first player in the list. We want the three-player
     * game to resemble the 2-player game whenever one player's response is fixed,
     * and we also want symmetry, so U(CCD) = U(CDC) etc. This gives the unique
     * ordering
     * 
     * U(DCC) > U(CCC) > U(DDC) > U(CDC) > U(DDD) > U(CDD)
     * 
     * The payoffs for player 1 are given by the following matrix:
     */

    static int[][][] payoff = { { { 6, 3 }, // payoffs when first and second players cooperate
            { 3, 0 } }, // payoffs when first player coops, second defects
            { { 8, 5 }, // payoffs when first player defects, second coops
                    { 5, 2 } } };// payoffs when first and second players defect

    /*
     * So payoff[i][j][k] represents the payoff to player 1 when the first player's
     * action is i, the second player's action is j, and the third player's action
     * is k.
     * 
     * In this simulation, triples of players will play each other repeatedly in a
     * 'match'. A match consists of about 100 rounds, and your score from that match
     * is the average of the payoffs from each round of that match. For each round,
     * your strategy is given a list of the previous plays (so you can remember what
     * your opponent did) and must compute the next action.
     */

    abstract class Player {
        // This procedure takes in the number of rounds elapsed so far (n), and
        // the previous plays in the match, and returns the appropriate action.
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            throw new RuntimeException("You need to override the selectAction method.");
        }

        // Used to extract the name of this player class.
        final String name() {
            String result = getClass().getName();
            return result.substring(result.indexOf('$') + 1);
        }
    }

    /* Here are four simple strategies: */

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
        // Similar to Standard T4T but with min{1− (T-R)/(R-S) , (R-P)/(T-P)} probability of forgiving defections
        // R (Reward) = 6 (payoff[0][0][0]), P (Punishment) = 2 (payoff[1][1][1]), T (Temptation) = 8 (payoff[1][0][0]), S (Sucker) = 0 (payoff[0][1][1])
        // Math.min(1 - (payoff[1][0][0] - payoff[0][0][0]) / (payoff[0][0][0] - payoff[0][1][1]),(payoff[0][0][0] - payoff[1][1][1]) / (payoff[1][0][0] - payoff[1][1][1])) 
        // Math.min(1 - (8-6)/(6-0), (6-2)/(8-2)) = Math.min(1 - 2/6, 4/6) = Math.min(2/3, 2/3) = 2/3
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
        // Tests opponents by defecting on the first round if opponent defects, switch to Standard T4T
        // if opponent cooperates, exploit by mixing cooperation and defection exploit every 5 rounds
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
            int lastPayoff = ThreePrisonersDilemma.payoff[myHistory[n - 1]][oppHistory1[n - 1]][oppHistory2[n - 1]];

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
            return new NicePlayer();
        case 1:
            return new NastyPlayer();
        case 2:
            return new RandomPlayer();
        case 3:
            return new TolerantPlayer();
        case 4:
            return new FreakyPlayer();
        case 5:
            return new T4TPlayer();
        case 6:
            return new SuspiciousT4TPlayer();
        case 7:
            return new StandardT4TPlayer();
        case 8:
            return new SuspiciousStandardT4TPlayer();
        case 9:
            return new GenerousT4TPlayer();
        case 10:
            return new JossPlayer();
        case 11:
            return new StandardT42TPlayer();
        case 12:
            return new StandardT43TPlayer();
        case 13:
            return new StandardT44TPlayer();
        case 14:
            return new TesterPlayer();
        case 15:
            return new PavlovPlayer();
        case 16:
            return new TriggerPlayer();
        case 17:
            return new EndGameDefactorPlayer();
        }
        throw new RuntimeException("Bad argument passed to makePlayer");
    }

    /* Finally, the remaining code actually runs the tournament. */

    public static void main(String[] args) {
        ThreePrisonersDilemma instance = new ThreePrisonersDilemma();
        instance.runTournament();
    }

    boolean verbose = true; // set verbose = false if you get too much text output

    void runTournament() {
        float[] totalScore = new float[numPlayers];

        // This loop plays each triple of players against each other.
        // Note that we include duplicates: two copies of your strategy will play once
        // against each other strategy, and three copies of your strategy will play
        // once.

        for (int i = 0; i < numPlayers; i++)
            for (int j = i; j < numPlayers; j++)
                for (int k = j; k < numPlayers; k++) {

                    Player A = makePlayer(i); // Create a fresh copy of each player
                    Player B = makePlayer(j);
                    Player C = makePlayer(k);
                    int rounds = 90 + (int) Math.rint(20 * Math.random()); // Between 90 and 110 rounds
                    float[] matchResults = scoresOfMatch(A, B, C, rounds); // Run match
                    totalScore[i] = totalScore[i] + matchResults[0];
                    totalScore[j] = totalScore[j] + matchResults[1];
                    totalScore[k] = totalScore[k] + matchResults[2];
                    if (verbose)
                        System.out.println(A.name() + " scored " + matchResults[0] + " points, " + B.name() + " scored "
                                + matchResults[1] + " points, and " + C.name() + " scored " + matchResults[2]
                                + " points." + " (Rounds: " + rounds + ")");
                }
        int[] sortedOrder = new int[numPlayers];
        // This loop sorts the players by their score.
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

        // Finally, print out the sorted results.
        if (verbose)
            System.out.println();
        System.out.println("Tournament Results");
        for (int i = 0; i < numPlayers; i++)
            System.out.println(makePlayer(sortedOrder[i]).name() + ": " + totalScore[sortedOrder[i]] + " points.");

    } // end of runTournament()

} // end of class PrisonersDilemma
