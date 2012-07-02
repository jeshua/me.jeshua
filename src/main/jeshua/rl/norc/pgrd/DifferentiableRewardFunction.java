package jeshua.rl.norc.pgrd;

import jeshua.rl.State;

public interface DifferentiableRewardFunction extends RewardFunction {
    public void fillGradient(State state1, int action, State state2, double[] gradient);  
    public int  numParams();
    public void updateParams(double rate, double[]delta);
}
