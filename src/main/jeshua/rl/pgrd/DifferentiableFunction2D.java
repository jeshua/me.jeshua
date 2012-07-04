package jeshua.rl.pgrd;
import java.util.Random;

public interface DifferentiableFunction2D extends ParameterizedFunction{	
	public static class OutputAndGradient2D{
		public double[]   y;
		public double[][] dy;
		public boolean logspace = false; //is gradient in logspace?
	}
	public OutputAndGradient2D evaluate(Object input);
	public Object generateRandomInput(Random rand);
}
