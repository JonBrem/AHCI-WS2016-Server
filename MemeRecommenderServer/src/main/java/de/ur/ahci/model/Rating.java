package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.HashMap;
import java.util.Map;

public class Rating {

    public static final String INDEX_NAME = "ratings";
    public static final String ES_USER_ID = "user_id";
    public static final String ES_MEME_ID = "meme_id";
    public static final String ES_VALUE = "value";
    public static final String ES_STATUS = "status";
    public static final String ES_RATING_TIME = "rating_time";

    /** not a field in the ES objects but instead the value for {@link #ES_STATUS} if the item has been rated. */
    public static final String ES_STATUS_RATED = "rated";
    /** not a field in the ES objects but instead the value for {@link #ES_STATUS} if the item has been rated. */
    public static final String ES_STATUS_PENDING = "pending";

    public static String save(ElasticSearchContextListener es, String userId, String memeId, String value) {
        try {
            String pendingItemId = getPendingItemId(es, userId, memeId);
            Map<String, Object> data = getRatingsObjectMap(userId, memeId, value);

            if(pendingItemId != null) {
                UpdateResponse response = es.updateRequest(INDEX_NAME, pendingItemId, data).actionGet();
                return response.getId();
            } else {
                IndexResponse response = es.indexRequest(INDEX_NAME, data).actionGet();
                return response.getId();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static void onSendToUser(ElasticSearchContextListener es, String userId, String memeId) {
        Map<String, Object> data = new HashMap<>();
        data.put(ES_USER_ID, userId);
        data.put(ES_MEME_ID, memeId);
        data.put(ES_STATUS, ES_STATUS_PENDING);
        data.put(ES_RATING_TIME, System.currentTimeMillis());
        es.indexRequest(INDEX_NAME, data);
    }

    private static String getPendingItemId(ElasticSearchContextListener es, String userId, String memeId) {
        String pendingItemId = null;

        SearchResponse response = es.searchrequest(INDEX_NAME, QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery(ES_USER_ID, userId))
                .must(QueryBuilders.matchQuery(ES_MEME_ID, memeId))
                .must(QueryBuilders.matchQuery(ES_STATUS, ES_STATUS_PENDING)), 0, 1).actionGet();

        if(response.getHits().getTotalHits() > 0) {
            pendingItemId = response.getHits().getAt(0).id();
        }

        return pendingItemId;
    }

    private static Map<String, Object> getRatingsObjectMap(String userId, String memeId, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(ES_USER_ID, userId);
        data.put(ES_MEME_ID, memeId);
        data.put(ES_VALUE, value);
        data.put(ES_STATUS, ES_STATUS_RATED);
        data.put(ES_RATING_TIME, System.currentTimeMillis());
        return data;
    }

    public static long getTotalNumberOfRatings(ElasticSearchContextListener es) {
        SearchResponse resp = es.searchrequest(INDEX_NAME, QueryBuilders.matchAllQuery(), 0, 0).actionGet();
        return resp.getHits().getTotalHits();
    }

    public static void removePendingRatings(String userId, ElasticSearchContextListener es) {
        // definitely not  more than 2, but to be safe, take 100...
        SearchResponse response = es.searchrequest(INDEX_NAME, QueryBuilders.matchQuery(ES_STATUS, ES_STATUS_PENDING), 0, 100).actionGet();
        for(SearchHit hit : response.getHits()) {
            es.deleteRequest(INDEX_NAME, hit.getId()).actionGet();
        }
    }
}
