package lander.controller;

import lander.LanderSpecs;
import lander.Lander;
import au.edu.ecu.is.fuzzy.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D.*;
import planet.Planet;

/**
   Piraveen Controller created to improve Fuzzy controller

   @author Piraveen Mahesan [MAPIC21]
   @version 26-05-2014 - Part B
*/
public class PiraveenController implements Controller
{
    private Planet planet;
    private LanderSpecs specs;

    private double maxHeight;
    private double maxSpeed;
    private double safeLandingSpeed;
    private double maxFuel;
    private double maxThrust;
    private double maxLocation;
    private double maxRotation;

    private SugenoRuleSet control;
    private FuzzyVariable fuzzyHeight;
    private FuzzyVariable fuzzySpeed;
    private FuzzyVariable fuzzyFuel;
    private FuzzyVariable fuzzyThrust;
    private FuzzyVariable fuzzyLocation;
    private FuzzyVariable fuzzyLeftThrust;
    private FuzzyVariable fuzzyRightThrust;
    private FuzzyVariable fuzzyRotation;

    /**
     * Create a fuzzy controller for a particular planet and lander
     *
     * @param planet - the planet
     * @param specs - the specs for the lander
     * @throws FuzzyException
     */
    public PiraveenController(Planet planet, LanderSpecs specs) throws FuzzyException
    {
        this.planet = planet;
        this.specs = specs;

        maxHeight = specs.getStartHeight();
        maxSpeed = Lander.terminalVelocity(planet, specs);
        safeLandingSpeed = specs.getSafeLandingSpeed();
        maxFuel = specs.getFuelCapacity();
        maxThrust = specs.getMaxThrust();
        maxLocation = 50;
        maxRotation = 50;

        control = new SugenoRuleSet();


       	/* heights scaled by maxHeight */
        fuzzyHeight = new FuzzyVariable("height", "m", 0.0, 1.0, 2);
        
        FuzzySet Height_High = new FuzzySet("high", 0.1, 0.15, 1, 1);
        fuzzyHeight.add(Height_High);
        FuzzySet Height_Low = new FuzzySet("low", 0.03, 0.06, 0.08, 0.12);
        fuzzyHeight.add(Height_Low);
        FuzzySet Height_Lower = new FuzzySet("lower", 0.0, 0.0, 0.02, 0.05);
        fuzzyHeight.add(Height_Lower);
        
        //fuzzyHeight.display();

         
        
        /* speed scaled by maxSpeed */
        fuzzySpeed = new FuzzyVariable("speed", "m/s", 0.0, 2.0, 2);
        
        FuzzySet Speed_Higher = new FuzzySet("higher", 1.6, 1.8, 1.9, 2.0);
        fuzzySpeed.add(Speed_Higher);
        FuzzySet Speed_High = new FuzzySet("high", 0.7, 1.0, 1.5, 1.7);
        fuzzySpeed.add(Speed_High);
        FuzzySet Speed_Medium = new FuzzySet("medium",0.15, 0.3, 0.5, 0.8);
        fuzzySpeed.add(Speed_Medium);
        FuzzySet Speed_Low = new FuzzySet("low", 0.03, 0.08, 0.1, 0.2);
        fuzzySpeed.add(Speed_Low);
        FuzzySet Speed_Lower = new FuzzySet("lower", 0.0, 0.0, 0.025, 0.05);
        fuzzySpeed.add(Speed_Lower);
        //fuzzySpeed.display();
        
        /* fuel scaled by maxFuel */
        fuzzyFuel = new FuzzyVariable("fuel", "kg", 0.0, 1.0, 2);
        FuzzySet fuelHigh = new FuzzySet("high", 0.7, 0.9, 1.0, 1.0);
        fuzzyFuel.add(fuelHigh);
        FuzzySet fuelMedium = new FuzzySet("medium", 0.2, 0.4, 0.6, 0.8);
        fuzzyFuel.add(fuelMedium);
        FuzzySet fuelLow = new FuzzySet("low", 0.0, 0.0, 0.2, 0.3);
        fuzzyFuel.add(fuelLow);

           /* thrust scaled by maxThrust */
        double Thrust_Highest = 1.0;
        double Thrust_Higher = 0.8;
        double Thrust_High = 0.6;
        double Thrust_Medium = 0.4;
        double Thrust_Low = 0.34;
        double Thrust_Lower = 0.01;
        double Thrust_Lowest = 0;
        
        
        fuzzyLocation = new FuzzyVariable("Location", "m", -1.0, 1.0, 2);

        FuzzySet Location_Much_More_Left = new FuzzySet("Location_Much_More_Left", -1.0, -1.0, -0.55, -0.45);
        fuzzyLocation.add(Location_Much_More_Left);
        FuzzySet Location_More_Left = new FuzzySet("Location_More_Left", -0.50, -0.40, -0.25, -0.15);
        fuzzyLocation.add(Location_More_Left);
        FuzzySet Location_Left = new FuzzySet("Location_Left", -0.25, -0.20, -0.1, -0.01);
        fuzzyLocation.add(Location_Left);
        FuzzySet Location_Center = new FuzzySet("Location_Center", -0.06, -0.02, 0.02, 0.06);
        fuzzyLocation.add(Location_Center);
        FuzzySet Location_Right = new FuzzySet("Location_Right", 0.01, 0.1, 0.20, 0.25);
        fuzzyLocation.add(Location_Right);
        FuzzySet Location_More_Right = new FuzzySet("Location_More_Right", 0.15, 0.25, 0.40, 0.50);
        fuzzyLocation.add(Location_More_Right);
        FuzzySet Location_Much_More_Right = new FuzzySet("Location_Much_More_Right", 0.45, 0.55, 1.0, 1.0);
        fuzzyLocation.add(Location_Much_More_Right);


        fuzzyRotation = new FuzzyVariable("Rotation", "m", -1.0, 1.0, 2);
        
        FuzzySet Rotation_Much_More_Right = new FuzzySet("Rotation_Much_More_Right", -1.0, -1.0, -0.65, -0.55);
        fuzzyRotation.add(Rotation_Much_More_Right);
        FuzzySet Rotation_More_Right = new FuzzySet("Rotation_More_Right",  -0.60, -0.50, -0.35, -0.25);
        fuzzyRotation.add(Rotation_More_Right);
        FuzzySet Rotation_Right = new FuzzySet("Rotation_Right", -0.3, -0.2, -0.1, -0.02);
        fuzzyRotation.add(Rotation_Right);
        FuzzySet Rotation_Center = new FuzzySet("Rotation_Center", -0.05, 0.0, 0.0, 0.05);
        fuzzyRotation.add(Rotation_Center);
        FuzzySet Rotation_Left = new FuzzySet("Rotation_Left", 0.02, 0.1, 0.2, 0.3);
        fuzzyRotation.add(Rotation_Left);
        FuzzySet Rotation_More_Left = new FuzzySet("Rotation_More_Left", 0.25, 0.35, 0.50, 0.60);
        fuzzyRotation.add(Rotation_More_Left);
        FuzzySet Rotation_Much_More_Left = new FuzzySet("Rotation_Much_More_Left", 0.55, 0.65, 1.0, 1.0);
        fuzzyRotation.add(Rotation_Much_More_Left);


        fuzzyThrust = new FuzzyVariable("thrust", "N/s", 0.0, 1.0, 2);

           FuzzySet[] heightSets = {Height_Lower, Height_Low, Height_High};
        FuzzySet[] speedSets = {Speed_Lower, Speed_Low, Speed_Medium, Speed_High, Speed_Higher};
        double[][] thrustMatrix =
            {
                {Thrust_Lowest,     Thrust_Low,        Thrust_High,      Thrust_Higher,    Thrust_Highest},
                {Thrust_Lowest,     Thrust_Lowest,     Thrust_Lower,     Thrust_Medium,    Thrust_Higher},
                {Thrust_Lowest,     Thrust_Lowest,     Thrust_Lowest,    Thrust_Lowest,    Thrust_Lowest}
                                    
            };
        control.addRuleMatrix(
            fuzzyHeight, heightSets,
            fuzzySpeed, speedSets,
            fuzzyThrust, thrustMatrix
            );
        fuzzyLeftThrust = new FuzzyVariable("Left_Thrust", "m", 0.0, 1, 2);

        fuzzyRightThrust = new FuzzyVariable("Right_Thrust", "m", 0.0, 1, 2);
        
      
        double Thrust_01 = 0.93;
        double Thrust_02 = 0.80;
        double Thrust_03 = 0.60;
        double Thrust_04 = 0.55;
        double Thrust_05 = 0.52;    
        double Thrust_06 = 0.51; 
        double Thrust_07 = 0.50;    
        double Thrust_08 = 0.49;
        double Thrust_09 = 0.48;
        double Thrust_10 = 0.45;
        double Thrust_11 = 0.40;
        double Thrust_12 = 0.30;
        double Thrust_13 = 0.54;
           
        FuzzySet[] locationSets = {Location_Much_More_Left, Location_More_Left, Location_Left, Location_Center, Location_Right, Location_More_Right, Location_Much_More_Right};
        FuzzySet[] rotationSets = {Rotation_Much_More_Left, Rotation_More_Left, Rotation_Left, Rotation_Center, Rotation_Right, Rotation_More_Right, Rotation_Much_More_Right};
        
        double[][] leftThrustMatrix =
            {
                {Thrust_03,  Thrust_03,   Thrust_04,   Thrust_05,   Thrust_06,   Thrust_07,   Thrust_07},
                {Thrust_03,  Thrust_03,   Thrust_04,   Thrust_05,   Thrust_05,   Thrust_07,   Thrust_07},
                {Thrust_04,  Thrust_04,   Thrust_03,   Thrust_06,   Thrust_07,   Thrust_09,   Thrust_09},
                {Thrust_05,  Thrust_05,   Thrust_08,   Thrust_07,   Thrust_08,   Thrust_09,   Thrust_09},
                {Thrust_06,  Thrust_06,   Thrust_07,   Thrust_08,   Thrust_03,   Thrust_10,   Thrust_10},
                {Thrust_10,  Thrust_10,   Thrust_07,   Thrust_08,   Thrust_10,   Thrust_11,   Thrust_11},
                {Thrust_10,  Thrust_10,   Thrust_08,   Thrust_09,   Thrust_10,   Thrust_11,   Thrust_11}
            };
        
        control.addRuleMatrix(
            fuzzyLocation, locationSets,
            fuzzyRotation, rotationSets,
            fuzzyLeftThrust, leftThrustMatrix
            );

        double[][] rightThrustMatrix =
            {
                {Thrust_11,   Thrust_11,   Thrust_10,  Thrust_09,    Thrust_08,   Thrust_07,  Thrust_07},
                {Thrust_11,   Thrust_11,   Thrust_10,  Thrust_08,    Thrust_13,   Thrust_07,  Thrust_07},
                {Thrust_10,   Thrust_10,   Thrust_12,  Thrust_08,    Thrust_01,   Thrust_06,  Thrust_07},
                {Thrust_09,   Thrust_09,   Thrust_08,  Thrust_07,    Thrust_06,   Thrust_05,  Thrust_07},
                {Thrust_08,   Thrust_08,   Thrust_02,  Thrust_06,    Thrust_12,   Thrust_04,  Thrust_07},
                {Thrust_04,   Thrust_04,   Thrust_05,  Thrust_06,    Thrust_04,   Thrust_03,  Thrust_07},
                {Thrust_04,   Thrust_04,   Thrust_06,  Thrust_05,    Thrust_04,   Thrust_03,  Thrust_07}

            };
        
        control.addRuleMatrix(
            fuzzyLocation, locationSets,
            fuzzyRotation, rotationSets,
            fuzzyRightThrust, rightThrustMatrix
            );

//      
//        (new FuzzyRuleMatrixPanel(control, fuzzyLocation, locationSets,
//            fuzzyRotation, rotationSets, fuzzyLeftThrust)).display();
//
//        (new FuzzyRuleMatrixPanel(control, fuzzyLocation, locationSets,
//            fuzzyRotation, rotationSets, fuzzyRightThrust)).display();

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
            fuzzyLocation.setValue(Math.max(-1.0, Math.min(1.0, location/maxLocation)));
            fuzzyRotation.setValue(Math.max(-1.0, Math.min(1.0, rotation/maxRotation)));

            control.update();

            double thrust = fuzzyThrust.getValue()*maxThrust;
            double leftThrust = fuzzyLeftThrust.getValue();
            double rightThrust = fuzzyRightThrust.getValue();

            return new Point2D.Double(leftThrust*thrust, rightThrust*thrust);
        }
        catch(FuzzyException e)
        {
            throw new ControllerException(e.getMessage());
        }
    }
}
