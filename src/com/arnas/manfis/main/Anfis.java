package com.arnas.manfis.main;

import com.arnas.manfis.learning.OfflineLearner;
import com.arnas.manfis.learning.OnlineLearner;
import com.arnas.manfis.network.Network;
import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.data.AnfisModel;
import com.arnas.data.handler.MetaData;
import com.arnas.manfis.data.AnfisLog;
import com.arnas.manfis.data.AnfisOutput;
import com.arnas.manfis.learning.Predictor;
import com.arnas.manfis.network.rule.Rule;
import com.arnas.manfis.network.rule.membershipFunction.MembershipFunction;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author ARNAS_
 */
public class Anfis {
    
    private AnfisInput[] dataTr;
    private AnfisInput[] dataVal;
    private AnfisInput[] dataTs;
    
    private final MetaData metadata;
    
    private final int nRule;
    private final String mfType;
    private final String tNorm;
    
    private final Network net;
    private final AnfisModel model;
    private AnfisLog log;
    
    private AnfisOutput resultTr;
    private AnfisOutput resultVal;
    private AnfisOutput resultTs;
    private AnfisOutput resultPr;
    
    public Anfis(AnfisInput[] dataTraining, AnfisInput[] dataTesting, 
            MetaData metadata, int nRule, String mfType) {
        this(dataTraining, dataTesting, null, metadata, nRule, mfType, Rule.PRODUCT_NORM);
    }

    public Anfis(AnfisInput[] dataTraining, AnfisInput[] dataTesting, AnfisInput[] dataHoldout,
            MetaData metadata, int nRule, String mfType, String tNorm) {
        this.dataTr = dataTraining;
        this.dataVal = dataTesting;
        this.dataTs = dataHoldout;
        this.metadata = metadata;
        this.nRule = nRule;
        this.mfType = mfType;
        this.tNorm = tNorm == null ? Rule.PRODUCT_NORM : tNorm;
        model = new AnfisModel();
        model.setMeta(this.mfType, this.tNorm, metadata.getOutputType());
        log = new AnfisLog(mfType, nRule, 0.001, 0.01);
        net = new Network(metadata.getOutputType(), mfType, this.tNorm,
                dataTraining[0].nInput(), nRule, metadata.nOutput());
        resultTr = new AnfisOutput(metadata.getOutputType(), dataTraining.length);
        resultVal = new AnfisOutput(metadata.getOutputType(),
                dataTesting == null ? 0 : dataTesting.length);
        resultTs = dataHoldout == null ? null
                : new AnfisOutput(metadata.getOutputType(), dataHoldout.length);
    }
    
    public void setNewData(AnfisInput[] dataTraining, AnfisInput[] dataTesting) {
        dataTr = dataTraining;
        dataVal = dataTesting;
        resultTr = new AnfisOutput(metadata.getOutputType(), dataTraining.length);
        resultVal = new AnfisOutput(metadata.getOutputType(),
                dataTesting == null ? 0 : dataTesting.length);
    }
    
    private OnlineLearner online;
    private OfflineLearner offline;

    private int epoch;
    private int e;
    private double tolerance;
    private int indicator;
    private int tempTime;
    private long startTime;
    
    public void startTraining(double η, double m) {
        e = 0;
        tempTime = 0;
        stagnant = 0;
        rmseTemp = Double.NaN;
        online = new OnlineLearner(η, m);
        offline = new OfflineLearner(dataTr.length, nRule, dataTr[0].nInput(), metadata.nOutput());
        log = new AnfisLog(mfType, nRule, η, m);
    }
    
    public void setIndicator(int maxEpoch, double errorTolerance, int stagnantIndicator) {
        this.epoch = maxEpoch - 1;
        this.tolerance = errorTolerance;
        this.indicator = stagnantIndicator;
    }
    
    public void useOfflineLearning() {
        offline.learn(net, dataTr);
    }
    
