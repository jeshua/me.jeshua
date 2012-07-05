package jeshua.rl.pgrd.demo;

import java.util.Random;

import jeshua.rl.State;
import jeshua.rl.pgrd.DifferentiableQFunction;
import jeshua.rl.uct.demo.*;

public class DemoQFunction implements DifferentiableQFunction {
	private int num_params;
	private int num_actions;
	private int num_states;
	private double[]theta;	
	private double[] Q;
	private double[][] dQ;
	
	public DemoQFunction(){		
		this.num_actions = DemoSim.num_actions;
		this.num_states = DemoSim.maze.width() * DemoSim.maze.height();
		this.num_params = num_states * num_actions;
		this.theta = new double[num_params];
		this.Q = new double[num_actions];
		this.dQ = new double[num_actions][num_params];
	}
	
	public double[] getQ(State s) {
		DemoState st = (DemoState)s;
		int t = st.y * DemoSim.maze.width() + st.x;		
		for(int a=0;a<num_actions;a++){
		  Q[a] = this.theta[a*this.num_states+t];
		}
		return Q;
	}
	
	public double[][] getGradQ(State s) {
	 DemoState st = (DemoState)s;
   int t = st.y * DemoSim.maze.width() + st.x; 
   
   for(int a=0;a<num_actions;a++)
     for(int i=0;i<num_states;i++){
       if(i == t)
         dQ[a][a*num_states+i] = 1;
       else
         dQ[a][a*num_states+i] = 0;
     }
   return dQ;
 }

	@Override
	public int numParams() {		
		return num_params;
	}

	@Override
	public void setParams(double[] theta) {
	  for(int a=0;a<theta.length;a++)
	      this.theta[a] = theta[a];
	}

	@Override
	public double[] getParams() {
	  return theta;
	}

	
	@Override
	public OutputAndGradient2D evaluate(State inp) {
		OutputAndGradient2D ret = new OutputAndGradient2D();
		ret.y = this.getQ(inp);
		ret.dy = this.getGradQ(inp);
		return ret;
	}
	
	@Override
	public Object generateRandomInput(Random rand){		
		return new DemoState(rand.nextInt(DemoSim.maze.width()),rand.nextInt(DemoSim.maze.height()));		
	}

  @Override
  public OutputAndGradient2D evaluate(Object input) {
    return evaluate((State)input);
  }

}
