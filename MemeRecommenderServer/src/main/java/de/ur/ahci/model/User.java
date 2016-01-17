package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.List;

public class User {

    public static List<String> getListOfMemeIDsUserHasRated(String userId, ElasticSearchContextListener es) {
        List<String> listOfMemeIDs = new ArrayList<>();

        int start = 0;
        int atATime = 5000;

        while(true) {
            SearchResponse response = es.searchrequest("ratings", QueryBuilders.matchQuery("user_id", userId), start, atATime).actionGet();
            SearchHits hits = response.getHits();

            for(SearchHit hit : hits) {
                listOfMemeIDs.add((String) hit.getSource().get("meme_id"));
            }

            if(hits.totalHits() < atATime) break;
            start += atATime;
        }
        return listOfMemeIDs;
    }

}
