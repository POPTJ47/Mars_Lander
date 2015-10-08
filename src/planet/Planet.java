package planet;

import java.awt.*;
import java.awt.geom.*;

/**
   A class to describe a planet
   
   @author phi
   @version 2009/2 - Part B
*/
public class Planet
{
    private double gravity;     //  m/s2

    // how objects are slowed by friction with the atmosphere
    private double viscocity;   // pascal.seconds

    // how winds buffet a falling object
    private double turbulence;     // N
    private Point2D.Double impulse = new Point2D.Double(0.0, 0.0); //N

    private static Planet mars = null;

    /**
     * Create a planet!
     *
     * @param gravity
     * @param viscocity
     * @param turbulence
     */
    public Planet(double gravity, double viscocity, double turbulence)
    {
        this.gravity = gravity;
        this.viscocity = viscocity;
        this.turbulence = turbulence;
    }

    public void reset()
    {
        impulse.x = impulse.y = 0.0;
    }

    /**
     * Create Mars
     *
     * @return Mars
     */
    public static Planet getMars()
    {
        if(mars == null)
        {
            mars = new Planet(3.69, 0.14, 50.0);
            // viscocity = M x gravity / terminalvelocity x 6 x pi x radiusoflander
        }

        return mars;
    }

    /*--------------------------------------------------------------------------*/

    // Getters and setters

    public double getGravity()
    {
        return gravity;
    }

    public double getViscocity()
    {
        return viscocity;
    }

    public Point2D.Double getTurbulentImpulse()
    {
        double x = impulse.getX() + turbulence*2.0*(Math.random()-0.5);
        double y = impulse.getY() + 0.1*turbulence*2.0*(Math.random()-0.5);
        
        impulse.setLocation(x, y);
        
        return impulse;
    }

    /*--------------------------------------------------------------------------*/

    private static Ellipse2D.Double dot = new Ellipse2D.Double();
    private static Line2D.Double line = new Line2D.Double();

    public void draw(Graphics2D g, double x, double y)
    {
        final double GAP = 5;
        double ix = impulse.getX()/turbulence;
        double iy = impulse.getY()/turbulence;
        double length = Math.sqrt(ix*ix + iy*iy);

        /* draw a HUD indicator of turbulence */
        
        g.setColor(Color.green);
        
        final double DOT = 5.0;
        dot.setFrame(x-DOT/2, y-DOT/2, DOT, DOT);
        g.fill(dot);

        final double SCALE = 5.0;
        line.setLine(x, y, x+ix*SCALE, y+iy*SCALE);
        g.draw(line);
    }
}
