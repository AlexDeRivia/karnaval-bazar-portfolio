package com.karnaval.entidad;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "empleados")
@Data
public class Empleado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 8, nullable = false, unique = true)
	@NotBlank(message = "El DNI no puede estar en blanco")
	@Size(min = 8, max = 8, message = "El DNI debe tener 8 caracteres")
	private String dni;

	@Column(length = 35, nullable = false)
	@NotBlank(message = "El apellido paterno no puede estar en blanco")
	@Size(min = 2, max = 35, message = "El apellido debe tener entre 2 y 35 caracteres")
	private String apellidoPaterno;

	@Column(length = 35, nullable = false)
	@NotBlank(message = "El apellido materno no puede estar en blanco")
	@Size(min = 2, max = 35, message = "El apellido debe tener entre 2 y 35 caracteres")
	private String apellidoMaterno;

	@Column(length = 35, nullable = false)
	@NotBlank(message = "El nombre no puede estar en blanco")
	@Size(min = 2, max = 35, message = "El nombre debe tener entre 2 y 35 caracteres")
	private String nombres;

	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Past
	private Date fechaNacimiento;

	@Column(length = 9, nullable = true, unique = true)
	@NotBlank(message = "El celular no puede estar en blanco")
	@Size(min = 9, max = 9, message = "El celular debe tener 9 caracteres")
	private String celular;

	@Column(length = 80)
	@NotBlank(message = "El eMail no puede estar vacio")
	@Email(message = "Debe ingresar un correo vélido")
	private String eMail;

	@Column(length = 80)
	private String direccion;
	
	public Empleado(String dni, String apellidoPaterno, String apellidoMaterno, String nombres, Date fechaNacimiento, String celular, String eMail, String direccion) {
	    this.dni = dni;
	    this.apellidoPaterno = apellidoPaterno;
	    this.apellidoMaterno = apellidoMaterno;
	    this.nombres = nombres;
	    this.fechaNacimiento = fechaNacimiento;
	    this.celular = celular;
	    this.eMail = eMail;
	    this.direccion = direccion;
	}

	public Empleado() {
		// TODO Auto-generated constructor stub
	}
}
