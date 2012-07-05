package jeshua.rl.uct.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
public class MazeGFX 
{   
    public static Dimension draw(Graphics2D g2d, Dimension size, Maze maze, int agx, int agy,
    		double[][] overlay) 
    {
        int width = maze.width();
        int height = maze.height();
        int cell_width = (size.width - 1) / width;
        int cell_height = (size.height - 1) / height;
        int box_size = Math.min(cell_width, cell_height);        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Serif", Font.PLAIN, 35);
            
        Color floor_color,wall_color, grid_color;
        floor_color = new Color(255,255,255);
        wall_color = new Color(0,0,0);
        Color black = new Color(0,0,0);
        grid_color = new Color(180,180,180);
        
        Stroke grid_stroke = new BasicStroke(5, 
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER, 
                10.0f);
        g2d.setFont(font);           
        
        Stroke wall_stroke = new BasicStroke(10, 
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER, 
                10.0f);
      
        for(int x=0; x<width;x++)
        {
            for(int y=0; y<height;y++)
            {
            	if(overlay!=null){
            		int v = (int)(255*overlay[x][y]);
            		floor_color = new Color(255,255-v,255-v);
            	}else{
            		floor_color = new Color(255,255,255);
            	}
            	
                g2d.setColor(floor_color);
                Rectangle2D square = new Rectangle2D.Double(x*box_size, y*box_size, box_size, box_size);   
                g2d.fill(square);                
            }
        }
        g2d.setStroke(grid_stroke);
        for(int x=0; x<width;x++)
            for(int y=0; y<height;y++)
            {
                g2d.setColor(grid_color);
                g2d.drawRect(x*box_size, y*box_size, box_size, box_size);
            }
        
        for(int x=0; x<width;x++)
            for(int y=0; y<height;y++)
                {
                    g2d.setColor(wall_color);
                    g2d.setStroke(wall_stroke);
                    if(!maze.legalMove(x,y, 0))
                        g2d.drawLine(x*box_size, y*box_size,
                                (x+1)*box_size, y*box_size);
                    if(!maze.legalMove(x,y, 1))
                        g2d.drawLine(x*box_size, (y+1)*box_size, 
                                (x+1)*box_size, (y+1)*box_size);
                    if(!maze.legalMove(x,y, 2) )
                        g2d.drawLine((x+1)*box_size, (y)*box_size, 
                                (x+1)*box_size, (y+1)*box_size);
                    if(!maze.legalMove(x,y, 3))
                        g2d.drawLine((x)*box_size, (y)*box_size,
                                (x)*box_size, (y+1)*box_size);
                }
        
       
        //DRAW AGENT
        g2d.setColor(new Color(255,255,255));
        g2d.fillOval(
                (int)(agx*box_size+.1*box_size),
                (int)(agy*box_size+.1*box_size),
                (int)(box_size-.2*box_size), 
                (int)( box_size-.2*box_size));
        
        g2d.setColor(new Color(0,0,0));
      
        
        centerText("A",null,g2d, black,agx*box_size,agy*box_size,box_size,box_size);
        
        return new Dimension(width * box_size, height * box_size);
    }
    // Utility method to center text in a rectangle
    @SuppressWarnings("deprecation")
    protected static void centerText(String s1, String s2, Graphics2D g, Color c,
                              int x, int y, int w, int h)
    {
      Font f = g.getFont();
      FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(f);
      int ascent = fm.getAscent();
      int height = fm.getHeight();
      int width1=0, width2 = 0, x0=0, x1=0, y0=0, y1=0;
      width1 = fm.stringWidth(s1);
      if (s2 != null) width2 = fm.stringWidth(s2);
      x0 = x + (w - width1)/2;
      x1 = x + (w - width2)/2;
      if (s2 == null)
        y0 = y + (h - height)/2 + ascent;
      else {
        y0 = y + (h - (int)(height * 2.2))/2 + ascent;
        y1 = y0 + (int)(height * 1.2);
      }
      g.setColor(c);
      g.drawString(s1, x0, y0);
      if (s2 != null) g.drawString(s2, x1, y1);
    }
}
