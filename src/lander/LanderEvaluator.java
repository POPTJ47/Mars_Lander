package lander;


import au.edu.ecu.is.fuzzy.FuzzyException;
import lander.controller.Controller;
import lander.controller.PiraveenController;
import planet.Planet;

/**
   Application that tests a controller for a simulated Mars lander
*/
public class LanderEvaluator implements LanderObserver, Runnable
{   
    private Planet planet;
    private LanderSpecs specs;
    private Lander lander;

    private boolean debug = false;

    private double fitness;

    private boolean runCompleted = false;

    private int reps = 20;
    private static int REPS = 100; //20;

    /**
     * Run some tests to evaluate a controller
     * 
     * @param args -d to turn on debugging info, -r 5 to do 5 trials
     * @throws java.lang.FuzzyException
     */
    public static void main(String[] args) throws FuzzyException
    {   
        boolean debug = true;
        int reps = REPS;
        int i = 0;
        while(i < args.length)
        {   
            if(args[i].equalsIgnoreCase("-d"))
            {
                debug = true;
            }
            else if(args[i].equalsIgnoreCase("-r"))
            {   
                i++;
                reps = Integer.parseInt(args[i]);
            }
            i++;
        }

        Planet mars = Planet.getMars();
        LanderSpecs marsLanderSpecs = LanderSpecs.getMarsLanderSpecs();
        Controller controller = new PiraveenController(mars, marsLanderSpecs);
        LanderEvaluator eval = LanderEvaluator.getMarsLanderEvaluator(controller, debug, reps);

        eval.run();
        
        System.out.println("Average fitness = " + eval.getFitness());
    }

    /**
     * Create a lander evaluator
     *
     * @param planet - the planet to land on
     * @param lander - the lander doing the landing
     * @param debug - whether to show debug info
     * @param reps - number of trials to run
     */
    public LanderEvaluator(Planet planet, Lander lander, boolean debug, int reps)
    {   
        this.planet = planet;
        this.lander = lander;
        this.debug = debug;
        this.reps = reps;

        lander.addObserver(this);
        //lander.setDebug(debug);
    }

    public static LanderEvaluator getMarsLanderEvaluator(Controller controller, boolean _debug, int _reps)
    {   
        Planet mars = Planet.getMars();
        LanderSpecs marsLanderSpecs = LanderSpecs.getMarsLanderSpecs();
        Lander marsLander = new Lander(mars, controller, marsLanderSpecs, 0);
        LanderEvaluator eval = new LanderEvaluator(mars, marsLander, _debug, _reps);

        return eval;
    }

    /*-------------------------------------------------------------------------*/

    public void run()
    {   
        runCompleted = false;
        fitness = 0.0;
        try
        {   
            for(int rep = 0; rep < reps; rep++)
            {   
                lander.reset();
                lander.run();
            }
            runCompleted = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            lander.stop(); // [ChangSu] added
        }
        
        lander.stop();
    }

    public boolean runCompleted()
    {
        return runCompleted;
    }

    /*--------------------------------------------------------------------------*/

    public void update(boolean finished)
    {
        if(finished)
        {
            if(debug)
            {
                System.out.println("Fitness for one run = " + lander.getFitness());
            }
            fitness += lander.getFitness();
        }
    }

    /*--------------------------------------------------------------------------*/

    public double getFitness()
    {
        return fitness/reps;
    }
}
