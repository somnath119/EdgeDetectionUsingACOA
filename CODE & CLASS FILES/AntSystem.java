
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.lang.*;
import java.awt.*;
import java.lang.Double.*;


public class AntSystem {
    // Algorithm parameters:
    // original amount of trail
    private double c = 1;
    // trail preference 5.5
    private double alpha = 5.5;
    // greedy preference
    private double beta = 7.5;
    // trail evaporation coefficient
    private double evaporation = 0.5;
    // new trail deposit coefficient;
    private double Q = 1000000.0;
    // number of ants used = numAntFactor*numTowns
    private double numAntFactor =0.01;
    // probability of pure random selection of the next town
    //private double pr = 0.01;

    // Reasonable number of iterations
    private int maxIterations = 100;

    public int n = 0;
    public static int width=0;
    public static int height=0; // # towns
    public int m = 0; // # ants
    private static double graph[][] = null;
    private double trails[][] = null;
    private double heuristic[][] = null;
    private Ant ants[] = null;
    private double probs[] = null;
    private int currentIndex = 0;
    public static BufferedImage img = null;
    public static File f = null;
    double vmax=0.0;
    public int[] bestTour;
    private Random rand = new Random();
    public double bestTourLength;
    public double Theresold=0.11;

    // Ant class. Maintains tour and tabu information ofr every ants
    private class Ant {
        public int tour[][] = new int[n][2];
        public boolean visited[][] = new boolean[width][height];

        public void visitPixel(int i,int j) {
            tour[currentIndex + 1][0] = i;
            tour[currentIndex + 1][1] = j;
            visited[i][j] = true;
        }

        public boolean visited(int i,int j) {
            return visited[i][j];
        }

        public double tourIntensity() {
            double intensity = 0.0;
            for (int i = 0; i < n-1; i++) {
				//System.out.println("I am in 10: i "+i +":"+ tour[i][0]+" "+tour[i][1] );
                intensity += calcIntensity(tour[i][0],tour[i][1]);
            }
            return intensity;
        }

        public void clear() {
            for (int i = 0; i < width; i++)
                for(int j=0; j<height; j++)
                visited[i][j] = false;
        }
    }

    public  double calcIntensity(int i,int j)
	{
	    double max=0;
		double intensity=0.0;
		//System.out.println("I am in calc intensity"+intensity);
		if(i+1<width && j+1<height && i-1>=0 && j-1 >=0 )
            intensity=(graph[i-1][j-1]-graph[i+1][j+1]);
        else
            intensity=0;

        if(intensity<0)
            intensity*=-1;
        if(intensity>max)
            max=intensity;
           // System.out.println("I am in calc intensity"+intensity+ " "+graph[i-1][j-1]+" "+graph[i+1][j+1] );
		if(i+1<width && i-1>=0)
            intensity= (graph[i-1][j]- graph[i+1][j]);
		else
            intensity=0;

        if(intensity<0)
            intensity*=-1;
        if(intensity>max)
            max=intensity;
          //  System.out.println("I am in calc intensity"+intensity);
		if(i+1<width && j+1<height && i-1>=0 && j-1 >=0 )
            intensity=(graph[i-1][j+1]-graph[i+1][j-1]);
        else
            intensity=0;

        if(intensity<0)
            intensity*=-1;
        if(intensity>max)
            max=intensity;
           // System.out.println("I am in calc intensity"+intensity);
		if(i+1<width && j+1<height && i-1>=0 && j-1 >=0 )
			intensity=(graph[i][j-1]-graph[i][j+1]) ;
        else
            intensity=0;

        if(intensity<0)
            intensity*=-1;
        if(intensity>max)
            max=intensity;
          //  System.out.println("I am in calc intensity"+intensity);
                return max;
	}
	public static int calcIntvalue(int red, int green, int blue)
	{
			int rgb = red;
			rgb = (rgb << 8) + green;
			rgb = (rgb << 8) + blue;
		return rgb;
	}

	public static double pow(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }

