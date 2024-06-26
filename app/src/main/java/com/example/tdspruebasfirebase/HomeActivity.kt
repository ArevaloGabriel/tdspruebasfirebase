package com.example.tdspruebasfirebase

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.Month

class HomeActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var botonBorrarVisible by mutableStateOf(false)
    private var botonCrashVisible by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        val currentUser = auth.currentUser

        super.onCreate(savedInstanceState)
        val userName = currentUser?.email?.let { NameUser(it) } ?: ""

        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val botonBorrar = Firebase.remoteConfig.getBoolean("boton_borrar")
                if (botonBorrar) {
                    this.botonBorrarVisible = true
                }

            }

        }

        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val botonCrash = Firebase.remoteConfig.getBoolean("boton_crash")
                if (botonCrash) {
                    this.botonCrashVisible = true
                }

            }

        }
        setContent {

            Column {
                TopBar(userName)
                StatusIndicators()
                Divider()
                NotesActivityScreen()
            }
        }

    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun NotesActivityScreen() {

        val context = LocalContext.current
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Text("Usuario no autenticado")
        } else {
            val userId = currentUser.email
            val analytics = FirebaseAnalytics.getInstance(context)
            val notes = remember { mutableStateListOf<Note>() }

            /*ACA SE REALIZA LAS CONSULTAS A LA BASE DE DATOS DE FIREBASE PARA RECUPERAR LAS NOTAS */
            LaunchedEffect(Unit) {
                val querySnapshot = userId?.let {
                    FirebaseFirestore.getInstance()
                        .collection("Usuarios con Notas").document(it).collection("Notas")
                        .orderBy("fecha")
                        .get()
                        .await()
                }
                notes.clear()

                var tieneNotaVencida = false
                var tieneNotaHoy = false

                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        val text = document.getString("texto") ?: ""
                        val month = document.getString("mes") ?: ""
                        val day = document.getString("dia") ?: ""
                        val id = document.id
                        notes.add(Note(id, text, month, day))
                    }

                    // Verifica las notas para el día actual
                    val today = LocalDate.now()

                    for (nota in notes) {
                        try {
                            val noteMonth = Month.valueOf(nota.month.uppercase())
                            val noteDate = LocalDate.of(today.year, noteMonth, nota.day.toInt())
                            if (noteDate.isEqual(today)) {
                                val bundle = Bundle().apply {
                                    putString(FirebaseAnalytics.Param.ITEM_ID, "nota Para Hoy")
                                    putString(FirebaseAnalytics.Param.ITEM_NAME, "nota Para Hoy")
                                    putString(FirebaseAnalytics.Param.CONTENT_TYPE, "nota")
                                }
                                tieneNotaHoy = true

                                analytics.logEvent("notas_Para_HOY", bundle)

                                // Agregar logs para verificar
                                Log.d(
                                    "FirebaseAnalytics",
                                    "Event 'notaParaHOY' logged with bundle: $bundle"
                                )

                                Toast.makeText(context, "tienes una nota", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            // verifica si hay alguna nota vencida
                            if(noteDate.isBefore(today)){
                                tieneNotaVencida = true
                            }
                        } catch (e: Exception) {
                            Log.e("NotesActivityScreen", "Error al verificar la nota: ${e.message}")
                            e.printStackTrace()
                        }
                    }

                    // User property si el usuario tiene alguna nota vencida
                    analytics.setUserProperty("tiene_nota_vencida", tieneNotaVencida.toString())

                    // User property si el usuario tiene notas para hoy
                    analytics.setUserProperty("tiene_nota_hoy", tieneNotaHoy.toString())
                } else {
                    Log.d("NotesActivityScreen", "querySnapshot es nulo")
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    floatingActionButton = {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.padding(end = 20.dp)
                        ) {
                            /* * * * *  * * * * * * * * *  **  * * * *
                            * REMOTE CONFIG
                            * PARA PODER VER EL BOTON DE BORRAR NOTAAS O NO DE
                            * LA BASE DE DATOS
                            *  */
                            if (botonBorrarVisible) {
                                /* * * * * * * * *  * * * * **/

                                FloatingActionButton(
                                    onClick = {

                                        // Llama a la función para borrar una nota específica
                                        if (notes.isNotEmpty()) {
                                            val noteId =
                                                notes[0].id // Borrar la primera nota como ejemplo
                                            val userName = currentUser.email
                                            DeletteNoteFromFirestore(userName.toString(), noteId)

                                            notes.removeAt(0) // Actualiza la lista local
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(16.dp),
                                    shape = CircleShape,
                                    containerColor = Color(color = 0xFFFF0000)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete")
                                }
                            }
                            Spacer(modifier = Modifier.padding(end = 180.dp))
                            FloatingActionButton(
                                onClick = {
                                    /*ME LLEVA A LA PANTALLA DE MES , PARA PODER ELEGIR */
                                    val intent =
                                        Intent(context, MonthsActivity::class.java).apply { }
                                    context.startActivity(intent)
                                    /* * *  **  * * * * * * * *  * * * **/
                                },
                                modifier = Modifier.padding(16.dp),
                                shape = CircleShape,
                                containerColor = Color(color = 0xFF039BE5)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Agregar nota",
                                    modifier = Modifier
                                        .background(Color(color = 0xFF039BE5))
                                        .size(36.dp)
                                )
                            }
                        }
                    },
                ) {
                    NotesList(notes)
                }
            }
        }
    }


    private fun DeletteNoteFromFirestore(userId: String, noteId: String) {
        val db = FirebaseFirestore.getInstance()
        val usuarioRef = db.collection("Usuarios con Notas").document(userId)
        val notaRef = usuarioRef.collection("Notas").document(noteId)
        Log.d("Firestore", "Borrando nota con ID: $noteId para usuario: $userId")
        lifecycleScope.launch {
            notaRef.delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Documento borrado con éxito")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al borrar el documento", e)
                }
        }
    }


    @Composable
    fun NotesList(notes: List<Note>) {
        LazyColumn {
            items(notes) { note ->
                NoteItem(note)
                Spacer(modifier = Modifier.padding(5.dp))
                Divider()
            }
        }
    }

    @Composable
    fun NoteItem(note: Note) {
        val monthSpanish = MonthSpanish(note.month)

        val noteDate =
            LocalDate.of(LocalDate.now().year, MonthValueToInt(note.month), note.day.toInt())
        val currentDate = LocalDate.now()

        val backgroundColor = when {
            noteDate.isBefore(currentDate) -> Color.LightGray
            noteDate.isEqual(currentDate) -> Color.Yellow
            else -> Color.Green
        }
        Column(
            modifier = Modifier
                .padding(10.dp)
                .background(backgroundColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f) //
                        .fillMaxHeight(),
                    contentAlignment = Center
                ) {
                    Text(
                        text = "${note.day} de ${monthSpanish}",
                        fontSize = 20.sp,
                        modifier = Modifier.align(Center)
                    )
                }

            }
        }
        Text(modifier = Modifier.padding(start = 8.dp), text = note.text)
    }


    @OptIn(ExperimentalMaterial3Api::class)

    @Composable
    fun TopBar(userName: String) {
        TopAppBar(

            title = { Text(text = "    Agenda de  " + userName, color = Color.Black) },
            colors = TopAppBarDefaults.topAppBarColors(Color(0xFF039BE5)),
            actions = {
                if(botonCrashVisible){
                    IconButton(onClick = {
                        val crashlytics = FirebaseCrashlytics.getInstance()
                        crashlytics.log("Error forzado en la homeScreen")
                        throw RuntimeException("Este es un error forzado")
                    }) {
                        Icon(Icons.Filled.Warning, contentDescription = "Forzar Error")
                    }
                }
                IconButton(onClick = {


                    /*ALERTA DE DIALOGO PARA CERRAR CESION DE FIREBASE*/
                    showLogoutConfirmationDialog()
                    /*  * * * * *  * * * * * * * * *  */

                }) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                }
            }
        )
    }


    private fun NameUser(email: String): String {
        return email.substringBefore('@')
    }

    /*  FUNCION PARA MOSTRAR CARTEL DE CERRAR SESION */
    private fun showLogoutConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Cerrar sesión")
        alertDialogBuilder.setMessage("¿Estás seguro de que quieres cerrar sesión?")
        alertDialogBuilder.setPositiveButton("Sí") { dialogInterface, _ ->
            /*AUTENTICACION*/
            /*CIERRO SESION DE USUARIO DE FIREBASE*/
            FirebaseAuth.getInstance().signOut()
            /* * * * * * * * * * * * * * * * * * * **/
            startActivity(Intent(this@HomeActivity, AuthActivity::class.java))

            dialogInterface.dismiss()
        }
        alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun MonthValueToInt(month: String): Int {
        return when (month) {
            Month.JANUARY.toString() -> 1
            Month.FEBRUARY.toString() -> 2
            Month.MARCH.toString() -> 3
            Month.APRIL.toString() -> 4
            Month.MAY.toString() -> 5
            Month.JUNE.toString() -> 6
            Month.JULY.toString() -> 7
            Month.AUGUST.toString() -> 8
            Month.SEPTEMBER.toString() -> 9
            Month.OCTOBER.toString() -> 10
            Month.NOVEMBER.toString() -> 11
            Month.DECEMBER.toString() -> 12
            else -> {
                0
            }
        }

    }

    fun MonthSpanish(month: String): String {
        return when (month) {
            Month.JANUARY.toString() -> "Enero"
            Month.FEBRUARY.toString() -> "Febrero"
            Month.MARCH.toString() -> "Marzo"
            Month.APRIL.toString() -> "Abril"
            Month.MAY.toString() -> "Mayo"
            Month.JUNE.toString() -> "Junio"
            Month.JULY.toString() -> "Julio"
            Month.AUGUST.toString() -> "Agosto"
            Month.SEPTEMBER.toString() -> "Septiembre"
            Month.OCTOBER.toString() -> "Octubre"
            Month.NOVEMBER.toString() -> "Noviembre"
            Month.DECEMBER.toString() -> "Diciembre"
            else -> {
                ""
            }
        }

    }
}

@Composable
fun StatusIndicators() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(60.dp),
        modifier = Modifier
            .padding(8.dp)
            .padding(start = 10.dp)
    ) {
        StatusItem(color = Color.LightGray, label = "vencidas")
        StatusItem(color = Color.Yellow, label = " hoy")
        StatusItem(color = Color.Green, label = "a vencer")
    }
}

@Composable
fun StatusItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )
        Text(text = label)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStatusIndicators() {
    StatusIndicators()
}

/*  CLASE PARA PODER MAPEAR LAS NOTAS */
data class Note(val id: String, val text: String, val month: String, val day: String)