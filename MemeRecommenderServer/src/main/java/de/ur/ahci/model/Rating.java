package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;

import java.util.HashMap;
import java.util.Map;

/*
@TODO figure out if we still need this (because of elasticsearch, there is no need to store model files that are mere data classes)
 */
public class Rating {

    public static final String RATINGS_INDEX = "ratings";

    public static String save(ElasticSearchContextListener es, String userId, String memeId, String value) {
        try {
            Map<String, Object> data = getRatingsObjectMap(userId, memeId, value);
            IndexResponse response = es.indexRequest("ratings", data).actionGet();
            return response.getId();
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, Object> getRatingsObjectMap(String userId, String memeId, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("meme_id", memeId);
        data.put("value", value);
        data.put("rating_time", System.currentTimeMillis());
        return data;
    }

}
