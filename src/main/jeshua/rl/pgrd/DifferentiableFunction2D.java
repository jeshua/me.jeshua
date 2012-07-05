package jeshua.rl.pgrd;
import java.util.Random;

public interface DifferentiableFunction2D extends ParameterizedFunction{	
	public static class OutputAndGradient2D{
	  //y is the output of this vector-valued function
		public double[]   y;
		//dy is jacobian or log jacobian of this function
		public double[][] dy; //dy[i][j] = dy[i]/dtheta[j]
		public boolean logspace = false; //is gradient in logspace?
	}
	public OutputAndGradient2D evaluate(Object input);
	public Object generateRandomInput(Random rand);
}
