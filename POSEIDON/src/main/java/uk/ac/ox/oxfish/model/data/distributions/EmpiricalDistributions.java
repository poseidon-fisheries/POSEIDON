package uk.ac.ox.oxfish.model.data.distributions;

public interface EmpiricalDistributions {
    double[] get(
        int year,
        String speciesCode
    );
}
