package com.notes.notesproxmlviews

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private var addNoteBtn: FloatingActionButton? = null
    private var recyclerView: RecyclerView? = null
    private var menuBtn: ImageButton? = null
    private var noteAdapter: NoteAdapter? = null
    private val noteList: MutableList<Note> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addNoteBtn = findViewById(R.id.add_note_btn)
        recyclerView = findViewById(R.id.recyler_view)
        menuBtn = findViewById(R.id.menu_btn)

        noteAdapter = NoteAdapter(this, noteList) { note: Note ->
            val intent = Intent(this, NoteDetailsActivity::class.java)
            intent.putExtra("title", note.getTitle())
            intent.putExtra("content", note.getContent())
            intent.putExtra("docId", note.getDocId())
            intent.putExtra("imageUrl", note.getImageUrl())
            startActivity(intent)
        }

        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = noteAdapter

        loadNotesFromFirebase()

        addNoteBtn!!.setOnClickListener {
            startActivity(Intent(this, NoteDetailsActivity::class.java))
        }

        menuBtn!!.setOnClickListener { showMenu() }
    }

    override fun onResume() {
        super.onResume()
        loadNotesFromFirebase()
    }

    private fun loadNotesFromFirebase() {
        Utility.getCollectionReferenceForNotes()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                noteList.clear()
                for (document in querySnapshot.documents) {
                    val note = document.toObject(Note::class.java)
                    if (note != null) {
                        note.setDocId(document.id)
                        noteList.add(note)
                    }
                }
                noteAdapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Utility.showToast(this, "Failed to load notes")
            }
    }

    private fun showMenu() {
        val popupMenu = android.widget.PopupMenu(this, menuBtn)
        popupMenu.menu.add("Logout")
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.title == "Logout") {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            } else false
        }
        popupMenu.show()
    }
}