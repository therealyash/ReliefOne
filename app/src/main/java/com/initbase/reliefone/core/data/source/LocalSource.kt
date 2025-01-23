package com.initbase.reliefone.core.data.source

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.initbase.reliefone.SosContacts
import java.io.InputStream
import java.io.OutputStream

object SosContactsSerializer : Serializer<SosContacts> {
    override val defaultValue: SosContacts = SosContacts.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SosContacts {
        try {
            return SosContacts.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: SosContacts,
        output: OutputStream
    ) = t.writeTo(output)
}
val Context.contactsDataStore by dataStore("dataStore.pb",SosContactsSerializer)