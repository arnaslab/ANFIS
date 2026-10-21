package com.arnas.manfis.learning;

import com.arnas.manfis.data.AnfisInput;
import com.arnas.manfis.data.AnfisOutput;
import com.arnas.manfis.network.Network;

/**
 *
 * @author arif
 */
public class Predictor {
    
    public void predict(Network net, AnfisInput[] dataPrediction, AnfisOutput result) {
        for (AnfisInput data1 : dataPrediction) {
            net.execute(data1);
            result.setResult(data1.index(), net.getResult());
        }
    }
    
}
