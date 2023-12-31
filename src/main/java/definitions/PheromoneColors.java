package definitions;

import java.awt.*;

/*
 *  Color [type] [level]
 *  type 0 = food (red )
 *  type 1 = home (blue)
 *  level 0 is unused
 */

public class PheromoneColors {
    private static final int alpha = 175; // opacity
    public static final Color[][] pheromoneColor = {
            {new Color(0, 0, 0, 0), new Color(255, 150, 150, alpha), new Color(255, 75, 75, alpha), new Color(255, 0, 0, alpha)},
            {new Color(0, 0, 0, 0), new Color(150, 150, 255, alpha), new Color(75, 75, 255, alpha), new Color(0, 0, 255, alpha)}
    };
}
