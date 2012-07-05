package jeshua.rl.pgrd.demo;

import java.util.Random;

import jeshua.rl.State;
import jeshua.rl.pgrd.RewardFunction;
import jeshua.rl.uct.demo.*;

/**
 * Runs PGRD UCT on simple maze.
 * @author Jeshua Bratman
 */
public class DemoQ {
	public static void main(String[] args) throws InterruptedException {
		Random rand1 = new Random();
		int sz = 12;
		Maze maze = new Maze(Maze.randomMaze(sz, sz, rand1));   
		maze.setCell(sz-1, sz-1, Maze.G);
		DemoSim.maze = maze;
		
		DemoSim simReal = new DemoSim(rand1);
		
		// simulator for planning
		Random rand2 = new Random();
		double alpha = .01;
		double gamma = .9;
		double epsilon = .2;
		final DemoQLearning agent = new DemoQLearning(epsilon,alpha,gamma,rand2);
		DemoState currState;
		
		class qrf implements RewardFunction{
			public double getReward(State st1, int action, State st2){
				DemoState st = ((DemoState)st2);
				int int_st = st.y * DemoSim.maze.width() + st.x;
				double max = Double.NEGATIVE_INFINITY;
				for(int i=0;i<DemoSim.num_actions;i++)
					if(agent.Q[i][int_st] > max)
						max = agent.Q[i][int_st];
				return max;
								
			}
		}
				
		DemoVisualizeR p = new DemoVisualizeR(DemoSim.maze,new qrf());
		for (int timestep = 0; timestep < 2000000; timestep++) {
			System.out.println(timestep);
			currState = (DemoState)simReal.getState();			
			double reward = simReal.getReward();
			int a = agent.step(currState, reward);
			simReal.takeAction(a);
			//if(timestep > 10000)
			if((timestep % 30) == 0)
			{
				p.redraw(currState.x, currState.y);
				//simReal.print();			
				//Thread.sleep(102);
			}
		}

	}
}
