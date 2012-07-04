package jeshua.rl.uct;
import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Random;

import jeshua.rl.State;
import jeshua.rl.uct.UCT;
import jeshua.rl.uct.demo.DemoSim;
import jeshua.rl.uct.demo.DemoState;
import jeshua.rl.uct.demo.Maze;

public class TestUCT {
	final int W = Maze.W;
	final int N = Maze.N;
	final int G = Maze.G;
	final int[][] maze1 = 
		        new int[][]{
		            {0,0,0,0},
		            {N,N,N,0},
		            {0,N,N,N|G}		            
		            };
	Random rand = new Random();
	Maze maze = new Maze(maze1);
	int trajectories = 100000;		
    int depth = 20;
    double thresh = .01;


	
	@Test
	public void testUCT() {
		int stepsToComplete = 11;
		
		double gamma = .99; 
				
		DemoSim sim = new DemoSim(rand,maze);
		sim.slip_prob = 0;		
		
		UCT planner = new UCT(sim, trajectories, depth,gamma, rand);
		planner.ucbScaler = 1;	
				
		State st = new DemoState(0,0);
		stepsToComplete = 10;
		planner.plan(st);
		assertEquals(planner.getQ(2),Math.pow(gamma,stepsToComplete),thresh);
		st = new DemoState(0,2);
		stepsToComplete = 2;
		planner.plan(st);
		assertEquals(planner.getQ(2),Math.pow(gamma,stepsToComplete),thresh);	
		for(int i = 0; i<4;i++)
			System.out.printf("%.4f ",planner.getQ(i));
	}
}
