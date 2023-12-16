package definitions;

/* Keep up to date with the secondary app */
public enum SimulationEventType {
    FOOD_CREATED,       // Food entity was created according to the pre-defined map.
    BIRTH,              // Ant has been born.
    FOLLOW_FOOD,        // Ant spotted food in its vision range and is heading straight for it.
    USE_HOME_PH,        // Ant is backtracking the HOME pheromones path from the other ants to find the same food source.
    MEAL,               // Ant arrived at the exact location of the food source and is now fed.
    FOOD_REDUCED,       // 1 quantity was reduced from the food source.
    FOOD_DEPLETED,      // Food quantity has reached 0.
    USE_FOOD_PH,        // Ant is backtracking the FOOD pheromones path from the other ants to find the nest.
    REPRODUCTION,       // A pair of ants has reproduced.
    DEATH               // Ant has died by starvation.
}