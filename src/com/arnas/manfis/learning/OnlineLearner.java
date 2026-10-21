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
    
    public void learn(Network net, AnfisInput[] dataTraining, AnfisOutput trainingResult) {
        learn(net, dataTraining, trainingResult, true);
    }

    /** Premise-only gradient step used by hybrid learning. */
    public void learnPremises(Network net, AnfisInput[] dataTraining, AnfisOutput trainingResult) {
        learn(net, dataTraining, trainingResult, false);
    }

    private void learn(Network net, AnfisInput[] dataTraining, AnfisOutput trainingResult,
            boolean updateConsequent) {
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
            if (updateConsequent) {
                net.correctError(data1, η, m);
            } else {
                net.correctPremiseError(data1, η, m);
            }
            SSE = SSE + error;
        }
        double rmse = Math.sqrt( SSE / dataTraining.length );
        double cr = (double)(correct * 100) / dataTraining.length;
        trainingResult.setEvaluation(rmse, cr);
    }
    
    public void validate(Network net, AnfisInput[] dataTesting, AnfisOutput testingResult) {
        double SSE = 0;
        int correct = 0;
        for (AnfisInput data1 : dataTesting) {
            net.execute(data1);
            testingResult.setResult(data1.index(), net.getResult());
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
