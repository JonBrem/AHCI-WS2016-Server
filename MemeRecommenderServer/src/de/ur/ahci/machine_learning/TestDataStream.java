package de.ur.ahci.machine_learning;

import java.io.File;

/**
 * Created by jonbr on 17.11.2015.
 */
public class TestDataStream {

    private String folder;
    private int currentIndex;

    public TestDataStream(String folder) {
        this.folder = folder;
        currentIndex = -1;
    }

    public TestData next() {
        File[] files = new File(folder).listFiles();
        if(files.length > currentIndex + 1) {
            currentIndex++;
            return createFromFile(files[currentIndex]);
        } else {
            return null;
        }
    }

    private TestData createFromFile(File file) {
        TestData testData = new TestData(getUserId(file), file.getAbsolutePath());
        return testData;
    }

    private int getUserId(File file) {
        String fileName = file.getName();
        int id = Integer.parseInt(fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf(".")));
        return id;
    }

}
