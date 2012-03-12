/**
 * 
 */
package com.sam.hex;

import android.graphics.Point;

/**
 * @author Sam Laane
 *
 */
public interface PlayingEntity {
	public void getPlayerTurn(byte[][] gameBoard);
	public Point getPlayerTurn(Point hex);
	public void getPlayerTurn();
}
