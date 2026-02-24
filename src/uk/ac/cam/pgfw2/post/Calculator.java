package uk.ac.cam.pgfw2.post;

import ai.onnxruntime.OrtException;
import uk.ac.cam.pgfw2.analysis.FrequencyExtractor;
import uk.ac.cam.pgfw2.buffers.DoubleBuffer;
import uk.ac.cam.pgfw2.buffers.FloatArrayBuffer;
import uk.ac.cam.pgfw2.input.*;

public class Calculator {
    private final static int STEP_DEPTH = 9;
    private final static int SMALL_DEPTH = 10;
    private final static int BIG_DEPTH = 13;

    public static void main(String[] args) {
        AudioStream stream = null;
        try (Network nn = new Network("params/guitar_model.onnx")){
            AudioReader reader = new MicrophoneReader();
//            AudioReader reader = new FileReader("rawData/audio/audio_mic/02_BN3-119-G_solo_mic.wav");

            stream = new AudioStream(reader, STEP_DEPTH, BIG_DEPTH);
            FrequencyExtractor extractor = new FrequencyExtractor(SMALL_DEPTH, BIG_DEPTH);
            DoubleBuffer audioBuffer = stream.getBuffer();
            float[] smallVols = new float[1 << (SMALL_DEPTH - 1)];
            float[] bigVols = new float[1 << (BIG_DEPTH - 1)];

            FloatArrayBuffer smallFrequencyBuffer = new FloatArrayBuffer(11, 1 << (SMALL_DEPTH - 1));
            FloatArrayBuffer bigFrequencyBuffer = new FloatArrayBuffer(11, 1 << (BIG_DEPTH - 1));
            float[][][][] smallInput = new float[1][1][1 << (SMALL_DEPTH - 1)][11];
            float[][][][] bigInput = new float[1][1][1 << (BIG_DEPTH - 1)][11];
            int[] result;
            TabPrinter printer = new TabSimplifier(4, 3);

            while (stream.read()) {
                extractor.evaluate(audioBuffer, smallVols, bigVols);
                smallFrequencyBuffer.update(smallVols).onnxRead(smallInput);
                bigFrequencyBuffer.update(bigVols).onnxRead(bigInput);
                result = nn.predict(smallInput, bigInput);
                printer.print(result);
            }
        } catch (AudioException e) {
            System.out.println(e.getMessage());
        } catch (OrtException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
