package com.fashiontothem.ff.presentation.products

import com.fashiontothem.ff.domain.model.OptionAttribute
import com.fashiontothem.ff.domain.model.ProductDetails

/**
 * Determines whether the current product requires the user to select a variant (size/color/shade)
 * before checking availability. Configurable products with at least one option require selection,
 * while simple products and any others without selectable options do not.
 */
internal fun ProductDetails.requiresVariantSelection(): Boolean {
    if (!type.equals("configurable", ignoreCase = true)) {
        return false
    }

    val options = options ?: return false
    return listOf(options.size, options.colorShade).any { it.hasSelectableValues() }
}

private fun OptionAttribute?.hasSelectableValues(): Boolean {
    return this?.options?.isNotEmpty() == true
}

