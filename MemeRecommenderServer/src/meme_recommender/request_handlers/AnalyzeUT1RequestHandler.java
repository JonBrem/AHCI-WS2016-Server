package meme_recommender.request_handlers;

import de.ur.ahci.machine_learning.*;
import de.ur.ahci.machine_learning.simple_predictions.*;
import meme_recommender.RequestHandler;
import util.FileUtility;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Request handler for an analysis of the results of the first user test.
 *
 */
public class AnalyzeUT1RequestHandler extends RequestHandler{

    private static final String[] keys = {"user","img","smiling","leftEyeX","leftEyeY","leftEyeOpen","rightEyeX","rightEyeY","rightEyeOpen","leftMouthX","leftMouthY","rightMouthX","rightMouthY","leftCheekX","leftCheekY","rightCheekX","rightCheekY","noseBaseX","noseBaseY","bottomMouthX","bottomMouthY","faceX","faceY","faceId","eulerY","eulerZ","faceWidth","faceHeight","selectedEmotion","timeStamp"};


    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Cookie[] cookies, PrintWriter out) {
        String url = req.getRequestURI();
        url = url.substring(1);

        if(!url.startsWith("analyze_meme_reaction")) return false;

        if(url.substring("analyze_meme_reaction/".length()).startsWith("load_file")) {
            Map<String, String[]> params = req.getParameterMap();
            try {
                writeDataToClient(params.get("filename")[0], resp.getWriter());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }


    public void writeDataToClient(String file, PrintWriter out) {
        FileUtility fileUtility = new FileUtility();
        out.write("[\n");
        String buffer = null;
        String line;

        while((line = fileUtility.readLine(file)) != null) {
            if(buffer != null) {
                out.write(jsonify(buffer, true));
            }
            buffer = line;
        }
        out.write(jsonify(buffer, false));
        out.write("]");
        out.flush();
        out.close();
    }

    private String jsonify(String line, boolean appendComma) {
        String[] lineParts = line.split("\t");
        StringBuilder json = new StringBuilder();

        json.append("\t{\n");

        int naCount = 0;
        for(int i = 0; i < keys.length; i++) {
            String part = lineParts[i - naCount];

            json.append("\t\t\"").append(keys[i]).append("\":");

            if(part.equals("NA")) {
                json.append("\"").append(part).append("\",\n")
                    .append("\t\t\"").append(keys[i+1]).append("\":\"NA\",\n");
                i++;
                naCount++;
                continue;
            } else {
                json.append(part);
            }

            if(i != keys.length - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("\t}");
        if(appendComma) {
            json.append(",");
        }
        return json.toString();
    }

    private String jsonifyClassifierResults(List<List<ClassifierTestResults>> results, int tabs) {
        StringBuilder json = new StringBuilder();

        String defaultTabs = "";
        for(int i = 0; i < tabs; i++) defaultTabs += "\t";

        json.append(defaultTabs).append("\"results\" : {\n");
        json.append(defaultTabs).append("\t\"general\":[")
                .append(getJsonArrayForClassifierTestResults(results.get(0).get(0))).append("],\n");

        json.append(defaultTabs).append("\t\"perUser\":[\n");
        for(int i = 0; i < results.get(1).size(); i++) {
            json.append(defaultTabs).append("\t\t[").append(getJsonArrayForClassifierTestResults(results.get(1).get(i))).append("]");
            if(i != results.get(1).size() - 1) json.append(",");
            json.append("\n");
        }
        json.append(defaultTabs).append("\t],\n");

        json.append(defaultTabs).append("\t\"perMeme\":[\n");
        for(int i = 0; i < results.get(2).size(); i++) {
            json.append(defaultTabs).append("\t\t[").append(getJsonArrayForClassifierTestResults(results.get(2).get(i))).append("]");
            if(i != results.get(2).size() - 1) json.append(",");
            json.append("\n");
        }
        json.append(defaultTabs).append("\t]\n");

        json.append(defaultTabs).append("}\n");

        return json.toString();
    }

    private String getJsonArrayForClassifierTestResults(ClassifierTestResults classifierTestResults) {
        return classifierTestResults.getPosReactionPosClassified()+ ","
                + classifierTestResults.getPosReactionNegClassified()+ ","
                + classifierTestResults.getPosReactionNotClassified()+ ","
                + classifierTestResults.getNegReactionPosClassified()+ ","
                + classifierTestResults.getNegReactionNegClassified()+ ","
                + classifierTestResults.getNegReactionNotClassified();
    }

}
