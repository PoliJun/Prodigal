package bob.sun.mpod.fragments;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.squareup.picasso.Picasso;

import bob.sun.mpod.R;
import bob.sun.mpod.controller.OnTickListener;
import bob.sun.mpod.controller.PlayingListener;
import bob.sun.mpod.model.MediaLibrary;
import bob.sun.mpod.model.SelectionDetail;
import bob.sun.mpod.model.SongBean;
import bob.sun.mpod.utils.VolumeUtil;


/**
 * Created by sunkuan on 15/5/4.
 */
public class NowPlayingFragment extends Fragment implements OnTickListener, PlayingListener {

    enum ViewMode {
        Playing,
        Volume,
        Seek,
    }

    SongBean song;
    View view, contentView, seekView;
    NumberProgressBar progressView, seeker;
    TextView currentTime, totalTime, seekerTitle;
    Runnable dismissRunnable;
    VolumeUtil volume;
    ViewMode viewMode;

    private long lastTick;

    public NowPlayingFragment() {
        viewMode = ViewMode.Playing;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup parent,
                             Bundle savedInstanceState
                             ){
        View ret = layoutInflater.inflate(R.layout.layout_now_playing,parent,false);
        view = ret;
        contentView = view.findViewById(R.id.id_now_playing_wrapper);
        seekView = view.findViewById(R.id.id_seeker_wrapper);
        progressView = (NumberProgressBar) view.findViewById(R.id.id_progress_view);
        seeker = (NumberProgressBar) view.findViewById(R.id.id_seeker);
        seekerTitle = (TextView) view.findViewById(R.id.id_seeker_title);
        currentTime = (TextView) view.findViewById(R.id.current_time);
        totalTime = (TextView) view.findViewById(R.id.total_time);
        volume = VolumeUtil.getStaticInstance(getActivity());
        return ret;
    }

    public void setSong(SongBean songBean){
        song = songBean;
        ((TextView) view.findViewById(R.id.id_now_playing_text_view_title)).setText(song.getTitle());
        ((TextView) view.findViewById(R.id.id_now_playing_text_view_artist)).setText(song.getArtist());
        ((TextView) view.findViewById(R.id.id_now_playing_text_view_album)).setText(song.getAlbum());
        progressView.setMax(100);
        progressView.setProgress(0);
        String img = MediaLibrary.getStaticInstance(view.getContext())
                .getCoverUriByAlbumId(songBean.getAlbumId());
        Picasso.with(view.getContext())
                .load(Uri.parse(img))
                .placeholder(R.drawable.album)
                .config(Bitmap.Config.RGB_565)
                .into((ImageView) view.findViewById(R.id.id_nowplaying_image_view_cover));

        viewMode = ViewMode.Playing;

        dismissRunnable = new Runnable() {
            @Override
            public void run() {
                long current = System.currentTimeMillis();
                if (current - lastTick > 2000) {
                    setViewMode(ViewMode.Playing);

                    seekView.setVisibility(View.INVISIBLE);
                    viewMode = ViewMode.Playing;
                }
            }
        };
    }

    @Override
    public void onNextTick() {
        lastTick = System.currentTimeMillis();
        switch (viewMode) {
            case Playing:
                setViewMode(ViewMode.Volume);
                break;
            case Volume:
                volume.raiseVolume();
                seeker.setProgress(volume.getCurrent());
                break;
            case Seek:
                //Seek here
                break;
        }
        seekView.postDelayed(dismissRunnable, 2000);
    }

    @Override
    public void onPreviousTick() {
        lastTick = System.currentTimeMillis();
        switch (viewMode) {
            case Playing:
                setViewMode(ViewMode.Volume);
                break;
            case Volume:
                volume.reduceVolume();
                seeker.setProgress(volume.getCurrent());
                break;
            case Seek:
                //Seek here
                break;
        }
        seekView.postDelayed(dismissRunnable, 2000);
    }

    @Override
    public SelectionDetail getCurrentSelection() {
        //Switch view mode here
        switch (viewMode) {
            case Playing:
                this.setViewMode(ViewMode.Seek);
                break;
            case Volume:
                this.setViewMode(ViewMode.Playing);
                seekView.postDelayed(dismissRunnable, 2000);
                break;
            case Seek:
                this.setViewMode(ViewMode.Playing);
                seekView.postDelayed(dismissRunnable, 2000);
                break;
        }
        return null;
    }

    @Override
    public void onSongChanged(SongBean bean) {
        setSong(bean);
    }

    @Override
    public void onProcessChanged(final int current, final int total) {
        if (progressView != null )
        view.post(new Runnable() {
            @Override
            public void run() {
                progressView.setProgress((int) (((float)current / (float)total) * 100));
                currentTime.setText(String.format("%02d:%02d", (current / 1000 / 60), (current / 1000 % 60)));
                totalTime.setText(String.format("%02d:%02d", (total / 1000 / 60), (total / 1000 % 60)));
            }
        });

    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        lastTick = 0;
        switch (viewMode) {
            case Playing:
                seekView.setVisibility(View.GONE);
                break;
            case Volume:
                seeker.setMax(volume.getMax());
                seeker.setProgress(volume.getCurrent());
                seekView.setVisibility(View.VISIBLE);
                seekerTitle.setText("Volume");
                break;
            case Seek:
                seeker.setMax(progressView.getMax());
                seeker.setProgress(progressView.getProgress());
                seekView.setVisibility(View.VISIBLE);
                seekerTitle.setText("Seek");
                break;
        }
        return;
    }
}
