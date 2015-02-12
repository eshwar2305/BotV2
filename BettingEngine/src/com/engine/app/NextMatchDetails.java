package com.engine.app;
import java.io.File;
import com.engine.dataaccess.*;
import com.engine.dataobject.*;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class NextMatchDetails extends Thread{
	/**
	   * How frequently to check for next match; 10 mins
	   */
	  private long m_sampleInterval = 1000 * 60 * 10;
	  
	  private boolean m_bettingWindowChanged = false;
	  
	  private ArrayList<Game> currentBettingGameList = new ArrayList<Game>();
	 
	  
	  private TryDBAccess m_dbHandle;
	  /**
	   * Set of listeners
	   */
	  private Set m_listeners = new HashSet();



	  public NextMatchDetails(TryDBAccess dbHandle) {
		  m_dbHandle = dbHandle;
	  }

	  public void addNextMatchListener( NextMatchListener nm )
	  {
	    this.m_listeners.add( nm );
	  }

	  public void removeNextMatchListener( NextMatchListener nm )
	  {
	    this.m_listeners.remove( nm );
	  }
	  
	  
	  protected void fireNextMatchDetailsAvailable( ArrayList<Game> gameList )
	  {
	    for( Iterator i=this.m_listeners.iterator(); i.hasNext(); )
	    {
	      NextMatchListener nm = ( NextMatchListener )i.next();
	      nm.fireNextMatchDetailsAvailable( gameList );
	    }
	  }
	  
	  /***Eshu -  **/
	  public void run()
	  {
		ArrayList <Game> newBettingGameList = new ArrayList<Game>();
		fireNextMatchDetailsAvailable(this.getBettingGameList()); //initialize the current games first
		while(true){		
			System.out.println( new Date()+ " | " + this.getClass().getSimpleName() + "| Start ");
			System.out.println(" -----------old  games -----------");
			Iterator<Game> iter = currentBettingGameList.iterator();
			while(iter.hasNext()){
				System.out.println(" Old Game : " + iter.next().getGameId());
			}
			
			newBettingGameList = this.getBettingGameList();	
			
			System.out.println(" -----------new  games -----------");
			iter = newBettingGameList.iterator();
			while(iter.hasNext()){
				System.out.println(" New Game : " + iter.next().getGameId());
			}
							
			m_bettingWindowChanged = this.isBettingWindowChanged(newBettingGameList);
			System.out.println(" changed ?  " + m_bettingWindowChanged);
			if(m_bettingWindowChanged){				
				fireNextMatchDetailsAvailable(currentBettingGameList);				
			}		
			System.out.println( new Date()+ " | " + this.getClass().getSimpleName() + "| Sleep Off ");
			
			//Once you get the next matches to bet for - go to sleep and wake up after 4hrs 
			//sleep for 4Hrs;
			try {
				sleep(m_sampleInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	    
	  }

	private ArrayList<Game> getBettingGameList() {
		//Ranju - Query DB to get the today matches
		return m_dbHandle.getGamesForBetting();
	}
	
	private boolean isBettingWindowChanged(ArrayList<Game> newBettingGameList){
		boolean ret = true;
		if((currentBettingGameList != null && newBettingGameList == null) ||
				(currentBettingGameList == null && newBettingGameList != null)||
				currentBettingGameList.size() != newBettingGameList.size())
				ret = true;
		
		List<Game> oldL = currentBettingGameList;
		ArrayList<Game> newL = new ArrayList<Game>();
		arrayListCopy(newL, newBettingGameList);		
		newL.removeAll(currentBettingGameList);
		
		if(newL.isEmpty())
			return false;		
		
		//refresh current
		currentBettingGameList.clear();
		currentBettingGameList.addAll(newBettingGameList);
		
		return ret;
	}
	
	private void arrayListCopy (ArrayList<Game> newL, ArrayList<Game> newBettingGameList){
		Iterator <Game> iter = newBettingGameList.iterator();
		while(iter.hasNext()){
			newL.add(iter.next());
		}
	}

	private boolean checkIfMatchStarted(int i) {
		// TODO Auto-generated method stub
		boolean flag;
		int matchNumber = i;
		
		// Ranju - Query DB to check if match i has started - return true or false;
		
		return true;
	}
	
	
	public void testRun(){
		ArrayList <Game> newBettingGameList = new ArrayList<Game>();
		
	//run()	fireNextMatchDetailsAvailable(this.getBettingGameList()); //initialize the current games first
		while(true){			
			System.out.println(this.getClass().getSimpleName() + "  " + new Date() + " Start ");
			System.out.println("-----------old  games -----------");
			Iterator<Game> iter = currentBettingGameList.iterator();
			while(iter.hasNext()){
				System.out.println(" Old Game : " + iter.next().getGameId());
			}
			newBettingGameList = this.getBettingGameList();	
			System.out.println(" -----------new  games -----------");
			iter = newBettingGameList.iterator();
			while(iter.hasNext()){
				System.out.println(" New Game : " + iter.next().getGameId());
			}
			m_bettingWindowChanged = this.isBettingWindowChanged(newBettingGameList);
			System.out.println(" changed ?  " + m_bettingWindowChanged);
			if(m_bettingWindowChanged){				
				System.out.println("Betting window has changed - fire");
			}
			
			System.out.println(Class.class + "  " + new Date() + " Sleep off ");
			
			//Once you get the next matches to bet for - go to sleep and wake up after 4hrs 
			//sleep for 4Hrs;
			try {
				sleep(1*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}	    

	}
	
	public static void main(String[] s){
		NextMatchDetails nm = new NextMatchDetails(new TryDBAccess());
		nm.testRun();
	}
	
}
