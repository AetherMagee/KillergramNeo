package aether.killergram.neo.ui.components

import aether.killergram.neo.ui.model.ModuleToggle
import aether.killergram.neo.ui.model.ToggleIcon
import aether.killergram.neo.ui.model.ToggleSection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Forward
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.CameraRear
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DoNotDisturb
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.KeyboardHide
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PhoneDisabled
import androidx.compose.material.icons.outlined.RestorePage
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ToggleSectionCard(
    section: ToggleSection,
    states: Map<String, Boolean>,
    onToggleChanged: (key: String, enabled: Boolean) -> Unit,
    onToggleParametersClick: (ModuleToggle) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable(section.title) { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                section.toggles.forEach { toggle ->
                    ToggleRow(
                        toggle = toggle,
                        checked = states[toggle.key] ?: false,
                        onCheckedChange = { enabled -> onToggleChanged(toggle.key, enabled) },
                        onParametersClick = { onToggleParametersClick(toggle) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    toggle: ModuleToggle,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onParametersClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val cardColor = if (checked) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            ),
        shape = shape,
        color = cardColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = toggle.icon.imageVector(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = toggle.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = toggle.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (toggle.parameters.isNotEmpty()) {
                    Text(
                        text = "Has ${toggle.parameters.size} parameter(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (toggle.parameters.isNotEmpty()) {
                    IconButton(onClick = onParametersClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Configure ${toggle.title}"
                        )
                    }
                }
                Switch(
                    checked = checked,
                    onCheckedChange = null
                )
            }
        }
    }
}

private fun ToggleIcon.imageVector(): ImageVector {
    return when (this) {
        ToggleIcon.PALETTE -> Icons.Outlined.ColorLens
        ToggleIcon.MENU -> Icons.Outlined.Menu
        ToggleIcon.VISIBILITY_OFF -> Icons.Outlined.VisibilityOff
        ToggleIcon.TIMER -> Icons.Outlined.AccessTime
        ToggleIcon.EDIT -> Icons.Outlined.Edit
        ToggleIcon.VERTICAL_SPLIT -> Icons.Outlined.ViewDay
        ToggleIcon.ADD_BOX_OFF -> Icons.Outlined.AddCircleOutline
        ToggleIcon.PINCH -> Icons.Outlined.Tag
        ToggleIcon.ADS_OFF -> Icons.Outlined.DoNotDisturb
        ToggleIcon.FORWARD -> Icons.AutoMirrored.Outlined.Forward
        ToggleIcon.ACCOUNT_TREE -> Icons.Outlined.AccountTree
        ToggleIcon.STAR_OFF -> Icons.Outlined.StarOutline
        ToggleIcon.VOLUME_OFF -> Icons.AutoMirrored.Outlined.VolumeOff
        ToggleIcon.HD -> Icons.Outlined.Hd
        ToggleIcon.BLUR_ON -> Icons.Outlined.BlurOn
        ToggleIcon.KEYBOARD_HIDE -> Icons.Outlined.KeyboardHide
        ToggleIcon.FONT_DOWNLOAD -> Icons.Outlined.TextFields
        ToggleIcon.SYSTEM_UPDATE_ALT -> Icons.Outlined.SystemUpdateAlt
        ToggleIcon.LABS -> Icons.Outlined.Science
        ToggleIcon.RESTORE_PAGE -> Icons.Outlined.RestorePage
        ToggleIcon.BUG_REPORT -> Icons.Outlined.BugReport
        ToggleIcon.HISTORY -> Icons.Outlined.History
        ToggleIcon.PHONE_OFF -> Icons.Outlined.PhoneDisabled
        ToggleIcon.VIDEOCAM -> Icons.Outlined.Videocam
        ToggleIcon.CAMERA_REAR -> Icons.Outlined.CameraRear
        ToggleIcon.ZOOM_IN -> Icons.Outlined.ZoomIn
        ToggleIcon.HIGH_QUALITY -> Icons.Outlined.HighQuality
        ToggleIcon.FOLDER -> Icons.Outlined.Folder
    }
}