    // Read in graph from a file.
    // Allocates all memory.
    // Adds 1 to edge lengths to ensure no zero length edges.
    public void readGraph() throws IOException {
        try{
          f = new File("C:\\Users\\Somnath Paul\\Desktop\\Test\\code\\Input.jpg");
          img = ImageIO.read(f);
        }catch(IOException e){
          System.out.println(e);
        }

        //get image width and height
        width = img.getWidth();
        height = img.getHeight();
		graph = new double[width][height];


        System.out.println("Width= "+width+" Height= "+height );

        for(int i=0;i<width;i++){
       // System.out.print("----------------------I am in  "+i+"-----------------\n");
            for(int j=0;j<height;j++){
				Color c = new Color(img.getRGB(i, j));
				int a= calcIntvalue(c.getRed(),c.getGreen(),c.getBlue());
				graph[i][j]=a;
                /*System.out.print(" "+String.format("0x%08X", graph(i,j)));*/
            }
          //  System.out.println("");
        }




        vmax=0.0;


        for(int i=1;i<width-1;i++){
            for(int j=1;j<height-1;j++){
                double temp=calcIntensity(i,j);
                if(temp>vmax)
                    vmax=temp;
            }
        }

        if(vmax==0)
            vmax=vmax+0.1;

	System.out.println("Maximum Intensity "+vmax);

        n= 10000;
        // all memory allocations done here
        trails = new double[width][height];
        heuristic = new double[width][height];
        probs = new double[8];

      //  System.out.println("I am here 1");
        for(int i=1;i<width;i++){
            for(int j=1;j<height;j++){
                heuristic[i][j]=calcIntensity(i,j)/vmax;
                if(heuristic[i][j]<0)
                    heuristic[i][j]=0-heuristic[i][j];
            }
        }
       // System.out.println("I am here 3");
        m =(int)(width * height* numAntFactor);
       // System.out.println("number of ants"+m);
        ants = new Ant[m];
        for (int j = 0; j < m; j++)
            ants[j] = new Ant();
       // System.out.println("I am here 4");
    }

