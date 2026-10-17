package com.arnas.manfis.network.rule.membershipFunction;

/**
 * bell membership function class
 * @author ARNAS_
 */
public class BellMF extends MembershipFunction {

    @Override
    public double compute(double x) {
        μ = 1 / (1 + Math.pow(((x - b) / a), 2));
        return μ;
    }
    
    private double α;
    private double β;
    
    @Override
    public void correctError(double error, double x, double η, double m) {
        α = m * α + η * error * 2 * Math.pow(x - b, 2) / (Math.pow(a, 3) * Math.pow(1 + Math.pow((x - b) / a, 2), 2));
        β = m * β + η * error * 2 * (x - b) / (Math.pow(a, 2) * Math.pow(1 + Math.pow((x - b) / a, 2), 2));
        a = a + α;
        b = b + β;
        //System.out.println(a+"\t"+b);
    }
    
}
