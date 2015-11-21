package de.ur.ahci.machine_learning.five_point_summaries;

import de.ur.ahci.machine_learning.MemeReactionData;
import de.ur.ahci.machine_learning.TestData;
import de.ur.ahci.machine_learning.TestDataStream;
import de.ur.ahci.machine_learning.TestDataStreamReader;
import util.Const;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jonbr on 19.11.2015.
 */
public class SimpleSummaryTest {

    private static final String[] EMOTIONCODES = {"neutral/other", "smile", "grin", "laughter", "ROFL"};

    private int notEnoughDataCount;
    private int correctPositive, correctNegative, wrongPositive, wrongNegative;

    public static void main(String... args) {
        new SimpleSummaryTest().run();
    }

    public SimpleSummaryTest() {
        notEnoughDataCount = 0;
        wrongNegative = 0;
        wrongPositive = 0;
        correctPositive = 0;
        correctNegative = 0;
    }

    public void run() {
        new TestDataStreamReader(new TestDataStream("C:\\Users\\jonbr\\Desktop\\utd"))
                .forEveryTestData(this::predictTestData);
        printResults();
    }

    private void predictTestData(TestData testData) {
        testData.forEveryMemeReactionData(this::predict);
    }

    private void predict(MemeReactionData memeReactionData) {
        if(memeReactionData.getFaceListSize() < 30 || getSmileMeasureCount(memeReactionData) < 15) {
            notEnoughDataCount++;
            return;
        }

        Summary<Float> summary = new Summary<>();
        for(int i = 0; i < memeReactionData.getFaceListSize(); i++) {
            summary.add(memeReactionData.getFaceAt(i).getSmilingProbability());
        }
        if(summary.getValueAt(SummaryMachineLearning.FLOAT_COMPARATOR, 0.75f) >= 0.2f) {
            if(memeReactionData.getSelectedEmotion() > 1) {
                correctPositive++;
            } else {
                wrongPositive++;
            }
        } else {
            if(memeReactionData.getSelectedEmotion() < 2) {
                correctNegative++;
            } else {
                wrongNegative++;
            }
        }
    }

    private int getSmileMeasureCount(MemeReactionData memeReactionData) {
        int smiles = 0;
        for(int i = 0; i < memeReactionData.getFaceListSize(); i++) {
            if(memeReactionData.getFaceAt(i).getSmilingProbability() > -1) smiles++;
        }
        return smiles;
    }

    private void printResults() {
        Const.log(Const.LEVEL_VERBOSE, "Correctly classified as positive ratings: " + correctPositive);
        Const.log(Const.LEVEL_VERBOSE, "Correctly classified as negative ratings: " + correctNegative);
        Const.log(Const.LEVEL_VERBOSE, "Incorrectly classified as positive ratings: " + wrongPositive);
        Const.log(Const.LEVEL_VERBOSE, "Incorrectly classified as negative ratings: " + wrongNegative);
        Const.log(Const.LEVEL_VERBOSE, "Error rate: " + ((wrongPositive + wrongNegative) / (float) (wrongNegative + wrongPositive + correctPositive + correctNegative)));
        Const.log(Const.LEVEL_VERBOSE, "There are " + notEnoughDataCount + " cases where no prediction was made due to lack of data.");
    }

}
