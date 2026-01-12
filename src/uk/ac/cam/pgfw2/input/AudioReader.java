package uk.ac.cam.pgfw2.input;

import javax.sound.sampled.AudioFormat;

public interface AudioReader {
    // Initialises reader, ready to update buffer
    public void open() throws AudioException;

    // Updates buffer with latest values
    public boolean read(byte[] buffer);

    // Closes the reader
    public void close();

    // Get format details, required to get sampleRate etc.
    public AudioFormat getFormat();
}
