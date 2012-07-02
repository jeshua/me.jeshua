package jeshua.rl.uct;
import java.util.Arrays;

import jeshua.rl.State;

/**
 * Represents an action in the UCT tree. For each state reached while taking this
 * action from the parent, an action node has one state node child.
 * @author Jeshua Bratman
 */
public class UCTActionNode {
	private static final int INIT_SIZE = 5;
	private static final int EXPAND_FACTOR = 2;
	int currBranches;
	State[] childStates = new State[INIT_SIZE];
	UCTStateNode[] childNodes = new UCTStateNode[INIT_SIZE];

	public UCTActionNode() {
		this.currBranches = 0;
	}

	UCTStateNode get(State state) {
		for (int i = 0; i < currBranches; ++i)
			if (childStates[i].equals(state))
				return childNodes[i];
		return null;
	}

	void add(State state, UCTStateNode node) {
		if (currBranches >= childNodes.length) {
			int newlen = EXPAND_FACTOR * childNodes.length;
			childNodes = Arrays.copyOf(childNodes, newlen);
			childStates = Arrays.copyOf(childStates, newlen);
		}
		childNodes[currBranches] = node;
		childStates[currBranches] = state;
		currBranches++;
	}
}