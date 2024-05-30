package com.example.tdspruebasfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdspruebasfirebase.ui.theme.TdspruebasfirebaseTheme
import com.google.firebase.auth.FirebaseAuth
import java.time.Month
import java.time.Year

class DayActivity : ComponentActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentUser = auth.currentUser
            val userName = currentUser?.email?.let { obtenerNombreUsuario(it) } ?: ""
            val mes = intent.getStringExtra("mes") ?: return@setContent
            Column {
                TopBar(mes)
                Divider()
                Spacer(modifier = Modifier.height(50.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(560.dp)
                ) {
                    DayActivityScreen(mes)
                }
            }
        }
    }
    fun obtenerNombreUsuario(correoElectronico: String): String {
        return correoElectronico.substringBefore('@')
    }
    @Composable
    fun DayActivityScreen(mes: String) {
        val daysInMonth = getDaysInMonth(Month.valueOf(mes.toUpperCase()))
        val context = LocalContext.current

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {


            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(daysInMonth) { day ->
                    DayButton(day) {
                        Toast.makeText(context, "Día seleccionado: $day", Toast.LENGTH_SHORT).show()
                        // Aquí puedes agregar la lógica para manejar la selección del día
                        // Navegar a MonthDetailActivity pasando el nombre del mes seleccionado
                        val intent = Intent(context, NoteActivity::class.java).apply {
                            putExtra("mes", mes)
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
            onClick = { onDaySelected(day) },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .height(40.dp)
            ,colors = ButtonDefaults.buttonColors((Color(0xFF039BE5)))
            ,
        ) {
            Text(text = day.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    private fun getDaysInMonth(month: Month): List<Int> {
        val year = Year.now().value
        val length = month.length(Year.isLeap(year.toLong()))
        return (1..length).toList()
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(mes: String) {
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
        TopAppBar(
            title = { Text(text ="                    "+monthSpanish.toString()
                , color = Color.Black
                ,textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            ) }
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
}