package jeshua.rl.uct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jeshua.rl.Maximizer;
import jeshua.rl.Simulator;
import jeshua.rl.State;

/**
 * UCT Planning algorithm. Takes a simulator and search parameters, then call
 * plan() to do UCT planning to estimate Q values at root node.
 * 
 * @author Jeshua Bratman
 */
public class UCT {
	
	protected class StateAtDepthVal{
		public ArrayList<HashMap<State, Double>> N;		
		public StateAtDepthVal(int maxDepth){
			N = new ArrayList<HashMap<State,Double>>();
			N.ensureCapacity(maxDepth);
		}		
		public double get(State s, int d){
			if(N.contains(s))
				return N.get(d).get(s);
			else
				return 0;
		}
		public void add(State s, int d, double amount){
			HashMap<State,Double> n = N.get(d);
			if(n.containsKey(s)) n.put(s, n.get(s) + amount);
			else n.put(s, amount);
		}
	}
	
	protected class StateActionAtDepthVal{
		public ArrayList<StateAtDepthVal> N;
		public StateActionAtDepthVal(int maxDepth, int numActions){
			N = new ArrayList<StateAtDepthVal>();			
			for(int i = 0;i<numActions;i++) N.add(new StateAtDepthVal(maxDepth));
		}
		public double get(State s, int a, int d){return N.get(a).get(s,d);}
		public void add(State s, int a, int d, double val){N.get(a).add(s, d, val);}
	}
	public StateActionAtDepthVal Q;
	public StateActionAtDepthVal Nsd;
	public StateAtDepthVal Ns;
	
	// this is the C value for UCB:
	public double ucbScaler = 1;
	// default value at leave of tree
	public double leafValue = 0;
	// default value at the end of an episode
	public double endEpisodeValue = 0;
	
	
	// ================================================================================
	// PUBLIC INTERFACE

	/**
	 * Create a new UCT planner
	 * 
	 * @param sim
	 *            Simulator object (note: this will be modified so pass in a copy!)
	 * @param trajectories
	 *            Number of trajectories per planning step.
	 * @param depth
	 *            Maximum search depth per trajectory.
	 * @param gamma
	 *            Discount factor.
	 * @param random
	 *            Random number generator for all tie breakers and action
	 *            decisions.
	 */
	public UCT(Simulator sim, int trajectories, int depth, double gamma,
			Random random) {
		this.random = random;
		this.maximizer = new Maximizer(random);
		this.maxDepth = depth;
		this.numTrajectories = trajectories;
		this.gamma = gamma;
		this.simulator = sim;
		this.numActions = sim.getNumActions();
		this.cache = new UCTNodeStore(this.numActions);
		this.root = null;
		
		this.Q = new StateActionAtDepthVal(maxDepth,numActions);
		this.Nsd = new StateActionAtDepthVal(maxDepth,numActions);
		this.Ns = new StateAtDepthVal(maxDepth);		
	}

	/**
	 * Plan starting from a root state.
	 * 
	 * @param state
	 *            Current state.
	 * @return
	 */
	public int plan(State state) {
		cache.clearHash();		
		this.rootState = state.copy();
		this.root = cache.checkout(rootState,0);
		for (int i = 0; i < numTrajectories; ++i) {
			simulator.setState(state.copy());
			plan(state.copy(), root, 0);
		}
		return getGreedyAction();
	}

	/**
	 * Get the greedy action given the current Q function. (note you must call
	 * plan first)
	 * 
	 * @return action index
	 */
	public int getGreedyAction() {
		maximizer.clear();
		double[] Q = root.Q;
		for (int a = 0; a < numActions; a++) {
			maximizer.add(Q[a], a);
		}
		return maximizer.getMaxIndex();
	}

	public double getQ(int action) {
		return root.Q[action];
	}

	// ======================================================================
	// IMPLEMENTATION

	protected Random random;
	protected Simulator simulator;
	protected double gamma; // discount factor

	protected int numActions;
	protected Maximizer maximizer;

	protected UCTNodeStore cache;
	protected UCTStateNode root;
	protected State rootState;
	protected int maxDepth;
	protected int numTrajectories;

	/**
	 * UCT planning procedure
	 * 
	 * @param state
	 *            Current state.
	 * @param node
	 *            Current node in the uct tree.
	 * @param depth
	 *            Current depth.
	 * @return
	 */
	protected double plan(State state, UCTStateNode node, int depth) {
		// BASE CASES:
		if (state.isAbsorbing()) {// end of episode
			// System.out.println("DONE");
			return endEpisodeValue;
		} else if (depth >= maxDepth) {// leaf node
			return leafValue;
		}
		// UCT RECURSION:
		else {
			// simulate an action
			int action = getPlanningAction(node);
			simulator.takeAction(action);			
			
			// take snapshot of current reward and state of simulator
			double r = simulator.getReward();
			State state2 = simulator.getState().copy();

			UCTStateNode child = node.getChildNode(action, state2,depth+1);
			// calculate Q via recursion
			double q = r + gamma * plan(state2, child, depth + 1);

			node.sCount++;
			int sa_count = ++node.saCounts[action];
			// compute rolling average
			node.Q[action] += (q - node.Q[action]) / sa_count;
			return q;
			/*double maxQ = Double.NEGATIVE_INFINITY;
            for(int i = 1;i<this.numActions;i++)
                if(node.Q[i] > maxQ) maxQ = node.Q[i];
            return maxQ;*/
		}
	}

	
	/**
	 * Update the Q function estimate at a node.
	 */
	//protected void updateValue()
	
	
	/**
	 * Get the action at a given node using the UCB rule
	 * @param node - state node at which to choose action
	 * @return - chosen action
	 */
	protected int getPlanningAction(UCTStateNode node) {
		if (node == null)
			return random.nextInt(numActions);
		else {
			maximizer.clear();

			double numerator = 0;
			numerator = Math.log(node.sCount);

			for (int a = 0; a < numActions; ++a) {
				double val = node.Q[a];
				if (node.saCounts[a] == 0)
					val = Double.MAX_VALUE;
				else
					val += ucbScaler * Math.sqrt(numerator / node.saCounts[a]);

				maximizer.add(val, a);
			}
			int mx = maximizer.getMaxIndex();
			return mx;
		}
	}
}
