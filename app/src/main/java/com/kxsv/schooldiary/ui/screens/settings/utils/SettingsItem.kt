package com.kxsv.schooldiary.ui.screens.settings.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kxsv.schooldiary.R

sealed class SettingsItemType {
	class Input(val currentValue: String?) : SettingsItemType()
	
	class Toggleable(val currentState: Boolean?) : SettingsItemType()
	
	class DropdownChoice<T : ISettingsDropDownChoice>(
		val currentValue: T?,
		val choiceOptions: Array<T>,
	) : SettingsItemType()
	
}

data class SettingsItem(
	@StringRes val label: Int,
	val type: SettingsItemType,
	val onValueChange: (Any) -> Unit,
	val onClick: (() -> Unit)? = null,
	val icon: ImageVector? = null,
)

@Composable
fun GetSettingItemComposable(settingItem: SettingsItem) {
	when (settingItem.type) {
		is SettingsItemType.Input -> {
			val clickableModifier = if (settingItem.onClick != null) {
				Modifier.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = rememberRipple(
						bounded = false,
						radius = Dp.Unspecified,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					),
					onClick = { settingItem.onClick.invoke() }
				)
			} else Modifier
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp)
					.clip(MaterialTheme.shapes.extraLarge)
					.then(clickableModifier),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Text(
						text = stringResource(settingItem.label),
						style = MaterialTheme.typography.titleMedium
					)
					Text(
						text = settingItem.type.currentValue
							?: stringResource(R.string.not_found),
						style = MaterialTheme.typography.titleMedium
					)
				}
			}
		}
		
		is SettingsItemType.Toggleable -> {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp)
					.clip(MaterialTheme.shapes.extraLarge)
					.clickable(
						interactionSource = MutableInteractionSource(),
						indication = rememberRipple(
							bounded = false,
							radius = Dp.Unspecified,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					) {
						settingItem.onValueChange.invoke(
							!(settingItem.type.currentState ?: false)
						)
					},
				verticalAlignment = Alignment.CenterVertically,
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Text(
						text = stringResource(settingItem.label),
						style = MaterialTheme.typography.titleMedium
					)
					Switch(
						checked = settingItem.type.currentState ?: false,
						onCheckedChange = settingItem.onValueChange,
						modifier = Modifier.sizeIn(maxHeight = (30.6).dp)
					)
				}
			}
		}
		
		is SettingsItemType.DropdownChoice<*> -> {
			var expanded by remember { mutableStateOf(false) }
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { expanded = true }
					.padding(dimensionResource(R.dimen.vertical_margin)),
				verticalAlignment = Alignment.CenterVertically
			) {
				if (settingItem.icon != null) {
					Icon(
						imageVector = settingItem.icon,
						contentDescription = "Icon of setting item",
						modifier = Modifier.size(18.dp)
					)
					Spacer(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin)))
				}
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Text(
						text = stringResource(settingItem.label),
						style = MaterialTheme.typography.titleMedium,
					)
					Text(
						text = stringResource(
							settingItem.type.currentValue?.textRes ?: R.string.not_found
						),
						style = MaterialTheme.typography.titleMedium,
					)
				}
				val configuration = LocalConfiguration.current
				val screenWidth = (configuration.screenWidthDp.dp / 3) * 2
				
				DropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false },
					offset = DpOffset(screenWidth, 10.dp),
				) {
					settingItem.type.choiceOptions.forEach {
						val isSelected = settingItem.type.currentValue == it
						val backgroundModifier = if (isSelected) {
							Modifier.background(MaterialTheme.colorScheme.outlineVariant)
						} else Modifier
						DropdownMenuItem(
							text = { Text(text = stringResource(it.textRes)) },
							onClick = {
								settingItem.onValueChange(it)
								expanded = false
							},
							enabled = !isSelected,
							modifier = Modifier.then(backgroundModifier),
							colors = MenuDefaults.itemColors(
								textColor = MaterialTheme.colorScheme.onSurface,
								disabledTextColor = MaterialTheme.colorScheme.onSurface,
							)
						)
					}
				}
			}
		}
	}
}