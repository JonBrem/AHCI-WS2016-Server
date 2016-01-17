package meme_recommender.recommender_engine;

import de.ur.ahci.model.Meme;
import de.ur.ahci.model.User;
import de.ur.ahci.model.UserPreferences;
import meme_recommender.ElasticSearchContextListener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

        return null;

//        if(userHasFewRatings(userPreferences)) {
//            if(fewRatingsInGeneral()) {
//                return showRandomMemes(howManyMemes, userPreferences);
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

//    private Meme[] showRandomMemes(int howManyMemes, UserPreferences userPreferences) {
//        StringBuilder sql = new StringBuilder().append("SELECT tmp2.* FROM (SELECT ROW_NUMBER() OVER() as rownum, tmp1.* FROM (SELECT * FROM memes");
//
//        List<String> memesUserHasRated = userPreferences.getMemesUserHasRated();
//        Collections.reverse(memesUserHasRated);
//
//        String notInString = getListUserHasRatedAsString(memesUserHasRated);
//        if(notInString.length() > 0) sql.append( " WHERE id NOT IN " ).append(notInString);
//
//        sql.append(" ORDER BY RANDOM() OFFSET 0 ROWS) AS tmp1) AS tmp2")
//            .append(" WHERE rownum<=").append(howManyMemes);
//
//        ResultSet results = db.query(sql.toString());
//        return getMemesFromResultSet(howManyMemes, results);
//    }
//
//    private Meme[] getMemesFromResultSet(int howManyMemes, ResultSet results) {
//        Meme[] memes = new Meme[howManyMemes];
//
//        for(int i = 0; i < memes.length; i++) {
//            try {
//                if(results.next()) {
//                    Meme m = new Meme();
//                    m.setImgUrl(results.getString(results.findColumn("img_url")));
//                    m.setTitle(results.getString(results.findColumn("title")));
//                    m.setUrl(results.getString(results.findColumn("url")));
//                    m.setId(results.getString(results.findColumn("id")));
//                    memes[i] = m;
//                } else break;
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return memes;
//    }

    private String getListUserHasRatedAsString(List<Integer> memesUserHasRated) {
        StringBuilder sql = new StringBuilder();
        int max = memesUserHasRated.size();
        max = max > 500? 500 : max;

        if(max != 0) {
            sql.append("(");
            for(int i = 0; i < max; i++) {
                sql.append(memesUserHasRated.get(i));
                if(i != max - 1) sql.append(",");
            }
            sql.append(")");
        }
        return sql.toString();
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

    private boolean userHasFewRatings(UserPreferences userPreferences) {
        return userPreferences.getMemesUserHasRated().size() < 20;
    }

}
