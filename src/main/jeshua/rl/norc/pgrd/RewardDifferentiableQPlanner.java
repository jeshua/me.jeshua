package jeshua.rl.norc.pgrd;

/**
 * Represents planner that computes Q value and gradients of those Q value w.r.t. reward features
 * @author jeshua
 *
 */
public interface RewardDifferentiableQPlanner {
  public double[][] getGradQ();
  public double[] getQ();
}
