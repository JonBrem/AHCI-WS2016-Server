package meme_recommender;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Map;

/**
 * Servlet class that directs HTTP-POST and HTTP-GET to the same method, "handleRequest"
 */
public class Servlet extends HttpServlet {

    public Servlet() throws Exception {
        InetAddress ip = InetAddress.getLocalHost();
        String hostname = ip.getHostName();
        System.out.println("Your current IP address : " + ip);
        System.out.println("Your current Hostname : " + hostname);
    }

    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestHandler.manageRequest(req, resp, this.getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
}
