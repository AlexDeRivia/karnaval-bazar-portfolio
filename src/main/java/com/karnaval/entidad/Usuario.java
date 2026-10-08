package com.karnaval.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@NotBlank(message = "El nombre debe tener al menos una letra")
	private String nombre;

	@Column(nullable = false)
	@NotBlank(message = "El password no debe estar en blanco")
	private String clave;

	@Column(nullable = false)
	private String rol;

	@Column(nullable = false)
	private int estado;

	public Usuario(String nombre, String clave, String rol, int estado) {
		super();
		this.nombre = nombre;
		this.clave = clave;
		this.rol = rol;
		this.estado = estado;
	}
}
