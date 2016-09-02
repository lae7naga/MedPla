package main;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.*;
import javafx.util.Duration;
import kelas.AlertInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by hendro-sinaga on 21/08/16.
 */
public class MainControll implements Initializable {
    private boolean onPlaying = false, firstPlay = true, songEnd = false;
    //private int mnt = 0, dtik = 0, ms = 0;
    private int plIndeks = 0, npl = 0;
    private double vol = 0.00d;
    private String fileName, duration, path, absolutePaths, urlFiles;
    private final List<String> pl = new ArrayList<>();
    private final HashMap<String, String> plHashMap = new HashMap<>();
    ObservableList<String> observableList = FXCollections.observableArrayList();
    Media media = null;
    MediaPlayer mediaPlayer = null;
    final StringProperty sf = new SimpleStringProperty();

    @FXML
    AnchorPane topAnchorPane;
    @FXML
    SubScene subsceneMediaView;
    @FXML ScrollPane topScrollPane;
    @FXML
    VBox vboxMenuBar;
    @FXML MenuBar menuBar;
    @FXML MediaView mediaView;
    @FXML MenuItem menuitemAddFile, menuitemAddFolder;
    @FXML Label lblJudul, lblDurasi, lblAlbum, lblArtist, lblYear, lblTotalTime, lblStatus, lblVolume;
    @FXML TextArea textareaInfoFile;
    @FXML Button btnPlay, btnStop;
    @FXML ListView<String> listViewPlaylist;
    @FXML Slider sliderVolume, sliderSeekTime;

    @FXML void handleAddFile (ActionEvent actionEvent) throws MalformedURLException {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MP3 Files", "*.mp3")
        );
        fileChooser.setTitle("Buka Berkas MP3");
        final File file = fileChooser.showOpenDialog(Main.mainStage);

