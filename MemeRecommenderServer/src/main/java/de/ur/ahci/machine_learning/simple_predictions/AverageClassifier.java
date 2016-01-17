package de.ur.ahci.machine_learning.simple_predictions;

import de.ur.ahci.machine_learning.MemeReactionData;

import java.util.*;

/**
 * Created by jonbr on 03.12.2015.
 */
public class AverageClassifier implements Classifier {

    private float threshold;

    public AverageClassifier() {
        threshold = 0.5f;
    }

    @Override
    public int predict(MemeReactionData data) {
        if(data.getFaceListSize() < 30 || SummaryClassifier.getSmileMeasureCount(data) < 15) {
            return - 1;
        }
        return average(data) > threshold? 1 : 0;
    }

    private float average(MemeReactionData data) {
        float avg = 0;

        for(int i = 0; i < data.getFaceListSize(); i++) {
            float smilingProbability = data.getFaceAt(i).getSmilingProbability();
            if(smilingProbability >= 0) avg += smilingProbability;
        }

        return avg / SummaryClassifier.getSmileMeasureCount(data);
    }

}
