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
import java.util.Map;


public class AppRequestHandler extends RequestHandler {


    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String uri = req.getRequestURI();
        uri = uri.substring(1);

        if(!(uri.startsWith("load_images") || uri.startsWith("request_id"))) return false;

        if (uri.startsWith("load_images")) {
            out.write("{\n");
            String ratings = applyRatings(req.getParameterMap());

            if(ratings != null && ratings.length() > 0) {
                out.write(ratings);
                out.write(",\n");
            }

            out.write(getRecommendedImages(req.getParameterMap()));
            out.write("}\n");
        } else if (uri.startsWith("request_id")) {
            respondToUserIdRequest(out);
        } else {
            out.write("{}");
        }

        out.flush();
        out.close();

        return true;
    }

    /**
     * Stores the ratings in the request in the database.
     * @param parameterMap
     * <br>Must contain <strong>ratings</strong> as a key for this to have some effect.
     * Ratings must be in the following format:<br>
     * <i>ImageId1:Rating1,ImageId2:Rating2,ImageId3:Rating3...</i><br>
     * Must also contain the <strong>user_id</strong>.
     */
    private String applyRatings(Map<String, String[]> parameterMap) {
        if(!parameterMap.containsKey("ratings")) return null;
        int userId = Integer.parseInt(parameterMap.get("user_id")[0]);
        String ratingsString = parameterMap.get("ratings")[0];

        int[] ratingIDs = storeRatings(userId, ratingsString);

        StringBuilder ratingIDString = new StringBuilder("\t\"rated_meme_ids\": [");
        for(int i = 0; i < ratingIDs.length; i++) {
            ratingIDString.append(ratingIDs[i]);
            if(i != ratingIDs.length - 1) ratingIDString.append(",");
        }
        ratingIDString.append("]");

        return ratingIDString.toString();
    }

    private int[] storeRatings(int userId, String ratingsString) {
        StringBuilder sql = new StringBuilder().append("INSERT INTO ratings (user_id,meme_id,rating) VALUES ");

        String[] ratings = ratingsString.split(",");
        int[] ratingIDs = new int[ratings.length];
        for(int i = 0; i < ratings.length; i++) {
            String[] rating = ratings[i].split(":");
            int memeId = Integer.parseInt(rating[0]);
            int value = Integer.parseInt(rating[1]);
            ratingIDs[i] = memeId;

            sql.append("(").append(userId).append(",").append(memeId).append(",").append(value).append(")");
            if(i != ratings.length - 1) {
                sql.append(",");
            }
        }

        DatabaseContextListener db = DatabaseContextListener.getInstance();
        db.executeUpdate(sql.toString());
        return ratingIDs;
    }

    private void respondToUserIdRequest(PrintWriter out) {
        int id = createNewUserId();
        if(id != -1) {
            out.write("{\"id\":" + id + ",\"status\":\"ok\"}");
        } else {
            out.write("{\"status\":\"error\"}");
        }
    }

    /**
     * @return
     * a new & unique user ID.
     * <br>
     * -1 if there was an error.
     */
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

    /**
     * @param params
     * Must contain the ID of the user that will get the recommendation with param <strong>user_id</strong>
     * <br>
     * Possible param: <strong>how_many</strong> (as in "how many images should be recommended"),
     * defaults to 2
     * @return
     * JSON string of memes that the user will see next.
     * {} if there is no user_id param.
     */
    private String getRecommendedImages(Map<String, String[]> params) {
        if(!params.containsKey("user_id")) return "{}";
        int userId = Integer.parseInt(params.get("user_id")[0]);

        int howManyMemes = 2;
        if(params.containsKey("how_many")) {
            howManyMemes = Integer.parseInt(params.get("how_many")[0]);
        }

        Meme[] memes = new MemeRecommender(new DatabaseContextListener()).recommend(userId, howManyMemes);


        StringBuilder builder = new StringBuilder();

        builder.append("\timages: [\n");

        for(int i = 0; i < memes.length; i++) {
            if(memes[i] == null) continue;

            builder.append("\t\t{\n");

            builder.append("\t\t\tid: ").append(memes[i].getId()).append(",\n");
            builder.append("\t\t\turl: \"").append(memes[i].getUrl()).append("\"\n");

            builder.append("\t\t}");
            if(i != memes.length - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }

        builder.append("\t]\n");
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