        if (file != null) {
            fileName = file.getName();

            try {
                pl.add(file.getName());
                plHashMap.put(file.getName(), file.toURI().toString());
                observableList.setAll(pl);
                listViewPlaylist.setItems(observableList);
                npl += 1;
                lblStatus.setText("");
            } catch (Exception ex) {
                AlertInfo.showAlertErrorMessage(
                        "Informasi",
                        "Checking Path File",
                        "Terjadi Kesalahan (Error detail):\n" + ex.getMessage(),
                        ButtonType.OK
                );
            }

        }
    }

    @FXML void handleAddFolder (ActionEvent actionEvent) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Pilih Direktori MP3");
        File file = directoryChooser.showDialog(Main.mainStage);

        if (file != null) {
            try {
                int items = 0;
                for (File tracks:
                        file.listFiles()) {
                    if (tracks.getName().endsWith(".mp3")) {
                        pl.add(tracks.getName());
                        plHashMap.put(tracks.getName(), tracks.toURI().toURL().toString());
                        items += 1;
                    }
                }
                if (items > 0) {
                    observableList.setAll(pl);
                    listViewPlaylist.setItems(observableList);

                    npl += items;
                    lblStatus.setText("");
                } else {
                    lblStatus.setText("Tidak ada berkas MP3 ditemukan...");
                }
            } catch (Exception ex) {
                AlertInfo.showAlertErrorMessage(
                        "Informasi",
                        "Checking Path File",
                        "Terjadi Kesalahan (Error detail):\n" + ex.getMessage(),
                        ButtonType.OK
                );
            }
        }
    }

    @FXML void handlePlay (ActionEvent actionEvent) {
        try {
            if (listViewPlaylist.getItems().size() > 0) {
                initPlaying();
            } else {
                //lblStatus.setText("No Playlist...");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            AlertInfo.showAlertWarningMessage(
                    "Informasi Aplikasi",
                    "Playing MP3 File",
                    "Terjadi kesalahan: " + e.getMessage(),
                    ButtonType.OK
            );
        }
    }

    @FXML void handleCloseApp (ActionEvent actionEvent) {
        Optional<ButtonType> buttonTypeOptional =
                AlertInfo.showConfirmMessage("MedPla - Informasi",
                        "Tutup Aplikasi",
                        "Apakah Anda yakin ingin menutup aplikasi?"
                );
        if (buttonTypeOptional.get().equals(AlertInfo.buttonTypeYes)) {
            if (mediaPlayer != null) {
                if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)
                        || mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)) {
                    mediaPlayer.stop();
                }
                mediaPlayer.dispose();
                System.gc();
            }
            Platform.exit();
        }
    }

    void initPlaying () throws Exception {
        if (btnPlay.getText().equalsIgnoreCase("Play")) {
            if (checkPlaylist()) {
                media = new Media(plHashMap.get(sf.getValue()));
                if (mediaPlayer != null) {
                    mediaPlayer.stop();

                }
                lblJudul.setText(sf.getValue());
                lblArtist.setText("Tidak Dikenal");
                lblAlbum.setText("Tidak Dikenal");
                lblYear.setText("Tidak Dikenal");
                media.getMetadata().addListener(new MapChangeListener<String, Object>() {
                    @Override
                    public void onChanged(Change<? extends String, ? extends Object> change) {
                        if (change.wasAdded()) {
                            handleMetadata(change.getKey(), change.getValueAdded());
                        }
                    }
                });
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setVolume(sliderVolume.getValue() / 100);

            } else if (!onPlaying) {
                if (mediaPlayer != null)
                    mediaPlayer.stop();

                media = new Media(plHashMap.get(sf.getValue()));
                lblJudul.setText(sf.getValue());
                lblArtist.setText("Tidak Dikenal");
                lblAlbum.setText("Tidak Dikenal");
                lblYear.setText("Tidak Dikenal");
                media.getMetadata().addListener(new MapChangeListener<String, Object>() {
                    @Override
                    public void onChanged(Change<? extends String, ? extends Object> change) {
                        if (change.wasAdded()) {
                            handleMetadata(change.getKey(), change.getValueAdded());
                        }
                    }
                });
                plIndeks = listViewPlaylist.getSelectionModel().getSelectedIndex();
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setVolume(sliderVolume.getValue() / 100);
            }
            btnPlay.setText("Pause");
            playMusic();
            lblStatus.setText("Now Playing...");
        } else {
            btnPlay.setText("Play");
            pauseMusic();
            lblStatus.setText("Paused...");
        }
    }

    void initSizeMediaView() throws Exception {
        DoubleProperty mvw = mediaView.fitWidthProperty();
        DoubleProperty mvh = mediaView.fitHeightProperty();
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);
        mvw.bind(Bindings.selectDouble(subsceneMediaView.widthProperty()));
        mvh.bind(Bindings.selectDouble(subsceneMediaView.heightProperty()));
    }

    void playMusic () throws Exception {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.HALTED) {
                return;
            }
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                mediaPlayer.play();
            } else {
                onPlaying = true;
                mediaView.setMediaPlayer(mediaPlayer);
                mediaView.setVisible(true);
                mediaPlayer.setVolume(sliderVolume.getValue() / 100);
                initSizeMediaView();
                //sliderSeekTime.setValue(mediaPlayer.getStartTime().toMillis());
                mediaView.getMediaPlayer().play();
            }
        } else {
            onPlaying = true;
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaView.setVisible(true);
            initSizeMediaView();
            mediaView.getMediaPlayer().play();
            //sliderSeekTime.setValue(mediaPlayer.getCurrentTime().toMillis());
        }
        songEnd = false;

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {

                sliderSeekTime.setMax(mediaPlayer.getTotalDuration().toMillis());
                double milisStop = mediaPlayer.getTotalDuration().toMillis();
                int minStop = (int) (milisStop / (1000 * 60));
                int secStop = (int) (milisStop / 1000);
                secStop = (secStop % 60); //+ (secStop / 60);
                final String strMinStop = (minStop < 10) ? "0" + minStop : "" + minStop;
                final String strSecStop = (secStop < 10) ? "0" + secStop : "" + secStop;
                lblTotalTime.setText(strMinStop + ":" + strSecStop);
            }
        });

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                btnPlay.setText("Play");
                onPlaying = false;
                songEnd = true;
                lblJudul.setText(" -");
                lblAlbum.setText(" -");
                lblArtist.setText(" -");
                lblYear.setText(" -");
                lblStatus.setText("No Playlist...");
                sliderSeekTime.setValue(mediaPlayer.getStopTime().toMillis());
                if (plIndeks < (listViewPlaylist.getItems().size() - 1)) {
                    plIndeks += 1;
                    listViewPlaylist.getSelectionModel().select(plIndeks);
                    sf.setValue(listViewPlaylist.getItems().get(plIndeks).toString());
                    try {
                        initPlaying();
                    } catch (Exception e) {
                        AlertInfo.showAlertWarningMessage(
                                "Informasi Aplikasi",
                                "Playing MP3 File",
                                "Terjadi kesalahan: " + e.getMessage(),
                                ButtonType.OK
                        );
                    }
                }
            }
        });
        mediaPlayer.setAudioSpectrumListener(new AudioSpectrumListener() {
            @Override
            public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {

                //double milis = mediaPlayer.getCurrentTime().toMillis();
                double milis = newValue.toMillis();
                int sec = ((int)milis / 1000) % 60;
                int min = (int) (milis / (1000 * 60));
                final String strMin = (min < 10) ? "0" + min : "" + min;
                final String strSec = (sec < 10) ? "0" + sec : "" + sec;
                sliderSeekTime.setValue(newValue.toMillis());
                lblDurasi.setText(strMin + ":" + strSec);
            }
        });

    }

    void pauseMusic () throws Exception {
        if (mediaPlayer != null && onPlaying) {
            mediaPlayer.pause();
        }
    }

    @FXML void handleStop (ActionEvent actionEvent) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
            if (onPlaying) {
                lblStatus.setText("Stopped...");
                onPlaying = false;
                songEnd = true;
            }
            btnPlay.setText("Play");
            //onPlaying = false;

        } catch (Exception e) {
            AlertInfo.showAlertWarningMessage(
                    "Informasi Aplikasi",
                    "Stoping MP3 File",
                    "Terjadi kesalahan: " + e.getMessage(),
                    ButtonType.OK
            );
        }
    }

    @FXML void handleOpenAbout (ActionEvent actionEvent) {
        final Stage ms = new Stage();
        try {
            final Parent root = FXMLLoader.load(getClass().getResource("aboutdoc.fxml"));
            ms.initModality(Modality.APPLICATION_MODAL);
            ms.initOwner(Main.mainStage);
            ms.setScene(new Scene(root, 600.0, 400.0));
            ms.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
            ms.centerOnScreen();
            ms.setResizable(false);
            ms.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.gc();
                }
            });
            ms.show();
        } catch (Exception e) {
            AlertInfo.showAlertErrorMessage(
                    "Informasi Aplikasi",
                    "Membuka Informasi Tentang Aplikasi",
                    "Terjadi kesalahan ketika akan membuka jendela informasi tentang aplikasi.",
                    ButtonType.OK
            );
        }

    }

    boolean checkPlaylist() throws Exception {
        boolean status = false;
        int count = listViewPlaylist.getItems().size();
        if (count > 0 && firstPlay && !onPlaying && listViewPlaylist.getSelectionModel().getSelectedIndex() < 0) {
            sf.setValue(listViewPlaylist.getItems().get(0).toString());
            listViewPlaylist.getSelectionModel().selectFirst();
            status = true;
            plIndeks = 0;
            firstPlay = false;
        }
        return status;
    }

    private void handleMetadata(String key, Object value) {
        String str = "\n";
        if (key.equalsIgnoreCase("album")) {
            str += "Album: " + value.toString() + "\n";
            lblAlbum.setText(value.toString());
        } else if (key.equalsIgnoreCase("artist")) {
            //artist.setText(value.toString());
            str += "Artis: " + value.toString() + "\n";
            lblArtist.setText(value.toString());
        } if (key.equalsIgnoreCase("title")) {
            lblJudul.setText(value.toString());
            //title.setText(value.toString());
        } else {
            if (sf.getValue() == "") {
                lblJudul.setText(observableList.get(0).toString());
            } else {
                lblJudul.setText(sf.getValue());
            }
        } if (key.equalsIgnoreCase("year")) {
            lblYear.setText(value.toString());
            str += "Tahun: " + value.toString();
        } if (key.equals("image")) {
            //albumCover.setImage((Image)value);
        }
        if (key.equalsIgnoreCase("duration")) {
            str += "\nDurasi: " + value.toString() + "\n";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            sf.set("");
            sliderSeekTime.setMin(0.00d);
            //menuBar.prefWidthProperty().bind(Bindings.selectDouble(topScrollPane.sceneProperty(), "width"));

            subsceneMediaView.setPickOnBounds(true);
            lblVolume.setText(40 + "");

            sliderVolume.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                    if (mediaPlayer != null)
                        mediaPlayer.setVolume(newValue.doubleValue() / 100);
                    lblVolume.setText(((int) Math.floor(newValue.doubleValue())) + "");
                }
            });

            sliderSeekTime.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (mediaPlayer != null) {
                        mediaPlayer.seek(new Duration(sliderSeekTime.getValue()));
                    }
                }
            });
            sliderSeekTime.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (!songEnd && mediaPlayer != null) {
                        mediaPlayer.seek(new Duration(sliderSeekTime.getValue()));
                    }
                }
            });

            listViewPlaylist.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        sf.setValue(newValue.toString());
                    }
            );
            listViewPlaylist.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if(event.getButton().equals(javafx.scene.input.MouseButton.PRIMARY)){
                        if(event.getClickCount() == 2){
                            btnStop.fire();
                            btnPlay.fire();
                        }
                    }
                }
            });
        } catch (Exception e) {
            AlertInfo.showAlertErrorMessage(
                    "Informasi Aplikasi",
                    "Load MP3 Default File",
                    "Eror: \n" + e.getMessage(),
                    ButtonType.OK
            );
        }
    }
}
