package br.gov.sp.etec.reminderx
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextWatcher
import android.text.format.DateUtils.formatDateTime
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.textfield.TextInputEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AddReminderActivity : AppCompatActivity() {

    private lateinit var reminderNameEditText: TextInputEditText
    private lateinit var dateTimePickerEditText: TextInputEditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        reminderNameEditText = findViewById(R.id.reminderNameEditText)
        dateTimePickerEditText = findViewById(R.id.dateTimePickerEditText)
        val saveReminderBtn: Button = findViewById(R.id.saveReminderBtn)
        val cancelReminderBtn: Button = findViewById(R.id.cancelReminderBtn)

        saveReminderBtn.setOnClickListener { saveReminder() }
        cancelReminderBtn.setOnClickListener { finish() }

        sharedPreferences = getSharedPreferences("reminders", Context.MODE_PRIVATE)

    }

    private fun saveReminder() {
        val reminderName = reminderNameEditText.text.toString().trim()
        val dateTime = dateTimePickerEditText.text.toString().trim()

        if (reminderName.isEmpty() || dateTime.isEmpty()) {
            Toast.makeText(this, "Por favor, insira o nome e a data/hora do lembrete", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDateTimeMillis = convertDateTimeToMillis(dateTime)

        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDateTime = dateTimeFormat.format(selectedDateTimeMillis)

        if (reminderName.isNotEmpty() && dateTime.isNotEmpty()) {

            setAlarm(formattedDateTime, reminderName)

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val currentDateAndTime: String = sdf.format(Date())

            val remindersSet = HashSet(sharedPreferences.getStringSet("reminders", HashSet())!!)
            remindersSet.add("$reminderName - $dateTime - $currentDateAndTime")
            sharedPreferences.edit().putStringSet("reminders", remindersSet).apply()

            Toast.makeText(this, "Lembrete $reminderName salvo com sucesso", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun convertDateTimeToMillis(dateTimeString: String): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        try {
            val date = sdf.parse(dateTimeString)
            return date?.time ?: 0
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    };

    private fun setAlarm(dateTime: String, reminderName: String) {
        val alarmManager = this@AddReminderActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Parse da data e hora do lembrete
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val reminderDateTime = sdf.parse(dateTime)
        val reminderTimeMillis = reminderDateTime!!.time

        val alarmIntent = Intent(this@AddReminderActivity, AlarmReceiver::class.java).apply {
            putExtra("message", reminderName)
        }
        val pendingIntent = PendingIntent.getBroadcast(this@AddReminderActivity, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent)

//        createNotificationChannel()
//        showNotification(reminderName)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ReminderChannel"
            val descriptionText = "Channel for reminder notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("reminder_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(reminderName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val builder = NotificationCompat.Builder(this, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Lembrete")
            .setContentText(reminderName)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@AddReminderActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(0, builder.build())
        }
    }
}