package com.example.scrollsnadbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), Onclick {
    override fun click(data: String) {
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
    }

    private val myDataset: ArrayList<String> = arrayListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MyAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val range = 1..10

        (range).forEach { element -> myDataset.add(element.toString()) }
        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(myDataset)
        viewAdapter.setClickListener(this)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }
}

class MyAdapter(private val myDataset: List<String>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    private var listener: Onclick? = null

    fun setClickListener(listener: Onclick) {
        this.listener = listener
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_text_view, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = myDataset[position]
        listener?.let {
            holder.textView.setOnClickListener {
                listener?.click(myDataset[position])
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}

interface Onclick {
    fun click(data: String)
}