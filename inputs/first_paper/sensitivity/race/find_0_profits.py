import spearfish
import sys

original_yaml = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/race/race.yaml"


# reads the yaml file in and over-writes parameters
def build_input(filename,
                maximum_biomass,
                catchability,
                demand_intercept,
                demand_slope,
                gas_prices
                ):
    import yaml

    print("given " + filename)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)

    data["Abstract"]["biologyInitializer"]["From Left To Right Fixed"]["maximumBiomass"] = float(maximum_biomass)
    data["Abstract"]["gear"]["Random Catchability"]["meanCatchabilityFirstSpecies"] = float(catchability)
    data["Abstract"]["gasPricePerLiter"] = float(gas_prices)

    data["Abstract"]["market"]["Moving Average Congested Market"]["demandIntercept"] = float(demand_intercept)
    data["Abstract"]["market"]["Moving Average Congested Market"]["demandSlope"] = float(demand_slope)

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
    cashflow = [float(x) for x in yamlfile["FishState"]["Average Cash-Flow"]]
    cashflow = (cashflow[-1] + cashflow[-2] + cashflow[-3] + cashflow[-4] + cashflow[-5]) / 5.0
    distance = cashflow ** 2

    landings = [float(x) for x in yamlfile["FishState"]["Species 0 Earnings"]][-1]

    print str(distance)
    return {"distance": distance,
            "meaningful": landings-10000}


# Write a function like this called 'main'
def main(job_id, params):
    print 'Anything printed here will end up in the output directory for job #%d' % job_id
    sys.stdout.write("Starting!")

    experiment_name = "find_0_profits_" + str(job_id)

    print "parameters:"
    print params

    yaml_input = lambda x: build_input(x,
                                       params['maximumBiomass'][0],
                                       params['catchability'][0],
                                       params['demand_intercept'][0],
                                       params['demand_slope'][0],
                                       params['gas_prices'][0]
                                       )
    result = spearfish.run_experiment(yaml_input,
                                      experiment_name,
                                      scorer=scorer,
                                      years_to_run=10,
                                      additional_data=False)

    return result
