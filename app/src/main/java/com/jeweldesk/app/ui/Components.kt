package com.jeweldesk.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.data.dateStr
import com.jeweldesk.app.data.monthStart
import com.jeweldesk.app.data.monthStr
import com.jeweldesk.app.data.newId
import com.jeweldesk.app.data.num
import com.jeweldesk.app.data.plain
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.ui.theme.JColors
import com.jeweldesk.app.ui.theme.LocalJ
import java.io.File
import java.util.Calendar
import java.util.TimeZone

val LocalNav = staticCompositionLocalOf<NavHostController> { error("No nav controller") }

val jc: JColors @Composable get() = LocalJ.current

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

val Pill = RoundedCornerShape(50)
val Card16 = RoundedCornerShape(16.dp)

// ---------------- header ----------------

@Composable
fun ThemeToggle() {
    val dark = jc.dark
    SquareIconButton(if (dark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode) {
        Store.update { it.copy(darkMode = !it.darkMode) }
    }
}

@Composable
fun SquareIconButton(icon: ImageVector, size: Dp = 52.dp, tint: Color = jc.text, onClick: () -> Unit) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(14.dp)).background(jc.surface)
            .border(1.dp, jc.border, RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = tint) }
}

@Composable
fun IconTile(icon: ImageVector, size: Dp = 48.dp, tint: Color = Gold.Text, bg: Color = jc.tile) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(bg)
            .border(1.dp, Gold.Main.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.5f)) }
}

@Composable
fun HeaderCard(
    title: String,
    subtitle: String = "",
    icon: ImageVector? = null,
    back: Boolean = false,
    letter: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val nav = LocalNav.current
    Row(
        Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)).background(jc.header)
            .border(1.dp, jc.headerBorder, RoundedCornerShape(18.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            back -> Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).border(1.5.dp, jc.text, RoundedCornerShape(14.dp))
                    .clickable { if (onBack != null) onBack() else nav.popBackStack() },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = jc.text) }
            letter != null -> Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(jc.tile)
                    .border(1.5.dp, Gold.Main, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) { Text(letter, color = Gold.Text, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
            icon != null -> IconTile(icon, 54.dp)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = jc.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 14.sp, color = jc.sub, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            actions()
            ThemeToggle()
        }
    }
}

// ---------------- buttons ----------------

@Composable
fun GoldButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.AutoMirrored.Filled.ArrowForward,
    enabled: Boolean = true,
    height: Dp = 56.dp,
    leading: ImageVector? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier.fillMaxWidth().height(height).alpha(if (enabled) 1f else 0.45f)
            .shadow(if (enabled) 6.dp else 0.dp, Pill, ambientColor = Gold.Main, spotColor = Gold.Deep)
            .clip(Pill).background(Gold.brush).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) { Icon(leading, null, tint = Color.Black); Spacer(Modifier.width(8.dp)) }
            Text(text, color = Color(0xFF1A1A1A), fontSize = 17.sp, fontWeight = FontWeight.Medium)
            if (icon != null) { Spacer(Modifier.width(8.dp)); Icon(icon, null, tint = Color.Black) }
        }
    }
}

@Composable
fun OutlineGoldButton(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, color: Color = Gold.Text, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().height(52.dp).clip(Pill).border(1.5.dp, color, Pill).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) { Icon(icon, null, tint = color); Spacer(Modifier.width(6.dp)) }
            Text(text, color = color, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun GoldSquareButton(icon: ImageVector, size: Dp = 56.dp, onClick: () -> Unit) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(14.dp)).background(Gold.Main).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = Color.Black) }
}

@Composable
fun GoldFab(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier.height(58.dp).shadow(10.dp, Pill, ambientColor = Gold.Main, spotColor = Gold.Deep).clip(Pill)
            .background(Gold.brush).clickable(onClick = onClick).padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.Black)
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SolidPill(text: String, icon: ImageVector, bg: Color, onClick: () -> Unit) {
    Row(
        Modifier.height(50.dp).shadow(4.dp, Pill).clip(Pill).background(bg).clickable(onClick = onClick).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

// ---------------- tabs / chips ----------------

@Composable
fun SegTabs(options: List<String>, selected: Int, modifier: Modifier = Modifier, height: Dp = 48.dp, onSelect: (Int) -> Unit) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEachIndexed { i, s -> TabBox(s, i == selected, Modifier.weight(1f), height) { onSelect(i) } }
    }
}

