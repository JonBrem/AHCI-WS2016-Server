package de.ur.ahci.machine_learning.simple_predictions;

import de.ur.ahci.machine_learning.*;

import java.util.*;

public class SummaryClassifier implements Classifier {

    private static final String[] EMOTIONCODES = {"neutral/other", "smile", "grin", "laughter", "ROFL"};

    private static HashMap<String, Object> params;

    static {
        params = new HashMap<>();
        params.put("quantile", 0.75f);
        params.put("value", 0.2f);
    }

    public SummaryClassifier() {
    }

    public void run() {
        new TestDataStreamReader(new TestDataStream("C:\\Users\\jonbr\\Desktop\\utd"))
                .forEveryTestData(this::predictTestData);
//        printResults();
    }

    private void predictTestData(TestData testData) {
        testData.forEveryMemeReactionData(this::predict);
    }

    public int predict(MemeReactionData memeReactionData) {
        if(memeReactionData.getFaceListSize() < 30 || getSmileMeasureCount(memeReactionData) < 15) {
            return - 1;
        }

        Summary<Float> summary = getFloatSummary(memeReactionData);
        if(summary.getValueAt(SummaryMachineLearning.FLOAT_COMPARATOR, (Float) params.get("quantile"))
                >= (Float) params.get("value")) {
            return 1;
        } else {
            return 0;
        }
    }

    private Summary<Float> getFloatSummary(MemeReactionData memeReactionData) {
        Summary<Float> summary = new Summary<>();
        for(int i = 0; i < memeReactionData.getFaceListSize(); i++) {
            summary.add(memeReactionData.getFaceAt(i).getSmilingProbability());
        }
        return summary;
    }

    @Override
    public Set<String> getParameters() {
        return params.keySet();
    }

    @Override
    public void setParameter(String name, Object value) {
        params.put(name, value);
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
        return (data.getSelectedEmotion() > 1) ? "laughter" : "no_laughter";
    }

    @Override
    public String getMorrisXKeyFor(MemeReactionData data) {
        Summary<Float> summary = getFloatSummary(data);
        Float value = summary.getValueAt(SummaryMachineLearning.FLOAT_COMPARATOR, (Float) params.get("quantile"));

        if(value == null) return "-1.0";

        float val = Math.round(value * 100 - value * 100 % 5) / (float) 100;
        return String.valueOf(val);
    }

    public static int getSmileMeasureCount(MemeReactionData memeReactionData) {
        int smiles = 0;
        for(int i = 0; i < memeReactionData.getFaceListSize(); i++) {
            if(memeReactionData.getFaceAt(i).getSmilingProbability() > -1) smiles++;
        }
        return smiles;
    }

}
