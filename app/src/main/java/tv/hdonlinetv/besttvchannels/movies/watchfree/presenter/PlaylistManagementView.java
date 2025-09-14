package tv.hdonlinetv.besttvchannels.movies.watchfree.presenter;

public interface PlaylistManagementView {
    void showPlaylistName(String name);


    void showValidationError(
            Integer playlistName
            , Integer playlistUrl
            , Integer userName
            , Integer password
    );

    void showToast(int resId);
    void updatePlaylistContent(String content);

    void showErrorToast(int clipboardContainsNoText);

    void onPlaylistUpdated();

    void showProgressBar();

    void hideProgressBar();

    void showError0(String localizedMessage);
}