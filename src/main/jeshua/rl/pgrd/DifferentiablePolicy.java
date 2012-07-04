package jeshua.rl.pgrd;

import jeshua.rl.State;

public interface DifferentiablePolicy extends DifferentiableFunction2D {	
	/**
	 * Update policy and gradients for a given state.
	 * @param st - state to plan from
	 * @return OutputGradientPair[y=policy,dy=log_grad_policy]
	 *    policy: num_actions length probability vector.
	 *    grad_policy: x num_reward_features array.
	 *      For each action a, getGradPolicy()[a] is gradient w.r.t. parameters theta
	 */
	public OutputAndGradient2D evaluate(State st);
	
	public OutputAndGradient2D getCurrentPolicy(); //just return most recent policy and gradient
}
