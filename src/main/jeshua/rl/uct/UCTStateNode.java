package jeshua.rl.uct;
import java.util.Arrays;

import jeshua.rl.State;


/**
 * A state node represents a single state in the UCT tree. Each state node
 * has a Q value associating current estimated value for each action and a
 * action node child. Additionally, state nodes store statistics about
 * the number of visits and actions attempted.
 * 
 * @author Jeshua Bratman
 *
 */
public class UCTStateNode 
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