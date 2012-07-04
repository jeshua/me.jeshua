package jeshua.rl.pgrd.demo;

import java.util.Random;

import jeshua.rl.pgrd.PGRD_UCT;
import jeshua.rl.pgrd.ValidateGradient;
import jeshua.rl.uct.demo.*;

/**
 * Runs PGRD UCT on simple maze.
 * @author Jeshua Bratman
 */
public class Demo {
	public static void main(String[] args) throws InterruptedException {
		
				
		// "real" world
		Random rand1 = new Random();
		int sz = 5;
		Maze maze = new Maze(Maze.randomMaze(sz, sz, rand1));   
		maze.setCell(sz-1, sz-1, Maze.G);
		DemoSim.maze = maze;
		
		DemoSim simReal = new DemoSim(rand1);
		
		// simulator for planning
		Random rand2 = new Random();
		DemoSim simPlan = new DemoSim(rand2);
		DemoRewardFunction rf = new DemoRewardFunction();
		
		//validate gradient
		ValidateGradient.validate(rf);
		
		
		int trajectories = 5000;		
		int depth = 20;
		double alpha = .001;
		double temperature = 10;
		double gamma = 1;
		PGRD_UCT pgrd = new PGRD_UCT(simPlan,rf,alpha,temperature,trajectories,depth,gamma,rand2);		
		DemoState currState;
		
		DemoVisualizePGRD p = new DemoVisualizePGRD(DemoSim.maze,pgrd);
		for (int timestep = 0; timestep < 200000; timestep++) {
			currState = (DemoState)simReal.getState();
			p.redraw(currState.x, currState.y);
			double reward = simReal.getReward();
			int a = pgrd.step(currState, reward);
			simReal.takeAction(a);
			//if(timestep > 10000)
			{
				simReal.print();			
				Thread.sleep(10);
			}
		}

	}
}
