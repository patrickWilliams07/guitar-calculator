package uk.ac.cam.pgfw2.buffers;

public class FloatArrayBuffer {
    // Number of past histories kept
    private final int historySize;

    private final int arrayHeight;

    private final float[][] circularBuffer;

    // pointer to where we write
    private int writeIndex;

    public FloatArrayBuffer(int historySize, int arrayHeight) {
        this.historySize = historySize;
        this.arrayHeight = arrayHeight;
        this.circularBuffer = new float[historySize][arrayHeight];
        this.writeIndex = 0;
    }

    public FloatArrayBuffer update(float[] floatArray) {
        if (floatArray.length != arrayHeight) {
            throw new IllegalArgumentException("Mismatched arrays for float buffer");
        }
        System.arraycopy(floatArray, 0, circularBuffer[writeIndex], 0, arrayHeight);
        writeIndex = (writeIndex + 1) % historySize;
        return this;
    }


    public void onnxRead(float[][][][] outputBuffer) {
        for (int freq = 0; freq < arrayHeight; freq++) {
            for (int t = 0; t < historySize; t++) {
                outputBuffer[0][0][freq][t] = circularBuffer[(t + writeIndex) % historySize][freq];
            }
        }
    }
}
