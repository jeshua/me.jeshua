package jeshua.rl.uct.demo;

import java.util.Random;

import jeshua.rl.State;
import jeshua.rl.uct.UCT;
import jeshua.rl.uct.UCT2;

/**
 * Simple episodic maze with 10% slipping (move in random direction).
 * @author Jeshua Bratman
 */
public class Demo {
	public static void main(String[] args) throws InterruptedException {
		
		// "real" world
		Random rand1 = new Random();
		int sz = 5;
		Maze maze = new Maze(Maze.randomMaze(sz, sz, rand1));
		maze.setCell(sz-1, sz-1, Maze.G);
		DemoSim simReal = new DemoSim(rand1,maze);
		
		// simulator for planning
		Random rand2 = new Random();
		DemoSim simPlan = new DemoSim(rand2,maze);

		int trajectories = 1000;		
		int depth = 20;
		UCT2 planner = new UCT2(simPlan, trajectories, depth,
				simPlan.getDiscountFactor(), rand2);
		planner.ucbScaler = 1;
		State currState;		
		
		//VisualizeUCT.vis(planner);

		for (int timestep = 0; timestep < 200000; timestep++) {
			currState = simReal.getState();
			int a = planner.plan(currState);
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
