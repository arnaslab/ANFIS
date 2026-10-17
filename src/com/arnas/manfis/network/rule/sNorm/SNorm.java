package com.arnas.manfis.network.rule.sNorm;

/**
 *
 * @author arif
 */
public abstract class SNorm {
    
    public abstract double computeNorm(double μ[]);
    
    public abstract double[] calculateError(double error, double μ[]);
    
}
