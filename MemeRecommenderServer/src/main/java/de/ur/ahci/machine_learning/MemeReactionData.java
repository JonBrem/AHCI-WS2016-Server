package de.ur.ahci.machine_learning;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonbr on 17.11.2015.
 */
public class MemeReactionData {

    private List<Face> recognizedFaces;
    private int selectedEmotion;
    private int imageNumber;

    public MemeReactionData() {
        recognizedFaces = new ArrayList<>();
    }

    public void addRecognizedFace(Face f) {
        recognizedFaces.add(f);
    }

    public int getFaceListSize() {
        return recognizedFaces.size();
    }

    public Face getFaceAt(int index) {
        return recognizedFaces.get(index);
    }

    public int getSelectedEmotion() {
        return selectedEmotion;
    }

    public void setSelectedEmotion(int selectedEmotion) {
        this.selectedEmotion = selectedEmotion;
    }

    public int getImageNumber() {
        return imageNumber;
    }

    public void setImageNumber(int imageNumber) {
        this.imageNumber = imageNumber;
    }
}
