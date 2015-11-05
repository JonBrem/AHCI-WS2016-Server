package meme_recommender.request_handlers;

import de.ur.ahci.model.Tag;
import meme_recommender.DatabaseContextListener;
import meme_recommender.RequestHandler;
import util.Const;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jonbr on 04.11.2015.
 */
public class DbInspectionRequestHandler extends RequestHandler {
    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String uri = req.getRequestURI();
        uri = uri.substring(1);

        if (!uri.startsWith("inspect_db")) return false;
        if (uri.startsWith("inspect_db/")) {
            handleInspectDbRequest(uri.substring("inspect_db/".length()), out, resp, req);
        }

        return false;
    }

    private void handleInspectDbRequest(String path, PrintWriter out, HttpServletResponse resp, HttpServletRequest req) {
        if (path.equals("get_all_tags")) {
            handleGetAllTagsRequest(out, resp);
        } else if (path.equals("add_new_tag")) {
            handleAddNewTagRequest(out, req.getParameterMap().get("tag_name")[0],
                    req.getParameterMap().containsKey("tag_id") ? Integer.parseInt(req.getParameterMap().get("tag_id")[0]) : -1);
        } else if (path.equals("load_meme")) {
            handleLoadMemeRequest(out, req);
        } else if (path.equals("add_tags_for_meme")) {
            handleAddTagsForMemeRequest(req);
        } else if (path.equals("delete_tag")) {
            handleDeleteTagRequest(req.getParameterMap());
        } else if (path.equals("delete_meme")) {
            handleDeleteMemeRequest(req.getParameterMap());
        }
    }

    private void handleDeleteMemeRequest(Map<String, String[]> parameterMap) {
        DatabaseContextListener db = DatabaseContextListener.getInstance();
        db.executeUpdate("DELETE FROM meme_tags WHERE meme_id=" + parameterMap.get("meme_id")[0]);
        db.executeUpdate("DELETE FROM memes WHERE id=" + parameterMap.get("meme_id")[0]);
    }

    private void handleDeleteTagRequest(Map<String, String[]> parameterMap) {
        DatabaseContextListener db = DatabaseContextListener.getInstance();
        db.executeUpdate("DELETE FROM meme_tags WHERE tag_id=" + parameterMap.get("tag_id")[0]);
        db.executeUpdate("DELETE FROM tags WHERE id=" + parameterMap.get("tag_id")[0]);
    }


    private void handleAddTagsForMemeRequest(HttpServletRequest req) {
        Map<String, String[]> params = req.getParameterMap();

        String memeId = params.get("meme_id")[0];
        DatabaseContextListener db = DatabaseContextListener.getInstance();

        db.executeUpdate("DELETE FROM meme_tags WHERE meme_id=" + memeId);

        List<String> inserts = new ArrayList<>();
        for (String tagId : params.get("tag_id")[0].split(",")) {

            inserts.add("(" + memeId + "," + tagId + ")");
        }

        StringBuilder insertString = new StringBuilder();
        insertString.append("INSERT INTO meme_tags(meme_id,tag_id) VALUES ");
        for (int i = 0; i < inserts.size(); i++) {
            insertString.append(inserts.get(i));
            if (i != inserts.size() - 1) insertString.append(",");
        }

        db.executeUpdate(insertString.toString());
    }

    private void handleLoadMemeRequest(PrintWriter out, HttpServletRequest req) {
        DatabaseContextListener db = DatabaseContextListener.getInstance();
        ResultSet resultSet = db.query(getMemeQuery(req));
        try {
            if (resultSet.next()) {
                int memeId = resultSet.getInt(1);

                out.write("{\n");
                out.write("\t\"id\": " + memeId + ",\n");
                out.write("\t\"url\": \"" + resultSet.getString(2) + "\",\n");
                out.write("\t\"img_url\": \"" + resultSet.getString(3) + "\",\n");
                out.write("\t\"title\": \"" + resultSet.getString(4) + "\"");


                writeTagsForMeme(out, db, memeId);
                out.write("}");
            } else {
                out.write("{\"status\":\"no memes found\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getMemeQuery(HttpServletRequest req) {
        Map<String, String[]> params = req.getParameterMap();
        String sql;
        if (params.containsKey("currentId")) {
            boolean up = params.get("dir")[0].equals("up");
            sql = "SELECT id, url, img_url, title FROM memes WHERE id " +
                    (up ? ">" : "<") + " " +
                    params.get("currentId")[0] +
                    " ORDER BY id " + (up ? "ASC" : "DESC");
        } else {
            sql = "SELECT id, url, img_url, title FROM memes ORDER BY id DESC";
        }
        return sql;
    }

    private void writeTagsForMeme(PrintWriter out, DatabaseContextListener db, int memeId) throws SQLException {
        ResultSet tagsForMeme = db.query("SELECT tag_id FROM meme_tags WHERE meme_id=" + memeId);
        if (tagsForMeme.next()) {
            out.write(",\n\t\"tags\":[");
            String tags = "";
            do {
                tags += tagsForMeme.getInt(1) + ",";
            } while (tagsForMeme.next());
            if (tags.endsWith(",")) tags = tags.substring(0, tags.length() - 1);
            out.write(tags);
            out.write("]\n");
        } else {
            out.write("\n");
        }
    }

    private void handleGetAllTagsRequest(PrintWriter out, HttpServletResponse resp) {
        resp.setStatus(200);
        out.write("[\n");
        List<Tag> tags = getAllTags();
        for (int i = 0; i < tags.size(); i++) {
            out.write("    {\n");
            out.write("        \"id\": " + tags.get(i).getId() + ",\n");
            out.write("        \"name\": \"" + tags.get(i).getTagName() + "\"\n");
            out.write("    }");
            if (i != tags.size() - 1) out.write(",");
            out.write("\n");
        }
        out.write("]");
    }

    private void handleAddNewTagRequest(PrintWriter out, String tagName, int id) {
        DatabaseContextListener db = DatabaseContextListener.getInstance();

        if (id == -1) {
            Tag t = new Tag();
            t.setTagName(tagName);
            out.write("{\n\"tagId\":" + t.insert(db) + "\n}");
        } else {
            db.executeUpdate("UPDATE tags SET tag_name='" + tagName + "' WHERE id=" + id);
            out.write("{\n\"tagId\":" + id + "\n}");
        }
    }

    private List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();

        DatabaseContextListener db = DatabaseContextListener.getInstance();

        ResultSet results = db.query("SELECT id, tag_name FROM tags");
        try {
            while (results.next()) {
                Tag tag = new Tag();
                tag.setTagName(results.getString(2));
                tag.setId(results.getInt(1));
                tags.add(tag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tags;
    }

}
