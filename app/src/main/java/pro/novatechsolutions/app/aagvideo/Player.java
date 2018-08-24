package pro.novatechsolutions.app.aagvideo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import pro.novatechsolutions.aagvideos.R;


public class Player extends Activity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, View.OnTouchListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        logo = findViewById(R.id.logo);
        aagscreen = findViewById(R.id.aagscreen);
        titleView = findViewById(R.id.title);
        spref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = spref.edit();
        titleView.setVisibility(View.GONE);
        editor.putString("title", "").commit();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mVideoHeight = displayMetrics.heightPixels;
        mVideoWidth = displayMetrics.widthPixels;




    }


    private void setupVideoView() {

        videoView = findViewById(R.id.video_view);
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setOnTouchListener(this);
        videoView.setOnErrorListener(this);
        videoView.setOnInfoListener(this);

        videoView.setVideoURI(Uri.parse("http://199.192.21.16:8080/live/streaming.m3u8"));
        hideSystemUi();


    }

    private void hideSystemUi() {
        videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) videoView.getLayoutParams();
        params.width = mVideoWidth;
        params.height = mVideoHeight;
        videoView.setLayoutParams(params);



    }




    @Override
    public void onStart(){
        super.onStart();
        setupVideoView();

    }

    @Override
    protected void onStop() {
        super.onStop();
        videoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
        videoView.setVideoURI(null);
    }




    @Override
    public boolean onTouch(View v, MotionEvent event) {
        videoView.start();
        return false;
    }


    private void displayTitle() {
        final Handler handler = new Handler();
        AsyncHttpClient client  =  new AsyncHttpClient();
        client.get("http://199.192.21.16/playing_now.php", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                final String title = response.optString("playing_now");

             //   Toast.makeText(Player.this,title, Toast.LENGTH_LONG).show();
               // String spref_title = spref.getString("title", "");
                //if(!title.equals(spref_title) ){
                   // editor.putString("title", title).commit();
                    final Animation fadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
                    final Animation fadeout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);
                    fadein.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    titleView.startAnimation(fadeout);
                                    titleView.setVisibility(View.GONE);
                                }
                            }, 3000);

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });


                    titleView.setVisibility(View.VISIBLE);
                    titleView.setText(title);
                    titleView.startAnimation(fadein);

                //}

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
//                                Log.e("Response", t.getMessage());



            }

        });



    }




    private VideoView videoView;
    private ImageView logo,aagscreen;
    private TextView titleView;
    private SharedPreferences spref;
    private SharedPreferences.Editor editor;
    private int mVideoWidth,mVideoHeight;

    @Override
    public void onCompletion(MediaPlayer mp) {
        //displayTitle();
        videoView.stopPlayback();
        videoView.setVideoURI(Uri.parse("http://199.192.21.16:8080/live/streaming.m3u8"));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        videoView.stopPlayback();
        videoView.setVideoURI(Uri.parse("http://199.192.21.16:8080/live/streaming.m3u8"));
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch(what) {

            case MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING:
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                videoView.stopPlayback();
                videoView.setVideoURI(Uri.parse("http://199.192.21.16:8080/live/streaming.m3u8"));
                break;


        }

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        videoView.start();
        mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                displayTitle();
            }
        });
        logo.setVisibility(View.VISIBLE);
        aagscreen.setVisibility(View.GONE);
    }
}
