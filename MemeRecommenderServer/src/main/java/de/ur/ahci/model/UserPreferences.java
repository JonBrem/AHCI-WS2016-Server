package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserPreferences {

    private static final String TYPE_NAME = "user_pref";

    private Map<String, TagPreference> userRatings;

    public UserPreferences() {
        userRatings = new HashMap<>();
    }

    public void load(String userId, ElasticSearchContextListener es) {
        SearchResponse response = es.searchrequest(TYPE_NAME, QueryBuilders.matchQuery("user_id", userId), 0, 1).actionGet();

        if(response.getHits().totalHits() > 0) {
            userRatings = new HashMap<>(); // todo!!
        }
    }

    public void build(String userId, ElasticSearchContextListener es) {
        try {
            int start = 0;
            int atATime = 5000;
            while(true) {
                SearchResponse response = es.searchrequest("ratings", QueryBuilders.matchQuery("user_id", userId), start, atATime).actionGet();
                buildPreferences(response.getHits(), es);

                if(response.getHits().totalHits() < atATime) break;
                start += atATime;
            }
            storePreferences(userId, es);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private synchronized void storePreferences(String userId, ElasticSearchContextListener es) {
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        for(String tagId : userRatings.keySet()) {
            data.put(tagId, userRatings.get(tagId).getValue());
            data.put(tagId + "_total", userRatings.get(tagId).totalRatingsCounter);
        }

        data.put("last_calculated", System.currentTimeMillis());
        String oldId = null;

        SearchResponse response = es.searchrequest(TYPE_NAME, QueryBuilders.matchQuery("user_id", userId), 0, 1).actionGet();
        if(response.getHits().totalHits() > 0) {
            oldId = response.getHits().getAt(0).id();
        }

        if (oldId != null) {
            es.updateRequest(TYPE_NAME, oldId, data);
        } else {
            es.indexRequest(TYPE_NAME, data);
        }
    }

    private void buildPreferences(SearchHits hits, ElasticSearchContextListener es) throws SQLException {

        for(int i = 0; i < hits.totalHits(); i++) {
            Map<String, Object> hit = hits.getAt(i).getSource();
            int rating = Integer.parseInt((String) hit.get("value"));

            List<String> tagIDs = getTagIDsForMeme((String) hit.get("meme_id"), es);
            if(tagIDs == null) return;
            for(String tagId : tagIDs) {
                storeRating(tagId, rating);
            }
        }
    }

    private List<String> getTagIDsForMeme(String meme_id, ElasticSearchContextListener es) {
        SearchResponse response = es.searchrequest("memes", QueryBuilders.idsQuery("memes").ids(meme_id), 0, 1).actionGet();
        SearchHits hits = response.getHits();
        if(hits.totalHits() > 0) {
            try {
                return (List<String>) hits.getAt(0).getSource().get("tag_list");
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    private void storeRating(String tagId, int rating) {
        if(!this.userRatings.containsKey(tagId)) {
            this.userRatings.put(tagId, new TagPreference());
        }

        TagPreference tagPreference = this.userRatings.get(tagId);
        tagPreference.increaseTotalRatingsCounter();
        if(rating == 1) {
            tagPreference.increaseGoodRatingsCounter();
        }
    }

    public Map<String, TagPreference> getUserRatings() {
        return userRatings;
    }

    private class TagPreference {
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
