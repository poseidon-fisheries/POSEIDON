package uk.ac.ox.oxfish.fisher.heatmap.regression.basis;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 3/7/17.
 */
public class RBFBasisTest {

    @Test
    public void computesCorrectly() throws Exception {

        RBFBasis basis = new RBFBasis(10,2,2);
        double similiarity = basis.evaluate(new double[]{1, 1});

        assertEquals(similiarity,.1141512,.0001);

    }
}