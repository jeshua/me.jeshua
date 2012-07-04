package jeshua.rl.uct;

import java.util.Random;

import jeshua.rl.Utils.Maximizer;
import jeshua.rl.Simulator;
import jeshua.rl.State;
import jeshua.rl.uct.UCTNodes.*;

/**
 * UCT Planning algorithm. Takes a simulator and search parameters, then call
 * plan() to do UCT planning to estimate Q values at root node.
 * 
 * @author Jeshua Bratman
 */
public class UCT {	
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
		this.cache = new UCTNodes.UCTNodeStore(this.numActions);
		this.root = null;	
	}

	/**
	 * Plan starting from a root state.
	 * 
	 * @param state
	 *            Current state.
	 * @return
	 */
	public int planAndAct(State state) {
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
	 * Get the greedy action given the current Q values.
	 * @return action index
	 */
	protected int getGreedyAction() {
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
			
			// recurse to get sample of Q
			double q = r + gamma * plan(state2, child, depth + 1);

			//update counts
			node.sCount++;
			int sa_count = ++node.saCounts[action];
			
			// compute rolling average for Q
			double alpha = 1d/sa_count;
			node.Q[action] += (q - node.Q[action]) * alpha;
			
			return q;
		}
	}

	/**
	 * Get the action at a given node using the UCB rule
	 * @param node - state node at which to choose action
	 * @return - chosen action
	 */
	protected int getPlanningAction(UCTStateNode node) {
		if (node == null) return random.nextInt(numActions);
		else {
			maximizer.clear();
			double numerator = Math.log(node.sCount);
			for (int a = 0; a < numActions; ++a) {
				double val = node.Q[a];
				//if this action has never been tried, give it max_value
				if (node.saCounts[a] == 0) val = Double.MAX_VALUE;
				//otherwise use UCB1 rule
				else val += ucbScaler * Math.sqrt(numerator / node.saCounts[a]);

				maximizer.add(val, a);
			}
			return maximizer.getMaxIndex();			
		}
	}
}
