package com.arnas.data.tools;

import com.arnas.data.handler.MetaData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataPreProcessorTest {

    @Test
    void dropsColumnThatContainsMissingValues() throws Exception {
        double[][] data = {
            {1.0, Double.NaN},
            {2.0, 3.0},
            {3.0, 4.0}
        };
        MetaData metadata = new MetaData();
        metadata.setDataType(new String[] {"linear", "linear"});
        metadata.setAttributeNames(new String[] {"keep", "drop"});

        double[][] processed = new DataPreProcessor().preprocess(data, metadata);

        assertEquals(1, processed[0].length);
        assertEquals(1, metadata.getDataType().length);
        assertEquals("keep", metadata.getAttributeNames()[0]);
    }

    @Test
    void outlierFilterDoesNotEmptyTheDataset() throws Exception {
        double[][] data = new double[12][1];
        for (int i = 0; i < 11; i++) {
            data[i][0] = 1.0;
        }
        data[11][0] = 100.0;
        MetaData metadata = new MetaData();
        metadata.setDataType(new String[] {"linear"});

        double[][] processed = new DataPreProcessor().preprocess(data, metadata);

        assertTrue(processed.length >= 8, "inliers should remain, got " + processed.length);
        assertTrue(processed.length < data.length, "the extreme outlier should be removed");
    }
}
