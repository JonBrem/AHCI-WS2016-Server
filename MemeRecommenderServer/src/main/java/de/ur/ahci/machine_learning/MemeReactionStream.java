package de.ur.ahci.machine_learning;

import util.FileUtility;

/**
 *
 */
public class MemeReactionStream {

    private FileUtility fileUtility;
    private String file;
    private String buffer;


    public MemeReactionStream(FileUtility fileUtility, String file) {
        this.fileUtility = fileUtility;
        this.file = file;

        buffer = fileUtility.readLine(file);
    }

    public MemeReactionData next() {
        MemeReactionData data = new MemeReactionData();

        if(buffer == null) {
            return null;
        } else {
            data.setSelectedEmotion(getSelectedEmotion(buffer));
            data.setImageNumber(getImageNumber(buffer));

            String line;
            while((line = fileUtility.readLine(file)) != null) {
                if(getImageNumber(line) != data.getImageNumber()) {
                    buffer = line;
                    break;
                } else {
                    data.addRecognizedFace(Face.createFromLine(buffer));
                    buffer = line;
                }
            }

            if(line == null) {
                data.addRecognizedFace(Face.createFromLine(buffer));
                buffer = null;
                fileUtility.closeReader(file);
            }
        }

        return data;
    }

    private int getImageNumber(String line) {
        String[] split = line.split("\t");
        return Integer.parseInt(split[1]);
    }

    private int getSelectedEmotion(String line) {
        String[] split = line.split("\t");
        if(split.length >= 2) {
            return Integer.parseInt(split[split.length - 2]);
        } else {
            return -1;
        }
    }

}
