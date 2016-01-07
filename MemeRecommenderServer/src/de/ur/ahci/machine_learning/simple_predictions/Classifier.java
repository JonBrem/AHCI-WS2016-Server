package de.ur.ahci.machine_learning.simple_predictions;

import de.ur.ahci.machine_learning.MemeReactionData;

import java.util.List;
import java.util.Set;

/**
 * Created by jonbr on 30.11.2015.
 */
public interface Classifier {

    int predict(MemeReactionData data);
    Set<String> getParameters();
    void setParameter(String name, Object value);

    String getMorrisXKey();
    List<String> getMorrisYKeys();
    List<String> getAllMorrisXKeys();

    String getMorrisYKeyFor(MemeReactionData data);
    String getMorrisXKeyFor(MemeReactionData data);
}
