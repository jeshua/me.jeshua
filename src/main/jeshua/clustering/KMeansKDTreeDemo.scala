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


//Visualize KD Tree KMEANS
object KMeansKDTreeDemo {
  import KDTree._

  def main(args: Array[String]) {
    import javax.swing._
    import java.awt._

    val num = 10000
    val numClusters = 4

    val data = new ArraySeq[DenseVector[Double]](num)
    var minX = 0d
    var minY = 0d
    var maxX = 0d
    var maxY = 0d
    val rng = new scala.util.Random(10)
    val means = for (i <- 0 until 6) yield (rng.nextInt(10).toDouble, rng.nextInt(10).toDouble)
      for (i <- 0 until num) {
        val d = rng.nextInt(means.length)
        data(i) = DenseVector.zeros[Double](2)
        val mean: (Double, Double) = means(d)
        val variance = 1.1
        data(i)(0) = rng.nextGaussian() * variance + mean._1
        data(i)(1) = rng.nextGaussian() * variance + mean._2
        if (data(i)(0) < minX) minX = data(i)(0)
        if (data(i)(0) > maxX) maxX = data(i)(0)
        if (data(i)(1) < minY) minY = data(i)(1)
        if (data(i)(1) > maxY) maxY = data(i)(1)
      }

    val xWidth = math.abs(maxX - minX)
    val yWidth = math.abs(maxY - minY)


    val initialCentroids =  
      for (i <- 0 until numClusters)
      yield data(rng.nextInt(data.length))
    val kmeans = new KMeansKDTree(numClusters, data, initialCentroids,1)
    kmeans.initializeCentroids()
    class DrawPanel() extends JPanel {
      this.setDoubleBuffered(true)

      val colorsTrans = Seq(new Color(1f, 0f, 0f, .1f),
                            new Color(0f, 1f, 0f, .1f),
                            new Color(0f, 0f, 1f, .1f),
                            new Color(1f, 1f, 0f, .1f),
                            new Color(1f, 0f, 1f, .1f),
                            new Color(0f, 1f, 1f, .1f))
      val colors = Seq(new Color(1f, 0f, 0f, 1f),
                       new Color(0f, 1f, 0f, 1f),
                       new Color(0f, 0f, 1f, 1f),
                       new Color(1f, 1f, 0f, 1f),
                       new Color(1f, 0f, 1f, 1f),
                       new Color(0f, 1f, 1f, 1f))

      override def paint(graphics: Graphics): Unit = {
        val g2d = graphics.asInstanceOf[Graphics2D]

        val w = getSize().width
        val h = getSize().height
        g2d.setColor(new Color(.6f,.6f,.6f));
        g2d.fillRect(0, 0, w, h)
        //convert to image coords
        def convertX(x: Double) = (((x) / (xWidth)) * w * .8 + w * .15).toInt
        def convertY(y: Double) = (((maxY-y) / (yWidth)) * h * .8 + h * .15).toInt
        def convertCoord(xy: (Double, Double)): (Int, Int) = (convertX(xy._1), convertY(xy._2));

        val pointW = math.max(1, w / 100)
        val centroidW = math.max(1, w / 60)

        for (i <- 0 until num) {
          val c = kmeans.getCluster(data(i))
          if (c != -1)
            g2d.setColor(colorsTrans(c % colorsTrans.length));
          else
            g2d.setColor(new Color(0f, 0f, 0f, .1f));
          val (xp, yp) = convertCoord(data(i)(0), data(i)(1))
          g2d.fillOval(xp - pointW / 2, yp - pointW / 2, pointW, pointW)
        }

        //draw bounding boxes		    

        def drawNode(node: Node) {
          node match {
            case n: InnerNode => {
              val (minx, miny) = convertCoord(n.min(0), n.min(1))
              val (maxx, maxy) = convertCoord(n.max(0), n.max(1))
              val (medx, medy) = convertCoord(n.median(0), n.median(1))
              val owner = if(n.id < kmeans.nodeOwners.length) kmeans.nodeOwners(n.id)
                          else -1

              g2d.setColor(new Color(0f, 0f, 0f, .1f));
              //max/min y are switched so y=0 is on the bottom
              g2d.drawRect(minx, maxy, maxx - minx, miny - maxy)
              if (owner != -1) {
                val c = colorsTrans(owner % colorsTrans.length)
                g2d.setColor(c)
                //max/min y are switched so y=0 is on the bottom
                g2d.fillRect(minx, maxy, maxx - minx, miny - maxy)
              } else {
                drawNode(n.left)
                drawNode(n.right)
              }       
            } case n: LeafNode => {
              val owner = kmeans.getNodeOwner(n)
              if(owner != -1){
                g2d.setColor(colors(owner % colors.length));
                for (p <- n.points) {
                  val (xp, yp) = convertCoord(p(0), p(1))
                  g2d.fillOval(xp - pointW / 2, yp - pointW / 2, pointW, pointW)
                }
              }
            }
          }
        }
        if (kmeans.kd != null)
          drawNode(kmeans.kd.root)
        //draw centroid
        val centroids = kmeans.getCentroids
        for (i <- 0 until centroids.length) {
          g2d.setColor(colors(i % colors.length))
          val (xp, yp) = convertCoord(
            centroids(i)(0),
            centroids(i)(1))
          g2d.fillOval(xp - centroidW / 2, yp - centroidW / 2, centroidW, centroidW)
        }
      }
    }

    val disp = new DrawPanel()
    val jf = new JFrame()
    jf.getContentPane().add(disp)
    disp.setSize(600, 600)
    jf.setSize(600, 600)
    jf.setVisible(true)
    for (i <- 0 until 8) {
     kmeans.em()
     disp.repaint()
     Thread.sleep(500)
    }
  }
}


