package com.kxsv.schooldiary.data.app_defaults

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class AppDefaults(
	val defaultPatternId: Long = 0L,
)

object AppDefaultsSerializer : Serializer<AppDefaults> {
	
	override val defaultValue = AppDefaults()
	
	override suspend fun readFrom(input: InputStream): AppDefaults {
		return try {
			Json.decodeFromString(
				deserializer = AppDefaults.serializer(),
				string = input.readBytes().decodeToString()
			)
		} catch (e: SerializationException) {
			e.printStackTrace()
			defaultValue
		}
	}
	
	override suspend fun writeTo(t: AppDefaults, output: OutputStream) {
		withContext(Dispatchers.IO) {
			output.write(
				Json.encodeToString(
					serializer = AppDefaults.serializer(),
					value = t
				).encodeToByteArray()
			)
		}
	}
}