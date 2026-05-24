package com.notes.notesproxmlviews

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NoteDetailsActivity : AppCompatActivity() {

    private var titleEditText: EditText? = null
    private var contentEditText: EditText? = null
    private var saveNoteBtn: ImageButton? = null
    private var pageTitleTextView: TextView? = null
    private var deleteNoteTextViewBtn: FloatingActionButton? = null
    private var noteImageView: ImageView? = null
    private var addImageBtn: Button? = null
    private var removeImageBtn: Button? = null

    private var docId: String? = null
    private var isEditMode: Boolean = false
    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            noteImageView?.setImageURI(it)
            noteImageView?.visibility = View.VISIBLE
            removeImageBtn?.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)

        titleEditText = findViewById(R.id.notes_title_text)
        contentEditText = findViewById(R.id.notes_content_text)
        saveNoteBtn = findViewById(R.id.save_note_btn)
        pageTitleTextView = findViewById(R.id.page_title)
        deleteNoteTextViewBtn = findViewById(R.id.delete_note_text_view_btn)
        noteImageView = findViewById(R.id.note_image_view)
        addImageBtn = findViewById(R.id.add_image_btn)
        removeImageBtn = findViewById(R.id.remove_image_btn)

        titleEditText!!.setText(intent.getStringExtra("title"))
        contentEditText!!.setText(intent.getStringExtra("content"))
        docId = intent.getStringExtra("docId")
        existingImageUrl = intent.getStringExtra("imageUrl")

        if (!docId.isNullOrEmpty()) {
            isEditMode = true
        }

        if (isEditMode) {
            pageTitleTextView!!.text = getString(R.string.edit_your_note)
            deleteNoteTextViewBtn!!.visibility = View.VISIBLE
        }

        // Carregar imagem existente
        val imageUrl = existingImageUrl
        if (!imageUrl.isNullOrEmpty()) {
            noteImageView?.visibility = View.VISIBLE
            removeImageBtn?.visibility = View.VISIBLE
            Glide.with(this).load(Uri.parse(imageUrl)).into(noteImageView!!)
        }

        saveNoteBtn!!.setOnClickListener { saveNote() }
        deleteNoteTextViewBtn!!.setOnClickListener { deleteNoteFromFirebase() }
        addImageBtn!!.setOnClickListener { pickImageLauncher.launch("image/*") }
        removeImageBtn!!.setOnClickListener {
            selectedImageUri = null
            existingImageUrl = null
            noteImageView?.setImageDrawable(null)
            noteImageView?.visibility = View.GONE
            removeImageBtn?.visibility = View.GONE
        }
    }

    private fun saveNote() {
        val noteTitle = titleEditText!!.text.toString()
        val noteContent = contentEditText!!.text.toString()

        if (noteTitle.isEmpty()) {
            titleEditText!!.error = "Title is required"
            return
        }

        val note = Note()
        note.setTitle(noteTitle)
        note.setContent(noteContent)
        note.setTimestamp(Timestamp.now())

        if (selectedImageUri != null) {
            // converte para Base64
            val base64 = uriToBase64(selectedImageUri!!)
            note.setImageUrl(base64)
        } else {
            note.setImageUrl(existingImageUrl)
        }

        saveNoteToFirebase(note)
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            else null
        } catch (e: Exception) {
            null
        }
    }

    private fun saveNoteToFirebase(note: Note) {
        val documentReference: DocumentReference = if (isEditMode) {
            Utility.getCollectionReferenceForNotes().document(docId.toString())
        } else {
            Utility.getCollectionReferenceForNotes().document()
        }

        documentReference.set(note).addOnCompleteListener(object : OnCompleteListener<Void?> {
            override fun onComplete(task: Task<Void?>) {
                if (task.isSuccessful) {
                    Utility.showToast(this@NoteDetailsActivity, "Note added successfully")
                    finish()
                } else {
                    Utility.showToast(this@NoteDetailsActivity, "Failed while adding note")
                }
            }
        })
    }

    private fun deleteNoteFromFirebase() {
        val documentReference: DocumentReference =
            Utility.getCollectionReferenceForNotes().document(docId.toString())

        documentReference.delete().addOnCompleteListener(object : OnCompleteListener<Void?> {
            override fun onComplete(task: Task<Void?>) {
                if (task.isSuccessful) {
                    Utility.showToast(this@NoteDetailsActivity, "Note deleted successfully")
                    finish()
                } else {
                    Utility.showToast(this@NoteDetailsActivity, "Failed while deleting note")
                }
            }
        })
    }
}