package uk.ac.cam.pgfw2.input;

import javax.sound.sampled.AudioFormat;

public class AudioStream {
    private final AudioReader reader;

    // Given to reader for updates
    private final byte[] inputBuffer;
    // Given externally to be read
    private final double[] outputBuffer;

    // Sample rate from the reader
    private final float sampleRate;
    // Fixed number of values for rest of code
    private final int bufferSize;

    public AudioStream(AudioReader reader, int depth) throws AudioException {
        this.reader = reader;

        reader.open();

        AudioFormat format = reader.getFormat();
        this.sampleRate = format.getSampleRate();
        int frameSize = format.getFrameSize();

        this.bufferSize = 1 << depth;

        // Input buffer scaled with frameSize
        this.inputBuffer = new byte[bufferSize * frameSize];
        this.outputBuffer = new double[bufferSize];
    }

    public double[] getBuffer() {
        return this.outputBuffer;
    }

    public boolean read() {
        // updated inputBuffer
        boolean success = reader.read(inputBuffer);
        if (!success) {
            return false;
        }

        // Convert into doubles
        for (int i = 0; i < bufferSize; i++) {
            // Locate the 2 bytes for this sample
            int byteIndex = i * 2;

            // COMBINE BYTES (Big Endian)
            // High byte shifted left, OR'd with Low byte (unsigned)
            int sample = (inputBuffer[byteIndex] << 8) | (inputBuffer[byteIndex + 1] & 0xFF);

            // Normalise
            outputBuffer[i] = sample / 32768.0;
        }
        return true;
    }

    public void close() {
        reader.close();
    }

    public float getSampleRate() {
        return sampleRate;
    }
}