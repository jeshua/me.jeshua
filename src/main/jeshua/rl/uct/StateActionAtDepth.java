package jeshua.rl.uct;

import jeshua.rl.State;

public class StateActionAtDepth {
	public State state;
	public int depth;
	public int action;

	public StateActionAtDepth(State s, int a,int d) {
		this.state = s;
		this.action = a;
		this.depth = d;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + action;
		result = prime * result + depth;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateActionAtDepth other = (StateActionAtDepth) obj;
		if (action != other.action)
			return false;
		if (depth != other.depth)
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
}
