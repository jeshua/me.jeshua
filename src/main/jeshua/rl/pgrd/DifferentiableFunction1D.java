package jeshua.rl.pgrd;

import java.util.Random;

public interface DifferentiableFunction1D extends ParameterizedFunction{
	public static class OutputAndGradient1D{
		public double   y;
		public double[] dy;
		public boolean logspace = false; //is gradient in logspace?
	}	
	public OutputAndGradient1D evaluate(Object input);
	public Object generateRandomInput(Random rand);
}
