package jeshua.rl;

import java.util.Random;

public class Utils {
	public static int sampleMultinomial(double[] probs, Random random){
		double dart = random.nextDouble();
		double sum = 0;
		for(int i=0;i<probs.length;i++){
			sum += probs[i];
			if(dart < sum) return i;
		}
		throw new IllegalArgumentException("Probabilities do not sum to 1.");
	}
	
	
	/**
	 * Maximizer is used to find max and argmax
	 * of double values with tie breaking. 
	 * 
	 * @author Jeshua Bratman
	 */
	public static class Maximizer {
		//maximum equality set to keep around
		private static final int MAX_SIZE = 10000;
		//contains set of indices for equal values 
		private int[] data; 
		private int pointer;
		private double current_max;
		//comparison threshold
		private double thresh;
		//random numbers for tie breaking
		private Random random;

		public Maximizer(Random random) {
			this(random, 1e-4);
		}

		public Maximizer(Random random, double threshold) {
			this.random = random;
			current_max = Double.NEGATIVE_INFINITY;
			this.thresh = threshold;
			data = new int[MAX_SIZE];
			pointer = 0;
		}

		public void setEqualityThresh(double t) {
			thresh = t;
		}

		/**
		 * Add a value with associated index
		 * @param value  double value (used for comparison)
		 * @param index  associated index
		 */
		public void add(double value, int index) {
			double diff = Math.abs(value - current_max);
			if (Double.isNaN(value) || Double.isInfinite(value)) {
				throw new IllegalArgumentException(
						"ERROR in maximizer: added NaN value.");
			}
			if (diff < thresh) {
				data[pointer] = index;
				pointer++;
			} else if (value > current_max) {
				data[0] = index;
				pointer = 1;
				current_max = value;
			}
			if (pointer >= MAX_SIZE)
				throw new IllegalArgumentException(
						"ERROR in maximizer: pointer reached end of data.");
		}

		/**
		 * Get the index of the maximum value
		 * @return
		 */
		public int getMaxIndex() {
			if (pointer > 0)
				return data[random.nextInt(pointer)];
			else
				throw new IllegalArgumentException(
						"ERROR in maximizer: cannot get max, nothing added!");
		}

		/**
		 * Get the maximum value
		 * @return
		 */
		public double getMaxValue() {
			return current_max;
		}

		/**
		 * Clear out equality sets and max values.
		 */
		public void clear() {
			pointer = 0;
			current_max = Double.NEGATIVE_INFINITY;
		}

	}
}
