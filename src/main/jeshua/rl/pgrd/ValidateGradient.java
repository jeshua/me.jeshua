package jeshua.rl.pgrd;

import java.util.Arrays;
import java.util.Random;

import jeshua.rl.pgrd.DifferentiableFunction1D.OutputAndGradient1D;
import jeshua.rl.pgrd.DifferentiableFunction2D.OutputAndGradient2D;

public class ValidateGradient {	
	public static boolean validate(DifferentiableFunction1D f){
		double delta = 0.01;
		double max_diff = 0.0001;
		int num_trials = 1000;		
		Random rand = new Random();
		boolean success = true;
		for(int trials=0;trials<num_trials;trials++){
			Object input = f.generateRandomInput(rand);
			double[] theta = f.getParams();
			double[] theta1 = f.getParams().clone();
			OutputAndGradient1D output = f.evaluate(input);

			//compute empirical gradient
			double[] dy_hat = new double[theta.length];
			Arrays.fill(dy_hat,0);
			for(int tind=0;tind<theta.length;tind++){
				//evaluate at theta[tind] += delta
				theta[tind] = theta1[tind] + delta;
				f.setParams(theta);
				double y1 = f.evaluate(input).y;
				//evaluate at theta[tind] -= delta
				theta[tind] = theta1[tind] - delta;
				f.setParams(theta);
				double y2 = f.evaluate(input).y;
				//check difference between errors
				dy_hat[tind] = (y1-y2)/(2*delta);
				//reset
				theta[tind] = theta1[tind];
			}
			//reset theta
			f.setParams(theta1);
			double diff = 0;
			for(int i=0;i<theta.length;i++){			
				diff += Math.abs(output.dy[i] - dy_hat[i]);
			}
			success = success & (diff < max_diff);

			//------
			//return results
			if(!success){
				System.out.print("dy   : ");
				for(int i=0;i<Math.min(theta.length,10);i++)System.out.printf("%.3f ",output.dy[i]);
				if(theta.length > 10)
					System.out.println("...");
				else
					System.out.println("");
				System.out.print("dy fd: ");
				for(int i=0;i<Math.min(theta.length,10);i++)System.out.printf("%.3f ",dy_hat[i]);
				if(theta.length > 10)
					System.out.println("...");
				else
					System.out.println();
				System.out.println("Difference: "+diff);
				break;
			}
		}
		return success;
	}

	public static boolean validate(DifferentiableFunction2D f){
		double delta = 0.01;
		double max_diff = 0.0001;
		int num_trials = 1000;		
		Random rand = new Random();
		boolean success = true;
		for(int trials=0;trials<num_trials;trials++){
			Object input = f.generateRandomInput(rand);
			double[] theta = f.getParams();
			double[] theta1 = f.getParams().clone();
			OutputAndGradient2D output = f.evaluate(input);

			//compute empirical gradient
			double[][] dy_hat = new double[output.dy.length][output.dy[0].length];
			for(int i=0;i<dy_hat.length;i++)
				Arrays.fill(dy_hat[i],0);
			
			for(int tind=0;tind<theta.length;tind++){
				//evaluate at theta[tind] += delta
				theta[tind] = theta1[tind] + delta;
				f.setParams(theta);
				double[] y1 = f.evaluate(input).y;
				//evaluate at theta[tind] -= delta
				theta[tind] = theta1[tind] - delta;
				f.setParams(theta);
				double[] y2 = f.evaluate(input).y;
				//check difference between errors

				for(int aind=0;aind<dy_hat.length;aind++){
					dy_hat[aind][tind] = (y1[aind]-y2[aind])/(2*delta);
				}
				//reset
				theta[tind] = theta1[tind];
			}
			//reset theta
			f.setParams(theta1);
			double diff = 0;
			for(int i=0;i<theta.length;i++){
				for(int aind=0;aind<dy_hat.length;aind++)			
					diff += Math.abs(output.dy[aind][i] - dy_hat[aind][i]);
			}
			success = success & (diff < max_diff);

			//------
			//return results
			if(!success){
				int max_show = 10;
				//----------------
				for(int q = 0; q<Math.min(2, dy_hat.length);q++){
					int num_shown = 0;

					for(int i=0;i<theta.length;i++){
						if(output.dy[0][i] != dy_hat[0][i]){
							num_shown++;
							System.out.printf("dy[%d]: %.3f ",i,output.dy[0][i]);
						}
						if(num_shown >= max_show) break;
					}
					if(num_shown >= max_show) System.out.println("...");
					else System.out.println("");	
				}
				System.out.println("..");
				//----------------
				for(int q = 0; q<Math.min(2, dy_hat.length);q++){
					int num_shown = 0;
					for(int i=0;i<theta.length;i++){
						if(output.dy[0][i] != dy_hat[0][i]){
							num_shown++;
							System.out.printf("dh[%d]: %.3f ",i,dy_hat[0][i]);
						}
						if(num_shown >= max_show) break;
					}
					if(num_shown >= max_show) System.out.println("...");
					else System.out.println("");
				}
				System.out.println("..");
				System.out.println("Difference: "+diff);
				break;
			}
			
		}
		return success;		
	}
}
