package uk.ac.cam.pgfw2.analysis;

public class RecursiveTransform implements FourierTransform {
    private final int length;
    private final Complex[][] values;
    private final Complex temp;
    private final Complex omega;

    public RecursiveTransform (int depth) {
        this.length = 1 << depth;
        this.values = new Complex[depth][length];
        this.temp = new Complex();
        this.omega = new Complex();
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < length; j++) {
                values[i][j] = new Complex();
            }
        }
    }

    public Complex[] evaluate(double[] audioBuffer) {
        if (audioBuffer.length != length) {
            throw new RuntimeException("Buffer Mismatch");
        }
        for (int i = 0; i < length; i++) {
            values[0][i].set(audioBuffer[i], 0);
        }
        fft(0, length, 0);
        return values[0];
    }

    private void fft(int d, int l, int idx) {
        if (l == 2) {
            temp.set(values[d][idx + 1]);
            values[d][idx + 1].set(values[d][idx]).minus(temp);
            values[d][idx].add(temp);
            return;
        }
        int halfl = l / 2;
        for (int i = 0; i < halfl; i++){
            values[d + 1][idx + i].set(values[d][idx +2*i]);
            values[d + 1][idx + halfl + i].set(values[d][idx + 2*i + 1]);
        }
        fft(d + 1, halfl, idx);
        fft(d + 1, halfl, idx + halfl);
        omega.cis(2 * Math.PI / l);
        temp.set(1, 0);
        for (int i = 0; i < halfl; i++) {
            values[d][idx + i].set(values[d+1][idx + halfl + i])
                    .multiply(temp)
                    .add(values[d+1][idx + i]);
            values[d][idx + halfl + i].set(values[d+1][idx + halfl + i])
                    .multiply(temp).multiply(-1)
                    .add(values[d+1][idx + i]);
            temp.multiply(omega);
        }
    }
}
