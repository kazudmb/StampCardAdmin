package com.kazukinakano.stampcardadmin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.tool_bar)
        toolbar.title = getString(R.string.app_name)
        setSupportActionBar(toolbar)

        scan.setOnClickListener {
            val intent = Intent(this, QRCodeScanActivity::class.java)
            startActivity(intent)
        }
    }
}
