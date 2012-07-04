package jeshua.rl.uct.demo;

import java.util.Random;

import jeshua.rl.State;
import jeshua.rl.uct.UCT;

/**
 * Runs UCT on simple episodic maze.
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

		int trajectories = 5000;		
		int depth = 50;
		UCT planner = new UCT(simPlan, trajectories, depth,
				simPlan.getDiscountFactor(), rand2);
		planner.ucbScaler = 1;
		State currState;		
		
		//VisualizeUCT.vis(planner);

		for (int timestep = 0; timestep < 200000; timestep++) {
			currState = simReal.getState();
			int a = planner.planAndAct(currState);
			System.out.print("Q: ");
			for(int i = 0; i<4;i++)
				System.out.printf("%.4f ",planner.getQ(i));
			System.out.println();
			simReal.takeAction(a);
			simReal.print();
			Thread.sleep(100);
		}

	}
}
