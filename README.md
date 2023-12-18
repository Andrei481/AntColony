# AntColony

Simulating the world of ants

### Team members:

- Andrei JOLDEA &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; — &nbsp;&nbsp; andrei.joldea@student.upt.ro  
- Andrei LAZAROV &nbsp;&nbsp; — &nbsp;&nbsp; andrei.lazarov@student.upt.ro
- Antonio OLAH &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; — &nbsp;&nbsp; antonio.olah@student.upt.ro
- David POPESCU &nbsp;&nbsp; — &nbsp;&nbsp; popescudavidalexandruemanuel@gmail.com

<img src="https://news.harvard.edu/wp-content/uploads/2009/11/kronaueretalarmyants5.jpg" alt="Stock image" width="500"/>

### Project specification

Our project aims to develop a detailed and interactive simulation of an ant colony within a digital world. This simulation will consist of two primary elements: a thriving population of ants and a landscape populated with food sources. The ants will exhibit natural behaviors, including foraging for food and returning to their designated home area, while also leaving pheromone trails as they move.

The digital environment will be carefully designed, featuring a home base for the ants and scattered food sources throughout. The ants' primary objectives will be to locate food and transport it back to their home base. During their journey, ants will leave pheromone trails, which serve as a form of communication among the colony members.

In this simulation, the ants will possess the ability to sense various environmental cues. They will detect the presence of pheromones left by their fellow ants, identify the location of food sources, and recognize their home area. These sensory inputs will inform their decision-making process as they navigate the world. While their choices will predominantly be randomized for authenticity, the ants will strive to make advantageous decisions based on the information they gather.

To add depth to the simulation, we will model pheromone evaporation. This means that over time, the pheromone trails left by ants will gradually fade away. This dynamic feature ensures that only the most valuable paths are retained, as ants will prioritize following fresher, more potent pheromone trails.

Moreover, we will introduce various types of pheromones, each signifying different actions or intentions, such as searching for food, returning home with or without food, or signaling reproduction. The implementation of rules, similar to those found in the Game of Life, will govern ant behavior. For instance, we might limit the number of neighboring ants permitted to reproduce or create distinct groups of ants based on their assigned tasks. This organization will encourage orderly travel and minimize potential mixing among ants with different objectives.

By executing this project description, we aspire to craft an engaging and informative ant colony simulation that captures the essence of ant behavior and pheromone-driven decision-making within a dynamic digital world.

### Concurrency problems

- **Race Conditions**: Race conditions can occur when multiple ants concurrently access and modify shared resources, potentially leading to data corruption or incorrect outcomes if synchronization is not properly managed.

- **Deadlocks**: Deadlocks can happen when ants wait for each other to release resources, causing the simulation to stall due to improper resource access coordination.

- **Pheromone Updates**: Coordinating pheromone deposition and evaporation among multiple ants is complex, as they may attempt to update pheromone levels on the same paths. Ensuring data consistency in such updates is a challenge.

- **Resource Contention**: Ants compete for resources like food and space. Contention for these resources can lead to inefficiencies, and managing resource allocation fairly and efficiently is a concurrency challenge.

- **Task Coordination**: Ants need to communicate and coordinate their activities, such as following specific pheromone trails. Coordinating communication and task synchronization among ants can be complex in a multi-threaded environment.

- **Complex Decision-Making**: Ants are expected to make random but informed decisions based on sensory inputs. Coordinating these decision-making processes in a multi-threaded environment while preventing ants from following the same paths is a challenging aspect of the simulation.

### Architecture

**_Modules_**

**Entities Module**

_Ant Class_: This class represents individual ants. It should include attributes such as position, pheromone levels, and state. Ants should be capable of performing actions like foraging, depositing pheromones, and returning home.

_Pheromone Class_: This class represents the pheromone trails left by ants and handles their deposition and evaporation.

_Food Class_: Represents food sources in the simulation. It manages the availability of food and tracks consumption by ants.

_Nest Class_:

**Simulation Module**

_EvaporationThread Class_: This class takes care of updating pheromones.

_TileManager Class_: This class defines the environment where the simulation takes place. It includes the terrain, home base, food sources, and obstacles. It manages the spatial layout of the simulation.

_CollisionChecker_: This class is used by each ant to check whenever it encounters another entity and instructs the ant what actions to perform.

**Screens Module**

_SimulationScreen_: Displays the simulation on the screen

**Utils Module**

_Logger Class_: Keeps track of every event.

_StatisticsProvider Class_: Sends event messages to the secondary app.

_ThreadMonitor Class_: Keeps track of the active threads to ease debugging.

**_Threads_**

_Main Thread_

_Thread Monitor_

_GUI_Thread_

_Ant Threads_:
        Each ant in the simulation runs as an independent thread. Ant threads simulate the autonomous behavior of ants, including wandering, foraging, and interacting with the environment. These threads continuously sense their surroundings and make decisions based on sensory inputs.

_Evaporation Thread_:
        A separate thread responsible for managing pheromone evaporation. It periodically updates pheromone levels across the world to simulate the fading of pheromone trails.

_Statistics Thread_

**_Entry Points_**

_Main Entry Point_:
        The main entry point initializes the simulation environment and orchestrates the creation of ant threads, the evaporation thread, and the world objects. It kicks off the simulation and ensures proper termination when the simulation ends.

_Ant Behavior Entry Point_:
        This entry point defines the behavior of individual ants. It handles the logic for ants to sense their surroundings, make decisions, move, deposit pheromones, and interact with food sources and the home base.

_Evaporation Thread Entry Point_:
        The entry point for the evaporation thread, responsible for managing pheromone evaporation. It periodically updates pheromone levels in the world to simulate the decay of pheromone trails.
