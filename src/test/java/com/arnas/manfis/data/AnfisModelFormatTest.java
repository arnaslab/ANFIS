package com.arnas.manfis.data;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnfisModelFormatTest {

    @TempDir
    Path temp;

    @Test
    void roundTripWritesVersionHeader() throws Exception {
        AnfisModel model = new AnfisModel();
        model.setMeta("BELL_MF", "PRODUCT", "NOMINAL");
        model.saveModel(
                new double[][] {{0.4, 0.4}},
                new double[][] {{0.0, 1.0}},
                new double[][][] {{{0.1, 0.2, 0.3}}});
        Path file = temp.resolve("m.model.txt");
        model.saveToFile(file.toFile());
        String text = Files.readString(file);
        assertTrue(text.startsWith("ANFIS 1"));
        assertTrue(text.contains("mf_type BELL_MF"));
        assertTrue(text.contains("n_rule 1"));

        AnfisModel loaded = new AnfisModel();
        loaded.loadFromFile(file.toFile());
        assertEquals("BELL_MF", loaded.getMfType());
        assertEquals("PRODUCT", loaded.getTNorm());
        assertEquals(1, loaded.getNRule());
        assertEquals(2, loaded.getNInput());
        assertEquals(1, loaded.getNOutput());
        assertEquals(0.4, loaded.getA()[0][0], 1e-12);
    }

    @Test
    void rejectsUnsupportedVersion() throws Exception {
        Path file = temp.resolve("bad.model.txt");
        Files.writeString(file, "ANFIS 99\nmf_type BELL_MF\n");
        assertThrows(Exception.class, () -> new AnfisModel().loadFromFile(file.toFile()));
    }
}
