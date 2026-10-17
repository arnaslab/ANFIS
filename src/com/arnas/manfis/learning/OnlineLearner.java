package com.arnas.manfis.learning;

import com.arnas.manfis.network.Network;
import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.data.AnfisOutput;

/**
 *
 * @author ARNAS_
 */
public class OnlineLearner {
    
    private final double η, m;
    
    public OnlineLearner(double η, double m) {
        this.η = η;
        this.m = m;
    }
    
    public void learn1(Network net, AnfisInput[] dataTraining, AnfisOutput trainingResult) {
        double SSE = 0;
        int correct = 0;
        //for (AnfisInput data1 : dataTraining) {
            //System.out.println(data1.index());
            net.execute(dataTraining[420]);
            trainingResult.setResult(dataTraining[420].index(), net.getResult());
            trainingResult.setCorrectnes(dataTraining[420].index(), dataTraining[420].YBin());
            if (trainingResult.correctness(dataTraining[420].index())) {
                correct++;
            }
            double error = net.calculateError(dataTraining[420]);
            net.correctError(dataTraining[420], η, m);
            SSE = SSE + error;
        //}
        double rmse = Math.sqrt( SSE / dataTraining.length );
        double cr = (double)(correct * 100) / dataTraining.length;
        trainingResult.setEvaluation(rmse, cr);
    }
    
    public void learn(Network net, AnfisInput[] dataTraining, AnfisOutput trainingResult) {
        double SSE = 0;
        int correct = 0;
        for (AnfisInput data1 : dataTraining) {
            net.execute(data1);
            trainingResult.setResult(data1.index(), net.getResult());
            trainingResult.setCorrectnes(data1.index(), data1.YBin());
            if (trainingResult.correctness(data1.index())) {
                correct++;
            }
            double error = net.calculateError(data1);
            net.correctError(data1, η, m);
            SSE = SSE + error;
            //System.out.println(data1.index()+"\t"+error);
        }
        double rmse = Math.sqrt( SSE / dataTraining.length );
        double cr = (double)(correct * 100) / dataTraining.length;
        trainingResult.setEvaluation(rmse, cr);
    }
    
    public void validate(Network net, AnfisInput[] dataTesting, AnfisOutput testingResult) {
        double SSE = 0;
        int correct = 0;
        for (AnfisInput data1 : dataTesting) {
            //System.out.println(data1.index());
            net.execute(data1);
            testingResult.setResult(data1.index(), net.getResult());
            //System.out.println("val");
            testingResult.setCorrectnes(data1.index(), data1.YBin());
            if (testingResult.correctness(data1.index())) {
                correct++;
            }
            double error = net.calculateError(data1);
            SSE = SSE + error;
        }
        double rmse = Math.sqrt( SSE / dataTesting.length );
        double cr = (double)(correct * 100) / dataTesting.length;
        testingResult.setEvaluation(rmse, cr);
    }
    
}
