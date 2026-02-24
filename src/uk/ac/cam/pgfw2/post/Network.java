package uk.ac.cam.pgfw2.post;

import ai.onnxruntime.*;
import java.util.*;

public class Network implements AutoCloseable {
    private final OrtEnvironment env;
    private final OrtSession session;

    public Network(String modelPath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT);
        this.session = env.createSession(modelPath, opts);
    }

    public int[] predict(float[][][][] small4D, float[][][][] big4D) throws OrtException {
        OnnxTensor tSmall = OnnxTensor.createTensor(env, small4D);
        OnnxTensor tBig   = OnnxTensor.createTensor(env, big4D);

        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put("small", tSmall);
        inputs.put("big", tBig);

        try (OrtSession.Result results = session.run(inputs)) {
            int[] predictions = new int[6];
            String[] stringNames = {"string_E", "string_A", "string_D", "string_G", "string_B", "string_e"};

            for (int i = 0; i < 6; i++) {
                float[][] outputProbs = (float[][]) results.get(stringNames[i]).get().getValue();
                predictions[i] = argmax(outputProbs[0]);
            }
            return predictions;
        }
    }

    private int argmax(float[] array) {
        int maxIdx = 0;
        float maxVal = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxVal = array[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    @Override
    public void close() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}