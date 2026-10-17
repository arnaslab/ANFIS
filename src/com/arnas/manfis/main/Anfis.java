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
import com.arnas.manfis.network.rule.membershipFunction.MembershipFunction;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author ARNAS_
 */
public class Anfis {
    
    private AnfisInput[] dataTr;
    private AnfisInput[] dataTs;
    private AnfisInput[] dataPr;
    
    private final MetaData metadata;
    
    private final int nRule;
    private final String mfType;
    
    private final Network net;
    private final AnfisModel model;
    private AnfisLog log;
    
    private AnfisOutput resultTr;
    private AnfisOutput resultTs;
    private AnfisOutput resultPr;
    
    public Anfis(AnfisInput[] dataTraining, AnfisInput[] dataTesting, 
            MetaData metadata, int nRule, String mfType) {
        this.dataTr = dataTraining;
        this.dataTs = dataTesting;
        this.metadata = metadata;
        this.nRule = nRule;
        this.mfType = mfType;
        model = new AnfisModel();
        log = new AnfisLog("BELL_MF", 2, 0.001, 0.01);
        //metadata.setMetaData(dataTraining, dataTesting);
        net = new Network(metadata.getOutputType(), mfType, dataTraining[0].nInput(), nRule, metadata.nOutput());
        resultTr = new AnfisOutput(metadata.getOutputType(), metadata.nDataTraining());
        resultTs = new AnfisOutput(metadata.getOutputType(), metadata.nDataTesting());
        //printData(dataTraining);//for testing purpose
        //printData(dataValidation);//for testing purpose
    }
    
    public void setNewData(AnfisInput[] dataTraining, AnfisInput[] dataTesting) {
        dataTr = dataTraining;
        dataTs = dataTesting;
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
        online = new OnlineLearner(η, m);
        offline = new OfflineLearner(metadata.nDataTraining(), nRule, metadata.nAttributes(), metadata.nOutput());
        log = new AnfisLog(mfType, nRule, η, m);
        //useOfflineLearning();
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
        online.learn(net, dataTr, resultTr);
        online.validate(net, dataTs, resultTs);
        tempTime = ((int)(System.nanoTime()-startTime)/1000000)+tempTime;
        //System.out.println(resultTr.getRmse()+"\t"+resultTr.getAcc()+"\t"+resultTs.getRmse()+"\t"+resultTs.getAcc());
        log.updateLog(e, tempTime, resultTr.getRmse(), resultTr.getAcc(), resultTs.getRmse(), resultTs.getAcc());
        model.saveModel(net.getA(), net.getB(), net.getP());
        //System.out.println(e+"\t"+printDouble(resultTr.getRmse())+"\t"+printDouble(resultTs.getRmse())
        //        +"\t"+printDouble(resultTr.getAcc())+"\t"+printDouble(resultTs.getAcc()));
        e++;
        if (isContinueTraining(resultTr.getRmse())) {
            return resultTr.getRmse();
        } else {
            return Double.NaN;
        }
    }
    
    public void trainRemainingEpoch() {
        do {
            startTime = System.nanoTime();
            online.learn(net, dataTr, resultTr);
            online.validate(net, dataTs, resultTs);
            tempTime = ((int)(System.nanoTime()-startTime)/1000000)+tempTime;
            log.updateLog(e, tempTime, resultTr.getRmse(), resultTr.getAcc(), resultTs.getRmse(), resultTs.getAcc());
            model.saveModel(net.getA(), net.getB(), net.getP());
            //System.out.println(e+"\t"+printDouble(resultTr.getRmse())+"\t"+printDouble(resultTs.getRmse())
            //    +"\t"+printDouble(resultTr.getCr())+"\t"+printDouble(resultTs.getCr()));
            e++;
        } while (isContinueTraining(resultTr.getRmse()));
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
        return resultTs;
    }
    
    public void saveModelToFile(File file) throws IOException {
        model.saveToFile(file);
    }
    
    public void loadModelFromFile(File file) throws Exception {
        model.loadFromFile(file);
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
    
    private String printDouble(double d) {
        return Double.toString(d).replace('.', ',');
    }
    
    public double[][][] getConsequentParameter() {
        return net.getP();
    }
    
}
