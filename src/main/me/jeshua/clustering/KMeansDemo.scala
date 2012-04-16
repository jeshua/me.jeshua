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
import collection.mutable.{ ArraySeq, ArrayBuffer }

/**
 * Runs the various kmeans algorithms and shows their resulting clusterings.
 *
 * Requires JFreeChart jar
 * 
 * @author Jeshua Bratman
 * */
object KMeansDemo {
  def visualize(km : KMeans) = {
    import scalala.library.Plotting._
    import scalala.library.plotting._
    import java.awt.Color
    //choose colors
    val colorList = Seq(
      new Color(1f,0f,0f,1f),
      new Color(0f,1f,0f,1f),
      new Color(0f,0f,1f,1f),
      new Color(1f,1f,0f,1f),
      new Color(1f,0f,1f,1f),
      new Color(0f,1f,1f,1f),
      new Color(1f,.6f,0f,1f),
      new Color(1f,0f,.5f,1f)
    )
    val colors: PartialFunction[Int, java.awt.Color] =
      { case i => colorList(km.getCluster(i) % colorList.length) }
    val centroidColors: PartialFunction[Int, java.awt.Color] =
      { case i => colorList(i % colorList.length) }
    //display as scatter plots    
    val x: DenseVector[Double] = new DenseVectorCol[Double](km.X.map(f => f(0)).toArray)
    val y: DenseVector[Double] = new DenseVectorCol[Double](km.X.map(f => f(1)).toArray)
    val s = DenseVector.ones[Double](x.length) :* ((x.max - x.min) * .01)
    //plot centroids
    val centroids = km.getCentroids
    val centroidX = new DenseVectorCol[Double](centroids.map(f => f(0)).toArray)
    val centroidY = new DenseVectorCol[Double](centroids.map(f => f(1)).toArray)
    plot.hold = true
    scatter(centroidX, centroidY, s :* 4, centroidColors)
    scatter(x, y, s, colors)
  }


  val rng = new scala.util.Random();

  def getSomeData(num : Int = 10000,numMeans : Int = 8) :  ArraySeq[DenseVector[Double]] = {
    val means = for (i <- 0 until numMeans) yield (rng.nextInt(10).toDouble, rng.nextInt(10).toDouble)
    val vars = for (i <- 0 until numMeans) yield (rng.nextDouble()+.2, rng.nextDouble()+.2)
        val data = new ArraySeq[DenseVector[Double]](num)
    for (i <- 0 until num) {
      val dx = rng.nextInt(numMeans)
      val x = rng.nextGaussian() * vars(dx)._1 + means(dx)._1
      val y = rng.nextGaussian() * vars(dx)._2 + means(dx)._2
      data(i) = DenseVector(x, y)
    }
    data
  }


  def main(args: Array[String]) = {
    val numCentroids = 8
    val data = getSomeData()

    val initialCentroids =  
      for (i <- 0 until numCentroids)
      yield data(rng.nextInt(data.length))

    import scalala.library.Plotting._
    import scalala.library.plotting._
    figure(1)
    title("Naive")
    val kmeans1 = new KMeansPlusPlus(numCentroids, data, initialCentroids)
    kmeans1.train()
    visualize(kmeans1)

    figure(2)
    title("Elkan")
    val kmeans2 = new KMeansElkan(numCentroids, data, initialCentroids)
    kmeans2.train()
    visualize(kmeans2)

    figure(3)
    title("KD-Tree")
    println("KD-Tree")
    val kmeans3 = new KMeansKDTree(numCentroids, data, initialCentroids)
    kmeans3.train()
    visualize(kmeans3)
  }
}

