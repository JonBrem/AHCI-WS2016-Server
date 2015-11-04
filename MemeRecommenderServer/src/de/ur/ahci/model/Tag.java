package de.ur.ahci.model;

import meme_recommender.DatabaseContextListener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jonbr on 27.10.2015.
 */
public class Tag {

    public final static String CREATE_TABLE_TAGS = "CREATE TABLE tags (" +
            "id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1), " +
            "tag_name VARCHAR(100)" +
            ")";

    public static final String CREATE_TABLE_MEME_TAGS  = "CREATE TABLE meme_tags(" +
            "id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1), " +
            "meme_id INTEGER, " +
            "tag_id INTEGER" +
            ")";

    public static Map<Integer, Tag> getAllTagsInDb(DatabaseContextListener db) {
        Map<Integer, Tag> allTags = new HashMap<>();

        ResultSet results = db.query("SELECT * FROM tags");
        try {
            while(results.next()) {
                Tag t = new Tag();
                t.setTagName(results.getString(2));
                allTags.put(results.getInt(1), t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allTags;
    }

    private String tagName;
    private int id;

    public Tag() {

    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public boolean existsInDb(DatabaseContextListener db) {
        ResultSet results = db.query("SELECT * FROM tags WHERE tag_name='" + tagName + "'");
        try {
            if(results.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int insert(DatabaseContextListener db) {
        db.executeUpdate("INSERT INTO tags (tag_name) VALUES ('" + tagName + "')");
        ResultSet results = db.query("SELECT id FROM tags WHERE tag_name='" + tagName + "' ORDER BY id DESC");

        try {
            if(results.next()) {
                return results.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void insertLink(int memeId, int tagId, DatabaseContextListener db) {
        db.executeUpdate("INSERT INTO meme_tags (meme_id, tag_id) VALUES (" + memeId + "," + tagId + ")");
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
