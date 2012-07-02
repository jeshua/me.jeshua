package jeshua.rl.uct.demo;
import java.util.Random;

import jeshua.rl.Simulator;
import jeshua.rl.State;


/**
 * Simulates a simple episodic maze domain.
 * @author Jeshua Bratman
 */
public class DemoSim implements Simulator
{  
	DemoState state;	
    private final double slip_prob = .1;//10% chance of slipping
    public int numActions = 4;
    //maze description
    private final int W = Maze.W;
    private final int N = Maze.N;
    private final int G = Maze.G;
    private final int[][] maze_data = 
        new int[][]{
            {0,0,0,0,W,0,0,W,0,0,0,0},
            {0,N|W,N,0,0,W|N,N,W|N,N,N,W,0},
            {0,W,0,0,0,W,0,W,G,0,0,0},
            {0,0,0,N,0,W,0,0,N,N,0,0},
            {0,0,0,W,0,N,N,N,0,0,W,0},
            {0,W,0,W,0,0,0,0,0,0,0,0}            
    };
    
    private Maze maze;  

    private Random random;
    
    // CONSTRUCTOR and INITIALIZATION
    public DemoSim(Random rand){
    	this(rand,null);
    }
    public DemoSim(Random rand, Maze maze)
    {	
    	if(maze == null)
    		this.maze = new Maze(maze_data);
    	else
    		this.maze = maze;
        this.random = rand;        
        this.state = new DemoState(this.maze.startX(),this.maze.startY());
    }
    
    public void initEpisode()
    {        
        state.x = maze.startX();
        state.y = maze.startY();
        state.epEnd = false;
    }    
  
    // ACTING
    public void takeAction(int a)
    {
    	if(state.epEnd){
    		this.initEpisode();
    	} else{
    	
        if(random.nextDouble() < slip_prob)
             a = random.nextInt(getNumActions());
        
        //move the agent
        switch(a)
        {
        case 0: //North
            if(maze.legalMove(state.x,state.y,0))
                state.y--;
            break;
        case 1: //South
            if(maze.legalMove(state.x,state.y,1))
                state.y++;
            break;
        case 2: //East
            if(maze.legalMove(state.x,state.y,2))
                state.x++;
            break;
        case 3: //West
            if(maze.legalMove(state.x,state.y,3))
                state.x--;
            break;
        }        
        if(maze.isGoal(state.x, state.y)){
        	state.epEnd = true;
        }
    	}
    }
    
    @Override
    public double getReward(){
    	if(maze.isGoal(state.x,state.y)){ 
    		return 1;
    	}
    	else
    		return 0;    	
    }    
    
    @Override
    public int getNumActions() {        
        return numActions;
    }

    @Override
    public double getDiscountFactor() {
        return 1;
    }

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.state = (DemoState)state;		
	}
	
	public void print(){
		maze.print(state.x,state.y);
	}
}