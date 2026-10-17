package com.arnas.manfis.main;

import com.arnas.data.handler.DataHandler;
import com.arnas.exec.Exec;
import com.arnas.exec.KFoldExec;
import com.arnas.manfis.data.AnfisOutput;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnfisTrainSmokeTest {

    @Test
    void trainsXorAndProducesFiniteRmse() throws Exception {
        Path dataDir = Path.of("dataset");
        DataHandler handler = new DataHandler();
        handler.loadAndPreprocess(
                dataDir.resolve("xor.dataTr.txt").toFile(),
                dataDir.resolve("xor.type.txt").toFile(),
                dataDir.resolve("xor.name.txt").toFile(),
                dataDir.resolve("xor.label.txt").toFile());
        handler.useFixSpliting(3, 1);

        Anfis anfis = new Anfis(handler.dataTraining(), handler.dataTesting(),
                handler.metadata(), 2, "BELL_MF");
        anfis.startTraining(0.01, 0.1);
        anfis.setIndicator(5, 0.0, 6);

        double rmse = Double.NaN;
        for (int i = 0; i < 5; i++) {
            double next = anfis.trainNextEpoch();
            if (!Double.isNaN(next)) {
                rmse = next;
            }
        }

        AnfisOutput training = anfis.getTrainingResult();
        assertFalse(Double.isNaN(training.getRmse()), "training RMSE should be finite");
        assertFalse(Double.isInfinite(training.getRmse()), "training RMSE should be finite");
        assertTrue(handler.dataTraining().length > 0);
        assertTrue(handler.dataTesting().length > 0);
        assertFalse(Double.isNaN(rmse) && Double.isNaN(training.getRmse()));
    }

    @Test
    void kFoldModelsAreVisibleOnParentExec() throws Exception {
        Path dataDir = Path.of("dataset");
        Exec exec = new Exec(null, null);
        exec.loadAndPreprocess(
                dataDir.resolve("xor.dataTr.txt").toFile(),
                dataDir.resolve("xor.type.txt").toFile(),
                dataDir.resolve("xor.name.txt").toFile(),
                dataDir.resolve("xor.label.txt").toFile());
        KFoldExec kfold = new KFoldExec(exec);
        kfold.splitDataTraining(5);
        kfold.startTraining("BELL_MF", 2, 0.01, 0.1, 1, 0.0);

        assertNotNull(exec.getTestingResult());
        assertFalse(Double.isNaN(exec.getTestingResult().getRmse()));
        exec.setAnfis(0);
        assertNotNull(exec.getMembershipFunctionList());
    }

    @Test
    void sampleDatasetFilesExist() {
        File dir = new File("dataset");
        assertTrue(new File(dir, "xor.dataTr.txt").isFile());
        assertTrue(new File(dir, "xor.type.txt").isFile());
        assertTrue(new File(dir, "xor.label.txt").isFile());
        assertTrue(new File(dir, "xor.name.txt").isFile());
    }
}
