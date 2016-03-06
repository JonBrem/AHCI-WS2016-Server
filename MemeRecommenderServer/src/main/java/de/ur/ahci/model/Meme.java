package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Meme {

    public static final String INDEX_NAME = "memes";

    public static final String ES_URL = "url";
    public static final String ES_TITLE = "title";
    public static final String ES_IMG_URL = "img_url";
    public static final String ES_TAG_LIST = "tag_list";
    public static final String ES_TIME_ADDED = "time_added";

    private String title;
    private String url;
    private List<String> tags;
    private String imgUrl;
    private String id;



    public Meme() {
        tags = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        String tagsString = "[";
        for(int i = 0; i < tags.size(); i++) {
            tagsString += tags.get(i);
            if(i != tags.size() - 1) tagsString += ",";
        }
        tagsString += "]";

        return url + "\t" + imgUrl + "\t" + tagsString + "\t" + title;
    }

    /**
     * Inserts the meme into the database.
     * Returns the meme's id.
     * @param es
     * @return The meme's id
     */
    public String insert(ElasticSearchContextListener es) {
        Map<String, Object> data = new HashMap<>();

        data.put(ES_URL, url);
        data.put(ES_IMG_URL, imgUrl);
        data.put(ES_TITLE, title);
        data.put(ES_TAG_LIST, getTags());
        data.put(ES_TIME_ADDED, System.currentTimeMillis());

        try {
            IndexResponse response = es.indexRequest(INDEX_NAME, data).actionGet();
            return response.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads the meme (with id, title, url, img_url and tags) with the specified id from elastic search.
     * @param id the meme's id
     * @param es elastic search connection
     * @return the meme object
     */
    public static Meme load(String id, ElasticSearchContextListener es) {
        SearchResponse response = es.searchrequest(INDEX_NAME, QueryBuilders.idsQuery(INDEX_NAME).ids(id), 0, 1).actionGet();
        SearchHits hits = response.getHits();

        if(hits.totalHits() > 0) {
            SearchHit hit = hits.getAt(0);
            Meme meme = new Meme();
            try {
                meme.setId(hit.getId());
                meme.setImgUrl((String) hit.getSource().get(ES_IMG_URL));
                meme.setUrl((String) hit.getSource().get(ES_URL));
                meme.setTitle((String) hit.getSource().get(ES_TITLE));
                if(hit.getSource().containsKey(ES_TAG_LIST)) {
                    ((List<String>) hit.getSource().get("tag_list")).forEach(meme::addTag);
                }
                return meme;
            } catch (Exception e) {
                return meme;
            }
        } else {
            return null;
        }
    }
}
