package com.regadeveloper.chatapp.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.regadeveloper.chatapp.R
import com.regadeveloper.chatapp.activity.ConversationActivity
import com.regadeveloper.chatapp.adapter.ChatAdapter
import com.regadeveloper.chatapp.data.Chat
import com.regadeveloper.chatapp.listener.ChatClickedListener
import com.regadeveloper.chatapp.listener.FailureCallback
import com.regadeveloper.chatapp.util.DATA_CHATS
import com.regadeveloper.chatapp.util.DATA_USERS
import com.regadeveloper.chatapp.util.DATA_USER_CHATS
import kotlinx.android.synthetic.main.fragment_chat.*


class ChatFragment : Fragment(), ChatClickedListener {

    private var chatsAdapter = ChatAdapter(arrayListOf())
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var failureCallback: FailureCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (userId.isNullOrEmpty()) {
            failureCallback?.onUserError()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatsAdapter.setOnItemClickListener(this)
        rv_chats.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = chatsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        firebaseDb.collection(DATA_USERS).document(userId!!)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException == null) {
                    refreshChats()
                }
            }

    }

    fun newChat(partnerId: String) {
        firebaseDb.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener { userDocument ->
                val userChatPartners = hashMapOf<String, String>()
                if (userDocument[DATA_USER_CHATS] != null &&
                    userDocument[DATA_USER_CHATS] is HashMap<*, *>
                ) {
                    val userDocumentMap =
                        userDocument[DATA_USER_CHATS] as HashMap<String, String>
                    if (userDocumentMap.containsKey(
                            partnerId
                        )
                    ) {
                        return@addOnSuccessListener
                    } else {
                        userChatPartners.putAll(userDocumentMap)
                    }
                }
                firebaseDb.collection(DATA_USERS).document(partnerId)
                    .get()
                    .addOnSuccessListener { partnerDocument ->
                        val partnerChatPartners =
                            hashMapOf<String, String>()
                        if (partnerDocument[DATA_USER_CHATS] != null &&
                            partnerDocument[DATA_USER_CHATS] is HashMap<*, *>
                        ) {
                            val partnerDocumentMap =
                                partnerDocument[DATA_USER_CHATS] as HashMap<String, String>
                            partnerChatPartners.putAll(partnerDocumentMap)
                        }
                        val chatParticipants = arrayListOf(userId, partnerId)
                        val chat = Chat(chatParticipants)
                        val chatRef = firebaseDb.collection(DATA_CHATS).document()
                        val userRef = firebaseDb.collection(DATA_USERS).document(userId)
                        val partnerRef = firebaseDb.collection(DATA_USERS).document(partnerId)
                        userChatPartners[partnerId] = chatRef.id
                        partnerChatPartners[userId] = chatRef.id

                        val batch = firebaseDb.batch()
                        batch.set(chatRef, chat)
                        batch.update(userRef, DATA_USER_CHATS, userChatPartners)
                        batch.update(partnerRef, DATA_USER_CHATS, partnerChatPartners)
                        batch.commit()
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }


    override fun onChatClicked(
        chatId: String?,
        otherUserId: String?,
        chatsImageUrl: String?,
        chatsName: String?
    ) {
        startActivity(
            ConversationActivity.newIntent(context, chatId, chatsImageUrl, otherUserId, chatsName)
        )
    }

    fun setFailureCallbackListener(listener: FailureCallback) {
        failureCallback = listener
    }

    private fun refreshChats() {
        firebaseDb.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener {
                if (it.contains(DATA_USER_CHATS)) {
                    val partners = it[DATA_USER_CHATS]
                    val chats = arrayListOf<String>()
                    for (partner in (partners as HashMap<String, String>).keys) {
                        if (partners[partner] != null) {
                            chats.add(partners[partner]!!)
                        }
                    }
                    chatsAdapter.updateChats(chats)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }


    }
}