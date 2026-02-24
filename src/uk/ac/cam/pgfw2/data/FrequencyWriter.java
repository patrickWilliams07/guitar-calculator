package uk.ac.cam.pgfw2.data;

import uk.ac.cam.pgfw2.analysis.FrequencyExtractor;
import uk.ac.cam.pgfw2.buffers.DoubleBuffer;
import uk.ac.cam.pgfw2.input.*;

public class FrequencyWriter {
    private final static int STEP_DEPTH = 9;
    private final static int SMALL_DEPTH = 10;
    private final static int BIG_DEPTH = 13;

    float[] smallVols = new float[1 << (SMALL_DEPTH - 1)];
    float[] bigVols = new float[1 << (BIG_DEPTH - 1)];

    FrequencyExtractor extractor = new FrequencyExtractor(SMALL_DEPTH, BIG_DEPTH);

    public void write(String inPath, String smallOutPath, String bigOutPath) {
        AudioStream stream = null;
        BinaryWriter smallOut = null;
        BinaryWriter bigOut = null;
        try {
            AudioReader reader = new FileReader(inPath);
            stream = new AudioStream(reader, STEP_DEPTH, BIG_DEPTH);
            DoubleBuffer buffer = stream.getBuffer();

            smallOut = new BinaryWriter(smallOutPath);
            bigOut = new BinaryWriter(bigOutPath);
            smallOut.write(SMALL_DEPTH - 1);
            bigOut.write(BIG_DEPTH - 1);

            while (stream.read()) {
                extractor.evaluate(buffer, smallVols, bigVols);
                smallOut.write(smallVols);
                bigOut.write(bigVols);
            }
        }
        catch (AudioException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (stream != null) {
                stream.close();
            }
            if (smallOut != null) {
                smallOut.close();
            }
            if (bigOut != null) {
                bigOut.close();
            }
        }
    }
}
