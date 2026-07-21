package com.naukri.automation.mail;

public class ExecutionSummary {

    private int featuresPassed;
    private int featuresFailed;
    private int featuresSkipped;

    private int scenariosPassed;
    private int scenariosFailed;
    private int scenariosSkipped;

    private int stepsPassed;
    private int stepsFailed;
    private int stepsSkipped;

    private int stepDurationMs;
    private int scenarioDurationMs;

    public int getStepDurationMs() {
        return stepDurationMs;
    }

    public void setStepDurationMs(int stepDurationMs) {
        this.stepDurationMs = stepDurationMs;
    }

    public int getScenarioDurationMs() {
        return scenarioDurationMs;
    }

    public void setScenarioDurationMs(int scenarioDurationMs) {
        this.scenarioDurationMs = scenarioDurationMs;
    }

    public int getFeaturesPassed() {
        return featuresPassed;
    }

    public void setFeaturesPassed(int featuresPassed) {
        this.featuresPassed = featuresPassed;
    }

    public int getFeaturesFailed() {
        return featuresFailed;
    }

    public void setFeaturesFailed(int featuresFailed) {
        this.featuresFailed = featuresFailed;
    }

    public int getFeaturesSkipped() {
        return featuresSkipped;
    }

    public void setFeaturesSkipped(int featuresSkipped) {
        this.featuresSkipped = featuresSkipped;
    }

    public int getScenariosPassed() {
        return scenariosPassed;
    }

    public void setScenariosPassed(int scenariosPassed) {
        this.scenariosPassed = scenariosPassed;
    }

    public int getScenariosFailed() {
        return scenariosFailed;
    }

    public void setScenariosFailed(int scenariosariosFailed) {
        this.scenariosFailed = scenariosariosFailed;
    }

    public int getScenariosSkipped() {
        return scenariosSkipped;
    }

    public void setScenariosSkipped(int scenariosSkipped) {
        this.scenariosSkipped = scenariosSkipped;
    }

    public int getStepsPassed() {
        return stepsPassed;
    }

    public void setStepsPassed(int stepsPassed) {
        this.stepsPassed = stepsPassed;
    }

    public int getStepsFailed() {
        return stepsFailed;
    }

    public void setStepsFailed(int stepsFailed) {
        this.stepsFailed = stepsFailed;
    }

    public int getStepsSkipped() {
        return stepsSkipped;
    }

    public void setStepsSkipped(int stepsSkipped) {
        this.stepsSkipped = stepsSkipped;
    }
}
