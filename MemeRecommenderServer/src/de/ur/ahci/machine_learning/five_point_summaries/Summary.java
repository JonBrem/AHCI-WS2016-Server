package de.ur.ahci.machine_learning.five_point_summaries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Summary<E> {

    List<E> items;

    public Summary() {
        items = new ArrayList<>();
    }

    public void add(E item) {
        items.add(item);
    }

    public String getSummary(Comparator<E> comparator, float[] steps) {
        Collections.sort(items, comparator);
        StringBuilder summaryString = new StringBuilder();
        for(int i = 0; i < steps.length; i++) {
            int index = (int) Math.floor(steps[i] * items.size());
            if(index >= items.size()) index = items.size() - 1;
            float actualStep = Math.round(index / (float) items.size() * 100) / 100f;
            E valueAtIndex = items.get(index);
            summaryString.append(actualStep).append(": ").append(valueAtIndex.toString());
            if(i != steps.length - 1) summaryString.append("\t");
        }
        return summaryString.toString();
    }

    public E getValueAt(Comparator<E> comparator, float step) {
        int index = (int) Math.floor(step * items.size());
        if(index >= items.size()) index = items.size() - 1;
        return items.get(index);
    }

}
