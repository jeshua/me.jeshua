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
import collection.mutable.{ ArraySeq, ArrayBuffer }


/**
 * Elkan's version of kmeans. See
 * See: Charles Elkan, Using the Triangle Inequality to Accelerate-Means 2003
 *
 * Faster than naive kmeans and kd tree especially when dimensionality of data
 * is very large.
 * 
 * @author Jeshua Bratman
 */
class KMeansElkan(
  K_ : Int,
  X_ : IndexedSeq[DenseVector[Double]],
  initialCentroids : IndexedSeq[DenseVector[Double]] = null,
  maxSteps : Int = Int.MaxValue) extends KMeansPlusPlus(K_, X_, initialCentroids, maxSteps) {

  def distance(a : DenseVector[Double], b : DenseVector[Double]) = Utils.l2Dist(a,b)

  val lowerBounds =  new Array[ArrayBuffer[Double]](X.length) //l(x,c)
  for(i <- 0 until X.length) lowerBounds(i) = new ArrayBuffer[Double]
  val upperBounds = new Array[Double](X.length) //u(x)
  val closest =  new Array[(Int,Double)](X.length) //(closest,u(x)) = (closest,d(x,closest))
  val cDists  = new ArrayBuffer[ArrayBuffer[Double]]//for each c,c pair
  val cMinDists =  new ArrayBuffer[Double](X.length)//for each c
  val outdated = new Array[Boolean](X.length) //for each x
  for(i <- 0 until X.length) outdated(i) = false

  override def getCluster(x : Int) = closest(x)._1

  //compute distance between centroids
  def getCDists() : IndexedSeq[IndexedSeq[Double]] = {
    for(i <- centroids)
      yield(
        for(j <- centroids) yield distance(i,j)
      )    
  }
  //compute 1/2 * mininum c distance
  def getCMinDists() : IndexedSeq[Double] = for(d <- cDists) yield 0.5 * d.min
  
  override protected def appendCentroid(newCentroid : DenseVector[Double]) = {
    //== update distance between centroids
    //new sequence with distances to all the old centroids
    val nl = new ArrayBuffer[Double]
    for(c <- centroids) nl.append(distance(newCentroid,c))
    nl.append(0d)//0 distance to itself
    //now extend each cdist with distance to new centroid
    for(i <- 0 until cDists.length) cDists(i).append(nl(i))
    //add the new row to cdist
    cDists.append(nl)

    //add the new centroid
    centroids.append(newCentroid)

    //== update minimum distances  
    cMinDists.append(0.5 * nl.min)

    //== update lower bounds
    for(i <- 0 until X.length) {
      lowerBounds(i).append(0)
      for(c <- 0 until centroids.length) {
        lowerBounds(i)(c) = 0
      }
    }

    //== update closest
    updateClosest()
  }

  def updateClosest() = {
    for(x <- 0 until X.length) 
      closest(x) = findCluster(x)
  }

  //find closest centroid for arbitrary point
  override def findCluster(v : DenseVector[Double]) : Int = {
    var closestDist = distance(v,centroids(0))
    var cx = 0
    for(i <- 1 until centroids.length){
      if(cDists(i)(cx) < 2*closestDist){
        val newDist = distance(v,centroids(i))
        if(newDist < closestDist){
          closestDist = newDist
          cx = i        
        }
      }
    }
    cx
  }


  //find closest for one of the training points
  def findCluster(x : Int)
  : (Int,Double) =  {
    var cx : Int = 0
    var closestDist = distance(X(x),centroids(0))
    lowerBounds(x)(0) = closestDist
    for(i <- 1 until centroids.length){
      //if distance between the two centroids larger than 2*closest, we can skip it
      if(cDists(i)(cx) < 2*closestDist){
        val newDist = distance(X(x),centroids(i))
        lowerBounds(x)(i) = newDist
        if(newDist < closestDist){
          closestDist = newDist
          cx = i
          upperBounds(x) = newDist
        }          
      }        
    }
    (cx,closestDist)
  }

  //compute distance between centroids
  def updateCDists()= {
    for(i <- 0 until centroids.length)
      for(j <- i until centroids.length){
        val d = distance(centroids(i),centroids(j))
        cDists(i)(j) = d
        cDists(j)(i) = d
      }
  }
  //compute 1/2 * miminum c distance
  def updateCMinDists() = 
    for(d <- 0 until cDists.length) cMinDists(d) =  0.5 * cDists(d).min
  

  def getXCDist(x : Int, c : Int) : Double = {
    val d = distance(X(x),centroids(c))
    lowerBounds(x)(c) = d
    if(c == closest(x)._1)
      upperBounds(x) = d
    d
  }

  //==================================================

  override protected def getDistsToClosestCentroids() : IndexedSeq[Double] = {
    for(x <- 0 until X.length) yield closest(x)._2
  }
  override def initializeCentroids() = {    
    super.initializeCentroids()
  }

  //==================================================
  var step = 0
  override def em() : Int = {    
    if(step > 0){
      updateCDists()
      updateCMinDists()
    }
    step += 1
    var numUpdated = 0
    for(x <- 0 until X.length){
      val cx = closest(x)._1
      if(upperBounds(x) > cMinDists(cx)){//don't bother updating these x's
        for(c <- 0 until centroids.length){
          if(c != cx && upperBounds(x) > lowerBounds(x)(c) && upperBounds(x) > 0.5 * cDists(cx)(c)){            
            //update if d(x,c(x)) is outdated
            if(outdated(x)) {
              closest(x) = (cx,getXCDist(x,cx))
              outdated(x) = false
            }else 
              closest(x) = (cx,upperBounds(x))            
            //do we still need to recalculate even after update
            if(closest(x)._2 > lowerBounds(x)(c) || 
               closest(x)._2 > 0.5*cDists(cx)(c)){
                 val d = getXCDist(x,c)
                 if(d < upperBounds(x)){
                   closest(x) = (c,d) //actually do the change
                   numUpdated += 1
                   upperBounds(x) = d
                 }
               }
          }
        }//end loop over centroids
      }
    }//end loop over points
    //update centroids
    val m = calcMeans() //c-length vector
    //distance between old centroids and new centroids
    val distChange =  for(i <- 0 until centroids.length) yield distance(centroids(i),m(i))
    for(x <- 0 until X.length){
      for(c <- 0 until centroids.length)
        lowerBounds(x)(c) = math.max(lowerBounds(x)(c) - distChange(c), 0d)
      upperBounds(x) = upperBounds(x) + distChange(closest(x)._1)
      outdated(x) = true
    }
    for(c <- 0 until centroids.length)
      centroids(c) = m(c)

    //return the number of updated points
    numUpdated
  } 


  def calcMeans() : ArraySeq[DenseVector[Double]] = {
    var means: ArraySeq[DenseVector[Double]] =
      ArraySeq((for (i <- 0 until centroids.length) yield DenseVector.zeros[Double](dims)):_*)
    //calculate cluster sums sizes
    val clusterCount = new ArraySeq[Int](centroids.length)
    for (x <- 0 until X.length) {
      means(closest(x)._1) :+= X(x)
      clusterCount(closest(x)._1) += 1
    }
    //divide by cluster size
    for (i <- 0 until centroids.length) if (clusterCount(i) > 0) means(i) :/= clusterCount(i)
    means
  }
}
