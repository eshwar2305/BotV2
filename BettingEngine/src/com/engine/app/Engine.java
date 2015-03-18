package com.engine.app;
import java.io.BufferedReader;

import com.engine.dataaccess.*;
import com.engine.dataobject.*;
import com.engine.util.OSValidator;

import java.sql.Timestamp;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class Engine implements LogFileTailerListener,CricinfoListener,NextMatchListener{

	private String out_filename;
	private String writer_botpath;
	private String writer_configpath;
	private LogFileTailer m_tailer;
	private NextMatchDetails m_nmDetails;
	//private Cricinfo m_cricinfo;
	private CricinfoWC2015 m_cricinfo;
	private TryDBAccess m_dbHandle;
	private HashMap<String, String> m_mp = new HashMap<String, String>();
	private ArrayList<Game> m_gamelist;
	//private String grpNumber = "13026901224-1394919313";// for testing purposes - redirect msg to Terminator
	//private String grpNumber = "919686811944-1394712848"; // Testing University group
	private String grpNumber = "15857890554-1423150978"; // Actual group
	String[] m_cmd = {
	        "python",
	        writer_botpath,
	        "demos",
	        "-c",
	        writer_configpath,
	        "-s",
	        //"13026901224-1394919313", //Send msg to Terminator
	        //"13026901224", //Send msg to Eshu number
	        //"13026901224-1333685017", -- BG family
	        //"919686811944-1394712848", //- BAri talk
	        //"13026901224-1423376234", // - Y-Testing
	        "15857890554-1423150978", // WC-2015
	        "test"
	    };
	
	public Engine(){
		OSValidator os = new OSValidator();
		if(os.isUnix()){
			out_filename = "/home/azureuser/git/BotV2/Reader_Bot/out.txt";
			writer_botpath = "/home/azureuser/git/BotV2/Writer_Bot/yowsup-cli";
			writer_configpath = "/home/azureuser/git/BotV2/Writer_Bot/config.txt";
		}else if(os.isMac()){
			out_filename = "/Users/eshwar2305/GitHub/BotV2/Reader_Bot/out.txt";
			writer_botpath = "/Users/eshwar2305/GitHub/BotV2/Writer_Bot/yowsup-cli";
			writer_configpath = "/Users/eshwar2305/GitHub/BotV2/Writer_Bot/config.txt";
		} 
	}
	
	public void spark() {
		m_dbHandle = new TryDBAccess(); //Eshu added this to initialize the object;
		loadPhoneNumbers();
		String workingdirectory = System.getProperty("user.dir");
		System.out.println(workingdirectory);
	    m_tailer = new LogFileTailer( new File(out_filename), 1000, false );
	    m_tailer.addLogFileTailerListener( this );
	    m_tailer.start();
	    
	    m_nmDetails = new NextMatchDetails(m_dbHandle);
	    m_nmDetails.addNextMatchListener(this);
	    m_nmDetails.start();
	    
	    //http://www.espncricinfo.com/icc-cricket-world-cup-2015/engine/series/509587.html
	    //m_cricinfo =  new Cricinfo("http://www.espncricinfo.com/icc-cricket-world-cup-2015/engine/series/806105.html");
	    m_cricinfo =  new CricinfoWC2015("http://www.espncricinfo.com/icc-cricket-world-cup-2015/engine/series/509587.html");
		m_cricinfo.addCricinfoListener(this);
		m_cricinfo.start();
	}
	
	@Override
	public void fireNextMatchDetailsAvailable(ArrayList<Game> gameList) {
		StringBuffer sendMsg =  new StringBuffer();
		for(int i=0;i<gameList.size();i++){
			Game g = gameList.get(i);
			
			sendMsg.append("M");
			sendMsg.append(g.getGameId());
			sendMsg.append(": ");
			sendMsg.append(g.getTeamA());
			sendMsg.append(" vs ");
			sendMsg.append(g.getTeamB());
			sendMsg.append(" at ");
			sendMsg.append(g.getPlace());
			//sendMsg.append("\n");
			sendMsg.append(" on ");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm");
			long t = g.getStartDate().getTime() + TimeUnit.MINUTES.toMillis(750);// Convert PST to IST 810 minutes = 13.5Hrs
			sendMsg.append(sdf.format(new Timestamp(t)));
			sendMsg.append(" (").append(g.getScorePerPlayer()).append(" Points)");
			sendMsg.append("\n");
		}	
		
		if(!isBettingOpen()){
			sendMsg.append("\n");
			sendMsg.append(StaticValues.STR_CLOSED_BETTING_MSG);
		}
		fireMsg(grpNumber,sendMsg.toString());

	}

	@Override
	public void fireMatchResultAvailable(Game g) {
		
		Game gFromDB = m_dbHandle.getLastMatchnumberWithResult(g);
		System.out.println(new java.util.Date() + " | Engine.fireMatchResultAvailable | lastmatch " + gFromDB);
		if(gFromDB != null && !gFromDB.getStatus().equalsIgnoreCase(StaticValues.GAME_STATUS_DONE)){
			System.out.println(new java.util.Date() + " | Engine.fireMatchResultAvailable | Result available-updating DB -Match number : Win team" + g.getGameId()+":"+g.getWinTeam());
			
			
			// call the win trigger for DB update
			m_dbHandle.winTrigger(g.getGameId(), g.getWinTeam());
			
			//sleep for 5s - let DB get updated with new calculation
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			StringBuffer sendMsg = new StringBuffer();
			sendMsg.append("Winner of M");
			sendMsg.append(g.getGameId());
			sendMsg.append(": ");
			sendMsg.append(g.getWinTeam());
			sendMsg.append("\n");
			sendMsg.append("Scorecard: ");
			sendMsg.append("\n");
			sendMsg.append(getCurrentScoreSheet());
			sendMsg.append("\n");
			/*sendMsg.append("Congratualtions to the top three players - Hip hip hurrah!!");
			sendMsg.append("\n");
			sendMsg.append("No.1 player - $10,000 has been deposited to your Swiss Bank Account no. ending XXXX420");
			sendMsg.append("\n");
			sendMsg.append("No.20 player - Happy Rahul Gandhi award to you");
			sendMsg.append("\n");
			sendMsg.append("Thank you all buffalo skinned ppl. Thanks for your patience listening to my abusive language. Its been a pleasure serving you all.");
			sendMsg.append("\n");
			sendMsg.append("\n");
			sendMsg.append("I will be back with a bang and more Kyaak Thu language.");
			sendMsg.append("\n");
			sendMsg.append("Astalavista Baby!!");
			sendMsg.append("\n");
			sendMsg.append("-Botappa signing off!!");*/
			System.out.println("Sending: " + sendMsg.toString());
	    	fireMsg(grpNumber,sendMsg.toString());
		}

	}

	@Override
	public void newLogFileLine(String line) {

		String number,name,timestamp,msg,meanMsg;
		int index;
	    System.out.println("Reading: "+line );
		
	    number = line.substring(0, 12);
	    //System.out.println(number);
	    
	    name = m_mp.get(number); // name of the better
	    //System.out.println(name);
	    
	    timestamp = line.substring(13,31);
	    
	    msg = line.substring(32);
	    msg = msg.toLowerCase();
	    //System.out.println(msg);
	    
	    if(name != null){
	    	StringBuffer sendMsg = new StringBuffer();
	    	if(msg.contains("help")){
	    		sendMsg.append("Status - List of players/non-players \n");
	    		sendMsg.append("Match - Matches for betting \n");
	    		sendMsg.append("Points - Points table \n");
	    		sendMsg.append("Score - Scorecard for match \n");
	    		sendMsg.append("Bet - Current bets placed \n");	    		
		    	System.out.println("Sending: " + sendMsg.toString());
		    	fireMsg(grpNumber,sendMsg.toString());  // Help for commands
		    	return;
	    	}
	    	if(msg.contains("status")){
	    		sendMsg = getBetterStatus();
		    	System.out.println("Sending: " + sendMsg.toString());
		    	fireMsg(grpNumber,sendMsg.toString());  // Latest Bet status sheet
		    	return;
	    	}
	    	if(msg.contains("match")){
	    		sendMsg = getNextMatchDetails();
		    	System.out.println("Sending: " + sendMsg.toString());
		    	fireMsg(grpNumber,sendMsg.toString());  // Match details
		    	return;
	    	}
	    	if(msg.contains("points")){
		    	sendMsg = getCurrentScoreSheet();
		    	System.out.println("Sending: " + sendMsg.toString());
		    	fireMsg(grpNumber,sendMsg.toString());  // Latest scoresheet
		    	return;
	    	}
	    	if(msg.contains("score")){
		    	sendMsg = getMatchScore();
	    		//sendMsg.append("No Score feature available");
		    	System.out.println("Sending: " + sendMsg.toString());
		    	fireMsg(grpNumber,sendMsg.toString());  // match score
		    	return;
	    	}
	    	if(msg.contains("bet")){
	    		if(isBettingOpen()){
	    			sendMsg = getCurrentBetSheet();
	    		}else{
	    			//sendMsg.append(name).append(" ");
		    		index = new Random().nextInt(StaticValues.STR_MESSAGE_CLOSEDBETTING.length);
		    		meanMsg = StaticValues.STR_MESSAGE_CLOSEDBETTING[index];
	    			sendMsg.append(meanMsg);
	    		}
	    		System.out.println("Sending: " + sendMsg.toString());
		    	fireMsg(grpNumber,sendMsg.toString());  // Latest Betsheet
		    	return;
	    	}

	    	ArrayList<String> noOfBetsInMsg = getTeamNamesFromMsg(msg);
	    	if(noOfBetsInMsg.size() == 0){
	    		sendMsg.append(name).append(" ");
	    		index = new Random().nextInt(StaticValues.STR_MESSAGE_UNREADABLE.length);
	    		meanMsg = StaticValues.STR_MESSAGE_UNREADABLE[index];	    		
	    		sendMsg.append(meanMsg);
	    		
	    		if(isBettingOpen()){
		    		System.out.println("Sending: " + sendMsg.toString());
		    		fireMsg(grpNumber,sendMsg.toString());  // indiviual bet error msg
		    		return;
	    		}else{
		    		System.out.println("Sending: " + sendMsg.toString());
		    		fireMsg(number,sendMsg.toString());  // indiviual bet error msg
		    		return;
	    		}

	    	}else{
	    		if((msg.contains("proxy") || msg.contains("override")) && 
	    			(name.contains("Eshwar") || name.contains("Ranjana") || name.contains("Ravi") || name.contains("Prashanth")) ){
	    			String proxyNumber = getProxyNumber(msg);
	    			if(proxyNumber == null) {
	    	    		sendMsg.append(name);
	    	    		sendMsg.append(" please specify correct player name.");
	    	    		System.out.println("Sending: " + sendMsg.toString());
	    	    		fireMsg(number,sendMsg.toString());  // individual bet error msg to Eshwar or ranjana
	    				return;
	    			}
	    			String proxyName = m_mp.get(proxyNumber);
	    			name = proxyName + "";
	    			number = proxyNumber + "";
	    		}
	    		sendMsg.append(name);
	    		sendMsg.append(": ");
		    	for(int i=0;i<noOfBetsInMsg.size();i++){
		    		String team = noOfBetsInMsg.get(i);	
		    		String res;
		    		if(msg.contains("override")){
		    			res = overrideBetForName(number,team); // Changing bets in DB - Must be done only on special cases
		    		}else{
			    		res = placeBetForName(number,team);//place bets for the individual
		    		}
		    		sendMsg.append(team);
		    		sendMsg.append("(");
		    		sendMsg.append(res);
		    		sendMsg.append(")");
		    		sendMsg.append("-");
		    	}
		    	if(isBettingOpen()){
			    	System.out.println("Sending: " + sendMsg.toString());
			    	fireMsg(grpNumber,sendMsg.toString());  // indiviual bet confirmation in group
		    	}else{
		    		sendMsg.append(StaticValues.STR_INDIVIDUAL_BET);
			    	System.out.println("Sending: " + sendMsg.toString());
			    	fireMsg(number,sendMsg.toString());  // indiviual bet confirmation to individual
		    	}

	    	}
	    	
    		if(isBettingOpen()){
    			sendMsg = getCurrentBetSheet();// Latest betsheet to the group 
    		}else{
    			sendMsg = new StringBuffer(name);
    			sendMsg.append(StaticValues.STR_THANK_YOU); // Send a confirmation of the individual that he/she has palced the bet to the group
    			sendMsg.append("\n");
    			sendMsg.append(StaticValues.STR_CLOSED_BETTING_MSG);
    		}
    		
			System.out.println("Sending: " + sendMsg.toString());
			fireMsg(grpNumber,sendMsg.toString());
	    }else{
	    	//Do nothing
	    	return;
	    }
	}

	private StringBuffer getMatchScore() {
		//m_cricinfo.updateCricInfoDetails(); - No need to update - as the score is read from a different html page
		StringBuffer sendMsg = new StringBuffer();
		sendMsg.append(m_cricinfo.getScoreSheet());
		return sendMsg;
	}

	private StringBuffer getNextMatchDetails() {
		ArrayList<Game> gameList = m_dbHandle.getGamesForBetting();
		StringBuffer sendMsg =  new StringBuffer();
		for(int i=0;i<gameList.size();i++){
			Game g = gameList.get(i);
			
			sendMsg.append("M");
			sendMsg.append(g.getGameId());
			sendMsg.append(": ");
			sendMsg.append(g.getTeamA());
			sendMsg.append(" vs ");
			sendMsg.append(g.getTeamB());
			sendMsg.append(" at ");
			sendMsg.append(g.getPlace());
			sendMsg.append(" on ");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm");
			long t = g.getStartDate().getTime() + TimeUnit.MINUTES.toMillis(750);// Convert PST to IST 810 minutes = 13.5Hrs
			sendMsg.append(sdf.format(new Timestamp(t)));
			sendMsg.append(" (").append(g.getScorePerPlayer()).append(" Points)");
			sendMsg.append("\n");
		}	
		
		if(!isBettingOpen()){
			sendMsg.append("\n");
			sendMsg.append(StaticValues.STR_CLOSED_BETTING_MSG);
		}
		return sendMsg;

	}

	private boolean isBettingOpen() {
		ArrayList<Game> gameList = m_dbHandle.getGamesForBetting();
		boolean open = true;
		for(int i=0;i<gameList.size();i++){
			Game g = gameList.get(i);
			if(g.getBettingType().equalsIgnoreCase(StaticValues.BETTING_TYPE_CLOSED)){
				open = false;
			}
		}
		return open;
	}

	private String overrideBetForName(String number, String team) {

		int res =  m_dbHandle.overrideBetting(number, team);	

		if(res == StaticValues.CODE_INVALID_BET)
			return StaticValues.STR_INVALID_BET;
		else if(res ==  StaticValues.CODE_SUCCESS)
			return StaticValues.STR_SUCCESS_BET_PLACED;			
		else if(res ==  StaticValues.CODE_SQL_FAILURE){
			int index = new Random().nextInt(StaticValues.STR_BET_ALREADY_PLACED.length) ;
			return StaticValues.STR_BET_ALREADY_PLACED[index];
		}
		return StaticValues.STR_FAILURE;
	
	}

	private String getProxyNumber(String msg) {
	    Iterator it = m_mp.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String playerName = (String) pairs.getValue();
	        if(msg.contains(playerName.toLowerCase())){
	        	return (String)pairs.getKey();
	        }         
	    }
	    return null;
	
	}

	private StringBuffer getCurrentScoreSheet() {	
		return new StringBuffer(m_dbHandle.displayScoreBoard());
	}

	private StringBuffer getBetterStatus() {
		return new StringBuffer(m_dbHandle.displayBetterStatus());
	}

	private void fireMsg(String number,String sendMsg) {
		if(number.contains("@")){
			number = number.substring(0, 11);
		}
		//System.out.println("Number :"+ number);
		m_cmd[6] = number;
		m_cmd[7] = sendMsg.toString();
    	try {
    		Process p = Runtime.getRuntime().exec(m_cmd);
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		/*BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
    		while(reader.readLine() != null){
    			System.out.println(reader.readLine());
    		}*/
    		
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}

	private StringBuffer getCurrentBetSheet() {
		return new StringBuffer(m_dbHandle.displayCurrentBet());		
	}

	private String placeBetForName(String number, String team) {
		int res =  m_dbHandle.insertBetting(number, team);	

		if(res == StaticValues.CODE_INVALID_BET)
			return StaticValues.STR_INVALID_BET;
		else if(res ==  StaticValues.CODE_SUCCESS)
			return StaticValues.STR_SUCCESS_BET_PLACED;			
		else if(res ==  StaticValues.CODE_SQL_FAILURE){
			int index = new Random().nextInt(StaticValues.STR_BET_ALREADY_PLACED.length) ;
			return StaticValues.STR_BET_ALREADY_PLACED[index];
		}
		return StaticValues.STR_FAILURE;
	}

	private ArrayList<String> getTeamNamesFromMsg(String msg) {
		ArrayList<String> teams = new ArrayList<String>();
		
		ArrayList<Game> gameList = m_dbHandle.getGamesForBetting();
		
		for(int i=0;i<gameList.size();i++){
			Game g = gameList.get(i);
			if(msg.contains(g.getTeamA().toLowerCase())){
				teams.add(g.getTeamA());
			}else if(msg.contains(g.getTeamB().toLowerCase())){
				teams.add(g.getTeamB());
			}
		}

		return teams;
	}

	private void loadPhoneNumbers() {
		m_mp = m_dbHandle.getPlayers(); //Eshu - fetching this from DB itself
		/*m_mp.put("13026901224@", "Eshwar");
		m_mp.put("919886558406", "Abhi");
		m_mp.put("919845208308", "Amruth");
		m_mp.put("919686433880", "Anuranjan");
		m_mp.put("919902315159", "Apurva");
		m_mp.put("919686811944", "Ashwin");
		m_mp.put("919611360852", "Chaitra");
		m_mp.put("919845632873", "DN");
		m_mp.put("919912091116", "PM");
		m_mp.put("919845407435", "Prashanth");
		m_mp.put("918008677337", "Rajiv");
		m_mp.put("917799889885", "Ranju");
		m_mp.put("919886741295", "Ravi");
		m_mp.put("919916394597", "Shashi");
		m_mp.put("919164100066", "Sinchana");
		m_mp.put("919945564212", "Sindhu");
		m_mp.put("919880716292", "Swaroop");
		m_mp.put("919701550588", "Veena");
		m_mp.put("919164444598", "Hemanth");
		m_mp.put("919880338008", "Pradeep");*/
	}
	
	public  String getNumberForPlayer(String player) {
	    Iterator it = m_mp.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String tempstr = (String) pairs.getValue();
	        if(tempstr.equals(player)){
	        	return tempstr;
	        }         
	    }
	    return "";
	}


}
