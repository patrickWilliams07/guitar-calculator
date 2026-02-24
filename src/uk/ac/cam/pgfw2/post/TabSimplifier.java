package uk.ac.cam.pgfw2.post;

import java.util.Arrays;

public class TabSimplifier implements TabPrinter {

    // Formatting State
    private boolean headerPrinted = false;

    // Smoothing State
    private final int windowSize;
    private final float noteBias;
    private int framesCollected;

    // Histogram to store votes: [String (0-5)][Class (0-25)]
    private final int[][] histogram;

    /**
     * @param windowSize Number of frames to collect before printing a line (e.g. 10).
     * @param noteBias   Multiplier for non-silence votes.
     * 1.0 = Standard.
     * 2.0 = Notes are 2x stronger than silence (Good for fast playing).
     */
    public TabSimplifier(int windowSize, float noteBias) {
        this.windowSize = windowSize;
        this.noteBias = noteBias;
        this.histogram = new int[6][26];
        this.framesCollected = 0;
    }

    @Override
    public void print(int[] rawIndices) {
        // 1. ACCUMULATE VOTES
        for (int s = 0; s < 6; s++) {
            int predictedClass = rawIndices[s];
            if (predictedClass >= 0 && predictedClass < 26) {
                histogram[s][predictedClass]++;
            }
        }
        framesCollected++;

        // 2. CHECK IF WINDOW IS FULL
        // If not full yet, we just return and wait for more data.
        if (framesCollected < windowSize) {
            return;
        }

        // 3. CALCULATE WINNERS
        int[] smoothedFrame = new int[6];
        for (int s = 0; s < 6; s++) {
            smoothedFrame[s] = getWeightedWinner(histogram[s]);
        }

        // 4. PRINT THE RESULT
        printLine(smoothedFrame);

        // 5. RESET BUFFER
        resetHistogram();
    }

    /**
     * Finds the index with the highest weighted score.
     * Non-zero indices (actual notes) get their vote count multiplied by 'noteBias'.
     */
    private int getWeightedWinner(int[] counts) {
        int bestClass = 0;
        float maxScore = -1.0f;

        for (int i = 0; i < counts.length; i++) {
            float score = counts[i];

            // Apply Bias: If this is NOT silence (index 0), multiply score
            if (i > 0) {
                score *= noteBias;
            }

            if (score > maxScore) {
                maxScore = score;
                bestClass = i;
            }
        }
        return bestClass;
    }

    private void printLine(int[] indices) {
        if (!headerPrinted) {
            printHeader();
            headerPrinted = true;
        }

        StringBuilder line = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int idx = indices[i];

            if (idx == 0) {
                line.append("|  ");
            } else {
                int fret = idx - 1; // Convert Class Index to Fret Number

                if (fret < 10) {
                    line.append(fret).append("  "); // Single digit padding
                } else {
                    line.append(fret).append(" ");  // Double digit padding
                }
            }
        }
        System.out.println(line.toString());
    }

    private void printHeader() {
        System.out.println();
        System.out.println("E  A  D  G  B  e");
        System.out.println("----------------");
    }

    private void resetHistogram() {
        framesCollected = 0;
        for (int[] row : histogram) {
            Arrays.fill(row, 0);
        }
    }
}