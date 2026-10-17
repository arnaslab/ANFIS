package com.arnas.manfis.learning;

import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.data.AnfisOutput;
import com.arnas.manfis.network.Network;
import com.arnas.manfis.network.rule.Rule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SineTrainTest {

    @Test
    void productTNormTrainingRmseDropsOnSine() {
        AnfisInput[] data = sineData(40);
        Network net = new Network(Network.LINEAR_OUTPUT, Rule.BELL_MF, 1, 4, 1);
        OnlineLearner learner = new OnlineLearner(0.05, 0.2);
        AnfisOutput result = new AnfisOutput(Network.LINEAR_OUTPUT, data.length);

        learner.learn(net, data, result);
        double first = result.getRmse();
        for (int epoch = 1; epoch < 40; epoch++) {
            learner.learn(net, data, result);
        }
        double last = result.getRmse();

        assertTrue(Double.isFinite(first) && Double.isFinite(last),
                "RMSE should stay finite, first=" + first + " last=" + last);
        assertTrue(last < first,
                "training RMSE should drop with product T-norm, first=" + first + " last=" + last);
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
