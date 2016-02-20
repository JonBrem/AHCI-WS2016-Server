package de.ur.ahci.machine_learning;

import util.FileUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class that facilitates iterating over all meme reactions available for one user.
 */
public class TestData {

    private final int user;
    private MemeReactionStream memeReactionStream;

    public TestData(int user, String fileName) {
        this.user = user;
        memeReactionStream = new MemeReactionStream(new FileUtility(), fileName);
    }

    public void forEveryMemeReactionData(Consumer<MemeReactionData> memeReactionDataConsumer) {
        MemeReactionData data;
        while((data = getNextMemeReactionData()) != null) {
            memeReactionDataConsumer.accept(data);
        }
    }

    private MemeReactionData getNextMemeReactionData() {
        return memeReactionStream.next();
    }

    public int getUserId() {
        return user;
    }

}
