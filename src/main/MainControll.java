package main;


import javafx.beans.binding.Bindings;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
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
import kelas.AlertInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by hendro-sinaga on 21/08/16.
 */
public class MainControll implements Initializable {
    boolean onPlaying = false;
    int mnt = 0, dtik = 0, ms = 0;
    String fileName, duration, path, absolutePaths, urlFiles;
    Media media = null;
    MediaPlayer mediaPlayer = null;
    URL res;
    File fm;

    @FXML
    AnchorPane topAnchorPane;
    @FXML ScrollPane topScrollPane;
    @FXML MenuBar menuBar;
    @FXML MediaView mediaView;
    @FXML MenuItem menuitemAddFile;
    @FXML Label lblJudul, lblDurasi, lblAlbum, lblArtist, lblYear, lblTotalTime, lblStatus;
    @FXML TextArea textareaInfoFile;
    @FXML Button btnPlay, btnStop;
    @FXML ListView listViewPlaylist;


    @FXML void handleAddFile (ActionEvent actionEvent) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));
        fileChooser.setTitle("Buka Berkas MP3");

        File file = null;
        file = fileChooser.showOpenDialog(Main.mainStage);

        if (file != null) {
            fileName = file.getName();
            this.lblJudul.setText(fileName);
            this.textareaInfoFile.setText(
                    "Judul: " + file.getName()
                    + "\nSize: " + file.length()

            );
            try {
                path = file.getPath();
                absolutePaths = file.getAbsolutePath();
                urlFiles = file.toURI().toString();
                media = new Media(file.toURI().toString());
                this.textareaInfoFile.appendText(
                        "\nDurasi: " + media.getDuration().toMinutes() + " mnt"
                );
                media.getMetadata().addListener(new MapChangeListener<String, Object>() {
                    @Override
                    public void onChanged(Change<? extends String, ? extends Object> change) {
                        if (change.wasAdded()) {
                            handleMetadata(change.getKey(), change.getValueAdded());
                        }
                    }
                });
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                    mediaPlayer = null;
                }
                mediaPlayer = new MediaPlayer(media);

            } catch (Exception ex) {
                AlertInfo.showAlertErrorMessage(
                        "Informasi",
                        "Checking Path File",
                        "Terjadi Kesalahan (Error detail):\n" + ex.getMessage(),
                        ButtonType.OK
                );
            }
            finally {
                //mediaPlayer.dispose();
            }

        }

    }

    @FXML void handlePlay (ActionEvent actionEvent) {
        try {
            if (btnPlay.getText().equalsIgnoreCase("Play")) {
                btnPlay.setText("Pause");
                playMusic();
                lblStatus.setText("Now Playing...");
            } else {
                btnPlay.setText("Play");
                pauseMusic();
                lblStatus.setText("Paused...");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertInfo.showAlertWarningMessage(
                    "Informasi Aplikasi",
                    "Playing MP3 File",
                    "Terjadi kesalahan: " + e.getMessage(),
                    ButtonType.OK
            );
        }
    }

    void playMusic () throws Exception {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                mediaPlayer.play();
            } else {
                onPlaying = true;
                mediaView.setMediaPlayer(mediaPlayer);
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
                lblStatus.setText("No Playlist...");
            }
        });
        mediaPlayer.setAudioSpectrumListener(new AudioSpectrumListener() {
            @Override
            public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {

                //lblDurasi.setText(Double.);
                //textareaInfoFile.appendText("Durasi: " + );

                //dtik += (int)Math.floor(timestamp % 100);
                /*dtik = (int)timestamp;
                dtik = dtik % 60;
                final Double a = new Double(timestamp % 60);
                if (a.intValue() > 0) {
                    if (dtik != a.intValue()) {
                        dtik = a.intValue();
                        lblDurasi.setText("0" + mnt + ":" + dtik);
                    }
                }*/

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
                //lblDurasi.setText(String.format("%02d:%02", min, sec));
            }
        });

        /*
        javafx.util.Duration ct = mediaPlayer.getCurrentTime();
        double milis = ct.toMillis();
        int sec = (int) (milis / 1000) * 60;
        int min = (int) (milis / (1000 * 60));
        lblDurasi.setText(String.format("%02d:%02", min, sec));
         */
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
        } catch (Exception e) {
            AlertInfo.showAlertWarningMessage(
                    "Informasi Aplikasi",
                    "Playing MP3 File",
                    "Terjadi kesalahan: " + e.getMessage(),
                    ButtonType.OK
            );
        }
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
            /*fm = new File(getClass().getRe);
            textareaInfoFile.setText("Cek: " + fm.exists());*/
            menuBar.prefWidthProperty().bind(Bindings.selectDouble(topScrollPane.sceneProperty(), "width"));
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
