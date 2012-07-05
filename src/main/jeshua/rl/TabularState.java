package jeshua.rl;

/**
 * State used for planning. Must have equality and hashcode functions as well as a copy function
 * so we can manipulate and compare them while planning.
 * @author Jeshua Bratman
 */
public abstract class TabularState extends State {
  protected int val;
  protected int num_states;
  
  public TabularState(int value, int num_states){
    this.val = value;
    this.num_states = num_states;
  }
  
  public int value(){return val;}
  public int numStates(){return num_states;} 
  
	public boolean equals(Object other){
	  if(this.value() == ((TabularState)other).value())
	    return true;
	  else
	    return false;
	}

	public int hashCode(){return this.value();}

	public abstract TabularState copy();
	public abstract boolean isAbsorbing();
}