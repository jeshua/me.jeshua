package jeshua.rl.uct;

import java.util.Arrays;
import java.util.HashMap;

import jeshua.rl.State;

public class UCTNodes {	
	/**
	 * Associates UCT state nodes with a state/depth pair so we can share information between
	 * states at the same depth. 
	 * 
	 * @author Jeshua Bratman
	 */
	public static class UCTNodeStore{
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
	
	
	/**
	 * Represents an action in the UCT tree. For each state reached while taking this
	 * action from the parent, an action node has one state node child.
	 * @author Jeshua Bratman
	 */
	public static class UCTActionNode {
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
	
	/**
	 * A state node represents a single state in the UCT tree. Each state node
	 * has a Q value associating current estimated value for each action and a
	 * action node child. Additionally, state nodes store statistics about
	 * the number of visits and actions attempted.
	 * 
	 * @author Jeshua Bratman
	 *
	 */
	public static class UCTStateNode 
	{ 
		private static final double INITIAL_VALUE = 0d;
		
	    private final UCTNodeStore nodeCache;
	    public double Q[]; 
	    public int sCount;//number of times visiting this state
	    public int saCounts[];//number of times visiting this state on each action

	    UCTActionNode[] children;

	    public UCTStateNode(UCTNodeStore nodeCache, int numActions)
	    {
	        this.nodeCache = nodeCache;
	        this.Q = new double[numActions];
	        this.saCounts = new int[numActions];
	        this.children = new UCTActionNode[numActions];
	        for(int a = 0; a < numActions; ++a)
	            children[a] = new UCTActionNode();
	        Arrays.fill(this.Q, UCTStateNode.INITIAL_VALUE);
	        this.sCount = 1;
	        Arrays.fill(saCounts, 0);        
	    }

	    //get the a child node when following a certain action.
	    public UCTStateNode getChildNode(int action, State state, int depth) {
	    	//get action node for this action
	        UCTActionNode actNode = children[action];
	        //get state node associated with that action node
	        UCTStateNode child = actNode.get(state);
	        //if it is null, then we haven't visited state/action at this depth        
	        if(child == null){
	            child = nodeCache.checkout(state,depth);
	            actNode.add(state.copy(), child);
	        }        
	        return child;
	    }
	}
}