@Composable
fun ScrollTabs(options: List<String>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEachIndexed { i, s -> TabBox(s, i == selected, Modifier.width(170.dp), 52.dp) { onSelect(i) } }
    }
}

@Composable
fun TabBox(text: String, selected: Boolean, modifier: Modifier, height: Dp = 48.dp, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier.height(height).clip(shape)
            .then(if (selected) Modifier.background(Gold.brush) else Modifier.background(jc.surface).border(1.dp, jc.border, shape))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (selected) Color.Black else jc.sub, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal, maxLines = 1)
    }
}

@Composable
fun ChipRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEachIndexed { i, s ->
            val sel = i == selected
            val shape = RoundedCornerShape(12.dp)
            Box(
                Modifier.height(44.dp).clip(shape)
                    .then(if (sel) Modifier.background(Gold.Main) else Modifier.border(1.dp, jc.border, shape))
                    .clickable { onSelect(i) }.padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) { Text(s, color = if (sel) Color.Black else jc.text, fontWeight = FontWeight.Medium) }
        }
    }
}

@Composable
fun SmallToggle(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.clip(RoundedCornerShape(10.dp)).background(jc.header).padding(3.dp)) {
        options.forEachIndexed { i, s ->
            val sel = i == selected
            Box(
                Modifier.size(width = 40.dp, height = 34.dp).clip(RoundedCornerShape(8.dp))
                    .then(if (sel) Modifier.background(jc.tile).border(1.dp, Gold.Main, RoundedCornerShape(8.dp)) else Modifier)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) { Text(s, color = if (sel) Gold.Text else jc.sub, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
fun RadioCard(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier.height(52.dp).clip(shape).background(if (selected) jc.cream else jc.field)
            .border(if (selected) 2.dp else 1.dp, if (selected) Gold.Main else jc.border, shape)
            .clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(22.dp).clip(CircleShape).then(if (selected) Modifier.background(Gold.Main) else Modifier.border(1.5.dp, jc.sub, CircleShape)),
            contentAlignment = Alignment.Center,
        ) { if (selected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(10.dp))
        Text(text, color = jc.text, fontWeight = FontWeight.Medium)
    }
}

// ---------------- cards ----------------

@Composable
fun JCard(modifier: Modifier = Modifier, bg: Color = jc.surface, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(Card16).background(bg).border(1.dp, jc.border, Card16)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content,
    )
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    JCard(modifier) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Gold.Main, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = jc.text, modifier = Modifier.weight(1f))
            trailing()
        }
        HorizontalDivider(color = jc.border)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun ValueRow(label: String, value: String, color: Color = Gold.Text, bold: Boolean = false, labelSuffix: String = "") {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = jc.text, fontSize = 16.sp, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)
        if (labelSuffix.isNotEmpty()) Text(" $labelSuffix", color = jc.sub, fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        Text(value, color = color, fontSize = 16.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun SplitRow(left: String, right: String?, bg: Color, onLeft: (() -> Unit)? = null, onRight: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg).heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(left, color = jc.text, fontSize = 14.sp, modifier = Modifier.weight(1f).then(if (onLeft != null) Modifier.clickable(onClick = onLeft) else Modifier).padding(12.dp))
        if (right != null) {
            Box(Modifier.width(1.dp).height(28.dp).background(jc.border))
            Text(right, color = jc.text, fontSize = 14.sp, modifier = Modifier.weight(1f).then(if (onRight != null) Modifier.clickable(onClick = onRight) else Modifier).padding(12.dp))
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = jc.empty, modifier = Modifier.size(110.dp))
        Spacer(Modifier.height(12.dp))
        Text(text, color = jc.sub, fontSize = 17.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text.uppercase(), color = jc.sub, fontSize = 13.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun BigTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = jc.text, modifier = modifier)
}

// ---------------- inputs ----------------

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold.Main,
    unfocusedBorderColor = jc.border,
    focusedContainerColor = jc.surface,
    unfocusedContainerColor = jc.surface,
    focusedLabelColor = Gold.Text,
    cursorColor = Gold.Deep,
    unfocusedLeadingIconColor = jc.text,
    focusedLeadingIconColor = jc.text,
    disabledBorderColor = jc.border,
    disabledTextColor = jc.text,
    disabledContainerColor = jc.field,
    disabledLabelColor = jc.sub,
    disabledLeadingIconColor = jc.text,
    disabledTrailingIconColor = jc.text,
)

@Composable
fun JField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    suffix: String? = null,
    enabled: Boolean = true,
    leadingTint: Color = Color.Unspecified,
    password: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        visualTransformation = if (password) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = modifier.fillMaxWidth(),
        shape = if (singleLine) RoundedCornerShape(28.dp) else RoundedCornerShape(20.dp),
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        leadingIcon = icon?.let { { Icon(it, null, tint = if (leadingTint == Color.Unspecified) jc.text else leadingTint) } },
        trailingIcon = trailing,
        suffix = suffix?.let { { Text(it, color = jc.sub) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        colors = fieldColors(),
    )
}

/** Numeric text field that keeps only digits and one decimal point. */
@Composable
fun NumField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    suffix: String? = null,
    integer: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    JField(
        value, { s -> onChange(cleanNum(s, integer)) }, label, modifier, icon,
        if (integer) KeyboardType.Number else KeyboardType.Decimal, suffix = suffix, trailing = trailing,
    )
}

