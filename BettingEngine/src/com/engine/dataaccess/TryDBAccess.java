package com.engine.dataaccess;

import com.engine.dataobject.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TryDBAccess
{
	Connection conn = null;
	private PreparedStatement playerInsert = null;
	private PreparedStatement bettingInsert = null;
	private PreparedStatement scoreUpdate = null;
	private PreparedStatement gameWinUpdate = null;
	private PreparedStatement gameStatusUpdate = null;
	private PreparedStatement gamesForBettingGetOpenWindow = null;	
	private PreparedStatement gamesForBettingGetRunningWindow = null;	
	private PreparedStatement gameGet = null;
	private PreparedStatement bettingGet = null;	
	private PreparedStatement scoreGet = null;
	private PreparedStatement currentBettingGet = null;
	private PreparedStatement playerAllGet = null;
	private PreparedStatement gameRefreshDone = null;
	private PreparedStatement gameRefreshStarted = null;
	private PreparedStatement gameStatusUpdateToStarted = null;	
	private PreparedStatement overrideBetting = null;
	static private ArrayList<Game>gamesForBetting = null; 

			
	public TryDBAccess(){
		getConnection();
		if(gamesForBetting == null){
			gamesForBetting = new ArrayList<Game>();
		}
		refreshGameStatus();		
	}
	
	private Connection getConnection(){
		if (conn == null){
			System.out.println("con is null");
			String driver = "org.apache.derby.jdbc.ClientDriver";
			// the database name  
			String dbName="TryDB";
			   // define the Derby connection URL to use 
			String connectionURL = "jdbc:derby://localhost:50000/" + dbName + ";create=true";
			try{
		         Class.forName(driver); 
		         System.out.println(driver + " loaded. ");
		         conn = DriverManager.getConnection(connectionURL);
		         
		         //initialize my prepstatments
		         playerInsert = conn.prepareStatement("insert into player(playerId, name) values (?,?)");
		         playerAllGet = conn.prepareStatement("select * from player");
		         bettingInsert = conn.prepareStatement("insert into betting(playerId, gameId, betTeam, score, betDate) values (?,?,?,0,?)");
		         gamesForBettingGetOpenWindow = conn.prepareStatement("select * from game where cast(startdate as date) in (select cast (startdate as date) from game where status =? order by cast (startdate as date) asc fetch first 1 row only )");
		         gamesForBettingGetRunningWindow = conn.prepareStatement(new StringBuffer ("select * from game where status ='").
		        		 append(StaticValues.GAME_STATUS_BETTING).append("'").toString());
		         gameGet = conn.prepareStatement("select * from game where gameId = ?");
		         scoreUpdate = conn.prepareStatement("update betting set score =?, haswon = ? where bettingId = ?");
		         gameWinUpdate = conn.prepareStatement("update game set winTeam = ? , status = ? where gameId = ?");
		         gameStatusUpdate = conn.prepareStatement("update game set status = ? where gameId = ?");
		         bettingGet = conn.prepareStatement("select * from betting where gameId = ?");
		         currentBettingGet = conn.prepareStatement("select name, betteam, gameid from betting as b, player as p where gameid=? and p.playerid = b.playerid order by name");
		         scoreGet = conn.prepareStatement("select name, sum(score), sum(haswon) from betting as b, player as p where b.playerid = p.playerid group by name order by name");
		         StringBuffer tmp = new StringBuffer("update game set status ='").append(StaticValues.GAME_STATUS_STARTED).
		        		 	append("' where cast(startdate as date) = CURRENT_DATE and startdate < CURRENT_TIMESTAMP");
		         gameStatusUpdateToStarted = conn.prepareStatement(tmp.toString());//started
		         tmp = new StringBuffer().append("update game set status = 'STARTED' where cast(startdate as date) = " +
		         		"(select cast (startdate as date) from game where status ='STARTED' order by cast (startdate as date) asc fetch first 1 row only)");
		         gameRefreshStarted = conn.prepareStatement(tmp.toString());
		         tmp = new StringBuffer(). append("update game set status= '").append(StaticValues.GAME_STATUS_DONE).
		        		 append("' where cast(startdate as date) < CURRENT_DATE"); //Done
		         gameRefreshDone = conn.prepareStatement(tmp.toString());		 
		         
		         overrideBetting = conn.prepareStatement("update betting set betTeam = ?, betDate = ? where playerid = ? and gameid = ?");
		         
		         
		    } catch(java.lang.ClassNotFoundException e)     {
		          System.err.print("ClassNotFoundException: ");
		          System.err.println(e.getMessage());
		          System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
		    } catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;
	}
	
	public void refreshGameStatus(){
		try {
			System.out.println(new java.util.Date() + " | TryDB.refreshGameStatus | Refresing " );
			gameRefreshStarted.executeUpdate();
			//gameRefreshDone.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
		
	public ArrayList<Game> getGamesForBetting(){	
		Game g = new Game();
		Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
		System.out.println(new java.util.Date() + " | TryDB.getGamesForBetting | Start " );
	
		
		//DitchCache or not
		boolean ditchCache = false;
		if(gamesForBetting != null){
			Iterator<Game> iter = gamesForBetting.iterator();
			ditchCache = !iter.hasNext(); //1. Cache Empty
			while(iter.hasNext()){ //2. any of the elements has gone old,
				g = (Game) iter.next();	
				long l = g.getStartDate().getTime();
				ditchCache = ditchCache || currentTimestamp.after(g.getStartDate());//corrupt ditch it	- Ranjana - hits null
				System.out.println(new java.util.Date() + " | TryDB.GetGamesForBetting | " + g.getGameId() + " DitchCache " + ditchCache);
			}
		}
			
		
		if(!ditchCache){
			System.out.println(new java.util.Date() + " | TryDB.getGamesForBetting | End " );
			return gamesForBetting; //we are good 
		}
	

		gamesForBetting.clear();
				
		//Get Status=betting
		
		forceGetGamesForBetting();
		
		if(gamesForBetting.isEmpty()){
			try {
				this.gameStatusUpdateToStarted.execute();
				this.gameRefreshStarted.execute();
				openGamesForBetting();
			} catch (SQLException e) {			
				e.printStackTrace();
			}	
		}
		System.out.println(new java.util.Date() + " | TryDB.getGamesForBetting | End " );
		return gamesForBetting;		
	}
		
	private void forceGetGamesForBetting(){
		System.out.println(new java.util.Date() + " | TryDB.forceGetGamesForBetting | Start " );
		gamesForBetting.clear();
		Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
		ArrayList<Game> tmpGameList = new ArrayList<Game> ();
 		try {			
			ResultSet rs = gamesForBettingGetRunningWindow.executeQuery();
			boolean ditchDB = false;
			while (rs.next()){				
				Game gg = new Game(rs);	
				long l = gg.getStartDate().getTime();
				ditchDB = ditchDB || currentTimestamp.after(gg.getStartDate());//corrupt ditch it and the whole iter here				
				tmpGameList.add(gg);							
			}	
			if(ditchDB){
				System.out.println(new java.util.Date() + " | TryDB.ForceGetGamesForBetting | Empty List Open new WIndow ");
				return; //return empty list
			}
			gamesForBetting.addAll(tmpGameList);	
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();			
		}	
 		System.out.println(new java.util.Date() + " | TryDB.forceGetGamesForBetting | End " );
		return;		
	}
	
	
	private ArrayList<Game> openGamesForBetting(){
		System.out.println(new java.util.Date() + " | TryDB.openGamesForBetting | Start " );
		gamesForBetting.clear();
		try {			
			gamesForBettingGetOpenWindow.setString(1, StaticValues.GAME_STATUS_NOT_STARTED);
			ResultSet rs = gamesForBettingGetOpenWindow.executeQuery();
			while (rs.next()){				
				Game gg = new Game(rs);
				gamesForBetting.add(gg);
				gameStatusUpdate.setString(1,StaticValues.GAME_STATUS_BETTING );
				gameStatusUpdate.setInt(2, gg.getGameId());
				gameStatusUpdate.executeUpdate();
			}			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		System.out.println(new java.util.Date() + " | TryDB.openGamesForBetting | End " );
		return gamesForBetting;		
	}
	
	
	private int validateBet(String betTeam){
		int ret = StaticValues.CODE_INVALID_BET;		
		//lookup on the cache
		getGamesForBetting(); //refresh gamesForBetting
		Iterator <Game> iter = gamesForBetting.iterator();
		while(iter.hasNext()){
			Game g = iter.next();
			if(g.getTeamA().equalsIgnoreCase(betTeam) || g.getTeamB().equalsIgnoreCase(betTeam)){
				return g.getGameId();
			}
		}
		return ret;
	}
	
	public boolean insertPlayer(String playerId, String name){		    
		boolean ret = false;
		try {
			playerInsert.setString(1, playerId);
			playerInsert.setString(2,name);
			ret = playerInsert.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return ret;
	}
	
	public HashMap<String,String> getPlayers(){
		HashMap <String, String> hm = new HashMap<String,String>();
		try {
			ResultSet rs = playerAllGet.executeQuery();
			while(rs.next()){
				hm.put(rs.getString(1), rs.getString(2));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return hm;
	}
	
	
	public int insertBetting(String playerId, String betTeam){
		int ret = StaticValues.CODE_SUCCESS;
		Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
		int gameId = validateBet(betTeam);
		
		if(gameId == StaticValues.CODE_INVALID_BET){
			return ret;
		}
		
		try {			
			bettingInsert.setString(1,playerId);
			bettingInsert.setInt(2,gameId);
			bettingInsert.setString(3, betTeam);
			bettingInsert.setTimestamp(4, currentTimestamp);
			bettingInsert.execute();
			ret = StaticValues.CODE_SUCCESS;
		} catch (SQLException e) {
			e.printStackTrace();
			if(e.getSQLState() == "23505")
				ret = StaticValues.CODE_DUPLICATE_ENTRY;
			else
				ret = StaticValues.CODE_SQL_FAILURE;			
		}		
		return ret;
	}
	
	
	public  int overrideBetting(String playerId, String betTeam){
		int ret = StaticValues.CODE_SUCCESS;
		Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
		int gameId = validateBet(betTeam);
		
		try {
			overrideBetting.setString(1, betTeam);
			overrideBetting.setTimestamp(2, currentTimestamp);			
			overrideBetting.setString(3,playerId);
			overrideBetting.setInt(4,gameId);
			overrideBetting.execute();
			ret = StaticValues.CODE_SUCCESS;
		} catch (SQLException e) {
			e.printStackTrace();
			if(e.getSQLState() == "23505")
				ret = StaticValues.CODE_DUPLICATE_ENTRY;
			else
				ret = StaticValues.CODE_SQL_FAILURE;			
		}		
		return ret;
	}
	
	public int winTrigger(int gameId, String winResult){
		int ret = StaticValues.CODE_SQL_FAILURE;
		
		try {
			gameGet.setInt(1,gameId);
			ResultSet rs = gameGet.executeQuery();
			Game g = null ;
			while(rs.next()){
				g = new Game(rs);
			}
			rs.close();
			
			if (g == null) return StaticValues.CODE_DATA_DOESNT_EXIST;
				
			if(!(winResult.equalsIgnoreCase("DRAW") ||
					winResult.equalsIgnoreCase(g.getTeamA())|| 
					winResult.equalsIgnoreCase(g.getTeamB()))){
				return StaticValues.CODE_DATA_DOESNT_EXIST;
			}
						
			bettingGet.setInt(1,gameId);
			ResultSet rsBet = bettingGet.executeQuery();
			ArrayList<Betting> winList = new ArrayList<Betting>();
			ArrayList<Betting> loseList = new ArrayList<Betting>();
			while(rsBet.next()){				
				Betting b = new Betting(rsBet.getInt(1),rsBet.getString(2), rsBet.getInt(3), rsBet.getString(4), rsBet.getFloat(5),rsBet.getInt(6),rsBet.getTimestamp(7));
				if(winResult.equalsIgnoreCase("DRAW") || b.getBetTeam().equalsIgnoreCase(winResult)){
					winList.add(b);
				}else{
					loseList.add(b);
				}
			}
			rsBet.close();
			
			
			float totalScore = StaticValues.TOTALPLAYERS * g.getScorePerPlayer();
			float winScore;
			
			if(winList.isEmpty())winScore = 0; //all losers				
			else winScore = totalScore/winList.size();
			
			
			scoreUpdate.setFloat(1, winScore);
			scoreUpdate.setInt(2,1);
			Iterator <Betting> iter = winList.iterator();
			while(iter.hasNext()){
				Betting b = iter.next();				
				scoreUpdate.setInt(3,b.getBettingId());
				scoreUpdate.executeUpdate();				
			}				
			
			
			//Update Games table			
			gameWinUpdate.setString(1, winResult);
			gameWinUpdate.setString(2, StaticValues.GAME_STATUS_DONE);
			gameWinUpdate.setInt(3, gameId);
			gameWinUpdate.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			ret = StaticValues.CODE_SQL_FAILURE;
		}
		
		return ret;
	}
	
	public String displayCurrentBet(){
		String ret = "No Bet Available";		
	
		this.getGamesForBetting();//refreshes the todaysGame if needed
		Iterator <Game> iter = gamesForBetting.iterator();
		StringBuffer buf = new StringBuffer("---------\n");
		while(iter.hasNext()){
			Game g = iter.next();
			buf.append("Game ").append(g.getGameId()).append(" : ");
			if(g.getBettingType().equalsIgnoreCase(StaticValues.BETTING_TYPE_CLOSED))
				buf.append("Nice Try! Closed Betting-No Display");
			else
				buf.append(displayCurrentBet(g.getGameId()));
			buf.append("-------\n");
		}						
		ret = buf.toString();
		return ret;
	}
	
	public String displayCurrentBet(int gameId){
		String ret = "No Bet Available for game " + gameId; 
		String tmp;
		try {
			this.getGamesForBetting();//refreshes the todaysGame if needed			
			Iterator <Game> iter = gamesForBetting.iterator();
			StringBuffer buf = new StringBuffer();
			currentBettingGet.setInt(1,gameId);
			ResultSet rs = currentBettingGet.executeQuery();
			Game g = new Game();
			while(iter.hasNext()){
				g = iter.next();
				if(g.getGameId() != gameId); //do nothing
				else break;
			}
			currentBettingGet.setInt(1,g.getGameId());				
			StringBuffer bufA= new StringBuffer(g.getTeamA()).append(" ( ");
			StringBuffer bufB = new StringBuffer(g.getTeamB()).append(" ( ");
			int betsForA=0;
			int betsForB=0;
			while(rs.next()){
				tmp = rs.getString(2);//betTeam
				if(tmp.equalsIgnoreCase(g.getTeamA())){
					bufA.append(rs.getString(1)).append(" - ");
					betsForA++;
				}	
				else{
					bufB.append(rs.getString(1)).append(" - ");
					betsForB++;
				}	
			}
			buf.append(bufA).append(")").append("[").append(betsForA).append("]").append("\n");
			buf.append("Vs\n");
			buf.append(bufB).append(")").append("[").append(betsForB).append("]").append("\n");		
			ret  = buf.toString();
			
			if(g.getBettingType().equals(StaticValues.BETTING_TYPE_CLOSED)){
				int totalBets = betsForA + betsForB;
				if(totalBets == StaticValues.TOTALPLAYERS){
					g.setBettingType(StaticValues.BETTING_TYPE_OPEN);
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;	
	}
	
	public String displayBetterStatus(){
		String ret = "No Bet Available";		
	
		this.getGamesForBetting();//refreshes the todaysGame if needed
		Iterator <Game> iter = gamesForBetting.iterator();
		StringBuffer buf = new StringBuffer("---------\n");
		while(iter.hasNext()){
			Game g = iter.next();
			buf.append("Game ").append(g.getGameId()).append(" : ").append("\n");
			buf.append(displayBetterStatus(g.getGameId()));
			buf.append("-------\n");
		}						
		ret = buf.toString();
		return ret;
	}
	
	public String displayBetterStatus(int gameId){
		String ret = "No Bet Available for game " + gameId; 
		String tmp;
		try {
			this.getGamesForBetting();//refreshes the todaysGame if needed			
			Iterator <Game> iter = gamesForBetting.iterator();
			StringBuffer buf = new StringBuffer();
			currentBettingGet.setInt(1,gameId);
			ResultSet rs = currentBettingGet.executeQuery();
			Game g = new Game();
			while(iter.hasNext()){
				g = iter.next();
				if(g.getGameId() != gameId); //do nothing
				else break;
			}
			currentBettingGet.setInt(1,g.getGameId());
			ArrayList<String> bettersList = new ArrayList<String>() ;
			StringBuffer bufBetPlaced= new StringBuffer("--Khiladis--").append("\n").append("(");
			StringBuffer bufBetNotPlaced = new StringBuffer("--Sleeping zzzz--").append("\n").append("(");
			int betters = 0;
			int nonBetters = 0;
			while(rs.next()){
				bufBetPlaced.append(rs.getString(1)).append(" - ");	
				bettersList.add(rs.getString(1));
				betters ++;
			}
			buf.append(bufBetPlaced).append(")").append("[").append(betters).append("]").append("\n");
			
			String tempstr;
			HashMap<String,String> m_mp = getPlayers();
		    Iterator it = m_mp.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        if(!bettersList.contains(pairs.getValue())){
		        	tempstr = (String) pairs.getValue();
		        	bufBetNotPlaced.append(tempstr).append(" - ");	
		        	nonBetters ++;
		        }         
		    }
		
			buf.append(bufBetNotPlaced).append(")").append("[").append(nonBetters).append("]").append("\n");		
			ret  = buf.toString();
			
			if(g.getBettingType().equals(StaticValues.BETTING_TYPE_CLOSED)){
				if(betters == StaticValues.TOTALPLAYERS){
					g.setBettingType(StaticValues.BETTING_TYPE_OPEN);
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;	
	}
	
	public String displayScoreBoard(){
		String ret = "No ScoreBoard Available!";
		
		try {
			ResultSet rs = scoreGet.executeQuery();
			List<ScoreObject> soList = new ArrayList<ScoreObject>();
			StringBuffer buf = new StringBuffer();
			while(rs.next()){
				//buf.append(rs.getString(1)).append("\t: ").append(rs.getFloat(2)).append("\t: ").append(rs.getInt(3)).append("\n");
				ScoreObject so = new ScoreObject(rs.getString(1),rs.getFloat(2),rs.getInt(3));
				soList.add(so);
			}
			Collections.sort(soList);
			Iterator<ScoreObject> it  = soList.iterator();
			int i = 1;
			while(it.hasNext()){
				buf.append(i++).append("\t:").append(it.next());
			}
			rs.close();		
			ret = buf.toString();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;		
	}
	
	
	public void close(){
		try {
			if(playerInsert != null && !playerInsert.isClosed()){
				playerInsert.close();
			}
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			if(bettingInsert != null && !bettingInsert.isClosed())
				bettingInsert.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			if(gameGet != null && !gameGet.isClosed()){
				gameGet.close();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(gamesForBettingGetRunningWindow != null && !gamesForBettingGetRunningWindow.isClosed()){
				gamesForBettingGetRunningWindow.close();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(gamesForBettingGetOpenWindow != null && !gamesForBettingGetOpenWindow.isClosed()){
				gamesForBettingGetOpenWindow.close();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			if (conn != null && !conn.isClosed()){
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    //   ## DERBY EXCEPTION REPORTING CLASSES  ## 
    /***     Exception reporting methods
    **      with special handling of SQLExceptions
    ***/
      static void errorPrint(Throwable e) {
         if (e instanceof SQLException) 
            SQLExceptionPrint((SQLException)e);
         else {
            System.out.println("A non SQL error occured.");
            e.printStackTrace();
         }   
      }  // END errorPrint 

    //  Iterates through a stack of SQLExceptions 
      static void SQLExceptionPrint(SQLException sqle) {
         while (sqle != null) {
            System.out.println("\n---SQLException Caught---\n");
            System.out.println("SQLState:   " + (sqle).getSQLState());
            System.out.println("Severity: " + (sqle).getErrorCode());
            System.out.println("Message:  " + (sqle).getMessage()); 
            sqle.printStackTrace();  
            sqle = sqle.getNextException();
         }
   }  //  END SQLExceptionPrint  
	
    public static void main(String[] args){
    	TryDBAccess dbA = new TryDBAccess();
    	
    //	dbA.insertPlayer("ranj", "ranj");
    	
    
    	/*ArrayList<Game> gList = dbA.forceGetTodaysGame(new Timestamp((new java.util.Date().getTime())));
    	Iterator<Game> iter = gList.iterator();
    	while(iter.hasNext()){
    		Game g = iter.next();
    		System.out.println("Game On : " + g.getGameId() + " " + g.getTeamA() + "  " + g.getTeamB());
    	}
    
    */	
    	
    //	dbA.insertBetting("eshu123", "India");
    //	dbA.winTrigger(5, "India");
    
    	/*ArrayList<Game> gList = dbA.getGamesForBetting();
    	Iterator <Game> iter = gList.iterator();
    	while(iter.hasNext()){
    		Game g = (Game) iter.next();
    		System.out.println("Game " + g.getGameId() + " : " + g.getTeamA() );
    	}*/
    	
    	/*Game  g= new Game();
    	g.setGameId(21);
    	g = dbA.getLastMatchnumberWithResult(g);
    	System.out.println("ggg" + g.getGameId() + "  " + g.getTeamA ()+ "  " + g.getTeamB()+ "  " + g.getStatus()); 
    
    	
    	System.out.println(dbA.displayCurrentBet());
    	//System.out.println(dbA.displayScoreBoard());*/
    	
    	System.out.println("Curretine " + new Timestamp (new java.util.Date().getTime()));
    	
    	dbA.close();    
    }

	public Game getLastMatchnumberWithResult(Game g) {
		// TODO Auto-generated method stub
		// Ranjana - Need the last match which was completed and result updated in database
		//System.out.println("In TryDB :  Match number- " + g.getGameId());
		Game thisGameObj = new Game();
		try {
			gameGet.setInt(1,g.getGameId());
			ResultSet rs = gameGet.executeQuery();		
			while(rs.next()){
				thisGameObj = new Game(rs);
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return thisGameObj;
	}
    
  	
}
