package com.geinzz.geinzwork.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.ktor.client.plugins.cache.storage.FileStorage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class repo_carga_img_general {
    val db= FirebaseFirestore.getInstance()
    val storage= FirebaseStorage.getInstance()


    suspend fun obtenerUrlsCarga(): List<String> = suspendCoroutine { continuation ->
        val folderRef = storage.reference.child("walpaper_geinz/emergencia")

        folderRef.listAll()
            .addOnSuccessListener { listResult ->
                val items = listResult.items
                val total = items.size
                val urls = mutableListOf<String>()

                if (total == 0) {
                    continuation.resume(emptyList())
                    return@addOnSuccessListener
                }

                items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        urls.add(uri.toString())

                        if (urls.size == total) {
                            continuation.resume(urls)
                        }
                    }.addOnFailureListener {
                        // si falla una, igual seguimos
                        urls.add("ERROR")
                        if (urls.size == total) {
                            continuation.resume(urls)
                        }
                    }
                }
            }
            .addOnFailureListener {
                continuation.resume(emptyList())
            }
    }
    suspend fun obtenerUrlsCarga_lugares_turisticos(): List<String> = suspendCoroutine { continuation ->
        val folderRef = storage.reference.child("walpaper_geinz/fondos_carga")

        folderRef.listAll()
            .addOnSuccessListener { listResult ->
                val items = listResult.items
                val total = items.size
                val urls = mutableListOf<String>()

                if (total == 0) {
                    continuation.resume(emptyList())
                    return@addOnSuccessListener
                }

                items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        urls.add(uri.toString())

                        if (urls.size == total) {
                            continuation.resume(urls)
                        }
                    }.addOnFailureListener {
                        // si falla una, igual seguimos
                        urls.add("ERROR")
                        if (urls.size == total) {
                            continuation.resume(urls)
                        }
                    }
                }
            }
            .addOnFailureListener {
                continuation.resume(emptyList())
            }
    }
}