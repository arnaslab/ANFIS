package com.arnas.data.handler;

import com.arnas.manfis.data.AnfisInput;
import com.arnas.data.tools.AnfisDataBuilder;
import com.arnas.data.tools.DataLoader;
import com.arnas.data.tools.DataPreProcessor;
import com.arnas.data.tools.DataSplitter;
import com.arnas.data.tools.AttributeSelector;
import com.arnas.manfis.network.Network;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author ARNAS_
 */
public class DataHandler {
    
    private DataPreProcessor preprocessor;
    private AnfisInput[] dataTraining;
    private AnfisInput[] dataTesting;
    private AnfisInput[] dataHoldout;
    private AnfisInput[] dataPrediction;
    private MetaData metadata;
    
    private String splitType;
    private KFoldData foldData;
    private AttributeEvaluatorData attributeEvaluator;
    private SplitTesterData splitTester;
    
    public void loadAndPreprocess(File dataFile, File typeFile, File labelFile)
            throws FileNotFoundException, IOException, Exception {
        loadAndPreprocess(dataFile, typeFile, null, labelFile);
    }

    public void loadAndPreprocess(File dataFile, File typeFile, File nameFile, File labelFile)
            throws FileNotFoundException, IOException, Exception {
        /* Load Data */
        DataLoader loader = new DataLoader();
        ArrayList<String[]> dataTemp = loader.loadFileToWord(dataFile, ",");
        String[] targetTemp = loader.splitLastWord(dataTemp);
        double[][] data = loader.convertWordToDouble(dataTemp);
        int[][] target = loader.convertWordToInteger(loader.parseLineToWord(targetTemp, "/"));
        metadata = new MetaData();
        metadata.setDataType(loader.loadFileToLine(typeFile)[0].split(","));
        if (metadata.getDataType().length != data[0].length) {
            throw new Exception("count of the data type not match");
        }
        if (nameFile != null && nameFile.exists()) {
            metadata.setAttributeNames(loader.loadFileToLine(nameFile)[0].split(","));
        }
        metadata.setClassLabel(loader.loadFileToLine(labelFile));
        
        /* Preprocess Data */
        preprocessor = new DataPreProcessor();
        data = preprocessor.preprocess(data, metadata);
        
        /* Built ANFIS Data */
        AnfisDataBuilder builder = new AnfisDataBuilder();
        dataTraining = builder.makeAnfisData(data, target);
        metadata.setOutputType(Network.NOMINAL_OUTPUT);
        metadata.setMetaData(dataTraining);
        selector = new AttributeSelector();
        splitType = "None";
        
    }
    
    AttributeSelector selector;
    
    public void selectAttributes(int n) throws Exception {
        selector = new AttributeSelector();
        dataTraining = selector.selectAttributes(n, dataTraining, metadata);
    }
    
    public void reClassCoding(int[][] newClassCode) {
        AnfisDataBuilder builder = new AnfisDataBuilder();
        dataTraining = builder.reClassCoding(dataTraining, metadata, newClassCode);
    }
    
    public void useFixSpliting(int tr, int ts) {
        DataSplitter splitter = new DataSplitter();
        splitType = "Fix";
        splitter.fixSplitter(dataTraining, metadata, tr, ts);
        dataTraining = splitter.getNewDataTraining();
        dataTesting = splitter.getNewDataValidation();
        dataHoldout = null;
        metadata.setMetaData(dataTraining, dataTesting);
    }

    /**
     * Split into train / validation / test. Validation is used for early stopping;
     * test is held out until training finishes.
     */
    public void useTrainValTestSplit(int trainRatio, int valRatio, int testRatio) {
        DataSplitter splitter = new DataSplitter();
        splitType = "Fix";
        splitter.threeWaySplitter(dataTraining, metadata, trainRatio, valRatio, testRatio);
        dataTraining = splitter.getNewDataTraining();
        dataTesting = splitter.getNewDataValidation();
        dataHoldout = splitter.getNewDataHoldout();
        metadata.setMetaData(dataTraining, dataTesting);
    }
    
