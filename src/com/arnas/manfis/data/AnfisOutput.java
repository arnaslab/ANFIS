package com.arnas.manfis.data;

import com.arnas.data.handler.MetaData;

/**
 *
 * @author ARNAS_
 */
public class AnfisOutput {
    
    private final String outputType;
    
    private double[][] y;
    private int[][] yBin;
    private int[] yNum;
    private boolean[] correctness;
    private double rmse;
    private double acc;
    
    public AnfisOutput(String outputType, int nData) {
        this.outputType = outputType;
        y = new double[nData+1][];
        yBin = new int[nData+1][];
        yNum = new int[nData+1];
        correctness = new boolean[nData+1];
    }
    
    public void setResult(int i, double[] Y) {
        y[i] = new double[Y.length];
        System.arraycopy(Y, 0, y[i], 0, Y.length);
        yBin[i] = resultToBin(Y);
        yNum[i] = binToNum(yBin[i]);
    }
    
           
    private int[] resultToBin(double[] Y) {
        int[] bin = new int[Y.length];
        for (int i = 0; i < bin.length; i++) {
            bin[i] = activationFunction(Y[i]);
        }
        return bin;
    }
    
    private int activationFunction(double x) {
        if (x < 0) {
            return 0;
        } else {
            return 1;
        }
    }
    
    private int binToNum(int[] bin) {
        int num = 0;
        for (int i = 0 ; i < bin.length; i++) {
            num = num + bin[i] * (int)Math.pow(2,i);
        }
        return num;
    }
    
    public void setCorrectnes(int i, int[] yBinTarget) {
        boolean isCorrect = true;
        int j = 0;
        while ((isCorrect) && (j < yBin[i].length)) {
            if ((yBinTarget[j] != 2) && (yBin[i][j] != yBinTarget[j])) {
                isCorrect = false;
            } else {
                j++;
            }
        }
        correctness[i] = isCorrect;
    }
    
    public void setEvaluation(double rmse, double acc) {
        this.rmse = rmse;
        this.acc = acc;
    }
    
    public int yNum(int i) {
        return yNum[i];
    }
    
    public int[] yBin(int i) {
        return yBin[i];
    }
    
    public boolean correctness(int i) {
        return correctness[i];
    }
    
    public double getRmse() {
        return rmse;
    }
    
    public double getAcc() {
        return acc;
    }
        
    public int[][] getCorrectnessDistribution(AnfisInput[] dataTraining, MetaData metadata) {
        int[][] cd = new int[2][metadata.nClass()];
        for (int i = 0; i < dataTraining.length; i++) {
            if (correctness[i]) {
                cd[0][metadata.getClassIndexFromCode(dataTraining[i].YNum()[0])] ++ ;
            } else {
                cd[1][metadata.getClassIndexFromCode(dataTraining[i].YNum()[0])] ++ ;
            }
        }
        return cd;
    }
    
    private int[][] confusionMatrix;
    
    public int[][] getConfusionMatrix(AnfisInput[] data, MetaData metadata) {
        confusionMatrix = new int[metadata.nClass()][metadata.nClass()];
        for (int i = 0; i < data.length; i++) {
            confusionMatrix[metadata.getClassIndexFromCode(data[i].YNum()[0])]
                    [metadata.getClassIndexFromCode(yNum[i])]++;
        }
        return confusionMatrix;
    }
    
    public int[] getTruePositive() {
        int[] TP = new int[confusionMatrix.length];
        for (int i = 0; i < TP.length; i++) {
            TP[i] = confusionMatrix[i][i];
        }
        return TP;
    }
    
    public int[] getTrueNegative() {
        int[] TN = new int[confusionMatrix.length];
        for (int i = 0; i < TN.length; i++) {
            for (int j = 0; j < TN.length; j++) {
                for (int k = 0; k < TN.length; k++) {
                    if (j != i && k != i) {
                        TN[i] += confusionMatrix[j][k];
                    }
                }
            }
        }
        return TN;
    }
    
    public int[] getFalsePositive() {
        int[] FP = new int[confusionMatrix.length];
        for (int i = 0; i < FP.length; i++) {
            for (int j = 0; j < FP.length; j++) {
                if (i != j) {
                    FP[i] += confusionMatrix[j][i];
                }
            }
        }
        return FP;
    }
    
    public int[] getFalseNegative() {
        int[] FN = new int[confusionMatrix.length];
        for (int i = 0; i < FN.length; i++) {
            for (int j = 0; j < FN.length; j++) {
                if (i != j) {
                    FN[i] += confusionMatrix[i][j];
                }
            }
        }
        return FN;
    }
    
    public int[] getRelevant() {
        int[] relevant = new int[confusionMatrix.length];
        int[] TP = getTruePositive();
        int[] FN = getFalseNegative();
        for (int i = 0; i < relevant.length; i++) {
            relevant[i] = TP[i] + FN[i];
        }
        return relevant;
    }
    
    public int[] getRetrieved() {
        int[] retrieved = new int[confusionMatrix.length];
        int[] TP = getTruePositive();
        int[] FP = getFalsePositive();
        for (int i = 0; i < retrieved.length; i++) {
            retrieved[i] = TP[i] + FP[i];
        }
        return retrieved;
    }
    
    public double[] getRecall() {
        double[] recall = new double[confusionMatrix.length];
        int[] TP = getTruePositive();
        int[] FN = getFalseNegative();
        for (int i = 0; i < recall.length; i++) {
            recall[i] = TP[i] / (double)(TP[i] + FN[i]);
        }
        return recall;
    }
    
    public double[] getPrecision() {
        double[] precision = new double[confusionMatrix.length];
        int[] TP = getTruePositive();
        int[] FP = getFalsePositive();
        for (int i = 0; i < precision.length; i++) {
            try {
                precision[i] = TP[i] / (double)(TP[i] + FP[i]);
            } catch(java.lang.ArithmeticException ex) {
                
            }
        }
        return precision;
    }
    
    public double[] getFMeasure() {
        double[] p = getPrecision();
        double[] r = getRecall();
        double[] f = new double[confusionMatrix.length];
        for (int i = 0; i < f.length; i++) {
            f[i] = 2 * (p[i] * r[i]) / (p[i] + r[i]);
        }
        return f;
    }
    
    public double[] getSensitivity() {
        return getRecall();
    }
    
    public double[] getSpecificity() {
        double[] specificity = new double[confusionMatrix.length];
        int[] TN = getTrueNegative();
        int[] FP = getFalsePositive();
        for (int i = 0; i < specificity.length; i++) {
            specificity[i] = TN[i] / (double)(FP[i] + TN[i]);
        }
        return specificity;
    }
    
}