package de.ur.ahci.machine_learning.simple_predictions;

/**
 * Created by jonbr on 02.12.2015.
 */
public class ClassifierTestResults {
    private int posReactionPosClassified = 0,
            posReactionNegClassified = 0,
            posReactionNotClassified = 0,
            negReactionPosClassified = 0,
            negReactionNegClassified = 0,
            negReactionNotClassified = 0;

    public int getPosReactionPosClassified() {
        return posReactionPosClassified;
    }

    public void increasePosReactionPosClassified() {
        this.posReactionPosClassified++;
    }

    public int getPosReactionNegClassified() {
        return posReactionNegClassified;
    }

    public void increasePosReactionNegClassified() {
        this.posReactionNegClassified++;
    }

    public int getPosReactionNotClassified() {
        return posReactionNotClassified;
    }

    public void increasePosReactionNotClassified() {
        this.posReactionNotClassified++;
    }

    public int getNegReactionPosClassified() {
        return negReactionPosClassified;
    }

    public void increaseNegReactionPosClassified() {
        this.negReactionPosClassified++;
    }

    public int getNegReactionNegClassified() {
        return negReactionNegClassified;
    }

    public void increaseNegReactionNegClassified() {
        this.negReactionNegClassified++;
    }

    public int getNegReactionNotClassified() {
        return negReactionNotClassified;
    }

    public void increaseNegReactionNotClassified() {
        this.negReactionNotClassified++;
    }
}
