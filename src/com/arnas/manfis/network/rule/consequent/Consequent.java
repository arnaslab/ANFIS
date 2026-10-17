package com.arnas.manfis.network.rule.consequent;

import com.arnas.manfis.data.AnfisInput;
import java.util.Random;

/**
 *
 * @author ARNAS_
 */
public class Consequent {
    
    private final int nInput;
    
    private double[] p;
    
    private double f;
    
    private double[] q;
    
    public Consequent(int nInput) {
        this.nInput = nInput;
        p = new double[nInput + 1];
        Random rand = new Random();
        for (int i = 0; i < nInput+1; i++) {
            p[i] = rand.nextDouble();
        }
        q = new double[nInput + 1];
    }
    
    public double compute(AnfisInput data) {
        f = 0;
        for (int i = 0; i < nInput; i++) {
            f = f + data.X(i)*p[i];
        }
        f = f + p[nInput];
        return f;
    }
    
    public void correctError(double error, double w̅, AnfisInput data, double η, double m) {
        int i = 0;
        for (; i < nInput; i++) {
            q[i] = m * q[i] + η * error * w̅ * data.X(i);
            p[i] = p[i] + q[i];
            //System.out.print(p[i]+"\t");
        }
        q[i] = m * q[i] + η * error * w̅;
        p[i] = p[i] + q[i];
        //System.out.print(p[i]+"\t");
    }
    
    public double f() {
        return f;
    }
    
    public double[] getParameter() {
        return p;
    }
    
    public void setParameters(double[] P) {
        System.arraycopy(P, 0, p, 0, nInput+1);
    }
}
