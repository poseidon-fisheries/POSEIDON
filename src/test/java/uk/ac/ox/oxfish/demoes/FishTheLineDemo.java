package uk.ac.ox.oxfish.demoes;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.junit.Assert;
import org.junit.Test;
import sim.field.grid.DoubleGrid2D;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializers;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripIterativeDestinationFactory;
import uk.ac.ox.oxfish.gui.drawing.CoordinateTransformer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static uk.ac.ox.oxfish.demoes.FunctionalFriendsDemo.stepsItTook;

/**
 * I write here in unit-test format the results of the demoes I show on the website. This will help make sure
 * these results are stable whenever I change the code.
 * Created by carrknight on 8/3/15.
 */
public class FishTheLineDemo {







    //create an MPA, after people fish everything else there is to fish, they'll mostly fish just at the border of
    //the MPA
    @Test
    public void fishTheLine()
    {

        int yearsBeforeCheck = 15;
        int MPAx = 20;
        int MPAy=20;
        int MPAheight=10;
        int MPAwidth = 10;

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(BiologyInitializers.CONSTRUCTORS.get("Diffusing Logistic").get());
        scenario.setRegulation(new ProtectedAreasOnlyFactory());
        scenario.setFishers(300);

        FishState state = new FishState(System.currentTimeMillis(), 1);
        state.setScenario(scenario);
        state.start();
        state.getMap().guiStart(state); //needed to have fishing hotspots
        state.schedule.step(state);
        CoordinateTransformer transformer = new CoordinateTransformer(null,state.getMap());

        //create the MPA
        GeometricShapeFactory geometryFactory = new GeometricShapeFactory();
        Point base = transformer.gridToJTSPoint(MPAx, MPAy);
        Point end = transformer.gridToJTSPoint(MPAx+MPAwidth, MPAy + MPAheight);
        geometryFactory.setBase( base.getCoordinate());
        geometryFactory.setHeight(end.getY() - base.getY());
        geometryFactory.setWidth(end.getX() - base.getX());
        final Polygon rectangle = geometryFactory.createRectangle();
        state.getMap().getMpaVectorField().addGeometry(new MasonGeometry(rectangle));
        state.getMap().recomputeTilesMPA();


        //make sure it drew correctly
        Assert.assertTrue(state.getMap().getSeaTile(MPAx,MPAy).isProtected());
        Assert.assertTrue(state.getMap().getSeaTile(MPAx+MPAwidth,MPAy+MPAwidth).isProtected());
        Assert.assertFalse(state.getMap().getSeaTile(MPAx-1,MPAy-1).isProtected());
        Assert.assertFalse(state.getMap().getSeaTile(MPAx+MPAwidth+1,MPAy+MPAwidth+1).isProtected());


        //run it for a long time
        while(state.getYear() <= yearsBeforeCheck)
            state.schedule.step(state);

        //now check the hotspots
        double allHotspots = 0;
        double onTheLine = 0;
        DoubleGrid2D hotspots = state.getMap().getFishedMap();
        for(int x =0; x<state.getMap().getWidth(); x++)
        {
            for (int y = 0; y < state.getMap().getHeight(); y++)
            {
                double hotspot = hotspots.get(x, y);
                allHotspots += hotspot;
                if(x >=  MPAx - 1 && x <= MPAx + 1 + MPAwidth && y>= MPAy-1  && y<=MPAy + 1 + MPAheight)
                    onTheLine+= hotspot;
                //also hotspot should be 0 in the MPA itself
                if(x >=  MPAx && x <= MPAx + MPAwidth && y>= MPAy  && y<=MPAy  + MPAheight)
                    Assert.assertEquals(0,hotspot,.0001);
            }

        }

        //on the line fishing make up at least 40% of all recent fishing (since exploration is pretty aggressive anyway)
        System.out.println(allHotspots + " --- " + onTheLine);
        System.out.println("percentage fished on the line : " + onTheLine/allHotspots);
        Assert.assertTrue(allHotspots * .40 <= onTheLine);
        Assert.assertTrue(onTheLine > 0);


    }




}