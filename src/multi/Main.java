package multi;


import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Stack;

public class Main extends Application {

    Image_Process Mission = null ;

    @Override
    public void start(Stage PrimaryStage) {

        BorderPane Root = new BorderPane();

        Scene S = new Scene(Root , 800 , 800) ;

        Stack<WritableImage>Undo_Stack = new Stack<>();
        Stack<WritableImage>Redo_Stack = new Stack<>();

        // Button if click then we can draw
        ToggleButton Draw = new ToggleButton("Draw");
        Draw.setStyle("-fx-background-color: #6680e6;");
        Draw.setCursor(Cursor.HAND);
        Draw.setMinWidth(90);
        Draw.setOnAction((ActionEvent e) -> {
            if(Draw.isSelected())
                Draw.setStyle("-fx-background-color: #1a3399;");
            else
                Draw.setStyle("-fx-background-color: #6680e6;");
        });

        // Back Step
        Button Undo = new Button("Undo");
        Undo.setStyle("-fx-background-color: #666;");
        Undo.setMinWidth(90);
        Undo.setCursor(Cursor.HAND);
        Undo.setTextFill(Color.WHITE);

        // Forward Step
//        Button Redo = new Button("Redo");
//        Redo.setStyle("-fx-background-color: #666;");
//        Redo.setMinWidth(90);
//        Redo.setCursor(Cursor.HAND);
//        Redo.setTextFill(Color.WHITE);


        // Save Image
        Button Save_Image = new Button("Save");
        Save_Image.setStyle("-fx-background-color: #80334d;");
        Save_Image.setMinWidth(90);
        Save_Image.setCursor(Cursor.HAND);
        Save_Image.setTextFill(Color.WHITE);

        // Open Image
        Button Open_Image = new Button("Open");
        Open_Image.setStyle("-fx-background-color: #80334d;");
        Open_Image.setMinWidth(90);
        Open_Image.setCursor(Cursor.HAND);
        Open_Image.setTextFill(Color.WHITE);

        Button Process_Image = new Button("Process");
        Process_Image.setStyle("-fx-background-color: #80334d;");
        Process_Image.setMinWidth(90);
        Process_Image.setCursor(Cursor.HAND);
        Process_Image.setTextFill(Color.WHITE);

        // All Color can we choose
        ColorPicker All_Color = new ColorPicker(Color.BLACK);
        All_Color.setMinWidth(90);

        // Panel We Want Draw on it
        Canvas Paper = new Canvas(500 , 500);
        GraphicsContext Pen = Paper.getGraphicsContext2D();
        Pen.setLineWidth(1);

        Paper.addEventHandler(MouseEvent.MOUSE_PRESSED , (MouseEvent e) -> {
            if(!Draw.isSelected() || this.Mission == null)
                return;
            WritableImage Image_Sorce = new WritableImage(500 , 500);
            Paper.snapshot(null,Image_Sorce);
            Undo_Stack.push(Image_Sorce);
            Redo_Stack.clear();
            Pen.setStroke(All_Color.getValue());
            Pen.beginPath();
            if(this.Mission != null)
                this.Mission.Set_Point((int) Math.round(e.getX()) , (int) Math.round(e.getY()) , All_Color.getValue() , 1);
        });

        Paper.addEventHandler(MouseEvent.MOUSE_DRAGGED , (MouseEvent e) -> {
            if(!Draw.isSelected() || this.Mission == null)
                return;
            Pen.lineTo(e.getX() , e.getY());
            Pen.stroke();
            if(this.Mission != null)
                this.Mission.Set_Point((int) Math.round(e.getX()) , (int) Math.round(e.getY()) , All_Color.getValue() , 2);
        });

        Paper.addEventHandler(MouseEvent.MOUSE_RELEASED , (MouseEvent e) -> {
            if(!Draw.isSelected() || this.Mission == null)
                return;
            Pen.lineTo(e.getX() , e.getY());
            Pen.stroke();
            Pen.closePath();
            if(this.Mission != null)
                this.Mission.Set_Point((int) Math.round(e.getX()) , (int) Math.round(e.getY()) , All_Color.getValue() , 3);
        });

        //Slider For Font Size
        Slider Font_Sizw = new Slider(1, 10, 1);
        Font_Sizw.setShowTickLabels(true);
        Font_Sizw.setShowTickMarks(true);

        Font_Sizw.valueProperty().addListener(e-> Pen.setLineWidth(Font_Sizw.getValue()));

        // Container All Buttons Control
        VBox All_Button_Menu = new VBox(10);
        All_Button_Menu.getChildren().addAll(Draw , All_Color , Undo, Save_Image, Open_Image , Process_Image , Font_Sizw) ;
        All_Button_Menu.setPadding(new Insets(5));
        All_Button_Menu.setStyle("-fx-background-color: #999");
        All_Button_Menu.setPrefWidth(100);

        //Undo & Redo
        Undo.setOnAction((ActionEvent e) -> {
            if(Undo_Stack.empty())
                return;
            WritableImage SnapShot = Undo_Stack.pop();
            this.Mission = new Image_Process(SnapShot);
            Redo_Stack.push(SnapShot);
            Pen.drawImage(SnapShot , 0 , 0 , 500 , 500);
        });

//        Redo.setOnAction((ActionEvent e) -> {
//            if(Redo_Stack.empty())
//                return;
//            WritableImage SnapShot = Redo_Stack.pop();
//            Undo_Stack.push(SnapShot);
//            Pen.drawImage(SnapShot , 0 , 0 , 500 , 500);
//        });

        //Save And Open And Process
        Open_Image.setOnAction((ActionEvent e) -> {
            FileChooser Window_Choose = new FileChooser();
            Window_Choose.getExtensionFilters().add(new FileChooser.ExtensionFilter
                    ("." , "*.jpg" , "*.jpeg" , "*.png"));
            Window_Choose.setTitle("Open File");
            File Image_File = Window_Choose.showOpenDialog(PrimaryStage);
            if(Image_File != null) {
                try {
                    InputStream io = new FileInputStream(Image_File);
                    Image Image_Choose = new Image(io);
                    Pen.drawImage(Image_Choose , 0 , 0 , 500 , 500);
                    this.Mission = new Image_Process(Paper.snapshot(null, null));
                } catch (IOException ex) {
                    System.out.println("Error!");
                }
            }

        });

        Save_Image.setOnAction((ActionEvent e) -> {
            FileChooser Window_Choose = new FileChooser();
            Window_Choose.getExtensionFilters().add(new FileChooser.ExtensionFilter
                    ("." , "*.jpg" , "*.jpeg" , "*.png"));
            Window_Choose.setTitle("Save File");

            File Image_File = Window_Choose.showSaveDialog(PrimaryStage);
            if (Image_File != null) {
                try {
                    WritableImage writableImage = new WritableImage(500, 500);
                    Paper.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    ImageIO.write(renderedImage, "png", Image_File);
                } catch (IOException ex) {
                    System.out.println("Error!");
                }
            }
        });
//        Undo.setOnAction((ActionEvent e) -> {
//            if(Undo_Stack.empty())
//                return;
//            this.Mission.UnDo_Step();
//            WritableImage SnapShot = Undo_Stack.pop();
//            Redo_Stack.push(SnapShot);
//            Pen.drawImage(SnapShot , 0 , 0 , 500 , 500);
//        });
        Process_Image.setOnAction((ActionEvent e) ->{
                            WritableImage Image_Sorce = new WritableImage(500 , 500);
                            Paper.snapshot(null,Image_Sorce);
                            Undo_Stack.push(Image_Sorce);
                             Pen.drawImage(this.Mission.Apply_Changes() , 0 , 0 , 500 , 500);
                        });
        //Show Stage & Add to Group
        All_Button_Menu.setPrefHeight(S.getHeight());
        Root.setLeft(All_Button_Menu);
        Root.setCenter(Paper);
        PrimaryStage.setScene(S);
        PrimaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
