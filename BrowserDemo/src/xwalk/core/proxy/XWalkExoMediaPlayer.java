package xwalk.core.proxy;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xwalk.core.XWalkExMediaPlayer;
import org.xwalk.core.XWalkView;

import xwalk.core.player.DashRendererBuilder;
import xwalk.core.player.DemoPlayer;
import xwalk.core.player.ExtractorRendererBuilder;
import xwalk.core.player.HlsRendererBuilder;
import xwalk.core.player.SmoothStreamingRendererBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.browserdemo.HomeActivity;
import com.example.browserdemo.R;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.util.DebugTextViewHelper;
import com.google.android.exoplayer.util.Util;
//import org.example.player.ExoMediaPlayer;

/**
 * Created by fujunwei on 16-5-11.
 */
public class XWalkExoMediaPlayer extends XWalkExMediaPlayer implements SurfaceHolder.Callback,
        DemoPlayer.Listener, DemoPlayer.Id3MetadataListener{
    static final String TAG = "ExoMediaPlayer";
    private static final int MENU_GROUP_TRACKS = 1;
    private static final int ID_OFFSET = 2;
    private Context mContext;

    private DemoPlayer player;
    //    private DebugTextViewHelper debugViewHelper;
    private boolean playerNeedsPrepare;
    private EventLogger eventLogger;
    private DebugTextViewHelper debugViewHelper;
    private MediaController mediaController;

    private Uri contentUri;
    private int contentType;
    private String contentId;
    private String provider;

    public static final String PROXY_HOST = "140.207.47.119";
    public static final int PROXY_HTTP_PORT = 10010;
    private String proxyHost;
    private int proxyPort;

    MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener;
    MediaPlayer.OnCompletionListener mCompletionListener;
    MediaPlayer.OnPreparedListener mPreparedListener;
    MediaPlayer.OnSeekCompleteListener mSeekCompleteListener;
    MediaPlayer.OnVideoSizeChangedListener mVideoSizeChangedListener;
    MediaPlayer.OnErrorListener mErrorListener;

    SurfaceView mSurfaceView;
    Surface xwalkSurface;
    XWalkView mXWalkView;
    private final int INVALID_ORIENTATION = -2;
    private int mPreOrientation = INVALID_ORIENTATION;
    boolean mCustomFullscreen;

    private int mBufferedPercentage;
    private int mVideoWidth;
    private int mVideoHeight;
    Map<String, String> mHeaders;

    public XWalkExoMediaPlayer(Context context, XWalkView xWalkView, SurfaceView surfaceview) {
        mContext = context;
        mXWalkView = xWalkView;
        mediaController = new KeyCompatibleMediaController(context);
        mSurfaceView = surfaceview;//new SurfaceView(context);
        mCustomFullscreen = false;
    }

    public void updateProxySetting(String host, int port) {
        proxyHost = host;
        proxyPort = port;
    }

    @Override
    public void prepareAsync() {
        preparePlayer(true);
        if (!mCustomFullscreen) {
            onShowCustomView(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mCustomFullscreen = true;
        }
    }

    @Override
    public void setSurface(Surface surface) {
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) {
        configurePlayingSource(uri, headers);
    }


    @Override
    public boolean isPlaying() {
        return player == null ? false : player.isPlaying();
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public int getCurrentPosition() {
        if (mBufferedPercentage != player.getBufferedPercentage()) {
            mBufferedPercentage = player.getBufferedPercentage();
            mBufferingUpdateListener.onBufferingUpdate(null, mBufferedPercentage);
        }
        return (int) player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return (int) player.getDuration();
    }

    @Override
    public void release() {
        releasePlayer();
    }

    @Override
    public void setVolume(float volume1, float volume2) {
    }

    @Override
    public void start() {
        player.setPlayWhenReady(true);

        if (!mCustomFullscreen) {
            onShowCustomView(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            player.setSurface(mSurfaceView.getHolder().getSurface());
            mCustomFullscreen = true;
        }
    }

    @Override
    public void pause() {
        player.setPlayWhenReady(false);
    }

    @Override
    public void seekTo(int msec) {
        player.seekTo(msec);
        mSeekCompleteListener.onSeekComplete(null);
    }

    @Override
    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener listener) {
        mBufferingUpdateListener = listener;
    }

    @Override
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mCompletionListener = listener;
    }

    @Override
    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        mErrorListener = listener;
    }

    @Override
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        mPreparedListener = listener;
    }

    @Override
    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener) {
        mSeekCompleteListener = listener;
    }

    @Override
    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener listener) {
        mVideoSizeChangedListener = listener;
    }

    private void configurePlayingSource(Uri uri, Map<String, String> headers) {
        contentUri = uri;//Uri.parse("http://122.96.25.242:8088/war.mp4");//uri;
        contentType = inferContentType(contentUri, "");
        contentId = "Demo Testing".toLowerCase(Locale.US).replaceAll("\\s", "");
        provider = "";

        mHeaders = headers;
    }

    private DemoPlayer.RendererBuilder getRendererBuilder() {
        switch (contentType) {
            case Util.TYPE_SS:
                return new SmoothStreamingRendererBuilder(mContext, mHeaders, contentUri.toString(),
                        new SmoothStreamingTestMediaDrmCallback(), proxyHost, proxyPort);
            case Util.TYPE_DASH:
                return new DashRendererBuilder(mContext, mHeaders, contentUri.toString(),
                        new WidevineTestMediaDrmCallback(contentId, provider), proxyHost, proxyPort);
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(mContext, mHeaders, contentUri.toString(),
                        proxyHost, proxyPort);
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(mContext, mHeaders, contentUri,
                        proxyHost, proxyPort);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    public void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);
            player.setMetadataListener(this);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(mSurfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
            mBufferedPercentage = 0;
        }
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch(playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                mBufferingUpdateListener.onBufferingUpdate(null, player.getBufferedPercentage());
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                mCompletionListener.onCompletion(null);
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                mPreparedListener.onPrepared(null);
                break;
            default:
                text += "unknown";
                break;
        }
    }

    @Override
    public void onError(Exception e) {
        String errorString = null;
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            errorString = mContext.getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
        } else if (e instanceof ExoPlaybackException
                && e.getCause() instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
            // Special case for decoder initialization failures.
            MediaCodecTrackRenderer.DecoderInitializationException decoderInitializationException =
                    (MediaCodecTrackRenderer.DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    errorString = mContext.getString(R.string.error_querying_decoders);
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = mContext.getString(R.string.error_no_secure_decoder,
                            decoderInitializationException.mimeType);
                } else {
                    errorString = mContext.getString(R.string.error_no_decoder,
                            decoderInitializationException.mimeType);
                }
            } else {
                errorString = mContext.getString(R.string.error_instantiating_decoder,
                        decoderInitializationException.decoderName);
            }
        }
        if (errorString != null) {
            Toast.makeText(mContext.getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
        showControls();

        mErrorListener.onError(null, MediaPlayer.MEDIA_ERROR_UNKNOWN, MediaPlayer.MEDIA_ERROR_UNSUPPORTED);
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoSizeChangedListener.onVideoSizeChanged(null, width, height);
    }

    // DemoPlayer.MetadataListener implementation

    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
        for (Id3Frame id3Frame : id3Frames) {
            if (id3Frame instanceof TxxxFrame) {
                TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id,
                        txxxFrame.description, txxxFrame.value));
            } else if (id3Frame instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
            } else if (id3Frame instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
            }
        }
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }
    /**
     * Makes a best guess to infer the type from a media {@link Uri} and an optional overriding file
     * extension.
     *
     * @param uri The {@link Uri} of the media.
     * @param fileExtension An overriding file extension.
     * @return The inferred type.
     */
    private static int inferContentType(Uri uri, String fileExtension) {
        String lastPathSegment = !TextUtils.isEmpty(fileExtension) ? "." + fileExtension
                : uri.getLastPathSegment();
        return Util.inferContentType(lastPathSegment);
    }

    private static final class KeyCompatibleMediaController extends MediaController {

        private MediaController.MediaPlayerControl playerControl;

        public KeyCompatibleMediaController(Context context) {
            super(context);
        }

        @Override
        public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
            super.setMediaPlayer(playerControl);
            this.playerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (playerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
                    show();
                }
                return true;
            } else if (playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
                    show();
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }

    private void toggleControlsVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaController.show(0);
    }

    /**
     * Get the current activity passed from callers. It's never null.
     * @return the activity instance passed from callers.
     *
     * @hide
     */
    public Activity getActivity() {
        if (mContext instanceof Activity) {
            return (Activity) mContext;
        }

        // Never achieve here.
        assert(false);
        return null;
    }

    private Activity addContentView(View view) {
    	HomeActivity activity = (HomeActivity) getActivity();

        if (activity != null) {
            activity.onFullscreenToggled(true);
        }
//        FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
//        decor.addView(view, 0,
//                new FrameLayout.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT,
//                        Gravity.CENTER));

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlsVisibility();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return true;
            }
        });
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                        || keyCode == KeyEvent.KEYCODE_MENU) {
                    return false;
                }
                return mediaController.dispatchKeyEvent(event);
            }
        });
        mediaController.setAnchorView(view);

        return activity;
    }

    /**
     * Notify the host application that the current page would
     * like to show a custom View in a particular orientation.
     * @param view is the View object to be shown.
     * @param requestedOrientation An orientation constant as used in
     * {@link ActivityInfo#screenOrientation ActivityInfo.screenOrientation}.
     * @param callback is the callback to be invoked if and when the view
     * is dismissed.
     */
    public void onShowCustomView(int requestedOrientation) {
        Activity activity = addContentView(mSurfaceView);
        if (activity == null) return;

        final int orientation = activity.getResources().getConfiguration().orientation;

        if (requestedOrientation != orientation &&
                requestedOrientation >= ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED &&
                requestedOrientation <= ActivityInfo.SCREEN_ORIENTATION_LOCKED) {
            mPreOrientation = orientation;
            activity.setRequestedOrientation(requestedOrientation);
        }
    }

    /**
     * Notify the host application that the current page would
     * like to hide its custom view.
     */
    public void onHideCustomView() {
        if (player != null) {
            player.setBackgrounded(true);
        }

        if (mCustomFullscreen) {
            HomeActivity activity = (HomeActivity) getActivity();

            if (activity != null) {
                activity.onFullscreenToggled(false);
            }

            // Remove video view from activity's ContentView.
//            FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
//            decor.removeView(mSurfaceView);

            if (mPreOrientation != INVALID_ORIENTATION &&
                    mPreOrientation >= ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED &&
                    mPreOrientation <= ActivityInfo.SCREEN_ORIENTATION_LOCKED) {
                activity.setRequestedOrientation(mPreOrientation);
                mPreOrientation = INVALID_ORIENTATION;
            }
            
            mSurfaceView.setVisibility(View.INVISIBLE);
//            mSurfaceView.setOnClickListener(null);
//            mSurfaceView.setOnTouchListener(null);
            mediaController.hide();
            mCustomFullscreen = false;
//        player.setSurface(xwalkSurface);
            player.setBackgrounded(false);
            player.setPlayWhenReady(false);
        }
    }

    public void setBackgrounded(boolean background) {
        if (player != null) {
            player.setBackgrounded(background);
        }
    }
}
