package com.fashiontothem.ff.data.remote.adapters

import com.fashiontothem.ff.data.remote.dto.ProductOptionsDto
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Moshi adapter that gracefully handles the `options` field from product details responses.
 *
 * Some simple products return `options: []` instead of an object, which causes Moshi to throw
 * `Expected BEGIN_OBJECT but was BEGIN_ARRAY`. This adapter skips arrays (and any unexpected types)
 * and returns null in those cases, allowing simple products to be parsed without crashing.
 */
class ProductOptionsJsonAdapter {

    @FromJson
    fun fromJson(
        reader: JsonReader,
        delegate: JsonAdapter<ProductOptionsDto>
    ): ProductOptionsDto? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                null
            }
            JsonReader.Token.BEGIN_OBJECT -> delegate.fromJson(reader)
            JsonReader.Token.BEGIN_ARRAY -> {
                // Skip unexpected array value and treat it as no options
                reader.beginArray()
                while (reader.hasNext()) {
                    reader.skipValue()
                }
                reader.endArray()
                null
            }
            else -> {
                // Skip any other unexpected token
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(
        writer: JsonWriter,
        value: ProductOptionsDto?,
        delegate: JsonAdapter<ProductOptionsDto>
    ) {
        if (value == null) {
            writer.nullValue()
        } else {
            delegate.toJson(writer, value)
        }
    }
}

