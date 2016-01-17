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

    private float threshold = 0.75f;
    private float value = 0.9f;

    public MouthSizeClassifier() {
    }

    @Override
    public int predict(MemeReactionData data) {
        List<Integer> mouthWidths = getMouthWidthList(data);

        if(mouthWidths.size() == 0) return -1;

        int index = (int) (mouthWidths.size() * value);
        if(index >= mouthWidths.size()) return -1;
        return mouthWidths.get(index) >= threshold? 1 : 0;
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
}
