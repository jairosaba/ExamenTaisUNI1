package com.example.examentais

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.examentais.model.Producto
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.detail_producto.view.*
import java.io.File
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProductoAdapter(private val mContext: Context, private val listaProducto: List<Producto>) : ArrayAdapter<Producto>(mContext,0,listaProducto) {
 override fun getView (position: Int, convertView: View? ,parent: ViewGroup):View{
  lateinit var storage: FirebaseStorage
  lateinit var storageReference : StorageReference
  val layout = LayoutInflater.from(mContext).inflate(R.layout.detail_producto,parent,false)
     val producto = listaProducto[position]
     storage = FirebaseStorage.getInstance();
     storageReference = storage.reference
     layout.textView.text = producto.descripcion
     layout.textView2.text= producto.stock + " un. en stock"
     layout.textView3.text= "S/. " + producto.precio

    val reference = storageReference.child("image/"+producto.direccion)
     var  localFile = File.createTempFile(producto.direccion,"jpeg")
     reference.getFile(localFile).addOnSuccessListener {
       taskSnapshot ->
      var bitmap = BitmapFactory.decodeFile(localFile.absolutePath);
      layout.imageView.setImageBitmap(bitmap)
     }


     return layout
 }
}