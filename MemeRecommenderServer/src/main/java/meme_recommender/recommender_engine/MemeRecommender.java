package meme_recommender.recommender_engine;

import de.ur.ahci.model.*;
import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.*;

/*
@todo there are a lot of random numbers in this class! Look for a way to justify them or at the very least extract them into constants that can be changed easily!
 */
/**
 *
 */
public class MemeRecommender {

    private ElasticSearchContextListener es;

    public MemeRecommender(ElasticSearchContextListener es) {
        this.es = es;
    }

    /**
     * @param userId
     * id of the user that gets the recommendation
     * @param howManyMemes
     * maximum number of memes that will be returned
     * @return
     * Array of recommended Memes
     */
    public MemeRecommendation[] recommend(String userId, int howManyMemes) {
        UserPreferences userPreferences = getUserPreferences(userId);
        List<String> memesUserHasRated = User.getListOfMemeIDsUserHasRated(userId, es);

        List<MemeRecommendation> recommendations = new ArrayList<>();
        if(howManyMemes > 1) {
            Meme randomHugelolMemeUserHasNotSeen = getRandomHugelolMemeUserHasNotSeen(memesUserHasRated);
            if(randomHugelolMemeUserHasNotSeen != null) {
                recommendations.add(new MemeRecommendation(randomHugelolMemeUserHasNotSeen, MemeRecommendation.REASON_RANDOM, ""));
                howManyMemes--;
            }
        }

//        if(userHasFewRatings(memesUserHasRated)) {
////             @todo determine if we need this if/else (basically we would just be guessing anyway...)
//            if(fewRatingsInGeneral(memesUserHasRated.size())) {
                recommendations.addAll(Arrays.asList(showRandomMemes(howManyMemes, memesUserHasRated)));
//            } else {
//                recommendations.addAll(Arrays.asList(showMemesForUserPreferences(howManyMemes, memesUserHasRated, userPreferences)));
//            }
//        } else {
//            if(fewRatingsInGeneral(memesUserHasRated.size())) {
//                recommendations.addAll(Arrays.asList(showMemesForUserPreferences(howManyMemes, memesUserHasRated, userPreferences)));
//            } else {
//                List<String> similarUsers = findSimilarUsers(userId, userPreferences);
//                float probability = probabilityOfShowingMemesSimilarUsersLiked(similarUsers);
//                Random rnd = new Random();
//                if (rnd.nextDouble() <= probability) {
//                    recommendations.addAll(Arrays.asList(showMemesSimilarUsersLiked(howManyMemes, memesUserHasRated, similarUsers)));
//                } else {
//                    recommendations.addAll(Arrays.asList(showMemesForUserPreferences(howManyMemes, memesUserHasRated, userPreferences)));
//                }
//            }
//        }

        MemeRecommendation[] toReturn = new MemeRecommendation[recommendations.size()];
        for(int i = 0; i < recommendations.size(); i++) toReturn[i] = recommendations.get(i);
        return toReturn;
    }

