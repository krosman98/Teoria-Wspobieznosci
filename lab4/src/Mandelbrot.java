import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Mandelbrot extends JFrame {

    private final int MAX_ITER = 40000;
    private final double ZOOM = 150;
    private BufferedImage I;

    private int numberOfThreads;
    private int numberOfTasks;


    private int height = 600;
    private int width = 800;

    private int widthForTask;

    public Mandelbrot(int numberOfThreads) {

        super("Mandelbrot Set");

        setBounds(100, 100, width, height);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        this.numberOfThreads = numberOfThreads;
        this.numberOfTasks = 10* numberOfThreads;
        this.widthForTask = width /numberOfTasks;

        long start = System.nanoTime();
        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(this.numberOfThreads);
        List<Future<int[][]>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfTasks; i++)futures.add(executor.submit(task(i)));

        try {
            for (int i = 0; i < numberOfTasks; i++) {
                Future<int[][]> future = futures.get(i);
                int[][] subArray = future.get();
                for (int y=0;y<height;y++){
                    for (int x=0;x<widthForTask;x++){
                        I.setRGB(x+i*widthForTask, y,subArray[y][x]);
                    }
                }
            }
        } catch (InterruptedException Int) {
            Int.printStackTrace();
        } catch (ExecutionException Exec) {
            Exec.printStackTrace();
        }

        System.out.println("Number of threads = "+numberOfThreads+"\nTime elapsed = "+(System.nanoTime()-start)/1e9);
    }

    private Callable<int[][]> task(int partNumber){
        return new Callable<int[][]>() {
            @Override
            public int[][] call() throws Exception {
                double zx,zy,cX,cY,tmp;
                int[][] taskArray = new int[height][widthForTask];
                int startingX = partNumber*widthForTask;
                int endingX = (partNumber+1)*widthForTask;
                for(int y=0;y<height;y++){
                    for(int x=startingX;x<endingX;x++){
                        zx = zy = 0;
                        cX = (x-400)/ZOOM;
                        cY = (y-300)/ZOOM;
                        int iter = MAX_ITER;
                        while(zx*zx+zy*zy<4&& iter > 0){
                            tmp = zx * zx - zy * zy +cX;
                            zy = 2.0 *zx *zy +cY;
                            zx =tmp;
                            iter--;
                        }
                        taskArray[y][x-startingX] = iter|(iter<<8);
                    }
                }
                return taskArray;
            }
        };

    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(I,0,0,this);
    }

    public static void main(String[] args)
    {
        for(int i=1;i<21;i++){
            new Mandelbrot(i).setVisible(false);
        }
    }
}
