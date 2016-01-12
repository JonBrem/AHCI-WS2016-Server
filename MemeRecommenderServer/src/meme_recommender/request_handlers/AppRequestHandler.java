package meme_recommender.request_handlers;

import de.ur.ahci.model.Meme;
import meme_recommender.DatabaseContextListener;
import meme_recommender.RequestHandler;
import meme_recommender.recommender_engine.MemeRecommender;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AppRequestHandler extends RequestHandler {


    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String uri = req.getRequestURI();
        uri = uri.substring(1);

        if(!(uri.startsWith("load_images") || uri.startsWith("request_id"))) return false;

        if (uri.startsWith("load_images")) {
            out.write(getRecommendedImages(1 /* @TODO read user id from a param */));
        } else if (uri.startsWith("request_id")) {
            respondToUserIdRequest(out);
        } else {
            out.write("{}");
        }

        out.flush();
        out.close();

        return true;
    }

    private void respondToUserIdRequest(PrintWriter out) {
        int id = createNewUserId();
        if(id != -1) {
            out.write("{\"id\":" + id + ",\"status\":\"ok\"}");
        } else {
            out.write("{\"status\":\"error\"}");
        }
    }

    private int createNewUserId() {
        DatabaseContextListener db = DatabaseContextListener.getInstance();
        ResultSet results = db.executeInsert("INSERT INTO users (id) VALUES (DEFAULT)");
        if(results == null) {
            return -1;
        }
        try {
            if(results.next()) {
                return results.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /*
        http://9gag.com/gag/aZNoBBp?sc=cute	http://img-9gag-fun.9cache.com/photo/aZNoBBp_700b.jpg	[cute,9gag]	Ew human.
        http://9gag.com/gag/aNK8ZqK?sc=cute	http://img-9gag-fun.9cache.com/photo/aNK8ZqK_700b.jpg	[cute,9gag]	After one week away, this is her happy face of seeing me.
        http://9gag.com/gag/adprLxd?sc=cute	http://img-9gag-fun.9cache.com/photo/adprLxd_700b.jpg	[cute,9gag]	Sad kitty is sad.
        http://9gag.com/gag/abbKM8E?sc=cute	http://img-9gag-fun.9cache.com/photo/abbKM8E_700b.jpg	[cute,9gag]	Morag's unique ginger and black coat
        http://9gag.com/gag/aQnBqWq?sc=cute	http://img-9gag-fun.9cache.com/photo/aQnBqWq_700b.jpg	[cute,9gag]	My Lion King

        http://9gag.com/gag/aDmrxgx?sc=cute	http://img-9gag-fun.9cache.com/photo/aDmrxgx_700b.jpg	[cute,9gag]	Easy, breezy, beautiful, cover squirrel
        http://9gag.com/gag/aepy00b?sc=cute	http://img-9gag-fun.9cache.com/photo/aepy00b_700b_v1.jpg	[cute,9gag]	This is my beauty from Namibia
        http://9gag.com/gag/a8jQM8e?sc=cute	http://img-9gag-fun.9cache.com/photo/a8jQM8e_700b.jpg	[cute,9gag]	She is almost 10 years old but still has a baby face
        http://9gag.com/gag/aYwZK40?sc=cute	http://img-9gag-fun.9cache.com/photo/aYwZK40_700b.jpg	[cute,9gag]	My best friend for life!
        http://9gag.com/gag/avL4n4d?sc=cute	http://img-9gag-fun.9cache.com/photo/avL4n4d_700b.jpg	[cute,9gag]	Wanted to share my beautiful old lady with you.

     */

    /**
     * @TODO really implement this!!
     * @param userId
     * The ID of the user that will get the recommendation
     * @return
     * JSON string of memes that the user will see next.
     */
    private String getRecommendedImages(int userId) {
        Meme[] memes = new MemeRecommender().recommend(userId);


        StringBuilder builder = new StringBuilder();
        builder.append("{\n");

        builder.append("\timages: [\n");

        for(int i = 0; i < memes.length; i++) {

            builder.append("\t\t{\n");

            builder.append("\t\t\tid: ").append(i).append(",\n");
            builder.append("\t\t\turl: \"").append(memes[i].getUrl()).append("\",\n");
            builder.append("\t\t\ttitle: \"").append(memes[i].getTitle()).append("\"\n");

            builder.append("\t\t}");
            if(i != memes.length - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }

        builder.append("\t]\n");
        builder.append("}\n");
        return builder.toString();
    }

    private Meme getMeme(String url, String imgUrl, String title, String... tags) {
        Meme m = new Meme();
        m.setImgUrl(imgUrl);
        m.setTitle(title);
        m.setUrl(url);
        for(String t : tags) {
            m.addTag(t);
        }
        return m;
    }

}
