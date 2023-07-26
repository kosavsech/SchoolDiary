package com.kxsv.schooldiary.data.local.features.subject

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants

@Entity(
	tableName = DatabaseConstants.SUBJECT_TABLE_NAME
)
data class SubjectEntity(
	val fullName: String,
	val cabinet: String? = null,
	val displayName: String? = null,
	val targetMark: Double? = null,
//    val tags: List<Tag>,
	@PrimaryKey(autoGenerate = true)
	val subjectId: Long = 0,
) {
	fun getName(): String {
		return this.displayName ?: this.fullName
	}
	
	fun getCabinetString(): String {
		return this.cabinet ?: ""
	}
}