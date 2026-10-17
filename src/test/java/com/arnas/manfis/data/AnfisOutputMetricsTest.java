package com.arnas.manfis.data;

import com.arnas.data.handler.MetaData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class AnfisOutputMetricsTest {

    @Test
    void confusionHelpersOnKnownTwoClassMatrix() {
        MetaData metadata = new MetaData();
        metadata.setClassLabel(new String[] {"A", "B"});
        metadata.setOutputType("NOMINAL");

        AnfisInput[] data = new AnfisInput[12];
        AnfisOutput out = new AnfisOutput("NOMINAL", 12);
        int i = 0;
        i = add(data, out, i, 0, -1.0, 5);
        i = add(data, out, i, 0, 1.0, 1);
        i = add(data, out, i, 1, -1.0, 2);
        add(data, out, i, 1, 1.0, 4);

        int[][] matrix = out.getConfusionMatrix(data, metadata);
        assertArrayEquals(new int[] {5, 1}, matrix[0]);
        assertArrayEquals(new int[] {2, 4}, matrix[1]);
        assertArrayEquals(new int[] {5, 4}, out.getTruePositive());
        assertArrayEquals(new int[] {4, 5}, out.getTrueNegative());
        assertArrayEquals(new int[] {2, 1}, out.getFalsePositive());
        assertArrayEquals(new int[] {1, 2}, out.getFalseNegative());
    }

    private static int add(AnfisInput[] data, AnfisOutput out, int start, int trueCode,
            double predictedY, int count) {
        for (int n = 0; n < count; n++) {
            int index = start + n;
            AnfisInput input = new AnfisInput(index, new double[] {0});
            input.setTarget(new int[] {trueCode}, new int[] {trueCode}, new double[] {trueCode == 0 ? -1 : 1});
            data[index] = input;
            out.setResult(index, new double[] {predictedY});
        }
        return start + count;
    }
}
