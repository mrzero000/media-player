package mediaplayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class musicPlayer extends Application {

    MediaPlayer mp;
    MediaView mv;
    private boolean repeat = false;
    static List<File> list = null;
    boolean atEndOfMedia = false;
    boolean stopRequested = false;
    Media med = null;
    Duration duration;
    Label playTime;
    Slider vol_slider;
    Slider time_slider;
    Button plps_btn;
    ToggleButton volbtn;
    HBox control;
    Button rep;
    static int count = 0;
    Button nxt;
    Button prev;
    ListView<String> lv;
    ObservableList<String> filelist;
    Image ply;
    ImageView pl_ps;
    Image pse;
    ToggleButton shufflebtn;
    ToggleButton flscrn;
    Image wall;
    ImageView wallpaper;
    ImageView albmimv;



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage pStage) {

        StackPane raudio=new StackPane();
        StackPane rvideo=new StackPane();
        BorderPane root = new BorderPane();
        Scene scaudio = new Scene(raudio, 1000, 700);
        Scene scvideo =new Scene(rvideo,1000,700);
        rvideo.setId("video");
        music(pStage, root,raudio,rvideo);
        scaudio.getStylesheets().add(musicPlayer.class.getResource("/style1.css").toExternalForm());
        scvideo.getStylesheets().add(musicPlayer.class.getResource("/style1.css").toExternalForm());
        pStage.setScene(scaudio);
        pStage.sizeToScene();
        pStage.setTitle("Music Player");
        pStage.setResizable(true);
        pStage.getIcons().add(new Image("/icon.png"));
        pStage.show();
    }

    public void music(Stage pStage, BorderPane root,StackPane ra,StackPane rv) {

        wall = new Image("/walle.jpg");
        wallpaper= new ImageView(wall);
        wallpaper.setEffect(new GaussianBlur(63.0));
        wallpaper.getStyleClass().add("wallpaper");

        albmimv = new ImageView();

        control = new HBox();
        control.setPadding(new Insets(10));
        control.setSpacing(5.0);

        Image full = new Image("/fullscreen.png", 40, 40, true, true);
        Image norm = new Image("/normalscreen.png", 40, 40, true, true);
        ImageView scr = new ImageView(full);
        flscrn = new ToggleButton("full", scr);
        flscrn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        flscrn.setDisable(true);
        flscrn.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pStage.setFullScreen(true);
                    scr.setImage(norm);
                } else {
                    pStage.setFullScreen(false);
                    scr.setImage(full);
                }
            }
        });

        ply = new Image("/Play.png", 40, 40, true, true);

        pse = new Image("/pause.png", 40, 40, true, true);

        pl_ps = new ImageView(ply);
        plps_btn = new Button("play_pause", pl_ps);
        plps_btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        plps_btn.setDisable(true);


        time_slider = new Slider();
        HBox.setHgrow(time_slider, Priority.ALWAYS);
        time_slider.setMinWidth(50);
        time_slider.setMaxWidth(Double.MAX_VALUE);
        time_slider.setPadding(new Insets(20, 10, 20, 10));
        time_slider.setDisable(true);


        playTime = new Label();
        playTime.setPrefWidth(80);
        playTime.setMinWidth(50);

        Image vol = new Image("/volmax.png", 40, 40, true, true);
        Image volmute = new Image("/volmute.png", 40, 40, true, true);
        ImageView volim = new ImageView(vol);
        volbtn = new ToggleButton("", volim);
        volbtn.setDisable(true);
        vol_slider = new Slider();
        vol_slider.setPrefWidth(100);
        vol_slider.setMinWidth(30);
        vol_slider.setMaxWidth(Region.USE_PREF_SIZE);
        vol_slider.setPadding(new Insets(20, 10, 20, 10));
        vol_slider.setDisable(true);

        plps_btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent ae) {
                MediaPlayer.Status status = mp.getStatus();
                if ((status == MediaPlayer.Status.UNKNOWN) ||
                        (status == MediaPlayer.Status.HALTED)) {
                    return;
                }
                if ((status == MediaPlayer.Status.PAUSED) ||
                        (status == MediaPlayer.Status.READY) ||
                        (status == MediaPlayer.Status.STOPPED)) {
                    if (atEndOfMedia) {
                        mp.seek(mp.getStartTime());
                        atEndOfMedia = false;
                    }
                    mp.play();
                } else {
                    mp.pause();
                }
            }
        });


        volbtn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent ae) {
                if (volbtn.isSelected()) {
                    mp.setVolume(0.0);
                    volim.setImage(volmute);
                } else {
                    mp.setVolume(vol_slider.getValue() / 100);
                    volim.setImage(vol);
                }
            }
        });

        ImageView sng = new ImageView("/songselect.png");
        sng.setFitWidth(40);
        sng.setFitHeight(40);
        Button sngslct = new Button("", sng);
        sngslct.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        sngslct.setOnAction(ae -> {
            FileChooser open = new FileChooser();
            open.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All", "*.mp3","*.mp4","*.flv","*.wav","*.aiff","*.fxm"),
                    new FileChooser.ExtensionFilter("FLV", "*.flv"),
                    new FileChooser.ExtensionFilter("Mp4", "*.mp4"),
                    new FileChooser.ExtensionFilter("MP3","*.mp3"),
                    new FileChooser.ExtensionFilter("WAV","*.wav"),
                    new FileChooser.ExtensionFilter("AIFF","*.aiff"),
                    new FileChooser.ExtensionFilter("FXM","*.fxm")
            );
            if (list == null) {
                try {
                    list = new LinkedList<File>(open.showOpenMultipleDialog(root.getScene().getWindow()));
                }
                catch (NullPointerException e){
                    System.out.println(e);
                }
                if (list != null) {
                    med = new Media(list.get(count++).toURI().toString());
                    mp = new MediaPlayer(med);
                    mv = new MediaView(mp);
                    plps_btn.setDisable(false);
                    vol_slider.setDisable(false);
                    time_slider.setDisable(false);
                    volbtn.setDisable(false);
                    rep.setDisable(false);
                    nxt.setDisable(false);
                    prev.setDisable(false);
                    flscrn.setDisable(false);
                    shufflebtn.setDisable(false);
                    scenechange(pStage,ra,rv,root);
                    updateview();
                    updating();
                    listsetting();
                }
            } else {
                try {
                    List<File> newlist = open.showOpenMultipleDialog(root.getScene().getWindow());
                    File f;
                    for (int i = 0; i < newlist.size(); i++) {
                        f = newlist.get(i);
                        list.add(f);
                    }
                }
                catch (NullPointerException e){
                    System.out.println(e);
                }
                listsetting();
            }
        });
        time_slider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ob) {
                if (time_slider.isValueChanging()) {
                    mp.seek(duration.multiply(time_slider.getValue() / 100.0));
                }
            }
        });


        nxt = new Button("", new ImageView(new Image("/next.png", 40, 40, true, true)));
        nxt.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        nxt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(shufflebtn.isSelected()) {
                    count = randomcount();
                    System.out.println(count);
                }
                if (count == list.size()) {
                    count = 0;
                    mp.pause();
                    med = new Media(list.get(count++).toURI().toString());
                    mp = new MediaPlayer(med);
                    mp.play();
                    updating();
                } else {
                    mp.pause();
                    med = new Media(list.get(count++).toURI().toString());
                    mp = new MediaPlayer(med);
                    mp.play();
                    updating();
                }
                scenechange(pStage,ra,rv,root);
                updateview();
                lv.getSelectionModel().select(count - 1);
            }
        });

        prev = new Button("", new ImageView(new Image("/previous.png", 40, 40, true, true)));
        prev.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        prev.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent ae) {
                if(shufflebtn.isSelected()) {
                    count = randomcount();
                    System.out.println(count);
                }
                if (count == 1) {
                    count = list.size() - 1;
                    mp.pause();
                    med = new Media(list.get(count++).toURI().toString());
                    mp = new MediaPlayer(med);
                    mp.play();
                    updating();
                    System.out.println("if last: " + count);
                } else {
                    mp.pause();
                    count -= 2;
                    med = new Media(list.get(count++).toURI().toString());
                    mp = new MediaPlayer(med);
                    mp.play();
                    System.out.println("last " + count);
                    updating();
                }
                scenechange(pStage,ra,rv,root);
                updateview();
                lv.getSelectionModel().select(count - 1);
            }
        });

        nxt.setDisable(true);
        prev.setDisable(true);

        vol_slider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (vol_slider.isValueChanging()) {
                    mp.setVolume(vol_slider.getValue() / 100.0);
                }
            }
        });


        Image norep = new Image("/norepeat.png", 40, 40, true, true);
        Image repone = new Image("/repeatone.png", 40, 40, true, true);
        ImageView repim = new ImageView(norep);
        rep = new Button("Repeat", repim);
        rep.setDisable(true);
        rep.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        rep.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!repeat) {
                    repeat = true;
                    repim.setImage(repone);
                } else if (repeat) {
                    repeat = false;
                    repim.setImage(norep);
                }
                mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
            }
        });


        VBox plist = new VBox();

        Label playlist = new Label("My PlayList");
        playlist.setLabelFor(lv);
        playlist.setId("playlist");
        plist.setAlignment(Pos.TOP_CENTER);
        Label lvlabel = new Label("No Content is Selected");
        lv = new ListView<>();
        lv.setPlaceholder(lvlabel);
        lv.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        lv.setEditable(false);
        root.setLeft(plist);
        lv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                count = lv.getSelectionModel().getSelectedIndex();
                nxt.fire();
            }
        });


        Image lout = new Image("/ArrowRight.png", 30, 30, false, true);
        Image lin = new Image("/ArrowLeft.png", 30, 30, false, true);
        ImageView ldirect = new ImageView(lin);
        ToggleButton lhide = new ToggleButton("Playlist ", ldirect);
        lhide.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (lhide.isSelected()) {
                    playlist.setVisible(false);
                    lv.setVisible(false);
                    ldirect.setImage(lout);
                } else {
                    playlist.setVisible(true);
                    lv.setVisible(true);
                    ldirect.setImage(lin);
                }
            }
        });
        lhide.setSelected(true);

        Image shfflimg = new Image("/shuffle.png", 40, 40, false, true);
        ImageView shfflimv = new ImageView(shfflimg);
        shufflebtn = new ToggleButton("shuffle", shfflimv);
        shufflebtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        shufflebtn.setDisable(true);

        plist.getChildren().addAll(lhide,playlist, lv);
        control.getChildren().addAll(prev, plps_btn, nxt, playTime, time_slider, volbtn, vol_slider,shufflebtn, rep, sngslct, flscrn);
        root.setBottom(control);

        ra.getChildren().addAll(wallpaper,albmimv,root);
        System.out.println(ra.getChildren()+"|----|");
        System.out.println(rv.getChildren());
    }

    void listsetting() {

        filelist = FXCollections.observableArrayList();
        for (int i = 0; i < list.size(); i++) {
            filelist.add(list.get(i).getName());
        }
        lv.setItems(filelist);

    }

    void updating() {
        mp.currentTimeProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ob) {
                updateValues();
            }
        });

        mp.setOnPlaying(new Runnable() {
            public void run() {
                if (stopRequested) {
                    stopRequested = false;
                    mp.pause();
                } else {
                    pl_ps.setImage(pse);
                }
            }
        });

        mp.setOnPaused(new Runnable() {
            public void run() {
                pl_ps.setImage(ply);
            }
        });

        mp.setOnReady(new Runnable() {
            public void run() {
                duration = mp.getMedia().getDuration();
                updateValues();
            }
        });


        mp.setOnEndOfMedia(new Runnable() {
            public void run() {
                if (!repeat) {
                    nxt.fire();
                }
            }
        });

    }

    void updateValues() {
        if (playTime != null && time_slider != null && vol_slider != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration current_time = mp.getCurrentTime();
                    playTime.setText(timeFormatter(current_time, duration));
                    time_slider.setDisable(duration.isUnknown());
                    if (!time_slider.isDisabled() && duration.greaterThan(Duration.ZERO) && !time_slider.isValueChanging()) {
                        time_slider.setValue(current_time.divide(duration).toMillis() * 100.0);
                    }
                }
            });
        }
    }

    public static String timeFormatter(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes;
        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds, durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }

    void updateview() {
        System.out.println("1");
        AtomicReference<Boolean> flag= new AtomicReference<>(false);

        if ((!(list.get(count-1).getName()).endsWith("mp4"))&&(!(list.get(count-1).getName()).endsWith("flv"))){
            med.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
                if(change.getKey()=="image") {
                    Image albumart = (Image) med.getMetadata().get("image");
                    albmimv.setImage(albumart);
                    albmimv.setFitHeight(500);
                    albmimv.setFitWidth(500);
                    albmimv.setPreserveRatio(true);
                    System.out.println(2);
                    wallpaper.setImage(albumart);
                    flag.set(true);
                }
                if(!flag.get()){
                    wallpaper.setImage(wall);
                }
            });
            System.out.println(3);
        }
        control.setId("control");
    }


    int randomcount(){
        int ran2,ran=0;
        float ran1;
        ran2=(int)(Math.random()*list.size()*10);
        if(ran2%10<5)
            return (ran2/10);
        else
            return ((ran2/10)+1);
    }
    void scenechange(Stage pstage,StackPane ra, StackPane rv,BorderPane root){
        if (((list.get(count-1).getName()).endsWith("mp4"))||((list.get(count-1).getName()).endsWith("flv"))) {
            pstage.setScene(rv.getScene());
            if(!rv.getChildren().contains(mv))
            rv.getChildren().add(0,mv);
            if(!rv.getChildren().contains(root))
                rv.getChildren().add(1,root);
        }
        else{
            pstage.setScene(ra.getScene());
            if(!ra.getChildren().contains(root))
            ra.getChildren().add(2,root);
        }

        System.out.println(ra.getChildren()+"|----|");
        System.out.println(rv.getChildren());
    }
}