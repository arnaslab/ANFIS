package com.arnas.manfis.network.rule.tNorm;

import com.arnas.manfis.network.rule.Rule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlgebricProductTest {

    @Test
    void firingStrengthIsProductNotSum() {
        AlgebricProduct product = new AlgebricProduct(2);
        assertEquals(0.12, product.computeNorm(new double[] {0.3, 0.4}), 1e-12);
    }

    @Test
    void ruleConstructorSelectsEinsteinAndHamacher() {
        Rule einstein = new Rule(2, 1, Rule.BELL_MF, Rule.EINSTEIN_NORM);
        Rule hamacher = new Rule(2, 1, Rule.BELL_MF, Rule.HAMACHER_NORM);
        einstein.executeLayer1(sample());
        hamacher.executeLayer1(sample());
        einstein.executeLayer2();
        hamacher.executeLayer2();
        assertTrue(einstein.w() > 0);
        assertTrue(hamacher.w() > 0);
    }

    private static com.arnas.manfis.data.AnfisInput sample() {
        return new com.arnas.manfis.data.AnfisInput(0, new double[] {0.0, 0.5});
    }

    @Test
    void backpropMultipliesErrorByOtherMemberships() {
        AlgebricProduct product = new AlgebricProduct(2);
        assertArrayEquals(new double[] {0.8, 0.6},
                product.calculateError(2.0, new double[] {0.3, 0.4}), 1e-12);
    }
}
