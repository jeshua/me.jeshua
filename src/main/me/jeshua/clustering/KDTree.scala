package me.jeshua.clustering
import scalala.tensor._
import scalala.tensor.dense.DenseVector
import scalala.tensor.dense.DenseMatrix
import scala.collection.mutable.ArrayBuffer

class KDTree(val points: IndexedSeq[DenseVector[Double]], val maxDepth : Int = Int.MaxValue) {
  import KDTree._
  val dims = points(0).length
  def numNodes = totalNodes
  private var totalNodes = 1;

  val root = createNode(points, 0)
  def createNode(points: IndexedSeq[DenseVector[Double]], depth: Int): Node = {
    this.totalNodes += 1
    //BASE CASES:
    if(points.length <= 0)null
    else if (points.length == 1) {
      new LeafNode(totalNodes,Seq(points(0)).toIndexedSeq)
    } else if(depth > maxDepth){
      new LeafNode(totalNodes,points)
    } 
    //SPLIT
      else {
        //choose splitting axis based on depth
        val axis = depth % dims
        //sort by axis dimension
        def sort(a: DenseVector[Double], b: DenseVector[Double]): Boolean = a(axis) < b(axis)
        val sortedPoints = points.sortWith(sort _)

        //recurse left and right of axis center
        val medianIdx = sortedPoints.length / 2
        val leftPoints = for(i <- 0 until (medianIdx)) yield sortedPoints(i)
        val leftNode = createNode(leftPoints, depth + 1)
        val rightPoints = for(i <- (medianIdx) until sortedPoints.length) yield sortedPoints(i)
        val rightNode = createNode(rightPoints, depth + 1)
        
        val median = sortedPoints(medianIdx)
        val sum = leftNode.getSum + rightNode.getSum

        //get bounding box
        val min = DenseVector.zeros[Double](dims)
        val max = DenseVector.zeros[Double](dims)
        for(i <- 0 until dims){
          val vec = for(p <- points) yield p(i)
          min(i) = vec.min
          max(i) = vec.max
        }        

        val newNode =  new InnerNode(
          totalNodes,
          axis,median(axis),
          min,median,max,sum,
          points.length,
          leftNode,rightNode)

        //set children's parents
        leftNode.parent = newNode
        rightNode.parent = newNode
        newNode //return
      }
  }//end createNode
  
  //traverse on a datapoint to find leaf node it belongs to
  def findLeaf(point : DenseVector[Double], node : Node = root) : Node = {
    node match{
      case n : InnerNode =>{
        if(point(n.splitDim) < n.splitVal) findLeaf(point,n.left)
        else findLeaf(point,n.right)
      }
      case n : LeafNode => n
      case x => null
    }
  }  
}

//======================================================================


object KDTree {
  def distance(v1: DenseVector[Double], v2: DenseVector[Double]): Double = Utils.l2Dist(v1, v2)

  //node classes:
  abstract class Node(val id : Int){
    def getSum() : DenseVector[Double]
    var parent : Node = null    
  }

  case class LeafNode(_id : Int, val points: IndexedSeq[DenseVector[Double]]) extends Node(_id) {
    val dim = points(0).length
    val sum = points.foldLeft(DenseVector.zeros[Double](dim))(_ :+ _)
    def getSum = sum
  }

  case class InnerNode(
    _id : Int,
    val splitDim: Int,               //dimension along which we split
    val splitVal: Double,            //value at which we split
    val min: DenseVector[Double],    //min along splitDim
    val median: DenseVector[Double], //median along splitDim
    val max: DenseVector[Double],    //max along splitDim
    val sum: DenseVector[Double],    
    val numPoints: Int,
    val left: Node,
    val right: Node) extends Node(_id) {
    val rect = new Rectangle(min, max)
    def getSum = sum
  }

  //defines an N-Dimensional rectangle
  class Rectangle(
    val hMin: DenseVector[Double],
    val hMax: DenseVector[Double]) {
    lazy val width = hMax - hMin
    //check if point is within rectangle
    def contains(v: DenseVector[Double]): Boolean = {
      var i = 0
      var ret: Boolean = true
      while(i < v.length && ret){
        ret = (v(i) >= hMin(i) && v(i) <= hMax(i))
          i+=1
      }
      ret
    }
    //find closest vector to v on the border of h
    def clip(v: DenseVector[Double]): DenseVector[Double] = {
      var ret = DenseVector.zeros[Double](v.length)
      for (i <- 0 until v.length) ret(i) = 
        if (v(i) < hMin(i)) hMin(i)
        else if (v(i) > hMax(i)) hMax(i)
        else v(i)      
      ret
    }
    //find closest vector to x within h
    def closest(x: DenseVector[Double]): DenseVector[Double] = {
      if (contains(x)) x
      else clip(x)
    }
    //distance between point and rectangle
    def dist(x: DenseVector[Double]): Double = {
      if (contains(x)) 0d
      else distance(x, clip(x))
    }
    //check if one point dominates another point for this rectangle    
    var p = DenseVector.zeros[Double](hMin.length)
    def dominates(x1: DenseVector[Double], x2: DenseVector[Double]) = {
      if(p.length != x1.length) p = DenseVector.zeros[Double](x1.length)
      for (i <- 0 until x1.length)
        p(i) = if (x2(i) > x1(i)) hMax(i) else hMin(i)
      if (distance(p, x1) < distance(p, x2)) true
      else false
    }
  }
}


