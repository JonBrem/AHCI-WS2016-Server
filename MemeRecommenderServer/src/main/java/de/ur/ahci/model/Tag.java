package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.bootstrap.Elasticsearch;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tag {

    public static Map<String, Tag> getAllTagsInDb(ElasticSearchContextListener es) {
        Map<String, Tag> allTags = new HashMap<>();

        Map<String, Object> query = new HashMap<>();

        ActionFuture<SearchResponse> response = es.searchrequest("tags", QueryBuilders.matchAllQuery(), 0, 10000);

        try {
            SearchResponse results = response.actionGet();
            results.getHits().forEach(searchHit -> addToTagMap(searchHit, allTags));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return allTags;
    }

    private static void addToTagMap(SearchHit searchHit, Map<String, Tag> allTags) {
        Tag tag = new Tag();
        tag.setTagName((String) searchHit.getSource().get("tag_name"));
        tag.setId(searchHit.getId());
        allTags.put(searchHit.getId(), tag);
    }

    private String tagName;
    private String id;

    public Tag() {

    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public boolean existsInDb(ElasticSearchContextListener es) {
        SearchResponse response = es.searchrequest("tags", QueryBuilders.matchQuery("tag_name", tagName), 0, 1).actionGet();
        return response.getHits().totalHits() > 0;
    }

    public String insert(ElasticSearchContextListener es) {
        Map<String, Object> data = new HashMap<>();
        data.put("tag_name", tagName);

        try {
            IndexResponse response = es.indexRequest("tags", data).actionGet();
            return response.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertLink(String memeId, String tagId, ElasticSearchContextListener es) {
        // get meme
        SearchResponse response = es.searchrequest("memes", QueryBuilders.idsQuery("memes").addIds(memeId), 0, 1).actionGet();

        // add to meme tags
        SearchHits hits = response.getHits();
        if(hits.totalHits() > 0) {
            SearchHit meme = hits.getAt(0);

            List<Object> tags = meme.getFields().get("tag_list").getValues();
            tags.add(tagId);

            Map<String, Object> data = new HashMap<>();
            data.put("tag_list", tags);

            es.updateRequest("memes", meme.getId(), data);
        }   // else {
            //   do nothing, we don't know what link to insert :(
            //}
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
