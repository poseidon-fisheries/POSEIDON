package uk.ac.ox.oxfish.environment;

/*
This class holds the environmental layer data to be used in a POSEIDON simulation

This includes currents, chlorophyll, temperature, frontal index, shear etc.
These are aspects of the environment that vary from time to time. So this does not include geography (depth)
Yes - this does change but on a much longer time scale. Depth is considered static for a POSEIDON run.
 */

import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;

import java.util.LinkedList;

public class EnvironmentalLayer {
    private LinkedList<EnvironmentalMapFactory> environmentalMaps = new LinkedList<>();

    public EnvironmentalLayer(){   }

    public EnvironmentalLayer(EnvironmentalMapFactory initialMap){
        this.environmentalMaps.add(initialMap);
    }

    public EnvironmentalLayer(final LinkedList<EnvironmentalMapFactory> environmentalMaps){
        this.environmentalMaps = environmentalMaps;
    }

    public LinkedList<EnvironmentalMapFactory> getEnvironmentalMaps() {
        return environmentalMaps;
    }

    public EnvironmentalMapFactory getEnvironmentalMap(String variableName){
        for(int i=0; i<this.environmentalMaps.size(); i++){
            if(environmentalMaps.get(i).getMapVariableName()==variableName) return environmentalMaps.get(i);
        }
        return null;
    }
    public void setEnvironmentalMaps(final LinkedList<EnvironmentalMapFactory> environmentalMaps) {
        this.environmentalMaps = environmentalMaps;
    }

    public void addEnvironmentalMap(final EnvironmentalMapFactory newMap){
        this.environmentalMaps.add(newMap);
    }

    public double getLayerValueNow(String variableName, int gridX, int gridY){

        return 0;
    }

}
