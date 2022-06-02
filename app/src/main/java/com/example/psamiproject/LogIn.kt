package com.example.psamiproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import android.util.Log


class LogIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_in)

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("466349359517-sdqnj3csf9n20rfh8a66ov6jf4ouqsrc.apps.googleusercontent.com")
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        var sign_in_button = findViewById(R.id.sign_in_button) as SignInButton
        sign_in_button.visibility = View.VISIBLE
        var tv_name = findViewById(R.id.tv_name) as TextView
        //tv_name.visibility = View.GONE
        sign_in_button.setSize(SignInButton.SIZE_STANDARD)
        sign_in_button.setOnClickListener{
            val signInIntent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(signInIntent)
            //getResult.launch(signInIntent)

        }
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            sign_in_button.visibility = View.GONE
            tv_name.text= acct.displayName
            tv_name.visibility = View.VISIBLE

        }

    }

    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("logowanie1",result.toString())
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("logowanie2",result.toString())
            // parse result and perform action
        }
    }
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            Log.d("logowanie",it.resultCode.toString())
            if (it.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                handleSignInResult(task)
            }
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d("annaf","AnnaF")
            runOnUiThread({
            // Signed in successfully, show authenticated UI.
            var sign_in_button = findViewById(R.id.sign_in_button) as SignInButton
            sign_in_button.visibility = View.GONE

            var tv_name = findViewById(R.id.tv_name) as TextView
                tv_name.text= "Anna"//account.displayName
                tv_name.visibility = View.VISIBLE})


        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            var sign_in_button = findViewById(R.id.sign_in_button) as SignInButton
            sign_in_button.visibility = View.VISIBLE
            var tv_name = findViewById(R.id.tv_name) as TextView
            //tv_name.text=""
            //tv_name.visibility = View.GONE
        }
    }
}