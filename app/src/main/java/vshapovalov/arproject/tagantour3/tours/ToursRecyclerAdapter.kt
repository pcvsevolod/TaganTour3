package vshapovalov.arproject.tagantour3.tours

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.*
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import vshapovalov.arproject.tagantour3.MainActivity
import vshapovalov.arproject.tagantour3.R
import vshapovalov.arproject.tagantour3.Tour
import java.io.File

class ToursRecyclerAdapter(var tours_list: List<ToursItem>, var act: MainActivity) : RecyclerView.Adapter<ToursRecyclerAdapter.ViewHolder>(){

    @NonNull
    override fun onCreateViewHolder(
        @NonNull parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.tours_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        @NonNull holder: ViewHolder,
        position: Int
    ) {
        //val id: Int = collection_list[position].id
        val title: String = tours_list[position].tour!!.name!!
        holder.title.text = title
        //holder.image.setImageResource(id)
        holder.cardView.setOnClickListener {
            openTourDialog(tours_list[position].tour!!, holder.cont)
        }
        val name = tours_list[position].tour!!.uniqueId
        if(name != "") {

            val outDir = holder.cont.cacheDir
            val root = File(outDir, "images")
            if (!root.exists()) {
                root.mkdir()
            }
            val fileName = "tour_$name.jpg"
            val localFile = File(outDir, fileName)
            if(!localFile.exists()) {
                //Log.i(TAG, "582 File doesn't exist, Loading $name")
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/$fileName")
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

    fun openTourDialog(tour : Tour, cont : Context) {
        val dialog = Dialog(cont)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog .setCancelable(true)
        dialog .setContentView(R.layout.dialog_tour_info)
        val textTitle = dialog .findViewById<TextView>(R.id.d_tour_info_title)
        textTitle.text = Html.fromHtml(tour.name)
        val textDesc = dialog .findViewById<TextView>(R.id.d_tour_info_desc)
        textDesc.movementMethod = ScrollingMovementMethod()
        textDesc.text = Html.fromHtml(tour.description)
        val btnClose = dialog .findViewById(R.id.d_tour_info_btn_close) as Button
        btnClose.setOnClickListener {
            dialog .dismiss()
        }
        val btnStartTour = dialog .findViewById(R.id.d_tour_info_btn_start) as Button
        btnStartTour.setOnClickListener {
            act.startTour(tour)
            dialog .dismiss()
        }
        val image = dialog .findViewById<ImageView>(R.id.d_tour_info_image)
        val name = tour.uniqueId
        if(name != "") {
            val outDir = cont.cacheDir
            val root = File(outDir, "images")
            if (!root.exists()) {
                root.mkdir()
            }

            val fileName = "tour_$name.jpg"

            val localFile = File(outDir, fileName)
            if(!localFile.exists()) {
                //Log.i(TAG, "582 File doesn't exist, Loading $name")
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/$fileName")
                imageRef.getFile(localFile)
                    .addOnSuccessListener {
                        image.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                        //Log.i(TAG, "582 Created file in \"" + localFile.absolutePath + "\"")
                    }.addOnFailureListener {
                        Toast.makeText(cont, "Download Failed", Toast.LENGTH_LONG).show()
                    }
            }
            else {
                image.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                //Toast.makeText(holder.cont, "File exists", Toast.LENGTH_LONG).show()
            }
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        dialog.show()

        dialog.window!!.attributes = lp
    }

    private val itemCount: Int = 0
    override fun getItemCount(): Int {
        //TODO("Not yet implemented")
        return tours_list.size
    }

    inner class ViewHolder(private val mView: View) :
        RecyclerView.ViewHolder(mView) {
        val title: TextView = mView.findViewById(R.id.tours_item_title)
        val image: ImageView = mView.findViewById(R.id.tours_item_image)
        //var btn_start: ImageButton = mView.findViewById(R.id.tours_item_btn_open)
        var cardView: CardView = mView.findViewById(R.id.cardview)
        val cont : Context = mView.context
        //var act : Activity = mView.activity
    }
}