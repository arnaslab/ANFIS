package com.arnas.manfis.learning; 

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 *
 * @author ARNAS_
 */
public class LSERecursive {
    
    double[][] C;   //[nData][nRule * (nInput + 1)]
    double[] y;     //[nData]
    RealMatrix theta;
    
    public LSERecursive(double[][] C, double[] y) {
        this.C = C;
        this.y = y;
    }
    
    public void compute(int n) {
        int cols = Math.min(n, C[0].length);
        double[][] trimmed = new double[C.length][cols];
        for (int i = 0; i < C.length; i++) {
            System.arraycopy(C[i], 0, trimmed[i], 0, cols);
        }
        RealMatrix A = MatrixUtils.createRealMatrix(trimmed);
        RealMatrix y0 = MatrixUtils.createColumnRealMatrix(y);
        theta = new SingularValueDecomposition(A).getSolver().solve(y0);
    }
    
    public double[][] getTheta(int n) {
        int m = theta.getRowDimension() / n;
        double[][] t = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                t[i][j] = theta.getEntry(i * m + j, 0);
            }
        }
        return t;
    }
}
