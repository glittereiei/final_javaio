package Lab_Ch7;

import java.util.concurrent.*;

abstract class MyAbstractThread extends Thread
{
    protected  int		rounds;
    protected  Buffer3		buffer;
    protected  MyBarrier	finish;
    protected  CyclicBarrier	cfinish;

    public MyAbstractThread(String name)		{ super(name); }

    public void setBuffer(Buffer3 b)			{ buffer = b; }
    public void setRounds(int r)			{ rounds = r; }
    public void setMyBarrier(MyBarrier f)		{ finish = f; finish.addThreads(); }
    public void setCyclicBarrier(CyclicBarrier f)	{ cfinish = f; }
    
    abstract public void bufferAccess();

    @Override
    public void run()
    {
	for (int i=1; i <= rounds; i++)  bufferAccess();

	// ----- (4) wait at barrier until all the others finish their works
	//finish.reach();
        int x = -1;
	try 
        { 
            x = cfinish.await(); 
        } catch (Exception e) { }
        System.out.printf("\n++++++++++ " + getName() + " finishes, x = %d ++++++++++\n", x);
    }
};


class ProducerThread extends MyAbstractThread
{
    public ProducerThread(String name)		{ super(name); }
    @Override
    public void bufferAccess()			{ buffer.put(); }
};

class ConsumerThread extends MyAbstractThread
{
    public ConsumerThread(String name)		{ super(name); }
    @Override
    public void bufferAccess()			{ buffer.get(); }
};

///////////////////////////////////////////////////////////////////////////////////////
class Buffer3
{
    private String  name;

    public Buffer3(String n)			{ name = n; }
    public String getName()			{ return name; }

    // ----- (1)  add prefix "synchronized" to put() and get()
    // ----- (3)  add prefix "static synchronized" to put() and get()
    synchronized public void put()
    {
	// ----- (2) use synchronized block instead of (1)
	//synchronized (this)
	{
            System.out.printf("%s puts ", Thread.currentThread().getName());
            for (int i=0; i < 10; i++)  
            {
		int delay = (int)(Math.random() * 123);
		try { Thread.sleep(delay); }  catch (InterruptedException e) { }
		System.out.print(i + " ");
            }
            System.out.println();
	}
    }

    synchronized public void get()
    {
	//synchronized (this)
        {
            System.out.printf("%s gets ", Thread.currentThread().getName());
            for (int i=9; i >= 0; i--)  
            {
		int delay = (int)(Math.random() * 123);
		try { Thread.sleep(delay); }  catch (InterruptedException e) { }
		System.out.print(i + " ");
            }
            System.out.println();
	}
    }
};

///////////////////////////////////////////////////////////////////////////////////////
class MyBarrier
{
    private int numthreads = 0;
    public MyBarrier()				{ }
    public MyBarrier(int n)			{ numthreads = n; }

    public synchronized void addThreads()	{ numthreads++; }

    // ----- (5) wait & notify must be executed inside the same monitor
    //           use synchronized block or add prefix "synchronized" to reach()
    public synchronized void reach()
    {
	numthreads--;
	if (numthreads > 0)
            try { wait(); } catch (InterruptedException e) { }
	else 
            notifyAll();
    }
};

///////////////////////////////////////////////////////////////////////////////////////
class w7_3_Monitor 
{
    public static void main(String[] args) 
    {
	Buffer3 bufferMain    = new Buffer3("main buffer");
	Buffer3 bufferAnother = new Buffer3("another buffer");

	ProducerThread P1 = new ProducerThread("Producer 1");
	ConsumerThread C1 = new ConsumerThread("Consumer 1");
	ConsumerThread C2 = new ConsumerThread("Consumer 2");
	
	// ---- (3) change C2's buffer to buffer_2
	P1.setBuffer(bufferMain);  P1.setRounds(3);
	C1.setBuffer(bufferMain);  C1.setRounds(3);
	C2.setBuffer(bufferMain);  C2.setRounds(3);

        
	// ---- (4) barrier synchronization
	/*
        MyBarrier finish = new MyBarrier();
        P1.setMyBarrier( finish );
        C1.setMyBarrier( finish );
        C2.setMyBarrier( finish );
	*/

	
	CyclicBarrier finish = new CyclicBarrier(3);
	P1.setCyclicBarrier( finish );
	C1.setCyclicBarrier( finish );
	C2.setCyclicBarrier( finish );
	

        
	P1.start();
	C1.start();
	C2.start();

        
	// ---- (6) joining threads
        //while(P1.isAlive() || C1.isAlive() || C2.isAlive())  { }
	
	try
	{
            P1.join();  C1.join();  C2.join();
	}
	catch (InterruptedException e) { }
	
        
	System.out.println("Main thread finishes");
    }
}
