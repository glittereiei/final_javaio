package Lab_Ch7;

import java.util.concurrent.*;

class MyThread2 extends Thread
{
    private  int       rounds;
    private  Buffer2   buffer;

    public MyThread2(String name)		{ super(name); }
    public void setBuffer(Buffer2 b)            { buffer = b; }
    public void setRounds(int r)		{ rounds = r; }

    @Override
    public void run()
    {
	for (int i=1; i <= rounds; i++)
	{
            buffer.access(i); //access to buffer
	}
    }
};

///////////////////////////////////////////////////////////////////////////////////////
// ----- (1) make buffer the semaphore 
//class Buffer2 extends Semaphore
class Buffer2 
{
    // ----- (2) constructor for semaphore
    //public Buffer2(int permits)			{ super(permits, true); }

    public void access(int c)
    {
	MyThread2 me = (MyThread2)Thread.currentThread();  
        //identify what thread is working

	// ----- (3) semaphore synchronization
	//try { acquire(); } catch (InterruptedException e) { }

	System.out.println( "\n" + me.getName() + " occupies buffer > " + c );
	for (int i=0; i < 100; i++) 
            System.out.print( me.getName() + " " );
        System.out.println( "\n" + me.getName() + " releases buffer > " + c );

	//release();
    }
};

///////////////////////////////////////////////////////////////////////////////////////
class w7_2_Semaphore 
{
    public static void main(String[] args) 
    {
	Buffer2 buffer = new Buffer2();

	// ----- (4) create buffer as a semaphore
	//           #of threads permitted at the same time
	//Buffer2 buffer = new Buffer2(1);
		
	MyThread2 [] T = { new MyThread2("A"), 
                           new MyThread2("B"), 
                           new MyThread2("C") };

        for(int i=0; i < T.length; i++) 
        {
            T[i].setBuffer(buffer);
            T[i].setRounds(2); //define round of thread work 
            //each thread use the same buufer and work 2 round
        } 
        //thread start working
	for(MyThread2 mt : T) mt.start(); 
    }
}
