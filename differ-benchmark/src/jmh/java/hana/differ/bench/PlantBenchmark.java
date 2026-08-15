package hana.differ.bench;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.javers.core.Javers;
import org.javers.core.diff.Diff;
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
        oldPlant = FixturesKt.plant(childCount, false, false);
        newPlant = FixturesKt.plant(childCount, flip, true);
    }

    @State(Scope.Thread)
    public static class JaversState {
        private Javers javers;

        @Setup
        public void setup() {
            javers = JaversPlant.INSTANCE.create();
        }
    }

    @State(Scope.Thread)
    public static class JsonState {
        private ObjectMapper mapper;

        @Setup
        public void setup() {
            mapper = JsonPlant.INSTANCE.mapper();
        }
    }

    @Benchmark
    public boolean differ() {
        PlantDiff diff = PlantDiffer.INSTANCE.diff(oldPlant, newPlant);
        return diff.getHasChanged() | (diff.getChanges().size() != 0);
    }

    @Benchmark
    public boolean handwritten() {
        HandwrittenPlant.Result result = HandwrittenPlant.INSTANCE.diff(oldPlant, newPlant);
        return result.getHasChanged() | (result.getChangeCount() != 0);
    }

    @Benchmark
    public boolean javers(JaversState state) {
        Diff diff = state.javers.compare(oldPlant, newPlant);
        return diff.hasChanges() | (diff.getChanges().size() != 0);
    }

    @Benchmark
    public boolean json(JsonState state) {
        return JsonPlant.INSTANCE.diff(state.mapper, oldPlant, newPlant).size() != 0;
    }
}
