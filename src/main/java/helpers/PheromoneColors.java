package helpers;

import java.awt.Color;

/*
 *  Color [type] [level]
 *  type 0 = food (red )
 *  type 1 = home (blue)
 *  level 0 is unused
 */

public class PheromoneColors {
    public static final Color[][] pheromoneColor = { { new Color(255, 255, 255, 0), new Color(255, 150, 150, 15), new Color(255,  75,  75, 15), new Color(255,   0,   0, 15) },
                                                     { new Color(255, 255, 255, 0), new Color(150, 150, 255, 15), new Color( 75,  75, 255, 15), new Color(  0,   0, 255, 15) } };
}
