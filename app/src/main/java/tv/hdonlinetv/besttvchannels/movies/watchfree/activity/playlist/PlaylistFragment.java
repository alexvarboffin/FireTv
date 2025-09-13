package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.walhalla.data.model.PlaylistImpl;

import com.walhalla.ui.DLog;

import java.util.ArrayList;
import java.util.List;

import tv.hdonlinetv.besttvchannels.movies.watchfree.PlaylistAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.TypeUtils;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentPlaylistBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.LoadingDialogFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.PlaylistInfoDialogFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.PlaylistManagementView;

import com.walhalla.data.repository.PlaylistPresenterImpl;
import com.walhalla.data.repository.RepoCallback;

public class PlaylistFragment extends BaseFragment implements PlaylistManagementView {

    private FragmentPlaylistBinding binding;
    private PlaylistAdapter adapter;
    //private List<Playlist> playlistList;

    private PlaylistPresenterImpl presenter;
    private LoadingDialogFragment loadingDialog;
    private final String THISCLAZZNAME = getClass().getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new PlaylistPresenterImpl(handler, this, getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        long noW = System.currentTimeMillis()/100;
//        playlistList.add(new Playlist("chs.m3u", "ss", noW, 4,
//                true));
//        playlistList.add(new Playlist("xxx.m3u", "ss", noW, 5,
//                true));
//        playlistList.add(new Playlist("Rus.m3u", "ss", noW, 6,
//                true));
        adapter = new PlaylistAdapter(getActivity(), null, new PlaylistAdapter.OnPlaylistActionListener() {
            @Override
            public void onEdit(PlaylistImpl playlist) {
                PlaylistInfoDialogFragment dialogFragment = new PlaylistInfoDialogFragment(playlist);
                dialogFragment.show(getChildFragmentManager(), "PlaylistInfoDialogFragment");
            }

            @Override
            public void onDelete(PlaylistImpl playlist, int absoluteAdapterPosition) {
                presenter.deletePlaylistAndRelatedChannels(playlist, new RepoCallback<>() {
                    @Override
                    public void successResult(Integer m) {
                        if (m > 0) {
                            adapter.onDelete(playlist, absoluteAdapterPosition);
                            setBadgeText(THISCLAZZNAME,
                                    String.valueOf(adapter.getItemCount()));
                        }
//                Toast.makeText(getContext(),
//                        String.format("Removed Items: %s", m), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void errorResult(String err) {

                    }
                });

            }

            @Override
            public void onItemClick(PlaylistImpl playlist, int absoluteAdapterPosition) {
                if (TypeUtils.TYPE_XTREAM_URL == playlist.type) {
                    startActivity(PlaylistXtreamActivity.newInstance(getActivity(), playlist));
                } else {
                    startActivity(PlaylistActivity.newInstance(getActivity(), playlist));
                }
            }

            @Override
            public void onUpdate(PlaylistImpl playlist, int absoluteAdapterPosition) {
                //String fileName = playlist.getFileName();
                presenter.loadAndSavePlaylist(playlist);
            }
        });

        // Настройка RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        presenter.selectAllPlaylist(new RepoCallback<List<PlaylistImpl>>() {
            @Override
            public void successResult(List<PlaylistImpl> playlists) {
                DLog.d("@@" + playlists);
                PlaylistFragment.this.adapter.swapData(playlists);
                PlaylistFragment.this.setBadgeText(THISCLAZZNAME, String.valueOf(playlists.size()));
            }

            @Override
            public void errorResult(String err) {
                Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void showPlaylistName(String name) {

    }

    @Override
    public void showValidationError(Integer playlistName, Integer playlistUrl, Integer userName, Integer password) {

    }


    @Override
    public void showToast(int resId) {

    }

    @Override
    public void updatePlaylistContent(String content) {

    }

    @Override
    public void showErrorToast(int res) {
        Toast.makeText(getContext(), res, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlaylistUpdated() {
        presenter.selectAllPlaylist(new RepoCallback<List<PlaylistImpl>>() {
            @Override
            public void successResult(List<PlaylistImpl> mm) {
                DLog.d("@@" + mm);
                List<PlaylistImpl> playlists = new ArrayList<>(mm);
                PlaylistFragment.this.setBadgeText(THISCLAZZNAME, String.valueOf(playlists.size()));
                PlaylistFragment.this.adapter.swapData(playlists);
            }

            @Override
            public void errorResult(String err) {

            }
        });
    }

    @Override
    public void showProgressBar() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialogFragment();
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show(getParentFragmentManager(), "loading_dialog");
    }

    @Override
    public void hideProgressBar() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void showError0(String localizedMessage) {

    }
}

