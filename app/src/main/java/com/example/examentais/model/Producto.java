package com.example.examentais.model;

public class Producto {
    public String  uid;
    public String descripcion;
    public String precio;
    public String stock;
    public String direccion;


    public Producto(String uid, String descripcion, String precio, String stock, String direccion) {
        this.uid = uid;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.direccion = direccion;
    }
    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }


}
