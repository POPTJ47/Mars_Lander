package lander.controller;

import lander.LanderSpecs;
import lander.Lander;
import au.edu.ecu.is.fuzzy.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D.*;
import planet.Planet;

/**
   An example fuzzy controller to get you started
   
   @author phi
   @version 2009/2 - Part B
*/
public class FuzzyController implements Controller
{
    private Planet planet;
    private LanderSpecs specs;

    private double maxHeight;
    private double maxSpeed;
    private double safeLandingSpeed;
    private double maxFuel;
    private double maxThrust;

    private SugenoRuleSet control;
    private FuzzyVariable fuzzyHeight;
    private FuzzyVariable fuzzySpeed;
    private FuzzyVariable fuzzyFuel;
    private FuzzyVariable fuzzyThrust;

    /**
     * Create a fuzzy controller for a particular planet and lander
     *  
     * @param planet - the planet
     * @param specs - the specs for the lander
     * @throws FuzzyException
     */
    public FuzzyController(Planet planet, LanderSpecs specs) throws FuzzyException
    {
        this.planet = planet;
        this.specs = specs;

        maxHeight = specs.getStartHeight();
        maxSpeed = Lander.terminalVelocity(planet, specs);
        safeLandingSpeed = specs.getSafeLandingSpeed();
        maxFuel = specs.getFuelCapacity();
        maxThrust = specs.getMaxThrust();

        control = new SugenoRuleSet();

        /* heights scaled by maxHeight */
        fuzzyHeight = new FuzzyVariable("height", "m", 0.0, 1.0, 2);
        FuzzySet heightHigh = new FuzzySet("high", 0.7, 0.9, 1.0, 1.0);
        fuzzyHeight.add(heightHigh);  
        FuzzySet heightMedium = new FuzzySet("medium", 0.04, 0.4, 0.6, 0.8);
        fuzzyHeight.add(heightMedium);  
        FuzzySet heightLow = new FuzzySet("low", 0.0, 0.0, 0.02, 0.1);
        fuzzyHeight.add(heightLow);  
       
        /* speed scaled by maxSpeed */
        fuzzySpeed = new FuzzyVariable("speed", "m/s", 0.0, 2.0, 2);
        FuzzySet speedHigh = new FuzzySet("high", 0.7, 0.9, 2.0, 2.0);
        fuzzySpeed.add(speedHigh);  
        FuzzySet speedMedium = new FuzzySet("medium",0.04, 0.3, 0.6, 0.8);
        fuzzySpeed.add(speedMedium);  
        FuzzySet speedLow = new FuzzySet("low", 0.0, 0.0, 0.025, 0.05);
        fuzzySpeed.add(speedLow);  

        /* fuel scaled by maxFuel */
        fuzzyFuel = new FuzzyVariable("fuel", "kg", 0.0, 1.0, 2);
        FuzzySet fuelHigh = new FuzzySet("high", 0.7, 0.9, 1.0, 1.0);
        fuzzyFuel.add(fuelHigh);  
        FuzzySet fuelMedium = new FuzzySet("medium", 0.2, 0.4, 0.6, 0.8);
        fuzzyFuel.add(fuelMedium);  
        FuzzySet fuelLow = new FuzzySet("low", 0.0, 0.0, 0.2, 0.3);
        fuzzyFuel.add(fuelLow);  

        /* thrust scaled by maxThrust */
        double thrustHigh = 1.0;
        double thrustMedium = 0.5;
        double thrustLow = 0.01;
        fuzzyThrust = new FuzzyVariable("thrust", "N/s", 0.0, 1.0, 2);
        
        FuzzySet[] heightSets = {heightLow, heightMedium, heightHigh};
        FuzzySet[] speedSets = {speedLow, speedMedium, speedHigh};
        double[][] thrustMatrix =
            {
                {0,         thrustMedium,   thrustHigh},
                {0,         thrustLow,      thrustMedium},
                {0,         0,              0}
            };
        control.addRuleMatrix(
            fuzzyHeight, heightSets,
            fuzzySpeed, speedSets,
            fuzzyThrust, thrustMatrix
            );


//        (new FuzzyRuleMatrixPanel(control, fuzzyHeight, heightSets,
//            fuzzySpeed, speedSets, fuzzyThrust)).display();
    }

    public Point2D.Double getThrust(double height, double speedY, double location, double speedX,
            double rotation, double rotationSpeed, double fuel) throws ControllerException
    {
        if(speedY < 0.0) return new Point2D.Double(0,0);

        try
        {
            control.clearVariables();

            fuzzyHeight.setValue(Math.max(0.0, Math.min(1.0, height/maxHeight)));
            fuzzySpeed.setValue(Math.max(-0.5, Math.min(2.0, speedY/maxSpeed)));
            fuzzyFuel.setValue(Math.max(0.0, Math.min(1.0, fuel/maxFuel)));
            
            control.update();

            double thrust = fuzzyThrust.getValue()*maxThrust;

            // adding a VERY!! rough steering control
            // you should replace this with something fuzzy
            // you might also want to completely rethink the design of the control system
            // rather than just doing a tack-on like this

            double balance = 0.5;

            if(location > 0.1)
            {
                balance = 0.49;
            }
            else if (location < -0.1)
            {
                balance = 0.51;
            }

            return new Point2D.Double(balance*thrust, (1-balance)*thrust);
        }
        catch(FuzzyException e)
        {
            throw new ControllerException(e.getMessage());
        }
    }
}
