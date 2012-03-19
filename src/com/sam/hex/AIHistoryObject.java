package com.sam.hex;

import java.util.ArrayList;
import java.util.List;

public class AIHistoryObject{
	List<List<List<Integer>>> pairs = new ArrayList<List<List<Integer>>>();;
	int[] n = {0,0};
	int[] m = {0,0};
	
	public AIHistoryObject(List<List<List<Integer>>> pairs, int[] n, int[] m) {
		for(int i=0;i<pairs.size();i++){
			this.pairs.add(pairs.get(i));
		}
		this.n[0] = n[0];
		this.n[1] = n[1];
		this.m[0] = m[0];
		this.m[1] = m[1];
	}
	
	public String toString(){
		return pairs.toString()+" : "+n.toString()+" : "+m.toString();
	}
}