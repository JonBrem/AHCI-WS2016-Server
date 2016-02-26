package de.ur.ahci.model;

public class MemeRecommendation {

    public static final int REASON_RANDOM = 0;
    public static final int REASON_TAGS = 1;
    public static final int REASON_SIMILAR_USERS = 2;

    private Meme meme;
    private int reason;
    private String explanation;

    public MemeRecommendation(Meme meme, int reason, String explanation) {
        setMeme(meme);
        setReason(reason);
        setExplanation(explanation);
    }

    public Meme getMeme() {
        return meme;
    }

    public void setMeme(Meme meme) {
        this.meme = meme;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
