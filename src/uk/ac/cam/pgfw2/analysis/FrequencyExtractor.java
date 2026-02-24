package uk.ac.cam.pgfw2.analysis;

import uk.ac.cam.pgfw2.buffers.DoubleBuffer;

public class FrequencyExtractor {
    private FourierTransform smallTransform;
    private FourierTransform bigTransform;

    private final double[] smallHanning;
    private final double[] bigHanning;

    private final double[] smallBuffer;
    private final double[] bigBuffer;

    public FrequencyExtractor(int smallDepth, int bigDepth) {
        int smallLength = 1 << smallDepth;
        int bigLength = 1 << bigDepth;

        smallTransform = new RecursiveTransform(smallDepth);
        bigTransform = new RecursiveTransform(bigDepth);
        smallHanning = generateHanning(smallLength);
        bigHanning = generateHanning(bigLength);
        smallBuffer = new double[smallLength];
        bigBuffer = new double[bigLength];
    }

    public void evaluate(DoubleBuffer buffer, float[] smallFloats, float[] bigFloats) {
       buffer.read(smallBuffer);
       buffer.read(bigBuffer);
       applyHanning(smallBuffer, smallHanning);
       applyHanning(bigBuffer, bigHanning);
       Complex[] smallComplex = smallTransform.evaluate(smallBuffer);
       Complex[] bigComplex = bigTransform.evaluate(bigBuffer);
       format(smallComplex, smallFloats);
       format(bigComplex, bigFloats);
    }

    private static double[] generateHanning(int length) {
        double sin;
        double[] hanning = new double[length];
        for (int i = 0; i < length; i++) {
            sin = Math.sin(Math.PI * i / (length - 1));
            hanning[i] = sin * sin;
        }
        return hanning;
    }

    private static void applyHanning(double[] buffer, double[] hanning) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] *= hanning[i];
        }
    }

    private static void format(Complex[] before, float[] after) {
        for (int i = 0; i < after.length; i++) {
            double modSquared = before[i].squareModulus();
            if (modSquared == 0.0 || i < 2) {
                after[i] = 0.0F;
            } else {
                double normalised = (10 * Math.log10(modSquared) - 20 * Math.log10(before.length) + 80) / 80;
                after[i] = (float) Math.max(0, Math.min(1, normalised));
            }
        }
    }

}
