package meme_recommender;

import de.ur.ahci.model.*;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

public class ElasticSearchContextListener implements ServletContextListener {

    public static final String INDEX_NAME = "meme_recommender";
    private static ElasticSearchContextListener instance;

    public static ElasticSearchContextListener getInstace() {
        return instance;
    }

    private static Node node;
    private static Client client;

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        instance = this;

        node = new NodeBuilder().settings(Settings.builder()
                .put("path.home", "C:\\ir\\ahci_es")
                .put("script.inline", "on")
                .put("script.indexed", "off")
                .build()).node();
        client = node.client();

        new Thread(() -> {
            try {
                Thread.sleep(8000);
                UserPrefTotals.reloadCompletely(ElasticSearchContextListener.getInstace());

            } catch (Exception ignored) {}
        }).start();
    }

    /**
     * <strong>Use with extreme caution!!</strong>
     * <p>If possible, use ANY of the other methods. This is for special requirement stuff only.</p>
     * @return
     * The ES client that performs all requests.
     */
    public Client getClient() {
        return client;
    }

    public ActionFuture<IndexResponse> indexRequest(String type, Map<String, Object> data) {
        IndexRequest request = client.prepareIndex(INDEX_NAME, type).setSource(data).request();
        return client.index(request);
    }

    public void indexRequest(String type, Map<String, Object> data, ActionListener<IndexResponse> responseListener) {
        IndexRequest request = client.prepareIndex(INDEX_NAME, type).setSource(data).request();
        client.index(request, responseListener);
    }

    public ActionFuture<UpdateResponse> updateRequest(String type, String id, Map<String, Object> data) {
        UpdateRequest request = client.prepareUpdate(INDEX_NAME, type, id).setDoc(data).request();
        return client.update(request);
    }

    public void updateRequest(String type, String id, Map<String, Object> data, ActionListener<UpdateResponse> responseListener) {
        UpdateRequest request = client.prepareUpdate(INDEX_NAME, type, id).setDoc(data).request();
        client.update(request, responseListener);
    }

    public ActionFuture<DeleteResponse> deleteRequest(String type, String id) {
        DeleteRequest request = client.prepareDelete(INDEX_NAME, type, id).request();
        return client.delete(request);
    }

    public void deleteRequest(String type, String id, ActionListener<DeleteResponse> responseListener) {
        DeleteRequest request = client.prepareDelete(INDEX_NAME, type, id).request();
        client.delete(request, responseListener);
    }

    public ActionFuture<SearchResponse> searchrequest(String type, QueryBuilder query, int start, int size) {
        SearchRequest request = client.prepareSearch(INDEX_NAME).setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
                .setQuery(query)
                .setFrom(start)
                .setSize(size)
                .request();

        return client.search(request);
    }

    public ActionFuture<SearchResponse> searchrequest(String type, QueryBuilder query, QueryBuilder filter, int start, int size) {
        SearchRequest request = client.prepareSearch(INDEX_NAME).setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
                .setQuery(query)
                .setPostFilter(filter)
                .setFrom(start)
                .setSize(size)
                .request();

        return client.search(request);
    }

    public void searchRequest(String type, QueryBuilder query, int start, int size, ActionListener<SearchResponse> responseListener) {
        SearchRequest request = client.prepareSearch(INDEX_NAME).setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
                .setQuery(query)
                .setFrom(start)
                .setSize(size)
                .request();

        client.search(request, responseListener);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        node.close();
    }

    /**
     * Use with extreme caution! Data can NOT be restored!!
     * Deletes all items of the specified type.
     *
     * @param name type name
     */
    private void clearType(String name) {
        int start = 0;
        int size = 5000;
        while (true) {
            SearchResponse response = searchrequest(name, QueryBuilders.matchAllQuery(), start, size).actionGet();
            for(SearchHit hit : response.getHits()) {
                deleteRequest(name, hit.getId()).actionGet();
            }
            if(response.getHits().getTotalHits() < size) break;
            start += size;
        }
    }

}
