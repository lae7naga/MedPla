package main;


import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import kelas.AlertInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by hendro-sinaga on 21/08/16.
 */
public class MainControll implements Initializable {
    boolean onPlaying = false, firstPlay = true;
    int mnt = 0, dtik = 0, ms = 0, plIndeks = 0;
    private String fileName, duration, path, absolutePaths, urlFiles;
    private final List<String> pl = new ArrayList<>();
    private final HashMap<String, String> plHashMap = new HashMap<>();
    ObservableList observableList = FXCollections.observableArrayList();
    Media media = null;
    MediaPlayer mediaPlayer = null;
    URL res;
    File fm;
    StringProperty sf = new SimpleStringProperty();

    @FXML
    AnchorPane topAnchorPane;
    @FXML ScrollPane topScrollPane;
    @FXML MenuBar menuBar;
    @FXML MediaView mediaView;
    @FXML MenuItem menuitemAddFile;
    @FXML Label lblJudul, lblDurasi, lblAlbum, lblArtist, lblYear, lblTotalTime, lblStatus, lblVolume;
    @FXML TextArea textareaInfoFile;
    @FXML Button btnPlay, btnStop;
    @FXML ListView listViewPlaylist;
    @FXML Slider sliderVolume, sliderSeekTime;


    @FXML void handleAddFile (ActionEvent actionEvent) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));
        fileChooser.setTitle("Buka Berkas MP3");

        File file = null;
        file = fileChooser.showOpenDialog(Main.mainStage);

        if (file != null) {
            fileName = file.getName();
            /*this.lblJudul.setText(fileName);
            this.textareaInfoFile.setText(
                    "Judul: " + file.getName()
                    + "\nSize: " + file.length()

            );*/
            try {
                path = file.getPath();
                absolutePaths = file.getAbsolutePath();
                urlFiles = file.toURI().toString();

                pl.add(file.getName());
                plHashMap.put(file.getName(), file.toURI().toString());
                observableList.setAll(pl);
                listViewPlaylist.setItems(observableList);
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
            initPlaying();
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

    void initPlaying () throws Exception {
        if (btnPlay.getText().equalsIgnoreCase("Play")) {
            if (checkPlaylist()) {
                media = new Media(plHashMap.get(sf.getValue()));
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                        /*mediaPlayer.dispose();
                        mediaPlayer = null;*/
                }
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
                    /*mediaPlayer.dispose();
                    mediaPlayer = null;*/
                media = new Media(plHashMap.get(sf.getValue()));
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
                textareaInfoFile.appendText("\nIndeksDipilih: " + plIndeks);
            }
            btnPlay.setText("Pause");
            //mediaPlayer.getTotalDuration().toMillis()
            sliderSeekTime.setMax(mediaPlayer.getTotalDuration().toMillis());
            playMusic();
            lblStatus.setText("Now Playing...");
        } else {
            btnPlay.setText("Play");
            pauseMusic();
            lblStatus.setText("Paused...");
        }
    }

    void playMusic () throws Exception {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                mediaPlayer.play();
            } else {
                onPlaying = true;
                mediaView.setMediaPlayer(mediaPlayer);
                mediaPlayer.setVolume(sliderVolume.getValue() / 100);
                mediaView.getMediaPlayer().play();
            }
        } else {
            onPlaying = true;
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            //mediaPlayer.setAutoPlay(true);
            mediaView.getMediaPlayer().play();
        }
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                btnPlay.setText("Play");
                onPlaying = false;
                lblStatus.setText("No Playlist...");
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

                //lblDurasi.setText("0" + mnt + ":" + dtik);
                double milis = mediaPlayer.getCurrentTime().toMillis();
                double milisStop = mediaPlayer.getTotalDuration().toMillis();
                int sec = ((int)milis / 1000) % 60;
                int minStop = (int) (milisStop / (1000 * 60));
                int secStop = (int) (milisStop / 1000);
                secStop = secStop % 60;
                int min = (int) (milis / (1000 * 60));
                lblDurasi.setText("0" + min + ":" + sec);
                lblTotalTime.setText(minStop + ":" + secStop);
                //sliderSeekTime.setValue(mediaPlayer.getCurrentTime().toMillis());
                //lblDurasi.setText(String.format("%02d:%02", min, sec));
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                sliderSeekTime.setValue(newValue.toMillis());
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
            lblStatus.setText("Stopped...");
            btnPlay.setText("Play");
            onPlaying = false;
        } catch (Exception e) {
            AlertInfo.showAlertWarningMessage(
                    "Informasi Aplikasi",
                    "Stoping MP3 File",
                    "Terjadi kesalahan: " + e.getMessage(),
                    ButtonType.OK
            );
        }
    }

    boolean checkPlaylist() throws Exception {
        boolean status = false;
        int count = listViewPlaylist.getItems().size();
        if (count > 0 && firstPlay && !onPlaying) {
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
            //album.setText(value.toString());
            str += "Album: " + value.toString() + "\n";
            lblAlbum.setText(value.toString());
        } else if (key.equalsIgnoreCase("artist")) {
            //artist.setText(value.toString());
            str += "Artis: " + value.toString() + "\n";
            lblArtist.setText(value.toString());
        } if (key.equalsIgnoreCase("title")) {
            lblJudul.setText(value.toString());
            //title.setText(value.toString());
        } if (key.equalsIgnoreCase("year")) {
            //year.setText(value.toString());
            lblYear.setText(value.toString());
            str += "Tahun: " + value.toString();
        } if (key.equals("image")) {
            //albumCover.setImage((Image)value);
        }
        if (key.equalsIgnoreCase("duration")) {
            str += "\nDurasi: " + value.toString() + "\n";
        }
        textareaInfoFile.appendText(str);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            sf.set("");
            sliderSeekTime.setMin(0.00d);
            menuBar.prefWidthProperty().bind(Bindings.selectDouble(topScrollPane.sceneProperty(), "width"));
            lblVolume.setText(40 + "");
            sliderVolume.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    mediaPlayer.setVolume((double)newValue / 100);
                    lblVolume.setText(((int) Math.floor((Double) newValue)) + "");
                }
            });

            listViewPlaylist.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        sf.setValue(newValue.toString());
                        textareaInfoFile.appendText("\nDipilih: " + sf.getValue());
                        textareaInfoFile.appendText("\nURL: " + plHashMap.get(newValue.toString()));
                    }
            );
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
