package jeshua.rl.uct.demo;

import jeshua.rl.State;

/**
 * 2d coordinate state with hash and equals.
 * @author Jeshua Bratman
 */
public class DemoState extends State {
	public int x,y;
	boolean epEnd = false;
	
	public DemoState(int x, int y){this.x = x;this.y = y;}
	
	@Override
	public State copy() {
		DemoState n = new DemoState(this.x,this.y);
		n.epEnd = this.epEnd;
		return n;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DemoState other = (DemoState) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
	
	@Override
	public boolean isAbsorbing() {
		return epEnd;
	}
	
	public String toString(){
		return this.x+","+this.y;
	}
}
