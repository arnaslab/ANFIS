package com.arnas.manfis.network.rule.membershipFunction;

import java.util.Random;

/**
 * gaussian membership function class
 * @author ARNAS_
 */
public class GaussianMF extends MembershipFunction {
    
    public GaussianMF() {
        Random rand = new Random();
        a = rand.nextDouble();
        b = rand.nextDouble();
    }
    
    private double α;
    private double β;
    
    @Override
    public double compute(double x) {
        μ = Math.exp(-b*Math.pow(a-x,2));
        return μ;
    }
    
    @Override
    public void correctError(double error, double x, double η, double m) {
        α = (1-m) * α + η * x * error * 2*b*(a-x)*μ;
        β = (1-m) * β + η * x * error * Math.pow(a-x,2)*-μ;
        a = a + α;
        b = b + β;
    }
    
}
