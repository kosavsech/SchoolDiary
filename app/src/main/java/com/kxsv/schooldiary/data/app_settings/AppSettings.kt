package com.kxsv.schooldiary.data.app_settings

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class AppSettings(
	val defaultPatternId: Long = 0L,
	val scheduleRefRangeStartId: Long = 0L,
	val scheduleRefRangeEndId: Long = 0L,
	val eduLogin: String? = null,
	val eduPassword: String? = null,
)

object AppDefaultsSerializer : Serializer<AppSettings> {
	
	override val defaultValue = AppSettings()
	
	override suspend fun readFrom(input: InputStream): AppSettings {
		return try {
			Json.decodeFromString(
				deserializer = AppSettings.serializer(),
				string = input.readBytes().decodeToString()
			)
		} catch (e: SerializationException) {
			e.printStackTrace()
			defaultValue
		}
	}
	
	override suspend fun writeTo(t: AppSettings, output: OutputStream) {
		withContext(Dispatchers.IO) {
			output.write(
				Json.encodeToString(
					serializer = AppSettings.serializer(),
					value = t
				).encodeToByteArray()
			)
		}
	}
}