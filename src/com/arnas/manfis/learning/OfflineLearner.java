package com.arnas.manfis.learning;

import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.network.Network;
import org.apache.commons.math3.linear.SingularMatrixException;

/**
 *
 * @author ARNAS_
 */
public class OfflineLearner {
    
    private final int nData, nRule, nInput, nOutput;
    
    public OfflineLearner(int nData, int nRule, int nInput, int nOutput) {
        this.nData = nData;
        this.nRule = nRule;
        this.nInput = nInput;
        this.nOutput = nOutput;
    }
    
    public void learn(Network net, AnfisInput[] data) throws SingularMatrixException {
        double[][] C = getC(net, data);
        //printMatrix(C);
        for (int i = 0; i < nOutput; i++) {
            double[] y = getY(data, i);
            LSERecursive lse = new LSERecursive(C, y);
            System.out.print("LSE recursive");
            lse.compute(nRule*(nInput+1));
            System.out.println("");
            double[][] p = lse.getTheta(nRule);
            for (int j = 0; j < nRule; j++) {
                net.rule(j).Cons(i).setParameters(p[j]);
            }
        }
    }
    
    private double[][] getC(Network net, AnfisInput[] data) {
        double[][] C = new double[nData][(nRule*(nInput+1))];
        for (int i = 0; i < nData; i++) {
            net.execute(data[i]);
            for (int j = 0; j < nRule; j++) {
                int k = 0;
                for ( ; k < nInput; k++) {
                    C[i][j*(nInput+1)+k] = net.rule(j).w̅() * data[i].X(k);
                }
                C[i][j*(nInput+1)+k] = net.rule(j).w̅();
            }
        }
        return C;
    }
    
    private double[] getY(AnfisInput[] data, int iOutput) {
        double[] y = new double[nData];
        for (int i = 0; i < nData; i++) {
            y[i] = data[i].Y(iOutput);
        }
        return y;
    }
    
    private void printMatrix(double[][] d) {
        for (double[] d1 : d) {
            for (double d2 : d1) {
                System.out.print(d2+"\t");
            }
            System.out.println("");
        }
    }
}
