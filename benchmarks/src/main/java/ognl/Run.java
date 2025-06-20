package ognl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class Run {

    private static final String BASELINE_FILE = "etc/ognl-runtime-benchmark-baseline.json";
    private static final String RESULTS_FILE = "target/ognl-runtime-benchmark-results.json";

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include("ognl.benchmarks.*")
                .resultFormat(ResultFormatType.JSON)
                .result(RESULTS_FILE)
                .build();

        new Runner(opt).run();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode baseline = mapper.readTree(new File(BASELINE_FILE));
        JsonNode current = mapper.readTree(new File(RESULTS_FILE));

        String options = System.getenv("OPTIONS");
        if (options != null && options.contains("publishSummary")) {
            System.out.println("Publishing Summary of comparing results with baseline");
            publishSummary(baseline, current);
        } else {
            System.out.println("Comparing results with baseline");
            compareResults(baseline, current);
        }
    }

    private static void compareResults(JsonNode baseline, JsonNode current) {
        for (JsonNode baseBench : baseline) {
            String benchmark = baseBench.get("benchmark").asText();
            String mode = baseBench.get("mode").asText();
            double baseScore = baseBench.get("primaryMetric").get("score").asDouble();

            // Find matching benchmark in current results
            Iterator<JsonNode> it = current.elements();
            boolean found = false;
            while (it.hasNext()) {
                JsonNode currBench = it.next();
                if (currBench.get("benchmark").asText().equals(benchmark) && currBench.get("mode").asText().equals(mode)) {
                    double currScore = currBench.get("primaryMetric").get("score").asDouble();
                    System.out.printf("%s [%s]: baseline=%.3f, current=%.3f, diff=%.3f (%.1f%%)%n",
                            benchmark, mode, baseScore, currScore, baseScore - currScore, (baseScore / currScore) * 100);
                    found = true;
                }
            }
            if (!found) {
                System.out.printf("----- NOT FOUND -----%n");
                System.out.printf("%s [%s]%n", benchmark, mode);
                System.out.printf("----- NOT FOUND -----%n");
            }
        }
    }

    private static void publishSummary(JsonNode baseline, JsonNode current) throws IOException {
        StringBuilder summary = new StringBuilder();
        summary.append("| Benchmark | Mode | Baseline | Current | Diff | Change (%) |\n");
        summary.append("|-----------|------|----------|---------|------|------------|\n");

        for (JsonNode baseBench : baseline) {
            String benchmark = baseBench.get("benchmark").asText();
            String mode = baseBench.get("mode").asText();
            double baseScore = baseBench.get("primaryMetric").get("score").asDouble();

            Iterator<JsonNode> it = current.elements();
            boolean found = false;
            while (it.hasNext()) {
                JsonNode currBench = it.next();
                if (currBench.get("benchmark").asText().equals(benchmark) && currBench.get("mode").asText().equals(mode)) {
                    double currScore = currBench.get("primaryMetric").get("score").asDouble();
                    double diff = baseScore - currScore;
                    double percent = (currScore != 0) ? ((currScore - baseScore) / baseScore) * 100 : 0;
                    summary.append(String.format("| %s | %s | %.3f | %.3f | %.3f | %.1f%% |\n",
                            benchmark, mode, baseScore, currScore, diff, percent));
                    found = true;
                }
            }
            if (!found) {
                summary.append(String.format("| %s | %s | %.3f | NOT FOUND | - | - |\n", benchmark, mode, baseScore));
            }
        }

        // Write summary to GitHub Actions summary file if available
        String summaryPath = System.getenv("GITHUB_STEP_SUMMARY");
        if (summaryPath != null) {
            Files.writeString(
                    Paths.get(summaryPath),
                    summary.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        }
    }
}
