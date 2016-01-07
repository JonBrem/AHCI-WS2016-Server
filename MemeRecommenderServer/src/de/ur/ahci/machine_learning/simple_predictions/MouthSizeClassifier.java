package de.ur.ahci.machine_learning.simple_predictions;

import de.ur.ahci.machine_learning.Face;
import de.ur.ahci.machine_learning.FaceLandmark;
import de.ur.ahci.machine_learning.MemeReactionData;

import java.util.*;

/**
 * Not fully implemented. Works to poorly after first tests (with average and quantile-style inputs) for me to
 * further pursue it at this point.
 */
public class MouthSizeClassifier implements Classifier {

    private Map<String, Float> params;

    public MouthSizeClassifier() {
        params = new HashMap<>();
        params.put("value", 0.9f);
        params.put("threshold", 0.75f);
    }

    @Override
    public Set<String> getParameters() {
        return params.keySet();
    }

    @Override
    public void setParameter(String name, Object value) {
        params.put(name, (Float) value);
    }

    @Override
    public int predict(MemeReactionData data) {
        List<Integer> mouthWidths = getMouthWidthList(data);

        if(mouthWidths.size() == 0) return -1;

        int index = (int) (mouthWidths.size() * params.get("value"));
        if(index >= mouthWidths.size()) return -1;
        return mouthWidths.get(index) >= params.get("threshold")? 1 : 0;
    }

    private List<Integer> getMouthWidthList(MemeReactionData data) {
        List<Integer> mouthWidths = new ArrayList<>();

//        int total = 0;

        for(int i = 0; i < data.getFaceListSize(); i++) {
            Face face = data.getFaceAt(i);
            FaceLandmark leftMouth = face.getLeftMouth(), rightMouth = face.getRightMouth();

            if(leftMouth != null && rightMouth != null) {
                mouthWidths.add((int) (Math.abs(rightMouth.getX() - leftMouth.getX())));
//                total += mouthWidths.get(mouthWidths.size() - 1);
            }
        }
        return mouthWidths;
    }

    @Override
    public String getMorrisXKey() {
        return "avg";
    }

    @Override
    public List<String> getMorrisYKeys() {
        return Arrays.asList("laughter", "no laughter");
    }

    @Override
    public List<String> getAllMorrisXKeys() {
        return Arrays.asList("0.000",
                "" + round(0.1 * params.get("value")),
                "" + round(0.2 * params.get("value")),
                "" + round(0.3 * params.get("value")),
                "" + round(0.4 * params.get("value")),
                "" + round(0.5 * params.get("value")),
                "" + round(0.6 * params.get("value")),
                "" + round(0.7 * params.get("value")),
                "" + round(0.8 * params.get("value")),
                "" + round(0.9 * params.get("value")),
                "" + round(1.0 * params.get("value")),
                "" + round(1.2 * params.get("value")),
                "" + round(1.3 * params.get("value")),
                "" + round(1.4 * params.get("value")),
                "" + round(1.5 * params.get("value")),
                "" + round(1.6 * params.get("value"))
        );
    }

    private String round(double d) {
        String asString = Double.toString(d);
        while(asString.length() < 4) {
            if(asString.contains(",")) asString += ".";
            else asString += "0";
        }
        return asString.substring(0, 4);
    }

    @Override
    public String getMorrisYKeyFor(MemeReactionData data) {
        return (data.getSelectedEmotion() > 1)? "laughter" : "no_laughter";
    }

    @Override
    public String getMorrisXKeyFor(MemeReactionData data) {
        List<Integer> mouthWidths = getMouthWidthList(data);

        int index = (int) (mouthWidths.size() * params.get("value"));
        if(index >= mouthWidths.size()) return "0.000";

        else return "0.000";
    }
}
