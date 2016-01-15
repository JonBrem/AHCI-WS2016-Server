package de.ur.ahci.model;

import meme_recommender.DatabaseContextListener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserPreferences {

    private List<Integer> memesUserHasRated;
    private Map<Integer, TagPreference> userRatings;

    public UserPreferences() {
        memesUserHasRated = new ArrayList<>();
        userRatings = new HashMap<>();
    }

    public void load(int userId, DatabaseContextListener db) {
        clearData();
        ResultSet userRatings = getUserRatings(userId, db);

        try {
            buildPreferences(userRatings);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void buildPreferences(ResultSet userRatings) throws SQLException {
        while(userRatings.next()) {
            int memeId = userRatings.getInt(1);
            int tagId = userRatings.getInt(2);
            int rating = userRatings.getInt(3);

            memesUserHasRated.add(memeId);
            storeRating(tagId, rating);
        }
    }

    private void storeRating(int tagId, int rating) {
        if(!this.userRatings.containsKey(tagId)) {
            this.userRatings.put(tagId, new TagPreference());
        }

        TagPreference tagPreference = this.userRatings.get(tagId);
        tagPreference.increaseTotalRatingsCounter();
        if(rating == 1) {
            tagPreference.increaseGoodRatingsCounter();
        }
    }

    private ResultSet getUserRatings(int userId, DatabaseContextListener db) {
        String sql = "SELECT meme_tags.meme_id,meme_tags.tag_id,user_ratings.rating FROM " +
                "(SELECT ratings.meme_id,ratings.rating FROM ratings WHERE user_id=" + userId + ") AS user_ratings " +
                "LEFT JOIN meme_tags " +
                "ON user_ratings.meme_id=meme_tags.meme_id";

        return db.query(sql);
    }

    private void clearData() {
        memesUserHasRated.clear();
        userRatings.clear();
    }

    public List<Integer> getMemesUserHasRated() {
        return memesUserHasRated;
    }

    public Map<Integer, TagPreference> getUserRatings() {
        return userRatings;
    }

    private class TagPreference {
        private int tagId;

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

        public TagPreference setTagId(int tagId) {
            this.tagId = tagId;
            return this;
        }

        public int getTagId() {
            return this.tagId;
        }

        public float getValue() {
            return this.goodRatingsCounter / (float) this.goodRatingsCounter;
        }

    }


}
