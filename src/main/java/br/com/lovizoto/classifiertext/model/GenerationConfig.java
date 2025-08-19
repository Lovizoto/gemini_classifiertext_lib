package br.com.lovizoto.classifiertext.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenerationConfig {

    private float temperature;

    @JsonProperty("maxOutputTokens")
    private int maxOutputTokens;

    @JsonProperty("topP")
    private float topP;

    @JsonProperty("topK")
    private int topK;

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    public float getTopP() {
        return topP;
    }

    public void setTopP(float topP) {
        this.topP = topP;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }
}
