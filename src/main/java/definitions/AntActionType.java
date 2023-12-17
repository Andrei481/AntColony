package definitions;

/*
 * The Ant can perform 1 action at a time.
 * After birth, it starts looking for food.
 * After the meal, it looks for the nest.
 * Inside the nests it searches for a partner to reproduce.
 */

public enum AntActionType {
    SEARCH_FOOD,
    SEARCH_NEST,
    SEARCH_MATE
}
