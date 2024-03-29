package com.kxsv.schooldiary.ui.screens.teacher_list

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kxsv.schooldiary.R
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.title


@Composable
fun AddEditTeacherDialog(
	dialogState: MaterialDialogState,
	firstName: String,
	lastName: String,
	patronymic: String,
	phoneNumber: String,
	isTeacherSaved: Boolean,
	@StringRes errorMessage: Int?,
	updateFirstName: (String) -> Unit,
	updateLastName: (String) -> Unit,
	updatePatronymic: (String) -> Unit,
	updatePhoneNumber: (String) -> Unit,
	clearErrorMessage: () -> Unit,
	onSaveClick: () -> Unit,
	onCancelClick: () -> Unit,
) {
	val isTextValid = remember(firstName, lastName, patronymic) {
		patronymic.isNotBlank() || lastName.isNotBlank() || firstName.isNotBlank()
	}
	LaunchedEffect(isTeacherSaved) {
		if (isTeacherSaved) {
			dialogState.hide()
			onCancelClick()
		}
	}
	LaunchedEffect(firstName, lastName, patronymic) {
		clearErrorMessage()
	}
	MaterialDialog(
		dialogState = dialogState,
		buttons = {
			positiveButton(
				res = R.string.btn_save,
				onClick = onSaveClick
			)
			negativeButton(
				res = R.string.btn_cancel,
				onClick = { onCancelClick(); dialogState.hide() }
			)
		},
		backgroundColor = MaterialTheme.colorScheme.surface,
		autoDismiss = false
	) {
		val focusManager = LocalFocusManager.current
		PositiveButtonEnabled(valid = (isTextValid && errorMessage == null), onDispose = {})
		title(
			res = R.string.add_teacher,
			color = MaterialTheme.colorScheme.onSurface
		)
		Column {
			input(
				label = stringResource(R.string.first_name_hint),
				prefill = firstName,
				onInput = { updateFirstName(it) },
				waitForPositiveButton = false,
				singleLine = true,
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Next,
					autoCorrect = false,
					capitalization = KeyboardCapitalization.Words,
					keyboardType = KeyboardType.Text
				),
				keyboardActions = KeyboardActions(
					onNext = {
						focusManager.moveFocus(FocusDirection.Next)
					}
				),
			)
			input(
				label = stringResource(R.string.last_name_hint),
				prefill = lastName,
				onInput = { updateLastName(it) },
				waitForPositiveButton = false,
				singleLine = true,
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Next,
					autoCorrect = false,
					capitalization = KeyboardCapitalization.Words,
					keyboardType = KeyboardType.Text
				),
				keyboardActions = KeyboardActions(
					onNext = {
						focusManager.moveFocus(FocusDirection.Next)
					}
				),
			)
			input(
				label = stringResource(R.string.patronymic_hint),
				prefill = patronymic,
				onInput = { updatePatronymic(it) },
				waitForPositiveButton = false,
				singleLine = true,
				keyboardOptions = KeyboardOptions(
					imeAction = if (!isTextValid) {
						ImeAction.None
					} else {
						ImeAction.Next
					},
					autoCorrect = false,
					capitalization = KeyboardCapitalization.Words,
					keyboardType = KeyboardType.Text
				),
				keyboardActions = KeyboardActions(
					onNext = {
						focusManager.moveFocus(FocusDirection.Next)
					},
				),
			)
			input(
				label = stringResource(R.string.phone_number_hint),
				prefill = phoneNumber,
				onInput = { updatePhoneNumber(it) },
				waitForPositiveButton = false,
				singleLine = true,
				keyboardOptions = KeyboardOptions(
					imeAction = if (!isTextValid) {
						ImeAction.Previous
					} else {
						ImeAction.Done
					},
					autoCorrect = false,
					capitalization = KeyboardCapitalization.None,
					keyboardType = KeyboardType.Phone
				),
				keyboardActions = KeyboardActions(
					onDone = {
						onSaveClick()
						dialogState.hide(focusManager)
					},
					onPrevious = {
						focusManager.moveFocus(FocusDirection.Previous)
					}
				),
			)
			if (errorMessage != null) {
				Text(
					text = stringResource(errorMessage),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.error,
					modifier = Modifier
						.padding(horizontal = 24.dp)
						.align(Alignment.End)
				)
			}
		}
	}
}