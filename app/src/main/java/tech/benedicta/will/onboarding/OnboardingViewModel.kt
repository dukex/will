package tech.benedicta.will.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: OnboardingRepository,
    private val inspector: PermissionInspector,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _state = MutableStateFlow(inspector.snapshot().advanceToFirstUngranted())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    /**
     * Re-read the system grants. Call from the host's onResume — the user may
     * have flipped a toggle in Settings while the onboarding screen was paused.
     */
    fun refresh() {
        val snapshot = inspector.snapshot()
        val current = _state.value
        val refreshed = current.copy(
            isDefaultLauncher = snapshot.isDefaultLauncher,
            hasOverlayPermission = snapshot.hasOverlayPermission,
            hasNotificationListener = snapshot.hasNotificationListener
        )
        _state.value = if (refreshed.isStepSatisfied()) refreshed.next() else refreshed
        persistIfDone()
    }

    /**
     * Advance manually after the user taps "next" on a step that has been
     * cleared. Required steps refuse to advance unless the grant is in.
     */
    fun next() {
        val current = _state.value
        if (!current.isStepSatisfied()) return
        _state.value = current.next()
        persistIfDone()
    }

    /**
     * Skip the current step. Only legal on optional steps; required steps
     * (default launcher, overlay) cannot be skipped.
     */
    fun skip() {
        val current = _state.value
        if (!current.canSkip) return
        _state.value = current.next()
        persistIfDone()
    }

    /**
     * Force the user back through onboarding from settings. Resets the
     * persisted completion flag and re-anchors the in-memory state at the
     * first ungranted step.
     */
    fun rerun() {
        _state.value = inspector.snapshot().advanceToFirstUngranted()
        viewModelScope.launch(dispatcher) { repository.reset() }
    }

    private fun persistIfDone() {
        if (_state.value.isComplete) {
            viewModelScope.launch(dispatcher) { repository.markCompleted() }
        }
    }

    private fun OnboardingState.isStepSatisfied(): Boolean = when (step) {
        OnboardingStep.DefaultLauncher -> isDefaultLauncher
        OnboardingStep.Overlay -> hasOverlayPermission
        OnboardingStep.Notifications -> hasNotificationListener
        OnboardingStep.Done -> true
    }

    class Factory(
        private val repository: OnboardingRepository,
        private val inspector: PermissionInspector
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OnboardingViewModel(repository, inspector) as T
    }
}
