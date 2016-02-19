package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.*;

/**
 * UserPreferences show how much a user likes images of a certain tag.
 */
public class UserPreferences {

    public static final String ES_INDEX_NAME = "user_pref";
    public static final String ES_USER_ID = "user_id";
    public static final String ES_LAST_CALCULATED = "last_calculated";
    /** <strong>NOT a key!!</strong> End part of some keys in this index!! */
    public static final String ES_APPENDIX_TOTAL = "_total";

    private Map<String, TagPreference> userRatings;

    public UserPreferences() {
        userRatings = new HashMap<>();
    }

    /**
     * @param userId the user's id
     * @param es elastic search connection
     */
    public void load(String userId, ElasticSearchContextListener es) {
        SearchResponse response = es.searchrequest(ES_INDEX_NAME, QueryBuilders.matchQuery(ES_USER_ID, userId), 0, 1).actionGet();

        if(response.getHits().totalHits() > 0) {
            userRatings = new HashMap<>();
            SearchHit hit = response.getHits().getAt(0);
            Map<String, Object> source = hit.getSource();

            for(String key : source.keySet()) {
                if(!key.endsWith(ES_APPENDIX_TOTAL) && !key.equals(ES_LAST_CALCULATED)) {
                    buildPreferenceFromStorage(source, key);
                }
            }
        }
    }

    /**
     * @param searchHitSource ElasticSearch source item for this index
     * @param key Key of the current Tag
     */
    private void buildPreferenceFromStorage(Map<String, Object> searchHitSource, String key) {
        TagPreference pref = new TagPreference();
        pref.totalRatingsCounter = (int) searchHitSource.get(key + ES_APPENDIX_TOTAL);
        pref.goodRatingsCounter = (int) (pref.totalRatingsCounter * (float) searchHitSource.get(key));
        userRatings.put(key, new TagPreference());
    }

    /**
     * Build / update user preferences from the user's ratings
     * @param userId the user's id
     * @param es elastic search connection
     */
    public void build(String userId, ElasticSearchContextListener es) {
        int start = 0;
        int atATime = 5000; // just so the system does not crash if there are too many ratings
        while(true) {
            SearchResponse response = es.searchrequest(Rating.RATINGS_INDEX, QueryBuilders.matchQuery(ES_USER_ID, userId), start, atATime).actionGet();
            buildPreferences(response.getHits(), es);

            if(response.getHits().totalHits() < atATime) break;
            start += atATime;
        }
        storePreferences(userId, es);
    }

    /**
     * Stores the preferences; checks if some prefs for the specified user exist (will override in that case)
     * @param userId the user's id
     * @param es elastic search connection
     */
    private synchronized void storePreferences(String userId, ElasticSearchContextListener es) {
        Map<String, Object> data = getMapForEsFromUserRatings(userId);

        String oldId = getOldIdIfExists(userId, es);

        if (oldId != null) {
            es.updateRequest(ES_INDEX_NAME, oldId, data);
        } else {
            es.indexRequest(ES_INDEX_NAME, data);
        }
    }

    /**
     * If preferences for this user already exist, this returns their id.
     * @param userId the user's id
     * @param es elastic search connection
     * @return The Id if prefs for this user exist or null
     */
    private String getOldIdIfExists(String userId, ElasticSearchContextListener es) {
        String oldId = null;

        SearchResponse response = es.searchrequest(ES_INDEX_NAME, QueryBuilders.matchQuery(ES_USER_ID, userId), 0, 1).actionGet();
        if(response.getHits().totalHits() > 0) {
            oldId = response.getHits().getAt(0).id();
        }
        return oldId;
    }

    /**
     * Converts the user ratings map to a map that can be stored in elastic search. adds timestamp.
     * @param userId the user's id
     * @return a map containing the Tag IDs (and Tag IDs + "_total" for every one of them) as keys and the likelihood
     *          that the user likes memes with that tag (or total ratings the user has made for that tag)
     *          & "last_calculated", containing the current system time.
     */
    private Map<String, Object> getMapForEsFromUserRatings(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put(ES_USER_ID, userId);
        for(String tagId : userRatings.keySet()) {
            data.put(tagId, userRatings.get(tagId).getValue());
            data.put(tagId + ES_APPENDIX_TOTAL, userRatings.get(tagId).totalRatingsCounter);
        }
        data.put(ES_LAST_CALCULATED, System.currentTimeMillis());

        return data;
    }

    /**
     * Decomposition method. Called from "build" with (potentially) a subset of the results.
     * @param hits The user's ratings as SearchHits
     * @param es Elastic Search Connection
     */
    private void buildPreferences(SearchHits hits, ElasticSearchContextListener es) {
        for(int i = 0; i < hits.totalHits(); i++) {
            Map<String, Object> hit = hits.getAt(i).getSource();
            int rating = Integer.parseInt((String) hit.get(Rating.ES_VALUE));

            Meme m = Meme.load((String) hit.get(Rating.ES_MEME_ID), es);
            List<String> tagIDs = (m == null)? null : m.getTags();
            if(tagIDs == null) return;
            for(String tagId : tagIDs) {
                handleRating(tagId, rating);
            }
        }
    }

    /**
     * Increases the counter (and potentially the "user liked this tag"-counter) for the tag, depending on the rating.
     * @param tagId the tag's id
     * @param rating the rating value (1 => user liked this)
     */
    private void handleRating(String tagId, int rating) {
        if(!this.userRatings.containsKey(tagId)) {
            this.userRatings.put(tagId, new TagPreference());
        }

        TagPreference tagPreference = this.userRatings.get(tagId);
        tagPreference.increaseTotalRatingsCounter();
        if(rating == 1) {
            tagPreference.increaseGoodRatingsCounter();
        }
    }

    /**
     * @return a map of Tag IDs and the corresponding TagPreferences for the user.
     */
    public Map<String, TagPreference> getUserRatings() {
        return userRatings;
    }

    public class TagPreference {
        private int totalRatingsCounter;
        private int goodRatingsCounter;

        public TagPreference() {
            totalRatingsCounter = 0;
            goodRatingsCounter = 0;
        }

        private void increaseTotalRatingsCounter() {
            totalRatingsCounter++;
        }

        private void increaseGoodRatingsCounter() {
            goodRatingsCounter++;
        }


        public float getValue() {
            return this.goodRatingsCounter / (float) this.totalRatingsCounter;
        }

    }


}
