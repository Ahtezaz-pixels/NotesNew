package com.ahtezaz.notesapp.utils

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatDialog
import com.ahtezaz.notesapp.databinding.PhoneAudioRecorderBinding

class PhoneAudioRecordDialogue(context: Context, var recorder: RecorderListener) :
    AppCompatDialog(context) {
    var time = 1
    lateinit var binding: PhoneAudioRecorderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PhoneAudioRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.start.setOnClickListener {

            recorder.startRecording()
            timer().start()

        }



        binding.stop.setOnClickListener {
            recorder.stopRecording()
            timer().cancel()
            cancel()

        }
    }

   private fun timer() = object : CountDownTimer(300000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.timer.text = "0:" + checkDigit(time)
            time++
        }

        override fun onFinish() {
            binding.timer.text = "00:00"
        }
    }

  private  fun checkDigit(number: Int): String? {
        return if (number <= 9) "0$number" else number.toString()
    }


}