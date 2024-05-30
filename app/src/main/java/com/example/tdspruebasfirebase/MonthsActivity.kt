package com.example.tdspruebasfirebase

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Person
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.time.Month
import java.time.Year

class MonthsActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        setContent {

            val currentUser = auth.currentUser

                       Column {
                TopBar()
                Divider()
                Spacer(modifier = Modifier.height(50.dp))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(560.dp)) {
                    MyActivityScreen()
                }
            }
        }
    }

    @Composable
    fun MyActivityScreen() {
        val context = LocalContext.current
        AgendaMeses { mes ->
            val intent = Intent(context, DayActivity::class.java).apply {
                putExtra("mes", mes.name)
            }
            context.startActivity(intent)
        }
    }

    @Composable
    fun CrashButton() {
        Button(
            onClick = {
                // Aquí forzamos un fallo
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.log("Simulando un fallo")
                crashlytics.recordException(Exception("¡Este es un crash de prueba!"))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Simular Crash")
        }
    }

    private fun getMonthInYear(year: Year): List<Month> {
        val monthsInYear = mutableListOf<Month>()

        for (month in Month.values()) {
                monthsInYear.add(month)
        }

        return monthsInYear
    }

    @Composable
    fun AgendaMeses(onMonthSelected: (Month) -> Unit) {
        /*  Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            val meses = Month.values()
            for (i in meses.indices step 2) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MesButton(mes = meses[i], onMonthSelected = onMonthSelected)
                    if (i + 1 < meses.size) {
                        MesButton(mes = meses[i + 1], onMonthSelected = onMonthSelected)
                    } else {
                        Spacer(modifier = Modifier.width(120.dp))
                    }
                }
            }
        }*/
        val months = getMonthInYear(Year.now())
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(months) { month ->
                MesButton(mes = month, onMonthSelected = onMonthSelected)

            }
        }
    }

    @Composable
    fun MesButton(mes: Month, onMonthSelected: (Month) -> Unit) {
        /**/
        val analytycs= FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message","Mes Seleccionado")
        /* * * * * * * * * * * * * * * * * * */

        Button(
            onClick = { onMonthSelected(mes)

                                    /*ANALITYCS*/
                /*MARCO COMO EVENTO MANUAL EN ANALITYCS EL MES SELECCIONADO */
                analytycs.logEvent("Mes Seleccionado $mes", bundle)
                /* * * * * *  * * * * * * * * */

            },
            modifier = Modifier
                .padding(8.dp)
                .width(300.dp)
                .height(40.dp)
            ,colors = ButtonDefaults.buttonColors((Color(0xFF039BE5)))
            ,contentPadding = PaddingValues(0.dp)
        ) {

         val  monthSpanish=  when (mes.toString()) {
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
            Text(
                monthSpanish.toString(),
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar() {
        TopAppBar(
                 title = { Text(text ="                    "+Year.now().toString()
                , color = Color.Black
                ,textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
                ) }
                ,colors = TopAppBarDefaults.topAppBarColors(Color(0xFF039BE5))
                ,navigationIcon = {

                IconButton(onClick = {
                    startActivity(Intent(this@MonthsActivity, HomeActivity ::class.java))
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                }
            },
            actions = {
            }
        )
    }

    fun UserName(email: String): String {
        return email.substringBefore('@')
    }

}


