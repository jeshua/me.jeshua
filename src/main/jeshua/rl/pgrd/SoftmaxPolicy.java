package jeshua.rl.pgrd;

import java.util.Random;

import jeshua.rl.State;

public class SoftmaxPolicy implements DifferentiablePolicy {
  protected DifferentiableQFunction planner;
  protected double temperature;
  protected double[] policy;
  protected double[][] log_grad_policy;

  public SoftmaxPolicy(DifferentiableQFunction planner, double temperature){
    this.planner = planner;
    this.temperature = temperature;
    this.policy = null;		
    this.log_grad_policy = null;		
  }

  public OutputAndGradient2D evaluate(State st) {
    OutputAndGradient2D qdq = this.planner.evaluate(st);
    double[][] grad_Q = qdq.dy;
    double[] Q = qdq.y;
    int num_actions = grad_Q.length;
    int num_reward_features = grad_Q[0].length;		

    /** Compute softmax policy.
     *    Softmax policy: mu(a) = exp((1/tau)*Q(a)) / (sum_b exp(tau*Q(b)))
     *       Where mu(a) is probability of taking action a, and tau is temperature parameter.
     * [note, we subtract max_a(Q(a)) from Q(a) to prevent overflow (while retaining correct probabilities)] 
     */
    if(this.policy == null) this.policy = new double[num_actions];
    double denom = 0;
    double max = Double.NEGATIVE_INFINITY;	    
    double t = 1/temperature;
    for(int a = 0; a < num_actions; a++) max = Math.max(max, Q[a]);    
    for(int a = 0; a < num_actions; a++) {	      
      this.policy[a] = Math.exp(t * (Q[a] - max));
      denom += this.policy[a];
    }        
    for(int a = 0; a < num_actions; a++) policy[a] /= denom;

    /** Compute gradient of the softmax policy w.r.t. reward features
     *   (log) Gradient of softmax policy is:
     *       dmu(a)/dtheta = (1/tau) * mu(a) * (dQ(a) - sum_b (mu(b) * dQ(b)))
     */
    if(this.log_grad_policy == null)
      this.log_grad_policy = new double[num_actions][num_reward_features];

    for (int i = 0; i < num_reward_features; ++i) {
      double sum = 0;
      for (int b = 0; b < num_actions; ++b) sum += policy[b] * grad_Q[b][i];
      for (int a = 0; a < num_actions; ++a) 
        this.log_grad_policy[a][i] = t * policy[a] * (grad_Q[a][i] - sum);
    }

    //return policy and log gradient of policy

    return getCurrentPolicy();	    
  }

  //===========================================================
  //Differentiable Function Interface

  @Override
  public OutputAndGradient2D evaluate(Object st) {return evaluate(((State)st));}
  @Override
  public int numParams() {
    return this.planner.numParams();
  }
  @Override
  public void setParams(double[] theta) {
    this.planner.setParams(theta);
  }
  @Override
  public double[] getParams() {
    return this.planner.getParams();
  }
  @Override
  public OutputAndGradient2D getCurrentPolicy() {
    OutputAndGradient2D ret = new OutputAndGradient2D();
    ret.y= this.policy;
    ret.dy = this.log_grad_policy;
    ret.logspace = false;
    return ret;
  }
  @Override
  public Object generateRandomInput(Random rand) {
    return planner.generateRandomInput(rand);
  }
}
