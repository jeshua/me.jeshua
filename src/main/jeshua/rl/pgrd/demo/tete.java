package jeshua.rl.pgrd.demo;

import jeshua.rl.pgrd.SoftmaxPolicy;
import jeshua.rl.pgrd.ValidateGradient;
import jeshua.rl.uct.demo.DemoSim;
import jeshua.rl.uct.demo.Maze;

public class tete {

  /**
   * @param args
   */
  public static void main(String[] args) {
    int[][] maze_data = 
        new int[][]{
            {0,0}            
    };
    
    DemoSim.maze = new Maze(maze_data);
    DemoQFunction qf = new DemoQFunction();
    ValidateGradient.validate(qf);    
    SoftmaxPolicy policy = new SoftmaxPolicy(qf, 1);
    ValidateGradient.validate(policy);    
  }

}
