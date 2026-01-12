package uk.ac.cam.pgfw2.analysis;

public interface FourierTransform {
    public Complex[] evaluate(double[] audioBuffer);
}
