package lander;

import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import lander.controller.Controller;
import lander.controller.ControllerException;
import planet.Planet;

/**
   Encapsulates a lander vehicle.
   
   @author phi, updated by ChangSu LEE
   @version 2009/2, updated Aug 2011 - Part B
*/
public class Lander implements Runnable
{
    private static final double TIMESTEP = 0.02;   // seconds
    private int delay;                             // milliseconds

    private Planet planet;
    private Controller controller;
    private LanderSpecs specs;

    private double emptyMass;   // kg
    private double burnRate;    // kg/Ns
    private double maxThrust;   // N
    private double radius;      // m

    private double height;      // m above surface
    private double speedY;       // m/s towards surface
    private double location;     // m left(-ve) or right(+ve)
    private double speedX;       // m/s towards right
    private double rotation;     // radians anti-clockwise
    private double rotationSpeed;   // radians/s

    private static final double ROTATION_RADIUS = 10.0;
    private static final double RR2 = ROTATION_RADIUS*ROTATION_RADIUS;

    private double fuel;        // kg
    private double thrustLeft;      // N
    private double thrustRight;      // N

    private double maxHeight;
    private double maxSpeed;
    private double maxFuel;

    private double safeLandingSpeed;

    private boolean running = false;
    private boolean paused = false;

    private Vector<LanderObserver> observers;

    private boolean debug = false;
    private static NumberFormat format = NumberFormat.getNumberInstance();
    static
    {
        format.setMaximumFractionDigits(2);
    }

    /**
     * Create a lander
     * 
     * @param planet - the planet we are landing on
     * @param controller - controls the thrust to land the lander safely
     * @param specs - specifications of this lander
     * @param height - initial height above ground
     * @param speed - initial speed of descent
     * @param fuel - initial amount of fuel
     * @param thrust - initial thrust
     * @param delay - time between controller updates in milliseconds
     */
    private Lander(Planet planet, Controller controller, LanderSpecs specs,
                double height, double speed, double fuel, double thrustLeft, double thrustRight,
                int delay)
    {
        this.planet = planet;
        this.controller = controller;
        this.specs = specs;

        emptyMass = specs.getEmptyMass();
        burnRate = specs.getBurnRate();
        maxThrust = specs.getMaxThrust();
        radius = specs.getRadius();

        maxHeight = height;
        maxFuel = specs.getFuelCapacity();
        maxSpeed = terminalVelocity(planet, specs);

        safeLandingSpeed = specs.getSafeLandingSpeed();

        this.height = height;
        this.speedY = speed;
        this.location = 0.0;
        this.rotation = 0.0;
        this.rotationSpeed = 0.0;

        this.speedX = 0.0;
        this.fuel = fuel;
        setThrust(thrustLeft, thrustRight);

        this.delay = delay;

        observers = new Vector<LanderObserver>();
    }

    /**
     * Create a lander with default initial settings
     *
     * @param planet - the planet we are landing on
     * @param controller - controls the thrust to land the lander safely
     * @param specs - specifications of this lander
     * @param delay - time between controller updates in milliseconds
     */
    public Lander(Planet planet, Controller controller, LanderSpecs specs, int delay)
    { 
        this(planet, controller, specs,
            specs.getStartHeight(),
            0.9*terminalVelocity(planet, specs),
            specs.getFuelCapacity(),
            0.0, 0.0,
            delay
            );
    }

    /**
     * Reset the lander to starting condition
     *
     */
    public void reset()
    {
        height = specs.getStartHeight();
        speedY = 0.9*terminalVelocity(planet, specs);
        location = 0.0;
        speedX = 0.0;
        rotation = 0.0;
        rotationSpeed = 0.0;

        fuel = specs.getFuelCapacity();
        setThrust(0.0, 0.0);

        // [ChangSu] Must reset the Plannet as well!
        planet.reset();
    }

    /**
     * Calculate the terminal velocity of a lander on this planet
     *
     * @param planet
     * @param specs
     * @return terminal velocity in m/s
     */
    static public double terminalVelocity(Planet planet, LanderSpecs specs)
    { 
        return 0.5*(specs.getEmptyMass()+specs.getFuelCapacity())*planet.getGravity()/(6.0*Math.PI*specs.getRadius()*planet.getViscocity());
    }

    /*-----------------------------------------------------------------------------------*/

    /**
     * Add an observer
     *
     * @param ob - the observer
     */
    public void addObserver(LanderObserver ob)
    {
        observers.add(ob);
    }

