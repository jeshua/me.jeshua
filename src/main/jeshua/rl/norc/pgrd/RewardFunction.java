package jeshua.rl.norc.pgrd;
import jeshua.rl.State;

public interface RewardFunction{
	public double getReward(State s1, int a, double r, State s2);
}
