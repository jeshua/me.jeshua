package jeshua.rl.uct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jeshua.rl.Utils.Maximizer;
import jeshua.rl.Simulator;
import jeshua.rl.State;

/**
 * UCT Planning algorithm. Takes a simulator and search parameters, then call
 * plan() to do UCT planning to estimate Q values at root node.
 * 
 * @author Jeshua Bratman
 */
public class UCT2 {
	
	protected class StateAtDepthVal{
		public ArrayList<HashMap<State, Double>> N;		
		public StateAtDepthVal(int maxDepth){
			N = new ArrayList<HashMap<State,Double>>(maxDepth);
			for(int d=0;d<maxDepth;d++) N.add(new HashMap<State,Double>());
		}		
		public double get(State s, int d){
			if(N.get(d).containsKey(s))
			  return N.get(d).get(s);
			else{
			    N.get(d).put(s, 0d);
			    return 0;
			}
		}
		public void add(State s, int d, double amount){			
			HashMap<State,Double> n = N.get(d);
			if(n.containsKey(s)) n.put(s, n.get(s) + amount);
			else n.put(s, amount);
		}
		public void set(State s, int d, double val){
			N.get(d).put(s, val);			
		}
	}
	
	protected class StateActionAtDepthVal{
		public ArrayList<StateAtDepthVal> N;
		public StateActionAtDepthVal(int maxDepth, int numActions){
			N = new ArrayList<StateAtDepthVal>(numActions);			
			for(int i = 0;i<numActions;i++) N.add(new StateAtDepthVal(maxDepth));
		}
		public double get(State s, int a, int d){return N.get(a).get(s,d);}
		public void add(State s, int a, int d, double amount){N.get(a).add(s, d, amount);}
		public void set(State s, int a, int d, double val){N.get(a).set(s, d, val);}
	}
	public StateActionAtDepthVal Q;
	public StateActionAtDepthVal sadCount;
	public StateAtDepthVal sdCount;
	
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
	public UCT2(Simulator sim, int trajectories, int depth, double gamma,
			Random random) {
		this.random = random;
		this.maximizer = new Maximizer(random);
		this.maxDepth = depth;
		this.numTrajectories = trajectories;
		this.gamma = gamma;
		this.simulator = sim;
		this.numActions = sim.getNumActions();
		
		this.Q = new StateActionAtDepthVal(maxDepth,numActions);
		this.sadCount = new StateActionAtDepthVal(maxDepth,numActions);
		this.sdCount = new StateAtDepthVal(maxDepth);
	}

	/**
	 * Plan starting from a root state.
	 * 
	 * @param state
	 *            Current state.
	 * @return
	 */
	public int plan(State state) {
		this.rootState = state.copy();
		for (int i = 0; i < numTrajectories; ++i) {
			simulator.setState(state.copy());
			plan(state.copy(), 0);
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
		for (int a = 0; a < numActions; a++) {
			maximizer.add(Q.get(rootState, a, 0), a);
		}
		return maximizer.getMaxIndex();
	}
	
	public double getQ(int action) {
		return Q.get(rootState,action,0);
	}

	// ======================================================================
	// IMPLEMENTATION

	protected Random random;
	protected Simulator simulator;
	protected double gamma; // discount factor

	protected int numActions;
	protected Maximizer maximizer;

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
	protected double plan(State state, int depth) {
		// BASE CASES:
		if (state.isAbsorbing()) {// end of episode
			return endEpisodeValue;
		} else if (depth >= maxDepth) {// leaf node
			return leafValue;
		}
		// UCT RECURSION:
		else {
			// simulate an action
			int action = ucbAction(state,depth);
			simulator.takeAction(action);			
			
			// take snapshot of current reward and state of simulator
			double r = simulator.getReward();
			State state2 = simulator.getState().copy();

			// recurse to get Q estimate
			double q = r + gamma * plan(state2, depth + 1);

			// add to Q value rolling average
			double c = this.getStateActionCount(state,action,depth);
			double alpha = 1d/(c+1);
			double oldQ = this.getQ(state,action,depth);
			double newQ = oldQ + alpha * (q - oldQ);
			Q.set(state,action,depth,newQ);
			// increment counts
			sdCount.add(state,depth,1);
			sadCount.set(state,action,depth,c+1);
			
			return q;
		}
	}
	
	protected int getStateActionCount(State state, int action, int depth){
	  return (int)this.sadCount.get(state,action,depth);
	}
	protected int getStateCount(State state, int depth){
	  return (int)sdCount.get(state,depth);
	}
	protected double getQ(State state, int action, int depth){
	  return Q.get(state, action, depth);
	}
	
	/**
	 * Get the action at a given node using the UCB1 rule
	 * @param node - state node at which to choose action
	 * @return - chosen action
	 */
	protected int ucbAction(State state, int depth) {
		maximizer.clear();
		double numerator = 2*Math.log(this.getStateCount(state, depth));

		for (int a = 0; a < numActions; ++a) {
			double q = this.getQ(state,a,depth);
			double c = this.getStateActionCount(state,a,depth);
			
			if(c == 0) q = Double.MAX_VALUE;
			else       q += this.ucbScaler * Math.sqrt(numerator / c);
			
			maximizer.add(q, a);
		}		
		return maximizer.getMaxIndex();
	}

}
