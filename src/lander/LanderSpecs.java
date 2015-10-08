package lander;


/**
   Specifications for a lander
   
   @author phi
   @version 2009/2
*/
public class LanderSpecs
{
    private double emptyMass;          // kg
    private double fuelCapacity;       // kg
    private double burnRate;           // kg/Ns
    private double maxThrust;          // N
    private double radius;             // m
    private double startHeight;        // m
    private double safeLandingSpeed;   // m/s

    /**
     * Create a set of specs
     *
     * @param emptyMass
     * @param fuelCapacity
     * @param burnRate
     * @param maxThrust
     * @param radius
     * @param startHeight
     * @param safeLandingSpeed
     */
    public LanderSpecs(double emptyMass, double fuelCapacity, double burnRate, double maxThrust, double radius, double startHeight, double safeLandingSpeed)
    {
        this.emptyMass = emptyMass;
        this.fuelCapacity = fuelCapacity;
        this.burnRate = burnRate;
        this.maxThrust = maxThrust;
        this.radius = radius;
        this.startHeight = startHeight;
        this.safeLandingSpeed = safeLandingSpeed;
    }

    /**
     * Create specs for a Mars lander
     *
     * @return the specs
     */
    public static LanderSpecs getMarsLanderSpecs()
    {
        return new LanderSpecs(
            570.0,       // emptyMass in kg
            190,         // fuelCapacity in kg
            1.0/2600.0,   // burnRate in kg/sN = 1/exhaust gas velocity
            400000.0,     // maxThrust in N    
            2.0,         // radius in m - a guess
            1500,        // start height in m
            10.0         // safe landing speed in m/s
        );
    }

    /*--------------------------------------------------------------------------*/

    /* Setters and getters */

    public double getEmptyMass()
    {
        return emptyMass;
    }

    public double getFuelCapacity()
    {
        return fuelCapacity;
    }

    public double getBurnRate()
    {
        return burnRate;
    }

    public double getMaxThrust()
    {
        return maxThrust;
    }

    public double getRadius()
    {
        return radius;
    }

    public double getStartHeight()
    {
        return startHeight;
    }

    public double getSafeLandingSpeed()
    {
        return safeLandingSpeed;
    }
}
