package definitions;

/*
 * If the food or the nest is in the vision radius, the ant goes directly to it.
 * Else If there are pheromones available, the ant will use them to find it.
 * Else the ant moves randomly (with a bias to the right).
 *
 * To find a mate the ant moves randomly within the nest.
 */
public enum AntMovementType {
    DIRECT,
    PHEROMONE,
    RANDOM_RIGHT,
    RANDOM_LEFT,
    RANDOM
}
