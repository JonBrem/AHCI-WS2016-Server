package meme_recommender.request_handlers;

import de.ur.ahci.model.Meme;
import de.ur.ahci.model.UserPreferences;
import meme_recommender.ElasticSearchContextListener;
import meme_recommender.RequestHandler;
import meme_recommender.recommender_engine.MemeRecommender;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.index.IndexResponse;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        String userId = parameterMap.get("user_id")[0];
        String ratingsString = parameterMap.get("ratings")[0];

        String[] ratingIDs = storeRatings(userId, ratingsString);

        StringBuilder ratingIDString = new StringBuilder("\t\"rated_meme_ids\": [");
        for(int i = 0; i < ratingIDs.length; i++) {
            ratingIDString.append("\"").append(ratingIDs[i]).append("\"");
            if(i != ratingIDs.length - 1) ratingIDString.append(",");
        }
        ratingIDString.append("]");

        return ratingIDString.toString();
    }

    private String[] storeRatings(String userId, String ratingsString) {
        ElasticSearchContextListener es = ElasticSearchContextListener.getInstace();

        String[] ratings = ratingsString.split(",");
        List<String> ratedMemeIDs = new ArrayList<String>();
        for(int i = 0; i < ratings.length; i++) {
            Map<String, Object> data = new HashMap<>();
            String[] rating = ratings[i].split(":");

            data.put("user_id", userId);
            data.put("meme_id", rating[0]);
            data.put("value", rating[1]);
            data.put("rating_time", System.currentTimeMillis());

            try {
                ActionFuture<IndexResponse> response = es.indexRequest("ratings", data);
                String ratingId = response.actionGet().getId(); // just call any method to trigger exception if there was a problem.
                ratedMemeIDs.add(rating[0]);
            } catch (Exception e) {
                // meme ID not being returned is the error
            }
        }

        new Thread(() -> {
            new UserPreferences().build(userId, ElasticSearchContextListener.getInstace());
        }).start();

        String[] ratedMemeIDsArr = new String[ratedMemeIDs.size()];
        for(int i = 0; i < ratedMemeIDs.size(); i++) ratedMemeIDsArr[i] = ratedMemeIDs.get(i);
        return ratedMemeIDsArr;
    }

    private void respondToUserIdRequest(PrintWriter out) {
        String id = createNewUserId();
        if(id != null) {
            out.write("{\"id\":\"" + id + "\",\"status\":\"ok\"}");
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
    private String createNewUserId() {
        ElasticSearchContextListener es = ElasticSearchContextListener.getInstace();

        // just put anything here so the user is not empty:
        Map<String, Object> data = new HashMap<>();
        data.put("created", System.currentTimeMillis());

        try {
            ActionFuture<IndexResponse> actions = es.indexRequest("users", data);
            return actions.actionGet().getId();
        } catch (Exception e) {
            return null;
        }
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
        String userId = params.get("user_id")[0];

        int howManyMemes = 2;
        if(params.containsKey("how_many")) {
            howManyMemes = Integer.parseInt(params.get("how_many")[0]);
        }

        Meme[] memes = new MemeRecommender(ElasticSearchContextListener.getInstace()).recommend(userId, howManyMemes);


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
