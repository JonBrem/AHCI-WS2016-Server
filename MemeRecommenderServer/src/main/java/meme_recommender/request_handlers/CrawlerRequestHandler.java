package meme_recommender.request_handlers;

import de.ur.ahci.model.Meme;
import de.ur.ahci.model.Tag;
import meme_recommender.ElasticSearchContextListener;
import meme_recommender.RequestHandler;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;



public class CrawlerRequestHandler extends RequestHandler {


    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String uri = req.getRequestURI();
        uri = uri.substring(1);

        if(!uri.startsWith("crawler")) return false;

        if(uri.startsWith("crawler/insert")) {
            Map<String, String[]> params = req.getParameterMap();
            try {
                try {
                    insertMemeIntoDb(params);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                resp.setStatus(200);
                out.write("{\n\tstatus:\"OK\"\n}");
            } catch (SQLException e) {
                e.printStackTrace();
            }
//        } else if(uri.startsWith("crawler/get_all")) { @TODO not implemented since the change from SQL to ES.
//            printWholeMemeDB(out);
        }

        return true;
    }

    private void insertMemeIntoDb(Map<String, String[]> params) throws SQLException, UnsupportedEncodingException {
        Meme m = new Meme();
        m.setTitle(URLDecoder.decode(params.get("title")[0], "UTF-8"));
        m.setUrl(URLDecoder.decode(params.get("url")[0], "UTF-8"));
        m.setImgUrl(URLDecoder.decode(params.get("img_url")[0], "UTF-8"));

        ElasticSearchContextListener es = ElasticSearchContextListener.getInstace();
        String memeId = m.insert(es);

        for(String tag : params.get("tag")) {
            try {
                insertTagIntoDb(tag, memeId, es);
            } catch (Exception e) {

            }
        }

    }

    private void insertTagIntoDb(String tagName, String memeId, ElasticSearchContextListener es) {
        Tag tag = new Tag();
        tag.setTagName(tagName);
        String tagId;
        if(!tag.existsInDb(es)) {
             tagId = tag.insert(es);
        } else {
            try {
                SearchResponse response = es.searchrequest("tags", QueryBuilders.matchQuery("tag_name", tagName), 0, 1).actionGet();
                SearchHits hits = response.getHits();
                if(hits.totalHits() > 0) {
                    tagId = hits.getAt(0).id();
                } else {
                    tagId = null;
                }
            } catch (Exception e) {
                throw e;
            }
        }
        if(tagId != null) tag.insertLink(memeId, tagId, es);
    }

}
