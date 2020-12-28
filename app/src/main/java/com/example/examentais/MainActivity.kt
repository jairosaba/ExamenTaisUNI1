package com.example.examentais

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import com.example.examentais.model.Producto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
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

        val items = arrayOf("Polo", "Camisa", "Pantalón", "Short")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,  items)
        act_categoria.threshold = 0
        act_categoria.setAdapter(adapter)
        act_categoria.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) act_categoria.showDropDown() })
        //lectura de los datos de envío
        //productoSelect = (this.intent.getSerializableExtra("Producto") as Producto?)!!
        //productoSelect =Producto("-MPaSdu1vjrafR4PGTuN","polo",null,null,"ff36b9bb-e0e7-4de8-9840-20a99107e201")
        val gson = Gson()
        productoSelect = gson.fromJson(intent.getStringExtra("Producto"), Producto::class.java)
        btnEliminar.isVisible=false;
        if (productoSelect.uid!=null){
            //cargade datos
            txtDescripcion.editText!!.setText(productoSelect.descripcion)
            txtPrecio.editText!!.setText(productoSelect.precio)
            txtStock.editText!!.setText(productoSelect.stock)
            act_categoria.setText(productoSelect.categoria)
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

            val descripcion=txtDescripcion.editText!!.text.toString()
            val precio=txtPrecio.editText!!.text.toString()
            val stock=txtStock.editText!!.text.toString()
            val categoria=til_categoria.editText!!.text.toString()
            var newId = ""
            var direccion = ""
            var ref = FirebaseDatabase.getInstance().getReference("productos")
            var exist = false

            if (productoSelect.uid!=null){
                newId = productoSelect.uid
                direccion = productoSelect.direccion
                val reference = storageReference.child("image/"+direccion)
                if (filePath!=null){
                    reference.putFile(filePath!!).addOnSuccessListener { taskSnapshot ->
                        Toast.makeText(this@MainActivity,"Acción Completada",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener( {e->
                        Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
                    })
                }


                val producto =  Producto(newId,descripcion,precio,stock,direccion, categoria)
                ref.child(newId.toString()).setValue(producto).addOnSuccessListener { taskSnapshot ->

                    Toast.makeText(this@MainActivity,"Guardado",Toast.LENGTH_SHORT).show()
                    //val intentAdd = Intent(applicationContext, ListProductos::class.java)
                    //startActivity(intentAdd)

                }.addOnFailureListener( {e->

                    Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
                }
                )
                //finish()
            }else{
                newId = ref.push().key.toString()
                direccion = UUID.randomUUID().toString()
                var ref2=FirebaseDatabase.getInstance().getReference().child("productos");
                ref2.orderByChild("descripcion").equalTo(descripcion).addValueEventListener(object :
                    ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        if(dataSnapshot.exists()) {

                        }
                        else{
                            val reference = storageReference.child("image/"+direccion)
                            if (filePath!=null){
                                reference.putFile(filePath!!).addOnSuccessListener { taskSnapshot ->
                                    Toast.makeText(this@MainActivity,"Acción Completada",Toast.LENGTH_SHORT).show()

                                }.addOnFailureListener( {e->
                                    Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
                                })
                            }


                            val producto =  Producto(newId,descripcion,precio,stock,direccion,categoria)
                            ref.child(newId.toString()).setValue(producto).addOnSuccessListener { taskSnapshot ->
                                Toast.makeText(this@MainActivity,"Guardado",Toast.LENGTH_SHORT).show()
                                //val intentAdd = Intent(applicationContext, ListProductos::class.java)
                                //startActivity(intentAdd)


                            }.addOnFailureListener( {e->
                                Toast.makeText(this@MainActivity,"Error",Toast.LENGTH_SHORT).show()
                            }
                            )
                            //finish()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }


                })
            }
        true
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
