package de.ur.ahci.machine_learning.simple_predictions;

import de.ur.ahci.machine_learning.*;

import java.util.*;

public class SummaryClassifier implements Classifier {

    private float quantile = 0.7f;
    private float value = 0.2f;

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
        if((summary.getValueAt(SummaryMachineLearning.FLOAT_COMPARATOR, 0.7f) >= 0.3f ||
                summary.getValueAt(SummaryMachineLearning.FLOAT_COMPARATOR, 0.95f) >= 0.8f)
                && someHighValuesInSecondHalf(memeReactionData)
//                && summary.getValueAt(SummaryMachineLearning.FLOAT_COMPARATOR, 0.9f) >= 0.4f
                ) {
            return 1;
        } else {
            return 0;
        }
    }

    private boolean someHighValuesInSecondHalf(MemeReactionData memeReactionData) {
        int highValueCount = 0;
        for(int i = memeReactionData.getFaceListSize() / 2; i < memeReactionData.getFaceListSize(); i++) {
            if(memeReactionData.getFaceAt(i).getSmilingProbability() >= 0.6f) highValueCount++;
        }

        return highValueCount >= 6;
    }

    private Summary<Float> getFloatSummary(MemeReactionData memeReactionData) {
        Summary<Float> summary = new Summary<>();
        for(int i = (int) (0.2 * memeReactionData.getFaceListSize()); i < memeReactionData.getFaceListSize(); i++) {
            summary.add(memeReactionData.getFaceAt(i).getSmilingProbability());
        }
        return summary;
    }

    public static int getSmileMeasureCount(MemeReactionData memeReactionData) {
        int smiles = 0;
        for(int i = 0; i < memeReactionData.getFaceListSize(); i++) {
            if(memeReactionData.getFaceAt(i).getSmilingProbability() > -1) smiles++;
        }
        return smiles;
    }

}
