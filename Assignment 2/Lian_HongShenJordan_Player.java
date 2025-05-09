class Lian_HongShenJordan_Player extends Player {
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
