package com.kxsv.schooldiary.app.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

/**
 * An entry point to retrieve the [HiltWorkerFactory] at runtime
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
	fun hiltWorkerFactory(): HiltWorkerFactory
}

private const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"

/**
 * Adds metadata to a WorkRequest to identify what [CoroutineWorker] the [DelegatingWorker] should
 * delegate to
 */
internal fun KClass<out CoroutineWorker>.delegatedData() =
	Data.Builder()
		.putString(WORKER_CLASS_NAME, qualifiedName)
		.build()

private const val TAG = "DelegatingWorker"

/**
 * A worker that delegates sync to another [CoroutineWorker] constructed with a [HiltWorkerFactory].
 *
 * This allows for creating and using [CoroutineWorker] instances with extended arguments
 * without having to provide a custom WorkManager configuration that the app module needs to utilize.
 *
 * In other words, it allows for custom workers in a library module without having to own
 * configuration of the WorkManager singleton.
 */
class DelegatingWorker(
	appContext: Context,
	workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
	
	private val workerClassName = workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""
	
	private val delegateWorker = EntryPointAccessors.fromApplication(
		appContext,
		HiltWorkerFactoryEntryPoint::class.java
	)
		.hiltWorkerFactory()
		.createWorker(appContext, workerClassName, workerParams) as? CoroutineWorker
		?: throw IllegalArgumentException("Unable to find appropriate worker")
	
	override suspend fun getForegroundInfo(): ForegroundInfo =
		delegateWorker.getForegroundInfo()
	
	override suspend fun doWork(): Result =
		delegateWorker.doWork()
}