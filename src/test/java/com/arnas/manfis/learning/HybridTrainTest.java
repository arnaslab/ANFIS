package com.arnas.manfis.learning;

import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.data.AnfisOutput;
import com.arnas.manfis.network.Network;
import com.arnas.manfis.network.rule.Rule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HybridTrainTest {

    @Test
    void lseThenPremiseGdProducesFiniteRmseOnSine() {
        AnfisInput[] data = sineData(40);
        Network net = new Network(Network.LINEAR_OUTPUT, Rule.BELL_MF, 1, 4, 1);
        OfflineLearner lse = new OfflineLearner(data.length, 4, 1, 1);
        OnlineLearner gd = new OnlineLearner(0.05, 0.2);
        AnfisOutput result = new AnfisOutput(Network.LINEAR_OUTPUT, data.length);

        lse.learn(net, data);
        gd.validate(net, data, result);
        double afterLse = result.getRmse();
        gd.learnPremises(net, data, result);
        lse.learn(net, data);
        gd.validate(net, data, result);
        double afterHybrid = result.getRmse();

        assertTrue(Double.isFinite(afterLse) && Double.isFinite(afterHybrid),
                "hybrid RMSE should stay finite, lse=" + afterLse + " hybrid=" + afterHybrid);
        assertTrue(afterHybrid <= afterLse + 1e-6 || afterHybrid < 0.5,
                "hybrid step should not explode, lse=" + afterLse + " hybrid=" + afterHybrid);
    }

    private static AnfisInput[] sineData(int n) {
        AnfisInput[] data = new AnfisInput[n];
        for (int i = 0; i < n; i++) {
            double x = -1.0 + 2.0 * i / (n - 1);
            double y = Math.sin(Math.PI * x);
            AnfisInput sample = new AnfisInput(i, new double[] {x});
            sample.setTarget(new int[] {0}, new int[] {0}, new double[] {y});
            data[i] = sample;
        }
        return data;
    }
}
