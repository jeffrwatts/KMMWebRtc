package com.jeffrwatts.kmmwebrtc.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.jeffrwatts.kmmwebrtc.Greeting
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.jeffrwatts.kmmwebrtc.Dog

class MainActivity : AppCompatActivity() {

    private val buttonRefresh: Button by lazy { findViewById(R.id.buttonRefresh) }
    private val recyclerViewDogs: RecyclerView by lazy { findViewById(R.id.recyclerViewDogs) }
    private val dogViewModel by lazy { ViewModelProvider(this)[DogViewModel::class.java] }
    private val dogAdapter by lazy { DogAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager = LinearLayoutManager(this)
        recyclerViewDogs.layoutManager = layoutManager;
        val dividerItemDecoration = DividerItemDecoration(recyclerViewDogs.context, layoutManager.orientation)
        recyclerViewDogs.addItemDecoration(dividerItemDecoration)
        recyclerViewDogs.adapter = dogAdapter

        buttonRefresh.setOnClickListener {
            dogViewModel.getDogs().observe(this) { dogs ->
                dogAdapter.submitList(dogs)
            }
        }

        dogViewModel.getDogs().observe(this) { dogs ->
            dogAdapter.submitList(dogs)
        }
    }
}

class DogAdapter : ListAdapter<Dog, DogAdapter.DogViewHolder>(DogDiffCallback) {
    class DogViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val nameView = itemView.findViewById<TextView>(android.R.id.text1)
        fun bind (dog: Dog) {
            nameView.text = dog.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = getItem(position)
        holder.bind(dog)
    }

    object DogDiffCallback : DiffUtil.ItemCallback<Dog>() {
        override fun areItemsTheSame(oldItem: Dog, newItem: Dog): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Dog, newItem: Dog): Boolean {
            return oldItem == newItem
        }
    }
}