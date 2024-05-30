package com.example.tdspruebasfirebase

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tdspruebasfirebase.ui.theme.TdspruebasfirebaseTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Month


class NoteActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    lateinit var mes: String
    lateinit var dia :String


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
      mes = intent.getStringExtra("mes") ?: ""
      dia = intent.getIntExtra("dia",0).toString()

        setContent {
            val currentUser = auth.currentUser
            val userName = currentUser?.email?.let { obtenerNombreUsuario(it) } ?: ""
Column {
    TopBar(userName)
    Divider()
    NoteActivityScreen()
}

        }
    }
    fun obtenerNombreUsuario(correoElectronico: String): String {
        return correoElectronico.substringBefore('@')
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NoteActivityScreen() {
        val context = LocalContext.current
        var textState by remember { mutableStateOf("") }
        val selectedDate by remember { mutableStateOf<String?>(dia) }
        val  monthSpanish=  when (mes) {
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
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "Escribe tu nota para el  $selectedDate de $monthSpanish")

            Spacer(modifier = Modifier.height(16.dp))

            val textValue = textState
            TextField(
                value = textValue,
                onValueChange = { textState = it },
                label = { Text("Ingresa tu nota",) },
                modifier = Modifier.fillMaxWidth() .background(Color.White),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.DarkGray,
                unfocusedIndicatorColor = Color.DarkGray,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.DarkGray
            )


            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val texto = textState
                    val mes = mes
                    val dia = dia


                    if (texto.isEmpty()) {

                          /* CRASHLYTICS 2*/
                        /*CREO UN CRASH SI TRATA DE GUARDAR UNA NOTA VACIA*/

                        val crashlytics = FirebaseCrashlytics.getInstance()
                        crashlytics.recordException(Exception("Intento guardar una nota vacia"))
                        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

                        Toast.makeText(context, "La nota no puede estar vacía", Toast.LENGTH_SHORT).show()
                    } else {
                            /*GUARDAR LA NOTA EN FIRESTORE*/
                            saveNoteToFirebase(context, texto, mes, dia)
                        Toast.makeText(context, "Nota guardada correctamente", Toast.LENGTH_SHORT)
                            .show()
                    }

                },
                modifier = Modifier
                    .align(Alignment.End)
                , colors = ButtonDefaults.buttonColors((Color(0xFF039BE5)))
            ) {
                Text("Guardar")
            }
        }
    }

    fun saveNoteToFirebase(context: Context, texto: String, mes: String, dia: String) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.email
            val note = hashMapOf(
                "texto" to texto,
                "mes" to mes,
                "dia" to dia,
                "userId" to userId
            )
                                    /* CLOUD FIRESTORE */
                         /* ACA SE ALMACENA LA NOTA EN FIREBASE */
            if (userId != null) {
                db.collection("Usuarios con Notas").document(userId).collection("Notas").add(note)
                .addOnSuccessListener { documentReference ->
                    val intent = Intent(context, HomeActivity::class.java)
                    context.startActivity(intent)
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                    Toast.makeText(context, "Error al guardar la nota", Toast.LENGTH_SHORT).show()
                }}
        } else {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
                   /* * * * * * * * * * * *  ** * * *  * * * * * *  * */
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(userName: String) {
        TopAppBar(

            title = { Text(text = "                 "+userName, color = Color.Black) }
            ,colors = TopAppBarDefaults.topAppBarColors(Color(0xFF039BE5))
            ,navigationIcon = {
                IconButton(onClick = {
                    startActivity(
                        Intent(
                            this@NoteActivity, HomeActivity ::class.java)

                    )
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                }
            },
            actions = {

            }

        )

    }
}