    /*-------------------------------------------------------------------------------------*/

    /**
     * The lander runs in its own thread, and can be started, paused, resumed, and stopped
     *
     */
    public void run()
    {
        running = true;

        while(running)
        {
            try
            {
                if(!paused) running = update(TIMESTEP);
                Thread.sleep(delay);
            }
            catch(InterruptedException ie)
            {
                ie.printStackTrace(System.out);
                running = false;
            }
            catch(ControllerException ce)
            {
                ce.printStackTrace(System.out);
                running = false;
            }
        }
    }

    public void stop()
    {
        running = false;
        paused = false;
    }

    public boolean isRunning()
    {
        return running;
    }
    
    public void pause()
    {
        paused = true;
    }
    
    public void resume()
    {
        paused = false;
    }
    
    public boolean isPaused()
    {
        return paused;
    }

    /*----------------------------------------------------------------------------------*/

    /*
     *  Getters and setters
     */

     public Planet getPlanet()
    {
        return planet;
    }

    public double getHeight()
    {
        return height;
    }

    public double getSpeedY()
    {
        return speedY;
    }

    public double getLocation()
    {
        return location;
    }

    public double getSpeedX()
    {
        return speedX;
    }

    public double getRotation()
    {
        return rotation;
    }

    public double getRotationSpeed()
    {
        return rotationSpeed;
    }

    public double getFuel()
    {
        return fuel;
    }

    public double getThrustLeft()
    {
        return thrustLeft;
    }

    public double getThrustRight()
    {
        return thrustRight;
    }

    public double getMaxHeight()
    {
        return maxHeight;
    }

    public double getMaxFuel()
    {
        return maxFuel;
    }

    public double getMaxSpeed()
    {
        return maxSpeed;
    }

    public double getMaxThrust()
    {
        return maxThrust;
    }

