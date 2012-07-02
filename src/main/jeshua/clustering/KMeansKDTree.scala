package jeshua.clustering
/*
 *Copyright (C) Jeshua Bratman 2012
 *              (jeshuabratman@gmail.com) 
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy
 *of this software to use in any way you see fit. I only ask you reference me
 *as the original author if you found this code useful.
 */

import scalala.tensor._
import scalala.tensor.dense.DenseVector
import util.control.Breaks._
import scala.collection.mutable.ArraySeq
import scalala.tensor.dense.DenseMatrix
import scala.collection.mutable.ArrayBuffer
import Utils._


/**
 * KD Tree based Kmeans 
 * See: Dan Pelleg and Andrew W. Moore. Accelerating exact k-means algorithms with geometric reasoning. 1999
 * This is the 'blacklisting' algorithm
 *
 * Faster than kmeans++ and elkan if number of data points is large, but dimensionality
 * is small -- this is because KD-Trees lose benefit in high dimensional spaces. There are other
 * tree sructures that work better such as ball trees.
 * 
 * @author Jeshua Bratman
 */

class KMeansKDTree(
  K_ : Int, 
  data_ : IndexedSeq[DenseVector[Double]],
  initialCentroids_ : IndexedSeq[DenseVector[Double]] = null,
  maxSteps_ : Int = Int.MaxValue
) extends KMeans(K_,data_,initialCentroids_,maxSteps_) with HasRNG
{
  import KDTree._
  var kd: KDTree = new KDTree(X)  

  //centroids need to be more than just vectors
  class Centroid(val v: DenseVector[Double]) {
    var count = 0
    var mass = DenseVector.zeros[Double](v.length)
  }
  var cntrds : ArraySeq[Centroid] = null

  //access actual centroid vectors indirectly
  def getCentroids : IndexedSeq[DenseVector[Double]] = for(i <- cntrds) yield i.v
  
  //assigned owner cluster
  val numNodes = kd.numNodes+1
  val nodeOwners = new ArraySeq[Int](numNodes)

  //recursively get owner of a node
  def getNodeOwner(node: Node): Int = {
    if (node.id < numNodes && nodeOwners(node.id) != -1) nodeOwners(node.id)
    else if (node.parent == null) -1
    else getNodeOwner(node.parent)
  }
  //get the cluster owner for a point
  //O(log(N)) where N is number of data points in tree
  def getCluster(x : Int): Int = getCluster(X(x))
  def getCluster(p : DenseVector[Double]): Int = {
    val node = kd.findLeaf(p)
    if (node == null) -1
    else getNodeOwner(node)
  }
  
  
  //==================================================
  
  //Random initialization of centroids
  //TODO!! Replace with power initialization (kmeans++)
  def initializeCentroids() = { 
    cntrds = new ArraySeq[Centroid](K) 
    if(initialCentroids == null)
      for (i <- 0 until K)
	cntrds(i) = new Centroid(X(rng.nextInt(X.length)).toDense)
    else
      for (i <- 0 until initialCentroids.length)
	cntrds(i) = new Centroid(initialCentroids(i).toDense)
  }

  
  //==================================================
  
  
  val clusterCounts = new ArraySeq[Int](K)
  def em(): Int = {
    //save previous cluster assignments    
    val countsLast = for (i <- clusterCounts) yield i
    for (i <- 0 until numNodes) nodeOwners(i) = -1
    for (i <- 0 until K) clusterCounts(i) = 0
    //one step of em
    em(kd.root)
    //update centroids
    for (i <- 0 until K) {
      for (j <- 0 until cntrds(i).v.length) {
        cntrds(i).v(j) = cntrds(i).mass(j) / cntrds(i).count
        cntrds(i).mass(j) = 0
      }
      cntrds(i).count = 0
    }
    //count changed cluster assignments    
    var numDiff = 0
    for (i <- 0 until K)
      if (countsLast(i) > clusterCounts(i))
        numDiff += countsLast(i) - clusterCounts(i)
    numDiff
  }

  /**
   * Recursive EM function:
   *  one step of em starting from node with set of centroids blacklisted as impossible 
   */
  def em(node: Node, blacklist_ : IndexedSeq[Boolean] = null): Unit = {
    val blacklist = for (i <- 0 until K)
                    yield (if (blacklist_ != null) blacklist_(i) else false);

    //----------
    node match {
      case n: LeafNode => {
        val dists = cntrds map (j => distance(n.points(0), j.v)) //TODO FIX THIS
        val m = whichMin(dists)
        cntrds(m).count += 1
        cntrds(m).mass += n.sum
        clusterCounts(m) += n.points.length
        nodeOwners(n.id) = m
      }
      //----------

      case n: InnerNode => {
        //calculate distance to each hyper-rectangle
        var lastDist = -1d
        var best = (0, Double.PositiveInfinity)
          var unique = true;
        var c = 0
        while (c < K && unique) {
          if (!blacklist(c)) {
            val dist = n.rect.dist(cntrds(c).v)
            if (dist == 0 && lastDist == 0) { unique = false; }
            else if (dist < best._2) { best = (c, dist) }
            lastDist = dist
          }
          c += 1
        } //done calculating distance from each centroid to each rectangle
        if (unique) {
          val newBlacklist = new ArraySeq[Boolean](K)
          var dominates = true
          for (c <- 0 until K) {
            if (c != best._1) {
              if (blacklist(c)) newBlacklist(c) = true
              else if (!n.rect.dominates(cntrds(best._1).v, cntrds(c).v)) {
                dominates = false;
                newBlacklist(c) = false
              } else {
                newBlacklist(c) = true
              }
            }
          }
          if (dominates) {
            cntrds(best._1).count += n.numPoints
            cntrds(best._1).mass += n.sum
            clusterCounts(best._1) += n.numPoints
            nodeOwners(n.id) = best._1
          } else {
            em(n.left, newBlacklist)
            em(n.right, newBlacklist)
          }
        } //not unique
        else { //otherwise recurse
            em(n.left, blacklist)
            em(n.right, blacklist)
        }
      }
    }
  }
}




