package jeshua.rl.pgrd.demo;

import java.util.Random;

import jeshua.rl.pgrd.OLGARB_Agent;
import jeshua.rl.pgrd.PGRD_UCT;
import jeshua.rl.pgrd.ValidateGradient;
import jeshua.rl.uct.demo.*;

/**
 * Runs PGRD UCT on simple maze.
 * @author Jeshua Bratman
 */
public class DemoOLGARB {
	public static void main(String[] args) throws InterruptedException {
		
				
		// "real" world
		Random rand1 = new Random();
		int sz = 2;
		Maze maze = new Maze(Maze.randomMaze(sz, sz, rand1));   
		maze.setCell(sz-1, sz-1, Maze.G);
		DemoSim.maze = maze;
		
		DemoSim simReal = new DemoSim(rand1);
		
		// simulator for planning
		Random rand2 = new Random();
		DemoQFunction qf = new DemoQFunction();
		
		double alpha = .01;
		double temperature = .02;
		double gamma = 1;
		OLGARB_Agent agent = new OLGARB_Agent(qf,alpha,temperature,gamma,rand2);		
		DemoState currState;
		
		DemoVisualizeOG p = new DemoVisualizeOG(DemoSim.maze,agent);
		for (int timestep = 0; timestep < 200000; timestep++) {
			currState = (DemoState)simReal.getState();
			p.redraw(currState.x, currState.y);
			double reward = simReal.getReward();
			int a = agent.step(currState, reward);
			simReal.takeAction(a);
			//if(timestep > 10000)
			{
				//simReal.print();			
				Thread.sleep(10);
			}
		}

	}
}
