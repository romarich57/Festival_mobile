package com.projetmobile.mobile.data.entity.profile

sealed interface OptionalField<out T> {
    data object Unchanged : OptionalField<Nothing>

    data class Value<T>(val value: T) : OptionalField<T>
}
