package de.ur.ahci.machine_learning;

import java.util.Arrays;
import java.util.List;

/**
 * model class for storing basically all the data that the google mobile vision api provides about faces.
 */
public class Face {

    private static final String[] KEYS = {"user","img","smiling","leftEyeX","leftEyeY","leftEyeOpen","rightEyeX","rightEyeY","rightEyeOpen","leftMouthX","leftMouthY","rightMouthX","rightMouthY","leftCheekX","leftCheekY","rightCheekX","rightCheekY","noseBaseX","noseBaseY","bottomMouthX","bottomMouthY","faceX","faceY","faceId","eulerY","eulerZ","faceWidth","faceHeight","selectedEmotion","timeStamp"};
    private static final List<String> POSSIBLE_NA = Arrays.asList("leftEyeX","leftEyeY","rightEyeX","rightEyeY",
            "leftMouthX","leftMouthY","rightMouthX","rightMouthY", "leftCheekX","leftCheekY","rightCheekX", "rightCheekY",
            "noseBaseX","noseBaseY","bottomMouthX", "bottomMouthY");

    private FaceLandmark leftEye,
                        rightEye,
                        leftCheek,
                        rightCheek,
                        leftMouth,
                        rightMouth,
                        bottomMouth,
                        noseBase;

    private float x, y, width, height;

    private float smilingProbability, leftEyeOpenProbability, rightEyeOpenProbability;
    private float eulerY, eulerZ;

    public static Face createFromLine(String line) {
        Face face = new Face();

        String[] lineParts = line.split("\t");
        int naCount = 0;
        for(int i = 0; i < KEYS.length; i++) {
            String part = lineParts[i - naCount];

            if(KEYS[i].equals("smiling")) {
                face.setSmilingProbability(Float.parseFloat(part));
            } else if (POSSIBLE_NA.contains(KEYS[i])) {
                if(part.equals("NA")) {
                    i++;
                    naCount++;
                } else {
                    addValue(i, part, face);
                }
            } else if (KEYS[i].equals("faceX")) {
                face.setX(Float.parseFloat(part));
            } else if (KEYS[i].equals("faceY")) {
                face.setY(Float.parseFloat(part));
            } else if (KEYS[i].equals("faceWidth")) {
                face.setWidth(Float.parseFloat(part));
            } else if (KEYS[i].equals("faceHeight")) {
                face.setHeight(Float.parseFloat(part));
            } else if (KEYS[i].equals("eulerY")) {
                face.setEulerY(Float.parseFloat(part));
            } else if (KEYS[i].equals("eulerZ")) {
                face.setEulerZ(Float.parseFloat(part));
            } else if (KEYS[i].equals("leftEyeOpen")) {
                face.setLeftEyeOpenProbability(Float.parseFloat(part));
            } else if (KEYS[i].equals("rightEyeOpen")) {
                face.setRightEyeOpenProbability(Float.parseFloat(part));
            }
        }

        return face;
    }

