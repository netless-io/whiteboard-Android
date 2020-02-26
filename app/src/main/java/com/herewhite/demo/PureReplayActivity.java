package com.herewhite.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.google.gson.Gson;
import com.herewhite.sdk.Logger;
import com.herewhite.sdk.Player;
import com.herewhite.sdk.PlayerEventListener;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PureReplayActivity extends AppCompatActivity implements PlayerEventListener {

    private WhiteboardView whiteboardView;
    @Nullable
    Player player;
    Gson gson;
    private boolean mUserIsSeeking;
    private SeekBar mSeekBar;
    private final String TAG = "player";

    public PureReplayActivity() {
        mUserIsSeeking = false;
        gson = new Gson();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pure_replay);
        mSeekBar = findViewById(R.id.player_seek_bar);
        whiteboardView = findViewById(R.id.white);
        WebView.setWebContentsDebuggingEnabled(true);
        //是否开启 httpDns
        useHttpDnsService(false);

        Intent intent = getIntent();
        final String uuid = intent.getStringExtra(StartActivity.EXTRA_MESSAGE);

        if (uuid != null) {
            new DemoAPI().getRoomToken(uuid, new DemoAPI.Result() {
                @Override
                public void success(String uuid, String roomToken) {
                    initPlayer(uuid, roomToken);
                }

                @Override
                public void fail(String message) {
                    alert("创建回放失败: ", message);
                }
            });
        }
    }

    private void useHttpDnsService(boolean use) {
        if (use) {
            // 阿里云 httpDns 替换
            HttpDnsService httpDns = HttpDns.getService(getApplicationContext(), "188301");
            httpDns.setPreResolveHosts(new ArrayList<>(
                    Arrays.asList("expresscloudharestoragev2.herewhite.com", "cloudharev2.herewhite.com",
                            "scdncloudharestoragev3.herewhite.com", "cloudcapiv4.herewhite.com")));
            whiteboardView.setWebViewClient(new WhiteWebViewClient(httpDns));
        }
    }

    //region Menu Item
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.replayer_command, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    public void getTimeInfo(MenuItem item) {
        Log.i(TAG, gson.toJson(player.getPlayerTimeInfo()));
    }

    public void getPlayState(MenuItem item) {
        Log.i(TAG, gson.toJson(player.getPlayerState()));
    }

    public void getPhase(MenuItem item) {
        Log.i(TAG, gson.toJson(player.getPlayerPhase()));
    }

    //endregion

    //region Play Action
    public void play() {
        if (player != null) {
            player.play();
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 100);
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
        }
    }

    void seek(Long time, TimeUnit timeUnit) {
        if (player != null) {
            long scheduleTime = TimeUnit.MILLISECONDS.convert(time, timeUnit);
            player.seekToScheduleTime(scheduleTime);
        }
    }

    void seek(float progress) {
        if (player != null && player.getPlayerPhase() != PlayerPhase.waitingFirstFrame) {
            PlayerTimeInfo timeInfo = player.getPlayerTimeInfo();
            long time = (long) progress * timeInfo.getTimeDuration();
            seek(time, TimeUnit.MILLISECONDS);
            mSeekBar.setProgress((int) playerProgress());
        }
    }
    //endregion

    //region button action

    void enableBtn() {
        findViewById(R.id.button_play).setEnabled(true);
        findViewById(R.id.button_pause).setEnabled(true);
        findViewById(R.id.button_reset).setEnabled(true);
    }

    public void play(android.view.View button) {
        play();
    }

    public void pause(android.view.View button) {
        pause();
    }

    public void rest(android.view.View button) {
        seek(0l);
    }

    //endregion

    //region seekBar
    private Handler mSeekBarUpdateHandler = new Handler();
    private Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mUserIsSeeking) {
                return;
            }
            float progress = playerProgress();
            Log.v(TAG, "progress: " + progress);
            mSeekBar.setProgress((int) progress);
            mSeekBarUpdateHandler.postDelayed(this, 100);
        }
    };

    private void setupSeekBar() {
        SeekBar seekBar = findViewById(R.id.player_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int userSelectedPosition = 0;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    userSelectedPosition = progress;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = false;
                PlayerPhase playerPhase = player.getPlayerPhase();
                seek(userSelectedPosition / 100l);
            }
        });
    }

    //endregion

    private void initPlayer(String uuid, String roomToken) {
        WhiteSdk whiteSdk = new WhiteSdk(whiteboardView, PureReplayActivity.this,
                new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1, true),
                new UrlInterrupter() {
            @Override
            public String urlInterrupter(String sourceUrl) {
                return sourceUrl;
            }
        });

        PlayerConfiguration playerConfiguration = new PlayerConfiguration(uuid, roomToken);
        // 60 秒，时间太长，seek bar 进度条移动不明显。
        playerConfiguration.setDuration(60000l);
        // 如果只想实现部分 PlayerEventListener 可以使用 AbstractPlayerEventListener，替换其中想实现的方法
        whiteSdk.createPlayer(playerConfiguration, this, new Promise<Player>() {
            @Override
            public void then(Player wPlayer) {
                player = wPlayer;
                setupSeekBar();
                wPlayer.seekToScheduleTime(0);
//                wPlayer.play();
                enableBtn();
            }

            @Override
            public void catchEx(SDKError t) {
                Logger.error("create player error, ", t);
                alert("创建回放失败", t.getJsStack());
            }
        });
    }

    //region PlayerEventListener
    @Override
    public void onPhaseChanged(PlayerPhase phase) {
        Log.i(TAG, "onPhaseChanged: " + phase);
        showToast(gson.toJson(phase));
    }

    @Override
    public void onLoadFirstFrame() {
        Log.i(TAG, "onLoadFirstFrame: ");
        showToast("onLoadFirstFrame");
    }

    @Override
    public void onSliceChanged(String slice) {
        //一般不需要实现
    }

    @Override
    public void onPlayerStateChanged(PlayerState modifyState) {
        Log.i(TAG, "onPlayerStateChanged: " + gson.toJson(modifyState));
    }

    @Override
    public void onStoppedWithError(SDKError error) {
        Log.d(TAG, "onStoppedWithError: " + error.getJsStack());
        showToast(error.getJsStack());
    }

    @Override
    public void onScheduleTimeChanged(long time) {
        Log.v(TAG, "onScheduleTimeChanged: " + time);
    }

    @Override
    public void onCatchErrorWhenAppendFrame(SDKError error) {
        showToast(error.getJsStack());
    }

    @Override
    public void onCatchErrorWhenRender(SDKError error) {
        showToast(error.getJsStack());
    }
    //endregion

    //region private

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                player.refreshViewSize();
            }
        }, 1000);
    }

    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }

    public void alert(final String title, final String detail) {

        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(PureReplayActivity.this).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(detail);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                alertDialog.show();
            }
        });
    }

    float playerProgress() {
        if (player == null || player.getPlayerPhase() == PlayerPhase.waitingFirstFrame) {
            return 0;
        }
        PlayerTimeInfo timeInfo = player.getPlayerTimeInfo();
        float progress = Float.valueOf(timeInfo.getScheduleTime()) / timeInfo.getTimeDuration() * 100.f;
        return progress;
    }
    //endregion

}