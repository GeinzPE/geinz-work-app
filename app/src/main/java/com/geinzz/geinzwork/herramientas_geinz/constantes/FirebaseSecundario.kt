package com.geinzz.geinzwork.herramientas_geinz.constantes

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseSecundario {
    private const val NOMBRE_APP = "firebaseTrabajadores"
    fun inicializar(context: Context) {
        if (FirebaseApp.getApps(context).none { it.name == NOMBRE_APP }) {
            val options = FirebaseOptions.Builder()
                .setProjectId("proyectolista-95172")
                .setApplicationId("1:250365546182:android:1d551cd44aa85afe9111c7")
                .setApiKey("AIzaSyCvXgvs6MQ8jXYx-vLsmWRdqw9bchHm1Cg")
                .setStorageBucket("proyectolista-95172.firebasestorage.app")
                .build()

            FirebaseApp.initializeApp(context, options, NOMBRE_APP)
        }
    }

    fun getFirestore(): FirebaseFirestore {
        val app = FirebaseApp.getInstance(NOMBRE_APP)
        return FirebaseFirestore.getInstance(app)
    }

    // Si quieres usar RealtimeDatabase
    fun getRealtimeDB(): FirebaseDatabase {
        val app = FirebaseApp.getInstance(NOMBRE_APP)
        return FirebaseDatabase.getInstance(app)
    }

    // Si quieres usar FirebaseStorage
    fun getStorage(): FirebaseStorage {
        val app = FirebaseApp.getInstance(NOMBRE_APP)
        return FirebaseStorage.getInstance(app)
    }
}