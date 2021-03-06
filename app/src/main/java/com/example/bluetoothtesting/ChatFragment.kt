package com.example.bluetoothtesting

import android.app.PendingIntent.getActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothtesting.databinding.ChatFragmentBinding
import com.google.android.material.internal.ContextUtils.getActivity

/**
 * Created by giuseppe gargani
 */

class ChatFragment : Fragment(), View.OnClickListener {

    private lateinit var chatInput: EditText
    private lateinit var sendButton: FrameLayout
    private var communicationListener: CommunicationListener? = null
    private var chatAdapter: ChatAdapter? = null
    private lateinit var recyclerviewChat: RecyclerView
    private val messageList = arrayListOf<Message>()
    private lateinit var pressureView: TextView

    companion object {
        fun newInstance(): ChatFragment {
            val myFragment = ChatFragment()
            val args = Bundle()
            myFragment.arguments = args
            return myFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val mView: View = LayoutInflater.from(activity).inflate(R.layout.chat_fragment, container, false)

        initViews(mView)
        return mView
    }

    private fun initViews(mView: View) {

        chatInput = mView.findViewById(R.id.chatInput)
        val chatIcon: ImageView = mView.findViewById(R.id.sendIcon)
        sendButton = mView.findViewById(R.id.sendButton)
        recyclerviewChat = mView.findViewById(R.id.chatRecyclerView)

        //chartButton
        val mActivity = requireActivity() as MainActivity
        val chartButton = mView.findViewById<Button>(R.id.chartButton)
        chartButton.setOnClickListener {
            mActivity.showChartFragment()
        }

        //Giuseppe
        pressureView = mView.findViewById(R.id.textView5)

        sendButton.isClickable = false
        sendButton.isEnabled = false

        val llm = LinearLayoutManager(activity)
        llm.reverseLayout = true
        recyclerviewChat.layoutManager = llm

        chatInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {

                if (s.isNotEmpty()) {
                    chatIcon.setImageDrawable(activity!!.getDrawable(R.drawable.ic_send))
                    sendButton.isClickable = true
                    sendButton.isEnabled = true
                }else {
                    chatIcon.setImageDrawable(activity!!.getDrawable(R.drawable.ic_send_depri))
                    sendButton.isClickable = false
                    sendButton.isEnabled = false
                }
            }
        })

        sendButton.setOnClickListener(this)


        chatAdapter = ChatAdapter(messageList.reversed(),requireActivity())
        recyclerviewChat.adapter = chatAdapter

    }

    override fun onClick(p0: View?) {

        if (chatInput.text.isNotEmpty()){
            communicationListener?.onCommunication(chatInput.text.toString())
            chatInput.setText("")
        }

    }


    fun setCommunicationListener(communicationListener: CommunicationListener){
        this.communicationListener = communicationListener
    }

    interface CommunicationListener{
        fun onCommunication(message: String)
    }

    /*Piace il check sulla nullit?? della mamma activity

     */
    fun communicate(message: Message){
        messageList.add(message)
        if(activity != null) {
            chatAdapter = ChatAdapter(messageList.reversed(), requireActivity())
            recyclerviewChat.adapter = chatAdapter
            recyclerviewChat.scrollToPosition(0)
        }
    }

    //Giuseppe da mettere in ViewModel
    fun cambiaValore(stringa:String){
        pressureView.text = stringa
    }

}