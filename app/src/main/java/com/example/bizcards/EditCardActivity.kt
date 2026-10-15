package com.example.bizcards

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bizcards.databinding.ActivityEditBinding
import kotlinx.coroutines.launch

class EditCardActivity : AppCompatActivity() {

    companion object { const val EXTRA_ID = "card_id" }

    private lateinit var b: ActivityEditBinding
    private lateinit var dao: CardDao
    private var cardId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityEditBinding.inflate(layoutInflater)
        setContentView(b.root)
        dao = AppDb.get(this).cardDao()
        cardId = intent.getIntExtra(EXTRA_ID, 0)

        if (cardId != 0) {
            b.btnDelete.visibility = View.VISIBLE
            lifecycleScope.launch {
                dao.getById(cardId).collect { c ->
                    c ?: return@collect
                    b.etName.setText(c.name)
                    b.etCompany.setText(c.company)
                    b.etPhone.setText(c.phone)
                    b.etEmail.setText(c.email)
                    b.etNote.setText(c.note)
                }
            }
        }

        b.btnSave.setOnClickListener { save() }
        b.btnDelete.setOnClickListener { delete() }
    }

    private fun save() {
        val name = b.etName.text.toString().trim()
        if (name.isBlank()) { toast("Введите имя"); return }
        val card = Card(
            id = cardId,
            name = name,
            company = b.etCompany.text.toString().trim(),
            phone = b.etPhone.text.toString().trim(),
            email = b.etEmail.text.toString().trim(),
            note = b.etNote.text.toString().trim()
        )
        lifecycleScope.launch {
            dao.insert(card)
            toast("Сохранено")
            finish()
        }
    }

    private fun delete() {
        lifecycleScope.launch {
            dao.getById(cardId).collect { c ->
                c?.let { dao.delete(it) }
                finish()
                return@collect
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
