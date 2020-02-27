package uk.ac.ox.oxfish.geography;

import org.jetbrains.annotations.NotNull;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.geography.ports.Port;

import java.util.Arrays;

public class TestUtilities {

    public static NauticalMap makeCornerPortMap(int width, int height) {
        int[][] altitudes = new int[width][height];
        for (int[] row : altitudes) Arrays.fill(row, -1);
        altitudes[0][0] = 1;
        NauticalMap map = makeMap(altitudes);
        map.addPort(new Port("", map.getSeaTile(0, 0), null, 0));
        return map;
    }

    public static NauticalMap makeMap(@NotNull int[][] altitude) {
        assert (altitude.length > 0);
        ObjectGrid2D grid2D = new ObjectGrid2D(altitude.length, altitude[0].length);
        for (int i = 0; i < altitude.length; i++)
            for (int j = 0; j < altitude[i].length; j++)
                grid2D.set(i, j, new SeaTile(i, j, altitude[i][j], new TileHabitat(0d)));
        return new NauticalMap(
            new GeomGridField(grid2D),
            new GeomVectorField(),
            new CartesianDistance(1),
            new StraightLinePathfinder()
        );
    }

    public static NauticalMap makeMap(int width, int height, int altitude) {
        int[][] altitudes = new int[width][height];
        for (int[] row : altitudes) Arrays.fill(row, altitude);
        return makeMap(altitudes);
    }
}
