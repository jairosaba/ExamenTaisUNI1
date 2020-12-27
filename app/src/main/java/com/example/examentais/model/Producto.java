package com.example.examentais.model;

public class Producto {
    public String  uid;
    public String descripcion;
    public String precio;
    public String stock;

    public Producto(String uid, String descripcion, String precio, String stock) {
        this.uid = uid;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
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
