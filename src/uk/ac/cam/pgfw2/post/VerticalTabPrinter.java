package uk.ac.cam.pgfw2.post;

public class VerticalTabPrinter implements TabPrinter {
    private boolean headerPrinted = false;

    public void print(int[] strings) {
        if (!headerPrinted) {
            printHeader();
            headerPrinted = true;
        }

        StringBuilder line = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int idx = strings[i];

            if (idx == 0) {
                line.append("| ");
            } else {
                int fret = idx - 1;

                if (fret < 10) {
                    line.append(fret).append(" "); // Single digit (e.g. "5 ")
                } else {
                    line.append(fret).append(" "); // Double digit (e.g. "12 ")
                }
            }
        }

        System.out.println(line.toString());
    }

    private void printHeader() {
        System.out.println();
        System.out.println("E A D G B e");
        System.out.println("----------------");
    }
}