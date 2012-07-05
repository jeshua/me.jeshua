package jeshua.rl.pgrd;

import java.util.Random;

import jeshua.rl.Agent;
import jeshua.rl.State;
import jeshua.rl.Utils;

public class OLGARB_Agent implements Agent {
	
  private DifferentiableQFunction qf;
	private SoftmaxPolicy           policy;
	private OLGARB                  policy_gradient;
		
	private Random random;	
	
	/**
	 * 
	 * @param qf           -- q function
	 * @param gamma        -- reward discount factor
	 * @param alpha        -- policy gradient learning rate
	 * @param temperature  -- softmax policy temperature
	 * @param depth        -- uct planning depth
	 * @param trajectories -- uct planning trajectory count
	 */
	public OLGARB_Agent(DifferentiableQFunction qf,
			        double alpha, double temperature,			        
			        double gamma, Random random){
			this.random = random;
		 policy          = new SoftmaxPolicy(qf, temperature);
		 policy_gradient = new OLGARB(policy,alpha,gamma,false);
		 this.qf = qf;
	}
	
	
	/**
	 * Plans from given state, updates reward parameters, returns chosen action
	 * @param st     -- current state
	 * @param reward -- objective reward sample
	 * @return chosen action
	 */
	public int step(State st1){
		policy.evaluate(st1);
		return Utils.sampleMultinomial(policy.getCurrentPolicy().y,this.random);
	}
	public int step(State st1, int a1, State st2, double reward){
		this.policy_gradient.learn(st1, a1, reward);		
		
		int new_action;
		if(st2.isAbsorbing()){
		  this.policy_gradient.initEpisode();
		  new_action = -1;
		} else
		  new_action = step(st2);		
		return new_action;
	}		
	public DifferentiableQFunction getQF(){return qf;}	
}
