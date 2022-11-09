package com.example.musicplayer

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.util.ArrayList

class MainScreenActivity : AppCompatActivity() {
    lateinit var mediaPlayer: MediaPlayer
    private val CHANNEL_ID: String = "My Channel"
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var drawable: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.play_button, null)
        var bitmapDrawable: BitmapDrawable = drawable as BitmapDrawable
        var largeIcon: Bitmap = bitmapDrawable.bitmap
        var notification: Notification
        var nm: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
             notification = Notification.Builder(this@MainScreenActivity)
                 .setColor(R.drawable.splash_screen_gradient_background)
                 .setColorized(true)
                 .setLargeIcon(largeIcon)
                 .setSmallIcon(R.drawable.music_image)
                 .setContentText("Song has started")
                 .setChannelId(CHANNEL_ID)
                 .build()
            nm.createNotificationChannel(NotificationChannel(CHANNEL_ID, "NEW CHANNEL", NotificationManager.IMPORTANCE_HIGH))

        }
        else{
            notification = Notification.Builder(this@MainScreenActivity).setLargeIcon(largeIcon).setSmallIcon(R.drawable.music_image).setContentText("It worked").build()
        }
        supportActionBar?.hide()
        val window: Window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_500)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.purple_500)
        setContentView(R.layout.activity_main)

        var progressThread: Thread
        Dexter.withContext(this@MainScreenActivity).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(object :
            PermissionListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                            val listview: ListView = findViewById(R.id.listView)
                            val mySongs: ArrayList<File> = fetchSongs(Environment.getExternalStorageDirectory())
                            val items: Array<String> = Array(mySongs.size) { i -> (i * 1).toString() }
                            if(mySongs.isEmpty()){
                                Toast.makeText(this@MainScreenActivity, "You don't have any songs in your device", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                for (i in 0 until mySongs.size) {
                                    items[i] = mySongs[i].name
                                }
                                val adapter = CustomListAdapter(
                                    this@MainScreenActivity,
                                    R.layout.custom_list,
                                    items
                                )
                                listview.adapter = adapter
                                runOnUiThread {
                                    var position= 0
                                    val uri = Uri.parse(mySongs[position].toString())
                                    mediaPlayer = MediaPlayer.create(this@MainScreenActivity, uri)
                                    var intent: Intent
                                    listview.onItemClickListener = AdapterView.OnItemClickListener{
                                            _, _, pos, _->
                                        nm.notify(1, notification)
                                        position = pos
                                        intent = Intent(this@MainScreenActivity, MainActivity2::class.java)
                                        val currentSong = listview.getItemAtPosition(pos).toString()
                                        intent.putExtra("songList", mySongs)
                                        intent.putExtra("currentSong", currentSong)
                                        intent.putExtra("position", pos)
                                        startActivity(intent)

                                    } }

                            }
                        }


            fun fetchSongs(file: File?): ArrayList<File> {
                val arrayList: ArrayList<File> = ArrayList()
                val songs = file?.listFiles()
                if(songs != null){
                    for(myFile in songs){
                        if(!myFile.isHidden && myFile.isDirectory){
                            arrayList.addAll(fetchSongs(myFile))
                        }
                        else{
                            if(myFile.name.endsWith(".mp3")){
                                arrayList.add(myFile)
                            }
                        }
                    }
                }
                return arrayList
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Toast.makeText(this@MainScreenActivity, "Permission denied", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
                Toast.makeText(this@MainScreenActivity, "requesting", Toast.LENGTH_SHORT).show()

            }
        }).check()
    }
    class CustomListAdapter(context: Context, textViewResourceId: Int, list: Array<String>) :
        ArrayAdapter<String?>(context, textViewResourceId, list) {
        private val mContext: Context
        private val id: Int
        private val items: List<String?>

        init {
            mContext = context
            id = textViewResourceId
            items = list.toList()
        }

        override fun getView(position: Int, v: View?, parent: ViewGroup): View {
            var mView: View? = v
            if (mView == null) {
                val vi =
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                mView = vi.inflate(id, null)
            }
            val text = mView?.findViewById(R.id.textView) as TextView
            if (items[position] != null) {
                text.setTextColor(Color.WHITE)
                text.text = items[position]
            }
            return mView
        }
    }
}
