package uk.ac.cam.pgfw2.buffers;

public class DoubleBuffer {
    // Small stepLength = microphone input
    private final int stepLength;
    // Number of past histories kept
    private final int historySize;
    // Total buffer bufferLength
    private final int bufferLength;
    
    private final double[] circularBuffer;

    // pointer to where we write
    private int writeIndex;

    public DoubleBuffer(int stepDepth, int maxDepth) {
        this.stepLength = 1 << stepDepth;
        this.bufferLength = 1 << maxDepth;
        this.historySize = bufferLength / stepLength;
        this.circularBuffer = new double[bufferLength];
        this.writeIndex = 0;
    }

    public void update(double[] audioBuffer) {
        if (audioBuffer.length != stepLength) {
            throw new IllegalArgumentException("Audio buffer doesn't match step");
        }
        int writePos = writeIndex * stepLength;
        System.arraycopy(audioBuffer, 0, circularBuffer, writePos, stepLength);
        writeIndex = (writeIndex + 1) % historySize;
    }


    public void read(double[] outputBuffer) {
        int readLength = outputBuffer.length;
        if (readLength > bufferLength) {
            throw new IllegalArgumentException("Read length invalid size.");
        }
        int startPos = writeIndex * stepLength - readLength;
        if (startPos >= 0) {
            System.arraycopy(circularBuffer, startPos, outputBuffer, 0, readLength);
        } else {
            System.arraycopy(circularBuffer, bufferLength + startPos, outputBuffer,0, -startPos);
            System.arraycopy(circularBuffer, 0, outputBuffer, -startPos, readLength + startPos);
        }
    }
}