fun cleanNum(s: String, integer: Boolean = false, allowNegative: Boolean = false): String {
    val sb = StringBuilder()
    var dot = false
    s.forEachIndexed { i, ch ->
        when {
            ch.isDigit() -> sb.append(ch)
            ch == '.' && !dot && !integer -> { dot = true; sb.append(ch) }
            ch == '-' && allowNegative && i == 0 -> sb.append(ch)
        }
    }
    return sb.toString()
}

private fun fmtNum(v: Double, decimals: Int) = if (v == 0.0) "" else v.plain(decimals)

/** Numeric field bound to a Double; keeps the user's raw text while it parses to the same value. */
@Composable
fun DNum(
    value: Double,
    onValue: (Double) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    suffix: String? = null,
    integer: Boolean = false,
    decimals: Int = 3,
    trailing: (@Composable () -> Unit)? = null,
) {
    var text by remember { mutableStateOf(fmtNum(value, decimals)) }
    LaunchedEffect(value) { if (text.num() != value) text = fmtNum(value, decimals) }
    NumField(text, { text = it; onValue(it.num()) }, label, modifier, icon, suffix, integer, trailing)
}

@Composable
fun <T> DropField(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, enabled = false,
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = icon?.let { { Icon(it, null) } },
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = Gold.Text) },
            singleLine = true, shape = RoundedCornerShape(28.dp), colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.matchParentSize().clip(RoundedCornerShape(28.dp)).clickable { open = true })
        DropdownMenu(open, { open = false }, Modifier.heightIn(max = 360.dp)) {
            options.forEach { o -> DropdownMenuItem(text = { Text(optionLabel(o)) }, onClick = { open = false; onSelect(o) }) }
        }
    }
}

@Composable
fun <T> InlineDrop(value: String, options: List<T>, optionLabel: (T) -> String, onSelect: (T) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(Modifier.clip(RoundedCornerShape(8.dp)).clickable { open = true }.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = jc.text, fontSize = 14.sp)
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Gold.Text)
        }
        DropdownMenu(open, { open = false }) {
            options.forEach { o -> DropdownMenuItem(text = { Text(optionLabel(o)) }, onClick = { open = false; onSelect(o) }) }
        }
    }
}

@Composable
fun SearchBar(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        placeholder = { Text(placeholder, color = jc.sub) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Gold.Text) },
        trailingIcon = if (value.isNotEmpty()) ({ Icon(Icons.Default.Close, null, Modifier.clickable { onChange("") }) }) else null,
        singleLine = true, shape = Pill, colors = fieldColors(),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun ToggleRow(label: String, checked: Boolean, sub: String? = null, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, color = jc.text, fontSize = 16.sp)
            if (sub != null) Text(sub, color = jc.sub, fontSize = 13.sp)
        }
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Gold.Main, checkedTrackColor = Color(0xFFF7E09A), checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color(0xFF7E766B), uncheckedTrackColor = jc.field, uncheckedBorderColor = Color(0xFF6E655C),
            ),
        )
    }
}

@Composable
fun <T> DropRow(label: String, value: String, options: List<T>, optionLabel: (T) -> String, onSelect: (T) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = jc.text, fontSize = 16.sp, modifier = Modifier.weight(1f))
        InlineDrop(value, options, optionLabel, onSelect)
    }
}

// ---------------- dialogs ----------------

