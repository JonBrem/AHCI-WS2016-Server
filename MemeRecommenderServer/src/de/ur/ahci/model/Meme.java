package de.ur.ahci.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonbr on 27.10.2015.
 */
public class Meme {

    private String title;
    private String url;
    private List<String> tags;
    private String imgUrl;

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

}
