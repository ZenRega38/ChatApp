package com.regadeveloper.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.regadeveloper.chatapp.MainActivity
import com.regadeveloper.chatapp.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.edt_email_login
import kotlinx.android.synthetic.main.activity_login.til_email_login

class LoginActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_login)

        setTextChangeListener(edt_email_login, til_email_login)
        setTextChangeListener(edt_password_login, til_password_login)

        btn_login.setOnClickListener {
            onLogin()
        }
        txt_signup.setOnClickListener {
            onSignup()
        }
    }

    private fun setTextChangeListener(edt: TextInputEditText?, til: TextInputLayout?){
        edt?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til?.isErrorEnabled = false
            }
        })
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener { firebaseAuthListener }
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.addAuthStateListener { firebaseAuthListener }
    }

    private fun onLogin() {
        var process = true
        if (edt_email_login.text.isNullOrEmpty()){
            til_email_login.error = "Membutuhkan Email"
            til_email_login.isErrorEnabled = true
            process = false
        }
        if (edt_password_login.text.isNullOrEmpty()){
            til_password_login.error = "Membutuhkan Password"
            til_password_login.isErrorEnabled = true
            process = false
        }

        if (process){
            progress_layout_login.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(
                edt_email_login.text.toString(),
                edt_password_login.text.toString()
            ).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    progress_layout_login.visibility = View.GONE
                    Toast.makeText(this, "Login Woi! ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT)
                }
            }.addOnFailureListener { e ->
                progress_layout_login.visibility = View.GONE
                e.printStackTrace()
            }
        }
    }

    private fun onSignup() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }
}