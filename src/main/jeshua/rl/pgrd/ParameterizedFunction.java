package jeshua.rl.pgrd;

public interface ParameterizedFunction {
	public int numParams();
	public void setParams(double[] theta);
	public double[] getParams();
}
