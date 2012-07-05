package jeshua.rl.pgrd;

import jeshua.rl.State;

/**
 * DifferentiableRewardFunction is a parameterized reward function for use in
 * gradient reward design methods e.g. PGRD
 * 
 * It is a RewardFunction (has getReward(state,action,state)
 * It is a DifferentiableFunction1D because it is a scalar valued function and has 
 *     an evaluate function to get reward and reward gradient
 * @author jeshua
 *
 */
public interface DifferentiableRFunction extends RewardFunction, DifferentiableFunction1D{	
	/**
	 * Compute gradient of the reward function w.r.t. parameters theta for a given (s,a,s) triple.
	 * @param state1
	 * @param action
	 * @param state2
	 * @return numParams length vector
	 */
    public double[] getGradR(State state1, int action, State state2);  
    
    
    public class SASTriple{    
    	public State state1;
    	public int action;
    	public State state2;
    }    
}
