package de.ur.ahci.model;

import meme_recommender.DatabaseContextListener;
import util.Const;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonbr on 27.10.2015.
 */
public class Meme {

    public static final String CREATE_TABLE  = "CREATE TABLE memes(" +
            "id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1), " +
            "url VARCHAR(255), " +
            "img_url VARCHAR(255), " +
            "title VARCHAR(510)" +
            ")";

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

    /**
     * Inserts the meme into the database.
     * Returns the meme's id.
     * @param db
     * @return
     */
    public int insert(DatabaseContextListener db) {
        StringBuilder builder = new StringBuilder();

        builder.append("INSERT INTO memes (url,img_url,title) VALUES (").
                append("'").append(url).append("',").
                append("'").append(imgUrl).append("',").
                append("'").append(title.replaceAll("'", "&quot;")).append("')");

        Const.log(Const.LEVEL_DEBUG, builder.toString());
        db.executeUpdate(builder.toString());

        ResultSet results = db.query("SELECT id FROM memes WHERE url='" + url + "'");
        try {
            if(results.next()) {
                return results.getInt(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
