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
