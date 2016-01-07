package de.ur.ahci.machine_learning;

import de.ur.ahci.machine_learning.simple_predictions.ClassifierTestResults;

import java.util.*;

/**
 * Created by jonbr on 01.12.2015.
 */
public class TestResults {

    private Map<String, ClassifierTestResults> results;

    private Map<String, Map<String, ClassifierTestResults>> resultsByParticipant;
    private Map<String, Map<String, ClassifierTestResults>> resultsByMeme;

    public TestResults() {
        results = new HashMap<>();
        resultsByParticipant = new HashMap<>();
        resultsByMeme = new HashMap<>();
    }

    public void addPredictionForNegativeReaction(String classifierName, int prediction, String participantId, String memeId) {
        ensureMapEntriesExist(classifierName, participantId, memeId);

        if (prediction == -1) {
            results.get(classifierName).increaseNegReactionNotClassified();
            resultsByParticipant.get(classifierName).get(participantId).increaseNegReactionNegClassified();
            resultsByMeme.get(classifierName).get(memeId).increaseNegReactionNotClassified();
        } else if (prediction == 1) {
            results.get(classifierName).increaseNegReactionPosClassified();
            resultsByParticipant.get(classifierName).get(participantId).increaseNegReactionPosClassified();
            resultsByMeme.get(classifierName).get(memeId).increaseNegReactionPosClassified();
        } else if (prediction == 0) {
            results.get(classifierName).increaseNegReactionNegClassified();
            resultsByParticipant.get(classifierName).get(participantId).increaseNegReactionNegClassified();
            resultsByMeme.get(classifierName).get(memeId).increaseNegReactionNegClassified();
        }
    }

    public void addPredictionForPositiveReaction(String classifierName, int prediction, String participantId, String memeId) {
        ensureMapEntriesExist(classifierName, participantId, memeId);

        if (prediction == -1) {
            results.get(classifierName).increasePosReactionNotClassified();
            resultsByParticipant.get(classifierName).get(participantId).increasePosReactionNotClassified();
            resultsByMeme.get(classifierName).get(memeId).increasePosReactionNotClassified();

        } else if (prediction == 1) {
            results.get(classifierName).increasePosReactionPosClassified();
            resultsByParticipant.get(classifierName).get(participantId).increasePosReactionPosClassified();
            resultsByMeme.get(classifierName).get(memeId).increasePosReactionPosClassified();
        } else if (prediction == 0) {
            results.get(classifierName).increasePosReactionNegClassified();
            resultsByParticipant.get(classifierName).get(participantId).increasePosReactionNegClassified();
            resultsByMeme.get(classifierName).get(memeId).increasePosReactionNegClassified();
        }
    }

    private void ensureMapEntriesExist(String classifierName, String participantId, String memeId) {
        if (!results.containsKey(classifierName)) {
            results.put(classifierName, new ClassifierTestResults());
        }

        if (!resultsByParticipant.containsKey(classifierName)) {
            resultsByParticipant.put(classifierName, new HashMap<>());
        }
        if (!resultsByParticipant.get(classifierName).containsKey(participantId)) {
            resultsByParticipant.get(classifierName).put(participantId, new ClassifierTestResults());
        }

        if (!resultsByMeme.containsKey(classifierName)) {
            resultsByMeme.put(classifierName, new HashMap<>());
        }
        if (!resultsByMeme.get(classifierName).containsKey(memeId)) {
            resultsByMeme.get(classifierName).put(memeId, new ClassifierTestResults());
        }
    }

    public List<List<ClassifierTestResults>> getResultsForClassifier(String classifier) {
        List<List<ClassifierTestResults>> toReturn = new ArrayList<>();

        toReturn.add(new ArrayList<>());
        toReturn.add(new ArrayList<>());
        toReturn.add(new ArrayList<>());

        toReturn.get(0).add(results.get(classifier));

        List<String> userIds = new ArrayList<>(resultsByParticipant.get(classifier).keySet());
        Collections.sort(userIds);

        for(String userId : userIds) {
            toReturn.get(1).add(resultsByParticipant.get(classifier).get(userId));
        }

        List<String> memeIds = new ArrayList<>(resultsByMeme.get(classifier).keySet());
        Collections.sort(memeIds);

        for(String memeId : memeIds) {
            toReturn.get(2).add(resultsByMeme.get(classifier).get(memeId));
        }


        return toReturn;
    }

}
