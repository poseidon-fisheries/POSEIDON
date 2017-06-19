package uk.ac.ox.oxfish.experiments;

import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.io.geo.ArcInfoASCGridExporter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by carrknight on 4/21/17.
 */
public class SaveBathymetryBack {


    public static void main(String[] args) throws IOException {

        CaliforniaAbundanceScenario scenario = new CaliforniaAbundanceScenario();
        FishState state = new FishState();
        state.setScenario(scenario);

        state.start();

        BufferedWriter writer = new BufferedWriter(new FileWriter("california_final_bathymetry.asc") );

        //only bathymetry
        GeomGridField toConvert = state.getRasterBathymetry();
        GeomGridField bathymetry = new GeomGridField(new DoubleGrid2D(toConvert.getGridWidth(),toConvert.getGridHeight()));
        bathymetry.setPixelHeight(toConvert.getPixelHeight());
        bathymetry.setPixelWidth(toConvert.getPixelWidth());
        bathymetry.setMBR(toConvert.getMBR());
        double[][] field = ((DoubleGrid2D) bathymetry.getGrid()).field;

        for(int i=0; i<field.length; i++)
            for(int j=0; j<field[0].length; j++)
            {
                field[i][j] = ((SeaTile) ((ObjectGrid2D) toConvert.getGrid()).field[i][j]).getAltitude();
            }

        assert  bathymetry.getPixelHeight() == toConvert.getPixelHeight();
        assert  bathymetry.getPixelWidth() == toConvert.getPixelWidth();
        ArcInfoASCGridExporter.write(bathymetry, writer);
        writer.close();


    }
}
