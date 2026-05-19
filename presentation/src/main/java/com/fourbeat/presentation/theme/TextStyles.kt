package com.fourbeat.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fourbeat.presentation.R

val NotoSerifKr = FontFamily(
    Font(R.font.noto_serif_kr_extra_light, FontWeight.ExtraLight),
    Font(R.font.noto_serif_kr_light, FontWeight.Light),
    Font(R.font.noto_serif_kr_regular, FontWeight.Normal),
    Font(R.font.noto_serif_kr_medium, FontWeight.Medium),
    Font(R.font.noto_serif_kr_semi_bold, FontWeight.SemiBold),
    Font(R.font.noto_serif_kr_bold, FontWeight.Bold),
    Font(R.font.noto_serif_kr_extra_bold, FontWeight.ExtraBold),
    Font(R.font.noto_serif_kr_black, FontWeight.Black),
)

val InstrumentSerifItalic = FontFamily(
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

val logoStyle = TextStyle(
    fontFamily = InstrumentSerifItalic,
    fontStyle = FontStyle.Italic,
)

val bold18 = TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.Bold,
)
val bold14 = TextStyle(
    fontFamily = NotoSerifKr,
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
)

val medium56 = TextStyle(
    fontFamily = NotoSerifKr,
    fontSize = 56.sp,
    fontWeight = FontWeight.Medium,
)
val medium40 = TextStyle(
    fontFamily = NotoSerifKr,
    fontSize = 40.sp,
    fontWeight = FontWeight.Medium,
)
val medium32 = TextStyle(
    fontFamily = NotoSerifKr,
    fontSize = 32.sp,
    fontWeight = FontWeight.Medium,
)

val normal20 = TextStyle(
    fontFamily = NotoSerifKr,
    fontSize = 20.sp,
    fontWeight = FontWeight.Normal,
)
val normal16 = TextStyle(
    fontFamily = NotoSerifKr,
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal,
)
val normal14 = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
)
val normalSerif14 = TextStyle(
    fontFamily = NotoSerifKr,
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
)