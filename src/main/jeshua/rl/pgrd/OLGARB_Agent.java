package jeshua.rl.pgrd;

import java.util.Random;

import jeshua.rl.Simulator;
import jeshua.rl.State;
import jeshua.rl.Utils;

public class OLGARB_Agent {
	
  private DifferentiableQFunction qf;
	private SoftmaxPolicy           policy;
	private OLGARB                  policy_gradient;
	
	private DifferentiableRFunction rf;
	public DifferentiableRFunction getRF(){return rf;}
	
	private int previous_action;
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
		 previous_action = -1;	
		 this.qf = qf;
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
				
		if(st.isAbsorbing()){
		  this.policy_gradient.initEpisode();
		  previous_action = -1;
		} else
		  previous_action = Utils.sampleMultinomial(mu,this.random);
		return previous_action;
	}
	
	
	public DifferentiableQFunction getQF(){return qf;}
}
