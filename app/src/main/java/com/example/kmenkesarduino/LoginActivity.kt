package com.example.kmenkesarduino

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // get reference to all views
        var et_nik = findViewById<EditText>(R.id.et_nik)
        var et_password = findViewById<EditText>(R.id.et_password)
        var btn_reset = findViewById<Button>(R.id.btn_reset)
        var btn_submit = findViewById<Button>(R.id.btn_submit)

        btn_reset.setOnClickListener {
            // clearing user_name and password edit text views on reset button click
            et_nik.setText("")
            et_password.setText("")
        }

        // set on-click listener
        btn_submit.setOnClickListener {
            val user_name = et_nik.text.toString()
            val password = et_password.text.toString()

            if (user_name.isNotEmpty() && password.isNotEmpty()) {
                if ((user_name == "admin" || user_name == "Admin") && password == "Qwe123@@") {
                    intent = Intent(this, AdminPage::class.java)
                    startActivity(intent)
                }

                if (user_name == "3206020208980004" && password == "Qwe123@@") {
                    intent = Intent(this, UserPage::class.java)
                    intent.putExtra("user_name", user_name)
                    startActivity(intent)
                }
            }
        }
    }
}