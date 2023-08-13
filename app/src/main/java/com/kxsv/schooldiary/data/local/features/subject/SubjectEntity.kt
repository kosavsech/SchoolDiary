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
	@PrimaryKey
	val subjectId: String = "",
) {
	fun getName(): String {
		return if (!this.displayName.isNullOrBlank()) {
			this.displayName
		} else {
			this.fullName
		}
	}
	
	fun getCabinetString(): String {
		return if (!this.cabinet.isNullOrBlank()) {
			this.cabinet
		} else {
			""
		}
	}
	
	fun getDisplayNameString(): String {
		return if (!this.displayName.isNullOrBlank()) {
			this.displayName
		} else {
			""
		}
	}
}