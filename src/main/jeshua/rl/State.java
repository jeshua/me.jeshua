package jeshua.rl;

/**
 * State used for planning. Must have equality and hashcode functions as well as a copy function
 * so we can manipulate and compare them while planning.
 * @author Jeshua Bratman
 */
public abstract class State {
	@Override
	public abstract boolean equals(Object other);

	@Override
	public abstract int hashCode();

	public abstract State copy();

	public abstract boolean isAbsorbing();
}