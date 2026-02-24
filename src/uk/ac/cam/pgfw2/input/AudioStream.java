package uk.ac.cam.pgfw2.input;

import uk.ac.cam.pgfw2.buffers.DoubleBuffer;

public class AudioStream {
    private final AudioReader reader;

    // Buffer for the reader 
    private final byte[] audioBuffer;
    
    // Buffer for maintaining frame history
    private final DoubleBuffer historyBuffer;
    
    // Conversion buffer 
    private final double[] doubleBuffer;
    
    // Fixed number of values for rest of code
    private final int stepSize;

    public AudioStream(AudioReader reader, int stepDepth, int maxDepth) throws AudioException {
        this.reader = reader;

        reader.open();

        this.stepSize = 1 << stepDepth;

        this.audioBuffer = new byte[stepSize * 2];
        this.historyBuffer = new DoubleBuffer(stepDepth, maxDepth);
        this.doubleBuffer = new double[stepSize];
    }
    
    public DoubleBuffer getBuffer() {
        return historyBuffer;
    }
    
    public boolean read() {
        boolean success = reader.read(audioBuffer);
        if (!success) {
            return false;
        }

        for (int i = 0; i < stepSize; i++) {
            int sample = (audioBuffer[i * 2] << 8) | (audioBuffer[i * 2 + 1] & 0xFF);

            doubleBuffer[i] = sample / 32768.0;
        }
        historyBuffer.update(doubleBuffer);
        return true;
    }

    public void close() {
        reader.close();
    }
}