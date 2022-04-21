package com.example.psamiproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val Zaloguj = findViewById(R.id.button_zaloguj) as Button
        val Tekst = findViewById(R.id.tekst) as TextView
        val Zarejestruj = findViewById(R.id.button_zarejestruj) as Button
        Zaloguj.setOnClickListener() {
            Tekst.setText("Zaloguj")
        }
        Zarejestruj.setOnClickListener(){
            Tekst.setText("Zarejestruj")
        }

    }

}