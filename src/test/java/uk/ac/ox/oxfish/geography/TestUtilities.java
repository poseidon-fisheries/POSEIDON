package uk.ac.ox.oxfish.geography;

import org.jetbrains.annotations.NotNull;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.Pathfinder;

import static org.mockito.Mockito.mock;

public class TestUtilities {
    public static NauticalMap makeMap(@NotNull int[][] altitude) {
        assert (altitude.length > 0);
        ObjectGrid2D grid2D = new ObjectGrid2D(altitude.length, altitude[0].length);
        for (int i = 0; i < altitude.length; i++)
            for (int j = 0; j < altitude[i].length; j++)
                grid2D.set(i, j, new SeaTile(i, j, altitude[i][j], new TileHabitat(0d)));
        return new NauticalMap(
            new GeomGridField(grid2D), new GeomVectorField(),
            new CartesianDistance(1), mock(Pathfinder.class)
        );
    }
}
