package de.ur.ahci.machine_learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public class MorrisGraphHandler {

    private Map<String, GraphData> graphs;

    public MorrisGraphHandler() {
        graphs = new HashMap<>();
    }

    public void initGraph(String graphName, String xKey, List<String> yKeys, List<String> xValues) {
        GraphData graphData = new GraphData(xKey, xValues, yKeys);
        graphs.put(graphName, graphData);
    }

    public void increaseDataForGraph(String graphName, String x, String yKey) {
        GraphData graphData = graphs.get(graphName);

        int yIndex = graphData.yKeys.indexOf(yKey);
        int xIndex = graphData.xValues.indexOf(x);
        System.out.println(x + "\t" + xIndex + "\t" + graphData.values.toArray());
//        graphData.values.get(xIndex).values.set(yIndex, graphData.values.get(xIndex).values.get(yIndex) + 1);
    }

    public String getMorrisGraph(String graphName) {
        return graphs.get(graphName).getMorrisRepresentation();
    }

    private class GraphData {
        private String xKey;
        private List<String> xValues;
        private List<String> yKeys;

        private List<GraphValue> values;

        public GraphData(String xKey, List<String> xValues, List<String> yKeys) {
            this.yKeys = yKeys;
            this.xValues = xValues;
            this.xKey = xKey;

            this.values = new ArrayList<>();

            for (String xValue : xValues) {
                GraphValue value = new GraphValue();
                value.x = xValue;
                for (int j = 0; j < yKeys.size(); j++) {
                    value.values.add(0);
                }
                this.values.add(value);
            }
        }

        public String getMorrisRepresentation() {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("{\n");
            stringBuilder.append("\t\"xkey\": \"").append(xKey).append("\",\n");
            stringBuilder.append("\t\"ykeys\": ").append(toJsonArray(yKeys)).append(",\n");
            stringBuilder.append("\t\"labels\": ").append(toJsonArray(yKeys)).append(",\n");
            stringBuilder.append("\t\"data\": ").append(toDataArray(values, yKeys)).append("\n");
            stringBuilder.append("}");

            return stringBuilder.toString();
        }

        private String toDataArray(List<GraphValue> values, List<String> yKeys) {
            StringBuilder builder = new StringBuilder();

            builder.append("[\n");

            for(int i = 0; i < values.size(); i++) {
                builder.append("\t{").append(valueToJson(values.get(i), yKeys)).append("}");
                if(i != values.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }

            builder.append("]");
            return builder.toString();
        }

        private String valueToJson(GraphValue graphValue, List<String> yKeys) {
            StringBuilder builder = new StringBuilder();

            builder.append("\"").append(xKey).append("\" : \"").append(graphValue.x).append("\"");
            for(int i = 0; i < yKeys.size(); i++) {
                builder.append(", ").append("\"").append(yKeys.get(i)).append("\": ").append(graphValue.values.get(i));
            }

            return builder.toString();
        }

        private String toJsonArray(List<String> yKeys) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");

            for(int i = 0; i < yKeys.size(); i++) {
                builder.append("\"").append(yKeys.get(i)).append("\"");
                if(i != yKeys.size() - 1) builder.append(",");
            }

            builder.append("]");
            return builder.toString();
        }
    }

    private class GraphValue {
        private String x;
        private List<Integer> values = new ArrayList<>();


    }
}
