package jeshua.rl.pgrd;

import java.util.Random;

import jeshua.rl.Simulator;
import jeshua.rl.State;
import jeshua.rl.Utils;

public class PGRD_UCT {
	
	private RewardDifferentiableUCT planner;
	private SoftmaxPolicy           policy;
	private OLGARB                  policy_gradient;
	
	private DifferentiableRewardFunction rf;
	public DifferentiableRewardFunction getRF(){return rf;}
	
	private int previous_action;
	private Random random;
	
	
	/**
	 * 
	 * @param sim          -- simulator to give to planner
	 * @param rf           -- reward function
	 * @param gamma        -- reward discount factor
	 * @param alpha        -- policy gradient learning rate
	 * @param temperature  -- softmax policy temperature
	 * @param depth        -- uct planning depth
	 * @param trajectories -- uct planning trajectory count
	 */
	public PGRD_UCT(Simulator sim, DifferentiableRewardFunction rf,
			        double alpha, double temperature,
			        int trajectories, int depth, 
			        double gamma, Random random){
			this.random = random;
		 planner         = new RewardDifferentiableUCT(sim, rf, trajectories, depth, gamma, random);
		 policy          = new SoftmaxPolicy(planner, temperature);
		 policy_gradient = new OLGARB(policy,alpha,gamma,true);
		 previous_action = -1;		
		 this.rf = rf;
	}
	
	
	
	
	/**
	 * Plans from given state, updates reward parameters, returns chosen action
	 * @param st     -- current state
	 * @param reward -- objective reward sample
	 * @return chosen action
	 */
	public int step(State st, double reward){
		
		double[] mu = policy.evaluate(st).y;		
		if(previous_action >= 0)
			this.policy_gradient.learn(previous_action, st, reward);
		
		previous_action = Utils.sampleMultinomial(mu,this.random);
		return previous_action;
	}
	
}
