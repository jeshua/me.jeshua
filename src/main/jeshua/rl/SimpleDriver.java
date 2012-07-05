package jeshua.rl;

public class SimpleDriver {
	public Simulator sim;
	public Agent ag;
	public State curr_state;
	public State last_state;
	public int last_action;
	
	public SimpleDriver(Simulator sim, Agent ag){
		this.sim = sim;
		this.ag = ag;
		this.curr_state = null;
		this.last_state = null;
		this.last_action = -1;
	}
	
	public void step(){
		curr_state = sim.getState();
		double reward = sim.getReward();
		int action = last_action;
		if(last_state == null)
			action = ag.step(curr_state);
		else
			action = ag.step(last_state,action,curr_state,reward);
		
		if(!curr_state.isAbsorbing()){
			sim.takeAction(action);
			last_state = curr_state;
			last_action = action;
		} else{
			sim.initEpisode();
			last_state = null;
			last_action = -1;
		}	
	}
}
