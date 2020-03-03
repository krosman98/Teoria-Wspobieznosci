import java.util.Scanner;

class PrintSequence implements Runnable{

    public static int number = 1;
    private int reminder;
    private int n;
    static Object lock = new Object();

    PrintSequence(int reminder,int n){
        this.reminder = reminder;
        this.n = n;
    }

    public void run(){
        while(true){
            synchronized (lock){
                while(number%this.n != reminder){
                    try{
                        lock.wait();
                    }catch (InterruptedException e){}
                }
                System.out.println(number+1);
                number = (number+1)%this.n;
                lock.notifyAll();
            }
        }
    }

}

public class counterOnNThreads {

    public static void main(String[] args){

        System.out.println("Insert n:");
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();

        PrintSequence[] seqs = new PrintSequence[n];
        Thread[] threads = new Thread[n];
        for (int i=0;i<n;i++){
            seqs[i] = new PrintSequence((i+1)%n,n);
            threads[i] = new Thread(seqs[i]);
        }
        for (int i =0;i<n;i++){
            threads[i].start();
        }
    }
}

