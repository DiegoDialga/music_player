package com.example.musicplayer

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*

open class MainActivity2: AppCompatActivity() {
    public override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
        finish()
    }
    lateinit var mediaPlayer: MediaPlayer
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val window: Window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_500)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.purple_500)
        window.setContentView(R.layout.activity_main2)

        val intent  = intent
        intent.extras
        val mySongs: ArrayList<File> = intent.getParcelableArrayListExtra<Parcelable>("songList") as ArrayList<File>
        val position = intent.getIntExtra("position", 0)
        val currentSongName = intent.getStringExtra("currentSong")
        val textview: TextView = findViewById(R.id.textView)
        val nextButton: ImageView = findViewById(R.id.activity2_nextButton)
        val prevButton: ImageView = findViewById(R.id.activity2_prevButton)
        val playButton: ImageView = findViewById(R.id.activity2_pauseButton)
        val seekBar: SeekBar = findViewById(R.id.activity2_seekBar)

        automateSongs(mySongs, currentSongName, position, seekBar, playButton, nextButton, prevButton, textview)
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun automateSongs(mySongs: ArrayList<File>, currentSongName: String?, position: Int, seekBar: SeekBar, playButton: ImageView, nextButton: ImageView, prevButton: ImageView, textview: TextView) {
        val duration0: TextView = findViewById(R.id.duration0)
        val durationOfSong: TextView = findViewById(R.id.durationOfSong)
        val loopButton: ImageView = findViewById(R.id.loopButton)

        var pos = position
        if(pos == mySongs.size){
            pos = 0
        }
        var uri = Uri.parse(mySongs[pos].toString())
        mediaPlayer = MediaPlayer.create(this@MainActivity2, uri)
        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer.start()
        loopButton.setOnClickListener{
            if(mediaPlayer.isLooping) {
                mediaPlayer.isLooping = false
                loopButton.setBackgroundColor(Color.parseColor("#FF6200EE"))
            }
            else {
                mediaPlayer.isLooping = true
                loopButton.setBackgroundColor(Color.parseColor("white"))
            }
        }

        textview.text = mySongs[pos].name
        textview.isSelected = true
        durationOfSong.text = convertDuration(mediaPlayer.duration.toLong())
        textview.setTextColor(Color.parseColor("white"))
        playButton.setImageResource(R.drawable.pause_button)
        playButton.layoutParams.height = 80
        playButton.layoutParams.width = 70

        if(!mediaPlayer.isPlaying) {
            playButton.setImageResource(R.drawable.play_button)
            playButton.layoutParams.height = 80
            playButton.layoutParams.width = 70
        }
        else {
            playButton.setImageResource(R.drawable.pause_button)
            playButton.layoutParams.height = 80
            playButton.layoutParams.width = 70
        }

        playButton.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                textview.text = mySongs[pos].name
                textview.isSelected = true
                durationOfSong.text = convertDuration(mediaPlayer.duration.toLong())//((mediaPlayer.duration.toString().toFloat())/60000).toString().replace('.', ':')
                playButton.setImageResource(R.drawable.pause_button)
                playButton.layoutParams.height = 80
                playButton.layoutParams.width = 70
                mediaPlayer.setOnCompletionListener {
                    mediaPlayer.release()
                    automateSongs(mySongs, currentSongName, pos + 1, seekBar, playButton, nextButton, prevButton, textview)
                }
                seekBar.max = mediaPlayer.duration
                Timer().scheduleAtFixedRate(object: TimerTask(){
                    override fun run(){
                        try {
                            seekBar.progress = mediaPlayer.currentPosition
                        }
                        catch (err: java.lang.Exception){
                            print("The error in 129 is $err")
                        }
                    }
                }, 0, 900)
            }
            else{
                mediaPlayer.pause()
                playButton.setImageResource(R.drawable.play_button)
                playButton.layoutParams.height = 80
                playButton.layoutParams.width = 70
            }
        }

        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
            automateSongs(mySongs, currentSongName, pos + 1, seekBar, playButton, nextButton, prevButton, textview)
        }

        seekBar.max = mediaPlayer.duration
        Timer().scheduleAtFixedRate(object: TimerTask(){
            override fun run(){
                try {
                    seekBar.progress = mediaPlayer.currentPosition

                    duration0.text = convertDuration(mediaPlayer.currentPosition.toLong())
                }
                catch (err: java.lang.Exception){
                    print("error is $err")
                }

            }
        }, 0, 900)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2)
                    mediaPlayer.seekTo(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        nextButton.setOnClickListener{
            if(pos < mySongs.size - 1) {
                pos++
            }
            else
                pos = 0
            mediaPlayer.release()
            uri = Uri.parse(mySongs[pos].toString())
            mediaPlayer = MediaPlayer.create(this@MainActivity2, uri)
            mediaPlayer.start()
                if(mediaPlayer.isLooping) {
                    mediaPlayer.isLooping = false
                    loopButton.setBackgroundColor(Color.parseColor("#FF6200EE"))
                }
                else {
                    mediaPlayer.isLooping = true
                    loopButton.setBackgroundColor(Color.parseColor("white"))
                }
            duration0.text = "0:00"
            durationOfSong.text = convertDuration(mediaPlayer.duration.toLong())
            textview.text = mySongs[pos].name
            textview.isSelected = true
            playButton.setImageResource(R.drawable.pause_button)
            playButton.layoutParams.height = 80
            playButton.layoutParams.width = 70
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
                automateSongs(mySongs, currentSongName, pos + 1, seekBar, playButton, nextButton, prevButton, textview)
            }
            seekBar.max = mediaPlayer.duration
            Timer().scheduleAtFixedRate(object: TimerTask(){
                override fun run(){
                    try {
                        seekBar.progress = mediaPlayer.currentPosition
                        duration0.text = convertDuration(mediaPlayer.currentPosition.toLong())
                    }
                    catch (err: java.lang.Exception){
                        print("The error in 178 is $err")
                    }
                }
            }, 0, 900)
        }

        prevButton.setOnClickListener{
            if(pos > 0)
                pos--
            else
                pos = mySongs.size - 1
            mediaPlayer.release()
            uri = Uri.parse(mySongs[pos].toString())
            mediaPlayer = MediaPlayer.create(this@MainActivity2, uri)
            mediaPlayer.start()
                if(mediaPlayer.isLooping) {
                    mediaPlayer.isLooping = false
                    loopButton.setBackgroundColor(Color.parseColor("#FF6200EE"))
                }
                else {
                    mediaPlayer.isLooping = true
                    loopButton.setBackgroundColor(Color.parseColor("white"))
                }
            durationOfSong.text = convertDuration(mediaPlayer.duration.toLong())
            textview.text = mySongs[pos].name
            textview.isSelected = true
            playButton.setImageResource(R.drawable.pause_button)
            playButton.layoutParams.height = 80
            playButton.layoutParams.width = 70
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
                automateSongs(mySongs, currentSongName, pos + 1, seekBar, playButton, nextButton, prevButton, textview)
            }
            seekBar.max = mediaPlayer.duration
            Timer().scheduleAtFixedRate(object: TimerTask(){
                override fun run(){
                    try{
                        seekBar.progress = mediaPlayer.currentPosition
                        duration0.text = convertDuration(mediaPlayer.currentPosition.toLong())
                    }
                    catch (err: java.lang.Exception){
                        print("The error on 211 is $err")
                    }
                } }, 0, 900)
        }
    }
    open fun convertDuration(duration: Long): String? {
        var out: String? = null
        val hours: Long = try {
            duration / 3600000
        } catch (e: Exception) {
            e.printStackTrace()
            return out
        }
        val remainingMinutes = (duration - hours * 3600000) / 60000
        var minutes = remainingMinutes.toString()
        if (minutes == 0.toString()) {
            minutes = "00"
        }
        val remainingSeconds = duration - hours * 3600000 - remainingMinutes * 60000
        var seconds = remainingSeconds.toString()
        seconds = if (seconds.length < 2) {
            "00"
        } else {
            seconds.substring(0, 2)
        }
        out = if (hours > 0) {
            "$hours:$minutes:$seconds"
        } else {
            "$minutes:$seconds"
        }
        return out
    }

}