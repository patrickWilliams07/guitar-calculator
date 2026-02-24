package uk.ac.cam.pgfw2.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class WriteManager {
    private static final String INPUT_ROOT = "rawData/audio/audio_mic";
    private static final String SMALL_ROOT = "data/small";
    private static final String BIG_ROOT = "data/big";


    public static void main(String[] args) {
        Path dir = Paths.get(INPUT_ROOT);
        FrequencyWriter freqWriter = new FrequencyWriter();
        try (Stream<Path> stream = Files.list(dir)) {
            stream
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".wav"))
                    .forEach(name -> {
                        freqWriter.write(INPUT_ROOT + "/" + name,
                                SMALL_ROOT + "/" + name + ".bin",
                                        BIG_ROOT + "/" + name + ".bin");
                        System.out.println("File Written: " + name);
                    });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
