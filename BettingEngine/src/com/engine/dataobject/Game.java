package com.engine.dataobject;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.sql.ResultSet;

public class Game{
	
		private int gameId;
		private String teamA;
		private String teamB;
		private String winTeam;
		private Timestamp startDate;
		private String status;
		private String description;
		private String place;
		private float scorePerPlayer;
		private String bettingType;
		public Game(){};
		public Game(int gameId, String teamA, String teamB, String winTeam, Timestamp startDate, String status, String description, String place, float scorePerPlayer, String bettingType){
			this.gameId = gameId;
			this.teamA= teamA;
			this.teamB = teamB;
			this.winTeam = winTeam;
			this.startDate = startDate;
			this.place = place;
			this.description = description;
			this.status = status;
			this.scorePerPlayer = scorePerPlayer;
			this.bettingType = bettingType;
		}
		
		public Game(ResultSet rs) throws SQLException{
			this(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5),rs.getString(6), rs.getString(7),rs.getString(8), rs.getFloat(9),rs.getString(10));
		}

		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getPlace() {
			return place;
		}
		public void setPlace(String place) {
			this.place = place;
		}
		
		public int getGameId() {
			return gameId;
		}
		public void setGameId(int gameId) {
			this.gameId = gameId;
		}
		public String getTeamA() {
			return teamA;
		}
		public void setTeamA(String teamA) {
			this.teamA = teamA;
		}
		public String getTeamB() {
			return teamB;
		}
		public void setTeamB(String teamB) {
			this.teamB = teamB;
		}
		public String getWinTeam() {
			return winTeam;
		}
		public void setWinTeam(String winTeam) {
			this.winTeam = winTeam;
		}
		public Timestamp getStartDate() {
			return startDate;
		}
		public void setStartDate(Timestamp startDate) {
			this.startDate = startDate;
		}
		
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + gameId;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Game other = (Game) obj;
			if (gameId != other.gameId)
				return false;
			return true;
		}
		public float getScorePerPlayer() {
			return scorePerPlayer;
		}
		public void setScorePerPlayer(float scorePerPlayer) {
			this.scorePerPlayer = scorePerPlayer;
		}
		public String getBettingType() {
			return bettingType;
		}
		public void setBettingType(String bettingType) {
			this.bettingType = bettingType;
		}
		
		
	}