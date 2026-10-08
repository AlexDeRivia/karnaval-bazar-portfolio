package com.karnaval.entidad;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "compras")
@Getter
@Setter
public class Compra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "idProveedor", 
			referencedColumnName = "id", 
			foreignKey = @ForeignKey(name = "fk_proveedor_compras"), 
			nullable = false)
	@NotNull(message = "Selecciona un proveedor")
	private Proveedor proveedor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "idEmpleado", 
			referencedColumnName = "id", 
			foreignKey = @ForeignKey(name = "fk_empleado_compras"),
			nullable = false)
	@NotNull(message = "Selecciona un empleado")
	private Empleado empleado;

	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull(message = "Selecciona una fecha")
	private Date fecha;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "compra", cascade = CascadeType.ALL)
    @Valid
    @NotEmpty(message = "Agrega al menos un producto a la compra")
    private List<CompraDetalle> compraDetalles;
}
