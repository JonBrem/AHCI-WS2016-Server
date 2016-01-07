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

    private static Map<String, Classifier> classifiersMap;
    private MorrisGraphHandler morrisGraphHandler;

    static {
        classifiersMap = new HashMap<>();
        classifiersMap.put("summary", new SummaryClassifier());
        classifiersMap.put("average", new AverageClassifier());
        classifiersMap.put("mouthsize", new MouthSizeClassifier());
    }

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
        } else if(url.substring("analyze_meme_reaction/".length()).startsWith("load_classifiers")) {
            try {
                writeClassifiersToClient(resp.getWriter());
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else if(url.substring("analyze_meme_reaction/".length()).startsWith("run_tests")) {
            try {
                morrisGraphHandler = new MorrisGraphHandler();
                TestResults results = runTests(req.getParameterMap());
                writeResultsToClient(results, morrisGraphHandler, resp.getWriter(), req.getParameterMap());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private void writeResultsToClient(TestResults results, MorrisGraphHandler morrisGraphHandler, PrintWriter writer, Map<String, String[]> parameterMap) {
        String[] classifierNames = parameterMap.get("classifiers")[0].split(",");

        writer.write("[\n");

        for(int i = 0; i < classifierNames.length; i++) {
            writer.write("\t{\n");
            writer.write("\t\t\"name\": \"" + classifierNames[i] + "\",\n");
            String graph = morrisGraphHandler.getMorrisGraph(classifierNames[i]);
            if(graph != null) {
                writer.write("\t\t\"graph\": ");
                writer.write(graph);
                writer.write(",\n");
            }
            writer.write(jsonifyClassifierResults(results.getResultsForClassifier(classifierNames[i]), 2));
            writer.write("\t}");
            if(i != classifierNames.length - 1) writer.write(",");
            writer.write("\n");
        }

        writer.write("]");
        writer.flush();
    }

    private TestResults runTests(Map<String, String[]> parameterMap) {
        TestResults results = new TestResults();

        String[] classifierNames = parameterMap.get("classifiers")[0].split(",");

        Map<String, Classifier> classifiers = new HashMap<>();

        for(int i = 0; i < classifierNames.length; i++) {
            Classifier classifier = classifiersMap.get(classifierNames[i]);
            for(String paramName : classifier.getParameters()) {
                classifier.setParameter(paramName, Float.parseFloat(parameterMap.get(classifierNames[i] + "_" + paramName)[0]));
            }

            morrisGraphHandler.initGraph(classifierNames[i], classifier.getMorrisXKey(), classifier.getMorrisYKeys(), classifier.getAllMorrisXKeys());
            classifiers.put(classifierNames[i], classifier);
        }

        TestDataStream testDataStream = new TestDataStream("C:/users/jonbr/Desktop/utd");
        TestDataStreamReader testDataStreamReader = new TestDataStreamReader(testDataStream);
        testDataStreamReader.forEveryTestData(testData -> this.handleTestData(testData, classifiers, results, testData.getUserId()));

        return results;
    }

    private void handleTestData(TestData testData, Map<String, Classifier> classifiers, TestResults results, int userId) {
        testData.forEveryMemeReactionData(memeReactionData -> this.handleMemeReactionData(memeReactionData, classifiers, results, userId));
    }

    private void handleMemeReactionData(MemeReactionData data, Map<String, Classifier> classifiers, TestResults results, int userId) {
        for(String classifierName : classifiers.keySet()) {
            Classifier classifier = classifiers.get(classifierName);
            int prediction = classifier.predict(data);
            morrisGraphHandler.increaseDataForGraph(classifierName, classifier.getMorrisXKeyFor(data), classifier.getMorrisYKeyFor(data));
            if(data.getSelectedEmotion() <= 1) {
                results.addPredictionForNegativeReaction(classifierName, prediction, userId+"", data.getImageNumber()+"");
            } else if(data.getSelectedEmotion() > 1) {
                results.addPredictionForPositiveReaction(classifierName, prediction, userId+"", data.getImageNumber()+"");
            }
        }
    }

    private void writeClassifiersToClient(PrintWriter writer) {
        StringBuilder json = new StringBuilder();

        json.append("[\n");
        boolean atLeastOneClassifier = false;

        for(String name : classifiersMap.keySet()) {
            atLeastOneClassifier = true;
            json.append(jsonifyClassifier(name, classifiersMap.get(name), 1)).append(",\n");
        }

        if(atLeastOneClassifier) {
            json.delete(json.length() - 2, json.length() - 1);
        }

        json.append("]");

        writer.write(json.toString());
        writer.flush();
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

    private String jsonifyClassifier(String name, Classifier classifier, int tabs) {
        StringBuilder json = new StringBuilder();

        String defaultTabs = "";
        for(int i = 0; i < tabs; i++) defaultTabs += "\t";

        json.append(defaultTabs).append("{\n");
        json.append(defaultTabs).append("\t").append("\"name\":\"").append(name).append("\",\n");
        json.append(defaultTabs).append("\t").append("\"params\":").append("[\n");

        boolean atLeastOneParam = false;
        for(String paramName : classifier.getParameters()) {
            atLeastOneParam = true;
            json.append(defaultTabs).append("\t\t\"").append(paramName).append("\",\n");
        }

        if(atLeastOneParam) json.deleteCharAt(json.length() - 2);


        json.append(defaultTabs).append("\t").append("]\n");
        json.append(defaultTabs).append("}");

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
