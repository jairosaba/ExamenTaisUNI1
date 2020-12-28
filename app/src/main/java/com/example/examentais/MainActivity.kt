package com.example.examentais

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.examentais.model.Producto
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(){
    internal var filePath: Uri? = null
    private val PERMISSION_PICK_IMAGE = 1001
    lateinit var dialog : android.app.AlertDialog
    lateinit var storage: FirebaseStorage
    lateinit var storageReference : StorageReference

    lateinit var productoSelect : Producto
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = FirebaseStorage.getInstance();
        storageReference = storage.reference
        dialog = SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        //lectura de los datos de envío
        //productoSelect = (this.intent.getSerializableExtra("Producto") as Producto?)!!
        //productoSelect =Producto("-MPaSdu1vjrafR4PGTuN","polo",null,null,"ff36b9bb-e0e7-4de8-9840-20a99107e201")
        productoSelect =Producto(null,null,null,null,null)
        btnEliminar.isVisible=false;
        if (productoSelect.uid!=null){
            //cargade datos
            txtDescripcion.editText!!.setText(productoSelect.descripcion)
            txtPrecio.editText!!.setText(productoSelect.precio)
            txtStock.editText!!.setText(productoSelect.stock)
            // boton de eliminar visible
            btnEliminar.isVisible=true;
            //cargar la imagen
            val reference = storageReference.child("image/"+productoSelect.direccion)
            try {
                var  localFile = File.createTempFile(productoSelect.direccion,"jpeg")
                reference.getFile(localFile).addOnSuccessListener {
                        taskSnapshot ->
                    var bitmap = BitmapFactory.decodeFile(localFile.absolutePath);
                    imageView2.setImageBitmap(bitmap)

                }
            }
            catch (e:IOException){
                e.printStackTrace()
            }

        }
        btnEliminar.setOnClickListener ({v->
            eliminar();
        })
        btnImagen.setOnClickListener ({v->
            chooseImage();
        })
        btnGuardar.setOnClickListener ({v->
            guardar();
        })
    }
    private fun eliminar(){
        val dialog= AlertDialog.Builder(this)
        dialog.setTitle("Confirmación")
        dialog.setMessage("Confirme si desea eliminar?")
        dialog.setPositiveButton("SI"){dialogInterface,i->
            var ref = FirebaseDatabase.getInstance().getReference("productos")
            var newId =  productoSelect.uid
            var direccion = productoSelect.direccion
            ref.child(newId.toString()).removeValue().addOnSuccessListener { taskSnapshot ->
                Toast.makeText(this@MainActivity,"Producto",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener( {e->
                Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
            }
            )
            val reference = storageReference.child("image/"+direccion)
            reference.delete().addOnSuccessListener { taskSnapshot ->
                Toast.makeText(this@MainActivity,"Foto eliminada",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener( {e->
                Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
            })

            finish()
        }
        dialog.setNegativeButton("NO"){dialogInterface,i->
            dialogInterface.dismiss()
        }
        dialog.show()
        true
    }
    private fun guardar(){
        if (filePath!=null){
            val descripcion=txtDescripcion.editText!!.text.toString()
            val precio=txtPrecio.editText!!.text.toString()
            val stock=txtStock.editText!!.text.toString()
            var newId = ""
            var direccion = ""
            var ref = FirebaseDatabase.getInstance().getReference("productos")
            if (productoSelect.uid!=null){
                newId = productoSelect.uid
                direccion = productoSelect.direccion
            }else{
                newId = ref.push().key.toString()
                direccion = UUID.randomUUID().toString()
            }

            val reference = storageReference.child("image/"+direccion)
            reference.putFile(filePath!!).addOnSuccessListener { taskSnapshot ->
                dialog.dismiss()
                Toast.makeText(this@MainActivity,"Acción Completada",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener( {e->
                dialog.dismiss()
                Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
            })

            val producto =  Producto(newId,descripcion,precio,stock,direccion)
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
