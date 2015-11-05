package meme_recommender.request_handlers;

import de.ur.ahci.model.Meme;
import de.ur.ahci.model.Tag;
import meme_recommender.DatabaseContextListener;
import meme_recommender.RequestHandler;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by jonbr on 31.10.2015.
 */
public class CrawlerRequestHandler extends RequestHandler {


    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String uri = req.getRequestURI();
        uri = uri.substring(1);

        if(!uri.startsWith("crawler")) return false;

        if(uri.startsWith("crawler/insert")) {
            Map<String, String[]> params = req.getParameterMap();
            try {
                try {
                    insertMemeIntoDb(params);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                resp.setStatus(200);
                out.write("{\n\tstatus:\"OK\"\n}");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if(uri.startsWith("crawler/get_all")) {
            printWholeMemeDB(out);
        } else if(uri.startsWith("crawler/fix_db")) {

        }

        return true;
    }

    private void insertMemeIntoDb(Map<String, String[]> params) throws SQLException, UnsupportedEncodingException {
        Meme m = new Meme();
        m.setTitle(URLDecoder.decode(params.get("title")[0], "UTF-8"));
        m.setUrl(URLDecoder.decode(params.get("url")[0], "UTF-8"));
        m.setImgUrl(URLDecoder.decode(params.get("img_url")[0], "UTF-8"));

        DatabaseContextListener db = DatabaseContextListener.getInstance();
        int memeId = m.insert(db);

        for(String tag : params.get("tag")) {
            insertTagIntoDb(tag, memeId, db);
        }

    }

    private void insertTagIntoDb(String tagName, int memeId, DatabaseContextListener db) throws SQLException{
        Tag tag = new Tag();
        tag.setTagName(tagName);
        int tagId;
        if(!tag.existsInDb(db)) {
             tagId = tag.insert(db);
        } else {
            ResultSet tags = db.query("SELECT id FROM tags WHERE tag_name='" + tagName + "'");
            if(tags.next()) {
                tagId = tags.getInt(1);
            } else {
                throw new SQLException();
            }
        }
        tag.insertLink(memeId, tagId, db);
    }

    private void printWholeMemeDB(PrintWriter out) {
        DatabaseContextListener db = DatabaseContextListener.getInstance();

        ResultSet results = db.query("SELECT id, url, img_url, title FROM memes");

        try {
            while(results.next()) {
                out.write(results.getInt(1) + "\t" + "\t" + results.getString(2) + "\t" +
                        results.getString(3) + "\t" + results.getString(4) + "<br>\n");
            }
            out.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
