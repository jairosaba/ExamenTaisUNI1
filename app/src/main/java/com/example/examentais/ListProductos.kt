package com.example.examentais

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.examentais.model.Producto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_list_productos.*
import kotlinx.android.synthetic.main.activity_main.*

class ListProductos : AppCompatActivity() {
    private val listProducts:MutableList<Producto> = ArrayList()

    private lateinit var messagesListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_productos)
        val database = Firebase.database
        val myRef = database.getReference("productos")
        myRef.addValueEventListener( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProducts.clear()
                snapshot.children.forEach { child ->
                    val producto: Producto? =
                        Producto(
                            child.child("uid").getValue<String>() as String?,
                            child.child("descripcion").getValue<String>() as String?,
                            child.child("precio").getValue<String>() as String?,
                            child.child("stock").getValue<String>() as String?,
                            child.child("direccion").getValue<String>() as String?,
                                child.child("categoria").getValue<String>() as String?
                        )
                    producto?.let { listProducts.add(producto) }
                }
                val adapter = ProductoAdapter(this@ListProductos,listProducts)
                listProductos.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        })

        listProductos.setOnItemClickListener { parent, view, position, id ->
            val customer =   listProductos.getItemAtPosition(position)
            //val prod = Producto(null,null,null,null,null)
            val gson = Gson()
            val myJson: String = gson.toJson(customer)
            val intentAdd = Intent(applicationContext, MainActivity::class.java)
            intentAdd.putExtra("Producto",myJson)
            startActivity(intentAdd)

        }
        fabBottomBar.setOnClickListener ({v->
            val prod = Producto(null,null,null,null,null, null)
            val gson = Gson()
            val myJson: String = gson.toJson(prod)
            val intentAdd = Intent(applicationContext, MainActivity::class.java)
            intentAdd.putExtra("Producto",myJson)
            startActivity(intentAdd)
        })

    }
    override fun onResume() {
        super.onResume()
        val database = Firebase.database
        val myRef = database.getReference("productos")
        myRef.addValueEventListener( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProducts.clear()
                snapshot.children.forEach { child ->
                    val producto: Producto? =
                        Producto(
                            child.child("uid").getValue<String>() as String?,
                            child.child("descripcion").getValue<String>() as String?,
                            child.child("precio").getValue<String>() as String?,
                            child.child("stock").getValue<String>() as String?,
                            child.child("direccion").getValue<String>() as String?,
                                child.child("categoria").getValue<String>() as String?
                        )
                    producto?.let { listProducts.add(producto) }
                }
                val adapter = ProductoAdapter(this@ListProductos,listProducts)
                listProductos.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        })
    }
}