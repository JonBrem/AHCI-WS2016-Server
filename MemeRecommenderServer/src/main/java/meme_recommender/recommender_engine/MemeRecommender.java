package meme_recommender.recommender_engine;

import de.ur.ahci.model.Meme;
import de.ur.ahci.model.User;
import de.ur.ahci.model.UserPreferences;
import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MemeRecommender {

    private ElasticSearchContextListener es;

    public MemeRecommender(ElasticSearchContextListener es) {
        this.es = es;
    }

    /**
     * @param userId
     * id of the user that gets the recommendation
     * @param howManyMemes
     * @return
     * Array of recommended Memes
     */
    public Meme[] recommend(String userId, int howManyMemes) {
        UserPreferences userPreferences = getUserPreferences(userId);
        List<String> memesUserHasRated = User.getListOfMemeIDsUserHasRated(userId, es);


//        if(userHasFewRatings(userPreferences)) {
//            if(fewRatingsInGeneral()) {
                return showRandomMemes(howManyMemes, memesUserHasRated);
//            } else {
//                return showMemesWithPositiveRatingsLooselyMatchingUser(howManyMemes, userPreferences);
//            }
//        } else {
//            if(fewRatingsInGeneral()) {
//                return showMemesForUserPreferences(howManyMemes, userPreferences);
//            } else {
//                List<User> similarUsers = findSimilarUsers(userId, userPreferences);
//                float probability = probabilityOfShowingMemesSimilarUsersLiked(howManyMemes, similarUsers);
//                Random rnd = new Random();
//                if (rnd.nextDouble() <= probability) {
//                    return showMemesSimilarUsersLiked(howManyMemes, similarUsers);
//                } else {
//                    return showRandomMemesForUserPreferences(howManyMemes, userPreferences);
//                }
//            }
//        }
    }

    private Meme[] showRandomMemes(int howManyMemes, List<String> memesUserHasRated) {
        QueryBuilder query = QueryBuilders.functionScoreQuery(QueryBuilders.matchAllQuery(),
                ScoreFunctionBuilders.randomFunction(new Random().nextInt()));
        QueryBuilder filter = QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("id", memesUserHasRated));
        SearchResponse response = es.searchrequest("memes", query, filter, 0, howManyMemes).actionGet();

        return getMemesFromResultSet(howManyMemes, response);
    }

    private Meme[] getMemesFromResultSet(int howManyMemes, SearchResponse response) {
        SearchHits hits = response.getHits();

        Meme[] memes = new Meme[hits.totalHits() > howManyMemes? howManyMemes : (int) hits.totalHits()];

        for(int i = 0; i < memes.length; i++) {
            Map<String, Object> memeData = hits.getAt(i).getSource();
            Meme m = new Meme();
            m.setImgUrl((String) memeData.get("img_url"));
            m.setTitle((String) memeData.get("title"));
            m.setUrl((String) memeData.get("url"));
            m.setId(hits.getAt(i).getId());

            memes[i] = m;
        }

        return memes;
    }


    private float probabilityOfShowingMemesSimilarUsersLiked(int howManyMemes, List<User> similarUsers) {
        return 0;
    }

    private List<User> findSimilarUsers(int userId, UserPreferences userPreferences) {
        List<User> similarUsers = new ArrayList<>();
        return similarUsers;
    }

    private UserPreferences getUserPreferences(String userId) {
        UserPreferences up = new UserPreferences();
        up.load(userId, es);
        return up;
    }

    private boolean fewRatingsInGeneral() {
        return false;
    }

    private boolean userHasFewRatings(List<String> memesUserHasRated) {
        return memesUserHasRated.size() < 20;
    }

}
