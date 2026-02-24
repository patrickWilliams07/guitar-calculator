package uk.ac.cam.pgfw2.post;

import java.util.Arrays;

public class VerticalArrayPrinter implements TabPrinter {
    @Override
    public void print(int[] strings) {
        System.out.println(Arrays.toString(strings));
    }
}
