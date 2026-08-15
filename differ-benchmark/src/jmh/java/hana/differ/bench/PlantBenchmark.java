package hana.differ.bench;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class PlantBenchmark {
    @Param({"4", "32", "256"})
    public int childCount;

    @Param({"unchanged", "oneLinkChanged"})
    public String scenario;

    private Plant oldPlant;
    private Plant newPlant;

    @Setup
    public void setup() {
        boolean flip = "oneLinkChanged".equals(scenario);
        oldPlant = FixturesKt.plant(childCount, false);
        newPlant = FixturesKt.plant(childCount, flip);
    }

    @Benchmark
    public boolean diff() {
        return PlantDiffer.INSTANCE.diff(oldPlant, newPlant).getHasChanged();
    }
}
