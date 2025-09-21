package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.playlist

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.walhalla.data.model.PlaylistImpl
import com.walhalla.data.repository.PlaylistPresenterImpl
import com.walhalla.data.repository.RepoCallback
import com.walhalla.ui.DLog.d
import tv.hdonlinetv.besttvchannels.movies.watchfree.PlaylistAdapter
import tv.hdonlinetv.besttvchannels.movies.watchfree.PlaylistAdapter.OnPlaylistActionListener
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.TypeUtils
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentPlaylistBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.LoadingDialogFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.PlaylistInfoDialogFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.PlaylistManagementView

class PlaylistFragment : BaseFragment(), PlaylistManagementView {
    private var binding: FragmentPlaylistBinding? = null
    private var adapter: PlaylistAdapter? = null

    //private List<Playlist> playlistList;
    private var presenter: PlaylistPresenterImpl? = null
    private var loadingDialog: LoadingDialogFragment? = null
    private val THISCLAZZNAME: String = javaClass.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler(Looper.getMainLooper())
        presenter = PlaylistPresenterImpl(handler, this, requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //        long noW = System.currentTimeMillis()/100;
//        playlistList.add(new Playlist("chs.m3u", "ss", noW, 4,
//                true));
//        playlistList.add(new Playlist("xxx.m3u", "ss", noW, 5,
//                true));
//        playlistList.add(new Playlist("Rus.m3u", "ss", noW, 6,
//                true));
        adapter = PlaylistAdapter(requireActivity(), null, object : OnPlaylistActionListener {
            override fun onEdit(playlist: PlaylistImpl) {
                val dialogFragment = PlaylistInfoDialogFragment(playlist)
                dialogFragment.show(childFragmentManager, "PlaylistInfoDialogFragment")
            }

            override fun onDelete(playlist: PlaylistImpl, absoluteAdapterPosition: Int) {
                presenter!!.deletePlaylistAndRelatedChannels(playlist, object : RepoCallback<Int> {
                    override fun successResult(m: Int) {
                        if (m > 0) {
                            adapter!!.onDelete(playlist, absoluteAdapterPosition)
                            setBadgeText(
                                THISCLAZZNAME,
                                adapter!!.itemCount.toString()
                            )
                        }
                        //                Toast.makeText(getContext(),
//                        String.format("Removed Items: %s", m), Toast.LENGTH_SHORT).show();
                    }

                    override fun errorResult(err: String) {
                    }
                })
            }

            override fun onItemClick(playlist: PlaylistImpl, absoluteAdapterPosition: Int) {
                if (TypeUtils.TYPE_XTREAM_URL == playlist.type) {
                    startActivity(PlaylistXtreamActivity.newInstance(activity, playlist))
                } else {
                    startActivity(PlaylistActivity.newInstance(activity, playlist))
                }
            }

            override fun onUpdate(playlist: PlaylistImpl, absoluteAdapterPosition: Int) {
                //String fileName = playlist.getFileName();
                presenter!!.loadAndSavePlaylist(playlist)
            }
        })

        // Настройка RecyclerView
        binding!!.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding!!.recyclerView.adapter = adapter

        presenter!!.selectAllPlaylist(object : RepoCallback<List<PlaylistImpl>> {
            override fun successResult(playlists: List<PlaylistImpl>) {
                d("@@$playlists")
                adapter!!.swapData(playlists)
                this@PlaylistFragment.setBadgeText(THISCLAZZNAME, playlists.size.toString())
            }

            override fun errorResult(err: String) {
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun showPlaylistName(name: String) {
    }

    override fun showValidationError(
        playlistName: Int,
        playlistUrl: Int,
        userName: Int,
        password: Int
    ) {
    }


    override fun showToast(resId: Int) {
    }

    override fun updatePlaylistContent(content: String) {
    }

    override fun showErrorToast(res: Int) {
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
    }

    override fun onPlaylistUpdated() {
        presenter!!.selectAllPlaylist(object : RepoCallback<List<PlaylistImpl>> {
            override fun successResult(mm: List<PlaylistImpl>) {
                d("@@$mm")
                val playlists: List<PlaylistImpl> = ArrayList(mm)
                this@PlaylistFragment.setBadgeText(THISCLAZZNAME, playlists.size.toString())
                adapter!!.swapData(playlists)
            }

            override fun errorResult(err: String) {
            }
        })
    }

    override fun showProgressBar() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialogFragment()
            loadingDialog!!.isCancelable = false
        }
        loadingDialog!!.show(parentFragmentManager, "loading_dialog")
    }

    override fun hideProgressBar() {
        if (loadingDialog != null) {
            loadingDialog!!.dismiss()
        }
    }

    override fun showError0(localizedMessage: String) {
    }
}

