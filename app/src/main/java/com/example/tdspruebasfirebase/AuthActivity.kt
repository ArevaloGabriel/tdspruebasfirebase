package com.example.tdspruebasfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Suppress("UNREACHABLE_CODE")
class AuthActivity : ComponentActivity() {
    private lateinit var firebaseCrashlytics: FirebaseCrashlytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseCrashlytics = Firebase.crashlytics

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 30 // 3600 = 1 hora, por default es 12 horas!!!
            fetchTimeoutInSeconds = 10
        }
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(mapOf("boton_borrar" to false))


        setContent {
            LoginScreen()

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen() {

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color(0xFFFFFFFF)) ,

            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id =  R.drawable.logo), contentDescription = "logo inicio")
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {  }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Color.White),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.DarkGray,

                )
            )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp)
                        .background(Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.DarkGray,
                        unfocusedBorderColor = Color.DarkGray,
                        cursorColor = Color.Black, // Color del cursor
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.DarkGray,

                    )

                )
            Button(
                onClick = {

                    if(email.isNotEmpty() && password.isNotEmpty()){
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    Toast.makeText(context, "Ingreso Correctamente", Toast.LENGTH_SHORT).show()
                                    startActivity(
                                        Intent(
                                            this@AuthActivity, HomeActivity ::class.java)
                                            .putExtra("email",email)
                                            .putExtra("contraseña",password)
                                    )
                                }
                            }}else{
                        Toast.makeText(context, "Error: No se pudo Ingresar", Toast.LENGTH_SHORT).show()

                    }

                },
                modifier = Modifier.width(140.dp),
                colors = ButtonDefaults.buttonColors((Color(0xFF039BE5)))
            ) {
                Text("Ingresar")
            }

            Spacer(modifier = Modifier.height(100.dp))

            Box(){
                Text(text ="No tienes cuenta? , Registrate." )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(

               onClick = {
                    if (!email.contains("@")) {
                        Toast.makeText(context, "Email inválido, falta el @", Toast.LENGTH_SHORT)
                            .show()
                        return@Button}

                    if(password.isNotEmpty()){
                        if(password.length>=6) {

                            Log.d("AuthActivity", "Email: $email")
                            Log.d("AuthActivity", "Password: $password")

                                            /*AUTENTICACION */
                            /*ACA SE CREA EL USUARIO CON EMAIL Y CONTRASEñA*/

                            FirebaseAuth.getInstance()
                                .createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        lifecycleScope.launch {
                                        SaveUser(email,password)
                                        }
                                        Toast.makeText(
                                            context,
                                            "Se creo Correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                                       /* CRASHLYTICS 1*/
                                        /*CREO UN CRASH SI TRATA DE REGISTRARSE CON UN USUARIO REGISTRADO*/

                                        firebaseCrashlytics = FirebaseCrashlytics.getInstance()
                                        firebaseCrashlytics.log("Intento registrarse con usuario registrado")
                                        firebaseCrashlytics.recordException(Exception("Intento registrarse con usuario registrado"))
                                      Log.d("crashlytics","Intento registrarse con usuario registrado")
                                        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

                                        Toast.makeText(context,"Error: El usuario $email , ya existe",Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }else{
                            Toast.makeText(context, "Error:EL PASSWORD DEBE CONTENER 6 CARACTERES", Toast.LENGTH_SHORT).show()

                        }
                }else{
                Toast.makeText(context, "Error: FALTA EL PASSWORD", Toast.LENGTH_SHORT).show()

            }},
                modifier = Modifier
                    .width(140.dp)
                    .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors((Color(0xFF039BE5)))
            ) {
                Text("Registrarse")
            }


        }
    }

                    /*CLOUD FIRESTORE*/
    /* ESTA FUNCION ME GUARDA EL USUARIO EN LA BASE DE DATOS DE FIREBASE*/
    suspend fun SaveUser(email: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val user = User(email, password)
        db.collection("Usuarios Autenticados").add(user).await()
    }
    /* * * * * * * * * * * * * * * * * */
    }



