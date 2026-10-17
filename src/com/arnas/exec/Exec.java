package com.arnas.exec;

import com.arnas.data.handler.DataHandler;
import com.arnas.data.handler.MetaData;
import com.arnas.gui.PreprocessingObject;
import com.arnas.gui.TrainingObject;
import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.data.AnfisLog;
import com.arnas.manfis.data.AnfisOutput;
import com.arnas.manfis.main.Anfis;
import com.arnas.manfis.network.rule.membershipFunction.MembershipFunction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Orchestrates load → preprocess → split → train for the Swing UI.
 * Reconstructed for Phase 0; the original source was never committed.
 */
public class Exec {

    /** Shared with {@link KFoldExec} so Main_frame can read models from the parent Exec. */
    static final class Session {
        final List<Anfis> anfisList = new ArrayList<>();
        int currentAnfis;
        volatile boolean stopRequested;
    }

    protected final PreprocessingObject preprocessingObject;
    protected final TrainingObject trainingObject;
    protected final DataHandler handler;
    protected final Session session;

    public Exec(PreprocessingObject preprocessingObject, TrainingObject trainingObject) {
        this(preprocessingObject, trainingObject, new DataHandler(), new Session());
    }

    protected Exec(PreprocessingObject preprocessingObject, TrainingObject trainingObject,
            DataHandler handler, Session session) {
        this.preprocessingObject = preprocessingObject;
        this.trainingObject = trainingObject;
        this.handler = handler;
        this.session = session;
    }

    public DataHandler getHandler() {
        return handler;
    }

    public void loadAndPreprocess(File dataFile, File typeFile, File nameFile, File labelFile)
            throws Exception {
        if (preprocessingObject != null) {
            preprocessingObject.loaderObject.startProcess();
        }
        try {
            handler.loadAndPreprocess(dataFile, typeFile, nameFile, labelFile);
        } finally {
            if (preprocessingObject != null) {
                preprocessingObject.loaderObject.endProcess(handler.metadata());
            }
        }
    }

    public void selectAttribute(int n) throws Exception {
        if (preprocessingObject != null) {
            preprocessingObject.selectorObject.startProcess();
        }
        try {
            handler.selectAttributes(n);
        } finally {
            if (preprocessingObject != null) {
                preprocessingObject.selectorObject.endProcess(handler.metadata());
            }
        }
    }

    public void splitDataTraining(int trainRatio, int testRatio) {
        if (preprocessingObject != null) {
            preprocessingObject.splitterObject.startProcess();
        }
        handler.useFixSpliting(trainRatio, testRatio);
        if (preprocessingObject != null) {
            preprocessingObject.splitterObject.endProcess(handler.metadata());
        }
    }

    public AnfisInput[] getDataTraining() {
        return handler.dataTraining();
    }

    public AnfisInput[] getDataTesting() {
        return handler.dataTesting();
    }

    public MetaData getMetaData() {
        return handler.metadata();
    }

    public void startTraining(String mfType, int nRule, double learningRate, double momentum,
            int maxEpoch, double tolerance) throws Exception {
        session.stopRequested = false;
        Anfis anfis = new Anfis(handler.dataTraining(), handler.dataTesting(),
                handler.metadata(), nRule, mfType);
        session.anfisList.add(anfis);
        session.currentAnfis = session.anfisList.size() - 1;
        runTraining(anfis, learningRate, momentum, maxEpoch, tolerance, true);
    }

    public void continueTraining(int maxEpoch, double tolerance) throws Exception {
        session.stopRequested = false;
        Anfis anfis = current();
        runTraining(anfis, anfis.getLog().getLearningRate(), anfis.getLog().getMomentum(),
                maxEpoch, tolerance, false);
    }

    public void stopTraining() {
        session.stopRequested = true;
    }

    protected void runTraining(Anfis anfis, double learningRate, double momentum,
            int maxEpoch, double tolerance, boolean startNew) throws Exception {
        if (startNew) {
            anfis.startTraining(learningRate, momentum);
            if (trainingObject != null) {
                trainingObject.startTrain();
                trainingObject.continueTrain();
            }
        } else if (trainingObject != null) {
            trainingObject.continueTrain();
        }
        anfis.setIndicator(maxEpoch, tolerance, 50);
        try {
            double rmse;
            do {
                if (session.stopRequested) {
                    break;
                }
                rmse = anfis.trainNextEpoch();
                if (trainingObject != null && !Double.isNaN(anfis.getTrainingResult().getRmse())) {
                    trainingObject.updateView(anfis.getLog());
                }
            } while (!Double.isNaN(rmse) && !session.stopRequested);
            saveRun(anfis);
            if (trainingObject != null) {
                trainingObject.addToResultTable(session.currentAnfis, anfis.getLog());
            }
        } finally {
            if (trainingObject != null) {
                trainingObject.stopTrain();
            }
        }
    }

