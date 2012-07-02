package jeshua.clustering
/*
 *Copyright (C) Jeshua Bratman 2012
 *              (jeshuabratman@gmail.com) 
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy
 *of this software to use in any way you see fit. I only ask you reference me
 *as the original author if you found this code useful.
 */
import scalala.tensor.dense.DenseVector
import scalala.tensor.dense.DenseVectorCol
import scalala.tensor.dense.DenseMatrix

trait HasRNG{val rng = scala.util.Random;}

/**
 * Useful collection of functions.
 *
 * @author Jeshua Bratman
 * */
object Utils{

  // find index corresponding to maximum element
  def whichMax(m : Iterable[Double], invert : Boolean = false) : Int = {
    def cmp(a : Double, b : Double) = if(invert) a < b else a > b
    val start = if(invert) Double.PositiveInfinity else Double.NegativeInfinity
    (m.foldLeft((0,0,start))((t,v) => 
      if(cmp(t._3,v)) (t._1+1,t._2,t._3) else {(t._1+1,t._1,v)})
   )._2
  }
  def whichMin(m : Iterable[Double]) : Int = whichMax(m,true)

  // l1 distance between two vectors
  def l1Dist(v1 : DenseVector[Double], v2 : DenseVector[Double]) : Double = 
    ((v1 - v2) map math.abs).sum  

  // l2 distance between two vectors
  def l2Dist(v1 : DenseVector[Double], v2 : DenseVector[Double]) : Double = {
    var sum = 0d
    for(i <- 0 until v1.length) sum += math.pow(v1(i)-v2(i),2)
    math.sqrt(sum)
  }

  // returns a sample from a multivariate Gaussian with mean mu and covariance matrix sigma
  def sampleGaussian(mu : DenseVector[Double], 
                     sigma : DenseMatrix[Double],
                     rng : scala.util.Random) 
  : DenseVector[Double] = {
    val d = mu.length
    val A = scalala.library.LinearAlgebra.cholesky(sigma) //d x d  
    val z = new DenseVectorCol[Double]((for(j <- 0 until d) yield rng.nextGaussian()).toArray) //d x 1
    mu.asCol + A * z
  }

  // returns sample from multinomial described by probs
  def sampleMultinomial(probs : Seq[Double], rng : scala.util.Random) : Int = {
    val dart = rng.nextDouble
    // probs might be long, need tail recursion here
    @annotation.tailrec
    def sample(sum : Double, n : Int) : Int = {
      if(n >= probs.length) throw new IllegalArgumentException("Probs don't sum to 1!")
      else if(sum >= dart) n
      else if(n+1 >= probs.length) {
        throw new IllegalArgumentException("Probs don't sum to 1!")
      }
      else sample(sum+probs(n+1),n+1)
    }
    sample(probs(0),0)
  }
}

