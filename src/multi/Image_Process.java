package multi;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Image_Process {

    private final int  Width , Height ;
    private final Color[][] ImagePixel;
    private final ArrayList<ArrayList<Pair<Integer,Integer>>> Points_Save ;
    private final ArrayList<Color> Color_Save ;
    private final Stack<Pair<ArrayList<Pair<Integer,Integer>>,Color>> UnDo ;
    private final int Sensitivity ;

    public Image_Process(WritableImage Current_Image) {
        this.Width = (int)Current_Image.getWidth();
        this.Height = (int)Current_Image.getHeight();
        this.ImagePixel = new Color[this.Width][this.Height];
        this.Points_Save = new ArrayList<>();
        this.Color_Save = new ArrayList<>();
        this.UnDo = new Stack<>();
        this.Sensitivity = 305000 ;
        for (int i = 0; i < this.Width; i++)
            for (int j = 0; j < this.Height; j++)
                this.ImagePixel[i][j] = Current_Image.getPixelReader().getColor(i,j);
    }

    private int RGB2Bits (Color Color_RGB) {
        int Red = (int) Math.round(Color_RGB.getRed() * 255) ;
        int Green = (int) Math.round(Color_RGB.getGreen() * 255) ;
        int Blue = (int) Math.round(Color_RGB.getBlue() * 255) ;
        java.awt.Color Temp = new java.awt.Color(Red , Green , Blue) ;
        return Temp.getRGB();
    }

    private boolean PixelValid( int Xpos , int Ypos , Color PreviusColor , boolean[][] Visited) {
        if (Xpos < 0 || Ypos < 0)
            return false;
        else if (Xpos >= this.Width || Ypos >= this.Height)
            return false;
        else if(Visited[Xpos][Ypos])
            return false;
        int Previus = this.RGB2Bits(PreviusColor) ;
        int Current_Pixel = this.RGB2Bits(this.ImagePixel[Xpos][Ypos]);
        return Current_Pixel >= Previus - this.Sensitivity &&
                Current_Pixel <= Previus + this.Sensitivity;
    }
//get all the pixles valid
    // BFS Algo
    private ArrayList<Pair<Integer,Integer>>
                Draw_Process(Pair<Integer,Integer> Pen_Corrdinate , Color Brash , boolean[][] Visited) {

        Queue<Pair<Integer, Integer>> Pixel_Changes = new LinkedList<>();
        ArrayList<Pair<Integer,Integer>> All_Point_Visited = new ArrayList<>();

        Pixel_Changes.add(Pen_Corrdinate);
        Visited[Pen_Corrdinate.getKey()][Pen_Corrdinate.getValue()] = true ;
        All_Point_Visited.add(Pen_Corrdinate);

        while (!Pixel_Changes.isEmpty()) {
            Pair<Integer, Integer> Coordinate = Pixel_Changes.poll();
            int X = Coordinate.getKey();
            int Y = Coordinate.getValue();
            Color PreviousColor = this.ImagePixel[X][Y];
            this.ImagePixel[X][Y] = Brash;
            // Up Pixel
            if(this.PixelValid(X+1 , Y , PreviousColor , Visited)) {
                Pixel_Changes.add(new Pair<>(X+1 , Y));
                Visited[X+1][Y] = true;
                All_Point_Visited.add(new Pair<>(X+1 , Y));
            }
            // Down Pixel
            if(this.PixelValid(X-1 , Y , PreviousColor , Visited)) {
                Pixel_Changes.add(new Pair<>(X-1 , Y));
                Visited[X-1][Y] = true;
                All_Point_Visited.add(new Pair<>(X-1 , Y));
            }
            // Right Pixel
            if(this.PixelValid(X , Y+1 , PreviousColor , Visited)) {
                Pixel_Changes.add(new Pair<>(X , Y+1));
                Visited[X][Y+1] = true;
                All_Point_Visited.add(new Pair<>(X , Y+1));
            }
            // Left Pixel
            if(this.PixelValid(X , Y-1 , PreviousColor , Visited)) {
                Pixel_Changes.add(new Pair<>(X , Y-1));
                Visited[X][Y-1] = true;
                All_Point_Visited.add(new Pair<>(X , Y-1));
            }
        }
        return All_Point_Visited ;
    }

    private void Process_Image() {
        boolean[][] Visited = new boolean[this.Width][this.Height];
        for (int i = 0; i < this.Width; i++)
            for (int j = 0; j < this.Height; j++)
                Visited[i][j] = false;
        ArrayList<Pair<Integer,Integer>> All_Visited = new ArrayList<>();
        for (int i = 0; i < this.Points_Save.size() ; i++) {
            ArrayList<Pair<Integer,Integer>> Line = this.Points_Save.get(i);
            for (Pair<Integer,Integer> Point: Line)
                if(!Visited[Point.getKey()][Point.getValue()])
                    All_Visited.addAll(this.Draw_Process(Point , this.Color_Save.get(i) , Visited));
            for (Pair<Integer,Integer> Point: All_Visited)
                Visited[Point.getKey()][Point.getValue()] = false;
            All_Visited.clear();
        }
        this.Points_Save.clear();
        this.Color_Save.clear();
    }
//
//    public void UnDo_Step() {
////        this.Points_Save.clear();
//    }
//
//    public void ReDo_Step() {
//        Pair<ArrayList<Pair<Integer,Integer>>,Color> Line = this.UnDo.pop();
//        this.Points_Save.add(Line.getKey());
//        this.Color_Save.add(Line.getValue());
//    }

    public void Set_Point (int PenX , int PenY , Color Brash , int State) {
        int Length ;
        switch (State) {
            case 1 : // MOUSE_PRESSED
                Length = this.Points_Save.size();
                this.Points_Save.add(new ArrayList<>());
                this.Points_Save.get(Length).add(new Pair<>(PenX , PenY));
                this.UnDo.clear();
                break;
            case 2: // MOUSE_DRAGGED
                Length = this.Points_Save.size() - 1;
                this.Points_Save.get(Length).add(new Pair<>(PenX , PenY));
                break;
            case 3: // MOUSE_RELEASED
                Length = this.Points_Save.size() - 1;
                this.Points_Save.get(Length).add(new Pair<>(PenX , PenY));
                this.Color_Save.add(Brash);
                break;
        }
    }

    public WritableImage Apply_Changes() {
        this.Process_Image();
        WritableImage Image_Changing = new WritableImage(this.Width , this.Height);
        for (int i = 0; i < this.Width; i++)
            for (int j = 0; j < this.Height; j++)
                Image_Changing.getPixelWriter().setColor(i , j
                    , this.ImagePixel[i][j]);
            return Image_Changing;
    }
}
