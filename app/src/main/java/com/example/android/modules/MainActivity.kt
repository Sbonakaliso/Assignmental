package com.example.android.modules

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.lang.Integer.parseInt
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), AssignmentAdapter.InteractionListener {

    private lateinit var etName: EditText
    private lateinit var etDate: EditText
    private lateinit var sbProgress: SeekBar
    private lateinit var rvAssignments: RecyclerView
    private lateinit var cardView: CardView
    private lateinit var btnSave: Button
    private lateinit var btnNew: Button
    private lateinit var tvActionTitle: TextView
    private lateinit var tvCurrentProgress: TextView
    private lateinit var pbLoading: ProgressBar

    private val assignmentList = ArrayList<Assignment>()
    private var assignmentListDB = ArrayList<Assignment>()
    var currentProgress: String = "Current Progress (0%)"
    val TAG: String = "Mgoza"
    var editingIndex: Int = 0
    var isEditing = false
    var isDeleting = false

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var assignment: Assignment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.etName)
        etDate = findViewById(R.id.etDate)
        sbProgress = findViewById(R.id.sbProgress)
        rvAssignments = findViewById(R.id.rvAssignments)
        cardView = findViewById(R.id.cardView)
        btnSave = findViewById(R.id.btnSave)
        btnNew = findViewById(R.id.btnNew)
        tvActionTitle = findViewById(R.id.tvActionTitle)
        tvCurrentProgress = findViewById(R.id.tvCurrentProgress)
        pbLoading = findViewById(R.id.pbLoading)

        etName.addTextChangedListener{
            showBtnIfFormFilled()
        }
        etDate.addTextChangedListener{
            showBtnIfFormFilled()
        }
        btnSave.setOnClickListener {
            if(!isEditing) {
                val id: String = Calendar.getInstance().time.toString().trim()
                assignment = Assignment(id, etName.text.toString().trim(), etDate.text.toString().trim(), sbProgress.progress)
                addToRealtimeDatabase(assignment)
            }else{
                val id = assignmentListDB[editingIndex].id
                assignment = Assignment(id, etName.text.toString().trim(), etDate.text.toString().trim(), sbProgress.progress)
                onUpdate(assignment)
            }
            setAdapter(assignmentListDB)

            //Clear everything
            etName.text.clear()
            etDate.text.clear()
            sbProgress.progress = 0
            btnSave.isEnabled = false
            tvActionTitle.text = "New Assignment"
            btnNew.visibility = View.VISIBLE
            cardView.visibility = View.GONE
        }
        btnNew.setOnClickListener {
            isEditing = false
            cardView.visibility = View.VISIBLE
            btnNew.visibility = View.GONE
        }
        sbProgress.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentProgress = "Current Progress ($progress%)"
                tvCurrentProgress.text = currentProgress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("Assignment")


        assignment = Assignment()
        getDataWhenChanged()
    }

    private fun getDataWhenChanged(){
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChildren())
                    assignmentListDB.clear()

                for(eachAssignment in snapshot.children){
                    eachAssignment.child("id")
                    val assignmentMod = eachAssignment.getValue(Assignment::class.java)
                    assignmentListDB.add(assignmentMod as Assignment)
                }

                setAdapter(assignmentListDB)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.toString()}")
            }

        })
    }

    private fun addToRealtimeDatabase(assignment: Assignment) {
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!isEditing && !isDeleting) {
                    assignment.id?.let { databaseReference.child(it).setValue(assignment) }

                    var statusStr: String = if(isEditing)
                        "Record was updated in"
                    else
                        "Item was added to"

                    Toast.makeText(this@MainActivity, "$statusStr database", Toast.LENGTH_SHORT).show()
                }else{
                    isEditing = false
                    isDeleting = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Data not added to database", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun showBtnIfFormFilled() {
        btnSave.isEnabled = etName.text.isNotBlank() && etDate.text.isNotBlank()
    }

    private fun setAdapter(currentList: ArrayList<Assignment>) {
        pbLoading.visibility = View.GONE
        val assignmentAdapter = AssignmentAdapter(currentList, this)
        val linearLayoutManager = LinearLayoutManager(this)
        rvAssignments.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvAssignments.layoutManager = linearLayoutManager
        rvAssignments.adapter = assignmentAdapter
    }
    private fun onUpdate(assignment: Assignment){
        var assignmentMap = HashMap<String, Any>()
        assignmentMap["id"] = assignment.id.toString()
        assignmentMap["name"] = assignment.name.toString()
        assignmentMap["date"] = assignment.date.toString()
        assignmentMap["progress"] = parseInt(assignment.progress.toString())

        val updateID = assignmentListDB[editingIndex].id
        if(updateID != null){
            databaseReference.child(updateID).updateChildren(assignmentMap as Map<String, Any>)
        }
    }

    override fun onDelete(position: Int) {
        val assignmentID = assignmentListDB[position].id
        Log.d(TAG, "onDelete: removing $assignmentID")

        if (assignmentID != null) {
            isDeleting = true
            databaseReference.child(assignmentID).removeValue()
        }
    }

    override fun onEdit(position: Int) {
        isEditing = true
        editingIndex = position
        tvActionTitle.text = "Editing ${assignmentListDB[position].name}"
        etName.setText(assignmentListDB[position].name)
        etDate.setText(assignmentListDB[position].date)
        sbProgress.progress = assignmentListDB[position].progress!!

        btnNew.visibility = View.GONE
        cardView.visibility = View.VISIBLE
        Toast.makeText(this, "Editing at $position", Toast.LENGTH_SHORT).show()
    }

}