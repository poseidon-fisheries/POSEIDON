# Optimization
### Optimization parameters 
```
!!eva2.optimization.OptimizationParameters
optimizer: !!eva2.optimization.strategies.ParticleSwarmOptimizationGCPSO
    algoType: Constriction
    checkRange: false
    checkSpeedLimit: false
    dmsRegroupGens: 10
    gcpso: true
    inertnessOrChi: 0.7298437881283576
    initialVelocity: 0.2
    maxSubSwarmSize: 0
    parameterControl: []
    phi1: 2.05
    phi2: 2.05
    population: !population
        initAround: 0.1
        initMethod: individualDefault
        initPos: [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
        popMetric: !!eva2.optimization.operator.distancemetric.PhenotypeMetric {}
        seedCardinality: {head: 5, tail: 1}
        targetSize: 50
    rho: 1.0
    show: false
    sleepTime: 0
    speedLimit: 0.1
    subSwarmRadius: 0.2
    topology: grid
    topologyRange: 2
    wrapTopology: true
postProcessParams: !!eva2.optimization.operator.postprocess.PostProcessParams
    PPMethod: nelderMead
    accAssumeConv: 1.0E-8
    accMaxEval: -1
    accuracies: [0.01]
    doPostProcessing: false
    postProcessClusterSigma: 0.05
    postProcessSteps: 5000
    printNBest: 10
    withPlot: false
problem: !!uk.ac.ox.oxfish.maximization.TunaCalibrator$1
    defaultAccuracy: 0.001
    defaultRange: 17.0
    individualTemplate: !!eva2.optimization.individuals.ESIndividualDoubleData
        areaConst4ParallelViolated: false
        crossoverOperator: !!eva2.optimization.operator.crossover.CrossoverESDefault {}
        crossoverProbability: 0.5
        initOperator: !!eva2.optimization.operator.initialization.DefaultInitialization {}
        isMarked: false
        isPenalized: false
        mutationOperator: !!eva2.optimization.operator.mutation.MutateESGlobal {crossoverType: intermediate,
            lowerLimitStepSize: 5.0E-7, mutationStepSize: 0.2, tau1: 0.15}
        mutationProbability: 1.0
    noise: 0.0
    parallelThreads: 32
    problemDimension: 10
    simpleProblem: !!uk.ac.ox.oxfish.maximization.GenericOptimization
        maximization: false
        parameters:
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: destinationStrategy.additionalHourlyDelayDolphinSets,
            alwaysPositive: true, maximum: 15.866645309617505, minimum: 10.577763539745005,
            rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: destinationStrategy.additionalHourlyDelayNonAssociatedSets,
            alwaysPositive: true, maximum: 1.4102486111047237, minimum: 0.9401657407364825,
            rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: destinationStrategy.minimumValueOpportunisticFadSets,
            alwaysPositive: true, maximum: 12283.520979055864, minimum: 8189.013986037243,
            rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: destinationStrategy.ownFadActionWeightBias,
            alwaysPositive: true, maximum: 57.67303712320782, minimum: 38.448691415471885,
            rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: destinationStrategy.deploymentBias,
            alwaysPositive: true, maximum: 16.425450015049407, minimum: 10.950300010032938,
            rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: destinationStrategy.noaBias,
            alwaysPositive: true, maximum: 0.5386733659737644, minimum: 0.359115577315843,
            rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: destinationStrategy.minimumPercentageOfTripDurationAllowed,
            alwaysPositive: true, hardMaximum: 0.7, hardMinimum: 0.1, maximum: 0.5953960595639097,
            minimum: 0.39693070637593986, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: abundancePurseSeineGearFactory.successfulSetProbability,
            alwaysPositive: true, hardMaximum: 1.0, hardMinimum: 0.5, maximum: 0.6,
            minimum: 0.4, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: destinationStrategy.fadModule.discretization.verticalSplits,
            alwaysPositive: true, hardMaximum: 50.0, hardMinimum: 5.0, maximum: 6.0,
            minimum: 4.0, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: destinationStrategy.fadModule.discretization.horizontalSplits,
            alwaysPositive: true, hardMaximum: 50.0, hardMinimum: 1.0, maximum: 4.704015603649301,
            minimum: 3.1360104024328677, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: destinationStrategy.fadModule.intercept,
            alwaysPositive: true, maximum: 0.04906494084962138, minimum: 0.03270996056641425,
            rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: destinationStrategy.fadModule.slope,
            alwaysPositive: true, maximum: 4.18715832239931, minimum: 2.7914388815995403,
            rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.fadDudRate,
            alwaysPositive: true, hardMaximum: 1.0, hardMinimum: 0.0, maximum: 0.055331399511533076,
            minimum: 0.03688759967435539, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.environmentalThresholds$0,
            alwaysPositive: true, hardMaximum: 0.5, hardMinimum: 0.0, maximum: 0.06290771350318429,
            minimum: 0.041938475668789534, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.environmentalThresholds$1,
            alwaysPositive: true, hardMaximum: 35.0, hardMinimum: 10.0, maximum: 37.81291903897902,
            minimum: 25.20861269265268, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.environmentalThresholds$2,
            alwaysPositive: true, hardMaximum: 25.0, hardMinimum: 0.0, maximum: 0.05580252581587178,
            minimum: 0.03720168387724786, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.environmentalThresholds$3,
            alwaysPositive: true, hardMaximum: 2.0, hardMinimum: 0.1, maximum: 0.2837702015609665,
            minimum: 0.18918013437397768, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: fadInitializerFactory.carryingCapacityScaleParameters~Bigeye
                tuna, alwaysPositive: true, maximum: 12489.298067609609, minimum: 8326.19871173974,
            rawNumber: true}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: fadInitializerFactory.carryingCapacityScaleParameters~Skipjack
                tuna, alwaysPositive: true, maximum: 17745.879724196424, minimum: 11830.586482797617,
            rawNumber: true}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: fadInitializerFactory.carryingCapacityScaleParameters~Yellowfin
                tuna, alwaysPositive: true, maximum: 19157.766328093487, minimum: 12771.844218728993,
            rawNumber: true}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.carryingCapacityShapeParameters~Bigeye
                tuna, alwaysPositive: true, hardMaximum: 2.0, hardMinimum: 1.0E-4,
            maximum: 1.0654045572520006, minimum: 0.7102697048346671, rawNumber: true}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.carryingCapacityShapeParameters~Skipjack
                tuna, alwaysPositive: true, hardMaximum: 2.0, hardMinimum: 1.0E-4,
            maximum: 0.8888376055416183, minimum: 0.5925584036944123, rawNumber: true}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.carryingCapacityShapeParameters~Yellowfin
                tuna, alwaysPositive: true, hardMaximum: 2.0, hardMinimum: 1.0E-4,
            maximum: 2.4, minimum: 1.6, rawNumber: true}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: fadInitializerFactory.catchabilities~Bigeye
                tuna, alwaysPositive: true, maximum: 0.21193225179957856, minimum: 0.14128816786638573,
            rawNumber: true}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: fadInitializerFactory.catchabilities~Skipjack
                tuna, alwaysPositive: true, maximum: 0.060131976658428926, minimum: 0.04008798443895262,
            rawNumber: true}
        - !!uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter {addressToModify: fadInitializerFactory.catchabilities~Yellowfin
                tuna, alwaysPositive: true, maximum: 0.016734593532363125, minimum: 0.011156395688242084,
            rawNumber: true}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.daysInWaterBeforeAttraction,
            alwaysPositive: true, hardMaximum: 2.14748E9, hardMinimum: 0.0, maximum: 1.3378759770597808,
            minimum: 0.891917318039854, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.fishReleaseProbabilityInPercent,
            alwaysPositive: true, hardMaximum: 3.5, hardMinimum: 0.0, maximum: 4.2,
            minimum: 2.8000000000000003, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: abundancePurseSeineGearFactory.successfulSetProbability,
            alwaysPositive: true, hardMaximum: 1.0, hardMinimum: 0.5, maximum: 1.2,
            minimum: 0.8, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: fadInitializerFactory.fishReleaseProbabilityInPercent,
            alwaysPositive: true, hardMaximum: 3.5, hardMinimum: 0.0, maximum: 2.615939062001465,
            minimum: 1.7439593746676434, rawNumber: false}
        - !!uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter {addressToModify: abundancePurseSeineGearFactory.fishValueCalculatorStandardDeviation,
            alwaysPositive: true, hardMaximum: 1.0, hardMinimum: 0.0, maximum: 0.01939138758672705,
            minimum: 0.0129275917244847, rawNumber: false}
        runsPerSetting: 2
        scenarioFile: inputs/epo_inputs/calibration/scenario_LCWCC_VPS.yaml
        simulatedYears: 2
        targets:
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of FAD deployments, fixedTarget: 27397.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of sets on own FADs, fixedTarget: 7483.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of sets on others' FADs, fixedTarget: 3090.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of non-associated sets, fixedTarget: 5472.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of dolphin sets, fixedTarget: 8768.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Bigeye tuna catches from dolphin sets, fixedTarget: 1000.0, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Skipjack tuna catches from dolphin sets, fixedTarget: 1621000.0,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Yellowfin tuna catches from dolphin sets, fixedTarget: 1.08274E8,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Bigeye tuna catches from sets on own FADs, fixedTarget: 4.6188E7,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Skipjack tuna catches from sets on own FADs, fixedTarget: 1.3866E8,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Yellowfin tuna catches from sets on own FADs, fixedTarget: 3.6789E7,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Bigeye tuna catches from non-associated sets, fixedTarget: 1555000.0,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Skipjack tuna catches from non-associated sets, fixedTarget: 8.9138E7,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Yellowfin tuna catches from non-associated sets, fixedTarget: 2.8344E7,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Bigeye tuna catches from sets on others' FADs, fixedTarget: 1.4516E7,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Skipjack tuna catches from sets on others' FADs, fixedTarget: 4.4836E7,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Yellowfin tuna catches from sets on others' FADs, fixedTarget: 1.467E7,
            verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Average
                Trip Duration, fixedTarget: 833.9759066, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Average
                Hours Out, fixedTarget: 5294.1343462, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of dolphin sets (East), fixedTarget: 1663.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of dolphin sets (North), fixedTarget: 6258.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of dolphin sets (South), fixedTarget: 722.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of dolphin sets (West), fixedTarget: 125.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of FAD deployments (East), fixedTarget: 6347.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of FAD deployments (North), fixedTarget: 9241.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of FAD deployments (South), fixedTarget: 9634.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of FAD deployments (West), fixedTarget: 2175.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of FAD sets (East), fixedTarget: 1803.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of FAD sets (North), fixedTarget: 4433.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of FAD sets (South), fixedTarget: 2507.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of FAD sets (West), fixedTarget: 1830.0, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of non-associated sets (East), fixedTarget: 2719.0, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of non-associated sets (North), fixedTarget: 2100.0, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of non-associated sets (South), fixedTarget: 547.0, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Number
                of non-associated sets (West), fixedTarget: 106.0, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Bigeye tuna catches (East), fixedTarget: 3055000.0, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Skipjack tuna catches (East), fixedTarget: 8.5087E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Yellowfin tuna catches (East), fixedTarget: 4.7176E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Bigeye tuna catches (North), fixedTarget: 2.0502E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Skipjack tuna catches (North), fixedTarget: 8.2063E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Yellowfin tuna catches (North), fixedTarget: 1.01659E8, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Bigeye tuna catches (South), fixedTarget: 1.9749E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Skipjack tuna catches (South), fixedTarget: 4.6408E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Yellowfin tuna catches (South), fixedTarget: 2.7931E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Bigeye tuna catches (West), fixedTarget: 1.8954E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Skipjack tuna catches (West), fixedTarget: 6.0697E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Sum
                of Yellowfin tuna catches (West), fixedTarget: 1.1311E7, verbose: false,
            weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Bigeye
                tuna Catches (kg), fixedTarget: 6.226E7, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Skipjack
                tuna Catches (kg), fixedTarget: 2.74255E8, verbose: false, weight: 1.0}
        - !!uk.ac.ox.oxfish.maximization.generic.SmapeDataTarget {columnName: Yellowfin
                tuna Catches (kg), fixedTarget: 1.88077E8, verbose: false, weight: 1.0}
        translateNANto: 1.0E7
randomSeed: 1670302090469
terminator: !!eva2.optimization.operator.terminators.EvaluationTerminator {fitnessCalls: 5000}

```

