package com.arnas.manfis.network.rule.tNorm;

/**
 *
 * @author ARNAS_
 */
public class HamacherProduct extends TNorm {
    
    private final int nInput;
    
    public HamacherProduct(int nInput) {
        this.nInput = nInput;
    }
    
    @Override
    public double computeNorm(double μ[]) {
        double sum = 0;
        double product = 1;
        for (int i = 0; i < nInput; i++) {
            sum = sum + μ[i];
            product = product * μ[i];
        }
        return product / (sum - product);
    }
    
    @Override
    public double[] calculateError(double error, double μ[]) {
        double E[] = new double[nInput];
        
        for (int i = 0; i < nInput; i++) {
            double sum = 0;
            double product = 1;
            for (int j = 0; j < nInput; j++) {
                if (i != j) {
                    sum = sum + μ[j];
                    product = product * μ[j];
                }
            }
            E[i] = error * product * sum / Math.pow((μ[i] + sum - μ[i] * product), 2);
        }
        
        return E;
    }
    
}
