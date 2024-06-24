package com.example.tdspruebasfirebase

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.google.firebase.ktx.Firebase
import java.time.Month
import java.time.Year

class DayActivity : ComponentActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics

        setContent {
            val currentUser = auth.currentUser
            val userName = currentUser?.email?.let { obtenerNombreUsuario(it) } ?: ""
            val month = intent.getStringExtra("mes") ?: return@setContent
            Column {
                TopBar()
                Divider()
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(800.dp)
                ) {
                    DayActivityScreen(month)
                }
            }
        }
    }

    @Composable
    fun DayActivityScreen(month: String) {
        val  monthSpanish=  MonthSpanish(month)

        val daysInMonth = getDaysInMonth(Month.valueOf(month.toUpperCase()))
        val context = LocalContext.current

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text =Year.now().toString() +"-"+ monthSpanish.toString(),
                color = Color.Black, fontSize = 25.sp
                ,textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold)
            Divider()
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(daysInMonth) { day ->
                    DayButton(day) {
                        val intent = Intent(context, NoteActivity::class.java).apply {
                            putExtra("mes", month)
                            putExtra("dia",day)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    @Composable
    fun DayButton(day: Int, onDaySelected: (Int) -> Unit) {
        Button(
            onClick = { onDaySelected(day)

                /* * * * * * *  * * * * * * * * *  * * * * * * * **/
                /*          ANALYTIC         */
                /* para saber el dia selecciondado*/
                firebaseAnalytics.logEvent("dia_Seleccionado") {
                    param("Dia", day.toString())
                    param("usuario", auth.currentUser?.email.toString())
                }
                      },
            modifier = Modifier
                .padding(8.dp)
                .width(300.dp)
                .height(35.dp)
            ,colors = ButtonDefaults.buttonColors((Color(0xFF039BE5)))
            ,
        ) {
            Text(text = day.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar() {

        TopAppBar(
            title = { Text(text ="               Elija el dia",
                color = Color.Black, fontSize = 25.sp
                ,textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold)
             }
            ,
            colors = TopAppBarDefaults.topAppBarColors(Color(0xFF039BE5)),
            navigationIcon = {
                IconButton(onClick = {
                    startActivity(
                        Intent(
                            this@DayActivity, MonthsActivity ::class.java)
                    )
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                }
            },
            actions = {

            }
        )
    }
    fun obtenerNombreUsuario(email: String): String {
        return email.substringBefore('@')
    }
     fun getDaysInMonth(month: Month): List<Int> {
        val year = Year.now().value
        val length = month.length(Year.isLeap(year.toLong()))
        return (1..length).toList()
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
}