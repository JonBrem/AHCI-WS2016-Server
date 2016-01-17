package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserPreferences {

    private List<String> memesUserHasRated;
    private Map<String, TagPreference> userRatings;

    public UserPreferences() {
        memesUserHasRated = new ArrayList<>();
        userRatings = new HashMap<>();
    }

    public void load(String userId, ElasticSearchContextListener es) {
        clearData();

        try {
            SearchResponse response = es.searchrequest("ratings", QueryBuilders.matchQuery("user_id", userId), 0, 9999).actionGet();
            buildPreferences(response.getHits());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void buildPreferences(SearchHits hits) throws SQLException {

        for(int i = 0; i < hits.totalHits(); i++) {
            Map<String, Object> hit = hits.getAt(i).getSource();
            String tagId = (String) hit.get("tag_id");
            int rating = (int) hit.get("value");

            storeRating(tagId, rating);
        }
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

    private void clearData() {
        memesUserHasRated.clear();
        userRatings.clear();
    }

    public List<String> getMemesUserHasRated() {
        return memesUserHasRated;
    }

    public Map<String, TagPreference> getUserRatings() {
        return userRatings;
    }

    private class TagPreference {
        private String tagId;

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

        public TagPreference setTagId(String tagId) {
            this.tagId = tagId;
            return this;
        }

        public String getTagId() {
            return this.tagId;
        }

        public float getValue() {
            return this.goodRatingsCounter / (float) this.goodRatingsCounter;
        }

    }


}
