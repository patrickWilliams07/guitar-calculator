package uk.ac.cam.pgfw2;

import uk.ac.cam.pgfw2.analysis.Complex;
import uk.ac.cam.pgfw2.analysis.FourierTransform;
import uk.ac.cam.pgfw2.analysis.RecursiveTransform;
import uk.ac.cam.pgfw2.input.AudioException;
import uk.ac.cam.pgfw2.input.AudioReader;
import uk.ac.cam.pgfw2.input.AudioStream;
import uk.ac.cam.pgfw2.input.MicrophoneReader;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        AudioStream stream = null;
        try {
            System.out.println("Initializing Audio Engine...");
            int depth = 12;
            // 1. SETUP
            AudioReader reader = new MicrophoneReader();
            stream = new AudioStream(reader, depth);
            FourierTransform transform = new RecursiveTransform(depth);
            double [] buffer = stream.getBuffer();

            System.out.println("Engine Started. Press Ctrl+C to stop.");
            char[] heatMap = " .:-=+*#%@".toCharArray();
            while (stream.read()) {
                Complex[] data = transform.evaluate(buffer);

                // TWEAK 1: Don't cut the bass! Start at 1 to keep Low E (82Hz).
                // We only care about the first 100 bins (approx 0Hz to 1000Hz),
                // which covers 90% of guitar notes.
                int viewRange = 100;
                double[] magnitudes = new double[viewRange];
                double maxVal = 0;

                for (int i = 1; i < viewRange; i++) {
                    // Use sqrt() for more natural volume scaling (Amplitude, not Power)
                    double val = Math.sqrt(data[i].squareModulus());
                    magnitudes[i] = val;
                    if (val > maxVal) maxVal = val;
                }

                // 2. Normalize and Draw
                StringBuilder sb = new StringBuilder();
                sb.append("|");

                for (int i = 1; i < viewRange; i++) {
                    double val = magnitudes[i];

                    // Normalize: Calculate brightness (0 to 9) relative to the loudest sound
                    // If silence, skip.
                    if (maxVal < 10) {
                        sb.append(" ");
                    } else {
                        int brightness = (int) ((val / maxVal) * (heatMap.length - 1));
                        sb.append(heatMap[brightness]);
                    }
                }
                sb.append("|");
                System.out.println(sb.toString());
            }
        }
        catch (AudioException e) {
            System.out.println(e.getMessage());
        }
        finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    // --- Helper Logic for the demo (You can delete this) ---

    private static double calculateRMS(double[] buffer) {
        double sum = 0;
        for (double sample : buffer) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / buffer.length);
    }

    private static void drawMeter(double volume) {
        // Scale volume for display (Sensibility tweak)
        int length = (int) (volume * 100);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 50; i++) {
            bar.append(i < length ? "=" : " ");
        }
        bar.append("]");
        // \r overwrites the current line in the console
        System.out.println(bar.toString());
    }
}