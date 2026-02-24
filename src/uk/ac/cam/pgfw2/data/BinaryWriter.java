package uk.ac.cam.pgfw2.data;

import java.io.*;

public class BinaryWriter {
    private final DataOutputStream out;

    public BinaryWriter(String path) {
        try {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path, false)));
        } catch (IOException e) {
            throw new RuntimeException("Error with Initialising Binary File", e);
        }
    }

    public void write(float[] arr) {
        try {
            for (float f : arr) {
                out.writeFloat(f);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to Binary File", e);
        }
    }

    public void write(int val) {
        try {
            out.writeInt(val);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to Binary File", e);
        }
    }

    public void close() {
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing Binary File", e);
        }
    }
}
