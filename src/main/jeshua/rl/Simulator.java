package jeshua.rl;

/**
 * Represents a generative model. All information should be kept in a state variable
 * so a planning algorithm can reset the simulator to specific states.
 * @author Jeshua Bratman
 */
public interface Simulator {
    public State getState();
    public void setState(State state);
    public void takeAction(int a); 
    public int getNumActions();    
    public double getDiscountFactor();
    public double getReward(); 			//reward at current state 
    public void initEpisode();
}
