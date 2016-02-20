package de.ur.ahci.machine_learning;

import java.util.function.Consumer;

/**
 * The TestDataStreamReader class makes it easy to iterate over all the data we collected.
 */
public class TestDataStreamReader {

    private final TestDataStream testDataStream;

    public TestDataStreamReader(TestDataStream testDataStream) {
        this.testDataStream = testDataStream;
    }

    public void forEveryTestData(Consumer<TestData> testDataConsumer) {
        TestData testData;
        while((testData = testDataStream.next()) != null) {
            testDataConsumer.accept(testData);
        }
    }

}
