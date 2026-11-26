/*
 * Copyright (c) 2023 You Apps
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.bnyro.translate.ui.components.prefs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.bnyro.translate.R
import com.bnyro.translate.util.Preferences

@Composable
fun EditTextPreference(
    modifier: Modifier = Modifier,
    preferenceKey: String,
    value: String,
    labelText: String,
    onValueChange: (String) -> Unit = {},
    isSecret: Boolean = false
) {
    val context = LocalContext.current
    var showPassword by remember { mutableStateOf(false) }

    val visualTransformation: VisualTransformation = if (isSecret && !showPassword) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    val trailingIcon: (@Composable () -> Unit)? = if (isSecret) {
        {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = context.getString(
                        if (showPassword) R.string.hide_key else R.string.show_key
                    )
                )
            }
        }
    } else {
        null
    }

    OutlinedTextField(
        value = value,
        onValueChange = {
            Preferences.put(preferenceKey, it)

            onValueChange.invoke(it)
        },
        label = {
            Text(
                text = labelText
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = if (isSecret) KeyboardType.Password else KeyboardType.Unspecified
        ),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        modifier = modifier
            .fillMaxWidth()
    )
}
