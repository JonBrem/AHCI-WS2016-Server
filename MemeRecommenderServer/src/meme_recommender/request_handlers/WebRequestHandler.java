package meme_recommender.request_handlers;

import meme_recommender.RequestHandler;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by jonbr on 28.10.2015.
 */
public class WebRequestHandler extends RequestHandler {

    public WebRequestHandler() {
    }

    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        InputStream s;
        if(req.getRequestURI().length() == 0 || req.getRequestURI().toString().equals("/")) {
            s = ctx.getResourceAsStream("/WEB-INF/index.html");
        } else {
           s = ctx.getResourceAsStream("/WEB-INF/" + req.getRequestURI());
        }

        if(s == null) {
            return false;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(s));
        String line;
        try {
            while((line = r.readLine()) != null) {
                out.write(line + "\n");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