    public void useKFold(int k) {
        DataSplitter splitter = new DataSplitter();
        splitType = "KFold";
        splitter.kFoldSplitter(dataTraining, metadata, k);
        foldData = new KFoldData(splitter.getFoldData());
        metadata.setMetaData(foldData);
    }
    
    public void useAttributeEvaluator(int tr, int ts) {
        useFixSpliting(tr, ts);
        splitType = "Evaluator";
        attributeEvaluator = new AttributeEvaluatorData(dataTraining, dataTesting); 
    }
    
    public void useSplitTester() {
        splitTester = new SplitTesterData(dataTraining, metadata);
        DataSplitter splitter = new DataSplitter();
        splitType = "Tester";
    }
    
    public void loadDataPrediction(File dataFile) throws Exception {
        DataLoader loader = new DataLoader();
        double[][] data = 
            loader.convertWordToDouble(
                loader.loadFileToWord(dataFile, ",")
            );
        dataPrediction = new AnfisInput[data.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = preprocessor.preprocess(data[i]);
            if (data[i].length != metadata.nAttributes()) {
                throw new Exception("count of the data type not match");
            }
            AnfisDataBuilder builder = new AnfisDataBuilder();
            dataPrediction[i] = builder.makeAnfisData(i, data[i]);
        }
    }
    
    public AnfisInput[] dataTraining() {
        switch (splitType) {
            case "None" :
                return dataTraining;
            case "KFold" :
                return foldData.getDataTraining();
            case "Evaluator" :
                return attributeEvaluator.getNextDataTraining();
            case "Tester" :
                return splitTester.getDataTraining();
            default :
                return dataTraining;
        }
    }
    
    public AnfisInput[] dataHoldout() {
        return dataHoldout;
    }

    public AnfisInput[] dataTesting() {
        switch (splitType) {
            case "KFold" :
                return foldData.getDataTesting();
            case "Evaluator" :
                return attributeEvaluator.getNextDataTesting();
            case "Tester" :
                return splitTester.getDataTesting();
            default :
                return dataTesting;
        }
    }
    
    public AnfisInput[] dataPrediction() {
        return dataPrediction;
    }
    
    public MetaData metadata() {
        return metadata;
    }
        
    public KFoldData foldData() throws Exception {
        if (splitType.equals("KFold")) {
            return foldData;
        } else {
            throw new Exception("not a k-fold");
        }
    }
    
    public AttributeEvaluatorData attributeEvaluator() throws Exception {
        if (splitType.equals("Evaluator")) {
            return attributeEvaluator;
        } else {
            throw new Exception("not an attribute evaluator");
        }
    }
    
    public SplitTesterData splitTester() throws Exception {
        if (splitType.equals("Tester")) {
            return splitTester;
        } else {
            throw new Exception("not a split tester");
        }
    }
    
    public Double[][] getCorrelation() {
        return selector.calculateCorrelation(dataTraining, metadata);
    }
    
    public Double[] getScore() {
        return selector.calculateFisherScore(dataTraining, metadata);
    }
    
    private void print(String title, ArrayList<String[]> S) {
        System.out.println(title);
        for (String[] s : S) {
            for (String t : s) {
                System.out.print(t+"\t");
            }
            System.out.println("");
        }
    }
    
    private void print(String title, String[] S) {
        System.out.println(title);
        for (String s : S) {
            System.out.print(s+"\t");
        }
        System.out.println("");
    }

    private void print(String title, int[][] I) {
        System.out.println(title);
        for (int[] i : I) {
            for (int i1 : i) {
                System.out.print(i1+"\t");
            }
            System.out.println("");
        }
    }
    
    private void print(String title, AnfisInput[] AI) {
        System.out.println(title);
        int count = 0;
        for (AnfisInput ai : AI) {
            //for (double d : ai.X()) {
            //    System.out.print(d + " ");
            //}
            //for (double d : ai.Y()) {
            //    System.out.print(d + " ");
            //}
            for (int i : ai.YNum()) {
                System.out.print(i + " ");
                if (i == 0) count++;
            }
            System.out.println("");
        }
        System.out.println(count);
    }
    
}