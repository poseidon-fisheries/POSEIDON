package uk.ac.ox.poseidon.examples.petersnapper;

public class Factories {

    private Factories() {
    }

    public static TotalLandingsPerYearAccumulatorFactory totalLandingsPerYearAccumulator() {
        return new TotalLandingsPerYearAccumulatorFactory();
    }
}