    // Store in probs array the probability of moving to each town
    private void probTo(Ant ant) {
        int i = ant.tour[currentIndex][0];
        int j = ant.tour[currentIndex][1];

        double denom = 0.0;
        /*need an updataion depending on result*/


            if(i-1<width && j-1 <height && i-1>=0 && j-1 >=0 ){
				//System.out.println("heuristic[i-1][j-1]="+heuristic[i-1][j-1] );

				Double x=pow(trails[i-1][j-1], alpha)* pow(heuristic[i-1][j-1], beta);
				if(!x.isNaN())
					denom+=x;
				else
					denom+=0.0001;

            }
            else
                denom +=0;
           // System.out.println("denom="+denom );
            if(i+1<width && j+1 <height && i+1>=0 && j+1 >=0 ){
                Double x= pow(trails[i+1][j+1], alpha)
                        * pow(heuristic[i+1][j+1], beta);
						if(!x.isNaN())
							denom+=x;
						else
					denom+=0.0001;
			}
            if(i<width && j+1 <height && i>=0 && j+1 >=0){
                Double x= pow(trails[i][j+1], alpha)
                        * pow(heuristic[i][j+1], beta);
				if(!x.isNaN())
					denom+=x;
				else
					denom+=0.0001;

			}

            if(i+1<width && j <height && i+1>=0 && j >=0){
                Double x= pow(trails[i+1][j], alpha)
                        * pow(heuristic[i+1][j], beta);
					if(!x.isNaN())
					denom+=x;
				else
					denom+=0.0001;
			}

            if(i-1<width && j+1 <height && i-1>=0 && j+1 >=0 ){
                Double x= pow(trails[i-1][j+1], alpha)
                        * pow(heuristic[i-1][j+1], beta);
				if(!x.isNaN())
					denom+=x;
					else
					denom+=0.0001;
			}

            if(i+1<width && j-1 <height && i+1>=0 && j-1 >=0){
               Double x= pow(trails[i+1][j-1], alpha)
                        * pow(heuristic[i+1][j-1], beta);
				if(!x.isNaN())
					denom+=x;
					else
					denom+=0.0001;

			}

            if(i<width && j-1 <height && i>=0 && j-1 >=0){
                Double x= pow(trails[i][j-1], alpha)
                        * pow(heuristic[i][j-1], beta);
				if(!x.isNaN())
					denom+=x;
				else
					denom+=0.0001;
			}

            if(i-1<width && j <height && i-1>=0 && j >=0){
                Double x= pow(trails[i-1][j], alpha)
                        * pow(heuristic[i-1][j], beta);
				if(!(x.isNaN()))
					denom+=x;
					else
					denom+=0.0001;
			}

  //  System.out.println("denom="+denom );

            if ( i-1>=width || j-1 >=height || i-1<0 || j-1<0 || ant.visited[i-1][j-1] ) {
                probs[0] = 0.0;
            } else {
                Double numerator = pow(trails[i-1][j-1], alpha)
                        * pow(heuristic[i-1][j-1], beta);
						if(numerator.isNaN())
							System.out.println(trails[i-1][j-1]+" "+heuristic[i-1][j-1]);
                probs[0] = numerator / denom;
            }

            if ( i+1>=width || j+1 >=height || i+1<0 || j+1<0  || ant.visited[i+1][j+1]) {
                probs[7] = 0.0;
            } else {
                double numerator = pow(trails[i+1][j+1], alpha)
                        * pow(heuristic[i+1][j+1], beta);
                probs[7] = numerator / denom;
            }

            if ( i>=width || j-1 >=height || i<0 || j-1<0 || ant.visited[i][j-1] ) {
                probs[1] = 0.0;
            } else {
                double numerator = pow(trails[i][j-1], alpha)
                        * pow(heuristic[i][j-1], beta);
                probs[1] = numerator / denom;
            }

            if ( i+1>=width || j-1 >=height || i+1<0 || j-1<0 || ant.visited[i+1][j-1]) {
                probs[2] = 0.0;
            } else {
                double numerator = pow(trails[i+1][j-1], alpha)
                        * pow(heuristic[i+1][j-1], beta);
                probs[2] = numerator / denom;
            }

            if ( i-1>=width || j >=height || i-1<0 || j<0 || ant.visited[i-1][j] ) {
                probs[3] = 0.0;
            } else {
                double numerator = pow(trails[i-1][j], alpha)
                        * pow(heuristic[i-1][j], beta);
                probs[3] = numerator / denom;
            }

            if ( i+1>=width || j >=height || i+1<0 || j<0 || ant.visited[i+1][j] ) {
                probs[4] = 0.0;
            } else {
                double numerator = pow(trails[i+1][j], alpha)
                        * pow(heuristic[i+1][j], beta);
                probs[4] = numerator / denom;
            }

            if ( i-1>=width || j+1 >=height || i-1<0 || j+1<0 || ant.visited[i-1][j+1]) {
                probs[5] = 0.0;
            } else {
                double numerator = pow(trails[i-1][j+1], alpha)
                        * pow(heuristic[i-1][j+1], beta);
                probs[5] = numerator / denom;
            }

            if ( i>=width || j+1 >=height || i<0 || j+1<0 || ant.visited[i][j+1]) {
                probs[6] = 0.0;
            } else {
                double numerator = pow(trails[i][j+1], alpha)
                        * pow(heuristic[i][j+1], beta);
                probs[6] = numerator / denom;
            }
		/*
		//To see probablity of every town
		System.out.println();
		for(int k=0;k<n;k++)
			System.out.print(probs[k]+" ");
		*/

    }

    // Given an ant select the next town based on the probabilities
    private void selectNextTown(Ant ant) {
        // calculate probabilities for each town (stored in probs)
        probTo(ant);
        int i=ant.tour[currentIndex][0];
        int j=ant.tour[currentIndex][1];
        //System.out.println("I am here 10");
        //select according to probs
		int next=-1;
		double max=0.0;
      /*  for(int a=0;a<8;a++)
            System.out.println(probs[a]);*/
       for (int k = 0; k < 8; k++) {
                //System.out.println("I am here 9 ");
            if (probs[k]>max){
                    next=k;
                    max=probs[k];
            }
        }

                if(next==0){
                    ant.visitPixel(i-1,j-1);
                    return;
                }
                else if(next==1){
                    ant.visitPixel(i,j-1);
                    return;
                }
                else if(next==2){
                    ant.visitPixel(i+1,j-1);
                    return;
                }
                else if(next==3){
                    ant.visitPixel(i-1,j);
                    return;
                }
                else if(next==4){
                    ant.visitPixel(i+1,j);
                    return;
                }
                else if(next==5){
                    ant.visitPixel(i-1,j+1);
                    return;
                }
                else if(next==6){
                    ant.visitPixel(i,j+1);
                    return;
                }
                else if(next==7){
                    ant.visitPixel(i+1,j+1);
                    return;
                }
                else{
                   // System.out.println("I am in selectnext, and something wrong");
                        ant.visitPixel(rand.nextInt(width),rand.nextInt(height));
                        return;
					//throw new RuntimeException("Not supposed to get here.");

                }



    }

