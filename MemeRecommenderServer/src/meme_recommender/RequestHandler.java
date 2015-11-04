package meme_recommender;

import meme_recommender.request_handlers.AppRequestHandler;
import meme_recommender.request_handlers.CrawlerRequestHandler;
import meme_recommender.request_handlers.WebRequestHandler;
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
 * Created by jonbr on 28.10.2015.
 */
public abstract class RequestHandler {

    public static List<RequestHandler> requestHandlers;

    static {
        requestHandlers = new ArrayList<>();
        requestHandlers.add(new WebRequestHandler());
        requestHandlers.add(new AppRequestHandler());
        requestHandlers.add(new CrawlerRequestHandler());
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
