import numpy
import spearfish
import sys

original_yaml = "/home/carrknight/code/oxfish/inputs/first_paper/oil1.yaml"
policy_yaml = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/gas_prices/policy_script.yaml"


# reads the yaml file in and over-writes parameters
def build_input(filename,
                cell_size,
                speed,
                capacity,
                epsilon,
                catchability,
                hold_size,
                fishers):
    import yaml

    print("given " + filename)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)

    data["Abstract"]["gasPricePerLiter"] = float(0)
    data["Abstract"]["mapInitializer"]["Simple Map"]["cellSizeInKilometers"] = float(cell_size)
    data["Abstract"]["speedInKmh"] = float(speed)

    data["Abstract"]["biologyInitializer"]["From Left To Right Fixed"]["maximumBiomass"] = float(capacity)

    data["Abstract"]["destinationStrategy"]["Imitator-Explorator"]["probability"]["Fixed Probability"][
        "explorationProbability"] = float(epsilon)
    data["Abstract"]["gear"]["Random Catchability"]["meanCatchabilityFirstSpecies"] = float(catchability)

    data["Abstract"]["holdSize"] = float(hold_size)
    data["Abstract"]["fishers"] = int(fishers)

    print("ready to output " + filename)
    #   print(data)
    # the directory where it is outputted depends fundamentally on the   caller, not the responsibility
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
    xs = [float(x) for x in yamlfile["FishState"]["Average Distance From Port"]]
    pre_oil_shock_X = [float(x) for x in yamlfile["FishState"]["Average X Towed"]][-1]


    pre_oil_shock_distance = xs[-2]
    last_distance=xs[-1]

    if numpy.isnan(pre_oil_shock_X) or numpy.isnan(last_distance) or  numpy.isnan(pre_oil_shock_distance):
        return {"distance": 100,
                "no_extinction":  -999,
                "initial_distance": -999}

    # +1000 is just to avoid infinity.
    biomass = [float(x) for x in yamlfile["FishState"]["Biomass Species 0"]]
    final_biomass = biomass[-1]

    #looking for the smallest change in distance!
    decrease_in_distance = pre_oil_shock_distance - last_distance
    return {"distance": decrease_in_distance,
            "no_extinction": (final_biomass-2000), #it can't be a weird thing where the fit is crazy bad
            "initial_distance": 30-pre_oil_shock_X} #ignore elements where there is little distance to begin with
# Write a function like this called 'main'
def main(job_id, params):
    print 'Anything printed here will end up in the output directory for job #%d' % job_id
    sys.stdout.write("Starting!")

    experiment_name = "gas_prices_" + str(job_id)

    print "parameters:"
    print params

    yaml_input = lambda x: build_input(x,
                                       params['cell_size'][0],
                                       params['speed'][0],
                                       params['capacity'][0],
                                       params['epsilon'][0],
                                       params['catchability'][0],
                                       params['hold_size'][0],
                                       params['fishers'][0])
    result = spearfish.run_experiment(yaml_input,
                                      experiment_name, scorer=scorer,
                                      years_to_run=3, additional_data=True,
                                      policy_file=policy_yaml)

    return result
