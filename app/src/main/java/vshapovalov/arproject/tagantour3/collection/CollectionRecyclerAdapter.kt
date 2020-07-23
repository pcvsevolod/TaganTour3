package vshapovalov.arproject.tagantour3.collection

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import vshapovalov.arproject.tagantour3.ArActivity
import vshapovalov.arproject.tagantour3.DialogPlace
import vshapovalov.arproject.tagantour3.R
import java.io.File

class CollectionRecyclerAdapter(var collection_list: List<CollectionItem>) : RecyclerView.Adapter<CollectionRecyclerAdapter.ViewHolder>()
{
    val TAG = "CollectionRecyclerAdapter"

    @NonNull
    override fun onCreateViewHolder(
        @NonNull parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.collection_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        @NonNull holder: ViewHolder,
        position: Int
    ) {
        //val id: Int = collection_list[position].id
        val title: String = collection_list[position].place!!.name!!
        holder.title.text = title
        //holder.image.setImageResource(id)
        holder.btn_open.setOnClickListener{
            //Toast.makeText(holder.cont, "opa", Toast.LENGTH_LONG).show()
            val intent = Intent(holder.cont, ArActivity::class.java)
            intent.putExtra("name", collection_list[position].place!!.fileName!!)
            holder.cont.startActivity(intent)
        }
        holder.btn_info.setOnClickListener {
            val dialogPlace = DialogPlace(holder.cont, "mock")
            dialogPlace.openInfo(collection_list[position].place!!)
        }

        val name = collection_list[position].place!!.fileName

        if(name != "") {

            val outDir = holder.cont.cacheDir
            val root = File(outDir, "images")
            if (!root.exists()) {
                root.mkdir()
            }

            val fileName = "col_image_$name.jpg"

            val localFile = File(outDir, fileName)
            if(!localFile.exists()) {
                //Log.i(TAG, "582 File doesn't exist, Loading $name")
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/$name.jpg")
                imageRef.getFile(localFile)
                    .addOnSuccessListener {
                        holder.image.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                        //Log.i(TAG, "582 Created file in \"" + localFile.absolutePath + "\"")
                    }.addOnFailureListener {
                        Toast.makeText(holder.cont, "Download Failed", Toast.LENGTH_LONG).show()
                    }
            }
            else {
                holder.image.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                //Toast.makeText(holder.cont, "File exists", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val itemCount: Int = 0
    override fun getItemCount(): Int {
        //TODO("Not yet implemented")
        return collection_list.size
    }
    //get() = home_list.size

    inner class ViewHolder(private val mView: View) :
        RecyclerView.ViewHolder(mView) {
        val title: TextView = mView.findViewById(R.id.col_item_title)
        val image: ImageView = mView.findViewById(R.id.col_item_image)
        var btn_open: Button = mView.findViewById(R.id.col_item_btn_display_model)
        var btn_info: Button = mView.findViewById(R.id.col_item_btn_show_info)
        val cont : Context = mView.context
    }

}