    public static File[] saveRun(Anfis anfis) throws IOException {
        Files.createDirectories(new File("result").toPath());
        Files.createDirectories(new File("log").toPath());
        String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File modelFile = new File("result", "anfis-" + stamp + ".model.txt");
        File logFile = new File("log", "anfis-" + stamp + ".log.txt");
        anfis.saveModelToFile(modelFile);
        anfis.saveLogToFile(logFile, String.valueOf(anfis.getLog().getDuration()));
        return new File[] {modelFile, logFile};
    }

    public int[][] getConfusionMatrix() {
        return current().getTestingResult()
                .getConfusionMatrix(handler.dataTesting(), handler.metadata());
    }

    public AnfisOutput getTestingResult() {
        return current().getTestingResult();
    }

    public void loadManfisModel(File modelFile) throws Exception {
        if (trainingObject != null) {
            trainingObject.loadModelStart();
        }
        try {
            Anfis anfis = currentOrCreateFromModel(modelFile);
            anfis.loadModelFromFile(modelFile);
            if (trainingObject != null) {
                trainingObject.loadModelFinish(modelFile.getName());
            }
        } catch (Exception ex) {
            if (trainingObject != null) {
                trainingObject.loadModelFinish(modelFile.getName());
            }
            throw ex;
        }
    }

    public void loadTrainingResult(File logFile) throws Exception {
        if (trainingObject != null) {
            trainingObject.loadResultStart();
        }
        try {
            AnfisLog log = currentOrCreate().loadLogFromFile(logFile);
            if (trainingObject != null) {
                trainingObject.setView(log);
                trainingObject.loadResultFinish(logFile.getName());
            }
        } catch (Exception ex) {
            if (trainingObject != null) {
                trainingObject.loadResultFinish("not loaded yet");
            }
            throw ex;
        }
    }

    public void predict(File dataPredictionFile) throws Exception {
        handler.loadDataPrediction(dataPredictionFile);
        current().predict(handler.dataPrediction());
    }

    public AnfisInput[] getDataPrediction() {
        return handler.dataPrediction();
    }

    public AnfisOutput getPredictionResult() {
        return current().getPredictionResult();
    }

    public void resetAnfis() {
        session.anfisList.clear();
        session.currentAnfis = 0;
        session.stopRequested = false;
    }

    public MembershipFunction[][] getMembershipFunctionList() {
        return current().getMembershipFunctionList();
    }

    public void reClassCoding(int[][] newClassCode) {
        handler.reClassCoding(newClassCode);
    }

    public Double[] getAttributeScore() {
        return handler.getScore();
    }

    public Double[][] getAttributeCorrelation() {
        return handler.getCorrelation();
    }

    public double[][][] getConsequentParameter() {
        return current().getConsequentParameter();
    }

    public void setAnfis(int index) {
        session.currentAnfis = index;
    }

    public void viewAnfisLog() {
        if (trainingObject != null) {
            trainingObject.setView(current().getLog());
        }
    }

    protected Anfis current() {
        if (session.anfisList.isEmpty()) {
            throw new NullPointerException("no ANFIS model has been trained or loaded");
        }
        return session.anfisList.get(session.currentAnfis);
    }

    protected Anfis currentOrCreate() {
        if (session.anfisList.isEmpty()) {
            Anfis anfis = new Anfis(handler.dataTraining(), handler.dataTesting(),
                    handler.metadata(), 2, "BELL_MF");
            session.anfisList.add(anfis);
            session.currentAnfis = 0;
        }
        return session.anfisList.get(session.currentAnfis);
    }

    protected Anfis currentOrCreateFromModel(File modelFile) throws Exception {
        if (!session.anfisList.isEmpty()) {
            return session.anfisList.get(session.currentAnfis);
        }
        int nRule = peekRuleCount(modelFile);
        Anfis anfis = new Anfis(handler.dataTraining(), handler.dataTesting(),
                handler.metadata(), nRule, "BELL_MF");
        session.anfisList.add(anfis);
        session.currentAnfis = 0;
        return anfis;
    }

    private static int peekRuleCount(File modelFile) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(modelFile))) {
            String[] part = reader.readLine().split(" ");
            if (!"A".equals(part[0])) {
                throw new Exception("wrong file");
            }
            return Integer.parseInt(part[1]);
        }
    }
}
