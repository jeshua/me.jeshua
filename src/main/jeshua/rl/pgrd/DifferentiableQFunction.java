package jeshua.rl.pgrd;

import jeshua.rl.State;

/**
 * Planner that computes Q value and gradients of those Q value w.r.t. reward features
 * @author Jeshua Bratman
 */
public interface DifferentiableQFunction extends DifferentiableFunction2D {		
	/**
	 * Update Q values and Q value gradients for a given state.
	 * @return OutputAndGradient[y=Q,dy=dQ]:
	 * 	  Q : num_actions length probability vector
	 *    dQ: num_actions x numParams array.
	 *        For each action a, getGradPolicy()[a] is gradient w.r.t. reward features
	 * @param st
	 */
	public OutputAndGradient2D evaluate(State st);
}
