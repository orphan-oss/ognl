package ognl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.IOException;
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

        compareResults();
    }

    private static void compareResults() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode baseline = mapper.readTree(new File(BASELINE_FILE));
        JsonNode current = mapper.readTree(new File(RESULTS_FILE));

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
}
