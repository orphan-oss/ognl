package ognl.benchmarks;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;

public class RunBenchmarks {

    public static void main(String[] args) throws RunnerException {
        String baselineFile = "etc/ognl-runtime-benchmark-baseline.json";
        String resultsFile = "target/ognl-runtime-benchmark-results.json";

        Options opt = new OptionsBuilder()
                .include(OgnlRuntimePerformanceBenchmarks.class.getSimpleName())
                .include(OgnlPerformanceBenchmarks.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result(resultsFile)
                .build();

        new Runner(opt).run();

        // If baseline exists, compare results
        File baseline = new File(baselineFile);
        if (baseline.exists()) {
            System.out.println("\nComparing with baseline results:");
            System.out.println("--------------------------------");
            System.out.println("Baseline comparison would be shown here");
            System.out.println("To implement full comparison, parse the JSON files:");
            System.out.println("- Baseline: " + baselineFile);
            System.out.println("- Current: " + resultsFile);
        } else {
            System.out.println("\nNo baseline found. Saving current results as baseline.");
            System.out.println("To save as baseline, copy " + resultsFile + " to " + baselineFile);
        }
    }
}
