package de.ur.ahci.machine_learning;

import de.ur.ahci.machine_learning.simple_predictions.AverageClassifier;
import de.ur.ahci.machine_learning.simple_predictions.Classifier;
import de.ur.ahci.machine_learning.simple_predictions.ClassifierTestResults;
import de.ur.ahci.machine_learning.simple_predictions.SummaryClassifier;

/**
 * Created by jonbr on 13.01.2016.
 */
public class RunTests {

    public static void main(String... args) {
        Classifier cl = new SummaryClassifier();
        new RunTests(cl);
    }

    private Classifier classifier;

    public RunTests(Classifier classifier) {
        this.classifier = classifier;
        ClassifierTestResults results = new ClassifierTestResults();
        new TestDataStreamReader(new TestDataStream("C:\\Users\\jonbr\\Desktop\\utd"))
                .forEveryTestData(data->RunTests.this.test(data, results));

        printResults(results);
    }

    public void test(TestData data, ClassifierTestResults results) {

        data.forEveryMemeReactionData(memeReactionData -> analyzeMemeReactionData(memeReactionData, results));

    }

    private void analyzeMemeReactionData(MemeReactionData memeReactionData, ClassifierTestResults results) {
        int classification = classifier.predict(memeReactionData);
        int realValue = memeReactionData.getSelectedEmotion();

        if(classification == 1 && realValue >= 2) results.increasePosReactionPosClassified();
        else if(classification == 0 && realValue >= 2) results.increasePosReactionNegClassified();
        else if(classification == -1 && realValue >= 2) results.increasePosReactionNotClassified();
        else if(classification == 1 && realValue < 2) results.increaseNegReactionPosClassified();
        else if(classification == 0 && realValue < 2) results.increaseNegReactionNegClassified();
        else if(classification == -1 && realValue < 2) results.increaseNegReactionNotClassified();
    }

    private void printResults(ClassifierTestResults results) {
        int total = results.getNegReactionNegClassified() + results.getNegReactionNotClassified() +
                results.getNegReactionPosClassified() + results.getPosReactionNegClassified() +
                results.getPosReactionPosClassified() + results.getPosReactionNotClassified();

        System.out.println(total + " meme reactions were analyzed.");

        int totalClassified = total - results.getNegReactionNotClassified() - results.getPosReactionNotClassified();

        System.out.println(totalClassified + " meme reactions were classified. (" + percent(totalClassified / (float) total) + "%)");

        int correctlyClassified = results.getNegReactionNegClassified() + results.getPosReactionPosClassified();
        int incorrectlyClassified = results.getNegReactionPosClassified() + results.getPosReactionNegClassified();

        System.out.println(correctlyClassified + " meme reactions were correctly classified. (" + percent(correctlyClassified / (float) totalClassified) + "%)");


        float yesProbability = ((results.getNegReactionPosClassified() + results.getPosReactionPosClassified()) / (float) totalClassified) *
                ((results.getPosReactionPosClassified() + results.getPosReactionNegClassified()) / (float) totalClassified);

        float noProbability = ((results.getNegReactionNegClassified() + results.getPosReactionNegClassified()) / (float) totalClassified) *
                ((results.getNegReactionNegClassified() + results.getNegReactionPosClassified()) / (float) totalClassified);

        float randomAgreement = yesProbability + noProbability;
        float agreement = correctlyClassified / (float) totalClassified;

        System.out.println("Kappa: " + ((agreement - randomAgreement) / (1 - randomAgreement)) + "(random agreement: " + percent(randomAgreement) + "%)");

    }

    private float percent(float f) {
        return (((int) (f * 100)) / (float) 100) * 100;
    }

}
