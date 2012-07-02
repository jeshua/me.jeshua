package jeshua.clustering
import scalala.tensor._
import scalala.tensor.dense.DenseVector
import scala.collection.mutable.ArraySeq
import scalala.tensor.dense.DenseMatrix
import scala.collection.mutable.ArrayBuffer
import java.awt._
import KDTree._

object KDTreeDemo{
  def main(args : Array[String]) {
    import javax.swing._
    val num = 1000
    val data = new ArraySeq[DenseVector[Double]](num)
    var minX = 0d
    var minY = 0d
    var maxX = 0d
    var maxY = 0d
    val rng = new util.Random()
    for(i <- 0 until num){
      val d = rng.nextDouble
      data(i) = DenseVector.zeros[Double](2)
      val means : (Double,Double) = if(d < .2) (1,1) else if(d < .7) (2,3) else (3,1)
        val variance = .4
      data(i)(0) = rng.nextGaussian()*variance + means._1
      data(i)(1) = rng.nextGaussian()*variance + means._2
      if(data(i)(0) < minX) minX = data(i)(0)
      if(data(i)(0) > maxX) maxX = data(i)(0)
      if(data(i)(1) < minY) minY = data(i)(1)
      if(data(i)(1) > maxY) maxY = data(i)(1)
    }
    val xWidth = math.abs(maxX - minX)
    val yWidth = math.abs(maxY - minY)
    var kd : KDTree = null
    
    
    class DrawPanel() extends JPanel {
      this.setDoubleBuffered(true)
      val colors = Seq(Color.red,Color.green,Color.blue,Color.orange)
      override def paint(graphics: Graphics): Unit = {
        val g2d = graphics.asInstanceOf[Graphics2D]
        val w = getSize().width
        val h = getSize().height
        //convert to image coords
        def convertX(x : Double) = (((maxX-x) / (xWidth))*w).toInt
        def convertY(y : Double) = (((maxY-y) / (yWidth))*h).toInt
        def convertCoord(xy : (Double,Double)) : (Int,Int) = (convertX(xy._1),convertY(xy._2))
	  
	  val pointW = math.max(1,w/100)
        val centroidW = math.max(1,w/60)
        g2d.setColor(Color.black)
        for(i <- 0 until num){
	  val (xp,yp) = convertCoord(data(i)(0),data(i)(1))		    	 
	  g2d.fillOval(xp-pointW/2,yp-pointW/2,pointW,pointW)		    	 
        }		    
        //draw bounding boxes
        def drawNode(node : Node){		      
	  node match{
	    case n : InnerNode =>{
	      val (minx,miny) = convertCoord(n.min(0),n.min(1))
	      val (maxx,maxy) = convertCoord(n.max(0),n.max(1))
	      val (medx,medy) = convertCoord(n.median(0),n.median(1))
	      
	      g2d.setColor(Color.black)
	      g2d.drawLine(minx,miny,minx,maxy)
	      g2d.drawLine(minx,maxy,maxx,maxy)
	      g2d.drawLine(maxx,maxy,maxx,miny)
	      g2d.drawLine(maxx,miny,minx,miny)
	      g2d.setColor(Color.red)
	      g2d.drawOval(medx-centroidW/2,medy-centroidW/2,centroidW,centroidW)
	      if(n.splitDim == 0){
	        val x1 = convertX(n.median(0))
	        val x2 = convertX(n.median(0))
	        val y1 = convertY(n.min(1))
	        val y2 = convertY(n.max(1))
	        g2d.drawLine(x1,y1,x2,y2)
	      } else {
	        val x1 = convertX(n.min(0))
	        val x2 = convertX(n.max(0))
	        val y1 = convertY(n.median(1))
	        val y2 = convertY(n.median(1))
	        g2d.drawLine(x1,y1,x2,y2)
	      }
	      
	      drawNode(n.left)
	      drawNode(n.right)
	    }case x =>
	  }
        }
        if(kd != null)
	  drawNode(kd.root)
      }
    }  
    
    val disp = new DrawPanel()
    val jf = new JFrame()
    jf.getContentPane().add(disp)
    disp.setSize(600,600)
    jf.setSize(600,600)
    jf.setVisible(true)    
    
    for(i <- 1 until 20){
      kd = new KDTree(data,i)
      disp.repaint()
      Thread.sleep(1000)
    }
  }
}

