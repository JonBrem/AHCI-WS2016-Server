package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tag {

    public static final String INDEX_NAME = "tags";
    public static final String ES_TAG_NAME = "tag_name";

    public static Map<String, Tag> getAllTagsInDb(ElasticSearchContextListener es) {
        Map<String, Tag> allTags = new HashMap<>();

        Map<String, Object> query = new HashMap<>();

        ActionFuture<SearchResponse> response = es.searchrequest(INDEX_NAME, QueryBuilders.matchAllQuery(), 0, 10000);

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
        tag.setTagName((String) searchHit.getSource().get(ES_TAG_NAME));
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
        SearchResponse response = es.searchrequest(INDEX_NAME, QueryBuilders.matchQuery(ES_TAG_NAME, tagName), 0, 1).actionGet();
        return response.getHits().totalHits() > 0;
    }

    public String insert(ElasticSearchContextListener es) {
        Map<String, Object> data = new HashMap<>();
        data.put(ES_TAG_NAME, tagName);

        try {
            IndexResponse response = es.indexRequest(INDEX_NAME, data).actionGet();
            return response.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds the tag id to the meme's list of tags.
     * @param memeId the meme's id
     * @param tagId the tag's id
     * @param es elastic search connection
     */
    public void insertLink(String memeId, String tagId, ElasticSearchContextListener es) {
        // get meme
        Meme meme = Meme.load(memeId, es);
        // add to meme tags
        if(meme != null) {
            List<String> tags = meme.getTags();
            tags.add(tagId);

            Map<String, Object> data = new HashMap<>();
            data.put(Meme.ES_TAG_LIST, tags);

            es.updateRequest(Meme.INDEX_NAME, meme.getId(), data);
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
