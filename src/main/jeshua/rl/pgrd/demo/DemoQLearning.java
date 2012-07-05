package jeshua.rl.pgrd.demo;

import java.util.Arrays;
import java.util.Random;

import jeshua.rl.State;
import jeshua.rl.uct.demo.DemoSim;
import jeshua.rl.uct.demo.DemoState;

public class DemoQLearning {
    private double alpha;        //learning rate
    private double gamma;        //discount factor
    public double[][] Q;
    private int num_actions;
    private int num_states;
    private int previous_action;
    private int previous_state;
    private double epsilon;
    private Random rand;
    
	public DemoQLearning(double epsilon, double alpha, double gamma, Random rand){
		this.num_actions = DemoSim.num_actions;
		this.num_states = DemoSim.maze.width() * DemoSim.maze.height();
		this.Q = new double[num_actions][num_states];
		this.alpha = alpha;
		this.gamma = gamma;
		this.previous_action = -1;
		this.epsilon = epsilon;
		this.rand = rand;
		for(int a=0;a<num_actions;a++)
			Arrays.fill(Q[a],1d);
		this.initEpisode();
	}
	public void initEpisode (){	  
	  this.previous_action = -1;
	}
	
	public int step(State st, double reward){
		DemoState state = ((DemoState)st);
		int int_state = state.y * DemoSim.maze.width() + state.x;
		
		//choose action
		double max = Double.NEGATIVE_INFINITY;
		int max_a = 0;
		for(int i=0;i<num_actions;i++){
			if(max < Q[i][int_state]){
				max_a = i;
				max = Q[i][int_state];
			}
		}
		//update
		if(previous_action >=0){
			double oldQ = Q[previous_action][previous_state];
			double newQ = Q[max_a][int_state];
			Q[previous_action][previous_state] += alpha * (reward + gamma * newQ - oldQ);
		}
		if(this.rand.nextDouble() < this.epsilon)
			max_a = this.rand.nextInt(this.num_actions);		
		previous_action = max_a;
		previous_state = int_state;
		return max_a;
	}
}
