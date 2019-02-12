package com.trivago.vo;

import java.util.List;

public class CucableFeature {
    private String name;
    private List<Integer> lineNumbers;

    public CucableFeature(final String name, final List<Integer> lineNumbers) {
        this.name = name;
        this.lineNumbers = lineNumbers;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    public boolean hasValidScenarioLineNumbers() {
        return lineNumbers != null && !lineNumbers.isEmpty();
    }
}
