#    This file is part of DEAP.
#
#    DEAP is free software: you can redistribute it and/or modify
#    it under the terms of the GNU Lesser General Public License as
#    published by the Free Software Foundation, either version 3 of
#    the License, or (at your option) any later version.
#
#    DEAP is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#    GNU Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with DEAP. If not, see <http://www.gnu.org/licenses/>.

import itertools
import random
from deap import base
from deap import creator
from deap import tools


def scorer(yamlinput):
    return float(yamlinput["FishState"]["Poor Fishers Total Income"][-1]), \
           sum(yamlinput["FishState"]["Species 0 Landings"])

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

    with open("base.yaml", 'r') as infile:
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


def run_ks_model(individual, counter=itertools.count()):
    import spearfish
    id = counter.next()
    experiment_name = "nsga_he_" + str(id)

    print "starting " + str(id)

    yaml_input = lambda x: build_mpa_input(x,
                                           individual[0], individual[1],  # MPA size
                                           individual[2], individual[3],  # MPA top corner
                                           )

    result1 = spearfish.run_experiment(yaml_input, experiment_name, scorer=scorer,
                                       main_directory=
    return result1


creator.create("FitnessMax", base.Fitness, weights=(1.0, 1.0))
creator.create("Individual", list, fitness=creator.FitnessMax)

toolbox = base.Toolbox()


POSITION_BOUND_LOW, POSITION_BOUND_HIGH = 0, 49
SIZE_BOUND_LOW, SIZE_BOUND_HIGH = 0, 49

NDIM = 4


def random_individual(
                      min_position=POSITION_BOUND_LOW,
                      max_position=POSITION_BOUND_HIGH,
                      min_size=SIZE_BOUND_LOW,
                      max_size=SIZE_BOUND_HIGH):
    '''
    returns a random list ready to be fed into the kitchen sink regulation
    :return: list of parameters
    '''
    return [
        random.randint(min_size, max_size),  # height
        random.randint(min_size, max_size),  # width
        random.randint(min_position, max_position),  # top-left x
        random.randint(min_position, max_position)  # top-left y
    ]


toolbox.register("individual", tools.initIterate, creator.Individual, random_individual)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)

toolbox.register("evaluate", run_ks_model)


def rounded_crossover(individual1, individual2, eta):
    '''
    does crossover of each "piece" separately
    '''

    # size
    size1 = [individual1[0]+1, individual1[1]+1]
    size2 = [individual2[0]+1, individual2[1]+1]
    size1, size2 = tools.cxSimulatedBinaryBounded(size1, size2,
                                                  low=SIZE_BOUND_LOW+1, up=SIZE_BOUND_HIGH+1, eta=eta)
    individual1[0], individual1[1] = int(size1[0]-1), int(size1[1]-1)
    individual2[0], individual2[1] = int(size2[0]-1), int(size2[1]-1)

    # position
    pos1 = [individual1[2]+1, individual1[3]+1]
    pos2 = [individual2[2]+1, individual2[3]+1]
    pos1, pos2 = tools.cxSimulatedBinaryBounded(pos1, pos2,
                                                low=POSITION_BOUND_LOW+1, up=POSITION_BOUND_HIGH+1, eta=eta)
    individual1[2], individual1[3] = int(pos1[0]-1), int(pos1[1]-1)
    individual2[2], individual2[3] = int(pos2[0]-1), int(pos2[1]-1)


    return individual1, individual2


toolbox.register("mate", rounded_crossover, eta=20.0)


def rounded_mutate(individual1, eta):
    '''
    does crossover of each "piece" separately
    '''


    # size
    size1 = [individual1[0], individual1[1]]
    size1 = tools.mutPolynomialBounded(size1,
                                       low=SIZE_BOUND_LOW, up=SIZE_BOUND_HIGH, eta=eta, indpb=1.0 / NDIM)[0]
    individual1[0], individual1[1] = int(size1[0]), int(size1[1])

    # position
    pos1 = [individual1[2], individual1[3]]
    pos1 = tools.mutPolynomialBounded(pos1,
                                      low=POSITION_BOUND_LOW, up=POSITION_BOUND_HIGH, eta=eta, indpb=1.0 / NDIM)[0]
    individual1[2], individual1[3] = int(pos1[0]), int(pos1[1])

    return individual1,


