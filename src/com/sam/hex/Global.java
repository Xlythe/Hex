package com.sam.hex;

import java.util.ArrayList;
import java.util.List;

import android.view.View;

public class Global{
	private static byte[][] gameboard;
	private static PlayingEntity player1;
	private static PlayingEntity player2;
	private static boolean running;
	private static int gridSize;
	private static Posn[][] polyXY;
	private static byte currentPlayer = 1;
	private static View board;
	private static List<Posn> moveList = new ArrayList<Posn>();
	private static byte gameType = 0; //0 Human v Human,1 Human v ai, 2 ai v Human, 3 ai v ai;
	private static Posn pendingMove;
	private static int hexLength;
	public static int getN() {
		return gridSize;
	}
	public static void setN(int size) {
		Global.gridSize = size;
	}
	public static int getGridSize() {
		return gridSize;
	}
	public static void setGridSize(int size) {
		Global.gridSize = size;
	}
	public static byte[][] getGameboard() {
		return gameboard;
	}
	public static void setGameboard(byte[][] gameboard) {
		Global.gameboard = gameboard;
	}
	public static void setGameboard(int x, int y, byte team) {
		Global.gameboard[x][y] = team;
	}
	public static PlayingEntity getPlayer1(){
		return player1;
	}
	public static void setPlayer1(PlayingEntity player){
		Global.player1 = player;
	}
	public static PlayingEntity getPlayer2(){
		return player2;
	}
	public static void setPlayer2(PlayingEntity player){
		Global.player2 = player;
	}
	public static Posn[][] getPolyXY() {
		return polyXY;
	}
	public static boolean getRunning(){
		return running;
	}
	public static void setRunning(boolean bool){
		Global.running = bool;
	}
	public static void setPolyXY(Posn[][] polyXY) {
		Global.polyXY = polyXY;
	}
	public static void setPolyXY(int x, int y, Posn posn) {
		Global.polyXY[x][y] = posn;
	}
	public static byte getCurrentPlayer(){
		return currentPlayer;
	}
	public static void setCurrentPlayer(byte player){
		Global.currentPlayer = player;
	}
	public static View getBoard(){
		return board;
	}
	public static void setBoard(View view){
		Global.board = view;
	}
	public static List<Posn> getMoveList(){
		return moveList;
	}
	public static void setMoveList(List<Posn> list){
		Global.moveList = list;
	}
	public static void addToMoveList(Posn pos){
		Global.moveList.add(pos);
	}
	public static void removeFromMoveList(Posn pos){
		Global.moveList.remove(pos);
	}
	public static void clearMoveList(){
		Global.moveList.clear();
	}
	public static byte getGameType(){
		return gameType;
	}
	public static void setGameType(byte type){
		Global.gameType = type;
	}
	public static void setGameType(String type){
		int temp = Integer.decode(type);
		Global.gameType = (byte) temp;
	}
	public static Posn getPendingMove(){
		return pendingMove;
	}
	public static void setPendingMove(Posn pos){
		Global.pendingMove = pos;
	}
	public static int getHexLength(){
		return hexLength;
	}
	public static void setHexLength(int L){
		Global.hexLength = L;
	}
}