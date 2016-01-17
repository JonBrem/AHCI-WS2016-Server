package meme_recommender.request_handlers;

import de.ur.ahci.model.Tag;
import meme_recommender.ElasticSearchContextListener;
import meme_recommender.RequestHandler;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import util.Const;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DbInspectionRequestHandler extends RequestHandler {
    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String uri = req.getRequestURI();
        uri = uri.substring(1);

        if (!uri.startsWith("inspect_db")) return false;
        if (uri.startsWith("inspect_db/")) {
            handleInspectDbRequest(uri.substring("inspect_db/".length()), out, resp, req);
        }

        return false;
    }

    private void handleInspectDbRequest(String path, PrintWriter out, HttpServletResponse resp, HttpServletRequest req) {
        if (path.equals("get_all_tags")) {
            handleGetAllTagsRequest(out, resp);
        } else if (path.equals("add_new_tag")) {
            handleAddNewTagRequest(out, req.getParameterMap().get("tag_name")[0],
                    req.getParameterMap().containsKey("tag_id") ? (req.getParameterMap().get("tag_id")[0]) : null);
        } else if (path.equals("load_meme")) {
            handleLoadMemeRequest(out, req);
        } else if (path.equals("add_tags_for_meme")) {
            handleAddTagsForMemeRequest(req);
        } else if (path.equals("delete_tag")) {
            handleDeleteTagRequest(req.getParameterMap());
        } else if (path.equals("delete_meme")) {
            handleDeleteMemeRequest(req.getParameterMap());
        }
    }

    private void handleDeleteMemeRequest(Map<String, String[]> parameterMap) {
        ElasticSearchContextListener es = ElasticSearchContextListener.getInstace();
        es.deleteRequest("memes", parameterMap.get("meme_id")[0]);
    }

    private void handleDeleteTagRequest(Map<String, String[]> parameterMap) {
        ElasticSearchContextListener es = ElasticSearchContextListener.getInstace();
        es.deleteRequest("tags", parameterMap.get("tag_id")[0]);

        int start = 0;
        int atATime = 1000;

        while(true) {
            SearchResponse response = es.searchrequest("memes",
                    QueryBuilders.matchQuery("tag_list", parameterMap.get("tag_id")[0]), start, atATime).actionGet();
            SearchHits hits = response.getHits();

            for(int i = 0; i < hits.totalHits(); i++) {
                SearchHit hit = hits.getAt(i);
                List<Object> tagList = (List<Object>) hit.getSource().get("tag_list");

                tagList.remove(parameterMap.get("tag_id")[0]);

                Map<String, Object> data = new HashMap<>();
                data.put("tag_list", tagList);
                es.updateRequest("memes", hit.getId(), data);

                try {
                    Thread.sleep(50);
                } catch (Exception e) {}
            }

            if(hits.totalHits() < atATime) break;

            start += atATime;
        }
    }

    private void handleAddTagsForMemeRequest(HttpServletRequest req) {
        Map<String, String[]> params = req.getParameterMap();
        String memeId = params.get("meme_id")[0];

        ElasticSearchContextListener es = ElasticSearchContextListener.getInstace();

        List<String> tags = new ArrayList<>();
        Collections.addAll(tags, params.get("tag_id")[0].split(","));

        Map<String, Object> data = new HashMap<>();
        data.put("tag_list", tags);

        es.updateRequest("memes", memeId, data);
    }

    private void handleLoadMemeRequest(PrintWriter out, HttpServletRequest req) {
        ElasticSearchContextListener es = ElasticSearchContextListener.getInstace();
        Map<String, String[]> params = req.getParameterMap();

        Client client = es.getClient();
        SearchRequestBuilder builder = client.prepareSearch(ElasticSearchContextListener.INDEX_NAME);
        builder.setFrom(0).setSize(1);

        if(params.containsKey("currentId")) {
            String memeId = params.get("currentId")[0];

            if(!params.containsKey("dir")) {
                builder.setQuery(QueryBuilders.idsQuery("memes").ids(memeId));
            } else {
                SearchResponse response = es.searchrequest("memes", QueryBuilders.idsQuery("memes").ids(memeId), 0, 1).actionGet();
                long currentMemeTime;
                try {
                    currentMemeTime = (long) response.getHits().getAt(0).getSource().get("time_added");
                } catch (Exception e) {
                    e.printStackTrace();
                    out.write("{\"status\":\"no memes found\"}");
                    return;
                }

                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("time_added");

                if (params.get("dir")[0].equals("up")) {
                    rangeQueryBuilder.gt(currentMemeTime);
                    rangeQueryBuilder.lt(Long.MAX_VALUE);
                    builder.addSort(SortBuilders.fieldSort("time_added").order(SortOrder.ASC));
                } else {
                    rangeQueryBuilder.lt(currentMemeTime);
                    rangeQueryBuilder.gt(Long.MIN_VALUE);
                    builder.addSort(SortBuilders.fieldSort("time_added").order(SortOrder.DESC));
                }

                builder.setQuery(rangeQueryBuilder);
            }

        } else {
            builder.addSort(SortBuilders.fieldSort("time_added").order(SortOrder.ASC));
        }


        SearchResponse response = client.search(builder.request()).actionGet();
        SearchHits hits = response.getHits();
        if(hits.totalHits() > 0) {
            Map<String, Object> values = hits.getAt(0).getSource();
            out.write("{\n");

            out.write("\t\"id\": \"" + hits.getAt(0).getId() + "\",\n");
            out.write("\t\"url\": \"" + values.get("url") + "\",\n");
            out.write("\t\"img_url\": \"" + values.get("img_url") + "\",\n");
            out.write("\t\"title\": \"" + values.get("title") + "\"");

            writeTagsForMeme(out, (List<Object>) values.get("tag_list"));

            out.write("}");
        } else {
            out.write("{\"status\":\"no memes found\"}");
        }
    }

    private void writeTagsForMeme(PrintWriter out, List<Object> tagIDs) {
        if (tagIDs != null && tagIDs.size() > 0) {
            out.write(",\n\t\"tags\":[");
            String tags = "";
            for(int i = 0; i < tagIDs.size(); i++) {
                tags += "\"" + tagIDs.get(i) + "\",";
            }
            if (tags.endsWith(",")) tags = tags.substring(0, tags.length() - 1);
            out.write(tags);
            out.write("]\n");
        } else {
            out.write("\n");
        }
    }

    private void handleGetAllTagsRequest(PrintWriter out, HttpServletResponse resp) {
        resp.setStatus(200);
        out.write("[\n");
        List<Tag> tags = getAllTags();
        for (int i = 0; i < tags.size(); i++) {
            out.write("    {\n");
            out.write("        \"id\": \"" + tags.get(i).getId() + "\",\n");
            out.write("        \"name\": \"" + tags.get(i).getTagName() + "\"\n");
            out.write("    }");
            if (i != tags.size() - 1) out.write(",");
            out.write("\n");
        }
        out.write("]");
    }

    private void handleAddNewTagRequest(PrintWriter out, String tagName, String id) {
        ElasticSearchContextListener es = new ElasticSearchContextListener();

        if (id == null) {
            Tag t = new Tag();
            t.setTagName(tagName);
            out.write("{\n\"tagId\":\"" + t.insert(es) + "\"\n}");
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("tag_name", tagName);
            UpdateResponse response = es.updateRequest("tags", id, data).actionGet();
            out.write("{\n\"tagId\":\"" + response.getId() + "\n}");
        }
    }

    private List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();

        ElasticSearchContextListener es = ElasticSearchContextListener.getInstace();
        SearchResponse response = es.searchrequest("tags", QueryBuilders.matchAllQuery(), 0, 10000).actionGet();

        SearchHits hits = response.getHits();
        for(int i = 0; i < hits.totalHits(); i++) {
            Tag tag = new Tag();
            tag.setTagName((String) hits.getAt(i).getSource().get("tag_name"));
            tag.setId(hits.getAt(i).getId());
            tags.add(tag);
        }

        return tags;
    }

}