@Composable
fun JDialog(
    title: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    headerActions: @Composable RowScope.() -> Unit = {},
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    scroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth(0.94f).heightIn(max = 760.dp).clip(RoundedCornerShape(24.dp)).background(jc.bg)
                .border(1.dp, jc.border, RoundedCornerShape(24.dp)),
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Gold.Main, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = jc.text, modifier = Modifier.weight(1f))
                headerActions()
                Spacer(Modifier.width(8.dp))
                SquareIconButton(Icons.Default.Close, 44.dp, onClick = onDismiss)
            }
            HorizontalDivider(color = jc.border)
            Column(
                Modifier.weight(1f, fill = false).then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
            if (footer != null) {
                HorizontalDivider(color = jc.border)
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = footer)
            }
        }
    }
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String = "Delete", danger: Boolean = true, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton({ onDismiss(); onConfirm() }) { Text(confirm, color = if (danger) Gold.Red else Gold.Text) } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel", color = jc.sub) } },
        containerColor = jc.surface,
    )
}

/** Converts a local-time millis value to the UTC-midnight value the Material date picker expects, and back. */
fun localToUtcDay(ms: Long): Long {
    val c = Calendar.getInstance().apply { timeInMillis = ms }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear(); set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

fun utcDayToLocal(ms: Long, keepTimeOf: Long = System.currentTimeMillis()): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = ms }
    val t = Calendar.getInstance().apply { timeInMillis = keepTimeOf }
    return Calendar.getInstance().apply {
        set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY)); set(Calendar.MINUTE, t.get(Calendar.MINUTE))
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePick(initial: Long, onDismiss: () -> Unit, onPick: (Long) -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = localToUtcDay(initial))
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton({ state.selectedDateMillis?.let { onPick(utcDayToLocal(it, initial)) }; onDismiss() }) { Text("OK", color = Gold.Text) } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel", color = jc.sub) } },
    ) { DatePicker(state) }
}

@Composable
fun DateField(label: String, value: Long, onChange: (Long) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        JField(value.dateStr(), {}, label, icon = Icons.Default.CalendarMonth, enabled = false)
        Box(Modifier.matchParentSize().clip(RoundedCornerShape(28.dp)).clickable { open = true })
    }
    if (open) DatePick(value, { open = false }, onChange)
}

@Composable
fun MonthSwitcher(month: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.KeyboardArrowLeft, null, tint = Gold.Text, modifier = Modifier.clip(CircleShape).clickable { onChange(month - 1) }.padding(4.dp))
        Box(Modifier.clip(Pill).border(1.dp, Gold.Main, Pill).padding(horizontal = 12.dp, vertical = 4.dp)) {
            Text(monthStart(month).monthStr(), color = Gold.Text, fontSize = 13.sp)
        }
        Icon(Icons.Default.KeyboardArrowRight, null, tint = Gold.Text, modifier = Modifier.clip(CircleShape).clickable { onChange(month + 1) }.padding(4.dp))
    }
}

// ---------------- images ----------------

@Composable
fun rememberBitmap(path: String, maxSize: Int = 512): Bitmap? = remember(path) { loadBitmap(path, maxSize) }

fun loadBitmap(path: String, maxSize: Int = 512): Bitmap? {
    if (path.isBlank()) return null
    val f = File(path)
    if (!f.exists()) return null
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, opts)
    var sample = 1
    while (opts.outWidth / sample > maxSize * 2 || opts.outHeight / sample > maxSize * 2) sample *= 2
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
}

@Composable
fun PathImage(path: String, modifier: Modifier, crop: Boolean = true) {
    val bmp = rememberBitmap(path)
    if (bmp != null) Image(bmp.asImageBitmap(), null, modifier, contentScale = if (crop) ContentScale.Crop else ContentScale.Fit)
}

/** Copies a content Uri into app storage and returns the absolute path. */
fun saveImage(context: Context, uri: android.net.Uri): String? = runCatching {
    val dir = File(context.filesDir, "images").apply { mkdirs() }
    val out = File(dir, newId() + ".jpg")
    context.contentResolver.openInputStream(uri)!!.use { input -> out.outputStream().use { input.copyTo(it) } }
    out.absolutePath
}.getOrNull()

@Composable
fun ScreenColumn(padding: PaddingValues = PaddingValues(16.dp), content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

val NoBorder = BorderStroke(0.dp, Color.Transparent)
