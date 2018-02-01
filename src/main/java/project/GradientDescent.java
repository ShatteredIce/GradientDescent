//Gradient Descent using Jzy3d graphing library - Created on 1/25/18 by Nathan Purwosumarto 

package project;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.bridge.newt.controllers.keyboard.NewtToAWTKeyListener;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.maths.Scale;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.Sphere;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class GradientDescent extends AbstractAnalysis implements KeyListener{
    
	//parameters for program
    float minBound = -3;
    float maxBound = 3;
    float minZ = 0;
    float maxZ = 5;
    double learningRate = 1;
    double delta = 0.0005; //precision for calculating derivatives
    int stepDelay = 1;  //delay between each step in milliseconds 
    int direction = 1;   //1 for finding minimum, -1 for finding maximum
    int iterations = 100000;
    float sphereSize = 0.1f;
    
    Random rng = new Random();
    boolean launched = false;
    boolean running = false;
    int currentIteration = 0;
    Sphere currentPos;
    Mapper mapper;
    
    Component canvas;
    
    public GradientDescent() {
    	
    	try {
			AnalysisLauncher.open(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	canvas.requestFocusInWindow();
    	launched = true;
    }

    @Override
    public void init() {
        //function to display
        mapper = new Mapper() {
            @Override
            public double f(double x, double y) {
            	return Math.pow((Math.pow(x, 2) + Math.pow(y, 2)),0.5) + 0.25;
//            	return 5 * Math.sin(Math.abs(x/4)+Math.abs(y/4));
//              return x * Math.sin(x * y);
//            	return y * x * Math.exp((-Math.pow(x, 2) - Math.pow(y, 2))/8);
            }
        };
      
        //range and precision for the function
        Range range = new Range(minBound, maxBound);
        int steps = 80;
        double startx = minBound + rng.nextDouble() * (maxBound - minBound);
        double starty = minBound + rng.nextDouble() * (maxBound - minBound);

        //create the object to represent the function over the given range.
        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);
      
        Coord3d startPos = new Coord3d(startx, starty, mapper.f(startx, starty));
        currentPos = new Sphere(startPos, sphereSize, 15, Color.BLACK);
        currentPos.setWireframeDisplayed(false);

        //create the chart
        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        chart.getScene().getGraph().add(surface);
        chart.getScene().getGraph().add(currentPos);
        chart.getView().setSquared(false);
        chart.getCanvas().addKeyController(this);
//        chart.setScale(new Scale(minZ, maxZ));
        canvas = (Component) chart.getCanvas();
        
        
        //animation thread
        Thread t = new Thread(){
			@Override
			public void run() {
				while(currentIteration != iterations){
					try {
						sleep(stepDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(launched && !canvas.isShowing()) {
						System.exit(0);
					}
					else if(running) {
						Coord2d temp = currentPos.getPosition().getXY();
						double x = temp.getX();
						double y = temp.getY();
						double dx = mapper.f(x + delta, y) - mapper.f(x, y);
						double dy = mapper.f(x, y + delta) - mapper.f(x, y);
						double newx = x - learningRate * (dx * direction);
						double newy = y - learningRate * (dy * direction);
						newx = Math.max(newx, minBound);
						newx = Math.min(newx, maxBound);
						newy = Math.max(newy, minBound);
						newy = Math.min(newy, maxBound);
						currentPos.setPosition(new Coord3d(newx, newy, mapper.f(newx, newy)));
						currentIteration++;
					}
				}
			}
		};
		t.start();
    }
    
    public static void main(String[] args) throws Exception {
        new GradientDescent();
    }

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
            running = !running;
         }
		//print out current coordinate
		else if(e.getKeyCode() == KeyEvent.VK_P) {
			Coord2d temp = currentPos.getPosition().getXY();
			double finalX = Math.round(temp.getX()*1000)/1000d;
			double finalY = Math.round(temp.getY()*1000)/1000d;
			System.out.println("Current Position " + "- Iteration: " + currentIteration);
			System.out.println("X: " + finalX);
			System.out.println("Y: " + finalY);
			System.out.println("Z: " + Math.round(mapper.f(finalX, finalY)*1000)/1000d);
			System.out.println("------------------------------------");
		}
		//skip to max iterations
		else if(e.getKeyCode() == KeyEvent.VK_I) {
			stepDelay = 0;
		}
		//set point to new random location
		else if(e.getKeyCode() == KeyEvent.VK_R) {
			double randomx = minBound + rng.nextDouble() * (maxBound - minBound);
	        double randomy = minBound + rng.nextDouble() * (maxBound - minBound);
	        currentPos.setPosition(new Coord3d(randomx, randomy, mapper.f(randomx, randomy)));
	        currentIteration = 0;
	        running = false;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