## Multirun 1
| FunctionCalls | currentBest | meanFit | currentWorst | runBest | currentBestFeasible | runBestFeasible | avgEucPopDistance | maxEucPopDistance | avgPopMetricDist | maxPopMetricDist | solution | sigma | meanCurSpeed | 
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | 
| 100 | 613.4809651865717 | 4000397.04934342 | 1.0E7 | 613.4809651865717 | 553.2656876197593 | 553.2656876197593 | 64.8795611114075 | 100.85663064049258 | 1.908222385629628 | 2.9663714894262525 | { 6.222, 14.444,-8.178,-11.847, 2.476,-2.995, 1.517,-1.881, 4.052,-9.081, 6.253, 4.597,-14.523, 6.290, 6.500, 9.457,-0.753,-0.375, 5.525, 18.157,-7.630,-2.302,-6.407,-4.630, 20.273,-12.235, 9.252, 12.847, 6.051,-2.470, 14.023} | 0.2 | 0.30606485065850036 | 
| 150 | 543.6665405246232 | 2200534.322534495 | 1.0E7 | 543.6665405246232 | 543.6665405246232 | 543.6665405246232 | 77.40736592438344 | 134.55551179458362 | 2.276687233070103 | 3.9575150527818717 | { 3.046, 20.716,-16.982,-22.721, 8.573,-0.735, 3.920, 4.594,-12.798,-0.466,-7.148, 6.159,-12.850, 15.830,-6.698, 16.969, 9.312,-4.481,-1.703, 20.153,-16.345, 11.976,-13.583,-18.014, 37.153,-7.132, 5.540, 7.553, 13.427,-10.273, 12.138} | 0.2 | 0.29286794874598876 | 
| 200 | 506.4408953066975 | 1000596.166676457 | 1.0E7 | 506.4408953066975 | 506.4408953066975 | 506.4408953066975 | 87.57102359594144 | 168.31214427887716 | 2.575618341057102 | 4.950357184672858 | { 1.278, 13.972,-27.920,-23.526, 3.815, 5.470, 3.493,-6.292,-7.905,-10.385,-6.170, 3.444,-9.201, 10.134,-4.927, 20.837, 8.162,-19.959, 3.340, 13.511,-16.430, 2.127,-9.143,-16.788, 29.474,-2.495, 18.769, 17.616, 5.682,-9.154, 11.817} | 0.2 | 0.33237945646655154 | 
| 250 | 517.0416365079044 | 2600471.559060306 | 1.0E7 | 506.4408953066975 | 517.0416365079044 | 506.4408953066975 | 89.57064143778581 | 156.2600388430143 | 2.6344306305231173 | 4.5958834953827745 | {-0.163, 31.738,-10.224,-30.152, 5.078,-0.803, 4.819,-9.027,-50.693,-0.985,-9.267, 2.410,-3.957, 9.867,-2.866, 31.280, 7.427,-14.825,-4.713, 40.028,-13.202, 32.246,-6.287, 7.213, 36.164, 0.021,-7.506, 9.077, 37.542,-3.111, 24.595} | 0.2 | 0.3602679986544831 | 
| 300 | 504.03239465866534 | 3200446.291123798 | 1.0E7 | 504.03239465866534 | 504.03239465866534 | 504.03239465866534 | 98.3156254236028 | 192.84615903552498 | 2.8916360418706666 | 5.6719458539860295 | { 3.445, 35.713,-18.476,-1.414, 11.361,-2.077, 8.374, 9.445,-22.035,-4.214, 1.359, 5.106,-12.327, 21.726,-3.861, 23.571,-9.162,-5.720,-9.375, 20.454, 8.213, 17.224,-36.287,-28.184, 41.543,-9.852, 10.639,-18.100, 11.120,-12.224, 22.101} | 0.2 | 0.36658257117645127 | 
| 350 | 513.1560839026324 | 3400421.349616846 | 1.0E7 | 504.03239465866534 | 513.1560839026324 | 504.03239465866534 | 99.72684163146018 | 170.80779601317812 | 2.9331424009252998 | 5.023758706269944 | { 10.375, 10.516,-80.577,-38.454, 11.776, 10.447, 4.426,-18.801, 2.727,-16.403,-27.096, 0.630,-13.525, 11.372,-2.856, 29.286, 8.660,-7.044,-5.425, 7.594,-25.877, 4.431,-12.829,-7.254, 52.928, 17.275, 24.274, 10.123, 11.916,-19.199, 6.832} | 0.2 | 0.42169625462617233 | 
| 400 | 509.30950446658244 | 3400455.2397106136 | 1.0E7 | 504.03239465866534 | 509.30950446658244 | 504.03239465866534 | 112.01252446652836 | 243.82528565210833 | 3.294486013721428 | 7.171331930944364 | { 5.576, 17.471,-10.311,-17.823, 6.095, 1.002, 6.234,-1.241,-12.212,-11.760,-8.950, 3.671,-12.601, 18.227,-4.987, 24.236, 10.138,-14.504, 4.362, 14.766,-16.271, 13.075,-12.640,-22.593, 43.524, 6.681, 6.873, 18.921, 9.876,-4.080,-9.139} | 0.2 | 0.38380948353683225 | 
| 450 | 496.33850505487845 | 1600546.6491837727 | 1.0E7 | 496.33850505487845 | 496.33850505487845 | 496.33850505487845 | 97.07187107210342 | 183.02057355417216 | 2.855055031532457 | 5.382958045710947 | { 3.335, 42.909, 9.275, 2.794, 10.093,-1.454, 8.877, 0.431,-22.928,-4.088,-4.410, 6.731,-10.261, 29.210,-2.406, 19.508,-10.069,-6.060,-14.030, 18.766,-12.427, 13.419,-26.650,-35.775, 38.995,-9.167, 28.769,-15.453, 8.530,-12.946, 22.533} | 0.2 | 0.4162424170993886 | 
| 500 | 503.67915787697126 | 3000475.745526727 | 1.0E7 | 496.33850505487845 | 503.67915787697126 | 496.33850505487845 | 114.62447105117151 | 234.97546227656633 | 3.3713079720932737 | 6.911043008134304 | { 3.696, 51.099,-27.336, 10.069, 6.502,-3.505, 7.852,-0.607,-24.522,-1.648, 12.199, 7.611,-10.817, 19.198,-1.969, 15.402,-3.571,-5.572,-20.224, 17.024,-10.866, 9.390,-19.767,-32.115, 42.838,-6.264, 24.217,-6.398, 4.551,-14.325, 25.120} | 0.2 | 0.4678855197289042 | 
| 550 | 503.07090446148163 | 2400471.685767266 | 1.0E7 | 496.33850505487845 | 503.07090446148163 | 496.33850505487845 | 110.57065960297264 | 200.91843383309734 | 3.2520782236168424 | 5.909365700973451 | { 2.828, 24.648,-16.987,-4.077, 9.502, 1.391, 4.643,-12.690,-28.100,-8.281,-15.365, 4.356,-8.841, 26.099,-4.904, 22.248,-4.105,-18.164,-11.232, 16.021,-39.346, 3.381,-21.779,-30.857, 45.251,-8.516, 65.171, 1.309, 11.907,-11.545, 22.462} | 0.2 | 0.44535697995314394 | 
| 600 | 497.50990810510547 | 2600459.9577775197 | 1.0E7 | 496.33850505487845 | 497.50990810510547 | 496.33850505487845 | 111.74825771978203 | 261.2471375512692 | 3.2867134623465284 | 7.6837393397432106 | { 3.291, 10.241,-1.409,-1.914, 11.409,-4.213, 11.682, 2.085,-15.649,-2.758,-4.269, 6.402,-8.656, 22.899,-0.400, 19.659,-8.864,-3.900,-21.128, 20.365,-8.081, 21.121,-31.642,-27.077, 54.208,-1.226, 38.036,-19.382, 3.758,-15.525, 18.754} | 0.2 | 0.4733336799772481 | 
| 650 | 484.9701925163449 | 3200445.9948401595 | 1.0E7 | 484.9701925163449 | 484.9701925163449 | 484.9701925163449 | 115.23815815367686 | 204.99351883979645 | 3.3893575927552044 | 6.029221142346954 | { 4.478, 40.507, 4.439,-8.814, 15.077, 3.457, 8.297, 4.930,-17.574,-5.452,-4.841, 9.323,-7.830, 27.316,-2.886, 29.428,-9.462,-3.777,-13.023, 22.736,-9.156, 10.811,-23.625,-22.140, 41.392,-10.363, 25.373,-2.375, 1.278,-14.148, 22.069} | 0.2 | 0.4608232279788646 | 
| 700 | 511.4776707131732 | 3600412.8104723804 | 1.0E7 | 484.9701925163449 | 511.4776707131732 | 484.9701925163449 | 111.76530342188303 | 201.85584980768678 | 3.2872148065259665 | 5.936936759049612 | { 0.672, 10.043,-23.631,-26.239, 5.965,-0.938, 10.932, 2.592, 10.167,-10.597,-10.600, 2.766,-21.373, 6.217,-4.165, 12.833,-3.406,-5.639,-17.924, 0.878,-14.164, 9.172,-33.771,-26.712, 44.291, 4.126, 36.673, 4.178, 7.912,-24.579, 10.520} | 0.2 | 0.468899104102262 | 
| 750 | 479.8908067673826 | 3600403.2708953433 | 1.0E7 | 479.8908067673826 | 479.8908067673826 | 479.8908067673826 | 110.78080711047764 | 216.6888532354081 | 3.2582590326611163 | 6.373201565747298 | { 4.355, 69.609, 2.025, 26.563, 8.611,-4.321, 13.926, 3.126,-35.012,-3.740, 1.110, 6.766,-9.360, 23.688,-4.097, 18.533,-8.077,-3.884,-19.445, 18.301,-10.769, 10.621,-28.410,-38.715, 43.722,-8.613, 36.374,-22.461, 12.668,-17.913, 27.765} | 0.2 | 0.4598828770660055 | 
| 800 | 501.5973338527876 | 2200499.7677093623 | 1.0E7 | 479.8908067673826 | 501.5973338527876 | 479.8908067673826 | 118.39645598024819 | 266.47001146353637 | 3.4822487053014104 | 7.837353278339306 | { 6.677, 53.985, 28.069, 42.463, 7.351,-1.614, 12.943,-2.108, 3.084,-7.922, 4.663, 4.250,-1.832, 17.014,-4.629, 12.131,-7.638,-4.140,-3.864, 38.860,-14.609, 10.055,-32.752,-29.616, 28.553,-11.235, 37.824,-27.357, 6.802,-16.375, 30.505} | 0.2 | 0.5285273728439774 | 
| 850 | 498.31763240143016 | 3400473.4993242724 | 1.0E7 | 479.8908067673826 | 498.31763240143016 | 479.8908067673826 | 122.4540919318572 | 245.10945783665895 | 3.6015909391722727 | 7.209101701078204 | { 5.796, 58.468,-30.923, 35.213, 9.259,-10.646, 13.603,-0.301,-57.569,-3.404, 16.668, 3.507,-0.043, 17.880,-5.859, 18.112,-7.106,-3.570,-26.202, 18.731,-8.360, 7.404,-18.100,-31.439, 49.725,-18.796, 28.854,-28.428, 14.442,-18.561, 29.206} | 0.2 | 0.4806156132148752 | 
| 900 | 480.0328356218175 | 2800469.8077217555 | 1.0E7 | 479.8908067673826 | 480.0328356218175 | 479.8908067673826 | 117.1719535589713 | 274.60067785206905 | 3.446233928205036 | 8.076490525060855 | { 6.922, 70.616, 4.450,-16.749, 14.141, 1.823, 11.215, 6.089,-17.434,-5.309,-0.456, 8.157,-1.022, 28.434,-6.058, 31.310,-3.121,-12.988,-26.914, 22.440,-5.943, 9.808,-18.362,-24.223, 44.182,-9.396, 18.408,-7.027, 4.101,-13.551, 22.200} | 0.2 | 0.4952233840667039 | 
| 950 | 493.81007367127455 | 2800451.927907987 | 1.0E7 | 479.8908067673826 | 493.81007367127455 | 479.8908067673826 | 114.80269081736853 | 246.50872333292364 | 3.3765497299226004 | 7.250256568615402 | { 4.297, 69.144,-8.525, 29.998, 6.700, 4.158, 13.535, 4.542,-12.947,-3.282,-20.209, 7.230,-11.200, 21.341,-5.919, 11.449, 1.255, 1.607,-20.312, 14.313,-1.663, 9.721,-25.982,-29.895, 38.936, 1.970, 38.501,-16.518, 10.466,-19.333, 9.743} | 0.2 | 0.4938856619538472 | 
| 1000 | 492.87163915061444 | 3400459.6232409067 | 1.0E7 | 479.8908067673826 | 492.87163915061444 | 479.8908067673826 | 120.0688621689984 | 255.44732999551874 | 3.531437122617596 | 7.51315676457408 | { 6.019, 69.011, 4.482,-15.089, 14.400, 1.741, 11.934, 6.534,-16.687,-4.867,-5.374, 7.716,-1.160, 29.114,-4.736, 30.761,-2.365,-2.973,-24.148, 22.505,-8.509, 11.266,-4.639,-22.269, 50.602,-9.343,-6.271,-7.215, 4.032,-13.398, 19.559} | 0.2 | 0.48202883547993325 | 
| 1050 | 500.4456197901835 | 2600509.431284726 | 1.0E7 | 479.8908067673826 | 500.4456197901835 | 479.8908067673826 | 128.22453618443 | 284.22659709714407 | 3.771309887777351 | 8.359605796974826 | { 9.711, 83.440, 9.284, 31.900, 8.961,-2.892, 14.956,-0.810,-28.412,-9.079, 4.959, 0.368,-9.780, 17.708,-4.418, 5.037,-7.419,-3.998,-23.983, 25.861,-14.330, 7.555,-36.484,-19.789, 40.404,-6.342, 38.171,-43.980, 9.441,-15.916, 35.919} | 0.2 | 0.4901449115219662 | 
| 1100 | 482.7025881769031 | 3000460.485593044 | 1.0E7 | 479.8908067673826 | 482.7025881769031 | 479.8908067673826 | 122.04217982876851 | 310.45513809193767 | 3.5894758773167204 | 9.131033473292282 | { 8.233, 61.509,-6.154, 33.958, 6.585,-4.542, 16.215, 21.089,-20.116,-0.565, 9.862, 6.214,-8.476, 18.620,-1.227, 18.136,-3.334,-3.041,-15.479, 19.433,-12.317, 11.157,-28.635,-32.476, 39.621,-10.787, 40.123,-18.659, 14.198,-28.207, 31.507} | 0.2 | 0.4905785724566455 | 
| 1150 | 478.9467011880996 | 3400436.5284687625 | 1.0E7 | 478.9467011880996 | 478.9467011880996 | 478.9467011880996 | 121.91822309124419 | 260.64599276394944 | 3.5858300909189436 | 7.666058610704396 | { 1.464, 80.188,-3.576, 25.992, 8.704,-3.628, 13.720, 1.625,-30.862,-4.832,-2.903, 3.196,-11.522, 28.059,-4.572, 20.594,-7.975,-3.720,-17.028, 21.377,-11.767, 12.809,-31.534,-30.277, 40.574,-11.244, 37.286,-46.674, 11.283,-15.377, 19.988} | 0.2 | 0.5088802480950287 | 
| 1200 | 493.96208161728873 | 3200435.867150316 | 1.0E7 | 478.9467011880996 | 493.96208161728873 | 478.9467011880996 | 130.4103013569254 | 321.6069897871217 | 3.8355970987330963 | 9.459029111385936 | { 8.609, 61.184,-10.343,-23.958, 13.756,-1.815, 10.978, 7.736,-16.134,-4.781, 1.869, 8.948,-1.664, 43.584,-6.036, 22.799,-5.504,-8.420,-42.112, 17.110,-5.266, 16.612,-25.453,-29.630, 41.754,-9.946, 62.866,-16.921, 2.945,-13.324, 22.032} | 0.2 | 0.5423825192593896 | 
| 1250 | 501.29438057510197 | 3600415.0320124542 | 1.0E7 | 478.9467011880996 | 501.29438057510197 | 478.9467011880996 | 142.47989239112738 | 441.11585268664834 | 4.190585070327279 | 12.973995667254364 | { 0.504, 64.331,-1.974, 22.924, 8.849,-2.989, 15.142,-0.329,-26.866,-4.329,-0.435, 6.070,-10.804, 22.949,-2.567, 20.744,-7.647,-5.040,-18.868, 20.230,-11.031, 12.353,-33.089,-30.106, 40.221,-11.860, 36.533,-31.104, 10.726,-14.751, 14.600} | 0.2 | 0.5275002868597506 | 
| 1300 | 496.77274177050106 | 2400480.138985845 | 1.0E7 | 478.9467011880996 | 496.77274177050106 | 478.9467011880996 | 129.4543470576649 | 280.1094544092474 | 3.8074807958136754 | 8.238513364977862 | { 2.987, 90.855, 5.071,-23.162, 12.974, 0.284, 14.771, 2.296,-23.403,-2.859,-5.886, 8.068,-7.049, 34.204,-4.101, 18.881, 9.222,-12.801,-15.024, 22.444,-15.394, 12.907,-22.307,-19.588, 38.898,-12.997, 31.181,-5.281, 8.467,-24.160, 27.538} | 0.2 | 0.5313125311290972 | 
| 1350 | 481.44177392886263 | 2400493.4542332445 | 1.0E7 | 478.9467011880996 | 481.44177392886263 | 478.9467011880996 | 125.19132147181425 | 265.9960679796947 | 3.682097690347481 | 7.823413764108669 | { 3.474, 23.808, 5.236,-20.681, 13.920, 2.099, 8.546, 4.905,-18.556,-5.214,-1.066, 8.784,-3.487, 30.848,-2.573, 28.970,-4.041,-14.808,-9.028, 18.195,-10.035, 10.410,-17.269,-24.488, 44.611,-10.795, 19.402,-4.806, 7.455,-14.011, 19.376} | 0.2 | 0.5003360792922746 | 
| 1400 | 482.41606527866827 | 3000432.856299626 | 1.0E7 | 478.9467011880996 | 482.41606527866827 | 478.9467011880996 | 128.12255620218718 | 305.2922034064833 | 3.7683104765349182 | 8.979182453131862 | { 0.301, 71.298,-5.881, 23.910, 8.571,-2.784, 12.775, 4.446,-39.253,-4.451,-4.226, 2.781,-11.058, 28.721,-2.571, 19.161,-8.175,-4.830,-18.150, 23.780,-11.575, 8.970,-30.206,-29.185, 41.858,-10.959, 36.953,-38.470, 11.736,-14.279, 24.119} | 0.2 | 0.5363290677711018 | 
| 1450 | 497.2025977429937 | 3000416.04566463 | 1.0E7 | 478.9467011880996 | 497.2025977429937 | 478.9467011880996 | 131.76151083840935 | 295.9965002172557 | 3.8753385540708636 | 8.70577941815458 | { 6.571, 65.281, 4.792,-16.218, 15.032, 0.970, 11.579, 6.098,-17.121,-2.697,-0.090, 8.347,-4.551, 29.195,-6.783, 30.664,-2.786,-13.431,-19.892, 22.271,-4.712, 10.086,-18.264,-24.249, 44.623,-9.720, 21.848,-6.402, 4.654,-12.248, 22.102} | 0.2 | 0.4880775160306471 | 
| 1500 | 478.06293132757526 | 2200513.443615673 | 1.0E7 | 478.06293132757526 | 478.06293132757526 | 478.06293132757526 | 141.0766782992923 | 316.1095848681913 | 4.149314067626246 | 9.297340731417393 | { 4.371, 81.338,-2.725, 26.276, 8.481,-4.141, 12.892, 2.189,-64.195,-3.407, 1.909, 12.636,-9.976, 18.995,-4.383, 19.184,-5.106,-4.495,-24.957, 18.751,-2.771, 10.690,-17.232,-31.010, 42.413,-18.121, 37.298,-25.134, 9.977,-18.422, 26.839} | 0.2 | 0.5469957476463422 | 
| 1550 | 483.36237584283094 | 2400474.3604914215 | 1.0E7 | 478.06293132757526 | 483.36237584283094 | 478.06293132757526 | 135.7827213959493 | 424.8950275258177 | 3.993609452822041 | 12.496912574288755 | { 5.727, 84.090, 24.138, 37.595, 11.914,-6.291, 16.335, 2.135,-48.248,-3.716,-1.156, 13.786,-7.969, 15.280,-3.671, 17.852,-5.842,-12.645,-14.697, 22.186,-9.790, 9.325,-11.571,-17.723, 39.787,-16.025, 34.595, 2.198, 7.655,-24.196, 34.341} | 0.2 | 0.5250383373394353 | 
| 1600 | 481.62203778465437 | 2800453.177014772 | 1.0E7 | 478.06293132757526 | 481.62203778465437 | 478.06293132757526 | 140.54526908653392 | 330.68980356111047 | 4.133684384898054 | 9.726170692973836 | { 5.157, 74.946,-4.288,-6.105, 11.114,-2.633, 15.027, 3.478,-29.963,-2.892, 7.305, 11.058,-3.782, 14.385,-4.732, 26.507,-5.961,-6.641,-24.142, 17.657,-0.126, 11.506,-20.921,-27.810, 41.304,-15.491, 42.106,-29.498, 15.004,-20.733, 26.477} | 0.2 | 0.557631096584342 | 
| 1650 | 468.1730884223334 | 2200477.495883815 | 1.0E7 | 468.1730884223334 | 468.1730884223334 | 468.1730884223334 | 144.31034266449933 | 471.92890669574473 | 4.244421843073509 | 13.88026196163955 | { 6.994, 76.843,-22.319,-17.232, 14.403, 0.731, 10.583, 6.096,-17.470,-5.284, 0.488, 10.707,-0.747, 29.027,-5.903, 28.635,-4.083,-11.431,-25.073, 18.296,-13.094, 15.070,-16.275,-21.634, 44.147,-8.777, 108.964,-21.186, 4.761,-13.714, 22.261} | 0.2 | 0.5356786610548915 | 
| 1700 | 482.4697420703627 | 3200435.902055344 | 1.0E7 | 468.1730884223334 | 482.4697420703627 | 468.1730884223334 | 163.15229894122132 | 440.8102066324227 | 4.798597027682977 | 12.965006077424198 | { 5.031, 58.405,-3.376, 66.404, 6.560, 1.948, 14.347, 2.895,-20.375,-3.488, 6.430, 13.907, 1.672, 43.033,-6.580, 18.979,-8.395,-2.490,-19.848, 6.424,-10.764, 2.552,-31.401,-32.188, 42.805,-13.301, 34.709,-43.388, 11.004,-22.688, 35.698} | 0.2 | 0.6525085802357011 | 
| 1750 | 481.63163682817304 | 2800481.698490843 | 1.0E7 | 468.1730884223334 | 481.63163682817304 | 468.1730884223334 | 154.16568628287425 | 314.21268653893276 | 4.534284890672766 | 9.241549604086257 | { 14.171, 48.572,-31.756,-26.145, 15.160, 1.676, 15.185, 11.937,-13.562,-0.913, 10.169, 18.671, 1.467, 16.486,-5.341, 20.052,-3.398,-11.968,-26.626, 16.695,-12.480, 14.949,-25.532,-30.356, 45.402,-8.637, 137.033,-26.311, 6.424,-21.461, 26.951} | 0.2 | 0.5264889598156455 | 
| 1800 | 479.441289615849 | 2200507.065123013 | 1.0E7 | 468.1730884223334 | 479.441289615849 | 468.1730884223334 | 149.90168028653494 | 435.5359411095771 | 4.40887294960396 | 12.809880620869915 | { 7.190, 66.678, 6.685,-17.387, 13.579, 0.171, 10.786, 6.059,-17.875,-1.022,-0.355, 9.026,-2.070, 29.022,-6.613, 27.945,-1.635,-13.702,-23.290, 23.406,-9.087, 7.062,-16.228,-22.470, 44.551,-9.282, 77.440,-13.556, 5.133,-12.951, 22.351} | 0.2 | 0.641402561942198 | 
| 1850 | 470.87375342310975 | 2400442.7864776324 | 1.0E7 | 468.1730884223334 | 470.87375342310975 | 468.1730884223334 | 144.10476321680255 | 561.4424590322378 | 4.238375388729479 | 16.51301350094817 | { 4.237, 77.787,-2.526, 26.864, 8.454,-1.327, 13.162, 1.856,-76.810,-3.633, 2.237, 12.315,-9.532, 19.689,-4.347, 19.262,-4.946,-4.611,-24.849, 18.751,-3.482, 11.707,-15.299,-31.441, 42.186,-16.153, 37.594,-25.952, 7.949,-18.426, 26.744} | 0.2 | 0.5637462265097792 | 
| 1900 | 470.83389029518094 | 2600435.196077114 | 1.0E7 | 468.1730884223334 | 470.83389029518094 | 468.1730884223334 | 148.44686585308293 | 433.71831152122684 | 4.366084289796545 | 12.756420927094906 | { 4.023, 71.995,-2.655, 28.583, 8.468, 0.442, 13.101, 2.657,-81.441,-3.933, 3.442, 11.736,-8.333, 22.015,-4.347, 19.182,-4.668,-4.619,-24.362, 18.754,-6.035, 8.506,-17.616,-31.906, 44.156,-12.444, 36.790,-25.904, 7.924,-18.443, 26.555} | 0.2 | 0.587291552590862 | 
| 1950 | 484.4456371722696 | 3200428.740626098 | 1.0E7 | 468.1730884223334 | 484.4456371722696 | 468.1730884223334 | 145.56839375139546 | 385.0348442562622 | 4.281423345629271 | 11.324554242831242 | { 5.824, 69.756, 3.997, 29.520, 8.790, 2.770, 13.680, 1.769,-25.611,-3.899,-0.192, 11.245,-7.164, 26.180,-3.242, 19.292,-2.880,-3.464,-19.724, 17.252,-6.752, 11.426,-44.719,-29.261, 44.248,-7.002, 35.553,-21.840, 14.130,-16.247, 26.089} | 0.2 | 0.5542752094845159 | 
| 2000 | 472.59074527677774 | 1200541.6408659345 | 1.0E7 | 468.1730884223334 | 472.59074527677774 | 468.1730884223334 | 149.27426966813383 | 521.8522077376095 | 4.390419696121569 | 15.348594345223809 | { 4.868, 17.672, 5.474,-18.622, 14.713, 1.493, 10.853, 5.304,-18.328,-5.044,-0.219, 8.820,-2.138, 29.000,-2.486, 31.821,-3.783,-12.925,-24.907, 21.913,-5.703, 7.448,-19.286,-22.616, 44.103,-11.000, 43.809,-5.089, 6.450,-12.790, 19.743} | 0.2 | 0.5356909282944476 | 
| 2050 | 485.0447760302003 | 1400496.8536185976 | 1.0E7 | 468.1730884223334 | 485.0447760302003 | 468.1730884223334 | 139.53579240368612 | 295.1613334468836 | 4.103993894226063 | 8.681215689614223 | { 2.559, 42.229,-3.095, 26.829, 6.937,-3.059, 13.292, 2.701,-81.986,-1.916, 4.967, 15.988,-10.063, 22.817,-6.070, 28.216,-6.856, 10.049,-29.275, 15.925, 1.256, 10.447, 12.921,-35.507, 43.333,-14.784, 34.898,-13.983, 7.988,-17.768, 30.377} | 0.2 | 0.5672706818232377 | 
| 2100 | 474.8715704416022 | 3000437.0130435056 | 1.0E7 | 468.1730884223334 | 474.8715704416022 | 468.1730884223334 | 162.23851180765448 | 574.1104599144212 | 4.7717209355192525 | 16.885601762188855 | { 11.041, 55.957,-28.677,-1.561, 13.457, 0.705, 11.169, 2.622,-33.922,-5.526, 0.562, 11.588,-6.555, 23.137,-6.077, 37.661,-3.880,-13.149,-25.095,-3.066, 17.069, 13.515,-22.615,-24.055, 30.768,-3.917, 77.839,-24.221, 6.337,-14.640, 21.643} | 0.2 | 0.5435444867297498 | 
| 2150 | 487.92422097145925 | 2200503.641304581 | 1.0E7 | 468.1730884223334 | 487.92422097145925 | 468.1730884223334 | 158.44880078621676 | 550.554804240475 | 4.66025884665344 | 16.192788360013974 | { 6.153, 28.036,-1.595,-31.290, 15.419, 1.354, 10.583,-3.847,-24.422,-4.797, 0.076, 15.731,-10.589, 24.953,-1.057, 27.136,-5.800,-17.526,-11.930, 10.652,-11.087, 3.849,-29.028,-23.795, 37.501,-7.793, 45.825,-45.020, 5.840,-20.113, 17.762} | 0.2 | 0.551738428586578 | 
| 2200 | 482.8680230131886 | 1400555.3897256327 | 1.0E7 | 468.1730884223334 | 482.8680230131886 | 468.1730884223334 | 139.91221179060796 | 323.9886207192068 | 4.115065052664938 | 9.529077079976668 | { 6.996, 76.405,-9.137,-17.368, 14.383, 0.087, 10.641, 6.093,-17.461,-0.528,-1.971, 8.680,-2.554, 28.802,-4.732, 28.145,-2.450,-13.532,-23.660, 21.305,-3.889, 4.954,-16.453,-22.623, 44.589,-8.728, 112.520,-21.294, 4.862,-13.367, 22.298} | 0.2 | 0.5518556925202158 | 
| 2250 | 476.863464006301 | 2600478.1298622084 | 1.0E7 | 468.1730884223334 | 476.863464006301 | 468.1730884223334 | 145.89625174175706 | 340.61959563797745 | 4.291066227698738 | 10.018223401116982 | { 3.655, 69.678,-1.477, 26.719, 8.451,-4.275, 14.279, 2.333,-49.204,-3.827, 5.365, 7.105,-9.347, 20.046,-3.931, 18.703,-10.626,-4.684,-22.727, 17.643,-6.024, 9.227, 19.177,-35.547, 44.459,-10.859, 37.705,-22.839, 8.482,-19.285, 25.197} | 0.2 | 0.5163432435541413 | 
| 2300 | 461.40572033838464 | 3000439.120159633 | 1.0E7 | 461.40572033838464 | 461.40572033838464 | 461.40572033838464 | 153.46243890567325 | 444.8430683459242 | 4.513601144284501 | 13.08361965723307 | { 6.457, 68.360,-60.670, 7.605, 13.644, 0.848, 11.280, 8.743,-23.377,-5.502, 0.535, 11.001,-8.570, 34.630,-6.105, 33.668,-4.217,-1.940,-25.279, 14.840, 2.893, 15.133,-19.391,-27.060, 40.063,-2.954, 44.308,-18.682, 5.627,-14.233, 23.352} | 0.2 | 0.5491776831652743 | 
| 2350 | 477.98469331748845 | 3400368.2104953704 | 1.0E7 | 461.40572033838464 | 477.98469331748845 | 461.40572033838464 | 151.91866075431014 | 450.9148039958734 | 4.468195904538535 | 13.262200117525685 | { 2.598, 71.663,-28.109, 53.755, 18.961,-0.021, 12.232, 10.343,-27.466,-6.239, 2.730, 8.900,-12.322, 32.859,-5.609, 26.590,-2.370,-9.717,-20.543, 18.700,-0.582, 15.204,-17.419,-30.666, 53.901,-7.678,-1.712,-32.855, 4.775,-14.684, 21.318} | 0.2 | 0.5979867127908435 | 
| 2400 | 481.0932136110822 | 2000473.7631363296 | 1.0E7 | 461.40572033838464 | 481.0932136110822 | 461.40572033838464 | 146.7239068534398 | 511.750894865333 | 4.315409025101163 | 15.051496907803909 | { 6.935, 74.915,-22.245,-4.836, 13.275, 0.389, 11.130, 10.779,-25.093,-1.578, 0.594, 8.848,-7.444, 32.960,-6.954, 33.007,-3.357,-5.899,-22.944, 15.705, 1.086, 10.842,-22.796,-26.860, 43.845,-5.642, 17.195,-9.326, 5.298,-14.649, 22.623} | 0.2 | 0.5938116200564709 | 
| 2450 | 470.05981614738215 | 1400599.0584648196 | 1.0E7 | 461.40572033838464 | 470.05981614738215 | 461.40572033838464 | 142.84606319818718 | 325.45523480856167 | 4.201354799946685 | 9.572212788487109 | { 7.148, 67.236,-51.290, 3.197, 13.693, 0.840, 11.288, 8.490,-23.955,-5.507, 0.540, 10.942,-7.807, 32.108,-6.128, 32.719,-4.188,-2.603,-25.216, 15.998, 0.739, 15.062,-19.184,-26.326, 40.722,-3.978, 47.347,-19.348, 5.990,-14.202, 23.289} | 0.2 | 0.5999621836026031 | 
| 2500 | 488.74261763465927 | 2200471.264511762 | 1.0E7 | 461.40572033838464 | 488.74261763465927 | 461.40572033838464 | 156.67793901576238 | 478.90831277424417 | 4.6081746769341825 | 14.085538611007179 | { 6.884, 76.489,-16.395,-24.167, 14.793, 0.841, 11.097, 5.794,-19.930,-5.380, 0.502, 11.371,-9.151, 25.612,-6.438, 30.613,-3.678,-20.089,-25.222, 19.290,-25.030, 15.563,-15.430,-25.111, 46.103,-3.856, 74.100,-20.815, 5.248,-14.141, 22.124} | 0.2 | 0.5584023708370026 | 
| 2550 | 434.76059649537973 | 1600484.814118493 | 1.0E7 | 434.76059649537973 | 434.76059649537973 | 434.76059649537973 | 134.664669520874 | 315.5372209175372 | 3.9607255741433494 | 9.280506497574624 | { 6.303, 31.043,-52.970, 9.185, 13.916, 1.537, 11.272, 3.304,-26.590,-4.809,-1.293, 8.482,-11.425, 28.727,-4.879, 29.253,-2.277,-7.442,-9.237, 12.688,-3.326, 14.285,-15.470,-26.264, 39.457,-5.870, 28.241,-39.562, 6.394,-16.373, 21.936} | 0.2 | 0.584465776399621 | 
| 2600 | 470.20827253771347 | 2000474.0485453987 | 1.0E7 | 434.76059649537973 | 470.20827253771347 | 434.76059649537973 | 149.57155553603246 | 336.37627385257474 | 4.399163398118607 | 9.893419819193372 | { 6.048, 54.273,-86.813, 54.671, 14.091, 0.815, 9.519, 9.774, 29.654,-5.174, 2.609, 11.268,-9.587, 38.609,-7.660, 41.875,-6.678,-2.386,-24.509, 15.880, 15.070, 14.203,-23.300,-26.342, 40.685,-7.581, 57.402, 16.287, 6.030,-15.290, 22.547} | 0.2 | 0.6214496408785195 | 
| 2650 | 446.70359802899236 | 2200479.8539256407 | 1.0E7 | 434.76059649537973 | 446.70359802899236 | 434.76059649537973 | 161.19970426470044 | 428.82465993635384 | 4.741167772491195 | 12.612489998128051 | { 5.905, 18.635,-61.396, 19.211, 13.611, 0.310, 12.304, 2.787,-24.274,-6.283, 0.904, 6.884,-16.495, 24.873,-4.730, 44.527,-2.434,-1.036,-10.455, 11.641,-2.020, 15.285,-14.995,-25.629, 39.554,-5.204, 29.135,-52.199, 11.797,-16.247, 18.102} | 0.2 | 0.6457245382778011 | 
| 2700 | 467.17286517541845 | 1600506.7510976864 | 1.0E7 | 434.76059649537973 | 467.17286517541845 | 434.76059649537973 | 154.5088611608961 | 408.3355811503555 | 4.544378269438119 | 12.009870033833984 | { 4.568, 59.795,-60.056,-6.543, 9.031, 5.652, 11.209, 2.582,-112.042,-5.755, 3.828, 11.524,-6.517, 5.247,-6.542, 37.526,-5.314, 1.571,-25.762, 12.017, 6.894, 3.372,-28.680,-30.290, 40.280,-4.561, 58.695,-32.083, 6.557,-8.785, 30.449} | 0.2 | 0.684827469790233 | 
| 2750 | 468.04136353006254 | 2000466.849894857 | 1.0E7 | 434.76059649537973 | 468.04136353006254 | 434.76059649537973 | 148.19428861782634 | 361.2950028035344 | 4.3586555475831235 | 10.626323611868658 | { 9.354, 59.030,-72.501, 38.188, 8.843, 0.944, 14.558,-1.222,-44.922,-4.868, 8.474, 10.882,-2.816, 29.410,-8.464, 33.384,-4.715,-3.493,-26.673, 14.644, 5.144, 7.624,-22.822,-28.781, 40.065,-12.248, 37.068, 13.614, 10.525,-14.953, 26.025} | 0.2 | 0.5986145071444939 | 
| 2800 | 477.34309751584215 | 1200517.82211358 | 1.0E7 | 434.76059649537973 | 477.34309751584215 | 434.76059649537973 | 144.11427037956403 | 398.24569888366324 | 4.238655011163652 | 11.713108790695975 | { 7.075, 44.041,-82.250, 7.453, 13.605, 1.376, 11.473, 6.430,-28.537,-2.173,-1.968, 7.138,-14.651, 28.698,-3.914, 28.889,-2.416, 1.872,-12.091, 21.073,-5.405, 15.511,-18.424,-25.295, 42.619,-8.627, 18.704,-24.728, 5.820,-15.195, 20.645} | 0.2 | 0.6391883529235458 | 
| 2850 | 471.1804512231682 | 1400507.6547725978 | 1.0E7 | 434.76059649537973 | 471.1804512231682 | 434.76059649537973 | 158.20159822842476 | 423.8485307965908 | 4.652988183188965 | 12.466133258723259 | { 8.524, 75.571,-50.530, 21.420, 9.746, 5.029, 14.721, 3.798,-62.450,-5.424, 2.407, 10.034,-10.598, 19.775,-5.166, 43.384,-4.271,-0.039,-24.582, 15.179,-10.279, 13.951,-17.604,-27.204, 43.423,-8.057, 56.674,-21.153, 5.579,-11.436, 24.257} | 0.2 | 0.5831715870681109 | 
| 2900 | 480.7846153854522 | 1400507.755571258 | 1.0E7 | 434.76059649537973 | 480.7846153854522 | 434.76059649537973 | 150.23218480189828 | 534.5935818284509 | 4.418593670644076 | 15.723340642013259 | { 5.651, 54.879,-63.282, 4.794, 13.515, 1.867, 13.185, 3.192,-36.175,-5.005,-1.858, 8.586,-18.098, 26.477,-2.992, 24.625,-12.465,-3.507,-9.714, 14.799,-6.107, 12.734,-17.660,-28.136, 31.533,-1.634, 27.350,-31.373, 6.393,-15.582, 18.386} | 0.2 | 0.6016877899688576 | 
| 2950 | 478.00174059145957 | 1400542.0750945657 | 1.0E7 | 434.76059649537973 | 478.00174059145957 | 434.76059649537973 | 148.33310302531433 | 622.6023622943671 | 4.3627383242739555 | 18.311834185128447 | { 6.288, 29.274,-56.613, 12.677, 13.923, 1.593, 11.315, 3.147,-27.097,-4.795,-1.488, 8.136,-11.734, 28.745,-4.834, 28.767,-1.879,-7.198,-9.551, 12.565,-3.168, 13.667,-14.359,-26.909, 39.183,-5.686,-0.412,-39.367, 6.737,-16.017, 21.795} | 0.2 | 0.5783423245455175 | 
| 3000 | 467.36061214636527 | 1600510.4151755578 | 1.0E7 | 434.76059649537973 | 467.36061214636527 | 434.76059649537973 | 143.5649643250554 | 429.79467184788876 | 4.2224989507369255 | 12.641019760232018 | { 7.181, 32.583,-56.320, 4.803, 13.767, 1.457, 11.214, 3.264,-27.993,-2.943,-1.545, 6.292,-13.686, 28.845,-4.818, 28.775,-2.094,-3.671,-7.489, 20.806, 4.930, 13.874,-14.731,-24.635, 41.358,-7.376, 15.835,-28.621, 5.862,-15.492, 20.691} | 0.2 | 0.6516766199863956 | 
| 3050 | 470.261582643559 | 1800450.925365679 | 1.0E7 | 434.76059649537973 | 470.261582643559 | 434.76059649537973 | 156.68162859380982 | 562.3482360392252 | 4.608283193935584 | 16.53965400115368 | { 8.888, 60.298,-65.240, 23.327, 6.738, 2.428, 15.842, 4.443,-49.235,-4.760, 8.377, 11.183,-7.139, 25.028,-6.064, 37.605,-4.696,-3.232,-24.782, 14.607,-5.094, 5.867,-25.852,-30.490, 40.794,-5.956, 43.961,-54.872, 8.200,-12.406, 24.244} | 0.2 | 0.5306570284914602 | 
| 3100 | 467.13761625038717 | 1000510.2041388544 | 1.0E7 | 434.76059649537973 | 467.13761625038717 | 434.76059649537973 | 160.84722487701038 | 612.4282590817046 | 4.73080073167678 | 18.012595855344262 | { 6.318, 11.694,-63.374, 6.492, 13.932,-0.413, 12.107, 0.155,-20.131,-4.821,-0.884, 2.836,-18.829, 24.361,-4.637, 25.101,-2.531,-0.141,-9.891, 12.597,-3.284, 14.845,-17.419,-26.186, 39.421,-5.862, 27.167,-46.896, 11.953,-16.535, 21.853} | 0.2 | 0.644573558911285 | 
| 3150 | 457.45154266051696 | 1600467.6223553852 | 1.0E7 | 434.76059649537973 | 457.45154266051696 | 434.76059649537973 | 154.35702059346661 | 748.8126071305671 | 4.539912370396077 | 22.023900209722562 | { 6.285, 28.970,-57.238, 13.276, 13.924, 1.604, 11.323, 3.119,-27.183,-4.793,-1.521, 8.076,-11.787, 28.748,-4.826, 28.684,-1.808,-7.155,-9.604, 12.543,-3.141, 13.560,-14.169,-27.019, 39.137,-5.653,-5.326,-39.334, 6.796,-15.956, 21.771} | 0.2 | 0.582511773933334 | 
| 3200 | 461.9298353811584 | 1000558.1919516196 | 1.0E7 | 434.76059649537973 | 461.9298353811584 | 434.76059649537973 | 145.92377844823613 | 502.53193392889966 | 4.291875836712824 | 14.780350997908812 | { 9.286, 62.941,-53.649,-2.201, 11.681, 1.468, 12.718, 0.918,-27.906,-5.545, 4.544, 11.409,-8.592, 25.272,-5.049, 38.645,-4.261,-1.938,-25.400, 15.042, 0.266, 10.728,-17.535,-29.603, 41.633,-5.078, 36.863,-20.715, 7.882,-13.375, 24.267} | 0.2 | 0.5391266466812227 | 
| 3250 | 461.39184858938813 | 1400486.5804898662 | 1.0E7 | 434.76059649537973 | 461.39184858938813 | 434.76059649537973 | 151.95441305946704 | 470.86382922307035 | 4.469247442925498 | 13.848936153619716 | { 4.354, 69.476,-88.936,-9.068, 17.685, 5.282, 11.461, 10.609,-25.912,-4.609, 2.938, 6.323,-9.244, 33.295,-5.895, 21.479,-1.735,-4.451,-23.274, 18.489, 1.808, 15.020,-17.470,-28.887, 45.501,-5.773, 11.637,-16.034, 5.474,-14.975, 20.318} | 0.2 | 0.6299155261128775 | 
| 3300 | 465.9408919785824 | 1400475.8626792768 | 1.0E7 | 434.76059649537973 | 465.9408919785824 | 434.76059649537973 | 154.91337083887842 | 616.4389219286422 | 4.556275612908186 | 18.130556527313008 | { 6.432, 22.394,-54.185, 5.967, 13.952,-0.704, 11.183, 7.320,-27.675,-4.948,-1.091, 7.307,-17.780, 25.273,-4.690, 80.981,-2.474,-0.811,-9.873, 12.196,-3.285, 14.885,-16.743,-26.234, 39.594,-5.584, 36.485,-40.228, 0.065,-16.437, 22.294} | 0.2 | 0.4975024318354786 | 
| 3350 | 469.37113465432355 | 1200479.1664231613 | 1.0E7 | 434.76059649537973 | 469.37113465432355 | 434.76059649537973 | 138.66351818273074 | 480.37016218254774 | 4.078338770080311 | 14.12853418183964 | { 6.832, 33.264,-47.070, 8.310, 14.061, 1.446, 11.209, 3.328,-27.346,-3.027,-1.279, 10.617,-11.986, 28.826,-4.590, 29.522,-2.292,-6.002,-7.360, 16.247,-2.038, 13.648,-14.122,-24.995, 41.325,-6.623, 32.082,-31.435, 5.461,-17.015, 21.146} | 0.2 | 0.606602522258161 | 
| 3400 | 475.52537119172257 | 800533.4527136948 | 1.0E7 | 434.76059649537973 | 475.52537119172257 | 434.76059649537973 | 154.88080723131142 | 930.1282767302121 | 4.555317859744457 | 27.35671402147683 | { 4.456, 71.102,-66.503,-1.398, 17.158, 3.817, 11.542, 9.546,-25.371,-4.776, 2.786, 7.730,-10.952, 33.530,-5.921, 28.184,-2.702,-7.005,-23.301, 17.541, 1.102, 15.031,-17.718,-28.103, 39.625,-6.880, 39.144,-66.676, 5.471,-14.963, 22.406} | 0.2 | 0.48232208061758863 | 
| 3450 | 463.21462724517403 | 1000483.9959270518 | 1.0E7 | 434.76059649537973 | 463.21462724517403 | 434.76059649537973 | 155.94998873446386 | 1024.6887461328583 | 4.586764374543069 | 30.137904298025244 | { 6.785, 31.517,-54.573, 9.176, 13.944, 1.586, 11.461, 6.167,-26.224,-4.876,-1.202, 8.447,-11.435, 28.342,-4.880, 29.308,-2.513,-1.282,-9.912, 13.243,-3.077, 15.942,-15.449,-25.860, 39.411,-5.794, 29.044,-52.157, 6.097,-16.491, 21.871} | 0.2 | 0.6357298830848914 | 
| 3500 | 443.15479651253463 | 1200483.5532731395 | 1.0E7 | 434.76059649537973 | 443.15479651253463 | 434.76059649537973 | 161.13798959810796 | 1023.4150833357501 | 4.7393526352384745 | 30.10044362752207 | { 6.364, 34.042,-56.009, 20.187, 16.862, 2.194, 10.899, 3.205,-27.015,-4.682,-0.595, 8.521,-13.322, 28.624,-6.312,-25.577,-0.241,-7.397,-8.099, 12.248,-13.766, 13.837,-15.399,-26.290, 36.623,-6.007, 30.713,-39.763, 6.398,-16.623, 19.426} | 0.2 | 0.47815162006583584 | 
| 3550 | 435.01223935088626 | 800546.6539661415 | 1.0E7 | 434.76059649537973 | 435.01223935088626 | 434.76059649537973 | 178.26532948625504 | 1106.0313821029015 | 5.243097926066323 | 32.5303347677324 | { 7.127, 26.841,-53.092, 8.939, 13.137, 1.934, 11.345, 3.065,-25.737,-4.813,-2.524, 7.218,-11.721, 27.014,-4.714, 28.654,-1.824,-6.524,-9.611, 14.940, 4.465, 14.025,-13.734,-26.362, 39.190,-5.660, 28.605,-37.962, 3.537,-15.222, 21.633} | 0.2 | 0.7360171420806202 | 
| 3600 | 452.4974972041628 | 800562.0880168336 | 1.0E7 | 434.76059649537973 | 452.4974972041628 | 434.76059649537973 | 156.03913099945697 | 1087.1231392208924 | 4.589386205866383 | 31.974209977085067 | { 6.196, 32.295,-53.179, 10.938, 13.717, 1.484, 11.249, 3.271,-27.659,-4.083,-1.395, 10.436,-11.232, 28.562,-5.216, 28.831,-2.308,-7.468,-8.169, 17.494,-7.329, 14.526,-15.414,-24.893, 40.423,-6.877, 41.502,-50.239, 6.207,-14.120, 21.956} | 0.2 | 0.4893390131744815 | 
| 3650 | 457.1162801818349 | 1400469.6084039817 | 1.0E7 | 434.76059649537973 | 457.1162801818349 | 434.76059649537973 | 146.26695924229236 | 494.7400546949824 | 4.3019693894791855 | 14.551178079264188 | { 6.364, 44.545,-54.675, 6.702, 13.837, 1.178, 11.278, 10.812,-22.509,-5.387, 0.948, 11.282,-9.109, 30.804,-6.094, 29.108,-2.856,-4.304,-3.870, 13.188, 5.561, 14.455,-17.503,-26.339, 39.553,-6.325, 23.427,-16.058, 6.500,-15.378, 21.186} | 0.2 | 0.6306698130926245 | 
| 3700 | 451.4597992605436 | 800491.4154168145 | 1.0E7 | 434.76059649537973 | 451.4597992605436 | 434.76059649537973 | 163.61688645886673 | 851.9318977746871 | 4.812261366437262 | 25.056820522784918 | { 6.366, 33.002,-53.418, 21.301, 17.571, 2.213, 11.523, 3.105,-26.940,-4.854,-0.235, 8.861,-12.563, 28.651,-4.984,-2.971,-4.298,-7.438,-8.206, 12.112,-24.248, 13.604,-15.547,-26.302, 38.280,-5.912, 31.569,-36.701, 6.423,-16.432, 20.808} | 0.2 | 0.5893857982447895 | 
| 3750 | 459.22715427299124 | 200552.48657180223 | 1.0E7 | 434.76059649537973 | 459.22715427299124 | 434.76059649537973 | 150.21965868888904 | 397.79795535724276 | 4.418225255555564 | 11.699939863448318 | { 5.776, 14.195,-62.381, 21.752, 13.212, 1.328, 12.528, 2.269,-24.237,-6.188, 6.861, 6.692,-2.023, 25.177,-4.746, 51.673, 2.580, 0.169,-11.212, 10.953,-0.077, 16.161,-14.976,-25.548, 38.739,-4.955, 27.713,-72.771, 7.886,-24.022, 20.699} | 0.2 | 0.5470067509791565 | 
| 3800 | 472.6821340533983 | 1200531.975831156 | 1.0E7 | 434.76059649537973 | 472.6821340533983 | 434.76059649537973 | 143.1772006469025 | 623.475127134215 | 4.211094136673601 | 18.337503739241612 | { 6.092, 25.344,-58.899, 14.183, 12.844, 1.584, 11.904, 3.362,-17.222,-5.383, 0.124, 8.099,-12.433, 27.067,-4.668, 25.475,-2.230,-4.047,-10.340, 11.904,-1.482, 15.247,-14.388,-25.845, 39.575,-5.492, 33.130,-56.134, 17.725,-16.263, 22.389} | 0.2 | 0.5837629001897813 | 
| 3850 | 455.00590095381875 | 600509.8091026901 | 1.0E7 | 434.76059649537973 | 455.00590095381875 | 434.76059649537973 | 139.51141964766813 | 429.1983289176951 | 4.103277048460836 | 12.62348026228515 | { 6.339, 35.930,-64.504, 9.256, 14.309, 3.440, 11.272, 3.255,-21.046,-5.656,-1.138, 12.997,-9.369, 7.152,-4.787, 29.205,-4.299,-6.073,-10.042, 7.979,-7.888, 12.842,-8.784,-30.261, 45.935,-12.973, 40.743,-47.959, 6.075,-11.046, 21.516} | 0.2 | 0.5443597010968719 | 
| 3900 | 459.7833788399 | 600505.927418161 | 1.0E7 | 434.76059649537973 | 459.7833788399 | 434.76059649537973 | 142.28716198486538 | 477.20138214821134 | 4.18491652896662 | 14.03533476906504 | { 5.704, 8.066,-58.248, 25.387, 13.391, 0.370, 12.169, 2.863,-24.182,-6.334, 5.686, 5.468,-14.840, 25.439,-4.766, 43.765,-4.772, 0.840,-10.613, 11.927,-1.519, 17.072,-15.083,-25.665, 38.194,-3.803, 36.216,-80.466, 8.669,-29.444, 17.405} | 0.2 | 0.5241072529763717 | 
| 3950 | 453.75052493897033 | 800518.4002662486 | 1.0E7 | 434.76059649537973 | 453.75052493897033 | 434.76059649537973 | 145.5258316084431 | 402.9686217891657 | 4.280171517895399 | 11.85201828791664 | { 5.160, 27.925,-55.054, 9.314, 20.038, 1.235, 11.210, 5.848,-27.985,-4.810,-1.310, 8.949,-18.300, 19.753,-5.020,-79.822,-2.753, 6.424,-9.607, 27.442, 0.435, 14.506,-22.270,-27.685, 38.894,-4.089,-152.632,-16.575, 6.321,-17.881, 25.871} | 0.2 | 0.5334571937040786 | 
| 4000 | 451.70243021834426 | 1200535.5149562953 | 1.0E7 | 434.76059649537973 | 451.70243021834426 | 434.76059649537973 | 144.61770930285027 | 499.4625148255817 | 4.253462038319132 | 14.690073965458287 | { 4.835, 42.460,-45.070, 11.469, 8.279, 3.997, 10.544, 4.116, 1.489,-5.289,-1.691, 9.900,-10.039,-2.112,-5.179, 29.646,-3.774,-3.558,-24.723, 17.005, 7.580, 5.011,-22.161,-30.218, 40.638,-4.730, 61.488,-28.149, 8.677,-12.135, 24.442} | 0.2 | 0.5217280759071942 | 
| 4050 | 445.8724027626274 | 800534.6563688515 | 1.0E7 | 434.76059649537973 | 445.8724027626274 | 434.76059649537973 | 134.66947694579662 | 368.5171772290985 | 3.960866968994027 | 10.83874050673819 | { 5.611, 32.840,-51.787, 9.128, 13.808, 1.396, 11.115, 5.608,-28.898,-4.436,-2.209, 8.408,-11.371, 28.540,-4.879, 28.730, 0.814,-6.483,-7.963, 12.763,-3.213, 16.978,-15.357,-26.838, 40.173,-5.850, 27.757,-28.594, 5.704,-16.286, 21.707} | 0.2 | 0.5718763095771956 | 
