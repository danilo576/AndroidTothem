package com.fashiontothem.ff.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import humer.UvcCamera.R

/**
 * Global fonts for the app.
 * Usage: Fonts.Poppins
 */
object Fonts {
    val Poppins: FontFamily = FontFamily(
        Font(R.font.poppins_regular, FontWeight.Normal),
        Font(R.font.poppins_medium, FontWeight.Medium),
        Font(R.font.poppins_bold, FontWeight.Bold)
    )
}


