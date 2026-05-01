package tech.benedicta.will.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import tech.benedicta.will.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Brand color constants — source of truth: .design/branding/will/patterns/will.yml
val WillBackground      = Color(0xFF000000)  // OLED true black
val WillSurface1        = Color(0xFF0D0D0D)  // card
val WillSurface2        = Color(0xFF161616)  // popover (bottom sheets, overlays)
val WillSurface3        = Color(0xFF202020)  // muted
val WillAccent          = Color(0xFF4F6EF7)  // primary / accent — interactive elements only
val WillAccentFg        = Color(0xFFFFFFFF)  // primary-foreground
val WillTextPrimary     = Color(0xFFF0F0F0)  // foreground
val WillTextSecondary   = Color(0xFF888888)  // muted-foreground
val WillTextTertiary    = Color(0xFF505050)  // disabled only
val WillBorder          = Color(0xFF2A2A2A)  // border
val WillBorderActive    = Color(0xFF404040)  // border-active
val WillDestructive     = Color(0xFFE05252)  // destructive
val WillSuccess         = Color(0xFF4CAF74)  // success

// ⚠ Accent leak: M3 applies `primary` automatically to Snackbar action text, Checkbox checked
// state, Switch thumb, CircularProgressIndicator track, Chip selected border.
// Rule: if the user taps it → accent is correct. If M3 just applies it decoratively → override
// `colors` parameter on that component to use a neutral color.
private val WillDarkColors = darkColorScheme(
    background           = WillBackground,
    surface              = WillSurface1,
    surfaceVariant       = WillSurface2,
    surfaceContainer     = WillSurface3,
    surfaceContainerHigh = WillSurface2,
    inverseSurface       = WillTextPrimary,
    inverseOnSurface     = WillBackground,
    primary              = WillAccent,
    onPrimary            = WillAccentFg,
    primaryContainer     = Color(0xFF161B3A),
    onPrimaryContainer   = WillAccent,
    secondary            = WillSurface2,
    onSecondary          = WillTextPrimary,
    secondaryContainer   = WillSurface3,
    onSecondaryContainer = WillTextPrimary,
    tertiary             = WillSurface3,
    onTertiary           = WillTextSecondary,
    tertiaryContainer    = WillSurface3,
    onTertiaryContainer  = WillTextSecondary,
    onBackground         = WillTextPrimary,
    onSurface            = WillTextPrimary,
    onSurfaceVariant     = WillTextSecondary,
    outline              = WillBorder,
    outlineVariant       = WillBorderActive,
    error                = WillDestructive,
    onError              = WillTextPrimary,
    errorContainer       = Color(0xFF3D1414),
    onErrorContainer     = WillDestructive,
    scrim                = Color(0xCC000000),
)

private val GeistSans = FontFamily(
    Font(R.font.geist_sans_regular, FontWeight.Normal),
    Font(R.font.geist_sans_medium,  FontWeight.Medium),
)
private val GeistMono = FontFamily(
    Font(R.font.geist_mono_regular, FontWeight.Normal),
)

private val WillTypography = Typography(
    displaySmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-0.02).em,
    ),
    headlineMedium = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = (-0.01).em,
    ),
    headlineSmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.01).em,
    ),
    titleLarge = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.01).em,
    ),
    titleMedium = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 21.sp,
    ),
    titleSmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 19.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.01).em,
    ),
    bodyMedium = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 21.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 19.sp,
    ),
    // Geist Mono — timestamps, source labels (data labels only, not titles)
    labelSmall = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.01.em,
    ),
)

private val WillShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),  // task cards — always 0dp
    small      = RoundedCornerShape(0.dp),  // inputs, badges
    medium     = RoundedCornerShape(4.dp),  // adaptive icon grid only
    large      = RoundedCornerShape(8.dp),  // bottom sheets (ModalBottomSheet uses large)
    extraLarge = RoundedCornerShape(8.dp),
)

object Spacing {
    val xs  = 4.dp
    val sm  = 8.dp
    val md  = 12.dp
    val lg  = 16.dp
    val xl  = 24.dp
    val xxl = 32.dp
    val screenHorizontal = 24.dp
    val cardVertical     = 12.dp
    val cardHorizontal   = 16.dp
    val touchTarget      = 48.dp
}

@Composable
fun WillTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WillDarkColors,
        typography  = WillTypography,
        shapes      = WillShapes,
        content     = content
    )
}
