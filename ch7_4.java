package Lab_Ch7;

class SharedBuffer
{
    private int share;
    public SharedBuffer(int s)         { share = s; }

    synchronized public void access()
    {
	WaitingThread me = (WaitingThread)(Thread.currentThread());
	while (share < me.getW())
	{
            System.out.printf("%s >> waits \n\n", me.getName());
            try { wait(); } catch(Exception e) { }
	}
	System.out.printf("%s >> is released \n\n", me.getName());
	share = me.updateShare();
	if (share > me.getN())
	{
            System.out.printf("%s >> notifies all \n\n", me.getName());
            notifyAll();
	}
    }
}

class WaitingThread extends Thread
{
    private int local_w, local_n, update;
    private SharedBuffer share;

    public WaitingThread(String na, SharedBuffer sh, int w, int n, int up)	
    { 
	super(na); 
	share   = sh; 
	local_w = w; 
	local_n = n; 
	update  = up; 
    }

    public int getW()	     { return local_w; }
    public int getN()	     { return local_n; }
    public int updateShare() { return update; }

    @Override
    public void run()
    {
	share.access();
    }
}

class w7_4_Waiting
{
    public static void main(String[] args) 
    {
	SharedBuffer share = new SharedBuffer(-10);
	WaitingThread A = new WaitingThread("A", share,    0,  10,  15); 
	WaitingThread B = new WaitingThread("B", share,   10, 100, 500); 
	WaitingThread C = new WaitingThread("C", share, -100,   0,   5); 
	A.start(); B.start(); C.start();
    }
}
