package main;

import au.edu.ecu.is.fuzzy.FuzzyException;
import java.io.IOException;
import planet.Planet;
import lander.controller.PiraveenController;
import lander.controller.Controller;
import lander.LanderObserver;
import lander.LanderSpecs;
import lander.Lander;
import java.awt.image.BufferedImage;

import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.text.*;
import java.net.*;

/**
   A class that presents a GUI for running a simulation of a Mars landing
   
   @author phi
   @version 2009/2
*/
public class Simulation extends JPanel implements LanderObserver, ActionListener
{
    private Planet planet;  // the planet to land on
    private Lander lander;  // the lander that lands
    private LanderSpecs specs; // specifications of the lander

    /* delay between updates - controls apparent speed */
    private static final int DELAY = 50;

    // GUI stuff
    private JButton goButton;
    private JButton pauseButton;
    private AnimPanel animPanel;
    private SpeedYPanel speedYPanel;
    private SpeedYLabel speedYLabel;
    private SpeedXPanel speedXPanel;
    private SpeedXLabel speedXLabel;
    private FuelPanel fuelPanel;
    private FuelLabel fuelLabel;
    private ThrustPanel thrustPanel;
    private ThrustLabel thrustLabel;
    private JTextArea reportArea;
    private static NumberFormat format = NumberFormat.getNumberInstance();
    static
    {
        format.setMaximumFractionDigits(2);
    }
    
    public static void main(String[] args) throws FuzzyException, IOException
    {
        JFrame frame = new JFrame("Mars Lander");
        
        Planet mars = Planet.getMars();
        LanderSpecs marsLanderSpecs = LanderSpecs.getMarsLanderSpecs();
        Controller controller = new PiraveenController(mars, marsLanderSpecs);
        final Simulation sim = Simulation.getMarsLanderSimulation(controller);

        frame.getContentPane().add(sim);

         frame.addWindowListener
               (
                  new WindowAdapter()
                  {
                     public void windowClosing(WindowEvent e)
                     {
                        sim.stop();
                        System.exit(0);
                     }
                  }
               );

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Create a simulation
     *
     * @param planet with this planet
     * @param lander and this lander
     * @throws java.io.IOException
     */
    public Simulation(Planet planet, Lander lander) throws IOException
    {
        this.planet = planet;
        this.lander = lander;
        lander.addObserver(this);

        // GUI stuff
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new GridLayout(1, 3));
            JPanel goPanel = new JPanel();
            goPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
                goButton = new JButton("Go!");
                goButton.addActionListener(this);
                goPanel.add(goButton);
                pauseButton = new JButton("Pause");
                pauseButton.addActionListener(this);
                goPanel.add(pauseButton);
            controlPanel.add(goPanel);

            JPanel readoutPanel = new JPanel(new GridLayout(4, 2));
            readoutPanel.setPreferredSize(new Dimension(200, 80));
            readoutPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
                speedYPanel = new SpeedYPanel();
                readoutPanel.add(speedYPanel);
                speedYLabel = new SpeedYLabel();
                readoutPanel.add(speedYLabel);
    
                speedXPanel = new SpeedXPanel();
                readoutPanel.add(speedXPanel);
                speedXLabel = new SpeedXLabel();
                readoutPanel.add(speedXLabel);

                fuelPanel = new FuelPanel();
                readoutPanel.add(fuelPanel);
                fuelLabel = new FuelLabel();
                readoutPanel.add(fuelLabel);
    
                thrustPanel = new ThrustPanel();
                readoutPanel.add(thrustPanel);
                thrustLabel = new ThrustLabel();
                readoutPanel.add(thrustLabel);
            controlPanel.add(readoutPanel);

            JPanel reportPanel = new JPanel();
            reportPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
                reportArea = new JTextArea();
                reportArea.setEditable(false);
                reportArea.setText("Fitness = ???");
                reportPanel.add(reportArea);
            controlPanel.add(reportPanel);

        add(controlPanel, BorderLayout.NORTH);

        animPanel = new AnimPanel();
        add(animPanel, BorderLayout.CENTER);        
    }

    /**
     * Create a simulation of a Mars lander landing on Mars
     *
     * @param controller - controlled by this controller
     * @return the simulation
     * @throws java.io.IOException
     */
    public static Simulation getMarsLanderSimulation(Controller controller) throws IOException
    {
        Planet mars = Planet.getMars();
        LanderSpecs marsLanderSpecs = LanderSpecs.getMarsLanderSpecs();
        Lander marsLander = new Lander(mars, controller, marsLanderSpecs, DELAY);
        Simulation sim = new Simulation(mars, marsLander);

        return sim;
    }

    /*--------------------------------------------------------------------------*/

