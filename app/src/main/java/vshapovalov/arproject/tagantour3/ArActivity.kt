package vshapovalov.arproject.tagantour3

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_ar.*
import java.io.File


class ArActivity : AppCompatActivity() {
    val TAG = "ArActivity"

    private lateinit var arFragment: ArFragment
    private lateinit var selectedObject: Uri
    lateinit var storageRef : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment
        storageRef = FirebaseStorage.getInstance().reference

        ar_btn_back.setOnClickListener {
            finish()
        }

        FirebaseApp.initializeApp(this)

        val intent = intent
        var name = intent.getStringExtra("name")
        val outDir = this.cacheDir
        val root = File(outDir, "images")
        if (!root.exists()) {
            root.mkdir()
        }

        val fileName = "model_$name.sfb"

        val localFile = File(outDir, fileName)
        if(!localFile.exists()) {val modelRef = storageRef.child("models/$name.sfb")
            modelRef.getFile(localFile)
                .addOnSuccessListener {
                    // Successfully downloaded data to local file
                    // ...

                    //Tab listener for the ArFragment
                    Log.i(TAG, "Created temp file in \"" + localFile.absolutePath + "\"")
                    Toast.makeText(this, "Downloaded", Toast.LENGTH_LONG).show()
                    setModelPath(localFile.path)
                    arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
                        //If surface is not horizontal and upward facing
                        if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                            //return for the callback
                            return@setOnTapArPlaneListener
                        }
                        //create a new anchor
                        val anchor = hitResult.createAnchor()
                        placeObject(arFragment, anchor, selectedObject)
                    }
                }.addOnFailureListener(OnFailureListener {
                    // Handle failed download
                    // ...
                    Toast.makeText(this, "Download Failed", Toast.LENGTH_LONG).show()
                })

        }
        else {
            //Toast.makeText(this, "Model exists", Toast.LENGTH_LONG).show()
            setModelPath(localFile.path)
            arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
                //If surface is not horizontal and upward facing
                if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                    //return for the callback
                    return@setOnTapArPlaneListener
                }
                //create a new anchor
                val anchor = hitResult.createAnchor()
                placeObject(arFragment, anchor, selectedObject)
            }
        }
    }

    /***
     * function to handle the renderable object and place object in scene
     */
    private fun placeObject(fragment: ArFragment, anchor: Anchor, modelUri: Uri) {
        val modelRenderable = ModelRenderable.builder()
            .setSource((fragment.requireContext()), modelUri)
            .build()
        //when the model render is build add node to scene
        modelRenderable.thenAccept { renderableObject -> addNodeToScene(fragment, anchor, renderableObject) }
        //handle error
        modelRenderable.exceptionally {
            val toast = Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT)
            toast.show()
            null
        }
    }

    /***
     * Function to a child anchor to a new scene.
     */
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderableObject: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderableObject
        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }

    /***
     * function to get the model resource on assets directory for each figure.
     */
    private fun setModelPath(modelFileName: String) {
        selectedObject = Uri.parse(modelFileName)
        //val toast = Toast.makeText(applicationContext, modelFileName, Toast.LENGTH_SHORT)
        //toast.show()
    }
}
