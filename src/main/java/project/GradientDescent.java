package project;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.Sphere;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class GradientDescent extends AbstractAnalysis {
    public static void main(String[] args) throws Exception {
        new GradientDescent();
    }
    
    public GradientDescent() {
    	try {
			AnalysisLauncher.open(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void init() {
        //function to display
        final Mapper mapper = new Mapper() {
            @Override
            public double f(double x, double y) {
                return x * Math.sin(x * y);
            }
        };
        
        //constants for program
        final float minBound = -3;
        final float maxBound = 3;  
        final double delta = 0.01; //precision for calculating derivatives
        final int stepDelay = 25;  //delay between each step in milliseconds 
        final int direction = 1;   //1 for finding minimum, -1 for finding maximum
        float sphereSize = 0.1f;

        

        //range and precision for the function
        Range range = new Range(minBound, maxBound);
        int steps = 80;

        //create the object to represent the function over the given range.
        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);
        
        Coord3d startPos = new Coord3d(-0.5, 0, mapper.f(0, 0));
        final Sphere currentPos = new Sphere(startPos, sphereSize, 15, Color.BLACK);
        currentPos.setWireframeDisplayed(false);

        //create the chart
        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        chart.getScene().getGraph().add(surface);
        chart.getScene().getGraph().add(currentPos);
        
        //animation thread
        Thread t = new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						sleep(stepDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Coord2d temp = currentPos.getPosition().getXY();
					double x = temp.getX();
					double y = temp.getY();
					double dx = mapper.f(x + delta, y) - mapper.f(x, y);
					double dy = mapper.f(x, y + delta) - mapper.f(x, y);
					double newx = x - dx * direction;
					double newy = y - dy * direction;
					newx = Math.max(newx, minBound);
					newx = Math.min(newx, maxBound);
					newy = Math.max(newy, minBound);
					newy = Math.min(newy, maxBound);
					currentPos.setPosition(new Coord3d(newx, newy, mapper.f(newx, newy)));
				}
			}
		};
		t.start();
    }

}
