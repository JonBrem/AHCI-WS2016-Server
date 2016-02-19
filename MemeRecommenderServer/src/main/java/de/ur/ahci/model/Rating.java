package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.index.IndexResponse;

import java.util.HashMap;
import java.util.Map;

/*
@TODO figure out if we still need this (because of elasticsearch, there is no need to store model files that are mere data classes)
 */
public class Rating {

    public static final String RATINGS_INDEX = "ratings";
    public static final String ES_USER_ID = "user_id";
    public static final String ES_MEME_ID = "meme_id";
    public static final String ES_VALUE = "value";
    public static final String ES_RATING_TIME = "rating_time";

    public static String save(ElasticSearchContextListener es, String userId, String memeId, String value) {
        try {
            Map<String, Object> data = getRatingsObjectMap(userId, memeId, value);
            IndexResponse response = es.indexRequest(RATINGS_INDEX, data).actionGet();
            return response.getId();
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, Object> getRatingsObjectMap(String userId, String memeId, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(ES_USER_ID, userId);
        data.put(ES_MEME_ID, memeId);
        data.put(ES_VALUE, value);
        data.put(ES_RATING_TIME, System.currentTimeMillis());
        return data;
    }

}
