package com.karnaval.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "proveedores")
@Data
public class Proveedor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 11, nullable = false, unique = true)
	@NotBlank(message = "El RUC no puede estar en blanco")
	@Size(min = 11, max = 11, message = "El RUC debe tener 11 caracteres")
	private String ruc;

	@Column(length = 35, nullable = false)
	@NotBlank(message = "La razon social no puede estar en blanco")
	@Size(min = 2, max = 255, message = "La razon social debe tener entre 2 y 255 caracteres")
	private String razonSocial;

	@Column(length = 15, nullable = true, unique = true)
	private String telefono;

	@Column(length = 80)
	private String direccion;
	
	public Proveedor() {
	}
	
	public Proveedor(String ruc, String razonSocial, String telefono, String direccion) {
	    this.ruc = ruc;
	    this.razonSocial = razonSocial;
	    this.telefono = telefono;
	    this.direccion = direccion;
	}
	
	
}
