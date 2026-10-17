package com.arnas.manfis.network.rule.membershipFunction;

/**
 * Gaussian membership function: {@code μ = exp(-½ ((x − c) / σ)²)}.
 * Parameter {@code a} is σ (width), {@code b} is c (center).
 */
public class GaussianMF extends MembershipFunction {

    private static final double MIN_SIGMA = 1e-8;

    private double α;
    private double β;

    @Override
    public double compute(double x) {
        double sigma = Math.max(Math.abs(a), MIN_SIGMA);
        μ = Math.exp(-0.5 * Math.pow((x - b) / sigma, 2));
        return μ;
    }

    @Override
    public void correctError(double error, double x, double η, double m) {
        double sigma = Math.max(Math.abs(a), MIN_SIGMA);
        double u = (x - b) / sigma;
        α = m * α + η * error * μ * u * u / sigma;
        β = m * β + η * error * μ * u / sigma;
        a = a + α;
        b = b + β;
        if (Math.abs(a) < MIN_SIGMA) {
            a = MIN_SIGMA;
        }
    }
}
