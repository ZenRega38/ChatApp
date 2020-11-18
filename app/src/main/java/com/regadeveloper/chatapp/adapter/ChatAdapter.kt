package com.regadeveloper.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.regadeveloper.chatapp.R
import com.regadeveloper.chatapp.data.User
import com.regadeveloper.chatapp.fragments.ChatFragment
import com.regadeveloper.chatapp.listener.ChatClickedListener
import com.regadeveloper.chatapp.util.DATA_CHATS
import com.regadeveloper.chatapp.util.DATA_CHAT_PARTICIPANTS
import com.regadeveloper.chatapp.util.DATA_USERS
import com.regadeveloper.chatapp.util.populateImage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_chat.*

class ChatAdapter(val chats: ArrayList<String>) :
    RecyclerView.Adapter<ChatAdapter.ChatsViewHolder>() {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var partnerId: String? = null
    private var chatName: String? = null
    private var chatImageUrl: String? = null

    private var chatClickedListener: ChatClickedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatsViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_chat, parent, false
        )
    )

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bindItem(chats[position], chatClickedListener)
    }

    override fun getItemCount() = chats.size

    fun setOnItemClickListener(listener: ChatFragment) {
        chatClickedListener = listener
        notifyDataSetChanged()
    }

    fun updateChats(updatedChats: ArrayList<String>) {
        chats.clear()
        chats.addAll(updatedChats)
        notifyDataSetChanged()
    }

    class ChatsViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bindItem(chatId: String, listener: ChatClickedListener?) {
            progress_layout.visibility = View.VISIBLE
            progress_layout.setOnTouchListener { v, event -> true }

            val firebaseDb = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            var partnerId: String? = null
            var chatName: String? = null
            var chatImageUrl: String? = null

            firebaseDb.collection(DATA_CHATS)
                .document(chatId)
                .get()
                .addOnSuccessListener {
                    val chatParticipants = it[DATA_CHAT_PARTICIPANTS]
                    if (chatParticipants != null) {
                        for (participant in chatParticipants as ArrayList<String>) {
                            if (participant != null && !participant.equals(userId)) {
                                partnerId = participant
                                firebaseDb.collection(DATA_USERS).document(partnerId!!).get()
                                    .addOnSuccessListener {
                                        val user = it.toObject(User::class.java)
                                        chatImageUrl = user?.imageUrl
                                        chatName = user?.name
                                        txt_chats.text = user?.name
                                        populateImage(
                                            img_chats.context, user?.imageUrl,
                                            img_chats, R.drawable.ic_user)
                                        progress_layout.visibility = View.GONE
                                    }
                                    .addOnFailureListener { e ->
                                        e.printStackTrace()
                                        progress_layout.visibility = View.GONE
                                    }
                            }
                        }
                    }
                    progress_layout.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    progress_layout.visibility = View.GONE
                }
            itemView.setOnClickListener {
                listener?.onChatClicked(chatId, userId, chatImageUrl, chatName)
            }
        }
    }


}
