package meme_recommender.request_handlers;

import meme_recommender.DatabaseContextListener;
import meme_recommender.ImportExportRequest;
import meme_recommender.RequestHandler;
import util.Const;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;

/**
 * Created by jonbr on 04.11.2015.
 */
public class DbExportImportRH extends RequestHandler {


    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String uri = req.getRequestURI().substring(1);

        if(!uri.startsWith("exp_imp")) return false;

        Map<String, String[]> params = req.getParameterMap();
        if(params.get("type")[0].equals("import")) {
            importDb(params);
        } else {
            exportDb(params);
        }

        return true;
    }

    private void exportDb(Map<String, String[]> params) {
        DatabaseContextListener db = DatabaseContextListener.getInstance();
        db.export(params.get("table_name")[0], params.get("to_file")[0]);

    }

    private void importDb(Map<String, String[]> params) {
        DatabaseContextListener db = DatabaseContextListener.getInstance();
        db.importData(params.get("table_name")[0], params.get("from_file")[0], params.get("columns")[0], params.get("column_indices")[0]);
    }
}
