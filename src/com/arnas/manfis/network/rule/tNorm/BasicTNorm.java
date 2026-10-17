package com.arnas.manfis.network.rule.tNorm;

/**
 *
 * @author ARNAS_
 */
public class BasicTNorm extends TNorm {
    
    private final int nInput;
    
    public BasicTNorm(int nInput) {
        this.nInput = nInput;
    }
	
    @Override
    public double computeNorm(double μ[]) {
        double min = Double.MAX_VALUE;
        for (double μ1 : μ) {
            if (μ1 < min)
                min = μ1;
        }
        return min;
    }
    
    @Override
    public double[] calculateError(double error, double μ[]) {
        double E[] = new double[nInput];
        
        double min = μ[0];
        int x = 0;
        for (int i = 1; i < nInput; i++) {
            if (μ[i] < min) {
                min = μ[i];
                x = i;
            }
        }
        
        E[x] = error;
        return E;
    }
    
    public double computeNorm1(double μ[]) {
        double sum = 0;
        for (double μ1 : μ) {
            sum += μ1;
        }
        return sum;
    }
    
    public double[] calculateError1(double error, double μ[]) {
        double E[] = new double[nInput];
        
        for (int i = 0; i < nInput; i++) {
            E[i] = error;
        }
        
        return E;
    }
}
