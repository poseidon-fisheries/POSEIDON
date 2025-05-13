package uk.ac.ox.poseidon.agents.behaviours.disposition;

import lombok.Data;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Content;

@Data
public class Disposition<C extends Content<C>> {
    private final Bucket<C> retained;
    private final Bucket<C> discardedAlive;
    private final Bucket<C> discardedDead;

    public static <C extends Content<C>> Disposition<C> empty() {
        return new Disposition<C>(Bucket.empty(), Bucket.empty(), Bucket.empty());
    }
}
