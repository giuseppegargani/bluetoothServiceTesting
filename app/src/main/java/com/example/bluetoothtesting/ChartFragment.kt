package com.example.bluetoothtesting

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChartFragment : Fragment(), View.OnClickListener {

    private lateinit var chartChatInput: EditText
    private lateinit var chartSendButton: FrameLayout
    private var communicationListener: CommunicationListener? = null
    private var chatAdapter: ChatAdapter? = null
    private lateinit var chartRecyclerviewChat: RecyclerView
    private val messageList = arrayListOf<Message>()
    private lateinit var chartPressureView: TextView

    companion object {
        fun newInstance(): ChartFragment {
            val myFragment = ChartFragment()
            val args = Bundle()
            myFragment.arguments = args
            return myFragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mView: View = LayoutInflater.from(activity).inflate(R.layout.chart_fragment, container, false)

        initViews(mView)
        return mView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun initViews(mView: View) {

        chartChatInput = mView.findViewById(R.id.chartChatInput)
        val chatIcon: ImageView = mView.findViewById(R.id.chartSendIcon)
        chartSendButton = mView.findViewById(R.id.chartSendButton)
        chartRecyclerviewChat = mView.findViewById(R.id.chartChatRecyclerView)

            //Giuseppe
        chartPressureView = mView.findViewById(R.id.chartTextView5)

        chartSendButton.isClickable = false
        chartSendButton.isEnabled = false

        val llm = LinearLayoutManager(activity)
        llm.reverseLayout = true
        chartRecyclerviewChat.layoutManager = llm

        chartChatInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {

                if (s.isNotEmpty()) {
                    chatIcon.setImageDrawable(activity!!.getDrawable(R.drawable.ic_send))
                    chartSendButton.isClickable = true
                    chartSendButton.isEnabled = true
                }else {
                    chatIcon.setImageDrawable(activity!!.getDrawable(R.drawable.ic_send_depri))
                    chartSendButton.isClickable = false
                    chartSendButton.isEnabled = false
                }
            }
        })

        chartSendButton.setOnClickListener(this)


        chatAdapter = ChatAdapter(messageList.reversed(),requireActivity())
        chartRecyclerviewChat.adapter = chatAdapter

    }

    override fun onClick(p0: View?) {

        if (chartChatInput.text.isNotEmpty()){
            communicationListener?.onCommunication(chartChatInput.text.toString())
            chartChatInput.setText("")
        }

    }


    fun setCommunicationListener(communicationListener: CommunicationListener){
        this.communicationListener = communicationListener
    }

    interface CommunicationListener{
        fun onCommunication(message: String)
    }

    /*Piace il check sulla nullità della mamma activity

     */
    fun communicate(message: Message){
        messageList.add(message)
        if(activity != null) {
            chatAdapter = ChatAdapter(messageList.reversed(), requireActivity())
            chartRecyclerviewChat.adapter = chatAdapter
            chartRecyclerviewChat.scrollToPosition(0)
        }
    }

    //Giuseppe da mettere in ViewModel
    fun cambiaValore(stringa:String){
        chartPressureView.text = stringa
    }

}