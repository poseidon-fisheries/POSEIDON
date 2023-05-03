package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Envelope;
import org.jetbrains.annotations.NotNull;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class TestUtilities {

    public static NauticalMap makeCornerPortMap(int width, int height) {
        return makeCornerPortMap(width, height, new GlobalBiology());
    }

    public static NauticalMap makeCornerPortMap(int width, int height, GlobalBiology globalBiology) {
        int[][] altitudes = new int[width][height];
        for (int[] row : altitudes) Arrays.fill(row, -1);
        altitudes[0][0] = 1;
        NauticalMap map = makeMap(altitudes);
        final MarketMap marketMap = new MarketMap(globalBiology);
        globalBiology.getSpecies().forEach(species -> marketMap.addMarket(species, new FixedPriceMarket(0)));
        map.addPort(new Port("", map.getSeaTile(0, 0), marketMap, 0));
        return map;
    }

    public static NauticalMap makeMap(@NotNull int[][] altitude) {
        assert altitude.length > 0;
        assert altitude[0].length > 0;
        ObjectGrid2D grid2D = new ObjectGrid2D(altitude.length, altitude[0].length);
        for (int i = 0; i < altitude.length; i++)
            for (int j = 0; j < altitude[i].length; j++)
                grid2D.set(i, j, new SeaTile(i, j, altitude[i][j], new TileHabitat(0d)));
        final GeomGridField gridField = new GeomGridField(grid2D);
        gridField.setMBR(new Envelope(0, altitude.length, 0, altitude[0].length));
        return new NauticalMap(
            gridField,
            new GeomVectorField(),
            new CartesianDistance(1),
            new StraightLinePathfinder()
        );
    }

    public static NauticalMap makeMap(int width, int height) {
        return makeMap(width, height, -1);
    }

    public static NauticalMap makeMap(int width, int height, int altitude) {
        int[][] altitudes = new int[width][height];
        for (int[] row : altitudes) Arrays.fill(row, altitude);
        return makeMap(altitudes);
    }

    public static Deque<SeaTile> makeRoute(NauticalMap map, int[]... points) {
        final Deque<SeaTile> route = new LinkedList<>();
        for (int[] point : points) route.add(map.getSeaTile(point[0], point[1]));
        return route;
    }

}
