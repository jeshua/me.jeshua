package me.jeshua.clustering
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
import Utils._


/**
 * Abstract KMeans interface.
 *
 * @param K - number of clusters
 * @param X - data as sequence of vectors
 * @param initialCentroids - if not set, will use random or kmeans++ to initialize
 * @param maxSteps - maximum number of EM steps
 *
 * call train() to initiate training
 *
 * @author Jeshua Bratman 
 * */
abstract class KMeans(
  var K: Int,
  val X: IndexedSeq[DenseVector[Double]],
  val initialCentroids: IndexedSeq[DenseVector[Double]] = null,
  val maxSteps: Int = Int.MaxValue) extends Serializable {
  val dims = X(0).length
  val num = X.length

  //==================================================
  //PUBLIC INTERFACE:

  /**
   * Return set of all centroids.
   * train() must be called first.
   * */
  def getCentroids : IndexedSeq[DenseVector[Double]]

  /**
   * Get the cluster for the ith training point
   * train() must be called first.
   * */
  def getCluster(i : Int) : Int
  
  /**
   * Get the cluster for a new point v. If v is part of the training set
   * use getCluster() instead.
   * train() must be called first.
   * */
  def findCluster(v : DenseVector[Double]) : Int = {
    val centroids = getCentroids
    val dists = centroids.map(c => l2Dist(v, c))
    whichMin(dists)
  }

  /**
   * Trains kmeans by running em() until converged or maxSteps has been exhausted
   * */
  def train() = {
    initializeCentroids()
    var numChanged = 0
    var steps = 0
    do {
      numChanged = em() //one step of em      
      steps += 1
    } while ((numChanged > 0 || steps < 3) && steps < maxSteps)
  }

  /**
   * Get feature vector for a given point
   * Either output feature with 1 in location of closest centroid, or normalized dist to all centroids
   * */
  def extractFeatures(v: DenseVector[Double], binary: Boolean = false): VectorCol[Double] = {    
    if (binary) {
      val closest = findCluster(v)
      val ret = SparseVector.zeros[Double](K)
      ret(closest) = 1d
      ret
    } else {
      val centroids = getCentroids
      val dists = centroids.map(c => l2Dist(v, c))
      val denom = dists.sum
      new DenseVectorCol(dists.map(d => d / denom).toArray)
    }
  }

  /**
   * Initializes the centroid vectors.
   * */
  def initializeCentroids(): Unit

  /**
   * Runs one step of EM (however that is implemented) and returns number
   * of pointes that changed clusters
   * */
  def em(): Int

}
