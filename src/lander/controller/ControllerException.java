package lander.controller;

/**
 * Exception thrown if anything goes wrong with a lander controller
 *
 * @author phi
 * @version 2009/2
 */

public class ControllerException extends Exception
{
    /**
     *
     * @param message - message to display
     */
    ControllerException(String message)
    {
        super(message);
    }
}
