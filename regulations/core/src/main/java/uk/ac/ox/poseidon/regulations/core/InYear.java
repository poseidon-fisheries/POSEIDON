package uk.ac.ox.poseidon.regulations.core;

import java.time.LocalDate;

public class InYear extends DateCondition {

    private final int year;

    public InYear(final int year) {
        this.year = year;
    }

    @Override
    boolean test(final LocalDate date) {
        return date.getYear() == year;
    }

    @Override
    public String toString() {
        return "InYear{" +
            "year=" + year +
            '}';
    }
}