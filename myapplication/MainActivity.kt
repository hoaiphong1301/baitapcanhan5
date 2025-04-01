package com.example.myapplication

import androidx.compose.foundation.background
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "login") {
                composable("login") { LoginScreen(navController) }
                composable("profile") { ProfileScreen(navController) }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var user by remember { mutableStateOf(auth.currentUser) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(auth) {
        auth.addAuthStateListener {
            user = it.currentUser
            if (user != null) {
                navController.navigate("profile") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("687433858678-gih25uk996411d83j3siqvb624vagagd.apps.googleusercontent.com") // Thay bằng Web Client ID thật
                .requestEmail()
                .build()
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    user = auth.currentUser
                } else {
                    errorMessage = "Đăng nhập thất bại"
                }
            }
        } catch (e: ApiException) {
            errorMessage = "Google Sign-In thất bại"
            Log.e("Login", "Google Sign-In thất bại", e)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.uth),
            contentDescription = "UTH Logo",
            modifier = Modifier.size(300.dp)
        )
        //Spacer(modifier = Modifier.height(16.dp))
        //Text("SmartTasks", fontSize = 24.sp)
        //Text("A simple and efficient to-do app")
        Image(
            painter = painterResource(id = R.drawable.uth2),
            contentDescription = "UTH Logo",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Welcome")
        Text("Ready to explore?Log in to get started.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { launcher.launch(googleSignInClient.signInIntent) }) {
            Text("SIGN IN WITH GOOGLE")
        }
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }
    }
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var name by remember { mutableStateOf(user?.displayName ?: "Unknown") }
    var email by remember { mutableStateOf(user?.email ?: "No Email") }
    var dob by remember { mutableStateOf("N/A") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.avt),
            contentDescription = "Profile Picture",
            modifier = Modifier.size(100.dp).background(Color.Gray, CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Profile", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("Date of Birth") })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            auth.signOut()
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
        }) {
            Text("Log Out")
        }
    }
}