toolbox.register("mutate", rounded_mutate, eta=20.0)
toolbox.register("select", tools.selNSGA2)


def main(seed=None):
    random.seed(seed)

    NGEN = 20
    MU = 100
    CXPB = 0.9

    stats = tools.Statistics(lambda ind: ind.fitness.values)
    # stats.register("avg", numpy.mean, axis=0)
    # stats.register("std", numpy.std, axis=0)
    stats.register("min", numpy.min, axis=0)
    stats.register("max", numpy.max, axis=0)

    logbook = tools.Logbook()
    logbook.header = "gen", "evals", "std", "min", "avg", "max"

    pop = toolbox.population(n=MU)

    # Evaluate the individuals with an invalid fitness
    invalid_ind = [ind for ind in pop if not ind.fitness.valid]
    fitnesses = toolbox.map(toolbox.evaluate, invalid_ind)
    for ind, fit in zip(invalid_ind, fitnesses):
        ind.fitness.values = fit

    # This is just to assign the crowding distance to the individuals
    # no actual selection is done
    pop = toolbox.select(pop, len(pop))

    record = stats.compile(pop)
    logbook.record(gen=0, evals=len(invalid_ind), **record)
    print(logbook.stream)

    # Begin the generational process
    for gen in range(1, NGEN):
        # Vary the population
        offspring = tools.selTournamentDCD(pop, len(pop))
        offspring = [toolbox.clone(ind) for ind in offspring]

        for ind1, ind2 in zip(offspring[::2], offspring[1::2]):
            if random.random() <= CXPB:
                toolbox.mate(ind1, ind2)

            toolbox.mutate(ind1)
            toolbox.mutate(ind2)
            del ind1.fitness.values, ind2.fitness.values

        # Evaluate the individuals with an invalid fitness
        invalid_ind = [ind for ind in offspring if not ind.fitness.valid]
        fitnesses = toolbox.map(toolbox.evaluate, invalid_ind)
        for ind, fit in zip(invalid_ind, fitnesses):
            ind.fitness.values = fit
        numpy.savetxt("ks2_pop_temp_"+str(gen)+".txt", numpy.array(pop), delimiter=",", fmt='%s')
        front = numpy.array([ind.fitness.values for ind in pop])
        numpy.savetxt("ks2_front_temp_"+str(gen)+".txt", front, delimiter=",", fmt='%s')

        # Select the next generation population
        pop = toolbox.select(pop + offspring, MU)
        front = numpy.array([ind.fitness.values for ind in pop])
        record = stats.compile(pop)
        logbook.record(gen=gen, evals=len(invalid_ind), **record)
        print(logbook.stream)

    print("Final population hypervolume is ")
    print(pop)
    return pop, logbook


if __name__ == "__main__":
    # with open("pareto_front/zdt1_front.json") as optimal_front_data:
    #     optimal_front = json.load(optimal_front_data)
    # Use 500 of the 1000 points in the json file
    # optimal_front = sorted(optimal_front[i] for i in range(0, len(optimal_front), 2))

    pop, stats = main()
    pop.sort(key=lambda x: x.fitness.values)

    print(stats)
    # print("Convergence: ", convergence(pop, optimal_front))
    # print("Diversity: ", diversity(pop, optimal_front[0], optimal_front[-1]))

    import matplotlib.pyplot as plt
    import numpy

    front = numpy.array([ind.fitness.values for ind in pop])
    # optimal_front = numpy.array(optimal_front)
    # plt.scatter(optimal_front[:,0], optimal_front[:,1], c="r")
    plt.scatter(front[:, 0], front[:, 1], c="b")
    plt.axis("tight")
    plt.show()

    numpy.savetxt("pop_ks2.txt", numpy.array(pop), delimiter=",", fmt='%s')
    numpy.savetxt("front_ks2.txt", front, delimiter=",", fmt='%s')
