package uk.ac.cam.pgfw2.input;

public interface AudioReader {
    // Initialises reader, ready to update buffer
    public void open() throws AudioException;

    // Updates buffer with latest values
    public boolean read(byte[] buffer);

    // Closes the reader
    public void close();
}
