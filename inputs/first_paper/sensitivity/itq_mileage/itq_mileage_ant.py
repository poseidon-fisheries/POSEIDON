import spearfish
import sys

original_yaml = "/home/carrknight/code/oxfish/inputs/first_paper/itq_mileage.yaml"


# reads the yaml file in and over-writes parameters
def build_input(filename,
                cell_size,
                speed,
                capacity,
                epsilon,
                catchability,
                hold_size,
                gas_prices
                ):
    import yaml

    print("given " + filename)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)

    data["Abstract"]["mapInitializer"]["Simple Map"]["cellSizeInKilometers"] = float(cell_size)
    data["Abstract"]["speedInKmh"] = float(speed)

    data["Abstract"]["biologyInitializer"]["From Left To Right Fixed"]["maximumBiomass"] = float(capacity)

    data["Abstract"]["destinationStrategy"]["Imitator-Explorator"]["probability"]["Fixed Probability"][
        "explorationProbability"] = float(epsilon)

    data["Abstract"]["destinationStrategy"]["Imitator-Explorator"]["probability"]["Fixed Probability"][
        "explorationProbability"] = float(epsilon)
    data["Abstract"]["gear"]["Random Catchability"]["meanCatchabilityFirstSpecies"] = float(catchability)

    data["Abstract"]["holdSize"] = float(hold_size)

    data["Abstract"]["gasPricePerLiter"] = float(gas_prices)


    print("ready to output " + filename)
    #   print(data)
    # the directory where it is outputted depends fundamentally on the caller, not the responsibility
    # of this class
    try:
        with open(filename, 'w') as outfile:
            outfile.write(str(data))
        print("done with the output")
    except TypeError as err:
        print "Unexpected error:" + str(err)
        print "Unexpected error:" + str(err.args)

    print("dumped file at: " + filename)


def scorer(yamlfile):
    # run a regression looking for a trend between time and distance from port
    final_biomass = [float(x) for x in yamlfile["FishState"]["Biomass Species 0"]][-1]
    correlation = [float(x) for x in yamlfile["FishState"]["Mileage-Catch Correlation"]][-1]



    return {"distance": -correlation, #want to maximize (since this correlation is negative)
            "positive_landings": (final_biomass - 2000)  # there needs to be positive biomass all the way to the end
            }


# Write a function like this called 'main'
def main(job_id, params):
    print 'Anything printed here will end up in the output directory for job #%d' % job_id
    sys.stdout.write("Starting!")

    experiment_name = "itq_mileage_sensitivity_" + str(job_id)

    print "parameters:"
    print params

    yaml_input = lambda x: build_input(x,
                                       params['cell_size'][0],
                                       params['speed'][0],
                                       params['capacity'][0],
                                       params['epsilon'][0],
                                       params['catchability'][0],
                                       params['hold_size'][0],
                                       params['gas_prices'][0]
                                       )
    result = spearfish.run_experiment(yaml_input,
                                      experiment_name,
                                      scorer=scorer,
                                      years_to_run=5,
                                      additional_data=True)

    return result
