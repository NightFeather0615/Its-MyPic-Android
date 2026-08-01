package dev.nightfeather.its_mypic

import android.view.View
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ComposeViewOwner :
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner, OnBackPressedDispatcherOwner
{
    override val onBackPressedDispatcher = OnBackPressedDispatcher()

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    private var recomposer: Recomposer? = null
    private var recomposeScope: CoroutineScope? = null
    private var isCreated = false
    private var isDestroyed = false

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore
        get() = store

    fun onCreate() {
        if (isCreated || isDestroyed) return
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        isCreated = true
    }

    fun onStart() {
        if (!isCreated || isDestroyed) return
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun onResume() {
        if (!isCreated || isDestroyed) return
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onPause() {
        if (!isCreated || isDestroyed) return
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    fun onStop() {
        if (!isCreated || isDestroyed) return
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    fun onDestroy() {
        if (isDestroyed) return
        isDestroyed = true

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        recomposer?.close()
        recomposeScope?.cancel()
        recomposer = null
        recomposeScope = null
    }

    fun attachToDecorView(decorView: View?) {
        if (decorView == null || isDestroyed) return

        decorView.let {
            it.setViewTreeViewModelStoreOwner(this)
            it.setViewTreeLifecycleOwner(this)
            it.setViewTreeSavedStateRegistryOwner(this)
            it.setViewTreeOnBackPressedDispatcherOwner(this)
        }

        if (recomposer != null) return

        val coroutineContext = AndroidUiDispatcher.CurrentThread
        val newRecomposeScope = CoroutineScope(coroutineContext)
        val newRecomposer = Recomposer(coroutineContext)

        decorView.compositionContext = newRecomposer
        newRecomposeScope.launch {
            newRecomposer.runRecomposeAndApplyChanges()
        }

        recomposer = newRecomposer
        recomposeScope = newRecomposeScope
    }
}
