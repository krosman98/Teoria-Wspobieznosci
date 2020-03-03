import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Buffer{
    private int M;
    private int BufferSize;
    private int CurrentSize;

    final Lock lock = new ReentrantLock();
    final Condition firstCons = lock.newCondition();
    final Condition firstProd = lock.newCondition();
    final Condition restCons = lock.newCondition();
    final Condition restProd = lock.newCondition();

    boolean firstConsFlag = true;
    boolean firstProdFlag = true;

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

    public long take(int value)throws InterruptedException{
        lock.lock();
        long start = System.nanoTime();
        try{
            while(!firstConsFlag)restCons.await();

            firstConsFlag = false;

            while(getCurrentSize() < value)firstCons.await();

            setCurrentSize(getCurrentSize()-value);

            long result = System.nanoTime()-start;
            firstConsFlag = true;

            restCons.signal();
            firstProd.signal();
            return result;
        }finally {
            lock.unlock();
        }

    }

    public long put(int value)throws InterruptedException{
        lock.lock();
        long start = System.nanoTime();
        try{
            while(!firstProdFlag)restProd.await();

            firstProdFlag = false;

            while(getCurrentSize() + value > getBufferSize())firstProd.await();

            setCurrentSize(getCurrentSize()+value);

            long result = System.nanoTime()-start;
            firstProdFlag = true;

            restProd.signal();
            firstCons.signal();

            return result;
        }finally {
            lock.unlock();
        }
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

        long elapsedTime = 0;
        try {
            elapsedTime = buffer.take(n);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(buffer.getM()+",K,"+n+","+P+"+"+K+",true,"+(RandBalanced?"Balanced":"NotBalanced")+","+elapsedTime);
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

        long elapsedTime = 0;
        try {
            elapsedTime = buffer.put(n);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(buffer.getM()+",P,"+n+","+P+"+"+K+",true,"+(RandBalanced?"Balanced":"NotBalanced")+","+elapsedTime);
    }

}
public class FairVPC {
    public static void main(String[] args) throws InterruptedException {

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