    private static void addValue(int i, String part, Face face) {
        if(KEYS[i].equals("leftEyeX")) {
            face.setLeftEye(new FaceLandmark());
            face.getLeftEye().setX(Float.parseFloat(part));
        } else if(KEYS[i].equals("leftEyeY")) {
            face.getLeftEye().setY(Float.parseFloat(part));
        } else if(KEYS[i].equals("rightEyeX")) {
            face.setRightEye(new FaceLandmark());
            face.getRightEye().setX(Float.parseFloat(part));
        } else if(KEYS[i].equals("rightEyeY")) {
            face.getRightEye().setY(Float.parseFloat(part));
        } else if(KEYS[i].equals("leftMouthX")) {
            face.setLeftMouth(new FaceLandmark());
            face.getLeftMouth().setX(Float.parseFloat(part));
        } else if(KEYS[i].equals("leftMouthY")) {
            face.getLeftMouth().setY(Float.parseFloat(part));
        } else if(KEYS[i].equals("rightMouthX")) {
            face.setRightMouth(new FaceLandmark());
            face.getRightMouth().setX(Float.parseFloat(part));
        } else if(KEYS[i].equals("rightMouthY")) {
            face.getRightMouth().setY(Float.parseFloat(part));
        } else if(KEYS[i].equals("leftCheekX")) {
            face.setLeftCheek(new FaceLandmark());
            face.getLeftCheek().setX(Float.parseFloat(part));
        } else if(KEYS[i].equals("leftCheekY")) {
            face.getLeftCheek().setY(Float.parseFloat(part));
        } else if(KEYS[i].equals("rightCheekX")) {
            face.setRightCheek(new FaceLandmark());
            face.getRightCheek().setX(Float.parseFloat(part));
        } else if(KEYS[i].equals("rightCheekY")) {
            face.getRightCheek().setY(Float.parseFloat(part));
        } else if(KEYS[i].equals("noseBaseX")) {
            face.setNoseBase(new FaceLandmark());
            face.getNoseBase().setX(Float.parseFloat(part));
        } else if(KEYS[i].equals("noseBaseY")) {
            face.getNoseBase().setY(Float.parseFloat(part));
        } else if(KEYS[i].equals("bottomMouthX")) {
            face.setBottomMouth(new FaceLandmark());
            face.getBottomMouth().setX(Float.parseFloat(part));
        } else if(KEYS[i].equals("bottomMouthY")) {
            face.getBottomMouth().setY(Float.parseFloat(part));
        }

    }

    public FaceLandmark getLeftEye() {
        return leftEye;
    }

    public void setLeftEye(FaceLandmark leftEye) {
        this.leftEye = leftEye;
    }

    public FaceLandmark getRightEye() {
        return rightEye;
    }

    public void setRightEye(FaceLandmark rightEye) {
        this.rightEye = rightEye;
    }

    public FaceLandmark getLeftCheek() {
        return leftCheek;
    }

    public void setLeftCheek(FaceLandmark leftCheek) {
        this.leftCheek = leftCheek;
    }

    public FaceLandmark getRightCheek() {
        return rightCheek;
    }

    public void setRightCheek(FaceLandmark rightCheek) {
        this.rightCheek = rightCheek;
    }

    public FaceLandmark getLeftMouth() {
        return leftMouth;
    }

    public void setLeftMouth(FaceLandmark leftMouth) {
        this.leftMouth = leftMouth;
    }

    public FaceLandmark getRightMouth() {
        return rightMouth;
    }

    public void setRightMouth(FaceLandmark rightMouth) {
        this.rightMouth = rightMouth;
    }

    public FaceLandmark getBottomMouth() {
        return bottomMouth;
    }

    public void setBottomMouth(FaceLandmark bottomMouth) {
        this.bottomMouth = bottomMouth;
    }

    public FaceLandmark getNoseBase() {
        return noseBase;
    }

    public void setNoseBase(FaceLandmark noseBase) {
        this.noseBase = noseBase;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getSmilingProbability() {
        return smilingProbability;
    }

    public void setSmilingProbability(float smilingProbability) {
        this.smilingProbability = smilingProbability;
    }

    public float getLeftEyeOpenProbability() {
        return leftEyeOpenProbability;
    }

    public void setLeftEyeOpenProbability(float leftEyeOpenProbability) {
        this.leftEyeOpenProbability = leftEyeOpenProbability;
    }

    public float getRightEyeOpenProbability() {
        return rightEyeOpenProbability;
    }

    public void setRightEyeOpenProbability(float rightEyeOpenProbability) {
        this.rightEyeOpenProbability = rightEyeOpenProbability;
    }

    public float getEulerY() {
        return eulerY;
    }

    public void setEulerY(float eulerY) {
        this.eulerY = eulerY;
    }

    public float getEulerZ() {
        return eulerZ;
    }

    public void setEulerZ(float eulerZ) {
        this.eulerZ = eulerZ;
    }
}
