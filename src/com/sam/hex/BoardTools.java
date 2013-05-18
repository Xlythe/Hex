package com.sam.hex;

import android.graphics.Bitmap;

public class BoardTools {
    static double spaceH; // Horizontal
    static double spaceV; // Vertical

    public static double radiusCalculator(double w, double h, double n) {

        spaceV = (((n - 1) * 3 / 2) + 2);

        spaceH = n + (n - 1) / 2; // always bigger.
        spaceH = (w / (spaceH * Math.sqrt(3)));
        spaceV = (h / spaceV);
        if(spaceV < spaceH) {
            return spaceV;
        }
        return spaceH;

    }

    public static Bitmap getBackground(int width, int height, Game game) {
        // the background is drawn to this bitmap.
        Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        RegularPolygonGameObject[][] gamePeace = game.gamePiece;
        double radius = BoardTools.radiusCalculator(width, height, game.gameOptions.gridSize);
        double hrad = radius * Math.sqrt(3) / 2; // Horizontal radius
        int yOffset = (int) ((height - ((3 * radius / 2) * (gamePeace[0].length - 1) + 2 * radius)) / 2);
        int xOffset = (int) ((width - (hrad * gamePeace.length * 2 + hrad * (gamePeace[0].length - 1))) / 2);
        int aX = xOffset;
        int aY = yOffset + (int) radius / 2;
        int bX = xOffset + (int) ((gamePeace.length - 1) * hrad + gamePeace.length * hrad * 2);
        int bY = yOffset + (int) (((gamePeace.length - 1) * radius * 3 / 2) + radius * 3 / 2);
        double slope1 = (double) (bY - aY) / (bX - aX);

        int cX = xOffset + (int) ((gamePeace.length - 1) * hrad + hrad / 2);
        int cY = yOffset + (int) (((gamePeace.length - 1) * radius * 3 / 2) + radius * 7 / 4);
        int dX = xOffset + (int) (gamePeace.length * hrad * 2 - hrad / 2);
        int dY = yOffset + (int) radius / 4;
        double slope2 = (double) (dY - cY) / (dX - cX);

        for(double x = 0; x < width; x++) {
            for(double y = 0; y < height; y++) {
                if(y > slope1 * x + (aY - aX * slope1) != y > slope2 * x + (cY - cX * slope2)) {
                    // if above line 1 == above line 2
                    background.setPixel((int) x, (int) y, game.player1.getColor());
                }
                else {
                    background.setPixel((int) x, (int) y, game.player2.getColor());
                }
            }
        }
        return background;

    }

    public static byte[][] teamGrid(Game game) {
        // not yet in use but will be used to send netcode
        byte[][] loyalty = new byte[game.gameOptions.gridSize][game.gameOptions.gridSize];
        RegularPolygonGameObject[][] gamePeace = game.gamePiece;
        for(int x = 0; x < gamePeace.length; x++)
            for(int y = 0; y < gamePeace[0].length; y++)
                loyalty[x][y] = gamePeace[x][y].getTeam();
        return loyalty;
    }

    public static void clearBoard(Game game) {
        for(RegularPolygonGameObject[] things : game.gamePiece)
            for(RegularPolygonGameObject guy : things)
                if(guy != null) guy.setTeam((byte) 0, game);
    }

    public static void setBoard(Game game) {
        for(RegularPolygonGameObject[] things : game.gamePiece)
            for(@SuppressWarnings("unused")
            RegularPolygonGameObject guy : things)
                guy = new RegularPolygonGameObject();
    }
}
