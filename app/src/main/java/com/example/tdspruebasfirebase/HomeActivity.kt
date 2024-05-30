package com.example.tdspruebasfirebase

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Month

class HomeActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentUser = auth.currentUser
            val userName = currentUser?.email?.let { NameUser(it) } ?: ""
            Column {
                TopBar(userName)
                NotesActivityScreen()
            }

        }
    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NotesActivityScreen() {
        val context = LocalContext.current
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Text("Usuario no autenticado")
        } else {
            val userId = currentUser.email
            val notes = remember { mutableStateListOf<Nota>() }

            /*ACA SE REALIZA LAS CONSULTAS A LA BASE DE DATOS DE FIREBASE PARA RECUPERAR LAS NOTAS */

            LaunchedEffect(Unit) {
                val querySnapshot = userId?.let {
                    FirebaseFirestore.getInstance()
                        .collection("Usuarios con Notas").document(it).collection("Notas")
                        .get()
                        .await()
                }
                 /*BORRO LAS NOTAS VIEJAS POR LAS DUDAS PARA ACTUALIZAR*/
                notes.clear()
                /* * * * * *  * * * * * * * */
                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        val texto = document.getString("texto") ?: ""
                        val mes = document.getString("mes") ?: ""
                        val dia = document.getString("dia") ?: ""
                        notes.add(Nota(texto, mes, dia))
                    }
                }
            }

            Scaffold(

                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            /*ME LLEVA A LA PANTALLA DE MES , PARA PODER ELEGIR */
                            val intent = Intent(context, MonthsActivity::class.java).apply { }
                            context.startActivity(intent)
                            /* * *  **  * * * * * * * *  * * * **/
                        },
                        modifier = Modifier.padding(16.dp)
                        ,shape = CircleShape
                        , containerColor = Color(color=0xFF039BE5)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar nota",
                           modifier = Modifier.background(Color(color=0xFF039BE5 ))
                                                .size(36.dp)
                        )
                    }
                }
            ) {

                /*ACA PASO LAS NOTAS RECIBIDAS PARA MOSTRARLAS */
                NotesList(notes)
            }
        }
    }


    @Composable
    fun NotesList(notas: List<Nota>) {
        LazyColumn {
            items(notas) { nota ->
                NoteItem(nota)
                Divider()
            }
        }
    }

    @Composable
    fun NoteItem(nota: Nota) {
        val  monthSpanish=  when (nota.mes) {
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
            else -> {}
        }
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "Fecha :${nota.dia} de ${monthSpanish}")
            Text(text = nota.texto)
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)

    @Composable
    fun TopBar(userName: String) {
       TopAppBar(

            title = { Text(text ="    Agenda de  " + userName, color = Color.Black) }
           ,colors = TopAppBarDefaults.topAppBarColors(Color(0xFF039BE5)) ,
           actions = {
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
    /* FUNCION PARA QUE ME DEJE EL NOMBRE DEL EMAIL , COMO NOMBRE DE USUARIO */
   private  fun NameUser(correoElectronico: String): String {
        return correoElectronico.substringBefore('@')
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
}

/*  CLASE PARA PODER MAPEAR LAS NOTAS */
data class Nota(val texto: String, val mes: String, val dia: String)