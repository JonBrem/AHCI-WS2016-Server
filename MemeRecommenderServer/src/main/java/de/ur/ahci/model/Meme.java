package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.index.IndexResponse;
import util.Const;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonbr on 27.10.2015.
 */
public class Meme {


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

        data.put("url", url);
        data.put("img_url", imgUrl);
        data.put("title", title);

        try {
            IndexResponse response = es.indexRequest("memes", data).actionGet();
            return response.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