    private Meme getRandomHugelolMemeUserHasNotSeen(List<String> memesUserHasRated) {
        Tag hugelolTag = Tag.getForName("hugelol", es);
        if(hugelolTag != null) {
            SearchResponse response = es.searchrequest(Meme.INDEX_NAME, QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchQuery(Meme.ES_TAG_LIST, hugelolTag.getId()))
                    .mustNot(QueryBuilders.idsQuery().ids(memesUserHasRated)), 0, 1).actionGet();

            SearchHits hits = response.getHits();
            if(hits.getTotalHits() > 0) {
                return Meme.load(hits.getAt(0).getId(), es);
            }
        }
        return null;
    }

    // @ todo test
    private MemeRecommendation[] showMemesSimilarUsersLiked(int howManyMemes, List<String> memesUserHasRated, List<String> similarUsers) {
        int maxSize = 10;
        int iterations = similarUsers.size() / maxSize;
        if((similarUsers.size() % maxSize) != 0) iterations++;

        Map<String, Integer> memeLikes = new HashMap<>();

        for(int i = 0; i < iterations; i++) {
            int start = i * maxSize;
            int end = (i + 1) * maxSize - 1;
            if(end >= similarUsers.size()) end = similarUsers.size() - 1;

            List<String> usersForIteration = similarUsers.subList(start, end);
            retrieveMemeRatingsForUsers(usersForIteration, memeLikes, memesUserHasRated);
        }

        List<String> memesSortedByNumberOfLikes = getMemesSortedByNumberOfLikes(memeLikes);

        if(memesSortedByNumberOfLikes.size() == 0) return showRandomMemes(howManyMemes, memesUserHasRated);

        if(memesSortedByNumberOfLikes.size() < howManyMemes) {
            howManyMemes = memesSortedByNumberOfLikes.size();
        }

        MemeRecommendation[] toReturn = new MemeRecommendation[howManyMemes];
        for(int i = 0; i < toReturn.length; i++) {
            toReturn[i] = new MemeRecommendation(Meme.load(memesSortedByNumberOfLikes.get(i), es), MemeRecommendation.REASON_SIMILAR_USERS, null);
        }
        return toReturn;
    }

    // @todo possibly: decomposition, test
    private List<String> getMemesSortedByNumberOfLikes(Map<String, Integer> memeLikes) {
        class IntAndString {
            public String str;
            public int i;
        }

        List<IntAndString> memesSortedByNumberOfLikes = new ArrayList<>();
        for(String key : memeLikes.keySet()) {
            IntAndString ias = new IntAndString();
            ias.i = memeLikes.get(key);
            ias.str = key;
            memesSortedByNumberOfLikes.add(ias);
        }
        Collections.sort(memesSortedByNumberOfLikes, (o1, o2) -> Integer.compare(o1.i, o2.i));
        Collections.reverse(memesSortedByNumberOfLikes);

        List<String> onlyStrings = new ArrayList<>();
        for(int i = 0; i < memesSortedByNumberOfLikes.size(); i++) onlyStrings.set(i, memesSortedByNumberOfLikes.get(i).str);
        return onlyStrings;
    }

    private void retrieveMemeRatingsForUsers(List<String> usersForIteration, Map<String, Integer> memeLikes, List<String> memesUserHasRated) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        usersForIteration.forEach(userId -> query.should(QueryBuilders.matchQuery(Rating.ES_USER_ID, userId)));
        query.minimumNumberShouldMatch(1);
        query.mustNot(QueryBuilders.idsQuery(toArray(memesUserHasRated)));
        query.mustNot(QueryBuilders.matchQuery(Rating.ES_STATUS, Rating.ES_STATUS_PENDING));

        int start = 0;
        int size = 5000;

        while(true) {
            SearchResponse response = es.searchrequest(Rating.INDEX_NAME, query, start, size).actionGet();
            SearchHits hits = response.getHits();

            for(SearchHit hit : hits) {
                addMemeRatingToTotalRatings(memeLikes, hit);
            }

            if(hits.getTotalHits() < size) break;
            start += size;
        }
    }

    private String[] toArray(List<String> memesUserHasRated) {
        String[] arr = new String[memesUserHasRated.size()];
        for(int i = 0; i < arr.length; i++) arr[i] = memesUserHasRated.get(i);
        return arr;
    }

    private void addMemeRatingToTotalRatings(Map<String, Integer> memeLikes, SearchHit hit) {
        Map<String, Object> source = hit.getSource();
        int value;
        Object val = source.get(Rating.ES_VALUE);
        if(val instanceof String) {
            value = Integer.parseInt((String) val);
        } else if (val instanceof Integer) {
            value = (Integer) val;
        } else if (val instanceof Float) {
            value = (int) val;
        } else {
            value = (int) val;
        }

        if(value != 1) {
            value = -2; // negative ratings are bad!!
        }

        String memeId = (String) source.get(Rating.ES_MEME_ID);
        if(memeLikes.containsKey(memeId)) {
            memeLikes.put(memeId, memeLikes.get(memeId) + value);
        } else {
            memeLikes.put(memeId, value);
        }
    }

    // @TODO test
    // @todo decide if weight should be how much the user likes it -0.5 or leave it as it is (user likes it with likelihood 0.1 -> tag being present leads to +0.1 score)
    private MemeRecommendation[] showMemesForUserPreferences(int howManyMemes, List<String> memesUserHasRated, UserPreferences userPreferences) {
        // Function score query: see https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
        FunctionScoreQueryBuilder queryBuilder = QueryBuilders.functionScoreQuery().boostMode(CombineFunction.MULT).scoreMode("sum");

        // for every tag: if the meme has the tag => add (likelihood for user to like meme with that tag) to total score
        Map<String, UserPreferences.TagPreference> userRatings = userPreferences.getUserRatings();
        for(String key : userRatings.keySet()) {
            queryBuilder.add(
                    QueryBuilders.matchQuery("tag_list", key),
                    ScoreFunctionBuilders.weightFactorFunction(userRatings.get(key).getValue())
            );
        }

        // same query, filtered by excluding the memes the user has already rated
        QueryBuilder filtered = QueryBuilders.boolQuery().must(queryBuilder)
                .filter(QueryBuilders.boolQuery().mustNot(QueryBuilders.idsQuery("memes").ids(memesUserHasRated)));

        // actual search in ES
        SearchResponse response = es.searchrequest("memes", filtered, 0, howManyMemes).actionGet();

        Meme[] memesFromResultSet = getMemesFromResultSet(howManyMemes, response);
        MemeRecommendation[] recommendations = new MemeRecommendation[memesFromResultSet.length];
        for (int i = 0; i < recommendations.length; i++) {
            recommendations[i] = new MemeRecommendation(memesFromResultSet[i], MemeRecommendation.REASON_TAGS, null);
        }
        return recommendations;
    }

    // @todo test
    private List<String> findSimilarUsers(String userId, UserPreferences userPreferences) {
        Map<String, UserPreferences.TagPreference> userRatings = userPreferences.getUserRatings();

        List<String> users = new ArrayList<>();
        int levelOfSimilarity = 0;
        while(users.size() < 8) {
            users = getUsersMatchingSimilarity(userId, userRatings, levelOfSimilarity);
            levelOfSimilarity++;
            if(levelOfSimilarity >= 4) break;
        }

        if(users.size() > 100) users.subList(0, 100);

        return users;
    }

    /**
     *
     * @param userId the user's id
     * @param userRatings the user's tag preferences
     * @param levelOfSimilarity
     * integer, these examples should illustrate how it works:
     * <ul>
     *     <li>0: Only users with the exact same ratings <small>Note: ratings are always rounded to 10%, so this is far from improbable for large numbers of users!</small> for each tag (or that lack values for that tag) will be returned.</li>
     *     <li>1: Same, but also values within a +/- 10% range of the user's values are acceptable</li>
     *     <li>2: Same as 1, but the range extends to +/- 20% now</li>
     * </ul>
     * @return list of users within the bounds specified by levelOfSimilarity
     */
    private List<String> getUsersMatchingSimilarity(String userId, Map<String, UserPreferences.TagPreference> userRatings, int levelOfSimilarity) {
        BoolQueryBuilder query = buildSimilarUserQuery(userId, userRatings, levelOfSimilarity);
        SearchResponse response = es.searchrequest(UserPreferences.INDEX_NAME, query, 1, 100).actionGet();
        List<String> userIds = new ArrayList<>();
        for(SearchHit hit : response.getHits()) {
            userIds.add(hit.getId());
        }
        return userIds;
    }

    // @todo: make sure users with almost no ratings are excluded

    /**
     * decomposition method, see {@link #getUsersMatchingSimilarity(String, Map, int)}
     */
    private BoolQueryBuilder buildSimilarUserQuery(String userId, Map<String, UserPreferences.TagPreference> userRatings, int levelOfSimilarity) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        for(String key : userRatings.keySet()) {
            BoolQueryBuilder partQueryForThisTag = QueryBuilders.boolQuery();
            for(int i = -1 * levelOfSimilarity; i < levelOfSimilarity + 1; i++) {
                partQueryForThisTag.should(QueryBuilders.matchQuery(key, (float) ((i * 0.1) + userRatings.get(key).getValue())));
            }
            partQueryForThisTag.should(QueryBuilders.missingQuery(key));
            partQueryForThisTag.minimumNumberShouldMatch(1);
            query.must(partQueryForThisTag);
        }

        query.mustNot(QueryBuilders.idsQuery(userId));



        return query;
    }

    private List<UserPreferences.TagPreference> getSortedUserPreferences(Map<String, UserPreferences.TagPreference> userRatings) {
        List<UserPreferences.TagPreference> sorted = new ArrayList<>();
        for(String key : userRatings.keySet()) {
            sorted.add(userRatings.get(key));
        }
        Collections.sort(sorted, new Comparator<UserPreferences.TagPreference>() {
            @Override
            public int compare(UserPreferences.TagPreference o1, UserPreferences.TagPreference o2) {
                return Float.compare(o1.getValue(), o2.getValue());
            }
        });
        Collections.reverse(sorted);
        return sorted;
    }


    private MemeRecommendation[] showRandomMemes(int howManyMemes, List<String> memesUserHasRated) {
        QueryBuilder query = QueryBuilders.functionScoreQuery(QueryBuilders.matchAllQuery(),
                ScoreFunctionBuilders.randomFunction(new Random().nextInt()));
        QueryBuilder filter = QueryBuilders.boolQuery().mustNot(QueryBuilders.idsQuery(Meme.INDEX_NAME).ids(memesUserHasRated));
        SearchResponse response = es.searchrequest("memes", query, filter, 0, howManyMemes).actionGet();

        Meme[] memesFromResultSet = getMemesFromResultSet(howManyMemes, response);
        MemeRecommendation[] recommendations = new MemeRecommendation[memesFromResultSet.length];
        for (int i = 0; i < recommendations.length; i++) {
            recommendations[i] = new MemeRecommendation(memesFromResultSet[i], MemeRecommendation.REASON_RANDOM, null);
        }
        return recommendations;
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


    /**
     * <ul>
     * <li>If there are few similar users, this will be very low (can reach 0).</li>
     * <li>However many similar users there are, someone will always have to rate new images, so the value should never be 1 (or close).X</li>
     * <li>The value rises continuously with a bigger number of users.</li>
     * </ul>
     * @param similarUsers
     * List of users that have ratings that are similar to the current user.
     * @return
     * Probability P, 0 <= P < 1.
     */
    private float probabilityOfShowingMemesSimilarUsersLiked(List<String> similarUsers) {
        int numUsers = similarUsers.size();
        return (numUsers / (float) (15 + numUsers)) * 0.8f;
    }

    private UserPreferences getUserPreferences(String userId) {
        UserPreferences up = new UserPreferences();
        up.load(userId, es);
        return up;
    }

    /**
     *
     * @param userRatings ratings for the user for whom the recommendation is made (if 50% of ratings are his, there aren't many ratings...)
     * @return whether or not there are enough ratings to really try and figure out a good recommendation based on
     * other ratings
     */
    private boolean fewRatingsInGeneral(int userRatings) {
        long totalRatingsByAllUsers = Rating.getTotalNumberOfRatings(es);

        if (totalRatingsByAllUsers < 100 && userRatings < totalRatingsByAllUsers * 0.5) return false;
        else if (totalRatingsByAllUsers < 1000 && userRatings < totalRatingsByAllUsers * 0.8) return false;
        else if (totalRatingsByAllUsers < 10000 && userRatings < totalRatingsByAllUsers * 0.95) return false;
        else return totalRatingsByAllUsers <= 100;
    }

    private boolean userHasFewRatings(List<String> memesUserHasRated) {
        return memesUserHasRated.size() < 12;
    }

}
