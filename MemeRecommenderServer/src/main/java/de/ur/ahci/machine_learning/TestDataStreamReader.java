package de.ur.ahci.machine_learning;

import java.util.function.Consumer;

/**
 * Created by jonbr on 17.11.2015.
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
