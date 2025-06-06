import spearfish
import sys

original_yaml = "/home/carrknight/code/oxfish/inputs/first_paper/gear_itq.yaml"


# reads the yaml file in and over-writes parameters
def build_input(filename,
                cell_size,
                speed,
                capacity,
                movement,
                epsilon,
                hold_size,
                gas_prices
                ):
    import yaml

    print("given " + filename)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)

    data["Abstract"]["mapInitializer"]["Simple Map"]["cellSizeInKilometers"] = float(cell_size)
    data["Abstract"]["speedInKmh"] = float(speed)

    data["Abstract"]["biologyInitializer"]["Well-Mixed"]["firstSpeciesCapacity"] = float(capacity)
    data["Abstract"]["biologyInitializer"]["Well-Mixed"]["differentialPercentageToMove"] = float(movement)
    # do not limit movement:
    data["Abstract"]["biologyInitializer"]["Well-Mixed"]["percentageLimitOnDailyMovement"] = float(1)

    data["Abstract"]["destinationStrategy"]["Imitator-Explorator"]["probability"]["Fixed Probability"][
        "explorationProbability"] = float(epsilon)

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
    red_catchability = [float(x) for x in yamlfile["FishState"]["Species 0 Catchability"]][-1]

    # do not allow extinction events
    biomass_red = [float(x) for x in yamlfile["FishState"]["Biomass Species 0"]]
    biomass_blue = [float(x) for x in yamlfile["FishState"]["Biomass Species 1"]]
    final_biomass = min(biomass_red[-1], biomass_blue[-1])

    print str(red_catchability)
    return {"red_to_total": red_catchability,
            "positive_biomass": (final_biomass - 2000)  # there needs to be positive biomass all the way to the end
            }


# Write a function like this called 'main'
def main(job_id, params):
    print 'Anything printed here will end up in the output directory for job #%d' % job_id
    sys.stdout.write("Starting!")

    experiment_name = "gear_itq_sensitivity_" + str(job_id)

    print "parameters:"
    print params

    yaml_input = lambda x: build_input(x,
                                       params['cell_size'][0],
                                       params['speed'][0],
                                       params['capacity'][0],
                                       params['movement'][0],
                                       params['epsilon'][0],
                                       params['hold_size'][0],
                                       params['gas_prices'][0]
                                       )
    result = spearfish.run_experiment(yaml_input,
                                      experiment_name,
                                      scorer=scorer,
                                      years_to_run=50,
                                      additional_data=False)

    return result
