package com.arnas.manfis.network.rule.sNorm;

/**
 *
 * @author arif
 */
public class AlgebricSum extends SNorm {
    
    private final int nInput;
    
    public AlgebricSum(int nInput) {
        this.nInput = nInput;
    }
    
    @Override
    public double computeNorm(double μ[]) {
        double sum = 0;
        for (int i = 0; i < nInput; i++) {
            sum = sum + μ[i];
        }
        return sum;
    }
    
    @Override
    public double[] calculateError(double error, double μ[]) {
        double E[] = new double[nInput];
        for (int i = 0; i < nInput; i++) {
            E[i] = 1.0;
        }
        return E;
    }
    
}