    public double getSafeLandingSpeed()
    {   return safeLandingSpeed;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public double getFitness()
    {
        double fitness = 0.0;

        double speed = Math.sqrt(speedX*speedX + speedY*speedY);

        // only makes sense when landed
        if(speed < 2.0*safeLandingSpeed)
        { 
            fitness = 80.0;

            // penalise landing in the wrong spot
            fitness -= 50.0*Math.abs(location)/100.0;

            boolean crashed = false;

            // penalise landing too fast
            // [ChangSu] scaling factor updated to 20.0 from 40.0
            if(speed > safeLandingSpeed) // [ChangSu] sign '>' remains
            { 
                fitness -= 20.0*(speed-safeLandingSpeed)/safeLandingSpeed;
                crashed = true;
            }
            
            // penalise landing wonky
            while(rotation > Math.PI) rotation -= 2*Math.PI;
            while(rotation < -Math.PI) rotation += 2*Math.PI;
            if(Math.abs(rotation) > 0.1)
            {
                fitness -= 40*(Math.abs(rotation)/Math.PI);
                crashed = true;
            }

            // any kind of crash is worse if there's fuel left
            // [ChangSu] scaling factor updated to 20.0 from 40.0
            // [ChangSu] updated to penalize the amount of fuel spent
            if(crashed)
            {
                fitness -= 20.0*(maxFuel-fuel)/maxFuel;
            }
            else
            {
                // [ChangSu] updated to '(fuel/maxFuel)' from '(maxFuel-fuel)/maxFuel'
                //            to reward the amount of fuel left after landing
                fitness += 20.0*(fuel/maxFuel);
            }
        }

        return Math.max(0.0, fitness);
    }

    public boolean setThrust(double newThrustLeft, double newThrustRight)
    {
        boolean success = false;

        if(fuel == 0.0 || height == 0.0)
        {
            success = newThrustLeft == 0.0 && newThrustRight == 0;
        }
        else if(newThrustLeft < 0 || newThrustLeft > maxThrust ||
                newThrustRight < 0 || newThrustRight > maxThrust)
        {
            success = false;
        }
        else
        {
            thrustLeft = newThrustLeft;
            thrustRight = newThrustRight;
            success = true;
        }

        return success;
    }

/*----------------------------------------------------------------------------------*/

    /**
     * updates lander position, speed, fuel
     * 
     * @param time - elapsed time in seconds
     * @return - whether still going
     * @throws ControllerException
     */
    private boolean update(double time) throws ControllerException
    {        
        // get new thrust setting from controller
        // and make adjustment
        try
        {
            Point2D.Double targetThrust = controller.getThrust(height, speedY, location, speedX,
                    Math.toDegrees(rotation), Math.toDegrees(rotationSpeed), fuel);
            setThrust(targetThrust.x, targetThrust.y);
        }
        catch(Exception exc)
        {
            exc.printStackTrace(System.out);
        }
        
        Point2D.Double ti = new Point2D.Double(0.0, 0.0);
        
        if(height > 0.0)    // still descending
        {
            // calculate amount of fuel used this time step
            // neglects the possibility that landing occurs during this period
            // if time step is small, shouldn't matter much
            double thrustTotal = thrustLeft+thrustRight;
            double fuelUsed = thrustTotal*time*burnRate;
            if(fuelUsed > fuel) // ran out of fuel
            {
                fuelUsed = fuel;
            }
            double effectiveThrustTotal = fuelUsed/(time*burnRate);
            double effectiveThrustLeft = 0.0;
            double effectiveThrustRight = 0.0;
            if(thrustTotal > 0.0)
            {
                effectiveThrustLeft = (effectiveThrustTotal/thrustTotal)*thrustLeft;
                effectiveThrustRight = (effectiveThrustTotal/thrustTotal)*thrustRight;
            }
    
            // compute new height
            height = height - speedY*time;
            if(height <= 0.0)
            {
                height = 0.0;  // landed
                thrustLeft = 0.0;
                thrustRight = 0.0;
            }

            // compute new location
            location = location + speedX*time;

            // compute new rotation
            rotation = rotation + rotationSpeed*time;

            // compute new speeds
            double mass = emptyMass + fuel; // neglect fuel burned in this time step - should be small
            // gravity
            speedY += time*planet.getGravity();

            // friction with atmosphere
            speedX -= time*6.0*Math.PI*radius*planet.getViscocity()*speedX/mass;
            speedY -= time*6.0*Math.PI*radius*planet.getViscocity()*speedY/mass;    // friction (Stoke's law)
            // neglect effect on rotation

            // turbulence
            ti = planet.getTurbulentImpulse();
            speedX += time*ti.x/mass;
            speedY += time*ti.y/mass;

            // spin faster anti-clockwise??
            if(effectiveThrustRight >= effectiveThrustLeft)
            {
                double rotationThrust = effectiveThrustRight - effectiveThrustLeft;
                rotationSpeed += time*rotationThrust/(mass * RR2);

                double thrust = 2*effectiveThrustLeft;

                speedY -= time*thrust*Math.cos(rotation)/mass;
                speedX -= time*thrust*Math.sin(rotation)/mass;
            }
            else
            {
                double rotationThrust = effectiveThrustLeft - effectiveThrustRight;
                rotationSpeed -= time*rotationThrust/(mass * RR2);

                double thrust = 2*effectiveThrustRight;

                speedY -= time*thrust*Math.cos(rotation)/mass;
                speedX -= time*thrust*Math.sin(rotation)/mass;
            }
                
            // compute fuel remaining
            fuel = fuel - fuelUsed;
            if(fuel == 0.0)
            {
                thrustLeft = 0.0;
                thrustRight = 0.0;
            }   // out of fuel - shut off rocket
        }

        if(debug)
        {
            System.out.println("height: " + format.format(height) + "\t" +
                             "vertical speed: " + format.format(speedY) + "\t" +
                             "location: " + format.format(location) + "\t" +
                             "horizontal speed: " + format.format(speedX) + "\t" +
                             "angle: " + format.format(Math.toDegrees(rotation)) + "\t" +
                             "rotational speed: " + format.format(Math.toDegrees(rotationSpeed)) + "\n" +
                             "fuel: " + format.format(fuel) + "\t" +
                             "left thrust: " + format.format(thrustLeft) + "\t" +
                             "right thrust: " + format.format(thrustRight) + "\t" +
                             "turbulence: " + format.format(ti.x) + " : " + format.format(ti.y));
        }

        for(LanderObserver ob: observers)
        {
            ob.update(height <= 0.0);
        }

        return height > 0.0;
    }    

    // some constants used to draw the lander - not realistic!!
    private static final double LEG_HEIGHT = 20.0;
    private static final double LEG_SPREAD = 40.0;
    private static final double LEG_THICKNESS = 6.0;
    private static final double LEG_LENGTH = Math.sqrt(LEG_HEIGHT*LEG_HEIGHT + LEG_SPREAD*LEG_SPREAD);
    private static final double FOOT_WIDTH = 36.0;
    private static final double FOOT_HEIGHT = 12.0;
    private static final double BODY_WIDTH = 80.0;
    private static final double BODY_HEIGHT = 40.0;

    public void draw(Graphics2D graphic, double x, double y)
    {
        Paint oldPaint = graphic.getPaint();

        AffineTransform oldTransform = graphic.getTransform();

        AffineTransform transform = (AffineTransform)oldTransform.clone();

        transform.translate(x, y);
        transform.rotate(-rotation);
        transform.translate(-x, -y);

        graphic.setTransform(transform);

        // body
        drawDome(graphic, x, y - LEG_HEIGHT, BODY_WIDTH, BODY_HEIGHT);

        // legs
        Stroke oldStroke = graphic.getStroke();
        Stroke thick = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        graphic.setStroke(thick);

        GradientPaint leftLegPaint = new GradientPaint(
                (float)(x - BODY_WIDTH/2 - LEG_SPREAD/2), (float)(y - LEG_HEIGHT/2), Color.white,
                (float)(x - BODY_WIDTH/2 - LEG_SPREAD/2 + LEG_THICKNESS*LEG_HEIGHT/LEG_LENGTH),
                (float)(y - LEG_HEIGHT/2 + LEG_THICKNESS*LEG_SPREAD/LEG_LENGTH), Color.blue, true);
        graphic.setPaint(leftLegPaint);

        Line2D.Double leftLeg = new Line2D.Double(
                x - BODY_WIDTH/2 - LEG_SPREAD,
                y,
                x - BODY_WIDTH/2,
                y - LEG_HEIGHT);
        graphic.draw(leftLeg);

        drawDome(graphic, x - BODY_WIDTH/2 - LEG_SPREAD, y, FOOT_WIDTH, FOOT_HEIGHT);

        GradientPaint rightLegPaint = new GradientPaint(
                (float)(x + BODY_WIDTH/2 + LEG_SPREAD/2), (float)(y - LEG_HEIGHT/2), Color.white,
                (float)(x + BODY_WIDTH/2 + LEG_SPREAD/2 - LEG_THICKNESS*LEG_HEIGHT/LEG_LENGTH),
                (float)(y - LEG_HEIGHT/2 + LEG_THICKNESS*LEG_SPREAD/LEG_LENGTH), Color.blue, true);
        graphic.setPaint(rightLegPaint);

        Line2D.Double rightLeg = new Line2D.Double(
                x + BODY_WIDTH/2 + LEG_SPREAD,
                y,
                x + BODY_WIDTH/2,
                y - LEG_HEIGHT);
        graphic.draw(rightLeg);

        drawDome(graphic, x + BODY_WIDTH/2 + LEG_SPREAD, y, FOOT_WIDTH, FOOT_HEIGHT);

        graphic.setStroke(oldStroke);
        graphic.setPaint(oldPaint);

        // draw flame from thrusters
        graphic.setColor(Color.yellow);

        if(thrustLeft > 0.0)
        {
            GeneralPath flame = new GeneralPath();
            flame.moveTo(x - BODY_WIDTH/2 - LEG_SPREAD - FOOT_WIDTH/2, y);
            flame.lineTo(x - BODY_WIDTH/2 - LEG_SPREAD + FOOT_WIDTH/2, y);
            flame.lineTo(x - BODY_WIDTH/2 - LEG_SPREAD, y+5+thrustLeft/maxThrust*25);
            flame.closePath();
            graphic.fill(flame);
        }

        if(thrustRight > 0.0)
        {
            GeneralPath flame = new GeneralPath();

            flame.moveTo(x + BODY_WIDTH/2 + LEG_SPREAD - FOOT_WIDTH/2, y);
            flame.lineTo(x + BODY_WIDTH/2 + LEG_SPREAD + FOOT_WIDTH/2, y);
            flame.lineTo(x + BODY_WIDTH/2 + LEG_SPREAD, y+5+thrustRight/maxThrust*25);
            flame.closePath();
            graphic.fill(flame);
        }

        graphic.setTransform(oldTransform);
    }

    private void drawDome(Graphics2D graphic, double x, double y, double width, double height)
    {
        Paint oldPaint = graphic.getPaint();

        GradientPaint bodyPaint = new GradientPaint(
                (float)x, (float)(y - height), Color.white,
                (float)x, (float)y, Color.blue, true);
        graphic.setPaint(bodyPaint);

        Arc2D.Double arc = new Arc2D.Double(
                x - width/2,
                y - height,
                width,
                2*height,
                0, 180, Arc2D.CHORD);

        graphic.fill(arc);

        graphic.setPaint(oldPaint);
    }
}
