package jeshua.rl.norc.pgrd;

public class SoftmaxPolicy {

  //returns policy from current state
  public static double[] getPolicy(RewardDifferentiableQPlanner qp, double temperature) {
    double probsum = 0;
    double[] Q = qp.getQ();
    int num_actions = Q.length;
    double[] ret = new double[num_actions];

    double max = Double.NEGATIVE_INFINITY;
    //compute probabilities in log space
    for(int a = 0; a < num_actions; a++) max = Math.max(max, Q[a]);    
    for(int a = 0; a < num_actions; a++) {
      double temp = Math.exp(temperature * (Q[a] - max));
      ret[a] = temp;
      probsum += temp;
    }        
    for(int a = 0; a < num_actions; a++) ret[a] /= probsum;
    return ret;
  }

//gradient of log policy probabilities
public static double[] getPolicyGrad(RewardDifferentiableQPlanner qp, int action, double[] psi) 
{
  double[] Q = qp.getQ();
  double[][] dQ = qp.getGradQ();
  int num_actions = dQ.length;
  int num_reward_features = dQ[0].length;
  for (int i = 0; i < num_reward_features; ++i) {
    double grad = grad_q[action][i];
    for (int b = 0; b < num_actions; ++b) {
      grad -= action_cache[b] * grad_q[b][i];
    }
    psi[i] = grad * temperature;
  }

}
}
