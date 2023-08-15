/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kxsv.schooldiary.di

import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.ApplicationScope
import com.kxsv.schooldiary.di.util.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {
	
	@Provides
	@Dispatcher(AppDispatchers.IO)
	fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO
	
	@Provides
	@Dispatcher(AppDispatchers.Default)
	fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
	
	@Provides
	@Singleton
	@ApplicationScope
	fun providesCoroutineScope(
		@Dispatcher(AppDispatchers.Default) dispatcher: CoroutineDispatcher,
	): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
