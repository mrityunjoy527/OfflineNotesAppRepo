package com.example.notesmvvm.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.notesmvvm.R
import com.example.notesmvvm.databinding.ActivityMainBinding
import com.example.notesmvvm.db.NoteDatabase
import com.example.notesmvvm.repository.NoteRepository
import com.example.notesmvvm.viewModel.NoteActivityViewModel
import com.example.notesmvvm.viewModel.NoteActivityViewModelFactory
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        try {
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteActivityViewModelFactory = NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(this, noteActivityViewModelFactory)[NoteActivityViewModel::class.java]
        } catch (e: Exception) {
            Log.d("Tag", "Error")
        }
    }
}