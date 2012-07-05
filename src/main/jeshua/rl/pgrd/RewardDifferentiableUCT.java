package jeshua.rl.pgrd;

import java.util.Arrays;
import java.util.Random;

import jeshua.rl.Simulator;
import jeshua.rl.State;
import jeshua.rl.pgrd.DifferentiableRFunction.SASTriple;
import jeshua.rl.uct.UCT;
import jeshua.rl.uct.UCTNodes.*;

public class RewardDifferentiableUCT extends UCT implements DifferentiableQFunction {

  protected double[][] gradQ;
  protected double[][] gradR;
  protected double[] gradQtmp;
  protected int numRewardFeatures;
  protected DifferentiableRFunction diffRF;

  public RewardDifferentiableUCT(Simulator sim, DifferentiableRFunction diffRF, 
		  int trajectories, int depth, double gamma, Random random) {
    super(sim, trajectories, depth, gamma, random);
    this.diffRF = diffRF;
    this.numRewardFeatures = diffRF.numParams();
    this.gradQtmp = new double[this.numRewardFeatures];
    this.gradQ = new double[super.numActions][this.numRewardFeatures];
    this.gradR = new double[super.maxDepth+1][this.numRewardFeatures];
  }

  /**
   * Plan starting from given state.
   * @param state
   * @return
   */
  public OutputAndGradient2D evaluate(State state) {
    cache.clearHash();		
    this.rootState = state.copy();
    this.root = cache.checkout(rootState,0);
    for (int i = 0; i < numTrajectories; ++i) {
      simulator.setState(state.copy());
      Arrays.fill(gradQtmp, 0);
      plan(state.copy(), root, 0);
    }    
    
    OutputAndGradient2D ret = new OutputAndGradient2D();
    ret.dy = this.gradQ;
    ret.y = this.root.Q;    
    return ret;
  }


  protected double plan(State state, UCTStateNode node, int depth) {
    // BASE CASES:
    if (state.isAbsorbing()) {// end of episode
      Arrays.fill(gradR[depth],0);
      return endEpisodeValue;
    } else if (depth >= maxDepth) {// leaf node
      Arrays.fill(gradR[depth],0);
      return leafValue;
    }
    // UCT RECURSION:
    else {
      // simulate an action
      int action = getPlanningAction(node);
      simulator.takeAction(action);
      // take snapshot of current state of simulator			
      State state2 = simulator.getState().copy();	 
      double r = this.diffRF.getReward(state,action,state2);
      UCTStateNode child = node.getChildNode(action, state2,depth+1);
      // calculate Q via recursion
      double q = r + gamma * plan(state2, child, depth + 1);
      
      // update counts
      node.sCount++;
      int sa_count = ++node.saCounts[action];

      /**
       * dQ = 
       *     sum over trajectories t
       *         sum over samples (s_t,a_t,s_{t+1})
       *            \gamma^t dR(s_t,a_t,s_{t+1})
       */

      //get dR from reward function object
      double[] gr = this.diffRF.getGradR(state, action, state2);
      for(int i=0;i<numRewardFeatures;i++) gradR[depth][i] = gr[i];
      //dQ = dR + gamma * dQ
      for(int i = 0; i < gradQtmp.length; ++i) gradQtmp[i] *= gamma;
      for(int i=0;i<numRewardFeatures;i++) gradQtmp[i] += gradR[depth][i];			

      //update rolling averages of Q
      double alpha = 1.0/sa_count;
      node.Q[action] += (q - node.Q[action]) * alpha;

      if (depth == 0) {//done with trajectory
        //calculate sample of dQ for this trajectory and update rolling average
        for (int i = 0; i < numRewardFeatures; ++i)
          gradQ[action][i] += alpha * (gradQtmp[i] - gradQ[action][i]);            	
      }
      //return for this trajectory
      return q;
    }
  }

  //======================================================================
  //Differentiable Function Interface

  @Override
  public OutputAndGradient2D evaluate(Object state) {
	  return evaluate((State)state);
  }  
  @Override
  public int numParams() {
	  return this.diffRF.numParams();
  }
  @Override
  public void setParams(double[] theta) {
	  this.diffRF.setParams(theta);
  }
  @Override
  public double[] getParams() {
	  return this.diffRF.getParams();
  }
  @Override
  public Object generateRandomInput(Random rand) {
	  SASTriple rt = (SASTriple)diffRF.generateRandomInput(rand);
	  return rt.state2;
  }
}
