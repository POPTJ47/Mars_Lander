package lander.controller;

import java.awt.geom.Point2D;


/**
   Interface for a lander controller.
    
   @author phi
   @version 2009/2 - Part B
*/

public interface Controller
{
    /**
     *
     * @param height - distance above ground in m
     * @param speedY - rate of descent in m/s
     * @param location - distance left (-ve) or right (+ve) or target
     * @param speedX - speed left or right
     * @param rotation - angle of rotation in degrees
     * @param rotationSpeed - how fast the lander is spinning in degrees/s
     * @param fuel - amount of fuel left in kg
     * @return - (left thrust in N, right thrust in N)
     * @throws ControllerException
     */
    public Point2D.Double getThrust(double height, double speedY, double location, double speedX, double rotation, double rotationSpeed, double fuel) throws ControllerException;
}
