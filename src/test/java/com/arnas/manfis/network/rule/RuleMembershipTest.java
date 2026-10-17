package com.arnas.manfis.network.rule;

import com.arnas.manfis.network.rule.membershipFunction.BellMF;
import com.arnas.manfis.network.rule.membershipFunction.GaussianMF;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleMembershipTest {

    @Test
    void gaussianMfTypeConstructsGaussianMf() {
        Rule gaussian = new Rule(2, 1, Rule.GAUSSIAN_MF);
        assertTrue(gaussian.getMembershipFunction(0) instanceof GaussianMF);
        assertTrue(gaussian.getMembershipFunction(1) instanceof GaussianMF);
    }

    @Test
    void bellMfTypeConstructsBellMf() {
        Rule bell = new Rule(1, 1, Rule.BELL_MF);
        assertTrue(bell.getMembershipFunction(0) instanceof BellMF);
    }

    @Test
    void gaussianFormulaIsStandard() {
        GaussianMF mf = new GaussianMF();
        mf.setParameters(1.0, 0.0);
        assertEquals(1.0, mf.compute(0.0), 1e-12);
        assertEquals(Math.exp(-0.5), mf.compute(1.0), 1e-12);
    }
}
