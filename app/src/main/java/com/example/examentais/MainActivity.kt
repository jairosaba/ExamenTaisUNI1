package com.example.examentais

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.examentais.model.Producto
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import dmax.dialog.SpotsDialog
import java.io.IOException
import java.util.*
import java.util.jar.Manifest


class MainActivity : AppCompatActivity(){
    internal var filePath: Uri? = null
    private val PERMISSION_PICK_IMAGE = 1001
    lateinit var dialog : android.app.AlertDialog
    lateinit var storage: FirebaseStorage
    lateinit var storageReference : StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = FirebaseStorage.getInstance();
        storageReference = storage.reference

        dialog = SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        btnImagen.setOnClickListener ({v->
            chooseImage();
        })
        btnGuardar.setOnClickListener ({v->
            guardar();
        })
    }
    private fun guardar(){
        if (filePath!=null){
            val descripcion=txtDescripcion.editText!!.text.toString()
            val precio=txtPrecio.editText!!.text.toString()
            val stock=txtStock.editText!!.text.toString()
            val ref = FirebaseDatabase.getInstance().getReference("productos")
            val newId = ref.push().key
            //filepath tiene el archivo pero no se como hacer para insertar eso
            val producto =  Producto(newId,descripcion,precio,stock)
            /*
            reference.putFile(filePath!!).addOnSuccessListener { taskSnapshot ->
                dialog.dismiss()
                Toast.makeText(this@MainActivity,"Guardado",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener( {e->
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
                }
            */
            ref.child(newId.toString()).setValue(producto).addOnSuccessListener { taskSnapshot ->
                dialog.dismiss()
                Toast.makeText(this@MainActivity,"Guardado",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener( {e->
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
                }
            )

        }
    }
    private fun validar(){

    }
    private fun limpiarMantenedor(){

    }
    private fun chooseImage(){

        Dexter.withActivity(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object :
            MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()){
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type=  "image/*"
                    startActivityForResult(Intent.createChooser(intent,"Escoja la imagen"),PERMISSION_PICK_IMAGE)
                }
                else{
                    Toast.makeText(this@MainActivity, "Permiso no concedido",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                 p1!!.continuePermissionRequest()
            }
        }
        ).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode==PERMISSION_PICK_IMAGE){
                    if (data != null){
                        if (data.data !=null){
                            filePath= data.data
                            try{
                                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,filePath)
                                imageView2.setImageBitmap(bitmap)
                            }
                            catch (e:IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }
