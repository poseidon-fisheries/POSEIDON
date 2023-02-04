import spearfish
import sys


# TACS
def build_mpa_input(filename, x, y):
    '''
    sets up the yaml to feed into the java model to have ITQs running and trading
    :param filename: the name of the yaml to output
    :param x: the numpy array with the parameters. I just expect 2: quota for first and quota for second species
    :return: nothing
    '''
    import yaml

    print("given " + filename)

    with open("base.yaml", 'r') as infile:
        data = yaml.load(infile)



    data["Prototype"]["regulation"] = dict()
    data["Prototype"]["regulation"]["Multi-TAC"] = {
        "firstSpeciesQuota": float(x),
        "otherSpeciesQuota": float(y)
    }
    # no need for predictors
    data['Prototype']['usePredictors'] = "false"

    print("ready to output " + filename)
    print(data)
    try:
        with open(filename, 'w') as outfile:
            outfile.write(str(data))
        print("done with the output")
    except TypeError as err:
        print "Unexpected error:" + str(err)
        print "Unexpected error:" + str(err.args)

    print("dumped file at: " + filename)


def scorer(yamlfile):
    print(yamlfile)
    print(yamlfile["FishState"]["Species 0 Landings"])

    return -float(yamlfile["FishState"]["Biomass Species 1"][-1]) - sum(yamlfile["FishState"]["Species 0 Landings"])


# Write a function like this called 'main'
def main(job_id, params):
    print 'Anything printed here will end up in the output directory for job #%d' % job_id
    sys.stderr.write("Starting!")

    experiment_name = "tac_" + str(job_id)

    print "parameters:"
    print params

    yaml_input = lambda x: build_mpa_input(x, params['x'][0], params['y'][0])
    result = spearfish.run_experiment(yaml_input, experiment_name,scorer=scorer)

    return result
