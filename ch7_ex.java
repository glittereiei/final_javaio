package Ex6_6613264;    

import java.util.*;
import java.util.concurrent.*;

////////////////////////////////////////////////////////////////////////////////
class BankThread extends Thread
{
    private Account             sharedAccount;    // threads from the same bank work on the same account
    private Exchanger<Account>  exchanger;         
    private CyclicBarrier       barrier;
    private int                 transaction = 1;

    public BankThread(String n, Account sa)             
    { 
        super(n); sharedAccount = sa; 
    }
    
    public void setBarrier(CyclicBarrier ba)            
    {
        barrier   = ba; 
    }
    
    public void setExchanger(Exchanger<Account> ex)     
    { 
        exchanger = ex; 
    }
    
    public void run() 
    {
        try
        {
        // (1) Only 1 thread (from any bank) print start deposit to signal deposit tasks
        //     - All threads must wait to do next step together
            if(barrier.await()==0)
            {
                System.out.printf("%s >> start deposit\n",Thread.currentThread().getName());
            }
            barrier.await();

          
        
        // (2) Each thread does 3 transactions of deposit by calling Account's deposit
            for(int i=0; i<3; i++)
            {
                sharedAccount.deposit(transaction);
                transaction++;
            }
            barrier.await();

        
        // (3) Each bank representative whose Exchanger != null exchanges shardAccount
        //     - Other threads who don't exchange objects must wait until this is done
            if(exchanger != null)
            {
                sharedAccount = exchanger.exchange(sharedAccount);
                System.out.printf("%s >> exchange account\n",Thread.currentThread().getName());
            }
            barrier.await(); 

        
        // (4) Only 1 thread (from any bank) print start withdraw to signal withdrawal tasks
        //     - All threads must wait to do the next step together
            if(barrier.await()==0)
            {
                System.out.printf("%s >> start withdraw\n",Thread.currentThread().getName());
            }
            barrier.await();

        
        // (5) Each thread does 3 transactions of withdrawal by calling Account's withdraw
            for(int i=0; i<3; i++)
            {
                sharedAccount.withdraw(transaction);
                transaction++;
            } 
            barrier.await();
        
        }catch (InterruptedException | BrokenBarrierException e)
            {
                e.printStackTrace();
            }
    }
}

////////////////////////////////////////////////////////////////////////////////
class Account {
    private String  name;
    private int     balance;
    
    public Account(String id, int b)   
    { 
        name = id;
        balance = b; 
    }
    
    public String getName()            
    { 
        return name;
    } 
    
    public int    getBalance()         
    { 
        return balance;
    } 
    
    public synchronized void deposit(int transaction) 
    {
        // Random money 1-100 to deposit; update balance
        // Report thread activity (see example output)
        int amount = new Random().nextInt(1,101);
        balance += amount;
        System.out.printf("%s >> transaction %d   %s %+4d   balance = %4d\n",Thread.currentThread().getName(),transaction,name,amount,balance);
    }
     
    public synchronized void withdraw(int transaction) 
    {
        if (balance > 0) 
        {
            int amount = new Random().nextInt(1, 101);
            if (amount > balance) {
                amount = balance; 
            }
            balance -= amount;
            System.out.printf("%s >> transaction %d   %s %4d   balance = %4d\n",Thread.currentThread().getName(),transaction,name,-amount,balance);
        }
        else 
        {
            System.out.printf("%s >> transaction %d   %s is closed\n",Thread.currentThread().getName(),transaction, name);
        }
    }

}

////////////////////////////////////////////////////////////////////////////////
public class Ex6 {
    public static void main(String[] args) {
        Ex6 mainApp = new Ex6();
        mainApp.runSimulation();
    }

    public void runSimulation()
    {    
        // (1) Suppose there are 2 banks (A and B). Each bank has 1 account
        Account [] accounts = { new Account("account A", 0), 
                                new Account(".".repeat(35) + "account B", 0) };   

        Scanner keyboardScan = new Scanner(System.in);
        System.out.printf("%s  >>  Enter #threads per bank = \n", Thread.currentThread().getName());  
        int num = keyboardScan.nextInt();

        
        // (2) Synchronization objects that will be shared by all threads from both banks
        Exchanger<Account> exchanger = new Exchanger<>();
        CyclicBarrier barrier        = new CyclicBarrier(num*2);   

        
        // (3) Add code to 
        //     - Create threads for bank A (using account A) and bank B (using account B)
        //     - Pass synchronization objects; Exchanger may be passed to 1 thread per bank
        //     - Start all Bankthreads
        ArrayList<BankThread> allThreads = new ArrayList<>();
        for(int i=0 ; i<num;i++)
        {
            BankThread bankA = new BankThread("A_"+(i),accounts[0]);
            bankA.setBarrier(barrier);
            if(i==0)
            {
                bankA.setExchanger(exchanger);
            }
            allThreads.add(bankA);
            
            BankThread bankB = new BankThread("B_" +(i),accounts[1]);
            bankB.setBarrier(barrier);
            if(i==0)
            {
                bankB.setExchanger(exchanger);
            }
            allThreads.add(bankB);
            
        }
        //start all thread
        for(BankThread bt : allThreads)
        {
            bt.start();
        }
     
        
        // (4) After all BankThreads complete their work, print final balance all accounts
        for(BankThread bt : allThreads)
        {
            try
            {
                bt.join();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        System.out.printf("%s >> \n",Thread.currentThread().getName());
        for(Account acc : accounts)
        {
            System.out.printf("%s >> final balance %s = %d \n",Thread.currentThread().getName(), acc.getName(),acc.getBalance());
        }
        
    }
}
