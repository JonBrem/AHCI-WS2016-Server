package de.ur.ahci.machine_learning.class_test;

import de.ur.ahci.machine_learning.*;
import util.Const;

/**
 * Substitute for JUnit-Tests (used this class in order to be more flexible
 * & so there is no need for mock-objects because, well, if it works, it works anyway).
 * Also shows how to get access to the reaction data.
 */
public class ClassTest {

    public static void main(String... args) {
        new TestDataStreamReader(new TestDataStream("C:\\Users\\jonbr\\Desktop\\utd"))
                .forEveryTestData(testData -> printTestData(testData));
    }

    private static void printTestData(TestData testData) {
        Const.log(Const.LEVEL_DEBUG, "user:" + testData.getUserId());
        if(testData.getUserId() == 50) {
            testData.forEveryMemeReactionData(memeReactionData -> printMemeReactionData(memeReactionData));
        }
    }

    private static void printMemeReactionData(MemeReactionData memeReactionData) {
        Const.log(Const.LEVEL_DEBUG, "\timage:" + memeReactionData.getImageNumber());
        if(memeReactionData.getImageNumber() == 0) {
            Const.log(Const.LEVEL_DEBUG, "\t\tselectedEmotion: " + memeReactionData.getSelectedEmotion());
            if(memeReactionData.getFaceListSize() > 0) {
                Face face = memeReactionData.getFaceAt(0);
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0LeftEye: " + face.getLeftEye().getX() + "\t" + face.getLeftEye().getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0RightEye: " + face.getRightEye().getX() + "\t" + face.getRightEye().getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0LeftCheek: " + face.getLeftCheek().getX() + "\t" + face.getLeftCheek().getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0RightCheek: " + face.getRightCheek().getX() + "\t" + face.getRightCheek().getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0LeftMouth: " + face.getLeftMouth().getX() + "\t" + face.getLeftMouth().getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0BottomMouth: " + face.getBottomMouth().getX() + "\t" + face.getBottomMouth().getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0RightMouth: " + face.getRightMouth().getX() + "\t" + face.getRightMouth().getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0NoseBase: " + face.getNoseBase().getX() + "\t" + face.getNoseBase().getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0FacePosition: " + face.getX() + "\t" + face.getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0FaceDimensions: " + face.getWidth() + "\t" + face.getHeight());

                Const.log(Const.LEVEL_DEBUG, "\t\tfame0Probabilities: " + face.getLeftEyeOpenProbability() + "\t" + face.getRightEyeOpenProbability() + "\t" + face.getSmilingProbability());
                Const.log(Const.LEVEL_DEBUG, "\t\tframe0Euler: " + face.getEulerY() + "\t" + face.getEulerZ());

                Const.log(Const.LEVEL_DEBUG, "\t\t");

                face = memeReactionData.getFaceAt(90);
                Const.log(Const.LEVEL_DEBUG, "\t\tframeWithRightMouthNull: " + face.getRightMouth());
                Const.log(Const.LEVEL_DEBUG, "\t\tframeProbabilities: " + face.getLeftEyeOpenProbability() + "\t" + face.getRightEyeOpenProbability() + "\t" + face.getSmilingProbability());
                Const.log(Const.LEVEL_DEBUG, "\t\tframeEuler: " + face.getEulerY() + "\t" + face.getEulerZ());
                Const.log(Const.LEVEL_DEBUG, "\t\tframeFacePosition: " + face.getX() + "\t" + face.getY());
                Const.log(Const.LEVEL_DEBUG, "\t\tframeFaceDimensions: " + face.getWidth() + "\t" + face.getHeight());
            }
        }
    }

}
