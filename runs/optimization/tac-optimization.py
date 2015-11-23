import optimization
import yaml



# TACS
def build_tac_input(filename, x):
    '''
    sets up the yaml to feed into the java model to have ITQs running and trading
    :param filename: the name of the yaml to output
    :param x: the numpy array with the parameters. I just expect 2: quota for first and quota for second species
    :return: nothing
    '''
    with open("base.yaml", 'r') as infile:
        data = yaml.load(infile)

    # change regulations
    data["Prototype"]["regulation"] = dict()
    data["Prototype"]["regulation"]["Multi-TAC"] = {
        "firstSpeciesQuota": float(x[0]),
        "otherSpeciesQuota": float(x[1])
    }
    # you also need to activate the predictors
    data['Prototype']['usePredictors'] = "true"

    with open(filename, 'w') as outfile:
        outfile.write(yaml.dump(data, default_flow_style=True))

title = "mixed_tac"
bounds = [(0,1000000),(0,1000000)]
optimization.optimize(title,build_tac_input,bounds)
