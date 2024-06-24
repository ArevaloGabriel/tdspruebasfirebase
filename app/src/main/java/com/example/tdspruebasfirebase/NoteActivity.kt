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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.time.Month


class NoteActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseCrashlytics:FirebaseCrashlytics
    lateinit var month: String
    lateinit var day :String


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        firebaseCrashlytics = Firebase.crashlytics
        month = intent.getStringExtra("mes") ?: ""
      day = intent.getIntExtra("dia",0).toString()

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NoteActivityScreen() {
        val context = LocalContext.current
        var textState by remember { mutableStateOf("") }
        val selectedDate by remember { mutableStateOf<String?>(day) }
        val  monthSpanish= MonthSpanish(month)

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
                    val mes = month
                    val dia = day
                    val fecha=  MonthValueToInt(mes)


                    if (texto.isEmpty()) {

                        firebaseAnalytics.logEvent("Intento_guardar_una_nota_vacia"){}


                        Toast.makeText(context, "La nota no puede estar vacía", Toast.LENGTH_SHORT).show()
                    } else {
                            /*GUARDAR LA NOTA EN FIRESTORE*/

                            saveNoteToFirebase(context, texto, mes, dia, fecha)

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(userName: String) {
        TopAppBar(

            title = { Text(text = "                 "+userName,
                color = Color.Black,
                fontSize = 25.sp
                ,textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold) }
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
    fun MonthSpanish(month:String):String{
        return    when (month) {
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
            else -> {""}
        }

    }

    fun saveNoteToFirebase(context: Context, texto: String, month: String, day: String,date :Int) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.email
            val note = hashMapOf(
                "texto" to texto,
                "mes" to month,
                "dia" to day,
                "fecha" to date,
                "userId" to userId,
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

    fun obtenerNombreUsuario(correoElectronico: String): String {
        return correoElectronico.substringBefore('@')
    }
    fun MonthValueToInt(month : String): Int {
        return  when (month) {
            Month.JANUARY.toString() -> 1
            Month.FEBRUARY.toString() -> 2
            Month.MARCH.toString() ->3
            Month.APRIL.toString() -> 4
            Month.MAY.toString() -> 5
            Month.JUNE.toString() -> 6
            Month.JULY.toString() -> 7
            Month.AUGUST.toString() ->8
            Month.SEPTEMBER.toString() -> 9
            Month.OCTOBER.toString() -> 10
            Month.NOVEMBER.toString() -> 11
            Month.DECEMBER.toString() -> 12
            else -> {0}
        }
    }
}
