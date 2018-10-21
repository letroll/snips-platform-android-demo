package ai.snips.snipsdemo

import androidx.lifecycle.*

fun <T> LifecycleOwner.removeObservers(liveData: LiveData<T>) {
    this.removeObservers(liveData)
}

inline fun <T> LifecycleOwner.observe(
        liveData: LiveData<T>,
        crossinline observer: (t: T?) -> Unit
): Observer<T> = Observer<T> {
    observer(it)
}.also { liveData.observe(this, it) }

inline fun <T : Any> LifecycleOwner.observeNotNull(
        liveData: LiveData<T>,
        crossinline observer: (t: T) -> Unit
): Observer<T> = Observer<T> {
    if (it != null) observer(it)
}.also { liveData.observe(this, it) }

@JvmName("observeWithLiveDataOfNullable")
inline fun <T : Any> LifecycleOwner.observeNotNull(
        liveData: LiveData<T?>,
        crossinline observer: (t: T) -> Unit
): Observer<T?> = Observer<T?> {
    if (it != null) observer(it)
}.also { liveData.observe(this, it) }

fun <T> LifecycleOwner.removeObservers(liveData: MutableLiveData<T>) {
    this.removeObservers(liveData)
}

inline fun <T> LifecycleOwner.observe(
        mutableLiveData: MutableLiveData<T>,
        crossinline observer: (t: T?) -> Unit
): Observer<T> = Observer<T> {
    observer(it)
}.also { mutableLiveData.observe(this, it) }

inline fun <T : Any> LifecycleOwner.observeNotNull(
        mutableLiveData: MutableLiveData<T>,
        crossinline observer: (t: T) -> Unit
): Observer<T> = Observer<T> {
    if (it != null) observer(it)
}.also { mutableLiveData.observe(this, it) }

/**
 * Applies the given function on the main thread to each value emitted by source
 * LiveData and returns LiveData, which emits resulting values.
 *
 * The given function [transform] will be **executed on the main thread**.
 *
 * @param transform   a function to apply
 * @param X           a type of source LiveData
 * @param Y           a type of resulting LiveData.
 * @return            a LiveData which emits resulting values
 */
inline fun <X, Y> LiveData<X>.map(
        crossinline transform: (X?) -> Y
): LiveData<Y> = Transformations.map(this) { input -> transform(input) }

inline fun <X, Y> LiveData<X>.mapNotNull(
        crossinline transform: (X) -> Y
): LiveData<Y> = Transformations.map(this) { input: X? ->
    input?.let { transform(it) }
}

inline fun <X, Y> LiveData<X>.switchMap(
        crossinline transform: (X?) -> LiveData<Y>?
): LiveData<Y> = Transformations.switchMap(this) { input -> transform(input) }

inline fun <X, Y> LiveData<X>.switchMapNotNull(
        crossinline transform: (X) -> LiveData<Y>?
): LiveData<Y> = Transformations.switchMap(this) { input: X? ->
    input?.let { transform(it) }
}