    // Update trails based on ants tours
    private void updateTrails() {
        // evaporation
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                trails[i][j] *= evaporation;

        // each ants contribution
        for (Ant a : ants) {
            double contribution = Q / a.tourIntensity();
            for (int i = 0; i < n - 1; i++) {
                trails[a.tour[i][0]][a.tour[i][1]] += contribution;
            }
        }
    }

    // Choose the next town for all ants
    private void moveAnts() {
        // each ant follows trails...
        //System.out.println("I am here 7");
        //This loop will do width*height
        while (currentIndex < n-1) {
            for (Ant a : ants){
                selectNextTown(a);
                //System.out.println("I am here 8 : Current Index"+currentIndex);
            }
            currentIndex++;
            //System.out.println("I am here 9");
        }
    }

    private void setupAnts() {

        currentIndex = -1;
        for (int i = 0; i < m; i++) {
            ants[i].clear(); // faster than fresh allocations.
            ants[i].visitPixel(rand.nextInt(width),rand.nextInt(height));
           // System.out.println("I am here 6");
        }
        currentIndex++;

    }


    public void solve() {
        // clear trails
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++){
                trails[i][j] = c;
                //System.out.println("I am here 5");
            }


        int iteration = 0;
        // run for maxIterations
        // preserve best tour
       while (iteration < maxIterations) {
			System.out.println("----------------------Iteration "+iteration+" ----------------------");
            setupAnts();
            moveAnts();
            updateTrails();
			//Displaytourlength();



            iteration++;
			System.out.println();
        }
        DisplayPheromoneMat();
        //DisplayHeuristics();
        PrintImage();
        // Subtract n because we added one to edges on load
       // System.out.println("Best tour length: " + (bestTourLength - n));
       // System.out.println("Best tour:" + tourToString(bestTour));
      //  return bestTour.clone();
    }

    public void PrintImage()
    {
        int a=255;
        int r=0;
        int g=0;
        int b=0;

        int p = (a<<24) | (r<<16) | (g<<8) | b;

        r=g=b=255;
        int q = (a<<24) | (r<<16) | (g<<8) | b;
        for(int i=0; i<width; i++)
            for(int j=0; j<height; j++){
                if(trails[i][j]>Theresold)
                    img.setRGB(i,j,p);
                else
                    img.setRGB(i,j,q);
            }

    try{
      f = new File("C:\\Users\\Somnath Paul\\Desktop\\Test\\code\\Output.jpg");
      ImageIO.write(img, "jpg", f);
    }catch(IOException e){
      System.out.println(e);
    }

    }

	public void DisplayPheromoneMat()
	{
	System.out.println("Phreomone Matrix" );
		for(int i=0;i<width;i++){
        System.out.print("----------------------I am in  "+i+"-----------------\n");
			for(int j=0;j<height;j++){
				System.out.print(" "+ trails[i][j]);
			}
			System.out.println(" ");
		}
	}

	public void DisplayHeuristics()
	{
	 System.out.println("graph Matrix" );
		for(int i=0;i<width;i++){
        System.out.print("----------------------I am in  "+i+"-----------------\n");
			for(int j=0;j<height;j++){
				System.out.print(" "+ graph[i][j]);
			}
			System.out.println(" ");
		}

	System.out.println("Heuristics Matrix" );
		for(int i=0;i<width;i++){
        System.out.print("----------------------I am in  "+i+"-----------------\n");
			for(int j=0;j<height;j++){
				System.out.print(" "+ heuristic[i][j]);
			}
			System.out.println(" ");
		}
	}


    public static void main(String[] args) {
        // Load in TSP data file.
       /* if (args.length < 1) {
            System.err.println("Please specify a TSP data file.");
            return;
        }*/
        AntSystem anttsp = new AntSystem();
       try {
            anttsp.readGraph();
        } catch (IOException e) {
            System.err.println("Error reading graph.");
            return;
        }
            anttsp.solve();

    }
}
