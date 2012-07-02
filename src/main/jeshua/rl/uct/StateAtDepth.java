package jeshua.rl.uct;

import jeshua.rl.State;

/**
 * Just hashable class containing a state/depth pair.
 * 
 * @author Jeshua Bratman
 */
public class StateAtDepth {
	public State state;
	public int depth;

	public StateAtDepth(State s, int d) {
		this.state = s;
		this.depth = d;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		StateAtDepth other = (StateAtDepth) obj;
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
