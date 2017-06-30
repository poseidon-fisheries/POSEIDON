import spearfish
import sys

# TACS
def build_mpa_input(filename, height,
                    width, x, y):
    '''
    sets up the yaml to feed into the java model to have ITQs running and trading
    :param filename: the name of the yaml to output
    :param x: the numpy array with the parameters. I just expect 2: quota for first and quota for second species
    :return: nothing
    '''
    import yaml

    print("given " + filename)

    with open("separated.yaml", 'r') as infile:
        data = yaml.load(infile)



    data["Prototype"]["startingMPAs"] = [{
        "height": int(height),
        "width": int(width),
        "topLeftX": int(x),
        "topLeftY": int(y)
    }]

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



# Write a function like this called 'main'
def main(job_id, params):
    print 'Anything printed here will end up in the output directory for job #%d' % job_id
    sys.stderr.write("Starting!")

    experiment_name = "mpa_" + str(job_id)

    print "parameters:"
    print params
    print params['height']
    print ['height'][0]
    print "done with parameters!:"

    yaml_input = lambda x: build_mpa_input(x, params['height'][0], params['width'][0], params['x'][0], params['y'][0])
    result = spearfish.run_experiment(yaml_input, experiment_name)

    return result

# params = {"x":0,"y":1,"width":50,"height":3}
# job_id=22
# print 'Anything printed here will end up in the output directory for job #%d' % job_id
# sys.stderr.write("Starting!")
#
# experiment_name = "mpa_" + str(job_id)
#
# print params
# yaml_input = lambda x: build_mpa_input(x, params['height'], params['width'], params['x'], params['y'])
# result = spearfish.run_experiment(yaml_input, experiment_name)
