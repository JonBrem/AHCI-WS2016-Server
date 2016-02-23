package de.ur.ahci.model;

import meme_recommender.ElasticSearchContextListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import util.Const;

import java.util.*;

public class UserPrefTotals {

    public static final String ES_INDEX_NAME = "user_pref_total";

    public static void reloadCompletely(ElasticSearchContextListener es) {
        Const.log(Const.LEVEL_VERBOSE, "Reloading total user prefs");
        Map<String, UserPrefDistribution> newDist = new HashMap<>();

        int start = 0, atOnce = 5000;
        while(true) {
            SearchResponse resp = es.searchrequest(UserPreferences.ES_INDEX_NAME, QueryBuilders.matchAllQuery(), start, atOnce).actionGet();
            SearchHits hits = resp.getHits();
            for(SearchHit hit : hits) {
                addValuesToCurrentDist(hit.getSource(), newDist);
            }
            if(hits.getTotalHits() < atOnce) break;
        }
        store(newDist, es);
        Const.log(Const.LEVEL_VERBOSE, "Done reloading total user prefs");
    }

    public static void subtract(Map<String, Object> values, ElasticSearchContextListener es) {
        Map<String, UserPrefDistribution> current = get(es);

        subtractValuesFromCurrentDist(values, current);
        store(current, es);
    }

    private static void subtractValuesFromCurrentDist(Map<String, Object> values, Map<String, UserPrefDistribution> current) {
        for (String key : values.keySet()) {
            if (ignoreKey(key)) continue;

            if (current.containsKey(key)) {
                if(values.get(key) instanceof String) {
                    current.get(key).decreaseValueFor(Double.parseDouble((String) values.get(key)));
                } else if (values.get(key) instanceof Float) {
                    current.get(key).decreaseValueFor((Float) values.get(key));
                }
            }
        }
    }

    private static void store(Map<String, UserPrefDistribution> current, ElasticSearchContextListener es) {
        Map<String, Object> data = new HashMap<>();
        for(String key : current.keySet()) data.put(key, current.get(key).asList());

        String oldId = getOldId(es);

        if(oldId == null) {
            es.indexRequest(ES_INDEX_NAME, data);
        } else {
            es.updateRequest(ES_INDEX_NAME, oldId, data);
        }

    }

    private static String getOldId(ElasticSearchContextListener es) {
        String oldId = null;
        SearchResponse resp = es.searchrequest(ES_INDEX_NAME, QueryBuilders.matchAllQuery(), 0, 1).actionGet();
        if(resp.getHits().getTotalHits() > 0) {
            oldId = resp.getHits().getAt(0).id();
        }
        return oldId;
    }

    public static void add(Map<String, Object> values, ElasticSearchContextListener es) {
        Map<String, UserPrefDistribution> current = get(es);
        addValuesToCurrentDist(values, current);
        store(current, es);
    }

    private static void addValuesToCurrentDist(Map<String, Object> values, Map<String, UserPrefDistribution> current) {
        for (String key : values.keySet()) {
            if (ignoreKey(key)) continue;

            if(!current.containsKey(key)) {
                current.put(key, new UserPrefDistribution());
            }
            if(values.get(key) instanceof String) {
                current.get(key).increaseValueFor(Double.parseDouble((String) values.get(key)));
            } else if (values.get(key) instanceof Float) {
                current.get(key).increaseValueFor((Float) values.get(key));
            } else if (values.get(key) instanceof Double) {
                current.get(key).increaseValueFor((Double) values.get(key));
            } else {
                System.out.println("And the winner is... " +values.get(key).getClass().getName());
            }
        }
    }

    private static boolean ignoreKey(String key) {
        return key.equals(UserPreferences.ES_USER_ID) || key.equals(UserPreferences.ES_LAST_CALCULATED) || key.endsWith(UserPreferences.ES_APPENDIX_TOTAL);
    }

    public static Map<String, UserPrefDistribution> get(ElasticSearchContextListener es) {
        SearchResponse response = es.searchrequest(ES_INDEX_NAME, QueryBuilders.matchAllQuery(), 0, 1).actionGet();
        SearchHits hits = response.getHits();

        if (hits.getTotalHits() > 0) {
            SearchHit value = hits.getAt(0);
            Map<String, Object> source = value.getSource();
            Map<String, UserPrefDistribution> current = new HashMap<>();

            for(String key : source.keySet()) {
                current.put(key, new UserPrefDistribution((List<Integer>) source.get(key)));
            }

            return current;
        } else {
            return new HashMap<>();
        }
    }

    public static class UserPrefDistribution {

        private long[] values;

        public UserPrefDistribution(List<Integer> valuesInSearchHit) {
            values = new long[11];
            for(int i = 0; i < values.length; i++) {
                values[i] = valuesInSearchHit.get(i);
            }
        }

        public UserPrefDistribution() {
            values = new long[11];
            for(int i = 0; i < values.length; i++) values[i] = 0;
        }

        public long getValueFor(double interval) {
            return values[(int) Math.floor(interval * 10)];
        }

        public void setValueFor(double interval, long value) {
            values[(int) Math.floor(interval * 10)] = value;
        }

        public void increaseValueFor(double interval) {
            values[(int) Math.floor(interval * 10)]++;
        }

        public void decreaseValueFor(double interval) {
            values[(int) Math.floor(interval * 10)]--;
        }

        public List<Long> asList() {
            List<Long> list = new ArrayList<>();
            for(long l : values) list.add(l);
            return list;
        }

    }
}
