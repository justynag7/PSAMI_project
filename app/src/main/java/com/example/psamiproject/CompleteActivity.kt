package com.example.psamiproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.psamiproject.data.UserActivity
import com.example.psamiproject.data.UserActivityRepo
import com.example.psamiproject.data.UserRepo
import com.example.psamiproject.databinding.ActivityCompleteActivityBinding
import com.example.psamiproject.history.ActivitiesHistory
import java.util.*

class CompleteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompleteActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.sendToDbBtn.setOnClickListener {

            val name = "swimming"
            val date = Date()
            val activity = UserActivity("", UserRepo.userId(), name, date.time)
            UserActivityRepo.addUserActivity(activity) {
                val intent = Intent(this, ActivitiesHistory::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
            }
        }
    }
}