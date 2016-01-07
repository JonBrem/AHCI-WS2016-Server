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

    @Override
    public Set<String> getParameters() {
        Set<String> params = new HashSet<>();
        params.add("threshold");
        return params;
    }

    @Override
    public void setParameter(String name, Object value) {
        threshold = (Float) value;
    }

    @Override
    public String getMorrisXKey() {
        return "value";
    }

    @Override
    public List<String> getMorrisYKeys() {
        return Arrays.asList("laughter", "no_laughter");
    }

    @Override
    public List<String> getAllMorrisXKeys() {
        return Arrays.asList("-1.0", "0.0", "0.05", "0.1", "0.15", "0.2", "0.25", "0.3", "0.35", "0.4", "0.45", "0.5",
                "0.55", "0.6", "0.65", "0.7", "0.75", "0.8", "0.85", "0.9", "0.95", "1.0");
    }

    @Override
    public String getMorrisYKeyFor(MemeReactionData data) {
        return (data.getSelectedEmotion() > 1)? "laughter" : "no_laughter";
    }

    @Override
    public String getMorrisXKeyFor(MemeReactionData data) {
        if(SummaryClassifier.getSmileMeasureCount(data) == 0) return "0.0";
        float avg = average(data);

        avg = Math.round(avg * 100 - avg * 100 % 5) / (float) 100;
        return String.valueOf(avg);
    }
}
