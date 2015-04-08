package com.engine.dataobject;

public class HallOfFame {
	private int tourid;
	private String tourname;
	private String gold;	
	private String silver;
	private String bronze;
	private String motte;
	private int year;
	public int getTourid() {
		return tourid;
	}
	public void setTourid(int tourid) {
		this.tourid = tourid;
	}
	public String getTourname() {
		return tourname;
	}
	public void setTourname(String tourname) {
		this.tourname = tourname;
	}
	public String getGold() {
		return gold;
	}
	public void setGold(String gold) {
		this.gold = gold;
	}
	public String getSilver() {
		return silver;
	}
	public void setSilver(String silver) {
		this.silver = silver;
	}
	public String getBronze() {
		return bronze;
	}
	public void setBronze(String bronze) {
		this.bronze = bronze;
	}
	public String getMotte() {
		return motte;
	}
	public void setMotte(String motte) {
		this.motte = motte;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public HallOfFame(int tourid, String tourname, String gold, String silver,
			String bronze, String motte, int year) {
		this.tourid = tourid;
		this.tourname = tourname;
		this.gold = gold;
		this.silver = silver;
		this.bronze = bronze;
		this.motte = motte;
		this.year = year;
	}
	

}
