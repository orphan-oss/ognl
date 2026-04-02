package ognl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
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
    private static final double REGRESSION_THRESHOLD = 10.0;

    public static void main(String[] args) throws Exception {
        ChainedOptionsBuilder builder = new OptionsBuilder()
                .include("ognl.benchmarks.*")
                .resultFormat(ResultFormatType.JSON)
                .result(RESULTS_FILE);

        // Parse -f flag from command-line args to override @Fork annotation
        for (int i = 0; i < args.length; i++) {
            if ("-f".equals(args[i]) && i + 1 < args.length) {
                builder = builder.forks(Integer.parseInt(args[i + 1]));
                i++;
            }
        }

        Options opt = builder.build();
        new Runner(opt).run();

        String options = System.getenv("OPTIONS");

        if (options != null && options.contains("generateBaseline")) {
            System.out.println("Baseline generation complete. Results written to " + RESULTS_FILE);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode baseline = mapper.readTree(new File(BASELINE_FILE));
        JsonNode current = mapper.readTree(new File(RESULTS_FILE));

        int regressions;
        if (options != null && options.contains("publishSummary")) {
            System.out.println("Publishing Summary of comparing results with baseline");
            regressions = publishSummary(baseline, current);
        } else {
            System.out.println("Comparing results with baseline");
            regressions = compareResults(baseline, current);
        }

        if (regressions > 0) {
            System.err.printf("%d benchmark(s) regressed >%.0f%% — failing build%n", regressions, REGRESSION_THRESHOLD);
            System.exit(1);
        }
    }

    private static int compareResults(JsonNode baseline, JsonNode current) {
        int regressionCount = 0;
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
                    double percent = (baseScore != 0) ? ((currScore - baseScore) / baseScore) * 100 : 0;
                    boolean isRegression = isRegression(mode, percent);
                    if (isRegression) regressionCount++;
                    String flag = isRegression ? " <-- REGRESSION" : "";
                    System.out.printf("%s [%s]: baseline=%.3f, current=%.3f, diff=%.3f (%.1f%%)%s%n",
                            benchmark, mode, baseScore, currScore, baseScore - currScore, percent, flag);
                    found = true;
                }
            }
            if (!found) {
                System.out.printf("----- NOT FOUND -----%n");
                System.out.printf("%s [%s]%n", benchmark, mode);
                System.out.printf("----- NOT FOUND -----%n");
            }
        }
        return regressionCount;
    }

    private static int publishSummary(JsonNode baseline, JsonNode current) throws IOException {
        StringBuilder table = new StringBuilder();
        table.append("| Benchmark | Mode | Baseline | Current | Diff | Change (%) | Status |\n");
        table.append("|-----------|------|----------|---------|------|------------|--------|\n");

        int regressionCount = 0;

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
                    double percent = (baseScore != 0) ? ((currScore - baseScore) / baseScore) * 100 : 0;
                    boolean isRegression = isRegression(mode, percent);
                    String status = isRegression ? ":warning:" : ":white_check_mark:";
                    if (isRegression) regressionCount++;
                    table.append(String.format("| %s | %s | %.3f | %.3f | %.3f | %.1f%% | %s |\n",
                            benchmark, mode, baseScore, currScore, diff, percent, status));
                    found = true;
                }
            }
            if (!found) {
                table.append(String.format("| %s | %s | %.3f | NOT FOUND | - | - | :grey_question: |\n", benchmark, mode, baseScore));
            }
        }

        StringBuilder summary = new StringBuilder();
        if (regressionCount == 0) {
            summary.append(":white_check_mark: **No significant regressions detected**\n\n");
        } else {
            summary.append(String.format(":warning: **%d benchmark(s) regressed >%.0f%%**\n\n", regressionCount, REGRESSION_THRESHOLD));
        }
        summary.append(table);

        String summaryPath = System.getenv("GITHUB_STEP_SUMMARY");
        if (summaryPath != null) {
            Files.writeString(
                    Paths.get(summaryPath),
                    summary.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        }

        return regressionCount;
    }

    private static boolean isRegression(String mode, double percentChange) {
        if ("thrpt".equals(mode)) {
            // Throughput: regression if current is >10% lower than baseline (negative percent)
            return percentChange < -REGRESSION_THRESHOLD;
        } else {
            // Average time: regression if current is >10% higher than baseline (positive percent)
            return percentChange > REGRESSION_THRESHOLD;
        }
    }
}
