package com.example.bizcards

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bizcards.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var dao: CardDao
    private lateinit var adapter: CardAdapter
    private var query = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        dao = AppDb.get(this).cardDao()
        adapter = CardAdapter(
            onClick = { card -> openEdit(card.id) },
            onLongClick = { card -> confirmDelete(card) }
        )
        b.rvCards.layoutManager = LinearLayoutManager(this)
        b.rvCards.adapter = adapter

        b.fabAdd.setOnClickListener {
            startActivity(Intent(this, EditCardActivity::class.java))
        }

        b.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true
            override fun onQueryTextChange(q: String?): Boolean {
                query = q.orEmpty()
                load()
                return true
            }
        })

        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val flow = if (query.isBlank()) dao.getAll() else dao.search(query)
            flow.collectLatest { adapter.submitList(it) }
        }
    }

    private fun openEdit(id: Int) {
        val i = Intent(this, EditCardActivity::class.java)
        i.putExtra(EditCardActivity.EXTRA_ID, id)
        startActivity(i)
    }

    private fun confirmDelete(card: Card) {
        val actions = arrayOf("Позвонить", "Удалить", "Отмена")
        MaterialAlertDialogBuilder(this)
            .setTitle(card.name)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> call(card.phone)
                    1 -> lifecycleScope.launch { dao.delete(card); toast("Удалено") }
                }
            }
            .show()
    }

    private fun call(phone: String) {
        if (phone.isBlank()) { toast("Нет номера"); return }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone")))
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CALL_PHONE), 1)
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
