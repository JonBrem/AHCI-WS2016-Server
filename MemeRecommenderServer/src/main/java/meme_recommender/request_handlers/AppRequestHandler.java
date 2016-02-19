package meme_recommender.request_handlers;

import de.ur.ahci.model.Meme;
import de.ur.ahci.model.Rating;
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

/**
 * The App Request Handler handles every request sent from the App.
 * It is used to deliver IDs, load images and store ratings.
 */
public class AppRequestHandler extends RequestHandler {

    public static final String LOAD_IMAGES = "load_images";
    public static final String REQUEST_ID = "request_id";

    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String uri = req.getRequestURI().substring(1); // get URI without /

        if(!handlesRequest(uri)) return false;

        if (uri.startsWith(LOAD_IMAGES)) {
            handleLoadImagesRequest(req, out);
        } else if (uri.startsWith(REQUEST_ID)) {
            respondToUserIdRequest(out);
        } else {
            out.write("{}");
        }

        out.flush();
        out.close();
        return true;
    }

    /**
     * @param uri the URI that was sent to the server.
     * @return true if this request handler handles the URI and false if it does not.
     */
    private boolean handlesRequest(String uri) {
        return uri.startsWith(LOAD_IMAGES) || uri.startsWith(REQUEST_ID);
    }

    /**
     * @param req the http request (possible containing ratings)
     * @param out stream that will receive the list of images for the client & possibly a list of ratings that
     *            were approved by the server.
     */
    private void handleLoadImagesRequest(HttpServletRequest req, PrintWriter out) {
        out.write("{\n");
        String ratings = applyRatings(req.getParameterMap());

        if(ratings != null && ratings.length() > 0) {
            out.write(ratings);
            out.write(",\n");
        }

        out.write(getRecommendedImages(req.getParameterMap()));
        out.write("}\n");
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

        String[] ratingIDs = handleSentRatings(userId, ratingsString);

        StringBuilder ratingIDString = new StringBuilder("\t\"rated_meme_ids\": [");
        for(int i = 0; i < ratingIDs.length; i++) {
            ratingIDString.append("\"").append(ratingIDs[i]).append("\"");
            if(i != ratingIDs.length - 1) ratingIDString.append(",");
        }
        ratingIDString.append("]");

        return ratingIDString.toString();
    }

    /**
     * @param userId The user's ID
     * @param ratingsString The ratings string (memeId1:ratingValue1,memeId2:ratingValue2,...)
     * @return Array of memeIDs, <strong>NOT</strong> rating IDs!! of the items that were stored.
     */
    private String[] handleSentRatings(String userId, String ratingsString) {
        ElasticSearchContextListener es = ElasticSearchContextListener.getInstace();

        List<String> ratedMemeIDs = storeRatings(userId, ratingsString, es);
        updateUserPreferences(userId);

        return toArray(ratedMemeIDs);
    }

    /**
     * @param strings List of Strings to put in Array
     * @return Array of Strings
     */
    private String[] toArray(List<String> strings) {
        String[] arr = new String[strings.size()];
        for(int i = 0; i < strings.size(); i++) arr[i] = strings.get(i);
        return arr;
    }

    /**
     * @param userId The user's ID
     * @param ratingsString The ratings string (memeId1:ratingValue1,memeId2:ratingValue2,...)
     * @param es ElasticSearch connection
     * @return List of memeIDs, <strong>NOT</strong> rating IDs!! of the items that were stored.
     */
    private List<String> storeRatings(String userId, String ratingsString, ElasticSearchContextListener es) {
        String[] ratings = ratingsString.split(",");
        List<String> ratedMemeIDs = new ArrayList<>();
        for (String rating : ratings) {
            String[] ratingParts = rating.split(":");

            String ratingId = Rating.save(es, userId, ratingParts[0], ratingParts[1]);

            if (ratingId != null) {
                ratedMemeIDs.add(ratingParts[0]);
            }
        }
        return ratedMemeIDs;
    }

    /**
     * Updates the user preferences of the specified user (done in background)
     * @param userId The user's id
     */
    private void updateUserPreferences(String userId) {
        new Thread(() -> {
            new UserPreferences().build(userId, ElasticSearchContextListener.getInstace());
        }).start();
    }

    /**
     * Creates a new user id and writes on the print writer as json
     * @param out PrintWriter (most likely for the HTTP response)
     */
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
        return buildMemeJsonResponse(memes);
    }

    /**
     * @param memes Array of Meme objects that will be sent to the client as JSON
     * @return JSON representation of the memes
     */
    private String buildMemeJsonResponse(Meme[] memes) {
        StringBuilder builder = new StringBuilder();
        builder.append("\timages: [\n");

        for(int i = 0; i < memes.length; i++) {
            if(memes[i] == null) continue;

            builder.append("\t\t{\n")
                    .append("\t\t\tid: ").append(memes[i].getId()).append(",\n")
                    .append("\t\t\turl: \"").append(memes[i].getUrl()).append("\"\n")
                    .append("\t\t}");
            if(i != memes.length - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }

        builder.append("\t]\n");
        return builder.toString();
    }

}
