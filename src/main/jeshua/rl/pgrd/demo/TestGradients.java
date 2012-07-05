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
		Random rand = new Random(63403);		
		DemoSim sim = new DemoSim(rand);
		DemoRewardFunction rf = new DemoRewardFunction();
		int trajectories = 20000;		
		int depth = 5;
		double gamma = 1;	
		RewardDifferentiableUCT planner = 
				new RewardDifferentiableUCT(sim, rf, trajectories, depth, gamma, rand);
		assertTrue(ValidateGradient.validate(planner,rand));		
	}
	
	@Test
  public void testPGRDGradient() {
    Random rand = new Random(493);   
    DemoSim sim = new DemoSim(rand);
    DemoRewardFunction rf = new DemoRewardFunction();
    int trajectories = 20000;   
    int depth = 5;
    double temperature = .01;
    double gamma = 1; 
    RewardDifferentiableUCT planner = 
        new RewardDifferentiableUCT(sim, rf, trajectories, depth, gamma, rand);
    SoftmaxPolicy policy = new SoftmaxPolicy(planner, temperature);
    assertTrue(ValidateGradient.validate(policy,rand));    
  }
}
