package meme_recommender;

import meme_recommender.request_handlers.*;
import util.Const;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A RequestHandler handles a subset of all URLs that reach the server.
 * One example is to match URLs that start with a certain path or pattern and respond to those.
 *
 * All RequestHandlers should be registered in this class.
 */
public abstract class RequestHandler {

    public static List<RequestHandler> requestHandlers;

    static {
        requestHandlers = new ArrayList<>();
        requestHandlers.add(new WebRequestHandler()); // first / index zero because this one handles files as well, and files are the "fallback" if no method can be found.
        requestHandlers.add(new AppRequestHandler());
        requestHandlers.add(new CrawlerRequestHandler());
        requestHandlers.add(new DbInspectionRequestHandler());
        requestHandlers.add(new AnalyzeUT1RequestHandler());
    }

    public static void manageRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx) {
        Const.log(Const.LEVEL_VERBOSE, "Managing Request: " + req.getRequestURI());

        Cookie[] cookies = req.getCookies();
        PrintWriter writer;
        try {
            writer = resp.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        for(int i = requestHandlers.size() - 1; i >= 0; i--) {
            if(requestHandlers.get(i).handleRequest(req, resp, ctx, cookies, writer)) {
                break;
            }
        }
    }

    public abstract boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out);

}
