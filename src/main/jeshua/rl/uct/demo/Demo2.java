package jeshua.rl.uct.demo;
import java.util.Random;

import jeshua.rl.uct.UCT;
import jeshua.rl.uct.VisualizeUCT;

/**
 * Runs one step of UCT in tiny problem and displays planning tree.
 * @author Jeshua Bratman
  */
public class Demo2 {
	public static void main(String[] args) throws InterruptedException {

        Maze maze = new Maze(new int[][]{{0},{0},{0},{0}});
        maze.setCell(0,3, Maze.G);//put goal in bottom right        
		
		// "real" world
		Random rand1 = new Random();
		DemoSim.maze = maze;
		DemoSim simReal = new DemoSim(rand1);
		DemoSim.num_actions = 2;
		
		// simulator for planning
		Random rand2 = new Random();
		DemoSim simPlan = new DemoSim(rand2);
		DemoSim.num_actions = 2;

		int trajectories = 200;
		int depth = 4;
		UCT planner = new UCT(simPlan, trajectories, depth,
				simPlan.getDiscountFactor(), rand2);
		planner.ucbScaler = 1;
		planner.planAndAct(simReal.getState());		
		VisualizeUCT.vis(planner);
	}
}
