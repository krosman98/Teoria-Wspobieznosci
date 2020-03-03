import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Buffer{
    private int M;
    private int BufferSize;
    private int CurrentSize;

    public Buffer(int M){
        this.M = M;
        this.BufferSize = 2*M;
        this.CurrentSize = 0;
    }

    public int getM(){
        return this.M;
    }

    public int getBufferSize(){
        return this.BufferSize;
    }

    public int getCurrentSize(){
        return this.CurrentSize;
    }

    public void setCurrentSize(int newSize){
        this.CurrentSize = newSize;
    }

    public synchronized long take(int value){
        long start = System.nanoTime();
        while(getCurrentSize() < value){
            try{
                wait();
            }catch(InterruptedException e){}
        }
        setCurrentSize(getCurrentSize()-value);
        long result = System.nanoTime()-start;
        notifyAll();
        return result;
    }

    public synchronized long put(int value){
        long start = System.nanoTime();
        while(getCurrentSize() + value > getBufferSize()){
            try{
                wait();
            }catch(InterruptedException e){}
        }
        setCurrentSize(getCurrentSize()+value);
        long result = System.nanoTime()-start;
        notifyAll();
        return result;
    }
}

class Consumer extends Thread{
    private Buffer buffer;
    private boolean RandBalanced;
    private int P;
    private int K;

    public Consumer(Buffer buffer,boolean RandBalanced,int P,int K){
        this.buffer = buffer;
        this.RandBalanced = RandBalanced;
        this.P = P;
        this.K = K;
    }

    public void run(){
        Random random = new Random();
        int n;

        if (RandBalanced){
            n = random.nextInt(buffer.getM())+1;
        }
        else{
            if(random.nextInt(10) > 2){
                n = random.nextInt(buffer.getM()/100)+1;
            }
            else
                n = random.nextInt(buffer.getM())+1;
        }

        long elapsedTime = buffer.take(n);
        System.out.println(buffer.getM()+",K,"+n+","+P+"+"+K+",false,"+(RandBalanced?"Balanced":"NotBalanced")+","+elapsedTime);
    }
}

class Producer extends Thread{
    private Buffer buffer;
    private boolean RandBalanced;
    private int P;
    private int K;

    public Producer(Buffer buffer,boolean RandBalanced,int P,int K){
        this.buffer = buffer;
        this.RandBalanced = RandBalanced;
        this.P = P;
        this.K = K;
    }

    public void run(){
        Random random = new Random();
        int n;

        if (RandBalanced){
            n = random.nextInt(buffer.getM())+1;
        }
        else{
            if(random.nextInt(10) > 2){
                n = random.nextInt(buffer.getM()/100)+1;
            }
            else
                n = random.nextInt(buffer.getM())+1;
        }

        long elapsedTime = buffer.put(n);
        System.out.println(buffer.getM()+",P,"+n+","+P+"+"+K+",false,"+(RandBalanced?"Balanced":"NotBalanced")+","+elapsedTime);
    }

}
public class NaiveVPC {
    public static void main(String[] args){

        int M = 100000;
        int P,K;
        P = K = 1000;
        boolean RandBalanced = false;

        Buffer buffer = new Buffer(M);

        List<Thread> threads = new ArrayList<>();

        for(int i=0;i<P;i++){
            threads.add(new Producer(buffer,RandBalanced,P,K));
        }
        for(int i=0;i<K;i++){
            threads.add(new Consumer(buffer,RandBalanced,P,K));
        }
        for(Thread thread: threads){
            thread.start();
        }

    }
}
