package uk.ac.cam.pgfw2.input;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FileReader implements AudioReader {
    private final File wavFile;
    private final AudioFormat targetFormat;

    private AudioInputStream sourceStream;
    private AudioInputStream convertedStream;

    public FileReader(String filePath) {
        this.wavFile = new File(filePath);
        this.targetFormat = new AudioFormat(44100.0F, 16, 1, true, true);
    }

    @Override
    public void open() throws AudioException {
        try {
            sourceStream = AudioSystem.getAudioInputStream(wavFile);
            AudioFormat sourceFormat = sourceStream.getFormat();

            if (!AudioSystem.isConversionSupported(targetFormat, sourceFormat)) {
                throw new AudioException("Cannot convert WAV file to required format (44.1kHz, 16-bit, Mono, BE).");
            }

            convertedStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);

        } catch (UnsupportedAudioFileException e) {
            throw new AudioException("File format not supported (must be valid WAV)", e);
        } catch (IOException e) {
            throw new AudioException("Could not read file: " + wavFile.getAbsolutePath(), e);
        }
    }

    @Override
    public boolean read(byte[] buffer) {
        if (convertedStream == null) {
            return false;
        }

        try {
            int totalBytesRead = 0;

            while (totalBytesRead < buffer.length) {
                int bytesRead = convertedStream.read(buffer, totalBytesRead, buffer.length - totalBytesRead);

                if (bytesRead == -1) {
                    if (totalBytesRead == 0) {
                        return false;
                    }
                    break;
                }

                totalBytesRead += bytesRead;
            }

            if (totalBytesRead < buffer.length) {
                Arrays.fill(buffer, totalBytesRead, buffer.length, (byte) 0);
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error reading from WAV file");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (convertedStream != null) convertedStream.close();
            if (sourceStream != null) sourceStream.close();
        } catch (IOException e) {
        }
    }
}