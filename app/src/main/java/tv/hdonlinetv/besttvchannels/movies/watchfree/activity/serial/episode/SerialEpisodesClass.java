package tv.hdonlinetv.besttvchannels.movies.watchfree.activity.serial.episode;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.Constant.INTERSTITIAL_POST_LIST;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.m3u.data.database.model.ResponseData;
import com.walhalla.data.model.Channel;
import com.walhalla.data.repository.RepoCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.ChannelAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.adapter.EpisodeAdapter;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.FragmentHomeBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.BaseFragment;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

public class SerialEpisodesClass
        extends BaseFragment implements RepoCallback<List<String>>, ChannelAdapter.OnItemClickListener {
    protected final String THISCLAZZNAME = getClass().getSimpleName();

    String tmp;
    protected static final String KEY_INTENT_CHANNEL_ID = "key_data1";
    protected FragmentHomeBinding binding;
    private PrefManager prefManager;
    private AdsPref adsPref;
    private AdNetwork adNetwork;
    private EpisodeAdapter adapter;

    Map<String, List<ResponseData.Episode>> map = new HashMap<>();
    private OnEpisodeSelectedListener listener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getArguments();
        if (intent != null && intent.containsKey(KEY_INTENT_CHANNEL_ID)) {
            tmp = getArguments().getString(KEY_INTENT_CHANNEL_ID);
        }
        //LinkedTreeMap cannot be cast to java.util.List
        if (tmp != null) {

            // Convert the JSON string back to Map
            Gson gson = new Gson();
//            m = gson.fromJson(data,
//                    new com.google.gson.reflect.TypeToken<Map<String, List<ResponseData.Episode>>>() {
//                    }.getType());

            JsonObject root = gson.fromJson(tmp, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                // Выводим ключ и значение

                if (value instanceof JsonArray) {
                    JsonArray jsonArray = (JsonArray) value;
//                    for (int i = 0; i < jsonArray.size(); i++) {
//                        DLog.d("@@@@ Key: " + key + ", Value: " + value.get);
//                    }

                    List<ResponseData.Episode> episodes = gson.fromJson(jsonArray,
                            new com.google.gson.reflect.TypeToken<List<ResponseData.Episode>>() {
                            }.getType());
                    map.put(key, episodes);

                    // Теперь можно работать с episodes
//                    for (ResponseData.Episode episode : episodes) {
//                        //DLog.d("@@@@ Key: " + key + ", Episode: " + episode.toString());
//                    }
                }
            }
//            JsonArray ja  = root.getAsJsonArray();
//
//            for(JsonElement j : ja){
//                //here use the json to parse into your custom object
//            }
        }
    }

//    public static <T> List<T> parseGsonArray(String json, Class<T[]> model) {
//        return Arrays.asList(new Gson().fromJson(json, model));
//    }


    protected void loadCategory() {
//        setBadgeText(THISCLAZZNAME, String.valueOf(data.size()));
//        categoriesAdapter.swapData(data);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        prefManager = new PrefManager(getActivity());
        adsPref = new AdsPref(getActivity());
        adNetwork = new AdNetwork(getActivity());
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

//        showRefresh(true);
//        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshData());

        binding.recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerView.setLayoutManager(gridLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), gridLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.custom_divider));
        binding.recyclerView.addItemDecoration(dividerItemDecoration);


        List<Object> items = new ArrayList<>();
        for (Map.Entry<String, List<ResponseData.Episode>> entry : map.entrySet()) {
            items.add("Season " + entry.getKey());
            items.addAll(entry.getValue());
        }

        adapter = new EpisodeAdapter(items, new EpisodeAdapter.OnEpisodeClickListener() {
            @Override
            public void onEpisodeClick(ResponseData.Episode episode) {
                if (listener != null) {
                    listener.onEpisodeSelected(episode);
                }
            }
        });
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == EpisodeAdapter.TYPE_HEADER) {
                    return 2; // Заголовок занимает 2 колонки
                } else {
                    return 1; // Эпизод занимает 1 колонку
                }
            }
        });
        binding.recyclerView.setAdapter(adapter);

        loadCategory();
        return view;
    }


    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }

    @Override
    public void onResume() {
        super.onResume();

        //binding.log.setText("@@@" + new GsonBuilder().setPrettyPrinting().create().toJson(tmp));
    }

    public static Fragment newInstance(Map<String, List<ResponseData.Episode>> episodes) {
        SerialEpisodesClass fragment = new SerialEpisodesClass();
        Bundle args = new Bundle();

        // Convert Map to JsonObject and then to String
        Gson gson = new Gson();
        String jsonString = gson.toJson(episodes);

        // Put the JSON string into the Bundle
        args.putString(KEY_INTENT_CHANNEL_ID, jsonString);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onItemClick(View view, Channel obj, int position) {

    }

    @Override
    public void successResult(List<String> data) {

    }

    @Override
    public void errorResult(String err) {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnEpisodeSelectedListener) {
            listener = (OnEpisodeSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnEpisodeSelectedListener");
        }
    }

    public interface OnEpisodeSelectedListener {
        void onEpisodeSelected(ResponseData.Episode episode);
    }
}