    public void update(boolean finished)
    {
        if(finished)
        {
            reportArea.setText("Fitness = " + format.format(lander.getFitness()));
        }
        repaint();
    }

    /*--------------------------------------------------------------------------*/

    // GUI stuff

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == goButton)
        {
            try
            {
                if(lander.isRunning())
                {
                    lander.stop();
                }
                lander.reset();
                pauseButton.setText("pause");
                lander.resume();
                go();
            }
            catch(Exception ex){}
        }
        else if(e.getSource() == pauseButton)
        {
            try
            {
                if(lander.isPaused())
                {
                    pauseButton.setText("pause");
                    lander.resume();
                }
                else
                {
                    pauseButton.setText("resume");
                    lander.pause();
                }
            }
            catch(Exception ex){}
        }
    }

    public void go()
    {
        reportArea.setText("Fitness = ???");
        new Thread(lander).start();
    }

    public void stop()
    {
        lander.stop();
    }

    private class AnimPanel extends JPanel
    {
        private Stroke dash = new BasicStroke(1.0f,           // Width
                                   BasicStroke.CAP_SQUARE,    // End cap
                                   BasicStroke.JOIN_MITER,    // Join style
                                   10.0f,                     // Miter limit
                                   new float[] {4.0f,5.0f}, // Dash pattern
                                   0.0f);
        private BufferedImage mars;

        public AnimPanel() throws IOException
        {
            URL url = new URL("file:"+System.getProperty("user.dir")+"/1N201070824EFF703FP0695L0M1.jpg");
            mars = ImageIO.read(url.openStream());

            setBorder(new BevelBorder(BevelBorder.RAISED));
            setPreferredSize(new Dimension(640, 600));
        }

        public void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D)g;

            final int FROM_BOTTOM = 100;
            final double MAX_HEIGHT = 2000.0;
            final double WIDTH = 200.0; // width of scene in m

            super.paintComponent(g);

            // background image
            Dimension size = getSize();
            g2.drawImage(mars, 0, 0, size.width, size.height, null);

            // draw guide lines
            g2.setColor(Color.white);
            Stroke old = g2.getStroke();
            g2.setStroke(dash);
            g2.drawLine(size.width/2, 0, size.width/2, size.height);
            g2.drawLine(0, size.height-FROM_BOTTOM, size.width, size.height-FROM_BOTTOM);
            g2.setStroke(old);

            // where to draw the lander
            double x = size.width*(0.5 + lander.getLocation()/WIDTH);
            double y = size.height*(1.0 - lander.getHeight()/MAX_HEIGHT) - FROM_BOTTOM;

            lander.draw(g2, x, y);
            planet.draw(g2, x, y);
        }
    }

    private class SpeedYPanel extends JPanel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            double speed = lander.getSpeedY();
            if(speed <= lander.getSafeLandingSpeed())
            {
                g.setColor(Color.green);
            }
            else
            {
                g.setColor(Color.red);
            }
            g.fill3DRect(0, 0, (int)(getWidth()*speed/lander.getMaxSpeed()), getHeight(), true);
        }
    }

    private class SpeedYLabel extends JLabel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            format.setMaximumFractionDigits(1);
            g2.drawString(" Down speed: " + format.format(lander.getSpeedY()) + " m/s", 0, getHeight()-5);
        }
    }

    private class SpeedXPanel extends JPanel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            double speed = lander.getSpeedX();
            if(speed <= lander.getSafeLandingSpeed())
            {
                g.setColor(Color.green);
            }
            else
            {
                g.setColor(Color.red);
            }
            g.fill3DRect(0, 0, (int)(getWidth()*speed/lander.getMaxSpeed()), getHeight(), true);
        }
    }

    private class SpeedXLabel extends JLabel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            format.setMaximumFractionDigits(1);
            g2.drawString(" Side speed: " + format.format(lander.getSpeedX()) + " m/s", 0, getHeight()-5);
        }
    }

    private class FuelPanel extends JPanel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            g.setColor(Color.blue);
            g.fill3DRect(0, 0, (int)(getWidth()*lander.getFuel()/lander.getMaxFuel()), getHeight(), true);
        }
    }

    private class FuelLabel extends JLabel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            format.setMaximumFractionDigits(1);
            g2.drawString(" Fuel:  " + format.format(lander.getFuel()) + " kg", 0, getHeight()-5);
        }
    }

    private class ThrustPanel extends JPanel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            g.setColor(Color.blue);
            g.fill3DRect(0, 0, (int)(getWidth()*lander.getThrustLeft()/lander.getMaxThrust()), getHeight(), true);
        }
    }

    private class ThrustLabel extends JLabel
    {
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;
            format.setMaximumFractionDigits(0);
            g2.drawString(" Thrust: " + format.format(lander.getThrustLeft()) + " N", 0, getHeight()-5);
        }
    }

}

