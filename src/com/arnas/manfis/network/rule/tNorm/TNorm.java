package com.arnas.manfis.network.rule.tNorm;

/**
 *
 * @author ARNAS_
 */
public abstract class TNorm {
    
    public abstract double computeNorm(double μ[]);
    
    public abstract double[] calculateError(double error, double μ[]);
    
}
