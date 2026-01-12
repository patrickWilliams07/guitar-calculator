package uk.ac.cam.pgfw2.source;

import javax.sound.sampled.*;
import java.util.Arrays;

public class MicrophoneReader implements AudioReader{
    private final AudioFormat format;
    private TargetDataLine line;

    public MicrophoneReader() {
        this.format = new AudioFormat(44100.0F, 16, 1, true, true);
    }

    @Override
    public void open() throws AudioException {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // Check we can use the format
            if (!AudioSystem.isLineSupported(info)) {
                throw new AudioException("Microphone format not supported");
            }

            // Get data line from system
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (LineUnavailableException e) {
            // Something messes up with hardware
            throw new AudioException("Microphone format unavailable", e);
        }
    }

    @Override
    public boolean read(byte[] buffer) {
        // Check for failure
        if (line == null || !line.isOpen()) {
            return false;
        }
        // WAITS here, updating buffer
        int bytesRead = line.read(buffer, 0, buffer.length);

        if (bytesRead == -1) {
            return false;
        }

        // Cuts out early: adds silence
        if (bytesRead < buffer.length) {
            Arrays.fill(buffer, bytesRead, buffer.length, (byte) 0);
        }
        return true;
    }

    @Override
    public void close() {
        if (line != null) {
            line.stop();
            line.close();
        }
    }

    @Override
    public AudioFormat getFormat() {
        return this.format;
    }
}
