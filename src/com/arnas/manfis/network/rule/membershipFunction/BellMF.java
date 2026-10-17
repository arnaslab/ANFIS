package com.arnas.manfis.network.rule.membershipFunction;

/**
 * Two-parameter Cauchy (special-case bell) membership function:
 * {@code μ = 1 / (1 + ((x − b) / a)²)}.
 * This is generalized bell with exponent 2, so the model file can keep A and B only.
 */
public class BellMF extends MembershipFunction {

    private static final double MIN_A = 1e-8;

    private double α;
    private double β;

    @Override
    public double compute(double x) {
        double width = Math.abs(a) < MIN_A ? MIN_A : a;
        μ = 1 / (1 + Math.pow(((x - b) / width), 2));
        return μ;
    }

    @Override
    public void correctError(double error, double x, double η, double m) {
        double width = Math.abs(a) < MIN_A ? MIN_A : a;
        double denom = Math.pow(1 + Math.pow((x - b) / width, 2), 2);
        α = m * α + η * error * 2 * Math.pow(x - b, 2) / (Math.pow(width, 3) * denom);
        β = m * β + η * error * 2 * (x - b) / (Math.pow(width, 2) * denom);
        a = a + α;
        b = b + β;
        if (Math.abs(a) < MIN_A) {
            a = MIN_A;
        }
    }
}
