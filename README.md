[![Java CI with Gradle](https://github.com/poseidon-fisheries/POSEIDON/actions/workflows/gradle.yml/badge.svg)](https://github.com/poseidon-fisheries/POSEIDON/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/poseidon-fisheries/POSEIDON/branch/main/graph/badge.svg)](https://codecov.io/gh/poseidon-fisheries/POSEIDON)
[![Javadoc](https://github.com/poseidon-fisheries/POSEIDON/actions/workflows/javadoc.yml/badge.svg)](https://poseidon-fisheries.github.io/POSEIDON/)

<h1><img src="docs/images/Poseidon-horizontal-RGB.png" alt="POSEIDON" width="480"></h1>

POSEIDON is an agent-based model of fisheries, built on the
[MASON](https://cs.gmu.edu/~eclab/projects/mason/) discrete-event simulation toolkit. It is
developed at the [School of Geography and the Environment](https://www.geog.ox.ac.uk/) of the
University of Oxford, and maintained by Nicolas Payette of the Oxford Research Software Engineering
group ([OxRSE](https://www.rse.ox.ac.uk/)). Vessels are the agents: they decide where, when and how to fish, learn
from their own experience and from other vessels, and land catches that are sold on markets. They
fish a spatial grid of species biomass that grows and moves, under regulations such as quotas,
closed areas and closed seasons.

A simulation is described by a **scenario**: a declarative, serializable bag of components (the
grid and bathymetry, species, ports, fleets, vessel behaviour, markets, regulations). A scenario is
composed in Java from component factories, can be written to a YAML file, and can be loaded back
from it. Because a scenario is data, the same model can be run, inspected, calibrated and served to
other tools without changing any code.

## Status

This is the `main` branch, a rewrite of the original model that is still under heavy development as
part of the [SURIMI](https://www.surimi-project.eu/) project, funded by the EU's Horizon Europe
programme (grant 101157456). Interfaces change often.

The original model, with the code behind its published tutorial, is on the
[`legacy`](https://github.com/poseidon-fisheries/POSEIDON/tree/legacy) branch. To see POSEIDON in
action or follow the old tutorial, your best bet is the
[`poseidon-gui`](https://github.com/poseidon-fisheries/poseidon-gui) repository.

## Getting started

You need a Java 25 JDK. Gradle is provided by the wrapper, and Node.js, used to format the sources,
is downloaded by Gradle.

```
./gradlew build                 # compile, run the tests and the static checks
```

The `examples` module holds a complete scenario, the Peter Snapper fishery, with its input data.
Open it in a window:

```
./gradlew :examples:run \
  -PmainClass=uk.ac.ox.poseidon.examples.petersnapper.PeterSnapperScenarioWithUI
```

To run it without a window instead, for three simulated months:

```
./gradlew :examples:run \
  -PmainClass=uk.ac.ox.poseidon.examples.QuickRunner \
  --args="-s inputs/peter_snapper/scenario.yaml -p P3M"
```

`-p` takes an ISO-8601 period (`P3M` is three months, `P2Y` two years).

`PeterSnapperScenario` in the same module shows how a scenario is written, and
`./gradlew :examples:writePeterSnapperScenario` regenerates its YAML file.

## Modules

Gradle subprojects, with dependencies flowing one way:

| Module        | What it holds                                                                         |
| ------------- | ------------------------------------------------------------------------------------- |
| `core`        | `Simulation`, `Scenario`, the factory pattern and its scopes, the temporal schedule    |
| `io`          | Reading and writing scenarios as YAML, and tabular data                               |
| `geography`   | Coordinates, grids, bathymetry, ports                                                 |
| `biology`     | Species, biomass and the fish on the grid                                             |
| `regulations` | Fishing regulations: permitted and forbidden actions, in space and time               |
| `fishing`     | Vessels and what they do: trips, tasks, choices, catches, fuel, markets and money     |
| `calibration` | Calibration of model parameters with genetic algorithms                               |
| `gui`         | The Swing-based visualisation                                                         |
| `examples`    | Runnable example scenarios                                                            |

## Documentation

- The [Javadoc](https://poseidon-fisheries.github.io/POSEIDON/) documents every public type.
- [`docs/agents/architecture.md`](docs/agents/architecture.md) explains the factory and scenario
  pattern, which is the way into the code.
- [`AGENTS.md`](AGENTS.md) lists the build commands, the module graph and the coding conventions,
  with the topic documents in [`docs/agents/`](docs/agents/). It is written for coding agents but
  serves contributors equally.

## Citing POSEIDON

If you use POSEIDON in published work, please cite the overview paper:

> Bailey, R.M., Carrella, E., Axtell, R., Burgess, M.G., Cabral, R.B., Drexler, M., Dorsett, C.,
> Madsen, J.K., Merkl, A. & Saul, S. (2019). A computational approach to managing coupled
> human–environmental systems: the POSEIDON model of ocean fisheries. *Sustainability Science*
> 14(2): 259–275. <https://doi.org/10.1007/s11625-018-0579-9>

```bibtex
@article{bailey2019poseidon,
  author  = {Bailey, R. M. and Carrella, E. and Axtell, R. and Burgess, M. G. and Cabral, R. B. and
             Drexler, M. and Dorsett, C. and Madsen, J. K. and Merkl, A. and Saul, S.},
  title   = {A computational approach to managing coupled human--environmental systems: the
             {POSEIDON} model of ocean fisheries},
  journal = {Sustainability Science},
  year    = {2019},
  volume  = {14},
  number  = {2},
  pages   = {259--275},
  doi     = {10.1007/s11625-018-0579-9}
}
```

## Publications

Work on POSEIDON, in chronological order within each group. Most of it describes the original model
or applications built on it, so details of the code differ from the `main` branch.

### Model and methods

- Bailey, R.M., Carrella, E., Axtell, R., Burgess, M.G., Cabral, R.B., Drexler, M., Dorsett, C.,
  Madsen, J.K., Merkl, A. & Saul, S. (2019). A computational approach to managing coupled
  human–environmental systems: the POSEIDON model of ocean fisheries. *Sustainability Science*
  14(2): 259–275. <https://doi.org/10.1007/s11625-018-0579-9>
- Carrella, E., Bailey, R.M. & Madsen, J.K. (2019). Repeated discrete choices in geographical agent
  based models with an application to fisheries. *Environmental Modelling & Software* 111: 204–230.
  <https://doi.org/10.1016/j.envsoft.2018.08.023>
- Carrella, E., Bailey, R. & Madsen, J. (2020). Calibrating agent-based models with linear
  regressions. *Journal of Artificial Societies and Social Simulation* 23(1): 7.
  <https://doi.org/10.18564/jasss.4150>
- Burgess, M.G., Carrella, E., Drexler, M., Axtell, R.L., Bailey, R.M., Watson, J.R., Cabral, R.B.,
  Clemence, M., Costello, C., Dorsett, C., Gaines, S.D., Klein, E.S., Koralus, P., Leonard, G.,
  Levin, S.A., Little, L.R., Lynham, J., Madsen, J.K., Merkl, A., Owashi, B., Saul, S.E., van
  Putten, I.E. & Wilcox, S. (2020). Opportunities for agent-based modelling in human dimensions of
  fisheries. *Fish and Fisheries* 21(3): 570–587. <https://doi.org/10.1111/faf.12447>
- Carrella, E. (2021). No free lunch when estimating simulation parameters. *Journal of Artificial
  Societies and Social Simulation* 24(2): 7. <https://doi.org/10.18564/jasss.4572>
- Madsen, J.K., Bailey, R., Carrella, E. & Koralus, P. (2021). From reactive towards anticipatory
  fishing agents. *Journal of Simulation* 15(1–2): 23–37.
  <https://doi.org/10.1080/17477778.2020.1742588>
- Madsen, J.K., Powers, B., Bailey, R., Carrella, E., Payette, N. & Pilditch, T. (2024). Modelling
  adaptive and anticipatory human decision-making in complex human-environment systems. *Journal of
  Artificial Societies and Social Simulation* 27(1): 1. <https://doi.org/10.18564/jasss.5214>
- Arton, A., Carrella, E., Madsen, J.K. & Bailey, R.M. (2024). Triggering the tragedy: the simulated
  effects of alternative fisher goals on marine fisheries and fisheries policy. *Ecological
  Complexity* 57: 101070. <https://doi.org/10.1016/j.ecocom.2023.101070>

### Applications

US West Coast groundfish:

- Carrella, E., Saul, S., Marshall, K., Burgess, M.G., Cabral, R.B., Bailey, R.M., Dorsett, C.,
  Drexler, M., Madsen, J.K. & Merkl, A. (2020). Simple adaptive rules describe fishing behaviour
  better than perfect rationality in the US West Coast groundfish fishery. *Ecological Economics*
  169: 106449. <https://doi.org/10.1016/j.ecolecon.2019.106449>

Indonesian snapper fisheries:

- Madsen, J.K., Ekawaty, R., Ananthanarayanan, A., Bailey, R., Carrella, E., Dorsett, C., Drexler,
  M., Mous, P., Muawanah, U. & Saul, S. (2023). Understanding fisher behavior: the case of snapper
  fishers in Indonesia. *Marine Resource Economics* 38(1): 85–100.
  <https://doi.org/10.1086/722725>

Japan (Bungo Channel hairtail):

- Carrella, E., Powers, J., Saul, S., Bailey, R.M., Payette, N., Vert-pre, K.A., Ananthanarayanan,
  A., Drexler, M., Dorsett, C. & Madsen, J.K. (2024). Rejection sampling and agent-based models for
  data limited fisheries. *Frontiers in Marine Science* 11: 1243954.
  <https://doi.org/10.3389/fmars.2024.1243954>

Eastern Pacific tropical tuna purse seine fishery (IATTC):

- Payette, N., Carrella, E., Vert-Pre, K., Powers, B., Saul, S., Drexler, M., Ananthanarayanan, A. &
  Bailey, R. (2023). Attracted to fish: a gravity-based model of purse-seine vessel behaviour. In
  *Advances in Social Simulation*, Springer Proceedings in Complexity, pp. 87–103.
  <https://doi.org/10.1007/978-3-031-34920-1_8>
- Vert-Pre, K.A., Payette, N., Carrella, E., Powers, B., Saul, S., Drexler, M., Madsen, J.K.,
  Ananthanarayanan, A. & Bailey, R. (2023). POSEIDON, a tool for the exploration of alternative
  management scenarios for Eastern Pacific Ocean tropical tuna species. IATTC Scientific Advisory
  Committee, SAC-14 INF-G.
  <https://www.iattc.org/GetAttachment/72f55682-fee9-4d6d-8e4f-a51a89af1f6a/SAC-14-INF-G_Poseidon.pdf>
- Vert-Pre, K.A., Payette, N., Norelli, A., Powers, B., Drexler, M., Saul, S., Ananthanarayanan, A.,
  Bailey, R., Madsen, J.K. & Carrella, E. (2024). Evaluate the impact of alternative management
  scenarios for the Eastern Pacific Ocean tropical tuna species using POSEIDON. IATTC Ad Hoc
  Permanent Working Group on FADs, FAD-08 INF-A.
  <https://www.iattc.org/getattachment/34dc00b7-981d-4ada-844a-20f1a248b522/FAD-08-INF-A_POSEIDON-FAD-limit-scenario.pdf>
- Powers, B., Vert-pre, K.A., Lopez, J., Payette, N., Bailey, R., Drexler, M., Carrella, E. & Saul,
  S. (2025). Modeling fish aggregating device drift in a high current mixing area: Eastern Pacific
  Ocean. SSRN preprint 5335998. <https://doi.org/10.2139/ssrn.5335998>

Eastern Atlantic tropical tuna purse seine fishery (ICCAT):

- Powers, B., Vert-Pre, K.A., Norelli, A., Grande, M., Merino, G., Moreno, G., Die, D., Murua, H.,
  Erauskin-Extramiana, M. & Restrepo, V. (2025). Calibrating the preliminary POSEIDON-Atlantic model
  for purse seine tropical tuna fisheries. *Collective Volume of Scientific Papers ICCAT* 82(5):
  1–31 (SCRS/2025/152). <https://www.iccat.int/Documents/CVSP/CV082_2025/n_5/CV082050152.pdf>
- Vert-Pre, K.A., Powers, B., Norelli, A., Grande, M., Merino, G., Moreno, G., Die, D., Murua, H.,
  Erauskin-Extramiana, M. & Restrepo, V. (2025). Evaluating FAD management approaches for purse
  seine tropical tuna fisheries. *Collective Volume of Scientific Papers ICCAT* 82(5): 1–39
  (SCRS/2025/185). <https://www.iccat.int/Documents/CVSP/CV082_2025/n_5/CV082050185.pdf>

## License

POSEIDON is free software, released under the
[GNU General Public License, version 3 or later](LICENSE). Copyright © University of Oxford.
