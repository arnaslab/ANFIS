package com.arnas.exec;

import com.arnas.data.handler.KFoldData;
import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.main.Anfis;

/**
 * Runs ANFIS training once per fold. Reconstructs the missing class used by Main_frame.
 * Shares the parent Exec session so confusion/predict/membership views still work.
 */
public class KFoldExec extends Exec {

    public KFoldExec(Exec exec) {
        super(exec.preprocessingObject, exec.trainingObject, exec.getHandler(), exec.session);
    }

    public void splitDataTraining(int k) {
        if (preprocessingObject != null) {
            preprocessingObject.splitterObject.startProcess();
        }
        handler.useKFold(k);
        if (preprocessingObject != null) {
            preprocessingObject.splitterObject.endProcess(handler.metadata());
        }
    }

    public AnfisInput[][] getFoldData() throws Exception {
        return handler.foldData().getFoldData();
    }

    @Override
    public void startTraining(String mfType, int nRule, double learningRate, double momentum,
            int maxEpoch, double tolerance) throws Exception {
        session.stopRequested = false;
        KFoldData folds = handler.foldData();
        folds.resetK();
        session.anfisList.clear();
        int foldCount = folds.getK();
        for (int i = 0; i < foldCount && !session.stopRequested; i++) {
            if (i > 0) {
                folds.setNextK();
            }
            handler.metadata().setMetaData(handler.dataTraining(), handler.dataTesting());
            Anfis anfis = new Anfis(handler.dataTraining(), handler.dataTesting(),
                    handler.metadata(), nRule, mfType);
            session.anfisList.add(anfis);
            session.currentAnfis = session.anfisList.size() - 1;
            runTraining(anfis, learningRate, momentum, maxEpoch, tolerance, true);
        }
    }
}