    public double trainNextEpoch() {
        startTime = System.nanoTime();
        useOfflineLearning();
        online.learnPremises(net, dataTr, resultTr);
        boolean hasVal = dataVal != null && dataVal.length > 0;
        if (hasVal) {
            online.validate(net, dataVal, resultVal);
        }
        tempTime = ((int)(System.nanoTime()-startTime)/1000000)+tempTime;
        double valRmse = hasVal ? resultVal.getRmse() : resultTr.getRmse();
        double valAcc = hasVal ? resultVal.getAcc() : resultTr.getAcc();
        log.updateLog(e, tempTime, resultTr.getRmse(), resultTr.getAcc(), valRmse, valAcc);
        model.saveModel(net.getA(), net.getB(), net.getP());
        e++;
        if (isContinueTraining(valRmse)) {
            return resultTr.getRmse();
        } else {
            return Double.NaN;
        }
    }
    
    public void trainRemainingEpoch() {
        while (!Double.isNaN(trainNextEpoch())) {
            // stop condition is inside trainNextEpoch
        }
    }
    
    private boolean isContinueTraining(double currentRmse) {
        return !((e > epoch) || isStagnant(currentRmse) || (currentRmse <= tolerance) || Double.isNaN(currentRmse));
    }
    
    private double rmseTemp;
    private int stagnant;
    
    private boolean isStagnant(double currentRmse) {
        if (currentRmse == rmseTemp) {
            stagnant++;
        } else {
            stagnant = 0;
        }
        rmseTemp = currentRmse;
        return (stagnant > indicator);
    }
    
    public AnfisOutput getTrainingResult() {
        return resultTr;
    }
    
    public AnfisOutput getTestingResult() {
        return resultVal;
    }

    public AnfisOutput getValidationResult() {
        return resultVal;
    }

    public AnfisOutput evaluateTestSet() {
        if (dataTs == null || dataTs.length == 0) {
            return null;
        }
        if (resultTs == null) {
            resultTs = new AnfisOutput(metadata.getOutputType(), dataTs.length);
        }
        if (online == null) {
            online = new OnlineLearner(0, 0);
        }
        online.validate(net, dataTs, resultTs);
        return resultTs;
    }

    public AnfisOutput getTestResult() {
        return resultTs;
    }
    
    public void saveModelToFile(File file) throws IOException {
        model.setMeta(mfType, tNorm, metadata.getOutputType());
        model.saveToFile(file);
    }
    
    public void loadModelFromFile(File file) throws Exception {
        model.loadFromFile(file);
        if (model.getNRule() != 0 && model.getNRule() != nRule) {
            throw new Exception("model n_rule " + model.getNRule() + " != " + nRule);
        }
        if (model.getNInput() != 0 && model.getNInput() != dataTr[0].nInput()) {
            throw new Exception("model n_input " + model.getNInput() + " != " + dataTr[0].nInput());
        }
        if (model.getNOutput() != 0 && model.getNOutput() != metadata.nOutput()) {
            throw new Exception("model n_output " + model.getNOutput() + " != " + metadata.nOutput());
        }
        rejectMetaMismatch("mf_type", model.getMfType(), mfType);
        rejectMetaMismatch("t_norm", model.getTNorm(), tNorm);
        rejectMetaMismatch("output_type", model.getOutputType(), metadata.getOutputType());
        net.setParameters(model.getA(), model.getB(), model.getP());
    }
    
    public void saveLogToFile(File file, String duration) throws IOException {
        log.saveToFile(file, duration);
    }
    
    public AnfisLog loadLogFromFile(File file) throws Exception {
        log.loadFromFile(file);
        return log;
    }
    
    public AnfisLog getLog() {
        return log;
    }
    
    public void predict(AnfisInput[] dataPr) {
        resultPr = new AnfisOutput(metadata.getOutputType(), dataPr.length);
        Predictor prediction = new Predictor();
        prediction.predict(net, dataPr, resultPr);
    }
    
    public AnfisOutput getPredictionResult() {
        return resultPr;
    }
    
    public MembershipFunction[][] getMembershipFunctionList() {
        return net.getMembershipFunctionList();
    }
    
    public double[][][] getConsequentParameter() {
        return net.getP();
    }

    public String getTNorm() {
        return tNorm;
    }

    private static void rejectMetaMismatch(String field, String actual, String expected) throws Exception {
        if (actual == null || "-".equals(actual) || actual.isEmpty()) {
            return;
        }
        if (!actual.equals(expected)) {
            throw new Exception("model " + field + " " + actual + " != " + expected);
        }
    }
    
}
