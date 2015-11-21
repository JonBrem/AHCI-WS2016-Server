package de.ur.ahci.machine_learning.five_point_summaries;

import de.ur.ahci.machine_learning.*;
import util.Const;

import java.util.ArrayList;
import java.util.List;

public class SimpleRiseTest {

    private static final float HIGH_AREA_TOTAL_THRESHOLD = 6;
    public static final double MIN_VALUE_REALLY_HAPPY = 0.4;
    private static final int SMOOTHING = 1;
    private static final double LATEST_LAUGHTER_REACTION = 0.56;

    public static void main(String... args) {
        new SimpleRiseTest().run();
    }

    private int notEnoughDataCount;
    private int wrongNegative, wrongPositive, correctPositive, correctNegative;

    public SimpleRiseTest() {
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

    private void predict(MemeReactionData data) {
        List<Float> smilingValues = getNonNegativeSmilingValues(data);
        if(smilingValues.size() < 15) {
            notEnoughDataCount++;
        }

        smoothe(smilingValues, SMOOTHING);
        if(isRising(smilingValues)) {
            if(data.getSelectedEmotion() >= 2) {
                correctPositive++;
            } else {
                wrongPositive++;
            }
        } else {
            if(data.getSelectedEmotion() >= 2) {
                wrongNegative++;
            } else {
                correctNegative++;
            }
        }
    }

    // @TODO needs a more sophisticated approach!!
    private boolean isRising(List<Float> smilingValues) {
        int i = highAreaCenter(smilingValues);
        if(i >= smilingValues.size() * LATEST_LAUGHTER_REACTION) {
            return true;
        } else {
            return false;
        }
    }

    private int highAreaCenter(List<Float> smilingValues) {
        for(int i = smilingValues.size() - 1; i > 0; i--) {
            if(smilingValues.get(i) >= MIN_VALUE_REALLY_HAPPY) {
                float highAreaTotal = smilingValues.get(i);
                int highAreSize = 1;

                for(int j = i; j >= 0; j--) {
                    if(smilingValues.get(j) >= MIN_VALUE_REALLY_HAPPY) {
                        highAreaTotal += smilingValues.get(j);
                        highAreSize++;
                    } else {
                        break;
                    }
                }

                if(highAreaTotal >= HIGH_AREA_TOTAL_THRESHOLD) {
                    return i - highAreSize / 2;
                } else {
                    continue;
                }
            }
        }

        return -1;
    }

    private void smoothe(List<Float> smilingValues, int smoothing) {
        for(int i = 0; i < smilingValues.size(); i++) {
            if(smoothing != 0) {
                int smoothingStart = (i - smoothing >= 0)? i - smoothing : 0;
                int smoothingEnd = (i + smoothing < smilingValues.size())? i + smoothing : smilingValues.size() - 1;

                float avg = 0;
                for(int j = smoothingStart; j < smoothingEnd; j++) {
                    avg += smilingValues.get(j);
                }
                smilingValues.set(i, avg / (smoothingEnd - smoothingStart));
            }
        }
    }

    private List<Float> getNonNegativeSmilingValues(MemeReactionData data) {
        List<Float> smilingValues = new ArrayList<>();

        for(int i = 0; i < data.getFaceListSize(); i++) {
            Face face = data.getFaceAt(i);
            if(face.getSmilingProbability() >= 0) {
                smilingValues.add(face.getSmilingProbability());
            }
        }

        return smilingValues;
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
