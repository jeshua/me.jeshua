package jeshua.rl.pgrd;

import java.util.Arrays;

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
		this.initEpisode();
	}
	public OLGARB(DifferentiablePolicy policy, double alpha, double gamma)
	{this(policy,alpha,gamma,false);};    
	
	public void initEpisode (){	  
	  Arrays.fill(Z, 0);
	  this.baseline = 0;
	  this.timestep = 0;	  
	}
	
	public void learn(State st1, int a1, double reward){	
		double[] mu = policy.getCurrentPolicy().y; 
		double[][] dmu = policy.getCurrentPolicy().dy;
		
		timestep++;
		baseline += (1d/timestep) * (reward - baseline);//rolling average			
		
		for(int i = 0; i < num_params; ++i){
			Z[i] = gamma * Z[i] + dmu[a1][i]/mu[a1];
		}
		this.theta = this.policy.getParams().clone();
		double delta = 0;
		if (use_baseline) delta = alpha * (reward-baseline);
		else delta = alpha * reward;
		
		for(int i=0;i<num_params;i++)
			theta[i] += delta * Z[i];
		policy.setParams(theta);
	}
}
