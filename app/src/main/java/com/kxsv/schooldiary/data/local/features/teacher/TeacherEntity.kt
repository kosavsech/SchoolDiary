package com.kxsv.schooldiary.data.local.features.teacher

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants

@Entity(
	tableName = DatabaseConstants.TEACHER_TABLE_NAME
)
data class TeacherEntity(
	val firstName: String,
	val lastName: String,
	val patronymic: String,
	val phoneNumber: String,
	@PrimaryKey(autoGenerate = true)
	val teacherId: Int = 0,
) {
	companion object {
		/**
		 * Obtains teacher's full name. Variants:
		 * 1) try to get L.N. Patronymic
		 * 2) if Patronymic isNotEmpty N. Patronymic / L. Patronymic / Patronymic
		 * 3) if firstName isNotEmpty  N. Lastname / Name
		 * 4) Lastname
		 *
		 * @return [teacher full name][String]
		 */
		fun TeacherEntity.fullName(): String {
			return if (this.lastName.isNotEmpty() && this.firstName.isNotEmpty() && this.patronymic.isNotEmpty()) {
				this.lastName[0] + "." + this.firstName[0] + ". " + this.patronymic
			} else if (this.patronymic.isNotEmpty()) {
				if (this.firstName.isNotEmpty()) {
					this.firstName[0] + "." + this.patronymic
				} else if (this.lastName.isNotEmpty()) {
					this.lastName[0] + "." + this.patronymic
				} else {
					this.patronymic
				}
			} else if (this.firstName.isNotEmpty()) {
				if (this.lastName.isNotEmpty()) {
					this.firstName[0] + "." + this.lastName
				} else {
					this.firstName
				}
			} else {
				this.lastName
			}
		}
	}
}