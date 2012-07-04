package jeshua.rl.pgrd;

import jeshua.rl.State;

/**
 * OLPOMDP Policy Gradient Algorithm
 * @author Jeshua Bratman
 *
 */
public class OLGARB {
	private DifferentiablePolicy policy;
    private double alpha;        //learning rate
    private double gamma;        //discount factor
    private double baseline;     //baseline
    private boolean use_baseline;

    private int timestep;    
    private int num_params;
    private double [] Z;         //eligibility trace vector
    private double [] theta;     //policy parameterization
    
    
	public OLGARB(DifferentiablePolicy policy, double alpha, double gamma,boolean use_baseline){
		this.policy = policy;
		this.alpha = alpha;
		this.gamma = gamma;
		this.use_baseline = use_baseline;
		this.num_params = policy.numParams();
		this.theta = new double[num_params];
		this.Z = new double[num_params];
		this.timestep = 0;
		this.baseline = 1;
	}
	public OLGARB(DifferentiablePolicy policy, double alpha, double gamma)
	{this(policy,alpha,gamma,false);};    
	
	
	public void learn(int previous_action, State st, double reward){
		double[][] grad = policy.getCurrentPolicy().dy;
		
		timestep++;
		baseline += (1d/timestep) * (reward - baseline);//rolling average			
		
		for(int i = 0; i < num_params; ++i){
			Z[i] = gamma * Z[i] + grad[previous_action][i];
		}
		this.theta = this.policy.getParams();
		double rate = 0;
		if (use_baseline) rate = alpha * (reward-baseline);
		else rate = alpha * reward;
		
		for(int i=0;i<num_params;i++)
			theta[i] += rate * Z[i];
		policy.setParams(theta);
	}
}
