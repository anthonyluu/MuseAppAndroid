package com.example.anthonyluu.museapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;
    private final IBinder musicBind = new MusicBinder();
    private  String songTitle="";
    private static final int NOTIFY_ID=1;
    private boolean shuffle = false;
    private Random random;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition = 0;

        player = new MediaPlayer();
        random = new Random();

        initMusicPlayer();
    }

    @Override
    public void onDestroy(){
        stopForeground(true);
    }

    public void initMusicPlayer() {
        // set player properties

        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> songList) {
        songs = songList;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public void playSong() {
        // play a song
        player.reset();
        Song playSong = songs.get(songPosition);
        songTitle = playSong.getTitle();
        long currSong = playSong.getId();

        // set Uri
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch (Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    public void setSong(int songIndex) {
        songPosition = songIndex;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Intent notificationIntent = new Intent (this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_play_arrow_black_48dp)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification notification = builder.build();

        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(player.getCurrentPosition() > 0){
            mediaPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    public int getSongPositionn(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int position){
        player.seekTo(position);
    }

    public void start(){
        player.start();
    }

    public void playPrev(){
        songPosition--;
        if (songPosition<0) {
            songPosition=songs.size()-1;
        }
        playSong();
    }

    public void playNext(){
        if (shuffle) {
            int newSong = songPosition;
            while (newSong == songPosition) {
                newSong = random.nextInt(songs.size());
            }
            songPosition = newSong;
        }else {
            songPosition++;
            if (songPosition >= songs.size()) {
                songPosition = 0;
            }
        }
        playSong();
    }

    public void setShuffle(){
        if (shuffle) {
            shuffle = false;
        }
        else {
            shuffle = true;
        }
    }
}
