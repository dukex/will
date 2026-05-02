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

    /**
     * Most recent grant snapshot we have observed. Used to detect
     * `false → true` transitions in [refresh] so we only auto-advance when
     * the user *just* granted the current step's permission, not when it
     * was already granted (e.g. left over from a prior install or held
     * across a rerun).
     */
    private var lastObserved: OnboardingState = inspector.snapshot()

    private val _state = MutableStateFlow(lastObserved.advanceToFirstUngranted())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    /**
     * Re-read system grants. Auto-advances by one step iff the current
     * step's grant transitioned `false → true` since the last observation.
     * Idempotent under double-fire (onResume + ActivityResult callback).
     */
    fun refresh() {
        val snapshot = inspector.snapshot()
        val current = _state.value
        val refreshed = current.copy(
            isDefaultLauncher = snapshot.isDefaultLauncher,
            hasOverlayPermission = snapshot.hasOverlayPermission,
            hasNotificationListener = snapshot.hasNotificationListener
        )

        val grantJustObtained = when (current.step) {
            OnboardingStep.DefaultLauncher ->
                !lastObserved.isDefaultLauncher && snapshot.isDefaultLauncher
            OnboardingStep.Overlay ->
                !lastObserved.hasOverlayPermission && snapshot.hasOverlayPermission
            OnboardingStep.Notifications ->
                !lastObserved.hasNotificationListener && snapshot.hasNotificationListener
            OnboardingStep.Done -> false
        }

        lastObserved = snapshot
        _state.value = if (grantJustObtained) refreshed.next() else refreshed
        persistIfDone()
    }

    /**
     * Advance to the next step. Called by the screen's primary action when
     * the current step's grant is already in (e.g. mid-rerun) — the user is
     * acknowledging they have done what the step asks for.
     */
    fun next() {
        val current = _state.value
        if (!current.isStepSatisfied()) return
        _state.value = current.next()
        persistIfDone()
    }

    /**
     * Skip the current step. Only legal on optional steps.
     */
    fun skip() {
        val current = _state.value
        if (!current.canSkip) return
        _state.value = current.next()
        persistIfDone()
    }

    /**
     * Walk the user back through the entire flow. Anchors at the first step
     * regardless of what is already granted, so the user can review every
     * screen and hit "continue" through the ones they have already cleared.
     */
    fun rerun() {
        val snapshot = inspector.snapshot()
        lastObserved = snapshot
        _state.value = snapshot.copy(step = OnboardingStep.DefaultLauncher)
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
