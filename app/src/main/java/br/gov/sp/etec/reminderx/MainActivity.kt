package br.gov.sp.etec.reminderx

import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var reminderAdapter: ArrayAdapter<String>
    private lateinit var reminderList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reminderAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        reminderList = findViewById(R.id.reminderList)


        val fab: FloatingActionButton = findViewById(R.id.fab)

        reminderList.adapter = reminderAdapter

        fab.setOnClickListener {
            val intent = Intent(this, AddReminderActivity::class.java)
            startActivity(intent)
        }

        updateReminderList()
    }

    private fun updateReminderList() {
        val sharedPreferences = getSharedPreferences("reminders", Context.MODE_PRIVATE)
        val remindersSet = sharedPreferences.getStringSet("reminders", HashSet()) ?: emptySet()

        val reminderArray = remindersSet.toTypedArray()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, reminderArray)
        reminderList.adapter = adapter

        reminderAdapter.notifyDataSetChanged()

    }

    override fun onResume() {
        super.onResume()
        updateReminderList()
    }

}