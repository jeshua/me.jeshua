package jeshua.rl.uct;

import java.util.HashMap;

import jeshua.rl.State;

/**
 * Associates UCT state nodes with a state/depth pair so we can share information between
 * states at the same depth. 
 * 
 * @author Jeshua Bratman
 */
public class UCTNodeStore {
	private int numActions;
	private HashMap<StateAtDepth, UCTStateNode> activeNodes;
	public boolean hashStates = true;
	public boolean considerDepth = false;

	public UCTNodeStore(int numActions) {
		this.numActions = numActions;
		this.activeNodes = new HashMap<StateAtDepth,UCTStateNode>();
	}

	/**
	 * Call this to get a state node object either from from the hash table or
	 * by constructing a new instance.
	 * 
	 * @return
	 */
	public UCTStateNode checkout(State state,int depth) {
		
		StateAtDepth sad;
		if(considerDepth)  sad = new StateAtDepth(state,depth);
		else               sad = new StateAtDepth(state,0);
		
		if (hashStates && activeNodes.containsKey(sad)) {			
			return activeNodes.get(sad);
		} else {
			UCTStateNode n = new UCTStateNode(this, numActions);
			if (hashStates)
				activeNodes.put(sad, n);
			return n;
		}
	}

	public void clearHash() {
		this.activeNodes.clear();
	}
}