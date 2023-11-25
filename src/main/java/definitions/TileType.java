package definitions;
import java.awt.image.BufferedImage;

/* This class contains the properties of a single tile type.
 * An array of TileTypes will be declared in TileManager.java containing all the available tile types. */

public class TileType {
    public BufferedImage image;
    public boolean collision = false;
    public boolean isFood = false;
    public boolean isHome = false;
}
