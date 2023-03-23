package com.example.kmenkesarduino

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.*

class AdminPage : AppCompatActivity() {

    data class User (
        val nik: String,
        val full_name: String,
        val age: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_page)

        var et_nik = findViewById<EditText>(R.id.et_nik)
        var btn_submit = findViewById<Button>(R.id.btn_submit_nik)

        btn_submit.setOnClickListener {
            val nik = et_nik.text.toString()

            if (nik.isNotEmpty()) {

                val data = listOf(
                    User("3206020208980004", "Ujang Randes", "02-08-1998"),
                    User("3206020208180004", "Lolo Kids", "02-08-2018")
                )

                val result = when (nik) {
                    "3206020208980004" -> 0
                    "3206020208180004" -> 1
                    else -> -1
                }

                if (result != -1) {
                    intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("nik", data[result].nik)
                    intent.putExtra("full_name", data[result].full_name)
                    intent.putExtra("age", data[result].age)
                    startActivity(intent)
                }else {
                    Toast.makeText(this, "NIK tidak ditemukan", Toast.LENGTH_SHORT).show()
                }

            }else {
                Toast.makeText(this, "NIK tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }
}