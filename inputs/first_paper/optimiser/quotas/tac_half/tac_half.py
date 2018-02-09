import spearfish
import sys
import math


original_yaml = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/half.yaml"


# reads the yaml file in and over-writes parameters
def build_input(filename, red, blue
                ):
    import yaml

    print("given " + filename)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)

    quotas = "0:" + str(float(red)) + ",1:" + str(float(blue))
    print(quotas)
    data["Abstract"]["regulation"]["Multi-TAC by List"]["yearlyQuotaMaps"] = quotas

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


def scorer(yamlfile,output_directory):
    cash = -sum([float(x) for x in yamlfile["FishState"]["Species 0 Landings"]]) - float(
        yamlfile["FishState"]["Biomass Species 1"][-1])
    return {"cash": cash}


# Write a function like this called 'main'
def main(job_id, params):
    print 'Anything printed here will end up in the output directory for job #%d' % job_id
    sys.stdout.write("Starting!")

    experiment_name = "tac_half_" + str(job_id)

    print "parameters:"
    print params

    yaml_input = lambda x: build_input(x,
                                       params['red'][0],
                                       params['blue'][0],
                                       )
    result = spearfish.run_experiment(yaml_input,
                                      experiment_name,
                                      scorer=scorer,
                                      years_to_run=20,
                                      additional_data=False)

    return result
