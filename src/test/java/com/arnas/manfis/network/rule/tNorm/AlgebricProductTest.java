package com.arnas.manfis.network.rule.tNorm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AlgebricProductTest {

    @Test
    void firingStrengthIsProductNotSum() {
        AlgebricProduct product = new AlgebricProduct(2);
        assertEquals(0.12, product.computeNorm(new double[] {0.3, 0.4}), 1e-12);
    }

    @Test
    void backpropMultipliesErrorByOtherMemberships() {
        AlgebricProduct product = new AlgebricProduct(2);
        assertArrayEquals(new double[] {0.8, 0.6},
                product.calculateError(2.0, new double[] {0.3, 0.4}), 1e-12);
    }
}
