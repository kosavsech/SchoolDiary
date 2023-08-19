package com.kxsv.schooldiary.data.util.user_preferences

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = PersistentList::class)
class PersistentListSerializer(
	private val dataSerializer: KSerializer<PeriodWithRange>,
) : KSerializer<PersistentList<PeriodWithRange>> {
	
	private class PersistentListDescriptor :
		SerialDescriptor by serialDescriptor<List<PeriodWithRange>>() {
		@ExperimentalSerializationApi
		override val serialName: String = "kotlinx.serialization.immutable.persistentList"
	}
	
	override val descriptor: SerialDescriptor = PersistentListDescriptor()
	
	override fun serialize(encoder: Encoder, value: PersistentList<PeriodWithRange>) {
		return ListSerializer(dataSerializer).serialize(encoder, value.toList())
	}
	
	override fun deserialize(decoder: Decoder): PersistentList<PeriodWithRange> {
		return ListSerializer(dataSerializer).deserialize(decoder).toPersistentList()
	}
}