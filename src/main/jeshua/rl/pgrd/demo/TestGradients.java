package jeshua.rl.pgrd.demo;

import static org.junit.Assert.*;

import java.util.Random;

import jeshua.rl.pgrd.RewardDifferentiableUCT;
import jeshua.rl.pgrd.SoftmaxPolicy;
import jeshua.rl.pgrd.ValidateGradient;
import jeshua.rl.uct.demo.DemoSim;

import org.junit.Test;

public class TestGradients {

	@Test
	public void testDemoRFunctionGradient() {
		DemoRewardFunction rf = new DemoRewardFunction();
		assertTrue(ValidateGradient.validate(rf));		
	}
	@Test
	public void testDemoQFunctionGradient() {
	  DemoQFunction qf = new DemoQFunction();
	  assertTrue(ValidateGradient.validate(qf));	    
	}
	@Test
	public void testSoftmaxGradient() {
	  DemoQFunction qf = new DemoQFunction();
	  SoftmaxPolicy policy = new SoftmaxPolicy(qf, 10);
	  assertTrue(ValidateGradient.validate(policy));      
	}
	
	@Test
	public void testUCTGradient() {
		Random rand = new Random(1432);		
		DemoSim sim = new DemoSim(rand);
		DemoRewardFunction rf = new DemoRewardFunction();
		int trajectories = 8000;	
		for(int depth = 1; depth<10; depth++){
			System.out.println("Depth: "+depth);
			double gamma = .95;	
			RewardDifferentiableUCT planner = 
					new RewardDifferentiableUCT(sim, rf, trajectories, depth, gamma, rand);
			planner.ucbScaler = 20;
			assertTrue(ValidateGradient.validate(planner,.005,rand));
		}
	}
	
	@Test
  public void testPGRDGradient() {
    Random rand = new Random(43243);   
    DemoSim sim = new DemoSim(rand);
    DemoRewardFunction rf = new DemoRewardFunction();
    int trajectories = 1000;   
    int depth = 1;
    double temperature = .02;
    double gamma = .95; 
    RewardDifferentiableUCT planner = 
        new RewardDifferentiableUCT(sim, rf, trajectories, depth, gamma, rand);
    SoftmaxPolicy policy = new SoftmaxPolicy(planner, temperature);
    assertTrue(ValidateGradient.validate(policy,0.001,rand));    
  }
}
