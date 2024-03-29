package id.faris.services.posts.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.faris.services.posts.data.Post
import id.faris.services.posts.data.source.PostRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject


class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository) : ViewModel() {

    var postsResult: MutableLiveData<List<Post>> = MutableLiveData()
    var postsError: MutableLiveData<String> = MutableLiveData()
    var postsLoader: MutableLiveData<Boolean> = MutableLiveData()
    lateinit var disposableObserver: DisposableObserver<List<Post>>

    fun postsResult(): LiveData<List<Post>> {
        return postsResult
    }

    fun postsLoader(): LiveData<Boolean> {
        return postsLoader
    }

    fun postsError(): LiveData<String> {
        return postsError
    }

    fun loadPosts(limit:Int, offset:Int) {

        disposableObserver = object : DisposableObserver<List<Post>>() {
            override fun onComplete() {

            }

            override fun onNext(posts: List<Post>) {
                postsResult.postValue(posts)
                postsLoader.postValue(false)
            }

            override fun onError(e: Throwable) {
                postsError.postValue(e.message)
                postsLoader.postValue(false)
            }
        }

        postRepository.getPosts(limit, offset)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .debounce(400, MILLISECONDS)
            .subscribe(disposableObserver)
    }

    fun disposeElements(){
        if(null != disposableObserver && !disposableObserver.isDisposed) disposableObserver.dispose()
    }

}
