package com.walhalla.data.repository

import android.content.Context
import android.os.Handler
import com.walhalla.data.model.Channel
import com.walhalla.ui.DLog.d
import com.walhalla.ui.DLog.handleException

class AllChannelPresenter
//    protected final Handler mThread;
//    public final LocalDatabaseRepo repo;
//    protected final ExecutorService executor;
//    private final Context context;
//
//
//    public AllChannelPresenter(Handler handler, Context context) {
//        this.context = context;
//        this.mThread = handler;
//        this.repo = LocalDatabaseRepo.getStoreInfoDatabase(context, handler);
//        this.executor = Executors.newSingleThreadExecutor();
//    }
    (handler: Handler, context: Context) : BasePresenter(handler, context) {
    fun getAllChannels(sortOption: Int, callback: RepoCallback<MutableList<Channel?>?>) {
        executeInBackground(Runnable {
            try {
                val channels = db_repo.getAllChannels(sortOption)
                postToMainThread(Runnable {
                    callback.successResult(channels)
                })
            } catch (e: Exception) {
                postToMainThread(Runnable {
                    callback.errorResult(e.message!!)
                })
            }
        })
    }

    fun getChannelById(id: Long, callback: RepoCallback<Channel>) {
        executeInBackground(Runnable {
            try {
                val channel = db_repo.getChannelById(id)
                postToMainThread(Runnable {
                    callback.successResult(channel)
                })
            } catch (e: Exception) {
                postToMainThread(Runnable {
                    callback.errorResult(e.message!!)
                })
            }
        })
    }

    fun getChannelsInCategory(categoryName: String, callback: RepoCallback<MutableList<Channel>>) {
        executeInBackground(Runnable {
            try {
                val channels = db_repo.getCategory(categoryName)
                postToMainThread(Runnable {
                    callback.successResult(channels)
                })
            } catch (e: Exception) {
                postToMainThread(Runnable {
                    callback.errorResult(e.message!!)
                })
            }
        })
    }


    fun isFavoriteChannel(channel: Channel, callback: RepoCallback<Boolean?>) {
        executeInBackground(Runnable {
            try {
                //return favoriteDatabase.favoriteDao().isFavorite(channel._id) == 1;
                val mm = db_repo.isFavorite(channel._id)
                d(mm.toString())

                postToMainThread {
                    callback.successResult(mm == 1)
                }
            } catch (e: Exception) {
                postToMainThread(Runnable {
                    callback.errorResult(e.message!!)
                    callback.successResult(false)
                })
            }
        })
    }

    fun addFavorite(channel: Channel, callback: RepoCallback<Int>) {
        executeInBackground {
            try {
                val m = db_repo.addFavorite(channel)
                postToMainThread(Runnable {
                    callback.successResult(m)
                })
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun deleteFavorite(channel: Channel) {
        executeInBackground(Runnable {
            try {
                db_repo.deleteFavorite(channel)
            } catch (e: Exception) {
                handleException(e)
            }
        })
    }

    fun getAllFavorite(callback: RepoCallback<MutableList<Channel>>) {
        executeInBackground {
            try {
                val m = db_repo.getFavorite(-1)
                postToMainThread {
                    callback.successResult(m)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun searchChannel(categoryName: String, callback: RepoCallback<MutableList<Channel>>) {
        executeInBackground(Runnable {
            try {
                val m = db_repo.searchChannel(categoryName)
                postToMainThread(Runnable {
                    callback.successResult(m)
                })
            } catch (e: Exception) {
                handleException(e)
            }
        })
    }
}
