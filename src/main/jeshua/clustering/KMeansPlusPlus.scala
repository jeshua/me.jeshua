package jeshua.clustering
/*
 *Copyright (C) Jeshua Bratman 2012
 *              (jeshuabratman@gmail.com) 
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy
 *of this software to use in any way you see fit. I only ask you reference me
 *as the original author if you found this code useful.
 */

import scalala.tensor.dense._
import scalala.tensor.sparse.SparseVector
import scalala.tensor._
import collection.mutable.{ ArraySeq, ArrayBuffer }
import Utils._

/**
 * KMeans++ learning algorithm
 *
 * This is the standard kmeans learning algorithm with the so-called power initialization.
 * See Arthur, David and Vassilvitskii, Sergei. k-means++: The Advantages of Careful Seeding. 2006.
 * 
 * Somewhat slower than naive implementation (because the initialization step is more work)
 * but almost always produces better final results.
 * 
 * @author Jeshua Bratman
 */
class KMeansPlusPlus(
  K_ : Int,
  X_ : IndexedSeq[DenseVector[Double]],
  initialCentroids : IndexedSeq[DenseVector[Double]] = null,
  maxSteps : Int = Int.MaxValue) extends KMeans(K_, X_, initialCentroids, maxSteps) with HasRNG {

  var labels: IndexedSeq[Int] = null
  var centroids :  ArrayBuffer[DenseVector[Double]]= null
  def getCentroids = centroids 
  def getCluster(x : Int) : Int = labels(x)//get cluster for one of the points
 

  protected def getDistsToClosestCentroids() : IndexedSeq[Double] = {
    for (x <- 0 until num) yield {
      val alld = centroids map (j => l2Dist(X(x), j)) //dists to all centers
      alld(whichMin(alld))
    }
  }
  protected def appendCentroid(c : DenseVector[Double]) = {
    centroids.append(c)
  }

  //find initial clusters using power initialization
  // e.g. Arthur, David and Vassilvitskii, Sergei (2006) k-means++: 
  //The Advantages of Careful Seeding. Technical Report. Stanford.
  override def initializeCentroids() = {
    this.centroids = new ArrayBuffer[DenseVector[Double]]
    if(initialCentroids != null){
      for(c <- initialCentroids)
        appendCentroid(c)
    } else {
      appendCentroid(X(rng.nextInt(num)).toDense)
      //keep choosing centroids until we have K of them, or data is perfectly represented
      var denom = 1d
      while (centroids.length < K && denom != 0) {
        //find distance of all data points to their closest center
        this.centroids = centroids        
        val dists = getDistsToClosestCentroids()
        //choose new centroid with prob proportional to distance from closest centroid
        val distSquared = dists.map(n => math.pow(n, 2))
        denom = distSquared.sum
        if (denom == 0) {
          K = centroids.length
        } else {
          val probs = distSquared.map(n => n / denom)
          val newCentroid = sampleMultinomial(probs,rng)
          appendCentroid(X(newCentroid).toDense)
        }
      }//end loop over k
      this.centroids = centroids
    }
  }

  // find closest centroid for each sample
  def expectation(centroids: IndexedSeq[DenseVector[Double]]): IndexedSeq[Int] = {
    val m = for (i <- 0 until num) yield {
      if (centroids.length <= 1) 0
      else {
        val dists = centroids map (j => l2Dist(X(i), j))
        whichMin(dists)
      }
    }
    m
  }

  // find new centroids by taking mean of each cluster
  def maximization(labels: IndexedSeq[Int]) = {
    for(i <- 0 until centroids.length)
      centroids(i) = DenseVector.zeros[Double](dims)
    //calculate cluster sums sizes
    val clusterCount = new ArraySeq[Int](K)
    for (i <- 0 until num) {
      centroids(labels(i)) :+= X(i)
      clusterCount(labels(i)) += 1
    }
    //divide by cluster size
    for (i <- 0 until K) if (clusterCount(i) > 0) centroids(i) :/= clusterCount(i)
  }

  //one step of expectation-maximization
  //returns number of changed points  
  def em(): Int = {
    if (labels == null) labels = expectation(centroids)
    val oldLabels = for (i <- labels) yield i
    maximization(labels)
    labels = expectation(centroids)
    //return number changed
    labels.zip(oldLabels).foldLeft(0)((i, j) => if (j._1 != j._2) i + 1 else i)
  }
}
