package com.arnas.manfis.network.rule.tNorm;

/**
 *
 * @author ARNAS_
 */
public class AlgebricProduct extends TNorm {
    
    private final int nInput;
    
    public AlgebricProduct(int nInput) {
        this.nInput = nInput;
    }
    
    @Override
    public double computeNorm(double μ[]) {
        double product = 1;
        for (int i = 0; i < nInput; i++) {
            product = product * μ[i];
        }
        return product;
    }
    
    @Override
    public double[] calculateError(double error, double μ[]) {
        double E[] = new double[nInput];
        for (int i = 0; i < nInput; i++) {
            E[i] = 1.0;
            for (int j = 0; j < nInput; j++) {
                if (i != j) {
                    E[i] = E[i] * μ[j];
                }
            }
            E[i] = E[i] * error;
        }
        return E;
    }
    
}
