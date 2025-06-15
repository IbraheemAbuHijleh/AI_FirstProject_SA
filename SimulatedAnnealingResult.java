package application;

public class SimulatedAnnealingResult {

private double [] iterationsResults;
private double finalResult;

public SimulatedAnnealingResult() {
}

public SimulatedAnnealingResult(double[] iterationsResults, double finalResult) {
    this.iterationsResults = iterationsResults;
    this.finalResult = finalResult;
}

public double[] getIterationsResults() {
    return iterationsResults;
}

public void setIterationsResults(double[] iterationsResults) {
    this.iterationsResults = iterationsResults;
}

public double getFinalResult() {
    return finalResult;
}

public void setFinalResult(double finalResult) {
    this.finalResult = finalResult;
}

}