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
 * Compares speed of three kmeans implementations on various sized synthetic datasets.
 *
 * @author Jeshua Bratman
 * */
object KMeansTests {
  val rng = new scala.util.Random();

  def makeData(num : Int, d : Int, numCenters : Int) :  ArraySeq[DenseVector[Double]] = {
    val means = for (i <- 0 until numCenters)
                yield {
                  val m = DenseVector.zeros[Double](d);
                  for(j <- 0 until d) m(j) = rng.nextDouble()*10;
                  m
                }
    val covs  = for (i <- 0 until numCenters)
                yield {
                  val m = DenseMatrix.zeros[Double](d,d);
                  for(j <- 0 until d) m(j,j) = rng.nextDouble();
                  m
                }

    val data = new ArraySeq[DenseVector[Double]](num)
    for (i <- 0 until num) {
      val dx = rng.nextInt(numCenters)
      data(i) = Utils.sampleGaussian(means(dx),covs(dx),rng)
    }
    data
  }

  def main(args: Array[String]) = {
    val ns = List(100, 1000, 5000, 10000);
    val ds = List(2, 5, 10);
    val maxSteps = 100;
    for(n <- ns)
      for(d <- ds){
        printf("%d points of dimension %d:\n",n,d)
        val numCentroids = n/100;
        val data = makeData(n,d,numCentroids)    

        var start = System.nanoTime()
        val kmeans1 = new KMeansPlusPlus(numCentroids, data,null,maxSteps)
        kmeans1.train()
        val time1 = System.nanoTime() - start
        printf("\tNaive: %.5f seconds\n",time1/1000000000f);

        start = System.nanoTime()
        val kmeans2 = new KMeansElkan(numCentroids, data,null,maxSteps)
        kmeans2.train()
        val time2 = System.nanoTime() - start
        printf("\tElkan: %.5f seconds\n",time2/1000000000f);

        start = System.nanoTime()
        val kmeans3 = new KMeansKDTree(numCentroids, data,null,maxSteps)
        kmeans3.train()
        val time3 = System.nanoTime() - start
        printf("\tKDTree: %.5f seconds\n",time3/1000000000f);
      }
  }
}

