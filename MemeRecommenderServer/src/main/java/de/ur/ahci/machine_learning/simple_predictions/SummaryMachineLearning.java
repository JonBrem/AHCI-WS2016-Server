package de.ur.ahci.machine_learning.simple_predictions;

import de.ur.ahci.machine_learning.MemeReactionData;
import de.ur.ahci.machine_learning.TestData;
import de.ur.ahci.machine_learning.TestDataStream;
import de.ur.ahci.machine_learning.TestDataStreamReader;
import util.Const;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 
 */
public class SummaryMachineLearning {

    private static final String[] EMOTIONCODES = {"neutral/other", "smile", "grin", "laughter", "ROFL"};

    public static final Comparator<Float> FLOAT_COMPARATOR = new Comparator<Float>() {
        @Override
        public int compare(Float o1, Float o2) {
            return o1.compareTo(o2);
        }
    };

    private List<Summary<Float>> summaryList;

    public static void main(String... args) {
        new SummaryMachineLearning().run();
    }

    public SummaryMachineLearning() {
        summaryList = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            summaryList.add(new Summary<Float>());
        }
    }

    public void run() {
        new TestDataStreamReader(new TestDataStream("C:\\Users\\jonbr\\Desktop\\utd"))
                .forEveryTestData(this::collectData);
        printSummaries();
    }

    private void printSummaries() {
        for(int i = 0; i < 5; i++) {
            printSummary(i, summaryList.get(i));
        }
    }

    private void printSummary(int index, Summary<Float> floatSummary) {
        String summaryString = "Summary for emotion " + getEmotionAt(index) + ":\t" + floatSummary.getSummary(FLOAT_COMPARATOR,
                new float[]{0.0f, 0.1f, 0.25f, 0.5f, 0.75f, 0.9f, 1.0f});
        Const.log(Const.LEVEL_VERBOSE, summaryString);
    }

    private String getEmotionAt(int index) {
        return EMOTIONCODES[index];
    }

    private void collectData(TestData testData) {
        testData.forEveryMemeReactionData(this::addDataToSummaries);
    }

    private void addDataToSummaries(MemeReactionData memeReactionData) {
        Summary<Float> summary = summaryList.get(memeReactionData.getSelectedEmotion());
        for(int i = 0; i < memeReactionData.getFaceListSize(); i++) {
            summary.add(memeReactionData.getFaceAt(i).getSmilingProbability());
        }
    }

}
