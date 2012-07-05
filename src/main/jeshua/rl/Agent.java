package jeshua.rl;

public interface Agent {
	public int step(State st1);
	public int step(State st1, int a1, State st2, double reward);
}
