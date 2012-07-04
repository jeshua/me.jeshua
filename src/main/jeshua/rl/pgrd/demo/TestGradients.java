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
	public void testDemoRewardFunctionGradient() {
		DemoRewardFunction rf = new DemoRewardFunction();
		assertTrue(ValidateGradient.validate(rf));
		
	}
	@Test
	public void testSoftmaxGradient() {
		Random rand = new Random();		
		DemoSim sim = new DemoSim(rand);
		DemoRewardFunction rf = new DemoRewardFunction();
		int trajectories = 100000;		
		int depth = 3;
		double temperature = 100;
		double gamma = 1;	
		RewardDifferentiableUCT planner = 
				new RewardDifferentiableUCT(sim, rf, trajectories, depth, gamma, rand);
		SoftmaxPolicy policy = new SoftmaxPolicy(planner, temperature);
		assertTrue(ValidateGradient.validate(policy));		
	}
	@Test
	public void testUCTGradient() {
		Random rand = new Random();		
		DemoSim sim = new DemoSim(rand);
		DemoRewardFunction rf = new DemoRewardFunction();
		int trajectories = 100000;		
		int depth = 3;
		double gamma = 1;	
		RewardDifferentiableUCT planner = 
				new RewardDifferentiableUCT(sim, rf, trajectories, depth, gamma, rand);
		planner.ucbScaler = 10000;
		assertTrue(ValidateGradient.validate(planner));		
	}
}
